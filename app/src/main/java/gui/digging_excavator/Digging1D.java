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
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.ExcavatorMenuActivity;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogOffset;
import gui.dialogs_and_toast.DialogSlope;
import gui.dialogs_and_toast.Dialog_TouchGo;
import gui.dialogs_and_toast.EasyConfigDialog;
import gui.dialogs_and_toast.LaserDialog;
import gui.draw_class.Draw1D;
import gui.draw_class.FlatAngleBar;
import gui.draw_class.HeightLevelBar;
import gui.draw_class.MyColorClass;

import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Excavator_RealValues;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.surfcreator.DistToPoint;
import services.CanSender;
import utils.LeicaLB;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;


public class Digging1D extends BaseClass {
    private boolean zommaIn, zommaOut;
    ConstraintLayout mainConstr;
    private double real_height;
    public static double mAltezza;
    private boolean mSentOFF = false;
    private boolean mSentON = false;

    ConstraintLayout panel1D, indicatorH, indicatorH2;
    ImageView setZero, laser, slope, settingOffset, shortcut, toBubble, showStick, depthMode, zoomC, zoomM, zoomP, zeroR;
    TextView leftLed, rightLed;
    FrameLayout centerLed;
    TextView textOffset, textSlopeY, textReach, textNameBucket;
    Draw1D draw1D;

    HeightLevelBar left, right;
    FlatAngleBar flatAngleBar;
    int indexMachineSelected, indexBucketSelected, indexAudioSystem;

    boolean[] audioFlags = new boolean[]{true, true, true, true};
    String heightPivot;
    MediaPlayer mediaPlayer;

    Dialog_TouchGo dialogTouchGo;


    public static boolean flagLaser_D1D = false;
    public int counterZero = 0;
    ImageView allarmeAlt;
    DialogOffset dialogOffset;
    DialogSlope dialogSlope;

