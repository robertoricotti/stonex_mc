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
 * Lettore e trasformatore di file .SP Stonex/SurPad/Cube.
 * Supporta:
 * - Type 221: Transverse Mercator (UTM-like)
 * - Type 162: Lambert Conformal Conic 2SP
 * - 4 parametri (Cx, Cy, Ca, Ck, Orgx, Orgy) con Ca dichiarato in senso orario (SurPad) -> convertito a CCW
 * - Height fitting (Z = h - (a0 + ...))
 * Thread-safe, nessuna allocazione nel path hot.
 */
public final class SpLocalization implements LocalizationModel {

    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateTransformFactory CT_FACTORY = new CoordinateTransformFactory();

    private final CoordinateTransform geoToProj;
    private final CoordinateTransform projToGeo;

    // 4-parametri
    private final boolean use4;
    private final double Cx, Cy, CaCCW, Ck, Orgx, Orgy; // CaCCW = -Ca(SurPad)
    private final double x0, y0; // pivot locali = Org + Cx/Cy

    // Height fitting
    private final boolean useHF;
    private final double a0, a1, a2, a3, a4, a5;

    // Reusable buffers
    private final ThreadLocal<ProjCoordinate> tlGeo = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    private SpLocalization(CoordinateTransform geoToProj,
                           CoordinateTransform projToGeo,
                           boolean use4, double Cx, double Cy, double CaCCW, double Ck, double Orgx, double Orgy,
                           boolean useHF, double a0, double a1, double a2, double a3, double a4, double a5) {
        this.geoToProj = geoToProj;
        this.projToGeo=projToGeo;
        this.use4 = use4;
        this.Cx = Cx;
        this.Cy = Cy;
        this.CaCCW = CaCCW; // già convertito a CCW
        this.Ck = Ck;
        this.Orgx = Orgx;   // N origine proiezione (SurPad)
        this.Orgy = Orgy;   // E origine proiezione (SurPad)
        this.x0 = Orgx + Cx; // Nord locale al pivot
        this.y0 = Orgy + Cy; // Est  locale al pivot
        this.useHF = useHF;
        this.a0 = a0;
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.a4 = a4;
        this.a5 = a5;
    }

    // ------------------- Factory -------------------
    public static SpLocalization fromSpFile(File f) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(f);
        Element head = doc.getDocumentElement();

        // ---- Proiezione ----
        Element proj = (Element) head.getElementsByTagName("CoordinateSystem_ProjectParameter").item(0);
        if (proj == null)
            throw new IllegalArgumentException("SP non valido: manca CoordinateSystem_ProjectParameter");

        double cmRaw = d(txt(proj, "CentralMeridian", "0"));
        double lon0deg = (Math.abs(cmRaw) > 10 ? cmRaw : Math.toDegrees(cmRaw));
        int type = (int) d(txt(proj, "Type", "0"));
        double tk = d(txt(proj, "TK", "1"));
        double Tx = d(txt(proj, "Tx", "0"));   // false northing (UTM Nord emisfero N = 0)
        double Ty = d(txt(proj, "Ty", "0"));   // false easting  (UTM = 500000)
        int south = (int) d(txt(proj, "South", "0"));

        // Fallback lon_0 da Type UTM-band se CentralMeridian = 0
        if (Math.abs(lon0deg) < 1e-9 && type >= 220 && type <= 230) {
            lon0deg = (type - 218) * 6 - 3; // 220→9°, 221→15°, etc.
        }
        // emisfero sud: aggiungi false northing se serve
        if (south == 1 && Tx < 5_000_000) Tx += 10_000_000;

        // LCC 2SP (Type 162) parametri (in SP sono in radianti)
        double refLat = d(txt(proj, "ReferenceLatitude", "0"));
        double par1 = d(txt(proj, "Parallel1", "0"));
        double par2 = d(txt(proj, "Parallel2", "0"));
        double lat0 = radOrDegToDeg(refLat);
        double lat1 = radOrDegToDeg(par1);
        double lat2 = radOrDegToDeg(par2);

        // Ellissoide
        Element ell = (Element) head.getElementsByTagName("CoordinateSystem_EllipsoidParameter").item(0);
        String ellName = ell != null ? txt(ell, "Name", "WGS 84").toUpperCase() : "WGS 84";
        String ellps = ellName.contains("GRS80") ? "GRS80" : "WGS84";

