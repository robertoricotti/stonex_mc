package gui.tech_menu;

import static gui.MyApp.errorCode;
import static gui.boot_and_choose.LaunchScreenActivity.hasAuto;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.SOLARDRILL;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.buckets.BucketChooserActivity;
import gui.debug_ecu.Can_Msg_Debug;
import gui.hydro.Hydro_Activity_Entering;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.DialogPassword;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.Dialog_InfoApp;
import gui.dialogs_user_settings.DialogUnitOfMeasure;
import gui.gps.Nuovo_Gps;
import packexcalib.exca.DataSaved;
import services.UpdateValuesService;
import utils.MyData;
import utils.WifiHelper;

public class ExcavatorChooserActivity extends BaseClass {
    DialogUnitOfMeasure dialogUnitOfMeasure;
    LinearLayout m1, m2, m3, m4;
    ImageView machine1, machine2, machine3, machine4, lockUnlock, savetofile, readfromfile;
    TextView nameMachine1, nameMachine2, nameMachine3, nameMachine4;
    ImageView canM1, canM2, canM3, canM4;
    ImageView bucketM1, bucketM2, bucketM3, bucketM4;
    ImageView settingsM1, settingsM2, settingsM3, settingsM4;
    ImageView back;
    ImageView img00, img01, btn_3;
    TextView txtProject;

    private boolean isDefault_1, isDefault_2, isDefault_3, isDefault_4;

    int indexMachineSelected, isWL1, isWL2, isWL3, isWL4, unitOfMeasure;

