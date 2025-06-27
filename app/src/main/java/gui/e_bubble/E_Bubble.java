package gui.e_bubble;

;

import static gui.MyApp.isApollo;

import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.ExcavatorMenuActivity;
import gui.dialogs_and_toast.CustomToast;
import gui.digging_excavator.Digging_CutAndFill1D;
import gui.digging_excavator.Digging1D;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Excavator_RealValues;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import services.CanService;
import utils.LeicaLB;
import utils.MyData;
import utils.MyDeviceManager;
import utils.UnitsConversion;
import utils.Utils;

public class E_Bubble extends BaseClass {
    MediaPlayer mediaPlayer;
    boolean[] audioFlags = new boolean[]{true, true, true, true};
    public boolean flagLaser_Ebub = false;
    private boolean mSentOFF = false;
    private boolean mSentON = false;
    ImageView toMain, setZero, moreDB, lessDB, target, bubble, setQuota, laser, ebubleerr;
    TextView m_X, m_Y, m_DB, leftLed, textReach;
    private int count_Z = 0;
    private boolean zerPressed, inrangeX, inrangeY;

    int indexMachineSelected, indexAudioSystem;
    CheckBox useTilt, useBk;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_ebubble);


        findView();
        init();
        onClic();
        if (DataSaved.enOUT == 1) {
            (new Handler()).postDelayed(this::updateUI, 1000);
        } else {
            updateUI();
        }
    }

    private void findView() {
        toMain = findViewById(R.id.toMain_b);
        setZero = findViewById(R.id.setZeroEbub);
        moreDB = findViewById(R.id.moreDb);
        lessDB = findViewById(R.id.lessDb);
        target = findViewById(R.id.cent_target);
        bubble = findViewById(R.id.bubble);
        m_X = findViewById(R.id.txt_X);
        m_Y = findViewById(R.id.txt_Y);
        m_DB = findViewById(R.id.txtdb);
        useTilt = findViewById(R.id.ck_useTilt);
        useBk = findViewById(R.id.ck_useBucket);
        leftLed = findViewById(R.id.txt_q);
        setQuota = findViewById(R.id.setZeroQuota);
        laser = findViewById(R.id.setLaser);
        textReach = findViewById(R.id.txt_r);
        ebubleerr = findViewById(R.id.ebubleerr);
    }

    private void init() {
        ebubleerr.setVisibility(View.INVISIBLE);
        if (DataSaved.isCanOpen == 1) {
            ebubleerr.setImageResource(R.drawable.debug_btn);
        } else {
            ebubleerr.setImageResource(R.drawable.debug_btn_tsm);
        }
        indexAudioSystem = MyData.get_Int("indexAudioSystem");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        DataSaved.offsetBubble_X = MyData.get_Double("M" + indexMachineSelected + "_ebubbleX");
        DataSaved.offsetBubble_Y = MyData.get_Double("M" + indexMachineSelected + "_ebubbleY");
        DataSaved.bubble_DB = MyData.get_Double("M" + indexMachineSelected + "_ebubbleDB");
        DataSaved.useTiltEbubble = MyData.get_Int("M" + indexMachineSelected + "_ebubbleUseTilt");

        switch (DataSaved.useTiltEbubble) {
            case 0:
                useTilt.setChecked(true);
                useBk.setChecked(false);
                break;
            case 1:
                useBk.setChecked(true);
                useTilt.setChecked(false);
                break;
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClic() {
        laser.setOnClickListener((View v) -> {
            if (DataSaved.laserOn == 0) {
                new CustomToast(this, "LASER OFF").show();
            } else {
                new CustomToast(this, getResources().getString(R.string.toast_hold_to_set)).show();
            }


        });
        laser.setOnLongClickListener((View v) -> {

            if (DataSaved.laserOn == 1) {
                if (Excavator_RealValues.realLaser() == 0) {
                    flagLaser_Ebub = true;
                    DataSaved.offsetLaserZH = ExcavatorLib.quotaLASER_2D;
                    MyData.push("Laser_Height_Zero", String.valueOf(DataSaved.offsetLaserZH));

                } else {
                    new CustomToast(this, "OUT OF RANGE!").show_alert();
                    flagLaser_Ebub = false;

                }
            }
            return true;
        });
        setQuota.setOnLongClickListener((View v) -> {
            flagLaser_Ebub = false;
            DataSaved.offsetZH = ExcavatorLib.quota2D;
            DataSaved.start2DX = ExcavatorLib.bucketCoord[0];
            DataSaved.start2DY = ExcavatorLib.bucketCoord[1];
            DataSaved.start2DZ = ExcavatorLib.bucketCoord[2];
            ExcavatorLib.startRX = DataSaved.start2DX;
            ExcavatorLib.startRY = DataSaved.start2DY;
            ExcavatorLib.startRZ = DataSaved.start2DZ;
            MyData.push("Offset_Zero", String.valueOf(DataSaved.offsetZH));


            return true;
        });


        useTilt.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (useTilt.isChecked()) {
                DataSaved.useTiltEbubble = 0;
                useBk.setChecked(false);
            }
        });
        useBk.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (useBk.isChecked()) {
                DataSaved.useTiltEbubble = 1;
                useTilt.setChecked(false);
            }
        });
        toMain.setOnClickListener(view -> {
            toMain.setEnabled(false);
            save();
            Intent intent = getIntent();
            String s = intent.getStringExtra("who");
            if (s.equals("A1D")) {

                startActivity(new Intent(this, Digging1D.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (s.equals("C1D")) {
                startActivity(new Intent(this, Digging_CutAndFill1D.class));
                overridePendingTransition(0, 0);
                finish();
            } else if (s.equals("E1D")) {
                startActivity(new Intent(this, ExcavatorMenuActivity.class));
                overridePendingTransition(0, 0);
                finish();
            } else {
                startActivity(new Intent(this, ExcavatorMenuActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
        setZero.setOnClickListener(v -> {

        });
        setZero.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    zerPressed = true;
                    count_Z = 0;
                    return false;

                case MotionEvent.ACTION_UP:
                    zerPressed = false;
                    count_Z = 0;
                    return false;
            }
            return false;
        });
        moreDB.setOnClickListener(v -> {
            if (DataSaved.bubble_DB >= 0) {
                DataSaved.bubble_DB += 0.1;
            } else {
                DataSaved.bubble_DB = 0;
            }

        });
        lessDB.setOnClickListener(v -> {
            if (DataSaved.bubble_DB >= 0) {
                DataSaved.bubble_DB -= 0.1;
            } else {
                DataSaved.bubble_DB = 0;
            }

        });
    }

    public void updateUI() {
        if (DataSaved.bubble_DB < 0) {
            DataSaved.bubble_DB = 0;
        }

        if (zerPressed) {
            count_Z++;

            if (count_Z == 50) {
                //setOffset qui
                if (DataSaved.useTiltEbubble == 0) {
                    DataSaved.offsetBubble_X = Sensors_Decoder.Deg_tilt;
                    DataSaved.offsetBubble_Y = Sensors_Decoder.Deg_Benna_W_Tilt;
                    saveZero();
                } else {
                    DataSaved.offsetBubble_X = Sensors_Decoder.Deg_roll;
                    DataSaved.offsetBubble_Y = Sensors_Decoder.Deg_bucket;
                    saveZero();
                }

            }
        }


        try {
            double real_height = !flagLaser_Ebub ? ExcavatorLib.quotaCentro - DataSaved.offsetZH + DataSaved.offsetH : ((DataSaved.offsetLaserZH - ExcavatorLib.quotaCentro) + DataSaved.offsetH * -1) * -1;


            if (DataSaved.useTiltEbubble == 0 && !CanService.tiltDisc) {//usa il tilt
                ebubleerr.setVisibility(View.INVISIBLE);
            } else if (DataSaved.useTiltEbubble == 0 && CanService.tiltDisc) {//usa il tilt
                ebubleerr.setVisibility(View.VISIBLE);
            }
            if (DataSaved.useTiltEbubble != 0 && !CanService.frameDisc) {//usa benna+roll
                Log.d("EBUBBLE", "sensorOK");
                ebubleerr.setVisibility(View.INVISIBLE);
            } else if (DataSaved.useTiltEbubble != 0 && CanService.frameDisc) {//usa benna+roll
                Log.d("EBUBBLE", "sensor NOT OK");
                ebubleerr.setVisibility(View.VISIBLE);
            }

            if (zerPressed && count_Z <= 50) {
                m_X.setText("WAIT.." + (6 - (count_Z / 10)));
                m_Y.setText("WAIT.." + (6 - (count_Z / 10)));
            } else if (zerPressed && count_Z > 50) {
                m_X.setText("OK");
                m_Y.setText("OK");
            } else {
                m_X.setText("\tX: " + String.format("%.1f", ExcavatorLib.correctEbubbleX).replace(",", ".") + "°");
                m_Y.setText("Y: " + String.format("%.1f", ExcavatorLib.correctEbubbleY).replace(",", ".") + "°");
            }
            m_DB.setText(String.format("%.1f", DataSaved.bubble_DB).replace(",", ".") + "°");
            inrangeX = Math.abs(ExcavatorLib.correctEbubbleX) <= DataSaved.bubble_DB + 0.05;
            inrangeY = Math.abs(ExcavatorLib.correctEbubbleY) <= DataSaved.bubble_DB + 0.05;
            if (inrangeX) {
                m_X.setTextColor(Color.GREEN);
            } else {
                m_X.setTextColor(Color.LTGRAY);
            }
            if (inrangeY) {
                m_Y.setTextColor(Color.GREEN);
            } else {
                m_Y.setTextColor(Color.LTGRAY);
            }
            if (inrangeX && inrangeY) {
                target.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
                bubble.setColorFilter(getResources().getColor(R.color.bg_sfsgreen), PorterDuff.Mode.SRC_ATOP);
                bubble.setTranslationX(moveX());
                bubble.setTranslationY(moveY());
                if (!mSentON) {

                    mSentON = true;
                    mSentOFF = false;


                }
            } else {
                target.setColorFilter(getResources().getColor(R.color.light_gray), PorterDuff.Mode.SRC_ATOP);
                bubble.setColorFilter(getResources().getColor(R.color.bg_sfsred), PorterDuff.Mode.SRC_ATOP);
                bubble.setTranslationX(moveX());
                bubble.setTranslationY(moveY());

                if (!mSentOFF) {

                    mSentOFF = true;
                    mSentON = false;
                }
            }
            if (real_height >= -DataSaved.deadbandH && real_height <= DataSaved.deadbandH) {
                leftLed.setTextColor(Color.GREEN);
                leftLed.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                MyDeviceManager.CanWrite(0,0xA8,3, LeicaLB.mapping(false,real_height,DataSaved.deadbandH));
            }
            if (real_height > DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);

                leftLed.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                MyDeviceManager.CanWrite(0,0xA8,3, LeicaLB.mapping(false,real_height,DataSaved.deadbandH));
            }
            if (real_height < -DataSaved.deadbandH) {
                leftLed.setTextColor(Color.WHITE);
                leftLed.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                MyDeviceManager.CanWrite(0,0xA8,3, LeicaLB.mapping(false,real_height,DataSaved.deadbandH));
            }
            if (DataSaved.laserOn == 1) {
                try {
                    if (Excavator_RealValues.realLaser() <= -10 && Sensors_Decoder.flagLaser != -101) {
                        laser.setImageResource(R.drawable.down_btn);
                        laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color._____cancel_text));
                    } else if (Excavator_RealValues.realLaser() == 0 && Sensors_Decoder.flagLaser != -101) {
                        laser.setImageResource(R.drawable.equals_btn);
                        laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                    } else if (Excavator_RealValues.realLaser() >= 10 && Sensors_Decoder.flagLaser != -101) {
                        laser.setImageResource(R.drawable.up_btn);
                        laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color._____cancel_text));
                    } else if (!(Sensors_Decoder.flagLaser != -101)) {
                        laser.setImageResource(R.drawable.laser_btn);
                        laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.nav_gray_color));
                    }

                } catch (Exception ignored) {
                    laser.setImageResource(R.drawable.laser_btn);
                    laser.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.nav_gray_color));
                }

            } else {
                laser.setImageResource(R.drawable.laser_off_btn);
            }
            if (indexAudioSystem == 1) {
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

                    mediaPlayer.stop();
                    audioFlags = new boolean[]{true, false, true, true};

                }
                if (real_height < -DataSaved.deadbandH && audioFlags[2]) {
                    try {
                        mediaPlayer.reset();
                    } catch (Exception ignored) {
                    }

                    mediaPlayer.stop();
                    audioFlags = new boolean[]{true, true, false, true};

                }
            }
            byte[] b0b1 = PLC_DataTypes_LittleEndian.S16_to_bytes((short) -moveX());
            byte[] b2b3 = PLC_DataTypes_LittleEndian.S16_to_bytes((short) -moveY());
            if (isApollo) {
                MyDeviceManager.CanWrite(0, 0x2F9,8, new byte[]{b0b1[0], b0b1[1], b2b3[0], b2b3[1], 0, 0, 0, (byte) 0xFA});
            }
            textReach.setText("r: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.distanza_inclinata - 0)));

        } catch (Exception e) {
            if (isApollo) {
                MyDeviceManager.CanWrite(0, 0x2F9,8, new byte[8]);
            }
        }

    }

    private void save() {
        MyData.push("M" + indexMachineSelected + "_ebubbleDB", String.valueOf(DataSaved.bubble_DB));
        MyData.push("M" + indexMachineSelected + "_ebubbleUseTilt", String.valueOf(DataSaved.useTiltEbubble));

    }

    private void saveZero() {
        MyData.push("M" + indexMachineSelected + "_ebubbleX", String.valueOf(DataSaved.offsetBubble_X));
        MyData.push("M" + indexMachineSelected + "_ebubbleY", String.valueOf(DataSaved.offsetBubble_Y));
    }

    public float moveX() {
        float traslX = 0f;
        double angle = UnitsConversion.limitD(-ExcavatorLib.correctEbubbleX, -30.0, 30.0);
        traslX = UnitsConversion.myscaleF((float) angle, -30.0f, 30.0f, -300f, 300f);


        return traslX;
    }

    public float moveY() {
        float traslY = 0f;
        double angle = UnitsConversion.limitD(-ExcavatorLib.correctEbubbleY, -30.0, 30.0);
        traslY = UnitsConversion.myscaleF((float) angle, -30.0f, 30.0f, -200f, 200f);

        return traslY;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        flagLaser_Ebub = false;
        try {

            if (indexAudioSystem == 1) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {

        }


    }
}