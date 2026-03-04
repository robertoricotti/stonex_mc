package packexcalib.gnss;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Lettore minimale per file .LOK (Leica SBG) e trasformazione geodetiche -> locali.
 * Copre i casi comuni: UTM/TM (CSD attivo), GTR/GRD/TPF disattivi.
 *
 * - CSD: Transverse Mercator con lon0, k0, FE/ FN dai numeri del blocco.
 * - Ellissoide: GRS80 se nome contiene ETRS/EUREF/GRS80, altrimenti WGS84.
 * - GTR/GRD/TPF: ignorati se i rispettivi flag == 0 (come negli esempi).
 *
 * Thread-safe: usa ThreadLocal per i buffer PROJ.
 *
 * Heading delta:
 * - out[3] = delta (gradi) da sommare a HDT True (0=N,90=E) per ottenere heading GRID (TM/UTM).
 * - delta = -gamma, dove gamma è la convergenza meridiana nel punto.
 */
public final class LeicaLokLocalization implements LocalizationModel {

    // ---- PROJ4J infra ----
    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateTransformFactory CT_FACTORY = new CoordinateTransformFactory();

    private final CoordinateTransform geoToProj;
    private final CoordinateTransform projToGeo;
    private final ThreadLocal<ProjCoordinate> tlGeo  = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    // ---- Config ricavata dal LOK (solo diagnostica/riuso) ----
    @SuppressWarnings("unused")
    private final String csdName;
    @SuppressWarnings("unused")
    private final String ellpsUsed;  // WGS84 o GRS80
    @SuppressWarnings("unused")
    private final double lon0Deg;
    @SuppressWarnings("unused")
    private final double lat0Deg;
    @SuppressWarnings("unused")
    private final double k0;
    @SuppressWarnings("unused")
    private final double falseE;
    @SuppressWarnings("unused")
    private final double falseN;

    private LeicaLokLocalization(CoordinateTransform geoToProj,
                                 CoordinateTransform projToGeo,
                                 String csdName,
                                 String ellpsUsed,
                                 double lon0Deg, double lat0Deg,
                                 double k0,
                                 double falseE, double falseN) {
        this.geoToProj = geoToProj;
        this.projToGeo = projToGeo;
        this.csdName = csdName;
        this.ellpsUsed = ellpsUsed;
        this.lon0Deg = lon0Deg;
        this.lat0Deg = lat0Deg;
        this.k0 = k0;
        this.falseE = falseE;
        this.falseN = falseN;
    }