    DialogPassword dialogPassword;
    ProgressBar progressBar;
    Dialog_InfoApp dialogInfoApp;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;

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
        dialogUnitOfMeasure = new DialogUnitOfMeasure(this);
        dialogInfoApp = new Dialog_InfoApp(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);

    }

    private void findView() {
        btn_3 = findViewById(R.id.btn_3);
        img00 = findViewById(R.id.img00);
        img01 = findViewById(R.id.img01);
        txtProject = findViewById(R.id.txtProject);
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
        lockUnlock = findViewById(R.id.lockunlock);
        savetofile = findViewById(R.id.savetofile);
        readfromfile = findViewById(R.id.readfromfile);

        back = findViewById(R.id.back);

        progressBar = findViewById(R.id.progressBar);
    }

    private void init() {
        progressBar.setVisibility(View.INVISIBLE);
        indexMachineSelected = MyData.get_Int("MachineSelected");
        unitOfMeasure = MyData.get_Int("Unit_Of_Measure");
        nameMachine1.setText(MyData.get_String("M1_Name"));
        nameMachine2.setText(MyData.get_String("M2_Name"));
        nameMachine3.setText(MyData.get_String("M3_Name"));
        nameMachine4.setText(MyData.get_String("M4_Name"));
        isDefault_1 = MyData.get_Double("M" + 1 + "_LengthBoom1") == -1d;
        isDefault_2 = MyData.get_Double("M" + 2 + "_LengthBoom1") == -1d;
        isDefault_3 = MyData.get_Double("M" + 3 + "_LengthBoom1") == -1d;
        isDefault_4 = MyData.get_Double("M" + 4 + "_LengthBoom1") == -1d;
        isWL1 = MyData.get_Int("M" + 1 + "_isWL");
        isWL2 = MyData.get_Int("M" + 2 + "_isWL");
        isWL3 = MyData.get_Int("M" + 3 + "_isWL");
        isWL4 = MyData.get_Int("M" + 4 + "_isWL");


    }

    public void updateUI() {
        try {
            if (DataSaved.gpsOk && errorCode == 0) {
                img00.setImageTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                img00.setImageTintList(ColorStateList.valueOf(Color.RED));
            }
            String ssid = WifiHelper.getConnectedSSID(getApplicationContext());
            if (ssid != null) {

                img01.setImageResource(R.drawable.baseline_signal_wifi_statusbar_4_bar_96);

            } else {

                img01.setImageResource(R.drawable.wifi_vuoto);

            }
        } catch (Exception e) {
            img01.setImageResource(R.drawable.wifi_off_96);
        }
        if (isTech) {
            lockUnlock.setImageResource(R.drawable.unlock);

        } else {
            lockUnlock.setImageResource(R.drawable.lock);
        }


        if (isWL1 == WHEELLOADER) {
            machine1.setImageResource(R.drawable.wheel_machines_btn);

        } else if (isWL1 == DOZER || isWL1 == DOZER_SIX) {
            machine1.setImageResource(R.drawable.dozer_machines_btn);
            bucketM1.setImageResource(R.drawable.ecu_96);

        } else if (isWL1 == GRADER) {
            machine1.setImageResource(R.drawable.grader_btn);
            bucketM1.setImageResource(R.drawable.ecu_96);
        }else if(isWL1==DRILL){
            machine1.setImageResource(R.drawable.drill_btn);
            bucketM1.setImageResource(R.drawable.ecu_96);
        }else if(isWL1==SOLARDRILL){
            machine1.setImageResource(R.drawable.drill_btn);
            bucketM1.setImageResource(R.drawable.ecu_96);
        }
        if (isWL2 == WHEELLOADER) {
            machine2.setImageResource(R.drawable.wheel_machines_btn);
        } else if (isWL2 == DOZER||isWL2==DOZER_SIX) {
            machine2.setImageResource(R.drawable.dozer_machines_btn);
            bucketM2.setImageResource(R.drawable.ecu_96);
        } else if (isWL2 == GRADER) {
            machine2.setImageResource(R.drawable.grader_btn);
            bucketM2.setImageResource(R.drawable.ecu_96);
        }else if(isWL2==DRILL){
            machine2.setImageResource(R.drawable.drill_btn);
            bucketM2.setImageResource(R.drawable.ecu_96);
        }else if(isWL2==SOLARDRILL){
            machine2.setImageResource(R.drawable.drill_btn);
            bucketM2.setImageResource(R.drawable.ecu_96);
        }
        if (isWL3 == WHEELLOADER) {
            machine3.setImageResource(R.drawable.wheel_machines_btn);
        } else if (isWL3 == DOZER||isWL3==DOZER_SIX) {
            machine3.setImageResource(R.drawable.dozer_machines_btn);
            bucketM3.setImageResource(R.drawable.ecu_96);
        } else if (isWL3 == GRADER) {
            machine3.setImageResource(R.drawable.grader_btn);
            bucketM3.setImageResource(R.drawable.ecu_96);
        }else if(isWL3==DRILL){
            machine3.setImageResource(R.drawable.drill_btn);
            bucketM3.setImageResource(R.drawable.ecu_96);
        }else if(isWL3==SOLARDRILL){
            machine3.setImageResource(R.drawable.drill_btn);
            bucketM3.setImageResource(R.drawable.ecu_96);
        }
        if (isWL4 == WHEELLOADER) {
            machine4.setImageResource(R.drawable.wheel_machines_btn);
        } else if (isWL4 == DOZER||isWL4==DOZER_SIX) {
            machine4.setImageResource(R.drawable.dozer_machines_btn);
            bucketM4.setImageResource(R.drawable.ecu_96);
        } else if (isWL4 == GRADER) {
            machine4.setImageResource(R.drawable.grader_btn);
            bucketM4.setImageResource(R.drawable.ecu_96);
        }else if(isWL4==DRILL){
            machine4.setImageResource(R.drawable.drill_btn);
            bucketM4.setImageResource(R.drawable.ecu_96);
        }else if(isWL4==SOLARDRILL){
            machine4.setImageResource(R.drawable.drill_btn);
            bucketM4.setImageResource(R.drawable.ecu_96);
        }
        m1.setBackground(indexMachineSelected == 1 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_1) {
            m1.setAlpha(0.2f);
        } else {
            m1.setAlpha(1.0f);
        }
        m2.setBackground(indexMachineSelected == 2 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_2) {
            m2.setAlpha(0.2f);
        } else {
            m2.setAlpha(1.0f);
        }
        m3.setBackground(indexMachineSelected == 3 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_3) {
            m3.setAlpha(0.2f);
        } else {
            m3.setAlpha(1.0f);
        }
        m4.setBackground(indexMachineSelected == 4 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_4) {
            m4.setAlpha(0.2f);
        } else {
            m4.setAlpha(1.0f);
        }


    }

    private void disableAll() {
        btn_3.setEnabled(false);
        lockUnlock.setEnabled(false);
        back.setEnabled(false);
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
        savetofile.setEnabled(false);
        readfromfile.setEnabled(false);

    }

    private void enableAll() {
        lockUnlock.setEnabled(true);
        back.setEnabled(true);
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
        savetofile.setOnClickListener(view -> {
            if (isTech) {
                // Crea un nuovo AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(ExcavatorChooserActivity.this);
                builder.setTitle(getResources().getString(R.string.savepara));
                builder.setMessage(getResources().getString(R.string.savepara_pro));

                // Aggiungi il pulsante "Sì"
                builder.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {

                    MyData.exportAllToJson();
                    new CustomToast(ExcavatorChooserActivity.this, "SAVED").show_long();
                });

                // Aggiungi il pulsante "No"
                builder.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {

                });
                builder.setCancelable(true);
                builder.show();



            } else {
                String lucchettoChiuso = "\uD83D\uDD12";
                new CustomToast(ExcavatorChooserActivity.this, lucchettoChiuso).show();
            }
        });
        readfromfile.setOnClickListener(view -> {
            if (isTech) {
                // Crea un nuovo AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(ExcavatorChooserActivity.this);
                builder.setTitle(getResources().getString(R.string.write_para));
                builder.setMessage(getResources().getString(R.string.savepara_pro));

                // Aggiungi il pulsante "Sì"
                builder.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {

                    MyData.restoreFromJson();

                });

                // Aggiungi il pulsante "No"
                builder.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {

                });
                builder.setCancelable(true);
                builder.show();



            } else {
                String lucchettoChiuso = "\uD83D\uDD12";
                new CustomToast(ExcavatorChooserActivity.this, lucchettoChiuso).show();
            }
        });
        img00.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });
        btn_3.setOnClickListener(view -> {
            disableAll();
            Intent intent = new Intent(this, Can_Msg_Debug.class);
            intent.putExtra("chi", "menu");
            startActivity(intent);
            finish();
        });
        lockUnlock.setOnClickListener(view -> {
            if (!isTech) {
                if (!dialogPassword.dialog.isShowing()) {
                    dialogPassword.show(-1);
                }
            }

        });
        back.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(getApplicationContext(), Activity_Home_Page.class));
            finish();
        });


        bucketM1.setOnClickListener((View v) -> {
            if (indexMachineSelected == 1) {
                if (isWL1 == EXCAVATOR|| isWL1 == WHEELLOADER) {
                    bucketM1.setEnabled(false);
                    Intent i = new Intent(this, BucketChooserActivity.class);
                    i.putExtra("whoDig", String.valueOf(MyApp.visibleActivity));
                    startActivity(i);
                    finish();
                } else {

                    if (hasAuto) {
                        if(isWL1==DOZER||isWL1==DOZER_SIX||isWL1==GRADER) {
                            if (isTech) {
                                disableAll();
                                startActivity(new Intent(this, Hydro_Activity_Entering.class));
                                finish();
                            } else {
                                if (!dialogPassword.dialog.isShowing()) {
                                    dialogPassword.show(3);
                                }
                            }
                        } else if (isWL1==DRILL||isWL1==SOLARDRILL) {
                            new CustomToast(this, "No AUTO Implemented").show_alert();

                        }
                    } else {
                        new CustomToast(this, "No AUTO License Activated!\nContact a Stonex Dealer").show_alert();
                    }
                }
            }
        });

        bucketM2.setOnClickListener((View v) -> {
            if (indexMachineSelected == 2) {
                if (isWL2 == EXCAVATOR || isWL2 == WHEELLOADER) {
                    bucketM2.setEnabled(false);
                    Intent i = new Intent(this, BucketChooserActivity.class);
                    i.putExtra("whoDig", String.valueOf(MyApp.visibleActivity));
                    startActivity(i);
                    finish();
                } else {

                    if (hasAuto) {
                        if(isWL2==DOZER||isWL2==DOZER_SIX||isWL2==GRADER) {
                            if (isTech) {
                                disableAll();
                                startActivity(new Intent(this, Hydro_Activity_Entering.class));
                                finish();
                            } else {
                                if (!dialogPassword.dialog.isShowing()) {
                                    dialogPassword.show(3);
                                }
                            }
                        } else if (isWL2==DRILL||isWL2==SOLARDRILL) {
                            new CustomToast(this, "No AUTO Implemented").show_alert();

                        }
                    } else {
                        new CustomToast(this, "No AUTO License Activated!\nContact a Stonex Dealer").show_alert();
                    }
                }
            }
        });

        bucketM3.setOnClickListener((View v) -> {
            if (indexMachineSelected == 3) {
                if (isWL3 == EXCAVATOR|| isWL3 == WHEELLOADER) {
                    bucketM3.setEnabled(false);
                    Intent i = new Intent(this, BucketChooserActivity.class);
                    i.putExtra("whoDig", String.valueOf(MyApp.visibleActivity));
                    startActivity(i);
                    finish();
                } else {

                    if (hasAuto) {
                        if(isWL3==DOZER||isWL3==DOZER_SIX||isWL3==GRADER) {
                            if (isTech) {
                                disableAll();
                                startActivity(new Intent(this, Hydro_Activity_Entering.class));
                                finish();
                            } else {
                                if (!dialogPassword.dialog.isShowing()) {
                                    dialogPassword.show(3);
                                }
                            }
                        } else if (isWL3==DRILL||isWL3==SOLARDRILL) {
                            new CustomToast(this, "No AUTO Implemented").show_alert();

                        }
                    } else {
                        new CustomToast(this, "No AUTO License Activated!\nContact a Stonex Dealer").show_alert();
                    }
                }
            }
        });

        bucketM4.setOnClickListener((View v) -> {
            if (indexMachineSelected == 4) {
                if (isWL4 == EXCAVATOR || isWL4 == WHEELLOADER) {
                    bucketM4.setEnabled(false);
                    Intent i = new Intent(this, BucketChooserActivity.class);
                    i.putExtra("whoDig", String.valueOf(MyApp.visibleActivity));
                    startActivity(i);
                    finish();
                } else {

                    if (hasAuto) {
                        if(isWL4==DOZER||isWL4==DOZER_SIX||isWL4==GRADER) {
                            if (isTech) {
                                disableAll();
                                startActivity(new Intent(this, Hydro_Activity_Entering.class));
                                finish();
                            } else {
                                if (!dialogPassword.dialog.isShowing()) {
                                    dialogPassword.show(3);
                                }
                            }
                        } else if (isWL4==DRILL||isWL4==SOLARDRILL) {
                            new CustomToast(this, "No AUTO Implemented").show_alert();

                        }
                    } else {
                        new CustomToast(this, "No AUTO License Activated!\nContact a Stonex Dealer").show_alert();
                    }
                }
            }
        });

        settingsM1.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexMachineSelected == 1) {
                    if (isTech) {
                        if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                            if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                                new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                                dialogUnitOfMeasure.show();
                            }
                        } else {
                            disableAll();
                            startActivity(new Intent(this, Nuova_Machine_Settings.class));
                            finish();
                        }
                    } else {

                        if (!dialogPassword.dialog.isShowing()) {
                            dialogPassword.show(2);
                        }

                    }
                }
            }


        });

        settingsM2.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexMachineSelected == 2) {
                    if (isTech) {
                        if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                            if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                                new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                                dialogUnitOfMeasure.show();
                            }
                        } else {
                            disableAll();
                            startActivity(new Intent(this, Nuova_Machine_Settings.class));
                            finish();
                        }
                    } else {
                        if (!dialogPassword.dialog.isShowing()) {
                            dialogPassword.show(2);
                        }
                    }
                }
            }


        });

        settingsM3.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexMachineSelected == 3) {
                    if (isTech) {
                        if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                            if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                                new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                                dialogUnitOfMeasure.show();
                            }
                        } else {
                            disableAll();
                            startActivity(new Intent(this, Nuova_Machine_Settings.class));
                            finish();
                        }
                    } else {
                        if (!dialogPassword.dialog.isShowing()) {
                            dialogPassword.show(2);
                        }
                    }
                }
            }


        });

        settingsM4.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexMachineSelected == 4) {
                    if (isTech) {
                        if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                            if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                                new CustomToast(this, "Select Feet or Meter to access calibration").show_long();
                                dialogUnitOfMeasure.show();
                            }
                        } else {
                            disableAll();
                            startActivity(new Intent(this, Nuova_Machine_Settings.class));
                            finish();
                        }
                    } else {
                        if (!dialogPassword.dialog.isShowing()) {
                            dialogPassword.show(2);
                        }
                    }
                }
            }


        });
        canM1.setOnClickListener(view -> {
            if (indexMachineSelected == 1) {
                if (isTech) {
                    disableAll();
                    startActivity(new Intent(ExcavatorChooserActivity.this, Nuovo_Gps.class));
                    finish();
                } else {
                    if (!dialogPassword.dialog.isShowing()) {
                        dialogPassword.show(1);
                    }
                }
            }
        });
        canM2.setOnClickListener(view -> {

            if (indexMachineSelected == 2) {
                if (isTech) {
                    disableAll();
                    startActivity(new Intent(ExcavatorChooserActivity.this, Nuovo_Gps.class));
                    finish();
                } else {
                    if (!dialogPassword.dialog.isShowing()) {
                        dialogPassword.show(1);
                    }
                }
            }


        });
        canM3.setOnClickListener(view -> {
            if (indexMachineSelected == 3) {
                if (isTech) {
                    disableAll();
                    startActivity(new Intent(ExcavatorChooserActivity.this, Nuovo_Gps.class));
                    finish();
                } else {
                    if (!dialogPassword.dialog.isShowing()) {
                        dialogPassword.show(1);
                    }
                }
            }

        });
        canM4.setOnClickListener(view -> {
            if (indexMachineSelected == 4) {
                if (isTech) {
                    disableAll();
                    startActivity(new Intent(ExcavatorChooserActivity.this, Nuovo_Gps.class));
                    finish();
                } else {
                    if (!dialogPassword.dialog.isShowing()) {
                        dialogPassword.show(1);
                    }
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
