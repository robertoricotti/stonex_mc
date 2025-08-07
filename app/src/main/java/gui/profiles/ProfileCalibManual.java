package gui.profiles;

import static gui.MyApp.isApollo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import java.util.ArrayList;

import gui.dialogs_and_toast.CustomToast;
import gui.draw_class.DrawPreviewProfile;
import gui.dialogs_and_toast.CustomQwertyDialog;
import utils.MyData;
import utils.Utils;

public class ProfileCalibManual extends AppCompatActivity {

    Button set1, set2, set3, set4, set5, set6, auto, reset, set;

    EditText editX, editY, editWidth, editSlope;

    TextView width_tv, slope_tv;

    Button exit, save;

    ConstraintLayout preview;

    EditText name;


    ArrayList<PointF> points;

    int indexProfile;

    DrawPreviewProfile drawPreview;

    ImageButton zoomIn, zoomOut, vista, center;

    CustomQwertyDialog qwertyDialog;
    SetWidthAndSlope customNumberDialog;

    int indexPoint = 0;
    boolean modify = false;
    int indexLastPick;

    String[] width = new String[6];
    String[] slope = new String[6];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_calib_manual);
        findView();
        init();
        onClick();
        updateUI();
    }

    private void findView() {
        set = findViewById(R.id.set);
        auto = findViewById(R.id.auto);
        exit = findViewById(R.id.exit);
        save = findViewById(R.id.save);
        set1 = findViewById(R.id.set1);
        set2 = findViewById(R.id.set2);
        set3 = findViewById(R.id.set3);
        set4 = findViewById(R.id.set4);
        set5 = findViewById(R.id.set5);
        set6 = findViewById(R.id.set6);
        preview = findViewById(R.id.preview);
        editX = findViewById(R.id.edit_x);
        editY = findViewById(R.id.edit_y);
        width_tv = findViewById(R.id.width_tv);
        slope_tv = findViewById(R.id.slope_tv);
        reset = findViewById(R.id.reset);
        zoomIn = findViewById(R.id.zoomIn);
        zoomOut = findViewById(R.id.zoomOut);
        vista = findViewById(R.id.vista);
        center = findViewById(R.id.center);
        name = findViewById(R.id.profileName);
        editWidth = findViewById(R.id.editWidth);
        editSlope = findViewById(R.id.editSlope);

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void init() {

        qwertyDialog = new CustomQwertyDialog(this,null);

        customNumberDialog = new SetWidthAndSlope(this);

        indexProfile = getIntent().getExtras().getInt("indexProfile");

        indexProfile = getIntent().getExtras().getInt("indexProfile");

        points = new ArrayList<>();

        width_tv.setText("WIDTH " + Utils.getMetriSimbol());
        slope_tv.setText("SLOPE " + Utils.getGradiSimbol());

        String pts = MyData.get_String("Profile" + indexProfile + "_punti");

        if (pts.length() > 0) {
            for (int i = 0; i < pts.split(";").length; i++) {
                points.add(new PointF(Float.parseFloat(pts.split(";")[i].split("/")[1]), Float.parseFloat(pts.split(";")[i].split("/")[2])));
            }
        }


        if (points.size() > 0) {

            indexPoint = points.size();
            modify = true;
            width[0] = Utils.readUnitOfMeasure("0");
            slope[0] = Utils.readUnitOfMeasure("0");


            for (int i = 1; i < points.size(); i++) {

                double x1 = points.get(i - 1).x;
                double y1 = points.get(i - 1).y;

                double x2 = points.get(i).x;
                double y2 = points.get(i).y;

                double diff_x = x2 - x1;
                double diff_y = y2 - y1;

                double lunghezza_inversa = Math.sqrt(diff_x * diff_x + diff_y * diff_y);

                double pendenza_inversa = Math.atan2(diff_y, diff_x);

                double pendenza_inversa_gradi = Math.toDegrees(pendenza_inversa);


                width[i] = Utils.readUnitOfMeasure(String.valueOf(lunghezza_inversa));
                slope[i] = Utils.readAngolo(String.valueOf(pendenza_inversa_gradi));
            }

            for (int i = points.size(); i < width.length; i++) {
                width[i] = Utils.readUnitOfMeasure("0");
                slope[i] = Utils.readAngolo("0");
            }


        } else {
            indexPoint = 2;
            points.add(0, new PointF(0, 0));
            for (int i = 0; i < width.length; i++) {
                width[i] = Utils.readUnitOfMeasure("0");
                slope[i] = Utils.readAngolo("0");
            }
        }

        name.setText(MyData.get_String("Profile" + indexProfile + "_name"));

        drawPreview = new DrawPreviewProfile(this, points);

        preview.addView(drawPreview);
    }


    private void save() {
        MyData.push("Profile" + indexProfile + "_name", name.getText().toString().trim().toUpperCase());
        MyData.push("Profile" + indexProfile + "_page", "1");

        StringBuilder pts = new StringBuilder();
        for (int i = 1; i <= points.size(); i++) {
            pts.append("0/").append(points.get(i - 1).x).append("/").append(points.get(i - 1).y).append(i != points.size() ? ";" : "");
        }
        MyData.push("Profile" + indexProfile + "_punti", String.valueOf(pts));
    }

    private void disableAll() {
        save.setEnabled(false);
        exit.setEnabled(false);
        auto.setEnabled(false);
    }

    private void onClick() {

        save.setOnClickListener((View v) -> {
            if (name.getText().toString().equals("") || name.getText().toString().contains("EMPTY")) {
                new CustomToast(this, "Missing name\nEMPTY is not a valid name").show_alert();
            } else {
                disableAll();
                save();
                startActivity(new Intent(this, ProfilesMenuActivity.class));
                finish();
            }
        });

        exit.setOnClickListener((View v) -> {
            startActivity(new Intent(this, ProfilesMenuActivity.class));
            finish();
        });

        auto.setOnClickListener((View v) -> {

            Intent intent = new Intent(this, ProfileCalibAuto.class);
            intent.putExtra("indexProfile", indexProfile);
            startActivity(intent);
            finish();
        });

        editWidth.setOnClickListener((View v) -> {
            if (indexPoint == 1) {
                new CustomToast(this, "LOCK!").show();
            } else {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(indexPoint, 1);
                }
            }
        });

        editSlope.setOnClickListener((View v) -> {
            if (indexPoint == 1) {
                new CustomToast(this, "LOCK!").show();
            } else {
                if (!customNumberDialog.dialog.isShowing()) {
                    customNumberDialog.show(indexPoint, 2);
                }
            }
        });

        set.setOnClickListener((View v) -> {
            if (indexLastPick > 1) {
                points = new ArrayList<>();
                points.add(new PointF(0, 0));
                for (int i = 0; i < indexLastPick - 1; i++) {
                    // Coordinata A
                    double x1 = points.get(i).x;
                    double y1 = points.get(i).y;

                    // Lunghezza e pendenza
                    double lunghezza = Double.parseDouble(Utils.writeMetri(width[i + 1]));
                    double pendenza = Math.toRadians(Double.parseDouble(Utils.writeGradi(slope[i + 1])));  // Converti l'angolo di pendenza in radianti

                    // Calcola le variazioni sulle coordinate
                    double delta_x = lunghezza * Math.cos(pendenza);
                    double delta_y = lunghezza * Math.sin(pendenza);

                    // Calcola le coordinate del punto B

                    points.add(i + 1, new PointF((float) (x1 + delta_x), (float) (y1 + delta_y)));
                }
                drawPreview.pPoints = points;
                drawPreview.invalidate();
            }
        });

        set1.setOnClickListener((View v) -> {
            if (1 < indexLastPick + 1) {
                indexPoint = 1;
            }
        });

        set2.setOnClickListener((View v) -> {
            if (2 <= indexLastPick + 1)
                indexPoint = 2;
            else
                new CustomToast(this, "SET PREVIOUS POINT!").show();
        });

        set3.setOnClickListener((View v) -> {
            if (3 <= indexLastPick + 1)
                indexPoint = 3;
            else
                new CustomToast(this, "SET PREVIOUS POINT!").show();
        });

        set4.setOnClickListener((View v) -> {
            if (4 <= indexLastPick + 1)
                indexPoint = 4;
            else
                new CustomToast(this, "SET PREVIOUS POINT!").show();
        });

        set5.setOnClickListener((View v) -> {
            if (5 <= indexLastPick + 1)
                indexPoint = 5;
            else
                new CustomToast(this, "SET PREVIOUS POINT!").show();
        });

        set6.setOnClickListener((View v) -> {
            if (6 <= indexLastPick + 1)
                indexPoint = 6;
            else
                new CustomToast(this, "SET PREVIOUS POINT!").show();
        });

        zoomIn.setOnClickListener((View v) -> {
            if (drawPreview.pPoints.size() > 1) {
                drawPreview.mScaleFactor *= 1.2f;
                drawPreview.mScaleFactor = Math.max(0.1f, Math.min(drawPreview.mScaleFactor, 10.0f));
                drawPreview.invalidate();
            } else {
                drawPreview.mScaleFactor = 1f;
                drawPreview.invalidate();
            }
        });

        zoomOut.setOnClickListener((View v) -> {
            if (drawPreview.pPoints.size() > 1) {
                drawPreview.mScaleFactor *= 0.80;
                drawPreview.mScaleFactor = Math.max(0.01f, Math.min(drawPreview.mScaleFactor, 10.0f));
                drawPreview.invalidate();
            } else {
                drawPreview.mScaleFactor = 1f;
                drawPreview.invalidate();
            }
        });

        vista.setOnClickListener((View v) -> {
            drawPreview.vista = drawPreview.vista == 0 ? 1 : 0;
            drawPreview.invalidate();
        });

        center.setOnClickListener((View v) -> {
            drawPreview.offsetX = 0;
            drawPreview.offsetY = 0;
            drawPreview.invalidate();
        });

        name.setOnClickListener((View v) -> {
            if (!qwertyDialog.dialog.isShowing()) {
                qwertyDialog.show(name);
            }
        });

        reset.setOnClickListener((View v) -> {
            points = new ArrayList<>();
            points.add(0, new PointF(0, 0));
            for (int i = 0; i < width.length; i++) {
                width[i] = Utils.readUnitOfMeasure("0");
                slope[i] = Utils.readAngolo("0");
            }
            drawPreview.pPoints = points;
            drawPreview.mScaleFactor = 1f;
            drawPreview.invalidate();
            indexPoint = 2;
            modify = false;
        });
    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {

        set1.setBackgroundTintList(ContextCompat.getColorStateList(this, indexPoint == 1 ? R.color.blue : R.color.dark_gray));
        set2.setBackgroundTintList(ContextCompat.getColorStateList(this, indexPoint == 2 ? R.color.blue : R.color.dark_gray));
        set3.setBackgroundTintList(ContextCompat.getColorStateList(this, indexPoint == 3 ? R.color.blue : R.color.dark_gray));
        set4.setBackgroundTintList(ContextCompat.getColorStateList(this, indexPoint == 4 ? R.color.blue : R.color.dark_gray));
        set5.setBackgroundTintList(ContextCompat.getColorStateList(this, indexPoint == 5 ? R.color.blue : R.color.dark_gray));
        set6.setBackgroundTintList(ContextCompat.getColorStateList(this, indexPoint == 6 ? R.color.blue : R.color.dark_gray));


        editWidth.setText(width[indexPoint - 1]);
        editSlope.setText(slope[indexPoint - 1]);

        for (int i = 1; i <= width.length; i++) {
            if (i == width.length) {
                indexLastPick = width.length;
                break;
            }
            if (Double.parseDouble(Utils.writeMetri(width[i])) == 0 && Double.parseDouble(Utils.writeGradi(slope[i])) == 0) {
                indexLastPick = i;
                break;
            }
        }

        try {
            editX.setText(Utils.readUnitOfMeasure(String.valueOf(points.get(indexPoint - 1).x)));
            editY.setText(Utils.readUnitOfMeasure(String.valueOf(points.get(indexPoint - 1).y)));
        } catch (Exception ignored) {
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
