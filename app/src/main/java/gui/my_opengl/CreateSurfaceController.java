package gui.my_opengl;

import android.graphics.Color;
import android.util.Log;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import dxf.Polyline;
import gui.draw_class.MyColorClass;
import gui.projects.Activity_Crea_Superficie;
import gui.projects.Dialog_Trench;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;

/**
 * Controller for live OpenGL surface creation.
 *
 * IMPORTANT INVARIANTS:
 * - PLAN / AREA / TRIANGLES / TRENCH: DataSaved.points_Create is the live list of picked user points.
 * - TRIANGLES: DataSaved.points_Create is never cleared/replaced during rebuild; Delaunay is derived from it.
 * - AB: first two points are A/B user points; after rebuild the preview publishes A-F in points_Create.
 * - No Create workflow code ever modifies DataSaved.glL_AnchorView.
 */
public class CreateSurfaceController {
    public static final int MODE_PLAN = 0;
    public static final int MODE_AB = 1;
    public static final int MODE_AREA = 2;
    public static final int MODE_TRENCH = 3;
    public static final int MODE_TRIANGLES = 4;

    private static final String TAG = "CreateSurface";
    private static final double DEFAULT_SIDE = 20.0;
    private static final double DEFAULT_AB_WIDTH = 20.0;
    private static final double EPS = 1e-9;
    private static final double INDEX_TOLERANCE = 0.01; // 1 cm, for JTS coordinate matching on large UTM values.

    private static CreateSurfaceController activeController;

    public static CreateSurfaceController getActiveController() {
        return activeController;
    }

    private final int mode;
    private final String projectType;

    private Layer faceLayer;
    private Layer polyLayer;
    private Layer pointLayer;

    private double planSide = DEFAULT_SIDE;
    private double abLeftWidth = DEFAULT_AB_WIDTH;
    private double abRightWidth = DEFAULT_AB_WIDTH;
    private double abLeftSlopeDeg = 0.0;
    private double abRightSlopeDeg = 0.0;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void resetCreateData() {
        DataSaved.points_Create = new ArrayList<>();
        DataSaved.polylines_Create = new ArrayList<>();
        DataSaved.dxfFaces_Create = new ArrayList<>();
        DataSaved.dxfTexts_Create = new ArrayList();
        DataSaved.puntiProgetto = null;
        activeController = null;

        Activity_Crea_Superficie.puntiAB = new Point3D[6];
        Activity_Crea_Superficie.coordinateP = new ArrayList<>();
        Activity_Crea_Superficie.point3DS = new Point3D[0];
        Activity_Crea_Superficie.facceTrench = new ArrayList<>();
        Activity_Crea_Superficie.polyTrench = new Polyline();
        Activity_Crea_Superficie.countPunti = 0;
        Activity_Crea_Superficie.indexSel = 0;
        Activity_Crea_Superficie.leftDIST = DEFAULT_AB_WIDTH;
        Activity_Crea_Superficie.rightDIST = DEFAULT_AB_WIDTH;
        Activity_Crea_Superficie.leftSLOPE = 0.0;
        Activity_Crea_Superficie.rightSLOPE = 0.0;
        Activity_Crea_Superficie.distAB = 0.0;
        Activity_Crea_Superficie.slopeAB = 0.0;
    }

    public CreateSurfaceController(String projectType) {
        this.projectType = projectType == null ? "PLAN" : projectType;
        this.mode = modeFromProject(this.projectType);
        activeController = this;
        initLayers();
        ensureCreateLists();
        initLegacyDefaultsForMode();
        rebuildPreview();
    }

    public int getMode() {
        return mode;
    }

    public String getProjectType() {
        return projectType;
    }

    public int getPickedCount() {
        ensureCreateLists();
        return mode == MODE_AB ? getABBaseCount() : DataSaved.points_Create.size();
    }

    public List<Point3D> getPickedPoints() {
        ensureCreateLists();
        return DataSaved.points_Create;
    }

    public Point3D[] pickedArray() {
        ensureCreateLists();
        if (mode == MODE_AB) {
            Point3D[] ab = getABPoints();
            ArrayList<Point3D> out = new ArrayList<>();
            if (ab[0] != null) out.add(ab[0]);
            if (ab[1] != null) out.add(ab[1]);
            return out.toArray(new Point3D[0]);
        }
        return DataSaved.points_Create.toArray(new Point3D[0]);
    }

    public double getPlanSide() {
        return planSide;
    }

    public void setPlanSide(double planSide) {
        if (planSide > 0) {
            this.planSide = planSide;
            rebuildPreview();
        }
    }

    public void setABParams(double leftWidth, double leftSlopeDeg, double rightWidth, double rightSlopeDeg) {
        abLeftWidth = Math.max(0.0, leftWidth);
        abLeftSlopeDeg = leftSlopeDeg;
        abRightWidth = Math.max(0.0, rightWidth);
        abRightSlopeDeg = rightSlopeDeg;
        rebuildPreview();
    }

    public void prepareABDialog() {
        rebuildPreview();
        syncLegacyStatics();
    }

    public void syncABParamsFromLegacy() {
        abLeftWidth = Activity_Crea_Superficie.leftDIST;
        abLeftSlopeDeg = Activity_Crea_Superficie.leftSLOPE;
        abRightWidth = Activity_Crea_Superficie.rightDIST;
        abRightSlopeDeg = Activity_Crea_Superficie.rightSLOPE;

        if (Activity_Crea_Superficie.puntiAB != null
                && Activity_Crea_Superficie.puntiAB.length >= 2
                && Activity_Crea_Superficie.puntiAB[0] != null
                && Activity_Crea_Superficie.puntiAB[1] != null) {
            ensureCreateLists();
            DataSaved.points_Create.clear();
            DataSaved.points_Create.add(cloneWithName(Activity_Crea_Superficie.puntiAB[0], "A"));
            DataSaved.points_Create.add(cloneWithName(Activity_Crea_Superficie.puntiAB[1], "B"));
        }
        rebuildPreview();
    }

