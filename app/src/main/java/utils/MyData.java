package utils;

import static android.content.Context.MODE_PRIVATE;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import gui.MyApp;

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


}
