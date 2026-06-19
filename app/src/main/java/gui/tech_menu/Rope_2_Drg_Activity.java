package gui.tech_menu;


import static packexcalib.exca.Sensors_Decoder_Dredge.Lunghezza_Fune_2;
import static services.CanService.RopeCon_2;
import static services.CanService.SlewCon;
import static utils.MyTypes.LIEBHERR_CRANE;
import static utils.MyTypes.SENNEBOGHEN;
import static utils.MyTypes.STONEX_SENSORS;

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
import com.example.stx_dig.R;
import gui.BaseClass;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import utils.MyData;
import utils.MyDeviceManager;
import utils.MyTypes;
import utils.Utils;

public class Rope_2_Drg_Activity extends BaseClass {
    TextView b2l, distVal,txtOffset,headerr;
    CheckBox ckOff, ckClock, ckRev;
    EditText diamVal,fixedOff;
    Button offsetSetZero;
    ImageView save, exit,stato;
    int indexMachineSelected;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;
    int indexMeasure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rope2_drg);
        findView();
        init();
        onClick();
        onCheckedChange();
        updateUI();
    }

    private void findView() {
        stato=findViewById(R.id.stato);
        headerr = findViewById(R.id.headerr);
        b2l = findViewById(R.id.b2l);
        distVal = findViewById(R.id.distVal);
        diamVal = findViewById(R.id.diamVal);
        txtOffset=findViewById(R.id.txtOffset);
        fixedOff = findViewById(R.id.fixedOff);
        ckOff = findViewById(R.id.cbxOff);
        ckClock = findViewById(R.id.cbxLeft);
        ckRev = findViewById(R.id.cbxRight);
        offsetSetZero = findViewById(R.id.offsetSetZero);
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit);
        switch (DataSaved.Dredge_Interface_Type) {
            case STONEX_SENSORS:
                headerr.setText("2nd ROPE ENCODER CALIBRATION - id: 0x18Eh");
                break;

            case LIEBHERR_CRANE:
                headerr.setText("2nd ROPE ENCODER CALIBRATION - id: 0x18FF8080h");
                diamVal.setVisibility(View.INVISIBLE);
                b2l.setVisibility(View.INVISIBLE);
                offsetSetZero.setVisibility(View.INVISIBLE);
                fixedOff.setVisibility(View.INVISIBLE);
                txtOffset.setVisibility(View.INVISIBLE);
                break;

            case SENNEBOGHEN:
                headerr.setText("2nd ROPE ENCODER CALIBRATION - id: 0x1F8h");
                diamVal.setVisibility(View.INVISIBLE);
                b2l.setVisibility(View.INVISIBLE);
                offsetSetZero.setVisibility(View.INVISIBLE);
                fixedOff.setVisibility(View.INVISIBLE);
                txtOffset.setVisibility(View.INVISIBLE);
                break;

        }

    }

    private void init() {
        try {
            Log.d("TipoDraga",DataSaved.Dredge_Interface_Type+"");
            indexMachineSelected = MyData.get_Int("MachineSelected");
            indexMeasure = MyData.get_Int("Unit_Of_Measure");

            if (indexMeasure == 4 || indexMeasure == 5) {
                numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
            } else {
                numberDialog = new CustomNumberDialog(this, -1);
            }

            diamVal.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "Diametro_Tamburo_2")));
            fixedOff.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "Rope_Fixed_Offset_2")));
            b2l.setText("WHEEL 2 DIAMETER " + Utils.getMetriSimbol());
            txtOffset.setText("ROPE 2 OFFSET "+Utils.getMetriSimbol());
            int mountPos = MyData.get_Int("M" + indexMachineSelected + "Pos_ENCODER_2");
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

            MyDeviceManager.CanWrite(true, 0, 0x60E, 8, new byte[]{0x23, 0x03, 0x60, 0, 0, 0, 0, 0});
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {

            }
            MyDeviceManager.CanWrite(true, 0, 0x60E, 8, new byte[]{0x23, 0x10, 0x10, 0x01, 0x73, 0x61, 0x76, 0x65});


            return true;
        });
        save.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!diamVal.getText().toString().contains("'")&&!fixedOff.getText().toString().contains("'")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    exit.setEnabled(false);
                    save.setEnabled(false);
                    save();
                    startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
                    finish();
                }
            } else {
                if (!diamVal.getText().toString().matches("-?\\d+(\\.\\d+)?")||!fixedOff.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
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
        fixedOff.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(fixedOff);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(fixedOff);
            }
        });
    }

    private void onCheckedChange() {
        ckOff.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (ckOff.isChecked()) {
                DataSaved.Pos_ENCODER_2 = 0;
                ckClock.setChecked(false);
                ckRev.setChecked(false);
            }
        });

        ckClock.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (ckClock.isChecked()) {
                DataSaved.Pos_ENCODER_2 = 1;
                ckOff.setChecked(false);
                ckRev.setChecked(false);
            }
        });

        ckRev.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (ckRev.isChecked()) {
                DataSaved.Pos_ENCODER_2 = -1;
                ckOff.setChecked(false);
                ckClock.setChecked(false);
            }
        });

    }

    public void updateUI() {
        try {
            if(RopeCon_2){
                stato.setImageResource(R.drawable.sfondo_bottone_selezionato);
            }else {
                stato.setImageResource(R.drawable.sfondo_auto_enabled);
            }
            distVal.setText(Utils.readSensorCalibration(String.valueOf(Lunghezza_Fune_2)));
        } catch (Exception e) {
            distVal.setText("Error");
        }
        ;

    }

    private void save() {
        int mounPos = 0;

        if (ckClock.isChecked()) {
            mounPos = 1;
        }
        if (ckRev.isChecked()) {
            mounPos = -1;
        }
        DataSaved.Pos_ENCODER_2 = mounPos;
        MyData.push("M" + indexMachineSelected + "Pos_ENCODER_2", String.valueOf(mounPos));

        MyData.push("M" + indexMachineSelected + "Diametro_Tamburo_2", Utils.writeMetri(diamVal.getText().toString()));
        DataSaved.Diametro_Tamburo_2 = Double.parseDouble(Utils.writeMetri(diamVal.getText().toString()));

        MyData.push("M" + indexMachineSelected + "Rope_Fixed_Offset_2", Utils.writeMetri(fixedOff.getText().toString()));
        DataSaved.Rope_Fixed_Offset_2 = Double.parseDouble(Utils.writeMetri(fixedOff.getText().toString()));
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