package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import packexcalib.exca.Sensors_Decoder;
import services.CanService;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_Wheel_Steer {
    Activity activity;
    int indexM;
    public Dialog dialog;
    private boolean isUpdating = false;
    private Handler handler;
    ImageView close, save;
    Button setMin, setCent, setMax, piuRange, menoRange;
    TextView tvMin, tvCent, tvMax, tvInput, tvRedsult;
    EditText tvRange;
    CheckBox setRev;
    CustomNumberDialog customNumberDialog;

    public Dialog_Wheel_Steer(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_wheel_steer);
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
        int height = (int) (displayMetrics.heightPixels * 0.95);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        indexM = MyData.get_Int("MachineSelected");
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
        startUpdatingCoordinates();
    }

    private void findView() {
        customNumberDialog = new CustomNumberDialog(activity, 999);
        close = dialog.findViewById(R.id.close);
        save = dialog.findViewById(R.id.save);
        setRev = dialog.findViewById(R.id.setRev);
        setMin = dialog.findViewById(R.id.setMin);
        setCent = dialog.findViewById(R.id.setMed);
        setMax = dialog.findViewById(R.id.setMax);
        tvMin = dialog.findViewById(R.id.tvMin);
        tvCent = dialog.findViewById(R.id.tvMed);
        tvMax = dialog.findViewById(R.id.tvMax);
        tvInput = dialog.findViewById(R.id.tvInput);
        tvRange = dialog.findViewById(R.id.tvRange);
        tvRedsult = dialog.findViewById(R.id.tvResult);
        DataSaved.Wheel_Steer_Rev=MyData.get_Int("M"+indexM+"Wheel_Steer_Rev");
        if(DataSaved.Wheel_Steer_Rev==1){
            setRev.setChecked(false);
        }else if(DataSaved.Wheel_Steer_Rev==-1){
            setRev.setChecked(false);
        }
        tvRange.setText(String.valueOf(DataSaved.Wheel_Steer_Range));
    }

    private void onClick() {
        setRev.setOnClickListener(view -> {
            if(DataSaved.Wheel_Steer_Rev==1){
                DataSaved.Wheel_Steer_Rev=-1;
            }else {
                DataSaved.Wheel_Steer_Rev=1;
            }
            MyData.push("M"+indexM+"Wheel_Steer_Rev",String.valueOf(DataSaved.Wheel_Steer_Rev));
        });
        tvRange.setOnClickListener(view -> {
            if(!customNumberDialog.dialog.isShowing()){
                customNumberDialog.show(tvRange);
            }
        });
        setMin.setOnLongClickListener(view -> {
            if(CanService.SteerConnected==2) {
                DataSaved.Wheel_Steer_Min = Sensors_Decoder.WheelSteer;
                MyData.push("M" + indexM + "Wheel_Steer_Min", String.valueOf(DataSaved.Wheel_Steer_Min));
            }
            return false;
        });

        setCent.setOnLongClickListener(view -> {
            if(CanService.SteerConnected==2) {
                DataSaved.Wheel_Steer_Med = Sensors_Decoder.WheelSteer;
                MyData.push("M" + indexM + "Wheel_Steer_Med", String.valueOf(DataSaved.Wheel_Steer_Med));
            }
            return false;
        });

        setMax.setOnLongClickListener(view -> {
            if(CanService.SteerConnected==2) {
                DataSaved.Wheel_Steer_Max = Sensors_Decoder.WheelSteer;
                MyData.push("M" + indexM + "Wheel_Steer_Max", String.valueOf(DataSaved.Wheel_Steer_Max));
            }
            return false;
        });
        close.setOnClickListener(view -> {
            stopUpdatingCoordinates();
            dialog.dismiss();
        });
        save.setOnClickListener(view -> {
            MyData.push("M" + indexM + "Wheel_Steer_Max", String.valueOf(DataSaved.Wheel_Steer_Range));
            stopUpdatingCoordinates();
            dialog.dismiss();
        });

    }

    public void updateUI() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    DataSaved.Wheel_Steer_Range=Double.parseDouble(tvRange.getText().toString());
                } catch (NumberFormatException e) {
                    tvRange.setText(e.toString());
                }
                if(!customNumberDialog.dialog.isShowing()){
                    tvRange.setText(String.valueOf(DataSaved.Wheel_Steer_Range));
                }


                tvMin.setText(String.valueOf(DataSaved.Wheel_Steer_Min));
                tvCent.setText(String.valueOf(DataSaved.Wheel_Steer_Med));
                tvMax.setText(String.valueOf(DataSaved.Wheel_Steer_Max));
                tvInput.setText(String.valueOf(Sensors_Decoder.WheelSteer));
                if(CanService.SteerConnected==0){
                    tvRedsult.setText("DISABLED");
                    tvRedsult.setTextColor(Color.DKGRAY);
                } else if (CanService.SteerConnected==2) {
                    tvRedsult.setText(String.format("%.2f", DataSaved.SteerWheel_Result));
                    tvRedsult.setTextColor(Color.BLUE);

                } else if (CanService.SteerConnected==1) {
                    tvRedsult.setText("DISCONNECTED");
                    tvRedsult.setTextColor(Color.RED);
                }
                tvRedsult.setText(String.format("%.2f", DataSaved.SteerWheel_Result).replace(",","."));

                if (isUpdating) {
                    updateUI();
                }
            }
        }, 100);
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

}
