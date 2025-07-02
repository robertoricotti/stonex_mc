package gui.dialogs_and_toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.stx_dig.R;

import utils.FullscreenActivity;

public class InformationsDialog {
    Activity activity;
   public Dialog alertDialog;
    Button ok;
    TextView title;
    String msg;




    public InformationsDialog(Activity activity, String msg) {
        this.activity = activity;
        this.msg = msg;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.informations_dialog, null));
        builder.setCancelable(true);
        alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();

        alertDialog.show();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        FullscreenActivity.setFullScreen(alertDialog);
        findView();
        onClick();

    }

    @SuppressLint("SetTextI18n")
    private void findView(){
        ok = alertDialog.findViewById(R.id.ok);
        title = alertDialog.findViewById(R.id.title);
        title.setText(msg);
    }

    private void onClick() {
        ok.setOnClickListener((View v) -> {
            alertDialog.dismiss();

        });
    }

}
