package dxf;

public class Triangle3D {
    public final Point3D a, b, c;
    public final double minX, maxX, minY, maxY;
    // precompute denom per barycentric
    private final double denom;

    public Triangle3D(Point3D a, Point3D b, Point3D c) {
        this.a = a;
        this.b = b;
        this.c = c;
        minX = Math.min(a.x, Math.min(b.x, c.x));
        maxX = Math.max(a.x, Math.max(b.x, c.x));
        minY = Math.min(a.y, Math.min(b.y, c.y));
        maxY = Math.max(a.y, Math.max(b.y, c.y));
        denom = ((b.y - c.y) * (a.x - c.x) + (c.x - b.x) * (a.y - c.y));
    }

    // controlla se il punto (x,y) è dentro la proiezione XY del triangolo
    public boolean containsXY(double x, double y) {
        // bounding-box quick reject
        if (x < minX || x > maxX || y < minY || y > maxY) return false;
        if (Math.abs(denom) < 1e-12) return false; // degenerato in XY
        double alpha = ((b.y - c.y) * (x - c.x) + (c.x - b.x) * (y - c.y)) / denom;
        double beta = ((c.y - a.y) * (x - c.x) + (a.x - c.x) * (y - c.y)) / denom;
        double gamma = 1.0 - alpha - beta;
        double eps = -1e-9; // tolleranza per bordi
        return alpha >= eps && beta >= eps && gamma >= eps;
    }

    // interpolate Z via barycentric (assume punto dentro)
    public double interpolateZ(double x, double y) {
        if (Math.abs(denom) < 1e-12) {
            // triangolo degenere in XY: fallback a media z
            return (a.z + b.z + c.z) / 3.0;
        }
        double alpha = ((b.y - c.y) * (x - c.x) + (c.x - b.x) * (y - c.y)) / denom;
        double beta = ((c.y - a.y) * (x - c.x) + (a.x - c.x) * (y - c.y)) / denom;
        double gamma = 1.0 - alpha - beta;
        return alpha * a.z + beta * b.z + gamma * c.z;
    }
}
