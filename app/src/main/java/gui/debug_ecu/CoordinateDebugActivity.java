package gui.debug_ecu;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.MyApp;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;

public class CoordinateDebugActivity extends AppCompatActivity {

    ImageView back, toGpsDebug;

    TextView S0_X, S1_X, S2_X, S3_X, S4_X, S5_X, S6_X, S7_X, LSV_X, LSH_X, S75_X, start_X, bucketLeft_X, bucket_X, bucketRight_X;
    TextView S0_Y, S1_Y, S2_Y, S3_Y, S4_Y, S5_Y, S6_Y, S7_Y, LSV_Y, LSH_Y, S75_Y, start_Y, bucketLeft_Y, bucket_Y, bucketRight_Y;
    TextView S0_Z, S1_Z, S2_Z, S3_Z, S4_Z, S5_Z, S6_Z, S7_Z, LSV_Z, LSH_Z, S75_Z, start_Z, bucketLeft_Z, bucket_Z, bucketRight_Z;

    private Handler handler;
    private boolean mRunning = true;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinate_debug);
        findView();
        onClick();
        updateUI();
    }

    private void findView() {
        toGpsDebug = findViewById(R.id.gpsDebug);
        S0_X = findViewById(R.id.s0_X);
        S1_X = findViewById(R.id.s1_X);
        S2_X = findViewById(R.id.s2_X);
        S3_X = findViewById(R.id.s3_X);
        S4_X = findViewById(R.id.s4_X);
        S5_X = findViewById(R.id.s5_X);
        S6_X = findViewById(R.id.s6_X);
        S7_X = findViewById(R.id.s7_X);
        LSV_X = findViewById(R.id.lsv_X);
        LSH_X = findViewById(R.id.lsh_X);
        S75_X = findViewById(R.id.s75_X);
        start_X = findViewById(R.id.start_X);
        bucketLeft_X = findViewById(R.id.buckLeft_X);
        bucket_X = findViewById(R.id.buck_X);
        bucketRight_X = findViewById(R.id.buckRight_X);

        S0_Y = findViewById(R.id.s0_Y);
        S1_Y = findViewById(R.id.s1_Y);
        S2_Y = findViewById(R.id.s2_Y);
        S3_Y = findViewById(R.id.s3_Y);
        S4_Y = findViewById(R.id.s4_Y);
        S5_Y = findViewById(R.id.s5_Y);
        S6_Y = findViewById(R.id.s6_Y);
        S7_Y = findViewById(R.id.s7_Y);
        LSV_Y = findViewById(R.id.lsv_Y);
        LSH_Y = findViewById(R.id.lsh_Y);
        S75_Y = findViewById(R.id.s75_Y);
        start_Y = findViewById(R.id.start_Y);
        bucketLeft_Y = findViewById(R.id.buckLeft_Y);
        bucket_Y = findViewById(R.id.buck_Y);
        bucketRight_Y = findViewById(R.id.buckRight_Y);

        S0_Z = findViewById(R.id.s0_Z);
        S1_Z = findViewById(R.id.s1_Z);
        S2_Z = findViewById(R.id.s2_Z);
        S3_Z = findViewById(R.id.s3_Z);
        S4_Z = findViewById(R.id.s4_Z);
        S5_Z = findViewById(R.id.s5_Z);
        S6_Z = findViewById(R.id.s6_Z);
        S7_Z = findViewById(R.id.s7_Z);
        LSV_Z = findViewById(R.id.lsv_Z);
        LSH_Z = findViewById(R.id.lsh_Z);
        S75_Z = findViewById(R.id.s75_Z);
        start_Z = findViewById(R.id.start_Z);
        bucketLeft_Z = findViewById(R.id.buckLeft_Z);
        bucket_Z = findViewById(R.id.buck_Z);
        bucketRight_Z = findViewById(R.id.buckRight_Z);


        back = findViewById(R.id.back);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);

    }

    private void onClick() {
        toGpsDebug.setOnClickListener(view -> {

            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });


        back.setOnClickListener((View v) -> {
            back.setEnabled(false);
            startActivity(new Intent(this, DebugExcavatorActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRunning) {
                    // Do something
                    handler.post(new Runnable() {
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void run() {
                            try {
                                if (DataSaved.gpsOk) {
                                    toGpsDebug.setImageResource(R.drawable.gps_ok);
                                } else {
                                    toGpsDebug.setImageResource(R.drawable.gps_err);
                                }

                                S0_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordinateDZ[0]).replace(",", "."));
                                S1_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordinateDX[0]).replace(",", "."));
                                S2_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordinateDY[0]).replace(",", "."));
                                S3_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordRoll[0]).replace(",", "."));
                                S4_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordPitch[0]).replace(",", "."));
                                S5_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordB1[0]).replace(",", "."));
                                S6_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordB2[0]).replace(",", "."));
                                S7_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordST[0]).replace(",", "."));
                                LSV_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordiLSV[0]).replace(",", "."));
                                LSH_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordLSH[0]).replace(",", "."));
                                S75_X.setText("X: " + String.format("%.3f", ExcavatorLib.coordPivoTilt[0]).replace(",", "."));
                                start_X.setText("X: " + String.format("%.3f", ExcavatorLib.startXYZ[0]).replace(",", "."));
                                bucketLeft_X.setText("X: " + String.format("%.3f", ExcavatorLib.bucketLeftCoord[0]).replace(",", "."));
                                bucket_X.setText("X: " + String.format("%.3f", ExcavatorLib.bucketCoord[0]).replace(",", "."));
                                bucketRight_X.setText("X: " + String.format("%.3f", ExcavatorLib.bucketRightCoord[0]).replace(",", "."));

                                S0_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordinateDZ[1]).replace(",", "."));
                                S1_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordinateDX[1]).replace(",", "."));
                                S2_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordinateDY[1]).replace(",", "."));
                                S3_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordRoll[1]).replace(",", "."));
                                S4_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordPitch[1]).replace(",", "."));
                                S5_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordB1[1]).replace(",", "."));
                                S6_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordB2[1]).replace(",", "."));
                                S7_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordST[1]).replace(",", "."));
                                LSV_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordiLSV[1]).replace(",", "."));
                                LSH_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordLSH[1]).replace(",", "."));
                                S75_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.coordPivoTilt[1]).replace(",", "."));
                                start_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.startXYZ[1]).replace(",", "."));
                                bucketLeft_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.bucketLeftCoord[1]).replace(",", "."));
                                bucket_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.bucketCoord[1]).replace(",", "."));
                                bucketRight_Y.setText("Y: " + String.format("%.3f", ExcavatorLib.bucketRightCoord[1]).replace(",", "."));

                                S0_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordinateDZ[2]).replace(",", "."));
                                S1_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordinateDX[2]).replace(",", "."));
                                S2_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordinateDY[2]).replace(",", "."));
                                S3_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordRoll[2]).replace(",", "."));
                                S4_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordPitch[2]).replace(",", "."));
                                S5_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordB1[2]).replace(",", "."));
                                S6_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordB2[2]).replace(",", "."));
                                S7_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordST[2]).replace(",", "."));
                                LSV_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordiLSV[2]).replace(",", "."));
                                LSH_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordLSH[2]).replace(",", "."));
                                S75_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.coordPivoTilt[2]).replace(",", "."));
                                start_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.startXYZ[2]).replace(",", "."));
                                bucketLeft_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.bucketLeftCoord[2]).replace(",", "."));
                                bucket_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.bucketCoord[2]).replace(",", "."));
                                bucketRight_Z.setText("Z: " + String.format("%.3f", ExcavatorLib.bucketRightCoord[2]).replace(",", "."));
                            } catch (Exception e) {

                            }

                        }
                    });
                    // sleep per intervallo update UI
                    try {
                        Thread.sleep(MyApp.timeUI);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRunning = false;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}
