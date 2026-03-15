package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.coloreAttacco;
import static gui.my_opengl.MyGLRenderer.coloreAttaccoScuro;
import static gui.my_opengl.MyGLRenderer.coloreBoom;
import static gui.my_opengl.MyGLRenderer.coloreBoomScuro;
import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;
import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.exca.My_Benna.cullaIndices;
import static gui.my_opengl.exca.My_Benna.larghezza_attacco;
import static gui.my_opengl.exca.My_Benna.leftSideIndices;
import static gui.my_opengl.exca.My_Benna.raggioPivot;
import static gui.my_opengl.exca.My_Benna.rightSideIndices;
import static gui.my_opengl.exca.My_Frame.R_Cingolo;
import static packexcalib.exca.DataSaved.GL_BENNA;

import android.graphics.Color;

import java.util.List;

import gui.my_opengl.Cylinder;
import gui.my_opengl.GLDrawer;
import gui.my_opengl.GL_Methods;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import dxf.Point3D;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;

public class GL_DrawExca {

    public static void draw(GL11 gl) {

        if (DataSaved.drwaMachieSchema > 0) {
            BennaRenderer bennaRenderer = new BennaRenderer(
                    DataSaved.GL_BENNA,
                    leftSideIndices,
                    rightSideIndices,
                    cullaIndices,
                    coloreInterno,
                    coloreEsterno,
                    0.02f
            );
            bennaRenderer.draw(gl);

            Cylinder ralla = new Cylinder(
                    p3tof(DataSaved.GL_FRAME_BASE[35]),
                    p3tof(DataSaved.GL_FRAME_BASE[36]),
                    0.25f * scale,
                    0.25f * scale,
                    coloreAttaccoScuro,
                    16,
                    false
            );
            ralla.draw(gl);

            Cylinder c2 = new Cylinder(
                    p3tof(DataSaved.GL_FRAME_BASE[37]),
                    p3tof(DataSaved.GL_FRAME_BASE[49]),
                    (float) (R_Cingolo * 0.5 * scale),
                    (float) (R_Cingolo * 0.5 * scale),
                    coloreAttacco,
                    16,
                    true
            );
            Cylinder c3 = new Cylinder(
                    p3tof(DataSaved.GL_FRAME_BASE[38]),
                    p3tof(DataSaved.GL_FRAME_BASE[50]),
                    (float) (R_Cingolo * 0.5 * scale),
                    (float) (R_Cingolo * 0.5 * scale),
                    coloreAttacco,
                    16,
                    true
            );
            Cylinder c4 = new Cylinder(
                    p3tof(DataSaved.GL_FRAME_BASE[39]),
                    p3tof(DataSaved.GL_FRAME_BASE[51]),
                    (float) (R_Cingolo * 0.5 * scale),
                    (float) (R_Cingolo * 0.5 * scale),
                    coloreAttacco,
                    16,
                    true
            );
            Cylinder c5 = new Cylinder(
                    p3tof(DataSaved.GL_FRAME_BASE[40]),
                    p3tof(DataSaved.GL_FRAME_BASE[52]),
                    (float) (R_Cingolo * 0.5 * scale),
                    (float) (R_Cingolo * 0.5 * scale),
                    coloreAttacco,
                    16,
                    true
            );
            c2.draw(gl);
            c3.draw(gl);
            c4.draw(gl);
            c5.draw(gl);

            BoomsDrawer cingoli = new BoomsDrawer(
                    DataSaved.GL_FRAME_BASE,
                    My_Frame.cingoliChiari(),
                    coloreAttacco,
                    My_Frame.cingoliScuri(),
                    coloreAttaccoScuro,
                    My_Frame.bordiCingoli()
            );
            cingoli.draw(gl);

            if (DataSaved.lrTilt != 0) {
                if (DataSaved.isTiltRotator == 1) {
                    Cylinder cyl1 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[0], DataSaved.GL_ATTACCO[1], DataSaved.GL_ATTACCO[2]},
                            new float[]{DataSaved.GL_ATTACCO[3], DataSaved.GL_ATTACCO[4], DataSaved.GL_ATTACCO[5]},
                            0.10f * scale,
                            0.10f * scale,
                            coloreAttacco,
                            16,
                            true
                    );
                    cyl1.draw(gl);

                    Cylinder cyl2 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[6], DataSaved.GL_ATTACCO[7], DataSaved.GL_ATTACCO[8]},
                            new float[]{DataSaved.GL_ATTACCO[9], DataSaved.GL_ATTACCO[10], DataSaved.GL_ATTACCO[11]},
                            0.22f * scale,
                            0.22f * scale,
                            coloreAttacco,
                            16,
                            true
                    );
                    cyl2.draw(gl);

