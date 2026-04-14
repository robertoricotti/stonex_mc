package gui.projects;


import static gui.MyApp.geoidAll;
import static gui.dialogs_and_toast.Diaalog_Set_SP.getCrsCodeFromFileName;
import static packexcalib.gnss.CRS_Strings._LOCAL_COORDINATES_FROM_GNSS;
import static utils.CanFileTransfer.sendFileViaCAN;
import static utils.CanFileTransfer.sendFileViaSerial;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.WHEELLOADER;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import drill_pile.gui.Drill_Activity;
import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomMenuLista;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Diaalog_Set_SP;
import gui.dialogs_and_toast.Dialog_Add_Surfaces;
import gui.my_opengl.My3DActivity;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.Deg2UTM;
import packexcalib.gnss.LocalizationFactory;
import serial.SerialPortManager;
import services.ReadProjectService;
import utils.CanFileTransfer;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_PRJ_Folder extends BaseClass {
    int perc = 0;
    Activity activity;
    public Dialog dialog;
    ImageView exit, setSP, setGeoide;
    TextView titFiles, messaggio, inUso, inUsoGeoid, textP;
    View titSP;
    ImageView usaSP, usaFile, deleteFile, deleteSP, addSurf;
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

    private Dialog_PRJ_Folder folderDialog;

    public Dialog_PRJ_Folder(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        folderDialog = this;

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
        inUso = dialog.findViewById(R.id.titoloSP);
        inUsoGeoid = dialog.findViewById(R.id.titoloGeo);
        titSP = dialog.findViewById(R.id.tvSP);
        recyclerViewFiles = dialog.findViewById(R.id.recycler_view);
        recyclerViewSP = dialog.findViewById(R.id.recycler_viewSP);
        usaFile = dialog.findViewById(R.id.usaFile);
        usaSP = dialog.findViewById(R.id.usaSP);
        deleteFile = dialog.findViewById(R.id.debugSerial);
        deleteSP = dialog.findViewById(R.id.deleteSP);
        setSP = dialog.findViewById(R.id.setSP);
        progressBar = dialog.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        messaggio = dialog.findViewById(R.id.msg);
        deleteFile.setVisibility(View.INVISIBLE);
        deleteSP.setVisibility(View.INVISIBLE);
        setGeoide = dialog.findViewById(R.id.geoidA);
        addSurf = dialog.findViewById(R.id.add_surf);
        textP = dialog.findViewById(R.id.textP);
        diaalogSetSp = new Diaalog_Set_SP(activity);
        dialogAddSurfaces = new Dialog_Add_Surfaces(activity, mPath);

    }

    public void init() {
        textP.setVisibility(View.GONE);
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
        if (DataSaved.isWL == DRILL) {
            addSurf.setVisibility(View.INVISIBLE);
        }


    }

    private void sortFiles() {
        try {
            File directory = new File(mPath);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    boolean isFolder = file.isDirectory();
                    if (file.getName().toLowerCase().endsWith(".dxf") || file.getName().toLowerCase().endsWith(".xml") || file.getName().toLowerCase().endsWith(".pstx") || file.getName().toLowerCase().endsWith(".csv")
                            || file.getName().toLowerCase().endsWith(".ird") || file.getName().toLowerCase().endsWith(".xls") || file.getName().toLowerCase().endsWith(".xlsx")) {
                        long size1 = file.isDirectory() ? getFolderSize(file) : file.length();
                        arrayFiles.add(new ProjectAdapter.FileItem(file.getName(), isFolder, size1));
                    }
                    if (file.getName().toLowerCase().endsWith(".sp") || file.getName().toLowerCase().endsWith(".loc") || file.getName().toLowerCase().endsWith(".lok")) {
                        long size = file.isDirectory() ? getFolderSize(file) : file.length();
                        arraySP.add(new ProjectFileAdapter.FileItem(file.getName(), isFolder, size, file.getAbsolutePath()));
                    }
                }
            } else {
                //new CustomToast(activity, "Reading File Error...").show_alert();
                dialog.dismiss();
            }
        } catch (Exception e) {
            new CustomToast(activity, "Reading File Error...\n" + Log.getStackTraceString(e)).show_error();
            dialog.dismiss();
        }

    }

    public void onClick() {

        textP.setOnClickListener(view -> {

            DataSaved.xyz_yxz += 1;
            DataSaved.xyz_yxz = DataSaved.xyz_yxz % 2;
            MyData.push("xyz", String.valueOf(DataSaved.xyz_yxz));
        });
        addSurf.setOnClickListener(view -> {
            if (!dialogAddSurfaces.dialog.isShowing()) {
                dialogAddSurfaces.show();
            }
        });
        setGeoide.setOnClickListener(view -> {
            CustomMenuLista customMenu = new CustomMenuLista(activity, "");

            if (geoidAll == null || geoidAll.length == 0) {
                new CustomToast(activity, "No Geoid Found").show_error();
                MyData.push("geoidPath", null);
                MyApp.GEOIDE_PATH = null;
                return;
            }

            // Lista nomi visibili
            List<String> menuItemsL = new ArrayList<>();

            // Mappa nome -> path completo
            Map<String, String> geoidMap = new HashMap<>();

            String disabled = activity.getResources().getString(R.string.disabled);
            menuItemsL.add(disabled);

            for (String fullPath : geoidAll) {
                String fileName = new File(fullPath).getName(); // 👈 solo nome file
                menuItemsL.add(fileName);
                geoidMap.put(fileName, fullPath); // 👈 salva associazione
            }

            customMenu.show(menuItemsL, selectedItem -> {
                new CustomToast(activity, "Geoid: " + selectedItem).show_added();

                if (selectedItem.equals(disabled)) {
                    MyData.push("geoidPath", "null");
                    MyApp.GEOIDE_PATH = null;
                } else {
                    String fullPath = geoidMap.get(selectedItem); // 👈 recupera path reale
                    MyData.push("geoidPath", fullPath);
                    MyApp.GEOIDE_PATH = fullPath;
                }
            });
        });

//        setGeoide.setOnClickListener(view -> {
//            CustomMenuLista customMenu = new CustomMenuLista(activity, "");
//
//            if (geoidAll == null || geoidAll.length == 0) {
//                new CustomToast(activity, "No Geoid Found").show_error();
//                MyData.push("geoidPath",null);
//                MyApp.GEOIDE_PATH=null;
//                return;
//            }
//
//            // Crea un nuovo array con spazio per "DISABLED" + tutti gli elementi di geoidAll
//            String[] menuItems = new String[geoidAll.length + 1];
//            menuItems[0] = activity.getResources().getString(R.string.disabled);
//            System.arraycopy(geoidAll, 0, menuItems, 1, geoidAll.length);
//
//            List<String> menuItemsL = Arrays.asList(menuItems);
//
//            customMenu.show(menuItemsL, new CustomMenu.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(String selectedItem) {
//                    new CustomToast(activity, "Geoid: " + selectedItem).show_added();
//                    if (selectedItem.equals(activity.getResources().getString(R.string.disabled))) {
//                        MyData.push("geoidPath","null");
//                        MyApp.GEOIDE_PATH=null;
//                    } else {
//                        MyData.push("geoidPath",selectedItem);
//                        MyApp.GEOIDE_PATH=selectedItem;
//                    }
//                }
//            });
//        });
        setSP.setOnClickListener(view -> {
            if (!diaalogSetSp.dialog.isShowing()) {
                diaalogSetSp.show(mPath, spAdapter);

                // Qui aggiungi il listener di chiusura
                diaalogSetSp.dialog.setOnDismissListener(d -> {
                    refreshSPAdapter();
                });
            }
        });

        usaFile.setOnClickListener(view -> {
            if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER || DataSaved.isWL == DOZER || DataSaved.isWL == GRADER || DataSaved.isWL == DOZER_SIX) {
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
                        Log.e("Orrore", Log.getStackTraceString(e));
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
                        Log.e("Orrore", Log.getStackTraceString(e));
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

                        if (activity instanceof My3DActivity) {
                            activity.startService(new Intent(activity, ReadProjectService.class));
                        } else {
                            activity.startActivity(new Intent(activity, Activity_Home_Page.class));
                            activity.finish();
                        }
                        dialog.dismiss();

                    } catch (Exception e) {
                        Log.e("Orrore", Log.getStackTraceString(e));
                        new CustomToast(activity, "CHECK FILE SELECTION!").show_alert();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            } else if (DataSaved.isWL == DRILL) {
                if (projectAdapter.getSelectedCkPoiPosition() == -1) {
                    new CustomToast(activity, "SELECT A POINT FILE TO USE").show();
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
                        Log.e("Orrore", Log.getStackTraceString(e));
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
                        Log.e("Orrore", Log.getStackTraceString(e));
                        new CustomToast(activity, "CHECK FILE SELECTION!").show_alert();
                    }

                    DataSaved.lockUnlock = 0; // Disable the point lock function before initializing a new project
                    try {
                        progressBar.setVisibility(View.VISIBLE);

                        fileName = mPath + "/" + arrayFiles.get(projectAdapter.getSelectedCkPoiPosition()).getName();
                        MyData.push("progettoSelected", fileName);
                        DataSaved.progettoSelected = MyData.get_String("progettoSelected");
                        DataSaved.progettoSelected_POLY = MyData.get_String("progettoSelected_POLY");
                        DataSaved.progettoSelected_POINT = MyData.get_String("progettoSelected_POINT");
                        new CustomToast(activity, activity.getString(R.string.wait_until)).show_long();
                        stopUpdating();

                        if (activity instanceof Drill_Activity) {
                            activity.startService(new Intent(activity, ReadProjectService.class));
                        } else {
                            activity.startActivity(new Intent(activity, Activity_Home_Page.class));
                            activity.finish();
                        }
                        dialog.dismiss();

                    } catch (Exception e) {
                        Log.e("Orrore", Log.getStackTraceString(e));
                        new CustomToast(activity, "CHECK FILE SELECTION!").show_alert();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
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
            builder.setTitle(" " + activity.getResources().getString(R.string.delete_file));
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
            builder.setTitle(" " + activity.getResources().getString(R.string.delete_file));
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

                String selectedFileName = arraySP.get(spAdapter.getSelectedItem()).getName();

                usaSP.setEnabled(false);
                Handler handler1 = new Handler(activity.getMainLooper());
                handler1.postDelayed(() -> {
                    try {
                        MyData.push("LastSP", selectedFileName);

                        String match = getCrsCodeFromFileName(selectedFileName);


                        if (match != null) {
                            if (match.equals("UTM")) {
                                //UTM autozone
                                MyData.push("crs", "UTM");
                                DataSaved.S_CRS = MyData.get_String("crs");

                            } else {
                                MyData.push("crs", match);
                                DataSaved.S_CRS = MyData.get_String("crs");
                                ReadProjectService.startCRS();

                            }
                        } else {
                            //invia file SP
                            usaSP.setEnabled(false);
                            if (MyData.get_String("crs").equals(_LOCAL_COORDINATES_FROM_GNSS)) {

                                MyData.push("crs", "_LOCAL_COORDINATES_FROM_GNSS");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                try {
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


                                    }
                                } catch (InterruptedException ignored) {

                                }
                                Log.d("TESTR", "invia file SP " + DataSaved.S_CRS);
                            } else {
                                MyData.push("CRS_ESTERNO", spAdapter.getSelectedFilePathAbs());
                                MyData.push("crs", ".SP FILE");
                                DataSaved.S_CRS = MyData.get_String("crs");
                                Log.d("TESTR", "Usa file SP " + DataSaved.S_CRS);
                            }
                            ReadProjectService.model = LocalizationFactory.fromFile(new File(spAdapter.getSelectedFilePathAbs()),
                                    Deg2UTM.nativeProjTransformer, Deg2UTM.nativeProjTransformerToGeo);
                            usaSP.setEnabled(true);

                        }


                    } catch (Exception e) {
                        new CustomToast(activity, "SP ERROR").show_error();
                        Log.e("SP_ERROR", "Error sending file", e);
                    }


                }, 100); // 100 milliseconds = 2 seconds

                usaSP.setEnabled(true);
                new CustomToast(activity, "SP SELECTED").show_alert();
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

                    if (DataSaved.isWL == DRILL) {
                        textP.setVisibility(View.VISIBLE);
                        addSurf.setVisibility(View.GONE);
                        if (DataSaved.xyz_yxz == 0) {
                            textP.setText("P-E-N-Z-D");
                        } else {
                            textP.setText("P-N-E-Z-D");
                        }
                    } else {
                        addSurf.setVisibility(View.VISIBLE);
                        textP.setVisibility(View.GONE);
                    }
                    String s1 = "";
                    s1 = MyData.get_String("LastSP");
                    if (s1 == null) {
                        inUso.setText("");
                    } else {
                        inUso.setText(s1);
                    }


                    if (MyApp.GEOIDE_PATH == null || MyApp.GEOIDE_PATH.equals("null") || MyApp.GEOIDE_PATH.isEmpty()) {
                        inUsoGeoid.setText("No Geoid");
                    } else {
                        inUsoGeoid.setText(MyApp.GEOIDE_PATH);
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

                    if (MyApp.GEOIDE_PATH == null || MyApp.GEOIDE_PATH.equals("null") || MyApp.GEOIDE_PATH.isEmpty()) {
                        setGeoide.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_grigio));
                        // Log.d("GoidStatus", "null");
                    } else {
                        setGeoide.setBackground(activity.getResources().getDrawable(R.drawable.sfondo_bottone_selezionato));
                        // Log.d("GoidStatus", MyApp.GEOIDE_PATH.toString());
                    }
                    if (isUpdating) {
                        updateView();

                    }

                } catch (Exception e) {

                    Log.e("GoidStatus", Log.getStackTraceString(e));
                }
            }
        }, 100);
    }


    public void refreshSPAdapter() {
        if (mPath == null || mPath.isEmpty()) {
            Log.e("refreshSPAdapter", "mPath non inizializzato");
            return;
        }

        ArrayList<ProjectFileAdapter.FileItem> nuovaLista = new ArrayList<>();

        try {
            File directory = new File(mPath);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().toLowerCase().endsWith(".sp") || file.getName().toLowerCase().endsWith(".loc") || file.getName().toLowerCase().endsWith(".lok")) {
                        boolean isFolder = file.isDirectory();
                        long size = isFolder ? getFolderSize(file) : file.length();
                        nuovaLista.add(new ProjectFileAdapter.FileItem(
                                file.getName(), isFolder, size, file.getAbsolutePath()
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("refreshSPAdapter", Log.getStackTraceString(e));
        }

        // Aggiorna la RecyclerView sul main thread
        activity.runOnUiThread(() -> {
            if (spAdapter != null) {
                spAdapter.updateItems(nuovaLista);
            } else {
                spAdapter = new ProjectFileAdapter(nuovaLista);
                recyclerViewSP.setAdapter(spAdapter);
                recyclerViewSP.setLayoutManager(new LinearLayoutManager(activity));
            }
        });
    }


}
