package gui.hydro;

import static utils.MyTypes.CASE_BUS;
import static utils.MyTypes.CAT_SEA;
import static utils.MyTypes.JD_LIEBHERR;
import static utils.MyTypes.KOMATSU_CAN;
import static utils.MyTypes.NOBAS;
import static utils.MyTypes.OEM_PROTO;
import static utils.MyTypes.STX_ECU;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomToast;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;
import utils.MyData;
import utils.MyTypes;

public class Hydro_Activity_Entering extends AppCompatActivity {
    int indexMachine;

    ImageView exit,to_calib;
    TextView cat,komatsu,deere,cnh,doosan,valve,nobas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydro_entering);
        try {
            indexMachine=MyData.get_Int("MachineSelected");
        } catch (Exception e) {
            indexMachine=0;
        }
        findView();
        onClick();
        updateUI();

    }

    private void findView(){
        exit=findViewById(R.id.btn_1);
        to_calib=findViewById(R.id.btn_2);
        cat=findViewById(R.id.img_1);
        deere=findViewById(R.id.img_2);
        komatsu=findViewById(R.id.img_3);
        cnh=findViewById(R.id.img_4);
        doosan=findViewById(R.id.img_5);
        valve=findViewById(R.id.img_6);
        nobas=findViewById(R.id.img_nobas);



    }
    private void onClick(){
        exit.setOnClickListener(view -> {
            exit.setEnabled(false);
            startActivity(new Intent(this, ExcavatorChooserActivity.class));
            finish();

        });
        to_calib.setOnClickListener(view -> {

            switch (DataSaved.Interface_Type){
                case STX_ECU:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, ECU_Activity.class));
                    finish();
                    break;

                case CAT_SEA:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, CAT_SEA_Activity.class));
                    finish();
                    break;

                case JD_LIEBHERR:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, DEERE_LIEBHERR_Activity.class));
                    finish();
                    break;

                case KOMATSU_CAN:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, KOMATSU_Activity.class));
                    finish();

                    break;
                case CASE_BUS:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, CASE_Activity.class));
                    finish();

                    break;
                case OEM_PROTO:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, OEM_Activity.class));
                    finish();
                    break;
                case NOBAS:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, ECU_Activity.class));
                    finish();
                    break;
            }


        });
        cat.setOnLongClickListener(view -> {
            DataSaved.Interface_Type= CAT_SEA;
            MyData.push("M"+indexMachine+"Interface_Type","1");
            return false;

        });
        deere.setOnLongClickListener(view -> {
            DataSaved.Interface_Type=JD_LIEBHERR;
            MyData.push("M"+indexMachine+"Interface_Type","2");
            return false;
        });
        komatsu.setOnLongClickListener(view -> {
            DataSaved.Interface_Type=KOMATSU_CAN;
            MyData.push("M"+indexMachine+"Interface_Type","3");
            return false;
        });
        cnh.setOnLongClickListener(view -> {
            DataSaved.Interface_Type=CASE_BUS;
            MyData.push("M"+indexMachine+"Interface_Type","4");


            return false;
        });
        doosan.setOnLongClickListener(view -> {


            DataSaved.Interface_Type=OEM_PROTO;
            MyData.push("M"+indexMachine+"Interface_Type","255");


            return false;
        });
        nobas.setOnLongClickListener(view -> {
            DataSaved.Interface_Type=NOBAS;
            MyData.push("M"+indexMachine+"Interface_Type","5");
            return false;
        });
        valve.setOnLongClickListener(view -> {
            DataSaved.Interface_Type=STX_ECU;
            MyData.push("M"+indexMachine+"Interface_Type","0");
            return false;
        });

    }
    public void updateUI(){

        switch (DataSaved.Interface_Type){
            case STX_ECU:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                nobas.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;

            case CAT_SEA:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                nobas.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;

            case JD_LIEBHERR:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                nobas.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;

            case KOMATSU_CAN:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                nobas.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;

            case CASE_BUS:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                nobas.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;

            case OEM_PROTO:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                nobas.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;
            case NOBAS:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                nobas.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                break;
        }

    }


}