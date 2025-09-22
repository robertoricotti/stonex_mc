package gui.dialogs_and_toast;

import static services.ReadProjectService.fileExtensionPOINT;
import static services.TriangleService.scanPNEZD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

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
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Add_Pnezd {
    CustomQwertyDialog customQwertyDialog;
    private boolean isUpdating = false;
    private Handler handler;
    Activity activity;
    public Dialog dialog;
    TextView filename, toolCoord, pnezd_color;
    TextView ETdescription;
    ImageView save, cancel, lista, removelast;
    String path = "";
    String filepath;
    LinearLayout customView;
    RecyclerView recyclerView;
    public static boolean showingRecycler;
    PNEZDAdapter adapter;
    double nord = 0.000;
    double est = 0.000;
    double quota = 100.000;

    public Dialog_Add_Pnezd(Activity activity, String path) {
        this.activity = activity;
        this.path = path;
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
                writer.write("P,N,E,Z,D,Color\n"); // intestazione
                writer.flush();
                Log.d("Dialog_Add_Pnezd", "Creato file CSV: " + csvFile.getAbsolutePath());
                filepath = csvFile.getAbsolutePath();
            } catch (IOException e) {
                Log.e("Dialog_Add_Pnezd", "Errore nella creazione del CSV: " + e.getMessage());
            }
        }


    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_add_pnezd);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        String lastDescription = MyData.get_String("lastDescription");
        String lastColor = MyData.get_String("lastColor");
        if (lastColor == null) {
            lastColor = "red";
            MyData.push("lastColor", "red");
        }

        if (lastDescription == null) {
            lastDescription = "No Data";
            MyData.push("lastDescription", "No Data");
        }
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 🔹 Applica dim
            window.setAttributes(wlp);
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
        customQwertyDialog = new CustomQwertyDialog(activity,null);
        onClick();
        adapter = new PNEZDAdapter(leggiCSV(filepath));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
        adapter.scrollToLast(recyclerView);
        startUpdatingCoordinates();

    }

    private void findView() {
        save = dialog.findViewById(R.id.save);
        cancel = dialog.findViewById(R.id.cancel);
        lista = dialog.findViewById(R.id.lista);
        removelast = dialog.findViewById(R.id.remove);
        customView = dialog.findViewById(R.id.customLinearLayout);
        recyclerView = dialog.findViewById(R.id.pointsRecyclerView);
        filename = dialog.findViewById(R.id.tvPath);
        toolCoord = dialog.findViewById(R.id.tvCoord);
        pnezd_color = dialog.findViewById(R.id.pnezd_color);
        ETdescription = dialog.findViewById(R.id.descr);
        ETdescription.setText(MyData.get_String("lastDescription"));
        lista.setRotation(0f);

    }

    private void update() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    pnezd_color.setBackgroundColor(setColor(MyData.get_String("lastColor")));
                    if (!showingRecycler) {
                        recyclerView.setVisibility(View.VISIBLE);
                        customView.setVisibility(View.INVISIBLE);
                        save.setVisibility(View.INVISIBLE);
                        removelast.setVisibility(View.VISIBLE);
                        lista.setRotation(0f);
                    } else {
                        save.setVisibility(View.VISIBLE);
                        removelast.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.INVISIBLE);
                        customView.setVisibility(View.VISIBLE);
                        lista.setRotation(180f);
                    }
                    filename.setText(filepath.replace("/storage/emulated/0/", ""));
                    String Snord = "0.000", Sest = "0.000", Squota = "0.000";
                    try {
                        switch (DataSaved.bucketEdge) {
                            case -1:
                                Snord = Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[1]));
                                Sest = Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[0]));
                                Squota = Utils.showCoords(String.valueOf(ExcavatorLib.bucketLeftCoord[2]));
                                nord = ExcavatorLib.bucketLeftCoord[1];
                                est = ExcavatorLib.bucketLeftCoord[0];
                                quota = ExcavatorLib.bucketLeftCoord[2];
                                break;

                            case 0:
                                Snord = Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[1]));
                                Sest = Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[0]));
                                Squota = Utils.showCoords(String.valueOf(ExcavatorLib.bucketCoord[2]));
                                nord = ExcavatorLib.bucketCoord[1];
                                est = ExcavatorLib.bucketCoord[0];
                                quota = ExcavatorLib.bucketCoord[2];
                                break;

                            case 1:
                                Snord = Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[1]));
                                Sest = Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[0]));
                                Squota = Utils.showCoords(String.valueOf(ExcavatorLib.bucketRightCoord[2]));
                                nord = ExcavatorLib.bucketRightCoord[1];
                                est = ExcavatorLib.bucketRightCoord[0];
                                quota = ExcavatorLib.bucketRightCoord[2];
                                break;
                        }
                    } catch (Exception ignored) {

                    }

                    toolCoord.setText("N: " + Snord + " E: " + Sest + " Z: " + Squota);
                    ETdescription.setText(MyData.get_String("lastDescription"));

                    Log.d("Dialog_Add_Pnezd", MyData.get_String("lastColor"));
                    if (isUpdating) {
                        update();
                    }
                } catch (Exception e) {
                    Log.e("Dialog_Add_Pnezd", Log.getStackTraceString(e));
                }
            }
        }, 100);
    }

    private void onClick() {
        pnezd_color.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(activity, pnezd_color);
            popupMenu.getMenu().add("RED");
            popupMenu.getMenu().add("YELLOW");
            popupMenu.getMenu().add("BLUE");
            popupMenu.getMenu().add("GREEN");
            popupMenu.getMenu().add("CYAN");
            popupMenu.getMenu().add("MAGENTA");
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getTitle().toString()) {
                        case "RED":
                            MyData.push("lastColor", "red");
                            return true;
                        case "YELLOW":
                            MyData.push("lastColor", "yellow");
                            return true;
                        case "BLUE":
                            MyData.push("lastColor", "blue");
                            return true;
                        case "GREEN":
                            MyData.push("lastColor", "green");
                            return true;
                        case "CYAN":
                            MyData.push("lastColor", "cyan");
                            return true;
                        case "MAGENTA":
                            MyData.push("lastColor", "magenta");
                            return true;


                        default:
                            return false;
                    }

                }
            });
            popupMenu.show();


        });
        ETdescription.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(ETdescription, -1, 994);
            }
        });
        cancel.setOnClickListener(view -> {
            stopUpdatingCoordinates();
            scanPNEZD();
            dialog.dismiss();
        });
        removelast.setOnClickListener(view -> {
            if(adapter.getSelectedItem()!=null) {
                // Crea un nuovo AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(activity.getString(R.string.delete_obj));
                builder.setIcon(activity.getResources().getDrawable(R.drawable.delete));

                // Aggiungi il pulsante "Sì"
                builder.setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        codeRemoveSelected();

                    }

                });
                builder.setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                });
                builder.show();

            }else {
                new CustomToast(activity,"No Selection").show();
            }

        });

        save.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Save Point?");
            builder.setIcon(activity.getResources().getDrawable(R.drawable.save_icon));

            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    codeSave();

                }

            });
            builder.setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
            ETdescription.setText(MyData.get_String("lastDescription"));
        });
        lista.setOnClickListener(v -> {
            showingRecycler = !showingRecycler;
            adapter.scrollToLast(recyclerView);
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
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    int pointNumber = Integer.parseInt(parts[0].trim());
                    double northing = Double.parseDouble(parts[1].trim());
                    double easting = Double.parseDouble(parts[2].trim());
                    double elevation = Double.parseDouble(parts[3].trim());
                    String description = parts[4].trim();
                    int color = Color.RED;

                    punti.add(new PNEZDPoint(pointNumber, northing, easting, elevation, description, color));
                } else if (parts.length > 5) {

                    int pointNumber = Integer.parseInt(parts[0].trim());
                    double northing = Double.parseDouble(parts[1].trim());
                    double easting = Double.parseDouble(parts[2].trim());
                    double elevation = Double.parseDouble(parts[3].trim());
                    String description = parts[4].trim();
                    int color = Integer.parseInt(parts[5].trim());

                    punti.add(new PNEZDPoint(pointNumber, northing, easting, elevation, description, color));

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
                    punto.getDescription() + "," +
                    punto.getColor() + "\n";
            writer.write(riga);
            writer.flush();
            Log.d("Dialog_Add_Pnezd", "Punto aggiunto: " + riga);
        } catch (IOException e) {
            Log.e("Dialog_Add_Pnezd", "Errore scrittura CSV: " + e.getMessage());
        }
    }

    private void startUpdatingCoordinates() {
        if (!isUpdating) {
            isUpdating = true;
            handler = new Handler();
            update();
        }
    }

    private void stopUpdatingCoordinates() {
        if (isUpdating) {
            isUpdating = false;
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
    }

    private void codeRemoveSelected() {
        if (adapter != null) {
            // Prendi il punto selezionato dall'adapter
            PNEZDPoint selected = adapter.getSelectedItem();
            if (selected == null) return;

            String selectedFilename = selected.getFilename();

            // Rimuovi dalla lista interna dei PNEZD
            DataSaved.pnezdPoints.remove(selected);

            //  Rimuovi dal file CSV e aggiorna numerazione
            File file = new File(filepath);
            if (!file.exists()) return;

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

            if (lines.size() <= 1) return; // niente dati oltre intestazione

            // Rimuovi la riga del punto selezionato
            lines.removeIf(line -> {
                if (line.trim().isEmpty()) return false;
                String[] parts = line.split(",");
                try {
                    int num = Integer.parseInt(parts[0].trim());
                    return num == selected.getPointNumber();
                } catch (Exception e) {
                    return false;
                }
            });

            // Riscrivi il CSV con numeri progressivi
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(lines.get(0) + "\n"); // intestazione
                for (int i = 1; i < lines.size(); i++) {
                    String[] parts = lines.get(i).split(",");
                    parts[0] = String.valueOf(i); // riassegna numero progressivo
                    writer.write(String.join(",", parts) + "\n");
                }
                writer.flush();
                new CustomToast(activity, "Point Removed").show_alert();
            } catch (IOException e) {
                Log.e("Dialog_Add_Pnezd", "Errore scrittura CSV: " + e.getMessage());
            }

            // 3️⃣ Rimuovi il punto selezionato da DataSaved.points SOLO se layer = "MyPNEZD"
            DataSaved.points.removeIf(p ->
                    p.getLayer() != null &&
                            "MyPNEZD".equals(p.getLayer().getLayerName()) &&
                            selectedFilename.equals(p.getFilename())
            );

            // 4️⃣ Aggiorna l’adapter con la lista aggiornata
            List<PNEZDPoint> nuoviPunti = leggiCSV(filepath);
            adapter.updateData(nuoviPunti);
            adapter.notifyDataSetChanged();
        }
    }




    private void codeRemoveLast() {
        {
            File file = new File(filepath);
            if (!file.exists()) {
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
                return;
            }

            // Rimuove l'ultima riga (ma mantiene intestazione)
            lines.remove(lines.size() - 1);

            try (FileWriter writer = new FileWriter(file, false)) {
                for (String l : lines) {
                    writer.write(l + "\n");
                }
                writer.flush();
                new CustomToast(activity, "Point Removed").show_alert();
            } catch (IOException e) {
                Log.e("Dialog_Add_Pnezd", "Errore scrittura CSV: " + e.getMessage());
            }

            // Se visibile, aggiorna la lista

            List<PNEZDPoint> nuoviPunti = leggiCSV(filepath);
            adapter.updateData(nuoviPunti); // metodo custom
            adapter.notifyDataSetChanged();
        }
    }

    private void codeSave() {
        {
            int numero = getNextPointNumber();  // ora è dinamico

            String descrizione = "No Description";
            try {
                descrizione = ETdescription.getText().toString();
            } catch (Exception e) {
                descrizione = "No Data";
            }

            PNEZDPoint nuovoPunto = new PNEZDPoint(numero, nord, est, quota, descrizione, setColor(MyData.get_String("lastColor")));
            aggiungiPuntoAlCSV(nuovoPunto);
            MyData.push("lastDescription", descrizione);


            List<PNEZDPoint> punti = leggiCSV(filepath);
            adapter.updateData(punti); // metodo custom
            adapter.notifyDataSetChanged();
            new CustomToast(activity, "P" + numero + "\n" + descrizione + "\n").show_alert();
            stopUpdatingCoordinates();
            scanPNEZD();
            dialog.dismiss();
        }
    }

    private int setColor(String color) {
        return switch (color) {
            case "red" -> {
                pnezd_color.setTextColor(Color.WHITE);
                yield Color.RED;
            }
            case "yellow" -> {
                pnezd_color.setTextColor(Color.DKGRAY);
                yield Color.YELLOW;
            }
            case "blue" -> {
                pnezd_color.setTextColor(Color.WHITE);
                yield Color.BLUE;
            }
            case "green" -> {
                pnezd_color.setTextColor(Color.DKGRAY);
                yield Color.GREEN;
            }
            case "cyan" -> {
                pnezd_color.setTextColor(Color.DKGRAY);
                yield Color.CYAN;
            }
            case "magenta" -> {
                pnezd_color.setTextColor(Color.WHITE);
                yield Color.MAGENTA;
            }
            default -> {
                pnezd_color.setTextColor(Color.WHITE);
                yield Color.RED;
            }
        };
    }
}
