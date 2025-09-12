package gui.boot_and_choose;

import static gui.MyApp.folderPath;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stx_dig.R;

import java.io.File;
import java.time.Duration;

import utils.MyDeviceManager;

public class LicenseFail_Activity extends AppCompatActivity {
    ImageView reload,erase;
    TextView messaggio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_fail);

        reload=findViewById(R.id.reload);
        erase=findViewById(R.id.erase);
        messaggio=findViewById(R.id.messaggio);

        messaggio.setText("Device S/N: "+MyDeviceManager.getDeviceSN(this)+"\n"+
                "Device MAC: "+MyDeviceManager.getMacAddress(this)+"\n"+
                getString(R.string.error_license));
        /*erase.setOnClickListener(view -> {
            try {
                // Percorso della cartella
                String pathL = Environment.getExternalStorageDirectory().toString() + folderPath + "/Config/License.json";

                // File di output JSON
                File myFile = new File(pathL);
                myFile.delete();
            } catch (Exception e) {
                Toast.makeText(LicenseFail_Activity.this,e.toString(), Toast.LENGTH_SHORT).show();

            }
        });*/

        reload.setOnClickListener(view -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            startActivity(new Intent(this, LaunchScreenActivity.class));
            finish();
        });
    }
}