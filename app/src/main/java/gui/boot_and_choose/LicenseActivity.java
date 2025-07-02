package gui.boot_and_choose;

import static gui.MyApp.folderPath;
import static gui.dialogs_and_toast.DialogPassword.isTech;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.example.stx_dig.R;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import drill_pile.gui.Drill_MainPage;
import gui.BaseClass;
import gui.MyApp;
import gui.dialogs_and_toast.CloseAppDialog;
import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import services.UpdateValuesService;
import utils.MyData;
import utils.VerifyLicense;

;

public class LicenseActivity extends BaseClass {
    CloseAppDialog closeAppDialog;
    TextView textView, text2;
    EditText editText;
    Button load, exit, erase, scan;
    CustomQwertyDialog customQwertyDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_license);
        findView();
        init();
        onClick();
        update();
        customQwertyDialog = new CustomQwertyDialog(this);
        closeAppDialog = new CloseAppDialog(this);


    }

    private void findView() {
        textView = findViewById(R.id.textView2);
        text2 = findViewById(R.id.textView3);
        editText = findViewById(R.id.editText);
        load = findViewById(R.id.button);
        exit = findViewById(R.id.button2);
        erase = findViewById(R.id.erase);
        scan = findViewById(R.id.scan);


    }

    private void init() {
        try {
            text2.setText(MyData.get_String("licenza"));


        } catch (Exception e) {
            text2.setText("NO LICENSE FOUND");


        }
        String s = "";
        if (Build.BRAND.equals("APOLLO2_10")||Build.BRAND.equals("APOLLO2_7")||Build.BRAND.equals("APOLLO2_12_PRO")||Build.BRAND.equals("APOLLO2_12_PLUS")) {
            Apollo2 apollo2 = Apollo2.getInstance(LicenseActivity.this);
            s = apollo2.getDeviceSN();
        } else {
            ApolloPro apolloPro = ApolloPro.getInstance(LicenseActivity.this);
            s = apolloPro.getDeviceSN();
        }
        switch (MyApp.KEY_LEVEL) {
            case -1:
            case 0:
                try {
                   String string=Environment.getExternalStorageDirectory().toString()+folderPath+"/Machines/Erased.csv";
                   CSVReader fileReader=new CSVReader(new FileReader(new File(string)));
                   String []s1= fileReader.readNext();
                    textView.setText(s+" ERASED LICENSE code: " + s1[1]);
                } catch (Exception e) {
                    textView.setText("NO LICENSE s/n " + s);
                }

                exit.setVisibility(View.INVISIBLE);
                scan.setVisibility(View.VISIBLE);
                erase.setVisibility(View.INVISIBLE);
                break;

            case 1:
                textView.setText("Dig 1D License No AUTO s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                break;

            case 2:
                textView.setText("Dig 1D / 2D License No AUTO s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                break;

            case 3:
                textView.setText("MC 1D / 2D / 3DEasy License No AUTO s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                break;

            case 4:
                textView.setText("MC 1D / 2D / 3DPRO License No AUTO s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                break;
            case 10:
                textView.setText("Drill License No AUTO s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                break;
            case 11:
                textView.setText("Drill License + AUTO s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                break;
            case 33:
                textView.setText("MC 1D License AUTO ENABLED s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                break;

            case 34:
                textView.setText("MC 1D / 2D License AUTO ENABLED s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                break;

            case 35:
                textView.setText("MC 1D / 2D / 3DEasy License AUTO ENABLED s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                break;

            case 36:
                textView.setText("MC 1D / 2D / 3DPRO License AUTO ENABLED s/n " + s);
                scan.setVisibility(View.INVISIBLE);
                editText.setVisibility(View.INVISIBLE);
                erase.setVisibility(View.VISIBLE);
                load.setAlpha(0.4f);
                break;
        }

    }

    private void onClick() {
        // Imposta il listener per il bottone
        erase.setOnClickListener(view -> {
            if (isTech) {
                // Crea un nuovo AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(LicenseActivity.this);
                builder.setTitle("ERASE LICENSE- License data will be lost");
                builder.setMessage("Do You Want to Proceed ?");

                // Aggiungi il pulsante "Sì"
                builder.setPositiveButton("YES", (dialog, which) -> {
                    try {
                        String fileName = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Lic.csv";
                        File file = new File(fileName);
                        file.delete();
                    } catch (Exception ignored) {
                       new CustomToast(LicenseActivity.this,"Error Deleting").show_error();
                    }
                    MyData.push("licenza", "000000");

                    try {
                        String s = "";
                        if (Build.BRAND.equals("APOLLO2_10")||Build.BRAND.equals("APOLLO2_7")||Build.BRAND.equals("APOLLO2_12_PRO")||Build.BRAND.equals("APOLLO2_12_PLUS")) {
                            Apollo2 apollo2 = Apollo2.getInstance(LicenseActivity.this);
                            s = apollo2.getDeviceSN();
                        } else {
                            ApolloPro apolloPro = ApolloPro.getInstance(LicenseActivity.this);
                            s = apolloPro.getDeviceSN();
                        }
                        int myInt=0;
                        switch (MyApp.KEY_LEVEL){
                            case 0:
                            case -1:
                                myInt=0;
                                break;
                            case 10:
                                myInt=10;
                                break;

                            case 11:
                                myInt=11;
                                break;
                            case 1:
                                myInt=50;
                                break;
                            case 2:
                                myInt=60;
                                break;
                            case 3:
                                myInt=70;
                                break;
                            case 4:
                                myInt=80;
                                break;
                            case 33:
                                myInt=81;
                                break;
                            case 34:
                                myInt=82;
                                break;
                            case 35:
                                myInt=83;
                                break;
                            case 36:
                                myInt=84;
                                break;
                        }
                        MyApp.KEY_LEVEL = -1;
                        String eraseCodeR= VerifyLicense.generateRestoreCode(s,myInt);
                        String claim=Environment.getExternalStorageDirectory().toString()+folderPath+"/Machines/Erased.csv";
                        File Ferased=new File(claim);
                        CSVWriter writer=null;
                        writer=new CSVWriter(new FileWriter(Ferased));
                        String[] eraseCode={s,eraseCodeR};
                        writer.writeNext(eraseCode);
                        try {
                            writer.close();
                        } catch (IOException e) {
                            Log.e("Scrittura",Log.getStackTraceString(e));
                        }
                    } catch (Exception ignored) {

                    }
                    startActivity(new Intent(this, LaunchScreenActivity.class));
                    overridePendingTransition(0, 0);
                    finish();

                });

                // Aggiungi il pulsante "No"
                builder.setNegativeButton("NO", (dialog, which) -> {
                    switch (MyApp.KEY_LEVEL){
                        case 10:
                        case 11:
                            Intent intent1;
                            intent1 = new Intent(this, Drill_MainPage.class);
                            startActivity(intent1);
                            overridePendingTransition(0,0);
                            finish();
                            break;

                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 33:
                        case 34:
                        case 35:
                        case 36:
                            Intent intent;
                            intent = new Intent(this, Activity_Home_Page.class);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                            finish();
                            break;
                    }

                });

                // Mostra il dialog
                builder.show();
            } else {
                new CustomToast(this, "You need Tech Permissions to erase a license").show_long();
            }
        });

        exit.setOnClickListener(view -> {
            switch (MyApp.KEY_LEVEL){
                case 10:
                case 11:
                    Intent intent1;
                    intent1 = new Intent(this, Drill_MainPage.class);
                    startActivity(intent1);
                    overridePendingTransition(0,0);
                    finish();
                    break;

                case 1:
                case 2:
                case 3:
                case 4:
                case 33:
                case 34:
                case 35:
                case 36:
                    Intent intent;
                    intent = new Intent(this, Activity_Home_Page.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                    finish();
                    break;
                default:
                 /*   try {
                        CPCanHelper.getInstance().disconnectAll();
                        stopService(new Intent(this, CanService.class));
                        stopService(new Intent(this, CanSender.class));
                        stopService(new Intent(this, TriangleService.class));
                        OpenSerialPort.mOpened = false;
                        SerialPortManager.instance().close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("ExceptClose", e.toString());
                    }

                    MyDeviceManager.showBar(this);
                    MyDeviceManager.OUT1(this, 0);
                    MyDeviceManager.OUT2(this, 0);
                    MyDeviceManager.host(this);
                    MyData.push("machinestate", "1");
                    finishAndRemoveTask();
                    // Chiudi l'applicazione e tutti i suoi processi
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);

            */
                    break;
            }
        });

        load.setOnClickListener(view -> {
            if (MyApp.KEY_LEVEL==-1||MyApp.KEY_LEVEL==0||MyApp.KEY_LEVEL==1||MyApp.KEY_LEVEL==2||MyApp.KEY_LEVEL==3||MyApp.KEY_LEVEL==10||MyApp.KEY_LEVEL==11||
                    MyApp.KEY_LEVEL==4||MyApp.KEY_LEVEL==33||MyApp.KEY_LEVEL==34||MyApp.KEY_LEVEL==35) {
                String s = editText.getText().toString().toUpperCase();
                if (verifyString(s)) {
                    MyApp.LICENSE_KEY = s;
                    MyData.push("licenza", s);
                    try {
                        String fileName = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Erased.csv";
                        File file = new File(fileName);
                        file.delete();
                    } catch (Exception ignored) {
                        new CustomToast(LicenseActivity.this,"Error Deleting").show_error();
                    }

                    startActivity(new Intent(this, LaunchScreenActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                } else if (s.equals("QWEDSAZXC")) {
                    isTech = true;
                    if (!closeAppDialog.alertDialog.isShowing()) {
                        closeAppDialog.show();
                    }

                } else {
                    new CustomToast(this, "Invalid License Format").show_error();
                }
            }
        });
        editText.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(editText);
            }
        });
        scan.setOnClickListener(view -> {

            try {
                String fileName = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Lic.csv";
                CSVReader reader = new CSVReader(new FileReader(fileName));
                String[] lic = reader.readNext();
                MyData.push("licenza", lic[1]);
                reader.close();
                try {
                    String fileNamea = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Erased.csv";
                    File file = new File(fileNamea);
                    file.delete();
                } catch (Exception ignored) {
                    new CustomToast(LicenseActivity.this,"Error Deleting").show_error();
                }
            } catch (Exception e) {
                try {



                    String fileName = Environment.getExternalStorageDirectory().toString() + folderPath + "/Lic.csv";
                    CSVReader reader = new CSVReader(new FileReader(fileName));
                    String[] lic = reader.readNext();
                    MyData.push("licenza", lic[1]);
                    reader.close();
                    try {
                        String fileNameb = Environment.getExternalStorageDirectory().toString() + folderPath + "/Machines/Erased.csv";
                        File file = new File(fileNameb);
                        file.delete();
                    } catch (Exception ignored) {
                        new CustomToast(LicenseActivity.this,"Error Deleting").show_error();
                    }
                } catch (Exception ingored) {

                }
            }


            startService(new Intent(this, UpdateValuesService.class));
            (new Handler()).postDelayed(this::startLaunch, 2000);


        });

    }

    public void startLaunch() {
        startActivity(new Intent(LicenseActivity.this, LaunchScreenActivity.class));
        finish();
    }

    private boolean verifyString(String s) {
        String regex = "^[A-Z0-9]{6}_[A-Z0-9]{6}_[A-Z0-9]{6}$";

        // Crea un pattern con la regex
        Pattern pattern = Pattern.compile(regex);

        // Crea un matcher che confronta la stringa con il pattern
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    private void update() {

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}