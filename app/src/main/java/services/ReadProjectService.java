package services;


import static gui.MyApp.gridFile_GR_dE;
import static gui.MyApp.gridFile_GR_dN;
import static gui.MyApp.heposTransformer;
import static packexcalib.gnss.CRS_Strings._NONE;
import static services.CanSender.GNSS_MSG;
import static services.CanSender.tryingBTCAN;
import static services.TriangleService.scanPNEZD;
import static services.UpdateValuesService.UTM;
import static services.UpdateValuesService.WGS84;
import static services.UpdateValuesService.crsFactory;
import static services.UpdateValuesService.ctFactory;
import static services.UpdateValuesService.result;
import static services.UpdateValuesService.resultWgs;
import static services.UpdateValuesService.utmToWgs;
import static services.UpdateValuesService.wgsToUtm;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.SOLARDRILL;
import static utils.MyTypes.WHEELLOADER;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.InvalidValueException;
import org.locationtech.proj4j.ProjCoordinate;
import org.locationtech.proj4j.UnknownAuthorityCodeException;
import org.locationtech.proj4j.UnsupportedParameterException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import drill_pile.gui.Drill_Activity;
import drill_pile.gui.ProjectReportCsvWriter;
import drill_pile.gui.ProjectStateCsvStore;
import dxf.Arc;
import dxf.Circle;
import dxf.DXFData;
import dxf.DXFParser_20;
import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Polyline_2D;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.my_opengl.My3DActivity;
import gui.projects.PickProject;
import iredes.DrillCSVParser;
import iredes.IrdParser;
import iredes.Point3D_Drill;
import landxml.LandXMLData;
import landxml.LandXMLParser;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.GridShiftTransformer;
import packexcalib.gnss.LocalizationFactory;
import packexcalib.gnss.LocalizationModel;
import utils.MyData;
import utils.MyDeviceManager;


public class ReadProjectService extends Service {


    static boolean mettiPoly, mettiPunti;
    int uom;
    boolean isFeet;
    public static boolean ERRORE_PROGETTO;
    private Executor mExecutor;
    private static final int THREAD_POOL_SIZE = 4;

    DXFData dxfData, dxfDataPoly, dxfDataPoint;
    LandXMLData landXMLData, landXMLPOLY, landXMLPOINT;
    public static String fileExtensionPOLY, fileExtensionPOINT;
    public static String parserStatus = "Wait Reading Files....";
    public static int numbers;
    public static boolean isFinishedDTM, isFinishedPOLY, isFinishedPOINT;
    public static double conversionFactor = 1;
    public static LocalizationModel model;

