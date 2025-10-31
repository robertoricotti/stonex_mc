package packexcalib.gnss;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CarlsonLocalization (solo .LOC)
 *
 * - Crea una Transverse Mercator locale centrata sul baricentro dei punti (ellps=WGS84, k=1).
 * - Fit XY: modello affine 2D (X = aE + bN + c ; Y = dE + eN + f), pesando i punti con Use_Horizontal="Yes".
 * - Fit Z : ΔZ = Zloc - hEll = k0 + k1*dE + k2*dN + k3*dE^2 + k4*dN^2 + k5*dE*dN, pesando Use_Vertical="Yes".
 * - Guard-rail quota: il polinomio completo si applica solo entro polyRadiusM; fuori si usa solo il piano (k0+k1*dE+k2*dN).
 * - Runtime senza allocazioni nel path hot (usa ThreadLocal).
 */
public final class CarlsonLocalization implements LocalizationModel {

    // ================== PROJ4J infra ==================
    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateTransformFactory CT_FACTORY = new CoordinateTransformFactory();

    private final CoordinateTransform geoToProj;
    private final CoordinateTransform projToGeo;

    // ============ Trasf. affine XY (locale) ============
    // Xloc = a*E + b*N + c
    // Yloc = d*E + e*N + f
    private final double a,b,c;
    private final double d,e,f;

    // =============== Modello ΔZ (centrato) ===============
    private final boolean useZ;
    private final double E0, N0;      // centro per dE,dN
    private final double k0,k1,k2,k3,k4,k5;

    // Guard-rail per la quota
    private final double polyRadiusM;           // raggio entro cui usare il polinomio completo
    private final boolean planeOutsideRadius;   // fuori raggio -> solo piano (niente termini quadratici)

    // ================= Buffers thread-local =================
    private final ThreadLocal<ProjCoordinate> tlGeo  = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    private CarlsonLocalization(CoordinateTransform geoToProj,
                                CoordinateTransform projToGeo,
                                double a,double b,double c,
                                double d,double e,double f,
                                boolean useZ,
                                double E0,double N0,
                                double k0,double k1,double k2,double k3,double k4,double k5,
                                double polyRadiusM, boolean planeOutsideRadius) {
        this.geoToProj = geoToProj;
        this.projToGeo = projToGeo;
        this.a=a; this.b=b; this.c=c;
        this.d=d; this.e=e; this.f=f;
        this.useZ = useZ;
        this.E0=E0; this.N0=N0;
        this.k0=k0; this.k1=k1; this.k2=k2; this.k3=k3; this.k4=k4; this.k5=k5;
        this.polyRadiusM = polyRadiusM;
        this.planeOutsideRadius = planeOutsideRadius;
    }

