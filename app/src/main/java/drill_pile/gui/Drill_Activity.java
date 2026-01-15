package drill_pile.gui;

import static gui.MyApp.errorCode;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.draw_class.MyColorClass;
import okio.Utf8;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
import utils.Utils;

public class Drill_Activity extends BaseClass {
    double currentDepth = 0;
    int flip = 0;
    Dialog_Drill_GNSS dialogDrillGnss;
    View divisorioC, divisorioDx, divisorioUp, divisorioDw;
    ImageView digMenu, drilltool, Status, folders, playpause,lineReference;
    ConstraintLayout topview, bubble;
    VerticalTargetIndicatorView indicator;
    TextView idpalo,txthdt,txttilt,txtdepth;
    LinearLayout sideLayout;
    int colorUp,colorDown,colorGreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill);
        findView();
        init();
        onClick();

    }

    private void findView() {
        dialogDrillGnss = new Dialog_Drill_GNSS(this);
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
        idpalo=findViewById(R.id.idpalo);
        txthdt=findViewById(R.id.txthdt);
        txttilt=findViewById(R.id.txttilt);
        txtdepth=findViewById(R.id.txtdepth);
        sideLayout=findViewById(R.id.sideLayout);
        lineReference=findViewById(R.id.lineReference);



    }

    private void init() {
        if (DataSaved.colorMode == 0) {
            colorUp = Color.RED;
            colorDown = Color.BLUE;
            colorGreen = Color.GREEN;
        } else {
            colorDown =Color.RED;
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
        double low=1250.123-0.5;
        double high=1250.123+2;
        indicator.setRange(low, high);
        indicator.setTolerance(DataSaved.deadbandH);
        indicator.setColors(colorUp,colorDown,colorGreen);


    }

    private void onClick() {

        lineReference.setOnClickListener(view -> {

            FragmentManager fm = getSupportFragmentManager();
            Fragment existing = fm.findFragmentByTag("drill_grid");
            if (fm.findFragmentByTag("drill_grid") != null) return;

            // 👉 non è aperta → apri
            DrillPointsFullscreenDialog
                        .newInstance("Drill Pattern", ReadProjectService.conversionFactor)
                        .show(fm, "drill_grid");

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
        currentDepth-=0.01;
        indicator.setCurrentValue((float)currentDepth);//TODO adattare in realtime
        txtdepth.setText(Utils.readUnitOfMeasureLITE(String.valueOf(currentDepth-1250.123))+Utils.getMetriSimbol().replace("[","").replace("]",""));

        if(DataSaved.Selected_Point3D_Drill!=null) {
            idpalo.setText("R:" + DataSaved.Selected_Point3D_Drill.getRowId() + " - " + "P:" + DataSaved.Selected_Point3D_Drill.getId());
            txthdt.setText(String.format("%.1f",DataSaved.Selected_Point3D_Drill.getHeadingDeg())+"°");
            txttilt.setText(String.format("%.1f",DataSaved.Selected_Point3D_Drill.getTilt())+"°");

        }else {
            idpalo.setText("R:__ P___");
            txthdt.setText("_°");
            txttilt.setText("_°");
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
}