package packexcalib.gnss;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class My_LocationCalc {
    private final static double EarthRadius=6378137.0;

    public static double calcBearing(double lat1_D,double lng1_D,double lat2_D,double lng2_D){

        double lat1_R = Math.toRadians(lat1_D);
        double lat2_R = Math.toRadians(lat2_D);
        double lng1_R = Math.toRadians(lng1_D);
        double lng2_R = Math.toRadians(lng2_D);

        double y = Math.sin(lng2_R - lng1_R) * Math.cos(lat2_R);
        double x = Math.cos(lat1_R) * Math.sin(lat2_R) - Math.sin(lat1_R) * Math.cos(lat2_R) * Math.cos(lng2_R - lng1_R);

        double bearing = Math.atan2(y, x);
        bearing = Math.toDegrees(bearing);
        if (bearing < 0) {
            bearing += 360;
        }
        return bearing;

    }
    public static double baseLineDist(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EarthRadius * c;
    }
    public static double calcBearingXY(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1; // Est
        double dy = y2 - y1; // Nord

        double bearing = Math.toDegrees(Math.atan2(dx, dy));
        if (bearing < 0) {
            bearing += 360.0;
        }

        return bearing;
    }
   /* public static double calcBearingXY(double x1, double y1, double x2, double y2) {
        double lat1_D = y1 / EarthRadius; // Converte y in metri in latitudine in gradi
        double lon1_D = x1 / (EarthRadius * Math.cos(Math.toRadians(lat1_D))); // Converte x in metri in longitudine in gradi
        double lat2_D = y2 / EarthRadius;
        double lon2_D = x2 / (EarthRadius * Math.cos(Math.toRadians(lat2_D)));

        double lat1_R = Math.toRadians(lat1_D);
        double lon1_R = Math.toRadians(lon1_D);
        double lat2_R = Math.toRadians(lat2_D);
        double lon2_R = Math.toRadians(lon2_D);

        double y = Math.sin(lon2_R - lon1_R) * Math.cos(lat2_R);
        double x = Math.cos(lat1_R) * Math.sin(lat2_R) - Math.sin(lat1_R) * Math.cos(lat2_R) * Math.cos(lon2_R - lon1_R);

        double bearing = Math.atan2(y, x);
        bearing = Math.toDegrees(bearing);
        if (bearing < 0) {
            bearing += 360;
        }

        return bearing;
    }*/
    public static double dmsToDecimal(String dms) {
        // Rimuovi spazi e caratteri speciali dalla stringa DMS
        dms = dms.replaceAll("[^0-9.°'-]", "");

        // Divide la stringa DMS nei componenti
        String[] parts = dms.split("[°'\"']+");

        if (parts.length == 3) {
            try {
                int degrees = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1])*Integer.compare(degrees,0);
                double seconds = Double.parseDouble(parts[2])*Integer.compare(degrees,0);

                double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);
                return decimal;
            } catch (NumberFormatException e) {
                // Gestisci eventuali errori di parsing
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static String decimalToDMS(double decimal) {
        int degrees = (int) decimal;
        double tempMinutes = (decimal - degrees) * 60;
        int minutes = (int) tempMinutes;
        double seconds = (tempMinutes - minutes) * 60;

        String formattedSeconds = String.format("%.6f", Math.abs(seconds));

        return String.format("%d° %d' %s\"", degrees, Math.abs(minutes), formattedSeconds).replace(",",".");
    }






}