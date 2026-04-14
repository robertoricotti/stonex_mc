package packexcalib.exca;


import org.apache.commons.math3.complex.Quaternion;


public class Exca_Quaternion {
    public static double[] endPoint(double[] startXYZ, double pitch, double roll, double len, double hdt) {
        // Coordinate di partenza (X, Y, Z)
        double startX = startXYZ[0];
        double startY = startXYZ[1];
        double startZ = startXYZ[2];


        // Converti gli angoli in radianti
        double pitchRadians = Math.toRadians(roll);
        double rollRadians = Math.toRadians(pitch);
        double yawRadians = Math.toRadians(-hdt);

        // Calcola il quaternione per l'orientamento
        Quaternion orientationQuaternion = new Quaternion(
                Math.cos(yawRadians / 2) * Math.cos(pitchRadians / 2) * Math.cos(rollRadians / 2) + Math.sin(yawRadians / 2) * Math.sin(pitchRadians / 2) * Math.sin(rollRadians / 2),
                Math.cos(yawRadians / 2) * Math.cos(pitchRadians / 2) * Math.sin(rollRadians / 2) - Math.sin(yawRadians / 2) * Math.sin(pitchRadians / 2) * Math.cos(rollRadians / 2),
                Math.cos(yawRadians / 2) * Math.sin(pitchRadians / 2) * Math.cos(rollRadians / 2) + Math.sin(yawRadians / 2) * Math.cos(pitchRadians / 2) * Math.sin(rollRadians / 2),
                Math.sin(yawRadians / 2) * Math.cos(pitchRadians / 2) * Math.cos(rollRadians / 2) - Math.cos(yawRadians / 2) * Math.sin(pitchRadians / 2) * Math.sin(rollRadians / 2)
        );

        // Calcola il vettore di spostamento lungo l'asse Z (lunghezza)
        Quaternion displacementQuaternion = new Quaternion(0, 0, len, 0);

        // Ruota il vettore di spostamento in base al quaternione di orientamento
        Quaternion rotatedDisplacementQuaternion = orientationQuaternion.multiply(displacementQuaternion).multiply(orientationQuaternion.getConjugate());

        // Estrai le coordinate dalla Quaternion risultante
        double endX = startX + rotatedDisplacementQuaternion.getQ1();
        double endY = startY + rotatedDisplacementQuaternion.getQ2();
        double endZ = startZ + rotatedDisplacementQuaternion.getQ3();

        return new double[]{endX, endY, endZ};
    }


    public static Quaternion getOrientationQuaternion(double pitch, double roll, double hdt) {
        double pitchRadians = Math.toRadians(roll);
        double rollRadians = Math.toRadians(pitch);
        double yawRadians = Math.toRadians(-hdt);

        return new Quaternion(
                Math.cos(yawRadians / 2) * Math.cos(pitchRadians / 2) * Math.cos(rollRadians / 2)
                        + Math.sin(yawRadians / 2) * Math.sin(pitchRadians / 2) * Math.sin(rollRadians / 2),

                Math.cos(yawRadians / 2) * Math.cos(pitchRadians / 2) * Math.sin(rollRadians / 2)
                        - Math.sin(yawRadians / 2) * Math.sin(pitchRadians / 2) * Math.cos(rollRadians / 2),

                Math.cos(yawRadians / 2) * Math.sin(pitchRadians / 2) * Math.cos(rollRadians / 2)
                        + Math.sin(yawRadians / 2) * Math.cos(pitchRadians / 2) * Math.sin(rollRadians / 2),

                Math.sin(yawRadians / 2) * Math.cos(pitchRadians / 2) * Math.cos(rollRadians / 2)
                        - Math.cos(yawRadians / 2) * Math.sin(pitchRadians / 2) * Math.sin(rollRadians / 2)
        );
    }

    public static double[] rotateVector(Quaternion q, double x, double y, double z) {
        Quaternion v = new Quaternion(0, x, y, z);
        Quaternion r = q.multiply(v).multiply(q.getConjugate());
        return new double[]{r.getQ1(), r.getQ2(), r.getQ3()};
    }

    public static double[] endPoint_2(double[] startXYZ, double pitch, double roll, double len, double hdt) {
        double[] dir = rotateVector(getOrientationQuaternion(pitch, roll, hdt), 0, len, 0);
        return new double[]{
                startXYZ[0] + dir[0],
                startXYZ[1] + dir[1],
                startXYZ[2] + dir[2]
        };
    }
}
