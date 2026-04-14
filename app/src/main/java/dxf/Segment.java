package dxf;

public class Segment {
    private Point3D start;
    private Point3D end;
    private Polyline polyline; // Riferimento alla polilinea di origine

    public Segment(Point3D start, Point3D end, Polyline polyline) {
        this.start = start;
        this.end = end;
        this.polyline = polyline;
    }

    public Segment(Point3D start, Point3D end) {
        this.start = start;
        this.end = end;
        this.polyline = null;
    }

    public Point3D getStart() {
        return start;
    }

    public Point3D getEnd() {
        return end;
    }

    public Polyline getPolyline() {
        return polyline;  // Metodo per ottenere la polilinea di origine
    }

    public Point3D getClosestPoint(double px, double py) {

        double x1 = start.getX();
        double y1 = start.getY();
        double z1 = start.getZ();

        double x2 = end.getX();
        double y2 = end.getY();
        double z2 = end.getZ();

        double dx = x2 - x1;
        double dy = y2 - y1;

        double lengthSq = dx * dx + dy * dy;

        // segmento degenerato
        if (lengthSq == 0) {
            return new Point3D(x1, y1, z1);
        }

        // parametro di proiezione
        double t = ((px - x1) * dx + (py - y1) * dy) / lengthSq;

        // clamp sul segmento
        t = Math.max(0.0, Math.min(1.0, t));

        // punto più vicino
        double cx = x1 + t * dx;
        double cy = y1 + t * dy;

        // interpolazione lineare della quota
        double cz = z1 + t * (z2 - z1);

        return new Point3D(cx, cy, cz);
    }

}

