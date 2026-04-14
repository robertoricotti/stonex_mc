package drill_pile.gui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.stx_dig.R;

import java.util.List;

import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_AutoSnap {


    FragmentActivity activity;
    public Dialog dialog;
    ImageView close, autosnap, pick, select;

    public Dialog_AutoSnap(FragmentActivity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_autosnap);
        dialog.setCancelable(false);
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

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.75);
        int height = (int) (displayMetrics.heightPixels * 0.55);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();

    }

    private void findView() {

        close = dialog.findViewById(R.id.chiudi);
        autosnap = dialog.findViewById(R.id.autosnap);
        pick = dialog.findViewById(R.id.pick_sel);
        select = dialog.findViewById(R.id.list_sel);
        checkStatus(DataSaved.isAutoSnap);


    }

    private void onClick() {
        close.setOnClickListener(view -> {
            dialog.dismiss();
        });

        select.setOnClickListener(view -> {
            DataSaved.isAutoSnap = 0;
            checkStatus(DataSaved.isAutoSnap);
            FragmentManager fm = activity.getSupportFragmentManager();
            String pointSiz = "";
            if (DataSaved.drill_points == null) {
                pointSiz = "No Points";
            } else {
                int[] stati = getPointStatus(DataSaved.drill_points);
                pointSiz = "TOTAL:" + DataSaved.drill_points.size() + "   DONE:" + stati[2] + "   REFUSED:" + stati[1];
            }

            if (fm.findFragmentByTag("drill_grid") != null) return;

            DrillPointsFullscreenDialog
                    .newInstance("Drill Pattern " + pointSiz, ReadProjectService.conversionFactor)
                    .show(fm, "drill_grid");
            dialog.dismiss();
        });
        autosnap.setOnClickListener(view -> {
            DataSaved.isAutoSnap = 1;
            checkStatus(DataSaved.isAutoSnap);

        });

        pick.setOnClickListener(view -> {
            DataSaved.isAutoSnap = 2;
            checkStatus(DataSaved.isAutoSnap);
        });


    }

    private void checkStatus(int status) {
        switch (status) {
            case 0:
                //Manuale
                select.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                autosnap.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_bianco));
                pick.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_bianco));
                break;

            case 1://Autosnap
                select.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_bianco));
                autosnap.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                pick.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_bianco));
                break;

            case 2:
                //Mode Pick
                select.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_bianco));
                autosnap.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_bianco));
                pick.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                break;

        }
        try {
            if (MyData.get_String("isAutosnap") == null) {
                MyData.push("isAutosnap", String.valueOf(DataSaved.isAutoSnap));
            } else {
                MyData.push("isAutosnap", String.valueOf(DataSaved.isAutoSnap));
            }
        } catch (Exception e) {
            MyData.push("isAutosnap", String.valueOf(0));
        }
    }

    private int[] getPointStatus(List<Point3D_Drill> points) {
        int todo = 0;
        int aborted = 0;
        int done = 0;

        for (Point3D_Drill p : points) {
            switch (p.getStatus()) {
                case 0:
                    todo++;
                    break;
                case -1:
                    aborted++;
                    break;
                case 1:
                    done++;
                    break;
                default:
                    // eventuale gestione stato sconosciuto
                    break;
            }
        }

        return new int[]{todo, aborted, done};
    }
}
