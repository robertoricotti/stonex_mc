package dxf;

import static gui.projects.Activity_Crea_Superficie.facceTrench;
import static gui.projects.Activity_Crea_Superficie.polyTrench;
import static gui.projects.Canvas_Crea_Superficie.buildTrenchEntities;

import android.graphics.Color;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ExportDXF_Trench {
    private List<double[]> coordinates;
    private Point3D[] centerLinePoints;
    private double leftWidth;
    private double rightWidth;
    private double leftSlopeDeg;
    private double rightSlopeDeg;
    int handle;

    static Layer trenchLayer, polylineLayer;
    private String filename;
    private String path;
    private double conversionFactor;
    private double METER_TO_FEET_CONVERSION;
    static String label;

    public ExportDXF_Trench(Point3D[] centerLinePoints,
                            double leftWidth, double rightWidth,
                            double leftSlopeDeg, double rightSlopeDeg,
                            String filename, String path,
                            double conversionFactor) {
        this.centerLinePoints = centerLinePoints;
        this.leftWidth = leftWidth;
        this.rightWidth = rightWidth;
        this.leftSlopeDeg = -leftSlopeDeg;
        this.rightSlopeDeg = -rightSlopeDeg;

        this.filename = filename;
        this.path = path;
        this.conversionFactor = conversionFactor;
        trenchLayer = new Layer(filename, "TRENCH", Color.YELLOW, true);
        polylineLayer = new Layer(filename, "CENTER LINE", Color.MAGENTA, true);
    }

    public void generateDXF() throws IOException {
        METER_TO_FEET_CONVERSION = conversionFactor;
        coordinates = new ArrayList<>();

        List<Point3D> centerLineList = Arrays.asList(centerLinePoints);
        buildTrenchEntities(
                centerLineList,
                leftWidth, rightWidth,
                leftSlopeDeg, rightSlopeDeg,
                Color.YELLOW, Color.MAGENTA,
                trenchLayer, polylineLayer


        );

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + filename))) {
            DXFWriteMethods.testa(writer, 3);
            DXFWriteMethods.writeLayer(writer, trenchLayer.getLayerName(), 2);
            DXFWriteMethods.writeLayer(writer, polylineLayer.getLayerName(), 6);
            DXFWriteMethods.writeLayer(writer, "LAYER_POINTS", 4);
            DXFWriteMethods.endLayers(writer);
            DXFWriteMethods.beginEntities(writer);


            handle = 0;

            for (Point3D point : centerLinePoints) {
                coordinates.add(handle, new double[]{point.getX(), point.getY(), point.getZ()});
                label = centerLinePoints[handle].getId();

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


            for (Face3D face : facceTrench) {
                Point3D p1 = face.getP1();
                Point3D p2 = face.getP2();
                Point3D p3 = face.getP3();
                Point3D p4 = face.getP4();

                writer.write("0\n3DFACE\n8\n" + face.getLayer().getLayerName() + "\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", p1.x / METER_TO_FEET_CONVERSION, p1.y / METER_TO_FEET_CONVERSION, p1.z / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "11\n%f\n21\n%f\n31\n%f\n", p2.x / METER_TO_FEET_CONVERSION, p2.y / METER_TO_FEET_CONVERSION, p2.z / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "12\n%f\n22\n%f\n32\n%f\n", p3.x / METER_TO_FEET_CONVERSION, p3.y / METER_TO_FEET_CONVERSION, p3.z / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "13\n%f\n23\n%f\n33\n%f\n", p4.x / METER_TO_FEET_CONVERSION, p4.y / METER_TO_FEET_CONVERSION, p4.z / METER_TO_FEET_CONVERSION));
            }

            List<Point3D> vertices = polyTrench.getVertices();
            if (vertices.size() >= 2) {
                writer.write("0\nPOLYLINE\n8\n" + polyTrench.getLayer().getLayerName() + "\n66\n1\n70\n8\n100\nAcDbEntity\n100\nAcDb3dPolyline\n");
                for (Point3D pt : vertices) {
                    writer.write("0\nVERTEX\n8\n" + polyTrench.getLayer().getLayerName() + "\n100\nAcDbEntity\n100\nAcDbVertex\n100\nAcDb3dPolylineVertex\n70\n32\n");
                    writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", pt.x / METER_TO_FEET_CONVERSION, pt.y / METER_TO_FEET_CONVERSION, pt.z / METER_TO_FEET_CONVERSION));
                }
                writer.write("0\nSEQEND\n");
            }

            DXFWriteMethods.coda(writer);
        }

        System.out.println("DXF generated: " + path + "/" + filename);
    }
}
