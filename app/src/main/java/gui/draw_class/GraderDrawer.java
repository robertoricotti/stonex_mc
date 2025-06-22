package gui.draw_class;

import android.graphics.PointF;

public class GraderDrawer {
    PointF startXYZ;
    PointF GPS_Z;
    PointF GPS_DX;
    PointF GPS_DY;
    PointF bladeCenter;
    PointF bladeLeft;
    PointF bladeRight;

    PointD rStartXYZ;
    PointD rGPS_Z;
    PointD rGPS_DX;
    PointD rGPS_DY;
    PointD rBladeCenter;
    PointD rBladeLeft;
    PointD rBladeRight;

    public GraderDrawer() {}

    // inizializza i punti di congiunzioni reali secondo il pivot point desiderato
    public void update(float canvasWidth, float canvasHeight, String pivotPoint, float percX, float percY, double scala){

        double heading = 166;

        rStartXYZ = new PointD(514238.763, 5045440.202);
        rGPS_Z = new PointD(514238.740, 5045440.225);
        rGPS_DX = new PointD(514237.918, 5045440.027);
        rGPS_DY = new PointD(514237.974, 5045439.793);
        rBladeCenter = new PointD(514238.602, 5045437.553);
        rBladeLeft = new PointD(514238.990, 5045437.649);
        rBladeRight = new PointD(514238.213, 5045437.458);

        PointF pivot = new PointF( canvasWidth * percX, canvasHeight * percY);

        PointD fixPoint;

        if (pivotPoint.equalsIgnoreCase("GPS_DY")) {
            fixPoint = new PointD(rGPS_DY.x, rGPS_DY.y);
        }
        else {
            fixPoint = new PointD(rBladeCenter.x, rBladeCenter.y);
        }

        double diff_x_GPS_DY = (rGPS_DY.x - fixPoint.x) * scala;
        double diff_y_GPS_DY  = (rGPS_DY.y - fixPoint.y) * scala;

        GPS_DY = new PointF(
                (float) (pivot.x + diff_x_GPS_DY * Math.cos(Math.toRadians(heading)) - diff_y_GPS_DY * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_GPS_DY * Math.sin(Math.toRadians(heading)) - diff_y_GPS_DY * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_GPS_DX = (rGPS_DX.x - fixPoint.x) * scala;
        double diff_y_GPS_DX  = (rGPS_DX.y - fixPoint.y) * scala;

        GPS_DX = new PointF(
                (float) (pivot.x + diff_x_GPS_DX * Math.cos(Math.toRadians(heading)) - diff_y_GPS_DX * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_GPS_DX * Math.sin(Math.toRadians(heading)) - diff_y_GPS_DX * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_startXYZ = (rStartXYZ.x - fixPoint.x) * scala;
        double diff_y_startXYZ  = (rStartXYZ.y - fixPoint.y) * scala;

        startXYZ = new PointF(
                (float) (pivot.x + diff_x_startXYZ * Math.cos(Math.toRadians(heading)) - diff_y_startXYZ * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_startXYZ * Math.sin(Math.toRadians(heading)) - diff_y_startXYZ * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_GPS_Z = (rGPS_Z.x - fixPoint.x) * scala;
        double diff_y_GPS_Z  = (rGPS_Z.y - fixPoint.y) * scala;

        GPS_Z = new PointF(
                (float) (pivot.x + diff_x_GPS_Z * Math.cos(Math.toRadians(heading)) - diff_y_GPS_Z * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_GPS_Z * Math.sin(Math.toRadians(heading)) - diff_y_GPS_Z * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_blade = (rBladeCenter.x - fixPoint.x) * scala;
        double diff_y_blade = (rBladeCenter.y - fixPoint.y) * scala;

        /*bladeCenter = new PointF(
                (float) (pivot.x + diff_x_blade * Math.cos(Math.toRadians(heading)) - diff_y_blade * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_blade * Math.sin(Math.toRadians(heading)) - diff_y_blade * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_bladeLeft = (rBladeLeft.x - fixPoint.x) * scala;
        double diff_y_bladeLeft = (rBladeLeft.y - fixPoint.y) * scala;

        bladeLeft = new PointF(
                (float) (pivot.x + diff_x_bladeLeft * Math.cos(Math.toRadians(heading)) - diff_y_bladeLeft * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_bladeLeft * Math.sin(Math.toRadians(heading)) - diff_y_bladeLeft * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_bladeRight = (rBladeRight.x - fixPoint.x) * scala;
        double diff_y_bladeRight = (rBladeRight.y - fixPoint.y) * scala;

        bladeRight = new PointF(
                (float) (pivot.x + diff_x_bladeRight * Math.cos(Math.toRadians(heading)) - diff_y_bladeRight * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_bladeRight * Math.sin(Math.toRadians(heading)) - diff_y_bladeRight * Math.cos(Math.toRadians(heading)))
        );*/

        bladeCenter = new PointF(canvasWidth / 2f, canvasHeight / 2f - 100);

        bladeLeft = new PointF(canvasWidth / 2f - 200, canvasHeight / 2f - 100);

        bladeRight = new PointF(canvasWidth / 2f + 200, canvasHeight / 2f - 100);
    }
}
