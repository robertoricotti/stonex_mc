package packexcalib.gnss;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import gui.MyApp;
import packexcalib.exca.DataSaved;

/**
 * SpLocalization (Stonex / SurPad / Cube .SP)
 *
 * Convenzioni adottate:
 * - Input geo: lat/lon in gradi, quota ellissoidale
 * - Output locale: E, N, Z
 * - PROJ usa x=lon, y=lat
 *
 * Parsing:
 * - robusto su Android
 * - tollera decimal comma
 * - tollera NaN / tag mancanti
 *
 * Coordinate:
 * - proiezione SP -> grid E/N
 * - opzionale 4-parametri SP (Cx, Cy, Ca, Ck, Orgx, Orgy)
 * - opzionale height fitting con origine x0/y0
 *
 * Heading:
 * - su indicazione SurPad, Ca è l'angolo da applicare direttamente all'HDT
 * - Ca nel file è in radianti
 * - out[3] = deltaHeadingDeg = degrees(Ca)
 *
 * Quindi:
 *   headingLocal = wrap360(headingTrue + out[3])
 */
public final class SpLocalization implements LocalizationModel {

    private static final String WGS84_DEF =
            "+proj=longlat +datum=WGS84 +no_defs +type=crs";

    private final NativeProjTransformer geoToProj;
    private final NativeProjTransformer projToGeo;

    // 4 parametri
    private final boolean use4;
    private final double Cx, Cy, CaRad, Ck, Orgx, Orgy;
    private final double cosA, sinA;

    // heading
    private final double headingDeltaDeg;

    // Height fitting
    private final boolean useHF;
    private final double a0, a1, a2, a3, a4, a5;
    private final double hfX0, hfY0;

    private SpLocalization(NativeProjTransformer geoToProj,
                           NativeProjTransformer projToGeo,
                           boolean use4,
                           double Cx, double Cy, double CaRad, double Ck, double Orgx, double Orgy,
                           double headingDeltaDeg,
                           boolean useHF,
                           double a0, double a1, double a2, double a3, double a4, double a5,
                           double hfX0, double hfY0) {

        this.geoToProj = geoToProj;
        this.projToGeo = projToGeo;

        this.use4 = use4;
        this.Cx = Cx;
        this.Cy = Cy;
        this.CaRad = CaRad;
        this.Ck = (Double.isFinite(Ck) && Math.abs(Ck) > 1e-12) ? Ck : 1.0;
        this.Orgx = Orgx; // N
        this.Orgy = Orgy; // E

        this.cosA = Math.cos(CaRad);
        this.sinA = Math.sin(CaRad);

        this.headingDeltaDeg = headingDeltaDeg;

        this.useHF = useHF;
        this.a0 = a0;
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.a4 = a4;
        this.a5 = a5;
        this.hfX0 = hfX0; // origine Nord del polinomio quota
        this.hfY0 = hfY0; // origine Est del polinomio quota
    }

