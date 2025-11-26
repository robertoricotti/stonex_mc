package gui.dialogs_and_toast;



import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.MyApp;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.PLC_DataTypes_BigEndian;
import packexcalib.gnss.Deg2UTM;
import packexcalib.gnss.NmeaListener;
import services.TriangleService;
import utils.CPCanHelper;
import utils.MyDeviceManager;
import utils.Utils;

public class Dialog_CutFill_3D {
    Activity activity;
    public Dialog dialog;
    ImageView close;
    TextView txtCutFill;
    LinearLayout layoutt;
    private boolean isUpdating = false;
    private Handler handler;

    public Dialog_CutFill_3D(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }
    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_cutfill_3d);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.95f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); //  Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.95);
        int height = (int) (displayMetrics.heightPixels * 0.95);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        findView();
        onClick();
        startUpdatingCoordinates();

    }
    private void findView(){
        txtCutFill=dialog.findViewById(R.id.texto);
        close=dialog.findViewById(R.id.close);
        layoutt=dialog.findViewById(R.id.layoutt);

    }
    private void onClick(){
        close.setOnClickListener(view -> {
            stopUpdatingCoordinates();
            dialog.dismiss();
        });

    }

    private void startUpdatingCoordinates() {
        if (!isUpdating) {
            isUpdating = true;
            handler = new Handler();
            updateCoordinates();
        }
    }

    private void stopUpdatingCoordinates() {
        if (isUpdating) {
            isUpdating = false;
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
    }

    private void updateCoordinates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update coord TextView with new coordinates
                try {
                    int colorUpCF, colorDownCF, colorGreenCF;
                    if (DataSaved.colorMode == 0) {
                        colorUpCF = R.drawable.cut_fill_sfondo_rosso;
                        colorDownCF = R.drawable.cut_fill_sfondo_blue;
                        colorGreenCF = R.drawable.cut_fill_sfondo_verde;
                    } else {
                        colorDownCF = R.drawable.cut_fill_sfondo_rosso;
                        colorUpCF = R.drawable.cut_fill_sfondo_blue;
                        colorGreenCF = R.drawable.cut_fill_sfondo_verde;
                    }
                    switch (DataSaved.bucketEdge) {
                        case -1:
                            if (TriangleService.ltOffGrid) {
                                txtCutFill.setText("-.---");
                                txtCutFill.setTextColor(Color.WHITE);
                                close.setImageTintList(activity.getColorStateList(R.color.white));
                                layoutt.setBackground(activity.getDrawable(R.drawable.custom_background_test3d_box));
                            } else {
                                if (TriangleService.quota3D_SX > DataSaved.deadbandH) {
                                    layoutt.setBackground(activity.getResources().getDrawable(colorUpCF));
                                    txtCutFill.setTextColor(Color.WHITE);
                                    close.setImageTintList(activity.getColorStateList(R.color.white));
                                    txtCutFill.setText("▼\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                                } else if (TriangleService.quota3D_SX < -DataSaved.deadbandH) {
                                    txtCutFill.setTextColor(Color.WHITE);
                                    close.setImageTintList(activity.getColorStateList(R.color.white));
                                    layoutt.setBackground(activity.getResources().getDrawable(colorDownCF));
                                    txtCutFill.setText("▲\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                                } else if (TriangleService.quota3D_SX >= -DataSaved.deadbandH && TriangleService.quota3D_SX <= DataSaved.deadbandH) {
                                    txtCutFill.setTextColor(Color.DKGRAY);
                                    close.setImageTintList(activity.getColorStateList(R.color._____cancel_text));
                                    layoutt.setBackground(activity.getDrawable(colorGreenCF));
                                    txtCutFill.setText("⧗\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_SX)));
                                }
                            }
                            break;

                        case 0:
                            if (TriangleService.ctOffGrid) {
                                txtCutFill.setText("-.---");
                                txtCutFill.setTextColor(Color.WHITE);
                                close.setImageTintList(activity.getColorStateList(R.color.white));
                                layoutt.setBackground(activity.getDrawable(R.drawable.custom_background_test3d_box));
                            } else {
                                if (TriangleService.quota3D_CT > DataSaved.deadbandH) {
                                    txtCutFill.setTextColor(Color.WHITE);
                                    close.setImageTintList(activity.getColorStateList(R.color.white));
                                    layoutt.setBackground(activity.getResources().getDrawable(colorUpCF));
                                    txtCutFill.setText("▼\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                } else if (TriangleService.quota3D_CT < -DataSaved.deadbandH) {
                                    txtCutFill.setTextColor(Color.WHITE);
                                    close.setImageTintList(activity.getColorStateList(R.color.white));
                                    layoutt.setBackground(activity.getResources().getDrawable(colorDownCF));
                                    txtCutFill.setText("▲\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                } else if (TriangleService.quota3D_CT >= -DataSaved.deadbandH && TriangleService.quota3D_CT <= DataSaved.deadbandH) {
                                    txtCutFill.setTextColor(Color.DKGRAY);
                                    close.setImageTintList(activity.getColorStateList(R.color._____cancel_text));
                                    layoutt.setBackground(activity.getDrawable(colorGreenCF));
                                    txtCutFill.setText("⧗\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT)));
                                }
                            }
                            break;

                        case 1:
                            if (TriangleService.rtOffGrid) {
                                txtCutFill.setText("-.---");
                                txtCutFill.setTextColor(Color.WHITE);
                                close.setImageTintList(activity.getColorStateList(R.color.white));
                                layoutt.setBackground(activity.getDrawable(R.drawable.custom_background_test3d_box));
                            } else {
                                if (TriangleService.quota3D_DX > DataSaved.deadbandH) {
                                    txtCutFill.setTextColor(Color.WHITE);
                                    close.setImageTintList(activity.getColorStateList(R.color.white));
                                    layoutt.setBackground(activity.getResources().getDrawable(colorUpCF));
                                    txtCutFill.setText("▼\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                                } else if (TriangleService.quota3D_DX < -DataSaved.deadbandH) {
                                    txtCutFill.setTextColor(Color.WHITE);
                                    close.setImageTintList(activity.getColorStateList(R.color.white));
                                    layoutt.setBackground(activity.getResources().getDrawable(colorDownCF));
                                    txtCutFill.setText("▲\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                                } else if (TriangleService.quota3D_DX >= -DataSaved.deadbandH && TriangleService.quota3D_SX <= DataSaved.deadbandH) {
                                    txtCutFill.setTextColor(Color.DKGRAY);
                                    close.setImageTintList(activity.getColorStateList(R.color._____cancel_text));
                                    layoutt.setBackground(activity.getDrawable(colorGreenCF));
                                    txtCutFill.setText("⧗\n" + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_DX)));
                                }
                            }
                            break;
                    }
                    if (isUpdating) {
                        updateCoordinates();
                    }
                } catch (Exception e) {
                }
            }
        }, 65);
    }

}
