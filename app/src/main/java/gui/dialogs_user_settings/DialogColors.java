package gui.dialogs_user_settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.digging_excavator.Digging3D_DXF;
import gui.draw_class.MyColorClass;
import gui.grading_dozergrader.Grading3D_DXF;
import gui.my_opengl.My3DActivity;
import gui.projects.Activity_Crea_Superficie;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class DialogColors {

    public Dialog dialog;
    public Activity activity;
    View img0, img1, img2;
    ImageView sel0, sel1, sel2;
    ImageView save, exit;
    TextView titolo;
    SeekBar lumin;
    int colorMode = 0;

    public DialogColors(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);


    }

    public void show() {
        init();
    }

    private void findView() {
        lumin = dialog.findViewById(R.id.luminosita);
        titolo=dialog.findViewById(R.id.titoloF);
        save = dialog.findViewById(R.id.save);
        exit = dialog.findViewById(R.id.exit);
        img0 = dialog.findViewById(R.id.img_0);
        img1 = dialog.findViewById(R.id.img_1);
        img2 = dialog.findViewById(R.id.img_2);
        sel0=dialog.findViewById(R.id.sel0);
        sel1=dialog.findViewById(R.id.sel1);
        sel2=dialog.findViewById(R.id.sel2);
        try {
            colorMode = MyData.get_Int("Tema_SW");

        } catch (NumberFormatException e) {
            colorMode = 0;
            MyData.push("Tema_SW", "0");
        }

    }

    private void onClick() {
        lumin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                titolo.setText(activity.getResources().getString(R.string.lumen)+"\n"+progress*10+"%");


            }
        });

        save.setOnClickListener(view -> {
            DataSaved.myBrightness=Float.parseFloat(String.valueOf((float) lumin.getProgress()/10));
            MyData.push("brightness", String.valueOf(DataSaved.myBrightness));
            DataSaved.temaSoftware = colorMode;
            MyData.push("Tema_SW", String.valueOf(colorMode));
            switch (DataSaved.temaSoftware) {
                case 0:
                    MyColorClass.colorSfondo = activity.getResources().getColor(R.color.black);
                    MyColorClass.colorConstraint = activity.getResources().getColor(R.color.bg_white);
                    MyColorClass.colorLabel = ((Color.YELLOW));
                    MyColorClass.colorGroundX = (activity.getResources().getColor(R.color.GROUNDred));
                    MyColorClass.colorGroundY = (activity.getResources().getColor(R.color.cyan));
                    MyColorClass.colorStick = (activity.getResources().getColor(R.color.orange));
                    MyColorClass.colorBucket = (activity.getResources().getColor(R.color.orange));
                    MyColorClass.colorPoint = (activity.getResources().getColor(R.color.red));
                    MyColorClass.colorPoly = (activity.getResources().getColor(R.color.teal_200));

                    MyColorClass.colorOffsetLine = (activity.getResources().getColor(R.color.bg_sfsred));
                    MyColorClass.colorX_2D = (activity.getResources().getColor(R.color.GROUNDred));
                    MyColorClass.colorY_2D = (activity.getResources().getColor(R.color.GROUNDblue));
                    // MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                    //MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                    MyColorClass.utilitiesColor = (Color.parseColor("#FFA500"));
                    MyColorClass.jsonColor = (activity.getResources().getColor(R.color.cyan));
                    break;


                case 1:
                    MyColorClass.colorSfondo = activity.getResources().getColor(R.color.light_gray);
                    MyColorClass.colorConstraint = activity.getResources().getColor(R.color.black);
                    MyColorClass.colorLabel = (activity.getResources().getColor(R.color.blue));
                    MyColorClass.colorGroundX = (activity.getResources().getColor(R.color.GROUNDblue));
                    MyColorClass.colorGroundY = (activity.getResources().getColor(R.color.GROUNDblue));
                    MyColorClass.colorStick = (activity.getResources().getColor(R.color.orange));
                    MyColorClass.colorBucket = (activity.getResources().getColor(R.color.orange));
                    MyColorClass.colorPoint = (activity.getResources().getColor(R.color.GROUNDred));
                    MyColorClass.colorPoly = (activity.getResources().getColor(R.color._____cancel_text));
                    MyColorClass.colorOffsetLine = (activity.getResources().getColor(R.color.bg_sfsred));
                    MyColorClass.colorX_2D = (activity.getResources().getColor(R.color.GROUNDred));
                    MyColorClass.colorY_2D = (activity.getResources().getColor(R.color.GROUNDblue));
                    //MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                   // MyColorClass.groundTransparency = Color.parseColor("#50BBBBBB");
                    MyColorClass.utilitiesColor = (activity.getResources().getColor(R.color.orange));
                    MyColorClass.jsonColor = (activity.getResources().getColor(R.color.red));
                    break;
                case 2:
                    MyColorClass.colorSfondo = activity.getResources().getColor(R.color.white);
                    MyColorClass.colorConstraint = activity.getResources().getColor(R.color.black);
                    MyColorClass.colorLabel = (activity.getResources().getColor(R.color.blue));
                    MyColorClass.colorGroundX = (activity.getResources().getColor(R.color.GROUNDblue));
                    MyColorClass.colorGroundY = (activity.getResources().getColor(R.color.GROUNDblue));
                    MyColorClass.colorStick = (activity.getResources().getColor(R.color.orange));
                    MyColorClass.colorBucket = (activity.getResources().getColor(R.color.orange));
                    MyColorClass.colorPoint = (activity.getResources().getColor(R.color.GROUNDred));
                    MyColorClass.colorPoly = (activity.getResources().getColor(R.color.magenta));
                    MyColorClass.colorOffsetLine = (activity.getResources().getColor(R.color.bg_sfsred));
                    MyColorClass.colorX_2D = (activity.getResources().getColor(R.color.GROUNDred));
                    MyColorClass.colorY_2D = (activity.getResources().getColor(R.color.GROUNDblue));
                    //MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                    MyColorClass.utilitiesColor = (activity.getResources().getColor(R.color.orange));
                    MyColorClass.jsonColor = (activity.getResources().getColor(R.color.red));
                    break;


                default:
                    MyColorClass.colorSfondo = activity.getResources().getColor(R.color.white);
                    MyColorClass.colorConstraint = activity.getResources().getColor(R.color.black);
                    MyColorClass.colorLabel = (activity.getResources().getColor(R.color.black));
                    MyColorClass.colorGroundX = (activity.getResources().getColor(R.color.GROUNDblue));
                    MyColorClass.colorGroundY = (activity.getResources().getColor(R.color.GROUNDblue));
                    MyColorClass.colorStick = (activity.getResources().getColor(R.color.orange));
                    MyColorClass.colorBucket = (activity.getResources().getColor(R.color.dark_gray));
                    MyColorClass.colorPoint = (activity.getResources().getColor(R.color.GROUNDred));
                    MyColorClass.colorPoly = (activity.getResources().getColor(R.color.magenta));
                    MyColorClass.colorOffsetLine = (activity.getResources().getColor(R.color.bg_sfsred));
                    MyColorClass.colorX_2D = (activity.getResources().getColor(R.color.GROUNDred));
                    MyColorClass.colorY_2D = (activity.getResources().getColor(R.color.GROUNDblue));
                    //MyColorClass.groundTransparency = Color.parseColor("#50FFFF00");
                    MyColorClass.utilitiesColor = (activity.getResources().getColor(R.color.teal_700));
                    MyColorClass.jsonColor = (activity.getResources().getColor(R.color.red));
                    break;


            }
            dialog.dismiss();
            if (activity instanceof Digging3D_DXF|| activity instanceof Grading3D_DXF||activity instanceof Activity_Crea_Superficie|| activity instanceof My3DActivity) {
                activity.recreate();
            }


        });
        img0.setOnClickListener(view -> {
            colorMode = 0;
            setup();

        });
        img1.setOnClickListener(view -> {
            colorMode = 1;
            setup();
        });
        img2.setOnClickListener(view -> {
            colorMode = 2;
            setup();
        });

        exit.setOnClickListener(view -> {
            dialog.dismiss();
        });


    }

    private void setup() {
        lumin.setProgress((int) (DataSaved.myBrightness*10));
        titolo.setText(activity.getResources().getString(R.string.lumen)+"\n"+String.format("%.0f",DataSaved.myBrightness*100)+"%");
        switch (colorMode) {
            case 0:
                sel0.setVisibility(View.VISIBLE);
                sel1.setVisibility(View.GONE);
                sel2.setVisibility(View.GONE);
                img0.setAlpha(1f);
                img1.setAlpha(1f);
                img2.setAlpha(1f);
                img0.setPadding(10, 10, 10, 10);
                img1.setPadding(50, 50, 50, 50);
                img2.setPadding(50, 50, 50, 50);
                break;
            case 1:
                sel0.setVisibility(View.GONE);
                sel1.setVisibility(View.VISIBLE);
                sel2.setVisibility(View.GONE);
                img0.setAlpha(1f);
                img1.setAlpha(1f);
                img2.setAlpha(1f);
                img1.setPadding(10, 10, 10, 10);
                img0.setPadding(50, 50, 50, 50);
                img2.setPadding(50, 50, 50, 50);
                break;
            case 2:
                sel0.setVisibility(View.GONE);
                sel1.setVisibility(View.GONE);
                sel2.setVisibility(View.VISIBLE);
                img0.setAlpha(1f);
                img1.setAlpha(1f);
                img2.setAlpha(1f);
                img2.setPadding(10, 10, 10, 10);
                img1.setPadding(50, 50, 50, 50);
                img0.setPadding(50, 50, 50, 50);
                break;

        }


    }


    @SuppressLint("SetTextI18n")
    private void init() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_colors_7);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        setup();
        onClick();


    }
}
