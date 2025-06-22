package dxf;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import utils.Utils;

public class ExportDXF_1P {
    private double[] centerPoint;
    private double sideLength;
    private String filename;
    private String path;
    private boolean useFeet;  // nuovo parametro per specificare l'uso di piedi
    private double METER_TO_FEET_CONVERSION;  // Fattore di conversione metri -> piedi

    public ExportDXF_1P(double[] centerPoint, double sideLength, String filename, String path, boolean useFeet) {
        this.centerPoint = centerPoint;
        this.sideLength = sideLength;
        this.filename = filename;
        this.path = path;
        this.useFeet = useFeet;
    }

    public void generateDXF() throws IOException {
        METER_TO_FEET_CONVERSION = useFeet ? 0.3048006096 : 1.0;
        double halfSide = sideLength / 2;
        double z = centerPoint[2];

        // Vertici del quadrato
        double[][] vertices = {
                {centerPoint[0] - halfSide, centerPoint[1] - halfSide, z},
                {centerPoint[0] + halfSide, centerPoint[1] - halfSide, z},
                {centerPoint[0] + halfSide, centerPoint[1] + halfSide, z},
                {centerPoint[0] - halfSide, centerPoint[1] + halfSide, z}
        };

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + filename))) {
            DXFWriteMethods.testa(writer, 3);
            DXFWriteMethods.writeLayer(writer, "LAYER_3D_FACES", 2);
            DXFWriteMethods.writeLayer(writer, "LAYER_POLYLINES", 6);
            DXFWriteMethods.writeLayer(writer, "LAYER_POINTS", 4);
            DXFWriteMethods.endLayers(writer);
            DXFWriteMethods.beginEntities(writer);


            // 3DFACE triangoli attorno al centro
            for (int i = 0; i < vertices.length; i++) {
                double[] p1 = vertices[i];
                double[] p2 = vertices[(i + 1) % vertices.length];
                double[] p3 = centerPoint;

                writer.write("0\n3DFACE\n8\nLAYER_3D_FACES\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", p1[0] / METER_TO_FEET_CONVERSION, p1[1] / METER_TO_FEET_CONVERSION, p1[2] / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "11\n%f\n21\n%f\n31\n%f\n", p2[0] / METER_TO_FEET_CONVERSION, p2[1] / METER_TO_FEET_CONVERSION, p2[2] / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "12\n%f\n22\n%f\n32\n%f\n", p3[0] / METER_TO_FEET_CONVERSION, p3[1] / METER_TO_FEET_CONVERSION, p3[2] / METER_TO_FEET_CONVERSION));
                writer.write(String.format(Locale.US, "13\n%f\n23\n%f\n33\n%f\n", p3[0] / METER_TO_FEET_CONVERSION, p3[1] / METER_TO_FEET_CONVERSION, p3[2] / METER_TO_FEET_CONVERSION));
            }
            int handle = 0;
            // POINT + TEXT per vertici
            for (double[] vertex : vertices) {
                handle++;
                writer.write("0\nPOINT\n8\nLAYER_POINTS\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", vertex[0] / METER_TO_FEET_CONVERSION, vertex[1] / METER_TO_FEET_CONVERSION, vertex[2] / METER_TO_FEET_CONVERSION));

                writer.write("0\nTEXT\n5\n" + Integer.toHexString(handle) + "\n100\nAcDbEntity\n8\nLAYER_POINTS\n100\nAcDbText\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", vertex[0] / METER_TO_FEET_CONVERSION, vertex[1] / METER_TO_FEET_CONVERSION, vertex[2] / METER_TO_FEET_CONVERSION));
                writer.write("40\n0.4\n41\n1\n");
                writer.write("1\n" + Utils.readUnitOfMeasureLITE(String.valueOf(vertex[2])) + "\n");
                writer.write("50\n0\n");
            }

            // POINT + TEXT per il centro
            writer.write("0\nPOINT\n8\nLAYER_POINTS\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", centerPoint[0] / METER_TO_FEET_CONVERSION, centerPoint[1] / METER_TO_FEET_CONVERSION, centerPoint[2] / METER_TO_FEET_CONVERSION));

            writer.write("0\nTEXT\n5\n0\n100\nAcDbEntity\n8\nLAYER_POINTS\n100\nAcDbText\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", centerPoint[0] / METER_TO_FEET_CONVERSION, centerPoint[1] / METER_TO_FEET_CONVERSION, centerPoint[2] / METER_TO_FEET_CONVERSION));
            writer.write("40\n0.4\n41\n1\n");
            writer.write("1\nP: " + Utils.readUnitOfMeasureLITE(String.valueOf(centerPoint[2])) + "\n");
            writer.write("50\n0\n");

            // 4 POLYLINE 3D (una per lato)
            for (int i = 0; i < vertices.length; i++) {
                double[] start = vertices[i];
                double[] end = vertices[(i + 1) % vertices.length];

                writer.write("0\nPOLYLINE\n8\nLAYER_POLYLINES\n66\n1\n70\n8\n");
                writer.write("100\nAcDbEntity\n100\nAcDb3dPolyline\n");

                // Primo vertice
                writer.write("0\nVERTEX\n8\nLAYER_POLYLINES\n100\nAcDbEntity\n100\nAcDbVertex\n100\nAcDb3dPolylineVertex\n70\n32\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                        start[0] / METER_TO_FEET_CONVERSION,
                        start[1] / METER_TO_FEET_CONVERSION,
                        start[2] / METER_TO_FEET_CONVERSION));

                // Secondo vertice
                writer.write("0\nVERTEX\n8\nLAYER_POLYLINES\n100\nAcDbEntity\n100\nAcDbVertex\n100\nAcDb3dPolylineVertex\n70\n32\n");
                writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                        end[0] / METER_TO_FEET_CONVERSION,
                        end[1] / METER_TO_FEET_CONVERSION,
                        end[2] / METER_TO_FEET_CONVERSION));

                writer.write("0\nSEQEND\n");
            }

            // FINE FILE
            DXFWriteMethods.coda(writer);
        }

        Log.d("DXFGenerator", "DXF file generated successfully at " + path + "/" + filename);
    }
}
