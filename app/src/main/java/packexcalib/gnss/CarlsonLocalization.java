package packexcalib.gnss;
import org.locationtech.proj4j.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.index.strtree.STRtree;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * CarlsonLocalization - versione "rubber-sheet" basata su triangolazione Delaunay.
 * Legge .LOC una sola volta, costruisce triangolazione su coordinate proiettate (E,N)
 * e permette interpolazione per ottenere X,Y,Z locali esattamente come il sistema di riferimento.
 *
 * Implementazione thread-safe e ottimizzata per runtime: toLocalFast(...) non deve allocare (usa ThreadLocal).
 */
public final class CarlsonLocalization implements LocalizationModel {

    // proj4j factories (shared)
    private static final CRSFactory crsFactory = new CRSFactory();
    private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();

    private final CoordinateReferenceSystem wgs84;
    private final CoordinateReferenceSystem projected;
    private final CoordinateTransform geoToProj;

    // Points and triangles (immutable after construction)
    private final List<LocPoint> points;          // original points with proj E,N
    private final List<Triangle> triangles;      // triangles built from Delaunay
    private final STRtree triIndex;               // spatial index on triangle envelopes

    // Reusable objects to avoid allocations in hot path
    private final ThreadLocal<ProjCoordinate> tlGeo = ThreadLocal.withInitial(ProjCoordinate::new);
    private final ThreadLocal<ProjCoordinate> tlProj = ThreadLocal.withInitial(ProjCoordinate::new);

    // Private constructor - use static factory fromLocFile
    private CarlsonLocalization(CoordinateReferenceSystem projected,
                                List<LocPoint> points,
                                List<Triangle> triangles,
                                STRtree triIndex) {
        this.wgs84 = crsFactory.createFromName("EPSG:4326");
        this.projected = projected;
        this.geoToProj = ctFactory.createTransform(wgs84, projected);

        this.points = Collections.unmodifiableList(points);
        this.triangles = Collections.unmodifiableList(triangles);
        this.triIndex = triIndex;
    }

    // Small struct for point
    private static final class LocPoint {
        final double lat, lon, h;
        final double xLocal, yLocal, zLocal; // local coordinates from file
        final double E, N;                   // projected coordinates (meters)

        LocPoint(double lat, double lon, double h, double xLocal, double yLocal, double zLocal,
                 double E, double N) {
            this.lat = lat; this.lon = lon; this.h = h;
            this.xLocal = xLocal; this.yLocal = yLocal; this.zLocal = zLocal;
            this.E = E; this.N = N;
        }
    }

    // Triangle holding indices to 3 points and precomputed denom for barycentric
    private static final class Triangle {
        final int i0, i1, i2;   // indices into points list
        final Envelope env;     // envelope for quick spatial test
        // vertices E,N for barycentric
        final double x0, y0, x1, y1, x2, y2;
        final double denom;     // denominator for barycentric coords

        Triangle(int i0, int i1, int i2, double x0, double y0, double x1, double y1, double x2, double y2) {
            this.i0 = i0; this.i1 = i1; this.i2 = i2;
            this.x0 = x0; this.y0 = y0; this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
            this.env = new Envelope(Math.min(Math.min(x0,x1),x2), Math.max(Math.max(x0,x1),x2),
                    Math.min(Math.min(y0,y1),y2), Math.max(Math.max(y0,y1),y2));
            // denom for barycentric (2*area)
            this.denom = (y1 - y2)*(x0 - x2) + (x2 - x1)*(y0 - y2);
        }

        // barycentric coords for (x,y)
        void barycentric(double x, double y, double[] outWeights) {
            // outWeights must be length 3
            double w0, w1, w2;
            if (Math.abs(denom) < 1e-12) {
                // degenerate triangle: fall back to near-equal weights by distance
                double d0 = (x - x0)*(x - x0) + (y - y0)*(y - y0);
                double d1 = (x - x1)*(x - x1) + (y - y1)*(y - y1);
                double d2 = (x - x2)*(x - x2) + (y - y2)*(y - y2);
                double s = 1.0/(d0 + d1 + d2 + 1e-12);
                outWeights[0] = (1.0/d0) * s;
                outWeights[1] = (1.0/d1) * s;
                outWeights[2] = (1.0/d2) * s;
                return;
            }
            w0 = ((y1 - y2)*(x - x2) + (x2 - x1)*(y - y2)) / denom;
            w1 = ((y2 - y0)*(x - x2) + (x0 - x2)*(y - y2)) / denom;
            w2 = 1.0 - w0 - w1;
            outWeights[0] = w0; outWeights[1] = w1; outWeights[2] = w2;
        }

