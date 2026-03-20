package gui.tech_menu;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import java.util.List;

import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.GgaLimiterPickerDialog;
import gui.dialogs_and_toast.MountpointPickerDialog;
import packexcalib.gnss.NtripClient;
import utils.MyData;

public class NetworkConfigSettings extends AppCompatActivity {

    private static final String TAG = NetworkConfigSettings.class.getSimpleName();

    // =========================
    // Views
    // =========================
    private ProgressBar progressBar;

    private ImageView networkStatus;
    private ImageView btnBack, btnSave;

    private TextView selWifi;
    private TextView selSIM;
    private TextView selNtrip;
    private TextView selGnssType;
    private TextView selNmeaRate;

    private LinearLayout wifiMenu;
    private LinearLayout simMenu;
    private LinearLayout ntripMenu;
    private LinearLayout gnssTypeMenu;
    private LinearLayout nmeaRateMenu;
    private LinearLayout ntripCredentials;

    private EditText hostIp;
    private EditText port;
    private EditText mountPoint;
    private EditText ggaLimiter;
    private EditText username;
    private EditText password;

    private Button btnWifiSettings;
    private Button btnSimSettings;
    private Button btnChooseMountpoint;
    private Button btnGetUploadGGA;

    private RadioButton rInternalGnss;
    private RadioButton rDemo;
    private RadioButton rNtripEnable;
    private RadioButton rNtripDisable;
    private RadioButton rNmeaRate1;
    private RadioButton rNmeaRate5;
    private RadioButton rNmeaRate10;
    private RadioButton rNmeaRate20;

    // =========================
    // System / Helpers
    // =========================
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private MountpointPickerDialog mountpointPickerDialog;
    private GgaLimiterPickerDialog ggaLimiterPickerDialog;

