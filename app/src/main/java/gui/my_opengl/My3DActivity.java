package gui.my_opengl;


import static gui.MyApp.errorCode;
import static gui.MyApp.hAlarm;
import static gui.MyApp.isOffgrid;
import static packexcalib.exca.DataSaved.OUTPUT_HYDRO;
import static services.CanSender.prepLeft;
import static services.CanSender.prepRight;
import static services.CanService.Dozer_Auto_Main;
import static services.CanService.Grader_AutoRight;
import static services.CanService.Grader_Auto_Left;
import static services.CanService.Grader_Auto_SS;
import static services.ReadProjectService.isFinishedDTM;
import static services.ReadProjectService.isFinishedPOINT;
import static services.ReadProjectService.isFinishedPOLY;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.JOYSTICKS;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.example.stx_dig.R;


import DPAD.DPadHelper;
import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.buckets.BucketChooserActivity;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogOffset_3D;
import gui.dialogs_and_toast.Dialog_Add_Pnezd;
import gui.dialogs_and_toast.Dialog_Blade_Wear;
import gui.dialogs_and_toast.Dialog_CutFill_3D;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.Dialog_MapMode;
import gui.dialogs_and_toast.Dialog_Point_Poly;
import gui.dialogs_user_settings.DialogAudioSystem;
import gui.dialogs_user_settings.DialogColors;
import gui.draw_class.DrawDXF_Layer1;
import gui.draw_class.DrawDXF_Layer1_Tilt;
import gui.draw_class.DrawDXF_Layer2;
import gui.draw_class.DrawDXF_Layer2_Tilt;
import gui.draw_class.MyColorClass;
import gui.draw_class.Top_View_DXF;
import gui.grade_draw_class.Grade_DrawDXF_Layer1;
import gui.grade_draw_class.Grade_DrawDXF_Layer2;
import gui.hydro.Dialog_Gain_Hydro;
import gui.projects.Dialog_PRJ_Folder;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;

import services.CanSender;
import services.TriangleService;
import utils.LeicaLB;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;


public class My3DActivity extends BaseClass {

    int flip = 0;

    ImageView navigatorHDT;

    TextView AUTO_SX, AUTO_SS, AUTO_DX;
    Dialog_Blade_Wear dialogBladeWear;
    public static boolean PNEZD_FUNCTION;
    ImageView allarmeAlt, allarmeBound, gl_hydroP;
    String bucketName;
    int indexMachineSelected, indexBucketSelected;
    TextView generalnfo, generalCoord;
    int initialFunction = 0;
    int indexAudioSystem;
    static float vol;
    TextView titolo,marcia;
    SeekBar seekRed;
    SeekBar seekGreen;
    SeekBar seekBlue;
    ConstraintLayout panel1, panel2, panel3;
    View layer1Canvas, layer2Canvas, layer3Canvas;
    View colorPreview;
    ImageView loading;
    ProgressBar progress;
    double dist;
    String distances;
    static boolean isCutFill;
    float inizioValue;
    static long delay = 10000;
    View view1, view2;
    Guideline divisorio_verticale, divisorio_orizzontale, divisorio_inizio, divisorio_fine;
    ConstraintLayout.LayoutParams paramsV1, paramsV2, inizio, fine;
    private final Handler no_touch_menu = new Handler();
    Dialog_MapMode dialogMapMode;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    Dialog_PRJ_Folder dialogPrjFolder;
    DialogColors dialogColors;
    DialogAudioSystem dialogAudioSystem;
    Dialog_Add_Pnezd dialogAddPnezd;
    Dialog_CutFill_3D dialogCutFill3D;
    public static Dialog_Gain_Hydro diaolgGainHydro;
    private MyGLSurfaceView glSurfaceView;

