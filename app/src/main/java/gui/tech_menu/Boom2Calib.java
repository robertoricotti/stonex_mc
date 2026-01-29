package gui.tech_menu;

import static gui.MyApp.isApollo;

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

import androidx.appcompat.app.AppCompatActivity;
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
public class Boom2Calib extends BaseClass {
    EditText lengthBoom2;
    CheckBox off, left, right;
    TextView boom2Angle, boom2OffsetAngle, textBoom2;
    Button minusOffset, plusOffset, setOffset;
    ImageView save, esc;
    int indexMachineSelected, count = 0;
    private boolean minusPressed, plusPressed;


    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boom2_calib);

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
        lengthBoom2 = findViewById(R.id.boom2Length);
        off = findViewById(R.id.cbxOff);
        left = findViewById(R.id.cbxLeft);
        right = findViewById(R.id.cbxRight);
        minusOffset = findViewById(R.id.offsetMinus);
        plusOffset = findViewById(R.id.offsetPlus);
        setOffset = findViewById(R.id.offsetSetZero);
        boom2Angle = findViewById(R.id.boom2Angle_tv);
        boom2OffsetAngle = findViewById(R.id.boom2OffsetAngle_tv);
        textBoom2 = findViewById(R.id.b2l);
    }


    @SuppressLint("SetTextI18n")
    private void init() {


        indexMachineSelected = MyData.get_Int("MachineSelected");

        indexMeasure = MyData.get_Int("Unit_Of_Measure");

        if(indexMeasure == 4 || indexMeasure == 5){
            numberDialogFtIn = new CustomNumberDialogFtIn(this,-1);
        }
        else {
            numberDialog = new CustomNumberDialog(this,-1);
        }

        lengthBoom2.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LengthBoom2")));
        textBoom2.setText(getResources().getString(R.string.units) + Utils.getMetriSimbol());

        int mountPos = MyData.get_Int("M" + indexMachineSelected + "_Boom2_MountPos");
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

    @SuppressLint("DefaultLocale")
    public void updateUI() {

                    if (plusPressed) {
                        count++;
                        if (count > 15) {
                            DataSaved.offsetBoom2 += 0.1;
                        }
                    }
                    if (minusPressed) {
                        count++;
                        if (count > 15) {
                            DataSaved.offsetBoom2 -= 0.1;
                        }
                    }

                            if(Math.abs(ExcavatorLib.correctBoom2)<=45.1d){
                                boom2Angle.setText(String.format("%.02f", ExcavatorLib.correctBoom2).replace(",", ".")+" ("+String.format("%.2f",(Math.tan(Math.toRadians(ExcavatorLib.correctBoom2))*100) )+"%)");}
                            else{
                                boom2Angle.setText(String.format("%.02f", ExcavatorLib.correctBoom2).replace(",", ".")+" ("+"--.--"+"%)");

                            }
                            boom2OffsetAngle.setText(String.format("%.02f", DataSaved.offsetBoom2).replace(",", "."));





    }

    private void save() {
        int mounPos = 0;
        if (left.isChecked()) {
            mounPos = 1;
        }
        if (right.isChecked()) {
            mounPos = -1;
        }
        DataSaved.lrBoom2 = mounPos;
        MyData.push("M" + indexMachineSelected + "_Boom2_MountPos", String.valueOf(mounPos));
        MyData.push("M" + indexMachineSelected + "_OffsetBoom2", String.valueOf(DataSaved.offsetBoom2));
        MyData.push("M" + indexMachineSelected + "_LengthBoom2", Utils.writeMetri(lengthBoom2.getText().toString()));
    }

    private void onClick() {
        save.setOnClickListener((View v) -> {
            if(indexMeasure == 4 || indexMeasure == 5){
                if(!lengthBoom2.getText().toString().contains("'")){
                    new CustomToast(this,"INPUT ERROR!!!").show_error();
                }
                else {
                    esc.setEnabled(false);
                    save.setEnabled(false);
                    save();
                    startService(new Intent(getApplicationContext(), UpdateValuesService.class));
                    startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
                    finish();
                }
            }
            else{
                if (!lengthBoom2.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
                    new CustomToast(this,"INPUT ERROR!!!").show_error();

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
            DataSaved.offsetBoom2 -= 0.05;
        });

        plusOffset.setOnClickListener((View v) -> {
            DataSaved.offsetBoom2 += 0.05;
        });

        lengthBoom2.setOnClickListener((View v) -> {
            if(indexMeasure == 4 || indexMeasure == 5){
                if(!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(lengthBoom2);
            }
            else{
                if(!numberDialog.dialog.isShowing())
                    numberDialog.show(lengthBoom2);
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
            DataSaved.offsetBoom2 = Sensors_Decoder.Deg_boom2;
            return true;
        });
    }

    private void onCheckedChange() {
        off.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (off.isChecked()) {
                DataSaved.lrBoom2 = 0;
                left.setChecked(false);
                right.setChecked(false);
            }
        });

        left.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (left.isChecked()) {
                DataSaved.lrBoom2 = 1;
                off.setChecked(false);
                right.setChecked(false);
            }
        });

        right.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (right.isChecked()) {
                DataSaved.lrBoom2 = -1;
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





