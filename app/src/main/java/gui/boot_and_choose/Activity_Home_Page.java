package gui.boot_and_choose;

import static gui.MyApp.KEY_LEVEL;
import static gui.MyApp.errorCode;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static services.ReadProjectService.numbers;
import static services.UpdateValuesService.firstLaunch;
import static services.UpdateValuesService.startedService;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CloseAppDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_Create_New_Prj;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.Dialog_InfoApp;
import gui.projects.PickProject;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
import services.UpdateValuesService;
import utils.LanguageSetter;
import utils.MyData;
import utils.MyDeviceManager;
import utils.WifiHelper;

public class Activity_Home_Page extends AppCompatActivity {
    ImageView close, toDig, joblist, lock, keyLic, wif, newProj, toDueD, toMachines, toUser, appInfo;
    CloseAppDialog closeAppDialog;
    ProgressBar progressBar;
    TextView stringsStat, titolo;
    DialogPassword dialogPassword;
    Dialog_Create_New_Prj dialogCreateNewPrj;
    Dialog_InfoApp dialogInfoApp;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        findView();
        try {
            LanguageSetter.setLocale(this, MyData.get_String("language"));
        } catch (Exception e) {
            MyData.push("language", "en_GB");
        }
        if (!startedService) {
            startService(new Intent(this, UpdateValuesService.class));

        }
        LanguageSetter.setLocale(this, MyData.get_String("language"));
        enableAll(false);
        if (!firstLaunch) {
            progressBar.setVisibility(View.VISIBLE);
            (new Handler()).postDelayed(this::enableAll, 5000);
            updateUI();
            firstLaunch = true;
        } else {
            updateUI();
            enableAll(true);
            firstLaunch = true;
            MyDeviceManager.WiFiEnable(this, 1);
        }

        onClick();


    }

    private void enableAll() {
        close.setEnabled(true);
        toDig.setEnabled(true);
        toDig.setEnabled(true);
        titolo.setEnabled(true);
        joblist.setEnabled(true);
        lock.setEnabled(true);
        keyLic.setEnabled(true);
        newProj.setEnabled(true);
        toDueD.setEnabled(true);
        toMachines.setEnabled(true);
        toUser.setEnabled(true);
        appInfo.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        stringsStat.setVisibility(View.INVISIBLE);
    }

    private void findView() {
        closeAppDialog = new CloseAppDialog(this);
        dialogPassword = new DialogPassword(this);
        dialogCreateNewPrj = new Dialog_Create_New_Prj(this);
        dialogInfoApp = new Dialog_InfoApp(this);
        dialogGnssCoordinates=new Dialog_GNSS_Coordinates(this);
        close = findViewById(R.id.btn_1);
        progressBar = findViewById(R.id.progressBar);
        stringsStat = findViewById(R.id.stringastat);
        toDig = findViewById(R.id.img_7);
        titolo = findViewById(R.id.txtProject);
        joblist = findViewById(R.id.img_6);
        lock = findViewById(R.id.btn_2);
        keyLic = findViewById(R.id.img00);
        wif = findViewById(R.id.img01);
        toDueD = findViewById(R.id.img_3);
        toMachines = findViewById(R.id.img_2);
        toUser = findViewById(R.id.img_1);
        newProj = findViewById(R.id.img_4);
        appInfo = findViewById(R.id.btn_3);
        try {
            String s = MyData.get_String("progettoSelected");
            s = s.replace("/storage/emulated/0/StonexMachineControl", "");
            s = s.substring(0, s.lastIndexOf("/"));
            titolo.setText(s);
        } catch (Exception e) {
            titolo.setText(" ");
        }


        progressBar.setVisibility(View.INVISIBLE);
        stringsStat.setVisibility(View.INVISIBLE);
        if (KEY_LEVEL < 3) {
            newProj.setAlpha(0.3f);
            toDig.setAlpha(0.3f);
            joblist.setAlpha(0.3f);
        }
        if (DataSaved.isWL > 0) {
            toDueD.setAlpha(0.3f);
        }

    }

    private void onClick() {
        toMachines.setOnClickListener(view -> {
            enableAll(false);
            startActivity(new Intent(this, ExcavatorChooserActivity.class));
            finish();
        });
        appInfo.setOnClickListener(view -> {
            if (!dialogInfoApp.dialog.isShowing()) {
                dialogInfoApp.show();
            }
        });
        toDueD.setOnClickListener(view -> {
            if (DataSaved.isWL > 0) {
                new CustomToast(this, " ").show_alert();
            } else {
                //TODO apri 2D
            }
        });
        keyLic.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()){
                dialogGnssCoordinates.show();
            }
        });
        newProj.setOnClickListener(view -> {
            if (KEY_LEVEL > 2) {
                if (!dialogCreateNewPrj.dialog.isShowing()) {
                    dialogCreateNewPrj.show();
                }
            }
        });
        lock.setOnClickListener(view -> {
            if (!isTech) {
                if (!dialogPassword.dialog.isShowing()) {
                    dialogPassword.show();
                }
            }

        });
        joblist.setOnClickListener((View v) -> {
            if (KEY_LEVEL > 2) {
                enableAll(false);
                startActivity(new Intent(this, PickProject.class));
                finish();
            }
        });
        close.setOnClickListener(view -> {
            if (!closeAppDialog.alertDialog.isShowing()) {
                closeAppDialog.show();
            }
        });

        toDig.setOnClickListener((View v) -> {


            if (KEY_LEVEL > 2) {
                enableAll(false);
                progressBar.setVisibility(View.VISIBLE);
                stringsStat.setVisibility(View.VISIBLE);
                startService(new Intent(this, ReadProjectService.class));
            }

        });
    }

    private void enableAll(boolean b) {
        close.setEnabled(b);
        toDig.setEnabled(b);
        toDig.setEnabled(b);
        titolo.setEnabled(b);
        joblist.setEnabled(b);
        lock.setEnabled(b);
        keyLic.setEnabled(b);
        newProj.setEnabled(b);
        toDueD.setEnabled(b);
        toUser.setEnabled(b);
        toMachines.setEnabled(b);
        appInfo.setEnabled(b);
    }

    public void updateUI() {
        try {
            if (DataSaved.gpsOk && errorCode == 0) {
                keyLic.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                keyLic.setImageTintList(ColorStateList.valueOf(Color.RED));
            }
            try {
                String ssid = WifiHelper.getConnectedSSID(getApplicationContext());
                if (ssid != null) {

                    wif.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);

                } else {

                    wif.setImageResource(R.drawable.wifi_vuoto);

                }
            } catch (Exception e) {
                wif.setImageResource(R.drawable.wifi_off_96);
            }

            if (isTech) {
                lock.setImageResource(R.drawable.unlock);

            } else {
                lock.setImageResource(R.drawable.lock);
            }
            stringsStat.setText(ReadProjectService.parserStatus + "\n" + numbers + " Rows\n");
            switch (DataSaved.isWL) {
                case 0:
                    toDig.setImageResource(R.drawable.bottone_scava);
                    break;

                case 1:
                    toDig.setImageResource(R.drawable.bottone_loada);
                    break;

                case 2:
                case 3:
                    toDig.setImageResource(R.drawable.bottone_grada);
                    break;


                case 4:
                    toDig.setImageResource(R.drawable.bottone_grada);

                    break;
                case 10:
                    toDig.setImageResource(R.drawable.bottone_drilla);

                    break;

            }
        } catch (Exception ignored) {

        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}