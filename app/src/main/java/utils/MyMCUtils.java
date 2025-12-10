package utils;

import android.util.Log;

import packexcalib.exca.DataSaved;

public class MyMCUtils {

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
    public static double myscaleD(double input, double inMin, double inMax,
                                  double outMin, double outMax) {

        if (input <= inMin) return outMin;
        if (input >= inMax) return outMax;


        return ((input - inMin) / (inMax - inMin)) * (outMax - outMin) + outMin;
    }

    public static float myscaleF(float input,float inMin,float inMax,float outMin,float outMax){
        if (input <= inMin) return outMin;
        if (input >= inMax) return outMax;
        return ((input-inMin)/(inMax-inMin)*(outMax-outMin))+outMin;
    }
    public static double limitD(double value,double min,double max) {
        return Math.max(min, Math.min(value, max));
    }
    public static short limitShort(short value,short min,short max) {
        return (short) Math.max(min, Math.min(value, max));
    }
    public static float limitF(float value,float min,float max) {
        return Math.max(min, Math.min(value, max));
    }

    public static int myscalIntD(int input,int inMin,int inMax,int outMin,int outMax){
        if (input <= inMin) return outMin;
        if (input >= inMax) return outMax;
        double a=(((double)input-(double)inMin)/((double)inMax-(double)inMin)*((double)outMax-(double)outMin))+(double)outMin;
        return (int) a;
    }
    public static int limitInt(int value,int min,int max) {
        return Math.max(min, Math.min(value, max));
    }



    public static double ledder(int Gain){
        return myscaleD((double)Gain,1,255,1,10);
    }
    public static int limitIntJDL(double input, int value, int min, int max) {
        double absInput = Math.abs(input);
        double multiplier;

        if (absInput <= 0.05) {
            multiplier = 1.0;
        } else if (absInput <= 0.07) {
            multiplier = 1.1;
        } else if (absInput <= 0.09) {
            multiplier = 1.2;
        } else if (absInput <= 0.11) {
            multiplier = 1.3;
        } else if (absInput <= 0.13) {
            multiplier = 1.4;
        } else if (absInput <= 0.15) {
            multiplier = 1.5;
        } else {
            multiplier = 1.6;
        }

        int center = 20000;
        double offset = value - center;

        // Applichiamo il moltiplicatore in modo simmetrico
        double result = center + offset * multiplier;

        // Clamp tra min e max
        int finalResult = (int) Math.round(Math.max(min, Math.min(result, max)));
        return finalResult;
    }
    public static double bladeSlope(double[] Pleft, double[] Pright) {

        // --- VALIDAZIONI ---
        if (Pleft == null || Pright == null) {
            return 0;   // oppure Double.NaN
        }

        if (Pleft.length < 3 || Pright.length < 3) {
            return 0;   // oppure Double.NaN
        }

        // --- CALCOLO ---
        boolean isNegative = Pleft[2] < Pright[2];

        double dist2D = DistToPoint.dist2D(Pleft, Pright);
        double dist3D = DistToPoint.dist3D(Pleft, Pright);

        // protezione matematica per evitare NaN
        double d = (dist3D * dist3D) - (dist2D * dist2D);
        if (d <= 0) return 0;

        double slope = Math.toDegrees(Math.sqrt(d));

        if (isNegative) slope *= -1;

        return slope;
    }


    /*
    public static double bladeSlope(double[] Pleft,double[] Pright){

        boolean isNegative=Pleft[2]<Pright[2];

        double dist2D= DistToPoint.dist2D(Pleft,Pright);
        double dist3D=DistToPoint.dist3D(Pleft,Pright);

        double slope=Math.toDegrees(Math.sqrt((dist3D*dist3D)-(dist2D*dist2D)));
        if(isNegative){
            slope=slope*-1;
        }
        return slope;

    }
*/
    public static double profile_3pt(double input, double min, double cent, double max, double maxSteerAngle) {
        double halfAngle = maxSteerAngle / 2.0;

        if (cent <= min || max <= cent) {
            return 0;
        }

        if (input <= min) {
            return -halfAngle;
        } else if (input >= max) {
            return halfAngle;
        } else if (input == cent) {
            return 0.0;
        } else if (input < cent) {
            // Scala da min→cent → [-halfAngle → 0]
            return -halfAngle + (input - min) * (0.0 - (-halfAngle)) / (cent - min);
        } else {
            // Scala da cent→max → [0 → +halfAngle]
            return 0.0 + (input - cent) * (halfAngle - 0.0) / (max - cent);
        }
    }

