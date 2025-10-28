package gui.hydro;

import static packexcalib.exca.DataSaved.REVERSE_LEFT;
import static packexcalib.exca.DataSaved.REVERSE_RIGHT;
import static packexcalib.exca.DataSaved.REVERSE_SS;
import static packexcalib.exca.DataSaved.maxSpeedLeftDW;
import static packexcalib.exca.DataSaved.maxSpeedLeftUP;
import static packexcalib.exca.DataSaved.maxSpeedRightDW;
import static packexcalib.exca.DataSaved.maxSpeedRightUP;
import static packexcalib.exca.DataSaved.maxSpeedSS_A;
import static packexcalib.exca.DataSaved.maxSpeedSS_B;
import static packexcalib.exca.DataSaved.minSpeedLeftDW;
import static packexcalib.exca.DataSaved.minSpeedLeftUP;
import static packexcalib.exca.DataSaved.minSpeedRightDW;
import static packexcalib.exca.DataSaved.minSpeedRightUP;
import static packexcalib.exca.DataSaved.minSpeedSS_A;
import static packexcalib.exca.DataSaved.minSpeedSS_B;
import static services.CanService.CAT_Connected;
import static services.CanService.JD_Connected;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import packexcalib.exca.DataSaved;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import services.CanService;
import utils.MyMCUtils;
import utils.MyData;
import utils.MyDeviceManager;

public class DEERE_LIEBHERR_Activity extends AppCompatActivity {
    Handler sendHandler = new Handler();
    Runnable sendRunnable;
    Handler handler = new Handler();
    Runnable repeater;
    int voceMenu, indexMachine;
    ImageView back, menuP, menuM, valM, valP,ECUCONN;
    TextView testValve, testo, funzione, tipo, pagina;
    int maxMenu;
    EditText valore;
    CheckBox rev_left,rev_right,rev_ss;


