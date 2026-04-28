package gui.my_opengl;

import static gui.my_opengl.My3DActivity.glVista3d;
import static gui.my_opengl.My3DActivity.isPan;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.HeadingDialog;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import services.TriangleService;
import utils.MyData;
import utils.Utils;

public class MyGLActivity_Create extends BaseClass {
    double[] spigoloSelezionato=new double[3];

    String proj = null;
    String type = null;
    String percorso = null;
    String whoPROJ = null;
    MyGLSurfaceView_Create glSurfaceViewCreate;
    ImageView digMenu, bucketEdgeL, bucketEdgeC, bucketEdgeR, add_point, remove_point, edit_point, salva;
    ImageView gl_facce, gl_fill, gl_gradient, gl_poly, gl_point, gl_text,navigatorHDT,gpsStat,statoImg,gl_2d3d,gl_croce;
    public static boolean gFacce, gFill, gGrad, gPoly, gPoint, gText;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    TextView mostraCoor;
    HeadingDialog headingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCreateGlState();

        setContentView(R.layout.activity_my_glactivity);
        startService(new Intent(this, TriangleService.class));

        gFacce=true;
        gFill=true;
        gGrad=false;
        gPoly=true;
        gPoint=true;
        gText=true;
        try {
            proj = getIntent().getStringExtra("proj");
        } catch (Exception e) {
            proj = null;
        }
        try {
            type = getIntent().getStringExtra("type");
        } catch (Exception e) {
            type = null;
        }
        try {
            percorso = getIntent().getStringExtra("mPath");
        } catch (Exception e) {
            percorso = null;
        }
        try {
            whoPROJ = getIntent().getStringExtra("whoPRJ");
        } catch (Exception e) {
            whoPROJ = null;
        }
        Log.d("ProjectType", proj + "\n" + type + "\n" + percorso);


