package gui.dialogs_user_settings;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.example.stx_dig.R;

import gui.boot_and_choose.AnyDeskLauncher;
import gui.dialogs_and_toast.CustomToast;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_QR {
    Activity activity;
    public Dialog dialog;
    TextView testo;
    ImageView mioQr, chiud, anydesk, quick;
    String string, s;


    public Dialog_QR(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_qr_code);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
    }

    private void findView() {
        testo = dialog.findViewById(R.id.testo);
        mioQr = dialog.findViewById(R.id.mioQr);
        chiud = dialog.findViewById(R.id.chiud);
        anydesk = dialog.findViewById(R.id.anydesk);
        quick = dialog.findViewById(R.id.quickSupport);
        //anydesk.setVisibility(View.INVISIBLE);
        quick.setVisibility(View.INVISIBLE);

    }

    private void init() {
        s = "";
        if (Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
            Apollo2 apollo2 = Apollo2.getInstance(activity);
            s = apollo2.getDeviceSN() + " " + Build.BRAND;
        } else {
            ApolloPro apolloPro = ApolloPro.getInstance(activity);
            s = apolloPro.getDeviceSN() + " " + Build.BRAND;
        }
        string = s + "\n" + MyData.get_String("techInfo");
        testo.setText(string);

    }

    private void onClick() {
        chiud.setOnClickListener(view -> {
            dialog.dismiss();
        });
        anydesk.setOnClickListener(view -> {
            new CustomToast(activity, "Long Press to Start Anydesk").show_long();
        });
        anydesk.setOnLongClickListener(view -> {
            AnyDeskLauncher anyDeskLauncher = new AnyDeskLauncher(activity);
            anyDeskLauncher.launchAnyDesk(0);
            dialog.dismiss();
            return false;
        });
        quick.setOnClickListener(view -> {
            new CustomToast(activity, "Long Press to Start QuickSupport").show_long();
        });
        quick.setOnLongClickListener(view -> {
            AnyDeskLauncher anyDeskLauncher = new AnyDeskLauncher(activity);
            anyDeskLauncher.launchAnyDesk(1);
            dialog.dismiss();
            return false;
        });
    }


}

