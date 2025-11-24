package dxf;

import java.io.Serializable;
import java.util.Objects;

public class Layer implements Serializable {
    private static final long serialVersionUID = 1L;
    String projName;
    String layerName;
    Integer color;
    boolean enable;

    public Layer(String projName, String layerName, Integer color, boolean enable) {
        this.projName = projName;
        this.layerName = layerName;
        this.color = color;
        this.enable = enable;

    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setProjName(String projName) {
        this.projName = projName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public void setColorState(Integer color) {
        this.color = color;
    }

    public String getProjName() {
        return projName;
    }

    public String getLayerName() {
        if (layerName != null) {
            return layerName;
        } else {
            return "null";
        }
    }

    public Integer getColorState() {
        try {
            return (this.color);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Layer other = (Layer) obj;

        String thisProj = projName == null ? "" : projName.trim().toLowerCase();
        String otherProj = other.projName == null ? "" : other.projName.trim().toLowerCase();

        String thisLayer = layerName == null ? "" : layerName.trim().toLowerCase();
        String otherLayer = other.layerName == null ? "" : other.layerName.trim().toLowerCase();

        // Rimuove spazi multipli e caratteri invisibili
        thisLayer = thisLayer.replaceAll("\\s+", " ");
        otherLayer = otherLayer.replaceAll("\\s+", " ");

        return thisProj.equals(otherProj) && thisLayer.equals(otherLayer);
    }

    @Override
    public int hashCode() {
        String normProj = projName == null ? "" : projName.trim().toLowerCase().replaceAll("\\s+", " ");
        String normLayer = layerName == null ? "" : layerName.trim().toLowerCase().replaceAll("\\s+", " ");
        return Objects.hash(normProj, normLayer);
    }


}
