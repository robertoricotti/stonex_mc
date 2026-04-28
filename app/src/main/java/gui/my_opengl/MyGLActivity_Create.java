package gui.my_opengl;

import static gui.my_opengl.My3DActivity.glVista3d;
import static gui.my_opengl.My3DActivity.isPan;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.Arrays;

import dxf.Point3D;
import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.HeadingDialog;
import gui.draw_class.MyColorClass;
import gui.projects.Dialog_Edita_Punti3D;
import gui.projects.Dialog_Parametri_AB;
import gui.projects.Dialog_Trench;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;
import services.TriangleService;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

/**
 * OpenGL surface creator.
 * Non-invasive version: original GL/machine/sidebar logic is preserved;
 * only add/remove/edit/save are connected to CreateSurfaceController.
 */
public class MyGLActivity_Create extends BaseClass {

    private static final String TAG = "GL_CREATE_ACTIVITY";

    public static double[] spigoloSelezionato = new double[3];

    String proj = null;
    String type = null;
    String percorso = null;
    String whoPROJ = null;

    MyGLSurfaceView_Create glSurfaceViewCreate;
    CreateSurfaceController createController;
    CreateSurfaceSaveDialog createSurfaceSaveDialog;

    ImageView digMenu, bucketEdgeL, bucketEdgeC, bucketEdgeR, add_point, remove_point, edit_point, salva;
    ImageView gl_facce, gl_fill, gl_poly, gl_point, gl_text, navigatorHDT, gpsStat, statoImg;
    ImageView gl_2d3d;

    public static boolean gFacce, gFill, gPoly, gPoint, gText;

    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    TextView mostraCoor;
    HeadingDialog headingDialog;
    Dialog_Parametri_AB dialogParametriAB;
    Dialog_Trench dialogTrench;

    boolean addPRJ = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CreateSurfaceController.resetCreateData();
        readIntent();
        initCreateToggles();

        setContentView(R.layout.activity_my_glactivity);
        DataSaved.showAlign = 0;
        try {
            startService(new Intent(this, TriangleService.class));
        } catch (Exception e) {
            Log.e(TAG, "Unable to start TriangleService", e);
        }

