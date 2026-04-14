package com.example.mylibrary.para;

import android.content.Context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class My_Read {
    static final int READ_BLOCK_SIZE = 150;
    public static double Dbl(String name, Context context){
        String DataSaved = null;
        try {
            FileInputStream fIn = context.openFileInput(name);
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
        if(DataSaved != null) {
            return Double.parseDouble(DataSaved.replace(",", "."));
        }else {
            return 0;
        }
    }

    public static int Int(String name, Context context){
        String DataSaved = null;
        try {
            FileInputStream fIn = context.openFileInput(name);
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
        if(DataSaved!=null){
        return Integer.parseInt(DataSaved);}
        else {
            return 0;
        }
    }

    public static String Str(String name, Context context){
        String DataSaved = null;
        try {
            FileInputStream fIn = context.openFileInput(name);
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
        if(DataSaved!=null){
            return DataSaved;}
        else {
            return "";
        }
    }

    public static float Flt(String name, Context context){
        String DataSaved = null;
        try {
            FileInputStream fIn = context.openFileInput(name);
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
        if(DataSaved!=null){
            return Float.parseFloat(DataSaved.replace(",","."));}
        else {
            return 0;
        }
    }
    public static boolean Bln(String name, Context context){
        String DataSaved = null;
        try {
            FileInputStream fIn = context.openFileInput(name);
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
        if(DataSaved!=null){
            return DataSaved.equals("true");

        }
        else {
            return false;
        }
    }
}