        findView();
        onClick();
    }

    private void findView() {

        glSurfaceViewCreate = findViewById(R.id.glSurfaceViewCreate);

        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        headingDialog = new HeadingDialog(this);
        gl_croce=findViewById(R.id.gl_croce);
        mostraCoor = findViewById(R.id.mostraCoor);
        statoImg=findViewById(R.id.statoImg);
        gpsStat=findViewById(R.id.gpsStat);
        navigatorHDT=findViewById(R.id.navigatorHDT);
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
        gl_gradient = findViewById(R.id.gl_gradient);
        gl_poly = findViewById(R.id.gl_poly);
        gl_point = findViewById(R.id.gl_point);
        gl_text = findViewById(R.id.gl_text);
        gl_2d3d=findViewById(R.id.gl_2d3d);
        try {
            MyGLRenderer.scale = MyData.get_Float("glScale");
            MyGLRenderer.angleX = MyData.get_Float("glAngleX");
            MyGLRenderer.angleY = MyData.get_Float("glAngleY");
            MyGLRenderer.panX = 0;
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


        if (MyGLRenderer.scale < 0.09f) {
            MyGLRenderer.scale = 0.09f;
        }
    }

    private void onClick() {

        add_point.setOnClickListener(v -> {

        });
        remove_point.setOnClickListener(v -> {

        });
        edit_point.setOnClickListener(v -> {

        });
        gl_croce.setOnClickListener(view -> {

            if (DataSaved.showAlign == 0) {
                DataSaved.showAlign = 1;
            } else if (DataSaved.showAlign == 1) {
                DataSaved.showAlign = 0;
            }

        });
        gpsStat.setOnClickListener(v -> {
            if(!dialogGnssCoordinates.alertDialog.isShowing()){
                dialogGnssCoordinates.show();
            }
        });
        gl_2d3d.setOnClickListener(v -> {

                glVista3d += 1;
                glVista3d = glVista3d % 2;


        });
        gl_facce.setOnClickListener(v -> {
            gFacce = !gFacce;
        });
        gl_fill.setOnClickListener(v -> {
            gFill = !gFill;
            if(gFill){
                gGrad=false;
            }

        });
        gl_gradient.setOnClickListener(v -> {
            gGrad = !gGrad;
            if(gGrad){
                gFill=false;
            }
        });
        gl_poly.setOnClickListener(v -> {

            gPoly = !gPoly;
        });
        gl_point.setOnClickListener(v -> {
            gPoint = !gPoint;
        });
        gl_text.setOnClickListener(v -> {
            gText = !gText;
        });
        digMenu.setOnClickListener(v -> {
            if (DataSaved.puntiProgetto != null) {
                if (!headingDialog.dialog.isShowing()) {
                    headingDialog.show();
                }
            } else {
                setAct();
            }
        });
    }
    public void updateUI(){
        mostraCoor.setTextColor(MyColorClass.colorConstraint);
        switch (DataSaved.bucketEdge) {
            case -1:
                spigoloSelezionato=ExcavatorLib.bucketLeftCoord;
                mostraCoor.setText("LEFT:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[0])) + "\n" +
                        "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[1])) + "\n" +
                        "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[2])));
                break;
            case 0:
                spigoloSelezionato=ExcavatorLib.bucketCoord;
                mostraCoor.setText("CENTER:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[0])) + "\n" +
                        "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[1])) + "\n" +
                        "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[2])));
                break;
            case 1:
                spigoloSelezionato=ExcavatorLib.bucketRightCoord;
                mostraCoor.setText("RIGHT:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[0])) + "\n" +
                        "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[1])) + "\n" +
                        "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[2])));
                break;
        }
        switch (proj){
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
        if(gFacce){
            gl_facce.setImageTintList(getColorStateList(R.color.green));
        }else {
            gl_facce.setImageTintList(getColorStateList(R.color.white));
        }
        if(gFill){
            gl_fill.setImageTintList(getColorStateList(R.color.green));
        }else {
            gl_fill.setImageTintList(getColorStateList(R.color.white));
        }
        if (gGrad) {
            gl_gradient.setImageResource(R.drawable.gradient);
        } else {
            gl_gradient.setImageResource(R.drawable.gradient_off);
        }
        if (gPoly) {
            gl_poly.setImageTintList(getColorStateList(R.color.green));
        } else {
            gl_poly.setImageTintList(getColorStateList(R.color.white));
        }
        if (gPoint) {
            gl_point.setImageTintList(getColorStateList(R.color.green));
        } else {
            gl_point.setImageTintList(getColorStateList(R.color.white));
        }
        if (gText) {
            gl_text.setImageTintList(getColorStateList(R.color.green));
        } else {
            gl_text.setImageTintList(getColorStateList(R.color.white));
        }
        if(DataSaved.gpsOk){
            gpsStat.setImageTintList(getColorStateList(R.color.green));
        }else {
            gpsStat.setImageTintList(getColorStateList(R.color.red));
        }
        if (glVista3d == 1) {
            isPan = false;
            gl_2d3d.setImageResource((R.drawable.tredi_vista));
        }else {
            isPan=true;
            gl_2d3d.setImageResource((R.drawable.duedi_vista));
        }
        if (DataSaved.showAlign > 0) {
            gl_croce.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            gl_croce.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, TriangleService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceViewCreate != null) {
            glSurfaceViewCreate.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceViewCreate != null) {
            glSurfaceViewCreate.onResume();
        }
    }
    private void setAct() {
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
    private void initCreateGlState() {
        // Vista 3D/2D
        try {
            glVista3d = Integer.parseInt(MyData.get_String("vista3D"));
        } catch (Exception e) {
            glVista3d = 1;
            MyData.push("vista3D", "1");
        }

        // typeView: il renderer Create disegna solo con 0 o 1
        try {
            DataSaved.typeView = Integer.parseInt(MyData.get_String("typeView"));
        } catch (Exception e) {
            DataSaved.typeView = 1;
            MyData.push("typeView", "1");
        }

        if (DataSaved.typeView != 0 && DataSaved.typeView != 1) {
            DataSaved.typeView = 1;
            MyData.push("typeView", "1");
        }

        // Parametri camera
        try {
            MyGLRenderer.scale = MyData.get_Float("glScale");
            MyGLRenderer.angleX = MyData.get_Float("glAngleX");
            MyGLRenderer.angleY = MyData.get_Float("glAngleY");
            MyGLRenderer.angleY_extra = MyData.get_Float("glAngleY_Extra");
            MyGLRenderer.scale_2d = MyData.get_Float("glScale_2d");
        } catch (Exception e) {
            MyGLRenderer.scale = 0.5f;
            MyGLRenderer.scale_2d = 0.5f;
            MyGLRenderer.angleX = -90f;
            MyGLRenderer.angleY = 0f;
            MyGLRenderer.angleY_extra = 0f;
        }

        MyGLRenderer.panX = 0f;
        MyGLRenderer.panY = -0.3f;

        if (MyGLRenderer.scale < 0.09f) {
            MyGLRenderer.scale = 0.09f;
        }
    }
}