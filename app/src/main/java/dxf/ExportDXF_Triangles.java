package dxf;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ExportDXF_Triangles {
    private List<double[]> coordinates; // Lista di coordinate [x, y, z]
    private List<Coordinate> outCoords;
    private Point3D[] point3DS;
    private String filename;
    private String path;
    private double conversionFactor;
    static String label;
    private double METER_TO_FEET_CONVERSION;

    public ExportDXF_Triangles(Point3D[] point3DS, String filename, String path, double conversionFactor) {
        this.point3DS = point3DS;
        this.filename = filename;
        this.path = path;
        this.conversionFactor = conversionFactor;
    }

    public void generateDXF() throws IOException {
        coordinates = new ArrayList<>();
        outCoords = new ArrayList<>();
        METER_TO_FEET_CONVERSION = conversionFactor;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + filename))) {
            DXFWriteMethods.testa(writer, 3);
            DXFWriteMethods.writeLayer(writer, "LAYER_3D_FACES", 2);
            DXFWriteMethods.writeLayer(writer, "LAYER_POLYLINES", 6);
            DXFWriteMethods.writeLayer(writer, "LAYER_POINTS", 4);
            DXFWriteMethods.endLayers(writer);
            DXFWriteMethods.beginEntities(writer);

            int handle = 0;

            for (Point3D point : point3DS) {
                coordinates.add(handle, new double[]{point.getX(), point.getY(), point.getZ()});
                label = point3DS[handle].getName();

                handle++;
                double x = point.getX() / METER_TO_FEET_CONVERSION;
                double y = point.getY() / METER_TO_FEET_CONVERSION;
                double z = point.getZ() / METER_TO_FEET_CONVERSION;

                // POINT
                writer.write("0\nPOINT\n8\nLAYER_POINTS\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));

                // TEXT
                // Aggiungi TEXT per il punto
                writer.write("0\nTEXT\n");
                writer.write("5\n" + Integer.toHexString(handle) + "\n");
                writer.write("100\nAcDbEntity\n");
                writer.write(String.format("8\n%s\n", "LAYER_POINTS"));
                writer.write("100\nAcDbText\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));
                writer.write("40\n0.400000\n41\n1\n");
                writer.write("1\n" + label + "\n");
                writer.write("50\n0\n");
                label = "";
            }

            // POLYLINE di contorno
            if (coordinates.size() > 2) {
                outCoords = calculateOuterBorder(coordinates);
            }


            List<double[]> coordList = Arrays.stream(point3DS)
                    .map(p -> new double[]{p.getX(), p.getY(), p.getZ()})
                    .collect(Collectors.toList());

            List<int[]> triangles = performDelaunayTriangulation(coordList);

            for (int[] triangle : triangles) {
                double[] p1 = coordList.get(triangle[0]);
                double[] p2 = coordList.get(triangle[1]);
                double[] p3 = coordList.get(triangle[2]);

                writer.write("0\n3DFACE\n");
                writer.write("8\nLAYER_3D_FACES\n");
                writer.write("100\nAcDbEntity\n");
                writer.write("100\nAcDbFace\n");

                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", p1[0] / METER_TO_FEET_CONVERSION, p1[1] / METER_TO_FEET_CONVERSION, p1[2] / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "11\n%f\n21\n%f\n31\n%f\n", p2[0] / METER_TO_FEET_CONVERSION, p2[1] / METER_TO_FEET_CONVERSION, p2[2] / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "12\n%f\n22\n%f\n32\n%f\n", p3[0] / METER_TO_FEET_CONVERSION, p3[1] / METER_TO_FEET_CONVERSION, p3[2] / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "13\n%f\n23\n%f\n33\n%f\n", p3[0] / METER_TO_FEET_CONVERSION, p3[1] / METER_TO_FEET_CONVERSION, p3[2] / METER_TO_FEET_CONVERSION)); // chiusura con P3
            }
            createPolylineFromVertices(writer, outCoords);
            DXFWriteMethods.coda(writer);
        }

        System.out.println("DXF file generated successfully at " + path + "/" + filename);
    }

    private void createPolylineFromVertices(BufferedWriter writer, List<Coordinate> vertices) throws IOException {
        writer.write("0\nPOLYLINE\n8\nLAYER_POLYLINES\n66\n1\n70\n8\n10\n0.0\n20\n0.0\n30\n0.0\n");

        for (Coordinate point : vertices) {
            double z = point.z;
            if (Double.isNaN(z)) {
                // Trova z originale dal punto con stessa x,y
                for (double[] original : coordinates) {
                    if (original[0] == point.x && original[1] == point.y) {
                        z = original[2];
                        break;
                    }
                }
            }
            writer.write("0\nVERTEX\n8\nLAYER_POLYLINES\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                    point.x / METER_TO_FEET_CONVERSION,
                    point.y / METER_TO_FEET_CONVERSION,
                    z / METER_TO_FEET_CONVERSION));
            writer.write("70\n32\n");
        }

        writer.write("0\nSEQEND\n");
    }

    private List<int[]> performDelaunayTriangulation(List<double[]> coordinates) {
        List<int[]> triangles = new ArrayList<>();
        GeometryFactory geometryFactory = new GeometryFactory();

        if (coordinates.size() < 3) return triangles;

        Coordinate[] coordsArray = coordinates.stream()
                .map(coord -> new Coordinate(coord[0], coord[1]))
                .toArray(Coordinate[]::new);

        DelaunayTriangulationBuilder dtBuilder = new DelaunayTriangulationBuilder();
        dtBuilder.setSites(geometryFactory.createMultiPointFromCoords(coordsArray));
        Geometry triangulation = dtBuilder.getTriangles(geometryFactory);

        for (int i = 0; i < triangulation.getNumGeometries(); i++) {
            Polygon triangle = (Polygon) triangulation.getGeometryN(i);
            Coordinate[] triangleCoords = triangle.getCoordinates();

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

        return triangles;
    }

    public List<Coordinate> calculateOuterBorder(List<double[]> points) {
        GeometryFactory gf = new GeometryFactory();
        List<Coordinate> inputCoords = new ArrayList<>();

        for (double[] p : points) {
            inputCoords.add(new Coordinate(p[0], p[1], p[2]));
        }

        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(inputCoords);
        Geometry triangles = builder.getTriangles(gf);

        // Mappa per contare i bordi
        Map<String, Integer> edgeCounter = new HashMap<>();

        for (int i = 0; i < triangles.getNumGeometries(); i++) {
            Polygon tri = (Polygon) triangles.getGeometryN(i);
            LineString exterior = tri.getExteriorRing();
            Coordinate[] coords = exterior.getCoordinates();

            for (int j = 0; j < coords.length - 1; j++) {
                Coordinate a = coords[j];
                Coordinate b = coords[j + 1];

                // Normalizza l'ordine dei punti
                String key = edgeKey(a, b);

                edgeCounter.put(key, edgeCounter.getOrDefault(key, 0) + 1);
            }
        }

        // Trova i bordi che appaiono solo una volta → sono quelli esterni
        Set<String> borderEdges = new HashSet<>();
        for (Map.Entry<String, Integer> e : edgeCounter.entrySet()) {
            if (e.getValue() == 1) {
                borderEdges.add(e.getKey());
            }
        }

        // Ricostruisci il bordo ordinato
        List<Coordinate> borderPoints = reconstructOrderedBorder(borderEdges);

        return borderPoints;
    }

    // Funzione di supporto per creare una chiave unica per un bordo
    private String edgeKey(Coordinate a, Coordinate b) {
        if (a.compareTo(b) < 0) {
            return a.x + "," + a.y + "-" + b.x + "," + b.y;
        } else {
            return b.x + "," + b.y + "-" + a.x + "," + a.y;
        }
    }

    // Funzione di supporto per ricostruire il bordo ordinato
    private List<Coordinate> reconstructOrderedBorder(Set<String> borderEdges) {
        Map<String, Coordinate> pointMap = new HashMap<>();
        Map<Coordinate, List<Coordinate>> adjacency = new HashMap<>();

        for (String edge : borderEdges) {
            String[] parts = edge.split("-");
            Coordinate a = parseCoordinate(parts[0]);
            Coordinate b = parseCoordinate(parts[1]);

            adjacency.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
            adjacency.computeIfAbsent(b, k -> new ArrayList<>()).add(a);

            pointMap.put(a.x + "," + a.y, a);
            pointMap.put(b.x + "," + b.y, b);
        }

        // Parti da un punto qualsiasi
        Coordinate start = adjacency.keySet().iterator().next();
        List<Coordinate> ordered = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();

        Coordinate current = start;
        ordered.add(current);

        while (true) {
            List<Coordinate> neighbors = adjacency.get(current);
            Coordinate next = null;
            for (Coordinate n : neighbors) {
                String key = edgeKey(current, n);
                if (!visitedEdges.contains(key)) {
                    next = n;
                    visitedEdges.add(key);
                    break;
                }
            }
            if (next == null) break;
            ordered.add(next);
            current = next;
        }

        return ordered;
    }

    // Parsing da stringa a Coordinate
    private Coordinate parseCoordinate(String text) {
        String[] parts = text.split(",");
        return new Coordinate(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

}


