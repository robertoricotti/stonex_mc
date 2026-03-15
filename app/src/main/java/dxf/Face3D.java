package dxf;



import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import gui.my_opengl.compat.GL11;

import gui.my_opengl.GL_Methods;
import gui.my_opengl.VBOHelper;

public class Face3D implements Serializable {
    private boolean isDirty = true;
    private int vboId = 0;
    private static final long serialVersionUID = 1L;

    Point3D p1, p2, p3, p4;
    int color;
    Layer layer;
    private FloatBuffer vertexBuffer = null;
    private float lastScale = -1f;
    private double[] lastBucketCenter = null;

    public Face3D(Point3D p1, Point3D p2, Point3D p3, Point3D p4, int color,Layer layer) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
        this.color = color;
        this.layer=layer;
    }


    // Metodo per ottenere un array di double rappresentante le coordinate XYZ e il centroide
    public double[][] toArrayWithCentroid() {
        Point3D centroid = Point3D.calculateCentroid(p1, p2, p3, p4);
        double[][] result = new double[][]{
                {p1.x, p1.y, p1.z},
                {p2.x, p2.y, p2.z},
                {p3.x, p3.y, p3.z},
                {centroid.x, centroid.y, centroid.z},

        };

        return result;
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
    public int getColor(){
        return color;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }


    public void prepareVertexBuffer(double[] bucketCenter, float scale) {
        if (vertexBuffer != null && lastBucketCenter != null &&
                bucketCenter[0] == lastBucketCenter[0] &&
                bucketCenter[1] == lastBucketCenter[1] &&
                bucketCenter[2] == lastBucketCenter[2] &&
                scale == lastScale) {
            return; // niente da aggiornare
        }
        // altrimenti aggiorna
        lastBucketCenter = bucketCenter.clone(); // oppure copia manuale
        lastScale = scale;


        Point3D[] pts;
        if (getP4().equals(getP3())) {
            pts = new Point3D[]{getP1(), getP2(), getP3()};
        } else {
            pts = new Point3D[]{getP1(), getP2(), getP3(), getP4()};
        }

        float[] coords = new float[pts.length * 3];
        for (int i = 0; i < pts.length; i++) {
            coords[i * 3] = (float) ((pts[i].getX() - bucketCenter[0]) * scale);
            coords[i * 3 + 1] = (float) ((pts[i].getY() - bucketCenter[1]) * scale);
            coords[i * 3 + 2] = (float) ((pts[i].getZ() - bucketCenter[2]) * scale);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);
        setDirty(true);
        lastScale = scale;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void prepareVertexBuffer2D() {
        float[] coords = new float[]{
                (float) (p1.getX()), (float) (p1.getY()), 0f,
                (float) (p2.getX()), (float) (p2.getY()), 0f,
                (float) (p3.getX()), (float) (p3.getY()), 0f,
                (float) (p4.getX()), (float) (p4.getY()), 0f
        };
        vertexBuffer = GL_Methods.createFloatBuffer(coords);
    }
    public List<Point3D> getVertices() {
        if (p4.equals(p3)) {
            return Arrays.asList(p1, p2, p3); // triangolo
        } else {
            return Arrays.asList(p1, p2, p3, p4); // quadrilatero
        }
    }
    public void prepareVertexBuffer2DForGradient(double[] bucketCenter, float scale) {
        Point3D[] pts = p4.equals(p3) ? new Point3D[]{p1, p2, p3} : new Point3D[]{p1, p2, p3, p4};

        float[] coords = new float[pts.length * 3];
        for (int i = 0; i < pts.length; i++) {
            coords[i * 3] = (float) ((pts[i].getX() - bucketCenter[0]) * scale);
            coords[i * 3 + 1] = (float) ((pts[i].getY() - bucketCenter[1]) * scale);
            coords[i * 3 + 2] = 0f; // appiattimento per 2D
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);
    }

    public List<Point3D> getVerticesWithZ() {
        return p4.equals(p3) ? Arrays.asList(p1, p2, p3) : Arrays.asList(p1, p2, p3, p4);
    }

    public int getOrCreateVboId(GL11 gl) {
        if (vboId == 0 && vertexBuffer != null) {
            vboId = VBOHelper.createVbo(gl, vertexBuffer);
        }
        return vboId;
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
    public void uploadToVbo(GL11 gl) {
        if (vertexBuffer == null) return;

        if (vboId == 0) {
            // Primo caricamento
            vboId = VBOHelper.createVbo(gl, vertexBuffer);
        } else if (isDirty) {
            // Ricarica i dati se sono cambiati
            VBOHelper.uploadVertexBuffer(gl, vboId, vertexBuffer);
        }

        isDirty = false; // Reset stato dirty dopo l'upload
    }

    @Override
    public Face3D clone() {
        return new Face3D(
                p1 != null ? p1.clone() : null,
                p2 != null ? p2.clone() : null,
                p3 != null ? p3.clone() : null,
                p4 != null ? p4.clone() : null,
                color,
                layer
        );
    }
}
