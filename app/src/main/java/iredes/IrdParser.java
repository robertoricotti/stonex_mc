package iredes;

import android.util.Log;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import services.ReadProjectService;

public class IrdParser {
    private static final String NS_JET = "urn:custom:jet:treatment:v1";
    private static final String NS_DR = "http://www.iredes.org/xml/DrillRig";
    private static final String NS_IR = "http://www.iredes.org/xml";

    public static List<Point3D_Drill> parseIrd(String filePath, int xyz, double conversionFactor) {
        List<Point3D_Drill> out = new ArrayList<>();

        try {
            File inputFile = new File(filePath);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList holes = doc.getElementsByTagNameNS(NS_DR, "Hole");
            Log.i("IRD", "Holes trovati: " + holes.getLength());

            for (int i = 0; i < holes.getLength(); i++) {
                Node n = holes.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) continue;
                Element holeEl = (Element) n;

                Point3D_Drill p = new Point3D_Drill();

                String holeName = textOfFirst(holeEl, NS_DR, "HoleName");
                String holeId   = textOfFirst(holeEl, NS_DR, "HoleId");

                // ✅ Preferenza: HoleName -> row/id
                // es: "1.1" => row=1 id=1
                if (holeName != null && holeName.contains(".")) {
                    String[] parts = holeName.trim().split("\\.");
                    p.setRowId(parts.length > 0 ? emptyToNull(parts[0]) : null);
                    p.setId(parts.length > 1 ? emptyToNull(parts[1]) : emptyToNull(holeName));
                } else {
                    // fallback: se HoleName manca usa HoleId, altrimenti HoleName
                    p.setRowId(null);
                    p.setId(holeName != null ? holeName : holeId);
                }

                // ✅ Diametro per ora non interessa
                p.setDiameter(null);

                // --- StartPoint / EndPoint ---
                Element sp = firstChild(holeEl, NS_DR, "StartPoint");
                Element ep = firstChild(holeEl, NS_DR, "EndPoint");

                // ✅ qui la conversione è già applicata: *conversionFactor
                Double[] head = readPointXYZ(sp, xyz, conversionFactor);
                Double[] end  = readPointXYZ(ep, xyz, conversionFactor);

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

                // Tilt calcolato da endpoints (0 verticale, 90 orizzontale)
                p.setTilt(computeTiltFromEndpoints(p));

                // heading/depth/length
                p.recomputeDerived();
                parseJetTreatmentIfPresent(holeEl, p);

                if (p.getId() != null || p.getRowId() != null ||
                        p.getHeadX() != null || p.getEndX() != null) {
                    out.add(p);
                    ReadProjectService.parserStatus = "Reading Points..."+"\n"+out.size();
                }
            }

        } catch (Exception e) {
            Log.e("IRD", "Errore parsing .ird", e);
        }
        ReadProjectService.isFinishedPOINT=true;
        return out;
    }

    // ----------------- Helpers -----------------

    private static Element firstChild(Element parent, String ns, String localName) {
        if (parent == null) return null;
        NodeList nl = parent.getElementsByTagNameNS(ns, localName);
        if (nl == null || nl.getLength() == 0) return null;
        Node n = nl.item(0);
        return (n.getNodeType() == Node.ELEMENT_NODE) ? (Element) n : null;
    }

    private static String textOfFirst(Element parent, String ns, String localName) {
        Element e = firstChild(parent, ns, localName);
        if (e == null) return null;
        return emptyToNull(e.getTextContent());
    }

    private static Double[] readPointXYZ(Element pointEl, int xyz, double conv) {
        if (pointEl == null) return null;

        Double x = parseDoubleSafe(textOfFirst(pointEl, NS_IR, "PointX"));
        Double y = parseDoubleSafe(textOfFirst(pointEl, NS_IR, "PointY"));
        Double z = parseDoubleSafe(textOfFirst(pointEl, NS_IR, "PointZ"));

        if (x == null || y == null || z == null) return null;

        // swap coerente col tuo LandXMLParser
        double xx = (xyz == 1) ? y : x;
        double yy = (xyz == 1) ? x : y;

        // ✅ conversione come richiesto
        return new Double[]{ xx * conv, yy * conv, z * conv };
    }

    private static Double computeTiltFromEndpoints(Point3D_Drill p) {
        if (p.getHeadX() == null || p.getHeadY() == null || p.getHeadZ() == null ||
                p.getEndX() == null || p.getEndY() == null || p.getEndZ() == null) {
            return null;
        }

        double dx = p.getEndX() - p.getHeadX();
        double dy = p.getEndY() - p.getHeadY();
        double dz = p.getEndZ() - p.getHeadZ();

        double horiz = Math.sqrt(dx * dx + dy * dy);
        double vert = Math.abs(dz);

        if (horiz == 0 && vert == 0) return 0.0;
        return Math.toDegrees(Math.atan2(horiz, vert)); // 0 verticale, 90 orizzontale
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static Double parseDoubleSafe(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        s = s.replace(',', '.');
        try {
            return Double.parseDouble(s);
        } catch (Exception ignore) {
            return null;
        }
    }
    private static void parseJetTreatmentIfPresent(Element holeEl, Point3D_Drill p) {
        Element treatmentEl = firstChild(holeEl, NS_JET, "Treatment");
        if (treatmentEl == null) return;

        NodeList children = treatmentEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;

            Element el = (Element) n;
            String key = el.getLocalName(); // es: "drlStart_1"
            if (key == null) key = el.getNodeName();
            String val = emptyToNull(el.getTextContent());
            if (val == null) continue;

            applyJetField(p, key, val);
        }
    }

    private static void applyJetField(Point3D_Drill p, String key, String value) {
        // Normalizza eventuali spazi
        key = key.trim();

        switch (key) {
            // ---- DRILL 1..4 ----
            case "drlStart_1": p.setDrlStart_1(value); break;
            case "drlStop_1":  p.setDrlStop_1(value);  break;
            case "pr_1":       p.setPr_1(value);       break;

            case "drlStart_2": p.setDrlStart_2(value); break;
            case "drlStop_2":  p.setDrlStop_2(value);  break;
            case "pr_2":       p.setPr_2(value);       break;

            case "drlStart_3": p.setDrlStart_3(value); break;
            case "drlStop_3":  p.setDrlStop_3(value);  break;
            case "pr_3":       p.setPr_3(value);       break;

            case "drlStart_4": p.setDrlStart_4(value); break;
            case "drlStop_4":  p.setDrlStop_4(value);  break;
            case "pr_4":       p.setPr_4(value);       break;

            // ---- JET 1..4 ----
            case "jetStart_1": p.setJetStart_1(value); break;
            case "jetStop_1":  p.setJetStop_1(value);  break;
            case "pr_j_1":     p.setPr_j_1(value);     break;

            case "jetStart_2": p.setJetStart_2(value); break;
            case "jetStop_2":  p.setJetStop_2(value);  break;
            case "pr_j_2":     p.setPr_j_2(value);     break;

            case "jetStart_3": p.setJetStart_3(value); break;
            case "jetStop_3":  p.setJetStop_3(value);  break;
            case "pr_j_3":     p.setPr_j_3(value);     break;

            case "jetStart_4": p.setJetStart_4(value); break;
            case "jetStop_4":  p.setJetStop_4(value);  break;
            case "pr_j_4":     p.setPr_j_4(value);     break;

            default:
                // Tag sconosciuto: lo ignoriamo (così puoi estendere senza rompere)
                Log.w("IRD-JET", "Tag JET non gestito: " + key + " = " + value);
                break;
        }
    }

}
