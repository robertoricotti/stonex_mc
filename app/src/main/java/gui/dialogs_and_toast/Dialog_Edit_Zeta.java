package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.projects.Activity_Crea_Superficie;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Edit_Zeta {
    private boolean isSaving = false;
    Activity activity;
    public Dialog dialog;
    Button save;
    TextView est, nord, uom, titolo;
    EditText zeta;
    int index;

    public Dialog_Edit_Zeta(Activity activity, int index) {

        this.activity = activity;
        this.index = index;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        isSaving = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_modify_z, null));

        dialog = builder.create();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        if (Build.BRAND.equals("SRT8PROS")) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
    }

    private void findView() {
        save = dialog.findViewById(R.id.ok);
        titolo = dialog.findViewById(R.id.title);
        est = dialog.findViewById(R.id.txtest);
        nord = dialog.findViewById(R.id.txtnord);
        zeta = dialog.findViewById(R.id.etquota);
        uom = dialog.findViewById(R.id.txtuom);
        DataSaved.offset_Z_antenna = 0;


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
            case 100:
                titolo.setText("APPLY Z VALUE");
                //Activity_Crea_Superficie is calling
                est.setText(Utils.readSensorCalibration(String.valueOf(Activity_Crea_Superficie.coordinateP.get(0)[0])));
                nord.setText(Utils.readSensorCalibration(String.valueOf(Activity_Crea_Superficie.coordinateP.get(0)[1])));
                zeta.setText(Utils.readSensorCalibration(String.valueOf(Activity_Crea_Superficie.coordinateP.get(0)[2])));
                break;
        }


    }

    private void pickA() {
    if (activity instanceof Activity_Crea_Superficie) {
            dialog.dismiss();
        }
    }

    private void onClick() {
        zeta.setOnClickListener(view -> {
            int index = MyData.get_Int("Unit_Of_Measure");
            if (index == 4 || index == 5) {
                new CustomNumberDialogFtIn(activity, 0).show(zeta);
            } else {
                new CustomNumberDialog(activity, 0).show(zeta);
            }
        });
        save.setOnClickListener(view -> {
            if (activity instanceof Activity_Crea_Superficie && !isSaving) {
                isSaving = true;
                for (int i = 0; i < Activity_Crea_Superficie.coordinateP.size(); i++) {
                    Activity_Crea_Superficie.coordinateP.get(i)[2] = Double.parseDouble(Utils.writeMetri(zeta.getText().toString()));
                }
                new Handler().postDelayed(this::pickA, 500);
            } else {
                if (!isSaving) {
                    isSaving = true;
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
                        DataSaved.offset_Z_antenna = 0;
                    }

                    new Handler().postDelayed(this::pickA, 1500);
                }
            }
        });
    }
}
