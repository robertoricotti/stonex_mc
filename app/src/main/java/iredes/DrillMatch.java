package iredes;

public final class DrillMatch {

    public static class MatchStates {
        public final boolean xyInRange;
        public final boolean tiltInRange;
        public final boolean orientationInRange;

        public MatchStates(boolean xyInRange, boolean tiltInRange, boolean orientationInRange) {
            this.xyInRange = xyInRange;
            this.tiltInRange = tiltInRange;
            this.orientationInRange = orientationInRange;
        }
    }

    /**
     * Matching mast vs hole:
     * - xyInRange: distanza planimetrica tra mastBit e holeStart <= XYToll
     * - tiltInRange: |tilt(mast) - tilt(hole)| <= AngleToll
     * - orientationInRange: delta azimut (wrap 0..360) <= AngleToll
     *
     * Convenzioni:
     * X = East, Y = North, Z = Elev
     * Heading/Bearing: 0=N, 90=E, 180=S, 270=W
     * Tilt: 0=verticale, 90=orizzontale
     */
    public static MatchStates matchMastToHole(
            double[] mastHead, double[] mastBit,
            double[] holeStart, double[] holeEnd,
            double XYTollMeters, double angleTollDeg
    ) {
        // Validazione minima
        if (!is3(mastHead) || !is3(mastBit) || !is3(holeStart) || !is3(holeEnd)) {
            return new MatchStates(false, false, false);
        }

        // -------------------------
        // 1) XY match (planimetria)
        // -------------------------
        double dx = mastBit[0] - holeStart[0];
        double dy = mastBit[1] - holeStart[1];
        double distXY = Math.sqrt(dx * dx + dy * dy);
        boolean xyInRange = distXY <= XYTollMeters;

        // -------------------------
        // 2) Tilt match (verticale)
        // -------------------------
        double mastTilt = tiltDegFromVertical(mastHead, mastBit);
        double holeTilt = tiltDegFromVertical(holeStart, holeEnd);
        boolean tiltInRange = !Double.isNaN(mastTilt) && !Double.isNaN(holeTilt)
                && Math.abs(mastTilt - holeTilt) <= angleTollDeg;

        // -------------------------
        // 3) Orientation match (bearing)
        // -------------------------
        double mastBearing = bearingDegN0E90(mastHead[0], mastHead[1], mastBit[0], mastBit[1]);
        double holeBearing = bearingDegN0E90(holeStart[0], holeStart[1], holeEnd[0], holeEnd[1]);

        boolean orientationInRange = !Double.isNaN(mastBearing) && !Double.isNaN(holeBearing)
                && angleDiffDeg(mastBearing, holeBearing) <= angleTollDeg;

        return new MatchStates(xyInRange, tiltInRange, orientationInRange);
    }

    // -------------------------
    // Helpers
    // -------------------------

    private static boolean is3(double[] v) {
        return v != null && v.length >= 3
                && !Double.isNaN(v[0]) && !Double.isNaN(v[1]) && !Double.isNaN(v[2]);
    }

    /** Bearing: 0=N, 90=E (X=E, Y=N). */
    private static double bearingDegN0E90(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1; // East
        double dy = y2 - y1; // North
        if (dx == 0.0 && dy == 0.0) return Double.NaN;

        double deg = Math.toDegrees(Math.atan2(dx, dy)); // NOTA: dx,dy
        if (deg < 0) deg += 360.0;
        return deg;
    }

    /** Tilt rispetto alla verticale: 0=verticale, 90=orizzontale. */
    private static double tiltDegFromVertical(double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];

        double horiz = Math.sqrt(dx * dx + dy * dy);
        double vert = Math.abs(dz);

        if (horiz == 0.0 && vert == 0.0) return Double.NaN;
        return Math.toDegrees(Math.atan2(horiz, vert));
    }

    /** Differenza angolare minima (0..180) tra due angoli in gradi (0..360). */
    private static double angleDiffDeg(double a, double b) {
        double d = Math.abs(a - b) % 360.0;
        return d > 180.0 ? 360.0 - d : d;
    }
}
