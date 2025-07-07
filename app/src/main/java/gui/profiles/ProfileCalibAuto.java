package gui.profiles;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Arrays;

import gui.dialogs_and_toast.CustomQwertyDialog;
import gui.dialogs_and_toast.CustomToast;
import gui.draw_class.DrawPreviewProfile;
import packexcalib.exca.ExcavatorLib;
import utils.MyData;
import utils.Utils;

public class ProfileCalibAuto extends AppCompatActivity {


    Button set1, set2, set3, set4, set5, set6, manual, reset;

    TextView length1_2, length2_3, length3_4, length4_5, length5_6;

    TextView slope1_2, slope2_3, slope3_4, slope4_5, slope5_6;

    Button exit, save;

    ConstraintLayout preview;

    EditText name;


    double offsetH, offsetR;

    ArrayList<PointF> points;

    int indexProfile;

    DrawPreviewProfile drawPreview;

    ImageButton zoomIn, zoomOut, vista, center;

    CustomQwertyDialog qwertyDialog;

    int indexLastPick = 0;
    boolean modify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_calib_auto);

        findView();
        init();
        updateUI();
        onClick();
    }

    private void findView() {
        set1 = findViewById(R.id.set1);
        set2 = findViewById(R.id.set2);
        set3 = findViewById(R.id.set3);
        set4 = findViewById(R.id.set4);
        set5 = findViewById(R.id.set5);
        set6 = findViewById(R.id.set6);

        length1_2 = findViewById(R.id.length12);
        length2_3 = findViewById(R.id.length23);
        length3_4 = findViewById(R.id.length34);
        length4_5 = findViewById(R.id.length45);
        length5_6 = findViewById(R.id.length56);

        slope1_2 = findViewById(R.id.slope12);
        slope2_3 = findViewById(R.id.slope23);
        slope3_4 = findViewById(R.id.slope34);
        slope4_5 = findViewById(R.id.slope45);
        slope5_6 = findViewById(R.id.slope56);

        manual = findViewById(R.id.manual);
        reset = findViewById(R.id.reset);
        exit = findViewById(R.id.exit);
        save = findViewById(R.id.save);
        preview = findViewById(R.id.preview);
        name = findViewById(R.id.name);
        zoomIn = findViewById(R.id.zoomIn);
        zoomOut = findViewById(R.id.zoomOut);
        vista = findViewById(R.id.vista);
        center = findViewById(R.id.center);
    }

    private void init() {


        qwertyDialog = new CustomQwertyDialog(this);

        indexProfile = getIntent().getExtras().getInt("indexProfile");

        points = new ArrayList<>();


        String pts = MyData.get_String("Profile" + indexProfile + "_punti");

        if (pts.length() > 0) {
            for (int i = 0; i < pts.split(";").length; i++) {
                points.add(new PointF(Float.parseFloat(pts.split(";")[i].split("/")[1]), Float.parseFloat(pts.split(";")[i].split("/")[2])));
            }
        }


        if (points.size() > 0) {
            Button[] buttonSets = new Button[]{set1, set2, set3, set4, set5, set6};
            for (int i = 0; i < points.size(); i++) {
                buttonSets[i].setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
            }
            indexLastPick = points.size();
            modify = true;
        }

        name.setText(MyData.get_String("Profile" + indexProfile + "_name"));

        drawPreview = new DrawPreviewProfile(this, points);

        preview.addView(drawPreview);
    }

    public void updateUI() {
        String[] p1_2 = points.size() > 1 ? calcolaDistanceAndSlope(points.get(0).x, points.get(1).x, points.get(0).y, points.get(1).y) : new String[]{"", ""};
        String[] p2_3 = points.size() > 2 ? calcolaDistanceAndSlope(points.get(1).x, points.get(2).x, points.get(1).y, points.get(2).y) : new String[]{"", ""};
        String[] p3_4 = points.size() > 3 ? calcolaDistanceAndSlope(points.get(2).x, points.get(3).x, points.get(2).y, points.get(3).y) : new String[]{"", ""};
        String[] p4_5 = points.size() > 4 ? calcolaDistanceAndSlope(points.get(3).x, points.get(4).x, points.get(3).y, points.get(4).y) : new String[]{"", ""};
        String[] p5_6 = points.size() > 5 ? calcolaDistanceAndSlope(points.get(4).x, points.get(5).x, points.get(4).y, points.get(5).y) : new String[]{"", ""};

        length1_2.setText(p1_2[0]);
        slope1_2.setText(p1_2[1]);

        length2_3.setText(p2_3[0]);
        slope2_3.setText(p2_3[1]);

        length3_4.setText(p3_4[0]);
        slope3_4.setText(p3_4[1]);

        length4_5.setText(p4_5[0]);
        slope4_5.setText(p4_5[1]);

        length5_6.setText(p5_6[0]);
        slope5_6.setText(p5_6[1]);

    }

    private String[] calcolaDistanceAndSlope(float value1, float value2, float value3, float value4) {
        try {

            double diff_x = (double) value2 - (double) value1;
            double diff_y = (double) value4 - (double) value3;

            double lunghezza_inversa = Math.sqrt(diff_x * diff_x + diff_y * diff_y);

            double pendenza_inversa = Math.atan2(diff_y, diff_x);

            double pendenza_inversa_gradi = Math.toDegrees(pendenza_inversa);
            return new String[]{Utils.readUnitOfMeasure(String.valueOf(Math.abs(lunghezza_inversa))), Utils.readAngolo(String.valueOf(pendenza_inversa_gradi)) + Utils.getGradiSimbol()};
        } catch (Exception e) {
            return new String[]{"", ""};
        }
    }

    private void save() {
        MyData.push("Profile" + indexProfile + "_name", name.getText().toString().trim().toUpperCase());
        MyData.push("Profile" + indexProfile + "_page", "0");

        StringBuilder pts = new StringBuilder();
        for (int i = 1; i <= points.size(); i++) {
            pts.append("0/").append(points.get(i - 1).x).append("/").append(points.get(i - 1).y).append(i != points.size() ? ";" : "");
        }
        MyData.push("Profile" + indexProfile + "_punti", String.valueOf(pts));

        Log
                .d("VALORE", ": " + pts);
    }

    private void disableAll() {
        exit.setEnabled(false);
        save.setEnabled(false);
    }

    private void onClick() {
        set1.setOnClickListener((View v) -> {
            if ((set1.getBackgroundTintList() != ContextCompat.getColorStateList(this, R.color.blue)) || (indexLastPick == 1 && !modify)) {
                set1.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
                manual.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_gray));
                offsetH = ExcavatorLib.bucketCoord[2];
                offsetR = ExcavatorLib.bucketCoord[1];
                if (indexLastPick == 1)
                    points.set(0, new PointF(0, 0));
                else
                    points.add(0, new PointF(0, 0));

                drawPreview.pPoints = points;
                drawPreview.invalidate();
                indexLastPick = 1;
            } else {
                new CustomToast(this, "ALREADY SET!").show();
            }

        });

        set2.setOnClickListener((View v) -> {
            if ((set1.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.blue) && set2.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.dark_gray)) || (indexLastPick == 2 && !modify)) {
                set2.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
                if (indexLastPick == 2)
                    points.set(1, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                else
                    points.add(1, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                drawPreview.pPoints = points;
                drawPreview.invalidate();
                indexLastPick = 2;
            } else if (set1.getBackgroundTintList() != ContextCompat.getColorStateList(this, R.color.blue)) {
                new CustomToast(this, "SET 1 FIRST!").show();
            } else {
                new CustomToast(this, "ALREADY SET!").show();
            }
        });

        set3.setOnClickListener((View v) -> {
            if ((set2.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.blue) && set3.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.dark_gray)) || (indexLastPick == 3 && !modify)) {
                set3.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
                if (indexLastPick == 3)
                    points.set(2, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                else
                    points.add(2, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                drawPreview.pPoints = points;
                drawPreview.invalidate();
                indexLastPick = 3;

            } else if (set2.getBackgroundTintList() != ContextCompat.getColorStateList(this, R.color.blue)) {
                new CustomToast(this, "SET 2 FIRST!").show();
            } else {
                new CustomToast(this, "ALREADY SET!").show();
            }
        });

        set4.setOnClickListener((View v) -> {
            if ((set3.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.blue) && set4.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.dark_gray)) || (indexLastPick == 4 && !modify)) {
                set4.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
                if (indexLastPick == 4)
                    points.set(3, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                else
                    points.add(3, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                drawPreview.pPoints = points;
                drawPreview.invalidate();
                indexLastPick = 4;
            } else if (set3.getBackgroundTintList() != ContextCompat.getColorStateList(this, R.color.blue)) {
                new CustomToast(this, "SET 3 FIRST!").show();
            } else {
                new CustomToast(this, "ALREADY SET!").show();
            }
        });

        set5.setOnClickListener((View v) -> {
            if ((set4.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.blue) && set5.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.dark_gray)) || (indexLastPick == 5 && !modify)) {
                set5.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
                if (indexLastPick == 5)
                    points.set(4, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                else
                    points.add(4, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                drawPreview.pPoints = points;
                drawPreview.invalidate();
                indexLastPick = 5;
            } else if (set4.getBackgroundTintList() != ContextCompat.getColorStateList(this, R.color.blue)) {
                new CustomToast(this, "SET 4 FIRST!").show();
            } else {
                new CustomToast(this, "ALREADY SET!").show();
            }
        });

        set6.setOnClickListener((View v) -> {
            if ((set5.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.blue) && set6.getBackgroundTintList() == ContextCompat.getColorStateList(this, R.color.dark_gray)) || (indexLastPick == 6 && !modify)) {
                set6.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
                if (indexLastPick == 6)
                    points.set(5, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                else
                    points.add(5, new PointF((float) Math.abs(ExcavatorLib.bucketCoord[1] - offsetR), (float) (ExcavatorLib.bucketCoord[2] - offsetH)));
                System.out.println(Arrays.toString(points.toArray()));
                drawPreview.pPoints = points;
                drawPreview.invalidate();
                indexLastPick = 6;
            } else if (set5.getBackgroundTintList() != ContextCompat.getColorStateList(this, R.color.blue)) {
                new CustomToast(this, "SET 5 FIRST!").show();
            } else {
                new CustomToast(this, "ALREADY SET!").show();
            }
        });

        name.setOnClickListener((View v) -> {
            if (!qwertyDialog.dialog.isShowing()) {
                qwertyDialog.show(name);
            }
        });


        reset.setOnClickListener((View v) -> {
            points = new ArrayList<>();
            set1.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_gray));
            set2.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_gray));
            set3.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_gray));
            set4.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_gray));
            set5.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_gray));
            set6.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_gray));
            manual.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dark_gray));
            drawPreview.pPoints = points;
            drawPreview.mScaleFactor = 1f;
            drawPreview.invalidate();
            indexLastPick = 0;
            modify = false;
        });

        manual.setOnClickListener((View v) -> {
            disableAll();
            Intent intent = new Intent(this, ProfileCalibManual.class);
            intent.putExtra("indexProfile", indexProfile);
            startActivity(intent);
            finish();
        });

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
            disableAll();
            startActivity(new Intent(this, ProfilesMenuActivity.class));
            finish();
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
