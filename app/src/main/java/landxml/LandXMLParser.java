package landxml;

import static services.ReadProjectService.isFinishedDTM;
import static services.ReadProjectService.isFinishedPOINT;
import static services.ReadProjectService.isFinishedPOLY;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import packexcalib.exca.DataSaved;

public class LandXMLParser {

    public static LandXMLData parseLandXML(String filePath, int xyz, double conversionFactor) {
        int colorIndex = 0;
        LandXMLData landXMLData = new LandXMLData();

        try {
            File inputFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList surfaceList = doc.getElementsByTagName("Surface");
            for (int i = 0; i < surfaceList.getLength(); i++) {
                Node surfaceNode = surfaceList.item(i);
                if (surfaceNode.getNodeType() != Node.ELEMENT_NODE) continue;

                Element surfaceElement = (Element) surfaceNode;

                // -----------------------------------------
                // 1️⃣ NORMALIZZI IL NOME
                // -----------------------------------------
                String baseName = normalizeLayerName(surfaceElement.getAttribute("name"));
                if (baseName.isEmpty()) baseName = "Layer_" + (i + 1);

                // -----------------------------------------
                // 2️⃣ GESTIONE NOMI DUPLICATI
                // -----------------------------------------
                String surfaceName = baseName;
                int counter = 2;
                while (landXMLData.layerExists(surfaceName)) {
                    surfaceName = baseName + " (" + counter + ")";
                    counter++;
                }

                Log.i("LandXML-LAYER", "Using layer name: " + surfaceName);

                // -----------------------------------------
                // 3️⃣ CREAZIONE DEL LAYER
                // -----------------------------------------
                Layer layer = new Layer(filePath, surfaceName, colorIndex++, true);
                landXMLData.addLayer(layer);

                // -----------------------------------------
                // 4️⃣ MAPPA PUNTI PER LA SUPERFICIE
                // -----------------------------------------
                Map<String, Point3D> pointsMap = new HashMap<>();

                NodeList pointNodes = surfaceElement.getElementsByTagName("P");
                for (int j = 0; j < pointNodes.getLength(); j++) {
                    Element pointElement = (Element) pointNodes.item(j);

                    String id = pointElement.getAttribute("id").trim();
                    if (pointsMap.containsKey(id)) {
                        Log.w("LandXML-P DUPLICATE",
                                "Surface=" + surfaceName + " ID=" + id + " → DUPLICATO SKIPPATO");
                        continue;
                    }

                    String[] c = pointElement.getTextContent().trim().split(" ");
                    double x = xyz == 1 ? Double.parseDouble(c[1]) : Double.parseDouble(c[0]);
                    double y = xyz == 1 ? Double.parseDouble(c[0]) : Double.parseDouble(c[1]);
                    double z = Double.parseDouble(c[2]);

                    Point3D p = new Point3D(id, x * conversionFactor, y * conversionFactor, z * conversionFactor);
                    p.setLayer(layer);
                    p.setFilename(filePath);

                    pointsMap.put(id, p);
                    landXMLData.addPoint(p);
                    landXMLData.addText(new DxfText(id, p.getX(), p.getY(), p.getZ(), layer.getColorState(), layer));
                }

                // -----------------------------------------
                // 5️⃣ FACCE DELLA SUPERFICIE
                // -----------------------------------------
                NodeList faceNodes = surfaceElement.getElementsByTagName("F");
                for (int j = 0; j < faceNodes.getLength(); j++) {
                    Element faceElement = (Element) faceNodes.item(j);
                    String[] ids = faceElement.getTextContent().trim().split(" ");

                    if (ids.length != 3) continue;

                    Point3D p1 = pointsMap.get(ids[0]);
                    Point3D p2 = pointsMap.get(ids[1]);
                    Point3D p3 = pointsMap.get(ids[2]);

                    if (p1 == null || p2 == null || p3 == null) {
                        Log.e("LandXML-FACE",
                                "Triangolo corrotto in layer " + surfaceName +
                                        " → " + Arrays.toString(ids));
                        continue;
                    }

                    landXMLData.addFace(new Face3D(p1, p2, p3, p3, layer.getColorState(), layer));
                }
            }

        } catch (Exception e) {
            Log.e("LandXMLParser", "Errore durante il parsing", e);
        }

        isFinished(filePath);
        return landXMLData;
    }

    private static void isFinished(String filePath) {
        if (filePath.equals(DataSaved.progettoSelected)) isFinishedDTM = true;
        if (filePath.equals(DataSaved.progettoSelected_POLY)) isFinishedPOLY = true;
        if (filePath.equals(DataSaved.progettoSelected_POINT)) isFinishedPOINT = true;
    }

    private static String normalizeLayerName(String s) {
        if (s == null) return "";
        return s.trim()
                .replace("\u00A0", "")
                .replace("\u2007", "")
                .replace("\u202F", "")
                .replace("\u2009", "")
                .replace("\t", "")
                .replace("\r", "")
                .replace("\n", "");
    }
}
