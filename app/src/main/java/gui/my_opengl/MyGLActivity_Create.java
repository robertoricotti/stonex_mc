package gui.my_opengl;

import static gui.my_opengl.My3DActivity.glVista3d;
import static gui.my_opengl.My3DActivity.isPan;
import static utils.MyTypes.EXCAVATOR;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;


import com.example.stx_dig.R;

import dxf.Point3D;
import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.HeadingDialog;
import gui.draw_class.MyColorClass;
import gui.projects.Dialog_Edita_Punti3D;
import gui.projects.Dialog_Parametri_AB;
import gui.projects.Dialog_Trench;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;
import services.TriangleService;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

/**
 * OpenGL surface creator.
 * Non-invasive version: original GL/machine/sidebar logic is preserved;
 * only add/remove/edit/save are connected to CreateSurfaceController.
 */
public class MyGLActivity_Create extends BaseClass {

    private static final String TAG = "GL_CREATE_ACTIVITY";

    public static double[] spigoloSelezionato = new double[3];

    String proj = null;
    String type = null;
    String percorso = null;
    String whoPROJ = null;

    MyGLSurfaceView_Create glSurfaceViewCreate;
    CreateSurfaceController createController;
    CreateSurfaceSaveDialog createSurfaceSaveDialog;

    ImageView digMenu, bucketEdgeL, bucketEdgeC, bucketEdgeR, add_point, remove_point, edit_point, salva;
    ImageView gl_facce, gl_fill, gl_poly, gl_point, gl_text, navigatorHDT, gpsStat, statoImg;
    ImageView gl_2d3d,gl_croce,gl_gradient;

    public static boolean gFacce, gFill, gPoly, gPoint, gText,gGradient;

    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    TextView mostraCoor;
    HeadingDialog headingDialog;
    Dialog_Parametri_AB dialogParametriAB;
    Dialog_Trench dialogTrench;

    boolean addPRJ = true;
    double tempOffsetH=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CreateSurfaceController.resetCreateData();
        readIntent();
        initCreateToggles();
        initCreateGlDefaults();
        DataSaved.selectedPoly=null;
        tempOffsetH=DataSaved.offsetH;
        DataSaved.offsetH=0;
        setContentView(R.layout.activity_my_glactivity);
        DataSaved.projectTAG="DXF";
        try {
            startService(new Intent(this, TriangleService.class));

        } catch (Exception e) {
            Log.e(TAG, "Unable to start TriangleService", e);
        }

