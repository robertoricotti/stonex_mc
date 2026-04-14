package drill_pile.gui;

/**
 * Utility per errore planimetrico (XY) rispetto all'asse foro (A->B).
 * Convenzioni: X=East, Y=North.
 */
public final class PlanError {

    private PlanError() {
    }

    /**
     * Risultato errore planimetrico rispetto all'asse A->B (retta infinita o segmento).
     */
    public static final class Result {
        public final double errE;     // errore East (P - proiezione)
        public final double errN;     // errore North (P - proiezione)
        public final double dist;     // distanza (m)
        public final double t;        // parametro proiezione (retta: (-inf,+inf), segmento: [0..1])
        public final double side;     // segno lato rispetto A->B (cross): >0 un lato, <0 altro lato
        public final double projE;    // punto proiezione sulla linea
        public final double projN;

        public Result(double errE, double errN, double dist, double t, double side, double projE, double projN) {
            this.errE = errE;
            this.errN = errN;
            this.dist = dist;
            this.t = t;
            this.side = side;
            this.projE = projE;
            this.projN = projN;
        }
    }

    /**
     * Errore planimetrico del punto P rispetto all'asse (A->B) in XY.
     *
     * @param clampToSegment se true, la proiezione è clamped sul segmento [A,B]
     */
    public static Result calcPlanErrorToAxisXY(
            double pE, double pN,
            double aE, double aN,
            double bE, double bN,
            boolean clampToSegment
    ) {
        double vx = bE - aE;
        double vy = bN - aN;

        double wx = pE - aE;
        double wy = pN - aN;

        double vv = vx * vx + vy * vy;
        if (vv < 1e-12) {
            // asse degenero -> usa A
            double errE = pE - aE;
            double errN = pN - aN;
            double dist = Math.sqrt(errE * errE + errN * errN);
            return new Result(errE, errN, dist, 0.0, 0.0, aE, aN);
        }

        double t = (wx * vx + wy * vy) / vv;
        if (clampToSegment) {
            if (t < 0.0) t = 0.0;
            else if (t > 1.0) t = 1.0;
        }

        double projE = aE + t * vx;
        double projN = aN + t * vy;

        double errE = pE - projE;
        double errN = pN - projN;
        double dist = Math.sqrt(errE * errE + errN * errN);

        // cross (segno lato): (P-A) x (B-A)
        double side = (wx * vy - wy * vx);

        return new Result(errE, errN, dist, t, side, projE, projN);
    }

    /**
     * Overload: default retta infinita (consigliato per "in asse").
     */
    public static Result calcPlanErrorToAxisXY(
            double pE, double pN,
            double aE, double aN,
            double bE, double bN
    ) {
        return calcPlanErrorToAxisXY(pE, pN, aE, aN, bE, bN, false);
    }
}
