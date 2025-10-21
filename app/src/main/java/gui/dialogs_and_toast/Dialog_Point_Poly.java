package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private PointAdapter pointAdapter; // 👈 variabile di classe
    private PolylineAdapter polyAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isRepeating = false;
    double myStep;

    Activity activity;
    public Dialog dialog;
    RecyclerView recyclerView;
    ImageView  exit,img_search,img_clean;
    CheckBox ckBoxPOINT, ckBoxPOLY, ckNone;

    CustomQwertyDialog customQwertyDialog;
    int select = 0;
    int tmpAutoSnap = 0;

    Object selectedItem;
    String myS;
    LinearLayout layoff,cercaPunto;
    TextView lineTit;
    EditText valore,et_cercaPunto;
    Button piu, meno, clear, pm;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;

    int larg = 1000, alt = 600;
    DisplayMetrics displayMetrics;

    public Dialog_Point_Poly(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        customQwertyDialog = new CustomQwertyDialog(activity, null);
        customNumberDialog = new CustomNumberDialog(activity, -1);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -1);
        displayMetrics = new DisplayMetrics();


    }


    public void show() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        larg = (int) (displayMetrics.widthPixels * 0.9);
        alt = (int) (displayMetrics.heightPixels * 0.9);
        dialog.create();
        dialog.setContentView(R.layout.dialog_point_poly);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.75f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
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
        int myInt = MyData.get_Int("Unit_Of_Measure");

        switch (myInt) {
            case 0:
                myStep = 0.005;
                break;
            case 1:

                myStep = 0.005;
                break;

            case 2:
                myStep = 0.0033;
                break;
            case 3:
                myStep = 0.0033;
                break;

            case 4:
                myStep = 0.0033;
                break;
            case 5:
                myStep = 0.0033;
                break;
            case 6:
                myStep = 0.0033;
                break;
            case 7:
                myStep = 0.0033;
                break;
        }

        img_clean=dialog.findViewById(R.id.img_clean);
        img_search=dialog.findViewById(R.id.img_search);
        et_cercaPunto=dialog.findViewById(R.id.et_cercaPunto);
        cercaPunto=dialog.findViewById(R.id.cercaPunto);
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
                cercaPunto.setVisibility(View.INVISIBLE);
                break;
            case 1:
                ckNone.setChecked(false);
                ckBoxPOLY.setChecked(false);
                ckBoxPOINT.setChecked(true);
                layoff.setVisibility(View.INVISIBLE);
                cercaPunto.setVisibility(View.VISIBLE);
                break;

            case 2:
                ckNone.setChecked(false);
                ckBoxPOLY.setChecked(true);
                ckBoxPOINT.setChecked(false);
                layoff.setVisibility(View.VISIBLE);
                cercaPunto.setVisibility(View.INVISIBLE);
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
        img_search.setOnClickListener(view -> {
            if (pointAdapter != null) {
                String query = et_cercaPunto.getText().toString();
                pointAdapter.filter(query);
            }
        });

        img_clean.setOnClickListener(view -> {
            et_cercaPunto.setText("");
            if (pointAdapter != null) {
                pointAdapter.filter(""); // reset
            }
        });

        et_cercaPunto.setOnClickListener(view -> {
            if(!customQwertyDialog.dialog.isShowing()){
                customQwertyDialog.show(et_cercaPunto);
            }
        });
        valore.setOnClickListener(view -> {
            if (MyData.get_Int("Unit_Of_Measure") == 0 |
                    MyData.get_Int("Unit_Of_Measure") == 1 |
                    MyData.get_Int("Unit_Of_Measure") == 2 |
                    MyData.get_Int("Unit_Of_Measure") == 3 |
                    MyData.get_Int("Unit_Of_Measure") == 6 |
                    MyData.get_Int("Unit_Of_Measure") == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(valore);
                }
            } else {
                new CustomToast(activity,"Use + / -").show_alert();
            }

        });
        pm.setOnClickListener(view -> {
            if (DataSaved.line_Offset != 0) {
                DataSaved.line_Offset = DataSaved.line_Offset * -1;
                valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
            }
        });
        clear.setOnClickListener(view -> {

            DataSaved.line_Offset = 0;
            valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
        });
        setupAutoRepeat(piu,()->{
            DataSaved.line_Offset += myStep;
            valore.setText(Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.line_Offset)));
        });

        setupAutoRepeat(meno,()->{
            DataSaved.line_Offset -=myStep;
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


        exit.setOnClickListener(view -> {
            if (DataSaved.isAutoSnap == 2) {
                MyData.push("line_Offset", String.valueOf(DataSaved.line_Offset));
            }
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

                 pointAdapter = new PointAdapter(activePoints);
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

                 polyAdapter = new PolylineAdapter(activePolys);
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

        // 🔹 Caso speciale: i PNEZD sono sempre attivi
        if ("MyPNEZD".equals(layerName) && DataSaved.PNEZDPath != null) {
            return true;
        }

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

                 pointAdapter = new PointAdapter(activePoints);
                pointAdapter.setOnItemClickListener(point ->
                        Log.d("Dialog_Point_Poly", "Cliccato Punto: " + (point.getName() != null ? point.getName() : point.getId()))
                );
                pointAdapter.setOnItemLongClickListener(point -> {
                    DataSaved.lockUnlock = 1;
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

                 polyAdapter = new PolylineAdapter(activePolys);
                polyAdapter.setOnItemClickListener(poly ->
                        Log.d("Dialog_Point_Poly", "Cliccata Polilinea, Layer: " +
                                (poly.getLayer() != null ? poly.getLayer().getLayerName() : "Nessun layer") +
                                ", Vertici=" + poly.getVertexCount())
                );
                polyAdapter.setOnItemLongClickListener(poly -> {
                    DataSaved.lockUnlock = 1;
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


    private void setupAutoRepeat(Button button, Runnable action) {
        button.setOnClickListener(v -> action.run());

        button.setOnLongClickListener(v -> {
            isRepeating = true;

            // Primo ritardo di 500ms prima di iniziare la ripetizione
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRepeating) {
                        action.run();
                        handler.postDelayed(this, 50); // ripeti ogni 50ms
                    }
                }
            }, 500);

            return true; // segnala che il long click è gestito
        });

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isRepeating = false; // stop
                    break;
            }
            return false;
        });
    }


}



