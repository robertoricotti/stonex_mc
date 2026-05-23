package drill_pile.gui;

import static packexcalib.exca.DataSaved.DRILL_STATUS;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import utils.FullscreenActivity;

public class Dialog_Error_Codes_Drill {
    Activity activity;

    public Dialog dialog;
    ImageView close;
    TextView messaggio;
    DisplayMetrics displayMetrics;
    int larg = 1000, alt = 600;
    String string="";

    public Dialog_Error_Codes_Drill(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        displayMetrics = new DisplayMetrics();
    }

    public void show() {

        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        larg = (int) (displayMetrics.widthPixels * 0.65);
        alt = (int) (displayMetrics.heightPixels * 0.45);
        dialog.create();
        dialog.setContentView(R.layout.dialog_error_codes_drill);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.15f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 🔹 Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        dialog.getWindow().setLayout(larg, alt);
        wlp.gravity = Gravity.CENTER;

        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();

    }

    private void findView() {
        close = dialog.findViewById(R.id.close);
        messaggio = dialog.findViewById(R.id.messaggio);
        string = switch (DRILL_STATUS) {
            case 0 -> "NO ERRORS ONLY TESTING MESSAGES";
            case 1 -> "PLUMBING IN PROGRESS";
            case 2 -> "DRILLING IN PROGRESS";
            case 3 -> "SLIDING IN PROGRESS";
            default -> "";
        };

    }

    private void init() {

        messaggio.setText(string);

    }

    private void onClick() {
        close.setOnClickListener(v -> {
            dialog.dismiss();
        });

    }

}