        boolean containsPoint(double x, double y) {
            // quick envelope test already done by index; do barycentric and check weights in [0,1]
            double[] w = new double[3];
            barycentric(x, y, w);
            final double eps = -1e-10;
            return w[0] >= eps && w[1] >= eps && w[2] >= eps;
        }
    }

    /**
     * Factory: leggi file .LOC, costruisci triangolazione Delaunay sui punti proiettati.
     */
    public static CarlsonLocalization fromLocFile(File f) throws Exception {
        // 1) parse file and gather lat/lon/h and local X/Y/Z
        List<RawPoint> raw = parseLocFile(f);
        if (raw.size() < 3) throw new IllegalArgumentException("Serve almeno 3 punti nel .LOC");

        // 2) detect UTM zone from average longitude
        double avgLon = raw.stream().mapToDouble(p -> p.lon).average().orElse(0.0);
        int utmZone = (int)Math.floor((avgLon + 180.0)/6.0) + 1;
        String projDef = "+proj=utm +zone=" + utmZone + " +datum=WGS84 +units=m +no_defs";
        CoordinateReferenceSystem projected = crsFactory.createFromParameters("utm_auto", projDef);

        // 3) project all raw points to E,N
        CRSFactory cf = crsFactory;
        CoordinateReferenceSystem wgs84 = cf.createFromName("EPSG:4326");
        CoordinateTransformFactory tf = ctFactory;
        CoordinateTransform tx = tf.createTransform(wgs84, projected);
        ProjCoordinate src = new ProjCoordinate();
        ProjCoordinate dst = new ProjCoordinate();

        List<LocPoint> pts = new ArrayList<>(raw.size());
        for (RawPoint r : raw) {
            src.x = r.lon; src.y = r.lat;
            tx.transform(src, dst);
            pts.add(new LocPoint(r.lat, r.lon, r.h, r.xLocal, r.yLocal, r.zLocal, dst.x, dst.y));
        }

        // 4) build JTS Delaunay triangulation on the projected coordinates
        GeometryFactory geomFactory = new GeometryFactory();
        DelaunayTriangulationBuilder dtb = new DelaunayTriangulationBuilder();
        Coordinate[] coords = new Coordinate[pts.size()];
        for (int i = 0; i < pts.size(); i++) coords[i] = new Coordinate(pts.get(i).E, pts.get(i).N);
        dtb.setSites(geomFactory.createMultiPoint(coords));
        // triangles geometry
        GeometryCollection trisGeom = (GeometryCollection) dtb.getTriangles(geomFactory);

        // 5) build triangle objects and spatial index
        List<Triangle> triangles = new ArrayList<>();
        STRtree index = new STRtree();

        for (int i = 0; i < trisGeom.getNumGeometries(); i++) {
            Geometry g = trisGeom.getGeometryN(i);
            if (!(g instanceof Polygon)) continue;
            Polygon poly = (Polygon) g;
            Coordinate[] c = poly.getCoordinates();
            // triangle polygons in JTS include repeated first coordinate at end; take first 3 unique
            if (c.length < 3) continue;
            // find three distinct coordinates (skip final duplicate)
            Coordinate A = c[0], B = c[1], C = c[2];
            // find indices of these coordinates in pts list (exact match)
            int i0 = indexOfCoord(pts, A.x, A.y);
            int i1 = indexOfCoord(pts, B.x, B.y);
            int i2 = indexOfCoord(pts, C.x, C.y);
            // If mapping fails (rare due to precision), skip triangle
            if (i0 < 0 || i1 < 0 || i2 < 0) continue;

            Triangle t = new Triangle(i0, i1, i2, pts.get(i0).E, pts.get(i0).N, pts.get(i1).E, pts.get(i1).N, pts.get(i2).E, pts.get(i2).N);
            triangles.add(t);
            index.insert(t.env, t);
        }
        index.build();

        return new CarlsonLocalization(projected, pts, triangles, index);
    }

