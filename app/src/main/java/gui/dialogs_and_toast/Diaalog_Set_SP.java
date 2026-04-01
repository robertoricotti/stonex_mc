package gui.dialogs_and_toast;

import static gui.MyApp.geoidAll;
import static packexcalib.gnss.CRS_Strings._NONE;
import static services.CanSender.GNSS_MSG;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gui.MyApp;
import gui.projects.ProjectFileAdapter;
import packexcalib.exca.CreateUTMFiles;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.Deg2UTM;
import packexcalib.gnss.LocalizationFactory;
import serial.SerialPortManager;
import services.ReadProjectService;
import services.UpdateValuesService;
import utils.CanFileTransfer;
import utils.FullscreenActivity;
import utils.MyData;
import utils.MyDeviceManager;
import utils.MyEpsgNumber;

public class Diaalog_Set_SP {
    private static final String ASSET_SP_ROOT = "sp_data";
    String mPath;
    List<ProjectFileAdapter.FileItem> arraySPOriginale = new ArrayList<>();
    TextView geoidStatus;
    CustomQwertyDialog customQwertyDialog;
    String mTesto;
    int perc = 0;
    Activity activity;
    public Dialog dialog;
    ImageView dismiss;
    TextView messaggio;
    ImageView usaSP, startSearch;
    RecyclerView recyclerViewSP;
    ArrayList<ProjectFileAdapter.FileItem> arrayFiles, arraySP;
    EditText cercaSP;
    ProjectFileAdapter spAdapter, remoteAdapter;
    ProgressBar progressBar;
    boolean isUpdating = false;
    private Handler handler;
    ArrayAdapter<String> adapter;
    List<String> nazioni;
    Spinner spinner;
    String selectedFolder;
    TextView inUso;
    private OnDialogDismissListener dismissListener;


    public interface OnDialogDismissListener {
        void onDismiss();
    }

    public void setOnDialogDismissListener(OnDialogDismissListener listener) {
        this.dismissListener = listener;
    }


    public Diaalog_Set_SP(Activity activity) {
        this.activity = activity;

        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    public void show() {


        dialog.create();
        dialog.setContentView(R.layout.dialog_sp_folders);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.setOnDismissListener(d -> {
            if (dismissListener != null) {
                dismissListener.onDismiss();
            }
        });
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        customQwertyDialog = new CustomQwertyDialog(activity, null);
        findView();
        init();
        onClick();
        startUpdating();

    }

    public void show(String mPath, ProjectFileAdapter remoteAdapter) {
        this.mPath = mPath;
        this.remoteAdapter = remoteAdapter;
        dialog.create();
        dialog.setContentView(R.layout.dialog_sp_folders);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        customQwertyDialog = new CustomQwertyDialog(activity, null);
        findView();
        init();
        onClick();
        startUpdating();

    }

    public void findView() {
        cercaSP = dialog.findViewById(R.id.cercaSP);
        startSearch = dialog.findViewById(R.id.startSerach);
        dismiss = dialog.findViewById(R.id.dismiss);
        recyclerViewSP = dialog.findViewById(R.id.recycler_viewSP);
        usaSP = dialog.findViewById(R.id.usaSP);
        progressBar = dialog.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        messaggio = dialog.findViewById(R.id.msg);
        spinner = dialog.findViewById(R.id.spinner);
        inUso = dialog.findViewById(R.id.tvSP);
        geoidStatus = dialog.findViewById(R.id.geoidStatus);


    }

    public void init() {


        arrayFiles = new ArrayList<>();
        arraySP = new ArrayList<>();
        spAdapter = new ProjectFileAdapter(arraySP);
        recyclerViewSP.setAdapter(spAdapter);
        recyclerViewSP.setLayoutManager(new LinearLayoutManager(activity));
        recyclerViewSP.setItemViewCacheSize(spAdapter.getItemCount());
        nazioni = getNomiCartelleDaAssets();
        adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, nazioni);
        adapter.setDropDownViewResource(R.layout.layout_custom_spinner_3);

        String s = "";
        s = MyData.get_String("LastNation");

        if (s == null) {
            spinner.setAdapter(adapter);
        } else {
            adapter.insert(s, 0);
            spinner.setAdapter(adapter);
        }
        String s1 = "";
        s1 = MyData.get_String("LastSP");
        if (s1 == null) {
            inUso.setText("");
        } else {
            inUso.setText(s1);
        }

        arraySPOriginale.clear();
        arraySPOriginale.addAll(arraySP);

    }

