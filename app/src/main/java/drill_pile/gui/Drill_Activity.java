package drill_pile.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static drill_pile.gui.ProjectStateCsvStore.canonicalHoleId;
import static gui.MyApp.errorCode;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static packexcalib.exca.DataSaved.DRILL_STATUS;
import static packexcalib.exca.DataSaved.Selected_Point3D_Drill;
import static packexcalib.exca.ExcavatorLib.coordTool;
import static packexcalib.exca.ExcavatorLib.correctToolPitch;
import static packexcalib.exca.ExcavatorLib.correctToolRoll;
import static packexcalib.exca.ExcavatorLib.toolEndCoord;
import static packexcalib.exca.Sensors_Decoder.normalizeAngle;
import static services.PointService.AB_REVERSED;
import static services.PointService.valoriTabella;
import static utils.MyMCUtils.projectPointOnAxis3D;
import static utils.MyTypes.JETGROUTING_MODE;
import static utils.MyTypes.JOYSTICKS;
import static utils.MyTypes.ROCKDRILL_MODE;
import static utils.MyTypes.SOLARFARM_MODE;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.FragmentManager;

import com.example.stx_dig.R;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import DPAD.DPadHelper;
import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.draw_class.MyColorClass;
import iredes.DateTimeIsoCompat;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import services.CanSender;
import services.PointService;
import services.ReadProjectService;
import utils.FullscreenActivity;
import utils.MyData;
import utils.MyDeviceManager;
import utils.MyMCUtils;
import utils.Utils;

public class Drill_Activity extends BaseClass implements DrillPointsFullscreenDialog.OnHoleActionListener {
    String penRate ="";
    private long lastPenRateLogMs = 0L;
    private Point3D_Drill lastSavedHole = null;
    private String lastSavedHoleId = null;
    int dialCounter;
    Dialog_Error_Codes_Drill dialogErrorCodesDrill;
    private int lastDrillStatus = -1;
    private long drillStatus2SinceMs = 0L;
    private boolean drillStatus2ClickDone = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRepeating = false;
    public static String NOME_OPERATORE;
    static double[] StartForo, FineForo;

    public static int previousState;
    static double mHdT = 0;
    private boolean running = false;
    private long startTime = 0L;
    // ROCKDRILL pause/resume
    private boolean isDrillPaused = false;
    private long activeDrillMillis = 0L;
    private long activeDrillStartMs = 0L;

