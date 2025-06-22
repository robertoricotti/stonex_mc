package packexcalib.gnss;


/*
senza Z
 */

public class CircumferenceCenterCalculator {

    // Metodo per calcolare il centro della circonferenza dati tre punti
    public static double[] findCircumferenceCenter(double x1, double y1,
                                                   double x2, double y2,
                                                   double x3, double y3) {
        // Calcola i punti medi tra le coppie di punti
        double midPointX1 = (x1 + x2) / 2;
        double midPointY1 = (y1 + y2) / 2;


        double midPointX2 = (x2 + x3) / 2;
        double midPointY2 = (y2 + y3) / 2;


        // Calcola le pendenze delle rette perpendicolari ai segmenti dei punti medi

        double slope1 = -(x2 - x1) / (y2 - y1);
        double slope2 = -(x3 - x2) / (y3 - y2);

        // Calcola le coordinate x e y del centro della circonferenza
        double centerX = (midPointY2 - midPointY1 + slope1 * midPointX1 - slope2 * midPointX2) / (slope1 - slope2);
        double centerY = slope1 * (centerX - midPointX1) + midPointY1;

        // Calcola la coordinata z del centro della circonferenza usando il punto 1
        double centerZ = ((x1 - centerX) * (x1 - centerX) + (y1 - centerY) * (y1 - centerY) - x1 * x1 - y1 * y1) / 2.0;


 
        return new double[]{centerX, centerY,calculateRadiusAndAngle(centerX,centerY,x3,y3)[0],calculateRadiusAndAngle(centerX,centerY,x3,y3)[1]};
    }

    static double[] calculateRadiusAndAngle(double xC, double yC,
                                            double xP, double yP) {
        // Calcola la lunghezza del raggio
        double radius = Math.sqrt(Math.pow(xP - xC, 2) + Math.pow(yP - yC, 2) );

        // Calcola l'angolazione in radianti tra il punto (xP, yP) e il centro (xC, yC)
        double angleInRadians = Math.atan2(yP - yC, xP - xC);

        // Converte l'angolazione in gradi e assicurati che sia un valore positivo tra 0 e 360 gradi
        double angleInDegrees = Math.toDegrees(angleInRadians);
        if (angleInDegrees < 0) {
            angleInDegrees += 360;
        }

        // Restituisci la lunghezza del raggio e l'angolazione come un array
        return new double[]{radius, angleInDegrees};
    }



}






