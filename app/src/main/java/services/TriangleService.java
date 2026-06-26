package services;

import static packexcalib.exca.DataSaved.polylines;
import static packexcalib.exca.DredgeLib.centroRalla;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;
import static packexcalib.exca.ExcavatorLib.hdt_LAMA;
import static packexcalib.exca.ExcavatorLib.yawSensor;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DREDGE;
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
import gui.my_opengl.GLDrawer;
import gui.my_opengl.My3DActivity;
import gui.my_opengl.MyGLActivity_Create;
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
import packexcalib.surfcreator.TriangleHelper;
import utils.DistToPoint;
import utils.MyData;

public class TriangleService extends Service {
    public static boolean istriRunning;
    public static double DGM_Letf, DGM_Right;
    public static short Mainfall_Value = 0;
    public static int segnoLinea = 1;
    public static double[] quoteDTM;
    public static double orientamentoFreccia;
    static boolean startSort;
    static boolean isUpdating;
    static int rilettura;
    public static double minZ, maxZ, minZCreate, maxZCreate;
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
        Log.w("TriSPELL","Created");
        istriRunning =true;
        minZCreate = Double.MAX_VALUE;
        maxZCreate = Double.MIN_VALUE;
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
        Log.w("TriSPELL","Destroyed");
        istriRunning =false;
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
                try {
                    switch (DataSaved.isWL) {
                        case EXCAVATOR:
                            DataSaved.glL_AnchorView = bucketCoord;

                            DataSaved.GL_Bucket_Coord = PuntiBenna.GLBucketCoord();
                            DataSaved.GL_BENNA = My_Benna.puntiBenna();
                            DataSaved.GL_ATTACCO = My_Benna.attacco();
                            DataSaved.GL_STICK = My_Stick.puntiStick();

                            if (DataSaved.lrBoom2 == 0) {
                                DataSaved.GL_BOOM1 = My_Boom1.puntiBoom();
                                DataSaved.GL_BOOM1_2 = null;
                            } else {
                                DataSaved.GL_BOOM1 = null;
                                DataSaved.GL_BOOM1_2 = My_Boom1_Boom2.puntiBoom();
                            }

                            DataSaved.GL_FRAME_BASE = buildScaledFrameForDraw(My_Frame.puntiFrame());
                            autoScaleMachineFrameByBoom();
                            break;

                        case DREDGE:
                            prepareDredgeFrameForDraw();

                            DataSaved.glL_AnchorView = bucketCoord;

                            // IMPORTANTISSIMO:
                            // in draga non devono restare geometrie escavatore vive/stale
                            DataSaved.GL_Bucket_Coord = null;
                            DataSaved.GL_BENNA = null;
                            DataSaved.GL_ATTACCO = null;
                            DataSaved.GL_STICK = null;
                            DataSaved.GL_BOOM1 = null;
                            DataSaved.GL_BOOM1_2 = null;

                            // Solo frame/cabina/ralla/cingoli standard
                            DataSaved.GL_FRAME_BASE = buildScaledFrameForDraw(My_Frame.puntiFrameDredge());
                            autoScaleMachineFrameByBoom();
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
                    Log.e("TRI_GL_CREATE", Log.getStackTraceString(e));
                }
                switch (DataSaved.projectTAG) {
                    case "DXF":
                    case "XML":
                    case "AB":
                    case "PLAN":
                    case "AREA":
                    case "TRIANGLES":
                    case "TRENCH":


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
                        if (MyApp.visibleActivity instanceof MyGLActivity_Create) {


                            for (Face3D face : DataSaved.dxfFaces_Create) { // Assumendo che DataSaved.dxfFaces sia una lista di Face3D
                                Point3D[] vertices = new Point3D[]{face.getP1(), face.getP2(), face.getP3(), face.getP4()};
                                for (Point3D vertex : vertices) {
                                    if (vertex != null) { // Verifica che il vertice non sia nullo
                                        if (vertex.getZ() < minZCreate) minZCreate = vertex.getZ();
                                        if (vertex.getZ() > maxZCreate) maxZCreate = vertex.getZ();
                                    }
                                }
                            }

                            projRead = true;
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

                                if (DataSaved.filteredPolylines == null || DataSaved.filteredPolylines.isEmpty()) {
                                    resetLineSnapValues();
                                    DataSaved.isAutoSnap = 0;
                                    break;
                                }

                                Point3D referencePoint;
                                double refE;
                                double refN;

                                switch (DataSaved.bucketEdge) {
                                    case -1:
                                        refE = bucketLeftCoord[0];
                                        refN = bucketLeftCoord[1];
                                        referencePoint = new Point3D(refE, refN, 0);
                                        break;

                                    case 1:
                                        refE = bucketRightCoord[0];
                                        refN = bucketRightCoord[1];
                                        referencePoint = new Point3D(refE, refN, 0);
                                        break;

                                    case 0:
                                    default:
                                        refE = bucketCoord[0];
                                        refN = bucketCoord[1];
                                        referencePoint = new Point3D(refE, refN, 0);
                                        break;
                                }

                                Segment closestSegment = null;
                                Polyline activeOriginalPoly = null;
                                Polyline activeOffsetPoly = null;

                                if (DataSaved.lockUnlock == 0) {

                                    List<Segment> allOffsetSegments =
                                            buildOffsetForSnap(DataSaved.filteredPolylines, DataSaved.line_Offset);

                                    closestSegment = findClosestSegment(referencePoint, allOffsetSegments);

                                    if (closestSegment != null) {
                                        activeOriginalPoly = closestSegment.getPolyline();

                                        if (activeOriginalPoly != null) {
                                            activeOffsetPoly = DataSaved.line_Offset != 0
                                                    ? JTSOffsetHelper.generateOffsetPolyline(activeOriginalPoly, DataSaved.line_Offset)
                                                    : activeOriginalPoly;

                                            DataSaved.selectedPoly = activeOriginalPoly;
                                            DataSaved.selectedPoly_OFFSET = activeOffsetPoly;
                                        }
                                    }

                                } else {

                                    activeOriginalPoly = DataSaved.selectedPoly;

                                    if (activeOriginalPoly != null) {
                                        activeOffsetPoly = DataSaved.line_Offset != 0
                                                ? JTSOffsetHelper.generateOffsetPolyline(activeOriginalPoly, DataSaved.line_Offset)
                                                : activeOriginalPoly;

                                        DataSaved.selectedPoly_OFFSET = activeOffsetPoly;

                                        if (activeOffsetPoly != null
                                                && activeOffsetPoly.getVertices() != null
                                                && activeOffsetPoly.getVertices().size() >= 2) {

                                            List<Segment> lockedSegments = new ArrayList<>();
                                            List<Point3D> verts = activeOffsetPoly.getVertices();

                                            for (int i = 0; i < verts.size() - 1; i++) {
                                                lockedSegments.add(new Segment(
                                                        verts.get(i),
                                                        verts.get(i + 1),
                                                        activeOriginalPoly
                                                ));
                                            }

                                            closestSegment = findClosestSegment(referencePoint, lockedSegments);
                                        }
                                    }
                                }

                                if (closestSegment == null) {
                                    resetLineSnapValues();
                                    break;
                                }

                                updateLineSnapResult(refE, refN, closestSegment);

                                break;
                            case 20:

                                if (DataSaved.lockUnlock == 0 || DataSaved.selectedPoly == null) {
                                    resetLineSnapValues();
                                    break;
                                }

                                Point3D referencePoint20;
                                double refE20;
                                double refN20;

                                switch (DataSaved.bucketEdge) {
                                    case -1:
                                        refE20 = bucketLeftCoord[0];
                                        refN20 = bucketLeftCoord[1];
                                        referencePoint20 = new Point3D(refE20, refN20, 0);
                                        break;

                                    case 1:
                                        refE20 = bucketRightCoord[0];
                                        refN20 = bucketRightCoord[1];
                                        referencePoint20 = new Point3D(refE20, refN20, 0);
                                        break;

                                    case 0:
                                    default:
                                        refE20 = bucketCoord[0];
                                        refN20 = bucketCoord[1];
                                        referencePoint20 = new Point3D(refE20, refN20, 0);
                                        break;
                                }

                                Polyline activeOriginalPoly20 = DataSaved.selectedPoly;

                                Polyline activeOffsetPoly20 = DataSaved.line_Offset != 0
                                        ? JTSOffsetHelper.generateOffsetPolyline(activeOriginalPoly20, DataSaved.line_Offset)
                                        : activeOriginalPoly20;

                                if (activeOffsetPoly20 == null
                                        || activeOffsetPoly20.getVertices() == null
                                        || activeOffsetPoly20.getVertices().size() < 2) {
                                    resetLineSnapValues();
                                    break;
                                }

                                DataSaved.selectedPoly_OFFSET = activeOffsetPoly20;

                                List<Segment> lockedSegments20 = new ArrayList<>();
                                List<Point3D> verts20 = activeOffsetPoly20.getVertices();

                                for (int i = 0; i < verts20.size() - 1; i++) {
                                    lockedSegments20.add(new Segment(
                                            verts20.get(i),
                                            verts20.get(i + 1),
                                            activeOriginalPoly20
                                    ));
                                }

                                Segment closestSegment20 = findClosestSegment(referencePoint20, lockedSegments20);

                                if (closestSegment20 == null) {
                                    resetLineSnapValues();
                                    break;
                                }

                                updateLineSnapResult(refE20, refN20, closestSegment20);

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

    public static double normalize360(double a) {
        a = a % 360.0;
        return a < 0 ? a + 360.0 : a;
    }




    private static double angleFromBucketToCut(double refE, double refN, Point3D p) {
        if (p == null) return normalize360(orientamentoFreccia);

        double dE = p.getX() - refE;
        double dN = p.getY() - refN;

        if (Math.hypot(dE, dN) < 1e-6) {
            return normalize360(orientamentoFreccia);
        }

        return normalize360(Math.toDegrees(Math.atan2(dN, dE)));
    }

    private static void resetLineSnapValues() {
        DataSaved.nearestSegment = null;

        dist3D_SX = 0;
        dist3D_CT = 0;
        dist3D_DX = 0;

        orientamentoFreccia = 0;

        DataSaved.snapRefWorldX = 0;
        DataSaved.snapRefWorldY = 0;

        DataSaved.cutWorldX_1 = 0;
        DataSaved.cutWorldY_1 = 0;
        DataSaved.cutWorldX_2 = 0;
        DataSaved.cutWorldY_2 = 0;
    }

    private static void assignLineDistanceToBucketEdge(double distance) {
        dist3D_SX = 0;
        dist3D_CT = 0;
        dist3D_DX = 0;

        switch (DataSaved.bucketEdge) {
            case -1:
                dist3D_SX = distance;
                break;

            case 1:
                dist3D_DX = distance;
                break;

            case 0:
            default:
                dist3D_CT = distance;
                break;
        }
    }

    private static void updateLineSnapResult(double refE, double refN, Segment closestSegment) {

        if (closestSegment == null) {
            resetLineSnapValues();
            return;
        }

        Point3D p = closestSegment.getClosestPoint(refE, refN);

        if (p == null) {
            resetLineSnapValues();
            return;
        }

        DataSaved.nearestSegment = closestSegment;

        /*
         * Estremo A della linea verde:
         * bucketEdge realmente usato per lo snap.
         */
        DataSaved.snapRefWorldX = refE;
        DataSaved.snapRefWorldY = refN;

        /*
         * Estremo B della linea verde:
         * punto più vicino reale sul segmento selezionato.
         */
        DataSaved.cutWorldX_1 = p.getX();
        DataSaved.cutWorldY_1 = p.getY();

        /*
         * Per ora teniamo anche cutWorld_2 uguale.
         * Così qualunque codice vecchio che legge cutWorldX_2/Y_2
         * non usa più bucketCoord fisso o una proiezione diversa.
         */
        DataSaved.cutWorldX_2 = p.getX();
        DataSaved.cutWorldY_2 = p.getY();

        double dE = p.getX() - refE;
        double dN = p.getY() - refN;

        /*
         * Distanza vera dal bucketEdge al segmento finito.
         * NON usare DistToLine qui.
         */
        double lineDist = Math.hypot(dE, dN);

        dist3D_SX = 0;
        dist3D_CT = 0;
        dist3D_DX = 0;

        switch (DataSaved.bucketEdge) {
            case -1:
                dist3D_SX = lineDist;
                break;

            case 1:
                dist3D_DX = lineDist;
                break;

            case 0:
            default:
                dist3D_CT = lineDist;
                break;
        }

        /*
         * Segno destra/sinistra rispetto alla macchina.
         * Questo non serve per orientare la freccia overlay,
         * ma può servire per UI, colori, correzioni, ecc.
         */
        double yawRad = Math.toRadians(ExcavatorLib.hdt_BOOM + yawSensor);

        double latX = Math.cos(yawRad);
        double latY = -Math.sin(yawRad);

        double lateral = dE * latX + dN * latY;

        segnoLinea = lateral > 0 ? -1 : 1;

        /*
         * Angolo world bucketEdge -> snap.
         * Lo lasciamo aggiornato per compatibilità/log/debug.
         * Per orientare la freccia in 2D usa però il metodo world→screen,
         * così sarà esattamente parallela alla linea verde disegnata.
         */
        if (lineDist > 1e-9) {
            orientamentoFreccia = normalize360(Math.toDegrees(Math.atan2(dN, dE)));
        }
    }
    private static void prepareDredgeFrameForDraw() {
        try {
            /*
             * Se DredgeLib.Dredge() viene già chiamata dal decoder CAN,
             * questa chiamata non è strettamente obbligatoria.
             * Però è utile perché forza il refresh anche quando cambia GNSS/HDT
             * senza nuovo messaggio CAN.
             */
            packexcalib.exca.DredgeLib.Dredge();

            /*
             * Bridge DREDGE -> geometria standard frame escavatore.
             * My_Frame.puntiFrame() legge da ExcavatorLib, non da DredgeLib.
             */
            ExcavatorLib.correctPitch = packexcalib.exca.DredgeLib.correctDredgePitch;
            ExcavatorLib.correctRoll = packexcalib.exca.DredgeLib.correctDredgeRoll;

            /*
             * My_Frame usa coordinateDY come perno frame/boom.
             * In DredgeLib coordinateDY è già stato popolato in ExcavatorLib
             * perché lo importi staticamente lì.
             */
            ExcavatorLib.coordMiniPitch = ExcavatorLib.coordinateDY;

            /*
             * Se non hai roll dedicato del boom in draga, evita valori stale
             * provenienti dall'escavatore.
             */
            packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll = 0.0d;

        } catch (Exception e) {
            Log.e("TRI_DREDGE_FRAME", Log.getStackTraceString(e));
        }
    }
    private static void autoScaleMachineFrameByBoom() {
        try {
            if (DataSaved.GL_FRAME_BASE == null || DataSaved.GL_FRAME_BASE.length <= 49) return;

            /*
             * Per DREDGE uso Lunghezza_Braccio.
             * Per EXCAVATOR uso L_Boom1 + L_Boom2.
             */
            double boomLength;
            if (DataSaved.isWL == DREDGE) {
                boomLength = DataSaved.Lunghezza_Braccio;
            } else if (DataSaved.isWL == EXCAVATOR) {
                boomLength = DataSaved.L_Boom1 + Math.max(0.0d, DataSaved.L_Boom2);
            } else {
                return;
            }

            if (boomLength <= 0.0d) return;

            /*
             * Taratura:
             * fino a circa 12 m resta uguale.
             * sopra i 12 m cresce progressivamente.
             */
            float widthScale = clampFloat(
                    (float) (1.0d + Math.max(0.0d, boomLength - 12.0d) / 30.0d),
                    1.0f,
                    1.75f
            );

            /*
             * Allargo molto in larghezza, un po' meno in lunghezza.
             */
            float lengthScale = 1.0f + (widthScale - 1.0f) * 0.45f;

            scaleFrameAroundRalla(widthScale, lengthScale);

        } catch (Exception e) {
            Log.e("FRAME_AUTOSCALE", Log.getStackTraceString(e));
        }
    }

    private static void scaleFrameAroundRalla(float widthScale, float lengthScale) {
        Point3DF[] frame = DataSaved.GL_FRAME_BASE;
        if (frame == null || frame.length <= 49) return;

        Point3DF center = frame[36]; // centro/ralla bassa
        if (center == null) center = frame[35]; // fallback ralla alta
        if (center == null) return;

        /*
         * Assi locali del carro ricavati dai punti cingolo/frame.
         * Stessa logica che usiamo per pontone/fallback.
         */
        Point3DF sideAxis = null;
        Point3DF forwardAxis = null;

        if (frame[49] != null && frame[37] != null) {
            sideAxis = frame[49].subtract(frame[37]).normalize();
        }

        if (sideAxis == null || sideAxis.length() < 0.0001f) {
            sideAxis = new Point3DF(1f, 0f, 0f);
        }

        if (frame[38] != null && frame[37] != null) {
            forwardAxis = frame[38].subtract(frame[37]).normalize();
        }

        if (forwardAxis == null || forwardAxis.length() < 0.0001f) {
            forwardAxis = new Point3DF(0f, 1f, 0f);
        }

        Point3DF upAxis = cross(sideAxis, forwardAxis).normalize();
        if (upAxis.length() < 0.0001f) {
            upAxis = new Point3DF(0f, 0f, 1f);
        }

        /*
         * Ricostruisco forward ortogonale, così evito deformazioni strane
         * se i punti frame non sono perfettamente ortogonali.
         */
        forwardAxis = cross(upAxis, sideAxis).normalize();
        if (forwardAxis.length() < 0.0001f) {
            forwardAxis = new Point3DF(0f, 1f, 0f);
        }

        for (int i = 0; i < frame.length; i++) {
            Point3DF p = frame[i];
            if (p == null) continue;

            Point3DF v = p.subtract(center);

            float localSide = dot(v, sideAxis);
            float localForward = dot(v, forwardAxis);
            float localUp = dot(v, upAxis);

            frame[i] = center
                    .add(sideAxis.scale(localSide * widthScale))
                    .add(forwardAxis.scale(localForward * lengthScale))
                    .add(upAxis.scale(localUp));
        }
    }

    private static float dot(Point3DF a, Point3DF b) {
        return a.getX() * b.getX()
                + a.getY() * b.getY()
                + a.getZ() * b.getZ();
    }

    private static Point3DF cross(Point3DF a, Point3DF b) {
        return new Point3DF(
                a.getY() * b.getZ() - a.getZ() * b.getY(),
                a.getZ() * b.getX() - a.getX() * b.getZ(),
                a.getX() * b.getY() - a.getY() * b.getX()
        );
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    public static Point3DF[] buildScaledFrameForDraw(Point3DF[] rawFrame) {
        try {
            if (rawFrame == null || rawFrame.length <= 49) return rawFrame;

            if (DataSaved.isWL != EXCAVATOR && DataSaved.isWL != DREDGE) {
                return rawFrame;
            }

            double boomLength;
            if (DataSaved.isWL == DREDGE) {
                boomLength = DataSaved.Lunghezza_Braccio;
            } else {
                boomLength = DataSaved.L_Boom1 + Math.max(0.0d, DataSaved.L_Boom2);
            }

            if (boomLength <= 0.0d) return rawFrame;

            float widthScale = clampFloat(
                    (float) (1.0d + Math.max(0.0d, boomLength - 12.0d) / 30.0d),
                    1.0f,
                    1.75f
            );

            float lengthScale = 1.0f + (widthScale - 1.0f) * 0.45f;

            return scaledFrameCopy(rawFrame, widthScale, lengthScale);

        } catch (Exception e) {
            Log.e("FRAME_AUTOSCALE", Log.getStackTraceString(e));
            return rawFrame;
        }
    }

    private static Point3DF[] scaledFrameCopy(Point3DF[] rawFrame, float widthScale, float lengthScale) {
        if (rawFrame == null || rawFrame.length <= 49) return rawFrame;

        Point3DF center = rawFrame[36];
        if (center == null) center = rawFrame[35];
        if (center == null) return rawFrame;

        Point3DF sideAxis = null;
        Point3DF forwardAxis = null;

        if (rawFrame[49] != null && rawFrame[37] != null) {
            sideAxis = rawFrame[49].subtract(rawFrame[37]).normalize();
        }

        if (sideAxis == null || sideAxis.length() < 0.0001f) {
            sideAxis = new Point3DF(1f, 0f, 0f);
        }

        if (rawFrame[38] != null && rawFrame[37] != null) {
            forwardAxis = rawFrame[38].subtract(rawFrame[37]).normalize();
        }

        if (forwardAxis == null || forwardAxis.length() < 0.0001f) {
            forwardAxis = new Point3DF(0f, 1f, 0f);
        }

        Point3DF upAxis = cross(sideAxis, forwardAxis).normalize();
        if (upAxis.length() < 0.0001f) {
            upAxis = new Point3DF(0f, 0f, 1f);
        }

        forwardAxis = cross(upAxis, sideAxis).normalize();
        if (forwardAxis.length() < 0.0001f) {
            forwardAxis = new Point3DF(0f, 1f, 0f);
        }

        Point3DF[] out = new Point3DF[rawFrame.length];

        for (int i = 0; i < rawFrame.length; i++) {
            Point3DF p = rawFrame[i];

            if (p == null) {
                out[i] = null;
                continue;
            }

            Point3DF v = p.subtract(center);

            float localSide = dot(v, sideAxis);
            float localForward = dot(v, forwardAxis);
            float localUp = dot(v, upAxis);

            out[i] = center
                    .add(sideAxis.scale(localSide * widthScale))
                    .add(forwardAxis.scale(localForward * lengthScale))
                    .add(upAxis.scale(localUp));
        }

        return out;
    }


}