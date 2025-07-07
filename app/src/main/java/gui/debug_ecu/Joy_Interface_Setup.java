package gui.debug_ecu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

public class Joy_Interface_Setup extends AppCompatActivity {
    boolean press;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joy_interface_setup);
        press = false;
        findView();
        onClick();

    }

    private void findView() {
        back = findViewById(R.id.back);
    }

    private void onClick() {
        back.setOnClickListener(view -> {
            if (!press) {
                press = true;
                startActivity(new Intent(this, Hydro_Lobby.class));
                finish();
            }
        });
    }

    public void updateUI() {

    }
}