package packexcalib.gnss;


import static gui.MyApp.heposTransformer;
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


/*********************************************
 * Class to convert Latitude and Longitude
 * in UTM
 *
 *
 ***************************************/

public class Deg2UTM {

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

    // ====== risultati (di istanza) ======
    private static double Easting;
    private static double Northing;
    private static double Quota;
    private static int Zone;
    private static char Letter;


    public static boolean geoidError;

    // buffer riusabile per UGF
    private static final double[] quotaBuf = new double[1];

    public static CoordinateXYZ trasform(double Lat, double Lon, double Z, String crs) {

        if (DataSaved.my_comPort == 4) {

            Northing = Lat;
            Easting = Lon;
            Quota = Z;

        } else {
            Zone = computeUtmZone(Lon);
            Letter=computeUtmLetter(Lat);
            switch (crs) {
                case _NONE:

                    double[] out = new double[3];
                    ReadProjectService.model.toLocalFast(Lat, Lon, Z, out);
                    Easting = out[0];
                    Northing = out[1];


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
                                            //Log.d("Deg2UTM", "BIN letto");
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
                                        //Log.e("Deg2UTM", "BIN load/use error", e);
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
                                            //Log.d("Deg2UTM", "GGF letto");
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
                                        //Log.e("Deg2UTM", "GGF load/use error", e);
                                        ggfReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                case "ugf":
                                    try {
                                        if (UGF_READER == null || !Objects.equals(UGF_PATH_LOADED, path)) {
                                            UGF_READER = new GeoideInterpolation(path);
                                            UGF_READER.readHeader();            // una sola volta per path
                                            // Log.d("Deg2UTM", "UGF letto");
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
                                        //Log.e("Deg2UTM", "UGF load/use error", e);
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
                        //Log.e("Deg2UTM", "Transform error", e);
                        geoidError = true;

                    }
                    //  Log.d("TestCRSSS",  Northing + "  " + Easting+ "  "+Quota);


                    break;
                case _UTM:


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
                                            //Log.d("Deg2UTM", "BIN letto");
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
                                        //Log.e("Deg2UTM", "BIN load/use error", e);
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
                                            //Log.d("Deg2UTM", "GGF letto");
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
                                        //Log.e("Deg2UTM", "GGF load/use error", e);
                                        ggfReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                case "ugf":
                                    try {
                                        if (UGF_READER == null || !Objects.equals(UGF_PATH_LOADED, path)) {
                                            UGF_READER = new GeoideInterpolation(path);
                                            UGF_READER.readHeader();            // una sola volta per path
                                            // Log.d("Deg2UTM", "UGF letto");
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
                                        //Log.e("Deg2UTM", "UGF load/use error", e);
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
                        //Log.e("Deg2UTM", "Transform error", e);
                        geoidError = true;

                    }
                    break;
                default:

                    // ====== ramo con geoide + trasformazione ======
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
                                            //Log.d("Deg2UTM", "BIN letto");
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
                                        //Log.e("Deg2UTM", "BIN load/use error", e);
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
                                            //Log.d("Deg2UTM", "GGF letto");
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
                                        //Log.e("Deg2UTM", "GGF load/use error", e);
                                        ggfReady = false;
                                        geoidError = true;
                                        q = Z;
                                    }
                                    break;

                                case "ugf":
                                    try {
                                        if (UGF_READER == null || !Objects.equals(UGF_PATH_LOADED, path)) {
                                            UGF_READER = new GeoideInterpolation(path);
                                            UGF_READER.readHeader();            // una sola volta per path
                                            // Log.d("Deg2UTM", "UGF letto");
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
                                        //Log.e("Deg2UTM", "UGF load/use error", e);
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

                        if (crs.equals("150580")) {

                            if (heposTransformer != null) {

                                shifted = heposTransformer.transform(Lat, Lon, q);
                                Easting = shifted.x;
                                Northing = shifted.y + 2000000;
                                Quota = shifted.z;

                            }
                        } else {
                            if (wgsToUtm != null && result != null) {
                                wgsToUtm.transform(new ProjCoordinate(Lon, Lat, q), result);
                                Easting = result.x;
                                Northing = result.y;
                                Quota = q;

                            }
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
                    }
                    break;

            }
        }


        return new CoordinateXYZ(Easting, Northing, Quota, Zone, Letter);
    }

    private static char computeUtmLetter(double Lat) {
        char Letter;
       /* char[] bands = {'C','D','E','F','G','H','J','K','L','M','N','P','Q','R','S','T','U','V','W','X'};
        int idx = (int)Math.floor((Lat + 80.0)/8.0);
        if (idx < 0) idx = 0;
        if (idx >= bands.length) idx = bands.length - 1;
        return bands[idx];*/
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