    public void onClick() {
        geoidStatus.setOnClickListener(view -> {
            CustomMenuLista customMenu = new CustomMenuLista(activity, "GEOID FILES");

            if (geoidAll == null || geoidAll.length == 0) {
                new CustomToast(activity, "No Geoid Found").show_error();
                MyData.push("geoidPath", null);
                MyApp.GEOIDE_PATH = null;

                return;
            }

            // Crea un nuovo array con spazio per "DISABLED" + tutti gli elementi di geoidAll
            String[] menuItems = new String[geoidAll.length + 1];
            menuItems[0] = activity.getResources().getString(R.string.disabled);
            System.arraycopy(geoidAll, 0, menuItems, 1, geoidAll.length);

            List<String> menuItemsL = Arrays.asList(menuItems);

            customMenu.show(menuItemsL, new CustomMenu.OnItemSelectedListener() {
                @Override
                public void onItemSelected(String selectedItem) {
                    new CustomToast(activity, "Geoid: " + selectedItem).show_added();

                    if (selectedItem.equals(activity.getResources().getString(R.string.disabled))) {
                        MyData.push("geoidPath", null);
                        MyApp.GEOIDE_PATH = null;
                    } else {
                        MyData.push("geoidPath", selectedItem);
                        MyApp.GEOIDE_PATH = selectedItem;
                    }
                }
            });
        });


        cercaSP.setOnClickListener(view -> {
            if (!customQwertyDialog.dialog.isShowing()) {
                customQwertyDialog.show(cercaSP);
            }
        });
        usaSP.setOnClickListener(view -> {


            if (spAdapter.getSelectedItem() == -1) {
                new CustomToast(activity, "SELECT A SP FILE TO USE").show();
            } else {


                // Supponiamo che "selectedFileName" sia il nome del file selezionato ottenuto dallo spinner
                String selectedFileName = arraySP.get(spAdapter.getSelectedItem()).getName();
                String folderPath = selectedFolder; // La cartella selezionata

                Handler handler1 = new Handler(activity.getMainLooper());
                handler1.postDelayed(() -> {
                    try {
                        MyData.push("LastSP", selectedFileName);
                        inUso.setText(selectedFileName);
                        String match = getCrsCodeFromFileName(selectedFileName);

                        MyData.push("CRS_ESTERNO", ASSET_SP_ROOT + "/" + selectedFolder + "/" + selectedFileName);
                        if (match != null) {

                            if (match.equals("UTM")) {
                                //UTM autozone

                                MyData.push("crs", "UTM");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                try {
                                    copyFromAssetsToFile(activity, ASSET_SP_ROOT + "/" + selectedFolder + "/" + selectedFileName, new File(mPath, selectedFileName));
                                } catch (Exception e) {
                                    Log.e("testSP", Log.getStackTraceString(e));
                                }

                                dialog.dismiss();
                            } else if (match.equals("LOCAL")) {
                                //LOCAL
                                MyData.push("crs", "LOCAL");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                try {
                                    copyFromAssetsToFile(activity, ASSET_SP_ROOT + "/" + selectedFolder + "/" + selectedFileName, new File(mPath, selectedFileName));
                                } catch (Exception e) {
                                    Log.e("testSP", Log.getStackTraceString(e));
                                }
                                dialog.dismiss();
                            } else {
                                MyData.push("crs", match);
                                DataSaved.S_CRS = MyData.get_String("crs");
                                try {
                                    copyFromAssetsToFile(activity, ASSET_SP_ROOT + "/" + selectedFolder + "/" + selectedFileName, new File(mPath, selectedFileName));
                                } catch (Exception e) {
                                    Log.e("testSP", Log.getStackTraceString(e));
                                }
                                ReadProjectService.startCRS();
                                dialog.dismiss();
                            }
                        } else {

                            //invia file SP

                            usaSP.setEnabled(false);
                            MyData.push("crs", ".SP FILE");
                            DataSaved.S_CRS = MyData.get_String("crs");

                            try {
                                MyData.push("CRS_ESTERNO", ASSET_SP_ROOT + "/" + selectedFolder + "/" + selectedFileName);
                                copyFromAssetsToFile(activity, ASSET_SP_ROOT + "/" + selectedFolder + "/" + selectedFileName, new File(mPath, selectedFileName));
                            } catch (Exception e) {
                                Log.e("testSP", Log.getStackTraceString(e));
                            }

                            ReadProjectService.model = LocalizationFactory.fromFile(new File(ASSET_SP_ROOT + "/" + selectedFolder + "/" + selectedFileName),
                                    Deg2UTM.nativeProjTransformer, Deg2UTM.nativeProjTransformer);
                            usaSP.setEnabled(true);
                        }


                    } catch (Exception e) {
                        new CustomToast(activity, "SP ERROR").show_error();
                        Log.e("SP_ERROR", "Error sending file", e);
                    }
                }, 100); // 100 milliseconds delay
            }


        });


        startSearch.setOnClickListener(view -> {
            try {
                cercaSP.getText().toString();
                if (!cercaSP.getText().toString().trim().isEmpty()) {
                    mTesto = cercaSP.getText().toString().trim();
                    Log.e("SpinnerTEST", mTesto + " before ");
                    cerca(mTesto);
                    Log.e("SpinnerTEST", mTesto + " after ");
                } else {
                    sortFiles(selectedFolder);
                }
            } catch (Exception e) {
                Log.e("SpinnerTEST", mTesto + "  " + e.toString());
            }

        });
        dismiss.setOnClickListener(view -> {
            // setupGNSS(DataSaved.S_CRS);

            dialog.dismiss();
        });
        // Aggiungi un listener per gestire la selezione degli elementi dello Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Ottieni il nome della cartella selezionata

                selectedFolder = (String) parent.getItemAtPosition(position);
                MyData.push("LastNation", selectedFolder);
                // Chiama il metodo sortFiles con il nome della cartella selezionata
                sortFiles(selectedFolder);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Azione quando nessun elemento è selezionato, se necessario
            }
        });
    }

    private void sortFiles(String mPath) {

        AssetManager assetManager = activity.getApplicationContext().getAssets(); // Ottiene l'AssetManager
        String folderPath = ASSET_SP_ROOT + "/" + mPath;

        try {
            // Ottiene tutti i file all'interno della cartella specificata
            String[] files = assetManager.list(folderPath);

            if (files != null) {
                arraySP.clear(); // Pulisce la lista prima di aggiungere i nuovi file

                for (String fileName : files) {
                    // Controlla se il file ha estensione ".SP"
                    if (fileName.toLowerCase().endsWith(".sp")) {
                        // Aggiungi il file all'array con il parametro `isFolder` impostato su `false`
                        long fileSize = getFileSizeFromAssets(assetManager, folderPath + "/" + fileName);
                        arraySP.add(new ProjectFileAdapter.FileItem(fileName, false, fileSize, new File(fileName).getAbsolutePath()));
                        arraySPOriginale.clear();
                        arraySPOriginale.addAll(arraySP);
                        spAdapter.notifyDataSetChanged();

                    }
                }


            } else {

            }
        } catch (IOException e) {
            Log.e("SpinnerTEST", "Errore durante la lettura dei file da assets", e);
        }
    }

    private long getFileSizeFromAssets(AssetManager assetManager, String filePath) {
        long size = 0;
        try {
            // Apri il file come InputStream per leggere i suoi byte
            InputStream inputStream = assetManager.open(filePath);
            size = inputStream.available(); // Ottieni la dimensione del file in byte
            inputStream.close(); // Chiudi l'InputStream dopo aver finito
        } catch (IOException e) {
            Log.e("FileSize", "Errore durante il calcolo della dimensione del file: " + filePath, e);
        }
        return size;
    }


    // Metodo per ottenere i nomi delle cartelle da "assets"
    private List<String> getNomiCartelleDaAssets() {
        List<String> cartelle = new ArrayList<>();
        AssetManager assetManager = activity.getApplicationContext().getAssets();

        try {
            String[] files = assetManager.list(ASSET_SP_ROOT);

            if (files != null) {
                for (String file : files) {
                    String fullPath = ASSET_SP_ROOT + "/" + file;
                    if (isDirectory(assetManager, fullPath) && file.equals(file.toUpperCase())) {
                        cartelle.add(file);
                    }
                }
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Errore durante la lettura delle cartelle da assets", e);
        }

        Collections.sort(cartelle);
        return cartelle;
    }

    // Metodo per verificare se un file è una directory
    private boolean isDirectory(AssetManager assetManager, String path) {
        try {
            // Prova a elencare il contenuto del percorso specificato; se ha contenuti, è una directory
            String[] files = assetManager.list(path);
            return files != null && files.length > 0;
        } catch (IOException e) {
            Log.e("MainActivity", "Errore nel controllo della directory: " + path, e);
            return false;
        }
    }

    private void startUpdating() {

        if (!isUpdating) {
            isUpdating = true;
            handler = new Handler();
            updateView();
        }
    }

    private void stopUpdating() {

        if (isUpdating) {
            isUpdating = false;
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
    }


    private void updateView() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update View

                try {
                    if (CanFileTransfer.sending) {
                        messaggio.setText("Sending...\n" + perc + " %");
                        progressBar.setVisibility(View.VISIBLE);
                        messaggio.setVisibility(View.VISIBLE);
                        recyclerViewSP.setVisibility(View.INVISIBLE);
                        dismiss.setVisibility(View.INVISIBLE);
                        usaSP.setVisibility(View.INVISIBLE);
                        startSearch.setVisibility(View.INVISIBLE);
                        spinner.setVisibility(View.INVISIBLE);
                        cercaSP.setVisibility(View.INVISIBLE);

                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        messaggio.setVisibility(View.INVISIBLE);
                        recyclerViewSP.setVisibility(View.VISIBLE);
                        dismiss.setVisibility(View.VISIBLE);
                        usaSP.setVisibility(View.VISIBLE);
                        startSearch.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.VISIBLE);
                        cercaSP.setVisibility(View.VISIBLE);
                    }

                    if (isUpdating) {
                        updateView();

                    }
                } catch (Exception e) {

                }
            }
        }, 100);
    }

    public void cerca(String testo) {
        List<ProjectFileAdapter.FileItem> risultatiFiltrati = new ArrayList<>();


        for (ProjectFileAdapter.FileItem item : arraySPOriginale) {


            if (item.getName().toLowerCase().contains(testo.toLowerCase())) {

                risultatiFiltrati.add(item);
            } else {
                Log.d("SpinnerTEST", "Elemento non trovato: fuori IF " + item.getName());
            }
        }

        // Aggiorna arraySP (che alimenta l'adapter)
        arraySP.clear();
        arraySP.addAll(risultatiFiltrati);
        spAdapter.notifyDataSetChanged();


    }


    private String copyAssetFileToTemp(String folderPath, String fileName) throws IOException {
        AssetManager assetManager = activity.getApplicationContext().getAssets();

        // Percorso completo nel pacchetto degli assets
        String assetPath = folderPath + "/" + fileName;

        // Directory temporanea in cui copiare il file
        File tempFile = new File(activity.getCacheDir(), fileName);

        try (InputStream inputStream = assetManager.open(assetPath);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        // Restituisce il percorso del file copiato
        return tempFile.getAbsolutePath();
    }

    public void copyFromAssetsToFile(Context context, String assetPath, File destinationFile) throws IOException {
        AssetManager assetManager = context.getAssets();

        File destFolder = destinationFile.getParentFile();
        if (destFolder != null && destFolder.exists()) {
            File[] existingSP = destFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".sp"));
            if (existingSP != null) {
                for (File f : existingSP) {
                    String crs = getCrsCodeFromFileName(f.getName());
                    // Cancella solo se appartiene agli asset (CRS riconosciuto)
                    if (crs != null) {
                        if (f.delete()) {
                            Log.d("SP_COPY", "Eliminato SP asset: " + f.getName());
                        } else {
                            Log.d("SP_COPY", "Impossibile eliminare SP asset: " + f.getName());
                        }
                    }
                }
            }
        }

        // Copia il nuovo file dall'asset
        try (InputStream inputStream = assetManager.open(assetPath);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }


    private void setupGNSS(String crs) {
        byte speed = 0;
        switch (DataSaved.reqSpeed) {
            case 0:
                speed = 5;
                break;
            case 1:
                speed = 4;
                break;
            case 2:
                speed = 3;
                break;
            case 3:
                speed = 0;
                break;

        }


        if (crs.equals(_NONE)) {
            //setup LLQ

            SerialPortManager.instance().sendCommand("set,device.remote.nmea,GGA:" + "0" + "|GSA:0|LLQ:" + "50" + "|GLL:0|HDT:" + "50" + "|\n");

            handler.postDelayed(() -> {
                SerialPortManager.instance().sendCommand("set,ports.reset,2\r\n");
                SerialPortManager.instance().sendCommand("set,ports.reset,1\r\n");
                SerialPortManager.instance().sendCommand("set,ports.reset,16\r\n");

            }, 1000);

        } else {
            //setup GGA
            SerialPortManager.instance().sendCommand("set,device.remote.nmea,GGA:" + "50" + "|GSA:0|LLQ:" + "0" + "|GLL:0|HDT:" + "50" + "|\n");
            Handler handler = new Handler(activity.getMainLooper());


            handler.postDelayed(() -> {
                SerialPortManager.instance().sendCommand("set,ports.reset,2\r\n");
                SerialPortManager.instance().sendCommand("set,ports.reset,1\r\n");
                SerialPortManager.instance().sendCommand("set,ports.reset,16\r\n");

            }, 1000);

        }

    }

    public static String getCrsCodeFromFileName(String fileName) {
        if (fileName == null || !fileName.toLowerCase().endsWith(".sp")) {
            return null;
        } else if (fileName.equals("UTM_AUTO_ZONE.SP")) {
            return "UTM";
        } else if (fileName.equals("LOCAL_COORDINATES_FROM_GNSS.SP")) {
            return "LOCAL";
        }
        {

            // Rimuove l'estensione .SP
            String methodName = fileName.substring(0, fileName.length() - 3);

            try {
                // Ottiene il campo statico della classe MyEpsgNumber con quel nome
                java.lang.reflect.Field field = MyEpsgNumber.class.getField(methodName);

                // Verifica se il campo è statico e di tipo int
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                    int value = field.getInt(null); // null per static field
                    return String.valueOf(value);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Campo non trovato o accesso non consentito
                return null;
            }

            return null;
        }


    }


}
