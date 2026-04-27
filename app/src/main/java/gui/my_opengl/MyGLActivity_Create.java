package gui.my_opengl;

import static services.CanService.Grader_Auto_SS;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stx_dig.R;

import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.draw_class.MyColorClass;
import gui.my_opengl.wheel.MyGLSurfaceView_Create;
import services.TriangleService;
import utils.MyData;
import utils.MyDeviceManager;

public class MyGLActivity_Create extends AppCompatActivity {
    String proj = null;
    String type =null;
    String percorso = null;
    MyGLSurfaceView_Create glSurfaceViewCreate;
    ImageView digMenu,bucketEdgeL,bucketEdgeC,bucketEdgeR,add_point,remove_point,edit_point,salva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_glactivity);
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
        Log.d("ProjectType", proj+"\n"+type+"\n"+percorso);
        glSurfaceViewCreate=new MyGLSurfaceView_Create(this);
        findView();
        onClick();


    }
    private void findView(){
        digMenu=findViewById(R.id.digMenu);
        bucketEdgeL=findViewById(R.id.bucketEdgeL);
        bucketEdgeC=findViewById(R.id.bucketEdgeC);
        bucketEdgeR=findViewById(R.id.bucketEdgeR);
        add_point=findViewById(R.id.add_point);
        remove_point=findViewById(R.id.remove_point);
        edit_point=findViewById(R.id.edit_point);
        salva=findViewById(R.id.salva);
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
    private void onClick(){
        digMenu.setOnClickListener(v -> {
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onResume() {
        super.onResume();


    }

}