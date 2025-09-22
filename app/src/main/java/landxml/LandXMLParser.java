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
import services.ReadProjectService;

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

            // Per ogni superficie
            NodeList surfaceList = doc.getElementsByTagName("Surface");
            for (int i = 0; i < surfaceList.getLength(); i++) {
                Node surfaceNode = surfaceList.item(i);
                if (surfaceNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element surfaceElement = (Element) surfaceNode;
                    String surfaceName = surfaceElement.getAttribute("name");
                    if (surfaceName.isEmpty()) {
                        surfaceName = "Layer_" + (i + 1);
                    }

                    // Crea un layer per la superficie
                    Layer layer = new Layer(filePath, surfaceName, colorIndex++, true);
                    landXMLData.addLayer(layer);

                    // Mappa dei punti per questa superficie
                    Map<String, Point3D> pointsMap = new HashMap<>();

                    // Parsing dei punti all'interno della superficie
                    NodeList pointNodes = surfaceElement.getElementsByTagName("P");
                    for (int j = 0; j < pointNodes.getLength(); j++) {
                        Node pointNode = pointNodes.item(j);
                        if (pointNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element pointElement = (Element) pointNode;
                            String id = pointElement.getAttribute("id");
                            String[] coordinates = pointElement.getTextContent().trim().split(" ");

                            double x = xyz == 1 ? Double.parseDouble(coordinates[1]) : Double.parseDouble(coordinates[0]);
                            double y = xyz == 1 ? Double.parseDouble(coordinates[0]) : Double.parseDouble(coordinates[1]);
                            double z = Double.parseDouble(coordinates[2]);

                            Point3D point = new Point3D(id, x * conversionFactor, y * conversionFactor, z * conversionFactor);
                            point.setFilename(filePath);
                            point.setLayer(layer); // Associa il punto al layer corrente
                            pointsMap.put(id, point);
                            landXMLData.addPoint(point); // Aggiungi al dataset generale
                            landXMLData.addText(new DxfText(id, point.getX(), point.getY(), point.getZ(), layer.getColorState(), layer));
                        }
                    }

                    // Parsing delle facce all'interno della superficie
                    NodeList faceNodes = surfaceElement.getElementsByTagName("F");
                    for (int j = 0; j < faceNodes.getLength(); j++) {
                        Node faceNode = faceNodes.item(j);
                        if (faceNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element faceElement = (Element) faceNode;
                            String[] pointIds = faceElement.getTextContent().trim().split(" ");

                            if (pointIds.length == 3) {
                                Point3D p1 = pointsMap.get(pointIds[0]);
                                Point3D p2 = pointsMap.get(pointIds[1]);
                                Point3D p3 = pointsMap.get(pointIds[2]);

                                if (p1 != null && p2 != null && p3 != null) {
                                    Face3D face = new Face3D(p1, p2, p3, p3, layer.getColorState(), layer);
                                    landXMLData.addFace(face);
                                } else {
                                    Log.e("LandXMLParser", "Punti mancanti per la faccia: " + Arrays.toString(pointIds));
                                }
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            isFinished(filePath);
            Log.e("LandXMLParser", "Errore durante il parsing del file LandXML", e);
        }
        isFinished(filePath);
        return landXMLData;
    }

    private static void isFinished(String filePath) {
        if (filePath.equals(DataSaved.progettoSelected)) {
            isFinishedDTM = true;
        }
        if (filePath.equals(DataSaved.progettoSelected_POLY)) {
            isFinishedPOLY = true;
        }
        if (filePath.equals(DataSaved.progettoSelected_POINT)) {
            isFinishedPOINT = true;
        }
    }
}


/*

import static services.ReadProjectService.isFinishedDTM;
import static services.ReadProjectService.isFinishedPOINT;
import static services.ReadProjectService.isFinishedPOLY;


import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
public class LandXMLParser {

    public static LandXMLData parseLandXML(String filePath, int xyz, boolean isFeet) {
        LandXMLData landXMLData = new LandXMLData();

        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            LandXMLHandler handler = new LandXMLHandler(landXMLData, filePath, xyz, isFeet);
            saxParser.parse(inputStream, handler);

        } catch (Exception e) {
            Log.e("LandXMLSAXParser", "Errore nel parsing SAX", e);
        }

        // Imposta flag a fine parsing
        isFinished(filePath);
        return landXMLData;
    }

    private static void isFinished(String filePath) {
        if (filePath.equals(DataSaved.progettoSelected)) {
            isFinishedDTM = true;
        } else if (filePath.equals(DataSaved.progettoSelected_POLY)) {
            isFinishedPOLY = true;
        } else if (filePath.equals(DataSaved.progettoSelected_POINT)) {
            isFinishedPOINT = true;
        }
    }
}
 */





