package cloud;

import static gui.MyApp.activationCode;
import static gui.MyApp.folderPath;
import static gui.MyApp.licenseType;
import static gui.MyApp.restoreCode;

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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import gui.MyApp;
import gui.projects.Remote_Activity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import utils.MyData;
import utils.MyDeviceManager;

public class WebSocketPlugin {
    private WebSocket webSocket;

    private static final String WS_URL = "wss://licensemc.stonexpositioning.com/api/v1/ws";
    private static final String SECRET_KEY_BASE64 = "Q6E2ZK3g1/XSO4VXxMGNehYmQUaJv8+M26j+xqlsgFs=";

    private static final String DEVICE_SN = MyDeviceManager.getDeviceSN(MyApp.visibleActivity);//passare

    private static final String MAC_ADDRESS = MyDeviceManager.getMacAddress(MyApp.visibleActivity).trim();//passare

    private static S3ManagerSingleton s3ManagerSingleton;

    private static WebSocketPlugin instance;

    private final int MAX_TRY_CONNECTION = 3;
    private int currentTry = 0;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private WebSocketPlugin() {
    }

    public static WebSocketPlugin getWebSocketPluginInstance(Context context) {
        if (instance == null) {
            synchronized (WebSocketPlugin.class) {
                instance = new WebSocketPlugin();

            }
        }
        s3ManagerSingleton = S3ManagerSingleton.getInstance(context);
        return instance;
    }

