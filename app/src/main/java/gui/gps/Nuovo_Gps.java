package gui.gps;

import static packexcalib.gnss.CRS_Strings._NONE;
import static utils.Utils.convertIpStringToBytes;
import static utils.Utils.createPackets;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stx_dig.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import event_bus.CMD_Event;
import event_bus.CanEvents;
import gui.debug_ecu.Can_Msg_Debug;
import gui.debug_ecu.Serial_Msg_Debug;
import gui.dialogs_and_toast.CustomMenu;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import serial.OpenSerialPort;
import serial.SerialPortManager;
import utils.MyData;
import utils.MyDeviceManager;
import utils.WifiHelper;

public class Nuovo_Gps extends AppCompatActivity {
    ConstraintLayout allerta;
    LinearLayout lowerView, upper;
    LinearLayout lay1, lay2, lay3, lay4, lay5, lay6, lay7, lay8, lay9, lay10, lay11, lay12, lay13, lay14, lay15, lay16;
    TextView txtlay1, txtlay2, txtlay3, txssid;
    boolean showWebUI = false;
    ScrollView scrollView;
    WebView webView;
    ImageView chiudiWeb, wifistate;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_PICKER_REQUEST_CODE = 1001;
    String stato = "";
    String cmdStatust = "";
    String sCanale = "";
    static byte[] ipBytes;
    static byte[] mPort;
    private static final byte READ = 0x01;
    private static final byte WRITE = 0x20;
    int indexOfMachine, chan;
    RadioButton rbWired, rbModem, rbWifi;
    ImageView back, gpsStat, debug, readAll, saveAll;
    ProgressBar progressBar2;
    TextView statusBar;
    CheckBox ckSmc, ckS980, ckSc600, ckCan, ckHSL0, ckHSL2, ckWK0, ckDemo, ckRadio, ckNtrip, ck20, ck10, ck5, ckOff;
    EditText etIntero, etDecimale, etCh, tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv28, tv38;
    Button defaultF, read0, read1, read2, read3, read4, read5, read6, read7, read8, read9, read10, read11, read12, read28, read38, readNet;
    Button write0, write1, write2, write3, write4, write5, write6, write7, write8, write9, write10, write11, write12, write28, write38, writeNet;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    CustomQwertyDialog customQwertyDialog;
    CustomNumberDialog customNumberDialog;
    static boolean isRead0, isRead1, isRead2, isRead3, isRead4, isRead5, isRead6, isRead7, isRead8, isRead9, isRead10, isRead11, isRead12, isRead28, isRead38, isReadNet;
    final List<byte[]> packetList = new ArrayList<>();
    String apn = "";
    String apnID = "";
    String apnPW = "";
    String corsIP = "";
    String corsPORT = "";
    String corsID = "";
    String corsPW = "";
    String mountpoint = "";
    String ggaUPLOAD = "";
    int intero, decimale, proto, selectedSpacing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuovo_gps);
        findView();
        init();
        ckBoxes();
        onClick();
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void updateUI() {
        try {
            String ssid = WifiHelper.getConnectedSSID(getApplicationContext());
            if (ssid != null) {

                wifistate.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);
                txssid.setText(ssid.replaceAll("\"", ""));

            } else {

                wifistate.setImageResource(R.drawable.wifi_vuoto);
                txssid.setText("DISCONNECTED");

            }

        } catch (Exception e) {
            wifistate.setBackgroundColor(getColor(R.color.light_yellow));
        }
        if (DataSaved.gpsType == 0 && DataSaved.my_comPort == 0) {
            showLayouts(0);
            allerta.setVisibility(View.INVISIBLE);
        } else {
            showLayouts(8);
            allerta.setVisibility(View.VISIBLE);
        }

        if (showWebUI) {
            upper.setVisibility(View.VISIBLE);
            lowerView.setVisibility(View.INVISIBLE);
            allerta.setVisibility(View.INVISIBLE);

            if (isKeyboardVisible(readAll.getRootView())) {
                saveAll.setImageResource(R.drawable.baseline_check_96);
                saveAll.setVisibility(View.VISIBLE);
            } else {
                saveAll.setImageResource(R.drawable.done_btn);
                saveAll.setVisibility(View.INVISIBLE);
                saveAll.setVisibility(View.VISIBLE);
            }
            scrollView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            chiudiWeb.setVisibility(View.VISIBLE);
            gpsStat.setVisibility(View.GONE);
            back.setVisibility(View.GONE);
            debug.setVisibility(View.GONE);
            readAll.setVisibility(View.GONE);
        } else {
            upper.setVisibility(View.INVISIBLE);
            lowerView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            chiudiWeb.setVisibility(View.GONE);
            gpsStat.setVisibility(View.VISIBLE);
            back.setVisibility(View.VISIBLE);
            debug.setVisibility(View.VISIBLE);
            readAll.setVisibility(View.VISIBLE);
            saveAll.setImageResource(R.drawable.btn_web_switch);
            saveAll.setVisibility(View.VISIBLE);
        }
        if (DataSaved.gpsOk) {
            gpsStat.setImageTintList(getColorStateList(R.color.green));
        } else {
            gpsStat.setImageTintList(getColorStateList(R.color.red));
        }
        if (isRead3) {
            defaultF.setVisibility(View.VISIBLE);
        } else {
            defaultF.setVisibility(View.INVISIBLE);
        }

    }

    private void findView() {
        indexOfMachine = MyData.get_Int("MachineSelected");
        allerta = findViewById(R.id.messaggioAllerta);
        lowerView = findViewById(R.id.lowerView);
        upper = findViewById(R.id.upper);
        chiudiWeb = findViewById(R.id.chiudiWeb);
        lay1 = findViewById(R.id.lay1);
        lay2 = findViewById(R.id.lay2);
        lay3 = findViewById(R.id.lay3);
        lay4 = findViewById(R.id.lay4);
        lay5 = findViewById(R.id.lay5);
        lay6 = findViewById(R.id.lay6);
        lay7 = findViewById(R.id.lay7);
        lay8 = findViewById(R.id.lay8);
        lay9 = findViewById(R.id.lay9);
        lay10 = findViewById(R.id.lay10);
        lay11 = findViewById(R.id.lay11);
        lay12 = findViewById(R.id.lay12);
        lay13 = findViewById(R.id.lay13);
        lay14 = findViewById(R.id.lay14);
        lay15 = findViewById(R.id.lay15);
        lay16 = findViewById(R.id.lay16);
        txtlay1 = findViewById(R.id.txtlay1);
        txtlay2 = findViewById(R.id.txtlay2);
        txtlay3 = findViewById(R.id.txtlay3);
        saveAll = findViewById(R.id.writeR);
        back = findViewById(R.id.toMain);
        gpsStat = findViewById(R.id.clearLog);
        debug = findViewById(R.id.toDebug);
        ckSmc = findViewById(R.id.smc);
        ckS980 = findViewById(R.id.s980);
        ckSc600 = findViewById(R.id.sc600);
        ckCan = findViewById(R.id.can);
        ckHSL0 = findViewById(R.id.hsl0);
        ckHSL2 = findViewById(R.id.hsl2);
        ckWK0 = findViewById(R.id.wk0);
        ckDemo = findViewById(R.id.demo);
        ckRadio = findViewById(R.id.radio);
        ckNtrip = findViewById(R.id.ntrip);
        ck20 = findViewById(R.id.ck20);
        ck10 = findViewById(R.id.ck10);
        ck5 = findViewById(R.id.ck5);
        ckOff = findViewById(R.id.ck0);
        etCh = findViewById(R.id.etCh);
        etIntero = findViewById(R.id.txtIntero);
        etDecimale = findViewById(R.id.txtDec);
        defaultF = findViewById(R.id.defaultF);
        tv1 = findViewById(R.id.txtRadioProt);
        tv2 = findViewById(R.id.txtRadioSpac);
        tv3 = findViewById(R.id.txtAPN);
        tv4 = findViewById(R.id.txtUser);
        tv5 = findViewById(R.id.txtPword);
        tv6 = findViewById(R.id.txtUserID);
        tv7 = findViewById(R.id.txtPwID);
        tv8 = findViewById(R.id.txtMOUNT);
        tv9 = findViewById(R.id.txtUpload);
        tv28 = findViewById(R.id.txtIP);
        tv38 = findViewById(R.id.txtPORT);
        read0 = findViewById(R.id.read0);
        read1 = findViewById(R.id.read1);
        read2 = findViewById(R.id.read2);
        read3 = findViewById(R.id.read3);
        read4 = findViewById(R.id.read4);
        read5 = findViewById(R.id.read5);
        read6 = findViewById(R.id.read6);
        read7 = findViewById(R.id.read7);
        read8 = findViewById(R.id.read8);
        read9 = findViewById(R.id.read9);
        read10 = findViewById(R.id.read10);
        read11 = findViewById(R.id.read11);
        read12 = findViewById(R.id.read12);
        read28 = findViewById(R.id.read28);
        read38 = findViewById(R.id.read38);
        write0 = findViewById(R.id.write0);
        write1 = findViewById(R.id.write1);
        write2 = findViewById(R.id.write2);
        write3 = findViewById(R.id.write3);
        write4 = findViewById(R.id.write4);
        write5 = findViewById(R.id.write5);
        write6 = findViewById(R.id.write6);
        write7 = findViewById(R.id.write7);
        write8 = findViewById(R.id.write8);
        write9 = findViewById(R.id.write9);
        write10 = findViewById(R.id.write10);
        write11 = findViewById(R.id.write11);
        write12 = findViewById(R.id.write12);
        write28 = findViewById(R.id.write28);
        write38 = findViewById(R.id.write38);
        rbWifi = findViewById(R.id.rbWifi);
        rbWired = findViewById(R.id.rbWired);
        rbModem = findViewById(R.id.rbModem);
        readNet = findViewById(R.id.readNet);
        writeNet = findViewById(R.id.writeNet);
        progressBar2 = findViewById(R.id.progressBar2);
        statusBar = findViewById(R.id.statusBar);
        readAll = findViewById(R.id.readR);
        wifistate = findViewById(R.id.wifiState);
        txssid = findViewById(R.id.txssid);
        endis(true);
        isReadNet = false;
        isRead0 = false;
        isRead1 = false;
        isRead2 = false;
        isRead3 = false;
        isRead4 = false;
        isRead5 = false;
        isRead6 = false;
        isRead7 = false;
        isRead8 = false;
        isRead9 = false;
        isRead10 = false;
        isRead11 = false;
        isRead12 = false;
        isRead28 = false;
        isRead38 = false;
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        customNumberDialog = new CustomNumberDialog(this, 100);
        customQwertyDialog = new CustomQwertyDialog(this,null);
        ckHSL0.setText(MyDeviceManager.serialCom(1).replaceAll("/dev/", ""));
        ckHSL2.setText(MyDeviceManager.serialCom(2).replaceAll("/dev/", ""));
        ckWK0.setText(MyDeviceManager.serialCom(3).replaceAll("/dev/", ""));
        progressBar2.setVisibility(View.INVISIBLE);
        scrollView = findViewById(R.id.scrollView);
        webView = findViewById(R.id.webView);
    }


    private void init() {
        showWebUI = false;
        if (chan < 1) {
            chan = 1;
        }
        ckSmc.setChecked(DataSaved.gpsType == 0);
        ckS980.setChecked(DataSaved.gpsType == 1);
        ckSc600.setChecked(DataSaved.gpsType == 2);
        ckCan.setChecked(DataSaved.my_comPort == 0);
        ckHSL0.setChecked(DataSaved.my_comPort == 1);
        ckHSL2.setChecked(DataSaved.my_comPort == 2);
        ckWK0.setChecked(DataSaved.my_comPort == 3);
        ckDemo.setChecked(DataSaved.my_comPort == 4);

    }

    private void ckBoxes() {

        rbWired.setOnClickListener(view -> {
            rbWired.setChecked(rbWired.isChecked());
        });
        rbWifi.setOnClickListener(view -> {
            rbWifi.setChecked(rbWifi.isChecked());
        });
        rbModem.setOnClickListener(view -> {
            rbModem.setChecked(rbModem.isChecked());
        });
        ckSmc.setOnClickListener(view -> {
            DataSaved.gpsType = 0;
            MyData.push("M" + indexOfMachine + "_sc600", String.valueOf(DataSaved.gpsType));
            init();
        });
        ckS980.setOnClickListener(view -> {
            DataSaved.gpsType = 1;
            MyData.push("M" + indexOfMachine + "_sc600", String.valueOf(DataSaved.gpsType));
            init();
        });
        ckSc600.setOnClickListener(view -> {
            DataSaved.gpsType = 2;
            MyData.push("M" + indexOfMachine + "_sc600", String.valueOf(DataSaved.gpsType));
            init();
        });
        ckCan.setOnClickListener(view -> {
            DataSaved.my_comPort = 0;
            MyData.push("M" + indexOfMachine + "_comPort", String.valueOf(DataSaved.my_comPort));
            init();
            SerialPortManager.instance().close();
        });
        ckHSL0.setOnClickListener(view -> {
            DataSaved.my_comPort = 1;
            MyData.push("M" + indexOfMachine + "_comPort", String.valueOf(DataSaved.my_comPort));
            init();
            new OpenSerialPort(Nuovo_Gps.this);
        });
        ckHSL2.setOnClickListener(view -> {
            DataSaved.my_comPort = 2;
            MyData.push("M" + indexOfMachine + "_comPort", String.valueOf(DataSaved.my_comPort));
            init();
            new OpenSerialPort(Nuovo_Gps.this);
        });
        ckWK0.setOnClickListener(view -> {
            DataSaved.my_comPort = 3;
            MyData.push("M" + indexOfMachine + "_comPort", String.valueOf(DataSaved.my_comPort));
            init();
            new OpenSerialPort(Nuovo_Gps.this);
        });
        ckDemo.setOnClickListener(view -> {
            DataSaved.my_comPort = 4;
            MyData.push("M" + indexOfMachine + "_comPort", String.valueOf(DataSaved.my_comPort));
            init();
            SerialPortManager.instance().close();
        });
        ck20.setOnClickListener(view -> {
            DataSaved.reqSpeed = 0;
            MyData.push("M" + indexOfMachine + "reqSpeed", String.valueOf(DataSaved.reqSpeed));
            ck20.setChecked(DataSaved.reqSpeed == 0);
            ck10.setChecked(DataSaved.reqSpeed == 1);
            ck5.setChecked(DataSaved.reqSpeed == 2);
            ckOff.setChecked(DataSaved.reqSpeed == 3);
        });
        ck10.setOnClickListener(view -> {
            DataSaved.reqSpeed = 1;
            MyData.push("M" + indexOfMachine + "reqSpeed", String.valueOf(DataSaved.reqSpeed));
            ck20.setChecked(DataSaved.reqSpeed == 0);
            ck10.setChecked(DataSaved.reqSpeed == 1);
            ck5.setChecked(DataSaved.reqSpeed == 2);
            ckOff.setChecked(DataSaved.reqSpeed == 3);
        });
        ck5.setOnClickListener(view -> {
            DataSaved.reqSpeed = 2;
            MyData.push("M" + indexOfMachine + "reqSpeed", String.valueOf(DataSaved.reqSpeed));
            ck20.setChecked(DataSaved.reqSpeed == 0);
            ck10.setChecked(DataSaved.reqSpeed == 1);
            ck5.setChecked(DataSaved.reqSpeed == 2);
            ckOff.setChecked(DataSaved.reqSpeed == 3);
        });
        ckOff.setOnClickListener(view -> {
            DataSaved.reqSpeed = 3;
            MyData.push("M" + indexOfMachine + "reqSpeed", String.valueOf(DataSaved.reqSpeed));
            ck20.setChecked(DataSaved.reqSpeed == 0);
            ck10.setChecked(DataSaved.reqSpeed == 1);
            ck5.setChecked(DataSaved.reqSpeed == 2);
            ckOff.setChecked(DataSaved.reqSpeed == 3);
        });
        ckRadio.setOnClickListener(view -> {
            DataSaved.radioMode = 0;
            ckNtrip.setChecked(false);
            init();

        });
        ckNtrip.setOnClickListener(view -> {
            DataSaved.radioMode = 1;
            ckRadio.setChecked(false);
            init();
        });
        rbWired.setOnClickListener(view -> {
            DataSaved.priorityNet = 0;
            rbWifi.setChecked(false);
            rbModem.setChecked(false);
            rbWired.setChecked(true);

        });
        rbWifi.setOnClickListener(view -> {
            DataSaved.priorityNet = 2;
            rbWifi.setChecked(true);
            rbModem.setChecked(false);
            rbWired.setChecked(false);

        });
        rbModem.setOnClickListener(view -> {
            DataSaved.priorityNet = 3;
            rbWifi.setChecked(false);
            rbModem.setChecked(true);
            rbWired.setChecked(false);

        });
    }

    private void onClick() {
        // Configura il WebChromeClient per gestire input file
        webView.setWebChromeClient(new WebChromeClient() {
            // Gestione di input file per "Browse"
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                openFileChooser(filePathCallback, fileChooserParams);
                return true;
            }
        });
        readAll.setOnClickListener(view -> {
            if (!showWebUI && DataSaved.gpsType == 0 && DataSaved.my_comPort == 0) {
                allReading();
            }
        });
        chiudiWeb.setOnClickListener(view -> {
            // Creazione della dialog di conferma
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle("")
                    .setMessage("EXIT?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Azione da eseguire se l'utente preme "Yes"
                        showWebUI = false;
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Chiudi la dialog senza fare nulla
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });
        saveAll.setOnClickListener(view -> {
            if (isKeyboardVisible(view)) {
                hideKeyboard(view);
               /* if (geckoSession != null) {
                    geckoSession.close();
                }*/
            } else {
                showWebUI = !showWebUI;
                if (showWebUI) {
                    openWebUI();
                }
            }
        });
        back.setOnClickListener(view -> {
            if (!showWebUI) {
                Intent intent;
                intent = new Intent(this, ExcavatorChooserActivity.class);
                startActivity(intent);
                finish();
            }
        });
        gpsStat.setOnClickListener(view -> {
            if (!showWebUI) {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                    dialogGnssCoordinates.show();
                }
            }
        });
        debug.setOnClickListener(view -> {
            if (!showWebUI) {
                endis(false);
                if (DataSaved.my_comPort == 0) {
                    Intent intent = new Intent(this, Can_Msg_Debug.class);
                    intent.putExtra("chi", "gps");
                    startActivity(intent);
                    finish();
                } else {
                    startActivity(new Intent(this, Serial_Msg_Debug.class));
                    finish();
                }
            }
        });
        read0.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead0();
        });
        read1.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead1();
        });
        read2.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead2();

        });
        read3.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead3();

        });
        read4.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead4();
        });
        read5.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead5();
        });
        read6.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead6();

        });
        read7.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead7();

        });
        read8.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead8();

        });
        read28.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead28();

        });
        read38.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead38();

        });
        read9.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead9();
        });
        read10.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead10();

        });
        read11.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead11();

        });
        read12.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mRead12();

        });
        readNet.setOnClickListener(view -> {
            showProgress(1000);
            statusBar.setText("Reading..");
            mReadNet();
        });
        write0.setOnClickListener(view -> {
            if (isRead0) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        int speed = 0;
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
                        MyDeviceManager.CanWrite(0, 0x18FF0001, 4, new byte[]{WRITE, (byte) 0x03, (byte) speed, (byte) 0x03});
                        break;
                    case 1:
                        //s980
                        String fgga1 = "50";
                        String sGst1 = "2000";
                        switch (DataSaved.reqSpeed) {
                            case 0:
                                fgga1 = "50";
                                break;
                            case 1:
                                fgga1 = "100";
                                break;
                            case 2:
                                fgga1 = "200";
                                break;
                            case 3:
                                fgga1 = "0";
                                break;
                        }
                        if (DataSaved.S_CRS.equals(_NONE)) {
                            SerialPortManager.instance().sendCommand("set,device.remote.nmea,GGA:" + "0" + "|GSA:0|LLQ:" + fgga1 + "|ZDA:0|RMC:0|VTG:0|GST:" + sGst1 + "|GLL:0|HDT:" + fgga1 + "|HPR:0|PJK:" + "0" + "|GRS:0|GSV:0|\r\n");
                        } else {
                            SerialPortManager.instance().sendCommand("set,device.remote.nmea,GGA:" + fgga1 + "|GSA:0|LLQ:" + "0" + "|ZDA:0|RMC:0|VTG:0|GST:" + sGst1 + "|GLL:0|HDT:" + fgga1 + "|HPR:0|PJK:" + "0" + "|GRS:0|GSV:0|\r\n");
                        }

                        break;
                    case 2:
                        //sc600
                        String fgga = "50";
                        String sGst = "2000";
                        switch (DataSaved.reqSpeed) {
                            case 0:
                                fgga = "50";
                                break;
                            case 1:
                                fgga = "100";
                                break;
                            case 2:
                                fgga = "200";
                                break;
                            case 3:
                                fgga = "0";
                                break;
                        }
                        if (DataSaved.S_CRS.equals(_NONE)) {
                            SerialPortManager.instance().sendCommand("set,ports.com3.nmea,GGA:" + "0" + "|GSA:0|LLQ:" + fgga + "|ZDA:0|RMC:0|VTG:0|GST:" + sGst + "|GLL:0|HDT:" + fgga + "|HPR:0|PJK:" + "0" + "|GRS:0|GSV:0|\r\n");
                        } else {
                            SerialPortManager.instance().sendCommand("set,ports.com3.nmea,GGA:" + fgga + "|GSA:0|LLQ:" + "0" + "|ZDA:0|RMC:0|VTG:0|GST:" + sGst + "|GLL:0|HDT:" + fgga + "|HPR:0|PJK:" + "0" + "|GRS:0|GSV:0|\r\n");

                        }
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write1.setOnClickListener(view -> {
            if (isRead1) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        if (DataSaved.radioMode == 0) {
                            //passa ad ntrip
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x20, (byte) 0x11, 0x1, (byte) DataSaved.priorityNet, (byte) 0x1, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
                            MyData.push("M" + indexOfMachine + "radioMode", "1");
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x20, (byte) 0x73, 0x61, (byte) 0x76, (byte) 0x65, (byte) 0x61, (byte) 0x6C, (byte) 0x6C});
                        } else {
                            //passa a uhf
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x20, (byte) 0x11, 0, (byte) DataSaved.priorityNet, (byte) 0x1, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
                            MyData.push("M" + indexOfMachine + "radioMode", "0");
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{0x20, (byte) 0x73, 0x61, (byte) 0x76, (byte) 0x65, (byte) 0x61, (byte) 0x6C, (byte) 0x6C});
                        }
                        break;
                    case 1:
                    case 2:
                        //s980
                        if (DataSaved.radioMode == 0) {
                            //passa ad ntrip
                            SerialPortManager.instance().sendCommand("set,ports.radio.function,RTK_IN\r\n");
                            SerialPortManager.instance().sendCommand("set,ports.radio.enable," + "NO" + "\r\n");
                            SerialPortManager.instance().sendCommand("set,radio.reset\r\n");
                            SerialPortManager.instance().sendCommand("set,ports.reset,2\r\n");
                            SerialPortManager.instance().sendCommand("set,ports.ntrip_client.enable,YES" + "\r\n");
                            SerialPortManager.instance().sendCommand("set,ports.reset,32\r\n");
                            MyData.push("M" + indexOfMachine + "radioMode", "1");
                        } else {
                            //passa a uhf
                            SerialPortManager.instance().sendCommand("set,ports.radio.function,RTK_IN\r\n");
                            SerialPortManager.instance().sendCommand("set,ports.radio.enable," + "YES" + "\r\n");
                            SerialPortManager.instance().sendCommand("set,radio.reset\r\n");
                            SerialPortManager.instance().sendCommand("set,ports.reset,2\r\n");
                            SerialPortManager.instance().sendCommand("set,ports.ntrip_client.enable,NO" + "\r\n");
                            SerialPortManager.instance().sendCommand("set,ports.reset,32\r\n");
                            MyData.push("M" + indexOfMachine + "radioMode", "0");
                        }
                        break;

                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write2.setOnClickListener(view -> {
            if (isRead2) {


                switch (DataSaved.gpsType) {
                    case 0:
                        try {
                            chan = Integer.parseInt(etCh.getText().toString().replace(" ", ""));
                            if (chan > 0 && chan < 9) {
                                showProgress(1000);
                                statusBar.setText("Writing...");
                                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{WRITE, (byte) 0x01, (byte) chan, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});

                            } else {
                                new CustomToast(Nuovo_Gps.this, "VALID IS CH 1..8").show_alert();
                            }
                        } catch (NumberFormatException e) {
                            tostaFail(e.toString());
                        }

                        //smc
                        break;
                    case 1:
                    case 2:
                        //sc600 s980+
                        SerialPortManager.instance().sendCommand("set,radio.channel," + etCh.getText().toString() + "\r\n");
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write3.setOnClickListener(view -> {
            if (isRead3) {
                switch (DataSaved.gpsType) {
                    case 0:
                        int mInt = 0;
                        int mDec = 0;
                        try {
                            showProgress(1000);
                            statusBar.setText("Writing...");
                            mInt = Integer.parseInt(etIntero.getText().toString().replace(" ", ""));
                            mDec = Integer.parseInt(etDecimale.getText().toString().replace(" ", ""));
                            byte[] radioInt = PLC_DataTypes_LittleEndian.U16_to_bytes(mInt);
                            byte[] radioDec = PLC_DataTypes_LittleEndian.U16_to_bytes(mDec);
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{WRITE, (byte) 0x02, radioInt[0], radioInt[1], radioDec[0], radioDec[1], (byte) 0xFF, (byte) 0xFF});
                        } catch (NumberFormatException e) {
                            tostaFail(e.toString());
                        }
                        //smc
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write4.setOnClickListener(view -> {
            if (isRead4) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{WRITE, (byte) 0x03, (byte) proto, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});

                        break;
                    case 1:
                    case 2:
                        //sc600 s980+
                        SerialPortManager.instance().sendCommand("set,radio.mode," + String.valueOf(proto) + "\r\n");
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write5.setOnClickListener(view -> {
            if (isRead5) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{WRITE, (byte) 0x05, (byte) selectedSpacing, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});

                        break;
                    case 1:
                    case 2:
                        String sSpacing = "25";
                        //sc600 s980+
                        switch (selectedSpacing) {
                            case 0:
                                sSpacing = "12.5";
                                break;
                            case 1:
                                sSpacing = "25";
                                break;
                        }
                        SerialPortManager.instance().sendCommand("set,radio.channel_spacing," + sSpacing + "\r\n");
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write6.setOnClickListener(view -> {
            if (isRead6) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        if (tv3.getText().toString().isEmpty()) {
                            MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{WRITE, 0x07, (byte) 0xFF});
                        } else {
                            List<byte[]> packets = createPackets(tv3.getText().toString(), (byte) 0x07);
                            for (byte[] packet : packets) {
                                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, packet);
                            }
                        }
                        new CustomToast(this, "Read again after few seconds...").show_alert();
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write7.setOnClickListener(view -> {
            if (isRead7) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        if (tv4.getText().toString().isEmpty()) {
                            MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{WRITE, 0x08, (byte) 0xFF});
                        } else {
                            List<byte[]> packets2 = createPackets(tv4.getText().toString(), (byte) 0x08);
                            for (byte[] packet : packets2) {
                                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, packet);
                            }
                        }
                        new CustomToast(this, "Read again after few seconds...").show_alert();
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write8.setOnClickListener(view -> {
            if (isRead8) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        if (tv5.getText().toString().isEmpty()) {
                            MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{WRITE, 0x09, (byte) 0xFF});
                        } else {
                            List<byte[]> packets3 = createPackets(tv5.getText().toString(), (byte) 0x09);
                            for (byte[] packet : packets3) {
                                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, packet);
                            }
                        }
                        new CustomToast(this, "Read again after few seconds...").show_alert();
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write28.setOnClickListener(view -> {
            if (isRead28) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        try {
                            ipBytes = convertIpStringToBytes(tv28.getText().toString());
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{WRITE, 0x0A, (byte) (ipBytes[0] & 0xff), (byte) (ipBytes[1] & 0xff), (byte) (ipBytes[2] & 0xff), (byte) (ipBytes[3] & 0xff), (byte) 0xFF, (byte) 0xFF});//
                        } catch (Exception e) {
                            new CustomToast(this, "INVALID IP FORMAT").show_error();
                        }
                        new CustomToast(this, "Read again after few seconds...").show_alert();
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write38.setOnClickListener(view -> {
            if (isRead38) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        try {
                            mPort = PLC_DataTypes_LittleEndian.U32_to_bytes(Integer.parseInt(tv38.getText().toString().replace(" ", "")));
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{WRITE, 0x0B, mPort[0], mPort[1], mPort[2], mPort[3], (byte) 0xFF, (byte) 0xFF});//

                        } catch (NumberFormatException e) {
                            new CustomToast(this, "INVALID PORT VALUE").show_error();
                        }
                        new CustomToast(this, "Read again after few seconds...").show_alert();
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write9.setOnClickListener(view -> {
            if (isRead9) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        if (tv6.getText().toString().isEmpty()) {
                            MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{WRITE, 0x0E, (byte) 0xFF});
                        } else {
                            List<byte[]> packets5 = createPackets(tv6.getText().toString(), (byte) 0x0E);
                            for (byte[] packet : packets5) {
                                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, packet);
                            }
                        }
                        new CustomToast(this, "Read again after few seconds...").show_alert();
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            }
        });
        write10.setOnClickListener(view -> {
            if (isRead10) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        if (tv7.getText().toString().isEmpty()) {
                            MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{WRITE, 0x0F, (byte) 0xFF});
                        } else {
                            List<byte[]> packets6 = createPackets(tv7.getText().toString(), (byte) 0x0F);
                            for (byte[] packet : packets6) {
                                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, packet);
                            }
                        }
                        new CustomToast(this, "Read again after few seconds...").show_alert();
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write11.setOnClickListener(view -> {
            if (isRead11) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        if (tv8.getText().toString().isEmpty()) {
                            MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{WRITE, 0x0C, (byte) 0xFF});
                        } else {
                            List<byte[]> packets4 = createPackets(tv8.getText().toString(), (byte) 0x0C);
                            for (byte[] packet : packets4) {
                                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, packet);
                            }
                        }
                        new CustomToast(this, "Read again after few seconds...").show_alert();
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        write12.setOnClickListener(view -> {
            if (isRead12) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        int upl = 10;
                        try {
                            upl = Integer.parseInt(tv9.getText().toString().replace(" ", ""));
                        } catch (NumberFormatException e) {
                            upl = 10;
                        }
                        MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{WRITE, 0x0D, (byte) upl, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//

                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }
            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }
        });
        writeNet.setOnClickListener(view -> {
            if (isReadNet) {
                showProgress(1000);
                statusBar.setText("Writing...");
                switch (DataSaved.gpsType) {
                    case 0:
                        //smc
                        int redio = 0;
                        if (DataSaved.radioMode == 0) {
                            redio = 1;
                        } else {
                            redio = 0;
                        }
                        MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{WRITE, (byte) 0x11, (byte) redio, (byte) DataSaved.priorityNet, (byte) 0x1, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
                        MyData.push("M" + indexOfMachine + "priorityNet", String.valueOf(DataSaved.priorityNet));
                        break;
                    case 1:
                        //s980
                        break;
                    case 2:
                        //sc600
                        break;
                }

            } else {
                new CustomToast(this, "READ FIRST").show_alert();
            }

        });
        defaultF.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(Nuovo_Gps.this);
            builder.setTitle("SET RADIO CHANNEL FREQUENCY TO DEFAULT");
            builder.setMessage("Do You Want to Proceed ?");
            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton("YES", (dialog, which) -> {
                if (isRead3) {
                    switch (DataSaved.gpsType) {
                        case 0:
                            //smc
                            MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{WRITE, (byte) 0x01, (byte) 0xFF, (byte) 1, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
                            break;
                        case 1:
                            //s980
                            break;
                        case 2:
                            //sc600
                            SerialPortManager.instance().sendCommand("set,radio.frequency,438.125|440.125|441.125|442.125|443.125|444.125|446.125|447.125\r\n");
                            break;
                    }
                    new CustomToast(this, "FREQUENCY RESTORED TO DEFAULT").show_long();
                } else {
                    new CustomToast(this, "READ FIRST").show_alert();
                }
            });
            // Aggiungi il pulsante "No"
            builder.setNegativeButton("NO", (dialog, which) -> {
                //do nothing
            });
            // Mostra il dialog
            builder.show();

        });
        etIntero.setOnClickListener(view -> {
            if (!customNumberDialog.dialog.isShowing() && DataSaved.gpsType == 0) {
                customNumberDialog.show(etIntero, 3);
            }
        });
        etDecimale.setOnClickListener(view -> {
            if (!customNumberDialog.dialog.isShowing() && DataSaved.gpsType == 0) {
                customNumberDialog.show(etDecimale, 4);
            }
        });
        etCh.setOnClickListener(view -> {
            if (!customNumberDialog.dialog.isShowing()) {
                customNumberDialog.show(etCh);
            }
        });
        tv1.setOnClickListener(view -> {
            if (isRead4) {
                List<String> menuItems = Arrays.asList(getResources().getStringArray(R.array.radio_proto));
                CustomMenu customMenu = new CustomMenu(Nuovo_Gps.this, "RADIO PROTOCOL");
                customMenu.show(menuItems, new CustomMenu.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(String selectedItem) {
                        {
                            switch (selectedItem) {
                                case "Satel":
                                    tv1.setText("Satel");
                                    proto = 0;
                                    break;
                                case "PCC-4FSK":
                                    tv1.setText("PCC-4FSK");
                                    proto = 1;
                                    break;
                                case "PCC-GMSK":
                                    tv1.setText("PCC-GMSK");
                                    proto = 2;
                                    break;
                                case "TrimTalk 450S":
                                    tv1.setText("TrimTalk 450S");
                                    proto = 3;
                                    break;
                                case "South 9600":
                                    tv1.setText("South 9600");
                                    proto = 4;
                                    break;
                                case "HITARGET(9600)":
                                    tv1.setText("HITARGET(9600)");
                                    proto = 6;
                                    break;
                                case "HITARGET(19200)":
                                    tv1.setText("HITARGET(19200)");
                                    proto = 7;
                                    break;
                                case "TrimMark III":
                                    tv1.setText("TrimMark III");
                                    proto = 9;
                                    break;
                                case "South 19200":
                                    tv1.setText("South 19200");
                                    proto = 10;
                                    break;
                                case "TrimTalk(4800)":
                                    tv1.setText("TrimTalk(4800)");
                                    proto = 11;
                                    break;
                                case "GEOTALK":
                                    tv1.setText("GEOTALK");
                                    proto = 13;
                                    break;
                                case "GEOMARK":
                                    tv1.setText("GEOMARK");
                                    proto = 14;
                                    break;
                                case "900M Hopping":
                                    tv1.setText("900M Hopping");
                                    proto = 15;
                                    break;
                                case "HZSZ":
                                    tv1.setText("HZSZ");
                                    proto = 16;
                                    break;
                                case "GEO FHSS":
                                    tv1.setText("GEO FHSS");
                                    proto = 17;
                                    break;
                                case "Satel_ADL":
                                    tv1.setText("Satel_ADL");
                                    proto = 19;
                                    break;
                                case "PCCFST":
                                    tv1.setText("PCCFST");
                                    proto = 20;
                                    break;
                                case "PCCFST_ADL":
                                    tv1.setText("PCCFST_ADL");
                                    proto = 21;
                                    break;

                            }

                        }
                    }
                });

            }
        });
        tv2.setOnClickListener(view -> {
            if (isRead5) {
                List<String> menuItems = Arrays.asList(getResources().getStringArray(R.array.radio_spacing));
                CustomMenu customMenu = new CustomMenu(Nuovo_Gps.this, "RADIO SPACING");
                customMenu.show(menuItems, new CustomMenu.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(String selectedItem) {

                        switch (selectedItem) {

                            case "12.5":
                                tv2.setText("12.5");
                                selectedSpacing = 0;
                                break;
                            case "25":
                                tv2.setText("25");
                                selectedSpacing = 1;
                                break;

                        }
                    }

                });
            }
        });
        tv3.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(tv3);
            }
        });
        tv4.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(tv4);
            }
        });
        tv5.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(tv5);
            }
        });
        tv7.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(tv7);
            }
        });
        tv8.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(tv8);
            }
        });
        tv6.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(tv6);
            }
        });
        tv9.setOnClickListener(view -> {
            if (!customNumberDialog.dialog.isShowing()) {
                customNumberDialog.show(tv9);
            }
        });
        tv28.setOnClickListener(view -> {
            if (!customNumberDialog.dialog.isShowing()) {
                customNumberDialog.show(tv28);
            }
        });
        tv38.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(tv38);
            }
        });

    }

    private void endis(boolean b) {
        back.setEnabled(b);
        gpsStat.setEnabled(b);
        debug.setEnabled(b);
        ckSmc.setEnabled(b);
        ckS980.setEnabled(b);
        ckSc600.setEnabled(b);
        ckCan.setEnabled(b);
        ckHSL0.setEnabled(b);
        ckHSL2.setEnabled(b);
        ckWK0.setEnabled(b);
        ckDemo.setEnabled(b);
        ckRadio.setEnabled(b);
        ckNtrip.setEnabled(b);
        etCh.setEnabled(b);
        etIntero.setEnabled(b);
        etDecimale.setEnabled(b);
        tv1.setEnabled(b);
        tv2.setEnabled(b);
        tv3.setEnabled(b);
        tv4.setEnabled(b);
        tv5.setEnabled(b);
        tv6.setEnabled(b);
        tv7.setEnabled(b);
        tv8.setEnabled(b);
        tv9.setEnabled(b);
        tv28.setEnabled(b);
        tv38.setEnabled(b);
        writeNet.setEnabled(b);
        read0.setEnabled(b);
        read1.setEnabled(b);
        read2.setEnabled(b);
        read3.setEnabled(b);
        read4.setEnabled(b);
        read5.setEnabled(b);
        read6.setEnabled(b);
        read7.setEnabled(b);
        read8.setEnabled(b);
        read9.setEnabled(b);
        read10.setEnabled(b);
        read11.setEnabled(b);
        read28.setEnabled(b);
        read38.setEnabled(b);
        write0.setEnabled(b);
        write1.setEnabled(b);
        write2.setEnabled(b);
        write3.setEnabled(b);
        write4.setEnabled(b);
        write5.setEnabled(b);
        write6.setEnabled(b);
        write7.setEnabled(b);
        write8.setEnabled(b);
        write9.setEnabled(b);
        write10.setEnabled(b);
        write11.setEnabled(b);
        writeNet.setEnabled(b);
        write28.setEnabled(b);
        write38.setEnabled(b);
        saveAll.setEnabled(b);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void canEvents(CanEvents canEvents) {
        if (canEvents.id > 2047) {
            Log.d("CAN_SMC", canEvents.candata);
            switch (canEvents.id) {

                case 0x18FF0C10:

                    switch (canEvents.msg[2]) {

                        case 5:
                            DataSaved.reqSpeed = 0;
                            break;
                        case 4:
                            DataSaved.reqSpeed = 1;
                            break;
                        case 3:
                            DataSaved.reqSpeed = 2;
                            break;
                        case 2:
                        case 1:
                        case 0:
                        case (byte) 0xFF:
                            DataSaved.reqSpeed = 3;
                            break;
                    }
                    ck20.setChecked(DataSaved.reqSpeed == 0);
                    ck10.setChecked(DataSaved.reqSpeed == 1);
                    ck5.setChecked(DataSaved.reqSpeed == 2);
                    ckOff.setChecked(DataSaved.reqSpeed == 3);
                    isRead0 = true;


                    break;
                case 0x18FFA210:
                    switch (canEvents.msg[1]) {
                        case 0x07:

                            isRead6 = true;
                            synchronized (packetList) {
                                packetList.add(canEvents.msg);
                                decodePacket_APN(canEvents.msg);
                            }
                            tv3.setText(apn);

                            break;

                        case 0x08:
                            isRead7 = true;
                            synchronized (packetList) {
                                packetList.add(canEvents.msg);
                                decodePacket_ID(canEvents.msg);
                            }
                            tv4.setText(apnID);

                            break;
                        case 0x09:
                            isRead8 = true;
                            synchronized (packetList) {
                                packetList.add(canEvents.msg);
                                decodePacket_PW(canEvents.msg);
                            }
                            tv5.setText(apnPW);

                            break;
                        case 0x0C:
                            isRead11 = true;
                            synchronized (packetList) {
                                packetList.add(canEvents.msg);
                                decodePacket_MP(canEvents.msg);
                            }
                            tv8.setText(mountpoint);

                            break;

                        case 0x0E:
                            isRead9 = true;
                            synchronized (packetList) {

                                decodePacket_CID(canEvents.msg);
                                packetList.add(canEvents.msg);
                            }
                            tv6.setText(corsID);

                            break;
                        case 0x0F:
                            isRead10 = true;
                            synchronized (packetList) {
                                packetList.add(canEvents.msg);
                                decodePacket_CPW(canEvents.msg);
                            }
                            tv7.setText(corsPW);

                            break;
                    }
                case 0x18FFA110:

                    switch (canEvents.msg[1]) {
                        case 0x01:
                            isRead2 = true;
                            chan = canEvents.msg[2];
                            etCh.setText(String.valueOf(chan));

                            break;
                        case 0x02:
                            isRead3 = true;
                            intero = (PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{canEvents.msg[2], canEvents.msg[3]}));
                            decimale = (PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{canEvents.msg[4], canEvents.msg[5]}));
                            Log.d("myDecimal", decimale + " " + canEvents.msg[4] + " " + canEvents.msg[5]);
                            etIntero.setText(String.format("%03d", intero));
                            etDecimale.setText(String.format("%04d", decimale));

                            break;
                        case 0x03:

                            switch (canEvents.msg[2]) {
                                case 0:
                                    tv1.setText("Satel");
                                    break;
                                case 1:
                                    tv1.setText("PCC-4FSK");
                                    break;
                                case 2:
                                    tv1.setText("PCC-GMSK");
                                    break;
                                case 3:
                                    tv1.setText("TrimTalk 450S");
                                    break;
                                case 4:
                                    tv1.setText("South 9600");
                                    break;
                                case 6:
                                    tv1.setText("HITARGET(9600)");
                                    break;
                                case 7:
                                    tv1.setText("HITARGET(19200)");
                                    break;
                                case 9:
                                    tv1.setText("TrimMark III");
                                    break;
                                case 10:
                                    tv1.setText("South 19200");
                                    break;
                                case 11:
                                    tv1.setText("TrimTalk(4800)");
                                    break;
                                case 13:
                                    tv1.setText("GEOTALK");
                                    break;
                                case 14:
                                    tv1.setText("GEOMARK");
                                    break;
                                case 15:
                                    tv1.setText("900M Hopping");
                                    break;
                                case 16:
                                    tv1.setText("HZSZ");
                                    break;
                                case 17:
                                    tv1.setText("GEO FHSS");
                                    break;
                                case 19:
                                    tv1.setText("Satel_ADL");
                                    break;
                                case 20:
                                    tv1.setText("PCCFST");
                                    break;
                                case 21:
                                    tv1.setText("PCCFST_ADL");
                                    break;

                            }
                            proto = canEvents.msg[2];
                            isRead4 = true;
                            break;

                        case 0x05:

                            switch (canEvents.msg[2]) {

                                case 0:
                                    tv2.setText("12.5");
                                    selectedSpacing = 0;
                                    break;
                                case 1:
                                    tv2.setText("25");
                                    selectedSpacing = 1;
                                    break;

                            }
                            isRead5 = true;
                            break;
                        case 0x0A:
                            isRead28 = true;
                            corsIP = "";
                            String s = String.valueOf(canEvents.msg[2] & 0xFF);
                            String s1 = String.valueOf(canEvents.msg[3] & 0xFF);
                            String s2 = String.valueOf(canEvents.msg[4] & 0xFF);
                            String s3 = String.valueOf(canEvents.msg[5] & 0xFF);
                            corsIP = s + "." + s1 + "." + s2 + "." + s3;
                            tv28.setText(corsIP);

                            break;
                        case 0x0B:
                            isRead38 = true;
                            corsPORT = String.valueOf(PLC_DataTypes_LittleEndian.byte_to_U32(new byte[]{canEvents.msg[2], canEvents.msg[3], canEvents.msg[4], canEvents.msg[5]}));
                            tv38.setText(corsPORT);

                            break;
                        case 0x0D:
                            isRead12 = true;
                            ggaUPLOAD = String.valueOf(canEvents.msg[2]);
                            tv9.setText(ggaUPLOAD);

                            break;


                        case 0x11:
                            isRead1 = true;
                            isReadNet = true;
                            switch (canEvents.msg[2]) {

                                case 0:

                                    DataSaved.radioMode = 1;
                                    ckRadio.setChecked(false);
                                    ckNtrip.setChecked(true);

                                    MyData.push("M" + indexOfMachine + "radioMode", String.valueOf(1));
                                    break;
                                case 1:

                                    DataSaved.radioMode = 0;
                                    ckRadio.setChecked(true);
                                    ckNtrip.setChecked(false);
                                    MyData.push("M" + indexOfMachine + "radioMode", String.valueOf(0));

                                    break;
                                case (byte) 0XFF:

                                    DataSaved.radioMode = 1;
                                    ckRadio.setChecked(false);
                                    ckNtrip.setChecked(true);
                                    MyData.push("M" + indexOfMachine + "radioMode", String.valueOf(1));
                                    break;

                            }

                            switch (canEvents.msg[3]) {

                                case 0:
                                    // wired = 0;
                                    rbWired.setChecked(true);
                                    rbWifi.setChecked(false);
                                    rbModem.setChecked(false);
                                    break;
                                case 2:
                                    // wired = 2;
                                    rbWired.setChecked(false);
                                    rbWifi.setChecked(true);
                                    rbModem.setChecked(false);
                                    break;
                                case 3:
                                    //wired = 3;
                                    rbWired.setChecked(false);
                                    rbWifi.setChecked(false);
                                    rbModem.setChecked(true);
                                    break;
                            }
                            DataSaved.priorityNet = canEvents.msg[3];
                            MyData.push("M" + indexOfMachine + "priorityNet", String.valueOf(DataSaved.priorityNet));


                            break;

                    }
                    break;

            }
        }
    }

    public void decodePacket_APN(byte[] packet) {

        StringBuilder partialMessage = new StringBuilder();
        for (int i = 2; i <= 7; i++) {
            if (packet[i] == -1) {
                apn += partialMessage.toString();
                return;
            }
            partialMessage.append((char) packet[i]);
        }
        apn += partialMessage.toString();

    }

    public void decodePacket_ID(byte[] packet) {

        StringBuilder partialMessage = new StringBuilder();
        for (int i = 2; i <= 7; i++) {
            if (packet[i] == -1) {
                apnID += partialMessage.toString();
                return;
            }
            partialMessage.append((char) packet[i]);
        }
        apnID += partialMessage.toString();

    }

    public void decodePacket_PW(byte[] packet) {

        StringBuilder partialMessage = new StringBuilder();
        for (int i = 2; i <= 7; i++) {
            if (packet[i] == -1) {
                apnPW += partialMessage.toString();
                return;
            }
            partialMessage.append((char) packet[i]);
        }
        apnPW += partialMessage.toString();

    }

    public void decodePacket_MP(byte[] packet) {

        StringBuilder partialMessage = new StringBuilder();
        for (int i = 2; i <= 7; i++) {
            if (packet[i] == -1) {
                mountpoint += partialMessage.toString();
                return;
            }
            partialMessage.append((char) packet[i]);
        }
        mountpoint += partialMessage.toString();

    }

    public void decodePacket_CID(byte[] packet) {
        // Variabile statica per l'accumulazione progressiva tra i pacchetti
        StringBuilder partialMessage = new StringBuilder(corsID);

        // Ciclo attraverso i byte utili del pacchetto, ignorando i primi due [2, 14]
        for (int i = 2; i < packet.length; i++) {
            if (packet[i] == -1) { // Carattere di fine messaggio
                corsID = partialMessage.toString(); // Salva il messaggio completo
                partialMessage.setLength(0); // Reset per la prossima ricezione
                return;
            }
            // Aggiungi il byte corrente alla stringa parziale
            partialMessage.append((char) packet[i]);
        }

        // Aggiorna corsID parzialmente per i pacchetti ricevuti senza -1
        corsID = partialMessage.toString();
    }


    public void decodePacket_CPW(byte[] packet) {
        // Variabile statica per l'accumulazione progressiva tra i pacchetti
        StringBuilder partialMessage = new StringBuilder(corsPW);

        // Ciclo attraverso i byte utili del pacchetto, ignorando i primi due [2, 14]
        for (int i = 2; i < packet.length; i++) {
            if (packet[i] == -1) { // Carattere di fine messaggio
                corsPW = partialMessage.toString(); // Salva il messaggio completo
                partialMessage.setLength(0); // Reset per la prossima ricezione
                return;
            }
            // Aggiungi il byte corrente alla stringa parziale
            partialMessage.append((char) packet[i]);
        }

        // Aggiorna corsID parzialmente per i pacchetti ricevuti senza -1
        corsPW = partialMessage.toString();
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void cmdEvent(CMD_Event cmdEvent) {
        if (cmdEvent.cmdIn.startsWith("@")) {


            stato = stato + "\n" + (cmdEvent.cmdIn);

            if (DataSaved.my_comPort > 0) {
                //bt connection to SC600
                cmdStatust = cmdEvent.cmdIn;
                String[] substrings = cmdStatust.split("\\|");
                try {
                    ///
                    if (DataSaved.gpsType > 0) {
                        for (String substring : substrings) {
                            if (substring.contains("OK,GGA:")) {
                                String fgga = substring.substring(substring.indexOf(":") + 1);

                            } else if (substring.startsWith("HDT:")) {
                                String fhdt = substring.substring(substring.indexOf(":") + 1);

                            } else if (substring.startsWith("PJK:")) {
                                String fpjk = substring.substring(substring.indexOf(":") + 1);

                            } else if (substring.startsWith("GST:")) {
                                String fgst = substring.substring(substring.indexOf(":") + 1);

                            } else if (substring.startsWith("RMC:")) {
                                String frmc = substring.substring(substring.indexOf(":") + 1);

                            } else if (substring.startsWith("LLQ:")) {
                                String fllq = substring.substring(substring.indexOf(":") + 1);

                            }

                        }
                    }
                    ///
                } catch (Exception e) {

                }
                try {
                    if (DataSaved.gpsType > 0) {
                        String[] substringsR = cmdStatust.split(",");
                        if (substringsR[2].equals("ports.radio.enable")) {

                            String radioEnA = substringsR[4].substring(0, substringsR[4].indexOf("*"));
                            switch (radioEnA) {
                                case "YES":
                                    ckRadio.setChecked(true);
                                    ckNtrip.setChecked(false);
                                    break;
                                case "NO":
                                    ckRadio.setChecked(false);
                                    ckNtrip.setChecked(true);
                                    break;
                            }
                            isRead1 = true;

                        } else if (substringsR[2].equals("radio.channel") && substringsR[3].equals("OK")) {
                            sCanale = substringsR[4].substring(0, substringsR[4].indexOf("*"));
                            etCh.setText(sCanale);
                            isRead2 = true;
                        } else if (substringsR[2].equals("radio.frequency") && substringsR[3].equals("OK")) {
                            String beforeAsterisk = substringsR[4].split("\\*")[0];
                            // Splittare la stringa usando "|"
                            String[] array = beforeAsterisk.split("\\|");
                            int inice = Integer.parseInt(sCanale);
                            String freqenza = array[inice - 1];
                            String[] parts = freqenza.split("\\.");


                            etIntero.setText(parts[0]);
                            etDecimale.setText(parts[1]);
                            isRead3 = true;
                           /*
                           @GNSS,get,radio.frequency,OK,438.125|439.125|440.125|441.125|442.125|443.125|444.125|445.125*C1
                            */
                        } else if (substringsR[2].equals("radio.mode") && substringsR[3].equals("OK")) {
                            String sProto = substringsR[4].substring(0, substringsR[4].indexOf("*"));

                            switch (sProto) {
                                case "0":
                                    tv1.setText("Satel");
                                    proto = 0;
                                    break;
                                case "1":
                                    tv1.setText("PCC-4FSK");
                                    proto = 1;
                                    break;
                                case "2":
                                    tv1.setText("PCC-GMSK");
                                    proto = 2;
                                    break;
                                case "3":
                                    tv1.setText("TrimTalk 450S");
                                    proto = 3;
                                    break;
                                case "4":
                                    tv1.setText("South 9600");
                                    proto = 4;
                                    break;
                                case "6":
                                    tv1.setText("HITARGET(9600)");
                                    proto = 6;
                                    break;
                                case "7":
                                    tv1.setText("HITARGET(19200)");
                                    proto = 7;
                                    break;
                                case "9":
                                    tv1.setText("TrimMark III");
                                    proto = 9;
                                    break;
                                case "10":
                                    tv1.setText("South 19200");
                                    proto = 10;
                                    break;
                                case "11":
                                    tv1.setText("TrimTalk(4800)");
                                    proto = 11;
                                    break;
                                case "13":
                                    tv1.setText("GEOTALK");
                                    proto = 13;
                                    break;
                                case "14":
                                    tv1.setText("GEOMARK");
                                    proto = 14;
                                    break;
                                case "15":
                                    tv1.setText("900M Hopping");
                                    proto = 15;
                                    break;
                                case "16":
                                    tv1.setText("HZSZ");
                                    proto = 16;
                                    break;
                                case "17":
                                    tv1.setText("GEO FHSS");
                                    proto = 17;
                                    break;
                                case "19":
                                    tv1.setText("Satel_ADL");
                                    proto = 19;
                                    break;
                                case "20":
                                    tv1.setText("PCCFST");
                                    proto = 20;
                                    break;
                                case "21":
                                    tv1.setText("PCCFST_ADL");
                                    proto = 21;
                                    break;

                            }
                            isRead4 = true;

                        } else if (substringsR[2].equals("radio.channel_spacing") && substringsR[3].equals("OK")) {
                            String sSpacing = substringsR[4].substring(0, substringsR[4].indexOf("*"));
                            tv2.setText(sSpacing);
                            switch (sSpacing) {
                                case "12.5":
                                    selectedSpacing = 0;
                                    break;
                                case "25":
                                    selectedSpacing = 1;
                                    break;
                            }
                            isRead5 = true;
                        } else if (substringsR[2].equals("ports.radio.function") && substringsR[3].equals("OK")) {//verificare come compare la stringa
                            String sRadioF = substringsR[4].substring(0, substringsR[4].indexOf("*"));
                        }

                    }
                } catch (Exception e) {
                    stato = e.toString() + "\n";
                }


            }
        }
    }

    private void allReading() {
        showProgress(9000);
        final Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < 16; i++) {
            final int finalI = i;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    statusBar.setText("WAIT\nREADING DATA..." + Math.min(finalI * 7, 100) + "%");
                    switch (finalI) {
                        case 0:
                            mRead0();
                            break;
                        case 1:
                            mRead1();
                            break;
                        case 2:
                            mRead2();
                            break;
                        case 3:
                            mRead3();
                            break;
                        case 4:
                            mRead4();
                            break;
                        case 5:
                            mRead5();
                            break;
                        case 6:
                            mRead6();
                            break;
                        case 7:
                            mRead7();
                            break;
                        case 8:
                            mRead8();
                            break;
                        case 9:
                            mRead28();
                            break;
                        case 10:
                            mRead38();
                            break;
                        case 11:
                            mRead9();
                            break;
                        case 12:
                            mRead10();
                            break;
                        case 13:
                            mRead11();
                            break;
                        case 14:
                            mRead12();
                            break;

                    }
                }
            }, 500 * i); // Ritardo di 500 millisecondi fra un msg e l'altro
        }
    }

    private void showProgress(long delay) {
        statusBar.setVisibility(View.VISIBLE);
        progressBar2.setVisibility(View.VISIBLE);
        endis(false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (progressBar2.getVisibility() == View.VISIBLE) {
                progressBar2.setVisibility(View.INVISIBLE);
                statusBar.setVisibility(View.INVISIBLE);
                statusBar.setText("");
                endis(true);
            }
        }, delay); // 1000 ms di ritardo
    }


    private void tostaFail(String s) {
        new CustomToast(Nuovo_Gps.this, s).show_error();
    }

    private void mRead0() {
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF0001, 4, new byte[]{READ, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
                break;
            case 1:
                //s980
                SerialPortManager.instance().sendCommand("get,device.loglist,gga|hdt|gst|llq\r\n");//LLQ must be added

                break;
            case 2:
                //sc600
                SerialPortManager.instance().sendCommand("get,ports.com3.nmea,gga|pjk|hdt|gst|llq\r\n");
                break;
        }
        init();
    }

    private void mRead1() {
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{READ, 0x11, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//NTRIP PSW REQUEST
                break;
            case 1:
                //s980
                SerialPortManager.instance().sendCommand("get,radio.status\r\n");
                break;
            case 2:
                //sc600
                SerialPortManager.instance().sendCommand("get,ports.radio.enable\r\n");
                break;
        }
        init();
    }

    private void mRead2() {
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{READ, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//READ RADIO CH
                break;
            case 1:
                //s980
                SerialPortManager.instance().sendCommand("get,radio.channel\r\n");
                break;
            case 2:
                //sc600
                SerialPortManager.instance().sendCommand("get,radio.channel\r\n");
                break;
        }
    }

    private void mRead3() {
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{READ, 0x02, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//READ RADIO FRQ
                break;
            case 1:
                //s980
                SerialPortManager.instance().sendCommand("get,radio.frequency\r\n");
                break;
            case 2:
                //sc600
                SerialPortManager.instance().sendCommand("get,radio.frequency\r\n");
                break;
        }
    }

    private void mRead4() {
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{READ, 0x03, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//READ RADIO PROTO
                break;
            case 1:
                //s980
                SerialPortManager.instance().sendCommand("get,radio.mode\r\n");
                break;
            case 2:
                //sc600
                SerialPortManager.instance().sendCommand("get,radio.mode\r\n");
                break;
        }
    }

    private void mRead5() {
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{READ, 0x05, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//READ RADIO CHN SPACING
                break;
            case 1:
                //s980
                SerialPortManager.instance().sendCommand("get,radio.channel_spacing\r\n");
                break;
            case 2:
                //sc600
                SerialPortManager.instance().sendCommand("get,radio.channel_spacing\r\n");
                break;
        }
    }

    private void mRead6() {
        apn = "";
        tv3.setText("");
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{READ, 0x07, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//APN REQUEST
                break;
            case 1:
                //s980
                break;
            case 2:
                //sc600
                break;
        }
    }

    private void mRead7() {
        apnID = "";
        tv4.setText("");
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{READ, 0x08, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//APN USER REQ

                break;
            case 1:
                //s980
                break;
            case 2:
                //sc600
                break;
        }
    }

    private void mRead8() {
        apnPW = "";
        tv5.setText("");
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{READ, 0x09, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//APN PSW REQ
                break;
            case 1:
                //s980
                break;
            case 2:
                //sc600
                break;
        }
    }

    private void mRead28() {
        corsIP = "";
        tv28.setText("");
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{READ, 0x0A, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//IP REQ
                break;
            case 1:
                //s980
                break;
            case 2:
                //sc600
                break;
        }
    }

    private void mRead38() {
        corsPORT = "";
        tv38.setText("");
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{READ, 0x0B, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//PORT REQUEST
                break;
            case 1:
                //s980
                break;
            case 2:
                //sc600
                break;
        }
    }

    private void mRead9() {
        corsID = "";
        tv6.setText("");
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{READ, 0x0E, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//NTRIP USER REQUEST
                break;
            case 1:
                //s980
                break;
            case 2:
                //sc600
                break;
        }

    }

    private void mRead10() {
        corsPW = "";
        tv7.setText("");
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{READ, 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//NTRIP PSW REQUEST
                break;
            case 1:
                //s980
                break;
            case 2:
                //sc600
                break;
        }
    }

    private void mRead11() {
        mountpoint = "";
        tv8.setText("");
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF2A01, 8, new byte[]{READ, 0x0C, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//MOUNT POINT REQUEST
                break;
            case 1:
                //s980
                break;
            case 2:
                //sc600
                break;
        }
    }

    private void mRead12() {
        ggaUPLOAD = "";
        tv9.setText("");
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{READ, 0x0D, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//UPLOAD GGA REQUEST
                break;
            case 1:
                //s980
                break;
            case 2:
                //sc600
                break;
        }
    }

    private void mReadNet() {
        switch (DataSaved.gpsType) {
            case 0:
                //smc
                MyDeviceManager.CanWrite(0, 0x18FF1A01, 8, new byte[]{READ, 0x11, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});//NTRIP PSW REQUEST
                break;
            case 1:
                //s980

                break;
            case 2:
                //sc600

                break;
        }
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void openWebUI() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true); // Abilita JavaScript
        webView.getSettings().setDomStorageEnabled(true); // Abilita DOM Storage
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // Abilita pop-up
        webView.getSettings().setSupportMultipleWindows(true); // Supporta finestre multiple
        webView.getSettings().setLoadWithOverviewMode(true); // Carica contenuti in modalità panoramica
        webView.getSettings().setBuiltInZoomControls(true); // Abilita controlli di zoom
        webView.getSettings().setDisplayZoomControls(false); // Nascondi controlli di zoom visibili
        webView.getSettings().setUseWideViewPort(true); // Abilita la modalità panoramica
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 (Desktop)");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                switch (DataSaved.gpsType) {
                    case 0:
                    case 2:
                        webView.loadUrl("javascript:document.getElementById('username').value = 'admin';" +
                                "document.getElementById('password1').value = 'password';" +
                                "document.getElementById('submit').onclick();");
                        break;
                    case 1:
                        webView.loadUrl("javascript:document.getElementById('user').value = 'admin';" +
                                "document.getElementById('passwd').value = 'password';" +
                                "document.querySelector('.btn.btn-primary.btn-wd').click()");
                        break;

                }
            }
        });

        // Carica la pagina di login
        webView.loadUrl("http://192.168.10.1/login.php");

    }


    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean isKeyboardVisible(View rootView) {
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int screenHeight = rootView.getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;

        // La tastiera è visibile se l'altezza è maggiore del 15% dell'altezza dello schermo
        return keypadHeight > screenHeight * 0.15;
    }


    private void openFileChooser(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        fileCallback = filePathCallback;

        // Intent per aprire il file manager
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // Permette di selezionare qualsiasi tipo di file

        // Filtra i MIME type specificati se presenti
        String[] acceptTypes = fileChooserParams.getAcceptTypes();
        if (acceptTypes != null && acceptTypes.length > 0) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes);
        }

        // Specifica un'app specifica come file manager
        intent.setPackage("com.alphainventor.filemanager"); // Cambia con il package dell'app che preferisci

        try {
            startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            new CustomToast(this, "File Manager+ not Found").show_error();
        }
        //startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    // Gestione del risultato della selezione del file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null && fileCallback != null) {
                Uri[] results = new Uri[]{data.getData()};
                fileCallback.onReceiveValue(results);
            } else {
                if (fileCallback != null) {
                    fileCallback.onReceiveValue(null); // Nessun file selezionato
                }
            }
            fileCallback = null; // Resetta il callback
        }
    }

    private void showLayouts(int v) {
        lay1.setVisibility(v);
        lay2.setVisibility(v);
        lay3.setVisibility(v);
        lay4.setVisibility(v);
        lay5.setVisibility(v);
        lay6.setVisibility(v);
        lay7.setVisibility(v);
        lay8.setVisibility(v);
        lay9.setVisibility(v);
        lay10.setVisibility(v);
        lay11.setVisibility(v);
        lay12.setVisibility(v);
        lay13.setVisibility(v);
        lay14.setVisibility(v);
        lay15.setVisibility(v);
        lay16.setVisibility(v);
        txtlay1.setVisibility(v);
        txtlay2.setVisibility(v);
        txtlay3.setVisibility(v);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}