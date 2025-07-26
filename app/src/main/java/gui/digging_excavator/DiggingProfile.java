package gui.digging_excavator;


import static gui.MyApp.hAlarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stx_dig.R;

import java.util.ArrayList;

import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogOffset;
import gui.draw_class.DrawProfile;
import gui.draw_class.FlatAngleBar;
import gui.draw_class.HeightLevelBar;
import gui.profiles.ProfilesMenuActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.LeicaLB;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class DiggingProfile extends BaseClass {
    private boolean mSentOFF = false;

    private boolean mSentON = false;

    ConstraintLayout indicatorH, profileView;

    ImageView setZero, profileMenu, settingOffset, shortcut, zoomOut, zoomIn, centerin, planeMode;

    TextView leftLed;

    FrameLayout centerLed;

    TextView textOffset, textInfo, textHeight, textReach, textSlopeY;

    DrawProfile drawProfile;

    HeightLevelBar leftBar;

    FlatAngleBar flatAngleBar;
    int indexMachineSelected;
    int indexProfileSelected;
    public static int indexOPSelected;
    int indexBucketSelected;
    int indexAudioSystem;
    int tipoQuota = 0;
    String heightPivot;
    boolean[] audioFlags = new boolean[]{true, true, true, true};

    MediaPlayer mediaPlayer;

    ArrayList<PointF> profilePoints;

    DialogOffset dialogOffset;


    @SuppressLint({"ResourceType", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dig_profile);


        findView();
        init();
        onClick();
        onLongClick();
        updateUI();
        DataSaved.isLowerEdge = false;


    }

    @SuppressLint("ClickableViewAccessibility")
    private void findView() {
        indicatorH = findViewById(R.id.IndicatorH);
        leftLed = findViewById(R.id.leftCorner);
        profileView = findViewById(R.id.panelProfile);
        centerLed = findViewById(R.id.deltaCenter);
        shortcut = findViewById(R.id.shortcut);
        setZero = findViewById(R.id.setZero);
        zoomIn = findViewById(R.id.zoomIn);
        zoomOut = findViewById(R.id.zoomOut);
        settingOffset = findViewById(R.id.settingOffset);
        profileMenu = findViewById(R.id.profileMenu);
        textOffset = findViewById(R.id.offset_tv);
        textInfo = findViewById(R.id.info_tv);
        textHeight = findViewById(R.id.height_tv);
        textReach = findViewById(R.id.reach_tv);
        textSlopeY = findViewById(R.id.slopeY_tv);
        centerin = findViewById(R.id.zoomC);
        planeMode = findViewById(R.id.planeMode);


    }

    @SuppressLint("SetTextI18n")
    private void init() {
        dialogOffset = new DialogOffset(this);
        indexAudioSystem = MyData.get_Int("indexAudioSystem");
        heightPivot = MyData.get_String("Pivot_Height_Alarm").replace(",", ".");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexBucketSelected = MyData.get_Int("M"+indexMachineSelected+"BucketSelected");
        indexProfileSelected = MyData.get_Int("ProfileSelected");
        indexOPSelected = MyData.get_Int("Profile" + indexProfileSelected + "_OP");

        profilePoints = new ArrayList<>();

        String pts = MyData.get_String("Profile" + indexProfileSelected + "_punti");

        if (pts.length() > 0) {
            for (int i = 0; i < pts.split(";").length; i++) {
                profilePoints.add(new PointF(Float.parseFloat(pts.split(";")[i].split("/")[1]), Float.parseFloat(pts.split(";")[i].split("/")[2])));
            }
        }

        drawProfile = new DrawProfile(this, profilePoints);

        profileView.addView(drawProfile);

        leftBar = new HeightLevelBar(this);
        indicatorH.addView(leftBar);

        flatAngleBar = new FlatAngleBar(this);
        centerLed.addView(flatAngleBar);
        int index = MyData.get_Int("Unit_Of_Measure");
        if (index == 4 || index == 5) {
            leftLed.setTextSize(45f);

        }
    }

    private void disableAll() {
        profileMenu.setEnabled(false);
        shortcut.setEnabled(false);
    }

    private void onClick() {
        leftLed.setOnClickListener(view -> {
            indexOPSelected = ++indexOPSelected > profilePoints.size() - 1 ? 1 : indexOPSelected;
            MyData.push("Profile" + indexProfileSelected + "_OP", String.valueOf(indexOPSelected));
        });
        centerin.setOnClickListener(view -> {
            drawProfile.offsetX = 0;
            drawProfile.offsetY = 0;
            drawProfile.invalidate();

        });
        profileMenu.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, ProfilesMenuActivity.class));
            finish();
        });

        settingOffset.setOnClickListener((View v) -> {
            if (!dialogOffset.dialog.isShowing()) {
                dialogOffset.show();
            }
        });

        shortcut.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, Activity_Home_Page.class));
            overridePendingTransition(0, 0);
            finish();
        });

        zoomIn.setOnClickListener((View v) -> {
            drawProfile.mScaleFactor *= 1.2f;
            drawProfile.mScaleFactor = Math.max(0.1f, Math.min(drawProfile.mScaleFactor, 50.0f));
        });

        zoomOut.setOnClickListener((View v) -> {
            drawProfile.mScaleFactor *= 0.80;
            drawProfile.mScaleFactor = Math.max(0.1f, Math.min(drawProfile.mScaleFactor, 50.0f));
        });

        setZero.setOnClickListener((View v) -> {
            new CustomToast(this, getResources().getString(R.string.toast_hold_to_set)).show();
        });

        planeMode.setOnClickListener((View v) -> {
            tipoQuota = tipoQuota == 0 ? 1 : 0;
        });
    }

    private void onLongClick() {
        setZero.setOnLongClickListener((View v) -> {
            ExcavatorLib.coordBuckProf[0] = ExcavatorLib.bucketCoord[0];
            ExcavatorLib.coordBuckProf[1] = ExcavatorLib.bucketCoord[1];
            ExcavatorLib.coordBuckProf[2] = ExcavatorLib.bucketCoord[2];
            return true;
        });
    }


    @SuppressLint("SetTextI18n")
    public void updateUI() {

        alerts();


        planeMode.setImageResource(tipoQuota == 0 ? R.drawable.rif_inclinato : R.drawable.rif_verticale);

        double real_height = tipoQuota == 0 ? ExcavatorLib.altezzaVerticale : ExcavatorLib.altezzaProiettata;

        drawProfile.distanza = ExcavatorLib.distanzaOrizzontale;


        if (real_height >= -DataSaved.deadbandH && real_height <= DataSaved.deadbandH) {
            leftBar.indexLeverBar = 2;
        } else if (real_height > -DataSaved.deadbandH && real_height <= DataSaved.deadbandH + 0.05) {
            leftBar.indexLeverBar = 1;
        } else if (real_height < -DataSaved.deadbandH && real_height >= -DataSaved.deadbandH - 0.05) {
            leftBar.indexLeverBar = 3;
        } else if (real_height > DataSaved.deadbandH + 0.05) {
            leftBar.indexLeverBar = 0;
        } else if (real_height < -DataSaved.deadbandH - 0.05) {
            leftBar.indexLeverBar = 4;
        }
        if (real_height >= -DataSaved.deadbandH && real_height <= DataSaved.deadbandH) {
            if (ExcavatorLib.indexSezione == -1) {
                textHeight.setTextColor(Color.YELLOW);
                textHeight.setTextSize(65);
                textHeight.setBackgroundColor(getColor(R.color.gray));
                textHeight.setText("OUT OF RANGE!");
                MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(true, real_height, DataSaved.deadbandH));
            } else {
                if (!hAlarm) {
                    textHeight.setTextColor(Color.BLACK);
                    textHeight.setBackgroundColor(Color.GREEN);
                }
                textHeight.setTextSize(85);
                textHeight.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
                MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(false, real_height, DataSaved.deadbandH));
                drawProfile.coloreQuota = Color.GREEN;
            }
        }
        if (real_height > DataSaved.deadbandH) {
            if (!hAlarm) {
                textHeight.setTextColor(Color.WHITE);
                textHeight.setBackgroundColor(DataSaved.colorMode == 0 ? Color.RED : Color.BLUE);
            }
            textHeight.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
            MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(false, real_height, DataSaved.deadbandH));
            drawProfile.coloreQuota = Color.WHITE;
        }
        if (real_height < -DataSaved.deadbandH) {
            if (!hAlarm) {
                textHeight.setTextColor(Color.WHITE);

                textHeight.setBackgroundColor(DataSaved.colorMode == 0 ? Color.BLUE : Color.RED);
            }
            textHeight.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(real_height)));
            MyDeviceManager.CanWrite(0, 0xA0, 3, LeicaLB.mapping(false, real_height, DataSaved.deadbandH));
            drawProfile.coloreQuota = Color.WHITE;
        }

        if ((ExcavatorLib.correctFlat >= (DataSaved.slopeY - DataSaved.deadbandFlatAngle)) && (ExcavatorLib.correctFlat <= (DataSaved.slopeY + DataSaved.deadbandFlatAngle))) {
            flatAngleBar.indexFlatBar = 1;
        } else if (ExcavatorLib.correctFlat > (DataSaved.slopeY + DataSaved.deadbandFlatAngle)) {
            flatAngleBar.indexFlatBar = 0;
        } else if (ExcavatorLib.correctFlat < (DataSaved.slopeY - DataSaved.deadbandFlatAngle)) {
            flatAngleBar.indexFlatBar = 2;
        }

        leftLed.setText("OP: " + indexOPSelected);
        textReach.setText("r: " + Utils.readUnitOfMeasureLITE(String.valueOf(Math.abs(ExcavatorLib.distanzaOrizzontale - profilePoints.get(indexOPSelected - 1).x))));
        textSlopeY.setText("Y: " + Utils.readAngolo(String.valueOf(ExcavatorLib.slopeProfile * -1)) + Utils.getGradiSimbol());
        textOffset.setText("OFFSET: " + Utils.readUnitOfMeasureLITE(String.valueOf((DataSaved.offsetH * -1))));
        textInfo.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + indexBucketSelected + "_Name") + " - " + MyData.get_String("Profile" + indexProfileSelected + "_name"));

        drawProfile.invalidate();
        flatAngleBar.invalidate();
        leftBar.invalidate();


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


    }

    private void alerts() {
        if (!hAlarm) {
            if (!mSentOFF) {

                mSentOFF = true;
                mSentON = false;
            }
            findViewById(R.id.panelProfile).setBackgroundColor(getColor(R.color.transparent));

        } else {
            findViewById(R.id.panelProfile).setBackgroundColor(Color.YELLOW);
            textHeight.setBackgroundColor(Color.YELLOW);
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

    }
}
