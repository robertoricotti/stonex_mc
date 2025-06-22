package dxf;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import gui.my_opengl.GL_Methods;

public class Polyline implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient FloatBuffer vertexBuffer;
    List<Point3D> vertices = new ArrayList<>();
    private int lineColor=0;//da DXF
    Layer layer;

    public Polyline() {
    }

    public Polyline(List<Point3D> vertices,Layer layer) {
        this.vertices = vertices;
        this.layer=layer;
    }



    public List<Point3D> getVertices() {
        return vertices;
    }

    public void setVertices(List<Point3D> vertices) {
        this.vertices = vertices;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(FloatBuffer buffer) {
        this.vertexBuffer = buffer;
    }

    public void prepareVertexBuffer2D() {
        if (vertices == null || vertices.isEmpty()) return;

        float[] coords = new float[vertices.size() * 3];
        for (int i = 0; i < vertices.size(); i++) {
            Point3D p = vertices.get(i);
            coords[i * 3] = (float) p.getX();
            coords[i * 3 + 1] = (float) p.getY();
            coords[i * 3 + 2] = 0f; // azzera la Z
        }

        vertexBuffer = GL_Methods.createFloatBuffer(coords);
    }
    public int getVertexCount() {
        return vertices != null ? vertices.size() : 0;
    }
}
