package gui.tech_menu;

import static utils.MyTypes.WHEELLOADER;

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


public class StickCalib extends BaseClass {
    EditText lengthStick, laserV, laserH, extValue;
    CheckBox off, left, right, isExt;
    TextView stickAngle, stickOffsetAngle, textLength, textLV, textLH,titolao;
    Button minusOffset, plusOffset, setOffset, setExt;
    ImageView save, esc;

    int indexMachineSelected, count = 0;


    private boolean minusPressed, plusPressed;
    boolean isPresse;

    //ImageButton mountButton;

    //PopupImageDialog mount;

    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stick_calib);

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
        laserV = findViewById(R.id.laserV);
        laserH = findViewById(R.id.laserH);
        textLength = findViewById(R.id.sl);
        textLV = findViewById(R.id.slv);
        textLH = findViewById(R.id.slh);
        isExt = findViewById(R.id.isExt);
        extValue = findViewById(R.id.extTxt);
        setExt = findViewById(R.id.setExt);
        titolao=findViewById(R.id.titolao);
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        if(DataSaved.isWL==WHEELLOADER){
            titolao.setText("MAIN BOOM");
        }
        isPresse = false;
        //mount = new PopupImageDialog(this, R.drawable.view);

        indexMachineSelected = MyData.get_Int("MachineSelected");

        indexMeasure = MyData.get_Int("Unit_Of_Measure");


            numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);

            numberDialog = new CustomNumberDialog(this, -1);


        lengthStick.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LengthStick")));
        laserV.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LaserVStick")));
        laserH.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_LaserHStick")));

        textLength.setText(getResources().getString(R.string.units) + Utils.getMetriSimbol());
        textLV.setText("LASER VERTICAL " + Utils.getMetriSimbol());
        textLH.setText("LASER HORIZONTAL " + Utils.getMetriSimbol());

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

        isExt.setChecked(DataSaved.isExtensionBoom > 0);

    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {
        if (minusPressed && plusPressed) {
            count++;
            if(count>40) {
                DataSaved.offsetStick = 0;
            }
        }

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

        if (Math.abs(ExcavatorLib.correctStick) <= 45.1d) {
            stickAngle.setText(String.format("%.02f", ExcavatorLib.correctStick).replace(",", ".") + " (" + String.format("%.2f", (Math.tan(Math.toRadians(ExcavatorLib.correctStick)) * 100)) + "%)");
        } else {
            stickAngle.setText(String.format("%.02f", ExcavatorLib.correctStick).replace(",", ".") + " (" + "--.--" + "%)");

        }
        stickOffsetAngle.setText(String.format("%.02f", DataSaved.offsetStick).replace(",", "."));


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
        DataSaved.lrStick = mounPos;
        MyData.push("M" + indexMachineSelected + "_Stick_MountPos", String.valueOf(mounPos));
        MyData.push("M" + indexMachineSelected + "_OffsetStick", String.valueOf(DataSaved.offsetStick));
        MyData.push("M" + indexMachineSelected + "_LengthStick", Utils.writeMetri(lengthStick.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_LaserVStick", Utils.writeMetri(laserV.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_LaserHStick", Utils.writeMetri(laserH.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_isExt", String.valueOf(DataSaved.isExtensionBoom));
        if(isPresse){
            MyDeviceManager.CanWrite(true,0,0x608,8,new byte[]{0x23,0x10,0x10,0x01,0x73,0x61,0x76,0x65});
        }
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
        save.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!lengthStick.getText().toString().contains("'") || !laserV.getText().toString().contains("'") || !laserH.getText().toString().contains("'")) {
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
                if (!(lengthStick.getText().toString().matches("-?\\d+(\\.\\d+)?") && laserV.getText().toString().matches("-?\\d+(\\.\\d+)?") && laserH.getText().toString().matches("-?\\d+(\\.\\d+)?"))) {
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

        laserV.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(laserV);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(laserV);
            }
        });

        laserH.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(laserH);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(laserH);
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












