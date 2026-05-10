package dxf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import utils.Utils;

/**
 * DXF exporter for DITCH surfaces.
 *
 * Topology:
 * - centerline P1..P6
 * - left edge L1..L6
 * - right edge R1..R6
 * - transverse polylines Li-Pi-Ri
 * - outer closed border L1..L6-R6..R1-L1
 *
 * Degenerate centerline segments (L = 0) are kept in point/polyline output,
 * but their zero-area 3DFACE entities are skipped.
 */
public class ExportDXF_Ditch {
    private static final String LAYER_FACES = "LAYER_3D_FACES";
    private static final String LAYER_POLYLINES = "LAYER_POLYLINES";
    private static final String LAYER_POINTS = "LAYER_POINTS";
    private static final double EPS = 1e-9;

    private final Point3D[] center;
    private final Point3D[] left;
    private final Point3D[] right;
    private final String filename;
    private final String path;
    private final double conversionFactor;

    private double meterToOutput;
    private int handle = 1;

    public ExportDXF_Ditch(Point3D[] center,
                           Point3D[] left,
                           Point3D[] right,
                           String filename,
                           String path,
                           double conversionFactor) {
        this.center = center;
        this.left = left;
        this.right = right;
        this.filename = filename;
        this.path = path;
        this.conversionFactor = conversionFactor;
    }

    public void generateDXF() throws IOException {
        meterToOutput = conversionFactor == 0.0 ? 1.0 : conversionFactor;
        validate();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + filename))) {
            DXFWriteMethods.testa(writer, 3);
            DXFWriteMethods.writeLayer(writer, LAYER_FACES, 2);
            DXFWriteMethods.writeLayer(writer, LAYER_POLYLINES, 6);
            DXFWriteMethods.writeLayer(writer, LAYER_POINTS, 4);
            DXFWriteMethods.endLayers(writer);
            DXFWriteMethods.beginEntities(writer);

            writePointsAndTexts(writer, center);
            writePointsAndTexts(writer, left);
            writePointsAndTexts(writer, right);

            writeFaces(writer);

            writePolyline(writer, asList(center));
            writePolyline(writer, asList(left));
            writePolyline(writer, asList(right));

            for (int i = 0; i < center.length; i++) {
                ArrayList<Point3D> cross = new ArrayList<>();
                cross.add(left[i]);
                cross.add(center[i]);
                cross.add(right[i]);
                writePolyline(writer, cross);
            }

            ArrayList<Point3D> outer = new ArrayList<>();
            for (Point3D p : left) outer.add(p);
            for (int i = right.length - 1; i >= 0; i--) outer.add(right[i]);
            outer.add(left[0]);
            writePolyline(writer, outer);

            DXFWriteMethods.coda(writer);
        }
    }

    private void validate() throws IOException {
        if (center == null || left == null || right == null
                || center.length != 6 || left.length != 6 || right.length != 6) {
            throw new IOException("DITCH requires exactly 6 center, 6 left and 6 right points");
        }
        for (int i = 0; i < 6; i++) {
            if (center[i] == null || left[i] == null || right[i] == null) {
                throw new IOException("DITCH contains null points");
            }
        }
    }

    private List<Point3D> asList(Point3D[] points) {
        ArrayList<Point3D> out = new ArrayList<>();
        for (Point3D p : points) out.add(p);
        return out;
    }

    private void writePointsAndTexts(BufferedWriter writer, Point3D[] points) throws IOException {
        for (int i = 0; i < points.length; i++) {
            Point3D p = points[i];
            String label = safeLabel(p, i);
            double x = out(p.getX());
            double y = out(p.getY());
            double z = out(p.getZ());

            writer.write("0\nPOINT\n8\n" + LAYER_POINTS + "\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));

            writer.write("0\nTEXT\n5\n" + Integer.toHexString(handle++)
                    + "\n100\nAcDbEntity\n8\n" + LAYER_POINTS + "\n100\nAcDbText\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));
            writer.write("40\n0.4\n41\n1\n");
            writer.write("1\n " + label + "  " + Utils.readUnitOfMeasureLITE(String.valueOf(p.getZ())) + "\n");
            writer.write("50\n0\n");
        }
    }

    private String safeLabel(Point3D p, int fallbackIndex) {
        String label = p.getName();
        if (label == null || label.trim().isEmpty()) label = p.getId();
        if (label == null || label.trim().isEmpty()) label = "P" + (fallbackIndex + 1);
        return label;
    }

    private void writeFaces(BufferedWriter writer) throws IOException {
        for (int i = 0; i < center.length - 1; i++) {
            Point3D c1 = center[i];
            Point3D c2 = center[i + 1];
            if (Math.hypot(c2.getX() - c1.getX(), c2.getY() - c1.getY()) <= EPS) {
                continue;
            }

            Point3D l1 = left[i];
            Point3D l2 = left[i + 1];
            Point3D r1 = right[i];
            Point3D r2 = right[i + 1];

            writeTriangle(writer, c1, c2, l2);
            writeTriangle(writer, c1, l2, l1);
            writeTriangle(writer, c1, r1, r2);
            writeTriangle(writer, c1, r2, c2);
        }
    }

    private void writeTriangle(BufferedWriter writer, Point3D p1, Point3D p2, Point3D p3) throws IOException {
        writer.write("0\n3DFACE\n8\n" + LAYER_FACES + "\n");
        writeFacePoint(writer, "10", "20", "30", p1);
        writeFacePoint(writer, "11", "21", "31", p2);
        writeFacePoint(writer, "12", "22", "32", p3);
        writeFacePoint(writer, "13", "23", "33", p3);
    }

    private void writeFacePoint(BufferedWriter writer, String gx, String gy, String gz, Point3D p) throws IOException {
        writer.write(String.format(Locale.US, "%s\n%f\n%s\n%f\n%s\n%f\n",
                gx, out(p.getX()), gy, out(p.getY()), gz, out(p.getZ())));
    }

    private void writePolyline(BufferedWriter writer, List<Point3D> points) throws IOException {
        if (points == null || points.size() < 2) return;

        writer.write("0\nPOLYLINE\n8\n" + LAYER_POLYLINES
                + "\n66\n1\n70\n8\n100\nAcDbEntity\n100\nAcDb3dPolyline\n");
        for (Point3D p : points) {
            writer.write("0\nVERTEX\n8\n" + LAYER_POLYLINES
                    + "\n100\nAcDbEntity\n100\nAcDbVertex\n100\nAcDb3dPolylineVertex\n70\n32\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                    out(p.getX()), out(p.getY()), out(p.getZ())));
        }
        writer.write("0\nSEQEND\n");
    }

    private double out(double meters) {
        return meters / meterToOutput;
    }
}
