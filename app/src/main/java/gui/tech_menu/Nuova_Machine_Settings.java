package gui.tech_menu;

import static gui.MyApp.errorCode;
import static gui.dialogs_and_toast.DialogPassword.isTech2;
import static packexcalib.exca.DataSaved.isCanOpen;
import static utils.MyTypes.AT_BODY;
import static utils.MyTypes.AT_BOOM;
import static utils.MyTypes.DEMO_BAG;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.FMI_SENS;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.JETGROUTING_MODE;
import static utils.MyTypes.JOYSTICKS;
import static utils.MyTypes.MAST_FORWARD;
import static utils.MyTypes.MAST_LEFT;
import static utils.MyTypes.MAST_RIGHT;
import static utils.MyTypes.ROCKDRILL_MODE;
import static utils.MyTypes.SOLARFARM_MODE;
import static utils.MyTypes.TSM_ACC;
import static utils.MyTypes.WHEELLOADER;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stx_dig.R;

import org.apache.commons.math3.ml.neuralnet.UpdateAction;

import gui.BaseClass;
import gui.MyApp;
import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_CanBaud;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.Dialog_Wheel_Steer;
import packexcalib.exca.DataSaved;
import services.UpdateValuesService;
import utils.MyData;
import utils.MyDeviceManager;

