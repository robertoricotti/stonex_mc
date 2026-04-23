package dxf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import iredes.Point3D_Drill;
import services.ReadProjectService;

public class DXFData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Face3D> faces;                // 3DFACE
    private List<Polyline> polylines;          // POLYLINE
    private List<Point3D> points;              // POINT
    private List<Point3D_Drill> drill_points;  // DRILL POINT
    private List<Polyline_2D> polylines_2D;    // LWPOLYLINE
    private List<DxfText> texts;               // TEXT / MTEXT
    private List<Arc> arcs;                    // ARC
    private List<Circle> circles;              // CIRCLE
    private List<Line> lines;                  // LINE

    // Nuove entità
    private List<Ellipse> ellipses;            // ELLIPSE
    private List<Spline> splines;              // SPLINE
    private List<Hatch> hatches;               // HATCH
    private List<Dimension> dimensions;        // DIMENSION
    private List<Solid> solids;                // SOLID
    private List<Trace> traces;                // TRACE
    private List<Leader> leaders;              // LEADER
    private List<XLine> xlines;                // XLINE
    private List<Ray> rays;                    // RAY

    private List<Layer> layers;
    private List<DxfBlock> blocks;
    private List<DxfInsert> inserts;

    public DXFData() {
        faces = new ArrayList<>();
        polylines = new ArrayList<>();
        points = new ArrayList<>();
        drill_points = new ArrayList<>();
        polylines_2D = new ArrayList<>();
        texts = new ArrayList<>();
        arcs = new ArrayList<>();
        circles = new ArrayList<>();
        lines = new ArrayList<>();

        ellipses = new ArrayList<>();
        splines = new ArrayList<>();
        hatches = new ArrayList<>();
        dimensions = new ArrayList<>();
        solids = new ArrayList<>();
        traces = new ArrayList<>();
        leaders = new ArrayList<>();
        xlines = new ArrayList<>();
        rays = new ArrayList<>();

        layers = new ArrayList<>();
        blocks = new ArrayList<>();
        inserts = new ArrayList<>();
    }

    public List<Face3D> getFaces() {
        return faces;
    }

    public void addFace(Face3D face) {
        faces.add(face);
        try {
            ReadProjectService.numbers++;
        } catch (Exception e) {
            ReadProjectService.numbers++;
        }
    }

    public List<Line> getLines() {
        return lines;
    }

    public void addLine(Line line) {
        lines.add(line);
    }

    public List<Polyline> getPolylines() {
        return polylines;
    }

    public void addPolyline(Polyline polyline) {
        polylines.add(polyline);
    }

    public List<Polyline_2D> getPolylines_2D() {
        return polylines_2D;
    }

    public void addPolyline2D(Polyline_2D polyline_2D) {
        polylines_2D.add(polyline_2D);
    }

    public List<Point3D> getPoints() {
        return points;
    }

    public void addPoint(Point3D point) {
        points.add(point);
    }

    public List<DxfText> getTexts() {
        return texts;
    }

    public void addText(DxfText dxfText) {
        texts.add(dxfText);
    }

    public List<Arc> getArcs() {
        return arcs;
    }

    public void addArc(Arc arc) {
        arcs.add(arc);
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public void addCircle(Circle circle) {
        circles.add(circle);
    }

    public List<Ellipse> getEllipses() {
        return ellipses;
    }

    public void addEllipse(Ellipse ellipse) {
        ellipses.add(ellipse);
    }

    public List<Spline> getSplines() {
        return splines;
    }

    public void addSpline(Spline spline) {
        splines.add(spline);
    }

    public List<Hatch> getHatches() {
        return hatches;
    }

    public void addHatch(Hatch hatch) {
        hatches.add(hatch);
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public void addDimension(Dimension dimension) {
        dimensions.add(dimension);
    }

    public List<Solid> getSolids() {
        return solids;
    }

    public void addSolid(Solid solid) {
        solids.add(solid);
    }

    public List<Trace> getTraces() {
        return traces;
    }

    public void addTrace(Trace trace) {
        traces.add(trace);
    }

    public List<Leader> getLeaders() {
        return leaders;
    }

    public void addLeader(Leader leader) {
        leaders.add(leader);
    }

    public List<XLine> getXLines() {
        return xlines;
    }

    public void addXLine(XLine xline) {
        xlines.add(xline);
    }

    public List<Ray> getRays() {
        return rays;
    }

    public void addRay(Ray ray) {
        rays.add(ray);
    }

    public void addLayer(Layer layer) {
        layers.add(layer);
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public static DXFData loadOrParse(File file, Supplier<DXFData> parser) {
        File cacheFile = new File(file.getAbsolutePath() + ".bin");
        DXFData data = null;

        if (cacheFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {
                data = (DXFData) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (data == null) {
            data = parser.get();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
                oos.writeObject(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return data;
    }

    public void addBlock(DxfBlock block) {
        blocks.add(block);
    }

    public List<DxfBlock> getBlocks() {
        return blocks;
    }

    public void addInsert(DxfInsert insert) {
        inserts.add(insert);
    }

    public List<DxfInsert> getInserts() {
        return inserts;
    }

    public List<Point3D_Drill> getDrill_points() {
        return drill_points;
    }

    public void addDrill_points(Point3D_Drill point3DDrill) {
        drill_points.add(point3DDrill);
    }
}