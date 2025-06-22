package gui.buckets;

import static gui.MyApp.folderPath;
import static gui.MyApp.isApollo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;


import gui.dialogs_and_toast.CustomToast;
import services.UpdateValuesService;
import utils.MyData;

public class PickBucket extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView back, confirm;
    ArrayList<String> arrayFiles;
    BucketsAdapter bucketsAdapter;
    int indexMachineSelected;
    int indexBucket;
    int tiltFlag = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_picker);

        findView();
        init();
        onClick();
    }


    private void findView() {
        recyclerView = findViewById(R.id.recycler_view);
        back = findViewById(R.id.back);
        confirm = findViewById(R.id.confirm);
    }

    private void init() {
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexBucket = getIntent().getExtras().getInt("indexBucket");
        tiltFlag = MyData.get_Int("M" + indexMachineSelected + "_Tilt_MountPos" + indexBucket);
        arrayFiles = new ArrayList<>();

        if (tiltFlag == 0) {
            String path = Environment.getExternalStorageDirectory().toString() +folderPath+ "/Machines/Machine " + indexMachineSelected + "/Buckets";
            File directory = new File(path);
            File[] files = directory.listFiles();
            assert files != null;
            for (File file : files) {
                arrayFiles.add(file.getName());
            }
        } else {
            String path = Environment.getExternalStorageDirectory().toString() +folderPath+ "/Machines/Machine " + indexMachineSelected + "/Buckets Tilt";
            File directory = new File(path);
            File[] files = directory.listFiles();
            assert files != null;
            for (File file : files) {
                arrayFiles.add(file.getName());
            }
        }

        bucketsAdapter = new BucketsAdapter(arrayFiles);

        recyclerView.setAdapter(bucketsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(bucketsAdapter.getItemCount());
    }
    private void disableAll(){

        confirm.setEnabled(false);
        back.setEnabled(false);

    }

    private void onClick() {
        back.setOnClickListener((View v) -> {
            disableAll();
            if ((MyData.get_String("M" + indexBucket + "_Tilt_MountPos1").equals("0"))) {
                Intent intent = new Intent(this, BucketCalib.class);
                intent.putExtra("indexBucket", indexBucket);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            }
            else {
                Intent intent = new Intent(this, BucketCalibTilt.class);
                intent.putExtra("indexBucket", indexBucket);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            }

        });

        confirm.setOnClickListener((View v) -> {
            disableAll();
            if (tiltFlag == 0)
                saveBucket();
            else saveTilt();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, BucketChooserActivity.class));
            overridePendingTransition(0, 0);
            finish();

        });
    }

    private void saveBucket() {
        if (bucketsAdapter.getSelectedItem() == -1) {
            new CustomToast(this, getResources().getString(R.string.selectafile)).show();
        } else {
            String fileName = Environment.getExternalStorageDirectory().toString() +folderPath+ "/Machines/Machine " + indexMachineSelected + "/Buckets/" + arrayFiles.get(bucketsAdapter.getSelectedItem());
            try {
                CSVReader reader = new CSVReader(new FileReader(fileName));
                String[] bucket = reader.readNext();

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Name", bucket[0]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Length", bucket[1]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Width", bucket[2]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_L4", bucket[3]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Offset", bucket[4]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Flat_Offset", bucket[5]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Flat", bucket[6]);

                reader.close();
            }
            catch (Exception ignored) {}
        }
    }

    private void saveTilt() {

        if (bucketsAdapter.getSelectedItem() == -1) {
            new CustomToast(this, getResources().getString(R.string.selectafile)).show();
        }
        else {
            String fileName = Environment.getExternalStorageDirectory().toString() +folderPath+ "/Machines/Machine " + indexMachineSelected + "/Buckets Tilt/" + arrayFiles.get(bucketsAdapter.getSelectedItem());
            try {

                CSVReader reader = new CSVReader(new FileReader(fileName));

                String[] bucket = reader.readNext();

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Name", bucket[0]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Length", bucket[1]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Width", bucket[2]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_L4", bucket[3]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Offset", bucket[4]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Flat_Offset", bucket[5]);

                MyData.push("M" + indexMachineSelected + "_Bucket_" + indexBucket + "_Flat", bucket[6]);

                MyData.push("M" + indexMachineSelected + "_Tilt_Offset_Angle" + indexBucket, bucket[7]);

                MyData.push("M" + indexMachineSelected + "_Offset_DegWTilt" + indexBucket, bucket[8]);

                MyData.push("M" + indexMachineSelected + "_Tilt_piccolaBucket" + indexBucket, bucket[9]);


                reader.close();
            } catch (Exception ignored) {}
        }

    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

    }

}
