package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.coloreAttacco;
import static gui.my_opengl.MyGLRenderer.coloreAttaccoScuro;
import static gui.my_opengl.MyGLRenderer.coloreBoom;
import static gui.my_opengl.MyGLRenderer.coloreBoomScuro;
import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;
import static gui.my_opengl.exca.My_Benna.cullaIndices;
import static gui.my_opengl.exca.My_Benna.larghezza_attacco;
import static gui.my_opengl.exca.My_Benna.leftSideIndices;
import static gui.my_opengl.exca.My_Benna.raggioPivot;
import static gui.my_opengl.exca.My_Benna.rightSideIndices;
import static gui.my_opengl.exca.My_Frame.R_Cingolo;
import static services.TriangleService.buildScaledFrameForDraw;
import static utils.MyTypes.DREDGE;

import android.graphics.Color;

import gui.my_opengl.Cylinder;
import gui.my_opengl.GL_Methods;
import gui.my_opengl.MyGLRenderer;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import packexcalib.exca.DataSaved;
import packexcalib.exca.DredgeLib;
import packexcalib.exca.ExcavatorLib;

public class GL_DrawExca {
    private static float rs() {
        return MyGLRenderer.currentRenderScale();
    }

    public static void draw(GL11 gl) {
        if (DataSaved.isWL == DREDGE) {
            drawDredge(gl);
            return;
        }

        if (DataSaved.drwaMachieSchema > 0) {
            drawBenna(gl);

            if (My_Pontone.hasPontone()) {
                My_Pontone.draw(gl);
            }

            // Cingoli e ralla sono sempre presenti: il pontone NON li sostituisce.
            // Se Delta_Z_Pontone e valorizzato, diventano piu alti per chiudere
            // il salto verticale tra pontone e sovrastruttura.
            drawCingoliAdaptive(gl);
            drawRallaAdaptive(gl);

            drawAttacco(gl, true);
            drawStick(gl);
            drawBoomStandard(gl);
            drawFrameBase(gl);
            drawCabina(gl);
            drawCappello(gl);
            drawMiniPitch(gl);
            drawBucketEdge(gl);
        } else {
            drawBenna(gl);
            drawAttacco(gl, false);
            drawStick(gl);
            drawBoomStandard(gl);
            drawBucketEdge(gl);
        }
    }

    private static void drawDredge(GL11 gl) {
        ensureDredgeStandardMachineGeometry();

        boolean standardMachineAvailable = hasStandardMachineGeometry();

        if (DataSaved.drwaMachieSchema > 0) {
            if (My_Pontone.hasPontone()) {
                My_Pontone.draw(gl);
            }

            if (standardMachineAvailable) {
                // Per la draga il pontone e solo la barge: cingoli e ralla
                // rimangono SEMPRE disegnati sopra, e vengono stirati in Z
                // secondo DataSaved.Delta_Z_Pontone.
                drawCingoliAdaptive(gl);
                drawRallaAdaptive(gl);

                drawFrameBase(gl);
                drawCabina(gl);
                drawCappello(gl);
                drawMiniPitch(gl);
            }
        }

        // Solo traliccio, fune e grab. Niente cabina/carro fallback qui.
        My_DredgeCrane.draw(gl, false);
    }

    private static void ensureDredgeStandardMachineGeometry() {
        try {
            if (DataSaved.glL_AnchorView == null || DataSaved.glL_AnchorView.length < 3) return;
            if (ExcavatorLib.coordinateDY == null || ExcavatorLib.coordinateDY.length < 3) return;

            // My_Frame.puntiFrame() usa le variabili standard di ExcavatorLib.
            // In modalita DREDGE le sincronizzo con i valori calcolati da DredgeLib,
            // altrimenti GL_FRAME_BASE resta nullo/stale e il renderer cade nel fallback.
            ExcavatorLib.correctPitch = DredgeLib.correctDredgePitch;
            ExcavatorLib.correctRoll = DredgeLib.correctDredgeRoll;
            ExcavatorLib.coordMiniPitch = ExcavatorLib.coordinateDY;

            //DataSaved.GL_FRAME_BASE = buildScaledFrameForDraw(My_Frame.puntiFrame());
        } catch (Exception ignored) {
            // Se qualcosa manca nei dati macchina, il disegno draga continua comunque
            // con pontone/traliccio/fune/tool, senza rompere il rendering.
        }
    }


