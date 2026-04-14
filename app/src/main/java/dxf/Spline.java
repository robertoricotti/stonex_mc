package dxf;

import java.util.ArrayList;
import java.util.List;

public class Spline {

    List<Point3D> controlPoints = new ArrayList<>();

    public Spline() {

    }

    public List<Point3D> getControlPoints() {
        return controlPoints;
    }
}
