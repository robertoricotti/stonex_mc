package dxf;


import java.io.Serializable;
import java.util.Objects;

public class Point3D implements Serializable {
    private static final long serialVersionUID = 1L;
    double x, y, z,bulge;
    String id,description,filename;
    int colore;
    Layer layer;

    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Point3D(String id,double x, double y, double z) {
        this.id=id;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Point3D(String id,double x, double y, double z,String description) {
        this.id=id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.description=description;
    }
    // Costruttore con bulge
    public Point3D(double x, double y, double z, double bulge) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.bulge = bulge;
    }
    public Point3D(double x, double y, double z, double bulge,int colore,Layer layer) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.bulge = bulge;
        this.colore=colore;
        this.layer=layer;
    }
    public Point3D(double x, double y, double z, int colore,Layer layer) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.colore=colore;
        this.layer=layer;
    }
    public Point3D(String filename,double x, double y, double z, int colore,Layer layer) {
        this.filename=filename;
        this.x = x;
        this.y = y;
        this.z = z;
        this.colore=colore;
        this.layer=layer;
    }

    public Point3D(String filename,String id,double x, double y, double z, int colore,Layer layer,String description) {
        this.filename=filename;
        this.id=id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.colore=colore;
        this.layer=layer;
        this.description=description;
    }
    public Point3D(String id,double x, double y, double z, double bulge,int colore,Layer layer,String description) {
        this.id=id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.bulge = bulge;
        this.colore=colore;
        this.layer=layer;
        this.description=description;
    }
    public Point3D(String filename,String id,double x, double y, double z, double bulge,int colore,Layer layer,String description) {
        this.id=id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.bulge = bulge;
        this.colore=colore;
        this.layer=layer;
        this.description=description;
        this.filename=filename;
    }
    @Override
    public String toString() {
        return "Point3D(" + x + ", " + y + ", " + z + ", bulge=" + bulge + ")";
    }
    public Point3D swapXY() {
        return new Point3D(this.y, this.x, this.z, this.bulge); // Mantiene il bulge
    }

    // Metodo per calcolare la media di due punti 3D
    public static Point3D calculateCentroid(Point3D p1, Point3D p2, Point3D p3, Point3D p4) {
        double centerX = (p1.x + p2.x + p3.x + p4.x) / 4.0;
        double centerY = (p1.y + p2.y + p3.y + p4.y) / 4.0;
        double centerZ = (p1.z + p2.z + p3.z + p4.z) / 4.0;
        return new Point3D(centerX, centerY, centerZ);
    }

    public Point3D subtract(Point3D other) {
        return new Point3D(this.x - other.x, this.y - other.y, this.z - other.z, this.bulge);
    }

    public Point3D add(Point3D other) {
        return new Point3D(this.x + other.x, this.y + other.y, this.z + other.z, this.bulge);
    }

    public Point3D scale(double factor) {
        return new Point3D(this.x * factor, this.y * factor, this.z * factor, this.bulge);
    }

    public double dot(Point3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public static Point3D centroidAreaWeighted(Face3D f) {
        // split: (p1,p2,p3) e (p1,p3,p4)
        Point3D c1 = triangleCentroid(f.p1, f.p2, f.p3);
        double a1 = triangleArea(f.p1, f.p2, f.p3);
        Point3D c2 = triangleCentroid(f.p1, f.p3, f.p4);
        double a2 = triangleArea(f.p1, f.p3, f.p4);
        double sumA = a1 + a2;
        if (sumA == 0) return calculateCentroid(f.p1,f.p2,f.p3,f.p4); // fallback
        double cx = (c1.x * a1 + c2.x * a2) / sumA;
        double cy = (c1.y * a1 + c2.y * a2) / sumA;
        double cz = (c1.z * a1 + c2.z * a2) / sumA;
        return new Point3D(cx, cy, cz);
    }
    private static Point3D triangleCentroid(Point3D a, Point3D b, Point3D c) {
        return new Point3D((a.x+b.x+c.x)/3.0, (a.y+b.y+c.y)/3.0, (a.z+b.z+c.z)/3.0);
    }
    private static double triangleArea(Point3D a, Point3D b, Point3D c) {
        // area 3D = |(b-a)x(c-a)| / 2
        double ux = b.x - a.x, uy = b.y - a.y, uz = b.z - a.z;
        double vx = c.x - a.x, vy = c.y - a.y, vz = c.z - a.z;
        double cx = uy * vz - uz * vy;
        double cy = uz * vx - ux * vz;
        double cz = ux * vy - uy * vx;
        return Math.sqrt(cx*cx + cy*cy + cz*cz) * 0.5;
    }


    Point3D cross(Point3D other) {
        return new Point3D(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x,
                this.bulge // Mantiene il valore di bulge
        );
    }
    // Getter e Setter per il bulge
    public double getBulge() {
        return bulge;
    }

    public void setBulge(double bulge) {
        this.bulge = bulge;
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

    public double getLength() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public void setColore(int colore) {
        this.colore = colore;
    }

    public int getColore() {
        return colore;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }


    public void setName(String description){
        this.description=description;
    }
    public String getName(){return description;}

    // Metodo per interpolare il punto a 1/3 tra this e target
    public Point3D interpolate(Point3D target, double fraction) {
        double newX = this.x + fraction * (target.x - this.x);
        double newY = this.y + fraction * (target.y - this.y);
        double newZ = this.z + fraction * (target.z - this.z);
        return new Point3D(newX, newY, newZ);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point3D)) return false;
        Point3D other = (Point3D) o;
        return Double.compare(x, other.x) == 0 &&
                Double.compare(y, other.y) == 0 &&
                Double.compare(z, other.z) == 0 &&
                Objects.equals(layer, other.layer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, layer);
    }

}