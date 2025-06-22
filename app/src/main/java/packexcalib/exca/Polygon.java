package packexcalib.exca;

import java.util.ArrayList;

public class Polygon {

    public static boolean isInsidePolygon(double x, double y, ArrayList<Double> polygonX, ArrayList<Double> polygonY) {
        int n = polygonX.size();
        boolean isInside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = polygonX.get(i), yi = polygonY.get(i);
            double xj = polygonX.get(j), yj = polygonY.get(j);

            boolean intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) isInside = !isInside;
        }
        return isInside;
    }
}

