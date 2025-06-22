package gui.dialogs_user_settings;

import static gui.MyApp.KEY_LEVEL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.CustomToast;
import gui.projects.Dialog_PRJ_Folder;
import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.DiggingProfile;
import gui.boot_and_choose.ExcavatorMenuActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Sensors_Decoder;
import services.ReadProjectService;
import services.UpdateValuesService;
import utils.LanguageSetter;
import utils.MyData;
import utils.Utils;

public class ExUserSettings extends BaseClass {
    Dialog_PRJ_Folder dialogPrjFolder;
    DialogColors dialogColors;
    DialogInvertColors dialogInvertColors;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    DialogHeightAlarm dialogHeightAlarm;
    DialogLanguages dialogLanguages;
    DialogUnitOfMeasure dialogUnitOfMeasure;
    DialogViewport dialogViewport;
    DialogAudioSystem dialogAudioSystem;
    DialogDeadbandFlatAngle dialogDeadbandFlatAngle;
    DialogDeadbandH dialogDeadbandH;



    TextView setDeadbandH, setDeadbandFlatAngle, setHeightAlarm, setViewport, setAudio, setUnitofMeasure;
    ImageView back, toDig, language, toColors, toChooseCutFill, laserOnOff, toGpsAcc;

    ImageButton dbH, dbFA, alarmH, viewport, audio, unit;

    int indexProfileSelected, indexAudioSelected, indexTypeViewSelected, indexUnitOfMeasureSelected;
    int indexMachineSelected, indexBucketSelected;
    String intLang;

