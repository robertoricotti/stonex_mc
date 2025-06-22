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
        Layer layer = (Layer) obj;
        return Objects.equals(projName, layer.projName) &&
                Objects.equals(layerName, layer.layerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projName, layerName);
    }

}
