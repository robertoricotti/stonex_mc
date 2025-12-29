package gui.tech_menu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;


import gui.BaseClass;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.exca.Sensors_Decoder_Drill;
import utils.MyData;

public class ToolSensor extends BaseClass {
    CheckBox off,left,right,fwd,bwd;
    TextView anglePitch,angleRoll,offPitch,offRoll;
    Button setPitch,setRoll;
    ImageView save, exit;
    int indexMach,toolMount;
    double tempOffP,tempOffR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_sensor);
        indexMach= MyData.get_Int( "MachineSelected");
        findView();
        init();
        onClick();

    }

    private void findView(){
        toolMount=MyData.get_Int("M"+indexMach+"toolMountPos");
        tempOffR=MyData.get_Double("M"+indexMach+"offset_Tool_Roll");
        tempOffP=MyData.get_Double("M"+indexMach+"offset_Tool_Pitch");
        off=findViewById(R.id.cbxOff);
        left=findViewById(R.id.cbxLeft);
        right=findViewById(R.id.cbxRight);
        fwd=findViewById(R.id.cbxFwd);
        bwd=findViewById(R.id.cbxBwd);
        exit=findViewById(R.id.exit);
        save=findViewById(R.id.save);
        anglePitch=findViewById(R.id.pitchAngle_tv);
        angleRoll=findViewById(R.id.rollAngle_tv);
        offPitch=findViewById(R.id.pitchOffsetAngle_tv);
        offRoll=findViewById(R.id.rollOffsetAngle_tv);
        setPitch=findViewById(R.id.offsetSetZeroPitch);
        setRoll=findViewById(R.id.offsetSetZeroRoll);


    }
    private void init(){
        checkChecked(toolMount);

    }
    private void onClick(){
        off.setOnClickListener(view -> {
            DataSaved.lrTool=0;
            checkChecked(DataSaved.lrTool);
        });
        left.setOnClickListener(view -> {
            DataSaved.lrTool=1;
            checkChecked(DataSaved.lrTool);
        });
        right.setOnClickListener(view -> {
            DataSaved.lrTool=2;
            checkChecked(DataSaved.lrTool);
        });
        fwd.setOnClickListener(view -> {
            DataSaved.lrTool=3;
            checkChecked(DataSaved.lrTool);
        });
        bwd.setOnClickListener(view -> {
            DataSaved.lrTool=4;
            checkChecked(DataSaved.lrTool);
        });


        exit.setOnClickListener(view -> {

            DataSaved.lrTool=MyData.get_Int("M"+indexMach+"toolMountPos");
            DataSaved.offset_Tool_Roll=MyData.get_Double("M"+indexMach+"offset_Tool_Roll");
            DataSaved.offset_Tool_Pitch=MyData.get_Double("M"+indexMach+"offset_Tool_Pitch");
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();

        });

        save.setOnClickListener(view -> {
            MyData.push("M"+indexMach+"toolMountPos",String.valueOf(DataSaved.lrTool));
            MyData.push("M"+indexMach+"offset_Tool_Roll",String.valueOf(DataSaved.offset_Tool_Roll));
            MyData.push("M"+indexMach+"offset_Tool_Pitch",String.valueOf( DataSaved.offset_Tool_Pitch));
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });

        setPitch.setOnLongClickListener(view -> {
            setPitch.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offset_Tool_Pitch= Sensors_Decoder.Deg_Tool_Pitch;
            return true;
        });
        setRoll.setOnLongClickListener(view -> {
            setRoll.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offset_Tool_Roll= Sensors_Decoder.Deg_Tool_Roll;
            return true;
        });

    }
    public void updateUI(){
        anglePitch.setText(String.format("%.02f", ExcavatorLib.correctToolPitch).replace(",","."));
        angleRoll.setText(String.format("%.02f", ExcavatorLib.correctToolRoll).replace(",","."));
        offPitch.setText(String.format("%.02f", DataSaved.offset_Tool_Pitch).replace(",","."));
        offRoll.setText(String.format("%.02f", DataSaved.offset_Tool_Roll).replace(",","."));

    }
    private void checkChecked(int t){
        switch (t){
            case 0:
                off.setChecked(true);
                left.setChecked(false);
                right.setChecked(false);
                fwd.setChecked(false);
                bwd.setChecked(false);
                break;
            case 1:
                off.setChecked(false);
                left.setChecked(true);
                right.setChecked(false);
                fwd.setChecked(false);
                bwd.setChecked(false);
                break;

            case 2:
                off.setChecked(false);
                left.setChecked(false);
                right.setChecked(true);
                fwd.setChecked(false);
                bwd.setChecked(false);
                break;

            case 3:
                off.setChecked(false);
                left.setChecked(false);
                right.setChecked(false);
                fwd.setChecked(true);
                bwd.setChecked(false);
                break;

            case 4:
                off.setChecked(false);
                left.setChecked(false);
                right.setChecked(false);
                fwd.setChecked(false);
                bwd.setChecked(true);
                break;
        }
    }
}