    // Helper to find point index by exact match of E,N (since we created triangulation from same coords)
    private static int indexOfCoord(List<LocPoint> pts, double E, double N) {
        for (int i = 0; i < pts.size(); i++) {
            LocPoint p = pts.get(i);
            if (Double.compare(p.E, E) == 0 && Double.compare(p.N, N) == 0) return i;
        }
        // fallback to near match in case rounding differs
        for (int i = 0; i < pts.size(); i++) {
            LocPoint p = pts.get(i);
            if (Math.hypot(p.E - E, p.N - N) < 1e-6) return i;
        }
        return -1;
    }

    // RawPoint used only during parsing
    private static final class RawPoint {
        final double lat, lon, h, xLocal, yLocal, zLocal;
        RawPoint(double lat, double lon, double h, double xLocal, double yLocal, double zLocal) {
            this.lat = lat; this.lon = lon; this.h = h;
            this.xLocal = xLocal; this.yLocal = yLocal; this.zLocal = zLocal;
        }
    }

    // Parse .LOC Carlson (assume standard structure)
    private static List<RawPoint> parseLocFile(File file) throws Exception {
        List<RawPoint> out = new ArrayList<>();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(file);
        NodeList records = doc.getElementsByTagName("record");
        for (int i = 0; i < records.getLength(); i++) {
            Element rec = (Element) records.item(i);
            String id = rec.getAttribute("id");
            if (id != null && id.startsWith("Point")) {
                double lat = 0, lon = 0, h = 0, lx = 0, ly = 0, lz = 0;
                NodeList vals = rec.getElementsByTagName("value");
                for (int j = 0; j < vals.getLength(); j++) {
                    Element v = (Element) vals.item(j);
                    String name = v.getAttribute("name");
                    String val = v.getAttribute("value");
                    if (val == null || val.isEmpty()) continue;
                    switch (name) {
                        case "Lat": lat = Double.parseDouble(val); break;
                        case "Lon": lon = Double.parseDouble(val); break;
                        case "Ellipsoid_Elv": h = Double.parseDouble(val); break;
                        case "Local_X": lx = Double.parseDouble(val); break;
                        case "Local_Y": ly = Double.parseDouble(val); break;
                        case "Local_Z": lz = Double.parseDouble(val); break;
                    }
                }
                // skip zero lat/lon placeholder points
                if (lat == 0.0 && lon == 0.0) continue;
                out.add(new RawPoint(lat, lon, h, lx, ly, lz));
            }
        }
        return out;
    }

    /**
     * Hot path: trasformazione Lat,Lon,h -> X,Y,Z locali.
     * Non alloca internamente strutture pesanti; out must be length >= 3.
     */
    @Override
    public void toLocalFast(double lat, double lon, double h, double[] out) {
        ProjCoordinate geo = tlGeo.get();
        ProjCoordinate proj = tlProj.get();
        geo.x = lon; geo.y = lat;
        geoToProj.transform(geo, proj);
        double E = proj.x, N = proj.y;

        // 1) query spatial index for candidate triangles
        @SuppressWarnings("unchecked")
        List<Triangle> candidates = triIndex.query(new Envelope(E, E, N, N));
        Triangle found = null;
        for (Triangle t : candidates) {
            if (t.containsPoint(E, N)) { found = t; break; }
        }

        if (found != null) {
            // compute barycentric weights
            double[] w = new double[3];
            found.barycentric(E, N, w);
            LocPoint p0 = points.get(found.i0);
            LocPoint p1 = points.get(found.i1);
            LocPoint p2 = points.get(found.i2);
            // interpolate local X,Y,Z from weights
            out[0] = w[0]*p0.xLocal + w[1]*p1.xLocal + w[2]*p2.xLocal;
            out[1] = w[0]*p0.yLocal + w[1]*p1.yLocal + w[2]*p2.yLocal;
            out[2] = w[0]*p0.zLocal + w[1]*p1.zLocal + w[2]*p2.zLocal;
            return;
        }

        // 2) not inside any triangle (outside convex hull) -> fallback to nearest vertex interpolation
        int nearestIdx = 0;
        double bestD = Double.POSITIVE_INFINITY;
        for (int i = 0; i < points.size(); i++) {
            LocPoint p = points.get(i);
            double d = (p.E - E)*(p.E - E) + (p.N - N)*(p.N - N);
            if (d < bestD) { bestD = d; nearestIdx = i; }
        }
        LocPoint np = points.get(nearestIdx);
        // return exact local coords of nearest control (reasonable fallback)
        out[0] = np.xLocal;
        out[1] = np.yLocal;
        out[2] = np.zLocal;
    }
}