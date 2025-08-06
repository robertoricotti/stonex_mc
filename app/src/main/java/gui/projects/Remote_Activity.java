package gui.projects;

import static gui.MyApp.folderPath;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cp.cputils.Apollo2;
import com.cp.cputils.ApolloPro;
import com.example.stx_dig.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cloud.S3ManagerSingleton;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import utils.MyDeviceManager;
import utils.NetworkUtils;

public class Remote_Activity extends AppCompatActivity {
    public static boolean isAuthenticated;
    static String LocalFilePath = null;
    static String RemoteFilePath = null;
    boolean isF;
    long folderSize, fileSize;
    TextView txt1, txt2;
    private boolean enImport, enExport;
    S3ManagerSingleton s3Manager;
    ImageView download, upload, refresh, back, status;
    RecyclerView recyclerProj, recyclerIn;
    ArrayList<ProjectFileAdapter.FileItem> filesProj, filesIN;
    ProjectFileAdapter adapterProj, adapterMC;
    List<String> serials = List.of("");
    String s;
    private Handler handler;
    private boolean mRunning = true;
    final String APP_PATH = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        findView();
        init();
        onClick();
        mupdateUI();


    }

    private void findView() {
        recyclerProj = findViewById(R.id.recycler_view_proj);
        recyclerIn = findViewById(R.id.recycler_view_in);

        s = "";
        if (Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
            Apollo2 apollo2 = Apollo2.getInstance(this);
            s = apollo2.getDeviceSN();
        } else {
            ApolloPro apolloPro = ApolloPro.getInstance(this);
            s = apolloPro.getDeviceSN();
        }
        serials = List.of(s);

        download = findViewById(R.id.download);
        upload = findViewById(R.id.upload);
        refresh = findViewById(R.id.refresh);
        back = findViewById(R.id.back);
        status = findViewById(R.id.status);
        txt1 = findViewById(R.id.txt1);
        txt2 = findViewById(R.id.txt2);


    }

    private void init() {
        txt1.setText("serials/"+ MyDeviceManager.getDeviceSN(this)+"/Projects/");
        download.setEnabled(false);
        download.setAlpha(0.3f);
        upload.setEnabled(false);
        upload.setAlpha(0.3f);
        s3Manager = S3ManagerSingleton.getInstance(this);
        readProjectFolder();
        txt2.setText(APP_PATH);

    /*    s3Manager.getFoldersFiles("downloads/apk_release/", new S3Manager.S3Callback() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Log.d("File Presenti","File: "+result.toString());
            }

            @Override
            public void onError(Exception e) {
                Log.e("File Presenti","File: "+e.getStackTrace());
            }
        });*/

    }

    public void updateUI() {

        try {


            if (adapterProj != null && adapterMC != null) {
                if (adapterProj.getSelectedItem() > -1 && adapterMC.getSelectedItem() > -1) {
                    adapterProj.setSelectedItem(-1);
                    adapterMC.setSelectedItem(-1);
                    adapterMC.notifyDataSetChanged();
                    adapterProj.notifyDataSetChanged();
                }


            }

            enExport = adapterProj.getSelectedItem() > -1;
            enImport = adapterMC.getSelectedItem() > -1;

            if (enImport) {
                download.setEnabled(true);
                download.setAlpha(1.0f);
                isF = adapterMC.isFold();
                RemoteFilePath = "serials/" + s + "/Projects/" + adapterMC.getSelectedFilePath();
            } else {
                RemoteFilePath = null;
                download.setEnabled(false);
                download.setAlpha(0.3f);
            }
            if (enExport) {
                upload.setEnabled(true);
                upload.setAlpha(1.0f);
                isF = adapterProj.isFold();
                LocalFilePath = adapterProj.getSelectedFilePath();
            } else {
                upload.setEnabled(false);
                upload.setAlpha(0.3f);
                LocalFilePath = null;
            }


        } catch (Exception ignored) {
            download.setEnabled(false);
            download.setAlpha(0.3f);
            upload.setEnabled(false);
            upload.setAlpha(0.3f);
        }
    }

    private void onClick() {
        refresh.setOnClickListener(view -> {
            if (isAuthenticated){
                readProjectFolder();
            s3Manager.getTreeFromS3V2(serials, new S3ManagerSingleton.S3Callback() {
                @Override
                public void onSuccess(Map<String, Object> result) {
                    // Eseguito nel thread di rete, puoi aggiornare la UI con runOnUiThread
                    runOnUiThread(() -> {
                        Log.d("S3Tree", result.toString());
                        parseProjectsContent(result.toString());
                        // Aggiorna la UI se necessario
                    });
                }

                @Override
                public void onError(Exception e) {
                    // Gestisci l'errore
                    runOnUiThread(() -> Log.e("S3Tree", "Errore durante la generazione dell'albero", e));

                }
            });
        }else{
                new CustomToast(Remote_Activity.this,"Not Logged In").show_error();
            }
        });
        upload.setOnClickListener(view -> {
            if (isAuthenticated){

            boolean isTheSame = false;

            if (enExport && LocalFilePath != null) {

                for (int i = 0; i < filesIN.size(); i++) {


                    if (filesIN.get(i).getName().equals(adapterProj.getSelectedFilePath())) {
                        isTheSame = true;
                    }
                }
                if (!isTheSame) {
                    if (isF) {
                        s3Manager.uploadFolderToS3(APP_PATH + "/" + LocalFilePath, "serials/" + s + "/Projects/" + LocalFilePath, new S3ManagerSingleton.S3Callback() {
                            @Override
                            public void onSuccess(Map<String, Object> result) {
                                runOnUiThread(() -> {
                                    Log.d("S3Manager:UploadFolder", "Cartella caricata con successo: " + result.get("folderPath"));
                                    adapterProj.setSelectedItem(-1);
                                    adapterMC.setSelectedItem(-1);
                                    if (adapterMC != null) {
                                        adapterMC.notifyDataSetChanged();
                                    }
                                    if (adapterProj != null) {
                                        adapterProj.notifyDataSetChanged();
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                runOnUiThread(() -> Log.e("S3Manager:UploadFolder", "Errore durante il caricamento della cartella: " + e.getMessage(), e));
                            }
                        });
                    } else {
                        s3Manager.uploadFile(APP_PATH + "/" + LocalFilePath, "serials/" + s + "/Projects/" + LocalFilePath);
                    }
                    (new Handler()).postDelayed(this::refresha, 1000);
                } else {
                    new CustomToast(Remote_Activity.this, getResources().getString(R.string.file_exists)).show_alert();
                    adapterProj.setSelectedItem(-1);
                }
            }
            }else{
                new CustomToast(Remote_Activity.this,"Not Logged In").show_error();
            }
        });

        download.setOnClickListener(view -> {
            if (isAuthenticated){
            boolean theSame = false;
            if (enImport && RemoteFilePath != null) {
                for (int i = 0; i < filesProj.size(); i++) {


                    if (filesProj.get(i).getName().equals(adapterMC.getSelectedFilePath())) {
                        theSame = true;
                    }
                }
                if (!theSame) {
                    if (isF) {
                        s3Manager.downloadFolderFromS3(RemoteFilePath, APP_PATH + "/" + adapterMC.getSelectedFilePath(), new S3ManagerSingleton.S3Callback() {
                            @Override
                            public void onSuccess(Map<String, Object> result) {
                                runOnUiThread(() -> {
                                    // Log.d("S3Manager:DownloadFolder", "Cartella scaricata con successo: " + result.get("folderPath"));
                                    adapterProj.setSelectedItem(-1);
                                    adapterMC.setSelectedItem(-1);
                                    if (adapterMC != null) {
                                        adapterMC.notifyDataSetChanged();
                                    }
                                    if (adapterProj != null) {
                                        adapterProj.notifyDataSetChanged();
                                    }

                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                runOnUiThread(() -> Log.e("S3Manager:DownloadFolder", "Errore durante il download della cartella: " + e.getMessage(), e));
                            }


                        });
                        //logica qui per capire quando ha finito


                    } else {
                        s3Manager.downloadFile("serials/" + s + "/Projects/" + RemoteFilePath, APP_PATH + "/" + RemoteFilePath);
                    }
                    (new Handler()).postDelayed(this::refresha, 1000);
                } else {
                    // Crea un nuovo AlertDialog.Builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(Remote_Activity.this);
                    builder.setTitle(R.string.file_exists);
                    builder.setMessage(R.string.overwrite);
                    builder.setPositiveButton(R.string.yes, (dialog, which) -> {

                        if (isF) {
                            s3Manager.downloadFolderFromS3(RemoteFilePath, APP_PATH + "/" + adapterMC.getSelectedFilePath(), new S3ManagerSingleton.S3Callback() {
                                @Override
                                public void onSuccess(Map<String, Object> result) {
                                    runOnUiThread(() -> {
                                        Log.d("S3Manager:DownloadFolder", "Cartella scaricata con successo: " + result.get("folderPath"));
                                        adapterProj.setSelectedItem(-1);
                                        adapterMC.setSelectedItem(-1);
                                        if (adapterMC != null) {
                                            adapterMC.notifyDataSetChanged();
                                        }
                                        if (adapterProj != null) {
                                            adapterProj.notifyDataSetChanged();
                                        }

                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    runOnUiThread(() -> Log.e("S3Manager:DownloadFolder", "Errore durante il download della cartella: " + e.getMessage(), e));
                                }


                            });
                        } else {
                            s3Manager.downloadFile("serials/" + s + "/Projects/" + RemoteFilePath, APP_PATH + "/" + RemoteFilePath);
                        }
                        (new Handler()).postDelayed(this::refresha, 1000);
                    });

                    builder.setNegativeButton(R.string.no, (dialog, which) -> {

                        (new Handler()).postDelayed(this::refresha, 200);
                    });
                    builder.show();
                }

            }
            }else{
                new CustomToast(Remote_Activity.this,"Not Logged In").show_error();
            }
        });
        back.setOnClickListener(view -> {
            back.setEnabled(false);
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });
    }

    private void refresha() {
        refresh.callOnClick();
    }

    private void mupdateUI() {
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRunning) {
                    // Elabora i dati qui
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (NetworkUtils.isInternetAvailable(getApplicationContext())) {
                                if (isAuthenticated) {
                                    status.setImageResource(R.drawable.cloud_ok);
                                    status.setImageTintList(getColorStateList(R.color.element_green));
                                    txt1.setBackgroundColor(getResources().getColor(R.color.element_green));
                                } else {
                                    status.setImageResource(R.drawable.cloud_ok);
                                    status.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                                    txt1.setBackgroundColor(getResources().getColor(R.color.colorStonexBlue));
                                }
                            } else {
                                status.setImageResource(R.drawable.cloud_ko);
                                status.setImageTintList(getColorStateList(R.color.red));
                                txt1.setBackgroundColor(getResources().getColor(R.color.bg_sfsred));
                            }
                        }
                    });
                    // sleep per intervallo update UI
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRunning = false;
    }

    private void readProjectFolder() {


        filesProj = new ArrayList<>();

        File projectFolder = new File(APP_PATH);

        if (projectFolder.exists() && projectFolder.isDirectory()) {
            File[] files = projectFolder.listFiles();

            assert files != null;
            for (File file : files) {
                boolean isFolder = file.isDirectory();
                long size = file.isDirectory() ? getFolderSize(file) : file.length();
                filesProj.add(new ProjectFileAdapter.FileItem(file.getName(), isFolder, size, file.getAbsolutePath()));
            }

            adapterProj = new ProjectFileAdapter(filesProj);
            recyclerProj.setAdapter(adapterProj);
            recyclerProj.setLayoutManager(new LinearLayoutManager(this));
            recyclerProj.setItemViewCacheSize(adapterProj.getItemCount());
        } else {
            new CustomToast(this, "Projects folder not found").show_alert();
        }
    }

    private long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += getFolderSize(file);
                }
            }
        }
        return length;
    }


    private void parseProjectsContent(String data) {
        // Trova l'inizio della sezione Projects
        int startIndex = data.indexOf("Projects={");
        if (startIndex == -1) {
            Log.e("ParseError", "Projects section not found");
            return;
        }

        int braceCount = 0;
        boolean insideProjects = false;
        StringBuilder currentEntry = new StringBuilder();
        filesIN = new ArrayList<>();

        // Inizia a scansionare il contenuto di Projects
        for (int i = startIndex; i < data.length(); i++) {
            char c = data.charAt(i);

            // Rileva l'inizio della sezione Projects
            if (!insideProjects && data.substring(i).startsWith("Projects={")) {
                insideProjects = true;
                braceCount = 1; // La prima graffa aperta
                i += "Projects={".length() - 1; // Avanza l'indice
                continue;
            }

            if (insideProjects) {
                // Gestisci le graffe
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                }

                // Controlla se abbiamo completato un elemento
                if (c == ',' && braceCount == 1) {
                    processEntry(currentEntry.toString().trim());
                    currentEntry.setLength(0); // Resetta il buffer
                } else {
                    currentEntry.append(c);
                }

                // Se il livello di braceCount torna a 0, siamo fuori da Projects
                if (braceCount == 0) {
                    processEntry(currentEntry.toString().trim()); // L'ultimo elemento
                    break;
                }
            }
        }

        // Configura l'adapter per la RecyclerView
        adapterMC = new ProjectFileAdapter(filesIN);
        recyclerIn.setAdapter(adapterMC);
        recyclerIn.setLayoutManager(new LinearLayoutManager(this));
        recyclerIn.setItemViewCacheSize(adapterMC.getItemCount());
    }

    private void processEntry(String entry) {
        if (entry.isEmpty()) return;
        // Riconosce una cartella
        int braceIndex = entry.indexOf("={");
        if (braceIndex != -1) {
            String folderName = entry.substring(0, braceIndex).trim();

            s3Manager.getFolderSize("serials/" + s + "/Projects/" + folderName, new S3ManagerSingleton.S3Callback() {
                @Override
                public void onSuccess(Map<String, Object> result) {
                    folderSize = (long) result.get("size");
                    runOnUiThread(() -> {
                        filesIN.add(new ProjectFileAdapter.FileItem(folderName, true, folderSize, null));
                        adapterMC.notifyItemInserted(filesIN.size() - 1); // Notifica l'adapter
                    });
                    Log.d("S3Size", folderName + " Size: " + folderSize + " bytes");
                }

                @Override
                public void onError(Exception e) {
                    Log.e("S3Size", "Errore nel recupero della dimensione della cartella", e);
                }
            });


        } else if (entry.endsWith("=null") || entry.endsWith("=null}")) {
            // Riconosce un file
            String fileName = entry.substring(0, entry.indexOf("=null")).trim();

            s3Manager.getFileSize("serials/" + s + "/Projects/" + fileName, new S3ManagerSingleton.S3Callback() {
                @Override
                public void onSuccess(Map<String, Object> result) {

                    fileSize = (long) result.get("size");
                    runOnUiThread(() -> {
                        filesIN.add(new ProjectFileAdapter.FileItem(fileName, false, fileSize, null));
                        adapterMC.notifyItemInserted(filesIN.size() - 1); // Notifica l'adapter
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e("S3Size", "Errore nel recupero della dimensione del file", e);
                }
            });

        }
    }


}