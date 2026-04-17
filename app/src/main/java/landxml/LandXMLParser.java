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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import dxf.Polyline;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;

public class LandXMLParser {

    public static LandXMLData parseLandXML(String filePath, int xyz, double conversionFactor) {
        return parseLandXML(filePath, xyz, conversionFactor, false);
    }

    public static LandXMLData parseLandXML(String filePath, int xyz, double conversionFactor, boolean parsePoly) {
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

                String baseName = normalizeLayerName(surfaceElement.getAttribute("name"));
                if (baseName.isEmpty()) baseName = "Layer_" + (i + 1);

                String surfaceName = baseName;
                int counter = 2;
                while (landXMLData.layerExists(surfaceName)) {
                    surfaceName = baseName + " (" + counter + ")";
                    counter++;
                }

                Log.i("LandXML-LAYER", "Using layer name: " + surfaceName);

                Layer layer = new Layer(filePath, surfaceName, colorIndex++, true);
                landXMLData.addLayer(layer);

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

            if (parsePoly) {
                colorIndex = parsePlanFeaturesAsPolylines(doc, landXMLData, filePath, xyz, conversionFactor, colorIndex);
            }

            parseCGPoints(doc, landXMLData, xyz, conversionFactor);

        } catch (Exception e) {
            Log.e("LandXMLParser", "Errore durante il parsing", e);
        }

