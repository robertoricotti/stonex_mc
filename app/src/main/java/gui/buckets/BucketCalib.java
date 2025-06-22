package gui.buckets;

import static gui.MyApp.folderPath;
import static utils.Utils.isNumeric;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;


import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.PopupImageDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;


public class BucketCalib extends AppCompatActivity {
    Button esc, save, offsetZeroMinus, offsetZeroPlus, offsetZero, offsetZeroFlat;
    ImageButton load;
    EditText nameBucket, lengthBucket, widthBucket, L4Bucket;
    CheckBox off, left, right, top,topRev;
    TextView bucketAngle, bucketFlatAngle, bucketAngleOffset, bucketFlatAngleOffset, textLength, textWidth, textL4;

    int indexBucket;
    int indexMachineSelected;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure = 0;

    CustomQwertyDialog qwertyDialog;
    ImageView infoAngolo, infoFlat;
    PopupImageDialog angolo, flat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.BRAND.equals("SRT8PROS")) {
            setContentView(R.layout.activity_bucket_calib_2);
        } else if (Build.BRAND.equals("SRT7PROS")||Build.BRAND.equals("APOLLO2_10")||Build.BRAND.equals("APOLLO2_7")||Build.BRAND.equals("qti")||Build.BRAND.equals("APOLLO2_12_PRO")||Build.BRAND.equals("APOLLO2_12_PLUS")) {
            setContentView(R.layout.activity_bucket_calib_7);
        } else {
            setContentView(R.layout.activity_bucket_calib_7);

        }
        findView();
        init();
        onClick();
        onLongClick();
        onTouch();
        updateUI();

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
        angolo = new PopupImageDialog(this, R.layout.popup_bucket_angle_90);
        flat = new PopupImageDialog(this, R.layout.popup_bucket_flat_angle);

        indexBucket = getIntent().getExtras().getInt("indexBucket");
        indexMachineSelected = MyData.get_Int("MachineSelected");

        nameBucket.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Name"));
        lengthBucket.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Length")));
        widthBucket.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Width")));
        L4Bucket.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_L4")));

        textLength.setText(getResources().getString(R.string.bucket_length) + Utils.getMetriSimbol());
        textWidth.setText(getResources().getString(R.string.bucket_width) + Utils.getMetriSimbol());
        textL4.setText(getResources().getString(R.string.Linkage_L4) + Utils.getMetriSimbol());

        int bucketMountPos = MyData.get_Int("M" + indexMachineSelected + "_Bucket_MountPos");
        switch (bucketMountPos) {
            case 0:
                off.setChecked(true);
                break;
            case 1:
                left.setChecked(true);
                break;
            case -1:
                right.setChecked(true);
                break;
            case 2:
                top.setChecked(true);
                break;
            case 3:
                topRev.setChecked(true);
                break;
        }
    }

    private void findView() {
        nameBucket = findViewById(R.id.bucket_ID);
        lengthBucket = findViewById(R.id.bucketLength);
        widthBucket = findViewById(R.id.bucketWidth);
        L4Bucket = findViewById(R.id.bucket_L4);
        nameBucket = findViewById(R.id.bucket_ID);
        esc = findViewById(R.id.exit);
        save = findViewById(R.id.save);
        offsetZeroMinus = findViewById(R.id.offsetMinus);
        offsetZero = findViewById(R.id.offsetZero);
        offsetZeroPlus = findViewById(R.id.offsetPlus);
        offsetZeroFlat = findViewById(R.id.offsetZeroFlat);
        textLength = findViewById(R.id.bkl);
        textWidth = findViewById(R.id.bwdt);
        textL4 = findViewById(R.id.l4);
        off = findViewById(R.id.cbxOff);
        left = findViewById(R.id.cbxLeft);
        right = findViewById(R.id.cbxRight);
        bucketAngle = findViewById(R.id.bucketAngle);
        bucketFlatAngle = findViewById(R.id.bucketAngleFlat);
        bucketAngleOffset = findViewById(R.id.bucketAngleOffset);
        bucketFlatAngleOffset = findViewById(R.id.bucketAngleFlatOffset);
        load = findViewById(R.id.load);
        infoAngolo = findViewById(R.id.infoAngolo);
        infoFlat = findViewById(R.id.infoFlat);
        top = findViewById(R.id.cbTop);
        topRev = findViewById(R.id.cbTopRev);

        off.setEnabled(false);
        off.setChecked(false);
        left.setEnabled(false);
        left.setChecked(false);
        right.setEnabled(false);
        right.setChecked(false);
        top.setEnabled(false);
        top.setChecked(false);
        topRev.setEnabled(false);
        topRev.setChecked(false);
    }

    private void onClick() {
        lengthBucket.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthBucket);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthBucket);
            }
        });

        widthBucket.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(widthBucket);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(widthBucket);
            }
        });

        L4Bucket.setOnClickListener((View v) -> {
            if(DataSaved.hasQuick==0&&DataSaved.L1>0) {
                if (indexMeasure == 4 || indexMeasure == 5) {
                    if (!numberDialogFtIn.dialog.isShowing())
                        numberDialogFtIn.show(L4Bucket);
                } else {
                    if (!numberDialog.dialog.isShowing())
                        numberDialog.show(L4Bucket);
                }
            }
        });

        nameBucket.setOnClickListener((View v) -> {
            if (!qwertyDialog.dialog.isShowing())
                qwertyDialog.show(nameBucket);
        });
        load.setOnClickListener((View v) -> {
            disableAll();
            Intent intent = new Intent(this, PickBucket.class);
            intent.putExtra("indexBucket", indexBucket);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });


        save.setOnClickListener((View v) -> {

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!lengthBucket.getText().toString().contains("'") || !widthBucket.getText().toString().contains("'") || !L4Bucket.getText().toString().contains("'")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    if (nameBucket.getText().toString().equals("")) {
                        new CustomToast(this, "Missing Name").show_alert();
                    } else {
                        disableAll();
                        save();
                        store();
                        startService(new Intent(this, UpdateValuesService.class));
                        startActivity(new Intent(this, BucketChooserActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                    }
                }
            } else {
                if (!(isNumeric(lengthBucket.getText().toString()) && isNumeric(widthBucket.getText().toString()) && isNumeric(L4Bucket.getText().toString()))) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    if (nameBucket.getText().toString().equals("")) {
                        new CustomToast(this, "Missing Name").show_alert();
                    } else {
                        disableAll();
                        save();
                        store();
                        startService(new Intent(this, UpdateValuesService.class));
                        startActivity(new Intent(this, BucketChooserActivity.class));
                        overridePendingTransition(0, 0);

                        finish();
                    }
                }
            }

        });

        esc.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, BucketChooserActivity.class));
            overridePendingTransition(0, 0);

            finish();

        });

        offsetZeroMinus.setOnClickListener((View v) -> {
            DataSaved.offsetBucket -= 0.1;
        });

        offsetZeroPlus.setOnClickListener((View v) -> {
            DataSaved.offsetBucket += 0.1;
        });

    }

    private void disableAll() {
        load.setEnabled(false);
        save.setEnabled(false);
        esc.setEnabled(false);
    }

    private void onLongClick() {
        offsetZero.setOnLongClickListener((View v) -> {
            offsetZero.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            if (DataSaved.L1 > 0) {
                DataSaved.offsetBucket = ExcavatorLib.bennaSimulata;
            } else {
                DataSaved.offsetBucket = Sensors_Decoder.Deg_bucket;
            }
            return true;
        });

        offsetZeroFlat.setOnLongClickListener((View v) -> {
            offsetZeroFlat.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));

                DataSaved.offsetFlat = ExcavatorLib.correctBucket;
                if (DataSaved.offsetFlat > -90) {
                    DataSaved.flat = 90 - Math.abs(DataSaved.offsetFlat);
                } else {
                    DataSaved.flat = Math.abs(DataSaved.offsetFlat) - 90;
                }
                Log.i("isWL",DataSaved.offsetFlat+"   "+DataSaved.flat);

            return true;
        });
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void updateUI() {


        bucketAngle.setText(((getResources().getString(R.string.bucket_angle) + String.format("%.2f", ExcavatorLib.correctBucket) + " °").replace(",", ".")));
        bucketAngleOffset.setText((String.format("%.2f", DataSaved.offsetBucket) + " °").replace(",", "."));
        bucketFlatAngle.setText(((getResources().getString(R.string.bucket_flat_angle) + String.format("%.2f", ExcavatorLib.correctFlat) + " °").replace(",", ".")));
        bucketFlatAngleOffset.setText((String.format("%.2f", DataSaved.offsetFlat) + " °").replace(",", "."));
        if (isNumeric(L4Bucket.getText().toString())) {
            DataSaved.L4 = Double.parseDouble(Utils.writeMetri(L4Bucket.getText().toString().trim()));
        } else {
            DataSaved.L4 = 0;
        }
        if (isNumeric(lengthBucket.getText().toString())) {
            DataSaved.L_Bucket = Double.parseDouble(Utils.writeMetri(lengthBucket.getText().toString().trim()));
        } else {
            DataSaved.L_Bucket = 0;
        }
        if (isNumeric(widthBucket.getText().toString())) {
            DataSaved.W_Bucket = Double.parseDouble(Utils.writeMetri(widthBucket.getText().toString().trim()));
        } else {
            DataSaved.W_Bucket = 0;
        }
        if (DataSaved.hasQuick == 1||DataSaved.L1<=0) {
            textL4.setAlpha(0.5f);
            L4Bucket.setAlpha(0.5f);
        } else {
            textL4.setAlpha(1f);
            L4Bucket.setAlpha(1f);
        }
    }


    private void save() {
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Name", nameBucket.getText().toString().trim().toUpperCase());
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Length", Utils.writeMetri(lengthBucket.getText().toString().trim()));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Width", Utils.writeMetri(widthBucket.getText().toString().trim()));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_L4", Utils.writeMetri(L4Bucket.getText().toString().trim()));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Offset", String.valueOf(DataSaved.offsetBucket));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Flat_Offset", String.valueOf(DataSaved.offsetFlat));
        MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Flat", String.valueOf(DataSaved.flat));
    }


    private void store() {
        String nameM = nameBucket.getText().toString().trim();
        String length = Utils.writeMetri(lengthBucket.getText().toString().trim());
        String width = Utils.writeMetri(widthBucket.getText().toString().trim());
        String L4 = Utils.writeMetri(L4Bucket.getText().toString().trim());
        String offset = String.valueOf(DataSaved.offsetBucket);
        String flatOffset = String.valueOf(DataSaved.offsetFlat);
        String flat = String.valueOf(DataSaved.flat);

        String path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Machine " + indexMachineSelected + "/Buckets";
        String fileName = nameM + ".csv";
        File f = new File(path, fileName);
        CSVWriter writer;

        try {
            writer = new CSVWriter(new FileWriter(f));

            String[] bucket = {nameM, length, width, L4, offset, flatOffset, flat};

            writer.writeNext(bucket);

            writer.close();
        } catch (Exception ignored) {
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onTouch() {
        infoAngolo.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                angolo.show();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                angolo.dialog.dismiss();
            }
            return false;
        });
        infoFlat.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                flat.show();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                flat.dialog.dismiss();
            }
            return false;
        });
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


