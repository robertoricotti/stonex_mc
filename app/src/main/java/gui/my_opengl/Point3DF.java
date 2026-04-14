package gui.my_opengl;

public class Point3DF {
    float x, y, z;
    String name;

    public Point3DF(float[] floats) {
        this.x = floats[0];
        this.y = floats[1];
        this.z = floats[2];
    }

    public Point3DF(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3DF(float x, float y, float z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }


    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public String getName() {
        return name;
    }

    public Point3DF add(Point3DF other) {
        return new Point3DF(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Point3DF subtract(Point3DF other) {
        return new Point3DF(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Point3DF scale(float scalar) {
        return new Point3DF(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Point3DF normalize() {
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        return len == 0 ? new Point3DF(0, 0, 0) : new Point3DF(x / len, y / len, z / len);
    }

    public Point3DF cross(Point3DF other) {
        return new Point3DF(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    // Metodo per calcolare il prodotto scalare
    public float dot(Point3DF other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D toVector() {
        return new Vector3D(this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return "Point3DF{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", name=" + name +
                '}';
    }

    public static Point3DF pTransform(double[] p3d, double[] anchor, float scale) {
        Point3DF p = new Point3DF((float) (p3d[0] - anchor[0]) * scale,
                (float) (p3d[1] - anchor[1]) * scale,
                (float) (p3d[2] - anchor[2]) * scale);
        return p;

    }
}
