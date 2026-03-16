package dxf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import gui.my_opengl.GLDrawer;
import gui.my_opengl.GL_Methods;
public class Polyline_2D implements Serializable {
    private transient FloatBuffer cachedSegmentBuffer;
    private transient double[] cachedAnchor;
    private transient float cachedScale = Float.NaN;
    private transient boolean glDirty = true;
    private transient int cachedVertexCount = 0;
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

    @Override
    public Polyline_2D clone() {
        Polyline_2D p = new Polyline_2D();
        p.lineColor = this.lineColor;
        p.layer = this.layer;

        for (Point3D v : vertices) {
            Point3D c = v.clone();
            c.setBulge(v.getBulge());
            p.vertices.add(c);
        }
        return p;
    }

    public void markGlDirty() {
        glDirty = true;
    }

    public int getCachedVertexCount() {
        return cachedVertexCount;
    }

    public FloatBuffer getOrBuildGlLineBuffer(double[] anchor, float scale) {
        if (!glDirty
                && cachedSegmentBuffer != null
                && cachedAnchor != null
                && cachedAnchor.length == 3
                && cachedAnchor[0] == anchor[0]
                && cachedAnchor[1] == anchor[1]
                && cachedAnchor[2] == anchor[2]
                && cachedScale == scale) {
            cachedSegmentBuffer.position(0);
            return cachedSegmentBuffer;
        }

        List<Point3D> vertices = getVertices();
        if (vertices == null || vertices.size() < 2) {
            cachedSegmentBuffer = null;
            cachedVertexCount = 0;
            return null;
        }

        List<Float> out = new ArrayList<>();

        for (int i = 0; i < vertices.size() - 1; i++) {
            Point3D p1 = vertices.get(i);
            Point3D p2 = vertices.get(i + 1);
            double bulge = p1.getBulge();

            if (bulge == 0) {
                out.add((float) ((p1.getX() - anchor[0]) * scale));
                out.add((float) ((p1.getY() - anchor[1]) * scale));
                out.add(0f);

                out.add((float) ((p2.getX() - anchor[0]) * scale));
                out.add((float) ((p2.getY() - anchor[1]) * scale));
                out.add(0f);
            } else {
                float[][] arcPoints = Math.abs(bulge) > 1
                        ? computeArcPointsM(p1, p2, bulge, scale, anchor)
                        : computeArcPoints(p1, p2, bulge, scale, anchor);

                for (int j = 0; j < arcPoints.length - 1; j++) {
                    out.add(arcPoints[j][0]);
                    out.add(arcPoints[j][1]);
                    out.add(arcPoints[j][2]);

                    out.add(arcPoints[j + 1][0]);
                    out.add(arcPoints[j + 1][1]);
                    out.add(arcPoints[j + 1][2]);
                }
            }
        }

        float[] coords = new float[out.size()];
        for (int i = 0; i < out.size(); i++) {
            coords[i] = out.get(i);
        }

        cachedSegmentBuffer = GL_Methods.createFloatBuffer(coords);
        cachedVertexCount = coords.length / 3;
        cachedAnchor = anchor.clone();
        cachedScale = scale;
        glDirty = false;

        cachedSegmentBuffer.position(0);
        return cachedSegmentBuffer;
    }
    private static float[][] computeArcPointsM(Point3D p1, Point3D p2, double bulge, float scale, double[] bucket) {
        final int numSegments = 32;

        float x1 = (float) ((p1.getX() - bucket[0]) * scale);
        float y1 = (float) ((p1.getY() - bucket[1]) * scale);
        float z1 = (float) ((p1.getZ() - bucket[2]) * scale);

        float x2 = (float) ((p2.getX() - bucket[0]) * scale);
        float y2 = (float) ((p2.getY() - bucket[1]) * scale);

        double chordLength = Math.hypot(x2 - x1, y2 - y1);
        double theta = 4 * Math.atan(Math.abs(bulge));
        double radius = Math.abs((chordLength / 2d) / Math.sin(theta / 2d));

        float midX = (x1 + x2) / 2f;
        float midY = (y1 + y2) / 2f;

        double sagitta = Math.sqrt(radius * radius - (chordLength / 2d) * (chordLength / 2d));

        float dx = x2 - x1;
        float dy = y2 - y1;
        float perpX = -dy;
        float perpY = dx;
        float norm = (float) Math.hypot(perpX, perpY);
        perpX /= norm;
        perpY /= norm;

        float centerX = midX + perpX * (float) sagitta * (bulge > 0 ? -1 : 1);
        float centerY = midY + perpY * (float) sagitta * (bulge > 0 ? -1 : 1);

        float startAngle = (float) Math.atan2(y1 - centerY, x1 - centerX);
        float endAngle = (float) Math.atan2(y2 - centerY, x2 - centerX);

        float sweepAngle = endAngle - startAngle;
        if (bulge > 0) {
            if (sweepAngle < 0) sweepAngle += 2f * (float) Math.PI;
        } else {
            if (sweepAngle > 0) sweepAngle -= 2f * (float) Math.PI;
        }

        float[][] arcPoints = new float[numSegments + 1][3];
        for (int i = 0; i <= numSegments; i++) {
            float angle = startAngle + (sweepAngle * i / numSegments);
            arcPoints[i][0] = (float) (centerX + radius * Math.cos(angle));
            arcPoints[i][1] = (float) (centerY + radius * Math.sin(angle));
            arcPoints[i][2] = z1;
        }
        return arcPoints;
    }

