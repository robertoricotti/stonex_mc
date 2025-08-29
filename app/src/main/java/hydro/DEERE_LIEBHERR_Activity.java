package hydro;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stx_dig.R;

public class DEERE_LIEBHERR_Activity extends AppCompatActivity {
    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deere_liebherr);
        findView();
        onClick();
        updateUI();
    }

    private void findView(){
        back=findViewById(R.id.btn_1);
    }
    private void onClick(){
        back.setOnClickListener(view -> {
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.exit));
            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton("YES", (dialog, which) -> {
                back.setEnabled(false);
                startActivity(new Intent(DEERE_LIEBHERR_Activity.this,Hydro_Activity_Entering.class));
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
    public void updateUI(){

    }
}