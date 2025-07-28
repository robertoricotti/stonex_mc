package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dxf.PNEZDPoint;
import utils.FullscreenActivity;

public class Dialog_Add_Pnezd {
    Activity activity;
    public Dialog dialog;
    ImageView save,cancel,lista,removelast;
    String path="";
    String filepath;
    ScrollView customView ;
    RecyclerView recyclerView ;
    boolean showingRecycler = false;
    public Dialog_Add_Pnezd(Activity activity,String path){
        Log.w("Dialog_Add_Pnezd", "Cartella: " + path);
        this.activity=activity;
        this.path=path;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);


// Estrai nome cartella
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs(); // crea la cartella se non esiste
        }

        String folderName = folder.getName();
        File csvFile = new File(folder, folderName + ".csv");
        filepath = csvFile.getAbsolutePath();  // aggiorna path al file vero

        if (!csvFile.exists()) {
            try (FileWriter writer = new FileWriter(csvFile)) {
                writer.write("P,N,E,Z,D\n"); // intestazione
                writer.flush();
                Log.d("Dialog_Add_Pnezd", "Creato file CSV: " + csvFile.getAbsolutePath());
                filepath= csvFile.getAbsolutePath();
            } catch (IOException e) {
                Log.e("Dialog_Add_Pnezd", "Errore nella creazione del CSV: " + e.getMessage());
            }
        }


    }
    public void show(){
        dialog.create();
        dialog.setContentView(R.layout.dialog_add_pnezd);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.85);
        int height = (int) (displayMetrics.heightPixels * 0.9);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
    }
    private void findView(){
        save=dialog.findViewById(R.id.save);
        cancel=dialog.findViewById(R.id.cancel);
        lista=dialog.findViewById(R.id.lista);
        removelast=dialog.findViewById(R.id.remove);
        customView = dialog.findViewById(R.id.customViewContainer);
        recyclerView = dialog.findViewById(R.id.pointsRecyclerView);

    }
    private void onClick(){

        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        removelast.setOnClickListener(view -> {
            File file = new File(filepath);
            if (!file.exists()) {
                Log.w("Dialog_Add_Pnezd", "File CSV non esiste.");
                return;
            }

            List<String> lines = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                Log.e("Dialog_Add_Pnezd", "Errore lettura CSV: " + e.getMessage());
                return;
            }

            if (lines.size() <= 1) {
                Log.w("Dialog_Add_Pnezd", "Nessun punto da rimuovere.");
                return;
            }

            // Rimuove l'ultima riga (ma mantiene intestazione)
            lines.remove(lines.size() - 1);

            try (FileWriter writer = new FileWriter(file, false)) {
                for (String l : lines) {
                    writer.write(l + "\n");
                }
                writer.flush();
                Log.d("Dialog_Add_Pnezd", "Ultimo punto rimosso.");
            } catch (IOException e) {
                Log.e("Dialog_Add_Pnezd", "Errore scrittura CSV: " + e.getMessage());
            }

            // Se visibile, aggiorna la lista
            if (showingRecycler) {
                List<PNEZDPoint> nuoviPunti = leggiCSV(filepath);
                PNEZDAdapter adapter = new PNEZDAdapter(nuoviPunti);
                recyclerView.setAdapter(adapter);
            }
        });

        save.setOnClickListener(view -> {
            int numero = getNextPointNumber();  // ora è dinamico
            double nord = 123.456;
            double est = 456.789;
            double quota = 78.9;
            String descrizione = "Punto aggiunto manualmente";

            PNEZDPoint nuovoPunto = new PNEZDPoint(numero, nord, est, quota, descrizione);
            aggiungiPuntoAlCSV(nuovoPunto);

            if (showingRecycler) {
                List<PNEZDPoint> punti = leggiCSV(filepath);
                PNEZDAdapter adapter = new PNEZDAdapter(punti);
                recyclerView.setAdapter(adapter);
            }
        });
        lista.setOnClickListener(v -> {
            if (!showingRecycler) {
                List<PNEZDPoint> punti = leggiCSV(filepath);
                PNEZDAdapter adapter = new PNEZDAdapter(punti);
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                recyclerView.setAdapter(adapter);
            }

            recyclerView.setVisibility(showingRecycler ? View.GONE : View.VISIBLE);
            customView.setVisibility(showingRecycler ? View.VISIBLE : View.GONE);
            showingRecycler = !showingRecycler;
        });



    }
    private List<PNEZDPoint> leggiCSV(String filePath) {
        List<PNEZDPoint> punti = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            Log.e("CSV", "File non trovato: " + filePath);
            return punti;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    int pointNumber = Integer.parseInt(parts[0].trim());
                    double northing = Double.parseDouble(parts[1].trim());
                    double easting = Double.parseDouble(parts[2].trim());
                    double elevation = Double.parseDouble(parts[3].trim());
                    String description = parts[4].trim();

                    punti.add(new PNEZDPoint(pointNumber, northing, easting, elevation, description));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            Log.e("CSV", "Errore durante la lettura del CSV: " + e.getMessage());
        }

        return punti;
    }
    private int getNextPointNumber() {
        int maxNumber = 0;

        File file = new File(filepath);
        if (!file.exists()) return 1; // se non esiste ancora

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // salta intestazione P,N,E,Z,D
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 1) {
                    try {
                        int currentNumber = Integer.parseInt(parts[0].trim());
                        if (currentNumber > maxNumber) {
                            maxNumber = currentNumber;
                        }
                    } catch (NumberFormatException e) {
                        // ignora righe malformate
                    }
                }
            }
        } catch (IOException e) {
            Log.e("Dialog_Add_Pnezd", "Errore lettura per numerazione: " + e.getMessage());
        }

        return maxNumber + 1;
    }
    private void aggiungiPuntoAlCSV(PNEZDPoint punto) {
        try (FileWriter writer = new FileWriter(filepath, true)) {
            String riga = punto.getPointNumber() + "," +
                    punto.getNorthing() + "," +
                    punto.getEasting() + "," +
                    punto.getElevation() + "," +
                    punto.getDescription() + "\n";
            writer.write(riga);
            writer.flush();
            Log.d("Dialog_Add_Pnezd", "Punto aggiunto: " + riga);
        } catch (IOException e) {
            Log.e("Dialog_Add_Pnezd", "Errore scrittura CSV: " + e.getMessage());
        }
    }

}
