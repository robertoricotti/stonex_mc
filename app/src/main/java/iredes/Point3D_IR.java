package iredes;

public final class Point3D_IR {
    private final double x;
    private final double y;
    private final double z;

    public Point3D_IR(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }



    @Override
    public String toString() {
        return "Point3D_IR{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
