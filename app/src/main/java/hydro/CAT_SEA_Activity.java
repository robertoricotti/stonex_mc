package hydro;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomNumberDialog;
import packexcalib.exca.DataSaved;
import utils.MyData;

public class CAT_SEA_Activity extends AppCompatActivity {
    int voceMenu, indexMachine;
    ImageView back, menuP, menuM;
    TextView testValve, testo, funzione,tipo;
    int maxMenu;
    CustomNumberDialog customNumberDialog;
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
        customNumberDialog=new CustomNumberDialog(this,10);
        back = findViewById(R.id.btn_1);
        menuP = findViewById(R.id.toright);
        menuM = findViewById(R.id.toleft);
        testValve = findViewById(R.id.test);
        funzione = findViewById(R.id.funzione);
        testo = findViewById(R.id.testo);
        tipo=findViewById(R.id.txtTipo);
        valore=findViewById(R.id.valore);
    }

    private void onClick() {
        valore.setOnClickListener(view -> {//TODO finire CAT
            if(!customNumberDialog.dialog.isShowing()){
                customNumberDialog.show(valore);
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
            DataSaved.CAT_Type++;
            DataSaved.CAT_Type=Math.abs(DataSaved.CAT_Type)%3;
            MyData.push("CAT_Type",String.valueOf(DataSaved.CAT_Type));
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
        switch (DataSaved.CAT_Type){
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
                funzione.setTextColor(Color.BLUE);
                funzione.setText("LEFT MIN SPEED UP");
                testo.setText("LEFT RISE Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;

            case 1:
                funzione.setTextColor(Color.RED);
                funzione.setText("LEFT MAX SPEED UP");
                testo.setText("LEFT RISE MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "until the blade moves as a manual operator's rise");


                break;

            case 2:
                funzione.setTextColor(Color.BLUE);
                funzione.setText("LEFT MIN SPEED DOWN");
                testo.setText("LEFT LOWER Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;
            case 3:
                funzione.setTextColor(Color.RED);
                funzione.setText("LEFT MAX SPEED DOWN");
                testo.setText("LEFT LOWER MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "until the blade moves as a manual operator's lower");

                break;

            case 4:
                funzione.setTextColor(Color.BLUE);
                funzione.setText("RIGHT MIN SPEED UP");
                testo.setText("RIGHT RISE Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;

            case 5:
                funzione.setTextColor(Color.RED);
                funzione.setText("RIGHT MAX SPEED UP");
                testo.setText("RIGHT RISE MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "until the blade moves as a manual operator's rise");
                break;

            case 6:
                funzione.setTextColor(Color.BLUE);
                funzione.setText("RIGHT MIN SPEED DOWN");
                testo.setText("RIGHT LOWER Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");
                break;

            case 7:
                funzione.setTextColor(Color.RED);
                funzione.setText("RIGHT MAX SPEED DOWN");
                testo.setText("RIGHT LOWER MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "until the blade moves as a manual operator's lower");
                break;

            case 8:
                funzione.setTextColor(Color.BLUE);
                funzione.setText("BLADE SIDESHIFT MIN SPEED LEFT");
                testo.setText("SIDESHIFT LEFT Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");

                break;

            case 9:
                funzione.setTextColor(Color.RED);
                funzione.setText("BLADE SIDESHIFT MAX SPEED LEFT");
                testo.setText("SIDESHIFT LEFT MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "until the blade moves as a manual operator's sideshift");
                break;

            case 10:
                funzione.setTextColor(Color.BLUE);
                funzione.setText("BLADE SIDESHIFT MIN SPEED RIGHT");
                testo.setText("SIDESHIFT RIGHT Minimum Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Increase the Value by 1 and press TEST\n until the cylinder starts moving slowly.\n" +
                        "0= NO MOVE");
                break;

            case 11:
                funzione.setTextColor(Color.RED);
                funzione.setText("BLADE SIDESHIFT MAX SPEED RIGHT");
                testo.setText("SIDESHIFT RIGHT MAX Hydraulic Speed\n" +
                        "Set the Machine to the operating rpm\n" +
                        "Set the Value and press TEST\n " +
                        "until the blade moves as a manual operator's sideshift");
                break;
        }

    }


}