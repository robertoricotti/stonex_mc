package packexcalib.gnss;


import static packexcalib.gnss.CRS_Strings._NONE;
import static packexcalib.gnss.CRS_Strings._UTM;
import static services.UpdateValuesService.result;
import static services.UpdateValuesService.wgsToUtm;

import android.util.Log;

import org.locationtech.proj4j.ProjCoordinate;

import java.io.File;
import java.io.IOException;


/*********************************************
 * Class to convert Latitude and Longitude
 * in UTM
 *
 *
 ***************************************/

public class Deg2UTM {

    static GeoideInterpolation geoideInterpolationGGF;
    static GeoidBinLite geoidBin;
    static GGFGeoide ggfGeoide;
    static double[] quota;
    static double mLat_;
    static double mLon_;
    static double Easting;
    static double Northing;
    static double Quota;
    static int Zone;
    static char Letter;
    public static boolean geoidError;


    public Deg2UTM(double Lat, double Lon, double Z, String crs, String geoidPath) {
        Log.w("Dimens", new File(geoidPath).length() + "");
        switch (crs) {
            case _NONE:
                Northing = Lat;
                Easting = Lon;
                Quota = Z;
                geoidError = false;
                break;
            case _UTM:
                geoidError = false;
                Quota = Z;
                Zone = (int) Math.floor(Lon / 6 + 31);
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
                Easting = 0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.625 / Math.pow((1 + Math.pow(0.0820944379, 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)), 0.5) * (1 + Math.pow(0.0820944379, 2) / 2 * Math.pow((0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) / 3) + 500000;
                Northing = (Math.atan(Math.tan(Lat * Math.PI / 180) / Math.cos((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) + 0.9996 * 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 3);
                if (Letter <= 'M')
                    Northing = Northing + 10000000;

                break;
            default:
                quota = new double[1];
                mLat_ = Lat;
                mLon_ = Lon;
                try {
                    if (geoidPath != null && !geoidPath.equals("null") && !geoidPath.isEmpty()) {
                        String extension = geoidPath.substring(geoidPath.lastIndexOf(".") + 1);
                        switch (extension.toLowerCase()) {
                            case "bin":
                                Log.d("Geoid", "BIN   " + geoidPath);
                                geoidBin = new GeoidBinLite(new File(geoidPath));
                                if (geoidBin.isInGrid(mLat_, mLon_)) {
                                    geoidError = false;
                                    quota[0] = geoidBin.getOrthometricHeight(mLat_, mLon_, Z);

                                } else {
                                    geoidError = true;
                                    quota[0] = Z;
                                }
                                break;

                            case "ggf":
                                Log.d("Geoid", "GGF   " + mLat_ + " " + mLon_);
                                try {
                                    ggfGeoide = new GGFGeoide();
                                    ggfGeoide.load(geoidPath);
                                    if (ggfGeoide.isInGrid(mLat_, mLon_)) {
                                        geoidError = false;
                                        quota[0] = Z - ggfGeoide.getUndulation(mLat_, mLon_);
                                        Log.w("Geoid", "OK   " + mLat_ + " " + mLon_ + "  " + ggfGeoide.getUndulation(mLat_, mLon_));
                                    } else {
                                        geoidError = true;
                                        quota[0] = Z;
                                        Log.e("Geoid", "KO   " + mLat_ + " " + mLon_ + "  " + quota[0]);

                                    }
                                } catch (Exception e) {
                                    geoidError = true;
                                    quota[0] = Z;
                                    Log.e("Geoid", Log.getStackTraceString(e));
                                }

                                break;

                            case "ugf":
                                ///  UGF

                                Log.d("Geoid", "UGF   " + geoidPath);
                                geoideInterpolationGGF = new GeoideInterpolation(geoidPath);
                                geoideInterpolationGGF.readHeader();
                                if (geoideInterpolationGGF.internalLetturaGeoide(mLat_, mLon_, Z, quota, false)) {
                                    geoidError = false;

                                } else {
                                    quota[0] = Z;
                                    geoidError = true;

                                }
                                break;

                            default:
                                quota[0] = Z;
                                break;
                        }

                    } else {
                        quota[0] = Z;
                        geoidError = false;
                    }

                    if (wgsToUtm != null && result != null) {
                        wgsToUtm.transform(new ProjCoordinate(mLon_, mLat_, quota[0]), result);
                        Easting = result.x;
                        Northing = result.y;
                        Quota = quota[0];
                        Log.d("OutCoord", Northing + "  " + Easting + "  " + Quota);

                    }
                } catch (IOException e) {
                    Log.e("myLoggy", geoidPath + "  " + e.toString());
                }
                break;

        }


    }


    public double getEasting() {
        return Easting;
    }

    public double getNorthing() {
        return Northing;
    }

    public double getQuota() {
        return Quota;
    }

    public int getZone() {
        return Zone;
    }

    public char getLetter() {
        return Letter;
    }


}
