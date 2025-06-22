package dxf;

import java.io.Serializable;

public class Arc implements Serializable {
    private static final long serialVersionUID = 1L;
    Point3D center; // Centro dell'arco come oggetto Point3D
    double radius;  // Raggio dell'arco
    double startAngle; // Angolo iniziale in gradi
    double endAngle;   // Angolo finale in gradi
    int color;         // Colore dell'arco in formato int (RGB o ARGB)
    Layer layer;

    // Costruttore per inizializzare l'arco con centro, raggio, angoli e colore
    public Arc(Point3D center, double radius, double startAngle, double endAngle, int color,Layer layer) {
        this.center = center;
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.color = color;
        this.layer=layer;
    }

    // Getter e Setter per i vari attributi
    public Point3D getCenter() {
        return center;
    }

    public void setCenter(Point3D center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public double getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }

    @Override
    public String toString() {
        return "Arc{" +
                "center=" + center +
                ", radius=" + radius +
                ", startAngle=" + startAngle +
                ", endAngle=" + endAngle +
                ", color=" + color +
                '}';
    }
}
