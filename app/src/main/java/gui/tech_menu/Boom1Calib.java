package gui.tech_menu;


import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_HiddenPin_Calc;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import services.UpdateValuesService;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class Boom1Calib extends AppCompatActivity {
    EditText lengthBoom1,extTxt;
    CheckBox off, left, right,isExt;
    TextView boom1Angle, boom1OffsetAngle, textBoom1,border,extValue;
    Button minusOffset, plusOffset, setOffset,setExt;
    ImageView save, esc;
    ImageView img_hiddenpin;
    boolean isPresse;

    int indexMachineSelected, count = 0;
    private boolean minusPressed, plusPressed;


    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure;
    Dialog_HiddenPin_Calc dialogHiddenPinCalc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boom1_calib);



        findView();
        init();
        onClick();
        onLongClick();
        onTouch();
        onCheckedChange();
        updateUI();


    }

    private void findView() {
        save = findViewById(R.id.save);
        esc = findViewById(R.id.exit);
        lengthBoom1 = findViewById(R.id.boom1Length);
        off = findViewById(R.id.cbxOff);
        left = findViewById(R.id.cbxLeft);
        right = findViewById(R.id.cbxRight);
        minusOffset = findViewById(R.id.offsetMinus);
        plusOffset = findViewById(R.id.offsetPlus);
        setOffset = findViewById(R.id.offsetSetZero);
        boom1Angle = findViewById(R.id.boom1Angle_tv);
        boom1OffsetAngle = findViewById(R.id.boom1OffsetAngle_tv);
        textBoom1 = findViewById(R.id.b1l);
        img_hiddenpin = findViewById(R.id.img_hiddenpin);
        border=findViewById(R.id.border);
        isExt = findViewById(R.id.isExt);
        extValue = findViewById(R.id.extTxt);
        setExt = findViewById(R.id.setExt);
        if(DataSaved.isWL==EXCAVATOR||DataSaved.isWL==DRILL){
            img_hiddenpin.setVisibility(View.VISIBLE);
        }else {
            img_hiddenpin.setVisibility(View.INVISIBLE);
        }
        if(DataSaved.isWL==DRILL){
            border.setVisibility(View.VISIBLE);
            isExt.setVisibility(View.VISIBLE);
            extValue.setVisibility(View.VISIBLE);
            setExt.setVisibility(View.VISIBLE);
        }else {
            border.setVisibility(View.GONE);
            isExt.setVisibility(View.GONE);
            extValue.setVisibility(View.GONE);
            setExt.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        isPresse = false;
        dialogHiddenPinCalc = new Dialog_HiddenPin_Calc(this);
        indexMachineSelected = MyData.get_Int( "MachineSelected");
        indexMeasure = MyData.get_Int("Unit_Of_Measure");

        if (indexMeasure == 4 || indexMeasure == 5) {
            numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
        } else {
            numberDialog = new CustomNumberDialog(this, -1);
        }

        lengthBoom1.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LengthBoom1")));
        textBoom1.setText(getResources().getString(R.string.units) + Utils.getMetriSimbol());

        int mountPos = MyData.get_Int("M" + indexMachineSelected + "_Boom1_MountPos");
        switch (mountPos) {
            case 0:
                off.setChecked(true);
                break;
            case 1:
                left.setChecked(true);
                break;
            case -1:
                right.setChecked(true);
        }
        isExt.setChecked(DataSaved.isExtensionBoom > 0);

    }

    private void onClick() {
        setExt.setOnLongClickListener(view -> {
            MyDeviceManager.CanWrite(true,0, 0x608, 8, new byte[]{0x23, 0x03, 0x60, 0, 0, 0, 0, 0});
            isPresse=true;
            return false;
        });
        isExt.setOnClickListener(view -> {
            DataSaved.isExtensionBoom += 1;
            DataSaved.isExtensionBoom = DataSaved.isExtensionBoom % 2;
            isExt.setChecked(DataSaved.isExtensionBoom > 0);
        });
        img_hiddenpin.setOnClickListener(view -> {
            if (!dialogHiddenPinCalc.dialog.isShowing()) {
                dialogHiddenPinCalc.show();
            }
        });
        save.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!lengthBoom1.getText().toString().contains("'")) {
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
                if (!lengthBoom1.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
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

        esc.setOnClickListener((View v) -> {
            esc.setEnabled(false);
            save.setEnabled(false);
            startService(new Intent(getApplicationContext(), UpdateValuesService.class));
            startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
            finish();
        });

        minusOffset.setOnClickListener((View v) -> {
            DataSaved.offsetBoom1 -= 0.05;

        });

        plusOffset.setOnClickListener((View v) -> {
            DataSaved.offsetBoom1 += 0.05;

        });

        lengthBoom1.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthBoom1);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthBoom1);
            }
        });


    }

    @SuppressLint("ClickableViewAccessibility")
    private void onTouch() {
        plusOffset.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    plusPressed = true;
                    count = 0;
                    return false;


                case MotionEvent.ACTION_UP:
                    plusPressed = false;
                    count = 0;
                    return false;
            }
            return false;
        });
        minusOffset.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    minusPressed = true;
                    count = 0;
                    return false;

                case MotionEvent.ACTION_UP:
                    minusPressed = false;
                    count = 0;
                    return false;
            }
            return false;
        });

    }

    private void onLongClick() {
        setOffset.setOnLongClickListener((View v) -> {
            setOffset.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offsetBoom1 = Sensors_Decoder.Deg_boom1;
            return true;
        });

    }

    private void onCheckedChange() {
        off.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (off.isChecked()) {
                DataSaved.lrBoom1 = 0;
                left.setChecked(false);
                right.setChecked(false);
            }
        });

        left.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (left.isChecked()) {
                DataSaved.lrBoom1 = 1;
                off.setChecked(false);
                right.setChecked(false);
            }
        });

        right.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (right.isChecked()) {
                DataSaved.lrBoom1 = -1;
                off.setChecked(false);
                left.setChecked(false);
            }
        });

    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {


        if (plusPressed) {
            count++;
            if (count > 15) {
                DataSaved.offsetBoom1 += 0.1;
            }
        }
        if (minusPressed) {
            count++;
            if (count > 15) {
                DataSaved.offsetBoom1 -= 0.1;
            }
        }

        if (Math.abs(ExcavatorLib.correctBoom1) <= 45.1d) {
            boom1Angle.setText(String.format("%.02f", ExcavatorLib.correctBoom1).replace(",", ".") + " (" + String.format("%.2f", (Math.tan(Math.toRadians(ExcavatorLib.correctBoom1)) * 100)) + "%)");
        } else {
            boom1Angle.setText(String.format("%.02f", ExcavatorLib.correctBoom1).replace(",", ".") + " (" + "--.--" + "%)");

        }
        boom1OffsetAngle.setText(String.format("%.02f", DataSaved.offsetBoom1).replace(",", "."));
        extValue.setText(Utils.readSensorCalibration(String.valueOf(Sensors_Decoder.ExtensionBoom)));


    }

    private void save() {
        int mounPos = 0;

        if (left.isChecked()) {
            mounPos = 1;
        }
        if (right.isChecked()) {
            mounPos = -1;
        }
        DataSaved.lrBoom1 = mounPos;
        MyData.push("M" + indexMachineSelected + "_Boom1_MountPos", String.valueOf(mounPos));
        MyData.push("M" + indexMachineSelected + "_OffsetBoom1", String.valueOf(DataSaved.offsetBoom1));
        MyData.push("M" + indexMachineSelected + "_LengthBoom1", Utils.writeMetri(lengthBoom1.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_isExt", String.valueOf(DataSaved.isExtensionBoom));

        if(isPresse){
            MyDeviceManager.CanWrite(true,0,0x608,8,new byte[]{0x23,0x10,0x10,0x01,0x73,0x61,0x76,0x65});
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


