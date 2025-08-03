package gui.projects;


import static gui.MyApp.geoidAll;
import static gui.dialogs_and_toast.Diaalog_Set_SP.getCrsCodeFromFileName;
import static utils.CanFileTransfer.sendFileViaCAN;
import static utils.CanFileTransfer.sendFileViaSerial;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomMenu;
import gui.dialogs_and_toast.CustomMenuLista;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Diaalog_Set_SP;
import gui.dialogs_and_toast.Dialog_Add_Surfaces;
import gui.my_opengl.My3DActivity;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.MyGeoide;
import serial.SerialPortManager;
import services.ReadProjectService;
import utils.CanFileTransfer;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_PRJ_Folder extends BaseClass {
    int perc = 0;
    Activity activity;
    public Dialog dialog;
    ImageView exit,setSP,setGeoide;
    TextView titFiles, messaggio,inUso,inUsoGeoid;
    View titSP;
    ImageView usaSP, usaFile, deleteFile, deleteSP,addSurf;
    RecyclerView recyclerViewFiles, recyclerViewSP;
    ArrayList<ProjectFileAdapter.FileItem> arraySP;
    ArrayList<ProjectAdapter.FileItem> arrayFiles;
    String mPath, fileName, filenamePoly, filenamePoint, filenameJson;
     ProjectAdapter projectAdapter;
     ProjectFileAdapter spAdapter;
    ProgressBar progressBar;
    boolean isUpdating = false;
    private Handler handler;
    Diaalog_Set_SP diaalogSetSp;
    Dialog_Add_Surfaces dialogAddSurfaces;


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
        inUso=dialog.findViewById(R.id.titoloSP);
        inUsoGeoid=dialog.findViewById(R.id.titoloGeo);
        titSP = dialog.findViewById(R.id.tvSP);
        recyclerViewFiles = dialog.findViewById(R.id.recycler_view);
        recyclerViewSP = dialog.findViewById(R.id.recycler_viewSP);
        usaFile = dialog.findViewById(R.id.usaFile);
        usaSP = dialog.findViewById(R.id.usaSP);
        deleteFile = dialog.findViewById(R.id.deleteFile);
        deleteSP = dialog.findViewById(R.id.deleteSP);
        setSP=dialog.findViewById(R.id.setSP);
        progressBar = dialog.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        messaggio = dialog.findViewById(R.id.msg);
        deleteFile.setVisibility(View.INVISIBLE);
        deleteSP.setVisibility(View.INVISIBLE);
        setGeoide=dialog.findViewById(R.id.geoidA);
        addSurf=dialog.findViewById(R.id.add_surf);
        diaalogSetSp=new Diaalog_Set_SP(activity);
        dialogAddSurfaces=new Dialog_Add_Surfaces(activity,mPath);

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
                    if (file.getName().toLowerCase().endsWith(".dxf") || file.getName().toLowerCase().endsWith(".xml") || file.getName().toLowerCase().endsWith(".pstx") || file.getName().toLowerCase().endsWith(".csv")) {
                        long size1 = file.isDirectory() ? getFolderSize(file) : file.length();
                        arrayFiles.add(new ProjectAdapter.FileItem(file.getName(), isFolder, size1));
                    }
                    if (file.getName().toLowerCase().endsWith(".sp")) {
                        long size = file.isDirectory() ? getFolderSize(file) : file.length();
                        arraySP.add(new ProjectFileAdapter.FileItem(file.getName(), isFolder, size,file.getAbsolutePath()));
                    }
                }
            } else {
                //new CustomToast(activity, "Reading File Error...").show_alert();
                dialog.dismiss();
            }
        } catch (Exception e) {
            new CustomToast(activity, "Reading File Error...\n"+Log.getStackTraceString(e)).show_error();
            dialog.dismiss();
        }

    }

    public void onClick() {
        addSurf.setOnClickListener(view -> {
            if(!dialogAddSurfaces.dialog.isShowing()){
                dialogAddSurfaces.show();
            }
        });
        setGeoide.setOnClickListener(view -> {
            CustomMenuLista customMenu = new CustomMenuLista(activity, "");

            if (geoidAll == null || geoidAll.length == 0) {
                new CustomToast(activity, "No Geoid Found").show_error();
                MyGeoide.setGeoid(null);
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
                        MyGeoide.setGeoid("null");
                        MyData.push("usaGeoide", String.valueOf(false));
                    } else {
                        MyGeoide.setGeoid(selectedItem);
                        MyData.push("usaGeoide", String.valueOf(true));
                    }
                }
            });
        });
        setSP.setOnClickListener(view -> {
            if(!diaalogSetSp.dialog.isShowing()){
                diaalogSetSp.show(mPath,spAdapter);
                dialog.dismiss();
            }
        });
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
                    Log.e("Orrore",Log.getStackTraceString(e));
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
                    Log.e("Orrore",Log.getStackTraceString(e));
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
                    new CustomToast(activity, activity.getString(R.string.wait_until)).show_long();
                    stopUpdating();

                    if(activity instanceof My3DActivity) {
                        activity.startService(new Intent(activity, ReadProjectService.class));
                    }else {
                        activity.startActivity(new Intent(activity, Activity_Home_Page.class));
                        activity.finish();
                    }
                    dialog.dismiss();

                } catch (Exception e) {
                    Log.e("Orrore",Log.getStackTraceString(e));
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

        deleteSP.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(" "+activity.getResources().getString(R.string.delete_file));
            builder.setIcon(activity.getResources().getDrawable(R.drawable.delete));

            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        int selectedItem = spAdapter.getSelectedItem();
                        if (selectedItem == -1) {
                            new CustomToast(activity, activity.getResources().getString(R.string.select_file)).show();

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
                        spAdapter.notifyItemRangeChanged(selectedItem, arrayFiles.size());
                        spAdapter.setItem(-1);
                    } catch (Exception e) {
                        new CustomToast(activity, activity.getResources().getString(R.string.selectproject)).show();
                        spAdapter.setItem(-1);
                    }
                }
            });
            builder.setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    spAdapter.setItem(-1);

                }
            });
            builder.show();

        });



        usaSP.setOnClickListener(view -> {

            if (spAdapter.getSelectedItem() == -1) {
                new CustomToast(activity, "SELECT A SP FILE TO USE").show();
            } else {

                String selectedFileName=arraySP.get(spAdapter.getSelectedItem()).getName();

                usaSP.setEnabled(false);
                Handler handler1 = new Handler(activity.getMainLooper());
                handler1.postDelayed(() -> {
                    try {
                        MyData.push("LastSP", selectedFileName);

                        String match=getCrsCodeFromFileName(selectedFileName);

                        if(match!=null){
                            if(match.equals("UTM")){
                                //UTM autozone
                                MyData.push("crs", "UTM");
                                DataSaved.S_CRS = MyData.get_String("crs");

                            }else {
                                MyData.push("crs", match);
                                DataSaved.S_CRS = MyData.get_String("crs");

                                ReadProjectService.startCRS();

                            }
                        }else {
                            //invia file SP
                            usaSP.setEnabled(false);
                            MyData.push("crs", ".SP FILE");
                            MyGeoide.setGeoid(null);
                            DataSaved.S_CRS = MyData.get_String("crs");
                            switch (DataSaved.my_comPort) {
                                case 0:
                                    // Copia il file da assets a una directory accessibile

                                    String filePath = spAdapter.getSelectedFilePathAbs();
                                    sendFileViaCAN(filePath, 0, 0x7DF, new CanFileTransfer.ProgressCallback() {
                                        @Override
                                        public void onProgressUpdate(int percentage) {
                                            perc = percentage;
                                        }
                                    });
                                    break;
                                case 1:
                                case 2:
                                    //send via serial
                                    String filePathS = spAdapter.getSelectedFilePathAbs();
                                    SerialPortManager.instance().sendCommand("SET,EXTERNAL.RECV_FILE,START\r\n");
                                    Thread.sleep(500);
                                    sendFileViaSerial(filePathS, new CanFileTransfer.ProgressCallback() {
                                        @Override
                                        public void onProgressUpdate(int percentage) {
                                            perc = percentage;
                                        }
                                    });
                                    break;
                                default:
                                    Thread.sleep(500);

                                    break;


                            }
                            usaSP.setEnabled(true);
                        }




                    } catch (Exception e) {
                        new CustomToast(activity, "SP ERROR").show_error();
                        Log.e("SP_ERROR", "Error sending file", e);
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
                    String s1 = "";
                    s1 = MyData.get_String("LastSP");
                    if (s1 == null) {
                        inUso.setText("");
                    } else {
                        inUso.setText(s1);
                    }

                    String s2 = "";
                    s2 = MyApp.GEOIDE_PATH;
                    if (s2 == null||s2.equals("null")) {
                        inUsoGeoid.setText("No Geoid");
                    } else {
                        inUsoGeoid.setText(s2);
                    }

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

                        if (projectAdapter.getSelectedItem() > -1) {
                            deleteFile.setVisibility(View.VISIBLE);
                        } else {
                            deleteFile.setVisibility(View.INVISIBLE);
                        }
                        if (spAdapter.getSelectedItem() > -1) {
                            deleteSP.setVisibility(View.VISIBLE);
                        } else {
                            deleteSP.setVisibility(View.INVISIBLE);
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
                    if (MyData.get_String("usaGeoide") == null) {
                        setGeoide.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_grigio));
                    }else {
                        if(MyData.get_String("usaGeoide").contains("true")){
                            setGeoide.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_selezionato));
                        }else {
                            setGeoide.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_grigio));
                        }
                    }
                } catch (Exception e) {

                    Log.e("Diomaiale", e.toString());
                }
            }
        }, 100);
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




}
