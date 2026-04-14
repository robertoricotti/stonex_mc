package packexcalib.gnss;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import gui.MyApp;

/**
 * Trasformatore HEPOS native-PROJ:
 * - Da HTRS07 / ETRS89 geodetico (lat, lon, h)
 * - A EGSA87 / TM87 (EPSG:2100) con precisione centimetrica
 * tramite:
 * 1) HTRS07 geodetico -> geocentrico
 * 2) Helmert 7 parametri
 * 3) geocentrico -> geodetico
 * 4) geodetico -> TM87
 * 5) correzione griglie dE/dN
 * <p>
 * NOTE IMPORTANTI:
 * - Input pubblico del metodo transform(): lat, lon, h
 * - Internamente PROJ lavora con x=lon, y=lat
 * <p>
 * con il codice esistente (shifted.x / shifted.y / shifted.z).
 */
public final class GridShiftTransformer implements AutoCloseable {

    // ---------------------------------------------------------------------
    // CRS / PROJ definitions
    // ---------------------------------------------------------------------
    private static final String HTRS07_GEO =
            "+proj=longlat +ellps=GRS80 +no_defs +type=crs";

    private static final String GRS80_GEOCENT =
            "+proj=geocent +ellps=GRS80 +units=m +no_defs +type=crs";

    private static final String TM87 =
            "+proj=tmerc +lat_0=0 +lon_0=24 +k=0.9996 " +
                    "+x_0=500000 +y_0=-2000000 +ellps=GRS80 +units=m +no_defs +type=crs";

    // ---------------------------------------------------------------------
    // 1. HELMERT 7 PARAMETRI HEPOS
    // ---------------------------------------------------------------------
    private static final double TX = 203.437;    // m
    private static final double TY = -73.461;    // m
    private static final double TZ = -243.594;   // m

    private static final double EX = Math.toRadians(-0.170 / 3600.0); // rad
    private static final double EY = Math.toRadians(-0.060 / 3600.0); // rad
    private static final double EZ = Math.toRadians(-0.151 / 3600.0); // rad

    private static final double DS = -0.294e-6; // ppm -> unitless

    private volatile double aggiuntaHDT;

    // ---------------------------------------------------------------------
    // 2. GESTIONE FILE GRIGLIA
    // ---------------------------------------------------------------------
    static final class GridFile {
        final int rows;
        final int cols;
        final double step;
        final double nMin;
        final double eMin;
        final double[][] values;

        GridFile(File file) throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                rows = Integer.parseInt(requireLine(br, "rows").trim());
                cols = Integer.parseInt(requireLine(br, "cols").trim());
                step = Double.parseDouble(requireLine(br, "step").trim());
                nMin = Double.parseDouble(requireLine(br, "nMin").trim());
                eMin = Double.parseDouble(requireLine(br, "eMin").trim());

                values = new double[rows][cols];

                int r = 0;
                int c = 0;
                String line;
                while ((line = br.readLine()) != null && r < rows) {
                    StringTokenizer st = new StringTokenizer(line);
                    while (st.hasMoreTokens() && c < cols) {
                        // da cm a m
                        values[r][c] = Double.parseDouble(st.nextToken()) / 100.0;
                        c++;
                    }
                    if (c == cols) {
                        c = 0;
                        r++;
                    }
                }

