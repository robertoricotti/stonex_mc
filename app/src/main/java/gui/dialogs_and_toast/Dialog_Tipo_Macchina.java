package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_Tipo_Macchina {

    Activity activity;
    public Dialog dialog;

    CheckBox ckExca, ckWheel, ckDozer, ckDozer6, ckGrader, ckDrill, ckPile;
    int mode, machinesel;
    ImageView macchina;


    public Dialog_Tipo_Macchina(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_tipo_macchina, null));

        builder.setCancelable(true);
        dialog = builder.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();

        if (MyData.get_String("BUILD").equals("SRT7PROS") || Build.BRAND.equals("qti")) {

            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        } else {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        update();
        onClick();


    }

    private void findView() {
        ckExca = dialog.findViewById(R.id.ckExca);
        ckDozer = dialog.findViewById(R.id.ckDozer);
        ckDozer6 = dialog.findViewById(R.id.ckDozer6);
        ckWheel = dialog.findViewById(R.id.ckWheel);
        ckGrader = dialog.findViewById(R.id.ckGrader);
        ckPile = dialog.findViewById(R.id.ckPileDriver);
        ckDrill = dialog.findViewById(R.id.ckDrilling);
        macchina = dialog.findViewById(R.id.macchina);


    }

    private void init() {
        machinesel = MyData.get_Int("MachineSelected");
        update();


    }

    private void update() {
        mode = MyData.get_Int("M" + machinesel + "_isWL");
        switch (mode) {
            case 0:
                ckExca.setChecked(true);
                ckWheel.setChecked(false);
                ckDozer.setChecked(false);
                ckDozer6.setChecked(false);
                ckGrader.setChecked(false);
                ckDrill.setChecked(false);
                ckPile.setChecked(false);
                macchina.setImageResource(R.drawable.escavatore_sfondo);
                macchina.setRotationY(0);
                break;
            case 1:
                ckExca.setChecked(false);
                ckWheel.setChecked(true);
                ckDozer.setChecked(false);
                ckDozer6.setChecked(false);
                ckGrader.setChecked(false);
                ckDrill.setChecked(false);
                ckPile.setChecked(false);
                macchina.setImageResource(R.drawable.wa_sfondo);
                macchina.setRotationY(180);
                break;
            case 2:

                ckExca.setChecked(false);
                ckWheel.setChecked(false);
                ckDozer.setChecked(true);
                ckDozer6.setChecked(false);
                ckGrader.setChecked(false);
                ckDrill.setChecked(false);
                ckPile.setChecked(false);
                macchina.setImageResource(R.drawable.bulldozer_sfondo);
                macchina.setRotationY(0);
                break;
            case 3:
                ckExca.setChecked(false);
                ckWheel.setChecked(false);
                ckDozer.setChecked(false);
                ckDozer6.setChecked(true);
                ckGrader.setChecked(false);
                ckDrill.setChecked(false);
                ckPile.setChecked(false);
                macchina.setImageResource(R.drawable.bulldozer_sfondo);
                macchina.setRotationY(0);
                break;


            case 4:
                ckExca.setChecked(false);
                ckWheel.setChecked(false);
                ckDozer.setChecked(false);
                ckDozer6.setChecked(false);
                ckGrader.setChecked(true);
                ckDrill.setChecked(false);
                ckPile.setChecked(false);
                macchina.setImageResource(R.drawable.cartoon_grader);
                macchina.setRotationY(180);
                break;
            case 5:
                ckExca.setChecked(false);
                ckWheel.setChecked(false);
                ckDozer.setChecked(false);
                ckDozer6.setChecked(false);
                ckGrader.setChecked(false);
                ckDrill.setChecked(true);
                ckPile.setChecked(false);
                macchina.setImageResource(R.drawable.drilling_black);

                break;
            case 6:
                ckExca.setChecked(false);
                ckWheel.setChecked(false);
                ckDozer.setChecked(false);
                ckDozer6.setChecked(false);
                ckGrader.setChecked(false);
                ckDrill.setChecked(false);
                ckPile.setChecked(true);
                macchina.setImageResource(R.drawable.piledriver_black);
                macchina.setRotationY(180);
                break;


        }

    }

    private void onClick() {
        ckExca.setOnClickListener(view -> {
            MyData.push("M" + machinesel + "_isWL", "0");
            DataSaved.isWL = MyData.get_Int("M" + machinesel + "_isWL");
            mode = DataSaved.isWL;
            update();
            dialog.dismiss();
        });
        ckWheel.setOnClickListener(view -> {
            MyData.push("M" + machinesel + "_isWL", "1");
            DataSaved.isWL = MyData.get_Int("M" + machinesel + "_isWL");
            mode = DataSaved.isWL;
            update();
            dialog.dismiss();

        });
        ckDozer.setOnClickListener(view -> {
            MyData.push("M" + machinesel + "_isWL", "2");
            DataSaved.isWL = MyData.get_Int("M" + machinesel + "_isWL");
            mode = DataSaved.isWL;
            update();
            dialog.dismiss();

        });
        ckDozer6.setOnClickListener(view -> {
            MyData.push("M" + machinesel + "_isWL", "3");
            DataSaved.isWL = MyData.get_Int("M" + machinesel + "_isWL");
            mode = DataSaved.isWL;
            update();
            dialog.dismiss();
        });
        ckGrader.setOnClickListener(view -> {
            MyData.push("M" + machinesel + "_isWL", "4");
            DataSaved.isWL = MyData.get_Int("M" + machinesel + "_isWL");
            mode = DataSaved.isWL;
            update();
            dialog.dismiss();
        });
        ckDrill.setOnClickListener(view -> {
            MyData.push("M" + machinesel + "_isWL", "5");
            DataSaved.isWL = MyData.get_Int("M" + machinesel + "_isWL");
            mode = DataSaved.isWL;
            update();
            dialog.dismiss();
        });
        ckPile.setOnClickListener(view -> {
            MyData.push("M" + machinesel + "_isWL", "6");
            DataSaved.isWL = MyData.get_Int("M" + machinesel + "_isWL");
            mode = DataSaved.isWL;
            update();
            dialog.dismiss();
        });


    }


}
