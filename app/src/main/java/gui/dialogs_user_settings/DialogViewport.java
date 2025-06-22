package gui.dialogs_user_settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.MyApp;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class DialogViewport {
    Activity activity;
    public Dialog alertDialog;
    CheckBox cbx1D, cbx2D, cbx3D;
    TextView titolo_L;
    Button save, exit;
    int index = 0;
    String buildBrand = "";


    public DialogViewport(Activity activity) {
        this.activity = activity;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_viewport, null));

        builder.setCancelable(false);
        alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        alertDialog.show();
        buildBrand = Build.BRAND;
        if (MyData.get_String("BUILD").equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO")||Build.BRAND.equals("APOLLO2_12_PLUS")) {

            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            alertDialog.getWindow().setLayout(900, 400);
        }
        FullscreenActivity.setFullScreen(alertDialog);
        findView();
        init();
        onClick();
        onCheckedChange();
    }

    private void findView() {
        titolo_L = alertDialog.findViewById(R.id.titolo_l);
        cbx1D = alertDialog.findViewById(R.id.cbx1D);
        cbx2D = alertDialog.findViewById(R.id.cbx2D);
        cbx3D = alertDialog.findViewById(R.id.cbx3D);
        save = alertDialog.findViewById(R.id.save);
        exit = alertDialog.findViewById(R.id.exit);
    }

    private void init() {

        switch (MyApp.KEY_LEVEL) {
            case 1:
                titolo_L.setText("STX MC 1D");
                cbx1D.setEnabled(true);
                cbx2D.setEnabled(false);
                cbx3D.setEnabled(false);
                cbx1D.setAlpha(1f);
                cbx2D.setAlpha(0.3f);
                cbx3D.setAlpha(0.3f);
                break;
            case 2:
                titolo_L.setText("STX MC 1D / 2D");
                cbx1D.setEnabled(true);
                cbx2D.setEnabled(true);
                cbx3D.setEnabled(false);
                cbx1D.setAlpha(1f);
                cbx2D.setAlpha(1f);
                cbx3D.setAlpha(0.3f);
                break;
            case 3:
                titolo_L.setText("STX MC 1D / 2D / 3D Easy");
                cbx3D.setText("3D Easy");
                cbx1D.setEnabled(true);
                cbx2D.setEnabled(true);
                cbx3D.setEnabled(true);
                cbx1D.setAlpha(1f);
                cbx2D.setAlpha(1f);
                cbx3D.setAlpha(1f);
                break;
            case 4:
                titolo_L.setText("STX MC 1D / 2D / 3D Pro");
                cbx3D.setText("3D Pro");
                cbx1D.setEnabled(true);
                cbx2D.setEnabled(true);
                cbx3D.setEnabled(true);
                cbx1D.setAlpha(1f);
                cbx2D.setAlpha(1f);
                cbx3D.setAlpha(1f);
                break;


        }


        index = MyData.get_Int("indexView");
        switch (index) {
            case 0:
                cbx1D.setChecked(true);
                cbx2D.setChecked(false);
                cbx3D.setChecked(false);
                break;
            case 1:
                cbx1D.setChecked(false);
                cbx2D.setChecked(true);
                cbx3D.setChecked(false);
                break;
            case 2:
                cbx1D.setChecked(false);
                cbx2D.setChecked(false);
                cbx3D.setChecked(true);
                break;

        }
    }

    private void updateCheck() {
        switch (index) {
            case 0:
                cbx1D.setChecked(true);
                cbx2D.setChecked(false);
                cbx3D.setChecked(false);
                break;
            case 1:
                cbx1D.setChecked(false);
                cbx2D.setChecked(true);
                cbx3D.setChecked(false);
                break;
            case 2:
                cbx1D.setChecked(false);
                cbx2D.setChecked(false);
                cbx3D.setChecked(true);
                break;

        }
    }

    private void onClick() {
        exit.setOnClickListener((View v) -> {
            alertDialog.dismiss();
        });

        save.setOnClickListener((View v) -> {
            MyData.push("indexView", String.valueOf(index));
            DataSaved.portView = index;
            if (DataSaved.portView >= 2) {
                MyData.push("ProfileSelected", String.valueOf(0));
                MyData.push("shortcutIndex", "7");
                DataSaved.profileSelected = 0;
            } else {
                MyData.push("shortcutIndex", "1");
            }
            alertDialog.dismiss();
        });
    }

    private void onCheckedChange() {
        cbx1D.setOnClickListener(view -> {
            index = 0;
            updateCheck();
        });

        cbx2D.setOnClickListener(view -> {
            index = 1;
            updateCheck();
        });


        cbx3D.setOnClickListener(view -> {
            index = 2;
            updateCheck();
        });


    }
}
