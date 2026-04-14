package dxf;

import java.io.Serializable;

public class Arc implements Serializable {
    private static final long serialVersionUID = 1L;

    Point3D center;
    double radius;
    double startAngle;
    double endAngle;
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    public Arc(Point3D center, double radius, double startAngle, double endAngle, int color, Layer layer) {
        this.center = center;
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.color = color;
        this.layer = layer;
    }

    public Point3D getCenter() {
        return center;
    }

    public void setCenter(Point3D center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public double getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }

    public DxfStyle getDxfStyle() {
        return dxfStyle;
    }

    public void setDxfStyle(DxfStyle dxfStyle) {
        this.dxfStyle = dxfStyle != null ? dxfStyle.copy() : null;
    }

    @Override
    public String toString() {
        return "Arc{" +
                "center=" + center +
                ", radius=" + radius +
                ", startAngle=" + startAngle +
                ", endAngle=" + endAngle +
                ", color=" + color +
                '}';
    }

    @Override
    public Arc clone() {
        Arc a = new Arc(
                center != null ? center.clone() : null,
                radius,
                startAngle,
                endAngle,
                color,
                layer
        );
        a.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;
        return a;
    }
}
