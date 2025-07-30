package gui.tech_menu;

import static gui.MyApp.folderPath;
import static gui.MyApp.isApollo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;
import java.io.File;

import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import services.UpdateValuesService;
import utils.MyData;

public class MachineInfoCalib extends AppCompatActivity {
    EditText machineName, techInfo;
    Button save, esc;
    int indexMachineSelected;
    CheckBox ckCan, ckOUT, isCanOpen, isTSM_Ang, isTsm, ckAutoStart,ckDEMO,ckDemoRoll,ckUseSwitch;
    CustomQwertyDialog customQwertyDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_info_calib);
        findView();
        init();
        onClick();
        onCheckedChange();
    }

    private void findView() {
        machineName = findViewById(R.id.nameExc);
        save = findViewById(R.id.save);
        esc = findViewById(R.id.exit);
        ckCan = findViewById(R.id.ckCanout);
        ckOUT = findViewById(R.id.ckOUTPUT);
        isCanOpen = findViewById(R.id.ckCanOpen);
        isTSM_Ang = findViewById(R.id.ckTSM_Ang);
        isTsm = findViewById(R.id.ckTSM);
        techInfo = findViewById(R.id.techInfo);
        ckAutoStart = findViewById(R.id.ckAutoStart);
        ckUseSwitch=findViewById(R.id.useSwitch);
        ckDEMO=findViewById(R.id.ckDEMO);
        ckDemoRoll=findViewById(R.id.ckDEMORoller);


    }

    private void init() {

        customQwertyDialog = new CustomQwertyDialog(this);

        indexMachineSelected = MyData.get_Int("MachineSelected");
        machineName.setText(MyData.get_String("M" + indexMachineSelected + "_Name"));
        techInfo.setText(MyData.get_String("techInfo"));
        ckCan.setChecked(MyData.get_Int("M" + indexMachineSelected + "_is40") != 0);
        ckOUT.setChecked(MyData.get_Int("M" + indexMachineSelected + "_enOUT") != 0);
        isCanOpen.setChecked(MyData.get_Int("M" + indexMachineSelected + "_useCanOpen") == 1);
        isTSM_Ang.setChecked(MyData.get_Int("M" + indexMachineSelected + "_useCanOpen") == 2);
        isTsm.setChecked(MyData.get_Int("M" + indexMachineSelected + "_useCanOpen") == 3);
        ckDEMO.setChecked(MyData.get_Int("M" + indexMachineSelected + "_useCanOpen") == 4);
        ckDemoRoll.setChecked(MyData.get_Int("M" + indexMachineSelected + "_useCanOpen") == 5);
        ckAutoStart.setChecked(MyData.get_Int("digStartUp") == 1);
        ckUseSwitch.setChecked(MyData.get_Int("M"+indexMachineSelected+"useQuickSwitch")==1);
        MyData.push("digStartUp", "0");
        if (isCanOpen.isChecked()) {
            isCanOpen.setEnabled(false);
        }
        if (isTSM_Ang.isChecked()) {
            isTSM_Ang.setEnabled(false);
        }
        if (isTsm.isChecked()) {
            isTsm.setEnabled(false);
        }
        if (ckDEMO.isChecked()) {
            ckDEMO.setEnabled(false);
        }
        if (ckDemoRoll.isChecked()) {
            ckDemoRoll.setEnabled(false);
        }

        ckCan.setEnabled(true);

    }

    private void onCheckedChange() {
        isCanOpen.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (isCanOpen.isChecked()) {
                ckDemoRoll.setEnabled(true);
                ckDemoRoll.setChecked(false);
                ckDEMO.setEnabled(true);
                ckDEMO.setChecked(false);
                isTSM_Ang.setEnabled(true);
                isTsm.setEnabled(true);
                isCanOpen.setEnabled(false);
                DataSaved.isCanOpen = 1;
                isTSM_Ang.setChecked(false);
                isTsm.setChecked(false);

            }
        });
        isTSM_Ang.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (isTSM_Ang.isChecked()) {
                ckDemoRoll.setEnabled(true);
                ckDemoRoll.setChecked(false);
                ckDEMO.setEnabled(true);
                ckDEMO.setChecked(false);
                isCanOpen.setEnabled(true);
                isTsm.setEnabled(true);
                isTSM_Ang.setEnabled(false);
                DataSaved.isCanOpen = 2;
                isCanOpen.setChecked(false);
                isTsm.setChecked(false);

            }
        });
        isTsm.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (isTsm.isChecked()) {
                ckDemoRoll.setEnabled(true);
                ckDemoRoll.setChecked(false);
                ckDEMO.setEnabled(true);
                ckDEMO.setChecked(false);
                isTSM_Ang.setEnabled(true);
                isTsm.setEnabled(false);
                isCanOpen.setEnabled(true);
                DataSaved.isCanOpen = 3;
                isTSM_Ang.setChecked(false);
                isCanOpen.setChecked(false);

            }

        });
        ckDEMO.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (ckDEMO.isChecked()) {
                ckDEMO.setEnabled(false);
                ckDEMO.setChecked(true);
                ckDemoRoll.setEnabled(true);
                ckDemoRoll.setChecked(false);
                isTSM_Ang.setEnabled(true);
                isTsm.setEnabled(true);
                isCanOpen.setEnabled(true);
                DataSaved.isCanOpen = 4;
                isTSM_Ang.setChecked(false);
                isCanOpen.setChecked(false);
                isTsm.setChecked(false);

            }

        });
        ckDemoRoll.setOnCheckedChangeListener((CompoundButton c, boolean b) -> {
            if (ckDemoRoll.isChecked()) {
                ckDemoRoll.setEnabled(false);
                ckDemoRoll.setChecked(true);
                ckDEMO.setEnabled(true);
                ckDEMO.setChecked(false);
                isTSM_Ang.setEnabled(true);
                isTsm.setEnabled(true);
                isCanOpen.setEnabled(true);
                DataSaved.isCanOpen = 5;
                isTSM_Ang.setChecked(false);
                isCanOpen.setChecked(false);
                isTsm.setChecked(false);

            }

        });
        ckAutoStart.setOnClickListener(view -> {
            ckAutoStart.setChecked(!ckAutoStart.isChecked());
            if (ckAutoStart.isChecked()) {
                ckAutoStart.setChecked(false);
                MyData.push("digStartUp", "0");

            }
           else if (!ckAutoStart.isChecked()) {
                ckAutoStart.setChecked(true);
                MyData.push("digStartUp", "1");

            }
        });
        ckUseSwitch.setOnClickListener(view -> {
            ckUseSwitch.setChecked(!ckUseSwitch.isChecked());
            if (ckUseSwitch.isChecked()) {
                ckUseSwitch.setChecked(false);
                MyData.push("M"+indexMachineSelected+"useQuickSwitch", "0");
            }
            else if (!ckUseSwitch.isChecked()) {
                ckUseSwitch.setChecked(true);
                MyData.push("M"+indexMachineSelected+"useQuickSwitch", "1");
            }
        });


    }

    private void save() {
        String nameM = MyData.get_String("M" + indexMachineSelected + "_Name");
        String path = Environment.getExternalStorageDirectory().toString() +folderPath+ "/Machines/Machine " + indexMachineSelected + "/Config";
        String fileName = nameM + ".csv";
        File f = new File(path, fileName);
        f.renameTo(new File(path, machineName.getText().toString().trim().toUpperCase() + ".csv"));

        MyData.push("M" + indexMachineSelected + "_Name", machineName.getText().toString().trim().toUpperCase());
        MyData.push("techInfo", techInfo.getText().toString().toUpperCase());
        if (ckCan.isChecked()) {
            MyData.push("M" + indexMachineSelected + "_is40", "1");
        } else {
            MyData.push("M" + indexMachineSelected + "_is40", "0");
        }
        if (ckOUT.isChecked()) {
            MyData.push("M" + indexMachineSelected + "_enOUT", "1");
        } else {
            MyData.push("M" + indexMachineSelected + "_enOUT", "0");
        }


        if (isCanOpen.isChecked()) {
            MyData.push("M" + indexMachineSelected + "_useCanOpen", "1");
        } else if (isTSM_Ang.isChecked()) {
            MyData.push("M" + indexMachineSelected + "_useCanOpen", "2");

        } else if (isTsm.isChecked()) {
            MyData.push("M" + indexMachineSelected + "_useCanOpen", "3");
        }else if (ckDEMO.isChecked()) {
            MyData.push("M" + indexMachineSelected + "_useCanOpen", "4");
        }
        else if (ckDemoRoll.isChecked()) {
            MyData.push("M" + indexMachineSelected + "_useCanOpen", "5");
        }else {
            MyData.push("M" + indexMachineSelected + "_useCanOpen", "0");
        }
    }

    private void onClick() {
        machineName.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(machineName);
            }
        });
        techInfo.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(techInfo);
            }
        });
        save.setOnClickListener((View v) -> {
            if (machineName.getText().toString().equals("")) {
                new CustomToast(this, "Missing name").show_alert();

            } else {
                esc.setEnabled(false);
                save.setEnabled(false);
                save();
                startService(new Intent(this, UpdateValuesService.class));
                startActivity(new Intent(this, Nuova_Machine_Settings.class));
                finish();
            }
        });

        esc.setOnClickListener((View v) -> {
            esc.setEnabled(false);
            save.setEnabled(false);
            startService(new Intent(getApplicationContext(), UpdateValuesService.class));
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}
