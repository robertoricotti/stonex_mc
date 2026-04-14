package gui.dialogs_and_toast;

import static gui.MyApp.isApollo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.stx_dig.R;

import utils.FullscreenActivity;

public class SensorAlertDialog {
    Activity activity;
    public Dialog alertDialog;
    Button ok;
    TextView title;
    String sensor;


    public SensorAlertDialog(Activity activity, String sensor) {
        this.activity = activity;
        this.sensor = sensor;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        if (isApollo) {
            builder.setView(inflater.inflate(R.layout.dialog_sensor_alert, null));
        } else {
            builder.setView(inflater.inflate(R.layout.dialog_sensor_alert_s80, null));
        }
        builder.setCancelable(false);
        alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        alertDialog.show();
        FullscreenActivity.setFullScreen(alertDialog);
        findView();
        onClick();

    }

    @SuppressLint("SetTextI18n")
    private void findView() {
        ok = alertDialog.findViewById(R.id.ok);
        title = alertDialog.findViewById(R.id.title);
        title.setText(sensor);
    }

    private void onClick() {
        ok.setOnClickListener((View v) -> {
            alertDialog.dismiss();
        });
    }

}
