package dxf;

public class Trace extends Solid {
    private static final long serialVersionUID = 1L;

    public Trace(Point3D p1, Point3D p2, Point3D p3, Point3D p4, int color, Layer layer) {
        super(p1, p2, p3, p4, color, layer);
    }

    @Override
    public Trace clone() {
        return new Trace(
                getVertices().get(0) != null ? getVertices().get(0).clone() : null,
                getVertices().get(1) != null ? getVertices().get(1).clone() : null,
                getVertices().get(2) != null ? getVertices().get(2).clone() : null,
                getVertices().get(3) != null ? getVertices().get(3).clone() : null,
                getColor(),
                getLayer()
        );
    }
}