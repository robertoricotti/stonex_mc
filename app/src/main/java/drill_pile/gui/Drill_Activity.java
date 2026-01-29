package drill_pile.gui;

import static gui.MyApp.errorCode;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static packexcalib.exca.ExcavatorLib.toolEndCoord;
import static packexcalib.exca.Sensors_Decoder.Deg_boom1;
import static packexcalib.exca.Sensors_Decoder.Deg_bucket;
import static packexcalib.exca.Sensors_Decoder.Deg_pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_roll;
import static utils.MyMCUtils.projectPointOnAxis3D;
import static utils.MyTypes.JOYSTICKS;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.example.stx_dig.R;

import DPAD.DPadHelper;
import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.draw_class.MyColorClass;
import gui.gps.NmeaGenerator;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import services.Joystick_Service;
import services.PointService;
import utils.DistToPoint;
import utils.MyData;
import utils.MyMCUtils;
import utils.Utils;

public class Drill_Activity extends BaseClass {
    int coloreAlto = Color.CYAN;
    int coloreBasso = Color.YELLOW;
    int coloreDashed = Color.CYAN;
    double poleHDT = 0;
    double poleTilt = 0;
    double mastHDT = 0;
    double mastTilt = 0;
    static int ringColor = MyColorClass.colorConstraint;
    static int tricolor = MyColorClass.colorConstraint;
    static int arrowColor = MyColorClass.colorConstraint;
    static float kostant = 1.2f;
    public static int typeVistaDrill;
    public static boolean isDrilling = false;
    int flip = 0;
    float rotationCont;

