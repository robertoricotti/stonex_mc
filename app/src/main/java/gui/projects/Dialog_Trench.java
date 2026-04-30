package gui.projects;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.util.Log;
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
import gui.my_opengl.CreateSurfaceController;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Trench {
    private static final String TAG = "Dialog_Trench";

    ImageView spiana, mantieni;

    /**
     * true  = spiana centerline with constant longitudinal grade from startZ_d to endZ_d.
     * false = keep current picked point Z values.
     */
    public static boolean flat = false;

    public static double leftW_d = 1.0;
    public static double leftS_d = 0.0;
    public static double rightW_d = 1.0;
    public static double rightS_d = 0.0;
    public static double startZ_d = Double.NaN;
    public static double endZ_d = Double.NaN;

    public Dialog dialog;
    Activity activity;
    Button ok, cancel, reload;
    int uom;
    EditText leftW, leftS, rightW, rightS, etStart, etEnd;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    Point3D[] point3DS;
    TextView tx1, tx2, tx3, txStart, txEnd;
    public static Point3D[] tempPoints = new Point3D[0];
    ConstraintLayout vistaTrench;
    SezioneTrenchView sezioneView;

    public Dialog_Trench(Activity activity, Point3D[] point3DS) {
        this.activity = activity;
        this.point3DS = point3DS == null ? new Point3D[0] : point3DS;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        uom = MyData.get_Int("Unit_Of_Measure");
        customNumberDialog = new CustomNumberDialog(activity, -256);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -256);
        initialiseStartEndIfNeeded();
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_trench);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
        }

        dialog.show();
        FullscreenActivity.setFullScreen(dialog);

        findView();
        init();
        onClick();
        updatePreview(false);
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
        tx1 = dialog.findViewById(R.id.tx1);
        tx2 = dialog.findViewById(R.id.tx2);
        tx3 = dialog.findViewById(R.id.tx3);
        txStart = dialog.findViewById(R.id.txStart);
        txEnd = dialog.findViewById(R.id.txEnd);
        etStart = dialog.findViewById(R.id.et_start);
        etEnd = dialog.findViewById(R.id.et_end);
    }

    private void init() {
        tx1.setText("CL ELEVATION MODE");
        tx2.setText("LEFT  WIDTH " + Utils.getMetriSimbolCoords() + "    LEFT  SLOPE " + Utils.getGradiSimbol());
        tx3.setText("RIGHT WIDTH " + Utils.getMetriSimbolCoords() + "    RIGHT SLOPE " + Utils.getGradiSimbol());
        txStart.setText("Start Z " + Utils.getMetriSimbol());
        txEnd.setText("End Z " + Utils.getMetriSimbol());

        leftW.setText(Utils.readUnitOfMeasureLITE(String.valueOf(leftW_d).replace(",", ".")));
        leftS.setText(Utils.readAngoloLITE(String.valueOf(leftS_d).replace(",", ".")));
        rightW.setText(Utils.readUnitOfMeasureLITE(String.valueOf(rightW_d).replace(",", ".")));
        rightS.setText(Utils.readAngoloLITE(String.valueOf(rightS_d).replace(",", ".")));
        etStart.setText(Utils.readUnitOfMeasureLITE(String.valueOf(startZ_d).replace(",", ".")));
        etEnd.setText(Utils.readUnitOfMeasureLITE(String.valueOf(endZ_d).replace(",", ".")));
    }

    private void onClick() {
        reload.setOnClickListener(view -> updatePreview(true));

        ok.setOnClickListener(view -> {
            updatePreview(true);
            commitToLegacyPointsIfNeeded();
            dialog.dismiss();
        });

        cancel.setOnClickListener(view -> dialog.dismiss());

        spiana.setOnClickListener(view -> {
            flat = true;
            updatePreview(true);
        });

        mantieni.setOnClickListener(view -> {
            flat = false;
            updatePreview(true);
        });

        setNumericClick(etStart);
        setNumericClick(etEnd);
        setNumericClick(leftW);
        setNumericClick(leftS);
        setNumericClick(rightW);
        setNumericClick(rightS);
    }

    private void setNumericClick(EditText editText) {
        editText.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(editText);
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(editText);
                }
            }
        });
    }

    /**
     * Reads UI values, refreshes the local section preview, and optionally pushes changes to the
     * active OpenGL create controller.
     */
    private void updatePreview(boolean pushToController) {
        readValuesFromFields();
        updateModeButtons();

        tempPoints = flat ? calcolaNuoveZ(point3DS) : clonePoints(point3DS);

        if (sezioneView != null) {
            sezioneView.setPoints(tempPoints);
        }

        if (pushToController) {
            CreateSurfaceController controller = CreateSurfaceController.getActiveController();
            if (controller != null && controller.getMode() == CreateSurfaceController.MODE_TRENCH) {
                controller.syncTrenchParamsFromLegacy();
            }
        }
    }

    private void readValuesFromFields() {
        try {
            leftW_d = Math.max(0.0, Double.parseDouble(Utils.writeMetri(leftW.getText().toString())));
        } catch (Exception e) {
            Log.w(TAG, "Invalid left width", e);
        }

        try {
            leftS_d = Double.parseDouble(Utils.writeGradi(leftS.getText().toString()));
        } catch (Exception e) {
            Log.w(TAG, "Invalid left slope", e);
        }

        try {
            rightW_d = Math.max(0.0, Double.parseDouble(Utils.writeMetri(rightW.getText().toString())));
        } catch (Exception e) {
            Log.w(TAG, "Invalid right width", e);
        }

        try {
            rightS_d = Double.parseDouble(Utils.writeGradi(rightS.getText().toString()));
        } catch (Exception e) {
            Log.w(TAG, "Invalid right slope", e);
        }

        try {
            startZ_d = Double.parseDouble(Utils.writeMetri(etStart.getText().toString()));
        } catch (Exception e) {
            Log.w(TAG, "Invalid start Z", e);
        }

        try {
            endZ_d = Double.parseDouble(Utils.writeMetri(etEnd.getText().toString()));
        } catch (Exception e) {
            Log.w(TAG, "Invalid end Z", e);
        }
    }

    private void updateModeButtons() {
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
    }

    private void initialiseStartEndIfNeeded() {
        if (point3DS == null || point3DS.length == 0) return;

        if (Double.isNaN(startZ_d)) {
            startZ_d = point3DS[0].getZ();
        }
        if (Double.isNaN(endZ_d)) {
            endZ_d = point3DS[point3DS.length - 1].getZ();
        }
    }

    /**
     * Applies the flat centerline to legacy point3DS only when OK is pressed. In OpenGL Create,
     * the controller rebuilds directly from DataSaved.points_Create and these static params.
     */
    private void commitToLegacyPointsIfNeeded() {
        if (flat && tempPoints != null && tempPoints.length == point3DS.length) {
            for (int i = 0; i < point3DS.length; i++) {
                if (point3DS[i] != null && tempPoints[i] != null) {
                    point3DS[i].setZ(tempPoints[i].getZ());
                }
            }
        } else if (point3DS != null && point3DS.length >= 2) {
            try {
                point3DS[0].setZ(startZ_d);
                point3DS[point3DS.length - 1].setZ(endZ_d);
            } catch (Exception ignored) {
            }
        }

        Activity_Crea_Superficie.point3DS = point3DS;
    }

    /**
     * Professional longitudinal flattening by cumulative station, not projection on P0-PN chord.
     */
    public Point3D[] calcolaNuoveZ(Point3D[] points) {
        if (points == null || points.length < 2) return clonePoints(points);

        double[] station = computeStations(points);
        double total = station[station.length - 1];

        Point3D[] result = new Point3D[points.length];
        for (int i = 0; i < points.length; i++) {
            Point3D p = points[i];
            if (p == null) continue;

            double t = total <= 1e-9 ? 0.0 : station[i] / total;
            double z = startZ_d + t * (endZ_d - startZ_d);
            result[i] = new Point3D(p.getId(), p.getX(), p.getY(), z, p.getName());
        }
        return result;
    }

    private double[] computeStations(Point3D[] points) {
        double[] station = new double[points.length];
        station[0] = 0.0;
        for (int i = 1; i < points.length; i++) {
            Point3D a = points[i - 1];
            Point3D b = points[i];
            if (a == null || b == null) {
                station[i] = station[i - 1];
            } else {
                station[i] = station[i - 1] + Math.hypot(b.getX() - a.getX(), b.getY() - a.getY());
            }
        }
        return station;
    }

    private Point3D[] clonePoints(Point3D[] points) {
        if (points == null) return new Point3D[0];
        Point3D[] out = new Point3D[points.length];
        for (int i = 0; i < points.length; i++) {
            Point3D p = points[i];
            if (p != null) {
                out[i] = new Point3D(p.getId(), p.getX(), p.getY(), p.getZ(), p.getName());
            }
        }
        return out;
    }
}
