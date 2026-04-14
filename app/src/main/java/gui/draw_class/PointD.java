package gui.draw_class;

public class PointD {

    double x;
    double y;

    public PointD(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "PointD(" +
                "x=" + x +
                ", y=" + y +
                ')';
    }
}
