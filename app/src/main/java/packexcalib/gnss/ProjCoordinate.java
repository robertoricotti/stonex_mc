package packexcalib.gnss;

public class ProjCoordinate {
    public double x;
    public double y;
    public double z;

    public ProjCoordinate() {}

    public ProjCoordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setValue(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setValue(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void clearZ() {
        this.z = Double.NaN;
    }

    @Override
    public String toString() {
        return "ProjCoordinate[" + x + " " + y + " " + z + "]";
    }
}