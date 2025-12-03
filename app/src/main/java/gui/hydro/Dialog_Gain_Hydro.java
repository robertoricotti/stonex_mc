package gui.hydro;

import static utils.MyTypes.GRADER;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.FullscreenActivity;
import utils.MyData;
import utils.MyTypes;

public class Dialog_Gain_Hydro {
    Activity activity;
    public Dialog dialog;
    int machineSelected;
    ImageView lamaDX, lamaCP, gainLPiu, gainLMen, gainRPiu, gainRMen, save, cancel;
    TextView gainL, gainR, centerTitle;
    Handler handler = new Handler(Looper.getMainLooper());
    // Definisci un Runnable separato per ciascun bottone
    Runnable gainLPiuRepeater;
    Runnable gainLMenRepeater;
    Runnable gainRPiuRepeater;
    Runnable gainRMenRepeater;

    public Dialog_Gain_Hydro(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_gain_hydro);
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
        int width = (int) (displayMetrics.widthPixels *1);
        int height = (int) (displayMetrics.heightPixels * 1);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();


    }

    private void findView() {
        lamaDX = dialog.findViewById(R.id.lamaDX);
        lamaCP = dialog.findViewById(R.id.lamaCP);

        gainLPiu = dialog.findViewById(R.id.gainLPiu);
        gainLMen = dialog.findViewById(R.id.gainLMen);
        gainRPiu = dialog.findViewById(R.id.gainRPiu);
        gainRMen = dialog.findViewById(R.id.gainRMen);
        save = dialog.findViewById(R.id.save);
        cancel = dialog.findViewById(R.id.cancel);
        gainL = dialog.findViewById(R.id.gainL);
        gainR = dialog.findViewById(R.id.gainR);
        centerTitle = dialog.findViewById(R.id.centerTitle);
        centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase());


    }

    private void init() {
        machineSelected = MyData.get_Int("MachineSelected");
        gainL.setText(String.valueOf(MyData.get_Int("M" + machineSelected + "GAIN_LEFT")));
        gainR.setText(String.valueOf(MyData.get_Int("M" + machineSelected + "GAIN_RIGHT")));
        if (DataSaved.isWL == GRADER) {
            switch (DataSaved.HYDRAULIC_CONTROL_POINT_GRADER) {
                case 0:
                    lamaCP.setImageResource((R.drawable.cent_right));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"CENTER-RIGHT");
                    break;

                case 1:
                    lamaCP.setImageResource((R.drawable.cent_left));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"CENTER-LEFT");
                    break;

                case 2:
                    lamaCP.setImageResource((R.drawable.left_right));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"LEFT-RIGHT");
                    break;
            }
        } else {
            switch (DataSaved.HYDRAULIC_CONTROL_POINT_DOZER) {
                case 0:
                    lamaCP.setImageResource((R.drawable.cent_right));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"CENTER-RIGHT");
                    break;

                case 1:
                    lamaCP.setImageResource((R.drawable.cent_left));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"CENTER-LEFT");
                    break;


            }
        }

    }

    private void onClick() {
        // --- GAIN LEFT + ---
        gainLPiu.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (DataSaved.GAIN_LEFT < 255) {
                        DataSaved.GAIN_LEFT += 1;
                    }
                    updateLama();

                    gainLPiuRepeater = new Runnable() {
                        @Override
                        public void run() {
                            if (DataSaved.GAIN_LEFT < 255) {
                                DataSaved.GAIN_LEFT += 1;
                                updateLama();
                                handler.postDelayed(this, 100);
                            }
                        }
                    };
                    handler.postDelayed(gainLPiuRepeater, 1000);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(gainLPiuRepeater);
                    return true;
            }
            return false;
        });

// --- GAIN LEFT - ---
        gainLMen.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (DataSaved.GAIN_LEFT > 1) {
                        DataSaved.GAIN_LEFT -= 1;
                    }
                    updateLama();

                    gainLMenRepeater = new Runnable() {
                        @Override
                        public void run() {
                            if (DataSaved.GAIN_LEFT > 1) {
                                DataSaved.GAIN_LEFT -= 1;
                                updateLama();
                                handler.postDelayed(this, 100);
                            }
                        }
                    };
                    handler.postDelayed(gainLMenRepeater, 1000);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(gainLMenRepeater);
                    return true;
            }
            return false;
        });

