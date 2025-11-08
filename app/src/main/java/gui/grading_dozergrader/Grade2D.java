package gui.grading_dozergrader;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.DialogSlope;
import gui.dialogs_and_toast.Dialog_Sensors_Setting;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import services.CanService;
import utils.MyDeviceManager;
import utils.Utils;

public class Grade2D extends AppCompatActivity {
    View linea;
    DialogSlope dialogSlope;
    int blink = 0;
    ImageView back, sensL, sensR, status;
    TextView auto;
    ImageView scendiSX, okSX, saliSX, scendiDX, okDX, saliDX;
    TextView autoL, autoR,slopeSet;
    Guideline leftGuid, rightGuid;
    final float LEFT_GUIDE_1D = 1f;
    final float RIGHT_GUIDE_1D = 0f;
    final float LEFT_GUIDE_2D = 0.45f;
    final float RIGHT_GUIDE_2D = 0.55f;
    Dialog_Sensors_Setting dialogSensorsSetting;
    ImageView lama;
    TextView degrees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade2_d);
        findView();
        init();
        onClick();
        enableAll();
    }

    private void findView() {
        back = findViewById(R.id.back);
        sensL = findViewById(R.id.sensleft);
        sensR = findViewById(R.id.sensright);
        auto = findViewById(R.id.setAuto);
        status = findViewById(R.id.status);
        scendiSX = findViewById(R.id.scendiSX);
        scendiDX = findViewById(R.id.scendiDX);
        saliSX = findViewById(R.id.saliSX);
        saliDX = findViewById(R.id.saliDX);
        okSX = findViewById(R.id.okSX);
        okDX = findViewById(R.id.okDX);
        leftGuid = findViewById(R.id.guiVleft);
        rightGuid = findViewById(R.id.guiVright);
        autoL = findViewById(R.id.leftAuto);
        autoR = findViewById(R.id.rightAuto);
        lama=findViewById(R.id.lama);
        degrees=findViewById(R.id.degrees);
        slopeSet=findViewById(R.id.slopeSet);
        linea=findViewById(R.id.linea);
    }

    private void init() {
        dialogSensorsSetting = new Dialog_Sensors_Setting(this);
        dialogSlope=new DialogSlope(this);
        if (DataSaved.rightSensorType == 0) {
            leftGuid.setGuidelinePercent(LEFT_GUIDE_1D);
            rightGuid.setGuidelinePercent(RIGHT_GUIDE_1D);
            autoR.setVisibility(View.GONE);
        } else {
            leftGuid.setGuidelinePercent(LEFT_GUIDE_2D);
            rightGuid.setGuidelinePercent(RIGHT_GUIDE_2D);
        }
    }

    private void disableAll() {
        back.setEnabled(false);
        sensL.setEnabled(false);
        sensR.setEnabled(false);
        auto.setEnabled(false);
        status.setEnabled(false);
    }

    private void enableAll() {
        back.setEnabled(true);
        sensL.setEnabled(true);
        sensR.setEnabled(true);
        auto.setEnabled(true);
        status.setEnabled(true);
    }

    private void onClick() {
        back.setOnClickListener(view -> {
            disableAll();
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();

        });
        auto.setOnClickListener(view -> {
            if(DataSaved.Interface_Type==0){
               if(CanService.isAuto!=3){
                   CanService.isAuto=3;
               }else {
                   CanService.isAuto=0;
               }

            }else {
                
                MyDeviceManager.CanWrite(true,0, 0x65, 8, new byte[]{48, 0, 0, 0, 0, 0, 0, 0});
            }
        });
        autoL.setOnClickListener(view -> {
            if(DataSaved.Interface_Type==0){}else {
            MyDeviceManager.CanWrite(true,0, 0x65, 8, new byte[]{16, 0, 0, 0, 0, 0, 0, 0});}
        });
        autoR.setOnClickListener(view -> {
            if(DataSaved.Interface_Type==0){}else {
            MyDeviceManager.CanWrite(true,0, 0x65, 8, new byte[]{32, 0, 0, 0, 0, 0, 0, 0});}
        });
        sensL.setOnClickListener(view -> {
            if (!dialogSensorsSetting.dialog.isShowing()) {
                dialogSensorsSetting.show();
            }
        });
        sensR.setOnClickListener(view -> {
            if (!dialogSensorsSetting.dialog.isShowing()) {
                dialogSensorsSetting.show();
            }
        });
        lama.setOnClickListener(view -> {
            if(!dialogSlope.dialog.isShowing()){
                dialogSlope.show();
            }
        });

    }

    public void updateUI() {
        //do your stuff
        update();

        if(DataSaved.rightSensorType==2||DataSaved.leftSensorType==2){
            lama.setVisibility(View.VISIBLE);
            degrees.setVisibility(View.VISIBLE);
            slopeSet.setVisibility(View.VISIBLE);
            linea.setVisibility(View.VISIBLE);
            lama.setRotation((float) ExcavatorLib.correctRoll);
            degrees.setRotation((float) ExcavatorLib.correctRoll);
            linea.setRotation((float) DataSaved.slopeX);
        }else {
            lama.setVisibility(View.INVISIBLE);
            degrees.setVisibility(View.INVISIBLE);
            slopeSet.setVisibility(View.INVISIBLE);
            linea.setVisibility(View.INVISIBLE);
        }

        degrees.setText("\n"+Utils.readAngoloLITE(String.valueOf(ExcavatorLib.correctRoll))+Utils.getGradiSimbol());
        slopeSet.setText("SLOPE"+"\n"+" "+Utils.readAngoloLITE(String.valueOf(DataSaved.slopeX))+Utils.getGradiSimbol());
        switch (CanService.isAuto) {
            case 0:
                autoL.setBackgroundColor(Color.TRANSPARENT);
                autoR.setBackgroundColor(Color.TRANSPARENT);
                break;
            case 1:
                autoL.setBackgroundColor(Color.RED);
                autoR.setBackgroundColor(Color.TRANSPARENT);
                break;
            case 2:
                autoL.setBackgroundColor(Color.TRANSPARENT);
                autoR.setBackgroundColor(Color.RED);
                break;
            case 3:
                autoL.setBackgroundColor(Color.RED);
                autoR.setBackgroundColor(Color.RED);
                break;
        }
        if (CanService.errorEcu == 0&&DataSaved.Interface_Type==1) {

            scendiSX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
            saliSX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
            scendiDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
            saliDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
            okSX.setBackground(getDrawable(R.drawable.custom_background_off_grade));
            okDX.setBackground(getDrawable(R.drawable.custom_background_off_grade));
            blink++;
            if (blink % 5 == 0) {
                status.setImageTintList(ContextCompat.getColorStateList(this, R.color.transparent));
            } else {
                status.setImageTintList(ContextCompat.getColorStateList(this, R.color.red));
            }
        } else {
            blink = 0;
            status.setImageTintList(ContextCompat.getColorStateList(this, R.color.blue));


            if(DataSaved.leftSensorType==1) {
                if (CanService.altosx == 1) {
                    scendiSX.setImageTintList(ContextCompat.getColorStateList(this, R.color.red));
                } else {
                    scendiSX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
                }
                if (CanService.centrosx == 1) {
                    okSX.setBackground(getDrawable(R.drawable.custom_background_ok_grade));
                } else {
                    okSX.setBackground(getDrawable(R.drawable.custom_background_off_grade));
                }
                if (CanService.bassosx == 1) {
                    saliSX.setImageTintList(ContextCompat.getColorStateList(this, R.color.yellow));
                } else {
                    saliSX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
                }
            }else if (DataSaved.leftSensorType==2){
                //da inclinometro

            }



            if(DataSaved.rightSensorType==1) {
                if (CanService.altodx == 1) {
                    scendiDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.red));
                } else {
                    scendiDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
                }
                if (CanService.centrodx == 1) {
                    okDX.setBackground(getDrawable(R.drawable.custom_background_ok_grade));
                } else {
                    okDX.setBackground(getDrawable(R.drawable.custom_background_off_grade));
                }
                if (CanService.bassodx == 1) {
                    saliDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.yellow));
                } else {
                    saliDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
                }
            }else if (DataSaved.rightSensorType==2){
                //da inclinometro

                        if (ExcavatorLib.correctRoll < DataSaved.slopeX + DataSaved.deadbandFlatAngle &&
                                ExcavatorLib.correctRoll > DataSaved.slopeX - DataSaved.deadbandFlatAngle) {
                            degrees.setTextColor(getColor(R.color.green));
                            lama.setImageTintList(getColorStateList(R.color.green));
                            okDX.setBackground(getDrawable(R.drawable.custom_background_ok_grade));
                            scendiDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
                            saliDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
                        }else {
                            okDX.setBackground(getDrawable(R.drawable.custom_background_off_grade));
                            if(ExcavatorLib.correctRoll>DataSaved.slopeX + DataSaved.deadbandFlatAngle){
                                degrees.setTextColor(getColor(R.color.white));
                                lama.setImageTintList(getColorStateList(R.color.white));
                                scendiDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
                                saliDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.yellow));
                            }else if(ExcavatorLib.correctRoll < DataSaved.slopeX - DataSaved.deadbandFlatAngle){
                                degrees.setTextColor(getColor(R.color.white));
                                lama.setImageTintList(getColorStateList(R.color.white));
                                scendiDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.red));
                                saliDX.setImageTintList(ContextCompat.getColorStateList(this, R.color.gray));
                            }
                        }



            }



        }
        if(DataSaved.leftSensorType==0) {
            scendiSX.setVisibility(View.INVISIBLE);
            okSX.setVisibility(View.INVISIBLE);
            saliSX.setVisibility(View.INVISIBLE);
        }
        if(DataSaved.rightSensorType==0) {
            scendiDX.setVisibility(View.INVISIBLE);
            okDX.setVisibility(View.INVISIBLE);
            saliDX.setVisibility(View.INVISIBLE);
        }
    }

    private void update() {
        switch (DataSaved.leftSensorType) {
            case 0:
                sensL.setImageResource(R.drawable.divieto_96);
                sensL.setRotation(0f);
                break;
            case 1:
                sensL.setImageResource(R.drawable.bucket);
                sensL.setRotation(0f);
                break;
            case 2:
                sensL.setImageResource(R.drawable.moba_sensore);
                sensL.setRotation(0f);
                break;
        }
        switch (DataSaved.rightSensorType) {
            case 0:
                sensR.setImageResource(R.drawable.divieto_96);
                sensR.setRotation(0f);
                break;
            case 1:
                sensR.setImageResource(R.drawable.bucket);
                sensR.setRotation(0f);
                break;
            case 2:
                sensR.setImageResource(R.drawable.moba_sensore);
                sensR.setRotation(0f);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disableAll();
    }
}