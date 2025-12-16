package gui.draw_class;

import dxf.Point2D;
import dxf.Point3D;

public final class Geometry2D {

    public static Point2D projectPointOnSegment(
            double bx, double by,
            Point3D s,
            Point3D e
    ) {
        double x1 = s.getX();
        double y1 = s.getY();
        double x2 = e.getX();
        double y2 = e.getY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double len2 = dx * dx + dy * dy;

        if (len2 == 0) {
            return new Point2D(x1, y1);
        }

        double t = ((bx - x1) * dx + (by - y1) * dy) / len2;
        t = Math.max(0.0, Math.min(1.0, t));

        return new Point2D(
                x1 + t * dx,
                y1 + t * dy
        );
    }
}
