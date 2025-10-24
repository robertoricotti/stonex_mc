package packexcalib.gnss;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Implementazione per file Carlson .LOC.
 * Legge il file solo una volta in fromLocFile(...) e costruisce un modello immutabile e thread-safe.
 */
public final class CarlsonLocalization implements LocalizationModel {

    // Proj4j factories (shared, threadsafe)
    private static final CRSFactory crsFactory = new CRSFactory();
    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

    private final CoordinateReferenceSystem wgs84;
    private final CoordinateReferenceSystem projected;
    private final CoordinateTransform geoToProj;

    // Parametri affine 2D per X,Y
    private final double a0, a1, a2;
    private final double b0, b1, b2;

    // Parametri Z: Z = c0 + c1*h + c2*E + c3*N
    private final double c0, c1, c2, c3;

    // Reusable ProjCoordinate to avoid allocations
    private final ThreadLocal<ProjCoordinate> tlGeo = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    // Private constructor
    private CarlsonLocalization(CoordinateReferenceSystem projected,
                                double a0, double a1, double a2,
                                double b0, double b1, double b2,
                                double c0, double c1, double c2, double c3) {
        this.wgs84 = crsFactory.createFromName("EPSG:4326");
        this.projected = projected;
        this.geoToProj = ctFactory.createTransform(wgs84, projected);

        this.a0 = a0; this.a1 = a1; this.a2 = a2;
        this.b0 = b0; this.b1 = b1; this.b2 = b2;
        this.c0 = c0; this.c1 = c1; this.c2 = c2; this.c3 = c3;
    }

    /**
     * Crea un'istanza leggendo il file .loc (Carlson XML). File letto una sola volta.
     */
    public static CarlsonLocalization fromLocFile(File f) throws Exception {
        List<LocPoint> pts = readLocFile(f);
        if (pts.size() < 3) throw new IllegalArgumentException("Serve almeno 3 punti nel .LOC per stimare la trasformazione");

        // calcola zona UTM media
        double avgLon = pts.stream().mapToDouble(p -> p.lon).average().orElse(0.0);
        int utmZone = (int) Math.floor((avgLon + 180.0) / 6.0) + 1;
        String projDef = "+proj=utm +zone=" + utmZone + " +datum=WGS84 +units=m +no_defs";
        CoordinateReferenceSystem projected = crsFactory.createFromParameters("utm_auto", projDef);

        // calcola parametri affine 2D e Z
        LocalTransform t = computeLocalTransform(pts, projected);

        return new CarlsonLocalization(projected,
                t.a0, t.a1, t.a2,
                t.b0, t.b1, t.b2,
                t.c0, t.c1, t.c2, t.c3);
    }

    private static final class LocPoint { double lat, lon, h, x, y, z; }

    private static List<LocPoint> readLocFile(File file) throws Exception {
        List<LocPoint> points = new ArrayList<>();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(file);

        NodeList records = doc.getElementsByTagName("record");
        for (int i = 0; i < records.getLength(); i++) {
            Element rec = (Element) records.item(i);
            String id = rec.getAttribute("id");
            if (id != null && id.startsWith("Point")) {
                LocPoint p = new LocPoint();
                NodeList vals = rec.getElementsByTagName("value");
                for (int j = 0; j < vals.getLength(); j++) {
                    Element v = (Element) vals.item(j);
                    String name = v.getAttribute("name");
                    String val = v.getAttribute("value");
                    switch (name) {
                        case "Lat": p.lat = Double.parseDouble(val); break;
                        case "Lon": p.lon = Double.parseDouble(val); break;
                        case "Ellipsoid_Elv": p.h = Double.parseDouble(val); break;
                        case "Local_X": p.x = Double.parseDouble(val); break;
                        case "Local_Y": p.y = Double.parseDouble(val); break;
                        case "Local_Z": p.z = Double.parseDouble(val); break;
                    }
                }
                // filtro punti nulli
                if (!(p.lat == 0.0 && p.lon == 0.0)) points.add(p);
            }
        }
        return points;
    }

    private static final class LocalTransform {
        double a0,a1,a2,b0,b1,b2,c0,c1,c2,c3;
    }

    private static LocalTransform computeLocalTransform(List<LocPoint> points, CoordinateReferenceSystem projected) {
        CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326");
        CoordinateTransform transform = ctFactory.createTransform(wgs84, projected);

        int n = points.size();
        double[][] A = new double[n][3];
        double[] Xloc = new double[n];
        double[] Yloc = new double[n];
        double[] Zloc = new double[n];
        double[] H = new double[n];

        for (int i = 0; i < n; i++) {
            LocPoint p = points.get(i);
            ProjCoordinate src = new ProjCoordinate(p.lon, p.lat);
            ProjCoordinate dst = new ProjCoordinate();
            transform.transform(src, dst);

            A[i][0] = 1.0; A[i][1] = dst.x; A[i][2] = dst.y;
            Xloc[i] = p.x; Yloc[i] = p.y; Zloc[i] = p.z; H[i] = p.h;
        }

        RealMatrix M = new Array2DRowRealMatrix(A);
        DecompositionSolver solver = new QRDecomposition(M).getSolver();

        RealVector solX = solver.solve(new ArrayRealVector(Xloc));
        RealVector solY = solver.solve(new ArrayRealVector(Yloc));

        // Z fitting: use polynomial a0 + a1*h + a2*E + a3*N  (simple linear + E,N)
        double[][] A3 = new double[n][4];
        for (int i = 0; i < n; i++) {
            A3[i][0] = 1.0;
            A3[i][1] = H[i];
            A3[i][2] = A[i][1];
            A3[i][3] = A[i][2];
        }
        RealMatrix M3 = new Array2DRowRealMatrix(A3);
        DecompositionSolver s3 = new QRDecomposition(M3).getSolver();
        RealVector solZ = s3.solve(new ArrayRealVector(Zloc));

        LocalTransform t = new LocalTransform();
        t.a0 = solX.getEntry(0); t.a1 = solX.getEntry(1); t.a2 = solX.getEntry(2);
        t.b0 = solY.getEntry(0); t.b1 = solY.getEntry(1); t.b2 = solY.getEntry(2);
        t.c0 = solZ.getEntry(0); t.c1 = solZ.getEntry(1); t.c2 = solZ.getEntry(2); t.c3 = solZ.getEntry(3);
        return t;
    }

    @Override
    public void toLocalFast(double lat, double lon, double h, double[] out) {
        ProjCoordinate geo = tlGeo.get();
        ProjCoordinate proj = tlProj.get();
        geo.x = lon; geo.y = lat; // proj4j uses (lon,lat)
        geoToProj.transform(geo, proj);
        double E = proj.x; double N = proj.y;
        out[0] = a0 + a1 * E + a2 * N;
        out[1] = b0 + b1 * E + b2 * N;
        out[2] = c0 + c1 * h + c2 * E + c3 * N;
    }
}
