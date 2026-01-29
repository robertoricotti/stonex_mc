package gui.tech_menu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import java.util.Arrays;

import gui.BaseClass;
import gui.dialogs_and_toast.InformationsDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.CircumferenceCenterCalculator;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import utils.DistToPoint;
import services.UpdateValuesService;
import utils.MyData;

public class GPS_Autocalib extends BaseClass {
    InformationsDialog informationsDialog;
    static double result, resultD1, resultD2, resultTOT, dX, dY;
    private boolean vis2, vis3, vis4, vis5;
    ImageView back, saveAll, gpsStatus, info, savehdt, savexy;
    TextView txt1, txt2, txt3, txt4, tRes, tXY, tpitch, troll;
    Button btn1, btn2, btn3, btn4, calc, turn;

    static double[] p1, p2, p3, p4, pos1, pos2, pos3, pos4, pos5, pos6, coordC;
    int indexMachineSelected;
    int countReader = 0;
    boolean isStarted = false;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.BRAND.equals("SRT8PROS")) {
            setContentView(R.layout.activity_gps_autocalib);
        } else {
            setContentView(R.layout.activity_gps_autocalib_7);
        }
        findView();
        init();
        onClick();
        updateUI();
    }

    private void findView() {
        back = findViewById(R.id.goback);
        txt1 = findViewById(R.id.tP1);
        txt2 = findViewById(R.id.tP2);
        txt3 = findViewById(R.id.tP3);
        txt4 = findViewById(R.id.tP4);
        btn1 = findViewById(R.id.bP1);
        btn2 = findViewById(R.id.bP2);
        btn3 = findViewById(R.id.bP3);
        btn4 = findViewById(R.id.bP4);
        saveAll = findViewById(R.id.saveAll);
        calc = findViewById(R.id.Calcola);
        tRes = findViewById(R.id.tRES);
        tXY = findViewById(R.id.tXY);
        turn = findViewById(R.id.CalculateXY);
        gpsStatus = findViewById(R.id.gpsStatus);
        info = findViewById(R.id.info);
        tpitch = findViewById(R.id.tpitch);
        troll = findViewById(R.id.troll);
        savehdt = findViewById(R.id.savehdt);
        savexy = findViewById(R.id.savexyz);
    }

    private void init() {

        indexMachineSelected = MyData.get_Int("MachineSelected");
        result = DataSaved.deltaGPS2;
        tRes.setText(String.format("%.3f", result));
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        informationsDialog = new InformationsDialog(GPS_Autocalib.this, "1: Place the excavator on flat surface (Pitch and Roll must be less than 2 degrees)  2: Place antenna 1 on a point in the center of the boom1 near the pivot and press P1  3: Place antenna 1 on a point in the center of the boom1 near the stick and press P2  4: Place antenna 1 on the GPS1 bracket and press P3  5: Place antenna 1 on the GPS2 bracket and press P4  6: Press CALC  7: Pess XYZ and begin to turn the machine WITHOUT MOVING TRACKS  When the calibration process is finished the values will be reporten below  Keep SAVED pressed to store calibration data or repeat Point nr.7  ");
    }

    private void onClick() {
        back.setOnClickListener(view -> {
            back.setEnabled(false);
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });
        btn1.setOnClickListener(view -> {
            //misura primo punto su braccio vecino cabina
            p1 = new double[]{NmeaListener.mLat_1, NmeaListener.mLon_1};
            txt1.setText(Arrays.toString(p1));
            vis2 = true;


        });
        btn2.setOnClickListener(view -> {
            //misura secondo punto su braccio vicino stick
            p2 = new double[]{NmeaListener.mLat_1, NmeaListener.mLon_1};
            txt2.setText(Arrays.toString(p2));
            vis3 = true;
        });
        btn3.setOnClickListener(view -> {
            //misura terzo punto in posizione gps1
            p3 = new double[]{NmeaListener.mLat_1, NmeaListener.mLon_1};
            txt3.setText(Arrays.toString(p3));
            vis4 = true;
        });
        btn4.setOnClickListener(view -> {
            //misura quarto punto in posizione gps2
            p4 = new double[]{NmeaListener.mLat_1, NmeaListener.mLon_1};
            txt4.setText(Arrays.toString(p4));
            vis5 = true;
        });
        calc.setOnClickListener(view -> {
            vis2 = false;
            vis3 = false;
            vis4 = false;
            vis5 = false;
            try {
                double H1 = My_LocationCalc.calcBearing(p1[0], p1[1], p2[0], p2[1]);//bearing sul boom
                double H2 = My_LocationCalc.calcBearing(p3[0], p3[1], p4[0], p4[1]);//bearing gps1/gps2
                result = (H1 - H2);
                if (result > 180) {
                    result -= 360;
                } else if (result < -180) {
                    result += 360;
                }

            } catch (Exception e) {
                result = 0;
                new CustomToast(GPS_Autocalib.this, "NO VALID DATA").show();
            }
        });
        turn.setOnClickListener(view -> {
            isStarted = !isStarted;


        });
        savehdt.setOnLongClickListener(view -> {
            new CustomToast(GPS_Autocalib.this, "SAVING HDT").show();
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", tRes.getText().toString());
            startService(new Intent(GPS_Autocalib.this, UpdateValuesService.class));
            return true;
        });
        savexy.setOnLongClickListener(view -> {
            new CustomToast(GPS_Autocalib.this, "SAVING XY").show();
            MyData.push("M" + indexMachineSelected + "_OffsetGPSY", String.valueOf(dY));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSX", String.valueOf(dX));
            startService(new Intent(GPS_Autocalib.this, UpdateValuesService.class));
            return true;
        });
        gpsStatus.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });
        info.setOnClickListener(view -> {
            if (!informationsDialog.alertDialog.isShowing()) {
                informationsDialog.show();
            }

        });
    }

    public void updateUI() {

        try {
            if (tXY.getText().toString().contains("?")) {
                savexy.setVisibility(View.INVISIBLE);
            } else {
                savexy.setVisibility(View.VISIBLE);
            }
            if (vis2) {
                btn2.setVisibility(View.VISIBLE);
            } else {
                btn2.setVisibility(View.INVISIBLE);
            }
            if (vis3) {
                btn3.setVisibility(View.VISIBLE);
            } else {
                btn3.setVisibility(View.INVISIBLE);
            }
            if (vis4) {
                btn4.setVisibility(View.VISIBLE);
            } else {
                btn4.setVisibility(View.INVISIBLE);
            }
            if (vis5) {
                calc.setVisibility(View.VISIBLE);
            } else {
                calc.setVisibility(View.INVISIBLE);
            }
            tpitch.setText("Pitch: " + String.format("%.1f", ExcavatorLib.correctPitch) + " °");
            troll.setText("Roll: " + String.format("%.1f", ExcavatorLib.correctRoll) + " °");
            if (DataSaved.gpsOk) {
                gpsStatus.setImageResource(R.drawable.gps_si);
            } else {
                gpsStatus.setImageResource(R.drawable.gps_no);
            }
            try {
                tRes.setText(String.format("%.3f", result));
            } catch (Exception e) {
                tRes.setText("No Valid Data");
            }

            if (isStarted) {
                if (Math.abs(ExcavatorLib.correctPitch) > 4.0 || Math.abs(ExcavatorLib.correctRoll) > 4.0) {
                    isStarted = false;
                    countReader = 0;
                    new CustomToast(GPS_Autocalib.this, "PROCESS ABORTED\nMACHINE IS NOT ON A FLAT SURFACE").show();
                }
                countReader += 1;
                if (countReader == 1) {
                    tXY.setText("TURN....");
                }

                if (countReader == 30) {
                    pos1 = new double[]{NmeaListener.Est1, NmeaListener.Nord1};
                    tXY.setText("TURN...P1");
                }
                if (countReader == 60) {
                    pos2 = new double[]{NmeaListener.Est1, NmeaListener.Nord1};
                    tXY.setText("TURN...P1..P2");
                }
                if (countReader == 90) {
                    pos3 = new double[]{NmeaListener.Est1, NmeaListener.Nord1};
                    tXY.setText("TURN...P1,..P2..P3");
                }
                if (countReader == 120) {
                    pos4 = new double[]{NmeaListener.Est1, NmeaListener.Nord1};
                    tXY.setText("TURN...P1..P2..P3..P4");

                }
                if (countReader == 150) {
                    pos5 = new double[]{NmeaListener.Est1, NmeaListener.Nord1};
                    tXY.setText("TURN...P1..P2..P3..P4..P5");

                }
                if (countReader == 180) {
                    pos6 = new double[]{NmeaListener.Est1, NmeaListener.Nord1};
                    tXY.setText("TURN...P1..P2..P3..P4..P5..P6");

                }
                if (countReader == 200) {
                    tXY.setText("WAIT...CALCULATING");
                }


                if (countReader == 220) {
                    tXY.setText("...CALCULATING....");
                    resultD1 = CircumferenceCenterCalculator.findCircumferenceCenter(pos1[0], pos1[1], pos2[0], pos2[1], pos3[0], pos3[1])[2];
                    resultD2 = CircumferenceCenterCalculator.findCircumferenceCenter(pos4[0], pos4[1], pos5[0], pos5[1], pos6[0], pos6[1])[2];
                    resultTOT = (resultD1 + resultD2) / 2.0d;
                    double x1 = CircumferenceCenterCalculator.findCircumferenceCenter(pos1[0], pos1[1], pos2[0], pos2[1], pos3[0], pos3[1])[0];
                    double y1 = CircumferenceCenterCalculator.findCircumferenceCenter(pos1[0], pos1[1], pos2[0], pos2[1], pos3[0], pos3[1])[1];
                    double x2 = CircumferenceCenterCalculator.findCircumferenceCenter(pos4[0], pos4[1], pos5[0], pos5[1], pos6[0], pos6[1])[0];
                    double y2 = CircumferenceCenterCalculator.findCircumferenceCenter(pos4[0], pos4[1], pos5[0], pos5[1], pos6[0], pos6[1])[1];
                    double coordXC = (x1 + x2) / 2.0d;
                    double coordYC = (y1 + y2) / 2.0d;
                    coordC = new double[]{coordXC, coordYC};
                    double gps1_C = new DistToPoint(NmeaListener.Est1, NmeaListener.Nord1, 0, coordXC, coordYC, 0).getDist_to_point();
                    double alpha = NmeaListener.mch_Orientation + DataSaved.deltaGPS2;
                    if (alpha > 180) {
                        alpha -= 360;
                    } else if (alpha < -180) {
                        alpha += 360;
                    }
                    double bear_GPS1_C = My_LocationCalc.calcBearingXY(NmeaListener.Est1, NmeaListener.Nord1, coordXC, coordYC);
                    double theta = alpha - bear_GPS1_C;
                    if (theta > 180) {
                        theta -= 360;
                    } else if (theta < -180) {
                        theta += 360;
                    }
                    dX = Math.abs(gps1_C * Math.sin(Math.toRadians(theta)));
                    dY = Math.abs(Math.sqrt(Math.abs(gps1_C) * Math.abs(gps1_C) - Math.abs(dX) * Math.abs(dX)));
                    tXY.setText("  r: " + String.format("%.3f", resultTOT) + "  DeltaX: " + String.format("%.3f", dX) + "  DeltaY: " + String.format("%.3f", dY));
                    isStarted = false;
                    countReader = 0;
                }

            }

        } catch (Exception e) {
            new CustomToast(GPS_Autocalib.this, " ").show();
        }

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}