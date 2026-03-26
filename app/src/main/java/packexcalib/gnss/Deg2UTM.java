package packexcalib.gnss;

import static gui.MyApp.heposTransformer;
import static packexcalib.gnss.CRS_Strings._LOCAL_COORDINATES_FROM_GNSS;
import static packexcalib.gnss.CRS_Strings._NONE;
import static packexcalib.gnss.CRS_Strings._UTM;

import static services.UpdateValuesService.shifted;


import android.util.Log;



import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import gui.MyApp;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;

/*********************************************
 * Class to convert Latitude and Longitude
 * in UTM / Project CRS
 *********************************************/
public class Deg2UTM {
    public static NativeProjTransformer nativeProjTransformer;
    public static NativeProjTransformer nativeProjTransformerToGeo;
    public static boolean nativeCzechReady = false;

    private static final ProjCoordinate baseWgs = new ProjCoordinate();
    private static final ProjCoordinate northWgs = new ProjCoordinate();
    private static final ProjCoordinate baseProj = new ProjCoordinate();
    private static final ProjCoordinate northProj = new ProjCoordinate();

    // buffer input WGS riusabile
    private static final ProjCoordinate inWgs = new ProjCoordinate();

    // ====== lettori geoide (cache condivisa per tutte le istanze) ======
    private static GeoideInterpolation UGF_READER;
    private static String UGF_PATH_LOADED;
    public static boolean ugfReady;

    private static GeoidBinLite BIN_READER;
    private static String BIN_PATH_LOADED;
    public static boolean binReady;

    private static GGFGeoide GGF_READER;
    private static String GGF_PATH_LOADED;
    public static boolean ggfReady;

    // ====== risultati ======
    private static double Easting;
    private static double Northing;
    private static double Quota;
    private static int Zone;
    private static char Letter;

    static double[] out = new double[4];
    public static boolean geoidError;

    // buffer riusabile per UGF
    private static final double[] quotaBuf = new double[1];

