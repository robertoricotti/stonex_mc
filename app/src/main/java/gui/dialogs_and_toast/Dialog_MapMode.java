package gui.dialogs_and_toast;


import static packexcalib.surfcreator.TriangleHelper.MAX_NUMERO_FACCE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.List;

import dxf.DisplayItem;
import dxf.Point3D;
import dxf.Polyline;
import gui.projects.LayerAdapter;
import packexcalib.exca.DataSaved;
import services.TriangleService;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_MapMode {
    Activity activity;
    public Dialog dialog;
    double prog;

    TextView valore, massimo, minimo, medio;
    SeekBar seekBar;
    ImageView save;
    int colore, triangoli, punti, poly, testi, utils, json;
    RecyclerView recyclerViewLayers;


    public Dialog_MapMode(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {

        activity.stopService(new Intent(activity,TriangleService.class));
        dialog.create();
        dialog.setContentView(R.layout.dialog_map_mode);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 🔹 Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();


    }


    private void findView() {

        save = dialog.findViewById(R.id.exit);
        seekBar = dialog.findViewById(R.id.seekbar);
        valore = dialog.findViewById(R.id.txtraggio);
        massimo = dialog.findViewById(R.id.massimo);
        minimo = dialog.findViewById(R.id.minimo);
        medio = dialog.findViewById(R.id.medio);
        recyclerViewLayers = dialog.findViewById(R.id.recyclerViewLayers);


    }

    private void init() {

        if (MyData.get_String("Colore_Surf") == null) {
            MyData.push("Colore_Surf", "0");

        }
        if (MyData.get_String("Triangoli_Surf") == null) {
            MyData.push("Triangoli_Surf", "1");

        }
        if (MyData.get_String("Punti_Surf") == null) {
            MyData.push("Punti_Surf", "0");
        }
        if (MyData.get_String("Poly_Surf") == null) {
            MyData.push("Poly_Surf", "0");
        }
        if (MyData.get_String("Mostra_Testo") == null) {
            MyData.push("Mostra_Testo", "0");
        }
        if (MyData.get_String("Mostra_Utils") == null) {
            MyData.push("Mostra_Utils", "0");
        }
        if (MyData.get_String("Mostra_Json") == null) {
            MyData.push("Mostra_Json", "0");
        }

        colore = MyData.get_Int("Colore_Surf");
        triangoli = MyData.get_Int("Triangoli_Surf");
        punti = MyData.get_Int("Punti_Surf");
        poly = MyData.get_Int("Poly_Surf");
        testi = MyData.get_Int("Mostra_Testo");
        utils = MyData.get_Int("Mostra_Utils");
        json = MyData.get_Int("Mostra_Json");

        prog = DataSaved.RaggioDXF;

        if (DataSaved.RaggioDXF < 200) {
            seekBar.setProgress((int) DataSaved.RaggioDXF / 10);
            valore.setText(( Utils.readUnitOfMeasureLITE(String.valueOf((int)DataSaved.RaggioDXF)))+ "");
        } else {
            valore.setText("MAX FACES: "+MAX_NUMERO_FACCE);
            seekBar.setProgress((int) 100);
        }


        double medium = 0;
        try {
            medium = (TriangleService.maxZ + TriangleService.minZ) / 2;
        } catch (Exception e) {
            medium = 0;
        }

        massimo.setText(Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.maxZ)));
        minimo.setText(Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.minZ)));
        medio.setText(Utils.readUnitOfMeasureLITE(String.valueOf(medium)));
        // Configura il LayoutManager
        recyclerViewLayers.setLayoutManager(new LinearLayoutManager(activity));

        // Ottieni i dati raggruppati
        List<DisplayItem> groupedItems = LayerAdapter.getGroupedLayers();

        // Configura l'adapter
        LayerAdapter layerAdapter = new LayerAdapter(groupedItems);
        recyclerViewLayers.setAdapter(layerAdapter);


    }


    private void onClick() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                if (progress < 10) {
                    valore.setText(( Utils.readUnitOfMeasureLITE(String.valueOf((int)progress*10)))+ "");
                    //valore.setText(progress * 10 + "");
                    prog = progress * 10;
                } else {
                    valore.setText("MAX FACES: "+MAX_NUMERO_FACCE);
                    prog = 10000;
                }


            }
        });

        save.setOnClickListener(view -> {
            MyData.push("Colore_Surf", String.valueOf(colore));
            MyData.push("Triangoli_Surf", String.valueOf(triangoli));
            MyData.push("Punti_Surf", String.valueOf(punti));
            MyData.push("Poly_Surf", String.valueOf(poly));
            MyData.push("Mostra_Testo", String.valueOf(testi));
            MyData.push("Mostra_Utils", String.valueOf(utils));
            MyData.push("Mostra_Json", String.valueOf(json));
            MyData.push("showAlign", String.valueOf(DataSaved.showAlign));
            DataSaved.Colore_Surf = colore;
            DataSaved.Triangoli_Surf = triangoli;
            DataSaved.Punti_Surf = punti;
            DataSaved.Poly_Surf = poly;
            DataSaved.ShowText = testi;
            DataSaved.ShowUtils = utils;
            DataSaved.ShowJson = json;
            try {


                DataSaved.RaggioDXF = prog;

            } catch (Exception e) {
                e.printStackTrace();
            }

            dialog.dismiss();
           activity.startService(new Intent(activity,TriangleService.class));
            //activity.recreate();

        });

    }



}
