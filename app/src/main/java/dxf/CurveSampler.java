package dxf;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility centralizzata per convertire curve DXF in punti / Polyline.
 *
 * Obiettivi:
 * - evitare logiche duplicate tra renderer, snap e highlight
 * - avere una discretizzazione coerente per ARC, CIRCLE, ELLIPSE, bulge e SPLINE
 * - restituire sempre geometrie facili da disegnare e rendere snappabili
 */
public final class CurveSampler {

    private static final int MIN_SEGMENTS = 12;
    private static final int MAX_SEGMENTS = 720;
    private static final double DEFAULT_TARGET_SEGMENT_LENGTH = 0.20; // unità mondo
    private static final double EPS = 1e-9;

    private CurveSampler() {
    }

    // =========================================================
    // PUBLIC API - restituisce direttamente Polyline
    // =========================================================

    public static Polyline sampleArc(Arc arc) {
        if (arc == null) return new Polyline();

        List<Point3D> pts = sampleArcPoints(
                arc.getCenter(),
                arc.getRadius(),
                arc.getStartAngle(),
                arc.getEndAngle()
        );

        Polyline p = new Polyline();
        p.setLayer(arc.getLayer());
        p.setLineColor(arc.getColor());
        p.setVertices(pts);
        p.markGlDirty();
        return p;
    }

    public static Polyline sampleCircle(Circle circle) {
        if (circle == null) return new Polyline();

        List<Point3D> pts = sampleCirclePoints(
                circle.getCenter(),
                circle.getRadius()
        );

        Polyline p = new Polyline();
        p.setLayer(circle.getLayer());
        p.setLineColor(circle.getColor());
        p.setVertices(pts);
        p.markGlDirty();
        return p;
    }

    public static Polyline sampleEllipse(Ellipse ellipse) {
        if (ellipse == null) return new Polyline();

        List<Point3D> pts = sampleEllipsePoints(
                ellipse.getCenter(),
                ellipse.getMajorAxisEnd(),
                ellipse.getAxisRatio(),
                ellipse.getStartParam(),
                ellipse.getEndParam()
        );

        Polyline p = new Polyline();
        p.setLayer(ellipse.getLayer());
        p.setLineColor(ellipse.getColor());
        p.setVertices(pts);
        p.markGlDirty();
        return p;
    }

    public static Polyline sampleBulge(Polyline_2D src) {
        Polyline out = new Polyline();
        if (src == null) return out;

        out.setLayer(src.getLayer());
        out.setLineColor(src.getLineColor());

        List<Point3D> verts = src.getVertices();
        if (verts == null || verts.size() < 2) return out;

        for (int i = 0; i < verts.size() - 1; i++) {
            Point3D a = verts.get(i);
            Point3D b = verts.get(i + 1);

            if (i == 0) {
                out.getVertices().add(a.clone());
            }

            double bulge = a.getBulge();
            if (Math.abs(bulge) < EPS) {
                out.getVertices().add(b.clone());
            } else {
                List<Point3D> sampled = sampleBulgeSegmentPoints(a, b, bulge);
                for (int k = 1; k < sampled.size(); k++) {
                    out.getVertices().add(sampled.get(k));
                }
            }
        }

        if (src.isClosed() && !out.getVertices().isEmpty()) {
            closePolylineIfNeeded(out);
        }

        out.markGlDirty();
        return out;
    }

    public static Polyline sampleSpline(Spline spline) {
        Polyline out = new Polyline();
        if (spline == null) return out;

        out.setLayer(spline.getLayer());
        out.setLineColor(spline.getColor());

        List<Point3D> pts = sampleSplinePoints(spline);
        out.setVertices(pts);

        if (spline.isClosed() && !out.getVertices().isEmpty()) {
            closePolylineIfNeeded(out);
        }

        out.markGlDirty();
        return out;
    }

    // =========================================================
    // ARC
    // =========================================================

