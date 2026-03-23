package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.example.stx_dig.BuildConfig;
import com.example.stx_dig.R;

import cloud.WebSocketPlugin;
import gui.MyApp;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;


public class Dialog_InfoApp {
    Activity activity;
    public Dialog dialog;
    ImageView close;
    TextView textView;

    public Dialog_InfoApp(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_infoapp);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); //  Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.75);
        int height = (int) (displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();

    }

    private void findView() {
        close = dialog.findViewById(R.id.chiudi);
        textView = dialog.findViewById(R.id.testo);
        String s = "";
        try {
            s = MyData.get_String("progettoSelected");
            s = s.replace("/storage/emulated/0/StonexMC_V4", "");
            s = s.substring(0, s.lastIndexOf("/"));
        } catch (Exception e) {
            s = "";
        }


        String espir="No Limit";
        if(MyApp.expiry.contains("9999-12-31")){
            espir="No Limit";
        }else {
            espir=MyApp.expiry;
        }

        textView.setText(
                "STX MC v " + BuildConfig.VERSION_NAME + "\n\n" +
                        "Device: " + Build.BRAND + "  S/N: " + MyApp.DEVICE_SN + "\n\n" +
                        "Support: " + MyData.get_String("techInfo") + "\n\n" +
                        "License Type: " + licenzaStringa() + "\n\n" +
                        "Expiry Date: "+espir+"\n\n"+
                        "Restore Code: "+MyApp.restoreCode+"\n\n"+
                        "Machine: " + DataSaved.machineName + "\n\n" +
                        "Project: " + s + "\n" +
                        "CRS: " + MyData.get_String("LastSP") + "\n" +
                        "Geoid: " + MyData.get_String("geoidPath") + "\n"
        );
    }

    private void onClick() {
        close.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    private String licenzaStringa() {
         switch (MyApp.licenseType) {
            case -1 :return "No License";
            case 0 :return  "Dig 1D License No AUTO ";
            case 1 :return  "Dig 1D / 2D License No AUTO";
            case 2 :return  "MC 1D / 2D / 3DPRO License No AUTO";
            case 3 :return  "MC 1D / 2D / 3DPRO License No ENABLED";
            case 4 :return  "MC 1D / 2D / 3DEasy License AUTO ENABLED";
            case 5 :return  "MC 1D / 2D / 3DPRO License AUTO ENABLED";
            default :return  "";
        }
    }
}
