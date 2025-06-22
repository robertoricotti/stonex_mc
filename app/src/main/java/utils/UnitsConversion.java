package utils;

public class UnitsConversion {

    public static double DegToPercent(String str) {
        double a = Math.toRadians(Double.parseDouble(str));
        return Math.tan(a) * 100;
    }

    public static double PercentToDeg(String str) {
        double a = Double.parseDouble(str);
        return Math.toDegrees(Math.atan(a / 100));
    }

    public static double MeterToFeet(String str) {
        double a = Double.parseDouble(str);
        return a*3.28084d;
    }

    public static double FeetToMeter(String str) {
        double a = Double.parseDouble(str);
        return a*0.3048006096d;
    }
    public static double MeterToInch(String str){
        double a=Double.parseDouble(str);
        return  a*39.3701d;

    }
    public static double InchToMeter(String str){
        double a=Double.parseDouble(str);
        return a*0.0254d;
    }
    public static double myscaleD(double input,double inMin,double inMax,double outMin,double outMax){
        return ((input-inMin)/(inMax-inMin)*(outMax-outMin))+outMin;
    }
    public static float myscaleF(float input,float inMin,float inMax,float outMin,float outMax){
        return ((input-inMin)/(inMax-inMin)*(outMax-outMin))+outMin;
    }
    public static double limitD(double value,double min,double max) {
        return Math.max(min, Math.min(value, max));
    }
    public static float limitF(float value,float min,float max) {
        return Math.max(min, Math.min(value, max));
    }

    public static int myscalIntD(int input,int inMin,int inMax,int outMin,int outMax){
        double a=(((double)input-(double)inMin)/((double)inMax-(double)inMin)*((double)outMax-(double)outMin))+(double)outMin;
        return (int) a;
    }
    public static int limitInt(int value,int min,int max) {
        return Math.max(min, Math.min(value, max));
    }



}
