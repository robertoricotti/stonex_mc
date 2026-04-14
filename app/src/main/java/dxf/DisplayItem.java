package dxf;

public class DisplayItem {
    public static final int TYPE_FILE = 0;
    public static final int TYPE_LAYER = 1;

    private int type; // Tipo di elemento (file o layer)
    private String fileName; // Nome del file
    private String layerName; // Nome del layer
    private Integer color; // Colore associato al layer
    private boolean isEnable; // Stato abilitato/disabilitato del layer

    // Costruttore per file
    public DisplayItem(int type, String fileName, String layerName) {
        this.type = type;
        this.fileName = fileName;
        this.layerName = layerName;
        this.color = -1;
        this.isEnable = true; // Default: abilitato
    }

    // Costruttore per layer
    public DisplayItem(int type, String fileName, String layerName, Integer color) {
        this.type = type;
        this.fileName = fileName;
        this.layerName = layerName;
        this.color = color;
        this.isEnable = true; // Default: abilitato
    }

    // Getter e setter
    public int getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLayerName() {
        return layerName;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
