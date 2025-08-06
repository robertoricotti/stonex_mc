package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Blade_Wear {
    int indexMeasure, indexMachineSelected;
    Activity activity;
    public Dialog dialog;
    ImageView save, cancel;
    EditText editText;
    TextView txtCutEdge;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;

    public Dialog_Blade_Wear(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_blade_wear);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 🔹 Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.75);
        int height = (int) (displayMetrics.heightPixels * 0.75);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
    }

    private void findView() {
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        customNumberDialog = new CustomNumberDialog(activity, -2);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -2);
        save = dialog.findViewById(R.id.save);
        cancel = dialog.findViewById(R.id.exit);
        editText = dialog.findViewById(R.id.etValor);
        txtCutEdge = dialog.findViewById(R.id.txtCutEdge);
        txtCutEdge.setText("Cutting Edge " + Utils.getMetriSimbol());
        editText.setText(Utils.readSensorCalibration(String.valueOf(DataSaved.usuraLamaCX)));
    }

    private void onClick() {
        editText.setOnClickListener(view -> {
            if(indexMeasure==4||indexMeasure==5){
                if(!customNumberDialogFtIn.dialog.isShowing()){
                    customNumberDialogFtIn.show(editText);
                }
            }else {
                if (!customNumberDialog.dialog.isShowing()){
                    customNumberDialog.show(editText);}
            }
        });
        save.setOnClickListener(view -> {
            DataSaved.usuraLamaCX = Double.parseDouble(Utils.writeMetri(editText.getText().toString()));
            MyData.push("M" + indexMachineSelected + "usuraLamaCX", Utils.writeMetri(editText.getText().toString()));
            new CustomToast(activity, activity.getString(R.string.save)).show();
            dialog.dismiss();
        });

        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }
}
