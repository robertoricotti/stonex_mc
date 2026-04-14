package utils;

public class DistToPoint {

    double dist_to_point;

    public DistToPoint(double X1, double Y1, double Z1, double X2, double Y2, double Z2) {
        //X= East Y=North
        double absX, absY, absZ;
        absX = Math.abs(X2 - X1);
        absY = Math.abs(Y2 - Y1);
        absZ = Math.abs(Z2 - Z1);
        dist_to_point = Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2) + Math.pow(absZ, 2));
    }

    public double getDist_to_point() {
        return dist_to_point;
    }

    public static double dist3D(double[] p1, double[] p2) {
        double X1 = p1[0];
        double Y1 = p1[1];
        double Z1 = p1[2];
        double X2 = p2[0];
        double Y2 = p2[1];
        double Z2 = p2[2];
        double absX, absY, absZ;
        absX = Math.abs(X2 - X1);
        absY = Math.abs(Y2 - Y1);
        absZ = Math.abs(Z2 - Z1);
        return Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2) + Math.pow(absZ, 2));

    }

    public static double dist2D(double[] p1, double[] p2) {
        double X1 = p1[0];
        double Y1 = p1[1];
        double Z1 = 0;
        double X2 = p2[0];
        double Y2 = p2[1];
        double Z2 = 0;
        double absX, absY, absZ;
        absX = Math.abs(X2 - X1);
        absY = Math.abs(Y2 - Y1);
        absZ = Math.abs(Z2 - Z1);
        return Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2) + Math.pow(absZ, 2));

    }
}
