package gui.dialogs_user_settings;

import static gui.MyApp.errorCode;
import static gui.dialogs_and_toast.DialogPassword.isTech;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.Dialog_InfoApp;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Sensors_Decoder;
import services.UpdateValuesService;
import utils.LanguageSetter;
import utils.MyData;
import utils.Utils;
import utils.WifiHelper;

public class Nuova_User_Settings extends AppCompatActivity {
    // Variabili di supporto
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
    Dialog_InfoApp dialogInfoApp;
    DialogColors dialogColors;
    TextView tvBrightValue, tvUomValue, tvAngValue, tvAudioValue, tvHAlarmValue, tvVert, tvAng, stepValue, tvoffstep;
    TextView tvVertValue;
    ImageView imgLocale, imgLse, imgCutFill, but_piu, but_meno, but_piu_db, but_meno_db, but_piu_an, but_meno_an;
    String intLang = "";
    int indexAudioSelected;
    double myStep, myStepAngle;
    TextView tvRotateMode, tvAXYValue, tvAngAutoValue, tvZValue,tvWINDOWValue;
    ImageView but_meno_auto_z, but_piu_auto_z, but_meno_ang_auto, but_piu_ang_auto, but_meno_xy, but_piu_xy,but_meno_auto_window,but_piu_auto_window;


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


    }

    private void findView() {
        dialogAudioSystem = new DialogAudioSystem(this);
        dialogPassword = new DialogPassword(this);
        dialogHeightAlarm = new DialogHeightAlarm(this);
        dialogInvertColors = new DialogInvertColors(this);
        dialogLanguages = new DialogLanguages(this);
        dialogUnitOfMeasure = new DialogUnitOfMeasure(this);
        dialogColors = new DialogColors(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogInfoApp = new Dialog_InfoApp(this);


        tvAXYValue = findViewById(R.id.tvAXYValue);
        tvWINDOWValue=findViewById(R.id.tvWINDOWValue);
        tvAngAutoValue = findViewById(R.id.tvAngAutoValue);
        tvZValue = findViewById(R.id.tvZValue);
        but_meno_auto_z = findViewById(R.id.but_meno_auto_z);
        but_piu_auto_z = findViewById(R.id.but_piu_auto_z);
        but_meno_ang_auto = findViewById(R.id.but_meno_ang_auto);
        but_piu_ang_auto = findViewById(R.id.but_piu_ang_auto);
        but_meno_xy = findViewById(R.id.but_meno_xy);
        but_piu_xy = findViewById(R.id.but_piu_xy);
        but_meno_auto_window=findViewById(R.id.but_meno_auto_window);
        but_piu_auto_window=findViewById(R.id.but_piu_auto_window);


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

    }

    private void onClick() {
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
        but_piu_db.setOnClickListener(view -> {
            DataSaved.deadbandH += myStep;
            MyData.push("Deadband_H", String.valueOf(DataSaved.deadbandH));

        });
        but_meno_db.setOnClickListener(view -> {

            if (DataSaved.deadbandH > 0) {
                DataSaved.deadbandH -= myStep;
            }
            if (DataSaved.deadbandH <= myStep) DataSaved.deadbandH = myStep;
            MyData.push("Deadband_H", String.valueOf(DataSaved.deadbandH));
        });
        /// //////////////////////////////////
        /// ////nuovi
        but_piu_auto_z.setOnClickListener(view -> {
            DataSaved.tolleranza_Z += myStep;
            MyData.push("tolleranza_Z", String.valueOf(DataSaved.tolleranza_Z));

        });

        but_meno_auto_z.setOnClickListener(view -> {

            if (DataSaved.tolleranza_Z > 0) {
                DataSaved.tolleranza_Z -= myStep;
            }
            if (DataSaved.tolleranza_Z <= myStep) DataSaved.tolleranza_Z = myStep;
            MyData.push("tolleranza_Z", String.valueOf(DataSaved.tolleranza_Z));
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


        but_piu_xy.setOnClickListener(view -> {
            DataSaved.tolleranza_XY += myStep;
            MyData.push("tolleranza_XY", String.valueOf(DataSaved.tolleranza_XY));

        });

        but_meno_xy.setOnClickListener(view -> {

            if (DataSaved.tolleranza_XY > 0) {
                DataSaved.tolleranza_XY -= myStep;
            }
            if (DataSaved.tolleranza_XY <= myStep) DataSaved.tolleranza_XY = myStep;
            MyData.push("tolleranza_XY", String.valueOf(DataSaved.tolleranza_XY));
        });

        /// /////nuovi

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
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
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
            String ssid = WifiHelper.getConnectedSSID(getApplicationContext());
            if (ssid != null) {

                wifi.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);

            } else {

                wifi.setImageResource(R.drawable.wifi_vuoto);

            }

            if (MyData.get_Double("Pivot_Height_Alarm") == 10000000.0d) {
                tvHAlarmValue.setText("OFF");
            } else {
                tvHAlarmValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(MyData.get_Double("Pivot_Height_Alarm"))));
            }
            switch (DataSaved.laserOn) {
                case 0:
                    imgLse.setImageResource(R.drawable.laser_off_btn);
                    Sensors_Decoder.flagLaser = -100;
                    Sensors_Decoder.flagLaserConnected = -100;
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
                    imgLocale.setImageResource(R.drawable.btn_eng);
                    break;
                case "en_US":
                    imgLocale.setImageResource(R.drawable.btn_usa);
                    break;
                case "it":
                    imgLocale.setImageResource(R.drawable.btn_ita);
                    break;
                case "de":
                    imgLocale.setImageResource(R.drawable.btn_deu);
                    break;
                case "es":
                    imgLocale.setImageResource(R.drawable.btn_esp);
                    break;
                case "fr":
                    imgLocale.setImageResource(R.drawable.btn_fra);
                    break;
                case "ru":
                    imgLocale.setImageResource(R.drawable.btn_rus);
                    break;
                case "zh":
                    imgLocale.setImageResource(R.drawable.btn_chi);
                    break;
                case "pt":
                    imgLocale.setImageResource(R.drawable.btn_pur);
                    break;
                case "el":
                    imgLocale.setImageResource(R.drawable.btn_gre);
                    break;
                case "ko":
                    imgLocale.setImageResource(R.drawable.btn_ko);
                    break;
                case "ro":
                    imgLocale.setImageResource(R.drawable.btn_rom);
                    break;
                case "nl":
                    imgLocale.setImageResource(R.drawable.btn_nl);
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
                    tvZValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_Z)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));



                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.005;
                    break;
                case 1:
                    myStepAngle = 0.055;
                    tvZValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_Z)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.005;
                    break;

                case 2:
                    myStepAngle = 0.1;
                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_Z)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.0033;
                    break;
                case 3:
                    myStepAngle = 0.055;
                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_Z)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.0033;
                    break;

                case 4:
                    myStepAngle = 0.1;
                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_Z)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.0033;
                    break;
                case 5:
                    myStepAngle = 0.055;
                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_Z)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.0033;
                    break;
                case 6:
                    myStepAngle = 0.1;
                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_Z)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.0033;
                    break;
                case 7:
                    myStepAngle = 0.055;
                    tvZValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_Z)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAXYValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.tolleranza_XY)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    tvAngAutoValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.tolleranza_Slope))) + Utils.getGradiSimbol());
                    tvWINDOWValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.HYDRAULIC_WINDOW)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));

                    tvAngValue.setText(Utils.readAngoloLITE((String.valueOf(DataSaved.deadbandFlatAngle))) + Utils.getGradiSimbol());
                    tvVertValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.deadbandH)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    stepValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.Off_Incr_Step)) + "  " + Utils.getMetriSimbol().replace("[", "").replace("]", ""));
                    myStep = 0.0033;
                    break;
            }

            tvoffstep.setText("EXTERNAL OFFSET STEP Inc/Dec");
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

}