    double vAlarm = 0.0d;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        LanguageSetter.setLocale(this, MyData.get_String("language"));
        findView();
        init();
        onClick();
        updateUI();

    }

    private void findView() {
        setDeadbandH = findViewById(R.id.setDeadbandH);
        setDeadbandFlatAngle = findViewById(R.id.setDeadbandFlatAngle);
        setHeightAlarm = findViewById(R.id.setHeightAlarm);
        setViewport = findViewById(R.id.setViewport);
        setAudio = findViewById(R.id.setAudio);
        setUnitofMeasure = findViewById(R.id.setUnitofmeasure);
        language = findViewById(R.id.language);
        dbH = findViewById(R.id.dbH);
        dbFA = findViewById(R.id.dbFA);
        alarmH = findViewById(R.id.alarmH);
        viewport = findViewById(R.id.viewport);
        audio = findViewById(R.id.audio);
        unit = findViewById(R.id.unit);
        back = findViewById(R.id.back);
        toDig = findViewById(R.id.pair);
        toColors = findViewById(R.id.toDrawing);
        toChooseCutFill = findViewById(R.id.tochooseCutFill);
        laserOnOff = findViewById(R.id.laserOnOff);
        toGpsAcc = findViewById(R.id.toGpsaccuracy);
        progressBar = findViewById(R.id.progressBar);


    }

    private void init() {

        progressBar.setVisibility(View.INVISIBLE);
        dialogColors = new DialogColors(this);
        dialogInvertColors = new DialogInvertColors(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogHeightAlarm = new DialogHeightAlarm(this);
        dialogLanguages = new DialogLanguages(this);
        dialogUnitOfMeasure = new DialogUnitOfMeasure(this);
        dialogViewport = new DialogViewport(this);
        dialogAudioSystem = new DialogAudioSystem(this);
        dialogDeadbandFlatAngle = new DialogDeadbandFlatAngle(this);
        dialogDeadbandH = new DialogDeadbandH(this);

        dialogPrjFolder=new Dialog_PRJ_Folder(this);
        if(DataSaved.isWL==2||DataSaved.isWL==3||DataSaved.isWL==4){
            dbH.setImageResource(R.drawable.tolleranza_metri_lama);
            dbFA.setImageResource(R.drawable.tolleranza_angolo_lama);

        }else {
            dbH.setImageResource(R.drawable.tolleranza_metri_);
            dbFA.setImageResource(R.drawable.tolleranza_angolo);
        }

    }

    @SuppressLint("SetTextI18n")
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
        indexProfileSelected = MyData.get_Int("ProfileSelected");
        indexAudioSelected = MyData.get_Int("indexAudioSystem");
        indexTypeViewSelected = MyData.get_Int("indexView");
        indexUnitOfMeasureSelected = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexBucketSelected = MyData.get_Int("BucketSelected");
        intLang = MyData.get_String("language");
        setDeadbandH.setText("+/- " + Utils.readUnitOfMeasureLITE(MyData.get_String("Deadband_H")));
        setDeadbandFlatAngle.setText("+/- " + Utils.readAngolo(MyData.get_String("Deadband_FlatAngle") ) + " " + Utils.getGradiSimbol());

        vAlarm = MyData.get_Double("Pivot_Height_Alarm");
        if (vAlarm == 10000000.0d) {
            setHeightAlarm.setText("OFF");
        } else {
            setHeightAlarm.setText(Utils.readUnitOfMeasureLITE(String.valueOf(vAlarm)));
        }

        setAudio.setText(indexAudioSelected == 0 ? "OFF" : ("ON / " + MyData.get_Int("volumeAudioSystem") * 10) + " %");

        switch (DataSaved.portView) {
            case 0:
            case 1:
                toGpsAcc.setAlpha(0.3f);
                toGpsAcc.setEnabled(false);
                break;
            case 2:
            case 3:
                toGpsAcc.setAlpha(1f);
                toGpsAcc.setEnabled(true);
                if (DataSaved.gpsOk) {
                    toGpsAcc.setImageResource(R.drawable.gps_si);
                } else {
                    toGpsAcc.setImageResource(R.drawable.gps_no);
                }
                break;
        }


        switch (DataSaved.laserOn) {
            case 0:
                laserOnOff.setImageResource(R.drawable.laser_off_btn);
                Sensors_Decoder.flagLaser = -100;
                Sensors_Decoder.flagLaserConnected = -100;
                break;
            case 1:
                laserOnOff.setImageResource(R.drawable.laser_on_btn);
                break;

        }
        switch (intLang) {
            case "en_GB":
                language.setImageResource(R.drawable.btn_eng);
                break;
            case "en_US":
                language.setImageResource(R.drawable.btn_usa);
                break;
            case "it":
                language.setImageResource(R.drawable.btn_ita);
                break;
            case "de":
                language.setImageResource(R.drawable.btn_deu);
                break;
            case "es":
                language.setImageResource(R.drawable.btn_esp);
                break;
            case "fr":
                language.setImageResource(R.drawable.btn_fra);
                break;
            case "ru":
                language.setImageResource(R.drawable.btn_rus);
                break;
            case "zh":
                language.setImageResource(R.drawable.btn_chi);
                break;
            case "pt":
                language.setImageResource(R.drawable.btn_pur);
                break;
            case "el":
                language.setImageResource(R.drawable.btn_gre);
                break;
            case "ko":
                language.setImageResource(R.drawable.btn_ko);
                break;
            case "ro":
                language.setImageResource(R.drawable.btn_rom);
                break;
            case "nl":
                language.setImageResource(R.drawable.btn_nl);
                break;
        }
        switch (indexTypeViewSelected) {
            case 0:
                setViewport.setText("1D");
                break;
            case 1:
                setViewport.setText("2D");
                break;
            case 2:
            case 3:
            case 4:
                setViewport.setText("3D");
                break;
        }
        switch (indexUnitOfMeasureSelected) {
            case 0:
                setUnitofMeasure.setText("m / °");
                break;
            case 1:
                setUnitofMeasure.setText("m / %");
                break;
            case 2:
                setUnitofMeasure.setText("ft / °");
                break;
            case 3:
                setUnitofMeasure.setText("ft / %");
                break;
            case 4:
                setUnitofMeasure.setText("ft in / °");
                break;
            case 5:
                setUnitofMeasure.setText("ft in / %");
                break;
        }


    }

    private void disableAll() {
        toDig.setEnabled(false);
        back.setEnabled(false);
        toColors.setEnabled(false);
        toChooseCutFill.setEnabled(false);
        toGpsAcc.setEnabled(false);
        alarmH.setEnabled(false);
        viewport.setEnabled(false);
        setDeadbandFlatAngle.setEnabled(false);
        setAudio.setEnabled(false);
        setDeadbandH.setEnabled(false);


    }

    private void enableAll() {
        toDig.setEnabled(true);
        back.setEnabled(true);
        toColors.setEnabled(true);
        toChooseCutFill.setEnabled(true);
        toGpsAcc.setEnabled(true);
        alarmH.setEnabled(true);
        viewport.setEnabled(true);
        setDeadbandFlatAngle.setEnabled(true);
        setAudio.setEnabled(true);
        setDeadbandH.setEnabled(true);

    }

    private void onClick() {

        toGpsAcc.setOnClickListener(view -> {
            if (DataSaved.portView >= 2) {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                    dialogGnssCoordinates.show();
                }
            } else {
                new CustomToast(ExUserSettings.this, "Only 3D").show();
            }
        });
        language.setOnClickListener(view -> {
            if (!dialogLanguages.alertDialog.isShowing()) {
                dialogLanguages.show();
            }

        });
        toDig.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            int profile = MyData.get_Int("ProfileSelected");
            int typeView = MyData.get_Int("indexView");
            if (profile == 0) {
                switch (typeView) {
                    case 0:
                        if(KEY_LEVEL>0) {
                            startActivity(new Intent(this, Digging1D.class));
                            overridePendingTransition(0, 0);
                            finish();
                        }else {
                            enableAll();
                            new CustomToast(this,"LICENSE MISSED").show_alert();
                        }
                        break;
                    case 1:
                        if(KEY_LEVEL>1) {
                            startActivity(new Intent(this, Digging2D.class));
                            overridePendingTransition(0, 0);
                            finish();
                        }else {
                            enableAll();
                            new CustomToast(this,"LICENSE MISSED").show_alert();
                        }
                        break;
                    case 2:
                    case 3:
                        if(KEY_LEVEL>2) {
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
                overridePendingTransition(0, 0);
            }
        });

        back.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, ExcavatorMenuActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        laserOnOff.setOnClickListener(view -> {

            if (DataSaved.laserOn == 0) {
                DataSaved.laserOn = 1;
                MyData.push("laserOn", String.valueOf(DataSaved.laserOn));
            } else if (DataSaved.laserOn == 1) {
                DataSaved.monumentRelease = 0;
                DataSaved.laserOn = 0;
                MyData.push("laserOn", String.valueOf(DataSaved.laserOn));
            }
        });

        dbH.setOnClickListener((View v) -> {
            if (indexUnitOfMeasureSelected == 4 || indexUnitOfMeasureSelected == 5) {
                new CustomToast(this, "Select Feet or Meter to change this value").show();

            } else {
                if (!dialogDeadbandH.dialog.isShowing()) {
                    dialogDeadbandH.show();
                }
            }
        });

        dbFA.setOnClickListener((View v) -> {
            if (!dialogDeadbandFlatAngle.dialog.isShowing()) {
                dialogDeadbandFlatAngle.show();
            }
        });


        alarmH.setOnClickListener((View v) -> {
            if (!dialogHeightAlarm.alertDialog.isShowing()) {
                dialogHeightAlarm.show();
            }
        });


        viewport.setOnClickListener((View v) -> {
            if (!dialogViewport.alertDialog.isShowing()) {
                dialogViewport.show();
            }

        });

        audio.setOnClickListener((View v) -> {
            if (!dialogAudioSystem.alertDialog.isShowing()) {
                dialogAudioSystem.show();
            }

        });

        unit.setOnClickListener((View v) -> {
            if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                dialogUnitOfMeasure.show();
            }
        });

        toColors.setOnClickListener(view -> {
            if (!dialogColors.dialog.isShowing()) {
                dialogColors.show();
            }
        });
        toChooseCutFill.setOnClickListener(view -> {
            if (!dialogInvertColors.dialog.isShowing()) {
                dialogInvertColors.show();
            }

        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}