    TextView boxLeft, boxCent, boxRight, txtCutFill, txtDist;
    LinearLayout sideBar, frameCent;
    ImageView bucketEdge, typeView, offsetSettings, lineReference, freccia, lucchetto, gl_benne;
    ImageView exit, btn_hide, btn_show, btn_color, btn_zoomC, btn_zoomM, btn_zoomP, btn_croce, btn_pnezd, hydroPoint;
    ImageView gl_sound, gl_pnezd, gl_bright, gl_pan_pinch, gl_facce, gl_poly, gl_punti, gl_testi, gl_vista, gl_fill, gl_gradient, gl_folder, gl_gps, gl_filter, gl_layers;
    public static boolean isPan, glFace, glPoint, glText, glPoly, glFilter, glGradient, glFill;
    public static int glVista3d;
    DialogOffset_3D dialogOffset;
    Dialog_Point_Poly dialogPointPoly;
    boolean serviseStrarted;
    String pathToPNEZD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkBooleans();
        serviseStrarted = false;
        Grader_Auto_SS = false;
        setContentView(R.layout.activity_my3_dactivity);
        progress = findViewById(R.id.progress);
        loading = findViewById(R.id.loading);
        progress.setClickable(true);
        loading.setClickable(true);
        if (!serviseStrarted) {
            if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                serviseStrarted = true;
                startService(new Intent(this, TriangleService.class));
            }

        }
        try {

            pathToPNEZD = MyData.get_String("progettoSelected");
            pathToPNEZD = pathToPNEZD.substring(0, pathToPNEZD.lastIndexOf("/"));
            String pathCompleto = pathToPNEZD + "/" + pathToPNEZD.substring(pathToPNEZD.lastIndexOf("/", pathToPNEZD.length() + 1)) + ".csv";
            DataSaved.PNEZDPath = pathCompleto;

        } catch (Exception e) {
            new CustomToast(this, "NO CSV FOUND").show_alert();
        }
        findView();
        onClick();


        //1
        // Ora troviamo il glSurfaceView dentro il layout
        GLSurfaceView oldView = findViewById(R.id.glSurfaceView);

        // Sostituirlo con il nostro MyGLSurfaceView
        glSurfaceView = new MyGLSurfaceView(this);

        // Impostiamo le stesse Constraint dinamicamente
        if (oldView.getLayoutParams() != null) {
            glSurfaceView.setLayoutParams(oldView.getLayoutParams());
        }

        // Sostituisci nel parent view
        ViewGroup parent = (ViewGroup) oldView.getParent();
        int index = parent.indexOfChild(oldView);
        parent.removeView(oldView);
        parent.addView(glSurfaceView, index);


        btn_hide.callOnClick();
        indexMachineSelected = MyData.get_Int("MachineSelected");
        if (DataSaved.isWL == EXCAVATOR) {
            try {
                indexBucketSelected = MyData.get_Int("M" + indexMachineSelected + "BucketSelected");
                bucketName = MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucketSelected + "_Name").toUpperCase();

            } catch (Exception e) {
                bucketName = "";
            }

        } else {
            bucketName = "";
        }

        if (DataSaved.Interface_Type == 2 || DataSaved.Interface_Type == 0) {

            MyDeviceManager.CanWrite(true, 1, 0x18EEFF85, 8,
                    new byte[]{(byte) 0xF4,
                            (byte) 0xF0,
                            (byte) 0x13,
                            (byte) 0x23,
                            (byte) 0x0,
                            (byte) 0x82,
                            (byte) 0x0,
                            (byte) 0xB0});
        }

    }

    private void findView() {
        marcia=findViewById(R.id.marcia);
        navigatorHDT = findViewById(R.id.navigatorHDT);
        navigatorHDT.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));
        panel1 = findViewById(R.id.panel1D);
        panel2 = findViewById(R.id.panel2D);
        panel3 = findViewById(R.id.panel3D);
        generalnfo = findViewById(R.id.generalInfo);
        generalCoord = findViewById(R.id.generaCoord);
        exit = findViewById(R.id.digMenu);
        gl_pan_pinch = findViewById(R.id.gl_pan_pinch);
        gl_facce = findViewById(R.id.gl_facce);
        gl_poly = findViewById(R.id.gl_poly);
        gl_testi = findViewById(R.id.gl_text);
        gl_vista = findViewById(R.id.gl_2d3d);
        gl_punti = findViewById(R.id.gl_point);
        gl_layers = findViewById(R.id.gl_layers);
        gl_gps = findViewById(R.id.Status);
        gl_fill = findViewById(R.id.gl_fill);
        gl_filter = findViewById(R.id.gl_filter);
        gl_folder = findViewById(R.id.gl_folder);
        gl_gradient = findViewById(R.id.gl_gradient);
        gl_pnezd = findViewById(R.id.gl_pnezd);
        btn_hide = findViewById(R.id.btn_hide);
        btn_show = findViewById(R.id.btn_show);
        sideBar = findViewById(R.id.sideLayout);
        btn_color = findViewById(R.id.gl_color);
        btn_zoomC = findViewById(R.id.gl_zoomC);
        btn_zoomP = findViewById(R.id.gl_zoomP);
        btn_zoomM = findViewById(R.id.gl_zoomM);
        btn_croce = findViewById(R.id.gl_croce);
        btn_pnezd = findViewById(R.id.btn_pnezd);
        bucketEdge = findViewById(R.id.bucketEdge);
        boxLeft = findViewById(R.id.txtLeft);
        boxCent = findViewById(R.id.txtCent);
        boxRight = findViewById(R.id.txtRight);
        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);
        divisorio_orizzontale = findViewById(R.id.divisorio_orizzontale);
        divisorio_verticale = findViewById(R.id.divisorio_vericale);
        divisorio_inizio = findViewById(R.id.guide_inizio);
        divisorio_fine = findViewById(R.id.fine);
        typeView = findViewById(R.id.typeView);
        paramsV1 = (ConstraintLayout.LayoutParams) divisorio_verticale.getLayoutParams();
        paramsV2 = (ConstraintLayout.LayoutParams) divisorio_orizzontale.getLayoutParams();
        inizio = (ConstraintLayout.LayoutParams) divisorio_inizio.getLayoutParams();
        fine = (ConstraintLayout.LayoutParams) divisorio_fine.getLayoutParams();
        txtCutFill = findViewById(R.id.txtCutFill);
        offsetSettings = findViewById(R.id.settingOffset);
        lineReference = findViewById(R.id.lineReference);
        frameCent = findViewById(R.id.frameCent);
        freccia = findViewById(R.id.arrowOr);
        lucchetto = findViewById(R.id.lockOr);
        txtDist = findViewById(R.id.txtDist);
        gl_bright = findViewById(R.id.gl_bright);
        gl_benne = findViewById(R.id.gl_benne);
        allarmeAlt = findViewById(R.id.allarmeAlt);
        allarmeBound = findViewById(R.id.allarmeBound);
        gl_sound = findViewById(R.id.gl_sound);
        gl_hydroP = findViewById(R.id.gl_hydroP);
        hydroPoint = findViewById(R.id.hydroPoint);
        allarmeAlt.setVisibility(View.GONE);
        dialogMapMode = new Dialog_MapMode(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogPrjFolder = new Dialog_PRJ_Folder(this);
        dialogColors = new DialogColors(this);
        dialogAudioSystem = new DialogAudioSystem(this);
        dialogOffset = new DialogOffset_3D(this);
        dialogPointPoly = new Dialog_Point_Poly(this);
        dialogAddPnezd = new Dialog_Add_Pnezd(this, pathToPNEZD);
        dialogBladeWear = new Dialog_Blade_Wear(this);
        diaolgGainHydro = new Dialog_Gain_Hydro(this);
        dialogCutFill3D = new Dialog_CutFill_3D(this);

        indexAudioSystem = MyData.get_Int("indexAudioSystem");
        vol = MyData.get_Float("volumeAudioSystem");
        layer3Canvas = new Top_View_DXF(this);
        if (DataSaved.isWL < DOZER) {
            layer1Canvas = (DataSaved.lrTilt != 0) ? new DrawDXF_Layer1_Tilt(this) : new DrawDXF_Layer1(this);
            layer2Canvas = (DataSaved.lrTilt != 0) ? new DrawDXF_Layer2_Tilt(this) : new DrawDXF_Layer2(this);
        } else {
            layer1Canvas = new Grade_DrawDXF_Layer1(this);
            layer2Canvas = new Grade_DrawDXF_Layer2(this);
        }
        panel1.addView(layer1Canvas);
        panel2.addView(layer2Canvas);
        panel3.addView(layer3Canvas);
        panel1.setBackgroundColor(MyColorClass.colorSfondo);
        panel2.setBackgroundColor(MyColorClass.colorSfondo);
        panel3.setBackgroundColor(MyColorClass.colorSfondo);

        if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER) {
            gl_benne.setImageResource((R.drawable.benna_vuota1));
            gl_hydroP.setVisibility(View.GONE);
        } else {

            gl_benne.setImageResource((R.drawable.window_blade));
            gl_hydroP.setVisibility(View.VISIBLE);
        }
        AUTO_SX = findViewById(R.id.AM_SX);
        AUTO_SS = findViewById(R.id.AM_SS);
        AUTO_DX = findViewById(R.id.AM_DX);

        try {
            MyGLRenderer.scale = MyData.get_Float("glScale");
            MyGLRenderer.angleX = MyData.get_Float("glAngleX");
            MyGLRenderer.angleY = MyData.get_Float("glAngleY");
            MyGLRenderer.panX = 0;
            MyGLRenderer.panY = -0.3f;
            MyGLRenderer.angleY_extra = MyData.get_Float("glAngleY_Extra");
        }catch (Exception e){
            MyGLRenderer.scale = 0.5f;
            MyGLRenderer.angleX = -90f;
            MyGLRenderer.angleY = 0f;
            MyGLRenderer.panX = 0f;
            MyGLRenderer.panY = -0.3f;
            MyGLRenderer.angleY_extra = 0.0f;
        }


        if(MyGLRenderer.scale<0.09f){
            MyGLRenderer.scale=0.09f;
        }
    }

    private void onClick() {


        hydroPoint.setOnClickListener(view -> {

            if (DataSaved.isWL == GRADER) {

                DataSaved.HYDRAULIC_CONTROL_POINT_GRADER += 1;
                DataSaved.HYDRAULIC_CONTROL_POINT_GRADER = DataSaved.HYDRAULIC_CONTROL_POINT_GRADER % 3;

            } else {

                DataSaved.HYDRAULIC_CONTROL_POINT_DOZER += 1;
                DataSaved.HYDRAULIC_CONTROL_POINT_DOZER = DataSaved.HYDRAULIC_CONTROL_POINT_DOZER % 2;
            }
            if (DataSaved.isWL == GRADER) {
                switch (DataSaved.HYDRAULIC_CONTROL_POINT_GRADER) {
                    case 0:
                        hydroPoint.setImageResource((R.drawable.cent_right_pg));

                        break;

                    case 1:
                        hydroPoint.setImageResource((R.drawable.cent_left_pg));

                        break;

                    case 2:
                        hydroPoint.setImageResource((R.drawable.left_right_pg));

                        break;
                }
            } else {
                switch (DataSaved.HYDRAULIC_CONTROL_POINT_DOZER) {
                    case 0:
                        hydroPoint.setImageResource((R.drawable.cent_right_pg));

                        break;

                    case 1:
                        hydroPoint.setImageResource((R.drawable.cent_left_pg));

                        break;


                }
            }
            MyData.push("M" + indexMachineSelected + "HYDRAULIC_CONTROL_POINT_GRADER", String.valueOf(DataSaved.HYDRAULIC_CONTROL_POINT_GRADER));
            MyData.push("M" + indexMachineSelected + "HYDRAULIC_CONTROL_POINT_DOZER", String.valueOf(DataSaved.HYDRAULIC_CONTROL_POINT_DOZER));

        });
        navigatorHDT.setOnLongClickListener(view -> {
            locateMachine();
            return false;
        });
        gl_hydroP.setOnClickListener(view -> {
            if (!diaolgGainHydro.dialog.isShowing()) {
                diaolgGainHydro.show();
            }
        });
        AUTO_SX.setOnLongClickListener(view -> {
            prepLeft = !prepLeft;
            return false;
        });
        AUTO_SS.setOnLongClickListener(view -> {
            Grader_Auto_SS = !Grader_Auto_SS;
            return false;
        });
        AUTO_DX.setOnLongClickListener(view -> {
            prepRight = !prepRight;
            return false;
        });

        gl_sound.setOnClickListener(view -> {
            if (!dialogAudioSystem.alertDialog.isShowing()) {
                dialogAudioSystem.show();
            }
        });
        gl_benne.setOnClickListener(view -> {
            if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER) {
                Intent i = new Intent(this, BucketChooserActivity.class);
                i.putExtra("whoDig", String.valueOf(MyApp.visibleActivity));
                startActivity(i);
                finish();
            } else {
                if (!dialogBladeWear.dialog.isShowing()) {
                    dialogBladeWear.show();
                }
            }
        });
        gl_bright.setOnClickListener(view -> {
            if (!dialogColors.dialog.isShowing()) {
                dialogColors.show();
            }
        });
        lucchetto.setOnClickListener(view -> {
            DataSaved.lockUnlock += 1;
            DataSaved.lockUnlock = DataSaved.lockUnlock % 2;
        });
        lineReference.setOnClickListener((View v) -> {

            if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                if (!dialogPointPoly.dialog.isShowing()) {
                    dialogPointPoly.show();
                }

            }
        });
        offsetSettings.setOnClickListener((View v) -> {
            if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                if (!dialogOffset.dialog.isShowing())
                    dialogOffset.show();
            }
        });
        boxLeft.setOnClickListener(view -> {
            isCutFill = !isCutFill;
        });
        txtCutFill.setOnClickListener(view -> {
            isCutFill = !isCutFill;
        });
        typeView.setOnClickListener(view -> {
            DataSaved.typeView++;
            DataSaved.typeView = DataSaved.typeView % 5;
            updateMemories();
        });
        typeView.setOnLongClickListener(view -> {
            if (!dialogCutFill3D.dialog.isShowing()) {
                dialogCutFill3D.show();
            }
            return false;
        });
        btn_croce.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            if (DataSaved.showAlign == 0) {
                DataSaved.showAlign = 1;
            } else if (DataSaved.showAlign == 1) {
                DataSaved.showAlign = 0;
            }
            updateMemories();
        });
        btn_zoomC.setOnClickListener(view -> {
            if(btn_zoomC.getAlpha()==1f) {
                no_touch_menu.removeCallbacks(timeOutTouch);
                no_touch_menu.postDelayed(timeOutTouch, delay);
                MyGLRenderer.panX = 0;
                MyGLRenderer.panY = -0.3f;
            }
        });
        btn_zoomP.setOnClickListener(view -> {
            if(btn_zoomP.getAlpha()==1f) {
                no_touch_menu.removeCallbacks(timeOutTouch);
                no_touch_menu.postDelayed(timeOutTouch, delay);
                MyGLRenderer.scale += 0.05f;
                MyGLRenderer.scale = Math.max(0.09f, Math.min(MyGLRenderer.scale, 1.5f));
            }
        });
        btn_zoomM.setOnClickListener(view -> {
            if(btn_zoomM.getAlpha()==1f) {
                no_touch_menu.removeCallbacks(timeOutTouch);
                no_touch_menu.postDelayed(timeOutTouch, delay);
                MyGLRenderer.scale -= 0.05f;
                MyGLRenderer.scale = Math.max(0.09f, Math.min(MyGLRenderer.scale, 1.5f));
            }
        });

        btn_color.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
           /* if (!dialogColors.dialog.isShowing()) {
                dialogColors.show();
            }*/
            showColorPickerDialog(this);
        });

        btn_show.setOnClickListener(v -> {
            if (sideBar.getVisibility() == View.GONE) {
                no_touch_menu.removeCallbacks(timeOutTouch);
                no_touch_menu.postDelayed(timeOutTouch, delay);
                // Fai apparire con fade-in
                sideBar.setAlpha(0f);
                sideBar.setVisibility(View.VISIBLE);
                sideBar.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start();
                // Fai apparire con fade-in
                btn_hide.setAlpha(0f);
                btn_hide.setVisibility(View.VISIBLE);
                btn_hide.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start();
            }
        });
        btn_hide.setOnClickListener(v -> {

            // Nascondi con fade-out
            sideBar.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> sideBar.setVisibility(View.GONE))
                    .start();

            btn_hide.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> btn_hide.setVisibility(View.GONE))
                    .start();

        });

        gl_layers.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            if (!dialogMapMode.dialog.isShowing()) {
                dialogMapMode.show();
            }
        });
        gl_gps.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });
        gl_pnezd.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            PNEZD_FUNCTION = !PNEZD_FUNCTION;
        });
        btn_pnezd.setOnClickListener(view -> {
            if (!dialogAddPnezd.dialog.isShowing()) {
                dialogAddPnezd.show();
            }
        });
        gl_folder.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                try {
                    String s = MyData.get_String("progettoSelected");
                    s = s.substring(0, s.lastIndexOf("/"));

                    String s2 = s.substring(s.lastIndexOf("/"));
                    boolean isDir = s.equals("/storage/emulated/0/StonexMC_V4/Projects");
                    if (!isDir) {
                        if (!dialogPrjFolder.dialog.isShowing()) {
                            dialogPrjFolder.show(s);
                        }
                    } else {
                        disableAll();
                        startActivity(new Intent(this, Activity_Home_Page.class));
                        finish();
                    }
                } catch (Exception e) {
                    disableAll();
                    startActivity(new Intent(this, Activity_Home_Page.class));
                    finish();
                }
            }
        });

        gl_pan_pinch.setOnClickListener(view -> {
            if(gl_pan_pinch.getAlpha()==1f) {
                no_touch_menu.removeCallbacks(timeOutTouch);
                no_touch_menu.postDelayed(timeOutTouch, delay);
                isPan = !isPan;
            }
        });
        exit.setOnClickListener(view -> {
            try {
                MyData.push("scaleFactor_vista1D", String.valueOf(DataSaved.scale_FactorVista1D));
                MyData.push("scaleFactor_vista2D", String.valueOf(DataSaved.scale_FactorVista2D));

                MyData.push("glScale", String.valueOf(MyGLRenderer.scale));
                MyData.push("glAngleX", String.valueOf(MyGLRenderer.angleX));
                MyData.push("glAngleY", String.valueOf(MyGLRenderer.angleY));
                MyData.push("glAngleY_Extra", String.valueOf(MyGLRenderer.angleY_extra));


            } catch (Exception e) {
            }
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });
        gl_facce.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            glFace = !glFace;
            updateMemories();


        });
        gl_poly.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            glPoly = !glPoly;
            if (glPoly) {
                glGradient = false;
            }
            updateMemories();
        });
        gl_punti.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            glPoint = !glPoint;
            if (glPoint) {
                glGradient = false;

            }
            updateMemories();

        });
        gl_testi.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            glText = !glText;
        });
        gl_fill.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            glFill = !glFill;
            if (glFill) {
                glGradient = false;

            }
            updateMemories();
        });
        gl_filter.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            glFilter = !glFilter;
            updateMemories();
        });
        gl_gradient.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            glGradient = !glGradient;
            if (glGradient) {
                glFill = false;
                glPoint = false;
                glText = false;
                glPoly = false;
            }
            if (!glGradient) {
                glFace = true;
            }

            updateMemories();
        });
        gl_vista.setOnClickListener(view -> {
            no_touch_menu.removeCallbacks(timeOutTouch);
            no_touch_menu.postDelayed(timeOutTouch, delay);
            glVista3d += 1;
            glVista3d = glVista3d % 2;
            if (isPan && glVista3d == 1) {
                isPan = false;
            }
            updateMemories();
        });
        gl_vista.setOnLongClickListener(view -> {
            if (glVista3d != 2) {
                glVista3d = 2;
            } else {
                glVista3d = MyData.get_Int("vista3D");
            }
            return true;
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
                        if (DataSaved.isWL == EXCAVATOR) {
                            bucketEdge.setImageResource(R.drawable.benna_misura_destra);
                        } else {
                            bucketEdge.setImageResource(R.drawable.lama_misura_destra);
                        }
                        break;
                    case 0:
                        if (DataSaved.isWL == EXCAVATOR) {
                            bucketEdge.setImageResource(R.drawable.benna_misura_cnt);
                        } else {
                            bucketEdge.setImageResource(R.drawable.lama_misura_cnt);
                        }
                        break;
                    case -1:
                        if (DataSaved.isWL == EXCAVATOR) {
                            bucketEdge.setImageResource(R.drawable.benna_misura_sinistra);
                        } else {
                            bucketEdge.setImageResource(R.drawable.lama_misura_sinistra);
                        }
                        break;
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceView != null) {
            glSurfaceView.onPause();// Molto importante!
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceView != null) {
            glSurfaceView.onResume(); // Molto importante!
        }

    }

    private final Runnable timeOutTouch = new Runnable() {
        @Override
        public void run() {
            // do stuff
            // Nascondi con fade-out
            sideBar.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> sideBar.setVisibility(View.GONE))
                    .start();

            btn_hide.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> btn_hide.setVisibility(View.GONE))
                    .start();
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Grader_Auto_SS = false;
        stopService(new Intent(this, TriangleService.class));
        MyDeviceManager.OUT1(MyApp.visibleActivity, 0);
        MyDeviceManager.OUT2(MyApp.visibleActivity, 0);


    }

    private void checkBooleans() {
        try {
            glVista3d = Integer.parseInt(MyData.get_String("vista3D"));
        } catch (Exception e) {
            glVista3d = 1;
            MyData.push("vista3D", String.valueOf(1));
        }
        try {
            glFace = Boolean.parseBoolean(MyData.get_String("glFace"));
        } catch (Exception e) {
            glFace = true;
            MyData.push("glFace", String.valueOf(true));
        }

        try {
            glPoint = Boolean.parseBoolean(MyData.get_String("glPoint"));
        } catch (Exception e) {
            glPoint = true;
            MyData.push("glPoint", String.valueOf(false));
        }
        try {
            glPoly = Boolean.parseBoolean(MyData.get_String("glPoly"));
        } catch (Exception e) {
            glPoly = true;
            MyData.push("glPoly", String.valueOf(true));
        }
        try {
            glFilter = Boolean.parseBoolean(MyData.get_String("glFilter"));
        } catch (Exception e) {
            glFilter = true;
            MyData.push("glFilter", String.valueOf(true));
        }
        try {
            glGradient = Boolean.parseBoolean(MyData.get_String("glGradient"));
        } catch (Exception e) {
            glGradient = true;
            MyData.push("glGradient", String.valueOf(false));
        }
        try {
            glFill = Boolean.parseBoolean(MyData.get_String("glFill"));
        } catch (Exception e) {
            glFill = true;
            MyData.push("glFill", String.valueOf(false));
        }
    }

    private void disableAll() {
        //disabilita pulsanti
    }

    public void updateUI() {
        if(DataSaved.isCanOpen==JOYSTICKS){
            marcia.setVisibility(View.VISIBLE);
            marcia.setText(DPadHelper.getMarcia(DPadHelper.getInstance().getStep()));
        }else {
            marcia.setVisibility(View.GONE);
        }
        if (glVista3d == 2) {
            btn_zoomP.setAlpha(0.3f);
            btn_zoomM.setAlpha(0.3f);
            btn_zoomC.setAlpha(0.3f);
            gl_pan_pinch.setAlpha(0.3f);
        } else {
            if (DataSaved.typeView == 0 || DataSaved.typeView == 1) {
                btn_zoomP.setAlpha(1f);
                btn_zoomM.setAlpha(1f);
                btn_zoomC.setAlpha(1f);
                gl_pan_pinch.setAlpha(1f);
            }else {
                btn_zoomP.setAlpha(0.3f);
                btn_zoomM.setAlpha(0.3f);
                btn_zoomC.setAlpha(1f);
                gl_pan_pinch.setAlpha(0.3f);
            }
        }
        if (DataSaved.isWL == DOZER || DataSaved.isWL == DOZER_SIX || DataSaved.isWL == GRADER) {
            if (prepLeft || prepRight) {
                hydroPoint.setVisibility(View.VISIBLE);
            } else {
                hydroPoint.setVisibility(View.GONE);
            }
        } else {
            hydroPoint.setVisibility(View.GONE);
        }
        if (!dialogCutFill3D.dialog.isShowing() && !dialogGnssCoordinates.alertDialog.isShowing()) {

            try {
                if (sideBar.getVisibility() == View.GONE) {
                    btn_hide.setVisibility(View.GONE);
                    btn_show.setVisibility(View.VISIBLE);

                } else {
                    btn_hide.setVisibility(View.VISIBLE);
                    btn_show.setVisibility(View.GONE);
                }

                if (PNEZD_FUNCTION) {
                    btn_pnezd.setVisibility(View.VISIBLE);
                    gl_pnezd.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    btn_pnezd.setVisibility(View.GONE);
                    gl_pnezd.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                }
                if (hAlarm) {
                    allarmeAlt.setVisibility(View.VISIBLE);
                } else {
                    allarmeAlt.setVisibility(View.GONE);
                }
                if (DataSaved.enOUT == 1) {
                    if (isOffgrid) {
                        allarmeBound.setVisibility(View.VISIBLE);
                    } else {
                        allarmeBound.setVisibility(View.GONE);
                    }
                } else {
                    allarmeBound.setVisibility(View.GONE);
                }
                generalCoord.setTextColor(MyColorClass.colorConstraint);
                generalnfo.setTextColor(MyColorClass.colorConstraint);
                generalnfo.setText(bucketName + "\n" + "Offset: " + Utils.readUnitOfMeasure(String.valueOf(-DataSaved.offsetH)));
                switch (DataSaved.bucketEdge) {
                    case -1:
                        generalCoord.setText(coordShow(DataSaved.coordOrder)[0]);
                        break;

                    case 0:
                        generalCoord.setText(coordShow(DataSaved.coordOrder)[1]);

                        break;

                    case 1:
                        generalCoord.setText(coordShow(DataSaved.coordOrder)[2]);

                        break;
                }

                if (isFinishedDTM && isFinishedPOLY && isFinishedPOINT) {
                    progress.setVisibility(View.GONE);
                    loading.setVisibility(View.GONE);
                }
                setupBoxes();
                btn_hide.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));
                btn_show.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));
                switch (DataSaved.bucketEdge) {
                    case 1:
                        if (DataSaved.isWL == EXCAVATOR) {
                            bucketEdge.setImageResource(R.drawable.benna_misura_destra);
                        } else {
                            bucketEdge.setImageResource(R.drawable.lama_misura_destra);
                        }
                        break;
                    case 0:
                        if (DataSaved.isWL == EXCAVATOR) {
                            bucketEdge.setImageResource(R.drawable.benna_misura_cnt);
                        } else {
                            bucketEdge.setImageResource(R.drawable.lama_misura_cnt);
                        }
                        break;
                    case -1:
                        if (DataSaved.isWL == EXCAVATOR) {
                            bucketEdge.setImageResource(R.drawable.benna_misura_sinistra);
                        } else {
                            bucketEdge.setImageResource(R.drawable.lama_misura_sinistra);
                        }
                        break;
                }
                if (DataSaved.isLowerEdge) {
                    bucketEdge.setBackground(getDrawable(R.drawable.custom_background_test3d_box_giallo));
                } else {
                    bucketEdge.setBackground(getDrawable(R.drawable.custom_background_test3d_box_grigino));
                }


                gl_vista.setImageTintList(ColorStateList.valueOf(Color.WHITE));

                if (DataSaved.gpsOk && errorCode == 0) {

                    gl_gps.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
                    gl_gps.setBackground(getDrawable(R.drawable.custom_background_test3d_box_gpsok));
                    flip = 0;
                } else {

                    flipFlop();
                    flip += 1;
                    flip = flip % 20;
                }

                if (isPan) {
                    gl_pan_pinch.setImageResource(R.drawable.gl_pan);
                } else {
                    gl_pan_pinch.setImageResource(R.drawable.baseline_swipe_96);
                }

                if (DataSaved.showAlign > 0) {
                    btn_croce.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    btn_croce.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                }
                if (glFace) {
                    gl_facce.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    gl_facce.setImageTintList(ColorStateList.valueOf(Color.GRAY));
                }
                if (glPoly) {
                    gl_poly.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    gl_poly.setImageTintList(ColorStateList.valueOf(Color.GRAY));
                }
                if (glPoint) {
                    gl_punti.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    gl_punti.setImageTintList(ColorStateList.valueOf(Color.GRAY));
                }
                if (glText) {
                    gl_testi.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    gl_testi.setImageTintList(ColorStateList.valueOf(Color.GRAY));
                }
                if (glGradient) {
                    gl_gradient.setImageResource(R.drawable.gradient);
                } else {
                    gl_gradient.setImageResource(R.drawable.gradient_off);
                }
                if (glFill) {
                    gl_fill.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                } else {
                    gl_fill.setImageTintList(ColorStateList.valueOf(Color.GRAY));
                }
                if (glFilter) {
                    gl_filter.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                    gl_filter.setImageResource(R.drawable.filter_96);
                } else {
                    gl_filter.setImageTintList(ColorStateList.valueOf(Color.GRAY));
                    gl_filter.setImageResource(R.drawable.filter_off96);
                }

                setLightBar();
                AutoHandling();
            } catch (Exception e) {
                Log.e("ErrorUI", Log.getStackTraceString(e));
            }
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setupBoxes() {
        view1.setBackgroundColor(MyColorClass.colorConstraint);
        view2.setBackgroundColor(MyColorClass.colorConstraint);
        if (glVista3d == 1) {
            //navigatorHDT.setVisibility(View.INVISIBLE);
            navigatorHDT.setVisibility(View.VISIBLE);
            float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
            rotBus = rotBus % 360;
            navigatorHDT.setRotation(rotBus);
            freccia.setVisibility(View.INVISIBLE);
            gl_vista.setImageResource(R.drawable.tredi_vista);
        } else if (glVista3d == 0) {
            navigatorHDT.setVisibility(View.VISIBLE);
            float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
            rotBus = rotBus % 360;
            navigatorHDT.setRotation(rotBus);
            freccia.setVisibility(View.VISIBLE);
            isPan = true;
            gl_vista.setImageResource(R.drawable.duedi_vista);
        } else {
            navigatorHDT.setVisibility(View.VISIBLE);
            float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
            rotBus = rotBus % 360;
            navigatorHDT.setRotation(rotBus);
            freccia.setVisibility(View.VISIBLE);
            isPan = true;
            gl_vista.setImageResource(R.drawable.baseline_panorama_96);
        }
        int colorUp = 0, colorDown = 0, colorGreen = 0;
        int colorUpCF, colorDownCF, colorGreenCF;
        if (DataSaved.colorMode == 0) {
            colorUpCF = R.drawable.custom_background_test3d_rosso;
            colorDownCF = R.drawable.custom_background_test3d_box_blu;
            colorGreenCF = R.drawable.custom_background_test3d_verde;
        } else {
            colorDownCF = R.drawable.custom_background_test3d_rosso;
            colorUpCF = R.drawable.custom_background_test3d_box_blu;
            colorGreenCF = R.drawable.custom_background_test3d_verde;
        }
        if (!isCutFill) {
            boxLeft.setVisibility(View.VISIBLE);
            if (DataSaved.isAutoSnap > 0) {
                frameCent.setVisibility(View.VISIBLE);
                boxCent.setVisibility(View.GONE);
            } else {
                frameCent.setVisibility(View.GONE);
                boxCent.setVisibility(View.VISIBLE);

            }
            boxRight.setVisibility(View.VISIBLE);
            txtCutFill.setVisibility(View.GONE);
            inizioValue = 0.0f;
            if (DataSaved.colorMode == 0) {
                colorUp = R.drawable.custom_background_test3d_rosso;
                colorDown = R.drawable.custom_background_test3d_box_blu;
                colorGreen = R.drawable.custom_background_test3d_verde;
            } else {
                colorDown = R.drawable.custom_background_test3d_rosso;
                colorUp = R.drawable.custom_background_test3d_box_blu;
                colorGreen = R.drawable.custom_background_test3d_verde;
            }
        } else {
            txtCutFill.setVisibility(View.VISIBLE);
            if ((MyData.get_Int("UpperBar_Visible") == 1)) {
                boxLeft.setVisibility(View.VISIBLE);
                boxRight.setVisibility(View.VISIBLE);
                if (DataSaved.isAutoSnap > 0) {
                    frameCent.setVisibility(View.VISIBLE);
                    boxCent.setVisibility(View.GONE);
                } else {
                    frameCent.setVisibility(View.GONE);
                    boxCent.setVisibility(View.VISIBLE);

                }
                colorUp = R.drawable.custom_background_test3d_box;
                colorDown = R.drawable.custom_background_test3d_box;
                colorGreen = R.drawable.custom_background_test3d_box;
            } else {
                boxLeft.setVisibility(View.GONE);
                boxRight.setVisibility(View.GONE);
                boxCent.setVisibility(View.GONE);
                if (DataSaved.isAutoSnap > 0) {
                    frameCent.setVisibility(View.VISIBLE);
                } else {
                    frameCent.setVisibility(View.GONE);

                }

            }
            {

                if (DataSaved.typeView != 1) {
                    inizioValue = 0.42f;
                } else {
                    inizioValue = 0.32f;
                }
                switch (DataSaved.bucketEdge) {
                    case -1:
                        if (TriangleService.ltOffGrid) {
                            txtCutFill.setText("-.---");
                            txtCutFill.setTextColor(Color.WHITE);
                            txtCutFill.setBackground(getDrawable(R.drawable.custom_background_test3d_box));
                        } else {
                            if (TriangleService.quota3D_SX > DataSaved.deadbandH) {
                                txtCutFill.setBackground(getResources().getDrawable(colorUpCF));
                                txtCutFill.setTextColor(Color.WHITE);
                                txtCutFill.setText("▼\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                                CanSender.onGrade = (byte) 125;
                            } else if (TriangleService.quota3D_SX < -DataSaved.deadbandH) {
                                txtCutFill.setTextColor(Color.WHITE);
                                txtCutFill.setBackground(getResources().getDrawable(colorDownCF));
                                txtCutFill.setText("▲\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                                CanSender.onGrade = (byte) 131;
                            } else if (TriangleService.quota3D_SX >= -DataSaved.deadbandH && TriangleService.quota3D_SX <= DataSaved.deadbandH) {
                                txtCutFill.setTextColor(Color.DKGRAY);

                                txtCutFill.setBackground(getDrawable(colorGreenCF));
                                txtCutFill.setText("⧗\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                                CanSender.onGrade = (byte) 128;
                            }
                        }
                        break;

                    case 0:
                        if (TriangleService.ctOffGrid) {
                            txtCutFill.setText("-.---");
                            txtCutFill.setTextColor(Color.WHITE);
                            txtCutFill.setBackground(getDrawable(R.drawable.custom_background_test3d_box));
                        } else {
                            if (TriangleService.quota3D_CT > DataSaved.deadbandH) {
                                txtCutFill.setTextColor(Color.WHITE);
                                txtCutFill.setBackground(getResources().getDrawable(colorUpCF));
                                txtCutFill.setText("▼\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                CanSender.onGrade = (byte) 125;
                            } else if (TriangleService.quota3D_CT < -DataSaved.deadbandH) {
                                txtCutFill.setTextColor(Color.WHITE);
                                txtCutFill.setBackground(getResources().getDrawable(colorDownCF));
                                txtCutFill.setText("▲\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                CanSender.onGrade = (byte) 131;
                            } else if (TriangleService.quota3D_CT >= -DataSaved.deadbandH && TriangleService.quota3D_CT <= DataSaved.deadbandH) {
                                txtCutFill.setTextColor(Color.DKGRAY);
                                txtCutFill.setBackground(getDrawable(colorGreenCF));
                                txtCutFill.setText("⧗\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                CanSender.onGrade = (byte) 128;
                            }
                        }
                        break;

                    case 1:
                        if (TriangleService.rtOffGrid) {
                            txtCutFill.setText("-.---");
                            txtCutFill.setTextColor(Color.WHITE);
                            txtCutFill.setBackground(getDrawable(R.drawable.custom_background_test3d_box));
                        } else {
                            if (TriangleService.quota3D_DX > DataSaved.deadbandH) {
                                txtCutFill.setTextColor(Color.WHITE);
                                txtCutFill.setBackground(getResources().getDrawable(colorUpCF));
                                txtCutFill.setText("▼\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                                CanSender.onGrade = (byte) 125;
                            } else if (TriangleService.quota3D_DX < -DataSaved.deadbandH) {
                                txtCutFill.setTextColor(Color.WHITE);
                                txtCutFill.setBackground(getResources().getDrawable(colorDownCF));
                                txtCutFill.setText("▲\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                                CanSender.onGrade = (byte) 131;
                            } else if (TriangleService.quota3D_DX >= -DataSaved.deadbandH && TriangleService.quota3D_SX <= DataSaved.deadbandH) {
                                txtCutFill.setTextColor(Color.DKGRAY);
                                txtCutFill.setBackground(getDrawable(colorGreenCF));
                                txtCutFill.setText("⧗\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                                CanSender.onGrade = (byte) 128;
                            }
                        }
                        break;
                }

            }


        }


        switch (DataSaved.typeView) {
            case 0:
                if (isCutFill) {
                    paramsV1.guidePercent = 0f + inizioValue;
                } else {
                    paramsV1.guidePercent = -0.02f + inizioValue;
                }
                paramsV2.guidePercent = 0.5f;
                inizio.guidePercent = 0.0f + inizioValue;
                fine.guidePercent = 1.0f;
                view1.setVisibility(View.INVISIBLE);
                view2.setVisibility(View.INVISIBLE);
                panel1.setVisibility(View.GONE);
                panel2.setVisibility(View.GONE);
                if (glVista3d == 2) {
                    glSurfaceView.setVisibility(View.GONE);
                    panel3.setVisibility(View.VISIBLE);
                    layer3Canvas.invalidate();
                } else {
                    glSurfaceView.setVisibility(View.VISIBLE);
                    panel3.setVisibility(View.GONE);
                }
                break;
            case 1:
                paramsV1.guidePercent = 0.4f + inizioValue;
                paramsV2.guidePercent = 0.5f;
                inizio.guidePercent = 0 + inizioValue;
                fine.guidePercent = 1.0f;
                view1.setVisibility(View.VISIBLE);
                view2.setVisibility(View.VISIBLE);
                panel1.setVisibility(View.VISIBLE);
                panel2.setVisibility(View.VISIBLE);
                layer1Canvas.invalidate();
                layer2Canvas.invalidate();
                if (glVista3d == 2) {
                    glSurfaceView.setVisibility(View.GONE);
                    panel3.setVisibility(View.VISIBLE);
                    layer3Canvas.invalidate();
                } else {
                    glSurfaceView.setVisibility(View.VISIBLE);
                    panel3.setVisibility(View.GONE);
                }
                break;
            case 2:
                paramsV1.guidePercent = 1.02f;
                paramsV2.guidePercent = 0.5f;
                inizio.guidePercent = 0.0f + inizioValue;
                fine.guidePercent = 1.0f + inizioValue;
                view1.setVisibility(View.INVISIBLE);
                view2.setVisibility(View.VISIBLE);
                glSurfaceView.setVisibility(View.GONE);
                panel1.setVisibility(View.VISIBLE);
                panel2.setVisibility(View.VISIBLE);
                layer1Canvas.invalidate();
                layer2Canvas.invalidate();
                panel3.setVisibility(View.GONE);
                break;
            case 3:
                paramsV1.guidePercent = 1.02f;
                paramsV2.guidePercent = 1.02f;
                inizio.guidePercent = 0.0f + inizioValue;
                fine.guidePercent = 1.0f;
                view1.setVisibility(View.INVISIBLE);
                view2.setVisibility(View.INVISIBLE);
                glSurfaceView.setVisibility(View.GONE);
                panel1.setVisibility(View.VISIBLE);
                panel2.setVisibility(View.GONE);
                layer1Canvas.invalidate();
                panel3.setVisibility(View.GONE);
                break;
            case 4:
                paramsV1.guidePercent = 1.02f;
                paramsV2.guidePercent = -0.02f;
                inizio.guidePercent = 0.0f + inizioValue;
                fine.guidePercent = 1.0f;
                view1.setVisibility(View.INVISIBLE);
                view2.setVisibility(View.INVISIBLE);
                glSurfaceView.setVisibility(View.GONE);
                panel1.setVisibility(View.GONE);
                panel2.setVisibility(View.VISIBLE);
                layer2Canvas.invalidate();
                panel3.setVisibility(View.GONE);
                break;


        }
        divisorio_orizzontale.setLayoutParams(paramsV2);
        divisorio_verticale.setLayoutParams(paramsV1);
        divisorio_inizio.setLayoutParams(inizio);
        divisorio_fine.setLayoutParams(fine);


        if (TriangleService.ltOffGrid) {
            boxLeft.setText("-.---");
            boxLeft.setTextColor(Color.WHITE);
            boxLeft.setBackground(getDrawable(R.drawable.custom_background_test3d_box));
        } else {
            if (TriangleService.quota3D_SX > DataSaved.deadbandH) {
                boxLeft.setBackground(getResources().getDrawable(colorUp));
                boxLeft.setTextColor(Color.WHITE);
                boxLeft.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                CanSender.onGrade = (byte) 125;
            } else if (TriangleService.quota3D_SX < -DataSaved.deadbandH) {
                boxLeft.setTextColor(Color.WHITE);
                boxLeft.setBackground(getResources().getDrawable(colorDown));
                boxLeft.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                CanSender.onGrade = (byte) 131;
            } else if (TriangleService.quota3D_SX >= -DataSaved.deadbandH && TriangleService.quota3D_SX <= DataSaved.deadbandH) {
                if (!isCutFill) {
                    boxLeft.setTextColor(Color.DKGRAY);
                } else {
                    boxLeft.setTextColor(Color.WHITE);
                }
                boxLeft.setBackground(getDrawable(colorGreen));
                boxLeft.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                CanSender.onGrade = (byte) 128;
            }
        }
        ///
        if (DataSaved.isAutoSnap == 0) {


            if (TriangleService.ctOffGrid) {
                boxCent.setText("-.---");
                boxCent.setTextColor(Color.WHITE);
                boxCent.setBackground(getDrawable(R.drawable.custom_background_test3d_box));
            } else {
                if (TriangleService.quota3D_CT > DataSaved.deadbandH) {
                    boxCent.setTextColor(Color.WHITE);
                    boxCent.setBackground(getResources().getDrawable(colorUp));
                    boxCent.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                    CanSender.onGrade = (byte) 125;
                } else if (TriangleService.quota3D_CT < -DataSaved.deadbandH) {
                    boxCent.setTextColor(Color.WHITE);
                    boxCent.setBackground(getResources().getDrawable(colorDown));
                    boxCent.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                    CanSender.onGrade = (byte) 131;
                } else if (TriangleService.quota3D_CT >= -DataSaved.deadbandH && TriangleService.quota3D_CT <= DataSaved.deadbandH) {
                    if (!isCutFill) {
                        boxCent.setTextColor(Color.DKGRAY);
                    } else {
                        boxCent.setTextColor(Color.WHITE);
                    }
                    boxCent.setBackground(getDrawable(colorGreen));
                    boxCent.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                    CanSender.onGrade = (byte) 128;
                }
            }

        } else {

            String offsetS = "";
            double rot;
            double rotFix = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));

            if (DataSaved.lockUnlock == 0) {
                lucchetto.setImageResource(R.drawable.unlock);
            } else {
                lucchetto.setImageResource(R.drawable.lock);
            }
            if (DataSaved.isAutoSnap == 1 || DataSaved.isAutoSnap == 3) {
                offsetS = "";
                double x = 0, y = 0;
                switch (DataSaved.bucketEdge) {
                    case -1:
                        x = ExcavatorLib.bucketLeftCoord[0];
                        y = ExcavatorLib.bucketLeftCoord[1];
                        break;

                    case 0:
                        x = ExcavatorLib.bucketCoord[0];
                        y = ExcavatorLib.bucketCoord[1];
                        break;

                    case 1:
                        x = ExcavatorLib.bucketRightCoord[0];
                        y = ExcavatorLib.bucketRightCoord[1];
                        break;
                }
                rot = My_LocationCalc.calcBearingXY(x, y, DataSaved.nearestPoint.getX(), DataSaved.nearestPoint.getY());
                rot = rot + rotFix;
                rot = rot % 360;
                freccia.setRotation((float) rot);

            } else if (DataSaved.isAutoSnap == 2 || DataSaved.isAutoSnap == 4) {
                offsetS = "\n" + "(" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)) + ")";
                rot = TriangleService.orientamentoFreccia + rotFix;
                rot = rot % 360;
                freccia.setRotation((float) rot);

            }
            switch (DataSaved.bucketEdge) {
                case -1:
                    dist = TriangleService.dist3D_SX;

                    break;
                case 0:
                    dist = TriangleService.dist3D_CT;
                    break;

                case 1:
                    dist = TriangleService.dist3D_DX;
                    break;
            }
            if (Math.abs(dist) <= DataSaved.tolleranza_XY) {
                frameCent.setBackground(getResources().getDrawable(R.drawable.custom_background_test3d_verde));
                freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                freccia.setImageResource(R.drawable.baseline_radio_button_checked_96);
                lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color._____cancel_text)));
                txtDist.setTextColor(getResources().getColor(R.color._____cancel_text));

            } else {
                frameCent.setBackground(getResources().getDrawable(R.drawable.custom_background_test3d_box));
                freccia.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                freccia.setImageResource(R.drawable.navigator_white);
                lucchetto.setImageTintList(ColorStateList.valueOf(getColor(R.color.white)));
                txtDist.setTextColor(getResources().getColor(R.color.white));
            }
            distances = Utils.readUnitOfMeasureLITE(String.valueOf(dist));
            txtDist.setText(distances + offsetS);


        }

        ////
        if (TriangleService.rtOffGrid) {
            boxRight.setText("-.---");
            boxRight.setTextColor(Color.WHITE);
            boxRight.setBackground(getDrawable(R.drawable.custom_background_test3d_box));
        } else {
            if (TriangleService.quota3D_DX > DataSaved.deadbandH) {
                boxRight.setTextColor(Color.WHITE);
                boxRight.setBackground(getResources().getDrawable(colorUp));
                boxRight.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                CanSender.onGrade = (byte) 125;
            } else if (TriangleService.quota3D_DX < -DataSaved.deadbandH) {
                boxRight.setTextColor(Color.WHITE);
                boxRight.setBackground(getResources().getDrawable(colorDown));
                boxRight.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                CanSender.onGrade = (byte) 131;
            } else if (TriangleService.quota3D_DX >= -DataSaved.deadbandH && TriangleService.quota3D_SX <= DataSaved.deadbandH) {
                if (!isCutFill) {
                    boxRight.setTextColor(Color.DKGRAY);
                } else {
                    boxRight.setTextColor(Color.WHITE);
                }
                boxRight.setBackground(getDrawable(colorGreen));
                boxRight.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                CanSender.onGrade = (byte) 128;
            }
        }

    }

    public void showColorPickerDialog(Context context) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.color_picker_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        titolo = dialogView.findViewById(R.id.titolo);
        ImageView cambia = dialogView.findViewById(R.id.function);
        seekRed = dialogView.findViewById(R.id.seekRed);
        seekGreen = dialogView.findViewById(R.id.seekGreen);
        seekBlue = dialogView.findViewById(R.id.seekBlue);
        colorPreview = dialogView.findViewById(R.id.colorPreview);
        Button btnOk = dialogView.findViewById(R.id.btnOk);

        AlertDialog dialog = builder.create();

        // Listener comune per tutte le seekbar
        SeekBar.OnSeekBarChangeListener commonListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int r = seekRed.getProgress();
                int g = seekGreen.getProgress();
                int bVal = seekBlue.getProgress();
                updatePreview(r, g, bVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };

        // Mostra i dati iniziali per initialFunction = 0
        updateDialogForFunction(initialFunction, commonListener);

        // Cambio funzione cliccando sull'immagine
        cambia.setOnClickListener(view -> {
            initialFunction = (initialFunction + 1) % 3;
            updateDialogForFunction(initialFunction, commonListener);
        });

        btnOk.setOnClickListener(v -> {
            saveColorIfChanged("colorBucket", coloreIniziale0, coloreAttuale0);
            saveColorIfChanged("colorBoom", coloreIniziale1, coloreAttuale1);
            saveColorIfChanged("colorQuick", coloreIniziale2, coloreAttuale2);
            dialog.dismiss();
            recreate();
        });

        dialog.show();
    }

    // Variabili per colore iniziale e attuale per ciascuna funzione
    private int coloreIniziale0, coloreAttuale0;
    private int coloreIniziale1, coloreAttuale1;
    private int coloreIniziale2, coloreAttuale2;

    private void updateDialogForFunction(int function, SeekBar.OnSeekBarChangeListener listener) {
        int color;
        String title;
        switch (function) {
            case 0:
                title = "BUCKET COLOR";
                color = MyData.get_Int("colorBucket");
                coloreIniziale0 = color;
                break;
            case 1:
                title = "BOOM COLOR";
                color = MyData.get_Int("colorBoom");
                coloreIniziale1 = color;
                break;
            case 2:
            default:
                title = "QUICK COUPLER COLOR";
                color = MyData.get_Int("colorQuick");
                coloreIniziale2 = color;
                break;
        }

        titolo.setText(title);

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Imposta seekbar (rimuove temporaneamente listener per evitare trigger inutili)
        seekRed.setOnSeekBarChangeListener(null);
        seekGreen.setOnSeekBarChangeListener(null);
        seekBlue.setOnSeekBarChangeListener(null);

        seekRed.setProgress(r);
        seekGreen.setProgress(g);
        seekBlue.setProgress(b);

        updatePreview(r, g, b);

        // Assegna il listener
        seekRed.setOnSeekBarChangeListener(listener);
        seekGreen.setOnSeekBarChangeListener(listener);
        seekBlue.setOnSeekBarChangeListener(listener);

        // Aggiorna il colore attuale associato
        int actualColor = Color.rgb(r, g, b);
        switch (function) {
            case 0:
                coloreAttuale0 = actualColor;
                break;
            case 1:
                coloreAttuale1 = actualColor;
                break;
            case 2:
                coloreAttuale2 = actualColor;
                break;
        }
    }

    private void saveColorIfChanged(String key, int initialColor, int currentColor) {
        if (initialColor != currentColor) {
            MyData.push(key, String.valueOf(currentColor));
        }
    }

    public void updatePreview(int r, int g, int b) {
        colorPreview.setBackgroundColor(Color.rgb(r, g, b));

        int newColor = Color.rgb(r, g, b);
        switch (initialFunction) {
            case 0:
                coloreAttuale0 = newColor;
                break;
            case 1:
                coloreAttuale1 = newColor;
                break;
            case 2:
                coloreAttuale2 = newColor;
                break;
        }
    }


    private void setLightBar() {
        if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER) {
            switch (DataSaved.bucketEdge) {
                case -1:
                    MyDeviceManager.CanWrite(true, 0, 0xA0, 3, LeicaLB.mapping(TriangleService.ltOffGrid, TriangleService.quota3D_SX, DataSaved.deadbandH));
                    //setAudio(TriangleService.quota3D_SX, !TriangleService.ltOffGrid);
                    break;
                case 0:
                    MyDeviceManager.CanWrite(true, 0, 0xA0, 3, LeicaLB.mapping(TriangleService.ctOffGrid, TriangleService.quota3D_CT, DataSaved.deadbandH));
                    //setAudio(TriangleService.quota3D_CT, !TriangleService.ctOffGrid);
                    break;
                case 1:
                    MyDeviceManager.CanWrite(true, 0, 0xA0, 3, LeicaLB.mapping(TriangleService.rtOffGrid, TriangleService.quota3D_DX, DataSaved.deadbandH));
                    //setAudio(TriangleService.quota3D_DX, !TriangleService.rtOffGrid);
                    break;
            }
        } else {
            MyDeviceManager.CanWrite(true, 0, 0xA0, 3, LeicaLB.mapping(TriangleService.ltOffGrid, TriangleService.quota3D_SX, DataSaved.deadbandH));

            MyDeviceManager.CanWrite(true, 0, 0xA8, 3, LeicaLB.mapping(TriangleService.rtOffGrid, TriangleService.quota3D_DX, DataSaved.deadbandH));
        }
    }

    private void updateMemories() {
        MyData.push("typeView", String.valueOf(DataSaved.typeView));
        MyData.push("glGradient", String.valueOf(glGradient));
        MyData.push("glFill", String.valueOf(glFill));
        MyData.push("glFace", String.valueOf(glFace));
        MyData.push("vista3D", String.valueOf(My3DActivity.glVista3d));
        MyData.push("showAlign", String.valueOf(DataSaved.showAlign));
        MyData.push("glPoly", String.valueOf(glPoly));
        MyData.push("glPoint", String.valueOf(glPoint));
        MyData.push("glFilter", String.valueOf(glFilter));


    }

    private void AutoHandling() {
        if (MyApp.licenseType == 5) {

            if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER) {
                AUTO_SX.setVisibility(View.INVISIBLE);
                AUTO_SS.setVisibility(View.INVISIBLE);
                AUTO_DX.setVisibility(View.INVISIBLE);

            } else {

                if (DataSaved.isWL == GRADER) {
                    //GRADER
                    AUTO_SX.setVisibility(View.VISIBLE);
                    AUTO_SS.setVisibility(View.VISIBLE);
                    AUTO_DX.setVisibility(View.VISIBLE);

                    if (prepLeft) {
                        if (Grader_Auto_Left) {
                            AUTO_SX.setBackground(getDrawable(R.drawable.sfondo_auto_enabled));


                        } else {
                            AUTO_SX.setBackground(getDrawable(R.drawable.sfondo_auto_prepared));
                        }
                    } else {
                        AUTO_SX.setBackground(getDrawable(R.drawable.sfondo_manuale));
                    }


                    if (Grader_Auto_SS) {
                        AUTO_SS.setBackground(getDrawable(R.drawable.sfondo_auto_enabled));


                    } else {
                        AUTO_SS.setBackground(getDrawable(R.drawable.sfondo_manuale));
                    }


                    if (prepRight) {
                        if (Grader_AutoRight) {
                            AUTO_DX.setBackground(getDrawable(R.drawable.sfondo_auto_enabled));

                        } else {
                            AUTO_DX.setBackground(getDrawable(R.drawable.sfondo_auto_prepared));
                        }
                    } else {
                        AUTO_DX.setBackground(getDrawable(R.drawable.sfondo_manuale));
                    }
                } else if (DataSaved.isWL == DOZER || DataSaved.isWL == DOZER_SIX) {
                    //DOZER
                    AUTO_SX.setVisibility(View.VISIBLE);
                    AUTO_SS.setVisibility(View.INVISIBLE);
                    AUTO_DX.setVisibility(View.VISIBLE);


                    if (prepLeft) {
                        if (Dozer_Auto_Main) {
                            AUTO_SX.setBackground(getDrawable(R.drawable.sfondo_auto_enabled));


                        } else {
                            AUTO_SX.setBackground(getDrawable(R.drawable.sfondo_auto_prepared));

                        }
                    } else {
                        AUTO_SX.setBackground(getDrawable(R.drawable.sfondo_manuale));

                    }

                    if (prepRight) {
                        if (Dozer_Auto_Main) {
                            AUTO_DX.setBackground(getDrawable(R.drawable.sfondo_auto_enabled));

                        } else {
                            AUTO_DX.setBackground(getDrawable(R.drawable.sfondo_auto_prepared));


                        }
                    } else {
                        AUTO_DX.setBackground(getDrawable(R.drawable.sfondo_manuale));


                    }
                }


            }

        } else {
            AUTO_SX.setVisibility(View.INVISIBLE);
            AUTO_SS.setVisibility(View.INVISIBLE);
            AUTO_DX.setVisibility(View.INVISIBLE);
        }
    }


    private void locateMachine() {
        if (DataSaved.my_comPort == 4) {
            try {
                DataSaved.demoNORD = DataSaved.dxfFaces.get(0).getP1().getY();
                DataSaved.demoEAST = DataSaved.dxfFaces.get(0).getP1().getX();
                DataSaved.demoZ = DataSaved.dxfFaces.get(0).getP1().getZ() + 3;
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
                        new CustomToast(My3DActivity.this, "Impossible to Locate Machine").show_error();
                    }
                }
            }

        }
        setDpad();
    }
    private void setDpad(){

        DPadHelper.getInstance().setXYZ( new double[]{ MyData.get_Double("demoEAST"), MyData.get_Double("demoNORD"), MyData.get_Double("demoZ")});
    }
    private static String[] coordShow(int mode) {
        String s = OUTPUT_HYDRO + "\n";
        if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER) {
            s = "";
        }

        String s0 = s + "E: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[0])) + "\nN: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[1])) + "\nZ: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[2]));
        String s1 = s + "E: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[0])) + "\nN: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[1])) + "\nZ: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[2]));
        String s2 = s + "E: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[0])) + "\nN: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[1])) + "\nZ: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[2]));


        String s4 = s + "N: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[1])) + "\nE: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[0])) + "\nZ: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[2]));
        String s5 = s + "N: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[1])) + "\nE: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[0])) + "\nZ: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[2]));
        String s6 = s + "N: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[1])) + "\nE: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[0])) + "\nZ: " + Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[2]));

        if (mode == 0) {
            return new String[]{s0, s1, s2};
        } else {
            return new String[]{s4, s5, s6};
        }
    }

    private void flipFlop() {

        if (flip == 0) {
            gl_gps.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            gl_gps.setBackground(getDrawable(R.drawable.custom_background_test3d_box_gpsko));
        }
        if (flip == 10) {
            gl_gps.setImageTintList(ColorStateList.valueOf(Color.RED));
            gl_gps.setBackground(getDrawable(R.drawable.custom_background_test3d_box_grigino));
        }
    }
}
