package packexcalib.gnss;

/**
 * Interfaccia semplice per i modelli di localizzazione.
 * Implementazioni devono essere thread-safe e leggere il file di definizione una sola volta.
 */
public interface LocalizationModel {
    /**
     * Trasforma Lat,Lon,Height(ellipsoidal) -> X,Y,Z locali.
     * Questo metodo **NON** deve allocare oggetti per chiamata ad alta frequenza.
     *
     * @param lat latitudine in gradi
     * @param lon longitudine in gradi
     * @param h   quota ellissoidale in metri
     * @param out array double di lunghezza >=3 in cui vengono scritti [X,Y,Z]
     */
    void toLocalFast(double lat, double lon, double h, double[] out);
    void toGeoFast(double E,double N,double H,double[] out);

    /**
     * Comodità che ritorna un nuovo array (può allocare). Usalo solo se non sei nel nodo critico.
     */
    default double[] toLocal(double lat, double lon, double h) {
        double[] out = new double[3];
        toLocalFast(lat, lon, h, out);
        return out;
    }
}
