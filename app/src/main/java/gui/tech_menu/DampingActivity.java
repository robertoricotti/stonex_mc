package gui.tech_menu;

import static services.CanService.boom1OK;
import static services.CanService.boom2OK;
import static services.CanService.bucketOK;
import static services.CanService.frameOK;
import static services.CanService.stickOK;
import static services.CanService.tiltOK;
import static utils.MyTypes.FMI_SENS;
import static utils.MyTypes.TSM_ACC;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import utils.MyDeviceManager;

public class DampingActivity extends BaseClass {
    String msgInfo = "3=Default Value\n\n" +
            "1=Min Sensor SPEED\n\n" +
            "2..7=Increase SPEED by 1\n\n" +
            "8=Max Sensor SPEED\n\n" +
            "Adjust the value and\n\nPowerCycle all sensors";
    boolean isSend = false;
    ImageView back, send, readalli;
    TextView tvfr, tvb1, tvb2, tvst, tvbk, tvtl, tvinfo;
    Button plus_fr, plus_b1, plus_b2, plus_st, plus_bk, plus_tl, min_fr, min_b1, min_b2, min_st, min_bk, min_tl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damping_7);


        readAll();

        back = findViewById(R.id._back);
        tvfr = findViewById(R.id.tx_fr);
        tvb1 = findViewById(R.id.tx_b1);
        tvb2 = findViewById(R.id.tx_b2);
        tvst = findViewById(R.id.tx_st);
        tvbk = findViewById(R.id.tx_bk);
        tvtl = findViewById(R.id.tx_tl);
        plus_fr = findViewById(R.id.plus_fr);
        plus_b1 = findViewById(R.id.plus_b1);
        plus_b2 = findViewById(R.id.plus_b2);
        plus_st = findViewById(R.id.plus_st);
        plus_bk = findViewById(R.id.plus_bk);
        plus_tl = findViewById(R.id.plus_tl);
        min_fr = findViewById(R.id.min_fr);
        min_b1 = findViewById(R.id.min_b1);
        min_b2 = findViewById(R.id.min_b2);
        min_st = findViewById(R.id.min_st);
        min_bk = findViewById(R.id.min_bk);
        min_tl = findViewById(R.id.min_tl);
        tvinfo = findViewById(R.id.img1);
        send = findViewById(R.id.sendMsg);
        readalli = findViewById(R.id.readAlli);
        tvinfo.setText(msgInfo);
        onClick();

    }

    private void onClick() {
        readalli.setOnClickListener(view -> {
            readAll();
        });
        send.setOnClickListener(view -> {
            save();
            new CustomToast(this, "PARAMETERS SAVED").show_alert();
            if (!isSend) {
                send.setEnabled(false);
                startActivity(new Intent(this, Nuova_Machine_Settings.class));
                finish();
            }
        });
        back.setOnClickListener((View v) -> {
            if (!isSend) {
                back.setEnabled(false);
                startActivity(new Intent(this, Nuova_Machine_Settings.class));
                finish();
            }

        });

        plus_fr.setOnClickListener(view -> {
            if (DataSaved.damp_Fr > 0) {
                DataSaved.damp_Fr = DataSaved.damp_Fr % 8;
                DataSaved.damp_Fr += 1;

            }
        });
        min_fr.setOnClickListener(view -> {
            if (DataSaved.damp_Fr > 0) {
                DataSaved.damp_Fr -= 1;
                if (DataSaved.damp_Fr < 1) DataSaved.damp_Fr = 1;

            }

        });

        plus_b1.setOnClickListener(view -> {
            if (DataSaved.damp_B1 > 0) {
                DataSaved.damp_B1 = DataSaved.damp_B1 % 8;
                DataSaved.damp_B1 += 1;

            }
        });
        min_b1.setOnClickListener(view -> {
            if (DataSaved.damp_B1 > 0) {
                DataSaved.damp_B1 -= 1;
                if (DataSaved.damp_B1 < 1) DataSaved.damp_B1 = 1;

            }

        });

        plus_b2.setOnClickListener(view -> {
            if (DataSaved.damp_B2 > 0) {
                DataSaved.damp_B2 = DataSaved.damp_B2 % 8;
                DataSaved.damp_B2 += 1;

            }
        });
        min_b2.setOnClickListener(view -> {
            if (DataSaved.damp_B2 > 0) {
                DataSaved.damp_B2 -= 1;
                if (DataSaved.damp_B2 < 1) DataSaved.damp_B2 = 1;

            }

        });

        plus_st.setOnClickListener(view -> {
            if (DataSaved.damp_St > 0) {
                DataSaved.damp_St = DataSaved.damp_St % 8;
                DataSaved.damp_St += 1;

            }
        });
        min_st.setOnClickListener(view -> {
            if (DataSaved.damp_St > 0) {
                DataSaved.damp_St -= 1;
                if (DataSaved.damp_St < 1) DataSaved.damp_St = 1;

            }

        });

        plus_bk.setOnClickListener(view -> {
            if (DataSaved.damp_Bk > 0) {
                DataSaved.damp_Bk = DataSaved.damp_Bk % 8;
                DataSaved.damp_Bk += 1;

            }
        });
        min_bk.setOnClickListener(view -> {
            if (DataSaved.damp_Bk > 0) {
                DataSaved.damp_Bk -= 1;
                if (DataSaved.damp_Bk < 1) DataSaved.damp_Bk = 1;

            }

        });

        plus_tl.setOnClickListener(view -> {
            if (DataSaved.damp_Tl > 0) {
                DataSaved.damp_Tl = DataSaved.damp_Tl % 8;
                DataSaved.damp_Tl += 1;

            }
        });
        min_tl.setOnClickListener(view -> {
            if (DataSaved.damp_Tl > 0) {
                DataSaved.damp_Tl -= 1;
                if (DataSaved.damp_Tl < 1) DataSaved.damp_Tl = 1;

            }

        });

    }

    public void save() {

        if (DataSaved.isCanOpen == TSM_ACC) {
            for (int i = 0; i < 50000; i++) {
                if (i == 1) {
                    isSend = true;

                }

                if (i == 2000) {
                    MyDeviceManager.CanWrite(true, 0, 0x601, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_Fr, 0, 0, 0});
                }
                if (i == 2500) {
                    MyDeviceManager.CanWrite(true, 0, 0x601, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }


                if (i == 5000) {
                    MyDeviceManager.CanWrite(true, 0, 0x602, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_B1, 0, 0, 0});
                }
                if (i == 5500) {
                    MyDeviceManager.CanWrite(true, 0, 0x602, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }

                if (i == 8000) {
                    MyDeviceManager.CanWrite(true, 0, 0x607, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_B2, 0, 0, 0});
                }
                if (i == 8500) {
                    MyDeviceManager.CanWrite(true, 0, 0x607, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }


                if (i == 11000) {
                    MyDeviceManager.CanWrite(true, 0, 0x604, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_St, 0, 0, 0});
                }

                if (i == 11500) {
                    MyDeviceManager.CanWrite(true, 0, 0x604, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }


                if (i == 12500) {
                    MyDeviceManager.CanWrite(true, 0, 0x605, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_Bk, 0, 0, 0});
                }
                if (i == 14500) {
                    MyDeviceManager.CanWrite(true, 0, 0x605, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }
                if (i == 17000) {
                    MyDeviceManager.CanWrite(true, 0, 0x606, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_Tl, 0, 0, 0});
                }
                if (i == 17500) {
                    MyDeviceManager.CanWrite(true, 0, 0x606, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }

                if (i == 20000) {
                    MyDeviceManager.CanWrite(true, 0, 0x603, 8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) 0x04, 0, 0, 0});
                }
                if (i == 20500) {
                    MyDeviceManager.CanWrite(true, 0, 0x603, 8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                    isSend = false;

                }
            }
        }
        if (DataSaved.isCanOpen == FMI_SENS) {
            for (int i = 0; i < 50000; i++) {
                if (i == 1) {
                    isSend = true;

                }

                if (i == 2000) {
                    MyDeviceManager.CanWrite(true, 0, 0x601, 8, new byte[]{0x2F, 0X0C, 0X30, 0X02, (byte) DataSaved.damp_Fr, 0, 0, 0});
                }


                if (i == 5000) {
                    MyDeviceManager.CanWrite(true, 0, 0x602, 8, new byte[]{0x2F, 0X0C, 0X30, 0X02, (byte) DataSaved.damp_B1, 0, 0, 0});
                }


                if (i == 8000) {
                    MyDeviceManager.CanWrite(true, 0, 0x607, 8, new byte[]{0x2F, 0X0C, 0X30, 0X02, (byte) DataSaved.damp_B2, 0, 0, 0});
                }


                if (i == 11000) {
                    MyDeviceManager.CanWrite(true, 0, 0x604, 8, new byte[]{0x2F, 0X0C, 0X30, 0X02, (byte) DataSaved.damp_St, 0, 0, 0});
                }


                if (i == 12500) {
                    MyDeviceManager.CanWrite(true, 0, 0x605, 8, new byte[]{0x2F, 0X0C, 0X30, 0X02, (byte) DataSaved.damp_Bk, 0, 0, 0});
                }

                if (i == 17000) {
                    MyDeviceManager.CanWrite(true, 0, 0x606, 8, new byte[]{0x2F, 0X0C, 0X30, 0X02, (byte) DataSaved.damp_Tl, 0, 0, 0});
                }


                if (i == 20000) {
                    MyDeviceManager.CanWrite(true, 0, 0x603, 8, new byte[]{0x2F, 0X0C, 0X30, 0X02, (byte) 0x04, 0, 0, 0});
                    isSend = false;
                }

            }
        }

    }

    private void readAll() {
        if (DataSaved.isCanOpen == TSM_ACC) {
            readTSM();

        }
        if (DataSaved.isCanOpen == FMI_SENS) {
            readFMI();

        }
    }


    public static void readTSM() {
        MyDeviceManager.CanWrite(true, 0, 0x601, 8, new byte[]{0x40, 0x0B, 0x30, 0x06, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x602, 8, new byte[]{0x40, 0x0B, 0x30, 0x06, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x607, 8, new byte[]{0x40, 0x0B, 0x30, 0x06, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x604, 8, new byte[]{0x40, 0x0B, 0x30, 0x06, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x605, 8, new byte[]{0x40, 0x0B, 0x30, 0x06, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x606, 8, new byte[]{0x40, 0x0B, 0x30, 0x06, 0, 0, 0, 0});
    }

    public static void readFMI() {
        MyDeviceManager.CanWrite(true, 0, 0x601, 8, new byte[]{0x40, 0x0C, 0x30, 0x02, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x602, 8, new byte[]{0x40, 0x0C, 0x30, 0x02, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x607, 8, new byte[]{0x40, 0x0C, 0x30, 0x02, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x604, 8, new byte[]{0x40, 0x0C, 0x30, 0x02, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x605, 8, new byte[]{0x40, 0x0C, 0x30, 0x02, 0, 0, 0, 0});
        MyDeviceManager.CanWrite(true, 0, 0x606, 8, new byte[]{0x40, 0x0C, 0x30, 0x02, 0, 0, 0, 0});
    }

    public void updateUI() {

        if (!frameOK) {
            tvfr.setTextColor(Color.RED);
            tvfr.setText("DISC");
        } else {
            tvfr.setTextColor(Color.BLACK);
            tvfr.setText(String.valueOf(DataSaved.damp_Fr));
        }

        if (!boom1OK) {
            tvb1.setTextColor(Color.RED);
            tvb1.setText("DISC");
        } else {
            tvb1.setTextColor(Color.BLACK);
            tvb1.setText(String.valueOf(DataSaved.damp_B1));
        }

        if (!boom2OK) {
            tvb2.setTextColor(Color.RED);
            tvb2.setText("DISC");
        } else {
            tvb2.setTextColor(Color.BLACK);
            tvb2.setText(String.valueOf(DataSaved.damp_B2));
        }


        if (!stickOK) {
            tvst.setTextColor(Color.RED);
            tvst.setText("DISC");
        } else {
            tvst.setTextColor(Color.BLACK);
            tvst.setText(String.valueOf(DataSaved.damp_St));
        }

        if (!bucketOK) {
            tvbk.setTextColor(Color.RED);
            tvbk.setText("DISC");
        } else {
            tvbk.setTextColor(Color.BLACK);
            tvbk.setText(String.valueOf(DataSaved.damp_Bk));
        }

        if (!tiltOK) {
            tvtl.setTextColor(Color.RED);
            tvtl.setText("DISC");
        } else {
            tvtl.setTextColor(Color.BLACK);
            tvtl.setText(String.valueOf(DataSaved.damp_Tl));
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSend = false;
    }

}