package gui.my_opengl;

import static gui.my_opengl.GL_Methods.createFloatBuffer;
import static gui.my_opengl.GL_Methods.getJetColor;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;

import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

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
    private static final Map<String, Integer> textTextureCache = new HashMap<>();
    private static final Map<String, float[]> textSizeCache = new HashMap<>();
    static GL10 glClear;


    public static void drawFaces(GL11 gl, List<Face3D> faces, float lineW, float scale, boolean isXML) {
        if (!My3DActivity.glGradient) {
            try {
                gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
                gl.glEnable(GL10.GL_BLEND);
                gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

                gl.glLineWidth(Math.max(1f, lineW * scale));

                double[] bucketCenter = DataSaved.glL_AnchorView;
                List<Face3D> sortedFaces = new ArrayList<>(faces);

                // Ordina dal più lontano al più vicino
                sortedFaces.sort((f1, f2) ->
                        Double.compare(
                                GL_Methods.averageZ(f2, bucketCenter),
                                GL_Methods.averageZ(f1, bucketCenter)
                        )
                );

                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

                for (Face3D face : sortedFaces) {
                    if (face.getLayer() == null || !isLayerEnabled(face.getLayer().getLayerName()))
                        continue;

                    face.prepareVertexBuffer(bucketCenter, scale);
                    FloatBuffer buffer = face.getVertexBuffer();
                    if (buffer == null) continue;

                    // Calcolo colore
                    int color = isXML
                            ? GL_Methods.myParseColor(AutoCADColor.getColor(String.valueOf(face.getLayer().getColorState())))
                            : GL_Methods.myParseColor(face.getLayer().getColorState());
                    float[] rgb = GL_Methods.parseColorToGL(color);

                    boolean isTriangle = face.getP4().equals(face.getP3());
                    int vertexCount = isTriangle ? 3 : 4;
                    int drawMode = isTriangle ? GL10.GL_TRIANGLES : GL10.GL_TRIANGLE_FAN;

                    gl.glPushMatrix();
                    gl.glTranslatef(0f, 0f, (float) (-DataSaved.offsetH * scale)); // <-- offset verticale
                    // Riempimento semitrasparente
                    if (My3DActivity.glFill) {
                        gl.glColor4f(rgb[0], rgb[1], rgb[2], 0.3f);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
                        gl.glDrawArrays(drawMode, 0, vertexCount);
                    }

                    // Contorno definito
                    if (My3DActivity.glFace) {
                        buffer.position(0);
                        gl.glColor4f(rgb[0], rgb[1], rgb[2], 1f);
                        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
                        gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, vertexCount);
                    }

                    gl.glPopMatrix();
                }

                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            } catch (Exception e) {
            }
        }
    }


    public static void drawFacesGradient(GL11 gl, List<Face3D> faces, float scale, double zMax, double zMin) {
        try {
            double[] bucketCenter = DataSaved.glL_AnchorView;
            List<Face3D> sortedFaces = new ArrayList<>(faces);

            // Ordina le facce per profondità rispetto alla benna
            sortedFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            for (Face3D face : sortedFaces) {
                if (!isLayerEnabled(face.getLayer().getLayerName())) continue;

                face.prepareVertexBuffer(bucketCenter, scale);
                FloatBuffer vertexBuffer = face.getVertexBuffer();
                if (vertexBuffer == null) continue;

                List<Point3D> points = face.getVertices(); // Assicurati che sia implementato
                int vertexCount = points.size();

                // === Crea il color buffer per il gradiente ===
                float[] colorArray = new float[vertexCount * 4];
                for (int i = 0; i < vertexCount; i++) {
                    float[] color = getJetColor(points.get(i).getZ(), zMin, zMax);
                    colorArray[i * 4] = color[0];
                    colorArray[i * 4 + 1] = color[1];
                    colorArray[i * 4 + 2] = color[2];
                    colorArray[i * 4 + 3] = 1f; // Opacità piena
                }
                FloatBuffer colorBuffer = createFloatBuffer(colorArray);

                // === Disegno ===
                gl.glPushMatrix();
                gl.glTranslatef(0f, 0f, (float) (-DataSaved.offsetH * scale)); // Offset Z

                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);

                int drawMode = face.getP4().equals(face.getP3()) ? GL10.GL_TRIANGLES : GL10.GL_TRIANGLE_FAN;
                gl.glDrawArrays(drawMode, 0, vertexCount);

                gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

                // === Bordo dei triangoli ===
                if (My3DActivity.glFace) {
                    gl.glLineWidth(1f);
                    float[] edgeColor = GL_Methods.parseColorToGL(MyColorClass.colorConstraint);
                    gl.glColor4f(edgeColor[0], edgeColor[1], edgeColor[2], 0.8f);

                    vertexBuffer.position(0); // Reset posizione
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
                    gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, vertexCount);
                }

                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glPopMatrix();
            }

        } catch (Exception e) {
        }
    }


    public static void drawFacesGradient2D(GL11 gl, List<Face3D> faces, float scale, double zMax, double zMin) {
        try {
            double[] bucketCenter = DataSaved.glL_AnchorView;
            List<Face3D> sortedFaces = new ArrayList<>(faces);

            // Ordina le facce per profondità (opzionale in 2D, ma utile per trasparenze future)
            sortedFaces.sort((f1, f2) ->
                    Double.compare(
                            GL_Methods.averageZ(f2, bucketCenter),
                            GL_Methods.averageZ(f1, bucketCenter)
                    )
            );

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            gl.glLineWidth(1f);

            for (Face3D face : sortedFaces) {
                if (!isLayerEnabled(face.getLayer().getLayerName())) continue;

                // Prepara il vertex buffer per 2D (Z = 0)
                face.prepareVertexBuffer2DForGradient(bucketCenter, scale);
                FloatBuffer vertexBuffer = face.getVertexBuffer();
                if (vertexBuffer == null) continue;

                // Usa i vertici originali per calcolare i colori da quota Z
                List<Point3D> points = face.getVerticesWithZ();
                int vertexCount = points.size();
                float[] colorArray = new float[vertexCount * 4];

                for (int i = 0; i < vertexCount; i++) {
                    float[] color = GL_Methods.getJetColor(points.get(i).getZ(), zMin, zMax); // 20 cm step
                    colorArray[i * 4] = color[0];     // R
                    colorArray[i * 4 + 1] = color[1]; // G
                    colorArray[i * 4 + 2] = color[2]; // B
                    colorArray[i * 4 + 3] = 1f;       // A
                }

                FloatBuffer colorBuffer = GL_Methods.createFloatBuffer(colorArray);

                gl.glPushMatrix();

                // Riempimento con gradiente
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
                gl.glDrawArrays(
                        face.getP4().equals(face.getP3()) ? GL10.GL_TRIANGLES : GL10.GL_TRIANGLE_FAN,
                        0,
                        vertexCount
                );

                // Bordo bianco
                vertexBuffer.position(0);
                gl.glColor4f(1f, 1f, 1f, 1f);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
                gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, vertexCount);

                gl.glPopMatrix();
            }

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        } catch (Exception e) {
        }
    }

    public static void drawPolylines(GL11 gl, List<Polyline> polylines, float lineW, float scale) {


        gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glLineWidth(Math.max(1f, lineW * scale));

        double[] bucketCenter = DataSaved.glL_AnchorView;
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        for (Polyline polyline : polylines) {
            if (polyline.getLayer() == null || !isLayerEnabled(polyline.getLayer().getLayerName()))
                continue;

            List<Point3D> vertices = polyline.getVertices();
            if (vertices.size() < 2) continue;

            float[] coords = new float[vertices.size() * 3];
            for (int i = 0; i < vertices.size(); i++) {
                Point3D pt = vertices.get(i);
                coords[i * 3] = (float) ((pt.getX() - bucketCenter[0]) * scale);
                coords[i * 3 + 1] = (float) ((pt.getY() - bucketCenter[1]) * scale);
                coords[i * 3 + 2] = (float) ((pt.getZ() - bucketCenter[2]) * scale);
            }

            FloatBuffer buffer = createFloatBuffer(coords);
            int color = GL_Methods.myParseColor(polyline.getLineColor());
            float[] rgb = GL_Methods.parseColorToGL(color);

            gl.glColor4f(rgb[0], rgb[1], rgb[2], 0.85f);  // Leggera trasparenza
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, vertices.size());
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    public static void drawLineedge(GL11 gl, float scale) {
        Point3DF pGround = null;
        float[] p = new float[3];
        try {
            switch (DataSaved.bucketEdge) {
                case -1:
                    p = new float[]{DataSaved.GL_Bucket_Coord[0][0], DataSaved.GL_Bucket_Coord[0][1], DataSaved.GL_Bucket_Coord[0][2]};
                    pGround = new Point3DF((float) (bucketLeftCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                            (float) (bucketLeftCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                            (float) ((bucketLeftCoord[2] - TriangleService.quota3D_SX) - DataSaved.glL_AnchorView[2]) * scale);
                    break;

                case 0:
                    p = new float[]{
                            DataSaved.GL_Bucket_Coord[4][0], DataSaved.GL_Bucket_Coord[4][1], DataSaved.GL_Bucket_Coord[4][2]
                    };

                    pGround = new Point3DF((float) (bucketCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                            (float) (bucketCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                            (float) ((bucketCoord[2] - TriangleService.quota3D_CT) - DataSaved.glL_AnchorView[2]) * scale);
                    break;


                case 1:
                    p = new float[]{
                            DataSaved.GL_Bucket_Coord[1][0], DataSaved.GL_Bucket_Coord[1][1], DataSaved.GL_Bucket_Coord[1][2]
                    };
                    pGround = new Point3DF((float) (bucketRightCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                            (float) (bucketRightCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                            (float) ((bucketRightCoord[2] - TriangleService.quota3D_DX) - DataSaved.glL_AnchorView[2]) * scale);
                    break;
            }
            float[] line = {
                    p[0], p[1], p[2],
                    pGround.x, pGround.y, pGround.z
            };

            FloatBuffer lineBuffer = ByteBuffer.allocateDirect(6 * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            lineBuffer.put(line).position(0);

            // Imposta colore e disegna la linea
            float[] red = GL_Methods.parseColorToGL(MyColorClass.colorConstraint);
            gl.glColor4f(red[0], red[1], red[2], 1.0f);
            gl.glLineWidth(2.0f);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);
            gl.glDrawArrays(GL10.GL_LINES, 0, 2);
            //punto
            FloatBuffer pointBuffer = ByteBuffer.allocateDirect(3 * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            pointBuffer.put(new float[]{pGround.x, pGround.y, pGround.z}).position(0);

            red = GL_Methods.parseColorToGL(Color.BLUE);
            gl.glColor4f(red[0], red[1], red[2], 1.0f);
            gl.glPointSize(9f);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, pointBuffer);
            gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        } catch (Exception e) {
        }

    }


    public static void drawSelectedPoly(GL11 gl, List<Point3D> vertices, float lineW, int color, float scale) {
        if (vertices == null || vertices.size() < 2) return;

        int c = GL_Methods.myParseColor(color);
        float[] rgb = GL_Methods.parseColorToGL(c);

        // Abilita antialiasing e blending per linee più lisce


        gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        // Imposta la larghezza della linea scalata e limitata ad almeno 1
        gl.glLineWidth(Math.max(1f, lineW * scale));

        double[] bucketCenter = DataSaved.glL_AnchorView;

        float[] coords = new float[vertices.size() * 3];
        for (int i = 0; i < vertices.size(); i++) {
            Point3D p = vertices.get(i);
            coords[i * 3] = (float) ((p.getX() - bucketCenter[0]) * scale);
            coords[i * 3 + 1] = (float) ((p.getY() - bucketCenter[1]) * scale);
            coords[i * 3 + 2] = (float) ((p.getZ() - bucketCenter[2]) * scale);
        }

        FloatBuffer buffer = createFloatBuffer(coords);

        gl.glColor4f(rgb[0], rgb[1], rgb[2], 1.0f);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, vertices.size());
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        // Disabilita blending e line smooth se non servono altrove
        gl.glDisable(GL10.GL_BLEND);

    }

    public static void drawPoints(GL11 gl, List<Point3D> points, float radius, float scale, boolean isXMLPoint) {
        double[] bucketCenter = DataSaved.glL_AnchorView;


        gl.glEnable(GL10.GL_POINT_SMOOTH);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // Imposta la dimensione dei punti (scalata in base al radius passato)
        gl.glPointSize(radius * 2 * scale); // moltiplica per 2 per diametro visibile più grande

        for (Point3D p : points) {
            if (p.getLayer() == null || !isLayerEnabled(p.getLayer().getLayerName())) continue;

            float x = (float) ((p.getX() - bucketCenter[0]) * scale);
            float y = (float) ((p.getY() - bucketCenter[1]) * scale);
            float z = (float) ((p.getZ() - bucketCenter[2]) * scale);

            int color = isXMLPoint ? GL_Methods.myParseColor(Color.WHITE)
                    : GL_Methods.myParseColor(p.getLayer().getColorState());
            float[] rgb = GL_Methods.parseColorToGL(color);

            float[] coords = new float[]{x, y, z};
            FloatBuffer buf = ByteBuffer.allocateDirect(3 * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            buf.put(coords);
            buf.position(0);

            gl.glColor4f(rgb[0], rgb[1], rgb[2], 1f);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buf);
            gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
        }


        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_POINT_SMOOTH);

    }

    public static void drawPNEZD(GL11 gl, List<PNEZDPoint> points, float radius, float scale) {
        double[] bucketCenter = DataSaved.glL_AnchorView;


        gl.glEnable(GL10.GL_POINT_SMOOTH);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // Imposta la dimensione dei punti (scalata in base al radius passato)
        gl.glPointSize(radius * 2 * scale); // moltiplica per 2 per diametro visibile più grande
        for (PNEZDPoint p : points) {


            float x = (float) ((p.getEasting() - bucketCenter[0]) * scale);
            float y = (float) ((p.getNorthing() - bucketCenter[1]) * scale);
            float z = (float) ((p.getElevation() - bucketCenter[2]) * scale);

            int color = GL_Methods.myParseColor(Color.WHITE);
            if(p.getColor()!=null){
                color=GL_Methods.myParseColor(p.getColor());
            }

            float[] rgb = GL_Methods.parseColorToGL(color);

            float[] coords = new float[]{x, y, z};
            FloatBuffer buf = ByteBuffer.allocateDirect(3 * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            buf.put(coords);
            buf.position(0);

            gl.glColor4f(rgb[0], rgb[1], rgb[2], 1f);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buf);
            gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
        }
    }
    public static void drawTextsBilBoardPNEZD(GL11 gl, List<PNEZDPoint> texts, double[] anchor, float charSpacingFactor, float scale, FontAtlas atlas) {
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer texBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_LIGHTING); // testi = non illuminati

        gl.glBindTexture(GL10.GL_TEXTURE_2D, atlas.getTextureId());

        float charW = atlas.getCellSize() * scale * 0.01f;
        float charH = atlas.getCellSize() * scale * 0.01f;
        GL11 gl11 = (gl instanceof GL11) ? (GL11) gl : null;
        if (gl11 == null) return;
        float[] modelView = new float[16];

        for (PNEZDPoint text : texts) {
            String str = text.getDescription();
            if (str == null || str.isEmpty()) continue;

            float baseX = (float) ((text.getEasting() - anchor[0]) * scale);
            float baseY = (float) ((text.getNorthing() - anchor[1]) * scale);
            float baseZ = (float) ((text.getElevation() - anchor[2]) * scale);

            gl.glPushMatrix();
            gl.glTranslatef(baseX, baseY, baseZ);

            // Billboard: rimuove la rotazione dalla matrice modelview
            gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelView, 0);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    modelView[i * 4 + j] = (i == j) ? 1f : 0f; // solo diagonale = no rotazione
                }
            }
            gl.glLoadMatrixf(modelView, 0);

            float xCursor = 0f; // testo locale, centrato sul punto

            for (char c : str.toCharArray()) {
                RectF uv = atlas.getUV(c);

                float[] vertices = {
                        xCursor, 0f, 0f,
                        xCursor + charW, 0f, 0f,
                        xCursor, charH, 0f,
                        xCursor + charW, charH, 0f
                };

                float[] tex = {
                        uv.left, uv.top,
                        uv.right, uv.top,
                        uv.left, uv.bottom,
                        uv.right, uv.bottom
                };

                vertexBuffer.clear();
                texBuffer.clear();
                vertexBuffer.put(vertices).position(0);
                texBuffer.put(tex).position(0);

                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
                gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer);
                gl.glColor4f(1f, 1f, 1f, 1f);
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                xCursor += charW * charSpacingFactor;
            }

            gl.glPopMatrix();
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    public static void drawTextsBilBoard(GL11 gl, List<DxfText> texts, double[] anchor, float charSpacingFactor, float scale, FontAtlas atlas) {
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer texBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_LIGHTING); // testi = non illuminati

        gl.glBindTexture(GL10.GL_TEXTURE_2D, atlas.getTextureId());

        float charW = atlas.getCellSize() * scale * 0.01f;
        float charH = atlas.getCellSize() * scale * 0.01f;
        GL11 gl11 = (gl instanceof GL11) ? (GL11) gl : null;
        if (gl11 == null) return;
        float[] modelView = new float[16];

        for (DxfText text : texts) {
            String str = text.getText();
            if (str == null || str.isEmpty()) continue;

            float baseX = (float) ((text.getX() - anchor[0]) * scale);
            float baseY = (float) ((text.getY() - anchor[1]) * scale);
            float baseZ = (float) ((text.getZ() - anchor[2]) * scale);

            gl.glPushMatrix();
            gl.glTranslatef(baseX, baseY, baseZ);

            // Billboard: rimuove la rotazione dalla matrice modelview
            gl11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelView, 0);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    modelView[i * 4 + j] = (i == j) ? 1f : 0f; // solo diagonale = no rotazione
                }
            }
            gl.glLoadMatrixf(modelView, 0);

            float xCursor = 0f; // testo locale, centrato sul punto

            for (char c : str.toCharArray()) {
                RectF uv = atlas.getUV(c);

                float[] vertices = {
                        xCursor, 0f, 0f,
                        xCursor + charW, 0f, 0f,
                        xCursor, charH, 0f,
                        xCursor + charW, charH, 0f
                };

                float[] tex = {
                        uv.left, uv.top,
                        uv.right, uv.top,
                        uv.left, uv.bottom,
                        uv.right, uv.bottom
                };

                vertexBuffer.clear();
                texBuffer.clear();
                vertexBuffer.put(vertices).position(0);
                texBuffer.put(tex).position(0);

                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
                gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer);
                gl.glColor4f(1f, 1f, 1f, 1f);
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

                xCursor += charW * charSpacingFactor;
            }

            gl.glPopMatrix();
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);
    }





    private static boolean isLayerEnabled(String layerName) {
        if (layerName == null || layerName.isEmpty()) {
            return false; // Layer nullo o vuoto non è abilitato
        }

        // Cerca il layer nelle tre liste
        for (Layer layer : DataSaved.dxfLayers_DTM) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                return true;
            }
        }
        for (Layer layer : DataSaved.dxfLayers_POLY) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                return true;
            }
        }
        for (Layer layer : DataSaved.dxfLayers_POINT) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                return true;
            }
        }

        return false; // Se il layer non è trovato o non è abilitato
    }

    /// entità solo 2D
    public static void drawLines2D(GL11 gl, List<Line> lines, float lineW, float scale) {
        if (lines == null || lines.isEmpty()) return;

        // Abilita antialiasing e blending per linee più lisce


        gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        // Imposta la larghezza della linea scalata, con minimo 1
        gl.glLineWidth(Math.max(1f, lineW * scale));

        double[] bucket = DataSaved.glL_AnchorView;

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        for (Line line : lines) {
            if (line.getLayer() == null || !isLayerEnabled(line.getLayer().getLayerName()))
                continue;

            float[] coords = new float[6];
            coords[0] = (float) ((line.getStart().getX() - bucket[0]) * scale);
            coords[1] = (float) ((line.getStart().getY() - bucket[1]) * scale);
            coords[2] = 0f;

            coords[3] = (float) ((line.getEnd().getX() - bucket[0]) * scale);
            coords[4] = (float) ((line.getEnd().getY() - bucket[1]) * scale);
            coords[5] = 0f;

            FloatBuffer buffer = createFloatBuffer(coords);

            float[] rgb = GL_Methods.parseColorToGL(GL_Methods.myParseColor(line.getColor()));
            gl.glColor4f(rgb[0], rgb[1], rgb[2], 1f);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
            gl.glDrawArrays(GL10.GL_LINES, 0, 2);
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        // Disabilita blending e line smooth
        gl.glDisable(GL10.GL_BLEND);

    }


    public static void drawCircles2D(GL11 gl, List<Circle> circles, float lineW, float scale) {
        // Abilita line smooth e blending per linee più morbide


        gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glLineWidth(Math.max(1f, lineW));
        double[] bucket = DataSaved.glL_AnchorView;

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        final int segments = 72; // Aumentato per un cerchio più liscio

        for (Circle circle : circles) {
            if (circle.getLayer() == null || !isLayerEnabled(circle.getLayer().getLayerName()))
                continue;

            float cx = (float) ((circle.getCenter().getX() - bucket[0]) * scale);
            float cy = (float) ((circle.getCenter().getY() - bucket[1]) * scale);
            float r = (float) (circle.getRadius() * scale);

            float[] coords = new float[(segments + 1) * 3];
            for (int i = 0; i <= segments; i++) {
                double angle = 2 * Math.PI * i / segments;
                coords[i * 3] = (float) (cx + r * Math.cos(angle));
                coords[i * 3 + 1] = (float) (cy + r * Math.sin(angle));
                coords[i * 3 + 2] = 0f;
            }

            FloatBuffer buffer = createFloatBuffer(coords);
            float[] rgb = GL_Methods.parseColorToGL(GL_Methods.myParseColor(circle.getColor()));
            gl.glColor4f(rgb[0], rgb[1], rgb[2], 1f);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, segments + 1);
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        // Disabilita blending e line smooth dopo il disegno
        gl.glDisable(GL10.GL_BLEND);

    }


    public static void drawArcs2D(GL11 gl, List<Arc> arcs, float lineW, float scale) {
        gl.glLineWidth(lineW);
        double[] bucket = DataSaved.glL_AnchorView;

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        final int segments = 36;

        for (Arc arc : arcs) {
            if (arc.getLayer() == null || !isLayerEnabled(arc.getLayer().getLayerName())) continue;

            float cx = (float) ((arc.getCenter().getX() - bucket[0]) * scale);
            float cy = (float) ((arc.getCenter().getY() - bucket[1]) * scale);
            float r = (float) (arc.getRadius() * scale);

            float startAngle = (float) arc.getStartAngle();
            float endAngle = (float) arc.getEndAngle();

            float sweep = endAngle - startAngle;
            if (sweep < 0) sweep += 360;

            float[] coords = new float[(segments + 1) * 3];
            for (int i = 0; i <= segments; i++) {
                double angleDeg = startAngle + sweep * i / segments;
                double angleRad = Math.toRadians(angleDeg);
                coords[i * 3] = (float) (cx + r * Math.cos(angleRad));
                coords[i * 3 + 1] = (float) (cy + r * Math.sin(angleRad));
                coords[i * 3 + 2] = 0f;
            }

            FloatBuffer buffer = createFloatBuffer(coords);
            float[] rgb = GL_Methods.parseColorToGL(GL_Methods.myParseColor(arc.getColor()));
            gl.glColor4f(rgb[0], rgb[1], rgb[2], 1f);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, segments + 1);
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }


    public static void drawPolylines2D(GL11 gl, List<Polyline_2D> polylines, float lineW, float scale) {
        // Abilita line smooth e blending per linee più morbide


        gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glLineWidth(Math.max(1f, lineW));
        double[] bucket = DataSaved.glL_AnchorView;
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        for (Polyline_2D polyline : polylines) {
            if (polyline.getLayer() == null || !isLayerEnabled(polyline.getLayer().getLayerName()))
                continue;

            List<Point3D> vertices = polyline.getVertices();
            if (vertices.size() < 2) continue;

            int color = GL_Methods.myParseColor(polyline.getLineColor());
            float[] rgb = GL_Methods.parseColorToGL(color);
            gl.glColor4f(rgb[0], rgb[1], rgb[2], 1.0f);

            for (int i = 0; i < vertices.size() - 1; i++) {
                Point3D p1 = vertices.get(i);
                Point3D p2 = vertices.get(i + 1);
                double bulge = p1.getBulge();

                if (bulge == 0) {
                    // Linea retta tra p1 e p2
                    float[] coords = new float[]{
                            (float) ((p1.getX() - bucket[0]) * scale),
                            (float) ((p1.getY() - bucket[1]) * scale),
                            0f,
                            (float) ((p2.getX() - bucket[0]) * scale),
                            (float) ((p2.getY() - bucket[1]) * scale),
                            0f
                    };
                    FloatBuffer buffer = createFloatBuffer(coords);
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
                    gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 2);

                } else {
                    // Arco tra p1 e p2
                    float[][] arcPoints;
                    if (Math.abs(bulge) > 1) {
                        arcPoints = computeArcPointsM(p1, p2, bulge, scale, bucket);
                    } else {
                        arcPoints = computeArcPoints(p1, p2, bulge, scale, bucket);
                    }
                    FloatBuffer buffer = createFloatBuffer(flatten(arcPoints));
                    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
                    gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, arcPoints.length);
                }
            }
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        // Disabilita blending e line smooth dopo il disegno
        gl.glDisable(GL10.GL_BLEND);

    }


    private static float[][] computeArcPointsM(Point3D p1, Point3D p2, double bulge, float scale, double[] bucket) {
        final int numSegments = 32;

        float x1 = (float) ((p1.getX() - bucket[0]) * scale);
        float y1 = (float) ((p1.getY() - bucket[1]) * scale);
        float z1 = (float) ((p1.getZ() - bucket[2]) * scale);

        float x2 = (float) ((p2.getX() - bucket[0]) * scale);
        float y2 = (float) ((p2.getY() - bucket[1]) * scale);
        float z2 = (float) ((p2.getZ() - bucket[2]) * scale);

        double chordLength = Math.hypot(x2 - x1, y2 - y1);
        double theta = 4 * Math.atan(Math.abs(bulge));
        double radius = Math.abs((chordLength / 2) / Math.sin(theta / 2));

        float midX = (x1 + x2) / 2;
        float midY = (y1 + y2) / 2;

        double sagitta = Math.sqrt(radius * radius - (chordLength / 2) * (chordLength / 2));

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
            if (sweepAngle < 0) sweepAngle += 2 * Math.PI;
        } else {
            if (sweepAngle > 0) sweepAngle -= 2 * Math.PI;
        }

        float[][] arcPoints = new float[numSegments + 1][3];
        for (int i = 0; i <= numSegments; i++) {
            float angle = startAngle + (sweepAngle * i / numSegments);
            float px = (float) (centerX + radius * Math.cos(angle));
            float py = (float) (centerY + radius * Math.sin(angle));
            arcPoints[i][0] = px;
            arcPoints[i][1] = py;
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
        float z2 = (float) ((p2.getZ() - bucket[2]) * scale);

        double distance = Math.hypot(x2 - x1, y2 - y1);
        double theta = 4 * Math.atan(Math.abs(bulge));
        double radius = (distance / 2) / Math.abs(Math.sin(theta / 2));

        float midX = (x1 + x2) / 2;
        float midY = (y1 + y2) / 2;

        double height = Math.sqrt(radius * radius - (distance / 2) * (distance / 2));

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
            if (sweepAngle < 0) sweepAngle += 2 * Math.PI;
        } else {
            if (sweepAngle > 0) sweepAngle -= 2 * Math.PI;
        }

        // Correzione sweep per archi "invertiti"
        if (Math.abs(bulge) > 1 && Math.abs(Math.toDegrees(sweepAngle)) < 180) {
            sweepAngle = (float) ((bulge > 0) ? (2 * Math.PI - Math.abs(sweepAngle)) : -(2 * Math.PI - Math.abs(sweepAngle)));
        }

        float[][] arcPoints = new float[numSegments + 1][3];
        for (int i = 0; i <= numSegments; i++) {
            float angle = startAngle + (sweepAngle * i / numSegments);
            float px = (float) (centerX + radius * Math.cos(angle));
            float py = (float) (centerY + radius * Math.sin(angle));
            arcPoints[i][0] = px;
            arcPoints[i][1] = py;
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


    // Disegna un cerchio pieno sul piano XY (a z = cz)
    public static void drawCircle(GL11 gl, float cx, float cy, float cz, float radius, int segments) {
        float[] coords = new float[(segments + 2) * 3]; // centro + tutti punti + ritorno

        coords[0] = cx;
        coords[1] = cy;
        coords[2] = cz;

        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            coords[(i + 1) * 3] = cx + (float) (radius * Math.cos(angle));
            coords[(i + 1) * 3 + 1] = cy + (float) (radius * Math.sin(angle));
            coords[(i + 1) * 3 + 2] = cz;
        }

        FloatBuffer buffer = ByteBuffer.allocateDirect(coords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(coords).position(0);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, segments + 2);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    public static void clearTextTextureCache() {
        try {
            for (int textureId : textTextureCache.values()) {
                int[] ids = {textureId};
                glClear.glDeleteTextures(1, ids, 0);
            }
            textTextureCache.clear();
            textSizeCache.clear();
        } catch (Exception ignored) {
        }

    }

}

