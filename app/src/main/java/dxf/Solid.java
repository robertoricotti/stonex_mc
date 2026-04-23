package dxf;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Solid implements Serializable {
    private static final long serialVersionUID = 1L;

    Point3D p1, p2, p3, p4;
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    public Solid(Point3D p1, Point3D p2, Point3D p3, Point3D p4, int color, Layer layer) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
        this.color = color;
        this.layer = layer;
    }

    public List<Point3D> getVertices() {
        return Arrays.asList(p1, p2, p3, p4);
    }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public Layer getLayer() { return layer; }
    public void setLayer(Layer layer) { this.layer = layer; }

    public DxfStyle getDxfStyle() { return dxfStyle; }
    public void setDxfStyle(DxfStyle dxfStyle) {
        this.dxfStyle = dxfStyle != null ? dxfStyle.copy() : null;
    }

    @Override
    public Solid clone() {
        Solid s = new Solid(
                p1 != null ? p1.clone() : null,
                p2 != null ? p2.clone() : null,
                p3 != null ? p3.clone() : null,
                p4 != null ? p4.clone() : null,
                color,
                layer
        );
        s.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;
        return s;
    }
}