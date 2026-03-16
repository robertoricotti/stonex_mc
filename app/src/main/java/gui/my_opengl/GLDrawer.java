package gui.my_opengl;

import static gui.my_opengl.GL_Methods.createFloatBuffer;
import static gui.my_opengl.GL_Methods.getJetColor;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;

import android.graphics.Color;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gui.my_opengl.compat.GL11;

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
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import services.TriangleService;

public class GLDrawer {
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

    private GLDrawer() {}

    // =========================
    // PUBLIC SETUP
    // =========================

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

    // =========================
    // FACES
    // =========================

    public static void drawFaces(GL11 gl, List<Face3D> faces, float lineW, float scale, boolean isXML) {
        if (faces == null || faces.isEmpty()) return;
        if (!ensureReady()) return;
        if (My3DActivity.glGradient) return;

        try {
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glLineWidth(Math.max(1f, lineW * scale));

            double[] bucketCenter = DataSaved.glL_AnchorView;
            List<Face3D> sortedFaces = new ArrayList<>(faces);
            sortedFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            for (Face3D face : sortedFaces) {
                if (face.getLayer() == null || !isLayerEnabled(face.getLayer().getLayerName())) continue;

                face.prepareVertexBuffer(bucketCenter, scale);
                FloatBuffer buffer = face.getVertexBuffer();
                if (buffer == null) continue;

                int color = isXML
                        ? GL_Methods.myParseColor(AutoCADColor.getColor(String.valueOf(face.getLayer().getColorState())))
                        : GL_Methods.myParseColor(face.getLayer().getColorState());
                float[] rgb = GL_Methods.parseColorToGL(color);

                boolean isTriangle = face.getP4().equals(face.getP3());
                int vertexCount = isTriangle ? 3 : 4;
                int drawMode = isTriangle ? GLES20.GL_TRIANGLES : GLES20.GL_TRIANGLE_FAN;

                float[] modelMatrix = buildTranslationModel(0f, 0f, (float) (-DataSaved.offsetH * scale));

                if (My3DActivity.glFill) {
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

    public static void drawFacesGradientPRO(GL11 gl, List<Face3D> faces, float scale, double zMax, double zMin) {
        if (faces == null || faces.isEmpty()) return;
        if (!ensureReady()) return;

        try {
            double[] bucketCenter = DataSaved.glL_AnchorView;
            List<Face3D> sortedFaces = new ArrayList<>(faces);

            sortedFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            for (Face3D face : sortedFaces) {
                if (face.getLayer() == null || !isLayerEnabled(face.getLayer().getLayerName())) continue;

                face.prepareVertexBuffer(bucketCenter, scale);
                FloatBuffer vertexBuffer = face.getVertexBuffer();
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
                float[] modelMatrix = buildTranslationModel(0f, 0f, (float) (-DataSaved.offsetH * scale));

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

    public static void drawFacesGradient(GL11 gl, List<Face3D> faces, float scale, double zMax, double zMin) {
        if (faces == null || faces.isEmpty()) return;
        if (!ensureReady()) return;

        try {
            double[] bucketCenter = DataSaved.glL_AnchorView;
            List<Face3D> sortedFaces = new ArrayList<>(faces);

            sortedFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            for (Face3D face : sortedFaces) {
                if (face.getLayer() == null || !isLayerEnabled(face.getLayer().getLayerName())) continue;

                face.prepareVertexBuffer(bucketCenter, scale);
                FloatBuffer vertexBuffer = face.getVertexBuffer();
                if (vertexBuffer == null) continue;

                List<Point3D> points = face.getVertices();
                int vertexCount = points.size();

                float[] colorArray = new float[vertexCount * 4];
                for (int i = 0; i < vertexCount; i++) {
                    float[] color = getJetColor(points.get(i).getZ(), zMin, zMax);
                    colorArray[i * 4] = color[0];
                    colorArray[i * 4 + 1] = color[1];
                    colorArray[i * 4 + 2] = color[2];
                    colorArray[i * 4 + 3] = 0.65f;
                }
                FloatBuffer colorBuffer = createFloatBuffer(colorArray);

                int drawMode = face.getP4().equals(face.getP3()) ? GLES20.GL_TRIANGLES : GLES20.GL_TRIANGLE_FAN;
                float[] modelMatrix = buildTranslationModel(0f, 0f, (float) (-DataSaved.offsetH * scale));

                drawGradientVertices(vertexBuffer, colorBuffer, vertexCount, drawMode, modelMatrix);

                if (My3DActivity.glFace) {
                    float[] edgeColor = GL_Methods.parseColorToGL(MyColorClass.colorConstraint);
                    vertexBuffer.position(0);
                    drawColoredVertices(vertexBuffer, vertexCount, GLES20.GL_LINE_LOOP, modelMatrix,
                            edgeColor[0], edgeColor[1], edgeColor[2], 0.8f);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "drawFacesGradient", e);
        }
    }

    public static void drawFacesGradient2D(GL11 gl, List<Face3D> faces, float scale, double zMax, double zMin) {
        if (faces == null || faces.isEmpty()) return;
        if (!ensureReady()) return;

        try {
            double[] bucketCenter = DataSaved.glL_AnchorView;
            List<Face3D> sortedFaces = new ArrayList<>(faces);

            sortedFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            for (Face3D face : sortedFaces) {
                if (face.getLayer() == null || !isLayerEnabled(face.getLayer().getLayerName())) continue;

                face.prepareVertexBuffer2DForGradient(bucketCenter, scale);
                FloatBuffer vertexBuffer = face.getVertexBuffer();
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

                drawGradientVertices(vertexBuffer, colorBuffer, vertexCount, drawMode, identity);

                vertexBuffer.position(0);
                drawColoredVertices(vertexBuffer, vertexCount, GLES20.GL_LINE_LOOP, identity, 1f, 1f, 1f, 1f);
            }
        } catch (Exception e) {
            Log.e(TAG, "drawFacesGradient2D", e);
        }
    }

    // =========================
    // POLYLINES / LINES / ARCS / CIRCLES
    // =========================

    public static void drawPolylines(GL11 gl, List<Polyline> polylines, float lineW, float scale) {
        if (polylines == null || polylines.isEmpty()) return;
        if (!ensureReady()) return;

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glLineWidth(Math.max(1f, lineW * scale));

        double[] bucketCenter = DataSaved.glL_AnchorView;
        float[] identity = identity();

        for (Polyline polyline : polylines) {
            if (polyline.getLayer() == null || !isLayerEnabled(polyline.getLayer().getLayerName())) continue;

            FloatBuffer buffer = polyline.getOrBuildGlBuffer(bucketCenter, scale);
            int vertexCount = polyline.getCachedVertexCount();
            if (buffer == null || vertexCount < 2) continue;

            int color = GL_Methods.myParseColor(polyline.getLineColor());
            float[] rgb = GL_Methods.parseColorToGL(color);

            drawColoredVertices(buffer, vertexCount, GLES20.GL_LINE_STRIP, identity, rgb[0], rgb[1], rgb[2], 0.85f);
        }
    }

    public static void drawLineedge(GL11 gl, float scale) {
        if (!ensureReady()) return;

        Point3DF pGround;
        float[] p;

        try {
            switch (DataSaved.bucketEdge) {
                case -1:
                    p = new float[]{
                            DataSaved.GL_Bucket_Coord[0][0],
                            DataSaved.GL_Bucket_Coord[0][1],
                            DataSaved.GL_Bucket_Coord[0][2]
                    };
                    pGround = new Point3DF(
                            (float) (bucketLeftCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                            (float) (bucketLeftCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                            (float) ((bucketLeftCoord[2] - TriangleService.quota3D_SX) - DataSaved.glL_AnchorView[2]) * scale
                    );
                    break;

                case 0:
                    p = new float[]{
                            DataSaved.GL_Bucket_Coord[4][0],
                            DataSaved.GL_Bucket_Coord[4][1],
                            DataSaved.GL_Bucket_Coord[4][2]
                    };
                    pGround = new Point3DF(
                            (float) (bucketCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                            (float) (bucketCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                            (float) ((bucketCoord[2] - TriangleService.quota3D_CT) - DataSaved.glL_AnchorView[2]) * scale
                    );
                    break;

                case 1:
                    p = new float[]{
                            DataSaved.GL_Bucket_Coord[1][0],
                            DataSaved.GL_Bucket_Coord[1][1],
                            DataSaved.GL_Bucket_Coord[1][2]
                    };
                    pGround = new Point3DF(
                            (float) (bucketRightCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                            (float) (bucketRightCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                            (float) ((bucketRightCoord[2] - TriangleService.quota3D_DX) - DataSaved.glL_AnchorView[2]) * scale
                    );
                    break;

                default:
                    return;
            }

            float[] line = {
                    p[0], p[1], p[2],
                    pGround.x, pGround.y, pGround.z
            };

            FloatBuffer lineBuffer = createFloatBuffer(line);
            float[] red = GL_Methods.parseColorToGL(MyColorClass.colorConstraint);
            GLES20.glLineWidth(2f);
            drawColoredVertices(lineBuffer, 2, GLES20.GL_LINES, identity(), red[0], red[1], red[2], 1f);

            FloatBuffer pointBuffer = createFloatBuffer(new float[]{pGround.x, pGround.y, pGround.z});
            float[] blue = GL_Methods.parseColorToGL(Color.BLUE);
            drawColoredVertices(pointBuffer, 1, GLES20.GL_POINTS, identity(), blue[0], blue[1], blue[2], 1f);
        } catch (Exception e) {
            Log.e(TAG, "drawLineedge", e);
        }
    }

    public static void drawSelectedPoly(GL11 gl, List<Point3D> vertices, float lineW, int color, float scale) {
        if (vertices == null || vertices.size() < 2) return;
        if (!ensureReady()) return;

        int c = GL_Methods.myParseColor(color);
        float[] rgb = GL_Methods.parseColorToGL(c);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glLineWidth(Math.max(1f, lineW * scale));

        double[] bucketCenter = DataSaved.glL_AnchorView;
        float[] coords = new float[vertices.size() * 3];
        for (int i = 0; i < vertices.size(); i++) {
            Point3D p = vertices.get(i);
            coords[i * 3] = (float) ((p.getX() - bucketCenter[0]) * scale);
            coords[i * 3 + 1] = (float) ((p.getY() - bucketCenter[1]) * scale);
            coords[i * 3 + 2] = (float) ((p.getZ() - bucketCenter[2]) * scale);
        }

        FloatBuffer buffer = createFloatBuffer(coords);
        drawColoredVertices(buffer, vertices.size(), GLES20.GL_LINE_STRIP, identity(), rgb[0], rgb[1], rgb[2], 1f);
    }

    public static void drawPoints(GL11 gl, List<Point3D> points, float radius, float scale, boolean isXMLPoint) {
        if (points == null || points.isEmpty()) return;
        if (!ensureReady()) return;

        double[] bucketCenter = DataSaved.glL_AnchorView;
        GLES20.glUniform1f(colorProgram.uPointSize, radius * scale * 2f);

        for (Point3D p : points) {
            if (p.getLayer() == null || !isLayerEnabled(p.getLayer().getLayerName())) continue;

            float x = (float) ((p.getX() - bucketCenter[0]) * scale);
            float y = (float) ((p.getY() - bucketCenter[1]) * scale);
            float z = (float) ((p.getZ() - bucketCenter[2]) * scale);

            int color = isXMLPoint
                    ? GL_Methods.myParseColor(Color.WHITE)
                    : GL_Methods.myParseColor(p.getLayer().getColorState());
            float[] rgb = GL_Methods.parseColorToGL(color);

            FloatBuffer buf = createFloatBuffer(new float[]{x, y, z});
            drawColoredVertices(buf, 1, GLES20.GL_POINTS, identity(), rgb[0], rgb[1], rgb[2], 1f);
        }
    }

    public static void drawPNEZD(GL11 gl, List<PNEZDPoint> points, float radius, float scale) {
        if (points == null || points.isEmpty()) return;
        if (!ensureReady()) return;

        double[] bucketCenter = DataSaved.glL_AnchorView;
        GLES20.glUniform1f(colorProgram.uPointSize, radius * scale * 2f);

        for (PNEZDPoint p : points) {
            float x = (float) ((p.getEasting() - bucketCenter[0]) * scale);
            float y = (float) ((p.getNorthing() - bucketCenter[1]) * scale);
            float z = (float) ((p.getElevation() - bucketCenter[2]) * scale);

            int color = GL_Methods.myParseColor(Color.WHITE);
            if (p.getColor() != null) {
                color = GL_Methods.myParseColor(p.getColor());
            }

            float[] rgb = GL_Methods.parseColorToGL(color);
            FloatBuffer buf = createFloatBuffer(new float[]{x, y, z});
            drawColoredVertices(buf, 1, GLES20.GL_POINTS, identity(), rgb[0], rgb[1], rgb[2], 1f);
        }
    }

    public static void drawTextsBilBoardPNEZD(GL11 gl, List<PNEZDPoint> texts, double[] anchor, float charSpacingFactor, float scale, FontAtlas atlas) {
        if (texts == null || texts.isEmpty() || atlas == null) return;
        if (!ensureReady() || !hasViewMatrix) return;
        drawBillboardTextsPnezdInternal(texts, anchor, charSpacingFactor, scale, atlas);
    }

    public static void drawTextsBilBoard(GL11 gl, List<DxfText> texts, double[] anchor, float charSpacingFactor, float scale, FontAtlas atlas) {
        if (texts == null || texts.isEmpty() || atlas == null) return;
        if (!ensureReady() || !hasViewMatrix) return;
        drawBillboardTextsDxfInternal(texts, anchor, charSpacingFactor, scale, atlas);
    }

    private static void drawBillboardTextsPnezdInternal(List<PNEZDPoint> texts, double[] anchor, float charSpacingFactor, float scale, FontAtlas atlas) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        float charW = atlas.getCellSize() * scale * 0.01f;
        float charH = atlas.getCellSize() * scale * 0.01f;

        float[] right = getCameraRight();
        float[] up = getCameraUp();

        for (PNEZDPoint text : texts) {
            String str = text.getDescription();
            if (str == null || str.isEmpty()) continue;

            float baseX = (float) ((text.getEasting() - anchor[0]) * scale);
            float baseY = (float) ((text.getNorthing() - anchor[1]) * scale);
            float baseZ = (float) ((text.getElevation() - anchor[2]) * scale);

            drawBillboardString(str, baseX, baseY, baseZ, charW, charH, charSpacingFactor, atlas, right, up);
        }
    }

    private static void drawBillboardTextsDxfInternal(List<DxfText> texts, double[] anchor, float charSpacingFactor, float scale, FontAtlas atlas) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        float charW = atlas.getCellSize() * scale * 0.01f;
        float charH = atlas.getCellSize() * scale * 0.01f;

        float[] right = getCameraRight();
        float[] up = getCameraUp();

        for (DxfText text : texts) {
            String str = text.getText();
            if (str == null || str.isEmpty()) continue;

            float baseX = (float) ((text.getX() - anchor[0]) * scale);
            float baseY = (float) ((text.getY() - anchor[1]) * scale);
            float baseZ = (float) ((text.getZ() - anchor[2]) * scale);

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

    public static void drawLines2D(GL11 gl, List<Line> lines, float lineW, float scale) {
        if (lines == null || lines.isEmpty()) return;
        if (!ensureReady()) return;

        double[] bucket = DataSaved.glL_AnchorView;
        float width = Math.max(1f, lineW );

        for (Line line : lines) {
            if (line.getLayer() == null || !isLayerEnabled(line.getLayer().getLayerName())) continue;

            float[] coords = new float[6];
            coords[0] = (float) ((line.getStart().getX() - bucket[0]) * scale);
            coords[1] = (float) ((line.getStart().getY() - bucket[1]) * scale);
            coords[2] = 0f;

            coords[3] = (float) ((line.getEnd().getX() - bucket[0]) * scale);
            coords[4] = (float) ((line.getEnd().getY() - bucket[1]) * scale);
            coords[5] = 0f;

            float[] rgb = GL_Methods.parseColorToGL(GL_Methods.myParseColor(line.getColor()));
            drawThickSegments2D(coords, width, new float[]{rgb[0], rgb[1], rgb[2], 1f});
        }
    }

    public static void drawCircles2D(GL11 gl, List<Circle> circles, float lineW, float scale) {
        if (circles == null || circles.isEmpty()) return;
        if (!ensureReady()) return;

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glLineWidth(Math.max(1f, lineW));

        double[] bucket = DataSaved.glL_AnchorView;
        float[] identity = identity();

        for (Circle circle : circles) {
            if (circle.getLayer() == null || !isLayerEnabled(circle.getLayer().getLayerName())) continue;

            float cx = (float) ((circle.getCenter().getX() - bucket[0]) * scale);
            float cy = (float) ((circle.getCenter().getY() - bucket[1]) * scale);
            float r = (float) (circle.getRadius() * scale);

            float[] coords = new float[(CIRCLE_SEGMENTS_2D + 1) * 3];
            for (int i = 0; i <= CIRCLE_SEGMENTS_2D; i++) {
                double angle = 2 * Math.PI * i / CIRCLE_SEGMENTS_2D;
                coords[i * 3] = (float) (cx + r * Math.cos(angle));
                coords[i * 3 + 1] = (float) (cy + r * Math.sin(angle));
                coords[i * 3 + 2] = 0f;
            }

            FloatBuffer buffer = createFloatBuffer(coords);
            float[] rgb = GL_Methods.parseColorToGL(GL_Methods.myParseColor(circle.getColor()));
            drawColoredVertices(buffer, CIRCLE_SEGMENTS_2D + 1, GLES20.GL_LINE_STRIP, identity, rgb[0], rgb[1], rgb[2], 1f);
        }
    }

    public static void drawArcs2D(GL11 gl, List<Arc> arcs, float lineW, float scale) {
        if (arcs == null || arcs.isEmpty()) return;
        if (!ensureReady()) return;

        GLES20.glLineWidth(Math.max(1f, lineW));
        double[] bucket = DataSaved.glL_AnchorView;
        float[] identity = identity();

        for (Arc arc : arcs) {
            if (arc.getLayer() == null || !isLayerEnabled(arc.getLayer().getLayerName())) continue;

            float cx = (float) ((arc.getCenter().getX() - bucket[0]) * scale);
            float cy = (float) ((arc.getCenter().getY() - bucket[1]) * scale);
            float r = (float) (arc.getRadius() * scale);

            float startAngle = (float) arc.getStartAngle();
            float endAngle = (float) arc.getEndAngle();

            float sweep = endAngle - startAngle;
            if (sweep < 0) sweep += 360f;

            float[] coords = new float[(ARC_SEGMENTS_2D + 1) * 3];
            for (int i = 0; i <= ARC_SEGMENTS_2D; i++) {
                double angleDeg = startAngle + sweep * i / ARC_SEGMENTS_2D;
                double angleRad = Math.toRadians(angleDeg);
                coords[i * 3] = (float) (cx + r * Math.cos(angleRad));
                coords[i * 3 + 1] = (float) (cy + r * Math.sin(angleRad));
                coords[i * 3 + 2] = 0f;
            }

            FloatBuffer buffer = createFloatBuffer(coords);
            float[] rgb = GL_Methods.parseColorToGL(GL_Methods.myParseColor(arc.getColor()));
            drawColoredVertices(buffer, ARC_SEGMENTS_2D + 1, GLES20.GL_LINE_STRIP, identity, rgb[0], rgb[1], rgb[2], 1f);
        }
    }

    public static void drawPolylines2D(GL11 gl, List<Polyline_2D> polylines, float lineW, float scale) {
        if (polylines == null || polylines.isEmpty()) return;
        if (!ensureReady()) return;

        double[] bucket = DataSaved.glL_AnchorView;
        float width = Math.max(1f, lineW);

        for (Polyline_2D polyline : polylines) {
            if (polyline.getLayer() == null || !isLayerEnabled(polyline.getLayer().getLayerName())) continue;

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
                                (p1.getX() - bucket[0]) * scale,
                                (p1.getY() - bucket[1]) * scale,
                                0
                        ));
                    }
                    glPoints.add(new Point3D(
                            (p2.getX() - bucket[0]) * scale,
                            (p2.getY() - bucket[1]) * scale,
                            0
                    ));
                } else {
                    float[][] arcPoints = Math.abs(bulge) > 1
                            ? computeArcPointsM(p1, p2, bulge, scale, bucket)
                            : computeArcPoints(p1, p2, bulge, scale, bucket);

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

        float[] arr = new float[out.size()];
        for (int i = 0; i < out.size(); i++) arr[i] = out.get(i);

        FloatBuffer buffer = createFloatBuffer(arr);
        drawColoredVertices(
                buffer,
                arr.length / 3,
                GLES20.GL_TRIANGLES,
                identity(),
                color[0],
                color[1],
                color[2],
                color.length > 3 ? color[3] : 1f
        );
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
                            "uniform float uPointSize;\n"+
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
    public static void drawThickLineStrip2D(List<Point3D> points, double[] anchor, float scale, float widthPx, float[] color) {
        if (points == null || points.size() < 2) return;
        if (!ensureReady()) return;

        float[] triVerts = buildThickLineStrip2D(points, anchor, scale, widthPx);
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

    private static float[] buildThickLineStrip2D(List<Point3D> points, double[] anchor, float scale, float widthPx) {
        if (points.size() < 2) return null;

        List<Float> out = new ArrayList<>();
        float half = Math.max(0.5f, widthPx * 0.5f);

        for (int i = 0; i < points.size() - 1; i++) {
            Point3D p1 = points.get(i);
            Point3D p2 = points.get(i + 1);

            float x1 = (float) ((p1.getX() - anchor[0]) * scale);
            float y1 = (float) ((p1.getY() - anchor[1]) * scale);
            float z1 = 0f;

            float x2 = (float) ((p2.getX() - anchor[0]) * scale);
            float y2 = (float) ((p2.getY() - anchor[1]) * scale);
            float z2 = 0f;

            appendThickSegment(out, x1, y1, z1, x2, y2, z2, half);
        }

        float[] arr = new float[out.size()];
        for (int i = 0; i < out.size(); i++) arr[i] = out.get(i);
        return arr;
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

        out.add(ax);  out.add(ay);  out.add(z1);
        out.add(bx);  out.add(by);  out.add(z1);
        out.add(cx);  out.add(cy);  out.add(z2);

        out.add(cx);  out.add(cy);  out.add(z2);
        out.add(bx);  out.add(by);  out.add(z1);
        out.add(dx2); out.add(dy2); out.add(z2);
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
}