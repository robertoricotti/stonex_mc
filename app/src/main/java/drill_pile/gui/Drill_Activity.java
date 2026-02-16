package drill_pile.gui;

import static gui.MyApp.errorCode;
import static packexcalib.exca.DataSaved.Selected_Point3D_Drill;
import static packexcalib.exca.ExcavatorLib.coordTool;
import static packexcalib.exca.ExcavatorLib.correctToolPitch;
import static packexcalib.exca.ExcavatorLib.correctToolRoll;
import static packexcalib.exca.ExcavatorLib.toolEndCoord;
import static packexcalib.exca.Sensors_Decoder.normalizeAngle;
import static services.PointService.getAlignmentPointsById;
import static services.PointService.valoriTabella;
import static utils.MyMCUtils.projectPointOnAxis3D;
import static utils.MyTypes.JETGROUTING_MODE;
import static utils.MyTypes.ROCKDRILL_MODE;
import static utils.MyTypes.SOLARFARM_MODE;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.example.stx_dig.R;

import java.io.IOException;
import java.util.Locale;

import DPAD.DPadHelper;
import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.draw_class.MyColorClass;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import services.PointService;
import services.ReadProjectService;
import utils.MyData;
import utils.MyMCUtils;
import utils.Utils;

public class Drill_Activity extends BaseClass implements DrillPointsFullscreenDialog.OnHoleActionListener {
    private boolean running = false;
    private long startTime = 0L;
    TableLayout tableDepthInfo;
    private boolean play = false;
    private boolean stop = false;
    private boolean abort = false;
    private double Start_dE, Start_dN, Start_dZ, End_dE, End_dN, End_dZ, delta_Tilt, delta_Bearing;

    private String currentHoleId;
    private String startIso;

    int coloreAlto = Color.CYAN;
    int coloreBasso = Color.YELLOW;
    int coloreDashed = Color.BLUE;
    double poleHDT = 0;
    double poleTilt = 0;
    double mastHDT = 0;
    double mastTilt = 0;
    static int ringColor = MyColorClass.colorConstraint;
    static int tricolor = MyColorClass.colorConstraint;
    static int arrowColor = MyColorClass.colorConstraint;
    static float kostant = 1.2f;
    public static boolean isDrilling = false;
    int flip = 0;
    float rotationCont;