    private static boolean hasStandardMachineGeometry() {
        return hasFramePoint(60);
    }

    private static boolean hasFramePoint(int index) {
        return DataSaved.GL_FRAME_BASE != null
                && DataSaved.GL_FRAME_BASE.length > index
                && DataSaved.GL_FRAME_BASE[index] != null;
    }

    private static boolean hasFramePoint(Point3DF[] frame, int index) {
        return frame != null && frame.length > index && frame[index] != null;
    }

    private static boolean hasArrayPoint(Point3DF[] points, int index) {
        return points != null && points.length > index && points[index] != null;
    }

    private static boolean hasFloatIndex(float[] values, int index) {
        return values != null && values.length > index;
    }

    private static void drawBenna(GL11 gl) {
        if (!hasArrayPoint(DataSaved.GL_BENNA, 0)) return;

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
    }

    private static void drawRalla(GL11 gl) {
        drawRallaFromFrame(gl, DataSaved.GL_FRAME_BASE);
    }

    private static void drawRallaAdaptive(GL11 gl) {
        drawRallaFromFrame(gl, frameConUndercarriageHeight());
    }

    private static void drawRallaFromFrame(GL11 gl, Point3DF[] frame) {
        if (!hasFramePoint(frame, 36)) return;

        Point3DF top = frame[35];
        Point3DF bottom = frame[36];

        Cylinder ralla = new Cylinder(
                p3tof(top),
                p3tof(bottom),
                0.25f * rs(),
                0.25f * rs(),
                coloreAttaccoScuro,
                16,
                false
        );
        ralla.draw(gl);
    }

    private static void drawCingoli(GL11 gl) {
        drawCingoliFromFrame(gl, DataSaved.GL_FRAME_BASE);
    }

    private static void drawCingoliAdaptive(GL11 gl) {
        drawCingoliFromFrame(gl, frameConUndercarriageHeight());
    }

    private static void drawCingoliFromFrame(GL11 gl, Point3DF[] frame) {
        if (!hasFramePoint(frame, 60)) return;

        float radius = crawlerCylinderRadius(frame);

        Cylinder c2 = new Cylinder(
                p3tof(frame[37]),
                p3tof(frame[49]),
                radius,
                radius,
                coloreAttacco,
                16,
                true
        );
        Cylinder c3 = new Cylinder(
                p3tof(frame[38]),
                p3tof(frame[50]),
                radius,
                radius,
                coloreAttacco,
                16,
                true
        );
        Cylinder c4 = new Cylinder(
                p3tof(frame[39]),
                p3tof(frame[51]),
                radius,
                radius,
                coloreAttacco,
                16,
                true
        );
        Cylinder c5 = new Cylinder(
                p3tof(frame[40]),
                p3tof(frame[52]),
                radius,
                radius,
                coloreAttacco,
                16,
                true
        );
        c2.draw(gl);
        c3.draw(gl);
        c4.draw(gl);
        c5.draw(gl);

        BoomsDrawer cingoli = new BoomsDrawer(
                frame,
                My_Frame.cingoliChiari(),
                coloreAttacco,
                My_Frame.cingoliScuri(),
                coloreAttaccoScuro,
                My_Frame.bordiCingoli()
        );
        cingoli.draw(gl);
    }

