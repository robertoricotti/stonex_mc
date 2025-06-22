package gui.dialogs_user_settings;


import static gui.MyApp.isApollo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import com.example.stx_dig.R;

import gui.digging_excavator.Digging3D_DXF;
import gui.my_opengl.My3DActivity;
import utils.FullscreenActivity;
import utils.MyData;

public class DialogAudioSystem {
    Activity activity;
   public Dialog alertDialog;
    CheckBox cbxOn, cbxOff,cbOn2;
    Button save, exit;
    int index = 0;
    SeekBar volume;


    public DialogAudioSystem(Activity activity) {
        this.activity = activity;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        if(isApollo){
        builder.setView(inflater.inflate(R.layout.dialog_audio, null));}
        else {
            builder.setView(inflater.inflate(R.layout.dialog_audio_s80, null));
        }
        builder.setCancelable(false);
        alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        alertDialog.show();
        if (MyData.get_String("BUILD").equals("APOLLO2_7")|| Build.BRAND.equals("APOLLO2_12_PRO")||Build.BRAND.equals("APOLLO2_12_PLUS")) {

            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }else {
            alertDialog.getWindow().setLayout(900, 400);
        }
        FullscreenActivity.setFullScreen(alertDialog);
        findView();
        init();
        onClick();
        onCheckedChange();
    }

    private void findView(){
        cbxOn = alertDialog.findViewById(R.id.cbxOn);
        cbxOff = alertDialog.findViewById(R.id.cbxOff);
        cbOn2=alertDialog.findViewById(R.id.cbxOn2);
        volume = alertDialog.findViewById(R.id.volume);
        save = alertDialog.findViewById(R.id.save);
        exit = alertDialog.findViewById(R.id.exit);
    }

    private void init(){

        index = MyData.get_Int("indexAudioSystem");

        volume.setProgress(MyData.get_Int("volumeAudioSystem"));

        switch (index){
            case  0:
                cbxOff.setChecked(true);
                volume.setVisibility(View.INVISIBLE);
                break;
            case  1:
                cbxOn.setChecked(true);
                volume.setVisibility(View.VISIBLE);
                break;
            case  2:
                cbOn2.setChecked(true);
                volume.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void onClick(){
        exit.setOnClickListener((View v) ->{
            alertDialog.dismiss();
        });

        save.setOnClickListener((View v) -> {
            MyData.push("indexAudioSystem", String.valueOf(index));
            MyData.push("volumeAudioSystem", String.valueOf(volume.getProgress()));
            AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume.getProgress() * 10, 0);

            if(activity instanceof Digging3D_DXF|| activity instanceof My3DActivity){
                activity.recreate();
            }
            alertDialog.dismiss();
        });
    }

    private void onCheckedChange() {
        cbxOff.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxOff.isChecked()) {
                index = 0;
                cbxOn.setChecked(false);
                cbOn2.setChecked(false);
                volume.setVisibility(View.INVISIBLE);
            }
        });

        cbxOn.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxOn.isChecked()) {
                index = 1;
                cbxOff.setChecked(false);
                cbOn2.setChecked(false);
                volume.setVisibility(View.VISIBLE);
            }
        });
        cbOn2.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbOn2.isChecked()) {
                index = 2;
                cbxOff.setChecked(false);
                cbxOn.setChecked(false);
                volume.setVisibility(View.VISIBLE);
            }
        });
    }
}