                    Cylinder cyl3 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[12], DataSaved.GL_ATTACCO[13], DataSaved.GL_ATTACCO[14]},
                            new float[]{DataSaved.GL_ATTACCO[15], DataSaved.GL_ATTACCO[16], DataSaved.GL_ATTACCO[17]},
                            larghezza_attacco * 0.75f,
                            ((larghezza_attacco * 0.5f) * 1.15f) * 0.5f,
                            coloreAttacco,
                            12,
                            true
                    );
                    cyl3.draw(gl);

                } else {
                    Cylinder cylinder = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[0], DataSaved.GL_ATTACCO[1], DataSaved.GL_ATTACCO[2]},
                            new float[]{DataSaved.GL_ATTACCO[3], DataSaved.GL_ATTACCO[4], DataSaved.GL_ATTACCO[5]},
                            larghezza_attacco * 0.5f,
                            (larghezza_attacco * 0.5f) * 0.5f,
                            coloreAttacco,
                            12,
                            true
                    );
                    cylinder.draw(gl);

                    Cylinder cylinder1 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[6], DataSaved.GL_ATTACCO[7], DataSaved.GL_ATTACCO[8]},
                            new float[]{DataSaved.GL_ATTACCO[9], DataSaved.GL_ATTACCO[10], DataSaved.GL_ATTACCO[11]},
                            raggioPivot,
                            raggioPivot,
                            coloreAttacco,
                            12,
                            true
                    );
                    cylinder1.draw(gl);

                    Cylinder cylinder2 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[6], DataSaved.GL_ATTACCO[7], DataSaved.GL_ATTACCO[8]},
                            new float[]{DataSaved.GL_ATTACCO[12], DataSaved.GL_ATTACCO[13], DataSaved.GL_ATTACCO[14]},
                            raggioPivot,
                            raggioPivot,
                            coloreAttacco,
                            12,
                            true
                    );
                    cylinder2.draw(gl);

                    Cylinder cylinder3 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[15], DataSaved.GL_ATTACCO[16], DataSaved.GL_ATTACCO[17]},
                            new float[]{DataSaved.GL_ATTACCO[18], DataSaved.GL_ATTACCO[19], DataSaved.GL_ATTACCO[20]},
                            larghezza_attacco * 0.75f,
                            ((larghezza_attacco * 0.5f) * 1.15f) * 0.5f,
                            coloreAttacco,
                            8,
                            true
                    );
                    cylinder3.draw(gl);
                }
            }

            Cylinder boccolaStick = new Cylinder(
                    p3tof(DataSaved.GL_STICK[0]),
                    p3tof(DataSaved.GL_STICK[6]),
                    (float) (DataSaved.L_Stick * 0.025f * scale),
                    (float) (DataSaved.L_Stick * 0.025f * scale),
                    coloreBoom,
                    12,
                    true
            );
            boccolaStick.draw(gl);

            BoomsDrawer stick = new BoomsDrawer(
                    DataSaved.GL_STICK,
                    My_Stick.facceChiare(),
                    coloreBoom,
                    My_Stick.facceScure(),
                    coloreBoomScuro,
                    My_Stick.contorno()
            );
            stick.draw(gl);

            if (DataSaved.lrBoom2 == 0) {
                BoomsDrawer boom1 = new BoomsDrawer(
                        DataSaved.GL_BOOM1,
                        My_Boom1.indici1(),
                        coloreBoom,
                        My_Boom1.indici2(),
                        coloreBoomScuro,
                        My_Boom1.contorno()
                );
                boom1.draw(gl);
            } else {
                BoomsDrawer boom1_2 = new BoomsDrawer(
                        DataSaved.GL_BOOM1_2,
                        My_Boom1_Boom2.facceChiare(),
                        coloreBoom,
                        My_Boom1_Boom2.facceScure(),
                        coloreBoomScuro,
                        My_Boom1_Boom2.contorni()
                );
                boom1_2.draw(gl);
            }

            BoomsDrawer frameBase = new BoomsDrawer(
                    DataSaved.GL_FRAME_BASE,
                    My_Frame.triangoliFrameChiari(),
                    coloreBoom,
                    My_Frame.triangoliFrameScuri(),
                    coloreBoomScuro,
                    My_Frame.bordi()
            );
            frameBase.draw(gl);

            BoomsDrawer cabina = new BoomsDrawer(
                    DataSaved.GL_FRAME_BASE,
                    My_Frame.cabinaChiara(),
                    coloreAttacco,
                    My_Frame.cabinaScura(),
                    coloreAttaccoScuro,
                    My_Frame.bordiCab()
            );
            cabina.draw(gl);

            BoomsDrawer cappello = new BoomsDrawer(
                    DataSaved.GL_FRAME_BASE,
                    My_Frame.zavorraMedia(),
                    coloreBoom,
                    My_Frame.cappello(),
                    coloreBoomScuro,
                    My_Frame.bordiCappello()
            );
            cappello.draw(gl);

            BoomsDrawer minipitch = new BoomsDrawer(
                    DataSaved.GL_FRAME_BASE,
                    My_Frame.triangoliMiniChiari(),
                    coloreBoom,
                    My_Frame.triangoliMiniScuri(),
                    coloreBoomScuro,
                    My_Frame.bordiMiniP()
            );
            minipitch.draw(gl);

            drawBucketEdge(gl);
            drawSnapHelpers(gl);

        } else {
            BennaRenderer bennaRenderer = new BennaRenderer(
                    DataSaved.GL_BENNA,
                    leftSideIndices,
                    rightSideIndices,
                    cullaIndices,
                    coloreInterno,
                    coloreEsterno,
                    0.02f
            );
            bennaRenderer.draw(gl);

            if (DataSaved.lrTilt != 0) {
                if (DataSaved.isTiltRotator == 1) {
                    Cylinder cyl1 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[0], DataSaved.GL_ATTACCO[1], DataSaved.GL_ATTACCO[2]},
                            new float[]{DataSaved.GL_ATTACCO[3], DataSaved.GL_ATTACCO[4], DataSaved.GL_ATTACCO[5]},
                            0.10f * scale,
                            0.10f * scale,
                            coloreAttacco,
                            16,
                            true
                    );
                    cyl1.draw(gl);

                    Cylinder cyl2 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[6], DataSaved.GL_ATTACCO[7], DataSaved.GL_ATTACCO[8]},
                            new float[]{DataSaved.GL_ATTACCO[9], DataSaved.GL_ATTACCO[10], DataSaved.GL_ATTACCO[11]},
                            0.22f * scale,
                            0.22f * scale,
                            coloreAttacco,
                            16,
                            true
                    );
                    cyl2.draw(gl);

                    Cylinder cyl3 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[12], DataSaved.GL_ATTACCO[13], DataSaved.GL_ATTACCO[14]},
                            new float[]{DataSaved.GL_ATTACCO[15], DataSaved.GL_ATTACCO[16], DataSaved.GL_ATTACCO[17]},
                            larghezza_attacco * 0.75f,
                            ((larghezza_attacco * 0.5f) * 1.15f) * 0.5f,
                            coloreAttacco,
                            12,
                            true
                    );
                    cyl3.draw(gl);

                } else {
                    Cylinder cylinder = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[0], DataSaved.GL_ATTACCO[1], DataSaved.GL_ATTACCO[2]},
                            new float[]{DataSaved.GL_ATTACCO[3], DataSaved.GL_ATTACCO[4], DataSaved.GL_ATTACCO[5]},
                            larghezza_attacco * 0.5f,
                            (larghezza_attacco * 0.5f) * 0.5f,
                            coloreAttacco,
                            12,
                            true
                    );
                    cylinder.draw(gl);

                    Cylinder cylinder1 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[6], DataSaved.GL_ATTACCO[7], DataSaved.GL_ATTACCO[8]},
                            new float[]{DataSaved.GL_ATTACCO[9], DataSaved.GL_ATTACCO[10], DataSaved.GL_ATTACCO[11]},
                            raggioPivot,
                            raggioPivot,
                            coloreAttacco,
                            12,
                            true
                    );
                    cylinder1.draw(gl);

                    Cylinder cylinder2 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[6], DataSaved.GL_ATTACCO[7], DataSaved.GL_ATTACCO[8]},
                            new float[]{DataSaved.GL_ATTACCO[12], DataSaved.GL_ATTACCO[13], DataSaved.GL_ATTACCO[14]},
                            raggioPivot,
                            raggioPivot,
                            coloreAttacco,
                            12,
                            true
                    );
                    cylinder2.draw(gl);

                    Cylinder cylinder3 = new Cylinder(
                            new float[]{DataSaved.GL_ATTACCO[15], DataSaved.GL_ATTACCO[16], DataSaved.GL_ATTACCO[17]},
                            new float[]{DataSaved.GL_ATTACCO[18], DataSaved.GL_ATTACCO[19], DataSaved.GL_ATTACCO[20]},
                            larghezza_attacco * 0.75f,
                            ((larghezza_attacco * 0.5f) * 1.15f) * 0.5f,
                            coloreAttacco,
                            8,
                            true
                    );
                    cylinder3.draw(gl);
                }
            }

            Cylinder boccolaStick = new Cylinder(
                    p3tof(DataSaved.GL_STICK[0]),
                    p3tof(DataSaved.GL_STICK[6]),
                    (float) (DataSaved.L_Stick * 0.025f * scale),
                    (float) (DataSaved.L_Stick * 0.025f * scale),
                    coloreBoom,
                    12,
                    true
            );
            boccolaStick.draw(gl);

            BoomsDrawer stick = new BoomsDrawer(
                    DataSaved.GL_STICK,
                    My_Stick.facceChiare(),
                    coloreBoom,
                    My_Stick.facceScure(),
                    coloreBoomScuro,
                    My_Stick.contorno()
            );
            stick.draw(gl);

            if (DataSaved.lrBoom2 == 0) {
                BoomsDrawer boom1 = new BoomsDrawer(
                        DataSaved.GL_BOOM1,
                        My_Boom1.indici1(),
                        coloreBoom,
                        My_Boom1.indici2(),
                        coloreBoomScuro,
                        My_Boom1.contorno()
                );
                boom1.draw(gl);
            } else {
                BoomsDrawer boom1_2 = new BoomsDrawer(
                        DataSaved.GL_BOOM1_2,
                        My_Boom1_Boom2.facceChiare(),
                        coloreBoom,
                        My_Boom1_Boom2.facceScure(),
                        coloreBoomScuro,
                        My_Boom1_Boom2.contorni()
                );
                boom1_2.draw(gl);
            }

            drawBucketEdge(gl);
            drawSnapHelpers(gl);
        }
    }

    private static float[] p3tof(Point3DF point3DF) {
        return new float[]{
                point3DF.getX(),
                point3DF.getY(),
                point3DF.getZ()
        };
    }

    private static void drawBucketEdge(GL11 gl) {
        Cylinder spigolo;
        int colore = Color.BLUE;
        if (DataSaved.isLowerEdge) {
            colore = Color.RED;
        }

        switch (DataSaved.bucketEdge) {
            case -1:
                spigolo = new Cylinder(
                        p3tof(DataSaved.GL_BENNA[24]),
                        p3tof(DataSaved.GL_BENNA[27]),
                        0.05f * scale,
                        0.02f * scale,
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl);
                break;

            case 0:
                spigolo = new Cylinder(
                        p3tof(DataSaved.GL_BENNA[22]),
                        p3tof(DataSaved.GL_BENNA[25]),
                        0.05f * scale,
                        0.01f * scale,
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl);
                break;

            case 1:
                spigolo = new Cylinder(
                        p3tof(DataSaved.GL_BENNA[23]),
                        p3tof(DataSaved.GL_BENNA[26]),
                        0.05f * scale,
                        0.01f * scale,
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl);
                break;
        }
    }

    private static void drawSnapHelpers(GL11 gl) {
        switch (DataSaved.isAutoSnap) {
            case 1:
            case 3:
                if (DataSaved.nearestPoint != null) {
                    drawPointDist(gl, 5f, Color.GREEN, scale);
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
                    GLDrawer.drawSelectedPoly(gl, polyVertices, 5f, Color.GREEN, scale);
                }
                drawLineDist(gl, 5f, Color.GREEN, scale);
                break;
        }
    }

    private static void drawLineDist(GL11 gl, float lineW, int color, float scale) {
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
                    Math.max(1f, lineW * scale)
            );
        } catch (Exception ignored) {
        }
    }

    private static void drawPointDist(GL11 gl, float lineW, int color, float scale) {
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