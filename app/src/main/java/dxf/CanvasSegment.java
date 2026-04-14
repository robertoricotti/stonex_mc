package dxf;

public class CanvasSegment {
    public float x1, y1, x2, y2;
    public Polyline polyline;

    public CanvasSegment(float x1, float y1, float x2, float y2, Polyline polyline) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.polyline = polyline;
    }
}

