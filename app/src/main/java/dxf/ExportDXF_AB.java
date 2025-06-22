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

public class ExportDXF_AB {
    private Point3D[] points; // Array con punti da A a F
    private String filename;
    private String path;
    private boolean useFeet; // Nuovo parametro per l'uso di piedi
    private double METER_TO_FEET_CONVERSION; // Conversione metri -> piedi
    public ExportDXF_AB(Point3D[] points, String filename, String path, boolean useFeet) {
        this.points = points;
        this.filename = filename;
        this.path = path;
        this.useFeet = useFeet;
    }
    public void generateDXF() throws IOException {
        METER_TO_FEET_CONVERSION = useFeet ? 0.3048006096 : 1.0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + filename))) {
            DXFWriteMethods.testa(writer,3);
            DXFWriteMethods.writeLayer(writer, "LAYER_3D_FACES", 2);
            DXFWriteMethods.writeLayer(writer, "LAYER_POLYLINES", 6);
            DXFWriteMethods.writeLayer(writer, "LAYER_POINTS", 4);
            DXFWriteMethods.endLayers(writer);
            DXFWriteMethods.beginEntities(writer);

            // POINT + TEXT
            int handle = 0;
            char name = 'A';
            for (Point3D point : points) {
                handle++;
                point.description = " "+String.valueOf(name++)+" "+ Utils.readUnitOfMeasureLITE(String.valueOf(point.getZ()));
                double x = point.x / METER_TO_FEET_CONVERSION;
                double y = point.y / METER_TO_FEET_CONVERSION;
                double z = point.z / METER_TO_FEET_CONVERSION;

                // POINT
                writer.write("0\nPOINT\n8\nLAYER_POINTS\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));

                // TEXT
                writer.write("0\nTEXT\n5\n"+Integer.toHexString(handle)+"\n100\nAcDbEntity\n8\nLAYER_POINTS\n100\nAcDbText\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));
                writer.write("40\n0.4\n41\n1\n");
                writer.write("1\n" + point.description + "\n");
                writer.write("50\n0\n");
            }

            // POLYLINE: contorno (AB, BC, CD, DA)
            createPolyline(writer, points[0], points[1]); // AB
            createPolyline(writer, points[1], points[2]); // BC
            createPolyline(writer, points[2], points[3]); // CD
            createPolyline(writer, points[3], points[0]); // DA

            // POLYLINE: diagonali (BE, EF, FA)
            createPolyline(writer, points[1], points[4]); // BE
            createPolyline(writer, points[4], points[5]); // EF
            createPolyline(writer, points[5], points[0]); // FA

            // 3DFACE (triangolazione)
            List<int[]> delaunayTriangles = performDelaunayTriangulation(points);
            for (int[] triangle : delaunayTriangles) {
                Point3D p1 = points[triangle[0]];
                Point3D p2 = points[triangle[1]];
                Point3D p3 = points[triangle[2]];

                writer.write("0\n3DFACE\n8\nLAYER_3D_FACES\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", p1.x/METER_TO_FEET_CONVERSION, p1.y/METER_TO_FEET_CONVERSION, p1.z/METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "11\n%f\n21\n%f\n31\n%f\n", p2.x/METER_TO_FEET_CONVERSION, p2.y/METER_TO_FEET_CONVERSION, p2.z/METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "12\n%f\n22\n%f\n32\n%f\n", p3.x/METER_TO_FEET_CONVERSION, p3.y/METER_TO_FEET_CONVERSION, p3.z/METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "13\n%f\n23\n%f\n33\n%f\n", p3.x/METER_TO_FEET_CONVERSION, p3.y/METER_TO_FEET_CONVERSION, p3.z/METER_TO_FEET_CONVERSION));
            }

            DXFWriteMethods.coda(writer);
        }

        System.out.println("DXF generated: " + path + "/" + filename);
    }

    private void createPolyline(BufferedWriter writer, Point3D start, Point3D end) throws IOException {
        writer.write("0\nPOLYLINE\n8\nLAYER_POLYLINES\n66\n1\n70\n8\n100\nAcDbEntity\n100\nAcDb3dPolyline\n");

        writer.write("0\nVERTEX\n8\nLAYER_POLYLINES\n100\nAcDbEntity\n100\nAcDbVertex\n100\nAcDb3dPolylineVertex\n70\n32\n");
        writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", start.x/METER_TO_FEET_CONVERSION, start.y/METER_TO_FEET_CONVERSION, start.z/METER_TO_FEET_CONVERSION));

        writer.write("0\nVERTEX\n8\nLAYER_POLYLINES\n100\nAcDbEntity\n100\nAcDbVertex\n100\nAcDb3dPolylineVertex\n70\n32\n");
        writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", end.x/METER_TO_FEET_CONVERSION, end.y/METER_TO_FEET_CONVERSION, end.z/METER_TO_FEET_CONVERSION));

        writer.write("0\nSEQEND\n");
    }

    private List<int[]> performDelaunayTriangulation(Point3D[] points) {
        List<int[]> triangles = new ArrayList<>();
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[points.length];

        for (int i = 0; i < points.length; i++) {
            coordinates[i] = new Coordinate(points[i].x, points[i].y, points[i].z);
        }

        // Poligoni di riferimento
        Polygon abcdPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(points[0].x, points[0].y),
                new Coordinate(points[1].x, points[1].y),
                new Coordinate(points[2].x, points[2].y),
                new Coordinate(points[3].x, points[3].y),
                new Coordinate(points[0].x, points[0].y)
        });

        Polygon abefPolygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(points[0].x, points[0].y),
                new Coordinate(points[1].x, points[1].y),
                new Coordinate(points[4].x, points[4].y),
                new Coordinate(points[5].x, points[5].y),
                new Coordinate(points[0].x, points[0].y)
        });

        // Delaunay
        DelaunayTriangulationBuilder dtBuilder = new DelaunayTriangulationBuilder();
        dtBuilder.setSites(geometryFactory.createMultiPointFromCoords(coordinates));
        Geometry trianglesGeom = dtBuilder.getTriangles(geometryFactory);

        for (int i = 0; i < trianglesGeom.getNumGeometries(); i++) {
            Polygon triangle = (Polygon) trianglesGeom.getGeometryN(i);
            Coordinate[] coords = triangle.getCoordinates();

            boolean isInside = abcdPolygon.contains(triangle) || abefPolygon.contains(triangle);

            if (isInside) {
                int[] triIndices = new int[3];
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < points.length; k++) {
                        if (coords[j].equals2D(new Coordinate(points[k].x, points[k].y))) {
                            triIndices[j] = k;
                            break;
                        }
                    }
                }
                triangles.add(triIndices);
            }
        }

        return triangles;
    }
}