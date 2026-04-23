package services;

import static packexcalib.exca.DataSaved.polylines;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;
import static packexcalib.exca.ExcavatorLib.hdt_LAMA;
import static packexcalib.exca.ExcavatorLib.yawSensor;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.OEM_PROTO;
import static utils.MyTypes.WHEELLOADER;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dxf.Arc;
import dxf.Circle;
import dxf.CurveSampler;
import dxf.Face3D;
import dxf.IntersectionFinder;
import dxf.JTSOffsetHelper;
import dxf.Layer;
import dxf.Line;
import dxf.PNEZDPoint;
import dxf.Point2D;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Polyline_2D;
import dxf.Segment;
import gui.MyApp;
import gui.draw_class.Geometry2D;
import gui.my_opengl.GLDrawer;
import gui.my_opengl.My3DActivity;
import gui.my_opengl.Point3DF;
import gui.my_opengl.Vector3D;
import gui.my_opengl.dozer.My_Lama;
import gui.my_opengl.exca.My_Benna;
import gui.my_opengl.exca.My_Boom1;
import gui.my_opengl.exca.My_Boom1_Boom2;
import gui.my_opengl.exca.My_Frame;
import gui.my_opengl.exca.My_Stick;
import gui.my_opengl.exca.PuntiBenna;
import gui.my_opengl.wheel.My_Wheel;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;
import packexcalib.exca.ExcavatorLib;
import packexcalib.surfcreator.DistToLine;
import packexcalib.surfcreator.TriangleHelper;
import utils.DistToPoint;
import utils.MyData;

public class TriangleService extends Service {
    public static double DGM_Letf, DGM_Right;
    public static short Mainfall_Value = 0;
    public static int segnoLinea = 1;
    public static double[] quoteDTM;
    public static double orientamentoFreccia;
    static boolean startSort;
    static boolean isUpdating;
    static int rilettura;
    public static double minZ, maxZ;
    boolean projRead = false;
    private boolean isRunning = false;
    private ExecutorService executor;
    public static double quota3D_SX, quota3D_CT, quota3D_DX;
    public static double[] posL, posC, posR;
    public static double dist3D_SX, dist3D_CT, dist3D_DX;
    public static boolean ltOffGrid, ctOffGrid, rtOffGrid;
    static Point2D[] Line_Avanti, Line_Dietro, Line_Destra, Line_Sinistra;
    public static Point2D[][] tutteLinee;
    public static Point3DF glLinePoint, glSegmentPoint, glSegmentEnd, glLinePunto, glPuntoTerra, glTerraPunto;
    static int indexAudio;
    private static TriangleHelper triangleHelper;
    private static double[] lastPosition;
    int countPnezd = -1;
    static double conversionFactor = 1;


