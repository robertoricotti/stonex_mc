package dxf;

import java.io.Serializable;

public class Ellipse implements Serializable {
    private static final long serialVersionUID = 1L;

    Point3D center;
    Point3D majorAxisEnd;   // vettore asse maggiore relativo al centro
    double axisRatio;       // minor/major
    double startParam;      // radianti
    double endParam;        // radianti
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    public Ellipse(Point3D center, Point3D majorAxisEnd, double axisRatio,
                   double startParam, double endParam, int color, Layer layer) {
        this.center = center;
        this.majorAxisEnd = majorAxisEnd;
        this.axisRatio = axisRatio;
        this.startParam = startParam;
        this.endParam = endParam;
        this.color = color;
        this.layer = layer;
    }

    public Point3D getCenter() { return center; }
    public void setCenter(Point3D center) { this.center = center; }

    public Point3D getMajorAxisEnd() { return majorAxisEnd; }
    public void setMajorAxisEnd(Point3D majorAxisEnd) { this.majorAxisEnd = majorAxisEnd; }

    public double getAxisRatio() { return axisRatio; }
    public void setAxisRatio(double axisRatio) { this.axisRatio = axisRatio; }

    public double getStartParam() { return startParam; }
    public void setStartParam(double startParam) { this.startParam = startParam; }

    public double getEndParam() { return endParam; }
    public void setEndParam(double endParam) { this.endParam = endParam; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public Layer getLayer() { return layer; }
    public void setLayer(Layer layer) { this.layer = layer; }

    public DxfStyle getDxfStyle() { return dxfStyle; }
    public void setDxfStyle(DxfStyle dxfStyle) {
        this.dxfStyle = dxfStyle != null ? dxfStyle.copy() : null;
    }

    @Override
    public Ellipse clone() {
        Ellipse e = new Ellipse(
                center != null ? center.clone() : null,
                majorAxisEnd != null ? majorAxisEnd.clone() : null,
                axisRatio,
                startParam,
                endParam,
                color,
                layer
        );
        e.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;
        return e;
    }
}