    public static List<Point3D> sampleArcPoints(Point3D center,
                                                double radius,
                                                double startAngleDeg,
                                                double endAngleDeg) {
        List<Point3D> out = new ArrayList<>();
        if (center == null || radius <= 0) return out;

        double start = Math.toRadians(startAngleDeg);
        double end = Math.toRadians(endAngleDeg);

        while (end < start) {
            end += Math.PI * 2.0;
        }

        double sweep = end - start;
        int segments = computeSegmentsByRadiusAndSweep(radius, sweep);

        for (int i = 0; i <= segments; i++) {
            double t = start + sweep * i / segments;
            double x = center.getX() + radius * Math.cos(t);
            double y = center.getY() + radius * Math.sin(t);
            double z = center.getZ();
            out.add(new Point3D(x, y, z));
        }

        return out;
    }

    // =========================================================
    // CIRCLE
    // =========================================================

    public static List<Point3D> sampleCirclePoints(Point3D center, double radius) {
        List<Point3D> out = new ArrayList<>();
        if (center == null || radius <= 0) return out;

        double sweep = Math.PI * 2.0;
        int segments = computeSegmentsByRadiusAndSweep(radius, sweep);

        for (int i = 0; i <= segments; i++) {
            double t = sweep * i / segments;
            double x = center.getX() + radius * Math.cos(t);
            double y = center.getY() + radius * Math.sin(t);
            double z = center.getZ();
            out.add(new Point3D(x, y, z));
        }

        return out;
    }

    // =========================================================
    // ELLIPSE
    // majorAxisEnd è il vettore dal centro all'estremo asse maggiore
    // axisRatio = minor/major
    // startParam/endParam in radianti DXF
    // =========================================================

    public static List<Point3D> sampleEllipsePoints(Point3D center,
                                                    Point3D majorAxisEnd,
                                                    double axisRatio,
                                                    double startParam,
                                                    double endParam) {
        List<Point3D> out = new ArrayList<>();
        if (center == null || majorAxisEnd == null) return out;

        double ax = majorAxisEnd.getX();
        double ay = majorAxisEnd.getY();
        double az = majorAxisEnd.getZ();

        double majorLen = Math.sqrt(ax * ax + ay * ay + az * az);
        if (majorLen <= EPS) return out;

        double minorLen = majorLen * axisRatio;

        // vettore unitario asse maggiore
        double ux = ax / majorLen;
        double uy = ay / majorLen;

        // asse minore ortogonale in XY
        double vx = -uy;
        double vy = ux;

        double start = startParam;
        double end = endParam;

        while (end < start) {
            end += Math.PI * 2.0;
        }

        double sweep = end - start;
        double effectiveRadius = Math.max(majorLen, minorLen);
        int segments = computeSegmentsByRadiusAndSweep(effectiveRadius, sweep);

        for (int i = 0; i <= segments; i++) {
            double t = start + sweep * i / segments;

            double localX = majorLen * Math.cos(t);
            double localY = minorLen * Math.sin(t);

            double x = center.getX() + localX * ux + localY * vx;
            double y = center.getY() + localX * uy + localY * vy;
            double z = center.getZ();

            out.add(new Point3D(x, y, z));
        }

        return out;
    }

    // =========================================================
    // BULGE
    // Replica la logica geometrica corretta per archi da LWPOLYLINE / POLYLINE
    // =========================================================

    public static List<Point3D> sampleBulgeSegmentPoints(Point3D p1, Point3D p2, double bulge) {
        List<Point3D> out = new ArrayList<>();

        if (p1 == null || p2 == null) return out;

        if (Math.abs(bulge) < EPS) {
            out.add(p1.clone());
            out.add(p2.clone());
            return out;
        }

        if (Math.abs(bulge) > 1.0) {
            return sampleBulgeSegmentMajorArc(p1, p2, bulge);
        } else {
            return sampleBulgeSegmentStandardArc(p1, p2, bulge);
        }
    }

