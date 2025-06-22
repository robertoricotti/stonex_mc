package dxf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Polyline_2D implements Serializable {
    private static final long serialVersionUID = 1L;
    List<Point3D> vertices = new ArrayList<>();
    private boolean isClosed = false;
    private double thickness = 0.0; // opzionale, se usato nel DXF
    private int lineColor=0;//da DXF
    Layer layer;

    public Polyline_2D() {}

    public Polyline_2D(List<Point3D> vertices,Layer layer) {
        this.vertices = vertices;
        this.layer=layer;
    }

    public List<Point3D> getVertices() {
        return vertices;
    }

    public void setVertices(List<Point3D> vertices) {
        this.vertices = vertices;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public double getThickness() {
        return thickness;
    }

    public void setThickness(double thickness) {
        this.thickness = thickness;
    }

    public void addVertex(Point3D vertex) {
        vertices.add(vertex);
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }
}
