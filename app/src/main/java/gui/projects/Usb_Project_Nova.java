package gui.projects;

import static gui.MyApp.folderPath;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
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

import gui.dialogs_and_toast.CustomToast;
import utils.MyData;
import utils.MyDeviceManager;
import utils.UsbReceiver;

public class Usb_Project_Nova extends AppCompatActivity {

    private final BroadcastReceiver usbReceiver = new UsbReceiver();
    TextView textView;
    int controllo = 0;
    private boolean enImport, enExport;
    RecyclerView recyclerProj, recyclerIn;
    ImageView back, update, read, write, usb_remove;
    ArrayList<ProjectFileAdapter.FileItem> filesProj, filesIN;
    ProjectFileAdapter adapterProj, adapterMC;
    static String usbPath;
    ProgressBar progressBar;
    final String APP_PATH = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects";

    boolean unmount;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_usb_nuova);
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
        usb_remove = findViewById(R.id.delete);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);


    }

    private void init() {
        readProjectFolder();


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
                //back.callOnClick();
            } catch (Exception e) {
                new CustomToast(this, e.toString()).show();
            }
        });
        back.setOnClickListener((View v) -> {
            back.setEnabled(false);
            startActivity(new Intent(this, Projects.class));
            overridePendingTransition(0, 0);
            finish();
        });

        update.setOnClickListener((View v) -> {
            usbPath = getStoragePath(Usb_Project_Nova.this, true);
            readProjectFolder();
            readFromUSB_MC(usbPath);


        });

        read.setOnClickListener((View v) -> {
            if (enImport) {
                showProgress();
                (new Handler()).postDelayed(this::importFromUSB, 1500);

            }
        });

        write.setOnClickListener((View v) -> {
            if (enExport) {
                showProgress();
                (new Handler()).postDelayed(this::exportToUSB, 1500);

            }
        });


    }

    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        read.setVisibility(View.INVISIBLE);
        write.setVisibility(View.INVISIBLE);
        usb_remove.setVisibility(View.INVISIBLE);
        update.setVisibility(View.INVISIBLE);
        recyclerIn.setVisibility(View.INVISIBLE);
        recyclerProj.setVisibility(View.INVISIBLE);
        back.setVisibility(View.INVISIBLE);

    }

    public void hideProgress() {

        progressBar.setVisibility(View.INVISIBLE);
        read.setVisibility(View.VISIBLE);
        write.setVisibility(View.VISIBLE);
        usb_remove.setVisibility(View.VISIBLE);
        update.setVisibility(View.VISIBLE);
        recyclerIn.setVisibility(View.VISIBLE);
        recyclerProj.setVisibility(View.VISIBLE);
        back.setVisibility(View.VISIBLE);
    }

    public void updateUI() {

        if (progressBar.getVisibility() != View.VISIBLE) {
            if (UsbReceiver.usbIsConnected) {
                usb_remove.setVisibility(View.VISIBLE);
            } else {
                usb_remove.setVisibility(View.INVISIBLE);
            }
        }

        if (progressBar.getVisibility() == View.VISIBLE) {
            controllo++;
        }
        if (controllo > 1000) {
            hideProgress();
        }

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
                filesProj.add(new ProjectFileAdapter.FileItem(file.getName(), isFolder, size));
            }

            adapterProj = new ProjectFileAdapter(filesProj);
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
                        filesIN.add(new ProjectFileAdapter.FileItem(file.getName(), isFolder, size));
                    }
                    adapterMC = new ProjectFileAdapter(filesIN);
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
        //IN or OUT
        showProgress();
        File usbFolder = new File(getStoragePath(this, true), foldername);

        if (usbFolder.exists() && usbFolder.isDirectory()) {

            if (adapterMC != null) {
                if (adapterMC.getSelectedItem() > -1) {
                    File selectedFileOrFolder = new File(usbFolder, adapterMC.getSelectedFilePath());
                    File destination = new File(APP_PATH, adapterMC.getSelectedFilePath());

                    try {

                        if (selectedFileOrFolder.isDirectory()) {
                            copyFolder(selectedFileOrFolder, destination);
                        } else {
                            if (!destination.exists()) {
                                InputStream in = new FileInputStream(selectedFileOrFolder);
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

                        new CustomToast(this, "Import successful").show();
                        hideProgress();

                    } catch (IOException e) {
                        new CustomToast(this, "Error during import: " + e.getMessage()).show_error();
                        hideProgress();
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

                        if (selectedFileOrFolder.isDirectory()) {
                            copyFolder(selectedFileOrFolder, destination);
                        } else {
                            if (!destination.exists()) {
                                InputStream in = new FileInputStream(selectedFileOrFolder);
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

                        new CustomToast(this, "Export successful").show();
                        hideProgress();

                    } catch (IOException e) {
                        new CustomToast(this, "Error during export: " + e.getMessage()).show_error();
                        hideProgress();
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


}
