package iredes;

import drill_pile.gui.PlanError;

/**
 * Match mast vs hole (professionale e robusto):
 * - XY: distanza del BIT dall'ASSE foro in XY (retta). Fallback su head se asse degenero.
 * - Tilt: confronto tilt rispetto verticale (0=verticale, 90=orizzontale).
 * - Orientation: confronto bearing in XY (0=N, 90=E) SOLO se il foro non è quasi verticale.
 * <p>
 * Convenzioni: X=East, Y=North, Z=Elev
 */
public final class DrillMatch {

    private DrillMatch() {
    }

    public static final class MatchStates {
        public final boolean xyInRange;
        public final boolean tiltInRange;
        public final boolean orientationInRange;

        public final double distXY;        // metri
        public final double mastTiltDeg;   // 0..90
        public final double holeTiltDeg;   // 0..90
        public final double mastBearingDeg; // 0..360 (NaN se non definito)
        public final double holeBearingDeg; // 0..360 (NaN se non definito)
        public final double dBearingDeg;    // 0..180 (NaN se non definito)

        public MatchStates(boolean xyInRange, boolean tiltInRange, boolean orientationInRange,
                           double distXY, double mastTiltDeg, double holeTiltDeg,
                           double mastBearingDeg, double holeBearingDeg, double dBearingDeg) {
            this.xyInRange = xyInRange;
            this.tiltInRange = tiltInRange;
            this.orientationInRange = orientationInRange;

            this.distXY = distXY;
            this.mastTiltDeg = mastTiltDeg;
            this.holeTiltDeg = holeTiltDeg;
            this.mastBearingDeg = mastBearingDeg;
            this.holeBearingDeg = holeBearingDeg;
            this.dBearingDeg = dBearingDeg;
        }
    }

    /**
     * @param mastHead  [E,N,Z] testa mast
     * @param mastBit   [E,N,Z] bit
     * @param holeStart [E,N,Z] testa palo
     * @param holeEnd   [E,N,Z] fondo palo
     */
    public static MatchStates matchMastToHole(
            double[] mastHead, double[] mastBit,
            double[] holeStart, double[] holeEnd,
            double xyTolMeters,
            double angleTolDeg,
            double hdtTolDeg
    ) {
        // Validazione
        if (!is3(mastHead) || !is3(mastBit) || !is3(holeStart) || !is3(holeEnd)) {
            return new MatchStates(false, false, false,
                    Double.NaN, Double.NaN, Double.NaN,
                    Double.NaN, Double.NaN, Double.NaN);
        }

        // -------------------------
        // 1) XY: bit -> asse foro (XY)
        // -------------------------
        double distXY = computeBitToHoleAxisDistXY(mastBit, holeStart, holeEnd);
        boolean xyInRange = !Double.isNaN(distXY) && distXY <= xyTolMeters;

        // -------------------------
        // 2) Tilt: confronto tilt rispetto verticale
        // -------------------------
        double mastTilt = tiltDegFromVertical(mastHead, mastBit);
        double holeTilt = tiltDegFromVertical(holeStart, holeEnd);

        boolean tiltInRange =
                !Double.isNaN(mastTilt) && !Double.isNaN(holeTilt)
                        && Math.abs(mastTilt - holeTilt) <= angleTolDeg;

        // -------------------------
        // 3) Orientation: bearing XY solo se foro NON quasi verticale
        // -------------------------
        final double verticalEpsDeg = 2.0; // sotto 2° azimut non è significativo
        double mastBearing = Double.NaN;
        double holeBearing = Double.NaN;
        double dBearing = Double.NaN;

        boolean orientationInRange;

        if (Double.isNaN(holeTilt) || holeTilt < verticalEpsDeg) {
            // palo quasi verticale -> ignora orientamento
            orientationInRange = true;
        } else {
            mastBearing = bearingDegN0E90(mastHead[0], mastHead[1], mastBit[0], mastBit[1]);
            holeBearing = bearingDegN0E90(holeStart[0], holeStart[1], holeEnd[0], holeEnd[1]);

            if (!Double.isNaN(mastBearing) && !Double.isNaN(holeBearing)) {
                dBearing = angleDiffDeg(mastBearing, holeBearing);
                orientationInRange = dBearing <= hdtTolDeg;
            } else {
                orientationInRange = false;
            }
        }

        return new MatchStates(
                xyInRange, tiltInRange, orientationInRange,
                distXY, mastTilt, holeTilt,
                mastBearing, holeBearing, dBearing
        );
    }

    // -------------------------
    // Helpers
    // -------------------------

    private static boolean is3(double[] v) {
        return v != null && v.length >= 3
                && !Double.isNaN(v[0]) && !Double.isNaN(v[1]) && !Double.isNaN(v[2]);
    }

    /**
     * Distanza bit -> asse foro in XY (retta). Fallback su distanza da holeStart se asse degenero.
     */
    private static double computeBitToHoleAxisDistXY(double[] mastBit, double[] holeStart, double[] holeEnd) {
        double vx = holeEnd[0] - holeStart[0];
        double vy = holeEnd[1] - holeStart[1];
        double vv = vx * vx + vy * vy;

        if (vv < 1e-12) {
            // asse in pianta degenero (verticale o endXY=headXY): fallback punto->head
            double dx = mastBit[0] - holeStart[0];
            double dy = mastBit[1] - holeStart[1];
            return Math.sqrt(dx * dx + dy * dy);
        }

        PlanError.Result pe = PlanError.calcPlanErrorToAxisXY(
                mastBit[0], mastBit[1],
                holeStart[0], holeStart[1],
                holeEnd[0], holeEnd[1],
                false // retta infinita consigliata
        );
        return pe.dist;
    }

    /**
     * Bearing: 0=N, 90=E (X=E, Y=N).
     */
    private static double bearingDegN0E90(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1; // East
        double dy = y2 - y1; // North
        if (Math.abs(dx) < 1e-12 && Math.abs(dy) < 1e-12) return Double.NaN;

        double deg = Math.toDegrees(Math.atan2(dx, dy)); // atan2(E, N)
        if (deg < 0) deg += 360.0;
        return deg;
    }

    /**
     * Tilt rispetto verticale: 0=verticale, 90=orizzontale.
     */
    private static double tiltDegFromVertical(double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];

        double horiz = Math.sqrt(dx * dx + dy * dy);
        double vert = Math.abs(dz);

        if (horiz < 1e-12 && vert < 1e-12) return Double.NaN;
        return Math.toDegrees(Math.atan2(horiz, vert));
    }

    public static double tiltDegFromVerticalPublic(double[] a, double[] b) {
        return tiltDegFromVertical(a, b);
    }

    /**
     * Differenza angolare minima 0..180 tra due angoli 0..360.
     */
    private static double angleDiffDeg(double a, double b) {
        double d = Math.abs(a - b) % 360.0;
        return d > 180.0 ? 360.0 - d : d;
    }
}
