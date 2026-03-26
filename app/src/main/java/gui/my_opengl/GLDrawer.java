package gui.my_opengl;

import static gui.my_opengl.GL_Methods.createFloatBuffer;
import static gui.my_opengl.GL_Methods.getJetColor;
import static packexcalib.exca.DataSaved.GL_BENNA;
import static packexcalib.exca.DataSaved.GL_LAMA;
import static packexcalib.exca.DataSaved.GL_WHEEL;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.WHEELLOADER;

import android.graphics.Color;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dxf.Arc;
import dxf.AutoCADColor;
import dxf.Circle;
import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Line;
import dxf.PNEZDPoint;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Polyline_2D;
import gui.my_opengl.compat.GL11;
import packexcalib.exca.DataSaved;

public class GLDrawer {
    //CULLING HELPERS
    private static final float CULL_MARGIN_XY = 0.15f;
    private static final float CULL_MARGIN_Z = 0.10f;

    private static boolean isAabbVisible(
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ
    ) {
        if (!hasVpMatrix) return true;

        float[] out = new float[4];

        float[][] corners = new float[][]{
                {minX, minY, minZ, 1f},
                {maxX, minY, minZ, 1f},
                {minX, maxY, minZ, 1f},
                {maxX, maxY, minZ, 1f},
                {minX, minY, maxZ, 1f},
                {maxX, minY, maxZ, 1f},
                {minX, maxY, maxZ, 1f},
                {maxX, maxY, maxZ, 1f}
        };

        boolean allLeft = true;
        boolean allRight = true;
        boolean allBottom = true;
        boolean allTop = true;
        boolean allNear = true;
        boolean allFar = true;

        for (float[] c : corners) {
            Matrix.multiplyMV(out, 0, sVpMatrix, 0, c, 0);

            float w = out[3];
            if (Math.abs(w) < 1e-6f) continue;

            float nx = out[0] / w;
            float ny = out[1] / w;
            float nz = out[2] / w;

            if (nx >= -1f - CULL_MARGIN_XY) allLeft = false;
            if (nx <= 1f + CULL_MARGIN_XY) allRight = false;

            if (ny >= -1f - CULL_MARGIN_XY) allBottom = false;
            if (ny <= 1f + CULL_MARGIN_XY) allTop = false;

            if (nz >= -1f - CULL_MARGIN_Z) allNear = false;
            if (nz <= 1f + CULL_MARGIN_Z) allFar = false;
        }

        return !(allLeft || allRight || allBottom || allTop || allNear || allFar);
    }

