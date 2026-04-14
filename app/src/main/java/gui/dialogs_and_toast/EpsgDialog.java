package gui.dialogs_and_toast;

import static packexcalib.gnss.CRS_Strings._NONE;
import static packexcalib.gnss.CRS_Strings._UTM;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.stx_dig.R;

import java.lang.reflect.Field;
import java.util.ArrayList;

import gui.MyApp;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.CRS_Strings;
import serial.SerialPortManager;
import services.CanSender;
import services.UpdateValuesService;
import utils.FullscreenActivity;
import utils.MyData;
import utils.MyDeviceManager;
import utils.MyEpsgNumber;

public class EpsgDialog {
    Activity activity;
    public Dialog dialog;
    Button epsgNumber;
    ListView listView;
    EditText epsgSearcher;
    Button save, exit;
    CustomQwertyDialog customQwertyDialog;
    String strDiRicerca = "";
    Handler handler;
    boolean mRunning = true;
    ArrayAdapter<String> arrayAdapter;
    String[] datas;
    ArrayList<String> epsgList;
    boolean flag = true;
    String selectedFromList;
    TextView realB;

    public EpsgDialog(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);

        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.setContentView(R.layout.dialog_epsg);
        } else {
            dialog.setContentView(R.layout.dialog_epsg_7);
        }
        customQwertyDialog = new CustomQwertyDialog(activity, null);
    }

    public void show(TextView textView) {
        realB = textView;
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.getWindow().setLayout(1100, 650);
        } else {
            dialog.getWindow().setLayout(800, 550);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        checker();
    }

    @SuppressLint("SetTextI18n")
    private void onClick() {
        epsgSearcher.setOnClickListener((View v) -> {
            if (!customQwertyDialog.dialog.isShowing())
                customQwertyDialog.show(epsgSearcher);
        });

        save.setOnClickListener((View v) -> {

            String crs = epsgNumber.getText().toString().replace("EPSG: ", "");
            setupGNSS(crs);
            realB.setText(crs + "\n" + CRS_Strings.description(MyData.get_String("crs")));
            MyData.push("crs", crs);
            DataSaved.S_CRS = MyData.get_String("crs");
            activity.startService(new Intent(activity, UpdateValuesService.class));
            mRunning = false;
            activity.recreate();
            dialog.dismiss();
        });

        exit.setOnClickListener((View v) -> {
            mRunning = false;
            dialog.dismiss();
        });

        listView.setOnItemClickListener((myAdapter, myView, myItemInt, mylng) -> {
            selectedFromList = (String) (listView.getItemAtPosition(myItemInt));
            try {
                Log.d("CHE", "" + selectedFromList);
                if (selectedFromList.equals(_UTM)) {
                    epsgNumber.setText(_UTM);
                } else if (selectedFromList.equals(_NONE)) {
                    epsgNumber.setText(_NONE);
                } else {
                    epsgNumber.setText("EPSG: " + MyEpsgNumber.class.getField(selectedFromList).getInt(selectedFromList));
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void findView() {
        epsgNumber = dialog.findViewById(R.id.epsgNumber);
        listView = dialog.findViewById(R.id.listView);
        epsgSearcher = dialog.findViewById(R.id.searcher);
        save = dialog.findViewById(R.id.save);
        exit = dialog.findViewById(R.id.exit);


    }

    @SuppressLint("SetTextI18n")
    private void init() {


        epsgNumber.setText("EPSG: " + realB.getText().toString());

        Class<MyEpsgNumber> clazz = MyEpsgNumber.class;
        Field[] methods = clazz.getFields();
        epsgList = new ArrayList<>();
        epsgList.add(_NONE);
        epsgList.add(_UTM);
        try {
            for (Field method : methods) {
                epsgList.add(method.getName());
            }

        } catch (Exception ignored) {
        }
        strDiRicerca = epsgSearcher.getText().toString();

    }

    @SuppressLint("SetTextI18n")
    private void checker() {
        handler = new Handler();
        new Thread(() -> {
            while (mRunning) {

                handler.post(() -> {

                    if (epsgSearcher.getText().toString().equals("") && flag) {

                        strDiRicerca = epsgSearcher.getText().toString();
                        datas = new String[epsgList.size()];

                        for (int i = 0; i < epsgList.size(); i++) {
                            datas[i] = epsgList.get(i);
                        }

                        arrayAdapter = new ArrayAdapter<String>(activity, R.layout.epsg_listview, R.id.textView, datas);

                        listView.setAdapter(arrayAdapter);

                        flag = false;
                    }

                    if (!epsgSearcher.getText().toString().equals(strDiRicerca) && !epsgSearcher.getText().toString().equals("")) {
                        flag = true;
                        strDiRicerca = epsgSearcher.getText().toString();

                        datas = new String[epsgList.size()];

                        for (int i = 0; i < epsgList.size(); i++) {
                            datas[i] = epsgList.get(i);
                        }

                        ArrayList<String> tmp = new ArrayList<>();
                        for (String data : datas) {
                            if (data.contains(strDiRicerca)) {
                                tmp.add(data);
                            }
                        }
                        datas = new String[tmp.size()];
                        for (int i = 0; i < tmp.size(); i++) {
                            datas[i] = tmp.get(i);
                        }

                        arrayAdapter = new ArrayAdapter<String>(activity, R.layout.epsg_listview, R.id.textView, datas);


                        listView.setAdapter(arrayAdapter);
                    }


                });
                // sleep per intervallo update UI
                try {
                    Thread.sleep(MyApp.timeUI);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
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


        MyDeviceManager.CanWrite(true, 0, 0x18FF0001, 4, new byte[]{0x20, CanSender.GNSS_MSG, speed, (byte) 0x03});
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
