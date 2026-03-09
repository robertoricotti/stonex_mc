package packexcalib.gnss;

import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Localizzazione da file .LOC appoggiata a un CRS proiettato di base.
 *
 * Modello:
 *   WGS84 -> CRS proiettato (grid E/N) -> similarità 2D -> sistema locale LOC
 *
 * Comportamento voluto:
 * - se cambi il CRS sorgente, cambia anche la rotazione finale
 * - il delta heading NON varia con la posizione GNSS corrente
 * - il delta heading è costante sul progetto:
 *
 *      delta = gamma0 - theta
 *
 *   dove:
 *   - gamma0 = convergenza meridiana del CRS sorgente nel centro della rete LOC
 *   - theta  = rotazione CCW della similarità Grid -> Local
 *
 * Convenzioni:
 * - headingTrue: 0=N, 90=E
 * - headingLocal = wrap360(headingTrue + delta)
 *
 * Output locale:
 * - out[0] = E locale
 * - out[1] = N locale
 * - out[2] = Z locale
 * - out[3] = delta heading (gradi)
 */
public final class ProjectedLocLocalization implements LocalizationModel {

    private final CoordinateTransform geoToProj;
    private final CoordinateTransform projToGeo;

    // Similarità 2D: Local = s * R(theta) * Grid + t
    private final double s, cosT, sinT, tE, tN;

    // Delta heading costante del progetto
    private final double gamma0Deg;

    // Fit quota
    private final boolean useZ;
    private final double E0, N0;
    private final double k0, k1, k2, k3, k4, k5;
    private final double polyRadiusM;

    // Buffer riusabili
    private final ThreadLocal<ProjCoordinate> tlGeo  = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    private ProjectedLocLocalization(CoordinateTransform geoToProj,
                                     CoordinateTransform projToGeo,
                                     double s, double cosT, double sinT, double tE, double tN,
                                     double gamma0Deg,
                                     boolean useZ, double E0, double N0,
                                     double k0, double k1, double k2, double k3, double k4, double k5,
                                     double polyRadiusM) {
        this.geoToProj = geoToProj;
        this.projToGeo = projToGeo;

        if (!Double.isFinite(s) || Math.abs(s) < 1e-12) {
            throw new IllegalArgumentException("LOC: scala XY non valida (s=" + s + ")");
        }

        this.s = s;
        this.cosT = cosT;
        this.sinT = sinT;
        this.tE = tE;
        this.tN = tN;
        this.gamma0Deg = gamma0Deg;

        this.useZ = useZ;
        this.E0 = E0;
        this.N0 = N0;
        this.k0 = k0;
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.k4 = k4;
        this.k5 = k5;
        this.polyRadiusM = (Double.isFinite(polyRadiusM) && polyRadiusM > 0.0) ? polyRadiusM : 300.0;
    }