    private static boolean isPolylineVisible(Polyline polyline, double[] bucketCenter, float scale) {
        if (polyline == null) return false;

        List<Point3D> pts = polyline.getVertices();
        if (pts == null || pts.isEmpty()) return false;

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (Point3D p : pts) {
            float x = (float) ((p.getX() - bucketCenter[0]) * scale);
            float y = (float) ((p.getY() - bucketCenter[1]) * scale);
            float z = (float) ((p.getZ() - bucketCenter[2]) * scale);

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        return isAabbVisible(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static boolean isFace3DVisible(Face3D face, double[] bucketCenter, float scale) {
        if (face == null) return false;

        List<Point3D> pts = face.getVertices();
        if (pts == null || pts.isEmpty()) return false;

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (Point3D p : pts) {
            float x = (float) ((p.getX() - bucketCenter[0]) * scale);
            float y = (float) ((p.getY() - bucketCenter[1]) * scale);
            float z = (float) ((p.getZ() - bucketCenter[2]) * scale);

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        float dz = (float) (DataSaved.offsetH * scale);
        minZ -= dz;
        maxZ -= dz;

        return isAabbVisible(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static boolean isFace3DVisible2D(Face3D face, double[] bucketCenter, float scale) {
        if (face == null) return false;

        List<Point3D> pts = face.getVerticesWithZ();
        if (pts == null || pts.isEmpty()) return false;

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (Point3D p : pts) {
            float x = (float) ((p.getX() - bucketCenter[0]) * scale);
            float y = (float) ((p.getY() - bucketCenter[1]) * scale);

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        return isAabbVisible(minX, minY, -0.01f, maxX, maxY, 0.01f);
    }

    private static boolean isPoint3DVisible(Point3D p, double[] bucketCenter, float scale, float radiusPx) {
        if (p == null) return false;

        float x = (float) ((p.getX() - bucketCenter[0]) * scale);
        float y = (float) ((p.getY() - bucketCenter[1]) * scale);
        float z = (float) ((p.getZ() - bucketCenter[2]) * scale);

        float r = Math.max(0.05f, radiusPx * 0.75f);

        return isAabbVisible(
                x - r, y - r, z - 0.01f,
                x + r, y + r, z + 0.01f
        );
    }

    /// ////////////////////////////////////

    private static int viewportWidth = 1;
    private static int viewportHeight = 1;
    private static float orthoWorldHalfHeight = 1f;
    private static float orthoWorldHalfWidth = 1f;
    private static boolean isOrtho2D = false;
    private static final String TAG = "GLDrawer";

    private static final Map<String, Integer> textTextureCache = new HashMap<>();
    private static final Map<String, float[]> textSizeCache = new HashMap<>();

    private static final float[] sVpMatrix = new float[16];
    private static final float[] sViewMatrix = new float[16];
    private static boolean hasVpMatrix = false;
    private static boolean hasViewMatrix = false;

    private static final float[] tempModel = new float[16];
    private static final float[] tempMvp = new float[16];
    private static final float[] tempInvView = new float[16];

    private static ColorProgram colorProgram;
    private static TextureProgram textureProgram;

    private static final int CIRCLE_SEGMENTS_2D = 72;
    private static final int ARC_SEGMENTS_2D = 36;
    private static final int MIN_CIRCLE_SEGMENTS_2D = 96;
    private static final int MAX_CIRCLE_SEGMENTS_2D = 720;
    private static final int MIN_ARC_SEGMENTS_2D = 32;
    private static final int MAX_ARC_SEGMENTS_2D = 360;
    private static final float CIRCLE_SEGMENT_PIXELS = 6f;
    private static final float ARC_SEGMENT_PIXELS = 8f;
    private static final float ROUND_JOIN_STEP_DEG = 10f;

    private GLDrawer() {
    }

    // =========================
    // PUBLIC SETUP
    // =========================
    private static float currentScale() {
        return MyGLRenderer.currentRenderScale();
    }

    public static void init() {
        if (colorProgram == null) {
            colorProgram = new ColorProgram();
        }
        if (textureProgram == null) {
            textureProgram = new TextureProgram();
        }
    }

    public static void setViewProjectionMatrix(float[] vpMatrix) {
        if (vpMatrix != null && vpMatrix.length >= 16) {
            System.arraycopy(vpMatrix, 0, sVpMatrix, 0, 16);
            hasVpMatrix = true;
        }
    }

    public static void setViewMatrix(float[] viewMatrix) {
        if (viewMatrix != null && viewMatrix.length >= 16) {
            System.arraycopy(viewMatrix, 0, sViewMatrix, 0, 16);
            hasViewMatrix = true;
        }
    }

    private static boolean ensureReady() {
        if (colorProgram == null || textureProgram == null) {
            init();
        }
        return hasVpMatrix;
    }

    public static void drawFaces(GL11 gl, List<Face3D> faces, float lineW, float scala, boolean isXML) {
        if (faces == null || faces.isEmpty()) return;
        if (!ensureReady()) return;
        if (My3DActivity.glGradient) return;

        try {
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glLineWidth(Math.max(1f, lineW * scala));

            double[] bucketCenter = DataSaved.glL_AnchorView;

            int totalFaces = 0;
            int visibleCount = 0;

            List<Face3D> visibleFaces = new ArrayList<>();

            for (Face3D face : faces) {
                totalFaces++;

                if (face == null) continue;
                if (face.getLayer() == null || !isLayerEnabled(face.getLayer().getLayerName()))
                    continue;
                if (!isFace3DVisible(face, bucketCenter, scala)) continue;

                visibleFaces.add(face);
                visibleCount++;
            }

            visibleFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            for (Face3D face : visibleFaces) {
                face.prepareVertexBuffer(bucketCenter, scala);
                FloatBuffer buffer = face.getVertexBuffer3D();
                if (buffer == null) continue;

                int color = isXML
                        ? GL_Methods.myParseColor(AutoCADColor.getColor(String.valueOf(face.getLayer().getColorState())))
                        : GL_Methods.myParseColor(face.getLayer().getColorState());
                float[] rgb = GL_Methods.parseColorToGL(color);

                boolean isTriangle = face.getP4().equals(face.getP3());
                int vertexCount = isTriangle ? 3 : 4;
                int drawMode = isTriangle ? GLES20.GL_TRIANGLES : GLES20.GL_TRIANGLE_FAN;

                float[] modelMatrix = buildTranslationModel(0f, 0f, (float) (-DataSaved.offsetH * scala));

                if (My3DActivity.glFill) {
                    buffer.position(0);
                    drawColoredVertices(buffer, vertexCount, drawMode, modelMatrix, rgb[0], rgb[1], rgb[2], 0.30f);
                }

                if (My3DActivity.glFace) {
                    buffer.position(0);
                    drawColoredVertices(buffer, vertexCount, GLES20.GL_LINE_LOOP, modelMatrix, rgb[0], rgb[1], rgb[2], 1f);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "drawFaces", e);
        }
    }

    public static void drawFacesGradientPRO(GL11 gl, List<Face3D> faces, float scala, double zMax, double zMin) {
        if (faces == null || faces.isEmpty()) return;
        if (!ensureReady()) return;

        try {
            double[] bucketCenter = DataSaved.glL_AnchorView;

            int totalFaces = 0;
            int visibleCount = 0;

            List<Face3D> visibleFaces = new ArrayList<>();

            for (Face3D face : faces) {
                totalFaces++;

                if (face == null) continue;
                if (face.getLayer() == null || !isLayerEnabled(face.getLayer().getLayerName()))
                    continue;
                if (!isFace3DVisible(face, bucketCenter, scala)) continue;

                visibleFaces.add(face);
                visibleCount++;
            }

            visibleFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            for (Face3D face : visibleFaces) {
                face.prepareVertexBuffer(bucketCenter, scala);
                FloatBuffer vertexBuffer = face.getVertexBuffer3D();
                if (vertexBuffer == null) continue;

                List<Point3D> points = face.getVertices();
                int vertexCount = points.size();

                float[] colorArray = new float[vertexCount * 4];
                for (int i = 0; i < vertexCount; i++) {
                    float[] color = getJetColor(points.get(i).getZ(), zMin, zMax);
                    colorArray[i * 4] = color[0];
                    colorArray[i * 4 + 1] = color[1];
                    colorArray[i * 4 + 2] = color[2];
                    colorArray[i * 4 + 3] = 0.75f;
                }
                FloatBuffer colorBuffer = createFloatBuffer(colorArray);

                int drawMode = face.getP4().equals(face.getP3()) ? GLES20.GL_TRIANGLES : GLES20.GL_TRIANGLE_FAN;
                float[] modelMatrix = buildTranslationModel(0f, 0f, (float) (-DataSaved.offsetH * scala));

                vertexBuffer.position(0);
                colorBuffer.position(0);
                drawGradientVertices(vertexBuffer, colorBuffer, vertexCount, drawMode, modelMatrix);

                float[] baseColor = getJetColor(points.get(0).getZ(), zMin, zMax);
                float edgeR = Math.max(baseColor[0] * 0.5f, 0.05f);
                float edgeG = Math.max(baseColor[1] * 0.5f, 0.05f);
                float edgeB = Math.max(baseColor[2] * 0.5f, 0.05f);

                vertexBuffer.position(0);
                drawColoredVertices(vertexBuffer, vertexCount, GLES20.GL_LINE_LOOP, modelMatrix, edgeR, edgeG, edgeB, 0.85f);
            }
        } catch (Exception e) {
            Log.e(TAG, "drawFacesGradientPRO", e);
        }
    }

    public static void drawFacesGradient2D(GL11 gl, List<Face3D> faces, float scala, double zMax, double zMin) {
        if (faces == null || faces.isEmpty()) return;
        if (!ensureReady()) return;

        try {
            double[] bucketCenter = DataSaved.glL_AnchorView;

            int totalFaces = 0;
            int visibleCount = 0;

            List<Face3D> visibleFaces = new ArrayList<>();

            for (Face3D face : faces) {
                totalFaces++;

                if (face == null) continue;
                if (face.getLayer() == null || !isLayerEnabled(face.getLayer().getLayerName()))
                    continue;
                if (!isFace3DVisible2D(face, bucketCenter, scala)) continue;

                visibleFaces.add(face);
                visibleCount++;
            }

            visibleFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            for (Face3D face : visibleFaces) {
                face.prepareVertexBuffer2DForGradient(bucketCenter, scala);
                FloatBuffer vertexBuffer = face.getVertexBuffer2D();
                if (vertexBuffer == null) continue;

                List<Point3D> points = face.getVerticesWithZ();
                int vertexCount = points.size();
                float[] colorArray = new float[vertexCount * 4];

                for (int i = 0; i < vertexCount; i++) {
                    float[] color = GL_Methods.getJetColor(points.get(i).getZ(), zMin, zMax);
                    colorArray[i * 4] = color[0];
                    colorArray[i * 4 + 1] = color[1];
                    colorArray[i * 4 + 2] = color[2];
                    colorArray[i * 4 + 3] = 0.75f;
                }

                FloatBuffer colorBuffer = GL_Methods.createFloatBuffer(colorArray);
                int drawMode = face.getP4().equals(face.getP3()) ? GLES20.GL_TRIANGLES : GLES20.GL_TRIANGLE_FAN;
                float[] identity = identity();

                vertexBuffer.position(0);
                colorBuffer.position(0);
                drawGradientVertices(vertexBuffer, colorBuffer, vertexCount, drawMode, identity);

                float[] outlineCoords = buildFaceOutlineCoords2D(face, bucketCenter, scala);
                if (outlineCoords != null) {
                    float outlineWidth = Math.max(1.0f, 1.2f * scala);
                    drawThickSegments2D(outlineCoords, outlineWidth, new float[]{
                            1f, 1f, 1f, 1f
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "drawFacesGradient2D", e);
        }
    }
    // =========================
    // POLYLINES / LINES / ARCS / CIRCLES
    // =========================

    public static void drawPolylines(GL11 gl, List<Polyline> polylines, float lineW, float scala) {
        if (polylines == null || polylines.isEmpty()) return;
        if (!ensureReady()) return;

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glLineWidth(Math.max(1f, lineW * scala));

        double[] bucketCenter = DataSaved.glL_AnchorView;
        float[] identity = identity();

        for (Polyline polyline : polylines) {
            if (polyline.getLayer() == null || !isLayerEnabled(polyline.getLayer().getLayerName()))
                continue;
            if (!isPolylineVisible(polyline, bucketCenter, scala)) continue;

            FloatBuffer buffer = polyline.getOrBuildGlBuffer(bucketCenter, scala);
            int vertexCount = polyline.getCachedVertexCount();
            if (buffer == null || vertexCount < 2) continue;

            int color = GL_Methods.myParseColor(polyline.getLineColor());
            float[] rgb = GL_Methods.parseColorToGL(color);

            drawColoredVertices(buffer, vertexCount, GLES20.GL_LINE_STRIP, identity, rgb[0], rgb[1], rgb[2], 0.85f);
        }
    }

    public static void drawSelectedPoly(GL11 gl, Polyline polyline, float lineW, int color, float scala) {
        if (polyline == null) return;
        if (!ensureReady()) return;

        int c = GL_Methods.myParseColor(color);
        float[] rgb = GL_Methods.parseColorToGL(c);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glLineWidth(Math.max(1f, lineW * scala));

        double[] bucketCenter = DataSaved.glL_AnchorView;
        FloatBuffer buffer = polyline.getOrBuildGlBuffer(bucketCenter, scala);
        int vertexCount = polyline.getCachedVertexCount();

        if (buffer == null || vertexCount < 2) return;

        buffer.position(0);
        drawColoredVertices(
                buffer,
                vertexCount,
                GLES20.GL_LINE_STRIP,
                identity(),
                rgb[0], rgb[1], rgb[2], 1f
        );
    }

    public static void drawPoints(GL11 gl, List<Point3D> points, float radius, float scala, boolean isXMLPoint) {
        if (points == null || points.isEmpty()) return;
        if (!ensureReady()) return;

        double[] bucketCenter = DataSaved.glL_AnchorView;
        GLES20.glUniform1f(colorProgram.uPointSize, radius * scala * 2f);

        for (Point3D p : points) {
            if (p.getLayer() == null || !isLayerEnabled(p.getLayer().getLayerName())) continue;
            if (!isPoint3DVisible(p, bucketCenter, scala, radius)) continue;

            float x = (float) ((p.getX() - bucketCenter[0]) * scala);
            float y = (float) ((p.getY() - bucketCenter[1]) * scala);
            float z = (float) ((p.getZ() - bucketCenter[2]) * scala);

            int color = isXMLPoint
                    ? GL_Methods.myParseColor(Color.WHITE)
                    : GL_Methods.myParseColor(p.getLayer().getColorState());
            float[] rgb = GL_Methods.parseColorToGL(color);

            FloatBuffer buf = createFloatBuffer(new float[]{x, y, z});
            drawColoredVertices(buf, 1, GLES20.GL_POINTS, identity(), rgb[0], rgb[1], rgb[2], 1f);
        }
    }

    public static void drawPNEZD(GL11 gl, List<PNEZDPoint> points, float radius, float scala) {
        if (points == null || points.isEmpty()) return;
        if (!ensureReady()) return;

        double[] bucketCenter = DataSaved.glL_AnchorView;
        GLES20.glUniform1f(colorProgram.uPointSize, radius * scala * 2f);

        for (PNEZDPoint p : points) {
            float x = (float) ((p.getEasting() - bucketCenter[0]) * scala);
            float y = (float) ((p.getNorthing() - bucketCenter[1]) * scala);
            float z = (float) ((p.getElevation() - bucketCenter[2]) * scala);

            int color = GL_Methods.myParseColor(Color.WHITE);
            if (p.getColor() != null) {
                color = GL_Methods.myParseColor(p.getColor());
            }

            float[] rgb = GL_Methods.parseColorToGL(color);
            FloatBuffer buf = createFloatBuffer(new float[]{x, y, z});
            drawColoredVertices(buf, 1, GLES20.GL_POINTS, identity(), rgb[0], rgb[1], rgb[2], 1f);
        }
    }

    public static void drawTextsBilBoardPNEZD(GL11 gl, List<PNEZDPoint> texts, double[] anchor, float charSpacingFactor, float scala, FontAtlas atlas) {
        if (texts == null || texts.isEmpty() || atlas == null) return;
        if (!ensureReady() || !hasViewMatrix) return;
        drawBillboardTextsPnezdInternal(texts, anchor, charSpacingFactor, scala, atlas);
    }

    public static void drawTextsBilBoard(GL11 gl, List<DxfText> texts, double[] anchor, float charSpacingFactor, float scala, FontAtlas atlas) {
        if (texts == null || texts.isEmpty() || atlas == null) return;
        if (!ensureReady() || !hasViewMatrix) return;
        drawBillboardTextsDxfInternal(texts, anchor, charSpacingFactor, scala, atlas);
    }

    private static void drawBillboardTextsPnezdInternal(List<PNEZDPoint> texts, double[] anchor, float charSpacingFactor, float scala, FontAtlas atlas) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        float charW = atlas.getCellSize() * scala * 0.01f;
        float charH = atlas.getCellSize() * scala * 0.01f;

        float[] right = getCameraRight();
        float[] up = getCameraUp();

        for (PNEZDPoint text : texts) {
            String str = text.getDescription();
            if (str == null || str.isEmpty()) continue;

            float baseX = (float) ((text.getEasting() - anchor[0]) * scala);
            float baseY = (float) ((text.getNorthing() - anchor[1]) * scala);
            float baseZ = (float) ((text.getElevation() - anchor[2]) * scala);

            drawBillboardString(str, baseX, baseY, baseZ, charW, charH, charSpacingFactor, atlas, right, up);
        }
    }

    private static void drawBillboardTextsDxfInternal(List<DxfText> texts, double[] anchor, float charSpacingFactor, float scala, FontAtlas atlas) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        float charW = atlas.getCellSize() * scala * 0.01f;
        float charH = atlas.getCellSize() * scala * 0.01f;

        float[] right = getCameraRight();
        float[] up = getCameraUp();

        for (DxfText text : texts) {
            String str = text.getText();
            if (str == null || str.isEmpty()) continue;

            float baseX = (float) ((text.getX() - anchor[0]) * scala);
            float baseY = (float) ((text.getY() - anchor[1]) * scala);
            float baseZ = (float) ((text.getZ() - anchor[2]) * scala);

            drawBillboardString(str, baseX, baseY, baseZ, charW, charH, charSpacingFactor, atlas, right, up);
        }
    }

    private static void drawBillboardString(String str, float baseX, float baseY, float baseZ,
                                            float charW, float charH, float charSpacingFactor,
                                            FontAtlas atlas, float[] right, float[] up) {
        float cursor = 0f;

        for (char c : str.toCharArray()) {
            RectF uv = atlas.getUV(c);
            if (uv == null) {
                cursor += charW * charSpacingFactor;
                continue;
            }

            float x0 = baseX + right[0] * cursor;
            float y0 = baseY + right[1] * cursor;
            float z0 = baseZ + right[2] * cursor;

            float x1 = x0 + right[0] * charW;
            float y1 = y0 + right[1] * charW;
            float z1 = z0 + right[2] * charW;

            float x2 = x0 + up[0] * charH;
            float y2 = y0 + up[1] * charH;
            float z2 = z0 + up[2] * charH;

            float x3 = x1 + up[0] * charH;
            float y3 = y1 + up[1] * charH;
            float z3 = z1 + up[2] * charH;

            float[] vertices = {
                    x0, y0, z0,
                    x1, y1, z1,
                    x2, y2, z2,
                    x3, y3, z3
            };

            float[] tex = {
                    uv.left, uv.top,
                    uv.right, uv.top,
                    uv.left, uv.bottom,
                    uv.right, uv.bottom
            };

            FloatBuffer vertexBuffer = createFloatBuffer(vertices);
            FloatBuffer texBuffer = createFloatBuffer(tex);

            drawTexturedQuad(vertexBuffer, texBuffer, atlas.getTextureId(), 4, identity());
            cursor += charW * charSpacingFactor;
        }
    }

    // =========================
    // 2D ENTITIES
    // =========================

    private static boolean isLayerEnabled(String layerName) {
        if (layerName == null || layerName.isEmpty()) return false;

        for (Layer layer : DataSaved.dxfLayers_DTM) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) return true;
        }
        for (Layer layer : DataSaved.dxfLayers_POLY) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) return true;
        }
        for (Layer layer : DataSaved.dxfLayers_POINT) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) return true;
        }

        return false;
    }

    public static void drawLines2D(GL11 gl, List<Line> lines, float lineW, float scala) {
        if (lines == null || lines.isEmpty()) return;
        if (!ensureReady()) return;

        double[] bucket = DataSaved.glL_AnchorView;
        float width = Math.max(1f, lineW);

        for (Line line : lines) {
            if (line.getLayer() == null || !isLayerEnabled(line.getLayer().getLayerName()))
                continue;

            float[] coords = new float[6];
            coords[0] = (float) ((line.getStart().getX() - bucket[0]) * scala);
            coords[1] = (float) ((line.getStart().getY() - bucket[1]) * scala);
            coords[2] = 0f;

            coords[3] = (float) ((line.getEnd().getX() - bucket[0]) * scala);
            coords[4] = (float) ((line.getEnd().getY() - bucket[1]) * scala);
            coords[5] = 0f;

            float[] rgb = GL_Methods.parseColorToGL(GL_Methods.myParseColor(line.getColor()));
            drawThickSegments2D(coords, width, new float[]{rgb[0], rgb[1], rgb[2], 1f});
        }
    }

    public static void drawCircles2D(GL11 gl, List<Circle> circles, float lineW, float scala) {
        if (circles == null || circles.isEmpty()) return;
        if (!ensureReady()) return;

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        double[] bucket = DataSaved.glL_AnchorView;
        float width = Math.max(1f, lineW);

        for (Circle circle : circles) {
            if (circle.getLayer() == null || !isLayerEnabled(circle.getLayer().getLayerName()))
                continue;

            float cx = (float) ((circle.getCenter().getX() - bucket[0]) * scala);
            float cy = (float) ((circle.getCenter().getY() - bucket[1]) * scala);
            float r = (float) (circle.getRadius() * scala);
            int segments = computeCircleSegments(r);

            float[] coords = new float[(segments + 1) * 3];
            for (int i = 0; i <= segments; i++) {
                double angle = 2d * Math.PI * i / segments;
                coords[i * 3] = (float) (cx + r * Math.cos(angle));
                coords[i * 3 + 1] = (float) (cy + r * Math.sin(angle));
                coords[i * 3 + 2] = 0f;
            }

            float[] rgb = GL_Methods.parseColorToGL(GL_Methods.myParseColor(circle.getColor()));
            drawThickSegments2D(coords, width, new float[]{rgb[0], rgb[1], rgb[2], 1f});
        }
    }

    public static void drawArcs2D(GL11 gl, List<Arc> arcs, float lineW, float scala) {
        if (arcs == null || arcs.isEmpty()) return;
        if (!ensureReady()) return;

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        double[] bucket = DataSaved.glL_AnchorView;
        float width = Math.max(1f, lineW);

        for (Arc arc : arcs) {
            if (arc.getLayer() == null || !isLayerEnabled(arc.getLayer().getLayerName())) continue;

            float cx = (float) ((arc.getCenter().getX() - bucket[0]) * scala);
            float cy = (float) ((arc.getCenter().getY() - bucket[1]) * scala);
            float r = (float) (arc.getRadius() * scala);

            float startAngle = (float) arc.getStartAngle();
            float endAngle = (float) arc.getEndAngle();

            float sweep = endAngle - startAngle;
            if (sweep < 0) sweep += 360f;
            int segments = computeArcSegments(r, sweep);

            float[] coords = new float[(segments + 1) * 3];
            for (int i = 0; i <= segments; i++) {
                double angleDeg = startAngle + sweep * i / segments;
                double angleRad = Math.toRadians(angleDeg);
                coords[i * 3] = (float) (cx + r * Math.cos(angleRad));
                coords[i * 3 + 1] = (float) (cy + r * Math.sin(angleRad));
                coords[i * 3 + 2] = 0f;
            }

            float[] rgb = GL_Methods.parseColorToGL(GL_Methods.myParseColor(arc.getColor()));
            drawThickSegments2D(coords, width, new float[]{rgb[0], rgb[1], rgb[2], 1f});
        }
    }

    public static void drawPolylines2D(GL11 gl, List<Polyline_2D> polylines, float lineW, float scala) {
        if (polylines == null || polylines.isEmpty()) return;
        if (!ensureReady()) return;

        double[] bucket = DataSaved.glL_AnchorView;
        float width = Math.max(1f, lineW);

        for (Polyline_2D polyline : polylines) {
            if (polyline.getLayer() == null || !isLayerEnabled(polyline.getLayer().getLayerName()))
                continue;

            List<Point3D> vertices = polyline.getVertices();
            if (vertices == null || vertices.size() < 2) continue;

            int color = GL_Methods.myParseColor(polyline.getLineColor());
            float[] rgb = GL_Methods.parseColorToGL(color);

            // Costruiamo SEMPRE punti già in GL-space
            List<Point3D> glPoints = new ArrayList<>();

            for (int i = 0; i < vertices.size() - 1; i++) {
                Point3D p1 = vertices.get(i);
                Point3D p2 = vertices.get(i + 1);
                double bulge = p1.getBulge();

                if (bulge == 0) {
                    if (glPoints.isEmpty()) {
                        glPoints.add(new Point3D(
                                (p1.getX() - bucket[0]) * scala,
                                (p1.getY() - bucket[1]) * scala,
                                0
                        ));
                    }
                    glPoints.add(new Point3D(
                            (p2.getX() - bucket[0]) * scala,
                            (p2.getY() - bucket[1]) * scala,
                            0
                    ));
                } else {
                    float[][] arcPoints = Math.abs(bulge) > 1
                            ? computeArcPointsM(p1, p2, bulge, scala, bucket)
                            : computeArcPoints(p1, p2, bulge, scala, bucket);

                    for (int j = 0; j < arcPoints.length; j++) {
                        float[] ap = arcPoints[j];

                        // evita duplicati consecutivi
                        if (!glPoints.isEmpty()) {
                            Point3D last = glPoints.get(glPoints.size() - 1);
                            if (Math.abs(last.getX() - ap[0]) < 1e-6 &&
                                    Math.abs(last.getY() - ap[1]) < 1e-6) {
                                continue;
                            }
                        }

                        glPoints.add(new Point3D(ap[0], ap[1], 0));
                    }
                }
            }

            if (glPoints.size() < 2) continue;

            drawThickLineStrip2D_GLSpace(
                    glPoints,
                    width,
                    new float[]{rgb[0], rgb[1], rgb[2], 1f}
            );
        }
    }

    private static void drawThickLineStrip2D_GLSpace(List<Point3D> points, float widthPx, float[] color) {
        if (points == null || points.size() < 2) return;
        if (!ensureReady()) return;

        float[] triVerts = buildPolylineTrianglesGL(points, widthPx, false);
        if (triVerts == null || triVerts.length == 0) return;

        FloatBuffer buffer = createFloatBuffer(triVerts);
        drawColoredVertices(
                buffer,
                triVerts.length / 3,
                GLES20.GL_TRIANGLES,
                identity(),
                color[0],
                color[1],
                color[2],
                color.length > 3 ? color[3] : 1f
        );
    }

    private static float[][] computeArcPointsM(Point3D p1, Point3D p2, double bulge, float scala, double[] bucket) {

        float x1 = (float) ((p1.getX() - bucket[0]) * scala);
        float y1 = (float) ((p1.getY() - bucket[1]) * scala);
        float z1 = (float) ((p1.getZ() - bucket[2]) * scala);

        float x2 = (float) ((p2.getX() - bucket[0]) * scala);
        float y2 = (float) ((p2.getY() - bucket[1]) * scala);

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

        int numSegments = computeBulgeArcSegments(radius, sweepAngle);
        float[][] arcPoints = new float[numSegments + 1][3];
        for (int i = 0; i <= numSegments; i++) {
            float angle = startAngle + (sweepAngle * i / numSegments);
            arcPoints[i][0] = (float) (centerX + radius * Math.cos(angle));
            arcPoints[i][1] = (float) (centerY + radius * Math.sin(angle));
            arcPoints[i][2] = z1;
        }
        return arcPoints;
    }

    private static float[][] computeArcPoints(Point3D p1, Point3D p2, double bulge, float scala, double[] bucket) {

        float x1 = (float) ((p1.getX() - bucket[0]) * scala);
        float y1 = (float) ((p1.getY() - bucket[1]) * scala);
        float z1 = (float) ((p1.getZ() - bucket[2]) * scala);

        float x2 = (float) ((p2.getX() - bucket[0]) * scala);
        float y2 = (float) ((p2.getY() - bucket[1]) * scala);

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

        int numSegments = computeBulgeArcSegments(radius, sweepAngle);
        float[][] arcPoints = new float[numSegments + 1][3];
        for (int i = 0; i <= numSegments; i++) {
            float angle = startAngle + (sweepAngle * i / numSegments);
            arcPoints[i][0] = (float) (centerX + radius * Math.cos(angle));
            arcPoints[i][1] = (float) (centerY + radius * Math.sin(angle));
            arcPoints[i][2] = z1;
        }

        return arcPoints;
    }

    private static float[] flatten(float[][] array) {
        float[] flat = new float[array.length * 3];
        for (int i = 0; i < array.length; i++) {
            flat[i * 3] = array[i][0];
            flat[i * 3 + 1] = array[i][1];
            flat[i * 3 + 2] = array[i][2];
        }
        return flat;
    }

    public static void drawCircle(GL11 gl, float cx, float cy, float cz, float radius, int segments, float[] color) {
        if (!ensureReady()) return;

        float[] coords = new float[(segments + 2) * 3];
        coords[0] = cx;
        coords[1] = cy;
        coords[2] = cz;

        for (int i = 0; i <= segments; i++) {
            double angle = 2d * Math.PI * i / segments;
            coords[(i + 1) * 3] = cx + (float) (radius * Math.cos(angle));
            coords[(i + 1) * 3 + 1] = cy + (float) (radius * Math.sin(angle));
            coords[(i + 1) * 3 + 2] = cz;
        }

        FloatBuffer buffer = createFloatBuffer(coords);
        drawColoredVertices(
                buffer,
                segments + 2,
                GLES20.GL_TRIANGLE_FAN,
                identity(),
                color[0],
                color[1],
                color[2],
                color.length > 3 ? color[3] : 1f
        );
    }

    public static void drawCircle(GL11 gl, float cx, float cy, float cz, float radius, int segments) {
        if (!ensureReady()) return;

        float[] coords = new float[(segments + 2) * 3];
        coords[0] = cx;
        coords[1] = cy;
        coords[2] = cz;

        for (int i = 0; i <= segments; i++) {
            double angle = 2d * Math.PI * i / segments;
            coords[(i + 1) * 3] = cx + (float) (radius * Math.cos(angle));
            coords[(i + 1) * 3 + 1] = cy + (float) (radius * Math.sin(angle));
            coords[(i + 1) * 3 + 2] = cz;
        }

        FloatBuffer buffer = createFloatBuffer(coords);
        drawColoredVertices(buffer, segments + 2, GLES20.GL_TRIANGLE_FAN, identity(), 1f, 1f, 1f, 1f);
    }

    public static void drawRawLines3D(float[] coords, int vertexCount, float[] color, float lineWidth) {
        if (coords == null || coords.length < vertexCount * 3) return;
        if (!ensureReady()) return;

        GLES20.glLineWidth(Math.max(1f, lineWidth));
        FloatBuffer buffer = createFloatBuffer(coords);
        drawColoredVertices(
                buffer,
                vertexCount,
                GLES20.GL_LINES,
                identity(),
                color[0],
                color[1],
                color[2],
                color.length > 3 ? color[3] : 1f
        );
    }

    public static void clearTextTextureCache() {
        try {
            if (!textTextureCache.isEmpty()) {
                int[] ids = new int[textTextureCache.size()];
                int i = 0;
                for (int textureId : textTextureCache.values()) {
                    ids[i++] = textureId;
                }
                GLES20.glDeleteTextures(ids.length, ids, 0);
            }
            textTextureCache.clear();
            textSizeCache.clear();
        } catch (Exception ignored) {
        }
    }

    // =========================
    // LOW LEVEL DRAW
    // =========================

    private static void drawColoredVertices(FloatBuffer vertexBuffer, int vertexCount, int mode,
                                            float[] modelMatrix, float r, float g, float b, float a) {
        if (vertexBuffer == null || vertexCount <= 0) return;

        colorProgram.use();
        buildMvp(modelMatrix);

        GLES20.glUniformMatrix4fv(colorProgram.uMvpMatrix, 1, false, tempMvp, 0);
        GLES20.glUniform4f(colorProgram.uColor, r, g, b, a);
        GLES20.glUniform1i(colorProgram.uUseVertexColor, 0);
        GLES20.glUniform1f(colorProgram.uPointSize, 8f);

        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(colorProgram.aPosition);
        GLES20.glVertexAttribPointer(colorProgram.aPosition, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glDisableVertexAttribArray(colorProgram.aColor);
        GLES20.glVertexAttrib4f(colorProgram.aColor, 1f, 1f, 1f, 1f);

        GLES20.glDrawArrays(mode, 0, vertexCount);
        GLES20.glDisableVertexAttribArray(colorProgram.aPosition);
    }

    private static void drawGradientVertices(FloatBuffer vertexBuffer, FloatBuffer colorBuffer,
                                             int vertexCount, int mode, float[] modelMatrix) {
        if (vertexBuffer == null || colorBuffer == null || vertexCount <= 0) return;

        colorProgram.use();
        buildMvp(modelMatrix);

        GLES20.glUniformMatrix4fv(colorProgram.uMvpMatrix, 1, false, tempMvp, 0);
        GLES20.glUniform4f(colorProgram.uColor, 1f, 1f, 1f, 1f);
        GLES20.glUniform1i(colorProgram.uUseVertexColor, 1);

        vertexBuffer.position(0);
        colorBuffer.position(0);

        GLES20.glEnableVertexAttribArray(colorProgram.aPosition);
        GLES20.glVertexAttribPointer(colorProgram.aPosition, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(colorProgram.aColor);
        GLES20.glVertexAttribPointer(colorProgram.aColor, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        GLES20.glDrawArrays(mode, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(colorProgram.aPosition);
        GLES20.glDisableVertexAttribArray(colorProgram.aColor);
    }

    private static void drawTexturedQuad(FloatBuffer vertexBuffer, FloatBuffer texBuffer,
                                         int textureId, int vertexCount, float[] modelMatrix) {
        if (vertexBuffer == null || texBuffer == null || vertexCount <= 0) return;

        textureProgram.use();
        buildMvp(modelMatrix);

        GLES20.glUniformMatrix4fv(textureProgram.uMvpMatrix, 1, false, tempMvp, 0);
        GLES20.glUniform4f(textureProgram.uColor, 1f, 1f, 1f, 1f);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(textureProgram.uTexture, 0);

        vertexBuffer.position(0);
        texBuffer.position(0);

        GLES20.glEnableVertexAttribArray(textureProgram.aPosition);
        GLES20.glVertexAttribPointer(textureProgram.aPosition, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(textureProgram.aTexCoord);
        GLES20.glVertexAttribPointer(textureProgram.aTexCoord, 2, GLES20.GL_FLOAT, false, 0, texBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(textureProgram.aPosition);
        GLES20.glDisableVertexAttribArray(textureProgram.aTexCoord);
    }

    private static float[] buildTranslationModel(float tx, float ty, float tz) {
        Matrix.setIdentityM(tempModel, 0);
        Matrix.translateM(tempModel, 0, tx, ty, tz);
        return tempModel;
    }

    private static float[] identity() {
        Matrix.setIdentityM(tempModel, 0);
        return tempModel;
    }

    private static void buildMvp(float[] modelMatrix) {
        Matrix.multiplyMM(tempMvp, 0, sVpMatrix, 0, modelMatrix, 0);
    }

    private static float[] getCameraRight() {
        float[] invView = new float[16];
        if (!Matrix.invertM(invView, 0, sViewMatrix, 0)) {
            return new float[]{1f, 0f, 0f};
        }
        return new float[]{invView[0], invView[1], invView[2]};
    }

    private static float[] getCameraUp() {
        float[] invView = new float[16];
        if (!Matrix.invertM(invView, 0, sViewMatrix, 0)) {
            return new float[]{0f, 1f, 0f};
        }
        return new float[]{invView[4], invView[5], invView[6]};
    }

    // =========================
    // SHADERS
    // =========================
    private static float[] buildFaceOutlineCoords2D(Face3D face, double[] bucketCenter, float scala) {
        if (face == null) return null;

        List<Point3D> pts = face.getVertices();
        if (pts == null || pts.size() < 3) return null;

        int count = pts.size();
        float[] coords = new float[count * 6]; // 2 punti per segmento, 3 float ciascuno
        int idx = 0;

        for (int i = 0; i < count; i++) {
            Point3D a = pts.get(i);
            Point3D b = pts.get((i + 1) % count);

            coords[idx++] = (float) ((a.getX() - bucketCenter[0]) * scala);
            coords[idx++] = (float) ((a.getY() - bucketCenter[1]) * scala);
            coords[idx++] = 0f;

            coords[idx++] = (float) ((b.getX() - bucketCenter[0]) * scala);
            coords[idx++] = (float) ((b.getY() - bucketCenter[1]) * scala);
            coords[idx++] = 0f;
        }

        return coords;
    }

    private static class ColorProgram {
        final int program;
        final int aPosition;
        final int aColor;
        final int uMvpMatrix;
        final int uColor;
        final int uUseVertexColor;
        final int uPointSize;

        ColorProgram() {
            String vertex =
                    "uniform mat4 uMVPMatrix;\n" +
                            "attribute vec4 aPosition;\n" +
                            "attribute vec4 aColor;\n" +
                            "uniform vec4 uColor;\n" +
                            "uniform int uUseVertexColor;\n" +
                            "uniform float uPointSize;\n" +
                            "varying vec4 vColor;\n" +
                            "void main() {\n" +
                            "  gl_Position = uMVPMatrix * aPosition;\n" +
                            "  gl_PointSize = uPointSize;\n" +
                            "  vColor = (uUseVertexColor == 1) ? aColor : uColor;\n" +
                            "}";

            String fragment =
                    "precision mediump float;\n" +
                            "varying vec4 vColor;\n" +
                            "void main() {\n" +
                            "  gl_FragColor = vColor;\n" +
                            "}";

            program = createProgram(vertex, fragment);
            aPosition = GLES20.glGetAttribLocation(program, "aPosition");
            aColor = GLES20.glGetAttribLocation(program, "aColor");
            uMvpMatrix = GLES20.glGetUniformLocation(program, "uMVPMatrix");
            uColor = GLES20.glGetUniformLocation(program, "uColor");
            uUseVertexColor = GLES20.glGetUniformLocation(program, "uUseVertexColor");
            uPointSize = GLES20.glGetUniformLocation(program, "uPointSize");
        }

        void use() {
            GLES20.glUseProgram(program);
        }
    }

    private static class TextureProgram {
        final int program;
        final int aPosition;
        final int aTexCoord;
        final int uMvpMatrix;
        final int uTexture;
        final int uColor;

        TextureProgram() {
            String vertex =
                    "uniform mat4 uMVPMatrix;\n" +
                            "attribute vec4 aPosition;\n" +
                            "attribute vec2 aTexCoord;\n" +
                            "varying vec2 vTexCoord;\n" +
                            "void main() {\n" +
                            "  gl_Position = uMVPMatrix * aPosition;\n" +
                            "  vTexCoord = aTexCoord;\n" +
                            "}";

            String fragment =
                    "precision mediump float;\n" +
                            "varying vec2 vTexCoord;\n" +
                            "uniform sampler2D uTexture;\n" +
                            "uniform vec4 uColor;\n" +
                            "void main() {\n" +
                            "  vec4 tex = texture2D(uTexture, vTexCoord);\n" +
                            "  gl_FragColor = tex * uColor;\n" +
                            "}";

            program = createProgram(vertex, fragment);
            aPosition = GLES20.glGetAttribLocation(program, "aPosition");
            aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord");
            uMvpMatrix = GLES20.glGetUniformLocation(program, "uMVPMatrix");
            uTexture = GLES20.glGetUniformLocation(program, "uTexture");
            uColor = GLES20.glGetUniformLocation(program, "uColor");
        }

        void use() {
            GLES20.glUseProgram(program);
        }
    }

    private static int createProgram(String vertexSource, String fragmentSource) {
        int vs = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);

        int[] link = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] == 0) {
            String msg = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Program link error: " + msg);
        }

        GLES20.glDeleteShader(vs);
        GLES20.glDeleteShader(fs);
        return program;
    }

    private static int compileShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String msg = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Shader compile error: " + msg);
        }
        return shader;
    }

    public static void drawRawLineStrip3D(float[] coords, int vertexCount, float[] color, float lineWidth) {
        if (coords == null || coords.length < vertexCount * 3) return;
        if (!ensureReady()) return;

        GLES20.glLineWidth(Math.max(1f, lineWidth));
        FloatBuffer buffer = createFloatBuffer(coords);
        drawColoredVertices(
                buffer,
                vertexCount,
                GLES20.GL_LINE_STRIP,
                identity(),
                color[0],
                color[1],
                color[2],
                color.length > 3 ? color[3] : 1f
        );
    }

    public static void drawSnapHelpers(GL11 gl11) {
        float s = currentScale();
        switch (DataSaved.isAutoSnap) {
            case 1:
            case 3:
                if (DataSaved.nearestPoint != null) {
                    switch (DataSaved.isWL) {
                        case EXCAVATOR:
                            drawPointDist_EXCA(gl11, 5f, Color.GREEN, s);
                            break;

                        case WHEELLOADER:
                            drawPointDist_WHEEL(gl11, 5f, Color.GREEN, s);
                            break;

                        case DOZER:
                        case DOZER_SIX:
                        case GRADER:
                            drawPointDist_DOZER(gl11, 5f, Color.GREEN, s);
                            break;
                    }

                }
                break;

            case 2:
            case 4:
                Polyline polyToDraw = null;

                if (DataSaved.selectedPoly_OFFSET != null && DataSaved.line_Offset != 0) {
                    polyToDraw = DataSaved.selectedPoly_OFFSET;
                } else if (DataSaved.selectedPoly != null) {
                    polyToDraw = DataSaved.selectedPoly;
                }

                if (polyToDraw != null) {
                    // versione piena
                    GLDrawer.drawSelectedPoly(gl11, polyToDraw, 5f, Color.GREEN, s);


                }
                switch (DataSaved.isWL) {
                    case EXCAVATOR:
                        drawLineDist_EXCA(gl11, 5f, Color.GREEN, s);
                        break;

                    case WHEELLOADER:
                        drawLineDist_WHEEL(gl11, 5f, Color.GREEN, s);
                        break;

                    case DOZER:
                    case DOZER_SIX:
                    case GRADER:
                        drawLineDist_DOZER(gl11, 5f, Color.GREEN, s);
                        break;
                }
                break;
        }
    }

    private static void drawLineDist_DOZER(GL11 gl, float lineW, int color, float scala) {
        try {
            float[] coords = new float[]{
                    GL_LAMA[35].getX(), GL_LAMA[35].getY(), GL_LAMA[35].getZ(),
                    GL_LAMA[36].getX(), GL_LAMA[36].getY(), GL_LAMA[36].getZ(),
                    GL_LAMA[37].getX(), GL_LAMA[37].getY(), GL_LAMA[37].getZ()
            };

            GLDrawer.drawRawLineStrip3D(
                    coords,
                    3,
                    GL_Methods.parseColorToGL(color),
                    Math.max(1f, lineW * scala)
            );
        } catch (Exception ignored) {
        }
    }

    private static void drawPointDist_DOZER(GL11 gl, float lineW, int color, float scala) {
        try {
            float[] coords = new float[]{
                    GL_LAMA[38].getX(), GL_LAMA[38].getY(), GL_LAMA[38].getZ(),
                    GL_LAMA[39].getX(), GL_LAMA[39].getY(), GL_LAMA[39].getZ(),
                    GL_LAMA[40].getX(), GL_LAMA[40].getY(), GL_LAMA[40].getZ()
            };

            GLDrawer.drawRawLineStrip3D(
                    coords,
                    3,
                    GL_Methods.parseColorToGL(color),
                    Math.max(1f, lineW * scala)
            );

            drawBillboardCircle(gl, DataSaved.nearestPoint, 0.08f, scala);
        } catch (Exception ignored) {
        }
    }

    private static void drawLineDist_WHEEL(GL11 gl, float lineW, int color, float scala) {
        try {
            float[] coords = new float[]{
                    GL_WHEEL[24].getX(), GL_WHEEL[24].getY(), GL_WHEEL[24].getZ(),
                    GL_WHEEL[25].getX(), GL_WHEEL[25].getY(), GL_WHEEL[25].getZ(),
                    GL_WHEEL[26].getX(), GL_WHEEL[26].getY(), GL_WHEEL[26].getZ()
            };

            GLDrawer.drawRawLineStrip3D(
                    coords,
                    3,
                    GL_Methods.parseColorToGL(color),
                    Math.max(1f, lineW * scala)
            );
        } catch (Exception ignored) {
        }
    }

    private static void drawPointDist_WHEEL(GL11 gl, float lineW, int color, float scala) {
        try {
            float[] coords = new float[]{
                    GL_WHEEL[27].getX(), GL_WHEEL[27].getY(), GL_WHEEL[27].getZ(),
                    GL_WHEEL[28].getX(), GL_WHEEL[28].getY(), GL_WHEEL[28].getZ(),
                    GL_WHEEL[29].getX(), GL_WHEEL[29].getY(), GL_WHEEL[29].getZ()
            };

            GLDrawer.drawRawLineStrip3D(
                    coords,
                    3,
                    GL_Methods.parseColorToGL(color),
                    Math.max(1f, lineW * scala)
            );

            drawBillboardCircle(gl, DataSaved.nearestPoint, 0.08f, scala);
        } catch (Exception ignored) {
        }
    }

    private static void drawLineDist_EXCA(GL11 gl, float lineW, int color, float scala) {
        try {
            float[] coords = new float[]{
                    GL_BENNA[28].getX(), GL_BENNA[28].getY(), GL_BENNA[28].getZ(),
                    GL_BENNA[29].getX(), GL_BENNA[29].getY(), GL_BENNA[29].getZ(),
                    GL_BENNA[30].getX(), GL_BENNA[30].getY(), GL_BENNA[30].getZ()
            };

            GLDrawer.drawRawLineStrip3D(
                    coords,
                    3,
                    GL_Methods.parseColorToGL(color),
                    Math.max(1f, lineW * scala)
            );
        } catch (Exception ignored) {
        }
    }

    private static void drawPointDist_EXCA(GL11 gl, float lineW, int color, float scala) {
        try {
            float[] coords = new float[]{
                    GL_BENNA[31].getX(), GL_BENNA[31].getY(), GL_BENNA[31].getZ(),
                    GL_BENNA[32].getX(), GL_BENNA[32].getY(), GL_BENNA[32].getZ(),
                    GL_BENNA[33].getX(), GL_BENNA[33].getY(), GL_BENNA[33].getZ()
            };

            GLDrawer.drawRawLineStrip3D(
                    coords,
                    3,
                    GL_Methods.parseColorToGL(color),
                    Math.max(1f, lineW * scala)
            );

            drawBillboardCircle(gl, DataSaved.nearestPoint, 0.08f, scala);
        } catch (Exception ignored) {
        }
    }

    private static void drawBillboardCircle(GL11 gl, Point3D center, float radius, float scala) {
        if (center == null) return;

        final int segments = 32;

        float cx = (float) ((center.getX() - DataSaved.glL_AnchorView[0]) * scala);
        float cy = (float) ((center.getY() - DataSaved.glL_AnchorView[1]) * scala);
        float cz = (float) ((center.getZ() - DataSaved.glL_AnchorView[2]) * scala);

        float rOuter = radius * 1.1f;
        float rInner = radius;

        float[] green = GL_Methods.parseColorToGL(Color.GREEN);
        GLDrawer.drawCircle(gl, cx, cy, cz, rOuter, segments, green);
        GLDrawer.drawCircle(gl, cx, cy, cz, rInner, segments, green);
    }

    public static void drawThickLineStrip2D(List<Point3D> points, double[] anchor, float scala, float widthPx, float[] color) {
        if (points == null || points.size() < 2) return;
        if (!ensureReady()) return;

        float[] triVerts = buildThickLineStrip2D(points, anchor, scala, widthPx);
        if (triVerts == null || triVerts.length == 0) return;

        FloatBuffer buffer = createFloatBuffer(triVerts);
        drawColoredVertices(
                buffer,
                triVerts.length / 3,
                GLES20.GL_TRIANGLES,
                identity(),
                color[0],
                color[1],
                color[2],
                color.length > 3 ? color[3] : 1f
        );
    }

    public static void drawThickSegments2D(float[] coords, float widthPx, float[] color) {
        if (coords == null || coords.length < 6) return;
        if (!ensureReady()) return;

        float[] triVerts = buildThickSegments2D(coords, widthPx);
        if (triVerts == null || triVerts.length == 0) return;

        FloatBuffer buffer = createFloatBuffer(triVerts);
        drawColoredVertices(
                buffer,
                triVerts.length / 3,
                GLES20.GL_TRIANGLES,
                identity(),
                color[0],
                color[1],
                color[2],
                color.length > 3 ? color[3] : 1f
        );
    }

    private static float[] buildThickLineStrip2D(List<Point3D> points, double[] anchor, float scala, float widthPx) {
        if (points.size() < 2) return null;

        List<Point3D> glPoints = new ArrayList<>(points.size());
        for (Point3D p : points) {
            glPoints.add(new Point3D(
                    (p.getX() - anchor[0]) * scala,
                    (p.getY() - anchor[1]) * scala,
                    0
            ));
        }
        return buildPolylineTrianglesGL(glPoints, widthPx, false);
    }

    private static int computeCircleSegments(float radiusWorld) {
        float radiusPx = worldToPixels((float) Math.abs(radiusWorld));
        float circumferencePx = (float) (2d * Math.PI * radiusPx);
        int computed = Math.max(CIRCLE_SEGMENTS_2D, Math.round(circumferencePx / CIRCLE_SEGMENT_PIXELS));
        return clamp(computed, MIN_CIRCLE_SEGMENTS_2D, MAX_CIRCLE_SEGMENTS_2D);
    }

    private static int computeArcSegments(float radiusWorld, float sweepDeg) {
        float radiusPx = worldToPixels((float) Math.abs(radiusWorld));
        float arcLengthPx = (float) ((2d * Math.PI * radiusPx) * (Math.abs(sweepDeg) / 360d));
        int computed = Math.max(ARC_SEGMENTS_2D, Math.round(arcLengthPx / ARC_SEGMENT_PIXELS));
        return clamp(computed, MIN_ARC_SEGMENTS_2D, MAX_ARC_SEGMENTS_2D);
    }

    private static int computeBulgeArcSegments(double radiusWorld, float sweepRad) {
        float sweepDeg = (float) Math.toDegrees(Math.abs(sweepRad));
        return computeArcSegments((float) radiusWorld, sweepDeg);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float worldToPixels(float world) {
        if (!isOrtho2D) return world;
        float pxX = world * viewportWidth / (2f * orthoWorldHalfWidth);
        float pxY = world * viewportHeight / (2f * orthoWorldHalfHeight);
        return Math.max(pxX, pxY);
    }

    private static float[] buildPolylineTrianglesGL(List<Point3D> points, float widthPx, boolean closed) {
        if (points == null || points.size() < 2) return null;

        List<Float> out = new ArrayList<>();
        float half = Math.max(0.5f, widthPx * 0.5f);

        for (int i = 0; i < points.size() - 1; i++) {
            Point3D p1 = points.get(i);
            Point3D p2 = points.get(i + 1);
            appendThickSegment(out,
                    (float) p1.getX(), (float) p1.getY(), 0f,
                    (float) p2.getX(), (float) p2.getY(), 0f,
                    half);
        }

        appendRoundCap(out, points.get(0), points.get(1), half, true);
        appendRoundCap(out, points.get(points.size() - 1), points.get(points.size() - 2), half, false);

        for (int i = 1; i < points.size() - 1; i++) {
            appendRoundJoin(out, points.get(i - 1), points.get(i), points.get(i + 1), half);
        }

        if (closed && points.size() > 2) {
            appendRoundJoin(out, points.get(points.size() - 2), points.get(points.size() - 1), points.get(0), half);
            appendRoundJoin(out, points.get(points.size() - 1), points.get(0), points.get(1), half);
        }

        float[] arr = new float[out.size()];
        for (int i = 0; i < out.size(); i++) arr[i] = out.get(i);
        return arr;
    }

    private static void appendRoundCap(List<Float> out, Point3D endpoint, Point3D neighbor, float halfWidthPx, boolean atStart) {
        float ex = (float) endpoint.getX();
        float ey = (float) endpoint.getY();
        float nx = (float) (neighbor.getX() - endpoint.getX());
        float ny = (float) (neighbor.getY() - endpoint.getY());
        float len = (float) Math.hypot(nx, ny);
        if (len < 1e-6f) return;

        nx /= len;
        ny /= len;
        if (!atStart) {
            nx = -nx;
            ny = -ny;
        }

        float startAngle = (float) Math.atan2(ny, nx) - (float) Math.PI * 0.5f;
        float endAngle = startAngle + (float) Math.PI;
        appendArcFan(out, ex, ey, 0f, halfWidthPx, startAngle, endAngle);
    }

    private static void appendRoundJoin(List<Float> out, Point3D prev, Point3D pivot, Point3D next, float halfWidthPx) {
        float px = (float) pivot.getX();
        float py = (float) pivot.getY();
        float dx1 = px - (float) prev.getX();
        float dy1 = py - (float) prev.getY();
        float dx2 = (float) next.getX() - px;
        float dy2 = (float) next.getY() - py;

        float len1 = (float) Math.hypot(dx1, dy1);
        float len2 = (float) Math.hypot(dx2, dy2);
        if (len1 < 1e-6f || len2 < 1e-6f) return;

        dx1 /= len1;
        dy1 /= len1;
        dx2 /= len2;
        dy2 /= len2;

        float cross = dx1 * dy2 - dy1 * dx2;
        if (Math.abs(cross) < 1e-5f) return;

        float nx1 = (cross > 0f) ? -dy1 : dy1;
        float ny1 = (cross > 0f) ? dx1 : -dx1;
        float nx2 = (cross > 0f) ? -dy2 : dy2;
        float ny2 = (cross > 0f) ? dx2 : -dx2;

        float a0 = (float) Math.atan2(ny1, nx1);
        float a1 = (float) Math.atan2(ny2, nx2);
        if (cross > 0f) {
            while (a1 < a0) a1 += (float) (Math.PI * 2d);
        } else {
            while (a1 > a0) a1 -= (float) (Math.PI * 2d);
        }

        appendArcFan(out, px, py, 0f, halfWidthPx, a0, a1);
    }

    private static void appendArcFan(List<Float> out, float cx, float cy, float cz, float radiusPx,
                                     float startAngle, float endAngle) {
        float radiusX = pixelsToWorldX(radiusPx);
        float radiusY = pixelsToWorldY(radiusPx);
        float delta = endAngle - startAngle;
        int steps = Math.max(4, (int) Math.ceil(Math.abs(Math.toDegrees(delta)) / ROUND_JOIN_STEP_DEG));
        float prevX = cx + (float) Math.cos(startAngle) * radiusX;
        float prevY = cy + (float) Math.sin(startAngle) * radiusY;

        for (int i = 1; i <= steps; i++) {
            float t = (float) i / (float) steps;
            float a = startAngle + delta * t;
            float x = cx + (float) Math.cos(a) * radiusX;
            float y = cy + (float) Math.sin(a) * radiusY;

            out.add(cx);
            out.add(cy);
            out.add(cz);
            out.add(prevX);
            out.add(prevY);
            out.add(cz);
            out.add(x);
            out.add(y);
            out.add(cz);

            prevX = x;
            prevY = y;
        }
    }

    private static float[] buildThickSegments2D(float[] coords, float widthPx) {
        if (coords.length < 6) return null;

        List<Float> out = new ArrayList<>();
        float half = Math.max(0.5f, widthPx * 0.5f);

        for (int i = 0; i <= coords.length - 6; i += 6) {
            float x1 = coords[i];
            float y1 = coords[i + 1];
            float z1 = coords[i + 2];

            float x2 = coords[i + 3];
            float y2 = coords[i + 4];
            float z2 = coords[i + 5];

            appendThickSegment(out, x1, y1, z1, x2, y2, z2, half);
        }

        float[] arr = new float[out.size()];
        for (int i = 0; i < out.size(); i++) arr[i] = out.get(i);
        return arr;
    }

    private static void appendThickSegment(List<Float> out,
                                           float x1, float y1, float z1,
                                           float x2, float y2, float z2,
                                           float halfWidthPx) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-6f) return;

        float nx = -dy / len;
        float ny = dx / len;

        float halfWorldX = pixelsToWorldX(halfWidthPx);
        float halfWorldY = pixelsToWorldY(halfWidthPx);

        float ox = nx * halfWorldX;
        float oy = ny * halfWorldY;

        float ax = x1 + ox, ay = y1 + oy;
        float bx = x1 - ox, by = y1 - oy;
        float cx = x2 + ox, cy = y2 + oy;
        float dx2 = x2 - ox, dy2 = y2 - oy;

        out.add(ax);
        out.add(ay);
        out.add(z1);
        out.add(bx);
        out.add(by);
        out.add(z1);
        out.add(cx);
        out.add(cy);
        out.add(z2);

        out.add(cx);
        out.add(cy);
        out.add(z2);
        out.add(bx);
        out.add(by);
        out.add(z1);
        out.add(dx2);
        out.add(dy2);
        out.add(z2);
    }

    public static void resetGlState() {
        colorProgram = null;
        textureProgram = null;
        hasVpMatrix = false;
        hasViewMatrix = false;
    }

    private static float pixelsToWorldX(float px) {
        if (!isOrtho2D) return px;
        return (2f * orthoWorldHalfWidth / (float) viewportWidth) * px;
    }

    private static float pixelsToWorldY(float px) {
        if (!isOrtho2D) return px;
        return (2f * orthoWorldHalfHeight / (float) viewportHeight) * px;
    }

    public static void setViewportSize(int width, int height) {
        viewportWidth = Math.max(1, width);
        viewportHeight = Math.max(1, height);
    }

    public static void setOrthoMetrics(boolean ortho2D, float halfWidth, float halfHeight) {
        isOrtho2D = ortho2D;
        orthoWorldHalfWidth = Math.max(1e-6f, halfWidth);
        orthoWorldHalfHeight = Math.max(1e-6f, halfHeight);
    }

    public static void drawFaces2D(GL11 gl, List<Face3D> faces, float lineW, float scala, boolean isXML) {
        if (faces == null || faces.isEmpty()) return;
        if (!ensureReady()) return;
        if (My3DActivity.glGradient) return;

        try {
            double[] bucketCenter = DataSaved.glL_AnchorView;

            List<Face3D> visibleFaces = new ArrayList<>();

            for (Face3D face : faces) {
                if (face == null) continue;
                if (face.getLayer() == null || !isLayerEnabled(face.getLayer().getLayerName()))
                    continue;
                if (!isFace3DVisible2D(face, bucketCenter, scala)) continue;

                visibleFaces.add(face);
            }

            visibleFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            float[] identity = identity();

            for (Face3D face : visibleFaces) {
                face.prepareVertexBuffer2DForGradient(bucketCenter, scala);
                FloatBuffer buffer = face.getVertexBuffer2D();
                if (buffer == null) continue;

                int color = isXML
                        ? GL_Methods.myParseColor(
                        AutoCADColor.getColor(String.valueOf(face.getLayer().getColorState()))
                )
                        : GL_Methods.myParseColor(face.getLayer().getColorState());

                float[] rgb = GL_Methods.parseColorToGL(color);

                boolean isTriangle = face.getP4().equals(face.getP3());
                int vertexCount = isTriangle ? 3 : 4;
                int drawMode = isTriangle ? GLES20.GL_TRIANGLES : GLES20.GL_TRIANGLE_FAN;

                if (My3DActivity.glFill) {
                    buffer.position(0);
                    drawColoredVertices(
                            buffer,
                            vertexCount,
                            drawMode,
                            identity,
                            rgb[0], rgb[1], rgb[2], 0.30f
                    );
                }

                if (My3DActivity.glFace) {
                    float[] outlineCoords = buildFaceOutlineCoords2D(face, bucketCenter, scala);
                    if (outlineCoords != null) {
                        float outlineWidth = Math.max(1.0f, lineW * scala);
                        drawThickSegments2D(outlineCoords, outlineWidth, new float[]{
                                rgb[0], rgb[1], rgb[2], 1f
                        });
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "drawFaces2D", e);
        }
    }
}


