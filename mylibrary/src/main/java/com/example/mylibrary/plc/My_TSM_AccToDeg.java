package com.example.mylibrary.plc;

public class My_TSM_AccToDeg {

    public My_TSM_AccToDeg(){

    }
    public static double Boom_Pitch(int side,short x,short y,short z){
        double norm=Math.sqrt(x * x + y * y + z * z);
        double ax_norm = (double) x / norm;
        double ay_norm = (double) y / norm;
        double az_norm = (double) z / norm;
        if (side == 1) {
            return  Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;


        } else if (side == -1) {
            return Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;

        } else {
           return  0d;

        }

    }
    public static double Boom_Roll(int side,short x,short y,short z){
        double norm=Math.sqrt(x * x + y * y + z * z);
        double ax_norm = (double) x / norm;
        double ay_norm = (double) y / norm;
        double az_norm = (double) z / norm;
        if (side == 1) {
            return -Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI;

        } else if (side == -1) {
            return Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI;
        } else {
            return 0;
        }
    }
    public static double Frame_Pitch(int side,short x,short y,short z){
        double norm=Math.sqrt(x * x + y * y + z * z);
        double ax_norm = (double) x / norm;
        double ay_norm = (double) y / norm;
        double az_norm = (double) z / norm;
        if(side==0){
            return 0;
        }else if(side==1){
            double d= (Math.atan2(ax_norm, -az_norm) * 180 / Math.PI);
            d += 180;
            if (d < -180) {
                d += 360;
            } else if (d > 180) {
                d -= 360;
            }
            return d;
        } else if (side==2) {
            double d= -(Math.atan2(ay_norm, -az_norm) * 180 / Math.PI);
            d += 180;
            if (d < -180) {
                d += 360;
            } else if (d > 180) {
                d -= 360;
            }
            return d;
        } else if (side==3) {
            double d= -(Math.atan2(ax_norm, -az_norm) * 180 / Math.PI);
            d += 180;
            if (d < -180) {
                d += 360;
            } else if (d > 180) {
                d -= 360;
            }
            return d;
        }else if(side==4){
            double d= Math.atan2(ay_norm, -az_norm) * 180 / Math.PI;
            d += 180;
            if (d < -180) {
                d += 360;
            } else if (d > 180) {
                d -= 360;
            }
            return d;
        }else {
            return 0;
        }

    }
    public static double Frame_Roll(int side,short x,short y,short z){
        double norm=Math.sqrt(x * x + y * y + z * z);
        double ax_norm = (double) x / norm;
        double ay_norm = (double) y / norm;
        double az_norm = (double) z / norm;
        if(side==0){
            return 0;

        }else if(side==1){
            double d=-((Math.atan2(ay_norm, -az_norm) * 180 / Math.PI));
            d += 180;
            if (d < -180) {
                d += 360;
            } else if (d > 180) {
                d -= 360;
            }
            return d;

        } else if (side==2) {
            double d=-(Math.atan2(ax_norm, -az_norm) * 180 / Math.PI);
            d += 180;
            if (d < -180) {
                d += 360;
            } else if (d > 180) {
                d -= 360;
            }
            return d;

        } else if (side==3) {
            double d=Math.atan2(ay_norm, -az_norm) * 180 / Math.PI;
            d += 180;
            if (d < -180) {
                d += 360;
            } else if (d > 180) {
                d -= 360;
            }
            return d;

        }else if(side==4){
            double d=Math.atan2(ax_norm, -az_norm) * 180 / Math.PI;
            d += 180;
            if (d < -180) {
                d += 360;
            } else if (d > 180) {
                d -= 360;
            }
            return d;

        }else {
            return 0;
        }
    }
}
