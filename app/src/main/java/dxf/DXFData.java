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

public class DXFData implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Face3D> faces; // Cambiato da double[][] a Face3D
    private List<Polyline> polylines;
    private List<Point3D> points;
    private List<Polyline_2D> polylines_2D;
    private List<DxfText> texts;
    private List<Arc> arcs;
    private List<Circle> circles;
    private List<Line> lines;
    private List<Layer> layers;

    public DXFData() {
        faces = new ArrayList<>();
        polylines = new ArrayList<>();
        points = new ArrayList<>();
        polylines_2D = new ArrayList<>();
        texts = new ArrayList<>();
        arcs = new ArrayList<>();
        circles = new ArrayList<>();
        lines = new ArrayList<>();
        layers = new ArrayList<>();
    }

    // Getter per Face3D
    public List<Face3D> getFaces() {
        return faces;
    }

    // Aggiunge una Face3D
    public void addFace(Face3D face) {
        faces.add(face);
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

}
