package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.coloreAttacco;
import static gui.my_opengl.MyGLRenderer.coloreAttaccoScuro;
import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.Sensors_Decoder_Dredge.Angolo_Slew_Dredge;
import static utils.MyTypes.DREDGE;
import static utils.MyTypes.EXCAVATOR;

import android.opengl.GLES20;

import gui.my_opengl.MyGLRenderer;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import packexcalib.exca.DataSaved;
import packexcalib.exca.DredgeLib;

public class My_Pontone {
    private static final double DEFAULT_SPESSORE_PONTONE = 2.0d;

    /*
     * Alpha del pontone.
     * 1.00 = opaco
     * 0.45 = semitrasparente
     */
    private static final float PONTONE_ALPHA = 0.45f;

    private static float rs() {
        return MyGLRenderer.currentRenderScale();
    }

    public static boolean hasPontone() {
        return DataSaved.Lunghezza_Pontone > 0.0d && DataSaved.Larghezza_Pontone > 0.0d;
    }

    public static void draw(GL11 gl) {
        if (!hasPontone()) return;

        Point3DF[] puntiPontone = null;

        if (DataSaved.isWL == DREDGE || DataSaved.isWL == EXCAVATOR) {
            puntiPontone = puntiDaDredgeLib();
        }

        if (puntiPontone == null) {
            puntiPontone = puntiDaFrameEscavatore();
        }

        if (puntiPontone == null) return;

        /*
         * Non modifichiamo i colori globali del renderer.
         * Cloniamo solo i colori del pontone e cambiamo l'alpha.
         */
        float[] pontoneChiaro = withAlpha(coloreAttacco, PONTONE_ALPHA);
        float[] pontoneScuro = withAlpha(coloreAttaccoScuro, PONTONE_ALPHA);

        /*
         * Il blend può essere già abilitato nel renderer, ma lo abilitiamo qui
         * per sicurezza. Importante: mentre disegniamo il pontone trasparente
         * disabilitiamo la scrittura nel depth buffer, altrimenti il pontone
         * rischia di "tagliare" oggetti disegnati dopo.
         */
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDepthMask(false);

        try {
            BoomsDrawer pontone = new BoomsDrawer(
                    puntiPontone,
                    facceChiare(),
                    pontoneChiaro,
                    facceScure(),
                    pontoneScuro,
                    bordi()
            );
            pontone.draw(gl);
        } finally {
            /*
             * Ripristino immediato: tutto il resto della macchina/terreno
             * continua a disegnare con depth write normale.
             */
            GLES20.glDepthMask(true);
        }
    }

    private static Point3DF[] puntiDaDredgeLib() {
        if (!isValid(DredgeLib.spigolo_Asx)
                || !isValid(DredgeLib.spigolo_Adx)
                || !isValid(DredgeLib.spigolo_Bdx)
                || !isValid(DredgeLib.spigolo_Bsx)
                || !isValid(DredgeLib.spigolo_Asx_BOTTOM)
                || !isValid(DredgeLib.spigolo_Adx_BOTTOM)
                || !isValid(DredgeLib.spigolo_Bdx_BOTTOM)
                || !isValid(DredgeLib.spigolo_Bsx_BOTTOM)) {
            return null;
        }

        return new Point3DF[]{
                pTransform(DredgeLib.spigolo_Asx, DataSaved.glL_AnchorView, rs()),
                pTransform(DredgeLib.spigolo_Adx, DataSaved.glL_AnchorView, rs()),
                pTransform(DredgeLib.spigolo_Bdx, DataSaved.glL_AnchorView, rs()),
                pTransform(DredgeLib.spigolo_Bsx, DataSaved.glL_AnchorView, rs()),
                pTransform(DredgeLib.spigolo_Asx_BOTTOM, DataSaved.glL_AnchorView, rs()),
                pTransform(DredgeLib.spigolo_Adx_BOTTOM, DataSaved.glL_AnchorView, rs()),
                pTransform(DredgeLib.spigolo_Bdx_BOTTOM, DataSaved.glL_AnchorView, rs()),
                pTransform(DredgeLib.spigolo_Bsx_BOTTOM, DataSaved.glL_AnchorView, rs())
        };
    }

