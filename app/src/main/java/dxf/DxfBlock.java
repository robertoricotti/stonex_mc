package dxf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import iredes.Point3D_Drill;

public class DxfBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private List<Line> lines = new ArrayList<>();
    private List<Polyline> polylines = new ArrayList<>();
    private List<Polyline_2D> polylines2D = new ArrayList<>();
    private List<Point3D> points = new ArrayList<>();
    private List<Point3D_Drill> point3DDrills=new ArrayList<>();
    private List<Circle> circles = new ArrayList<>();
    private List<Arc> arcs = new ArrayList<>();
    private List<DxfText> texts = new ArrayList<>();
    private List<Face3D> faces = new ArrayList<>();

    // origine del block (codici 10/20/30)
    private double baseX;
    private double baseY;
    private double baseZ;

    public DxfBlock(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public double getBaseX() { return baseX; }
    public double getBaseY() { return baseY; }
    public double getBaseZ() { return baseZ; }

    public void setBasePoint(double x, double y, double z) {
        this.baseX = x;
        this.baseY = y;
        this.baseZ = z;
    }

    public List<Line> getLines() { return lines; }
    public List<Polyline> getPolylines() { return polylines; }
    public List<Polyline_2D> getPolylines2D() { return polylines2D; }
    public List<Point3D> getPoints() { return points; }
    public List<Point3D_Drill> getPoint3DDrills() { return point3DDrills; }
    public List<Circle> getCircles() { return circles; }
    public List<Arc> getArcs() { return arcs; }
    public List<DxfText> getTexts() { return texts; }
    public List<Face3D> getFaces() { return faces; }

    public void addLine(Line l) { lines.add(l); }
    public void addPolyline(Polyline p) { polylines.add(p); }
    public void addPolyline2D(Polyline_2D p) { polylines2D.add(p); }
    public void addPoint(Point3D p) { points.add(p); }
    public void addPointDrill(Point3D_Drill p) { point3DDrills.add(p); }
    public void addCircle(Circle c) { circles.add(c); }
    public void addArc(Arc a) { arcs.add(a); }
    public void addText(DxfText t) { texts.add(t); }
    public void addFace(Face3D f) { faces.add(f); }

    public void setName(String name) {
        this.name = name;
    }
}
