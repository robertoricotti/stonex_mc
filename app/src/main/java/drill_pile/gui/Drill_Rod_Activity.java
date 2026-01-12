package drill_pile.gui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.my_opengl.My3DActivity;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class Drill_Rod_Activity extends BaseClass {
    int indexMachine;
    int indexMeasure;
    ImageView save;
    Intent goBackIntent;
    ImageView gpsDebug;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;
    Dialog_Drill_GNSS dialogDrillGnss;
    TextView titolo,t1,t2,t3;
    EditText firstR,nextR,bitL,bitW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_rod);
        findView();
        init();
        onClick();


    }
    private void findView(){
        indexMachine= MyData.get_Int("MachineSelected");
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        if (indexMeasure == 4 || indexMeasure == 5) {
            numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
        } else {
            numberDialog = new CustomNumberDialog(this, -1);
        }
        save=findViewById(R.id.save);
        titolo=findViewById(R.id.titolo);
        gpsDebug=findViewById(R.id.gpsdebugg);
        t1=findViewById(R.id.t1);
        t2=findViewById(R.id.t2);
        t3=findViewById(R.id.t3);
        firstR=findViewById(R.id.firstR);
        nextR=findViewById(R.id.nextR);
        bitL=findViewById(R.id.bitL);
        bitW=findViewById(R.id.bitW);

        numberDialog=new CustomNumberDialog(this,-1);
        numberDialogFtIn=new CustomNumberDialogFtIn(this,-1);
        dialogDrillGnss =new Dialog_Drill_GNSS(this);

    }
    private void init(){
        try {

            String whoDig=getIntent().getStringExtra("whoDrill");
            if(whoDig==null) {
                goBackIntent = new Intent(this, Activity_Home_Page.class);
            }else {
                if(whoDig.contains("ExcavatorChooserActivity")){
                    goBackIntent=new Intent(this, ExcavatorChooserActivity.class);
                }else if (whoDig.contains("Drill_Activity")){
                    goBackIntent=new Intent(this, Drill_Activity.class);
                }
            }
        } catch (Exception e) {

            goBackIntent = new Intent(this, Activity_Home_Page.class);
        }
        t1.setText(getResources().getString(R.string.first_rod_l)+" "+Utils.getMetriSimbol());
        t2.setText(getResources().getString(R.string.rod_l)+" "+Utils.getMetriSimbol());
        t3.setText(getResources().getString(R.string.bit_l)+" "+Utils.getMetriSimbol());
        updateTxt();
    }
    private void onClick(){
        numberDialog.dialog.setOnDismissListener(dialog -> {
            Log.d("NumberDialog", "Dialog chiusa");
            updateOnClose();
            updateTxt();


        });
        save.setOnClickListener(view -> {
            save.setEnabled(false);
            firstR.setEnabled(false);
            nextR.setEnabled(false);
            bitL.setEnabled(false);
            bitW.setEnabled(false);
            gpsDebug.setEnabled(false);
            salva();
            startActivity(goBackIntent);
            finish();

        });


        gpsDebug.setOnClickListener(view -> {
            if (!dialogDrillGnss.alertDialog.isShowing()) {
                    dialogDrillGnss.show();
                }

        });

        firstR.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(firstR);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(firstR);
            }

        });

        nextR.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(nextR);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(nextR);
            }

        });
        bitL.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(bitL);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(bitL);
            }

        });
        bitW.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(bitW);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(bitW);
            }

        });

    }
    public void updateUI(){
        if (DataSaved.gpsOk) {
            gpsDebug.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            gpsDebug.setImageTintList(ColorStateList.valueOf(Color.RED));
        }
        titolo.setText("     E: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.toolEndCoord[0])) + "    N: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.toolEndCoord[1])) + "  " + "   Z: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.toolEndCoord[2])));

    }

    private void salva(){
        MyData.push("M" + indexMachine + "drill_First_Rod_Len", Utils.writeMetri(firstR.getText().toString().replace(",", ".")));
        MyData.push("M" + indexMachine + "drill_Rod_Len", Utils.writeMetri(nextR.getText().toString().replace(",", ".")));
        MyData.push("M" + indexMachine + "drill_Bit_Len", Utils.writeMetri(bitL.getText().toString().replace(",", ".")));
        MyData.push("M" + indexMachine + "drill_Bit_Width", Utils.writeMetri(bitW.getText().toString().replace(",", ".")));
        startService(new Intent(this, UpdateValuesService.class));
    }

    private void updateOnClose() {
        DataSaved.drill_First_Rod_Len = Double.parseDouble(Utils.writeMetri((firstR.getText().toString())));
        DataSaved.drill_Rod_Len = Double.parseDouble(Utils.writeMetri((nextR.getText().toString())));
        DataSaved.drill_Bit_Len = Double.parseDouble(Utils.writeMetri((bitL.getText().toString())));
        DataSaved.drill_Bit_Width=Double.parseDouble(Utils.writeMetri((bitW.getText().toString())));
    }
    private void updateTxt() {
        firstR.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.drill_First_Rod_Len)));
        nextR.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.drill_Rod_Len)));
        bitL.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.drill_Bit_Len)));
        bitW.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.drill_Bit_Width)));
    }
}