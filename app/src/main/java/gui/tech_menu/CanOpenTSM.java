package gui.tech_menu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Sensors_Decoder;
import services.CanService;
import services.UpdateValuesService;
import utils.MyDeviceManager;


public class CanOpenTSM extends AppCompatActivity {
    int progresso = 0;
    TextView txtProgress;

    CheckBox cbFR, cbB1, cbB2, cbST, cbBK, cbTL, cbDEF;
    Button txtSource, txtDest;
    ProgressBar progressBar;
    Button gotoCanOpen, backToNova, restoreDef, storeStx, saveall, changeId;
    Button exit;
    int sourceID = 0x603, destID = 0x03;
    String sA = "From ID", dA = "To ID";
    static boolean isSend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can_open_tsm);


        findView();
        onClick();
        updateUI();

    }

    private void findView() {


        exit = findViewById(R.id.exitS);
        cbFR = findViewById(R.id.cbFR);
        cbB1 = findViewById(R.id.cbB1);
        cbB2 = findViewById(R.id.cbB2);
        cbST = findViewById(R.id.cbST);
        cbBK = findViewById(R.id.cbBK);
        cbTL = findViewById(R.id.cbTL);
        cbDEF = findViewById(R.id.cbDef);
        gotoCanOpen = findViewById(R.id.goToCanOpen);
        backToNova = findViewById(R.id.backNova);
        restoreDef = findViewById(R.id.restoreDefault);
        storeStx = findViewById(R.id.storeStxCfg);
        saveall = findViewById(R.id.saveAll);
        changeId = findViewById(R.id.changeId);
        txtSource = findViewById(R.id.txt_sA);
        txtDest = findViewById(R.id.txt_dA);
        progressBar = findViewById(R.id.progressBar);
        txtProgress = findViewById(R.id.txtProgress);
        MyDeviceManager.CanWrite(0, 0, 2, new byte[]{1, 0});
        gotoCanOpen.setVisibility(View.INVISIBLE);
        backToNova.setVisibility(View.INVISIBLE);
        restoreDef.setVisibility(View.INVISIBLE);
        storeStx.setVisibility(View.INVISIBLE);
        saveall.setVisibility(View.INVISIBLE);
    }


    private void onClick() {
        exit.setOnClickListener(view -> {
            if (!isSend) {
                exit.setEnabled(false);
                startService(new Intent(getApplicationContext(), UpdateValuesService.class));
                startActivity(new Intent(getApplicationContext(), Nuova_Machine_Settings.class));
                finish();
            }

        });
        txtSource.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(CanOpenTSM.this, txtSource);
            popupMenu.getMenu().add("Default");
            popupMenu.getMenu().add("Frame");
            popupMenu.getMenu().add("Boom1");
            popupMenu.getMenu().add("Boom2");
            popupMenu.getMenu().add("Stick");
            popupMenu.getMenu().add("Bucket");
            popupMenu.getMenu().add("Tilt");
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getTitle().toString()) {
                        case "Frame":
                            sourceID = 0x601;
                            sA = "From ID: Frame";
                            return true;
                        case "Boom1":
                            sourceID = 0x602;
                            sA = "From ID: Boom1";
                            return true;
                        case "Default":
                            sourceID = 0x603;
                            sA = "From ID: Default";
                            return true;
                        case "Stick":
                            sourceID = 0x604;
                            sA = "From ID: Stick";
                            return true;
                        case "Bucket":
                            sourceID = 0x605;
                            sA = "From ID: Bucket";
                            return true;
                        case "Tilt":
                            sourceID = 0x606;
                            sA = "From ID: Tilt";
                            return true;
                        case "Boom2":
                            sourceID = 0x607;
                            sA = "From ID: Boom2";
                            return true;

                        default:
                            return false;
                    }

                }
            });
            popupMenu.show();


        });
        txtDest.setOnClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(CanOpenTSM.this, txtDest);
            popupMenu.getMenu().add("Default");
            popupMenu.getMenu().add("Frame");
            popupMenu.getMenu().add("Boom1");
            popupMenu.getMenu().add("Boom2");
            popupMenu.getMenu().add("Stick");
            popupMenu.getMenu().add("Bucket");
            popupMenu.getMenu().add("Tilt");
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getTitle().toString()) {
                        case "Frame":
                            destID = 0x01;
                            dA = "To ID: Frame";
                            return true;
                        case "Boom1":
                            destID = 0x02;
                            dA = "To ID: Boom1";
                            return true;
                        case "Default":
                            destID = 0x03;
                            dA = "To ID: Default";
                            return true;
                        case "Stick":
                            destID = 0x04;
                            dA = "To ID: Stick";
                            return true;
                        case "Bucket":
                            destID = 0x05;
                            dA = "To ID: Bucket";
                            return true;
                        case "Tilt":
                            destID = 0x06;
                            dA = "To ID: Tilt";
                            return true;
                        case "Boom2":
                            destID = 0x07;
                            dA = "To ID: Boom2";
                            return true;

                        default:
                            return false;
                    }

                }
            });
            popupMenu.show();


        });


        gotoCanOpen.setOnClickListener(view -> {
            // askConf(1);
        });
        backToNova.setOnClickListener(view -> {
            // askConf(2);

        });

        restoreDef.setOnClickListener(view -> {
            // askConf(3);
        });

        storeStx.setOnClickListener(view -> {
            Handler handler = new Handler();
            handler.postDelayed(this::msg1, 50);
            handler.postDelayed(this::msg2, 200);
            handler.postDelayed(this::msg3, 400);
            handler.postDelayed(this::msg4, 600);
            handler.postDelayed(this::msg5, 800);
            handler.postDelayed(this::msg6, 1000);
            handler.postDelayed(this::msg7, 1200);

            handler.postDelayed(this::msg8, 1400);
            handler.postDelayed(this::msg9, 1600);
            handler.postDelayed(this::msg10, 1800);
            handler.postDelayed(this::msg11, 2000);
            handler.postDelayed(this::msg12, 2200);
            handler.postDelayed(this::msg13, 2400);
            handler.postDelayed(this::msg14, 2600);

            handler.postDelayed(this::msg15, 2800);
            handler.postDelayed(this::msg16, 3000);
            handler.postDelayed(this::msg17, 3200);
            handler.postDelayed(this::msg18, 3400);
            handler.postDelayed(this::msg19, 3600);
            handler.postDelayed(this::msg20, 3800);
            handler.postDelayed(this::msg21, 4000);

            handler.postDelayed(this::msg22, 4200);
            handler.postDelayed(this::msg23, 4400);
            handler.postDelayed(this::msg24, 4600);
            handler.postDelayed(this::msg25, 4800);
            handler.postDelayed(this::msg26, 5000);
            handler.postDelayed(this::msg27, 5200);
            handler.postDelayed(this::msg28, 5400);

            handler.postDelayed(this::msg29, 5600);
            handler.postDelayed(this::msg30, 5800);
            handler.postDelayed(this::msg31, 6000);
            handler.postDelayed(this::msg32, 6200);
            handler.postDelayed(this::msg33, 6400);
            handler.postDelayed(this::msg34, 6600);
            handler.postDelayed(this::msg35, 6800);

            handler.postDelayed(this::msg36, 7000);
            handler.postDelayed(this::msg37, 7200);
            handler.postDelayed(this::msgT, 7200);
            handler.postDelayed(this::msg38, 7400);
            handler.postDelayed(this::msg39, 7800);
            handler.postDelayed(this::msg40, 8000);

            handler.postDelayed(this::msg41, 8200);
            handler.postDelayed(this::msg42, 8400);
            handler.postDelayed(this::msg43, 8600);
            handler.postDelayed(this::msg44, 8800);
            handler.postDelayed(this::msg45, 9000);
            handler.postDelayed(this::msg46, 9200);


        });

        saveall.setOnClickListener(view -> {
         /*   Handler handler = new Handler();
           handler.postDelayed(this::msg47, 1);
            handler.postDelayed(this::msg48, 200);
            handler.postDelayed(this::msg49, 400);
            handler.postDelayed(this::msg50, 600);
            handler.postDelayed(this::msg51, 800);
            handler.postDelayed(this::msg52, 1000);
            handler.postDelayed(this::msg53, 1200);
           */

        });

        changeId.setOnClickListener(view -> {
            Handler handler = new Handler();
            MyDeviceManager.CanWrite(0, sourceID, 8, new byte[]{0x2F, 1, 0x30, 0, (byte) (destID), 0, 0, 0});
            handler.postDelayed(this::msg47, 100);
            handler.postDelayed(this::msg48, 200);
            handler.postDelayed(this::msg49, 400);
            handler.postDelayed(this::msg50, 600);
            handler.postDelayed(this::msg51, 800);
            handler.postDelayed(this::msg52, 1000);
            handler.postDelayed(this::msg53, 1200);
        });
    }

    public void updateUI() {
        try {
            if (isSend) {
                progressBar.setVisibility(View.VISIBLE);
                txtProgress.setVisibility(View.VISIBLE);


            } else {
                progressBar.setVisibility(View.INVISIBLE);
                txtProgress.setVisibility(View.INVISIBLE);
            }
            txtSource.setText(sA);
            txtDest.setText(dA);
            cbFR.setChecked(CanService.frameOK);
            cbB1.setChecked(CanService.boom1OK);
            cbB2.setChecked(CanService.boom2OK);
            cbST.setChecked(CanService.stickOK);
            cbBK.setChecked(CanService.bucketOK);
            cbTL.setChecked(CanService.tiltOK);
            cbDEF.setChecked(Sensors_Decoder.flagDefault != -100);
            if (isSend) {
                gotoCanOpen.setVisibility(View.INVISIBLE);
                backToNova.setVisibility(View.INVISIBLE);
                restoreDef.setVisibility(View.INVISIBLE);
                storeStx.setVisibility(View.INVISIBLE);
                saveall.setVisibility(View.INVISIBLE);
            }
            if (!isSend) {
               // gotoCanOpen.setVisibility(View.VISIBLE);
               // backToNova.setVisibility(View.VISIBLE);
               // restoreDef.setVisibility(View.VISIBLE);
                storeStx.setVisibility(View.VISIBLE);
               // saveall.setVisibility(View.VISIBLE);
            }
            txtProgress.setText(String.valueOf(progresso)+"%");

        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }



    private void msg1() {
        isSend = true;
        //msg1
        MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{0x23, (byte) 0x80, 0x1F, 0, 8, 0, 0, 0});
        //NMT startup avvia la comunicazione senza id 0 iniziale
        progresso+=2;
    }

    private void msg2() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{43, 0, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{43, 0, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg3() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{43, 2, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{43, 2, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg4() {
        MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{43, 0, 96, 0, 10, 0, 0, 0});
        progresso+=2;

    }

    private void msg5() {
        MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{47, 17, 96, 0, 65, 0, 0, 0});
        progresso+=2;

    }

    private void msg6() {
        MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) 3, 0, 0, 0});
        progresso+=2;

    }

    private void msg7() {
        MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
        progresso+=2;

    }

    private void msg8() {
        MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{0x23, (byte) 0x80, 0x1F, 0, 8, 0, 0, 0});//NMT statyup
        progresso+=2;

    }

    private void msg9() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{43, 0, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{43, 0, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg10() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{43, 2, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{43, 2, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg11() {
        MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{43, 0, 96, 0, 10, 0, 0, 0});
        progresso+=2;

    }

    private void msg12() {
        MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{47, 17, 96, 0, 65, 0, 0, 0});
        progresso+=2;
    }

    private void msg13() {
        MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) 3, 0, 0, 0});
        progresso+=2;
    }

    private void msg14() {
        MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
        progresso+=2;
    }

    private void msg15() {
        MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{0x23, (byte) 0x80, 0x1F, 0, 8, 0, 0, 0});
        progresso+=2;
    }

    private void msg16() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{43, 0, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{43, 0, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg17() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{43, 2, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{43, 2, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg18() {
        MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{43, 0, 96, 0, 10, 0, 0, 0});
        progresso+=2;
    }

    private void msg19() {
        MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{47, 17, 96, 0, 65, 0, 0, 0});
        progresso+=2;
    }

    private void msg20() {
        MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) 3, 0, 0, 0});
        progresso+=2;
    }

    private void msg21() {
        MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
        progresso+=2;
    }

    private void msg22() {
        MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{0x23, (byte) 0x80, 0x1F, 0, 8, 0, 0, 0});//NMT STARTUP
        progresso+=2;
    }

    private void msg23() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{43, 0, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{43, 0, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg24() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{43, 2, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{43, 2, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg25() {
        MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{43, 0, 96, 0, 10, 0, 0, 0});
        progresso+=2;
    }

    private void msg26() {
        MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{47, 17, 96, 0, 65, 0, 0, 0});
        progresso+=2;
    }

    private void msg27() {
        MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) 3, 0, 0, 0});
        progresso+=2;
    }

    private void msg28() {
        MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
        progresso+=2;
    }

    private void msg29() {
        MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{0x23, (byte) 0x80, 0x1F, 0, 8, 0, 0, 0});//NMT STARTUP
        progresso+=2;
    }

    private void msg30() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{43, 0, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{43, 0, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg31() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{43, 2, 24, 5, 0, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{43, 2, 24, 5, 50, 0, 0, 0});
            MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg32() {
        MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{43, 0, 96, 0, 10, 0, 0, 0});
        progresso+=2;
    }

    private void msg33() {
        MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{47, 17, 96, 0, 65, 0, 0, 0});
        progresso+=2;
    }

    private void msg34() {
        MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) 3, 0, 0, 0});
        progresso+=2;
    }

    private void msg35() {
        MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
        progresso+=2;
    }

    private void msg36() {
        MyDeviceManager.CanWrite(0, 0x606, 8, new byte[]{0x23, (byte) 0x80, 0x1F, 0, 8, 0, 0, 0});//NMT STARTUP
        progresso+=2;
    }

    private void msg37() {
        MyDeviceManager.CanWrite(0, 0x606, 8, new byte[]{43, 0, 24, 5, 0, 0, 0, 0});
        progresso+=2;
    }
    private void msgT(){
        MyDeviceManager.CanWrite(0, 0x606, 8, new byte[]{43, 2, 24, 5, 50, 0, 0, 0});
        MyDeviceManager.CanWrite(0, 0x606, 8, new byte[]{43, 3, 24, 5, 0, 0, 0, 0});//qui si abilita o disabilita il 480+id
        progresso+=2;
    }

    private void msg38() {
        MyDeviceManager.CanWrite(0, 0x606, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) 0x03, 0, 0, 0});
        progresso+=2;
    }

    private void msg39() {
        MyDeviceManager.CanWrite(0, 0x606, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
        progresso+=2;
    }

    private void msg40() {
        MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{0x23, (byte) 0x80, 0x1F, 0, 8, 0, 0, 0});//NMT STARTUP
        progresso+=2;
    }

    private void msg41() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{43, 0, 24, 5, 50, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{43, 0, 24, 5, 0, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg42() {
        if (DataSaved.isCanOpen == 2) {
            MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{43, 2, 24, 5, 0, 0, 0, 0});
        } else if (DataSaved.isCanOpen == 3) {
            MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{43, 2, 24, 5, 50, 0, 0, 0});
        }
        progresso+=2;
    }

    private void msg43() {
        MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{43, 0, 96, 0, 10, 0, 0, 0});
        progresso+=2;
    }

    private void msg44() {
        MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{47, 17, 96, 0, 65, 0, 0, 0});
        progresso+=2;
    }

    private void msg45() {
        MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) 0x03, 0, 0, 0});
        progresso+=5;
    }

    private void msg46() {
        progresso=100;
        MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
        new CustomToast(CanOpenTSM.this, getResources().getString(R.string.powercycle_sensor)).show();
        isSend = false;
        progresso=0;

    }


    /////////////////////////////
    private void msg47() {
        isSend = true;
        progresso=20;
        MyDeviceManager.CanWrite(0, 0x601, 8, new byte[]{0x23, 0x10, 0x10, 0x01, 0x73, 0x61, 0x76, 0x65});
    }

    private void msg48() {
        progresso=30;
        MyDeviceManager.CanWrite(0, 0x602, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});

    }

    private void msg49() {
        progresso=60;
        MyDeviceManager.CanWrite(0, 0x607, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});

    }

    private void msg50() {
        progresso=65;
        MyDeviceManager.CanWrite(0, 0x604, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});

    }

    private void msg51() {
        progresso=70;
        MyDeviceManager.CanWrite(0, 0x605, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});

    }

    private void msg52() {
        progresso=85;
        MyDeviceManager.CanWrite(0, 0x606, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});

    }

    private void msg53() {
        progresso=100;
        MyDeviceManager.CanWrite(0, 0x603, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
        new CustomToast(CanOpenTSM.this, getResources().getString(R.string.powercycle_sensor)).show();
        isSend = false;
        progresso=0;



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        isSend = false;

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }
}