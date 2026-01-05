package gui.tech_menu;

import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import utils.MyDeviceManager;
import utils.Utils;

public class MastLinkCalib extends BaseClass {
    EditText lengthStick,boomOffset;
    CheckBox off, left, right;
    TextView stickAngle, stickOffsetAngle, textLength,titolao,toolBoomDelta;
    Button minusOffset, plusOffset, setOffset;
    ImageView save, esc;
    int indexMachineSelected, count = 0;
    private boolean minusPressed, plusPressed;
    boolean isPresse;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;
    int indexMeasure = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mast_link_calib);
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
        lengthStick = findViewById(R.id.stickLength);
        off = findViewById(R.id.cbxOff);
        left = findViewById(R.id.cbxLeft);
        right = findViewById(R.id.cbxRight);
        minusOffset = findViewById(R.id.offsetMinus);
        plusOffset = findViewById(R.id.offsetPlus);
        setOffset = findViewById(R.id.offsetSetZero);
        stickAngle = findViewById(R.id.stickAngle_tv);
        stickOffsetAngle = findViewById(R.id.stickOffsetAngle_tv);
        textLength = findViewById(R.id.sl);
        titolao=findViewById(R.id.titolao);
        toolBoomDelta=findViewById(R.id.toolBoomDelta);
        boomOffset=findViewById(R.id.boomOffset);
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        try {

            isPresse = false;

            indexMachineSelected = MyData.get_Int("MachineSelected");

            indexMeasure = MyData.get_Int("Unit_Of_Measure");

            if (indexMeasure == 4 || indexMeasure == 5) {
                numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
            } else {
                numberDialog = new CustomNumberDialog(this, -1);
            }

            lengthStick.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LengthStick")));
            boomOffset.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "offset_Boom_Tool")));


            textLength.setText(getResources().getString(R.string.units) + Utils.getMetriSimbol());
            toolBoomDelta.setText(getResources().getString(R.string.units) + Utils.getMetriSimbol());


            int mountPos = MyData.get_Int("M" + indexMachineSelected + "_Stick_MountPos");
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
        } catch (Exception e) {

        }


    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {

        if (plusPressed) {
            count++;
            if (count > 15) {
                DataSaved.offsetStick += 0.1;
            }
        }
        if (minusPressed) {
            count++;
            if (count > 15) {
                DataSaved.offsetStick -= 0.1;
            }
        }

        if (Math.abs(ExcavatorLib.correctMastLink) <= 45.1d) {
            stickAngle.setText(String.format("%.02f", ExcavatorLib.correctMastLink).replace(",", ".") + " (" + String.format("%.2f", (Math.tan(Math.toRadians(ExcavatorLib.correctMastLink)) * 100)) + "%)");
        } else {
            stickAngle.setText(String.format("%.02f", ExcavatorLib.correctMastLink).replace(",", ".") + " (" + "--.--" + "%)");

        }
        stickOffsetAngle.setText(String.format("%.02f", DataSaved.offsetStick).replace(",", "."));




    }

    private void save() {
        int mounPos = 0;
        if (left.isChecked()) {
            mounPos = 1;
        }
        if (right.isChecked()) {
            mounPos = -1;
        }
        DataSaved.lrStick = mounPos;
        DataSaved.L_Stick=Double.parseDouble(Utils.writeMetri(lengthStick.getText().toString()));
        DataSaved.offset_Boom_Tool=Double.parseDouble(Utils.writeMetri(boomOffset.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_Stick_MountPos", String.valueOf(mounPos));
        MyData.push("M" + indexMachineSelected + "_OffsetStick", String.valueOf(DataSaved.offsetStick));
        MyData.push("M" + indexMachineSelected + "_LengthStick", Utils.writeMetri(lengthStick.getText().toString()));
        MyData.push("M" + indexMachineSelected + "offset_Boom_Tool", Utils.writeMetri(boomOffset.getText().toString()));

    }

    private void onClick() {


        save.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!lengthStick.getText().toString().contains("'")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    save.setEnabled(false);
                    esc.setEnabled(false);
                    save();
                    startService(new Intent(this, UpdateValuesService.class));
                    startActivity(new Intent(this, Nuova_Machine_Settings.class));
                    finish();
                }
            } else {
                if (!(lengthStick.getText().toString().matches("-?\\d+(\\.\\d+)?"))) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    save.setEnabled(false);
                    esc.setEnabled(false);
                    save();
                    startService(new Intent(this, UpdateValuesService.class));
                    startActivity(new Intent(this, Nuova_Machine_Settings.class));
                    finish();
                }
            }
        });

        esc.setOnClickListener((View v) -> {
            save.setEnabled(false);
            esc.setEnabled(false);
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });

        minusOffset.setOnClickListener((View v) -> {
            DataSaved.offsetStick -= 0.05;
        });

        plusOffset.setOnClickListener((View v) -> {
            DataSaved.offsetStick += 0.05;
        });

        lengthStick.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthStick);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthStick);
            }
        });

        boomOffset.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(boomOffset);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(boomOffset);
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
            DataSaved.offsetStick = Sensors_Decoder.Deg_stick;
            return true;
        });
    }

    private void onCheckedChanged() {
        off.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (off.isChecked()) {
                DataSaved.lrStick = 0;
                left.setChecked(false);
                right.setChecked(false);
            }
        });

        left.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (left.isChecked()) {
                DataSaved.lrStick = 1;
                off.setChecked(false);
                right.setChecked(false);
            }
        });

        right.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (right.isChecked()) {
                DataSaved.lrStick = -1;
                off.setChecked(false);
                left.setChecked(false);
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