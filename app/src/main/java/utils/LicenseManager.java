package utils;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LicenseManager {

    static final String URL = "https://mclicense.stonexpositioning.com";

    public interface LicenseManagerCallback {
        void onSuccess(String response);

        void onError(Exception e);
    }

    public static void checkLicense(String serialNumber, String license, LicenseManagerCallback callback) {

        OkHttpClient client = new OkHttpClient();

        String urlApi = URL + "/api/apollo/claimLicense";

        String json = "{"
                + "\"serialNumber\":\"" + serialNumber + "\","
                + "\"license\":\"" + license + "\""
                + "}";

        RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(urlApi)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (callback == null) return;

                if (response.isSuccessful()) {
                    try {
                        callback.onSuccess(response.body().string());
                    } catch (IOException e) {
                    }
                } else {
                    callback.onError(new IOException("HTTP error: " + response.code() + " - " + response.message()));
                }
            }
        });
    }

    public static void resetLicense(String serialNumber, String type, LicenseManagerCallback callback) {

        OkHttpClient client = new OkHttpClient();

        String urlApi = URL + "/api/apollo/resetLicense";

        String json = "{"
                + "\"serialNumber\":\"" + serialNumber + "\","
                + "\"type\":\"" + type + "\""
                + "}";

        RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(urlApi)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (callback == null) return;

                if (response.isSuccessful()) {
                    try {
                        callback.onSuccess(response.body().string());
                    } catch (IOException e) {
                    }
                } else {
                    callback.onError(new IOException("HTTP error: " + response.code() + " - " + response.message()));
                }
            }
        });
    }

    public static void getLicense(String serialNumber, LicenseManagerCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String urlApi = URL + "/api/apollo/getLicense";

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(urlApi))
                .newBuilder()
                .addQueryParameter("serialNumber", serialNumber)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (callback == null) return;

                if (response.isSuccessful()) {
                    try {
                        callback.onSuccess(response.body().string());
                    } catch (IOException e) {
                    }
                } else {
                    callback.onError(new IOException("HTTP error: " + response.code() + " - " + response.message()));
                }
            }
        });
    }

    public static void getRestoreCode(String serialNumber, LicenseManagerCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String urlApi = URL + "/api/apollo/getRestoreCode";

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(urlApi))
                .newBuilder()
                .addQueryParameter("serialNumber", serialNumber)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (callback == null) return;

                if (response.isSuccessful()) {
                    try {
                        callback.onSuccess(response.body().string());
                    } catch (IOException e) {
                    }
                } else {
                    callback.onError(new IOException("HTTP error: " + response.code() + " - " + response.message()));
                }
            }
        });
    }
}
