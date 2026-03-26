package packexcalib.gnss;

import static services.UpdateValuesService.wgsToUtm;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Trasformatore HEPOS:
 *  - Da HTRS07 (ETRS89) lat/lon/h
 *  - A EGSA87 (EPSG:2100) con precisione centimetrica
 *    tramite Helmert 7 param + proiezione + griglie dE/dN.
 */
public class GridShiftTransformer {
    private static final ProjCoordinate baseWgs = new ProjCoordinate();
    private static final ProjCoordinate northWgs = new ProjCoordinate();
    private static final ProjCoordinate baseProj = new ProjCoordinate();
    private static final ProjCoordinate northProj = new ProjCoordinate();

    // -------------------------------
    // 1. HELMERT 7 PARAMETRI HEPOS
    // -------------------------------
    private static final double TX = 203.437;   // m
    private static final double TY = -73.461;  // m
    private static final double TZ = -243.594; // m

    private static final double EX = Math.toRadians(-0.170 / 3600.0); // rad
    private static final double EY = Math.toRadians(-0.060 / 3600.0);
    private static final double EZ = Math.toRadians(-0.151 / 3600.0);

    private static final double DS = -0.294e-6; // ppm -> unitless
    private static double aggiuntaHDT;

    private static double[] helmert(double X, double Y, double Z) {
        double X2 = TX + (1 + DS) * (X + EZ * Y - EY * Z);
        double Y2 = TY + (1 + DS) * (-EZ * X + Y + EX * Z);
        double Z2 = TZ + (1 + DS) * (EY * X - EX * Y + Z);
        return new double[]{X2, Y2, Z2};
    }

    // -------------------------------
    // 2. GESTIONE FILE GRIGLIA
    // -------------------------------
    static class GridFile {
        int rows, cols;
        double step;
        double nMin, eMin;
        double[][] values;

        GridFile(File file) throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                rows = Integer.parseInt(br.readLine().trim());
                cols = Integer.parseInt(br.readLine().trim());
                step = Double.parseDouble(br.readLine().trim());
                nMin = Double.parseDouble(br.readLine().trim());
                eMin = Double.parseDouble(br.readLine().trim());

                values = new double[rows][cols];
                int r = 0, c = 0;
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
            }
        }
    }

    static class GridInterpolator {
        private final GridFile grid;

        GridInterpolator(GridFile grid) {
            this.grid = grid;
        }

        double interpolate(double E, double N) {
            int col = (int) Math.floor((E - grid.eMin) / grid.step);
            int row = (int) Math.floor((N - grid.nMin) / grid.step);

            if (col < 0 || col >= grid.cols - 1 || row < 0 || row >= grid.rows - 1) {
                throw new IllegalArgumentException("Coordinate fuori griglia");
            }

            double e0 = grid.eMin + col * grid.step;
            double n0 = grid.nMin + row * grid.step;

            double t = (E - e0) / grid.step;
            double u = (N - n0) / grid.step;

            double v00 = grid.values[row][col];
            double v10 = grid.values[row][col + 1];
            double v01 = grid.values[row + 1][col];
            double v11 = grid.values[row + 1][col + 1];

            return (1 - t) * (1 - u) * v00 +
                    t * (1 - u) * v10 +
                    (1 - t) * u * v01 +
                    t * u * v11;
        }
    }

    // -------------------------------
    // 3. ATTRIBUTI DI TRASFORMAZIONE
    // -------------------------------
    private final GridInterpolator dEInterp;
    private final GridInterpolator dNInterp;

    private final CoordinateTransform geoToCart;  // geodetico -> cartesiano
    private final CoordinateTransform cartToGeo;  // cartesiano -> geodetico
    private final CoordinateTransform geoToTM87;  // geodetico -> EGSA87 proiettato



    public GridShiftTransformer(File dEfile, File dNfile) throws IOException {
        GridFile gridE = new GridFile(dEfile);
        GridFile gridN = new GridFile(dNfile);

        dEInterp = new GridInterpolator(gridE);
        dNInterp = new GridInterpolator(gridN);

        CRSFactory crsFactory = new CRSFactory();
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

        CoordinateReferenceSystem geo = crsFactory.createFromParameters("HTRS07",
                "+proj=longlat +ellps=GRS80 +no_defs");

        CoordinateReferenceSystem geocent = crsFactory.createFromParameters("Geocent",
                "+proj=geocent +ellps=GRS80 +units=m +no_defs");


        // EGSA87 proiettato (EPSG:2100)
        CoordinateReferenceSystem tm87 = crsFactory.createFromParameters("TM87",
                "+proj=tmerc +lat_0=0 +lon_0=24 +k=0.9996 +x_0=500000 +y_0=-2000000 +ellps=GRS80 +units=m +no_defs");
        geoToCart = ctFactory.createTransform(geo, geocent);
        cartToGeo = ctFactory.createTransform(geocent, geo);
        geoToTM87 = ctFactory.createTransform(geo, tm87);
    }

    // -------------------------------
    // 4. TRASFORMAZIONE COMPLETA
    // -------------------------------
    public ProjCoordinate transform(double lat, double lon, double h) {
        // Step 1: da geodetico (lat,lon,h) -> cartesiano (X,Y,Z)
        ProjCoordinate geoCoord = new ProjCoordinate(lon, lat, h);
        ProjCoordinate cartHTRS = new ProjCoordinate();
        geoToCart.transform(geoCoord, cartHTRS);

        // Step 2: applica Helmert
        double[] xyzEGSA = helmert(cartHTRS.x, cartHTRS.y, cartHTRS.z);

        // Step 3: cartesiano -> geodetico EGSA87
        ProjCoordinate cartEGSA = new ProjCoordinate(xyzEGSA[0], xyzEGSA[1], xyzEGSA[2]);
        ProjCoordinate geoEGSA = new ProjCoordinate();
        cartToGeo.transform(cartEGSA, geoEGSA);

        // Step 4: proiezione TM87 (E,N)
        ProjCoordinate tm87Coord = new ProjCoordinate();
        geoToTM87.transform(geoEGSA, tm87Coord);

        double E = tm87Coord.x;
        double N = tm87Coord.y;

        // Step 5: correzioni da griglia
        double dE = dEInterp.interpolate(E, N);
        double dN = dNInterp.interpolate(E, N);
        aggiuntaHDT=gridHeadingDeltaDeg(lat,lon);

        // Step 6: applica correzioni
        return new ProjCoordinate(E + dE, N + dN, geoEGSA.z);
    }
    private double gridHeadingDeltaDeg(double latDeg, double lonDeg) {

        if (geoToTM87 == null) return 0.0;

        final double epsDeg = 1e-4; // ~11 m

        baseWgs.x = lonDeg;
        baseWgs.y = latDeg;

        northWgs.x = lonDeg;
        northWgs.y = latDeg + epsDeg;

        geoToTM87.transform(baseWgs, baseProj);
        geoToTM87.transform(northWgs, northProj);

        double dE = northProj.x - baseProj.x;
        double dN = northProj.y - baseProj.y;

        return Math.toDegrees(Math.atan2(dE, dN));
    }

    public  double getAggiuntaHDT() {
        return aggiuntaHDT;
    }

}
