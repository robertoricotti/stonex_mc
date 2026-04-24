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

        double minorLen = Math.abs(majorLen * axisRatio);
        if (minorLen <= EPS) return out;

        double ux = ax / majorLen;
        double uy = ay / majorLen;

        // Assumiamo piano XY, coerente col tuo renderer 2D attuale.
        double vx = -uy;
        double vy = ux;

        double start = startParam;
        double end = endParam;
        while (end < start) end += Math.PI * 2.0;

        double sweep = end - start;
        if (sweep <= EPS) sweep = Math.PI * 2.0;

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

    public static List<Point3D> sampleSplinePoints(Spline spline) {
        List<Point3D> out = new ArrayList<>();
        if (spline == null) return out;

        List<Point3D> fitPoints = spline.getFitPoints();
        if (fitPoints != null && fitPoints.size() >= 2) {
            return sampleCatmullRomSpline(fitPoints, spline.isClosed());
        }

        List<Point3D> controlPoints = spline.getControlPoints();
        if (controlPoints != null && controlPoints.size() >= 2) {
            int degree = spline.getDegree() > 0 ? spline.getDegree() : 3;
            return sampleBSpline(controlPoints, spline.getKnots(), degree, spline.isClosed());
        }

        return out;
    }

    private static List<Point3D> sampleCatmullRomSpline(List<Point3D> pts, boolean closed) {
        List<Point3D> out = new ArrayList<>();
        if (pts == null || pts.size() < 2) return out;

        List<Point3D> working = new ArrayList<>();
        if (closed) {
            working.add(pts.get(pts.size() - 1));
            working.addAll(clonePoints(pts));
            working.add(pts.get(0));
            working.add(pts.get(1 % pts.size()));
        } else {
            working.add(pts.get(0));
            working.addAll(clonePoints(pts));
            working.add(pts.get(pts.size() - 1));
        }

        for (int i = 0; i < working.size() - 3; i++) {
            Point3D p0 = working.get(i);
            Point3D p1 = working.get(i + 1);
            Point3D p2 = working.get(i + 2);
            Point3D p3 = working.get(i + 3);

            if (i == 0) out.add(p1.clone());

            double chord = distance(p1, p2);
            int segs = clamp((int) Math.ceil(chord / DEFAULT_TARGET_SEGMENT_LENGTH), 12, 96);
            for (int k = 1; k <= segs; k++) {
                double t = (double) k / segs;
                out.add(catmullRomPoint(p0, p1, p2, p3, t));
            }
        }

        return out;
    }

    private static Point3D catmullRomPoint(Point3D p0, Point3D p1, Point3D p2, Point3D p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double x = 0.5 * ((2 * p1.getX()) + (-p0.getX() + p2.getX()) * t + (2*p0.getX() - 5*p1.getX() + 4*p2.getX() - p3.getX()) * t2 + (-p0.getX() + 3*p1.getX() - 3*p2.getX() + p3.getX()) * t3);
        double y = 0.5 * ((2 * p1.getY()) + (-p0.getY() + p2.getY()) * t + (2*p0.getY() - 5*p1.getY() + 4*p2.getY() - p3.getY()) * t2 + (-p0.getY() + 3*p1.getY() - 3*p2.getY() + p3.getY()) * t3);
        double z = 0.5 * ((2 * p1.getZ()) + (-p0.getZ() + p2.getZ()) * t + (2*p0.getZ() - 5*p1.getZ() + 4*p2.getZ() - p3.getZ()) * t2 + (-p0.getZ() + 3*p1.getZ() - 3*p2.getZ() + p3.getZ()) * t3);
        return new Point3D(x, y, z);
    }

    private static List<Point3D> sampleBSpline(List<Point3D> controlPoints, List<Double> knots, int degree, boolean closed) {
        List<Point3D> out = new ArrayList<>();
        if (controlPoints == null || controlPoints.size() < 2) return out;
        degree = Math.max(1, Math.min(degree, controlPoints.size() - 1));

        List<Point3D> cps = new ArrayList<>(clonePoints(controlPoints));
        if (closed && cps.size() > degree) {
            for (int i = 0; i < degree; i++) cps.add(controlPoints.get(i).clone());
        }

        List<Double> U = knots;
        int n = cps.size() - 1;
        if (U == null || U.size() != n + degree + 2) {
            U = buildClampedUniformKnots(n, degree);
        }

        double uStart = U.get(degree);
        double uEnd = U.get(n + 1);
        int samples = clamp(cps.size() * 24, 48, 600);

        for (int i = 0; i <= samples; i++) {
            double u = uStart + (uEnd - uStart) * i / samples;
            if (i == samples) u = uEnd - 1e-10;
            out.add(deBoorPoint(cps, U, degree, u));
        }

        if (closed && !out.isEmpty()) closePolylineIfNeeded(fromPoints(out, null, 0, false));
        return out;
    }

    private static Point3D deBoorPoint(List<Point3D> cps, List<Double> knots, int degree, double u) {
        int n = cps.size() - 1;
        int k = findSpan(n, degree, u, knots);

        Point3D[] d = new Point3D[degree + 1];
        for (int j = 0; j <= degree; j++) {
            d[j] = cps.get(k - degree + j).clone();
        }

        for (int r = 1; r <= degree; r++) {
            for (int j = degree; j >= r; j--) {
                int idx = k - degree + j;
                double denom = knots.get(idx + degree - r + 1) - knots.get(idx);
                double alpha = Math.abs(denom) < EPS ? 0.0 : (u - knots.get(idx)) / denom;
                d[j] = lerp(d[j - 1], d[j], alpha);
            }
        }
        return d[degree];
    }

    private static int findSpan(int n, int degree, double u, List<Double> knots) {
        if (u >= knots.get(n + 1)) return n;
        if (u <= knots.get(degree)) return degree;
        int low = degree;
        int high = n + 1;
        int mid = (low + high) / 2;
        while (u < knots.get(mid) || u >= knots.get(mid + 1)) {
            if (u < knots.get(mid)) high = mid;
            else low = mid;
            mid = (low + high) / 2;
        }
        return mid;
    }

    private static List<Double> buildClampedUniformKnots(int n, int degree) {
        List<Double> knots = new ArrayList<>();
        int m = n + degree + 1;
        for (int i = 0; i <= m; i++) {
            if (i <= degree) knots.add(0.0);
            else if (i >= m - degree) knots.add(1.0);
            else knots.add((double)(i - degree) / (m - 2 * degree));
        }
        return knots;
    }

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

    private static List<Point3D> clonePoints(List<Point3D> src) {
        List<Point3D> out = new ArrayList<>();
        for (Point3D p : src) out.add(p.clone());
        return out;
    }

    private static boolean samePoint(Point3D a, Point3D b) {
        if (a == null || b == null) return false;
        return Math.abs(a.getX() - b.getX()) < EPS
                && Math.abs(a.getY() - b.getY()) < EPS
                && Math.abs(a.getZ() - b.getZ()) < EPS;
    }

    private static Point3D lerp(Point3D a, Point3D b, double t) {
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
