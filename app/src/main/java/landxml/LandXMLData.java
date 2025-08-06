package landxml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import dxf.Polyline;
import services.ReadProjectService;

public class LandXMLData implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Face3D> faces; // Cambiato da double[][] a Face3D
    private List<Polyline> polylines;
    private List<Point3D> points;
    private List<DxfText> texts;
    private List<Layer> layers;

    public LandXMLData() {
        faces = new ArrayList<>();
        polylines = new ArrayList<>();
        points = new ArrayList<>();
        texts = new ArrayList<>();
        layers = new ArrayList<>();
    }

    // Getter per Face3D
    public List<Face3D> getFaces() {
        return faces;
    }

    // Aggiunge una Face3D
    public void addFace(Face3D face) {
        faces.add(face);
        try {
            ReadProjectService.numbers++;
        } catch (Exception e) {
            ReadProjectService.numbers++;
        }
    }

    // Getter per Polyline
    public List<Polyline> getPolylines() {
        return polylines;
    }

    // Aggiunge una Polyline
    public void addPolyline(Polyline polyline) {
        polylines.add(polyline);
    }

    // Getter per Point3D
    public List<Point3D> getPoints() {
        return points;
    }

    // Aggiunge un Point3D
    public void addPoint(Point3D point) {
        points.add(point);
    }

    // Getter per DxfText
    public List<DxfText> getTexts() {
        return texts;
    }

    // Aggiunge un DxfText
    public void addText(DxfText text) {
        texts.add(text);
    }

    // Getter per Layer
    public List<Layer> getLayers() {
        return layers;
    }

    // Aggiunge un Layer
    public void addLayer(Layer layer) {
        layers.add(layer);
    }
}

