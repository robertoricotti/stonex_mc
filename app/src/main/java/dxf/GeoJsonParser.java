package dxf;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import gui.draw_class.MyColorClass;

public class GeoJsonParser {
    private GeoJsonData geoJsonData;

    // Passiamo solo il nome del file
    public GeoJsonParser(String filePath) {
        // Creiamo un'istanza di GeoJsonData
        geoJsonData = new GeoJsonData();

        // Carichiamo il contenuto del file GeoJSON dalla memoria esterna
        String geoJsonString = loadGeoJsonFromExternal(filePath);

        // Se il file è stato caricato correttamente, parsalo
        if (geoJsonString != null) {
            List<Polyline_2D> polylines = parseGeoJson(geoJsonString);
            geoJsonData.setPolylinesJson(polylines);  // Impostiamo le polilinee trovate
        }
    }

    public GeoJsonData getGeoJsonData() {
        return geoJsonData;
    }

    // Metodo per caricare il contenuto del file GeoJSON dalla memoria esterna
    private String loadGeoJsonFromExternal(String filePath) {
        String json = null;
        try {
            File file = new File(filePath);
            InputStream is = new FileInputStream(file);  // Carichiamo il file dalla memoria esterna
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            //Log.d("GeoJsonParser", "Errore nella lettura del file: " + ex.toString());
            return null;
        }
        return json;
    }

    // Metodo per fare il parsing del contenuto GeoJSON e creare le Polyline_2D
    private List<Polyline_2D> parseGeoJson(String geoJson) {
        List<Polyline_2D> polylines = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(geoJson);
            JSONArray features = jsonObject.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");

                // Verifica se il tipo di geometria è LineString
                if (geometry.getString("type").equals("LineString")) {
                    JSONArray coordinates = geometry.getJSONArray("coordinates");

                    // Creazione di una nuova Polyline_2D
                    Polyline_2D polyline = new Polyline_2D();

                    // Parsing delle coordinate
                    for (int j = 0; j < coordinates.length(); j++) {
                        JSONArray coord = coordinates.getJSONArray(j);
                        double x = coord.getDouble(0); // Longitudine
                        double y = coord.getDouble(1); // Latitudine
                        double z = 0; // Elevazione opzionale

                        // Creiamo un punto e lo aggiungiamo alla polyline
                        Point3D point = new Point3D(x, y, z);
                        polyline.addVertex(point);
                        polyline.setLineColor(MyColorClass.jsonColor);
                    }

                    polylines.add(polyline);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return polylines;
    }
}
