package gui.debug_ecu;


import static gui.MyApp.licenseType;
import static services.CanService.boom1Disc;
import static services.CanService.boom2Disc;
import static services.CanService.bucketDisc;
import static services.CanService.frameDisc;
import static services.CanService.stickDisc;
import static services.CanService.tiltDisc;

import android.annotation.SuppressLint;

import android.content.Intent;

import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;



import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.DiggingProfile;

import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Excavator_RealValues;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.gnss.NmeaListener;
import services.CanService;
import services.ReadProjectService;
import utils.MyData;
import utils.MyDeviceManager;


public class DebugExcavatorActivity extends BaseClass {
    int count=0;

    String b1, b2, st, bk, fr, tl, ls;
    ImageView back, status, toDig, coord;
    TextView boom1An, boom1Offset, boom1Stat, boom2An, boom2Offset, boom2Stat, stickAn, stickOffset, stickStat, statStickLaser, bucketAn, bucketOffset, bucketStat,
            pitchAn, pitchOffset, pitchStat, compassAn, compassOffset, compassStat, tiltAn, tiltOffset, tiltStat,
            posBoom1, posBoom2, posStick, posFrame, posBucket, posCompass, posTilt, voltage;


    ProgressBar progressBar;
    Float power = 0.0f;
    Float batt = 0.0f;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        findView();
        init();
        onClick();
        check();
        updateUI();
        power=0.0f;
    }

    private void findView() {
        back = findViewById(R.id.back);
        status = findViewById(R.id.status);
        boom1An = findViewById(R.id.ValueBoom1_tv);
        boom2Offset = findViewById(R.id.OffsetBoom2_tv);
        boom1Offset = findViewById(R.id.OffsetBoom1_tv);
        boom1Stat = findViewById(R.id.StatusBoom1_tv);
        boom2An = findViewById(R.id.ValueBoom2_tv);
        boom2Stat = findViewById(R.id.StatusBoom2_tv);
        stickAn = findViewById(R.id.ValueStick_tv);
        stickOffset = findViewById(R.id.OffsetStick_tv);
        stickStat = findViewById(R.id.StatusStick_tv);
        bucketAn = findViewById(R.id.ValueBucket_tv);
        bucketOffset = findViewById(R.id.OffsetBucket_tv);
        bucketStat = findViewById(R.id.StatusBucket_tv);
        pitchAn = findViewById(R.id.ValuePitch_tv);
        pitchOffset = findViewById(R.id.OffsetPitch_tv);
        pitchStat = findViewById(R.id.StatusPitch_tv);
        compassAn = findViewById(R.id.ValueCompass_tv);
        compassOffset = findViewById(R.id.OffsetCompass_tv);
        compassStat = findViewById(R.id.StatusCompass_tv);
        tiltAn = findViewById(R.id.ValueTilt_tv);
        tiltOffset = findViewById(R.id.OffsetTilt_tv);
        tiltStat = findViewById(R.id.StatusTilt_tv);
        statStickLaser = findViewById(R.id.StatusStickLaser_tv);
        posBoom1 = findViewById(R.id.ValueBoom1_Pos);
        posBoom2 = findViewById(R.id.ValueBoom2_Pos);
        posStick = findViewById(R.id.ValueStick_Pos);
        posBucket = findViewById(R.id.ValueBucket_Pos);
        posFrame = findViewById(R.id.ValuePitch_Pos);
        posTilt = findViewById(R.id.ValueTilt_Pos);
        posCompass = findViewById(R.id.ValueCompass_Pos);
        voltage = findViewById(R.id.voltage);
        toDig = findViewById(R.id.pair);
        coord = findViewById(R.id.coord);
        progressBar = findViewById(R.id.progressBar);



    }
    private void updateVoltage(){
        try {
            power = MyDeviceManager.getVoltage();

        } catch (Exception e) {
            power=0f;
        }
    }

    private void check() {
        if (DataSaved.lrBucket < 5) {

            switch (DataSaved.lrBoom1) {

                case 0:
                    b1 = "OFF";
                    break;
                case 1:
                    b1 = "L";
                    break;
                case -1:
                    b1 = "R";
                    break;

            }
            switch (DataSaved.lrBoom2) {
                case 0:
                    b2 = "OFF";
                    break;
                case 1:
                    b2 = "L";
                    break;
                case -1:
                    b2 = "R";
                    break;

            }
            switch (DataSaved.lrStick) {
                case 0:
                    st = "OFF";
                    break;
                case 1:
                    st = "L";
                    break;
                case -1:
                    st = "R";
                    break;

            }
            switch (DataSaved.lrBucket) {
                case 0:
                    bk = "OFF";
                    break;
                case 1:
                    bk = "L";
                    break;
                case -1:
                    bk = "R";
                    break;
                case 2:
                    bk = "T";
                    break;


            }
            switch (DataSaved.lrFrame) {
                case 0:
                    fr = "OFF";
                    break;
                case 1:
                    fr = "FW";
                    break;
                case 2:
                    fr = "RT";
                    break;
                case 3:
                    fr = "BW";
                    break;
                case 4:
                    fr = "LT";
                    break;

            }
            switch (DataSaved.lrTilt) {
                case 0:
                    tl = "OFF";
                    break;
                case 1:
                    tl = "L";
                    break;
                case -1:
                    tl = "R";
                    break;

            }

        } else {
            b1 = "OFF";
            b2 = "OFF";
            st = "OFF";
            bk = "OFF";
            fr = "OFF";
            tl = "OFF";
        }
    }

    private void init() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void updateUI() {
        switch (DataSaved.isWL){
            case 0:
                toDig.setImageResource(R.drawable.go_dig);
                break;

            case 1:
                toDig.setImageResource(R.drawable.go_dig);
                break;

            case 2:
                toDig.setImageResource(R.drawable.go_grade);
                break;

            default:
                toDig.setImageResource(R.drawable.go_dig);
                break;
        }
        count++;
        if(count%50==0){
            updateVoltage();
        }
        if (DataSaved.lrBucket < 5) {



            check();


            if (DataSaved.lrBucket < 5) {
                try {
                    if(Build.BRAND.contains("APOLLO2")) {
                        voltage.setText("");

                    }else {
                        voltage.setText(String.format("%.1f", power / 1000).replace(",", ".") + " V");
                    }


                    posBoom1.setText(b1);
                    posBoom2.setText(b2);
                    posStick.setText(st);
                    posBucket.setText(bk);
                    posTilt.setText(tl);
                    posFrame.setText(fr);
                    posCompass.setText("HDT");
                    boom1An.setText(String.format("%.2f", ExcavatorLib.correctBoom1).replace(",", ".") + " °");
                    boom1Offset.setText(String.format("%.2f", DataSaved.offsetBoom1).replace(",", ".") + " °");
                    boom2An.setText(String.format("%.2f", ExcavatorLib.correctBoom2).replace(",", ".") + " °");
                    boom2Offset.setText(String.format("%.2f", DataSaved.offsetBoom2).replace(",", ".") + " °");
                    stickAn.setText(String.format("%.2f", ExcavatorLib.correctStick).replace(",", ".") + " °");
                    stickOffset.setText(String.format("%.2f", DataSaved.offsetStick).replace(",", ".") + " °");
                    bucketAn.setText(String.format("%.2f", ExcavatorLib.correctBucket).replace(",", ".") + " °" + " (W Tilt " + String.format("%.2f", ExcavatorLib.correctWTilt) + "°)");
                    bucketOffset.setText(String.format("%.2f", DataSaved.offsetBucket).replace(",", ".") + " °" + " (W Tilt " + String.format("%.2f", DataSaved.offsetDegWTilt) + "°)");
                    pitchAn.setText("P: " + String.format("%.2f", ExcavatorLib.correctPitch).replace(",", ".") + " ° / R: " + String.format("%.2f", ExcavatorLib.correctRoll) + " °");
                    pitchOffset.setText("P: " + String.format("%.2f", DataSaved.offsetPitch).replace(",", ".") + " ° / R: " + String.format("%.2f", DataSaved.offsetRoll) + " °");
                    tiltAn.setText(String.format("%.2f", ExcavatorLib.correctTilt).replace(",", ".") + " °");
                    tiltOffset.setText(String.format("%.2f", DataSaved.offsetTilt).replace(",", ".") + " °  - Yaw ("+String.format("%.2f", Sensors_Decoder.Deg_Yaw_Tilt).replace(",", ".")+")");
                    if (!CanService.boom1OK) {
                        boom1Stat.setText("DISCONNECTED");
                        boom1Stat.setTextColor(getColor(R.color.red));
                    } else {
                        boom1Stat.setTextColor(getColor(R.color.green));
                        boom1Stat.setText("CONNECTED");
                    }

                    if (!CanService.boom2OK) {
                        boom2Stat.setText("DISCONNECTED");
                        boom2Stat.setTextColor(getColor(R.color.red));
                    } else {
                        boom2Stat.setTextColor(getColor(R.color.green));
                        boom2Stat.setText("CONNECTED");
                    }
                    if (!CanService.stickOK) {
                        stickStat.setText("DISCONNECTED");
                        stickStat.setTextColor(getColor(R.color.red));
                    } else {
                        stickStat.setTextColor(getColor(R.color.green));
                        stickStat.setText("CONNECTED");
                    }
                    if (!CanService.bucketOK) {
                        bucketStat.setText("DISCONNECTED");
                        bucketStat.setTextColor(getColor(R.color.red));
                    } else {
                        bucketStat.setTextColor(getColor(R.color.green));
                        bucketStat.setText("CONNECTED");
                    }
                    if (!CanService.frameOK) {
                        pitchStat.setText("DISCONNECTED");
                        pitchStat.setTextColor(getColor(R.color.red));
                    } else {
                        pitchStat.setTextColor(getColor(R.color.green));
                        pitchStat.setText("CONNECTED");
                    }
                    if (!CanService.tiltOK ) {
                        tiltStat.setText("DISCONNECTED");
                        tiltStat.setTextColor(getColor(R.color.red));
                    } else {
                        tiltStat.setTextColor(getColor(R.color.green));
                        tiltStat.setText("CONNECTED");
                    }
                    if(DataSaved.useYawFrame==0) {
                        compassAn.setText(String.format("%.1f", NmeaListener.mch_Hdt).replace(",", "."));
                        if (NmeaListener.mch_Hdt == 999.999) {
                            compassStat.setText("ERROR");
                            compassStat.setTextColor(getColor(R.color.red));
                        } else {
                            compassStat.setText("CONNECTED");
                            compassStat.setTextColor(getColor(R.color.green));
                        }
                    }else {
                        compassAn.setText(String.format("%.1f", Sensors_Decoder.Deg_Yaw_Frame).replace(",", "."));
                        if (frameDisc) {
                            compassStat.setText("ERROR");
                            compassStat.setTextColor(getColor(R.color.red));
                        } else {
                            compassStat.setText("CONNECTED");
                            compassStat.setTextColor(getColor(R.color.green));
                        }
                    }
                    compassOffset.setText(String.format("%.1f", DataSaved.offsetHDT).replace(",", "."));

                    if (!(Sensors_Decoder.flagLaser > -101)) {
                        statStickLaser.setText("OFF");
                        statStickLaser.setTextColor(getColor(R.color.white));
                    } else {

                        if (Excavator_RealValues.realLaser() < 0) {
                            statStickLaser.setText("▼ " + String.valueOf(Excavator_RealValues.realLaser()));
                            statStickLaser.setTextColor(getColor(R.color.red));
                        } else if (Excavator_RealValues.realLaser() > 0) {
                            statStickLaser.setTextColor(getColor(R.color.cyan));
                            statStickLaser.setText("▲ " + String.valueOf(Excavator_RealValues.realLaser()));
                        } else {
                            statStickLaser.setTextColor(getColor(R.color.green));
                            statStickLaser.setText("⧗ " + String.valueOf(Excavator_RealValues.realLaser()));

                        }
                    }
                    if (!frameDisc || !boom1Disc || !boom2Disc || !stickDisc || !bucketDisc || !tiltDisc) {
                        status.setImageResource(R.drawable.can_ok);
                    } else {
                        status.setImageResource(R.drawable.can_err);
                    }


                } catch (Exception e) {
                    System.out.println(e.toString());
                }

            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    private void disableAll() {

        toDig.setEnabled(false);
        back.setEnabled(false);
        coord.setEnabled(false);

    }
    private void enableAll() {

        toDig.setEnabled(true);
        back.setEnabled(true);
        coord.setEnabled(true);

    }

    private void onClick() {
        toDig.setOnClickListener((View v) -> {
            disableAll();
            int profile = MyData.get_Int("ProfileSelected");
            int typeView = MyData.get_Int("indexView");
            if (profile == 0) {
                switch (typeView) {
                    case 0:
                        if(licenseType >-1) {
                            startActivity(new Intent(this, Digging1D.class));
                            finish();
                        }else {
                            enableAll();
                            new CustomToast(this,"LICENSE MISSED").show_alert();
                        }
                        break;
                    case 1:
                        if(licenseType >0) {
                            startActivity(new Intent(this, Digging2D.class));
                            finish();
                        }else {
                            enableAll();
                            new CustomToast(this,"LICENSE MISSED").show_alert();
                        }
                        break;
                    case 2:
                    case 3:
                        if(licenseType >1) {
                            progressBar.setVisibility(View.VISIBLE);
                            startService(new Intent(this, ReadProjectService.class));
                        }else {
                            enableAll();
                            new CustomToast(this,"LICENSE MISSED").show_alert();
                        }
                        break;
                }
            } else {
                startActivity(new Intent(this, DiggingProfile.class));
                finish();
            }
        });

        back.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });
        coord.setOnClickListener((View v) -> {
            Intent intent=new Intent(this, Can_Msg_Debug.class);
            intent.putExtra("chi","debug");
            startActivity(intent);
            finish();
        });
        voltage.setOnClickListener(view -> {
        });
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


}