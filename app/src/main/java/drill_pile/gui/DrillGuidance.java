package drill_pile.gui;

/**
 * Guida pitch/roll (triangoli) in modo robusto.
 *
 * Frame macchina definito da headingDeg:
 * - heading=0 => forward=N, right=E
 *
 * Pitch: positivo quando il BIT è più "in avanti" (forward positivo) rispetto alla testa.
 * Roll:  positivo quando il BIT va a sinistra (right negativo) -> coerente con "testa a destra, bit a sinistra".
 */
public final class DrillGuidance {

    private DrillGuidance() {}

    public static final class Triangles {
        public final boolean up;    // aumenta pitch
        public final boolean right; // aumenta roll
        public final boolean down;  // diminuisce pitch
        public final boolean left;  // diminuisce roll

        public final double mastPitchDeg, mastRollDeg;
        public final double holePitchDeg, holeRollDeg;
        public final double deltaPitchDeg, deltaRollDeg;

        public Triangles(boolean up, boolean right, boolean down, boolean left,
                         double mastPitchDeg, double mastRollDeg,
                         double holePitchDeg, double holeRollDeg,
                         double deltaPitchDeg, double deltaRollDeg) {
            this.up = up;
            this.right = right;
            this.down = down;
            this.left = left;
            this.mastPitchDeg = mastPitchDeg;
            this.mastRollDeg = mastRollDeg;
            this.holePitchDeg = holePitchDeg;
            this.holeRollDeg = holeRollDeg;
            this.deltaPitchDeg = deltaPitchDeg;
            this.deltaRollDeg = deltaRollDeg;
        }
    }