    public static double wrap(double angleDeg) {

        // Usa la funzione IEEE remainder (più stabile del %)
        angleDeg = Math.IEEEremainder(angleDeg, 360.0);

        // Forziamo comunque l'intervallo corretto
        if (angleDeg <= -180.0) angleDeg += 360.0;
        if (angleDeg >   180.0) angleDeg -= 360.0;

        return angleDeg;
    }
    public static double computeDeltaYawFromTiltAndCurl(
            double tiltDeg,        // Roll reale: >0 scende DX | <0 scende SX
            double curlDeg ,        // correctBucket: -180 .. +180
            double piccolaBuck,     //distanza pivot centro benna
            double bucketlen        //ampiezza benna
    ) {

        // -------------------------------
        // 1) DEAD ZONE SUL TILT
        // -------------------------------
        double deadTilt = 1.0; // gradi
        if (Math.abs(tiltDeg) < deadTilt) return 0.0;

        // -------------------------------
        // 2) NORMALIZZA CURL in [-180, +180]
        // -------------------------------
        double curl = curlDeg;
        if (curl > 180)  curl -= 360;
        if (curl < -180) curl += 360;

        // -------------------------------
        // 3) FATTORE GEOMETRICO CORRETTO
        //   yaw MAX a 0° e -180°
        //   yaw ZERO a -90°
        // -------------------------------
        double curlFactor;

        if (curl <= 0 && curl >= -180) {
            // Triangolare con minimo a -90
            curlFactor = Math.abs(Math.abs(curl) - 90.0) / 90.0;
            // 0   → 1
            // -90 → 0
            // -180→ 1
        }
        else if (curl > 0 && curl <= 180) {
            // zona teorica: simmetrica
            curlFactor = Math.abs(Math.abs(curl) - 90.0) / 90.0;
        }
        else {
            curlFactor = 0.0;
        }

        // Clamp sicurezza
        if (curlFactor < 0) curlFactor = 0;
        if (curlFactor > 1) curlFactor = 1;

        // -------------------------------
        // 4) SEGNO FISICO DELLA ROTAZIONE
        //
        // REGOLA:
        //   0 → -90     : segno diretto
        //  -90 → -180   : segno invertito
        // -------------------------------
        int sign = 0;

        boolean frontRange = (curl <= 0  && curl > -90);
        boolean backRange  = (curl <= -90 && curl >= -180);

        if (frontRange) {
            sign = (tiltDeg > 0) ? +1 : -1;
        }
        else if (backRange) {
            sign = (tiltDeg > 0) ? -1 : +1;
        }
        else {
            sign = 0;
        }

        // -------------------------------
        // 5) GUADAGNO DI TARATURA
        // -------------------------------
       // double K = 1.8;   // lo regoli sul campo
        double K = bucketlen/piccolaBuck;
        // -------------------------------
        // 6) DELTA YAW CINEMATICO
        // -------------------------------
        double deltaYaw = sign * K * Math.abs(tiltDeg) * curlFactor;

        // -------------------------------
        // 7) SATURAZIONE FISICA
        // -------------------------------
        double maxYaw = 45.0;   // gradi realistici
        if (deltaYaw >  maxYaw) deltaYaw =  maxYaw;
        if (deltaYaw < -maxYaw) deltaYaw = -maxYaw;

        return -deltaYaw;
    }



}
