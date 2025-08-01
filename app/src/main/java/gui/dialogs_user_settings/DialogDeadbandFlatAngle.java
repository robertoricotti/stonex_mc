package gui.dialogs_user_settings;

import static utils.Utils.isNumeric;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stx_dig.R;

import utils.FullscreenActivity;
import utils.MyData;
import utils.Utils;

public class DialogDeadbandFlatAngle {
    Activity activity;
    public Dialog dialog;
    ImageView canc, save;
    EditText value;
    TextView title, measure;
    Button b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bdot, bcanc, bdel;
    boolean c = true;

    public DialogDeadbandFlatAngle(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    @SuppressLint("SetTextI18n")
    public void show() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_flat_deadband);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // layout trasparente
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.dimAmount = 0.7f; //  Offusca sfondo (0 = nessun dim, 1 = nero pieno)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 🔹 Applica dim
            window.setAttributes(wlp);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;

        // Calcola 75% della larghezza dello schermo
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels * 0.85);
        int height = (int) (displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();
        title.setText(dialog.getContext().getResources().getString(R.string.flat_angle_deadband));
        value.setText(Utils.readAngolo(MyData.get_String("Deadband_FlatAngle")));
        measure.setText("(" + Utils.getGradiSimbol() + " )");
    }

    private void findView() {
        title = dialog.findViewById(R.id.title);
        canc = dialog.findViewById(R.id.exit);
        save = dialog.findViewById(R.id.save);
        value = dialog.findViewById(R.id.value);
        measure = dialog.findViewById(R.id.unitOfMeasure);
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
    }

    private void onClick() {
        save.setOnClickListener((View v) -> {
            if (isNumeric(value.getText().toString())) {
                MyData.push("Deadband_FlatAngle", Utils.writeGradi(value.getText().toString()));
                dialog.cancel();
            } else {
                Toast.makeText(activity, "Error INPUT!", Toast.LENGTH_SHORT).show();
            }
        });

        canc.setOnClickListener((View v) -> {
            dialog.cancel();
        });

        b1.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("1"));
        });

        b2.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("2"));
        });

        b3.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("3"));
        });

        b4.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("4"));
        });

        b5.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("5"));
        });

        b6.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("6"));
        });

        b7.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("7"));
        });

        b8.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("8"));
        });

        b9.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("9"));
        });

        b0.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("0"));
        });

        bdot.setOnClickListener((View v) -> {
            if (c) {
                value.setText("");
                c = false;
            }
            value.setText(value.getText().toString().concat("."));
        });

        bcanc.setOnClickListener((View v) -> {
            value.setText(Utils.readAngolo("0"));
            c = true;
        });

        bdel.setOnClickListener((View v) -> {
            if (value.getText().toString().length() > 0)
                value.setText(value.getText().toString().substring(0, value.getText().toString().length() - 1));
        });
    }
}
