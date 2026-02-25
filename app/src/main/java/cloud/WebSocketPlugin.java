package cloud;

import static gui.MyApp.activationCode;
import static gui.MyApp.folderPath;
import static gui.MyApp.isApollo;
import static gui.MyApp.licenseType;
import static gui.MyApp.restoreCode;
import static utils.MyTypes.MC_3D_PRO_AUTO;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import gui.MyApp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import utils.MyData;
import utils.MyDeviceManager;


public class WebSocketPlugin {

    // ---- Public state ----
    public static volatile boolean isAuthenticated;

    // ---- URLs ----
    private static final String WS_URL_IT = "wss://licensemc.stonexpositioning.com/api/v1/ws";
    private static final String WS_URL_US = "wss://licensemc-us.stonexpositioning.com/api/v1/ws";

    // ---- Crypto ----
    private static final String SECRET_KEY_BASE64 = "Q6E2ZK3g1/XSO4VXxMGNehYmQUaJv8+M26j+xqlsgFs=";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ---- Singleton ----
    private static volatile WebSocketPlugin instance;

    public static WebSocketPlugin getWebSocketPluginInstance(Context context) {
        if (instance == null) {
            synchronized (WebSocketPlugin.class) {
                if (instance == null)
                    instance = new WebSocketPlugin(context.getApplicationContext());
            }
        }
        return instance;
    }

    // ---- Internals ----
    private enum Region {IT, US}

    private final Context appContext;
    private final OkHttpClient client;
    private final ScheduledExecutorService scheduler;

    private volatile WebSocket activeWs;
    private volatile Region activeRegion = Region.IT;


    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private final AtomicBoolean authed = new AtomicBoolean(false);


    private static final int MAX_TRIES_PER_REGION = 3;
    private final AtomicInteger triesOnCurrentRegion = new AtomicInteger(0);

    private static final int AUTH_TIMEOUT_MS = 7000;

    private volatile ScheduledFuture<?> authTimeoutFuture;

    private final Object wsLock = new Object();

    private final S3ManagerSingleton s3ManagerSingleton;

    private WebSocketPlugin(Context context) {
        this.appContext = context;
        this.client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.s3ManagerSingleton = S3ManagerSingleton.getInstance(context);
    }


    public void start() {
        if (!isApollo) {
            parseCode("");
            return;
        }

        if (authed.get()) return;

        if (!connecting.compareAndSet(false, true)) return;

        // reset state
        isAuthenticated = false;
        triesOnCurrentRegion.set(0);
        activeRegion = Region.IT;

        closeActiveWs();
        connect(activeRegion);
    }


    public void sendCommand(String command, Map<String, Object> payload) {
        WebSocket ws = activeWs;
        if (ws == null || !authed.get()) {
            Log.e("WebSocketPlugin", "WebSocket not authenticated/connected.");
            return;
        }

        try {
            JSONObject message = new JSONObject();
            message.put("type", command);
            message.put("timeStamp", System.currentTimeMillis());

            if (payload != null) {
                for (Map.Entry<String, Object> entry : payload.entrySet()) {
                    message.put(entry.getKey(), entry.getValue());
                }
            }

            boolean ok = ws.send(message.toString());
            if (!ok) Log.e("WebSocketPlugin", "sendCommand failed (send returned false).");
        } catch (Exception e) {
            Log.e("WebSocketPlugin", "sendCommand exception: " + e.getMessage());
        }
    }


    public void stop() {
        authed.set(false);
        isAuthenticated = false;
        connecting.set(false);
        cancelAuthTimeout();
        closeActiveWs();

        // scheduler.shutdownNow();
    }

