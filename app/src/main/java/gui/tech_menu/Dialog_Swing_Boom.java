package gui.tech_menu;

import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static packexcalib.exca.ExcavatorLib.swing_boom_angle;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.NmeaListener;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Swing_Boom {
    private Handler handler;
    private boolean isUpdating = false;
    int indexMeasure, indexMachineSelected;
    Activity activity;
    Button sett;
    public Dialog dialog;
    TextView valoreReal,anglolomch;
    ImageView save, cancel, immagine, immagine2,reload;
    EditText editText, valuedeg;
    EditText unita, unitadeg;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;

    public Dialog_Swing_Boom(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_swing_boom);
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
        int width = (int) (displayMetrics.widthPixels * 0.85);
        int height = (int) (displayMetrics.heightPixels * 0.75);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        startUpdatingCoordinates();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
    }

    private void findView() {
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        customNumberDialog = new CustomNumberDialog(activity, -2);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -2);
        save = dialog.findViewById(R.id.save);
        cancel = dialog.findViewById(R.id.cancel);
        editText = dialog.findViewById(R.id.val);
        valuedeg = dialog.findViewById(R.id.valdeg);
        reload=dialog.findViewById(R.id.update);
        unita = dialog.findViewById(R.id.unita);
        unitadeg = dialog.findViewById(R.id.unitadeg);
        immagine = dialog.findViewById(R.id.immagine);
        immagine2 = dialog.findViewById(R.id.immagine2);
        valoreReal = dialog.findViewById(R.id.angoloreal);
        anglolomch=dialog.findViewById(R.id.anglolomch);
        sett=dialog.findViewById(R.id.sett);
        editText.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.miniPitch_L)));
        valuedeg.setText(String.format("%.2f", DataSaved.offsetSwingExca).replace(",","."));
        unita.setText(Utils.getMetriSimbol().replace("[", " ").replace("]", " "));
        if (DataSaved.isWL == EXCAVATOR||DataSaved.isWL==DRILL) {
            immagine.setImageResource(R.drawable.swingboom);
            immagine2.setVisibility(View.VISIBLE);
            valuedeg.setVisibility(View.VISIBLE);
            unitadeg.setVisibility(View.VISIBLE);
            valoreReal.setVisibility(View.VISIBLE);
        } else {
            immagine.setImageResource(R.drawable.weel1);
            immagine2.setVisibility(View.GONE);
            valuedeg.setVisibility(View.GONE);
            unitadeg.setVisibility(View.GONE);
            valoreReal.setVisibility(View.GONE);
        }
    }

    private void onClick() {
        sett.setOnLongClickListener(view -> {
            boolean err=false;
            if(DataSaved.my_comPort==0) {
                err= NmeaListener.mch_Hdt_1 == 999.999;
            }else {
                err= NmeaListener.mch_Hdt == 999.999;
            }

            if(!err) {
                double valoreFinale = normalizeAngle(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                double valoreIniziale = normalizeAngle(NmeaListener.roof_Orientation);
                DataSaved.offsetSwingExca = valoreFinale-valoreIniziale  ;

                try {
                    valuedeg.setText(String.format("%.2f", DataSaved.offsetSwingExca).replace(",","."));
                    MyData.push("M" + indexMachineSelected + "offsetSwingExca", Utils.writeMetri(valuedeg.getText().toString()));
                } catch (NumberFormatException ignored) {

                }
            }else {
                new CustomToast(activity,"HDT Err").show_error();
            }
            return true;
        });
        reload.setOnClickListener(view -> {
            DataSaved.miniPitch_L = Double.parseDouble(Utils.writeMetri(editText.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_Swing_Len", Utils.writeMetri(editText.getText().toString()));
            try {
                DataSaved.offsetSwingExca = Double.parseDouble(valuedeg.getText().toString());
                MyData.push("M" + indexMachineSelected + "offsetSwingExca", Utils.writeMetri(valuedeg.getText().toString()));
            } catch (NumberFormatException ignored) {

            }
        });
        editText.setOnClickListener(view -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(editText);
                }
            } else {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(editText);
                }
            }
        });

        valuedeg.setOnClickListener(view -> {

            if (!customNumberDialog.dialog.isShowing()) {
                customNumberDialog.show(valuedeg);
            }

        });
        save.setOnClickListener(view -> {
            stopUpdatingCoordinates();
            DataSaved.miniPitch_L = Double.parseDouble(Utils.writeMetri(editText.getText().toString()));
            MyData.push("M" + indexMachineSelected + "_Swing_Len", Utils.writeMetri(editText.getText().toString()));
            try {
                DataSaved.offsetSwingExca = Double.parseDouble(valuedeg.getText().toString());
                MyData.push("M" + indexMachineSelected + "offsetSwingExca", Utils.writeMetri(valuedeg.getText().toString()));
            } catch (NumberFormatException ignored) {

            }

            new CustomToast(activity, activity.getString(R.string.save)).show();
            dialog.dismiss();
        });

        cancel.setOnClickListener(view -> {
            stopUpdatingCoordinates();
            dialog.dismiss();
        });
    }


    private void startUpdatingCoordinates() {
        if (!isUpdating) {
            isUpdating = true;
            handler = new Handler();
            updateUI();
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

    private void updateUI() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("Dioo",NmeaListener.mch_Hdt+"");
                    double hdt = normalizeAngle(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                    double valore= hdt_BOOM;
                    valore=valore % 360;
                    if(NmeaListener.roof_Orientation==999.999) {
                        valoreReal.setText("Error" + " °\n\n\n");
                    }else{
                        valoreReal.setText(String.format("%.2f", valore).replace(",",".") + " °\n\n\n");
                    }
                    if(DataSaved.my_comPort==0) {
                        if (NmeaListener.mch_Hdt_1 == 999.999) {
                            anglolomch.setText("HDT Error");

                        } else {
                            anglolomch.setText(String.format("%.2f", hdt).replace(",", ".") + " °");

                        }
                    }else {
                        if (NmeaListener.mch_Hdt== 999.999) {
                            anglolomch.setText("HDT Error");

                        } else {
                            anglolomch.setText(String.format("%.2f", hdt).replace(",", ".") + " °");

                        }
                    }

                    if (isUpdating) {
                        updateUI();
                    }
                } catch (Exception ignored) {

                }

            }
        }, 100);
    }
    private static double normalizeAngle(double a) {
        a = a % 360.0;
        if (a < 0) a += 360.0;
        return a;
    }
}
