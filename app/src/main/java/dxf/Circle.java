package dxf;

import java.io.Serializable;

public class Circle implements Serializable {
    private static final long serialVersionUID = 1L;
     Point3D center; // Centro del cerchio come oggetto Point3D
     double radius;  // Raggio del cerchio
     int color;// Colore del cerchio in formato int (RGB o ARGB)
    Layer layer;

    // Costruttore per inizializzare il centro, raggio e colore
    public Circle(Point3D center, double radius, int color,Layer layer) {
        this.center = center;
        this.radius = radius;
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
        return "Circle{" +
                "center=" + center +
                ", radius=" + radius +
                ", color=" + color +
                '}';
    }

    @Override
    public Circle clone() {
        Circle c = new Circle(
                center.clone(),
                radius,
                color,
                layer
        );
        return c;
    }
}

