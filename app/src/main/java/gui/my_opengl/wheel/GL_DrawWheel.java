package gui.my_opengl.wheel;

import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;
import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.wheel.My_Wheel.cullaIndicesW;
import static gui.my_opengl.wheel.My_Wheel.leftSideIndicesW;
import static gui.my_opengl.wheel.My_Wheel.rightSideIndicesW;
import static packexcalib.exca.DataSaved.GL_WHEEL;

import android.graphics.Color;

import java.util.List;

import gui.my_opengl.Cylinder;
import gui.my_opengl.GLDrawer;
import gui.my_opengl.GL_Methods;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import dxf.Point3D;
import gui.my_opengl.dozer.WheelRender;
import packexcalib.exca.DataSaved;

public class GL_DrawWheel {

    public static void draw(GL11 gl11) {

        WheelRender wheelRender = new WheelRender(
                DataSaved.GL_WHEEL,
                leftSideIndicesW,
                rightSideIndicesW,
                cullaIndicesW,
                coloreInterno,
                coloreEsterno,
                0.02f
        );
        wheelRender.draw(gl11);

        drawBucketEdge(gl11);
        drawAlignHelpers(gl11);
        drawSnapHelpers(gl11);
    }

    private static void drawBucketEdge(GL11 gl11) {
        Cylinder spigolo;
        int colore = Color.BLUE;
        if (DataSaved.isLowerEdge) {
            colore = Color.RED;
        }

        switch (DataSaved.bucketEdge) {
            case -1:
                spigolo = new Cylinder(
                        p3tof(GL_WHEEL[20]),
                        p3tof(GL_WHEEL[23]),
                        0.05f * scale,
                        0.02f * scale,
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl11);
                break;

            case 0:
                spigolo = new Cylinder(
                        p3tof(GL_WHEEL[18]),
                        p3tof(GL_WHEEL[21]),
                        0.05f * scale,
                        0.01f * scale,
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl11);
                break;

            case 1:
                spigolo = new Cylinder(
                        p3tof(GL_WHEEL[19]),
                        p3tof(GL_WHEEL[22]),
                        0.05f * scale,
                        0.01f * scale,
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl11);
                break;
        }
    }

    private static void drawAlignHelpers(GL11 gl11) {
        if (DataSaved.showAlign <= 0) return;

        float[] c = GL_Methods.parseColorToGL(Color.GREEN);

        float[] lineVertices = {
                GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[31].getX(), GL_WHEEL[31].getY(), GL_WHEEL[31].getZ(),
                GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[32].getX(), GL_WHEEL[32].getY(), GL_WHEEL[32].getZ(),
                GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[34].getX(), GL_WHEEL[34].getY(), GL_WHEEL[34].getZ(),
                GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[33].getX(), GL_WHEEL[33].getY(), GL_WHEEL[33].getZ(),
        };

        GLDrawer.drawRawLines3D(
                lineVertices,
                8,
                new float[]{c[0], c[1], c[2], 1f},
                3f
        );
    }

    private static void drawSnapHelpers(GL11 gl11) {
        switch (DataSaved.isAutoSnap) {
            case 1:
            case 3:
                if (DataSaved.nearestPoint != null) {
                    drawPointDist(gl11, 5f, Color.GREEN, scale);
                }
                break;

            case 2:
            case 4:
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
                point3DF.getX(),
                point3DF.getY(),
                point3DF.getZ()
        };
    }

    private static void drawLineDist(GL11 gl, float lineW, int color, float scale) {
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
                    Math.max(1f, lineW * scale)
            );
        } catch (Exception ignored) {
        }
    }

    private static void drawPointDist(GL11 gl, float lineW, int color, float scale) {
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
                    Math.max(1f, lineW * scale)
            );

            drawBillboardCircle(gl, DataSaved.nearestPoint, 0.08f, scale);
        } catch (Exception ignored) {
        }
    }

    private static void drawBillboardCircle(GL11 gl, Point3D center, float radius, float scale) {
        if (center == null) return;

        final int segments = 32;

        float cx = (float) ((center.getX() - DataSaved.glL_AnchorView[0]) * scale);
        float cy = (float) ((center.getY() - DataSaved.glL_AnchorView[1]) * scale);
        float cz = (float) ((center.getZ() - DataSaved.glL_AnchorView[2]) * scale);

        float rOuter = radius * 1.1f;
        float rInner = radius;

        float[] green = GL_Methods.parseColorToGL(Color.GREEN);
        GLDrawer.drawCircle(gl, cx, cy, cz, rOuter, segments, green);
        GLDrawer.drawCircle(gl, cx, cy, cz, rInner, segments, green);
    }
}