    public void syncTrenchParamsFromLegacy() {
        rebuildPreview();
    }

    public boolean canAddPoint() {
        ensureCreateLists();
        switch (mode) {
            case MODE_PLAN:
                return DataSaved.points_Create.size() < 1;
            case MODE_AB:
                return getABBaseCount() < 2;
            default:
                return true;
        }
    }

    public boolean addCurrentMachinePoint() {
        return addMachinePoint(currentEdgeCoord());
    }

    public boolean addMachinePoint(double[] selectedCoord) {
        ensureCreateLists();
        if (!canAddPoint()) return false;

        if (selectedCoord == null || selectedCoord.length < 3) {
            Log.w(TAG, "selected coordinate not ready");
            return false;
        }

        Point3D p = makePoint(
                "P" + (DataSaved.points_Create.size() + 1),
                selectedCoord[0], selectedCoord[1], selectedCoord[2]
        );

        switch (mode) {
            case MODE_PLAN:
                DataSaved.points_Create.clear();
                DataSaved.points_Create.add(cloneWithName(p, "P"));
                break;

            case MODE_AB:
                if (getABBaseCount() == 0) {
                    DataSaved.points_Create.clear();
                    DataSaved.points_Create.add(cloneWithName(p, "A"));
                } else if (getABBaseCount() == 1) {
                    Point3D a = cloneWithName(DataSaved.points_Create.get(0), "A");
                    DataSaved.points_Create.clear();
                    DataSaved.points_Create.add(a);
                    DataSaved.points_Create.add(cloneWithName(p, "B"));
                }
                break;

            case MODE_AREA:
                if (!DataSaved.points_Create.isEmpty()) {
                    Point3D first = DataSaved.points_Create.get(0);
                    p = makePoint(
                            "P" + (DataSaved.points_Create.size() + 1),
                            selectedCoord[0], selectedCoord[1], first.getZ()
                    );
                }
                DataSaved.points_Create.add(cloneWithName(p, "P" + (DataSaved.points_Create.size() + 1)));
                break;

            case MODE_TRENCH:
            case MODE_TRIANGLES:
            default:
                DataSaved.points_Create.add(cloneWithName(p, "P" + (DataSaved.points_Create.size() + 1)));
                break;
        }

        rebuildPreview();
        return true;
    }

    public boolean removeLastPoint() {
        ensureCreateLists();
        if (DataSaved.points_Create.isEmpty()) return false;

        if (mode == MODE_AB) {
            int baseCount = getABBaseCount();
            if (baseCount >= 2) {
                Point3D a = cloneWithName(DataSaved.points_Create.get(0), "A");
                DataSaved.points_Create.clear();
                DataSaved.points_Create.add(a);
            } else {
                DataSaved.points_Create.clear();
            }
        } else {
            DataSaved.points_Create.remove(DataSaved.points_Create.size() - 1);
            renumberPickedPoints();
        }

        rebuildPreview();
        return true;
    }

    public void clear() {
        ensureCreateLists();
        DataSaved.points_Create.clear();
        rebuildPreview();
    }

    public void replacePickedPoints(Point3D[] points) {
        ensureCreateLists();
        DataSaved.points_Create.clear();
        if (points != null) {
            for (Point3D p : points) {
                if (p != null) DataSaved.points_Create.add(cloneWithName(p, p.getName()));
            }
        }
        renumberPickedPoints();

        if (mode == MODE_AREA && !DataSaved.points_Create.isEmpty()) {
            double z = DataSaved.points_Create.get(0).getZ();
            for (int i = 1; i < DataSaved.points_Create.size(); i++) {
                Point3D p = DataSaved.points_Create.get(i);
                DataSaved.points_Create.set(i, makeNamedPoint(nameForIndex(i), p.getX(), p.getY(), z));
            }
        }

        rebuildPreview();
    }

    public boolean isReadyToSave() {
        ensureCreateLists();
        switch (mode) {
            case MODE_PLAN:
                return DataSaved.points_Create.size() == 1;
            case MODE_AB:
                return getABPoints()[5] != null;
            case MODE_AREA:
                return DataSaved.points_Create.size() >= 3;
            case MODE_TRENCH:
                return DataSaved.points_Create.size() >= 2;
            case MODE_TRIANGLES:
                return DataSaved.points_Create.size() >= 3 && !performDelaunay(DataSaved.points_Create, null).isEmpty();
            default:
                return false;
        }
    }

    public int saveFlag() {
        return mode;
    }

    public void rebuild() {
        rebuildPreview();
    }

    public void rebuildPreview() {
        ensureCreateLists();
        clearDerivedGeometry();

        try {
            switch (mode) {
                case MODE_PLAN:
                    rebuildPlan();
                    break;
                case MODE_AB:
                    rebuildAB();
                    break;
                case MODE_AREA:
                    rebuildArea();
                    break;
                case MODE_TRENCH:
                    rebuildTrench();
                    break;
                case MODE_TRIANGLES:
                    rebuildTriangles();
                    break;
                default:
                    break;
            }
            syncLegacyStatics();
        } catch (Exception e) {
            Log.e(TAG, "rebuildPreview failed", e);
        }
    }

