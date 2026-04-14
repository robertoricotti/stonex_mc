package gui.tech_menu;

import static packexcalib.exca.ExcavatorLib.toolEndCoord;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.NmeaListener;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class Mast_Antenna extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRepeating = false;
    EditText dx, dY, dZ, dHdt;
    ImageView gpsDebug;
    int indexMachineSelected;
    int indexMeasure;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;
    Dialog_Drill_GNSS dialogDrillGnss;
    ImageView xp, xm, yp, ym, zp, zm, hp, hm;
    ImageView save, exit, update;
    TextView tvX, tvY, tvZ, titolo;
    double myStep = 0.001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mast_antenna);
        xm = findViewById(R.id.but_meno_x);
        xp = findViewById(R.id.but_piu_x);
        ym = findViewById(R.id.but_meno_y);
        yp = findViewById(R.id.but_piu_y);
        zm = findViewById(R.id.but_meno_z);
        zp = findViewById(R.id.but_piu_z);
        hm = findViewById(R.id.but_meno_h);
        hp = findViewById(R.id.but_piu_h);

        titolo = findViewById(R.id.titolo);
        update = findViewById(R.id.updateTool);
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit);
        dx = findViewById(R.id.deltaX);
        dY = findViewById(R.id.deltaY);
        dZ = findViewById(R.id.deltaZ);
        dHdt = findViewById(R.id.deltaHDT);
        tvX = findViewById(R.id.txtDeltaX);
        tvY = findViewById(R.id.txtDeltaYt);
        tvZ = findViewById(R.id.txtDeltaZt);
        gpsDebug = findViewById(R.id.gpsdebugg);
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");

        numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);

        numberDialog = new CustomNumberDialog(this, -1);

        dialogDrillGnss = new Dialog_Drill_GNSS(this);
        updateTxt();
        tvX.setText("ΔX " + Utils.getMetriSimbol());
        tvY.setText("ΔY " + Utils.getMetriSimbol());
        tvZ.setText("ΔZ " + Utils.getMetriSimbol());

        gpsDebug.setOnClickListener(view -> {
            if (!dialogDrillGnss.alertDialog.isShowing()) {
                dialogDrillGnss.show();
            }
        });
        update.setOnClickListener(view -> {
            Log.d("CalledUPF", "Called");
            MyData.push("M" + indexMachineSelected + "Tool_Delta_X", Utils.writeMetri(dx.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "Tool_Delta_Y", Utils.writeMetri(dY.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "Tool_Delta_Z", Utils.writeMetri(dZ.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", dHdt.getText().toString().replace(",", "."));

        });
        save.setOnClickListener(view -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            MyData.push("M" + indexMachineSelected + "Tool_Delta_X", Utils.writeMetri(dx.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "Tool_Delta_Y", Utils.writeMetri(dY.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "Tool_Delta_Z", Utils.writeMetri(dZ.getText().toString().replace(",", ".")));
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", dHdt.getText().toString().replace(",", "."));
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();


        });
        exit.setOnClickListener(view -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });

        setupAutoRepeat(xp, () -> {
            DataSaved.Tool_Delta_X += myStep;
            updateTxt();
        });
        setupAutoRepeat(xm, () -> {
            DataSaved.Tool_Delta_X -= myStep;
            updateTxt();
        });
        setupAutoRepeat(yp, () -> {
            DataSaved.Tool_Delta_Y += myStep;
            updateTxt();
        });
        setupAutoRepeat(ym, () -> {
            DataSaved.Tool_Delta_Y -= myStep;
            updateTxt();
        });
        setupAutoRepeat(zp, () -> {
            DataSaved.Tool_Delta_Z += myStep;
            updateTxt();
        });
        setupAutoRepeat(zm, () -> {
            DataSaved.Tool_Delta_Z -= myStep;
            updateTxt();
        });
        setupAutoRepeat(hp, () -> {
            DataSaved.deltaGPS2 += 0.01;
            updateTxt();
        });
        setupAutoRepeat(hm, () -> {
            DataSaved.deltaGPS2 -= 0.01;
            updateTxt();
        });


        dx.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(dx);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(dx);
            }


        });
        dY.setOnClickListener(view -> {

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(dY);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(dY);
            }

        });
        dZ.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(dZ);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(dZ);
            }

        });
        dHdt.setOnClickListener(view -> {

            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(dHdt);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(dHdt);
            }

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

    private void updateOnClose() {
        DataSaved.Tool_Delta_X = Double.parseDouble(Utils.writeMetri((dx.getText().toString())));
        DataSaved.Tool_Delta_Y = Double.parseDouble(Utils.writeMetri((dY.getText().toString())));
        DataSaved.Tool_Delta_Z = Double.parseDouble(Utils.writeMetri((dZ.getText().toString())));
        DataSaved.deltaGPS2 = Double.parseDouble(((dHdt.getText().toString())));

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
        hdt = NmeaListener.mch_Orientation + DataSaved.deltaGPS2;
        hdt = hdt % 360;

        titolo.setText("     E: " + Utils.readSensorCalibration(String.valueOf(toolEndCoord[0])) + "    N: " + Utils.readSensorCalibration(String.valueOf(toolEndCoord[1])) + "  " + "   Z: " + Utils.readSensorCalibration(String.valueOf(toolEndCoord[2])) + "   HDT: " + String.format("%.2f", hdt).replace(",", ".") + " °");

        if (DataSaved.gpsOk) {
            gpsDebug.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            gpsDebug.setImageTintList(ColorStateList.valueOf(Color.RED));
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void updateTxt() {
        dx.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Tool_Delta_X)));
        dY.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Tool_Delta_Y)));
        dZ.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Tool_Delta_Z)));
        dHdt.setText(String.format("%.3f", DataSaved.deltaGPS2).replace(",", "."));
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