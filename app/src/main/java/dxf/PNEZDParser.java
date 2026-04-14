package dxf;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PNEZDParser {
    //mode 0= PNEZD mode 1=PENZD
    static PNEZDPoint point;

    public static List<PNEZDPoint> parseCSV(String filePath, int mode) {
        List<PNEZDPoint> points = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                // Salta righe vuote o commenti
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//") || trimmed.startsWith(";")) {
                    continue;
                }

                String[] tokens = line.split(",", 5); // max 5 elementi per gestire descrizioni con virgole

                if (tokens.length < 4) {
                    Log.e("pnezdParser", "Riga non valida (meno di 4 campi): " + line);
                    continue;
                }

                try {
                    int pointNumber = Integer.parseInt(tokens[0].trim());
                    double northing = Double.parseDouble(tokens[1].trim());
                    double easting = Double.parseDouble(tokens[2].trim());
                    double elevation = Double.parseDouble(tokens[3].trim());
                    String description = "";
                    if (tokens[4] != null) {
                        description = tokens[4].trim();
                    }


                    if (mode == 0) {
                        point = new PNEZDPoint(pointNumber, northing, easting, elevation, description);
                    } else if (mode == 1) {
                        point = new PNEZDPoint(pointNumber, easting, northing, elevation, description);
                    }
                    points.add(point);
                } catch (NumberFormatException e) {
                    Log.e("pnezdParser", "Errore di parsing numerico nella riga: " + line);
                }
            }
        } catch (IOException e) {
            Log.e("pnezdParser", "Errore nella lettura del file: " + e.getMessage());
        }

        return points;
    }
}
