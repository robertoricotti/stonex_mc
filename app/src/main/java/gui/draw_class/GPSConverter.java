package gui.draw_class;

import android.graphics.PointF;

public class GPSConverter {

    private double bucketNord;// posizione attuale centro benna
    private double bucketEast;// posizione attuale centro benna

    private float metersToPixelsScale;// Fattore di scala per mappare distanza a pixel (da verificare se cambiano da metri a feet).

    private double viewCenterX; // Coordinata X del centro della  View.
    private double viewCenterY; // Coordinata Y del centro della  View.

    // imposta le dimensioni della  View. Vedi se serve o meno
    public void setViewDimensions(float viewWidth, float viewHeight) {
        viewCenterX = viewWidth ;
        viewCenterY = viewHeight;
    }


    public void updateReferenceCoordinates(double nMeters, double eMeters) {
        // aggiorna le coordinate della benna da disegnare.
        bucketNord = nMeters;
        bucketEast = eMeters;
    }


    public void setMetersToPixelsScale(float scale) {
        // scala di conversione da metri a pixel (da verificare per feet)
        /*
        // Dimensioni  View
int viewWidth = 1000;
int viewHeight = 1000;

// Dimensione effettiva area in metri
double areaWidthMeters = 100.0;

// Calcola la scala
float defaultScale = (float) (viewWidth / areaWidthMeters);
         */
        metersToPixelsScale = scale;
    }

    // converte le coordinate metriche in coordinate della View.
    public PointF convertToViewCoordinates(double nMeters, double eMeters) {
        // Calcola la posizione relativa in termini di metri rispetto al punto di riferimento.(verificare in feet)
        double relativeN = nMeters - bucketNord;
        double relativeE = eMeters - bucketEast;


        // Converte la posizione relativa in pixel utilizzando il fattore di scala definito.
        float pixelX = (float) (viewCenterX - (relativeE * metersToPixelsScale));
        float pixelY = (float) (viewCenterY + (relativeN * metersToPixelsScale)); // inversione per senso inverso della Y. da verificare

        return new PointF(pixelX, pixelY);
    }
}
