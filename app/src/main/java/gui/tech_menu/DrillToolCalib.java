package gui.tech_menu;

import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static utils.MyTypes.DRILL;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.MyData;
import utils.Utils;

public class DrillToolCalib extends BaseClass {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRepeating = false;
    ImageView but_meno_x, but_piu_x, but_meno_y, but_piu_y, but_meno_z, but_piu_z,
            save, updateTool, exit,
            gpsdebugg;
    EditText deltaX, deltaY, deltaZ;
    TextView titolo, txtDeltaX, txtDeltaYt, txtDeltaZt;
    int indexMachineSelected;
    int indexMeasure;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    Dialog_Drill_GNSS dialogDrillGnss;
    double myStep = 0.001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_tool_calib);

        findView();
        init();
        onClick();
        updateUI();
    }

    private void findView() {
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
        numberDialog = new CustomNumberDialog(this, -1);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogDrillGnss = new Dialog_Drill_GNSS(this);
        but_meno_x = findViewById(R.id.but_meno_x);
        but_piu_x = findViewById(R.id.but_piu_x);
        but_meno_y = findViewById(R.id.but_meno_y);
        but_piu_y = findViewById(R.id.but_piu_y);
        but_meno_z = findViewById(R.id.but_meno_z);
        but_piu_z = findViewById(R.id.but_piu_z);
        save = findViewById(R.id.save);
        updateTool = findViewById(R.id.updateTool);
        exit = findViewById(R.id.exit);
        gpsdebugg = findViewById(R.id.gpsdebugg);
        deltaX = findViewById(R.id.deltaX);
        deltaY = findViewById(R.id.deltaY);
        deltaZ = findViewById(R.id.deltaZ);
        titolo = findViewById(R.id.titolo);
        txtDeltaX = findViewById(R.id.txtDeltaX);
        txtDeltaYt = findViewById(R.id.txtDeltaYt);
        txtDeltaZt = findViewById(R.id.txtDeltaZt);
        updateTxt();
        txtDeltaX.setText("Tool ΔX " + Utils.getMetriSimbol());
        txtDeltaYt.setText("Tool ΔY " + Utils.getMetriSimbol());
        txtDeltaZt.setText("Tool ΔZ " + Utils.getMetriSimbol());
    }

    private void init() {

    }

    private void onClick() {
        deltaX.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(deltaX);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(deltaX);
            }
        });
        deltaY.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(deltaY);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(deltaY);
            }
        });
        deltaZ.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(deltaZ);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(deltaZ);
            }
        });
        gpsdebugg.setOnClickListener(view -> {
            if (DataSaved.isWL == DRILL) {
                if (!dialogDrillGnss.alertDialog.isShowing()) {
                    dialogDrillGnss.show();
                }
            } else {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                    dialogGnssCoordinates.show();
                }
            }
        });
        save.setOnClickListener(view -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            disableAll();
            MyData.push("M" + indexMachineSelected + "Tool_Delta_X", Utils.writeMetri(deltaX.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "Tool_Delta_Y", Utils.writeMetri(deltaY.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "Tool_Delta_Z", Utils.writeMetri(deltaZ.getText().toString().replace(",", ".")));

            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });
        exit.setOnClickListener(view -> {
            disableAll();
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();

        });
        updateTool.setOnClickListener(view -> {
            MyData.push("M" + indexMachineSelected + "Tool_Delta_X", Utils.writeMetri(deltaX.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "Tool_Delta_Y", Utils.writeMetri(deltaY.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "Tool_Delta_Z", Utils.writeMetri(deltaZ.getText().toString().replace(",", ".")));
        });

        setupAutoRepeat(but_piu_x, () -> {
            DataSaved.Tool_Delta_X += myStep;
            updateTxt();
        });
        setupAutoRepeat(but_meno_x, () -> {
            DataSaved.Tool_Delta_X -= myStep;
            updateTxt();
        });
        setupAutoRepeat(but_piu_y, () -> {
            DataSaved.Tool_Delta_Y += myStep;
            updateTxt();
        });
        setupAutoRepeat(but_meno_y, () -> {
            DataSaved.Tool_Delta_Y -= myStep;
            updateTxt();
        });
        setupAutoRepeat(but_piu_z, () -> {
            DataSaved.Tool_Delta_Z += myStep;
            updateTxt();
        });
        setupAutoRepeat(but_meno_z, () -> {
            DataSaved.Tool_Delta_Z -= myStep;
            updateTxt();
        });
        numberDialog.dialog.setOnDismissListener(dialog -> {

            updateOnClose();
            updateTxt();


        });
        numberDialogFtIn.dialog.setOnDismissListener(dialog -> {

            updateOnClose();
            updateTxt();


        });
    }


    public void updateUI() {
        switch (indexMeasure) {
            case 0:
            case 1:
                myStep = 0.001;
                break;

            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                myStep = 0.0003048;
                break;
            default:
                myStep = 0.0003048;
                break;
        }
        double hdt = 0;
        hdt = hdt_BOOM;
        hdt = hdt % 360;

        titolo.setText("     E: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.toolEndCoord[0])) + "    N: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.toolEndCoord[1])) + "  " + "   Z: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.toolEndCoord[2])) + "   HDT: " + String.format("%.2f", hdt).replace(",", ".") + " °");

        if (DataSaved.gpsOk) {
            gpsdebugg.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            gpsdebugg.setImageTintList(ColorStateList.valueOf(Color.RED));
        }
    }

    private void disableAll() {
        save.setEnabled(false);
        gpsdebugg.setEnabled(false);
        exit.setEnabled(false);
        updateTool.setEnabled(false);

    }

    private void updateOnClose() {
        DataSaved.Tool_Delta_X = Double.parseDouble(Utils.writeMetri((deltaX.getText().toString())));
        DataSaved.Tool_Delta_Y = Double.parseDouble(Utils.writeMetri((deltaY.getText().toString())));
        DataSaved.Tool_Delta_Z = Double.parseDouble(Utils.writeMetri((deltaZ.getText().toString())));

    }

    private void updateTxt() {
        deltaX.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Tool_Delta_X)));
        deltaY.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Tool_Delta_Y)));
        deltaZ.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Tool_Delta_Z)));

    }

    private void setupAutoRepeat(ImageView button, Runnable action) {
        button.setOnClickListener(v -> action.run());

        button.setOnLongClickListener(v -> {
            isRepeating = true;


            // Primo ritardo di 500ms prima di iniziare la ripetizione
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRepeating) {
                        action.run();
                        handler.postDelayed(this, 50); // ripeti ogni 50ms
                    }
                }
            }, 500);

            return true; // segnala che il long click è gestito
        });

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isRepeating) {
                        updateOnClose();
                        updateTxt();

                    }
                    isRepeating = false; // stop

                    break;
            }
            return false;
        });
    }

}