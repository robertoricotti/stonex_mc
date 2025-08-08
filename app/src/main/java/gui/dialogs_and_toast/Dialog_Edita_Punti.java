package gui.dialogs_and_toast;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Edita_Punti {
    int uom;
    Activity activity;
    public Dialog dialog;
    TextView nome,txEst,txNord,txZ,txSide;
    EditText est, nord, quota,side;
    Button save,exit;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    int counter;
    double[]coord;
    public static  double d_side;
    public Dialog_Edita_Punti(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        uom= MyData.get_Int("Unit_Of_Measure");
        customNumberDialog = new CustomNumberDialog(activity, 100);
        customNumberDialogFtIn=new CustomNumberDialogFtIn(activity,100);
    }
    public void show(int counter,double[]coord) {
        this.coord=coord;
        dialog.create();
        dialog.setContentView(R.layout.dialog_edit_punto);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        this.counter = counter;
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        update();
        onClick();


    }
    public void findView() {
        nome = dialog.findViewById(R.id.txtName);
        est = dialog.findViewById(R.id.valueEst);
        nord = dialog.findViewById(R.id.valueNord);
        quota = dialog.findViewById(R.id.valueZ);
        save = dialog.findViewById(R.id.save);
        txEst=dialog.findViewById(R.id.txtEst);
        txNord=dialog.findViewById(R.id.txtNord);
        txZ=dialog.findViewById(R.id.txtZ);
        txSide=dialog.findViewById(R.id.txtWidth);
        side=dialog.findViewById(R.id.valueW);
        exit=dialog.findViewById(R.id.exit);
    }
    public void init() {
        txEst.setText("EAST "+Utils.getMetriSimbol());
        txNord.setText("NORTH "+Utils.getMetriSimbol());
        txZ.setText("ELEVATION "+Utils.getMetriSimbol());
        txSide.setText("SURFACE SIDE LEN "+Utils.getMetriSimbol());
        if(d_side==0){
            d_side=20;
        }
        switch (counter) {
            case 0:
                nome.setText("POINT A");
                est.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[0])));
                nord.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[1])));
                quota.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[2])));
                side.setText(Utils.readUnitOfMeasureLITE(String.valueOf(d_side)));
                break;

            case 1:
                nome.setText("POINT B");
                est.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[0])));
                nord.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[1])));
                quota.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[2])));
                break;
            case 2:
                nome.setText("POINT C");
                est.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[0])));
                nord.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[1])));
                quota.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[2])));
                break;

            case 3:
                nome.setText("POINT D");
                est.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[0])));
                nord.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[1])));
                quota.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[2])));
                break;
            case 4:
                nome.setText("POINT E");
                est.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[0])));
                nord.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[1])));
                quota.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[2])));
                break;

            case 5:
                nome.setText("POINT F");
                est.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[0])));
                nord.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[1])));
                quota.setText(Utils.readUnitOfMeasureLITE(String.valueOf(coord[2])));
                break;
        }

    }
    public void update() {

    }

    public void onClick() {
        exit.setOnClickListener(view -> {
            dialog.dismiss();
        });
        est.setOnClickListener(view -> {

            if(uom==4 || uom==5) {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(side);
                }

            }else {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(side);
                }
            }
        });
        nord.setOnClickListener(view -> {
            if(uom==4 || uom==5) {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(side);
                }

            }else {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(side);
                }
            }
        });
        quota.setOnClickListener(view -> {
            if(uom==4 || uom==5) {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(side);
                }

            }else {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(side);
                }
            }
        });
        side.setOnClickListener(view -> {
            if(uom==4 || uom==5) {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(side);
                }

            }else {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(side);
                }
            }
        });
        save.setOnClickListener(view -> {
            switch (counter) {
                case 0:
                    DataSaved.puntiProgetto[0].x = Double.parseDouble(Utils.writeMetri(est.getText().toString()));
                    DataSaved.puntiProgetto[0].y = Double.parseDouble(Utils.writeMetri(nord.getText().toString()));
                    DataSaved.puntiProgetto[0].z = Double.parseDouble(Utils.writeMetri(quota.getText().toString()));
                    d_side=Double.parseDouble(Utils.writeMetri(side.getText().toString()));
                    dialog.dismiss();
                    break;
                case 1:

                    dialog.dismiss();
                    break;
                case 2:

                    dialog.dismiss();
                    break;
                case 3:

                    dialog.dismiss();
                    break;
                case 4:

                    dialog.dismiss();
                    break;
                case 5:

                    dialog.dismiss();
                    break;
            }
        });

    }
}