    // =====================================================================
    // Factory: SOLO LOC (autonomo)
    // =====================================================================
    public static CarlsonLocalization fromLocFile(File locFile) throws Exception {
        final List<RawPoint> raw = parseLocFile(locFile);
        if (raw.size() < 2) {
            throw new IllegalArgumentException("Il file LOC deve contenere almeno 2 punti validi.");
        }

        // 1) Centro della TM locale: baricentro (lat,lon) dei punti
        double lat0 = 0.0, lon0 = 0.0;
        for (RawPoint p : raw) { lat0 += p.lat; lon0 += p.lon; }
        lat0 /= raw.size(); lon0 /= raw.size();

        // k=1: conforme locale senza schiacciamenti arbitrari (l'affine gestisce le discrepanze)
        String projDef = String.format(Locale.US,
                "+proj=tmerc +lat_0=%.9f +lon_0=%.9f +k=1 +x_0=0 +y_0=0 +ellps=WGS84 +units=m +no_defs",
                lat0, lon0);

        CoordinateReferenceSystem wgs84 = CRS_FACTORY.createFromName("EPSG:4326");
        CoordinateReferenceSystem proj   = CRS_FACTORY.createFromParameters("LOCAL_TM", projDef);
        CoordinateTransform geoToProj = CT_FACTORY.createTransform(wgs84, proj);
        CoordinateTransform projToGeo = CT_FACTORY.createTransform(proj, wgs84);

        // 2) Proietta i punti e prepara vettori/maschere
        final int n = raw.size();
        double[] E = new double[n], N = new double[n];
        double[] X = new double[n], Y = new double[n], Hloc = new double[n], Hell = new double[n];
        boolean[] useH = new boolean[n], useV = new boolean[n];

        ProjCoordinate src = new ProjCoordinate();
        ProjCoordinate dst = new ProjCoordinate();
        for (int i = 0; i < n; i++) {
            RawPoint p = raw.get(i);
            src.x = p.lon; src.y = p.lat;
            geoToProj.transform(src, dst);
            E[i] = dst.x; N[i] = dst.y;
            X[i] = p.xLocal; Y[i] = p.yLocal;
            Hloc[i] = p.zLocal; Hell[i] = p.hEll;
            useH[i] = p.useH;
            useV[i] = p.useV;
        }

        // 3) Fit affine XY (pesa solo Use_Horizontal="Yes")
        double[] abc = fitAffine2DWeighted(E, N, X, useH);
        double[] def = fitAffine2DWeighted(E, N, Y, useH);
        double a=abc[0], b=abc[1], c=abc[2];
        double d=def[0], e=def[1], f=def[2];

        // 4) Fit verticale ΔZ su (E,N) centrati, pesando Use_Vertical
        //    Se pochi punti verticali -> solo offset medio; altrimenti polinomio 2° ordine
        double E0 = meanWeighted(E, useV);
        double N0 = meanWeighted(N, useV);

        boolean hasZData = hasAny(useV) && hasNonZero(Hloc, useV);
        boolean useZ = hasZData;
        double k0=0,k1=0,k2=0,k3=0,k4=0,k5=0;

        if (useZ) {
            double[] dZ = new double[n];
            for (int i = 0; i < n; i++) dZ[i] = Hloc[i] - Hell[i];

            int vertCount = countTrue(useV);
            if (vertCount < 3) {
                // troppo pochi -> offset medio
                double meanDZ = meanWeighted(dZ, useV);
                k0 = meanDZ; k1 = k2 = k3 = k4 = k5 = 0;
            } else {
                double[] kk = fitPoly2DZWeighted(E, N, dZ, E0, N0, useV);
                k0=kk[0]; k1=kk[1]; k2=kk[2]; k3=kk[3]; k4=kk[4]; k5=kk[5];
            }
        }

        // 5) Guard-rail radius: circa 2× la massima baseline dei controlli orizzontali (min 300 m)
        double maxBaseline = 0.0;
        List<Integer> idxH = indicesTrue(useH);
        for (int i = 0; i < idxH.size(); i++) {
            int ii = idxH.get(i);
            for (int j = i + 1; j < idxH.size(); j++) {
                int jj = idxH.get(j);
                double dE = E[ii] - E[jj], dN = N[ii] - N[jj];
                double dBL = Math.hypot(dE, dN);
                if (dBL > maxBaseline) maxBaseline = dBL;
            }
        }
        double polyRadiusM = Math.max(300.0, 2.0 * maxBaseline);
        boolean planeOutside = true;  // fuori raggio -> solo piano (stabile)

        // 6) Ritorna modello
        return new CarlsonLocalization(geoToProj, projToGeo,
                a,b,c, d,e,f,
                useZ, E0,N0, k0,k1,k2,k3,k4,k5,
                polyRadiusM, planeOutside);
    }

    // =====================================================================
    // Trasformazioni
    // =====================================================================
    @Override
    public void toLocalFast(double latDeg, double lonDeg, double hEll, double[] out) {
        final ProjCoordinate geo = tlGeo.get();
        final ProjCoordinate proj = tlProj.get();
        geo.x = lonDeg; geo.y = latDeg;
        geoToProj.transform(geo, proj);

        final double E = proj.x, N = proj.y;

        final double X = a*E + b*N + c;
        final double Y = d*E + e*N + f;

        double Z = hEll;
        if (useZ) {
            double dE = E - E0, dN = N - N0;
            double r = Math.hypot(dE, dN);
            double dZ;
            if (r <= polyRadiusM) {
                dZ = k0 + k1*dE + k2*dN + k3*dE*dE + k4*dN*dN + k5*dE*dN;
            } else if (planeOutsideRadius) {
                dZ = k0 + k1*dE + k2*dN; // piano fuori zona
            } else {
                // Clamping dolce dei termini quadratici (alternativa)
                double s = polyRadiusM / r;
                double dEs = dE * s, dNs = dN * s;
                dZ = k0 + k1*dE + k2*dN + k3*dEs*dEs + k4*dNs*dNs + k5*dEs*dNs;
            }
            Z = hEll + dZ;
        }

        out[0] = X;   // Est
        out[1] = Y;   // Nord
        out[2] = Z;   // Quota locale
    }

