package hydro;

import static packexcalib.exca.DataSaved.*;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;
import utils.MyData;

public class CAT_SEA_Activity extends AppCompatActivity {
    Handler handler = new Handler();
    Runnable repeater;
    int voceMenu, indexMachine;
    ImageView back, menuP, menuM,valM,valP;
    TextView testValve, testo, funzione,tipo;
    int maxMenu;
    EditText valore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_sea);
        maxMenu = 12;
        findView();
        onClick();
        updateUI();
        voceMenu = 0;
        try {
            indexMachine = MyData.get_Int("MachineSelected");
        } catch (Exception e) {
            indexMachine = 0;
        }

    }

    private void findView() {
        back = findViewById(R.id.btn_1);
        menuP = findViewById(R.id.toright);
        menuM = findViewById(R.id.toleft);
        testValve = findViewById(R.id.test);
        funzione = findViewById(R.id.funzione);
        testo = findViewById(R.id.testo);
        tipo=findViewById(R.id.txtTipo);
        valore=findViewById(R.id.valore);
        valM=findViewById(R.id.val_M);
        valP=findViewById(R.id.val_P);
    }

    private void onClick() {
        testValve.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(Color.TRANSPARENT); // trasparente quando premuto
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setBackgroundResource(R.drawable.sfondo_bottone_selezionato); // ripristina
                    break;
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
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // interrompi il loop
                    handler.removeCallbacks(repeater);
                    break;
            }
            return true; // consumiamo l'evento
        });


        valM.setOnClickListener(view -> {
            switch (voceMenu) {
                case 0:
                    if(minSpeedLeftUP>0)
                        minSpeedLeftUP--;
                    break;
                case 1:
                    if(maxSpeedLeftUP>0)
                        maxSpeedLeftUP--;
                    break;
                case 2:
                    if(minSpeedLeftDW>0)
                        minSpeedLeftDW--;
                    break;
                case 3:
                    if(maxSpeedLeftDW>0)
                        maxSpeedLeftDW--;
                    break;
                case 4:
                    if(minSpeedRightUP>0)
                        minSpeedRightUP--;
                    break;
                case 5:
                    if(maxSpeedRightUP>0)
                        maxSpeedRightUP--;
                    break;
                case 6:
                    if(minSpeedRightDW>0)
                        minSpeedRightDW--;
                    break;
                case 7:
                    if(maxSpeedRightDW>0)
                        maxSpeedRightDW--;
                    break;
                case 8:
                    if(minSpeedSS_A>0)
                        minSpeedSS_A--;
                    break;
                case 9:
                    if(maxSpeedSS_A>0)
                        maxSpeedSS_A--;
                    break;
                case 10:
                    if(minSpeedSS_B>0)
                        minSpeedSS_B--;
                    break;
                case 11:
                    if(maxSpeedSS_B>0)
                        maxSpeedSS_B--;
                    break;
                case 12:
                    CAT_Type--;
                    CAT_Type=Math.abs(CAT_Type)%3;
                    MyData.push("CAT_Type",String.valueOf(CAT_Type));
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
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // interrompi il loop
                    handler.removeCallbacks(repeater);
                    break;
            }
            return true; // consumiamo l'evento
        });
        valP.setOnClickListener(view -> {
            switch (voceMenu) {
                case 0:
                    if(minSpeedLeftUP<255)
                        minSpeedLeftUP++;
                    break;
                case 1:
                    if(maxSpeedLeftUP<255)
                        maxSpeedLeftUP++;
                    break;
                case 2:
                    if(minSpeedLeftDW<255)
                        minSpeedLeftDW++;
                    break;
                case 3:
                    if(maxSpeedLeftDW<255)
                        maxSpeedLeftDW++;
                    break;
                case 4:
                    if(minSpeedRightUP<255)
                        minSpeedRightUP++;
                    break;
                case 5:
                    if(maxSpeedRightUP<255)
                        maxSpeedRightUP++;
                    break;
                case 6:
                    if(minSpeedRightDW<255)
                        minSpeedRightDW++;
                    break;
                case 7:
                    if(maxSpeedRightDW<255)
                        maxSpeedRightDW++;
                    break;
                case 8:
                    if(minSpeedSS_A<255)
                        minSpeedSS_A++;
                    break;
                case 9:
                    if(maxSpeedSS_A<255)
                        maxSpeedSS_A++;
                    break;
                case 10:
                    if(minSpeedSS_B<255)
                        minSpeedSS_B++;
                    break;
                case 11:
                    if(maxSpeedSS_B<255)
                        maxSpeedSS_B++;
                    break;
                case 12:
                    CAT_Type++;
                    CAT_Type=Math.abs(CAT_Type)%3;
                    MyData.push("CAT_Type",String.valueOf(CAT_Type));
                    break;
                default:
                    break;
            }

        });

        menuM.setOnClickListener(view -> {
            if (voceMenu > 0) {
                voceMenu--;
                voceMenu = Math.abs(voceMenu) % maxMenu;
            }


        });
        menuP.setOnClickListener(view -> {
            voceMenu++;
            voceMenu = Math.abs(voceMenu) % maxMenu;

        });
        tipo.setOnClickListener(view -> {
            CAT_Type++;
            CAT_Type=Math.abs(CAT_Type)%3;
            MyData.push("CAT_Type",String.valueOf(CAT_Type));
        });
        back.setOnClickListener(view -> {
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.exit));
            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton("YES", (dialog, which) -> {
                back.setEnabled(false);
                startActivity(new Intent(CAT_SEA_Activity.this, Hydro_Activity_Entering.class));
                finish();
            });
            // Aggiungi il pulsante "No"
            builder.setNegativeButton("NO", (dialog, which) -> {
                //do nothing
            });
            // Mostra il dialog
            builder.show();


        });
    }

    public void updateUI() {
        switch (CAT_Type){
            case 0:
                tipo.setText("CAT K - N Series");
                break;
            case 1:
                tipo.setText("CAT NextGen");
                break;

            case 2:
                tipo.setText("CAT M Series");
                break;
        }

        switch (voceMenu) {
            case 0:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.BLUE);
                funzione.setText("LEFT MIN SPEED UP");
                testo.setText("LEFT RISE Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;

            case 1:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.RED);
                funzione.setText("LEFT MAX SPEED UP");
                testo.setText("LEFT RISE MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");


                break;

            case 2:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.BLUE);
                funzione.setText("LEFT MIN SPEED DOWN");
                testo.setText("LEFT LOWER Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;
            case 3:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.RED);
                funzione.setText("LEFT MAX SPEED DOWN");
                testo.setText("LEFT LOWER MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");

                break;

            case 4:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.BLUE);
                funzione.setText("RIGHT MIN SPEED UP");
                testo.setText("RIGHT RISE Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;

            case 5:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.RED);
                funzione.setText("RIGHT MAX SPEED UP");
                testo.setText("RIGHT RISE MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");
                break;

            case 6:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.BLUE);
                funzione.setText("RIGHT MIN SPEED DOWN");
                testo.setText("RIGHT LOWER Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");
                break;

            case 7:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.RED);
                funzione.setText("RIGHT MAX SPEED DOWN");
                testo.setText("RIGHT LOWER MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");
                break;

            case 8:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.BLUE);
                funzione.setText("BLADE SIDESHIFT MIN SPEED LEFT");
                testo.setText("SIDESHIFT LEFT Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;

            case 9:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.RED);
                funzione.setText("BLADE SIDESHIFT MAX SPEED LEFT");
                testo.setText("SIDESHIFT LEFT MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "MAX Speed must be higer than MIN Speed");
                break;

            case 10:
                valore.setVisibility(TextView.VISIBLE);
                funzione.setTextColor(Color.BLUE);
                funzione.setText("BLADE SIDESHIFT MIN SPEED RIGHT");
                testo.setText("SIDESHIFT RIGHT Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");
                break;

            case 11:
                valore.setVisibility(TextView.VISIBLE);
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
                funzione.setText("MACHINE MODEL");
                switch (CAT_Type){
                    case 0:
                        testo.setText("CAT K - N Series");
                        break;
                    case 1:
                        testo.setText("CAT NextGen");
                        break;

                    case 2:
                        testo.setText("CAT M Series");
                        break;
                }
                break;
        }

    }
    private void executeMenuAction_M() {
        switch (voceMenu) {
            case 0:
                if (minSpeedLeftUP > 0) minSpeedLeftUP--;
                break;
            case 1:
                if (maxSpeedLeftUP > 0) maxSpeedLeftUP--;
                break;
            case 2:
                if (minSpeedLeftDW > 0) minSpeedLeftDW--;
                break;
            case 3:
                if (maxSpeedLeftDW > 0) maxSpeedLeftDW--;
                break;
            case 4:
                if (minSpeedRightUP > 0) minSpeedRightUP--;
                break;
            case 5:
                if (maxSpeedRightUP > 0) maxSpeedRightUP--;
                break;
            case 6:
                if (minSpeedRightDW > 0) minSpeedRightDW--;
                break;
            case 7:
                if (maxSpeedRightDW > 0) maxSpeedRightDW--;
                break;
            case 8:
                if (minSpeedSS_A > 0) minSpeedSS_A--;
                break;
            case 9:
                if (maxSpeedSS_A > 0) maxSpeedSS_A--;
                break;
            case 10:
                if (minSpeedSS_B > 0) minSpeedSS_B--;
                break;
            case 11:
                if (maxSpeedSS_B > 0) maxSpeedSS_B--;
                break;
            case 12:
                CAT_Type--;
                CAT_Type = Math.abs(CAT_Type) % 3;
                MyData.push("CAT_Type", String.valueOf(CAT_Type));
                break;
            default:
                break;
        }
    }

    private void executeMenuAction_P() {
        switch (voceMenu) {
            case 0:
                if (minSpeedLeftUP <255) minSpeedLeftUP++;
                break;
            case 1:
                if (maxSpeedLeftUP <255) maxSpeedLeftUP++;
                break;
            case 2:
                if (minSpeedLeftDW <255) minSpeedLeftDW++;
                break;
            case 3:
                if (maxSpeedLeftDW <255) maxSpeedLeftDW++;
                break;
            case 4:
                if (minSpeedRightUP <255) minSpeedRightUP++;
                break;
            case 5:
                if (maxSpeedRightUP <255) maxSpeedRightUP++;
                break;
            case 6:
                if (minSpeedRightDW <255) minSpeedRightDW++;
                break;
            case 7:
                if (maxSpeedRightDW <255) maxSpeedRightDW++;
                break;
            case 8:
                if (minSpeedSS_A <255) minSpeedSS_A++;
                break;
            case 9:
                if (maxSpeedSS_A <255) maxSpeedSS_A++;
                break;
            case 10:
                if (minSpeedSS_B <255) minSpeedSS_B++;
                break;
            case 11:
                if (maxSpeedSS_B <255) maxSpeedSS_B++;
                break;
            case 12:
                CAT_Type++;
                CAT_Type = Math.abs(CAT_Type) % 3;
                MyData.push("CAT_Type", String.valueOf(CAT_Type));
                break;
            default:
                break;
        }
    }


}