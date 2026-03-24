package gui.boot_and_choose;

import static gui.MyApp.DEVICE_SN;
import static gui.MyApp.deu;
import static gui.MyApp.folderPath;
import static gui.MyApp.geoidAll;
import static gui.MyApp.licenseType;
import static gui.MyApp.errorCode;
import static gui.MyApp.listFilesInFolderGeoid;
import static gui.MyApp.numGeoidiInterni;
import static gui.MyApp.updateGeoidFolderFromCloud;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static services.ReadProjectService.numbers;
import static services.UpdateValuesService.firstLaunch;
import static services.UpdateValuesService.startedService;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.MC_1D;
import static utils.MyTypes.MC_2D;
import static utils.MyTypes.MC_3D_EASY;
import static utils.MyTypes.MC_3D_EASY_AUTO;
import static utils.MyTypes.MC_3D_PRO;
import static utils.MyTypes.MC_3D_PRO_AUTO;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import cloud.S3ManagerSingleton;
import drill_pile.gui.PickReport;
import gui.BaseClass;
import gui.dialogs_and_toast.CloseAppDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_Create_New_Prj;
import gui.dialogs_and_toast.Dialog_Drill_GNSS;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.Dialog_InfoApp;
import gui.dialogs_and_toast.Dialog_To_DueDi;
import gui.dialogs_user_settings.Nuova_User_Settings;
import gui.projects.PickProject;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.NativeCzechTransformer;
import services.ReadProjectService;
import services.UpdateValuesService;
import utils.LanguageSetter;
import utils.MyData;
import utils.MyDeviceManager;
import utils.WifiHelper;

