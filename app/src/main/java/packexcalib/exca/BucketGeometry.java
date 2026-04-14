package packexcalib.exca;

import android.util.Log;

public class BucketGeometry {

    /**
     * Calcola le coordinate 3D del centro, dello spigolo sinistro e destro della benna tilt,
     * e la rotazione effettiva rispetto al boom (in gradi).
     */
    public static BucketPoints compute(
            double[] pivotTilt,
            double correctWTilt,  // apertura/chiusura (pitch)
            double correctTilt,   // tilt laterale (roll)
            double yawSensor,     // rototilt (se presente, altrimenti 0)
            double hdt_BOOM,      // heading del boom (gradi, CW)
            double bucketLength,  // lunghezza pivot → centro benna
            double bucketWidth    // larghezza benna (spigolo→spigolo)
    ) {

        // ---- 1️⃣ Calcolo centro benna (lungo asse del tilt)
        double[] bucketCenter = Exca_Quaternion.endPoint(
                pivotTilt,
                correctWTilt,
                correctTilt,
                bucketLength,
                hdt_BOOM + yawSensor
        );

        // ---- 2️⃣ Calcolo orientamento completo benna
        double yaw = Math.toRadians(-(hdt_BOOM + yawSensor));   // CW → negativo
        double pitch = Math.toRadians(correctWTilt);            // apertura
        double roll = Math.toRadians(correctTilt);              // tilt laterale

        Quaternion qYaw = Quaternion.fromAxisAngle(0, 0, 1, yaw);
        Quaternion qPitch = Quaternion.fromAxisAngle(1, 0, 0, pitch);
        Quaternion qRoll = Quaternion.fromAxisAngle(0, 1, 0, roll);

        // Ordine coerente: Yaw → Roll → Pitch

        Quaternion qTotal = qYaw.multiply(qRoll).multiply(qPitch);
        // ---- 3️⃣ Calcolo spigoli laterali (±X locale)
        double halfW = bucketWidth * 0.5;

        Quaternion sideRight = new Quaternion(0, halfW, 0, 0);
        Quaternion sideLeft = new Quaternion(0, -halfW, 0, 0);

        Quaternion rotatedRight = qTotal.multiply(sideRight).multiply(qTotal.getConjugate());
        Quaternion rotatedLeft = qTotal.multiply(sideLeft).multiply(qTotal.getConjugate());

        double[] bucketRight = {
                bucketCenter[0] + rotatedRight.getQ1(),
                bucketCenter[1] + rotatedRight.getQ2(),
                bucketCenter[2] + rotatedRight.getQ3()
        };

        double[] bucketLeft = {
                bucketCenter[0] + rotatedLeft.getQ1(),
                bucketCenter[1] + rotatedLeft.getQ2(),
                bucketCenter[2] + rotatedLeft.getQ3()
        };

        // ---- 4️⃣ Calcolo rotazione effettiva rispetto alla benna verticale (pitch=-90, roll=0)
        Quaternion qRefPitch = Quaternion.fromAxisAngle(1, 0, 0, Math.toRadians(-90));
        Quaternion qRef = qYaw.multiply(qRefPitch);

        Quaternion normalLocal = new Quaternion(0, 0, 0, 1); // Z locale (normale piano benna)

        Quaternion rotatedNormal = qTotal.multiply(normalLocal).multiply(qTotal.getConjugate());
        Quaternion refNormal = qRef.multiply(normalLocal).multiply(qRef.getConjugate());

        double nx = rotatedNormal.getQ1();
        double ny = rotatedNormal.getQ2();
        double nz = rotatedNormal.getQ3();
        double rx = refNormal.getQ1();
        double ry = refNormal.getQ2();
        double rz = refNormal.getQ3();

        double dot = nx * rx + ny * ry + nz * rz;
        dot = Math.max(-1.0, Math.min(1.0, dot)); // limita errore numerico
        double deltaRad = Math.acos(dot);
        double deltaDeg = Math.toDegrees(deltaRad);

        // Segno (dx/sx)
        double sign = Math.signum(rotatedNormal.getQ1()); // asse X positivo = rotazione verso destra
        deltaDeg *= sign;

        // --- Log per debug
        Log.d("BUCKET_ROTATION", String.format(
                "Pitch=%.2f Roll=%.2f -> Rotazione effettiva = %.2f°",
                correctWTilt, correctTilt, deltaDeg));

        // ---- 5️⃣ Restituisci risultato completo
        return new BucketPoints(bucketCenter, bucketLeft, bucketRight, deltaDeg);
    }

    /**
     * Struttura di ritorno con i tre punti principali della benna
     * e la rotazione effettiva rispetto al boom.
     */
    public static class BucketPoints {
        public final double[] center;
        public final double[] left;
        public final double[] right;
        public final double rotationDeg; // ← nuova proprietà

        public BucketPoints(double[] center, double[] left, double[] right, double rotationDeg) {
            this.center = center;
            this.left = left;
            this.right = right;
            this.rotationDeg = rotationDeg;
        }
    }
}
