package gui.dialogs_and_toast;

import static gui.MyApp.isApollo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import utils.FullscreenActivity;
import utils.MyData;

public class LaserDialog {
    Digging1D activity1D;
    Digging2D activity2D;
    Digging_CutAndFill1D activityCut1D;
    Digging_CutAndFill2D activityCut2D;
    public Dialog alertDialog;
    Button yes, exit;
    int count;

    public LaserDialog(Digging1D activity) {
        this.activity1D = activity;
        count = 1;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public LaserDialog(Digging2D activity) {
        this.activity2D = activity;
        count = 2;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public LaserDialog(Digging_CutAndFill1D activity) {
        this.activityCut1D = activity;
        count = 3;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public LaserDialog(Digging_CutAndFill2D activity) {
        this.activityCut2D = activity;
        count = 4;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        AlertDialog.Builder builder = null;
        LayoutInflater inflater = null;
        switch (count) {
            case 1:
                builder = new AlertDialog.Builder(activity1D);
                inflater = activity1D.getLayoutInflater();
                break;
            case 2:
                builder = new AlertDialog.Builder(activity2D);
                inflater = activity2D.getLayoutInflater();

                break;
            case 3:
                builder = new AlertDialog.Builder(activityCut1D);
                inflater = activityCut1D.getLayoutInflater();
                break;
            case 4:
                builder = new AlertDialog.Builder(activityCut2D);
                inflater = activityCut2D.getLayoutInflater();
                break;

        }
        if (isApollo) {
            builder.setView(inflater.inflate(R.layout.dialog_laser, null));
        } else {
            builder.setView(inflater.inflate(R.layout.dialog_laser_s80, null));
        }
        alertDialog = builder.create();
        builder.setCancelable(false);
        FullscreenActivity.setFullScreen(alertDialog);
        Window window = alertDialog.getWindow();
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
        alertDialog.show();
        findView();
        onClick();


    }

    private void findView() {
        yes = alertDialog.findViewById(R.id.add_new);
        exit = alertDialog.findViewById(R.id.create_new);
    }

    private void onClick() {
        yes.setOnClickListener((View v) -> {
            DataSaved.offsetH = DataSaved.offsetLaserZH - ExcavatorLib.quota2D;

            switch (count) {
                case 1:
                    MyData.push("Operator_Offset", String.valueOf(DataSaved.offsetH));

                    if (++activity1D.counterZero > 1) {
                        activity1D.counterZero = 0;
                        Digging1D.flagLaser_D1D = false;

                    }
                    break;

                case 2:
                    MyData.push("Operator_Offset", String.valueOf(DataSaved.offsetH));

                    if (++activity2D.counterZero > 1) {
                        activity2D.counterZero = 0;
                        Digging2D.flagLaser_D2D = false;

                    }
                    break;

                case 3:
                    MyData.push("Operator_Offset", String.valueOf(DataSaved.offsetH));

                    if (++activityCut1D.counterZero > 1) {
                        activityCut1D.counterZero = 0;
                        Digging_CutAndFill1D.flagLaser_C1D = false;

                    }
                    break;

                case 4:
                    MyData.push("Operator_Offset", String.valueOf(DataSaved.offsetH));

                    if (++activityCut2D.counterZero > 1) {
                        activityCut2D.counterZero = 0;
                        Digging_CutAndFill2D.flagLaser_C2D = false;

                    }
                    break;
            }


            alertDialog.dismiss();
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
            alertDialog.dismiss();
        });
    }
}


