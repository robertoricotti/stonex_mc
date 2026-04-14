package gui.dialogs_and_toast;

import static utils.Utils.isNumeric;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import utils.DistToPoint;
import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class DialogSlope {
    int neg;
    double xa, ya, za, xb, yb, zb;
    ConstraintLayout mainLayout;
    Activity activity;
    public Dialog dialog;
    Button canc, save, changeSlope, setA, setB, reverse;
    EditText value;
    TextView title, measure;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bdot, bcanc, bdel;
    static int changeType;
    double offsetH;
    double offsetR;
    static boolean cbool;
    int index;


    String strTmpX;
    String strTmpY;

    public DialogSlope(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    @SuppressLint("SetTextI18n")
    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_slope);
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
        title.setText(dialog.getContext().getResources().getString(R.string.slopeY));
        title.setTextColor(Color.WHITE);
        strTmpY = Utils.readAngolo(MyData.get_String("shortcutSlopeY_" + DataSaved.shortcutIndex));
        strTmpX = Utils.readAngolo(MyData.get_String("shortcutSlopeX_" + DataSaved.shortcutIndex));
        mainLayout.setBackgroundColor(MyColorClass.colorY_2D);
        changeType = 0;
        cbool = true;

        value.setText(strTmpY);
    }

    @SuppressLint("SetTextI18n")
    private void findView() {
        mainLayout = dialog.findViewById(R.id.mainLayout);
        changeSlope = dialog.findViewById(R.id.changeSlope);
        title = dialog.findViewById(R.id.title);
        canc = dialog.findViewById(R.id.exit);
        save = dialog.findViewById(R.id.save);
        value = dialog.findViewById(R.id.value);
        setA = dialog.findViewById(R.id.setA);
        setB = dialog.findViewById(R.id.setB);
        measure = dialog.findViewById(R.id.unitOfMeasure);
        reverse = dialog.findViewById(R.id.inverted);
        b1 = dialog.findViewById(R.id.b1);
        b2 = dialog.findViewById(R.id.b2);
        b3 = dialog.findViewById(R.id.b3);
        b4 = dialog.findViewById(R.id.b4);
        b5 = dialog.findViewById(R.id.b5);
        b6 = dialog.findViewById(R.id.b6);
        b7 = dialog.findViewById(R.id.b7);
        b8 = dialog.findViewById(R.id.b8);
        b9 = dialog.findViewById(R.id.b9);
        b0 = dialog.findViewById(R.id.b0);
        bdot = dialog.findViewById(R.id.bdot);
        bcanc = dialog.findViewById(R.id.bc);
        bdel = dialog.findViewById(R.id.bdel);
        measure.setText("(" + Utils.getGradiSimbol() + " )");
        index = DataSaved.portView;
        switch (index) {
            case 0:
                changeSlope.setVisibility(View.INVISIBLE);
                break;
            case 1:
            case 2:
                changeSlope.setVisibility(View.VISIBLE);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void onClick() {
        setA.setOnClickListener((View v) -> {
            xa = ExcavatorLib.bucketCoord[0];
            ya = ExcavatorLib.bucketCoord[1];
            za = ExcavatorLib.bucketCoord[2];
            setA.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.white));
            setA.setTextColor(Color.BLACK);
        });

        setB.setOnClickListener((View v) -> {
            if (setA.getBackgroundTintList() == ContextCompat.getColorStateList(activity, R.color.white)) {
                cbool = true;
                xb = ExcavatorLib.bucketCoord[0];
                yb = ExcavatorLib.bucketCoord[1];
                zb = ExcavatorLib.bucketCoord[2];
                setB.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.white));
                setB.setTextColor(Color.BLACK);

                double b = new DistToPoint(xa, ya, 0, xb, yb, 0).getDist_to_point();//base
                if (Math.abs(b) > 0.1) {
                    double cC = new DistToPoint(xa, ya, za, xb, yb, zb).getDist_to_point();//lato lungo
                    double a = Math.sqrt(Math.abs(cC * cC - b * b));//altezza
                    double dist = (b * b) + (cC * cC) - (a * a); // Corrected formula
                    double g = Math.toDegrees(Math.acos(dist / (2 * b * cC)));
                    neg = -1;
                    if (za > zb) {
                        neg = 1;
                    }


                    if (Double.isNaN(g)) {
                        value.setText(Utils.readAngolo("0"));
                    } else
                        value.setText(Utils.readAngolo(String.valueOf(g * neg)));
                } else {
                    value.setText(Utils.readAngolo("0"));
                }
            } else {
                new CustomToast(activity, "SET PREVIOUS POINT!").show();
            }


        });

        reverse.setOnClickListener((View v) -> {
            if (!value.getText().toString().equals("")) {
                if (!value.getText().toString().contains("-")) {
                    value.setText(value.getText().insert(0, "-"));
                } else {
                    value.setText(value.getText().toString().replace("-", ""));
                }
            }
        });

        save.setOnClickListener((View v) -> {

            String isNY = changeType == 0 ? value.getText().toString() : strTmpY;
            String isNX = changeType == 1 ? value.getText().toString() : strTmpX;
            if (isNumeric(isNY) && isNumeric(isNX)) {
                MyData.push("SLOPE_Y", Utils.writeGradi(isNY));
                MyData.push("shortcutSlopeY_" + DataSaved.shortcutIndex, Utils.writeGradi(isNY));
                DataSaved.slopeY = Double.parseDouble(Utils.writeGradi(isNY));

                MyData.push("SLOPE_X", Utils.writeGradi(isNX));
                MyData.push("shortcutSlopeX_" + DataSaved.shortcutIndex, Utils.writeGradi(isNX));
                DataSaved.slopeX = Double.parseDouble(Utils.writeGradi(isNX));

                dialog.cancel();
            } else {
                new CustomToast(activity, "Error INPUT!").show_error();

            }
        });

        canc.setOnClickListener((View v) -> {
            dialog.cancel();
        });

        changeSlope.setOnClickListener((View v) -> {
            resetColor();
            if (changeType == 0) {
                cbool = true;
                changeType = 1;
                strTmpY = value.getText().toString();
                changeSlope.setText("<<");
                title.setText(dialog.getContext().getResources().getString(R.string.slopeX));
                setA.setVisibility(View.INVISIBLE);
                setB.setVisibility(View.INVISIBLE);
                title.setTextColor(Color.WHITE);
                mainLayout.setBackgroundColor(MyColorClass.colorX_2D);
                value.setText(strTmpX);
            } else {
                cbool = true;
                changeType = 0;
                strTmpX = value.getText().toString();
                changeSlope.setText(">>");
                title.setText(dialog.getContext().getResources().getString(R.string.slopeY));
                setA.setVisibility(View.VISIBLE);
                setB.setVisibility(View.VISIBLE);
                title.setTextColor(Color.WHITE);
                mainLayout.setBackgroundColor(MyColorClass.colorY_2D);
                value.setText(strTmpY);
            }
        });

        b1.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("1"));
            resetColor();
        });

        b2.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("2"));
            resetColor();
        });

        b3.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("3"));
            resetColor();
        });

        b4.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("4"));
            resetColor();
        });

        b5.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("5"));
            resetColor();
        });

        b6.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("6"));
            resetColor();
        });

        b7.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("7"));
            resetColor();
        });

        b8.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("8"));
            resetColor();
        });

        b9.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("9"));
            resetColor();

        });

        b0.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("0"));
            resetColor();
        });

        bdot.setOnClickListener((View v) -> {
            if (cbool) {
                value.setText("");
                cbool = false;
            }
            value.setText(value.getText().toString().concat("."));
            resetColor();
        });

        bcanc.setOnClickListener((View v) -> {
            value.setText(Utils.readAngolo("0"));
            cbool = true;
            resetColor();

        });

        bdel.setOnClickListener((View v) -> {
            if (value.getText().toString().length() > 0) {
                value.setText(value.getText().toString().substring(0, value.getText().toString().length() - 1));
                resetColor();
            }

        });
    }

    private void resetColor() {
        setA.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.dark_gray));
        setA.setTextColor(Color.WHITE);
        setB.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.dark_gray));
        setB.setTextColor(Color.WHITE);
    }
}