package gui.tech_menu;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;
import utils.MyData;
import utils.Utils;

public class DrillToolCalib extends BaseClass {
    ImageView but_meno_x, but_piu_x, but_meno_y, but_piu_y, but_meno_z, but_piu_z,
            save, updateTool, exit,
            gpsdebugg;
    EditText deltaX,deltaY,deltaZ;
    TextView titolo,txtDeltaX,txtDeltaYt,txtDeltaZt;
    int indexMachineSelected;
    int indexMeasure;
    CustomNumberDialog numberDialog;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
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
        numberDialog = new CustomNumberDialog(this, -1);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        but_meno_x=findViewById(R.id.but_meno_x);
        but_piu_x=findViewById(R.id.but_piu_x);
        but_meno_y=findViewById(R.id.but_meno_y);
        but_piu_y=findViewById(R.id.but_piu_y);
        but_meno_z=findViewById(R.id.but_meno_z);
        but_piu_z=findViewById(R.id.but_piu_z);
        save=findViewById(R.id.save);
        updateTool=findViewById(R.id.updateTool);
        exit=findViewById(R.id.exit);
        gpsdebugg=findViewById(R.id.gpsdebugg);
        deltaX=findViewById(R.id.deltaX);
        deltaY=findViewById(R.id.deltaY);
        deltaZ=findViewById(R.id.deltaZ);
        titolo=findViewById(R.id.titolo);
        txtDeltaX=findViewById(R.id.txtDeltaX);
        txtDeltaYt=findViewById(R.id.txtDeltaYt);
        txtDeltaZt=findViewById(R.id.txtDeltaZt);
        txtDeltaX.setText("Tool ΔX " + Utils.getMetriSimbol());
        txtDeltaYt.setText("Tool ΔY " + Utils.getMetriSimbol());
        txtDeltaZt.setText("Tool ΔZ " + Utils.getMetriSimbol());
    }

    private void init() {

    }

    private void onClick() {
        save.setOnClickListener(view -> {
            disableAll();
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });
        exit.setOnClickListener(view -> {
            disableAll();
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();

        });
        updateTool.setOnClickListener(view -> {

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
        hdt = NmeaListener.mch_Orientation + DataSaved.deltaGPS2;
        hdt = hdt % 360;

        titolo.setText("     E: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.toolEndCoord[0])) + "    N: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.toolEndCoord[1])) + "  " + "   Z: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.toolEndCoord[2])) + "   HDT: " + String.format("%.2f", hdt).replace(",", ".") + " °");

        if (DataSaved.gpsOk) {
            gpsdebugg.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            gpsdebugg.setImageTintList(ColorStateList.valueOf(Color.RED));
        }
    }

    private void disableAll(){
        save.setEnabled(false);
        gpsdebugg.setEnabled(false);
        exit.setEnabled(false);
        updateTool.setEnabled(false);

    }
}