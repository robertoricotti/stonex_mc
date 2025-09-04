package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.stx_dig.R;

import gui.gps.NmeaGenerator;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_Edit_Coordinate_Demo {

    int index = 0;
    Activity activity;
    public Dialog dialog;
    Button chiudi, calcola;
    EditText est, nord, zeta;

    public Dialog_Edit_Coordinate_Demo(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_edit_coordinate_demo, null));

        dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        index = 0;
    }

    private void findView() {
        est = dialog.findViewById(R.id.utmest);
        nord = dialog.findViewById(R.id.utmnord);
        zeta = dialog.findViewById(R.id.utmz);
        chiudi = dialog.findViewById(R.id.chiudi);
        calcola = dialog.findViewById(R.id.calcola);

    }

    private void init() {
        try {


            est.setText(String.format("%.3f", DataSaved.demoEAST).replaceAll(",","."));
            nord.setText(String.format("%.3f", DataSaved.demoNORD).replaceAll(",","."));
            zeta.setText(String.format("%.3f", DataSaved.demoZ).replaceAll(",","."));


        } catch (Exception e) {
            new CustomToast(activity, "Error Coordinates").show_error();
        }
    }

    private void onClick() {
        est.setOnClickListener(view -> {
            index = 1;
            new CustomNumberDialog(activity, 100).show(est);
        });
        nord.setOnClickListener(view -> {
            index = 2;
            new CustomNumberDialog(activity, 100).show(nord);
        });
        zeta.setOnClickListener(view -> {
            index = 3;
            new CustomNumberDialog(activity, 100).show(zeta);
        });

        calcola.setOnClickListener(view -> {
            dialog.dismiss();
        });
        chiudi.setOnClickListener(view -> {
            save();
        });

    }


    private void save() {
        if (!nord.getText().toString().isEmpty()) {
            DataSaved.demoNORD = Double.parseDouble(nord.getText().toString().replace(",", "."));
            NmeaGenerator.LATITUDE = DataSaved.demoNORD;
            MyData.push("demoNORD", String.valueOf(DataSaved.demoNORD));
        }
        if (!est.getText().toString().isEmpty()) {
            DataSaved.demoEAST = Double.parseDouble(est.getText().toString().replace(",", "."));
            NmeaGenerator.LONGITUDE = DataSaved.demoEAST;
            MyData.push("demoEAST", String.valueOf(DataSaved.demoEAST));
        }
        if (!zeta.getText().toString().isEmpty()) {
            DataSaved.demoZ = Double.parseDouble(zeta.getText().toString().replace(",", "."));
            NmeaGenerator.ALTITUDE = DataSaved.demoZ;
            MyData.push("demoZ", String.valueOf(DataSaved.demoZ));
        }


        dialog.dismiss();

    }

}
