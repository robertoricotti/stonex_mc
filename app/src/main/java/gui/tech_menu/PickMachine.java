package gui.tech_menu;

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

public class PickMachine extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView back, confirm;
    MachinesAdapter machinesAdapter;
    ArrayList<String> arrayFiles;
    int indexMachineSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_picker);
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

        arrayFiles = new ArrayList<>();

        String path = Environment.getExternalStorageDirectory().toString() +folderPath +"/Machines/Machine " + indexMachineSelected + "/Config";
        File directory = new File(path);
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            arrayFiles.add(file.getName());
        }

        machinesAdapter = new MachinesAdapter(arrayFiles);

        recyclerView.setAdapter(machinesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(machinesAdapter.getItemCount());
    }

    private void onClick() {
        back.setOnClickListener((View v) -> {
            back.setEnabled(false);
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
        });

        confirm.setOnClickListener((View v) -> {
            if (machinesAdapter.getSelectedItem() == -1) {
                new CustomToast(this,"SELECT FILE!").show();

            }
            else {
               leggi_nuova();
            }
        });
    }

    public void leggi(){


            String fileName = Environment.getExternalStorageDirectory().toString() +folderPath +"/Machines/Machine " + indexMachineSelected + "/Config/" + arrayFiles.get(machinesAdapter.getSelectedItem());
            try {
                CSVReader reader = new CSVReader(new FileReader(fileName));


                String[] canType = reader.readNext();
                String[] boom1 = reader.readNext();
                String[] boom2 = reader.readNext();
                String[] stick = reader.readNext();
                String[] linkage = reader.readNext();
                String[] frame = reader.readNext();
                String[] tiltMountPos = reader.readNext();
                String[] benneTilt_Leng = reader.readNext();
                String[] benneTilt_Offset = reader.readNext();
                String[] gps1x=reader.readNext();
                String[] gps1y=reader.readNext();
                String[] gps1z=reader.readNext();
                String[] gps2=reader.readNext();
                String[] gpsT = reader.readNext();
                String[] com=reader.readNext();
                String[] wl=reader.readNext();
                String[] speed=reader.readNext();
                String [] radio=reader.readNext();
                String [] qcoupler=reader.readNext();
                String [] lingua=reader.readNext();


                MyData.push("M" + indexMachineSelected + "_Name", arrayFiles.get(machinesAdapter.getSelectedItem()).replace(".csv",""));
                MyData.push("M" + indexMachineSelected + "_useCanOpen", canType[0]);

                MyData.push("M" + indexMachineSelected + "_Boom1_MountPos", boom1[0]);

                MyData.push("M" + indexMachineSelected + "_LengthBoom1", boom1[1]);

                MyData.push("M" + indexMachineSelected + "_OffsetBoom1", boom1[2]);

                MyData.push("M" + indexMachineSelected + "_Boom2_MountPos", boom2[0]);

                MyData.push("M" + indexMachineSelected + "_LengthBoom2", boom2[1]);

                MyData.push("M" + indexMachineSelected + "_OffsetBoom2", boom2[2]);

                MyData.push("M" + indexMachineSelected + "_Stick_MountPos", stick[0]);

                MyData.push("M" + indexMachineSelected + "_LengthStick", stick[1]);

                MyData.push("M" + indexMachineSelected + "_OffsetStick", stick[2]);

                MyData.push("M" + indexMachineSelected + "_LaserVStick", stick[3]);

                MyData.push("M" + indexMachineSelected + "_LaserHStick", stick[4]);

                MyData.push("M" + indexMachineSelected + "_Bucket_MountPos", linkage[0]);

                MyData.push("M" + indexMachineSelected + "_LengthL1", linkage[1]);

                MyData.push("M" + indexMachineSelected + "_LengthL2", linkage[2]);

                MyData.push("M" + indexMachineSelected + "_LengthL3", linkage[3]);

                MyData.push("M" + indexMachineSelected + "_OffsetDB", linkage[4]);

                MyData.push("M" + indexMachineSelected + "_Frame_MountPos", frame[0]);

                MyData.push("M" + indexMachineSelected + "_LengthPitch", frame[1]);

                MyData.push("M" + indexMachineSelected + "_LengthRoll", frame[2]);

                MyData.push("M" + indexMachineSelected + "_OffsetFrameY", frame[3]);

                MyData.push("M" + indexMachineSelected + "_OffsetFrameX", frame[4]);

                for(int i = 0; i < tiltMountPos.length; i++){
                    MyData.push("M" + indexMachineSelected + "_Tilt_MountPos" + i, tiltMountPos[i]);
                    MyData.push("M" + indexMachineSelected + "_Tilt_Length" + i, benneTilt_Leng[i]);
                    MyData.push("M" + indexMachineSelected + "_Tilt_Offset" + i, benneTilt_Offset[i]);
                }
                MyData.push("M" + indexMachineSelected + "_OffsetGPSX", gps1x[0]);
                MyData.push("M" + indexMachineSelected + "_OffsetGPSY", gps1y[0]);
                MyData.push("M" + indexMachineSelected + "_OffsetGPSZ", gps1z[0]);
                MyData.push("M" + indexMachineSelected + "_OffsetGPS2", gps2[0]);

                MyData.push("M" + indexMachineSelected + "_sc600", gpsT[0]);
                MyData.push("M" + indexMachineSelected + "_comPort", com[0]);
                MyData.push("M" + indexMachineSelected + "_isWL", wl[0]);
                MyData.push("M" + indexMachineSelected + "reqSpeed", speed[0]);
                MyData.push("M" + indexMachineSelected + "radioMode", radio[0]);
                MyData.push("M" + indexMachineSelected + "_hasQuick", qcoupler[0]);
                MyData.push("language",lingua[0]);

                reader.close();

            }
            catch (Exception ignored) {}
            confirm.setEnabled(false);
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, Nuova_Machine_Settings.class));
            finish();
    }
    public void leggi_nuova(){


        String fileName = Environment.getExternalStorageDirectory().toString() +folderPath +"/Machines/Machine " + indexMachineSelected + "/Config/" + arrayFiles.get(machinesAdapter.getSelectedItem());
        try {
            CSVReader reader = new CSVReader(new FileReader(fileName));


            String[] canType = reader.readNext();
            String[] boom1 = reader.readNext();
            String[] boom2 = reader.readNext();
            String[] stick = reader.readNext();
            String[] linkage = reader.readNext();
            String[] frame = reader.readNext();
            String[] tiltMountPos = reader.readNext();
            String[] benneTilt_Leng = reader.readNext();
            String[] benneTilt_Offset = reader.readNext();
            String[] gps1x=reader.readNext();
            String[] gps1y=reader.readNext();
            String[] gps1z=reader.readNext();
            String[] gps2=reader.readNext();
            String[] gpsT = reader.readNext();
            String[] com=reader.readNext();
            String[] wl=reader.readNext();
            String[] speed=reader.readNext();
            String [] radio=reader.readNext();
            String [] qcoupler=reader.readNext();
            String [] lingua=reader.readNext();
            String [] wiR=reader.readNext();
            String [] wiL=reader.readNext();
            String [] palol=reader.readNext();
            String [] lamal=reader.readNext();
            String [] between=reader.readNext();
            String [] g1g2l=reader.readNext();


            MyData.push("M" + indexMachineSelected + "_Name", arrayFiles.get(machinesAdapter.getSelectedItem()).replace(".csv",""));
            MyData.push("M" + indexMachineSelected + "_useCanOpen", canType[1]);

            MyData.push("M" + indexMachineSelected + "_Boom1_MountPos", boom1[1]);

            MyData.push("M" + indexMachineSelected + "_LengthBoom1", boom1[2]);

            MyData.push("M" + indexMachineSelected + "_OffsetBoom1", boom1[3]);

            MyData.push("M" + indexMachineSelected + "_Boom2_MountPos", boom2[1]);

            MyData.push("M" + indexMachineSelected + "_LengthBoom2", boom2[2]);

            MyData.push("M" + indexMachineSelected + "_OffsetBoom2", boom2[3]);

            MyData.push("M" + indexMachineSelected + "_Stick_MountPos", stick[1]);

            MyData.push("M" + indexMachineSelected + "_LengthStick", stick[2]);

            MyData.push("M" + indexMachineSelected + "_OffsetStick", stick[3]);

            MyData.push("M" + indexMachineSelected + "_LaserVStick", stick[4]);

            MyData.push("M" + indexMachineSelected + "_LaserHStick", stick[5]);

            MyData.push("M" + indexMachineSelected + "_Bucket_MountPos", linkage[1]);

            MyData.push("M" + indexMachineSelected + "_LengthL1", linkage[2]);

            MyData.push("M" + indexMachineSelected + "_LengthL2", linkage[3]);

            MyData.push("M" + indexMachineSelected + "_LengthL3", linkage[4]);

            MyData.push("M" + indexMachineSelected + "_OffsetDB", linkage[5]);

            MyData.push("M" + indexMachineSelected + "_Frame_MountPos", frame[1]);

            MyData.push("M" + indexMachineSelected + "_LengthPitch", frame[2]);

            MyData.push("M" + indexMachineSelected + "_LengthRoll", frame[3]);

            MyData.push("M" + indexMachineSelected + "_OffsetFrameY", frame[4]);

            MyData.push("M" + indexMachineSelected + "_OffsetFrameX", frame[5]);

            for(int i = 0; i < tiltMountPos.length; i++){
                MyData.push("M" + indexMachineSelected + "_Tilt_MountPos" + i, tiltMountPos[i]);
                MyData.push("M" + indexMachineSelected + "_Tilt_Length" + i, benneTilt_Leng[i]);
                MyData.push("M" + indexMachineSelected + "_Tilt_Offset" + i, benneTilt_Offset[i]);
            }
            MyData.push("M" + indexMachineSelected + "_OffsetGPSX", gps1x[1]);
            MyData.push("M" + indexMachineSelected + "_OffsetGPSY", gps1y[1]);
            MyData.push("M" + indexMachineSelected + "_OffsetGPSZ", gps1z[1]);
            MyData.push("M" + indexMachineSelected + "_OffsetGPS2", gps2[1]);

            MyData.push("M" + indexMachineSelected + "_sc600", gpsT[1]);
            MyData.push("M" + indexMachineSelected + "_comPort", com[1]);
            MyData.push("M" + indexMachineSelected + "_isWL", wl[1]);
            MyData.push("M" + indexMachineSelected + "reqSpeed", speed[1]);
            MyData.push("M" + indexMachineSelected + "radioMode", radio[1]);
            MyData.push("M" + indexMachineSelected + "_hasQuick", qcoupler[1]);
            MyData.push("language",lingua[1]);
            MyData.push("M"+indexMachineSelected+ "_Bucket_" + "0" + "_Width_R",wiR[1]);
            MyData.push("M"+indexMachineSelected+ "_Bucket_" + "0" + "_Width_L",wiL[1]);
            MyData.push("M"+indexMachineSelected+ "_Bucket_" + "0" + "_Palo",palol[1]);
            MyData.push("M"+indexMachineSelected+ "_Bucket_" + "0" + "_Lama",lamal[1]);
            MyData.push("M"+indexMachineSelected+ "_Bucket_" + "0" + "_Between",between[1]);
            MyData.push("M" + indexMachineSelected + "_distG1_G2", g1g2l[1]);
            reader.close();

        }
        catch (Exception ignored) {
            leggi();
        }
        confirm.setEnabled(false);
        startService(new Intent(this, UpdateValuesService.class));
        startActivity(new Intent(this, Nuova_Machine_Settings.class));
        overridePendingTransition(0, 0);
        finish();
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {}
}
