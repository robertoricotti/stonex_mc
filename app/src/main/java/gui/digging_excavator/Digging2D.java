package gui.digging_excavator;


import static gui.MyApp.hAlarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.ExcavatorMenuActivity;

import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogOffset;
import gui.dialogs_and_toast.DialogSlope;
import gui.dialogs_and_toast.Dialog_TouchGo;
import gui.dialogs_and_toast.EasyConfigDialog;
import gui.dialogs_and_toast.HeadingDialog;
import gui.dialogs_and_toast.LaserDialog;
import gui.draw_class.Draw2D_Layer1;
import gui.draw_class.Draw2D_Layer1_Tilt;
import gui.draw_class.Draw2D_Layer2;
import gui.draw_class.Draw2D_Layer2_Tilt;
import gui.draw_class.FlatAngleBar;
import gui.draw_class.HeightLevelBar;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Excavator_RealValues;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.gnss.NmeaListener;
import packexcalib.surfcreator.DistToPoint;
import services.CanSender;
import services.CanService;
import utils.LeicaLB;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;


public class Digging2D extends BaseClass {
    ImageView overlayImage;
    HeadingDialog headingDialog;
    private boolean zommaIn, zommaOut;
    ConstraintLayout mainConstr;
    private double real_height;
    public static double mAltezza2D;
    View divider;
    public static int vista = 0;
    private boolean mSentOFF = false;
    private boolean mSentON = false;
    Guideline centro;
    ConstraintLayout panel1D, panel2D, indicatorH, indicatorH2;
    ImageView setZero, laser, slope, settingOffset, shortcut, navigator, bucketEdge, showStick, depthMode, zoomC, zoomM, zoomP, zeroR;
    TextView leftLed, slopeY2D, slopeX2D, heading_tv;
    LinearLayout rightLed;
    FrameLayout centerLed;
    TextView textOffset, textOffset2, textSlopeX, textSlopeY, textReach, textNameBucket, textNameBucket2;
    Draw2D_Layer1 layer1;
    Draw2D_Layer2 layer2;
    Draw2D_Layer1_Tilt layer1_tilt;
    Draw2D_Layer2_Tilt layer2_tilt;
    HeightLevelBar left, right;
    FlatAngleBar flatAngleBar;
    int indexMachineSelected, indexBucketSelected, indexAudioSystem;
    String heightPivot;
    MediaPlayer mediaPlayer;
    boolean[] audioFlags = new boolean[]{true, true, true, true};


    public static boolean flagLaser_D2D = false;
    public int counterZero = 0;

