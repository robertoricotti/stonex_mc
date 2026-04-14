package gui.dialogs_and_toast;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stx_dig.R;

import java.util.Arrays;

import packexcalib.gnss.CircumferenceCenterCalculator;
import utils.FullscreenActivity;
import utils.MyData;

public class Dialog_HiddenPin_Calc {
    Activity activity;
    public Dialog dialog;
    ImageView btn_calc, close, btn_apply;
    EditText etX1, etX2, etX3, etY1, etY2, etY3;

    TextView res_m, res_ft, res_deg;

    boolean isNumber1 = false, isNumber2 = false, isNumber3 = false, isNumber4 = false, isNumber5 = false, isNumber6 = false;

    public Dialog_HiddenPin_Calc(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);

    }

    public void show() {

        dialog.create();
        dialog.setContentView(R.layout.dialog_hidden_pin);

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
        init();
        onClick();


    }

    private void findView() {
        close = dialog.findViewById(R.id.esci);
        btn_calc = dialog.findViewById(R.id.calcol);
        btn_apply = dialog.findViewById(R.id.applica);
        etX1 = dialog.findViewById(R.id.et_x1);
        etX2 = dialog.findViewById(R.id.et_x2);
        etX3 = dialog.findViewById(R.id.et_x3);
        etY1 = dialog.findViewById(R.id.et_y1);
        etY2 = dialog.findViewById(R.id.et_y2);
        etY3 = dialog.findViewById(R.id.et_y3);
        res_m = dialog.findViewById(R.id.txt_result_m);
        res_ft = dialog.findViewById(R.id.txt_result_ft);
        res_deg = dialog.findViewById(R.id.txt_result_deg);
        btn_apply.setVisibility(View.INVISIBLE);

    }

    private void onClick() {
        etX1.setOnClickListener(view -> {
            new CustomNumberDialog(activity, 0).show(etX1);
        });
        etX2.setOnClickListener(view -> {
            new CustomNumberDialog(activity, 0).show(etX2);
        });
        etX3.setOnClickListener(view -> {
            new CustomNumberDialog(activity, 0).show(etX3);
        });
        etY1.setOnClickListener(view -> {
            new CustomNumberDialog(activity, 0).show(etY1);
        });
        etY2.setOnClickListener(view -> {
            new CustomNumberDialog(activity, 0).show(etY2);
        });
        etY3.setOnClickListener(view -> {
            new CustomNumberDialog(activity, 0).show(etY3);
        });
        close.setOnClickListener(view -> {
            dialog.dismiss();
        });
        btn_apply.setOnClickListener(view -> {
            // Crea un nuovo AlertDialog.Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(" " + activity.getResources().getString(R.string.save));
            builder.setIcon(activity.getResources().getDrawable(R.drawable.save));

            // Aggiungi il pulsante "Sì"
            builder.setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {


                }
            });
            builder.show();

        });

        btn_calc.setOnClickListener(view -> {

            calcola();
        });

    }

    private void calcola() {
        isNumber1 = isDecimalNumber(etX1.getText().toString());
        isNumber2 = isDecimalNumber(etX2.getText().toString());
        isNumber3 = isDecimalNumber(etX3.getText().toString());
        isNumber4 = isDecimalNumber(etY1.getText().toString());
        isNumber5 = isDecimalNumber(etY2.getText().toString());
        isNumber6 = isDecimalNumber(etY3.getText().toString());

        if (isNumber1 && isNumber2 && isNumber3 && isNumber4 && isNumber5 && isNumber6) {
            double x1 = Double.parseDouble(etX1.getText().toString());
            double x2 = Double.parseDouble(etX2.getText().toString());
            double x3 = Double.parseDouble(etX3.getText().toString());
            double y1 = Double.parseDouble(etY1.getText().toString());
            double y2 = Double.parseDouble(etY2.getText().toString());
            double y3 = Double.parseDouble(etY3.getText().toString());
            double[] result = CircumferenceCenterCalculator.findCircumferenceCenter(x1, y1, x2, y2, x3, y3);
            res_m.setText(String.format("%.3f", result[2]).replace(",", ".") + " m");
            res_deg.setText(String.format("%.2f", result[3]).replace(",", ".") + " °");
            res_ft.setText(String.format("%.4f", (result[2] * 3.28083333333)).replace(",", ".") + " ft");
            String[] resultArray = new String[]{res_m.getText().toString().replace(" m", ""), res_deg.getText().toString().replace(" °", ""), res_ft.getText().toString().replace(" ft", "")};
            MyData.push("boomresult", Arrays.toString(resultArray));

        } else {
            res_m.setText("Input Err");
            res_ft.setText("Input Err");
            res_deg.setText("Input Err");
        }
    }

    public boolean isDecimalNumber(String input) {
        // Definisci il pattern di un numero decimale
        String decimalPattern = "^[-+]?\\d*\\.?\\d+$";

        // Verifica se la stringa corrisponde al pattern
        return input.matches(decimalPattern);
    }

    private void init() {
        String[] a = MyData.get_String("boomresult").split(",");
        res_m.setText(a[0].replace("[", "") + " m");
        res_deg.setText(a[1] + " °");
        res_ft.setText(a[2].replace("]", "") + " ft");
    }
}