    private void connect(Region region) {
        activeRegion = region;

        Request request = new Request.Builder().url(region == Region.IT ? WS_URL_IT : WS_URL_US).build();

        WebSocket ws = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                synchronized (wsLock) {
                    activeWs = webSocket;
                }

                scheduleAuthTimeout(region, webSocket);

                try {
                    JSONObject payload = new JSONObject();
                    payload.put("serial_number", MyDeviceManager.getDeviceSN(MyApp.visibleActivity));
                    payload.put("mac_address", MyDeviceManager.getMacAddress(MyApp.visibleActivity).trim().toLowerCase());
                    payload.put("firmware", MyDeviceManager.getBuildVersion(MyApp.visibleActivity).trim().toUpperCase());
                    payload.put("timeStamp", System.currentTimeMillis());

                    JSONObject encryptedMessage = encryptAndSign(payload);

                    JSONObject message = new JSONObject();
                    message.put("type", "auth");
                    message.put("iv", encryptedMessage.getString("iv"));
                    message.put("encrypted", encryptedMessage.getString("encrypted"));
                    message.put("hmac", encryptedMessage.getString("hmac"));

                    webSocket.send(message.toString());
                } catch (Exception e) {
                    Log.e("WebSocketPlugin", "onOpen auth build/send error: " + e.getMessage());
                    // fallback
                    failAndFallback(region, webSocket);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject response = new JSONObject(text);
                    String status = response.optString("status");

                    switch (status) {
                        case "authenticated": {
                            // stop timeout
                            cancelAuthTimeout();

                            authed.set(true);
                            isAuthenticated = true;
                            connecting.set(false);

                            // donne la socket au S3 manager
                            s3ManagerSingleton.setWebSocket(webSocket);

                            JSONObject update = new JSONObject();
                            update.put("type", "check_updates");
                            update.put("timeStamp", System.currentTimeMillis());
                            update.put("licenceID", activationCode);
                            webSocket.send(update.toString());
                            break;
                        }

                        case "update_available": {
                            JSONObject data = response.optJSONObject("data");
                            if (data != null) {
                                decryptAndAcknowledge(webSocket, data);
                            } else {
                                cancellaJson();
                                delayedSend(webSocket, new JSONObject().put("status", "restore_ack").put("type", "restore_ack").put("activated", true).put("timeStamp", System.currentTimeMillis()), 2000);
                            }
                            break;
                        }

                        case "no_updates":
                            // rien
                            break;

                        case "command":
                            handleCommand(webSocket, response);
                            break;

                        default:
                            Log.e("WebSocketPlugin", "Unrecognized response: " + text);
                            break;
                    }

                } catch (Exception e) {
                    Log.e("WebSocketPlugin", "onMessage parse error: " + e.getMessage());
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                if (!authed.get()) {
                    Log.e("WebSocketPlugin", "onClosed before auth. code=" + code + " reason=" + reason);
                    failAndFallback(region, webSocket);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocketPlugin", "onFailure (" + region + "): " + (t != null ? t.getMessage() : "null"));
                if (!authed.get()) {
                    failAndFallback(region, webSocket);
                } else {
                    authed.set(false);
                    isAuthenticated = false;
                    connecting.set(false);
                    cancelAuthTimeout();
                }
            }
        });

