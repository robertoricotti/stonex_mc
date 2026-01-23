package drill_pile.gui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.stx_dig.R;

public class Dialog_DrillSet {
    Activity activity;
    public Dialog dialog;
    ImageView close;
    private boolean isUpdating = false;
    private Handler handler;


    public Dialog_DrillSet(Activity activity){
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }
    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_infopoint);
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
        findView();
        onClick();
        startUpdatingCoordinates();

    }

    private void findView(){

    }
    private void onClick(){

    }

    private void updateCoordinates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    // Update coord TextView with new coordinates

                    if (isUpdating) {
                        updateCoordinates();
                    }
                } catch (Exception e) {
                }
            }
        }, 100);
    }


    private void startUpdatingCoordinates() {
        if (!isUpdating) {
            isUpdating = true;
            handler = new Handler();
            updateCoordinates();
        }
    }

    private void stopUpdatingCoordinates() {
        if (isUpdating) {
            isUpdating = false;
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
    }
}
