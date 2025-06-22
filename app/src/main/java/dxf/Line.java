package dxf;

import java.io.Serializable;

public class Line implements Serializable {
    private static final long serialVersionUID = 1L;
    Point3D start;
    Point3D end;
    int color;
    Layer layer;

    public Line (Point3D start,Point3D end,int color,Layer layer){
        this.start=start;
        this.end=end;
        this.color=color;
        this.layer=layer;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setStart(Point3D start) {
        this.start = start;
    }

    public void setEnd(Point3D end) {
        this.end = end;
    }

    public Point3D getStart() {
        return start;
    }

    public Point3D getEnd() {
        return end;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }
}
