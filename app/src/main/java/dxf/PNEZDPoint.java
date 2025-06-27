package dxf;

public class PNEZDPoint {
    private int pointNumber;      // P
    private double northing;      // N
    private double easting;       // E
    private double elevation;     // Z
    private String description;   // D

    // Costruttore
    public PNEZDPoint(int pointNumber, double northing, double easting, double elevation, String description) {
        this.pointNumber = pointNumber;
        this.northing = northing;
        this.easting = easting;
        this.elevation = elevation;
        this.description = description;
    }

    // Getter e Setter
    public int getPointNumber() {
        return pointNumber;
    }

    public void setPointNumber(int pointNumber) {
        this.pointNumber = pointNumber;
    }

    public double getNorthing() {
        return northing;
    }

    public void setNorthing(double northing) {
        this.northing = northing;
    }

    public double getEasting() {
        return easting;
    }

    public void setEasting(double easting) {
        this.easting = easting;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // toString per visualizzazione
    @Override
    public String toString() {
        return pointNumber + ", " + northing + ", " + easting + ", " + elevation + ", " + description;
    }
}