        // Build proj4
        String projDef;
        switch (type) {
            case 162: // Lambert Conformal Conic 2SP
                projDef = String.format(Locale.US,
                        "+proj=lcc +lat_1=%.10f +lat_2=%.10f +lat_0=%.10f +lon_0=%.10f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lat1, lat2, lat0, lon0deg, Ty, Tx, ellps);
                break;

            case 151: // Mercator
                projDef = String.format(Locale.US,
                        "+proj=merc +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lon0deg, tk, Ty, Tx, ellps);
                break;

            case 180: // Oblique Mercator
                double azimuth = Math.toDegrees(d(txt(proj, "Azimuth", "0")));
                projDef = String.format(Locale.US,
                        "+proj=omerc +lat_0=0 +lonc=%.10f +alpha=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lon0deg, azimuth, tk, Ty, Tx, ellps);
                break;

            case 182: // Stereographic
                projDef = String.format(Locale.US,
                        "+proj=stere +lat_0=%.10f +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lat0, lon0deg, tk, Ty, Tx, ellps);
                break;

            case 183: // Lambert Azimuthal Equal Area
                projDef = String.format(Locale.US,
                        "+proj=laea +lat_0=%.10f +lon_0=%.10f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lat0, lon0deg, Ty, Tx, ellps);
                break;
            case 12000:
            case 12001:
            case 12002:
            case 12003:
            case 12004:
            case 12005:
            case 12006:
            case 12007:
            case 12008:
            case 12009:
            case 12010:
            case 12011:
            case 12012:
            case 12013:
            case 12014:
            case 12015:
            case 12016:
            case 12017:
            case 12018:
            case 12019:

                // Japanese Plane Rectangular Coordinate System (JGD2011)
                // Basato su Transverse Mercator, ellissoide GRS80
                // Parametri già forniti nel file .SP in radianti
                double lon0JGD = radOrDegToDeg(cmRaw);
                double lat0JGD = radOrDegToDeg(refLat);
                double kJGD = (tk == 0.0 ? 0.9999 : tk);

                projDef = String.format(Locale.US,
                        "+proj=tmerc +lat_0=%.10f +lon_0=%.10f +k_0=%.6f +x_0=%.3f +y_0=%.3f +ellps=GRS80 +units=m +no_defs",
                        lat0JGD, lon0JGD, kJGD, Ty, Tx);
                break;

            case 10800:
            case 10801:
            case 10802:
            case 10803:
            case 10804:
            case 10805:
            case 10806:
            case 10807:
            case 10808:
            case 10809:
            case 10900:
            case 10901:
            case 10902:
            case 10903:
            case 10904:
            case 10905:
            case 10906:
            case 10907:
            case 10908:
            case 10909:
            case 10880:
            case 10881:
            case 10882:
            case 10883:
            case 10884:
            case 10885:
            case 10886:
            case 10887:
            case 10888:
            case 10889:
            case 10890:
            case 10891:
            case 10892:
            case 10893:
            case 10894:
            case 10895:
            case 10896:
            case 10897:
            case 10898:
            case 10899:

                // Canada MTM zones (NAD83 / CSRS)
                double lon0MTM = radOrDegToDeg(cmRaw);
                double lat0MTM = radOrDegToDeg(refLat);
                double kMTM = (tk == 0.0 ? 0.9999 : tk);
                double fe = Ty; // 304800 m (984250 ft)
                double fn = Tx;

                projDef = String.format(Locale.US,
                        "+proj=tmerc +lat_0=%.10f +lon_0=%.10f +k_0=%.6f +x_0=%.3f +y_0=%.3f +ellps=GRS80 +units=m +no_defs",
                        lat0MTM, lon0MTM, kMTM, fe, fn);
                break;


            default: // Transverse Mercator (UTM-like o custom)
                projDef = String.format(Locale.US,
                        "+proj=tmerc +lat_0=0 +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                        lon0deg, tk, Ty, Tx, ellps);
                break;

        }


        CoordinateReferenceSystem wgs84 = CRS_FACTORY.createFromParameters("WGS84", "+proj=longlat +datum=WGS84 +no_defs");
        CoordinateReferenceSystem projected = CRS_FACTORY.createFromParameters("SP_PROJ", projDef);
        CoordinateTransform geoToProj = CT_FACTORY.createTransform(wgs84, projected);
        CoordinateTransform projToGeo = CT_FACTORY.createTransform(projected, wgs84);

        // ---- 4 parametri ----
        Element four = (Element) head.getElementsByTagName("CoordinateSystem_FourParameter").item(0);
        boolean use4 = false;
        double Cx = 0, Cy = 0, Ca = 0, Ck = 1, Orgx = 0, Orgy = 0;
        if (four != null) {
            use4 = isTrue(txt(four, "Use", "0"));
            Cx = d(txt(four, "Cx", "0"));
            Cy = d(txt(four, "Cy", "0"));
            Ca = d(txt(four, "Ca", "0"));  // SurPad: CW
            Ck = d(txt(four, "Ck", "1"));
            Orgx = d(txt(four, "Orgx", "0")); // N (surpad)
            Orgy = d(txt(four, "Orgy", "0")); // E (surpad)
        }
        // Se Ca è in gradi, convertilo. Poi inverti di segno: CW -> CCW
        if (Math.abs(Ca) > 0.5) Ca = Math.toRadians(Ca);
        double CaCCW = -Ca;

        // ---- Height fitting ----
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

        return new SpLocalization(geoToProj,projToGeo, use4, Cx, Cy, CaCCW, Ck, Orgx, Orgy,
                useHF, a0, a1, a2, a3, a4, a5);
    }

    // ------------------- Transform -------------------
    @Override
    public void toLocalFast(double lat, double lon, double h, double[] out) {
        ProjCoordinate g = tlGeo.get();
        ProjCoordinate p = tlProj.get();
        g.x = lon;
        g.y = lat;
        geoToProj.transform(g, p);

        double E = p.x;
        double N = p.y;

        if (use4) {
            // SurPad: Orgx = Nord, Orgy = Est
            double dE = E - Orgy;
            double dN = N - Orgx;

            // Rotazione CCW (CaCCW) + scala
            double cosA = Math.cos(CaCCW);
            double sinA = Math.sin(CaCCW);

            // Applica su vettore [dE, dN]
            double Ep = dE * cosA - dN * sinA;
            double Np = dE * sinA + dN * cosA;

            // Shift ai pivot locali
            E = (Orgy + Cy) + Ck * Ep;   // Est locale
            N = (Orgx + Cx) + Ck * Np;   // Nord locale
        }

        double Z = h;
        if (useHF) {
            // Fitting definito su locali: Z = h - (a0 + a1*dx + a2*dy + a3*dx*dx + a4*dy*dy + a5*dx*dy)
            double dx = N - (Orgx + Cx); // rispetto al pivot locale (Nord)
            double dy = E - (Orgy + Cy); // rispetto al pivot locale (Est)
            double dh = a0 + a1 * dx + a2 * dy + a3 * dx * dx + a4 * dy * dy + a5 * dx * dy;
            Z = h - dh;
        }

        out[0] = E; // Est
        out[1] = N; // Nord
        out[2] = Z; // Quota
    }

    @Override
    public void toGeoFast(double E, double N, double H, double[] out) {
        ProjCoordinate p = tlProj.get();
        ProjCoordinate g = tlGeo.get();

        double e = E;
        double n = N;

// 1️⃣ Inverso dell'Height fitting (usa E,N locali)
        double hEll = H;
        if (useHF) {
            double dx = n - (Orgx + Cx);
            double dy = e - (Orgy + Cy);
            double dh = a0 + a1 * dx + a2 * dy + a3 * dx * dx + a4 * dy * dy + a5 * dx * dy;
            hEll = H + dh;
        }

// 2️⃣ Inverso del 4-parametri (rotazione / scala / traslazione)
        if (use4) {
            double dE = (e - (Orgy + Cy)) / Ck;
            double dN = (n - (Orgx + Cx)) / Ck;

            double cosA = Math.cos(CaCCW);
            double sinA = Math.sin(CaCCW);

            // rotazione inversa (CCW invertita)
            double Ei = dE * cosA + dN * sinA;
            double Ni = -dE * sinA + dN * cosA;

            e = Orgy + Ei;
            n = Orgx + Ni;
        }

        // 3️⃣ Inverso proiezione: locale → WGS84
        p.x = e;
        p.y = n;
        projToGeo.transform(p, g);

        out[0] = g.y; // lat
        out[1] = g.x; // lon
        out[2] = hEll; // ellipsoid height
    }


    // ------------------- Helpers -------------------
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

    private static double radOrDegToDeg(double v) {
        // Heuristica: in SP questi valori sono quasi sempre in radianti.
        if (Math.abs(v) <= Math.PI + 1e-9) return Math.toDegrees(v);
        return v; // già in gradi
    }

}
