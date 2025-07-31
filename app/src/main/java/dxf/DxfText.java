package dxf;

import java.io.Serializable;

public class DxfText implements Serializable {
    private static final long serialVersionUID = 1L;

    String text;
    double x, y, z;
    int colore;
    Layer layer;

    public DxfText(String text, double x, double y, double z,int colore,Layer layer) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.z = z;
        this.colore=colore;
        this.layer=layer;
    }
    public DxfText(String text, double x, double y, double z,int colore) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.z = z;
        this.colore=colore;

    }

    @Override
    public String toString() {

            return "Text{" +
                    "text='" + text + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';

    }

    public double getZ() {
        return z;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColore(int colore) {
        this.colore = colore;
    }

    public int getColore() {
        return colore;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }
}
