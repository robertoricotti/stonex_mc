package dxf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dxf.DXFWriteMethods;
import dxf.Point3D;
import utils.Utils;

/**
 * DXF exporter for TRENCH / corridor surfaces.
 *
 * This exporter is intentionally self-contained: it does NOT call
 * Canvas_Crea_Superficie.buildTrenchEntities() and does NOT read
 * Activity_Crea_Superficie.facceTrench/polyTrench.
 *
 * Input centerLinePoints must already contain the desired centerline Z values.
 * If the UI is in "flat" mode, pass the flattened/interpolated centerline points
 * from the controller, e.g. controller.getTrenchExportPoints().
 *
 * Slope convention:
 *   positive slope = rises outward from the centerline
 *   negative slope = falls outward from the centerline
 */
public class ExportDXF_Trench {
    private static final String LAYER_FACES = "LAYER_3D_FACES";
    private static final String LAYER_POLYLINES = "LAYER_POLYLINES";
    private static final String LAYER_POINTS = "LAYER_POINTS";
    private static final double EPS = 1e-9;
    private static final double MAX_MITER_FACTOR = 3.0;

    private final Point3D[] centerLinePoints;
    private final double leftWidth;
    private final double rightWidth;
    private final double leftSlopeDeg;
    private final double rightSlopeDeg;
    private final String filename;
    private final String path;
    private final double conversionFactor;

    private double meterToOutput;
    private int handle = 1;

    public ExportDXF_Trench(Point3D[] centerLinePoints,
                            double leftWidth,
                            double rightWidth,
                            double leftSlopeDeg,
                            double rightSlopeDeg,
                            String filename,
                            String path,
                            double conversionFactor) {
        this.centerLinePoints = centerLinePoints;
        this.leftWidth = Math.max(0.0, leftWidth);
        this.rightWidth = Math.max(0.0, rightWidth);
        this.leftSlopeDeg = leftSlopeDeg;
        this.rightSlopeDeg = rightSlopeDeg;
        this.filename = filename;
        this.path = path;
        this.conversionFactor = conversionFactor;
    }

