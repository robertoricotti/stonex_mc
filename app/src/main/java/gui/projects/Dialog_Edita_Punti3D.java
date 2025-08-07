package gui.projects;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stx_dig.R;

import java.util.Arrays;

import dxf.Point3D;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomQwertyDialog;
import utils.FullscreenActivity;

public class Dialog_Edita_Punti3D {
    Activity activity;
    Point3D[] point3DS;
    public static Dialog dialog;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    CustomQwertyDialog customQwertyDialog;
    RecyclerView recyclerView;
    Button okBtn;
    public static Punti3DAdapter adapter;
    public Dialog_Edita_Punti3D(Activity activity, Point3D[] point3DS){
        this.activity=activity;
        this.point3DS=point3DS;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        customNumberDialogFtIn=new CustomNumberDialogFtIn(activity, 111);
        customNumberDialog=new CustomNumberDialog(activity, 111);
        customQwertyDialog=new CustomQwertyDialog(activity,null);


    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_edita_punti_3d);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
    }
    private void findView(){
        okBtn=dialog.findViewById(R.id.okBtn);
        recyclerView = dialog.findViewById(R.id.myrecyc);

    }
    private void init(){
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new Punti3DAdapter(Arrays.asList(point3DS));
        recyclerView.setAdapter(adapter);
    }


    private void onClick(){
        okBtn.setOnClickListener(view -> {
            dialog.dismiss();
        });

    }
    public static void chiudi(){
        dialog.dismiss();
    }
    public static void aggiornaLista() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    }

