package gui.tech_menu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;


import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import utils.MyDeviceManager;

public class DampingActivity extends AppCompatActivity {
    String msgInfo="3=Default Value\n\n" +
            "1=Min Sensor SPEED\n\n" +
            "2..7=Increase SPEED by 1\n\n" +
            "8=Max Sensor SPEED\n\n" +
            "Adjust the value and\n\nPowerCycle all sensors";
    boolean isSend=false;
    ImageView back,send;
    TextView tvfr, tvb1, tvb2, tvst, tvbk, tvtl,tvinfo;
    Button plus_fr, plus_b1, plus_b2, plus_st, plus_bk, plus_tl, min_fr, min_b1, min_b2, min_st, min_bk, min_tl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damping_7);

        if (DataSaved.isCanOpen==3||DataSaved.isCanOpen==2){
            readTSM();
            (new Handler()).postDelayed(this::readTSM, 1000);
        }else {
            MyDeviceManager.CanWrite(0,0,2,new byte[]{1,0});
        }

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
        tvinfo=findViewById(R.id.img1);
        send=findViewById(R.id.sendMsg);
        tvinfo.setText(msgInfo);
        (new Handler()).postDelayed(this::updateTV, 500);
        (new Handler()).postDelayed(this::onClick, 1000);
    }

    private void onClick() {
        send.setOnClickListener(view -> {
            save();
        });
        back.setOnClickListener((View v) -> {
            if(!isSend){
                back.setEnabled(false);
                startActivity(new Intent(this, Nuova_Machine_Settings.class));
                }
            finish();
        });

        plus_fr.setOnClickListener(view -> {
            if(DataSaved.damp_Fr>0){
            DataSaved.damp_Fr=DataSaved.damp_Fr%8;
            DataSaved.damp_Fr+=1;
            updateTV();}
        });
        min_fr.setOnClickListener(view -> {
            if(DataSaved.damp_Fr>0){
            DataSaved.damp_Fr-=1;
            if(DataSaved.damp_Fr<1)DataSaved.damp_Fr=1;
            updateTV();}

        });

        plus_b1.setOnClickListener(view -> {
            if(DataSaved.damp_B1>0){
            DataSaved.damp_B1=DataSaved.damp_B1%8;
            DataSaved.damp_B1 +=1;
            updateTV();}
        });
        min_b1.setOnClickListener(view -> {
            if(DataSaved.damp_B1>0){
            DataSaved.damp_B1-=1;
            if(DataSaved.damp_B1<1)DataSaved.damp_B1=1;
            updateTV();}

        });

        plus_b2.setOnClickListener(view -> {
            if(DataSaved.damp_B2>0){
            DataSaved.damp_B2=DataSaved.damp_B2%8;
            DataSaved.damp_B2 +=1;
            updateTV();}
        });
        min_b2.setOnClickListener(view -> {
            if(DataSaved.damp_B2>0){
            DataSaved.damp_B2-=1;
            if(DataSaved.damp_B2<1)DataSaved.damp_B2=1;
            updateTV();}

        });

        plus_st.setOnClickListener(view -> {
            if(DataSaved.damp_St>0){
            DataSaved.damp_St=DataSaved.damp_St%8;
            DataSaved.damp_St +=1;
            updateTV();}
        });
        min_st.setOnClickListener(view -> {
            if(DataSaved.damp_St>0){
            DataSaved.damp_St-=1;
            if(DataSaved.damp_St<1)DataSaved.damp_St=1;
            updateTV();}

        });

        plus_bk.setOnClickListener(view -> {
            if(DataSaved.damp_Bk>0){
            DataSaved.damp_Bk=DataSaved.damp_Bk%8;
            DataSaved.damp_Bk +=1;
            updateTV();}
        });
        min_bk.setOnClickListener(view -> {
            if(DataSaved.damp_Bk>0){
            DataSaved.damp_Bk-=1;
            if(DataSaved.damp_Bk<1)DataSaved.damp_Bk=1;
            updateTV();}

        });

        plus_tl.setOnClickListener(view -> {
            if(DataSaved.damp_Tl>0){
            DataSaved.damp_Tl=DataSaved.damp_Tl%8;
            DataSaved.damp_Tl +=1;
            updateTV();}
        });
        min_tl.setOnClickListener(view -> {
            if(DataSaved.damp_Tl>0){
            DataSaved.damp_Tl -=1;
            if(DataSaved.damp_Tl<1)DataSaved.damp_Tl=1;
            updateTV();}

        });

    }
    public void save(){

        if(DataSaved.isCanOpen==3||DataSaved.isCanOpen==2) {
            for (int i = 0; i < 50000; i++) {
                if (i == 1) {
                    isSend = true;

                }

                if (i == 2000) {
                    MyDeviceManager.CanWrite(0, 0x601,8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_Fr, 0, 0, 0});
                }
                if (i == 2500) {
                    MyDeviceManager.CanWrite(0, 0x601,8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }


                if (i == 5000) {
                    MyDeviceManager.CanWrite(0, 0x602,8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_B1, 0, 0, 0});
                }
                if (i == 5500) {
                    MyDeviceManager.CanWrite(0, 0x602,8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }

                if (i == 8000) {
                    MyDeviceManager.CanWrite(0, 0x607,8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_B2, 0, 0, 0});
                }
                if (i == 8500) {
                    MyDeviceManager.CanWrite(0, 0x607,8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }


                if (i == 11000) {
                    MyDeviceManager.CanWrite(0, 0x604,8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_St, 0, 0, 0});
                }

                if (i == 11500) {
                    MyDeviceManager.CanWrite(0, 0x604,8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }


                if (i == 12500) {
                    MyDeviceManager.CanWrite(0, 0x605,8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_Bk, 0, 0, 0});
                }
                if (i == 14500) {
                    MyDeviceManager.CanWrite(0, 0x605, 8,new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }
                if (i == 17000) {
                    MyDeviceManager.CanWrite(0, 0x606,8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) DataSaved.damp_Tl, 0, 0, 0});
                }
                if (i == 17500) {
                    MyDeviceManager.CanWrite(0, 0x606, 8,new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                }

                if (i == 20000) {
                    MyDeviceManager.CanWrite(0, 0x603,8, new byte[]{0x2F, 0X0B, 0X30, 0X06, (byte) 0x04, 0, 0, 0});
                }
                if (i == 20500) {
                    MyDeviceManager.CanWrite(0, 0x603,8, new byte[]{35, 16, 16, 1, 0x73, 0x61, 0x76, 0x65});
                    new CustomToast(DampingActivity.this, getResources().getString(R.string.powercycle_sensor)).show();
                    (new Handler()).postDelayed(this::powerOn, 1000);
                    (new Handler()).postDelayed(this::powerOff, 5000);
                    isSend = false;

                }
            }
        }

    }
    private void powerOn(){
       // VanCmd.exec("echo \"100019\" >/dev/gpio_dev", 1);

    }

    private void powerOff() {
        //VanCmd.exec("echo \"100018\" >/dev/gpio_dev", 0);
    }

    private void updateTV(){


            tvfr.setText(String.valueOf(DataSaved.damp_Fr));

            tvb1.setText(String.valueOf(DataSaved.damp_B1));

            tvb2.setText(String.valueOf(DataSaved.damp_B2));

            tvst.setText(String.valueOf(DataSaved.damp_St));

            tvbk.setText(String.valueOf(DataSaved.damp_Bk));

            tvtl.setText(String.valueOf(DataSaved.damp_Tl));


    }
    private void readTSM(){
        MyDeviceManager.CanWrite(0,0x601,8,new byte[]{0x40,0x0B,0x30,0x06,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x602,8,new byte[]{0x40,0x0B,0x30,0x06,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x607,8,new byte[]{0x40,0x0B,0x30,0x06,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x604,8,new byte[]{0x40,0x0B,0x30,0x06,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x605,8,new byte[]{0x40,0x0B,0x30,0x06,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x606,8,new byte[]{0x40,0x0B,0x30,0x06,0,0,0,0});
    }
    private void  readNova(){
        MyDeviceManager.CanWrite(0,0x601,8,new byte[]{0x40,0x60,0x23,0x0,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x602,8,new byte[]{0x40,0x60,0x23,0x0,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x603,8,new byte[]{0x40,0x60,0x23,0x0,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x604,8,new byte[]{0x40,0x60,0x23,0x0,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x605,8,new byte[]{0x40,0x60,0x23,0x0,0,0,0,0});
        MyDeviceManager.CanWrite(0,0x606,8,new byte[]{0x40,0x60,0x23,0x0,0,0,0,0});
    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isSend=false;
    }

}