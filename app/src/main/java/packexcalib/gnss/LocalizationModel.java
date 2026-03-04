package packexcalib.gnss;

/**
 * Interfaccia semplice per i modelli di localizzazione.
 * Implementazioni devono essere thread-safe e leggere il file di definizione una sola volta.
 */
public interface LocalizationModel {

    /**
     * Trasforma Lat,Lon,Height(ellipsoidal) -> X,Y,Z locali.
     * Questo metodo NON deve allocare oggetti per chiamata ad alta frequenza.
     *
     * @param lat latitudine in gradi
     * @param lon longitudine in gradi
     * @param h   quota ellissoidale in metri
     * @param out array double di lunghezza >=3 in cui vengono scritti [X,Y,Z]
     */
    void toLocalFast(double lat, double lon, double h, double[] out);

    /**
     * Trasforma X,Y,Z locali -> Lat,Lon,Height(ellipsoidal).
     *
     * @param E  Est locale
     * @param N  Nord locale
     * @param H  quota locale (come definita dal modello)
     * @param out array double di lunghezza >=3 in cui vengono scritti [lat,lon,hEll]
     */
    void toGeoFast(double E, double N, double H, double[] out);

    /**
     * Comodità che ritorna un nuovo array (può allocare). Usalo solo se non sei nel nodo critico.
     */
    default double[] toLocal(double lat, double lon, double h) {
        double[] out = new double[3];
        toLocalFast(lat, lon, h, out);
        return out;
    }

    /**
     * NUOVO:
     * out[0..2]=XYZ locali, out[3]=delta (gradi) da sommare all’HDT true per ottenere heading locale.
     */
    default void toLocalFastWithHeadingDelta(double lat, double lon, double h, double[] out) {
        toLocalFast(lat, lon, h, out);
        if (out.length > 3) out[3] = 0.0; // default: nessuna correzione
    }
}