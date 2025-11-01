package gui.boot_and_choose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stx_dig.R;

import java.io.File;

import packexcalib.gnss.LocalizationFactory;
import packexcalib.gnss.LocalizationModel;
import utils.MyData;

/**
 * Activity di test per SpLocalization:
 * Permette di caricare un file .SP e testare le trasformazioni
 * diretta (Lat,Lon,H → E,N,Z) e inversa (E,N,Z → Lat,Lon,H).
 */
public class SpTestActivity extends Activity {
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

        try {
            etEst.setText(sE);
            etNord.setText(sN);
            etZ.setText(sH);
            etLat.setText(sLat);
            etLon.setText(sLon);
            etH.setText(sHll);

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
                sE = String.format("%.3f", out[0]).replace(",", ".");
                sN = String.format("%.3f", out[1]).replace(",", ".");
                sH = String.format("%.3f", out[2]).replace(",", ".");
                etEst.setText(sE);
                etNord.setText(sN);
                etZ.setText(sH);
                log(String.format("→ Locale: E=%.3f  N=%.3f  Z=%.3f", out[0], out[1], out[2]).replaceAll(",", "."));
            } catch (Exception e) {
                log("Errore: " + e.getMessage());
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
        }
    }

    @Override
    public void onBackPressed() {

    }
}