    // =====================================================================
    // Factory
    // =====================================================================
    public static LeicaLokLocalization fromLokFile(File lok) throws Exception {
        if (lok == null) throw new IllegalArgumentException("lok == null");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(lok), StandardCharsets.UTF_8))) {

            LokTokens tk = new LokTokens(br);

            // Riga 1: commento // SBG Loc file (ignorato)
            // Riga 2: versione (int) -> ignorabile
            tk.nextIntOrDefault(1);

            // Riga 3: nome/datum di comodo, es. "EUREF89 UTM 33 N"
            String headerName = tk.nextLineTrim();

            // --------- GTR (7-parameter) ----------
            tk.skipUntilStartsWith("// Gtr from local");
            int gtrFlag = tk.nextIntOrDefault(0);
            tk.skipUntilStartsWith("// Gtr data");
            // 7 numeri + nome datum
            double gtrDX = tk.nextDoubleOrZero();
            double gtrDY = tk.nextDoubleOrZero();
            double gtrDZ = tk.nextDoubleOrZero();
            double gtrRX = tk.nextDoubleOrZero();
            double gtrRY = tk.nextDoubleOrZero();
            double gtrRZ = tk.nextDoubleOrZero();
            double gtrScale = tk.nextDoubleOrDefault(1.0);
            String  gtrDatum = tk.nextLineTrim();

            // --------- CSD (projection) ----------
            tk.skipUntilStartsWith("// Csd data");
            String csdName = tk.nextLineTrim();
            int csdType  = tk.nextIntOrDefault(2);
            tk.nextIntOrDefault(0); // csdCode (unused)

            // Vettore 8 double:
            // [0]=lat0(rad), [1]=FE, [2]=lon0(rad), [3]=k0, [4]=FN, [5..7]=0
            double v0 = tk.nextDoubleOrZero();          // lat0 (rad)
            double v1 = tk.nextDoubleOrZero();          // FE (a volte con segno)
            double v2 = tk.nextDoubleOrZero();          // lon0 (rad)
            double v3 = tk.nextDoubleOrDefault(1.0);    // k0
            double v4 = tk.nextDoubleOrZero();          // FN
            tk.nextDoubleOrZero();
            tk.nextDoubleOrZero();
            tk.nextDoubleOrZero();

            // --------- GRD/TPF (non usati qui) ----------
            tk.skipUntilStartsWith("// Grd data");
            int grdFlag = tk.nextIntOrDefault(0);

            tk.skipUntilStartsWith("// Use tpf");
            int tpfFlag = tk.nextIntOrDefault(0);
            tk.skipUntilStartsWith("// Tpf data");

            // ---- Ellissoide
            String ellps = chooseEllps(headerName, csdName, gtrDatum);

            // ---- Mappa CSD -> TM
            if (csdType != 2) csdType = 2;

            double lat0Deg = Math.toDegrees(v0);
            double lon0Deg = Math.toDegrees(v2);
            double k0 = v3;
            double FE = Math.abs(v1);
            double FN = v4;

            // Fallback prudente FE/FN
            if (FE == 0.0 && Math.abs(v4) > 100000 && Math.abs(v4) < 1000000) {
                FE = Math.abs(v4);
                FN = v1;
            }

            // Default UTM se manca lon0
            if (Math.abs(lon0Deg) < 1e-12 && csdName.toUpperCase(Locale.ROOT).contains("UTM")) {
                int zone = extractUtmZone(csdName);
                if (zone >= 1 && zone <= 60) {
                    lon0Deg = zone * 6 - 183;
                }
                if (k0 == 0.0) k0 = 0.9996;
                if (FE == 0.0) FE = 500000.0;
            }

            String projDef = String.format(Locale.US,
                    "+proj=tmerc +lat_0=%.10f +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                    lat0Deg, lon0Deg, (k0==0.0?1.0:k0), FE, FN, ellps);

            CoordinateReferenceSystem wgs = CRS_FACTORY.createFromParameters(
                    "WGS84", "+proj=longlat +datum=WGS84 +no_defs"
            );
            CoordinateReferenceSystem proj = CRS_FACTORY.createFromParameters("LOK_TM", projDef);
            CoordinateTransform geoToProj = CT_FACTORY.createTransform(wgs, proj);
            CoordinateTransform projToGeo = CT_FACTORY.createTransform(proj, wgs);

            return new LeicaLokLocalization(geoToProj, projToGeo,
                    csdName, ellps, lon0Deg, lat0Deg, k0, FE, FN);
        }
    }

    // =====================================================================
    // Runtime transform
    // =====================================================================
    @Override
    public void toLocalFast(double lat, double lon, double h, double[] out) {
        final ProjCoordinate g = tlGeo.get();
        final ProjCoordinate p = tlProj.get();
        g.x = lon; g.y = lat; // PROJ4J: x=lon, y=lat
        geoToProj.transform(g, p);
        out[0] = p.x;   // Est (grid)
        out[1] = p.y;   // Nord (grid)
        out[2] = h;     // quota ellissoidale (il .lok non contiene fitting quota)
    }

    @Override
    public void toGeoFast(double E, double N, double H, double[] out) {
        final ProjCoordinate p = tlProj.get();
        final ProjCoordinate g = tlGeo.get();

        p.x = E;
        p.y = N;
        projToGeo.transform(p, g);

        out[0] = g.y; // lat
        out[1] = g.x; // lon
        out[2] = H;   // quota ellissoidale (nessuna correzione in .lok)
    }

    /**
     * Convergenza meridiana γ (gradi) nel punto, calcolata numericamente:
     * azimut (0=N,90=E) del vettore True-North espresso in coordinate proiettate.
     */
    private double meridianConvergenceDeg(double latDeg, double lonDeg) {
        final double eps = 1e-5; // ~1.1 m

        final ProjCoordinate g = tlGeo.get();
        final ProjCoordinate p = tlProj.get();

        // punto base
        g.x = lonDeg;
        g.y = latDeg;
        geoToProj.transform(g, p);
        final double E1 = p.x;
        final double N1 = p.y;

        // punto leggermente a nord
        g.x = lonDeg;
        g.y = latDeg + eps;
        geoToProj.transform(g, p);
        final double dE = p.x - E1;
        final double dN = p.y - N1;

        double gammaRad = Math.atan2(dE, dN); // 0=N, 90=E
        return Math.toDegrees(gammaRad);
    }

    /**
     * out[3] = delta (gradi) da sommare all’HDT True per ottenere heading grid TM/UTM.
     * True -> Grid: H_grid = H_true - gamma  => delta = -gamma
     */
    @Override
    public void toLocalFastWithHeadingDelta(double lat, double lon, double h, double[] out) {
        toLocalFast(lat, lon, h, out);
        if (out.length > 3) {
            out[3] = -meridianConvergenceDeg(lat, lon);
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================
    private static String chooseEllps(String... names) {
        for (String s : names) {
            if (s == null) continue;
            String u = s.toUpperCase(Locale.ROOT);
            if (u.contains("GRS80") || u.contains("ETRS") || u.contains("EUREF"))
                return "GRS80";
        }
        return "WGS84";
    }

    private static int extractUtmZone(String csdName) {
        String u = csdName.toUpperCase(Locale.ROOT);
        int idx = u.indexOf("UTM");
        if (idx < 0) return -1;
        for (int i = idx+3; i < u.length(); i++) {
            if (Character.isDigit(u.charAt(i))) {
                int j = i;
                while (j < u.length() && Character.isDigit(u.charAt(j))) j++;
                try {
                    return Integer.parseInt(u.substring(i, j));
                } catch (Exception ignore) { return -1; }
            }
        }
        return -1;
    }

    // Reader che salta commenti `//` e fornisce next-as-* con default
    private static final class LokTokens {
        private final BufferedReader br;

        LokTokens(BufferedReader br) { this.br = br; }

        String nextLineTrim() throws IOException {
            String s = br.readLine();
            if (s == null) return "";
            int c = s.indexOf("//");
            if (c >= 0) s = s.substring(0, c);
            return s.trim();
        }

        void skipUntilStartsWith(String marker) throws IOException {
            String s;
            while ((s = br.readLine()) != null) {
                String t = s.trim();
                if (t.startsWith(marker)) return;
            }
        }

        int nextIntOrDefault(int def) throws IOException {
            String s = nextLineTrim();
            if (s.isEmpty()) return def;
            try { return Integer.parseInt(s); } catch (Exception e) { return def; }
        }

        double nextDoubleOrZero() throws IOException {
            return nextDoubleOrDefault(0.0);
        }

        double nextDoubleOrDefault(double def) throws IOException {
            String s = nextLineTrim();
            if (s.isEmpty()) return def;
            try { return Double.parseDouble(s); } catch (Exception e) { return def; }
        }
    }
}