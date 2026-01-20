package drill_pile.gui;

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
     * Calcola quali triangoli mostrare per riportare mast pitch/roll
     * in tolleranza rispetto al foro (start->end), nel frame macchina.
     *
     * headingDeg: 0=N, 90=E
     * angleTollDeg: tolleranza in gradi (stessa per pitch e roll)
     *
     * Triangoli:
     * - UP    = aumenta pitch (piu' "verso cabina" / bit piu' avanti)
     * - DOWN  = diminuisce pitch
     * - RIGHT = aumenta roll (testa piu' a destra, bit piu' a sinistra)
     * - LEFT  = diminuisce roll
     */
    public static Triangles computeTiltTriangles(
            double[] mastHead, double[] mastBit,
            double[] holeStart, double[] holeEnd,
            double headingDeg,
            double angleTollDeg
    ) {
        double[] mastPR = pitchRollDeg(mastHead, mastBit, headingDeg);
        double[] holePR = pitchRollDeg(holeStart, holeEnd, headingDeg);

        double mastPitch = mastPR[0];
        double mastRoll  = mastPR[1];
        double holePitch = holePR[0];
        double holeRoll  = holePR[1];

        // Se impossibile calcolare (es. dz ~ 0), niente triangoli
        if (Double.isNaN(mastPitch) || Double.isNaN(mastRoll) ||
                Double.isNaN(holePitch) || Double.isNaN(holeRoll)) {
            return new Triangles(false,false,false,false,
                    mastPitch, mastRoll, holePitch, holeRoll,
                    Double.NaN, Double.NaN);
        }

        double dPitch = mastPitch - holePitch;
        double dRoll  = mastRoll  - holeRoll;

        boolean up=false, down=false, left=false, right=false;

        // Pitch: se mastPitch troppo basso -> devi aumentare pitch (UP)
        if (Math.abs(dPitch) > angleTollDeg) {
            if (dPitch < 0) up = true;   // mast < hole -> aumenta pitch
            else down = true;            // mast > hole -> diminuisci pitch
        }

        // Roll: se mastRoll troppo basso -> devi aumentare roll (RIGHT)
        if (Math.abs(dRoll) > angleTollDeg) {
            if (dRoll < 0) right = true; // mast < hole -> aumenta roll (testa più a destra)
            else left = true;            // mast > hole -> diminuisci roll
        }

        return new Triangles(up, right, down, left,
                mastPitch, mastRoll, holePitch, holeRoll, dPitch, dRoll);
    }

    /**
     * Calcola pitch/roll nel frame macchina.
     * Pitch > 0 quando il vettore head->bit punta "forward" (in avanti).
     * Roll  > 0 quando il vettore head->bit punta "left" (bit a sinistra) -> coerente con tua definizione:
     *         roll positivo = head a destra e bit a sinistra.
     *
     * Ritorna [pitchDeg, rollDeg]
     */
    private static double[] pitchRollDeg(double[] a, double[] b, double headingDeg) {
        if (!is3(a) || !is3(b)) return new double[]{Double.NaN, Double.NaN};

        double dE = b[0] - a[0]; // East
        double dN = b[1] - a[1]; // North
        double dZ = b[2] - a[2]; // Elev

        // componente verticale (usiamo abs: tilt rispetto alla verticale)
        double vert = Math.abs(dZ);
        if (vert < 1e-6 && (Math.abs(dE) > 1e-6 || Math.abs(dN) > 1e-6)) {
            // quasi orizzontale -> pitch/roll molto grandi, ma calcolabili
            vert = 1e-6;
        } else if (vert < 1e-6) {
            return new double[]{Double.NaN, Double.NaN};
        }

        // Ruota (dE,dN) dal mondo al frame macchina:
        // heading=0 => forward=N, right=E
        double h = Math.toRadians(headingDeg);
        double forward =  Math.cos(h) * dN + Math.sin(h) * dE;
        double right   = -Math.sin(h) * dN + Math.cos(h) * dE;

        // Pitch: positivo se forward positivo (bit avanti)
        double pitch = Math.toDegrees(Math.atan2(forward, vert));

        // Roll: vuoi roll positivo quando bit va a sinistra (right negativo)
        // quindi invertiamo il segno di "right"
        double roll = Math.toDegrees(Math.atan2(-right, vert));

        return new double[]{pitch, roll};
    }

    private static boolean is3(double[] v) {
        return v != null && v.length >= 3
                && !Double.isNaN(v[0]) && !Double.isNaN(v[1]) && !Double.isNaN(v[2]);
    }
}

