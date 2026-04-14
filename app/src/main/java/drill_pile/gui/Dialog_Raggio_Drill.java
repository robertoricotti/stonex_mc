package drill_pile.gui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_Raggio_Drill {
    Activity activity;
    public Dialog dialog;

    private SeekBar seekRaggio;
    private TextView tvValue;
    private ImageView btnOk;

    private final double MIN = 100.0;
    private final double MAX = 1000.0;
    private final int VALUES = 10; // 10 valori totali
    private double stepSize;


    public Dialog_Raggio_Drill(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_raggio_drill);
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
        int width = (int) (displayMetrics.widthPixels * 0.8);
        int height = (int) (displayMetrics.heightPixels * 0.45);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        findView();
        onClick();
        FullscreenActivity.setFullScreen(dialog);

    }

    private void findView() {
        seekRaggio = dialog.findViewById(R.id.seekRaggio);
        tvValue = dialog.findViewById(R.id.tvValue);
        btnOk = dialog.findViewById(R.id.btnOk);

        stepSize = (MAX - MIN) / (double) (VALUES - 1); // 1100
        seekRaggio.setMax(VALUES - 1); // 9

        // 🔹 Inizializza dal valore salvato
        int initialProgress = progressFromValue(DataSaved.Raggio_Drill);
        seekRaggio.setProgress(initialProgress);

        double currentValue = valueFromProgress(initialProgress);
        //tvValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(currentValue))+" "+Utils.getMetriSimbol());
        tvValue.setText((initialProgress + 1) + "/10");
    }

    private void onClick() {

        seekRaggio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                double value = valueFromProgress(progress);

                // 🔹 aggiorna variabile statica

                DataSaved.Raggio_Drill = value;
                if (value == MAX) {
                    DataSaved.Raggio_Drill = 10000.0d;
                }

                //tvValue.setText(Utils.readUnitOfMeasureLITE(String.valueOf(value))+" "+Utils.getMetriSimbol());
                tvValue.setText((progress + 1) + "/10");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        btnOk.setOnClickListener(view -> {
            MyData.push("Raggio_Drill", String.valueOf(DataSaved.Raggio_Drill));
            dialog.dismiss();
        });


    }

    private double valueFromProgress(int progress) {
        return MIN + (progress * stepSize);
    }

    private int progressFromValue(double value) {
        int p = (int) Math.round((value - MIN) / stepSize);

        // sicurezza nel caso il valore sia fuori range
        if (p < 0) p = 0;
        if (p > VALUES - 1) p = VALUES - 1;

        return p;
    }


}
