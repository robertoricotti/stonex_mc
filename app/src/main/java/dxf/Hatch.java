package dxf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hatch implements Serializable {
    private static final long serialVersionUID = 1L;

    List<Polyline> boundaryPolylines = new ArrayList<>();
    String patternName;
    boolean solidFill;
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    public List<Polyline> getBoundaryPolylines() { return boundaryPolylines; }

    public String getPatternName() { return patternName; }
    public void setPatternName(String patternName) { this.patternName = patternName; }

    public boolean isSolidFill() { return solidFill; }
    public void setSolidFill(boolean solidFill) { this.solidFill = solidFill; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public Layer getLayer() { return layer; }
    public void setLayer(Layer layer) { this.layer = layer; }

    public DxfStyle getDxfStyle() { return dxfStyle; }
    public void setDxfStyle(DxfStyle dxfStyle) {
        this.dxfStyle = dxfStyle != null ? dxfStyle.copy() : null;
    }

    @Override
    public Hatch clone() {
        Hatch h = new Hatch();
        h.patternName = patternName;
        h.solidFill = solidFill;
        h.color = color;
        h.layer = layer;
        h.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;

        for (Polyline p : boundaryPolylines) {
            h.boundaryPolylines.add(p.clone());
        }
        return h;
    }
}