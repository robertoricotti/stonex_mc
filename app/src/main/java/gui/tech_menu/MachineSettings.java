package gui.tech_menu;

import static gui.MyApp.folderPath;
import static gui.MyApp.isApollo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.example.stx_dig.R;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_CanBaud;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.Dialog_Tipo_Macchina;
import packexcalib.exca.DataSaved;
import services.UpdateValuesService;
import utils.MyData;

public class MachineSettings extends BaseClass {
    Dialog_Tipo_Macchina dialogTipoMacchina;
    private boolean isStarted = false;
    TextView machineName, calibResult;
    ImageView save, back, file, gpsstat, sfondo, can1, can2;
    Button info, boom1, boom2, linkage, stick, tilt, frame, compass, canopenB, sensorDamping, startcalib, slideBoom;
    int indexMachineSelected;
    DialogPassword dialogPassword;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    Dialog_CanBaud dialogCanBaud;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_config);

        findView();
        init();
        onClick();
        onLongClick();
        updateUI();
    }

    private void findView() {
        machineName = findViewById(R.id.machineName);
        save = findViewById(R.id.storeData);
        back = findViewById(R.id.back);
        info = findViewById(R.id.generalDialog);
        boom1 = findViewById(R.id.openBoom1Dialog);
        boom2 = findViewById(R.id.openBoom2Dialog);
        linkage = findViewById(R.id.openLinkageDialog);
        stick = findViewById(R.id.openStickDialog);
        tilt = findViewById(R.id.openTiltDialog);
        frame = findViewById(R.id.openFrameDialog);
        compass = findViewById(R.id.openCompassDialog);
        file = findViewById(R.id.upload_file);
        gpsstat = findViewById(R.id.gpsstatis);
        canopenB = findViewById(R.id.canOpenset);
        sensorDamping = findViewById(R.id.sensorDamping);
        startcalib = findViewById(R.id.startCalib);
        calibResult = findViewById(R.id.txtCheckCalib);
        slideBoom = findViewById(R.id.openSlideBoom);
        sfondo = findViewById(R.id.imageView3);
        slideBoom.setVisibility(View.INVISIBLE);
        can1 = findViewById(R.id.can1setup);
        can2 = findViewById(R.id.can2setup);


    }

    private void init() {

        dialogCanBaud = new Dialog_CanBaud(this);
        startcalib.setVisibility(View.INVISIBLE);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogPassword = new DialogPassword(this);
        dialogTipoMacchina = new Dialog_Tipo_Macchina(this);
        indexMachineSelected = MyData.get_Int("MachineSelected");
        machineName.setText(MyData.get_String("M" + indexMachineSelected + "_Name"));

        if (DataSaved.isCanOpen == 2 || DataSaved.isCanOpen == 3) {
            canopenB.setVisibility(View.VISIBLE);
        } else {
            canopenB.setVisibility(View.INVISIBLE);
        }
        if (DataSaved.isCanOpen == 3) {
            sensorDamping.setVisibility(View.VISIBLE);
        } else {
            sensorDamping.setVisibility(View.VISIBLE);
        }
        if (DataSaved.isWL == 2 || DataSaved.isWL == 3 || DataSaved.isWL == 4) {
            if (DataSaved.L_Boom1 == -1d) {
                boom1.setVisibility(View.VISIBLE);
            } else {
                boom1.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void disableAll() {
        startcalib.setEnabled(false);
        sensorDamping.setEnabled(false);
        back.setEnabled(false);
        info.setEnabled(false);
        boom1.setEnabled(false);
        boom2.setEnabled(false);
        linkage.setEnabled(false);
        stick.setEnabled(false);
        frame.setEnabled(false);
        compass.setEnabled(false);
        tilt.setEnabled(false);
        file.setEnabled(false);
        slideBoom.setEnabled(false);
        can1.setEnabled(false);
        can2.setEnabled(false);
    }

    private void onClick() {
        can1.setOnClickListener(view -> {
            if (!dialogCanBaud.dialog.isShowing()) {
                dialogCanBaud.show(1);
            }
        });
        can2.setOnClickListener(view -> {
            if (!dialogCanBaud.dialog.isShowing()) {
                dialogCanBaud.show(2);
            }
        });
        machineName.setOnClickListener(view -> {
            if (!dialogTipoMacchina.dialog.isShowing()) {
                dialogTipoMacchina.show();
            }

        });

        gpsstat.setOnClickListener(view -> {
            if (DataSaved.portView >= 2) {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                    dialogGnssCoordinates.show();
                }
            }
        });
        startcalib.setOnClickListener(view -> {
            disableAll();
            startActivity(new Intent(this, GPS_Autocalib.class));
            overridePendingTransition(0, 0);
            finish();
        });
        sensorDamping.setOnClickListener(view -> {
            if (isApollo) {
                disableAll();
                startActivity(new Intent(this, DampingActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else {
                new CustomToast(this, "Not Implemented via BT").show();
            }

        });
        back.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, ExcavatorChooserActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        info.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, MachineInfoCalib.class));
            overridePendingTransition(0, 0);
            finish();
        });

        boom1.setOnClickListener((View v) -> {

            if (DataSaved.isWL < 1) {
                disableAll();
                startActivity(new Intent(this, Boom1Calib.class));
                overridePendingTransition(0, 0);
                finish();
            } else {
                if (DataSaved.L_Boom1 == -1) {
                    MyData.push("M" + indexMachineSelected + "_LengthBoom1", "0");
                    DataSaved.L_Boom1 = 0;
                } else {
                    MyData.push("M" + indexMachineSelected + "_LengthBoom1", "-1");
                    DataSaved.L_Boom1 = -1;
                }
                ;
            }
        });

        boom2.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, Boom2Calib.class));
            overridePendingTransition(0, 0);
            finish();
        });

        linkage.setOnClickListener((View v) -> {
            if (DataSaved.isWL < 2) {
                disableAll();
                startActivity(new Intent(this, LinkageCalib.class));
                overridePendingTransition(0, 0);
                finish();
            } else {
                disableAll();
                startActivity(new Intent(this, Tilt_Blade.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        stick.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, StickCalib.class));
            overridePendingTransition(0, 0);
            finish();
        });

        frame.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, FrameCalib.class));
            overridePendingTransition(0, 0);
            finish();
        });

        compass.setOnClickListener((View v) -> {
            if (DataSaved.isWL < 2) {
                disableAll();
                startActivity(new Intent(this, XYZ_Calib.class));
                overridePendingTransition(0, 0);
                finish();
            } else {
                disableAll();
                startActivity(new Intent(this, XYZ_Calib_Dozer.class));
                overridePendingTransition(0, 0);
                finish();
            }

        });

        tilt.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, TiltCalib.class));
            overridePendingTransition(0, 0);
            finish();
        });

        file.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, PickMachine.class));
            overridePendingTransition(0, 0);
            finish();
        });

        canopenB.setOnClickListener(view -> {
            if (isApollo) {
                if (!dialogPassword.dialog.isShowing()) {
                    dialogPassword.show();
                }
            } else {
                new CustomToast(this, "Not Implemented").show();
            }


        });
        slideBoom.setOnClickListener(view -> {
            disableAll();
            startActivity(new Intent(this, SlideBoomActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void onLongClick() {
        save.setOnClickListener((View v) -> {
            if (MyData.get_String("M" + indexMachineSelected + "_Name").equals("")) {
                new CustomToast(MachineSettings.this, "MISSING NAME").show();
            } else {
                if (MyData.get_String("M" + indexMachineSelected + "_Name").equals("EXCAVATOR 1") ||
                        MyData.get_String("M" + indexMachineSelected + "_Name").equals("EXCAVATOR 2") ||
                        MyData.get_String("M" + indexMachineSelected + "_Name").equals("WHEEL LOADER 1") ||
                        MyData.get_String("M" + indexMachineSelected + "_Name").equals("DOZER 1")
                ) {
                    new CustomToast(MachineSettings.this, "CHANGE MACHINE NAME BEFORE SAVE").show_alert();
                } else {
                    // Crea un nuovo AlertDialog.Builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(MachineSettings.this);
                    builder.setTitle("SAVE ALL PARAMETERS TO EXTERNAL MEMORY");
                    builder.setMessage("Do You Want to Proceed ?");

                    // Aggiungi il pulsante "Sì"
                    builder.setPositiveButton("YES", (dialog, which) -> {

                        save_nuova();
                        new CustomToast(MachineSettings.this, "SAVED").show();
                    });

                    // Aggiungi il pulsante "No"
                    builder.setNegativeButton("NO", (dialog, which) -> {

                    });
                    builder.setCancelable(true);
                    builder.show();

                }
            }


        });
    }

    private void save() {
        String typeCan = MyData.get_String("M" + indexMachineSelected + "_useCanOpen");
        String nameM = MyData.get_String("M" + indexMachineSelected + "_Name");
        String lrBoom1 = MyData.get_String("M" + indexMachineSelected + "_Boom1_MountPos");
        String lengthBoom1 = MyData.get_String("M" + indexMachineSelected + "_LengthBoom1");
        String offsetBoom1 = MyData.get_String("M" + indexMachineSelected + "_OffsetBoom1");
        String lrBoom2 = MyData.get_String("M" + indexMachineSelected + "_Boom2_MountPos");
        String lengthBoom2 = MyData.get_String("M" + indexMachineSelected + "_LengthBoom2");
        String offsetBoom2 = MyData.get_String("M" + indexMachineSelected + "_OffsetBoom2");
        String lrStick = MyData.get_String("M" + indexMachineSelected + "_Stick_MountPos");
        String lengthStick = MyData.get_String("M" + indexMachineSelected + "_LengthStick");
        String offsetStick = MyData.get_String("M" + indexMachineSelected + "_OffsetStick");
        String lsv = MyData.get_String("M" + indexMachineSelected + "_LaserVStick");
        String lsh = MyData.get_String("M" + indexMachineSelected + "_LaserHStick");
        String lrBucket = MyData.get_String("M" + indexMachineSelected + "_Bucket_MountPos");
        String L1 = MyData.get_String("M" + indexMachineSelected + "_LengthL1");
        String L2 = MyData.get_String("M" + indexMachineSelected + "_LengthL2");
        String L3 = MyData.get_String("M" + indexMachineSelected + "_LengthL3");
        String offsetDogBone = MyData.get_String("M" + indexMachineSelected + "_OffsetDB");
        String lrFrame = MyData.get_String("M" + indexMachineSelected + "_Frame_MountPos");
        String lengthPitch = MyData.get_String("M" + indexMachineSelected + "_LengthPitch");
        String lengthRoll = MyData.get_String("M" + indexMachineSelected + "_LengthRoll");
        String offsetPitch = MyData.get_String("M" + indexMachineSelected + "_OffsetFrameY");
        String offsetRoll = MyData.get_String("M" + indexMachineSelected + "_OffsetFrameX");
        String gpsdeltaX = MyData.get_String("M" + indexMachineSelected + "_OffsetGPSX");
        String gpsdeltaY = MyData.get_String("M" + indexMachineSelected + "_OffsetGPSY");
        String gpsdeltaZ = MyData.get_String("M" + indexMachineSelected + "_OffsetGPSZ");
        String gps2Dev = MyData.get_String("M" + indexMachineSelected + "_OffsetGPS2");
        String gpsType = MyData.get_String("M" + indexMachineSelected + "_sc600");
        String comPort = MyData.get_String("M" + indexMachineSelected + "_comPort");
        String isWL = MyData.get_String("M" + indexMachineSelected + "_isWL");
        String reqSpeed = MyData.get_String("M" + indexMachineSelected + "reqSpeed");
        String radioMode = MyData.get_String("M" + indexMachineSelected + "radioMode");
        String licenza = MyData.get_String("licenza");
        String quick = MyData.get_String("M" + indexMachineSelected + "_hasQuick");
        String lang = MyData.get_String("language");
        String widR = MyData.get_String("M" + indexMachineSelected + "_Bucket_" + "0" + "_Width_R");
        String widL = MyData.get_String("M" + indexMachineSelected + "_Bucket_" + "0" + "_Width_L");
        String palo = MyData.get_String("M" + indexMachineSelected + "_Bucket_" + "0" + "_Palo");
        String lama = MyData.get_String("M" + indexMachineSelected + "_Bucket_" + "0" + "_Lama");


        String[] tiltMountPos = new String[10];
        String[] benneTilt_Leng = new String[10];
        String[] benneTilt_Offset = new String[10];


        for (int i = 0; i < tiltMountPos.length; i++) {
            tiltMountPos[i] = MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + i);
            benneTilt_Leng[i] = MyData.get_String("M" + indexMachineSelected + "_Tilt_Length" + i);
            benneTilt_Offset[i] = MyData.get_String("M" + indexMachineSelected + "_Tilt_Offset" + i);
        }

        String path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Machine " + indexMachineSelected + "/Config";
        String fileName = nameM + ".csv";
        File f = new File(path, fileName);
        CSVWriter writer;
        String pathL = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines";
        String fileNameL = "Lic.csv";
        File file1 = new File(pathL, fileNameL);
        CSVWriter writer1;

        try {
            writer = new CSVWriter(new FileWriter(f));

            String[] can = {typeCan};
            String[] boom1 = {lrBoom1, lengthBoom1, offsetBoom1};
            String[] boom2 = {lrBoom2, lengthBoom2, offsetBoom2};
            String[] stick = {lrStick, lengthStick, offsetStick, lsv, lsh};
            String[] linkage = {lrBucket, L1, L2, L3, offsetDogBone};
            String[] frame = {lrFrame, lengthPitch, lengthRoll, offsetPitch, offsetRoll};
            String[] gps1x = {gpsdeltaX};
            String[] gps1y = {gpsdeltaY};
            String[] gps1z = {gpsdeltaZ};
            String[] gps2 = {gps2Dev};
            String[] gpsT = {gpsType};
            String[] com = {comPort};
            String[] wl = {isWL};
            String[] speed = {reqSpeed};
            String[] radio = {radioMode};
            String[] coupler = {quick};
            String[] langua = {lang};
            String[] wir = {widR};
            String[] wil = {widL};
            String[] paloL = {palo};
            String[] lamaL = {lama};


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


            writer.close();
        } catch (Exception ignored) {
        }
        try {
            String s = "";
            if (Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
                Apollo2 apollo2 = Apollo2.getInstance(MachineSettings.this);
                s = apollo2.getDeviceSN();
            } else {
                ApolloPro apolloPro = ApolloPro.getInstance(MachineSettings.this);
                s = apolloPro.getDeviceSN();
            }
            writer1 = new CSVWriter(new FileWriter(file1));
            String[] lic = {s, licenza};
            writer1.writeNext(lic);
            writer1.close();
        } catch (Exception ignored) {

        }
    }

    private void save_nuova() {
        String typeCan = MyData.get_String("M" + indexMachineSelected + "_useCanOpen");
        String nameM = MyData.get_String("M" + indexMachineSelected + "_Name");
        String lrBoom1 = MyData.get_String("M" + indexMachineSelected + "_Boom1_MountPos");
        String lengthBoom1 = MyData.get_String("M" + indexMachineSelected + "_LengthBoom1");
        String offsetBoom1 = MyData.get_String("M" + indexMachineSelected + "_OffsetBoom1");
        String lrBoom2 = MyData.get_String("M" + indexMachineSelected + "_Boom2_MountPos");
        String lengthBoom2 = MyData.get_String("M" + indexMachineSelected + "_LengthBoom2");
        String offsetBoom2 = MyData.get_String("M" + indexMachineSelected + "_OffsetBoom2");
        String lrStick = MyData.get_String("M" + indexMachineSelected + "_Stick_MountPos");
        String lengthStick = MyData.get_String("M" + indexMachineSelected + "_LengthStick");
        String offsetStick = MyData.get_String("M" + indexMachineSelected + "_OffsetStick");
        String lsv = MyData.get_String("M" + indexMachineSelected + "_LaserVStick");
        String lsh = MyData.get_String("M" + indexMachineSelected + "_LaserHStick");
        String lrBucket = MyData.get_String("M" + indexMachineSelected + "_Bucket_MountPos");
        String L1 = MyData.get_String("M" + indexMachineSelected + "_LengthL1");
        String L2 = MyData.get_String("M" + indexMachineSelected + "_LengthL2");
        String L3 = MyData.get_String("M" + indexMachineSelected + "_LengthL3");
        String offsetDogBone = MyData.get_String("M" + indexMachineSelected + "_OffsetDB");
        String lrFrame = MyData.get_String("M" + indexMachineSelected + "_Frame_MountPos");
        String lengthPitch = MyData.get_String("M" + indexMachineSelected + "_LengthPitch");
        String lengthRoll = MyData.get_String("M" + indexMachineSelected + "_LengthRoll");
        String offsetPitch = MyData.get_String("M" + indexMachineSelected + "_OffsetFrameY");
        String offsetRoll = MyData.get_String("M" + indexMachineSelected + "_OffsetFrameX");
        String gpsdeltaX = MyData.get_String("M" + indexMachineSelected + "_OffsetGPSX");
        String gpsdeltaY = MyData.get_String("M" + indexMachineSelected + "_OffsetGPSY");
        String gpsdeltaZ = MyData.get_String("M" + indexMachineSelected + "_OffsetGPSZ");
        String gps2Dev = MyData.get_String("M" + indexMachineSelected + "_OffsetGPS2");
        String gpsType = MyData.get_String("M" + indexMachineSelected + "_sc600");
        String comPort = MyData.get_String("M" + indexMachineSelected + "_comPort");
        String isWL = MyData.get_String("M" + indexMachineSelected + "_isWL");
        String reqSpeed = MyData.get_String("M" + indexMachineSelected + "reqSpeed");
        String radioMode = MyData.get_String("M" + indexMachineSelected + "radioMode");
        String licenza = MyData.get_String("licenza");
        String quick = MyData.get_String("M" + indexMachineSelected + "_hasQuick");
        String lang = MyData.get_String("language");
        String widR = MyData.get_String("M" + indexMachineSelected + "_Bucket_" + "0" + "_Width_R");
        String widL = MyData.get_String("M" + indexMachineSelected + "_Bucket_" + "0" + "_Width_L");
        String palo = MyData.get_String("M" + indexMachineSelected + "_Bucket_" + "0" + "_Palo");
        String lama = MyData.get_String("M" + indexMachineSelected + "_Bucket_" + "0" + "_Lama");


        String[] tiltMountPos = new String[10];
        String[] benneTilt_Leng = new String[10];
        String[] benneTilt_Offset = new String[10];


        for (int i = 0; i < tiltMountPos.length; i++) {
            tiltMountPos[i] = MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + i);
            benneTilt_Leng[i] = MyData.get_String("M" + indexMachineSelected + "_Tilt_Length" + i);
            benneTilt_Offset[i] = MyData.get_String("M" + indexMachineSelected + "_Tilt_Offset" + i);
        }

        String path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Machine " + indexMachineSelected + "/Config";
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

            writer.close();
        } catch (Exception ignored) {
        }
        try {
            String s = "";
            if (Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
                Apollo2 apollo2 = Apollo2.getInstance(MachineSettings.this);
                s = apollo2.getDeviceSN();
            } else {
                ApolloPro apolloPro = ApolloPro.getInstance(MachineSettings.this);
                s = apolloPro.getDeviceSN();
            }
            writer1 = new CSVWriter(new FileWriter(file1));
            String[] lic = {s, licenza};
            writer1.writeNext(lic);
            writer1.close();
        } catch (Exception ignored) {

        }
    }

    public void updateUI() {

        try {
            if (DataSaved.gpsOk) {
                gpsstat.setImageResource(R.drawable.gps_si);
            } else {
                gpsstat.setImageResource(R.drawable.gps_no);
            }
            switch (DataSaved.isWL) {
                case 0:
                    sfondo.setImageResource(R.drawable.escavatore_sfondo);
                    sfondo.setRotationY(0);
                    boom1.setEnabled(true);
                    boom2.setEnabled(true);
                    tilt.setEnabled(true);
                    stick.setEnabled(true);
                    frame.setEnabled(true);

                    boom1.setVisibility(View.VISIBLE);
                    boom2.setVisibility(View.VISIBLE);
                    tilt.setVisibility(View.VISIBLE);
                    stick.setVisibility(View.VISIBLE);
                    frame.setVisibility(View.VISIBLE);
                    boom1.setText("BOOM 1");
                    boom1.setTextColor(getColor(R.color.white));
                    linkage.setText("LINKAGE\nBUCKET");
                    break;
                case 1:
                    sfondo.setImageResource(R.drawable.wa_sfondo);
                    sfondo.setRotationY(180);
                    boom1.setEnabled(true);
                    boom2.setEnabled(true);
                    tilt.setEnabled(true);
                    stick.setEnabled(true);
                    frame.setEnabled(true);
                    boom1.setVisibility(View.VISIBLE);
                    boom2.setVisibility(View.INVISIBLE);
                    tilt.setVisibility(View.INVISIBLE);
                    stick.setVisibility(View.VISIBLE);
                    frame.setVisibility(View.VISIBLE);
                    boom1.setText("BOOM 1");
                    stick.setText("MAIN BOOM");
                    boom1.setTextColor(getColor(R.color.white));
                    linkage.setText("LINKAGE\nBUCKET");
                    break;
                case 2:
                case 3:
                    //dozer
                    sfondo.setImageResource(R.drawable.bulldozer_sfondo);
                    sfondo.setRotationY(0);
                    boom1.setEnabled(true);
                    boom2.setEnabled(false);
                    tilt.setEnabled(false);
                    stick.setEnabled(false);
                    frame.setEnabled(false);
                    boom1.setVisibility(View.VISIBLE);
                    boom2.setVisibility(View.INVISIBLE);
                    tilt.setVisibility(View.INVISIBLE);
                    stick.setVisibility(View.INVISIBLE);
                    frame.setVisibility(View.INVISIBLE);
                    linkage.setText("BLADE\nTILT");

                    break;
                case 4:
                    //grader
                    sfondo.setImageResource(R.drawable.cartoon_graderr);
                    sfondo.setRotationY(0);
                    boom1.setEnabled(true);
                    boom2.setEnabled(false);
                    tilt.setEnabled(false);
                    stick.setEnabled(false);
                    frame.setEnabled(false);
                    boom1.setVisibility(View.VISIBLE);
                    boom2.setVisibility(View.INVISIBLE);
                    tilt.setVisibility(View.INVISIBLE);
                    stick.setVisibility(View.INVISIBLE);
                    frame.setVisibility(View.INVISIBLE);

                    linkage.setText("BLADE\nTILT");
                    break;
                case 5:
                    //drill
                    sfondo.setImageResource(R.drawable.drilling_black);
                    sfondo.setRotationY(0);
                    boom1.setEnabled(true);
                    boom2.setEnabled(true);
                    tilt.setEnabled(true);
                    stick.setEnabled(true);
                    frame.setEnabled(true);

                    boom1.setVisibility(View.VISIBLE);
                    boom2.setVisibility(View.VISIBLE);
                    tilt.setVisibility(View.VISIBLE);
                    stick.setVisibility(View.VISIBLE);
                    frame.setVisibility(View.VISIBLE);
                    boom1.setText("BOOM 1");
                    boom1.setTextColor(getColor(R.color.white));
                    linkage.setText("LINKAGE\nBUCKET");
                    break;
                case 6:
                    //piledriver
                    sfondo.setImageResource(R.drawable.piledriver_black);
                    break;

            }
            if (DataSaved.isWL == 1 || DataSaved.isWL == 2 || DataSaved.isWL == 3 || DataSaved.isWL == 4) {
                if (DataSaved.L_Boom1 == -1d) {
                    boom1.setText("MACHINE DISABLED");
                    boom1.setBackgroundTintList(getColorStateList(R.color._____cancel_text));
                    boom1.setTextColor(getColor(R.color.white));
                } else {
                    boom1.setText("MACHINE ENABLED");
                    boom1.setBackgroundTintList(getColorStateList(R.color.green));
                    boom1.setTextColor(getColor(R.color._____cancel_text));
                }
            } else if (DataSaved.isWL == 5 || DataSaved.isWL == 6) {
                if (DataSaved.L_Boom1 == -1d) {
                    boom1.setText("DRILL DISABLED");
                    boom1.setBackgroundTintList(getColorStateList(R.color._____cancel_text));
                    boom1.setTextColor(getColor(R.color.white));
                } else {
                    boom1.setText("DRILL ENABLED");
                    boom1.setBackgroundTintList(getColorStateList(R.color.green));
                    boom1.setTextColor(getColor(R.color._____cancel_text));
                }

            } else {
                boom1.setText("BOOM 1");
                boom1.setBackgroundTintList(getColorStateList(R.color.blue));
                boom1.setTextColor(getColor(R.color.white));
            }

        } catch (Exception ignored) {

        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

}