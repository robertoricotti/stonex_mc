package gui.dialogs_and_toast;

import static packexcalib.gnss.CRS_Strings._NONE;
import static utils.CanFileTransfer.sendFileViaCAN;
import static utils.CanFileTransfer.sendFileViaSerial;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import gui.MyApp;
import gui.projects.ProjectFileAdapter;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.MyGeoide;
import serial.SerialPortManager;
import services.ReadProjectService;
import utils.CanFileTransfer;
import utils.FullscreenActivity;
import utils.MyData;
import utils.MyDeviceManager;

public class Diaalog_Set_SP {
    CheckBox geo1, geo2, geo3, geo4, geo5,geo6;
    CustomQwertyDialog customQwertyDialog;
    String mTesto;
    int perc = 0;
    Activity activity;
    public Dialog dialog;
    Button dismiss;
    TextView messaggio;
    ImageView usaSP, startSearch;
    RecyclerView recyclerViewSP;
    ArrayList<ProjectFileAdapter.FileItem> arrayFiles, arraySP;
    EditText cercaSP;
    ProjectFileAdapter spAdapter;
    ProgressBar progressBar;
    boolean isUpdating = false;
    private Handler handler;
    ArrayAdapter<String> adapter;
    List<String> nazioni;
    Spinner spinner;
    String selectedFolder;
    TextView inUso;




    public Diaalog_Set_SP(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    public void show() {
        if (MyData.get_String("usaGeoide") == null) {
            MyData.push("usaGeoide", String.valueOf(false));
        }

        dialog.create();
        dialog.setContentView(R.layout.dialog_sp_folders);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        customQwertyDialog = new CustomQwertyDialog(activity);
        findView();
        init();
        onClick();
        startUpdating();

    }

    public void findView() {
        cercaSP = dialog.findViewById(R.id.cercaSP);
        startSearch = dialog.findViewById(R.id.startSerach);
        dismiss = dialog.findViewById(R.id.dismiss);
        recyclerViewSP = dialog.findViewById(R.id.recycler_viewSP);
        usaSP = dialog.findViewById(R.id.usaSP);
        progressBar = dialog.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        messaggio = dialog.findViewById(R.id.msg);
        spinner = dialog.findViewById(R.id.spinner);
        inUso = dialog.findViewById(R.id.inUso);
        geo1 = dialog.findViewById(R.id.geo1);
        geo2 = dialog.findViewById(R.id.geo2);
        geo3 = dialog.findViewById(R.id.geo3);
        geo4 = dialog.findViewById(R.id.geo4);
        geo5 = dialog.findViewById(R.id.geo5);
        geo6=dialog.findViewById(R.id.geo6);

    }

    public void init() {
        checkGeo(MyData.get_String("geoidPath"));
        arrayFiles = new ArrayList<>();
        arraySP = new ArrayList<>();
        spAdapter = new ProjectFileAdapter(arraySP);
        recyclerViewSP.setAdapter(spAdapter);
        recyclerViewSP.setLayoutManager(new LinearLayoutManager(activity));
        recyclerViewSP.setItemViewCacheSize(spAdapter.getItemCount());
        nazioni = getNomiCartelleDaAssets();
        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, nazioni);
        adapter.setDropDownViewResource(R.layout.layout_custom_spinner_3);
        String s = "";
        s = MyData.get_String("LastNation");

        if (s == null) {
            spinner.setAdapter(adapter);
        } else {
            adapter.insert(s, 0);
            spinner.setAdapter(adapter);
        }
        String s1 = "";
        s1 = MyData.get_String("LastSP");
        if (s1 == null) {
            inUso.setText("");
        } else {
            inUso.setText(s1);
        }


    }

