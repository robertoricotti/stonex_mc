package gui.boot_and_choose;


import static gui.MyApp.KEY_LEVEL;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static services.ReadProjectService.numbers;
import static services.UpdateValuesService.firstLaunch;
import static services.UpdateValuesService.startedService;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.BuildConfig;
import com.example.stx_dig.R;

import gui.buckets.BucketChooserActivity;
import gui.debug_ecu.DebugExcavatorActivity;
import gui.debug_ecu.Hydro_Lobby;
import gui.dialogs_and_toast.CloseAppDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_Edit_Coordinate_Demo;
import gui.dialogs_and_toast.Dialog_SSID;
import gui.dialogs_user_settings.Dialog_QR;
import gui.dialogs_user_settings.ExUserSettings;
import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.DiggingProfile;
import gui.gps.Nuovo_Gps;
import gui.profiles.ProfilesMenuActivity;
import gui.projects.Projects;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.Deg2UTM;
import packexcalib.gnss.UTM2Deg;
import services.ReadProjectService;
import services.UpdateValuesService;
import utils.LanguageSetter;
import utils.MyData;
import utils.MyDeviceManager;
import utils.WifiHelper;

public class ExcavatorMenuActivity extends AppCompatActivity {

    Dialog_Edit_Coordinate_Demo dialogEditCoordinateDemo;
    Dialog_QR dialogQr;
    CloseAppDialog closeAppDialog;
    ImageView userSettings, buckets, machines, profiles, gnss_setup, projects, keyLic, wifi;
    public ImageView toDig, toMain, toDebug, toEBubble, help, lock;
    TextView titolo, info, mSSID;
    ProgressBar progressBar;
    TextView stringsStat, selectBuck, profik, filek;
    LinearLayout layoutRemote;
    Dialog_SSID dialogSsid;
    boolean hasAuto = false;


    int indexMachineSelected;


