package utils;

import static android.content.Context.MODE_PRIVATE;
import static gui.MyApp.DEVICE_SN;
import static gui.MyApp.folderPath;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cloud.S3ManagerSingleton;
import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;

public class MyData {
    static final int READ_BLOCK_SIZE = 150;
    private static final Gson gson = new Gson();

    public static <T> void pushList(String DataName, List<T> list) {
        String json = gson.toJson(list);
        push(DataName, json);
    }

    public static <T> List<T> getList(String DataName, Class<T> clazz) {
        String jsonSaved = get_String(DataName);
        if (jsonSaved == null || jsonSaved.isEmpty()) return null;

        Type type = TypeToken.getParameterized(List.class, clazz).getType();
        return gson.fromJson(jsonSaved, type);
    }

    public static void push(String DataName, String Data2Save) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(MyApp.visibleActivity.getApplicationContext().openFileOutput(DataName, MODE_PRIVATE));
            osw.write(Data2Save);
            osw.flush();
            osw.close();
        } catch (IOException | NullPointerException ignored) {

        }
    }

    public static String get_String(String DataName) {
        String DataSaved = null;
        try {
            FileInputStream fIn = MyApp.visibleActivity.getApplicationContext().openFileInput(DataName);
            InputStreamReader isr = new InputStreamReader(fIn);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            StringBuilder s = new StringBuilder();
            int charRead;
            while ((charRead = isr.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                s.append(readString);
                inputBuffer = new char[READ_BLOCK_SIZE];
            }
            DataSaved = s.toString();
        } catch (IOException | NullPointerException ignored) {
        }
        return DataSaved;
    }

    public static double get_Double(String DataName) {
        String DataSaved = null;
        double res = 0;
        try {
            FileInputStream fIn = MyApp.visibleActivity.getApplicationContext().openFileInput(DataName);
            InputStreamReader isr = new InputStreamReader(fIn);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            StringBuilder s = new StringBuilder();
            int charRead;
            while ((charRead = isr.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                s.append(readString);
                inputBuffer = new char[READ_BLOCK_SIZE];
            }
            DataSaved = s.toString();
            res = Double.parseDouble(DataSaved.replace(",", "."));

        } catch (IOException | NullPointerException | NumberFormatException ignored) {
            return 0;
        }
        return res;
    }

    public static int get_Int(String DataName) {
        String DataSaved = null;
        int res = 0;
        try {
            FileInputStream fIn = MyApp.visibleActivity.getApplicationContext().openFileInput(DataName);
            InputStreamReader isr = new InputStreamReader(fIn);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            StringBuilder s = new StringBuilder();
            int charRead;
            while ((charRead = isr.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                s.append(readString);
                inputBuffer = new char[READ_BLOCK_SIZE];
            }
            DataSaved = s.toString();
            res = Integer.parseInt(DataSaved);

        } catch (IOException | NullPointerException | NumberFormatException ignored) {
            return 0;
        }
        return res;
    }

    public static float get_Float(String DataName) {
        String DataSaved = null;
        float res = 0;
        try {
            FileInputStream fIn = MyApp.visibleActivity.getApplicationContext().openFileInput(DataName);
            InputStreamReader isr = new InputStreamReader(fIn);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            StringBuilder s = new StringBuilder();
            int charRead;
            while ((charRead = isr.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                s.append(readString);
                inputBuffer = new char[READ_BLOCK_SIZE];
            }
            DataSaved = s.toString();
            res = Float.parseFloat(DataSaved.replace(",", "."));

        } catch (IOException | NullPointerException ignored) {
            return 0;
        }
        return res;
    }

    public static long get_Long(String DataName) {
        String DataSaved = null;
        long res = 0;
        try {
            FileInputStream fIn = MyApp.visibleActivity.getApplicationContext().openFileInput(DataName);
            InputStreamReader isr = new InputStreamReader(fIn);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            StringBuilder s = new StringBuilder();
            int charRead;
            while ((charRead = isr.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                s.append(readString);
                inputBuffer = new char[READ_BLOCK_SIZE];
            }
            DataSaved = s.toString();
            res = Long.parseLong(DataSaved);

        } catch (IOException | NullPointerException ignored) {
            return 0;
        }
        return res;
    }

    public static void exportAllToJson() {
        File dirOut = new File(Environment.getExternalStorageDirectory(), folderPath + "/Config/");
        if (!dirOut.exists()) {
            dirOut.mkdirs();
        }
        String stringa = "mcconfig_" + DEVICE_SN;
        File jsonFile = new File(dirOut, stringa + ".json");

        String[] allFiles = MyApp.visibleActivity.getApplicationContext().fileList();
        JSONObject jsonObject = new JSONObject();

        // CHIAVI DA ESCLUDERE
        Set<String> excludeKeys = new HashSet<>(Arrays.asList(
                "licenza",
                "progettoSelected",
                "progettoSelected_POINT",
                "progettoSelected_POLY",
                "CRS_ESTERNO"
        ));

        for (String fileName : allFiles) {
            String stringas = "mcconfig_" + DEVICE_SN;
            if (fileName.equals(stringas + ".json")) continue;
            if (excludeKeys.contains(fileName)) continue;

            try (FileInputStream fIn = MyApp.visibleActivity.getApplicationContext().openFileInput(fileName);
                 InputStreamReader isr = new InputStreamReader(fIn)) {

                char[] inputBuffer = new char[READ_BLOCK_SIZE];
                StringBuilder s = new StringBuilder();
                int charRead;
                while ((charRead = isr.read(inputBuffer)) > 0) {
                    s.append(String.copyValueOf(inputBuffer, 0, charRead));
                }
                jsonObject.put(fileName, s.toString());

            } catch (IOException | JSONException ignored) {
            }
        }

        // ✅ Salvataggio in locale
        try (FileOutputStream fos = new FileOutputStream(jsonFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos)) {

            osw.write(jsonObject.toString(4));
            osw.flush();

        } catch (IOException | JSONException e) {
            Log.e("exportAllToJson", "Errore salvataggio JSON: " + e.getMessage());
            return;
        }

        // ✅ Upload su cloud solo se online
        if (isNetworkAvailable(MyApp.visibleActivity)) {
            try {
                String deviceSN = DEVICE_SN;
                if (deviceSN == null || deviceSN.isEmpty()) {
                    Log.w("S3Upload", "DEVICE_SN non definito, upload saltato");
                    return;
                }

                String remotePath = "serials/" + deviceSN + "/Config/" + jsonFile.getName();
                S3ManagerSingleton s3Manager = S3ManagerSingleton.getInstance(MyApp.visibleActivity);
                s3Manager.uploadFile(jsonFile.getAbsolutePath(), remotePath);

                Log.i("S3Upload", "Upload avviato: " + remotePath);

            } catch (Exception e) {
                Log.w("S3Upload", "Errore upload (nessuna azione): " + e.getMessage());
            }
        } else {
            Log.i("S3Upload", "Nessuna connessione disponibile: salvataggio solo locale");
        }
    }

    /**
     * Controlla se c’è connessione internet
     */
    private static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static void restoreFromJson() {
        String stringa = "mcconfig_" + DEVICE_SN;
        File jsonFile = new File(Environment.getExternalStorageDirectory(), folderPath + "/Config/" + stringa + ".json");

        if (!jsonFile.exists()) {
            new CustomToast(MyApp.visibleActivity, "File Not Found").show_error();
            return; // Se il file non esiste, esci
        }
// Chiavi da non ripristinare
        Set<String> excludeKeys = new HashSet<>(Arrays.asList(
                "licenza",
                "progettoSelected",
                "progettoSelected_POINT",
                "progettoSelected_POLY",
                "CRS_ESTERNO"
                // aggiungi altre chiavi da ignorare
        ));
        try {
            FileInputStream fis = new FileInputStream(jsonFile); // Legge dal percorso esterno
            InputStreamReader isr = new InputStreamReader(fis);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            StringBuilder s = new StringBuilder();
            int charRead;
            while ((charRead = isr.read(inputBuffer)) > 0) {
                s.append(String.copyValueOf(inputBuffer, 0, charRead));
            }
            isr.close();

            JSONObject jsonObject = new JSONObject(s.toString());

            // Itera su tutte le chiavi
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (excludeKeys.contains(key)) continue; //  Salta le chiavi escluse
                String value = jsonObject.getString(key);

                // Scrive ogni coppia come file separato nella memoria interna
                push(key, value);
            }
            new CustomToast(MyApp.visibleActivity, "RESTART THE APPLICATION").show_alert();
        } catch (IOException | JSONException ignored) {

            // Se il file non è leggibile o è malformato, lo ignora
        }
    }


}