    public Point3D[] getABPoints() {
        ensureCreateLists();
        Point3D[] out = new Point3D[6];

        if (DataSaved.points_Create.size() > 0) out[0] = cloneWithName(DataSaved.points_Create.get(0), "A");
        if (DataSaved.points_Create.size() > 1) out[1] = cloneWithName(DataSaved.points_Create.get(1), "B");
        if (out[0] == null || out[1] == null) return out;

        Point3D a = out[0];
        Point3D b = out[1];
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double len = Math.hypot(dx, dy);
        if (len < EPS) return out;

        double nx = -dy / len;
        double ny = dx / len;

        double leftDz = Math.tan(Math.toRadians(abLeftSlopeDeg)) * abLeftWidth;
        double rightDz = Math.tan(Math.toRadians(abRightSlopeDeg)) * abRightWidth;

        out[2] = makeNamedPoint("C", b.getX() + nx * abLeftWidth, b.getY() + ny * abLeftWidth, b.getZ() + leftDz);
        out[3] = makeNamedPoint("D", a.getX() + nx * abLeftWidth, a.getY() + ny * abLeftWidth, a.getZ() + leftDz);
        out[4] = makeNamedPoint("E", b.getX() - nx * abRightWidth, b.getY() - ny * abRightWidth, b.getZ() + rightDz);
        out[5] = makeNamedPoint("F", a.getX() - nx * abRightWidth, a.getY() - ny * abRightWidth, a.getZ() + rightDz);
        return out;
    }

    public List<double[]> getAreaCoordinates() {
        ensureCreateLists();
        List<double[]> out = new ArrayList<>();
        double z = DataSaved.points_Create.isEmpty() ? 0.0 : DataSaved.points_Create.get(0).getZ();
        for (Point3D p : DataSaved.points_Create) {
            out.add(new double[]{p.getX(), p.getY(), z});
        }
        return out;
    }

    public Point3D[] getTrenchOrTrianglePoints() {
        ensureCreateLists();
        return DataSaved.points_Create.toArray(new Point3D[0]);
    }

    private void rebuildPlan() {
        if (DataSaved.points_Create.isEmpty()) return;

        Point3D center = cloneWithName(DataSaved.points_Create.get(0), "P");
        replaceFirstPoint(center);

        double h = planSide / 2.0;
        double z = center.getZ();

        double headingRad = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);

        // Asse forward: 0° = Nord, 90° = Est
        double fx = Math.sin(headingRad);
        double fy = Math.cos(headingRad);

        // Asse right: perpendicolare al forward
        double rx = Math.cos(headingRad);
        double ry = -Math.sin(headingRad);

        Point3D p1 = makeNamedPoint(
                "P1",
                center.getX() - fx * h - rx * h,
                center.getY() - fy * h - ry * h,
                z
        );

        Point3D p2 = makeNamedPoint(
                "P2",
                center.getX() + fx * h - rx * h,
                center.getY() + fy * h - ry * h,
                z
        );

        Point3D p3 = makeNamedPoint(
                "P3",
                center.getX() + fx * h + rx * h,
                center.getY() + fy * h + ry * h,
                z
        );

        Point3D p4 = makeNamedPoint(
                "P4",
                center.getX() - fx * h + rx * h,
                center.getY() - fy * h + ry * h,
                z
        );

        DataSaved.dxfFaces_Create.add(new Face3D(p1, p2, center, center, Color.YELLOW, faceLayer));
        DataSaved.dxfFaces_Create.add(new Face3D(p2, p3, center, center, Color.YELLOW, faceLayer));
        DataSaved.dxfFaces_Create.add(new Face3D(p3, p4, center, center, Color.YELLOW, faceLayer));
        DataSaved.dxfFaces_Create.add(new Face3D(p4, p1, center, center, Color.YELLOW, faceLayer));

        addPolylineClosedSegment(p1, p2);
        addPolylineClosedSegment(p2, p3);
        addPolylineClosedSegment(p3, p4);
        addPolylineClosedSegment(p4, p1);

        DataSaved.puntiProgetto = new Coordinate[]{
                new Coordinate(center.getX(), center.getY(), center.getZ())
        };

