package gui.tech_menu;

import static packexcalib.exca.Offset_Applier.dbAlert;
import static utils.Utils.isNumeric;

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


public class LinkageCalib extends BaseClass {
    EditText lengthL1, lengthL2, lengthL3, lengthL4;
    CheckBox off, left, right, cbtop, cbtopRev;
    TextView dogBoneAngle, dogBoneOffsetAngle, textL1, textL2, textL3, textL4;
    Button minusOffset, plusOffset, setOffset;
    ImageView save, esc;
    CheckBox hasQuick;
    int indexMachineSelected;


    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkage_calib);


        findView();
        init();
        onClick();
        onLongClick();
        onCheckedChanged();
        updateUI();

    }

    private void findView() {
        save = findViewById(R.id.save);
        esc = findViewById(R.id.exit);
        lengthL1 = findViewById(R.id.L1Length);
        lengthL2 = findViewById(R.id.L2Length);
        lengthL3 = findViewById(R.id.L3Length);
        off = findViewById(R.id.cbxOff);
        left = findViewById(R.id.cbxLeft);
        right = findViewById(R.id.cbxRight);
        cbtop = findViewById(R.id.cbxTop);
        cbtopRev = findViewById(R.id.cbxTopRev);
        minusOffset = findViewById(R.id.offsetMinus);
        plusOffset = findViewById(R.id.offsetPlus);
        setOffset = findViewById(R.id.offsetSetZero);
        dogBoneAngle = findViewById(R.id.dogBoneAngle_tv);
        dogBoneOffsetAngle = findViewById(R.id.dogBoneOffsetAngle_tv);
        textL1 = findViewById(R.id.tl1);
        textL2 = findViewById(R.id.tl2);
        textL3 = findViewById(R.id.tl3);
        hasQuick = findViewById(R.id.hasQuick);
        textL4 = findViewById(R.id.L4);
        lengthL4 = findViewById(R.id.L4Length);
    }

    @SuppressLint("SetTextI18n")
    private void init() {


        indexMachineSelected = MyData.get_Int("MachineSelected");

        indexMeasure = MyData.get_Int("Unit_Of_Measure");


        numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);

        numberDialog = new CustomNumberDialog(this, -1);


        lengthL1.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LengthL1")));
        lengthL2.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LengthL2")));
        lengthL3.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LengthL3")));
        textL1.setText("LENGTH L1 " + Utils.getMetriSimbol());
        textL2.setText("LENGTH L2 " + Utils.getMetriSimbol());
        textL3.setText("LENGTH L3 " + Utils.getMetriSimbol());
        textL4.setText("L4 " + Utils.getMetriSimbol());
        int in = MyData.get_Int("M" + indexMachineSelected + "_hasQuick");


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
                cbtop.setChecked(true);
                break;
            case 3:
                cbtopRev.setChecked(true);
                break;
        }
        hasQuick.setChecked(in == 1);
        if (in == 1) {
            lengthL4.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 1 + "_L4")));

        }

    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void updateUI() {

        if (hasQuick.isChecked()) {
            textL4.setVisibility(View.VISIBLE);
            lengthL4.setVisibility(View.VISIBLE);
        } else {
            textL4.setVisibility(View.INVISIBLE);
            lengthL4.setVisibility(View.INVISIBLE);
        }

        dogBoneAngle.setText((String.format("%.02f", ExcavatorLib.correctDBStickAngle) + " °").replace(",", "."));
        dogBoneOffsetAngle.setText(String.format("%.02f", DataSaved.offsetDogBone).replace(",", "."));
        if (isNumeric(lengthL1.getText().toString())) {
            DataSaved.L1 = Float.parseFloat(Utils.writeMetri(lengthL1.getText().toString().trim()));
            if (DataSaved.L1 > 0) {
                hasQuick.setVisibility(View.VISIBLE);
            } else {
                hasQuick.setVisibility(View.INVISIBLE);
            }
        } else {
            DataSaved.L1 = 0;
        }
        if (isNumeric(lengthL2.getText().toString())) {
            DataSaved.L2 = Float.parseFloat(Utils.writeMetri(lengthL2.getText().toString().trim()));
        } else {
            DataSaved.L2 = 0;
        }
        if (isNumeric(lengthL3.getText().toString())) {
            DataSaved.L3 = Float.parseFloat(Utils.writeMetri(lengthL3.getText().toString().trim()));
        } else {
            DataSaved.L3 = 0;
        }
        if (isNumeric(lengthL4.getText().toString())) {
            DataSaved.L4 = Float.parseFloat(Utils.writeMetri(lengthL4.getText().toString().trim()));
        } else {
            DataSaved.L4 = 0;
        }
        if (DataSaved.L1 != 0) {
            lengthL1.setVisibility(View.VISIBLE);
            lengthL2.setVisibility(View.VISIBLE);
            lengthL3.setVisibility(View.VISIBLE);
            dogBoneOffsetAngle.setVisibility(View.VISIBLE);
            dogBoneAngle.setVisibility(View.VISIBLE);
            minusOffset.setVisibility(View.VISIBLE);
            plusOffset.setVisibility(View.VISIBLE);
            setOffset.setVisibility(View.VISIBLE);

        } else {
            lengthL1.setVisibility(View.VISIBLE);
            lengthL2.setVisibility(View.INVISIBLE);
            lengthL3.setVisibility(View.INVISIBLE);
            dogBoneOffsetAngle.setVisibility(View.INVISIBLE);
            dogBoneAngle.setVisibility(View.INVISIBLE);
            minusOffset.setVisibility(View.INVISIBLE);
            plusOffset.setVisibility(View.INVISIBLE);
            setOffset.setVisibility(View.INVISIBLE);
        }


    }

    private void save() {
        int mounPos = 0;

        if (left.isChecked()) {
            mounPos = 1;
        }
        if (right.isChecked()) {
            mounPos = -1;
        }
        if (cbtop.isChecked()) {
            mounPos = 2;
        }
        if (cbtopRev.isChecked()) {
            mounPos = 3;
        }
        int isQuick = 0;
        if (hasQuick.isChecked()) {
            isQuick = 1;
        } else {
            isQuick = 0;
        }
        DataSaved.hasQuick = isQuick;
        DataSaved.lrBucket = mounPos;
        MyData.push("M" + indexMachineSelected + "_Bucket_MountPos", String.valueOf(mounPos));
        MyData.push("M" + indexMachineSelected + "_OffsetDB", String.valueOf(DataSaved.offsetDogBone));
        MyData.push("M" + indexMachineSelected + "_LengthL1", Utils.writeMetri(lengthL1.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_LengthL2", Utils.writeMetri(lengthL2.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_LengthL3", Utils.writeMetri(lengthL3.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_hasQuick", String.valueOf(DataSaved.hasQuick));
        if (DataSaved.hasQuick == 1) {
            for (int i = 1; i <= 4; i++) {
                for (int j = 1; j <= 20; j++) {
                    MyData.push("M" + i + "_Bucket_" + j + "_L4", Utils.writeMetri(lengthL4.getText().toString()));

                }
            }
        }

    }

    private void onClick() {
        save.setOnClickListener((View v) -> {
            if (dbAlert) {
                new CustomToast(this, "CHECK BUCKET CALIBRATION" + "\n" + "OR CHECK L1 L2 L3").show_alert();

            }

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!lengthL1.getText().toString().contains("'") || !lengthL2.getText().toString().contains("'") || !lengthL3.getText().toString().contains("'")) {
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
                if (!(lengthL1.getText().toString().matches("-?\\d+(\\.\\d+)?") && lengthL2.getText().toString().matches("-?\\d+(\\.\\d+)?") && lengthL3.getText().toString().matches("-?\\d+(\\.\\d+)?"))) {
                    new CustomToast(this, "INPUT ERROR!!!" + "\n" + "CHECK L1 L2 L3").show_alert();
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

        esc.setOnClickListener((View v) -> {
            esc.setEnabled(false);
            save.setEnabled(false);
            startService(new Intent(getApplicationContext(), UpdateValuesService.class));
            startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
            finish();
        });

        minusOffset.setOnClickListener((View v) -> {
            DataSaved.offsetDogBone -= 0.05;
        });

        plusOffset.setOnClickListener((View v) -> {
            DataSaved.offsetDogBone += 0.05;
        });

        lengthL1.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthL1);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthL1);
            }
        });

        lengthL2.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthL2);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthL2);
            }
        });

        lengthL3.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthL3);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthL3);
            }
        });
        lengthL4.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthL4);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthL4);
            }
        });


    }

    private void onLongClick() {
        setOffset.setOnLongClickListener((View v) -> {
            setOffset.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offsetDogBone = 90.0 - (Sensors_Decoder.Deg_bucket - ExcavatorLib.correctStick);
            return true;
        });
    }

    private void onCheckedChanged() {
        off.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (off.isChecked()) {
                DataSaved.lrBucket = 0;
                left.setChecked(false);
                right.setChecked(false);
                cbtop.setChecked(false);
                cbtopRev.setChecked(false);
            }
        });

        left.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (left.isChecked()) {
                DataSaved.lrBucket = 1;
                off.setChecked(false);
                right.setChecked(false);
                cbtop.setChecked(false);
                cbtopRev.setChecked(false);
            }
        });

        right.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (right.isChecked()) {
                DataSaved.lrBucket = -1;
                off.setChecked(false);
                left.setChecked(false);
                cbtop.setChecked(false);
                cbtopRev.setChecked(false);
            }
        });
        cbtop.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbtop.isChecked()) {
                DataSaved.lrBucket = 2;
                left.setChecked(false);
                right.setChecked(false);
                off.setChecked(false);
                cbtopRev.setChecked(false);
            }
        });
        cbtopRev.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbtopRev.isChecked()) {
                DataSaved.lrBucket = 3;
                left.setChecked(false);
                right.setChecked(false);
                off.setChecked(false);
                cbtop.setChecked(false);
            }
        });

        hasQuick.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (hasQuick.isChecked()) {
                DataSaved.hasQuick = 1;
                lengthL4.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 1 + "_L4")));

            }
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