    // ---------------------------------------------------------------------
    // Factory
    // ---------------------------------------------------------------------
    public static ProjectedLocLocalization fromLocFile(File locFile,
                                                       CoordinateTransform geoToProj,
                                                       CoordinateTransform projToGeo) throws Exception {
        if (geoToProj == null || projToGeo == null) {
            throw new IllegalArgumentException("Transform geo<->proj null.");
        }

        final List<RawPoint> pts = parseLocFile(locFile);
        if (pts.size() < 2) {
            throw new IllegalArgumentException("Il file LOC deve contenere almeno 2 punti validi.");
        }

        final int n = pts.size();

        double[] E = new double[n];
        double[] N = new double[n];
        double[] EL = new double[n];
        double[] NL = new double[n];
        double[] ZL = new double[n];
        double[] Hell = new double[n];
        boolean[] useH = new boolean[n];
        boolean[] useV = new boolean[n];

        ProjCoordinate g = new ProjCoordinate();
        ProjCoordinate p = new ProjCoordinate();

        for (int i = 0; i < n; i++) {
            RawPoint rp = pts.get(i);

            g.x = rp.lon;
            g.y = rp.lat;
            g.z = rp.hEll;
            geoToProj.transform(g, p);

            E[i] = p.x;
            N[i] = p.y;
            EL[i] = rp.eLocal;
            NL[i] = rp.nLocal;
            ZL[i] = rp.zLocal;
            Hell[i] = rp.hEll;
            useH[i] = rp.useH;
            useV[i] = rp.useV;
        }

        // Fit orizzontale
        Similarity sim = fitSimilarityWeighted(E, N, EL, NL, useH);

        // Centro rete per quota
        double E0 = meanWeighted(E, useV);
        double N0 = meanWeighted(N, useV);

        // Centro rete per heading/convergenza: meglio sui punti orizzontali
        double Eh = meanWeighted(E, useH);
        double Nh = meanWeighted(N, useH);

        // Calcolo una volta sola la convergenza nel centro della rete
        double gamma0Deg = meridianConvergenceDegAtProjectedPoint(Eh, Nh, projToGeo, geoToProj);

        // Fit quota
        boolean useZ = false;
        double k0 = 0, k1 = 0, k2 = 0, k3 = 0, k4 = 0, k5 = 0;

        if (hasAny(useV)) {
            double[] dZ = new double[n];
            double[] w = new double[n];
            for (int i = 0; i < n; i++) {
                dZ[i] = ZL[i] - Hell[i];
                w[i] = useV[i] ? 1.0 : 0.0;
            }

            double[] coeff = fitPoly2DZ_Ridge(E, N, dZ, w, E0, N0, 1e-12);
            k0 = coeff[0];
            k1 = coeff[1];
            k2 = coeff[2];
            k3 = coeff[3];
            k4 = coeff[4];
            k5 = coeff[5];
            useZ = true;
        }

        // Guard-rail per quota
        double maxBL = 0.0;
        List<Integer> idxH = indicesTrue(useH);
        if (idxH.size() >= 2) {
            for (int i = 0; i < idxH.size(); i++) {
                int ii = idxH.get(i);
                for (int j = i + 1; j < idxH.size(); j++) {
                    int jj = idxH.get(j);
                    double d = Math.hypot(E[ii] - E[jj], N[ii] - N[jj]);
                    if (d > maxBL) maxBL = d;
                }
            }
        }
        double polyRadius = Math.max(300.0, 2.0 * maxBL);

        return new ProjectedLocLocalization(
                geoToProj, projToGeo,
                sim.s, sim.cosT, sim.sinT, sim.tE, sim.tN,
                gamma0Deg,
                useZ, E0, N0, k0, k1, k2, k3, k4, k5,
                polyRadius
        );
    }

    // ---------------------------------------------------------------------
    // Transform
    // ---------------------------------------------------------------------
    @Override
    public void toLocalFast(double latDeg, double lonDeg, double hEll, double[] out) {
        ProjCoordinate g = tlGeo.get();
        ProjCoordinate p = tlProj.get();

        // WGS84 -> CRS sorgente
        g.x = lonDeg;
        g.y = latDeg;
        g.z = hEll;
        geoToProj.transform(g, p);

        double E = p.x;
        double N = p.y;

        // CRS sorgente -> locale LOC
        double Eloc = s * (cosT * E - sinT * N) + tE;
        double Nloc = s * (sinT * E + cosT * N) + tN;

        double Zloc = hEll;
        if (useZ) {
            double dE = E - E0;
            double dN = N - N0;
            double r = Math.hypot(dE, dN);

            double dZ = (r <= polyRadiusM)
                    ? (k0 + k1 * dE + k2 * dN + k3 * dE * dE + k4 * dN * dN + k5 * dE * dN)
                    : (k0 + k1 * dE + k2 * dN);

            Zloc = hEll + dZ;
        }

        out[0] = Eloc;
        out[1] = Nloc;
        out[2] = Zloc;
    }

