package gui.tech_menu;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class TiltCalib extends AppCompatActivity {
    Button save, exit;

    Button bucket1, bucket2, bucket3, bucket4, bucket5, bucket6, bucket7, bucket8, bucket9, bucket10;

    EditText tiltLength;

    Button minus, plus, set;

    TextView angleTv, offsetTv, tiltLT, angleTV_2;

    CheckBox cbxOff, cbxLeft, cbxRight, cbxInvTR;


    int indexMachineSelected;

    int currentBucket = 1;

    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.BRAND.equals("SRT8PROS")) {
            setContentView(R.layout.activity_tilt_config);
        } else if (Build.BRAND.equals("APOLLO2_10")||Build.BRAND.equals("SRT7PROS") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("qti") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
            setContentView(R.layout.activity_tilt_config_7);
        } else {
            setContentView(R.layout.activity_tilt_config_7);
        }

        findView();
        init();
        updateUI();
        onClick();
        onLongClick();
        onCheckedChanged();

    }

    private void findView() {
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit);
        bucket1 = findViewById(R.id.bucket1);
        bucket2 = findViewById(R.id.bucket2);
        bucket3 = findViewById(R.id.bucket3);
        bucket4 = findViewById(R.id.bucket4);
        bucket5 = findViewById(R.id.bucket5);
        bucket6 = findViewById(R.id.bucket6);
        bucket7 = findViewById(R.id.bucket7);
        bucket8 = findViewById(R.id.bucket8);
        bucket9 = findViewById(R.id.bucket9);
        bucket10 = findViewById(R.id.bucket10);
        tiltLength = findViewById(R.id.tiltLength);
        tiltLT = findViewById(R.id.tiltLT);
        cbxOff = findViewById(R.id.cbxOff);
        cbxLeft = findViewById(R.id.cbxLeft);
        cbxRight = findViewById(R.id.cbxRight);
        angleTv = findViewById(R.id.tiltAngle_tv);
        angleTV_2 = findViewById(R.id.tiltAngle_tv2);
        offsetTv = findViewById(R.id.tiltOffsetAngle_tv);
        set = findViewById(R.id.setOffset);
        plus = findViewById(R.id.offsetPlus);
        minus = findViewById(R.id.offsetMinus);
        cbxInvTR = findViewById(R.id.cbxInvTR);

    }

    @SuppressLint("SetTextI18n")
    private void init() {

        try {
            cbxInvTR.setChecked(DataSaved.reverseRotator == 1);
        } catch (Exception e) {

        }


        indexMachineSelected = MyData.get_Int("MachineSelected");

        indexMeasure = MyData.get_Int("Unit_Of_Measure");

        if (indexMeasure == 4 || indexMeasure == 5) {
            numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
        } else {
            numberDialog = new CustomNumberDialog(this, -1);
        }

        tiltLength.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Tilt_Length" + currentBucket)));

        tiltLT.setText("LENGTH " + Utils.getMetriSimbol());
        updateValues();
        int mountPos = Integer.parseInt(MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + currentBucket));
        switch (mountPos) {
            case 0:
                cbxOff.setChecked(true);
                break;
            case 1:
                cbxLeft.setChecked(true);
                break;
            case -1:
                cbxRight.setChecked(true);
        }
    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {


        bucket1.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 1 ? R.color.blue : R.color.dark_gray));
        bucket2.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 2 ? R.color.blue : R.color.dark_gray));
        bucket3.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 3 ? R.color.blue : R.color.dark_gray));
        bucket4.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 4 ? R.color.blue : R.color.dark_gray));
        bucket5.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 5 ? R.color.blue : R.color.dark_gray));
        bucket6.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 6 ? R.color.blue : R.color.dark_gray));
        bucket7.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 7 ? R.color.blue : R.color.dark_gray));
        bucket8.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 8 ? R.color.blue : R.color.dark_gray));
        bucket9.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 9 ? R.color.blue : R.color.dark_gray));
        bucket10.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), currentBucket == 10 ? R.color.blue : R.color.dark_gray));
        angleTv.setText(String.format("%.02f", ExcavatorLib.correctDeltaAngle).replace(",", "."));
        offsetTv.setText(String.format("%.02f", DataSaved.offsetTiltDeltaAngle).replace(",", "."));
        angleTV_2.setText(String.format("%.02f", Sensors_Decoder.Deg_Benna_W_Tilt).replace(",", ".") + " " + String.format("%.02f", Sensors_Decoder.Deg_tilt).replace(",", "."));


    }


    private void onLongClick() {
        set.setOnLongClickListener((View v) -> {
            set.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offsetTiltDeltaAngle = ExcavatorLib.correctBucket;
            return true;
        });
    }


    private void onClick() {
        exit.setOnClickListener((View v) -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, MachineSettings.class));
            finish();
        });

        save.setOnClickListener((View v) -> {

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!tiltLength.getText().toString().contains("'")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    exit.setEnabled(false);
                    save.setEnabled(false);
                    save();
                    startService(new Intent(this, UpdateValuesService.class));
                    startActivity(new Intent(this, MachineSettings.class));
                    finish();
                }
            } else {
                if (!(tiltLength.getText().toString().matches("-?\\d+(\\.\\d+)?"))) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    exit.setEnabled(false);
                    save.setEnabled(false);
                    save();
                    startService(new Intent(this, UpdateValuesService.class));
                    startActivity(new Intent(this, MachineSettings.class));
                    finish();
                }
            }

        });

        minus.setOnClickListener((View v) -> {
            DataSaved.offsetTiltDeltaAngle -= 0.1;
        });

        plus.setOnClickListener((View v) -> {
            DataSaved.offsetTiltDeltaAngle += 0.1;
        });

        bucket1.setOnClickListener((View v) -> {
            currentBucket = 1;
            updateValues();
        });

        bucket2.setOnClickListener((View v) -> {
            currentBucket = 2;
            updateValues();
        });

        bucket3.setOnClickListener((View v) -> {
            currentBucket = 3;
            updateValues();
        });

        bucket4.setOnClickListener((View v) -> {
            currentBucket = 4;
            updateValues();
        });

        bucket5.setOnClickListener((View v) -> {
            currentBucket = 5;
            updateValues();
        });

        bucket6.setOnClickListener((View v) -> {
            currentBucket = 6;
            updateValues();
        });

        bucket7.setOnClickListener((View v) -> {
            currentBucket = 7;
            updateValues();
        });

        bucket8.setOnClickListener((View v) -> {
            currentBucket = 8;
            updateValues();
        });

        bucket9.setOnClickListener((View v) -> {
            currentBucket = 9;
            updateValues();
        });

        bucket10.setOnClickListener((View v) -> {
            currentBucket = 10;
            updateValues();
        });

        tiltLength.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(tiltLength);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(tiltLength);
            }

        });

    }


    private void onCheckedChanged() {
        cbxInvTR.setOnClickListener(view -> {
            DataSaved.reverseRotator += 1;
            DataSaved.reverseRotator = DataSaved.reverseRotator % 2;
            if (DataSaved.reverseRotator == 0) {
                cbxInvTR.setChecked(false);
            } else if (DataSaved.reverseRotator == 1) {
                cbxInvTR.setChecked(true);
            }
            MyData.push("M" + indexMachineSelected + "revTiltRot", String.valueOf(DataSaved.reverseRotator));
        });
        cbxOff.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxOff.isChecked()) {
                DataSaved.lrTilt = 0;
                cbxLeft.setChecked(false);
                cbxRight.setChecked(false);
            }
        });

        cbxLeft.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxLeft.isChecked()) {
                DataSaved.lrTilt = 1;
                cbxOff.setChecked(false);
                cbxRight.setChecked(false);
            }
        });

        cbxRight.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxRight.isChecked()) {
                DataSaved.lrTilt = -1;
                cbxOff.setChecked(false);
                cbxLeft.setChecked(false);
            }
        });
    }

    private void updateValues() {
        DataSaved.lrTilt = MyData.get_Int("M" + indexMachineSelected + "_Tilt_MountPos" + currentBucket);
        DataSaved.L_Tilt = MyData.get_Double("M" + indexMachineSelected + "_Tilt_Length" + currentBucket);
        DataSaved.offsetTiltDeltaAngle = MyData.get_Double("M" + indexMachineSelected + "_Tilt_Offset" + currentBucket);
        tiltLength.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Tilt_Length" + currentBucket)));
        int mountPos = MyData.get_Int("M" + indexMachineSelected + "_Tilt_MountPos" + currentBucket);
        switch (mountPos) {
            case 0:
                cbxOff.setChecked(true);
                break;
            case 1:
                cbxLeft.setChecked(true);
                break;
            case 2:
                cbxRight.setChecked(true);
        }
    }

    private void save() {
        int mounPos = 0;

        if (cbxLeft.isChecked()) {
            mounPos = 1;
        }
        if (cbxRight.isChecked()) {
            mounPos = -1;
        }
        DataSaved.lrTilt = mounPos;

        MyData.push("M" + indexMachineSelected + "_Tilt_MountPos" + currentBucket, String.valueOf(mounPos));

        MyData.push("M" + indexMachineSelected + "_Tilt_Length" + currentBucket, Utils.writeMetri(tiltLength.getText().toString()));

        MyData.push("M" + indexMachineSelected + "_Tilt_Offset" + currentBucket, String.valueOf(DataSaved.offsetTiltDeltaAngle));
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


