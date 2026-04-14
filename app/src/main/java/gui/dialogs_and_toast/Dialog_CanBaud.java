package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_CanBaud {

    public Dialog dialog;
    Activity activity;
    int flag;
    TextView textView;
    RadioButton rb1, rb2, rb3, rb4;

    public Dialog_CanBaud(Activity activity) {
        this.activity = activity;

        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show(int flag) {
        this.flag = flag;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_can_setup, null));
        builder.setCancelable(true);
        dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        try {
            DataSaved.boudrateCAN1 = MyData.get_Int("canBaud1");
            DataSaved.boudrateCAN2 = MyData.get_Int("canBaud2");
        } catch (Exception ignored) {

        }
        FullscreenActivity.setFullScreen(dialog);
        textView = dialog.findViewById(R.id.itolo);
        rb1 = dialog.findViewById(R.id.rb1);
        rb2 = dialog.findViewById(R.id.rb2);
        rb3 = dialog.findViewById(R.id.rb3);
        rb4 = dialog.findViewById(R.id.rb4);
        switch (flag) {
            case 1:
                textView.setText("CAN 1 Baudrate");
                setCan1();
                rb1.setEnabled(false);
                rb2.setEnabled(false);
                rb3.setEnabled(false);
                rb4.setEnabled(false);
                new CustomToast(activity, "Primary CAN baudrate cannot be changed").show_long();
                break;

            case 2:
                textView.setText("CAN 2 Baudrate");
                setCan2();
                break;
        }
        onClick();
    }

    private void onClick() {
        rb1.setOnClickListener(view -> {
            if (flag == 2) {
                MyData.push("canBaud" + flag, "125000");
                dialog.dismiss();
            } else {
                new CustomToast(activity, "Primary CAN baudrate cannot be changed").show_alert();
            }
        });
        rb2.setOnClickListener(view -> {
            if (flag == 2) {
                MyData.push("canBaud" + flag, "250000");
                dialog.dismiss();
            } else {
                new CustomToast(activity, "Primary CAN baudrate cannot be changed").show_alert();
            }
        });
        rb3.setOnClickListener(view -> {
            if (flag == 2) {
                MyData.push("canBaud" + flag, "500000");
                dialog.dismiss();
            } else {
                new CustomToast(activity, "Primary CAN baudrate cannot be changed").show_alert();
            }

        });
        rb4.setOnClickListener(view -> {
            if (flag == 2) {
                MyData.push("canBaud" + flag, "1000000");
                dialog.dismiss();
            } else {
                new CustomToast(activity, "Primary CAN baudrate cannot be changed").show_alert();
            }

        });
    }

    private void setCan1() {
        switch (DataSaved.boudrateCAN1) {
            case 125000:
                rb1.setChecked(true);
                rb2.setChecked(false);
                rb3.setChecked(false);
                rb4.setChecked(false);
                break;
            case 250000:
                rb1.setChecked(false);
                rb2.setChecked(true);
                rb3.setChecked(false);
                rb4.setChecked(false);
                break;
            case 500000:
                rb1.setChecked(false);
                rb2.setChecked(false);
                rb3.setChecked(true);
                rb4.setChecked(false);
                break;
            case 1000000:
                rb1.setChecked(false);
                rb2.setChecked(false);
                rb3.setChecked(false);
                rb4.setChecked(true);
                break;

        }
    }

    private void setCan2() {
        switch (DataSaved.boudrateCAN2) {
            case 125000:
                rb1.setChecked(true);
                rb2.setChecked(false);
                rb3.setChecked(false);
                rb4.setChecked(false);
                break;
            case 250000:
                rb1.setChecked(false);
                rb2.setChecked(true);
                rb3.setChecked(false);
                rb4.setChecked(false);
                break;
            case 500000:
                rb1.setChecked(false);
                rb2.setChecked(false);
                rb3.setChecked(true);
                rb4.setChecked(false);
                break;
            case 1000000:
                rb1.setChecked(false);
                rb2.setChecked(false);
                rb3.setChecked(false);
                rb4.setChecked(true);
                break;

        }
    }
}
