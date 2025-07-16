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
import android.view.View;
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
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import packexcalib.exca.DataSaved;
import utils.MyData;

public class Nuova_Machine_Settings extends AppCompatActivity {
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    CheckBox ckCloud, ckDO, ckUHF, ckUpper, ckIMU, ckDEMO;
    CustomQwertyDialog customQwertyDialog;
    ImageView back, exca, wheel, grader, dozer, menu_1, menu_2, saveToFile, readFromFile, status;
    ConstraintLayout constraintLayout, constraintLayout_2;
    TextView tvFrame, tvBoom1, tvBoom2, tvStick, tvLink, tvTilt, tvXYZ;
    EditText mchName;
    int mode, machineSel;
    boolean menu1_visible, menu2_visible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrazione_macchine);
        findView();
        onClick();
        updateCK();
        updateUI();
    }

    private void findView() {
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        customQwertyDialog = new CustomQwertyDialog(this);
        machineSel = MyData.get_Int("MachineSelected");
        mode = MyData.get_Int("M" + machineSel + "_isWL");
        back = findViewById(R.id.btn_1);
        exca = findViewById(R.id.sel_exca);
        wheel = findViewById(R.id.sel_wheel);
        grader = findViewById(R.id.sel_grader);
        dozer = findViewById(R.id.sel_dozer);
        menu_1 = findViewById(R.id.bt_menu1);
        menu_2 = findViewById(R.id.bt_sens_set);
        constraintLayout = findViewById(R.id.constraint_general);
        constraintLayout_2 = findViewById(R.id.constr_2);
        tvFrame = findViewById(R.id.toFrame);
        tvBoom1 = findViewById(R.id.toBoom1);
        tvBoom2 = findViewById(R.id.toBoom2);
        tvStick = findViewById(R.id.toStick);
        tvLink = findViewById(R.id.toDogBone);
        tvTilt = findViewById(R.id.toTilt);
        tvXYZ = findViewById(R.id.toxyz);
        mchName = findViewById(R.id.mch_name);
        ckCloud = findViewById(R.id.ck1);
        ckDO = findViewById(R.id.ck2);
        ckUHF = findViewById(R.id.ck3);
        ckUpper = findViewById(R.id.ck4);
        ckIMU = findViewById(R.id.ck5);
        ckDEMO = findViewById(R.id.ck6);
        saveToFile = findViewById(R.id.img01);
        readFromFile = findViewById(R.id.img11);
        status = findViewById(R.id.img00);
        mchName.setText(MyData.get_String("M" + machineSel + "_Name"));
    }

    private void onClick() {
        status.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });
        saveToFile.setOnClickListener((View v) -> {
            if (MyData.get_String("M" + machineSel + "_Name").equals("")) {
                new CustomToast(Nuova_Machine_Settings.this, "MISSING NAME").show();
            } else {
                if (MyData.get_String("M" + machineSel + "_Name").equals("EXCAVATOR 1") ||
                        MyData.get_String("M" + machineSel + "_Name").equals("EXCAVATOR 2") ||
                        MyData.get_String("M" + machineSel + "_Name").equals("WHEEL LOADER 1") ||
                        MyData.get_String("M" + machineSel + "_Name").equals("DOZER 1")
                ) {
                    new CustomToast(Nuova_Machine_Settings.this, "CHANGE MACHINE NAME BEFORE SAVE").show_alert();
                } else {
                    // Crea un nuovo AlertDialog.Builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(Nuova_Machine_Settings.this);
                    builder.setTitle("SAVE ALL PARAMETERS TO EXTERNAL MEMORY");
                    builder.setMessage("Do You Want to Proceed ?");

                    // Aggiungi il pulsante "Sì"
                    builder.setPositiveButton("YES", (dialog, which) -> {

                        save_nuova();
                        new CustomToast(Nuova_Machine_Settings.this, "SAVED").show();
                    });

                    // Aggiungi il pulsante "No"
                    builder.setNegativeButton("NO", (dialog, which) -> {

                    });
                    builder.setCancelable(true);
                    builder.show();

                }
            }


        });
        readFromFile.setOnClickListener((View v) -> {
            en_dis(false);
            startActivity(new Intent(this, PickMachine.class));
            finish();
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

        ckCloud.setOnClickListener(view -> {
            ckCloud.setChecked(!ckCloud.isChecked());
            if (ckCloud.isChecked()) {
                ckCloud.setChecked(false);
                MyData.push("M" + machineSel + "_is40", "0");
            } else if (!ckCloud.isChecked()) {
                ckCloud.setChecked(true);
                MyData.push("M" + machineSel + "_is40", "1");
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
                MyData.push("digStartUp", "0");

            } else if (!ckUpper.isChecked()) {
                ckUpper.setChecked(true);
                MyData.push("digStartUp", "1");

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
        tvFrame.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, FrameCalib.class));
            finish();
        });
        tvBoom1.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, Boom1Calib.class));
            finish();
        });
        tvBoom2.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, Boom2Calib.class));
            finish();
        });
        tvStick.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, StickCalib.class));
            finish();
        });
        tvLink.setOnClickListener(view -> {
            en_dis(false);
            startActivity(new Intent(this, LinkageCalib.class));
            finish();
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
                startActivity(new Intent(this, XYZ_Calib_Dozer.class));
                finish();
            }


        });
        menu_1.setOnClickListener(view -> {
            menu1_visible = !menu1_visible;
            updateCK();
        });
        menu_2.setOnClickListener(view -> {
            menu2_visible = !menu2_visible;
            updateCK();
        });
        back.setOnClickListener(view -> {
            saveName();
            en_dis(false);
            startActivity(new Intent(this, ExcavatorChooserActivity.class));
            finish();
        });
        exca.setOnClickListener(view -> {
            MyData.push("M" + machineSel + "_isWL", "0");
            DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
            mode = DataSaved.isWL;
            updateCK();
        });
        wheel.setOnClickListener(view -> {
            MyData.push("M" + machineSel + "_isWL", "1");
            DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
            mode = DataSaved.isWL;
            updateCK();
        });
        dozer.setOnClickListener(view -> {
            MyData.push("M" + machineSel + "_isWL", "2");
            DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
            mode = DataSaved.isWL;
            updateCK();
        });
        grader.setOnClickListener(view -> {
            MyData.push("M" + machineSel + "_isWL", "4");
            DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
            mode = DataSaved.isWL;
            updateCK();
        });
    }

    public void updateUI() {
        if (DataSaved.gpsOk && errorCode == 0) {
            status.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            status.setImageTintList(ColorStateList.valueOf(Color.RED));
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
        switch (DataSaved.isWL) {
            case 0:
                exca.setAlpha(1.0f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(0.2f);
                break;
            case 1:
                exca.setAlpha(0.2f);
                wheel.setAlpha(1f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(0.2f);
                break;
            case 2:
            case 3:
                exca.setAlpha(0.2f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(1f);
                grader.setAlpha(0.2f);
                break;
            case 4:
                exca.setAlpha(0.2f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(1f);
                break;
        }

        ckUHF.setChecked(MyData.get_Int("M" + machineSel + "useQuickSwitch") == 1);
        ckDO.setChecked(MyData.get_Int("M" + machineSel + "_enOUT") != 0);
        ckCloud.setChecked(MyData.get_Int("M" + machineSel + "_is40") != 0);
        ckIMU.setChecked(MyData.get_Int("M" + machineSel + "_useCanOpen") == 3);//TSM
        ckDEMO.setChecked(MyData.get_Int("M" + machineSel + "_useCanOpen") == 5);//DEMO Roller Bag
        ckUpper.setChecked(MyData.get_Int("digStartUp") == 1);

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
    }
    public void saveName(){
        try {
            MyData.push("M" + machineSel + "_Name", mchName.getText().toString().trim().toUpperCase());
        } catch (Exception e) {
            new CustomToast(this, "Missing Name").show_error();
        }
    }

    private void save_nuova() {
        String typeCan = MyData.get_String("M" + machineSel + "_useCanOpen");
        String nameM = MyData.get_String("M" + machineSel + "_Name");
        String lrBoom1 = MyData.get_String("M" + machineSel + "_Boom1_MountPos");
        String lengthBoom1 = MyData.get_String("M" + machineSel + "_LengthBoom1");
        String offsetBoom1 = MyData.get_String("M" + machineSel + "_OffsetBoom1");
        String lrBoom2 = MyData.get_String("M" + machineSel + "_Boom2_MountPos");
        String lengthBoom2 = MyData.get_String("M" + machineSel + "_LengthBoom2");
        String offsetBoom2 = MyData.get_String("M" + machineSel + "_OffsetBoom2");
        String lrStick = MyData.get_String("M" + machineSel + "_Stick_MountPos");
        String lengthStick = MyData.get_String("M" + machineSel + "_LengthStick");
        String offsetStick = MyData.get_String("M" + machineSel + "_OffsetStick");
        String lsv = MyData.get_String("M" + machineSel + "_LaserVStick");
        String lsh = MyData.get_String("M" + machineSel + "_LaserHStick");
        String lrBucket = MyData.get_String("M" + machineSel + "_Bucket_MountPos");
        String L1 = MyData.get_String("M" + machineSel + "_LengthL1");
        String L2 = MyData.get_String("M" + machineSel + "_LengthL2");
        String L3 = MyData.get_String("M" + machineSel + "_LengthL3");
        String offsetDogBone = MyData.get_String("M" + machineSel + "_OffsetDB");
        String lrFrame = MyData.get_String("M" + machineSel + "_Frame_MountPos");
        String lengthPitch = MyData.get_String("M" + machineSel + "_LengthPitch");
        String lengthRoll = MyData.get_String("M" + machineSel + "_LengthRoll");
        String offsetPitch = MyData.get_String("M" + machineSel + "_OffsetFrameY");
        String offsetRoll = MyData.get_String("M" + machineSel + "_OffsetFrameX");
        String gpsdeltaX = MyData.get_String("M" + machineSel + "_OffsetGPSX");
        String gpsdeltaY = MyData.get_String("M" + machineSel + "_OffsetGPSY");
        String gpsdeltaZ = MyData.get_String("M" + machineSel + "_OffsetGPSZ");
        String gps2Dev = MyData.get_String("M" + machineSel + "_OffsetGPS2");
        String gpsType = MyData.get_String("M" + machineSel + "_sc600");
        String comPort = MyData.get_String("M" + machineSel + "_comPort");
        String isWL = MyData.get_String("M" + machineSel + "_isWL");
        String reqSpeed = MyData.get_String("M" + machineSel + "reqSpeed");
        String radioMode = MyData.get_String("M" + machineSel + "radioMode");
        String licenza = MyData.get_String("licenza");
        String quick = MyData.get_String("M" + machineSel + "_hasQuick");
        String lang = MyData.get_String("language");
        String widR = MyData.get_String("M" + machineSel + "_Bucket_" + "0" + "_Width_R");
        String widL = MyData.get_String("M" + machineSel + "_Bucket_" + "0" + "_Width_L");
        String palo = MyData.get_String("M" + machineSel + "_Bucket_" + "0" + "_Palo");
        String lama = MyData.get_String("M" + machineSel + "_Bucket_" + "0" + "_Lama");
        String between = MyData.get_String("M" + machineSel + "_Bucket_" + "0" + "_Between");
        String g1g2 = MyData.get_String("M" + machineSel + "_distG1_G2");

        String[] tiltMountPos = new String[10];
        String[] benneTilt_Leng = new String[10];
        String[] benneTilt_Offset = new String[10];


        for (int i = 0; i < tiltMountPos.length; i++) {
            tiltMountPos[i] = MyData.get_String("M" + machineSel + "_Tilt_MountPos" + i);
            benneTilt_Leng[i] = MyData.get_String("M" + machineSel + "_Tilt_Length" + i);
            benneTilt_Offset[i] = MyData.get_String("M" + machineSel + "_Tilt_Offset" + i);
        }

        String path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Machine " + machineSel + "/Config";
        String fileName = nameM + ".csv";
        File f = new File(path, fileName);
        CSVWriter writer;
        String pathL = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines";
        String fileNameL = "Lic.csv";
        File file1 = new File(pathL, fileNameL);
        CSVWriter writer1;

        try {
            writer = new CSVWriter(new FileWriter(f));

            String[] can = {"CAN TYPE", typeCan};
            String[] boom1 = {"BOOM   1", lrBoom1, lengthBoom1, offsetBoom1};
            String[] boom2 = {"BOOM   2", lrBoom2, lengthBoom2, offsetBoom2};
            String[] stick = {"STICK   ", lrStick, lengthStick, offsetStick, lsv, lsh};
            String[] linkage = {"LINKAGE ", lrBucket, L1, L2, L3, offsetDogBone};
            String[] frame = {"FRAME   ", lrFrame, lengthPitch, lengthRoll, offsetPitch, offsetRoll};
            String[] gps1x = {"GPS1 ∆X ", gpsdeltaX};
            String[] gps1y = {"GPS1 ∆Y ", gpsdeltaY};
            String[] gps1z = {"GPS1 ∆Z ", gpsdeltaZ};
            String[] gps2 = {"GPS2∆HDT", gps2Dev};
            String[] gpsT = {"GPS TYPE", gpsType};
            String[] com = {"GPS COM ", comPort};
            String[] wl = {"MCH TYPE", isWL};
            String[] speed = {"NMEARATE", reqSpeed};
            String[] radio = {"DATALINK", radioMode};
            String[] coupler = {"QUICK CO", quick};
            String[] langua = {"LANGUAGE", lang};
            String[] wir = {"BLADE_WR", widR};
            String[] wil = {"BLADE_WL", widL};
            String[] paloL = {"MAST___H", palo};
            String[] lamaL = {"BLADE__H", lama};
            String[] betweenl = {"P1>>P2", between};
            String[]g1g2L={"G1>>G2",g1g2};


            writer.writeNext(can);
            writer.writeNext(boom1);
            writer.writeNext(boom2);
            writer.writeNext(stick);
            writer.writeNext(linkage);
            writer.writeNext(frame);
            writer.writeNext(tiltMountPos);
            writer.writeNext(benneTilt_Leng);
            writer.writeNext(benneTilt_Offset);
            writer.writeNext(gps1x);
            writer.writeNext(gps1y);
            writer.writeNext(gps1z);
            writer.writeNext(gps2);
            writer.writeNext(gpsT);
            writer.writeNext(com);
            writer.writeNext(wl);
            writer.writeNext(speed);
            writer.writeNext(radio);
            writer.writeNext(coupler);
            writer.writeNext(langua);
            writer.writeNext(wir);
            writer.writeNext(wil);
            writer.writeNext(paloL);
            writer.writeNext(lamaL);
            writer.writeNext(betweenl);
            writer.writeNext(g1g2L);

            writer.close();
        } catch (Exception ignored) {
        }
        try {
            String s = "";
            if (Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
                Apollo2 apollo2 = Apollo2.getInstance(Nuova_Machine_Settings.this);
                s = apollo2.getDeviceSN();
            } else {
                ApolloPro apolloPro = ApolloPro.getInstance(Nuova_Machine_Settings.this);
                s = apolloPro.getDeviceSN();
            }
            writer1 = new CSVWriter(new FileWriter(file1));
            String[] lic = {s, licenza};
            writer1.writeNext(lic);
            writer1.close();
        } catch (Exception ignored) {

        }
    }
}