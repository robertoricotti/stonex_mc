package packexcalib.exca;

/**
 * Classe minimale per gestire quaternioni di rotazione.
 * Compatibile con Exca_Quaternion.endPoint().
 */
public class Quaternion {

    private final double q0; // parte scalare
    private final double q1; // X
    private final double q2; // Y
    private final double q3; // Z

    // Costruttore standard
    public Quaternion(double q0, double q1, double q2, double q3) {
        this.q0 = q0;
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
    }

    // Factory da asse (unitario) + angolo
    public static Quaternion fromAxisAngle(double ax, double ay, double az, double angle) {
        double half = angle * 0.5;
        double s = Math.sin(half);
        return new Quaternion(Math.cos(half), ax * s, ay * s, az * s);
    }

    // Moltiplicazione fra quaternioni (this * r)
    public Quaternion multiply(Quaternion r) {
        double w = q0 * r.q0 - q1 * r.q1 - q2 * r.q2 - q3 * r.q3;
        double x = q0 * r.q1 + q1 * r.q0 + q2 * r.q3 - q3 * r.q2;
        double y = q0 * r.q2 - q1 * r.q3 + q2 * r.q0 + q3 * r.q1;
        double z = q0 * r.q3 + q1 * r.q2 - q2 * r.q1 + q3 * r.q0;
        return new Quaternion(w, x, y, z);
    }

    // Coniugato (inverso per rotazioni unitarie)
    public Quaternion getConjugate() {
        return new Quaternion(q0, -q1, -q2, -q3);
    }

    // Getter compatibili con il tuo codice
    public double getQ0() { return q0; }
    public double getQ1() { return q1; }
    public double getQ2() { return q2; }
    public double getQ3() { return q3; }

    // Normalizzazione opzionale (in caso di accumulo numerico)
    public Quaternion normalize() {
        double norm = Math.sqrt(q0*q0 + q1*q1 + q2*q2 + q3*q3);
        return new Quaternion(q0/norm, q1/norm, q2/norm, q3/norm);
    }

    @Override
    public String toString() {
        return String.format("Quaternion[w=%.6f, x=%.6f, y=%.6f, z=%.6f]", q0, q1, q2, q3);
    }
}