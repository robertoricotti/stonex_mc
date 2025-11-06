package gui.boot_and_choose;

import static gui.MyApp.gridFile_GR_dE;
import static gui.MyApp.gridFile_GR_dN;
import static gui.MyApp.heposTransformer;
import static packexcalib.gnss.CRS_Strings._NONE;

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
import java.util.Locale;
import java.util.Objects;

import gui.MyApp;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.GGFGeoide;
import packexcalib.gnss.GeoidBinLite;
import packexcalib.gnss.GeoideInterpolation;
import packexcalib.gnss.GridShiftTransformer;
import packexcalib.gnss.LocalizationFactory;
import packexcalib.gnss.LocalizationModel;
import utils.MyData;

/**
 * Activity di test per SpLocalization:
 * Permette di caricare un file .SP e testare le trasformazioni
 * diretta (Lat,Lon,H → E,N,Z) e inversa (E,N,Z → Lat,Lon,H).
 */
public class SpTestActivity extends Activity {
    private final double[] quotaBuf = new double[1];
    boolean ggfReady;
    private static GeoideInterpolation UGF_READER;
    private static String UGF_PATH_LOADED;
    public  static boolean ugfReady;

    private static GeoidBinLite BIN_READER;
    private static String BIN_PATH_LOADED;
    public  static boolean binReady;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sp_test);

        myresult = new ProjCoordinate();
        myshifted = new ProjCoordinate();
        myresultWgs = new ProjCoordinate();


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
                    double Hq=ramoGeoide(lat,lon,out[2]);
                    sE = String.format("%.3f", out[0]).replace(",", ".");
                    sN = String.format("%.3f", out[1]).replace(",", ".");
                    sH = String.format("%.3f", Hq).replace(",", ".");
                    etEst.setText(sE);
                    etNord.setText(sN);
                    etZ.setText(sH);
                    log(String.format("→ Locale: E=%.3f  N=%.3f  Z=%.3f", out[0], out[1], Hq).replaceAll(",", "."));
                } catch (Exception e) {
                    log("Error: " + e.getMessage());
                }
            } else {
                try {
                    Log.d("EPGS",DataSaved.S_CRS);
                    setEPSGLocal();
                    double lat = Double.parseDouble(etLat.getText().toString().replace(",", "."));
                    double lon = Double.parseDouble(etLon.getText().toString().replace(",", "."));
                    double h = Double.parseDouble(etH.getText().toString().replace(",", "."));
                    double Easting, Northing, Quota;
                    if (DataSaved.S_CRS.equals("150580")) {

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
                        }
                    } else {
                        if (mywgsToUtm != null && myresult != null) {
                            mywgsToUtm.transform(new ProjCoordinate(lon, lat, h), myresult);
                            Easting = myresult.x;
                            Northing = myresult.y;
                            Quota =ramoGeoide(lat,lon,h);
                            sE = String.format("%.3f", Easting).replace(",", ".");
                            sN = String.format("%.3f", Northing).replace(",", ".");
                            sH = String.format("%.3f", Quota).replace(",", ".");
                            etEst.setText(sE);
                            etNord.setText(sN);
                            etZ.setText(sH);

                        }
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
                etLat.setText(sLat);
                etLon.setText(sLon);
                etH.setText(sHll);
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
            etLat.setText(sLat);
            etLon.setText(sLon);
            etH.setText(sHll);

        } catch (Exception ignored) {

        }
    }

    private void log(String s) {
        tvOutput.append(s + "\n");
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

    private double ramoGeoide(double Lat,double Lon,double Z){
        double outQ=0;
        try {

            double q = Z;

            final String path = MyApp.GEOIDE_PATH;
            if (path != null && !path.isEmpty() && !"null".equals(path)) {
                int dot = path.lastIndexOf('.');
                String ext = (dot > 0) ? path.substring(dot + 1).toLowerCase(Locale.ROOT) : "";

                switch (ext) {
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
                                //Log.d("Deg2UTM", "GGF letto");
                                GGF_PATH_LOADED = path;
                                ggfReady = true;
                            }
                            if (ggfReady && GGF_READER.isInGrid(Lat, Lon)) {
                                double und = GGF_READER.getUndulation(Lat, Lon);
                                q = Double.isNaN(und) ? Z : (Z - und);

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
                                // Log.d("Deg2UTM", "UGF letto");
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

            outQ= q;

        } catch (Exception e) {
            //Log.e("Deg2UTM", "Transform error", e);
            outQ= 0;

        }
        return outQ;
    }

    @Override
    public void onBackPressed() {

    }
}