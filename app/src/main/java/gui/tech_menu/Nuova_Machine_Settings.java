package gui.tech_menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stx_dig.R;

import gui.projects.PickProject;
import packexcalib.exca.DataSaved;
import utils.MyData;

public class Nuova_Machine_Settings extends AppCompatActivity {

    ImageView back,exca,wheel,grader,dozer,menu_1,menu_2;
    ConstraintLayout constraintLayout,constraintLayout_2;
    TextView tvFrame,tvBoom1,tvBoom2,tvStick,tvLink,tvTilt,tvXYZ;
    int mode,machineSel;
    boolean menu1_visible,menu2_visible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrazione_macchine);
        findView();
        onClick();
        updateCK();
        updateUI();
    }
    private void findView(){
        machineSel = MyData.get_Int("MachineSelected");
        mode = MyData.get_Int("M" + machineSel + "_isWL");
        back=findViewById(R.id.btn_1);
        exca=findViewById(R.id.sel_exca);
        wheel=findViewById(R.id.sel_wheel);
        grader=findViewById(R.id.sel_grader);
        dozer=findViewById(R.id.sel_dozer);
        menu_1=findViewById(R.id.bt_menu1);
        menu_2=findViewById(R.id.bt_sens_set);
        constraintLayout=findViewById(R.id.constraint_general);
        constraintLayout_2=findViewById(R.id.constr_2);
        tvFrame=findViewById(R.id.toFrame);
        tvBoom1=findViewById(R.id.toBoom1);
        tvBoom2=findViewById(R.id.toBoom2);
        tvStick=findViewById(R.id.toStick);
        tvLink=findViewById(R.id.toDogBone);
        tvTilt=findViewById(R.id.toTilt);
        tvXYZ=findViewById(R.id.toxyz);


    }
    private void onClick(){
        tvFrame.setOnClickListener(view -> {
            startActivity(new Intent(this, FrameCalib.class));
            finish();
        });
        tvBoom1.setOnClickListener(view -> {
            startActivity(new Intent(this, Boom1Calib.class));
            finish();
        });
        tvBoom2.setOnClickListener(view -> {
            startActivity(new Intent(this, Boom2Calib.class));
            finish();
        });
        tvStick.setOnClickListener(view -> {
            startActivity(new Intent(this, StickCalib.class));
            finish();
        });
        tvLink.setOnClickListener(view -> {
            startActivity(new Intent(this, LinkageCalib.class));
            finish();
        });
        tvTilt.setOnClickListener(view -> {
            startActivity(new Intent(this, TiltCalib.class));
            finish();
        });
        tvXYZ.setOnClickListener(view -> {
            startActivity(new Intent(this, XYZ_Calib.class));
            finish();
        });
        menu_1.setOnClickListener(view -> {
            menu1_visible=!menu1_visible;
            updateCK();
        });
        menu_2.setOnClickListener(view -> {
            menu2_visible=!menu2_visible;
            updateCK();
        });
        back.setOnClickListener(view -> {
            startActivity(new Intent(this, ExcavatorChooserActivity.class));
            finish();
        });
        exca.setOnClickListener(view -> {
            MyData.push("M" + machineSel + "_isWL", "0");
            DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
            mode = DataSaved.isWL;
            updateCK();
        });
        wheel.setOnClickListener(view -> {
            MyData.push("M" + machineSel + "_isWL", "1");
            DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
            mode = DataSaved.isWL;
            updateCK();
        });
        dozer.setOnClickListener(view -> {
            MyData.push("M" + machineSel + "_isWL", "2");
            DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
            mode = DataSaved.isWL;
            updateCK();
        });
        grader.setOnClickListener(view -> {
            MyData.push("M" + machineSel + "_isWL", "4");
            DataSaved.isWL = MyData.get_Int("M" + machineSel + "_isWL");
            mode = DataSaved.isWL;
            updateCK();
        });
    }
    private void updateUI(){

    }
    private void updateCK(){
        if(menu1_visible){
            menu_1.setImageResource(R.drawable.keyboard_arrow_down_96);
            constraintLayout.setVisibility(View.VISIBLE);
        }else {
            constraintLayout.setVisibility(View.GONE);
            menu_1.setImageResource(R.drawable.key_arrow_right);
        }
        if(menu2_visible){
            menu_2.setImageResource(R.drawable.keyboard_arrow_down_96);
            constraintLayout_2.setVisibility(View.VISIBLE);
        }else {
            constraintLayout_2.setVisibility(View.GONE);
            menu_2.setImageResource(R.drawable.key_arrow_right);
        }
        switch (DataSaved.isWL){
            case 0:
                exca.setAlpha(1.0f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(0.2f);
                break;
            case 1:
                exca.setAlpha(0.2f);
                wheel.setAlpha(1f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(0.2f);
                break;
            case 2:
            case 3:
                exca.setAlpha(0.2f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(1f);
                grader.setAlpha(0.2f);
                break;
            case 4:
                exca.setAlpha(0.2f);
                wheel.setAlpha(0.2f);
                dozer.setAlpha(0.2f);
                grader.setAlpha(1f);
                break;
        }

    }
}