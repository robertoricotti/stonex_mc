package gui.tech_menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import services.UpdateValuesService;
import utils.MyData;
import utils.Utils;

public class Tilt_Rot_Activity extends BaseClass {
    ImageView save,exit;
    TextView txtDeltaFW,txtDeltaDW;
    EditText deltaFW,deltaDW;
    int indexMachineSelected,indexMeasure;
    CustomNumberDialog numberDialog;
    CustomNumberDialogFtIn numberDialogFtIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilt_rot);
        findView();
        init();
        onClick();
        updateUI();

    }
    private void findView(){

        save=findViewById(R.id.save);
        exit=findViewById(R.id.exit);
        txtDeltaFW=findViewById(R.id.txtDeltaFW);
        txtDeltaDW=findViewById(R.id.txtDeltaDW);
        deltaFW=findViewById(R.id.deltaFW);
        deltaDW=findViewById(R.id.deltaDW);

    }
    private void init(){
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexMeasure = MyData.get_Int("Unit_Of_Measure");
        if (indexMeasure == 4 || indexMeasure == 5) {
            numberDialogFtIn = new CustomNumberDialogFtIn(this, -1);
        } else {
            numberDialog = new CustomNumberDialog(this, -1);
        }

    }
    private void onClick(){
        save.setOnClickListener(view -> {
            save.setEnabled(false);
                try {
                    MyData.push("M" + indexMachineSelected + "Offset_Engcon_Forward", Utils.writeMetri(deltaFW.getText().toString()));

                } catch (Exception ignored) {

                }

            try {
                MyData.push("M" + indexMachineSelected + "Offset_Engcon_Down", Utils.writeMetri(deltaDW.getText().toString()));

            } catch (Exception ignored) {
            }

            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
            new CustomToast(Tilt_Rot_Activity.this,"Saved!").show_added();
        });
        exit.setOnClickListener(view -> {
            exit.setEnabled(false);
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });

        deltaFW.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(deltaFW);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(deltaFW);
            }

        });
        deltaDW.setOnClickListener((View v) -> {
            if (indexMeasure == 4 || indexMeasure == 5) {
                if (!numberDialogFtIn.dialog.isShowing())
                    numberDialogFtIn.show(deltaDW);
            } else {
                if (!numberDialog.dialog.isShowing())
                    numberDialog.show(deltaDW);
            }

        });

    }
    private void updateUI(){
        txtDeltaFW.setText("Roto\nΔ-FWD "+ Utils.getMetriSimbol());
        txtDeltaDW.setText("Roto\nΔ-DOWN "+Utils.getMetriSimbol());
        deltaFW.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "Offset_Engcon_Forward")));
        deltaDW.setText(Utils.readSensorCalibration(MyData.get_String("M" + indexMachineSelected + "Offset_Engcon_Down")));
    }



}