    private static Point3DF[] frameConUndercarriageHeight() {
        if (!hasFramePoint(60)) return DataSaved.GL_FRAME_BASE;

        float targetHeight = undercarriageHeightGl();
        if (targetHeight <= 0.001f) return DataSaved.GL_FRAME_BASE;

        Point3DF[] src = DataSaved.GL_FRAME_BASE;
        Point3DF rallaTop = src[35];
        if (rallaTop == null) return src;

        float topZ = rallaTop.getZ();
        float bottomZ = minZ(src, UNDERCARRIAGE_INDICES);
        float currentHeight = topZ - bottomZ;

        if (currentHeight <= 0.001f) return src;

        float scaleZ = targetHeight / currentHeight;

        Point3DF[] out = new Point3DF[src.length];
        System.arraycopy(src, 0, out, 0, src.length);

        /*
         * Delta_Z_Pontone definisce l'altezza TOTALE del blocco cingoli+ralla.
         * Non e un extra da aggiungere all'altezza standard.
         * Punto fisso: cima ralla GL_FRAME_BASE[35].
         * Punti scalati verso il basso: ralla bassa + tutto il carro/cingoli.
         * Questa regola vale sia per EXCAVATOR sia per DREDGE, perche le funzioni
         * adaptive vengono usate in entrambi i rami.
         */
        for (int i : UNDERCARRIAGE_INDICES) {
            if (i >= 0 && i < out.length && out[i] != null) {
                float dzFromTop = topZ - out[i].getZ();
                float newZ = topZ - (dzFromTop * scaleZ);
                out[i] = new Point3DF(out[i].getX(), out[i].getY(), newZ);
            }
        }

        return out;
    }

    private static final int[] UNDERCARRIAGE_INDICES = new int[]{
            36,
            37, 38, 39, 40,
            41, 42, 43, 44,
            45, 46, 47, 48,
            49, 50, 51, 52,
            53, 54, 55, 56,
            57, 58, 59, 60
    };

    private static float undercarriageHeightGl() {
        double h = Math.abs(DataSaved.Delta_Z_Pontone);
        if (Double.isNaN(h) || Double.isInfinite(h)) return 0.0f;
        if (h <= 0.0d) return 0.0f;
        return (float) h * rs();
    }

    private static float minZ(Point3DF[] points, int[] indices) {
        float min = Float.MAX_VALUE;
        if (points == null || indices == null) return min;
        for (int i : indices) {
            if (i >= 0 && i < points.length && points[i] != null) {
                min = Math.min(min, points[i].getZ());
            }
        }
        return min;
    }

    private static float crawlerCylinderRadius(Point3DF[] frame) {
        float fallback = (float) (R_Cingolo * 0.5 * rs());
        float radius = 0.0f;
        radius = Math.max(radius, halfVerticalDistance(frame, 41, 45));
        radius = Math.max(radius, halfVerticalDistance(frame, 42, 46));
        radius = Math.max(radius, halfVerticalDistance(frame, 43, 47));
        radius = Math.max(radius, halfVerticalDistance(frame, 44, 48));
        radius = Math.max(radius, halfVerticalDistance(frame, 53, 54));
        radius = Math.max(radius, halfVerticalDistance(frame, 55, 56));
        radius = Math.max(radius, halfVerticalDistance(frame, 57, 58));
        radius = Math.max(radius, halfVerticalDistance(frame, 59, 60));

        if (radius <= 0.001f) radius = fallback;
        return Math.max(radius, 0.02f * rs());
    }

    private static float halfVerticalDistance(Point3DF[] frame, int a, int b) {
        if (!hasFramePoint(frame, a) || !hasFramePoint(frame, b)) return 0.0f;
        return Math.abs(frame[a].getZ() - frame[b].getZ()) * 0.5f;
    }