    @Override
    public void toGeoFast(double Eloc, double Nloc, double Zloc, double[] out) {
        ProjCoordinate p = tlProj.get();
        ProjCoordinate g = tlGeo.get();

        // Inverso locale -> CRS sorgente
        double E = ( cosT * (Eloc - tE) + sinT * (Nloc - tN)) / s;
        double N = (-sinT * (Eloc - tE) + cosT * (Nloc - tN)) / s;

        double hEll = Zloc;
        if (useZ) {
            double dE = E - E0;
            double dN = N - N0;
            double r = Math.hypot(dE, dN);

            double dZ = (r <= polyRadiusM)
                    ? (k0 + k1 * dE + k2 * dN + k3 * dE * dE + k4 * dN * dN + k5 * dE * dN)
                    : (k0 + k1 * dE + k2 * dN);

            hEll = Zloc - dZ;
        }

        // CRS sorgente -> WGS84
        p.x = E;
        p.y = N;
        p.z = hEll;
        projToGeo.transform(p, g);

        out[0] = g.y;
        out[1] = g.x;
        out[2] = hEll;
    }

    /**
     * Rotazione della similarità Grid -> Local, in gradi CCW.
     */
    public double similarityThetaDeg() {
        return Math.toDegrees(Math.atan2(this.sinT, this.cosT));
    }

    /**
     * Convergenza fissata nel centro della rete di localizzazione.
     */
    public double gamma0Deg() {
        return gamma0Deg;
    }

    /**
     * Delta heading costante del progetto.
     *
     * headingGrid  = headingTrue + gamma0
     * headingLocal = headingGrid - theta
     *              = headingTrue + gamma0 - theta
     */
    public double headingOffsetDeg() {
        return gamma0Deg - similarityThetaDeg();
    }

    @Override
    public void toLocalFastWithHeadingDelta(double latDeg, double lonDeg, double hEll, double[] out) {
        toLocalFast(latDeg, lonDeg, hEll, out);
        if (out.length > 3) {
            out[3] = headingOffsetDeg();
        }
    }

    // ---------------------------------------------------------------------
    // Convergenza meridiana nel centro rete
    // ---------------------------------------------------------------------
    private static double meridianConvergenceDegAtProjectedPoint(double E,
                                                                 double N,
                                                                 CoordinateTransform projToGeo,
                                                                 CoordinateTransform geoToProj) {
        final double epsDeg = 1e-4;

        ProjCoordinate p = new ProjCoordinate();
        ProjCoordinate g = new ProjCoordinate();
        ProjCoordinate p2 = new ProjCoordinate();

        // projected -> geo
        p.x = E;
        p.y = N;
        projToGeo.transform(p, g);

        double lonDeg = g.x;
        double latDeg = g.y;

        // punto base
        g.x = lonDeg;
        g.y = latDeg;
        geoToProj.transform(g, p);
        final double E1 = p.x;
        final double N1 = p.y;

        // punto leggermente più a nord vero
        g.x = lonDeg;
        g.y = latDeg + epsDeg;
        geoToProj.transform(g, p2);

        final double dE = p2.x - E1;
        final double dN = p2.y - N1;

        return Math.toDegrees(Math.atan2(dE, dN));
    }

    // ---------------------------------------------------------------------
    // Similarità 2D
    // ---------------------------------------------------------------------
    private static final class Similarity {
        final double s, cosT, sinT, tE, tN;

        Similarity(double s, double cosT, double sinT, double tE, double tN) {
            this.s = s;
            this.cosT = cosT;
            this.sinT = sinT;
            this.tE = tE;
            this.tN = tN;
        }
    }

