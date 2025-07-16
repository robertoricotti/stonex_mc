package gui.dialogs_user_settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.stx_dig.R;

import gui.buckets.BucketChooserActivity;
import gui.digging_excavator.Digging3D_DXF;
import gui.projects.Activity_Crea_Superficie;
import gui.tech_menu.ExcavatorChooserActivity;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class DialogUnitOfMeasure {
    Activity activity;
    public Dialog alertDialog;
    CheckBox cbxm, cbxft, cbxinch,cbxINft;
    CheckBox cbxdegree, cbxpercent;
    Button save, exit;
    int index = 0;


    public DialogUnitOfMeasure(Activity activity) {
        this.activity = activity;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_unit_of_measure, null));

        builder.setCancelable(false);
        alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        alertDialog.show();
        if (MyData.get_String("BUILD").equals("APOLLO2_7")||Build.BRAND.equals("APOLLO2_12_PRO")||Build.BRAND.equals("APOLLO2_12_PLUS")) {

            alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }else {
            alertDialog.getWindow().setLayout(900, 400);
        }
        FullscreenActivity.setFullScreen(alertDialog);
        findView();
        init();
        onClick();
        onCheckedChange();
    }

    private void findView() {
        cbxm = alertDialog.findViewById(R.id.cbxm);
        cbxft = alertDialog.findViewById(R.id.cbxft);
        cbxINft=alertDialog.findViewById(R.id.cbxINft);
        cbxinch = alertDialog.findViewById(R.id.cbxinch);
        cbxdegree = alertDialog.findViewById(R.id.cbxdegree);
        cbxpercent = alertDialog.findViewById(R.id.cbxpercent);
        save = alertDialog.findViewById(R.id.save);
        exit = alertDialog.findViewById(R.id.exit);
    }

    private void init() {



        index = MyData.get_Int("Unit_Of_Measure");

        switch (index) {
            case 0:
                cbxm.setChecked(true);
                cbxdegree.setChecked(true);
                break;
            case 1:
                cbxm.setChecked(true);
                cbxpercent.setChecked(true);
                break;
            case 2:
                cbxft.setChecked(true);
                cbxdegree.setChecked(true);
                break;
            case 3:
                cbxft.setChecked(true);
                cbxpercent.setChecked(true);
                break;
            case 4:
                cbxinch.setChecked(true);
                cbxdegree.setChecked(true);
                break;
            case 5:
                cbxinch.setChecked(true);
                cbxpercent.setChecked(true);
                break;
            case 6:
                cbxINft.setChecked(true);
                cbxdegree.setChecked(true);
                break;
            case 7:
                cbxINft.setChecked(true);
                cbxpercent.setChecked(true);
                break;
        }
    }

    private void onClick() {
        exit.setOnClickListener((View v) -> {
            alertDialog.dismiss();
        });

        save.setOnClickListener((View v) -> {
            if (cbxm.isChecked() && cbxdegree.isChecked()) {
                index = 0;
            }
            if (cbxm.isChecked() && cbxpercent.isChecked()) {
                index = 1;
            }
            if (cbxft.isChecked() && cbxdegree.isChecked()) {
                index = 2;
            }
            if (cbxft.isChecked() && cbxpercent.isChecked()) {
                index = 3;
            }
            if (cbxinch.isChecked() && cbxdegree.isChecked()) {
                index = 4;
            }
            if (cbxinch.isChecked() && cbxpercent.isChecked()) {
                index = 5;
            }
            if (cbxINft.isChecked() && cbxdegree.isChecked()) {
                index = 6;
            }
            if (cbxINft.isChecked() && cbxpercent.isChecked()) {
                index = 7;
            }
            MyData.push("Unit_Of_Measure", String.valueOf(index));


            if (activity instanceof ExcavatorChooserActivity) {
                activity.recreate();
            }
            if (activity instanceof BucketChooserActivity) {
                activity.recreate();
            }
            if (activity instanceof Digging3D_DXF) {
                activity.recreate();
            }
            if (activity instanceof Activity_Crea_Superficie) {
                activity.recreate();
            }
            DataSaved.lastProjectName="";
            DataSaved.lastProjectNamePOLY = "";
            DataSaved.lastProjectNamePOINT = "";
            alertDialog.dismiss();
        });
    }

    private void onCheckedChange() {
        cbxm.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxm.isChecked()) {
                cbxft.setChecked(false);
                cbxinch.setChecked(false);
                cbxINft.setChecked(false);
            }
        });

        cbxft.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxft.isChecked()) {
                cbxm.setChecked(false);
                cbxinch.setChecked(false);
                cbxINft.setChecked(false);
            }
        });

        cbxinch.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxinch.isChecked()) {
                cbxm.setChecked(false);
                cbxft.setChecked(false);
                cbxINft.setChecked(false);
            }
        });
        cbxINft.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxINft.isChecked()) {
                cbxm.setChecked(false);
                cbxft.setChecked(false);
                cbxinch.setChecked(false);
            }
        });

        cbxdegree.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxdegree.isChecked()) {
                cbxpercent.setChecked(false);
            }
        });

        cbxpercent.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (cbxpercent.isChecked()) {
                cbxdegree.setChecked(false);
            }
        });
    }
}
