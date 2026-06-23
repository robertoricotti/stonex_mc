package gui.tech_menu;

import static packexcalib.exca.DataSaved.Delta_X_Pontone;
import static packexcalib.exca.DataSaved.Delta_Y_Pontone;
import static packexcalib.exca.DataSaved.Larghezza_Pontone;
import static packexcalib.exca.DataSaved.Lunghezza_Pontone;
import static packexcalib.exca.DataSaved.Unit_Of_Measure;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import utils.MyData;
import utils.Utils;

public class Ponton_Drg_Activity extends BaseClass {
    TextView headerr;
    ImageView save;
    EditText larghezza,altezza,deltaX,deltaY;
    ConstraintLayout panel3D;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    double tempLarg,tempAlt,tempdX,tempdY;
    int machineSelected;
    PontonTopView pontonTopView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ponton_drg);
        findView();
        init();
        onClick();
        customNumberDialog.dialog.setOnDismissListener(dialog -> {

            //aggiornare dati qui
            updateValues();

        });
        customNumberDialogFtIn.dialog.setOnDismissListener(dialog -> {


            //aggiornare dati qui
            updateValues();

        });

    }
    private void findView(){
        machineSelected = MyData.get_Int("MachineSelected");
        customNumberDialog=new CustomNumberDialog(this,-1);
        customNumberDialogFtIn=new CustomNumberDialogFtIn(this,-1);
        headerr=findViewById(R.id.headerr);
        panel3D=findViewById(R.id.panel3D);
        save=findViewById(R.id.save);
        larghezza=findViewById(R.id.larghezza);
        altezza=findViewById(R.id.altezza);
        deltaX=findViewById(R.id.deltaX);
        deltaY=findViewById(R.id.deltaY);
        pontonTopView = new PontonTopView(this);
        panel3D.addView(pontonTopView, new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        ));
    }
    private void init(){
        tempLarg= Larghezza_Pontone;
        tempAlt= Lunghezza_Pontone;
        tempdX= Delta_X_Pontone;
        tempdY= Delta_Y_Pontone;

        headerr.setText("BARGE DIMENSION "+Utils.getMetriSimbol());
        larghezza.setText(Utils.readSensorCalibration(String.valueOf(Larghezza_Pontone)));
        altezza.setText(Utils.readSensorCalibration(String.valueOf(Lunghezza_Pontone)));
        deltaX.setText(Utils.readSensorCalibration(String.valueOf(Delta_X_Pontone)));
        deltaY.setText(Utils.readSensorCalibration(String.valueOf(Delta_Y_Pontone)));
        pontonTopView.setValues(
                tempLarg,
                tempAlt,
                tempdX,
                tempdY
        );
    }
    private void onClick(){
        save.setOnClickListener(view -> {
            Larghezza_Pontone=tempLarg;
            Lunghezza_Pontone=tempAlt;
            Delta_X_Pontone=tempdX;
            Delta_Y_Pontone=tempdY;
            MyData.push("M"+machineSelected+"Larghezza_Pontone",String.valueOf(Larghezza_Pontone));
            MyData.push("M"+machineSelected+"Lunghezza_Pontone",String.valueOf(Lunghezza_Pontone));
            MyData.push("M"+machineSelected+"Delta_X_Pontone",String.valueOf(Delta_X_Pontone));
            MyData.push("M"+machineSelected+"Delta_Y_Pontone",String.valueOf(Delta_Y_Pontone));
           startActivity(new Intent(this,Nuova_Machine_Settings.class));
           finish();
        });
        altezza.setOnClickListener(view -> {
            if (Unit_Of_Measure == 4 || Unit_Of_Measure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing())
                    customNumberDialogFtIn.show(altezza);
            } else {
                if (!customNumberDialog.dialog.isShowing())
                    customNumberDialog.show(altezza);
            }
        });
        larghezza.setOnClickListener(view -> {
            if (Unit_Of_Measure == 4 || Unit_Of_Measure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing())
                    customNumberDialogFtIn.show(larghezza);
            } else {
                if (!customNumberDialog.dialog.isShowing())
                    customNumberDialog.show(larghezza);
            }
        });
        deltaX.setOnClickListener(view -> {
            if (Unit_Of_Measure == 4 || Unit_Of_Measure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing())
                    customNumberDialogFtIn.show(deltaX);
            } else {
                if (!customNumberDialog.dialog.isShowing())
                    customNumberDialog.show(deltaX);
            }
        });
        deltaY.setOnClickListener(view -> {
            if (Unit_Of_Measure == 4 || Unit_Of_Measure == 5) {
                if (!customNumberDialogFtIn.dialog.isShowing())
                    customNumberDialogFtIn.show(deltaY);
            } else {
                if (!customNumberDialog.dialog.isShowing())
                    customNumberDialog.show(deltaY);
            }
        });
    }

    private void updateValues(){
        try {
            tempLarg=Double.parseDouble(Utils.writeMetri(larghezza.getText().toString()));
        } catch (NumberFormatException e) {
            new CustomToast(Ponton_Drg_Activity.this,e.getMessage()).show_error();
        }
        try {
            tempAlt=Double.parseDouble(Utils.writeMetri(altezza.getText().toString()));
        } catch (NumberFormatException e) {
            new CustomToast(Ponton_Drg_Activity.this,e.getMessage()).show_error();
        }
        try {
            tempdX=Double.parseDouble(Utils.writeMetri(deltaX.getText().toString()));
        } catch (NumberFormatException e) {
            new CustomToast(Ponton_Drg_Activity.this,e.getMessage()).show_error();
        }
        try {
            tempdY=Double.parseDouble(Utils.writeMetri(deltaY.getText().toString()));
        } catch (NumberFormatException e) {
            new CustomToast(Ponton_Drg_Activity.this,e.getMessage()).show_error();
        }
        pontonTopView.setValues(
                tempLarg,
                tempAlt,
                tempdX,
                tempdY
        );
    }


}