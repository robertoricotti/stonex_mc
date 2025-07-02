package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Edit_Zeta_DXF {



    Activity activity;
    public Dialog alertDialog;
    Button save, canc;
    TextView est, nord, uom;
    EditText zeta;
    ImageView close;
    int index;

    public Dialog_Edit_Zeta_DXF(Activity activity, int index) {

        this.activity = activity;
        this.index = index;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_modify_z_dxf, null));

        alertDialog = builder.create();
        alertDialog.setCancelable(true);
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        alertDialog.show();
        if (Build.BRAND.equals("SRT8PROS")) {
            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        FullscreenActivity.setFullScreen(alertDialog);
        findView();
        init();
        onClick();
    }

    private void findView() {
        save = alertDialog.findViewById(R.id.ok);
        canc = alertDialog.findViewById(R.id.delete);
        est = alertDialog.findViewById(R.id.txtest);
        nord = alertDialog.findViewById(R.id.txtnord);
        zeta = alertDialog.findViewById(R.id.etquota);
        uom = alertDialog.findViewById(R.id.txtuom);
        close=alertDialog.findViewById(R.id._btnclose);



    }

    private void init() {
        uom.setText("Z  " + Utils.getMetriSimbol() + " :");
        switch (index) {

            case -1:
                est.setText("E  " + Utils.getMetriSimbol() + " :" + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketLeftCoord[0])));
                nord.setText("N  " + Utils.getMetriSimbol() + " :" + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketLeftCoord[1])));
                zeta.setText(Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketLeftCoord[2])));
                break;

            case 0:
                est.setText("E  " + Utils.getMetriSimbol() + " :" + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[0])));
                nord.setText("N  " + Utils.getMetriSimbol() + " :" + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[1])));
                zeta.setText(Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketCoord[2])));
                break;
            case 1:
                est.setText("E  " + Utils.getMetriSimbol() + " :" + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketRightCoord[0])));
                nord.setText("N  " + Utils.getMetriSimbol() + " :" + Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketRightCoord[1])));
                zeta.setText(Utils.readSensorCalibration(String.valueOf(ExcavatorLib.bucketRightCoord[2])));
                break;
        }


    }


    private void onClick() {
        close.setOnClickListener(view -> {
            alertDialog.dismiss();
        });
        canc.setOnClickListener(view -> {
           MyData.push("ZDXF","0.0");
            DataSaved.offset_Z_antenna=0;
            alertDialog.dismiss();
        });
        zeta.setOnClickListener(view -> {
            int index = MyData.get_Int("Unit_Of_Measure");
            if (index == 4 || index == 5) {
                new CustomNumberDialogFtIn(activity, 100).show(zeta);
            } else {
                new CustomNumberDialog(activity, 100).show(zeta);
            }
        });
        save.setOnClickListener(view -> {
            MyData.push("ZDXF","0.0");
            DataSaved.offset_Z_antenna=0;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {

            }

            double value =Double.parseDouble(Utils.writeMetri(zeta.getText().toString()));
                try {

                    switch (index) {
                        case -1:
                            DataSaved.offset_Z_antenna = (value - ExcavatorLib.bucketLeftCoord[2]);
                            break;
                        case 0:
                            DataSaved.offset_Z_antenna = (value - ExcavatorLib.bucketCoord[2]);
                            break;
                        case 1:
                            DataSaved.offset_Z_antenna = (value - ExcavatorLib.bucketRightCoord[2]);
                            break;
                    }
                } catch (Exception e) {
                    Log.e("ErrorZ",e.toString());
                    DataSaved.offset_Z_antenna = 0;
                }
                MyData.push("ZDXF", String.valueOf(DataSaved.offset_Z_antenna));
                alertDialog.dismiss();



        });
    }
}
