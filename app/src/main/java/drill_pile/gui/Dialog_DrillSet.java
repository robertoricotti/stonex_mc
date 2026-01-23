package drill_pile.gui;

import static packexcalib.exca.DataSaved.Unit_Of_Measure;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import gui.tech_menu.Nuova_Machine_Settings;
import packexcalib.exca.DataSaved;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class Dialog_DrillSet {
    Activity activity;
    public Dialog dialog;
    ImageView close;
    TextView angleTit,distTit;
    EditText tv1,tv2,tv3,tv4,tv5;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;




    public Dialog_DrillSet(Activity activity){
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }
    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_drillset);
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

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.65);
        int height = (int) (displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        findView();
        onClick();

    }

    private void findView(){
        customNumberDialog=new CustomNumberDialog(activity,-1);
        customNumberDialogFtIn=new CustomNumberDialogFtIn(activity,-1);
        close=dialog.findViewById(R.id.chiudi);
        angleTit=dialog.findViewById(R.id.angleTit);
        distTit=dialog.findViewById(R.id.distTit);
        distTit.setText("DISTANCES  " +Utils.getMetriSimbol());
        tv1=dialog.findViewById(R.id.tv1);
        tv2=dialog.findViewById(R.id.tv2);
        tv3=dialog.findViewById(R.id.tv3);
        tv4=dialog.findViewById(R.id.tv4);
        tv5=dialog.findViewById(R.id.tv5);

        tv1.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Drill_tolleranza_XY)));
        tv2.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Drill_tolleranza_Axis)));
        tv3.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Drill_tolleranza_Z)));
        tv4.setText(Utils.readAngolo(String.valueOf(DataSaved.Drill_tolleranza_Angolo)));
        tv5.setText(Utils.readAngolo(String.valueOf(DataSaved.Drill_tolleranza_HDT)));

    }
    private void onClick(){
        close.setOnClickListener(view -> {
            save();
            dialog.dismiss();
        });
        tv1.setOnClickListener((View v) -> {
            if (Unit_Of_Measure == 4 || Unit_Of_Measure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing())
                    customNumberDialogFtIn.show(tv1);
            } else {
                if (!customNumberDialog.dialog.isShowing())
                    customNumberDialog.show(tv1);
            }
        });
        tv2.setOnClickListener((View v) -> {
            if (Unit_Of_Measure == 4 || Unit_Of_Measure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing())
                    customNumberDialogFtIn.show(tv2);
            } else {
                if (!customNumberDialog.dialog.isShowing())
                    customNumberDialog.show(tv2);
            }
        });
        tv3.setOnClickListener((View v) -> {
            if (Unit_Of_Measure == 4 || Unit_Of_Measure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing())
                    customNumberDialogFtIn.show(tv3);
            } else {
                if (!customNumberDialog.dialog.isShowing())
                    customNumberDialog.show(tv3);
            }
        });
        tv4.setOnClickListener((View v) -> {
            if (Unit_Of_Measure == 4 || Unit_Of_Measure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing())
                    customNumberDialogFtIn.show(tv4);
            } else {
                if (!customNumberDialog.dialog.isShowing())
                    customNumberDialog.show(tv4);
            }
        });
        tv5.setOnClickListener((View v) -> {
            if (Unit_Of_Measure == 4 || Unit_Of_Measure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing())
                    customNumberDialogFtIn.show(tv5);
            } else {
                if (!customNumberDialog.dialog.isShowing())
                    customNumberDialog.show(tv5);
            }
        });

    }

    private void save(){
        try {

            DataSaved.Drill_tolleranza_XY = Double.parseDouble(Utils.writeMetri(tv1.getText().toString()));
            DataSaved.Drill_tolleranza_Axis = Double.parseDouble(Utils.writeMetri(tv2.getText().toString()));
            DataSaved.Drill_tolleranza_Z = Double.parseDouble(Utils.writeMetri(tv3.getText().toString()));
            DataSaved.Drill_tolleranza_Angolo = Double.parseDouble(String.valueOf(tv4.getText().toString()));
            DataSaved.Drill_tolleranza_HDT = Double.parseDouble(String.valueOf(tv5.getText().toString()));
            MyData.push("Drill_tolleranza_XY", String.valueOf(DataSaved.Drill_tolleranza_XY));
            MyData.push("Drill_tolleranza_Axis", String.valueOf(DataSaved.Drill_tolleranza_Axis));
            MyData.push("Drill_tolleranza_Z", String.valueOf(DataSaved.Drill_tolleranza_Z));
            MyData.push("Drill_tolleranza_Angolo", String.valueOf(DataSaved.Drill_tolleranza_Angolo));
            MyData.push("Drill_tolleranza_HDT", String.valueOf(DataSaved.Drill_tolleranza_HDT));
        } catch (Exception e) {
           new CustomToast(activity,"Error...").show_error();
        }

    }

}
