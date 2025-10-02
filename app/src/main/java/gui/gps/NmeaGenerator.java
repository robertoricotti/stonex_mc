package gui.gps;

import android.util.Log;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

import packexcalib.exca.DataSaved;
import packexcalib.gnss.CalculateXor8;
import packexcalib.gnss.NmeaListener;

public class NmeaGenerator {
    public static double LATITUDE, LONGITUDE, ALTITUDE, HEADING, NORD_SIMUL, EAST_SIMUL;


    static CalculateXor8 calculateXor8, calculateXorHdt;
    static String myNmea;


    public static String generateGPGGA() {
        try {
            double lat, lon;
            lat = 39.74040133208518 ;//to test
            lon = 20.821570772611768;//to test
            NmeaListener.VRMS_ = "0.014";
            Calendar now = Calendar.getInstance();
            DecimalFormat decimalFormat = new DecimalFormat("0.0000000000", DecimalFormatSymbols.getInstance(Locale.US));
            decimalFormat.setRoundingMode(RoundingMode.DOWN);
            double formattedLatitude = Double.parseDouble(decimalFormat.format(Math.abs(lat)));
            double formattedLongitude = Double.parseDouble(decimalFormat.format(Math.abs(lon)));

            char latitudeDirection = lat >= 0 ? 'N' : 'S';
            char longitudeDirection = lon >= 0 ? 'E' : 'W';

            String sLat = new Formatter().format("%02d%.7f", (int) formattedLatitude, (formattedLatitude % 1) * 60).toString().replace(",", ".");
            String sLon = new Formatter().format("%03d%.7f", (int) formattedLongitude, (formattedLongitude % 1) * 60).toString().replace(",", ".");
            String sAlt = String.valueOf(ALTITUDE).replace(",", ".");
            String stime = formatTime(now);

            String gpggaString = "$GPGGA," + stime + "," + sLat + "," + latitudeDirection + "," + sLon + "," + longitudeDirection + ",4,24,0.9," + sAlt + ",M,0,M,01,0000";
            myNmea = gpggaString.substring(gpggaString.indexOf("$") + 1, gpggaString.length());
            calculateXor8 = new CalculateXor8(myNmea.getBytes(StandardCharsets.UTF_8));
            int checksum = calculateXor8.xor;
            return gpggaString + "*" + String.format("%02X", checksum);
        } catch (Exception e) {
            return "$GPGGA,220955.20,4533.7144380,N,00910.9522140,E,1,20,0.9,80.456,M,0,M,01,0000*70";
        }
    }

    public static String generateLLQ() {
        try {

            if (LATITUDE == 0) {
                LATITUDE = DataSaved.demoNORD;

            }
            if (LONGITUDE == 0) {
                LONGITUDE = DataSaved.demoEAST;
            }
            if (ALTITUDE == 0) {
                ALTITUDE = DataSaved.demoZ;
            }
            if (HEADING == 0) {
                HEADING = 90;
            }

            NmeaListener.VRMS_ = "0.014";
            Calendar now = Calendar.getInstance();
            DecimalFormat decimalFormat = new DecimalFormat("0.0000000000", DecimalFormatSymbols.getInstance(Locale.US));
            decimalFormat.setRoundingMode(RoundingMode.DOWN);

            char latitudeDirection = 'M';
            char longitudeDirection = 'M';

            String sAlt = String.format("%.3f", ALTITUDE).replace(",", ".");
            String stime = formatTime(now);
            String date = "13112024";
            String gpggaString = "$GPLLQ," + stime + "," + date + "," + String.format("%.3f", LONGITUDE).replace(",", ".") + "," + longitudeDirection + "," + String.format("%.3f", LATITUDE).replace(",", ".") + "," + latitudeDirection + ",3,24," + NmeaListener.VRMS_ + "," + sAlt + ",M,0,M,01,0000";
            myNmea = gpggaString.substring(gpggaString.indexOf("$") + 1, gpggaString.length());
            calculateXor8 = new CalculateXor8(myNmea.getBytes(StandardCharsets.UTF_8));
            int checksum = calculateXor8.xor;
            return gpggaString + "*" + String.format("%02X", checksum);
        } catch (Exception e) {
            return "$GPLLQ,220955.20,13112024,678515.714,M,4848012.952,M,1,20,0.091,80.456,M,0,M,01,0000*6F";
        }
    }


    private static String formatTime(Calendar calendar) {
        DecimalFormat decimalFormat = new DecimalFormat("00");
        return String.format("%s%s%s.%s", decimalFormat.format(calendar.get(Calendar.HOUR_OF_DAY)),
                decimalFormat.format(calendar.get(Calendar.MINUTE)),
                decimalFormat.format(calendar.get(Calendar.SECOND)),
                decimalFormat.format(calendar.get(Calendar.MILLISECOND) / 10));
    }

    public static String generateGPHDT() {
        try {
            String sh = "$GPHDT,";
            String hdt = String.valueOf(HEADING).replace(",", ".");
            String gphdtString = sh + hdt + ",T";
            String s = gphdtString.substring(gphdtString.indexOf("$") + 1, gphdtString.length());
            calculateXorHdt = new CalculateXor8(s.getBytes(StandardCharsets.UTF_8));
            int checksum = calculateXorHdt.xor;
            return gphdtString + "*" + String.format("%02X", checksum);
        } catch (Exception e) {
            return "$GPHDT,359.99,T*0A";
        }

    }

}