        Activity_Crea_Superficie.coordinateP = new ArrayList<>();
        Activity_Crea_Superficie.coordinateP.add(new double[]{p1.getX(), p1.getY(), p1.getZ()});
        Activity_Crea_Superficie.coordinateP.add(new double[]{p2.getX(), p2.getY(), p2.getZ()});
        Activity_Crea_Superficie.coordinateP.add(new double[]{p3.getX(), p3.getY(), p3.getZ()});
        Activity_Crea_Superficie.coordinateP.add(new double[]{p4.getX(), p4.getY(), p4.getZ()});
        addPointText("P", center);
        addPointText("P1", p1);
        addPointText("P2", p2);
        addPointText("P3", p3);
        addPointText("P4", p4);
    }
    private void rebuildAB() {
        Point3D[] p = getABPoints();
        if (p[0] == null || p[1] == null) return;
        if (p[2] == null || p[3] == null || p[4] == null || p[5] == null) return;

        DataSaved.points_Create.clear();
        String[] names = {"A", "B", "C", "D", "E", "F"};
        for (int i = 0; i < 6; i++) {
            DataSaved.points_Create.add(cloneWithName(p[i], names[i]));
        }

        addPolylineList(p[0], p[1], p[2], p[3], p[0]);
        addPolylineList(p[0], p[1], p[4], p[5], p[0]);

        DataSaved.dxfFaces_Create.add(new Face3D(p[0], p[1], p[2], p[2], Color.YELLOW, faceLayer));
        DataSaved.dxfFaces_Create.add(new Face3D(p[0], p[2], p[3], p[3], Color.YELLOW, faceLayer));
        DataSaved.dxfFaces_Create.add(new Face3D(p[0], p[5], p[4], p[4], Color.YELLOW, faceLayer));
        DataSaved.dxfFaces_Create.add(new Face3D(p[0], p[4], p[1], p[1], Color.YELLOW, faceLayer));
        addPointText("A", p[0]);
        addPointText("B", p[1]);
        addPointText("C", p[2]);
        addPointText("D", p[3]);
        addPointText("E", p[4]);
        addPointText("F", p[5]);
        Activity_Crea_Superficie.puntiAB = getABPoints();

    }

    private void rebuildArea() {
        if (DataSaved.points_Create.isEmpty()) return;

        double z = DataSaved.points_Create.get(0).getZ();
        for (int i = 0; i < DataSaved.points_Create.size(); i++) {
            Point3D p = DataSaved.points_Create.get(i);
            DataSaved.points_Create.set(i, makeNamedPoint(nameForIndex(i), p.getX(), p.getY(), z));
        }

        if (DataSaved.points_Create.size() >= 2) {
            List<Point3D> border = new ArrayList<>(DataSaved.points_Create);
            if (DataSaved.points_Create.size() >= 3) {
                border.add(cloneWithName(DataSaved.points_Create.get(0), DataSaved.points_Create.get(0).getName()));
            }
            addPolyline(border, Color.MAGENTA);
        }

        if (DataSaved.points_Create.size() >= 3) {
            Polygon polygon = polygonFromPoints(DataSaved.points_Create);
            addFacesFromTriangles(DataSaved.points_Create, performDelaunay(DataSaved.points_Create, polygon));
        }
        for (int i = 0; i < DataSaved.points_Create.size(); i++) {
            Point3D p = DataSaved.points_Create.get(i);
            addPointText("P" + (i + 1), p);
        }
    }

    private void rebuildTrench() {
        ensureCreateLists();

        if (DataSaved.points_Create == null || DataSaved.points_Create.size() < 2) {
            return;
        }

        renumberPickedPoints();

        buildTrenchCorridor(
                DataSaved.points_Create,
                Dialog_Trench.leftW_d,
                Dialog_Trench.rightW_d,
                Dialog_Trench.leftS_d,
                Dialog_Trench.rightS_d,
                Dialog_Trench.flat,
                Dialog_Trench.startZ_d,
                Dialog_Trench.endZ_d
        );
    }
    public Point3D[] getTrenchExportPoints() {
        ensureCreateLists();

        if (mode != MODE_TRENCH) {
            return getTrenchOrTrianglePoints();
        }

        if (DataSaved.points_Create == null || DataSaved.points_Create.isEmpty()) {
            return new Point3D[0];
        }

        List<Point3D> center = buildTrenchCenterlineForExport();
        return center.toArray(new Point3D[0]);
    }
    private List<Point3D> buildTrenchCenterlineForExport() {
        List<Point3D> out = new ArrayList<>();

        if (DataSaved.points_Create == null || DataSaved.points_Create.isEmpty()) {
            return out;
        }

        double[] station = computeStations(DataSaved.points_Create);
        double total = station.length == 0 ? 0.0 : station[station.length - 1];

        double startZ = Dialog_Trench.startZ_d;
        double endZ = Dialog_Trench.endZ_d;

        for (int i = 0; i < DataSaved.points_Create.size(); i++) {
            Point3D p = DataSaved.points_Create.get(i);

            double z = p.getZ();

            if (Dialog_Trench.flat && total > 1e-9) {
                double t = station[i] / total;
                z = startZ + t * (endZ - startZ);
            } else if (Dialog_Trench.flat) {
                z = startZ;
            }

            out.add(makeNamedPoint(
                    "P" + (i + 1),
                    p.getX(),
                    p.getY(),
                    z
            ));
        }

        return out;
    }
    private double[] computeStations(List<Point3D> points) {
        if (points == null || points.isEmpty()) {
            return new double[0];
        }

        double[] station = new double[points.size()];
        station[0] = 0.0;

        for (int i = 1; i < points.size(); i++) {
            Point3D prev = points.get(i - 1);
            Point3D curr = points.get(i);

            double dx = curr.getX() - prev.getX();
            double dy = curr.getY() - prev.getY();

            // Progressiva planimetrica lungo la dorsale.
            // Uso XY, non XYZ, perché la quota viene calcolata separatamente.
            double segmentLength = Math.hypot(dx, dy);

            station[i] = station[i - 1] + segmentLength;
        }

        return station;
    }
    private void buildTrenchCorridor(List<Point3D> pickedCenter,
                                     double leftWidth,
                                     double rightWidth,
                                     double leftSlopeDeg,
                                     double rightSlopeDeg,
                                     boolean flat,
                                     double startZ,
                                     double endZ) {
        if (pickedCenter == null || pickedCenter.size() < 2) return;

        final int n = pickedCenter.size();
        final double leftW = Math.max(0.0, leftWidth);
        final double rightW = Math.max(0.0, rightWidth);
        final double leftSlopeRad = Math.toRadians(leftSlopeDeg);
        final double rightSlopeRad = Math.toRadians(rightSlopeDeg);

        double[] station = computeTrenchStations(pickedCenter);
        double totalLength = station[n - 1];

        double safeStartZ = Double.isNaN(startZ) ? pickedCenter.get(0).getZ() : startZ;
        double safeEndZ = Double.isNaN(endZ) ? pickedCenter.get(n - 1).getZ() : endZ;

        ArrayList<Point3D> center = new ArrayList<>();
        ArrayList<Point3D> left = new ArrayList<>();
        ArrayList<Point3D> right = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Point3D src = pickedCenter.get(i);
            if (src == null) continue;

            double zCenter;
            if (flat) {
                double t = totalLength <= EPS ? 0.0 : station[i] / totalLength;
                zCenter = safeStartZ + t * (safeEndZ - safeStartZ);
            } else {
                zCenter = src.getZ();
            }

            Point3D c = makeNamedPoint("P" + (i + 1), src.getX(), src.getY(), zCenter);
            center.add(c);

            TrenchOffsetFrame frame = computeTrenchOffsetFrame(pickedCenter, i);

            double leftOffset = computeTrenchMiterOffset(leftW, frame.miterDenom);
            double rightOffset = computeTrenchMiterOffset(rightW, frame.miterDenom);

            double zLeft = zCenter + Math.tan(leftSlopeRad) * leftW;
            double zRight = zCenter + Math.tan(rightSlopeRad) * rightW;

            left.add(makeNamedPoint(
                    "L" + (i + 1),
                    c.getX() + frame.nx * leftOffset,
                    c.getY() + frame.ny * leftOffset,
                    zLeft
            ));

            right.add(makeNamedPoint(
                    "R" + (i + 1),
                    c.getX() - frame.nx * rightOffset,
                    c.getY() - frame.ny * rightOffset,
                    zRight
            ));
        }

        if (center.size() < 2 || left.size() != center.size() || right.size() != center.size()) return;

        // Preview polylines: centerline + left edge + right edge.
        addPolyline(center, Color.MAGENTA);
        addPolyline(left, Color.YELLOW);
        addPolyline(right, Color.YELLOW);

        // Structured mesh, segment by segment. No Delaunay: a trench is a corridor, not a point cloud.
        for (int i = 0; i < center.size() - 1; i++) {
            Point3D c1 = center.get(i);
            Point3D c2 = center.get(i + 1);
            Point3D l1 = left.get(i);
            Point3D l2 = left.get(i + 1);
            Point3D r1 = right.get(i);
            Point3D r2 = right.get(i + 1);

            // Left strip: C1-C2-L2-L1
            DataSaved.dxfFaces_Create.add(new Face3D(c1, c2, l2, l2, Color.YELLOW, faceLayer));
            DataSaved.dxfFaces_Create.add(new Face3D(c1, l2, l1, l1, Color.YELLOW, faceLayer));

            // Right strip: C1-R1-R2-C2
            DataSaved.dxfFaces_Create.add(new Face3D(c1, r1, r2, r2, Color.YELLOW, faceLayer));
            DataSaved.dxfFaces_Create.add(new Face3D(c1, r2, c2, c2, Color.YELLOW, faceLayer));
        }

        Activity_Crea_Superficie.point3DS = center.toArray(new Point3D[0]);
        Activity_Crea_Superficie.facceTrench = new ArrayList<>(DataSaved.dxfFaces_Create);
        Activity_Crea_Superficie.polyTrench = new Polyline(new ArrayList<>(center), polyLayer);

        Log.e("TRENCH_DEBUG",
                "points=" + center.size()
                        + " faces=" + DataSaved.dxfFaces_Create.size()
                        + " polylines=" + DataSaved.polylines_Create.size()
                        + " flat=" + flat
                        + " startZ=" + safeStartZ
                        + " endZ=" + safeEndZ
                        + " leftW=" + leftW
                        + " rightW=" + rightW
                        + " leftS=" + leftSlopeDeg
                        + " rightS=" + rightSlopeDeg);
    }

    private double[] computeTrenchStations(List<Point3D> points) {
        double[] station = new double[points.size()];
        station[0] = 0.0;
        for (int i = 1; i < points.size(); i++) {
            Point3D a = points.get(i - 1);
            Point3D b = points.get(i);
            station[i] = station[i - 1] + Math.hypot(b.getX() - a.getX(), b.getY() - a.getY());
        }
        return station;
    }

    private TrenchOffsetFrame computeTrenchOffsetFrame(List<Point3D> points, int i) {
        int n = points.size();

        double[] tPrev;
        double[] tNext;

        if (i == 0) {
            tPrev = unitTrenchDirection(points.get(0), points.get(1));
            tNext = tPrev;
        } else if (i == n - 1) {
            tPrev = unitTrenchDirection(points.get(n - 2), points.get(n - 1));
            tNext = tPrev;
        } else {
            tPrev = unitTrenchDirection(points.get(i - 1), points.get(i));
            tNext = unitTrenchDirection(points.get(i), points.get(i + 1));
        }

        double[] nPrev = leftTrenchNormal(tPrev);
        double[] nNext = leftTrenchNormal(tNext);

        double mx = nPrev[0] + nNext[0];
        double my = nPrev[1] + nNext[1];
        double mLen = Math.hypot(mx, my);

        if (mLen < 1e-6) {
            mx = nPrev[0];
            my = nPrev[1];
            mLen = 1.0;
        }

        mx /= mLen;
        my /= mLen;

        double denom = mx * nPrev[0] + my * nPrev[1];
        if (Math.abs(denom) < 0.25) {
            denom = denom < 0 ? -0.25 : 0.25;
        }

        return new TrenchOffsetFrame(mx, my, denom);
    }

    private double computeTrenchMiterOffset(double width, double denom) {
        if (width <= 0.0) return 0.0;
        double scale = width / denom;
        double maxAbs = width * 3.0;
        if (scale > maxAbs) scale = maxAbs;
        if (scale < -maxAbs) scale = -maxAbs;
        return scale;
    }

    private double[] unitTrenchDirection(Point3D a, Point3D b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double len = Math.hypot(dx, dy);
        if (len < EPS) return new double[]{0.0, 1.0};
        return new double[]{dx / len, dy / len};
    }

    private double[] leftTrenchNormal(double[] tangent) {
        return new double[]{-tangent[1], tangent[0]};
    }

    private static class TrenchOffsetFrame {
        final double nx;
        final double ny;
        final double miterDenom;

        TrenchOffsetFrame(double nx, double ny, double miterDenom) {
            this.nx = nx;
            this.ny = ny;
            this.miterDenom = miterDenom;
        }
    }


    private void rebuildTriangles() {
        ensureCreateLists();

        if (DataSaved.points_Create == null || DataSaved.points_Create.isEmpty()) {
            return;
        }

        renumberPickedPoints();

        if (DataSaved.points_Create.size() < 3) {
            return;
        }

        List<int[]> triangles = performDelaunay(DataSaved.points_Create, null);

        Log.e("TRIANGLES_DEBUG",
                "points=" + DataSaved.points_Create.size()
                        + " triangles=" + triangles.size());

        addFacesFromTriangles(DataSaved.points_Create, triangles);

        List<Point3D> border = convexHullBorder(DataSaved.points_Create);

        if (border.size() >= 3) {
            border.add(cloneWithName(border.get(0), border.get(0).getName()));
            addPolyline(border, Color.MAGENTA);
        }

        for (int i = 0; i < DataSaved.points_Create.size(); i++) {
            Point3D p = DataSaved.points_Create.get(i);
            addPointText("P" + (i + 1), p);
        }
    }

    private void buildTrenchEntities(List<Point3D> center, double leftW, double rightW,
                                     double leftSlopeDeg, double rightSlopeDeg) {
        if (center.size() < 2) return;
        List<Point3D> left = new ArrayList<>();
        List<Point3D> right = new ArrayList<>();
        double leftSlopeRad = Math.toRadians(leftSlopeDeg);
        double rightSlopeRad = Math.toRadians(rightSlopeDeg);

        for (int i = 0; i < center.size(); i++) {
            Point3D prev = center.get(Math.max(0, i - 1));
            Point3D next = center.get(Math.min(center.size() - 1, i + 1));
            double dx = next.getX() - prev.getX();
            double dy = next.getY() - prev.getY();
            double len = Math.hypot(dx, dy);
            if (len < EPS) {
                dx = 0;
                dy = 1;
                len = 1;
            }
            double nx = -dy / len;
            double ny = dx / len;
            Point3D c = center.get(i);
            left.add(makeNamedPoint("L" + (i + 1),
                    c.getX() + nx * leftW,
                    c.getY() + ny * leftW,
                    c.getZ() - Math.tan(leftSlopeRad) * leftW));
            right.add(makeNamedPoint("R" + (i + 1),
                    c.getX() - nx * rightW,
                    c.getY() - ny * rightW,
                    c.getZ() - Math.tan(rightSlopeRad) * rightW));
        }

        addPolyline(center, Color.MAGENTA);
        addPolyline(left, Color.MAGENTA);
        addPolyline(right, Color.MAGENTA);

        for (int i = 0; i < center.size() - 1; i++) {
            Point3D c1 = center.get(i);
            Point3D c2 = center.get(i + 1);
            Point3D l1 = left.get(i);
            Point3D l2 = left.get(i + 1);
            Point3D r1 = right.get(i);
            Point3D r2 = right.get(i + 1);
            DataSaved.dxfFaces_Create.add(new Face3D(c1, c2, l2, l2, Color.YELLOW, faceLayer));
            DataSaved.dxfFaces_Create.add(new Face3D(c1, l2, l1, l1, Color.YELLOW, faceLayer));
            DataSaved.dxfFaces_Create.add(new Face3D(c1, r2, c2, c2, Color.YELLOW, faceLayer));
            DataSaved.dxfFaces_Create.add(new Face3D(c1, r1, r2, r2, Color.YELLOW, faceLayer));
        }
    }

    private void addFacesFromTriangles(List<Point3D> points, List<int[]> triangles) {
        if (points == null || triangles == null) return;
        for (int[] t : triangles) {
            if (t == null || t.length < 3) continue;
            if (t[0] < 0 || t[1] < 0 || t[2] < 0) continue;
            if (t[0] >= points.size() || t[1] >= points.size() || t[2] >= points.size()) continue;
            Point3D p1 = points.get(t[0]);
            Point3D p2 = points.get(t[1]);
            Point3D p3 = points.get(t[2]);
            DataSaved.dxfFaces_Create.add(new Face3D(p1, p2, p3, p3, Color.YELLOW, faceLayer));
        }
    }

    private List<int[]> performDelaunay(List<Point3D> points, Polygon clipPolygon) {
        List<int[]> out = new ArrayList<>();
        if (points == null || points.size() < 3) return out;

        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[points.size()];
        for (int i = 0; i < points.size(); i++) {
            coords[i] = new Coordinate(points.get(i).getX(), points.get(i).getY(), points.get(i).getZ());
        }

        try {
            DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
            builder.setSites(gf.createMultiPointFromCoords(coords));
            Geometry triangles = builder.getTriangles(gf);

            for (int i = 0; i < triangles.getNumGeometries(); i++) {
                Polygon tri = (Polygon) triangles.getGeometryN(i);
                if (clipPolygon != null && !clipPolygon.covers(tri.getCentroid())) continue;

                Coordinate[] tc = tri.getCoordinates();
                if (tc == null || tc.length < 3) continue;

                int[] idx = new int[3];
                boolean ok = true;
                for (int j = 0; j < 3; j++) {
                    idx[j] = indexOf2D(points, tc[j]);
                    if (idx[j] < 0) {
                        ok = false;
                        break;
                    }
                }
                if (ok && idx[0] != idx[1] && idx[1] != idx[2] && idx[2] != idx[0]) {
                    out.add(idx);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Delaunay failed", e);
        }
        return out;
    }






    private int indexOf2D(List<Point3D> points, Coordinate c) {
        final double tol = 0.05; // 5 cm

        int bestIndex = -1;
        double bestDist = Double.MAX_VALUE;

        for (int i = 0; i < points.size(); i++) {
            Point3D p = points.get(i);

            double dx = p.getX() - c.x;
            double dy = p.getY() - c.y;
            double d = Math.hypot(dx, dy);

            if (d < bestDist) {
                bestDist = d;
                bestIndex = i;
            }
        }

        return bestDist <= tol ? bestIndex : -1;
    }

    private Polygon polygonFromPoints(List<Point3D> points) {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] c = new Coordinate[points.size() + 1];
        for (int i = 0; i < points.size(); i++) {
            c[i] = new Coordinate(points.get(i).getX(), points.get(i).getY(), points.get(i).getZ());
        }
        c[points.size()] = new Coordinate(points.get(0).getX(), points.get(0).getY(), points.get(0).getZ());
        return gf.createPolygon(c);
    }

    private void addPolylineClosedSegment(Point3D a, Point3D b) {
        ArrayList<Point3D> vertices = new ArrayList<>();
        vertices.add(a);
        vertices.add(b);
        addPolyline(vertices, Color.MAGENTA);
    }

    private void addPolylineList(Point3D... pts) {
        ArrayList<Point3D> vertices = new ArrayList<>();
        for (Point3D p : pts) if (p != null) vertices.add(p);
        addPolyline(vertices, Color.MAGENTA);
    }

    private void addPolyline(List<Point3D> vertices, int color) {
        if (vertices == null || vertices.size() < 2) return;
        ArrayList<Point3D> copy = new ArrayList<>();
        for (Point3D p : vertices) {
            Point3D q = cloneWithName(p, p.getName());
            q.setLayer(pointLayer);
            copy.add(q);
        }
        Polyline polyline = new Polyline(copy, polyLayer);
        polyline.setLineColor(color);
        DataSaved.polylines_Create.add(polyline);
    }

    private void ensureCreateLists() {
        if (DataSaved.points_Create == null) DataSaved.points_Create = new ArrayList<>();
        if (DataSaved.polylines_Create == null) DataSaved.polylines_Create = new ArrayList<>();
        if (DataSaved.dxfFaces_Create == null) DataSaved.dxfFaces_Create = new ArrayList<>();
        if (DataSaved.dxfTexts_Create == null) DataSaved.dxfTexts_Create = new ArrayList();
        ensureCreateLayersRegistered();
    }

    private void ensureCreateLayersRegistered() {
        if (DataSaved.dxfLayers_DTM == null) DataSaved.dxfLayers_DTM = new ArrayList<>();
        if (DataSaved.dxfLayers_POLY == null) DataSaved.dxfLayers_POLY = new ArrayList<>();
        if (DataSaved.dxfLayers_POINT == null) DataSaved.dxfLayers_POINT = new ArrayList<>();
        upsertLayer(DataSaved.dxfLayers_DTM, faceLayer);
        upsertLayer(DataSaved.dxfLayers_POLY, polyLayer);
        upsertLayer(DataSaved.dxfLayers_POINT, pointLayer);
    }

    private void upsertLayer(List<Layer> layers, Layer layer) {
        if (layers == null || layer == null) return;
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer existing = layers.get(i);
            if (existing != null && layer.getLayerName().equals(existing.getLayerName())) {
                layers.remove(i);
            }
        }
        layers.add(layer);
    }

    private void clearDerivedGeometry() {
        if (DataSaved.polylines_Create != null) DataSaved.polylines_Create.clear();
        if (DataSaved.dxfFaces_Create != null) DataSaved.dxfFaces_Create.clear();
        if (DataSaved.dxfTexts_Create != null) DataSaved.dxfTexts_Create.clear();
    }

    private void renumberPickedPoints() {
        if (mode == MODE_AB) return;
        for (int i = 0; i < DataSaved.points_Create.size(); i++) {
            Point3D p = DataSaved.points_Create.get(i);
            if (mode == MODE_PLAN) {
                DataSaved.points_Create.set(i, makeNamedPoint("P", p.getX(), p.getY(), p.getZ()));
            } else {
                DataSaved.points_Create.set(i, makeNamedPoint(nameForIndex(i), p.getX(), p.getY(), p.getZ()));
            }
        }
    }

    private void replaceFirstPoint(Point3D point) {
        if (DataSaved.points_Create == null || DataSaved.points_Create.isEmpty()) return;
        DataSaved.points_Create.set(0, cloneWithName(point, point.getName()));
    }

    private Point3D cloneWithName(Point3D p, String name) {
        Point3D out = new Point3D(name, p.getX(), p.getY(), p.getZ(), name);
        out.setLayer(pointLayer);
        return out;
    }

    private Point3D makePoint(String name, double x, double y, double z) {
        Point3D out = new Point3D(name, x, y, z, name);
        out.setLayer(pointLayer);
        return out;
    }

    private Point3D makeNamedPoint(String name, double x, double y, double z) {
        return makePoint(name, x, y, z);
    }

    private int getABBaseCount() {
        ensureCreateLists();
        if (DataSaved.points_Create.isEmpty()) return 0;
        if (DataSaved.points_Create.size() == 1) return 1;
        return 2;
    }

    private String nameForIndex(int i) {
        return "P" + (i + 1);
    }

    private double[] currentEdgeCoord() {
        try {
            switch (DataSaved.bucketEdge) {
                case -1:
                    return ExcavatorLib.bucketLeftCoord;
                case 1:
                    return ExcavatorLib.bucketRightCoord;
                case 0:
                default:
                    return ExcavatorLib.bucketCoord;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private int modeFromProject(String projectType) {
        if (projectType == null) return MODE_PLAN;
        switch (projectType) {
            case "PLAN": return MODE_PLAN;
            case "AB": return MODE_AB;
            case "AREA": return MODE_AREA;
            case "TRENCH": return MODE_TRENCH;
            case "TRIANGLES": return MODE_TRIANGLES;
            default: return MODE_PLAN;
        }
    }

    private void initLayers() {
        faceLayer = new Layer("CREATE", "LAYER_3D_FACES", Color.YELLOW, true);
        polyLayer = new Layer("CREATE", "LAYER_POLYLINES", Color.MAGENTA, true);
        pointLayer = new Layer("CREATE", "LAYER_POINTS", Color.CYAN, true);
    }

    private void initLegacyDefaultsForMode() {
        Activity_Crea_Superficie.leftDIST = abLeftWidth;
        Activity_Crea_Superficie.rightDIST = abRightWidth;
        Activity_Crea_Superficie.leftSLOPE = abLeftSlopeDeg;
        Activity_Crea_Superficie.rightSLOPE = abRightSlopeDeg;
    }

    public void syncLegacyStatics() {
        ensureCreateLists();
        Activity_Crea_Superficie.countPunti = mode == MODE_AB ? getABBaseCount() : DataSaved.points_Create.size();
        Activity_Crea_Superficie.indexSel = 0;
        Activity_Crea_Superficie.point3DS = getTrenchOrTrianglePoints();

        if (mode == MODE_AREA) {
            Activity_Crea_Superficie.coordinateP = new ArrayList<>();
            for (Point3D p : DataSaved.points_Create) {
                Activity_Crea_Superficie.coordinateP.add(new double[]{p.getX(), p.getY(), p.getZ()});
            }
        }

        if (mode == MODE_TRENCH) {
            List<Point3D> exportCenter = buildTrenchCenterlineForExport();

            Activity_Crea_Superficie.point3DS =
                    exportCenter.toArray(new Point3D[0]);

            Activity_Crea_Superficie.polyTrench =
                    DataSaved.polylines_Create.isEmpty()
                            ? new Polyline()
                            : DataSaved.polylines_Create.get(0);

            Activity_Crea_Superficie.facceTrench =
                    new ArrayList<>(DataSaved.dxfFaces_Create);
        }

        Point3D[] ab = getABPoints();
        Activity_Crea_Superficie.puntiAB = ab;
        Activity_Crea_Superficie.leftDIST = abLeftWidth;
        Activity_Crea_Superficie.leftSLOPE = abLeftSlopeDeg;
        Activity_Crea_Superficie.rightDIST = abRightWidth;
        Activity_Crea_Superficie.rightSLOPE = abRightSlopeDeg;
        if (ab[0] != null && ab[1] != null) {
            double dx = ab[1].getX() - ab[0].getX();
            double dy = ab[1].getY() - ab[0].getY();
            Activity_Crea_Superficie.distAB = Math.hypot(dx, dy);
            Activity_Crea_Superficie.slopeAB = Activity_Crea_Superficie.distAB < EPS ? 0.0 :
                    Math.toDegrees(Math.atan2(ab[1].getZ() - ab[0].getZ(), Activity_Crea_Superficie.distAB));
        } else {
            Activity_Crea_Superficie.distAB = 0.0;
            Activity_Crea_Superficie.slopeAB = 0.0;
        }

        if (mode == MODE_PLAN && !DataSaved.points_Create.isEmpty()) {
            Point3D p = DataSaved.points_Create.get(0);
            DataSaved.puntiProgetto = new Coordinate[]{new Coordinate(p.getX(), p.getY(), p.getZ())};
        } else if (mode == MODE_PLAN) {
            DataSaved.puntiProgetto = null;
        }
    }
    private void addPointText(String label, Point3D p) {
        if (label == null || label.trim().isEmpty() || p == null) return;

        if (DataSaved.dxfTexts_Create == null) {
            DataSaved.dxfTexts_Create = new ArrayList<>();
        }

        DataSaved.dxfTexts_Create.add(
                new DxfText(
                        label,
                        p.getX(),
                        p.getY(),
                        p.getZ(),
                        MyColorClass.colorConstraint,
                        pointLayer
                )
        );
    }
    private List<Point3D> convexHullBorder(List<Point3D> points) {
        List<Point3D> pts = new ArrayList<>();

        if (points == null || points.size() < 3) {
            return pts;
        }

        for (Point3D p : points) {
            if (p != null) {
                pts.add(cloneWithName(p, p.getName()));
            }
        }

        pts.sort((a, b) -> {
            int cmpX = Double.compare(a.getX(), b.getX());
            if (cmpX != 0) return cmpX;
            return Double.compare(a.getY(), b.getY());
        });

        List<Point3D> lower = new ArrayList<>();
        for (Point3D p : pts) {
            while (lower.size() >= 2
                    && cross(lower.get(lower.size() - 2), lower.get(lower.size() - 1), p) <= 0) {
                lower.remove(lower.size() - 1);
            }
            lower.add(p);
        }

        List<Point3D> upper = new ArrayList<>();
        for (int i = pts.size() - 1; i >= 0; i--) {
            Point3D p = pts.get(i);

            while (upper.size() >= 2
                    && cross(upper.get(upper.size() - 2), upper.get(upper.size() - 1), p) <= 0) {
                upper.remove(upper.size() - 1);
            }
            upper.add(p);
        }

        lower.remove(lower.size() - 1);
        upper.remove(upper.size() - 1);

        List<Point3D> hull = new ArrayList<>();
        hull.addAll(lower);
        hull.addAll(upper);

        return hull;
    }

    private double cross(Point3D o, Point3D a, Point3D b) {
        return (a.getX() - o.getX()) * (b.getY() - o.getY())
                - (a.getY() - o.getY()) * (b.getX() - o.getX());
    }
}
