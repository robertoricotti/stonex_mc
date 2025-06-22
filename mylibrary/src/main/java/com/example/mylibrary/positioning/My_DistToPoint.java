package com.example.mylibrary.positioning;

public class My_DistToPoint {

  public My_DistToPoint(){

  }

    public static double Distanxce(double X1, double Y1, double Z1, double X2, double Y2, double Z2) {
        //X= East Y=North
        double absX, absY, absZ;
        absX = Math.abs(X2 - X1);
        absY = Math.abs(Y2 - Y1);
        absZ = Math.abs(Z2 - Z1);
        return Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2) + Math.pow(absZ, 2));
    }
    public static double Distanxce3D(double X1, double Y1, double Z1, double X2, double Y2, double Z2) {
        //X= East Y=North
        double absX, absY, absZ;
        absX = Math.abs(X2 - X1);
        absY = Math.abs(Y2 - Y1);
        absZ = Math.abs(Z2 - Z1);
        return Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2) + Math.pow(absZ, 2));
    }
    public static double Distanxce2D(double X1, double Y1, double X2, double Y2) {
        //X= East Y=North
        double absX, absY;
        absX = Math.abs(X2 - X1);
        absY = Math.abs(Y2 - Y1);
        return Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2));
    }


}