    public void toGeoFast(double est, double nord, double quotaLoc, double[] out) {
        // inverti l'affine XY
        double det = a*e - b*d;
        if (Math.abs(det) < 1e-18)
            throw new IllegalStateException("Trasformazione affine XY non invertibile.");
        double inv11 =  e/det, inv12 = -b/det;
        double inv21 = -d/det, inv22 =  a/det;

        double E = inv11*(est - c) + inv12*(nord - f);
        double N = inv21*(est - c) + inv22*(nord - f);

        double hEll = quotaLoc;
        if (useZ) {
            double dE = E - E0, dN = N - N0;
            double r = Math.hypot(dE, dN);
            double dZ;
            if (r <= polyRadiusM) {
                dZ = k0 + k1*dE + k2*dN + k3*dE*dE + k4*dN*dN + k5*dE*dN;
            } else if (planeOutsideRadius) {
                dZ = k0 + k1*dE + k2*dN; // piano fuori zona
            } else {
                double s = polyRadiusM / r;
                double dEs = dE * s, dNs = dN * s;
                dZ = k0 + k1*dE + k2*dN + k3*dEs*dEs + k4*dNs*dNs + k5*dEs*dNs;
            }
            hEll = quotaLoc - dZ;
        }

        final ProjCoordinate proj = tlProj.get();
        final ProjCoordinate geo  = tlGeo.get();
        proj.x = E; proj.y = N;
        projToGeo.transform(proj, geo);

        out[0] = geo.y; // lat
        out[1] = geo.x; // lon
        out[2] = hEll;  // quota ellissoidale
    }

    // =====================================================================
    // Fit helpers (weighted)
    // =====================================================================
    private static double[] fitAffine2DWeighted(double[] E, double[] N, double[] T, boolean[] use) {
        double SEE=0,SEN=0,SE=0,SNN=0,SN=0,S1=0, SET=0,SNT=0,ST=0;
        int count=0;
        for (int i=0;i<E.length;i++){
            if (!use[i]) continue;
            double e=E[i], n=N[i], t=T[i];
            SEE+=e*e; SEN+=e*n; SE+=e;
            SNN+=n*n; SN+=n; S1+=1;
            SET+=e*t; SNT+=n*t; ST+=t;
            count++;
        }
        if (count < 2) throw new IllegalArgumentException("Punti orizzontali insufficienti/validi nel LOC.");
        double[][] A={{SEE,SEN,SE},{SEN,SNN,SN},{SE,SN,S1}};
        double[] B={SET,SNT,ST};
        return solve3x3(A,B);
    }

    private static double[] fitPoly2DZWeighted(double[] E, double[] N, double[] dZ,
                                               double E0, double N0, boolean[] use) {
        final int m=6;
        double[][] ATA=new double[m][m];
        double[]   ATb=new double[m];
        int count=0;
        for (int i=0;i<E.length;i++){
            if (!use[i]) continue;
            count++;
            double dE=E[i]-E0, dN=N[i]-N0;
            double[] phi={1.0, dE, dN, dE*dE, dN*dN, dE*dN};
            for (int r=0;r<m;r++){
                ATb[r]+=phi[r]*dZ[i];
                for (int c=0;c<m;c++) ATA[r][c]+=phi[r]*phi[c];
            }
        }
        if (count < 3) {
            // fallback di sicurezza: offset medio
            double meanDZ=0; int k=0;
            for (int i=0;i<dZ.length;i++) if (use[i]) { meanDZ+=dZ[i]; k++; }
            meanDZ = (k>0 ? meanDZ/k : 0);
            return new double[]{meanDZ,0,0,0,0,0};
        }
        return solveNxN(ATA, ATb);
    }

    // =====================================================================
    // Linear solvers
    // =====================================================================
    private static double[] solve3x3(double[][] A, double[] b){
        double[][] M=new double[3][4];
        for(int i=0;i<3;i++){System.arraycopy(A[i],0,M[i],0,3); M[i][3]=b[i];}
        for(int p=0;p<3;p++){
            int max=p; for(int r=p+1;r<3;r++) if(Math.abs(M[r][p])>Math.abs(M[max][p])) max=r;
            if(max!=p){double[] t=M[p];M[p]=M[max];M[max]=t;}
            double piv=M[p][p]; if (Math.abs(piv)<1e-18) throw new IllegalStateException("Sistema singolare (3x3).");
            for(int c=p;c<4;c++) M[p][c]/=piv;
            for(int r=0;r<3;r++) if(r!=p){double f=M[r][p]; for(int c=p;c<4;c++) M[r][c]-=f*M[p][c];}
        }
        return new double[]{M[0][3],M[1][3],M[2][3]};
    }

