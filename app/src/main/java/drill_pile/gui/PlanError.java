package drill_pile.gui;

public  class PlanError {
    /** Errore planimetrico del punto P rispetto all'asse (A->B) considerando solo XY. */
    public static double[] calcPlanErrorToAxisXY(
            double pE, double pN,
            double aE, double aN,
            double bE, double bN
    ) {
        double vx = bE - aE;
        double vy = bN - aN;

        double wx = pE - aE;
        double wy = pN - aN;

        double vv = vx*vx + vy*vy;
        if (vv < 1e-12) {
            // asse degenerato -> usa A
            double errE = pE - aE;
            double errN = pN - aN;
            double dist = Math.sqrt(errE*errE + errN*errN);
            return new double[]{errE, errN, dist};
        }

        // parametro di proiezione sulla linea infinita
        double t = (wx*vx + wy*vy) / vv;

        // se vuoi limitarlo al segmento A-B:
        // t = Math.max(0.0, Math.min(1.0, t));

        double projE = aE + t * vx;
        double projN = aN + t * vy;

        double errE = pE - projE;
        double errN = pN - projN;
        double dist = Math.sqrt(errE*errE + errN*errN);

        return new double[]{errE, errN, dist};
    }

}

