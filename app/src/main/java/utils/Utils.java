package utils;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;


public class Utils {

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isNumericInch(String strNum) {
        if (strNum == null || Math.abs(Double.parseDouble(strNum)) > 11) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String showCoords(String str) {
        double v = Double.parseDouble(str);
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 2 || index == 3 || index == 4 || index == 5) {
            return String.format("%.4f", v / 0.3048006096).replace(",", ".");
        } else if (index==6||index==7) {
            return String.format("%.4f", v / 0.3048).replace(",", ".");
        } else {
            return String.format("%.3f", v).replace(",", ".");
        }
    }


    @SuppressLint("DefaultLocale")
    public static String readSensorCalibration(String str) {
        double v = Double.parseDouble(str);
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 2 || index == 3) {
            return String.format("%.4f", v / 0.3048006096).replace(",", ".");
        } else if (index == 4 || index == 5) {
            double inches = v / 0.0254;
            int feet = (int) (inches / 12);
            double leftover = inches % 12;

            String fractionInches = decimalToCommonFraction(Math.abs(leftover));
            if (v < 0) {

                return "-" + Math.abs(feet) + "' " + fractionInches + "\"";
            } else {
                return Math.abs(feet) + "' " + fractionInches + "\"";
            }

        } else if (index == 6 || index == 7) {
            return String.format("%.4f", v / 0.3048).replace(",", ".");
        } else {
            return String.format("%.3f", v).replace(",", ".");
        }
    }

    @SuppressLint("DefaultLocale")
    public static String writeMetri(String str) {
        int index = MyData.get_Int("Unit_Of_Measure");

        if (index == 2 || index == 3) {
            double v = Double.parseDouble(str);
            return String.format("%.4f", v * 0.3048006096).replace(",", ".");
        } else if (index == 4 || index == 5) {
            String[] parts = str.split("'");
            double feet = Double.parseDouble(parts[0].trim());
            String inchPart = parts[1].trim().replace("\"", "").trim();

            double inches = 0.0;
            if (inchPart.contains(" ")) {
                String[] inchParts = inchPart.split(" ");
                inches = Double.parseDouble(inchParts[0].trim());

                String fractionPart = inchParts[1].trim();
                double fraction = parseFraction(fractionPart);
                inches += fraction;
            } else if (inchPart.contains("/")) {
                inches = parseFraction(inchPart);
            } else {
                inches = Double.parseDouble(inchPart);
            }

            double totalInches = Math.abs(feet * 12) + inches;
            if (feet < 0) {
                totalInches = -totalInches;
            }
            double meters = totalInches * 0.0254;
            return String.valueOf(meters);
        } else if (index==6||index==7) {
            double v = Double.parseDouble(str);
            return String.format("%.4f", v * 0.3048).replace(",", ".");
        } else {
            double v = Double.parseDouble(str);
            return String.format("%.3f", v).replace(",", ".");
        }
    }

    @SuppressLint("DefaultLocale")
    public static String readUnitOfMeasure(String str) {
        double v = Double.parseDouble(str);
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 2 || index == 3) {
            return String.format("%.3f", v / 0.3048006096).replace(",", ".");
        } else if (index == 4 || index == 5) {
            double inches = v / 0.0254;
            int feet = (int) (inches / 12);
            double leftover = inches % 12;

            String fractionInches = decimalToCommonFraction(Math.abs(leftover));
            if (v < 0) {

                return "-" + Math.abs(feet) + "' " + fractionInches + "\"";
            } else {
                return Math.abs(feet) + "' " + fractionInches + "\"";
            }

        } else if (index==6||index==7) {
            return String.format("%.3f", v / 0.3048).replace(",", ".");
        }else {
            return String.format("%.2f", v).replace(",", ".");
        }
    }


    @SuppressLint("DefaultLocale")
    public static String writeGradi(String str) {
        double p = Double.parseDouble(str);

        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 1 || index == 3 || index == 5||index==7) {
            //convertire % deg
            return String.format("%.3f", Math.toDegrees(Math.atan(p / 100))).replace(",", ".");
        } else {
            return String.format("%.3f", p).replace(",", ".");
        }
    }

    @SuppressLint("DefaultLocale")
    public static String readAngolo(String str) {
        double p = (Double.parseDouble(str));
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 1 || index == 3 || index == 5||index==7) {
            double a = Math.toRadians(Double.parseDouble(str));
            //convertire % in deg

            return String.format("%.2f", (Math.tan(a) * 100.0d)).replace(",", ".");
        } else {
            return String.format("%.2f", p).replace(",", ".");
        }
    }

    @SuppressLint("DefaultLocale")
    public static String readAngoloLITE(String str) {
        double p = (Double.parseDouble(str));
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 1 || index == 3 || index == 5||index==7) {
            double a = Math.toRadians(Double.parseDouble(str));
            //convertire % in deg

            return String.format("%.1f", (Math.tan(a) * 100.0d)).replace(",", ".");
        } else {
            return String.format("%.1f", p).replace(",", ".");
        }
    }

    public static String getGradiSimbol() {
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 0 || index == 2 || index == 4|| index==6)
            return " °";
        else
            return " %";
    }


    public static String getMetriSimbol() {
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 0 || index == 1)
            return "[m]";
        else if (index == 2 || index == 3)
            return "[US ft]";
        else if (index==6||index==7) {
            return "[Int ft]";
        } else
            return "[ft in]";
    }
    public static String getMetriSimbolCoords() {
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 0 || index == 1)
            return "[m]";
        else if (index == 2 || index == 3||index == 4 || index == 5)
            return "[US ft]";

        else if (index==6||index==7) {
            return "[Int ft]";
        } else
            return "[ft in]";
    }

    @SuppressLint("DefaultLocale")
    public static String readUnitOfMeasureLITE(String str) {
        double v = Double.parseDouble(str);

        int index = MyData.get_Int("Unit_Of_Measure");

        if (index == 2 || index == 3) {
            return String.format("%.2f", v / 0.3048006096).replace(",", ".");
        } else if (index == 4 || index == 5) {
            double inches = v / 0.0254;
            int feet = (int) (inches / 12);
            double leftover = inches % 12;

            String fractionInches = decimalToCommonFraction(Math.abs(leftover));
            if (v < 0) {

                return "-" + Math.abs(feet) + "' " + fractionInches + "\"";
            } else {
                return Math.abs(feet) + "' " + fractionInches + "\"";
            }

        } else if (index == 6 || index == 7) {
            return String.format("%.2f", v / 0.3048).replace(",", ".");
        } else {
            return String.format("%.2f", v).replace(",", ".");
        }
    }

    @SuppressLint("DefaultLocale")
    public static String readUnitOfMeasureLB(String str) {
        double v = Double.parseDouble(str);

        int index = MyData.get_Int("Unit_Of_Measure");

        if (index == 2 || index == 3 || index == 4 || index == 5) {
            return String.format("%.2f", v / 0.3048006096).replace(",", ".");
        } else if (index==6||index==7) {
            return String.format("%.2f", v / 0.3048).replace(",", ".");
        } else {
            return String.format("%.2f", v).replace(",", ".");
        }
    }


    public static String decimalToCommonFraction(double decimal) {
        final double[] decimalValues = {0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875};
        final String[] fractionStrings = {"0/0", "1/8", "1/4", "3/8", "1/2", "5/8", "3/4", "7/8"};

        boolean isNegative = decimal < 0;
        decimal = Math.abs(decimal);

        int wholeInches = (int) decimal;
        double fractionalPart = decimal - wholeInches;

        double smallestError = Double.MAX_VALUE;
        String bestFraction = " ";

        for (int i = 0; i < decimalValues.length; i++) {
            double error = Math.abs(fractionalPart - decimalValues[i]);
            if (error < smallestError) {
                smallestError = error;
                bestFraction = fractionStrings[i];
            }
        }

        String result;

        if (wholeInches == 0 && bestFraction.equals(" ")) {
            result = "0 0/0";
        } else if (bestFraction.equals("0/0")) {
            result = Integer.toString(wholeInches) + " " + bestFraction;
        } else if (wholeInches == 0) {
            result = "0 " + bestFraction;
        } else {
            result = wholeInches + " " + bestFraction;
        }

        if (isNegative) {
            result = "-" + result;
        }

        return result;
    }

    @SuppressLint("DefaultLocale")
    public static String writeMetriLITE(String str) {
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 2 || index == 3) {
            double v = Double.parseDouble(str);
            return String.format("%.4f", v * 0.3048006096).replace(",", ".");
        } else if (index == 4 || index == 5) {
            String[] parts = str.split("'");

            double feet = Double.parseDouble(parts[0].trim());
            String sFeet = parts[0].trim();
            String inchPart = parts[1].trim().replace("\"", "").trim();

            double inches = 0.0;
            if (inchPart.contains(" ")) {
                String[] inchParts = inchPart.split(" ");
                inches = Double.parseDouble(inchParts[0].trim());

                String fractionPart = inchParts[1].trim();
                double fraction = parseFraction(fractionPart);
                inches += fraction;
            } else if (inchPart.contains("/")) {
                inches = parseFraction(inchPart);
            } else {
                inches = Double.parseDouble(inchPart);
            }

            double totalInches = Math.abs(feet * 12) + inches;
            if (sFeet.contains("-")) {
                totalInches = -totalInches;
            }
            double meters = totalInches * 0.0254;
            if (str.trim().substring(0, 1).equals("-")) {

                return String.valueOf(meters);
            } else {
                return String.valueOf(meters);
            }

        } else if (index == 6 || index == 7) {
            double v = Double.parseDouble(str);
            return String.format("%.4f", v * 0.3048).replace(",", ".");
        } else {
            double v = Double.parseDouble(str);
            return String.format("%.3f", v).replace(",", ".");
        }
    }

    private static double parseFraction(String fraction) {
        String[] fractionParts = fraction.split("/");
        if (fraction.equals("0/0")) {
            return 0;
        } else {
            double numerator = Double.parseDouble(fractionParts[0].trim());
            double denominator = Double.parseDouble(fractionParts[1].trim());
            return numerator / denominator;
        }
    }

    public static List<byte[]> createPackets(String message, byte function) {
        List<byte[]> packets = new ArrayList<>();
        byte[] messageBytes = message.getBytes();
        int messageLength = messageBytes.length;

        for (int i = 0; i < messageLength; i += 6) {
            byte[] packet = new byte[8];
            packet[0] = 0x20; // scrivi
            packet[1] = function; // valore

            int j;
            for (j = 0; j < 6 && (i + j) < messageLength; j++) {
                packet[2 + j] = messageBytes[i + j];
            }

            // Se il ciclo si interrompe prima di riempire il pacchetto, aggiungi il byte di terminazione
            if (j < 6) {
                packet[2 + j] = -1;
            }

            packets.add(packet);
        }

        // Aggiungi un pacchetto finale se l'ultimo pacchetto è completamente pieno (lunghezza multipla di 6)
        if (messageLength % 6 == 0) {
            byte[] packet = new byte[8];
            packet[0] = 0x20; // Cambia questi valori secondo la tua logica
            packet[1] = function; // Cambia questi valori secondo la tua logica
            packet[2] = -1; // Byte di terminazione
            packets.add(packet);
        }

        return packets;
    }

    public static byte[] convertIpStringToBytes(String ipString) {
        try {
            String[] parts = ipString.split("\\.");
            byte[] ipBytes = new byte[4];

            for (int i = 0; i < parts.length; i++) {
                ipBytes[i] = (byte) Integer.parseInt(parts[i]);
            }

            return ipBytes;
        } catch (NumberFormatException e) {
            return new byte[4];
        }

    }


}