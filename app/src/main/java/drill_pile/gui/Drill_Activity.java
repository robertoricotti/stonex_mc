package drill_pile.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.stx_dig.R;

import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;

public class Drill_Activity extends AppCompatActivity {
    Dialog_Drill_GNSS dialogDrillGnss;
    View divisorioC,divisorioDx,divisorioUp,divisorioDw;
    ImageView digMenu,drilltool,Status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill);
        findView();
        init();
        onClick();

    }

    private void findView(){
        dialogDrillGnss=new Dialog_Drill_GNSS(this);
        divisorioC=findViewById(R.id.divisorioC);
        divisorioDx=findViewById(R.id.divisorioDx);
        divisorioUp=findViewById(R.id.divisorioUp);
        divisorioDw=findViewById(R.id.divisorioDw);
        digMenu=findViewById(R.id.digMenu);
        Status=findViewById(R.id.Status);
        drilltool=findViewById(R.id.drilltool);

    }
    private void init(){

    }
    private void onClick(){
        digMenu.setOnClickListener(view -> {
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });
        Status.setOnClickListener(view -> {
            if(!dialogDrillGnss.alertDialog.isShowing()){
                dialogDrillGnss.show();
            }
        });
        drilltool.setOnClickListener(view -> {
            Intent i = new Intent(this, Drill_Rod_Activity.class);
            i.putExtra("whoDrill", String.valueOf(MyApp.visibleActivity));
            startActivity(i);
            finish();
        });

    }

    public void updateUI(){

    }
}