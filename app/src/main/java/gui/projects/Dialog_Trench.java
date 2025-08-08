package gui.projects;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stx_dig.R;

import dxf.Point3D;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Trench {
    ImageView spiana, mantieni;
    static boolean flat;
    public Dialog dialog;
    Activity activity;
    Button ok, cancel, reload;
    int uom;
    EditText leftW, leftS, rightW, rightS,etStart,etEnd;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    Point3D[] point3DS;
    public static double leftW_d, leftS_d, rightW_d, rightS_d;
    TextView tx1, tx2, tx3,txStart,txEnd;
    static Point3D[] tempPoints;
    ConstraintLayout vistaTrench;
    SezioneTrenchView sezioneView;

    public Dialog_Trench(Activity activity, Point3D[] point3DS) {
        this.activity = activity;
        this.point3DS = point3DS;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        uom = MyData.get_Int("Unit_Of_Measure");
        customNumberDialog = new CustomNumberDialog(activity, -256);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -256);
        if (tempPoints == null) {
            tempPoints = new Point3D[0];
        }
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_trench);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();
        update();


    }

    private void findView() {
        vistaTrench = dialog.findViewById(R.id.vistaTrench);
        reload = dialog.findViewById(R.id.reload);
        ok = dialog.findViewById(R.id.ok);
        cancel = dialog.findViewById(R.id.cancel);
        spiana = dialog.findViewById(R.id.spiana);
        mantieni = dialog.findViewById(R.id.mantieni);
        leftW = dialog.findViewById(R.id.leftW);
        leftS = dialog.findViewById(R.id.leftS);
        rightW = dialog.findViewById(R.id.rightW);
        rightS = dialog.findViewById(R.id.rightS);
        sezioneView = dialog.findViewById(R.id.sezioneView);
        tx1=dialog.findViewById(R.id.tx1);
        tx2=dialog.findViewById(R.id.tx2);
        tx3=dialog.findViewById(R.id.tx3);
        txStart=dialog.findViewById(R.id.txStart);
        txEnd=dialog.findViewById(R.id.txEnd);
        etStart=dialog.findViewById(R.id.et_start);
        etEnd=dialog.findViewById(R.id.et_end);

    }

    private void init() {
        leftW.setText(Utils.readUnitOfMeasureLITE(String.valueOf(leftW_d).replace(",", ".")));
        leftS.setText(Utils.readAngoloLITE(String.valueOf(leftS_d).replace(",", ".")));
        rightW.setText(Utils.readUnitOfMeasureLITE(String.valueOf(rightW_d).replace(",", ".")));
        rightS.setText(Utils.readAngoloLITE(String.valueOf(rightS_d).replace(",", ".")));
        tx1.setText("CL ELEVATION MODE");
        tx2.setText("LEFT  WIDTH "+Utils.getMetriSimbolCoords()+"    LEFT  SLOPE "+Utils.getGradiSimbol());
        tx3.setText("RIGHT WIDTH "+Utils.getMetriSimbolCoords()+"    RIGHT SLOPE "+Utils.getGradiSimbol());
        txStart.setText("Start Z "+ Utils.getMetriSimbol());
        txEnd.setText("End Z "+ Utils.getMetriSimbol());
        try {
            etStart.setText(Utils.readUnitOfMeasureLITE(String.valueOf(point3DS[0].getZ())));
            etEnd.setText(Utils.readUnitOfMeasureLITE(String.valueOf(point3DS[point3DS.length-1].getZ())));
        } catch (Exception ignored) {

        }


    }

    private void onClick() {
        reload.setOnClickListener(view -> {
            update();
        });
        ok.setOnClickListener(view -> {
            update();
            dialog.dismiss();
        });
        cancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        spiana.setOnClickListener(view -> {
            flat = true;
            update();
        });
        mantieni.setOnClickListener(view -> {
            flat = false;
            update();
        });
        etStart.setOnClickListener(view -> {
            if(uom==0||uom==1||uom==2||uom==3||uom==6||uom==7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(etStart);
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(etStart);
                }
            }
        });
        etEnd.setOnClickListener(view -> {
            if(uom==0||uom==1||uom==2||uom==3||uom==6||uom==7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(etEnd);
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(etEnd);
                }
            }
        });
        leftW.setOnClickListener(view -> {
            if(uom==0||uom==1||uom==2||uom==3||uom==6||uom==7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(leftW);
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(leftW);
                }
            }

        });
        leftS.setOnClickListener(view -> {
            if(uom==0||uom==1||uom==2||uom==3||uom==6||uom==7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(leftS);
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(leftS);
                }
            }
        });
        rightW.setOnClickListener(view -> {
            if(uom==0||uom==1||uom==2||uom==3||uom==6||uom==7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(rightW);
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(rightW);
                }
            }
        });
        rightS.setOnClickListener(view -> {
            if(uom==0||uom==1||uom==2||uom==3||uom==6||uom==7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(rightS);
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(rightS);
                }
            }

        });
    }

    private void update() {
        try {
            point3DS[0].setZ(Double.parseDouble(Utils.writeMetri(etStart.getText().toString())));
            point3DS[point3DS.length-1].setZ(Double.parseDouble(Utils.writeMetri(etEnd.getText().toString())));
        } catch (Exception ignored) {

        }
        if (flat) {
            spiana.setAlpha(1.0f);
            spiana.setBackgroundColor(Color.YELLOW);
            mantieni.setAlpha(0.3f);
            mantieni.setBackgroundColor(Color.TRANSPARENT);
        } else {
            spiana.setAlpha(0.3f);
            spiana.setBackgroundColor(Color.TRANSPARENT);
            mantieni.setAlpha(1.0f);
            mantieni.setBackgroundColor(Color.YELLOW);
        }

        leftW_d = Double.parseDouble(Utils.writeMetri(leftW.getText().toString()));
        leftS_d = Double.parseDouble(Utils.writeGradi(leftS.getText().toString()));
        rightW_d = Double.parseDouble(Utils.writeMetri(rightW.getText().toString()));
        rightS_d = Double.parseDouble(Utils.writeGradi(rightS.getText().toString()));
        if (flat) {
            tempPoints = calcolaNuoveZ(point3DS);
        }

        Point3D[] puntiDaMostrare = flat ? tempPoints : point3DS;
        sezioneView.setPoints(puntiDaMostrare);

        try {
            etStart.setText(Utils.readUnitOfMeasureLITE(String.valueOf(point3DS[0].getZ())));
            etEnd.setText(Utils.readUnitOfMeasureLITE(String.valueOf(point3DS[point3DS.length-1].getZ())));
        } catch (Exception ignored) {

        }


    }

    public Point3D[] calcolaNuoveZ(Point3D[] pointS) {
        if (pointS == null || pointS.length < 2) return pointS;

        Point3D[] result = new Point3D[pointS.length];

        Point3D p0 = pointS[0];
        Point3D pN = pointS[pointS.length - 1];

        double x0 = p0.getX();
        double y0 = p0.getY();
        double z0 = p0.getZ();

        double x1 = pN.getX();
        double y1 = pN.getY();
        double z1 = pN.getZ();

        // Direzione del segmento base
        double dx = x1 - x0;
        double dy = y1 - y0;
        double segmentLengthSq = dx * dx + dy * dy;

        for (int i = 0; i < pointS.length; i++) {
            double x = pointS[i].getX();
            double y = pointS[i].getY();

            // Calcola t = proiezione del punto sulla retta base (parametrica)
            double t;
            if (segmentLengthSq == 0) {
                t = 0; // Evita divisione per zero, segmento nullo
            } else {
                t = ((x - x0) * dx + (y - y0) * dy) / segmentLengthSq;
            }

            // Interpola Z usando t
            double z = z0 + t * (z1 - z0);

            // Crea nuovo punto
            result[i] = new Point3D(pointS[i].getId(), x, y, z, pointS[i].getName());
        }

        return result;
    }


}
