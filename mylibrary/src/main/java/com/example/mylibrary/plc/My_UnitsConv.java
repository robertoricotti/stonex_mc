package com.example.mylibrary.plc;

public class My_UnitsConv {

    public static double DegToPercent(double str) {
        double a = Math.toRadians(str);
        return Math.tan(a) * 100;
    }

    public static double PercentToDeg(double str) {
        return Math.toDegrees(Math.atan(str / 100));
    }

    public static double MeterToFeet(double str) {
        return str *3.28084d;
    }

    public static double FeetToMeter(double str) {
        return str *0.3048d;
    }
    public static double MeterToInch(double str){
        return  str *39.3701d;

    }
    public static double InchToMeter(double str){
        return str *0.0254d;
    }
    public static double scaleD(double input,double inMin,double inMax,double outMin,double outMax){
        return ((input-inMin)/(inMax-inMin)*(outMax-outMin))+outMin;
    }
    public static float scaleF(float input,float inMin,float inMax,float outMin,float outMax){
        return ((input-inMin)/(inMax-inMin)*(outMax-outMin))+outMin;
    }
    public static int scaleInt(int input,int inMin,int inMax,int outMin,int outMax){
        double a=(((double)input-(double)inMin)/((double)inMax-(double)inMin)*((double)outMax-(double)outMin))+(double)outMin;
        return (int) a;
    }
    public static double limitD(double value,double min,double max) {
        return Math.max(min, Math.min(value, max));
    }
    public static float limitF(float value,float min,float max) {
        return Math.max(min, Math.min(value, max));
    }


    public static int limitInt(int value,int min,int max) {
        return Math.max(min, Math.min(value, max));
    }


}
