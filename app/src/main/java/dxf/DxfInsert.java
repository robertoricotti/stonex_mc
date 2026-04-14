package dxf;

import java.io.Serializable;

public class DxfInsert implements Serializable {

    private static final long serialVersionUID = 1L;

    private String blockName;
    private double x, y, z;
    private double rotation;   // group code 50
    private double scaleX;     // 41
    private double scaleY;     // 42
    private double scaleZ;     // 43
    private Layer layer;

    public DxfInsert(String blockName) {
        this.blockName = blockName;
        this.scaleX = this.scaleY = this.scaleZ = 1.0; // default
    }

    public String getBlockName() {
        return blockName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getScaleX() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public double getScaleZ() {
        return scaleZ;
    }

    public void setScaleX(double s) {
        this.scaleX = s;
    }

    public void setScaleY(double s) {
        this.scaleY = s;
    }

    public void setScaleZ(double s) {
        this.scaleZ = s;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public void setBlockName(String name) {
        this.blockName = name;
    }
}