    DialogPassword dialogPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            LanguageSetter.setLocale(this, MyData.get_String("language"));
        } catch (Exception e) {
            MyData.push("language", "en_GB");
        }
        setContentView(R.layout.activity_dig_menu);
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


        if (KEY_LEVEL == 11 || KEY_LEVEL == 33 || KEY_LEVEL == 34 || KEY_LEVEL == 35 || KEY_LEVEL == 36) {
            hasAuto = true;
        } else {
            hasAuto = false;
        }
    }

    private void disableAll() {
        userSettings.setEnabled(false);
        buckets.setEnabled(false);
        machines.setEnabled(false);
        profiles.setEnabled(false);
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
        profiles.setEnabled(true);
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
        titolo = findViewById(R.id.titolo);
        toMain = findViewById(R.id.toMain);
        buckets = findViewById(R.id.buckets);
        machines = findViewById(R.id.machines);
        profiles = findViewById(R.id.toEcu);
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
        closeAppDialog = new CloseAppDialog(ExcavatorMenuActivity.this);
        dialogEditCoordinateDemo = new Dialog_Edit_Coordinate_Demo(this);
        progressBar.setVisibility(View.INVISIBLE);
        stringsStat.setVisibility(View.INVISIBLE);
        dialogPassword = new DialogPassword(this);
        dialogQr = new Dialog_QR(this);


        try {
            indexMachineSelected = MyData.get_Int("MachineSelected");
        } catch (NumberFormatException e) {
            indexMachineSelected = 1;
        }

        titolo.setText(MyData.get_String("M" + indexMachineSelected + "_Name"));
        info.setText("STX MC\n" + BuildConfig.VERSION_NAME);
        //info.setTextColor(Color.WHITE);
        //info.setBackgroundColor(Color.RED);


    }

    public void updateUI() {
        try {
/*
            Deg2UTM deg2UTM2 = new Deg2UTM(38.051143275, 23.739465975, 150.162, DataSaved.S_CRS);
            double N = deg2UTM2.getNorthing();
            double E = deg2UTM2.getEasting();
            double Q = deg2UTM2.getQuota();
            Log.d("TestGre", DataSaved.S_CRS + "  " + N + "  " + E + "  " + Q);
            UTM2Deg utm2Deg = new UTM2Deg(0, 'N', E, N, Q, DataSaved.S_CRS);
           // Log.d("Grecia", "TO DEG:" + utm2Deg.getLatitude() + " " + utm2Deg.getLongitude());
            */

            String ssid = WifiHelper.getConnectedSSID(getApplicationContext());
            if (ssid != null) {

                wifi.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);
                mSSID.setText(ssid.replaceAll("\"", ""));
            } else {

                wifi.setImageResource(R.drawable.wifi_vuoto);
                mSSID.setText("DISCONNECTED");
            }

        } catch (Exception e) {
            wifi.setBackgroundColor(getColor(R.color.light_yellow));
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


        stringsStat.setText(ReadProjectService.parserStatus + "\n" + numbers + " Rows\n");
        if (DataSaved.is40 == 0) {
            layoutRemote.setVisibility(View.INVISIBLE);

        } else {
            layoutRemote.setVisibility(View.INVISIBLE);
            // layoutRemote.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.blink_500));
        }


        if (isTech) {
            lock.setImageResource(R.drawable.unlock);

        } else {
            lock.setImageResource(R.drawable.lock);
        }
        if (DataSaved.portView < 2) {
            projects.setAlpha(0.3f);
            filek.setAlpha(0.3f);
        } else {
            projects.setAlpha(1.0f);
            filek.setAlpha(1.0f);
        }

        switch (DataSaved.isWL) {
            case 0:
                toDig.setImageResource(R.drawable.go_dig);

                buckets.setAlpha(1f);
                selectBuck.setAlpha(1f);
                profiles.setImageResource(R.drawable.profiles_btn);
                profik.setText(getResources().getString(R.string.profiles));
                break;
            case 1:
                toDig.setImageResource(R.drawable.go_dig);

                buckets.setAlpha(1f);
                selectBuck.setAlpha(1f);
                profiles.setImageResource(R.drawable.btn_ecu);
                profik.setText("ECU SETUP");
                if (!hasAuto) {
                    profiles.setAlpha(0.3f);
                    profik.setAlpha(0.3f);
                } else {
                    profiles.setAlpha(1f);
                    profik.setAlpha(1f);
                }
                break;
            case 2:
            case 3:

                toDig.setImageResource(R.drawable.go_grade);

                buckets.setAlpha(0.2f);
                selectBuck.setAlpha(0.2f);
                profiles.setImageResource(R.drawable.btn_ecu);
                profik.setText("ECU SETUP");
                if (!hasAuto) {
                    profiles.setAlpha(0.3f);
                    profik.setAlpha(0.3f);
                } else {
                    profiles.setAlpha(1f);
                    profik.setAlpha(1f);
                }
                break;
            case 4:
                toDig.setImageResource(R.drawable.go_grade);

                buckets.setAlpha(0.2f);
                selectBuck.setAlpha(0.2f);
                profiles.setImageResource(R.drawable.btn_ecu);
                profik.setText("ECU SETUP");
                if (!hasAuto) {
                    profiles.setAlpha(0.3f);
                    profik.setAlpha(0.3f);
                } else {
                    profiles.setAlpha(1f);
                    profik.setAlpha(1f);
                }
                break;
            default:
                toDig.setImageResource(R.drawable.go_dig);
                break;
        }
        numbers++;
    }


    private void onClick() {

        wifi.setOnClickListener(view -> {

        });
        info.setOnClickListener(view -> {


        });
        keyLic.setOnClickListener(view -> {

            disableAll();
            startActivity(new Intent(getApplicationContext(), LicenseActivity.class));
            finish();


        });


        lock.setClickable(false);
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
        toDebug.setOnClickListener(view -> {

            if (true) {
                disableAll();
                startActivity(new Intent(this, DebugExcavatorActivity.class));
                finish();
            }
        });
        buckets.setOnClickListener((View v) -> {

            if (DataSaved.isWL == 0) {
                disableAll();
                startActivity(new Intent(this, BucketChooserActivity.class));
                finish();
            }


        });

        machines.setOnClickListener((View v) -> {

            if (true) {
                disableAll();
                startActivity(new Intent(this, ExcavatorChooserActivity.class));
                finish();
            }

        });

        profiles.setOnClickListener((View v) -> {
            if (DataSaved.isWL < 1) {
                if (DataSaved.portView < 2) {
                    disableAll();
                    startActivity(new Intent(this, ProfilesMenuActivity.class));
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.treddi_acxtive)).show_alert();
                }
            } else {
                if (hasAuto) {
                    if (isTech) {
                        disableAll();
                        startActivity(new Intent(this, Hydro_Lobby.class));
                        finish();
                    } else {
                        String lucchettoChiuso = "\uD83D\uDD12";
                        new CustomToast(ExcavatorMenuActivity.this, lucchettoChiuso).show();
                    }
                } else {
                    new CustomToast(this, "No AUTO License Activated!\nContact a Stonex Dealer").show_alert();
                }
            }

        });


        projects.setOnClickListener((View v) -> {

            if (DataSaved.portView >= 2) {
                disableAll();
                startActivity(new Intent(this, Projects.class));
                finish();

            } else {
                new CustomToast(ExcavatorMenuActivity.this, getResources().getString(R.string.treddi_off)).show();
            }
        });
        toEBubble.setOnClickListener(view -> {

            if (true) {
                /*
                if (DataSaved.portView >= 2) {
                    new CustomToast(this, "Enable 1D or 2D").show();
                } else {
                    disableAll();
                    Intent intent = new Intent(this, E_Bubble.class);
                    intent.putExtra("who", "E1D");
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }

                 */


            }

        });

        gnss_setup.setOnClickListener((View v) -> {

            if (isTech) {

                disableAll();
                startActivity(new Intent(getApplicationContext(), Nuovo_Gps.class));
                finish();

            } else {
                String lucchettoChiuso = "\uD83D\uDD12";
                new CustomToast(ExcavatorMenuActivity.this, lucchettoChiuso).show();
            }

        });

        userSettings.setOnClickListener((View v) -> {

            if (true) {
                disableAll();
                startActivity(new Intent(getApplicationContext(), ExUserSettings.class));
                finish();
            }

        });

        toDig.setOnClickListener((View v) -> {
            disableAll();
            int profile = MyData.get_Int("ProfileSelected");
            int typeView = MyData.get_Int("indexView");
            if (profile == 0) {
                switch (typeView) {
                    case 0:
                        if (KEY_LEVEL > 0) {
                            startActivity(new Intent(this, Digging1D.class));
                            finish();
                        } else {
                            enableAll();
                            new CustomToast(this, "LICENSE MISSED").show_alert();
                        }
                        break;
                    case 1:
                        if (KEY_LEVEL > 1) {
                            startActivity(new Intent(this, Digging2D.class));
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
                            startService(new Intent(ExcavatorMenuActivity.this, ReadProjectService.class));
                        } else {
                            enableAll();
                            new CustomToast(this, "LICENSE MISSED").show_alert();
                        }
                        break;

                }
            } else {
                startActivity(new Intent(this, DiggingProfile.class));
                finish();
            }

        });

        toMain.setOnClickListener((View v) -> {

            if (true) {
                if (!closeAppDialog.alertDialog.isShowing()) {
                    closeAppDialog.show();
                }
            } else {
                String lucchettoChiuso = "\uD83D\uDD12";
                new CustomToast(ExcavatorMenuActivity.this, lucchettoChiuso).show();
            }
        });

    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

   /* private void testMethod(){

        byte[] pgn1=new byte[]{17, (byte) 210,48, (byte) 189, (byte) 171, (byte) 255, (byte) 255, (byte) 255};
        long res= PLC_DataTypes_LittleEndian.byte_to_S64_le(pgn1);
        double rseD= (double) res /1000;
        Log.d("resD",res+"");
        Log.d("resD",rseD+"");

        long value=-26600469528597L;
        byte[]ar= PLC_DataTypes_LittleEndian.S64_to_bytes(value);
        Log.d("redD", Arrays.toString(ar));

    }*/


}

