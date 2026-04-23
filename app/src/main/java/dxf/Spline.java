package dxf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Spline implements Serializable {
    private static final long serialVersionUID = 1L;

    List<Point3D> controlPoints = new ArrayList<>();
    List<Point3D> fitPoints = new ArrayList<>();
    List<Double> knots = new ArrayList<>();
    List<Double> weights = new ArrayList<>();

    int degree;
    boolean closed;
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    public List<Point3D> getControlPoints() { return controlPoints; }
    public List<Point3D> getFitPoints() { return fitPoints; }
    public List<Double> getKnots() { return knots; }
    public List<Double> getWeights() { return weights; }

    public int getDegree() { return degree; }
    public void setDegree(int degree) { this.degree = degree; }

    public boolean isClosed() { return closed; }
    public void setClosed(boolean closed) { this.closed = closed; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public Layer getLayer() { return layer; }
    public void setLayer(Layer layer) { this.layer = layer; }

    public DxfStyle getDxfStyle() { return dxfStyle; }
    public void setDxfStyle(DxfStyle dxfStyle) {
        this.dxfStyle = dxfStyle != null ? dxfStyle.copy() : null;
    }

    @Override
    public Spline clone() {
        Spline s = new Spline();
        s.degree = degree;
        s.closed = closed;
        s.color = color;
        s.layer = layer;
        s.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;

        for (Point3D p : controlPoints) s.controlPoints.add(p.clone());
        for (Point3D p : fitPoints) s.fitPoints.add(p.clone());
        s.knots.addAll(knots);
        s.weights.addAll(weights);
        return s;
    }
}