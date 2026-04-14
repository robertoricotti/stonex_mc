package dxf;

import java.io.Serializable;

/**
 * Stile sorgente DXF dell'entità, separato dallo stato di rendering risolto.
 * Serve soprattutto per gestire correttamente BYLAYER/BYBLOCK durante
 * esplosione blocchi, clone e post-processing.
 */
public class DxfStyle implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ColorMode {
        BYLAYER,
        BYBLOCK,
        EXPLICIT
    }

    private String layerName = "0";
    private ColorMode colorMode = ColorMode.BYLAYER;
    private Integer explicitArgb;

    public DxfStyle() {
    }

    public DxfStyle(String layerName, ColorMode colorMode, Integer explicitArgb) {
        setLayerName(layerName);
        this.colorMode = colorMode != null ? colorMode : ColorMode.BYLAYER;
        this.explicitArgb = explicitArgb;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = sanitizeLayerName(layerName);
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode != null ? colorMode : ColorMode.BYLAYER;
    }

    public Integer getExplicitArgb() {
        return explicitArgb;
    }

    public void setExplicitArgb(Integer explicitArgb) {
        this.explicitArgb = explicitArgb;
    }

    public boolean hasExplicitColor() {
        return explicitArgb != null;
    }

    public DxfStyle copy() {
        return new DxfStyle(layerName, colorMode, explicitArgb);
    }

    public static String sanitizeLayerName(String layerName) {
        if (layerName == null || layerName.trim().isEmpty()) {
            return "0";
        }
        return layerName.trim();
    }
}
