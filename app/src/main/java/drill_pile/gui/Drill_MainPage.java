package drill_pile.gui;



import static gui.dialogs_and_toast.DialogPassword.isTech;
import static services.UpdateValuesService.firstLaunch;
import static services.UpdateValuesService.startedService;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.BuildConfig;
import com.example.stx_dig.R;

import gui.BaseClass;
import gui.debug_ecu.Can_Msg_Debug;
import gui.dialogs_and_toast.CloseAppDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_SSID;
import gui.dialogs_user_settings.Dialog_QR;
import gui.gps.Nuovo_Gps;
import packexcalib.exca.DataSaved;
import services.UpdateValuesService;
import utils.LanguageSetter;
import utils.MyData;
import utils.MyDeviceManager;
import utils.WifiHelper;

public class Drill_MainPage extends BaseClass {
    Dialog_QR dialogQr;
    CloseAppDialog closeAppDialog;
    ImageView userSettings, buckets, machines, toEcu, gnss_setup, projects, keyLic, wifi;
    public ImageView toDig, toMain, toDebug, toEBubble, help, lock;
    TextView info, mSSID;
    ProgressBar progressBar;
    TextView stringsStat, selectBuck, profik, filek;
    LinearLayout layoutRemote;
    Dialog_SSID dialogSsid;

    int indexMachineSelected;
    DialogPassword dialogPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_main_page);
        try {
            LanguageSetter.setLocale(this, MyData.get_String("language"));
        } catch (Exception e) {
            MyData.push("language", "en_GB");
        }

        if (!startedService) {
            startService(new Intent(this, UpdateValuesService.class));

        }
        LanguageSetter.setLocale(this, MyData.get_String("language"));
        findView();
        disableAll();
        init();
        onClick();
        if (!firstLaunch) {
            progressBar.setVisibility(View.VISIBLE);
            (new Handler()).postDelayed(this::enableAll, 5000);
            updateUI();
            firstLaunch = true;
        } else {
            updateUI();
            enableAll();
            firstLaunch = true;
            MyDeviceManager.WiFiEnable(this, 1);
        }


    }

    private void disableAll() {
        userSettings.setEnabled(false);
        buckets.setEnabled(false);
        machines.setEnabled(false);
        toEcu.setEnabled(false);
        gnss_setup.setEnabled(false);
        projects.setEnabled(false);
        toDig.setEnabled(false);
        toMain.setEnabled(false);
        toDebug.setEnabled(false);
        toEBubble.setEnabled(false);
        help.setEnabled(false);
        lock.setEnabled(false);
        keyLic.setEnabled(false);
        wifi.setEnabled(false);
    }

    private void enableAll() {
        userSettings.setEnabled(true);
        buckets.setEnabled(true);
        machines.setEnabled(true);
        toEcu.setEnabled(true);
        gnss_setup.setEnabled(true);
        projects.setEnabled(true);
        toDig.setEnabled(true);
        toMain.setEnabled(true);
        toDebug.setEnabled(true);
        toEBubble.setEnabled(true);
        help.setEnabled(true);
        lock.setEnabled(true);
        keyLic.setEnabled(true);
        wifi.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        stringsStat.setVisibility(View.INVISIBLE);
    }

    private void findView() {
        toMain = findViewById(R.id.toMain);
        buckets = findViewById(R.id.buckets);
        machines = findViewById(R.id.machines);
        toEcu = findViewById(R.id.toEcu);
        toDig = findViewById(R.id.pair);
        gnss_setup = findViewById(R.id.gps_setup);
        projects = findViewById(R.id.projects);
        userSettings = findViewById(R.id.settings);
        toDebug = findViewById(R.id.debugPage);
        toEBubble = findViewById(R.id.to_ebubble);
        projects.setAlpha(1.0f);
        gnss_setup.setAlpha(1.0f);
        info = findViewById(R.id.infoApp);
        help = findViewById(R.id.helpCall);
        lock = findViewById(R.id.empty);
        progressBar = findViewById(R.id.progressBar);
        keyLic = findViewById(R.id.updateCode);
        stringsStat = findViewById(R.id.stringastat);
        selectBuck = findViewById(R.id.selectBuck);
        profik = findViewById(R.id.profik);
        filek = findViewById(R.id.filek);
        wifi = findViewById(R.id.wifi);
        mSSID = findViewById(R.id.txssid);
        layoutRemote = findViewById(R.id.layout_remote);


    }

    private void init() {
        dialogSsid = new Dialog_SSID(this);
        closeAppDialog = new CloseAppDialog(Drill_MainPage.this);
        progressBar.setVisibility(View.INVISIBLE);
        stringsStat.setVisibility(View.INVISIBLE);
        dialogPassword = new DialogPassword(this);
        dialogQr = new Dialog_QR(this);
        MyData.push("MachineSelected","1");
        try {
            indexMachineSelected = MyData.get_Int("MachineSelected");
        } catch (NumberFormatException e) {
            indexMachineSelected = 1;
        }

        info.setText("STX MC\n" + BuildConfig.VERSION_NAME);


    }

    public void onClick() {
        buckets.setOnClickListener(view -> {
            Intent intent=new Intent(this, Can_Msg_Debug.class);
            intent.putExtra("chi","drill_main");
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        gnss_setup.setOnClickListener((View v) -> {
            if (isTech) {
                disableAll();
                startActivity(new Intent(getApplicationContext(), Nuovo_Gps.class));//passare parametri
                overridePendingTransition(0, 0);
                finish();

            } else {
                String lucchettoChiuso = "\uD83D\uDD12";
                new CustomToast(Drill_MainPage.this, lucchettoChiuso).show();
            }

        });
        keyLic.setOnClickListener(view -> {

        });


        lock.setOnClickListener(view -> {
            if (!isTech) {
                if (!dialogPassword.dialog.isShowing()) {
                    dialogPassword.show(-1);
                }
            }
        });
        help.setOnClickListener(view -> {
            if (!dialogQr.dialog.isShowing()) {
                dialogQr.show();
            }
        });
        toMain.setOnClickListener((View v) -> {
            if (!closeAppDialog.alertDialog.isShowing()) {
                closeAppDialog.show();
            }

        });
        toEcu.setOnClickListener(view -> {
            if (isTech) {
            startActivity(new Intent(this,Ecu_Sensors_Activity.class));
            overridePendingTransition(0,0);
            finish();
            } else {
                String lucchettoChiuso = "\uD83D\uDD12";
                new CustomToast(Drill_MainPage.this, lucchettoChiuso).show();
            }
        });
    }

    public void updateUI() {
        //J2VF77_B7XCCR_EXGY97
        if (isTech) {
            lock.setImageResource(R.drawable.unlock);

        } else {
            lock.setImageResource(R.drawable.lock);
        }
        try {
            switch (DataSaved.ConnectionStatus){
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
    }
}