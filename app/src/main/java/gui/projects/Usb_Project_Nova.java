package gui.projects;

import static gui.MyApp.folderPath;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cp.cputils.Apollo2;
import com.example.stx_dig.R;
import com.van.jni.VanCmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import utils.MyData;
import utils.MyDeviceManager;
import utils.UsbReceiver;

public class Usb_Project_Nova extends AppCompatActivity {

    private final BroadcastReceiver usbReceiver = new UsbReceiver();
    TextView textView,txt2;
    int controllo = 0;
    private boolean enImport, enExport;
    RecyclerView recyclerProj, recyclerIn;
    ImageView back, update, read, write, usb_remove;
    ArrayList<ProjectFileAdapter.FileItem> filesProj, filesIN;
    ProjectFileAdapter adapterProj, adapterMC;
    static String usbPath;

    String APP_PATH = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects";
    String s;
    boolean unmount;
    int filterType;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_usb_nuova);
        Intent intent=getIntent();
        s=intent.getStringExtra("usb");
        if(s.equals("main")){
            APP_PATH=Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects";
            filterType=1;
        }else {
            APP_PATH=s;
            filterType=2;
        }
        Log.d("APP_PATH",s);
        usbPath = MyData.get_String("_usbPath");
        findView();
        init();
        onClick();
        updateUI();
        MyDeviceManager.host(this);
        unmount = false;

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(usbReceiver);
    }

    private void findView() {
        recyclerProj = findViewById(R.id.recycler_view_proj);
        recyclerIn = findViewById(R.id.recycler_view_in);
        back = findViewById(R.id.back);
        update = findViewById(R.id.new_Update);
        read = findViewById(R.id.new_copy_from_usb);
        write = findViewById(R.id.new_copy_to_usb);
        textView = findViewById(R.id.txt1);
        txt2=findViewById(R.id.txt2);
        usb_remove = findViewById(R.id.delete);



    }

    private void init() {
        readProjectFolder();
        txt2.setText(APP_PATH);


    }


    private void onClick() {
        usb_remove.setOnClickListener(view -> {
            try {
                if (Build.BRAND.equals("APOLLO2_10")||Build.BRAND.equals("APOLLO2_7")||Build.BRAND.equals("APOLLO2_12_PRO")||Build.BRAND.equals("APOLLO2_12_PLUS")) {
                    Apollo2 apollo2 = Apollo2.getInstance(this);
                    apollo2.exec("umount " + usbPath);

                } else {
                    VanCmd.exec("umount " + usbPath, 0);
                }
                new CustomToast(this, "USB EJECTED").show_long();
                unmount = true;

            } catch (Exception e) {
                new CustomToast(this, e.toString()).show();
            }
        });
        back.setOnClickListener((View v) -> {
            if(s.equals("main")) {
                back.setEnabled(false);
                startActivity(new Intent(this, Activity_Home_Page.class));
                overridePendingTransition(0, 0);
                finish();
            }else {
                back.setEnabled(false);
                startActivity(new Intent(this, PickProject.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        update.setOnClickListener((View v) -> {
            usbPath = getStoragePath(Usb_Project_Nova.this, true);
            readProjectFolder();
            readFromUSB_MC(usbPath);


        });

        read.setOnClickListener((View v) -> {
            if (enImport) {
                showProgress();
                (new Handler()).postDelayed(this::importFromUSB, 500);

            }
        });

        write.setOnClickListener((View v) -> {
            if (enExport) {
                showProgress();
                (new Handler()).postDelayed(this::exportToUSB, 500);

            }
        });


    }

    public void showProgress() {
        read.setVisibility(View.INVISIBLE);
        write.setVisibility(View.INVISIBLE);
        usb_remove.setVisibility(View.INVISIBLE);
        update.setVisibility(View.INVISIBLE);
        recyclerIn.setVisibility(View.INVISIBLE);
        recyclerProj.setVisibility(View.INVISIBLE);
        back.setVisibility(View.INVISIBLE);

    }

    public void hideProgress() {

        read.setVisibility(View.VISIBLE);
        write.setVisibility(View.VISIBLE);
        usb_remove.setVisibility(View.VISIBLE);
        update.setVisibility(View.VISIBLE);
        recyclerIn.setVisibility(View.VISIBLE);
        recyclerProj.setVisibility(View.VISIBLE);
        back.setVisibility(View.VISIBLE);
    }

    public void updateUI() {

        if (adapterProj != null) {
            enExport = adapterProj.getSelectedItem() > -1;
        } else {
            enExport = false;
        }
        if (adapterMC != null) {
            enImport = adapterMC.getSelectedItem() > -1;
        } else {
            enImport = false;
        }

        if (enImport) {
            read.setAlpha(1f);
        } else {
            read.setAlpha(0.3f);
        }

        if (enExport) {
            write.setAlpha(1f);
        } else {
            write.setAlpha(0.3f);
        }
        if (adapterProj != null && adapterMC != null) {
            if (adapterProj.getSelectedItem() > -1) {
                adapterMC.notifyDataSetChanged();

            }
            if (adapterMC.getSelectedItem() > -1) {
                adapterProj.notifyDataSetChanged();

            }

        }


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
                filesProj.add(new ProjectFileAdapter.FileItem(file.getName(), isFolder, size,file.getAbsolutePath()));
            }

            adapterProj = new ProjectFileAdapter(filesProj,filterType);
            recyclerProj.setAdapter(adapterProj);
            recyclerProj.setLayoutManager(new LinearLayoutManager(this));
            recyclerProj.setItemViewCacheSize(adapterProj.getItemCount());
        } else {
            new CustomToast(this, "Projects folder not found").show_alert();
        }
    }

    private void readFromUSB_MC(String usbPath) {


        if (usbPath != null) {


            filesIN = new ArrayList<>();


            File usbFolder = new File(usbPath);


            if (usbFolder.exists() && usbFolder.isDirectory()) {


                File inFolder = new File(usbFolder, "STX_MC");

                if (inFolder.exists() && inFolder.isDirectory()) {

                    File[] files = inFolder.listFiles();

                    assert files != null;
                    for (File file : files) {
                        boolean isFolder = file.isDirectory();
                        long size = file.isDirectory() ? getFolderSize(file) : file.length();
                        filesIN.add(new ProjectFileAdapter.FileItem(file.getName(), isFolder, size,file.getAbsolutePath()));
                    }
                    adapterMC = new ProjectFileAdapter(filesIN,filterType);
                    recyclerIn.setAdapter(adapterMC);
                    recyclerIn.setLayoutManager(new LinearLayoutManager(this));
                    recyclerIn.setItemViewCacheSize(adapterMC.getItemCount());
                    textView.setText(inFolder.toString());
                } else {
                    try {
                        //qui copiare un pdf nella usbFolder
                        if (inFolder.mkdir()) {
                            new CustomToast(this, "Folder 'STX_MC' created on USB stick").show();
                        } else {
                            new CustomToast(this, "Failed to create folder 'STX_MC' on USB stick").show_error();
                        }
                    } catch (Exception e) {
                        new CustomToast(this, "Failed to create folder 'STX_MC' on USB stick").show_error();
                    }

                }
            } else {
                new CustomToast(this, "USB stick not found").show_alert();
            }
        }
    }
    public void importFromUSB() {
        String foldername = "STX_MC";
        showProgress();

        File usbFolder = new File(getStoragePath(this, true), foldername);

        if (usbFolder.exists() && usbFolder.isDirectory()) {
            if (adapterMC != null && adapterMC.getSelectedItem() > -1) {
                File selectedFileOrFolder = new File(usbFolder, adapterMC.getSelectedFilePath());
                File destination = new File(APP_PATH, adapterMC.getSelectedFilePath());

                if (destination.exists()) {
                    hideProgress(); // nasconde subito lo spinner mentre aspettiamo risposta

                    new AlertDialog.Builder(this)
                            .setTitle("Sovrascrivere?")
                            .setMessage("Il file/cartella \"" + destination.getName() + "\" esiste già. Vuoi sovrascrivere?")
                            .setPositiveButton("Sì", (dialog, which) -> {
                                showProgress(); // rimostriamo lo spinner
                                try {
                                    copyWithProgress(selectedFileOrFolder, destination, selectedFileOrFolder.isDirectory());
                                } catch (Exception e) {
                                    new CustomToast(this, "Errore: " + e.getMessage()).show_error();
                                    hideProgress();
                                }
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                new CustomToast(this, "Importazione annullata").show();
                            })
                            .show();
                } else {
                    try {
                        copyWithProgress(selectedFileOrFolder, destination, selectedFileOrFolder.isDirectory());
                    } catch (Exception e) {
                        new CustomToast(this, "Errore: " + e.getMessage()).show_error();
                        hideProgress();
                    }
                }
            } else {
                new CustomToast(this, "Nessun file selezionato").show();
                hideProgress();
            }
        } else {
            new CustomToast(this, "Cartella 'STX_MC' non trovata sulla chiavetta USB").show();
            hideProgress();
        }
    }


/*
    public void importFromUSB() {
        String foldername = "STX_MC";
        //IN or OUT
        showProgress();
        File usbFolder = new File(getStoragePath(this, true), foldername);

        if (usbFolder.exists() && usbFolder.isDirectory()) {

            if (adapterMC != null) {
                if (adapterMC.getSelectedItem() > -1) {
                    File selectedFileOrFolder = new File(usbFolder, adapterMC.getSelectedFilePath());
                    File destination = new File(APP_PATH, adapterMC.getSelectedFilePath());

                    try {
                        copyWithProgress(selectedFileOrFolder, destination, selectedFileOrFolder.isDirectory());
                    } catch (Exception e) {
                        new CustomToast(this, "Errore: " + e.getMessage()).show_error();
                    }

                } else {
                    new CustomToast(this, "No Files Selected").show();
                }
            }

            readProjectFolder();
            readFromUSB_MC(usbPath);

        } else {
            new CustomToast(this, "Folder '" + "STX_MC" + "' not found on USB stick").show();
            hideProgress();
        }
    }
*/


    private void exportToUSB() {
        String nameFolder = "STX_MC";
        showProgress();
        File usbFolder = new File(getStoragePath(this, true), nameFolder);

        if (usbFolder.exists() && usbFolder.isDirectory()) {
            File projectFolder = new File(APP_PATH);

            if (adapterProj != null && adapterProj.getSelectedItem() > -1) {
                File selectedFileOrFolder = new File(projectFolder, adapterProj.getSelectedFilePath());
                File destination = new File(usbFolder, adapterProj.getSelectedFilePath());

                if (destination.exists()) {
                    hideProgress(); // fermiamo subito lo spinner prima della scelta

                    new AlertDialog.Builder(this)
                            .setTitle("Sovrascrivere?")
                            .setMessage("Il file/cartella \"" + destination.getName() + "\" esiste già sulla USB. Vuoi sovrascrivere?")
                            .setPositiveButton("Sì", (dialog, which) -> {
                                showProgress(); // lo riattiviamo se l'utente accetta
                                try {
                                    copyWithProgress(selectedFileOrFolder, destination, selectedFileOrFolder.isDirectory());
                                } catch (Exception e) {
                                    new CustomToast(this, "Errore: " + e.getMessage()).show_error();
                                    hideProgress();
                                }
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                new CustomToast(this, "Esportazione annullata").show();
                            })
                            .show();
                } else {
                    try {
                        copyWithProgress(selectedFileOrFolder, destination, selectedFileOrFolder.isDirectory());
                    } catch (Exception e) {
                        new CustomToast(this, "Errore: " + e.getMessage()).show_error();
                        hideProgress();
                    }
                }
            } else {
                new CustomToast(this, "Nessun file selezionato").show();
                hideProgress();
            }

            readProjectFolder();
            readFromUSB_MC(usbPath);

        } else {
            new CustomToast(this, "Cartella '" + nameFolder + "' non trovata sulla chiavetta USB").show();
            (new Handler()).postDelayed(this::hideProgress, 1500);
        }
    }


    /*
    private void exportToUSB() {
        // MC
        String nameFolder = "STX_MC";
        showProgress();
        File usbFolder = new File(getStoragePath(this, true), nameFolder);

        if (usbFolder.exists() && usbFolder.isDirectory()) {

            File projectFolder = new File(APP_PATH);

            if (adapterProj != null) {
                if (adapterProj.getSelectedItem() > -1) {
                    File selectedFileOrFolder = new File(projectFolder, adapterProj.getSelectedFilePath());
                    File destination = new File(usbFolder, adapterProj.getSelectedFilePath());

                    try {
                        copyWithProgress(selectedFileOrFolder, destination, selectedFileOrFolder.isDirectory());
                    } catch (Exception e) {
                        new CustomToast(this, "Errore: " + e.getMessage()).show_error();
                    }

                } else {
                    new CustomToast(this, "No Files Selected").show();
                    hideProgress();
                }
            }

            readProjectFolder();
            readFromUSB_MC(usbPath);

        } else {
            new CustomToast(this, "Folder '" + nameFolder + "' not found on USB stick").show();
            (new Handler()).postDelayed(this::hideProgress, 1500);
        }
    }

*/



    @SuppressLint("PrivateApi")
    private String getStoragePath(Context context, boolean isUsb) {

        String path = "";

        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        Class<?> volumeInfoClazz;
        Class<?> diskInfoClaszz;

        try {

            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");

            diskInfoClaszz = Class.forName("android.os.storage.DiskInfo");

            Method StorageManager_getVolumes = Class.forName("android.os.storage.StorageManager").getMethod("getVolumes");

            Method VolumeInfo_GetDisk = volumeInfoClazz.getMethod("getDisk");

            Method VolumeInfo_GetPath = volumeInfoClazz.getMethod("getPath");

            Method DiskInfo_IsUsb = diskInfoClaszz.getMethod("isUsb");

            Method DiskInfo_IsSd = diskInfoClaszz.getMethod("isSd");

            List<Object> List_VolumeInfo = (List<Object>) StorageManager_getVolumes.invoke(mStorageManager);

            assert List_VolumeInfo != null;

            for (int i = 0; i < List_VolumeInfo.size(); i++) {

                Object volumeInfo = List_VolumeInfo.get(i);

                Object diskInfo = VolumeInfo_GetDisk.invoke(volumeInfo);

                if (diskInfo == null) continue;

                boolean sd = (boolean) DiskInfo_IsSd.invoke(diskInfo);

                boolean usb = (boolean) DiskInfo_IsUsb.invoke(diskInfo);

                File file = (File) VolumeInfo_GetPath.invoke(volumeInfo);

                if (isUsb == usb) {//usb

                    if (file != null) {

                        path = file.getAbsolutePath();
                    }

                } else if (!isUsb == sd) {//sd

                    if (file != null) {

                        path = file.getAbsolutePath();
                    }

                }

            }

        } catch (Exception e) {

            new CustomToast(Usb_Project_Nova.this, e.toString());

        }
        MyData.push("_usbPath", path);
        return path;
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyDeviceManager.periph(this);
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }


    private void copyFolder(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String[] children = source.list();
            for (String file : children) {
                copyFolder(new File(source, file), new File(destination, file));
            }
        } else {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(destination);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
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
    private void copyWithProgress(File source, File destination, boolean isFolder) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.progress_dialog, null);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_bar);
        TextView progressText = dialogView.findViewById(R.id.progress_text);

        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        progressDialog.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                if (isFolder) {
                    long totalSize = getFolderSize(source);
                    long[] copied = {0};
                    copyFolderWithProgress(source, destination, totalSize, copied, handler, progressBar, progressText);
                } else {
                    long totalSize = source.length();
                    try (
                            InputStream in = new FileInputStream(source);
                            OutputStream out = new FileOutputStream(destination)
                    ) {
                        byte[] buffer = new byte[4096];
                        int length;
                        long copiedBytes = 0;

                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                            copiedBytes += length;
                            int percent = (int) ((copiedBytes * 100) / totalSize);

                            int finalPercent = percent;
                            handler.post(() -> {
                                progressBar.setProgress(finalPercent);
                                progressText.setText("Copying... " + finalPercent + "%");
                            });
                        }
                    }
                }

                handler.post(() -> {
                    progressDialog.dismiss();
                    hideProgress();
                    new CustomToast(this, "Import successful").show();
                    readProjectFolder();
                    readFromUSB_MC(usbPath);
                });

            } catch (IOException e) {
                handler.post(() -> {
                    progressDialog.dismiss();
                    hideProgress();
                    new CustomToast(this, "Error: " + e.getMessage()).show_error();
                });
            }
        });
    }
    private void copyFolderWithProgress(File src, File dest, long totalSize, long[] copied, Handler handler, ProgressBar bar, TextView text) throws IOException {
        if (!dest.exists()) dest.mkdirs();
        File[] files = src.listFiles();
        if (files != null) {
            for (File file : files) {
                File target = new File(dest, file.getName());
                if (file.isDirectory()) {
                    copyFolderWithProgress(file, target, totalSize, copied, handler, bar, text);
                } else {
                    try (
                            InputStream in = new FileInputStream(file);
                            OutputStream out = new FileOutputStream(target)
                    ) {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                            copied[0] += length;
                            int percent = (int) ((copied[0] * 100) / totalSize);
                            handler.post(() -> {
                                bar.setProgress(percent);
                                text.setText("Copying... " + percent + "%");
                            });
                        }
                    }
                }
            }
        }
    }


}
