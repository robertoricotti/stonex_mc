package hydro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ImageDecoderKt;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stx_dig.R;

import gui.debug_ecu.Hydraulic_Setup;
import gui.debug_ecu.Hydro_Lobby;
import gui.dialogs_and_toast.CustomToast;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;
import utils.MyData;

public class Hydro_Activity_Entering extends AppCompatActivity {
    int indexMachine;

    ImageView exit,to_calib;
    TextView cat,komatsu,deere,cnh,doosan,valve;
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



    }
    private void onClick(){
        exit.setOnClickListener(view -> {
            exit.setEnabled(false);
            startActivity(new Intent(this, ExcavatorChooserActivity.class));
            finish();

        });
        to_calib.setOnClickListener(view -> {

            switch (DataSaved.Interface_Type){
                case 0:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, Hydraulic_Setup.class));
                    finish();
                    break;

                case 1:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, CAT_SEA_Activity.class));
                    finish();
                    break;

                case 2:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, DEERE_LIEBHERR_Activity.class));
                    finish();
                    break;

                case 3:
                    to_calib.setEnabled(false);
                    startActivity(new Intent(Hydro_Activity_Entering.this, KOMATSU_Activity.class));
                    finish();

                    break;
                case 4:
                    //to_calib.setEnabled(false);
                    break;
                case 5:
                    //to_calib.setEnabled(false);
                    break;
            }


        });
        cat.setOnLongClickListener(view -> {
            DataSaved.Interface_Type=1;
            MyData.push("M"+indexMachine+"Interface_Type","1");
            return false;

        });
        deere.setOnLongClickListener(view -> {
            DataSaved.Interface_Type=2;
            MyData.push("M"+indexMachine+"Interface_Type","2");
            return false;
        });
        komatsu.setOnLongClickListener(view -> {
            DataSaved.Interface_Type=3;
            MyData.push("M"+indexMachine+"Interface_Type","3");
            return false;
        });
        cnh.setOnLongClickListener(view -> {
            new CustomToast(Hydro_Activity_Entering.this,"Not Implemented").show_error();
           /* DataSaved.Interface_Type=4;
            MyData.push("M"+indexMachine+"Interface_Type","4");

            */
            return false;
        });
        doosan.setOnLongClickListener(view -> {
            new CustomToast(Hydro_Activity_Entering.this,"Not Implemented").show_error();
            /*
            DataSaved.Interface_Type=5;
            MyData.push("M"+indexMachine+"Interface_Type","5");

             */
            return false;
        });
        valve.setOnLongClickListener(view -> {
            DataSaved.Interface_Type=0;
            MyData.push("M"+indexMachine+"Interface_Type","0");
            return false;
        });

    }
    public void updateUI(){

        switch (DataSaved.Interface_Type){
            case 0:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));

                break;

            case 1:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;

            case 2:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;

            case 3:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;

            case 4:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                break;

            case 5:
                valve.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cat.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                deere.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                komatsu.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                cnh.setBackground(getDrawable(R.drawable.sfondo_bottone_grigio));
                doosan.setBackground(getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                break;
        }

    }


}