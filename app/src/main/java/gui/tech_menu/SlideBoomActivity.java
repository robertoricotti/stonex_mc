package gui.tech_menu;

import static gui.MyApp.isApollo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

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

public class SlideBoomActivity extends AppCompatActivity {
    CheckBox off, fw, bw;
    EditText minipitchL, PLA, PLB, PLC;
    TextView encoderValue, tv1, tv2, tv3, tv4;
    ImageView clc;

    double lenA, lenB, lenC;

    int indexMachineSelected;

    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;

    int indexMeasure = 0;
    Button save, esc, setOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_boom);
        findView();
        init();
        onClick();
        updateUI();
    }

    private void findView() {
        save = findViewById(R.id.save);
        esc = findViewById(R.id.exit);
        off = findViewById(R.id.cbxOff);
        fw = findViewById(R.id.cbxLeft);
        bw = findViewById(R.id.cbxRight);
        minipitchL = findViewById(R.id.minipitchLength);
        encoderValue = findViewById(R.id.encoderValue_tv);
        setOffset = findViewById(R.id.offsetSetZero);
        PLA = findViewById(R.id.pL1et);
        PLB = findViewById(R.id.pL2et);
        PLC = findViewById(R.id.pL3et);
        tv1 = findViewById(R.id.minipitchL);
        tv2 = findViewById(R.id.pL1);
        tv3 = findViewById(R.id.pL2);
        tv4 = findViewById(R.id.pL3);
        clc=findViewById(R.id.img_clc);
    }

    private void init() {

        indexMachineSelected = MyData.get_Int("MachineSelected");

        indexMeasure = MyData.get_Int("Unit_Of_Measure");

        if (indexMeasure == 4 || indexMeasure == 5) {
            numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
        } else {
            numberDialog = new CustomNumberDialog(this, -1);
        }
        minipitchL.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Swing_Len")));
        PLA.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Swing_Len_LA")));

        PLB.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Swing_Len_LB")));

        PLC.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "_Swing_Len_LC")));
        save();
        int mountPos = MyData.get_Int("M" + indexMachineSelected + "_Swing_MountPos");
        switch (mountPos) {
            case 0:
                off.setChecked(true);
                break;
            case 1:
                fw.setChecked(true);
                break;
            case -1:
                bw.setChecked(true);
        }
    }

    private void onClick() {
        clc.setOnLongClickListener(view -> {
            double res=Math.sqrt((ExcavatorLib.swing_cylinder_Len*ExcavatorLib.swing_cylinder_Len)+(DataSaved.swing_LB*DataSaved.swing_LB));
            PLC.setText(Utils.readSensorCalibration(String.valueOf(res)));
            return false;
        });
        PLA.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(PLA);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(PLA);
            }

        });
        PLB.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(PLB);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(PLB);
            }
        });
        PLC.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(PLC);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(PLC);
            }
        });
        minipitchL.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(minipitchL);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(minipitchL);
            }
        });
        setOffset.setOnLongClickListener(view -> {
            if(isApollo) {
                MyDeviceManager.CanWrite(0, 1568,8, new byte[]{35, 3, 33, 0, 0, 0, 0, 0});//write the proper message
            }
            (new Handler()).postDelayed(this::saveCan, 800);//save cand data after 800mS
            return false;
        });
        off.setOnClickListener(view -> {
            off.setChecked(true);
            fw.setChecked(false);
            bw.setChecked(false);
            DataSaved.fwbwSwing = 0;


        });
        fw.setOnClickListener(view -> {
            off.setChecked(false);
            fw.setChecked(true);
            bw.setChecked(false);
            DataSaved.fwbwSwing = 1;
        });
        bw.setOnClickListener(view -> {
            off.setChecked(false);
            fw.setChecked(false);
            bw.setChecked(true);
            DataSaved.fwbwSwing = -1;
        });
        esc.setOnClickListener(view -> {
            esc.setEnabled(false);
            save.setEnabled(false);
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });
        save.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!minipitchL.getText().toString().contains("'") || !PLA.getText().toString().contains("'") || !PLB.getText().toString().contains("'") || !PLC.getText().toString().contains("'")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();
                } else {
                    save.setEnabled(false);
                    esc.setEnabled(false);
                    save();
                    startService(new Intent(getApplicationContext(), UpdateValuesService.class));
                    startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
                    overridePendingTransition(0, 0);
                    finish();
                }
            } else {
                if (!minipitchL.getText().toString().matches("-?\\d+(\\.\\d+)?") || !PLA.getText().toString().matches("-?\\d+(\\.\\d+)?") || !PLB.getText().toString().matches("-?\\d+(\\.\\d+)?") || !PLC.getText().toString().matches("-?\\d+(\\.\\d+)?")) {
                    new CustomToast(this, "INPUT ERROR!!!").show_error();

                } else {
                    save.setEnabled(false);
                    esc.setEnabled(false);
                    save();
                    startService(new Intent(getApplicationContext(), UpdateValuesService.class));
                    startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
                    overridePendingTransition(0, 0);
                    finish();
                }
            }
        });

    }



    public void save() {
        MyData.push("M" + indexMachineSelected + "_Swing_MountPos", String.valueOf(DataSaved.fwbwSwing));
        MyData.push("M" + indexMachineSelected + "_Swing_Len", Utils.writeMetri(minipitchL.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_Swing_Len_LA", Utils.writeMetri(PLA.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_Swing_Len_LB", Utils.writeMetri(PLB.getText().toString()));
        MyData.push("M" + indexMachineSelected + "_Swing_Len_LC", Utils.writeMetri(PLC.getText().toString()));
        lenA = Double.parseDouble(MyData.get_String("M" + indexMachineSelected + "_Swing_Len_LA"));
        lenB = Double.parseDouble(MyData.get_String("M" + indexMachineSelected + "_Swing_Len_LB"));
        lenC = Double.parseDouble(MyData.get_String("M" + indexMachineSelected + "_Swing_Len_LC"));
        DataSaved.swing_LA=lenA;
        DataSaved.swing_LB=lenB;
        DataSaved.swing_LC=lenC;

    }

    private void saveCan() {
        if(isApollo) {
            MyDeviceManager.CanWrite(0, 0x620, 8,new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
        }
        new CustomToast(SlideBoomActivity.this, "PowerCycle Sensor").show();
    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {


                            try {
                                DataSaved.miniPitch_L = Double.parseDouble(minipitchL.getText().toString().replace(",", "."));
                            } catch (Exception e) {
                                DataSaved.miniPitch_L = 0;
                            }


                            encoderValue.setText(Utils.readSensorCalibration(String.valueOf(Sensors_Decoder.Swing_Encoder))+"\t\t\t"+"("+Utils.readSensorCalibration(String.valueOf(ExcavatorLib.swing_cylinder_Len))+")"+"\t\t\t"+"("+
                                    String.format("%.2f",ExcavatorLib.swing_boom_angle)+" °)");
                            if (off.isChecked()) {
                                minipitchL.setEnabled(false);
                                PLA.setEnabled(false);
                                PLB.setEnabled(false);
                                PLC.setEnabled(false);
                                minipitchL.setAlpha(0.3f);
                                PLA.setAlpha(0.3f);
                                PLB.setAlpha(0.3f);
                                PLC.setAlpha(0.3f);
                                tv1.setAlpha(0.3f);
                                tv2.setAlpha(0.3f);
                                tv3.setAlpha(0.3f);
                                tv4.setAlpha(0.3f);
                            } else {
                                minipitchL.setEnabled(true);
                                PLA.setEnabled(true);
                                PLB.setEnabled(true);
                                PLC.setEnabled(true);
                                minipitchL.setAlpha(1f);
                                PLA.setAlpha(1f);
                                PLB.setAlpha(1f);
                                PLC.setAlpha(1f);
                                tv1.setAlpha(1f);
                                tv2.setAlpha(1f);
                                tv3.setAlpha(1f);
                                tv4.setAlpha(1f);
                            }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}