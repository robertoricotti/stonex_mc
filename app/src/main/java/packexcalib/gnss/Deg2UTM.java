package packexcalib.gnss;

import static gui.MyApp.cz_Q1;
import static gui.MyApp.cz_Q3;
import static gui.MyApp.heposTransformer;
import static packexcalib.gnss.CRS_Strings._LOCAL_COORDINATES_FROM_GNSS;
import static packexcalib.gnss.CRS_Strings._NONE;
import static packexcalib.gnss.CRS_Strings._UTM;
import static services.UpdateValuesService.result;
import static services.UpdateValuesService.shifted;
import static services.UpdateValuesService.wgsToUtm;

import android.util.Log;

import org.locationtech.proj4j.ProjCoordinate;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import gui.MyApp;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
import utils.MyData;

/*********************************************
 * Class to convert Latitude and Longitude
 * in UTM / Project CRS
 *********************************************/
public class Deg2UTM {

    // buffer input WGS riusabile
    private static final ProjCoordinate inWgs = new ProjCoordinate();

    // buffer riusabili per heading delta Krovak 5514
    private static final ProjCoordinate krovakBaseWgs = new ProjCoordinate();
    private static final ProjCoordinate krovakNorthWgs = new ProjCoordinate();
    private static final ProjCoordinate krovakBaseProj = new ProjCoordinate();
    private static final ProjCoordinate krovakNorthProj = new ProjCoordinate();

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
                    NmeaListener.AGGIUNTA_HDT = 0;

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

                        if (crs.equals("150580") && heposTransformer != null) {

                            shifted = heposTransformer.transform(Lat, Lon, q);
                            Easting = shifted.x;
                            Northing = shifted.y + 2000000;
                            Quota = shifted.z;

                        } else if (crs.equals("150581") && cz_Q1 != null) {
                            if (wgsToUtm != null && result != null) {
                                inWgs.x = Lon;
                                inWgs.y = Lat;
                                inWgs.z = q;
                                wgsToUtm.transform(inWgs, result);
                                cz_Q1.applyInPlace(result);
                                Easting = result.x;
                                Northing = result.y;
                                Quota = q;
                            }

                        } else if (crs.equals("150582") && cz_Q3 != null) {
                            if (wgsToUtm != null && result != null) {
                                inWgs.x = Lon;
                                inWgs.y = Lat;
                                inWgs.z = q;
                                wgsToUtm.transform(inWgs, result);
                                cz_Q3.applyInPlace(result);
                                Easting = result.x;
                                Northing = result.y;
                                Quota = q;
                            }

                        } else {
                            if (wgsToUtm != null && result != null) {
                                wgsToUtm.transform(new ProjCoordinate(Lon, Lat, q), result);
                                Easting = result.x;
                                Northing = result.y;
                                Quota = q;
                            }
                        }

                        // SOLO QUI: aggiunta heading dinamica per EPSG:5514
                        if ("5514".equals(crs)) {
                            NmeaListener.AGGIUNTA_HDT = krovak5514HeadingDeltaDeg(Lat, Lon);
                        }

                    } catch (Exception e) {
                        Log.e("GridShift", "Transform error", e);
                        geoidError = true;

                        if (wgsToUtm != null && result != null) {
                            wgsToUtm.transform(new ProjCoordinate(Lon, Lat, Z), result);
                            Easting = result.x;
                            Northing = result.y;
                            Quota = Z;
                        }

                        // fallback heading anche in caso di eccezione, solo per 5514
                        if ("5514".equals(crs)) {
                            try {
                                NmeaListener.AGGIUNTA_HDT = krovak5514HeadingDeltaDeg(Lat, Lon);
                            } catch (Exception ignored) {
                                NmeaListener.AGGIUNTA_HDT = 0;
                            }
                        }
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
     *
     * Formula numerica:
     * - proietta il punto
     * - proietta un punto leggermente più a nord vero
     * - calcola l'azimut del vero nord nel piano proiettato
     *
     * 0 = nord, 90 = est
     */
    private static double krovak5514HeadingDeltaDeg(double latDeg, double lonDeg) {
        if (wgsToUtm == null) return 0.0;

        final double epsDeg = 1e-5; // circa 1.1 m

        // punto base
        krovakBaseWgs.x = lonDeg;
        krovakBaseWgs.y = latDeg;
        krovakBaseWgs.z = 0.0;
        wgsToUtm.transform(krovakBaseWgs, krovakBaseProj);

        // punto leggermente più a nord vero
        krovakNorthWgs.x = lonDeg;
        krovakNorthWgs.y = latDeg + epsDeg;
        krovakNorthWgs.z = 0.0;
        wgsToUtm.transform(krovakNorthWgs, krovakNorthProj);

        double dE = krovakNorthProj.x - krovakBaseProj.x;
        double dN = krovakNorthProj.y - krovakBaseProj.y;

        return Math.toDegrees(Math.atan2(dE, dN));
    }

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
}