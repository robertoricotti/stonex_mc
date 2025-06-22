package gui.tech_menu;

import static gui.MyApp.KEY_LEVEL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.Nuova_Choose;
import gui.buckets.BucketChooserActivity;
import gui.debug_ecu.Can_Msg_Debug;
import gui.debug_ecu.DebugExcavatorActivity;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_user_settings.DialogUnitOfMeasure;
import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.DiggingProfile;
import gui.boot_and_choose.ExcavatorMenuActivity;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
import services.UpdateValuesService;
import utils.MyData;

public class ExcavatorChooserActivity extends BaseClass {
    DialogUnitOfMeasure dialogUnitOfMeasure;
    LinearLayout m1, m2, m3, m4;
    ImageView machine1, machine2, machine3, machine4;
    TextView nameMachine1, nameMachine2, nameMachine3, nameMachine4;
    ImageView canM1, canM2, canM3, canM4;
    ImageView bucketM1, bucketM2, bucketM3, bucketM4;
    ImageView settingsM1, settingsM2, settingsM3, settingsM4;
    ImageView back, toDig;
    TextView headerMachine;
    private boolean isDefault_1, isDefault_2, isDefault_3, isDefault_4;

    int indexMachineSelected, isWL1, isWL2, isWL3, isWL4,unitOfMeasure;