public class Activity_Home_Page extends BaseClass {
    public static boolean HasDownloaded;
    ImageView close, toDig, joblist, lock, keyLic, wif, newProj, toDueD, toMachines, toUser, appInfo,testSP;
    CloseAppDialog closeAppDialog;
    ProgressBar progressBar;
    TextView stringsStat, titolo,txt2d;
    DialogPassword dialogPassword;
    Dialog_Create_New_Prj dialogCreateNewPrj;
    Dialog_InfoApp dialogInfoApp;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    Dialog_Drill_GNSS dialogDrillGnss;
    Dialog_To_DueDi dialogToDueDi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            LanguageSetter.setLocale(this, MyData.get_String("language"));
        } catch (Exception e) {
            MyData.push("language", "en_GB");
        }
        setContentView(R.layout.activity_home_page);
        DataSaved.portView=2;
        findView();

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

        scaricaGeoidi();
        provaPROJ();


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
        testSP.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        stringsStat.setVisibility(View.INVISIBLE);
    }

    private void findView() {
        txt2d=findViewById(R.id.textView3);
        closeAppDialog = new CloseAppDialog(this);
        dialogPassword = new DialogPassword(this);
        dialogCreateNewPrj = new Dialog_Create_New_Prj(this);
        dialogInfoApp = new Dialog_InfoApp(this);
        dialogGnssCoordinates=new Dialog_GNSS_Coordinates(this);
        dialogDrillGnss=new Dialog_Drill_GNSS(this);
        dialogToDueDi=new Dialog_To_DueDi(this);
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
        testSP=findViewById(R.id.testSP);
        try {
            String s = MyData.get_String("progettoSelected");
            s = s.replace("/storage/emulated/0/StonexMC_V4", "");
            s = s.substring(0, s.lastIndexOf("/"));
            titolo.setText(s);

        } catch (Exception e) {
            titolo.setText("");
        }


        progressBar.setVisibility(View.INVISIBLE);
        stringsStat.setVisibility(View.INVISIBLE);
        if (licenseType ==MC_1D||licenseType==MC_2D) {
            newProj.setAlpha(0.3f);
            toDig.setAlpha(0.3f);
            joblist.setAlpha(0.3f);

        }else {
            DataSaved.portView=2;
        }
        switch (DataSaved.isWL){
            case EXCAVATOR:
                toDueD.setAlpha(1.0f);
                toDueD.setImageResource(R.drawable.bottone_duedi);
                txt2d.setText("1D  - 2D");
                break;
            case WHEELLOADER:
            case DOZER:
            case GRADER:
            case DOZER_SIX:
                toDueD.setAlpha(0.3f);
                toDueD.setImageResource(R.drawable.bottone_duedi);
                txt2d.setText("1D  - 2D");
                break;
            case DRILL:
                toDueD.setAlpha(1.0f);
                toDueD.setImageResource(R.drawable.bottone_report_prj);
                txt2d.setText("REPORTS");
                break;
        }


    }

    private void onClick() {
        testSP.setOnClickListener(view -> {
            enableAll(false);
            startActivity(new Intent(this, SpTestActivity.class));
            finish();
        });
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
            switch (DataSaved.isWL){
                case EXCAVATOR:
                    if(!dialogToDueDi.dialog.isShowing()){
                        dialogToDueDi.show();
                    }
                    break;

                case DRILL:
                    startActivity(new Intent(this, PickReport.class));
                    finish();
                    break;
                default:

                    break;
            }

        });
        keyLic.setOnClickListener(view -> {
            if(DataSaved.isWL==DRILL){
                if (!dialogDrillGnss.alertDialog.isShowing()) {
                    dialogDrillGnss.show();
                }
            }else {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                    dialogGnssCoordinates.show();
                }
            }
        });
        newProj.setOnClickListener(view -> {
            if (licenseType ==MC_3D_EASY||licenseType==MC_3D_PRO||licenseType==MC_3D_PRO_AUTO) {
                if (!dialogCreateNewPrj.dialog.isShowing()) {
                    dialogCreateNewPrj.show();
                }
            }
        });
        lock.setOnClickListener(view -> {
            if (!isTech) {
                if (!dialogPassword.dialog.isShowing()) {
                    dialogPassword.show(-1);
                }
            }

        });
        joblist.setOnClickListener((View v) -> {
            if (licenseType ==MC_3D_EASY||licenseType==MC_3D_PRO||licenseType==MC_3D_PRO_AUTO||licenseType==MC_3D_EASY_AUTO) {
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


            if (licenseType ==MC_3D_EASY||licenseType==MC_3D_PRO||licenseType==MC_3D_PRO_AUTO||licenseType==MC_3D_EASY_AUTO) {
                enableAll(false);
                    progressBar.setVisibility(View.VISIBLE);
                    stringsStat.setVisibility(View.VISIBLE);
                    startService(new Intent(this, ReadProjectService.class));
            }else {
                new CustomToast(this,"No License").show_alert();
            }

        });
        toUser.setOnClickListener(view -> {
            enableAll(false);
            startActivity(new Intent(this, Nuova_User_Settings.class));
            finish();
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
        testSP.setEnabled(b);
    }

    public void updateUI() {
        try {
            if(isTech){
                testSP.setVisibility(View.VISIBLE);
            }else {
                testSP.setVisibility(View.INVISIBLE);
            }
            if (DataSaved.gpsOk && errorCode == 0) {
                keyLic.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                keyLic.setImageTintList(ColorStateList.valueOf(Color.RED));
            }
            try {
                switch (DataSaved.ConnectionStatus){
                    case 0:
                        wif.setImageResource(R.drawable.network_off);
                        break;

                    case 1:
                        wif.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);
                        break;

                    case 2:
                        wif.setImageResource(R.drawable.sim_96);
                        break;
                    default:
                        wif.setImageResource(R.drawable.network_off);
                        break;
                }

            } catch (Exception e) {
                wif.setBackgroundColor(getColor(R.color.light_yellow));
            }

            if (isTech) {
                lock.setImageResource(R.drawable.unlock);

            } else {
                lock.setImageResource(R.drawable.lock);
            }

            switch (DataSaved.isWL) {
                case EXCAVATOR:
                    stringsStat.setText(ReadProjectService.parserStatus + "\n" + numbers + " New Faces\n");
                    toDig.setImageResource(R.drawable.bottone_scava);
                    break;

                case WHEELLOADER:
                    stringsStat.setText(ReadProjectService.parserStatus + "\n" + numbers + " New Faces\n");
                    toDig.setImageResource(R.drawable.bottone_loada);
                    break;

                case DOZER:
                case DOZER_SIX:
                    stringsStat.setText(ReadProjectService.parserStatus + "\n" + numbers + " New Faces\n");
                    toDig.setImageResource(R.drawable.bottone_grada);
                    break;


                case GRADER:
                    stringsStat.setText(ReadProjectService.parserStatus + "\n" + numbers + " New Faces\n");
                    toDig.setImageResource(R.drawable.bottone_grada);

                    break;
                case DRILL:
                    stringsStat.setText(ReadProjectService.parserStatus);
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
    private void scaricaGeoidi(){

        if(!HasDownloaded) {
            try {
                HasDownloaded=true;
                String pp = Environment.getExternalStorageDirectory().toString() + folderPath + "/Geoids/";
                updateGeoidFolderFromCloud(
                        pp,
                        "serials/" + DEVICE_SN + "/Geoids/"


                );
                Log.d("S3Test", "Manager initialized: " + S3ManagerSingleton.isInitialized());


                geoidAll = listFilesInFolderGeoid(pp);

                // Crea un nuovo array con spazio per i 3 elementi aggiuntivi
                int originalLength = geoidAll != null ? geoidAll.length : 0;
                String[] newGeoidAll = new String[originalLength + numGeoidiInterni];

                // Se l'array originale non è nullo, copialo
                if (geoidAll != null) {
                    System.arraycopy(geoidAll, 0, newGeoidAll, 0, originalLength);
                }

                newGeoidAll[originalLength] = deu;
                geoidAll = newGeoidAll;
            } catch (Exception e) {
                HasDownloaded=false;
                Log.e("S3Test", "Manager initialized: " + e.getMessage());
            }
        }else {
            try {
                String pp = Environment.getExternalStorageDirectory().toString() + folderPath + "/Geoids/";
                geoidAll = listFilesInFolderGeoid(pp);

                // Crea un nuovo array con spazio per i 3 elementi aggiuntivi
                int originalLength = geoidAll != null ? geoidAll.length : 0;
                String[] newGeoidAll = new String[originalLength + numGeoidiInterni];

                // Se l'array originale non è nullo, copialo
                if (geoidAll != null) {
                    System.arraycopy(geoidAll, 0, newGeoidAll, 0, originalLength);
                }
                newGeoidAll[originalLength] = deu;
                geoidAll = newGeoidAll;
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       // MyDeviceManager.UsbDebugUtils.logUsbDevices(this);
       // InputDebugUtils.logInputDevices();

    }

    public void provaPROJ(){
        NativeCzechTransformer transformer = new NativeCzechTransformer();
        try {
            transformer.init(getApplicationContext());
            double lon = 17.304690900;
            double lat = 49.582911992;
            double h = 0.0;

            double[] r5514 = transformer.wgs84To5514(lon, lat, h);
            double[] r5513 = transformer.wgs84To5513(lon, lat, h);

            Log.w("PROJ_TEST", "5514: " + r5514[0] + ", " + r5514[1] + ", " + r5514[2]);
            Log.d("PROJ_TEST", "5513: " + r5513[0] + ", " + r5513[1] + ", " + r5513[2]);


            transformer.close();
        } catch (Exception e) {
            Log.e("PROJ_TEST",Log.getStackTraceString(e));
        }


    }
}