    /**
     * Triangoli per riportare mast pitch/roll in tolleranza rispetto all'asse foro.
     *
     * @param mastHead [E,N,Z]
     * @param mastBit  [E,N,Z]
     * @param holeStart [E,N,Z]
     * @param holeEnd   [E,N,Z]
     * @param headingDeg 0=N, 90=E
     * @param angleTolDeg tolleranza (deg)
     */
    public static Triangles computeTiltTriangles(
            double[] mastHead, double[] mastBit,
            double[] holeStart, double[] holeEnd,
            double headingDeg,
            double angleTolDeg
    ) {
        if (!is3(mastHead) || !is3(mastBit) || !is3(holeStart) || !is3(holeEnd)) {
            return new Triangles(false, false, false, false,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }

        // Se il foro è quasi verticale, pitch/roll target ~0 e l'azimut non è significativo:
        // puoi comunque guidare verso verticalità, ma spesso è meglio spegnere triangoli.
        double holeTilt = tiltDegFromVertical(holeStart, holeEnd);
        final double verticalEpsDeg = 2.0;
        if (!Double.isNaN(holeTilt) && holeTilt < verticalEpsDeg) {
            double[] mastPR = pitchRollDeg(mastHead, mastBit, headingDeg);
            return new Triangles(false, false, false, false,
                    mastPR[0], mastPR[1], 0.0, 0.0, mastPR[0], mastPR[1]);
        }

        double[] mastPR = pitchRollDeg(mastHead, mastBit, headingDeg);
        double[] holePR = pitchRollDeg(holeStart, holeEnd, headingDeg);

        double mastPitch = mastPR[0];
        double mastRoll  = mastPR[1];
        double holePitch = holePR[0];
        double holeRoll  = holePR[1];

        if (Double.isNaN(mastPitch) || Double.isNaN(mastRoll) ||
                Double.isNaN(holePitch) || Double.isNaN(holeRoll)) {
            return new Triangles(false, false, false, false,
                    mastPitch, mastRoll, holePitch, holeRoll,
                    Double.NaN, Double.NaN);
        }

        double dPitch = mastPitch - holePitch;
        double dRoll  = mastRoll  - holeRoll;

        boolean up = false, down = false, left = false, right = false;

        if (Math.abs(dPitch) > angleTolDeg) {
            // mastPitch < holePitch -> devi aumentare pitch (UP)
            if (dPitch < 0) up = true;
            else down = true;
        }

        if (Math.abs(dRoll) > angleTolDeg) {
            // mastRoll < holeRoll -> devi aumentare roll (RIGHT)
            if (dRoll < 0) right = true;
            else left = true;
        }

        return new Triangles(up, right, down, left,
                mastPitch, mastRoll, holePitch, holeRoll, dPitch, dRoll);
    }

    public static Triangles computeTiltTrianglesToTargets(
            double[] mastHead, double[] mastBit,
            double headingDeg,
            double targetPitchDeg,
            double targetRollDeg,
            double angleTolDeg
    ) {
        if (!is3(mastHead) || !is3(mastBit)) {
            return new Triangles(false, false, false, false,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }

        double[] mastPR = pitchRollDeg(mastHead, mastBit, headingDeg);
        double mastPitch = mastPR[0];
        double mastRoll  = mastPR[1];

        if (Double.isNaN(mastPitch) || Double.isNaN(mastRoll)) {
            return new Triangles(false, false, false, false,
                    mastPitch, mastRoll,
                    targetPitchDeg, targetRollDeg,
                    Double.NaN, Double.NaN);
        }

        double dPitch = mastPitch - targetPitchDeg;
        double dRoll  = mastRoll  - targetRollDeg;

        boolean up = false, down = false, left = false, right = false;

        if (Math.abs(dPitch) > angleTolDeg) {
            if (dPitch < 0) up = true;
            else down = true;
        }

        if (Math.abs(dRoll) > angleTolDeg) {
            if (dRoll < 0) right = true;
            else left = true;
        }

        return new Triangles(up, right, down, left,
                mastPitch, mastRoll,
                targetPitchDeg, targetRollDeg,
                dPitch, dRoll);
    }



    // -------------------------
    // Pitch/Roll robusti
    // -------------------------

    /**
     * Calcola pitch/roll nel frame macchina.
     *
     * Usa vert coerente: vert = headZ - bitZ (bit sotto -> vert positivo).
     * Questo evita errori di segno su inclinato (niente Math.abs(dZ)).
     *
     * Ritorna [pitchDeg, rollDeg]
     */
    private static double[] pitchRollDeg(double[] head, double[] bit, double headingDeg) {

        double dE = bit[0] - head[0];
        double dN = bit[1] - head[1];
        double dZ = bit[2] - head[2];

        // "verticale utile": positiva se bit è sotto la testa
        double vert = -dZ; // = headZ - bitZ

        // Se il vettore mast è troppo piccolo, non posso stimare pitch/roll.
// In JET è meglio considerare "in bolla" (0/0) piuttosto che NaN (che spegne tutto).
        double norm2 = dE*dE + dN*dN + dZ*dZ;
        if (norm2 < 1e-10) { // soglia ~ (0.00001 m)^2
            return new double[]{0.0, 0.0};
        }

// Se vert ~ 0, evita divisioni instabili
        if (Math.abs(vert) < 1e-6) {
            vert = (vert >= 0) ? 1e-6 : -1e-6;
        }

        // Ruota XY mondo -> frame macchina (forward/right) usando heading
        double h = Math.toRadians(headingDeg);

        // heading=0 => forward=N, right=E
        double forward =  Math.cos(h) * dN + Math.sin(h) * dE;
        double right   = -Math.sin(h) * dN + Math.cos(h) * dE;

        // Pitch: positivo se forward positivo (bit avanti)
        double pitch = Math.toDegrees(Math.atan2(forward, vert));

        // Roll: positivo se bit va a sinistra (right negativo) => atan2(-right, vert)
        double roll  = Math.toDegrees(Math.atan2(-right, vert));

        return new double[]{pitch, roll};
    }

    private static double tiltDegFromVertical(double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];

        double horiz = Math.sqrt(dx * dx + dy * dy);
        double vert = Math.abs(dz);

        if (horiz < 1e-12 && vert < 1e-12) return Double.NaN;
        return Math.toDegrees(Math.atan2(horiz, vert));
    }

    private static boolean is3(double[] v) {
        return v != null && v.length >= 3
                && !Double.isNaN(v[0]) && !Double.isNaN(v[1]) && !Double.isNaN(v[2]);
    }
}
