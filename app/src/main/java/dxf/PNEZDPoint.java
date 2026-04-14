package dxf;

public class PNEZDPoint {
    private String filename;   // Nome
    private int pointNumber;      // P
    private double northing;      // N
    private double easting;       // E
    private double elevation;     // Z
    private String description;   // D
    private Integer color;        // Colore opzionale (può essere null)

    // Costruttore senza colore
    public PNEZDPoint(int pointNumber, double northing, double easting, double elevation, String description) {
        this.pointNumber = pointNumber;
        this.northing = northing;
        this.easting = easting;
        this.elevation = elevation;
        this.description = description;
        this.color = null; // Colore non impostato
    }

    public PNEZDPoint(String filename, int pointNumber, double northing, double easting, double elevation, String description) {
        this.filename = filename;
        this.pointNumber = pointNumber;
        this.northing = northing;
        this.easting = easting;
        this.elevation = elevation;
        this.description = description;
        this.color = null; // Colore non impostato
    }

    // Costruttore con colore
    public PNEZDPoint(int pointNumber, double northing, double easting, double elevation, String description, int color) {
        this.pointNumber = pointNumber;
        this.northing = northing;
        this.easting = easting;
        this.elevation = elevation;
        this.description = description;
        this.color = color;
    }

    // Costruttore con colore e filePath
    public PNEZDPoint(String filename, int pointNumber, double northing, double easting, double elevation, String description, int color) {
        this.pointNumber = pointNumber;
        this.northing = northing;
        this.easting = easting;
        this.elevation = elevation;
        this.description = description;
        this.color = color;
        this.filename = filename;
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

    // Getter e Setter colore
    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    // toString
    @Override
    public String toString() {
        return pointNumber + ", " + northing + ", " + easting + ", " + elevation + ", " + description +
                (color != null ? ", Color: " + color : ", Color: (none)");
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
