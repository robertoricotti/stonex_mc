package gui.my_opengl;

import static gui.MyApp.folderPath;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.stx_dig.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dxf.ExportDXF_1P;
import dxf.ExportDXF_AB;
import dxf.ExportDXF_Area;
import dxf.ExportDXF_Trench;
import dxf.ExportDXF_Triangles;
import dxf.Point3D;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.projects.Dialog_Trench;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;

public class CreateSurfaceSaveDialog {
    private static final String TAG = "CreateSaveDialog";

    private final Activity activity;
    private final CreateSurfaceController controller;
    private final boolean addPRJ;
    private final String percorso;
    public Dialog dialog;
    private Button save, exit;
    private EditText fileName;
    private CustomQwertyDialog qwertyDialog;
    private double conversionFactor = 1.0;
    private final String fileExtension = ".pstx";

    public CreateSurfaceSaveDialog(Activity activity, CreateSurfaceController controller, boolean addPRJ, String percorso) {
        this.activity = activity;
        this.controller = controller;
        this.addPRJ = addPRJ;
        this.percorso = percorso;
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_save_filename);
    }

    public void show() {
        if (dialog == null) return;
        dialog.setCancelable(false);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            window.setAttributes(wlp);
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
        qwertyDialog = new CustomQwertyDialog(activity, null);
    }

    private void findView() {
        save = dialog.findViewById(R.id.save);
        exit = dialog.findViewById(R.id.exit);
        fileName = dialog.findViewById(R.id.fileName);
        try {
            switch (MyData.get_Int("Unit_Of_Measure")) {
                case 0:
                case 1:
                    conversionFactor = 1.0;
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    conversionFactor = 0.3048006096;
                    break;
                case 6:
                case 7:
                    conversionFactor = 0.3048;
                    break;
                default:
                    conversionFactor = 1.0;
                    break;
            }
        } catch (Exception ignored) {
            conversionFactor = 1.0;
        }
    }

    private void onClick() {
        fileName.setOnClickListener(v -> {
            if (qwertyDialog != null && !qwertyDialog.dialog.isShowing()) {
                qwertyDialog.show(fileName);
            }
        });
        exit.setOnClickListener(v -> dialog.dismiss());
        save.setOnClickListener(v -> saveFile());
    }

    private void saveFile() {
        String name = fileName.getText() == null ? "" : fileName.getText().toString().trim();
        if (name.isEmpty() || name.contains(".")) {
            new CustomToast(activity, "Missing name/Error!").show_error();
            return;
        }
        if (!controller.isReadyToSave()) {
            new CustomToast(activity, "Surface is incomplete").show_alert();
            return;
        }

        String path;
        if (addPRJ) {
            path = percorso;
        } else {
            path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects/" + name;
            File directory = new File(path);
            if (!directory.exists() && !directory.mkdirs()) {
                new CustomToast(activity, "Cannot create folder").show_error();
                return;
            }
        }
        if (path == null || path.trim().isEmpty()) {
            new CustomToast(activity, "Invalid path").show_error();
            return;
        }

        String filename = name + fileExtension;
        try {
            controller.rebuildPreview();
            controller.syncLegacyStatics();

            switch (controller.saveFlag()) {
                case CreateSurfaceController.MODE_PLAN: {
                    Point3D center = DataSaved.points_Create.get(0);
                    new ExportDXF_1P(new double[]{center.getX(), center.getY(), center.getZ()},
                            controller.getPlanSide(), filename, path, conversionFactor).generateDXF();
                    break;
                }
                case CreateSurfaceController.MODE_AB:
                    new ExportDXF_AB(controller.getABPoints(), filename, path, conversionFactor).generateDXF();
                    break;
                case CreateSurfaceController.MODE_AREA:
                    new ExportDXF_Area(controller.getAreaCoordinates(), filename, path, conversionFactor).generateDXF();
                    break;
                case CreateSurfaceController.MODE_TRENCH:
                    new ExportDXF_Trench(controller.getTrenchOrTrianglePoints(),
                            Dialog_Trench.leftW_d, Dialog_Trench.rightW_d,
                            Dialog_Trench.leftS_d, Dialog_Trench.rightS_d,
                            filename, path, conversionFactor).generateDXF();
                    break;
                case CreateSurfaceController.MODE_TRIANGLES:
                    new ExportDXF_Triangles(controller.getTrenchOrTrianglePoints(), filename, path, conversionFactor).generateDXF();
                    break;
            }

            String fullPath = path + "/" + filename;
            MyData.push("progettoSelected", fullPath);
            MyData.push("progettoSelected_POLY", fullPath);
            MyData.push("progettoSelected_POINT", fullPath);
            DataSaved.progettoSelected = fullPath;
            DataSaved.progettoSelected_POLY = fullPath;
            DataSaved.progettoSelected_POINT = fullPath;
            new CustomToast(activity, "File Saved").show();
            dialog.dismiss();

            activity.startActivity(new Intent(activity, Activity_Home_Page.class));
            activity.finish();
        } catch (Exception e) {
            Log.e(TAG, "Save failed", e);
            new CustomToast(activity, e.toString()).show_error();
        }
    }
}
