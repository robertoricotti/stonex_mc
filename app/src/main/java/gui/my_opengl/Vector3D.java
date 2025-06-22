package gui.my_opengl;

import dxf.Point3D;

public class Vector3D {
    public float x, y, z;

    public Vector3D() {
        this(0, 0, 0);
    }

    public Vector3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
    }

    public Vector3D(Point3DF p) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
    }

    public Vector3D add(Vector3D v) {
        return new Vector3D(this.x + v.x, this.y + v.y, this.z + v.z);
    }
    public static Vector3D subtract(Point3DF from, Point3DF to) {
        return new Vector3D(to.x - from.x, to.y - from.y, to.z - from.z);
    }
    public Vector3D subtract(Vector3D v) {
        return new Vector3D(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public Vector3D multiply(float scalar) {
        return new Vector3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public float dot(Vector3D v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vector3D cross(Vector3D v) {
        return new Vector3D(
                this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x
        );
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D normalize() {
        float len = length();
        if (len == 0) return new Vector3D(0, 0, 0);
        return new Vector3D(x / len, y / len, z / len);
    }

    public Point3DF toPoint3DF() {
        return new Point3DF(x, y, z);
    }
    public static Vector3D subtract(Point3D from, Point3D to) {
        return new Vector3D(to.getX() - from.getX(),
                to.getY() - from.getY(),
                to.getZ() - from.getZ());
    }

    @Override
    public String toString() {
        return "Vector3D(" + x + ", " + y + ", " + z + ")";
    }
}
