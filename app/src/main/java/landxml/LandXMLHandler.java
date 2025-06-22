package landxml;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;

public class LandXMLHandler extends DefaultHandler {

    private final LandXMLData data;
    private final StringBuilder content = new StringBuilder();
    private Map<String, Point3D> pointsMap = new HashMap<>();
    private Layer currentLayer;
    private final int xyz;
    private final double conversion;
    private final String filePath;
    private int colorIndex = 0;

    public LandXMLHandler(LandXMLData data, String filePath, int xyz, boolean isFeet) {
        this.data = data;
        this.filePath = filePath;
        this.xyz = xyz;
        this.conversion = isFeet ? 0.3048006096 : 1.0;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        content.setLength(0);

        if (qName.equalsIgnoreCase("Surface")) {
            String surfaceName = attributes.getValue("name");
            if (surfaceName == null || surfaceName.isEmpty()) {
                surfaceName = "Layer_" + (colorIndex + 1);
            }

            currentLayer = new Layer(filePath, surfaceName, colorIndex++, true);
            data.addLayer(currentLayer);
            pointsMap = new HashMap<>();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        content.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("P")) {
            String[] tokens = content.toString().trim().split(" ");
            if (tokens.length >= 3) {
                String id = tokens[0];
                double x = xyz == 1 ? Double.parseDouble(tokens[1]) : Double.parseDouble(tokens[0]);
                double y = xyz == 1 ? Double.parseDouble(tokens[0]) : Double.parseDouble(tokens[1]);
                double z = Double.parseDouble(tokens[2]);

                Point3D point = new Point3D(id, x * conversion, y * conversion, z * conversion);
                point.setLayer(currentLayer);
                pointsMap.put(id, point);
                data.addPoint(point);
                data.addText(new DxfText(id, point.getX(), point.getY(), point.getZ(), currentLayer.getColorState(), currentLayer));
            }

        } else if (qName.equalsIgnoreCase("F")) {
            String[] ids = content.toString().trim().split(" ");
            if (ids.length == 3) {
                Point3D p1 = pointsMap.get(ids[0]);
                Point3D p2 = pointsMap.get(ids[1]);
                Point3D p3 = pointsMap.get(ids[2]);

                if (p1 != null && p2 != null && p3 != null) {
                    Face3D face = new Face3D(p1, p2, p3, p3, currentLayer.getColorState(), currentLayer);
                    data.addFace(face);
                } else {
                    Log.d("LandXMLHandler", "Faccia con punti mancanti: " + Arrays.toString(ids));
                }
            }
        }
    }
}