    // =========================
    // State
    // =========================
    private boolean ntripToggleSetup = false;
    private boolean isFetchingMountpoints = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_config_settings);

        bindViews();
        initManagersAndDialogs();
        setupMenus();
        setupButtons();
        setupGnssSelection();
        initializeUiState();
    }

    // =========================================================
    // Initialization
    // =========================================================

    private void bindViews() {
        progressBar = findViewById(R.id.progressBar);

        networkStatus = findViewById(R.id.img00);

        selWifi = findViewById(R.id.sel_wifi);
        selSIM = findViewById(R.id.sel_sim);
        selNtrip = findViewById(R.id.sel_ntrip);
        selGnssType = findViewById(R.id.sel_gnss_type);
        selNmeaRate = findViewById(R.id.sel_nmea_rate);

        wifiMenu = findViewById(R.id.wifi_menu);
        simMenu = findViewById(R.id.sim_menu);
        ntripMenu = findViewById(R.id.ntrip_menu);
        gnssTypeMenu = findViewById(R.id.gnss_type_menu);
        nmeaRateMenu = findViewById(R.id.nmea_rate_menu);
        ntripCredentials = findViewById(R.id.ntrip_credentials_wrapper);


        rInternalGnss = findViewById(R.id.internal_gnss);
        rDemo = findViewById(R.id.demo_gnss);
        rNtripEnable = findViewById(R.id.ntrip_enable);
        rNtripDisable = findViewById(R.id.ntrip_disable);
        rNmeaRate1 = findViewById(R.id.nmea_rate_1);
        rNmeaRate5 = findViewById(R.id.nmea_rate_5);
        rNmeaRate10 = findViewById(R.id.nmea_rate_10);
        rNmeaRate20 = findViewById(R.id.nmea_rate_20);


        hostIp = findViewById(R.id.ntrip_host_ip);
        port = findViewById(R.id.ntrip_port);
        mountPoint = findViewById(R.id.ntrip_mountpoint);
        ggaLimiter = findViewById(R.id.ntrip_gga);
        username = findViewById(R.id.ntrip_username);
        password = findViewById(R.id.ntrip_password);

        btnBack = findViewById(R.id.btn_1);
        btnSave = findViewById(R.id.btn_2);

        btnWifiSettings = findViewById(R.id.btn_wifi_settings);
        btnSimSettings = findViewById(R.id.btn_sim_settings);
        btnChooseMountpoint = findViewById(R.id.btn_get_mountpoint);
        btnGetUploadGGA = findViewById(R.id.btn_get_upload_gga);
    }

    private void initManagersAndDialogs() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mountpointPickerDialog = new MountpointPickerDialog();
        ggaLimiterPickerDialog = new GgaLimiterPickerDialog();


    }

    private void setupMenus() {
        setupMenuToggle(selWifi, wifiMenu);
        setupMenuToggle(selSIM, simMenu);
        setupMenuToggle(selGnssType, gnssTypeMenu);
        setupMenuToggle(selNmeaRate, nmeaRateMenu);
        // NTRIP viene configurato dinamicamente solo quando GNSS = Internal
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> navigateBackToHome());
        btnWifiSettings.setOnClickListener(v -> openWifiSettings());
        btnSimSettings.setOnClickListener(v -> openSimSettings());
        btnChooseMountpoint.setOnClickListener(v -> fetchMountpoints());
        btnGetUploadGGA.setOnClickListener(v -> openGgaLimiterDialog());

        btnSave.setOnClickListener(v -> save());

        rNtripEnable.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                ntripCredentials.setVisibility(View.VISIBLE);
            }
        });

        rNtripDisable.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                ntripCredentials.setVisibility(View.GONE);
            }
        });
    }

    private void setupGnssSelection() {
        View.OnClickListener gnssClickListener = v -> {
            boolean internalSelected = rInternalGnss != null && rInternalGnss.isChecked();
            setNtripVisible(internalSelected);
        };

        if (rInternalGnss != null) rInternalGnss.setOnClickListener(gnssClickListener);
        if (rDemo != null) rDemo.setOnClickListener(gnssClickListener);
    }

    private void initializeUiState() {
        progressBar.setVisibility(View.GONE);

        closeAllMenus();

        ConnectionType type = getConnectionType(this);
        logConnection(type);
        updateNetworkIcon(type);

        // ======================
        // GNSS TYPE
        // ======================
        String gnssType = MyData.get_String("gnssType");
        boolean internal = !"DEMO".equalsIgnoreCase(gnssType);
        applyInitialGnssState(internal);

        // ======================
        // NTRIP ENABLE
        // ======================
        String ntripEnabled = MyData.get_String("ntripEnabled");
        boolean enabled = "ENABLED".equalsIgnoreCase(ntripEnabled);

        rNtripEnable.setChecked(enabled);
        rNtripDisable.setChecked(!enabled);

        ntripCredentials.setVisibility(enabled ? View.VISIBLE : View.GONE);

        // ======================
        // NTRIP FIELDS
        // ======================
        hostIp.setText(safeGet("ntripHostIp"));
        port.setText(safeGet("ntripPort"));
        mountPoint.setText(safeGet("ntripMountPoint"));
        ggaLimiter.setText(safeGet("ntripGgaLimiter"));
        username.setText(safeGet("ntripUsername"));
        password.setText(safeGet("ntripPassword"));

        // ======================
        // NMEA RATE
        // ======================
        String nmeaRate = MyData.get_String("nmeaRate");

        if ("1".equals(nmeaRate)) {
            rNmeaRate1.setChecked(true);
        } else if ("5".equals(nmeaRate)) {
            rNmeaRate5.setChecked(true);
        } else if ("10".equals(nmeaRate)) {
            rNmeaRate10.setChecked(true);
        } else if ("20".equals(nmeaRate)) {
            rNmeaRate20.setChecked(true);
        } else {
            rNmeaRate1.setChecked(true);
        }
    }

    // =========================================================
    // Lifecycle
    // =========================================================

    @Override
    protected void onStart() {
        super.onStart();
        registerNetworkCallback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterNetworkCallback();
    }

    // =========================================================
    // Network callback
    // =========================================================

    private void registerNetworkCallback() {
        if (connectivityManager == null) return;

        if (networkCallback == null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    runOnUiThread(NetworkConfigSettings.this::refreshNetworkState);
                }

                @Override
                public void onLost(Network network) {
                    runOnUiThread(NetworkConfigSettings.this::refreshNetworkState);
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    runOnUiThread(NetworkConfigSettings.this::refreshNetworkState);
                }
            };
        }

        try {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } catch (Exception e) {
            Log.e(TAG, "registerDefaultNetworkCallback error", e);
            refreshNetworkState();
        }
    }

    private void unregisterNetworkCallback() {
        if (connectivityManager == null || networkCallback == null) return;

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception e) {
            Log.w(TAG, "unregisterNetworkCallback error", e);
        }
    }

    private void refreshNetworkState() {
        ConnectionType type = getConnectionType(this);
        logConnection(type);
        updateNetworkIcon(type);
    }

    // =========================================================
    // Navigation / Settings
    // =========================================================

    private void navigateBackToHome() {
        Intent intent = new Intent(this, ExcavatorChooserActivity.class);
        startActivity(intent);
        finish();
    }

    private void openWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void openSimSettings() {
        Intent intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // =========================================================
    // GNSS / NTRIP visibility
    // =========================================================

    private void applyInitialGnssState(boolean internal) {
        if (rInternalGnss != null) rInternalGnss.setChecked(internal);
        if (rDemo != null) rDemo.setChecked(!internal);

        setNtripVisible(internal);
    }

    private void setNtripVisible(boolean visible) {
        if (visible) {
            selNtrip.setVisibility(View.VISIBLE);
            selNtrip.setEnabled(true);

            if (!ntripToggleSetup) {
                setupMenuToggle(selNtrip, ntripMenu);
                ntripToggleSetup = true;
            }
        } else {
            ntripMenu.setVisibility(View.GONE);
            selNtrip.setVisibility(View.GONE);
            selNtrip.setEnabled(false);
            selNtrip.setBackground(ContextCompat.getDrawable(this, R.drawable.sfondo_bottone_trasparente));

            // forza NTRIP disabilitato quando GNSS = DEMO
            rNtripDisable.setChecked(true);
            rNtripEnable.setChecked(false);
            ntripCredentials.setVisibility(View.GONE);
        }
    }

    // =========================================================
    // Menu handling
    // =========================================================

    private void setupMenuToggle(TextView selectedTab, LinearLayout selectedMenu) {
        selectedTab.setOnClickListener(v -> {
            if (selectedMenu.getVisibility() == View.VISIBLE) return;

            closeAllMenusKeepNtripVisibility();

            selectedMenu.setVisibility(View.VISIBLE);
            selectedTab.setBackground(
                    ContextCompat.getDrawable(this, R.drawable.sfondo_bottone_selezionato)
            );
        });
    }

    private void closeAllMenus() {
        closeAllMenusKeepNtripVisibility();
        selNtrip.setVisibility(View.GONE);
        selNtrip.setEnabled(false);
    }

    private void closeAllMenusKeepNtripVisibility() {
        wifiMenu.setVisibility(View.GONE);
        simMenu.setVisibility(View.GONE);
        gnssTypeMenu.setVisibility(View.GONE);
        nmeaRateMenu.setVisibility(View.GONE);
        ntripMenu.setVisibility(View.GONE);

        resetMenuBackgrounds();
    }

    private void resetMenuBackgrounds() {
        selWifi.setBackground(ContextCompat.getDrawable(this, R.drawable.sfondo_bottone_trasparente));
        selSIM.setBackground(ContextCompat.getDrawable(this, R.drawable.sfondo_bottone_trasparente));
        selGnssType.setBackground(ContextCompat.getDrawable(this, R.drawable.sfondo_bottone_trasparente));
        selNmeaRate.setBackground(ContextCompat.getDrawable(this, R.drawable.sfondo_bottone_trasparente));
        selNtrip.setBackground(ContextCompat.getDrawable(this, R.drawable.sfondo_bottone_trasparente));
    }

    // =========================================================
    // NTRIP actions
    // =========================================================

    private void fetchMountpoints() {
        if (isFetchingMountpoints) return;

        String host = hostIp.getText().toString().trim();
        String portText = port.getText().toString().trim();

        if (host.isEmpty() || portText.isEmpty()) {
            Toast.makeText(this, "Host IP and Port are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int parsedPort;
        try {
            parsedPort = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid port", Toast.LENGTH_SHORT).show();
            return;
        }

        isFetchingMountpoints = true;

        progressBar.setVisibility(View.VISIBLE);
        setUiEnabled(false);

        new NtripClient().fetchMountpointsOnce(host, parsedPort, new NtripClient.MountpointsCallback() {
            @Override
            public void onSuccess(List<String> mountpoints) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    setUiEnabled(true);
                    isFetchingMountpoints = false;
                    openMountpointDialog(mountpoints);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    setUiEnabled(true);
                    isFetchingMountpoints = false;
                    Toast.makeText(NetworkConfigSettings.this, "Error fetching mountpoints", Toast.LENGTH_SHORT).show();
                });

            }
        });
    }

    private void openMountpointDialog(List<String> mountpoints) {
        if (mountpoints == null || mountpoints.isEmpty()) {
            Log.w(TAG, "No mountpoints found");
            Toast.makeText(this, "No mountpoints found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mountpointPickerDialog.isShowing()) return;

        mountpointPickerDialog.show(
                this,
                mountpoints,
                new MountpointPickerDialog.Callback() {
                    @Override
                    public void onSelected(String selectedMountpoint) {
                        Log.d(TAG, "Selected mountpoint: " + selectedMountpoint);
                        mountPoint.setText(selectedMountpoint);
                    }

                    @Override
                    public void onCancelled() {
                        Log.d(TAG, "Mountpoint selection cancelled");
                        mountPoint.setText("");
                    }
                }
        );
    }

    private void openGgaLimiterDialog() {
        if (ggaLimiterPickerDialog.isShowing()) return;

        ggaLimiterPickerDialog.show(
                this,
                new GgaLimiterPickerDialog.Callback() {
                    @Override
                    public void onSelected(int valueSec, String label) {
                        ggaLimiter.setText(label);
                    }

                    @Override
                    public void onCancelled() {
                        // no-op
                    }
                }
        );
    }

    // =========================================================
    // Save
    // =========================================================

    private void save() {
        boolean isInternalGnss = rInternalGnss.isChecked();
        boolean isNtripEnabled = rNtripEnable.isChecked();

        String hostText = hostIp.getText().toString().trim();
        String portText = port.getText().toString().trim();
        String mountpointText = mountPoint.getText().toString().trim();
        String ggaLimiterText = ggaLimiter.getText().toString().trim();
        String usernameText = username.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        String selectedNmeaRate = "1";
        if (rNmeaRate5.isChecked()) {
            selectedNmeaRate = "5";
        } else if (rNmeaRate10.isChecked()) {
            selectedNmeaRate = "10";
        } else if (rNmeaRate20.isChecked()) {
            selectedNmeaRate = "20";
        }

        if (isNtripEnabled) {
            if (hostText.isEmpty() ||
                    portText.isEmpty() ||
                    mountpointText.isEmpty() ||
                    ggaLimiterText.isEmpty() ||
                    usernameText.isEmpty() ||
                    passwordText.isEmpty()) {

                Toast.makeText(this, "All NTRIP fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int parsedPort = Integer.parseInt(portText);
                if (parsedPort < 1 || parsedPort > 65535) {
                    Toast.makeText(this, "Invalid port", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid port", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        MyData.push("gnssType", isInternalGnss ? "INTERNAL" : "DEMO");
        MyData.push("ntripEnabled", isNtripEnabled ? "ENABLED" : "DISABLED");
        MyData.push("nmeaRate", selectedNmeaRate);

        if (isNtripEnabled) {
            MyData.push("ntripHostIp", hostText);
            MyData.push("ntripPort", portText);
            MyData.push("ntripMountPoint", mountpointText);
            MyData.push("ntripGgaLimiter", ggaLimiterText);
            MyData.push("ntripUsername", usernameText);
            MyData.push("ntripPassword", passwordText);
        }

        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();

    }

    // =========================================================
    // Network UI
    // =========================================================

    private void logConnection(ConnectionType type) {
        switch (type) {
            case WIFI:
                Log.d("NETWORK", "Connesso tramite WIFI");
                break;
            case MOBILE:
                Log.d("NETWORK", "Connesso tramite DATI MOBILI");
                break;
            case NONE:
            default:
                Log.d("NETWORK", "Nessuna connessione");
                break;
        }
    }

    private void updateNetworkIcon(ConnectionType type) {
        switch (type) {
            case WIFI:
                networkStatus.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);
                break;

            case MOBILE:
                networkStatus.setImageResource(R.drawable.sim_96);
                break;

            case NONE:
            default:
                networkStatus.setImageResource(R.drawable.network_off);
                break;
        }

        networkStatus.setVisibility(View.VISIBLE);
    }

    // =========================================================
    // Utilities
    // =========================================================

    public enum ConnectionType {
        WIFI,
        MOBILE,
        NONE
    }

    public static ConnectionType getConnectionType(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return ConnectionType.NONE;

        Network network = cm.getActiveNetwork();
        if (network == null) return ConnectionType.NONE;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        if (capabilities == null) return ConnectionType.NONE;

        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return ConnectionType.WIFI;
        }

        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return ConnectionType.MOBILE;
        }

        return ConnectionType.NONE;
    }

    private String safeGet(String key) {
        String value = MyData.get_String(key);
        return value != null ? value : "";
    }

    private void setUiEnabled(boolean enabled) {

        selWifi.setEnabled(enabled);
        selSIM.setEnabled(enabled);
        selNtrip.setEnabled(enabled);
        selGnssType.setEnabled(enabled);
        selNmeaRate.setEnabled(enabled);

        hostIp.setEnabled(enabled);
        port.setEnabled(enabled);
        mountPoint.setEnabled(enabled);
        ggaLimiter.setEnabled(enabled);
        username.setEnabled(enabled);
        password.setEnabled(enabled);

        btnWifiSettings.setEnabled(enabled);
        btnSimSettings.setEnabled(enabled);
        btnChooseMountpoint.setEnabled(enabled);
        btnGetUploadGGA.setEnabled(enabled);

        rInternalGnss.setEnabled(enabled);
        rDemo.setEnabled(enabled);
        rNtripEnable.setEnabled(enabled);
        rNtripDisable.setEnabled(enabled);

        rNmeaRate1.setEnabled(enabled);
        rNmeaRate5.setEnabled(enabled);
        rNmeaRate10.setEnabled(enabled);
        rNmeaRate20.setEnabled(enabled);

        btnBack.setEnabled(enabled);
        btnSave.setEnabled(enabled);
    }
}