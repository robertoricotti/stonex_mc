package gui.boot_and_choose;

import static gui.MyApp.activationCode;
import static gui.MyApp.folderPath;
import static gui.MyApp.licenseType;
import static gui.MyApp.visibleActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.BuildConfig;
import com.example.stx_dig.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import cloud.WebSocketPlugin;
import drill_pile.gui.Drill_MainPage;
import gui.BaseClass;
import gui.MyApp;
import gui.dialogs_and_toast.CustomToast;
import gui.draw_class.MyColorClass;
import gui.projects.Dialog_Trench;
import gui.projects.LayerAdapter;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import services.UpdateValuesService;
import utils.MyData;
import utils.MyDeviceManager;
import utils.NetworkUtils;


@SuppressLint("CustomSplashScreen")
public class LaunchScreenActivity extends BaseClass {
    private Handler handler = new Handler();
    private int[] images;
    private int currentIndex = 0;
    private ProgressBar pgBar;

    private int progress = 0;
    String[] PERMISSIONS;
    final int PERMISSION_REQUEST_CODE = 1;
    CountDownTimer count;
    String deviceId;
    TextView textView;
    int isAutoStart = 0;
    ImageView animazione;
    public static boolean hasAuto;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen_dig);
        readCode();
        MyDeviceManager.setSize(this);
        WebSocketPlugin.getWebSocketPluginInstance(this).start();
        images = new int[]{R.drawable.img_step_1, R.drawable.img_step_2, R.drawable.img_step_3};
        isAutoStart = 0;
        DataSaved.lastProjectName = "";
        DataSaved.lastProjectNamePOLY = "";
        DataSaved.lastProjectNamePOINT = "";
        MyColorClass.colorTriangle = Color.BLACK;
        pgBar = findViewById(R.id.progressBar);
        animazione = findViewById(R.id.animazione);

        textView = findViewById(R.id.textView5);
        textView.setText("STX MC " + BuildConfig.VERSION_NAME);


        ExcavatorLib.Excavator(new double[100]);
        UpdateValuesService.firstLaunch = false;
        Dialog_Trench.leftW_d = 0.5f;
        Dialog_Trench.rightW_d = 0.5f;
        LayerAdapter.selectA = true;

        if (licenseType == -1) {
            if (NetworkUtils.isInternetAvailable(this)) {
                //cerca dal server
                WebSocketPlugin.getWebSocketPluginInstance(this).start();
            } else {
                readCode();
                //cerca in locale
            }
        }


        count = new CountDownTimer(3000, 1) {
            @Override
            public void onTick(long l) {
                progress++;
                pgBar.setProgress((int) progress++);
            }

            @Override
            public void onFinish() {
                deviceId = Build.BRAND;

                MyData.push("BUILD", deviceId.toString());


                startMe();


            }

            private void startMe() {

                try {
                    MyDeviceManager.hideBar(visibleActivity);

                    if(!activationCode.equals(MyData.get_String("licenza"))){
                        licenseType=-1;
                    }
                    if (licenseType > -1) {
                        createSystemFolders();
                        initColori();
                        startService(new Intent(visibleActivity, UpdateValuesService.class));
                        (new Handler()).postDelayed(LaunchScreenActivity.this::goMain, 3500);

                    } else {
                        new CustomToast(LaunchScreenActivity.this, "LICENSE IS MISSED").show_alert();
                        MyDeviceManager.showBar(visibleActivity);
                        startActivity(new Intent(LaunchScreenActivity.this, LicenseFail_Activity.class));
                        finish();
                    }
                    hasAuto = licenseType == 5;
                } catch (Exception e) {
                    new CustomToast(LaunchScreenActivity.this, "No License CODE").show_error();
                    finishAndRemoveTask();
                    finish();

                }
            }
        };

        checkExternalEnviroment();

        init();
        MyData.push("machinestate", "0");

        startImageSwitch();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void init() {


        if (Build.VERSION.SDK_INT >= 33) {
            PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,


            };

        }
        if (Build.VERSION.SDK_INT < 33 && Build.VERSION.SDK_INT > 29) {
            PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE
            };

        }
        if (Build.VERSION.SDK_INT <= 29) {

            PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE};


        }

        //if (checkPermissions()&&requestOverlayPermission()) {
        if (checkPermissions()) {
            count.start();
            try {

                Toast.makeText(this, "STX MC RUNNING...", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                //
            }

        } else {
            requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkExternalEnviroment() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
        }
    }

    private boolean checkPermissions() {
        requestPermission();
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;

    }

    private void createSystemFolders() {
        // Path della cartella di sistema
        String basePath = Environment.getExternalStorageDirectory().toString() + folderPath;

        // Verifica se esiste una cartella chiamata "Stx_Dig"
        File stxDigDir = new File(Environment.getExternalStorageDirectory().toString() + "/Stx_Dig");
        if (stxDigDir.exists() && stxDigDir.isDirectory()) {
            // Rinomina la cartella "Stx_Dig" nella cartella di destinazione specificata da folderPath
            File newDir = new File(basePath);
            boolean success = stxDigDir.renameTo(newDir);
            if (success) {
                // Se la rinomina è avvenuta con successo, aggiorna folderPath
                new CustomToast(this, "'Stx_MC' folder Renamed in 'StonexMachineControl").show_alert();
            } else {
                new CustomToast(this, "Unable to rename 'Stx_MC' folder please check").show_alert();
            }
        }
        String path = Environment.getExternalStorageDirectory().toString() + folderPath;
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Projects");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines");
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File(path + "/Geoids");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/GNSS FirmWare");
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File(path + "/Localizations");
        if (!directory.exists()) {
            directory.mkdir();
        }


        directory = new File(path + "/Machines/Machine 1");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 2");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 3");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 4");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 1/Config");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 2/Config");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 3/Config");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 4/Config");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 1/Buckets");
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File(path + "/Machines/Machine 1/Buckets Tilt");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 2/Buckets");
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File(path + "/Machines/Machine 2/Buckets Tilt");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 3/Buckets");
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File(path + "/Machines/Machine 3/Buckets Tilt");
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(path + "/Machines/Machine 4/Buckets");
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File(path + "/Machines/Machine 4/Buckets Tilt");
        if (!directory.exists()) {
            directory.mkdir();
        }
        try {
            File f = new File(path + "/As-Built");
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
            //do nothing
        }

    }

    private void requestPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent a = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(a);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                count.start();
            } else {
                try {
                    count.wait();
                } catch (InterruptedException ignored) {
                }
                Toast.makeText(this, "Until you grant the permission, we cannot proceed further", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void goMain() {

        if (DataSaved.enOUT == 1) {
            MyDeviceManager.OUT1(visibleActivity, 0);
        }

        switch (MyApp.licenseType) {
            case 10:
            case 11:
                Intent intent1;
                intent1 = new Intent(this, Drill_MainPage.class);
                startActivity(intent1);
                finish();
                break;

            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                Intent intent;
                intent = new Intent(this, Activity_Home_Page.class);
                startActivity(intent);
                finish();
                break;
        }


    }


    private void initColori() {
        try {
            DataSaved.temaSoftware = MyData.get_Int("Tema_SW");

        } catch (NumberFormatException e) {
            MyData.push("Tema_SW", "2");
            DataSaved.temaSoftware = 2;
        }
        switch (DataSaved.temaSoftware) {
            case 0:
                MyColorClass.colorSfondo = getResources().getColor(R.color.black);
                MyColorClass.colorConstraint = getResources().getColor(R.color.bg_white);
                MyColorClass.colorLabel = ((Color.YELLOW));
                MyColorClass.colorGroundX = (getResources().getColor(R.color.GROUNDred));
                MyColorClass.colorGroundY = (getResources().getColor(R.color.cyan));
                MyColorClass.colorStick = (getResources().getColor(R.color.orange));
                MyColorClass.colorBucket = (getResources().getColor(R.color.orange));
                MyColorClass.colorPoint = (getResources().getColor(R.color.red));
                MyColorClass.colorPoly = (getResources().getColor(R.color.teal_200));

                MyColorClass.colorOffsetLine = (getResources().getColor(R.color.bg_sfsred));
                MyColorClass.colorX_2D = (getResources().getColor(R.color.GROUNDred));
                MyColorClass.colorY_2D = (getResources().getColor(R.color.GROUNDblue));
                // MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                //MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                MyColorClass.utilitiesColor = (Color.parseColor("#FFA500"));
                MyColorClass.jsonColor = (getResources().getColor(R.color.cyan));
                break;


            case 1:
                MyColorClass.colorSfondo = getResources().getColor(R.color.light_gray);
                MyColorClass.colorConstraint = getResources().getColor(R.color.black);
                MyColorClass.colorLabel = (getResources().getColor(R.color.blue));
                MyColorClass.colorGroundX = (getResources().getColor(R.color.GROUNDblue));
                MyColorClass.colorGroundY = (getResources().getColor(R.color.GROUNDblue));
                MyColorClass.colorStick = (getResources().getColor(R.color.orange));
                MyColorClass.colorBucket = (getResources().getColor(R.color.orange));
                MyColorClass.colorPoint = (getResources().getColor(R.color.GROUNDred));
                MyColorClass.colorPoly = (getResources().getColor(R.color._____cancel_text));
                MyColorClass.colorOffsetLine = (getResources().getColor(R.color.bg_sfsred));
                MyColorClass.colorX_2D = (getResources().getColor(R.color.GROUNDred));
                MyColorClass.colorY_2D = (getResources().getColor(R.color.GROUNDblue));
                //MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                //MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                MyColorClass.utilitiesColor = (getResources().getColor(R.color.orange));
                MyColorClass.jsonColor = (getResources().getColor(R.color.red));
                break;
            case 2:
                MyColorClass.colorSfondo = getResources().getColor(R.color.white);
                MyColorClass.colorConstraint = getResources().getColor(R.color.black);
                MyColorClass.colorLabel = (getResources().getColor(R.color.blue));
                MyColorClass.colorGroundX = (getResources().getColor(R.color.GROUNDblue));
                MyColorClass.colorGroundY = (getResources().getColor(R.color.GROUNDblue));
                MyColorClass.colorStick = (getResources().getColor(R.color.orange));
                MyColorClass.colorBucket = (getResources().getColor(R.color.dark_gray));
                MyColorClass.colorPoint = (getResources().getColor(R.color.GROUNDred));
                MyColorClass.colorPoly = (getResources().getColor(R.color.magenta));
                MyColorClass.colorOffsetLine = (getResources().getColor(R.color.bg_sfsred));
                MyColorClass.colorX_2D = (getResources().getColor(R.color.GROUNDred));
                MyColorClass.colorY_2D = (getResources().getColor(R.color.GROUNDblue));
                //MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                MyColorClass.utilitiesColor = (getResources().getColor(R.color.orange));
                MyColorClass.jsonColor = (getResources().getColor(R.color.red));
                break;


            default:
                MyColorClass.colorSfondo = getResources().getColor(R.color.white);
                MyColorClass.colorConstraint = getResources().getColor(R.color.black);
                MyColorClass.colorLabel = (getResources().getColor(R.color.black));
                MyColorClass.colorGroundX = (getResources().getColor(R.color.GROUNDblue));
                MyColorClass.colorGroundY = (getResources().getColor(R.color.GROUNDblue));
                MyColorClass.colorStick = (getResources().getColor(R.color.orange));
                MyColorClass.colorBucket = (getResources().getColor(R.color.orange));
                MyColorClass.colorPoint = (getResources().getColor(R.color.GROUNDred));
                MyColorClass.colorPoly = (getResources().getColor(R.color.magenta));
                MyColorClass.colorOffsetLine = (getResources().getColor(R.color.bg_sfsred));
                MyColorClass.colorX_2D = (getResources().getColor(R.color.GROUNDred));
                MyColorClass.colorY_2D = (getResources().getColor(R.color.GROUNDblue));
                //MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                MyColorClass.utilitiesColor = (getResources().getColor(R.color.teal_700));
                MyColorClass.jsonColor = (getResources().getColor(R.color.red));
                break;


        }
    }


    private void startImageSwitch() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                animazione.setImageResource(images[currentIndex]);
                if (currentIndex < 2) {
                    currentIndex = (currentIndex + 1) % images.length;
                }
                handler.postDelayed(this, 1250); // Ripete ogni 1 secondo
            }
        }, 1250);
    }

    public void readCode() {
        try {
            // Percorso del file
            String pathL = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/License.json";
            File file = new File(pathL);
            try {
                Log.d("Lic: " ,pathL);
            } catch (Exception e) {
                Log.e("Lic: " ,e.getMessage());
            }

            if (!file.exists()) {
                System.out.println("File non trovato: " + pathL);
                return;
            }

            // Leggi contenuto del file
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // Parsing JSON
            JSONObject jsonObject = new JSONObject(sb.toString());

            // Ripopola i campi
            MyApp.activationCode = jsonObject.getString("activationCode");
            MyApp.restoreCode = jsonObject.getString("restoreCode");
            String deviceSN = jsonObject.getString("deviceSN");
            licenseType = jsonObject.getInt("licenseType");
            String userID = jsonObject.getString("userID");
            String category = jsonObject.getString("category");
            long timestamp = jsonObject.getLong("timestamp");
            MyApp.expiry = jsonObject.getString("expiry");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}