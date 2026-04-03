package gui.boot_and_choose;


import static gui.MyApp.TEST_MODE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.stx_dig.R;

import java.util.Locale;

import packexcalib.exca.DataSaved;
import packexcalib.gnss.CoordinateXYZ;
import packexcalib.gnss.Deg2UTM;
import packexcalib.gnss.NmeaListener;
import packexcalib.gnss.UTM2Deg;
import services.ReadProjectService;
import utils.MyData;

/**
 * Activity di test per SpLocalization:
 * Permette di caricare un file .SP e testare le trasformazioni
 * diretta (Lat,Lon,H → E,N,Z) e inversa (E,N,Z → Lat,Lon,H).
 */
public class SpTestActivity extends Activity {

    public static String sN, sE, sH, sLat, sLon, sHll;
    private EditText etLat, etLon, etH;
    private EditText etEst, etNord, etZ,etDH;
    private TextView tvOutput;
    private Button btnLoad, btnToLocal, btnToGeo, btn_Exit;
    int proximus=0;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        TEST_MODE=true;
        proximus=DataSaved.my_comPort;
        if(DataSaved.my_comPort==4){
            DataSaved.my_comPort=0;
        }

        setContentView(R.layout.activity_sp_test);
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
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //btnToGeo.setVisibility(TextView.INVISIBLE);
        etEst.setText("");
        etNord.setText("");
        etZ.setText("");
        btnToLocal.setText("→ Transform in Local (N, E ,Z)");

        try {

            etLat.setText(sLat.replace(",","."));
            etLon.setText(sLon.replace(",","."));
            etH.setText(sHll.replace(",","."));
            String s1 = "";
            s1 = MyData.get_String("LastSP");
            if (s1 == null) {
                btnLoad.setText("");
            } else {
                btnLoad.setText(s1);
            }

        } catch (Exception ignored) {

        }


        btn_Exit.setOnClickListener(view -> {
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });

        btnToLocal.setOnClickListener(v -> {

            try {
                    double lat = Double.parseDouble(etLat.getText().toString().replace(",", "."));
                    double lon = Double.parseDouble(etLon.getText().toString().replace(",", "."));
                    double h = Double.parseDouble(etH.getText().toString().replace(",", "."));
                    CoordinateXYZ coordinateXYZ=  Deg2UTM.trasform(lat, lon, h, DataSaved.S_CRS);

                    sE = String.format(Locale.US, "%.3f", coordinateXYZ.getEasting());
                    sN = String.format(Locale.US, "%.3f", coordinateXYZ.getNorthing());
                    sH = String.format(Locale.US, "%.3f", coordinateXYZ.getQuota());

                    etEst.setText(sE);
                    etNord.setText(sN);
                    etZ.setText(sH);
                    etDH.setText(String.format(Locale.US, "%.2f", NmeaListener.AGGIUNTA_HDT)+" °");


                    MyData.push("Test_sLat", String.format(Locale.US, "%.11f", lat));
                    MyData.push("Test_sLon", String.format(Locale.US, "%.11f", lon));
                    MyData.push("Test_sHll", String.format(Locale.US, "%.3f", h));
                    updateLLQ();

                } catch (Exception e) {
                    log("Error: " + e.getMessage());
                }


        });

        btnToGeo.setOnClickListener(view -> {
            try {
                double est = Double.parseDouble(etEst.getText().toString().replace(",", "."));
                double nord = Double.parseDouble(etNord.getText().toString().replace(",", "."));
                double quota = Double.parseDouble(etZ.getText().toString().replace(",", "."));

                double[] coordinates = UTM2Deg.toGeo(est, nord, quota, DataSaved.S_CRS);

                sLat = String.format(Locale.US, "%.11f", coordinates[0]);
                sLon = String.format(Locale.US, "%.11f", coordinates[1]);
                sHll = String.format(Locale.US, "%.3f", coordinates[2]);

                etLat.setText(sLat);
                etLon.setText(sLon);
                etH.setText(sHll);

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

    private void init() {

            ReadProjectService.startCRS();

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
        DataSaved.my_comPort=proximus;
        TEST_MODE=false;
    }
}