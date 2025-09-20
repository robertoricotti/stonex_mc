package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.List;

import dxf.Layer;
import dxf.Point3D;
import dxf.PointAdapter;
import dxf.Polyline;
import dxf.PolylineAdapter;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Point_Poly {



    Activity activity;
    public Dialog dialog;
    RecyclerView recyclerView;
    ImageView save, exit;
    CheckBox ckBoxPOINT, ckBoxPOLY, ckNone;

    CustomQwertyDialog customQwertyDialog;
    int select = 0;
    int tmpAutoSnap = 0;

    Object selectedItem;
    String myS;
    LinearLayout layoff;
    TextView lineTit;
    EditText valore;
    Button piu, meno, clear, pm;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    int larg=1000,alt=600;
    DisplayMetrics displayMetrics;
    public Dialog_Point_Poly(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        customQwertyDialog = new CustomQwertyDialog(activity,null);
        customNumberDialog = new CustomNumberDialog(activity, -1);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -1);
        displayMetrics = new DisplayMetrics();


    }


    public void show() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        larg = (int) (displayMetrics.widthPixels * 0.9);
        alt = (int) (displayMetrics.heightPixels * 0.8);
        dialog.create();
        dialog.setContentView(R.layout.dialog_point_poly);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
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
        dialog.getWindow().setLayout(larg, alt);
        wlp.gravity = Gravity.CENTER;

        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        update();
        onClick();
        refreshRecyclerView();
    }

    private void findView() {

        save = dialog.findViewById(R.id.salva);
        exit = dialog.findViewById(R.id.dismiss);
        ckBoxPOINT = dialog.findViewById(R.id.ckAutoSnap);
        ckBoxPOLY = dialog.findViewById(R.id.ckAutoSnapPoly);
        ckNone = dialog.findViewById(R.id.ckNone);
        recyclerView = dialog.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        layoff = dialog.findViewById(R.id.lineoff_lay);
        lineTit = dialog.findViewById(R.id.lineTit);
        valore = dialog.findViewById(R.id.off_valore);
        piu = dialog.findViewById(R.id.offs_piu);
        meno = dialog.findViewById(R.id.offs_men);
        clear = dialog.findViewById(R.id.off_clear);
        pm = dialog.findViewById(R.id.off_pm);

    }

    private void init() {





    }

    private void update() {

        switch (DataSaved.isAutoSnap) {
            case 0:
                ckNone.setChecked(true);
                ckBoxPOLY.setChecked(false);
                ckBoxPOINT.setChecked(false);
                layoff.setVisibility(View.INVISIBLE);
                break;
            case 1:
                ckNone.setChecked(false);
                ckBoxPOLY.setChecked(false);
                ckBoxPOINT.setChecked(true);
                layoff.setVisibility(View.INVISIBLE);
                break;

            case 2:
                ckNone.setChecked(false);
                ckBoxPOLY.setChecked(true);
                ckBoxPOINT.setChecked(false);
                layoff.setVisibility(View.VISIBLE);
                break;
        }



        lineTit.setText("LINE OFFSET " + Utils.getMetriSimbol());
        valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
        populateList(
                DataSaved.isAutoSnap,   // passa direttamente il valore 0,1,2
                DataSaved.points,       // lista dei Point3D dal parser
                DataSaved.polylines     // lista delle Polyline dal parser
        );

    }


    private void onClick() {
        valore.setOnClickListener(view -> {
            if(MyData.get_Int("Unit_Of_Measure")==0|
                    MyData.get_Int("Unit_Of_Measure")==1|
                    MyData.get_Int("Unit_Of_Measure")==2|
                    MyData.get_Int("Unit_Of_Measure")==3|
                    MyData.get_Int("Unit_Of_Measure")==6|
                    MyData.get_Int("Unit_Of_Measure")==7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(valore);
                }
            }else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(valore);
                }
            }

        });
        pm.setOnClickListener(view -> {
            if (DataSaved.line_Offset != 0) {
                //DataSaved.lockUnlock=0;
                DataSaved.line_Offset = DataSaved.line_Offset * -1;
                valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
            }
        });
        clear.setOnClickListener(view -> {
            if(DataSaved.line_Offset!=0){
                DataSaved.lockUnlock=0;
            }
            DataSaved.line_Offset = 0;
            valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
        });
        piu.setOnClickListener(view -> {
            DataSaved.line_Offset += 0.01;
            valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
        });
        meno.setOnClickListener(view -> {
            DataSaved.line_Offset -= 0.01;
            valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
        });




        ckNone.setOnClickListener(view -> {
            DataSaved.isAutoSnap = 0;
            DataSaved.lockUnlock = 0;
            update();
            refreshRecyclerView();

        });
        ckBoxPOINT.setOnClickListener(view -> {

            DataSaved.isAutoSnap = 1;
            DataSaved.lockUnlock = 0;
            update();
            refreshRecyclerView();

        });
        ckBoxPOLY.setOnClickListener(view -> {
            DataSaved.isAutoSnap = 2;
            DataSaved.lockUnlock = 0;
            update();
            refreshRecyclerView();
        });

        save.setOnClickListener(view -> {
            if(DataSaved.isAutoSnap==2) {
                MyData.push("line_Offset", String.valueOf(DataSaved.line_Offset));
            }
            dialog.dismiss();
        });
        exit.setOnClickListener(view -> {
            dialog.dismiss();
        });


    }
    private void populateList(int enType, List<Point3D> points, List<Polyline> polylines) {
        switch (enType) {
            case 0: // niente
                recyclerView.setAdapter(null);
                break;

            case 1: // punti
                // Filtra solo punti con layer abilitato
                List<Point3D> activePoints = new ArrayList<>();
                for (Point3D p : points) {
                    if (p.getLayer() != null && p.getLayer().isEnable()) {
                        activePoints.add(p);
                    }
                }

                PointAdapter pointAdapter = new PointAdapter(activePoints);
                pointAdapter.setOnItemClickListener(point -> {
                    Log.d("Dialog_Point_Poly", "Cliccato Punto: " +
                            (point.getName() != null ? point.getName() : point.getId()));
                });
                recyclerView.setAdapter(pointAdapter);

                break;

            case 2: // polilinee
                // Filtra solo polilinee con layer abilitato
                List<Polyline> activePolys = new ArrayList<>();
                for (Polyline poly : polylines) {
                    if (poly.getLayer() != null && poly.getLayer().isEnable()) {
                        activePolys.add(poly);
                    }
                }

                PolylineAdapter polyAdapter = new PolylineAdapter(activePolys);
                polyAdapter.setOnItemClickListener(poly -> {
                    Log.d("Dialog_Point_Poly", "Cliccata Polilinea, Layer: " +
                            (poly.getLayer() != null ? poly.getLayer().getLayerName() : "Nessun layer") +
                            ", Vertici=" + poly.getVertexCount());
                });
                recyclerView.setAdapter(polyAdapter);

                break;

            default:
                recyclerView.setAdapter(null);
                break;
        }
    }

    // Metodo per verificare se il layer è abilitato (usa stesso criterio del canvas)
    private boolean isItemLayerEnabled(Layer layer) {
        if (layer == null) return false;
        String layerName = layer.getLayerName();
        if (layerName == null || layerName.isEmpty()) return false;

        for (Layer l : DataSaved.dxfLayers_DTM)
            if (layerName.equals(l.getLayerName()) && l.isEnable()) return true;
        for (Layer l : DataSaved.dxfLayers_POLY)
            if (layerName.equals(l.getLayerName()) && l.isEnable()) return true;
        for (Layer l : DataSaved.dxfLayers_POINT)
            if (layerName.equals(l.getLayerName()) && l.isEnable()) return true;

        return false;
    }

    private void refreshRecyclerView() {
        int enType = DataSaved.isAutoSnap;

        switch (enType) {
            case 0:
                recyclerView.setAdapter(null);
                break;

            case 1: // punti
            {
                List<Point3D> activePoints = new ArrayList<>();
                for (Point3D p : DataSaved.points) {
                    if (isItemLayerEnabled(p.getLayer())) activePoints.add(p);
                }

                PointAdapter pointAdapter = new PointAdapter(activePoints);
                pointAdapter.setOnItemClickListener(point ->
                        Log.d("Dialog_Point_Poly", "Cliccato Punto: " + (point.getName() != null ? point.getName() : point.getId()))
                );
                pointAdapter.setOnItemLongClickListener(point -> {
                    DataSaved.lockUnlock=1;
                    DataSaved.nearestPoint = point;
                    Log.d("Selezioni", DataSaved.selectedPoly + "  " + DataSaved.nearestPoint);
                });
                pointAdapter.setSelectedItem(DataSaved.nearestPoint); // evidenzia subito se esiste
                recyclerView.setAdapter(pointAdapter);
                if (DataSaved.nearestPoint != null) {
                    int index = activePoints.indexOf(DataSaved.nearestPoint);
                    if (index >= 0) {
                        recyclerView.scrollToPosition(index);
                    }
                } else {
                    recyclerView.scrollToPosition(0); // nessun punto selezionato → vai all’inizio
                }
            }
            break;

            case 2: // polilinee
            {
                List<Polyline> activePolys = new ArrayList<>();
                for (Polyline poly : DataSaved.polylines) {
                    if (isItemLayerEnabled(poly.getLayer())) activePolys.add(poly);
                }

                PolylineAdapter polyAdapter = new PolylineAdapter(activePolys);
                polyAdapter.setOnItemClickListener(poly ->
                        Log.d("Dialog_Point_Poly", "Cliccata Polilinea, Layer: " +
                                (poly.getLayer() != null ? poly.getLayer().getLayerName() : "Nessun layer") +
                                ", Vertici=" + poly.getVertexCount())
                );
                polyAdapter.setOnItemLongClickListener(poly -> {
                    DataSaved.lockUnlock=1;
                    DataSaved.selectedPoly = poly;
                    Log.d("Selezioni", DataSaved.selectedPoly + "  " + DataSaved.nearestPoint);
                });
                polyAdapter.setSelectedItem(DataSaved.selectedPoly); // evidenzia subito se esiste
                recyclerView.setAdapter(polyAdapter);
                if (DataSaved.selectedPoly != null) {
                    int index = activePolys.indexOf(DataSaved.selectedPoly);
                    if (index >= 0) {
                        recyclerView.scrollToPosition(index);
                    }
                } else {
                    recyclerView.scrollToPosition(0); // nessuna poly selezionata → vai all’inizio
                }

            }
            break;
        }
    }

}



