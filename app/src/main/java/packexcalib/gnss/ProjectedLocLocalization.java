package packexcalib.gnss;

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
 * Convenzioni adottate:
 * - Input geo: lat/lon in gradi, quota ellissoidale
 * - Output locale: E, N, Z
 * - PROJ usa x=lon, y=lat
 *
 * Semantica heading:
 * - per coerenza col tuo uso macchina e con SpLocalization,
 *   out[3] = Ca in gradi
 *
 * Modello XY stimato dal LOC:
 *
 *     E_loc = (Orgy + Cy) + Ck * ( dE*cosA + dN*sinA )
 *     N_loc = (Orgx + Cx) + Ck * (-dE*sinA + dN*cosA )
 *
 *     con:
 *       dE = Egrid - Orgy
 *       dN = Ngrid - Orgx
 *
 * Quota:
 *
 *     dh = a0 + a1*dx + a2*dy + a3*dx^2 + a4*dy^2 + a5*dx*dy
 *     dx = Nloc - x0
 *     dy = Eloc - y0
 *
 *     Zloc = hEll - dh
 */
public final class ProjectedLocLocalization implements LocalizationModel {

    private final NativeProjTransformer geoToProj;
    private final NativeProjTransformer projToGeo;

    // 4 parametri in convenzione SP/CubeA
    private final boolean use4;
    private final double Cx, Cy, CaRad, Ck, Orgx, Orgy;
    private final double cosA, sinA;

    // heading: per uso operativo = Ca in gradi
    private final double headingDeltaDeg;

    // Height fitting
    private final boolean useHF;
    private final double a0, a1, a2, a3, a4, a5;
    private final double hfX0, hfY0;

    private ProjectedLocLocalization(NativeProjTransformer geoToProj,
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
        this.Orgx = Orgx; // North origin
        this.Orgy = Orgy; // East origin
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
        this.hfX0 = hfX0; // north locale
        this.hfY0 = hfY0; // east locale
    }

    // ---------------------------------------------------------------------
    // Factory
    // ---------------------------------------------------------------------
    public static ProjectedLocLocalization fromLocFile(File locFile,
                                                       NativeProjTransformer geoToProj,
                                                       NativeProjTransformer projToGeo) throws Exception {
        if (geoToProj == null || projToGeo == null) {
            throw new IllegalArgumentException("Transform geo<->proj null.");
        }

        final List<RawPoint> pts = parseLocFile(locFile);
        if (pts.size() < 2) {
            throw new IllegalArgumentException("Il file LOC deve contenere almeno 2 punti validi.");
        }

        final int n = pts.size();

        double[] E = new double[n];      // projected East
        double[] N = new double[n];      // projected North
        double[] EL = new double[n];     // local East
        double[] NL = new double[n];     // local North
        double[] ZL = new double[n];     // local Z
        double[] Hell = new double[n];   // ellipsoidal height

        boolean[] useH = new boolean[n];
        boolean[] useV = new boolean[n];

        for (int i = 0; i < n; i++) {
            RawPoint rp = pts.get(i);

            double[] p = geoToProj.transformPrepared(rp.lon, rp.lat, rp.hEll);

            E[i] = p[0];
            N[i] = p[1];

            EL[i] = rp.eLocal;
            NL[i] = rp.nLocal;
            ZL[i] = rp.zLocal;
            Hell[i] = rp.hEll;

            useH[i] = rp.useH;
            useV[i] = rp.useV;
        }

        // ------------------ Fit 4 parametri ------------------
        FourParam fp = fitFourParameters(E, N, EL, NL, useH);

        // ------------------ Fit quota ------------------
        boolean useHF = false;
        double a0 = 0, a1 = 0, a2 = 0, a3 = 0, a4 = 0, a5 = 0;
        double hfX0 = 0, hfY0 = 0;

        if (hasAny(useV)) {
            // origine polinomio in coordinate locali
            hfX0 = meanWeighted(NL, useV); // north local
            hfY0 = meanWeighted(EL, useV); // east local

            double[] dH = new double[n];
            double[] w = new double[n];

            for (int i = 0; i < n; i++) {
                // Zloc = hEll - dh  => dh = hEll - Zloc
                dH[i] = Hell[i] - ZL[i];
                w[i] = useV[i] ? 1.0 : 0.0;
            }

            double[] coeff = fitPlaneLocal(EL, NL, dH, w, hfY0, hfX0);
            a0 = coeff[0];
            a1 = coeff[1];
            a2 = coeff[2];
            a3 = 0.0;
            a4 = 0.0;
            a5 = 0.0;
            useHF = true;
        }

        double headingDeltaDeg = Math.toDegrees(fp.CaRad);

        return new ProjectedLocLocalization(
                geoToProj, projToGeo,
                true,
                fp.Cx, fp.Cy, fp.CaRad, fp.Ck, fp.Orgx, fp.Orgy,
                headingDeltaDeg,
                useHF,
                a0, a1, a2, a3, a4, a5,
                hfX0, hfY0
        );
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
            double dE = E - Orgy;
            double dN = N - Orgx;

            double Ep = dE * cosA + dN * sinA;
            double Np = -dE * sinA + dN * cosA;

            E = (Orgy + Cy) + Ck * Ep;
            N = (Orgx + Cx) + Ck * Np;
        }

