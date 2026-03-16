package dxf;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.nio.FloatBuffer;
import gui.my_opengl.GL_Methods;
import gui.my_opengl.GL_Methods;

public class Polyline implements Serializable {
    private transient FloatBuffer cachedVertexBuffer;
    private transient double[] cachedAnchor;
    private transient float cachedScale = Float.NaN;
    private transient boolean glDirty = true;
    private transient int cachedVertexCount = 0;
    String filename;
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
    public Polyline(List<Point3D> vertices,String filename,Layer layer) {
        this.filename=filename;
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

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public Polyline clone() {
        Polyline p = new Polyline();
        p.lineColor = this.lineColor;
        p.layer = this.layer;
        p.filename = this.filename;

        for (Point3D v : vertices) {
            p.vertices.add(v.clone());
        }
        return p;
    }

    public void markGlDirty() {
        glDirty = true;
    }

    public int getCachedVertexCount() {
        return cachedVertexCount;
    }

    public FloatBuffer getOrBuildGlBuffer(double[] anchor, float scale) {
        if (!glDirty
                && cachedVertexBuffer != null
                && cachedAnchor != null
                && cachedAnchor.length == 3
                && cachedAnchor[0] == anchor[0]
                && cachedAnchor[1] == anchor[1]
                && cachedAnchor[2] == anchor[2]
                && cachedScale == scale) {
            cachedVertexBuffer.position(0);
            return cachedVertexBuffer;
        }

        if (getVertices() == null || getVertices().size() < 2) {
            cachedVertexBuffer = null;
            cachedVertexCount = 0;
            return null;
        }

        float[] coords = new float[getVertices().size() * 3];
        for (int i = 0; i < getVertices().size(); i++) {
            Point3D pt = getVertices().get(i);
            coords[i * 3] = (float) ((pt.getX() - anchor[0]) * scale);
            coords[i * 3 + 1] = (float) ((pt.getY() - anchor[1]) * scale);
            coords[i * 3 + 2] = (float) ((pt.getZ() - anchor[2]) * scale);
        }

        cachedVertexBuffer = GL_Methods.createFloatBuffer(coords);
        cachedVertexCount = coords.length / 3;
        cachedAnchor = anchor.clone();
        cachedScale = scale;
        glDirty = false;

        cachedVertexBuffer.position(0);
        return cachedVertexBuffer;
    }
}
