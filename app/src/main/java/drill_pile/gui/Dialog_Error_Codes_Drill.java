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
    int mDrillStatus;

    public Dialog_Error_Codes_Drill(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        displayMetrics = new DisplayMetrics();
    }

    public void show(int mDrillStatus) {

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
        this.mDrillStatus=mDrillStatus;
        findView();
        init();
        onClick();

    }

    private void findView() {
        close = dialog.findViewById(R.id.close);
        messaggio = dialog.findViewById(R.id.messaggio);
        string =errorMessage(mDrillStatus);

    }

    private void init() {

        messaggio.setText(string);

    }

    private void onClick() {
        close.setOnClickListener(v -> {
            dialog.dismiss();
        });

    }

    private static String errorMessage(int status) {
        String s = switch (status) {
            case 0 -> "NO ERRORS";

            case 10 -> "GPS not Fix or Machine not in Range";
            case 11 -> "Main Encoder ERROR Check Cabling";
            case 12 -> "Emergency Pressed or Remote Disabled";
            case 13 -> "Line Distance Not in Range for AutoSteer";

            case 14 -> "Safety Sensor 1";
            case 15 -> "Safety Sensor 2";
            case 16 -> "Safety Sensor 3";
            case 17 -> "Safety Sensor 4";
            case 18 -> "Safety Sensor 5";
            case 19 -> "Safety Sensor 6";

            case 20 -> "Machine or GPS Not in Range for AutoSteer";
            case 21 -> "Machine Orientation Not in Range for AutoSteer";
            case 22 -> "Mast Slope Not in Range for AutoSteer";
            case 23 -> "Next Pole Too Near AutoSteer not Permitted";
            case 24 -> "Nearest Pole Behind You. AutoSteer not Permitted";

            case 25 -> "Mast Tilt Sensor ERROR Check Cabling";
            case 26 -> "Machine Frame Sensor ERROR Check Cabling";
            case 27 -> "First IO Fault";
            case 28 -> "Second IO Fault";
            case 29 -> "Linear Encoder T Slide ERROR Check Cabling";
            case 30 -> "Linear Encoder L Slide ERROR Check Cabling";
            case 31 -> "Mast Position Encoder ERROR Check Cabling";
            case 32 -> "Oil Heating Active. Please Wait...";
            case 33 -> "Machine Frame Not in Range";
            case 256 -> "Log Suspended...Rods Changing";

            default -> " _ ";
        };

        return s;
    }

}