    DialogPassword dialogPassword;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_machines);


        findView();
        init();
        onClick();
        onLongClick();
        updateUI();
        dialogPassword = new DialogPassword(this);
        dialogUnitOfMeasure=new DialogUnitOfMeasure(this);
    }

    private void findView() {
        m1 = findViewById(R.id.m1);
        m2 = findViewById(R.id.m2);
        m3 = findViewById(R.id.m3);
        m4 = findViewById(R.id.m4);
        machine1 = findViewById(R.id.img_mac_1);
        machine2 = findViewById(R.id.img_mac_2);
        machine3 = findViewById(R.id.img_mac_3);
        machine4 = findViewById(R.id.img_mac_4);
        nameMachine1 = findViewById(R.id.name_mac_1);
        nameMachine2 = findViewById(R.id.name_mac_2);
        nameMachine3 = findViewById(R.id.name_mac_3);
        nameMachine4 = findViewById(R.id.name_mac_4);
        canM1 = findViewById(R.id.conM1);
        canM2 = findViewById(R.id.conM2);
        canM3 = findViewById(R.id.conM3);
        canM4 = findViewById(R.id.conM4);
        bucketM1 = findViewById(R.id.gotoBuck1);
        bucketM2 = findViewById(R.id.gotoBuck2);
        bucketM3 = findViewById(R.id.gotoBuck3);
        bucketM4 = findViewById(R.id.gotoBuck4);
        settingsM1 = findViewById(R.id.modify_mac_1);
        settingsM2 = findViewById(R.id.modify_mac_2);
        settingsM3 = findViewById(R.id.modify_mac_3);
        settingsM4 = findViewById(R.id.modify_mac_4);
        headerMachine = findViewById(R.id.machinesTitle);
        back = findViewById(R.id.back);
        toDig = findViewById(R.id.pair);
        progressBar = findViewById(R.id.progressBar);
    }

    private void init() {
        progressBar.setVisibility(View.INVISIBLE);
        indexMachineSelected = MyData.get_Int("MachineSelected");
        unitOfMeasure=MyData.get_Int("Unit_Of_Measure");
        headerMachine.setText(MyData.get_String("M" + indexMachineSelected + "_Name"));
        nameMachine1.setText(MyData.get_String("M1_Name"));
        nameMachine2.setText(MyData.get_String("M2_Name"));
        nameMachine3.setText(MyData.get_String("M3_Name"));
        nameMachine4.setText(MyData.get_String("M4_Name"));
        isDefault_1 = MyData.get_Double("M" + 1 + "_LengthBoom1") == -1d;
        isDefault_2 = MyData.get_Double("M" + 2 + "_LengthBoom1") == -1d;
        isDefault_3 = MyData.get_Double("M" + 3 + "_LengthBoom1") == -1d;
        isDefault_4 = MyData.get_Double("M" + 4 + "_LengthBoom1") == -1d;
        isWL1 = MyData.get_Int("M" + 1 + "_isWL");
        isWL2 =  MyData.get_Int("M" + 2 + "_isWL");
        isWL3 =  MyData.get_Int("M" + 3 + "_isWL");
        isWL4 =  MyData.get_Int("M" + 4 + "_isWL");
        switch (DataSaved.isWL){
            case 0:
                toDig.setImageResource(R.drawable.machines_btn);
                break;
            case 1:
                toDig.setImageResource(R.drawable.wheel_machines_btn);
                break;
            case 2:
                toDig.setImageResource(R.drawable.dozer_machines_btn);
                break;
            default:
                toDig.setImageResource(R.drawable.machines_btn);
                break;
        }
        if (DataSaved.isCanOpen == 1) {
            canM1.setImageResource(R.drawable.debug_btn);
            canM2.setImageResource(R.drawable.debug_btn);
            canM3.setImageResource(R.drawable.debug_btn);
            canM4.setImageResource(R.drawable.debug_btn);
        } else {
            canM1.setImageResource(R.drawable.debug_btn_tsm);
            canM2.setImageResource(R.drawable.debug_btn_tsm);
            canM3.setImageResource(R.drawable.debug_btn_tsm);
            canM4.setImageResource(R.drawable.debug_btn_tsm);
        }
    }

    public void updateUI() {

        headerMachine.setText( MyData.get_String("M" + indexMachineSelected + "_Name"));
        if (isWL1 == 1) {
            machine1.setImageResource(R.drawable.wheel_machines_btn);

        }else if(isWL1==2||isWL1==3){
            machine1.setImageResource(R.drawable.dozer_machines_btn);
            bucketM1.setVisibility(View.INVISIBLE);

        } else if (isWL1==4) {
            machine1.setImageResource(R.drawable.grader_btn);
            bucketM1.setVisibility(View.INVISIBLE);
        }
        if (isWL2 == 1) {
            machine2.setImageResource(R.drawable.wheel_machines_btn);
        }else if(isWL2==2){
            machine2.setImageResource(R.drawable.dozer_machines_btn);
            bucketM2.setVisibility(View.INVISIBLE);
        }else if (isWL2==4) {
            machine2.setImageResource(R.drawable.grader_btn);
            bucketM2.setVisibility(View.INVISIBLE);
        }
        if (isWL3 == 1) {
            machine3.setImageResource(R.drawable.wheel_machines_btn);
        }else if(isWL3==2){
            machine3.setImageResource(R.drawable.dozer_machines_btn);
            bucketM3.setVisibility(View.INVISIBLE);
        }else if (isWL3==4) {
            machine3.setImageResource(R.drawable.grader_btn);
            bucketM3.setVisibility(View.INVISIBLE);
        }
        if (isWL4 == 1) {
            machine4.setImageResource(R.drawable.wheel_machines_btn);
        }else if(isWL4==2){
            machine4.setImageResource(R.drawable.dozer_machines_btn);
            bucketM4.setVisibility(View.INVISIBLE);
        }else if (isWL4==4) {
            machine4.setImageResource(R.drawable.grader_btn);
            bucketM4.setVisibility(View.INVISIBLE);
        }
        m1.setBackgroundColor(indexMachineSelected == 1 ? getColor(R.color.orange) : getColor(R.color.transparent));
        if (isDefault_1) {
            m1.setAlpha(0.2f);
        } else {
            m1.setAlpha(1.0f);
        }
        m2.setBackgroundColor(indexMachineSelected == 2 ? getColor(R.color.orange) : getColor(R.color.transparent));
        if (isDefault_2) {
            m2.setAlpha(0.2f);
        } else {
            m2.setAlpha(1.0f);
        }
        m3.setBackgroundColor(indexMachineSelected == 3 ? getColor(R.color.orange) : getColor(R.color.transparent));
        if (isDefault_3) {
            m3.setAlpha(0.2f);
        } else {
            m3.setAlpha(1.0f);
        }
        m4.setBackgroundColor(indexMachineSelected == 4 ? getColor(R.color.orange) : getColor(R.color.transparent));
        if (isDefault_4) {
            m4.setAlpha(0.2f);
        } else {
            m4.setAlpha(1.0f);
        }
        if(DataSaved.isWL==2||DataSaved.isWL==3||DataSaved.isWL==4){
            toDig.setImageResource(R.drawable.go_grade);
        }else {
            toDig.setImageResource(R.drawable.go_dig);
        }


    }

    private void disableAll() {
        back.setEnabled(false);
        toDig.setEnabled(false);
        machine1.setEnabled(false);
        machine2.setEnabled(false);
        machine3.setEnabled(false);
        machine4.setEnabled(false);
        canM1.setEnabled(false);
        canM2.setEnabled(false);
        canM3.setEnabled(false);
        canM4.setEnabled(false);
        bucketM1.setEnabled(false);
        bucketM2.setEnabled(false);
        bucketM3.setEnabled(false);
        bucketM4.setEnabled(false);
        settingsM1.setEnabled(false);
        settingsM2.setEnabled(false);
        settingsM3.setEnabled(false);
        settingsM4.setEnabled(false);

    }
    private void enableAll() {
        back.setEnabled(true);
        toDig.setEnabled(true);
        machine1.setEnabled(true);
        machine2.setEnabled(true);
        machine3.setEnabled(true);
        machine4.setEnabled(true);
        canM1.setEnabled(true);
        canM2.setEnabled(true);
        canM3.setEnabled(true);
        canM4.setEnabled(true);
        bucketM1.setEnabled(true);
        bucketM2.setEnabled(true);
        bucketM3.setEnabled(true);
        bucketM4.setEnabled(true);
        settingsM1.setEnabled(true);
        settingsM2.setEnabled(true);
        settingsM3.setEnabled(true);
        settingsM4.setEnabled(true);
    }

    private void onClick() {
        back.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(getApplicationContext(), ExcavatorMenuActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        toDig.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            int profile =  MyData.get_Int("ProfileSelected");
            int typeView =  MyData.get_Int("indexView");
            if (profile == 0) {
                switch (typeView) {
                    case 0:
                        if(KEY_LEVEL>0) {
                            startActivity(new Intent(this, Digging1D.class));
                            overridePendingTransition(0, 0);
                            finish();
                        }else {
                            enableAll();
                            new CustomToast(this,"LICENSE MISSED").show_alert();
                        }
                        break;
                    case 1:
                        if(KEY_LEVEL>1) {
                            startActivity(new Intent(this, Digging2D.class));
                            overridePendingTransition(0, 0);
                            finish();
                        }else {
                            enableAll();
                            new CustomToast(this,"LICENSE MISSED").show_alert();
                        }
                        break;
                    case 2:
                    case 3:
                        if(KEY_LEVEL>2) {
                            progressBar.setVisibility(View.VISIBLE);
                            startService(new Intent(this, ReadProjectService.class));
                        }else {
                            enableAll();
                            new CustomToast(this,"LICENSE MISSED").show_alert();
                        }
                        break;

                }
            } else {
                startActivity(new Intent(this, DiggingProfile.class));
                overridePendingTransition(0, 0);
                finish();
            }

        });

        bucketM1.setOnClickListener((View v) -> {
            if (indexMachineSelected == 1) {
                bucketM1.setEnabled(false);
                startActivity(new Intent(this, BucketChooserActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        bucketM2.setOnClickListener((View v) -> {
            if (indexMachineSelected == 2) {
                bucketM2.setEnabled(false);
                startActivity(new Intent(this, BucketChooserActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        bucketM3.setOnClickListener((View v) -> {
            if (indexMachineSelected == 3) {
                bucketM3.setEnabled(false);
                startActivity(new Intent(this, BucketChooserActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        bucketM4.setOnClickListener((View v) -> {
            if (indexMachineSelected == 4) {
                bucketM4.setEnabled(false);
                startActivity(new Intent(this, BucketChooserActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        settingsM1.setOnClickListener((View v) -> {
            if(unitOfMeasure==4||unitOfMeasure==5) {
                if(!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                    dialogUnitOfMeasure.show();
                }

            }else {
                if (indexMachineSelected == 1 && !dialogPassword.dialog.isShowing()) {
                    dialogPassword.show();
                }
            }
        });

        settingsM2.setOnClickListener((View v) -> {
            if(unitOfMeasure==4||unitOfMeasure==5) {
                if(!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                    dialogUnitOfMeasure.show();
                }
            }else {
                if (indexMachineSelected == 2 && !dialogPassword.dialog.isShowing()) {
                    dialogPassword.show();
                }
            }
        });

        settingsM3.setOnClickListener((View v) -> {
            if(unitOfMeasure==4||unitOfMeasure==5) {
                if(!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                    dialogUnitOfMeasure.show();
                }
            }else {
                if (indexMachineSelected == 3 && !dialogPassword.dialog.isShowing()) {
                    dialogPassword.show();
                }
            }
        });

        settingsM4.setOnClickListener((View v) -> {
            if(unitOfMeasure==4||unitOfMeasure==5) {
                if(!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                    dialogUnitOfMeasure.show();
                }
            }else {
                if (indexMachineSelected == 4 && !dialogPassword.dialog.isShowing()) {
                    dialogPassword.show();
                }
            }
        });
        canM1.setOnClickListener(view -> {
            if (indexMachineSelected == 1) {
                canM1.setEnabled(false);
                if(DataSaved.isWL<2) {
                    startActivity(new Intent(ExcavatorChooserActivity.this, DebugExcavatorActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                }else {
                    Intent intent=new Intent(ExcavatorChooserActivity.this, Can_Msg_Debug.class);
                    intent.putExtra("chi","choose");
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            }
        });
        canM2.setOnClickListener(view -> {
            if (indexMachineSelected == 2) {
                canM2.setEnabled(false);
                if(DataSaved.isWL<2) {
                    startActivity(new Intent(ExcavatorChooserActivity.this, DebugExcavatorActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                }else {

                    Intent intent=new Intent(ExcavatorChooserActivity.this, Can_Msg_Debug.class);
                    intent.putExtra("chi","choose");
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            }

        });
        canM3.setOnClickListener(view -> {
            if (indexMachineSelected == 3) {
                canM3.setEnabled(false);
                if(DataSaved.isWL<2) {
                    startActivity(new Intent(ExcavatorChooserActivity.this, DebugExcavatorActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                }else {

                    Intent intent=new Intent(ExcavatorChooserActivity.this, Can_Msg_Debug.class);
                    intent.putExtra("chi","choose");
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            }

        });
        canM4.setOnClickListener(view -> {
            if (indexMachineSelected == 4) {
                canM4.setEnabled(false);
                if(DataSaved.isWL<2) {
                    startActivity(new Intent(ExcavatorChooserActivity.this, DebugExcavatorActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                }else {

                    Intent intent=new Intent(ExcavatorChooserActivity.this, Can_Msg_Debug.class);
                    intent.putExtra("chi","choose");
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            }

        });
    }



    private void onLongClick() {
        machine1.setOnClickListener((View v) -> {
            MyData.push("MachineSelected", "1");
            indexMachineSelected = 1;
            startService(new Intent(this, UpdateValuesService.class));

        });

        machine2.setOnClickListener((View v) -> {
            MyData.push("MachineSelected", "2");
            indexMachineSelected = 2;
            startService(new Intent(this, UpdateValuesService.class));

        });

        machine3.setOnClickListener((View v) -> {
            MyData.push("MachineSelected", "3");
            indexMachineSelected = 3;
            startService(new Intent(this, UpdateValuesService.class));

        });

        machine4.setOnClickListener((View v) -> {
            MyData.push("MachineSelected", "4");
            indexMachineSelected = 4;
            startService(new Intent(this, UpdateValuesService.class));
            if(DataSaved.isWL==2||DataSaved.isWL==3||DataSaved.isWL==4){
                toDig.setImageResource(R.drawable.go_grade);
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

}
