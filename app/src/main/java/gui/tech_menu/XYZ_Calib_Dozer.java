package gui.tech_menu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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

public class XYZ_Calib_Dozer extends BaseClass {
    ImageView gpsDebug;
    Button save, exit, update;
    EditText dx, dY, dZ, dHdt,etBladeR,etBladeL,altLama,altPalo,distBetween;
    int indexMachineSelected;
    int indexMeasure;
    CustomNumberDialog numberDialog;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    TextView tvX, tvY, tvZ,titolo,tvAltPalo,tvAltLama,tvLeftSide,tvRighrSide,tvBetween;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xyz_dozer);
        titolo=findViewById(R.id.titolo);
        update = findViewById(R.id.updateGps);
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit);
        dx = findViewById(R.id.deltaX);
        dY = findViewById(R.id.deltaY);
        dZ = findViewById(R.id.deltaZ);
        dHdt = findViewById(R.id.deltaHDT);
        tvX = findViewById(R.id.textView48);
        tvY = findViewById(R.id.textView49);
        tvZ = findViewById(R.id.textView52);
        tvBetween=findViewById(R.id.textViewDistBetween);
        etBladeR = findViewById(R.id.bladeR);
        etBladeL = findViewById(R.id.bladeL);
        gpsDebug = findViewById(R.id.gpsdebugg);
        altLama = findViewById(R.id.alteLama);
        altPalo = findViewById(R.id.altPalo);
        tvAltLama=findViewById(R.id.txLama);
        tvAltPalo=findViewById(R.id.txtPole);
        tvLeftSide=findViewById(R.id.txtBleft);
        tvRighrSide=findViewById(R.id.txtBright);
        distBetween=findViewById(R.id.distBetween);
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        numberDialog = new CustomNumberDialog(this, -1);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        updateTxt();
        tvX.setText("ΔX " + Utils.getMetriSimbol());
        tvY.setText("ΔY " + Utils.getMetriSimbol());
        tvZ.setText("ΔZ " + Utils.getMetriSimbol());
        tvLeftSide.setText("Right Side " + Utils.getMetriSimbol());
        tvRighrSide.setText("Left Side " + Utils.getMetriSimbol());
        tvAltPalo.setText("Pole Height " + Utils.getMetriSimbol());
        tvAltLama.setText("Blade Height " + Utils.getMetriSimbol());
        tvBetween.setText("Right Edge >> GPS2 "+Utils.getMetriSimbol());
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
            MyData.push("M"+indexMachineSelected+"_Bucket_"+"0"+"_Width_L", Utils.writeMetri(etBladeL.getText().toString()));
            MyData.push("M"+indexMachineSelected+"_Bucket_"+"0"+"_Width_R", Utils.writeMetri(etBladeR.getText().toString()));

            MyData.push("M"+indexMachineSelected+"_Bucket_"+"0"+"_Lama", Utils.writeMetri(altLama.getText().toString()));
            MyData.push("M"+indexMachineSelected+"_Bucket_"+"0"+"_Palo", Utils.writeMetri(altPalo.getText().toString()));
            MyData.push("M"+indexMachineSelected+"_Bucket_"+"0"+"_Between", Utils.writeMetri(distBetween.getText().toString()));
            startService(new Intent(this, UpdateValuesService.class));
        });

        save.setOnClickListener(view -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            MyData.push("M" + indexMachineSelected + "_OffsetGPSX", Utils.writeMetri(dx.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSY", Utils.writeMetri(dY.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPSZ", Utils.writeMetri(dZ.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", dHdt.getText().toString());
            MyData.push("M" + indexMachineSelected +"_Bucket_"+"0"+"_Width_L", Utils.writeMetri(etBladeL.getText().toString()));
            MyData.push("M"+indexMachineSelected+"_Bucket_"+"0"+"_Width_R", Utils.writeMetri(etBladeR.getText().toString()));

            MyData.push("M"+indexMachineSelected+"_Bucket_"+"0"+"_Lama", Utils.writeMetri(altLama.getText().toString()));
            MyData.push("M"+indexMachineSelected+"_Bucket_"+"0"+"_Palo", Utils.writeMetri(altPalo.getText().toString()));
            MyData.push("M"+indexMachineSelected+"_Bucket_"+"0"+"_Between", Utils.writeMetri(distBetween.getText().toString()));
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, MachineSettings.class));
            finish();


        });
        exit.setOnClickListener(view -> {
            exit.setEnabled(false);
            save.setEnabled(false);
            startActivity(new Intent(this, MachineSettings.class));
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
        etBladeL.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(etBladeL);
        });
        etBladeR.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(etBladeR);
        });
        altLama.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(altLama);
        });
        altPalo.setOnClickListener(view -> {
            if (!numberDialog.dialog.isShowing())
                numberDialog.show(altPalo);
        });
        distBetween.setOnClickListener(view -> {
            if(!numberDialog.dialog.isShowing()){
                numberDialog.show(distBetween);
            }
        });


    }

    public void updateUI() {
        DataSaved.offset_Z_antenna=0;
        double hdt=0;
        hdt=NmeaListener.mch_Orientation + DataSaved.deltaGPS2;
        hdt=hdt%360;

        titolo.setText("     E: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[0])) + "    N: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[1])) + "  " + "   Z: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[2])) + "   HDT: "+String.format("%.2f",hdt).replace(",",".")+" °");

        if (DataSaved.gpsOk) {
            gpsDebug.setImageResource(R.drawable.gps_si);
        } else {
            gpsDebug.setImageResource(R.drawable.gps_no);
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
        etBladeR.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.W_Blade_RIGHT)));
        etBladeL.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.W_Blade_LEFT)));
        altPalo.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.altezzaPali)));
        distBetween.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.distBetween)));
        altLama.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.altezzaLama)));
        dHdt.setText(String.format("%.3f", DataSaved.deltaGPS2).replace(",", "."));
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}