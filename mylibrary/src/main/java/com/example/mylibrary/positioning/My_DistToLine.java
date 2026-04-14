package com.example.mylibrary.positioning;


public class My_DistToLine {

    static double linedistance;

    static boolean isInside;
    public My_DistToLine(){

    }

    public static double [] Distance(double tool_X, double tool_Y, double A_X, double A_Y, double B_X, double B_Y) {
         double l_AB =  My_DistToPoint.Distanxce(A_X, A_Y, 0, B_X, B_Y, 0);

        double sign = ((tool_X - A_X) * (B_Y - A_Y)) - ((tool_Y - A_Y) * (B_X - A_X));
        linedistance = Math.abs((B_X - A_X) * (A_Y - tool_Y) - (A_X - tool_X) * (B_Y - A_Y)) / l_AB;
        isInside= !(linedistance < 0) && !(linedistance > l_AB); // Il punto è oltre un estremo della linea
        if (sign < 0) {
            linedistance = linedistance * -1;
        } else {
            linedistance = linedistance * 1;
        }
        double isIn=0;
        if(isInside){isIn=1;}else {isIn=0;}


        return new double[]{linedistance,isIn};
    }


}
