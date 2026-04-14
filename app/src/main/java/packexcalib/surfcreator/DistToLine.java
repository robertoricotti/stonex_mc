package packexcalib.surfcreator;


import utils.DistToPoint;

public class DistToLine {

    double linedistance;
    int quad, toolquad;
    boolean isInside;

    public DistToLine(double tool_X, double tool_Y, double A_X, double A_Y, double B_X, double B_Y) {
        double l_AB = new DistToPoint(A_X, A_Y, 0, B_X, B_Y, 0).getDist_to_point();
        double dY, dToolY, dX, dToolX;
        double sign = ((tool_X - A_X) * (B_Y - A_Y)) - ((tool_Y - A_Y) * (B_X - A_X));

        dY = B_Y - A_Y;
        dX = B_X - A_X;

        dToolY = tool_Y - A_Y;
        dToolX = tool_X - A_X;
        if (dY > 0 && dX > 0) {
            quad = 1;
        } else if (dY > 0 && dX < 0) {
            quad = 2;
        } else if (dY < 0 && dX < 0) {
            quad = 3;
        } else if (dY < 0 && dX > 0) {
            quad = 4;
        }
        if (dToolY > 0 && dToolX > 0) {
            toolquad = 1;
        } else if (dToolY > 0 && dToolX < 0) {
            toolquad = 2;
        } else if (dToolY < 0 && dToolX < 0) {
            toolquad = 3;
        } else if (dToolY < 0 && dToolX > 0) {
            toolquad = 4;
        }

        linedistance = Math.abs((B_X - A_X) * (A_Y - tool_Y) - (A_X - tool_X) * (B_Y - A_Y)) / l_AB;
        isInside = !(linedistance < 0) && !(linedistance > l_AB); // Il punto è oltre un estremo della linea
        if (sign < 0) {
            linedistance = linedistance * -1;
        } else {
            linedistance = linedistance * 1;
        }


    }

    public double getLinedistance() {
        return linedistance;
    }

    public int getQuad() {
        return quad;
    }

    public boolean isInside() {
        return isInside;
    }
}