    private static void drawAttacco(GL11 gl, boolean forceDraw) {
        if (!forceDraw && DataSaved.lrTilt == 0) return;
        if (!hasFloatIndex(DataSaved.GL_ATTACCO, 20)) return;

        if (DataSaved.isTiltRotator == 1) {
            Cylinder cyl1 = new Cylinder(
                    new float[]{DataSaved.GL_ATTACCO[0], DataSaved.GL_ATTACCO[1], DataSaved.GL_ATTACCO[2]},
                    new float[]{DataSaved.GL_ATTACCO[3], DataSaved.GL_ATTACCO[4], DataSaved.GL_ATTACCO[5]},
                    0.10f * rs(),
                    0.10f * rs(),
                    coloreAttacco,
                    16,
                    true
            );
            cyl1.draw(gl);

            Cylinder cyl2 = new Cylinder(
                    new float[]{DataSaved.GL_ATTACCO[6], DataSaved.GL_ATTACCO[7], DataSaved.GL_ATTACCO[8]},
                    new float[]{DataSaved.GL_ATTACCO[9], DataSaved.GL_ATTACCO[10], DataSaved.GL_ATTACCO[11]},
                    0.22f * rs(),
                    0.22f * rs(),
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

    private static void drawStick(GL11 gl) {
        if (!hasArrayPoint(DataSaved.GL_STICK, 6)) return;

        Cylinder boccolaStick = new Cylinder(
                p3tof(DataSaved.GL_STICK[0]),
                p3tof(DataSaved.GL_STICK[6]),
                (float) (DataSaved.L_Stick * 0.025f * rs()),
                (float) (DataSaved.L_Stick * 0.025f * rs()),
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
    }

    private static void drawBoomStandard(GL11 gl) {
        if (DataSaved.lrBoom2 == 0) {
            if (!hasArrayPoint(DataSaved.GL_BOOM1, 0)) return;

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
            if (!hasArrayPoint(DataSaved.GL_BOOM1_2, 0)) return;

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
    }

    private static void drawFrameBase(GL11 gl) {
        if (!hasFramePoint(9)) return;

        BoomsDrawer frameBase = new BoomsDrawer(
                DataSaved.GL_FRAME_BASE,
                My_Frame.triangoliFrameChiari(),
                coloreBoom,
                My_Frame.triangoliFrameScuri(),
                coloreBoomScuro,
                My_Frame.bordi()
        );
        frameBase.draw(gl);
    }

    private static void drawCabina(GL11 gl) {
        if (!hasFramePoint(17)) return;

        BoomsDrawer cabina = new BoomsDrawer(
                DataSaved.GL_FRAME_BASE,
                My_Frame.cabinaChiara(),
                coloreAttacco,
                My_Frame.cabinaScura(),
                coloreAttaccoScuro,
                My_Frame.bordiCab()
        );
        cabina.draw(gl);
    }

    private static void drawCappello(GL11 gl) {
        if (!hasFramePoint(34)) return;

        BoomsDrawer cappello = new BoomsDrawer(
                DataSaved.GL_FRAME_BASE,
                My_Frame.zavorraMedia(),
                coloreBoom,
                My_Frame.cappello(),
                coloreBoomScuro,
                My_Frame.bordiCappello()
        );
        cappello.draw(gl);
    }

    private static void drawMiniPitch(GL11 gl) {
        if (!hasFramePoint(25)) return;

        BoomsDrawer minipitch = new BoomsDrawer(
                DataSaved.GL_FRAME_BASE,
                My_Frame.triangoliMiniChiari(),
                coloreBoom,
                My_Frame.triangoliMiniScuri(),
                coloreBoomScuro,
                My_Frame.bordiMiniP()
        );
        minipitch.draw(gl);
    }

    private static float[] p3tof(Point3DF point3DF) {
        return new float[]{
                point3DF.getX(),
                point3DF.getY(),
                point3DF.getZ()
        };
    }

    private static void drawBucketEdge(GL11 gl) {
        if (!hasArrayPoint(DataSaved.GL_BENNA, 27)) return;

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
                        0.05f * rs(),
                        0.02f * rs(),
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
                        0.05f * rs(),
                        0.01f * rs(),
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
                        0.05f * rs(),
                        0.01f * rs(),
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl);
                break;
        }
    }
}
