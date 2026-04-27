package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.stx_dig.R;

import gui.my_opengl.My3DActivity;
import gui.projects.Activity_Crea_Superficie;
import gui.projects.Dialog_PRJ_Folder;
import gui.projects.Usb_Project_Nova;
import services.ReadProjectService;
import utils.FullscreenActivity;


public class Dialog_Add_Surfaces {
    Activity activity;
    public Dialog dialog;
    ImageView close, usb, flat, ab, area, trincea, triangoli;
    String mPath;
    String chiamata = "HOME";

    public Dialog_Add_Surfaces(Activity activity, String mPath) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        this.mPath = mPath;
        if (activity instanceof My3DActivity) {
            chiamata = "DIG";
        } else {
            chiamata = "HOME";
        }

    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_add_new_surgaces);
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
        int width = (int) (displayMetrics.widthPixels * 0.8);
        int height = (int) (displayMetrics.heightPixels * 0.8);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
    }

    private void findView() {
        close = dialog.findViewById(R.id.cancel);
        usb = dialog.findViewById(R.id.usb);
        flat = dialog.findViewById(R.id.flat);
        ab = dialog.findViewById(R.id.ab);
        area = dialog.findViewById(R.id.flatarea);
        trincea = dialog.findViewById(R.id.trench);
        triangoli = dialog.findViewById(R.id.terrein);

    }

    private void onClick() {

        close.setOnClickListener(view -> {
            dialog.dismiss();
        });
        usb.setOnClickListener(view -> {
            Intent intent = new Intent(activity, Usb_Project_Nova.class);
            intent.putExtra("usb", mPath);
            activity.startActivity(intent);
            activity.finish();
            dialog.dismiss();
        });

        flat.setOnClickListener(view -> {
            Dialog_PRJ_Folder dialogPrjFolder = new Dialog_PRJ_Folder(activity);
            if (dialogPrjFolder.dialog.isShowing()) {
                dialogPrjFolder.dialog.dismiss();
            }
            Intent intent = new Intent(activity, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "PLAN");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            intent.putExtra("whoPRJ", chiamata);
            intent.putExtra("mPath", mPath);//scegliere path se Projects o dentro folder
            activity.startActivity(intent);
            dialog.dismiss();
            activity.finish();
        });
        ab.setOnClickListener(view -> {
            Dialog_PRJ_Folder dialogPrjFolder = new Dialog_PRJ_Folder(activity);
            if (dialogPrjFolder.dialog.isShowing()) {
                dialogPrjFolder.dialog.dismiss();
            }
            Intent intent = new Intent(activity, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "AB");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            intent.putExtra("whoPRJ", chiamata);
            intent.putExtra("mPath", mPath);
            activity.startActivity(intent);
            dialog.dismiss();
            activity.finish();
        });
        triangoli.setOnClickListener(view -> {
            Dialog_PRJ_Folder dialogPrjFolder = new Dialog_PRJ_Folder(activity);
            if (dialogPrjFolder.dialog.isShowing()) {
                dialogPrjFolder.dialog.dismiss();
            }
            Intent intent = new Intent(activity, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "TRIANGLES");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            intent.putExtra("whoPRJ", chiamata);
            intent.putExtra("mPath", mPath);
            activity.startActivity(intent);
            dialog.dismiss();
            activity.finish();
        });
        area.setOnClickListener(view -> {
            Dialog_PRJ_Folder dialogPrjFolder = new Dialog_PRJ_Folder(activity);
            if (dialogPrjFolder.dialog.isShowing()) {
                dialogPrjFolder.dialog.dismiss();
            }
            Intent intent = new Intent(activity, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "AREA");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            intent.putExtra("whoPRJ", chiamata);
            intent.putExtra("mPath", mPath);
            activity.startActivity(intent);
            dialog.dismiss();
            activity.finish();
        });
        trincea.setOnClickListener(view -> {
            Dialog_PRJ_Folder dialogPrjFolder = new Dialog_PRJ_Folder(activity);
            if (dialogPrjFolder.dialog.isShowing()) {
                dialogPrjFolder.dialog.dismiss();
            }
            Intent intent = new Intent(activity, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "TRENCH");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            intent.putExtra("whoPRJ", chiamata);
            intent.putExtra("mPath", mPath);
            activity.startActivity(intent);
            dialog.dismiss();
            activity.finish();
        });
    }
}
