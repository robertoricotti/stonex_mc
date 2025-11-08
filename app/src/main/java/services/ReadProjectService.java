package services;


import static gui.MyApp.gridFile_GR_dE;
import static gui.MyApp.gridFile_GR_dN;
import static gui.MyApp.heposTransformer;
import static packexcalib.gnss.CRS_Strings._NONE;
import static services.TriangleService.scanPNEZD;
import static services.UpdateValuesService.UTM;
import static services.UpdateValuesService.WGS84;
import static services.UpdateValuesService.crsFactory;
import static services.UpdateValuesService.ctFactory;
import static services.UpdateValuesService.result;
import static services.UpdateValuesService.resultWgs;
import static services.UpdateValuesService.utmToWgs;
import static services.UpdateValuesService.wgsToUtm;

import android.app.Service;
import android.content.Context;
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
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dxf.DXFData;
import dxf.DXFParser;
import dxf.Face3D;
import dxf.Point3D;
import dxf.Polyline;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.my_opengl.My3DActivity;
import gui.projects.PickProject;
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
    static double conversionFactor = 1;
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
                                    dxfData = DXFParser.parseDXF(nomeProgettoTRM, conversionFactor);

                                    dxfData = DXFParser.parseDXF(nomeProgettoTRM, conversionFactor);
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
                                                dxfDataPoly = DXFParser.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);

                                                dxfDataPoly = DXFParser.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);
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
                                                dxfDataPoint = DXFParser.parseDXF(DataSaved.progettoSelected_POINT, conversionFactor);

                                                dxfDataPoint = DXFParser.parseDXF(DataSaved.progettoSelected_POINT, conversionFactor);
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
                                                My3DActivity.PNEZD_FUNCTION=true;
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
                                                dxfDataPoly = DXFParser.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);

                                                dxfDataPoly = DXFParser.parseDXF(DataSaved.progettoSelected_POLY, conversionFactor);
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
                                                dxfDataPoint = DXFParser.parseDXF(DataSaved.progettoSelected_POINT, conversionFactor);
                                                dxfDataPoint = DXFParser.parseDXF(DataSaved.progettoSelected_POINT, conversionFactor);
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
                                                My3DActivity.PNEZD_FUNCTION=true;
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                    startCorrectActivity();
                } else {
                    handler.postDelayed(this, 200); // Riprova ogni 200ms
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

    public static void clearCache(Context context, String projectName) {
        File file = new File(context.getFilesDir(), projectName + ".ser");
        if (file.exists()) file.delete();
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
                                Log.e("GridShift",Log.getStackTraceString(e));
                                heposTransformer = null;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
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
            }else if(s.equals(_NONE)){
                String CRS_ESTERNO=MyData.get_String("CRS_ESTERNO");
                if(CRS_ESTERNO!=null){
                    try {
                        model= LocalizationFactory.fromFile(new File(CRS_ESTERNO));
                    } catch (Exception e) {
                        Log.e("CRS_ESTERNO",Log.getStackTraceString(e));
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
            byte msg = 0x01;

            MyDeviceManager.CanWrite(true,0, 0x18FF0001, 4, new byte[]{0x20, msg, speed, (byte) 0x03});
        }
    }
}