    Dialog_Drill_GNSS dialogDrillGnss;
    View divisorioC, divisorioDx, divisorioUp, divisorioDw, topViewCanvas, bubbleCanvas;
    ImageView digMenu, drilltool, Status, folders, playpause, lineReference, tiposnap, imgHdt,
            zoom_P, zoom_M, zoom_C, compass, quotaIndicator, infoPoint, drillSet, puntatore, abortisci, normal_stop, imgTilt;
    ConstraintLayout topview, bubble;
    VerticalTargetIndicatorView indicator;
    TextView idpalo, txthdt, txttilt, txtdepth, uomesure, textInfo, tiltInfo, txttiltActual, txthdtActual, diration;
    LinearLayout sideLayout;
    int colorUp, colorDown, colorGreen;
    Dialog_AutoSnap dialogAutoSnap;
    Dialog_InfoPoint dialogInfoPoint;
    Dialog_DrillSet dialogDrillSet;
    Guideline cent_v, side, centro;
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

    }

    @Override
    protected void onStop() {
        super.onStop();
        MyData.push("scaleFactor3D", String.valueOf(DataSaved.scale_Factor3D));
        stopService(new Intent(this, PointService.class));

    }

    private void findView() {

        diration = findViewById(R.id.diration);
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
        abortisci = findViewById(R.id.abortisci);
        normal_stop = findViewById(R.id.normalStop);
        cent_v = findViewById(R.id.cent_v);
        centro = findViewById(R.id.centro);
        side = findViewById(R.id.side);
        compass = findViewById(R.id.compass);
        textInfo = findViewById(R.id.textInfo);
        tiltInfo = findViewById(R.id.tiltInfo);
        drillSet = findViewById(R.id.drillSet);
        infoPoint = findViewById(R.id.infoPoint);
        puntatore = findViewById(R.id.puntatore);
        quotaIndicator = findViewById(R.id.quotaIndicator);
        txthdtActual = findViewById(R.id.txthdtActual);
        txttiltActual = findViewById(R.id.txttiltActual);
        imgHdt = findViewById(R.id.imgHdt);
        imgTilt = findViewById(R.id.imgTilt);
        tableDepthInfo = findViewById(R.id.tableDepthInfo);


    }

    private void init() {
        dialogDrillGnss = new Dialog_Drill_GNSS(this);
        dialogAutoSnap = new Dialog_AutoSnap(this);
        dialogInfoPoint = new Dialog_InfoPoint(this);
        dialogDrillSet = new Dialog_DrillSet(this);
        try {
            if (MyData.get_String("showCroce") != null) {
                showCroce = Boolean.parseBoolean(MyData.get_String("showCroce"));
            }
        } catch (Exception e) {
            showCroce = false;
        }

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
        if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {
            lineReference.setVisibility(View.VISIBLE);
            imgTilt.setVisibility(View.GONE);
            txttiltActual.setVisibility(View.GONE);
            txttilt.setVisibility(View.GONE);
            drilltool.setVisibility(View.GONE);
            int mchint = MyData.get_Int("MachineSelected");
            MyData.push("M" + mchint + "numeroAste", "0");
            DataSaved.numeroAste = 0;
        } else {
            lineReference.setVisibility(View.GONE);
            imgTilt.setVisibility(View.VISIBLE);
            txttiltActual.setVisibility(View.VISIBLE);
            txttilt.setVisibility(View.VISIBLE);
            drilltool.setVisibility(View.VISIBLE);
            int mchint = MyData.get_Int("MachineSelected");
            DataSaved.numeroAste = MyData.get_Int("M" + mchint + "numeroAste");
        }

        if (DataSaved.colorMode == 1) {
            colorUp = Color.RED;
            colorDown = Color.BLUE;
            colorGreen = getResources().getColor(R.color.verde_sfondo_scuro);
        } else {
            colorDown = Color.RED;
            colorUp = Color.BLUE;
            colorGreen = getResources().getColor(R.color.verde_sfondo_scuro);
        }
        divisorioC.setBackgroundColor(MyColorClass.colorConstraint);
        divisorioDw.setBackgroundColor(MyColorClass.colorConstraint);
        divisorioDx.setBackgroundColor(MyColorClass.colorConstraint);
        divisorioUp.setBackgroundColor(MyColorClass.colorConstraint);
        bubble.setBackgroundColor(MyColorClass.colorSfondo);
        topview.setBackgroundColor(MyColorClass.colorSfondo);
        sideLayout.setBackgroundColor(MyColorClass.colorSfondo);

        indicator.setTolerance(DataSaved.Drill_tolleranza_Z);
        indicator.setColors(colorDown, colorUp, colorGreen);
        textInfo.setTextColor(MyColorClass.colorConstraint);
        tiltInfo.setTextColor(MyColorClass.colorConstraint);

        topViewCanvas = new Drill_TopView(this);
        topview.addView(topViewCanvas);
        bubbleCanvas = new Drill_Bubble(this);
        bubble.addView(bubbleCanvas);
        ((Drill_TopView) topViewCanvas).setTargetScale(1.25f);
        ((Drill_TopView) topViewCanvas).setUiRotationDeg(90 * DataSaved.Drill_Screen);
        ((Drill_Bubble) bubbleCanvas).setUiRotationDeg(90 * DataSaved.Drill_Screen);
        if (DataSaved.Drilling_Mode == JETGROUTING_MODE) {
            addEmptyRows(4, 4);
            ((Drill_Bubble) bubbleCanvas).setBubbleTransform(0.65f, 115f);
            tableDepthInfo.setVisibility(View.VISIBLE);
            centro.setGuidelinePercent(0.5f);
        } else {

            ((Drill_Bubble) bubbleCanvas).resetBubbleTransform();
            tableDepthInfo.setVisibility(View.GONE);
            centro.setGuidelinePercent(0.08f);
        }
        switch (DataSaved.temaSoftware) {
            case 0:
                tiposnap.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                tiposnap.setImageTintList(getColorStateList(R.color.Bg_yellow));
                uomesure.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                uomesure.setTextColor(getColor(R.color.Bg_yellow));
                puntatore.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                puntatore.setImageTintList(getColorStateList(R.color.Bg_yellow));
                zoom_P.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_P.setImageTintList(getColorStateList(R.color.Bg_yellow));
                zoom_M.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_M.setImageTintList(getColorStateList(R.color.Bg_yellow));
                zoom_C.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_C.setImageTintList(getColorStateList(R.color.Bg_yellow));
                drillSet.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                drillSet.setImageTintList(getColorStateList(R.color.Bg_yellow));
                infoPoint.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                infoPoint.setImageTintList(getColorStateList(R.color.Bg_yellow));

                ((Drill_TopView) topViewCanvas).setColorTarget_Alto(Color.CYAN);
                ((Drill_TopView) topViewCanvas).setColorTarget_Basso(Color.YELLOW);
                ((Drill_TopView) topViewCanvas).setColoreCroce(Color.YELLOW);
                ((Drill_TopView) topViewCanvas).setColorDashed_Line(Color.BLUE);
                coloreAlto = Color.CYAN;
                coloreBasso = Color.YELLOW;
                coloreDashed = Color.BLUE;

                break;

            case 1:
            case 2:
                tiposnap.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                tiposnap.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                uomesure.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                uomesure.setTextColor(getColor(R.color.colorStonexBlue));
                puntatore.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                puntatore.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                zoom_P.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                zoom_P.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                zoom_M.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                zoom_M.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                zoom_C.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                zoom_C.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                drillSet.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                drillSet.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                infoPoint.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                infoPoint.setImageTintList(getColorStateList(R.color.colorStonexBlue));
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
        diration.setTextColor(MyColorClass.colorConstraint);


    }

    private void onClick() {
        tiposnap.setOnClickListener(view -> {
            if (!dialogAutoSnap.dialog.isShowing()) {
                dialogAutoSnap.show();
            }
        });
        normal_stop.setOnLongClickListener(view -> {
            if (isDrilling) {
                stop = true;
                play = false;
                abort = false;
                Drill_Routine(DataSaved.Drilling_Mode, play, stop, abort);
            }
            return true;
        });
        abortisci.setOnLongClickListener(view -> {
            if (isDrilling) {
                abort = true;
                stop = false;
                play = false;
                Drill_Routine(DataSaved.Drilling_Mode, play, stop, abort);
            }
            return true;
        });
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
            MyData.push("showCroce", String.valueOf(showCroce));
        });
        drillSet.setOnClickListener(view -> {
            if (!dialogDrillSet.dialog.isShowing()) {
                dialogDrillSet.show();
            }
        });
        infoPoint.setOnClickListener(view -> {
            if (Selected_Point3D_Drill == null) {
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

        lineReference.setOnClickListener(view -> {
            DataSaved.isDefiningAB = !DataSaved.isDefiningAB;

            if (DataSaved.isDefiningAB) {
                // reset provvisorio
                DataSaved.alignAId = null;
                DataSaved.alignBId = null;
                // toast: "Pick point A"
                new CustomToast(Drill_Activity.this, "Pick point A").show_alert();
            } else {
                // toast: "Alignment selection canceled"
                new CustomToast(Drill_Activity.this, "Alignment selection canceled").show_long();
            }

            // invalidate topview se serve
            // topView.invalidate();
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
            if (DataSaved.Drilling_Mode == JETGROUTING_MODE&&!isDrilling ) {
                clearTable();
                setupTabella();

            }
            if (PointService.okStart) {
                if (!isDrilling) {
                    play = true;
                    stop = false;
                    abort = false;
                    Drill_Routine(DataSaved.Drilling_Mode, play, stop, abort);
                }
            }


        });

    }

    public void updateUI() {

        // =========================
        // 0) Allineamento AB (SOLARFARM)
        // =========================
        if (DataSaved.alignAId != null && DataSaved.alignBId != null) {
            Point3D_Drill[] pab = getAlignmentPointsById(DataSaved.alignAId, DataSaved.alignBId);
            if (pab != null && pab.length >= 2 && pab[0] != null && pab[1] != null
                    && pab[0].getHeadX() != null && pab[0].getHeadY() != null
                    && pab[1].getHeadX() != null && pab[1].getHeadY() != null) {

                DataSaved.ALLINEAMENTO_AB = My_LocationCalc.calcBearingXY(
                        pab[0].getHeadX(), pab[0].getHeadY(),
                        pab[1].getHeadX(), pab[1].getHeadY()
                );
            }
        }

        // =========================
        // 1) UI comune (testi, gps status, hole id, depth text, etc.)
        // =========================
        setCommonElelemnts();

        // =========================
        // 2) Bubble heading (vista)
        // =========================
        double extraHeading = NmeaListener.roof_Orientation + DataSaved.offsetSwingExca;
        if (DataSaved.Extra_Heading == 0) extraHeading = 0;

        double viewHeading = (NmeaListener.mch_Orientation + DataSaved.deltaGPS2) + extraHeading;
        setBubble(viewHeading);

        // =========================
        // 3) TopView: bit su testa (XY + Z se significativa)
        //    Nota: col nuovo service okZ è già TRUE quando Z non è significativa
        // =========================
        boolean bitOnHead = (PointService.distXYToHead <= DataSaved.Drill_tolleranza_XY) && PointService.okZ;
        ((Drill_TopView) topViewCanvas).setBitOnHoleHead(bitOnHead);

        // Colori target topview: verde se READY (okStart)
        if (PointService.okStart) {
            ((Drill_TopView) topViewCanvas).setColorTarget_Basso(Color.GREEN);
            ((Drill_TopView) topViewCanvas).setColorTarget_Alto(getResources().getColor(R.color.verde_sfondo_scuro));
            ((Drill_TopView) topViewCanvas).setColorDashed_Line(getResources().getColor(R.color.verde_sfondo_scuro));
        } else {
            ((Drill_TopView) topViewCanvas).setColorTarget_Basso(coloreBasso);
            ((Drill_TopView) topViewCanvas).setColorTarget_Alto(coloreAlto);
            ((Drill_TopView) topViewCanvas).setColorDashed_Line(coloreDashed);
        }

        // =========================
        // 4) Bubble: cross-only e indicator quota / timer
        // =========================
        if (isDrilling) {
            side.setGuidelinePercent(0.93f);

            ((Drill_Bubble) bubbleCanvas).setCrossOnly(false); // durante drill: mostra sempre frecce/guida
            setIndicator();
            setFrecciaDrill(); // se lo usi; altrimenti puoi rimuoverlo
            startTimer();
            diration.setText(getElapsedTime());
        } else {
            side.setGuidelinePercent(1.0f);
            // a riposo: se okStart puoi mostrare "READY" (cross-only)
            ((Drill_Bubble) bubbleCanvas).setCrossOnly(PointService.okStart);
            stopTimer();
            diration.setText("");
            // aggiorna anche la freccia quota “pre-drill”
            settaFreccia();
        }

        // =========================
        // 5) Mast angles (actual)
        // =========================
        mastHDT = My_LocationCalc.calcBearingXY(
                coordTool[0], coordTool[1],
                toolEndCoord[0], toolEndCoord[1]
        );

        mastTilt = MyMCUtils.calculateTotalTilt(correctToolPitch, correctToolRoll);

        // Debug tilt info pitch/roll raw
        String s = String.format(Locale.US, "Y: %7.2f°\nX: %7.2f°", correctToolPitch, correctToolRoll);
        tiltInfo.setText(s.replace(",", "."));

        txttiltActual.setText(String.format(Locale.US, "%.1f°", mastTilt).replace(",", "."));

        // =========================
        // 6) Hole target angles (target)
        // =========================
        Point3D_Drill sel = Selected_Point3D_Drill;

        double targetHdt;
        double targetTilt;

        if (sel != null) {
            targetTilt = (sel.getTilt() != null) ? sel.getTilt() : 0.0;

            if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {
                targetHdt = normalizeAngle(DataSaved.ALLINEAMENTO_AB);
            } else {
                targetHdt = (sel.getHeadingDeg() != null) ? sel.getHeadingDeg() : Double.NaN;
            }
        } else {
            targetTilt = Double.NaN;
            targetHdt = Double.NaN;
        }
        txttilt.setText(String.format("%.1f", targetTilt).replace(",", ".") + "°");
        txthdt.setText(String.format("%.1f", targetHdt).replace(",", ".") + "°");


        // =========================
        // 7) Heading UI (actual + colori coerenti con okOri)
        // =========================
        if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {

            double gpsHdt = normalizeAngle(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
            txthdtActual.setText(String.format(Locale.US, "%.1f°", gpsHdt).replace(",", "."));


        } else {
            // ROCK / JET: actual = mastHDT, target = hole bearing (se inclinato) ma okOri già gestito dal service
            txthdtActual.setText(String.format(Locale.US, "%.1f°", mastHDT).replace(",", "."));


        }

        // =========================
        // 8) Tilt UI (coerente con okTilt)
        // =========================
        // colori: verde sfondo se okOri, rosso se no
        if (PointService.okOri) {
            txthdtActual.setTextColor(Color.WHITE);
            txthdt.setTextColor(Color.WHITE);
            txthdt.setBackgroundColor(getColor(R.color.verde_sfondo_scuro));
            txthdtActual.setBackgroundColor(getColor(R.color.verde_sfondo_scuro));
            imgHdt.setBackgroundColor(getColor(R.color.verde_sfondo_scuro));
        } else {
            txthdtActual.setTextColor(Color.WHITE);
            txthdt.setTextColor(Color.WHITE);
            txthdt.setBackgroundColor(getColor(R.color._____cancel_text));
            txthdtActual.setBackgroundColor(getColor(R.color._____cancel_text));
            imgHdt.setBackgroundColor(getColor(R.color._____cancel_text));
        }
        if (PointService.okTilt) {
            txttiltActual.setTextColor(Color.WHITE);
            txttilt.setTextColor(Color.WHITE);
            txttilt.setBackgroundColor(getColor(R.color.verde_sfondo_scuro));
            txttiltActual.setBackgroundColor(getColor(R.color.verde_sfondo_scuro));
            imgTilt.setBackgroundColor(getColor(R.color.verde_sfondo_scuro));
        } else {
            txttiltActual.setTextColor(Color.WHITE);
            txttilt.setTextColor(Color.WHITE);
            txttilt.setBackgroundColor(getColor(R.color._____cancel_text));
            txttiltActual.setBackgroundColor(getColor(R.color._____cancel_text));
            imgTilt.setBackgroundColor(getColor(R.color._____cancel_text));
        }

        // =========================
        // 9) SOLARFARM: linea reference visible/defining e fine foro
        // =========================
        if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {

            if (DataSaved.isAutoSnap == 2) {
                lineReference.setVisibility(View.VISIBLE);
            } else {
                lineReference.setVisibility(View.INVISIBLE);
            }

            if (DataSaved.isDefiningAB) {
                lineReference.setBackground(getDrawable(R.drawable.custom_background_test3d_box_giallo));
            } else {
                lineReference.setBackground(getDrawable(R.drawable.custom_background_test3d_box_grigino));
            }

            // stop automatico quando raggiungi fondo (se hai endZ valido)
            if (isDrilling && sel != null && sel.getEndZ() != null) {
                double zeta = sel.getEndZ() + DataSaved.Drill_tolleranza_Z;
                if (toolEndCoord[2] < zeta) {
                    End_Foro_Ok();
                    isDrilling = false;
                }
            }

        } else {
            lineReference.setVisibility(View.INVISIBLE);
        }

        // =========================
        // 10) Tabella JET: aggiorna quando cambia hole
        // =========================
       /* if (DataSaved.Drilling_Mode == JETGROUTING_MODE && tableDepthInfo != null) {

            String newHoleId = (sel != null) ? sel.getId() : null;

            if (newHoleId == null) {
                // nessun hole selezionato -> tieni righe vuote
                clearTable();
                addEmptyRows(4, 4);
                currentHoleId = null;

            } else if (currentHoleId == null || !currentHoleId.equals(newHoleId)) {
                // hole cambiato -> ricrea tabella
                currentHoleId = newHoleId;

                clearTable();
                // se vuoi SEMPRE 8 righe presenti:
                addEmptyRows(4, 4);

                // se vuoi popolarle con i valori (se li hai già aggiornati da service)
                // NB: setupTabella() aggiunge righe, quindi se la vuoi "sovrascrivere"
                // devi prima clearare di nuovo o chiamarla al posto di addEmptyRows.
                clearTable();
                setupTabella();
            }
        }*/

        // =========================
        // 11) Enable/Disable pulsanti in base a drill state + okStart
        // =========================
        if (isDrilling) {
            playpause.setAlpha(0.3f);
            normal_stop.setAlpha(1.0f);
            abortisci.setAlpha(1.0f);

            digMenu.setEnabled(false);
            digMenu.setAlpha(0.3f);

            lineReference.setEnabled(false);
            lineReference.setAlpha(0.3f);

            drillSet.setEnabled(false);
            drillSet.setVisibility(View.INVISIBLE);

        } else {
            normal_stop.setAlpha(0.3f);
            abortisci.setAlpha(0.3f);


            playpause.setAlpha(PointService.okStart ? 1.0f : 0.3f);

            digMenu.setEnabled(true);
            digMenu.setAlpha(1.0f);

            lineReference.setEnabled(true);
            lineReference.setAlpha(1.0f);

            drillSet.setEnabled(true);
            drillSet.setVisibility(View.VISIBLE);
        }

        // =========================
        // 12) Invalidate canvases
        // =========================
        topViewCanvas.invalidate();
        bubbleCanvas.invalidate();
    }


    private void setCommonElelemnts() {
        textInfo.setText(setTesto());
        float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rotBus = rotBus % 360;
        compass.setRotation(rotBus);
        if (DataSaved.gpsOk && errorCode == 0) {

            Status.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
            Status.setBackground(getDrawable(R.drawable.custom_background_test3d_box_gpsok));
            flip = 0;
        } else {

            flipFlop();
            flip += 1;
            flip = flip % 20;
        }
        if (Selected_Point3D_Drill != null) {
            String roww = Selected_Point3D_Drill.getRowId();
            if (roww == null) {
                roww = " ";
            }
            idpalo.setText("R: " + roww + "   -   " + "P: " + Selected_Point3D_Drill.getId());


        } else {
            idpalo.setText("R:___ P:___");

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


        if (isDrilling) {
            normal_stop.setAlpha(1.0f);
            abortisci.setAlpha(1.0f);
            digMenu.setEnabled(false);
            digMenu.setAlpha(0.3f);
            lineReference.setEnabled(false);
            lineReference.setAlpha(0.3f);
            drillSet.setEnabled(false);
            drillSet.setVisibility(View.INVISIBLE);

        } else {
            normal_stop.setAlpha(0.3f);
            abortisci.setAlpha(0.3f);
            if (!PointService.okStart) {
                playpause.setAlpha(0.3f);
            } else {
                playpause.setAlpha(1.0f);
            }
            digMenu.setEnabled(true);
            digMenu.setAlpha(1.0f);
            lineReference.setEnabled(true);
            lineReference.setAlpha(1.0f);
            drillSet.setEnabled(true);
            drillSet.setVisibility(View.VISIBLE);
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
        String s0 = "Tool E: " + Utils.showCoords(String.valueOf(toolEndCoord[0])).replace(",", ".");
        String s1 = "Tool N: " + Utils.showCoords(String.valueOf(toolEndCoord[1])).replace(",", ".");
        String s2 = "Tool Z: " + Utils.showCoords(String.valueOf(toolEndCoord[2])).replace(",", ".");
        String p = "Project: " + DataSaved.progettoSelected_POINT.substring(DataSaved.progettoSelected_POINT.lastIndexOf("/") + 1);
        if (DataSaved.coordOrder == 0) {
            return new String(s0 + "\n" + s1 + "\n" + s2 + "\n" + p);
        } else {
            return new String(s1 + "\n" + s0 + "\n" + s2 + "\n" + p);
        }
    }

    private void setIndicator() {
        try {
            Point3D_Drill sel = Selected_Point3D_Drill;
            if (sel == null || indicator == null) return;

            double bitZ = safeBitZ(toolEndCoord);
            if (Double.isNaN(bitZ)) return;

            Double headZ = sel.getHeadZ();
            Double endZ = sel.getEndZ();

            // Se Z non è significativa -> indicator neutro
            if (!isZValid(headZ)) {
                indicator.setTolerance(DataSaved.Drill_tolleranza_Z);
                indicator.setTargetValue(0.0);
                indicator.setRange(-1.0, 1.0);
                indicator.setCurrentValue(0.0);
                return;
            }

            // Target: prima = headZ, durante = endZ se valido altrimenti headZ
            double target = (!isDrilling)
                    ? headZ
                    : (isZValid(endZ) ? endZ : headZ);

            indicator.setTolerance(DataSaved.Drill_tolleranza_Z);
            indicator.setTargetValue(target);

            // Range “visivo”: taralo come preferisci
            double low = target - 0.5;
            double high = target + 2.0;

            indicator.setRange(low, high);
            indicator.setCurrentValue(bitZ);

        } catch (Exception ignored) {
        }
    }


    private void locateMachine() {

        if (DataSaved.my_comPort == 4) {
            try {
                DataSaved.demoNORD = DataSaved.drill_points.get(0).getHeadY();
                DataSaved.demoEAST = DataSaved.drill_points.get(0).getHeadX();
                DataSaved.demoZ = DataSaved.drill_points.get(0).getHeadZ() + 4;
                MyData.push("demoNORD", String.valueOf(DataSaved.demoNORD));
                MyData.push("demoEAST", String.valueOf(DataSaved.demoEAST));
                MyData.push("demoZ", String.valueOf(DataSaved.demoZ));

            } catch (Exception e) {
                try {
                    DataSaved.demoNORD = DataSaved.points.get(0).getY();
                    DataSaved.demoEAST = DataSaved.points.get(0).getX();
                    DataSaved.demoZ = DataSaved.points.get(0).getZ() + 3;
                    MyData.push("demoNORD", String.valueOf(DataSaved.demoNORD));
                    MyData.push("demoEAST", String.valueOf(DataSaved.demoEAST));
                    MyData.push("demoZ", String.valueOf(DataSaved.demoZ));

                } catch (Exception ex) {
                    try {
                        DataSaved.demoNORD = DataSaved.polylines.get(0).getVertices().get(0).getY();
                        DataSaved.demoEAST = DataSaved.polylines.get(0).getVertices().get(0).getX();
                        DataSaved.demoZ = DataSaved.polylines.get(0).getVertices().get(0).getZ() + 3;
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

    private void setDpad() {

        DPadHelper.getInstance().setXYZ(new double[]{MyData.get_Double("demoEAST"), MyData.get_Double("demoNORD"), MyData.get_Double("demoZ")});

    }

    private void settaFreccia() {

        Point3D_Drill sel = Selected_Point3D_Drill;
        if (sel == null) return;

        double bitZ = safeBitZ(toolEndCoord);
        if (Double.isNaN(bitZ)) return;

        Double headZ = sel.getHeadZ();

        // Z non significativa: neutro e testo vuoto
        if (!isZValid(headZ)) {
            quotaIndicator.setImageResource(R.drawable.outline_arrows_left_right_circle_96);
            quotaIndicator.setRotation(0);
            txtdepth.setBackgroundColor(Color.DKGRAY);
            quotaIndicator.setBackgroundColor(Color.DKGRAY);
            txtdepth.setText(""); // ✅
            return;
        }

        // errore quota: positivo => bit sopra testa => devi scendere
        double err = bitZ - headZ;
        double tol = DataSaved.Drill_tolleranza_Z;

        // ✅ testo: distanza in quota dalla drillbit alla testa (con segno o assoluto)
        // Qui ti metto "err" con segno: + = sopra, - = sotto
        txtdepth.setText(fmtM((err)));

        if (err > tol) {
            quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
            quotaIndicator.setRotation(0);
            txtdepth.setBackgroundColor(colorDown);
            quotaIndicator.setBackgroundColor(colorDown);

        } else if (err < -tol) {
            quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
            quotaIndicator.setRotation(180);
            txtdepth.setBackgroundColor(colorUp);
            quotaIndicator.setBackgroundColor(colorUp);

        } else {
            quotaIndicator.setImageResource(R.drawable.outline_arrows_left_right_circle_96);
            quotaIndicator.setRotation(0);
            txtdepth.setBackgroundColor(colorGreen);
            quotaIndicator.setBackgroundColor(colorGreen);
        }
    }


    private void setFrecciaDrill() {

        Point3D_Drill sel = Selected_Point3D_Drill;
        if (sel == null) return;

        double[] bit = toolEndCoord;
        if (bit == null || bit.length < 3) return;

        Double hxObj = sel.getHeadX(), hyObj = sel.getHeadY(), hzObj = sel.getHeadZ();
        if (hxObj == null || hyObj == null) return;

        // se headZ non è valida, non ha senso profondità/remaining
        if (!isZValid(hzObj)) {
            quotaIndicator.setImageResource(R.drawable.outline_arrows_left_right_circle_96);
            quotaIndicator.setRotation(0);
            quotaIndicator.setBackgroundColor(Color.DKGRAY);
            txtdepth.setBackgroundColor(Color.DKGRAY);
            txtdepth.setText("");
            return;
        }

        Double exObj = sel.getEndX(), eyObj = sel.getEndY(), ezObj = sel.getEndZ();
        boolean hasEnd = (exObj != null && eyObj != null && isZValid(ezObj));

        if (!hasEnd) {
            quotaIndicator.setImageResource(R.drawable.outline_arrows_left_right_circle_96);
            quotaIndicator.setRotation(0);
            quotaIndicator.setBackgroundColor(getColor(R.color._____cancel_text));
            txtdepth.setBackgroundColor(getColor(R.color._____cancel_text));
            txtdepth.setText(""); // ✅
            return;
        }

        boolean vertical = isHoleVertical(sel);

        double tol = DataSaved.Drill_tolleranza_Z;

        if (vertical) {
            // ✅ testo: quanto manca al fondo in Z
            double remainingZ =bit[2]- ezObj  ;  // >0 manca ancora
            txtdepth.setText(fmtM((remainingZ)));

            // Frecce: se remainingZ > tol => devi scendere (down)
            if (remainingZ > tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(0);
                txtdepth.setBackgroundColor(colorDown);
                quotaIndicator.setBackgroundColor(colorDown);

            } else if (remainingZ < -tol) {
                // sei oltre il fondo
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(180);
                txtdepth.setBackgroundColor(colorUp);
                quotaIndicator.setBackgroundColor(colorUp);

            } else {
                quotaIndicator.setImageResource(R.drawable.outline_arrows_left_right_circle_96);
                quotaIndicator.setRotation(0);
                txtdepth.setBackgroundColor(colorGreen);
                quotaIndicator.setBackgroundColor(colorGreen);
            }

        } else {
            // ✅ inclinato: quanto manca lungo asse
            double s = distAlongAxisFromHead(bit, hxObj, hyObj, hzObj, exObj, eyObj, ezObj);
            if (!isFinite(s)) {
                quotaIndicator.setImageResource(R.drawable.outline_arrows_left_right_circle_96);
                quotaIndicator.setRotation(0);
                txtdepth.setBackgroundColor(getColor(R.color._____cancel_text));
                quotaIndicator.setBackgroundColor(getColor(R.color._____cancel_text));
                txtdepth.setText(""); // ✅
                return;
            }

            double ax = exObj - hxObj;
            double ay = eyObj - hyObj;
            double az = ezObj - hzObj;
            double L = Math.sqrt(ax * ax + ay * ay + az * az);

            double sClamped = clamp(s, 0.0, L);
            double remainingAxisRaw = L - s;
            double remainingAxis = clamp(remainingAxisRaw, -L, L);  // solo per sicurezza

            // ✅ testo: remainingAxis (metri lungo asse)
            txtdepth.setText(fmtM((remainingAxisRaw)));

            if (remainingAxisRaw  > tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(0);
                txtdepth.setBackgroundColor(colorDown);
                quotaIndicator.setBackgroundColor(colorDown);

            } else if (remainingAxisRaw  < -tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(180);
                txtdepth.setBackgroundColor(colorUp);
                quotaIndicator.setBackgroundColor(colorUp);

            } else {
                quotaIndicator.setImageResource(R.drawable.outline_arrows_left_right_circle_96);
                quotaIndicator.setRotation(0);
                txtdepth.setBackgroundColor(colorGreen);
                quotaIndicator.setBackgroundColor(colorGreen);
            }
        }
    }


    private static String fmtM(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "";
        return String.format(Locale.US, "%.3f", v).replace(",", ".");
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
        Point3D_Drill sel = Selected_Point3D_Drill;


// drilling overlay (croce rotante) SOLO come overlay grafico
        if (isDrilling) {
            rotationCont = (rotationCont + 1) % 360;
            ((Drill_Bubble) bubbleCanvas).setDrillingMode(true, rotationCont);
            /*if(DataSaved.Drilling_Mode==JETGROUTING_MODE){
                ((Drill_Bubble) bubbleCanvas).setBubbleTransform(0.75f, 120f);
            }*/
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
            int triColorLocal = (DataSaved.temaSoftware == 0) ? Color.YELLOW : Color.BLUE;
            // 4) TRIANGOLI: sì, tienili sempre (anche palo verticale = “in bolla”)

            if (PointService.okTilt) {
                ((Drill_Bubble) bubbleCanvas).setTriMeasure(0.45f, 0.95f, 1.25f);
                triColorLocal = Color.GREEN;
                ((Drill_Bubble) bubbleCanvas).setTriangles(
                        true,
                        true,
                        true,
                        true
                );
            } else {
                ((Drill_Bubble) bubbleCanvas).setTriMeasure(0.8f, 1.35f, 0.95f);
                ((Drill_Bubble) bubbleCanvas).setTriangles(
                        PointService.FrecciaUP,
                        PointService.FrecciaLEFT,
                        PointService.FrecciaDOWN,
                        PointService.FrecciaRIGHT
                );
            }


            // 5) COLORI
            // ring = okTilt? verde : rosso

            int ringColorLocal = PointService.isTiltWithinTolerance() ? getColor(R.color.verde_sfondo_scuro)
                    : getResources().getColor(R.color.bg_sfsred);

            // arrowColor = in base alla distanza che stai mostrando (dShown), non pe[2] a caso
            double dForColor = isDrilling ? PointService.distAxis : ((!isVertical) ? PointService.distXYToHead : PointService.distAxis);

            double mTol = DataSaved.Drill_tolleranza_XY;
            if (!isVertical) {
                mTol = DataSaved.Drill_tolleranza_Axis;
            }
            int arrowColorLocal;
            if (!Double.isFinite(dForColor)) {
                arrowColorLocal = getColor(R.color.rosso_sfondo_scuro);
            } else if (dForColor <= mTol) {
                arrowColorLocal = getColor(R.color.verde_sfondo_scuro);
            } else if (dForColor < 1.0) {
                arrowColorLocal = getColor(R.color.arancio_sfondo_scuro);
            } else {
                arrowColorLocal = getColor(R.color.rosso_sfondo_scuro);
            }


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

    /**
     *
     * QUI I METODI PER REPORT
     */
    private void Start_Foro() {
        if (Selected_Point3D_Drill == null) return;

        // HoleId uniforme: SOLO ID (se hai già buildHoleId ok, ma deve tornare point.getId())
        currentHoleId = buildHoleId(Selected_Point3D_Drill);
        startIso = NmeaListener.date_time_iso;

        // Stato runtime (in memoria)
        Selected_Point3D_Drill.setStatus(0); // TODO

        // Persistenza STATE (CSV)
        try {
            ReadProjectService.stateStore.upsertAndSave(
                    currentHoleId,
                    ProjectStateCsvStore.HoleState.TODO,
                    startIso,
                    null,
                    null,
                    null
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        isDrilling = true;
        refreshAfterStateChange();
    }


    private void End_Foro_Ok() {
        if (Selected_Point3D_Drill == null) return;

        final iredes.Point3D_Drill p = Selected_Point3D_Drill; // snapshot
        final String holeId = buildHoleId(p);
        final String endIso = NmeaListener.date_time_iso;

        // Stato runtime (in memoria)
        p.setStatus(1); // DONE

        // 1) Persistenza STATE (CSV)
        try {
            ReadProjectService.stateStore.upsertAndSave(
                    holeId,
                    ProjectStateCsvStore.HoleState.DONE,
                    startIso,
                    endIso,
                    "",
                    "HOLES/" + holeId + ".csv"
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 2) Report generale XLSX (una riga per foro)
        try {
            ProjectReportXlsxWriter.HoleSummaryRow row = new ProjectReportXlsxWriter.HoleSummaryRow();
            row.holeId = holeId;

            // Coordinate foro (attenzione: tu usi N/E come? qui assumo headY=N, headX=E)
            row.holeN = p.getHeadY();
            row.holeE = p.getHeadX();
            row.holeZ = p.getHeadZ();

            // Bearing/tilt/profondità/lunghezza se disponibili (o calcola con recomputeDerived)
            // Se non sei sicuro che siano calcolati:
            p.recomputeDerived();

            row.holeBearing = p.getHeadingDeg();
            row.holeTilt = p.getTilt();
            row.holeDepth = p.getDepth();
            row.holeLength = p.getLength();

            // tempi
            row.startTimeIso = startIso;
            row.endTimeIso = endIso;

            // TODO: questi li colleghiamo dopo (quando mi dici da dove arrivano)
            row.startdN = Start_dN;
            row.startdE = Start_dE;
            row.startdZ = Start_dZ;
            row.enddN = End_dN;
            row.enddE = End_dE;
            row.enddZ = End_dZ;
            row.dTilt = delta_Tilt;
            row.dBearing = delta_Bearing;
            row.avgPenetrationRate = null;

            row.state = "DONE";

            ReadProjectService.reportXlsxWriter.appendHoleRow(row);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // chiusura UI
        isDrilling = false;

        refreshAfterStateChange();
        if (DataSaved.Drilling_Mode == JETGROUTING_MODE) {
            clearTable();
            addEmptyRows(4, 4);
        }
    }


    private void End_Foro_Aborted() {
        if (Selected_Point3D_Drill == null) return;

        final iredes.Point3D_Drill p = Selected_Point3D_Drill; // snapshot
        final String holeId = buildHoleId(p);
        final String endIso = NmeaListener.date_time_iso;

        // Stato runtime (in memoria)
        p.setStatus(-1); // ABORTED (nel tuo modello è -1)

        // 1) Persistenza STATE (CSV)
        try {
            ReadProjectService.stateStore.upsertAndSave(
                    holeId,
                    ProjectStateCsvStore.HoleState.ABORTED,
                    startIso,
                    endIso,
                    "Operator aborted",
                    "HOLES/" + holeId + ".csv"
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 2) Report generale XLSX
        try {
            ProjectReportXlsxWriter.HoleSummaryRow row = new ProjectReportXlsxWriter.HoleSummaryRow();
            row.holeId = holeId;

            p.recomputeDerived();

            row.holeN = p.getHeadY();
            row.holeE = p.getHeadX();
            row.holeZ = p.getHeadZ();

            row.holeBearing = p.getHeadingDeg();
            row.holeTilt = p.getTilt();
            row.holeDepth = p.getDepth();
            row.holeLength = p.getLength();

            row.startTimeIso = startIso;
            row.endTimeIso = endIso;

            // per ora null, li riempiamo dopo
            row.startdN = Start_dN;
            row.startdE = Start_dE;
            row.startdZ = Start_dZ;
            row.enddN = End_dN;
            row.enddE = End_dE;
            row.enddZ = End_dZ;
            row.dTilt = delta_Tilt;
            row.dBearing = delta_Bearing;
            row.avgPenetrationRate = null;

            row.state = "ABORTED";

            ReadProjectService.reportXlsxWriter.appendHoleRow(row);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        isDrilling = false;

        refreshAfterStateChange();
        if (DataSaved.Drilling_Mode == JETGROUTING_MODE) {
            clearTable();
            addEmptyRows(4, 4);
        }
    }


    private void refreshAfterStateChange() {

        Point3D_Drill sel = Selected_Point3D_Drill;

        // 1) Nessun selected → solo refresh grafico
        if (sel == null) {
            //invalidateViews();
            return;
        }

        // 2) Stato sicuro (default TODO = 0)
        Integer st = sel.getStatus();
        if (st == null) st = 0;

        // 3) Se NON è TODO → deseleziona
        if (st != 0) {
            Selected_Point3D_Drill = null;
        }

        // 4) Refresh UI sempre
        //invalidateViews();
    }


    private String buildHoleId(iredes.Point3D_Drill p) {
        String id = p.getId() != null ? p.getId() : "";
        String row = p.getRowId() != null ? p.getRowId() : "";
        return row + id;
    }


    private void Drill_Routine(int mode, boolean play, boolean stop, boolean abort) {

        switch (mode) {
            case ROCKDRILL_MODE:
                if (play && !isDrilling) {
                    Start_dE = Math.abs(Selected_Point3D_Drill.getHeadX() - toolEndCoord[0]);
                    Start_dN = Math.abs(Selected_Point3D_Drill.getHeadY() - toolEndCoord[1]);
                    Start_dZ = Math.abs(Selected_Point3D_Drill.getHeadZ() - toolEndCoord[2]);
                    delta_Tilt = Math.abs(Selected_Point3D_Drill.getTilt() - MyMCUtils.calculateTotalTilt(correctToolPitch, correctToolRoll));
                    delta_Bearing = Math.abs(Selected_Point3D_Drill.getHeadingDeg() - My_LocationCalc.calcBearingXY(
                            coordTool[0], coordTool[1],
                            toolEndCoord[0], toolEndCoord[1]

                    ));

                    Start_Foro();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = true;
                }
                if (stop && isDrilling) {
                    End_dE = Math.abs(Selected_Point3D_Drill.getEndX() - toolEndCoord[0]);
                    End_dN = Math.abs(Selected_Point3D_Drill.getEndY() - toolEndCoord[1]);
                    End_dZ = Math.abs(Selected_Point3D_Drill.getEndZ() - toolEndCoord[2]);
                    End_Foro_Ok();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                if (abort && isDrilling) {
                    End_dE = 0;
                    End_dN = 0;
                    End_dZ = 0;
                    End_Foro_Aborted();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                break;

            case JETGROUTING_MODE:
                if (play && !isDrilling) {
                    Start_dE = Math.abs(Selected_Point3D_Drill.getHeadX() - toolEndCoord[0]);
                    Start_dN = Math.abs(Selected_Point3D_Drill.getHeadY() - toolEndCoord[1]);
                    Start_dZ = Math.abs(Selected_Point3D_Drill.getHeadZ() - toolEndCoord[2]);
                    delta_Tilt = Math.abs(Selected_Point3D_Drill.getTilt() - MyMCUtils.calculateTotalTilt(correctToolPitch, correctToolRoll));
                    delta_Bearing = 0;
                    Start_Foro();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = true;
                }
                if (stop && isDrilling) {
                    End_dE = 0;
                    End_dN = 0;
                    End_dZ = 0;
                    End_Foro_Ok();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                if (abort && isDrilling) {
                    End_dE = 0;
                    End_dN = 0;
                    End_dZ = 0;
                    End_Foro_Aborted();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                break;

            case SOLARFARM_MODE:
                //TODO il delta bearing va sulla linea selezionata, non sulla testa Mast
                if (play && !isDrilling) {
                    Start_dE = Math.abs(Selected_Point3D_Drill.getHeadX() - toolEndCoord[0]);
                    Start_dN = Math.abs(Selected_Point3D_Drill.getHeadY() - toolEndCoord[1]);
                    Start_dZ = Math.abs(Selected_Point3D_Drill.getHeadZ() - toolEndCoord[2]);
                    delta_Tilt = Math.abs(Selected_Point3D_Drill.getTilt() - MyMCUtils.calculateTotalTilt(correctToolPitch, correctToolRoll));
                    delta_Bearing = Math.abs(normalizeAngle(NmeaListener.mch_Orientation + DataSaved.deltaGPS2) - normalizeAngle(DataSaved.ALLINEAMENTO_AB));
                    Start_Foro();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = true;
                }
                if (stop && isDrilling) {
                    End_dE = 0;
                    End_dN = 0;
                    End_dZ = 0;
                    End_Foro_Ok();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                if (abort && isDrilling) {
                    End_dE = 0;
                    End_dN = 0;
                    End_dZ = 0;
                    End_Foro_Aborted();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                break;
        }
    }


    private void reopenHoleToTodo(Point3D_Drill p) {
        if (p == null || p.getId() == null || p.getId().trim().isEmpty()) return;

        final String holeId = p.getId().trim();
        final String nowIso = NmeaListener.date_time_iso;

        // 1) Runtime
        p.setStatus(0); // TODO

        // 2) STATE.csv: sovrascrivi + pulisci campi (qui IMPORTANT: usare "" non null)
        try {
            ReadProjectService.stateStore.upsertAndSave(
                    holeId,
                    ProjectStateCsvStore.HoleState.TODO,
                    "",   // startTimeIso pulito (oppure nowIso se vuoi memorizzare la riapertura)
                    "",   // endTimeIso pulito
                    "RE-OPENED", // note (audit anche nello state, opzionale)
                    ""    // holeReportFile pulito
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 3) REPORT.xlsx: aggiungi riga audit ciano
        try {
            ProjectReportXlsxWriter.HoleSummaryRow row = new ProjectReportXlsxWriter.HoleSummaryRow();
            row.holeId = holeId;

            row.holeN = p.getHeadY();
            row.holeE = p.getHeadX();
            row.holeZ = p.getHeadZ();

            p.recomputeDerived();
            row.holeBearing = p.getHeadingDeg();
            row.holeTilt = p.getTilt();
            row.holeDepth = p.getDepth();
            row.holeLength = p.getLength();

            row.startTimeIso = nowIso;   // momento evento
            row.endTimeIso = "";         // vuoto
            row.state = "RE-OPENED";

            ReadProjectService.reportXlsxWriter.appendHoleRow(row);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 4) Deseleziona se era selezionato
        if (DataSaved.Selected_Point3D_Drill != null &&
                holeId.equals(DataSaved.Selected_Point3D_Drill.getId())) {
            DataSaved.Selected_Point3D_Drill = null;
        }

        refreshAfterStateChange();
    }

    @Override
    public void onReopenRequested(@NonNull Point3D_Drill hole) {
        reopenHoleToTodo(hole); // chiama il metodo che abbiamo definito (STATE overwrite + XLSX append ciano)
        // Se vuoi anche aggiornare subito la mappa:
        // invalidateViews();
    }


    private void addRow(String tipo, String inizio, String fine, String pr) {

        TableRow row = new TableRow(this);
        row.setPadding(2, 2, 2, 2);
        row.setBackgroundColor(MyColorClass.colorSfondo);

        row.addView(createCell(tipo, true, 1f));      // Tipo
        row.addView(createCell(inizio, false, 1.8f));  // Inizio
        row.addView(createCell(fine, false, 1.8f));    // Fine
        row.addView(createCell(pr, false, 0.6f));      // Pr

        tableDepthInfo.addView(row);
    }


    private TextView createCell(String text, boolean bold, float weight) {

        TextView tv = new TextView(this);

        TableRow.LayoutParams params =
                new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        weight
                );

        tv.setLayoutParams(params);
        tv.setText(text != null ? text : "");
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(2, 2, 2, 2);
        tv.setTextColor(MyColorClass.colorConstraint);
        tv.setBackgroundResource(R.drawable.cell_border);
        tv.setTextSize(18f);

        if (bold) {
            tv.setTypeface(null, Typeface.BOLD);
        }

        return tv;
    }


    private void setupTabella() {
        if (valoriTabella[0] != null && !valoriTabella[0].isEmpty()) {
            addRow("DRL", valoriTabella[0], valoriTabella[1], valoriTabella[2]);
        } else {
            addRow("DRL", "", "", "");
        }
        if (valoriTabella[3] != null && !valoriTabella[3].isEmpty()) {
            addRow("DRL", valoriTabella[3], valoriTabella[4], valoriTabella[5]);
        } else {
            addRow("DRL", "", "", "");
        }
        if (valoriTabella[6] != null && !valoriTabella[6].isEmpty()) {
            addRow("DRL", valoriTabella[6], valoriTabella[7], valoriTabella[8]);
        } else {
            addRow("DRL", "", "", "");
        }
        if (valoriTabella[9] != null && !valoriTabella[9].isEmpty()) {
            addRow("DRL", valoriTabella[9], valoriTabella[10], valoriTabella[11]);
        } else {
            addRow("DRL", "", "", "");
        }
        if (valoriTabella[12] != null && !valoriTabella[12].isEmpty()) {
            addRow("JET", valoriTabella[12], valoriTabella[13], valoriTabella[14]);
        } else {
            addRow("JET", "", "", "");
        }
        if (valoriTabella[15] != null && !valoriTabella[15].isEmpty()) {
            addRow("JET", valoriTabella[15], valoriTabella[16], valoriTabella[17]);
        } else {
            addRow("JET", "", "", "");
        }
        if (valoriTabella[18] != null && !valoriTabella[18].isEmpty()) {
            addRow("JET", valoriTabella[18], valoriTabella[19], valoriTabella[20]);
        } else {
            addRow("JET", "", "", "");
        }
        if (valoriTabella[21] != null && !valoriTabella[21].isEmpty()) {
            addRow("JET", valoriTabella[21], valoriTabella[22], valoriTabella[23]);
        } else {
            addRow("JET", "", "", "");
        }
    }

    private void clearTable() {

        int childCount = tableDepthInfo.getChildCount();

        if (childCount > 1) {
            tableDepthInfo.removeViews(1, childCount - 1);
        }

    }

    private void addEmptyRows(int drlCount, int jetCount) {

        for (int i = 0; i < drlCount; i++) {
            addRow("DRL", "", "", "");
        }

        for (int i = 0; i < jetCount; i++) {
            addRow("JET", "", "", "");
        }
    }

    public void startTimer() {
        if (!running) {
            startTime = android.os.SystemClock.elapsedRealtime();
            running = true;
        }
    }

    public void stopTimer() {
        running = false;
    }

    public String getElapsedTime() {
        if (!running) return "0:00:00";

        long elapsedMillis = android.os.SystemClock.elapsedRealtime() - startTime;

        long seconds = elapsedMillis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format(Locale.getDefault(),
                "%d:%02d:%02d", hours, minutes, secs);
    }

    private static boolean isZValid(Double z) {
        return z != null && Math.abs(z) > 1e-9;
    }

    private static double safeBitZ(double[] toolEndCoord) {
        if (toolEndCoord == null || toolEndCoord.length < 3) return Double.NaN;
        return toolEndCoord[2];
    }

}