    public void generateDXF() throws IOException {
        meterToOutput = conversionFactor == 0.0 ? 1.0 : conversionFactor;

        List<Point3D> center = sanitizeCenterline(centerLinePoints);
        if (center.size() < 2) {
            throw new IOException("TRENCH requires at least 2 centerline points");
        }

        Corridor corridor = buildCorridor(center);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + filename))) {
            DXFWriteMethods.testa(writer, 3);
            DXFWriteMethods.writeLayer(writer, LAYER_FACES, 2);     // yellow
            DXFWriteMethods.writeLayer(writer, LAYER_POLYLINES, 6); // magenta
            DXFWriteMethods.writeLayer(writer, LAYER_POINTS, 4);    // cyan
            DXFWriteMethods.endLayers(writer);
            DXFWriteMethods.beginEntities(writer);

            writeCenterPointsAndTexts(writer, corridor.center);

            // Main geometry: left side and right side corridor meshes.
            writeCorridorFaces(writer, corridor);

            // Polylines: centerline + left edge + right edge.
            writePolyline(writer, corridor.center, LAYER_POLYLINES);
            writePolyline(writer, corridor.left, LAYER_POLYLINES);
            writePolyline(writer, corridor.right, LAYER_POLYLINES);

            DXFWriteMethods.coda(writer);
        }
    }

    private List<Point3D> sanitizeCenterline(Point3D[] points) {
        List<Point3D> out = new ArrayList<>();
        if (points == null) return out;

        for (int i = 0; i < points.length; i++) {
            Point3D p = points[i];
            if (p == null) continue;

            String id = safeLabel(p, i);
            out.add(new Point3D(id, p.getX(), p.getY(), p.getZ(), id));
        }
        return out;
    }

    private Corridor buildCorridor(List<Point3D> centerInput) {
        List<Point3D> center = new ArrayList<>();
        List<Point3D> left = new ArrayList<>();
        List<Point3D> right = new ArrayList<>();

        double leftDz = Math.tan(Math.toRadians(leftSlopeDeg)) * leftWidth;
        double rightDz = Math.tan(Math.toRadians(rightSlopeDeg)) * rightWidth;

        for (int i = 0; i < centerInput.size(); i++) {
            Point3D c = centerInput.get(i);
            center.add(new Point3D("P" + (i + 1), c.getX(), c.getY(), c.getZ(), "P" + (i + 1)));

            OffsetBasis basis = offsetBasisAt(centerInput, i);

            left.add(new Point3D(
                    "L" + (i + 1),
                    c.getX() + basis.leftX * basis.leftScale,
                    c.getY() + basis.leftY * basis.leftScale,
                    c.getZ() + leftDz,
                    "L" + (i + 1)
            ));

            right.add(new Point3D(
                    "R" + (i + 1),
                    c.getX() - basis.rightX * basis.rightScale,
                    c.getY() - basis.rightY * basis.rightScale,
                    c.getZ() + rightDz,
                    "R" + (i + 1)
            ));
        }

        return new Corridor(center, left, right);
    }

    /**
     * Miter-limited offset basis at a centerline vertex.
     * The vector points to the left side of local travel direction.
     */
    private OffsetBasis offsetBasisAt(List<Point3D> pts, int i) {
        int n = pts.size();

        if (n < 2) {
            return new OffsetBasis(0, 1, leftWidth, 0, 1, rightWidth);
        }

        double[] tPrev;
        double[] tNext;

        if (i == 0) {
            tPrev = tangent(pts.get(0), pts.get(1));
            tNext = tPrev;
        } else if (i == n - 1) {
            tPrev = tangent(pts.get(n - 2), pts.get(n - 1));
            tNext = tPrev;
        } else {
            tPrev = tangent(pts.get(i - 1), pts.get(i));
            tNext = tangent(pts.get(i), pts.get(i + 1));
        }

        double[] nPrev = leftNormal(tPrev);
        double[] nNext = leftNormal(tNext);

        double mx = nPrev[0] + nNext[0];
        double my = nPrev[1] + nNext[1];
        double mLen = Math.hypot(mx, my);

        if (mLen < EPS) {
            // Hairpin / 180° turn: fallback to current segment normal.
            mx = nNext[0];
            my = nNext[1];
            mLen = Math.hypot(mx, my);
        }

        mx /= mLen;
        my /= mLen;

        double denom = mx * nPrev[0] + my * nPrev[1];
        double leftScale = miterScale(leftWidth, denom);
        double rightScale = miterScale(rightWidth, denom);

        return new OffsetBasis(mx, my, leftScale, mx, my, rightScale);
    }

    private double miterScale(double width, double denom) {
        if (width <= EPS) return 0.0;
        if (Math.abs(denom) < 0.25) return width;

        double scale = width / denom;
        double max = width * MAX_MITER_FACTOR;
        if (scale > max) scale = max;
        if (scale < -max) scale = -max;
        return scale;
    }

    private double[] tangent(Point3D a, Point3D b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double len = Math.hypot(dx, dy);
        if (len < EPS) return new double[]{0.0, 1.0};
        return new double[]{dx / len, dy / len};
    }

    private double[] leftNormal(double[] t) {
        return new double[]{-t[1], t[0]};
    }

    private void writeCenterPointsAndTexts(BufferedWriter writer, List<Point3D> center) throws IOException {
        for (int i = 0; i < center.size(); i++) {
            Point3D p = center.get(i);
            String label = safeLabel(p, i);

            writePoint(writer, p, LAYER_POINTS);
            writeText(writer, p, LAYER_POINTS,
                    " " + label + "  " + Utils.readUnitOfMeasureLITE(String.valueOf(p.getZ())));
        }
    }

    private void writeCorridorFaces(BufferedWriter writer, Corridor corridor) throws IOException {
        for (int i = 0; i < corridor.center.size() - 1; i++) {
            Point3D c0 = corridor.center.get(i);
            Point3D c1 = corridor.center.get(i + 1);
            Point3D l0 = corridor.left.get(i);
            Point3D l1 = corridor.left.get(i + 1);
            Point3D r0 = corridor.right.get(i);
            Point3D r1 = corridor.right.get(i + 1);

            // Left side: c0-c1-l1-l0 as two 3DFACEs.
            write3dFace(writer, c0, c1, l1, l1, LAYER_FACES);
            write3dFace(writer, c0, l1, l0, l0, LAYER_FACES);

            // Right side: c0-r1-c1 and c0-r0-r1.
            write3dFace(writer, c0, r1, c1, c1, LAYER_FACES);
            write3dFace(writer, c0, r0, r1, r1, LAYER_FACES);
        }
    }

    private void writePoint(BufferedWriter writer, Point3D p, String layer) throws IOException {
        writer.write("0\nPOINT\n8\n" + layer + "\n");
        writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                p.getX() / meterToOutput,
                p.getY() / meterToOutput,
                p.getZ() / meterToOutput));
    }

    private void writeText(BufferedWriter writer, Point3D p, String layer, String text) throws IOException {
        writer.write("0\nTEXT\n");
        writer.write("5\n" + Integer.toHexString(handle++) + "\n");
        writer.write("100\nAcDbEntity\n");
        writer.write("8\n" + layer + "\n");
        writer.write("100\nAcDbText\n");
        writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                p.getX() / meterToOutput,
                p.getY() / meterToOutput,
                p.getZ() / meterToOutput));
        writer.write("40\n0.400000\n41\n1\n");
        writer.write("1\n" + text + "\n");
        writer.write("50\n0\n");
    }

    private void write3dFace(BufferedWriter writer,
                             Point3D p1, Point3D p2, Point3D p3, Point3D p4,
                             String layer) throws IOException {
        writer.write("0\n3DFACE\n8\n" + layer + "\n");
        writer.write("100\nAcDbEntity\n100\nAcDbFace\n");
        writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                p1.getX() / meterToOutput, p1.getY() / meterToOutput, p1.getZ() / meterToOutput));
        writer.write(String.format(Locale.US, "11\n%f\n21\n%f\n31\n%f\n",
                p2.getX() / meterToOutput, p2.getY() / meterToOutput, p2.getZ() / meterToOutput));
        writer.write(String.format(Locale.US, "12\n%f\n22\n%f\n32\n%f\n",
                p3.getX() / meterToOutput, p3.getY() / meterToOutput, p3.getZ() / meterToOutput));
        writer.write(String.format(Locale.US, "13\n%f\n23\n%f\n33\n%f\n",
                p4.getX() / meterToOutput, p4.getY() / meterToOutput, p4.getZ() / meterToOutput));
    }

    private void writePolyline(BufferedWriter writer, List<Point3D> vertices, String layer) throws IOException {
        if (vertices == null || vertices.size() < 2) return;

        writer.write("0\nPOLYLINE\n8\n" + layer + "\n66\n1\n70\n8\n100\nAcDbEntity\n100\nAcDb3dPolyline\n");
        for (Point3D pt : vertices) {
            writer.write("0\nVERTEX\n8\n" + layer + "\n100\nAcDbEntity\n100\nAcDbVertex\n100\nAcDb3dPolylineVertex\n70\n32\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n",
                    pt.getX() / meterToOutput,
                    pt.getY() / meterToOutput,
                    pt.getZ() / meterToOutput));
        }
        writer.write("0\nSEQEND\n");
    }

    private String safeLabel(Point3D p, int index) {
        if (p == null) return "P" + (index + 1);

        String id = p.getId();
        if (id != null && !id.trim().isEmpty()) return id.trim();

        String name = p.getName();
        if (name != null && !name.trim().isEmpty()) return name.trim();

        return "P" + (index + 1);
    }

    private static class Corridor {
        final List<Point3D> center;
        final List<Point3D> left;
        final List<Point3D> right;

        Corridor(List<Point3D> center, List<Point3D> left, List<Point3D> right) {
            this.center = center;
            this.left = left;
            this.right = right;
        }
    }

    private static class OffsetBasis {
        final double leftX;
        final double leftY;
        final double leftScale;
        final double rightX;
        final double rightY;
        final double rightScale;

        OffsetBasis(double leftX, double leftY, double leftScale,
                    double rightX, double rightY, double rightScale) {
            this.leftX = leftX;
            this.leftY = leftY;
            this.leftScale = leftScale;
            this.rightX = rightX;
            this.rightY = rightY;
            this.rightScale = rightScale;
        }
    }
}
