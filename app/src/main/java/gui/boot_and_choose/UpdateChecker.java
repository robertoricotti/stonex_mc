package gui.boot_and_choose;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateChecker {

    public interface Callback {
        void onResult(UpdateInfo info);
        void onError(Exception e);
    }

    public static class UpdateInfo {
        public boolean updateAvailable;
        public long remoteVersionCode;
        public String remoteVersionName;
        public long localVersionCode;
        public String localVersionName;
        public String apkUrl;
        public String releaseNotes;
    }

    public static void checkForUpdate(Context context, Callback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://api.github.com/repos/robertoricotti/stonex_mc/releases/latest");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new IOException("HTTP " + responseCode);
                }

                String json;
                try (InputStream is = connection.getInputStream()) {
                    json = readAll(is);
                }

                JSONObject root = new JSONObject(json);

                String tagName = root.optString("tag_name", "").trim();
                String body = root.optString("body", "");

                String remoteVersionName = parseVersionName(tagName);
                long remoteVersionCode = parseVersionCode(tagName);

                JSONArray assets = root.optJSONArray("assets");
                String apkUrl = null;

                if (assets != null) {
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.getJSONObject(i);
                        String name = asset.optString("name", "");
                        if (name != null && name.toLowerCase().endsWith(".apk")) {
                            apkUrl = asset.optString("browser_download_url", null);
                            break;
                        }
                    }
                }

                String localVersionName = parseVersionName(getLocalVersionName(context));
                long localVersionCode = getLocalVersionCode(context);

                boolean updateAvailable = false;

                // Priorità assoluta al versionCode, ma solo se nel tag remoto esiste davvero
                if (apkUrl != null) {
                    if (remoteVersionCode > 0) {
                        updateAvailable = remoteVersionCode > localVersionCode;
                    } else {
                        updateAvailable = isRemoteVersionNewer(remoteVersionName, localVersionName);
                    }
                }

                UpdateInfo info = new UpdateInfo();
                info.remoteVersionCode = remoteVersionCode;
                info.remoteVersionName = remoteVersionName;
                info.localVersionCode = localVersionCode;
                info.localVersionName = localVersionName;
                info.apkUrl = apkUrl;
                info.releaseNotes = body;
                info.updateAvailable = updateAvailable;

                mainHandler.post(() -> callback.onResult(info));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                executor.shutdown();
            }
        });
    }

    private static String getLocalVersionName(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return pInfo.versionName != null ? pInfo.versionName.trim() : "";
    }

    private static long getLocalVersionCode(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return pInfo.getLongVersionCode();
        } else {
            return pInfo.versionCode;
        }
    }

    private static String readAll(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    private static long parseVersionCode(String tag) {
        // Esempi supportati:
        // v4.1.1+4101
        // 4.1.1+4101
        if (tag == null) {
            return 0;
        }

        int plus = tag.lastIndexOf('+');
        if (plus >= 0 && plus < tag.length() - 1) {
            try {
                return Long.parseLong(tag.substring(plus + 1).trim());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private static String parseVersionName(String tag) {
        if (tag == null) {
            return "";
        }

        String cleaned = tag.trim();

        if (cleaned.startsWith("v") || cleaned.startsWith("V")) {
            cleaned = cleaned.substring(1);
        }

        int plus = cleaned.indexOf('+');
        if (plus >= 0) {
            cleaned = cleaned.substring(0, plus);
        }

        // rimuove eventuali suffix tipo " -GEN2"
        int suffixIndex = cleaned.indexOf(" -");
        if (suffixIndex >= 0) {
            cleaned = cleaned.substring(0, suffixIndex);
        }

        return cleaned.trim();
    }

    private static boolean isRemoteVersionNewer(String remote, String local) {
        if (remote == null) remote = "";
        if (local == null) local = "";

        String[] remoteParts = remote.split("\\.");
        String[] localParts = local.split("\\.");

        int max = Math.max(remoteParts.length, localParts.length);

        for (int i = 0; i < max; i++) {
            int remoteValue = i < remoteParts.length ? safeParseInt(remoteParts[i]) : 0;
            int localValue = i < localParts.length ? safeParseInt(localParts[i]) : 0;

            if (remoteValue > localValue) {
                return true;
            }
            if (remoteValue < localValue) {
                return false;
            }
        }

        return false;
    }

    private static int safeParseInt(String value) {
        if (value == null) {
            return 0;
        }

        value = value.trim();
        if (value.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}