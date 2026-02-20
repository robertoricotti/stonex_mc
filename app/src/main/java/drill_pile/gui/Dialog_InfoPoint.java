package drill_pile.gui;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import java.util.List;

import gui.tech_menu.DampingActivity;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import services.PointService;
import utils.FullscreenActivity;
import utils.MyMCUtils;

public class Dialog_InfoPoint {
    Activity activity;
    public Dialog dialog;
    ImageView close;
    TextView actualAzimut, actualPitch, actualRoll, actualTilt, targetAzimut, targetPitch, targetRoll, targetTilt,titolo,titoloUP;
    private boolean isUpdating = false;
    private Handler handler;


    public Dialog_InfoPoint(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_infopoint);
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
        int width = (int) (displayMetrics.widthPixels * 0.8);
        int height = (int) (displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        findView();
        onClick();
        init();
        startUpdatingCoordinates();
        FullscreenActivity.setFullScreen(dialog);

    }

    private void findView() {
        close = dialog.findViewById(R.id.chiudi);
        actualAzimut = dialog.findViewById(R.id.actualAzimut);
        actualPitch = dialog.findViewById(R.id.actualPitch);
        actualRoll = dialog.findViewById(R.id.actualRoll);
        actualTilt = dialog.findViewById(R.id.actualTilt);
        targetAzimut = dialog.findViewById(R.id.targetAzimut);
        targetPitch = dialog.findViewById(R.id.targetPitch);
        targetRoll = dialog.findViewById(R.id.targetRoll);
        targetTilt = dialog.findViewById(R.id.targetTilt);
        titolo=dialog.findViewById(R.id.titolo);
        titoloUP=dialog.findViewById(R.id.titoloUP);

    }
    private void init(){
        int[] stati=getPointStatus(DataSaved.drill_points);
        // Update coord TextView with new coordinates

        String tit="TOTAL:"+DataSaved.drill_points.size()+"  -  TO DO:"+stati[0]+"  -  DONE:"+stati[2]+"  -  REFUSED:"+stati[1];
        String mid="";
        if(!DataSaved.Selected_Point3D_Drill.getRowId().isEmpty()) {
            mid=DataSaved.Selected_Point3D_Drill.getRowId() + "-" + DataSaved.Selected_Point3D_Drill.getId();

        }else {
            mid=DataSaved.Selected_Point3D_Drill.getId();

        }
        titoloUP.setText(tit);
        titolo.setText(mid);

    }

    private void onClick() {
        close.setOnClickListener(view -> {
            stopUpdatingCoordinates();
            dialog.dismiss();
        });

    }

    private void updateCoordinates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {

                    double masAzimut= My_LocationCalc.calcBearingXY(
                            ExcavatorLib.coordTool[0], ExcavatorLib.coordTool[1],
                            ExcavatorLib.toolEndCoord[0], ExcavatorLib.toolEndCoord[1]);

                    actualAzimut.setText(String.format("%.2f",masAzimut).replace(",","."));
                    actualPitch.setText(String.format("%.2f",ExcavatorLib.correctToolPitch).replace(",","."));
                    actualRoll.setText(String.format("%.2f",ExcavatorLib.correctToolRoll).replace(",","."));
                    actualTilt.setText(String.format("%.2f", MyMCUtils.calculateTotalTilt(ExcavatorLib.correctToolPitch,ExcavatorLib.correctToolRoll)).replace(",","."));

                    targetAzimut.setText(String.format("%.2f",DataSaved.Selected_Point3D_Drill.getHeadingDeg()).replace(",","."));
                    targetPitch.setText(String.format("%.2f", PointService.holePitchDeg).replace(",","."));
                    targetRoll.setText(String.format("%.2f", PointService.holeRollDeg).replace(",","."));
                    targetTilt.setText(String.format("%.2f", MyMCUtils.calculateTotalTilt(PointService.holePitchDeg,PointService.holeRollDeg)).replace(",","."));




                    if (isUpdating) {
                        updateCoordinates();
                    }
                } catch (Exception e) {
                }
            }
        }, 100);
    }


    private void startUpdatingCoordinates() {
        if (!isUpdating) {
            isUpdating = true;
            handler = new Handler();
            updateCoordinates();
        }
    }

    private void stopUpdatingCoordinates() {
        if (isUpdating) {
            isUpdating = false;
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
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