        isFinished(filePath);
        return landXMLData;
    }

    private static int parsePlanFeaturesAsPolylines(Document doc,
                                                    LandXMLData landXMLData,
                                                    String filePath,
                                                    int xyz,
                                                    double conversionFactor,
                                                    int colorIndex) {
        NodeList planFeatureNodes = doc.getElementsByTagName("PlanFeature");
        if (planFeatureNodes == null || planFeatureNodes.getLength() == 0) {
            return colorIndex;
        }

        final int polylineColor = (colorIndex > 0) ? colorIndex : 5;
        Layer polylineLayer = null;
        boolean addedAnyPolyline = false;

        for (int i = 0; i < planFeatureNodes.getLength(); i++) {
            Node node = planFeatureNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element planFeature = (Element) node;

            Element coordGeom = firstDirectChildElement(planFeature, "CoordGeom");
            if (coordGeom == null) continue;

            NodeList lineNodes = coordGeom.getElementsByTagName("Line");
            if (lineNodes == null || lineNodes.getLength() == 0) continue;

            List<Point3D> vertices = new ArrayList<>();

            for (int j = 0; j < lineNodes.getLength(); j++) {
                Node lineNode = lineNodes.item(j);
                if (lineNode.getNodeType() != Node.ELEMENT_NODE) continue;

                Element lineElement = (Element) lineNode;

                Double[] start = firstCoordFromChildren(lineElement, xyz, conversionFactor, "Start");
                Double[] end = firstCoordFromChildren(lineElement, xyz, conversionFactor, "End");

                if (start == null || end == null) {
                    Log.w("LandXML-POLYLINE",
                            "Segmento ignorato in PlanFeature index=" + i +
                                    " perché Start/End non validi");
                    continue;
                }

                Point3D pStart = new Point3D("PF_" + i + "_" + j + "_S", start[0], start[1], start[2]);
                Point3D pEnd = new Point3D("PF_" + i + "_" + j + "_E", end[0], end[1], end[2]);

                if (vertices.isEmpty()) {
                    vertices.add(pStart);
                } else if (!samePoint(vertices.get(vertices.size() - 1), pStart, 1e-6)) {
                    vertices.add(pStart);
                }

                if (vertices.isEmpty() || !samePoint(vertices.get(vertices.size() - 1), pEnd, 1e-6)) {
                    vertices.add(pEnd);
                }
            }

            if (vertices.size() >= 2) {
                if (polylineLayer == null) {
                    String layerName = "POLYLINE";
                    int counter = 2;
                    while (landXMLData.layerExists(layerName)) {
                        layerName = "POLYLINE (" + counter + ")";
                        counter++;
                    }

                    polylineLayer = new Layer(filePath, layerName, polylineColor, true);
                    landXMLData.addLayer(polylineLayer);
                }

                for (Point3D p : vertices) {
                    p.setLayer(polylineLayer);
                    p.setFilename(filePath);
                }

                Polyline polyline = new Polyline(vertices, filePath, polylineLayer);
                polyline.setLayer(polylineLayer);
                polyline.setFilename(filePath);
                polyline.setLineColor(polylineLayer.getColorState());
                polyline.markGlDirty();

                landXMLData.addPolyline(polyline);
                addedAnyPolyline = true;
            }
        }

        return addedAnyPolyline ? Math.max(colorIndex, polylineColor + 1) : colorIndex;
    }

    private static Layer findLayerByName(LandXMLData landXMLData, String name) {
        if (name == null) return null;
        String normalized = normalizeLayerName(name);
        if (normalized.isEmpty()) return null;

        for (Layer l : landXMLData.getLayers()) {
            if (l != null && l.getLayerName() != null &&
                    l.getLayerName().equalsIgnoreCase(normalized)) {
                return l;
            }
        }
        return null;
    }
    private static Element firstDirectChildElement(Element parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) continue;

            Element el = (Element) child;
            if (matchesTag(el, tagName)) {
                return el;
            }
        }
        return null;
    }

    private static boolean matchesTag(Element e, String expected) {
        if (e == null || expected == null) return false;

        String nodeName = e.getNodeName();
        String localName = e.getLocalName();

        return expected.equals(nodeName) || expected.equals(localName);
    }

    private static boolean samePoint(Point3D a, Point3D b, double eps) {
        if (a == null || b == null) return false;

        return Math.abs(a.getX() - b.getX()) <= eps
                && Math.abs(a.getY() - b.getY()) <= eps
                && Math.abs(a.getZ() - b.getZ()) <= eps;
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

    // ------------------------------------------------------------
    // CGPoints / CgPoints
    // ------------------------------------------------------------
    private static void parseCGPoints(Document doc, LandXMLData landXMLData, int xyz, double conversionFactor) {
        // LandXML standard: <CgPoints><CgPoint name="...">y x z</CgPoint></CgPoints>
        // Alcuni software usano tag/attributi custom: cerchiamo in modo robusto.
        NodeList nodes = doc.getElementsByTagName("CgPoint");
        if (nodes == null || nodes.getLength() == 0) {
            nodes = doc.getElementsByTagName("CGPoint");
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            Element e = (Element) n;

            Point3D_Drill p = new Point3D_Drill();

            // --- ID / Row / Desc ---
            p.setId(firstNonEmptyAttr(e, "name", "id", "pntRef", "oID", "point", "code"));
            p.setRowId(firstNonEmptyAttr(e, "row", "Row", "alignment", "Alignment", "group", "Group", "set", "Set", "line", "Line"));

            String desc = firstNonEmptyAttr(e, "desc", "Desc", "description", "Description");
            if (desc == null) desc = firstChildText(e, "Desc", "Description", "Descr", "Note");
            p.setDescription(desc);

            // --- Diametro / Tilt ---
            p.setDiameter(firstNonEmptyDouble(e,
                    new String[]{"diameter", "Diameter", "dia", "Dia", "d", "D"},
                    new String[]{"Diameter", "Dia", "D", "diam"}));
            p.setTilt(firstNonEmptyDouble(e,
                    new String[]{"tilt", "Tilt", "inclination", "Inclination", "inclin", "Inclin", "slope", "Slope"},
                    new String[]{"Tilt", "Inclination", "Inclin", "Slope"}));

            // --- Coordinate testa/fine ---
            // 1) figli espliciti
            Double[] head = firstCoordFromChildren(e, xyz, conversionFactor, "Start", "Head", "From", "Top", "P1");
            Double[] end = firstCoordFromChildren(e, xyz, conversionFactor, "End", "Tail", "To", "Bottom", "P2");

            // 2) attributi (varianti)
            if (head == null) head = coordFromAttributes(e, xyz, conversionFactor,
                    new String[]{"x", "X", "e", "E", "east", "East", "easting", "Easting", "xHead", "XHead", "headX", "HeadX"},
                    new String[]{"y", "Y", "n", "N", "north", "North", "northing", "Northing", "yHead", "YHead", "headY", "HeadY"},
                    new String[]{"z", "Z", "h", "H", "elev", "Elev", "elevation", "Elevation", "zHead", "ZHead", "headZ", "HeadZ"}
            );
            if (end == null) end = coordFromAttributes(e, xyz, conversionFactor,
                    new String[]{"xEnd", "XEnd", "endX", "EndX", "x2", "X2", "x_to", "X_to"},
                    new String[]{"yEnd", "YEnd", "endY", "EndY", "y2", "Y2", "y_to", "Y_to"},
                    new String[]{"zEnd", "ZEnd", "endZ", "EndZ", "z2", "Z2", "z_to", "Z_to"}
            );

            // 3) testo del CgPoint: può essere (x y z) o (y x z) e a volte (6 numeri) testa+fine
            if (head == null) {
                Double[] coords = coordFromText(e.getTextContent(), xyz, conversionFactor);
                if (coords != null) head = coords;
            }
            if (end == null) {
                Double[] coords6 = coordFromText6(e.getTextContent(), xyz, conversionFactor);
                if (coords6 != null) end = new Double[]{coords6[3], coords6[4], coords6[5]};
            }

            if (head != null) {
                p.setHeadX(head[0]);
                p.setHeadY(head[1]);
                p.setHeadZ(head[2]);
            }
            if (end != null) {
                p.setEndX(end[0]);
                p.setEndY(end[1]);
                p.setEndZ(end[2]);
            }

            // Calcola heading/depth/length se possibile
            p.recomputeDerived();

            // Salva solo se c'è almeno un dato utile
            if (p.getId() != null || p.getRowId() != null || p.getDescription() != null ||
                    p.getHeadX() != null || p.getEndX() != null || p.getDiameter() != null || p.getTilt() != null) {
                landXMLData.addDrillPoint(p);
            }
        }
    }

    private static String firstNonEmptyAttr(Element e, String... names) {
        for (String n : names) {
            String v = e.getAttribute(n);
            if (v != null) {
                v = v.trim();
                if (!v.isEmpty()) return v;
            }
        }
        return null;
    }

    private static String firstChildText(Element e, String... childNames) {
        for (String name : childNames) {
            NodeList nl = e.getElementsByTagName(name);
            if (nl != null && nl.getLength() > 0) {
                String t = nl.item(0).getTextContent();
                if (t != null) {
                    t = t.trim();
                    if (!t.isEmpty()) return t;
                }
            }
        }
        return null;
    }

    private static Double firstNonEmptyDouble(Element e, String[] attrNames, String[] childNames) {
        // attributi
        for (String n : attrNames) {
            String v = e.getAttribute(n);
            Double d = parseDoubleSafe(v);
            if (d != null) return d;
        }
        // figli
        for (String cn : childNames) {
            NodeList nl = e.getElementsByTagName(cn);
            if (nl != null && nl.getLength() > 0) {
                Double d = parseDoubleSafe(nl.item(0).getTextContent());
                if (d != null) return d;
            }
        }
        return null;
    }

    private static Double parseDoubleSafe(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        s = s.replace(',', '.'); // virgola decimale
        try {
            return Double.parseDouble(s);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static Double[] firstCoordFromChildren(Element e, int xyz, double conv, String... childNames) {
        for (String name : childNames) {
            NodeList nl = e.getElementsByTagName(name);
            if (nl != null && nl.getLength() > 0) {
                String txt = nl.item(0).getTextContent();
                Double[] c = coordFromText(txt, xyz, conv);
                if (c != null) return c;
            }
        }
        return null;
    }

    private static Double[] coordFromAttributes(Element e, int xyz, double conv,
                                                String[] xNames, String[] yNames, String[] zNames) {
        Double x = null, y = null, z = null;
        for (String n : xNames) {
            x = parseDoubleSafe(e.getAttribute(n));
            if (x != null) break;
        }
        for (String n : yNames) {
            y = parseDoubleSafe(e.getAttribute(n));
            if (y != null) break;
        }
        for (String n : zNames) {
            z = parseDoubleSafe(e.getAttribute(n));
            if (z != null) break;
        }
        if (x == null || y == null || z == null) return null;

        // Adegua swap X/Y come nel resto del parser
        double xx = xyz == 1 ? y : x;
        double yy = xyz == 1 ? x : y;
        return new Double[]{xx * conv, yy * conv, z * conv};
    }

    private static Double[] coordFromText(String text, int xyz, double conv) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;

        String[] tokens = t.split("[\\s,;]+");
        if (tokens.length < 3) return null;

        Double a = parseDoubleSafe(tokens[0]);
        Double b = parseDoubleSafe(tokens[1]);
        Double c = parseDoubleSafe(tokens[2]);
        if (a == null || b == null || c == null) return null;

        double x = xyz == 1 ? b : a;
        double y = xyz == 1 ? a : b;
        return new Double[]{x * conv, y * conv, c * conv};
    }

    private static Double[] coordFromText6(String text, int xyz, double conv) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty()) return null;

        String[] tokens = t.split("[\\s,;]+");
        if (tokens.length < 6) return null;

        Double a1 = parseDoubleSafe(tokens[0]);
        Double b1 = parseDoubleSafe(tokens[1]);
        Double c1 = parseDoubleSafe(tokens[2]);
        Double a2 = parseDoubleSafe(tokens[3]);
        Double b2 = parseDoubleSafe(tokens[4]);
        Double c2 = parseDoubleSafe(tokens[5]);
        if (a1 == null || b1 == null || c1 == null || a2 == null || b2 == null || c2 == null)
            return null;

        double x1 = xyz == 1 ? b1 : a1;
        double y1 = xyz == 1 ? a1 : b1;
        double x2 = xyz == 1 ? b2 : a2;
        double y2 = xyz == 1 ? a2 : b2;
        return new Double[]{x1 * conv, y1 * conv, c1 * conv, x2 * conv, y2 * conv, c2 * conv};
    }
}