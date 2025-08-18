package gui.dialogs_and_toast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.boot_and_choose.Activity_Home_Page;
import gui.digging_excavator.Digging2D;
import gui.digging_excavator.Digging_CutAndFill2D;
import gui.projects.Activity_Crea_Superficie;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.gnss.NmeaListener;
import utils.FullscreenActivity;
import utils.MyData;

public class HeadingDialog {
    Activity activity;
    public Dialog dialog;
    Button yes, exit;
    TextView title;
    CheckBox ckInvert;
    int value;
    SeekBar seekBar;
    TextView title2, valueDrift;
    ImageView imageView;
    int prog;

    public HeadingDialog(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {

        dialog.create();
        dialog.setContentView(R.layout.dialog_heading);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//necessario per mostrare il layout di sfondo
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
    }

    private void findView() {
        yes = dialog.findViewById(R.id.add_new);
        exit = dialog.findViewById(R.id.create_new);
        title = dialog.findViewById(R.id.title);
        title2 = dialog.findViewById(R.id.titDrift);
        ckInvert = dialog.findViewById(R.id.ckInvert);
        seekBar = dialog.findViewById(R.id.valoreDrift);
        valueDrift = dialog.findViewById(R.id.valueDrift);
        imageView = dialog.findViewById(R.id.imgClose);
        if (activity instanceof Activity_Crea_Superficie ) {
            title.setText(R.string.exit_proj);
            yes.setText(R.string.yes);
            exit.setText(R.string.no);

            yes.setBackgroundTintList(ColorStateList.valueOf(activity.getColor(R.color.bg_sfsred)));
        } if (DataSaved.useYawFrame == 1&& (activity instanceof Digging_CutAndFill2D||activity instanceof Digging2D)) {
                title2.setVisibility(View.VISIBLE);
                ckInvert.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                valueDrift.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);

            } else {
                title2.setVisibility(View.INVISIBLE);
                ckInvert.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                valueDrift.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.INVISIBLE);
            }

    }

    private void onClick() {
        ckInvert.setChecked(DataSaved.driftSign == 1);


        if (DataSaved.driftStep == 0) {
            valueDrift.setText("OFF");
            seekBar.setProgress(0);
        } else {
            valueDrift.setText(String.valueOf((DataSaved.driftStep/10))+" Sec.");
            seekBar.setProgress((DataSaved.driftStep/10)-1);
        }
        ckInvert.setOnClickListener(view -> {
            ckInvert.setChecked(ckInvert.isChecked());
            if (ckInvert.isChecked()) {

                DataSaved.driftSign = 1;
                MyData.push("driftSign", String.valueOf(DataSaved.driftSign));
            } else {
                DataSaved.driftSign = 0;
                MyData.push("driftSign", String.valueOf(DataSaved.driftSign));
            }
        });
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



                switch (progress){
                        case 0:
                            valueDrift.setText("OFF");
                            DataSaved.driftStep = 0;
                            MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                            break;
                    case 1:
                        valueDrift.setText(String.valueOf("2 Sec."));
                        DataSaved.driftStep =20;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 2:
                        valueDrift.setText(String.valueOf("3 Sec."));
                        DataSaved.driftStep =30;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 3:
                        valueDrift.setText(String.valueOf("4 Sec."));
                        DataSaved.driftStep =40;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 4:
                        valueDrift.setText(String.valueOf("5 Sec."));
                        DataSaved.driftStep =50;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 5:
                        valueDrift.setText(String.valueOf("6 Sec."));
                        DataSaved.driftStep =60;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 6:
                        valueDrift.setText(String.valueOf("7 Sec."));
                        DataSaved.driftStep =70;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 7:
                        valueDrift.setText(String.valueOf("8 Sec."));
                        DataSaved.driftStep =80;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 8:
                        valueDrift.setText(String.valueOf("9 Sec."));
                        DataSaved.driftStep =90;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 9:
                        valueDrift.setText(String.valueOf("10 Sec."));
                        DataSaved.driftStep =100;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 10:
                        valueDrift.setText(String.valueOf("11 Sec."));
                        DataSaved.driftStep =110;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 11:
                        valueDrift.setText(String.valueOf("12 Sec."));
                        DataSaved.driftStep =120;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 12:
                        valueDrift.setText(String.valueOf("13 Sec."));
                        DataSaved.driftStep =130;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 13:
                        valueDrift.setText(String.valueOf("14 Sec."));
                        DataSaved.driftStep =140;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 14:
                        valueDrift.setText(String.valueOf("15 Sec."));
                        DataSaved.driftStep =150;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 15:
                        valueDrift.setText(String.valueOf("16 Sec."));
                        DataSaved.driftStep =160;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 16:
                        valueDrift.setText(String.valueOf("17 Sec."));
                        DataSaved.driftStep =170;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 17:
                        valueDrift.setText(String.valueOf("18 Sec."));
                        DataSaved.driftStep =180;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 18:
                        valueDrift.setText(String.valueOf("19 Sec."));
                        DataSaved.driftStep =190;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 19:
                        valueDrift.setText(String.valueOf("20 Sec."));
                        DataSaved.driftStep =200;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    case 20:
                        valueDrift.setText(String.valueOf("30 Sec."));
                        DataSaved.driftStep =300;
                        MyData.push("driftStep", String.valueOf(DataSaved.driftStep));
                        break;
                    }






            }
        });
        imageView.setOnClickListener(view -> {
            dialog.dismiss();
        });
        yes.setOnClickListener((View v) -> {
            if (activity instanceof Activity_Crea_Superficie ) {
                activity.startActivity(new Intent(activity, Activity_Home_Page.class));
                activity.finish();

            } else {
                if (DataSaved.useYawFrame == 0) {
                    DataSaved.offsetHDT = NmeaListener.roof_Orientation;
                } else {
                    DataSaved.offsetHDT = Sensors_Decoder.Deg_Yaw_Frame;

                }
                MyData.push("Offset_Hdt", String.valueOf(DataSaved.offsetHDT));
                dialog.dismiss();
            }
        });

        exit.setOnClickListener((View v) -> {
            dialog.dismiss();
        });
    }
}

