package cloud;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import utils.MyDeviceManager;

public class WebSocketPlugin {

    private static final String WS_URL = "wss://licensemc.stonexpositioning.com/api/v1/ws";
    private static final String SECRET_KEY_BASE64 = "Q6E2ZK3g1/XSO4VXxMGNehYmQUaJv8+M26j+xqlsgFs=";

    private static final String DEVICE_SN = MyDeviceManager.getDeviceSN(MyApp.visibleActivity);//passare

    private static final String MAC_ADDRESS = MyDeviceManager.getMacAddress(MyApp.visibleActivity).trim();//passare

    private static S3ManagerSingleton s3ManagerSingleton;

    private static WebSocketPlugin instance;

    private final int MAX_TRY_CONNECTION = 3;
    private  int currentTry = 0;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private WebSocketPlugin() {}

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

        Log.d("TestM","Start..");
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();

        Request request = new Request.Builder().url(WS_URL).build();

        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                currentTry = 0;
                System.out.println("Connected to WebSocket server.");
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

                    Log.d("Auth",payload.toString());
                    ws.send(message.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                System.out.println("Server response: " + text);
                try {
                    JSONObject response = new JSONObject(text);
                    String status = response.optString("status");

                    switch (status) {
                        case "authenticated":
                            System.out.println("Authentication successful. Sending update request...");

                            JSONObject update = new JSONObject();
                            update.put("type", "check_updates");
                            update.put("timeStamp", System.currentTimeMillis());
                            update.put("licenceID", "none");//activation code
                            ws.send(update.toString());

                            s3ManagerSingleton.setWebSocket(ws);

                            break;

                        case "update_available":
                            JSONObject data = response.optJSONObject("data");
                            if (data != null) {
                                decryptAndAcknowledge(ws, data);

                            } else {
                                // da sostituire con codice di revoke
                                delayedSend(ws, new JSONObject().put("status", "restore_ack").put("type", "restore_ack").put("activated", true).put("timeStamp", System.currentTimeMillis()), 2000);
                            }

                            break;

                        case "no_updates":
                            System.out.println("No update available for the device.");
                            break;

                        case "command":
                            handleCommand(ws, response);
                            break;

                        default:
                            System.out.println("Unrecognized response: " + text);
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error reading response: " + e.getMessage());
                }
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                System.out.println("Connection closed.");
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                s3ManagerSingleton.shutdown();
                System.err.println("WebSocket error: " + t.getMessage());

                // have to retry max 3 times
                if(currentTry < MAX_TRY_CONNECTION) {
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

        System.out.println("Decrypted data: " + new String(decrypted, StandardCharsets.UTF_8));

        delayedSend(ws, new JSONObject().put("status", "activation_ack").put("type", "activation_ack").put("activated", true).put("timeStamp", System.currentTimeMillis()), 2000);
    }

    private void handleCommand(WebSocket ws, JSONObject response) throws Exception {
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
            System.out.println("Restoring license...");
            delayedSend(ws,
                    new JSONObject().put("status", "ack")
                            .put("type", "restore SW")
                            .put("restored", true)
                            .put("timeStamp", System.currentTimeMillis()), 2000);
        }

        else if ("temp_credentials".equals(type)) {
            JSONObject credentials = response.optJSONObject("data");
            System.out.println("Received temp credentials: " + credentials);
            if (credentials == null) {
                return;
            }
            System.out.println("Setting AWS credentials...");
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


}

