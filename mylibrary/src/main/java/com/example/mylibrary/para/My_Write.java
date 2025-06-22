package com.example.mylibrary.para;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class My_Write {
    public static void Dbl(double val, String name, Context context){
        try {
            OutputStreamWriter osw=new OutputStreamWriter(context.openFileOutput(name, MODE_PRIVATE));
            osw.write(String.valueOf(val));
            osw.flush( );
            osw.close( );
        } catch (IOException | NullPointerException ignored){}
    }

    public static void Int(int val, String name, Context context){
        try {
            OutputStreamWriter osw=new OutputStreamWriter(context.openFileOutput(name, MODE_PRIVATE));
            osw.write(String.valueOf(val));
            osw.flush( );
            osw.close( );
        } catch (IOException | NullPointerException ignored){}
    }
    public static void Str(String val, String name, Context context){
        try {
            OutputStreamWriter osw=new OutputStreamWriter(context.openFileOutput(name, MODE_PRIVATE));
            osw.write(String.valueOf(val));
            osw.flush( );
            osw.close( );
        } catch (IOException | NullPointerException ignored){}
    }
    public static void Flt(float val, String name, Context context){
        try {
            OutputStreamWriter osw=new OutputStreamWriter(context.openFileOutput(name, MODE_PRIVATE));
            osw.write(String.valueOf(val));
            osw.flush( );
            osw.close( );
        } catch (IOException | NullPointerException ignored){}
    }
    public static void Bln(boolean val, String name, Context context){
        try {
            OutputStreamWriter osw=new OutputStreamWriter(context.openFileOutput(name, MODE_PRIVATE));
            osw.write(String.valueOf(val));
            osw.flush( );
            osw.close( );
        } catch (IOException | NullPointerException ignored){}
    }
}
