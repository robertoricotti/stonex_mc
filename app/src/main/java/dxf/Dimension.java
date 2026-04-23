package dxf;

import java.io.Serializable;

public class Dimension implements Serializable {
    private static final long serialVersionUID = 1L;

    String dimensionTypeName;
    String text;
    String blockName;
    Point3D definitionPoint;
    Point3D textMidPoint;
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    public String getDimensionTypeName() { return dimensionTypeName; }
    public void setDimensionTypeName(String dimensionTypeName) { this.dimensionTypeName = dimensionTypeName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public Point3D getDefinitionPoint() { return definitionPoint; }
    public void setDefinitionPoint(Point3D definitionPoint) { this.definitionPoint = definitionPoint; }

    public Point3D getTextMidPoint() { return textMidPoint; }
    public void setTextMidPoint(Point3D textMidPoint) { this.textMidPoint = textMidPoint; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public Layer getLayer() { return layer; }
    public void setLayer(Layer layer) { this.layer = layer; }

    public DxfStyle getDxfStyle() { return dxfStyle; }
    public void setDxfStyle(DxfStyle dxfStyle) {
        this.dxfStyle = dxfStyle != null ? dxfStyle.copy() : null;
    }

    @Override
    public Dimension clone() {
        Dimension d = new Dimension();
        d.dimensionTypeName = dimensionTypeName;
        d.text = text;
        d.blockName = blockName;
        d.definitionPoint = definitionPoint != null ? definitionPoint.clone() : null;
        d.textMidPoint = textMidPoint != null ? textMidPoint.clone() : null;
        d.color = color;
        d.layer = layer;
        d.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;
        return d;
    }
}