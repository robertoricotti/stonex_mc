package gui.projects;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stx_dig.R;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

import dxf.Face3D;
import dxf.Point3D;
import dxf.Polyline;
import gui.BaseClass;
import gui.MyApp;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_and_toast.Dialog_Edit_Zeta;
import gui.dialogs_and_toast.Dialog_Edita_Punti;
import gui.dialogs_and_toast.Dialog_GNSS_Coordinates;
import gui.dialogs_and_toast.HeadingDialog;
import gui.dialogs_user_settings.DialogColors;
import gui.dialogs_user_settings.DialogUnitOfMeasure;
import gui.draw_class.MyColorClass;
import gui.my_opengl.My3DActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import utils.DistToPoint;
import utils.MyData;
import utils.Utils;

public class Activity_Crea_Superficie extends BaseClass {
    //TODO planimetria di sfondo in 2D per aggiunta superficie

    Activity previousActivity;
    public static List<Face3D> facceTrench;
    public static Polyline polyTrench;
    public static int indexSel;
    static int countPunti;
    static double z = 0;
    public static List<double[]> coordinateP;
    static int pointIndex;
    public static Point3D[] point3DS;
    TextView mostraCoor;
    ImageView exit, bucketEdgeL, bucketEdgeC, bucketEdgeR, add, remove,
            edit, save, zoomP, zoomM, zoomC, gnssSt, colori, units, bussola, alert, statiImg;
    ProgressBar progressBar;
    HeadingDialog headingDialog;
    DialogUnitOfMeasure dialogUnitOfMeasure;
    Dialog_GNSS_Coordinates dialogGnssCoordinates;
    Dialog_Edita_Punti dialogEditaPunti;
    Dialog_Parametri_AB dialogParametriAb;
    SaveFileDialog saveFileDialog;
    Dialog_Edit_Zeta dialogEditZeta;
    DialogColors dialogColors;
    int spigolo = 0;
    double[] coordSpigolo;
    View crea_Superficie;
    ConstraintLayout panel3;
    boolean zommaIn, zommaOut;
    float rotBus = 0f;
    public static Point3D[] puntiAB;
    static int countPP;
    public static double leftDIST, leftSLOPE, rightDIST, rightSLOPE, distAB, slopeAB;
    String tipo = "";
    boolean addPRJ;
    String percorso = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crea_superficie);
        findView();
        init();
        onClick();
        selSPigolo(spigolo);


    }

    private void findView() {
        exit = findViewById(R.id.uscita);
        bucketEdgeL = findViewById(R.id.bucketEdgeL);
        bucketEdgeC = findViewById(R.id.bucketEdgeC);
        bucketEdgeR = findViewById(R.id.bucketEdgeR);
        add = findViewById(R.id.add_point);
        remove = findViewById(R.id.remove_point);
        edit = findViewById(R.id.edit_point);
        save = findViewById(R.id.salva);
        zoomP = findViewById(R.id.zoomP);
        zoomM = findViewById(R.id.zoomM);
        zoomC = findViewById(R.id.zoomC);
        gnssSt = findViewById(R.id.gpsInfo);
        colori = findViewById(R.id.colori);
        units = findViewById(R.id.units);
        bussola = findViewById(R.id.bussola);
        alert = findViewById(R.id.alertGnss);
        statiImg = findViewById(R.id.statoImg);
        progressBar = findViewById(R.id.progressBar);
        panel3 = findViewById(R.id.panel3D);
        dialogColors = new DialogColors(this);
        crea_Superficie = new Canvas_Crea_Superficie(this);

        mostraCoor = findViewById(R.id.mostraCoor);
        mostraCoor.setTextColor(MyColorClass.colorConstraint);
        colori.setVisibility(View.INVISIBLE);
        units.setVisibility(View.INVISIBLE);

    }

    private void init() {
        DataSaved.scale_Factor3D = MyData.get_Double("scaleFactor3D");
        try {
            tipo = getIntent().getStringExtra("type");
            percorso = getIntent().getStringExtra("mPath");
            switch (tipo) {
                case "OVER":
                    addPRJ = true;
                    break;
                case "NEW":
                    addPRJ = false;
                    break;
            }
        } catch (Exception e) {
            tipo = "";
            addPRJ = false;
        }
        try {
            switch (getIntent().getStringExtra("proj")) {
                case "PLAN":
                    Canvas_Crea_Superficie.mode = 0;
                    if (DataSaved.isWL < 2) {
                        statiImg.setImageResource(R.drawable.piano_benna);
                    } else {
                        statiImg.setImageResource(R.drawable.piano_lama);
                    }
                    break;
                case "AB":

                    Canvas_Crea_Superficie.mode = 1;
                    puntiAB = new Point3D[6];
                    countPP = 0;
                    leftDIST = 20;
                    leftSLOPE = 0;
                    rightDIST = 20;
                    rightSLOPE = 0;
                    if (DataSaved.isWL < 2) {
                        statiImg.setImageResource(R.drawable.ab_piano_benna);
                    } else {
                        statiImg.setImageResource(R.drawable.ab_piano_lama);
                    }
                    break;
                case "AREA":
                    Canvas_Crea_Superficie.mode = 2;
                    pointIndex = 0;

                    if (DataSaved.isWL < 2) {
                        statiImg.setImageResource(R.drawable.perimetro_benna);
                    } else {
                        statiImg.setImageResource(R.drawable.perimetro_lama);
                    }
                    break;
                case "TRENCH":
                    //TODO Trencher
                    statiImg.setImageResource(R.drawable.polyline);
                    Canvas_Crea_Superficie.mode = 3;
                    point3DS = new Point3D[0];
                    facceTrench = new ArrayList<>();
                    polyTrench = new Polyline();

                    break;

                case "TRIANGLES":
                    statiImg.setImageResource(R.drawable.terrain_model);
                    Canvas_Crea_Superficie.mode = 4;
                    point3DS = new Point3D[0];

                    break;
                default:
                    if (DataSaved.isWL < 2) {
                        statiImg.setImageResource(R.drawable.piano_benna);
                    } else {
                        statiImg.setImageResource(R.drawable.piano_lama);
                    }
                    break;
            }
        } catch (Exception e) {
            if (DataSaved.isWL < 2) {
                statiImg.setImageResource(R.drawable.piano_benna);
            } else {
                statiImg.setImageResource(R.drawable.piano_lama);
            }
        }

        progressBar.setVisibility(View.INVISIBLE);
        headingDialog = new HeadingDialog(this);
        dialogGnssCoordinates = new Dialog_GNSS_Coordinates(this);
        dialogUnitOfMeasure = new DialogUnitOfMeasure(this);
        dialogEditaPunti = new Dialog_Edita_Punti(this);
        saveFileDialog = new SaveFileDialog(this);
        dialogParametriAb = new Dialog_Parametri_AB(this);
        dialogEditZeta = new Dialog_Edit_Zeta(this, 100);

        if (DataSaved.isWL < 2) {
            bucketEdgeL.setImageResource(R.drawable.benna_misura_sinistra);
            bucketEdgeC.setImageResource(R.drawable.benna_misura_cnt);
            bucketEdgeR.setImageResource(R.drawable.benna_misura_destra);
        } else {
            bucketEdgeL.setImageResource(R.drawable.lama_misura_sinistra);
            bucketEdgeC.setImageResource(R.drawable.lama_misura_cnt);
            bucketEdgeR.setImageResource(R.drawable.lama_misura_destra);
        }
        panel3.addView(crea_Superficie);
        panel3.setBackgroundColor(MyColorClass.colorSfondo);
        statiImg.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));
        bussola.setImageTintList(ColorStateList.valueOf(MyColorClass.colorConstraint));

    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClick() {
        add.setOnClickListener(view -> {
            switch (Canvas_Crea_Superficie.mode) {
                case 0:
                    selSPigolo(DataSaved.bucketEdge);
                    DataSaved.puntiProgetto = new Coordinate[]{new Coordinate(coordSpigolo[0], coordSpigolo[1], coordSpigolo[2])};
                    new CustomToast(this, this.getResources().getString(R.string.punto_salvato)).show_added();

                    break;
                case 1:

                    switch (countPP) {
                        case 0:
                            selSPigolo(DataSaved.bucketEdge);
                            countPP++;
                            puntiAB[0] = new Point3D(coordSpigolo[0], coordSpigolo[1], coordSpigolo[2]);
                            new CustomToast(this, this.getResources().getString(R.string.punto_salvato)).show_added();
                            break;
                        case 1:
                            selSPigolo(DataSaved.bucketEdge);
                            reloadAB(leftDIST, leftSLOPE, rightDIST, rightSLOPE);
                            new CustomToast(this, this.getResources().getString(R.string.punto_salvato)).show_added();
                            break;

                    }

                    break;

                case 2:
                    if (coordinateP == null) {
                        coordinateP = new ArrayList<>();
                    }
                    selSPigolo(DataSaved.bucketEdge);
                    if (DataSaved.gpsOk) {
                        if (coordinateP.size() > 0) {
                            z = coordinateP.get(0)[2];
                        }

                        if (coordinateP.size() > 1) {
                            coordinateP.add(new double[]{coordSpigolo[0], coordSpigolo[1], z});
                        } else {
                            coordinateP.add(coordSpigolo);
                        }
                        pointIndex++;
                        new CustomToast(this, this.getResources().getString(R.string.punto_salvato)).show_added();
                    } else {
                        new CustomToast(this, "GPS ERROR").show_error();
                    }
                    break;

                case 3:
                    //TODO trincea
                    selSPigolo(DataSaved.bucketEdge);
                    if (DataSaved.gpsOk) {
                        countPunti++;
                        aggiungiPunto(new Point3D("P" + countPunti, coordSpigolo[0], coordSpigolo[1], coordSpigolo[2], " "));
                        new CustomToast(this, this.getResources().getString(R.string.punto_salvato)).show_added();
                    } else {
                        new CustomToast(this, "GNSS ERROR").show_alert();
                    }
                    break;

                case 4:
                    selSPigolo(DataSaved.bucketEdge);
                    if (DataSaved.gpsOk) {
                        countPunti++;
                        aggiungiPunto(new Point3D("P" + countPunti, coordSpigolo[0], coordSpigolo[1], coordSpigolo[2], " "));
                        new CustomToast(this, this.getResources().getString(R.string.punto_salvato)).show_added();
                    } else {
                        new CustomToast(this, "GNSS ERROR").show_alert();
                    }
                    break;
            }
        });
        remove.setOnClickListener(view -> {
            askConfirm(Canvas_Crea_Superficie.mode);

        });
        edit.setOnClickListener(view -> {
            switch (Canvas_Crea_Superficie.mode) {
                case 0:
                    if (DataSaved.puntiProgetto != null) {
                        if (!dialogEditaPunti.dialog.isShowing()) {
                            dialogEditaPunti.show(0, new double[]{DataSaved.puntiProgetto[0].x, DataSaved.puntiProgetto[0].y, DataSaved.puntiProgetto[0].z});
                        }
                    } else {
                        new CustomToast(this, "Pick a Point First").show_alert();
                    }
                    break;
                case 1:
                    if (puntiAB[1] != null) {
                        if (!dialogParametriAb.dialog.isShowing()) {
                            dialogParametriAb.show();
                        }
                    }
                    break;
                case 2:
                    if (coordinateP != null) {
                        if (coordinateP.size() > 0) {
                            if (!dialogEditZeta.dialog.isShowing()) {
                                dialogEditZeta.show();
                            }
                        } else {
                            new CustomToast(this, "Pick at least 1 point").show_alert();
                        }
                    } else {
                        new CustomToast(this, "Pick at least 1 point").show_alert();
                    }
                    break;
                case 3:
                    //TODO
                    if (point3DS != null && point3DS.length > 1) {
                        new Dialog_Trench(this, point3DS).show();
                    } else {
                        new CustomToast(this, "Pick at least 2 points").show_alert();
                    }
                    break;

                case 4:

                    new Dialog_Edita_Punti3D(this, point3DS).show();
                    break;
            }
        });
        save.setOnClickListener(view -> {
            switch (Canvas_Crea_Superficie.mode) {
                case 0:
                    if (DataSaved.puntiProgetto != null) {
                        if (!saveFileDialog.dialog.isShowing()) {
                            if (Dialog_Edita_Punti.d_side == 0) {
                                Dialog_Edita_Punti.d_side = 20;
                            }
                            saveFileDialog.show(0, new double[]{Dialog_Edita_Punti.d_side}, addPRJ, percorso);
                        }
                    } else {
                        new CustomToast(this, "Pick a Point First").show_alert();
                    }
                    break;
                case 1:
                    if (puntiAB[3] != null) {
                        if (!saveFileDialog.dialog.isShowing()) {
                            saveFileDialog.show(1, new double[]{0}, addPRJ, percorso);
                        }
                    } else {
                        new CustomToast(this, "Complete the AB Surface").show_alert();
                    }

                    break;

                case 2:
                    if (coordinateP != null) {
                        if (coordinateP.size() >= 3) {
                            if (!saveFileDialog.dialog.isShowing()) {
                                saveFileDialog.show(2, new double[]{0}, addPRJ, percorso);
                            }
                        } else {
                            new CustomToast(this, "Get 3 Pts at least").show_alert();
                        }
                    }

                    break;
                case 3:
                    //TODO
                    if (point3DS != null) {
                        if (point3DS.length >= 2) {
                            if (!saveFileDialog.dialog.isShowing()) {
                                saveFileDialog.show(3, new double[]{0}, addPRJ, percorso);
                            }
                        } else {
                            new CustomToast(this, "Get 2 Pts at least").show_alert();
                        }

                    }
                    break;

                case 4:

                    if (point3DS != null) {
                        if (point3DS.length >= 3) {
                            if (!saveFileDialog.dialog.isShowing()) {
                                saveFileDialog.show(4, new double[]{0}, addPRJ, percorso);
                            }
                        } else {
                            new CustomToast(this, "Get 3 Pts at least").show_alert();
                        }

                    }
                    break;
            }

        });
        exit.setOnClickListener(view -> {
            MyData.push("scaleFactor3D", String.valueOf(DataSaved.scale_Factor3D));
            switch (Canvas_Crea_Superficie.mode) {
                case 0:
                    if (DataSaved.puntiProgetto != null) {
                        if (!headingDialog.dialog.isShowing()) {
                            headingDialog.show();
                        }
                    } else {
                        setAct();
                    }
                    break;
                case 1:
                    if (puntiAB[0] != null) {
                        if (!headingDialog.dialog.isShowing()) {
                            headingDialog.show();
                        }
                    } else {
                        setAct();
                    }
                    break;

                case 2:
                    if (coordinateP != null) {
                        if (!headingDialog.dialog.isShowing()) {
                            headingDialog.show();
                        }
                    } else {
                        setAct();
                    }

                    break;
                case 3:
                case 4:

                    if (point3DS != null) {
                        if (point3DS.length > 0) {
                            if (!headingDialog.dialog.isShowing()) {
                                headingDialog.show();
                            }
                        } else {
                            setAct();
                        }
                    } else {
                        setAct();
                    }
                    break;
            }
        });
        gnssSt.setOnClickListener(view -> {
            if (!dialogGnssCoordinates.alertDialog.isShowing()) {
                dialogGnssCoordinates.show();
            }
        });
        units.setOnClickListener(view -> {
            if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                dialogUnitOfMeasure.show();
            }
        });
        bucketEdgeL.setOnClickListener(v -> {
            spigolo = -1;
            selSPigolo(spigolo);
        });
        bucketEdgeC.setOnClickListener(v -> {
            spigolo = 0;
            selSPigolo(spigolo);
        });
        bucketEdgeR.setOnClickListener(v -> {
            spigolo = 1;
            selSPigolo(spigolo);
        });
        zoomM.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                zommaIn = true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                zommaIn = false;
            }
            return true;
        });
        zoomC.setOnTouchListener((view, motionEvent) -> {
            zommaOut = false;
            zommaIn = false;
            Canvas_Crea_Superficie.offsetX = 0;
            Canvas_Crea_Superficie.offsetY = 150;
            return true;
        });
        zoomP.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                zommaOut = true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                zommaOut = false;
            }
            return true;
        });
        colori.setOnClickListener(view -> {
            if (!dialogColors.dialog.isShowing()) {
                dialogColors.show();
            }
        });


    }


    private void selSPigolo(int sp) {
        DataSaved.bucketEdge = sp;
        switch (sp) {
            case -1:
                coordSpigolo = ExcavatorLib.bucketLeftCoord;
                bucketEdgeL.setBackgroundColor(getColor(R.color.yellow));
                bucketEdgeC.setBackgroundColor(getColor(R.color.light_gray));
                bucketEdgeR.setBackgroundColor(getColor(R.color.light_gray));
                break;
            case 0:
                coordSpigolo = ExcavatorLib.bucketCoord;
                bucketEdgeL.setBackgroundColor(getColor(R.color.light_gray));
                bucketEdgeC.setBackgroundColor(getColor(R.color.yellow));
                bucketEdgeR.setBackgroundColor(getColor(R.color.light_gray));
                break;
            case 1:
                coordSpigolo = ExcavatorLib.bucketRightCoord;
                bucketEdgeL.setBackgroundColor(getColor(R.color.light_gray));
                bucketEdgeC.setBackgroundColor(getColor(R.color.light_gray));
                bucketEdgeR.setBackgroundColor(getColor(R.color.yellow));
                break;
        }
    }

    public void updateUI() {
        //do your stuff

        switch (DataSaved.bucketEdge) {
            case -1:
                mostraCoor.setText("LEFT:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[0])) + "\n" +
                        "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[1])) + "\n" +
                        "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketLeftCoord[2])));
                break;
            case 0:
                mostraCoor.setText("CENTER:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[0])) + "\n" +
                        "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[1])) + "\n" +
                        "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketCoord[2])));
                break;
            case 1:
                mostraCoor.setText("RIGHT:\n" + "E: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[0])) + "\n" +
                        "N: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[1])) + "\n" +
                        "Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(ExcavatorLib.bucketRightCoord[2])));
                break;
        }

        if (Canvas_Crea_Superficie.mode == 1) {
            if (puntiAB[1] != null) {
                remove.setVisibility(View.VISIBLE);
            } else {
                remove.setVisibility(View.INVISIBLE);
            }
        }
        if (Canvas_Crea_Superficie.mode == 2) {
            if (coordinateP != null) {
                if (coordinateP.size() > 0) {
                    remove.setVisibility(View.VISIBLE);
                } else {
                    remove.setVisibility(View.INVISIBLE);
                }

            }
        }
        if (Canvas_Crea_Superficie.mode == 3) {
            if (point3DS != null) {
                if (point3DS.length > 0) {
                    remove.setVisibility(View.VISIBLE);
                } else {
                    remove.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (Canvas_Crea_Superficie.mode == 4) {
            if (point3DS != null) {
                if (point3DS.length > 0) {
                    remove.setVisibility(View.VISIBLE);
                } else {
                    remove.setVisibility(View.INVISIBLE);
                }

            }
        }

        rotBus = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rotBus = rotBus % 360;
        bussola.setRotation(rotBus);
        if (DataSaved.gpsOk) {
            alert.setVisibility(View.INVISIBLE);
        } else {
            alert.setVisibility(View.VISIBLE);
        }
        if (zommaIn) {
            zommaOut = false;
            if (DataSaved.scale_Factor3D > 0.05f) {
                DataSaved.scale_Factor3D -= 0.025f;
            }


        }
        if (zommaOut) {
            zommaIn = false;
            if (DataSaved.scale_Factor3D < 4.5f) {
                DataSaved.scale_Factor3D += 0.025f;
            }
        }
        crea_Superficie.invalidate();
    }

    private void reloadAB(double leftDist, double leftSlope, double rightDist, double rightSlope) {
        puntiAB[1] = new Point3D(coordSpigolo[0], coordSpigolo[1], coordSpigolo[2]);
        double hdtAB = My_LocationCalc.calcBearingXY(puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[1].getX(), puntiAB[1].getY());
        double[] tmpC = Exca_Quaternion.endPoint(new double[]{puntiAB[1].getX(), puntiAB[1].getY(), puntiAB[1].getZ()}, rightSlope, 0, rightDist, hdtAB + 90);
        puntiAB[2] = new Point3D(tmpC[0], tmpC[1], tmpC[2]);
        double[] tmpD = Exca_Quaternion.endPoint(new double[]{puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[0].getZ()}, rightSlope, 0, rightDist, hdtAB + 90);
        puntiAB[3] = new Point3D(tmpD[0], tmpD[1], tmpD[2]);

        double[] tmpE = Exca_Quaternion.endPoint(new double[]{puntiAB[1].getX(), puntiAB[1].getY(), puntiAB[1].getZ()}, leftSlope, 0, leftDist, hdtAB - 90);
        puntiAB[4] = new Point3D(tmpE[0], tmpE[1], tmpE[2]);

        double[] tmpF = Exca_Quaternion.endPoint(new double[]{puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[0].getZ()}, leftSlope, 0, leftDist, hdtAB - 90);
        puntiAB[5] = new Point3D(tmpF[0], tmpF[1], tmpF[2]);
        getDistAndSlope();
    }

    public void getDistAndSlope() {
        distAB = new DistToPoint(puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[0].getZ(), puntiAB[1].getX(), puntiAB[1].getY(), puntiAB[1].getZ()).getDist_to_point();
        slopeAB = getSlope(new double[]{puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[0].getZ()}, new double[]{puntiAB[1].getX(), puntiAB[1].getY(), puntiAB[1].getZ()});
    }

    private double getSlope(double[] A, double[] B) {

        double base = new DistToPoint(A[0], A[1], 0, B[0], B[1], 0).getDist_to_point();//base

        if (Math.abs(base) > 0.1) {
            double sideC = new DistToPoint(A[0], A[1], A[2], B[0], B[1], B[2]).getDist_to_point();//lato lungo
            double height = Math.sqrt(Math.abs(sideC * sideC - base * base));//altezza
            double dist = (base * base) + (sideC * sideC) - (height * height); // Corrected formula
            double angle = Math.toDegrees(Math.acos(dist / (2 * base * sideC)));
            int sign = A[2] < B[2] ? 1 : -1;

            return Double.isNaN(angle) ? 0 : (angle * sign);
        }
        return 0;
    }

    public void removeLast() {
        try {
            coordinateP.remove(coordinateP.size() - 1);
            new CustomToast(MyApp.visibleActivity, MyApp.visibleActivity.getResources().getString(R.string.punto_rimosso)).show_removed();
        } catch (Exception e) {
            try {
                coordinateP.remove(pointIndex - 2);
            } catch (Exception ex) {
                new CustomToast(this, "No Points").show_long();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyData.push("scaleFactor3D", String.valueOf(DataSaved.scale_Factor3D));
        countPunti = 0;
        indexSel = 0;
        switch (Canvas_Crea_Superficie.mode) {
            case 0:
                DataSaved.puntiProgetto = null;
                break;
            case 1:
                puntiAB = null;
                break;

            case 2:

                if (coordinateP != null) {
                    coordinateP = null;
                }
                pointIndex = 0;
                break;
            case 3:
                point3DS = null;
                Dialog_Trench.leftS_d = 0;
                Dialog_Trench.rightS_d = 0;
                break;
            case 4:
                point3DS = null;
                break;


        }

    }


    public static void aggiungiPunto(Point3D nuovoPunto) {
        // Creiamo un nuovo array con una posizione in più
        Point3D[] nuovoArray = new Point3D[point3DS.length + 1];

        // Copiamo gli elementi esistenti nel nuovo array
        System.arraycopy(point3DS, 0, nuovoArray, 0, point3DS.length);

        // Aggiungiamo il nuovo punto
        nuovoArray[point3DS.length] = nuovoPunto;

        // Aggiorniamo il riferimento all'array
        point3DS = nuovoArray;
    }

    public static void rimuoviUltimoPunto() {
        if (point3DS.length == 0) return; // Evita errori se l'array è vuoto

        // Creiamo un nuovo array con una posizione in meno
        Point3D[] nuovoArray = new Point3D[point3DS.length - 1];

        // Copiamo gli elementi tranne l'ultimo
        System.arraycopy(point3DS, 0, nuovoArray, 0, nuovoArray.length);

        // Aggiorniamo il riferimento all'array
        point3DS = nuovoArray;
        new CustomToast(MyApp.visibleActivity, MyApp.visibleActivity.getResources().getString(R.string.punto_rimosso)).show_removed();

        countPunti--;
    }

    private void askConfirm(int mode) {
        // Crea un nuovo AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Crea_Superficie.this);
        builder.setTitle("REMOVE LAST POINT");
        builder.setMessage("Do You Want to Remove Last Point ?");

        // Aggiungi il pulsante "Sì"
        builder.setPositiveButton("YES", (dialog, which) -> {

            switch (mode) {
                case 0:
                    DataSaved.puntiProgetto = null;
                    new CustomToast(this, this.getResources().getString(R.string.punto_rimosso)).show_removed();
                    break;
                case 1:
                    if (puntiAB != null) {
                        if (countPP > 0) {
                            puntiAB[1] = null;
                            countPP = 1;
                            new CustomToast(this, this.getResources().getString(R.string.punto_rimosso)).show_removed();
                        }
                    }


                    break;

                case 2:
                    removeLast();
                    break;

                case 3:
                    //TODO
                    rimuoviUltimoPunto();
                    break;

                case 4:
                    rimuoviUltimoPunto();//rimuovi ultimo punto da superficie TRIANGLES
                    break;
            }

        });

        // Aggiungi il pulsante "No"
        builder.setNegativeButton("NO", (dialog, which) -> {

        });

        // Mostra il dialog
        builder.show();
    }

    private void setAct() {
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