    private static Point3DF[] puntiDaFrameEscavatore() {
        if (DataSaved.GL_FRAME_BASE == null || DataSaved.GL_FRAME_BASE.length <= 49) return null;
        if (DataSaved.GL_FRAME_BASE[35] == null || DataSaved.GL_FRAME_BASE[36] == null
                || DataSaved.GL_FRAME_BASE[37] == null || DataSaved.GL_FRAME_BASE[38] == null
                || DataSaved.GL_FRAME_BASE[49] == null) {
            return null;
        }

        Point3DF centroRalla = DataSaved.GL_FRAME_BASE[36];

        Point3DF asseX = DataSaved.GL_FRAME_BASE[49].subtract(DataSaved.GL_FRAME_BASE[37]).normalize();
        if (asseX.length() == 0f) {
            asseX = new Point3DF(1f, 0f, 0f);
        }

        Point3DF asseY = DataSaved.GL_FRAME_BASE[38].subtract(DataSaved.GL_FRAME_BASE[37]).normalize();
        if (asseY.length() == 0f) {
            asseY = new Point3DF(0f, -1f, 0f);
        }

        if (Angolo_Slew_Dredge != 0.0d) {
            asseX = rotateZ(asseX, -Angolo_Slew_Dredge).normalize();
            asseY = rotateZ(asseY, -Angolo_Slew_Dredge).normalize();
        }

        Point3DF asseZ = DataSaved.GL_FRAME_BASE[35].subtract(DataSaved.GL_FRAME_BASE[36]).normalize();
        if (asseZ.length() == 0f) {
            asseZ = new Point3DF(0f, 0f, 1f);
        }

        float scala = rs();
        float dx = (float) (DataSaved.Delta_X_Pontone * scala);
        float dy = (float) (DataSaved.Delta_Y_Pontone * scala);
        float lunghezza = (float) (DataSaved.Lunghezza_Pontone * scala);
        float larghezza = (float) (DataSaved.Larghezza_Pontone * scala);
        float spessore = (float) (DEFAULT_SPESSORE_PONTONE * scala);

        Point3DF topEdge = centroRalla.add(asseY.scale(dy));
        Point3DF asx = topEdge.add(asseX.scale(dx));
        Point3DF adx = asx.add(asseX.scale(larghezza));
        Point3DF bdx = adx.subtract(asseY.scale(lunghezza));
        Point3DF bsx = asx.subtract(asseY.scale(lunghezza));

        Point3DF verticalDown = asseZ.scale(-spessore);

        return new Point3DF[]{
                asx,
                adx,
                bdx,
                bsx,
                asx.add(verticalDown),
                adx.add(verticalDown),
                bdx.add(verticalDown),
                bsx.add(verticalDown)
        };
    }

    private static Point3DF rotateZ(Point3DF p, double degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        return new Point3DF(
                (float) (p.getX() * cos - p.getY() * sin),
                (float) (p.getX() * sin + p.getY() * cos),
                p.getZ()
        );
    }

    private static boolean isValid(double[] p) {
        return p != null
                && p.length >= 3
                && (Math.abs(p[0]) > 0.000001d
                || Math.abs(p[1]) > 0.000001d
                || Math.abs(p[2]) > 0.000001d);
    }

    private static short[] facceChiare() {
        return new short[]{
                0, 1, 2,
                2, 3, 0,
                0, 4, 5,
                5, 1, 0,
                1, 5, 6,
                6, 2, 1
        };
    }

    private static short[] facceScure() {
        return new short[]{
                3, 2, 6,
                6, 7, 3,
                0, 3, 7,
                7, 4, 0,
                4, 7, 6,
                6, 5, 4
        };
    }

    private static short[] bordi() {
        return new short[]{
                0, 1,
                1, 2,
                2, 3,
                3, 0,
                4, 5,
                5, 6,
                6, 7,
                7, 4,
                0, 4,
                1, 5,
                2, 6,
                3, 7
        };
    }

    private static float[] withAlpha(float[] color, float alpha) {
        if (color == null || color.length < 4) {
            return new float[]{0.5f, 0.5f, 0.5f, alpha};
        }

        float[] out = color.clone();
        out[3] = alpha;
        return out;
    }
}
