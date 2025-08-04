package gui.tech_menu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class XYZ_Calib extends AppCompatActivity {
    ImageView gpsDebug;
    ImageView save, exit, update;
    EditText dx, dY, dZ, dHdt, dist;
    int indexMachineSelected;
    int indexMeasure;
    CustomNumberDialog numberDialog;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    ImageView wlwl, imgupsx, imgupdx, imgdwdx, aaaa;
    TextView tvX, tvY, tvZ, titolo, tv_btw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass_calib);
        wlwl = findViewById(R.id.wlwl);
        imgdwdx = findViewById(R.id.imgdwdx);
        imgupsx = findViewById(R.id.imgupsx);
        imgupdx = findViewById(R.id.imgupdx);
        aaaa = findViewById(R.id.aaaa);
        tv_btw = findViewById(R.id.tv_betw);
        dist = findViewById(R.id.distBetween);

        if (DataSaved.isWL == 0) {
            imgdwdx.setVisibility(View.VISIBLE);
            imgupsx.setVisibility(View.VISIBLE);
            imgupdx.setVisibility(View.VISIBLE);
            aaaa.setVisibility(View.VISIBLE);
            wlwl.setVisibility(View.INVISIBLE);
        } else {
            imgdwdx.setVisibility(View.INVISIBLE);
            imgupsx.setVisibility(View.INVISIBLE);
            imgupdx.setVisibility(View.INVISIBLE);
            aaaa.setVisibility(View.INVISIBLE);
            wlwl.setVisibility(View.VISIBLE);
        }


        titolo = findViewById(R.id.titolo);
        update = findViewById(R.id.updateGps);
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit);
        dx = findViewById(R.id.deltaX);
        dY = findViewById(R.id.deltaY);
        dZ = findViewById(R.id.deltaZ);
        dHdt = findViewById(R.id.deltaHDT);
        tvX = findViewById(R.id.txtDeltaX);
        tvY = findViewById(R.id.textView49);
        tvZ = findViewById(R.id.textView52);
        gpsDebug = findViewById(R.id.gpsdebugg);
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        numberDialog = new CustomNumberDialog(this, -1);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        updateTxt();
        tvX.setText("ΔX " + Utils.getMetriSimbol());
        tvY.setText("ΔY " + Utils.getMetriSimbol());
        tvZ.setText("ΔZ " + Utils.getMetriSimbol());
        tv_btw.setText("G1>>G2 " + Utils.getMetriSimbol());

        gpsDebug.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });
        update.setOnClickListener(view -> {
            MyData.push("M" + indexMachineSelected + "_OffsetGPSX", Utils.writeMetri(dx.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSY", Utils.writeMetri(dY.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSZ", Utils.writeMetri(dZ.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", dHdt.getText().toString());
            MyData.push("M" + indexMachineSelected + "_distG1_G2", Utils.writeMetri(dist.getText().toString()));
            startService(new Intent(this, UpdateValuesService.class));
        });

        save.setOnClickListener(view -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            MyData.push("M" + indexMachineSelected + "_OffsetGPSX", Utils.writeMetri(dx.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSY", Utils.writeMetri(dY.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSZ", Utils.writeMetri(dZ.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", dHdt.getText().toString());
            MyData.push("M" + indexMachineSelected + "_distG1_G2", Utils.writeMetri(dist.getText().toString()));
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
        dx.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(dx);


        });
        dY.setOnClickListener(view -> {

            if (!numberDialog.dialog.isShowing())
                numberDialog.show(dY);

        });
        dZ.setOnClickListener(view -> {

            if (!numberDialog.dialog.isShowing())
                numberDialog.show(dZ);

        });
        dHdt.setOnClickListener(view -> {

            if (!numberDialog.dialog.isShowing())
                numberDialog.show(dHdt);

        });
        dist.setOnClickListener(view -> {

            if (!numberDialog.dialog.isShowing())
                numberDialog.show(dist);

        });


    }

    public void updateUI() {
        DataSaved.offset_Z_antenna = 0;
        double hdt = 0;
        hdt = NmeaListener.mch_Orientation + DataSaved.deltaGPS2;
        hdt = hdt % 360;

        titolo.setText("     E: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[0])) + "    N: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[1])) + "  " + "   Z: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[2])) + "   HDT: " + String.format("%.2f", hdt) + " °");

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
        dx.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deltaX)));
        dY.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deltaY)));
        dZ.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deltaZ)));
        dHdt.setText(String.format("%.3f", DataSaved.deltaGPS2).replace(",", "."));
        dist.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.distG1_G2)));
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}