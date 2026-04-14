package gui.projects;

import static gui.MyApp.folderPath;
import static gui.projects.Dialog_Trench.leftS_d;
import static gui.projects.Dialog_Trench.leftW_d;
import static gui.projects.Dialog_Trench.rightS_d;
import static gui.projects.Dialog_Trench.rightW_d;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.stx_dig.R;

import java.io.File;
import java.io.IOException;

import dxf.ExportDXF_1P;
import dxf.ExportDXF_AB;
import dxf.ExportDXF_Area;
import dxf.ExportDXF_Trench;
import dxf.ExportDXF_Triangles;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import utils.FullscreenActivity;
import utils.MyData;


public class SaveFileDialog {
    static double conversionFactor = 1;
    static boolean isPRO;
    Activity activity;
    public Dialog dialog;
    Button save, exit;
    EditText fileName;
    CustomQwertyDialog qwertyDialog;
    int flag;
    double[] param;
    boolean useFeet, addPRJ;
    String fileExtension = ".pstx";
    String percorso;

    public SaveFileDialog(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_save_filename);

    }

    public void show(int flag, double[] param, boolean addPRJ, String percorso) {
        this.flag = flag;
        this.param = param;
        this.addPRJ = addPRJ;
        this.percorso = percorso;
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();

        qwertyDialog = new CustomQwertyDialog(activity, null);
    }

    private void findView() {
        isPRO = MyApp.licenseType > 1;
        save = dialog.findViewById(R.id.save);
        exit = dialog.findViewById(R.id.exit);
        fileName = dialog.findViewById(R.id.fileName);
        switch (MyData.get_Int("Unit_Of_Measure")) {
            case 0:
            case 1:
                conversionFactor = 1;
                break;

            case 2:
            case 3:
                conversionFactor = 0.3048006096;
                break;
            case 4:
            case 5:
                conversionFactor = 0.3048006096;
                break;
            case 6:
            case 7:
                conversionFactor = 0.3048;
                break;
        }
        if (isPRO) {
            fileExtension = ".pstx";
        } else {
            fileExtension = ".pstx";
        }
    }

    private void onClick() {

        fileName.setOnClickListener((View v) -> {
            if (!qwertyDialog.dialog.isShowing())
                qwertyDialog.show(fileName);
        });

        save.setOnClickListener((View vw) -> {

            if (!addPRJ) {
                if (!fileName.getText().toString().equals("") && !fileName.getText().toString().contains(".")) {

                    String path = Environment.getExternalStorageDirectory().toString() + folderPath + "/Projects/" + fileName.getText().toString();
                    File directory = new File(path);
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    if (activity instanceof Activity_Crea_Superficie) {
                        useFeet = MyData.get_Int("Unit_Of_Measure") > 1;
                        switch (flag) {
                            case 0:
                                // Salva il dxf 1 punto
                                double[] centerPoint = {DataSaved.puntiProgetto[0].x, DataSaved.puntiProgetto[0].y, DataSaved.puntiProgetto[0].z}; // Centro del quadrato

                                String filename = fileName.getText().toString() + fileExtension;

                                //DXFGenerator generator = new DXFGenerator(centerPoint, param[0], filename, path, useFeet);
                                ExportDXF_1P generator = new ExportDXF_1P(centerPoint, param[0], filename, path, conversionFactor);
                                try {
                                    generator.generateDXF();

                                    MyData.push("progettoSelected", path + "/" + filename);
                                    MyData.push("progettoSelected_POLY", path + "/" + filename);
                                    MyData.push("progettoSelected_POINT", path + "/" + filename);
                                    DataSaved.progettoSelected = path + "/" + filename;
                                    DataSaved.progettoSelected_POLY = path + "/" + filename;
                                    DataSaved.progettoSelected_POINT = path + "/" + filename;
                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();

                                } catch (IOException e) {
                                    new CustomToast(activity, e.toString());
                                    dialog.dismiss();
                                }

                                break;

                            case 1:

                                String filenamea = fileName.getText().toString() + fileExtension;

                                //DXFGeneratorAB dxfGeneratorAB = new DXFGeneratorAB(Activity_Crea_Superficie.puntiAB, filenamea, path, useFeet);
                                ExportDXF_AB dxfGeneratorAB = new ExportDXF_AB(Activity_Crea_Superficie.puntiAB, filenamea, path, conversionFactor);


                                try {
                                    dxfGeneratorAB.generateDXF();
                                    MyData.push("progettoSelected", path + "/" + filenamea);
                                    MyData.push("progettoSelected_POLY", path + "/" + filenamea);
                                    MyData.push("progettoSelected_POINT", path + "/" + filenamea);
                                    DataSaved.progettoSelected = path + "/" + filenamea;
                                    DataSaved.progettoSelected_POLY = path + "/" + filenamea;
                                    DataSaved.progettoSelected_POINT = path + "/" + filenamea;
                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();
                                } catch (IOException e) {
                                    new CustomToast(activity, e.toString()).show_error();
                                    dialog.dismiss();
                                }

                                break;

                            case 2:

                                String filenameb = fileName.getText().toString() + fileExtension;

                                //DxfGeneratorArea dxfGeneratorArea = new DxfGeneratorArea(Activity_Crea_Superficie.coordinateP, filenameb, path, useFeet);
                                ExportDXF_Area dxfGeneratorArea = new ExportDXF_Area(Activity_Crea_Superficie.coordinateP, filenameb, path, conversionFactor);

                                try {
                                    dxfGeneratorArea.generateDXF();
                                    MyData.push("progettoSelected", path + "/" + filenameb);
                                    MyData.push("progettoSelected_POLY", path + "/" + filenameb);
                                    MyData.push("progettoSelected_POINT", path + "/" + filenameb);
                                    DataSaved.progettoSelected = path + "/" + filenameb;
                                    DataSaved.progettoSelected_POLY = path + "/" + filenameb;
                                    DataSaved.progettoSelected_POINT = path + "/" + filenameb;
                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();
                                } catch (Exception e) {
                                    Log.e("ErrDXF", e.toString());
                                    new CustomToast(activity, e.toString()).show_error();
                                    dialog.dismiss();
                                }
                                break;

                            case 3:
                                String filenameDre = fileName.getText().toString() + fileExtension;
                                ExportDXF_Trench exportDXFTrench = new ExportDXF_Trench(Activity_Crea_Superficie.point3DS, leftW_d, rightW_d, leftS_d, rightS_d, filenameDre, path, conversionFactor);
                                try {
                                    exportDXFTrench.generateDXF();
                                    MyData.push("progettoSelected", path + "/" + filenameDre);
                                    MyData.push("progettoSelected_POLY", path + "/" + filenameDre);
                                    MyData.push("progettoSelected_POINT", path + "/" + filenameDre);
                                    DataSaved.progettoSelected = path + "/" + filenameDre;
                                    DataSaved.progettoSelected_POLY = path + "/" + filenameDre;
                                    DataSaved.progettoSelected_POINT = path + "/" + filenameDre;
                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();
                                } catch (Exception e) {
                                    Log.e("ErrDXF", e.toString());
                                    new CustomToast(activity, e.toString()).show_error();
                                    dialog.dismiss();
                                }
                                break;

                            case 4:
                                String filenameTri = fileName.getText().toString() + fileExtension;
                                ExportDXF_Triangles dxfGeneratorTriangles = new ExportDXF_Triangles(Activity_Crea_Superficie.point3DS, filenameTri, path, conversionFactor);

                                try {
                                    dxfGeneratorTriangles.generateDXF();
                                    MyData.push("progettoSelected", path + "/" + filenameTri);
                                    MyData.push("progettoSelected_POLY", path + "/" + filenameTri);
                                    MyData.push("progettoSelected_POINT", path + "/" + filenameTri);
                                    DataSaved.progettoSelected = path + "/" + filenameTri;
                                    DataSaved.progettoSelected_POLY = path + "/" + filenameTri;
                                    DataSaved.progettoSelected_POINT = path + "/" + filenameTri;
                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();
                                } catch (Exception e) {
                                    Log.e("ErrDXF", e.toString());
                                    new CustomToast(activity, e.toString()).show_error();
                                    dialog.dismiss();
                                }
                                break;

                        }
                    }
                } else {
                    new CustomToast(activity, "Missing name/Error!").show_error();

                }
            } else {

                if (!fileName.getText().toString().equals("") && !fileName.getText().toString().contains(".")) {

                    if (activity instanceof Activity_Crea_Superficie) {
                        useFeet = MyData.get_Int("Unit_Of_Measure") > 1;
                        switch (flag) {
                            case 0:
                                // Salva il dxf 1 punto
                                double[] centerPoint = {DataSaved.puntiProgetto[0].x, DataSaved.puntiProgetto[0].y, DataSaved.puntiProgetto[0].z}; // Centro del quadrato

                                String filename = fileName.getText().toString() + fileExtension;

                                //DXFGenerator generator = new DXFGenerator(centerPoint, param[0], filename, percorso, useFeet);
                                ExportDXF_1P generator = new ExportDXF_1P(centerPoint, param[0], filename, percorso, conversionFactor);
                                try {
                                    generator.generateDXF();

                                    MyData.push("progettoSelected", percorso + "/" + filename);
                                    MyData.push("progettoSelected_POLY", percorso + "/" + filename);
                                    MyData.push("progettoSelected_POINT", percorso + "/" + filename);
                                    DataSaved.progettoSelected = percorso + "/" + filename;
                                    DataSaved.progettoSelected_POLY = percorso + "/" + filename;
                                    DataSaved.progettoSelected_POINT = percorso + "/" + filename;

                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();

                                } catch (IOException e) {
                                    new CustomToast(activity, e.toString());
                                    dialog.dismiss();
                                }

                                break;

                            case 1:

                                String filenamea = fileName.getText().toString() + fileExtension;

                                //DXFGeneratorAB dxfGeneratorAB = new DXFGeneratorAB(Activity_Crea_Superficie.puntiAB, filenamea, percorso, useFeet);
                                ExportDXF_AB dxfGeneratorAB = new ExportDXF_AB(Activity_Crea_Superficie.puntiAB, filenamea, percorso, conversionFactor);

                                try {
                                    dxfGeneratorAB.generateDXF();
                                    MyData.push("progettoSelected", percorso + "/" + filenamea);
                                    MyData.push("progettoSelected_POLY", percorso + "/" + filenamea);
                                    MyData.push("progettoSelected_POINT", percorso + "/" + filenamea);
                                    DataSaved.progettoSelected = percorso + "/" + filenamea;
                                    DataSaved.progettoSelected_POLY = percorso + "/" + filenamea;
                                    DataSaved.progettoSelected_POINT = percorso + "/" + filenamea;

                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();
                                } catch (IOException e) {
                                    new CustomToast(activity, e.toString()).show_error();
                                    dialog.dismiss();
                                }

                                break;

                            case 2:

                                String filenameb = fileName.getText().toString() + fileExtension;

                                //DxfGeneratorArea dxfGeneratorArea = new DxfGeneratorArea(Activity_Crea_Superficie.coordinateP, filenameb, percorso, useFeet);
                                ExportDXF_Area dxfGeneratorArea = new ExportDXF_Area(Activity_Crea_Superficie.coordinateP, filenameb, percorso, conversionFactor);

                                try {
                                    dxfGeneratorArea.generateDXF();
                                    MyData.push("progettoSelected", percorso + "/" + filenameb);
                                    MyData.push("progettoSelected_POLY", percorso + "/" + filenameb);
                                    MyData.push("progettoSelected_POINT", percorso + "/" + filenameb);
                                    DataSaved.progettoSelected = percorso + "/" + filenameb;
                                    DataSaved.progettoSelected_POLY = percorso + "/" + filenameb;
                                    DataSaved.progettoSelected_POINT = percorso + "/" + filenameb;

                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();
                                } catch (Exception e) {
                                    Log.e("ErrDXF", e.toString());
                                    new CustomToast(activity, e.toString()).show_error();
                                    dialog.dismiss();
                                }
                                break;

                            case 3:
                                String filenameDre = fileName.getText().toString() + fileExtension;
                                ExportDXF_Trench exportDXFTrench = new ExportDXF_Trench(Activity_Crea_Superficie.point3DS, leftW_d, rightW_d, leftS_d, rightS_d, filenameDre, percorso, conversionFactor);
                                try {
                                    exportDXFTrench.generateDXF();
                                    MyData.push("progettoSelected", percorso + "/" + filenameDre);
                                    MyData.push("progettoSelected_POLY", percorso + "/" + filenameDre);
                                    MyData.push("progettoSelected_POINT", percorso + "/" + filenameDre);
                                    DataSaved.progettoSelected = percorso + "/" + filenameDre;
                                    DataSaved.progettoSelected_POLY = percorso + "/" + filenameDre;
                                    DataSaved.progettoSelected_POINT = percorso + "/" + filenameDre;

                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();
                                } catch (Exception e) {
                                    Log.e("ErrDXF", e.toString());
                                    new CustomToast(activity, e.toString()).show_error();
                                    dialog.dismiss();
                                }
                                break;

                            case 4:
                                String filenameTri = fileName.getText().toString() + fileExtension;
                                //DxfGeneratorTriangles dxfGeneratorTriangles = new DxfGeneratorTriangles(Activity_Crea_Superficie.point3DS, filenameTri, percorso, useFeet);
                                ExportDXF_Triangles dxfGeneratorTriangles = new ExportDXF_Triangles(Activity_Crea_Superficie.point3DS, filenameTri, percorso, conversionFactor);

                                try {
                                    dxfGeneratorTriangles.generateDXF();
                                    MyData.push("progettoSelected", percorso + "/" + filenameTri);
                                    MyData.push("progettoSelected_POLY", percorso + "/" + filenameTri);
                                    MyData.push("progettoSelected_POINT", percorso + "/" + filenameTri);
                                    DataSaved.progettoSelected = percorso + "/" + filenameTri;
                                    DataSaved.progettoSelected_POLY = percorso + "/" + filenameTri;
                                    DataSaved.progettoSelected_POINT = percorso + "/" + filenameTri;

                                    new CustomToast(activity, "File Saved").show();
                                    setAct();
                                    dialog.dismiss();
                                } catch (Exception e) {
                                    Log.e("ErrDXF", e.toString());
                                    new CustomToast(activity, e.toString()).show_error();
                                    dialog.dismiss();
                                }
                                break;

                        }
                    }
                } else {
                    new CustomToast(activity, "Missing name/Error!").show_error();

                }
            }
        });

        exit.setOnClickListener((View vw) -> {
            dialog.dismiss();
        });
    }

    private void setAct() {
        if (activity.getIntent().getStringExtra("whoPRJ") != null) {
            if (activity.getIntent().getStringExtra("whoPRJ").equals("DIG")) {
                activity.startActivity(new Intent(activity, Activity_Home_Page.class));
                activity.finish();
            } else {
                activity.startActivity(new Intent(activity, PickProject.class));
                activity.finish();
            }
        } else {
            activity.startActivity(new Intent(activity, Activity_Home_Page.class));
            activity.finish();
        }
    }
}