    // ---------------------------------------------------------------------
    // Factory
    // ---------------------------------------------------------------------
    public static SpLocalization fromSpFile(File f) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // hardening parser Android / anti-XXE best effort
        try { dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Throwable ignored) {}
        try { dbf.setFeature("http://xml.org/sax/features/external-general-entities", false); } catch (Throwable ignored) {}
        try { dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false); } catch (Throwable ignored) {}
        try { dbf.setXIncludeAware(false); } catch (Throwable ignored) {}
        try { dbf.setExpandEntityReferences(false); } catch (Throwable ignored) {}

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(f);
        Element head = doc.getDocumentElement();
        if (head == null) {
            throw new IllegalArgumentException("SP non valido: root mancante");
        }

        Element cs = (Element) head.getElementsByTagName("CoordinateSystem").item(0);
        if (cs == null) {
            cs = head;
        }

        // ------------------ Proiezione ------------------
        Element proj = (Element) cs.getElementsByTagName("CoordinateSystem_ProjectParameter").item(0);
        if (proj == null) {
            throw new IllegalArgumentException("SP non valido: manca CoordinateSystem_ProjectParameter");
        }

        int type = (int) d(txt(proj, "Type", "0"));
        double tk = d(txt(proj, "TK", "1"));

        // in SP spesso Tx=N, Ty=E
        double Tx = d(txt(proj, "Tx", "0")); // false northing
        double Ty = d(txt(proj, "Ty", "0")); // false easting
        int south = (int) d(txt(proj, "South", "0"));

        double cmRaw = d(txt(proj, "CentralMeridian", "0"));
        double lon0deg = radOrDegToDeg(cmRaw);

        // fallback UTM-like se lon0 non valorizzato
        if (Math.abs(lon0deg) < 1e-12 && type >= 220 && type <= 230) {
            lon0deg = (type - 218) * 6.0 - 3.0;
        }

        if (south == 1 && Tx < 5_000_000) {
            Tx += 10_000_000;
        }

        double refLatRaw = d(txt(proj, "ReferenceLatitude", "0"));
        double par1Raw = d(txt(proj, "Parallel1", "0"));
        double par2Raw = d(txt(proj, "Parallel2", "0"));
        double lat0deg = radOrDegToDeg(refLatRaw);
        double lat1deg = radOrDegToDeg(par1Raw);
        double lat2deg = radOrDegToDeg(par2Raw);

        double azRaw = d(txt(proj, "Azimuth", "0"));
        double azDeg = radOrDegToDeg(azRaw);

        Element ell = (Element) cs.getElementsByTagName("CoordinateSystem_EllipsoidParameter").item(0);
        String ellName = ell != null ? txt(ell, "Name", "WGS 84") : "WGS 84";
        String ellUp = ellName.toUpperCase(Locale.ROOT);
        String ellps = (ellUp.contains("GRS80") || ellUp.contains("GRS 80")) ? "GRS80" : "WGS84";

        // PROJ usa x_0=easting, y_0=northing -> x_0=Ty, y_0=Tx
        String projDef;
        switch (type) {
            case 162: // Lambert Conformal Conic 2SP
                projDef = String.format(Locale.US,
                        "+proj=lcc +lat_1=%.10f +lat_2=%.10f +lat_0=%.10f +lon_0=%.10f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs +type=crs",
                        lat1deg, lat2deg, lat0deg, lon0deg, Ty, Tx, ellps);
                break;

            case 151: // Mercator
                projDef = String.format(Locale.US,
                        "+proj=merc +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs +type=crs",
                        lon0deg, tk, Ty, Tx, ellps);
                break;

            case 180: // Oblique Mercator
                projDef = String.format(Locale.US,
                        "+proj=omerc +lat_0=0 +lonc=%.10f +alpha=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs +type=crs",
                        lon0deg, azDeg, tk, Ty, Tx, ellps);
                break;

            case 182: // Stereographic
                projDef = String.format(Locale.US,
                        "+proj=stere +lat_0=%.10f +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs +type=crs",
                        lat0deg, lon0deg, tk, Ty, Tx, ellps);
                break;

            case 183: // Lambert Azimuthal Equal Area
                projDef = String.format(Locale.US,
                        "+proj=laea +lat_0=%.10f +lon_0=%.10f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs +type=crs",
                        lat0deg, lon0deg, Ty, Tx, ellps);
                break;

            default: // Transverse Mercator / UTM-like
                projDef = String.format(Locale.US,
                        "+proj=tmerc +lat_0=0 +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs +type=crs",
                        lon0deg, tk, Ty, Tx, ellps);
                break;
        }

        Context ctx = requireAppContext();

        NativeProjTransformer geoToProj = new NativeProjTransformer();
        geoToProj.init(ctx);
        geoToProj.initFromParameters(WGS84_DEF, projDef);

        NativeProjTransformer projToGeo = new NativeProjTransformer();
        projToGeo.init(ctx);
        projToGeo.initFromParameters(projDef, WGS84_DEF);

        // ------------------ 4 parametri ------------------
        Element four = (Element) cs.getElementsByTagName("CoordinateSystem_FourParameter").item(0);

        boolean use4 = false;
        double Cx = 0, Cy = 0, CaRaw = 0, Ck = 1, Orgx = 0, Orgy = 0;

        if (four != null) {
            use4 = isTrue(txt(four, "Use", "0"));
            Cx = d(txt(four, "Cx", "0"));
            Cy = d(txt(four, "Cy", "0"));
            CaRaw = d(txt(four, "Ca", "0"));
            Ck = d(txt(four, "Ck", "1"));
            Orgx = d(txt(four, "Orgx", "0")); // N
            Orgy = d(txt(four, "Orgy", "0")); // E
        }

        double CaRad = (Math.abs(CaRaw) <= (2.0 * Math.PI + 1e-12))
                ? CaRaw
                : Math.toRadians(CaRaw);

        double headingDeltaDeg = use4 ? Math.toDegrees(CaRad) : 0.0;
        DataSaved.DELTA_HDT_SMC = headingDeltaDeg;

        // ------------------ Height fitting ------------------
        Element hf = (Element) cs.getElementsByTagName("CoordinateSystem_HeightFittingParameter").item(0);

        boolean useHF = false;
        double a0 = 0, a1 = 0, a2 = 0, a3 = 0, a4 = 0, a5 = 0;
        double hfX0 = 0, hfY0 = 0;

        if (hf != null) {
            useHF = isTrue(txt(hf, "Use", "0"));
            a0 = d(txt(hf, "a0", "0"));
            a1 = d(txt(hf, "a1", "0"));
            a2 = d(txt(hf, "a2", "0"));
            a3 = d(txt(hf, "a3", "0"));
            a4 = d(txt(hf, "a4", "0"));
            a5 = d(txt(hf, "a5", "0"));
            hfX0 = d(txt(hf, "x0", "0"));
            hfY0 = d(txt(hf, "y0", "0"));
        }

        return new SpLocalization(
                geoToProj, projToGeo,
                use4, Cx, Cy, CaRad, Ck, Orgx, Orgy,
                headingDeltaDeg,
                useHF, a0, a1, a2, a3, a4, a5, hfX0, hfY0
        );
    }

    private static Context requireAppContext() {
        if (MyApp.visibleActivity == null || MyApp.visibleActivity.getApplicationContext() == null) {
            throw new IllegalStateException("Context Android non disponibile per inizializzare PROJ");
        }
        return MyApp.visibleActivity.getApplicationContext();
    }

    // ---------------------------------------------------------------------
    // Trasformazioni
    // ---------------------------------------------------------------------
    @Override
    public void toLocalFast(double latDeg, double lonDeg, double hEll, double[] out) {
        double[] p = geoToProj.transformPrepared(lonDeg, latDeg, hEll);

        double E = p[0];
        double N = p[1];

        if (use4) {
            // SurPad: Orgx=N, Orgy=E
            double dE = E - Orgy;
            double dN = N - Orgx;

            double Ep = dE * cosA + dN * sinA;
            double Np = -dE * sinA + dN * cosA;

            E = (Orgy + Cy) + Ck * Ep;
            N = (Orgx + Cx) + Ck * Np;
        }

        double Zloc = hEll;
        if (useHF) {
            double dx = N - hfX0; // north
            double dy = E - hfY0; // east
            double dh = a0 + a1 * dx + a2 * dy + a3 * dx * dx + a4 * dy * dy + a5 * dx * dy;
            Zloc = hEll - dh;
        }

        out[0] = E;
        out[1] = N;
        out[2] = Zloc;
    }

    @Override
    public void toGeoFast(double Eloc, double Nloc, double Zloc, double[] out) {
        double e = Eloc;
        double n = Nloc;

        // 1) inverso height fitting
        double hEll = Zloc;
        if (useHF) {
            double dx = n - hfX0; // north
            double dy = e - hfY0; // east
            double dh = a0 + a1 * dx + a2 * dy + a3 * dx * dx + a4 * dy * dy + a5 * dx * dy;
            hEll = Zloc + dh;
        }

        // 2) inverso 4-parametri
        if (use4) {
            double dE = (e - (Orgy + Cy)) / Ck;
            double dN = (n - (Orgx + Cx)) / Ck;

            double Ei = dE * cosA - dN * sinA;
            double Ni = dE * sinA + dN * cosA;

            e = Orgy + Ei;
            n = Orgx + Ni;
        }

        // 3) proj -> geo
        double[] g = projToGeo.transformPrepared(e, n, hEll);

        out[0] = g[1]; // lat
        out[1] = g[0]; // lon
        out[2] = hEll;
    }

    @Override
    public void toLocalFastWithHeadingDelta(double latDeg, double lonDeg, double hEll, double[] out) {
        toLocalFast(latDeg, lonDeg, hEll, out);
        if (out.length > 3) {
            out[3] = headingDeltaDeg;
        }
    }

    public double getHeadingDeltaDeg() {
        return headingDeltaDeg;
    }

    public boolean isUsingFourParameters() {
        return use4;
    }

    public boolean isUsingHeightFitting() {
        return useHF;
    }

    public double getHfX0() {
        return hfX0;
    }

    public double getHfY0() {
        return hfY0;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private static double d(String s) {
        if (s == null) return 0.0;
        s = s.trim();
        if (s.isEmpty()) return 0.0;
        s = s.replace(',', '.');

        try {
            double v = Double.parseDouble(s);
            return Double.isFinite(v) ? v : 0.0;
        } catch (Throwable t) {
            return 0.0;
        }
    }

    private static String txt(Element p, String tag, String def) {
        if (p == null) return def;
        NodeList nl = p.getElementsByTagName(tag);
        if (nl == null || nl.getLength() == 0) return def;

        String s = nl.item(0).getTextContent();
        if (s == null) return def;

        s = s.trim();
        return s.isEmpty() ? def : s;
    }

    private static boolean isTrue(String s) {
        if (s == null) return false;
        s = s.trim();
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s);
    }

    /**
     * Se |v| <= 2π assume radianti, altrimenti gradi.
     */
    private static double radOrDegToDeg(double v) {
        if (!Double.isFinite(v)) return 0.0;
        return (Math.abs(v) <= (2.0 * Math.PI + 1e-12)) ? Math.toDegrees(v) : v;
    }
}