package packexcalib.mymatrix;

public class Vector3d {
    private double x;
    private double y;
    private double z;

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getMagnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3d getNormalized() {
        double mag = getMagnitude();
        return new Vector3d(x / mag, y / mag, z / mag);
    }

    public Vector3d crossProduct(Vector3d other) {
        double x = this.y * other.getZ() - this.z * other.getY();
        double y = this.z * other.getX() - this.x * other.getZ();
        double z = this.x * other.getY() - this.y * other.getX();
        return new Vector3d(x, y, z);
    }

    public double dotProduct(Vector3d other) {
        return this.x * other.getX() + this.y * other.getY() + this.z * other.getZ();
    }

    public Vector3d subtract(Vector3d other) {
        return new Vector3d(this.x - other.getX(), this.y - other.getY(), this.z - other.getZ());
    }

    public Vector3d add(Vector3d other) {
        return new Vector3d(this.x + other.getX(), this.y + other.getY(), this.z + other.getZ());
    }
    public void cross(Vector3d v1, Vector3d v2) {
        double crossX = v1.y * v2.z - v2.y * v1.z;
        double crossY = v1.z * v2.x - v2.z * v1.x;
        double crossZ = v1.x * v2.y - v2.x * v1.y;
        this.x = crossX;
        this.y = crossY;
        this.z = crossZ;
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
