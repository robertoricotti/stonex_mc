package packexcalib.gnss;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * CarlsonLocalization (LOC)
 *
 * Modello:
 * - Geo(WGS84) -> ECEF -> ENU (topocentrico) con origine al baricentro dei punti.
 * - XY: similarità 2D stimata (s, θ, tx, ty) con Procrustes pesato (Use_Horizontal).
 * - Z : ΔZ = Zloc - hEll fit con polinomio 2° (Use_Vertical) + ridge + guard-rail (termini quadratici solo entro polyRadius).
 *
 * Heading:
 * - In ENU, N è (quasi) True-North e E è True-East (esatto all'origine).
 * - La similarità ruota ENU -> Local di θ (CCW, convenzione matematica) con:
 *      X = s*( cosθ*E - sinθ*N ) + tx
 *      Y = s*( sinθ*E + cosθ*N ) + ty
 * - Quindi un vettore True-North (E=0,N=1) in locale ha bearing = atan2(dX,dY)=atan2(-sinθ,cosθ)= -θ.
 *   => delta da sommare all'heading True per avere heading locale: delta = -θ (in gradi).
 */
public final class CarlsonLocalization implements LocalizationModel {

    // ===== WGS84 =====
    private static final double A  = 6378137.0;
    private static final double F  = 1.0 / 298.257223563;
    private static final double E2 = F * (2 - F);

    // ===== Origine ENU =====
    private final double lat0Deg, lon0Deg, h0;
    private final double[] xyz0;     // ECEF origine
    private final double[][] Renu;   // rotazione ECEF->ENU (3x3)

    // ===== Similarità XY: (X,Y) = s*R(θ)*[E,N] + t
    private final double s, cosT, sinT, tx, ty;

    // ===== Modello ΔZ =====
    private final boolean useZ;
    private final double E0, N0;                    // centro per dE,dN
    private final double k0,k1,k2,k3,k4,k5;         // coeff. polinomio
    private final double polyRadiusM;               // raggio validità termini quadratici

    // ===== Scratch (zero alloc nel path hot) =====
    private final ThreadLocal<double[]> tlXyz = ThreadLocal.withInitial(() -> new double[3]);
    private final ThreadLocal<double[]> tlEnu = ThreadLocal.withInitial(() -> new double[3]);

    private CarlsonLocalization(double lat0Deg, double lon0Deg, double h0,
                                double[] xyz0, double[][] Renu,
                                double s, double cosT, double sinT, double tx, double ty,
                                boolean useZ, double E0, double N0,
                                double k0, double k1, double k2, double k3, double k4, double k5,
                                double polyRadiusM) {
        this.lat0Deg = lat0Deg; this.lon0Deg = lon0Deg; this.h0 = h0;
        this.xyz0 = xyz0; this.Renu = Renu;

        // hardening scala
        if (!Double.isFinite(s) || Math.abs(s) < 1e-12) {
            throw new IllegalArgumentException("LOC: scala XY non valida (s=" + s + ")");
        }
        this.s = s;

        this.cosT = cosT;
        this.sinT = sinT;
        this.tx = tx;
        this.ty = ty;

        this.useZ = useZ;
        this.E0 = E0; this.N0 = N0;
        this.k0 = k0; this.k1 = k1; this.k2 = k2; this.k3 = k3; this.k4 = k4; this.k5 = k5;

        this.polyRadiusM = (Double.isFinite(polyRadiusM) && polyRadiusM > 0) ? polyRadiusM : 300.0;
    }

    // =====================================================================
    // Factory
    // =====================================================================
    public static CarlsonLocalization fromLocFile(File locFile) throws Exception {
        final List<RawPoint> pts = parseLocFile(locFile);

        if (pts.size() < 2)
            throw new IllegalArgumentException("Il file LOC deve contenere almeno 2 punti validi.");

        // Origine ENU = baricentro (sufficiente per aree locali)
        double lat0 = 0, lon0 = 0, h0 = 0;
        for (RawPoint p : pts) { lat0 += p.lat; lon0 += p.lon; h0 += p.hEll; }
        lat0 /= pts.size(); lon0 /= pts.size(); h0 /= pts.size();

        final double[] xyz0 = geoToEcef(lat0, lon0, h0);
        final double[][] Renu = enuRotation(lat0, lon0);

        // Proietta i punti in ENU
        final int n = pts.size();
        double[] E = new double[n], N = new double[n];
        double[] X = new double[n], Y = new double[n], Z = new double[n], Hell = new double[n];
        boolean[] useH = new boolean[n], useV = new boolean[n];

        double[] xyz = new double[3], enu = new double[3];
        for (int i = 0; i < n; i++) {
            RawPoint p = pts.get(i);

            geoToEcef(p.lat, p.lon, p.hEll, xyz);
            ecefToEnu(xyz, xyz0, Renu, enu);

            E[i] = enu[0];
            N[i] = enu[1];

            // Convenzione: Local_X = X (tipicamente Easting), Local_Y = Y (tipicamente Northing)
            X[i] = p.xLocal;
            Y[i] = p.yLocal;
            Z[i] = p.zLocal;
            Hell[i] = p.hEll;

            useH[i] = p.useH;
            useV[i] = p.useV;
        }

        // Fit XY: similarità pesata
        Similarity sim = fitSimilarityWeighted(E, N, X, Y, useH);

        // Fit ΔZ: polinomio 2° sui soli verticali
        double E0c = meanWeighted(E, useV);
        double N0c = meanWeighted(N, useV);

        boolean useZ = false;
        double k0=0,k1=0,k2=0,k3=0,k4=0,k5=0;

        if (hasAny(useV)) {
            double[] dZ = new double[n];
            double[] w  = new double[n];
            for (int i=0; i<n; i++){
                dZ[i] = Z[i] - Hell[i];
                w[i]  = useV[i] ? 1.0 : 0.0;
            }
            double[] coeff = fitPoly2DZ_Ridge(E, N, dZ, w, E0c, N0c, 1e-12);
            k0=coeff[0]; k1=coeff[1]; k2=coeff[2]; k3=coeff[3]; k4=coeff[4]; k5=coeff[5];
            useZ = true;
        }

        // Guard-rail per termini quadratici quota (raggio basato sulla rete)
        double maxBL = 0.0;
        List<Integer> idxH = indicesTrue(useH);
        if (idxH.size() >= 2) {
            for (int i=0;i<idxH.size();i++){
                int ii = idxH.get(i);
                for (int j=i+1;j<idxH.size();j++){
                    int jj = idxH.get(j);
                    double dE = E[ii]-E[jj], dN = N[ii]-N[jj];
                    double d  = Math.hypot(dE, dN);
                    if (d > maxBL) maxBL = d;
                }
            }
        }
        double polyRadius = Math.max(300.0, 2.0 * maxBL);

        return new CarlsonLocalization(
                lat0, lon0, h0, xyz0, Renu,
                sim.s, sim.cosT, sim.sinT, sim.tx, sim.ty,
                useZ, E0c, N0c, k0, k1, k2, k3, k4, k5,
                polyRadius
        );
    }

    // =====================================================================
    // Trasformazioni
    // =====================================================================
    @Override
    public void toLocalFast(double latDeg, double lonDeg, double hEll, double[] out) {
        double[] xyz = tlXyz.get();
        double[] enu = tlEnu.get();

        geoToEcef(latDeg, lonDeg, hEll, xyz);
        ecefToEnu(xyz, xyz0, Renu, enu);

        double E = enu[0];
        double N = enu[1];

        // Similarità 2D: ENU -> Local
        double X = s * (  cosT*E - sinT*N) + tx;
        double Y = s * (  sinT*E + cosT*N) + ty;

        // Quota locale
        double Zloc = hEll;
        if (useZ) {
            double dE = E - E0, dN = N - N0;
            double r = Math.hypot(dE, dN);

            double dZ = (r <= polyRadiusM)
                    ? (k0 + k1*dE + k2*dN + k3*dE*dE + k4*dN*dN + k5*dE*dN)
                    : (k0 + k1*dE + k2*dN);

            Zloc = hEll + dZ;
        }

        out[0] = X;
        out[1] = Y;
        out[2] = Zloc;
    }

    @Override
    public void toGeoFast(double X, double Y, double Zloc, double[] out) {
        // Inverso similarità 2D: Local -> ENU
        double Ex = (  cosT*(X - tx) + sinT*(Y - ty) ) / s;
        double Nx = ( -sinT*(X - tx) + cosT*(Y - ty) ) / s;

        // Inverso ΔZ
        double hEll = Zloc;
        if (useZ) {
            double dE = Ex - E0, dN = Nx - N0;
            double r = Math.hypot(dE, dN);

            double dZ = (r <= polyRadiusM)
                    ? (k0 + k1*dE + k2*dN + k3*dE*dE + k4*dN*dN + k5*dE*dN)
                    : (k0 + k1*dE + k2*dN);

            hEll = Zloc - dZ;
        }

        // ENU -> ECEF -> Geo
        double[] ecef = tlXyz.get(); // riuso
        double U = hEll - h0;        // Up coerente con quota ellissoidale relativa
        enuToEcef(Ex, Nx, U, xyz0, Renu, ecef);

        double[] llh = tlEnu.get();  // riuso come [lat,lon,hTmp]
        ecefToGeo(ecef[0], ecef[1], ecef[2], llh);

        out[0] = llh[0];
        out[1] = llh[1];
        out[2] = hEll;
    }

    /**
     * Delta (gradi) da sommare all'Heading True (0=N,90=E) per ottenere heading locale.
     * delta = -θ, dove θ è la rotazione CCW della similarità ENU->Local.
     */
    public double headingOffsetDeg() {
        double thetaRad = Math.atan2(this.sinT, this.cosT); // θ in [-pi,pi]
        return -Math.toDegrees(thetaRad);
    }

    @Override
    public void toLocalFastWithHeadingDelta(double lat, double lon, double h, double[] out) {
        toLocalFast(lat, lon, h, out);
        if (out.length > 3) out[3] = headingOffsetDeg();
    }

    // =====================================================================
    // ENU / ECEF / GEO
    // =====================================================================
    private static void geoToEcef(double latDeg, double lonDeg, double h, double[] out) {
        double lat = Math.toRadians(latDeg);
        double lon = Math.toRadians(lonDeg);

        double s = Math.sin(lat), c = Math.cos(lat);
        double N = A / Math.sqrt(1 - E2*s*s);

        out[0] = (N + h) * c * Math.cos(lon);
        out[1] = (N + h) * c * Math.sin(lon);
        out[2] = (N*(1 - E2) + h) * s;
    }

    private static double[] geoToEcef(double latDeg, double lonDeg, double h) {
        double[] out = new double[3];
        geoToEcef(latDeg, lonDeg, h, out);
        return out;
    }

    private static double[][] enuRotation(double lat0Deg, double lon0Deg) {
        double lat = Math.toRadians(lat0Deg), lon = Math.toRadians(lon0Deg);
        double sL = Math.sin(lat), cL = Math.cos(lat);
        double sO = Math.sin(lon), cO = Math.cos(lon);

        // ECEF -> ENU
        return new double[][]{
                { -sO,            cO,           0 },
                { -sL*cO,        -sL*sO,       cL },
                {  cL*cO,         cL*sO,       sL }
        };
    }

    private static void ecefToEnu(double[] xyz, double[] xyz0, double[][] R, double[] out) {
        double dx = xyz[0]-xyz0[0], dy = xyz[1]-xyz0[1], dz = xyz[2]-xyz0[2];
        out[0] = R[0][0]*dx + R[0][1]*dy + R[0][2]*dz; // E
        out[1] = R[1][0]*dx + R[1][1]*dy + R[1][2]*dz; // N
        out[2] = R[2][0]*dx + R[2][1]*dy + R[2][2]*dz; // U
    }

    private static void enuToEcef(double E, double N, double U, double[] xyz0, double[][] R, double[] out) {
        // ENU -> ECEF usa la trasposta di R (perché R è ECEF->ENU)
        double dx = R[0][0]*E + R[1][0]*N + R[2][0]*U;
        double dy = R[0][1]*E + R[1][1]*N + R[2][1]*U;
        double dz = R[0][2]*E + R[1][2]*N + R[2][2]*U;

        out[0] = xyz0[0] + dx;
        out[1] = xyz0[1] + dy;
        out[2] = xyz0[2] + dz;
    }

    private static void ecefToGeo(double x, double y, double z, double[] out) {
        double lon = Math.atan2(y, x);
        double p = Math.hypot(x, y);

        // iterazione rapida
        double lat = Math.atan2(z, p*(1 - E2));
        for (int i=0; i<6; i++) {
            double s = Math.sin(lat);
            double N = A / Math.sqrt(1 - E2*s*s);
            lat = Math.atan2(z + N*E2*s, p);
        }

        double s = Math.sin(lat);
        double N = A / Math.sqrt(1 - E2*s*s);
        double h = p/Math.cos(lat) - N;

        out[0] = Math.toDegrees(lat);
        out[1] = Math.toDegrees(lon);
        out[2] = h;
    }

    // =====================================================================
    // Fit Similarità 2D (Procrustes) con pesi {0/1}
    // =====================================================================
    private static final class Similarity {
        final double s, cosT, sinT, tx, ty;
        Similarity(double s, double cosT, double sinT, double tx, double ty){
            this.s=s; this.cosT=cosT; this.sinT=sinT; this.tx=tx; this.ty=ty;
        }
    }

    private static Similarity fitSimilarityWeighted(double[] E, double[] N, double[] X, double[] Y, boolean[] use){
        int n = E.length;

        double wsum=0, Er=0, Nr=0, Xr=0, Yr=0;
        for (int i=0;i<n;i++){
            double w = use[i] ? 1.0 : 0.0;
            wsum += w; Er += w*E[i]; Nr += w*N[i]; Xr += w*X[i]; Yr += w*Y[i];
        }
        if (wsum < 2.0) throw new IllegalArgumentException("Punti orizzontali insufficienti nel LOC (Use_Horizontal).");

        Er/=wsum; Nr/=wsum; Xr/=wsum; Yr/=wsum;

        double Sxx=0, Sxy=0, Syx=0, Syy=0, Srr=0;
        int used = 0;
        for (int i=0;i<n;i++){
            if (!use[i]) continue;
            used++;
            double e = E[i]-Er, nn = N[i]-Nr;
            double x = X[i]-Xr, y = Y[i]-Yr;
            Sxx += e*x;  Sxy += e*y;
            Syx += nn*x; Syy += nn*y;
            Srr += e*e + nn*nn;
        }
        if (used < 2 || Srr < 1e-12) throw new IllegalStateException("Configurazione H degenerata nel LOC.");

        // θ = atan2(Sxy - Syx, Sxx + Syy)
        double c1 = Sxx + Syy;
        double s1 = Sxy - Syx;
        double rnorm = Math.hypot(c1, s1);

        double cosT = (rnorm>0)? c1/rnorm : 1.0;
        double sinT = (rnorm>0)? s1/rnorm : 0.0;

        double s = rnorm / Srr;

        double tx = Xr - s*( cosT*Er - sinT*Nr );
        double ty = Yr - s*( sinT*Er + cosT*Nr );

        return new Similarity(s, cosT, sinT, tx, ty);
    }

    // =====================================================================
    // Fit ΔZ con ridge
    // =====================================================================
    private static double[] fitPoly2DZ_Ridge(double[] E, double[] N, double[] dZ, double[] w,
                                             double E0, double N0, double lambda){
        final int m = 6;
        double[][] ATA = new double[m][m];
        double[]   ATb = new double[m];
        int used = 0;

        for (int i=0;i<E.length;i++){
            double wi = w[i];
            if (wi <= 0) continue;
            used++;
            double de = E[i]-E0, dn = N[i]-N0;
            double[] phi = {1.0, de, dn, de*de, dn*dn, de*dn};

            for (int r=0;r<m;r++){
                ATb[r] += wi * phi[r] * dZ[i];
                for (int c=0;c<m;c++){
                    ATA[r][c] += wi * phi[r] * phi[c];
                }
            }
        }

        // se pochi punti verticali: usa solo offset k0
        if (used < 3) {
            double num=0, den=0;
            for (int i=0;i<dZ.length;i++){ num += w[i]*dZ[i]; den += w[i]; }
            double k0 = (den>0)? num/den : 0.0;
            return new double[]{k0,0,0,0,0,0};
        }

        for (int i=0;i<m;i++) ATA[i][i] += lambda;

        return solveNxN(ATA, ATb);
    }

    // =====================================================================
    // Algebra lineare
    // =====================================================================
    private static double[] solveNxN(double[][] A, double[] b){
        int n = b.length;
        double[][] M = new double[n][n+1];
        for (int i=0;i<n;i++){
            System.arraycopy(A[i],0,M[i],0,n);
            M[i][n]=b[i];
        }
        for (int p=0;p<n;p++){
            int max = p;
            for (int r=p+1;r<n;r++) if (Math.abs(M[r][p]) > Math.abs(M[max][p])) max=r;
            if (Math.abs(M[max][p]) < 1e-14) throw new IllegalStateException("Sistema singolare (fit quota).");
            if (max != p){ double[] t=M[p]; M[p]=M[max]; M[max]=t; }

            double piv = M[p][p];
            for (int c=p;c<=n;c++) M[p][c] /= piv;

            for (int r=0;r<n;r++){
                if (r==p) continue;
                double f = M[r][p];
                for (int c=p;c<=n;c++) M[r][c] -= f*M[p][c];
            }
        }
        double[] x = new double[n];
        for (int i=0;i<n;i++) x[i]=M[i][n];
        return x;
    }

    // =====================================================================
    // Utilità
    // =====================================================================
    private static double meanWeighted(double[] v, boolean[] use){
        double s=0; int k=0;
        for (int i=0;i<v.length;i++) if (use[i]){ s+=v[i]; k++; }
        return (k>0)? s/k : 0.0;
    }
    private static boolean hasAny(boolean[] a){
        for (boolean b: a) if (b) return true;
        return false;
    }
    private static List<Integer> indicesTrue(boolean[] a){
        List<Integer> L=new ArrayList<>();
        for(int i=0;i<a.length;i++) if(a[i]) L.add(i);
        return L;
    }

    // =====================================================================
    // Parser LOC (hardening anti-XXE)
    // =====================================================================
    private static final class RawPoint {
        final double lat, lon, hEll, xLocal, yLocal, zLocal;
        final boolean useH, useV;
        RawPoint(double lat,double lon,double hEll,double x,double y,double z, boolean useH, boolean useV){
            this.lat=lat; this.lon=lon; this.hEll=hEll;
            this.xLocal=x; this.yLocal=y; this.zLocal=z;
            this.useH=useH; this.useV=useV;
        }
    }

    private static List<RawPoint> parseLocFile(File file) throws Exception {
        List<RawPoint> out = new ArrayList<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Throwable ignored) {}
        // alcuni parser Android crashano su setXIncludeAware
        try { dbf.setXIncludeAware(false); } catch (Throwable ignored) {}

// alcuni parser Android crashano su setExpandEntityReferences
        try { dbf.setExpandEntityReferences(false); } catch (Throwable ignored) {}

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        NodeList records = doc.getElementsByTagName("record");
        for (int i=0;i<records.getLength();i++){
            Element rec = (Element) records.item(i);
            String id = rec.getAttribute("id");
            if (id == null) continue;

            // "Point ..." case-insensitive
            String idLow = id.trim().toLowerCase(Locale.ROOT);
            if (!idLow.startsWith("point")) continue;

            double lat=Double.NaN, lon=Double.NaN, hell=0, lx=0, ly=0, lz=0;
            boolean useH = true, useV = true;

            NodeList vals = rec.getElementsByTagName("value");
            for (int j=0;j<vals.getLength();j++){
                Element v = (Element) vals.item(j);
                String name = v.getAttribute("name");
                String val  = v.getAttribute("value");
                if (name == null || val == null) continue;

                name = name.trim();
                val  = val.trim();
                if (val.isEmpty()) continue;

                switch (name) {
                    case "Lat": lat = parseNum(val); break;
                    case "Lon": lon = parseNum(val); break;

                    case "Ellipsoid_Elv":
                    case "EllipsoidElv":
                    case "EllipsoidHeight":
                        hell = parseNum(val); break;

                    case "Local_X":
                    case "LocalX":
                        lx = parseNum(val); break;

                    case "Local_Y":
                    case "LocalY":
                        ly = parseNum(val); break;

                    case "Local_Z":
                    case "LocalZ":
                        lz = parseNum(val); break;

                    case "Use_Horizontal":
                        useH = parseBoolYes(val, true); break;

                    case "Use_Vertical":
                        useV = parseBoolYes(val, true); break;

                    default:
                        // ignore
                }
            }

            if (!Double.isFinite(lat) || !Double.isFinite(lon)) continue;
            if (lat == 0.0 && lon == 0.0) continue; // placeholder frequente

            out.add(new RawPoint(lat, lon, hell, lx, ly, lz, useH, useV));
        }

        if (out.isEmpty())
            throw new IllegalArgumentException("Nessun record 'Point ...' valido nel LOC.");

        return out;
    }

    private static double parseNum(String s){
        s = s.trim().replace(',', '.');
        double v = Double.parseDouble(s);
        return Double.isFinite(v) ? v : 0.0;
    }

    private static boolean parseBoolYes(String v, boolean def){
        if (v == null) return def;
        v = v.trim();
        if (v.isEmpty()) return def;
        if ("yes".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v) || "1".equals(v)) return true;
        if ("no".equalsIgnoreCase(v)  || "false".equalsIgnoreCase(v) || "0".equals(v)) return false;
        return def;
    }
}