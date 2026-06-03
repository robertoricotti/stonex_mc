package drill_pile.gui;

import static packexcalib.exca.ExcavatorLib.toolEndCoord;
import static utils.MyTypes.SOLARFARM_MODE;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.dialogs_and_toast.CustomToast;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;
import utils.FullscreenActivity;
import utils.Utils;

public class Dialog_Apply_Z_Drill {
    private static final long UI_UPDATE_INTERVAL_MS = 80;
    private static final String TAG = "ApplyZDrill";

    private boolean uiUpdateRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    Activity activity;
    public Dialog dialog;

    ImageView close, applica, btReset;
    TextView valore;

    DisplayMetrics displayMetrics;
    int larg = 1000, alt = 600;

    public Dialog_Apply_Z_Drill(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        displayMetrics = new DisplayMetrics();
    }

    public void show() {
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        larg = (int) (displayMetrics.widthPixels * 0.65);
        alt = (int) (displayMetrics.heightPixels * 0.45);

        dialog.create();
        dialog.setContentView(R.layout.dialog_apply_z_drill);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(d -> stopUiUpdates());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.25f;

            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(wlp);
        }

        dialog.show();

        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setLayout(larg, alt);

            WindowManager.LayoutParams wlp = dialogWindow.getAttributes();
            wlp.gravity = Gravity.CENTER;
            dialogWindow.setAttributes(wlp);
        }

        FullscreenActivity.setFullScreen(dialog);

        findView();
        init();
        onClick();
        startUiUpdates();
    }

    private void findView() {
        btReset = dialog.findViewById(R.id.btReset);
        close = dialog.findViewById(R.id.close);
        valore = dialog.findViewById(R.id.valore);
        applica = dialog.findViewById(R.id.applica);
    }

    private void init() {
        updateValore();
    }

    private void updateUI() {
        updateValore();
    }

    private void onClick() {
        applica.setOnLongClickListener(v -> {
            applyCurrentToolZToSolarFarmPoints();
            return true;
        });

        btReset.setOnLongClickListener(v -> {
            resetSolarFarmZOverride();
            return true;
        });

        close.setOnClickListener(v -> {
            stopUiUpdates();
            dialog.dismiss();
        });
    }

    private void applyCurrentToolZToSolarFarmPoints() {
        if (DataSaved.Drilling_Mode != SOLARFARM_MODE) {
            new CustomToast(activity, "This function is available only in Solar Farm mode.").show_error();
            return;
        }

        if (DataSaved.drill_points == null || DataSaved.drill_points.isEmpty()) {
            new CustomToast(activity, "No Solar Farm points available.").show_error();
            return;
        }

        if (ReadProjectService.stateStore == null) {
            new CustomToast(activity, "Project state file is not available.").show_error();
            return;
        }

        if (toolEndCoord == null || toolEndCoord.length < 3) {
            new CustomToast(activity, "Tool point elevation is not available.").show_error();
            return;
        }

        double z = toolEndCoord[2];

        if (Double.isNaN(z) || Double.isInfinite(z)) {
            new CustomToast(activity, "Tool point elevation is not valid.").show_error();
            return;
        }

        try {
            ReadProjectService.persistSolarFarmCurrentToolZToAllPoints();

            updateValore();

            new CustomToast(
                    activity,
                    "Tool point elevation applied to all Solar Farm points."
            ).show_alert();

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            new CustomToast(activity, "Unable to apply tool point elevation.").show_error();
        }
    }

    private void resetSolarFarmZOverride() {
        if (DataSaved.Drilling_Mode != SOLARFARM_MODE) {
            new CustomToast(activity, "This function is available only in Solar Farm mode.").show_error();
            return;
        }

        if (DataSaved.drill_points == null || DataSaved.drill_points.isEmpty()) {
            new CustomToast(activity, "No Solar Farm points available.").show_error();
            return;
        }

        if (ReadProjectService.stateStore == null) {
            new CustomToast(activity, "Project state file is not available.").show_error();
            return;
        }

        try {
            ReadProjectService.clearSolarFarmZOverrideAndRestoreOriginalPoints();

            updateValore();

            new CustomToast(
                    activity,
                    "Point elevations restored from the project file."
            ).show_alert();

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            new CustomToast(activity, "Unable to reset point elevations.").show_error();
        }
    }

    private void updateValore() {
        if (valore == null) return;

        valore.setText(getSavedSolarFarmElevationText());
    }

    private String getSavedSolarFarmElevationText() {
        if (DataSaved.Drilling_Mode != SOLARFARM_MODE) {
            return "---";
        }

        if (ReadProjectService.stateStore == null) {
            return "---";
        }

        if (DataSaved.drill_points == null || DataSaved.drill_points.isEmpty()) {
            return "---";
        }

        for (Point3D_Drill p : DataSaved.drill_points) {
            if (p == null) continue;

            String holeId = ProjectStateCsvStore.canonicalHoleId(p);
            if (holeId == null || holeId.trim().isEmpty()) continue;

            Double savedZ = ReadProjectService.stateStore.getHoleZOverride(holeId);

            if (savedZ != null && !Double.isNaN(savedZ) && !Double.isInfinite(savedZ)) {
                return Utils.readUnitOfMeasureLITE(String.valueOf(savedZ))
                        + " " + Utils.getMetriSimbol();
            }
        }

        return "---";
    }

    private final Runnable uiUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!uiUpdateRunning || dialog == null || !dialog.isShowing()) {
                return;
            }

            updateUI();

            handler.postDelayed(this, UI_UPDATE_INTERVAL_MS);
        }
    };

    private void startUiUpdates() {
        if (uiUpdateRunning) return;

        uiUpdateRunning = true;
        handler.post(uiUpdateRunnable);
    }

    private void stopUiUpdates() {
        uiUpdateRunning = false;
        handler.removeCallbacks(uiUpdateRunnable);
    }
}