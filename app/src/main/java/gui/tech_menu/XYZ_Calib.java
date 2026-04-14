package gui.tech_menu;

import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class XYZ_Calib extends BaseClass {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRepeating = false;

    ImageView gpsDebug;
    ImageView save, exit, update;
    EditText dx, dY, dZ, dHdt, dist;
    int indexMachineSelected;
    int indexMeasure;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    Dialog_Drill_GNSS dialogDrillGnss;
    ImageView wlwl, imgupsx, imgupdx, imgdwdx, aaaa;
    TextView tvX, tvY, tvZ, titolo, tv_btw;
    ImageView xp, xm, yp, ym, zp, zm, hp, hm;
    double myStep = 0.001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass_calib);
        wlwl = findViewById(R.id.wlwl);
        imgdwdx = findViewById(R.id.img104);
        imgupsx = findViewById(R.id.img101);
        imgupdx = findViewById(R.id.img102);
        aaaa = findViewById(R.id.img103);
        tv_btw = findViewById(R.id.tv_betw);
        dist = findViewById(R.id.distBetween);
        xm = findViewById(R.id.but_meno_x);
        xp = findViewById(R.id.but_piu_x);
        ym = findViewById(R.id.but_meno_y);
        yp = findViewById(R.id.but_piu_y);
        zm = findViewById(R.id.but_meno_z);
        zp = findViewById(R.id.but_piu_z);
        hm = findViewById(R.id.but_meno_h);
        hp = findViewById(R.id.but_piu_h);


        if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == DRILL) {
            imgdwdx.setVisibility(View.VISIBLE);
            imgupsx.setVisibility(View.VISIBLE);
            imgupdx.setVisibility(View.VISIBLE);
            aaaa.setVisibility(View.VISIBLE);
            wlwl.setVisibility(View.INVISIBLE);
        } else {
            imgdwdx.setVisibility(View.INVISIBLE);
            imgupsx.setVisibility(View.INVISIBLE);
            imgupdx.setVisibility(View.INVISIBLE);
            aaaa.setVisibility(View.INVISIBLE);
            wlwl.setVisibility(View.VISIBLE);
        }


        titolo = findViewById(R.id.titolo);
        update = findViewById(R.id.updateTool);
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit);
        dx = findViewById(R.id.deltaX);
        dY = findViewById(R.id.deltaY);
        dZ = findViewById(R.id.deltaZ);
        dHdt = findViewById(R.id.deltaHDT);
        tvX = findViewById(R.id.txtDeltaX);
        tvY = findViewById(R.id.txtDeltaYt);
        tvZ = findViewById(R.id.txtDeltaZt);
        gpsDebug = findViewById(R.id.gpsdebugg);
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");

        numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);

        numberDialog = new CustomNumberDialog(this, -1);

        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogDrillGnss = new Dialog_Drill_GNSS(this);
        updateTxt();
        tvX.setText("ΔX " + Utils.getMetriSimbol());
        tvY.setText("ΔY " + Utils.getMetriSimbol());
        tvZ.setText("ΔZ " + Utils.getMetriSimbol());
        tv_btw.setText("G1>>G2 " + Utils.getMetriSimbol());

        gpsDebug.setOnClickListener(view -> {
            if (DataSaved.isWL == DRILL) {
                if (!dialogDrillGnss.alertDialog.isShowing()) {
                    dialogDrillGnss.show();
                }
            } else {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                    dialogGnssCoordinates.show();
                }
            }
        });
        update.setOnClickListener(view -> {

            MyData.push("M" + indexMachineSelected + "_OffsetGPSX", Utils.writeMetri(dx.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSY", Utils.writeMetri(dY.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSZ", Utils.writeMetri(dZ.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", dHdt.getText().toString().replace(",", "."));
            MyData.push("M" + indexMachineSelected + "_distG1_G2", Utils.writeMetri(dist.getText().toString().replace(",", ".")));

        });

        save.setOnClickListener(view -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            MyData.push("M" + indexMachineSelected + "_OffsetGPSX", Utils.writeMetri(dx.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSY", Utils.writeMetri(dY.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSZ", Utils.writeMetri(dZ.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", dHdt.getText().toString().replace(",", "."));
            MyData.push("M" + indexMachineSelected + "_distG1_G2", Utils.writeMetri(dist.getText().toString().replace(",", ".")));
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();


        });
        exit.setOnClickListener(view -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });

        setupAutoRepeat(xp, () -> {
            DataSaved.deltaX += myStep;
            updateTxt();
        });
        setupAutoRepeat(xm, () -> {
            DataSaved.deltaX -= myStep;
            updateTxt();
        });
        setupAutoRepeat(yp, () -> {
            DataSaved.deltaY += myStep;
            updateTxt();
        });
        setupAutoRepeat(ym, () -> {
            DataSaved.deltaY -= myStep;
            updateTxt();
        });
        setupAutoRepeat(zp, () -> {
            DataSaved.deltaZ += myStep;
            updateTxt();
        });
        setupAutoRepeat(zm, () -> {
            DataSaved.deltaZ -= myStep;
            updateTxt();
        });
        setupAutoRepeat(hp, () -> {
            DataSaved.deltaGPS2 += 0.01;
            updateTxt();
        });
        setupAutoRepeat(hm, () -> {
            DataSaved.deltaGPS2 -= 0.01;
            updateTxt();
        });


        dx.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(dx);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(dx);
            }


        });
        dY.setOnClickListener(view -> {

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(dY);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(dY);
            }

        });
        dZ.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(dZ);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(dZ);
            }

        });
        dHdt.setOnClickListener(view -> {

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(dHdt);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(dHdt);
            }

        });
        dist.setOnClickListener(view -> {

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(dist);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(dist);
            }

        });
        numberDialog.dialog.setOnDismissListener(dialog -> {

            updateOnClose();
            updateTxt();


        });
        numberDialogFtIn.dialog.setOnDismissListener(dialog -> {

            updateOnClose();
            updateTxt();


        });

    }

    private void updateOnClose() {
        DataSaved.deltaX = Double.parseDouble(Utils.writeMetri((dx.getText().toString())));
        DataSaved.deltaY = Double.parseDouble(Utils.writeMetri((dY.getText().toString())));
        DataSaved.deltaZ = Double.parseDouble(Utils.writeMetri((dZ.getText().toString())));
        DataSaved.deltaGPS2 = Double.parseDouble(((dHdt.getText().toString())));
        DataSaved.distG1_G2 = Double.parseDouble(Utils.writeMetri((dist.getText().toString())));
    }

    public void updateUI() {
        switch (indexMeasure) {
            case 0:
            case 1:
                myStep = 0.001;
                break;

            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                myStep = 0.0003048;
                break;
            default:
                myStep = 0.0003048;
                break;
        }
        DataSaved.offset_Z_antenna = 0;
        double hdt = 0;
        hdt = NmeaListener.mch_Orientation + DataSaved.deltaGPS2;
        hdt = hdt % 360;

        titolo.setText("     E: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[0])) + "    N: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[1])) + "  " + "   Z: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[2])) + "   HDT: " + String.format("%.2f", hdt).replace(",", ".") + " °");

        if (DataSaved.gpsOk) {
            gpsDebug.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            gpsDebug.setImageTintList(ColorStateList.valueOf(Color.RED));
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void updateTxt() {
        dx.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deltaX)));
        dY.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deltaY)));
        dZ.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deltaZ)));
        dHdt.setText(String.format("%.3f", DataSaved.deltaGPS2).replace(",", "."));
        dist.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.distG1_G2)));
    }

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
                    if (isRepeating) {
                        updateOnClose();
                        updateTxt();

                    }
                    isRepeating = false; // stop

                    break;
            }
            return false;
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}