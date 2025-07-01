package gui.boot_and_choose;

import static gui.MyApp.KEY_LEVEL;
import static services.ReadProjectService.numbers;
import static services.UpdateValuesService.startedService;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CloseAppDialog;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
import services.UpdateValuesService;
import utils.LanguageSetter;
import utils.MyData;

public class Activity_Home_Page extends AppCompatActivity {
    ImageView close, toDig;
    CloseAppDialog closeAppDialog;
    ProgressBar progressBar;
    TextView stringsStat, titolo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            LanguageSetter.setLocale(this, MyData.get_String("language"));
        } catch (Exception e) {
            MyData.push("language", "en_GB");
        }
        setContentView(R.layout.activity_dig_menu);
        if (!startedService) {
            startService(new Intent(this, UpdateValuesService.class));

        }
        LanguageSetter.setLocale(this, MyData.get_String("language"));
        setContentView(R.layout.activity_home_page);
        findView();
        onClick();

    }

    private void findView() {
        closeAppDialog = new CloseAppDialog(this);
        close = findViewById(R.id.btn_1);
        progressBar = findViewById(R.id.progressBar);
        stringsStat = findViewById(R.id.stringastat);
        toDig = findViewById(R.id.img_7);
        titolo = findViewById(R.id.txtProject);
        try {
            String s = MyData.get_String("progettoSelected");
            s = s.replace("/storage/emulated/0/StonexMachineControl", "");
            s = s.substring(0, s.lastIndexOf("/"));
            titolo.setText(s);
        } catch (Exception e) {
            titolo.setText("SELECT A PROJECT");
        }


        progressBar.setVisibility(View.INVISIBLE);
        stringsStat.setVisibility(View.INVISIBLE);

    }

    private void onClick() {
        close.setOnClickListener(view -> {
            if (!closeAppDialog.alertDialog.isShowing()) {
                closeAppDialog.show();
            }
        });

        toDig.setOnClickListener((View v) -> {
            enableAll(false);
            if (KEY_LEVEL > 2) {
                progressBar.setVisibility(View.VISIBLE);
                stringsStat.setVisibility(View.VISIBLE);
                startService(new Intent(this, ReadProjectService.class));
            } else {
                enableAll(true);
                new CustomToast(this, "LICENSE MISSED").show_alert();
            }

        });
    }

    private void enableAll(boolean b) {
        close.setEnabled(b);
        toDig.setEnabled(b);
    }

    public void updateUI() {
        try {
            stringsStat.setText(ReadProjectService.parserStatus + "\n" + numbers + " Rows\n");
            switch (DataSaved.isWL) {
                case 0:
                    toDig.setImageResource(R.drawable.bottone_scava);
                    break;

                case 1:
                    toDig.setImageResource(R.drawable.bottone_loada);
                    break;

                case 2:
                case 3:
                    toDig.setImageResource(R.drawable.bottone_grada);
                    break;


                case 4:
                    toDig.setImageResource(R.drawable.bottone_grada);

                    break;
                case 10:
                    toDig.setImageResource(R.drawable.bottone_drilla);

                    break;

            }
        } catch (Exception ignored) {

        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}