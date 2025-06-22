package gui.boot_and_choose;

import static gui.MyApp.KEY_LEVEL;
import static gui.boot_and_choose.ExcavatorMenuActivity.firstLaunch;
import static gui.boot_and_choose.ExcavatorMenuActivity.startedService;
import static gui.dialogs_and_toast.DialogPassword.isTech;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.buckets.BucketChooserActivity;
import gui.debug_ecu.Hydro_Lobby;
import gui.dialogs_and_toast.CloseAppDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_user_settings.ExUserSettings;
import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.DiggingProfile;
import gui.gps.Nuovo_Gps;
import gui.profiles.ProfilesMenuActivity;
import gui.projects.Projects;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
import services.UpdateValuesService;
import utils.MyData;
import utils.MyDeviceManager;
import utils.WifiHelper;

public class Nuova_Choose extends BaseClass {
    boolean hasAuto;
    ImageView key, lock, wifi, gps, can;
    ImageView settings, machines, profile, buckets, ecu, gps_setup, filemanager, info;
    ImageView btn_1, btn_2, btn_3, btn_4;
    CloseAppDialog closeAppDialog;
    DialogPassword dialogPassword;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    ProgressBar progressBar;
    TextView stringsStat;
    int indexMachineSelected;
    //public static boolean firstLaunch, startedService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuova_choose);
        if (!startedService) {
            startService(new Intent(this, UpdateValuesService.class));

        }
        findView();
        dis_en(false);
        init();
        onClick();
        if (!firstLaunch) {
            progressBar.setVisibility(View.VISIBLE);
            (new Handler()).postDelayed(this::enableAll, 5000);
            updateUI();
            firstLaunch = true;
        } else {
            updateUI();
            dis_en(true);
            firstLaunch = true;
            MyDeviceManager.WiFiEnable(this, 1);
        }
        DataSaved.xyz = 1;
        if (KEY_LEVEL == 11 || KEY_LEVEL == 33 || KEY_LEVEL == 34 || KEY_LEVEL == 35 || KEY_LEVEL == 36) {
            hasAuto = true;
        } else {
            hasAuto = false;
        }
    }

    private void findView() {
        key = findViewById(R.id.img_1);
        lock = findViewById(R.id.img_2);
        wifi = findViewById(R.id.img_3);
        gps = findViewById(R.id.img_4);
        can = findViewById(R.id.img_5);
        settings = findViewById(R.id.settings);
        machines = findViewById(R.id.tech_settings);
        profile = findViewById(R.id.profiles);
        buckets = findViewById(R.id.buckets);
        ecu = findViewById(R.id.ecu);
        gps_setup = findViewById(R.id.instrument);
        filemanager = findViewById(R.id.filemanager);
        info = findViewById(R.id.info);
        btn_1 = findViewById(R.id.btn_1);
        btn_2 = findViewById(R.id.btn_2);
        btn_3 = findViewById(R.id.btn_3);
        btn_4 = findViewById(R.id.btn_4);
        progressBar = findViewById(R.id.progressBar);
        stringsStat = findViewById(R.id.stringastat);
    }

    private void init() {
        btn_1.setImageResource(R.drawable.power_off_btn);
        btn_4.setImageResource(R.drawable.go_dig);
        closeAppDialog = new CloseAppDialog(this);
        dialogPassword = new DialogPassword(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        try {
            indexMachineSelected = MyData.get_Int("MachineSelected");
        } catch (NumberFormatException e) {
            indexMachineSelected = 1;
        }
        switch (DataSaved.isWL) {
            case 0:
                machines.setImageResource(R.drawable.machines_btn);
                break;

            case 1:
                machines.setImageResource(R.drawable.wheel_machines_btn);
                break;

            case 2:
            case 3:
                machines.setImageResource(R.drawable.dozer_machines_btn);
                break;
            case 4:
                machines.setImageResource(R.drawable.grader_btn);
                break;

            default:
                machines.setImageResource(R.drawable.machines_btn);
                break;
        }
    }

    private void dis_en(boolean en) {
        key.setEnabled(en);
        lock.setEnabled(en);
        wifi.setEnabled(en);
        gps.setEnabled(en);
        can.setEnabled(en);
        settings.setEnabled(en);
        machines.setEnabled(en);
        profile.setEnabled(en);
        buckets.setEnabled(en);
        ecu.setEnabled(en);
        gps_setup.setEnabled(en);
        filemanager.setEnabled(en);
        info.setEnabled(en);
        btn_1.setEnabled(en);
        btn_2.setEnabled(en);
        btn_3.setEnabled(en);
        btn_4.setEnabled(en);
        if (en) {
            progressBar.setVisibility(View.INVISIBLE);
            stringsStat.setVisibility(View.INVISIBLE);
        }

    }

    public void enableAll() {
        boolean en = true;
        key.setEnabled(en);
        lock.setEnabled(en);
        wifi.setEnabled(en);
        gps.setEnabled(en);
        can.setEnabled(en);
        settings.setEnabled(en);
        machines.setEnabled(en);
        profile.setEnabled(en);
        buckets.setEnabled(en);
        ecu.setEnabled(en);
        gps_setup.setEnabled(en);
        filemanager.setEnabled(en);
        info.setEnabled(en);
        btn_1.setEnabled(en);
        btn_2.setEnabled(en);
        btn_3.setEnabled(en);
        btn_4.setEnabled(en);
        progressBar.setVisibility(View.INVISIBLE);
        stringsStat.setVisibility(View.INVISIBLE);
    }

    private void onClick() {
        settings.setOnClickListener(view -> {
            dis_en(false);
            startActivity(new Intent(getApplicationContext(), ExUserSettings.class));
            overridePendingTransition(0, 0);
            finish();
        });
        buckets.setOnClickListener((View v) -> {

            dis_en(false);
            startActivity(new Intent(this, BucketChooserActivity.class));
            overridePendingTransition(0, 0);
            finish();


        });

        machines.setOnClickListener((View v) -> {

            dis_en(false);
            startActivity(new Intent(this, ExcavatorChooserActivity.class));
            overridePendingTransition(0, 0);
            finish();


        });

        profile.setOnClickListener((View v) -> {
            if (DataSaved.isWL < 1) {
                if (DataSaved.portView < 2) {
                    dis_en(false);
                    startActivity(new Intent(this, ProfilesMenuActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.treddi_acxtive)).show_alert();
                }
            }

        });
        ecu.setOnClickListener(view -> {
            if (hasAuto) {
                if (isTech) {
                    dis_en(false);
                    startActivity(new Intent(this, Hydro_Lobby.class));
                    overridePendingTransition(0, 0);
                    finish();
                } else {
                    String lucchettoChiuso = "\uD83D\uDD12";
                    new CustomToast(Nuova_Choose.this, lucchettoChiuso).show();
                }
            } else {
                new CustomToast(this, "No AUTO License Activated!\nContact a Stonex Dealer").show_alert();
            }
        });


        filemanager.setOnClickListener((View v) -> {

            if (DataSaved.portView >= 2) {
                dis_en(false);
                startActivity(new Intent(this, Projects.class));
                overridePendingTransition(0, 0);
                finish();

            } else {
                new CustomToast(Nuova_Choose.this, getResources().getString(R.string.treddi_off)).show();
            }
        });
        key.setOnClickListener(view -> {
            dis_en(false);
            startActivity(new Intent(this, LicenseActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        wifi.setOnClickListener(view -> {
            try {
                String ssid = WifiHelper.getConnectedSSID(getApplicationContext());
                if (ssid != null) {

                    new CustomToast(Nuova_Choose.this, "Connected to:\n" + ssid.replaceAll("\"", "")).show_alert();
                } else {

                    new CustomToast(Nuova_Choose.this, "NOT CONNECTED").show();
                }

            } catch (Exception e) {
                new CustomToast(Nuova_Choose.this, e.getStackTrace().toString()).show_error();
            }
        });
        gps.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });
        lock.setOnClickListener(view -> {
            if (!isTech) {
                if (!dialogPassword.dialog.isShowing()) {
                    dialogPassword.show();
                }
            }

        });
        gps_setup.setOnClickListener((View v) -> {

            if (isTech) {

                dis_en(false);
                startActivity(new Intent(getApplicationContext(), Nuovo_Gps.class));
                overridePendingTransition(0, 0);
                finish();

            } else {
                String lucchettoChiuso = "\uD83D\uDD12";
                new CustomToast(Nuova_Choose.this, lucchettoChiuso).show();
            }

        });

        btn_1.setOnClickListener((View v) -> {
            if (!closeAppDialog.alertDialog.isShowing()) {
                closeAppDialog.show();
            }

        });
        btn_4.setOnClickListener((View v) -> {

            dis_en(false);
            int profile = MyData.get_Int("ProfileSelected");
            int typeView = MyData.get_Int("indexView");
            if (profile == 0) {
                switch (typeView) {
                    case 0:
                        if (KEY_LEVEL > 0) {
                            startActivity(new Intent(this, Digging1D.class));
                            overridePendingTransition(0, 0);
                            finish();
                        } else {
                            enableAll();
                            new CustomToast(this, "LICENSE MISSED").show_alert();
                        }
                        break;
                    case 1:
                        if (KEY_LEVEL > 1) {
                            startActivity(new Intent(this, Digging2D.class));
                            overridePendingTransition(0, 0);
                            finish();
                        } else {
                            enableAll();
                            new CustomToast(this, "LICENSE MISSED").show_alert();
                        }
                        break;
                    case 2:
                    case 3:
                        if (KEY_LEVEL > 2) {
                            progressBar.setVisibility(View.VISIBLE);
                            stringsStat.setVisibility(View.VISIBLE);
                            startService(new Intent(this, ReadProjectService.class));
                        } else {
                            enableAll();
                            new CustomToast(this, "LICENSE MISSED").show_alert();
                        }
                        break;

                }
            } else {
                startActivity(new Intent(this, DiggingProfile.class));
                overridePendingTransition(0, 0);
                finish();
            }

        });

    }

    public void updateUI() {
        stringsStat.setText(ReadProjectService.parserStatus);
        if (DataSaved.gpsOk) {
            gps.setImageTintList(getColorStateList(R.color.green));
        } else {
            gps.setImageTintList(getColorStateList(R.color.red));
        }
        if (isTech) {
            lock.setImageResource(R.drawable.teck_unlock);
        } else {
            lock.setImageResource(R.drawable.lock);
        }
        try {
            String ssid = WifiHelper.getConnectedSSID(getApplicationContext());
            if (ssid != null) {

                wifi.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);
                wifi.setImageTintList(getColorStateList(R.color.green));
            } else {

                wifi.setImageResource(R.drawable.wifi_vuoto);
                wifi.setImageTintList(getColorStateList(R.color.white));
            }

        } catch (Exception e) {
            wifi.setBackgroundColor(getColor(R.color.light_yellow));
        }
        if (DataSaved.portView < 2) {
            filemanager.setAlpha(0.3f);

        } else {
            filemanager.setAlpha(1.0f);
        }

    }
}