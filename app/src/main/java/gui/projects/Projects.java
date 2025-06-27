package gui.projects;

import static gui.MyApp.KEY_LEVEL;
import static gui.MyApp.folderPath;
import static gui.MyApp.isApollo;
import static packexcalib.exca.DataSaved.gpsOk;
import static packexcalib.gnss.CRS_Strings._2100;
import static packexcalib.gnss.CRS_Strings._28992;
import static packexcalib.gnss.CRS_Strings._31370;
import static packexcalib.gnss.CRS_Strings._UTM;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.ExcavatorMenuActivity;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Diaalog_Set_SP;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.digging_excavator.Digging1D;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.DiggingProfile;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
import utils.MyData;

public class Projects extends BaseClass {

    ImageView createPlan, createAB, createTriangles, createArea, createTrench;
    ImageButton epsg;
    ImageButton load, usb, remotes;
    TextView projectTitle;
    ImageView back, toDig, gpsS;
    String replaceStr = Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath + "/Projects/";
    TextView txtCrs;
    ProgressBar progressBar;
    Diaalog_Set_SP diaalogSetSp;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        findView();
        init();
        onClick();
        updateUI();
    }

    private void findView() {
        back = findViewById(R.id.back);
        toDig = findViewById(R.id.toDig);
        load = findViewById(R.id.load);
        createPlan = findViewById(R.id.projectPlan);
        createAB = findViewById(R.id.projectAB);
        createTriangles = findViewById(R.id.triangola);
        createTrench = findViewById(R.id.to_trench);
        epsg = findViewById(R.id.crs);
        projectTitle = findViewById(R.id.projectTitle);
        usb = findViewById(R.id.usb);
        txtCrs = findViewById(R.id.txtCrs);
        createArea = findViewById(R.id.area);
        progressBar = findViewById(R.id.progressBar);
        remotes = findViewById(R.id.remoteS);
        diaalogSetSp = new Diaalog_Set_SP(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);

        remotes.setAlpha(0.3f);
        gpsS = findViewById(R.id.gpsS);


    }


    @SuppressLint("SetTextI18n")
    private void init() {

        if (isApollo) {
            usb.setVisibility(View.VISIBLE);
        } else {
            usb.setVisibility(View.INVISIBLE);
        }
        progressBar.setVisibility(View.INVISIBLE);
        try {
            String strProj = MyData.get_String("progettoSelected").replace(replaceStr, "");
            strProj = strProj.replace("#AB_#", "").replace("#1P_#", "").replace("#CS_#", "").replace("#AR_#", "");
            String s = getString(R.string.project);
            projectTitle.setText((strProj.equals("") ? "" : " ( " + strProj + " )"));
        } catch (Exception e) {

        }


        if (MyData.get_String("crs").equals(_UTM)) {
            txtCrs.setText(" " + _UTM + " ");

        } else if (MyData.get_String("crs").equals("2100")) {
            txtCrs.setText(_2100);
        } else if (MyData.get_String("crs").equals("28992")) {
            txtCrs.setText(_28992);
        } else if (MyData.get_String("crs").equals("31370")) {
            txtCrs.setText(_31370);
        } else {
            txtCrs.setText(DataSaved.S_CRS);
        }
        if (DataSaved.isWL == 2) {
            toDig.setImageResource(R.drawable.go_grade);
            createPlan.setImageResource(R.drawable.piano_lama);
            createAB.setImageResource(R.drawable.ab_piano_lama);
            createArea.setImageResource(R.drawable.perimetro_lama);
        } else {
            toDig.setImageResource(R.drawable.go_dig);
            createPlan.setImageResource(R.drawable.piano_benna);
            createAB.setImageResource(R.drawable.ab_piano_benna);
            createArea.setImageResource(R.drawable.perimetro_benna);
        }


    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    private void disableAll() {
        back.setEnabled(false);
        createAB.setEnabled(false);
        createArea.setEnabled(false);
        createPlan.setEnabled(false);
        back.setEnabled(false);
        toDig.setEnabled(false);
        load.setEnabled(false);
        usb.setEnabled(false);
        remotes.setEnabled(false);
        createTrench.setEnabled(false);
        gpsS.setEnabled(false);

    }

    private void enableAll() {
        back.setEnabled(true);
        createAB.setEnabled(true);
        createArea.setEnabled(true);
        createPlan.setEnabled(true);
        back.setEnabled(true);
        toDig.setEnabled(true);
        load.setEnabled(true);
        usb.setEnabled(true);
        remotes.setEnabled(true);
        createTrench.setEnabled(true);
        gpsS.setEnabled(true);

    }
    public void updateUI(){
        try {
            if (gpsOk) {
                gpsS.setImageResource(R.drawable.gps_si);
            } else {
                gpsS.setImageResource(R.drawable.gps_no);
            }
        } catch (Exception e) {

        }

    }

    private void onClick() {
        gpsS.setOnClickListener(view -> {
            if(!dialogGnssCoordinates.alertDialog.isShowing()){
                dialogGnssCoordinates.show();
            }

        });
        createTrench.setOnClickListener(view -> {
            disableAll();
           showCustomTrench();


        });
        remotes.setOnClickListener(view -> {
            new CustomToast(this, "NOT IMPLEMENTED").show();
            /*   disableAll();
            Intent intent = new Intent(this, Remote_Activity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();*/


        });

        createAB.setOnClickListener((View v) -> {

            disableAll();
            showCustomDialog_AB();

        });
        createArea.setOnClickListener(view -> {
            disableAll();
            showCustomDialog_AREA();
        });

        createTriangles.setOnClickListener((View v) -> {
            disableAll();
            showCustomDialog_TRI();

        });

        createPlan.setOnClickListener((View v) -> {
            disableAll();

            showCustomDialog_1P();
        });

        back.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, ExcavatorMenuActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        toDig.setOnClickListener((View v) -> {
            disableAll();
            int profile = MyData.get_Int("ProfileSelected");
            int typeView = MyData.get_Int("indexView");
            if (profile == 0) {
                switch (typeView) {
                    case 0:
                        if (KEY_LEVEL > 0) {
                            startActivity(new Intent(this, Digging1D.class));
                            overridePendingTransition(0, 0);
                            finish();
                        } else {
                            enableAll();
                            new CustomToast(this, "LICENSE MISSED").show_alert();
                        }
                        break;
                    case 1:
                        if (KEY_LEVEL > 1) {
                            startActivity(new Intent(this, Digging2D.class));
                            overridePendingTransition(0, 0);
                            finish();
                        } else {
                            enableAll();
                            new CustomToast(this, "LICENSE MISSED").show_alert();
                        }
                        break;
                    case 2:
                    case 3:
                        if (KEY_LEVEL > 2) {
                            progressBar.setVisibility(View.VISIBLE);
                            startService(new Intent(this, ReadProjectService.class));
                        } else {
                            enableAll();
                            new CustomToast(this, "LICENSE MISSED").show_alert();
                        }
                        break;
                }
            } else {
                startActivity(new Intent(this, DiggingProfile.class));
                overridePendingTransition(0, 0);
                finish();
            }


        });


        load.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, PickProject.class));
            overridePendingTransition(0, 0);
            finish();
        });

        epsg.setOnClickListener((View v) -> {

            if (!diaalogSetSp.dialog.isShowing()) {
                diaalogSetSp.show();
            }


        });

        usb.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, Usb_Project_Nova.class));
            overridePendingTransition(0, 0);
            finish();
        });

    }

    private void showCustomDialog_AB() {
        // Crea la dialog
        Dialog dialog = new Dialog(this);

        // Imposta il layout personalizzato
        dialog.setContentView(R.layout.dialog_add_surface); // Sostituisci con il nome effettivo del file XML
        dialog.setCancelable(false);
        // Imposta che la dialog sia a schermo intero (opzionale)
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Trova i bottoni all'interno della Dialog
        Button btnYes = dialog.findViewById(R.id.add_new);
        Button btnNo = dialog.findViewById(R.id.create_new);
        Button btnClose = dialog.findViewById(R.id.esci);

        // Gestisci il click di YES
        btnYes.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "AB");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di NO
        btnNo.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "AB");
            intent.putExtra("type", "NEW"); // Passa il valore NEW
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di CLOSE
        btnClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    enableAll();
                }
        );

        // Mostra la dialog
        dialog.show();
    }

    private void showCustomDialog_1P() {
        // Crea la dialog
        Dialog dialog = new Dialog(this);

        // Imposta il layout personalizzato
        dialog.setContentView(R.layout.dialog_add_surface); // Sostituisci con il nome effettivo del file XML
        dialog.setCancelable(false);

        // Imposta che la dialog sia a schermo intero (opzionale)
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Trova i bottoni all'interno della Dialog
        Button btnYes = dialog.findViewById(R.id.add_new);
        Button btnNo = dialog.findViewById(R.id.create_new);
        Button btnClose = dialog.findViewById(R.id.esci);

        // Gestisci il click di YES
        btnYes.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "PLAN");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di NO
        btnNo.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "PLAN");
            intent.putExtra("type", "NEW"); // Passa il valore NEW
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di CLOSE
        btnClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    enableAll();
                }
        );

        // Mostra la dialog
        dialog.show();
    }
    private void showCustomTrench(){
        // Crea la dialog
        Dialog dialog = new Dialog(this);

        // Imposta il layout personalizzato
        dialog.setContentView(R.layout.dialog_add_surface); // Sostituisci con il nome effettivo del file XML
        dialog.setCancelable(false);

        // Imposta che la dialog sia a schermo intero (opzionale)
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Trova i bottoni all'interno della Dialog
        Button btnYes = dialog.findViewById(R.id.add_new);
        Button btnNo = dialog.findViewById(R.id.create_new);
        Button btnClose = dialog.findViewById(R.id.esci);

        // Gestisci il click di YES
        btnYes.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "TRENCH");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di NO
        btnNo.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "TRENCH");
            intent.putExtra("type", "NEW"); // Passa il valore NEW
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di CLOSE
        btnClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    enableAll();
                }
        );

        // Mostra la dialog
        dialog.show();
    }

    private void showCustomDialog_TRI() {
        // Crea la dialog
        Dialog dialog = new Dialog(this);

        // Imposta il layout personalizzato
        dialog.setContentView(R.layout.dialog_add_surface); // Sostituisci con il nome effettivo del file XML
        dialog.setCancelable(false);
        // Imposta che la dialog sia a schermo intero (opzionale)
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Trova i bottoni all'interno della Dialog
        Button btnYes = dialog.findViewById(R.id.add_new);
        Button btnNo = dialog.findViewById(R.id.create_new);
        Button btnClose = dialog.findViewById(R.id.esci);

        // Gestisci il click di YES
        btnYes.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "TRIANGLES");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di NO
        btnNo.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "TRIANGLES");
            intent.putExtra("type", "NEW"); // Passa il valore NEW
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di CLOSE
        btnClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    enableAll();
                }
        );

        // Mostra la dialog
        dialog.show();
    }

    private void showCustomDialog_AREA() {
        // Crea la dialog
        Dialog dialog = new Dialog(this);

        // Imposta il layout personalizzato
        dialog.setContentView(R.layout.dialog_add_surface); // Sostituisci con il nome effettivo del file XML
        dialog.setCancelable(false);
        // Imposta che la dialog sia a schermo intero (opzionale)
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Trova i bottoni all'interno della Dialog
        Button btnYes = dialog.findViewById(R.id.add_new);
        Button btnNo = dialog.findViewById(R.id.create_new);
        Button btnClose = dialog.findViewById(R.id.esci);

        // Gestisci il click di YES
        btnYes.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "AREA");
            intent.putExtra("type", "OVER"); // Passa il valore OVER
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di NO
        btnNo.setOnClickListener(v -> {
            Intent intent = new Intent(this, Activity_Crea_Superficie.class);
            intent.putExtra("proj", "AREA");
            intent.putExtra("type", "NEW"); // Passa il valore NEW
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        // Gestisci il click di CLOSE
        btnClose.setOnClickListener(v -> {
                    dialog.dismiss();
                    enableAll();
                }
        );

        // Mostra la dialog
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
