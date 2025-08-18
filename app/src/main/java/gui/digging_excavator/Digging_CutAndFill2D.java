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
import android.widget.LinearLayout;
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
import gui.dialogs_and_toast.HeadingDialog;
import gui.dialogs_and_toast.LaserDialog;
import gui.draw_class.FlatAngleBar;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Excavator_RealValues;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.gnss.NmeaListener;
import services.CanService;
import utils.LeicaLB;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class Digging_CutAndFill2D extends BaseClass {
    private double real_height;
    public static double mAltezza2D_cf;
    private boolean mSentOFF = false;
    private boolean mSentON = false;
    ConstraintLayout panel;
    ImageView setZero, laser, slope, settingOffset, todig, navigator, bucketEdge, depthMode, showStick, zeroR;
    TextView leftLed, slopeY2D, slopeX2D, heading_tv;
    LinearLayout rightLed;
    FrameLayout centerLed;
    TextView textOffset, textSlopeX, textSlopeY, textReach, textNameBucket, textHeight;
    FlatAngleBar flatAngleBar;
    int indexMachineSelected, indexBucketSelected, indexAudioSystem;
    String heightPivot;
    MediaPlayer mediaPlayer;
    boolean[] audioFlags = new boolean[]{true, true, true, true};

    public static boolean flagLaser_C2D = false;
    public int counterZero = 0;
    ImageView allarmeAlt;
    Dialog_TouchGo dialogTouchGo;
    DialogOffset dialogOffset;
    DialogSlope dialogSlope;
    HeadingDialog headingDialog;
    LaserDialog laserDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dig_cut_and_fill_2d);


        findView();
        init();
        onClick();
        onLongClick();
        updateUI();

    }

    private void findView() {
        panel = findViewById(R.id.panel);
        leftLed = findViewById(R.id.leftCorner);
        centerLed = findViewById(R.id.deltaCenter);
        rightLed = findViewById(R.id.rightCorner);
        navigator = findViewById(R.id.navigatorHDT);
        todig = findViewById(R.id.pair);
        setZero = findViewById(R.id.setZero);
        laser = findViewById(R.id.laser);
        settingOffset = findViewById(R.id.settingOffset);
        slope = findViewById(R.id.slope);
        heading_tv = findViewById(R.id.heading_tv);
        textOffset = findViewById(R.id.offset_tv);
        textNameBucket = findViewById(R.id.bucket_tv);
        textSlopeX = findViewById(R.id.slopeX_tv);
        textSlopeY = findViewById(R.id.slopeY_tv);
        textReach = findViewById(R.id.reach_tv);
        textHeight = findViewById(R.id.height_tv);
        bucketEdge = findViewById(R.id.bucketEdge);
        slopeY2D = findViewById(R.id.slopeY2D_tv);
        slopeX2D = findViewById(R.id.slopeX2D_tv);
        depthMode = findViewById(R.id.depthMode);
        showStick = findViewById(R.id.showStick);
        zeroR = findViewById(R.id.setReach);
        allarmeAlt = findViewById(R.id.allarmeAlt);
        allarmeAlt.setVisibility(View.GONE);


    }

    private void init() {
        laserDialog = new LaserDialog(this);
        headingDialog = new HeadingDialog(this);
        dialogOffset = new DialogOffset(this);
        dialogSlope = new DialogSlope(this);
        dialogTouchGo = new Dialog_TouchGo(this);

        indexAudioSystem = MyData.get_Int("indexAudioSystem");
        heightPivot = MyData.get_String("Pivot_Height_Alarm").replace(",", ".");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexBucketSelected = MyData.get_Int("M"+indexMachineSelected+"BucketSelected");
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

        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 4 || index == 5) {
            leftLed.setTextSize(45f);

        }
    }

    private void disableAll() {
        todig.setEnabled(false);

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

        todig.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, Digging2D.class));
            overridePendingTransition(0, 0);
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
        bucketEdge.setOnLongClickListener(view -> {
            DataSaved.isLowerEdge = !DataSaved.isLowerEdge;
            return false;
        });

        bucketEdge.setOnClickListener(view -> {
            if (!DataSaved.isLowerEdge) {
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


            if (flagLaser_C2D) {
                if (!laserDialog.alertDialog.isShowing()) {
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
                    flagLaser_C2D = true;
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

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void updateUI() {
        try {
            if (DataSaved.isLowerEdge) {
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
            } else {
                bucketEdge.setBackgroundColor(getColor(R.color.nav_gray_color));
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
            if (DataSaved.projectionFlag == 0) {
                depthMode.setImageResource((R.drawable.rif_inclinato));
            } else {
                depthMode.setImageResource((R.drawable.rif_verticale));
            }
            double hdt = 0;
            if (DataSaved.useYawFrame == 0) {
                hdt = (NmeaListener.roof_Orientation - DataSaved.offsetHDT);
            } else {
                hdt = (Sensors_Decoder.Deg_Yaw_Frame - DataSaved.offsetHDT);
            }


            if (hdt > 180.0) {
                hdt = hdt - 360;
            }
            if (hdt < -180.0) {
                hdt = hdt + 360;
            }
            if (DataSaved.useYawFrame == 0) {
                if (NmeaListener.roof_Orientation == 999.999) {
                    heading_tv.setText("ERROR");
                    navigator.setRotation((float) hdt * 0);
                } else {
                    try {
                        heading_tv.setText(String.format("%.1f", hdt).replace(",", ".") + "°");
                    } catch (Exception e) {
                        heading_tv.setText(String.format("%.1f", hdt));
                    }
                    navigator.setRotation((float) hdt * -1);
                }
            } else {
                if (CanService.frameDisc) {
                    heading_tv.setText("ERROR");
                    navigator.setRotation((float) hdt * 0);
                } else {
                    try {
                        heading_tv.setText(String.format("%.1f", hdt).replace(",", ".") + "°");
                    } catch (Exception e) {
                        heading_tv.setText(String.format("%.1f", hdt));
                    }
                    navigator.setRotation((float) hdt * -1);
                }
            }

            real_height = !flagLaser_C2D ? ExcavatorLib.quota2D - DataSaved.offsetZH + DataSaved.offsetH : ((DataSaved.offsetLaserZH - ExcavatorLib.quota2D) + DataSaved.offsetH * -1) * -1;
            real_height = real_height - DataSaved.monumentRelease;
            ExcavatorLib.msideC = real_height;
            ExcavatorLib.msideCX = real_height;
            if (DataSaved.projectionFlag == 1 && Math.abs(ExcavatorLib.actualY2D) >= 10) {
                if (ExcavatorLib.distToSurf >= -DataSaved.deadbandH && ExcavatorLib.distToSurf <= DataSaved.deadbandH) {
                    leftLed.setTextColor(Color.GREEN);
                    leftLed.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                    textHeight.setText(leftLed.getText().toString());
                    MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(false, ExcavatorLib.distToSurf, DataSaved.deadbandH));
                    if (!hAlarm) {
                        panel.setBackgroundColor(Color.GREEN);
                        depthMode.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                        zeroR.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                        showStick.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                        textHeight.setTextColor(Color.BLACK);
                        textSlopeY.setTextColor(Color.BLACK);
                        textSlopeX.setTextColor(Color.BLACK);
                        textSlopeX.setTextColor(Color.BLACK);
                        slopeY2D.setTextColor(Color.BLACK);
                        slopeX2D.setTextColor(Color.BLACK);
                        textOffset.setTextColor(Color.BLACK);
                        textNameBucket.setTextColor(Color.BLACK);
                        textReach.setTextColor(Color.BLACK);
                    }
                }
                if (ExcavatorLib.distToSurf > DataSaved.deadbandH) {
                    leftLed.setTextColor(Color.WHITE);
                    leftLed.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                    textHeight.setText(leftLed.getText().toString());
                    MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(false, ExcavatorLib.distToSurf, DataSaved.deadbandH));
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
                        textSlopeX.setTextColor(Color.WHITE);
                        slopeY2D.setTextColor(Color.WHITE);
                        slopeX2D.setTextColor(Color.WHITE);
                    }
                }
                if (ExcavatorLib.distToSurf < -DataSaved.deadbandH) {
                    leftLed.setTextColor(Color.WHITE);
                    leftLed.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distToSurf)));
                    textHeight.setText(leftLed.getText().toString());
                    MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(false, ExcavatorLib.distToSurf, DataSaved.deadbandH));
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
                        textSlopeX.setTextColor(Color.WHITE);
                        slopeY2D.setTextColor(Color.WHITE);
                        slopeX2D.setTextColor(Color.WHITE);
                    }
                }
            } else {
                if (real_height >= -DataSaved.deadbandH && real_height <= DataSaved.deadbandH) {
                    leftLed.setTextColor(Color.GREEN);
                    leftLed.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                    textHeight.setText(leftLed.getText().toString());
                    MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(false, real_height, DataSaved.deadbandH));
                    if (!hAlarm) {
                        panel.setBackgroundColor(Color.GREEN);
                        depthMode.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                        zeroR.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                        showStick.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
                        textHeight.setTextColor(Color.BLACK);
                        textSlopeY.setTextColor(Color.BLACK);
                        textSlopeX.setTextColor(Color.BLACK);
                        slopeY2D.setTextColor(Color.BLACK);
                        slopeX2D.setTextColor(Color.BLACK);
                        textOffset.setTextColor(Color.BLACK);
                        textNameBucket.setTextColor(Color.BLACK);
                        textReach.setTextColor(Color.BLACK);
                    }
                }
                if (real_height > DataSaved.deadbandH) {
                    leftLed.setTextColor(Color.WHITE);
                    leftLed.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                    textHeight.setText(leftLed.getText().toString());
                    MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(false, real_height, DataSaved.deadbandH));
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
                        textSlopeX.setTextColor(Color.WHITE);
                        slopeY2D.setTextColor(Color.WHITE);
                        slopeX2D.setTextColor(Color.WHITE);
                    }
                }
                if (real_height < -DataSaved.deadbandH) {
                    leftLed.setTextColor(Color.WHITE);
                    leftLed.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                    textHeight.setText(leftLed.getText().toString());
                    MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(false, real_height, DataSaved.deadbandH));
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
                        textSlopeX.setTextColor(Color.WHITE);
                        slopeY2D.setTextColor(Color.WHITE);
                        slopeX2D.setTextColor(Color.WHITE);
                    }
                }
            }

            textSlopeX.setText("X: " + Utils.readAngolo(String.valueOf(DataSaved.slopeX)) + Utils.getGradiSimbol());
            textSlopeY.setText("Y: " + Utils.readAngolo(String.valueOf(DataSaved.slopeY)) + Utils.getGradiSimbol());
            textOffset.setText("OFFSET: " + Utils.readUnitOfMeasureLITE(String.valueOf((DataSaved.offsetH * -1))));
            textNameBucket.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucketSelected + "_Name"));
            textReach.setText("r: " + Utils.readUnitOfMeasureLITE(String.valueOf(Math.abs(ExcavatorLib.distanza_inclinata - 0))));


            slopeY2D.setText(Utils.readAngolo(String.valueOf(ExcavatorLib.actualY2D)) + Utils.getGradiSimbol());
            slopeX2D.setText(Utils.readAngolo(String.valueOf(ExcavatorLib.actualX2D)) + Utils.getGradiSimbol());

            if ((ExcavatorLib.correctFlat >= (DataSaved.slopeY - DataSaved.deadbandFlatAngle)) && (ExcavatorLib.correctFlat <= (DataSaved.slopeY + DataSaved.deadbandFlatAngle))) {
                flatAngleBar.indexFlatBar = 1;
            } else if (ExcavatorLib.correctFlat > (DataSaved.slopeY + DataSaved.deadbandFlatAngle)) {
                flatAngleBar.indexFlatBar = 0;
            } else if (ExcavatorLib.correctFlat < (DataSaved.slopeY - DataSaved.deadbandFlatAngle)) {
                flatAngleBar.indexFlatBar = 2;
            }

            if (DataSaved.laserOn == 1) {
                try {
                    if (Excavator_RealValues.realLaser() <= -10 && Sensors_Decoder.flagLaser > -101) {
                        laser.setImageResource(R.drawable.down_btn);
                        laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color._____cancel_text));
                    } else if (Excavator_RealValues.realLaser() == 0 && Sensors_Decoder.flagLaser > -101) {
                        laser.setImageResource(R.drawable.equals_btn);
                        laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                    } else if (Excavator_RealValues.realLaser() >= 10 && Sensors_Decoder.flagLaser > -101) {
                        laser.setImageResource(R.drawable.up_btn);
                        laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color._____cancel_text));
                    } else if (Sensors_Decoder.flagLaser == -101) {
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
                    mAltezza2D_cf = real_height;
                } else {
                    mAltezza2D_cf = ExcavatorLib.distToSurf;
                }
            } catch (Exception e) {
                mAltezza2D_cf = 0;
            }
        } catch (Exception e) {

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
        flagLaser_C2D = false;

    }
}
