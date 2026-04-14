package gui.dialogs_and_toast;

import static gui.MyApp.isApollo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.Digging_CutAndFill1D;
import gui.digging_excavator.Digging_CutAndFill2D;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.FullscreenActivity;

public class Dialog_TouchGo {


    Activity activity;
    public Dialog alertDialog;
    ImageView yes, yes_2;
    TextView value, title;
    ImageView close;

    public Dialog_TouchGo(Activity activity) {
        this.activity = activity;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        if (isApollo) {
            builder.setView(inflater.inflate(R.layout.dialog_touch_e_go, null));
        } else {
            builder.setView(inflater.inflate(R.layout.dialog_touch_e_go_s80, null));
        }
        alertDialog = builder.create();
        alertDialog.setCancelable(false);

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
        if (Build.BRAND.equals("SRT8PROS")) {
            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        findView();
        onClick();
        FullscreenActivity.setFullScreen(alertDialog);
    }

    private void findView() {
        yes = alertDialog.findViewById(R.id.add_new);
        yes_2 = alertDialog.findViewById(R.id._h_yes2);
        yes_2.setEnabled(false);
        yes_2.setAlpha(0.3f);
        close = alertDialog.findViewById(R.id.closeTeG);
        value = alertDialog.findViewById(R.id.tcg_value);
        value.setText(String.format("%.2f", DataSaved.monumentSet));
        title = alertDialog.findViewById(R.id.title);
        title.setText(activity.getResources().getString(R.string.pick_1));

    }

    private void onClick() {
        yes.setOnClickListener(view -> {


            if (activity instanceof Digging1D) {

                DataSaved.monumentSet = Digging1D.mAltezza;
                yes_2.setEnabled(true);
                yes_2.setAlpha(1.0f);
                yes_2.setImageTintList(ContextCompat.getColorStateList(activity.getApplicationContext(), R.color.bg_stonex_blue));
                yes.setImageTintList(ContextCompat.getColorStateList(activity.getApplicationContext(), R.color._____cancel_text));
                yes.setAlpha(0.3f);
                yes.setEnabled(false);

            } else if (activity instanceof Digging2D) {

                DataSaved.monumentSet = Digging2D.mAltezza2D;
                yes_2.setEnabled(true);
                yes_2.setAlpha(1.0f);
                yes.setAlpha(0.3f);
                yes.setEnabled(false);

                yes_2.setImageTintList(ContextCompat.getColorStateList(activity.getApplicationContext(), R.color.bg_stonex_blue));
                yes.setImageTintList(ContextCompat.getColorStateList(activity.getApplicationContext(), R.color._____cancel_text));
            } else if (activity instanceof Digging_CutAndFill1D) {

                DataSaved.monumentSet = Digging_CutAndFill1D.mAltezza1D_cf;
                yes_2.setEnabled(true);
                yes_2.setAlpha(1.0f);
                yes.setAlpha(0.3f);
                yes.setEnabled(false);
                yes_2.setImageTintList(ContextCompat.getColorStateList(activity.getApplicationContext(), R.color.bg_stonex_blue));
                yes.setImageTintList(ContextCompat.getColorStateList(activity.getApplicationContext(), R.color._____cancel_text));
            } else if (activity instanceof Digging_CutAndFill2D) {

                DataSaved.monumentSet = Digging_CutAndFill2D.mAltezza2D_cf;
                yes_2.setEnabled(true);
                yes_2.setAlpha(1.0f);
                yes.setAlpha(0.3f);
                yes.setEnabled(false);
                yes_2.setImageTintList(ContextCompat.getColorStateList(activity.getApplicationContext(), R.color.bg_stonex_blue));
                yes.setImageTintList(ContextCompat.getColorStateList(activity.getApplicationContext(), R.color._____cancel_text));
            }


            title.setText(activity.getResources().getString(R.string.touch_e_go));


        });


        yes_2.setOnClickListener((View v) -> {
            switch (DataSaved.bucketEdge) {
                case -1:
                    DataSaved.start2DX = ExcavatorLib.bucketLeftCoord[0];
                    DataSaved.start2DY = ExcavatorLib.bucketLeftCoord[1];
                    DataSaved.start2DZ = ExcavatorLib.bucketLeftCoord[2];

                    break;
                case 0:
                    DataSaved.start2DX = ExcavatorLib.bucketCoord[0];
                    DataSaved.start2DY = ExcavatorLib.bucketCoord[1];
                    DataSaved.start2DZ = ExcavatorLib.bucketCoord[2];

                    break;
                case 1:
                    DataSaved.start2DX = ExcavatorLib.bucketRightCoord[0];
                    DataSaved.start2DY = ExcavatorLib.bucketRightCoord[1];
                    DataSaved.start2DZ = ExcavatorLib.bucketRightCoord[2];

                    break;
            }

            if (activity instanceof Digging1D) {

                DataSaved.monumentRelease = Digging1D.mAltezza - DataSaved.monumentSet;
            } else if (activity instanceof Digging2D) {

                DataSaved.monumentRelease = Digging2D.mAltezza2D - DataSaved.monumentSet;
            } else if (activity instanceof Digging_CutAndFill1D) {


                DataSaved.monumentRelease = Digging_CutAndFill1D.mAltezza1D_cf - DataSaved.monumentSet;
            } else if (activity instanceof Digging_CutAndFill2D) {

                DataSaved.monumentRelease = Digging_CutAndFill2D.mAltezza2D_cf - DataSaved.monumentSet;


            }


            DataSaved.monumentSet = 0;

            alertDialog.dismiss();
        });
        close.setOnClickListener(view -> {
            DataSaved.monumentSet = 0;
            DataSaved.monumentRelease = 0;
            alertDialog.dismiss();
        });


    }
}
