package drill_pile.gui;

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

import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_Add_Rod {
    Activity activity;
    public Dialog dialog;
    ImageView bt_meno, bt_piu, deleteAll, save_;
    TextView title_, txtrod;
    int indexMachine;

    public Dialog_Add_Rod(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_add_rod);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); //  Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        // Calcola % della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.5);
        int height = (int) (displayMetrics.heightPixels * 0.5);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        findView();
        init();
        onClick();
        FullscreenActivity.setFullScreen(dialog);

    }

    private void findView() {
        indexMachine = MyData.get_Int("MachineSelected");
        bt_meno = dialog.findViewById(R.id.bt_meno);
        bt_piu = dialog.findViewById(R.id.bt_piu);
        deleteAll = dialog.findViewById(R.id.deleteAll);
        title_ = dialog.findViewById(R.id.title_);
        txtrod = dialog.findViewById(R.id.txtrod);
        save_ = dialog.findViewById(R.id.save_);

    }

    private void init() {
        updateTxt();
    }

    private void onClick() {
        save_.setOnClickListener(view -> {
            salva();
            dialog.dismiss();
        });
        bt_piu.setOnClickListener(view -> {
            DataSaved.numeroAste += 1;
            updateTxt();
        });
        bt_meno.setOnClickListener(view -> {
            if(DataSaved.numeroAste>0) {
                DataSaved.numeroAste -= 1;
                updateTxt();
            }

        });
        deleteAll.setOnClickListener(view -> {
            DataSaved.numeroAste =0;
            updateTxt();
        });

    }

    private void updateTxt() {

        txtrod.setText(String.valueOf(DataSaved.numeroAste));
    }

    private void salva() {

        MyData.push("M" + indexMachine + "numeroAste", String.valueOf(DataSaved.numeroAste));

    }
}
