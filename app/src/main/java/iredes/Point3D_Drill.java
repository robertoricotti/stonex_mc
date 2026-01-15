package iredes;

import java.io.Serializable;

/**
 * Punto di trivellazione: testa (start/head) + fine (end/to) e metadati.
 * Tutti i campi sono opzionali: se non presenti nel file sorgente restano null.
 */
public class Point3D_Drill implements Serializable {

    private static final long serialVersionUID = 1L;
    // 0 = da fare, 1 = fatto, -1 = abortito
    private Integer status;  // nullable, default 0 consigliato

    // Identificativi / metadati
    private String id;          // id del punto (o name)
    private String rowId;       // riga/gruppo di appartenenza (se presente)
    private String description; // descrizione

    // Coordinate testa
    private Double headX;
    private Double headY;
    private Double headZ;

    // Coordinate fine
    private Double endX;
    private Double endY;
    private Double endZ;

    // Dati geometrici / tecnici
    private Double diameter;    // diametro palo
    private Double tilt;        // inclinazione (tilt)

    // Derivati (calcolati on-demand; possono restare null se manca una coordinata)
    private Double headingDeg;  // azimut da testa a fine (0..360)
    private Double depth;       // profondità = headZ - endZ (positiva se scende)
    private Double length;      // lunghezza 3D testa-fine

    public Point3D_Drill() {}

    public Point3D_Drill(String id) {
        this.id = id;
    }

    // -------------------------
    // Calcoli derivati
    // -------------------------
    public void recomputeDerived() {
        // Heading (azimut)
        if (headX != null && headY != null && endX != null && endY != null) {
            double dx = endX - headX; // Est
            double dy = endY - headY; // Nord

            double deg = Math.toDegrees(Math.atan2(dx, dy));
            if (deg < 0) deg += 360.0;

            headingDeg = deg;
        } else {
            headingDeg = null;
        }

        // Depth (delta quota)
        if (headZ != null && endZ != null) {
            depth = headZ - endZ; // positiva se end è più basso
        } else {
            depth = null;
        }

        // Length 3D
        if (headX != null && headY != null && headZ != null &&
                endX != null && endY != null && endZ != null) {
            double dx = endX - headX;
            double dy = endY - headY;
            double dz = endZ - headZ;
            length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        } else {
            length = null;
        }
    }

    // -------------------------
    // Getter / Setter
    // -------------------------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRowId() { return rowId; }
    public void setRowId(String rowId) { this.rowId = rowId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getHeadX() { return headX; }
    public void setHeadX(Double headX) { this.headX = headX; }
    public Double getHeadY() { return headY; }
    public void setHeadY(Double headY) { this.headY = headY; }
    public Double getHeadZ() { return headZ; }
    public void setHeadZ(Double headZ) { this.headZ = headZ; }

    public Double getEndX() { return endX; }
    public void setEndX(Double endX) { this.endX = endX; }
    public Double getEndY() { return endY; }
    public void setEndY(Double endY) { this.endY = endY; }
    public Double getEndZ() { return endZ; }
    public void setEndZ(Double endZ) { this.endZ = endZ; }

    public Double getDiameter() { return diameter; }
    public void setDiameter(Double diameter) { this.diameter = diameter; }

    public Double getTilt() { return tilt; }
    public void setTilt(Double tilt) { this.tilt = tilt; }

    public Double getHeadingDeg() { return headingDeg; }
    public Double getDepth() { return depth; }
    public Double getLength() { return length; }

    public void setHeadingDeg(Double headingDeg) { this.headingDeg = headingDeg; }
    public void setDepth(Double depth) { this.depth = depth; }
    public void setLength(Double length) { this.length = length; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    @Override
    public String toString() {
        return "Point3D_Drill{" +
                "id='" + id + '\'' +
                ", rowId='" + rowId + '\'' +
                ", description='" + description + '\'' +
                ", head=(" + headX + "," + headY + "," + headZ + ")" +
                ", end=(" + endX + "," + endY + "," + endZ + ")" +
                ", diameter=" + diameter +
                ", tilt=" + tilt +
                ", headingDeg=" + headingDeg +
                ", depth=" + depth +
                ", length=" + length +
                '}';
    }
}