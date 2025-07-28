package gui.boot_and_choose;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stx_dig.R;

import utils.MyDeviceManager;

public class LicenseFail_Activity extends AppCompatActivity {
    ImageView reload;
    TextView messaggio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_fail);

        reload=findViewById(R.id.reload);
        messaggio=findViewById(R.id.messaggio);

        messaggio.setText("Device S/N: "+MyDeviceManager.getDeviceSN(this)+"\n"+
                "Device MAC: "+MyDeviceManager.getMacAddress(this)+"\n"+
                getString(R.string.error_license));

        reload.setOnClickListener(view -> {
            startActivity(new Intent(this,LaunchScreenActivity.class));
            finish();
        });
    }
}