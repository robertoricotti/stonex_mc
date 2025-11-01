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
 * - GTR/GRD/TPF: ignorati se i rispettivi flag == 0 (come negli esempi incollati).
 *
 * Thread-safe: usa ThreadLocal per i buffer PROJ.
 */
public final class LeicaLokLocalization implements LocalizationModel {

    // ---- PROJ4J infra ----
    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateTransformFactory CT_FACTORY = new CoordinateTransformFactory();

    private final CoordinateTransform geoToProj;
    private final CoordinateTransform projToGeo;
    private final ThreadLocal<ProjCoordinate> tlGeo  = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    // ---- Config ricavata dal LOK ----
    private final String csdName;
    private final String ellpsUsed;  // WGS84 o GRS80
    private final double lon0Deg;
    private final double lat0Deg;
    private final double k0;
    private final double falseE;
    private final double falseN;

    private LeicaLokLocalization(CoordinateTransform geoToProj,
                                 CoordinateTransform projToGeo,
                                 String csdName,
                                 String ellpsUsed,
                                 double lon0Deg, double lat0Deg,
                                 double k0,
                                 double falseE, double falseN) {
        this.geoToProj = geoToProj;
        this.projToGeo=projToGeo;
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
            int version = tk.nextIntOrDefault(1);

            // Riga 3: nome/datum di comodo, es. "EUREF89 UTM 33 N"
            String headerName = tk.nextLineTrim(); // può contenere spazi

            // --------- GTR (7-parameter) ----------
            tk.skipUntilStartsWith("// Gtr from local");
            int gtrFlag = tk.nextIntOrDefault(0); // 0=off, 1=on
            tk.skipUntilStartsWith("// Gtr data");
            // 7 numeri + nome datum: DX DY DZ RX RY RZ SCALE  +  datum string
            double gtrDX = tk.nextDoubleOrZero();
            double gtrDY = tk.nextDoubleOrZero();
            double gtrDZ = tk.nextDoubleOrZero();
            double gtrRX = tk.nextDoubleOrZero();
            double gtrRY = tk.nextDoubleOrZero();
            double gtrRZ = tk.nextDoubleOrZero();
            double gtrScale = tk.nextDoubleOrDefault(1.0);
            String  gtrDatum = tk.nextLineTrim(); // es. "None"
            // In questo primo cut non applichiamo GTR se gtrFlag==0 (tipico nei tuoi esempi)

            // --------- CSD (projection) ----------
            tk.skipUntilStartsWith("// Csd data");
            String csdName = tk.nextLineTrim(); // es. "UTM 33 N"
            int csdType  = tk.nextIntOrDefault(2); // spesso 2 = Transverse Mercator
            int csdCode  = tk.nextIntOrDefault(0); // opzionale

            // La maggioranza dei .lok Leica elenca 8 double dopo:
            // [0]=lat0(rad), [1]=FE (a volte con segno), [2]=lon0(rad), [3]=k0,
            // [4]=FN, [5..7] riservati/zero.
            double v0 = tk.nextDoubleOrZero(); // lat0 (rad)
            double v1 = tk.nextDoubleOrZero(); // FE  (può essere -500000)
            double v2 = tk.nextDoubleOrZero(); // lon0 (rad)
            double v3 = tk.nextDoubleOrDefault(1.0); // k0
            double v4 = tk.nextDoubleOrZero(); // FN
            double v5 = tk.nextDoubleOrZero();
            double v6 = tk.nextDoubleOrZero();
            double v7 = tk.nextDoubleOrZero();

            // --------- GRD/TPF (non usati qui) ----------
            tk.skipUntilStartsWith("// Grd data");
            int grdFlag = tk.nextIntOrDefault(0);

            tk.skipUntilStartsWith("// Use tpf");
            int tpfFlag = tk.nextIntOrDefault(0);
            tk.skipUntilStartsWith("// Tpf data");
            // (ignoriamo eventuali righe successive)

            // ---- Ellissoide: scegli GRS80 per ETRS/EUREF/GRS80, altrimenti WGS84
            String ellps = chooseEllps(headerName, csdName, gtrDatum);

            // ---- Mappa CSD -> TM
            if (csdType != 2) {
                // Nei tuoi esempi (UTM/MTM) è sempre 2 (Transverse Mercator).
                // Se trovi altro: qui potresti estendere.
                csdType = 2;
            }

            double lat0Deg = Math.toDegrees(v0);
            double lon0Deg = Math.toDegrees(v2);
            double k0 = v3;
            double FE = Math.abs(v1); // normalizzo easting a valore positivo
            double FN = v4;

            // Alcuni .lok scambiano FE/FN o usano segni: fallback prudente
            // Se FE==0 e |v4|~500000, prendi FE=abs(v4) e FN=v1
            if (FE == 0.0 && Math.abs(v4) > 100000 && Math.abs(v4) < 1000000) {
                FE = Math.abs(v4);
                FN = v1;
            }

            // Default TM sensata se lon0==0 e il nome "UTM xx N"
            if (Math.abs(lon0Deg) < 1e-12 && csdName.toUpperCase(Locale.ROOT).contains("UTM")) {
                // Estrai zona da nome
                int zone = extractUtmZone(csdName);
                if (zone >= 1 && zone <= 60) {
                    lon0Deg = zone * 6 - 183; // classico UTM lon0
                }
                if (k0 == 0.0) k0 = 0.9996;
                if (FE == 0.0) FE = 500000.0;
                // FN resta 0 in emisfero nord
            }

            String projDef = String.format(Locale.US,
                    "+proj=tmerc +lat_0=%.10f +lon_0=%.10f +k=%.12f +x_0=%.3f +y_0=%.3f +ellps=%s +units=m +no_defs",
                    lat0Deg, lon0Deg, (k0==0.0?1.0:k0), FE, FN, ellps);

            CoordinateReferenceSystem wgs = CRS_FACTORY.createFromParameters("WGS84", "+proj=longlat +datum=WGS84 +no_defs");
            CoordinateReferenceSystem proj = CRS_FACTORY.createFromParameters("LOK_TM", projDef);
            CoordinateTransform geoToProj = CT_FACTORY.createTransform(wgs, proj);
            CoordinateTransform projToGeo = CT_FACTORY.createTransform(proj, wgs);

            return new LeicaLokLocalization(geoToProj,projToGeo, csdName, ellps, lon0Deg, lat0Deg, k0, FE, FN);
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
        out[0] = p.x;   // Est
        out[1] = p.y;   // Nord
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
        // es. "UTM 33 N" -> 33
        String u = csdName.toUpperCase(Locale.ROOT);
        int idx = u.indexOf("UTM");
        if (idx < 0) return -1;
        // cerca un numero dopo "UTM"
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
        private String buffered;

        LokTokens(BufferedReader br) { this.br = br; }

        String nextLineRaw() throws IOException {
            return br.readLine();
        }

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
            // se non trovato, non esplodere: il file potrebbe essere minimale
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
            try {
                // I .lok usano notazione scientifica C (es. 9.9960000000000004e-001)
                return Double.parseDouble(s);
            } catch (Exception e) {
                return def;
            }
        }
    }
}
