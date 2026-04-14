package dxf;

import java.io.Serializable;

public class Line implements Serializable {
    private static final long serialVersionUID = 1L;

    Point3D start;
    Point3D end;
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    public Line(Point3D start, Point3D end, int color, Layer layer) {
        this.start = start;
        this.end = end;
        this.color = color;
        this.layer = layer;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setStart(Point3D start) {
        this.start = start;
    }

    public void setEnd(Point3D end) {
        this.end = end;
    }

    public Point3D getStart() {
        return start;
    }

    public Point3D getEnd() {
        return end;
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
    public Line clone() {
        Line l = new Line(
                start != null ? start.clone() : null,
                end != null ? end.clone() : null,
                color,
                layer
        );
        l.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;
        return l;
    }
}