    int valueLEFT = 20000;
    int valueRIGHT = 20000;
    int valueSS = 20000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deere_liebherr);
        MyDeviceManager.CanWrite(1, 0x18EEFF85, 8,
                new byte[]{(byte) 0xF4,
                        (byte) 0xF0,
                        (byte) 0x13,
                        (byte) 0x23,
                        (byte) 0x0,
                        (byte) 0x82,
                        (byte) 0x0,
                        (byte) 0xB0});
        maxMenu = 13;
        findView();
        onClick();
        updateUI();
        voceMenu = 0;
        try {
            indexMachine = MyData.get_Int("MachineSelected");
        } catch (Exception e) {
            indexMachine = 0;
        }
        sendMsg();
    }

    private void findView() {
        back = findViewById(R.id.btn_1);
        menuP = findViewById(R.id.toright);
        menuM = findViewById(R.id.toleft);
        testValve = findViewById(R.id.test);
        funzione = findViewById(R.id.funzione);
        testo = findViewById(R.id.testo);
        tipo = findViewById(R.id.txtTipo);
        valore = findViewById(R.id.valore);
        valM = findViewById(R.id.val_M);
        valP = findViewById(R.id.val_P);
        pagina = findViewById(R.id.pagina);
        rev_left=findViewById(R.id.rev_left);
        rev_right=findViewById(R.id.rev_right);
        rev_ss=findViewById(R.id.rev_ss);
        ECUCONN=findViewById(R.id.ECUCONN);
    }

    private void onClick() {
        rev_left.setOnClickListener(view -> {
            if(REVERSE_LEFT>0){
                REVERSE_LEFT=0;
            }else {
                REVERSE_LEFT=1;
            }
            MyData.push("M"+indexMachine+"REVERSE_LEFT",String.valueOf(REVERSE_LEFT));

        });
        rev_right.setOnClickListener(view -> {
            if(REVERSE_RIGHT>0){
                REVERSE_RIGHT=0;
            }else {
                REVERSE_RIGHT=1;
            }
            MyData.push("M"+indexMachine+"REVERSE_RIGHT",String.valueOf(REVERSE_RIGHT));
        });
        rev_ss.setOnClickListener(view -> {
            if(REVERSE_SS>0){
                REVERSE_SS=0;
            }else {
                REVERSE_SS=1;
            }
            MyData.push("M"+indexMachine+"REVERSE_SS",String.valueOf(REVERSE_SS));
        });

        testValve.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    testValve.setAlpha(0.1f);
                    switch (voceMenu) {
                        case 1:
                            valueLEFT = MyMCUtils.myscalIntD(minSpeedLeftUP,0,255,20000,35000);
                            valueRIGHT = 20000;
                            valueSS = 20000;

                            break;
                        case 0:
                            valueLEFT = MyMCUtils.myscalIntD(maxSpeedLeftUP,0,255,20000,35000);
                            valueRIGHT = 20000;
                            valueSS = 20000;

                            break;
                        case 3:
                            valueLEFT =  MyMCUtils.myscalIntD(minSpeedLeftDW, 0, 255,20000,5000);
                            valueRIGHT = 20000;
                            valueSS = 20000;

                            break;
                        case 2:
                            valueLEFT =  MyMCUtils.myscalIntD(maxSpeedLeftDW, 0, 255,20000,5000);
                            valueRIGHT = 20000;
                            valueSS = 20000;

                            break;

                        case 5:
                            valueRIGHT = MyMCUtils.myscalIntD(minSpeedRightUP, 0, 255,20000,35000);
                            valueLEFT = 20000;
                            valueSS = 20000;

                            break;
                        case 4:
                            valueRIGHT =  MyMCUtils.myscalIntD(maxSpeedRightUP, 0, 255,20000,35000);
                            valueLEFT = 20000;
                            valueSS = 20000;

                            break;
                        case 7:
                            valueRIGHT = MyMCUtils.myscalIntD(minSpeedRightDW, 0, 255,20000,5000);
                            valueLEFT = 20000;
                            valueSS = 20000;

                            break;
                        case 6:
                            valueRIGHT = MyMCUtils.myscalIntD(maxSpeedRightDW, 0, 255,20000,5000);
                            valueLEFT = 20000;
                            valueSS = 20000;

                            break;

                        case 9:
                            valueSS = MyMCUtils.myscalIntD(minSpeedSS_A, 0, 255,20000,5000);
                            valueLEFT = 20000;
                            valueRIGHT = 20000;

                            break;
                        case 8:
                            valueSS = MyMCUtils.myscalIntD(maxSpeedSS_A, 0, 255,20000,5000);
                            valueLEFT = 20000;
                            valueRIGHT = 20000;

                            break;
                        case 11:
                            valueSS = MyMCUtils.myscalIntD(minSpeedSS_B, 0, 255,20000,35000);
                            valueLEFT = 20000;
                            valueRIGHT = 20000;

                            break;
                        case 10:
                            valueSS = MyMCUtils.myscalIntD(maxSpeedSS_B, 0, 255,20000,35000);
                            valueLEFT = 20000;
                            valueRIGHT = 20000;

                            break;


                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    testValve.setAlpha(1.0f);
                    valueLEFT = 20000;
                    valueRIGHT = 20000;
                    valueSS = 20000;
                    return true;
            }
            return false; // permette comunque al click di propagare
        });
        valM.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // esegui subito una volta
                    //executeMenuAction_M();

                    // definisci il runnable che ripete ogni 100ms
                    repeater = new Runnable() {
                        @Override
                        public void run() {
                            executeMenuAction_M();
                            handler.postDelayed(this, 100); // richiama dopo 100ms
                        }
                    };
                    handler.postDelayed(repeater, 1000); // primo repeat dopo 1000ms
                    return false;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // interrompi il loop
                    handler.removeCallbacks(repeater);
                    return false;
            }
            return false; // consumiamo l'evento
        });


        valM.setOnClickListener(view -> {
            switch (voceMenu) {
                case 1:
                    if (minSpeedLeftUP > 0)
                        minSpeedLeftUP--;
                    break;
                case 0:
                    if (maxSpeedLeftUP > 0)
                        maxSpeedLeftUP--;
                    break;
                case 3:
                    if (minSpeedLeftDW > 0)
                        minSpeedLeftDW--;
                    break;
                case 2:
                    if (maxSpeedLeftDW > 0)
                        maxSpeedLeftDW--;
                    break;
                case 5:
                    if (minSpeedRightUP > 0)
                        minSpeedRightUP--;
                    break;
                case 4:
                    if (maxSpeedRightUP > 0)
                        maxSpeedRightUP--;
                    break;
                case 7:
                    if (minSpeedRightDW > 0)
                        minSpeedRightDW--;
                    break;
                case 6:
                    if (maxSpeedRightDW > 0)
                        maxSpeedRightDW--;
                    break;
                case 9:
                    if (minSpeedSS_A > 0)
                        minSpeedSS_A--;
                    break;
                case 8:
                    if (maxSpeedSS_A > 0)
                        maxSpeedSS_A--;
                    break;
                case 11:
                    if (minSpeedSS_B > 0)
                        minSpeedSS_B--;
                    break;
                case 10:
                    if (maxSpeedSS_B > 0)
                        maxSpeedSS_B--;
                    break;

                default:
                    break;
            }

        });

        valP.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // esegui subito una volta
                    //executeMenuAction_P();

                    // definisci il runnable che ripete ogni 100ms
                    repeater = new Runnable() {
                        @Override
                        public void run() {
                            executeMenuAction_P();
                            handler.postDelayed(this, 100); // richiama dopo 100ms
                        }
                    };
                    handler.postDelayed(repeater, 1000); // primo repeat dopo 1000ms
                    return false;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // interrompi il loop
                    handler.removeCallbacks(repeater);
                    return false;
            }
            return false; // consumiamo l'evento
        });
        valP.setOnClickListener(view -> {
            switch (voceMenu) {
                case 1:
                    if (minSpeedLeftUP < 255)
                        minSpeedLeftUP++;
                    break;
                case 0:
                    if (maxSpeedLeftUP < 255)
                        maxSpeedLeftUP++;
                    break;
                case 3:
                    if (minSpeedLeftDW < 255)
                        minSpeedLeftDW++;
                    break;
                case 2:
                    if (maxSpeedLeftDW < 255)
                        maxSpeedLeftDW++;
                    break;
                case 5:
                    if (minSpeedRightUP < 255)
                        minSpeedRightUP++;
                    break;
                case 4:
                    if (maxSpeedRightUP < 255)
                        maxSpeedRightUP++;
                    break;
                case 7:
                    if (minSpeedRightDW < 255)
                        minSpeedRightDW++;
                    break;
                case 6:
                    if (maxSpeedRightDW < 255)
                        maxSpeedRightDW++;
                    break;
                case 9:
                    if (minSpeedSS_A < 255)
                        minSpeedSS_A++;
                    break;
                case 8:
                    if (maxSpeedSS_A < 255)
                        maxSpeedSS_A++;
                    break;
                case 11:
                    if (minSpeedSS_B < 255)
                        minSpeedSS_B++;
                    break;
                case 10:
                    if (maxSpeedSS_B < 255)
                        maxSpeedSS_B++;
                    break;

                default:
                    break;
            }

        });

        menuM.setOnClickListener(view -> {
            if (voceMenu > 0) {
                voceMenu--;
            } else {
                voceMenu = maxMenu - 1;
            }
            voceMenu = Math.abs(voceMenu) % maxMenu;

        });
        menuP.setOnClickListener(view -> {
            voceMenu++;
            voceMenu = Math.abs(voceMenu) % maxMenu;

        });

        back.setOnClickListener(view -> {
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.exit));
            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                back.setEnabled(false);
                sendHandler.removeCallbacks(sendRunnable);
                MyData.push("M" + indexMachine + "minSpeedLeftUP", String.valueOf(minSpeedLeftUP));
                MyData.push("M" + indexMachine + "maxSpeedLeftUP", String.valueOf(maxSpeedLeftUP));
                MyData.push("M" + indexMachine + "minSpeedLeftDW", String.valueOf(minSpeedLeftDW));
                MyData.push("M" + indexMachine + "maxSpeedLeftDW", String.valueOf(maxSpeedLeftDW));
                MyData.push("M" + indexMachine + "minSpeedRightUP", String.valueOf(minSpeedRightUP));
                MyData.push("M" + indexMachine + "maxSpeedRightUP", String.valueOf(maxSpeedRightUP));
                MyData.push("M" + indexMachine + "minSpeedRightDW", String.valueOf(minSpeedRightDW));
                MyData.push("M" + indexMachine + "maxSpeedRightDW", String.valueOf(maxSpeedRightDW));
                MyData.push("M" + indexMachine + "minSpeedSS_A", String.valueOf(minSpeedSS_A));
                MyData.push("M" + indexMachine + "maxSpeedSS_A", String.valueOf(maxSpeedSS_A));
                MyData.push("M" + indexMachine + "minSpeedSS_B", String.valueOf(minSpeedSS_B));
                MyData.push("M" + indexMachine + "maxSpeedSS_B", String.valueOf(maxSpeedSS_B));
                startActivity(new Intent(DEERE_LIEBHERR_Activity.this, Hydro_Activity_Entering.class));
                finish();
            });
            // Aggiungi il pulsante "No"
            builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
                //do nothing
            });
            // Mostra il dialog
            builder.show();


        });
    }

    public void updateUI() {
        rev_left.setChecked(DataSaved.REVERSE_LEFT == 1);
        rev_right.setChecked(DataSaved.REVERSE_RIGHT == 1);
        rev_ss.setChecked(DataSaved.REVERSE_SS == 1);

        if(REVERSE_LEFT==1){
            rev_left.setBackground(getDrawable(R.drawable.sfondo_auto_prepared));
        }else {
            rev_left.setBackground(getDrawable(R.drawable.sfondo_manuale));
        }

        if(REVERSE_RIGHT==1){
            rev_right.setBackground(getDrawable(R.drawable.sfondo_auto_prepared));
        }else {
            rev_right.setBackground(getDrawable(R.drawable.sfondo_manuale));
        }

        if(REVERSE_SS==1){
            rev_ss.setBackground(getDrawable(R.drawable.sfondo_auto_prepared));
        }else {
            rev_ss.setBackground(getDrawable(R.drawable.sfondo_manuale));
        }


        pagina.setText((voceMenu + 1) + " / " + (maxMenu));
        switch (voceMenu) {
            case 1:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(minSpeedLeftUP));
                funzione.setTextColor(Color.BLUE);
                funzione.setText("LEFT THRESHOLD UP");
                testo.setText("LEFT RISE Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;

            case 0:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(maxSpeedLeftUP));
                funzione.setTextColor(Color.RED);
                funzione.setText("LEFT MAX SPEED UP");
                testo.setText("LEFT RISE MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");


                break;

            case 3:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(minSpeedLeftDW));
                funzione.setTextColor(Color.BLUE);
                funzione.setText("LEFT THRESHOLD DOWN");
                testo.setText("LEFT LOWER Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;
            case 2:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(maxSpeedLeftDW));
                funzione.setTextColor(Color.RED);
                funzione.setText("LEFT MAX SPEED DOWN");
                testo.setText("LEFT LOWER MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");

                break;

            case 5:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(minSpeedRightUP));
                funzione.setTextColor(Color.BLUE);
                funzione.setText("RIGHT THRESHOLD UP");
                testo.setText("RIGHT RISE Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;

            case 4:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(maxSpeedRightUP));
                funzione.setTextColor(Color.RED);
                funzione.setText("RIGHT MAX SPEED UP");
                testo.setText("RIGHT RISE MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");
                break;

            case 7:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(minSpeedRightDW));
                funzione.setTextColor(Color.BLUE);
                funzione.setText("RIGHT THRESHOLD DOWN");
                testo.setText("RIGHT LOWER Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");
                break;

            case 6:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(maxSpeedRightDW));
                funzione.setTextColor(Color.RED);
                funzione.setText("RIGHT MAX SPEED DOWN");
                testo.setText("RIGHT LOWER MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");
                break;

            case 9:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(minSpeedSS_A));
                funzione.setTextColor(Color.BLUE);
                funzione.setText("BLADE SIDESHIFT THRESHOLD LEFT");
                testo.setText("SIDESHIFT LEFT Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;

            case 8:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(maxSpeedSS_A));
                funzione.setTextColor(Color.RED);
                funzione.setText("BLADE SIDESHIFT MAX SPEED LEFT");
                testo.setText("SIDESHIFT LEFT MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");
                break;

            case 11:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(minSpeedSS_B));
                funzione.setTextColor(Color.BLUE);
                funzione.setText("BLADE SIDESHIFT THRESHOLD RIGHT");
                testo.setText("SIDESHIFT RIGHT Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");
                break;

            case 10:
                valore.setVisibility(TextView.VISIBLE);
                valore.setText(String.valueOf(maxSpeedSS_B));
                funzione.setTextColor(Color.RED);
                funzione.setText("BLADE SIDESHIFT MAX SPEED RIGHT");
                testo.setText("SIDESHIFT RIGHT MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");
                break;
            case 12:
                valore.setVisibility(TextView.INVISIBLE);
                funzione.setTextColor(Color.BLACK);
                funzione.setText("CAN DATA MAP");
                testo.setText("Dozer: " + CanService.JD_Joystick + "\n" + "Grader: " + CanService.JD_GP_Joystyck);

                break;
        }

        if(JD_Connected){
            ECUCONN.setImageTintList(getColorStateList(R.color.green));
        }else {
            ECUCONN.setImageTintList(getColorStateList(R.color.red));
        }
    }

    private void executeMenuAction_M() {
        switch (voceMenu) {
            case 1:
                if (minSpeedLeftUP > 0) minSpeedLeftUP--;
                break;
            case 0:
                if (maxSpeedLeftUP > 0) maxSpeedLeftUP--;
                break;
            case 3:
                if (minSpeedLeftDW > 0) minSpeedLeftDW--;
                break;
            case 2:
                if (maxSpeedLeftDW > 0) maxSpeedLeftDW--;
                break;
            case 5:
                if (minSpeedRightUP > 0) minSpeedRightUP--;
                break;
            case 4:
                if (maxSpeedRightUP > 0) maxSpeedRightUP--;
                break;
            case 7:
                if (minSpeedRightDW > 0) minSpeedRightDW--;
                break;
            case 6:
                if (maxSpeedRightDW > 0) maxSpeedRightDW--;
                break;
            case 9:
                if (minSpeedSS_A > 0) minSpeedSS_A--;
                break;
            case 8:
                if (maxSpeedSS_A > 0) maxSpeedSS_A--;
                break;
            case 11:
                if (minSpeedSS_B > 0) minSpeedSS_B--;
                break;
            case 10:
                if (maxSpeedSS_B > 0) maxSpeedSS_B--;
                break;

            default:
                break;
        }
    }

    private void executeMenuAction_P() {
        switch (voceMenu) {
            case 1:
                if (minSpeedLeftUP < 255) minSpeedLeftUP++;
                break;
            case 0:
                if (maxSpeedLeftUP < 255) maxSpeedLeftUP++;
                break;
            case 3:
                if (minSpeedLeftDW < 255) minSpeedLeftDW++;
                break;
            case 2:
                if (maxSpeedLeftDW < 255) maxSpeedLeftDW++;
                break;
            case 5:
                if (minSpeedRightUP < 255) minSpeedRightUP++;
                break;
            case 4:
                if (maxSpeedRightUP < 255) maxSpeedRightUP++;
                break;
            case 7:
                if (minSpeedRightDW < 255) minSpeedRightDW++;
                break;
            case 6:
                if (maxSpeedRightDW < 255) maxSpeedRightDW++;
                break;
            case 9:
                if (minSpeedSS_A < 255) minSpeedSS_A++;
                break;
            case 8:
                if (maxSpeedSS_A < 255) maxSpeedSS_A++;
                break;
            case 11:
                if (minSpeedSS_B < 255) minSpeedSS_B++;
                break;
            case 10:
                if (maxSpeedSS_B < 255) maxSpeedSS_B++;
                break;

            default:
                break;
        }
    }

    private void sendMsg() {
        sendRunnable = new Runnable() {
            @Override
            public void run() {
                byte[]valoreSX= new byte[]{0x4E,0x20};
                byte[]valoreDX= new byte[]{0x4E,0x20};
                byte[]valoreSS= new byte[]{0x4E,0x20};

                int resultL,resultR,resultSS;
                if(DataSaved.REVERSE_LEFT==1){
                    resultL=40000-valueLEFT;
                }else {
                    resultL=valueLEFT;
                }
                if(DataSaved.REVERSE_RIGHT==1){
                    resultR=40000-valueRIGHT;
                }else {
                    resultR=valueRIGHT;
                }
                if(DataSaved.REVERSE_SS==1){
                    resultSS=40000-valueSS;
                }else {
                    resultSS=valueSS;
                }
                valoreSX= PLC_DataTypes_LittleEndian.U16_to_bytes(resultL);
                valoreDX= PLC_DataTypes_LittleEndian.U16_to_bytes(resultR);
                valoreSS= PLC_DataTypes_LittleEndian.U16_to_bytes(resultSS);
                MyDeviceManager.CanWrite(1, 0x00EFFF85, 8,
                        new byte[]{
                                (byte) 0xF2,
                                (byte) 0x1A,
                                (byte) valoreSX[0],
                                (byte) valoreSX[1],
                                (byte) valoreDX[0],
                                (byte) valoreDX[1],
                                (byte) valoreSS[0],
                                (byte) valoreSS[1]  });

                sendHandler.postDelayed(this, 50);
            }

        };
        sendHandler.postDelayed(sendRunnable, 500); // primo repeat dopo 500ms
    }
}