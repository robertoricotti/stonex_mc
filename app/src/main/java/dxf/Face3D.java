package dxf;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import gui.my_opengl.GL_Methods;
import gui.my_opengl.VBOHelper;
import gui.my_opengl.compat.GL11;

public class Face3D implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean isDirty = true;
    private int vboId = 0;

    Point3D p1, p2, p3, p4;
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    private FloatBuffer vertexBuffer3D = null;
    private FloatBuffer vertexBuffer2D = null;

    private float lastScale3D = -1f;
    private double[] lastBucketCenter3D = null;

    private float lastScale2D = -1f;
    private double[] lastBucketCenter2D = null;

    public Face3D(Point3D p1, Point3D p2, Point3D p3, Point3D p4, int color, Layer layer) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
        this.color = color;
        this.layer = layer;
    }

    public double[][] toArrayWithCentroid() {
        Point3D centroid = Point3D.calculateCentroid(p1, p2, p3, effectiveP4());
        return new double[][]{
                {p1.x, p1.y, p1.z},
                {p2.x, p2.y, p2.z},
                {p3.x, p3.y, p3.z},
                {centroid.x, centroid.y, centroid.z},
        };
    }

    public Point3D getP1() {
        return p1;
    }

    public Point3D getP2() {
        return p2;
    }

    public Point3D getP3() {
        return p3;
    }

    public Point3D getP4() {
        return p4;
    }

    public void setP1(Point3D p1) {
        this.p1 = p1;
    }

    public void setP2(Point3D p2) {
        this.p2 = p2;
    }

    public void setP3(Point3D p3) {
        this.p3 = p3;
    }

    public void setP4(Point3D p4) {
        this.p4 = p4;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
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

    public void prepareVertexBuffer(double[] bucketCenter, float scale) {
        if (vertexBuffer3D != null && lastBucketCenter3D != null
                && bucketCenter[0] == lastBucketCenter3D[0]
                && bucketCenter[1] == lastBucketCenter3D[1]
                && bucketCenter[2] == lastBucketCenter3D[2]
                && scale == lastScale3D) {
            return;
        }

        lastBucketCenter3D = bucketCenter.clone();
        lastScale3D = scale;

        Point3D[] pts = isTriangle()
                ? new Point3D[]{getP1(), getP2(), getP3()}
                : new Point3D[]{getP1(), getP2(), getP3(), effectiveP4()};

        float[] coords = new float[pts.length * 3];
        for (int i = 0; i < pts.length; i++) {
            coords[i * 3] = (float) ((pts[i].getX() - bucketCenter[0]) * scale);
            coords[i * 3 + 1] = (float) ((pts[i].getY() - bucketCenter[1]) * scale);
            coords[i * 3 + 2] = (float) ((pts[i].getZ() - bucketCenter[2]) * scale);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer3D = bb.asFloatBuffer();
        vertexBuffer3D.put(coords);
        vertexBuffer3D.position(0);

        setDirty(true);
    }

    public FloatBuffer getVertexBuffer3D() {
        return vertexBuffer3D;
    }

    public FloatBuffer getVertexBuffer2D() {
        return vertexBuffer2D;
    }

    public void prepareVertexBuffer2D() {
        Point3D q4 = effectiveP4();
        float[] coords = new float[]{
                (float) p1.getX(), (float) p1.getY(), 0f,
                (float) p2.getX(), (float) p2.getY(), 0f,
                (float) p3.getX(), (float) p3.getY(), 0f,
                (float) q4.getX(), (float) q4.getY(), 0f
        };
        vertexBuffer2D = GL_Methods.createFloatBuffer(coords);
    }

    public List<Point3D> getVertices() {
        return isTriangle()
                ? Arrays.asList(p1, p2, p3)
                : Arrays.asList(p1, p2, p3, effectiveP4());
    }

    public void prepareVertexBuffer2DForGradient(double[] bucketCenter, float scale) {
        if (vertexBuffer2D != null && lastBucketCenter2D != null
                && bucketCenter[0] == lastBucketCenter2D[0]
                && bucketCenter[1] == lastBucketCenter2D[1]
                && bucketCenter[2] == lastBucketCenter2D[2]
                && scale == lastScale2D) {
            return;
        }

        lastBucketCenter2D = bucketCenter.clone();
        lastScale2D = scale;

        Point3D q4 = effectiveP4();
        Point3D[] pts = isTriangle()
                ? new Point3D[]{p1, p2, p3}
                : new Point3D[]{p1, p2, p3, q4};

        float[] coords = new float[pts.length * 3];
        for (int i = 0; i < pts.length; i++) {
            coords[i * 3] = (float) ((pts[i].getX() - bucketCenter[0]) * scale);
            coords[i * 3 + 1] = (float) ((pts[i].getY() - bucketCenter[1]) * scale);
            coords[i * 3 + 2] = 0f;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer2D = bb.asFloatBuffer();
        vertexBuffer2D.put(coords);
        vertexBuffer2D.position(0);
    }

    public List<Point3D> getVerticesWithZ() {
        return isTriangle()
                ? Arrays.asList(p1, p2, p3)
                : Arrays.asList(p1, p2, p3, effectiveP4());
    }

    public int getOrCreateVboId(GL11 gl) {
        if (vboId == 0 && vertexBuffer3D != null) {
            vboId = VBOHelper.createVbo(gl, vertexBuffer3D);
        }
        return vboId;
    }

    public void uploadToVbo(GL11 gl) {
        if (vertexBuffer3D == null) return;

        if (vboId == 0) {
            vboId = VBOHelper.createVbo(gl, vertexBuffer3D);
        } else if (isDirty) {
            VBOHelper.uploadVertexBuffer(gl, vboId, vertexBuffer3D);
        }

        isDirty = false;
    }

    public void resetVbo() {
        this.vboId = 0;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    private boolean isTriangle() {
        Point3D q4 = effectiveP4();
        return q4.equals(p3);
    }

    private Point3D effectiveP4() {
        return p4 != null ? p4 : p3;
    }

    @Override
    public Face3D clone() {
        Face3D cloned = new Face3D(
                p1 != null ? p1.clone() : null,
                p2 != null ? p2.clone() : null,
                p3 != null ? p3.clone() : null,
                p4 != null ? p4.clone() : null,
                color,
                layer
        );
        cloned.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;
        return cloned;
    }
}
