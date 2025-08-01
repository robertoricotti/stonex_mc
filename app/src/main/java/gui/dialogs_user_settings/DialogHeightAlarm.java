package gui.dialogs_user_settings;

import static gui.MyApp.isApollo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import packexcalib.exca.ExcavatorLib;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class DialogHeightAlarm {
    Activity activity;
    public Dialog alertDialog;
    ImageView save,canc;
    Button  set, reset, plus, minus;
    EditText value, delta;
    TextView title;
    boolean p_pressed, m_pressed;

    double tmepValue, tempDelta;

    double tmp = 0;
    double tmpD = 0;
    private boolean isUpdating = false;
    private Handler handler;

    public DialogHeightAlarm(Activity activity) {
        this.activity = activity;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        alertDialog.setContentView(R.layout.dialog_alarm_pivot);

        alertDialog.setCancelable(false);
    }

    public void show() {
        init();
    }

    private void findView() {
        title = alertDialog.findViewById(R.id.title);
        canc = alertDialog.findViewById(R.id.exit);
        set = alertDialog.findViewById(R.id.setPivot);
        save = alertDialog.findViewById(R.id.save);
        reset = alertDialog.findViewById(R.id.reset);
        value = alertDialog.findViewById(R.id.value);
        delta = alertDialog.findViewById(R.id.valueDelta);
        plus = alertDialog.findViewById(R.id.plus);
        minus = alertDialog.findViewById(R.id.minus);
    }

    @SuppressLint("SetTextI18n")
    private void init() {


        alertDialog.setCancelable(false);
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 🔹 Applica dim
            window.setAttributes(wlp);
            // Calcola 75% della larghezza dello schermo
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.85);
            int height = (int) (displayMetrics.heightPixels * 0.85);
            alertDialog.getWindow().setLayout(width, height);
            alertDialog.getWindow().setGravity(Gravity.CENTER);
            alertDialog.show();
        }
        FullscreenActivity.setFullScreen(alertDialog);
        findView();
        onClick();

        tmepValue = MyData.get_Double("Pivot_Height_Alarm");
        try {
            tempDelta = MyData.get_Double("DeltaPivot");

        } catch (Exception e) {
            MyData.push("DeltaPivot", "0");
            tempDelta = 0;
        }
        if (tmepValue == 10000000.0) {
            value.setText("OFF");
        } else {
            value.setText(Utils.readUnitOfMeasureLITE(String.valueOf(tmepValue)));
        }
        String s = activity.getResources().getString(R.string.height_alarm);
        title.setText(s + " " + Utils.getMetriSimbol());
        delta.setText(Utils.readUnitOfMeasureLITE(String.valueOf(tempDelta)));
        onTouch();
        startUpdatingCoordinates();
    }

    @SuppressLint("DefaultLocale")
    private void onClick() {
        plus.setOnClickListener(view -> {
            tempDelta+=0.01;
            delta.setText(Utils.readUnitOfMeasureLITE(String.valueOf(tempDelta)));
        });
        minus.setOnClickListener(view -> {
            tempDelta-=0.01;
            if(tempDelta<=0){
                tempDelta=0;
            }
            delta.setText(Utils.readUnitOfMeasureLITE(String.valueOf(tempDelta)));
        });

        set.setOnClickListener((View v) -> {
            tmp = ExcavatorLib.highestPoint;
            value.setText(Utils.readUnitOfMeasureLITE(String.valueOf(tmp)));
        });

        reset.setOnClickListener((View v) -> {
            MyData.push("Pivot_Height_Alarm", "100000.0");
            MyData.push("DeltaPivot", String.valueOf(0));
            tmp = 10000000.0;
            value.setText("OFF");
            delta.setText(Utils.readUnitOfMeasureLITE(String.valueOf(tempDelta)));

        });

        save.setOnClickListener((View v) -> {
            stopUpdatingCoordinates();
            MyData.push("Pivot_Height_Alarm", String.valueOf(tmp));
            MyData.push("DeltaPivot", String.valueOf(tempDelta));
            alertDialog.cancel();
        });

        canc.setOnClickListener((View v) -> {
            stopUpdatingCoordinates();
            alertDialog.cancel();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onTouch() {
        plus.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("pressss","pigiato");
                    p_pressed = true;
                    return false;


                case MotionEvent.ACTION_UP:
                    p_pressed = false;
                    Log.d("pressss","rilasciato");
                    return false;
            }
            return false;
        });
        minus.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    m_pressed = true;
                    return false;

                case MotionEvent.ACTION_UP:
                    m_pressed = false;
                    return false;
            }
            return false;
        });

    }

    private void rise() {
        tempDelta += 0.01;
        delta.setText(Utils.readUnitOfMeasureLITE(String.valueOf(tempDelta)));
    }

    private void lower() {
        tempDelta -= 0.01;
        if (tempDelta <= 0) {
            tempDelta = 0;
        }
        delta.setText(Utils.readUnitOfMeasureLITE(String.valueOf(tempDelta)));
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


                    // Update value
                    Log.d("pressss", "running");
                    if (p_pressed) {
                        rise();
                    }
                    if (m_pressed) {
                        lower();
                    }

                    if (isUpdating) {
                        updateUI();
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }, 250);
    }

}