        findView();
        initCameraFromMemory();
        initCreateWorkflow();
        onClick();
        updateUI();
    }

    private void readIntent() {
        try {
            proj = getIntent().getStringExtra("proj");
        } catch (Exception ignored) {
            proj = null;
        }
        try {
            type = getIntent().getStringExtra("type");
        } catch (Exception ignored) {
            type = null;
        }
        try {
            percorso = getIntent().getStringExtra("mPath");
        } catch (Exception ignored) {
            percorso = null;
        }
        try {
            whoPROJ = getIntent().getStringExtra("whoPRJ");
        } catch (Exception ignored) {
            whoPROJ = null;
        }
        addPRJ = "OVER".equalsIgnoreCase(type);
        Log.d("ProjectType", proj + "\n" + type + "\n" + percorso + "\n" + whoPROJ);
    }

    private void initCreateToggles() {
        gFacce = true;
        gFill = true;
        gPoly = true;
        gPoint = true;
        gText = true;
        gGradient=false;
    }

    private void findView() {
        glSurfaceViewCreate = findViewById(R.id.glSurfaceViewCreate);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        headingDialog = new HeadingDialog(this);
        gl_gradient=findViewById(R.id.gl_gradient);
        mostraCoor = findViewById(R.id.mostraCoor);
        statoImg = findViewById(R.id.statoImg);
        gpsStat = findViewById(R.id.gpsStat);
        navigatorHDT = findViewById(R.id.navigatorHDT);
        digMenu = findViewById(R.id.digMenu);
        bucketEdgeL = findViewById(R.id.bucketEdgeL);
        bucketEdgeC = findViewById(R.id.bucketEdgeC);
        bucketEdgeR = findViewById(R.id.bucketEdgeR);
        add_point = findViewById(R.id.add_point);
        remove_point = findViewById(R.id.remove_point);
        edit_point = findViewById(R.id.edit_point);
        salva = findViewById(R.id.salva);
        gl_facce = findViewById(R.id.gl_facce);
        gl_fill = findViewById(R.id.gl_fill);
        gl_poly = findViewById(R.id.gl_poly);
        gl_point = findViewById(R.id.gl_point);
        gl_text = findViewById(R.id.gl_text);
        gl_2d3d = findViewById(R.id.gl_2d3d);
        gl_croce=findViewById(R.id.gl_croce);

        if (DataSaved.isWL == EXCAVATOR) {
            bucketEdgeL.setImageResource(R.drawable.benna_misura_sinistra);
            bucketEdgeC.setImageResource(R.drawable.benna_misura_cnt);
            bucketEdgeR.setImageResource(R.drawable.benna_misura_destra);
        } else {
            bucketEdgeL.setImageResource(R.drawable.lama_misura_sinistra);
            bucketEdgeC.setImageResource(R.drawable.lama_misura_cnt);
            bucketEdgeR.setImageResource(R.drawable.lama_misura_destra);
        }

    }


    private void initCameraFromMemory() {
        try {
            MyGLRenderer.scale = MyData.get_Float("glScale");
            MyGLRenderer.angleX = MyData.get_Float("glAngleX");
            MyGLRenderer.angleY = MyData.get_Float("glAngleY");
            MyGLRenderer.panX = 0f;
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

        if (MyGLRenderer.scale < 0.09f) MyGLRenderer.scale = 0.09f;
        if (glVista3d != 0 && glVista3d != 1) glVista3d = 1;
        if (DataSaved.typeView != 0 && DataSaved.typeView != 1) DataSaved.typeView = 1;
    }

    private void initCreateWorkflow() {
        createController = new CreateSurfaceController(proj);
        createSurfaceSaveDialog = new CreateSurfaceSaveDialog(this, createController, addPRJ, percorso);
    }

    private void onClick() {
        gl_gradient.setOnClickListener(v -> {
            gGradient=!gGradient;
            if(gGradient){
                gFill=false;
            }
        });
        gl_croce.setOnClickListener(v -> {
            if (DataSaved.showAlign == 0) {
                DataSaved.showAlign = 1;
            } else if (DataSaved.showAlign == 1) {
                DataSaved.showAlign = 0;
            }
        });
        bucketEdgeL.setOnClickListener(v -> {
            DataSaved.bucketEdge = -1;
            spigoloSelezionato = ExcavatorLib.bucketLeftCoord;
            requestGlRender();
            updateUI();
        });

        bucketEdgeC.setOnClickListener(v -> {
            DataSaved.bucketEdge = 0;
            spigoloSelezionato = ExcavatorLib.bucketCoord;
            requestGlRender();
            updateUI();
        });

        bucketEdgeR.setOnClickListener(v -> {
            DataSaved.bucketEdge = 1;
            spigoloSelezionato = ExcavatorLib.bucketRightCoord;
            requestGlRender();
            updateUI();
        });
        add_point.setOnClickListener(v -> addCreatePoint());
        remove_point.setOnClickListener(v -> removeCreatePoint());
        edit_point.setOnClickListener(v -> editCreateData());
        salva.setOnClickListener(v -> saveCreateSurface());

        if (gpsStat != null) {
            gpsStat.setOnClickListener(v -> {
                if (!dialogGnssCoordinates.alertDialog.isShowing()) dialogGnssCoordinates.show();
            });
        }

        if (gl_2d3d != null) {
            gl_2d3d.setOnClickListener(v -> {
                glVista3d += 1;
                glVista3d = glVista3d % 2;
                requestGlRender();
                updateUI();
            });
        }

        gl_facce.setOnClickListener(v -> {
            gFacce = !gFacce;
            requestGlRender();
            updateUI();
        });
        gl_fill.setOnClickListener(v -> {
            gFill = !gFill;
            if(gFill){
                gGradient=false;
            }
            requestGlRender();
            updateUI();
        });

        gl_poly.setOnClickListener(v -> {
            gPoly = !gPoly;
            requestGlRender();
            updateUI();
        });
        gl_point.setOnClickListener(v -> {
            gPoint = !gPoint;
            requestGlRender();
            updateUI();
        });
        gl_text.setOnClickListener(v -> {
            gText = !gText;
            requestGlRender();
            updateUI();
        });

        digMenu.setOnClickListener(v -> setAct());
    }

    private void addCreatePoint() {
        if (createController == null) return;
        if (!createController.canAddPoint()) {
            if (createController.getMode() == CreateSurfaceController.MODE_PLAN) {
                new CustomToast(this, "PLAN: 1pt Max").show_alert();
            } else if (createController.getMode() == CreateSurfaceController.MODE_AB) {
                new CustomToast(this, "AB: 2pts Max").show_alert();
            } else if (createController.getMode() == CreateSurfaceController.MODE_DITCH) {
                new CustomToast(this, "DITCH: 1pt Max").show_alert();
            } else {
                new CustomToast(this, "Point Not Added").show_alert();
            }
            return;
        }
        if (createController.addCurrentMachinePoint()) {
            requestGlRender();
            updateUI();
            if (createController.getMode() == CreateSurfaceController.MODE_DITCH
                    && !createController.isDitchPointRoleLocked()) {
                showDitchPointPositionDialog();
            }
        } else {
            new CustomToast(this, "Invalid Coordinates").show_error();
        }
    }

    private void removeCreatePoint() {
        if (createController == null) return;
        if (createController.removeLastPoint()) {
            new CustomToast(this, "POINT REMOVED").show();
        }
        requestGlRender();
        updateUI();
    }

    private void editCreateData() {
        if (createController == null) return;
        switch (createController.getMode()) {
            case CreateSurfaceController.MODE_PLAN:
                showPlanSizeDialog();
                break;
            case CreateSurfaceController.MODE_AB:
                if (createController.getPickedCount() < 2) {
                    new CustomToast(this, "AB: 2pts are needed").show_alert();
                    return;
                }
                createController.syncLegacyStatics();
                createController.prepareABDialog();
                dialogParametriAB = new Dialog_Parametri_AB(this);
                dialogParametriAB.show();
                dialogParametriAB.dialog.setOnDismissListener(d -> {
                    createController.syncABParamsFromLegacy();
                    requestGlRender();
                    updateUI();
                });
                break;
            case CreateSurfaceController.MODE_TRENCH:
                if (createController.getPickedCount() < 2) {
                    new CustomToast(this, "TRENCH: 2pts Min").show_alert();
                    return;
                }
                createController.syncLegacyStatics();
                dialogTrench = new Dialog_Trench(this, createController.getTrenchOrTrianglePoints());
                dialogTrench.show();
                dialogTrench.dialog.setOnDismissListener(d -> {
                    createController.syncTrenchParamsFromLegacy();
                    requestGlRender();
                    updateUI();
                });
                break;
            case CreateSurfaceController.MODE_DITCH:
                if (createController.getPickedCount() < 1) {
                    new CustomToast(this, "DITCH: 1pt is needed").show_alert();
                    return;
                }
                showDitchEditDialog();
                break;
            case CreateSurfaceController.MODE_AREA:
            case CreateSurfaceController.MODE_TRIANGLES:
            default:
                showEditPointsDialog();
                break;
        }
    }

    private void showDitchPointPositionDialog() {
        final String[] labels = {"P1", "P2", "P3", "P4", "P5", "P6"};
        final int[] selected = {createController.getDitchMeasuredPointIndex()};

        FrameLayout root = new FrameLayout(this);

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.ppp16); // nome reale della tua immagine
        background.setScaleType(ImageView.ScaleType.FIT_CENTER);
        background.setAdjustViewBounds(false);
        background.setAlpha(1.0f);

        root.addView(background, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        radioGroup.setBackgroundColor(Color.TRANSPARENT);

        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        radioGroup.setPadding(pad, pad, pad, pad);

        for (int i = 0; i < labels.length; i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(labels[i]);
            radioButton.setTextSize(18);
            radioButton.setTextColor(Color.BLACK);
            radioButton.setButtonTintList(ColorStateList.valueOf(Color.BLACK));
            radioButton.setBackgroundColor(Color.TRANSPARENT);
            radioButton.setId(1000 + i);
            radioButton.setPadding(0, 0, 0, 0);

            LinearLayout.LayoutParams rbLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rbLp.setMargins(0, 0, 0, pad / 3);

            radioGroup.addView(radioButton, rbLp);
        }

        radioGroup.check(1000 + selected[0]);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selected[0] = checkedId - 1000;
        });

        FrameLayout.LayoutParams radioLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        radioLp.gravity = Gravity.TOP | Gravity.LEFT;
        radioLp.setMargins(pad, pad, 0, 0);

        root.addView(radioGroup, radioLp);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(root)
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    createController.setDitchMeasuredPointIndexOnce(selected[0]);
                    requestGlRender();
                    updateUI();
                })
                .setNegativeButton(android.R.string.cancel, (d, which) -> {
                    createController.lockDefaultDitchMeasuredPointIndex();
                    requestGlRender();
                    updateUI();
                })
                .create();

        dialog.setOnCancelListener(d -> {
            createController.lockDefaultDitchMeasuredPointIndex();
            requestGlRender();
            updateUI();
        });

        dialog.setOnDismissListener(d -> FullscreenActivity.setFullScreen(this));

        dialog.setOnShowListener(d -> {
            FullscreenActivity.setFullScreen(dialog);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
                lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
                window.setAttributes(lp);
            }
        });

        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
    }

    private void showDitchEditDialog() {
        double[] lengths = createController.getDitchSegmentLengths();
        double[] slopes = createController.getDitchSegmentSlopesPercent();

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(Color.WHITE);

        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        content.setPadding(pad, pad, pad, pad);

        EditText[] lengthInputs = new EditText[5];
        EditText[] slopeInputs = new EditText[5];

        for (int i = 0; i < 5; i++) {
            content.addView(newDitchLabel("P" + (i + 1) + " > P" + (i + 2), 18, i == 0 ? 0 : 12));

            lengthInputs[i] = addDitchLabeledInput(
                    content,
                    "Length "+Utils.getMetriSimbol(),
                    Utils.readUnitOfMeasureLITE(String.valueOf(lengths[i])),
                    false
            );

            slopeInputs[i] = addDitchLabeledInput(
                    content,
                    "Slope %",
                    String.valueOf(slopes[i]),
                    true
            );
        }

        //content.addView(newDitchLabel("Side distances", 18, 12));

        EditText leftWidthInput = addDitchLabeledInput(
                content,
                "Left distance "+Utils.getMetriSimbol(),
                Utils.readUnitOfMeasureLITE(String.valueOf(createController.getDitchLeftWidth())),
                false
        );

        EditText rightWidthInput = addDitchLabeledInput(
                content,
                "Right distance "+Utils.getMetriSimbol(),
                Utils.readUnitOfMeasureLITE(String.valueOf(createController.getDitchRightWidth())),
                false
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);
        scroll.addView(content);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("DITCH surface")
                .setView(scroll)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    FullscreenActivity.setFullScreen(this);
                    try {
                        double[] newLengths = new double[5];
                        double[] newSlopes = new double[5];

                        for (int i = 0; i < 5; i++) {
                            newLengths[i] = Math.max(
                                    0.0,
                                    Double.parseDouble(Utils.writeMetri(lengthInputs[i].getText().toString()))
                            );
                            newSlopes[i] = Double.parseDouble(slopeInputs[i].getText().toString());
                        }

                        double leftWidth = Math.max(
                                0.0,
                                Double.parseDouble(Utils.writeMetri(leftWidthInput.getText().toString()))
                        );

                        double rightWidth = Math.max(
                                0.0,
                                Double.parseDouble(Utils.writeMetri(rightWidthInput.getText().toString()))
                        );

                        createController.setDitchParams(newLengths, newSlopes, leftWidth, rightWidth);
                        requestGlRender();
                        updateUI();
                    } catch (Exception e) {
                        new CustomToast(this, "Invalid Value").show_error();
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    FullscreenActivity.setFullScreen(this);
                })
                .create();

        alertDialog.setOnShowListener(dialog -> {
            FullscreenActivity.setFullScreen(alertDialog);
            lengthInputs[0].requestFocus();
        });

        alertDialog.show();
        FullscreenActivity.setFullScreen(alertDialog);
    }
    private TextView newDitchLabel(String text, int textSizeSp, int topMarginDp) {
        int padH = (int) (10 * getResources().getDisplayMetrics().density);
        int padV = (int) (6 * getResources().getDisplayMetrics().density);
        int topMargin = (int) (topMarginDp * getResources().getDisplayMetrics().density);

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(textSizeSp);
        textView.setBackgroundColor(Color.DKGRAY);
        textView.setPadding(padH, padV, padH, padV);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, topMargin, 0, 0);
        textView.setLayoutParams(lp);

        return textView;
    }

    private EditText addDitchLabeledInput(LinearLayout parent, String label, String value, boolean signed) {
        int pad = (int) (8 * getResources().getDisplayMetrics().density);
        int marginBottom = (int) (6 * getResources().getDisplayMetrics().density);

        parent.addView(newDitchLabel(label, 14, 0));

        EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setText(value);
        editText.setSelectAllOnFocus(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        editText.setTextColor(Color.BLACK);
        editText.setHintTextColor(Color.GRAY);
        editText.setBackgroundColor(Color.WHITE);
        editText.setPadding(pad, 0, pad, 0);

        int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        if (signed) {
            inputType |= InputType.TYPE_NUMBER_FLAG_SIGNED;
        }
        editText.setInputType(inputType);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 0, 0, marginBottom);
        editText.setLayoutParams(lp);

        parent.addView(editText);
        return editText;
    }

    private EditText newDitchNumberInput(String hint, String value, boolean signed) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(hint);
        input.setText(value);
        input.setSelectAllOnFocus(true);
        int type = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        if (signed) type |= InputType.TYPE_NUMBER_FLAG_SIGNED;
        input.setInputType(type);
        return input;
    }

    private void showPlanSizeDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        TextView sideLabel = new TextView(this);
        sideLabel.setText("PLAN Side "+Utils.getMetriSimbol());

        final EditText sideEditText = new EditText(this);
        sideEditText.setSingleLine(true);
        sideEditText.setText(Utils.readUnitOfMeasureLITE(String.valueOf(createController.getPlanSide())));
        sideEditText.setSelectAllOnFocus(true);
        sideEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        sideEditText.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        TextView zLabel = new TextView(this);
        zLabel.setText("PLAN Elevation "+Utils.getMetriSimbol());

        final EditText zEditText = new EditText(this);
        zEditText.setSingleLine(true);

        double planZ = createController.getPlanZ();
        if (!Double.isNaN(planZ) && !Double.isInfinite(planZ)) {
            zEditText.setText(Utils.readUnitOfMeasureLITE(String.valueOf(planZ)));
        }

        zEditText.setSelectAllOnFocus(true);
        zEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        zEditText.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_NUMBER_FLAG_SIGNED
        );

        layout.addView(sideLabel);
        layout.addView(sideEditText);
        layout.addView(zLabel);
        layout.addView(zEditText);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("PLAN")
                .setView(layout)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    FullscreenActivity.setFullScreen(this);
                    try {
                        double side = Double.parseDouble(Utils.writeMetri(sideEditText.getText().toString()));
                        double z = Double.parseDouble(Utils.writeMetri(zEditText.getText().toString()));

                        createController.setPlanSide(side);

                        if (!createController.setPlanZ(z)) {
                            new CustomToast(this, "Invalid Elevation").show_error();
                            return;
                        }

                        requestGlRender();
                        updateUI();
                    } catch (Exception e) {
                        new CustomToast(this, "Invalid Value").show_error();
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    FullscreenActivity.setFullScreen(this);
                })
                .create();

        alertDialog.setOnShowListener(dialog -> {
            FullscreenActivity.setFullScreen(alertDialog);
            sideEditText.requestFocus();
        });

        alertDialog.show();
        FullscreenActivity.setFullScreen(alertDialog);
    }

    private void showEditPointsDialog() {
        Point3D[] editArray = createController.pickedArray();
        Dialog_Edita_Punti3D dialogEditaPunti3D = new Dialog_Edita_Punti3D(this, editArray);

        dialogEditaPunti3D.setOnPointsChangedListener(points -> {
            createController.replacePickedPoints(points);
            requestGlRender();
            updateUI();
        });

        dialogEditaPunti3D.show();

        Dialog_Edita_Punti3D.dialog.setOnDismissListener(d -> {
            createController.replacePickedPoints(editArray);
            requestGlRender();
            updateUI();
        });
    }

    private void saveCreateSurface() {
        if (createController == null || createSurfaceSaveDialog == null) return;
        if (!createController.isReadyToSave()) {
            new CustomToast(this, "Invalid Surface").show_alert();
            return;
        }
        createController.syncLegacyStatics();
        createSurfaceSaveDialog.show();
    }

    public void updateUI() {
        if(navigatorHDT!=null){
            navigatorHDT.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));
        }
        if (mostraCoor != null) {
            mostraCoor.setTextColor(MyColorClass.colorConstraint);
            try {
                switch (DataSaved.bucketEdge) {
                    case -1:
                        bucketEdgeL.setBackgroundColor(getColor(R.color.yellow));
                        bucketEdgeC.setBackgroundColor(getColor(R.color.nav_gray_color));
                        bucketEdgeR.setBackgroundColor(getColor(R.color.nav_gray_color));
                        spigoloSelezionato = ExcavatorLib.bucketLeftCoord;
                        mostraCoor.setText("LEFT:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[0])) + "\n" +
                                "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[1])) + "\n" +
                                "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[2])));
                        break;
                    case 0:
                        bucketEdgeL.setBackgroundColor(getColor(R.color.nav_gray_color));
                        bucketEdgeC.setBackgroundColor(getColor(R.color.yellow));
                        bucketEdgeR.setBackgroundColor(getColor(R.color.nav_gray_color));
                        spigoloSelezionato = ExcavatorLib.bucketCoord;
                        mostraCoor.setText("CENTER:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[0])) + "\n" +
                                "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[1])) + "\n" +
                                "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[2])));
                        break;
                    case 1:
                        bucketEdgeL.setBackgroundColor(getColor(R.color.nav_gray_color));
                        bucketEdgeC.setBackgroundColor(getColor(R.color.nav_gray_color));
                        bucketEdgeR.setBackgroundColor(getColor(R.color.yellow));
                        spigoloSelezionato = ExcavatorLib.bucketRightCoord;
                        mostraCoor.setText("RIGHT:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[0])) + "\n" +
                                "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[1])) + "\n" +
                                "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[2])));
                        break;
                }
            } catch (Exception ignored) {
                mostraCoor.setText("Coordinate non pronte");
            }
        }

        updateModeIcon();
        tintToggle(gl_facce, gFacce);
        tintToggle(gl_fill, gFill);
        tintToggle(gl_poly, gPoly);
        tintToggle(gl_point, gPoint);
        tintToggle(gl_text, gText);

        if(DataSaved.showAlign==1){
            gl_croce.setImageTintList(getColorStateList(R.color.green));
        }else {
            gl_croce.setImageTintList(getColorStateList(R.color.white));
        }
        if (gpsStat != null)
            gpsStat.setImageTintList(getColorStateList(DataSaved.gpsOk ? R.color.green : R.color.red));

        if (gGradient) {
            gl_gradient.setImageResource(R.drawable.gradient);
        } else {
            gl_gradient.setImageResource(R.drawable.gradient_off);
        }
        if (glVista3d == 1) {
            gl_2d3d.setImageResource(R.drawable.tredi_vista);
            isPan = false;
        } else {
            isPan = true;
            gl_2d3d.setImageResource(R.drawable.duedi_vista);

        }
        float rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rotBus = rotBus % 360;
        navigatorHDT.setRotation(rotBus);
        if (DataSaved.points_Create != null) {
            if (DataSaved.points_Create.size() < 1) {
                remove_point.setVisibility(View.INVISIBLE);
            } else {
                remove_point.setVisibility(View.VISIBLE);
            }
        }

    }

    private void updateModeIcon() {
        if (statoImg == null) return;
        String p = proj == null ? "" : proj;
        switch (p) {
            case "PLAN":
                statoImg.setImageResource(R.drawable.piano_benna);
                break;
            case "AB":
                statoImg.setImageResource(R.drawable.ab_piano_benna);
                break;
            case "AREA":
                statoImg.setImageResource(R.drawable.perimetro_benna);
                break;
            case "TRENCH":
                statoImg.setImageResource(R.drawable.polyline);
                break;
            case "TRIANGLES":
                statoImg.setImageResource(R.drawable.terrain_model);
                break;
            case "DITCH":
                statoImg.setImageResource(R.drawable.ditches);
                break;
            default:
                statoImg.setImageResource(R.drawable.baseline_help_96);
                break;
        }

    }

    private void tintToggle(ImageView view, boolean enabled) {
        if (view != null)
            view.setImageTintList(getColorStateList(enabled ? R.color.green : R.color.white));
    }

    private void requestGlRender() {
        if (glSurfaceViewCreate != null) glSurfaceViewCreate.requestRender();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, TriangleService.class));
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceViewCreate != null) glSurfaceViewCreate.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceViewCreate != null) glSurfaceViewCreate.onResume();
        updateUI();
    }

    private void setAct() {
        if(DataSaved.points_Create!=null){
            if(!DataSaved.points_Create.isEmpty()) {
                if (!headingDialog.dialog.isShowing()) {
                    headingDialog.show();
                }
            }else {
                if (getIntent().getStringExtra("whoPRJ") != null) {
                    if (getIntent().getStringExtra("whoPRJ").equals("DIG")) {
                        startActivity(new Intent(this, My3DActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(this, Activity_Home_Page.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(this, Activity_Home_Page.class));
                    finish();
                }
            }
        }

    }



    private void initCreateGlDefaults() {
        try {
            My3DActivity.glVista3d = Integer.parseInt(MyData.get_String("vista3D"));
        } catch (Exception e) {
            My3DActivity.glVista3d = 0; // se vuoi aprire Create in 2D
            MyData.push("vista3D", String.valueOf(My3DActivity.glVista3d));
        }

        try {
            DataSaved.typeView = Integer.parseInt(MyData.get_String("typeView"));
        } catch (Exception e) {
            DataSaved.typeView = 1;
            MyData.push("typeView", "1");
        }

        if (DataSaved.typeView != 0 && DataSaved.typeView != 1) {
            DataSaved.typeView = 1;
        }

        try {
            MyGLRenderer.scale = MyData.get_Float("glScale");
            MyGLRenderer.angleX = MyData.get_Float("glAngleX");
            MyGLRenderer.angleY = MyData.get_Float("glAngleY");
            MyGLRenderer.angleY_extra = MyData.get_Float("glAngleY_Extra");
            MyGLRenderer.scale_2d = MyData.get_Float("glScale_2d");
        } catch (Exception e) {
            MyGLRenderer.scale = 0.5f;
            MyGLRenderer.scale_2d = 0.5f;
            MyGLRenderer.angleX = -90f;
            MyGLRenderer.angleY = 0f;
            MyGLRenderer.angleY_extra = 0f;
        }

        MyGLRenderer.panX = 0f;
        MyGLRenderer.panY = -0.3f;

        if (MyGLRenderer.scale < 0.09f) {
            MyGLRenderer.scale = 0.09f;
        }

        // fondamentale: se parto in 2D, il touch deve essere pan, non rotate
        My3DActivity.isPan = My3DActivity.glVista3d == 0;
    }
}

