package gui.tech_menu;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class TiltCalib extends BaseClass {
    ImageView save, exit;

    ImageView bennaMeno, bennaPiu, bennaSelezionata;

    EditText tiltLength;

    Button minus, plus, set;

    TextView angleTv, offsetTv, tiltLT, angleTV_2, titolo;

    CheckBox cbxOff, cbxLeft, cbxRight, cbxInvTR;

    int indexMachineSelected;

    int currentBucket = 1;

    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure = 0;

    private boolean isLoadingBucket = false;

    private static final int MIN_BUCKET = 1;
    private static final int MAX_BUCKET = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilt_config_7);
        findView();
        init();
        onClick();
        onLongClick();
        onCheckedChanged();
        updateUI();
    }

    private void findView() {
        titolo = findViewById(R.id.titolo);
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit);
        bennaMeno = findViewById(R.id.bennaMeno);
        bennaPiu = findViewById(R.id.bennaPiu);
        bennaSelezionata = findViewById(R.id.bennaSelezionata);
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
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexMeasure = MyData.get_Int("Unit_Of_Measure");

        try {
            cbxInvTR.setChecked(DataSaved.revTiltRot == 1);
            currentBucket = MyData.get_Int("M" + indexMachineSelected + "BucketSelected");
        } catch (Exception e) {
            currentBucket = MIN_BUCKET;
        }

        currentBucket = clampBucket(currentBucket);
        MyData.push("M" + indexMachineSelected + "BucketSelected", String.valueOf(currentBucket));

        numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
        numberDialog = new CustomNumberDialog(this, -1);

        tiltLT.setText("LENGTH " + Utils.getMetriSimbol());
        updateValues();
    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {
        switch (currentBucket) {
            case 1:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_1));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota1));
                }
                break;
            case 2:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_2));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota2));
                }
                break;
            case 3:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_3));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota3));
                }
                break;
            case 4:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_4));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota4));
                }
                break;
            case 5:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_5));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota5));
                }
                break;
            case 6:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_6));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota6));
                }
                break;
            case 7:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_7));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota7));
                }
                break;
            case 8:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_8));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota8));
                }
                break;
            case 9:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_9));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota9));
                }
                break;
            case 10:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_10));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota10));
                }
                break;
            case 11:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_11));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota11));
                }
                break;
            case 12:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_12));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota12));
                }
                break;
            case 13:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_13));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota13));
                }
                break;
            case 14:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_14));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota14));
                }
                break;
            case 15:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_15));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota15));
                }
                break;
            case 16:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_16));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota16));
                }
                break;
            case 17:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_17));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota17));
                }
                break;
            case 18:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_18));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota18));
                }
                break;
            case 19:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_19));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota19));
                }
                break;
            case 20:
                if (DataSaved.lrTilt != 0) {
                    bennaSelezionata.setImageResource((R.drawable.benna_tilt_20));
                } else {
                    bennaSelezionata.setImageResource((R.drawable.benna_vuota20));
                }
                break;
        }

        titolo.setText(getString(R.string.tilt_calibration) + " n:" + currentBucket);
        angleTv.setText(String.format("%.02f", ExcavatorLib.correctDeltaAngle).replace(",", "."));
        offsetTv.setText(String.format("%.02f", DataSaved.offsetTiltDeltaAngle).replace(",", "."));
        angleTV_2.setText(String.format("%.02f", Sensors_Decoder.Deg_Benna_W_Tilt).replace(",", ".") + " " + String.format("%.02f", Sensors_Decoder.Deg_tilt).replace(",", "."));
    }

    private void onLongClick() {
        set.setOnLongClickListener((View v) -> {
            set.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offsetTiltDeltaAngle = ExcavatorLib.correctBucket;
            updateUI();
            return true;
        });
    }

    private void onClick() {
        bennaPiu.setOnClickListener(view -> changeBucket(+1));
        bennaMeno.setOnClickListener(view -> changeBucket(-1));

        exit.setOnClickListener((View v) -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });

        save.setOnClickListener((View v) -> {
            if (!isCurrentInputValid()) {
                new CustomToast(this, "INPUT ERROR!!!").show_error();
                return;
            }

            exit.setEnabled(false);
            save.setEnabled(false);
            save();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });

        minus.setOnClickListener((View v) -> {
            DataSaved.offsetTiltDeltaAngle -= 0.1;
            updateUI();
        });

        plus.setOnClickListener((View v) -> {
            DataSaved.offsetTiltDeltaAngle += 0.1;
            updateUI();
        });

        tiltLength.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing()) {
                    numberDialogFtIn.show(tiltLength);
                }
            } else {
                if (!numberDialog.dialog.isShowing()) {
                    numberDialog.show(tiltLength);
                }
            }
        });
    }

    private void onCheckedChanged() {
        cbxInvTR.setOnClickListener(view -> {
            DataSaved.revTiltRot += 1;
            DataSaved.revTiltRot = DataSaved.revTiltRot % 2;

            if (DataSaved.revTiltRot == 0) {
                cbxInvTR.setChecked(false);
            } else if (DataSaved.revTiltRot == 1) {
                cbxInvTR.setChecked(true);
            }

            MyData.push("M" + indexMachineSelected + "revTiltRot", String.valueOf(DataSaved.revTiltRot));
        });

        cbxOff.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (isLoadingBucket) return;

            if (cbxOff.isChecked()) {
                DataSaved.lrTilt = 0;
                cbxLeft.setChecked(false);
                cbxRight.setChecked(false);
                updateUI();
            }
        });

        cbxLeft.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (isLoadingBucket) return;

            if (cbxLeft.isChecked()) {
                DataSaved.lrTilt = 1;
                cbxOff.setChecked(false);
                cbxRight.setChecked(false);
                updateUI();
            }
        });

        cbxRight.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (isLoadingBucket) return;

            if (cbxRight.isChecked()) {
                DataSaved.lrTilt = -1;
                cbxOff.setChecked(false);
                cbxLeft.setChecked(false);
                updateUI();
            }
        });
    }

    private void changeBucket(int delta) {
        if (!isCurrentInputValid()) {
            new CustomToast(this, "INPUT ERROR!!!").show_error();
            return;
        }

        saveCurrentBucketOnly();

        currentBucket = clampBucket(currentBucket + delta);
       //MyData.push("M" + indexMachineSelected + "BucketSelected", String.valueOf(currentBucket));

        updateValues();
        updateUI();
    }

    private int clampBucket(int bucket) {
        if (bucket < MIN_BUCKET) return MIN_BUCKET;
        if (bucket > MAX_BUCKET) return MAX_BUCKET;
        return bucket;
    }

    private boolean isCurrentInputValid() {
        String value = tiltLength.getText().toString().trim();

        if (indexMeasure == 4 || indexMeasure == 5) {
            return value.contains("'");
        }

        return value.matches("-?\\d+(\\.\\d+)?");
    }

    private void updateValues() {
        isLoadingBucket = true;

        try {
            DataSaved.lrTilt = MyData.get_Int("M" + indexMachineSelected + "_Tilt_MountPos" + currentBucket);
            DataSaved.L_Tilt = MyData.get_Double("M" + indexMachineSelected + "_Tilt_Length" + currentBucket);
            DataSaved.offsetTiltDeltaAngle = MyData.get_Double("M" + indexMachineSelected + "_Tilt_Offset" + currentBucket);

            tiltLength.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Tilt_Length" + currentBucket)));

            cbxOff.setChecked(false);
            cbxLeft.setChecked(false);
            cbxRight.setChecked(false);

            int mountPos = MyData.get_Int("M" + indexMachineSelected + "_Tilt_MountPos" + currentBucket);

            switch (mountPos) {
                case 1:
                    cbxLeft.setChecked(true);
                    break;

                case -1:
                    cbxRight.setChecked(true);
                    break;

                case 0:
                default:
                    cbxOff.setChecked(true);
                    DataSaved.lrTilt = 0;
                    break;
            }
        } finally {
            isLoadingBucket = false;
        }
    }

    private void saveCurrentBucketOnly() {
        int mounPos = 0;

        if (cbxLeft.isChecked()) {
            mounPos = 1;
        }

        if (cbxRight.isChecked()) {
            mounPos = -1;
        }

        String tiltLengthValue = Utils.writeMetri(tiltLength.getText().toString());

        DataSaved.lrTilt = mounPos;
        try {
            DataSaved.L_Tilt = Double.parseDouble(tiltLengthValue);
        } catch (Exception ignored) {
            DataSaved.L_Tilt = MyData.get_Double("M" + indexMachineSelected + "_Tilt_Length" + currentBucket);
        }

        MyData.push("M" + indexMachineSelected + "_Tilt_MountPos" + currentBucket, String.valueOf(mounPos));
        MyData.push("M" + indexMachineSelected + "_Tilt_Length" + currentBucket, tiltLengthValue);
        MyData.push("M" + indexMachineSelected + "_Tilt_Offset" + currentBucket, String.valueOf(DataSaved.offsetTiltDeltaAngle));
    }

    private void save() {
        saveCurrentBucketOnly();
        //MyData.push("M" + indexMachineSelected + "BucketSelected", String.valueOf(currentBucket));
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
