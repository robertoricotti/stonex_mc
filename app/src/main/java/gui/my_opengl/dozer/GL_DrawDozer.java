package gui.my_opengl.dozer;

import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;
import static gui.my_opengl.MyGLRenderer.scale;
import static packexcalib.exca.DataSaved.GL_LAMA;
import static utils.MyTypes.JOYSTICKS;

import android.graphics.Color;

import java.util.List;

import gui.my_opengl.Cylinder;
import gui.my_opengl.GLDrawer;
import gui.my_opengl.GL_Methods;
import gui.my_opengl.My3DActivity;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import gui.my_opengl.exca.BoomsDrawer;
import dxf.Point3D;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;

public class GL_DrawDozer {

    public static void draw(GL11 gl11) {
        if (My3DActivity.glVista3d == 1) {
            BoomsDrawer lama = new BoomsDrawer(
                    GL_LAMA,
                    My_Lama.lamaChiara(),
                    new float[]{coloreEsterno[0], coloreEsterno[1], coloreEsterno[2], 1f},
                    My_Lama.lamaScura(),
                    coloreInterno,
                    My_Lama.lamaContour()
            );
            lama.draw(gl11);
        } else {
            BoomsDrawer lama = new BoomsDrawer(
                    GL_LAMA,
                    My_Lama.lamaChiara(),
                    new float[]{coloreInterno[0], coloreInterno[1], coloreInterno[2], 1f},
                    My_Lama.lamaScura(),
                    coloreEsterno,
                    My_Lama.lamaContour()
            );
            lama.draw(gl11);
        }

        Cylinder attacco = new Cylinder(
                p3tof(GL_LAMA[14]),
                p3tof(GL_LAMA[15]),
                (float) (DataSaved.altezzaLama * 0.15f * scale),
                (float) (DataSaved.altezzaLama * 0.15f * scale),
                coloreInterno,
                8,
                true
        );
        attacco.draw(gl11);

        Cylinder palo1 = new Cylinder(
                p3tof(GL_LAMA[16]),
                p3tof(GL_LAMA[17]),
                0.05f * scale,
                0.04f * scale,
                GL_Methods.parseColorToGL(MyColorClass.colorStick),
                10,
                true
        );
        Cylinder palo2 = new Cylinder(
                p3tof(GL_LAMA[18]),
                p3tof(GL_LAMA[19]),
                0.05f * scale,
                0.04f * scale,
                GL_Methods.parseColorToGL(MyColorClass.colorStick),
                10,
                true
        );
        Cylinder ant1 = new Cylinder(
                p3tof(GL_LAMA[20]),
                p3tof(GL_LAMA[16]),
                0.1f * scale,
                0.09f * scale,
                GL_Methods.parseColorToGL(Color.BLUE),
                10,
                true
        );
        Cylinder ant2 = new Cylinder(
                p3tof(GL_LAMA[21]),
                p3tof(GL_LAMA[18]),
                0.1f * scale,
                0.09f * scale,
                GL_Methods.parseColorToGL(Color.RED),
                10,
                true
        );
        Cylinder cover1 = new Cylinder(
                p3tof(GL_LAMA[17]),
                p3tof(GL_LAMA[22]),
                0.11f * scale,
                0.11f * scale,
                GL_Methods.parseColorToGL(MyColorClass.colorStick),
                4,
                true
        );
        Cylinder cover2 = new Cylinder(
                p3tof(GL_LAMA[19]),
                p3tof(GL_LAMA[23]),
                0.11f * scale,
                0.11f * scale,
                GL_Methods.parseColorToGL(MyColorClass.colorStick),
                4,
                true
        );

        drawBucketEdge(gl11);

        if (DataSaved.isCanOpen != JOYSTICKS) {
            cover1.drawL(gl11, true);
            cover2.drawL(gl11, true);
            palo1.draw(gl11);
            palo2.draw(gl11);
            ant1.draw(gl11);
            ant2.draw(gl11);
        }

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
                        p3tof(GL_LAMA[26]),
                        p3tof(GL_LAMA[29]),
                        0.05f * scale,
                        0.01f * scale,
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl11);
                break;

            case 0:
                spigolo = new Cylinder(
                        p3tof(GL_LAMA[24]),
                        p3tof(GL_LAMA[27]),
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
                        p3tof(GL_LAMA[25]),
                        p3tof(GL_LAMA[28]),
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
                GL_LAMA[30].getX(), GL_LAMA[30].getY(), GL_LAMA[30].getZ(), GL_LAMA[31].getX(), GL_LAMA[31].getY(), GL_LAMA[31].getZ(),
                GL_LAMA[30].getX(), GL_LAMA[30].getY(), GL_LAMA[30].getZ(), GL_LAMA[32].getX(), GL_LAMA[32].getY(), GL_LAMA[32].getZ(),
                GL_LAMA[30].getX(), GL_LAMA[30].getY(), GL_LAMA[30].getZ(), GL_LAMA[34].getX(), GL_LAMA[34].getY(), GL_LAMA[34].getZ(),
                GL_LAMA[30].getX(), GL_LAMA[30].getY(), GL_LAMA[30].getZ(), GL_LAMA[33].getX(), GL_LAMA[33].getY(), GL_LAMA[33].getZ(),
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
                    GL_LAMA[35].getX(), GL_LAMA[35].getY(), GL_LAMA[35].getZ(),
                    GL_LAMA[36].getX(), GL_LAMA[36].getY(), GL_LAMA[36].getZ(),
                    GL_LAMA[37].getX(), GL_LAMA[37].getY(), GL_LAMA[37].getZ()
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
                    GL_LAMA[38].getX(), GL_LAMA[38].getY(), GL_LAMA[38].getZ(),
                    GL_LAMA[39].getX(), GL_LAMA[39].getY(), GL_LAMA[39].getZ(),
                    GL_LAMA[40].getX(), GL_LAMA[40].getY(), GL_LAMA[40].getZ()
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