    private static Similarity fitSimilarityWeighted(double[] E, double[] N,
                                                    double[] EL, double[] NL,
                                                    boolean[] use) {
        int n = E.length;

        double wsum = 0.0, Er = 0.0, Nr = 0.0, ELr = 0.0, NLr = 0.0;
        for (int i = 0; i < n; i++) {
            double w = use[i] ? 1.0 : 0.0;
            wsum += w;
            Er += w * E[i];
            Nr += w * N[i];
            ELr += w * EL[i];
            NLr += w * NL[i];
        }

        if (wsum < 2.0) {
            throw new IllegalArgumentException("Punti orizzontali insufficienti nel LOC.");
        }

        Er /= wsum;
        Nr /= wsum;
        ELr /= wsum;
        NLr /= wsum;

        double Sxx = 0.0, Sxy = 0.0, Syx = 0.0, Syy = 0.0, Srr = 0.0;
        for (int i = 0; i < n; i++) {
            if (!use[i]) continue;

            double e  = E[i]  - Er;
            double nn = N[i]  - Nr;
            double le = EL[i] - ELr;
            double ln = NL[i] - NLr;

            Sxx += e  * le;
            Sxy += e  * ln;
            Syx += nn * le;
            Syy += nn * ln;
            Srr += e * e + nn * nn;
        }

        if (Srr < 1e-12) {
            throw new IllegalStateException("Configurazione H degenerata nel LOC.");
        }

        double c1 = Sxx + Syy;
        double s1 = Sxy - Syx;
        double rnorm = Math.hypot(c1, s1);

        double cosT = (rnorm > 0.0) ? c1 / rnorm : 1.0;
        double sinT = (rnorm > 0.0) ? s1 / rnorm : 0.0;
        double s = rnorm / Srr;

        double tE = ELr - s * (cosT * Er - sinT * Nr);
        double tN = NLr - s * (sinT * Er + cosT * Nr);

        return new Similarity(s, cosT, sinT, tE, tN);
    }

    // ---------------------------------------------------------------------
    // Fit quota
    // ---------------------------------------------------------------------
    private static double[] fitPoly2DZ_Ridge(double[] E, double[] N, double[] dZ, double[] w,
                                             double E0, double N0, double lambda) {
        final int m = 6;
        double[][] ATA = new double[m][m];
        double[] ATb = new double[m];
        int used = 0;

        for (int i = 0; i < E.length; i++) {
            double wi = w[i];
            if (wi <= 0.0) continue;
            used++;

            double dE = E[i] - E0;
            double dN = N[i] - N0;
            double[] phi = {1.0, dE, dN, dE * dE, dN * dN, dE * dN};

            for (int r = 0; r < m; r++) {
                ATb[r] += wi * phi[r] * dZ[i];
                for (int c = 0; c < m; c++) {
                    ATA[r][c] += wi * phi[r] * phi[c];
                }
            }
        }

        if (used < 3) {
            double num = 0.0, den = 0.0;
            for (int i = 0; i < dZ.length; i++) {
                num += w[i] * dZ[i];
                den += w[i];
            }
            double k0 = (den > 0.0) ? num / den : 0.0;
            return new double[]{k0, 0, 0, 0, 0, 0};
        }

        for (int i = 0; i < m; i++) ATA[i][i] += lambda;
        return solveNxN(ATA, ATb);
    }

