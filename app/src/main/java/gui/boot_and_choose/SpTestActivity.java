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
import packexcalib.gnss.NmeaListener;
import packexcalib.gnss.Ntv2GsbMetersGrid;
import utils.MyData;

/**
 * Activity di test per SpLocalization:
 * Permette di caricare un file .SP e testare le trasformazioni
 * diretta (Lat,Lon,H → E,N,Z) e inversa (E,N,Z → Lat,Lon,H).
 */
public class SpTestActivity extends Activity {

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
    private EditText etEst, etNord, etZ,etDH;
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
        etDH=findViewById(R.id.etDH);
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

                    double[] out = new double[4];
                    localizationFactory.toLocalFastWithHeadingDelta(lat, lon, h, out);

                    double Hq = ramoGeoide(lat, lon, out[2]);

                    sE = String.format(Locale.US, "%.3f", out[0]);
                    sN = String.format(Locale.US, "%.3f", out[1]);
                    sH = String.format(Locale.US, "%.3f", Hq);

                    etEst.setText(sE);
                    etNord.setText(sN);
                    etZ.setText(sH);
                    etDH.setText(String.format(Locale.US, "%.3f", out[3]));

                    log(String.format(Locale.US,
                            "→ Locale: E=%.3f  N=%.3f  Z=%.3f  dHDT=%.3f",
                            out[0], out[1], Hq, out[3]));

