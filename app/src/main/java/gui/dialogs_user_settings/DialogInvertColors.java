package gui.dialogs_user_settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
        dialog.setContentView(R.layout.dialog__invert_colors);

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
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);

        findView();
        onClick();
        change();


    }
}
