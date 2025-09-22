package gui.my_opengl.wheel;

import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;
import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.wheel.My_Wheel.cullaIndicesW;
import static gui.my_opengl.wheel.My_Wheel.leftSideIndicesW;
import static gui.my_opengl.wheel.My_Wheel.rightSideIndicesW;

import static packexcalib.exca.DataSaved.GL_WHEEL;

import android.graphics.Color;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import dxf.Point3D;
import gui.my_opengl.Cylinder;
import gui.my_opengl.GLDrawer;
import gui.my_opengl.GL_Methods;
import gui.my_opengl.Point3DF;
import gui.my_opengl.dozer.WheelRender;
import packexcalib.exca.DataSaved;

public class GL_DrawWheel {

    public static void draw(GL11 gl11) {

        WheelRender wheelRender=new WheelRender(DataSaved.GL_WHEEL,leftSideIndicesW,rightSideIndicesW,cullaIndicesW,coloreInterno, coloreEsterno, 0.02f);
        wheelRender.draw(gl11);
       /* float radius= (float) (DataSaved.W_Bucket*0.08*scale);
        Cylinder cylinder=new Cylinder(p3tof(GL_WHEEL[35]),p3tof(GL_WHEEL[36]),radius,radius,coloreInterno,10,true);
        cylinder.draw(gl11);*/

        Cylinder spigolo;
        int colore = Color.BLUE;
        if (DataSaved.isLowerEdge) {
            colore = Color.RED;
        }
        switch (DataSaved.bucketEdge) {
            case -1:
                spigolo = new Cylinder(p3tof(GL_WHEEL[20]), p3tof(GL_WHEEL[23]), 0.05f * scale, 0.02f * scale,
                        GL_Methods.parseColorToGL(colore), 8, false);
                spigolo.draw(gl11);
                break;

            case 0:
                spigolo = new Cylinder(p3tof(GL_WHEEL[18]), p3tof(GL_WHEEL[21]), 0.05f * scale, 0.01f * scale,
                        GL_Methods.parseColorToGL(colore), 8, false);
                spigolo.draw(gl11);
                break;

            case 1:
                spigolo = new Cylinder(p3tof(GL_WHEEL[19]), p3tof(GL_WHEEL[22]), 0.05f * scale, 0.01f * scale,
                        GL_Methods.parseColorToGL(colore), 8, false);
                spigolo.draw(gl11);
                break;
        }

        if (DataSaved.showAlign > 0) {
            gl11.glLineWidth(3f);
            float[] c = GL_Methods.parseColorToGL(Color.GREEN);
            gl11.glColor4f(c[0], c[1], c[2], 1);
            // Prepara buffer per le 4 linee
            float[] lineVertices = {
                    GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[31].getX(), GL_WHEEL[31].getY(), GL_WHEEL[31].getZ(),
                    GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[32].getX(), GL_WHEEL[32].getY(), GL_WHEEL[32].getZ(),
                    GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[34].getX(), GL_WHEEL[34].getY(), GL_WHEEL[34].getZ(),
                    GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[33].getX(), GL_WHEEL[33].getY(), GL_WHEEL[33].getZ(),
            };

            ByteBuffer bb = ByteBuffer.allocateDirect(lineVertices.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(lineVertices);
            vertexBuffer.position(0);

            // Abilita e disegna
            gl11.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl11.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            gl11.glDrawArrays(GL10.GL_LINES, 0, 8);
            gl11.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        }

        switch (DataSaved.isAutoSnap) {
            case 1:
            case 3:
                if (DataSaved.nearestPoint != null) {
                    drawPointDist(gl11, 5f, Color.GREEN, scale);
                }
                break;

            case 2:
            case 4:
                // Scegli quale polyline disegnare
                List<Point3D> polyVertices;
                if (DataSaved.selectedPoly_OFFSET != null && DataSaved.line_Offset != 0) {
                    polyVertices = DataSaved.selectedPoly_OFFSET.getVertices();
                } else if (DataSaved.selectedPoly != null) {
                    polyVertices = DataSaved.selectedPoly.getVertices();
                } else {
                    polyVertices = null;
                }

                if (polyVertices != null) {
                    GLDrawer.drawSelectedPoly(gl11, polyVertices, 5f, Color.GREEN, scale);
                }
                drawLineDist(gl11, 5f, Color.GREEN, scale);
                break;
        }
    }


    private static float[] p3tof(Point3DF point3DF) {
        return new float[]{
                point3DF.getX(), point3DF.getY(), point3DF.getZ()
        };
    }
    private static void drawLineDist(GL11 gl, float lineW, int color,float scale) {
        try {
            // Abilita anti-aliasing e blending per linee più lisce
            gl.glEnable(GL10.GL_LINE_SMOOTH);
            gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

            float[] c = GL_Methods.parseColorToGL(color);
            gl.glColor4f(c[0], c[1], c[2], 1.0f);

            // Imposta la larghezza della linea (assumendo lineW sia già corretto)
            gl.glLineWidth(Math.max(1f, lineW*scale));



            float[] coords = new float[]{
                    GL_WHEEL[24].getX(), GL_WHEEL[24].getY(), GL_WHEEL[24].getY(),
                    GL_WHEEL[25].getX(), GL_WHEEL[25].getY(), GL_WHEEL[25].getZ(),
                    GL_WHEEL[26].getX(), GL_WHEEL[26].getY(), GL_WHEEL[26].getZ()
            };

            FloatBuffer buffer = GL_Methods.createFloatBuffer(coords);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 3); // linea p1 -> p2 -> p3
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

            // Disabilita blending e line smooth dopo il disegno
            gl.glDisable(GL10.GL_BLEND);
            gl.glDisable(GL10.GL_LINE_SMOOTH);

        } catch (Exception e) {
        }
    }
    private static void drawPointDist(GL11 gl,float lineW,int color,float scale){
        {
            try {
                // Abilita anti-aliasing e blending per linee più lisce
                gl.glEnable(GL10.GL_LINE_SMOOTH);
                gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
                gl.glEnable(GL10.GL_BLEND);
                gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

                float[] c = GL_Methods.parseColorToGL(color);
                gl.glColor4f(c[0], c[1], c[2], 1.0f);

                // Imposta la larghezza della linea (assumendo lineW sia già corretto)
                gl.glLineWidth(Math.max(1f, lineW*scale));


                float[] coords = new float[]{
                        GL_WHEEL[27].getX(), GL_WHEEL[27].getY(), GL_WHEEL[27].getZ(),
                        GL_WHEEL[28].getX(), GL_WHEEL[28].getY(), GL_WHEEL[28].getZ(),
                        GL_WHEEL[29].getX(), GL_WHEEL[29].getY(), GL_WHEEL[29].getZ()
                };

                FloatBuffer buffer = GL_Methods.createFloatBuffer(coords);

                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, buffer);
                gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 3); // linea p1 -> p2 -> p3

                drawBillboardCircle(gl, DataSaved.nearestPoint,0.08f,scale);

                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

                // Disabilita blending e line smooth dopo il disegno
                gl.glDisable(GL10.GL_BLEND);
                gl.glDisable(GL10.GL_LINE_SMOOTH);

            } catch (Exception e) {
            }
        }
    }
    private static void drawBillboardCircle(GL11 gl, Point3D center, float radius, float scale) {
        final int segments = 32; // aumenta per cerchi più lisci

        float cx = (float) ((center.getX() - DataSaved.glL_AnchorView[0]) * scale);
        float cy = (float) ((center.getY() - DataSaved.glL_AnchorView[1]) * scale);
        float cz = (float) ((center.getZ() - DataSaved.glL_AnchorView[2]) * scale);

        // Raggio scalato
        float rOuter = radius * 1.1f; // bordo leggermente più grande
        float rInner = radius;

        // BORDO ESTERNO BIANCO
        float[] borderColor = GL_Methods.parseColorToGL(Color.GREEN);
        gl.glColor4f(borderColor[0], borderColor[1], borderColor[2], 1f);
        GLDrawer.drawCircle(gl, cx, cy, cz, rOuter, segments);

        // CERCHIO INTERNO ROSSO
        float[] fillColor = GL_Methods.parseColorToGL(Color.GREEN);
        gl.glColor4f(fillColor[0], fillColor[1], fillColor[2], 1f);
        GLDrawer.drawCircle(gl, cx, cy, cz, rInner, segments);
    }
}
