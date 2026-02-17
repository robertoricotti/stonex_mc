package gui.dialogs_and_toast;

import static gui.MyApp.isApollo;
import static services.UpdateValuesService.startedService;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.stx_dig.R;

import java.io.DataOutputStream;

import serial.OpenSerialPort;
import serial.SerialPortManager;
import services.CanSender;
import services.CanService;
import services.TriangleService;
import utils.CPCanHelper;
import utils.FullscreenActivity;
import utils.MyData;
import utils.MyDeviceManager;


public class CloseAppDialog {
    Activity activity;
    public Dialog alertDialog;
    Button yes, no, poweroff, reboot;


    public CloseAppDialog(Activity activity) {
        this.activity = activity;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_close_app, null));
        builder.setCancelable(true);
        alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }

        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        alertDialog.show();

        findView();
        if (startedService) {
            onClick();
        }
        FullscreenActivity.setFullScreen(alertDialog);
    }

    private void findView() {
        yes = alertDialog.findViewById(R.id.yes);
        no = alertDialog.findViewById(R.id.no);
        poweroff = alertDialog.findViewById(R.id.shutdown);
        reboot = alertDialog.findViewById(R.id.reboot);
        if (isApollo) {
            poweroff.setVisibility(View.GONE);
            reboot.setVisibility(View.GONE);
            if (true) {
                yes.setVisibility(View.VISIBLE);
            } else {
                yes.setVisibility(View.GONE);
            }
        } else {
            yes.setVisibility(View.VISIBLE);
            poweroff.setVisibility(View.GONE);
            reboot.setVisibility(View.GONE);
        }
    }

    private void onClick() {


        yes.setOnClickListener((View v) -> {

            try {
                CPCanHelper.getInstance().disconnectAll();
                activity.stopService(new Intent(activity, CanService.class));
                activity.stopService(new Intent(activity, CanSender.class));
                activity.stopService(new Intent(activity, TriangleService.class));
                OpenSerialPort.mOpened = false;
                SerialPortManager.instance().close();



            } catch (Exception e) {
                e.printStackTrace();
            }

            MyDeviceManager.showBar(activity);
            MyDeviceManager.OUT1(activity, 0);
            MyDeviceManager.OUT2(activity, 0);
            if(Build.BRAND.equals("MEGA_1")) {
                enableAdcKeys();
            }

            MyData.push("machinestate", "1");
            alertDialog.dismiss();
            activity.finishAndRemoveTask();
            // Chiudi l'applicazione e tutti i suoi processi
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);

        });

        no.setOnClickListener((View v) -> {
            alertDialog.dismiss();
        });
    }


    private void enableAdcKeys() {
        new Thread(() -> {
            try {
                Process su = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(su.getOutputStream());

                //  Riabilita TUTTI gli adc-keys dinamicamente
                os.writeBytes(
                        "sh -c 'for f in /sys/devices/platform/adc-keys/input/*/inhibited; do echo 0 > $f; done'\n"
                );

                os.writeBytes("sync\n");
                os.writeBytes("exit\n");
                os.flush();

                su.waitFor();

            } catch (Exception e) {
                Log.e("ADC_KEYS", Log.getStackTraceString(e));
            }
        }).start();
    }

}
