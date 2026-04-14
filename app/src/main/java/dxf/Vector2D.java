package dxf;

public class Vector2D {
    public double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Point3D from, Point3D to) {
        this.x = to.x - from.x;
        this.y = to.y - from.y;
    }

    public Vector2D normalize() {
        double len = Math.sqrt(x * x + y * y);
        return new Vector2D(x / len, y / len);
    }

    public Vector2D add(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D getNormal() {
        return new Vector2D(-y, x).normalize();
    }
}

