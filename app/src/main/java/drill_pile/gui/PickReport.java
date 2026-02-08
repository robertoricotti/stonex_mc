package drill_pile.gui;

import static gui.MyApp.folderPath;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.io.File;
import java.util.ArrayList;

import gui.boot_and_choose.Activity_Home_Page;


public class PickReport extends AppCompatActivity {
    String path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Exported";
    ImageView back;
    TextView titolone;

    RecyclerView recyclerView;
    ReportFileAdapter reportFileAdapter;
    ArrayList<ReportFileAdapter.FileItem> arrayFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_picker);
        findView();
        init();
        onClick();
    }

    private void findView() {
        recyclerView = findViewById(R.id.recycler_view);
        back = findViewById(R.id.back);
        titolone = findViewById(R.id.titolone);

    }
    private void init() {

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



    }
    private void onClick() {
        back.setOnClickListener((View v) -> {
            disableAll();
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });
    }
    private void disableAll(){

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