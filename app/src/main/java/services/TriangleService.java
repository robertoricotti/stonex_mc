package services;

import static packexcalib.exca.DataSaved.polylines;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;
import static packexcalib.exca.ExcavatorLib.correctPitch;
import static packexcalib.exca.ExcavatorLib.correctRoll;
import static packexcalib.exca.ExcavatorLib.hdt_LAMA;

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

import dxf.Face3D;
import dxf.IntersectionFinder;
import dxf.JTSOffsetHelper;
import dxf.Layer;
import dxf.PNEZDPoint;
import dxf.Point2D;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Segment;
import gui.MyApp;
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
                    conversionFactor = 0.3048006096;
                    break;
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
            conversionFactor=1;
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



                                if (DataSaved.filteredPolylines != null && !DataSaved.filteredPolylines.isEmpty()) {

                                    // Genera segmenti offset a partire dalle polilinee filtrate
                                    List<Segment> allOffsetSegments = buildOffsetForSnap(DataSaved.filteredPolylines, DataSaved.line_Offset);

                                    Point3D referencePoint;
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

                                    Segment closestSegment;

                                    if (DataSaved.lockUnlock == 0) {
                                        // comportamento normale: trova il segmento più vicino tra tutti
                                        closestSegment = findClosestSegment(referencePoint, allOffsetSegments);
                                        DataSaved.selectedPoly = closestSegment.getPolyline();
                                    } else {
                                        // lock attivo: calcola l'offset partendo sempre dall'originale
                                        Polyline offsetPoly = DataSaved.line_Offset != 0 ?
                                                JTSOffsetHelper.generateOffsetPolyline(DataSaved.selectedPoly, DataSaved.line_Offset) :
                                                DataSaved.selectedPoly;

                                        // genera segmenti della polyline selezionata solo
                                        List<Segment> lockedSegments = new ArrayList<>();
                                        List<Point3D> verts = offsetPoly.getVertices();
                                        for (int i = 0; i < verts.size() - 1; i++) {
                                            lockedSegments.add(new Segment(verts.get(i), verts.get(i + 1), DataSaved.selectedPoly));
                                        }

                                        closestSegment = findClosestSegment(referencePoint, lockedSegments);
                                    }

                                    // salva i risultati
                                    DataSaved.nearestSegment = closestSegment;
                                    DataSaved.selectedPoly_OFFSET = closestSegment != null ? closestSegment.getPolyline() : null;

                                    // calcola le distanze 3D
                                    if (DataSaved.bucketEdge == -1 && closestSegment != null) {
                                        dist3D_SX = Math.abs(new DistToLine(bucketLeftCoord[0], bucketLeftCoord[1],
                                                closestSegment.getStart().getX(), closestSegment.getStart().getY(),
                                                closestSegment.getEnd().getX(), closestSegment.getEnd().getY()).getLinedistance());
                                    } else if (DataSaved.bucketEdge == 0 && closestSegment != null) {
                                        dist3D_CT = Math.abs(new DistToLine(bucketCoord[0], bucketCoord[1],
                                                closestSegment.getStart().getX(), closestSegment.getStart().getY(),
                                                closestSegment.getEnd().getX(), closestSegment.getEnd().getY()).getLinedistance());
                                    } else if (DataSaved.bucketEdge == 1 && closestSegment != null) {
                                        dist3D_DX = Math.abs(new DistToLine(bucketRightCoord[0], bucketRightCoord[1],
                                                closestSegment.getStart().getX(), closestSegment.getStart().getY(),
                                                closestSegment.getEnd().getX(), closestSegment.getEnd().getY()).getLinedistance());
                                    }

                                } else {
                                    DataSaved.isAutoSnap = 0;
                                }

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
                        case 0:
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

                        case 1:
                            DataSaved.glL_AnchorView = bucketCoord;//scegliere quale è il punto sul quale ancorare la vista GL
                            DataSaved.GL_WHEEL = My_Wheel.puntiBenna();
                            break;

                        case 2:
                        case 3:
                        case 4:
                            DataSaved.glL_AnchorView = bucketCoord;//scegliere quale è il punto sul quale ancorare la vista GL
                            DataSaved.GL_LAMA = My_Lama.puntiLama();
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

        double[] newPositionFW = Exca_Quaternion.endPoint(bucketCoord,0,0,0.5,hdt_LAMA);
        double[] newPositionBW = Exca_Quaternion.endPoint(bucketCoord,0,0,0.5,hdt_LAMA+180);
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

        double [] mQuoDTM=new double[]{
                triangleHelper.calculateZ(newPositionL),
                triangleHelper.calculateZ(newPositionC),
                triangleHelper.calculateZ(newPositionR),
                triangleHelper.calculateZ(newPositionFW),
                triangleHelper.calculateZ(newPositionBW)
        };
        double deltaZL = triangleHelper.calculateDeltaZ(newPositionL);
        double deltaZC = triangleHelper.calculateDeltaZ(newPositionC);
        double deltaZR = triangleHelper.calculateDeltaZ(newPositionR);
        posL = new double[]{newPositionL[0], newPositionL[1],mQuoDTM[0] };
        posC = new double[]{newPositionC[0], newPositionC[1],mQuoDTM[1] };
        posR = new double[]{newPositionR[0], newPositionR[1],mQuoDTM[2] };

        quoteDTM=mQuoDTM;

        ltOffGrid = deltaZL == Double.MIN_VALUE;
        ctOffGrid = deltaZC == Double.MIN_VALUE;
        rtOffGrid = deltaZR == Double.MIN_VALUE;

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
        if (DataSaved.lockUnlock == 0) {
            //  Comportamento attuale: cerca il più vicino tra tutti
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

        } else {
            //  Usa SEMPRE la polyline offsettata della selezionata
            if (DataSaved.selectedPoly == null || DataSaved.selectedPoly.getVertices().size() < 2)
                return null;

            Polyline polyToUse;
            if (DataSaved.line_Offset == 0) {
                polyToUse = DataSaved.selectedPoly;
            } else {
                polyToUse = JTSOffsetHelper.generateOffsetPolyline(DataSaved.selectedPoly, DataSaved.line_Offset);
            }

            if (polyToUse == null || polyToUse.getVertices().size() < 2) return null;

            List<Segment> lockedSegments = new ArrayList<>();
            List<Point3D> verts = polyToUse.getVertices();
            for (int i = 0; i < verts.size() - 1; i++) {
                lockedSegments.add(new Segment(verts.get(i), verts.get(i + 1), polyToUse));
            }

            Segment closestSegment = null;
            double minDistance = Double.MAX_VALUE;

            for (Segment segment : lockedSegments) {
                double distance = pointToSegmentDistance(point, segment);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestSegment = segment;
                }
            }

            return closestSegment;
        }
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

        if (polylines == null || polylines.isEmpty()) {
            return filteredPolylines; // Ritorna una lista vuota se non ci sono polylines
        }

        for (Polyline polyline : polylines) {
            if (polyline.getLayer().getLayerName() != null) {
                if (isLayerEnabled(polyline.getLayer().getLayerName())) {
                    filteredPolylines.add(polyline); // Aggiungi solo le polylines con layer attivo
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
            Polyline polyToUse;

            //  Se offset == 0, usa direttamente la polyline originale
            if (offset == 0) {
                polyToUse = poly;
            } else {
                polyToUse = JTSOffsetHelper.generateOffsetPolyline(poly, offset);
            }

            if (polyToUse == null || polyToUse.getVertices().size() < 2) continue;

            List<Point3D> verts = polyToUse.getVertices();

            for (int i = 0; i < verts.size() - 1; i++) {
                segments.add(new Segment(verts.get(i), verts.get(i + 1), polyToUse));
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
                                    northing = Double.parseDouble(parts[1].trim())*conversionFactor;
                                if (parts.length > 2 && !parts[2].trim().isEmpty())
                                    easting = Double.parseDouble(parts[2].trim())*conversionFactor;
                                if (parts.length > 3 && !parts[3].trim().isEmpty())
                                    elevation = Double.parseDouble(parts[3].trim())*conversionFactor;
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}