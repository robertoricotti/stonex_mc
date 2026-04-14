package dxf;

import java.util.ArrayList;
import java.util.List;

public class GeoJsonData {
    private List<Polyline_2D> polylinesJson;

    public GeoJsonData() {
        this.polylinesJson = new ArrayList<>();
    }

    public List<Polyline_2D> getPolylinesJson() {
        return polylinesJson;
    }

    public void setPolylinesJson(List<Polyline_2D> polylinesJson) {
        this.polylinesJson = polylinesJson;
    }
}
