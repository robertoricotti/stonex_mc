package dxf;

import java.io.Serializable;

public class XLine implements Serializable {
    private static final long serialVersionUID = 1L;

    Point3D basePoint;
    Point3D direction;
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    public XLine(Point3D basePoint, Point3D direction, int color, Layer layer) {
        this.basePoint = basePoint;
        this.direction = direction;
        this.color = color;
        this.layer = layer;
    }

    public Point3D getBasePoint() { return basePoint; }
    public void setBasePoint(Point3D basePoint) { this.basePoint = basePoint; }

    public Point3D getDirection() { return direction; }
    public void setDirection(Point3D direction) { this.direction = direction; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public Layer getLayer() { return layer; }
    public void setLayer(Layer layer) { this.layer = layer; }

    public DxfStyle getDxfStyle() { return dxfStyle; }
    public void setDxfStyle(DxfStyle dxfStyle) {
        this.dxfStyle = dxfStyle != null ? dxfStyle.copy() : null;
    }

    @Override
    public XLine clone() {
        XLine x = new XLine(
                basePoint != null ? basePoint.clone() : null,
                direction != null ? direction.clone() : null,
                color,
                layer
        );
        x.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;
        return x;
    }
}
