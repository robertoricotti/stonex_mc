package drill_pile.gui;

import static gui.MyApp.errorCode;

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

import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import services.PointService;
import utils.Utils;

public class Drill_Activity extends BaseClass {
    public static int typeVistaDrill;
    double currentDepth = 0;
    int flip = 0;
    Dialog_Drill_GNSS dialogDrillGnss;
    View divisorioC, divisorioDx, divisorioUp, divisorioDw;
    ImageView digMenu, drilltool, typeView, Status, folders, playpause, lineReference, tiposnap,
            zoom_P, zoom_M, zoom_C, compass;
    ConstraintLayout topview, bubble;
    VerticalTargetIndicatorView indicator;
    TextView idpalo, txthdt, txttilt, txtdepth, uomesure, textInfo;
    LinearLayout sideLayout;
    int colorUp, colorDown, colorGreen;
    Dialog_AutoSnap dialogAutoSnap;
    Guideline cent_v;
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
        stopService(new Intent(this, PointService.class));
    }

    private void findView() {
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
        typeView = findViewById(R.id.typeView);
        compass = findViewById(R.id.compass);
        textInfo = findViewById(R.id.textInfo);


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

        // configurazione iniziale
        currentDepth = 1254.0;

        indicator.setTargetValue(1250.123f);
        double low = 1250.123 - 0.5;
        double high = 1250.123 + 2;
        indicator.setRange(low, high);
        indicator.setTolerance(DataSaved.deadbandH);
        indicator.setColors(colorUp, colorDown, colorGreen);
        textInfo.setTextColor(MyColorClass.colorConstraint);
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

                break;


        }
        uomesure.setText(Utils.getMetriSimbol().replace("[", "").replace("]", ""));


    }

    private void onClick() {
        compass.setOnLongClickListener(view -> {

            return true;
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

        });

    }

    public void updateUI() {
        rot += 0.5f;
        compass.setRotation(rot);
        switch (typeVistaDrill) {
            case 0:
                cent_v.setGuidelinePercent(0.46f);
                zoom_P.setVisibility(View.VISIBLE);
                zoom_M.setVisibility(View.VISIBLE);
                zoom_C.setVisibility(View.VISIBLE);
                compass.setVisibility(View.VISIBLE);
                break;

            case 1:

                cent_v.setGuidelinePercent(-0.01f);
                zoom_P.setVisibility(View.VISIBLE);
                zoom_M.setVisibility(View.VISIBLE);
                zoom_C.setVisibility(View.VISIBLE);
                compass.setVisibility(View.VISIBLE);
                break;

            case 2:
                cent_v.setGuidelinePercent(0.92f);
                zoom_P.setVisibility(View.INVISIBLE);
                zoom_M.setVisibility(View.INVISIBLE);
                zoom_C.setVisibility(View.INVISIBLE);
                compass.setVisibility(View.INVISIBLE);
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
        // realtime update
        currentDepth -= 0.01;
        indicator.setCurrentValue((float) currentDepth);//TODO adattare in realtime
        txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(currentDepth - 1250.123)));

        if (DataSaved.Selected_Point3D_Drill != null) {
            idpalo.setText("R:" + DataSaved.Selected_Point3D_Drill.getRowId() + " - " + "P:" + DataSaved.Selected_Point3D_Drill.getId());
            txthdt.setText(String.format("%.1f", DataSaved.Selected_Point3D_Drill.getHeadingDeg()) + "°");
            txttilt.setText(String.format("%.1f", DataSaved.Selected_Point3D_Drill.getTilt()) + "°");

        } else {
            idpalo.setText("R:___ P:___");
            txthdt.setText("_°");
            txttilt.setText("_°");
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
        String s0 = "Tool E: " + Utils.showCoords(String.valueOf(ExcavatorLib.toolEndCoord[0]));
        String s1 = "Tool N: " + Utils.showCoords(String.valueOf(ExcavatorLib.toolEndCoord[1]));
        String s2 = "Tool Z: " + Utils.showCoords(String.valueOf(ExcavatorLib.toolEndCoord[2]));
        String p= "Project: "+DataSaved.progettoSelected_POINT.substring(DataSaved.progettoSelected_POINT.lastIndexOf("/") + 1);
        if(DataSaved.coordOrder==0){
            return new String(s0+"\n"+s1+"\n"+s2+"\n"+p);
        }else {
            return new String(s1+"\n"+s0+"\n"+s2+"\n"+p);
        }
    }

}