    // Scrittura file fuori dal main thread
    private final ExecutorService drillFileWriterExecutor = Executors.newSingleThreadExecutor();
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
    double mastHDT = 0;
    double mastTilt = 0;
    static float kostant = 1.2f;
    public static boolean isDrilling = false;
    int flip = 0;
    float rotationCont;
    Dialog_Raggio_Drill dialogRaggioDrill;
    Dialog_Drill_GNSS dialogDrillGnss;
    Dialog_Pile_Hydro dialogPileHydro;
    Dialog_Drill_Z_Adjust dialogDrillZAdjust;
    View divisorioC, divisorioDx, divisorioUp, divisorioDw, topViewCanvas, bubbleCanvas;
    ImageView digMenu, drilltool, Status, folders, playpause, lineReference, tiposnap, imgHdt, uomesure,postElev,
            zoom_P, zoom_M, zoom_C, compass, quotaIndicator, infoPoint, drillSet, puntatore, abortisci, normal_stop, imgTilt, mostratesto, hydromenu;
    ConstraintLayout topview, bubble;
    VerticalTargetIndicatorView indicator;
    TextView marcia, idpalo, txtHDTSet, txttilt, txtdepth, textInfo, tiltInfo, txttiltActual, txthdtActual, diration, einauto, rodNum;
    LinearLayout sideLayout;
    int colorUp, colorDown, colorGreen;
    Dialog_AutoSnap dialogAutoSnap;
    Dialog_InfoPoint dialogInfoPoint;
    Dialog_DrillSet dialogDrillSet;
    Dialog_Add_Rod dialogAddRod;
    Guideline cent_v, side, centro;
    public static boolean showCroce;
    Dialog_Operator_Login dialogOperatorLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill);
        dialogOperatorLogin = new Dialog_Operator_Login(this);
        if (NOME_OPERATORE == null) {
            if (!dialogOperatorLogin.dialog.isShowing()) {
                dialogOperatorLogin.show();
            }
        }
        initTollerances();
        findView();
        init();
        onClick();
        sendToEcu();


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
        rodNum = findViewById(R.id.rodNum);
        einauto = findViewById(R.id.hydroStat);
        marcia = findViewById(R.id.marcia);
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
        txtHDTSet = findViewById(R.id.txthdt);
        txttilt = findViewById(R.id.txttilt);
        txtdepth = findViewById(R.id.txtdepth);
        sideLayout = findViewById(R.id.sideLayout);
        lineReference = findViewById(R.id.lineReference);
        tiposnap = findViewById(R.id.tiposnap);
        uomesure = findViewById(R.id.uomesure);
        hydromenu = findViewById(R.id.hydromenu);
        postElev=findViewById(R.id.postElev);
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
        mostratesto = findViewById(R.id.mostratesto);


    }

    private void init() {
        dialogErrorCodesDrill = new Dialog_Error_Codes_Drill(this);
        dialogPileHydro = new Dialog_Pile_Hydro(this);
        dialogRaggioDrill = new Dialog_Raggio_Drill(this);
        dialogDrillGnss = new Dialog_Drill_GNSS(this);
        dialogAutoSnap = new Dialog_AutoSnap(this);
        dialogInfoPoint = new Dialog_InfoPoint(this);
        dialogDrillSet = new Dialog_DrillSet(this);
        dialogAddRod = new Dialog_Add_Rod(this);
        dialogDrillZAdjust = new Dialog_Drill_Z_Adjust(this);
        rodNum.setTextColor(MyColorClass.colorConstraint);
        if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {
            infoPoint.setImageResource((R.drawable.outline_contract_delete_96));
        }
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
        ((Drill_TopView) topViewCanvas).setDrawMachineSchema(DataSaved.drwaMachieSchema == 1);
        ((Drill_TopView) topViewCanvas).setUiRotationDeg(90 * DataSaved.Drill_Screen);
        ((Drill_Bubble) bubbleCanvas).setUiRotationDeg(90 * DataSaved.Drill_Screen);
        ((Drill_Bubble) bubbleCanvas).resetBubbleTransform();
        if (DataSaved.Drilling_Mode == JETGROUTING_MODE) {
            addEmptyRows(4, 4);
            //((Drill_Bubble) bubbleCanvas).setBubbleTransform(0.65f, 115f);
            tableDepthInfo.setVisibility(View.VISIBLE);
            centro.setGuidelinePercent(0.49f);
        } else {

            tableDepthInfo.setVisibility(View.GONE);
            centro.setGuidelinePercent(0.08f);
        }
        switch (DataSaved.temaSoftware) {
            case 0:
                tiposnap.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                tiposnap.setImageTintList(getColorStateList(R.color.light_yellow));
                uomesure.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                uomesure.setImageTintList(getColorStateList(R.color.light_yellow));
                puntatore.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                puntatore.setImageTintList(getColorStateList(R.color.light_yellow));
                hydromenu.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                hydromenu.setImageTintList(getColorStateList(R.color.light_yellow));
                zoom_P.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_P.setImageTintList(getColorStateList(R.color.light_yellow));
                zoom_M.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_M.setImageTintList(getColorStateList(R.color.light_yellow));
                zoom_C.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                zoom_C.setImageTintList(getColorStateList(R.color.light_yellow));
                drillSet.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                drillSet.setImageTintList(getColorStateList(R.color.light_yellow));
                infoPoint.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                infoPoint.setImageTintList(getColorStateList(R.color.light_yellow));
                mostratesto.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_chiaro));
                mostratesto.setImageTintList(getColorStateList(R.color.light_yellow));

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
                uomesure.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                puntatore.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                puntatore.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                hydromenu.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                hydromenu.setImageTintList(getColorStateList(R.color.colorStonexBlue));
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
                mostratesto.setBackground(getResources().getDrawable(R.drawable.sfondo_trasp_scuro));
                mostratesto.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                ((Drill_TopView) topViewCanvas).setColorTarget_Alto(Color.BLUE);
                ((Drill_TopView) topViewCanvas).setColorTarget_Basso(getResources().getColor(R.color.bg));
                ((Drill_TopView) topViewCanvas).setColoreCroce(getResources().getColor(R.color.bg));
                ((Drill_TopView) topViewCanvas).setColorDashed_Line(Color.BLUE);
                coloreAlto = Color.BLUE;
                coloreBasso = getResources().getColor(R.color.bg);
                coloreDashed = Color.BLUE;

                break;


        }
        //uomesure.setText(Utils.getMetriSimbol().replace("[", "").replace("]", ""));
        diration.setTextColor(MyColorClass.colorConstraint);


    }

    private void onClick() {
        postElev.setOnClickListener(v -> {

        });
        hydromenu.setOnClickListener(v -> {
            if (isTech) {
                if (!dialogPileHydro.dialog.isShowing()) {
                    dialogPileHydro.show();
                }
            }
        });
        uomesure.setOnClickListener(view -> {
          /*  if (!dialogRaggioDrill.dialog.isShowing()) {
                dialogRaggioDrill.show();
            }*/
            {
                FragmentManager fm = this.getSupportFragmentManager();
                String pointSiz = "";
                if (DataSaved.drill_points == null) {
                    pointSiz = "No Points";
                } else {
                    int[] stati = getPointStatus(DataSaved.drill_points);
                    pointSiz = "TOTAL:" + DataSaved.drill_points.size() + "   DONE:" + stati[2] + "   REFUSED:" + stati[1];
                }

                if (fm.findFragmentByTag("drill_grid") != null) return;

                DrillPointsFullscreenDialog
                        .newInstance("Drill Pattern " + pointSiz, ReadProjectService.conversionFactor)
                        .show(fm, "drill_grid");

            }
        });
        mostratesto.setOnClickListener(view -> {
            DataSaved.ShowText += 1;
            DataSaved.ShowText = DataSaved.ShowText % 2;
            MyData.push("Mostra_Testo", String.valueOf(DataSaved.ShowText));

        });
        mostratesto.setOnLongClickListener(view -> {

            String[] items = {
                    Drill_Text_Mode.opt_0,
                    Drill_Text_Mode.opt_1,
                    Drill_Text_Mode.opt_2,
                    Drill_Text_Mode.opt_3,
                    Drill_Text_Mode.opt_4,
                    Drill_Text_Mode.opt_5,
                    Drill_Text_Mode.opt_6
            };

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Drill Text Mode")
                    .setItems(items, (dialogInterface, which) -> {

                        DataSaved.Drill_Text_Mode = which;
                        MyData.push("Drill_Text_Mode", String.valueOf(which));

                    })
                    .show();

            // fullscreen
            FullscreenActivity.setFullScreen(dialog);

            return true;
        });
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
        setupAutoRepeat(zoom_P, () -> {
            DataSaved.scale_Factor3D *= kostant;
            DataSaved.scale_Factor3D = Math.max(0.1f, Math.min(DataSaved.scale_Factor3D, 6.5f));
        });
        setupAutoRepeat(zoom_M, () -> {
            DataSaved.scale_Factor3D /= kostant;
            DataSaved.scale_Factor3D = Math.max(0.1f, Math.min(DataSaved.scale_Factor3D, 6.5f));
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

            if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {
                //TODO rimuovi ultimo palo
                reopenLastSavedHole();


            } else {
                if (Selected_Point3D_Drill == null) {
                    new CustomToast(this, "No Point Selected!").show();
                } else {
                    if (!dialogInfoPoint.dialog.isShowing()) {
                        dialogInfoPoint.show();
                    }
                }
            }

        });
        compass.setOnLongClickListener(view -> {
            locateMachine();
            return false;
        });

        lineReference.setOnClickListener(view -> {

            previousState = DataSaved.isAutoSnap;

            if (DataSaved.isDefiningAB) {

                // --- Già in modalità definizione → chiedi se abortire ---
                AlertDialog dialog = new AlertDialog.Builder(Drill_Activity.this)
                        .setTitle("Cancel Alignment")
                        .setMessage("Alignment definition is in progress.\nDo you want to abort?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", (dialogInterface, which) -> {

                            previousState = 0;
                            DataSaved.isDefiningAB = false;
                            DataSaved.alignAId = null;
                            DataSaved.alignBId = null;

                            new CustomToast(
                                    Drill_Activity.this,
                                    "Alignment selection canceled"
                            ).show_long();

                        })
                        .show();

                // fullscreen
                FullscreenActivity.setFullScreen(dialog);

            } else {

                // --- Non attivo → chiedi se iniziare ---
                AlertDialog dialog = new AlertDialog.Builder(Drill_Activity.this)
                        .setTitle("Define Alignment AB")
                        .setMessage("Do you want to start defining alignment AB?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", (dialogInterface, which) -> {

                            DataSaved.isAutoSnap = 2;
                            DataSaved.isDefiningAB = true;
                            DataSaved.alignAId = null;
                            DataSaved.alignBId = null;

                            new CustomToast(
                                    Drill_Activity.this,
                                    "Pick point A"
                            ).show_alert();

                        })
                        .show();

                // fullscreen
                FullscreenActivity.setFullScreen(dialog);
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
            if (!dialogAddRod.dialog.isShowing()) {
                dialogAddRod.show();
            }
        });
        folders.setOnClickListener(view -> {

        });
        playpause.setOnClickListener(view -> {

            // PAUSE / RESUME solo ROCKDRILL durante foro in corso
            if (DataSaved.Drilling_Mode == ROCKDRILL_MODE && isDrilling) {
                if (isDrillPaused) {
                    resumeDrillClock();
                    playpause.setImageResource(R.drawable.btn_pause);
                    // eventuale cambio icona: qui puoi mettere icona pause
                    // playpause.setImageResource(R.drawable.xxx_pause);
                } else {
                    pauseDrillClock();
                    if (!dialogErrorCodesDrill.dialog.isShowing()) {
                        dialogErrorCodesDrill.show(256);
                    }
                    playpause.setImageResource(R.drawable.btn_play);
                    // qui tu aprirai Dialog_Error_Codes_Drill o altra dialog
                    // eventuale cambio icona: qui puoi mettere icona play
                    // playpause.setImageResource(R.drawable.xxx_play);
                }
                return;
            }

            if (DataSaved.Drilling_Mode == JETGROUTING_MODE && !isDrilling) {
                clearTable();
                setupTabella();
            }

            startDrillIfPossible();
        });
        quotaIndicator.setOnLongClickListener(v -> {
            if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {
                //TODO open dialog Z adjust
                if (!dialogDrillZAdjust.dialog.isShowing()) {
                    dialogDrillZAdjust.show();
                }
            }
            return true;
        });

    }

    public void updateUI() {
        if (NOME_OPERATORE != null) {
            if (isTech) {
                if (hydromenu != null) {
                    hydromenu.setVisibility(View.VISIBLE);
                }
            } else {
                if (hydromenu != null) {
                    hydromenu.setVisibility(View.INVISIBLE);
                }
            }
            if (DataSaved.ShowText == 1) {
                mostratesto.setAlpha(1.0f);
            } else {
                mostratesto.setAlpha(0.4f);
            }
            if (DataSaved.isCanOpen == JOYSTICKS) {
                marcia.setVisibility(View.VISIBLE);
                marcia.setText(DPadHelper.getMarcia(DPadHelper.getInstance().getStep()));
            } else {
                marcia.setVisibility(View.GONE);
            }
            // =========================
            // 0) Allineamento AB (SOLARFARM)
            // =========================
            if (DataSaved.alignAId != null && DataSaved.alignBId != null) {
                Point3D_Drill[] pab = getAlignmentPointsByKey(DataSaved.alignAId, DataSaved.alignBId);

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
                ((Drill_Bubble) bubbleCanvas).setCrossOnly(false);
                setIndicator();
                setFrecciaDrill();

                if (!isDrillPaused) {
                    startTimer();
                }
                String sTim = getElapsedTime();

                long nowLog = android.os.SystemClock.elapsedRealtime();

                if (nowLog - lastPenRateLogMs >= 1000L) {
                    lastPenRateLogMs = nowLog;
                   int index = MyData.get_Int("Unit_Of_Measure");

                    Double currentPenMmS = getCurrentPenRateMmS();
                    Double currentPenFtS = getCurrentPenRateFtS();
                    if(index>1){
                        penRate = String.format("%.3f",currentPenFtS)+" ft/S";
                    }else {
                        penRate = String.format("%.2f",currentPenMmS)+" mm/S";
                    }
                }

                diration.setText(sTim+"\n"+penRate);
            } else {
                if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {
                    side.setGuidelinePercent(0.93f);
                    setIndicator();
                } else {
                    side.setGuidelinePercent(1.0f);
                }
                // a riposo: se okStart puoi mostrare "READY" (cross-only)
                ((Drill_Bubble) bubbleCanvas).setCrossOnly(PointService.okStart);
                stopTimer();
                diration.setText("");
                penRate="";
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
            String s = String.format(Locale.US, "Y: %7.1f °\nX: %7.1f °", correctToolPitch, correctToolRoll);
            tiltInfo.setText(NOME_OPERATORE + "\n" + s.replace(",", "."));

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
            txtHDTSet.setText(String.format("%.1f", targetHdt).replace(",", ".") + "°");


            // =========================
            // 7) Heading UI (actual + colori coerenti con okOri)
            // =========================
            if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {

                double gpsHdt = normalizeAngle(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                mHdT = gpsHdt;
                txthdtActual.setText(String.format(Locale.US, "%.1f°", (targetHdt - gpsHdt)).replace(",", "."));


            } else {
                mHdT = mastHDT;
                // ROCK / JET: actual = mastHDT, target = hole bearing (se inclinato) ma okOri già gestito dal service
                txthdtActual.setText(String.format(Locale.US, "%.1f°", mastHDT).replace(",", "."));


            }

            // =========================
            // 8) Tilt UI (coerente con okTilt)
            // =========================
            // colori: verde sfondo se okOri, rosso se no
            if (PointService.okOri) {
                txthdtActual.setTextColor(Color.WHITE);
                txtHDTSet.setTextColor(Color.WHITE);
                txtHDTSet.setBackgroundColor(getColor(R.color.verde_sfondo_scuro));
                txthdtActual.setBackgroundColor(getColor(R.color.verde_sfondo_scuro));
                imgHdt.setImageResource(R.drawable.straight_96);
                imgHdt.setBackgroundColor(getColor(R.color.verde_sfondo_scuro));
            } else {
                txthdtActual.setTextColor(Color.WHITE);
                txtHDTSet.setTextColor(Color.WHITE);
                txtHDTSet.setBackgroundColor(getColor(R.color._____cancel_text));
                txthdtActual.setBackgroundColor(getColor(R.color._____cancel_text));
                double diff = signedAngleDiff(mHdT, targetHdt);

// Se sei in reverse, inverti il target di 180°
// (così la direzione rimane coerente visivamente)
                if (AB_REVERSED) {
                    diff = signedAngleDiff(mHdT, normalizeAngle(targetHdt + 180.0));
                }

                if (diff > 0) {
                    imgHdt.setImageResource(R.drawable.outline_rotate_right_96);
                } else {
                    imgHdt.setImageResource(R.drawable.outline_rotate_left_96);
                }
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
                if (DRILL_STATUS > 3 && !dialogErrorCodesDrill.dialog.isShowing()) {
                    dialCounter++;
                    if (dialCounter % 100 == 0) {
                        if (!dialogErrorCodesDrill.dialog.isShowing()) {
                            dialogErrorCodesDrill.show(DRILL_STATUS);
                            dialCounter = 0;
                        }
                    }
                } else {
                    dialCounter = 0;
                }
                rodNum.setVisibility(View.GONE);
                txtHDTSet.setVisibility(View.GONE);

                if (DataSaved.isDefiningAB) {
                    if (DataSaved.alignAId == null && DataSaved.alignBId == null) {
                        lineReference.setBackground(getDrawable(R.drawable.custom_background_test3d_box_gpsok));
                    }
                    if (DataSaved.alignAId != null && DataSaved.alignBId == null) {
                        lineReference.setBackground(getDrawable(R.drawable.custom_background_test3d_box_giallo));
                    }

                } else {
                    lineReference.setBackground(getDrawable(R.drawable.custom_background_test3d_box_grigino));
                }

                // stop automatico quando raggiungi fondo (se hai endZ valido)
                Log.w("DrillStatus", DRILL_STATUS + "");

// Quando DRILL_STATUS passa da qualsiasi valore a 2,
// parte il conteggio. Se resta 2 per almeno 1 secondo,
// viene fatto playpause.callOnClick() una sola volta.
                long now = android.os.SystemClock.elapsedRealtime();

                if (DRILL_STATUS != lastDrillStatus) {
                    lastDrillStatus = DRILL_STATUS;

                    if (DRILL_STATUS == 2) {
                        drillStatus2SinceMs = now;
                        drillStatus2ClickDone = false;
                    } else {
                        drillStatus2SinceMs = 0L;
                        drillStatus2ClickDone = false;
                    }
                }

                if (DataSaved.autoSavePoint == 1) {
                    if (DRILL_STATUS == 2
                            && !drillStatus2ClickDone
                            && drillStatus2SinceMs > 0L
                            && now - drillStatus2SinceMs >= 1000L) {

                        if (startDrillIfPossible()) {
                            drillStatus2ClickDone = true;
                        }
                    }
                }
                if (isDrilling && sel != null && sel.getEndZ() != null) {
                    double zeta = sel.getEndZ() + DataSaved.Drill_tolleranza_Z;
                    if (toolEndCoord[2] < zeta) {
                        End_Foro_Ok();
                        isDrilling = false;
                    }
                }

                switch (DRILL_STATUS) {
                    case 0:
                        einauto.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_grigio));
                        einauto.setTextColor(getResources().getColor(R.color._____cancel_text));
                        break;
                    case 1:
                    case 2:
                    case 3:
                        einauto.setBackground(getResources().getDrawable(R.drawable.sfondo_auto_enabled));
                        einauto.setTextColor(getResources().getColor(R.color.light_yellow));
                        break;

                    default:
                        einauto.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_grigio));
                        einauto.setTextColor(getResources().getColor(R.color._____cancel_text));
                        break;

                }

            } else {
                dialCounter = 0;
                rodNum.setVisibility(View.VISIBLE);
                txtHDTSet.setVisibility(View.VISIBLE);
                einauto.setVisibility(View.INVISIBLE);
                lineReference.setVisibility(View.INVISIBLE);
                rodNum.setText("ROD: " + DataSaved.numeroAste);
            }


            // =========================
            // 11) Enable/Disable pulsanti in base a drill state + okStart
            // =========================
            if (isDrilling) {

                playpause.setAlpha(DataSaved.Drilling_Mode == ROCKDRILL_MODE ? 1.0f : 0.3f);
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
    }


    private void setCommonElelemnts() {
        textInfo.setText(setTesto());

        float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rotBus = rotBus % 360;
        compass.setRotation(rotBus + (90 * DataSaved.Drill_Screen));
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
            CanSender.remainingZed = 0;
            return;
        }

        // errore quota: positivo => bit sopra testa => devi scendere
        double err = bitZ - headZ;
        double tol = DataSaved.Drill_tolleranza_Z;

        // ✅ testo: distanza in quota dalla drillbit alla testa (con segno o assoluto)
        // Qui ti metto "err" con segno: + = sopra, - = sotto
        txtdepth.setText(fmtM((err)));
        CanSender.remainingZed = err;

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
            CanSender.remainingZed = 0;
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
            CanSender.remainingZed = 0;
            return;
        }

        boolean vertical = isHoleVertical(sel);

        double tol = DataSaved.Drill_tolleranza_Z;

        if (vertical) {
            // ✅ testo: quanto manca al fondo in Z
            double remainingZ = bit[2] - ezObj;  // >0 manca ancora
            CanSender.remainingZed = remainingZ;
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
                CanSender.remainingZed = 0;
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
            CanSender.remainingZed = remainingAxisRaw;

            if (remainingAxisRaw > tol) {
                quotaIndicator.setImageResource(R.drawable.baseline_arrow_circle_down);
                quotaIndicator.setRotation(0);
                txtdepth.setBackgroundColor(colorDown);
                quotaIndicator.setBackgroundColor(colorDown);

            } else if (remainingAxisRaw < -tol) {
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
        return Utils.readSensorCalibration(String.valueOf(v).replace(",", "."));

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
    /**
     *
     * QUI I METODI PER REPORT
     */
    private void Start_Foro() {
        if (Selected_Point3D_Drill == null) return;

        final iredes.Point3D_Drill p = Selected_Point3D_Drill;

        // HoleId uniforme
        currentHoleId = canonicalHoleId(p);
        startIso = DateTimeIsoCompat.normalize(NmeaListener.date_time_Y_M_D);

        final String holeIdSnapshot = currentHoleId;
        final String startIsoSnapshot = startIso;

        resetDrillClock();

        // Stato runtime (in memoria)
        p.setStatus(0);

        StartForo = (toolEndCoord != null) ? toolEndCoord.clone() : null;
        FineForo = (toolEndCoord != null) ? toolEndCoord.clone() : null;

        isDrilling = true;

        // Persistenza STATE (CSV) fuori dal main thread
        runFileWriteAsync(() -> {
            if (ReadProjectService.stateStore != null) {
                ReadProjectService.stateStore.upsertAndSave(
                        holeIdSnapshot,
                        ProjectStateCsvStore.HoleState.TODO,
                        startIsoSnapshot,
                        null,
                        null,
                        null
                );
            }
        });

        refreshAfterStateChange();
    }


    private void End_Foro_Ok() {
        if (Selected_Point3D_Drill == null) return;

        final iredes.Point3D_Drill p = Selected_Point3D_Drill; // snapshot
        final int drillingMode = DataSaved.Drilling_Mode;

        final String holeId = canonicalHoleId(p);
        final String startIsoSnapshot = startIso;
        final String endIso = DateTimeIsoCompat.normalize(NmeaListener.date_time_Y_M_D);

        // Stato runtime (in memoria)
        p.setStatus(1); // DONE
        rememberLastSavedHole(p);

        FineForo = (toolEndCoord != null) ? toolEndCoord.clone() : null;

        final long effectiveDrillMillis = closeEffectiveDrillMillis();
        final String effectiveDuration = formatDurationHHmmssSSS(effectiveDrillMillis);

        // Assicura derived aggiornati
        p.recomputeDerived();

        final Double penMmS = computePenRateMmS(effectiveDrillMillis, StartForo, FineForo, p);
        final Double penFtS = (penMmS == null) ? null : mmPerSecToFtPerSecAlways(penMmS);

        // Conversioni unità utente
        final Double headN = toUserUnitsMeters(p.getHeadY());
        final Double headE = toUserUnitsMeters(p.getHeadX());
        final Double headZ = toUserUnitsMeters(p.getHeadZ());

        final Double endN = toUserUnitsMeters(p.getEndY());
        final Double endE = toUserUnitsMeters(p.getEndX());
        final Double endZ = toUserUnitsMeters(p.getEndZ());

        final Double depth = toUserUnitsMeters(p.getDepth());
        final Double length = toUserUnitsMeters(p.getLength());

        final Double sdN = toUserUnitsMeters(Start_dN);
        final Double sdE = toUserUnitsMeters(Start_dE);
        final Double sdZ = toUserUnitsMeters(Start_dZ);

        final Double edN = toUserUnitsMeters(End_dN);
        final Double edE = toUserUnitsMeters(End_dE);
        final Double edZ = toUserUnitsMeters(End_dZ);

        final Double bearing = p.getHeadingDeg();
        final Double tilt = p.getTilt();

        final Double embedmentM = (StartForo != null && FineForo != null)
                ? (StartForo[2] - FineForo[2])
                : 0;

        final Double embedmentUser = toUserUnitsMeters(Math.abs(embedmentM));
        final Double dTilt = delta_Tilt;
        final Double dBearing = delta_Bearing;

        switch (drillingMode) {

            case SOLARFARM_MODE: {
                final ProjectReportXlsxWriter.SolarRow row = new ProjectReportXlsxWriter.SolarRow();

                row.operator = NOME_OPERATORE;
                row.pileId = holeId;
                row.pileDescr = p.getDescription();

                row.pileN = headN;
                row.pileE = headE;
                row.pileZ = headZ;

                row.pileAzimuth = bearing;
                row.pileTilt = tilt;

                row.startTimeIso = startIsoSnapshot;
                row.endTimeIso = endIso;
                row.durationOverride = effectiveDuration;

                row.startdN = sdN;
                row.startdE = sdE;
                row.startdZ = sdZ;

                row.enddN = edN;
                row.enddE = edE;
                row.enddZ = edZ;

                row.embedment = embedmentUser;
                row.dTilt = dTilt;
                row.dAzimuth = dBearing;

                row.avgPenRateMmS = penMmS;
                row.avgPenRateFtS = penFtS;

                row.state = "DONE";
                row.comment = "";

                runFileWriteAsync(() -> {
                    if (ReadProjectService.stateStore != null) {
                        ReadProjectService.stateStore.upsertAndSave(
                                holeId,
                                ProjectStateCsvStore.HoleState.DONE,
                                startIsoSnapshot,
                                endIso,
                                "",
                                "HOLES/" + holeId + ".csv"
                        );
                    }

                    if (ReadProjectService.reportXlsxWriter != null) {
                        ReadProjectService.reportXlsxWriter.appendSolarRow(row);
                    }
                });

                break;
            }

            case JETGROUTING_MODE: {
                final ProjectReportXlsxWriter.JetRow row = new ProjectReportXlsxWriter.JetRow();

                row.operator = NOME_OPERATORE;
                row.holeId = holeId;
                row.holeDescr = p.getDescription();

                row.holeN = headN;
                row.holeE = headE;
                row.holeZ = headZ;

                row.holeEndN = endN;
                row.holeEndE = endE;
                row.holeEndZ = endZ;

                row.holeBearing = bearing;
                row.holeTilt = tilt;
                row.holeDepth = depth;
                row.holeLength = length;

                row.startTimeIso = startIsoSnapshot;
                row.endTimeIso = endIso;
                row.durationOverride = effectiveDuration;

                row.startdN = sdN;
                row.startdE = sdE;
                row.startdZ = sdZ;

                row.enddN = edN;
                row.enddE = edE;
                row.enddZ = edZ;

                row.dTilt = dTilt;
                row.dBearing = dBearing;

                row.state = "DONE";
                row.comment = "";

                runFileWriteAsync(() -> {
                    if (ReadProjectService.stateStore != null) {
                        ReadProjectService.stateStore.upsertAndSave(
                                holeId,
                                ProjectStateCsvStore.HoleState.DONE,
                                startIsoSnapshot,
                                endIso,
                                "",
                                "HOLES/" + holeId + ".csv"
                        );
                    }

                    if (ReadProjectService.reportXlsxWriter != null) {
                        ReadProjectService.reportXlsxWriter.appendJetRow(row);
                    }
                });

                break;
            }

            default: { // ROCKDRILL
                final ProjectReportXlsxWriter.RockRow row = new ProjectReportXlsxWriter.RockRow();

                row.operator = NOME_OPERATORE;
                row.holeId = holeId;
                row.holeDescr = p.getDescription();

                row.holeN = headN;
                row.holeE = headE;
                row.holeZ = headZ;

                row.holeEndN = endN;
                row.holeEndE = endE;
                row.holeEndZ = endZ;

                row.holeBearing = bearing;
                row.holeTilt = tilt;
                row.holeDepth = depth;
                row.holeLength = length;

                row.startTimeIso = startIsoSnapshot;
                row.endTimeIso = endIso;
                row.durationOverride = effectiveDuration;

                row.startdN = sdN;
                row.startdE = sdE;
                row.startdZ = sdZ;

                row.enddN = edN;
                row.enddE = edE;
                row.enddZ = edZ;

                row.dTilt = dTilt;
                row.dBearing = dBearing;

                row.rods = DataSaved.numeroAste;

                row.avgPenRateMmS = penMmS;
                row.avgPenRateFtS = penFtS;

                row.state = "DONE";
                row.comment = "";

                runFileWriteAsync(() -> {
                    if (ReadProjectService.stateStore != null) {
                        ReadProjectService.stateStore.upsertAndSave(
                                holeId,
                                ProjectStateCsvStore.HoleState.DONE,
                                startIsoSnapshot,
                                endIso,
                                "",
                                "HOLES/" + holeId + ".csv"
                        );
                    }

                    if (ReadProjectService.reportXlsxWriter != null) {
                        ReadProjectService.reportXlsxWriter.appendRockRow(row);
                    }
                });

                break;
            }
        }

        // chiusura UI immediata, senza aspettare scrittura file
        isDrilling = false;
        isDrillPaused = false;

        DataSaved.numeroAste = 0;
        int mchint = MyData.get_Int("MachineSelected");
        MyData.push("M" + mchint + "numeroAste", "0");

        refreshAfterStateChange();

        if (drillingMode == JETGROUTING_MODE) {
            clearTable();
            addEmptyRows(4, 4);
        }
    }


    private void End_Foro_Aborted() {
        if (Selected_Point3D_Drill == null) return;

        final iredes.Point3D_Drill p = Selected_Point3D_Drill; // snapshot
        final int drillingMode = DataSaved.Drilling_Mode;

        final String holeId = canonicalHoleId(p);
        final String startIsoSnapshot = startIso;
        final String endIso = DateTimeIsoCompat.normalize(NmeaListener.date_time_Y_M_D);

        // Stato runtime (in memoria)
        p.setStatus(-1); // ABORTED
        rememberLastSavedHole(p);

        FineForo = (toolEndCoord != null) ? toolEndCoord.clone() : null;

        final long effectiveDrillMillis = closeEffectiveDrillMillis();
        final String effectiveDuration = formatDurationHHmmssSSS(effectiveDrillMillis);

        p.recomputeDerived();

        final Double penMmS = computePenRateMmS(effectiveDrillMillis, StartForo, FineForo, p);
        final Double penFtS = (penMmS == null) ? null : mmPerSecToFtPerSecAlways(penMmS);

        final Double headN = toUserUnitsMeters(p.getHeadY());
        final Double headE = toUserUnitsMeters(p.getHeadX());
        final Double headZ = toUserUnitsMeters(p.getHeadZ());

        final Double endN = toUserUnitsMeters(p.getEndY());
        final Double endE = toUserUnitsMeters(p.getEndX());
        final Double endZ = toUserUnitsMeters(p.getEndZ());

        final Double depth = toUserUnitsMeters(p.getDepth());
        final Double length = toUserUnitsMeters(p.getLength());

        final Double sdN = toUserUnitsMeters(Start_dN);
        final Double sdE = toUserUnitsMeters(Start_dE);
        final Double sdZ = toUserUnitsMeters(Start_dZ);

        final Double edN = toUserUnitsMeters(End_dN);
        final Double edE = toUserUnitsMeters(End_dE);
        final Double edZ = toUserUnitsMeters(End_dZ);

        final Double bearing = p.getHeadingDeg();
        final Double tilt = p.getTilt();

        final Double embedmentM = (p.getHeadZ() != null && FineForo != null && FineForo.length >= 3)
                ? (p.getHeadZ() - FineForo[2])
                : null;

        final Double embedmentUser = toUserUnitsMeters(embedmentM);
        final Double dTilt = delta_Tilt;
        final Double dBearing = delta_Bearing;

        switch (drillingMode) {

            case SOLARFARM_MODE: {
                final ProjectReportXlsxWriter.SolarRow row = new ProjectReportXlsxWriter.SolarRow();

                row.operator = NOME_OPERATORE;
                row.pileId = holeId;
                row.pileDescr = p.getDescription();

                row.pileN = headN;
                row.pileE = headE;
                row.pileZ = headZ;

                row.pileAzimuth = bearing;
                row.pileTilt = tilt;

                row.startTimeIso = startIsoSnapshot;
                row.endTimeIso = endIso;
                row.durationOverride = effectiveDuration;

                row.startdN = sdN;
                row.startdE = sdE;
                row.startdZ = sdZ;

                row.enddN = edN;
                row.enddE = edE;
                row.enddZ = edZ;

                row.embedment = embedmentUser;
                row.dTilt = dTilt;
                row.dAzimuth = dBearing;

                row.avgPenRateMmS = penMmS;
                row.avgPenRateFtS = penFtS;

                row.state = "ABORTED";
                row.comment = "Operator aborted";

                runFileWriteAsync(() -> {
                    if (ReadProjectService.stateStore != null) {
                        ReadProjectService.stateStore.upsertAndSave(
                                holeId,
                                ProjectStateCsvStore.HoleState.ABORTED,
                                startIsoSnapshot,
                                endIso,
                                "Operator aborted",
                                "HOLES/" + holeId + ".csv"
                        );
                    }

                    if (ReadProjectService.reportXlsxWriter != null) {
                        ReadProjectService.reportXlsxWriter.appendSolarRow(row);
                    }
                });

                break;
            }

            case JETGROUTING_MODE: {
                final ProjectReportXlsxWriter.JetRow row = new ProjectReportXlsxWriter.JetRow();

                row.operator = NOME_OPERATORE;
                row.holeId = holeId;
                row.holeDescr = p.getDescription();

                row.holeN = headN;
                row.holeE = headE;
                row.holeZ = headZ;

                row.holeEndN = endN;
                row.holeEndE = endE;
                row.holeEndZ = endZ;

                row.holeBearing = bearing;
                row.holeTilt = tilt;
                row.holeDepth = depth;
                row.holeLength = length;

                row.startTimeIso = startIsoSnapshot;
                row.endTimeIso = endIso;
                row.durationOverride = effectiveDuration;

                row.startdN = sdN;
                row.startdE = sdE;
                row.startdZ = sdZ;

                row.enddN = edN;
                row.enddE = edE;
                row.enddZ = edZ;

                row.dTilt = dTilt;
                row.dBearing = dBearing;

                row.state = "ABORTED";
                row.comment = "Operator aborted";

                runFileWriteAsync(() -> {
                    if (ReadProjectService.stateStore != null) {
                        ReadProjectService.stateStore.upsertAndSave(
                                holeId,
                                ProjectStateCsvStore.HoleState.ABORTED,
                                startIsoSnapshot,
                                endIso,
                                "Operator aborted",
                                "HOLES/" + holeId + ".csv"
                        );
                    }

                    if (ReadProjectService.reportXlsxWriter != null) {
                        ReadProjectService.reportXlsxWriter.appendJetRow(row);
                    }
                });

                break;
            }

            default: { // ROCKDRILL
                final ProjectReportXlsxWriter.RockRow row = new ProjectReportXlsxWriter.RockRow();

                row.operator = NOME_OPERATORE;
                row.holeId = holeId;
                row.holeDescr = p.getDescription();

                row.holeN = headN;
                row.holeE = headE;
                row.holeZ = headZ;

                row.holeEndN = endN;
                row.holeEndE = endE;
                row.holeEndZ = endZ;

                row.holeBearing = bearing;
                row.holeTilt = tilt;
                row.holeDepth = depth;
                row.holeLength = length;

                row.startTimeIso = startIsoSnapshot;
                row.endTimeIso = endIso;
                row.durationOverride = effectiveDuration;

                row.startdN = sdN;
                row.startdE = sdE;
                row.startdZ = sdZ;

                row.enddN = edN;
                row.enddE = edE;
                row.enddZ = edZ;

                row.dTilt = dTilt;
                row.dBearing = dBearing;

                row.rods = DataSaved.numeroAste;

                row.avgPenRateMmS = penMmS;
                row.avgPenRateFtS = penFtS;

                row.state = "ABORTED";
                row.comment = "Operator aborted";

                runFileWriteAsync(() -> {
                    if (ReadProjectService.stateStore != null) {
                        ReadProjectService.stateStore.upsertAndSave(
                                holeId,
                                ProjectStateCsvStore.HoleState.ABORTED,
                                startIsoSnapshot,
                                endIso,
                                "Operator aborted",
                                "HOLES/" + holeId + ".csv"
                        );
                    }

                    if (ReadProjectService.reportXlsxWriter != null) {
                        ReadProjectService.reportXlsxWriter.appendRockRow(row);
                    }
                });

                break;
            }
        }

        // chiusura UI immediata, senza aspettare scrittura file
        isDrilling = false;
        isDrillPaused = false;

        DataSaved.numeroAste = 0;
        int mchint = MyData.get_Int("MachineSelected");
        MyData.push("M" + mchint + "numeroAste", "0");

        refreshAfterStateChange();

        if (drillingMode == JETGROUTING_MODE) {
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

    private void reopenHoleToTodo(Point3D_Drill p) {
        if (p == null || p.getId() == null || p.getId().trim().isEmpty()) return;

        final String holeId = canonicalHoleId(p);
        final String nowIso = NmeaListener.date_time_Y_M_D;

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
        // 3) REPORT.xlsx: aggiungi riga audit ciano
        try {
            p.recomputeDerived();

            final Double headN = toUserUnitsMeters(p.getHeadY());
            final Double headE = toUserUnitsMeters(p.getHeadX());
            final Double headZ = toUserUnitsMeters(p.getHeadZ());

            final Double bearing = p.getHeadingDeg();
            final Double tilt = p.getTilt();

            switch (DataSaved.Drilling_Mode) {

                case SOLARFARM_MODE: {
                    ProjectReportXlsxWriter.SolarRow row = new ProjectReportXlsxWriter.SolarRow();
                    row.operator = "";
                    row.pileId = holeId;
                    row.pileDescr = "";

                    row.pileN = headN;
                    row.pileE = headE;
                    row.pileZ = headZ;

                    row.pileAzimuth = bearing;
                    row.pileTilt = tilt;

                    row.startTimeIso = nowIso;
                    row.endTimeIso = "";

                    row.state = "RE-OPENED";
                    row.comment = "RE-OPENED";

                    ReadProjectService.reportXlsxWriter.appendSolarRow(row);
                    break;
                }

                case JETGROUTING_MODE: {
                    ProjectReportXlsxWriter.JetRow row = new ProjectReportXlsxWriter.JetRow();
                    row.operator = "";
                    row.holeId = holeId;
                    row.holeDescr = "";

                    row.holeN = headN;
                    row.holeE = headE;
                    row.holeZ = headZ;

                    row.holeBearing = bearing;
                    row.holeTilt = tilt;

                    row.startTimeIso = nowIso;
                    row.endTimeIso = "";

                    row.state = "RE-OPENED";
                    row.comment = "RE-OPENED";

                    ReadProjectService.reportXlsxWriter.appendJetRow(row);
                    break;
                }

                default: {
                    ProjectReportXlsxWriter.RockRow row = new ProjectReportXlsxWriter.RockRow();
                    row.operator = "";
                    row.holeId = holeId;
                    row.holeDescr = "";

                    row.holeN = headN;
                    row.holeE = headE;
                    row.holeZ = headZ;

                    row.holeBearing = bearing;
                    row.holeTilt = tilt;

                    row.startTimeIso = nowIso;
                    row.endTimeIso = "";

                    row.state = "RE-OPENED";
                    row.comment = "RE-OPENED";

                    ReadProjectService.reportXlsxWriter.appendRockRow(row);
                    break;
                }
            }

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

    private boolean hasUsableToolEndCoord() {
        return toolEndCoord != null
                && toolEndCoord.length >= 3
                && isFinite(toolEndCoord[0])
                && isFinite(toolEndCoord[1])
                && isFinite(toolEndCoord[2]);
    }

    private boolean hasUsableCoordTool() {
        return coordTool != null
                && coordTool.length >= 2
                && isFinite(coordTool[0])
                && isFinite(coordTool[1]);
    }

    private boolean canRunDrillRoutine(String action) {
        if (Selected_Point3D_Drill == null) {
            new CustomToast(this, "No Point Selected!").show_error();
            return false;
        }
        if (!hasUsableToolEndCoord()) {
            new CustomToast(this, "Invalid tool coordinates").show_error();
            return false;
        }
        return true;
    }

    private static double safeDouble(Double value, double fallback) {
        return (value != null && isFinite(value)) ? value : fallback;
    }

    private static double safeAbsDelta(Double target, double actual) {
        if (target == null || !isFinite(target) || !isFinite(actual)) return 0.0;
        return Math.abs(target - actual);
    }

    private double safeCurrentMastBearingDeg() {
        if (!hasUsableCoordTool() || !hasUsableToolEndCoord()) return 0.0;
        return My_LocationCalc.calcBearingXY(
                coordTool[0], coordTool[1],
                toolEndCoord[0], toolEndCoord[1]
        );
    }

    private void fillStartDeltas(Point3D_Drill p) {
        Start_dE = safeAbsDelta(p.getHeadX(), toolEndCoord[0]);
        Start_dN = safeAbsDelta(p.getHeadY(), toolEndCoord[1]);
        Start_dZ = safeAbsDelta(p.getHeadZ(), toolEndCoord[2]);
    }

    private void fillEndDeltas(Point3D_Drill p) {
        End_dE = safeAbsDelta(p.getEndX(), toolEndCoord[0]);
        End_dN = safeAbsDelta(p.getEndY(), toolEndCoord[1]);
        End_dZ = safeAbsDelta(p.getEndZ(), toolEndCoord[2]);
    }

    private void resetEndDeltas() {
        End_dE = 0;
        End_dN = 0;
        End_dZ = 0;
    }

    private void Drill_Routine(int mode, boolean play, boolean stop, boolean abort) {

        switch (mode) {
            case ROCKDRILL_MODE:

                if (play && !isDrilling) {
                    if (!canRunDrillRoutine("start")) return;

                    Point3D_Drill p = Selected_Point3D_Drill;
                    fillStartDeltas(p);

                    double actualTilt = MyMCUtils.calculateTotalTilt(correctToolPitch, correctToolRoll);
                    double targetTilt = safeDouble(p.getTilt(), actualTilt);
                    delta_Tilt = Math.abs(targetTilt - actualTilt);

                    double mastBearing = safeCurrentMastBearingDeg();
                    double targetBearing = safeDouble(p.getHeadingDeg(), mastBearing);
                    delta_Bearing = Math.abs(targetBearing - mastBearing);

                    Start_Foro();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = true;
                }
                if (stop && isDrilling) {
                    if (!canRunDrillRoutine("stop")) return;

                    Point3D_Drill p = Selected_Point3D_Drill;
                    fillEndDeltas(p);

                    End_Foro_Ok();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                if (abort && isDrilling) {
                    resetEndDeltas();
                    End_Foro_Aborted();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                break;

            case JETGROUTING_MODE:
                if (play && !isDrilling) {
                    if (!canRunDrillRoutine("start")) return;

                    Point3D_Drill p = Selected_Point3D_Drill;
                    fillStartDeltas(p);

                    double actualTilt = MyMCUtils.calculateTotalTilt(correctToolPitch, correctToolRoll);
                    double targetTilt = safeDouble(p.getTilt(), actualTilt);
                    delta_Tilt = Math.abs(targetTilt - actualTilt);
                    delta_Bearing = 0;

                    Start_Foro();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = true;
                }
                if (stop && isDrilling) {
                    resetEndDeltas();
                    End_Foro_Ok();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                if (abort && isDrilling) {
                    resetEndDeltas();
                    End_Foro_Aborted();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                break;

            case SOLARFARM_MODE:
                if (play && !isDrilling) {
                    if (!canRunDrillRoutine("start")) return;

                    Point3D_Drill p = Selected_Point3D_Drill;
                    fillStartDeltas(p);

                    double actualTilt = MyMCUtils.calculateTotalTilt(correctToolPitch, correctToolRoll);
                    double targetTilt = safeDouble(p.getTilt(), actualTilt);
                    delta_Tilt = Math.abs(targetTilt - actualTilt);

                    double ori = normalizeAngle(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                    double ab = normalizeAngle(DataSaved.ALLINEAMENTO_AB);
                    // delta firmato rispetto alla linea AB/BA
                    delta_Bearing = signedLineDeltaDeg(ori, ab);

                    Start_Foro();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = true;
                }
                if (stop && isDrilling) {
                    if (!canRunDrillRoutine("stop")) return;

                    Point3D_Drill p = Selected_Point3D_Drill;
                    fillEndDeltas(p);

                    End_Foro_Ok();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                if (abort && isDrilling) {
                    resetEndDeltas();
                    End_Foro_Aborted();
                    abort = false;
                    play = false;
                    stop = false;
                    isDrilling = false;
                }
                break;
        }
    }


    @Override
    public void onReopenRequested(@NonNull Point3D_Drill hole) {
        reopenHoleToTodo(hole); // chiama il metodo che abbiamo definito (STATE overwrite + XLSX append ciano)
        // Se vuoi anche aggiornare subito la mappa:
        // invalidateViews();
    }


    private void addRow(String tipo, String inizio, String fine, String pr) {

        TableRow row = new TableRow(this);
        row.setPadding(1, 1, 1, 1);
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
        tv.setPadding(1, 1, 1, 1);
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
        if (!running && !isDrillPaused) {
            activeDrillStartMs = android.os.SystemClock.elapsedRealtime();
            running = true;
        }
    }

    public void stopTimer() {
        running = false;
    }

    public String getElapsedTime() {
        return formatDurationHHmmss(getEffectiveDrillMillisLive());
    }

    //PLAYPAUSE ROCK
    private void resetDrillClock() {
        activeDrillMillis = 0L;
        activeDrillStartMs = android.os.SystemClock.elapsedRealtime();
        isDrillPaused = false;
        running = true;
    }

    private void pauseDrillClock() {
        if (!isDrilling || isDrillPaused) return;

        long now = android.os.SystemClock.elapsedRealtime();
        if (running && activeDrillStartMs > 0L) {
            activeDrillMillis += Math.max(0L, now - activeDrillStartMs);
        }

        running = false;
        isDrillPaused = true;
    }

    private void resumeDrillClock() {
        if (!isDrilling || !isDrillPaused) return;

        activeDrillStartMs = android.os.SystemClock.elapsedRealtime();
        running = true;
        isDrillPaused = false;
    }

    private long getEffectiveDrillMillisLive() {
        long total = activeDrillMillis;

        if (running && !isDrillPaused && activeDrillStartMs > 0L) {
            long now = android.os.SystemClock.elapsedRealtime();
            total += Math.max(0L, now - activeDrillStartMs);
        }

        return Math.max(0L, total);
    }

    private long closeEffectiveDrillMillis() {
        long total = getEffectiveDrillMillisLive();

        activeDrillMillis = total;
        activeDrillStartMs = 0L;
        running = false;
        isDrillPaused = false;

        return total;
    }

    private static String formatDurationHHmmss(long millis) {
        if (millis < 0L) millis = 0L;

        long seconds = millis / 1000L;
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;

        return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
    }

    private static String formatDurationHHmmssSSS(long millis) {
        if (millis < 0L) millis = 0L;

        long hours = millis / 3_600_000L;
        millis %= 3_600_000L;
        long minutes = millis / 60_000L;
        millis %= 60_000L;
        long seconds = millis / 1_000L;
        long ms = millis % 1_000L;

        return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, minutes, seconds, ms);
    }


    private static boolean isZValid(Double z) {
        return z != null && Math.abs(z) > 1e-9;
    }

    private static double safeBitZ(double[] toolEndCoord) {
        if (toolEndCoord == null || toolEndCoord.length < 3) return Double.NaN;
        return toolEndCoord[2];
    }

    private double signedAngleDiff(double current, double target) {

        double diff = target - current;

        // normalizza in range -180..+180
        diff = ((diff + 180) % 360 + 360) % 360 - 180;

        return diff;
    }

    private Point3D_Drill[] getAlignmentPointsByKey(String aKey, String bKey) {

        if (aKey == null || bKey == null) return null;

        Point3D_Drill A = null;
        Point3D_Drill B = null;

        for (Point3D_Drill p : DataSaved.drill_points) {   // <-- usa la tua lista reale
            String pk = buildPointKey(p);

            if (pk == null) continue;

            if (pk.equalsIgnoreCase(aKey.trim())) {
                A = p;
            }

            if (pk.equalsIgnoreCase(bKey.trim())) {
                B = p;
            }

            if (A != null && B != null) break;
        }

        if (A == null || B == null) return null;

        return new Point3D_Drill[]{A, B};
    }

    private String buildPointKey(Point3D_Drill p) {
        if (p == null) return null;

        String row = p.getRowId();
        String id = p.getId();

        row = (row == null) ? "" : row.trim();
        id = (id == null) ? "" : id.trim();

        if (id.isEmpty()) return null;

        if (!row.isEmpty()) {
            return row + "-" + id;
        }
        return id;
    }

    private static boolean hasValidXYZ(Double x, Double y, Double z) {
        return x != null && y != null && z != null
                && isFinite(x) && isFinite(y) && isFinite(z);
    }

    private static boolean hasValidENZ(double[] v) {
        return v != null && v.length >= 3
                && isFinite(v[0]) && isFinite(v[1]) && isFinite(v[2]);
    }

    private static boolean samePoint(Double x1, Double y1, Double z1,
                                     Double x2, Double y2, Double z2,
                                     double tol) {

        if (!hasValidXYZ(x1, y1, z1) || !hasValidXYZ(x2, y2, z2)) {
            return false;
        }

        return Math.abs(x1 - x2) <= tol &&
                Math.abs(y1 - y2) <= tol &&
                Math.abs(z1 - z2) <= tol;
    }

    /**
     * Converte metri -> unità utente (metri/ft survey/ft international) come Utils.showCoords, ma torna Double numerico.
     */
    private static Double toUserUnitsMeters(Double meters) {
        if (meters == null || meters.isNaN() || meters.isInfinite()) return null;

        int index = MyData.get_Int("Unit_Of_Measure");

        // stesso criterio di Utils.showCoords()
        if (index == 2 || index == 3 || index == 4 || index == 5) {
            // US survey ft
            return meters / 0.3048006096;
        } else if (index == 6 || index == 7) {
            // international ft
            return meters / 0.3048;
        } else {
            // metric (m)
            return meters;
        }
    }

    /**
     * Overload per primitive (se ti è comodo).
     */
    private static Double toUserUnitsMeters(double meters) {
        return toUserUnitsMeters(Double.valueOf(meters));
    }

    /**
     * mm/s -> ft/s (international), SEMPRE, indipendente dalla UOM scelta nel software
     */
    private static Double mmPerSecToFtPerSecAlways(Double mmps) {
        if (mmps == null) return null;
        if (mmps.isNaN() || mmps.isInfinite()) return null;
        return mmps / 304.8; // ft INTERNATIONAL sempre
    }

    /**
     * Calcola penetration rate mm/s con la regola: se foro "punto" -> verticale down only, altrimenti lungo asse
     */
    private static Double computePenRateMmS(
            long effectiveDurationMs,
            double[] startForoENZ,
            double[] fineForoENZ,
            iredes.Point3D_Drill p
    ) {
        if (p == null) return null;
        if (effectiveDurationMs <= 0L) return null;
        if (!hasValidENZ(startForoENZ) || !hasValidENZ(fineForoENZ)) return null;

        boolean hasHead = hasValidXYZ(p.getHeadX(), p.getHeadY(), p.getHeadZ());
        boolean hasEnd = hasValidXYZ(p.getEndX(), p.getEndY(), p.getEndZ());

        boolean verticalPoint = hasHead && hasEnd && samePoint(
                p.getHeadX(), p.getHeadY(), p.getHeadZ(),
                p.getEndX(), p.getEndY(), p.getEndZ(),
                1e-6
        );

        double progressMm;

        if (!hasHead || !hasEnd || verticalPoint) {
            double downM = startForoENZ[2] - fineForoENZ[2]; // positivo se scendi
            if (downM <= 0) {
                progressMm = 0.0;
            } else {
                progressMm = downM * 1000.0;
            }
        } else {
            double[] headENZ = new double[]{p.getHeadX(), p.getHeadY(), p.getHeadZ()};
            double[] endENZ = new double[]{p.getEndX(), p.getEndY(), p.getEndZ()};

            double ax = endENZ[0] - headENZ[0];
            double ay = endENZ[1] - headENZ[1];
            double az = endENZ[2] - headENZ[2];

            double L = Math.sqrt(ax * ax + ay * ay + az * az);
            if (L < 1e-9) return null;

            double ux = ax / L;
            double uy = ay / L;
            double uz = az / L;

            double dx = fineForoENZ[0] - startForoENZ[0];
            double dy = fineForoENZ[1] - startForoENZ[1];
            double dz = fineForoENZ[2] - startForoENZ[2];

            double progressM = dx * ux + dy * uy + dz * uz;
            if (Double.isNaN(progressM) || Double.isInfinite(progressM)) return null;

            if (progressM <= 0) {
                progressMm = 0.0;
            } else {
                progressMm = progressM * 1000.0;
            }
        }

        double seconds = effectiveDurationMs / 1000.0;
        if (seconds <= 0.0) return null;

        double mmps = progressMm / seconds;
        if (Double.isNaN(mmps) || Double.isInfinite(mmps)) return null;

        return mmps;
    }

    /**
     * Delta orientamento firmato rispetto a una LINEA (AB/BA equivalenti): risultato in [-90..+90]
     */
    private static double signedLineDeltaDeg(double angleDeg, double lineDeg) {
        // normalizza a [0..360)
        double a = ((angleDeg % 360) + 360) % 360;
        double l = ((lineDeg % 360) + 360) % 360;

        // differenza su cerchio 360 in [-180..+180]
        double diff = a - l;
        diff = ((diff + 180) % 360 + 360) % 360 - 180;

        // porta su linea: identità a 180° -> diff in [-90..+90]
        if (diff > 90) diff -= 180;
        if (diff < -90) diff += 180;

        return diff; // firmato
    }
    //TODO

    /**
     * AGGIUNGERE STATO TO-DO ABORTED REFUSED DONE
     * REFUSED apre dialog per commento
     * ABORTED può essere continuato e posso snappare su quello
     * PDF EXPORT SU REPORT XLS
     * ICON STONEX IN FILE REPORT SIA PDF CHE XLS
     * Priorità bassa AUTO REFUSAL
     * password personalizzabile
     * opertor password per funzione di azzeramento
     */
    private void setupAutoRepeat(ImageView button, Runnable action) {
        button.setOnClickListener(v -> action.run());

        button.setOnLongClickListener(v -> {
            isRepeating = true;

            // Primo ritardo di 500ms prima di iniziare la ripetizione
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRepeating) {
                        action.run();
                        handler.postDelayed(this, 50); // ripeti ogni 50ms
                    }
                }
            }, 500);

            return true; // segnala che il long click è gestito
        });

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isRepeating = false; // stop
                    break;
            }
            return false;
        });
    }

    private void sendToEcu() {
        Handler handler = new Handler(Looper.getMainLooper());

        byte[] tollXY_ = PLC_DataTypes_LittleEndian.U16_to_bytes((int) (DataSaved.Drill_tolleranza_XY*0.5 * 1000));
        byte[] tollHam_ = PLC_DataTypes_LittleEndian.U16_to_bytes((int) (DataSaved.Drill_tolleranza_Z * 1000));
        byte[] tollAn_ = PLC_DataTypes_LittleEndian.U16_to_bytes((int) (DataSaved.Drill_tolleranza_Angolo*0.5 * 1000));
        byte[] windowT = PLC_DataTypes_LittleEndian.U16_to_bytes(35);

        handler.post(() ->
                MyDeviceManager.CanWrite(true, 1, 0x73, 8, new byte[]{(byte) 132, tollAn_[0], tollAn_[1], 0, 0, 0, 0, 0})
        );

        handler.postDelayed(() ->
                        MyDeviceManager.CanWrite(true, 1, 0x73, 8, new byte[]{(byte) 130, tollHam_[0], tollHam_[1], 0, 0, 0, 0, 0})
                , 100);

        handler.postDelayed(() ->
                        MyDeviceManager.CanWrite(true, 1, 0x73, 8, new byte[]{(byte) 129, tollXY_[0], tollXY_[1], 0, 0, 0, 0, 0})
                , 200);

        handler.postDelayed(() ->
                        MyDeviceManager.CanWrite(true, 1, 0x73, 8, new byte[]{(byte) 128, windowT[0], windowT[1], 0, 0, 0, 0, 0})
                , 300);
    }


    /// //////


    private int[] getPointStatus(List<Point3D_Drill> points) {
        int todo = 0;
        int aborted = 0;
        int done = 0;

        for (Point3D_Drill p : points) {
            switch (p.getStatus()) {
                case 0:
                    todo++;
                    break;
                case -1:
                    aborted++;
                    break;
                case 1:
                    done++;
                    break;
                default:
                    // eventuale gestione stato sconosciuto
                    break;
            }
        }

        return new int[]{todo, aborted, done};
    }

    private boolean startDrillIfPossible() {
        if ( !isDrilling) {
            play = true;
            stop = false;
            abort = false;
            Drill_Routine(DataSaved.Drilling_Mode, play, stop, abort);
            return true;
        }

        return false;
    }

    private void reopenLastSavedHole() {
        Point3D_Drill p = lastSavedHole;

        if (p == null) {
            String savedId = MyData.get_String("DRILL_LAST_SAVED_HOLE_ID");
            p = findDrillPointByCanonicalId(savedId);
        }

        if (p == null) {
            new CustomToast(this, "No last saved hole").show_alert();
            return;
        }

        int st = p.getStatus() == null ? 0 : p.getStatus();

        if (st == 0) {
            new CustomToast(this, "Last hole is already TO DO").show_alert();
            return;
        }

        final Point3D_Drill holeToReopen = p;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Re-open last hole")
                .setMessage("Last saved hole will be set back to TODO and a RE-OPENED entry will be appended to the project report.\n\nContinue?")
                .setPositiveButton("RE-OPEN", (d, which) -> {
                    reopenHoleToTodo(holeToReopen);

                    lastSavedHole = null;
                    lastSavedHoleId = null;
                    MyData.push("DRILL_LAST_SAVED_HOLE_ID", "");
                })
                .setNegativeButton("CANCEL", null)
                .show();

        FullscreenActivity.setFullScreen(dialog);
    }

    private void rememberLastSavedHole(Point3D_Drill p) {
        if (p == null) return;

        lastSavedHole = p;
        lastSavedHoleId = canonicalHoleId(p);

        // opzionale ma utile se vuoi mantenerlo anche dopo refresh/activity recreate
        MyData.push("DRILL_LAST_SAVED_HOLE_ID", lastSavedHoleId);
    }

    private Point3D_Drill findDrillPointByCanonicalId(String holeId) {
        if (holeId == null || holeId.trim().isEmpty()) return null;
        if (DataSaved.drill_points == null) return null;

        String target = holeId.trim();

        for (Point3D_Drill p : DataSaved.drill_points) {
            if (p == null) continue;

            String id = canonicalHoleId(p);
            if (target.equals(id)) {
                return p;
            }
        }

        return null;
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        drillFileWriterExecutor.shutdown();
    }

    private interface IoTask {
        void run() throws Exception;
    }

    private void runFileWriteAsync(IoTask task) {
        drillFileWriterExecutor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                Log.e("DrillFileWrite", Log.getStackTraceString(e));
                handler.post(() ->
                        new CustomToast(this, "Error writing drill report").show_error()
                );
            }
        });
    }

    private Double getCurrentPenRateMmS() {
        if (!isDrilling) return null;
        if (isDrillPaused) return null;
        if (Selected_Point3D_Drill == null) return null;
        if (StartForo == null) return null;
        if (toolEndCoord == null || toolEndCoord.length < 3) return null;

        long liveMs = getEffectiveDrillMillisLive();

        return computePenRateMmS(
                liveMs,
                StartForo,
                toolEndCoord.clone(),
                Selected_Point3D_Drill
        );
    }

    private Double getCurrentPenRateFtS() {
        Double mmS = getCurrentPenRateMmS();
        return (mmS == null) ? null : mmPerSecToFtPerSecAlways(mmS);
    }
}