package gui.debug_ecu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.ExcavatorMenuActivity;
import gui.boot_and_choose.Nuova_Choose;

public class Hydro_Lobby extends BaseClass {
    ImageView back;
    ImageButton normal_hydro,joy_interface;
    boolean press=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydro_lobby);
        press=false;
        back=findViewById(R.id.back);
        normal_hydro=findViewById(R.id.normal_hydro);
        joy_interface=findViewById(R.id.joy_interfce);

        back.setOnClickListener(view ->  {
            if(!press) {
                press = true;
                startActivity(new Intent(this, ExcavatorMenuActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
        normal_hydro.setOnClickListener(view -> {
            if(!press) {
                press = true;
                startActivity(new Intent(this, Hydraulic_Setup.class));
                overridePendingTransition(0, 0);
                finish();
            }

        });
        joy_interface.setOnClickListener(view -> {
            if(!press) {
                press = true;
                startActivity(new Intent(this, Joy_Interface_Setup.class));
                overridePendingTransition(0, 0);
                finish();
            }

        });

    }
}