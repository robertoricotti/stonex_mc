package dxf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import iredes.Point3D_Drill;

/**
 * Contenitore completo di un BLOCK DXF.
 * <p>
 * Tiene sia il base point originale del blocco che le entità contenute,
 * inclusi eventuali INSERT annidati.
 */
public class DxfBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private final List<Line> lines = new ArrayList<>();
    private final List<Polyline> polylines = new ArrayList<>();
    private final List<Polyline_2D> polylines2D = new ArrayList<>();
    private final List<Point3D> points = new ArrayList<>();
    private final List<Point3D_Drill> point3DDrills = new ArrayList<>();
    private final List<Circle> circles = new ArrayList<>();
    private final List<Arc> arcs = new ArrayList<>();
    private final List<DxfText> texts = new ArrayList<>();
    private final List<Face3D> faces = new ArrayList<>();
    private final List<DxfInsert> inserts = new ArrayList<>();

    /**
     * Origine del block (group code 10/20/30 del record BLOCK).
     */
    private double baseX;
    private double baseY;
    private double baseZ;

    public DxfBlock(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBaseX() {
        return baseX;
    }

    public void setBaseX(double baseX) {
        this.baseX = baseX;
    }

    public double getBaseY() {
        return baseY;
    }

    public void setBaseY(double baseY) {
        this.baseY = baseY;
    }

    public double getBaseZ() {
        return baseZ;
    }

    public void setBaseZ(double baseZ) {
        this.baseZ = baseZ;
    }

    public void setBasePoint(double x, double y, double z) {
        this.baseX = x;
        this.baseY = y;
        this.baseZ = z;
    }

    public List<Line> getLines() {
        return lines;
    }

    public List<Polyline> getPolylines() {
        return polylines;
    }

    public List<Polyline_2D> getPolylines2D() {
        return polylines2D;
    }

    public List<Point3D> getPoints() {
        return points;
    }

    public List<Point3D_Drill> getPoint3DDrills() {
        return point3DDrills;
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public List<Arc> getArcs() {
        return arcs;
    }

    public List<DxfText> getTexts() {
        return texts;
    }

    public List<Face3D> getFaces() {
        return faces;
    }

    public List<DxfInsert> getInserts() {
        return inserts;
    }

    public void addLine(Line line) {
        if (line != null) lines.add(line);
    }

    public void addPolyline(Polyline polyline) {
        if (polyline != null) polylines.add(polyline);
    }

    public void addPolyline2D(Polyline_2D polyline) {
        if (polyline != null) polylines2D.add(polyline);
    }

    public void addPoint(Point3D point) {
        if (point != null) points.add(point);
    }

    public void addPointDrill(Point3D_Drill pointDrill) {
        if (pointDrill != null) point3DDrills.add(pointDrill);
    }

    public void addCircle(Circle circle) {
        if (circle != null) circles.add(circle);
    }

    public void addArc(Arc arc) {
        if (arc != null) arcs.add(arc);
    }

    public void addText(DxfText text) {
        if (text != null) texts.add(text);
    }

    public void addFace(Face3D face) {
        if (face != null) faces.add(face);
    }

    public void addInsert(DxfInsert insert) {
        if (insert != null) inserts.add(insert);
    }

    public boolean isEmpty() {
        return lines.isEmpty()
                && polylines.isEmpty()
                && polylines2D.isEmpty()
                && points.isEmpty()
                && point3DDrills.isEmpty()
                && circles.isEmpty()
                && arcs.isEmpty()
                && texts.isEmpty()
                && faces.isEmpty()
                && inserts.isEmpty();
    }
}
