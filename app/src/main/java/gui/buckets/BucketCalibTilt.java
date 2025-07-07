package gui.buckets;

import static gui.MyApp.folderPath;
import static utils.Utils.isNumeric;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;

import gui.MyApp;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class BucketCalibTilt extends AppCompatActivity {
    TextView bucketLengthT, bucketWidthT, L4T, tinyBukt;
    EditText name, bucketLength, bucketWidth, L4;
    Button minusTiltLevelAngle, plusTiltLevelAngle, setTiltLevelAngle;
    Button setFlatAngle, offsetZero, plusBuck, minusBuck;
    Button save, exit;
    ImageButton load;
    TextView offsetBucketAngle, offsetFlatAngle, offsetTiltLevelAngle;
    TextView bucketAngleTv, flatAngleTv, tiltLevelAngleTv;
    CheckBox cbxOff, cbxLeft, cbxRight, cbTOff, cbTL, cbTR, top,topRev;
    int indexBucket;
    int indexMachineSelected;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;
    int indexMeasure = 0;
    CustomQwertyDialog qwertyDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.BRAND.equals("SRT8PROS")) {
            setContentView(R.layout.activity_bucket_tilt_calib);
        } else if (Build.BRAND.equals("SRT7PROS") || Build.BRAND.equals("APOLLO2_7") ||Build.BRAND.equals("APOLLO2_10")|| Build.BRAND.equals("qti")||Build.BRAND.equals("APOLLO2_12_PRO")||Build.BRAND.equals("APOLLO2_12_PLUS")) {
            setContentView(R.layout.activity_bucket_tilt_calib_7);
        } else {
            setContentView(R.layout.activity_bucket_tilt_calib_7);
        }
        findView();
        init();
        updateUI();
        onClick();
        onLongClick();


    }

    private void findView() {
        bucketLengthT = findViewById(R.id.bkl);
        bucketWidthT = findViewById(R.id.bwdt);
        L4T = findViewById(R.id.l4);
        name = findViewById(R.id.bucket_ID);
        bucketLength = findViewById(R.id.bucketLength);
        bucketWidth = findViewById(R.id.bucketWidth);
        L4 = findViewById(R.id.bucket_L4);
        tinyBukt = findViewById(R.id.tinyBucket);
        plusBuck = findViewById(R.id.offsetPlus);
        minusBuck = findViewById(R.id.offsetMinus);
        minusTiltLevelAngle = findViewById(R.id.tiltLevelAngleMinus);
        plusTiltLevelAngle = findViewById(R.id.tiltLevelAnglePlus);
        setTiltLevelAngle = findViewById(R.id.tiltLevelAngleSet);
        setFlatAngle = findViewById(R.id.offsetZeroFlat);
        offsetZero = findViewById(R.id.offsetZero);
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit);
        load = findViewById(R.id.load);
        offsetBucketAngle = findViewById(R.id.bucketAngleOffset);
        offsetFlatAngle = findViewById(R.id.bucketAngleFlatOffset);
        offsetTiltLevelAngle = findViewById(R.id.tiltLevelAngleOffset);
        bucketAngleTv = findViewById(R.id.bucketAngle);
        flatAngleTv = findViewById(R.id.bucketAngleFlat);
        tiltLevelAngleTv = findViewById(R.id.tiltLevelAngle);
        cbxOff = findViewById(R.id.cbxOff);
        cbxLeft = findViewById(R.id.cbxLeft);
        cbxRight = findViewById(R.id.cbxRight);
        cbTOff = findViewById(R.id.cbxTiltOff);
        cbTL = findViewById(R.id.cbxTiltLeft);
        cbTR = findViewById(R.id.cbxTiltRight);
        top = findViewById(R.id.cbTop);
        topRev = findViewById(R.id.cbTopRev);
        cbxOff.setEnabled(false);
        cbxOff.setChecked(false);
        cbxLeft.setEnabled(false);
        cbxLeft.setChecked(false);
        cbxRight.setEnabled(false);
        cbxRight.setChecked(false);
        top.setEnabled(false);
        top.setChecked(false);
        topRev.setEnabled(false);
        topRev.setChecked(false);

        cbTOff.setEnabled(false);
        cbTOff.setChecked(false);
        cbTL.setEnabled(false);
        cbTL.setChecked(false);
        cbTR.setEnabled(false);
        cbTR.setChecked(false);

    }

    @SuppressLint("SetTextI18n")
    private void init() {



        indexMeasure = MyData.get_Int("Unit_Of_Measure");

        if (indexMeasure == 4 || indexMeasure == 5) {
            numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
        } else {
            numberDialog = new CustomNumberDialog(this, -1);
        }

        qwertyDialog = new CustomQwertyDialog(this);
        indexBucket = getIntent().getExtras().getInt("indexBucket");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        name.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Name"));
        bucketLength.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Length")));
        bucketWidth.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Width")));
        L4.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_L4")));
        bucketLengthT.setText(getResources().getString(R.string.bucket_length) + Utils.getMetriSimbol());
        bucketWidthT.setText(getResources().getString(R.string.bucket_width) + Utils.getMetriSimbol());
        L4T.setText(getResources().getString(R.string.Linkage_L4) + Utils.getMetriSimbol());
        int bucketMountPos = MyData.get_Int("M" + indexMachineSelected + "_Bucket_MountPos");

        switch (bucketMountPos) {
            case 0:
                cbxOff.setChecked(true);
                break;
            case 1:
                cbxLeft.setChecked(true);
                break;
            case -1:
                cbxRight.setChecked(true);
                break;
            case 2:
                top.setChecked(true);
                break;
            case 3:
                topRev.setChecked(true);
                break;
        }
        int mountPos = MyData.get_Int("M" + indexMachineSelected + "_Tilt_MountPos" + indexBucket);
        switch (mountPos) {
            case 0:
                cbTOff.setChecked(true);
                break;
            case 1:
                cbTL.setChecked(true);
                break;
            case -1:
                cbTR.setChecked(true);
        }
    }

    public void updateUI() {

        tinyBukt.setText(" (" + String.format("%.3f", DataSaved.piccolaBucket) + ")");
        tiltLevelAngleTv.setText((getResources().getString(R.string.txt_tiltangle) + String.format("%.2f", ExcavatorLib.correctTilt) + " °").replace(",", "."));
        offsetTiltLevelAngle.setText((String.format("%.2f", DataSaved.offsetTilt) + " °").replace(",", "."));
        bucketAngleTv.setText(((getResources().getString(R.string.bucket_angle) + String.format("%.2f", ExcavatorLib.correctWTilt) + " °").replace(",", ".")));
        offsetBucketAngle.setText((String.format("%.2f", DataSaved.offsetDegWTilt) + " °").replace(",", "."));
        flatAngleTv.setText(((getResources().getString(R.string.bucket_flat_angle) + String.format("%.2f", ExcavatorLib.correctFlat) + " °").replace(",", ".")));
        offsetFlatAngle.setText((String.format("%.2f", DataSaved.offsetFlat) + " °").replace(",", "."));
        if (isNumeric(L4.getText().toString())) {
            DataSaved.L4 = Double.parseDouble(Utils.writeMetri(L4.getText().toString().trim()));
        } else {
            DataSaved.L4 = 0;
        }
        if (isNumeric(bucketLength.getText().toString())) {
            DataSaved.L_Bucket = Double.parseDouble(Utils.writeMetri(bucketLength.getText().toString().trim()));
        } else {
            DataSaved.L_Bucket = 0;
        }
        if (isNumeric(bucketWidth.getText().toString())) {
            DataSaved.W_Bucket = Double.parseDouble(Utils.writeMetri(bucketWidth.getText().toString().trim()));
        } else {
            DataSaved.W_Bucket = 0;
        }

        if (DataSaved.hasQuick == 1||DataSaved.L1<=0) {
            L4.setAlpha(0.5f);
            L4T.setAlpha(0.5f);
        } else {
            L4.setAlpha(1f);
            L4T.setAlpha(1f);
        }
    }

    private void onClick() {
        bucketLength.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(bucketLength);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(bucketLength);
            }
        });

        bucketWidth.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(bucketWidth);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(bucketWidth);
            }
        });

        L4.setOnClickListener((View v) -> {
            if(DataSaved.hasQuick==0&&DataSaved.L1>0) {
                if (indexMeasure == 4 || indexMeasure == 5) {
                    if (!numberDialogFtIn.dialog.isShowing())
                        numberDialogFtIn.show(L4);
                } else {
                    if (!numberDialog.dialog.isShowing())
                        numberDialog.show(L4);
                }
            }
        });

        name.setOnClickListener((View v) -> {
            if (!qwertyDialog.dialog.isShowing())
                qwertyDialog.show(name);
        });
        load.setOnClickListener((View v) -> {
            disableAll();
            Intent intent = new Intent(this, PickBucket.class);
            intent.putExtra("indexBucket", indexBucket);
            startActivity(intent);
            finish();
        });
        plusBuck.setOnClickListener((View v) -> {
            DataSaved.offsetDegWTilt += 0.05;

        });
        minusBuck.setOnClickListener((View v) -> {
            DataSaved.offsetDegWTilt -= 0.05;
        });


        name.setOnClickListener((View v) -> {
            if (!qwertyDialog.dialog.isShowing())
                qwertyDialog.show(name);
        });

        minusTiltLevelAngle.setOnClickListener((View v) -> {
            DataSaved.offsetTilt -= 0.05;
        });

        plusTiltLevelAngle.setOnClickListener((View v) -> {
            DataSaved.offsetTilt += 0.05;
        });

        save.setOnClickListener((View v) -> {

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!bucketLength.getText().toString().contains("'") || !bucketWidth.getText().toString().contains("'") || !L4.getText().toString().contains("'")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    if (name.getText().toString().equals("")) {
                        new CustomToast(this, "Missing Name").show_alert();
                    } else {
                        disableAll();
                        save();
                        store();
                        startService(new Intent(this, UpdateValuesService.class));
                        startActivity(new Intent(this, BucketChooserActivity.class));
                        finish();
                    }
                }
            } else {
                if (!(isNumeric(bucketLength.getText().toString()) && isNumeric(bucketWidth.getText().toString()) && isNumeric(L4.getText().toString()))) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    if (name.getText().toString().equals("")) {
                        new CustomToast(this, "Missing Name").show_alert();
                    } else {
                        disableAll();
                        save();
                        store();
                        startService(new Intent(this, UpdateValuesService.class));
                        startActivity(new Intent(this, BucketChooserActivity.class));
                        finish();
                    }
                }
            }
        });

        exit.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, BucketChooserActivity.class));
            finish();
        });

    }

    private void disableAll() {
        load.setEnabled(false);
        save.setEnabled(false);
        exit.setEnabled(false);
    }

    private void onLongClick() {
        offsetZero.setOnLongClickListener((View v) -> {
            offsetZero.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            if (DataSaved.L1 > 0) {
                DataSaved.offsetBucket = ExcavatorLib.bennaSimulata;
            } else {
                DataSaved.offsetBucket = Sensors_Decoder.Deg_bucket;
            }
            double a = DataSaved.L_Tilt;
            double c = DataSaved.L_Bucket;
            double beta = ExcavatorLib.correctBucket - ExcavatorLib.correctDeltaAngle;

            DataSaved.piccolaBucket = Math.sqrt((a * a) + (c * c) - 2 * a * c * Math.cos(Math.toRadians(beta)));

            DataSaved.offsetDegWTilt = Sensors_Decoder.Deg_Benna_W_Tilt;
            return true;
        });


        setTiltLevelAngle.setOnLongClickListener((View v) -> {
            setTiltLevelAngle.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offsetTilt = Sensors_Decoder.Deg_tilt;

            return true;
        });

        setFlatAngle.setOnLongClickListener((View v) -> {
            setFlatAngle.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));

                DataSaved.offsetFlat = ExcavatorLib.correctBucket;
                if (DataSaved.offsetFlat > -90) {
                    DataSaved.flat = 90 - Math.abs(DataSaved.offsetFlat);
                } else {
                    DataSaved.flat = Math.abs(DataSaved.offsetFlat) - 90;
                }

            return true;
        });
    }

    private void save() {
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Name", name.getText().toString().trim().toUpperCase());
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Length", Utils.writeMetri(bucketLength.getText().toString().trim()));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Width", Utils.writeMetri(bucketWidth.getText().toString().trim()));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_L4", Utils.writeMetri(L4.getText().toString().trim()));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Offset", String.valueOf(DataSaved.offsetBucket));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Flat_Offset", String.valueOf(DataSaved.offsetFlat));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Flat", String.valueOf(DataSaved.flat));
        MyData.push("M" + indexMachineSelected + "_Tilt_Offset_Angle" + indexBucket, String.valueOf(DataSaved.offsetTilt));
        MyData.push("M" + indexMachineSelected + "_Offset_DegWTilt" + indexBucket, String.valueOf(DataSaved.offsetDegWTilt));
        MyData.push("M" + indexMachineSelected + "_Tilt_piccolaBucket" + indexBucket, String.valueOf(DataSaved.piccolaBucket));
    }

    private void store() {
        String nameM = name.getText().toString().trim();
        String length = Utils.writeMetri(bucketLength.getText().toString().trim());
        String width = Utils.writeMetri(bucketWidth.getText().toString().trim());
        String L4Length = Utils.writeMetri(L4.getText().toString().trim());
        String offset = String.valueOf(DataSaved.offsetBucket);
        String flatOffset = String.valueOf(DataSaved.offsetFlat);
        String flat = String.valueOf(DataSaved.flat);
        String offsetTilt = String.valueOf(DataSaved.offsetTilt);
        String offsetWtilt = String.valueOf(DataSaved.offsetDegWTilt);
        String offsetpiccola = String.valueOf(DataSaved.piccolaBucket);
        String path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Machine " + indexMachineSelected + "/Buckets Tilt";
        String fileName = nameM + ".csv";
        File f = new File(path, fileName);
        CSVWriter writer;

        try {
            writer = new CSVWriter(new FileWriter(f));

            String[] bucket = {nameM, length, width, L4Length, offset, flatOffset, flat, offsetTilt, offsetWtilt, offsetpiccola};

            writer.writeNext(bucket);

            writer.close();
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



