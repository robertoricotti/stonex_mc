package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.stx_dig.R;

import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.Digging_CutAndFill1D;
import gui.digging_excavator.Digging_CutAndFill2D;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.MyData;

public class LaserDialog {

  Activity activity;
    public Dialog dialog;
    Button yes, exit;
    static int count;

    public LaserDialog(Digging1D activity) {
        this.activity = activity;
        count = 1;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public LaserDialog(Digging2D activity) {
        this.activity = activity;
        count = 2;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public LaserDialog(Digging_CutAndFill1D activity) {
        this.activity = activity;
        count = 3;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public LaserDialog(Digging_CutAndFill2D activity) {
        this.activity = activity;
        count = 4;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {


        dialog.create();
        dialog.setContentView(R.layout.dialog_laser);
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
        int height = (int) (displayMetrics.heightPixels * 0.75);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        findView();
        onClick();


    }

    private void findView() {
        yes = dialog.findViewById(R.id.add_new);
        exit = dialog.findViewById(R.id.create_new);
    }

    private void onClick() {
        yes.setOnClickListener((View v) -> {
            DataSaved.offsetH = DataSaved.offsetLaserZH - ExcavatorLib.quota2D;

            switch (count) {
                case 1:
                    MyData.push("Operator_Offset", String.valueOf(DataSaved.offsetH));
                    Digging1D.flagLaser_D1D = false;
                    break;

                case 2:
                    MyData.push("Operator_Offset", String.valueOf(DataSaved.offsetH));
                    Digging2D.flagLaser_D2D = false;
                    break;

                case 3:
                    MyData.push("Operator_Offset", String.valueOf(DataSaved.offsetH));
                    Digging_CutAndFill1D.flagLaser_C1D = false;
                    break;

                case 4:
                    MyData.push("Operator_Offset", String.valueOf(DataSaved.offsetH));
                    Digging_CutAndFill2D.flagLaser_C2D = false;
                    break;
            }


            count=0;
            dialog.dismiss();
        });

        exit.setOnClickListener((View v) -> {
            switch (count) {
                case 1:
                    Digging1D.flagLaser_D1D = false;
                    break;
                case 2:
                    Digging2D.flagLaser_D2D = false;
                    break;
                case 3:
                    Digging_CutAndFill1D.flagLaser_C1D = false;
                    break;
                case 4:
                    Digging_CutAndFill2D.flagLaser_C2D = false;
                    break;

            }
            count=0;
            dialog.dismiss();
        });
    }
}


