package iredes;

import java.io.Serializable;

/**
 * Punto di trivellazione: testa (start/head) + fine (end/to) e metadati.
 * Tutti i campi sono opzionali: se non presenti nel file sorgente restano null.
 */
public class Point3D_Drill implements Serializable {

    private static final long serialVersionUID = 1L;
    // 0 = da fare, 1 = fatto, -1 = abortito , -2 rifiutato
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

    private String drlStart_1;
    private String drlStop_1;
    private String pr_1;

    private String drlStart_2;
    private String drlStop_2;
    private String pr_2;

    private String drlStart_3;
    private String drlStop_3;
    private String pr_3;

    private String drlStart_4;
    private String drlStop_4;
    private String pr_4;

    private String jetStart_1;
    private String jetStop_1;
    private String pr_j_1;

    private String jetStart_2;
    private String jetStop_2;
    private String pr_j_2;

    private String jetStart_3;
    private String jetStop_3;
    private String pr_j_3;

    private String jetStart_4;
    private String jetStop_4;
    private String pr_j_4;


    public Point3D_Drill() {
    }

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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRowId() {
        if (rowId != null) {
            return rowId;
        } else return "";
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getHeadX() {
        return headX;
    }

    public void setHeadX(Double headX) {
        this.headX = headX;
    }

    public Double getHeadY() {
        return headY;
    }

    public void setHeadY(Double headY) {
        this.headY = headY;
    }

    public Double getHeadZ() {
        return headZ;
    }

    public void setHeadZ(Double headZ) {
        this.headZ = headZ;
    }

    public Double getEndX() {
        return endX;
    }

    public void setEndX(Double endX) {
        this.endX = endX;
    }

    public Double getEndY() {
        return endY;
    }

    public void setEndY(Double endY) {
        this.endY = endY;
    }

    public Double getEndZ() {
        return endZ;
    }

    public void setEndZ(Double endZ) {
        this.endZ = endZ;
    }

    public Double getDiameter() {
        return diameter;
    }

    public void setDiameter(Double diameter) {
        this.diameter = diameter;
    }

    public Double getTilt() {
        return tilt;
    }

    public void setTilt(Double tilt) {
        this.tilt = tilt;
    }

    public Double getHeadingDeg() {
        return headingDeg;
    }

    public Double getDepth() {
        return depth;
    }

    public Double getLength() {
        return length;
    }

    public void setHeadingDeg(Double headingDeg) {
        this.headingDeg = headingDeg;
    }

    public void setDepth(Double depth) {
        this.depth = depth;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

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

    public String getDrlStart_1() {
        return drlStart_1;
    }

    public String getDrlStart_2() {
        return drlStart_2;
    }

    public String getDrlStart_3() {
        return drlStart_3;
    }

    public String getDrlStart_4() {
        return drlStart_4;
    }

    public String getDrlStop_1() {
        return drlStop_1;
    }

    public String getDrlStop_2() {
        return drlStop_2;
    }

    public String getDrlStop_3() {
        return drlStop_3;
    }

    public String getDrlStop_4() {
        return drlStop_4;
    }

    public String getJetStart_1() {
        return jetStart_1;
    }

    public String getJetStart_2() {
        return jetStart_2;
    }

    public String getJetStart_3() {
        return jetStart_3;
    }

    public String getJetStart_4() {
        return jetStart_4;
    }

    public String getJetStop_1() {
        return jetStop_1;
    }

    public String getJetStop_2() {
        return jetStop_2;
    }

    public String getJetStop_3() {
        return jetStop_3;
    }

    public String getJetStop_4() {
        return jetStop_4;
    }

    public String getPr_1() {
        return pr_1;
    }

    public String getPr_2() {
        return pr_2;
    }

    public String getPr_3() {
        return pr_3;
    }

    public String getPr_4() {
        return pr_4;
    }

    public String getPr_j_1() {
        return pr_j_1;
    }

    public String getPr_j_2() {
        return pr_j_2;
    }

    public String getPr_j_3() {
        return pr_j_3;
    }

    public String getPr_j_4() {
        return pr_j_4;
    }

    public void setDrlStart_1(String drlStart_1) {
        this.drlStart_1 = drlStart_1;
    }

    public void setDrlStart_2(String drlStart_2) {
        this.drlStart_2 = drlStart_2;
    }

    public void setDrlStart_3(String drlStart_3) {
        this.drlStart_3 = drlStart_3;
    }

    public void setDrlStart_4(String drlStart_4) {
        this.drlStart_4 = drlStart_4;
    }

    public void setDrlStop_1(String drlStop_1) {
        this.drlStop_1 = drlStop_1;
    }

    public void setDrlStop_2(String drlStop_2) {
        this.drlStop_2 = drlStop_2;
    }

    public void setDrlStop_3(String drlStop_3) {
        this.drlStop_3 = drlStop_3;
    }

    public void setDrlStop_4(String drlStop_4) {
        this.drlStop_4 = drlStop_4;
    }

    public void setJetStart_1(String jetStart_1) {
        this.jetStart_1 = jetStart_1;
    }

    public void setJetStart_2(String jetStart_2) {
        this.jetStart_2 = jetStart_2;
    }

    public void setJetStart_3(String jetStart_3) {
        this.jetStart_3 = jetStart_3;
    }

    public void setJetStart_4(String jetStart_4) {
        this.jetStart_4 = jetStart_4;
    }

    public void setJetStop_1(String jetStop_1) {
        this.jetStop_1 = jetStop_1;
    }
    public void setJetStop_2(String jetStop_2) {
        this.jetStop_2 = jetStop_2;
    }

    public void setJetStop_3(String jetStop_3) {
        this.jetStop_3 = jetStop_3;
    }

    public void setJetStop_4(String jetStop_4) {
        this.jetStop_4 = jetStop_4;
    }

    public void setPr_j_1(String pr_j_1) {
        this.pr_j_1 = pr_j_1;
    }

    public void setPr_j_2(String pr_j_2) {
        this.pr_j_2 = pr_j_2;
    }

    public void setPr_j_3(String pr_j_3) {
        this.pr_j_3 = pr_j_3;
    }

    public void setPr_j_4(String pr_j_4) {
        this.pr_j_4 = pr_j_4;
    }

    public void setPr_1(String pr_1) {
        this.pr_1 = pr_1;
    }

    public void setPr_2(String pr_2) {
        this.pr_2 = pr_2;
    }

    public void setPr_3(String pr_3) {
        this.pr_3 = pr_3;
    }

    public void setPr_4(String pr_4) {
        this.pr_4 = pr_4;
    }


}