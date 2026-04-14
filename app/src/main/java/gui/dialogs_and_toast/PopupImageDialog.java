package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import utils.FullscreenActivity;

public class PopupImageDialog {

    Activity activity;
    public Dialog dialog;
    int resource;

    public PopupImageDialog(Activity activity, int resource) {
        this.activity = activity;
        this.resource = resource;
        dialog = new Dialog(activity);
        dialog.setContentView(resource);
    }

    public void show() {
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        dialog.getWindow().setLayout(1024, WindowManager.LayoutParams.MATCH_PARENT);
        FullscreenActivity.setFullScreen(dialog);

    }
}
