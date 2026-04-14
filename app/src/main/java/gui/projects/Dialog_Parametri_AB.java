package gui.projects;

import static gui.projects.Activity_Crea_Superficie.distAB;
import static gui.projects.Activity_Crea_Superficie.leftDIST;
import static gui.projects.Activity_Crea_Superficie.leftSLOPE;
import static gui.projects.Activity_Crea_Superficie.puntiAB;
import static gui.projects.Activity_Crea_Superficie.rightDIST;
import static gui.projects.Activity_Crea_Superficie.rightSLOPE;
import static gui.projects.Activity_Crea_Superficie.slopeAB;
import static utils.MyTypes.DOZER;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import dxf.Point3D;
import gui.dialogs_and_toast.CustomNumberDialog;
import gui.dialogs_and_toast.CustomNumberDialogFtIn;
import gui.dialogs_and_toast.CustomToast;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import utils.DistToPoint;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class Dialog_Parametri_AB {
    ImageView imgAB, imgL, imgR;
    TextView txt_1, txt_2, txt_3, txt_4, txt_5, txt_6, txt_7, txt_8, txt_9;
    double[] nuovoB = new double[]{0, 0, 0};
    double hdtAB;
    Activity activity;
    public Dialog dialog;
    CustomNumberDialog customNumberDialog;
    CustomNumberDialogFtIn customNumberDialogFtIn;
    public Button exit, cancel, reload;
    EditText estA, nordA, zetaA;
    EditText estB, nordB, zetaB;
    EditText estC, nordC, zetaC;
    EditText estD, nordD, zetaD;
    EditText estE, nordE, zetaE;
    EditText estF, nordF, zetaF;
    EditText et_distAB, et_slopeAB, et_leftW, et_leftS, et_rightW, et_rightS;
    ImageView collectCL, collectCC, collectCR, collectDL, collectDC, collectDR, collectEL, collectEC, collectER, collectFL, collectFC, collectFR, reload_1, reload_2, reload_3, reload_4, reload_5;
    static int functionIndex;
    int v = -256;
    int uom;

    public Dialog_Parametri_AB(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        uom = MyData.get_Int("Unit_Of_Measure");
        customNumberDialog = new CustomNumberDialog(activity, -256);
        customNumberDialogFtIn = new CustomNumberDialogFtIn(activity, -256);
    }

    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_parametri_ab);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        init();
        onClick();


    }

    private void findView() {
        txt_1 = dialog.findViewById(R.id.txt_1);
        txt_2 = dialog.findViewById(R.id.txt_2);
        txt_3 = dialog.findViewById(R.id.txt_3);
        txt_4 = dialog.findViewById(R.id.txt_4);
        txt_5 = dialog.findViewById(R.id.txt_5);
        txt_6 = dialog.findViewById(R.id.txt_6);
        txt_7 = dialog.findViewById(R.id.txt_7);
        txt_8 = dialog.findViewById(R.id.txt_8);
        txt_9 = dialog.findViewById(R.id.txt_9);
        reload = dialog.findViewById(R.id.reload);
        exit = dialog.findViewById(R.id.ok);
        estA = dialog.findViewById(R.id.et_estA);
        nordA = dialog.findViewById(R.id.et_nordA);
        zetaA = dialog.findViewById(R.id.et_zetaA);
        estB = dialog.findViewById(R.id.et_estB);
        nordB = dialog.findViewById(R.id.et_nordB);
        zetaB = dialog.findViewById(R.id.et_zetaB);
        estC = dialog.findViewById(R.id.et_estC);
        nordC = dialog.findViewById(R.id.et_nordC);
        zetaC = dialog.findViewById(R.id.et_zetaC);
        estD = dialog.findViewById(R.id.et_estD);
        nordD = dialog.findViewById(R.id.et_nordD);
        zetaD = dialog.findViewById(R.id.et_zetaD);
        estE = dialog.findViewById(R.id.et_estE);
        nordE = dialog.findViewById(R.id.et_nordE);
        zetaE = dialog.findViewById(R.id.et_zetaE);
        estF = dialog.findViewById(R.id.et_estF);
        nordF = dialog.findViewById(R.id.et_nordF);
        zetaF = dialog.findViewById(R.id.et_zetaF);
        et_distAB = dialog.findViewById(R.id.et_distAB);
        et_slopeAB = dialog.findViewById(R.id.et_slopeAB);
        et_leftW = dialog.findViewById(R.id.et_leftW);
        et_leftS = dialog.findViewById(R.id.et_leftS);
        et_rightW = dialog.findViewById(R.id.et_rightW);
        et_rightS = dialog.findViewById(R.id.et_rightS);
        cancel = dialog.findViewById(R.id.cancel);
        collectCL = dialog.findViewById(R.id.collectCL);
        collectCC = dialog.findViewById(R.id.collectCC);
        collectCR = dialog.findViewById(R.id.collectCR);
        collectDL = dialog.findViewById(R.id.collectDL);
        collectDC = dialog.findViewById(R.id.collectDC);
        collectDR = dialog.findViewById(R.id.collectDR);
        collectEL = dialog.findViewById(R.id.collectEL);
        collectEC = dialog.findViewById(R.id.collectEC);
        collectER = dialog.findViewById(R.id.collectER);
        collectFL = dialog.findViewById(R.id.collectFL);
        collectFC = dialog.findViewById(R.id.collectFC);
        collectFR = dialog.findViewById(R.id.collectFR);
        reload_1 = dialog.findViewById(R.id.reload_1);
        reload_2 = dialog.findViewById(R.id.reload_2);
        reload_3 = dialog.findViewById(R.id.reload_3);
        reload_4 = dialog.findViewById(R.id.reload_4);
        reload_5 = dialog.findViewById(R.id.reload_5);
        hdtAB = My_LocationCalc.calcBearingXY(puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[1].getX(), puntiAB[1].getY());
        txt_1.setText("AB PARAM " + Utils.getMetriSimbol() + "  (" + Utils.getGradiSimbol() + ")");
        txt_2.setText("LEFT PARAM " + Utils.getMetriSimbol() + "  (" + Utils.getGradiSimbol() + ")");
        txt_3.setText("RIGHT PARAM " + Utils.getMetriSimbol() + "  (" + Utils.getGradiSimbol() + ")");
        txt_4.setText("POINT A " + Utils.getMetriSimbol());
        txt_5.setText("POINT B " + Utils.getMetriSimbol());
        txt_6.setText("POINT C " + Utils.getMetriSimbol());
        txt_7.setText("POINT D " + Utils.getMetriSimbol());
        txt_8.setText("POINT E " + Utils.getMetriSimbol());
        txt_9.setText("POINT F " + Utils.getMetriSimbol());
        if (DataSaved.isWL < DOZER) {
            collectCL.setImageResource(R.drawable.benna_misura_sinistra);
            collectDL.setImageResource(R.drawable.benna_misura_sinistra);
            collectEL.setImageResource(R.drawable.benna_misura_sinistra);
            collectFL.setImageResource(R.drawable.benna_misura_sinistra);

            collectCC.setImageResource(R.drawable.benna_misura_cnt);
            collectDC.setImageResource(R.drawable.benna_misura_cnt);
            collectEC.setImageResource(R.drawable.benna_misura_cnt);
            collectFC.setImageResource(R.drawable.benna_misura_cnt);

            collectCR.setImageResource(R.drawable.benna_misura_destra);
            collectDR.setImageResource(R.drawable.benna_misura_destra);
            collectER.setImageResource(R.drawable.benna_misura_destra);
            collectFR.setImageResource(R.drawable.benna_misura_destra);

        } else {
            collectCL.setImageResource(R.drawable.lama_misura_sinistra);
            collectDL.setImageResource(R.drawable.lama_misura_sinistra);
            collectEL.setImageResource(R.drawable.lama_misura_sinistra);
            collectFL.setImageResource(R.drawable.lama_misura_sinistra);

            collectCC.setImageResource(R.drawable.lama_misura_cnt);
            collectDC.setImageResource(R.drawable.lama_misura_cnt);
            collectEC.setImageResource(R.drawable.lama_misura_cnt);
            collectFC.setImageResource(R.drawable.lama_misura_cnt);

            collectCR.setImageResource(R.drawable.lama_misura_destra);
            collectDR.setImageResource(R.drawable.lama_misura_destra);
            collectER.setImageResource(R.drawable.lama_misura_destra);
            collectFR.setImageResource(R.drawable.lama_misura_destra);
        }
        imgAB = dialog.findViewById(R.id.imgAB);
        imgL = dialog.findViewById(R.id.imgLeft);
        imgR = dialog.findViewById(R.id.imgRight);

    }

    private void init() {
        functionIndex = 0;
        estA.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[0].getX())));
        nordA.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[0].getY())));
        zetaA.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[0].getZ())));
        estB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[1].getX())));
        nordB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[1].getY())));
        zetaB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[1].getZ())));
        estC.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[2].getX())));
        nordC.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[2].getY())));
        zetaC.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[2].getZ())));
        estD.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[3].getX())));
        nordD.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[3].getY())));
        zetaD.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[3].getZ())));
        estE.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[4].getX())));
        nordE.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[4].getY())));
        zetaE.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[4].getZ())));
        estF.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[5].getX())));
        nordF.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[5].getY())));
        zetaF.setText(Utils.readUnitOfMeasureLITE(String.valueOf(puntiAB[5].getZ())));
        et_distAB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(Activity_Crea_Superficie.distAB)));
        et_slopeAB.setText(Utils.readAngolo(String.valueOf(Activity_Crea_Superficie.slopeAB)));
        et_leftW.setText(Utils.readUnitOfMeasureLITE(String.valueOf(Activity_Crea_Superficie.leftDIST)));
        et_leftS.setText(Utils.readAngolo(String.valueOf(Activity_Crea_Superficie.leftSLOPE)));
        et_rightW.setText(Utils.readUnitOfMeasureLITE(String.valueOf(Activity_Crea_Superficie.rightDIST)));
        et_rightS.setText(Utils.readAngolo(String.valueOf(rightSLOPE)));
        imgAB.setRotation(45 + ((float) -slopeAB));
        imgL.setRotation((float) (45 + leftSLOPE));
        imgR.setRotation(45 + ((float) -rightSLOPE));

    }

    private void update() {
        if (puntiAB != null) {
            puntiAB[0].setX(Double.parseDouble(Utils.writeMetri(estA.getText().toString())));
            puntiAB[0].setY(Double.parseDouble(Utils.writeMetri(nordA.getText().toString())));
            puntiAB[0].setZ(Double.parseDouble(Utils.writeMetri(zetaA.getText().toString())));
            puntiAB[1].setX(Double.parseDouble(Utils.writeMetri(estB.getText().toString())));
            puntiAB[1].setY(Double.parseDouble(Utils.writeMetri(nordB.getText().toString())));
            puntiAB[1].setZ(Double.parseDouble(Utils.writeMetri(zetaB.getText().toString())));
            puntiAB[2].setX(Double.parseDouble(Utils.writeMetri(estC.getText().toString())));
            puntiAB[2].setY(Double.parseDouble(Utils.writeMetri(nordC.getText().toString())));
            puntiAB[2].setZ(Double.parseDouble(Utils.writeMetri(zetaC.getText().toString())));
            puntiAB[3].setX(Double.parseDouble(Utils.writeMetri(estD.getText().toString())));
            puntiAB[3].setY(Double.parseDouble(Utils.writeMetri(nordD.getText().toString())));
            puntiAB[3].setZ(Double.parseDouble(Utils.writeMetri(zetaD.getText().toString())));
            puntiAB[4].setX(Double.parseDouble(Utils.writeMetri(estE.getText().toString())));
            puntiAB[4].setY(Double.parseDouble(Utils.writeMetri(nordE.getText().toString())));
            puntiAB[4].setZ(Double.parseDouble(Utils.writeMetri(zetaE.getText().toString())));
            puntiAB[5].setX(Double.parseDouble(Utils.writeMetri(estF.getText().toString())));
            puntiAB[5].setY(Double.parseDouble(Utils.writeMetri(nordF.getText().toString())));
            puntiAB[5].setZ(Double.parseDouble(Utils.writeMetri(zetaF.getText().toString())));
        }
    }

    private void onClick() {
        exit.setOnClickListener(view -> {

            update();
            dialog.dismiss();
        });
        estA.setOnClickListener(view -> {

        });
        nordA.setOnClickListener(view -> {

        });
        zetaA.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(zetaA);
                    functionIndex = 1;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(zetaA);
                    functionIndex = 1;
                }
            }
        });

        estB.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(estB);
                    functionIndex = 2;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(estB);
                    functionIndex = 2;
                }
            }
        });
        nordB.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(nordB);
                    functionIndex = 2;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(nordB);
                    functionIndex = 2;
                }
            }
        });
        zetaB.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(zetaB);
                    functionIndex = 2;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(zetaB);
                    functionIndex = 2;
                }
            }
        });

        estC.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(estC);
                    functionIndex = 3;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(estC);
                    functionIndex = 3;
                }
            }
        });
        nordC.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(nordC);
                    functionIndex = 3;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(nordC);
                    functionIndex = 3;
                }
            }
        });
        zetaC.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(zetaC);
                    functionIndex = 3;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(zetaC);
                    functionIndex = 3;
                }
            }
        });

        estD.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(estB);
                    functionIndex = 4;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(estD);
                    functionIndex = 4;
                }
            }
        });
        nordD.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(nordD);
                    functionIndex = 4;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(nordD);
                    functionIndex = 4;
                }
            }
        });
        zetaD.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(zetaD);
                    functionIndex = 4;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(zetaD);
                    functionIndex = 4;
                }
            }
        });
        estE.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(estE);
                    functionIndex = 5;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(estE);
                    functionIndex = 5;
                }
            }
        });
        nordE.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(nordE);
                    functionIndex = 5;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(nordE);
                    functionIndex = 5;
                }
            }
        });
        zetaE.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(zetaE);
                    functionIndex = 5;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(zetaE);
                    functionIndex = 5;
                }
            }
        });
        estF.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(estF);
                    functionIndex = 6;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(estF);
                    functionIndex = 6;
                }
            }
        });
        nordF.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(nordF);
                    functionIndex = 6;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(nordF);
                    functionIndex = 6;
                }
            }
        });
        zetaF.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(zetaF);
                    functionIndex = 6;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(zetaF);
                    functionIndex = 6;
                }
            }
        });

        et_distAB.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(et_distAB);
                    functionIndex = 7;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(et_distAB);
                    functionIndex = 7;
                }
            }
        });
        et_slopeAB.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(et_slopeAB);
                    functionIndex = 8;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(et_slopeAB);
                    functionIndex = 8;
                }
            }
        });
        et_leftW.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(et_leftW);
                    functionIndex = 9;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(et_leftW);
                    functionIndex = 9;
                }
            }
        });
        et_leftS.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(et_leftS);
                    functionIndex = 10;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(et_leftS);
                    functionIndex = 10;
                }
            }
        });
        et_rightW.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(et_rightW);
                    functionIndex = 11;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(et_rightW);
                    functionIndex = 11;
                }
            }
        });
        et_rightS.setOnClickListener(view -> {
            if (uom == 0 || uom == 1 || uom == 2 || uom == 3 || uom == 6 || uom == 7) {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(et_rightS);
                    functionIndex = 12;
                }
            } else {
                if (!customNumberDialogFtIn.dialog.isShowing()) {
                    customNumberDialogFtIn.show(et_rightS);
                    functionIndex = 12;
                }
            }
        });

        cancel.setOnClickListener(view -> {
            functionIndex = 0;

            dialog.dismiss();
        });
        collectCL.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estC.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[0])));
                nordC.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[1])));
                zetaC.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectCC.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estC.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[0])));
                nordC.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[1])));
                zetaC.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectCR.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estC.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[0])));
                nordC.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[1])));
                zetaC.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectDL.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estD.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[0])));
                nordD.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[1])));
                zetaD.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectDC.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estD.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[0])));
                nordD.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[1])));
                zetaD.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectDR.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estD.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[0])));
                nordD.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[1])));
                zetaD.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectEL.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estE.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[0])));
                nordE.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[1])));
                zetaE.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectEC.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estE.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[0])));
                nordE.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[1])));
                zetaE.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectER.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;
                estE.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[0])));
                nordE.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[1])));
                zetaE.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectFL.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estF.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[0])));
                nordF.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[1])));
                zetaF.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketLeftCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectFC.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estF.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[0])));
                nordF.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[1])));
                zetaF.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        collectFR.setOnLongClickListener(view -> {
            if (DataSaved.gpsOk) {
                functionIndex = 0;

                estF.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[0])));
                nordF.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[1])));
                zetaF.setText(Utils.writeMetri(String.valueOf(ExcavatorLib.bucketRightCoord[2])));
            } else {
                new CustomToast(activity, "GPS ERROR").show_error();
            }
            return false;
        });
        reload.setOnClickListener(view -> {
            updateTasto();
        });
        reload_1.setOnClickListener(view -> {
            updateTasto();
        });
        reload_2.setOnClickListener(view -> {
            updateTasto();
        });
        reload_3.setOnClickListener(view -> {
            updateTasto();
        });
        reload_4.setOnClickListener(view -> {
            updateTasto();
        });
        reload_5.setOnClickListener(view -> {
            updateTasto();
        });


    }

    public void updateTasto() {
        Log.d("Flaggg", "Chiamato...");
        switch (functionIndex) {
            case 0:
                //do nothing
                break;
            case 1:
                puntiAB[0].setZ(Double.parseDouble(Utils.readUnitOfMeasureLITE(zetaA.getText().toString())));
                reloadAB();
                init();
                break;
            case 2:
                puntiAB[1].setZ(Double.parseDouble(Utils.readUnitOfMeasureLITE(zetaB.getText().toString())));
                reloadAB();
                init();
                break;

            case 7:
                nuovoB = Exca_Quaternion.endPoint(new double[]{puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[0].getZ()}, slopeAB, 0, Double.parseDouble(Utils.writeMetri(et_distAB.getText().toString())), hdtAB);
                puntiAB[1].setX(nuovoB[0]);
                puntiAB[1].setY(nuovoB[1]);
                puntiAB[1].setZ(nuovoB[2]);
                estB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(nuovoB[0])));
                nordB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(nuovoB[1])));
                zetaB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(nuovoB[2])));
                reloadAB();
                init();
                break;
            case 8:
                nuovoB = Exca_Quaternion.endPoint(new double[]{puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[0].getZ()}, Double.parseDouble(Utils.writeGradi(et_slopeAB.getText().toString())), 0, distAB, hdtAB);
                puntiAB[1].setX(nuovoB[0]);
                puntiAB[1].setY(nuovoB[1]);
                puntiAB[1].setZ(nuovoB[2]);
                estB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(nuovoB[0])));
                nordB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(nuovoB[1])));
                zetaB.setText(Utils.readUnitOfMeasureLITE(String.valueOf(nuovoB[2])));
                reloadAB();
                init();
                break;
            case 9:
                leftDIST = Double.parseDouble(Utils.writeMetri(et_leftW.getText().toString()));
                reloadAB();
                init();
                break;
            case 10:
                leftSLOPE = Double.parseDouble(Utils.writeGradi(et_leftS.getText().toString()));
                reloadAB();
                init();
                break;
            case 11:
                rightDIST = Double.parseDouble(Utils.writeMetri(et_rightW.getText().toString()));
                reloadAB();
                init();
                break;
            case 12:

                rightSLOPE = Double.parseDouble(Utils.writeGradi(et_rightS.getText().toString()));
                reloadAB();
                init();
                break;
        }
    }

    private void reloadAB() {
        double hdtAB = My_LocationCalc.calcBearingXY(puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[1].getX(), puntiAB[1].getY());
        double[] tmpC = Exca_Quaternion.endPoint(new double[]{puntiAB[1].getX(), puntiAB[1].getY(), puntiAB[1].getZ()}, rightSLOPE, 0, rightDIST, hdtAB + 90);
        puntiAB[2] = new Point3D(tmpC[0], tmpC[1], tmpC[2]);
        double[] tmpD = Exca_Quaternion.endPoint(new double[]{puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[0].getZ()}, rightSLOPE, 0, rightDIST, hdtAB + 90);
        puntiAB[3] = new Point3D(tmpD[0], tmpD[1], tmpD[2]);

        double[] tmpE = Exca_Quaternion.endPoint(new double[]{puntiAB[1].getX(), puntiAB[1].getY(), puntiAB[1].getZ()}, leftSLOPE, 0, leftDIST, hdtAB - 90);
        puntiAB[4] = new Point3D(tmpE[0], tmpE[1], tmpE[2]);

        double[] tmpF = Exca_Quaternion.endPoint(new double[]{puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[0].getZ()}, leftSLOPE, 0, leftDIST, hdtAB - 90);
        puntiAB[5] = new Point3D(tmpF[0], tmpF[1], tmpF[2]);
        getDistAndSlope();
    }

    public void getDistAndSlope() {
        Activity_Crea_Superficie.distAB = new DistToPoint(puntiAB[0].getX(), puntiAB[0].getY(), puntiAB[0].getZ(), puntiAB[1].getX(), puntiAB[1].getY(), puntiAB[1].getZ()).getDist_to_point();
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


}
