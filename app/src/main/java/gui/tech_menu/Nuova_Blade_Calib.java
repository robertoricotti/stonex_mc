package gui.tech_menu;

import static gui.MyApp.errorCode;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;
import utils.WifiHelper;

public class Nuova_Blade_Calib extends BaseClass {
    int count = 0;
    int indexMeasure, indexMachineSelected;
    ConstraintLayout pag_1, pag_2, pag_3;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    ImageView save, update, exit, toLeft, toRight, status, wifi;
    TextView txtCoord, txtDeltaX, txtBright, txtBleft, txLama, txtPole, textViewDistBetween, indicazione, txtDeltaY, txtDeltaHDT, txtCutEdge;
    EditText distBetween, deltaX, alteLama, altPalo, bladeR, bladeL, deltaY, deltaHDT, etCutEdge;
    CustomNumberDialog numberDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nuova_grade_calib);
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        findView();
        onClick();
        updateUI();
    }

    private void findView() {
        numberDialog = new CustomNumberDialog(this, -1);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        save = findViewById(R.id.salva);
        update = findViewById(R.id.update);
        exit = findViewById(R.id.exit);
        toLeft = findViewById(R.id.toLeft);
        toRight = findViewById(R.id.toRight);
        status = findViewById(R.id.img00);
        wifi = findViewById(R.id.img01);
        txtCoord = findViewById(R.id.txtCoord);
        txtCutEdge = findViewById(R.id.txtCutEdge);
        etCutEdge = findViewById(R.id.etCutEdge);
        pag_1 = findViewById(R.id.pag_1);
        pag_2 = findViewById(R.id.pag_2);
        pag_3 = findViewById(R.id.pag_3);

        //pag1
        txtDeltaX = findViewById(R.id.txtDeltaX);
        txtBright = findViewById(R.id.txtBright);
        txtBleft = findViewById(R.id.txtBleft);
        txLama = findViewById(R.id.txLama);
        txtPole = findViewById(R.id.txtPole);
        textViewDistBetween = findViewById(R.id.textViewDistBetween);
        indicazione = findViewById(R.id.indicazione);
        distBetween = findViewById(R.id.distBetween);
        deltaX = findViewById(R.id.deltaX);
        alteLama = findViewById(R.id.alteLama);
        altPalo = findViewById(R.id.altPalo);
        bladeR = findViewById(R.id.bladeR);
        bladeL = findViewById(R.id.bladeL);
        txtDeltaX.setText("ΔX " + Utils.getMetriSimbol());
        txtBleft.setText("Right Side " + Utils.getMetriSimbol());
        txtBright.setText("Left Side " + Utils.getMetriSimbol());
        txtPole.setText("Pole Height " + Utils.getMetriSimbol());
        txLama.setText("Blade Height " + Utils.getMetriSimbol());
        textViewDistBetween.setText("Right Edge >> GPS2 " + Utils.getMetriSimbol());
        txtCutEdge.setText("Cutting Edge " + Utils.getMetriSimbol());

        // pag2
        txtDeltaY = findViewById(R.id.txtDeltaY);
        txtDeltaY.setText("ΔY " + Utils.getMetriSimbol());
        deltaY = findViewById(R.id.deltaY);
        deltaHDT = findViewById(R.id.deltaHDT);
        txtDeltaHDT = findViewById(R.id.txtDeltaHDT);

        initEdits();
    }

    private void onClick() {
        etCutEdge.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(etCutEdge);
        });
        deltaX.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(deltaX);
        });
        deltaY.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(deltaY);
        });
        deltaHDT.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(deltaHDT);
        });
        bladeL.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(bladeL);
        });
        bladeR.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(bladeR);
        });
        altPalo.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(altPalo);
        });
        alteLama.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(alteLama);
        });
        distBetween.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing()) {
                numberDialog.show(distBetween);
            }
        });
        exit.setOnClickListener(view -> {
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });

        save.setOnClickListener(view -> {
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });
        update.setOnClickListener(view -> {
            DataSaved.altezzaLama = Double.parseDouble(Utils.writeMetri(alteLama.getText().toString()));
            DataSaved.altezzaPali = Double.parseDouble(Utils.writeMetri(altPalo.getText().toString()));
            DataSaved.distBetween = Double.parseDouble(Utils.writeMetri(distBetween.getText().toString()));
            DataSaved.deltaZ = DataSaved.altezzaLama + DataSaved.altezzaPali;
            DataSaved.deltaX = Double.parseDouble(Utils.writeMetri(deltaX.getText().toString()));
            DataSaved.deltaY = Double.parseDouble(Utils.writeMetri(deltaY.getText().toString()));
            DataSaved.W_Blade_LEFT = Double.parseDouble(Utils.writeMetri(bladeL.getText().toString()));
            DataSaved.W_Blade_RIGHT = Double.parseDouble(Utils.writeMetri(bladeL.getText().toString()));
            DataSaved.W_Blade_TOT = DataSaved.W_Blade_LEFT + DataSaved.W_Blade_RIGHT;
            DataSaved.deltaGPS2 = Double.parseDouble(Utils.writeMetri(deltaHDT.getText().toString()));
            DataSaved.usuraLamaCX = Double.parseDouble(etCutEdge.getText().toString());
            MyData.push("M" + indexMachineSelected + "_OffsetGPSX", Utils.writeMetri(deltaX.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSY", Utils.writeMetri(deltaY.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSZ", Utils.writeMetri(String.valueOf(DataSaved.deltaZ)));
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", deltaHDT.getText().toString());
            MyData.push("M" + indexMachineSelected + "_Bucket_" + "0" + "_Width_L", Utils.writeMetri(bladeL.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_Bucket_" + "0" + "_Width_R", Utils.writeMetri(bladeL.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_Bucket_" + "0" + "_Lama", Utils.writeMetri(alteLama.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_Bucket_" + "0" + "_Palo", Utils.writeMetri(altPalo.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_Bucket_" + "0" + "_Between", Utils.writeMetri(distBetween.getText().toString()));
            MyData.push("M" + indexMachineSelected + "usuraLamaCX", Utils.writeMetri(etCutEdge.getText().toString()));
            startService(new Intent(this, UpdateValuesService.class));
        });
        status.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });
        toLeft.setOnClickListener(view -> {
            if (count > 0) {
                count--;
            } else {
                count = 0;
            }

        });
        toRight.setOnClickListener(view -> {
            if (count < 2) {
                count++;
            } else {
                count = 2;
            }

        });
    }

    public void updateUI() {
        try {
            DataSaved.deltaZ = DataSaved.altezzaLama + DataSaved.altezzaPali;
            switch (count) {
                case 0:
                    pag_1.setVisibility(TextView.VISIBLE);
                    pag_2.setVisibility(TextView.GONE);
                    pag_3.setVisibility(TextView.GONE);
                    showPag1(true);
                    showPag2(false);
                    showPag3(false);
                    break;

                case 1:
                    pag_1.setVisibility(TextView.GONE);
                    pag_2.setVisibility(TextView.VISIBLE);
                    pag_3.setVisibility(TextView.GONE);
                    showPag1(false);
                    showPag2(true);
                    showPag3(false);
                    break;

                case 2:
                    pag_1.setVisibility(TextView.GONE);
                    pag_2.setVisibility(TextView.GONE);
                    pag_3.setVisibility(TextView.VISIBLE);
                    showPag1(false);
                    showPag2(false);
                    showPag3(true);
                    break;
                default:
                    pag_1.setVisibility(TextView.GONE);
                    pag_2.setVisibility(TextView.GONE);
                    pag_3.setVisibility(TextView.GONE);
                    showPag1(false);
                    showPag2(false);
                    showPag3(false);
                    break;
            }
            if (DataSaved.gpsOk && errorCode == 0) {
                status.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                status.setImageTintList(ColorStateList.valueOf(Color.RED));
            }
            if (count == 0) {
                toLeft.setVisibility(TextView.INVISIBLE);
            } else {
                toLeft.setVisibility(TextView.VISIBLE);
            }
            if (count == 2) {
                toRight.setVisibility(TextView.INVISIBLE);
            } else {
                toRight.setVisibility(TextView.VISIBLE);
            }
            DataSaved.offset_Z_antenna = 0;
            double hdt = 0;
            hdt = NmeaListener.mch_Orientation + DataSaved.deltaGPS2;
            hdt = hdt % 360;
            txtCoord.setText("     E: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[0])) + "    N: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[1])) + "  " + "   Z: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[2])) + "   HDT: " + String.format("%.2f", hdt).replace(",", ".") + " °");
            try {
                String ssid = WifiHelper.getConnectedSSID(getApplicationContext());
                if (ssid != null) {

                    wifi.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);

                } else {

                    wifi.setImageResource(R.drawable.wifi_vuoto);

                }
            } catch (Exception e) {
                wifi.setImageResource(R.drawable.wifi_off_96);
            }


        } catch (Exception ignored) {

        }

    }

    public void showPag1(boolean mostra) {
        if (mostra) {
            txtDeltaX.setVisibility(View.VISIBLE);
            txtBright.setVisibility(View.VISIBLE);
            txtBleft.setVisibility(View.VISIBLE);
            txLama.setVisibility(View.VISIBLE);
            txtPole.setVisibility(View.VISIBLE);
            textViewDistBetween.setVisibility(View.VISIBLE);
            indicazione.setVisibility(View.VISIBLE);
            distBetween.setVisibility(View.VISIBLE);
            deltaX.setVisibility(View.VISIBLE);
            alteLama.setVisibility(View.VISIBLE);
            altPalo.setVisibility(View.VISIBLE);
            bladeR.setVisibility(View.VISIBLE);
            bladeL.setVisibility(View.VISIBLE);
        } else {
            txtDeltaX.setVisibility(View.GONE);
            txtBright.setVisibility(View.GONE);
            txtBleft.setVisibility(View.GONE);
            txLama.setVisibility(View.GONE);
            txtPole.setVisibility(View.GONE);
            textViewDistBetween.setVisibility(View.GONE);
            indicazione.setVisibility(View.GONE);
            distBetween.setVisibility(View.GONE);
            deltaX.setVisibility(View.GONE);
            alteLama.setVisibility(View.GONE);
            altPalo.setVisibility(View.GONE);
            bladeR.setVisibility(View.GONE);
            bladeL.setVisibility(View.GONE);
        }
    }

    public void showPag2(boolean mostra) {
        if (mostra) {
            txtDeltaY.setVisibility(View.VISIBLE);
            deltaY.setVisibility(View.VISIBLE);
            deltaHDT.setVisibility(View.VISIBLE);
            txtDeltaHDT.setVisibility(View.VISIBLE);

        } else {
            txtDeltaY.setVisibility(View.GONE);
            deltaY.setVisibility(View.GONE);
            deltaHDT.setVisibility(View.GONE);
            txtDeltaHDT.setVisibility(View.GONE);
        }
    }
    public void showPag3(boolean mostra) {
        if (mostra) {
            txtCutEdge.setVisibility(View.VISIBLE);
            etCutEdge.setVisibility(View.VISIBLE);


        } else {
            txtCutEdge.setVisibility(View.GONE);
            etCutEdge.setVisibility(View.GONE);
        }
    }

    private void initEdits() {
        deltaX.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deltaX)));
        deltaY.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deltaY)));
        bladeR.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.W_Blade_RIGHT)));
        bladeL.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.W_Blade_LEFT)));
        altPalo.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.altezzaPali)));
        distBetween.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.distBetween)));
        alteLama.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.altezzaLama)));
        deltaHDT.setText(String.format("%.3f", DataSaved.deltaGPS2).replace(",", "."));
        etCutEdge.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.usuraLamaCX)));
    }
}