public class Nuova_Machine_Settings extends BaseClass {
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    Dialog_Drill_GNSS dialogDrillGnss;
    CheckBox ckDO, ckUHF, ckUpper, ck_stxGen1, ckDEMO, ckSchermo, ckMach, ck22, ck_stxGen2,ckJ,ckRock,ckJet,ckSolar;
    CheckBox ckBody,ckBoom,ckAtLeft,ckAtFwd,ckAtRight;
    CustomQwertyDialog customQwertyDialog;
    ImageView back, exca, wheel, grader, dozer, drill, menu_1, menu_2, saveToFile, readFromFile, status, menu_3;
    ConstraintLayout constraintLayout, constraintLayout_2, constraintLayout_3;
    TextView toExtraSensor, tvSwing, tvFrame, tvBoom1, tvBoom2, tvStick, tvLink,tvMast, tvTilt, tvXYZ,drillEnc, toCanopen, toDamping, can1bd, can2bd;
    EditText mchName, techInfo;
    int mode, machineSel;
    public static boolean menu1_visible, menu2_visible, menu3_visible;
    DialogPassword dialogPassword;
    Dialog_CanBaud dialogCanBaud;
    Dialog_Swing_Boom dialogSwingBoom;
    Dialog_Wheel_Steer dialogWheelSteer;
    LinearLayout linear_1, linear_2, linear_3,lay_drilmode,lay_antmount,lay_ant_orient;
    int small, bigg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuova_machine_settings);
        if (Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("MEGA_1")) {
            bigg = 80;
            small = 70;
        } else {
            bigg = 110;
            small = 90;
        }
        findView();
        onClick();
        updateCK();
        updateUI();
    }

    private void findView() {
        dialogCanBaud = new Dialog_CanBaud(this);
        dialogPassword = new DialogPassword(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogDrillGnss =new Dialog_Drill_GNSS(this);
        customQwertyDialog = new CustomQwertyDialog(this, null);
        dialogSwingBoom = new Dialog_Swing_Boom(this);
        dialogWheelSteer = new Dialog_Wheel_Steer(this);
        linear_1 = findViewById(R.id.linear_1);
        linear_2 = findViewById(R.id.linear_2);
        linear_3 = findViewById(R.id.linear_3);
        machineSel = MyData.get_Int("MachineSelected");
        mode = MyData.get_Int("M" + machineSel + "_isWL");
        back = findViewById(R.id.btn_1);
        exca = findViewById(R.id.sel_exca);
        wheel = findViewById(R.id.sel_wheel);
        grader = findViewById(R.id.sel_grader);
        dozer = findViewById(R.id.sel_dozer);
        drill = findViewById(R.id.sel_drill);
        menu_1 = findViewById(R.id.bt_menu1);
        menu_2 = findViewById(R.id.bt_sens_set);
        techInfo = findViewById(R.id.techInfo);
        constraintLayout = findViewById(R.id.constraint_general);
        constraintLayout_2 = findViewById(R.id.constr_2);
        constraintLayout_3 = findViewById(R.id.constr_3);
        tvSwing = findViewById(R.id.tvSwing);
        toExtraSensor = findViewById(R.id.toExtraSensor);
        drillEnc = findViewById(R.id.drillEnc);
        lay_drilmode=findViewById(R.id.lay_drilmode);
        ckRock=findViewById(R.id.ckRock);
        ckJet=findViewById(R.id.ckJet);
        ckSolar=findViewById(R.id.ckSolar);
        tvFrame = findViewById(R.id.toFrame);
        tvBoom1 = findViewById(R.id.toBoom1);
        tvBoom2 = findViewById(R.id.toBoom2);
        tvStick = findViewById(R.id.toStick);
        tvLink = findViewById(R.id.toDogBone);
        tvMast=findViewById(R.id.toTool);
        tvTilt = findViewById(R.id.toTilt);
        tvXYZ = findViewById(R.id.toxyz);
        mchName = findViewById(R.id.mch_name);
        ckSchermo = findViewById(R.id.ckSchermo);
        ckMach = findViewById(R.id.ckMach);
        ckDO = findViewById(R.id.ck2);
        ck22 = findViewById(R.id.ck22);
        ckJ = findViewById(R.id.ckJ);
        ckUHF = findViewById(R.id.ck3);
        ckUpper = findViewById(R.id.ck4);
        ck_stxGen2 = findViewById(R.id.ckVecchia);
        ck_stxGen1 = findViewById(R.id.ck5);
        ckDEMO = findViewById(R.id.ck6);
        saveToFile = findViewById(R.id.img01);
        readFromFile = findViewById(R.id.img11);
        status = findViewById(R.id.img00);
        menu_3 = findViewById(R.id.bt_menu3);
        toCanopen = findViewById(R.id.toCanOpen);
        toDamping = findViewById(R.id.toDamping);
        can1bd = findViewById(R.id.toCan1);
        can2bd = findViewById(R.id.toCan2);
        lay_antmount=findViewById(R.id.lay_antmount);
        lay_ant_orient=findViewById(R.id.lay_ant_orient);
        ckBody=findViewById(R.id.ckBody);
        ckBoom=findViewById(R.id.ckBoom);
        ckAtLeft=findViewById(R.id.ckAtLeft);
        ckAtFwd=findViewById(R.id.ckAtFwd);
        ckAtRight=findViewById(R.id.ckAtRight);


        mchName.setText(MyData.get_String("M" + machineSel + "_Name"));

    }

    private void onClick() {
        ckBody.setOnClickListener(view -> {
            ckBody.setChecked(true);
            ckBoom.setChecked(false);
            DataSaved.Drill_Antenna_Mounting=AT_BODY;
            MyData.push("M"+machineSel+"Drill_Antenna_Mounting",AT_BODY);
        });
        ckBoom.setOnClickListener(view -> {
            DataSaved.L_Boom1=0;
            MyData.push("M"+machineSel+"_LengthBoom1","0.0");
            ckBody.setChecked(false);
            ckBoom.setChecked(true);
            DataSaved.Drill_Antenna_Mounting=AT_BOOM;
            MyData.push("M"+machineSel+"Drill_Antenna_Mounting",AT_BOOM);
            DataSaved.lrFrame=0;
            DataSaved.lrBoom1=0;
            DataSaved.lrBoom2=0;
            DataSaved.lrStick=0;
            DataSaved.lrTilt=0;
            DataSaved.lrBucket=0;
            MyData.push("M"+machineSel+"_Frame_MountPos","0");
            MyData.push("M"+machineSel+"_Boom1_MountPos","0");
            MyData.push("M"+machineSel+"_Boom2_MountPos","0");
            MyData.push("M"+machineSel+"_Stick_MountPos","0");
            MyData.push("M"+machineSel+"_Bucket_MountPos","0");

        });

        ckAtLeft.setOnClickListener(view -> {
            ckAtLeft.setChecked(true);
            ckAtFwd.setChecked(false);
            ckAtRight.setChecked(false);
            DataSaved.Drill_Mast_Position=MAST_LEFT;
            MyData.push("M"+machineSel+"Drill_Mast_Position",MAST_LEFT);
        });
        ckAtFwd.setOnClickListener(view -> {
            ckAtLeft.setChecked(false);
            ckAtFwd.setChecked(true);
            ckAtRight.setChecked(false);
            DataSaved.Drill_Mast_Position=MAST_FORWARD;
            MyData.push("M"+machineSel+"Drill_Mast_Position",MAST_FORWARD);
        });
        ckAtRight.setOnClickListener(view -> {
            ckAtLeft.setChecked(false);
            ckAtFwd.setChecked(false);
            ckAtRight.setChecked(true);
            DataSaved.Drill_Mast_Position=MAST_RIGHT;
            MyData.push("M"+machineSel+"Drill_Mast_Position",MAST_RIGHT);
        });




        can1bd.setOnClickListener(view -> {
            if (!dialogCanBaud.dialog.isShowing()) {
                dialogCanBaud.show(1);
            }
        });
        can2bd.setOnClickListener(view -> {
            if (!dialogCanBaud.dialog.isShowing()) {
                dialogCanBaud.show(2);
            }
        });
        status.setOnClickListener(view -> {
            if(DataSaved.isWL==DRILL){
                if (!dialogDrillGnss.alertDialog.isShowing()) {
                    dialogDrillGnss.show();
                }
            }else {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                    dialogGnssCoordinates.show();
                }
            }
        });

        ckRock.setOnClickListener(view -> {
            ckRock.setChecked(true);
            ckJet.setChecked(false);
            ckSolar.setChecked(false);
            MyData.push("M" + machineSel + "Drilling_Mode", "0");
            DataSaved.Drilling_Mode=0;

        });
        ckJet.setOnClickListener(view -> {
            ckRock.setChecked(false);
            ckJet.setChecked(true);
            ckSolar.setChecked(false);
            MyData.push("M" + machineSel + "Drilling_Mode", "1");
            DataSaved.Drilling_Mode=1;

        });
        ckSolar.setOnClickListener(view -> {
            ckRock.setChecked(false);
            ckJet.setChecked(false);
            ckSolar.setChecked(true);
            MyData.push("M" + machineSel + "Drilling_Mode", "2");
            DataSaved.Drilling_Mode=2;

        });

        ck_stxGen1.setOnClickListener(view -> {
            ckDEMO.setChecked(false);
            ck_stxGen1.setChecked(true);
            ck_stxGen2.setChecked(false);
            ckJ.setChecked(false);
            MyData.push("M" + machineSel + "_useCanOpen", "3");
            isCanOpen=TSM_ACC;

        });
        ck_stxGen2.setOnClickListener(view -> {
            ckDEMO.setChecked(false);
            ck_stxGen2.setChecked(true);
            ck_stxGen1.setChecked(false);
            ckJ.setChecked(false);
            MyData.push("M" + machineSel + "_useCanOpen", "1");
            isCanOpen=FMI_SENS;

        });
        ckDEMO.setOnClickListener(view -> {
            ckDEMO.setChecked(true);
            ck_stxGen1.setChecked(false);
            ck_stxGen2.setChecked(false);
            ckJ.setChecked(false);
            MyData.push("M" + machineSel + "_useCanOpen", "5");
            isCanOpen=DEMO_BAG;
        });
        ckJ.setOnClickListener(view -> {
            ckDEMO.setChecked(false);
            ck_stxGen1.setChecked(false);
            ck_stxGen2.setChecked(false);
            ckJ.setChecked(true);
            MyData.push("M" + machineSel + "_useCanOpen", "10");
            isCanOpen=JOYSTICKS;

        });

        ck22.setOnClickListener(view -> {
            ck22.setChecked(!ck22.isChecked());
            if (ck22.isChecked()) {
                ck22.setChecked(false);
                MyData.push("M" + machineSel + "Extra_Heading", "0");
                DataSaved.Extra_Heading = 0;
            } else if (!ck22.isChecked()) {
                ck22.setChecked(true);
                MyData.push("M" + machineSel + "Extra_Heading", "1");
                DataSaved.Extra_Heading = 1;
            }
        });
        ckDO.setOnClickListener(view -> {
            ckDO.setChecked(!ckDO.isChecked());
            if (ckDO.isChecked()) {
                ckDO.setChecked(false);
                MyData.push("M" + machineSel + "_enOUT", "0");
                DataSaved.enOUT = 0;
            } else if (!ckDO.isChecked()) {
                ckDO.setChecked(true);
                MyData.push("M" + machineSel + "_enOUT", "1");
                DataSaved.enOUT = 1;
            }
        });
        ckUpper.setOnClickListener(view -> {
            ckUpper.setChecked(!ckUpper.isChecked());
            if (ckUpper.isChecked()) {
                ckUpper.setChecked(false);
                MyData.push("UpperBar_Visible", "0");

            } else if (!ckUpper.isChecked()) {
                ckUpper.setChecked(true);
                MyData.push("UpperBar_Visible", "1");

            }
        });
        ckSchermo.setOnClickListener(view -> {
            ckSchermo.setChecked(!ckSchermo.isChecked());
            if (ckSchermo.isChecked()) {
                ckSchermo.setChecked(false);
                MyData.push("ckSchermo", "0");

            } else if (!ckSchermo.isChecked()) {
                ckSchermo.setChecked(true);
                MyData.push("ckSchermo", "1");

            }
        });
        ckMach.setOnClickListener(view -> {
            ckMach.setChecked(!ckMach.isChecked());
            if (ckMach.isChecked()) {
                ckMach.setChecked(false);
                MyData.push("drwaMachieSchema", "0");
                DataSaved.drwaMachieSchema = 0;

            } else if (!ckMach.isChecked()) {
                ckMach.setChecked(true);
                MyData.push("drwaMachieSchema", "1");
                DataSaved.drwaMachieSchema = 1;

            }
        });
        ckUHF.setOnClickListener(view -> {
            ckUHF.setChecked(!ckUHF.isChecked());
            if (ckUHF.isChecked()) {
                ckUHF.setChecked(false);
                MyData.push("M" + machineSel + "useQuickSwitch", "0");
            } else if (!ckUHF.isChecked()) {
                ckUHF.setChecked(true);
                MyData.push("M" + machineSel + "useQuickSwitch", "1");
            }
        });

        mchName.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(mchName);
            }
        });
        tvSwing.setOnClickListener(view -> {
            if (!dialogSwingBoom.dialog.isShowing()) {
                dialogSwingBoom.show();
            }
        });
        toExtraSensor.setOnClickListener(view -> {
            if (!dialogWheelSteer.dialog.isShowing()) {
                dialogWheelSteer.show();
            }
        });
        tvFrame.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, FrameCalib.class));
            finish();
        });
        drillEnc.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, DrillEncoder.class));
            finish();
        });
        tvBoom1.setOnClickListener(view -> {

            if (DataSaved.isWL == WHEELLOADER || DataSaved.isWL == EXCAVATOR || DataSaved.isWL == DRILL) {
                en_dis(false);
                startActivity(new Intent(this, Boom1Calib.class));
                finish();
            } else {
                if (DataSaved.L_Boom1 == -1) {
                    MyData.push("M" + machineSel + "_LengthBoom1", "0");
                    DataSaved.L_Boom1 = 0;
                } else {
                    MyData.push("M" + machineSel + "_LengthBoom1", "-1");
                    DataSaved.L_Boom1 = -1;
                }

            }
        });
        tvBoom2.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, Boom2Calib.class));
            finish();
        });
        techInfo.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(techInfo);
            }
        });
        tvStick.setOnClickListener(view -> {
            if(DataSaved.isWL==DRILL){
                en_dis(false);
                startActivity(new Intent(this, MastLinkCalib.class));
                finish();
            }else {
                en_dis(false);
                startActivity(new Intent(this, StickCalib.class));
                finish();
            }
        });
        tvMast.setOnClickListener(view -> {
            if(DataSaved.isWL==DRILL){
                en_dis(false);
                startActivity(new Intent(this, ToolSensor.class));
                finish();
            }
        });
        tvLink.setOnClickListener(view -> {
            if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER ) {
                en_dis(false);
                startActivity(new Intent(this, LinkageCalib.class));
                finish();
            } else if(DataSaved.isWL==DRILL){
                en_dis(false);
                startActivity(new Intent(this, DrillToolCalib.class));
                finish();
            }else {
                en_dis(false);
                startActivity(new Intent(this, Tilt_Blade.class));
                finish();
            }
        });
        tvTilt.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, TiltCalib.class));
            finish();
        });
        tvXYZ.setOnClickListener(view -> {

            if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER || DataSaved.isWL == DRILL) {
                en_dis(false);
                startActivity(new Intent(this, XYZ_Calib.class));
                finish();
            } else if (DataSaved.isWL == DOZER || DataSaved.isWL == DOZER_SIX || DataSaved.isWL == GRADER) {
                en_dis(false);
                startActivity(new Intent(this, Nuova_Blade_Calib.class));
                finish();
            }


        });
        menu_1.setOnClickListener(view -> {
            menu1_visible = !menu1_visible;
            menu2_visible = false;
            menu3_visible = false;
            updateCK();
        });
        menu_2.setOnClickListener(view -> {
            menu2_visible = !menu2_visible;
            menu1_visible = false;
            menu3_visible = false;
            updateCK();
        });
        menu_3.setOnClickListener(view -> {
            menu3_visible = !menu3_visible;
            menu2_visible = false;
            menu1_visible = false;
            updateCK();
        });
        linear_1.setOnClickListener(view -> {
            menu_1.callOnClick();
        });
        linear_2.setOnClickListener(view -> {
            menu_2.callOnClick();
        });
        linear_3.setOnClickListener(view -> {
            menu_3.callOnClick();
        });
        back.setOnClickListener(view -> {
            saveName();
            try {
                MyData.push("techInfo", techInfo.getText().toString());
                if (MyData.get_Int("ckSchermo") == 1) {
                    MyDeviceManager.showBar(Nuova_Machine_Settings.this);
                } else {
                    MyDeviceManager.hideBar(Nuova_Machine_Settings.this);
                }
            } catch (Exception ignored) {

            }
            if (DataSaved.isWL == WHEELLOADER) {
                for (int i = 1; i <= 20; i++) {
                    MyData.push("M" + machineSel + "_Tilt_MountPos" + i, "0");
                }
                DataSaved.lrTilt = 0;
            }
            en_dis(false);
            startActivity(new Intent(this, ExcavatorChooserActivity.class));
            finish();
        });
        exca.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(Nuova_Machine_Settings.this);
            builder.setTitle(getString(R.string.change_machine));
            builder.setMessage(getString(R.string.procedi));
            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton("YES", (dialog, which) -> {
                MyData.push("M" + machineSel + "_isWL", "0");
                DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
                mode = DataSaved.isWL;
                updateCK();
            });
            // Aggiungi il pulsante "No"
            builder.setNegativeButton("NO", (dialog, which) -> {
                //do nothing
            });
            // Mostra il dialog
            builder.show();

        });
        wheel.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(Nuova_Machine_Settings.this);
            builder.setTitle(getString(R.string.change_machine));
            builder.setMessage(getString(R.string.procedi));
            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton("YES", (dialog, which) -> {
                MyData.push("M" + machineSel + "_isWL", "1");
                DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
                mode = DataSaved.isWL;
                updateCK();
            });
            // Aggiungi il pulsante "No"
            builder.setNegativeButton("NO", (dialog, which) -> {
                //do nothing
            });
            // Mostra il dialog
            builder.show();

        });
        dozer.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(Nuova_Machine_Settings.this);
            builder.setTitle(getString(R.string.change_machine));
            builder.setMessage(getString(R.string.procedi));
            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton("YES", (dialog, which) -> {
                MyData.push("M" + machineSel + "_isWL", "2");
                DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
                mode = DataSaved.isWL;
                updateCK();
            });
            // Aggiungi il pulsante "No"
            builder.setNegativeButton("NO", (dialog, which) -> {
                //do nothing
            });
            // Mostra il dialog
            builder.show();

        });
        grader.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(Nuova_Machine_Settings.this);
            builder.setTitle(getString(R.string.change_machine));
            builder.setMessage(getString(R.string.procedi));
            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton("YES", (dialog, which) -> {
                MyData.push("M" + machineSel + "_isWL", "4");
                DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
                mode = DataSaved.isWL;
                updateCK();
            });
            // Aggiungi il pulsante "No"
            builder.setNegativeButton("NO", (dialog, which) -> {
                //do nothing
            });
            // Mostra il dialog
            builder.show();

        });
        drill.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(Nuova_Machine_Settings.this);
            builder.setTitle(getString(R.string.change_machine));
            builder.setMessage(getString(R.string.procedi));
            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton("YES", (dialog, which) -> {
                MyData.push("M" + machineSel + "_isWL", "10");
                DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
                mode = DataSaved.isWL;
                updateCK();
            });
            // Aggiungi il pulsante "No"
            builder.setNegativeButton("NO", (dialog, which) -> {
                //do nothing
            });
            // Mostra il dialog
            builder.show();

        });
        toCanopen.setOnClickListener(view -> {
            if(isTech2){
                startActivity(new Intent(this, CanOpenTSM.class));
                finish();
            }else {
                if (!dialogPassword.dialog.isShowing()) {
                    dialogPassword.show(-1);
                }
            }
        });
        toDamping.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, DampingActivity.class));
            finish();
        });
    }

    public void updateUI() {
        if(DataSaved.isWL==DRILL){
            lay_drilmode.setVisibility(View.VISIBLE);
            lay_ant_orient.setVisibility(View.VISIBLE);
            lay_antmount.setVisibility(View.VISIBLE);
            if(DataSaved.Drill_Antenna_Mounting.equals(AT_BODY)){
                lay_ant_orient.setVisibility(View.INVISIBLE);
            }else if(DataSaved.Drill_Antenna_Mounting.equals(AT_BOOM)){
                lay_ant_orient.setVisibility(View.VISIBLE);
            }
        }else {
            lay_drilmode.setVisibility(View.GONE);
            lay_ant_orient.setVisibility(View.GONE);
            lay_antmount.setVisibility(View.GONE);
        }
        if (DataSaved.isWL ==EXCAVATOR||DataSaved.isWL==WHEELLOADER||DataSaved.isWL==DRILL) {
            toCanopen.setVisibility(View.VISIBLE);
            toDamping.setVisibility(View.VISIBLE);
        }else {
            toCanopen.setVisibility(View.INVISIBLE);
            toDamping.setVisibility(View.INVISIBLE);
        }
        if (DataSaved.gpsOk && errorCode == 0) {
            status.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            status.setImageTintList(ColorStateList.valueOf(Color.RED));
        }

        switch (mode) {
            case 0:
                //Excavatore
                tvMast.setVisibility(View.GONE);
                drillEnc.setVisibility(View.GONE);
                toExtraSensor.setVisibility(View.GONE);
                tvFrame.setVisibility(View.VISIBLE);
                if (DataSaved.Extra_Heading > 0) {
                    tvSwing.setVisibility(View.VISIBLE);
                    tvSwing.setText("SWING BOOM");
                } else {
                    tvSwing.setVisibility(View.GONE);
                    tvSwing.setText("SWING BOOM");
                }
                tvBoom1.setVisibility(View.VISIBLE);
                tvBoom1.setText("BOOM 1");
                tvBoom2.setVisibility(View.VISIBLE);
                tvStick.setVisibility(View.VISIBLE);
                tvStick.setText("STICK - LASER");
                tvLink.setVisibility(View.VISIBLE);
                tvLink.setText("LINKAGE - BUCKET");
                tvTilt.setVisibility(View.VISIBLE);
                tvXYZ.setVisibility(View.VISIBLE);
                tvBoom1.setBackgroundTintList(getColorStateList(R.color.bg_stonex_blue));
                tvBoom1.setTextColor(getColor(R.color.white));
                break;
            case 1:
                //Wheel
                tvMast.setVisibility(View.GONE);
                drillEnc.setVisibility(View.GONE);
                if (DataSaved.Extra_Heading > 0) {
                    toExtraSensor.setVisibility(View.VISIBLE);
                    tvSwing.setVisibility(View.VISIBLE);
                    tvSwing.setText("STEERING PIVOT");
                } else {
                    toExtraSensor.setVisibility(View.GONE);
                    tvSwing.setVisibility(View.GONE);
                    tvSwing.setText("STEERING PIVOT");
                }
                tvFrame.setVisibility(View.VISIBLE);
                tvBoom1.setVisibility(View.VISIBLE);
                tvBoom2.setVisibility(View.GONE);
                tvStick.setVisibility(View.VISIBLE);
                tvStick.setText("MAIN BOOM");
                tvLink.setVisibility(View.VISIBLE);
                tvTilt.setVisibility(View.GONE);
                tvXYZ.setVisibility(View.VISIBLE);
                if (DataSaved.L_Boom1 == -1d) {
                    tvBoom1.setText("MACHINE DISABLED");
                    tvBoom1.setBackgroundTintList(getColorStateList(R.color._____cancel_text));
                    tvBoom1.setTextColor(getColor(R.color.white));
                } else {
                    tvBoom1.setText("MACHINE ENABLED");
                    tvBoom1.setBackgroundTintList(getColorStateList(R.color.green));
                    tvBoom1.setTextColor(getColor(R.color._____cancel_text));
                }
                break;
            case 2:
            case 3:
                //Dozer
                tvMast.setVisibility(View.GONE);
                drillEnc.setVisibility(View.GONE);
                toExtraSensor.setVisibility(View.GONE);
                tvFrame.setVisibility(View.GONE);
                tvSwing.setVisibility(View.GONE);
                tvBoom1.setVisibility(View.VISIBLE);
                tvBoom2.setVisibility(View.GONE);
                tvStick.setVisibility(View.GONE);
                tvLink.setVisibility(View.VISIBLE);
                tvTilt.setVisibility(View.GONE);
                tvXYZ.setVisibility(View.VISIBLE);
                tvLink.setText("BLADE SENSOR");
                if (DataSaved.L_Boom1 == -1d) {
                    tvBoom1.setText("MACHINE DISABLED");
                    tvBoom1.setBackgroundTintList(getColorStateList(R.color._____cancel_text));
                    tvBoom1.setTextColor(getColor(R.color.white));
                } else {
                    tvBoom1.setText("MACHINE ENABLED");
                    tvBoom1.setBackgroundTintList(getColorStateList(R.color.green));
                    tvBoom1.setTextColor(getColor(R.color._____cancel_text));
                }
                break;
            case 4:
                //Grader
                tvMast.setVisibility(View.GONE);
                drillEnc.setVisibility(View.GONE);
                toExtraSensor.setVisibility(View.GONE);
                tvFrame.setVisibility(View.GONE);
                tvSwing.setVisibility(View.GONE);
                tvBoom1.setVisibility(View.VISIBLE);
                tvBoom2.setVisibility(View.GONE);
                tvStick.setVisibility(View.GONE);
                tvLink.setVisibility(View.VISIBLE);
                tvTilt.setVisibility(View.GONE);
                tvXYZ.setVisibility(View.VISIBLE);
                tvLink.setText("BLADE SENSOR");
                if (DataSaved.L_Boom1 == -1d) {
                    tvBoom1.setText("MACHINE DISABLED");
                    tvBoom1.setBackgroundTintList(getColorStateList(R.color._____cancel_text));
                    tvBoom1.setTextColor(getColor(R.color.white));
                } else {
                    tvBoom1.setText("MACHINE ENABLED");
                    tvBoom1.setBackgroundTintList(getColorStateList(R.color.green));
                    tvBoom1.setTextColor(getColor(R.color._____cancel_text));
                }
                break;
            case 10:
                //DRILL
                if(DataSaved.Drill_Antenna_Mounting.equals(AT_BODY)) {
                    tvMast.setVisibility(View.VISIBLE);
                    drillEnc.setVisibility(View.VISIBLE);
                    tvFrame.setVisibility(View.VISIBLE);
                    toExtraSensor.setVisibility(View.GONE);
                    if (DataSaved.Extra_Heading > 0) {
                        tvSwing.setVisibility(View.VISIBLE);
                        tvSwing.setText("SWING BOOM");
                    } else {
                        tvSwing.setVisibility(View.GONE);
                        tvSwing.setText("SWING BOOM");
                    }
                    tvBoom1.setVisibility(View.VISIBLE);
                    tvBoom1.setText("BOOM 1");
                    tvBoom2.setVisibility(View.VISIBLE);
                    tvStick.setVisibility(View.VISIBLE);
                    tvStick.setText("MAST LINK");

                    tvLink.setVisibility(View.VISIBLE);
                    tvLink.setText("TOOL ΔX ΔY ΔZ");
                    tvTilt.setVisibility(View.GONE);
                    tvXYZ.setVisibility(View.VISIBLE);
                    tvBoom1.setBackgroundTintList(getColorStateList(R.color.bg_stonex_blue));
                    tvBoom1.setTextColor(getColor(R.color.white));
                }else {
                    tvMast.setVisibility(View.VISIBLE);
                    drillEnc.setVisibility(View.VISIBLE);
                    tvFrame.setVisibility(View.GONE);
                    toExtraSensor.setVisibility(View.GONE);
                    if (DataSaved.Extra_Heading > 0) {
                        tvSwing.setVisibility(View.GONE);
                        tvSwing.setText("SWING BOOM");
                    } else {
                        tvSwing.setVisibility(View.GONE);
                        tvSwing.setText("SWING BOOM");
                    }
                    tvBoom1.setVisibility(View.GONE);
                    tvBoom1.setText("BOOM 1");
                    tvBoom2.setVisibility(View.GONE);
                    tvStick.setVisibility(View.GONE);
                    tvStick.setText("MAST LINK");

                    tvLink.setVisibility(View.VISIBLE);
                    tvLink.setText("TOOL ΔX ΔY ΔZ");
                    tvTilt.setVisibility(View.GONE);
                    tvXYZ.setVisibility(View.GONE);
                    tvBoom1.setBackgroundTintList(getColorStateList(R.color.bg_stonex_blue));
                    tvBoom1.setTextColor(getColor(R.color.white));
                }
                break;


        }
    }

    private void updateCK() {
        if (menu1_visible) {
            menu_1.setImageResource(R.drawable.keyboard_arrow_down_96);
            constraintLayout.setVisibility(View.VISIBLE);
        } else {
            constraintLayout.setVisibility(View.GONE);
            menu_1.setImageResource(R.drawable.key_arrow_right);
        }
        if (menu2_visible) {
            menu_2.setImageResource(R.drawable.keyboard_arrow_down_96);
            constraintLayout_2.setVisibility(View.VISIBLE);
        } else {
            constraintLayout_2.setVisibility(View.GONE);
            menu_2.setImageResource(R.drawable.key_arrow_right);
        }
        if (menu3_visible) {
            menu_3.setImageResource(R.drawable.keyboard_arrow_down_96);
            constraintLayout_3.setVisibility(View.VISIBLE);
        } else {
            constraintLayout_3.setVisibility(View.GONE);
            menu_3.setImageResource(R.drawable.key_arrow_right);
        }
        ViewGroup.LayoutParams excaP = exca.getLayoutParams();
        ViewGroup.LayoutParams wheelP = wheel.getLayoutParams();
        ViewGroup.LayoutParams dozerP = dozer.getLayoutParams();
        ViewGroup.LayoutParams graderP = grader.getLayoutParams();
        ViewGroup.LayoutParams drillP = drill.getLayoutParams();

        switch (DataSaved.isWL) {
            case 0:
                excaP.width = bigg;
                excaP.height = bigg;
                wheelP.width = small;
                wheelP.height = small;
                graderP.width = small;
                graderP.height = small;
                dozerP.width = small;
                dozerP.height = small;
                drillP.width = small;
                drillP.height = small;
                exca.setLayoutParams(excaP);
                wheel.setLayoutParams(wheelP);
                dozer.setLayoutParams(dozerP);
                grader.setLayoutParams(graderP);
                drill.setLayoutParams(drillP);
                exca.setAlpha(1.0f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(0.2f);
                drill.setAlpha(0.2f);
                exca.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                wheel.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                dozer.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                grader.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                drill.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                break;
            case 1:
                excaP.width = small;
                excaP.height = small;
                wheelP.width = bigg;
                wheelP.height = bigg;
                graderP.width = small;
                graderP.height = small;
                dozerP.width = small;
                dozerP.height = small;
                drillP.width = small;
                drillP.height = small;
                exca.setLayoutParams(excaP);
                wheel.setLayoutParams(wheelP);
                dozer.setLayoutParams(dozerP);
                grader.setLayoutParams(graderP);
                drill.setLayoutParams(drillP);
                exca.setAlpha(0.2f);
                wheel.setAlpha(1f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(0.2f);
                drill.setAlpha(0.2f);
                wheel.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                exca.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                dozer.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                grader.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                drill.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                break;
            case 2:
            case 3:
                excaP.width = small;
                excaP.height = small;
                wheelP.width = small;
                wheelP.height = small;
                graderP.width = small;
                graderP.height = small;
                dozerP.width = bigg;
                dozerP.height = bigg;
                drillP.width = small;
                drillP.height = small;
                exca.setLayoutParams(excaP);
                wheel.setLayoutParams(wheelP);
                dozer.setLayoutParams(dozerP);
                grader.setLayoutParams(graderP);
                drill.setLayoutParams(drillP);
                exca.setAlpha(0.2f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(1f);
                grader.setAlpha(0.2f);
                drill.setAlpha(0.2f);
                dozer.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                wheel.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                exca.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                grader.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                drill.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                break;
            case 4:
                excaP.width = small;
                excaP.height = small;
                wheelP.width = small;
                wheelP.height = small;
                graderP.width = bigg;
                graderP.height = bigg;
                dozerP.width = small;
                dozerP.height = small;
                drillP.width = small;
                drillP.height = small;
                exca.setLayoutParams(excaP);
                wheel.setLayoutParams(wheelP);
                dozer.setLayoutParams(dozerP);
                grader.setLayoutParams(graderP);
                drill.setLayoutParams(drillP);
                exca.setAlpha(0.2f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(0.2f);
                drill.setAlpha(0.2f);
                grader.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                wheel.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                dozer.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                exca.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                drill.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                break;
            case 10:

                excaP.width = small;
                excaP.height = small;
                wheelP.width = small;
                wheelP.height = small;
                graderP.width = small;
                graderP.height = small;
                dozerP.width = small;
                dozerP.height = small;
                drillP.width = bigg;
                drillP.height = bigg;
                exca.setLayoutParams(excaP);
                wheel.setLayoutParams(wheelP);
                dozer.setLayoutParams(dozerP);
                grader.setLayoutParams(graderP);
                drill.setLayoutParams(drillP);
                exca.setAlpha(0.2f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(0.2f);
                drill.setAlpha(1.0f);
                exca.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                wheel.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                dozer.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                grader.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                drill.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                break;
        }

        ckUHF.setChecked(MyData.get_Int("M" + machineSel + "useQuickSwitch") == 1);
        ck22.setChecked(MyData.get_Int("M" + machineSel + "Extra_Heading") != 0);
        ckDO.setChecked(MyData.get_Int("M" + machineSel + "_enOUT") != 0);
        ck_stxGen1.setChecked(MyData.get_Int("M" + machineSel + "_useCanOpen") == 3);//TSM
        ck_stxGen2.setChecked(MyData.get_Int("M" + machineSel + "_useCanOpen") == 1);//G2
        ckDEMO.setChecked(MyData.get_Int("M" + machineSel + "_useCanOpen") == 5);//DEMO Roller Bag
        ckJ.setChecked(MyData.get_Int("M" + machineSel + "_useCanOpen") == 10);
        ckUpper.setChecked(MyData.get_Int("UpperBar_Visible") == 1);
        ckSchermo.setChecked(MyData.get_Int("ckSchermo") == 1);
        ckMach.setChecked(MyData.get_Int("drwaMachieSchema") == 1);
        techInfo.setText(MyData.get_String("techInfo"));
        ckRock.setChecked(MyData.get_Int("M" + machineSel + "Drilling_Mode") == ROCKDRILL_MODE);
        ckJet.setChecked(MyData.get_Int("M" + machineSel + "Drilling_Mode") == JETGROUTING_MODE);
        ckSolar.setChecked(MyData.get_Int("M" + machineSel + "Drilling_Mode") == SOLARFARM_MODE);
        ckBody.setChecked(MyData.get_String("M"+machineSel+"Drill_Antenna_Mounting").endsWith(AT_BODY));
        ckBoom.setChecked(MyData.get_String("M"+machineSel+"Drill_Antenna_Mounting").endsWith(AT_BOOM));
        ckAtLeft.setChecked(MyData.get_String("M"+machineSel+"Drill_Mast_Position").endsWith(MAST_LEFT));
        ckAtFwd.setChecked(MyData.get_String("M"+machineSel+"Drill_Mast_Position").endsWith(MAST_FORWARD));
        ckAtRight.setChecked(MyData.get_String("M"+machineSel+"Drill_Mast_Position").endsWith(MAST_RIGHT));
    }

    private void en_dis(boolean b) {
        back.setEnabled(b);
        tvFrame.setEnabled(b);
        tvBoom1.setEnabled(b);
        tvBoom2.setEnabled(b);
        tvStick.setEnabled(b);
        tvLink.setEnabled(b);
        tvTilt.setEnabled(b);
        tvXYZ.setEnabled(b);
        toDamping.setEnabled(b);
        toCanopen.setEnabled(b);
        menu_3.setEnabled(b);
        exca.setEnabled(b);
        wheel.setEnabled(b);
        dozer.setEnabled(b);
        grader.setEnabled(b);
        drill.setEnabled(b);
    }

    public void saveName() {
        try {
            MyData.push("M" + machineSel + "_Name", mchName.getText().toString().trim().toUpperCase());
        } catch (Exception e) {
            new CustomToast(this, "Missing Name").show_error();
        }
    }


}