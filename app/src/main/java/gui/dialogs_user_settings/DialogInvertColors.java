package gui.dialogs_user_settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.stx_dig.R;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class DialogInvertColors {
    public Dialog dialog;
    public Activity activity;

    ImageView save, exit;
    LinearLayout def,opt;

    public DialogInvertColors(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_invert_colors);

    }

    public void show() {
        init();
    }

    private void findView() {
        save = dialog.findViewById(R.id.save);
        exit = dialog.findViewById(R.id.exit);
        def=dialog.findViewById(R.id.option_def);
        opt=dialog.findViewById(R.id.option_custom);

    }

    private void onClick() {
        save.setOnClickListener(view -> {
            MyData.push("colorMode",String.valueOf(DataSaved.colorMode));
            dialog.dismiss();

        });

        exit.setOnClickListener(view -> {
            dialog.dismiss();
        });
        def.setOnClickListener(view -> {
            DataSaved.colorMode=0;
            change();
        });
        opt.setOnClickListener(view -> {
            DataSaved.colorMode=1;
            change();
        });


    }
    private void change(){
        switch (DataSaved.colorMode){
            case 0:
                opt.setAlpha(0.3f);
                def.setAlpha(1.0f);
                break;
            case 1:
                opt.setAlpha(1.0f);
                def.setAlpha(0.3f);
                break;
        }

    }



    @SuppressLint("SetTextI18n")
    private void init() {
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
        int width = (int) (displayMetrics.widthPixels * 0.75);
        int height = (int) (displayMetrics.heightPixels * 0.65);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);

        findView();
        onClick();
        change();


    }
}
