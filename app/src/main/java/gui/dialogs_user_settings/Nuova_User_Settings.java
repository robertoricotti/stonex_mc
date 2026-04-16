package gui.dialogs_user_settings;

import static drill_pile.gui.PickReport.getAllUtcOffsets;
import static gui.MyApp.errorCode;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static utils.MyTypes.DRILL;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import java.util.List;

import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.Dialog_InfoApp;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.UtcOffset;
import services.CanService;
import services.UpdateValuesService;
import utils.LanguageSetter;
import utils.MyData;
import utils.Utils;

public class Nuova_User_Settings extends BaseClass {
    List<UtcOffset> offsets;
    int selectedOffsetMinutes;
    // Variabili di supporto
    static int miocoloreBenna, miocoloreStick;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRepeating = false;
    ImageView status, wifi, back, info, lock;
    DialogAudioSystem dialogAudioSystem;
    DialogHeightAlarm dialogHeightAlarm;
    DialogPassword dialogPassword;
    DialogInvertColors dialogInvertColors;
    DialogLanguages dialogLanguages;
    DialogUnitOfMeasure dialogUnitOfMeasure;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    Dialog_Drill_GNSS dialogDrillGnss;
    Dialog_InfoApp dialogInfoApp;
    DialogColors dialogColors;
    TextView tvBrightValue, tvUomValue, tvAngValue, tvAudioValue, tvHAlarmValue, tvVert, tvAng, stepValue, tvoffstep, tvMainfallValue;
    TextView tvVertValue, tvZValueR, tvUtcOffset;
    ImageView imgLocale, imgLse, imgCutFill, but_piu, but_meno, but_piu_db, but_meno_db, but_piu_an, but_meno_an, but_meno_mainfall, but_piu_ang_mainfall;
    String intLang = "";
    int indexAudioSelected;
    double myStep, myStepAngle;
    TextView tvRotateMode, tvAXYValue, tvAngAutoValue, tvZValue, tvWINDOWValue;
    ImageView but_meno_auto_zR, but_piu_auto_zR, but_meno_auto_z, but_piu_auto_z, but_meno_ang_auto, but_piu_ang_auto, but_meno_xy, but_piu_xy, but_meno_auto_window, but_piu_auto_window;
    ImageView but_meno_bkc, but_piu_bkc, but_meno_boom, but_piu_boom;
    TextView coloreBenna, coloreBoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            LanguageSetter.setLocale(this, MyData.get_String("language"));
        } catch (Exception e) {

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settaggio_utente);
        findView();
        init();
        onClick();
        updateUI();
        offsets = getAllUtcOffsets();

    }

    private void findView() {


        // ricava indice se ti serve per ciclarli
        miocoloreBenna = colorResToIndex(MyColorClass.colorBucket);
        miocoloreStick = colorResToIndex(MyColorClass.colorStick);
        dialogAudioSystem = new DialogAudioSystem(this);
        dialogPassword = new DialogPassword(this);
        dialogHeightAlarm = new DialogHeightAlarm(this);
        dialogInvertColors = new DialogInvertColors(this);
        dialogLanguages = new DialogLanguages(this);
        dialogUnitOfMeasure = new DialogUnitOfMeasure(this);
        dialogColors = new DialogColors(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogDrillGnss = new Dialog_Drill_GNSS(this);
        dialogInfoApp = new Dialog_InfoApp(this);

        tvMainfallValue = findViewById(R.id.tvMainfallValue);
        but_meno_mainfall = findViewById(R.id.but_meno_mainfall);
        but_piu_ang_mainfall = findViewById(R.id.but_piu_ang_mainfall);

        but_piu_auto_zR = findViewById(R.id.but_piu_auto_zR);
        but_meno_auto_zR = findViewById(R.id.but_meno_auto_zR);
        tvZValueR = findViewById(R.id.tvZValueR);
        tvUtcOffset = findViewById(R.id.tvUtcOffset);
        tvAXYValue = findViewById(R.id.tvAXYValue);
        tvWINDOWValue = findViewById(R.id.tvWINDOWValue);
        tvAngAutoValue = findViewById(R.id.tvAngAutoValue);
        tvZValue = findViewById(R.id.tvZValue);
        but_meno_auto_z = findViewById(R.id.but_meno_auto_z);
        but_piu_auto_z = findViewById(R.id.but_piu_auto_z);
        but_meno_ang_auto = findViewById(R.id.but_meno_ang_auto);
        but_piu_ang_auto = findViewById(R.id.but_piu_ang_auto);
        but_meno_xy = findViewById(R.id.but_meno_xy);
        but_piu_xy = findViewById(R.id.but_piu_xy);
        but_meno_auto_window = findViewById(R.id.but_meno_auto_window);
        but_piu_auto_window = findViewById(R.id.but_piu_auto_window);


        but_meno_bkc = findViewById(R.id.but_meno_bkc);
        but_piu_bkc = findViewById(R.id.but_piu_bkc);
        but_meno_boom = findViewById(R.id.but_meno_boom);
        but_piu_boom = findViewById(R.id.but_piu_boom);

        coloreBenna = findViewById(R.id.coloreBenna);
        coloreBoom = findViewById(R.id.coloreBoom);


        status = findViewById(R.id.img00);
        wifi = findViewById(R.id.img01);
        back = findViewById(R.id.btn_1);
        info = findViewById(R.id.btn_3);
        lock = findViewById(R.id.btn_2);
        tvBrightValue = findViewById(R.id.tvBrightValue);
        tvUomValue = findViewById(R.id.tvUomValue);
        tvVertValue = findViewById(R.id.tvVertValue);
        tvAngValue = findViewById(R.id.tvAngValue);
        tvAudioValue = findViewById(R.id.tvAudioValue);
        tvHAlarmValue = findViewById(R.id.tvHAlarmValue);
        imgLocale = findViewById(R.id.imgLocale);
        imgLse = findViewById(R.id.imgLse);
        imgCutFill = findViewById(R.id.imgCutFill);
        tvVert = findViewById(R.id.tvVert);
        tvAng = findViewById(R.id.tvAng);
        stepValue = findViewById(R.id.tvOffStepValue);
        tvoffstep = findViewById(R.id.tvoffstep);
        but_piu = findViewById(R.id.but_piu);
        but_meno = findViewById(R.id.but_meno);
        but_piu_db = findViewById(R.id.but_piu_db);
        but_meno_db = findViewById(R.id.but_meno_db);
        but_piu_an = findViewById(R.id.but_piu_ang);
        but_meno_an = findViewById(R.id.but_meno_ang);
        tvRotateMode = findViewById(R.id.tvRotateMode);

    }

    private void init() {
        intLang = MyData.get_String("language");
        indexAudioSelected = MyData.get_Int("indexAudioSystem");
        tvUtcOffset.setText(formatOffset(DataSaved.UTC_Offset));

    }

    private void onClick() {
        tvUtcOffset.setOnClickListener(view -> {
            showUtcPopupMenu(view);
        });
        but_meno_bkc.setOnClickListener(view -> {
            miocoloreBenna = normalizeIndex(miocoloreBenna - 1);

            int resId = indexToColorRes(miocoloreBenna);                  // resource id
            int colorInt = ContextCompat.getColor(this, resId);           // colore ARGB

            MyColorClass.colorBucket = colorInt;                          // salva ARGB in memoria
            MyData.push("M" + MyData.get_Int("MachineSelected") + "coloreBenna", String.valueOf(colorInt));         // salva ARGB in DB

            applyColorsToViews();
        });

        but_piu_bkc.setOnClickListener(view -> {
            miocoloreBenna = normalizeIndex(miocoloreBenna + 1);

            int resId = indexToColorRes(miocoloreBenna);
            int colorInt = ContextCompat.getColor(this, resId);

            MyColorClass.colorBucket = colorInt;
            MyData.push("M" + MyData.get_Int("MachineSelected") + "coloreBenna", String.valueOf(colorInt));

            applyColorsToViews();
        });

// --- analoghi per boom ---

        but_meno_boom.setOnClickListener(view -> {
            miocoloreStick = normalizeIndex(miocoloreStick - 1);

            int resId = indexToColorRes(miocoloreStick);
            int colorInt = ContextCompat.getColor(this, resId);

            MyColorClass.colorStick = colorInt;
            MyData.push("M" + MyData.get_Int("MachineSelected") + "coloreStick", String.valueOf(colorInt));

            applyColorsToViews();
        });

        but_piu_boom.setOnClickListener(view -> {
            miocoloreStick = normalizeIndex(miocoloreStick + 1);

            int resId = indexToColorRes(miocoloreStick);
            int colorInt = ContextCompat.getColor(this, resId);

            MyColorClass.colorStick = colorInt;
            MyData.push("M" + MyData.get_Int("MachineSelected") + "coloreStick", String.valueOf(colorInt));

            applyColorsToViews();
        });


        but_piu.setOnClickListener(view -> {
            DataSaved.Off_Incr_Step += myStep;
            MyData.push("Off_Incr_Step", String.valueOf(DataSaved.Off_Incr_Step));

        });
        but_meno.setOnClickListener(view -> {
            if (DataSaved.Off_Incr_Step > 0) {
                DataSaved.Off_Incr_Step -= myStep;
            }
            if (DataSaved.Off_Incr_Step <= myStep) DataSaved.Off_Incr_Step = myStep;
            MyData.push("Off_Incr_Step", String.valueOf(DataSaved.Off_Incr_Step));
        });
        /// //////////////////////////////////

        setupAutoRepeat(but_piu_db, () -> {
            DataSaved.deadbandH += myStep;
            MyData.push("Deadband_H", String.valueOf(DataSaved.deadbandH));
        });
        setupAutoRepeat(but_meno_db, () -> {
            if (DataSaved.deadbandH > 0) {
                DataSaved.deadbandH -= myStep;
            }
            if (DataSaved.deadbandH <= myStep) DataSaved.deadbandH = myStep;
            MyData.push("Deadband_H", String.valueOf(DataSaved.deadbandH));
        });


        /// //////////////////////////////////
        /// ////nuovi
        but_piu_auto_z.setOnClickListener(view -> {
            DataSaved.tolleranza_ZL += myStep;
            MyData.push("tolleranza_ZL", String.valueOf(DataSaved.tolleranza_ZL));

        });

        but_meno_auto_z.setOnClickListener(view -> {

            if (DataSaved.tolleranza_ZL > 0) {
                DataSaved.tolleranza_ZL -= myStep;
            }
            if (DataSaved.tolleranza_ZL <= myStep) DataSaved.tolleranza_ZL = myStep;
            MyData.push("tolleranza_ZL", String.valueOf(DataSaved.tolleranza_ZL));
        });
        but_piu_auto_zR.setOnClickListener(view -> {
            DataSaved.tolleranza_ZR += myStep;
            MyData.push("tolleranza_ZR", String.valueOf(DataSaved.tolleranza_ZR));

        });

        but_meno_auto_zR.setOnClickListener(view -> {

            if (DataSaved.tolleranza_ZR > 0) {
                DataSaved.tolleranza_ZR -= myStep;
            }
            if (DataSaved.tolleranza_ZR <= myStep) DataSaved.tolleranza_ZR = myStep;
            MyData.push("tolleranza_ZR", String.valueOf(DataSaved.tolleranza_ZR));
        });


        setupAutoRepeat(but_piu_auto_window, () -> {
            DataSaved.HYDRAULIC_WINDOW += myStep;
            MyData.push("HYDRAULIC_WINDOW", String.valueOf(DataSaved.HYDRAULIC_WINDOW));
        });

        setupAutoRepeat(but_meno_auto_window, () -> {
            if (DataSaved.HYDRAULIC_WINDOW > 0) {
                DataSaved.HYDRAULIC_WINDOW -= myStep;
            }
            if (DataSaved.HYDRAULIC_WINDOW <= myStep) {
                DataSaved.HYDRAULIC_WINDOW = myStep;
            }
            MyData.push("HYDRAULIC_WINDOW", String.valueOf(DataSaved.HYDRAULIC_WINDOW));
        });


        but_piu_ang_auto.setOnClickListener(view -> {
            DataSaved.tolleranza_Slope += myStepAngle;
            MyData.push("tolleranza_Slope", String.valueOf(DataSaved.tolleranza_Slope));

        });

        but_meno_ang_auto.setOnClickListener(view -> {

            if (DataSaved.tolleranza_Slope > 0) {
                DataSaved.tolleranza_Slope -= myStepAngle;
            }
            if (DataSaved.tolleranza_Slope <= myStepAngle) DataSaved.tolleranza_Slope = myStepAngle;
            MyData.push("tolleranza_Slope", String.valueOf(DataSaved.tolleranza_Slope));
        });


        setupAutoRepeat(but_piu_xy, () -> {
            DataSaved.tolleranza_XY += myStep;
            MyData.push("tolleranza_XY", String.valueOf(DataSaved.tolleranza_XY));
        });
        setupAutoRepeat(but_meno_xy, () -> {
            if (DataSaved.tolleranza_XY > 0) {
                DataSaved.tolleranza_XY -= myStep;
            }
            if (DataSaved.tolleranza_XY <= myStep) DataSaved.tolleranza_XY = myStep;
            MyData.push("tolleranza_XY", String.valueOf(DataSaved.tolleranza_XY));

        });


        but_piu_an.setOnClickListener(view -> {
            DataSaved.deadbandFlatAngle += myStepAngle;
            MyData.push("Deadband_FlatAngle", String.valueOf(DataSaved.deadbandFlatAngle));

        });

        but_meno_an.setOnClickListener(view -> {

            if (DataSaved.deadbandFlatAngle > 0) {
                DataSaved.deadbandFlatAngle -= myStepAngle;
            }
            if (DataSaved.deadbandFlatAngle <= myStepAngle)
                DataSaved.deadbandFlatAngle = myStepAngle;
            MyData.push("Deadband_FlatAngle", String.valueOf(DataSaved.deadbandFlatAngle));
        });

        tvAudioValue.setOnClickListener(view -> {
            if (!dialogAudioSystem.alertDialog.isShowing()) {
                dialogAudioSystem.show();
            }
        });
        lock.setOnClickListener(view -> {
            if (!isTech) {
                if (!dialogPassword.dialog.isShowing()) {
                    dialogPassword.show(-1);
                }
            }

        });
        tvHAlarmValue.setOnClickListener(view -> {
            if (!dialogHeightAlarm.alertDialog.isShowing()) {
                dialogHeightAlarm.show();
            }
        });
        imgLse.setOnClickListener(view -> {
            if (DataSaved.laserOn == 0) {
                DataSaved.laserOn = 1;
                MyData.push("laserOn", String.valueOf(DataSaved.laserOn));
            } else if (DataSaved.laserOn == 1) {
                DataSaved.monumentRelease = 0;
                DataSaved.laserOn = 0;
                MyData.push("laserOn", String.valueOf(DataSaved.laserOn));
            }
        });
        imgCutFill.setOnClickListener(view -> {
            if (!dialogInvertColors.dialog.isShowing()) {
                dialogInvertColors.show();
            }
        });


        imgLocale.setOnClickListener(view -> {
            if (!dialogLanguages.dialog.isShowing()) {
                dialogLanguages.show();
            }
        });
        tvUomValue.setOnClickListener(view -> {
            if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                dialogUnitOfMeasure.show();
            }
        });
        tvBrightValue.setOnClickListener(view -> {
            if (!dialogColors.dialog.isShowing()) {
                dialogColors.show();
            }
        });
        status.setOnClickListener(view -> {
            if (DataSaved.isWL == DRILL) {
                if (!dialogDrillGnss.alertDialog.isShowing()) {
                    dialogDrillGnss.show();
                }
            } else {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                    dialogGnssCoordinates.show();
                }
            }
        });
        back.setOnClickListener(view -> {
            en_dis(false);
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });
        info.setOnClickListener(view -> {
            if (!dialogInfoApp.dialog.isShowing()) {
                dialogInfoApp.show();
            }
        });
        tvRotateMode.setOnClickListener(view -> {
            DataSaved.lock3dRotation++;
            DataSaved.lock3dRotation = DataSaved.lock3dRotation % 2;
            MyData.push("lock3dRotation", String.valueOf(DataSaved.lock3dRotation));
        });


        /// //////////////////////////////
        but_piu_ang_mainfall.setOnClickListener(view -> {
            DataSaved.Mainfall_Distance += myStep;
            MyData.push("Mainfall_Distance", String.valueOf(DataSaved.Mainfall_Distance));

        });
        but_meno_mainfall.setOnClickListener(view -> {
            if (DataSaved.Mainfall_Distance > 0) {
                DataSaved.Mainfall_Distance -= myStep;
            }
            if (DataSaved.Mainfall_Distance <= myStep) DataSaved.Mainfall_Distance = myStep;
            MyData.push("Mainfall_Distance", String.valueOf(DataSaved.Mainfall_Distance));
        });
        /// //////////////////////////////////

        setupAutoRepeat(but_piu_ang_mainfall, () -> {
            DataSaved.Mainfall_Distance += myStep;
            MyData.push("Mainfall_Distance", String.valueOf(DataSaved.Mainfall_Distance));
        });
        setupAutoRepeat(but_meno_mainfall, () -> {
            if (DataSaved.Mainfall_Distance > 0) {
                DataSaved.Mainfall_Distance -= myStep;
            }
            if (DataSaved.Mainfall_Distance <= myStep) DataSaved.Mainfall_Distance = myStep;
            MyData.push("Mainfall_Distance", String.valueOf(DataSaved.Mainfall_Distance));
        });
    }

    public void updateUI() {
        try {
            indexAudioSelected = MyData.get_Int("indexAudioSystem");
            tvAudioValue.setText(indexAudioSelected == 0 ? "OFF" : ("ON / " + MyData.get_Int("volumeAudioSystem") * 10) + " %");
            if (DataSaved.lock3dRotation > 0) {
                tvRotateMode.setText(R.string.rotate_w);
            } else {
                tvRotateMode.setText(R.string.rotate_m);
            }

            if (isTech) {
                lock.setImageResource(R.drawable.unlock);

            } else {
                lock.setImageResource(R.drawable.lock);
            }
            if (DataSaved.gpsOk && errorCode == 0) {
                status.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                status.setImageTintList(ColorStateList.valueOf(Color.RED));
            }
            try {
                switch (DataSaved.ConnectionStatus) {
                    case 0:
                        wifi.setImageResource(R.drawable.network_off);
                        break;

                    case 1:
                        wifi.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);
                        break;

                    case 2:
                        wifi.setImageResource(R.drawable.sim_96);
                        break;
                    default:
                        wifi.setImageResource(R.drawable.network_off);
                        break;
                }

            } catch (Exception e) {
                wifi.setBackgroundColor(getColor(R.color.light_yellow));
            }

            if (MyData.get_Double("Pivot_Height_Alarm") == 10000000.0d) {
                tvHAlarmValue.setText("OFF");
            } else {
                tvHAlarmValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(MyData.get_Double("Pivot_Height_Alarm"))));
            }
            switch (DataSaved.laserOn) {
                case 0:
                    imgLse.setImageResource(R.drawable.laser_off_btn);
                    CanService.flagLaser = false;

                    break;
                case 1:
                    imgLse.setImageResource(R.drawable.laser_on_btn);
                    break;

            }
            tvVert.setText(getString(R.string.height_deadband) + "  (+/-) ");
            tvAng.setText(getString(R.string.flat_angle_deadband) + "  (+/-) ");
            tvBrightValue.setText(String.format("%.0f", DataSaved.myBrightness * 100) + "%");
            tvUomValue.setText(Utils.getMetriSimbol().replace("[", "").replace("]", "") + " / " + Utils.getGradiSimbol());
            switch (intLang) {
                case "en_GB":
                    imgLocale.setImageResource(R.drawable.uk);
                    break;
                case "en_US":
                    imgLocale.setImageResource(R.drawable.usa);
                    break;
                case "it":
                    imgLocale.setImageResource(R.drawable.italia);
                    break;
                case "de":
                    imgLocale.setImageResource(R.drawable.germania);
                    break;
                case "es":
                    imgLocale.setImageResource(R.drawable.spagna);
                    break;
                case "fr":
                    imgLocale.setImageResource(R.drawable.francia);
                    break;
                case "ru":
                    imgLocale.setImageResource(R.drawable.russia);
                    break;
                case "zh":
                    imgLocale.setImageResource(R.drawable.cina);
                    break;
                case "pt":
                    imgLocale.setImageResource(R.drawable.portogallo);
                    break;
                case "el":
                    imgLocale.setImageResource(R.drawable.grecia);
                    break;
                case "ko":
                    imgLocale.setImageResource(R.drawable.south_korea);
                    break;
                case "ro":
                    imgLocale.setImageResource(R.drawable.romania);
                    break;
                case "nl":
                    imgLocale.setImageResource(R.drawable.olanda);
                    break;
                case "ja":
                    imgLocale.setImageResource(R.drawable.giappone);
                    break;
                case "cs":
                    imgLocale.setImageResource(R.drawable.repubblica_ceca);
                    break;
                case "nb":
                    imgLocale.setImageResource(R.drawable.norvegia);
                    break;
                case "sv":
                    imgLocale.setImageResource(R.drawable.svezia);
                    break;
                case "da":
                    imgLocale.setImageResource(R.drawable.danimarca);
                    break;
                case "is":
                    imgLocale.setImageResource(R.drawable.islanda);
                    break;
                case "fi":
                    imgLocale.setImageResource(R.drawable.finlandia);
                    break;
                default:
                    imgLocale.setImageResource(R.drawable.baseline_help_96);
                    break;
            }
            switch (DataSaved.colorMode) {
                case 0:
                    imgCutFill.setImageResource(R.drawable.cutfillred);
                    break;

                case 1:
                    imgCutFill.setImageResource(R.drawable.cutfillblu);
                    break;
            }

            int myInt = MyData.get_Int("Unit_Of_Measure");

            switch (myInt) {
                case 0:
                    myStepAngle = 0.1;
                    tvZValueR.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_ZR)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvZValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_ZL)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvMainfallValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Mainfall_Distance)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.005;
                    break;
                case 1:
                    myStepAngle = 0.055;
                    tvZValueR.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_ZR)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvZValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_ZL)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvMainfallValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Mainfall_Distance)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.005;
                    break;

                case 2:
                    myStepAngle = 0.1;
                    tvZValueR.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZR)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZL)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvMainfallValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Mainfall_Distance)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.003048;
                    break;
                case 3:
                    myStepAngle = 0.055;
                    tvZValueR.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZR)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZL)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvMainfallValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Mainfall_Distance)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.003048;
                    break;

                case 4:
                    myStepAngle = 0.1;
                    tvZValueR.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZR)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZL)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvMainfallValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Mainfall_Distance)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.003048;
                    break;
                case 5:
                    myStepAngle = 0.055;
                    tvZValueR.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZR)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZL)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvMainfallValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Mainfall_Distance)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.003048;
                    break;
                case 6:
                    myStepAngle = 0.1;
                    tvZValueR.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZR)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZL)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvMainfallValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Mainfall_Distance)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.003048;
                    break;
                case 7:
                    myStepAngle = 0.055;
                    tvZValueR.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZR)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_ZL)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvMainfallValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Mainfall_Distance)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.003048;
                    break;
            }

            tvoffstep.setText("EXTERNAL OFFSET STEP Inc/Dec");

            applyColorsToViews();

        } catch (Exception ex) {
            Log.e("UserMenu", Log.getStackTraceString(ex));
        }
    }

    private void en_dis(boolean b) {
        back.setEnabled(b);
        status.setEnabled(b);
        info.setEnabled(b);
    }


    private void setupAutoRepeat(ImageView button, Runnable action) {
        button.setOnClickListener(v -> action.run());

        button.setOnLongClickListener(v -> {
            isRepeating = true;

            // Primo ritardo di 500ms prima di iniziare la ripetizione
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRepeating) {
                        action.run();
                        handler.postDelayed(this, 50); // ripeti ogni 50ms
                    }
                }
            }, 500);

            return true; // segnala che il long click è gestito
        });

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isRepeating = false; // stop
                    break;
            }
            return false;
        });
    }


    private static final int[] COLOR_RES = {

            R.color._____cancel_text,
            R.color.volvo_grey,
            R.color.purple_700,
            R.color.blue,
            R.color.magenta,
            R.color.purple_200,
            R.color.cyan,
            R.color.hitachi,
            R.color.bg,
            R.color.orange,
            R.color.yellow,
            R.color.element_green,
            R.color.white
    };

    private int normalizeIndex(int idx) {
        int n = COLOR_RES.length;
        return ((idx % n) + n) % n;
    }

    private int indexToColorRes(int idx) {
        return COLOR_RES[normalizeIndex(idx)];
    }

    // se ti serve: ottenere indice dal resource id (ritorna -1 se non presente)
    private int colorResToIndex(int resId) {
        for (int i = 0; i < COLOR_RES.length; i++) {
            if (COLOR_RES[i] == resId) return i;
        }
        return -1;
    }

    private void applyColorsToViews() {
        coloreBenna.setBackgroundColor(MyColorClass.colorBucket);
        coloreBoom.setBackgroundColor(MyColorClass.colorStick);
    }

    private void showUtcPopupMenu(View anchor) {

        PopupMenu popup = new PopupMenu(this, anchor);

        for (int i = 0; i < offsets.size(); i++) {
            popup.getMenu().add(
                    0,
                    i,
                    i,
                    offsets.get(i).label
            );
        }

        popup.setOnMenuItemClickListener(item -> {

            UtcOffset sel = offsets.get(item.getItemId());

            int selectedOffsetMinutes = sel.minutes; // il numero intero che ti serve

            // Salva con MyData
            MyData.push("UTC_Offset", String.valueOf(selectedOffsetMinutes));
            DataSaved.UTC_Offset = selectedOffsetMinutes;

            // Aggiorna TextView
            tvUtcOffset.setText(sel.label);

            return true;
        });

        popup.show();
    }

    private String formatOffset(int minutes) {
        int h = minutes / 60;
        int m = Math.abs(minutes % 60);
        return String.format("UTC%+03d:%02d", h, m);
    }

}