    @Override
    public void onCreate() {
        try {
            indexAudio = MyData.get_Int("indexAudioSystem");
        } catch (Exception e) {
            indexAudio = 0;
        }
        try {
            switch (MyData.get_Int("Unit_Of_Measure")) {
                case 0:
                case 1:
                    conversionFactor = 1;
                    break;

                case 2:
                case 3:
                case 4:
                case 5:
                    conversionFactor = 0.3048006096;
                    break;
                case 6:
                case 7:
                    conversionFactor = 0.3048;
                    break;
            }
        } catch (Exception e) {
            conversionFactor = 1;
        }
        super.onCreate();
        posL = new double[3];
        posC = new double[3];
        posR = new double[3];
        countPnezd = -1;
        startSort = false;
        DataSaved.filteredPolylines = new ArrayList<>();
        DataSaved.filteredPoints = new ArrayList<>();
        DataSaved.filteredFaces = new ArrayList<>();
        DataSaved.filteredDxfTexts = new ArrayList<>();
        DataSaved.filteredPolylinesGL_2D = new ArrayList<>();
        DataSaved.filteredFacesGL_2D = new ArrayList<>();
        isUpdating = false;
        triangleHelper = new TriangleHelper();
        lastPosition = new double[]{0, 0, 0};  // Posizione iniziale
        executor = Executors.newCachedThreadPool();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!isRunning) {
            isRunning = true;
            executor.execute(triangleRunnable);
        }


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        isUpdating = false;
        MyApp.isAlto = false;
        MyApp.isBasso = false;
        MyApp.isCentro = false;
        stopTriangleLoop();
    }

    private void stopTriangleLoop() {
        isRunning = false;
        executor.shutdownNow();
    }

    private final Runnable triangleRunnable = () -> {
        try {
            while (isRunning) {

                long startTime = System.currentTimeMillis();
                switch (DataSaved.projectTAG) {
                    case "DXF":
                    case "XML":
                    case "AB":
                    case "PLAN":
                    case "AREA":


                        double[][] positions = {bucketLeftCoord, bucketCoord, bucketRightCoord};
                        double[] quotas = updateCurrentPosition(positions);


                        quota3D_SX = quotas[0] - (DataSaved.offsetH * -1);

                        quota3D_CT = quotas[1] - (DataSaved.offsetH * -1);

                        quota3D_DX = quotas[2] - (DataSaved.offsetH * -1);

                        if (DataSaved.Interface_Type == OEM_PROTO) {
                            double QuotaMedia = (minZ + maxZ) / 2.0d;

                            DGM_Letf = bucketLeftCoord[2] - QuotaMedia;
                            DGM_Right = bucketRightCoord[2] - QuotaMedia;
                        }

                        switch (DataSaved.bucketEdge) {
                            case -1:
                                Line_Avanti = IntersectionFinder.Intersections(bucketLeftCoord, 0);
                                Line_Dietro = IntersectionFinder.Intersections(bucketLeftCoord, 180);
                                Line_Destra = IntersectionFinder.Intersections(bucketCoord, 90);
                                Line_Sinistra = IntersectionFinder.Intersections(bucketCoord, 270);
                                break;
                            case 0:
                                Line_Avanti = IntersectionFinder.Intersections(bucketCoord, 0);
                                Line_Dietro = IntersectionFinder.Intersections(bucketCoord, 180);
                                Line_Destra = IntersectionFinder.Intersections(bucketCoord, 90);
                                Line_Sinistra = IntersectionFinder.Intersections(bucketCoord, 270);
                                break;
                            case 1:
                                Line_Avanti = IntersectionFinder.Intersections(bucketRightCoord, 0);
                                Line_Dietro = IntersectionFinder.Intersections(bucketRightCoord, 180);
                                Line_Destra = IntersectionFinder.Intersections(bucketCoord, 90);
                                Line_Sinistra = IntersectionFinder.Intersections(bucketCoord, 270);
                                break;
                        }
                        tutteLinee = new Point2D[][]{Line_Avanti, Line_Dietro, Line_Sinistra, Line_Destra};


                        switch (DataSaved.isAutoSnap) {
                            case 0:
                                dist3D_SX = 0;
                                dist3D_CT = 0;
                                dist3D_DX = 0;
                                orientamentoFreccia = 0;

                                break;

                            case 1:
                                orientamentoFreccia = 0;
                                if (DataSaved.points != null && !DataSaved.points.isEmpty()) {
                                    switch (DataSaved.bucketEdge) {
                                        case -1:
                                            DataSaved.nearestPoint = findNearestPoint(bucketLeftCoord[0], bucketLeftCoord[1], DataSaved.filteredPoints);
                                            dist3D_SX = new DistToPoint(bucketLeftCoord[0], bucketLeftCoord[1], 0, DataSaved.nearestPoint != null ? DataSaved.nearestPoint.getX() : 0, DataSaved.nearestPoint != null ? DataSaved.nearestPoint.getY() : 0, 0).getDist_to_point();

                                            break;

                                        case 0:
                                            DataSaved.nearestPoint = findNearestPoint(bucketCoord[0], bucketCoord[1], DataSaved.filteredPoints);
                                            dist3D_CT = new DistToPoint(bucketCoord[0], bucketCoord[1], 0, DataSaved.nearestPoint != null ? DataSaved.nearestPoint.getX() : 0, DataSaved.nearestPoint != null ? DataSaved.nearestPoint.getY() : 0, 0).getDist_to_point();

                                            break;

                                        case 1:
                                            DataSaved.nearestPoint = findNearestPoint(bucketRightCoord[0], bucketRightCoord[1], DataSaved.filteredPoints);
                                            dist3D_DX = new DistToPoint(bucketRightCoord[0], bucketRightCoord[1], 0, DataSaved.nearestPoint != null ? DataSaved.nearestPoint.getX() : 0, DataSaved.nearestPoint != null ? DataSaved.nearestPoint.getY() : 0, 0).getDist_to_point();

                                            break;
                                    }
                                } else {
                                    DataSaved.isAutoSnap = 0;
                                }
                                break;

                            case 2:

                                Point3D referencePoint = new Point3D(bucketCoord[0], bucketCoord[1], 0);

                                if (DataSaved.filteredPolylines != null && !DataSaved.filteredPolylines.isEmpty()) {

                                    // Genera segmenti offset a partire dalle polilinee filtrate
                                    List<Segment> allOffsetSegments = buildOffsetForSnap(DataSaved.filteredPolylines, DataSaved.line_Offset);
                                    switch (DataSaved.bucketEdge) {
                                        case -1:
                                            referencePoint = new Point3D(bucketLeftCoord[0], bucketLeftCoord[1], 0);
                                            break;
                                        case 0:
                                            referencePoint = new Point3D(bucketCoord[0], bucketCoord[1], 0);
                                            break;
                                        case 1:
                                            referencePoint = new Point3D(bucketRightCoord[0], bucketRightCoord[1], 0);
                                            break;
                                        default:
                                            referencePoint = new Point3D(bucketCoord[0], bucketCoord[1], 0);
                                            break;
                                    }
                                    Segment closestSegment = null;
                                    Polyline activeOriginalPoly = null;
                                    Polyline activeOffsetPoly = null;
                                    if (DataSaved.lockUnlock == 0) {
                                        // normale: cerca tra tutti i segmenti offsettati/originali
                                        closestSegment = findClosestSegment(referencePoint, allOffsetSegments);

                                        if (closestSegment != null) {
                                            activeOriginalPoly = closestSegment.getPolyline(); // ora è SEMPRE l'originale
                                            activeOffsetPoly = (DataSaved.line_Offset != 0)
                                                    ? JTSOffsetHelper.generateOffsetPolyline(activeOriginalPoly, DataSaved.line_Offset)
                                                    : activeOriginalPoly;

                                            DataSaved.selectedPoly = activeOriginalPoly;
                                            DataSaved.selectedPoly_OFFSET = activeOffsetPoly;
                                        }
                                    } else {
                                        // lock: lavora SEMPRE partendo dall'originale bloccata
                                        activeOriginalPoly = DataSaved.selectedPoly;

                                        if (activeOriginalPoly != null) {
                                            activeOffsetPoly = (DataSaved.line_Offset != 0)
                                                    ? JTSOffsetHelper.generateOffsetPolyline(activeOriginalPoly, DataSaved.line_Offset)
                                                    : activeOriginalPoly;

                                            DataSaved.selectedPoly_OFFSET = activeOffsetPoly;

                                            List<Segment> lockedSegments = new ArrayList<>();
                                            List<Point3D> verts = activeOffsetPoly.getVertices();

                                            for (int i = 0; i < verts.size() - 1; i++) {
                                                // il Segment può continuare a puntare all'originale
                                                lockedSegments.add(new Segment(verts.get(i), verts.get(i + 1), activeOriginalPoly));
                                            }
                                            closestSegment = findClosestSegment(referencePoint, lockedSegments);
                                        }
                                    }

                                    DataSaved.nearestSegment = closestSegment;
                                    double refE = 0, refN = 0;
                                    // salva i risultati


                                    // calcola le distanze 3D
                                    if (DataSaved.bucketEdge == -1 && closestSegment != null) {
                                        dist3D_SX = Math.abs(new DistToLine(bucketLeftCoord[0], bucketLeftCoord[1],
                                                closestSegment.getStart().getX(), closestSegment.getStart().getY(),
                                                closestSegment.getEnd().getX(), closestSegment.getEnd().getY()).getLinedistance());
                                        refE = bucketLeftCoord[0];
                                        refN = bucketLeftCoord[1];

                                    } else if (DataSaved.bucketEdge == 0 && closestSegment != null) {
                                        dist3D_CT = Math.abs(new DistToLine(bucketCoord[0], bucketCoord[1],
                                                closestSegment.getStart().getX(), closestSegment.getStart().getY(),
                                                closestSegment.getEnd().getX(), closestSegment.getEnd().getY()).getLinedistance());
                                        refE = bucketCoord[0];
                                        refN = bucketCoord[1];
                                    } else if (DataSaved.bucketEdge == 1 && closestSegment != null) {
                                        dist3D_DX = Math.abs(new DistToLine(bucketRightCoord[0], bucketRightCoord[1],
                                                closestSegment.getStart().getX(), closestSegment.getStart().getY(),
                                                closestSegment.getEnd().getX(), closestSegment.getEnd().getY()).getLinedistance());
                                        refE = bucketRightCoord[0];
                                        refN = bucketRightCoord[1];
                                    }

                                    Point3D p = closestSegment.getClosestPoint(refE, refN);
                                    double dE = p.getX() - refE;
                                    double dN = p.getY() - refN;
                                    double yawRad = Math.toRadians(ExcavatorLib.hdt_BOOM + yawSensor);
                                    double latX = Math.cos(yawRad);
                                    double latY = -Math.sin(yawRad);
                                    double lateral = dE * latX + dN * latY;
                                    if (lateral > 0) {
                                        segnoLinea = -1;
                                    } else {
                                        segnoLinea = 1;
                                    }

                                    Segment seg = DataSaved.nearestSegment;
                                    Point2D cut1 =
                                            Geometry2D.projectPointOnSegment(
                                                    referencePoint.getX(),
                                                    referencePoint.getY(),
                                                    seg.getStart(),
                                                    seg.getEnd()
                                            );
                                    DataSaved.cutWorldX_1 = cut1.getX();
                                    DataSaved.cutWorldY_1 = cut1.getY();


                                    Point2D cut2 =
                                            Geometry2D.projectPointOnSegment(
                                                    bucketCoord[0],
                                                    bucketCoord[1],
                                                    seg.getStart(),
                                                    seg.getEnd()
                                            );
                                    DataSaved.cutWorldX_2 = cut2.getX();
                                    DataSaved.cutWorldY_2 = cut2.getY();
                                } else {
                                    DataSaved.isAutoSnap = 0;
                                }

                                break;
                            case 20:
                                if (DataSaved.lockUnlock == 0 || DataSaved.selectedPoly == null) {
                                    DataSaved.nearestSegment = null;
                                    dist3D_SX = 0;
                                    dist3D_CT = 0;
                                    dist3D_DX = 0;
                                    orientamentoFreccia = 0;
                                    break;
                                }

                                Point3D referencePoint20;
                                double refE20;
                                double refN20;

                                switch (DataSaved.bucketEdge) {
                                    case -1:
                                        referencePoint20 = new Point3D(bucketLeftCoord[0], bucketLeftCoord[1], 0);
                                        refE20 = bucketLeftCoord[0];
                                        refN20 = bucketLeftCoord[1];
                                        break;
                                    case 1:
                                        referencePoint20 = new Point3D(bucketRightCoord[0], bucketRightCoord[1], 0);
                                        refE20 = bucketRightCoord[0];
                                        refN20 = bucketRightCoord[1];
                                        break;
                                    case 0:
                                    default:
                                        referencePoint20 = new Point3D(bucketCoord[0], bucketCoord[1], 0);
                                        refE20 = bucketCoord[0];
                                        refN20 = bucketCoord[1];
                                        break;
                                }

                                Polyline activeOriginalPoly20 = DataSaved.selectedPoly;
                                Polyline activeOffsetPoly20 = (DataSaved.line_Offset != 0)
                                        ? JTSOffsetHelper.generateOffsetPolyline(activeOriginalPoly20, DataSaved.line_Offset)
                                        : activeOriginalPoly20;

                                if (activeOffsetPoly20 == null || activeOffsetPoly20.getVertices() == null
                                        || activeOffsetPoly20.getVertices().size() < 2) {
                                    DataSaved.nearestSegment = null;
                                    orientamentoFreccia = 0;
                                    break;
                                }

                                DataSaved.selectedPoly_OFFSET = activeOffsetPoly20;

                                List<Segment> lockedSegments20 = new ArrayList<>();
                                List<Point3D> verts20 = activeOffsetPoly20.getVertices();
                                for (int i = 0; i < verts20.size() - 1; i++) {
                                    lockedSegments20.add(new Segment(verts20.get(i), verts20.get(i + 1), activeOriginalPoly20));
                                }

                                Segment closestSegment20 = findClosestSegment(referencePoint20, lockedSegments20);
                                DataSaved.nearestSegment = closestSegment20;

                                if (closestSegment20 == null) {
                                    orientamentoFreccia = 0;
                                    break;
                                }

                                dist3D_SX = 0;
                                dist3D_CT = 0;
                                dist3D_DX = 0;

                                double lineDist20 = Math.abs(new DistToLine(
                                        refE20, refN20,
                                        closestSegment20.getStart().getX(), closestSegment20.getStart().getY(),
                                        closestSegment20.getEnd().getX(), closestSegment20.getEnd().getY()
                                ).getLinedistance());

                                switch (DataSaved.bucketEdge) {
                                    case -1:
                                        dist3D_SX = lineDist20;
                                        break;
                                    case 0:
                                        dist3D_CT = lineDist20;
                                        break;
                                    case 1:
                                        dist3D_DX = lineDist20;
                                        break;
                                }

                                Point3D p20 = closestSegment20.getClosestPoint(refE20, refN20);
                                double dE20 = p20.getX() - refE20;
                                double dN20 = p20.getY() - refN20;

                                double yawRad20 = Math.toRadians(ExcavatorLib.hdt_BOOM + yawSensor);
                                double latX20 = Math.cos(yawRad20);
                                double latY20 = -Math.sin(yawRad20);
                                double lateral20 = dE20 * latX20 + dN20 * latY20;

                                segnoLinea = (lateral20 > 0) ? -1 : 1;

                                // se la freccia usa questo campo, aggiornalo qui e non lasciarlo stale
                                orientamentoFreccia = Math.toDegrees(Math.atan2(
                                        closestSegment20.getEnd().getY() - closestSegment20.getStart().getY(),
                                        closestSegment20.getEnd().getX() - closestSegment20.getStart().getX()
                                ));

                                Point2D cut1_20 = Geometry2D.projectPointOnSegment(
                                        referencePoint20.getX(),
                                        referencePoint20.getY(),
                                        closestSegment20.getStart(),
                                        closestSegment20.getEnd()
                                );
                                DataSaved.cutWorldX_1 = cut1_20.getX();
                                DataSaved.cutWorldY_1 = cut1_20.getY();

                                // importante: qui NON bucketCoord fisso, ma lo stesso ref selezionato
                                Point2D cut2_20 = Geometry2D.projectPointOnSegment(
                                        refE20,
                                        refN20,
                                        closestSegment20.getStart(),
                                        closestSegment20.getEnd()
                                );
                                DataSaved.cutWorldX_2 = cut2_20.getX();
                                DataSaved.cutWorldY_2 = cut2_20.getY();

                                break;


                        }

                        if (!projRead) {
                            minZ = Double.MAX_VALUE;
                            maxZ = Double.MIN_VALUE;

                            for (Face3D face : DataSaved.dxfFaces) { // Assumendo che DataSaved.dxfFaces sia una lista di Face3D
                                Point3D[] vertices = new Point3D[]{face.getP1(), face.getP2(), face.getP3(), face.getP4()};
                                for (Point3D vertex : vertices) {
                                    if (vertex != null) { // Verifica che il vertice non sia nullo
                                        if (vertex.getZ() < minZ) minZ = vertex.getZ();
                                        if (vertex.getZ() > maxZ) maxZ = vertex.getZ();
                                    }
                                }
                            }

                            projRead = true;
                        }
                        break;

                }


                try {


                    switch (DataSaved.isWL) {
                        case EXCAVATOR:
                            DataSaved.glL_AnchorView = bucketCoord;//scegliere quale è il punto sul quale ancorare la vista GL
                            DataSaved.GL_Bucket_Coord = PuntiBenna.GLBucketCoord();
                            DataSaved.GL_BENNA = My_Benna.puntiBenna();
                            DataSaved.GL_ATTACCO = My_Benna.attacco();
                            DataSaved.GL_STICK = My_Stick.puntiStick();
                            if (DataSaved.lrBoom2 == 0) {
                                DataSaved.GL_BOOM1 = My_Boom1.puntiBoom();
                            } else {
                                DataSaved.GL_BOOM1_2 = My_Boom1_Boom2.puntiBoom();
                            }
                            DataSaved.GL_FRAME_BASE = My_Frame.puntiFrame();


                            break;

                        case WHEELLOADER:
                            DataSaved.glL_AnchorView = bucketCoord;//scegliere quale è il punto sul quale ancorare la vista GL
                            DataSaved.GL_WHEEL = My_Wheel.puntiBenna();
                            break;

                        case DOZER:
                        case DOZER_SIX:
                        case GRADER:
                            DataSaved.glL_AnchorView = bucketCoord;//scegliere quale è il punto sul quale ancorare la vista GL
                            DataSaved.GL_LAMA = My_Lama.puntiLama();
                            //TODO machine frame
                            break;
                        //TODO altre macchine
                    }

                } catch (Exception e) {
                }


                DataSaved.filteredPolylines = getFilteredPolylines();


                countPnezd++;
                if (countPnezd % 100 == 0) {

                    scanPNEZD(conversionFactor);
                }


                //Conditions
                if (indexAudio > 0) {
                    switch (DataSaved.bucketEdge) {
                        case -1:
                            if (!ltOffGrid) {
                                if (Math.abs(quota3D_SX) <= DataSaved.deadbandH) {
                                    MyApp.isCentro = true;
                                    MyApp.isAlto = false;
                                    MyApp.isBasso = false;
                                } else if (quota3D_SX < DataSaved.deadbandH * -1) {
                                    MyApp.isCentro = false;
                                    MyApp.isAlto = false;
                                    MyApp.isBasso = true;
                                } else if (quota3D_SX > DataSaved.deadbandH) {
                                    MyApp.isCentro = false;
                                    MyApp.isAlto = true;
                                    MyApp.isBasso = false;
                                }
                            } else {
                                MyApp.isCentro = false;
                                MyApp.isAlto = false;
                                MyApp.isBasso = false;
                            }

                            break;

                        case 0:
                            if (!ctOffGrid) {
                                if (Math.abs(quota3D_CT) <= DataSaved.deadbandH) {
                                    MyApp.isCentro = true;
                                    MyApp.isAlto = false;
                                    MyApp.isBasso = false;
                                } else if (quota3D_CT < DataSaved.deadbandH * -1) {
                                    MyApp.isCentro = false;
                                    MyApp.isAlto = false;
                                    MyApp.isBasso = true;
                                } else if (quota3D_CT > DataSaved.deadbandH) {
                                    MyApp.isCentro = false;
                                    MyApp.isAlto = true;
                                    MyApp.isBasso = false;
                                }
                            } else {
                                MyApp.isCentro = false;
                                MyApp.isAlto = false;
                                MyApp.isBasso = false;
                            }
                            break;

                        case 1:
                            if (!rtOffGrid) {
                                if (Math.abs(quota3D_DX) <= DataSaved.deadbandH) {
                                    MyApp.isCentro = true;
                                    MyApp.isAlto = false;
                                    MyApp.isBasso = false;
                                } else if (quota3D_DX < DataSaved.deadbandH * -1) {
                                    MyApp.isCentro = false;
                                    MyApp.isAlto = false;
                                    MyApp.isBasso = true;
                                } else if (quota3D_DX > DataSaved.deadbandH) {
                                    MyApp.isCentro = false;
                                    MyApp.isAlto = true;
                                    MyApp.isBasso = false;
                                }
                            } else {
                                MyApp.isCentro = false;
                                MyApp.isAlto = false;
                                MyApp.isBasso = false;
                            }
                            break;
                    }
                } else {
                    MyApp.isCentro = false;
                    MyApp.isAlto = false;
                    MyApp.isBasso = false;
                }
                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = 100 - elapsedTime;

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(Math.abs(sleepTime));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (Exception e) {

        }
    };

    public static double[] updateCurrentPosition(double[][] positions) {


        double[] newPositionL = new double[]{positions[0][0], positions[0][1], positions[0][2]};
        double[] newPositionC = new double[]{positions[1][0], positions[1][1], positions[1][2]};
        double[] newPositionR = new double[]{positions[2][0], positions[2][1], positions[2][2]};

        double[] newPositionFW = Exca_Quaternion.endPoint(bucketCoord, 0, 0, DataSaved.Mainfall_Distance, hdt_LAMA);
        double[] newPositionBW = Exca_Quaternion.endPoint(bucketCoord, 0, 0, DataSaved.Mainfall_Distance, hdt_LAMA + 180);
        rilettura++;
        // Controlla se è necessario aggiornare i triangoli nel raggio
        double r = DataSaved.RaggioDXF / 4;
        r = Math.min(r, 30);
        if (DistToPoint.dist2D(newPositionC, lastPosition) > r) {
            GLDrawer.clearTextTextureCache();
            lastPosition = newPositionC;
            triangleHelper.updateTrianglesInRadius(lastPosition, DataSaved.RaggioDXF);

        }
        if (DataSaved.points != null) {
            if (!DataSaved.points.isEmpty()) {
                if (!startSort) {
                    lastPosition = newPositionC;
                    triangleHelper.updateTrianglesInRadius(lastPosition, DataSaved.RaggioDXF);
                    startSort = true;
                }
            }
        }

        double[] mQuoDTM = new double[]{
                triangleHelper.calculateZ(newPositionL),
                triangleHelper.calculateZ(newPositionC),
                triangleHelper.calculateZ(newPositionR),
                triangleHelper.calculateZ(newPositionFW),
                triangleHelper.calculateZ(newPositionBW)
        };
        double deltaZL = triangleHelper.calculateDeltaZ(newPositionL);
        double deltaZC = triangleHelper.calculateDeltaZ(newPositionC);
        double deltaZR = triangleHelper.calculateDeltaZ(newPositionR);
        posL = new double[]{newPositionL[0], newPositionL[1], mQuoDTM[0]};
        posC = new double[]{newPositionC[0], newPositionC[1], mQuoDTM[1]};
        posR = new double[]{newPositionR[0], newPositionR[1], mQuoDTM[2]};

        quoteDTM = mQuoDTM;

        ltOffGrid = deltaZL == Double.MIN_VALUE;
        ctOffGrid = deltaZC == Double.MIN_VALUE;
        rtOffGrid = deltaZR == Double.MIN_VALUE;
        Mainfall_Value = calculateSlopePercentShort(newPositionC, newPositionFW);

        return new double[]{deltaZL, deltaZC, deltaZR};
    }


    public static Point3D findNearestPoint(double bucketEst, double bucketNord, List<Point3D> filteredPoints) {
        if (DataSaved.lockUnlock == 0) {
            if (filteredPoints == null || filteredPoints.isEmpty()) {
                return null;
            }

            Point3D nearestPoint = null;
            double minDistance = Double.MAX_VALUE;

            for (Point3D point : filteredPoints) {
                double distance = new DistToPoint(bucketEst, bucketNord, 0, point.getX(), point.getY(), 0).getDist_to_point();
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestPoint = point;
                }
            }

            return nearestPoint;
        } else {
            return DataSaved.nearestPoint;
        }
    }

    public static Segment findClosestSegment(Point3D point, List<Segment> segments) {
        if (segments == null || segments.isEmpty()) return null;

        Segment closestSegment = null;
        double minDistance = Double.MAX_VALUE;

        for (Segment segment : segments) {
            double distance = pointToSegmentDistance(point, segment);
            if (distance < minDistance) {
                minDistance = distance;
                closestSegment = segment;
            }
        }

        return closestSegment;
    }


    private static double pointToSegmentDistance(Point3D p, Segment segment) {
        double x = p.getX(), y = p.getY();
        double x1 = segment.getStart().getX();
        double y1 = segment.getStart().getY();
        double x2 = segment.getEnd().getX();
        double y2 = segment.getEnd().getY();

        double dx = x2 - x1;
        double dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            // segmento degenerato (punto)
            return Math.hypot(x - x1, y - y1);
        }

        double t = ((x - x1) * dx + (y - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));

        double projX = x1 + t * dx;
        double projY = y1 + t * dy;

        return Math.hypot(x - projX, y - projY);
    }

    public static List<Polyline> getFilteredPolylines() {
        List<Polyline> filteredPolylines = new ArrayList<>();

        // POLYLINE native
        if (polylines != null) {
            for (Polyline polyline : polylines) {
                if (polyline != null
                        && polyline.getLayer() != null
                        && polyline.getLayer().getLayerName() != null
                        && isLayerEnabled(polyline.getLayer().getLayerName())) {
                    filteredPolylines.add(polyline);
                }
            }
        }

        // Merge opzionale delle altre entità 2D
        if (DataSaved.merge2DEntitiesForSnap == 0) {
            return filteredPolylines;
        }

        // LINE
        if (DataSaved.lines_2D != null) {
            for (Line line : DataSaved.lines_2D) {
                if (line != null
                        && line.getLayer() != null
                        && line.getLayer().getLayerName() != null
                        && isLayerEnabled(line.getLayer().getLayerName())) {
                    Polyline poly = convertLineToPolyline(line);
                    if (poly.getVertices() != null && poly.getVertices().size() >= 2) {
                        filteredPolylines.add(poly);
                    }
                }
            }
        }

        // LWPOLYLINE
        if (DataSaved.polylines_2D != null) {
            for (Polyline_2D lw : DataSaved.polylines_2D) {
                if (lw != null
                        && lw.getLayer() != null
                        && lw.getLayer().getLayerName() != null
                        && isLayerEnabled(lw.getLayer().getLayerName())) {
                    Polyline poly = CurveSampler.sampleBulge(lw);
                    if (poly.getVertices() != null && poly.getVertices().size() >= 2) {
                        filteredPolylines.add(poly);
                    }
                }
            }
        }

        // ARC
        if (DataSaved.arcs != null) {
            for (Arc arc : DataSaved.arcs) {
                if (arc != null
                        && arc.getLayer() != null
                        && arc.getLayer().getLayerName() != null
                        && isLayerEnabled(arc.getLayer().getLayerName())) {
                    Polyline poly = CurveSampler.sampleArc(arc);
                    if (poly.getVertices() != null && poly.getVertices().size() >= 2) {
                        filteredPolylines.add(poly);
                    }
                }
            }
        }

        // CIRCLE
        if (DataSaved.circles != null) {
            for (Circle circle : DataSaved.circles) {
                if (circle != null
                        && circle.getLayer() != null
                        && circle.getLayer().getLayerName() != null
                        && isLayerEnabled(circle.getLayer().getLayerName())) {
                    Polyline poly = CurveSampler.sampleCircle(circle);
                    if (poly.getVertices() != null && poly.getVertices().size() >= 2) {
                        filteredPolylines.add(poly);
                    }
                }
            }
        }

        return filteredPolylines;
    }


    public static boolean isLayerEnabled(String layerName) {
        try {


            if (layerName == null || layerName.isEmpty()) {
                return false; // Layer nullo o vuoto non è abilitato
            }

            // Cerca il layer nelle tre liste
            for (Layer layer : DataSaved.dxfLayers_DTM) {
                if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                    return true;
                }
            }
            for (Layer layer : DataSaved.dxfLayers_POLY) {
                if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                    return true;
                }
            }
            for (Layer layer : DataSaved.dxfLayers_POINT) {
                if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                    return true;
                }
            }

        } catch (Exception e) {
            return false;
        }
        return false; // Se il layer non è trovato o non è abilitato
    }

    public static List<Segment> buildOffsetForSnap(List<Polyline> polylines, double offset) {
        List<Segment> segments = new ArrayList<>();

        for (Polyline poly : polylines) {
            Polyline geometryPoly;

            if (offset == 0) {
                geometryPoly = poly;
            } else {
                geometryPoly = JTSOffsetHelper.generateOffsetPolyline(poly, offset);
            }

            if (geometryPoly == null || geometryPoly.getVertices().size() < 2) continue;

            List<Point3D> verts = geometryPoly.getVertices();

            for (int i = 0; i < verts.size() - 1; i++) {
                // riferimento SEMPRE alla polyline originale
                segments.add(new Segment(verts.get(i), verts.get(i + 1), poly));
            }
        }

        return segments;
    }

    public static Point3D getProjectedPointOnSegment3D(Point3D P, Point3D A, Point3D B) {
        Vector3D AP = Vector3D.subtract(P, A);
        Vector3D AB = Vector3D.subtract(B, A);

        double abLengthSquared = AB.dot(AB);
        if (abLengthSquared == 0) return A; // Segmento degenerato

        double t = AP.dot(AB) / abLengthSquared;
        t = Math.max(0, Math.min(1, t)); // Clamp tra 0 e 1 per restare sul segmento

        return new Point3D(
                A.getX() + t * (B.getX() - A.getX()),
                A.getY() + t * (B.getY() - A.getY()),
                A.getZ() + t * (B.getZ() - A.getZ()) // opzionalmente interpola anche Z se i punti sono in 3D
        );
    }

    public static void scanPNEZD(double conversionFactor) {
        if (My3DActivity.PNEZD_FUNCTION || DataSaved.isAutoSnap == 1 || My3DActivity.glPoint) {

            try {
                String filePath = DataSaved.PNEZDPath;
                File file = new File(filePath);

                List<PNEZDPoint> punti = new ArrayList<>();

                if (file.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        boolean firstLine = true;

                        while ((line = br.readLine()) != null) {
                            if (firstLine) {
                                firstLine = false; // salta intestazione
                                continue;
                            }
                            if (line.trim().isEmpty()) continue;

                            String[] parts = line.split(",");

                            // Default values
                            int pointNumber = -1;
                            double northing = 0.0;
                            double easting = 0.0;
                            double elevation = 0.0;
                            String description = "";
                            int color = Color.RED; // fallback costante

                            try {
                                if (parts.length > 0 && !parts[0].trim().isEmpty())
                                    pointNumber = Integer.parseInt(parts[0].trim());
                                if (parts.length > 1 && !parts[1].trim().isEmpty())
                                    northing = Double.parseDouble(parts[1].trim()) * conversionFactor;
                                if (parts.length > 2 && !parts[2].trim().isEmpty())
                                    easting = Double.parseDouble(parts[2].trim()) * conversionFactor;
                                if (parts.length > 3 && !parts[3].trim().isEmpty())
                                    elevation = Double.parseDouble(parts[3].trim()) * conversionFactor;
                                if (parts.length > 4)
                                    description = parts[4].trim();
                                if (parts.length > 5 && !parts[5].trim().isEmpty())
                                    color = Integer.parseInt(parts[5].trim());
                            } catch (Exception ex) {
                                Log.e("PNEZD", "Parse parziale fallito, uso valori di default: " + line);
                            }

                            // Se manca pointNumber, salta la riga
                            if (pointNumber == -1) continue;

                            PNEZDPoint punto = new PNEZDPoint(
                                    filePath,
                                    pointNumber,
                                    northing,
                                    easting,
                                    elevation,
                                    description,
                                    color
                            );
                            punti.add(punto);
                        }
                    }
                }

                DataSaved.pnezdPoints = punti;

            } catch (Exception e) {
                Log.e("PnezdE", Log.getStackTraceString(e));
            }

            if (DataSaved.points == null) {
                DataSaved.points = new ArrayList<>();
            }

            // Aggiungi sempre i PNEZD
            for (PNEZDPoint p : DataSaved.pnezdPoints) {
                Point3D newPoint = new Point3D(
                        p.getFilename(),
                        "PNEZD: " + p.getPointNumber(),
                        p.getEasting(),
                        p.getNorthing(),
                        p.getElevation(),
                        p.getColor(),
                        new Layer(DataSaved.PNEZDPath, "MyPNEZD", Color.WHITE, true),
                        p.getDescription()
                );

                if (!DataSaved.points.contains(newPoint)) {
                    DataSaved.points.add(newPoint);
                }
                if (!DataSaved.filteredPoints.contains(newPoint)) {
                    DataSaved.filteredPoints.add(newPoint);
                }
            }
        }
    }


    public static short calculateSlopePercentShort(double[] newPositionC, double[] newPositionFW) {

        if (newPositionC == null || newPositionFW == null
                || newPositionC.length < 3 || newPositionFW.length < 3) {
            throw new IllegalArgumentException("Gli array devono contenere almeno 3 valori (X,Y,Z)");
        }

        double deltaX = newPositionFW[0] - newPositionC[0];
        double deltaY = newPositionFW[1] - newPositionC[1];
        double deltaZ = newPositionFW[2] - newPositionC[2];

        // Distanza orizzontale (XY)
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (horizontalDistance == 0) {
            return 0; // evita divisione per zero
        }

        // Pendenza percentuale
        double slopePercent = (deltaZ / horizontalDistance) * 100.0;

        // 2 decimali → moltiplico ×100 e arrotondo
        int scaled = (int) Math.round(slopePercent * 100.0);

        // Clamp nei limiti dello short (-32768 a 32767)
        if (scaled > Short.MAX_VALUE) scaled = Short.MAX_VALUE;
        if (scaled < Short.MIN_VALUE) scaled = Short.MIN_VALUE;

        return (short) scaled;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static class PolyPickResult {
        public final Polyline polyline;
        public final Polyline offsetPolyline;
        public final Segment closestSegment;
        public final int candidateCount;
        public final List<PolyCandidate> candidates;

        public PolyPickResult(Polyline polyline,
                              Polyline offsetPolyline,
                              Segment closestSegment,
                              int candidateCount,
                              List<PolyCandidate> candidates) {
            this.polyline = polyline;
            this.offsetPolyline = offsetPolyline;
            this.closestSegment = closestSegment;
            this.candidateCount = candidateCount;
            this.candidates = candidates != null ? candidates : new ArrayList<>();
        }
    }

    public static class PolyCandidate {
        public final Polyline originalPolyline;
        public final Polyline offsetPolyline;
        public final Segment closestSegment;
        public final double distance;

        public PolyCandidate(Polyline originalPolyline,
                             Polyline offsetPolyline,
                             Segment closestSegment,
                             double distance) {
            this.originalPolyline = originalPolyline;
            this.offsetPolyline = offsetPolyline;
            this.closestSegment = closestSegment;
            this.distance = distance;
        }

        public String getLabel() {
            String layerName = "NO_LAYER";
            try {
                if (originalPolyline != null
                        && originalPolyline.getLayer() != null
                        && originalPolyline.getLayer().getLayerName() != null
                        && !originalPolyline.getLayer().getLayerName().isEmpty()) {
                    layerName = originalPolyline.getLayer().getLayerName();
                }
            } catch (Exception ignored) {
            }

            return layerName + " • d=" + String.format(java.util.Locale.US, "%.2f", distance);
        }
    }

    public static PolyPickResult pickPolylineNear(Point3D point,
                                                  List<Polyline> polylines,
                                                  double offset,
                                                  double maxDistance) {
        if (point == null || polylines == null || polylines.isEmpty()) {
            return new PolyPickResult(null, null, null, 0, new ArrayList<>());
        }

        List<PolyCandidate> candidates = new ArrayList<>();

        for (Polyline original : polylines) {
            if (original == null || original.getVertices() == null || original.getVertices().size() < 2) {
                continue;
            }

            Polyline geometryPolyline = (offset != 0)
                    ? JTSOffsetHelper.generateOffsetPolyline(original, offset)
                    : original;

            if (geometryPolyline == null
                    || geometryPolyline.getVertices() == null
                    || geometryPolyline.getVertices().size() < 2) {
                continue;
            }

            Segment bestSegmentForPolyline = null;
            double bestDistanceForPolyline = Double.MAX_VALUE;

            List<Point3D> verts = geometryPolyline.getVertices();
            for (int i = 0; i < verts.size() - 1; i++) {
                Segment segment = new Segment(verts.get(i), verts.get(i + 1), original);
                double distance = pointToSegmentDistance(point, segment);

                if (distance < bestDistanceForPolyline) {
                    bestDistanceForPolyline = distance;
                    bestSegmentForPolyline = segment;
                }
            }

            if (bestSegmentForPolyline != null && bestDistanceForPolyline <= maxDistance) {
                candidates.add(new PolyCandidate(
                        original,
                        geometryPolyline,
                        bestSegmentForPolyline,
                        bestDistanceForPolyline
                ));
            }
        }

        if (candidates.isEmpty()) {
            return new PolyPickResult(null, null, null, 0, candidates);
        }

        candidates.sort((a, b) -> Double.compare(a.distance, b.distance));

        if (candidates.size() > 1) {
            return new PolyPickResult(null, null, null, candidates.size(), candidates);
        }

        PolyCandidate chosen = candidates.get(0);
        return new PolyPickResult(
                chosen.originalPolyline,
                chosen.offsetPolyline,
                chosen.closestSegment,
                1,
                candidates
        );
    }


    /// ///////////////
    private static Polyline convertLineToPolyline(Line line) {
        Polyline p = new Polyline();
        p.setLayer(line.getLayer());
        p.setLineColor(line.getColor());

        if (line.getStart() != null) {
            p.getVertices().add(line.getStart().clone());
        }
        if (line.getEnd() != null) {
            p.getVertices().add(line.getEnd().clone());
        }

        p.markGlDirty();
        return p;
    }


}