    private static List<Point3D> sampleBulgeSegmentStandardArc(Point3D p1, Point3D p2, double bulge) {
        List<Point3D> out = new ArrayList<>();

        double x1 = p1.getX();
        double y1 = p1.getY();
        double z1 = p1.getZ();

        double x2 = p2.getX();
        double y2 = p2.getY();

        double distance = Math.hypot(x2 - x1, y2 - y1);
        if (distance <= EPS) {
            out.add(p1.clone());
            return out;
        }

        double theta = 4.0 * Math.atan(Math.abs(bulge));
        double radius = (distance / 2.0) / Math.abs(Math.sin(theta / 2.0));

        double midX = (x1 + x2) / 2.0;
        double midY = (y1 + y2) / 2.0;

        double height = Math.sqrt(Math.max(0.0, radius * radius - (distance / 2.0) * (distance / 2.0)));

        double dx = x2 - x1;
        double dy = y2 - y1;
        double norm = Math.hypot(dx, dy);
        if (norm <= EPS) {
            out.add(p1.clone());
            return out;
        }

        double perpX = -dy / norm;
        double perpY = dx / norm;

        double centerX = midX + perpX * height * (bulge > 0 ? 1 : -1);
        double centerY = midY + perpY * height * (bulge > 0 ? 1 : -1);

        double startAngle = Math.atan2(y1 - centerY, x1 - centerX);
        double endAngle = Math.atan2(y2 - centerY, x2 - centerX);

        double sweepAngle = endAngle - startAngle;
        if (bulge > 0) {
            if (sweepAngle < 0) sweepAngle += 2.0 * Math.PI;
        } else {
            if (sweepAngle > 0) sweepAngle -= 2.0 * Math.PI;
        }

        if (Math.abs(bulge) > 1.0 && Math.abs(Math.toDegrees(sweepAngle)) < 180.0) {
            sweepAngle = (bulge > 0)
                    ? (2.0 * Math.PI - Math.abs(sweepAngle))
                    : -(2.0 * Math.PI - Math.abs(sweepAngle));
        }

        int segments = computeSegmentsByRadiusAndSweep(radius, sweepAngle);

        for (int i = 0; i <= segments; i++) {
            double angle = startAngle + sweepAngle * i / segments;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            out.add(new Point3D(x, y, z1));
        }

        return out;
    }

    private static List<Point3D> sampleBulgeSegmentMajorArc(Point3D p1, Point3D p2, double bulge) {
        List<Point3D> out = new ArrayList<>();

        double x1 = p1.getX();
        double y1 = p1.getY();
        double z1 = p1.getZ();

        double x2 = p2.getX();
        double y2 = p2.getY();

        double chordLength = Math.hypot(x2 - x1, y2 - y1);
        if (chordLength <= EPS) {
            out.add(p1.clone());
            return out;
        }

        double theta = 4.0 * Math.atan(Math.abs(bulge));
        double radius = Math.abs((chordLength / 2.0) / Math.sin(theta / 2.0));

        double midX = (x1 + x2) / 2.0;
        double midY = (y1 + y2) / 2.0;

        double sagitta = Math.sqrt(Math.max(0.0, radius * radius - (chordLength / 2.0) * (chordLength / 2.0)));

        double dx = x2 - x1;
        double dy = y2 - y1;
        double norm = Math.hypot(dx, dy);
        if (norm <= EPS) {
            out.add(p1.clone());
            return out;
        }

        double perpX = -dy / norm;
        double perpY = dx / norm;

        double centerX = midX + perpX * sagitta * (bulge > 0 ? -1 : 1);
        double centerY = midY + perpY * sagitta * (bulge > 0 ? -1 : 1);

        double startAngle = Math.atan2(y1 - centerY, x1 - centerX);
        double endAngle = Math.atan2(y2 - centerY, x2 - centerX);

        double sweepAngle = endAngle - startAngle;
        if (bulge > 0) {
            if (sweepAngle < 0) sweepAngle += 2.0 * Math.PI;
        } else {
            if (sweepAngle > 0) sweepAngle -= 2.0 * Math.PI;
        }

        int segments = computeSegmentsByRadiusAndSweep(radius, sweepAngle);

        for (int i = 0; i <= segments; i++) {
            double angle = startAngle + sweepAngle * i / segments;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            out.add(new Point3D(x, y, z1));
        }

        return out;
    }

