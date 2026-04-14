package gui.hydro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.example.stx_dig.R;

import gui.BaseClass;
import packexcalib.exca.DataSaved;
import utils.MyData;

public class OEM_Activity extends BaseClass {
    ImageView btn_1;
    CheckBox ckReverseMainfall, ckReverseSign, ckReverseSS;
    int indexMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oem);
        try {
            indexMachine = MyData.get_Int("MachineSelected");
        } catch (Exception e) {
            indexMachine = 0;
        }
        btn_1 = findViewById(R.id.btn_1);
        ckReverseMainfall = findViewById(R.id.ckReverseMainfall);
        ckReverseSign = findViewById(R.id.ckReverseSign);
        ckReverseSS = findViewById(R.id.ckReverseSS);
        init();

        onClick();
    }

    private void init() {
        ckReverseMainfall.setChecked(DataSaved.OEM_REV_MAINFALL == 1);
        ckReverseSign.setChecked(DataSaved.OEM_REV_UPDW == 1);
        ckReverseSS.setChecked(DataSaved.OEM_REV_SS == 1);


    }

    private void onClick() {
        ckReverseMainfall.setOnClickListener(view -> {
            DataSaved.OEM_REV_MAINFALL++;
            DataSaved.OEM_REV_MAINFALL = DataSaved.OEM_REV_MAINFALL % 2;
            init();
            MyData.push("M" + indexMachine + "OEM_REV_MAINFALL", String.valueOf(DataSaved.OEM_REV_MAINFALL));

        });
        ckReverseSign.setOnClickListener(view -> {
            DataSaved.OEM_REV_UPDW++;
            DataSaved.OEM_REV_UPDW = DataSaved.OEM_REV_UPDW % 2;
            init();
            MyData.push("M" + indexMachine + "OEM_REV_UPDW", String.valueOf(DataSaved.OEM_REV_UPDW));

        });
        ckReverseSS.setOnClickListener(view -> {
            DataSaved.OEM_REV_SS++;
            DataSaved.OEM_REV_SS = DataSaved.OEM_REV_SS % 2;
            init();
            MyData.push("M" + indexMachine + "OEM_REV_SS", String.valueOf(DataSaved.OEM_REV_SS));

        });
        btn_1.setOnClickListener(view -> {
            startActivity(new Intent(this, Hydro_Activity_Entering.class));
            finish();
        });
    }
}