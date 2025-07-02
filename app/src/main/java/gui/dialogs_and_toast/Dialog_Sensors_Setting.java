package gui.dialogs_and_toast;

import static gui.MyApp.isApollo;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import com.example.stx_dig.R;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;


public class Dialog_Sensors_Setting {

    Activity activity;
    public Dialog dialog;
    ImageView imgLeft, imgRight;
    Button save, exit;
    int indexMach;


    public Dialog_Sensors_Setting(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {

        dialog.create();
        dialog.setContentView(R.layout.dialog_sensors_config);

        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
    }

    public void findView() {
        imgLeft = dialog.findViewById(R.id.leftSensor);
        imgRight = dialog.findViewById(R.id.rightSensor);
        save = dialog.findViewById(R.id.save);
        exit = dialog.findViewById(R.id.clos);
    }

    private void update() {
        switch (DataSaved.leftSensorType) {
            case 0:
                imgLeft.setImageResource(R.drawable.divieto_96);
                imgLeft.setRotation(0f);
                break;
            case 1:
                imgLeft.setImageResource(R.drawable.bucket);
                imgLeft.setRotation(0f);
                break;
            case 2:
                imgLeft.setImageResource(R.drawable.moba_sensore);
                imgLeft.setRotation(45f);
                break;
        }
        switch (DataSaved.rightSensorType) {
            case 0:
                imgRight.setImageResource(R.drawable.divieto_96);
                imgRight.setRotation(0f);
                break;
            case 1:
                imgRight.setImageResource(R.drawable.bucket);
                imgRight.setRotation(0f);
                break;
            case 2:
                imgRight.setImageResource(R.drawable.moba_sensore);
                imgRight.setRotation(45f);
                break;
        }
    }

    public void init() {
        indexMach= MyData.get_Int("MachineSelected");
        update();

    }

    public void onClick() {
        imgLeft.setOnClickListener(view -> {
            DataSaved.leftSensorType += 1;
            DataSaved.leftSensorType = DataSaved.leftSensorType % 3;
            update();

        });
        imgRight.setOnClickListener(view -> {
            DataSaved.rightSensorType += 1;
            DataSaved.rightSensorType = DataSaved.rightSensorType % 3;
            update();

        });
        exit.setOnClickListener(view -> {
            DataSaved.leftSensorType = MyData.get_Int("M" + indexMach + "leftSensor");
            DataSaved.rightSensorType = MyData.get_Int("M" + indexMach + "rightSensor");
            dialog.dismiss();
        });
        save.setOnClickListener(view -> {
            MyData.push("M" + indexMach + "leftSensor", String.valueOf(DataSaved.leftSensorType));
            MyData.push("M" + indexMach + "rightSensor", String.valueOf(DataSaved.rightSensorType));
            dialog.dismiss();
            activity.recreate();

        });


    }
}