    EasyConfigDialog easyConfigDialog;
    LaserDialog laserDialog;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dig_1d);

        mainConstr = this.getWindow().findViewById(R.id.mainConstr);
        findView();
        init();
        onClick();
        onLongClick();
        updateUI();
        DataSaved.isLowerEdge=false;


    }

    private void findView() {
        panel1D = findViewById(R.id.panel1D);
        indicatorH = findViewById(R.id.IndicatorH);
        indicatorH2 = findViewById(R.id.IndicatorH2);
        leftLed = findViewById(R.id.leftCorner);
        centerLed = findViewById(R.id.deltaCenter);
        rightLed = findViewById(R.id.rightCorner);
        shortcut = findViewById(R.id.shortcut);
        setZero = findViewById(R.id.setZero);
        shortcut = findViewById(R.id.shortcut);
        laser = findViewById(R.id.laser);
        settingOffset = findViewById(R.id.settingOffset);
        slope = findViewById(R.id.slope);
        textOffset = findViewById(R.id.offset_tv);
        textSlopeY = findViewById(R.id.slopeY_tv);
        textReach = findViewById(R.id.reach_tv);
        textNameBucket = findViewById(R.id.bucket_tv);
        toBubble = findViewById(R.id.toBubble);
        depthMode = findViewById(R.id.planeMode);
        zoomC = findViewById(R.id.zoomC);
        zoomM = findViewById(R.id.zoomM);
        zoomP = findViewById(R.id.zoomP);
        showStick = findViewById(R.id.showStick);
        zeroR = findViewById(R.id.setReach);
        mainConstr.setBackgroundColor(MyColorClass.colorConstraint);
        allarmeAlt = findViewById(R.id.allarmeAlt);
        allarmeAlt.setVisibility(View.GONE);
        textNameBucket.setTextColor(MyColorClass.colorConstraint);
        textSlopeY.setTextColor(MyColorClass.colorConstraint);
        textReach.setTextColor(MyColorClass.colorConstraint);
        textOffset.setTextColor(MyColorClass.colorConstraint);
        dialogTouchGo = new Dialog_TouchGo(this);
        zoomC.setVisibility(View.INVISIBLE);
    }

    private void init() {
        laserDialog = new LaserDialog(this);
        easyConfigDialog = new EasyConfigDialog(this);
        dialogOffset = new DialogOffset(this);
        dialogSlope = new DialogSlope(this);
        int index= MyData.get_Int("Unit_Of_Measure");
        if(index==4||index==5) {
            leftLed.setTextSize(45f);
        }
        indexAudioSystem = MyData.get_Int("indexAudioSystem");
        heightPivot = MyData.get_String("Pivot_Height_Alarm").replace(",", ".");

        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexBucketSelected = MyData.get_Int("BucketSelected");

        draw1D = new Draw1D(this);

        panel1D.addView(draw1D);

        left = new HeightLevelBar(this);
        indicatorH.addView(left);

        right = new HeightLevelBar(this);
        indicatorH2.addView(right);

        flatAngleBar = new FlatAngleBar(this);
        centerLed.addView(flatAngleBar);
        panel1D.setBackgroundColor(MyColorClass.colorSfondo);
        if (DataSaved.isWL == 1) {
            centerLed.setRotationY(180f);
        }

    }

    private void disableAll() {
        shortcut.setEnabled(false);
        toBubble.setEnabled(false);
        leftLed.setEnabled(false);



    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClick() {
        showStick.setOnClickListener(view -> {
            if (!easyConfigDialog.dialog.isShowing()) {
                easyConfigDialog.show();
            }
        });
        zoomC.setOnClickListener(view -> {
         /*   zommaOut = false;
            zommaIn = false;
            draw1D.offsetX = 0;
            draw1D.offsetY = 0;*/
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
        toBubble.setOnClickListener((View v) -> {
          /*  disableAll();
            Intent intent = new Intent(this, E_Bubble.class);
            intent.putExtra("who", "A1D");
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();*/

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
            startActivity(new Intent(this, Digging_CutAndFill1D.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private void onLongClick() {
        setZero.setOnLongClickListener((View v) -> {
            try {
                DataSaved.monumentRelease = 0;
                DataSaved.offsetmDeltaX = new DistToPoint(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0, ExcavatorLib.coordPitch[0], ExcavatorLib.coordPitch[1], 0).getDist_to_point();
                DataSaved.offsetmDeltaY = ExcavatorLib.bucketCoord[2] - ExcavatorLib.coordPitch[2];
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


                if (flagLaser_D1D) {
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

        laser.setOnLongClickListener((View v) -> {

            if (DataSaved.laserOn == 1) {
                DataSaved.monumentRelease = 0;
                if (Excavator_RealValues.realLaser() == 0) {
                    flagLaser_D1D = true;
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


    @SuppressLint("SetTextI18n")
    public void updateUI() {


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

        if (DataSaved.projectionFlag == 0) {
            depthMode.setImageResource((R.drawable.rif_inclinato));
        } else {
            depthMode.setImageResource((R.drawable.rif_verticale));
        }

        real_height = !flagLaser_D1D ? ExcavatorLib.quota2D - DataSaved.offsetZH + DataSaved.offsetH : ((DataSaved.offsetLaserZH - ExcavatorLib.quota2D) + DataSaved.offsetH * -1) * -1;
        real_height = real_height - DataSaved.monumentRelease;
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
            }
            if (ExcavatorLib.distToSurf > DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,ExcavatorLib.distToSurf,DataSaved.deadbandH));
            }
            if (ExcavatorLib.distToSurf < -DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,ExcavatorLib.distToSurf,DataSaved.deadbandH));
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
                CanSender.onGrade= (byte) 128;
            }
            if (real_height > DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,real_height,DataSaved.deadbandH));
                CanSender.onGrade= (byte) 125;
            }
            if (real_height < -DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                MyDeviceManager.CanWrite(0,0xA0,3, LeicaLB.mapping(false,real_height,DataSaved.deadbandH));
                CanSender.onGrade= (byte) 131;
            }
        }

        textSlopeY.setText("Y: " + Utils.readAngolo(String.valueOf(DataSaved.slopeY)) + Utils.getGradiSimbol());
        textOffset.setText("OFFSET: " + Utils.readUnitOfMeasureLITE(String.valueOf((DataSaved.offsetH * -1))));
        textNameBucket.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucketSelected + "_Name"));

        textReach.setText("r: " + Utils.readUnitOfMeasureLITE(String.valueOf(Math.abs(ExcavatorLib.distanza_inclinata - 0))));


        //levelbar down here


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

        draw1D.invalidate();
        left.invalidate();
        right.invalidate();
        flatAngleBar.invalidate();


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
        try {
            if (DataSaved.projectionFlag == 0) {
                mAltezza = real_height;
            } else {
                mAltezza = ExcavatorLib.distToSurf;
            }
        } catch (Exception e) {
            mAltezza = 0;
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
                if (DataSaved.enOUT == 1) {
                    //VanCmd.exec("echo \"100009\" >/dev/gpio_dev", 0);
                }
                mSentON = true;
                mSentOFF = false;
            }
        }

    }




    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (indexAudioSystem != 0 && mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        flagLaser_D1D = false;


    }


}