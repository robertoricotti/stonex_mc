package gui.tech_menu;

import static services.CanService.BraccioCon;
import static services.CanService.CarroCon;
import static utils.MyTypes.DREDGE;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.LIEBHERR_CRANE;
import static utils.MyTypes.SENNEBOGHEN;
import static utils.MyTypes.STONEX_SENSORS;

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
import gui.dialogs_and_toast.Dialog_HiddenPin_Calc;
import packexcalib.exca.DataSaved;
import packexcalib.exca.DredgeLib;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.exca.Sensors_Decoder_Dredge;
import services.UpdateValuesService;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class Boom_Drg_Activity extends BaseClass {
    EditText lengthBoom1;
    CheckBox off, left, right;
    TextView boom1Angle, boom1OffsetAngle, textBoom1, border,headerr;
    Button minusOffset, plusOffset, setOffset;
    ImageView save, esc;
    ImageView img_hiddenpin,stato;


    int indexMachineSelected, count = 0;
    private boolean minusPressed, plusPressed;


    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure;
    Dialog_HiddenPin_Calc dialogHiddenPinCalc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boom_drg);

        findView();
        init();
        onClick();
        onLongClick();
        onTouch();
        onCheckedChange();
        updateUI();


    }

    private void findView() {
        headerr=findViewById(R.id.headerr);
        stato=findViewById(R.id.stato);
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
        border = findViewById(R.id.border);

        if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == DRILL||DataSaved.isWL==DREDGE) {
            img_hiddenpin.setVisibility(View.VISIBLE);
        } else {
            img_hiddenpin.setVisibility(View.INVISIBLE);
        }

    }

    @SuppressLint("SetTextI18n")
    private void init() {
        switch (DataSaved.Dredge_Interface_Type) {
            case STONEX_SENSORS:
                headerr.setText(getResources().getString(R.string.boom1_calibration) + " - id: 0x382h");
                break;
            case SENNEBOGHEN:
                headerr.setText(getResources().getString(R.string.boom1_calibration) + " - id: 0x1C034333h");
                break;

            case LIEBHERR_CRANE:
                headerr.setText(getResources().getString(R.string.boom1_calibration) + " - id: 0x18FF8380h");
                break;


        }

        dialogHiddenPinCalc = new Dialog_HiddenPin_Calc(this);
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexMeasure = MyData.get_Int("Unit_Of_Measure");


        numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);

        numberDialog = new CustomNumberDialog(this, -1);


        lengthBoom1.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "Lunghezza_Braccio")));
        textBoom1.setText(getResources().getString(R.string.units) + Utils.getMetriSimbol());

        int mountPos = MyData.get_Int("M" + indexMachineSelected + "Pos_BOOM");
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

    }

    private void onClick() {

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
        //+++++

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
            DataSaved.offsetBoom1 = Sensors_Decoder_Dredge.Angolo_Braccio_Dredge;
            return true;
        });

    }

    private void onCheckedChange() {
        off.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (off.isChecked()) {
                DataSaved.Pos_BOOM = 0;
                left.setChecked(false);
                right.setChecked(false);
            }
        });

        left.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (left.isChecked()) {
                DataSaved.Pos_BOOM = 1;
                off.setChecked(false);
                right.setChecked(false);
            }
        });

        right.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (right.isChecked()) {
                DataSaved.Pos_BOOM = -1;
                off.setChecked(false);
                left.setChecked(false);
            }
        });

    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {
        if (BraccioCon) {
            stato.setImageResource(R.drawable.sfondo_bottone_selezionato);
        } else {
            stato.setImageResource(R.drawable.sfondo_auto_enabled);
        }
        if (minusPressed && plusPressed) {
            count++;
            if (count > 40) {
                DataSaved.offsetBoom1 = 0;
            }
        }

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

        if (Math.abs(DredgeLib.correctDredgeBoom) <= 45.1d) {
            boom1Angle.setText(String.format("%.02f", DredgeLib.correctDredgeBoom).replace(",", ".") + " (" + String.format("%.2f", (Math.tan(Math.toRadians(DredgeLib.correctDredgeBoom)) * 100)) + "%)");
        } else {
            boom1Angle.setText(String.format("%.02f", DredgeLib.correctDredgeBoom).replace(",", ".") + " (" + "--.--" + "%)");

        }
        boom1OffsetAngle.setText(String.format("%.02f", DataSaved.offsetBoom1).replace(",", "."));


    }

    private void save() {
        int mounPos = 0;

        if (left.isChecked()) {
            mounPos = 1;
        }
        if (right.isChecked()) {
            mounPos = -1;
        }
        DataSaved.Pos_BOOM = mounPos;
        MyData.push("M" + indexMachineSelected + "Pos_BOOM", String.valueOf(mounPos));
        MyData.push("M" + indexMachineSelected + "_OffsetBoom1", String.valueOf(DataSaved.offsetBoom1));
        MyData.push("M" + indexMachineSelected + "Lunghezza_Braccio", Utils.writeMetri(lengthBoom1.getText().toString()));


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