                if (r < rows) {
                    throw new IOException("Griglia incompleta: attese " + rows + " righe, lette " + r);
                }
            }
        }

        private static String requireLine(BufferedReader br, String fieldName) throws IOException {
            String line = br.readLine();
            if (line == null) {
                throw new IOException("EOF inatteso leggendo il campo " + fieldName);
            }
            return line;
        }
    }

    static final class GridInterpolator {
        private final GridFile grid;

        GridInterpolator(GridFile grid) {
            this.grid = grid;
        }

        double interpolate(double e, double n) {
            int col = (int) Math.floor((e - grid.eMin) / grid.step);
            int row = (int) Math.floor((n - grid.nMin) / grid.step);

            if (col < 0 || col >= grid.cols - 1 || row < 0 || row >= grid.rows - 1) {
                throw new IllegalArgumentException(
                        "Coordinate fuori griglia: E=" + e + " N=" + n
                );
            }

            double e0 = grid.eMin + col * grid.step;
            double n0 = grid.nMin + row * grid.step;

            double t = (e - e0) / grid.step;
            double u = (n - n0) / grid.step;

            double v00 = grid.values[row][col];
            double v10 = grid.values[row][col + 1];
            double v01 = grid.values[row + 1][col];
            double v11 = grid.values[row + 1][col + 1];

            return (1.0 - t) * (1.0 - u) * v00
                    + t * (1.0 - u) * v10
                    + (1.0 - t) * u * v01
                    + t * u * v11;
        }
    }

    // ---------------------------------------------------------------------
    // 3. ATTRIBUTI DI TRASFORMAZIONE
    // ---------------------------------------------------------------------
    private final GridInterpolator dEInterp;
    private final GridInterpolator dNInterp;

    private final NativeProjTransformer geoToCart;
    private final NativeProjTransformer cartToGeo;
    private final NativeProjTransformer geoToTM87;

    private static double[] helmert(double x, double y, double z) {
        double x2 = TX + (1.0 + DS) * (x + EZ * y - EY * z);
        double y2 = TY + (1.0 + DS) * (-EZ * x + y + EX * z);
        double z2 = TZ + (1.0 + DS) * (EY * x - EX * y + z);
        return new double[]{x2, y2, z2};
    }

    public GridShiftTransformer(File dEfile, File dNfile) throws IOException {
        GridFile gridE = new GridFile(dEfile);
        GridFile gridN = new GridFile(dNfile);

        dEInterp = new GridInterpolator(gridE);
        dNInterp = new GridInterpolator(gridN);

        geoToCart = new NativeProjTransformer();
        cartToGeo = new NativeProjTransformer();
        geoToTM87 = new NativeProjTransformer();

        boolean ok = false;
        try {
            Context context = requireAppContext();

            geoToCart.init(context);
            geoToCart.initFromParameters(HTRS07_GEO, GRS80_GEOCENT);

            cartToGeo.init(context);
            cartToGeo.initFromParameters(GRS80_GEOCENT, HTRS07_GEO);

            geoToTM87.init(context);
            geoToTM87.initFromParameters(HTRS07_GEO, TM87);

            ok = true;
        } catch (Exception e) {
            closeQuietly(geoToCart);
            closeQuietly(cartToGeo);
            closeQuietly(geoToTM87);
            throw new IOException("Init GridShiftTransformer native PROJ fallita", e);
        } finally {
            if (!ok) {
                aggiuntaHDT = 0.0;
            }
        }
    }

    private static Context requireAppContext() {
        if (MyApp.visibleActivity == null || MyApp.visibleActivity.getApplicationContext() == null) {
            throw new IllegalStateException("Context Android non disponibile per inizializzare PROJ");
        }
        return MyApp.visibleActivity.getApplicationContext();
    }

    // ---------------------------------------------------------------------
    // 4. TRASFORMAZIONE COMPLETA
    // ---------------------------------------------------------------------
    public double[] transform(double lat, double lon, double h) {
        try {
            // Step 1: geodetico HTRS07 (lon,lat,h) -> geocentrico
            double[] cartHTRS = geoToCart.transformPrepared(lon, lat, h);

            // Step 2: Helmert 7 parametri
            double[] xyzEGSA = helmert(cartHTRS[0], cartHTRS[1], cartHTRS[2]);

            // Step 3: geocentrico -> geodetico EGSA87-like su GRS80
            double[] geoEGSA = cartToGeo.transformPrepared(xyzEGSA[0], xyzEGSA[1], xyzEGSA[2]);

            // Step 4: geodetico -> TM87
            double[] tm87 = geoToTM87.transformPrepared(geoEGSA[0], geoEGSA[1], geoEGSA[2]);

            double e = tm87[0];
            double n = tm87[1];

            // Step 5: correzioni da griglia
            double dE = dEInterp.interpolate(e, n);
            double dN = dNInterp.interpolate(e, n);

            // Heading numerico sulla griglia NON corretta, come nel comportamento originale
            aggiuntaHDT = gridHeadingDeltaDeg(lat, lon);

            // Step 6: applica correzioni
            return new double[]{e + dE, n + dN, geoEGSA[2]};

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("GridShift transform fallita", ex);
        }
    }

    private double gridHeadingDeltaDeg(double latDeg, double lonDeg) {
        final double epsDeg = 1e-4; // ~11 m

        try {
            double[] baseProj = geoToTM87.transformPrepared(lonDeg, latDeg, 0.0);
            double[] northProj = geoToTM87.transformPrepared(lonDeg, latDeg + epsDeg, 0.0);

            double dE = northProj[0] - baseProj[0];
            double dN = northProj[1] - baseProj[1];

            return Math.toDegrees(Math.atan2(dE, dN));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getAggiuntaHDT() {
        return aggiuntaHDT;
    }

    @Override
    public void close() {
        closeQuietly(geoToCart);
        closeQuietly(cartToGeo);
        closeQuietly(geoToTM87);
    }

    private static void closeQuietly(AutoCloseable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Exception ignored) {
        }
    }
}