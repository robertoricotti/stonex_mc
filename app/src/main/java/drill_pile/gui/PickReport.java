package drill_pile.gui;


import static cloud.WebSocketPlugin.isAuthenticated;
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
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cp.cputils.Apollo2;
import com.cp.cputils.shellcommand.CpCmd;
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
import java.util.Map;

import cloud.S3ManagerSingleton;
import cloud.WebSocketPlugin;
import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.UtcOffset;
import utils.MyData;
import utils.NetworkUtils;
import utils.UsbReceiver;


public class PickReport extends BaseClass {
    List<UtcOffset> offsets;
    int selectedOffsetMinutes;

    private final BroadcastReceiver usbReceiver = new UsbReceiver();
    String path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Exported";
    ImageView back, usb_remove, status, setClock;
    TextView titolone, tvUtcOffset;

    RecyclerView recyclerView;
    ReportFileAdapter reportFileAdapter;
    ArrayList<ReportFileAdapter.FileItem> arrayFiles;
    boolean unmount;
    static String usbPath;
    S3ManagerSingleton s3Manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_picker);
        findView();
        init();
        onClick();
        usbPath = MyData.get_String("_usbPath");
        unmount = false;
        updateUI();
        offsets = getAllUtcOffsets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
        if (NetworkUtils.isInternetAvailable(this)) {

            WebSocketPlugin.getWebSocketPluginInstance(this).start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(usbReceiver);
    }

    private void findView() {
        recyclerView = findViewById(R.id.recycler_view);
        back = findViewById(R.id.back);
        titolone = findViewById(R.id.titolone);
        usb_remove = findViewById(R.id.usb_remove);
        status = findViewById(R.id.status);
        setClock = findViewById(R.id.setClock);
        tvUtcOffset = findViewById(R.id.tvUtcOffset);

    }

    private void init() {
        tvUtcOffset.setText(formatOffset(DataSaved.UTC_Offset));
        s3Manager = S3ManagerSingleton.getInstance(this);
        arrayFiles = new ArrayList<>();
        path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Exported";
        File directory = new File(path);
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            boolean isFolder = file.isDirectory();
            long size = file.isDirectory() ? getFolderSize(file) : file.length();
            arrayFiles.add(new ReportFileAdapter.FileItem(file.getName(), isFolder, size, file.getAbsolutePath()));
        }
        reportFileAdapter = new ReportFileAdapter(arrayFiles);
        recyclerView.setAdapter(reportFileAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(reportFileAdapter.getItemCount());
        reportFileAdapter.setOnUsbClickListener(position -> {

            if (reportFileAdapter == null || position == RecyclerView.NO_POSITION) {

                return;
            }

            // 👉 equivalente ESATTO del tuo controllo adapterProj
            if (reportFileAdapter.getSelectedItem() < 0) {

                new CustomToast(this, "Select a report").show();
                return;
            }

            File source = new File(reportFileAdapter.getSelectedFilePathAbs());
            exportToUSB(source); // 🔥 tuo metodo reale
        });

        titolone.setText(path);


    }

    private void onClick() {
        setClock.setOnClickListener(view -> {
            showUtcPopupMenu(view);
        });
        status.setOnClickListener(view -> {
            if (NetworkUtils.isInternetAvailable(this)) {

                WebSocketPlugin.getWebSocketPluginInstance(this).start();
            }
        });
        back.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });
        usb_remove.setOnClickListener(view -> {
            try {
                if (Build.BRAND.equals("APOLLO2_10") || Build.BRAND.equals("APOLLO2_7") || Build.BRAND.equals("APOLLO2_12_PRO") || Build.BRAND.equals("APOLLO2_12_PLUS")) {
                    Apollo2 apollo2 = Apollo2.getInstance(this);
                    apollo2.exec("umount " + usbPath);

                } else if (Build.BRAND.equals("SRT8PROS") || Build.BRAND.equals("SRT7PROS")) {
                    VanCmd.exec("umount " + usbPath, 0);
                } else if (Build.BRAND.equals("MEGA_1")) {
                    new CpCmd().exceCmd("umount " + usbPath);
                }
                new CustomToast(this, getResources().getString(R.string.rimuovi_usb)).show_alert();
                unmount = true;

            } catch (Exception e) {
                new CustomToast(this, e.toString()).show();
            }
        });
        reportFileAdapter.setOnCloudClickListener(position -> {

            if (!isAuthenticated) {
                new CustomToast(this, "Not Logged In").show_error();
                return;
            }

            ReportFileAdapter.FileItem item = reportFileAdapter.getItem(position);

            if (item == null) return;

            uploadReportToS3(item);
        });

    }

    public void updateUI() {
        if (UsbReceiver.usbIsConnected) {
            usb_remove.setVisibility(View.VISIBLE);
        } else {
            usb_remove.setVisibility(View.INVISIBLE);
        }
        if (NetworkUtils.isInternetAvailable(getApplicationContext())) {
            if (isAuthenticated) {
                status.setImageResource(R.drawable.cloud_ok);
                status.setImageTintList(getColorStateList(R.color.element_green));
                //txt1.setBackgroundColor(getResources().getColor(R.color.element_green));
            } else {
                status.setImageResource(R.drawable.cloud_ok);
                status.setImageTintList(getColorStateList(R.color.colorStonexBlue));
                //txt1.setBackgroundColor(getResources().getColor(R.color.colorStonexBlue));
            }
        } else {
            status.setImageResource(R.drawable.cloud_ko);
            status.setImageTintList(getColorStateList(R.color.red));
            //txt1.setBackgroundColor(getResources().getColor(R.color.bg_sfsred));
        }
    }

    private void disableAll() {

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

    private void copyRecursive(File source, File dest) throws IOException {

        if (source.isDirectory()) {
            if (!dest.exists()) dest.mkdirs();

            File[] children = source.listFiles();
            if (children != null) {
                for (File child : children) {
                    copyRecursive(child, new File(dest, child.getName()));
                }
            }
        } else {
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(dest)) {

                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
        }
    }

    private void deleteRecursive(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            File[] files = fileOrDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecursive(f);
                }
            }
        }
        fileOrDir.delete();
    }

    private void exportToUSB(File source) {
        back.setEnabled(false);
        usb_remove.setEnabled(false);

        new Thread(() -> {

            try {
                File usbRoot = new File(getStoragePath(this, true));
                File stxFolder = new File(usbRoot, "STX_MC");

                if (!stxFolder.exists()) {
                    stxFolder.mkdirs();
                }

                File destination = new File(stxFolder, source.getName());

                if (destination.exists()) {
                    deleteRecursive(destination);
                }

                copyRecursive(source, destination);

                runOnUiThread(() -> {
                    back.setEnabled(true);
                    usb_remove.setEnabled(true);
                    new CustomToast(this, "Export completed").show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    back.setEnabled(true);
                    usb_remove.setEnabled(true);
                    new CustomToast(this, "Export error: " + e.getMessage()).show_error();
                });
            }

        }).start();
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
            @SuppressWarnings("unchecked")
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


        }
        MyData.push("_usbPath", path);
        return path;
    }

    private void uploadReportToS3(ReportFileAdapter.FileItem item) {

        Log.d("itemPath", item.getPath());
        String localPath = item.getPath();
        String name = item.getName();
        boolean isFolder = item.isFolder();

        String s3BasePath = "serials/" + MyApp.DEVICE_SN + "/Exported/" + name;

        if (isFolder) {

            s3Manager.uploadFolderToS3(
                    localPath,
                    s3BasePath,
                    new S3ManagerSingleton.S3Callback() {

                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            runOnUiThread(() -> {
                                reportFileAdapter.setSelectedItem(-1);
                                reportFileAdapter.notifyDataSetChanged();
                                new CustomToast(PickReport.this, "Upload completed").show();
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() ->
                                    Log.e("S3UploadReport", "Upload error: " + e.getMessage(), e)
                            );
                        }
                    }
            );

        } else {
            // singolo file
            s3Manager.uploadFile(
                    localPath,
                    s3BasePath + name
            );
        }
    }

    public static List<UtcOffset> getAllUtcOffsets() {

        List<UtcOffset> list = new ArrayList<>();

        int[] special = {15, 30, 45};

        for (int h = -12; h <= 14; h++) {

            // ore intere
            list.add(new UtcOffset(
                    String.format("UTC%+03d:00", h),
                    h * 60
            ));

            // mezze / quarti (solo dove esistono davvero)
            if (h >= -11 && h <= 12) {
                for (int m : special) {
                    list.add(new UtcOffset(
                            String.format("UTC%+03d:%02d", h, m),
                            h * 60 + m
                    ));
                }
            }
        }

        return list;
    }

    private void showUtcPopupMenu(View anchor) {

        PopupMenu popup = new PopupMenu(this, anchor);

        for (int i = 0; i < offsets.size(); i++) {
            popup.getMenu().add(
                    0,
                    i,
                    i,
                    offsets.get(i).label
            );
        }

        popup.setOnMenuItemClickListener(item -> {

            UtcOffset sel = offsets.get(item.getItemId());

            int selectedOffsetMinutes = sel.minutes; // il numero intero che ti serve

            // Salva con MyData
            MyData.push("UTC_Offset", String.valueOf(selectedOffsetMinutes));
            DataSaved.UTC_Offset = selectedOffsetMinutes;

            // Aggiorna TextView
            tvUtcOffset.setText(sel.label);

            return true;
        });

        popup.show();
    }

    private String formatOffset(int minutes) {
        int h = minutes / 60;
        int m = Math.abs(minutes % 60);
        return String.format("UTC%+03d:%02d", h, m);
    }

}