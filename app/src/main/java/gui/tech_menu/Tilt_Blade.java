package gui.tech_menu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import services.CanService;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class Tilt_Blade extends AppCompatActivity {
    Button setPitch,setRoll;
    ImageView save,cancex;
    int indexMachineSelected, bucketMountPos;
    CheckBox off,fw,bw,vert_up,vert_dw;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;
    TextView pitch,roll,offsetPitch,offsetRoll;
    int countZero,countZeroP;
    boolean minusPressedP,minusPressedR,plusPressedR,plusPressedP;
    Button offsetMinusPitch,offsetPlusPitch,offsetMinus,offsetPlus;

    int indexMeasure = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilt_blade);
        save=findViewById(R.id.save);
        cancex=findViewById(R.id.exit);
        off=findViewById(R.id.cbxOff);
        fw=findViewById(R.id.cbxFw);
        bw=findViewById(R.id.cbxBw);
        pitch=findViewById(R.id.pitchAngle_tv);
        roll=findViewById(R.id.dogBoneAngle_tv);
        setPitch=findViewById(R.id.offsetSetZeroPitch);
        setRoll=findViewById(R.id.offsetSetZero);
        offsetPitch=findViewById(R.id.pitchOffsetAngle_tv);
        offsetRoll=findViewById(R.id.dogBoneOffsetAngle_tv);
        offsetMinus=findViewById(R.id.offsetMinus);
        offsetPlus=findViewById(R.id.offsetPlus);
        offsetMinusPitch=findViewById(R.id.offsetMinusPitch);
        offsetPlusPitch=findViewById(R.id.offsetPlusPitch);
        vert_up=findViewById(R.id.vert_up);
        vert_dw=findViewById(R.id.vert_dw);
        init();
        onClick();
        updateUI();



    }

    private void init(){
        indexMachineSelected = MyData.get_Int("MachineSelected");

        indexMeasure = MyData.get_Int("Unit_Of_Measure");

        if (indexMeasure == 4 || indexMeasure == 5) {
            numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
        } else {
            numberDialog = new CustomNumberDialog(this, -1);
        }

        bucketMountPos = MyData.get_Int("M" + indexMachineSelected + "_Bucket_MountPos");
        switch (bucketMountPos) {
            case 0:
                off.setChecked(true);
                fw.setChecked(false);
                bw.setChecked(false);
                vert_up.setChecked(false);
                vert_dw.setChecked(false);
                break;
            case 1:
                off.setChecked(false);
                fw.setChecked(true);
                bw.setChecked(false);
                vert_up.setChecked(false);
                vert_dw.setChecked(false);
                break;
            case -1:
                off.setChecked(false);
                fw.setChecked(false);
                bw.setChecked(true);
                vert_up.setChecked(false);
                vert_dw.setChecked(false);
                break;
            case 2://vert_up
                off.setChecked(false);
                fw.setChecked(false);
                bw.setChecked(false);
                vert_up.setChecked(true);
                vert_dw.setChecked(false);

                break;
            case 3://vert_dw
                off.setChecked(false);
                fw.setChecked(false);
                bw.setChecked(false);
                vert_up.setChecked(false);
                vert_dw.setChecked(true);
                break;
            default:
                off.setChecked(true);
                fw.setChecked(false);
                bw.setChecked(false);
                vert_up.setChecked(false);
                vert_dw.setChecked(false);
                break;

        }


    }
    private void ckInit(){
        switch (bucketMountPos) {
            case 0:
                off.setChecked(true);
                fw.setChecked(false);
                bw.setChecked(false);
                vert_up.setChecked(false);
                vert_dw.setChecked(false);
                break;
            case 1:
                off.setChecked(false);
                fw.setChecked(true);
                bw.setChecked(false);
                vert_up.setChecked(false);
                vert_dw.setChecked(false);
                break;
            case -1:
                off.setChecked(false);
                fw.setChecked(false);
                bw.setChecked(true);
                vert_up.setChecked(false);
                vert_dw.setChecked(false);
                break;
            case 2://vert_up
                off.setChecked(false);
                fw.setChecked(false);
                bw.setChecked(false);
                vert_up.setChecked(true);
                vert_dw.setChecked(false);

                break;
            case 3://vert_dw
                off.setChecked(false);
                fw.setChecked(false);
                bw.setChecked(false);
                vert_up.setChecked(false);
                vert_dw.setChecked(true);
                break;
            default:
                off.setChecked(true);
                fw.setChecked(false);
                bw.setChecked(false);
                vert_up.setChecked(false);
                vert_dw.setChecked(false);
                break;

        }
        DataSaved.lrBucket=bucketMountPos;
    }
    @SuppressLint("ClickableViewAccessibility")
    private void onClick(){
        setPitch.setOnLongClickListener(view -> {
            setPitch.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offsetPitch = Sensors_Decoder.Deg_pitch;
            return true;
        });
        setRoll.setOnLongClickListener(view -> {
            setRoll.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            DataSaved.offsetRoll = Sensors_Decoder.Deg_roll;
            return true;
        });
        off.setOnClickListener(view -> {
            bucketMountPos=0;
            ckInit();
        });
        fw.setOnClickListener(view -> {
            bucketMountPos=1;
            ckInit();
        });
        bw.setOnClickListener(view -> {
            bucketMountPos=-1;

            ckInit();
        });
        vert_up.setOnClickListener(view -> {
            bucketMountPos=2;
            ckInit();
        });
        vert_dw.setOnClickListener(view -> {
            bucketMountPos=3;
            ckInit();
        });
        save.setOnClickListener(view -> {
            disableAll();
            MyData.push("M" + indexMachineSelected + "_Bucket_MountPos", String.valueOf(bucketMountPos));
            MyData.push("M" + indexMachineSelected + "_OffsetFrameY", String.valueOf(DataSaved.offsetPitch));
            MyData.push("M" + indexMachineSelected + "_OffsetFrameX", String.valueOf(DataSaved.offsetRoll));
            startActivity(new Intent(this,Nuova_Machine_Settings.class));
            startService(new Intent(this, UpdateValuesService.class));
            finish();

        });
        cancex.setOnClickListener(view -> {
            disableAll();
            DataSaved.lrBucket=MyData.get_Int("M" + indexMachineSelected + "_Bucket_MountPos");
            DataSaved.offsetPitch=MyData.get_Double("M" + indexMachineSelected + "_OffsetFrameY");
            DataSaved.offsetRoll=MyData.get_Double("M" + indexMachineSelected + "_OffsetFrameX");
            startActivity(new Intent(this,Nuova_Machine_Settings.class));
            finish();
        });

        //
        offsetMinusPitch.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                minusPressedP = true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                minusPressedP = false;
                countZeroP=0;
            }
            return false;
        });
        offsetPlusPitch.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                plusPressedP = true;

            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                plusPressedP = false;
                countZeroP=0;
            }
            return false;
        });

        offsetMinus.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                minusPressedR = true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                minusPressedR = false;
                countZero=0;
            }
            return false;
        });
        offsetPlus.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                plusPressedR = true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                plusPressedR = false;
                countZero=0;
            }
            return false;
        });
        offsetMinusPitch.setOnClickListener((View v) -> {
            DataSaved.offsetPitch -= 0.05;
        });

        offsetPlusPitch.setOnClickListener((View v) -> {
            DataSaved.offsetPitch += 0.05;
        });

        offsetMinus.setOnClickListener((View v) -> {
            DataSaved.offsetRoll -= 0.05;
        });

        offsetPlus.setOnClickListener((View v) -> {
            DataSaved.offsetRoll += 0.05;
        });

    }
    private void disableAll(){
        save.setEnabled(false);
        cancex.setEnabled(false);
    }
    @SuppressLint("DefaultLocale")
    public void updateUI(){
        pitch.setText(String.format("%.02f", ExcavatorLib.correctPitch).replace(",", "."));
        roll.setText(String.format("%.02f",ExcavatorLib.correctRoll).replace(",", "."));
        offsetPitch.setText(String.format("%.02f",DataSaved.offsetPitch).replace(",", "."));
        offsetRoll.setText(String.format("%.02f",DataSaved.offsetRoll).replace(",", "."));
        if(!CanService.tiltDisc){
            pitch.setTextColor(getColor(R.color.blue));
            roll.setTextColor(getColor(R.color.blue));
        }else {

            pitch.setTextColor(getColor(R.color.red));
            roll.setTextColor(getColor(R.color.red));
        }

        if (minusPressedR && plusPressedR) {
            countZero++;
            if(countZero>40) {
                DataSaved.offsetRoll = 0;
            }
        }
        if (minusPressedP && plusPressedP) {
            countZeroP++;
            if(countZeroP>40) {
                DataSaved.offsetPitch = 0;
            }
        }

    }
}