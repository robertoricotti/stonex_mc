package drill_pile.gui;

import static gui.MyApp.errorCode;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.draw_class.MyColorClass;
import gui.gps.NmeaGenerator;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import services.PointService;
import utils.DistToPoint;
import utils.MyData;
import utils.MyMCUtils;
import utils.Utils;

public class Drill_Activity extends BaseClass {
    public static int typeVistaDrill;
    public static boolean isDrilling;
    int flip = 0;
    float rotationCont;

    Dialog_Drill_GNSS dialogDrillGnss;
    View divisorioC, divisorioDx, divisorioUp, divisorioDw, topViewCanvas, bubbleCanvas;
    ImageView digMenu, drilltool, typeView, Status, folders, playpause, lineReference, tiposnap,
            zoom_P, zoom_M, zoom_C, compass, quotaIndicator;
    ConstraintLayout topview, bubble;
    VerticalTargetIndicatorView indicator;
    TextView idpalo, txthdt, txttilt, txtdepth, uomesure, textInfo, tiltInfo, txttiltActual, txthdtActual;
    LinearLayout sideLayout;
    int colorUp, colorDown, colorGreen;
    Dialog_AutoSnap dialogAutoSnap;
    Guideline cent_v, side;
    float rot = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill);
        findView();
        init();
        onClick();


    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, PointService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyData.push("scaleFactor3D", String.valueOf(DataSaved.scale_Factor3D));
        stopService(new Intent(this, PointService.class));
    }

    private void findView() {
        DataSaved.scale_Factor3D = MyData.get_Double("scaleFactor3D");
        try {
            if (MyData.get_String("isAutosnap") == null) {
                MyData.push("isAutosnap", String.valueOf(DataSaved.isAutoSnap));
            } else {
                DataSaved.isAutoSnap = MyData.get_Int("isAutosnap");

            }
        } catch (Exception e) {
            MyData.push("isAutosnap", String.valueOf(0));
            DataSaved.isAutoSnap = 0;
        }
        dialogDrillGnss = new Dialog_Drill_GNSS(this);
        dialogAutoSnap = new Dialog_AutoSnap(this);
        divisorioC = findViewById(R.id.divisorioC);
        divisorioDx = findViewById(R.id.divisorioDx);
        divisorioUp = findViewById(R.id.divisorioUp);
        divisorioDw = findViewById(R.id.divisorioDw);
        digMenu = findViewById(R.id.digMenu);
        Status = findViewById(R.id.Status);
        drilltool = findViewById(R.id.drilltool);
        folders = findViewById(R.id.folders);
        playpause = findViewById(R.id.playpause);
        topview = findViewById(R.id.topview);
        bubble = findViewById(R.id.bubble);
        indicator = findViewById(R.id.verticalIndicator);
        idpalo = findViewById(R.id.idpalo);
        txthdt = findViewById(R.id.txthdt);
        txttilt = findViewById(R.id.txttilt);
        txtdepth = findViewById(R.id.txtdepth);
        sideLayout = findViewById(R.id.sideLayout);
        lineReference = findViewById(R.id.lineReference);
        tiposnap = findViewById(R.id.tiposnap);
        uomesure = findViewById(R.id.uomesure);
        zoom_P = findViewById(R.id.zoom_P);
        zoom_M = findViewById(R.id.zoom_M);
        zoom_C = findViewById(R.id.zoom_C);
        cent_v = findViewById(R.id.cent_v);
        side = findViewById(R.id.side);
        typeView = findViewById(R.id.typeView);
        compass = findViewById(R.id.compass);
        textInfo = findViewById(R.id.textInfo);
        tiltInfo = findViewById(R.id.tiltInfo);

        quotaIndicator = findViewById(R.id.quotaIndicator);
        txthdtActual = findViewById(R.id.txthdtActual);
        txttiltActual = findViewById(R.id.txttiltActual);


    }

    private void init() {
        if (DataSaved.colorMode == 0) {
            colorUp = Color.RED;
            colorDown = Color.BLUE;
            colorGreen = Color.GREEN;
        } else {
            colorDown = Color.RED;
            colorUp = Color.BLUE;
            colorGreen = Color.GREEN;
        }
        divisorioC.setBackgroundColor(MyColorClass.colorConstraint);
        divisorioDw.setBackgroundColor(MyColorClass.colorConstraint);
        divisorioDx.setBackgroundColor(MyColorClass.colorConstraint);
        divisorioUp.setBackgroundColor(MyColorClass.colorConstraint);
        bubble.setBackgroundColor(MyColorClass.colorSfondo);
        topview.setBackgroundColor(MyColorClass.colorSfondo);
        sideLayout.setBackgroundColor(MyColorClass.colorSfondo);


        indicator.setTolerance(DataSaved.deadbandH);
        indicator.setColors(colorUp, colorDown, colorGreen);
        textInfo.setTextColor(MyColorClass.colorConstraint);
        tiltInfo.setTextColor(MyColorClass.colorConstraint);

        topViewCanvas = new Drill_TopView(this);
        topview.addView(topViewCanvas);
        bubbleCanvas = new Drill_Bubble(this);
        bubble.addView(bubbleCanvas);
        switch (DataSaved.temaSoftware) {
            case 0:
                tiposnap.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                tiposnap.setImageTintList(getColorStateList(R.color.white));
                uomesure.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                uomesure.setTextColor(getColor(R.color.white));
                zoom_P.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_P.setImageTintList(getColorStateList(R.color.white));
                zoom_M.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_M.setImageTintList(getColorStateList(R.color.white));
                zoom_C.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_C.setImageTintList(getColorStateList(R.color.white));
                compass.setImageTintList(getColorStateList(R.color.white));
                ((Drill_TopView) topViewCanvas).setColorTarget_Alto(Color.CYAN);
                ((Drill_TopView) topViewCanvas).setColorTarget_Basso(Color.YELLOW);

                break;

            case 1:
            case 2:
                tiposnap.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                tiposnap.setImageTintList(getColorStateList(R.color._____cancel_text));
                uomesure.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                uomesure.setTextColor(getColor(R.color._____cancel_text));
                zoom_P.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                zoom_P.setImageTintList(getColorStateList(R.color._____cancel_text));
                zoom_M.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                zoom_M.setImageTintList(getColorStateList(R.color._____cancel_text));
                zoom_C.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                zoom_C.setImageTintList(getColorStateList(R.color._____cancel_text));
                compass.setImageTintList(getColorStateList(R.color._____cancel_text));
                ((Drill_TopView) topViewCanvas).setColorTarget_Alto(Color.BLUE);
                ((Drill_TopView) topViewCanvas).setColorTarget_Basso(getResources().getColor(R.color.bg));

                break;


        }
        uomesure.setText(Utils.getMetriSimbol().replace("[", "").replace("]", ""));


    }

    private void onClick() {
        compass.setOnLongClickListener(view -> {
            locateMachine();
            return false;
        });
        typeView.setOnClickListener(view -> {
            typeVistaDrill += 1;
            typeVistaDrill = typeVistaDrill % 3;

        });

        lineReference.setOnClickListener(view -> {

            if (!dialogAutoSnap.dialog.isShowing()) {
                dialogAutoSnap.show();
            }

        });


        digMenu.setOnClickListener(view -> {
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });
        Status.setOnClickListener(view -> {
            if (!dialogDrillGnss.alertDialog.isShowing()) {
                dialogDrillGnss.show();
            }
        });
        drilltool.setOnClickListener(view -> {
            Intent i = new Intent(this, Drill_Rod_Activity.class);
            i.putExtra("whoDrill", String.valueOf(MyApp.visibleActivity));
            startActivity(i);
            finish();
        });
        folders.setOnClickListener(view -> {

        });
        playpause.setOnClickListener(view -> {
            if (
                    PointService.okTilt && PointService.okXY
            ) {
                isDrilling = !isDrilling;
            } else {
                isDrilling = false;
            }//TODO ROUTINE trivellazione

        });

    }

    public void updateUI() {

        float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rotBus = rotBus % 360;
        compass.setRotation(rotBus);
        float cen = 0.35f;
        float lef = -0.01f;
        float rig = 0.92f;
        float sid = 0.92f;
        if (!isDrilling) {
            cen = 0.43f;
            rig = 1f;
            sid = 1f;
            settaFreccia();
        } else {
            quotaIndicator.setImageResource((R.drawable.baseline_arrow_circle_down));
            quotaIndicator.setRotation(0);
        }
        side.setGuidelinePercent(sid);

        switch (typeVistaDrill) {
            case 0:
                //

                tiltInfo.setVisibility(View.VISIBLE);
                cent_v.setGuidelinePercent(cen);
                zoom_P.setVisibility(View.INVISIBLE);
                zoom_M.setVisibility(View.INVISIBLE);
                zoom_C.setVisibility(View.INVISIBLE);
                compass.setVisibility(View.VISIBLE);
                break;

            case 1:

                tiltInfo.setVisibility(View.VISIBLE);
                cent_v.setGuidelinePercent(lef);
                zoom_P.setVisibility(View.INVISIBLE);
                zoom_M.setVisibility(View.INVISIBLE);
                zoom_C.setVisibility(View.INVISIBLE);
                compass.setVisibility(View.VISIBLE);
                break;

            case 2:
                //

                tiltInfo.setVisibility(View.VISIBLE);
                cent_v.setGuidelinePercent(rig);
                zoom_P.setVisibility(View.INVISIBLE);
                zoom_M.setVisibility(View.INVISIBLE);
                zoom_C.setVisibility(View.INVISIBLE);
                compass.setVisibility(View.VISIBLE);
                break;
        }


        if (DataSaved.gpsOk && errorCode == 0) {

            Status.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
            Status.setBackground(getDrawable(R.drawable.custom_background_test3d_box_gpsok));
            flip = 0;
        } else {

            flipFlop();
            flip += 1;
            flip = flip % 20;
        }


        if (DataSaved.Selected_Point3D_Drill != null) {
            idpalo.setText("R:" + DataSaved.Selected_Point3D_Drill.getRowId() + " - " + "P:" + DataSaved.Selected_Point3D_Drill.getId());
            txthdt.setText(String.format("%.1f", DataSaved.Selected_Point3D_Drill.getHeadingDeg()) + "°");
            txttilt.setText(String.format("%.1f", DataSaved.Selected_Point3D_Drill.getTilt()) + "°");
            if (isDrilling) {
                txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(deltaQuota3D())));
            } else {
                txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.toolEndCoord[2] - DataSaved.Selected_Point3D_Drill.getHeadZ())));
            }
        } else {
            idpalo.setText("R:___ P:___");
            txthdt.setText("_._°");
            txttilt.setText("_._°");
            txtdepth.setText("__.__");
        }
        switch (DataSaved.isAutoSnap) {
            case 0:
                tiposnap.setImageResource(R.drawable.edit_list);
                break;

            case 1:
                tiposnap.setImageResource(R.drawable.autosnappi);
                break;

            case 2:
                tiposnap.setImageResource(R.drawable.pick_pp);
                break;
        }

        textInfo.setText(setTesto());
        topViewCanvas.invalidate();
        int ringColor = getColor(R.color.verde_sfondo_scuro);
        int tricolor = getColor(R.color.transparent);
        int arrowColor = getColor(R.color.verde_sfondo_scuro);

        if (!PointService.okTilt) {
            ringColor = getResources().getColor(R.color.bg_sfsred);
            if (DataSaved.temaSoftware == 0) {
                tricolor = Color.YELLOW;
            } else {
                tricolor = Color.BLUE;
            }
        }

        if (!PointService.okXY) {
            if (Math.abs(PointService.pe[2]) < 1) {
                arrowColor = getColor(R.color.arancio_sfondo_scuro);
            } else {
                arrowColor = getColor(R.color.rosso_sfondo_scuro);
                ;
            }

        }


        rotationCont += 1;
        rotationCont = rotationCont % 360;
        ((Drill_Bubble) bubbleCanvas).setDrillingMode(isDrilling, rotationCont);
        ((Drill_Bubble) bubbleCanvas).setColors(ringColor, arrowColor, MyColorClass.colorSfondo, MyColorClass.colorConstraint, tricolor);
        ((Drill_Bubble) bubbleCanvas).setTriangles(PointService.FrecciaUP, PointService.FrecciaLEFT, PointService.FrecciaDOWN, PointService.FrecciaRIGHT);
        ((Drill_Bubble) bubbleCanvas).setPlanError(PointService.pe[0], PointService.pe[1]);
        if (!isDrilling) {
            if (DataSaved.Selected_Point3D_Drill == null) {
                ((Drill_Bubble) bubbleCanvas).setCenterDistance("???");
            } else {
                ((Drill_Bubble) bubbleCanvas).setCenterDistance((Utils.readUnitOfMeasureLITE(String.valueOf(PointService.pe[2]))));
            }
        } else {
            ((Drill_Bubble) bubbleCanvas).setCenterDistance(Utils.readUnitOfMeasureLITE(String.valueOf(deltaQuota3D())));
        }

        ((Drill_Bubble) bubbleCanvas).setHeadingDeg(hdt_BOOM);
        ((Drill_Bubble) bubbleCanvas).setCrossOnly(PointService.okXY);
        bubbleCanvas.invalidate();

        setIndicator();
        double mastHDT = My_LocationCalc.calcBearingXY(
                ExcavatorLib.coordTool[0], ExcavatorLib.coordTool[1],
                ExcavatorLib.toolEndCoord[0], ExcavatorLib.toolEndCoord[1]);
        double poleHDT = 0;
        double poleTilt = 0;

        double mastTilt = MyMCUtils.calculateTotalTilt(ExcavatorLib.correctToolPitch,
                ExcavatorLib.correctToolRoll);
        if (DataSaved.Selected_Point3D_Drill != null) {
            poleHDT =DataSaved.Selected_Point3D_Drill.getHeadingDeg();

            poleTilt = DataSaved.Selected_Point3D_Drill.getTilt();
        }
        String s = String.format(
                java.util.Locale.US,
                "Y: %7.2f°\nX: %7.2f°",
                ExcavatorLib.correctToolPitch,
                ExcavatorLib.correctToolRoll
        );
        tiltInfo.setText(s);

        txthdtActual.setText(String.format("%.1f", mastHDT) + "°");
        txttiltActual.setText(String.format("%.1f", mastTilt) + "°");
        double confronto = hdt_BOOM;
        if (!isInRangeAngle(mastTilt, 0, DataSaved.tolleranza_Slope)) {
            confronto = mastHDT;
        }
        Log.d("Confronto", String.valueOf(confronto)+"  "+poleHDT+"  "+isInRangeAngle(confronto, poleHDT, DataSaved.tolleranza_Slope));
        if (isInRangeAngle(confronto, poleHDT, DataSaved.tolleranza_Slope)) {
            txthdtActual.setTextColor(Color.GREEN);
        } else {
            txthdtActual.setTextColor(Color.WHITE);
        }
        if (isInRangeAngle(mastTilt, poleTilt, DataSaved.tolleranza_Slope)) {
            txttiltActual.setTextColor(Color.GREEN);
        } else {
            txttiltActual.setTextColor(Color.WHITE);
        }


    }

    private void flipFlop() {

        if (flip == 0) {
            Status.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            Status.setBackground(getDrawable(R.drawable.custom_background_test3d_box_gpsko));
        }
        if (flip == 10) {
            Status.setImageTintList(ColorStateList.valueOf(Color.RED));
            Status.setBackground(getDrawable(R.drawable.custom_background_test3d_box_grigino));
        }
    }

    private String setTesto() {
        String s0 = "Tool E: " + Utils.showCoords(String.valueOf(ExcavatorLib.toolEndCoord[0]));
        String s1 = "Tool N: " + Utils.showCoords(String.valueOf(ExcavatorLib.toolEndCoord[1]));
        String s2 = "Tool Z: " + Utils.showCoords(String.valueOf(ExcavatorLib.toolEndCoord[2]));
        String p = "Project: " + DataSaved.progettoSelected_POINT.substring(DataSaved.progettoSelected_POINT.lastIndexOf("/") + 1);
        if (DataSaved.coordOrder == 0) {
            return new String(s0 + "\n" + s1 + "\n" + s2 + "\n" + p);
        } else {
            return new String(s1 + "\n" + s0 + "\n" + s2 + "\n" + p);
        }
    }

    private void setIndicator() {
        try {
            indicator.setTargetValue(DataSaved.Selected_Point3D_Drill.getEndZ());
            double low = DataSaved.Selected_Point3D_Drill.getEndZ() - 0.5;
            double high = DataSaved.Selected_Point3D_Drill.getEndZ() + 2;
            indicator.setRange(low, high);
            indicator.setCurrentValue((float) ExcavatorLib.toolEndCoord[2]);//TODO adattare in realtime

        } catch (Exception ignored) {

        }

    }

    private void locateMachine() {
        if (DataSaved.my_comPort == 4) {
            try {
                DataSaved.demoNORD = DataSaved.drill_points.get(0).getHeadY();
                NmeaGenerator.LATITUDE = DataSaved.demoNORD;
                DataSaved.demoEAST = DataSaved.drill_points.get(0).getHeadX();
                NmeaGenerator.LONGITUDE = DataSaved.demoEAST;
                DataSaved.demoZ = DataSaved.drill_points.get(0).getHeadZ() + 4;
                NmeaGenerator.ALTITUDE = DataSaved.demoZ;
                MyData.push("demoNORD", String.valueOf(DataSaved.demoNORD));
                MyData.push("demoEAST", String.valueOf(DataSaved.demoEAST));
                MyData.push("demoZ", String.valueOf(DataSaved.demoZ));

            } catch (Exception e) {
                try {
                    DataSaved.demoNORD = DataSaved.points.get(0).getY();
                    NmeaGenerator.LATITUDE = DataSaved.demoNORD;
                    DataSaved.demoEAST = DataSaved.points.get(0).getX();
                    NmeaGenerator.LONGITUDE = DataSaved.demoEAST;
                    DataSaved.demoZ = DataSaved.points.get(0).getZ() + 3;
                    NmeaGenerator.ALTITUDE = DataSaved.demoZ;
                    MyData.push("demoNORD", String.valueOf(DataSaved.demoNORD));
                    MyData.push("demoEAST", String.valueOf(DataSaved.demoEAST));
                    MyData.push("demoZ", String.valueOf(DataSaved.demoZ));

                } catch (Exception ex) {
                    try {
                        DataSaved.demoNORD = DataSaved.polylines.get(0).getVertices().get(0).getY();
                        NmeaGenerator.LATITUDE = DataSaved.demoNORD;
                        DataSaved.demoEAST = DataSaved.polylines.get(0).getVertices().get(0).getX();
                        NmeaGenerator.LONGITUDE = DataSaved.demoEAST;
                        DataSaved.demoZ = DataSaved.polylines.get(0).getVertices().get(0).getZ() + 3;
                        NmeaGenerator.ALTITUDE = DataSaved.demoZ;
                        MyData.push("demoNORD", String.valueOf(DataSaved.demoNORD));
                        MyData.push("demoEAST", String.valueOf(DataSaved.demoEAST));
                        MyData.push("demoZ", String.valueOf(DataSaved.demoZ));
                    } catch (Exception exception) {
                        new CustomToast(this, "Impossible to Locate Machine").show_error();
                    }
                }
            }

        }
    }

    private void settaFreccia() {
        if (DataSaved.Selected_Point3D_Drill == null) {
            return;
        }
        double dist = ExcavatorLib.toolEndCoord[2] - DataSaved.Selected_Point3D_Drill.getHeadZ();
        if (dist > DataSaved.deadbandH) {
            quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
            quotaIndicator.setRotation(0);
        } else if (dist < -DataSaved.deadbandH) {
            quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
            quotaIndicator.setRotation(180);
        } else {
            quotaIndicator.setImageResource(R.drawable.outline_adjust_24);
            quotaIndicator.setRotation(0);
        }

    }

    private double deltaQuota3D() {
        if (DataSaved.Selected_Point3D_Drill != null) {
            return DistToPoint.dist3D(ExcavatorLib.toolEndCoord, new double[]{DataSaved.Selected_Point3D_Drill.getEndX(),
                    DataSaved.Selected_Point3D_Drill.getEndY(), DataSaved.Selected_Point3D_Drill.getEndZ()});
        } else {
            return 0;
        }
    }

    private boolean isInRangeAngle(double angle, double target, double deadband) {
        return Math.abs(angle - target) <= deadband;
    }

    private static double normalizeAngle(double a) {
        a = a % 360.0;
        if (a > 180) a -= 360;
        if (a < -180) a += 360;
        return a;
    }
}