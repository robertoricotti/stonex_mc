package packexcalib.exca;

public class RodLocationLR {
    /**
     * Calcola le coordinate del punto di destinazione (p2) rispetto al punto iniziale (p1).
     *
     * @param location Le coordinate del punto iniziale (p1).
     * @param length   La lunghezza del segmento.
     * @param yaw      L'angolo di yaw (heading) in gradi.
     * @param pitch    L'angolo di pitch in gradi.
     * @param roll     L'angolo di roll in gradi.
     * @return Le coordinate del punto di destinazione (p2) rispetto a p1.
     */

    public static double[] rodloc(double[] location, double pitch, double roll, double length, double yaw) {
        pitch = pitch + 90;
        roll = -90 + roll;
        yaw = -yaw;

        double ryaw = Math.toRadians(yaw);
        double rpitch = Math.toRadians(pitch);
        double rroll = Math.toRadians(roll);

        double[] x = {1, 0, 0};
        double[] y = {0, 1, 0};
        double[] z = {0, 0, 1};

        double[] yprime = rotatearound(x, z, ryaw);
        double[] xprime = rotatearound(y, z, ryaw);


        double[] x2prime = rotatearound(xprime, yprime, rpitch);

        double[] z2prime = rotatearound(z, yprime, rpitch);


        double[] z3prime = rotatearound(z2prime, x2prime, rroll);

        double[] rotend = {length * z3prime[0], length * z3prime[1], length * z3prime[2]};

        double[] result = new double[3];
        result[0] = location[0] + rotend[0];
        result[1] = location[1] + rotend[1];
        result[2] = location[2] + rotend[2];
        return result;


    }


    /**
     * Moltiplica un vettore per una matrice.
     *
     * @param offset Il vettore dell'offset.
     * @param rotate La matrice di rotazione.
     * @return Il nuovo vettore di offset.
     */
    public static double[] vmmult(double[] offset, double[][] rotate) {
        double[] result = new double[3];
        for (int i = 0; i < 3; i++) {
            result[i] = xmult(offset, rotate[i]);
        }
        return result;
    }

    /**
     * Calcola il prodotto scalare tra due vettori.
     *
     * @param col Il primo vettore.
     * @param row Il secondo vettore.
     * @return Il prodotto scalare.
     */
    public static double xmult(double[] col, double[] row) {
        double result = 0.0;
        for (int i = 0; i < 3; i++) {
            result += col[i] * row[i];
        }
        return result;
    }

    /**
     * Ruota un punto intorno a un vettore proiettato dall'origine.
     *
     * @param point Il punto da ruotare.
     * @param vec   Il vettore intorno al quale ruotare.
     * @param angle L'angolo (in radianti) di rotazione.
     * @return La nuova posizione del punto.
     */
    public static double[] rotatearound(double[] point, double[] vec, double angle) {
        double[][] rotmat = setuprotationmatrix(angle, vec);
        return vmmult(point, rotmat);
    }

    /**
     * Imposta una matrice di rotazione.
     *
     * @param angle L'angolo di rotazione.
     * @param vec   Il vettore intorno al quale ruotare.
     * @return La matrice di rotazione.
     */
    public static double[][] setuprotationmatrix(double angle, double[] vec) {
        double u = vec[0];
        double v = vec[1];
        double w = vec[2];
        double L = u * u + v * v + w * w;
        double u2 = u * u;
        double v2 = v * v;
        double w2 = w * w;

        double[][] rotmat = new double[3][3];
        rotmat[0][0] = (u2 + (v2 + w2) * Math.cos(angle)) / L;
        rotmat[0][1] = (u * v * (1 - Math.cos(angle)) - w * Math.sqrt(L) * Math.sin(angle)) / L;
        rotmat[0][2] = (u * w * (1 - Math.cos(angle)) + v * Math.sqrt(L) * Math.sin(angle)) / L;

        rotmat[1][0] = (u * v * (1 - Math.cos(angle)) + w * Math.sqrt(L) * Math.sin(angle)) / L;
        rotmat[1][1] = (v2 + (u2 + w2) * Math.cos(angle)) / L;
        rotmat[1][2] = (v * w * (1 - Math.cos(angle)) - u * Math.sqrt(L) * Math.sin(angle)) / L;

        rotmat[2][0] = (u * w * (1 - Math.cos(angle)) - v * Math.sqrt(L) * Math.sin(angle)) / L;
        rotmat[2][1] = (v * w * (1 - Math.cos(angle)) + u * Math.sqrt(L) * Math.sin(angle)) / L;
        rotmat[2][2] = (w2 + (u2 + v2) * Math.cos(angle)) / L;

        return rotmat;
    }
}