    Dialog_Drill_GNSS dialogDrillGnss;
    View divisorioC, divisorioDx, divisorioUp, divisorioDw, topViewCanvas, bubbleCanvas;
    ImageView digMenu, drilltool, typeView, Status, folders, playpause, lineReference, tiposnap,
            zoom_P, zoom_M, zoom_C, compass, quotaIndicator, infoPoint, drillSet, puntatore;
    ConstraintLayout topview, bubble;
    VerticalTargetIndicatorView indicator;
    TextView idpalo, txthdt, txttilt, txtdepth, uomesure, textInfo, tiltInfo, txttiltActual, txthdtActual;
    LinearLayout sideLayout;
    int colorUp, colorDown, colorGreen;
    Dialog_AutoSnap dialogAutoSnap;
    Dialog_InfoPoint dialogInfoPoint;
    Dialog_DrillSet dialogDrillSet;
    Guideline cent_v, side;
    public static boolean showCroce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill);
        initTollerances();
        findView();
        init();
        onClick();


    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, PointService.class));
        if(DataSaved.isCanOpen==JOYSTICKS){
            startService(new Intent(this, Joystick_Service.class));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyData.push("scaleFactor3D", String.valueOf(DataSaved.scale_Factor3D));
        stopService(new Intent(this, PointService.class));
        if(DataSaved.isCanOpen==JOYSTICKS){
            stopService(new Intent(this, Joystick_Service.class));
        }
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
        dialogInfoPoint = new Dialog_InfoPoint(this);
        dialogDrillSet = new Dialog_DrillSet(this);
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
        drillSet = findViewById(R.id.drillSet);
        infoPoint = findViewById(R.id.infoPoint);
        puntatore = findViewById(R.id.puntatore);
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
        ((Drill_TopView) topViewCanvas).setTargetScale(1.25f);
        ((Drill_TopView) topViewCanvas).setUiRotationDeg(90*DataSaved.Drill_Screen);
        ((Drill_Bubble)bubbleCanvas).setUiRotationDeg(90*DataSaved.Drill_Screen);
        switch (DataSaved.temaSoftware) {
            case 0:
                tiposnap.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                tiposnap.setImageTintList(getColorStateList(R.color.white));
                uomesure.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                uomesure.setTextColor(getColor(R.color.white));
                puntatore.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                puntatore.setImageTintList(getColorStateList(R.color.white));
                zoom_P.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_P.setImageTintList(getColorStateList(R.color.white));
                zoom_M.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_M.setImageTintList(getColorStateList(R.color.white));
                zoom_C.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_C.setImageTintList(getColorStateList(R.color.white));

                ((Drill_TopView) topViewCanvas).setColorTarget_Alto(Color.CYAN);
                ((Drill_TopView) topViewCanvas).setColorTarget_Basso(Color.YELLOW);
                ((Drill_TopView) topViewCanvas).setColoreCroce(Color.YELLOW);
                ((Drill_TopView) topViewCanvas).setColorDashed_Line(Color.CYAN);
                coloreAlto = Color.CYAN;
                coloreBasso = Color.YELLOW;
                coloreDashed = Color.CYAN;

                break;

            case 1:
            case 2:
                tiposnap.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                tiposnap.setImageTintList(getColorStateList(R.color._____cancel_text));
                uomesure.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                uomesure.setTextColor(getColor(R.color._____cancel_text));
                puntatore.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                puntatore.setImageTintList(getColorStateList(R.color._____cancel_text));
                zoom_P.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                zoom_P.setImageTintList(getColorStateList(R.color._____cancel_text));
                zoom_M.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                zoom_M.setImageTintList(getColorStateList(R.color._____cancel_text));
                zoom_C.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                zoom_C.setImageTintList(getColorStateList(R.color._____cancel_text));
                ((Drill_TopView) topViewCanvas).setColorTarget_Alto(Color.BLUE);
                ((Drill_TopView) topViewCanvas).setColorTarget_Basso(getResources().getColor(R.color.bg));
                ((Drill_TopView) topViewCanvas).setColoreCroce(getResources().getColor(R.color.bg));
                ((Drill_TopView) topViewCanvas).setColorDashed_Line(Color.BLUE);
                coloreAlto = Color.BLUE;
                coloreBasso = getResources().getColor(R.color.bg);
                coloreDashed = Color.BLUE;

                break;


        }
        uomesure.setText(Utils.getMetriSimbol().replace("[", "").replace("]", ""));


    }

    private void onClick() {
        zoom_P.setOnClickListener(view -> {
            DataSaved.scale_Factor3D *= kostant;
            DataSaved.scale_Factor3D = Math.max(0.4f, Math.min(DataSaved.scale_Factor3D, 6.5f));
        });
        zoom_M.setOnClickListener(view -> {
            DataSaved.scale_Factor3D /= kostant;
            DataSaved.scale_Factor3D = Math.max(0.4f, Math.min(DataSaved.scale_Factor3D, 6.5f));
        });
        zoom_C.setOnClickListener(view -> {
            Drill_TopView.offsetX = 0;
            Drill_TopView.offsetY = 0;
        });
        puntatore.setOnClickListener(view -> {
            showCroce = !showCroce;
        });
        drillSet.setOnClickListener(view -> {
            if (!dialogDrillSet.dialog.isShowing()) {
                dialogDrillSet.show();
            }
        });
        infoPoint.setOnClickListener(view -> {
            if (DataSaved.Selected_Point3D_Drill == null) {
                new CustomToast(this, "No Point Selected!").show();
            } else {
                if (!dialogInfoPoint.dialog.isShowing()) {
                    dialogInfoPoint.show();
                }
            }

        });
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
        setCommonElelemnts();
        double extraHeading = NmeaListener.roof_Orientation + DataSaved.offsetSwingExca;
        if (DataSaved.Extra_Heading == 0) {
            extraHeading = 0;
        }
        double viewHeading = (NmeaListener.mch_Orientation + DataSaved.deltaGPS2) + extraHeading;
        setBubble(viewHeading);

        boolean bitOnHead = (PointService.distXYToHead <= DataSaved.Drill_tolleranza_XY)
                && (PointService.okZ || Double.isNaN(PointService.dzToHead)); // se quota non disponibile
        ((Drill_TopView) topViewCanvas).setBitOnHoleHead(bitOnHead);
        if (PointService.okStart) {
            ((Drill_TopView) topViewCanvas).setColorTarget_Basso(Color.GREEN);
            ((Drill_TopView) topViewCanvas).setColorTarget_Alto(getResources().getColor(R.color.verde_sfondo_scuro));
            ((Drill_TopView) topViewCanvas).setColorDashed_Line(getResources().getColor(R.color.verde_sfondo_scuro));
        } else {
            ((Drill_TopView) topViewCanvas).setColorTarget_Basso(coloreBasso);
            ((Drill_TopView) topViewCanvas).setColorTarget_Alto(coloreAlto);
            ((Drill_TopView) topViewCanvas).setColorDashed_Line(coloreDashed);
        }
        if (isDrilling) {
            ((Drill_Bubble) bubbleCanvas).setCrossOnly(false); // voglio freccia SEMPRE
            setIndicator();
        } else {
            ((Drill_Bubble) bubbleCanvas).setCrossOnly(PointService.okStart); // se vuoi “READY”
        }


        mastHDT = My_LocationCalc.calcBearingXY(
                ExcavatorLib.coordTool[0], ExcavatorLib.coordTool[1],
                toolEndCoord[0], toolEndCoord[1]);


        mastTilt = MyMCUtils.calculateTotalTilt(ExcavatorLib.correctToolPitch,
                ExcavatorLib.correctToolRoll);
        if (DataSaved.Selected_Point3D_Drill != null) {
            poleHDT = DataSaved.Selected_Point3D_Drill.getHeadingDeg();

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
        if (!isInRangeAngle(mastTilt, 0, DataSaved.Drill_tolleranza_HDT)) {
            confronto = mastHDT;
        }

        if (isInRangeAngle(confronto, poleHDT, DataSaved.Drill_tolleranza_HDT)) {
            txthdtActual.setTextColor(Color.GREEN);
        } else {
            txthdtActual.setTextColor(Color.WHITE);
        }
        if (isInRangeAngle(mastTilt, poleTilt, DataSaved.Drill_tolleranza_Angolo)) {
            txttiltActual.setTextColor(Color.GREEN);
        } else {
            txttiltActual.setTextColor(Color.WHITE);
        }

        topViewCanvas.invalidate();
        bubbleCanvas.invalidate();
    }

    private void setCommonElelemnts() {
        float cen = 0.35f;
        float lef = -0.01f;
        float rig = 0.92f;
        float sid = 0.92f;
        float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rotBus = rotBus % 360;
        compass.setRotation(rotBus);
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
                zoom_P.setVisibility(View.VISIBLE);
                zoom_M.setVisibility(View.VISIBLE);
                zoom_C.setVisibility(View.VISIBLE);
                compass.setVisibility(View.VISIBLE);
                break;

            case 1:

                tiltInfo.setVisibility(View.VISIBLE);
                cent_v.setGuidelinePercent(lef);
                zoom_P.setVisibility(View.VISIBLE);
                zoom_M.setVisibility(View.VISIBLE);
                zoom_C.setVisibility(View.VISIBLE);
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
            String roww = DataSaved.Selected_Point3D_Drill.getRowId();
            if (roww == null) {
                roww = " ";
            }
            idpalo.setText("R:" + roww + " - " + "P:" + DataSaved.Selected_Point3D_Drill.getId());
            txthdt.setText(String.format("%.1f", DataSaved.Selected_Point3D_Drill.getHeadingDeg()) + "°");
            txttilt.setText(String.format("%.1f", DataSaved.Selected_Point3D_Drill.getTilt()) + "°");

            Point3D_Drill sel = DataSaved.Selected_Point3D_Drill;

            if (sel == null || toolEndCoord == null || toolEndCoord.length < 3) {
                txtdepth.setText("__.__");
                return;
            }

            boolean vertical = isHoleVertical(sel);

            Double hxObj = sel.getHeadX();
            Double hyObj = sel.getHeadY();
            Double hzObj = sel.getHeadZ();
            Double exObj = sel.getEndX();
            Double eyObj = sel.getEndY();
            Double ezObj = sel.getEndZ();

            double[] bit = toolEndCoord;

            boolean hasHead = (hxObj != null && hyObj != null && hzObj != null);
            boolean hasEnd = (exObj != null && eyObj != null && ezObj != null);

            if (!hasHead) {
                txtdepth.setText("__.__");
                return;
            }

            double hx = hxObj, hy = hyObj, hz = hzObj;

            if (!isDrilling) {
                // -------- PRIMA DI TRIVELLARE --------
                if (vertical) {
                    // delta Z (bit - head)
                    double dz = bit[2] - hz;
                    txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(dz)));
                } else {
                    // palo inclinato: distanza 3D bit<->head (usa service se presente)
                    double d3 = PointService.dist3DToHead;
                    if (isFinite(d3)) {
                        txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(d3)));
                    } else {
                        double dx = bit[0] - hx;
                        double dy = bit[1] - hy;
                        double dz = bit[2] - hz;
                        double dist3D = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(dist3D)));
                    }
                }

            } else {
                // -------- DURANTE TRIVELLAZIONE --------
                if (!hasEnd) {
                    // fallback: quello che avevi tu
                    txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(deltaQuota3D())));
                    return;
                }

                double ex = exObj, ey = eyObj, ez = ezObj;

                if (vertical) {
                    // verticale: residuo in Z verso fondo (endZ - bitZ)
                    double dzEnd = ez - bit[2]; // >0 se manca ancora da scendere
                    txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(dzEnd)));
                } else {
                    // inclinato: residuo lungo asse palo (consigliato)
                    double s = distAlongAxisFromHead(bit, hx, hy, hz, ex, ey, ez); // metri da head lungo asse
                    if (!isFinite(s)) {
                        txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(deltaQuota3D())));
                        return;
                    }

                    // lunghezza palo
                    double L = Math.sqrt((ex - hx) * (ex - hx) + (ey - hy) * (ey - hy) + (ez - hz) * (ez - hz));

                    // clamp per avere range fisico 0..L
                    double sClamped = clamp(s, 0.0, L);
                    double remaining = L - sClamped; // metri che mancano al fondo lungo asse

                    txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(remaining)));
                }
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
        String s0 = "Tool E: " + Utils.showCoords(String.valueOf(toolEndCoord[0]));
        String s1 = "Tool N: " + Utils.showCoords(String.valueOf(toolEndCoord[1]));
        String s2 = "Tool Z: " + Utils.showCoords(String.valueOf(toolEndCoord[2]));
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
            indicator.setCurrentValue((float) toolEndCoord[2]);//TODO adattare in realtime

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
        setDpad();
    }
    private void setDpad(){
        DPadHelper.getInstance().update(
                NmeaGenerator.HEADING,
                -90+DataSaved.offsetStick,
                0,
                DataSaved.demoEAST,
                DataSaved.demoNORD,
                -90+Deg_bucket,
                30+Deg_boom1,
                0,
                Deg_roll,
                Deg_pitch,
                new double[]{0,0,DataSaved.demoZ}



        );
    }
    private void settaFreccia() {

        Point3D_Drill sel = DataSaved.Selected_Point3D_Drill;
        if (sel == null) return;

        double[] bit = toolEndCoord;
        if (bit == null || bit.length < 3) return;

        Double hxObj = sel.getHeadX(), hyObj = sel.getHeadY(), hzObj = sel.getHeadZ();
        if (hxObj == null || hyObj == null || hzObj == null) return;

        boolean vertical = isHoleVertical(sel);

        // targetError > 0  => sei "sopra" il target (devi scendere / aumentare avanzamento)
        // targetError < 0  => sei "sotto" il target (devi salire / diminuire avanzamento)
        double targetError;

        if (!isDrilling) {
            // =========================
            // PRIMA DI TRIVELLARE
            // =========================
            if (vertical) {
                // confronto in Z (bitZ - headZ)
                targetError = bit[2] - hzObj;
                // >0 => bit sopra la testa -> devi scendere (freccia giù)
                // <0 => bit sotto la testa -> devi salire  (freccia su)
            } else {
                // inclinato: guida a "portare il bit sulla testa" in 3D
                double dx = bit[0] - hxObj;
                double dy = bit[1] - hyObj;
                double dz = bit[2] - hzObj;
                double dist3D = Math.sqrt(dx * dx + dy * dy + dz * dz);

                // target = 0 (vuoi arrivare esattamente sulla testa)
                // qui però serve anche il segno per dire su/giù:
                // usiamo solo dz per la freccia quota, mentre la distanza 3D la mostri come numero.
                targetError = dz; // coerente: >0 sopra -> scendi, <0 sotto -> sali
            }

            double tol = DataSaved.Drill_tolleranza_Z;

            if (targetError > tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(0);
            } else if (targetError < -tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(180);
            } else {
                quotaIndicator.setImageResource(R.drawable.outline_adjust_24);
                quotaIndicator.setRotation(0);
            }

            return;
        }

        // =========================
        // DURANTE TRIVELLAZIONE
        // =========================

        Double exObj = sel.getEndX(), eyObj = sel.getEndY(), ezObj = sel.getEndZ();
        boolean hasEnd = (exObj != null && eyObj != null && ezObj != null);

        if (!hasEnd) {
            // fallback: non posso stimare correttamente "quanto manca"
            quotaIndicator.setImageResource(R.drawable.outline_adjust_24);
            quotaIndicator.setRotation(0);
            return;
        }

        if (vertical) {
            // verticale: quanto manca al fondo in Z (endZ - bitZ)
            double remainingZ = ezObj - bit[2];
            // >0 => manca ancora (devi scendere) => freccia giù
            // <0 => sei oltre fondo => freccia su
            targetError = remainingZ;

            double tol = DataSaved.Drill_tolleranza_Z;

            if (targetError > tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(0);
            } else if (targetError < -tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(180);
            } else {
                quotaIndicator.setImageResource(R.drawable.outline_adjust_24);
                quotaIndicator.setRotation(0);
            }

        } else {
            // inclinato: quanto manca lungo asse (remainingAxis)
            double s = distAlongAxisFromHead(bit, hxObj, hyObj, hzObj, exObj, eyObj, ezObj);
            if (!isFinite(s)) {
                quotaIndicator.setImageResource(R.drawable.outline_adjust_24);
                quotaIndicator.setRotation(0);
                return;
            }

            double L = Math.sqrt((exObj - hxObj) * (exObj - hxObj) + (eyObj - hyObj) * (eyObj - hyObj) + (ezObj - hzObj) * (ezObj - hzObj));
            double sClamped = clamp(s, 0.0, L);
            double remainingAxis = L - sClamped;

            // target = 0 (vuoi arrivare a fondo), quindi "errore" = remainingAxis
            // >0 => manca ancora => scendi
            // <0 non succede con clamp, ma lo gestiamo uguale
            targetError = remainingAxis;

            // qui TI SERVE una tolleranza "lungo asse" (metri).
            // se non l’hai, usa Drill_tolleranza_Z come fallback ma meglio creare Drill_tolleranza_DepthAxis
            double tol = DataSaved.Drill_tolleranza_Z;

            if (targetError > tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(0);
            } else if (targetError < -tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(180);
            } else {
                quotaIndicator.setImageResource(R.drawable.outline_adjust_24);
                quotaIndicator.setRotation(0);
            }
        }
    }


    private double deltaQuota3D() {
        if (DataSaved.Selected_Point3D_Drill != null) {
            return DistToPoint.dist3D(toolEndCoord, new double[]{DataSaved.Selected_Point3D_Drill.getEndX(),
                    DataSaved.Selected_Point3D_Drill.getEndY(), DataSaved.Selected_Point3D_Drill.getEndZ()});
        } else {
            return 0;
        }
    }

    private boolean isInRangeAngle(double angle, double target, double deadband) {
        return Math.abs(angle - target) <= deadband;
    }

    private void initTollerances() {
        String Drill_tolleranza_Axis = MyData.get_String("Drill_tolleranza_Axis");
        String Drill_tolleranza_Z = MyData.get_String("Drill_tolleranza_Z");
        String Drill_tolleranza_XY = MyData.get_String("Drill_tolleranza_XY");
        String Drill_tolleranza_Angolo = MyData.get_String("Drill_tolleranza_Angolo");
        String Drill_tolleranza_HDT = MyData.get_String("Drill_tolleranza_HDT");

        if (Drill_tolleranza_Axis == null) {
            MyData.push("Drill_tolleranza_Axis", "0.03");
        }
        if (Drill_tolleranza_Z == null) {
            MyData.push("Drill_tolleranza_Z", "0.05");
        }
        if (Drill_tolleranza_XY == null) {
            MyData.push("Drill_tolleranza_XY", "0.03");
        }
        if (Drill_tolleranza_Angolo == null) {
            MyData.push("Drill_tolleranza_Angolo", "0.3");
        }
        if (Drill_tolleranza_HDT == null) {
            MyData.push("Drill_tolleranza_HDT", "0.5");
        }

        try {
            DataSaved.Drill_tolleranza_Axis = MyData.get_Double("Drill_tolleranza_Axis");
        } catch (Exception ignored) {

        }
        try {
            DataSaved.Drill_tolleranza_Z = MyData.get_Double("Drill_tolleranza_Z");
        } catch (Exception ignored) {

        }
        try {
            DataSaved.Drill_tolleranza_XY = MyData.get_Double("Drill_tolleranza_XY");
        } catch (Exception ignored) {

        }
        try {
            DataSaved.Drill_tolleranza_Angolo = MyData.get_Double("Drill_tolleranza_Angolo");
        } catch (Exception ignored) {

        }
        try {
            DataSaved.Drill_tolleranza_HDT = MyData.get_Double("Drill_tolleranza_HDT");
        } catch (Exception ignored) {

        }


    }

    private static boolean isHoleVertical(Point3D_Drill sel) {
        double tiltProj = (sel.getTilt() != null) ? sel.getTilt() : 0.0;
        return tiltProj < 1.0; // soglia verticale 1°
    }

    private static boolean isFinite(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    private static double distAlongAxisFromHead(
            double[] bit,  // [E,N,Z]
            double hx, double hy, double hz,
            double ex, double ey, double ez
    ) {
        double ax = ex - hx;
        double ay = ey - hy;
        double az = ez - hz;

        double L = Math.sqrt(ax * ax + ay * ay + az * az);
        if (L < 1e-9) return Double.NaN;

        // versore asse
        double ux = ax / L;
        double uy = ay / L;
        double uz = az / L;

        // vettore head->bit
        double bx = bit[0] - hx;
        double by = bit[1] - hy;
        double bz = bit[2] - hz;

        // proiezione scalare (metri lungo asse, 0=head, L=end)
        return (bx * ux + by * uy + bz * uz);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private void setBubble(double viewHeading) {
        /// bubble
        // --------------------
// BUBBLE (unica UI sempre uguale)
// cambia solo input: errE/errN + testo + colori + triangoli
// --------------------
        Point3D_Drill sel = DataSaved.Selected_Point3D_Drill;


// drilling overlay (croce rotante) SOLO come overlay grafico
        if (isDrilling) {
            rotationCont = (rotationCont + 1) % 360;
            ((Drill_Bubble) bubbleCanvas).setDrillingMode(true, rotationCont);
        } else {
            rotationCont = 0;
            ((Drill_Bubble) bubbleCanvas).setDrillingMode(false, 0);
        }

// Se non ho selezione -> reset bubble
        if (sel == null || sel.getHeadX() == null || sel.getHeadY() == null) {
            ((Drill_Bubble) bubbleCanvas).setCenterDistance("???");
            ((Drill_Bubble) bubbleCanvas).setPlanError(0, 0);
            ((Drill_Bubble) bubbleCanvas).setHeadingDeg(viewHeading);
            ((Drill_Bubble) bubbleCanvas).setTriangles(false, false, false, false);
            // colori neutri
            ((Drill_Bubble) bubbleCanvas).setColors(
                    getColor(R.color.rosso_sfondo_scuro),
                    MyColorClass.colorSfondo,
                    getColor(R.color.rosso_sfondo_scuro),
                    MyColorClass.colorConstraint,
                    getColor(R.color.rosso_sfondo_scuro)
            );
        } else {

            // verticale se tilt progetto < 1°
            double tiltProj = (sel.getTilt() != null) ? sel.getTilt() : 0.0;
            boolean isVertical = tiltProj < 1.0;

            boolean hasEndXY = (sel.getEndX() != null && sel.getEndY() != null);
            boolean hasEndXYZ = (sel.getEndX() != null && sel.getEndY() != null && sel.getEndZ() != null
                    && sel.getHeadZ() != null);

            double bitX = toolEndCoord[0];
            double bitY = toolEndCoord[1];

            double headX = sel.getHeadX();
            double headY = sel.getHeadY();

            // 1) ERRORE (errE/errN) = VETTORE DI CORREZIONE NEL MONDO
            double errE, errN;

            if (!isDrilling) {
                // ---- NAVIGAZIONE / START ----
                if (!isVertical) {
                    // palo inclinato -> guida verso TESTA
                    errE = headX - bitX;
                    errN = headY - bitY;
                } else {
                    // palo verticale -> guida verso ASSE (se possibile) altrimenti verso testa
                    if (hasEndXY) {
                        PlanError.Result per = PlanError.calcPlanErrorToAxisXY(
                                bitX, bitY,
                                headX, headY,
                                sel.getEndX(), sel.getEndY(),
                                false
                        );
                        errE = per.projE - bitX;
                        errN = per.projN - bitY;
                    } else {
                        errE = headX - bitX;
                        errN = headY - bitY;
                    }
                }
            } else {
                // ---- DRILLING ---- guida SEMPRE verso ASSE (punto sull’asse alla quota bit)
                if (hasEndXYZ) {
                    double[] qpos = projectPointOnAxis3D(
                            toolEndCoord[0], toolEndCoord[1], toolEndCoord[2],
                            sel.getHeadX(), sel.getHeadY(), sel.getHeadZ(),
                            sel.getEndX(), sel.getEndY(), sel.getEndZ()
                    );
                    errE = qpos[0] - bitX;
                    errN = qpos[1] - bitY;
                } else if (hasEndXY) {
                    PlanError.Result per = PlanError.calcPlanErrorToAxisXY(
                            bitX, bitY,
                            headX, headY,
                            sel.getEndX(), sel.getEndY(),
                            false
                    );
                    errE = per.projE - bitX;
                    errN = per.projN - bitY;
                } else {
                    // fallback
                    errE = headX - bitX;
                    errN = headY - bitY;
                }
            }

            ((Drill_Bubble) bubbleCanvas).setPlanError(errE, errN);

            // 2) HEADING della vista (stesso sia nav che drill)
            // (IMPORTANTE: niente radianti, solo gradi 0..360)
            ((Drill_Bubble) bubbleCanvas).setHeadingDeg(viewHeading);

            // 3) TESTO CENTRALE
            if (!isDrilling && PointService.okStart) {
                ((Drill_Bubble) bubbleCanvas).setCenterDistance("READY");
            } else {
                double dShown;
                if (isDrilling) {
                    // durante drilling mostri SEMPRE dist da asse
                    dShown = PointService.distAxis;
                } else {
                    // in navigazione: testa se inclinato, asse se verticale
                    dShown = (!isVertical) ? PointService.distXYToHead : PointService.distAxis;
                }
                ((Drill_Bubble) bubbleCanvas).setCenterDistance(
                        Double.isFinite(dShown) ? Utils.readUnitOfMeasureLITE(String.valueOf(dShown)) : "???"
                );
            }

            // 4) TRIANGOLI: sì, tienili sempre (anche palo verticale = “in bolla”)
            ((Drill_Bubble) bubbleCanvas).setTriangles(
                    PointService.FrecciaUP,
                    PointService.FrecciaLEFT,
                    PointService.FrecciaDOWN,
                    PointService.FrecciaRIGHT
            );

            // 5) COLORI
            // ring = okTilt? verde : rosso
            int ringColorLocal = PointService.okTilt ? getColor(R.color.verde_sfondo_scuro)
                    : getResources().getColor(R.color.bg_sfsred);

            // arrowColor = in base alla distanza che stai mostrando (dShown), non pe[2] a caso
            double dForColor = isDrilling ? PointService.distAxis : ((!isVertical) ? PointService.distXYToHead : PointService.distAxis);

            int arrowColorLocal;
            if (!Double.isFinite(dForColor)) {
                arrowColorLocal = getColor(R.color.rosso_sfondo_scuro);
            } else if (dForColor <= DataSaved.Drill_tolleranza_XY) {
                arrowColorLocal = getColor(R.color.verde_sfondo_scuro);
            } else if (dForColor < 1.0) {
                arrowColorLocal = getColor(R.color.arancio_sfondo_scuro);
            } else {
                arrowColorLocal = getColor(R.color.rosso_sfondo_scuro);
            }

            int triColorLocal = (DataSaved.temaSoftware == 0) ? Color.YELLOW : Color.BLUE;

            // FIX IMPORTANTISSIMO: ordine corretto setColors(ring, in, arrow, text, tri)
            ((Drill_Bubble) bubbleCanvas).setColors(
                    ringColorLocal,
                    arrowColorLocal,
                    MyColorClass.colorSfondo,
                    MyColorClass.colorConstraint,
                    triColorLocal
            );

            // 6) NON nascondere mai la freccia durante drilling
            ((Drill_Bubble) bubbleCanvas).setCrossOnly(false);
        }


        //end bubble
    }


}