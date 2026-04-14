package drill_pile.gui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.dialogs_and_toast.Dialog_CanBaud;

public class Ecu_Sensors_Activity extends BaseClass {
    Dialog_CanBaud dialogCanBaud;
    ImageView back, can1, can2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecu_sensors);
        findView();
        onClick();
    }

    private void findView() {
        back = findViewById(R.id.back);
        can1 = findViewById(R.id.can1setup);
        can2 = findViewById(R.id.can2setup);
        dialogCanBaud = new Dialog_CanBaud(this);
    }

    private void onClick() {
        back.setOnClickListener(view -> {
            startActivity(new Intent(this, Drill_MainPage.class));
            overridePendingTransition(0, 0);
            finish();
        });
        can1.setOnClickListener(view -> {
            if (!dialogCanBaud.dialog.isShowing()) {
                dialogCanBaud.show(1);
            }
        });
        can2.setOnClickListener(view -> {
            if (!dialogCanBaud.dialog.isShowing()) {
                dialogCanBaud.show(2);
            }
        });
    }

    public void updateUI() {

    }
}