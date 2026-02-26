package gui.boot_and_choose;

import static gui.MyApp.gridFile_GR_dE;
import static gui.MyApp.gridFile_GR_dN;
import static gui.MyApp.heposTransformer;
import static packexcalib.gnss.CRS_Strings._150580;
import static packexcalib.gnss.CRS_Strings._NONE;
import static packexcalib.gnss.CRS_Strings._UTM;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stx_dig.R;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.InvalidValueException;
import org.locationtech.proj4j.ProjCoordinate;
import org.locationtech.proj4j.UnknownAuthorityCodeException;
import org.locationtech.proj4j.UnsupportedParameterException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

import gui.MyApp;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.CoordinateXYZ;
import packexcalib.gnss.CzechGridShiftTransformer;
import packexcalib.gnss.Deg2UTM;
import packexcalib.gnss.GGFGeoide;
import packexcalib.gnss.GeoidBinLite;
import packexcalib.gnss.GeoideInterpolation;
import packexcalib.gnss.GridShiftTransformer;
import packexcalib.gnss.LocalizationFactory;
import packexcalib.gnss.LocalizationModel;
import packexcalib.gnss.Ntv2GsbMetersGrid;
import utils.MyData;

/**
 * Activity di test per SpLocalization:
 * Permette di caricare un file .SP e testare le trasformazioni
 * diretta (Lat,Lon,H → E,N,Z) e inversa (E,N,Z → Lat,Lon,H).
 */
public class SpTestActivity extends Activity {
    // --- CRS 5514 base (quello che matcha epsg.io) ---
    private static final String EPSG5514_BASE =
            "+proj=krovak +lat_0=49.5 +lon_0=24.8333333333333 +alpha=30.2881397527778 " +
                    "+k=0.9999 +x_0=0 +y_0=0 +ellps=bessel " +
                    "+towgs84=572.213,85.334,461.94,4.9732,1.529,5.2484,3.5378 " +
                    "+units=m +no_defs";

    // --- Cache griglie CZ (caricate da assets) ---
    private static CzechGridShiftTransformer czQ1;
    private static CzechGridShiftTransformer czQ3;

    // --- Cache transform WGS84 -> 5514 base ---
    private static CoordinateTransform wgsTo5514Base;
    CoordinateXYZ coordinateXYZ;
    private final double[] quotaBuf = new double[1];
    boolean ggfReady;
    private static GeoideInterpolation UGF_READER;
    private static String UGF_PATH_LOADED;
    public static boolean ugfReady;

    private static GeoidBinLite BIN_READER;
    private static String BIN_PATH_LOADED;
    public static boolean binReady;

    private static GGFGeoide GGF_READER;
    private static String GGF_PATH_LOADED;
    CRSFactory mycrsFactory;
    CoordinateReferenceSystem myWGS84, myUTM;
    CoordinateTransformFactory myctFactory;
    CoordinateTransform mywgsToUtm, myutmToWgs;
    ProjCoordinate myshifted, myresult, myresultWgs;