    private static double[] solveNxN(double[][] A, double[] b) {
        int n = b.length;
        double[][] M = new double[n][n + 1];

        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, M[i], 0, n);
            M[i][n] = b[i];
        }

        for (int p = 0; p < n; p++) {
            int max = p;
            for (int r = p + 1; r < n; r++) {
                if (Math.abs(M[r][p]) > Math.abs(M[max][p])) max = r;
            }

            if (Math.abs(M[max][p]) < 1e-14) {
                throw new IllegalStateException("Sistema singolare (fit quota).");
            }

            if (max != p) {
                double[] tmp = M[p];
                M[p] = M[max];
                M[max] = tmp;
            }

            double piv = M[p][p];
            for (int c = p; c <= n; c++) M[p][c] /= piv;

            for (int r = 0; r < n; r++) {
                if (r == p) continue;
                double f = M[r][p];
                for (int c = p; c <= n; c++) M[r][c] -= f * M[p][c];
            }
        }

        double[] x = new double[n];
        for (int i = 0; i < n; i++) x[i] = M[i][n];
        return x;
    }

    // ---------------------------------------------------------------------
    // Util
    // ---------------------------------------------------------------------
    private static double meanWeighted(double[] v, boolean[] use) {
        double s = 0.0;
        int k = 0;
        for (int i = 0; i < v.length; i++) {
            if (use[i]) {
                s += v[i];
                k++;
            }
        }
        return (k > 0) ? s / k : 0.0;
    }

    private static boolean hasAny(boolean[] a) {
        for (boolean b : a) if (b) return true;
        return false;
    }

    private static List<Integer> indicesTrue(boolean[] a) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < a.length; i++) if (a[i]) out.add(i);
        return out;
    }

    // ---------------------------------------------------------------------
    // Parser LOC
    // ---------------------------------------------------------------------
    private static final class RawPoint {
        final double lat, lon, hEll, eLocal, nLocal, zLocal;
        final boolean useH, useV;

        RawPoint(double lat, double lon, double hEll,
                 double eLocal, double nLocal, double zLocal,
                 boolean useH, boolean useV) {
            this.lat = lat;
            this.lon = lon;
            this.hEll = hEll;
            this.eLocal = eLocal;
            this.nLocal = nLocal;
            this.zLocal = zLocal;
            this.useH = useH;
            this.useV = useV;
        }
    }

    private static List<RawPoint> parseLocFile(File file) throws Exception {
        List<RawPoint> out = new ArrayList<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try { dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Throwable ignored) {}
        try { dbf.setFeature("http://xml.org/sax/features/external-general-entities", false); } catch (Throwable ignored) {}
        try { dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false); } catch (Throwable ignored) {}
        try { dbf.setXIncludeAware(false); } catch (Throwable ignored) {}
        try { dbf.setExpandEntityReferences(false); } catch (Throwable ignored) {}

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        NodeList records = doc.getElementsByTagName("record");
        for (int i = 0; i < records.getLength(); i++) {
            Element rec = (Element) records.item(i);
            String id = rec.getAttribute("id");
            if (id == null) continue;

            String idLow = id.trim().toLowerCase(Locale.ROOT);
            if (!idLow.startsWith("point")) continue;

            double lat = Double.NaN, lon = Double.NaN, hell = 0.0, e = 0.0, n = 0.0, z = 0.0;
            boolean useH = true, useV = true;

            NodeList vals = rec.getElementsByTagName("value");
            for (int j = 0; j < vals.getLength(); j++) {
                Element v = (Element) vals.item(j);
                String name = v.getAttribute("name");
                String val = v.getAttribute("value");
                if (name == null || val == null) continue;

                name = name.trim();
                val = val.trim();
                if (val.isEmpty()) continue;

                switch (name) {
                    case "Lat":
                        lat = parseNum(val);
                        break;
                    case "Lon":
                        lon = parseNum(val);
                        break;

                    case "Ellipsoid_Elv":
                    case "EllipsoidElv":
                    case "EllipsoidHeight":
                        hell = parseNum(val);
                        break;

                    case "Local_X":
                    case "LocalX":
                        e = parseNum(val);
                        break;

                    case "Local_Y":
                    case "LocalY":
                        n = parseNum(val);
                        break;

                    case "Local_Z":
                    case "LocalZ":
                        z = parseNum(val);
                        break;

                    case "Use_Horizontal":
                        useH = parseBoolYes(val, true);
                        break;

                    case "Use_Vertical":
                        useV = parseBoolYes(val, true);
                        break;

                    default:
                        break;
                }
            }

            if (!Double.isFinite(lat) || !Double.isFinite(lon)) continue;
            if (lat == 0.0 && lon == 0.0) continue;

            out.add(new RawPoint(lat, lon, hell, e, n, z, useH, useV));
        }

        if (out.isEmpty()) {
            throw new IllegalArgumentException("Nessun record 'Point ...' valido nel LOC.");
        }

        return out;
    }

    private static double parseNum(String s) {
        s = s.trim().replace(',', '.');
        double v = Double.parseDouble(s);
        return Double.isFinite(v) ? v : 0.0;
    }

    private static boolean parseBoolYes(String v, boolean def) {
        if (v == null) return def;
        v = v.trim();
        if (v.isEmpty()) return def;
        if ("yes".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v) || "1".equals(v)) return true;
        if ("no".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v) || "0".equals(v)) return false;
        return def;
    }
}