        findView();
        initCameraFromMemory();
        initCreateWorkflow();
        onClick();
        updateUI();
    }

    private void readIntent() {
        try {
            proj = getIntent().getStringExtra("proj");
        } catch (Exception ignored) {
            proj = null;
        }
        try {
            type = getIntent().getStringExtra("type");
        } catch (Exception ignored) {
            type = null;
        }
        try {
            percorso = getIntent().getStringExtra("mPath");
        } catch (Exception ignored) {
            percorso = null;
        }
        try {
            whoPROJ = getIntent().getStringExtra("whoPRJ");
        } catch (Exception ignored) {
            whoPROJ = null;
        }
        addPRJ = "OVER".equalsIgnoreCase(type);
        Log.d("ProjectType", proj + "\n" + type + "\n" + percorso + "\n" + whoPROJ);
    }

    private void initCreateToggles() {
        gFacce = true;
        gFill = true;
        gPoly = true;
        gPoint = true;
        gText = true;
    }

    private void findView() {
        glSurfaceViewCreate = findViewById(R.id.glSurfaceViewCreate);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        headingDialog = new HeadingDialog(this);

        mostraCoor = findViewById(R.id.mostraCoor);
        statoImg = findViewById(R.id.statoImg);
        gpsStat = findViewById(R.id.gpsStat);
        navigatorHDT = findViewById(R.id.navigatorHDT);
        digMenu = findViewById(R.id.digMenu);
        bucketEdgeL = findViewById(R.id.bucketEdgeL);
        bucketEdgeC = findViewById(R.id.bucketEdgeC);
        bucketEdgeR = findViewById(R.id.bucketEdgeR);
        add_point = findViewById(R.id.add_point);
        remove_point = findViewById(R.id.remove_point);
        edit_point = findViewById(R.id.edit_point);
        salva = findViewById(R.id.salva);
        gl_facce = findViewById(R.id.gl_facce);
        gl_fill = findViewById(R.id.gl_fill);
        gl_poly = findViewById(R.id.gl_poly);
        gl_point = findViewById(R.id.gl_point);
        gl_text = findViewById(R.id.gl_text);
        gl_2d3d = findViewById(R.id.gl_2d3d);

    }


    private void initCameraFromMemory() {
        try {
            MyGLRenderer.scale = MyData.get_Float("glScale");
            MyGLRenderer.angleX = MyData.get_Float("glAngleX");
            MyGLRenderer.angleY = MyData.get_Float("glAngleY");
            MyGLRenderer.panX = 0f;
            MyGLRenderer.panY = -0.3f;
            MyGLRenderer.angleY_extra = MyData.get_Float("glAngleY_Extra");
            MyGLRenderer.scale_2d = MyData.get_Float("glScale_2d");
        } catch (Exception e) {
            MyGLRenderer.scale_2d = 0.5f;
            MyGLRenderer.scale = 0.5f;
            MyGLRenderer.angleX = -90f;
            MyGLRenderer.angleY = 0f;
            MyGLRenderer.panX = 0f;
            MyGLRenderer.panY = -0.3f;
            MyGLRenderer.angleY_extra = 0.0f;
        }

        if (MyGLRenderer.scale < 0.09f) MyGLRenderer.scale = 0.09f;
        if (glVista3d != 0 && glVista3d != 1) glVista3d = 1;
        if (DataSaved.typeView != 0 && DataSaved.typeView != 1) DataSaved.typeView = 1;
    }

    private void initCreateWorkflow() {
        createController = new CreateSurfaceController(proj);
        createSurfaceSaveDialog = new CreateSurfaceSaveDialog(this, createController, addPRJ, percorso);
    }

    private void onClick() {
        bucketEdgeL.setOnClickListener(v -> {
            DataSaved.bucketEdge = -1;
            spigoloSelezionato = ExcavatorLib.bucketLeftCoord;
            requestGlRender();
            updateUI();
        });

        bucketEdgeC.setOnClickListener(v -> {
            DataSaved.bucketEdge = 0;
            spigoloSelezionato = ExcavatorLib.bucketCoord;
            requestGlRender();
            updateUI();
        });

        bucketEdgeR.setOnClickListener(v -> {
            DataSaved.bucketEdge = 1;
            spigoloSelezionato = ExcavatorLib.bucketRightCoord;
            requestGlRender();
            updateUI();
        });
        add_point.setOnClickListener(v -> addCreatePoint());
        remove_point.setOnClickListener(v -> removeCreatePoint());
        edit_point.setOnClickListener(v -> editCreateData());
        salva.setOnClickListener(v -> saveCreateSurface());

        if (gpsStat != null) {
            gpsStat.setOnClickListener(v -> {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) dialogGnssCoordinates.show();
            });
        }

        if (gl_2d3d != null) {
            gl_2d3d.setOnClickListener(v -> {
                glVista3d += 1;
                glVista3d = glVista3d % 2;
                requestGlRender();
                updateUI();
            });
        }

        gl_facce.setOnClickListener(v -> {
            gFacce = !gFacce;
            requestGlRender();
            updateUI();
        });
        gl_fill.setOnClickListener(v -> {
            gFill = !gFill;
            requestGlRender();
            updateUI();
        });

        gl_poly.setOnClickListener(v -> {
            gPoly = !gPoly;
            requestGlRender();
            updateUI();
        });
        gl_point.setOnClickListener(v -> {
            gPoint = !gPoint;
            requestGlRender();
            updateUI();
        });
        gl_text.setOnClickListener(v -> {
            gText = !gText;
            requestGlRender();
            updateUI();
        });

        digMenu.setOnClickListener(v -> setAct());
    }

    private void addCreatePoint() {
        if (createController == null) return;
        if (!createController.canAddPoint()) {
            if (createController.getMode() == CreateSurfaceController.MODE_PLAN) {
                new CustomToast(this, "PLAN: 1pt Max").show_alert();
            } else if (createController.getMode() == CreateSurfaceController.MODE_AB) {
                new CustomToast(this, "AB: 2pts Max").show_alert();
            } else {
                new CustomToast(this, "Point Not Added").show_alert();
            }
            return;
        }
        if (createController.addCurrentMachinePoint()) {
            new CustomToast(this, getResources().getString(R.string.punto_salvato)).show_added();
            requestGlRender();
            updateUI();
        } else {
            new CustomToast(this, "Invalid Coordinates").show_error();
        }
    }

    private void removeCreatePoint() {
        if (createController == null) return;
        if (createController.removeLastPoint()) {
            new CustomToast(this, "POINT REMOVED").show();
        }
        requestGlRender();
        updateUI();
    }

    private void editCreateData() {
        if (createController == null) return;
        switch (createController.getMode()) {
            case CreateSurfaceController.MODE_PLAN:
                showPlanSizeDialog();
                break;
            case CreateSurfaceController.MODE_AB:
                if (createController.getPickedCount() < 2) {
                    new CustomToast(this, "AB: 2pts are needed").show_alert();
                    return;
                }
                createController.syncLegacyStatics();
                createController.prepareABDialog();
                dialogParametriAB = new Dialog_Parametri_AB(this);
                dialogParametriAB.show();
                dialogParametriAB.dialog.setOnDismissListener(d -> {
                    createController.syncABParamsFromLegacy();
                    requestGlRender();
                    updateUI();
                });
                break;
            case CreateSurfaceController.MODE_TRENCH:
                if (createController.getPickedCount() < 2) {
                    new CustomToast(this, "TRENCH: 2pts Min").show_alert();
                    return;
                }
                createController.syncLegacyStatics();
                dialogTrench = new Dialog_Trench(this, createController.getTrenchOrTrianglePoints());
                dialogTrench.show();
                dialogTrench.dialog.setOnDismissListener(d -> {
                    createController.syncTrenchParamsFromLegacy();
                    requestGlRender();
                    updateUI();
                });
                break;
            case CreateSurfaceController.MODE_AREA:
            case CreateSurfaceController.MODE_TRIANGLES:
            default:
                showEditPointsDialog();
                break;
        }
    }

    private void showPlanSizeDialog() {
        final EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setText(Utils.readUnitOfMeasureLITE(String.valueOf(createController.getPlanSide())));
        editText.setSelectAllOnFocus(true);

        editText.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL

        );

        new AlertDialog.Builder(this)
                .setTitle("PLAN side")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    FullscreenActivity.setFullScreen(this);
                    try {
                        double side = Double.parseDouble(Utils.writeMetri(editText.getText().toString()));
                        createController.setPlanSide(side);
                        requestGlRender();
                        updateUI();
                    } catch (Exception e) {
                        new CustomToast(this, "Invalid Value").show_error();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

    }

    private void showEditPointsDialog() {
        Point3D[] editArray = createController.pickedArray();
        Dialog_Edita_Punti3D dialogEditaPunti3D = new Dialog_Edita_Punti3D(this, editArray);
        dialogEditaPunti3D.show();
        Dialog_Edita_Punti3D.dialog.setOnDismissListener(d -> {
            createController.replacePickedPoints(editArray);
            requestGlRender();
            updateUI();
        });
    }

    private void saveCreateSurface() {
        if (createController == null || createSurfaceSaveDialog == null) return;
        if (!createController.isReadyToSave()) {
            new CustomToast(this, "Invalid Surface").show_alert();
            return;
        }
        createController.syncLegacyStatics();
        createSurfaceSaveDialog.show();
    }

    public void updateUI() {
        if (mostraCoor != null) {
            mostraCoor.setTextColor(MyColorClass.colorConstraint);
            try {
                switch (DataSaved.bucketEdge) {
                    case -1:
                        bucketEdgeL.setBackgroundColor(getColor(R.color.yellow));
                        bucketEdgeC.setBackgroundColor(getColor(R.color.nav_gray_color));
                        bucketEdgeR.setBackgroundColor(getColor(R.color.nav_gray_color));
                        spigoloSelezionato = ExcavatorLib.bucketLeftCoord;
                        mostraCoor.setText("LEFT:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[0])) + "\n" +
                                "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[1])) + "\n" +
                                "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[2])));
                        break;
                    case 0:
                        bucketEdgeL.setBackgroundColor(getColor(R.color.nav_gray_color));
                        bucketEdgeC.setBackgroundColor(getColor(R.color.yellow));
                        bucketEdgeR.setBackgroundColor(getColor(R.color.nav_gray_color));
                        spigoloSelezionato = ExcavatorLib.bucketCoord;
                        mostraCoor.setText("CENTER:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[0])) + "\n" +
                                "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[1])) + "\n" +
                                "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[2])));
                        break;
                    case 1:
                        bucketEdgeL.setBackgroundColor(getColor(R.color.nav_gray_color));
                        bucketEdgeC.setBackgroundColor(getColor(R.color.nav_gray_color));
                        bucketEdgeR.setBackgroundColor(getColor(R.color.yellow));
                        spigoloSelezionato = ExcavatorLib.bucketRightCoord;
                        mostraCoor.setText("RIGHT:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[0])) + "\n" +
                                "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[1])) + "\n" +
                                "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[2])));
                        break;
                }
            } catch (Exception ignored) {
                mostraCoor.setText("Coordinate non pronte");
            }
        }

        updateModeIcon();
        tintToggle(gl_facce, gFacce);
        tintToggle(gl_fill, gFill);
        tintToggle(gl_poly, gPoly);
        tintToggle(gl_point, gPoint);
        tintToggle(gl_text, gText);


        if (gpsStat != null)
            gpsStat.setImageTintList(getColorStateList(DataSaved.gpsOk ? R.color.green : R.color.red));

        if (glVista3d == 1) {
            gl_2d3d.setImageResource(R.drawable.tredi_vista);
            isPan = false;
        } else {
            isPan = true;
            gl_2d3d.setImageResource(R.drawable.duedi_vista);

        }
        float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rotBus = rotBus % 360;
        navigatorHDT.setRotation(rotBus);
        if (DataSaved.points_Create != null) {
            if (DataSaved.points_Create.size() < 1) {
                remove_point.setVisibility(View.INVISIBLE);
            } else {
                remove_point.setVisibility(View.VISIBLE);
            }
        }

    }

    private void updateModeIcon() {
        if (statoImg == null) return;
        String p = proj == null ? "" : proj;
        switch (p) {
            case "PLAN":
                statoImg.setImageResource(R.drawable.piano_benna);
                break;
            case "AB":
                statoImg.setImageResource(R.drawable.ab_piano_benna);
                break;
            case "AREA":
                statoImg.setImageResource(R.drawable.perimetro_benna);
                break;
            case "TRENCH":
                statoImg.setImageResource(R.drawable.polyline);
                break;
            case "TRIANGLES":
                statoImg.setImageResource(R.drawable.terrain_model);
                break;
            default:
                statoImg.setImageResource(R.drawable.baseline_help_96);
                break;
        }

    }

    private void tintToggle(ImageView view, boolean enabled) {
        if (view != null)
            view.setImageTintList(getColorStateList(enabled ? R.color.green : R.color.white));
    }

    private void requestGlRender() {
        if (glSurfaceViewCreate != null) glSurfaceViewCreate.requestRender();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, TriangleService.class));
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceViewCreate != null) glSurfaceViewCreate.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceViewCreate != null) glSurfaceViewCreate.onResume();
        updateUI();
    }

    private void setAct() {
        if(DataSaved.points_Create!=null){
            if(!DataSaved.points_Create.isEmpty()) {
                if (!headingDialog.dialog.isShowing()) {
                    headingDialog.show();
                }
            }else {
                if (getIntent().getStringExtra("whoPRJ") != null) {
                    if (getIntent().getStringExtra("whoPRJ").equals("DIG")) {
                        startActivity(new Intent(this, My3DActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(this, Activity_Home_Page.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(this, Activity_Home_Page.class));
                    finish();
                }
            }
        }

    }

    private void testCreatePlanDirect() {
        if (DataSaved.points_Create == null) {
            DataSaved.points_Create = new java.util.ArrayList<>();
        }
        if (DataSaved.polylines_Create == null) {
            DataSaved.polylines_Create = new java.util.ArrayList<>();
        }
        if (DataSaved.dxfFaces_Create == null) {
            DataSaved.dxfFaces_Create = new java.util.ArrayList<>();
        }
        if (DataSaved.dxfTexts_Create == null) {
            DataSaved.dxfTexts_Create = new java.util.ArrayList<>();
        }

        DataSaved.points_Create.clear();
        DataSaved.polylines_Create.clear();
        DataSaved.dxfFaces_Create.clear();
        DataSaved.dxfTexts_Create.clear();

        double[] c;
        switch (DataSaved.bucketEdge) {
            case -1:
                c = packexcalib.exca.ExcavatorLib.bucketLeftCoord;
                break;
            case 1:
                c = packexcalib.exca.ExcavatorLib.bucketRightCoord;
                break;
            case 0:
            default:
                c = packexcalib.exca.ExcavatorLib.bucketCoord;
                break;
        }

        if (c == null || c.length < 3) {
            android.util.Log.e("CREATE_TEST", "Punto macchina nullo");
            return;
        }

        double side = 20.0;
        double h = side / 2.0;
        double z = c[2];

        dxf.Layer faceLayer = new dxf.Layer("CREATE", "CREATE_FACES", android.graphics.Color.YELLOW, true);
        dxf.Layer polyLayer = new dxf.Layer("CREATE", "CREATE_POLYLINES", android.graphics.Color.MAGENTA, true);

        dxf.Point3D center = new dxf.Point3D("P", c[0], c[1], z, "P");

        dxf.Point3D p1 = new dxf.Point3D("P1", c[0] - h, c[1] - h, z, "P1");
        dxf.Point3D p2 = new dxf.Point3D("P2", c[0] + h, c[1] - h, z, "P2");
        dxf.Point3D p3 = new dxf.Point3D("P3", c[0] + h, c[1] + h, z, "P3");
        dxf.Point3D p4 = new dxf.Point3D("P4", c[0] - h, c[1] + h, z, "P4");

        DataSaved.points_Create.add(center);
        DataSaved.points_Create.add(p1);
        DataSaved.points_Create.add(p2);
        DataSaved.points_Create.add(p3);
        DataSaved.points_Create.add(p4);

        DataSaved.dxfFaces_Create.add(new dxf.Face3D(p1, p2, center, center, android.graphics.Color.YELLOW, faceLayer));
        DataSaved.dxfFaces_Create.add(new dxf.Face3D(p2, p3, center, center, android.graphics.Color.YELLOW, faceLayer));
        DataSaved.dxfFaces_Create.add(new dxf.Face3D(p3, p4, center, center, android.graphics.Color.YELLOW, faceLayer));
        DataSaved.dxfFaces_Create.add(new dxf.Face3D(p4, p1, center, center, android.graphics.Color.YELLOW, faceLayer));

        DataSaved.polylines_Create.add(makeCreatePolyline(polyLayer, p1, p2));
        DataSaved.polylines_Create.add(makeCreatePolyline(polyLayer, p2, p3));
        DataSaved.polylines_Create.add(makeCreatePolyline(polyLayer, p3, p4));
        DataSaved.polylines_Create.add(makeCreatePolyline(polyLayer, p4, p1));

        DataSaved.typeView = 1;

        android.util.Log.e("CREATE_TEST",
                "CREATO PLAN DIRETTO"
                        + " points=" + DataSaved.points_Create.size()
                        + " faces=" + DataSaved.dxfFaces_Create.size()
                        + " polylines=" + DataSaved.polylines_Create.size()
                        + " typeView=" + DataSaved.typeView
                        + " glVista3d=" + My3DActivity.glVista3d
                        + " gFacce=" + gFacce
                        + " gPoly=" + gPoly
                        + " gPoint=" + gPoint);
    }

    private dxf.Polyline makeCreatePolyline(dxf.Layer layer, dxf.Point3D a, dxf.Point3D b) {
        java.util.ArrayList<dxf.Point3D> vertices = new java.util.ArrayList<>();
        vertices.add(a);
        vertices.add(b);

        dxf.Polyline polyline = new dxf.Polyline(vertices, layer);
        polyline.setLineColor(android.graphics.Color.MAGENTA);
        return polyline;
    }
}

