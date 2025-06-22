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
}

