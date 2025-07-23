package gui.dialogs_user_settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class DialogBrightness {
    Activity activity;
    public Dialog dialog;
    TextView titolo;

    ImageView save, exit;
    int index = 0;
    SeekBar lumin;


    public DialogBrightness(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_brigthness, null));

        builder.setCancelable(false);
        dialog = builder.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();

    }
    private void findView(){

        lumin = dialog.findViewById(R.id.luminosita);
        save = dialog.findViewById(R.id.save);
        exit = dialog.findViewById(R.id.exit);
        titolo= dialog.findViewById(R.id.titoloF);
    }
    private void init() {
        lumin.setProgress((int) (DataSaved.myBrightness*10));
        titolo.setText(activity.getResources().getString(R.string.lumen)+"\n\n"+String.format("%.0f",DataSaved.myBrightness*100)+"%");

    }
    private void onClick(){
        exit.setOnClickListener((View v) ->{
            dialog.dismiss();
        });

        save.setOnClickListener((View v) -> {
            DataSaved.myBrightness=Float.parseFloat(String.valueOf((float) lumin.getProgress()/10));
            MyData.push("brightness", String.valueOf(DataSaved.myBrightness));
            WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
            layoutParams.screenBrightness = DataSaved.myBrightness; // Imposta il valore desiderato compreso tra 0.0f e 1.0f
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            dialog.dismiss();
        });
        lumin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                titolo.setText(activity.getResources().getString(R.string.lumen)+"\n\n"+progress*10+"%");


            }
        });
    }

}
