package gui.my_opengl;

// ===== ColorRamp.java (puoi metterlo dentro GL_Methods se preferisci) =====

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import gui.my_opengl.compat.GL10;
import gui.my_opengl.compat.GL11;

public final class ColorRamp {

    // --- Parametri regolabili ---
    /**
     * Step minimo visibile in metri per la quantizzazione (0 => disattivo).
     */
    public static double MIN_STEP_METERS = 0.10;   // 10 cm (metti 0 per gradiente continuo)
    /**
     * Parametro per remap asinh (metri). 0 => disattivo; usa solo lineare/quant.
     */
    public static double ASINH_K_METERS = 0.0;     // es. 0.20 per enfatizzare i primi cm
    /**
     * Risoluzione della rampa (numero di “campioni” lungo t).
     */
    public static int RAMP_SIZE = 512;             // 256..1024 vanno bene
    /**
     * Filtro texture: true=NEAREST (fasce nette), false=LINEAR (morbido).
     */
    public static boolean USE_NEAREST_FILTER = true;

    private static int rampTexId = 0; // 0 = non creato

    private ColorRamp() {
    }

    // ---------- Mapping z → t in [0..1] assoluto ----------

    private static double clamp01(double x) {
        return (x < 0) ? 0 : ((x > 1) ? 1 : x);
    }

    /**
     * Normalizza in [0..1] con quantizzazione per metri (se abilitata). Assoluto (usa zMin/zMax globali).
     */
    public static double tQuantized(double z, double zMin, double zMax, double minStepMeters) {
        double range = zMax - zMin;
        if (range <= 0) return 0.0;
        double t = (z - zMin) / range;
        t = clamp01(t);
        if (minStepMeters > 0) {
            double epsT = minStepMeters / range;
            if (epsT < 1e-6) epsT = 1e-6;
            t = Math.round(t / epsT) * epsT;
            t = clamp01(t);
        }
        return t;
    }

    /**
     * Remap asinh globale (opzionale) per espandere piccoli Δz. kMeters=0 => disattivo.
     */
    public static double tAsinh(double z, double zMin, double zMax, double kMeters) {
        if (kMeters <= 0) {
            // fallback: lineare
            double range = zMax - zMin;
            return (range <= 0) ? 0.0 : clamp01((z - zMin) / range);
        }
        double range = zMax - zMin;
        if (range <= 0) return 0.0;
        double u = (z - zMin) / kMeters;
        double ur = (range) / kMeters;
        double num = Math.log(u + Math.sqrt(u * u + 1.0));   // asinh(u)
        double denom = Math.log(ur + Math.sqrt(ur * ur + 1.0));  // asinh(ur)
        double t = (denom == 0) ? 0.0 : (num / denom);
        return clamp01(t);
    }

    /**
     * Calcola t finale combinando quantizzazione e/o asinh se richiesti.
     */
    public static double mapZtoT(double z, double zMin, double zMax) {
        double t;
        if (ASINH_K_METERS > 0) {
            t = tAsinh(z, zMin, zMax, ASINH_K_METERS);
            // opzionale: quantizza dopo asinh in metri “local-equivalenti” (di solito non serve)
            if (MIN_STEP_METERS > 0) {
                // Per semplicità: quantizza direttamente t con “eps in t”
                double range = (zMax - zMin);
                double epsT = Math.max(1e-6, MIN_STEP_METERS / Math.max(1e-6, range));
                t = Math.round(t / epsT) * epsT;
                t = clamp01(t);
            }
        } else {
            t = tQuantized(z, zMin, zMax, MIN_STEP_METERS);
        }
        return t;
    }

    // ---------- Palette: Rosso -> Verde -> Blu su t in [0..1] ----------

    public static float[] rgbR_G_B_fromT(double t) {
        t = clamp01(t);
        float r, g, b;
        if (t < 0.5) {
            double u = t / 0.5;
            r = (float) (1.0 - u);
            g = (float) (u);
            b = 0f;
        } else {
            double u = (t - 0.5) / 0.5;
            r = 0f;
            g = (float) (1.0 - u);
            b = (float) (u);
        }
        return new float[]{r, g, b, 1f};
    }

    // ---------- Creazione / gestione texture Nx1 ----------

    /**
     * Assicurati che la rampa esista. Se non c'è, creala.
     */
    public static int ensureRampTexture(GL11 gl) {
        if (rampTexId != 0) return rampTexId;

        int[] ids = new int[1];
        gl.glGenTextures(1, ids, 0);
        rampTexId = ids[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rampTexId);

        // Genera i R,G,B,A della rampa (Nx1)
        final int N = Math.max(2, RAMP_SIZE);
        byte[] rgba = new byte[N * 4];
        for (int i = 0; i < N; i++) {
            double t = (double) i / (double) (N - 1);
            float[] c = rgbR_G_B_fromT(t); // palette assoluta
            rgba[4 * i + 0] = (byte) Math.round(c[0] * 255);
            rgba[4 * i + 1] = (byte) Math.round(c[1] * 255);
            rgba[4 * i + 2] = (byte) Math.round(c[2] * 255);
            rgba[4 * i + 3] = (byte) 255;
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(rgba.length).order(ByteOrder.nativeOrder());
        buf.put(rgba).position(0);

        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, N, 1, 0,
                GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, buf);

        // Filtri
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                USE_NEAREST_FILTER ? GL10.GL_NEAREST : GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                USE_NEAREST_FILTER ? GL10.GL_NEAREST : GL10.GL_LINEAR);

        return rampTexId;
    }

    /**
     * Ricrea/aggiorna la texture (se cambi palette/parametri che impattano la rampa).
     */
    public static void rebuildRampTexture(GL11 gl) {
        if (rampTexId != 0) {
            int[] ids = new int[]{rampTexId};
            gl.glDeleteTextures(1, ids, 0);
            rampTexId = 0;
        }
        ensureRampTexture(gl);
    }
}