    public static CoordinateXYZ trasform(double Lat, double Lon, double Z, String crs) {

        if (DataSaved.my_comPort == 4) {

            Northing = Lat;
            Easting = Lon;
            Quota = Z;

        } else {

            switch (crs) {
                case _LOCAL_COORDINATES_FROM_GNSS:
                    Northing = Lat;
                    Easting = Lon;
                    Quota = Z;
                    NmeaListener.AGGIUNTA_HDT = DataSaved.DELTA_HDT_SMC;
                    break;

                case _NONE:

                    ReadProjectService.model.toLocalFastWithHeadingDelta(Lat, Lon, Z, out);
                    Easting = out[0];
                    Northing = out[1];
                    NmeaListener.AGGIUNTA_HDT = out[3];

                    try {
                        double q = out[2];

                        final String path = MyApp.GEOIDE_PATH;
                        if (path != null && !path.isEmpty() && !"null".equals(path)) {
                            int dot = path.lastIndexOf('.');
                            String ext = (dot > 0) ? path.substring(dot + 1).toLowerCase(Locale.ROOT) : "";

                            switch (ext) {
                                case "bin":
                                    try {
                                        if (BIN_READER == null || !Objects.equals(BIN_PATH_LOADED, path)) {
                                            BIN_READER = new GeoidBinLite(new File(path));
                                            BIN_PATH_LOADED = path;
                                            binReady = true;
                                        }
                                        if (binReady && BIN_READER.isInGrid(Lat, Lon)) {
                                            q = BIN_READER.getOrthometricHeight(Lat, Lon, Z);
                                            geoidError = false;
                                        } else {
                                            geoidError = true;
                                            q = Z;
                                        }
                                    } catch (IOException e) {
                                        binReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                case "ggf":
                                    try {
                                        if (GGF_READER == null || !Objects.equals(GGF_PATH_LOADED, path)) {
                                            GGF_READER = new GGFGeoide();
                                            GGF_READER.load(path);
                                            GGF_PATH_LOADED = path;
                                            ggfReady = true;
                                        }
                                        if (ggfReady && GGF_READER.isInGrid(Lat, Lon)) {
                                            double und = GGF_READER.getUndulation(Lat, Lon);
                                            q = Double.isNaN(und) ? Z : (Z - und);
                                            geoidError = false;
                                        } else {
                                            geoidError = true;
                                            q = Z;
                                        }
                                    } catch (Exception e) {
                                        ggfReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                case "ugf":
                                    try {
                                        if (UGF_READER == null || !Objects.equals(UGF_PATH_LOADED, path)) {
                                            UGF_READER = new GeoideInterpolation(path);
                                            UGF_READER.readHeader();
                                            UGF_PATH_LOADED = path;
                                            ugfReady = true;
                                        }
                                        if (ugfReady && UGF_READER != null) {
                                            if (UGF_READER.internalLetturaGeoide(Lat, Lon, Z, quotaBuf, false)) {
                                                q = quotaBuf[0];
                                                geoidError = false;
                                            } else {
                                                geoidError = true;
                                                q = Z;
                                            }
                                        } else {
                                            geoidError = true;
                                            q = Z;
                                        }
                                    } catch (Exception e) {
                                        ugfReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                default:
                                    q = out[2];
                                    geoidError = false;
                            }
                        } else {
                            q = out[2];
                            geoidError = false;
                        }

                        Quota = q;

                    } catch (Exception e) {
                        geoidError = true;
                    }

                    break;

                case _UTM:
                    NmeaListener.AGGIUNTA_HDT = 0;

                    Easting = 0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.625 / Math.pow((1 + Math.pow(0.0820944379, 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)), 0.5) * (1 + Math.pow(0.0820944379, 2) / 2 * Math.pow((0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) / 3) + 500000;
                    Northing = (Math.atan(Math.tan(Lat * Math.PI / 180) / Math.cos((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) + 0.9996 * 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 3);
                    if (Letter <= 'M')
                        Northing = Northing + 10000000;

                    try {
                        double q = Z;

                        final String path = MyApp.GEOIDE_PATH;
                        if (path != null && !path.isEmpty() && !"null".equals(path)) {
                            int dot = path.lastIndexOf('.');
                            String ext = (dot > 0) ? path.substring(dot + 1).toLowerCase(Locale.ROOT) : "";

                            switch (ext) {
                                case "bin":
                                    try {
                                        if (BIN_READER == null || !Objects.equals(BIN_PATH_LOADED, path)) {
                                            BIN_READER = new GeoidBinLite(new File(path));
                                            BIN_PATH_LOADED = path;
                                            binReady = true;
                                        }
                                        if (binReady && BIN_READER.isInGrid(Lat, Lon)) {
                                            q = BIN_READER.getOrthometricHeight(Lat, Lon, Z);
                                            geoidError = false;
                                        } else {
                                            geoidError = true;
                                            q = Z;
                                        }
                                    } catch (IOException e) {
                                        binReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                case "ggf":
                                    try {
                                        if (GGF_READER == null || !Objects.equals(GGF_PATH_LOADED, path)) {
                                            GGF_READER = new GGFGeoide();
                                            GGF_READER.load(path);
                                            GGF_PATH_LOADED = path;
                                            ggfReady = true;
                                        }
                                        if (ggfReady && GGF_READER.isInGrid(Lat, Lon)) {
                                            double und = GGF_READER.getUndulation(Lat, Lon);
                                            q = Double.isNaN(und) ? Z : (Z - und);
                                            geoidError = false;
                                        } else {
                                            geoidError = true;
                                            q = Z;
                                        }
                                    } catch (Exception e) {
                                        ggfReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                case "ugf":
                                    try {
                                        if (UGF_READER == null || !Objects.equals(UGF_PATH_LOADED, path)) {
                                            UGF_READER = new GeoideInterpolation(path);
                                            UGF_READER.readHeader();
                                            UGF_PATH_LOADED = path;
                                            ugfReady = true;
                                        }
                                        if (ugfReady && UGF_READER != null) {
                                            if (UGF_READER.internalLetturaGeoide(Lat, Lon, Z, quotaBuf, false)) {
                                                q = quotaBuf[0];
                                                geoidError = false;
                                            } else {
                                                geoidError = true;
                                                q = Z;
                                            }
                                        } else {
                                            geoidError = true;
                                            q = Z;
                                        }
                                    } catch (Exception e) {
                                        ugfReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                default:
                                    q = Z;
                                    geoidError = false;
                            }
                        } else {
                            q = Z;
                            geoidError = false;
                        }

                        Quota = q;

                    } catch (Exception e) {
                        geoidError = true;
                    }
                    break;

                default:


                    try {

                        double q = Z;

                        final String path = MyApp.GEOIDE_PATH;
                        if (path != null && !path.isEmpty() && !"null".equals(path)) {
                            int dot = path.lastIndexOf('.');
                            String ext = (dot > 0) ? path.substring(dot + 1).toLowerCase(Locale.ROOT) : "";

                            switch (ext) {
                                case "bin":
                                    try {
                                        if (BIN_READER == null || !Objects.equals(BIN_PATH_LOADED, path)) {
                                            BIN_READER = new GeoidBinLite(new File(path));
                                            BIN_PATH_LOADED = path;
                                            binReady = true;
                                        }
                                        if (binReady && BIN_READER.isInGrid(Lat, Lon)) {
                                            q = BIN_READER.getOrthometricHeight(Lat, Lon, Z);
                                            geoidError = false;
                                        } else {
                                            geoidError = true;
                                            q = Z;
                                        }
                                    } catch (IOException e) {
                                        binReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                case "ggf":
                                    try {
                                        if (GGF_READER == null || !Objects.equals(GGF_PATH_LOADED, path)) {
                                            GGF_READER = new GGFGeoide();
                                            GGF_READER.load(path);
                                            GGF_PATH_LOADED = path;
                                            ggfReady = true;
                                        }
                                        if (ggfReady && GGF_READER.isInGrid(Lat, Lon)) {
                                            double und = GGF_READER.getUndulation(Lat, Lon);
                                            q = Double.isNaN(und) ? Z : (Z - und);
                                            geoidError = false;
                                        } else {
                                            geoidError = true;
                                            q = Z;
                                        }
                                    } catch (Exception e) {
                                        ggfReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                case "ugf":
                                    try {
                                        if (UGF_READER == null || !Objects.equals(UGF_PATH_LOADED, path)) {
                                            UGF_READER = new GeoideInterpolation(path);
                                            UGF_READER.readHeader();
                                            UGF_PATH_LOADED = path;
                                            ugfReady = true;
                                        }
                                        if (ugfReady && UGF_READER != null) {
                                            if (UGF_READER.internalLetturaGeoide(Lat, Lon, Z, quotaBuf, false)) {
                                                q = quotaBuf[0];
                                                geoidError = false;
                                            } else {
                                                geoidError = true;
                                                q = Z;
                                            }
                                        } else {
                                            geoidError = true;
                                            q = Z;
                                        }
                                    } catch (Exception e) {
                                        ugfReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                default:
                                    q = Z;
                                    geoidError = false;
                            }
                        } else {
                            q = Z;
                            geoidError = false;
                        }
                        if(crs.equals("150580") && heposTransformer != null){
                            NmeaListener.AGGIUNTA_HDT =heposTransformer.getAggiuntaHDT();
                            double[] p = heposTransformer.transform(Lat, Lon, q);
                            Easting = p[0];
                            Northing = p[1] + 2000000;
                            Quota = p[2];
                        }else if (crs.equals("150581")) {
                            NmeaListener.AGGIUNTA_HDT = gridHeadingDeltaDeg(Lat, Lon, "5514");
                            double[] p = trasformCS2CS(Lon, Lat, q);
                            Easting = p[0];
                            Northing = p[1];
                            Quota = q;


                        }else if (crs.equals("150582")) {
                            NmeaListener.AGGIUNTA_HDT = wrap180(gridHeadingDeltaDeg(Lat, Lon, "5514") + 180.0);
                            double[] p = trasformCS2CS(Lon, Lat, q);
                            Easting = -p[0];
                            Northing = -p[1];
                            Quota = q;
                        }
                        else if (crs.equals("5516")) {
                            NmeaListener.AGGIUNTA_HDT = gridHeadingDeltaDeg(Lat, Lon, "5516");
                            double[] p = trasformCS2CS(Lon, Lat, q);
                            Easting = p[0];
                            Northing = p[1];
                            Quota = q;
                        }else if (crs.equals("5514") ) {
                            NmeaListener.AGGIUNTA_HDT = gridHeadingDeltaDeg(Lat, Lon, "5514");
                            double[] p = trasformCS2CS(Lon, Lat, q);
                            Easting = p[0];
                            Northing = p[1];
                            Quota = q;
                        } else if (crs.equals("5513")) {
                            NmeaListener.AGGIUNTA_HDT = gridHeadingDeltaDeg(Lat, Lon, "5513");
                            double[] p = trasformCS2CS(Lon, Lat, q);
                            Easting = p[0];
                            Northing = p[1];
                            Quota = q;
                        } else {
                            NmeaListener.AGGIUNTA_HDT = gridHeadingDeltaDeg(Lat, Lon);
                            double[] p = trasformCS2CS(Lon, Lat, q);
                            Easting = p[0];
                            Northing = p[1];
                            Quota = q;
                        }


                    } catch (Exception e) {
                        Log.e("GridShift", "Transform error", e);
                        geoidError = true;
                        NmeaListener.AGGIUNTA_HDT = 0;
                        double[] p = trasformCS2CS(Lon, Lat, Z);
                        Easting = p[0];
                        Northing = p[1];
                        Quota = Z;


                    }
                    break;
            }

            try {
                Zone = computeUtmZone(Lon);
                Letter = computeUtmLetter(Lat);
            } catch (Exception ignored) {
            }
        }

        return new CoordinateXYZ(Easting, Northing, Quota, Zone, Letter);
    }

    /**
     * Delta heading per EPSG:5514.
     * Restituisce i gradi da sommare all'HDT true per allinearlo agli assi EN del sistema Krovak East North.
     * <p>
     * Formula numerica:
     * - proietta il punto
     * - proietta un punto leggermente più a nord vero
     * - calcola l'azimut del vero nord nel piano proiettato
     * <p>
     * 0 = nord, 90 = est
     */


    private static char computeUtmLetter(double Lat) {
        char Letter;

        if (Lat < -72)
            Letter = 'C';
        else if (Lat < -64)
            Letter = 'D';
        else if (Lat < -56)
            Letter = 'E';
        else if (Lat < -48)
            Letter = 'F';
        else if (Lat < -40)
            Letter = 'G';
        else if (Lat < -32)
            Letter = 'H';
        else if (Lat < -24)
            Letter = 'J';
        else if (Lat < -16)
            Letter = 'K';
        else if (Lat < -8)
            Letter = 'L';
        else if (Lat < 0)
            Letter = 'M';
        else if (Lat < 8)
            Letter = 'N';
        else if (Lat < 16)
            Letter = 'P';
        else if (Lat < 24)
            Letter = 'Q';
        else if (Lat < 32)
            Letter = 'R';
        else if (Lat < 40)
            Letter = 'S';
        else if (Lat < 48)
            Letter = 'T';
        else if (Lat < 56)
            Letter = 'U';
        else if (Lat < 64)
            Letter = 'V';
        else if (Lat < 72)
            Letter = 'W';
        else
            Letter = 'X';

        return Letter;
    }

    private static int computeUtmZone(double Lon) {
        return (int) Math.floor(Lon / 6 + 31);
    }

    private static double gridHeadingDeltaDeg(double latDeg, double lonDeg) {
        if (nativeProjTransformer == null) return 0.0;

        final double epsDeg = 1e-4; // ~11 m

        double[] p1 = nativeProjTransformer.transformPrepared(lonDeg, latDeg, 0.0);
        double[] p2 = nativeProjTransformer.transformPrepared(lonDeg, latDeg + epsDeg, 0.0);

        double dE = p2[0] - p1[0];
        double dN = p2[1] - p1[1];

        return Math.toDegrees(Math.atan2(dE, dN));
    }

    private static double gridHeadingDeltaDeg(double latDeg, double lonDeg, String crs) {
        final double epsDeg = 1e-4; // ~11 m

        try {
            double[] p1;
            double[] p2;

            if ("5514".equals(crs) ||
                    "5513".equals(crs) ||
                    "5516".equals(crs) ||
                    "150581".equals(crs) ||
                    "150582".equals(crs)) {

                p1 = trasformCS2CS(lonDeg, latDeg, 0.0);
                p2 = trasformCS2CS(lonDeg, latDeg + epsDeg, 0.0);

            } else {
                if (nativeProjTransformer == null) return 0.0;

                p1 = nativeProjTransformer.transformPrepared(lonDeg, latDeg, 0.0);
                p2 = nativeProjTransformer.transformPrepared(lonDeg, latDeg + epsDeg, 0.0);
            }

            if (p1 == null || p2 == null) return 0.0;

            double dE = p2[0] - p1[0];
            double dN = p2[1] - p1[1];

            return Math.toDegrees(Math.atan2(dE, dN));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static void ensureNativeCzechReady() {
        if (!nativeCzechReady) {
            throw new IllegalStateException("NativeProjTransformer not init");
        }
    }

    private static double[] trasformCS2CS(double x,double y,double z){
        ensureNativeCzechReady();
        return nativeProjTransformer.transformPrepared(x,y,z);
    }

    private static double wrap180(double deg) {
        while (deg <= -180.0) deg += 360.0;
        while (deg > 180.0) deg -= 360.0;
        return deg;
    }

}