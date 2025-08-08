package gui.grading_dozergrader;

import static gui.MyApp.licenseType;
import static gui.MyApp.hAlarm;
import static gui.grade_draw_class.Grade_Top_View_DXF.giroFrecciaDozer;
import static services.ReadProjectService.isFinishedDTM;
import static services.ReadProjectService.isFinishedPOINT;
import static services.ReadProjectService.isFinishedPOLY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
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
import gui.dialogs_and_toast.DialogOffset_3D;
import gui.dialogs_and_toast.Dialog_Edit_Coordinate_Demo;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.Dialog_Gain;
import gui.dialogs_and_toast.Dialog_MapMode;
import gui.dialogs_and_toast.Dialog_Point_Poly;
import gui.dialogs_user_settings.DialogColors;
import gui.dialogs_user_settings.DialogUnitOfMeasure;
import gui.draw_class.MyColorClass;
import gui.gps.NmeaGenerator;
import gui.grade_draw_class.Grade_DrawDXF_Layer2;
import gui.grade_draw_class.Grade_Top_View_DXF;
import gui.projects.Dialog_PRJ_Folder;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import services.CanSender;
import services.CanService;
import services.TriangleService;
import utils.LeicaLB;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class Grading3D_DXF extends BaseClass {
    Dialog_Gain dialogGain;
    ImageView autoL, autoR;
    Dialog_MapMode dialogMapMode;
    Dialog_PRJ_Folder dialogPrjFolder;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    DialogColors dialogColors;
    Dialog_Edit_Coordinate_Demo dialogEditCoordinateDemo;
    LinearLayout centLayout;
    View layer2Canvas, topViewCanvas;

    String distances = "";
    private boolean showCutFill = false;
    private boolean zommaIn3D, zommaOut3D;
    TextView heightLT, heightCT, heightRT, txtwait;
    ImageView menu, bucketEdge, offsetSettings, typeView, lineReference, shortcutSurface, freccia, showZ, bussola, units, hydro_set, pickpp;
    ConstraintLayout panel2, panel3;
    ImageView zoomIn, zoomOut, center, gpsCoord, alertGnss;
    ImageView headingTv, lucchetto, tema;
    TextView distTv, offsetTv, txcutfill;
    Guideline guideV, guideH, bordoSX;
    int indexView = 0;
    DialogOffset_3D dialogOffset;
    ConstraintLayout mainConstr, cutfill;
    int colorUp = Color.BLUE, colorDown = Color.RED;
    static double dist;
    double real_height;
    ImageView allarmeAlt;
    float rotBus = 0f;
    DialogUnitOfMeasure dialogUnitOfMeasure;
    Dialog_Point_Poly dialogPointPoly;
    int indexMachineSelected, indexBucketSelected;

    ConstraintLayout.LayoutParams paramsH;
    ConstraintLayout.LayoutParams paramsV;
    ConstraintLayout.LayoutParams paramsLeft;
    ConstraintLayout.LayoutParams p2;
    ConstraintLayout.LayoutParams p3;
    static String whats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_3d);
        if (DataSaved.dxfFaces == null) {
            new CustomToast(this, "Error Project").show_error();
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        } else {
            try {
                if (whats == null) {
                    Intent intent = getIntent();
                    whats = intent.getStringExtra("whats");
                    if (DataSaved.my_comPort == 4 && whats != null) {
                        DataSaved.demoNORD = DataSaved.dxfFaces.get(0).getP1().getY();
                        NmeaGenerator.LATITUDE = DataSaved.demoNORD;
                        DataSaved.demoEAST = DataSaved.dxfFaces.get(0).getP1().getX();
                        NmeaGenerator.LONGITUDE = DataSaved.demoEAST;
                        DataSaved.demoZ = DataSaved.dxfFaces.get(0).getP1().getZ() + 2.5;
                        NmeaGenerator.ALTITUDE = DataSaved.demoZ;
                        MyData.push("demoNORD", String.valueOf(DataSaved.demoNORD));
                        MyData.push("demoEAST", String.valueOf(DataSaved.demoEAST));
                        MyData.push("demoZ", String.valueOf(DataSaved.demoZ));
                        //Log.d("initDemo", whats);

                    }
                }
            } catch (Exception e) {
                new CustomToast(this, "NO DTM FOUND").show_alert();
            }

            mainConstr = this.getWindow().findViewById(R.id.mainConstr);
            findView();
            init();
            onClick();
            updateViste();
            updateUI();
            dialogUnitOfMeasure = new DialogUnitOfMeasure(this);
            dialogMapMode = new Dialog_MapMode(this);
            dialogPointPoly = new Dialog_Point_Poly(this);
            dialogPrjFolder = new Dialog_PRJ_Folder(this);
            dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
            dialogColors = new DialogColors(this);
            dialogEditCoordinateDemo = new Dialog_Edit_Coordinate_Demo(this);
            dialogGain = new Dialog_Gain(this);

        }
    }

    private void findView() {
        txtwait = findViewById(R.id.txtwait);
        tema = findViewById(R.id.tema);
        heightLT = findViewById(R.id.heightLT);
        heightCT = findViewById(R.id.heightCT);
        heightRT = findViewById(R.id.heightRT);
        pickpp = findViewById(R.id.pickpp);
        menu = findViewById(R.id.digMenu);
        bucketEdge = findViewById(R.id.bucketEdge);
        offsetSettings = findViewById(R.id.settingOffset);
        typeView = findViewById(R.id.typeView);
        lineReference = findViewById(R.id.lineReference);
        shortcutSurface = findViewById(R.id.shortcutSurface);

        panel2 = findViewById(R.id.panel2D);
        panel3 = findViewById(R.id.panel3D);


        zoomIn = findViewById(R.id.zoomP);
        zoomOut = findViewById(R.id.zoomM);
        center = findViewById(R.id.zoomC);
        gpsCoord = findViewById(R.id.gpsInfo);

        headingTv = findViewById(R.id.navigatorHDT);

        distTv = findViewById(R.id.dist_tv);
        offsetTv = findViewById(R.id.offset_tv);
        centLayout = findViewById(R.id.centLayout);

        guideH = findViewById(R.id.guideH);
        guideV = findViewById(R.id.guideV);
        bordoSX = findViewById(R.id.bordoSX);

        mainConstr.setBackgroundColor(MyColorClass.colorConstraint);
        freccia = findViewById(R.id.arrowOr);
        alertGnss = findViewById(R.id.alertGnss);
        showZ = findViewById(R.id.showz);
        allarmeAlt = findViewById(R.id.allarmeAlt);
        allarmeAlt.setVisibility(View.GONE);
        bussola = findViewById(R.id.bussola);
        units = findViewById(R.id.units);
        bussola.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));
        units.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));
        tema.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));
        cutfill = findViewById(R.id.cutfill3d);
        txcutfill = findViewById(R.id.txtcutfill3d);
        lucchetto = findViewById(R.id.lucchetto);
        hydro_set = findViewById(R.id.hydro_setup);
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexBucketSelected = MyData.get_Int("M"+indexMachineSelected+"BucketSelected");
        layer2Canvas = new Grade_DrawDXF_Layer2(this);
        topViewCanvas = new Grade_Top_View_DXF(this);
        autoL = findViewById(R.id.auto_l);
        autoR = findViewById(R.id.auto_r);
    }

    private void init() {
        if (licenseType == 5 ) {
            hydro_set.setVisibility(View.VISIBLE);
        } else {
            hydro_set.setVisibility(View.GONE);
        }

        cutfill.setVisibility(View.GONE);
        if (Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("SRT7PROS") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("qti") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
            heightLT.setTextSize(36f);
            heightCT.setTextSize(24f);
            heightRT.setTextSize(36f);
        }
        dialogOffset = new DialogOffset_3D(this);
        showZ.setImageResource(R.drawable.btn_mappa);


        panel2.addView(layer2Canvas);
        panel3.addView(topViewCanvas);
        panel2.setBackgroundColor(MyColorClass.colorSfondo);
        panel3.setBackgroundColor(MyColorClass.colorSfondo);

        if (DataSaved.colorMode == 0) {
            colorUp = (Color.RED);
            colorDown = (Color.BLUE);
        } else {
            colorUp = (Color.BLUE);
            colorDown = (Color.RED);
        }
        offsetTv.setTextColor(MyColorClass.colorConstraint);
        distTv.setTextColor(MyColorClass.colorConstraint);
        try {
            String s = MyData.get_String("progettoSelected");
            s = s.replace("/storage/emulated/0/StonexMC_V4", "");
            s = s.substring(0, s.lastIndexOf("/"));
            distTv.setText(s);
        } catch (Exception e) {
            distTv.setText("");
        }

        switch (DataSaved.bucketEdge) {
            case 1:
                bucketEdge.setImageResource(R.drawable.lama_misura_destra);
                break;
            case 0:
                bucketEdge.setImageResource(R.drawable.lama_misura_cnt);
                break;
            case -1:
                bucketEdge.setImageResource(R.drawable.lama_misura_sinistra);
                break;
        }


        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 4 || index == 5) {
            heightLT.setTextSize(45f);
            heightCT.setTextSize(36f);
            heightRT.setTextSize(45f);
            txcutfill.setTextSize(80f);
        } else {
            txcutfill.setTextSize(90f);
        }


        indexView = DataSaved.typeView;

        (new Handler()).postDelayed(this::startSs, 2000);

    }

    public void startSs() {
        startService(new Intent(Grading3D_DXF.this, TriangleService.class));
    }


    private void disableAll() {
        shortcutSurface.setEnabled(false);
        menu.setEnabled(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClick() {
        pickpp.setOnClickListener(view -> {
            DataSaved.pickPP=!DataSaved.pickPP;
        });
        hydro_set.setOnClickListener(view -> {
            if (!dialogGain.dialog.isShowing()) {
                dialogGain.show();
            }
        });
        bussola.setOnClickListener(view -> {
            if (DataSaved.my_comPort == 4) {
                if (!dialogEditCoordinateDemo.dialog.isShowing()) {
                    dialogEditCoordinateDemo.show();
                }
            }
        });
        tema.setOnClickListener(view -> {
            if (!dialogColors.dialog.isShowing()) {
                dialogColors.show();
            }
        });

        lucchetto.setOnClickListener(view -> {
            DataSaved.lockUnlock += 1;
            DataSaved.lockUnlock = DataSaved.lockUnlock % 2;

        });
        heightLT.setOnClickListener(view -> {
            indexView = 0;
            showCutFill = !showCutFill;
            if (showCutFill) {
                cutfill.setVisibility(View.VISIBLE);
            } else {
                cutfill.setVisibility(View.GONE);
            }
            updateViste();


        });
        units.setOnClickListener(view -> {
            if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                dialogUnitOfMeasure.show();
            }
        });

        showZ.setOnClickListener(view -> {
            if (!dialogMapMode.dialog.isShowing()) {
                dialogMapMode.show();
            }
        });
        gpsCoord.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }

        });
        ////////


        zoomIn.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                zommaIn3D = true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                zommaIn3D = false;
            }
            return true;
        });
        zoomOut.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                zommaOut3D = true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                zommaOut3D = false;
            }
            return true;
        });

        //////////


        center.setOnClickListener(view -> {
            Grade_Top_View_DXF.offsetX = 0;
            Grade_Top_View_DXF.offsetY = 150;
            Grade_DrawDXF_Layer2.offsetX = 0;
            Grade_DrawDXF_Layer2.offsetY = 0;
            MyData.push("scaleFactor3D", String.valueOf(DataSaved.scale_Factor3D));
            MyData.push("scaleFactor_vista1D", String.valueOf(DataSaved.scale_FactorVista1D));
            MyData.push("scaleFactor_vista2D", String.valueOf(DataSaved.scale_FactorVista2D));


        });

        typeView.setOnClickListener((View v) -> {
            indexView++;
            if (showCutFill) {
                indexView = indexView % 2;
            } else {
                indexView = indexView % 3;
            }

            updateViste();


        });

        menu.setOnClickListener((View v) -> {
            MyData.push("scaleFactor3D", String.valueOf(DataSaved.scale_Factor3D));
            MyData.push("scaleFactor_vista1D", String.valueOf(DataSaved.scale_FactorVista1D));
            MyData.push("scaleFactor_vista2D", String.valueOf(DataSaved.scale_FactorVista2D));
            disableAll();
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
            whats = null;
        });

        offsetSettings.setOnClickListener((View v) -> {
            if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                if (!dialogOffset.dialog.isShowing())
                    dialogOffset.show();
            }
        });
        bucketEdge.setOnLongClickListener(view -> {
            DataSaved.isLowerEdge = !DataSaved.isLowerEdge;
            return false;
        });

        bucketEdge.setOnClickListener(view -> {
            if (!DataSaved.isLowerEdge) {
                if (++DataSaved.bucketEdge > 1)
                    DataSaved.bucketEdge = -1;
                switch (DataSaved.bucketEdge) {
                    case 1:
                        bucketEdge.setImageResource(R.drawable.lama_misura_destra);
                        break;
                    case 0:
                        bucketEdge.setImageResource(R.drawable.lama_misura_cnt);
                        break;
                    case -1:
                        bucketEdge.setImageResource(R.drawable.lama_misura_sinistra);
                        break;
                }
            }
        });

        shortcutSurface.setOnClickListener((View v) -> {
            if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                try {
                    String s = MyData.get_String("progettoSelected");
                    s = s.substring(0, s.lastIndexOf("/"));

                    boolean isDir = s.equals("/storage/emulated/0/StonexMC_V4/Projects");
                    if (!isDir) {
                        if (!dialogPrjFolder.dialog.isShowing()) {
                            dialogPrjFolder.show(s);
                        }
                    } else {
                        disableAll();
                        startActivity(new Intent(this, Activity_Home_Page.class));
                        overridePendingTransition(0, 0);
                        finish();
                    }
                } catch (Exception e) {
                    disableAll();
                    startActivity(new Intent(this, Activity_Home_Page.class));
                    overridePendingTransition(0, 0);
                    finish();
                }
            }

        });

        lineReference.setOnClickListener((View v) -> {
            if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                if (!dialogPointPoly.dialog.isShowing()) {
                    dialogPointPoly.show();
                }

            }
        });
    }

    private void updateViste() {
        paramsH = (ConstraintLayout.LayoutParams) guideH.getLayoutParams();
        paramsV = (ConstraintLayout.LayoutParams) guideV.getLayoutParams();
        paramsLeft = (ConstraintLayout.LayoutParams) bordoSX.getLayoutParams();
        p2 = (ConstraintLayout.LayoutParams) panel2.getLayoutParams();
        p3 = (ConstraintLayout.LayoutParams) panel3.getLayoutParams();
        if (showCutFill) {
            switch (indexView) {

                case 0:
                    paramsLeft.guidePercent = 0f;
                    paramsV.guidePercent = 0.5f;
                    paramsH.guidePercent = 0.52f;
                    p3.leftMargin = 0;
                    break;
                case 1:
                    paramsLeft.guidePercent = 0.50f;
                    paramsV.guidePercent = 0.95f;
                    paramsH.guidePercent = 0.52f;
                    p3.leftMargin = 0;
                    break;


            }
        } else {
            switch (indexView) {
                case 1:
                    paramsLeft.guidePercent = 0f;
                    paramsV.guidePercent = 0f;
                    paramsH.guidePercent = 0.52f;
                    p3.leftMargin = 0;
                    break;
                case 2:
                    paramsLeft.guidePercent = 0f;
                    paramsV.guidePercent = 0.95f;
                    paramsH.guidePercent = 0.9f;
                    break;
                case 3:
                    paramsLeft.guidePercent = 0f;
                    paramsV.guidePercent = 0.95f;
                    paramsH.guidePercent = 0.13f;
                    p2.rightMargin = 0;
                    p2.topMargin = 0;
                    break;
                case 0:
                    paramsLeft.guidePercent = 0f;
                    paramsV.guidePercent = 0.50f;
                    paramsH.guidePercent = 0.52f;

                    int pixel = (int) (0.5f * this.getResources().getDisplayMetrics().density + 0.5f);

                    p3.leftMargin = pixel;
                    p2.rightMargin = pixel;
                    p2.topMargin = pixel;
                    break;
            }
        }

        guideH.setLayoutParams(paramsH);
        guideV.setLayoutParams(paramsV);
        bordoSX.setLayoutParams(paramsLeft);
        panel2.setLayoutParams(p2);
        panel3.setLayoutParams(p3);
        DataSaved.typeView = indexView;
        MyData.push("typeView", String.valueOf(indexView));
    }

    public void updateUI() {
        if(DataSaved.pickPP){
            pickpp.setImageTintList(getColorStateList(R.color.bg_sfsgreen));
        }else {
            pickpp.setImageTintList(getColorStateList(R.color.gray));
        }
        if (txtwait.getVisibility() == View.VISIBLE) {
            bucketEdge.setVisibility(View.INVISIBLE);
            offsetSettings.setVisibility(View.INVISIBLE);
            typeView.setVisibility(View.INVISIBLE);
            lineReference.setVisibility(View.INVISIBLE);
            showZ.setVisibility(View.INVISIBLE);
            hydro_set.setVisibility(View.INVISIBLE);

        } else {
            bucketEdge.setVisibility(View.VISIBLE);
            offsetSettings.setVisibility(View.VISIBLE);
            typeView.setVisibility(View.VISIBLE);
            lineReference.setVisibility(View.VISIBLE);
            showZ.setVisibility(View.VISIBLE);
            hydro_set.setVisibility(View.VISIBLE);
        }
        if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
            txtwait.setVisibility(View.GONE);
            try {
                if (!dialogGnssCoordinates.alertDialog.isShowing() &&
                        !dialogMapMode.dialog.isShowing() &&
                        !dialogOffset.dialog.isShowing() &&
                        !dialogPointPoly.dialog.isShowing() &&
                        !dialogPrjFolder.dialog.isShowing() &&
                        !dialogUnitOfMeasure.alertDialog.isShowing()) {
                    if (DataSaved.isLowerEdge) {
                        bucketEdge.setBackgroundColor(getColor(R.color.yellow));
                        switch (DataSaved.bucketEdge) {
                            case 1:
                                bucketEdge.setImageResource(R.drawable.lama_misura_destra);
                                break;
                            case 0:
                                bucketEdge.setImageResource(R.drawable.lama_misura_cnt);
                                break;
                            case -1:
                                bucketEdge.setImageResource(R.drawable.lama_misura_sinistra);
                                break;
                        }
                    } else {
                        bucketEdge.setBackgroundColor(getColor(R.color.nav_gray_color));
                        switch (DataSaved.bucketEdge) {
                            case 1:
                                bucketEdge.setImageResource(R.drawable.lama_misura_destra);
                                break;
                            case 0:
                                bucketEdge.setImageResource(R.drawable.lama_misura_cnt);
                                break;
                            case -1:
                                bucketEdge.setImageResource(R.drawable.lama_misura_sinistra);
                                break;
                        }
                    }

                    if (DataSaved.lockUnlock == 1) {
                        lucchetto.setImageResource((R.drawable.lock));
                    } else {
                        lucchetto.setImageResource((R.drawable.unlock));
                    }


                    rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
                    rotBus = rotBus % 360;

                    bussola.setRotation(rotBus);
                    if (hAlarm) {
                        allarmeAlt.setVisibility(View.VISIBLE);
                    } else {
                        allarmeAlt.setVisibility(View.GONE);
                    }

                    if (DataSaved.gpsOk) {
                        alertGnss.setVisibility(View.GONE);
                    } else {
                        alertGnss.setVisibility(View.VISIBLE);
                    }
                    //////////////////

                    if (zommaOut3D) {
                        zommaIn3D = false;
                        if (DataSaved.scale_Factor3D > 0.05f) {
                            DataSaved.scale_Factor3D -= 0.025f;
                        }

                        if (DataSaved.scale_FactorVista2D > 0.05f) {
                            DataSaved.scale_FactorVista2D -= 0.025;
                        }

                    }
                    if (zommaIn3D) {
                        zommaOut3D = false;
                        if (DataSaved.scale_Factor3D < 4.5f) {
                            DataSaved.scale_Factor3D += 0.025f;


                        }

                        if (DataSaved.scale_FactorVista2D < 4.5f) {
                            DataSaved.scale_FactorVista2D += 0.025;
                        }

                    }


                    /////////////////

                    if (DataSaved.isAutoSnap == 0) {
                        lucchetto.setVisibility(View.GONE);
                        freccia.setVisibility(View.GONE);
                        if (TriangleService.ctOffGrid) {
                            heightCT.setTextColor(getColor(R.color.white));
                            heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                            centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setText(R.string.off_grid);
                        } else {
                            if (!showCutFill) {
                                if (TriangleService.quota3D_CT > DataSaved.deadbandH) {
                                    heightCT.setTextColor(getColor(R.color.white));
                                    heightCT.setBackgroundColor(colorUp);
                                    centLayout.setBackgroundColor(colorUp);
                                    heightCT.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                } else if (TriangleService.quota3D_CT < -DataSaved.deadbandH) {
                                    heightCT.setTextColor(getColor(R.color.white));
                                    heightCT.setBackgroundColor(colorDown);
                                    centLayout.setBackgroundColor(colorDown);
                                    heightCT.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                } else if (TriangleService.quota3D_CT >= -DataSaved.deadbandH && TriangleService.quota3D_CT <= DataSaved.deadbandH) {
                                    heightCT.setTextColor(getColor(R.color._____cancel_text));
                                    heightCT.setBackgroundColor(getColor(R.color.green));
                                    centLayout.setBackgroundColor(colorUp);
                                    heightCT.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                }
                            } else {
                                if (TriangleService.quota3D_CT > DataSaved.deadbandH) {
                                    heightCT.setTextColor(getColor(R.color.white));
                                    heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                                    centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                                    heightCT.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                } else if (TriangleService.quota3D_CT < -DataSaved.deadbandH) {
                                    heightCT.setTextColor(getColor(R.color.white));
                                    centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                                    heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                                    heightCT.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                } else if (TriangleService.quota3D_CT >= -DataSaved.deadbandH && TriangleService.quota3D_CT <= DataSaved.deadbandH) {
                                    heightCT.setTextColor(getColor(R.color.white));
                                    centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                                    heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                                    heightCT.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                }
                            }
                        }
                    } else {

                        setFreccia();
                    }

                    ////////////////

                    if (TriangleService.ltOffGrid) {
                        heightLT.setTextColor(getColor(R.color.white));
                        heightLT.setBackgroundColor(getColor(R.color._____cancel_text));
                        heightLT.setText(R.string.off_grid);
                    } else {

                        if (TriangleService.quota3D_SX > DataSaved.deadbandH) {
                            heightLT.setTextColor(getColor(R.color.white));
                            heightLT.setBackgroundColor(colorUp);
                            heightLT.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                        } else if (TriangleService.quota3D_SX < -DataSaved.deadbandH) {
                            heightLT.setTextColor(getColor(R.color.white));
                            heightLT.setBackgroundColor(colorDown);
                            heightLT.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                        } else if (TriangleService.quota3D_SX >= -DataSaved.deadbandH && TriangleService.quota3D_SX <= DataSaved.deadbandH) {
                            heightLT.setTextColor(getColor(R.color._____cancel_text));
                            heightLT.setBackgroundColor(getColor(R.color.green));
                            heightLT.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                        }
                    }


                    if (TriangleService.rtOffGrid) {
                        heightRT.setTextColor(getColor(R.color.white));
                        heightRT.setBackgroundColor(getColor(R.color._____cancel_text));
                        heightRT.setText(R.string.off_grid);
                    } else {
                        if (TriangleService.quota3D_DX > DataSaved.deadbandH) {
                            heightRT.setTextColor(getColor(R.color.white));
                            heightRT.setBackgroundColor(colorUp);
                            heightRT.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                        } else if (TriangleService.quota3D_DX < -DataSaved.deadbandH) {
                            heightRT.setTextColor(getColor(R.color.white));
                            heightRT.setBackgroundColor(colorDown);
                            heightRT.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                        } else if (TriangleService.quota3D_DX >= -DataSaved.deadbandH && TriangleService.quota3D_DX <= DataSaved.deadbandH) {
                            heightRT.setTextColor(getColor(R.color._____cancel_text));
                            heightRT.setBackgroundColor(getColor(R.color.green));
                            heightRT.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                        }


                    }
                    if (showCutFill) {
                        heightLT.setBackgroundColor(getColor(R.color._____cancel_text));
                        heightLT.setTextColor(getColor(R.color.white));
                        heightRT.setBackgroundColor(getColor(R.color._____cancel_text));
                        heightRT.setTextColor(getColor(R.color.white));
                        switch (DataSaved.bucketEdge) {
                            case -1:
                                if (!TriangleService.ltOffGrid) {
                                    if (TriangleService.quota3D_SX > DataSaved.deadbandH) {
                                        txcutfill.setTextColor(getColor(R.color.white));
                                        cutfill.setBackgroundColor(colorUp);
                                        txcutfill.setText("▼\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                                        CanSender.onGrade = (byte) 125;
                                    } else if (TriangleService.quota3D_SX < -DataSaved.deadbandH) {
                                        txcutfill.setTextColor(getColor(R.color.white));
                                        cutfill.setBackgroundColor(colorDown);
                                        txcutfill.setText("▲\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                                        CanSender.onGrade = (byte) 131;
                                    } else if (TriangleService.quota3D_SX >= -DataSaved.deadbandH && TriangleService.quota3D_SX <= DataSaved.deadbandH) {
                                        txcutfill.setTextColor(getColor(R.color._____cancel_text));
                                        cutfill.setBackgroundColor(getColor(R.color.green));
                                        txcutfill.setText("⧗\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                                        CanSender.onGrade = (byte) 128;
                                    }
                                } else {
                                    txcutfill.setText(R.string.off_grid);
                                }
                                break;
                            case 0:
                                if (!TriangleService.ctOffGrid) {
                                    if (TriangleService.quota3D_CT > DataSaved.deadbandH) {
                                        txcutfill.setTextColor(getColor(R.color.white));
                                        cutfill.setBackgroundColor(colorUp);
                                        txcutfill.setText("▼\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                        CanSender.onGrade = (byte) 125;
                                    } else if (TriangleService.quota3D_CT < -DataSaved.deadbandH) {
                                        txcutfill.setTextColor(getColor(R.color.white));
                                        cutfill.setBackgroundColor(colorDown);
                                        txcutfill.setText("▲\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                        CanSender.onGrade = (byte) 131;
                                    } else if (TriangleService.quota3D_CT >= -DataSaved.deadbandH && TriangleService.quota3D_CT <= DataSaved.deadbandH) {
                                        txcutfill.setTextColor(getColor(R.color._____cancel_text));
                                        cutfill.setBackgroundColor(getColor(R.color.green));
                                        txcutfill.setText("⧗\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                        CanSender.onGrade = (byte) 128;
                                    }
                                } else {
                                    txcutfill.setText(R.string.off_grid);
                                }
                                break;
                            case 1:
                                if (!TriangleService.rtOffGrid) {
                                    if (TriangleService.quota3D_DX > DataSaved.deadbandH) {
                                        txcutfill.setTextColor(getColor(R.color.white));
                                        cutfill.setBackgroundColor(colorUp);
                                        txcutfill.setText("▼\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                                        CanSender.onGrade = (byte) 125;
                                    } else if (TriangleService.quota3D_DX < -DataSaved.deadbandH) {
                                        txcutfill.setTextColor(getColor(R.color.white));
                                        cutfill.setBackgroundColor(colorDown);
                                        txcutfill.setText("▲\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                                        CanSender.onGrade = (byte) 131;
                                    } else if (TriangleService.quota3D_DX >= -DataSaved.deadbandH && TriangleService.quota3D_DX <= DataSaved.deadbandH) {
                                        txcutfill.setTextColor(getColor(R.color._____cancel_text));
                                        cutfill.setBackgroundColor(getColor(R.color.green));
                                        txcutfill.setText("⧗\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                                        CanSender.onGrade = (byte) 128;
                                    }
                                } else {
                                    txcutfill.setText(R.string.off_grid);
                                }
                                break;

                        }
                        topViewCanvas.invalidate();
                        layer2Canvas.invalidate();
                    } else {
                        switch (indexView) {
                            case 0:
                                topViewCanvas.invalidate();
                                layer2Canvas.invalidate();
                                break;
                            case 1:
                                topViewCanvas.invalidate();
                                break;
                            case 2:
                                layer2Canvas.invalidate();
                                break;
                            case 3:
                                layer2Canvas.invalidate();
                                break;
                        }
                    }


                    String qs = "";
                    switch (DataSaved.bucketEdge) {
                        case -1:
                            qs = "Blade L: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[2]));
                            break;
                        case 0:
                            qs = "Blade C: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[2]));
                            break;
                        case 1:
                            qs = "Blade R: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[2]));
                            break;
                    }
                    offsetTv.setText(qs + "\nOFFSET: " + Utils.readUnitOfMeasureLITE(String.valueOf(-DataSaved.offsetH)));
                    if (MyApp.canError) {
                        heightLT.setText("Can Err");
                        heightCT.setText("Can Err");
                        heightRT.setText("Can Err");
                    }
                    setLightBar();

                }
            } catch (Exception e) {
                Log.e("Error_DXF_Activity", e.toString());
                e.printStackTrace();
                heightLT.setText("Error");
                heightRT.setText("Error");
            }

        } else {
            txtwait.setVisibility(View.VISIBLE);
        }
        if (MyApp.licenseType == 5) {
            if (CanService.isAutoL) {
                autoL.setVisibility(View.VISIBLE);
                autoL.setImageResource(R.drawable.auto_test_rosso);
                autoL.setAlpha(1f);
            } else {
                autoL.setVisibility(View.VISIBLE);
                autoL.setImageResource(R.drawable.auto_test_ko);
                autoL.setAlpha(0.5f);
            }
            if (CanService.isAutoR) {
                autoR.setVisibility(View.VISIBLE);
                autoR.setImageResource(R.drawable.auto_test_rosso);
                autoR.setAlpha(1f);
            } else {
                autoR.setVisibility(View.VISIBLE);
                autoR.setImageResource(R.drawable.auto_test_ko);
                autoR.setAlpha(0.5f);
            }
        } else {
            autoL.setVisibility(View.GONE);
            autoR.setVisibility(View.GONE);
        }

    }

    public void setFreccia() {
        double rot;
        double rotFix = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        try {

            lucchetto.setVisibility(View.VISIBLE);

            if (DataSaved.isAutoSnap == 1 || DataSaved.isAutoSnap == 3) {
                freccia.setVisibility(View.VISIBLE);
                switch (DataSaved.bucketEdge) {
                    case -1:
                        dist = TriangleService.dist3D_SX;
                        if (Math.abs(dist) <= DataSaved.deadbandH) {
                            centLayout.setBackgroundColor(getColor(R.color.green));
                            heightCT.setBackgroundColor(getColor(R.color.green));
                            lucchetto.setBackgroundColor(getColor(R.color.green));
                            heightCT.setTextColor(getColor(R.color._____cancel_text));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                            freccia.setImageResource(R.drawable.baseline_radio_button_checked_96);
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));

                        } else {
                            centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                            lucchetto.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setTextColor(getColor(R.color.white));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            freccia.setImageResource(R.drawable.navigator_white);
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                        }
                        distances = Utils.readUnitOfMeasureLITE(String.valueOf(dist));
                        heightCT.setText(distances);
                        real_height = TriangleService.quota3D_SX;
                        rot = My_LocationCalc.calcBearingXY(ExcavatorLib.bucketLeftCoord[0], ExcavatorLib.bucketLeftCoord[1], DataSaved.nearestPoint.getX(), DataSaved.nearestPoint.getY());
                        rot = rot + rotFix;
                        rot = rot % 360;
                        freccia.setRotation((float) rot);
                        break;
                    case 0:
                        dist = TriangleService.dist3D_CT;
                        if (Math.abs(dist) <= DataSaved.deadbandH) {
                            centLayout.setBackgroundColor(getColor(R.color.green));
                            heightCT.setBackgroundColor(getColor(R.color.green));
                            lucchetto.setBackgroundColor(getColor(R.color.green));
                            heightCT.setTextColor(getColor(R.color._____cancel_text));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                            freccia.setImageResource(R.drawable.baseline_radio_button_checked_96);
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));

                        } else {
                            centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                            lucchetto.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setTextColor(getColor(R.color.white));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            freccia.setImageResource(R.drawable.navigator_white);
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                        }
                        distances = Utils.readUnitOfMeasureLITE(String.valueOf(dist));
                        heightCT.setText(distances);
                        real_height = TriangleService.quota3D_CT;
                        rot = My_LocationCalc.calcBearingXY(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], DataSaved.nearestPoint.getX(), DataSaved.nearestPoint.getY());
                        rot = rot + rotFix;
                        rot = rot % 360;
                        freccia.setRotation((float) rot);
                        break;

                    case 1:
                        dist = TriangleService.dist3D_DX;
                        if (Math.abs(dist) <= DataSaved.deadbandH) {
                            centLayout.setBackgroundColor(getColor(R.color.green));
                            heightCT.setBackgroundColor(getColor(R.color.green));
                            lucchetto.setBackgroundColor(getColor(R.color.green));
                            heightCT.setTextColor(getColor(R.color._____cancel_text));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                            freccia.setImageResource(R.drawable.baseline_radio_button_checked_96);
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));

                        } else {
                            centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                            lucchetto.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setTextColor(getColor(R.color.white));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            freccia.setImageResource(R.drawable.navigator_white);
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                        }
                        distances = Utils.readUnitOfMeasureLITE(String.valueOf(dist));
                        heightCT.setText(distances);
                        real_height = TriangleService.quota3D_DX;
                        rot = My_LocationCalc.calcBearingXY(ExcavatorLib.bucketRightCoord[0], ExcavatorLib.bucketRightCoord[1], DataSaved.nearestPoint.getX(), DataSaved.nearestPoint.getY());
                        rot = rot + rotFix;
                        rot = rot % 360;
                        freccia.setRotation((float) rot);
                        break;
                }
            } else if (DataSaved.isAutoSnap == 2 || DataSaved.isAutoSnap == 4) {
                freccia.setVisibility(View.VISIBLE);
                switch (DataSaved.bucketEdge) {
                    case -1:
                        dist = TriangleService.dist3D_SX;
                        if (Math.abs(dist) <= DataSaved.deadbandH) {
                            centLayout.setBackgroundColor(getColor(R.color.green));
                            heightCT.setBackgroundColor(getColor(R.color.green));
                            lucchetto.setBackgroundColor(getColor(R.color.green));
                            heightCT.setTextColor(getColor(R.color._____cancel_text));
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                            freccia.setImageResource(R.drawable.baseline_radio_button_checked_96);

                        } else {
                            centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                            lucchetto.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setTextColor(getColor(R.color.white));
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            freccia.setImageResource(R.drawable.navigator_white);
                        }
                        distances = Utils.readUnitOfMeasureLITE(String.valueOf(dist));
                        heightCT.setText(distances + "\n" + "(" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)) + ")");
                        real_height = TriangleService.quota3D_SX;
                        freccia.setRotation((float) giroFrecciaDozer);

                        break;
                    case 0:
                        dist = TriangleService.dist3D_CT;
                        if (Math.abs(dist) <= DataSaved.deadbandH) {
                            centLayout.setBackgroundColor(getColor(R.color.green));
                            heightCT.setBackgroundColor(getColor(R.color.green));
                            lucchetto.setBackgroundColor(getColor(R.color.green));
                            heightCT.setTextColor(getColor(R.color._____cancel_text));
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                            freccia.setImageResource(R.drawable.baseline_radio_button_checked_96);

                        } else {
                            centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                            lucchetto.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setTextColor(getColor(R.color.white));
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            freccia.setImageResource(R.drawable.navigator_white);
                        }
                        distances = Utils.readUnitOfMeasureLITE(String.valueOf(dist));
                        heightCT.setText(distances + "\n" + "(" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)) + ")");
                        real_height = TriangleService.quota3D_CT;
                        freccia.setRotation((float) giroFrecciaDozer);

                        break;

                    case 1:
                        dist = TriangleService.dist3D_DX;
                        if (Math.abs(dist) <= DataSaved.deadbandH) {
                            centLayout.setBackgroundColor(getColor(R.color.green));
                            heightCT.setBackgroundColor(getColor(R.color.green));
                            lucchetto.setBackgroundColor(getColor(R.color.green));
                            heightCT.setTextColor(getColor(R.color._____cancel_text));
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                            freccia.setImageResource(R.drawable.baseline_radio_button_checked_96);

                        } else {
                            centLayout.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setBackgroundColor(getColor(R.color._____cancel_text));
                            lucchetto.setBackgroundColor(getColor(R.color._____cancel_text));
                            heightCT.setTextColor(getColor(R.color.white));
                            lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                            freccia.setImageResource(R.drawable.navigator_white);
                        }
                        distances = Utils.readUnitOfMeasureLITE(String.valueOf(dist));
                        heightCT.setText(distances + "\n" + "(" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)) + ")");
                        real_height = TriangleService.quota3D_DX;
                        freccia.setRotation((float) giroFrecciaDozer);

                        break;


                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            heightLT.setText("Error");
            heightCT.setText("Error");
            heightLT.setText("Error");
        }
    }


    private void setLightBar() {

        MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(TriangleService.ltOffGrid, TriangleService.quota3D_SX, DataSaved.deadbandH));

        MyDeviceManager.CanWrite(0, 0xA8, 3, LeicaLB.mapping(TriangleService.rtOffGrid, TriangleService.quota3D_DX, DataSaved.deadbandH));

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

    protected void onDestroy() {
        super.onDestroy();


        stopService(new Intent(Grading3D_DXF.this, TriangleService.class));

    }

}