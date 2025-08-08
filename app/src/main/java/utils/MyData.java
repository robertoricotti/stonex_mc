package utils;

import static android.content.Context.MODE_PRIVATE;


import static gui.MyApp.folderPath;

import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;
import gui.tech_menu.ExcavatorChooserActivity;

public class MyData {
    static final int READ_BLOCK_SIZE = 150;
    public static void push(String DataName, String Data2Save){
        try {
            OutputStreamWriter osw=new OutputStreamWriter(MyApp.visibleActivity.getApplicationContext().openFileOutput(DataName, MODE_PRIVATE));
            osw.write(Data2Save);
            osw.flush( );
            osw.close( );
        } catch (IOException | NullPointerException ignored){

        }
    }

    public static String get_String(String DataName){
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
        } catch (IOException | NullPointerException ignored) {}
        return DataSaved;
    }
    public static double get_Double(String DataName){
        String DataSaved = null;
        double res=0;
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
            res=Double.parseDouble(DataSaved.replace(",","."));

        } catch (IOException | NullPointerException |NumberFormatException ignored) {
            return 0;
        }
        return res;
    }
    public static int get_Int(String DataName){
        String DataSaved = null;
        int res=0;
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
            res=Integer.parseInt(DataSaved);

        } catch (IOException | NullPointerException |NumberFormatException ignored) {
            return 0;
        }
        return res;
    }
    public static float get_Float(String DataName){
        String DataSaved = null;
        float res=0;
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
            res=Float.parseFloat(DataSaved.replace(",","."));

        } catch (IOException | NullPointerException ignored) {
            return 0;
        }
        return res;
    }
    public static long get_Long(String DataName){
        String DataSaved = null;
        long res=0;
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
            res=Long.parseLong(DataSaved);

        } catch (IOException | NullPointerException ignored) {
            return 0;
        }
        return res;
    }

    public static void exportAllToJson() {

        File dirOut = new File(Environment.getExternalStorageDirectory(), folderPath+"/Config/");
        if (!dirOut.exists()) {
            dirOut.mkdirs();
        }

        File jsonFile = new File(dirOut, "mcconfig.json");

        String[] allFiles = MyApp.visibleActivity.getApplicationContext().fileList();
        JSONObject jsonObject = new JSONObject();
        //  CHIAVI DA ESCLUDERE (i nomi file equivalgono alle chiavi nel JSON)
        Set<String> excludeKeys = new HashSet<>(Arrays.asList(
                "licenza",
                "tempConfig",
                "debugMode"
                // aggiungi qui le chiavi da NON  esportare nel JSON
        ));
        for (String fileName : allFiles) {
            if (fileName.equals("mcconfig.json")) continue; // evita il loop infinito
            if (excludeKeys.contains(fileName)) continue;   // esclude chiavi specifiche
            try {
                FileInputStream fIn = MyApp.visibleActivity.getApplicationContext().openFileInput(fileName);
                InputStreamReader isr = new InputStreamReader(fIn);
                char[] inputBuffer = new char[READ_BLOCK_SIZE];
                StringBuilder s = new StringBuilder();
                int charRead;
                while ((charRead = isr.read(inputBuffer)) > 0) {
                    s.append(String.copyValueOf(inputBuffer, 0, charRead));
                }
                isr.close();

                jsonObject.put(fileName, s.toString());

            } catch (IOException | JSONException ignored) {
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(jsonFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(jsonObject.toString(4));
            osw.flush();
            osw.close();
        } catch (IOException ignored) {} catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void restoreFromJson() {

        File jsonFile = new File(Environment.getExternalStorageDirectory(), folderPath + "/Config/mcconfig.json");

        if (!jsonFile.exists()){
            new CustomToast(MyApp.visibleActivity,"File Not Found").show_error();
            return; // Se il file non esiste, esci
        }
// Chiavi da non ripristinare
        Set<String> excludeKeys = new HashSet<>(Arrays.asList(
                "licenza",
                "tempConfig",
                "debugMode"
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
            new CustomToast(MyApp.visibleActivity, "RESTORED").show_alert();
        } catch (IOException | JSONException ignored) {

            // Se il file non è leggibile o è malformato, lo ignora
        }
    }




}
