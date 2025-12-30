package gui.tech_menu;

import static packexcalib.exca.Sensors_Decoder_Drill.RopeLen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import services.UpdateValuesService;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class DrillEncoder extends BaseClass {
    TextView b2l, distVal;
    CheckBox ckOff, ckClock, ckRev;
    EditText diamVal;
    Button offsetSetZero;
    ImageView save, exit;
    int indexMachineSelected;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;
    int indexMeasure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_encoder);
        findView();
        init();
        onClick();
        onCheckedChange();
        updateUI();
    }

    private void findView() {
        b2l=findViewById(R.id.b2l);
        distVal=findViewById(R.id.distVal);
        diamVal=findViewById(R.id.diamVal);
        ckOff=findViewById(R.id.cbxOff);
        ckClock=findViewById(R.id.cbxLeft);
        ckRev=findViewById(R.id.cbxRight);
        offsetSetZero=findViewById(R.id.offsetSetZero);
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit);

    }

    private void init() {
        try {
            indexMachineSelected = MyData.get_Int("MachineSelected");
            indexMeasure = MyData.get_Int("Unit_Of_Measure");

            if (indexMeasure == 4 || indexMeasure == 5) {
                numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
            } else {
                numberDialog = new CustomNumberDialog(this, -1);
            }

            diamVal.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "Rotary_Diam")));
            b2l.setText("WHEEL DIAMETER " + Utils.getMetriSimbol());
            int mountPos = MyData.get_Int("M" + indexMachineSelected + "Rotary_Mount");
            switch (mountPos) {
                case 0:
                    ckOff.setChecked(true);
                    break;
                case 1:
                    ckClock.setChecked(true);
                    break;
                case -1:
                    ckRev.setChecked(true);
            }

        } catch (Exception e) {

        }
    }

    private void onClick() {
        offsetSetZero.setOnLongClickListener(view -> {
            //
            MyDeviceManager.CanWrite(true,0,0x610,8,new byte[]{0x23,0x03,0x60,0,0,0,0,0});
            MyDeviceManager.CanWrite(true,0,0x610,8,new byte[]{0x23,0x10,0x10,0x01,0x73,0x61,0x76,0x65});
             return true;
        });
        save.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!diamVal.getText().toString().contains("'")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    exit.setEnabled(false);
                    save.setEnabled(false);
                    save();
                    startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
                    finish();
                }
            } else {
                if (!diamVal.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    exit.setEnabled(false);
                    save.setEnabled(false);
                    save();
                    startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
                    finish();
                }
            }
        });

        exit.setOnClickListener((View v) -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
            finish();
        });
        diamVal.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(diamVal);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(diamVal);
            }
        });
    }
    private void onCheckedChange() {
        ckOff.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (ckOff.isChecked()) {
                DataSaved.lrRotary = 0;
                ckClock.setChecked(false);
                ckRev.setChecked(false);
            }
        });

        ckClock.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (ckClock.isChecked()) {
                DataSaved.lrRotary = 1;
                ckOff.setChecked(false);
                ckRev.setChecked(false);
            }
        });

        ckRev.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (ckRev.isChecked()) {
                DataSaved.lrRotary = -1;
                ckOff.setChecked(false);
                ckClock.setChecked(false);
            }
        });

    }

    public void updateUI() {
        try {
            distVal.setText(Utils.readSensorCalibration(String.valueOf(RopeLen)));
        }catch (Exception e){
            distVal.setText("Error");
        };

    }

    private void save() {
        int mounPos = 0;

        if (ckClock.isChecked()) {
            mounPos = 1;
        }
        if (ckRev.isChecked()) {
            mounPos = -1;
        }
        DataSaved.lrRotary = mounPos;
        MyData.push("M" + indexMachineSelected + "Rotary_Mount", String.valueOf(mounPos));

        MyData.push("M" + indexMachineSelected + "Rotary_Diam", Utils.writeMetri(diamVal.getText().toString()));
        DataSaved.Rotary_Diam=Double.parseDouble(Utils.writeMetri(diamVal.getText().toString()));
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