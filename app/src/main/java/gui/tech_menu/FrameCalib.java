package gui.tech_menu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.PopupImageDialog;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.gnss.NmeaListener;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;


public class FrameCalib extends BaseClass {

    private double mHdt;
    static double mOffsetHdt;

    EditText lengthPitch, lengthRoll;
    CheckBox off, fwd, bwd, left, right, useYawF;
    TextView pitchAngle, pitchOffsetAngle, rollAngle, rollOffsetAngle, textPitchL, textRollL, tempHDT;
    Button minusOffsetPitch, plusOffsetPitch, setOffsetPitch, minusOffsetRoll, plusOffsetRoll, setOffsetRoll;
    ImageView infoMount;
    ImageView save, esc;
    int countZero, countZeroP;
    boolean minusPressedP, plusPressedP, minusPressedR, plusPressedR;
    int indexMachineSelected;


    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure = 0;

    PopupImageDialog mount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_calib);
        findView();
        init();
        onClick();
        onLongClick();
        onTouch();
        onCheckedChanged();
        updateUI();


    }

    private void findView() {
        save = findViewById(R.id.save);
        esc = findViewById(R.id.exit);
        lengthPitch = findViewById(R.id.pitchLength);
        lengthRoll = findViewById(R.id.rollLength);
        off = findViewById(R.id.cbxOff);
        fwd = findViewById(R.id.cbxLeft);
        bwd = findViewById(R.id.cbxRight);
        left = findViewById(R.id.cbxFwd);
        right = findViewById(R.id.cbxBwd);
        minusOffsetPitch = findViewById(R.id.offsetMinusPitch);
        plusOffsetPitch = findViewById(R.id.offsetPlusPitch);
        setOffsetPitch = findViewById(R.id.offsetSetZeroPitch);
        pitchAngle = findViewById(R.id.pitchAngle_tv);
        pitchOffsetAngle = findViewById(R.id.pitchOffsetAngle_tv);
        minusOffsetRoll = findViewById(R.id.offsetMinusRoll);
        plusOffsetRoll = findViewById(R.id.offsetPlusRoll);
        setOffsetRoll = findViewById(R.id.offsetSetZeroRoll);
        rollAngle = findViewById(R.id.rollAngle_tv);
        rollOffsetAngle = findViewById(R.id.rollOffsetAngle_tv);
        textPitchL = findViewById(R.id.pl);
        textRollL = findViewById(R.id.rl);
        infoMount = findViewById(R.id.infoMount);
        tempHDT = findViewById(R.id.tempHdt);
        useYawF = findViewById(R.id.useYawF);
        useYawF.setVisibility(View.GONE);
        tempHDT.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void init() {

        mount = new PopupImageDialog(this, R.layout.popup_frame_mount);

        indexMachineSelected = MyData.get_Int("MachineSelected");

        indexMeasure = MyData.get_Int("Unit_Of_Measure");


        numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);

        numberDialog = new CustomNumberDialog(this, -1);

        MyData.push("M" + indexMachineSelected + "useYawFrame", "0");//Temporary RR
        useYawF.setChecked(!MyData.get_String("M" + indexMachineSelected + "useYawFrame").equals("0"));
        lengthPitch.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LengthPitch")));
        lengthRoll.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LengthRoll")));
        textPitchL.setText("PITCH LENGTH " + Utils.getMetriSimbol());
        textRollL.setText("ROLL LENGTH " + Utils.getMetriSimbol());

        int mountPos = MyData.get_Int("M" + indexMachineSelected + "_Frame_MountPos");
        switch (mountPos) {
            case 0:
            case -1:
                off.setChecked(true);
                break;
            case 1:
                fwd.setChecked(true);
                break;
            case 2:
                right.setChecked(true);
                break;
            case 3:
                bwd.setChecked(true);
                break;
            case 4:
                left.setChecked(true);
                break;
        }
    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {


        try {

            if (minusPressedR && plusPressedR) {
                countZero++;
                if (countZero > 40) {
                    DataSaved.offsetRoll = 0;
                }
            }
            if (minusPressedP && plusPressedP) {
                countZeroP++;
                if (countZeroP > 40) {
                    DataSaved.offsetPitch = 0;
                }
            }
            if (DataSaved.useYawFrame == 0) {
                mHdt = NmeaListener.mch_Hdt - mOffsetHdt;
                mHdt = mHdt % 360;
                if (mHdt < -180d) {
                    mHdt += 360;
                }
                if (mHdt > 180) {
                    mHdt -= 360;
                }
                tempHDT.setText(String.format("%.1f", mHdt));
            } else {
                mHdt = Sensors_Decoder.Deg_Yaw_Frame - mOffsetHdt;
                mHdt = mHdt % 360;
                if (mHdt < -180d) {
                    mHdt += 360;
                }
                if (mHdt > 180) {
                    mHdt -= 360;
                }
                tempHDT.setText(String.format("%.1f", mHdt));
            }
        } catch (Exception e) {
            tempHDT.setText("ERROR");
        }
        pitchAngle.setText(String.format("%.02f", ExcavatorLib.correctPitch).replace(",", "."));
        pitchOffsetAngle.setText(String.format("%.02f", DataSaved.offsetPitch).replace(",", "."));
        rollAngle.setText(String.format("%.02f", ExcavatorLib.correctRoll).replace(",", "."));
        rollOffsetAngle.setText(String.format("%.02f", DataSaved.offsetRoll).replace(",", "."));


    }

    private void save() {
        int mounPos = 0;

        if (fwd.isChecked()) {
            mounPos = 1;
        }
        if (right.isChecked()) {
            mounPos = 2;
        }
        if (bwd.isChecked()) {
            mounPos = 3;
        }
        if (left.isChecked()) {
            mounPos = 4;
        }
        if (off.isChecked()) {
            mounPos = 0;
        }
        DataSaved.lrFrame = mounPos;
        MyData.push("M" + indexMachineSelected + "_Frame_MountPos", String.valueOf(mounPos));
        MyData.push("M" + indexMachineSelected + "_OffsetFrameY", String.valueOf(DataSaved.offsetPitch));
        MyData.push("M" + indexMachineSelected + "_OffsetFrameX", String.valueOf(DataSaved.offsetRoll));
        MyData.push("M" + indexMachineSelected + "_LengthPitch", Utils.writeMetri(lengthPitch.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_LengthRoll", Utils.writeMetri(lengthRoll.getText().toString()));

    }

    private void onClick() {
        useYawF.setOnClickListener(view -> {
            if (MyData.get_String("M" + indexMachineSelected + "useYawFrame").equals("0")) {
                MyData.push("M" + indexMachineSelected + "useYawFrame", "1");
                DataSaved.useYawFrame = 1;
                useYawF.setChecked(true);
            } else {
                MyData.push("M" + indexMachineSelected + "useYawFrame", "0");
                DataSaved.useYawFrame = 0;
                useYawF.setChecked(false);
            }
        });
        save.setOnClickListener((View v) -> {

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!lengthPitch.getText().toString().contains("'") || !lengthRoll.getText().toString().contains("'")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    esc.setEnabled(false);
                    save.setEnabled(false);
                    save();
                    startService(new Intent(getApplicationContext(), UpdateValuesService.class));
                    startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
                    finish();
                }
            } else {
                if (!(lengthPitch.getText().toString().matches("-?\\d+(\\.\\d+)?") && lengthRoll.getText().toString().matches("-?\\d+(\\.\\d+)?"))) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    esc.setEnabled(false);
                    save.setEnabled(false);
                    save();
                    startService(new Intent(getApplicationContext(), UpdateValuesService.class));
                    startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
                    finish();
                }
            }
        });
        tempHDT.setOnClickListener(view -> {
            if (DataSaved.useYawFrame == 0) {
                mOffsetHdt = NmeaListener.mch_Orientation;
            } else {
                mOffsetHdt = Sensors_Decoder.Deg_Yaw_Frame;
            }
        });

        esc.setOnClickListener((View v) -> {
            esc.setEnabled(false);
            save.setEnabled(false);
            startService(new Intent(getApplicationContext(), UpdateValuesService.class));
            startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
            finish();
        });

        minusOffsetPitch.setOnClickListener((View v) -> {
            DataSaved.offsetPitch -= 0.05;
        });

        plusOffsetPitch.setOnClickListener((View v) -> {
            DataSaved.offsetPitch += 0.05;
        });

        minusOffsetRoll.setOnClickListener((View v) -> {
            DataSaved.offsetRoll -= 0.05;
        });

        plusOffsetRoll.setOnClickListener((View v) -> {
            DataSaved.offsetRoll += 0.05;
        });

        lengthPitch.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthPitch);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthPitch);
            }
        });

        lengthRoll.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthRoll);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthRoll);
            }
        });


    }


    private void onLongClick() {
        setOffsetPitch.setOnLongClickListener((View v) -> {
            setOffsetPitch.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offsetPitch = Sensors_Decoder.Deg_pitch;
            return true;
        });

        setOffsetRoll.setOnLongClickListener((View v) -> {
            setOffsetRoll.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offsetRoll = Sensors_Decoder.Deg_roll;
            return true;
        });

    }

    private void onCheckedChanged() {
        off.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (off.isChecked()) {
                DataSaved.lrFrame = 0;
                fwd.setChecked(false);
                bwd.setChecked(false);
                left.setChecked(false);
                right.setChecked(false);
            }
        });

        fwd.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (fwd.isChecked()) {
                DataSaved.lrFrame = 1;
                off.setChecked(false);
                bwd.setChecked(false);
                left.setChecked(false);
                right.setChecked(false);
            }
        });

        bwd.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (bwd.isChecked()) {
                DataSaved.lrFrame = 3;
                off.setChecked(false);
                fwd.setChecked(false);
                left.setChecked(false);
                right.setChecked(false);
            }
        });
        left.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (left.isChecked()) {
                DataSaved.lrFrame = 4;
                off.setChecked(false);
                fwd.setChecked(false);
                bwd.setChecked(false);
                right.setChecked(false);
            }
        });
        right.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (right.isChecked()) {
                DataSaved.lrFrame = 2;
                off.setChecked(false);
                fwd.setChecked(false);
                bwd.setChecked(false);
                left.setChecked(false);
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void onTouch() {
        infoMount.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                mount.show();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                mount.dialog.dismiss();
            }
            return false;
        });

        minusOffsetPitch.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                minusPressedP = true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                minusPressedP = false;
                countZeroP = 0;
            }
            return false;
        });
        plusOffsetPitch.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                plusPressedP = true;

            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                plusPressedP = false;
                countZeroP = 0;
            }
            return false;
        });

        minusOffsetRoll.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                minusPressedR = true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                minusPressedR = false;
                countZero = 0;
            }
            return false;
        });
        plusOffsetRoll.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                plusPressedR = true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                plusPressedR = false;
                countZero = 0;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countZero = 0;
        countZeroP = 0;

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

}