        double Zloc = hEll;
        if (useHF) {
            double dx = N - hfX0; // north local
            double dy = E - hfY0; // east local
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
            double dx = n - hfX0; // north local
            double dy = e - hfY0; // east local
            double dh = a0 + a1 * dx + a2 * dy + a3 * dx * dx + a4 * dy * dy + a5 * dx * dy;
            hEll = Zloc + dh;
        }

        // 2) inverso 4 parametri
        if (use4) {
            double dE = (e - (Orgy + Cy)) / Ck;
            double dN = (n - (Orgx + Cx)) / Ck;

            double Ei = dE * cosA - dN * sinA;
            double Ni = dE * sinA + dN * cosA;

            e = Orgy + Ei;
            n = Orgx + Ni;
        }

        // 3) projected -> geo
        double[] g = projToGeo.transformPrepared(e, n, hEll);

        out[0] = g[1]; // lat
        out[1] = g[0]; // lon
        out[2] = hEll;
    }

    /**
     * out[3] = delta heading da sommare all'HDT true.
     * Per coerenza con SP e con il tuo uso macchina:
     * out[3] = CaDeg
     */
    @Override
    public void toLocalFastWithHeadingDelta(double latDeg, double lonDeg, double hEll, double[] out) {
        toLocalFast(latDeg, lonDeg, hEll, out);
        if (out.length > 3) {
            out[3] = headingDeltaDeg;
        }
    }

    // ---------------------------------------------------------------------
    // Getter utili
    // ---------------------------------------------------------------------
    public boolean isUsingFourParameters() {
        return use4;
    }

    public boolean isUsingHeightFitting() {
        return useHF;
    }

    public double getCx() {
        return Cx;
    }

    public double getCy() {
        return Cy;
    }

    public double getCaRad() {
        return CaRad;
    }

    public double getCaDeg() {
        return Math.toDegrees(CaRad);
    }

    public double getCk() {
        return Ck;
    }

    public double getOriginNorth() {
        return Orgx;
    }

    public double getOriginEast() {
        return Orgy;
    }

    public double getHeadingDeltaDeg() {
        return headingDeltaDeg;
    }

    public double getA0() {
        return a0;
    }

    public double getA1() {
        return a1;
    }

    public double getA2() {
        return a2;
    }

    public double getA3() {
        return a3;
    }

    public double getA4() {
        return a4;
    }

    public double getA5() {
        return a5;
    }

    public double getHfX0() {
        return hfX0;
    }

    public double getHfY0() {
        return hfY0;
    }

    // ---------------------------------------------------------------------
    // Fit 4 parametri
    // ---------------------------------------------------------------------
    private static final class FourParam {
        final double Cx, Cy, CaRad, Ck, Orgx, Orgy;

        FourParam(double Cx, double Cy, double CaRad, double Ck, double Orgx, double Orgy) {
            this.Cx = Cx;
            this.Cy = Cy;
            this.CaRad = CaRad;
            this.Ck = Ck;
            this.Orgx = Orgx;
            this.Orgy = Orgy;
        }
    }

    private static FourParam fitFourParameters(double[] E, double[] N,
                                               double[] EL, double[] NL,
                                               boolean[] use) {
        int n = E.length;

        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (use[i]) idx.add(i);
        }

        if (idx.size() < 2) {
            throw new IllegalArgumentException("Punti orizzontali insufficienti nel LOC.");
        }

        // Fit lineare:
        // EL = te + a*E + b*N
        // NL = tn - b*E + a*N
        double[][] ATA = new double[4][4];
        double[] ATb = new double[4];

        for (int i : idx) {
            addNormalEq(ATA, ATb, new double[]{E[i], N[i], 1.0, 0.0}, EL[i]);
            addNormalEq(ATA, ATb, new double[]{N[i], -E[i], 0.0, 1.0}, NL[i]);
        }

        double[] x = solveNxN(ATA, ATb);

        double a = x[0];
        double b = x[1];
        double te = x[2];
        double tn = x[3];

        double Ck = Math.hypot(a, b);
        if (!Double.isFinite(Ck) || Ck < 1e-12) {
            throw new IllegalStateException("LOC: scala XY non valida.");
        }

        double CaRad = Math.atan2(b, a);

        // origine = baricentro dei punti projected usati
        double Orgx = meanWeighted(N, use);
        double Orgy = meanWeighted(E, use);

        // Conversione nella stessa forma della SP
        double Cy = te - Orgy + a * Orgy + b * Orgx;
        double Cx = tn - Orgx - b * Orgy + a * Orgx;

        return new FourParam(Cx, Cy, CaRad, Ck, Orgx, Orgy);
    }

    private static void addNormalEq(double[][] ATA, double[] ATb, double[] row, double obs) {
        for (int r = 0; r < row.length; r++) {
            ATb[r] += row[r] * obs;
            for (int c = 0; c < row.length; c++) {
                ATA[r][c] += row[r] * row[c];
            }
        }
    }

    // ---------------------------------------------------------------------
    // Fit quota
    // ---------------------------------------------------------------------
    private static double[] fitPoly2DLocal(double[] EL, double[] NL, double[] dH, double[] w,
                                           double y0, double x0, double lambda) {
        final int m = 6;
        double[][] ATA = new double[m][m];
        double[] ATb = new double[m];
        int used = 0;

        for (int i = 0; i < EL.length; i++) {
            double wi = w[i];
            if (wi <= 0.0) continue;
            used++;

            double dx = NL[i] - x0; // north local
            double dy = EL[i] - y0; // east local
            double[] phi = {1.0, dx, dy, dx * dx, dy * dy, dx * dy};

            for (int r = 0; r < m; r++) {
                ATb[r] += wi * phi[r] * dH[i];
                for (int c = 0; c < m; c++) {
                    ATA[r][c] += wi * phi[r] * phi[c];
                }
            }
        }

        if (used < 3) {
            double num = 0.0, den = 0.0;
            for (int i = 0; i < dH.length; i++) {
                num += w[i] * dH[i];
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
                throw new IllegalStateException("Sistema singolare.");
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

            double lat = Double.NaN, lon = Double.NaN, hell = 0.0;
            double e = 0.0, n = 0.0, z = 0.0;
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

                    // LOC:
                    // Local_X = East locale
                    // Local_Y = North locale
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
        if (s == null) return 0.0;
        s = s.trim().replace(',', '.');
        if (s.isEmpty()) return 0.0;

        try {
            double v = Double.parseDouble(s);
            return Double.isFinite(v) ? v : 0.0;
        } catch (Throwable t) {
            return 0.0;
        }
    }

    private static boolean parseBoolYes(String v, boolean def) {
        if (v == null) return def;
        v = v.trim();
        if (v.isEmpty()) return def;
        if ("yes".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v) || "1".equals(v)) return true;
        if ("no".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v) || "0".equals(v)) return false;
        return def;
    }

    private static double[] fitPlaneLocal(double[] EL, double[] NL, double[] dH, double[] w,
                                          double y0, double x0) {
        // dh = a0 + a1*dx + a2*dy
        // dx = Nloc - x0
        // dy = Eloc - y0

        double[][] ATA = new double[3][3];
        double[] ATb = new double[3];
        int used = 0;

        for (int i = 0; i < EL.length; i++) {
            double wi = w[i];
            if (wi <= 0.0) continue;
            used++;

            double dx = NL[i] - x0; // north local
            double dy = EL[i] - y0; // east local

            double[] phi = {1.0, dx, dy};

            for (int r = 0; r < 3; r++) {
                ATb[r] += wi * phi[r] * dH[i];
                for (int c = 0; c < 3; c++) {
                    ATA[r][c] += wi * phi[r] * phi[c];
                }
            }
        }

        if (used < 1) {
            return new double[]{0.0, 0.0, 0.0};
        }

        double[] sol = solveNxN(ATA, ATb);
        return new double[]{sol[0], sol[1], sol[2]};
    }
}