    // =========================================================
    // SPLINE
    // Versione pragmatica:
    // - se ci sono fit points, li usa come base
    // - altrimenti usa control points
    // - campiona linearmente tra i punti base
    //
    // Non è una NURBS completa, ma è un primo passo robusto e semplice.
    // =========================================================

    public static List<Point3D> sampleSplinePoints(Spline spline) {
        List<Point3D> out = new ArrayList<>();
        if (spline == null) return out;

        List<Point3D> base = null;

        if (spline.getFitPoints() != null && spline.getFitPoints().size() >= 2) {
            base = spline.getFitPoints();
        } else if (spline.getControlPoints() != null && spline.getControlPoints().size() >= 2) {
            base = spline.getControlPoints();
        }

        if (base == null || base.size() < 2) return out;

        // Primo step semplice: densifica i segmenti base
        for (int i = 0; i < base.size() - 1; i++) {
            Point3D a = base.get(i);
            Point3D b = base.get(i + 1);

            if (i == 0) out.add(a.clone());

            double dist = distance(a, b);
            int segs = clamp((int) Math.ceil(dist / DEFAULT_TARGET_SEGMENT_LENGTH), 4, 64);

            for (int k = 1; k <= segs; k++) {
                double t = (double) k / segs;
                out.add(interpolate(a, b, t));
            }
        }

        return out;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    public static Polyline fromPoints(List<Point3D> points, Layer layer, int color, boolean closed) {
        Polyline p = new Polyline();
        p.setLayer(layer);
        p.setLineColor(color);

        if (points != null) {
            for (Point3D pt : points) {
                p.getVertices().add(pt.clone());
            }
        }

        if (closed) {
            closePolylineIfNeeded(p);
        }

        p.markGlDirty();
        return p;
    }

    private static void closePolylineIfNeeded(Polyline polyline) {
        List<Point3D> verts = polyline.getVertices();
        if (verts == null || verts.size() < 2) return;

        Point3D first = verts.get(0);
        Point3D last = verts.get(verts.size() - 1);

        if (!samePoint(first, last)) {
            verts.add(first.clone());
        }
    }

    private static boolean samePoint(Point3D a, Point3D b) {
        if (a == null || b == null) return false;
        return Math.abs(a.getX() - b.getX()) < EPS
                && Math.abs(a.getY() - b.getY()) < EPS
                && Math.abs(a.getZ() - b.getZ()) < EPS;
    }

    private static Point3D interpolate(Point3D a, Point3D b, double t) {
        return new Point3D(
                a.getX() + (b.getX() - a.getX()) * t,
                a.getY() + (b.getY() - a.getY()) * t,
                a.getZ() + (b.getZ() - a.getZ()) * t
        );
    }

    private static double distance(Point3D a, Point3D b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double dz = b.getZ() - a.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static int computeSegmentsByRadiusAndSweep(double radius, double sweep) {
        double absSweep = Math.abs(sweep);
        int byLength = (int) Math.ceil((radius * absSweep) / DEFAULT_TARGET_SEGMENT_LENGTH);
        int byAngle = (int) Math.ceil(Math.toDegrees(absSweep) / 8.0);

        int segments = Math.max(byLength, byAngle);
        return clamp(segments, MIN_SEGMENTS, MAX_SEGMENTS);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}