    public static String sN, sE, sH, sLat, sLon, sHll;
    LocalizationModel localizationFactory;
    private EditText etLat, etLon, etH;
    private EditText etEst, etNord, etZ;
    private TextView tvOutput;
    private Button btnLoad, btnToLocal, btnToGeo, btn_Exit;
    int tempCom=0;
    int previousCom;
    static final String _5514_param=
            "+proj=krovak +lat_0=49.5 +lon_0=24.8333333333333 +alpha=30.2881397527778 " +
            "+k=0.9999 +x_0=0 +y_0=0 +ellps=bessel +towgs84=589,76,480,0,0,0,0 " +
            "+units=m +no_defs";

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sp_test);
        DataSaved.my_comPort=tempCom;
        previousCom=DataSaved.my_comPort;
        myresult = new ProjCoordinate();
        myshifted = new ProjCoordinate();
        myresultWgs = new ProjCoordinate();

        updateLLQ();

        etLat = findViewById(R.id.etLat);
        etLon = findViewById(R.id.etLon);
        etH = findViewById(R.id.etH);
        etEst = findViewById(R.id.etEst);
        etNord = findViewById(R.id.etNord);
        etZ = findViewById(R.id.etZ);
        btn_Exit = findViewById(R.id.btn_Exit);
        tvOutput = findViewById(R.id.tvOutput);
        btnLoad = findViewById(R.id.btnLoad);
        btnToLocal = findViewById(R.id.btnTransform);
        btnToGeo = findViewById(R.id.btnInverse);
        btnToGeo.setVisibility(TextView.INVISIBLE);
        etEst.setText("");
        etNord.setText("");
        etZ.setText("");

        try {

            etLat.setText(sLat.replace(",","."));
            etLon.setText(sLon.replace(",","."));
            etH.setText(sHll.replace(",","."));

        } catch (Exception ignored) {

        }


        btn_Exit.setOnClickListener(view -> {
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });


        btnLoad.setOnClickListener(v -> {
            init();
        });

        btnToLocal.setOnClickListener(v -> {



            if (DataSaved.S_CRS.equals(_NONE)) {
                if (localizationFactory == null) {
                    toast("Load  file .SP - .LOC");
                    return;
                }
                try {
                    double lat = Double.parseDouble(etLat.getText().toString().replace(",", "."));
                    double lon = Double.parseDouble(etLon.getText().toString().replace(",", "."));
                    double h = Double.parseDouble(etH.getText().toString().replace(",", "."));
                    double[] out = new double[3];
                    localizationFactory.toLocalFast(lat, lon, h, out);
                    double Hq = ramoGeoide(lat, lon, out[2]);
                    sE = String.format("%.3f", out[0]).replace(",", ".");
                    sN = String.format("%.3f", out[1]).replace(",", ".");
                    sH = String.format("%.3f", Hq).replace(",", ".");
                    etEst.setText(sE);
                    etNord.setText(sN);
                    etZ.setText(sH);
                    log(String.format("→ Locale: E=%.3f  N=%.3f  Z=%.3f", out[0], out[1], Hq).replaceAll(",", "."));
                    MyData.push("Test_sLat",String.format("%.9f", lat));
                    MyData.push("Test_sLon",String.format("%.9f", lon));
                    MyData.push("Test_sHll",String.format("%.3f", h));
                    updateLLQ();
                } catch (Exception e) {
                    log("Error: " + e.getMessage());
                }
            }
            else {
                try {

                    setEPSGLocal();
                    double lat = Double.parseDouble(etLat.getText().toString().replace(",", "."));
                    double lon = Double.parseDouble(etLon.getText().toString().replace(",", "."));
                    double h = Double.parseDouble(etH.getText().toString().replace(",", "."));
                    double Easting, Northing, Quota;
                    coordinateXYZ=Deg2UTM.trasform(lat,lon,h,_UTM);
                    switch (DataSaved.S_CRS){

                        case "5514":
                        case "150583":
                            CRSFactory crsFactory = new CRSFactory();
                            CoordinateReferenceSystem wgs84 = crsFactory.createFromName("epsg:4326");

                            String epsg5514 =
                                    "+proj=krovak +lat_0=49.5 +lon_0=24.8333333333333 +alpha=30.2881397527778 " +
                                            "+k=0.9999 +x_0=0 +y_0=0 +ellps=bessel " +
                                            "+towgs84=572.213,85.334,461.94,4.9732,1.529,5.2484,3.5378 " +
                                            "+units=m +no_defs";

                            CoordinateReferenceSystem sjtsk5514 = crsFactory.createFromParameters("EPSG:5514", epsg5514);

                            CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
                            CoordinateTransform t = ctFactory.createTransform(wgs84, sjtsk5514);

                            // input: lon,lat (EPSG:4326)
                            ProjCoordinate in = new ProjCoordinate(lon, lat);
                            ProjCoordinate out = new ProjCoordinate();
                            t.transform(in, out);
                            if(DataSaved.S_CRS.equals("5514")) {
                                Easting = out.x;
                                Northing = out.y;
                            }else {
                                Easting = -out.x;
                                Northing = -out.y;
                            }
                            Quota = ramoGeoide(lat, lon, h);
                            sE = String.format("%.3f", Easting).replace(",", ".");
                            sN = String.format("%.3f", Northing).replace(",", ".");
                            sH = String.format("%.3f", Quota).replace(",", ".");
                            etEst.setText(sE);
                            etNord.setText(sN);
                            etZ.setText(sH);
                            MyData.push("Test_sLat", String.format("%.9f", lat));
                            MyData.push("Test_sLon", String.format("%.9f", lon));
                            MyData.push("Test_sHll", String.format("%.3f", h));
                            updateLLQ();
                            break;
                        case "5513":
                            CRSFactory crsFactory5513 = new CRSFactory();
                            CoordinateReferenceSystem wgs845513 = crsFactory5513.createFromName("epsg:4326");

                            String epsg5513 =
                                    "+proj=krovak +axis=swu +lat_0=49.5 +lon_0=24.8333333333333 +alpha=30.2881397527778 " +
                                            "+k=0.9999 +x_0=0 +y_0=0 +ellps=bessel " +
                                            "+towgs84=572.213,85.334,461.94,4.9732,1.529,5.2484,3.5378 " +
                                            "+units=m +no_defs";

                            CoordinateReferenceSystem sjtsk5513 = crsFactory5513.createFromParameters("EPSG:5514", epsg5513);

                            CoordinateTransformFactory ctFactory5513 = new CoordinateTransformFactory();
                            CoordinateTransform t5513 = ctFactory5513.createTransform(wgs845513, sjtsk5513);

                            // input: lon,lat (EPSG:4326)
                            ProjCoordinate in5513 = new ProjCoordinate(lon, lat);
                            ProjCoordinate out5513 = new ProjCoordinate();
                            t5513.transform(in5513, out5513);
                            Easting = out5513.x;
                            Northing = out5513.y;
                            Quota = ramoGeoide(lat, lon, h);
                            sE = String.format("%.3f", Easting).replace(",", ".");
                            sN = String.format("%.3f", Northing).replace(",", ".");
                            sH = String.format("%.3f", Quota).replace(",", ".");
                            etEst.setText(sE);
                            etNord.setText(sN);
                            etZ.setText(sH);
                            MyData.push("Test_sLat", String.format("%.9f", lat));
                            MyData.push("Test_sLon", String.format("%.9f", lon));
                            MyData.push("Test_sHll", String.format("%.3f", h));
                            updateLLQ();
                            break;
                        case _150580:
                            if (heposTransformer != null) {
                                myshifted = heposTransformer.transform(lat, lon, h);
                                Easting = myshifted.x;
                                Northing = myshifted.y + 2000000;
                                Quota = myshifted.z;
                                sE = String.format("%.3f", Easting).replace(",", ".");
                                sN = String.format("%.3f", Northing).replace(",", ".");
                                sH = String.format("%.3f", Quota).replace(",", ".");
                                etEst.setText(sE);
                                etNord.setText(sN);
                                etZ.setText(sH);
                                MyData.push("Test_sLat", String.format("%.9f", lat));
                                MyData.push("Test_sLon", String.format("%.9f", lon));
                                MyData.push("Test_sHll", String.format("%.3f", h));
                                updateLLQ();
                            }
                            break;
                        case "150581":
                            ensureCzechTransformsReady();
                            if (wgsToKrovakGRS80 == null || czQ1 == null) {
                                toast("CZ grid Q1 non disponibile (assets mancanti?)");
                                break;
                            }

                            ProjCoordinate in1 = new ProjCoordinate(lon, lat);
                            ProjCoordinate out1 = new ProjCoordinate();
                            wgsToKrovakGRS80.transform(in1, out1);

                            // Applica griglia v1710 Quadrant 1 (in metri su E/N)
                            // Se la tua CzechGridShiftTransformer al momento accetta double[],
                            // usa questo; se hai la versione applyInPlace(ProjCoordinate), ancora meglio.
                            double[] en = { out1.x, out1.y };
                            czQ1.applyInPlace(en);
                            Easting = en[0];
                            Northing = en[1];

                            Quota = ramoGeoide(lat, lon, h);

                            sE = String.format("%.3f", Easting).replace(",", ".");
                            sN = String.format("%.3f", Northing).replace(",", ".");
                            sH = String.format("%.3f", Quota).replace(",", ".");
                            etEst.setText(sE);
                            etNord.setText(sN);
                            etZ.setText(sH);

                            MyData.push("Test_sLat", String.format("%.9f", lat));
                            MyData.push("Test_sLon", String.format("%.9f", lon));
                            MyData.push("Test_sHll", String.format("%.3f", h));
                            updateLLQ();
                            break;


                        case "150582":
                            ensureCzechTransformsReady();
                            if (wgsToKrovakGRS80 == null || czQ3 == null) {
                                toast("CZ grid Q3 non disponibile (assets mancanti?)");
                                break;
                            }

                            ProjCoordinate in3 = new ProjCoordinate(lon, lat);
                            ProjCoordinate out3 = new ProjCoordinate();
                            wgsToKrovakGRS80.transform(in3, out3);

                            // Applica griglia v1710 Quadrant 3
                            double[] en3 = { out3.x, out3.y };
                            czQ3.applyInPlace(en3);
                            Easting = en3[0];
                            Northing = en3[1];

                            Quota = ramoGeoide(lat, lon, h);

                            sE = String.format("%.3f", Easting).replace(",", ".");
                            sN = String.format("%.3f", Northing).replace(",", ".");
                            sH = String.format("%.3f", Quota).replace(",", ".");
                            etEst.setText(sE);
                            etNord.setText(sN);
                            etZ.setText(sH);

                            MyData.push("Test_sLat", String.format("%.9f", lat));
                            MyData.push("Test_sLon", String.format("%.9f", lon));
                            MyData.push("Test_sHll", String.format("%.3f", h));
                            updateLLQ();
                            break;

                        case _UTM:
                            Easting =coordinateXYZ.getEasting();
                            Northing = coordinateXYZ.getNorthing();
                            Quota = ramoGeoide(lat, lon, h);
                            sE = String.format("%.3f", Easting).replace(",", ".");
                            sN = String.format("%.3f", Northing).replace(",", ".");
                            sH = String.format("%.3f", Quota).replace(",", ".");
                            etEst.setText(sE);
                            etNord.setText(sN);
                            etZ.setText(sH);
                            MyData.push("Test_sLat", String.format("%.9f", lat));
                            MyData.push("Test_sLon", String.format("%.9f", lon));
                            MyData.push("Test_sHll", String.format("%.3f", h));
                            updateLLQ();
                            break;
                        default:
                            if (mywgsToUtm != null && myresult != null) {
                                mywgsToUtm.transform(new ProjCoordinate(lon, lat, h), myresult);
                                Easting = myresult.x;
                                Northing = myresult.y;
                                Quota = ramoGeoide(lat, lon, h);
                                sE = String.format("%.3f", Easting).replace(",", ".");
                                sN = String.format("%.3f", Northing).replace(",", ".");
                                sH = String.format("%.3f", Quota).replace(",", ".");
                                etEst.setText(sE);
                                etNord.setText(sN);
                                etZ.setText(sH);
                                MyData.push("Test_sLat", String.format("%.9f", lat));
                                MyData.push("Test_sLon", String.format("%.9f", lon));
                                MyData.push("Test_sHll", String.format("%.3f", h));
                                updateLLQ();
                            }
                            break;
                    }

                } catch (Exception e) {
                    log("Error: " + e.getMessage());
                }
            }


        });

        btnToGeo.setOnClickListener(v -> {
            if (localizationFactory == null) {
                toast("Load  file .SP - .LOC");
                return;
            }
            try {
                double e = Double.parseDouble(etEst.getText().toString());
                double n = Double.parseDouble(etNord.getText().toString());
                double z = Double.parseDouble(etZ.getText().toString());
                double[] out = new double[3];
                localizationFactory.toGeoFast(e, n, z, out);
                sLat = String.format("%.9f", out[0]).replace(",", ".");
                sLon = String.format("%.9f", out[1]).replace(",", ".");
                sHll = String.format("%.3f", out[2]).replace(",", ".");
                etLat.setText(sLat.replace(",","."));
                etLon.setText(sLon.replace(",","."));
                etH.setText(sHll.replace(",","."));
                log(String.format("→ Geo: Lat=%.8f  Lon=%.8f  H=%.3f", out[0], out[1], out[2]).replaceAll(",", "."));
            } catch (Exception e) {
                log("Error: " + e.getMessage());
            }
        });

        init();

        try {
            etEst.setText(sE);
            etNord.setText(sN);
            etZ.setText(sH);


        } catch (Exception ignored) {

        }
    }

    private void log(String s) {
        tvOutput.append(s + "\n");
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private static CoordinateTransform wgsToKrovakGRS80;

    private void ensureCzechTransformsReady() {
        Log.d("CZ", "ensureCzechTransformsReady() start");

        try {
            if (wgsToKrovakGRS80 == null) {
                Log.d("CZ", "init wgsToKrovakGRS80...");
                CRSFactory crsFactory = new CRSFactory();
                CoordinateReferenceSystem wgs84 = crsFactory.createFromName("epsg:4326");

                String krovakGRS80 =
                        "+proj=krovak +lat_0=49.5 +lon_0=24.8333333333333 +alpha=30.2881397527778 " +
                                "+k=0.9999 +x_0=0 +y_0=0 +ellps=GRS80 +units=m +no_defs";

                CoordinateReferenceSystem krovak = crsFactory.createFromParameters("KROVAK_GRS80", krovakGRS80);
                CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
                wgsToKrovakGRS80 = ctFactory.createTransform(wgs84, krovak);

                Log.d("CZ", "wgsToKrovakGRS80 OK");
            }

        } catch (Exception e) {
            Log.e("CZ", "ERRORE init wgsToKrovakGRS80", e);
        }

        try {
            if (czQ1 == null) {
                Log.d("CZ", "loading asset Q1...");
                // DEBUG: lista assets root
                String[] list = getAssets().list("");
                Log.d("CZ", "assets root = " + java.util.Arrays.toString(list));

                try (InputStream is = getAssets().open("table_yx_3_v1710_Q1.gsb")) {
                    czQ1 = new CzechGridShiftTransformer(is);
                }
                Log.d("CZ", "czQ1 OK");
            }
        } catch (Exception e) {
            Log.e("CZ", "ERRORE open/parse Q1", e);
        }

        try {
            if (czQ3 == null) {
                Log.d("CZ", "loading asset Q3...");
                String[] list = getAssets().list("");
                Log.d("CZ", "assets root = " + java.util.Arrays.toString(list));

                try (InputStream is = getAssets().open("table_yx_3_v1710_Q3.gsb")) {
                    czQ3 = new CzechGridShiftTransformer(is);
                }
                Log.d("CZ", "czQ3 OK");
            }
        } catch (Exception e) {
            Log.e("CZ", "ERRORE open/parse Q3", e);
        }

        Log.d("CZ", "ensure done: wgsToKrovakGRS80=" + (wgsToKrovakGRS80 != null)
                + " czQ1=" + (czQ1 != null) + " czQ3=" + (czQ3 != null));
    }
    private void init() {
        try {

            File file = new File(MyData.get_String("CRS_ESTERNO"));
            localizationFactory = LocalizationFactory.fromFile(file);

            log("✅ File  successfully loaded");
            btnLoad.setText(MyData.get_String("CRS_ESTERNO"));
            log("File loaded: " + localizationFactory.getClass().getSimpleName());
        } catch (Exception e) {
            log("❌ Error loading File: " + e.getMessage());
            btnLoad.setText(e.getMessage());

            try {

                setEPSGLocal();
            } catch (Exception ex) {
                log("Errore: " + e.getMessage());
            }
        }
    }

    private void setEPSGLocal() {
        String s = MyData.get_String("crs");
        if (!s.equals("UTM") && !s.equals(_NONE)) {
            if (s.equals("150580")) {
                try {
                    mycrsFactory = new CRSFactory();
                    myctFactory = new CoordinateTransformFactory();
                    myWGS84 = mycrsFactory.createFromName("epsg:4326");

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
                    myresult = new ProjCoordinate();
                    myresultWgs = new ProjCoordinate();
                    mycrsFactory = new CRSFactory();
                    myctFactory = new CoordinateTransformFactory();
                    myWGS84 = mycrsFactory.createFromName("epsg:" + "4326");
                    try {
                        myUTM = mycrsFactory.createFromName("epsg:" + DataSaved.S_CRS);

                    } catch (UnsupportedParameterException e) {
                        throw new RuntimeException(e);
                    } catch (InvalidValueException e) {
                    } catch (UnknownAuthorityCodeException e) {
                    }
                    mywgsToUtm = myctFactory.createTransform(myWGS84, myUTM);
                    myutmToWgs = myctFactory.createTransform(myUTM, myWGS84);
                } catch (Exception e) {
                }

                ///////////
            }
        }

    }

    private double ramoGeoide(double Lat, double Lon, double Z) {
        double outQ = 0;
        try {

            double q = Z;

            final String path = MyApp.GEOIDE_PATH;
            if (path != null && !path.isEmpty() && !"null".equals(path)) {
                int dot = path.lastIndexOf('.');
                String ext = (dot > 0) ? path.substring(dot + 1).toLowerCase(Locale.ROOT) : "";

                switch (ext.toLowerCase()) {
                    case "bin":
                        try {
                            if (BIN_READER == null || !Objects.equals(BIN_PATH_LOADED, path)) {
                                BIN_READER = new GeoidBinLite(new File(path));
                                BIN_PATH_LOADED = path;
                                //Log.d("Deg2UTM", "BIN letto");
                                binReady = true;
                            }
                            if (binReady && BIN_READER.isInGrid(Lat, Lon)) {
                                q = BIN_READER.getOrthometricHeight(Lat, Lon, Z);

                            } else {

                                q = Z;
                            }
                        } catch (IOException e) {
                            //Log.e("Deg2UTM", "BIN load/use error", e);
                            binReady = false;

                            q = Z;
                        }
                        break;

                    case "ggf":
                        try {
                            if (GGF_READER == null || !Objects.equals(GGF_PATH_LOADED, path)) {
                                GGF_READER = new GGFGeoide();
                                GGF_READER.load(path);

                                GGF_PATH_LOADED = path;
                                ggfReady = true;
                                Log.d("Deg2UTM", "GGF letto "+GGF_PATH_LOADED+" "+ggfReady);
                            }
                            Log.d("Deg2UTM", GGF_READER.isInGrid(Lat, Lon)+"");
                            if (ggfReady && GGF_READER.isInGrid(Lat, Lon)) {
                                double und = GGF_READER.getUndulation(Lat, Lon);
                                q = Double.isNaN(und) ? Z : (Z - und);
                                Log.d("Deg2UTM", "GGF trasformato");

                            } else {

                                q = Z;
                            }
                        } catch (Exception e) {
                            //Log.e("Deg2UTM", "GGF load/use error", e);
                            ggfReady = false;

                            q = Z;
                        }
                        break;

                    case "ugf":
                        try {
                            if (UGF_READER == null || !Objects.equals(UGF_PATH_LOADED, path)) {
                                UGF_READER = new GeoideInterpolation(path);
                                UGF_READER.readHeader();            // una sola volta per path
                                 Log.d("Deg2UTM", "UGF letto");
                                UGF_PATH_LOADED = path;
                                ugfReady = true;
                            }
                            if (ugfReady && UGF_READER != null) {
                                if (UGF_READER.internalLetturaGeoide(Lat, Lon, Z, quotaBuf, false)) {
                                    q = quotaBuf[0];

                                } else {

                                    q = Z;
                                }
                            } else {

                                q = Z;
                            }
                        } catch (Exception e) {
                            //Log.e("Deg2UTM", "UGF load/use error", e);
                            ugfReady = false;

                            q = Z;
                        }
                        break;

                    default:
                        q = Z;

                }
            } else {
                q = Z;

            }

            outQ = q;

        } catch (Exception e) {
            //Log.e("Deg2UTM", "Transform error", e);
            outQ = 0;

        }
        return outQ;
    }

    private void updateLLQ() {

        sLat = MyData.get_String("Test_sLat");
        sLon = MyData.get_String("Test_sLon");
        sHll = MyData.get_String("Test_sHll");
        if (sLat == null) {
            sLat = "43.012345601";
            MyData.push("Test_sLat", sLat);
        } else {
            MyData.push("Test_sLat", sLat);
        }
        if (sLon == null) {
            sLon = "10.012345601";
            MyData.push("Test_sLon", sLon);
        } else {
            MyData.push("Test_sLon", sLon);
        }
        if (sHll == null) {
            sHll = "100.012";
            MyData.push("Test_sHll", sHll);
        } else {
            MyData.push("Test_sHll", sHll);
        }

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataSaved.my_comPort=previousCom;
    }
}