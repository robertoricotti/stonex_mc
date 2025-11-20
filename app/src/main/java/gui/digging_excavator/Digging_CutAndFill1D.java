package gui.digging_excavator;


import static gui.MyApp.hAlarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogOffset;
import gui.dialogs_and_toast.DialogSlope;
import gui.dialogs_and_toast.Dialog_TouchGo;
import gui.dialogs_and_toast.EasyConfigDialog;
import gui.dialogs_and_toast.LaserDialog;
import gui.draw_class.FlatAngleBar;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Excavator_RealValues;
import services.CanService;
import utils.LeicaLB;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class Digging_CutAndFill1D extends BaseClass {
    private double real_height;
    public static double mAltezza1D_cf;
    private boolean mSentOFF = false;
    private boolean mSentON = false;
    ConstraintLayout panel;
    ImageView setZero, laser, slope, settingOffset, back, depthMode, toBubble, showStick, zeroR;
    TextView leftLed, rightLed;
    FrameLayout centerLed;
    TextView textOffset, textSlopeY, textReach, textNameBucket, textHeight;

    FlatAngleBar flatAngleBar;
    int indexMachineSelected, indexBucketSelected, indexAudioSystem;
    String heightPivot;

    MediaPlayer mediaPlayer;
    boolean[] audioFlags = new boolean[]{true, true, true, true};

    public static boolean flagLaser_C1D = false;
    public static int counterZero = 0;
    ImageView allarmeAlt;
    Dialog_TouchGo dialogTouchGo;
    DialogOffset dialogOffset;
    DialogSlope dialogSlope;
    EasyConfigDialog easyConfigDialog;
    LaserDialog laserDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dig_cut_and_fill_1d);


        findView();
        init();
        onClick();
        onLongClick();
        updateUI();
        DataSaved.isLowerEdge = false;

    }

    private void findView() {
        panel = findViewById(R.id.panel);
        leftLed = findViewById(R.id.leftCorner);
        centerLed = findViewById(R.id.deltaCenter);
        rightLed = findViewById(R.id.rightCorner);
        back = findViewById(R.id.back);
        setZero = findViewById(R.id.setZero);
        laser = findViewById(R.id.laser);
        settingOffset = findViewById(R.id.settingOffset);
        slope = findViewById(R.id.slope);
        textOffset = findViewById(R.id.offset_tv);
        textSlopeY = findViewById(R.id.slopeY_tv);
        textReach = findViewById(R.id.reach_tv);
        textNameBucket = findViewById(R.id.bucket_tv);
        textHeight = findViewById(R.id.height_tv);
        depthMode = findViewById(R.id.depthMode);
        toBubble = findViewById(R.id.toBubble);
        showStick = findViewById(R.id.showStick);
        zeroR = findViewById(R.id.setReach);
        allarmeAlt = findViewById(R.id.allarmeAlt);
        allarmeAlt.setVisibility(View.GONE);

    }

    private void init() {
        easyConfigDialog = new EasyConfigDialog(this);
        dialogOffset = new DialogOffset(this);
        dialogSlope = new DialogSlope(this);
        indexAudioSystem = MyData.get_Int("indexAudioSystem");
        heightPivot = MyData.get_String("Pivot_Height_Alarm").replace(",", ".");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexBucketSelected = MyData.get_Int("M"+indexMachineSelected+"BucketSelected");
        dialogTouchGo = new Dialog_TouchGo(this);
        flatAngleBar = new FlatAngleBar(this);
        centerLed.addView(flatAngleBar);
        laserDialog = new LaserDialog(this);
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 4 || index == 5) {
            leftLed.setTextSize(45f);

        }
    }

    private void disableAll() {
        toBubble.setEnabled(false);
        back.setEnabled(false);
    }

    private void onClick() {
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

        back.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(getApplicationContext(), Digging1D.class));
            finish();
        });



        setZero.setOnClickListener((View v) -> {
            new CustomToast(this, getResources().getString(R.string.toast_hold_to_set)).show();

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
        showStick.setOnClickListener(view -> {
            if (!easyConfigDialog.dialog.isShowing()) {
                easyConfigDialog.show();
            }
        });
    }

    private void onLongClick() {
        setZero.setOnLongClickListener((View v) -> {
            DataSaved.monumentRelease = 0;
            DataSaved.offsetZH = ExcavatorLib.quota2D;
            DataSaved.start2DX = ExcavatorLib.bucketCoord[0];
            DataSaved.start2DY = ExcavatorLib.bucketCoord[1];
            DataSaved.start2DZ = ExcavatorLib.bucketCoord[2];
            ExcavatorLib.startRX = DataSaved.start2DX;
            ExcavatorLib.startRY = DataSaved.start2DY;
            ExcavatorLib.startRZ = DataSaved.start2DZ;
            MyData.push("start2DX", String.valueOf(DataSaved.start2DX));
            MyData.push("start2DY", String.valueOf(DataSaved.start2DY));
            MyData.push("start2DZ", String.valueOf(DataSaved.start2DZ));
            MyData.push("Offset_Zero", String.valueOf(DataSaved.offsetZH));


            if (flagLaser_C1D) {
                if (!laserDialog.dialog.isShowing()) {
                    laserDialog.show();
                }

            } else {
                new CustomToast(this, "ZERO\nOK").show();
            }
            return true;
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
                    flagLaser_C1D = true;
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
        real_height = !flagLaser_C1D ? ExcavatorLib.quota2D - DataSaved.offsetZH + DataSaved.offsetH : ((DataSaved.offsetLaserZH - ExcavatorLib.quota2D) + DataSaved.offsetH * -1) * -1;
        real_height = real_height - DataSaved.monumentRelease;
        ExcavatorLib.msideC = real_height;
        ExcavatorLib.msideCX = real_height;
        if (DataSaved.projectionFlag == 1 && Math.abs(ExcavatorLib.actualY2D) >= 10) {
            if (ExcavatorLib.distToSurf >= -DataSaved.deadbandH && ExcavatorLib.distToSurf <= DataSaved.deadbandH) {
                leftLed.setTextColor(Color.GREEN);
                leftLed.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                textHeight.setText(leftLed.getText().toString());
                MyDeviceManager.CanWrite(true,0, 0xA0, 3, LeicaLB.mapping(false, ExcavatorLib.distToSurf, DataSaved.deadbandH));
                if (!hAlarm) {
                    panel.setBackgroundColor(Color.GREEN);
                    depthMode.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                    zeroR.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                    showStick.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                    textHeight.setTextColor(Color.BLACK);
                    textSlopeY.setTextColor(Color.BLACK);
                    textOffset.setTextColor(Color.BLACK);
                    textNameBucket.setTextColor(Color.BLACK);
                    textReach.setTextColor(Color.BLACK);
                }
            }
            if (ExcavatorLib.distToSurf > DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                textHeight.setText(leftLed.getText().toString());
                MyDeviceManager.CanWrite(true,0, 0xA0, 3, LeicaLB.mapping(false, ExcavatorLib.distToSurf, DataSaved.deadbandH));
                if (!hAlarm) {
                    panel.setBackgroundColor(Color.RED);
                    depthMode.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    zeroR.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    showStick.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    textHeight.setTextColor(Color.WHITE);
                    textSlopeY.setTextColor(Color.WHITE);
                    textOffset.setTextColor(Color.WHITE);
                    textNameBucket.setTextColor(Color.WHITE);
                    textReach.setTextColor(Color.WHITE);
                }
            }
            if (ExcavatorLib.distToSurf < -DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                textHeight.setText(leftLed.getText().toString());
                MyDeviceManager.CanWrite(true,0, 0xA0, 3, LeicaLB.mapping(false, ExcavatorLib.distToSurf, DataSaved.deadbandH));
                if (!hAlarm) {
                    panel.setBackgroundColor(Color.BLUE);
                    depthMode.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    zeroR.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    showStick.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    textHeight.setTextColor(Color.WHITE);
                    textSlopeY.setTextColor(Color.WHITE);
                    textOffset.setTextColor(Color.WHITE);
                    textNameBucket.setTextColor(Color.WHITE);
                    textReach.setTextColor(Color.WHITE);
                }
            }
        } else {
            if (real_height >= -DataSaved.deadbandH && real_height <= DataSaved.deadbandH) {
                leftLed.setTextColor(Color.GREEN);
                leftLed.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                textHeight.setText(leftLed.getText().toString());
                MyDeviceManager.CanWrite(true,0, 0xA0, 3, LeicaLB.mapping(false, real_height, DataSaved.deadbandH));
                if (!hAlarm) {
                    panel.setBackgroundColor(Color.GREEN);
                    depthMode.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                    zeroR.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                    showStick.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                    textHeight.setTextColor(Color.BLACK);
                    textSlopeY.setTextColor(Color.BLACK);
                    textOffset.setTextColor(Color.BLACK);
                    textNameBucket.setTextColor(Color.BLACK);
                    textReach.setTextColor(Color.BLACK);
                }
            }
            if (real_height > DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                textHeight.setText(leftLed.getText().toString());
                MyDeviceManager.CanWrite(true,0, 0xA0, 3, LeicaLB.mapping(false, real_height, DataSaved.deadbandH));
                if (!hAlarm) {
                    if (DataSaved.colorMode == 0) {
                        panel.setBackgroundColor(Color.RED);
                    } else {
                        panel.setBackgroundColor(Color.BLUE);
                    }
                    depthMode.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    zeroR.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    showStick.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    textHeight.setTextColor(Color.WHITE);
                    textSlopeY.setTextColor(Color.WHITE);
                    textOffset.setTextColor(Color.WHITE);
                    textNameBucket.setTextColor(Color.WHITE);
                    textReach.setTextColor(Color.WHITE);
                }
            }
            if (real_height < -DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                textHeight.setText(leftLed.getText().toString());
                MyDeviceManager.CanWrite(true,0, 0xA0, 3, LeicaLB.mapping(false, real_height, DataSaved.deadbandH));
                if (!hAlarm) {
                    if (DataSaved.colorMode == 0) {
                        panel.setBackgroundColor(Color.BLUE);
                    } else {
                        panel.setBackgroundColor(Color.RED);
                    }
                    depthMode.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    zeroR.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    showStick.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    textHeight.setTextColor(Color.WHITE);
                    textSlopeY.setTextColor(Color.WHITE);
                    textOffset.setTextColor(Color.WHITE);
                    textNameBucket.setTextColor(Color.WHITE);
                    textReach.setTextColor(Color.WHITE);
                }
            }
        }

        textSlopeY.setText("Y: " + Utils.readAngolo(String.valueOf(DataSaved.slopeY)) + Utils.getGradiSimbol());
        textOffset.setText("OFFSET: " + Utils.readUnitOfMeasureLITE(String.valueOf((DataSaved.offsetH * -1))));
        textNameBucket.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucketSelected + "_Name"));

        textReach.setText("r: " + Utils.readUnitOfMeasureLITE(String.valueOf(Math.abs(ExcavatorLib.distanza_inclinata - 0))));


        if ((ExcavatorLib.correctFlat >= (DataSaved.slopeY - DataSaved.deadbandFlatAngle)) && (ExcavatorLib.correctFlat <= (DataSaved.slopeY + DataSaved.deadbandFlatAngle))) {
            flatAngleBar.indexFlatBar = 1;
        } else if (ExcavatorLib.correctFlat > (DataSaved.slopeY + DataSaved.deadbandFlatAngle)) {
            flatAngleBar.indexFlatBar = 0;
        } else if (ExcavatorLib.correctFlat < (DataSaved.slopeY - DataSaved.deadbandFlatAngle)) {
            flatAngleBar.indexFlatBar = 2;
        }
        if (DataSaved.laserOn == 1) {
            try {
                if (Excavator_RealValues.realLaser() <= -10 && CanService.flagLaser) {
                    laser.setImageResource(R.drawable.down_btn);
                    laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color._____cancel_text));
                } else if (Excavator_RealValues.realLaser() == 0 && CanService.flagLaser) {
                    laser.setImageResource(R.drawable.equals_btn);
                    laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                } else if (Excavator_RealValues.realLaser() >= 10 && CanService.flagLaser) {
                    laser.setImageResource(R.drawable.up_btn);
                    laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color._____cancel_text));
                } else if (!(CanService.flagLaser)) {
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
        flatAngleBar.invalidate();

        if (indexAudioSystem != 0) {
            if (real_height >= -DataSaved.deadbandH && real_height <= DataSaved.deadbandH && audioFlags[0]) {
                //verde
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
                mAltezza1D_cf = real_height;
            } else {
                mAltezza1D_cf = ExcavatorLib.distToSurf;
            }
        } catch (Exception e) {
            mAltezza1D_cf = 0;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (indexAudioSystem != 0 && mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        flagLaser_C1D = false;


    }

}
