package gui.draw_class;

public class Dist2Point {

    double dist_to_point;

    public Dist2Point() {}

    public double getDist_to_point(double X1, double Y1, double Z1, double X2, double Y2, double Z2) {
        //X= East Y=North
        double absX, absY, absZ;
        absX = Math.abs(X2 - X1);
        absY = Math.abs(Y2 - Y1);
        absZ = Math.abs(Z2 - Z1);
        return Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2) + Math.pow(absZ, 2));
    }

    public double getDist_to_point2(double X1, double Y1, double Z1, double X2, double Y2, double Z2) {
        //X= East Y=North
        double absX, absY, absZ;
        absX = (X2 - X1);
        absY = (Y2 - Y1);
        absZ = (Z2 - Z1);
        return Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2) + Math.pow(absZ, 2));
    }
}