// --- GAIN RIGHT + ---
        gainRPiu.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (DataSaved.GAIN_RIGHT < 255) {
                        DataSaved.GAIN_RIGHT += 1;
                    }
                    updateLama();

                    gainRPiuRepeater = new Runnable() {
                        @Override
                        public void run() {
                            if (DataSaved.GAIN_RIGHT < 255) {
                                DataSaved.GAIN_RIGHT += 1;
                                updateLama();
                                handler.postDelayed(this, 100);
                            }
                        }
                    };
                    handler.postDelayed(gainRPiuRepeater, 1000);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(gainRPiuRepeater);
                    return true;
            }
            return false;
        });

// --- GAIN RIGHT - ---
        gainRMen.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (DataSaved.GAIN_RIGHT > 1) {
                        DataSaved.GAIN_RIGHT -= 1;
                    }
                    updateLama();

                    gainRMenRepeater = new Runnable() {
                        @Override
                        public void run() {
                            if (DataSaved.GAIN_RIGHT > 1) {
                                DataSaved.GAIN_RIGHT -= 1;
                                updateLama();
                                handler.postDelayed(this, 100);
                            }
                        }
                    };
                    handler.postDelayed(gainRMenRepeater, 1000);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(gainRMenRepeater);
                    return true;
            }
            return false;
        });

        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });


        lamaDX.setOnClickListener(view -> {
            if(DataSaved.isWL==GRADER) {

                    DataSaved.HYDRAULIC_CONTROL_POINT_GRADER += 1;
                    DataSaved.HYDRAULIC_CONTROL_POINT_GRADER=DataSaved.HYDRAULIC_CONTROL_POINT_GRADER%3;

            }else {

                    DataSaved.HYDRAULIC_CONTROL_POINT_DOZER += 1;
                DataSaved.HYDRAULIC_CONTROL_POINT_DOZER=DataSaved.HYDRAULIC_CONTROL_POINT_DOZER%2;
            }
            updateLama();
        });

        save.setOnClickListener(view -> {
            MyData.push("M" + machineSelected + "GAIN_LEFT", String.valueOf(DataSaved.GAIN_LEFT));
            MyData.push("M" + machineSelected + "GAIN_RIGHT", String.valueOf(DataSaved.GAIN_RIGHT));
            MyData.push("M" + machineSelected + "HYDRAULIC_CONTROL_POINT_GRADER", String.valueOf(DataSaved.HYDRAULIC_CONTROL_POINT_GRADER));
            MyData.push("M" + machineSelected + "HYDRAULIC_CONTROL_POINT_DOZER", String.valueOf(DataSaved.HYDRAULIC_CONTROL_POINT_DOZER));
            dialog.dismiss();
            this.dialog.dismiss();

        });
    }

    private void updateLama() {
        gainL.setText(String.valueOf(DataSaved.GAIN_LEFT));
        gainR.setText(String.valueOf(DataSaved.GAIN_RIGHT));
        if (DataSaved.isWL == 4) {
            switch (DataSaved.HYDRAULIC_CONTROL_POINT_GRADER) {
                case 0:
                    lamaCP.setImageResource((R.drawable.cent_right));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"CENTER-RIGHT");
                    break;

                case 1:
                    lamaCP.setImageResource((R.drawable.cent_left));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"CENTER-LEFT");
                    break;

                case 2:
                    lamaCP.setImageResource((R.drawable.left_right));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"LEFT-RIGHT");
                    break;
            }
        } else {
            switch (DataSaved.HYDRAULIC_CONTROL_POINT_DOZER) {
                case 0:
                    lamaCP.setImageResource((R.drawable.cent_right));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"CENTER-RIGHT");
                    break;

                case 1:
                    lamaCP.setImageResource((R.drawable.cent_left));
                    centerTitle.setText(activity.getString(R.string.hydro_cp).toUpperCase()+"\n"+"CENTER-LEFT");
                    break;


            }
        }
    }
}
