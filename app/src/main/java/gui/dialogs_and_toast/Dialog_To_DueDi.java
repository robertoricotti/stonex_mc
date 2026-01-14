package gui.dialogs_and_toast;

import static gui.MyApp.licenseType;
import static utils.MyTypes.MC_1D;
import static utils.MyTypes.MC_2D;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.stx_dig.R;

import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.profiles.ProfilesMenuActivity;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;

public class Dialog_To_DueDi {
    Activity activity;
    public Dialog dialog;
    ImageView cancel, to1d, to2d, toProfile;

    public Dialog_To_DueDi(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_unodi_duedi);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 🔹 Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.75);
        int height = (int) (displayMetrics.heightPixels * 0.65);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
    }

    private void findView() {
        cancel = dialog.findViewById(R.id.cancel);
        to1d = dialog.findViewById(R.id.to1D);
        to2d = dialog.findViewById(R.id.to2D);
        toProfile = dialog.findViewById(R.id.toProfile);
        if(licenseType<MC_2D){
            to2d.setAlpha(0.3f);
        }
    }

    private void onClick() {

        toProfile.setOnClickListener(view -> {
            DataSaved.portView=0;
            DataSaved.profileSelected=0;
            activity.startActivity(new Intent(activity, ProfilesMenuActivity.class));
            activity.finish();
            dialog.dismiss();
        });
        to1d.setOnClickListener(view -> {
            DataSaved.portView=0;
            DataSaved.profileSelected=0;
            if (licenseType > -1) {
                activity.startActivity(new Intent(activity, Digging1D.class));
                activity.finish();
                dialog.dismiss();
            } else {
                new CustomToast(activity, "LICENSE MISSED").show_alert();
            }
        });
        to2d.setOnClickListener(view -> {
            DataSaved.portView=1;
            DataSaved.profileSelected=0;
            if (licenseType > MC_1D) {
                activity.startActivity(new Intent(activity, Digging2D.class));
                activity.finish();
                dialog.dismiss();
            } else {

                new CustomToast(activity, "LICENSE MISSED").show_alert();
            }
        });
        cancel.setOnClickListener(view -> {
            DataSaved.portView=2;
            dialog.dismiss();
        });

    }
}