    ConstraintLayout.LayoutParams params;
    ImageView allarmeAlt;
    Dialog_TouchGo dialogTouchGo;
    DialogOffset dialogOffset;
    DialogSlope dialogSlope;
    EasyConfigDialog easyConfigDialog;
    LaserDialog laserDialog;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dig_2d);
        mainConstr = this.getWindow().findViewById(R.id.mainConstr);
        findView();
        onClick();
        onLongClick();
        init();
        updateUI();


    }

    private void findView() {
        centro = findViewById(R.id.guideline20);
        panel1D = findViewById(R.id.panel1D);
        panel2D = findViewById(R.id.panel2D);
        indicatorH = findViewById(R.id.IndicatorH);
        indicatorH2 = findViewById(R.id.IndicatorH2);
        leftLed = findViewById(R.id.leftCorner);
        centerLed = findViewById(R.id.deltaCenter);
        rightLed = findViewById(R.id.rightCorner);
        setZero = findViewById(R.id.setZero);
        shortcut = findViewById(R.id.shortcut);
        laser = findViewById(R.id.laser);
        settingOffset = findViewById(R.id.settingOffset);
        slope = findViewById(R.id.slope);
        textOffset = findViewById(R.id.offset_tv);//
        textNameBucket = findViewById(R.id.bucket_tv);//
        textOffset2 = findViewById(R.id.offset_tv2);
        textNameBucket2 = findViewById(R.id.bucket_tv2);
        heading_tv = findViewById(R.id.heading_tv);
        textSlopeX = findViewById(R.id.slopeX_tv);
        textSlopeY = findViewById(R.id.slopeY_tv);
        textReach = findViewById(R.id.reach_tv);
        navigator = findViewById(R.id.navigatorHDT);
        slopeY2D = findViewById(R.id.slopeY2D_tv);
        slopeX2D = findViewById(R.id.slopeX2D_tv);
        bucketEdge = findViewById(R.id.bucketEdge);
        divider = findViewById(R.id.divider);
        depthMode = findViewById(R.id.planeMode);
        zoomC = findViewById(R.id.zoomC);
        zoomM = findViewById(R.id.zoomM);
        zoomP = findViewById(R.id.zoomP);
        showStick = findViewById(R.id.showStick);
        zeroR = findViewById(R.id.setReach);
        allarmeAlt = findViewById(R.id.allarmeAlt);
        allarmeAlt.setVisibility(View.GONE);
        mainConstr.setBackgroundColor(MyColorClass.colorConstraint);
        findViewById(R.id.panel1D).setBackgroundColor(MyColorClass.colorSfondo);
        findViewById(R.id.panel2D).setBackgroundColor(MyColorClass.colorSfondo);
        textNameBucket.setTextColor(MyColorClass.colorConstraint);
        textNameBucket2.setTextColor(MyColorClass.colorConstraint);
        slopeX2D.setTextColor(MyColorClass.colorConstraint);
        slopeY2D.setTextColor(MyColorClass.colorConstraint);
        textSlopeX.setTextColor(MyColorClass.colorConstraint);
        textSlopeY.setTextColor(MyColorClass.colorConstraint);
        textOffset2.setTextColor(MyColorClass.colorConstraint);
        textOffset.setTextColor(MyColorClass.colorConstraint);
        textReach.setTextColor(MyColorClass.colorConstraint);
        overlayImage=findViewById(R.id.overlayImage);


    }

    private void init() {
        laserDialog = new LaserDialog(this);
        headingDialog = new HeadingDialog(this);
        easyConfigDialog = new EasyConfigDialog(this);
        dialogOffset = new DialogOffset(this);
        dialogSlope = new DialogSlope(this);
        params = (ConstraintLayout.LayoutParams) centro.getLayoutParams();
        int index= MyData.get_Int("Unit_Of_Measure");
        if(index==4||index==5) {
            leftLed.setTextSize(45f);
        }
        indexAudioSystem = MyData.get_Int("indexAudioSystem");
        heightPivot = MyData.get_String("Pivot_Height_Alarm").replace(",", ".");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexBucketSelected = MyData.get_Int("BucketSelected");
        if (DataSaved.lrTilt != 0) {
            layer1_tilt = new Draw2D_Layer1_Tilt(this);
            layer2_tilt = new Draw2D_Layer2_Tilt(this);
            panel1D.addView(layer1_tilt);
            panel2D.addView(layer2_tilt);
        } else {
            layer1 = new Draw2D_Layer1(this);
            layer2 = new Draw2D_Layer2(this);
            panel1D.addView(layer1);
            panel2D.addView(layer2);
        }
        left = new HeightLevelBar(this);
        indicatorH.addView(left);

        right = new HeightLevelBar(this);
        indicatorH2.addView(right);

        flatAngleBar = new FlatAngleBar(this);
        centerLed.addView(flatAngleBar);
        switch (DataSaved.bucketEdge) {
            case -1:
                bucketEdge.setImageResource(R.drawable.benna_misura_sinistra);
                break;
            case 0:
                bucketEdge.setImageResource(R.drawable.benna_misura_cnt);
                break;
            case 1:
                bucketEdge.setImageResource(R.drawable.benna_misura_destra);
                break;

        }
        if (DataSaved.isWL == 1) {
            centerLed.setRotationY(180f);
        }
        dialogTouchGo = new Dialog_TouchGo(this);


    }

    private void disableAll() {
        shortcut.setEnabled(false);
        left.setEnabled(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClick() {
        showStick.setOnClickListener(view -> {
            if (!easyConfigDialog.dialog.isShowing()) {
                easyConfigDialog.show();
            }
        });
        zoomC.setOnClickListener(view -> {
            if (vista == 1) {
                vista = 0;
            } else if (vista == 0) {
                vista = -1;
            } else if (vista == -1) {
                vista = 1;
            }
          /*  zommaOut = false;
            zommaIn = false;
            if (DataSaved.lrTilt != 0) {
                layer1_tilt.offsetX = 0;
                layer1_tilt.offsetY = 0;
                if (vista == 1) {
                    layer2_tilt.offsetY = 0;
                }
            } else {
                layer1.offsetX = 0;
                layer1.offsetY = 0;
                if (vista == 1) {
                    layer2.offsetY = 0;
                }
            }*/


        });


        zoomP.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                zommaIn = true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                zommaIn = false;
            }
            return true;
        });
        zoomM.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                zommaOut = true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                zommaOut = false;
            }
            return true;
        });
        bucketEdge.setOnLongClickListener(view -> {
            DataSaved.isLowerEdge=!DataSaved.isLowerEdge;
            return false;
        });

        bucketEdge.setOnClickListener(view -> {
            if(!DataSaved.isLowerEdge) {
                if (++DataSaved.bucketEdge > 1) {
                    DataSaved.bucketEdge = -1;

                }
                switch (DataSaved.bucketEdge) {
                    case 1:
                        bucketEdge.setImageResource(R.drawable.benna_misura_destra);
                        break;
                    case 0:
                        bucketEdge.setImageResource(R.drawable.benna_misura_cnt);
                        break;
                    case -1:
                        bucketEdge.setImageResource(R.drawable.benna_misura_sinistra);
                        break;
                }
            }

        });
        depthMode.setOnClickListener(view -> {
            if (DataSaved.projectionFlag == 0) {
                DataSaved.projectionFlag = 1;
                MyData.push("projectionFlag", String.valueOf(DataSaved.projectionFlag));

            } else {
                DataSaved.projectionFlag = 0;
                MyData.push("projectionFlag", String.valueOf(DataSaved.projectionFlag));

            }
        });

        slope.setOnClickListener((View v) -> {
            if (!dialogSlope.dialog.isShowing()) {
                dialogSlope.show();
            }
        });

        settingOffset.setOnClickListener((View v) -> {
            if (!dialogOffset.dialog.isShowing()) {
                dialogOffset.show();
            }
        });

        shortcut.setOnClickListener((View v) -> {
            disableAll();
            MyData.push("scaleFactor", String.valueOf(DataSaved.scale_Factor));
            startActivity(new Intent(this, ExcavatorMenuActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });


        laser.setOnClickListener((View v) -> {
            if (DataSaved.laserOn == 0) {
                DataSaved.monumentRelease = 0;
                if (!dialogTouchGo.alertDialog.isShowing()) {
                    dialogTouchGo.show();
                }
            } else {
                new CustomToast(this, getResources().getString(R.string.toast_hold_to_set)).show();
            }


        });

        setZero.setOnClickListener((View v) -> {
            new CustomToast(this, getResources().getString(R.string.toast_hold_to_set)).show();

        });

        leftLed.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, Digging_CutAndFill2D.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void onLongClick() {

        setZero.setOnLongClickListener((View v) -> {
            try {
                DataSaved.monumentRelease = 0;
                switch (DataSaved.bucketEdge) {
                    case -1:
                        DataSaved.offsetmDeltaX = new DistToPoint(ExcavatorLib.bucketLeftCoord[0], ExcavatorLib.bucketLeftCoord[1], 0, ExcavatorLib.coordPitch[0], ExcavatorLib.coordPitch[1], 0).getDist_to_point();
                        DataSaved.offsetmDeltaY = ExcavatorLib.bucketLeftCoord[2] - ExcavatorLib.coordPitch[2];
                        break;
                    case 0:
                        DataSaved.offsetmDeltaX = new DistToPoint(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0, ExcavatorLib.coordPitch[0], ExcavatorLib.coordPitch[1], 0).getDist_to_point();
                        DataSaved.offsetmDeltaY = ExcavatorLib.bucketCoord[2] - ExcavatorLib.coordPitch[2];
                        break;
                    case 1:
                        DataSaved.offsetmDeltaX = new DistToPoint(ExcavatorLib.bucketRightCoord[0], ExcavatorLib.bucketRightCoord[1], 0, ExcavatorLib.coordPitch[0], ExcavatorLib.coordPitch[1], 0).getDist_to_point();
                        DataSaved.offsetmDeltaY = ExcavatorLib.bucketRightCoord[2] - ExcavatorLib.coordPitch[2];
                        break;
                }
                DataSaved.offsetZH = ExcavatorLib.quota2D;
                DataSaved.start2DX = ExcavatorLib.bucketCoord[0];
                DataSaved.start2DY = ExcavatorLib.bucketCoord[1];
                DataSaved.start2DZ = ExcavatorLib.bucketCoord[2];
                ExcavatorLib.startRX = DataSaved.start2DX;
                ExcavatorLib.startRY = DataSaved.start2DY;
                ExcavatorLib.startRZ = DataSaved.start2DZ;
                MyData.push("offsetmDeltaX", String.valueOf(DataSaved.offsetmDeltaX));
                MyData.push("offsetmDeltaY", String.valueOf(DataSaved.offsetmDeltaY));
                MyData.push("start2DX", String.valueOf(DataSaved.start2DX));
                MyData.push("start2DY", String.valueOf(DataSaved.start2DY));
                MyData.push("start2DZ", String.valueOf(DataSaved.start2DZ));
                MyData.push("Offset_Zero", String.valueOf(DataSaved.offsetZH));


                if (flagLaser_D2D) {
                    if (!laserDialog.alertDialog.isShowing()) {
                        laserDialog.show();
                    }
                } else {
                    new CustomToast(this, "ZERO\nOK").show();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        });
        zeroR.setOnLongClickListener(view -> {
            ExcavatorLib.startRX = ExcavatorLib.bucketCoord[0];
            ExcavatorLib.startRY = ExcavatorLib.bucketCoord[1];
            ExcavatorLib.startRZ = ExcavatorLib.bucketCoord[2];
            return false;
        });


        rightLed.setOnClickListener((View v) -> {
            if (heading_tv.getText().toString().contains("ERROR")) {
                new CustomToast(this, getResources().getString(R.string.toast_cantset_heading)).show();

            } else {
                if (!headingDialog.dialog.isShowing()) {
                    headingDialog.show();
                }
            }

        });

        laser.setOnLongClickListener((View v) -> {
            if (DataSaved.laserOn == 1) {
                DataSaved.monumentRelease = 0;
                if (Excavator_RealValues.realLaser() == 0) {
                    flagLaser_D2D = true;
                    DataSaved.offsetLaserZH = ExcavatorLib.quotaLASER_2D;
                    MyData.push("Laser_Height_Zero", String.valueOf(DataSaved.offsetLaserZH));
                    new CustomToast(this, getResources().getString(R.string.laserOK)).show();

                } else {
                    new CustomToast(this, getResources().getString(R.string.laserKO)).show();

                }
            }
            return true;
        });

    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void updateUI() {
        if(DataSaved.isLowerEdge){
            bucketEdge.setBackgroundColor(getColor(R.color.yellow));
            switch (DataSaved.bucketEdge) {
                case 1:
                    bucketEdge.setImageResource(R.drawable.benna_misura_destra);
                    break;
                case 0:
                    bucketEdge.setImageResource(R.drawable.benna_misura_cnt);
                    break;
                case -1:
                    bucketEdge.setImageResource(R.drawable.benna_misura_sinistra);
                    break;
            }
        }else {
            bucketEdge.setBackgroundColor(getColor(R.color.nav_gray_color));
        }

        if (zommaOut) {
            zommaIn = false;

            DataSaved.scale_Factor -= 10;
        }
        if (zommaIn) {
            zommaOut = false;
            DataSaved.scale_Factor += 10;

        }


        if (DataSaved.scale_Factor >= 800) {
            DataSaved.scale_Factor = 800;
        }
        if (DataSaved.scale_Factor <= 50) {
            DataSaved.scale_Factor = 50;
        }
        if (DataSaved.monumentRelease != 0) {
            bucketEdge.setEnabled(false);
            bucketEdge.setAlpha(0.4f);
        } else {
            bucketEdge.setEnabled(true);
            bucketEdge.setAlpha(1.0f);
        }
        alerts();

        switch (DataSaved.shortcutIndex) {
            case 1:
                showStick.setImageResource(R.drawable.numero_1);
                break;
            case 2:
                showStick.setImageResource(R.drawable.numero_2);
                break;
            case 3:
                showStick.setImageResource(R.drawable.numero_3);
                break;
            case 4:
                showStick.setImageResource(R.drawable.numero_4);
                break;
            case 5:
                showStick.setImageResource(R.drawable.numero_5);
                break;
            case 6:
                showStick.setImageResource(R.drawable.numero_6);
                break;
        }
        switch (vista) {
            case 0:
                params.guidePercent = 0.5f;
                divider.setVisibility(View.VISIBLE);

                textOffset2.setVisibility(View.INVISIBLE);
                textNameBucket2.setVisibility(View.INVISIBLE);
                textOffset.setVisibility(View.VISIBLE);
                textNameBucket.setVisibility(View.VISIBLE);
                break;
            case 1:
                params.guidePercent = 0.05f;
                divider.setVisibility(View.INVISIBLE);

                textOffset2.setVisibility(View.VISIBLE);
                textNameBucket2.setVisibility(View.VISIBLE);
                textOffset.setVisibility(View.INVISIBLE);
                textNameBucket.setVisibility(View.INVISIBLE);
                break;
            case -1:
                params.guidePercent = 0.95f;
                divider.setVisibility(View.INVISIBLE);

                textOffset.setVisibility(View.VISIBLE);
                textNameBucket.setVisibility(View.VISIBLE);
                textOffset2.setVisibility(View.INVISIBLE);
                textNameBucket2.setVisibility(View.INVISIBLE);
                break;
        }

        centro.setLayoutParams(params);
        if (DataSaved.projectionFlag == 0) {
            depthMode.setImageResource((R.drawable.rif_inclinato));

        } else {
            depthMode.setImageResource((R.drawable.rif_verticale));

        }
        double hdt=0;
        if(DataSaved.useYawFrame==0) {
            hdt = (NmeaListener.mch_Hdt - DataSaved.offsetHDT);
        }else {
            hdt = (Sensors_Decoder.Deg_Yaw_Frame - DataSaved.offsetHDT);
        }


        if (hdt > 180.0) {
            hdt = hdt - 360;
        }
        if (hdt < -180.0) {
            hdt = hdt + 360;
        }
        if(DataSaved.useYawFrame==0){
        if (NmeaListener.mch_Hdt == 999.999) {
            heading_tv.setText("ERROR");
            navigator.setRotation((float) hdt * 0);
        } else {
            try {
                heading_tv.setText(String.format("%.1f", hdt).replace(",", ".")+"°");
            } catch (Exception e) {
                heading_tv.setText(String.format("%.1f", hdt));
            }
            navigator.setRotation((float) hdt * -1);
        }
        }else {
            if (CanService.frameDisc) {
                heading_tv.setText("ERROR");
                navigator.setRotation((float) hdt * 0);
            } else {
                try {
                    heading_tv.setText(String.format("%.1f", hdt).replace(",", ".")+"°");
                } catch (Exception e) {
                    heading_tv.setText(String.format("%.1f", hdt));
                }
                navigator.setRotation((float) hdt * -1);
            }
        }


        real_height = !flagLaser_D2D ? (ExcavatorLib.quota2D - DataSaved.offsetZH + DataSaved.offsetH) : ((DataSaved.offsetLaserZH - ExcavatorLib.quota2D) + DataSaved.offsetH * -1) * -1;
        real_height = real_height - DataSaved.monumentRelease;
        //double real_height_sx = !flagLaser_D2D ? ExcavatorLib.quotaSx - DataSaved.offsetZH + DataSaved.offsetH : ((DataSaved.offsetLaserZH - ExcavatorLib.quotaSx) + DataSaved.offsetH * -1) * -1;
        //double real_height_cx = !flagLaser_D2D ? ExcavatorLib.quotaCentro - DataSaved.offsetZH + DataSaved.offsetH : ((DataSaved.offsetLaserZH - ExcavatorLib.quotaCentro) + DataSaved.offsetH * -1) * -1;
        //double real_height_dx = !flagLaser_D2D ? ExcavatorLib.quotaDx - DataSaved.offsetZH + DataSaved.offsetH : ((DataSaved.offsetLaserZH - ExcavatorLib.quotaDx) + DataSaved.offsetH * -1) * -1;

        ExcavatorLib.msideC = real_height;
        ExcavatorLib.msideCX = real_height;
        if (DataSaved.projectionFlag == 1 && Math.abs(ExcavatorLib.actualY2D) >= 10) {


            if (ExcavatorLib.distToSurf >= -DataSaved.deadbandH && ExcavatorLib.distToSurf <= DataSaved.deadbandH) {
                left.indexLeverBar = 2;
                right.indexLeverBar = 2;
            } else if (ExcavatorLib.distToSurf > -DataSaved.deadbandH && ExcavatorLib.distToSurf <= DataSaved.deadbandH + 0.05) {
                left.indexLeverBar = 1;
                right.indexLeverBar = 1;
            } else if (ExcavatorLib.distToSurf < -DataSaved.deadbandH && ExcavatorLib.distToSurf >= -DataSaved.deadbandH - 0.05) {
                left.indexLeverBar = 3;
                right.indexLeverBar = 3;
            } else if (ExcavatorLib.distToSurf > DataSaved.deadbandH + 0.05) {
                left.indexLeverBar = 0;
                right.indexLeverBar = 0;
            } else if (ExcavatorLib.distToSurf < -DataSaved.deadbandH - 0.05) {
                left.indexLeverBar = 4;
                right.indexLeverBar = 4;
            }

            if (ExcavatorLib.distToSurf >= -DataSaved.deadbandH && ExcavatorLib.distToSurf <= DataSaved.deadbandH) {
                leftLed.setTextColor(Color.GREEN);
                leftLed.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,ExcavatorLib.distToSurf,DataSaved.deadbandH));
                CanSender.onGrade= (byte) 128;
            }
            if (ExcavatorLib.distToSurf > DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);

                leftLed.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,ExcavatorLib.distToSurf,DataSaved.deadbandH));
                CanSender.onGrade= (byte) 125;
            }
            if (ExcavatorLib.distToSurf < -DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,ExcavatorLib.distToSurf,DataSaved.deadbandH));
                CanSender.onGrade= (byte) 131;
            }
        } else {


            if (real_height >= -DataSaved.deadbandH && real_height <= DataSaved.deadbandH) {
                left.indexLeverBar = 2;
                right.indexLeverBar = 2;
            } else if (real_height > -DataSaved.deadbandH && real_height <= DataSaved.deadbandH + 0.05) {
                left.indexLeverBar = 1;
                right.indexLeverBar = 1;
            } else if (real_height < -DataSaved.deadbandH && real_height >= -DataSaved.deadbandH - 0.05) {
                left.indexLeverBar = 3;
                right.indexLeverBar = 3;
            } else if (real_height > DataSaved.deadbandH + 0.05) {
                left.indexLeverBar = 0;
                right.indexLeverBar = 0;
            } else if (real_height < -DataSaved.deadbandH - 0.05) {
                left.indexLeverBar = 4;
                right.indexLeverBar = 4;
            }


            if (real_height >= -DataSaved.deadbandH && real_height <= DataSaved.deadbandH) {
                leftLed.setTextColor(Color.GREEN);
                leftLed.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,real_height,DataSaved.deadbandH));
            }
            if (real_height > DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,real_height,DataSaved.deadbandH));
            }
            if (real_height < -DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,real_height,DataSaved.deadbandH));

            }
        }


        textSlopeX.setText("X: " + Utils.readAngolo(String.valueOf(DataSaved.slopeX)) + Utils.getGradiSimbol());
        textSlopeY.setText("Y: " + Utils.readAngolo(String.valueOf(DataSaved.slopeY)) + Utils.getGradiSimbol());
        textOffset.setText("OFFSET: " + Utils.readUnitOfMeasureLITE(String.valueOf((DataSaved.offsetH * -1))));
        textNameBucket.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucketSelected + "_Name"));
        textOffset2.setText("OFFSET: " + Utils.readUnitOfMeasureLITE(String.valueOf((DataSaved.offsetH * -1))));
        textNameBucket2.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucketSelected + "_Name"));
        textReach.setText("r: " + Utils.readUnitOfMeasureLITE(String.valueOf(Math.abs(ExcavatorLib.distanza_inclinata - 0))));


        slopeY2D.setText(Utils.readAngolo(String.valueOf(ExcavatorLib.actualY2D)) + Utils.getGradiSimbol());
        slopeX2D.setText(Utils.readAngolo(String.valueOf(ExcavatorLib.actualX2D)) + Utils.getGradiSimbol());


        if ((ExcavatorLib.correctFlat >= (DataSaved.slopeY - DataSaved.deadbandFlatAngle)) && (ExcavatorLib.correctFlat <= (DataSaved.slopeY + DataSaved.deadbandFlatAngle))) {
            flatAngleBar.indexFlatBar = 1;
        } else if (ExcavatorLib.correctFlat > (DataSaved.slopeY + DataSaved.deadbandFlatAngle)) {
            if (DataSaved.isWL == 0) {
                flatAngleBar.indexFlatBar = 0;
            } else {
                flatAngleBar.indexFlatBar = 2;
            }
        } else if (ExcavatorLib.correctFlat < (DataSaved.slopeY - DataSaved.deadbandFlatAngle)) {
            if (DataSaved.isWL == 0) {
                flatAngleBar.indexFlatBar = 2;
            } else {
                flatAngleBar.indexFlatBar = 0;
            }
        }

        if (DataSaved.laserOn == 1) {
            try {
                if (Excavator_RealValues.realLaser() <= -10 && Sensors_Decoder.flagLaser > -101) {
                    laser.setImageResource(R.drawable.down_btn);
                    laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color._____cancel_text));
                } else if (Excavator_RealValues.realLaser() == 0 && Sensors_Decoder.flagLaser > -101) {
                    laser.setImageResource(R.drawable.equals_btn);
                    laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                } else if (Excavator_RealValues.realLaser() >= 10 && Sensors_Decoder.flagLaser > -101 && Excavator_RealValues.realLaser() != 255) {
                    laser.setImageResource(R.drawable.up_btn);
                    laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color._____cancel_text));
                } else {
                    laser.setImageResource(R.drawable.laser_btn);
                    laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.nav_gray_color));
                }

            } catch (Exception ignored) {
                laser.setImageResource(R.drawable.laser_btn);
                laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.nav_gray_color));
            }
        } else {
            laser.setImageResource(R.drawable.touch_go_btn);
        }
        if (DataSaved.lrTilt != 0) {
            layer1_tilt.invalidate();
            layer2_tilt.invalidate();
        } else {
            layer1.invalidate();
            layer2.invalidate();
        }

        flatAngleBar.invalidate();
        left.invalidate();
        right.invalidate();

        if (indexAudioSystem != 0) {
            if (real_height >= -DataSaved.deadbandH && real_height <= DataSaved.deadbandH && audioFlags[0]) {
                try {
                    mediaPlayer.reset();
                } catch (Exception ignored) {
                }
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.audio_verde);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                audioFlags = new boolean[]{false, true, true, true};
            }
            if (real_height > DataSaved.deadbandH && audioFlags[1]) {
                try {
                    mediaPlayer.reset();
                } catch (Exception ignored) {
                }
                if (indexAudioSystem == 1) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.audio_rosso);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
                audioFlags = new boolean[]{true, false, true, true};
            }
            if (real_height < -DataSaved.deadbandH && audioFlags[2]) {
                try {
                    mediaPlayer.reset();
                } catch (Exception ignored) {
                }
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.audio_blu);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                audioFlags = new boolean[]{true, true, false, true};

            }

        }
        if (DataSaved.lrTilt != 0) {
            if (vista == 0) {
                layer2_tilt.offsetY = layer1_tilt.offsetY;
            }
        } else {
            if (vista == 0) {
                layer2.offsetY = layer1.offsetY;
            }
        }
        try {
            if (DataSaved.projectionFlag == 0) {
                mAltezza2D = real_height;
            } else {
                mAltezza2D = ExcavatorLib.distToSurf;
            }
        } catch (Exception e) {
            mAltezza2D = 0;
        }


    }

    private void alerts() {
        if (!hAlarm) {
            allarmeAlt.setVisibility(View.GONE);
            if (!mSentOFF) {

                mSentOFF = true;
                mSentON = false;
            }


        } else {
            allarmeAlt.setVisibility(View.VISIBLE);

            if (!mSentON) {

                mSentON = true;
                mSentOFF = false;
            }

        }

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    protected void onDestroy() {
        super.onDestroy();


        if (indexAudioSystem != 0 && mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        flagLaser_D2D = false;


    }
}