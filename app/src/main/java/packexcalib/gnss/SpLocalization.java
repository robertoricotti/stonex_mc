package packexcalib.gnss;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * SpLocalization (Stonex / SurPad / Cube .SP)
 *
 * Convenzioni adottate:
 * - Input geo: lat/lon in gradi, quota ellissoidale
 * - Output locale: E, N, Z
 * - PROJ4J usa x=lon, y=lat
 *
 * Parsing:
 * - robusto su Android (setFeature / setXIncludeAware / setExpandEntityReferences in try/catch)
 * - tollera decimal comma
 * - tollera NaN / tag mancanti
 *
 * Coordinate:
 * - proiezione SP -> grid E/N
 * - opzionale 4-parametri SP (Cx, Cy, Ca, Ck, Orgx, Orgy)
 * - opzionale height fitting
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

    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateTransformFactory CT_FACTORY = new CoordinateTransformFactory();

    private final CoordinateTransform geoToProj;
    private final CoordinateTransform projToGeo;

    // 4 parametri
    private final boolean use4;
    private final double Cx, Cy, CaRad, Ck, Orgx, Orgy;
    private final double cosA, sinA;

    // heading
    private final double headingDeltaDeg;

    // Height fitting
    private final boolean useHF;
    private final double a0, a1, a2, a3, a4, a5;

    // buffer riusabili
    private final ThreadLocal<ProjCoordinate> tlGeo  = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    private SpLocalization(CoordinateTransform geoToProj,
                           CoordinateTransform projToGeo,
                           boolean use4,
                           double Cx, double Cy, double CaRad, double Ck, double Orgx, double Orgy,
                           double headingDeltaDeg,
                           boolean useHF,
                           double a0, double a1, double a2, double a3, double a4, double a5) {

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

        // ------------------ Proiezione ------------------
        Element proj = (Element) head.getElementsByTagName("CoordinateSystem_ProjectParameter").item(0);
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
        double par1Raw   = d(txt(proj, "Parallel1", "0"));
        double par2Raw   = d(txt(proj, "Parallel2", "0"));
        double lat0deg   = radOrDegToDeg(refLatRaw);
        double lat1deg   = radOrDegToDeg(par1Raw);
        double lat2deg   = radOrDegToDeg(par2Raw);

        double azRaw = d(txt(proj, "Azimuth", "0"));
        double azDeg = radOrDegToDeg(azRaw);

        Element ell = (Element) head.getElementsByTagName("CoordinateSystem_EllipsoidParameter").item(0);
        String ellName = ell != null ? txt(ell, "Name", "WGS 84") : "WGS 84";
        String ellUp = ellName.toUpperCase(Locale.ROOT);
        String ellps = (ellUp.contains("GRS80") || ellUp.contains("GRS 80")) ? "GRS80" : "WGS84";

        // PROJ usa x_0=easting, y_0=northing -> x_0=Ty, y_0=Tx
        String projDef;
        switch (type) {
            case 162: // Lambert Conformal Conic 2SP
                projDef = String.format(Locale.US,
                        "+proj=lcc +lat_1=%.10f +lat_2=%.10f +lat_0=%.10f +lon_0=%.10f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lat1deg, lat2deg, lat0deg, lon0deg, Ty, Tx, ellps);
                break;

            case 151: // Mercator
                projDef = String.format(Locale.US,
                        "+proj=merc +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lon0deg, tk, Ty, Tx, ellps);
                break;

            case 180: // Oblique Mercator
                projDef = String.format(Locale.US,
                        "+proj=omerc +lat_0=0 +lonc=%.10f +alpha=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lon0deg, azDeg, tk, Ty, Tx, ellps);
                break;

            case 182: // Stereographic
                projDef = String.format(Locale.US,
                        "+proj=stere +lat_0=%.10f +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lat0deg, lon0deg, tk, Ty, Tx, ellps);
                break;

            case 183: // Lambert Azimuthal Equal Area
                projDef = String.format(Locale.US,
                        "+proj=laea +lat_0=%.10f +lon_0=%.10f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lat0deg, lon0deg, Ty, Tx, ellps);
                break;

            default: // Transverse Mercator / UTM-like
                projDef = String.format(Locale.US,
                        "+proj=tmerc +lat_0=0 +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lon0deg, tk, Ty, Tx, ellps);
                break;
        }

        CoordinateReferenceSystem wgs84 = CRS_FACTORY.createFromParameters(
                "WGS84",
                "+proj=longlat +datum=WGS84 +no_defs"
        );
        CoordinateReferenceSystem projected = CRS_FACTORY.createFromParameters("SP_PROJ", projDef);

        CoordinateTransform geoToProj = CT_FACTORY.createTransform(wgs84, projected);
        CoordinateTransform projToGeo = CT_FACTORY.createTransform(projected, wgs84);

        // ------------------ 4 parametri ------------------
        Element four = (Element) head.getElementsByTagName("CoordinateSystem_FourParameter").item(0);

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

        // Ca nel file: radianti se piccolo, altrimenti gradi
        double CaRad = (Math.abs(CaRaw) <= (2.0 * Math.PI + 1e-12))
                ? CaRaw
                : Math.toRadians(CaRaw);

        // SurPad: Ca da convertire in gradi e sommare direttamente all'HDT
        double headingDeltaDeg = use4 ? Math.toDegrees(CaRad) : 0.0;

        // ------------------ Height fitting ------------------
        Element hf = (Element) head.getElementsByTagName("CoordinateSystem_HeightFittingParameter").item(0);

        boolean useHF = false;
        double a0 = 0, a1 = 0, a2 = 0, a3 = 0, a4 = 0, a5 = 0;

        if (hf != null) {
            useHF = isTrue(txt(hf, "Use", "0"));
            a0 = d(txt(hf, "a0", "0"));
            a1 = d(txt(hf, "a1", "0"));
            a2 = d(txt(hf, "a2", "0"));
            a3 = d(txt(hf, "a3", "0"));
            a4 = d(txt(hf, "a4", "0"));
            a5 = d(txt(hf, "a5", "0"));
        }

        return new SpLocalization(
                geoToProj, projToGeo,
                use4, Cx, Cy, CaRad, Ck, Orgx, Orgy,
                headingDeltaDeg,
                useHF, a0, a1, a2, a3, a4, a5
        );
    }

    // ---------------------------------------------------------------------
    // Trasformazioni
    // ---------------------------------------------------------------------
    @Override
    public void toLocalFast(double latDeg, double lonDeg, double hEll, double[] out) {
        ProjCoordinate g = tlGeo.get();
        ProjCoordinate p = tlProj.get();

        g.x = lonDeg;
        g.y = latDeg;
        geoToProj.transform(g, p);

        double E = p.x;
        double N = p.y;

        if (use4) {
            // SurPad: Orgx=N, Orgy=E
            double dE = E - Orgy;
            double dN = N - Orgx;

            // Applico la rotazione/similarità così come definita dal file SP
            double Ep = dE * cosA - dN * sinA;
            double Np = dE * sinA + dN * cosA;

            E = (Orgy + Cy) + Ck * Ep;
            N = (Orgx + Cx) + Ck * Np;
        }

        double Zloc = hEll;
        if (useHF) {
            double dx = N - (Orgx + Cx);
            double dy = E - (Orgy + Cy);
            double dh = a0 + a1 * dx + a2 * dy + a3 * dx * dx + a4 * dy * dy + a5 * dx * dy;
            Zloc = hEll - dh;
        }

        out[0] = E;
        out[1] = N;
        out[2] = Zloc;
    }

    @Override
    public void toGeoFast(double Eloc, double Nloc, double Zloc, double[] out) {
        ProjCoordinate p = tlProj.get();
        ProjCoordinate g = tlGeo.get();

        double e = Eloc;
        double n = Nloc;

        // 1) inverso height fitting
        double hEll = Zloc;
        if (useHF) {
            double dx = n - (Orgx + Cx);
            double dy = e - (Orgy + Cy);
            double dh = a0 + a1 * dx + a2 * dy + a3 * dx * dx + a4 * dy * dy + a5 * dx * dy;
            hEll = Zloc + dh;
        }

        // 2) inverso 4-parametri
        if (use4) {
            double dE = (e - (Orgy + Cy)) / Ck;
            double dN = (n - (Orgx + Cx)) / Ck;

            double Ei = dE * cosA + dN * sinA;
            double Ni = -dE * sinA + dN * cosA;

            e = Orgy + Ei;
            n = Orgx + Ni;
        }

        // 3) proj -> geo
        p.x = e;
        p.y = n;
        projToGeo.transform(p, g);

        out[0] = g.y; // lat
        out[1] = g.x; // lon
        out[2] = hEll;
    }

    /**
     * out[3] = delta in gradi da sommare all'HDT true:
     *
     * headingLocal = wrap360(headingTrue + out[3])
     *
     * Su conferma SurPad: delta = degrees(Ca)
     */
    @Override
    public void toLocalFastWithHeadingDelta(double latDeg, double lonDeg, double hEll, double[] out) {
        toLocalFast(latDeg, lonDeg, hEll, out);
        if (out.length > 3) {
            out[3] = headingDeltaDeg;
        }
    }

    // opzionale, utile per debug/UI
    public double getHeadingDeltaDeg() {
        return headingDeltaDeg;
    }

    public boolean isUsingFourParameters() {
        return use4;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private static double d(String s) {
        if (s == null) return 0.0;
        s = s.trim();
        if (s.isEmpty()) return 0.0;
        s = s.replace(',', '.');

        double v = Double.parseDouble(s);
        return Double.isFinite(v) ? v : 0.0;
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