    private static float[][] computeArcPoints(Point3D p1, Point3D p2, double bulge, float scale, double[] bucket) {
        final int numSegments = 32;

        float x1 = (float) ((p1.getX() - bucket[0]) * scale);
        float y1 = (float) ((p1.getY() - bucket[1]) * scale);
        float z1 = (float) ((p1.getZ() - bucket[2]) * scale);

        float x2 = (float) ((p2.getX() - bucket[0]) * scale);
        float y2 = (float) ((p2.getY() - bucket[1]) * scale);

        double distance = Math.hypot(x2 - x1, y2 - y1);
        double theta = 4 * Math.atan(Math.abs(bulge));
        double radius = (distance / 2d) / Math.abs(Math.sin(theta / 2d));

        float midX = (x1 + x2) / 2f;
        float midY = (y1 + y2) / 2f;

        double height = Math.sqrt(radius * radius - (distance / 2d) * (distance / 2d));

        float dx = x2 - x1;
        float dy = y2 - y1;
        float perpX = -dy;
        float perpY = dx;
        float norm = (float) Math.hypot(perpX, perpY);
        perpX /= norm;
        perpY /= norm;

        float centerX = midX + perpX * (float) height * (bulge > 0 ? 1 : -1);
        float centerY = midY + perpY * (float) height * (bulge > 0 ? 1 : -1);

        float startAngle = (float) Math.atan2(y1 - centerY, x1 - centerX);
        float endAngle = (float) Math.atan2(y2 - centerY, x2 - centerX);

        float sweepAngle = endAngle - startAngle;
        if (bulge > 0) {
            if (sweepAngle < 0) sweepAngle += 2f * (float) Math.PI;
        } else {
            if (sweepAngle > 0) sweepAngle -= 2f * (float) Math.PI;
        }

        if (Math.abs(bulge) > 1 && Math.abs(Math.toDegrees(sweepAngle)) < 180) {
            sweepAngle = (float) ((bulge > 0)
                    ? (2 * Math.PI - Math.abs(sweepAngle))
                    : -(2 * Math.PI - Math.abs(sweepAngle)));
        }

        float[][] arcPoints = new float[numSegments + 1][3];
        for (int i = 0; i <= numSegments; i++) {
            float angle = startAngle + (sweepAngle * i / numSegments);
            arcPoints[i][0] = (float) (centerX + radius * Math.cos(angle));
            arcPoints[i][1] = (float) (centerY + radius * Math.sin(angle));
            arcPoints[i][2] = z1;
        }

        return arcPoints;
    }
}
