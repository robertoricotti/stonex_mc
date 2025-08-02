package gui.dialogs_user_settings;

import static gui.MyApp.errorCode;
import static gui.dialogs_and_toast.DialogPassword.isTech;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
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

    ImageView status, wifi, back, info, lock;
    DialogAudioSystem dialogAudioSystem;
    DialogHeightAlarm dialogHeightAlarm;
    DialogPassword dialogPassword;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    DialogInvertColors dialogInvertColors;
    DialogDeadbandFlatAngle dialogDeadbandFlatAngle;
    DialogDeadbandH dialogDeadbandH;
    DialogLanguages dialogLanguages;
    DialogUnitOfMeasure dialogUnitOfMeasure;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    Dialog_InfoApp dialogInfoApp;
    DialogColors dialogColors;
    TextView tvBrightValue, tvUomValue, tvAngValue, tvAudioValue, tvHAlarmValue,tvVert,tvAng;
    EditText tvVertValue;
    ImageView imgLocale, imgLse, imgCutFill;
    String intLang="";
    int indexAudioSelected;

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
        dialogAudioSystem=new DialogAudioSystem(this);
        dialogPassword=new DialogPassword(this);
        dialogHeightAlarm=new DialogHeightAlarm(this);
        dialogInvertColors=new DialogInvertColors(this);
        customNumberDialogFtIn=new CustomNumberDialogFtIn(this,0);
        dialogDeadbandFlatAngle=new DialogDeadbandFlatAngle(this);
        dialogDeadbandH =new DialogDeadbandH(this);
        dialogLanguages=new DialogLanguages(this);
        dialogUnitOfMeasure=new DialogUnitOfMeasure(this);
        dialogColors = new DialogColors(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogInfoApp = new Dialog_InfoApp(this);
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
        tvVert=findViewById(R.id.tvVert);
        tvAng=findViewById(R.id.tvAng);

    }

    private void init() {
        intLang = MyData.get_String("language");
        indexAudioSelected = MyData.get_Int("indexAudioSystem");
    }

    private void onClick() {
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
            if(!dialogInvertColors.dialog.isShowing()){
                dialogInvertColors.show();
            }
        });
        tvAngValue.setOnClickListener(view -> {
            if(!dialogDeadbandFlatAngle.dialog.isShowing()){
                dialogDeadbandFlatAngle.show();
            }
        });
        tvVertValue.setOnClickListener(view -> {
            if(MyData.get_String("Unit_Of_Measure").equals("4")||MyData.get_String("Unit_Of_Measure").equals("5")){
                if(!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(tvVertValue);
                }
            }else {
                if (!dialogDeadbandH.dialog.isShowing()) {
                    dialogDeadbandH.show();
                }
            }
        });
        imgLocale.setOnClickListener(view -> {
            if(!dialogLanguages.dialog.isShowing()){
                dialogLanguages.show();
            }
        });
        tvUomValue.setOnClickListener(view -> {
            if(!dialogUnitOfMeasure.alertDialog.isShowing()){
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
    }

    public void updateUI() {
        try {
            indexAudioSelected = MyData.get_Int("indexAudioSystem");
            tvAudioValue.setText(indexAudioSelected == 0 ? "OFF" : ("ON / " + MyData.get_Int("volumeAudioSystem") * 10) + " %");
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
            tvVert.setText(getString(R.string.height_deadband)+"  (+/-) ");
            tvAng.setText(getString(R.string.flat_angle_deadband)+"  (+/-) ");
            tvAngValue.setText(Utils.readAngolo(MyData.get_String("Deadband_FlatAngle") ) +  Utils.getGradiSimbol());
            tvVertValue.setText(Utils.readSensorCalibration(MyData.get_String("Deadband_H"))+Utils.getMetriSimbol());
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
            switch (DataSaved.colorMode){
                case 0:
                    imgCutFill.setImageResource(R.drawable.cutfillred);
                    break;

                case 1:
                    imgCutFill.setImageResource(R.drawable.cutfillblu);
                    break;
            }
        } catch (Exception ex) {
            MyData.push("Deadband_H","0.025");
            Log.d("Panerai",Log.getStackTraceString(ex));
        }
    }

    private void en_dis(boolean b) {
        back.setEnabled(b);
        status.setEnabled(b);
        info.setEnabled(b);
    }

}