    public void start() {

        Log.d("TestM", "Start..");
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();

        Request request = new Request.Builder().url(WS_URL).build();

        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                webSocket=ws;
                currentTry = 0;
                Log.d("TestM", "Connected to WebSocket server.");
                try {

                    JSONObject payload = new JSONObject();
                    payload.put("serial_number", DEVICE_SN);
                    payload.put("mac_address", MAC_ADDRESS);
                    payload.put("timeStamp", System.currentTimeMillis());

                    JSONObject encryptedMessage = encryptAndSign(payload);

                    JSONObject message = new JSONObject();
                    message.put("type", "auth");
                    message.put("iv", encryptedMessage.getString("iv"));
                    message.put("encrypted", encryptedMessage.getString("encrypted"));
                    message.put("hmac", encryptedMessage.getString("hmac"));

                    Log.d("Auth", message.toString());
                    ws.send(message.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d("TestM", "Server response: " + text);
                try {
                    JSONObject response = new JSONObject(text);
                    String status = response.optString("status");

                    switch (status) {
                        case "authenticated":
                            Log.d("TestM", "Authentication successful. Sending update request...");

                            JSONObject update = new JSONObject();
                            update.put("type", "check_updates");
                            update.put("timeStamp", System.currentTimeMillis());
                            update.put("licenceID", activationCode);
                            Log.d("TYestSend", update.toString());
                            ws.send(update.toString());
                            s3ManagerSingleton.setWebSocket(ws);

                            break;

                        case "update_available":
                            JSONObject data = response.optJSONObject("data");
                            if (data != null) {
                                Log.d("RRR", "In IF");
                                decryptAndAcknowledge(ws, data);
                                Log.d("RRR", "Dopo IF");
                            } else {
                                // da sostituire con codice di revoke
                                delayedSend(ws, new JSONObject().put("status", "restore_ack").put("type", "restore_ack").put("activated", true).put("timeStamp", System.currentTimeMillis()), 2000);
                            }

                            break;

                        case "no_updates":
                            Log.d("TestM", "No update available for the device.");
                            break;

                        case "command":
                            handleCommand(ws, response);
                            Log.d("TestM", response.toString());
                            break;

                        default:
                            Log.d("TestM", "Unrecognized response: " + text);
                            break;
                    }
                } catch (Exception e) {
                    Log.e("TestM", "Error reading response: " + e.getMessage());
                }
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d("TestM", "Connection closed.");
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                webSocket=null;
                s3ManagerSingleton.shutdown();
                Log.d("TestM", "WebSocket error: " + t.getMessage());

                // have to retry max 3 times
                if (currentTry < MAX_TRY_CONNECTION) {
                    try {
                        Thread.sleep(3000);
                        currentTry++;
                        start();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        });
    }

    public void sendCommand(String command, Map<String, Object> payload) {
        if (this.webSocket == null) {
            System.err.println("WebSocket is not connected.");
            return;
        }

        JSONObject message = new JSONObject();
        try {
            message.put("type", command);
            message.put("timeStamp", System.currentTimeMillis());

            if (payload != null) {
                for (Map.Entry<String, Object> entry : payload.entrySet()) {
                    message.put(entry.getKey(), entry.getValue());
                }
            }

            webSocket.send(message.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        byte[] key = sha.digest((DEVICE_SN + "9448d53e2f962c3ab6bd6a50dc48427a").getBytes());

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);

        String stringa = new String(decrypted, StandardCharsets.UTF_8);
        parseCode(stringa);

        Log.w("TestM", "Decrypted data: " + new String(decrypted, StandardCharsets.UTF_8));

        delayedSend(ws, new JSONObject().put("status", "activation_ack").put("type", "activation_ack").put("activated", true).put("timeStamp", System.currentTimeMillis()), 2000);
    }

    private void handleCommand(WebSocket ws, JSONObject response) throws Exception {
        Log.d("MyResponse", response.toString());
        Remote_Activity.isAuthenticated = false;
        String type = response.optString("type");
        JSONObject license = response.optJSONObject("license");

        if ("activate SW license".equals(type) && license != null) {
            decryptAndAcknowledge(ws, license);
            delayedSend(ws,
                    new JSONObject().put("status", "ack")
                            .put("type", "activation SW")
                            .put("activated", true)
                            .put("timeStamp", System.currentTimeMillis()), 2000);

        } else if ("restore SW license".equals(type)) {
            Log.d("TestM", "Restoring license...");
            delayedSend(ws,
                    new JSONObject().put("status", "ack")
                            .put("type", "restore SW")
                            .put("restored", true)
                            .put("timeStamp", System.currentTimeMillis()), 2000);
        } else if ("temp_credentials".equals(type)) {
            JSONObject credentials = response.optJSONObject("data");
            Log.d("TestM", "Received temp credentials: " + credentials);
            Remote_Activity.isAuthenticated = true;
            if (credentials == null) {
                return;
            }
            Log.d("TestM", "Setting AWS credentials...");
            s3ManagerSingleton.setS3Credentials(
                    credentials.getString("region"),
                    credentials.getString("accessKeyId"),
                    credentials.getString("secretAccessKey"),
                    credentials.getString("sessionToken"),
                    credentials.getString("bucketName"),
                    credentials.getLong("expiration")
            );
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
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ws.send(json.toString());
            }
        }, delayMillis);
    }

    public void parseCode(String jsonString) {
        try {
            // Crea oggetto JSON
            JSONObject jsonObject = new JSONObject(jsonString);

            // Estrae e popola i campi
            activationCode = jsonObject.getString("activationCode");
            MyData.push("licenza", activationCode);
            restoreCode = jsonObject.getString("restoreCode");
            String deviceSN = jsonObject.getString("deviceSN");
            licenseType = jsonObject.getInt("licenseType");
            String userID = jsonObject.getString("userID");
            String category = jsonObject.getString("category");
            long timestamp = jsonObject.getLong("timestamp");
            MyApp.expiry = jsonObject.getString("expiry");

            // Percorso della cartella
            String pathL = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines";

            // Crea la cartella se non esiste
            File directory = new File(pathL);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // File di output JSON
            File outputFile = new File(directory, "License.json");

            // Scrive direttamente il JSON nel file
            FileWriter writer = new FileWriter(outputFile);
            writer.write(jsonObject.toString()); // salva il JSON intero
            writer.close();


        } catch (JSONException e) {
            activationCode = "none";
            restoreCode = "none";
            licenseType = -1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


