package packexcalib.exca;


import org.apache.commons.math3.complex.Quaternion;


public class Exca_Quaternion {
    public static double []endPoint(double[]startXYZ,double pitch,double roll,double len,double hdt) {
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
}
