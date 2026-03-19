package dxf;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import utils.Utils;

public class ExportDXF_Area {
    private List<double[]> coordinates; // Lista di coordinate [x, y, z]
    private String filename;
    private String path;
    private double conversionFactor;
    private double METER_TO_FEET_CONVERSION;
    static double z;

    public ExportDXF_Area(List<double[]> coordinates, String filename, String path, double conversionFactor) {
        this.coordinates = coordinates;
        this.filename = filename;
        this.path = path;
        this.conversionFactor = conversionFactor;
    }
    public void generateDXF() throws IOException {
        METER_TO_FEET_CONVERSION = conversionFactor;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + filename))) {

            DXFWriteMethods.testa(writer,3);
            DXFWriteMethods.writeLayer(writer, "LAYER_3D_FACES", 2);
            DXFWriteMethods.writeLayer(writer, "LAYER_POLYLINES", 6);
            DXFWriteMethods.writeLayer(writer, "LAYER_POINTS", 4);
            DXFWriteMethods.endLayers(writer);
            DXFWriteMethods.beginEntities(writer);

            z = coordinates.get(0)[2]; // Tutti alla stessa quota

            // Aggiungi POINT + TEXT
            int handle=0;
            for (int i = 0; i < coordinates.size(); i++) {
                handle++;
                double[] coord = coordinates.get(i);
                String pointName = "P" + (i + 1);

                // POINT
                writer.write("0\nPOINT\n8\nLAYER_POINTS\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                        coord[0] / METER_TO_FEET_CONVERSION, coord[1] / METER_TO_FEET_CONVERSION, z / METER_TO_FEET_CONVERSION));

                // TEXT
                // Aggiungi TEXT per il punto
                writer.write("0\nTEXT\n");
                writer.write("5\n"+Integer.toHexString(handle)+"\n");
                writer.write("100\nAcDbEntity\n");
                writer.write(String.format("8\n%s\n", "LAYER_POINTS"));
                writer.write("100\nAcDbText\n");
                writer.write(String.format(Locale.US,"10\n%f\n20\n%f\n30\n%f\n", coord[0] / METER_TO_FEET_CONVERSION, coord[1] / METER_TO_FEET_CONVERSION, z / METER_TO_FEET_CONVERSION));
                writer.write("40\n0.400000\n41\n1\n");
                writer.write("1\n"+" "+ pointName +"  "+ Utils.readUnitOfMeasureLITE(String.valueOf(z))+"\n");
                writer.write("50\n0\n");
            }

            // POLYLINE di contorno
            for (int i = 0; i < coordinates.size() - 1; i++) {
                createPolyline(writer, coordinates.get(i), coordinates.get(i + 1));
            }
            // chiusura ciclo
            if (coordinates.size() > 2) {
                createPolyline(writer, coordinates.get(coordinates.size() - 1), coordinates.get(0));
            }

            // 3DFACES
            List<int[]> delaunayTriangles = performDelaunayTriangulation(coordinates);
            for (int[] triangle : delaunayTriangles) {
                writer.write("0\n3DFACE\n8\nLAYER_3D_FACES\n");

                double[] p1 = coordinates.get(triangle[0]);
                double[] p2 = coordinates.get(triangle[1]);
                double[] p3 = coordinates.get(triangle[2]);

                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", p1[0] / METER_TO_FEET_CONVERSION, p1[1] / METER_TO_FEET_CONVERSION, z / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "11\n%f\n21\n%f\n31\n%f\n", p2[0] / METER_TO_FEET_CONVERSION, p2[1] / METER_TO_FEET_CONVERSION, z / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "12\n%f\n22\n%f\n32\n%f\n", p3[0] / METER_TO_FEET_CONVERSION, p3[1] / METER_TO_FEET_CONVERSION, z / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "13\n%f\n23\n%f\n33\n%f\n", p3[0] / METER_TO_FEET_CONVERSION, p3[1] / METER_TO_FEET_CONVERSION, z / METER_TO_FEET_CONVERSION));
            }

            DXFWriteMethods.coda(writer);
        }

        System.out.println("DXF file generated successfully at " + path + "/" + filename);
    }


    private void createPolyline(BufferedWriter writer, double[] start, double[] end) throws IOException {
        writer.write("0\nPOLYLINE\n8\nLAYER_POLYLINES\n66\n1\n70\n8\n10\n0.0\n20\n0.0\n30\n0.0\n");

        writer.write("0\nVERTEX\n8\nLAYER_POLYLINES\n");
        writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                start[0] / METER_TO_FEET_CONVERSION, start[1] / METER_TO_FEET_CONVERSION, z / METER_TO_FEET_CONVERSION));
        writer.write("70\n32\n");

        writer.write("0\nVERTEX\n8\nLAYER_POLYLINES\n");
        writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                end[0] / METER_TO_FEET_CONVERSION, end[1] / METER_TO_FEET_CONVERSION, z / METER_TO_FEET_CONVERSION));
        writer.write("70\n32\n");

        writer.write("0\nSEQEND\n");
    }
    private List<int[]> performDelaunayTriangulation(List<double[]> coordinates) {
        List<int[]> triangles = new ArrayList<>();
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordsArray = coordinates.stream()
                .map(coord -> new Coordinate(coord[0], coord[1], coord[2]))
                .toArray(Coordinate[]::new);

        // Creazione del poligono chiuso per l'area
        Polygon areaPolygon = createClosedPolygon(coordsArray, geometryFactory);

        DelaunayTriangulationBuilder dtBuilder = new DelaunayTriangulationBuilder();
        dtBuilder.setSites(geometryFactory.createMultiPointFromCoords(coordsArray));
        Geometry triangulation = dtBuilder.getTriangles(geometryFactory);

        for (int i = 0; i < triangulation.getNumGeometries(); i++) {
            Polygon triangle = (Polygon) triangulation.getGeometryN(i);
            Coordinate[] triangleCoords = triangle.getCoordinates();

            if (areaPolygon.contains(triangle)) {
                int[] triangleIndices = new int[3];
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < coordinates.size(); k++) {
                        if (triangleCoords[j].equals2D(new Coordinate(coordinates.get(k)[0], coordinates.get(k)[1]))) {
                            triangleIndices[j] = k;
                            break;
                        }
                    }
                }
                triangles.add(triangleIndices);
            }
        }

        return triangles;
    }

    private Polygon createClosedPolygon(Coordinate[] coordinates, GeometryFactory geometryFactory) {
        if (!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
            Coordinate[] closedCoordinates = new Coordinate[coordinates.length + 1];
            System.arraycopy(coordinates, 0, closedCoordinates, 0, coordinates.length);
            closedCoordinates[closedCoordinates.length - 1] = coordinates[0];
            coordinates = closedCoordinates;
        }
        return geometryFactory.createPolygon(coordinates);
    }
}
