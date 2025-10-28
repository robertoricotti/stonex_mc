package packexcalib.gnss;

import org.locationtech.proj4j.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.Locale;

/**
 * Lettore e trasformatore di file .SP Stonex/SurPad.
 * Funziona correttamente con file aventi central meridian in radianti
 * e con convenzioni 4-parametri Stonex standard.
 * Thread-safe e senza allocazioni a runtime.
 */
public final class SpLocalization implements LocalizationModel {

    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateTransformFactory CT_FACTORY = new CoordinateTransformFactory();

    private final CoordinateTransform geoToProj;
    private final boolean use4;
    private final double Ca, Ck, Orgx, Orgy, x0, y0;
    private final boolean useHF;
    private final double a0, a1, a2, a3, a4, a5;

    private final ThreadLocal<ProjCoordinate> tlGeo  = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    private SpLocalization(CoordinateTransform geoToProj,
                           boolean use4, double Ca, double Ck,
                           double Orgx, double Orgy, double x0, double y0,
                           boolean useHF,
                           double a0, double a1, double a2, double a3, double a4, double a5) {
        this.geoToProj = geoToProj;
        this.use4 = use4;
        this.Ca = Ca; // già in radianti
        this.Ck = Ck;
        this.Orgx = Orgx;
        this.Orgy = Orgy;
        this.x0 = x0;
        this.y0 = y0;
        this.useHF = useHF;
        this.a0 = a0;
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.a4 = a4;
        this.a5 = a5;
    }

    // ------------------------------------------------------------------
    // Lettura file .SP e costruzione automatica proiezione
    // ------------------------------------------------------------------
    public static SpLocalization fromSpFile(File f) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(f);
        Element head = doc.getDocumentElement();

        // --- Parametri di proiezione ---
        Element proj = (Element) head.getElementsByTagName("CoordinateSystem_ProjectParameter").item(0);
        double centralMeridianRad = d(txt(proj, "CentralMeridian", "0"));
        double lon0 = Math.toDegrees(centralMeridianRad);
        double k0 = d(txt(proj, "TK", "1"));
        double Tx = d(txt(proj, "Tx", "0"));  // false northing
        double Ty = d(txt(proj, "Ty", "0"));  // false easting
        int type = (int) d(txt(proj, "Type", "0"));

        // ✅ FIX: usa CentralMeridian se diverso da 0, altrimenti fallback da Type
        if (Math.abs(lon0) < 1e-9 && type >= 220 && type <= 230)
            lon0 = (type - 218) * 6 - 3;

        // Definizione corretta della proiezione Transverse Mercator (UTM-like)
        String projDef = String.format(Locale.US,
                "+proj=tmerc +lat_0=0 +lon_0=%.9f +k=%.12f +x_0=%.3f +y_0=%.3f "
                        + "+datum=WGS84 +units=m +no_defs",
                lon0, k0, Ty, Tx);

        CoordinateReferenceSystem wgs84 =
                CRS_FACTORY.createFromParameters("WGS84", "+proj=longlat +datum=WGS84 +no_defs");
        CoordinateReferenceSystem projected =
                CRS_FACTORY.createFromParameters("LOCALPROJ", projDef);
        CoordinateTransform geoToProj = CT_FACTORY.createTransform(wgs84, projected);

        // --- Parametri 4-parametri ---
        Element four = (Element) head.getElementsByTagName("CoordinateSystem_FourParameter").item(0);
        boolean use4 = isTrue(txt(four, "Use", "0"));
        double Cx = d(txt(four, "Cx", "0"));
        double Cy = d(txt(four, "Cy", "0"));
        double Ca = d(txt(four, "Ca", "0"));
        double Ck = d(txt(four, "Ck", "1"));
        double Orgx = d(txt(four, "Orgx", "0"));
        double Orgy = d(txt(four, "Orgy", "0"));

        // ✅ FIX: autodetect se Ca è in gradi o radianti
        if (Math.abs(Ca) > 0.5) Ca = Math.toRadians(Ca);

        // pivot locali corretti
        double locx0 = Orgx + Cx;
        double locy0 = Orgy + Cy;

        // --- Parametri Height fitting ---
        Element hf = (Element) head.getElementsByTagName("CoordinateSystem_HeightFittingParameter").item(0);
        boolean useHF = isTrue(txt(hf, "Use", "0"));
        double a0 = d(txt(hf, "a0", "0"));
        double a1 = d(txt(hf, "a1", "0"));
        double a2 = d(txt(hf, "a2", "0"));
        double a3 = d(txt(hf, "a3", "0"));
        double a4 = d(txt(hf, "a4", "0"));
        double a5 = d(txt(hf, "a5", "0"));

        return new SpLocalization(geoToProj, use4, Ca, Ck, Orgx, Orgy, locx0, locy0,
                useHF, a0, a1, a2, a3, a4, a5);
    }

    // ------------------------------------------------------------------
    // Trasformazione diretta WGS84 → locale
    // ------------------------------------------------------------------
    @Override
    public void toLocalFast(double lat, double lon, double h, double[] out) {
        ProjCoordinate geo = tlGeo.get();
        ProjCoordinate proj = tlProj.get();

        geo.x = lon; // lon
        geo.y = lat; // lat

        geoToProj.transform(geo, proj);

        double E = proj.x;
        double N = proj.y;

        double X = E;
        double Y = N;

        if (use4) {
            double dN = N - Orgx;
            double dE = E - Orgy;
            double cosA = Math.cos(Ca);
            double sinA = Math.sin(Ca);

            // Rotazione + scala (4-parametri)
            double Np = dN * cosA - dE * sinA;
            double Ep = dN * sinA + dE * cosA;

            double nord = x0 + Ck * Np;
            double est  = y0 + Ck * Ep;

            X = nord;
            Y = est;
        }

        double Z = h;
        if (useHF) {
            double dx = X - x0;
            double dy = Y - y0;
            double dh = a0 + a1 * dx + a2 * dy + a3 * dx * dx + a4 * dy * dy + a5 * dx * dy;
            Z = h - dh;
        }

        out[0] = Y; // Est
        out[1] = X; // Nord
        out[2] = Z; // Quota
    }

    // ------------------------------------------------------------------
    // Helper parsing
    // ------------------------------------------------------------------
    private static double d(String s) {
        return (s == null || s.isEmpty()) ? 0 : Double.parseDouble(s);
    }

    private static String txt(Element p, String tag, String def) {
        if (p == null) return def;
        NodeList nl = p.getElementsByTagName(tag);
        if (nl == null || nl.getLength() == 0) return def;
        String s = nl.item(0).getTextContent();
        return (s == null || s.isEmpty()) ? def : s.trim();
    }

    private static boolean isTrue(String s) {
        return s != null && ("1".equals(s.trim()) || "true".equalsIgnoreCase(s.trim()));
    }
}