        synchronized (wsLock) {
            activeWs = ws;
        }
    }

    private void failAndFallback(Region region, WebSocket ws) {
        cancelAuthTimeout();
        safeClose(ws);

        if (authed.get()) return;

        int tries = triesOnCurrentRegion.incrementAndGet();

        if (tries < MAX_TRIES_PER_REGION) {
            scheduleConnect(region, 3000);
            return;
        }

        triesOnCurrentRegion.set(0);

        if (region == Region.IT) {
            scheduleConnect(Region.US, 0);
        } else {
            scheduleConnect(Region.IT, 5000);
        }
    }

    private void scheduleConnect(Region region, int delayMs) {
        scheduler.schedule(() -> {
            if (authed.get()) {
                connecting.set(false);
                return;
            }
            connect(region);
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private void scheduleAuthTimeout(Region region, WebSocket ws) {
        cancelAuthTimeout();
        authTimeoutFuture = scheduler.schedule(() -> {
            if (authed.get()) return;
            Log.e("WebSocketPlugin", "AUTH TIMEOUT (" + region + ")");
            failAndFallback(region, ws);
        }, AUTH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    private void cancelAuthTimeout() {
        ScheduledFuture<?> f = authTimeoutFuture;
        if (f != null) {
            f.cancel(false);
            authTimeoutFuture = null;
        }
    }

    private void closeActiveWs() {
        WebSocket ws;
        synchronized (wsLock) {
            ws = activeWs;
            activeWs = null;
        }
        safeClose(ws);
    }

    private void safeClose(WebSocket ws) {
        if (ws == null) return;
        try {
            ws.close(1000, "closing");
        } catch (Exception ignored) {
        }
    }

    // =========================
    // Crypto / Protocol
    // =========================

    private JSONObject encryptAndSign(JSONObject data) throws Exception {
        byte[] iv = new byte[12];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        byte[] secretKey = Base64.decode(SECRET_KEY_BASE64, Base64.DEFAULT);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] plaintext = data.toString().getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = cipher.doFinal(plaintext);

        byte[] hmac = hmacSHA256(secretKey, concat(iv, encrypted));

        JSONObject result = new JSONObject();
        result.put("iv", Base64.encodeToString(iv, Base64.NO_WRAP));
        result.put("encrypted", Base64.encodeToString(encrypted, Base64.NO_WRAP));
        result.put("hmac", Base64.encodeToString(hmac, Base64.NO_WRAP));
        return result;
    }

    private void decryptAndAcknowledge(WebSocket ws, JSONObject data) throws Exception {
        String ivBase64 = data.getString("iv");
        String encryptedBase64 = data.getString("encrypted");

        byte[] iv = Base64.decode(ivBase64, Base64.DEFAULT);
        byte[] encrypted = Base64.decode(encryptedBase64, Base64.DEFAULT);

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest((MyDeviceManager.getDeviceSN(MyApp.visibleActivity) + "9448d53e2f962c3ab6bd6a50dc48427a").getBytes());

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);

        String stringa = new String(decrypted, StandardCharsets.UTF_8);
        parseCode(stringa);

        delayedSend(ws, new JSONObject().put("status", "activation_ack").put("type", "activation_ack").put("activated", true).put("timeStamp", System.currentTimeMillis()), 2000);
    }

    private void handleCommand(WebSocket ws, JSONObject response) throws Exception {
        isAuthenticated = false;

        String type = response.optString("type");
        JSONObject license = response.optJSONObject("license");

        if ("activate SW license".equals(type) && license != null) {
            decryptAndAcknowledge(ws, license);
            delayedSend(ws, new JSONObject().put("status", "ack").put("type", "activation SW").put("activated", true).put("timeStamp", System.currentTimeMillis()), 2000);

        } else if ("restore SW license".equals(type)) {
            Log.e("WebSocketPlugin", "Restoring license...");
            cancellaJson();
            delayedSend(ws, new JSONObject().put("status", "ack").put("type", "restore SW").put("restored", true).put("timeStamp", System.currentTimeMillis()), 2000);

        } else if ("temp_credentials".equals(type)) {
            JSONObject credentials = response.optJSONObject("data");
            isAuthenticated = true;

            if (credentials == null) return;

            s3ManagerSingleton.setS3Credentials(credentials.getString("region"), credentials.getString("accessKeyId"), credentials.getString("secretAccessKey"), credentials.getString("sessionToken"), credentials.getString("bucketName"), credentials.getLong("expiration"));

            delayedSend(ws, new JSONObject().put("status", "ack").put("type", "temp_credentials_ack").put("saved", true).put("timeStamp", System.currentTimeMillis()), 2000);
        }
    }

    private byte[] hmacSHA256(byte[] key, byte[] message) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret = new SecretKeySpec(key, "HmacSHA256");
        hmac.init(secret);
        return hmac.doFinal(message);
    }

    private byte[] concat(byte[] a, byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(a.length + b.length);
        buffer.put(a);
        buffer.put(b);
        return buffer.array();
    }


    private void delayedSend(WebSocket ws, JSONObject json, int delayMillis) {
        scheduler.schedule(() -> {
            try {
                ws.send(json.toString());
            } catch (Exception ignored) {
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    public void parseCode(String jsonString) {
        if (isApollo) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);

                activationCode = jsonObject.getString("activationCode");
                MyData.push("licenza", activationCode);

                restoreCode = jsonObject.getString("restoreCode");
                String deviceSN = jsonObject.getString("deviceSN");
                licenseType = jsonObject.getInt("licenseType");
                String userID = jsonObject.getString("userID");
                String category = jsonObject.getString("category");
                long timestamp = jsonObject.getLong("timestamp");
                MyApp.expiry = jsonObject.getString("expiry");

                String pathL = Environment.getExternalStorageDirectory().toString() + folderPath + "/Config";
                File directory = new File(pathL);
                if (!directory.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    directory.mkdirs();
                }

                File outputFile = new File(directory, "License.json");

                FileWriter writer = new FileWriter(outputFile);
                writer.write(jsonObject.toString());
                writer.close();

            } catch (JSONException e) {
                activationCode = "none";
                restoreCode = "none";
                licenseType = -1;
            } catch (IOException e) {
                Log.e("WebSocketPlugin", "parseCode IO: " + e.getMessage());
            }
        } else {
            activationCode = "0123456789";
            licenseType = MC_3D_PRO_AUTO;
            MyData.push("licenza", activationCode);
            Log.d("WebSocketPlugin", licenseType + "  " + activationCode);
        }
    }

    public void cancellaJson() {
        try {
            String pathL = Environment.getExternalStorageDirectory().toString() + folderPath + "/Config/License.json";
            File myFile = new File(pathL);
            //noinspection ResultOfMethodCallIgnored
            myFile.delete();
        } catch (Exception e) {
            Log.e("WebSocketPlugin", "cancellaJson: " + e.getMessage());
        }
    }
}