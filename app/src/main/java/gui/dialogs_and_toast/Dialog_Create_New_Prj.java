package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.stx_dig.R;

import gui.projects.Remote_Activity;
import gui.projects.Usb_Project_Nova;
import utils.FullscreenActivity;

public class Dialog_Create_New_Prj {
    Activity activity;
    public Dialog dialog;
    ImageView cancel, nuovo, daUsb,dacloud;
    CustomQwertyDialog customQwertyDialog;
    public Dialog_Create_New_Prj(Activity activity){
        this.activity=activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }
    public void show(){
        dialog.create();
        dialog.setContentView(R.layout.dialog_create_new_prj);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.75);
        int height = (int) (displayMetrics.heightPixels * 0.65);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
    }
    private void findView(){
        customQwertyDialog=new CustomQwertyDialog(activity);
        cancel=dialog.findViewById(R.id.cancel);
        nuovo=dialog.findViewById(R.id.nuovo);
        daUsb =dialog.findViewById(R.id.usb);
        dacloud=dialog.findViewById(R.id.add);

    }
    private void onClick(){
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        nuovo.setOnClickListener(view -> {

            if(!customQwertyDialog.dialog.isShowing()){
                customQwertyDialog.show(997,null,null,null);
                dialog.dismiss();
            }

        });

        daUsb.setOnClickListener(view -> {

            activity.startActivity(new Intent(activity, Usb_Project_Nova.class));
            activity.overridePendingTransition(0, 0);
            activity.finish();
            dialog.dismiss();
        });
        dacloud.setOnClickListener(view -> {
            new CustomToast(activity, "NOT IMPLEMENTED").show();
            Intent intent = new Intent(activity, Remote_Activity.class);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
            activity.finish();
            dialog.dismiss();
        });
    }
}