                    MyData.push("Test_sLat", String.format(Locale.US, "%.9f", lat));
                    MyData.push("Test_sLon", String.format(Locale.US, "%.9f", lon));
                    MyData.push("Test_sHll", String.format(Locale.US, "%.3f", h));
                    updateLLQ();

                } catch (Exception e) {
                    log("Error: " + e.getMessage());
                }

            } else {
                try {
                    setEPSGLocal();

                    double lat = Double.parseDouble(etLat.getText().toString().replace(",", "."));
                    double lon = Double.parseDouble(etLon.getText().toString().replace(",", "."));
                    double h = Double.parseDouble(etH.getText().toString().replace(",", "."));

                    coordinateXYZ = Deg2UTM.trasform(lat, lon, h, DataSaved.S_CRS);

                    double Easting = coordinateXYZ.getEasting();
                    double Northing = coordinateXYZ.getNorthing();
                    double Quota = coordinateXYZ.getQuota();

                    sE = String.format(Locale.US, "%.3f", Easting);
                    sN = String.format(Locale.US, "%.3f", Northing);
                    sH = String.format(Locale.US, "%.3f", Quota);

                    etEst.setText(sE);
                    etNord.setText(sN);
                    etZ.setText(sH);
                    etDH.setText(String.format(Locale.US, "%.3f", NmeaListener.AGGIUNTA_HDT));

                    log(String.format(Locale.US,
                            "→ CRS %s: E=%.3f  N=%.3f  Z=%.3f  dHDT=%.3f",
                            DataSaved.S_CRS, Easting, Northing, Quota, NmeaListener.AGGIUNTA_HDT));

                    MyData.push("Test_sLat", String.format(Locale.US, "%.9f", lat));
                    MyData.push("Test_sLon", String.format(Locale.US, "%.9f", lon));
                    MyData.push("Test_sHll", String.format(Locale.US, "%.3f", h));
                    updateLLQ();

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
        if (s == null) return;

        if (!s.equals(_UTM) && !s.equals(_NONE)) {

            if (s.equals("150580")) {
                try {
                    mycrsFactory = new CRSFactory();
                    myctFactory = new CoordinateTransformFactory();
                    myWGS84 = mycrsFactory.createFromName("epsg:4326");

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
                    Log.e("setEPSGLocal", Log.getStackTraceString(e));
                }

            } else {
                try {
                    myresult = new ProjCoordinate();
                    myresultWgs = new ProjCoordinate();
                    mycrsFactory = new CRSFactory();
                    myctFactory = new CoordinateTransformFactory();
                    myWGS84 = mycrsFactory.createFromName("epsg:4326");

                    switch (s) {
                        case "5514": {
                            String epsg5514 =
                                    "+proj=krovak +lat_0=49.5 +lon_0=24.8333333333333 +alpha=30.2881397527778 " +
                                            "+k=0.9999 +x_0=0 +y_0=0 +ellps=bessel " +
                                            "+towgs84=572.213,85.334,461.94,4.9732,1.529,5.2484,3.5378 " +
                                            "+units=m +no_defs";

                            myUTM = mycrsFactory.createFromParameters("EPSG:5514", epsg5514);
                            mywgsToUtm = myctFactory.createTransform(myWGS84, myUTM);
                            myutmToWgs = myctFactory.createTransform(myUTM, myWGS84);
                            break;
                        }

                        case "5513": {
                            String epsg5513 =
                                    "+proj=krovak +axis=swu +lat_0=49.5 +lon_0=24.8333333333333 +alpha=30.2881397527778 " +
                                            "+k=0.9999 +x_0=0 +y_0=0 +ellps=bessel " +
                                            "+towgs84=572.213,85.334,461.94,4.9732,1.529,5.2484,3.5378 " +
                                            "+units=m +no_defs";

                            myUTM = mycrsFactory.createFromParameters("EPSG:5513", epsg5513);
                            mywgsToUtm = myctFactory.createTransform(myWGS84, myUTM);
                            myutmToWgs = myctFactory.createTransform(myUTM, myWGS84);
                            break;
                        }

                        case "150581": {
                            String krovakGRS80 =
                                    "+proj=krovak +lat_0=49.5 +lon_0=24.8333333333333 +alpha=30.2881397527778 " +
                                            "+k=0.9999 +x_0=0 +y_0=0 +ellps=GRS80 +units=m +no_defs";

                            myUTM = mycrsFactory.createFromParameters("KROVAK_GRS80", krovakGRS80);
                            mywgsToUtm = myctFactory.createTransform(myWGS84, myUTM);
                            myutmToWgs = myctFactory.createTransform(myUTM, myWGS84);

                            if (czQ1 == null) {
                                try (InputStream is = getAssets().open("table_yx_3_v1710_Q1.gsb")) {
                                    czQ1 = new CzechGridShiftTransformer(is);
                                } catch (Exception e) {
                                    Log.e("GridShiftCZ", Log.getStackTraceString(e));
                                    czQ1 = null;
                                }
                            }
                            break;
                        }

                        case "150582": {
                            String krovakGRS80 =
                                    "+proj=krovak +lat_0=49.5 +lon_0=24.8333333333333 +alpha=30.2881397527778 " +
                                            "+k=0.9999 +x_0=0 +y_0=0 +ellps=GRS80 +units=m +no_defs";

                            myUTM = mycrsFactory.createFromParameters("KROVAK_GRS80", krovakGRS80);
                            mywgsToUtm = myctFactory.createTransform(myWGS84, myUTM);
                            myutmToWgs = myctFactory.createTransform(myUTM, myWGS84);

                            if (czQ3 == null) {
                                try (InputStream is = getAssets().open("table_yx_3_v1710_Q3.gsb")) {
                                    czQ3 = new CzechGridShiftTransformer(is);
                                } catch (Exception e) {
                                    Log.e("GridShiftCZ", Log.getStackTraceString(e));
                                    czQ3 = null;
                                }
                            }
                            break;
                        }

                        default: {
                            try {
                                myUTM = mycrsFactory.createFromName("epsg:" + DataSaved.S_CRS);
                            } catch (InvalidValueException | UnknownAuthorityCodeException |
                                     UnsupportedParameterException e) {
                                Log.e("GridShift", Log.getStackTraceString(e));
                                myUTM = null;
                            }

                            if (myUTM != null) {
                                mywgsToUtm = myctFactory.createTransform(myWGS84, myUTM);
                                myutmToWgs = myctFactory.createTransform(myUTM, myWGS84);
                            }
                            break;
                        }
                    }

                } catch (Exception e) {
                    Log.e("setEPSGLocal", Log.getStackTraceString(e));
                }
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