package packexcalib.gnss;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Locale;

/**
 * Implementazione per file .SP (format fornito). Legge una sola volta e crea un modello immutabile.
 * La trasformazione planimetrica usa i 4 parametri (Cx,Cy,Ca,Ck) come descritto.
 * Il fitting altimetrico usa i coefficienti a0..a5 su X,Y (forma polinomiale) se presenti.
 */
public final class SpLocalization implements LocalizationModel {

    private static final CRSFactory crsFactory = new CRSFactory();
    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

    private final CoordinateReferenceSystem wgs84;
    private final CoordinateReferenceSystem projected;
    private final CoordinateTransform geoToProj;

    // 4-parameter
    private final boolean use4Param;
    private final double Cx, Cy, Ca, Ck, Orgx, Orgy;

    // height fitting
    private final boolean useHeightFit;
    private final double[] a; // a0..a5 coefficients
    private final double x0, y0;

    // Reusable coords
    private final ThreadLocal<ProjCoordinate> tlGeo = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    private SpLocalization(CoordinateReferenceSystem projected,
                           boolean use4Param, double Cx, double Cy, double Ca, double Ck, double Orgx, double Orgy,
                           boolean useHeightFit, double[] a, double x0, double y0) {
        this.wgs84 = crsFactory.createFromName("EPSG:4326");
        this.projected = projected;
        this.geoToProj = ctFactory.createTransform(wgs84, projected);

        this.use4Param = use4Param; this.Cx = Cx; this.Cy = Cy; this.Ca = Ca; this.Ck = Ck; this.Orgx = Orgx; this.Orgy = Orgy;
        this.useHeightFit = useHeightFit; this.a = a; this.x0 = x0; this.y0 = y0;
    }

    public static SpLocalization fromSpFile(File f) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(f);
        Element head = doc.getDocumentElement();

        // PROJECT PARAM
        Element projNode = (Element) head.getElementsByTagName("CoordinateSystem_ProjectParameter").item(0);
        double centralMeridianRad = Double.parseDouble(getTextContentOrDefault(projNode, "CentralMeridian", "0"));
        double centralMeridianDeg = Math.toDegrees(centralMeridianRad);
        double k = Double.parseDouble(getTextContentOrDefault(projNode, "TK", "1"));
        double falseE = Double.parseDouble(getTextContentOrDefault(projNode, "Tx", "0"));
        double falseN = Double.parseDouble(getTextContentOrDefault(projNode, "Ty", "0"));

        // Build proj4 string for Transverse Mercator
        String projDef = String.format(Locale.US,
                "+proj=tmerc +lon_0=%.9f +k=%.12f +x_0=%.3f +y_0=%.3f +datum=WGS84 +units=m +no_defs",
                centralMeridianDeg, k, falseE, falseN);
        CoordinateReferenceSystem projected = crsFactory.createFromParameters("tmerc_auto", projDef);

        // 4-param
        Element four = (Element) head.getElementsByTagName("CoordinateSystem_FourParameter").item(0);
        boolean use4 = "1".equals(getTextContentOrDefault(four, "Use", "0"));
        double Cx = Double.parseDouble(getTextContentOrDefault(four, "Cx", "0"));
        double Cy = Double.parseDouble(getTextContentOrDefault(four, "Cy", "0"));
        double Ca = Double.parseDouble(getTextContentOrDefault(four, "Ca", "0"));
        double Ck = Double.parseDouble(getTextContentOrDefault(four, "Ck", "1"));
        double Orgx = Double.parseDouble(getTextContentOrDefault(four, "Orgx", "0"));
        double Orgy = Double.parseDouble(getTextContentOrDefault(four, "Orgy", "0"));

        // height fitting
        Element hf = (Element) head.getElementsByTagName("CoordinateSystem_HeightFittingParameter").item(0);
        boolean useHF = hf != null && "1".equals(getTextContentOrDefault(hf, "Use", "0"));
        double[] a = new double[6]; double x0 = 0, y0 = 0;
        if (useHF) {
            for (int i = 0; i < 6; i++) a[i] = Double.parseDouble(getTextContentOrDefault(hf, "a" + i, "0"));
            x0 = Double.parseDouble(getTextContentOrDefault(hf, "x0", "0"));
            y0 = Double.parseDouble(getTextContentOrDefault(hf, "y0", "0"));
        }

        return new SpLocalization(projected, use4, Cx, Cy, Ca, Ck, Orgx, Orgy, useHF, a, x0, y0);
    }

    private static String getTextContentOrDefault(Element parent, String tag, String def) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl == null || nl.getLength() == 0) return def;
        String s = nl.item(0).getTextContent();
        return s == null || s.isEmpty() ? def : s.trim();
    }

    @Override
    public void toLocalFast(double lat, double lon, double h, double[] out) {
        ProjCoordinate geo = tlGeo.get();
        ProjCoordinate proj = tlProj.get();
        geo.x = lon; geo.y = lat;
        geoToProj.transform(geo, proj);
        double x = proj.x; double y = proj.y;

        double xl = x, yl = y;
        if (use4Param) {
            // shift to origin
            double xd = x - Orgx;
            double yd = y - Orgy;
            double cosA = Math.cos(Ca);
            double sinA = Math.sin(Ca);
            double xr = (xd * cosA - yd * sinA);
            double yr = (xd * sinA + yd * cosA);
            xl = Cx + Ck * xr;
            yl = Cy + Ck * yr;
        }

        double zl = h;
        if (useHeightFit) {
            // polynomial: a0 + a1*(x-x0) + a2*(y-y0) + a3*(x-x0)^2 + a4*(y-y0)^2 + a5*(x-x0)*(y-y0)
            double dx = x - x0;
            double dy = y - y0;
            zl = a[0] + a[1]*dx + a[2]*dy + a[3]*dx*dx + a[4]*dy*dy + a[5]*dx*dy;
        }

        out[0] = xl; out[1] = yl; out[2] = zl;
    }
}
