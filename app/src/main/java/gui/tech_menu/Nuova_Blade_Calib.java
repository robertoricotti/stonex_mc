package gui.tech_menu;

import static gui.MyApp.errorCode;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;
import utils.Utils;

public class Nuova_Blade_Calib extends BaseClass {
    int count=0;
    ConstraintLayout pag_1,pag_2,pag_3;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    ImageView save,update,exit,toLeft,toRight,status,wifi;
    TextView txtCoord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nuova_grade_calib);

        findView();
        onClick();
        updateUI();
    }

    private void findView(){
        dialogGnssCoordinates=new Dialog_GNSS_Coordinates(this);
        save=findViewById(R.id.salva);
        update=findViewById(R.id.update);
        exit=findViewById(R.id.exit);
        toLeft=findViewById(R.id.toLeft);
        toRight=findViewById(R.id.toRight);
        status=findViewById(R.id.img00);
        wifi=findViewById(R.id.img01);
        txtCoord=findViewById(R.id.txtCoord);
        pag_1=findViewById(R.id.pag_1);
        pag_2=findViewById(R.id.pag_2);
        pag_3=findViewById(R.id.pag_3);
    }

    private void onClick(){
        exit.setOnClickListener(view -> {
            startActivity(new Intent(this,Nuova_Machine_Settings.class));
            finish();
        });

        save.setOnClickListener(view -> {
            startActivity(new Intent(this,Nuova_Machine_Settings.class));
            finish();
        });
        update.setOnClickListener(view -> {

        });
        status.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });
        toLeft.setOnClickListener(view -> {
            if (count>0) {
                count--;
            }else {
                count=0;
            }

        });
        toRight.setOnClickListener(view -> {
            if(count<2){
                count++;
            }else {
                count=2;
            }

        });
    }

    public void updateUI(){
        try {
            switch (count){
                case 0:
                    pag_1.setVisibility(TextView.VISIBLE);
                    pag_2.setVisibility(TextView.GONE);
                    pag_3.setVisibility(TextView.GONE);
                    break;

                case 1:
                    pag_1.setVisibility(TextView.GONE);
                    pag_2.setVisibility(TextView.VISIBLE);
                    pag_3.setVisibility(TextView.GONE);
                    break;

                case 2:
                    pag_1.setVisibility(TextView.GONE);
                    pag_2.setVisibility(TextView.GONE);
                    pag_3.setVisibility(TextView.VISIBLE);
                    break;
                default:
                    pag_1.setVisibility(TextView.GONE);
                    pag_2.setVisibility(TextView.GONE);
                    pag_3.setVisibility(TextView.GONE);
                    break;
            }
            if (DataSaved.gpsOk && errorCode == 0) {
                status.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                status.setImageTintList(ColorStateList.valueOf(Color.RED));
            }
            if(count==0){
                toLeft.setVisibility(TextView.INVISIBLE);
            }else {
                toLeft.setVisibility(TextView.VISIBLE);
            }
            if(count==2){
                toRight.setVisibility(TextView.INVISIBLE);
            }else {
                toRight.setVisibility(TextView.VISIBLE);
            }
            DataSaved.offset_Z_antenna=0;
            double hdt=0;
            hdt= NmeaListener.mch_Orientation + DataSaved.deltaGPS2;
            hdt=hdt%360;
            txtCoord.setText("     E: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[0])) + "    N: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[1])) + "  " + "   Z: " + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[2])) + "   HDT: "+String.format("%.2f",hdt).replace(",",".")+" °");
        } catch (Exception ignored) {

        }

    }
}