    public void onClick() {
        geo1.setOnClickListener(view -> {
            if (geo1.isChecked()) {
                MyData.push("usaGeoide", String.valueOf(true));
                MyData.push("geoidPath", MyApp.geoidFilePath_USA2018);
            } else {
                MyData.push("usaGeoide", String.valueOf(false));
                MyData.push("geoidPath", null);
            }
            checkGeo(MyData.get_String("geoidPath"));
        });
        geo2.setOnClickListener(view -> {
            if (geo2.isChecked()) {
                MyData.push("usaGeoide", String.valueOf(true));
                MyData.push("geoidPath", MyApp.geoidFilePath_USA2012);
            } else {
                MyData.push("usaGeoide", String.valueOf(false));
                MyData.push("geoidPath", null);
            }
            checkGeo(MyData.get_String("geoidPath"));
        });
        geo3.setOnClickListener(view -> {
            if (geo3.isChecked()) {
                MyData.push("usaGeoide", String.valueOf(true));
                MyData.push("geoidPath", MyApp.geoidFilePath_NL);
            } else {
                MyData.push("usaGeoide", String.valueOf(false));
                MyData.push("geoidPath", null);
            }
            checkGeo(MyData.get_String("geoidPath"));

        });
        geo4.setOnClickListener(view -> {
            if (geo4.isChecked()) {
                MyData.push("usaGeoide", String.valueOf(true));
                MyData.push("geoidPath", MyApp.geoidFilePath_BG);
            } else {
                MyData.push("usaGeoide", String.valueOf(false));
                MyData.push("geoidPath", null);
            }
            checkGeo(MyData.get_String("geoidPath"));

        });
        geo5.setOnClickListener(view -> {

            if (geo5.isChecked()) {
                MyData.push("usaGeoide", String.valueOf(true));
                MyData.push("geoidPath", MyApp.geoidFilePath_GR);
            } else {
                MyData.push("usaGeoide", String.valueOf(false));
                MyData.push("geoidPath", null);
            }
            checkGeo(MyData.get_String("geoidPath"));

        });
        geo6.setOnClickListener(view -> {

            if (geo6.isChecked()) {
                MyData.push("usaGeoide", String.valueOf(true));
                MyData.push("geoidPath", MyApp.geoidFilePath_DEU);
            } else {
                MyData.push("usaGeoide", String.valueOf(false));
                MyData.push("geoidPath", null);
            }
            checkGeo(MyData.get_String("geoidPath"));

        });
        cercaSP.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(cercaSP);
            }
        });
        usaSP.setOnClickListener(view -> {

            if (spAdapter.getSelectedItem() == -1) {
                new CustomToast(activity, "SELECT A SP FILE TO USE").show();
            } else {

                // Supponiamo che "selectedFileName" sia il nome del file selezionato ottenuto dallo spinner
                String selectedFileName = arraySP.get(spAdapter.getSelectedItem()).getName();
                String folderPath = selectedFolder; // La cartella selezionata

                Handler handler1 = new Handler(activity.getMainLooper());
                handler1.postDelayed(() -> {
                    try {
                        MyData.push("LastSP", selectedFileName);
                        inUso.setText(selectedFileName);

                        // Invia il file tramite CAN
                        switch (selectedFileName) {
                            //FINLAND
                            case "Finland_ETRS89-GK19FIN_3873.SP":
                                MyData.push("crs", "3873");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-20FIN_3874.SP":
                                MyData.push("crs", "3874");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-21FIN_3875.SP":
                                MyData.push("crs", "3875");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-22FIN_3876.SP":
                                MyData.push("crs", "3876");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-23FIN_3877.SP":
                                MyData.push("crs", "3877");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-24FIN_3878.SP":
                                MyData.push("crs", "3878");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-25FIN_3879.SP":
                                MyData.push("crs", "3879");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-26FIN_3880.SP":
                                MyData.push("crs", "3880");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-27FIN_3881.SP":
                                MyData.push("crs", "3881");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-28FIN_3882.SP":
                                MyData.push("crs", "3882");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-29FIN_3883.SP":
                                MyData.push("crs", "3883");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-30FIN_3884.SP":
                                MyData.push("crs", "3884");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89GK-31FIN_3885.SP":
                                MyData.push("crs", "3885");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Finland_ETRS89TM-35FIN(EN)_3067.SP":
                                MyData.push("crs", "3067");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            //ICELAND
                            case "Iceland_ISN93_3057.SP":
                                MyData.push("crs", "3057");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            //FRANCE
                            case "France_RGF93CC42_3942.SP":
                                MyData.push("crs", "3942");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "France_RGF93CC43_3943.SP":
                                MyData.push("crs", "3943");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "France_RGF93CC44_3944.SP":
                                MyData.push("crs", "3944");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "France_RGF93CC45_3945.SP":
                                MyData.push("crs", "3945");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "France_RGF93CC46_3946.SP":
                                MyData.push("crs", "3946");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "France_RGF93CC47_3947.SP":
                                MyData.push("crs", "3947");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "France_RGF93CC48_3948.SP":
                                MyData.push("crs", "3948");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "France_RGF93CC49_3949.SP":
                                MyData.push("crs", "3949");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "France_RGF93CC50_3950.SP":
                                MyData.push("crs", "3950");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;


                            ///
                            //AUSTRALIA
                            case "Australia_GDA2020-MGA-Zone48_7848.SP":
                                MyData.push("crs", "7848");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone49_7849.SP":
                                MyData.push("crs", "7849");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone50_7850.SP":
                                MyData.push("crs", "7850");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone51_7851.SP":
                                MyData.push("crs", "7851");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone52_7852.SP":
                                MyData.push("crs", "7852");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone53_7853.SP":
                                MyData.push("crs", "7853");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone54_7854.SP":
                                MyData.push("crs", "7854");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone55_7855.SP":
                                MyData.push("crs", "7855");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone56_7856.SP":
                                MyData.push("crs", "7856");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone57_7857.SP":
                                MyData.push("crs", "7857");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Australia_GDA2020-MGA-Zone58_7858.SP":
                                MyData.push("crs", "7858");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            //CANADA
                            case "CANADA__102_W_TO_96_W__NAD83_CSRS__UTM_ZONE_14N__3158.SP":
                                MyData.push("crs", "3158");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__108_W_TO_102_W__NAD83_CSRS__UTM_ZONE_13N__2957.SP":
                                MyData.push("crs", "2957");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__108_W_TO_102_W__SOUTH_OF_60_N__NAD83_CSRS98__UTM_ZONE_13N__2151.SP":
                                MyData.push("crs", "2151");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__114_W_TO_108_W__NAD83_CSRS__UTM_ZONE_12N__2956.SP":
                                MyData.push("crs", "2956");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__114_W_TO_108_W__SOUTH_OF_60_N__NAD83_CSRS98__UTM_ZONE_12N__2152.SP":
                                MyData.push("crs", "2152");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__120_W_TO_114_W__NAD83_CSRS__UTM_ZONE_11N__2955.SP":
                                MyData.push("crs", "2955");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__120_W_TO_114_W__SOUTH_OF_60_N__NAD83_CSRS98__UTM_ZONE_11N__2153.SP":
                                MyData.push("crs", "2153");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__126_W_TO_120_W__NAD83_CSRS__UTM_ZONE_10N__3157.SP":
                                MyData.push("crs", "3157");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__132_W_TO_126_W__NAD83_CSRS__UTM_ZONE_9N__3156.SP":
                                MyData.push("crs", "3156");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__138_W_TO_132_W__NAD83_CSRS__UTM_ZONE_8N__3155.SP":
                                MyData.push("crs", "3155");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__144_W_TO_138_W__NAD83_CSRS__UTM_ZONE_7N__3154.SP":
                                MyData.push("crs", "3154");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__48_W_TO_42_W__NAD83__UTM_ZONE_23N__26923.SP":
                                MyData.push("crs", "26923");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__54_W_TO_48_W__NAD27__UTM_ZONE_22N__26722.SP":
                                MyData.push("crs", "26722");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__54_W_TO_48_W__NAD83_CSRS__UTM_ZONE_22N__3761.SP":
                                MyData.push("crs", "3761");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__54_W_TO_48_W__NAD83__UTM_ZONE_22N__26922.SP":
                                MyData.push("crs", "26922");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__60_W_TO_54_W_AND_NAD27__NAD27__UTM_ZONE_21N__26721.SP":
                                MyData.push("crs", "26721");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__60_W_TO_54_W__NAD83_CSRS__UTM_ZONE_21N__2962.SP":
                                MyData.push("crs", "2962");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__60_W_TO_54_W__NAD83__UTM_ZONE_21N__26921.SP":
                                MyData.push("crs", "26921");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__66_W_TO_60_W__NAD83_CSRS__UTM_ZONE_20N__2961.SP":
                                MyData.push("crs", "2961");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__66_W_TO_60_W__SOUTH_OF_60_N__NAD83_CSRS98__UTM_ZONE_20N__2038.SP":
                                MyData.push("crs", "2038");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__72_W_TO_66_W__NAD83_CSRS__UTM_ZONE_19N__2960.SP":
                                MyData.push("crs", "2960");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__72_W_TO_66_W__SOUTH_OF_62_N__NAD83_CSRS98__UTM_ZONE_19N__2037.SP":
                                MyData.push("crs", "2037");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__78_W_TO_72_W__NAD83_CSRS__UTM_ZONE_18N__2959.SP":
                                MyData.push("crs", "2959");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__84_W_TO_78_W__NAD83_CSRS__UTM_ZONE_17N__2958.SP":
                                MyData.push("crs", "2958");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__90_W_TO_84_W__NAD83_CSRS__UTM_ZONE_16N__3160.SP":
                                MyData.push("crs", "3160");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__96_W_TO_90_W__NAD83_CSRS__UTM_ZONE_15N__3159.SP":
                                MyData.push("crs", "3159");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__ALBERTA__115_5_W_TO_112_5_W__NAD27__ALBERTA_3TM_REF_MERID_114_W__3772.SP":
                                MyData.push("crs", "3772");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__ALBERTA__115_5_W_TO_112_5_W__NAD83_CSRS__ALBERTA_3TM_REF_MERID_114_W__3780.SP":
                                MyData.push("crs", "3780");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__ALBERTA__115_5_W_TO_112_5_W__NAD83__ALBERTA_3TM_REF_MERID_114_W__3776.SP":
                                MyData.push("crs", "3776");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__ALBERTA__118_5_W_TO_115_5_W__NAD27__ALBERTA_3TM_REF_MERID_117_W__3773.SP":
                                MyData.push("crs", "3773");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__ALBERTA__118_5_W_TO_115_5_W__NAD83_CSRS__ALBERTA_3TM_REF_MERID_117_W__3781.SP":
                                MyData.push("crs", "3781");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__ALBERTA__118_5_W_TO_115_5_W__NAD83__ALBERTA_3TM_REF_MERID_117_W__3777.SP":
                                MyData.push("crs", "3777");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__ALBERTA__EAST_OF_112_5_W__NAD27__ALBERTA_3TM_REF_MERID_111_W__3771.SP":
                                MyData.push("crs", "3771");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__ALBERTA__EAST_OF_112_5_W__NAD83_CSRS__ALBERTA_3TM_REF_MERID_111_W__3779.SP":
                                MyData.push("crs", "3779");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__ALBERTA__EAST_OF_112_5_W__NAD83__ALBERTA_3TM_REF_MERID_111_W__3775.SP":
                                MyData.push("crs", "3775");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "CANADA__YUKON__NAD83__YUKON_ALBERS__3578.SP":
                                MyData.push("crs", "3578");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                                //
                            case "Canada_NAD83(CSRS)-MTM-Nova Scotia-Zone4_8082.SP":
                                MyData.push("crs", "8082");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "Canada_NAD83(CSRS)-MTM-Nova Scotia-Zone5_8083.SP":
                                MyData.push("crs", "8083");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            //
                            //GERMANY
                            case "GERMANY__WEST_OF_6_E__ETRS89__UTM_ZONE_31N__N_ZE__5651.SP":
                                MyData.push("crs", "5651");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "GERMANY__WEST_OF_6_E__ETRS89__UTM_ZONE_31N__ZE_N__5649.SP":
                                MyData.push("crs", "5649");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "GERMANY__6_E_TO_12_E__ETRS89__UTM_ZONE_32N__ZE_N__4647.SP":
                                MyData.push("crs", "4647");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "GERMANY__6_E_TO_12_E__ETRS89__UTM_ZONE_32N__N_ZE__5652.SP":
                                MyData.push("crs", "5652");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "GERMANY__EAST_OF_12_E__ETRS89__UTM_ZONE_33N__N_ZE__5653.SP":
                                MyData.push("crs", "5653");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "GERMANY__EAST_OF_12_E__ETRS89__UTM_ZONE_33N__ZE_N__5650.SP":
                                MyData.push("crs", "5660");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            //g2012ba0
                            case "USA__ALASKA__144_W_TO_141_W__NAD83_2011__ALASKA_ZONE_2__6395.SP":
                                MyData.push("crs", "6395");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALASKA__148_W_TO_144_W__NAD83_2011__ALASKA_ZONE_3__6396.SP":
                                MyData.push("crs", "6396");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALASKA__152_W_TO_148_W__NAD83_2011__ALASKA_ZONE_4__6397.SP":
                                MyData.push("crs", "6397");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALASKA__160_W_TO_156_W__NAD83_2011__ALASKA_ZONE_6__6399.SP":
                                MyData.push("crs", "6399");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALASKA__156_W_TO_152_W__NAD83_2011__ALASKA_ZONE_5__6398.SP":
                                MyData.push("crs", "6398");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALASKA__PANHANDLE__NAD83_2011__ALASKA_ZONE_1__6394.SP":
                                MyData.push("crs", "6394");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALASKA__164_W_TO_160_W__NAD83_2011__ALASKA_ZONE_7__6400.SP":
                                MyData.push("crs", "6400");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALASKA__NORTH_OF_54_5_N__168_W_TO_164_W__NAD83_2011__ALASKA_ZONE_8__6401.SP":
                                MyData.push("crs", "6401");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALASKA__NORTH_OF_54_5_N__WEST_OF_168_W__NAD83_2011__ALASKA_ZONE_9__6402.SP":
                                MyData.push("crs", "6402");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALASKA__ALEUTIAN_ISLANDS__NAD83_2011__ALASKA_ZONE_10__6403.SP":
                                MyData.push("crs", "6403");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            /*
                             *
                             */

                            //g2018u0
                            case "USA__ALABAMA__SPCS__E__NAD83_HARN__ALABAMA_EAST__2759.SP":
                                MyData.push("crs", "2759");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ALABAMA__SPCS__W__NAD83_HARN__ALABAMA_WEST__2760.SP":
                                MyData.push("crs", "2760");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ARIZONA__SPCS__E__NAD83__ARIZONA_EAST__26948.SP":
                                MyData.push("crs", "26948");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ARIZONA__SPCS__C__NAD83__ARIZONA_CENTRAL__26949.SP":
                                MyData.push("crs", "26949");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ARIZONA__SPCS__W__NAD83__ARIZONA_WEST__26950.SP":
                                MyData.push("crs", "26950");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ARKANSAS__SPCS__N__NAD83__ARKANSAS_NORTH__26951.SP":
                                MyData.push("crs", "26951");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ARKANSAS__SPCS__S__NAD83__ARKANSAS_SOUTH__26952.SP":
                                MyData.push("crs", "26952");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__CALIFORNIA__SPCS__1__NAD83__CALIFORNIA_ZONE_1__26941.SP":
                                MyData.push("crs", "26941");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__CALIFORNIA__SPCS__2__NAD83__CALIFORNIA_ZONE_2__26942.SP":
                                MyData.push("crs", "26942");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__CALIFORNIA__SPCS__3__NAD83__CALIFORNIA_ZONE_3__26943.SP":
                                MyData.push("crs", "26943");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__CALIFORNIA__SPCS__4__NAD83__CALIFORNIA_ZONE_4__26944.SP":
                                MyData.push("crs", "26944");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__CALIFORNIA__SPCS83__5__NAD83__CALIFORNIA_ZONE_5__26945.SP":
                                MyData.push("crs", "26945");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__CALIFORNIA__SPCS__6__NAD83__CALIFORNIA_ZONE_6__26946.SP":
                                MyData.push("crs", "26946");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__TEXAS__SPCS__N__NAD83__TEXAS_NORTH__32137.SP":
                                MyData.push("crs", "32137");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__TEXAS__SPCS__NC__NAD83__TEXAS_NORTH_CENTRAL__32138.SP":
                                MyData.push("crs", "32138");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__TEXAS__SPCS__C__NAD83__TEXAS_CENTRAL__32139.SP":
                                MyData.push("crs", "32139");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__TEXAS__SPCS83__SC__NAD83__TEXAS_SOUTH_CENTRAL__32140.SP":
                                MyData.push("crs", "32140");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__TEXAS__SPCS83__S__NAD83__TEXAS_SOUTH__32141.SP":
                                MyData.push("crs", "32141");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WISCONSIN__SPCS__N__NAD83__WISCONSIN_NORTH__32152.SP":
                                MyData.push("crs", "32152");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            case "USA__WISCONSIN__SPCS__C__NAD83__WISCONSIN_CENTRAL__32153.SP":
                                MyData.push("crs", "32153");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WISCONSIN__SPCS__S__NAD83__WISCONSIN_SOUTH__32154.SP":
                                MyData.push("crs", "32154");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WYOMING__SPCS__E__NAD83__WYOMING_EAST__32155.SP":
                                MyData.push("crs", "32155");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WYOMING__SPCS__EC__NAD83__WYOMING_EAST_CENTRAL__32156.SP":
                                MyData.push("crs", "32156");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WYOMING__SPCS__WC__NAD83__WYOMING_WEST_CENTRAL__32157.SP":
                                MyData.push("crs", "32157");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WYOMING__SPCS__W__NAD83__WYOMING_WEST__32158.SP":
                                MyData.push("crs", "32158");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__UTAH__SPCS__N__NAD83__UTAH_NORTH__32142.SP":
                                MyData.push("crs", "32142");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__UTAH__SPCS__C__NAD83__UTAH_CENTRAL__32143.SP":
                                MyData.push("crs", "32143");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__UTAH__SPCS__S__NAD83__UTAH_SOUTH__32144.SP":
                                MyData.push("crs", "32144");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__VERMONT__NAD83__VERMONT__32145.SP":
                                MyData.push("crs", "32145");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__VIRGINIA__SPCS__N__NAD83__VIRGINIA_NORTH__32146.SP":
                                MyData.push("crs", "32146");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__VIRGINIA__SPCS__S__NAD83__VIRGINIA_SOUTH__32147.SP":
                                MyData.push("crs", "32147");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WASHINGTON__SPCS83__N__NAD83__WASHINGTON_NORTH__32148.SP":
                                MyData.push("crs", "32148");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WASHINGTON__SPCS83__S__NAD83__WASHINGTON_SOUTH__32149.SP":
                                MyData.push("crs", "32149");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WEST_VIRGINIA__SPCS__N__NAD83__WEST_VIRGINIA_NORTH__32150.SP":
                                MyData.push("crs", "32150");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__WEST_VIRGINIA__SPCS__S__NAD83__WEST_VIRGINIA_SOUTH__32151.SP":
                                MyData.push("crs", "32151");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__COLORADO__SPCS__C__NAD83__COLORADO_CENTRAL__26954.SP":
                                MyData.push("crs", "26954");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__COLORADO__SPCS__S__NAD83__COLORADO_SOUTH__26955.SP":
                                MyData.push("crs", "26955");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__CONNECTICUT__NAD83__CONNECTICUT__26956.SP":
                                MyData.push("crs", "26956");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__DELAWARE__NAD83__DELAWARE__26957.SP":
                                MyData.push("crs", "26957");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__FLORIDA__SPCS__E__NAD83__FLORIDA_EAST__26958.SP":
                                MyData.push("crs", "26958");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__FLORIDA__SPCS__N__NAD83__FLORIDA_NORTH__26960.SP":
                                MyData.push("crs", "26960");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__HAWAII__ISLAND_OF_HAWAII__ONSHORE__NAD83__HAWAII_ZONE_1__26961.SP":
                                MyData.push("crs", "26961");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__HAWAII__MAUI__KAHOOLAWE__LANAI__MOLOKAI__ONSHORE__NAD83__HAWAII_ZONE_2__26962.SP":
                                MyData.push("crs", "26962");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__HAWAII__OAHU__ONSHORE__NAD83__HAWAII_ZONE_3__26963.SP":
                                MyData.push("crs", "26963");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__HAWAII__KAUAI__ONSHORE__NAD83__HAWAII_ZONE_4__26964.SP":
                                MyData.push("crs", "26964");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__HAWAII__NIIHAU__ONSHORE__NAD83__HAWAII_ZONE_5__26965.SP":
                                MyData.push("crs", "26965");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__IDAHO__SPCS__E__NAD83__IDAHO_EAST__26968.SP":
                                MyData.push("crs", "26968");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__IDAHO__SPCS__C__NAD83__IDAHO_CENTRAL__26969.SP":
                                MyData.push("crs", "26969");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__IDAHO__SPCS__W__NAD83__IDAHO_WEST__26970.SP":
                                MyData.push("crs", "26970");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ILLINOIS__SPCS__E__NAD83__ILLINOIS_EAST__26971.SP":
                                MyData.push("crs", "26971");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__ILLINOIS__SPCS__W__NAD83__ILLINOIS_WEST__26972.SP":
                                MyData.push("crs", "26972");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__INDIANA__SPCS__E__NAD83__INDIANA_EAST__26973.SP":
                                MyData.push("crs", "26973");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__INDIANA__SPCS__W__NAD83__INDIANA_WEST__26974.SP":
                                MyData.push("crs", "26974");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__IOWA__SPCS__N__NAD83__IOWA_NORTH__26975.SP":
                                MyData.push("crs", "26975");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__IOWA__SPCS__S__NAD83__IOWA_SOUTH__26976.SP":
                                MyData.push("crs", "26976");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__KANSAS__SPCS__N__NAD83__KANSAS_NORTH__26977.SP":
                                MyData.push("crs", "26977");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__KANSAS__SPCS__S__NAD83__KANSAS_SOUTH__26978.SP":
                                MyData.push("crs", "26978");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__KENTUCKY__SPCS__N__NAD83__KENTUCKY_NORTH__2205.SP":
                                MyData.push("crs", "2205");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__KENTUCKY__SPCS__S__NAD83__KENTUCKY_SOUTH__26980.SP":
                                MyData.push("crs", "26980");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__LOUISIANA__SPCS__N__NAD83__LOUISIANA_NORTH__26981.SP":
                                MyData.push("crs", "26981");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__LOUISIANA__NAD83__LOUISIANA_OFFSHORE__32199.SP":
                                MyData.push("crs", "32199");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__LOUISIANA__SPCS83__S__NAD83__LOUISIANA_SOUTH__26982.SP":
                                MyData.push("crs", "26982");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MAINE__SPCS__E__NAD83__MAINE_EAST__26983.SP":
                                MyData.push("crs", "26983");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MAINE__SPCS__W__NAD83__MAINE_WEST__26984.SP":
                                MyData.push("crs", "26984");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MARYLAND__NAD83__MARYLAND__26985.SP":
                                MyData.push("crs", "26985");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MASSACHUSETTS__SPCS__MAINLAND__NAD83__MASSACHUSETTS_MAINLAND__26986.SP":
                                MyData.push("crs", "26986");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MASSACHUSETTS__SPCS__ISLANDS__NAD83__MASSACHUSETTS_ISLAND__26987.SP":
                                MyData.push("crs", "26987");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MICHIGAN__SPCS__N__NAD83__MICHIGAN_NORTH__26988.SP":
                                MyData.push("crs", "26988");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MICHIGAN__SPCS__C__NAD83__MICHIGAN_CENTRAL__26989.SP":
                                MyData.push("crs", "26989");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MICHIGAN__SPCS__S__NAD83__MICHIGAN_SOUTH__26990.SP":
                                MyData.push("crs", "26990");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MINNESOTA__SPCS__N__NAD83__MINNESOTA_NORTH__26991.SP":
                                MyData.push("crs", "26991");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MINNESOTA__SPCS__C__NAD83__MINNESOTA_CENTRAL__26992.SP":
                                MyData.push("crs", "26992");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MINNESOTA__SPCS__S__NAD83__MINNESOTA_SOUTH__26993.SP":
                                MyData.push("crs", "26993");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MISSISSIPPI__SPCS__E__NAD83__MISSISSIPPI_EAST__26994.SP":
                                MyData.push("crs", "26994");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MISSISSIPPI__SPCS__W__NAD83__MISSISSIPPI_WEST__26995.SP":
                                MyData.push("crs", "26995");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MISSOURI__SPCS__E__NAD83__MISSOURI_EAST__26996.SP":
                                MyData.push("crs", "26996");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MISSOURI__SPCS__C__NAD83__MISSOURI_CENTRAL__26997.SP":
                                MyData.push("crs", "26997");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MISSOURI__SPCS__W__NAD83__MISSOURI_WEST__26998.SP":
                                MyData.push("crs", "26998");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__MONTANA__NAD83__MONTANA__32100.SP":
                                MyData.push("crs", "32100");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEBRASKA__NAD83__NEBRASKA__32104.SP":
                                MyData.push("crs", "32104");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEVADA__SPCS__W__NAD83__NEVADA_WEST__32109.SP":
                                MyData.push("crs", "32109");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEVADA__SPCS__E__NAD83__NEVADA_EAST__32107.SP":
                                MyData.push("crs", "32107");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEVADA__SPCS__C__NAD83__NEVADA_CENTRAL__32108.SP":
                                MyData.push("crs", "32108");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEW_JERSEY__NAD83__NEW_JERSEY__32111.SP":
                                MyData.push("crs", "32111");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEW_HAMPSHIRE__NAD83__NEW_HAMPSHIRE__32110.SP":
                                MyData.push("crs", "32110");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEW_MEXICO__SPCS__E__NAD83__NEW_MEXICO_EAST__32112.SP":
                                MyData.push("crs", "32112");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEW_MEXICO__SPCS83__C__NAD83__NEW_MEXICO_CENTRAL__32113.SP":
                                MyData.push("crs", "32113");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEW_MEXICO__SPCS83__W__NAD83__NEW_MEXICO_WEST__32114.SP":
                                MyData.push("crs", "32114");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            ///
                            case "USA__NEW_YORK__SPCS__E__NAD83__NEW_YORK_EAST__32115.SP":
                                MyData.push("crs", "32115");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEW_YORK__SPCS__C__NAD83__NEW_YORK_CENTRAL__32116.SP":
                                MyData.push("crs", "32116");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEW_YORK__SPCS__W__NAD83__NEW_YORK_WEST__32117.SP":
                                MyData.push("crs", "32117");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NEW_YORK__SPCS__LONG_ISLAND__NAD83__NEW_YORK_LONG_ISLAND__32118.SP":
                                MyData.push("crs", "32118");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NORTH_CAROLINA__NAD83__NORTH_CAROLINA__32119.SP":
                                MyData.push("crs", "32119");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NORTH_DAKOTA__SPCS__N__NAD83__NORTH_DAKOTA_NORTH__32120.SP":
                                MyData.push("crs", "32120");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__NORTH_DAKOTA__SPCS__S__NAD83__NORTH_DAKOTA_SOUTH__32121.SP":
                                MyData.push("crs", "32121");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__OHIO__SPCS__N__NAD83__OHIO_NORTH__32122.SP":
                                MyData.push("crs", "32122");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__OHIO__SPCS__S__NAD83__OHIO_SOUTH__32123.SP":
                                MyData.push("crs", "32123");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__OKLAHOMA__SPCS__N__NAD83__OKLAHOMA_NORTH__32124.SP":
                                MyData.push("crs", "32124");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__OKLAHOMA__SPCS__S__NAD83__OKLAHOMA_SOUTH__32125.SP":
                                MyData.push("crs", "32125");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__OREGON__SPCS__N__NAD83__OREGON_NORTH__32126.SP":
                                MyData.push("crs", "32126");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__OREGON__SPCS__S__NAD83__OREGON_SOUTH__32127.SP":
                                MyData.push("crs", "32127");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__PENNSYLVANIA__SPCS__N__NAD83__PENNSYLVANIA_NORTH__32128.SP":
                                MyData.push("crs", "32128");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__PENNSYLVANIA__SPCS__S__NAD83__PENNSYLVANIA_SOUTH__32129.SP":
                                MyData.push("crs", "32129");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__RHODE_ISLAND__NAD83__RHODE_ISLAND__32130.SP":
                                MyData.push("crs", "32130");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__SOUTH_CAROLINA__NAD83__SOUTH_CAROLINA__32133.SP":
                                MyData.push("crs", "32133");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__SOUTH_DAKOTA__SPCS__N__NAD83__SOUTH_DAKOTA_NORTH__32134.SP":
                                MyData.push("crs", "32134");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__SOUTH_DAKOTA__SPCS__S__NAD83__SOUTH_DAKOTA_SOUTH__32135.SP":
                                MyData.push("crs", "32135");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "USA__TENNESSEE__NAD83__TENNESSEE__32136.SP":
                                MyData.push("crs", "32136");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;


                            case "BELGIUM_ONSHORE_BD72_BELGIAN_LAMBERT_72_31370.SP":
                                //belgio
                                MyData.push("crs", "31370");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            case "RDNAPTRANS2018.SP":
                                //olanda
                                MyData.push("crs", "28992");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;

                            //////////////////NO GEOIDS
                            case "UTM_AUTO_ZONE.SP":
                                //utm
                                MyData.push("crs", "UTM");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                dialog.dismiss();
                                break;
                            case "Greece_HEPOS_GGRS87_TM87_Grid_w_Geoid.SP":
                                //grecia
                                MyData.push("crs", "2100");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                activity.recreate();

                                ReadProjectService.startCRS();
                                dialog.dismiss();
                                break;
                            default:
                                //invia file SP
                                usaSP.setEnabled(false);
                                MyData.push("crs", ".SP FILE");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                switch (DataSaved.my_comPort) {
                                    case 0:
                                        // Copia il file da assets a una directory accessibile
                                        String filePath = copyAssetFileToTemp(folderPath, selectedFileName);
                                        sendFileViaCAN(filePath, 0, 0x7DF, new CanFileTransfer.ProgressCallback() {
                                            @Override
                                            public void onProgressUpdate(int percentage) {
                                                perc = percentage;
                                            }
                                        });
                                        break;
                                    case 1:
                                    case 2:
                                        //send via serial
                                        String filePathS = copyAssetFileToTemp(folderPath, selectedFileName);
                                        SerialPortManager.instance().sendCommand("SET,EXTERNAL.RECV_FILE,START\r\n");
                                        Thread.sleep(500);
                                        sendFileViaSerial(filePathS, new CanFileTransfer.ProgressCallback() {
                                            @Override
                                            public void onProgressUpdate(int percentage) {
                                                perc = percentage;
                                            }
                                        });
                                        break;
                                    default:
                                        Thread.sleep(500);
                                        dialog.dismiss();
                                        break;


                                }
                                usaSP.setEnabled(true);
                                break;
                        }

                    } catch (Exception e) {
                        new CustomToast(activity, "SP ERROR").show_error();
                        Log.e("SP_ERROR", "Error sending file", e);
                    }
                }, 100); // 100 milliseconds delay
            }
        });
        startSearch.setOnClickListener(view -> {
            try {
                cercaSP.getText().toString();
                if (!cercaSP.getText().toString().trim().isEmpty()) {
                    mTesto = cercaSP.getText().toString().trim();
                    Log.e("SpinnerTEST", mTesto + " before ");
                    cerca(mTesto);
                    Log.e("SpinnerTEST", mTesto + " after ");
                } else {
                    sortFiles(selectedFolder);
                }
            } catch (Exception e) {
                Log.e("SpinnerTEST", mTesto + "  " + e.toString());
            }

        });
        dismiss.setOnClickListener(view -> {
            activity.recreate();
            setupGNSS(DataSaved.S_CRS);
            dialog.dismiss();
        });
        // Aggiungi un listener per gestire la selezione degli elementi dello Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Ottieni il nome della cartella selezionata

                selectedFolder = (String) parent.getItemAtPosition(position);
                Log.d("SpinnerTEST", position + " " + selectedFolder);
                MyData.push("LastNation", selectedFolder);
                // Chiama il metodo sortFiles con il nome della cartella selezionata
                sortFiles(selectedFolder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Azione quando nessun elemento è selezionato, se necessario
            }
        });
    }

    private void sortFiles(String mPath) {
        AssetManager assetManager = activity.getApplicationContext().getAssets(); // Ottiene l'AssetManager
        String folderPath = mPath; // Specifica la cartella all'interno di "assets" da cui vuoi leggere i file

        try {
            // Ottiene tutti i file all'interno della cartella specificata
            String[] files = assetManager.list(folderPath);

            if (files != null) {
                arraySP.clear(); // Pulisce la lista prima di aggiungere i nuovi file

                for (String fileName : files) {
                    // Controlla se il file ha estensione ".SP"
                    if (fileName.toLowerCase().endsWith(".sp")) {
                        // Aggiungi il file all'array con il parametro `isFolder` impostato su `false`
                        long fileSize = getFileSizeFromAssets(assetManager, folderPath + "/" + fileName);
                        arraySP.add(new ProjectFileAdapter.FileItem(fileName, false, fileSize));
                        spAdapter.notifyDataSetChanged();
                    }
                }

                Log.d("SpinnerTEST", "File trovati: " + arraySP.size());
            } else {
                Log.d("SpinnerTEST", "Nessun file trovato nella cartella: " + folderPath);
            }
        } catch (IOException e) {
            Log.e("SpinnerTEST", "Errore durante la lettura dei file da assets", e);
        }
    }

    private long getFileSizeFromAssets(AssetManager assetManager, String filePath) {
        long size = 0;
        try {
            // Apri il file come InputStream per leggere i suoi byte
            InputStream inputStream = assetManager.open(filePath);
            size = inputStream.available(); // Ottieni la dimensione del file in byte
            inputStream.close(); // Chiudi l'InputStream dopo aver finito
        } catch (IOException e) {
            Log.e("FileSize", "Errore durante il calcolo della dimensione del file: " + filePath, e);
        }
        return size;
    }


    // Metodo per ottenere i nomi delle cartelle da "assets"
    private List<String> getNomiCartelleDaAssets() {
        List<String> cartelle = new ArrayList<>();
        AssetManager assetManager = activity.getApplicationContext().getAssets();


        try {
            // Ottieni le directory principali all'interno di assets
            String[] files = assetManager.list("");

            if (files != null) {
                for (String file : files) {
                    // Controlla se il file è una cartella
                    if (isDirectory(assetManager, file) && file.equals(file.toUpperCase())) {
                        cartelle.add(file); // Aggiungi solo le cartelle alla lista
                    }
                }
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Errore durante la lettura delle cartelle da assets", e);
        }

        return cartelle;
    }

    // Metodo per verificare se un file è una directory
    private boolean isDirectory(AssetManager assetManager, String path) {
        try {
            // Prova a elencare il contenuto del percorso specificato; se ha contenuti, è una directory
            String[] files = assetManager.list(path);
            return files != null && files.length > 0;
        } catch (IOException e) {
            Log.e("MainActivity", "Errore nel controllo della directory: " + path, e);
            return false;
        }
    }

    private void startUpdating() {

        if (!isUpdating) {
            isUpdating = true;
            handler = new Handler();
            updateView();
        }
    }

    private void stopUpdating() {

        if (isUpdating) {
            isUpdating = false;
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
    }

    public void checkGeo(String s) {
        MyGeoide.setGeoid(s);
        if (s.equals(MyApp.geoidFilePath_USA2018)) {
            geo1.setChecked(true);
            geo2.setChecked(false);
            geo3.setChecked(false);
            geo4.setChecked(false);
            geo5.setChecked(false);
            geo6.setChecked(false);
        } else if (s.equals(MyApp.geoidFilePath_USA2012)) {
            geo1.setChecked(false);
            geo2.setChecked(true);
            geo3.setChecked(false);
            geo4.setChecked(false);
            geo5.setChecked(false);
            geo6.setChecked(false);
        } else if (s.equals(MyApp.geoidFilePath_NL)) {
            geo1.setChecked(false);
            geo2.setChecked(false);
            geo3.setChecked(true);
            geo4.setChecked(false);
            geo5.setChecked(false);
            geo6.setChecked(false);
        } else if (s.equals(MyApp.geoidFilePath_BG)) {
            geo1.setChecked(false);
            geo2.setChecked(false);
            geo3.setChecked(false);
            geo4.setChecked(true);
            geo5.setChecked(false);
            geo6.setChecked(false);
        } else if (s.equals(MyApp.geoidFilePath_GR)) {
            geo1.setChecked(false);
            geo2.setChecked(false);
            geo3.setChecked(false);
            geo4.setChecked(false);
            geo5.setChecked(true);
            geo6.setChecked(false);
        } else if (s.equals(MyApp.geoidFilePath_DEU)) {
            geo1.setChecked(false);
            geo2.setChecked(false);
            geo3.setChecked(false);
            geo4.setChecked(false);
            geo5.setChecked(false);
            geo6.setChecked(true);
        } else {
            geo1.setChecked(false);
            geo2.setChecked(false);
            geo3.setChecked(false);
            geo4.setChecked(false);
            geo5.setChecked(false);
            geo6.setChecked(false);

        }
    }

    private void updateView() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update View

                try {


                    if (CanFileTransfer.sending) {
                        messaggio.setText("Sending...\n" + perc + " %");
                        progressBar.setVisibility(View.VISIBLE);
                        messaggio.setVisibility(View.VISIBLE);
                        recyclerViewSP.setVisibility(View.INVISIBLE);
                        dismiss.setVisibility(View.INVISIBLE);
                        usaSP.setVisibility(View.INVISIBLE);
                        startSearch.setVisibility(View.INVISIBLE);
                        spinner.setVisibility(View.INVISIBLE);
                        cercaSP.setVisibility(View.INVISIBLE);

                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        messaggio.setVisibility(View.INVISIBLE);
                        recyclerViewSP.setVisibility(View.VISIBLE);
                        dismiss.setVisibility(View.VISIBLE);
                        usaSP.setVisibility(View.VISIBLE);
                        startSearch.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.VISIBLE);
                        cercaSP.setVisibility(View.VISIBLE);
                    }

                    if (isUpdating) {
                        updateView();

                    }
                } catch (Exception e) {
                    Log.d("step!", e.toString());
                }
            }
        }, 100);
    }

    public void cerca(String testo) {
        // Lista temporanea per contenere i file che corrispondono al testo cercato
        List<ProjectFileAdapter.FileItem> risultatiFiltrati = new ArrayList<>();
        Log.d("SpinnerTEST", "Testo cercato: " + testo);

        // Itera su tutti gli elementi di arraySP
        for (ProjectFileAdapter.FileItem item : arraySP) {
            Log.d("SpinnerTEST", "Nome del file: " + item.getName());

            // Controlla se il nome del file contiene la stringa di testo cercata (case insensitive)
            if (item.getName().toLowerCase().contains(testo.toLowerCase())) {
                Log.d("SpinnerTEST", "Elemento trovato: dentro IF " + item.getName());
                risultatiFiltrati.add(item);
            } else {
                Log.d("SpinnerTEST", "Elemento non trovato: fuori IF " + item.getName());
            }
        }

        // Aggiorna arraySP con i risultati filtrati
        arraySP.clear();
        arraySP.addAll(risultatiFiltrati);

        // Notifica l'adattatore delle modifiche
        spAdapter.notifyDataSetChanged();

        // Log per il debugging
        Log.d("SpinnerTEST", "Numero di elementi trovati: " + arraySP.size());
    }

    private String copyAssetFileToTemp(String folderPath, String fileName) throws IOException {
        AssetManager assetManager = activity.getApplicationContext().getAssets();

        // Percorso completo nel pacchetto degli assets
        String assetPath = folderPath + "/" + fileName;

        // Directory temporanea in cui copiare il file
        File tempFile = new File(activity.getCacheDir(), fileName);

        try (InputStream inputStream = assetManager.open(assetPath);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        // Restituisce il percorso del file copiato
        return tempFile.getAbsolutePath();
    }

    private void setupGNSS(String crs) {
        byte speed = 0;
        switch (DataSaved.reqSpeed) {
            case 0:
                speed = 5;
                break;
            case 1:
                speed = 4;
                break;
            case 2:
                speed = 3;
                break;
            case 3:
                speed = 0;
                break;

        }
        byte msg = 0x03;


        Log.d("SCRS", "start " + crs + "  " + _NONE + "  " + msg);
        MyDeviceManager.CanWrite(0, 0x18FF0001, 4, new byte[]{0x20, msg, speed, (byte) 0x03});
        if (crs.equals(_NONE)) {
            //setup LLQ

            SerialPortManager.instance().sendCommand("set,device.remote.nmea,GGA:" + "0" + "|GSA:0|LLQ:" + "50" + "|GLL:0|HDT:" + "50" + "|\n");

            handler.postDelayed(() -> {
                SerialPortManager.instance().sendCommand("set,ports.reset,2\r\n");
                SerialPortManager.instance().sendCommand("set,ports.reset,1\r\n");
                SerialPortManager.instance().sendCommand("set,ports.reset,16\r\n");

            }, 1000);

        } else {
            //setup GGA
            SerialPortManager.instance().sendCommand("set,device.remote.nmea,GGA:" + "50" + "|GSA:0|LLQ:" + "0" + "|GLL:0|HDT:" + "50" + "|\n");
            Handler handler = new Handler(activity.getMainLooper());


            handler.postDelayed(() -> {
                SerialPortManager.instance().sendCommand("set,ports.reset,2\r\n");
                SerialPortManager.instance().sendCommand("set,ports.reset,1\r\n");
                SerialPortManager.instance().sendCommand("set,ports.reset,16\r\n");

            }, 1000);

        }

    }

}
