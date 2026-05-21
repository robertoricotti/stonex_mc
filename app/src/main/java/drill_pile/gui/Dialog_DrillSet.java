package drill_pile.gui;

import static gui.dialogs_and_toast.DialogPassword.isTech;
import static packexcalib.exca.DataSaved.Unit_Of_Measure;
import static utils.MyTypes.SOLARFARM_MODE;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import utils.FullscreenActivity;
import utils.MyData;
import utils.MyDeviceManager;
import utils.Utils;

public class Dialog_DrillSet {
    Activity activity;
    public Dialog dialog;
    ImageView close, toLeft, toWard, toRight;
    int tempScreen, finalScreen;
    TextView angleTit, distTit,psav;
    EditText tv1, tv2, tv3, tv4, tv5;
    CheckBox ckSavingMode;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    int mchint;


    public Dialog_DrillSet(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        finalScreen = -2;
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
        int width = (int) (displayMetrics.widthPixels);
        int height = (int) (displayMetrics.heightPixels);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.show();
        findView();
        onClick();
        FullscreenActivity.setFullScreen(dialog);

    }

    private void findView() {
        mchint = MyData.get_Int("MachineSelected");
        customNumberDialog = new CustomNumberDialog(activity, -1);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -1);
        close = dialog.findViewById(R.id.chiudi);
        angleTit = dialog.findViewById(R.id.angleTit);
        distTit = dialog.findViewById(R.id.distTit);
        distTit.setText("DISTANCES  " + Utils.getMetriSimbol());
        psav=dialog.findViewById(R.id.psav);
        tv1 = dialog.findViewById(R.id.tv1);
        tv2 = dialog.findViewById(R.id.tv2);
        tv3 = dialog.findViewById(R.id.tv3);
        tv4 = dialog.findViewById(R.id.tv4);
        tv5 = dialog.findViewById(R.id.tv5);

        ckSavingMode=dialog.findViewById(R.id.ckSavingMode);
        ckSavingMode.setChecked(DataSaved.autoSavePoint==1);
        if(DataSaved.Drilling_Mode==SOLARFARM_MODE){
            ckSavingMode.setVisibility(View.VISIBLE);
            psav.setVisibility(View.VISIBLE);

        }else{
            ckSavingMode.setVisibility(View.GONE);
            psav.setVisibility(View.GONE);
        }

        tv1.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Drill_tolleranza_XY)));
        tv2.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Drill_tolleranza_Axis)));
        tv3.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.Drill_tolleranza_Z)));
        tv4.setText((String.valueOf(DataSaved.Drill_tolleranza_Angolo)));
        tv5.setText((String.valueOf(DataSaved.Drill_tolleranza_HDT)));
        toLeft = dialog.findViewById(R.id.screetoLeft);
        toWard = dialog.findViewById(R.id.screetoTop);
        toRight = dialog.findViewById(R.id.screetoRight);

        tempScreen = DataSaved.Drill_Screen;
        updateImage(tempScreen);



    }

    private void onClick() {
        ckSavingMode.setOnClickListener(v -> {
            DataSaved.autoSavePoint+=1;
            DataSaved.autoSavePoint=DataSaved.autoSavePoint%2;
            ckSavingMode.setChecked(DataSaved.autoSavePoint==1);
            MyData.push("autoSavePoint",String.valueOf( DataSaved.autoSavePoint));
        });

        toLeft.setOnClickListener(view -> {
            tempScreen = -1;
            DataSaved.Drill_Screen = -1;
            finalScreen = tempScreen;
            updateImage(tempScreen);
        });
        toWard.setOnClickListener(view -> {
            tempScreen = 0;
            DataSaved.Drill_Screen = 0;
            finalScreen = tempScreen;
            updateImage(tempScreen);
        });
        toRight.setOnClickListener(view -> {
            tempScreen = 1;
            DataSaved.Drill_Screen = 1;
            finalScreen = tempScreen;
            updateImage(tempScreen);
        });

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

    private void save() {
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
            sendToEcu();
            if (tempScreen == finalScreen) {
                MyData.push("Drill_Screen", String.valueOf(DataSaved.Drill_Screen));
                new CustomToast(activity, "Restart Working Page").show_alert();
            }
        } catch (Exception e) {
            new CustomToast(activity, "Error...").show_error();
        }

    }

    private void updateImage(int screen) {
        ckSavingMode.setChecked(DataSaved.autoSavePoint==1);

        switch (screen) {
            case -1:
                toLeft.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                toWard.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                toRight.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                break;

            case 0:
                toLeft.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                toWard.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                toRight.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                break;

            case 1:
                toLeft.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                toWard.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_trasparente));
                toRight.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_mch_selezionata));
                break;

        }
    }

    private void sendToEcu() {
        Handler handler = new Handler(Looper.getMainLooper());

        byte[] tollXY_ = PLC_DataTypes_LittleEndian.U16_to_bytes((int) (DataSaved.Drill_tolleranza_XY*0.5 * 1000));
        byte[] tollHam_ = PLC_DataTypes_LittleEndian.U16_to_bytes((int) (DataSaved.Drill_tolleranza_Z * 1000));
        byte[] tollAn_ = PLC_DataTypes_LittleEndian.U16_to_bytes((int) (DataSaved.Drill_tolleranza_Angolo*0.5 * 1000));
        byte[] windowT = PLC_DataTypes_LittleEndian.U16_to_bytes(35);

        handler.post(() ->
                MyDeviceManager.CanWrite(true, 1, 0x73, 8, new byte[]{(byte) 132, tollAn_[0], tollAn_[1], 0, 0, 0, 0, 0})
        );

        handler.postDelayed(() ->
                        MyDeviceManager.CanWrite(true, 1, 0x73, 8, new byte[]{(byte) 130, tollHam_[0], tollHam_[1], 0, 0, 0, 0, 0})
                , 100);

        handler.postDelayed(() ->
                        MyDeviceManager.CanWrite(true, 1, 0x73, 8, new byte[]{(byte) 129, tollXY_[0], tollXY_[1], 0, 0, 0, 0, 0})
                , 200);

        handler.postDelayed(() ->
                        MyDeviceManager.CanWrite(true, 1, 0x73, 8, new byte[]{(byte) 128, windowT[0], windowT[1], 0, 0, 0, 0, 0})
                , 300);
    }

}
