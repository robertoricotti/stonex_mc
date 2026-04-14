package dxf;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.operation.buffer.OffsetCurveBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility per creare una polyline offsettata (aperta) usando JTS.
 */
public class JTSOffsetHelper {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Genera una polyline offsettata a partire da una polyline originale,
     * mantenendola aperta (nessun boundary).
     *
     * @param polyline Polyline originale (almeno 2 punti)
     * @param offset   distanza di offset (positiva o negativa)
     * @return Polyline offsettata, o null se errore
     */
    public static Polyline generateOffsetPolyline(Polyline polyline, double offset) {
        if (polyline == null || polyline.getVertices().size() < 2) return null;

        // Converti in LineString JTS
        Coordinate[] coords = new Coordinate[polyline.getVertices().size()];
        for (int i = 0; i < polyline.getVertices().size(); i++) {
            Point3D p = polyline.getVertices().get(i);
            coords[i] = new Coordinate(p.getX(), p.getY());
        }

        LineString line = geometryFactory.createLineString(coords);

        // Costruisce una curva offset aperta
        BufferParameters bufferParams = new BufferParameters();
        bufferParams.setEndCapStyle(BufferParameters.CAP_FLAT); // o CAP_ROUND o CAP_SQUARE se preferisci

        OffsetCurveBuilder offsetBuilder = new OffsetCurveBuilder(
                new PrecisionModel(), bufferParams
        );

        Coordinate[] offsetCoords = offsetBuilder.getOffsetCurve(coords, offset);

        if (offsetCoords == null || offsetCoords.length < 2) return null;

        // Converti in Point3D e ricrea una Polyline
        List<Point3D> offsetPoints = new ArrayList<>();
        for (Coordinate c : offsetCoords) {
            offsetPoints.add(new Point3D(c.x, c.y, 0));
        }

        Polyline offsetPolyline = new Polyline();
        offsetPolyline.setVertices(offsetPoints);
        offsetPolyline.setLayer(polyline.getLayer());
        offsetPolyline.setLineColor(polyline.getLineColor());

        return offsetPolyline;
    }
}