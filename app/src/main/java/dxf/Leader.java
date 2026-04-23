package dxf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Leader implements Serializable {
    private static final long serialVersionUID = 1L;

    List<Point3D> vertices = new ArrayList<>();
    boolean hasArrowHead;
    int color;
    Layer layer;
    private DxfStyle dxfStyle;

    public List<Point3D> getVertices() { return vertices; }

    public boolean isHasArrowHead() { return hasArrowHead; }
    public void setHasArrowHead(boolean hasArrowHead) { this.hasArrowHead = hasArrowHead; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public Layer getLayer() { return layer; }
    public void setLayer(Layer layer) { this.layer = layer; }

    public DxfStyle getDxfStyle() { return dxfStyle; }
    public void setDxfStyle(DxfStyle dxfStyle) {
        this.dxfStyle = dxfStyle != null ? dxfStyle.copy() : null;
    }

    @Override
    public Leader clone() {
        Leader l = new Leader();
        l.hasArrowHead = hasArrowHead;
        l.color = color;
        l.layer = layer;
        l.dxfStyle = this.dxfStyle != null ? this.dxfStyle.copy() : null;

        for (Point3D p : vertices) l.vertices.add(p.clone());
        return l;
    }
}