    private static double[] solveNxN(double[][] A, double[] b){
        int n=b.length;
        double[][] M=new double[n][n+1];
        for(int i=0;i<n;i++){System.arraycopy(A[i],0,M[i],0,n); M[i][n]=b[i];}
        for(int p=0;p<n;p++){
            int max=p; for(int r=p+1;r<n;r++) if(Math.abs(M[r][p])>Math.abs(M[max][p])) max=r;
            if(max!=p){double[] t=M[p];M[p]=M[max];M[max]=t;}
            double piv=M[p][p]; if (Math.abs(piv)<1e-18) throw new IllegalStateException("Sistema singolare (NxN).");
            for(int c=p;c<=n;c++) M[p][c]/=piv;
            for(int r=0;r<n;r++) if(r!=p){double f=M[r][p]; for(int c=p;c<=n;c++) M[r][c]-=f*M[p][c];}
        }
        double[] x=new double[n];
        for(int i=0;i<n;i++) x[i]=M[i][n];
        return x;
    }

    // =====================================================================
    // Util
    // =====================================================================
    private static double meanWeighted(double[] v, boolean[] use){
        double s=0; int k=0;
        for (int i=0;i<v.length;i++) if (use[i]) { s+=v[i]; k++; }
        return (k>0 ? s/k : 0.0);
    }
    private static boolean hasNonZero(double[] v, boolean[] use){
        for (int i=0;i<v.length;i++) if (use[i] && Math.abs(v[i])>1e-10) return true;
        return false;
    }
    private static boolean hasAny(boolean[] use){
        for (boolean b: use) if (b) return true;
        return false;
    }
    private static int countTrue(boolean[] use){
        int c=0; for (boolean b: use) if (b) c++; return c;
    }
    private static List<Integer> indicesTrue(boolean[] use){
        List<Integer> idx = new ArrayList<>();
        for (int i=0;i<use.length;i++) if (use[i]) idx.add(i);
        return idx;
    }

    // =====================================================================
    // Parser LOC (Carlson / CubeA / SurvCE)
    // =====================================================================
    private static final class RawPoint {
        final double lat, lon, hEll, xLocal, yLocal, zLocal;
        final boolean useH, useV;
        RawPoint(double lat,double lon,double hEll,double x,double y,double z, boolean useH, boolean useV){
            this.lat=lat; this.lon=lon; this.hEll=hEll; this.xLocal=x; this.yLocal=y; this.zLocal=z;
            this.useH=useH; this.useV=useV;
        }
    }

    private static List<RawPoint> parseLocFile(File file) throws Exception {
        List<RawPoint> out = new ArrayList<>();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(file);
        NodeList records = doc.getElementsByTagName("record");
        for (int i=0;i<records.getLength();i++){
            Element rec = (Element) records.item(i);
            String id = rec.getAttribute("id");
            if (id == null) continue;
            if (id.toLowerCase(Locale.ROOT).startsWith("point")) {
                double lat=0, lon=0, hell=0, lx=0, ly=0, lz=0;
                boolean useH = true, useV = true;
                NodeList vals = rec.getElementsByTagName("value");
                for (int j=0;j<vals.getLength();j++){
                    Element v = (Element) vals.item(j);
                    String name = v.getAttribute("name");
                    String val  = v.getAttribute("value");
                    if (val == null || val.isEmpty()) continue;
                    switch (name) {
                        case "Lat": lat = Double.parseDouble(val); break;
                        case "Lon": lon = Double.parseDouble(val); break;
                        case "Ellipsoid_Elv": hell = Double.parseDouble(val); break;
                        case "Local_X":
                        case "LocalX": lx = Double.parseDouble(val); break;
                        case "Local_Y":
                        case "LocalY": ly = Double.parseDouble(val); break;
                        case "Local_Z":
                        case "LocalZ": lz = Double.parseDouble(val); break;
                        case "Use_Horizontal": useH = val.equalsIgnoreCase("Yes"); break;
                        case "Use_Vertical":   useV = val.equalsIgnoreCase("Yes"); break;
                        default: /* ignore others */
                            break;
                    }
                }
                // scarta placeholder vuoti
                if (Double.isNaN(lat) || Double.isNaN(lon)) continue;
                if (lat==0.0 && lon==0.0) continue;
                out.add(new RawPoint(lat,lon,hell,lx,ly,lz,useH,useV));
            }
        }
        if (out.isEmpty())
            throw new IllegalArgumentException("Nessun punto 'Point n' valido nel LOC.");
        return out;
    }
}
