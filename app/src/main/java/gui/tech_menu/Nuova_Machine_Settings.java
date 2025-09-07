package gui.tech_menu;

import static gui.MyApp.errorCode;
import static gui.MyApp.folderPath;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.example.stx_dig.R;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;

import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_CanBaud;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.gps.Nuovo_Gps;
import packexcalib.exca.DataSaved;
import serial.SerialPortManager;
import utils.MyData;
import utils.MyDeviceManager;

public class Nuova_Machine_Settings extends AppCompatActivity {
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    CheckBox  ckDO, ckUHF, ckUpper, ckIMU, ckDEMO,ckSchermo,ckMach,ck22;
    CustomQwertyDialog customQwertyDialog;
    ImageView back, exca, wheel, grader, dozer, menu_1, menu_2, saveToFile, readFromFile, status,bt_canopen;
    ConstraintLayout constraintLayout, constraintLayout_2,constraintLayout_3;
    TextView tvSwing,tvFrame, tvBoom1, tvBoom2, tvStick, tvLink, tvTilt, tvXYZ,toCanopen,toDamping,can1bd,can2bd;
    EditText mchName,techInfo;
    int mode, machineSel;
    public static boolean menu1_visible, menu2_visible,menu3_visible;
    DialogPassword dialogPassword;
    Dialog_CanBaud dialogCanBaud;
    Dialog_Swing_Boom dialogSwingBoom;
    int small,bigg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrazione_macchine);
        if(Build.BRAND.equals("APOLLO2_7")){
            bigg=80;
            small=70;
        }else {
            bigg=110;
            small=90;
        }
        findView();
        onClick();
        updateCK();
        updateUI();
    }

    private void findView() {
        dialogCanBaud=new Dialog_CanBaud(this);
        dialogPassword = new DialogPassword(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        customQwertyDialog = new CustomQwertyDialog(this,null);
        dialogSwingBoom=new Dialog_Swing_Boom(this);
        machineSel = MyData.get_Int("MachineSelected");
        mode = MyData.get_Int("M" + machineSel + "_isWL");
        back = findViewById(R.id.btn_1);
        exca = findViewById(R.id.sel_exca);
        wheel = findViewById(R.id.sel_wheel);
        grader = findViewById(R.id.sel_grader);
        dozer = findViewById(R.id.sel_dozer);
        menu_1 = findViewById(R.id.bt_menu1);
        menu_2 = findViewById(R.id.bt_sens_set);
        techInfo = findViewById(R.id.techInfo);
        constraintLayout = findViewById(R.id.constraint_general);
        constraintLayout_2 = findViewById(R.id.constr_2);
        constraintLayout_3 = findViewById(R.id.constr_3);
        tvSwing=findViewById(R.id.tvSwing);
        tvFrame = findViewById(R.id.toFrame);
        tvBoom1 = findViewById(R.id.toBoom1);
        tvBoom2 = findViewById(R.id.toBoom2);
        tvStick = findViewById(R.id.toStick);
        tvLink = findViewById(R.id.toDogBone);
        tvTilt = findViewById(R.id.toTilt);
        tvXYZ = findViewById(R.id.toxyz);
        mchName = findViewById(R.id.mch_name);
        ckSchermo=findViewById(R.id.ckSchermo);
        ckMach=findViewById(R.id.ckMach);
        ckDO = findViewById(R.id.ck2);
        ck22=findViewById(R.id.ck22);
        ckUHF = findViewById(R.id.ck3);
        ckUpper = findViewById(R.id.ck4);
        ckIMU = findViewById(R.id.ck5);
        ckDEMO = findViewById(R.id.ck6);
        saveToFile = findViewById(R.id.img01);
        readFromFile = findViewById(R.id.img11);
        status = findViewById(R.id.img00);
        bt_canopen=findViewById(R.id.bt_canopen);
        toCanopen=findViewById(R.id.toCanOpen);
        toDamping=findViewById(R.id.toDamping);
        can1bd=findViewById(R.id.toCan1);
        can2bd=findViewById(R.id.toCan2);
        mchName.setText(MyData.get_String("M" + machineSel + "_Name"));



    }

    private void onClick() {
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
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });


        ckIMU.setOnClickListener(view -> {
            ckDEMO.setChecked(false);
            ckIMU.setChecked(true);
            MyData.push("M" + machineSel + "_useCanOpen", "3");

        });
        ckDEMO.setOnClickListener(view -> {
            ckDEMO.setChecked(true);
            ckIMU.setChecked(false);
            MyData.push("M" + machineSel + "_useCanOpen", "5");
        });

        ck22.setOnClickListener(view -> {
            ck22.setChecked(!ck22.isChecked());
            if (ck22.isChecked()) {
                ck22.setChecked(false);
                MyData.push("M" + machineSel + "Extra_Heading", "0");
                DataSaved.Extra_Heading=0;
            } else if (!ck22.isChecked()) {
                ck22.setChecked(true);
                MyData.push("M" + machineSel + "Extra_Heading", "1");
                DataSaved.Extra_Heading=1;
            }
        });
        ckDO.setOnClickListener(view -> {
            ckDO.setChecked(!ckDO.isChecked());
            if (ckDO.isChecked()) {
                ckDO.setChecked(false);
                MyData.push("M" + machineSel + "_enOUT", "0");
            } else if (!ckDO.isChecked()) {
                ckDO.setChecked(true);
                MyData.push("M" + machineSel + "_enOUT", "1");
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
                DataSaved.drwaMachieSchema=0;

            } else if (!ckMach.isChecked()) {
                ckMach.setChecked(true);
                MyData.push("drwaMachieSchema", "1");
                DataSaved.drwaMachieSchema=1;

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
            if(!dialogSwingBoom.dialog.isShowing()){
                dialogSwingBoom.show();
            }
        });
        tvFrame.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, FrameCalib.class));
            finish();
        });
        tvBoom1.setOnClickListener(view -> {

            if (DataSaved.isWL < 1) {
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
            en_dis(false);
            startActivity(new Intent(this, StickCalib.class));
            finish();
        });
        tvLink.setOnClickListener(view -> {
            if (DataSaved.isWL < 2) {
                en_dis(false);
                startActivity(new Intent(this, LinkageCalib.class));
                finish();
            } else {
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

            if (DataSaved.isWL < 2) {
                en_dis(false);
                startActivity(new Intent(this, XYZ_Calib.class));
                finish();
            } else {
                en_dis(false);
                startActivity(new Intent(this, Nuova_Blade_Calib.class));
                finish();
            }


        });
        menu_1.setOnClickListener(view -> {
            menu1_visible = !menu1_visible;
            menu2_visible=false;
            menu3_visible=false;
            updateCK();
        });
        menu_2.setOnClickListener(view -> {
            menu2_visible = !menu2_visible;
            menu1_visible=false;
            menu3_visible=false;
            updateCK();
        });
        bt_canopen.setOnClickListener(view -> {
            menu3_visible = !menu3_visible;
            menu2_visible=false;
            menu1_visible=false;
            updateCK();
        });
        back.setOnClickListener(view -> {
            saveName();
            try {
                MyData.push("techInfo", techInfo.getText().toString());
                if(MyData.get_Int("ckSchermo") == 1){
                    MyDeviceManager.showBar(Nuova_Machine_Settings.this);
                }else {
                    MyDeviceManager.hideBar(Nuova_Machine_Settings.this);
                }
            }catch(Exception ignored){

            }
            if(DataSaved.isWL==1){
                for (int i=1;i<=20;i++){
                    MyData.push("M"+machineSel+"_Tilt_MountPos"+i,"0");
                }
                DataSaved.lrTilt=0;
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
        toCanopen.setOnClickListener(view -> {
            if (!dialogPassword.dialog.isShowing()) {
                dialogPassword.show(-1);
            }
        });
        toDamping.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, DampingActivity.class));
            finish();
        });
    }

    public void updateUI() {
        if (DataSaved.gpsOk && errorCode == 0) {
            status.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            status.setImageTintList(ColorStateList.valueOf(Color.RED));
        }

        switch (mode) {
            case 0:
                //Excavatore
                tvFrame.setVisibility(View.VISIBLE);
                tvSwing.setVisibility(View.VISIBLE);
                tvSwing.setText("SWING BOOM");
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
                tvFrame.setVisibility(View.VISIBLE);
                tvBoom1.setVisibility(View.VISIBLE);
                tvSwing.setVisibility(View.VISIBLE);
                tvSwing.setText("STEERING PIVOT");
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
            bt_canopen.setImageResource(R.drawable.keyboard_arrow_down_96);
            constraintLayout_3.setVisibility(View.VISIBLE);
        } else {
            constraintLayout_3.setVisibility(View.GONE);
            bt_canopen.setImageResource(R.drawable.key_arrow_right);
        }
        ViewGroup.LayoutParams excaP = exca.getLayoutParams();
        ViewGroup.LayoutParams wheelP = wheel.getLayoutParams();
        ViewGroup.LayoutParams dozerP=dozer.getLayoutParams();
        ViewGroup.LayoutParams graderP=grader.getLayoutParams();

        switch (DataSaved.isWL) {
            case 0:
                excaP.width = bigg;
                excaP.height = bigg;
                wheelP.width=small;
                wheelP.height=small;
                graderP.width=small;
                graderP.height=small;
                dozerP.width=small;
                dozerP.height=small;
                exca.setLayoutParams(excaP);
                wheel.setLayoutParams(wheelP);
                dozer.setLayoutParams(dozerP);
                grader.setLayoutParams(graderP);
                exca.setAlpha(1.0f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(0.2f);
                exca.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                wheel.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                dozer.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                grader.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                break;
            case 1:
                excaP.width = small;
                excaP.height = small;
                wheelP.width=bigg;
                wheelP.height=bigg;
                graderP.width=small;
                graderP.height=small;
                dozerP.width=small;
                dozerP.height=small;
                exca.setLayoutParams(excaP);
                wheel.setLayoutParams(wheelP);
                dozer.setLayoutParams(dozerP);
                grader.setLayoutParams(graderP);
                exca.setAlpha(0.2f);
                wheel.setAlpha(1f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(0.2f);
                wheel.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                exca.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                dozer.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                grader.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));

                break;
            case 2:
            case 3:
                excaP.width = small;
                excaP.height = small;
                wheelP.width=small;
                wheelP.height=small;
                graderP.width=small;
                graderP.height=small;
                dozerP.width=bigg;
                dozerP.height=bigg;
                exca.setLayoutParams(excaP);
                wheel.setLayoutParams(wheelP);
                dozer.setLayoutParams(dozerP);
                grader.setLayoutParams(graderP);
                exca.setAlpha(0.2f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(1f);
                grader.setAlpha(0.2f);
                dozer.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                wheel.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                exca.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                grader.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                break;
            case 4:
                excaP.width = small;
                excaP.height = small;
                wheelP.width=small;
                wheelP.height=small;
                graderP.width=bigg;
                graderP.height=bigg;
                dozerP.width=small;
                dozerP.height=small;
                exca.setLayoutParams(excaP);
                wheel.setLayoutParams(wheelP);
                dozer.setLayoutParams(dozerP);
                grader.setLayoutParams(graderP);
                exca.setAlpha(0.2f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(1f);
                grader.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                wheel.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                dozer.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                exca.setBackground(getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                break;
        }

        ckUHF.setChecked(MyData.get_Int("M" + machineSel + "useQuickSwitch") == 1);
        ck22.setChecked(MyData.get_Int("M"+machineSel+"Extra_Heading")!=0);
        ckDO.setChecked(MyData.get_Int("M" + machineSel + "_enOUT") != 0);
        ckIMU.setChecked(MyData.get_Int("M" + machineSel + "_useCanOpen") == 3);//TSM
        ckDEMO.setChecked(MyData.get_Int("M" + machineSel + "_useCanOpen") == 5);//DEMO Roller Bag
        ckUpper.setChecked(MyData.get_Int("UpperBar_Visible") == 1);
        ckSchermo.setChecked(MyData.get_Int("ckSchermo") == 1);
        ckMach.setChecked(MyData.get_Int("drwaMachieSchema")==1);
        techInfo.setText(MyData.get_String("techInfo"));

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
        bt_canopen.setEnabled(b);
    }

    public void saveName() {
        try {
            MyData.push("M" + machineSel + "_Name", mchName.getText().toString().trim().toUpperCase());
        } catch (Exception e) {
            new CustomToast(this, "Missing Name").show_error();
        }
    }


}