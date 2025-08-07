package gui.projects;

import static gui.MyApp.folderPath;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.io.File;
import java.util.ArrayList;

import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import services.UpdateValuesService;
import utils.MyData;


public class PickProject extends BaseClass {
    CustomQwertyDialog customQwertyDialog;
    Dialog_PRJ_Folder dialogPrjFolder;
    String path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects";

    RecyclerView recyclerView;
    ImageView back, confirm, rename;
    String fileName = "";
    ProjectFileAdapter projectAdapter;
    ArrayList<ProjectFileAdapter.FileItem> arrayFiles;
    TextView titolone;
    ImageView deletaFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_picker);
        findView();
        init();
        onClick();
    }


    private void findView() {
        recyclerView = findViewById(R.id.recycler_view);
        back = findViewById(R.id.back);
        confirm = findViewById(R.id.confirm);
        deletaFile = findViewById(R.id.deleteFile);
        rename = findViewById(R.id.imgCopy);
        titolone=findViewById(R.id.titolone);
        customQwertyDialog = new CustomQwertyDialog(this,null);

    }

    private void init() {

        arrayFiles = new ArrayList<>();
        path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects";
        File directory = new File(path);
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            boolean isFolder = file.isDirectory();
            long size = file.isDirectory() ? getFolderSize(file) : file.length();
            arrayFiles.add(new ProjectFileAdapter.FileItem(file.getName(), isFolder, size, file.getAbsolutePath()));
        }
        projectAdapter = new ProjectFileAdapter(arrayFiles);
        recyclerView.setAdapter(projectAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(projectAdapter.getItemCount());
        dialogPrjFolder = new Dialog_PRJ_Folder(this);


    }

    public void updateUI() {
        try {
            String s = MyData.get_String("progettoSelected");
            s = s.replace("/storage/emulated/0/StonexMachineControl", "");
            s = s.substring(0, s.lastIndexOf("/"));
            titolone.setText(s);
        } catch (Exception e) {
            titolone.setText(getResources().getString(R.string.selectproject));
        }
        try {

            if (projectAdapter.getSelectedItem() > -1) {
                deletaFile.setVisibility(View.VISIBLE);
                rename.setVisibility(View.VISIBLE);
                confirm.setVisibility(View.VISIBLE);
            } else {
                deletaFile.setVisibility(View.INVISIBLE);
                rename.setVisibility(View.INVISIBLE);
                confirm.setVisibility(View.INVISIBLE);
            }
        } catch (Exception ignored) {

        }
    }

    private void disableAll() {
        back.setEnabled(false);
        confirm.setEnabled(false);
        deletaFile.setEnabled(false);
        rename.setEnabled(false);
    }

    private void onClick() {
        rename.setOnClickListener(view -> {
            try {
                int selectedItem = projectAdapter.getSelectedItem();
                if (selectedItem == -1) {
                    new CustomToast(this, getResources().getString(R.string.select_file)).show();

                } else {

                    // Crea un nuovo AlertDialog.Builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(PickProject.this);
                    builder.setTitle(" " + getResources().getString(R.string.rename_file)+"?");
                    builder.setIcon(getResources().getDrawable(R.drawable.rename_96));


                    // Aggiungi il pulsante "Sì"
                    builder.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {


                        if (projectAdapter != null) {
                            if (projectAdapter.getSelectedItem() > -1) {
                                if (!customQwertyDialog.dialog.isShowing()) {
                                    customQwertyDialog.show(999, projectAdapter, path, projectAdapter.getSelectedFilePath());
                                }
                            }
                        }


                    });

                    // Aggiungi il pulsante "No"
                    builder.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {


                    });

                    // Mostra il dialog
                    builder.show();

                }
            } catch (Exception e) {
                new CustomToast(PickProject.this, getResources().getString(R.string.selectproject)).show();
            }



            /// ////////

        });
        back.setOnClickListener((View v) -> {

            disableAll();
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });

        confirm.setOnClickListener((View v) -> {
            try {
                if (projectAdapter.getSelectedItem() == -1) {
                    new CustomToast(this, getResources().getString(R.string.select_file)).show();
                } else {
                    if (arrayFiles.get(projectAdapter.getSelectedItem()).isFolder()) {

                        //gestisci  qui i file dentro la cartella

                        // Handle the case where the selected item is a folder
                        String m_folderPath = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects/" + arrayFiles.get(projectAdapter.getSelectedItem()).getName();
                        File directory = new File(m_folderPath);
                        File[] files = directory.listFiles();

                        if (files != null) {
                            ArrayList<String> dxfPstxFiles = new ArrayList<>();


                            for (File file : files) {
                                if (file.isFile()) {
                                    String fileName = file.getName();
                                    if (fileName.toLowerCase().endsWith(".dxf") || fileName.toLowerCase().endsWith(".pstx") || fileName.toLowerCase().endsWith(".xml") || fileName.toLowerCase().endsWith(".sp")|| fileName.toLowerCase().endsWith(".csv")) {
                                        dxfPstxFiles.add(fileName);
                                    }
                                }
                            }

                            for (String fileName : dxfPstxFiles) {
                                Log.d("FolderFiles", fileName);
                            }
                            if (!dialogPrjFolder.dialog.isShowing()) {
                                dialogPrjFolder.show(m_folderPath);
                            }


                        } else {
                            new CustomToast(this, "Folder is empty or cannot be accessed.").show_alert();
                        }


                    } else {
                        disableAll();

                        DataSaved.lockUnlock = 0; // Disable the point lock function before initializing a new project
                        fileName = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects/" + arrayFiles.get(projectAdapter.getSelectedItem()).getName();

                        MyData.push("progettoSelected", fileName);

                        startService(new Intent(this, UpdateValuesService.class));
                        startActivity(new Intent(this, Activity_Home_Page.class));
                        finish();
                    }
                }
            } catch (Exception e) {
                new CustomToast(this, "...").show();
            }
        });

        deletaFile.setOnClickListener(view -> {
            try {
                int selectedItem = projectAdapter.getSelectedItem();
                if (selectedItem == -1) {
                    new CustomToast(this, getResources().getString(R.string.select_file)).show();

                } else {

                    // Crea un nuovo AlertDialog.Builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(PickProject.this);
                    builder.setTitle(" " + getResources().getString(R.string.delete_file));
                    builder.setIcon(getResources().getDrawable(R.drawable.delete));


                    // Aggiungi il pulsante "Sì"
                    builder.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {


                        ProjectFileAdapter.FileItem selectedFileItem = arrayFiles.get(selectedItem);
                        fileName = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects/" + selectedFileItem.getName();

                        File file = new File(fileName);

                        if (selectedFileItem.isFolder()) {
                            // Delete the folder and its contents
                            deleteRecursive(file);
                        } else {
                            // Delete the single file
                            file.delete();
                        }

                        MyData.push("progettoSelected", "");
                        startService(new Intent(PickProject.this, UpdateValuesService.class));
                        arrayFiles.remove(selectedItem);
                        projectAdapter.notifyItemRemoved(selectedItem);
                        projectAdapter.notifyItemRangeChanged(selectedItem, arrayFiles.size());
                        projectAdapter.setSelectedItem(-1);
                        new CustomToast(PickProject.this, fileName + " " + getResources().getString(R.string.deleted)).show();



                    });

                    // Aggiungi il pulsante "No"
                    builder.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {


                    });

                    // Mostra il dialog
                    builder.show();

                }
            } catch (Exception e) {
                new CustomToast(PickProject.this, getResources().getString(R.string.selectproject)).show();
            }

        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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


}