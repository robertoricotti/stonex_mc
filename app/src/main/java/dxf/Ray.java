package dxf;

public class Ray extends XLine {
    private static final long serialVersionUID = 1L;

    public Ray(Point3D basePoint, Point3D direction, int color, Layer layer) {
        super(basePoint, direction, color, layer);
    }

    @Override
    public Ray clone() {
        return new Ray(
                getBasePoint() != null ? getBasePoint().clone() : null,
                getDirection() != null ? getDirection().clone() : null,
                getColor(),
                getLayer()
        );
    }
}