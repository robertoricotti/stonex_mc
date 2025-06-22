package dxf;


import java.io.Serializable;

public class Point3D implements Serializable {
    private static final long serialVersionUID = 1L;
    double x, y, z,bulge;
    String id,description;
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
}