    public ReadProjectService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        numbers = 0;
        mettiPoly = false;
        mettiPunti = false;
        isFinishedDTM = false;
        isFinishedPOLY = false;
        isFinishedPOINT = false;
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
        mExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mExecutor.execute(new MyAsync_Excecutor());
    }

    private class MyAsync_Excecutor implements Runnable {
        @Override
        public void run() {
            switch (DataSaved.isWL) {
                case EXCAVATOR:
                case DOZER:
                case DOZER_SIX:
                case WHEELLOADER:
                case GRADER:
                    Execute_MC();
                    break;
                case DRILL:
                case SOLARDRILL:

                    Excecute_DRILL();
                    break;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            String nomeProgettoTRM = MyData.get_String("progettoSelected");
            if (!DataSaved.lastProjectName.equals(nomeProgettoTRM)) {
                reconnectLayers();
            }
        } catch (Exception e) {
            ((ExecutorService) mExecutor).shutdown();
        }

        ((ExecutorService) mExecutor).shutdown();
    }

    private void copiaFacce() {
        DataSaved.dxfFacesGL_2D = new ArrayList<>();

        for (Face3D face : DataSaved.dxfFaces) {
            // Copia i 3 o 4 punti della faccia, azzerando la Z
            Point3D p1 = new Point3D(face.getP1().getX(), face.getP1().getY(), 0);
            Point3D p2 = new Point3D(face.getP2().getX(), face.getP2().getY(), 0);
            Point3D p3 = new Point3D(face.getP3().getX(), face.getP3().getY(), 0);
            Point3D p4 = face.getP4(); // può essere uguale a p3 (triangolo) o diverso (quadrilatero)
            Point3D p4New = p4.equals(face.getP3()) ? p3 : new Point3D(p4.getX(), p4.getY(), 0);

            // Crea nuova Face3D con layer e colore uguali
            Face3D face2D = new Face3D(p1, p2, p3, p4New, face.getColor(), face.getLayer());
            face2D.setLayer(face.getLayer());

            DataSaved.dxfFacesGL_2D.add(face2D);
        }
    }

    private void copiaPoly() {
        DataSaved.polylinesGL_2D = new ArrayList<>();
        for (Polyline poly : DataSaved.polylines) {
            List<Point3D> newVertices = new ArrayList<>();
            for (Point3D pt : poly.getVertices()) {
                newVertices.add(new Point3D(pt.getX(), pt.getY(), 0)); // Z azzerata
            }

            Polyline poly2D = new Polyline(newVertices, poly.getLayer());
            poly2D.setLineColor(poly.getLineColor());

            DataSaved.polylinesGL_2D.add(poly2D);
        }
    }

    private void goToLicense() {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                DataSaved.isAutoSnap = 0;
                Intent intent = new Intent(MyApp.visibleActivity, Activity_Home_Page.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                MyApp.visibleActivity.finish();
            }
        };
        handler.post(runnable);
    }

    private void waitForWLThenStartActivity() {
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable checkWL = new Runnable() {
            @Override
            public void run() {
                if (DataSaved.isWL == EXCAVATOR ||
                        DataSaved.isWL == WHEELLOADER ||
                        DataSaved.isWL == DOZER ||
                        DataSaved.isWL == DOZER_SIX ||
                        DataSaved.isWL == GRADER) {
                    if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                        startCorrectActivity();
                    } else {
                        handler.postDelayed(this, 200); // Riprova ogni 200ms
                    }
                } else if (DataSaved.isWL == DRILL || DataSaved.isWL == SOLARDRILL) {
                    if (isFinishedPOINT) {
                        startCorrectActivityDrill();
                    } else {
                        handler.postDelayed(this, 200); // Riprova ogni 200ms
                    }
                }
            }
        };

        handler.post(checkWL);
    }

    private void startCorrectActivity() {
        if (MyApp.visibleActivity == null) return;

        if (!(MyApp.visibleActivity instanceof My3DActivity)) {
            Intent intent;
            intent = new Intent(MyApp.visibleActivity, My3DActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("whats", "whats");
            startActivity(intent);

            MyApp.visibleActivity.finish();
        } else {
            MyApp.visibleActivity.recreate();
        }
    }

    private void startCorrectActivityDrill() {
        if (MyApp.visibleActivity == null) return;
        if (DataSaved.drill_points != null&& !DataSaved.drill_points.isEmpty()) {
            try{
                if (!(MyApp.visibleActivity instanceof Drill_Activity)) {
                    Intent intent;
                    intent = new Intent(MyApp.visibleActivity, Drill_Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("whats", "whats");
                    startActivity(intent);

                    MyApp.visibleActivity.finish();
                } else {
                    MyApp.visibleActivity.recreate();}
            } catch (Exception e) {
                Intent intent;
                intent = new Intent(MyApp.visibleActivity, PickProject.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        } else {
            Intent intent;
            intent = new Intent(MyApp.visibleActivity, PickProject.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    public static void startCRS() {
        String s = MyData.get_String("crs");

        if (s != null) {
            if (!s.equals("UTM") && !s.equals(_NONE)) {
                if (s.equals("150580")) {
                    try {
                        crsFactory = new CRSFactory();
                        ctFactory = new CoordinateTransformFactory();
                        WGS84 = crsFactory.createFromName("epsg:4326");

                        // >>> Non usare Proj4J per EGSA87 con griglia!
                        // Inizializza il nostro trasformatore
                        if (heposTransformer == null) {
                            try {
                                File dEfile = new File(gridFile_GR_dE);
                                File dNfile = new File(gridFile_GR_dN);

                                heposTransformer = new GridShiftTransformer(dEfile, dNfile);
                            } catch (Exception e) {
                                Log.e("GridShift", Log.getStackTraceString(e));
                                heposTransformer = null;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ////////
                    try {
                        result = new ProjCoordinate();
                        resultWgs = new ProjCoordinate();
                        crsFactory = new CRSFactory();
                        ctFactory = new CoordinateTransformFactory();
                        WGS84 = crsFactory.createFromName("epsg:" + "4326");
                        try {
                            UTM = crsFactory.createFromName("epsg:" + DataSaved.S_CRS);
                        } catch (UnsupportedParameterException e) {
                            throw new RuntimeException(e);
                        } catch (InvalidValueException e) {
                        } catch (UnknownAuthorityCodeException e) {
                        }
                        wgsToUtm = ctFactory.createTransform(WGS84, UTM);
                        utmToWgs = ctFactory.createTransform(UTM, WGS84);
                    } catch (Exception e) {
                    }

                    ///////////
                }
            } else if (s.equals(_NONE)) {
                String CRS_ESTERNO = MyData.get_String("CRS_ESTERNO");
                if (CRS_ESTERNO != null) {
                    try {
                        model = LocalizationFactory.fromFile(new File(CRS_ESTERNO));
                    } catch (Exception e) {
                        Log.e("CRS_ESTERNO", Log.getStackTraceString(e));
                    }
                }
            }
            byte speed = 0;
            switch (DataSaved.reqSpeed) {
                case 0:
                    speed = 5;
                    break;
                case 1:
                    speed = 4;
                    break;
                case 2:
                    speed = 3;
                    break;
                case 3:
                    speed = 0;
                    break;

            }
            DataSaved.gpsOk = false;


            MyDeviceManager.CanWrite(true, 0, 0x18FF0001, 4, new byte[]{0x20, GNSS_MSG, speed, (byte) 0x03});
        }
    }

    private static String normalizeLayerName(String s) {
        if (s == null) return null;
        return s
                .trim()
                .replace("\u00A0", "")  // no-break space
                .replace("\u2007", "")  // figura space
                .replace("\u202F", "")  // narrow no-break space
                .replace("\u2009", "")  // thin space
                .replace("\t", "")
                .replace("\r", "")
                .replace("\n", "");
    }

    public static void reconnectLayers() {

        // Costruisci mappa per avere un solo oggetto Layer per nome normalizzato
        Map<String, Layer> unified = new HashMap<>();

        // Unifica i layer da tutte le liste
        List<Layer> all = new ArrayList<>();
        all.addAll(DataSaved.dxfLayers_DTM);
        all.addAll(DataSaved.dxfLayers_POLY);
        all.addAll(DataSaved.dxfLayers_POINT);

        for (Layer l : all) {
            String key = normalizeLayerName(l.getLayerName());
            if (!unified.containsKey(key)) {
                unified.put(key, l); // il primo diventa quello “ufficiale”
            }
        }

        //  Per debug:
        // System.out.println("Unified Layers: " + unified.keySet());


        // Ricollega i layer reali a TUTTE le entità -------------------------

        // Faces (DTM)
        for (Face3D f : DataSaved.dxfFaces) {
            if (f.getLayer() != null) {
                String key = normalizeLayerName(f.getLayer().getLayerName());
                Layer real = unified.get(key);
                if (real != null) f.setLayer(real);
            }
        }

        // Polilinee 3D
        for (Polyline p : DataSaved.polylines) {
            if (p.getLayer() != null) {
                String key = normalizeLayerName(p.getLayer().getLayerName());
                Layer real = unified.get(key);
                if (real != null) p.setLayer(real);
            }
        }

        // Polilinee 2D
        for (Polyline_2D p : DataSaved.polylines_2D) {
            if (p.getLayer() != null) {
                String key = normalizeLayerName(p.getLayer().getLayerName());
                Layer real = unified.get(key);
                if (real != null) p.setLayer(real);
            }
        }

        // Punti
        for (Point3D p : DataSaved.points) {
            if (p.getLayer() != null) {
                String key = normalizeLayerName(p.getLayer().getLayerName());
                Layer real = unified.get(key);
                if (real != null) p.setLayer(real);
            }
        }

        // Arcs
        for (Arc a : DataSaved.arcs) {
            if (a.getLayer() != null) {
                String key = normalizeLayerName(a.getLayer().getLayerName());
                Layer real = unified.get(key);
                if (real != null) a.setLayer(real);
            }
        }

        // Circles
        for (Circle c : DataSaved.circles) {
            if (c.getLayer() != null) {
                String key = normalizeLayerName(c.getLayer().getLayerName());
                Layer real = unified.get(key);
                if (real != null) c.setLayer(real);
            }
        }
    }

    private void Execute_MC() {
        startCRS();
        if (DataSaved.dxfLayers_DTM == null) {
            DataSaved.dxfLayers_DTM = new ArrayList<>();
        }
        if (DataSaved.dxfLayers_POLY == null) {
            DataSaved.dxfLayers_POLY = new ArrayList<>();
        }
        if (DataSaved.dxfLayers_POINT == null) {
            DataSaved.dxfLayers_POINT = new ArrayList<>();
        }
        String nomeProgettoTRM = MyData.get_String("progettoSelected");
        int lastIndexTRM = nomeProgettoTRM.lastIndexOf(".");
        String fileExtensionTRM = nomeProgettoTRM.substring(lastIndexTRM + 1);

        String nomeProgettoPOLY = MyData.get_String("progettoSelected_POLY");
        mettiPoly = nomeProgettoPOLY != null && !nomeProgettoPOLY.equals("");
        if (nomeProgettoPOLY == null || nomeProgettoPOLY.equals("")) {
            isFinishedPOLY = true;
        }

        String nomeProgettoPOINT = MyData.get_String("progettoSelected_POINT");
        mettiPunti = nomeProgettoPOINT != null && !nomeProgettoPOINT.equals("");
        if (nomeProgettoPOINT == null || nomeProgettoPOINT.equals("")) {
            isFinishedPOINT = true;
        }

        if (MyApp.licenseType > 1) {
            if (!nomeProgettoTRM.equals("")) {
                try {
                    if (mettiPoly) {
                        int lastIndexPOLY = nomeProgettoPOLY.lastIndexOf(".");
                        fileExtensionPOLY = nomeProgettoPOLY.substring(lastIndexPOLY + 1);
                        DataSaved.progettoSelected_POLY = nomeProgettoPOLY;
                    } else {
                        if (DataSaved.polylines != null) {
                            DataSaved.polylines.clear();
                        }
                        if (DataSaved.polylines_2D != null) {
                            DataSaved.polylines_2D.clear();
                        }
                        if (DataSaved.arcs != null) {
                            DataSaved.arcs.clear();
                        }
                        if (DataSaved.circles != null) {
                            DataSaved.circles.clear();
                        }
                        if (DataSaved.lines_2D != null) {
                            DataSaved.lines_2D.clear();
                        }
                        if (DataSaved.dxfLayers_POLY != null) {
                            DataSaved.dxfLayers_POLY.clear();
                        }


                    }
                    if (mettiPunti) {
                        int lastIndexPOINT = nomeProgettoPOINT.lastIndexOf(".");
                        fileExtensionPOINT = nomeProgettoPOINT.substring(lastIndexPOINT + 1);
                        DataSaved.progettoSelected_POINT = nomeProgettoPOINT;
                    } else {
                        if (DataSaved.dxfTexts != null) {
                            DataSaved.dxfTexts.clear();
                        }
                        if (DataSaved.points != null) {
                            DataSaved.points.clear();
                        }
                        if (DataSaved.dxfLayers_POINT != null) {
                            DataSaved.dxfLayers_POINT.clear();
                        }
                    }
                    parserStatus = "Reading TRM...";
                    if (fileExtensionTRM.equalsIgnoreCase("dxf") || fileExtensionTRM.equalsIgnoreCase("pstx")) {
                        if (MyApp.licenseType > 1) {
                            isFinishedDTM = false;

                            DataSaved.projectTAG = "DXF";
                            if (MyData.get_String("ZDXF") == null) {
                                MyData.push("ZDXF", "0.0");
                                DataSaved.offset_Z_antenna = 0;
                            } else {
                                DataSaved.offset_Z_antenna = MyData.get_Double("ZDXF");
                            }
                            try {
                                uom = MyData.get_Int("Unit_Of_Measure");
                            } catch (NumberFormatException e) {
                                uom = 0;
                            }
                            isFeet = uom > 1;
                            if (!DataSaved.lastProjectName.equals(nomeProgettoTRM)) {
                                DataSaved.filteredFaces = new ArrayList<>();
                                DataSaved.dxfFaces = new ArrayList<>();
                                dxfData = DXFParser_20.parseDXF(nomeProgettoTRM, conversionFactor);

                                dxfData = DXFParser_20.parseDXF(nomeProgettoTRM, conversionFactor);
                                DataSaved.dxfFaces = dxfData.getFaces();
                                DataSaved.dxfLayers_DTM = dxfData.getLayers();
                                copiaFacce();
                            } else {
                                isFinishedDTM = true;
                            }
                            if (!DataSaved.lastProjectNamePOLY.equals(nomeProgettoPOLY)) {
                                isFinishedPOLY = false;
                                DataSaved.polylines = new ArrayList<>();
                                DataSaved.polylines_2D = new ArrayList<>();
                                DataSaved.arcs = new ArrayList<>();
                                DataSaved.circles = new ArrayList<>();
                                DataSaved.lines_2D = new ArrayList<>();

                                if (mettiPoly) {
                                    switch (fileExtensionPOLY.toLowerCase()) {
                                        case "dxf":
                                        case "pstx":

                                            parserStatus = "Reading Polylines...";
                                            dxfDataPoly = DXFParser_20.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);

                                            dxfDataPoly = DXFParser_20.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);
                                            DataSaved.polylines = dxfDataPoly.getPolylines();
                                            DataSaved.polylines_2D = dxfDataPoly.getPolylines_2D();
                                            DataSaved.arcs = dxfDataPoly.getArcs();
                                            DataSaved.circles = dxfDataPoly.getCircles();
                                            DataSaved.lines_2D = dxfDataPoly.getLines();
                                            DataSaved.dxfLayers_POLY = dxfDataPoly.getLayers();
                                            copiaPoly();
                                            break;
                                        case "xml":

                                            parserStatus = "Reading Polylines...";

                                            landXMLPOLY = LandXMLParser.parseLandXML(DataSaved.progettoSelected_POLY, 1, conversionFactor);

                                            DataSaved.polylines = landXMLPOLY.getPolylines();
                                            DataSaved.dxfLayers_POLY = landXMLPOLY.getLayers();
                                            copiaPoly();
                                            break;
                                    }

                                }


                            } else {
                                isFinishedPOLY = true;
                            }
                            if (!DataSaved.lastProjectNamePOINT.equals(nomeProgettoPOINT)) {
                                isFinishedPOINT = false;
                                DataSaved.points = new ArrayList<>();
                                DataSaved.dxfTexts = new ArrayList<>();

                                if (mettiPunti) {
                                    switch (fileExtensionPOINT.toLowerCase()) {
                                        case "dxf":
                                        case "pstx":
                                            parserStatus = "Reading Points...";
                                            dxfDataPoint = DXFParser_20.parseDXF(DataSaved.progettoSelected_POINT, conversionFactor);

                                            dxfDataPoint = DXFParser_20.parseDXF(DataSaved.progettoSelected_POINT, conversionFactor);
                                            DataSaved.points = dxfDataPoint.getPoints();
                                            DataSaved.dxfTexts = dxfDataPoint.getTexts();
                                            DataSaved.dxfLayers_POINT = dxfDataPoint.getLayers();
                                            break;
                                        case "xml":
                                            parserStatus = "Reading Points...";
                                            landXMLPOINT = LandXMLParser.parseLandXML(DataSaved.progettoSelected_POINT, 1, conversionFactor);

                                            DataSaved.points = landXMLPOINT.getPoints();
                                            DataSaved.dxfTexts = landXMLPOINT.getTexts();
                                            DataSaved.dxfLayers_POINT = landXMLPOINT.getLayers();
                                            break;

                                        case "csv":
                                            //parsare pnezd
                                            scanPNEZD(conversionFactor);
                                            isFinishedPOINT = true;
                                            My3DActivity.PNEZD_FUNCTION = true;
                                            break;
                                    }
                                }

                            } else {
                                isFinishedPOINT = true;
                            }


                            if (DataSaved.dxfFaces != null && (!DataSaved.lastProjectName.equals(nomeProgettoTRM))) {

                                if (DataSaved.dxfFaces.size() <= 2500) {
                                    DataSaved.RaggioDXF = 10000d;
                                    MyData.push("glFilter", String.valueOf(false));
                                } else {

                                    DataSaved.RaggioDXF = 40;
                                    MyData.push("glFilter", String.valueOf(true));
                                }
                            }
                            // Imposta isReading su false
                            if (DataSaved.polylines == null && DataSaved.points == null) {
                                DataSaved.isAutoSnap = 0;
                            }

                            DataSaved.lastProjectName = nomeProgettoTRM;
                            DataSaved.lastProjectNamePOLY = nomeProgettoPOLY;
                            DataSaved.lastProjectNamePOINT = nomeProgettoPOINT;


                            waitForWLThenStartActivity();


                        } else {
                            goToLicense();
                        }

                    } else if (fileExtensionTRM.equalsIgnoreCase("xml")) {
                        if (MyApp.licenseType > 1) {

                            isFinishedDTM = false;
                            DataSaved.projectTAG = "XML";
                            if (MyData.get_String("ZDXF") == null) {
                                MyData.push("ZDXF", "0.0");
                                DataSaved.offset_Z_antenna = 0;
                            } else {
                                DataSaved.offset_Z_antenna = MyData.get_Double("ZDXF");
                            }
                            try {
                                uom = MyData.get_Int("Unit_Of_Measure");
                            } catch (NumberFormatException e) {
                                uom = 0;
                            }
                            isFeet = uom > 1;
                            parserStatus = "Reading TRM...";
                            if (!DataSaved.lastProjectName.equals(nomeProgettoTRM)) {
                                DataSaved.filteredFaces = new ArrayList<>();
                                DataSaved.dxfFaces = new ArrayList<>();


                                landXMLData = LandXMLParser.parseLandXML(nomeProgettoTRM, 1, conversionFactor);

                                landXMLData = LandXMLParser.parseLandXML(nomeProgettoTRM, 1, conversionFactor);


                                DataSaved.dxfFaces = landXMLData.getFaces();
                                DataSaved.dxfLayers_DTM = landXMLData.getLayers();
                                copiaFacce();
                            } else {
                                isFinishedDTM = true;
                            }
                            if (!DataSaved.lastProjectNamePOLY.equals(nomeProgettoPOLY)) {
                                isFinishedPOLY = false;
                                DataSaved.polylines = new ArrayList<>();
                                DataSaved.polylines_2D = new ArrayList<>();
                                DataSaved.arcs = new ArrayList<>();
                                DataSaved.circles = new ArrayList<>();
                                DataSaved.lines_2D = new ArrayList<>();
                                if (mettiPoly) {
                                    switch (fileExtensionPOLY.toLowerCase()) {
                                        case "dxf":
                                        case "pstx":
                                            parserStatus = "Reading Polylines...";
                                            dxfDataPoly = DXFParser_20.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);

                                            dxfDataPoly = DXFParser_20.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);
                                            DataSaved.polylines = dxfDataPoly.getPolylines();
                                            DataSaved.polylines_2D = dxfDataPoly.getPolylines_2D();
                                            DataSaved.arcs = dxfDataPoly.getArcs();
                                            DataSaved.circles = dxfDataPoly.getCircles();
                                            DataSaved.lines_2D = dxfDataPoly.getLines();
                                            DataSaved.dxfLayers_POLY = dxfDataPoly.getLayers();
                                            copiaPoly();
                                            break;
                                        case "xml":
                                            parserStatus = "Reading Polylines...";

                                            landXMLPOLY = LandXMLParser.parseLandXML(DataSaved.progettoSelected_POLY, 1, conversionFactor);

                                            DataSaved.polylines = landXMLPOLY.getPolylines();
                                            DataSaved.dxfLayers_POLY = landXMLPOLY.getLayers();
                                            copiaPoly();
                                            break;
                                    }

                                }


                            } else {
                                isFinishedPOLY = true;
                            }
                            if (!DataSaved.lastProjectNamePOINT.equals(nomeProgettoPOINT)) {
                                isFinishedPOINT = false;
                                DataSaved.points = new ArrayList<>();
                                DataSaved.dxfTexts = new ArrayList<>();

                                if (mettiPunti) {
                                    switch (fileExtensionPOINT.toLowerCase()) {
                                        case "dxf":
                                        case "pstx":
                                            parserStatus = "Reading Points...";
                                            dxfDataPoint = DXFParser_20.parseDXF(DataSaved.progettoSelected_POINT, conversionFactor);
                                            dxfDataPoint = DXFParser_20.parseDXF(DataSaved.progettoSelected_POINT, conversionFactor);
                                            DataSaved.points = dxfDataPoint.getPoints();
                                            DataSaved.dxfTexts = dxfDataPoint.getTexts();
                                            DataSaved.dxfLayers_POINT = dxfDataPoint.getLayers();


                                            break;
                                        case "xml":
                                            parserStatus = "Reading Points...";
                                            landXMLPOINT = LandXMLParser.parseLandXML(DataSaved.progettoSelected_POINT, 1, conversionFactor);
                                            DataSaved.points = landXMLPOINT.getPoints();
                                            DataSaved.dxfTexts = landXMLPOINT.getTexts();
                                            DataSaved.dxfLayers_POINT = landXMLPOINT.getLayers();
                                            break;
                                        case "csv":
                                            //parsare pnezd
                                            scanPNEZD(conversionFactor);
                                            isFinishedPOINT = true;
                                            My3DActivity.PNEZD_FUNCTION = true;
                                            break;
                                    }
                                }

                            } else {
                                isFinishedPOINT = true;
                            }


                            if (DataSaved.dxfFaces != null && (!DataSaved.lastProjectName.equals(nomeProgettoTRM))) {
                                if (DataSaved.dxfFaces.size() <= 2500) {
                                    DataSaved.RaggioDXF = 10000d;
                                    MyData.push("glFilter", String.valueOf(false));
                                } else {
                                    DataSaved.RaggioDXF = 40;
                                    MyData.push("glFilter", String.valueOf(true));
                                }
                            }

                            // Imposta isReading su false
                            if (DataSaved.polylines == null && DataSaved.points == null) {
                                DataSaved.isAutoSnap = 0;
                            }

                            DataSaved.lastProjectName = nomeProgettoTRM;
                            DataSaved.lastProjectNamePOLY = nomeProgettoPOLY;
                            DataSaved.lastProjectNamePOINT = nomeProgettoPOINT;


                            waitForWLThenStartActivity();


                        } else {
                            goToLicense();
                        }

                    } else {

                        DataSaved.isAutoSnap = 0;
                        Intent intent = new Intent(MyApp.visibleActivity, PickProject.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        MyApp.visibleActivity.finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    DataSaved.isAutoSnap = 0;
                    ERRORE_PROGETTO = true;

                    DataSaved.offset_Z_antenna = 0;
                    Intent intent = new Intent(MyApp.visibleActivity, PickProject.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    MyApp.visibleActivity.finish();

                }
            } else {

                DataSaved.isAutoSnap = 0;

                Intent intent = new Intent(MyApp.visibleActivity, PickProject.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                MyApp.visibleActivity.finish();

            }

        } else {

            goToLicense();
        }

    }

    private void Excecute_DRILL() {

        startCRS();
        String nomeProgettoPOINT = MyData.get_String("progettoSelected_POINT");

        mettiPunti = nomeProgettoPOINT != null && !nomeProgettoPOINT.equals("");
        if (nomeProgettoPOINT == null || nomeProgettoPOINT.equals("")) {
            isFinishedPOINT = true;
        }

        String nomeProgettoPOLY = MyData.get_String("progettoSelected_POLY");
        mettiPoly = nomeProgettoPOLY != null && !nomeProgettoPOLY.equals("");
        if (nomeProgettoPOLY == null || nomeProgettoPOLY.equals("")) {
            isFinishedPOLY = true;
        }
        if (MyApp.licenseType > 1) {
            if (nomeProgettoPOINT != null && !nomeProgettoPOINT.equals("")) {
                //TODO Read DRrill
                try {
                    if (mettiPoly) {
                        int lastIndexPOLY = nomeProgettoPOLY.lastIndexOf(".");
                        fileExtensionPOLY = nomeProgettoPOLY.substring(lastIndexPOLY + 1);
                        DataSaved.progettoSelected_POLY = nomeProgettoPOLY;
                    } else {
                        if (DataSaved.polylines != null) {
                            DataSaved.polylines.clear();
                        }
                        if (DataSaved.polylines_2D != null) {
                            DataSaved.polylines_2D.clear();
                        }
                        if (DataSaved.arcs != null) {
                            DataSaved.arcs.clear();
                        }
                        if (DataSaved.circles != null) {
                            DataSaved.circles.clear();
                        }
                        if (DataSaved.lines_2D != null) {
                            DataSaved.lines_2D.clear();
                        }
                        if (DataSaved.dxfLayers_POLY != null) {
                            DataSaved.dxfLayers_POLY.clear();
                        }


                    }
                    if (mettiPunti) {
                        int lastIndexPOINT = nomeProgettoPOINT.lastIndexOf(".");
                        fileExtensionPOINT = nomeProgettoPOINT.substring(lastIndexPOINT + 1);
                        DataSaved.progettoSelected_POINT = nomeProgettoPOINT;
                    } else {

                        if (DataSaved.drill_points != null) {
                            DataSaved.drill_points.clear();
                        }
                        if (DataSaved.dxfLayers_POINT != null) {
                            DataSaved.dxfLayers_POINT.clear();
                        }
                    }
                    parserStatus = "Reading TRM...";
                    try {
                        uom = MyData.get_Int("Unit_Of_Measure");
                    } catch (NumberFormatException e) {
                        uom = 0;
                    }
                    isFeet = uom > 1;

                    Log.d("RESD",DataSaved.lastProjectNamePOINT+"\n"+nomeProgettoPOINT);
                    if (!DataSaved.lastProjectNamePOINT.equals(nomeProgettoPOINT)) {
                        isFinishedPOINT = false;
                        DataSaved.drill_points = new ArrayList<>();


                        if (mettiPunti) {
                            DataSaved.Selected_Point3D_Drill=null;
                            switch (fileExtensionPOINT.toLowerCase()) {
                               /* case "dxf":
                                    parserStatus = "Reading Points...";
                                    //TODO DXFPOINTS
                                    break;*/
                                case "xml":
                                    //TODO CGPOINTS
                                    parserStatus = "Reading Points...";
                                    landXMLPOINT = LandXMLParser.parseLandXML(DataSaved.progettoSelected_POINT, DataSaved.xyz_yxz, conversionFactor);
                                    DataSaved.drill_points = landXMLPOINT.getDrillPoints();
                                    break;

                                case "csv":
                                    //parsare pnezd o gestire i csv in maniera efficace
                                    parserStatus = "Reading Points...";
                                    File f = new File(DataSaved.progettoSelected_POINT);
                                    DataSaved.drill_points = DrillCSVParser.parse(f, 0, DataSaved.xyz_yxz, conversionFactor);

                                    break;
                                case "ird":
                                    //TODO parsare IREDES
                                    parserStatus = "Reading Points...";
                                    DataSaved.drill_points = IrdParser.parseIrd(DataSaved.progettoSelected_POINT, DataSaved.xyz_yxz, conversionFactor);
                                    break;
                                default:
                                    isFinishedPOINT=true;
                                    DataSaved.isAutoSnap = 0;
                                    ERRORE_PROGETTO = true;

                                    Intent intent = new Intent(MyApp.visibleActivity, PickProject.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    MyApp.visibleActivity.finish();
                                    break;
                            }
                        }
                        DataSaved.lastProjectNamePOINT=nomeProgettoPOINT;
                    } else {
                        isFinishedPOINT = true;
                    }
                    if (!DataSaved.lastProjectNamePOLY.equals(nomeProgettoPOLY)) {
                        isFinishedPOLY = false;
                        DataSaved.polylines = new ArrayList<>();
                        DataSaved.polylines_2D = new ArrayList<>();
                        DataSaved.arcs = new ArrayList<>();
                        DataSaved.circles = new ArrayList<>();
                        DataSaved.lines_2D = new ArrayList<>();

                        if (mettiPoly) {
                            switch (fileExtensionPOLY.toLowerCase()) {
                                case "dxf":
                                    parserStatus = "Reading Polylines...";
                                    dxfDataPoly = DXFParser_20.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);

                                    dxfDataPoly = DXFParser_20.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);
                                    DataSaved.polylines = dxfDataPoly.getPolylines();
                                    DataSaved.polylines_2D = dxfDataPoly.getPolylines_2D();
                                    DataSaved.arcs = dxfDataPoly.getArcs();
                                    DataSaved.circles = dxfDataPoly.getCircles();
                                    DataSaved.lines_2D = dxfDataPoly.getLines();
                                    DataSaved.dxfLayers_POLY = dxfDataPoly.getLayers();
                                    copiaPoly();
                                    break;
                                case "xml":
                                    parserStatus = "Reading Polylines...";
                                    landXMLPOLY = LandXMLParser.parseLandXML(DataSaved.progettoSelected_POLY, 1, conversionFactor);
                                    DataSaved.polylines = landXMLPOLY.getPolylines();
                                    DataSaved.dxfLayers_POLY = landXMLPOLY.getLayers();
                                    copiaPoly();
                                    break;
                            }

                        }


                    } else {
                        isFinishedPOLY = true;
                    }


                } catch (Exception e) {
                    e.printStackTrace();

                    DataSaved.isAutoSnap = 0;
                    ERRORE_PROGETTO = true;

                    Intent intent = new Intent(MyApp.visibleActivity, PickProject.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    MyApp.visibleActivity.finish();
                }
            } else {

                DataSaved.isAutoSnap = 0;

                Intent intent = new Intent(MyApp.visibleActivity, PickProject.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                MyApp.visibleActivity.finish();

            }
            DataSaved.lastProjectName = DataSaved.lastProjectNamePOINT;
            waitForWLThenStartActivity();
        } else {
            goToLicense();
        }

    }

    private void generaReport(){
        //TODO
       /* File outDir = new File(basePath + "/Exported/" + projectFolderName + "_OUT");

        ProjectReportCsvWriter writer = new ProjectReportCsvWriter(outDir, projectFolderName);

        LinkedHashMap<String, String> preamble = new LinkedHashMap<>();
        preamble.put("Company", DataSaved.companyName);
        preamble.put("Machine", DataSaved.machineName);
        preamble.put("Project", projectFolderName);

        writer.initReport(preamble);*/
    }
    private void generaState(){
      /*  File outDir = new File(basePath + "/Exported/" + projectFolderName + "_OUT");

        ProjectStateCsvStore stateStore = new ProjectStateCsvStore(outDir, projectFolderName);
        stateStore.initAndLoad(); // crea file se non esiste + carica cache

// Poi, quando costruisci/riempi drill_points:
// per ogni punto, applichi lo stato salvato:
        for (Point3D_Drill p : DataSaved.drill_points) {
            String id = p.getRowId()+p.getId();
            ProjectStateCsvStore.HoleState st = stateStore.getState(id);

            // mappa sui tuoi status int: 0=TODO, 1=DONE, 2=ABORTED (esempio)
            if (st == ProjectStateCsvStore.HoleState.DONE) p.setStatus(1);
            else if (st == ProjectStateCsvStore.HoleState.ABORTED) p.setStatus(2);
            else p.setStatus(0);
        }*/
    }
}