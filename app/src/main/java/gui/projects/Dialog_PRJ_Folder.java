package gui.projects;


import static utils.CanFileTransfer.sendFileViaCAN;
import static utils.CanFileTransfer.sendFileViaSerial;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.io.File;
import java.util.ArrayList;

import gui.BaseClass;
import gui.boot_and_choose.ExcavatorMenuActivity;
import gui.boot_and_choose.Nuova_Choose;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import serial.SerialPortManager;
import utils.CanFileTransfer;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_PRJ_Folder extends BaseClass {
    int perc = 0;
    Activity activity;
    public Dialog dialog;
    Button exit;
    TextView titFiles, titSP, messaggio;
    ImageView usaSP, usaFile, deleteFile, deleteSP;
    RecyclerView recyclerViewFiles, recyclerViewSP;
    ArrayList<ProjectFileAdapter.FileItem> arraySP;
    ArrayList<ProjectAdapter.FileItem> arrayFiles;
    String mPath, fileName, filenamePoly, filenamePoint, filenameJson;
    ProjectAdapter projectAdapter;
    ProjectFileAdapter spAdapter;
    ProgressBar progressBar;
    boolean isUpdating = false;
    private Handler handler;


    public Dialog_PRJ_Folder(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    public void show(String path) {
        dialog.create();
        dialog.setContentView(R.layout.dialog_progect_folder);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        mPath = path;
        findView();
        init();
        onClick();
        startUpdating();

    }

    public void findView() {
        exit = dialog.findViewById(R.id.dismiss);
        titFiles = dialog.findViewById(R.id.tvPrj);
        titSP = dialog.findViewById(R.id.tvSP);
        recyclerViewFiles = dialog.findViewById(R.id.recycler_view);
        recyclerViewSP = dialog.findViewById(R.id.recycler_viewSP);
        usaFile = dialog.findViewById(R.id.usaFile);
        usaSP = dialog.findViewById(R.id.usaSP);
        deleteFile = dialog.findViewById(R.id.deleteFile);
        deleteSP = dialog.findViewById(R.id.deleteSP);
        progressBar = dialog.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        messaggio = dialog.findViewById(R.id.msg);
        deleteFile.setVisibility(View.INVISIBLE);
        deleteSP.setVisibility(View.INVISIBLE);

    }

    public void init() {
        titFiles.setText(mPath.substring(mPath.lastIndexOf("/Projects/")));
        arrayFiles = new ArrayList<>();
        arraySP = new ArrayList<>();
        sortFiles();
        projectAdapter = new ProjectAdapter(arrayFiles);
        spAdapter = new ProjectFileAdapter(arraySP);
        recyclerViewFiles.setAdapter(projectAdapter);
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(activity));
        recyclerViewFiles.setItemViewCacheSize(projectAdapter.getItemCount());
        recyclerViewSP.setAdapter(spAdapter);
        recyclerViewSP.setLayoutManager(new LinearLayoutManager(activity));
        recyclerViewSP.setItemViewCacheSize(spAdapter.getItemCount());


    }

    private void sortFiles() {
        try {
            File directory = new File(mPath);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    boolean isFolder = file.isDirectory();
                    if (file.getName().toLowerCase().endsWith(".dxf") || file.getName().toLowerCase().endsWith(".xml") || file.getName().toLowerCase().endsWith(".pstx") || file.getName().toLowerCase().endsWith(".geojson")) {
                        long size1 = file.isDirectory() ? getFolderSize(file) : file.length();
                        arrayFiles.add(new ProjectAdapter.FileItem(file.getName(), isFolder, size1));
                    }
                    if (file.getName().toLowerCase().endsWith(".sp")) {
                        long size = file.isDirectory() ? getFolderSize(file) : file.length();
                        arraySP.add(new ProjectFileAdapter.FileItem(file.getName(), isFolder, size));
                    }
                }
            } else {
                new CustomToast(activity, "Reading File Error...").show_error();
                dialog.dismiss();
            }
        } catch (Exception e) {
            new CustomToast(activity, "Reading File Error...").show_error();
            dialog.dismiss();
        }

    }

    public void onClick() {
        usaFile.setOnClickListener(view -> {
            if (projectAdapter.getSelectedCkTrmPosition() == -1) {
                new CustomToast(activity, "SELECT A TERRAIN MODEL TO USE").show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                recyclerViewSP.setVisibility(View.INVISIBLE);
                recyclerViewFiles.setVisibility(View.INVISIBLE);
                exit.setVisibility(View.INVISIBLE);
                usaFile.setVisibility(View.INVISIBLE);
                usaSP.setVisibility(View.INVISIBLE);
                try {
                    if (projectAdapter.getSelectedCkPolyPosition() != -1) {
                        filenamePoly = mPath + "/" + arrayFiles.get(projectAdapter.getSelectedCkPolyPosition()).getName();
                        MyData.push("progettoSelected_POLY", filenamePoly);

                    } else {
                        MyData.push("progettoSelected_POLY", "");
                    }
                } catch (Exception e) {
                    new CustomToast(activity, "CHECK FILE SELECTION!").show_alert();
                }
                try {
                    if (projectAdapter.getSelectedCkPoiPosition() != -1) {
                        filenamePoint = mPath + "/" + arrayFiles.get(projectAdapter.getSelectedCkPoiPosition()).getName();
                        MyData.push("progettoSelected_POINT", filenamePoint);
                    } else {
                        MyData.push("progettoSelected_POINT", "");
                    }
                } catch (Exception e) {
                    new CustomToast(activity, "CHECK FILE SELECTION!").show_alert();
                }

                DataSaved.lockUnlock = 0; // Disable the point lock function before initializing a new project
                try {
                    progressBar.setVisibility(View.VISIBLE);

                    fileName = mPath + "/" + arrayFiles.get(projectAdapter.getSelectedCkTrmPosition()).getName();
                    MyData.push("progettoSelected", fileName);
                    DataSaved.progettoSelected = MyData.get_String("progettoSelected");
                    DataSaved.progettoSelected_POLY = MyData.get_String("progettoSelected_POLY");
                    DataSaved.progettoSelected_POINT = MyData.get_String("progettoSelected_POINT");
                    new CustomToast(activity, "..Saved..").show_long();
                    stopUpdating();
                    activity.startActivity(new Intent(activity, ExcavatorMenuActivity.class));
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                    dialog.dismiss();

                } catch (Exception e) {
                    new CustomToast(activity, "CHECK FILE SELECTION!").show_alert();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });


        exit.setOnClickListener(view -> {
            stopUpdating();
            dialog.dismiss();
        });
        deleteFile.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(" "+activity.getResources().getString(R.string.delete_file));
            builder.setIcon(activity.getResources().getDrawable(R.drawable.delete));

            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        int selectedItem = projectAdapter.getSelectedItem();
                        if (selectedItem == -1) {
                            new CustomToast(activity, activity.getResources().getString(R.string.select_file)).show();

                        }

                        ProjectAdapter.FileItem selectedFileItem = arrayFiles.get(selectedItem);
                        fileName = mPath + "/" + selectedFileItem.getName();

                        File file = new File(fileName);

                        if (selectedFileItem.isFolder()) {
                            // Delete the folder and its contents
                            deleteRecursive(file);
                        } else {
                            // Delete the single file
                            file.delete();
                        }
                        new CustomToast(activity, activity.getResources().getString(R.string.deleted)).show();
                        arrayFiles.remove(selectedItem);
                        projectAdapter.notifyItemRemoved(selectedItem);
                        projectAdapter.notifyItemRangeChanged(selectedItem, arrayFiles.size());
                        projectAdapter.setItem(-1);
                    } catch (Exception e) {
                        new CustomToast(activity, activity.getResources().getString(R.string.selectproject)).show();
                        projectAdapter.setItem(-1);
                    }
                }
            });
            builder.setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    projectAdapter.setItem(-1);

                }
            });
            builder.show();

        });

        deleteSP.setOnLongClickListener(view -> {
            try {
                int selectedItem = spAdapter.getSelectedItem();
                if (selectedItem == -1) {
                    new CustomToast(activity, activity.getResources().getString(R.string.select_file)).show();
                    return false;
                }

                ProjectFileAdapter.FileItem selectedFileItem = arraySP.get(selectedItem);
                fileName = mPath + "/" + selectedFileItem.getName();

                File file = new File(fileName);

                if (selectedFileItem.isFolder()) {
                    // Delete the folder and its contents
                    deleteRecursive(file);
                } else {
                    // Delete the single file
                    file.delete();
                }
                new CustomToast(activity, activity.getResources().getString(R.string.deleted)).show();
                arraySP.remove(selectedItem);
                spAdapter.notifyItemRemoved(selectedItem);
                spAdapter.notifyItemRangeChanged(selectedItem, arraySP.size());
            } catch (Exception e) {
                new CustomToast(activity, activity.getResources().getString(R.string.selectproject)).show();
            }

            return false;
        });


        deleteSP.setOnClickListener(view -> {
            new CustomToast(activity, "Long Click to delet a file").show();

        });

        usaSP.setOnClickListener(view -> {


            if (spAdapter.getSelectedItem() == -1) {
                new CustomToast(activity, "SELECT A SP FILE TO USE").show();
            } else {

                usaSP.setEnabled(false);
                Handler handler1 = new Handler(activity.getMainLooper());
                handler1.postDelayed(() -> {
                    try {
                        MyData.push("crs", ".SP FILE");
                        DataSaved.S_CRS = MyData.get_String("crs");
                        switch (DataSaved.my_comPort) {
                            case 0:
                                String filename = mPath + "/" + spAdapter.getSelectedFilePath();
                                sendFileViaCAN(filename, 0, 0x7DF, new CanFileTransfer.ProgressCallback() {
                                    @Override
                                    public void onProgressUpdate(int percentage) {
                                        perc = percentage;
                                    }
                                });
                                break;

                            case 1:
                            case 2:
                                //send via serial
                                String filename1 = mPath + "/" + spAdapter.getSelectedFilePath();
                                SerialPortManager.instance().sendCommand("SET,EXTERNAL.RECV_FILE,START\r\n");
                                Thread.sleep(500);
                                sendFileViaSerial(filename1, new CanFileTransfer.ProgressCallback() {
                                    @Override
                                    public void onProgressUpdate(int percentage) {
                                        perc = percentage;
                                    }
                                });
                                break;
                        }


                    } catch (Exception e) {

                        new CustomToast(activity, "SP ERROR").show_error();
                    }


                }, 100); // 100 milliseconds = 2 seconds

                usaSP.setEnabled(true);
            }
        });


    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
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
                        deleteSP.setVisibility(View.INVISIBLE);
                        deleteFile.setVisibility(View.INVISIBLE);
                        recyclerViewSP.setVisibility(View.INVISIBLE);
                        recyclerViewFiles.setVisibility(View.INVISIBLE);
                        exit.setVisibility(View.INVISIBLE);
                        usaFile.setVisibility(View.INVISIBLE);
                        usaSP.setVisibility(View.INVISIBLE);

                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        messaggio.setVisibility(View.INVISIBLE);
                        deleteSP.setVisibility(View.INVISIBLE);
                        if (projectAdapter.getSelectedItem() > -1) {
                            deleteFile.setVisibility(View.VISIBLE);
                        } else {
                            deleteFile.setVisibility(View.INVISIBLE);
                        }
                        recyclerViewSP.setVisibility(View.VISIBLE);
                        recyclerViewFiles.setVisibility(View.VISIBLE);
                        exit.setVisibility(View.VISIBLE);
                        usaFile.setVisibility(View.VISIBLE);
                        usaSP.setVisibility(View.VISIBLE);
                    }

                    if (isUpdating) {
                        updateView();

                    }
                } catch (Exception e) {
                    Log.d("step!", e.toString());
                }
            }
        }, 100);
    }


}
