package gui.dialogs_and_toast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import services.CanService;
import utils.FullscreenActivity;
import utils.MyDeviceManager;
import utils.Utils;

public class Dialog_Gain {

    Activity activity;
    public Dialog dialog;
    Button ok;
    ImageButton left_su,left_giu,right_su,right_giu,db_piu,db_meno;
    TextView left,right,db;
    ImageView ecu;

    public Dialog_Gain(Activity activity){
        this.activity=activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_gain, null));
        dialog = builder.create();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();

    }

    private void findView(){
        ok=dialog.findViewById(R.id.ok);
        left_su=dialog.findViewById(R.id.left_su);
        left_giu=dialog.findViewById(R.id.left_giu);
        right_su=dialog.findViewById(R.id.right_su);
        right_giu=dialog.findViewById(R.id.right_giu);
        left=dialog.findViewById(R.id.tv_left);
        right=dialog.findViewById(R.id.tv_right);
        db=dialog.findViewById(R.id.tv_db);
        db_piu=dialog.findViewById(R.id.db_su);
        db_meno=dialog.findViewById(R.id.db_giu);
        ecu=dialog.findViewById(R.id.ecu);

        updateUI();
    }
    @SuppressLint("ClickableViewAccessibility")
    private void onClick(){
        ok.setOnClickListener(view -> {

            dialog.dismiss();
        });
        db_piu.setOnClickListener(view -> {
            updateUI();
        });
        db_meno.setOnClickListener(view -> {
            updateUI();
        });
        left_su.setOnClickListener(view -> {
            updateUI();
        });
        left_giu.setOnClickListener(view -> {
            updateUI();
        });
        right_su.setOnClickListener(view -> {
            updateUI();
        });
        right_giu.setOnClickListener(view -> {
            updateUI();
        });

        left_su.setOnTouchListener((v, event) -> {
            if (CanService.ECU_Connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 13, 1, 0, (byte) 254, 0, 0, 0, 0});

                        return false;

                    case MotionEvent.ACTION_UP:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 13, 0, 0, (byte) 254, 0, 0, 0, 0});
                        updateUI();
                        return false;
                }
            }

            return false;
        });
        left_giu.setOnTouchListener((v, event) -> {
            if (CanService.ECU_Connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 13, 0, 1, (byte) 254, 0, 0, 0, 0});

                        return false;

                    case MotionEvent.ACTION_UP:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 13, 0, 0, (byte) 254, 0, 0, 0, 0});
                        updateUI();
                        return false;
                }
            }

            return false;
        });
        right_su.setOnTouchListener((v, event) -> {
            if (CanService.ECU_Connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 14, 1, 0, (byte) 254, 0, 0, 0, 0});

                        return false;

                    case MotionEvent.ACTION_UP:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 14, 0, 0, (byte) 254, 0, 0, 0, 0});
                        updateUI();

                        return false;
                }
            }

            return false;
        });
        right_giu.setOnTouchListener((v, event) -> {
            if (CanService.ECU_Connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 14, 0, 1, (byte) 254, 0, 0, 0, 0});

                        return false;

                    case MotionEvent.ACTION_UP:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 14, 0, 0, (byte) 254, 0, 0, 0, 0});
                        updateUI();
                        return false;
                }
            }

            return false;
        });


        db_piu.setOnTouchListener((v, event) -> {
            if (CanService.ECU_Connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 15, 1, 0, (byte) 254, 0, 0, 0, 0});

                        return false;

                    case MotionEvent.ACTION_UP:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 15, 0, 0, (byte) 254, 0, 0, 0, 0});
                        updateUI();

                        return false;
                }
            }

            return false;
        });
        db_meno.setOnTouchListener((v, event) -> {
            if (CanService.ECU_Connected) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 15, 0, 1, (byte) 254, 0, 0, 0, 0});

                        return false;

                    case MotionEvent.ACTION_UP:
                        MyDeviceManager.CanWrite(1, 2051, 8, new byte[]{(byte) 15, 0, 0, (byte) 254, 0, 0, 0, 0});
                        updateUI();
                        return false;
                }
            }

            return false;
        });

    }
    private void updateUI(){
        left.setText(String.valueOf(CanService.left_Gain));
        right.setText(String.valueOf(CanService.right_Gain));
        db.setText(Utils.readSensorCalibration(String.valueOf(CanService.elevationDB * 0.001)) + " " + Utils.getMetriSimbol());
        if(!CanService.ECU_Connected){
            ecu.setVisibility(View.VISIBLE);
        }else {
            ecu.setVisibility(View.GONE);
        }
    }
}
