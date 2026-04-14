package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;
import utils.WifiHelper;

public class Dialog_SSID {

    Activity activity;
    public Dialog dialog;
    EditText editText;
    Button button;
    CustomQwertyDialog customQwertyDialog;

    public Dialog_SSID(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_ssid, null));

        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
    }

    private void findView() {
        editText = dialog.findViewById(R.id.etssid);
        button = dialog.findViewById(R.id.save);
        customQwertyDialog = new CustomQwertyDialog(activity, null);

    }

    private void init() {
        editText.setText(DataSaved.wifiSSID.trim());

    }

    private void onClick() {
        editText.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(editText);
            }
        });
        button.setOnClickListener(view -> {
            MyData.push("wifiSSID", editText.getText().toString().trim());
            DataSaved.wifiSSID = editText.getText().toString().trim();

            WifiHelper.connectToWifi(activity, DataSaved.wifiSSID, "", true);
            activity.recreate();
            dialog.dismiss();
        });
    }
}
