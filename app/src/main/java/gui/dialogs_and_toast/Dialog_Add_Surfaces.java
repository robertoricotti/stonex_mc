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

import gui.projects.Usb_Project_Nova;
import utils.FullscreenActivity;


public class Dialog_Add_Surfaces {
    Activity activity;
    public Dialog dialog;
    ImageView close,usb,flat,ab,area,trincea,triangoli;
    String mPath;

    public Dialog_Add_Surfaces(Activity activity,String mPath){
        this.activity=activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        this.mPath=mPath;

    }
    public void show(){
        dialog.create();
        dialog.setContentView(R.layout.dialog_add_new_surgaces);
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
        close=dialog.findViewById(R.id.cancel);
        usb=dialog.findViewById(R.id.usb);
        flat=dialog.findViewById(R.id.flat);
        ab=dialog.findViewById(R.id.ab);
        area=dialog.findViewById(R.id.flatarea);
        trincea=dialog.findViewById(R.id.trench);
        triangoli=dialog.findViewById(R.id.terrein);

    }
    private void onClick(){
        close.setOnClickListener(view -> {
            dialog.dismiss();
        });
        usb.setOnClickListener(view -> {
            Intent intent=new Intent(activity, Usb_Project_Nova.class);
            intent.putExtra("usb",mPath);
            activity.startActivity(intent);
            activity.finish();
            dialog.dismiss();
        });
    }
}
