package gui.buckets;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.stx_dig.R;

import gui.BaseClass;
import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.dialogs_user_settings.DialogUnitOfMeasure;
import gui.my_opengl.My3DActivity;
import gui.tech_menu.ExcavatorChooserActivity;
import services.UpdateValuesService;
import utils.MyData;

public class
BucketChooserActivity extends BaseClass {
    DialogUnitOfMeasure dialogUnitOfMeasure;
    int unitOfMeasure;
    private boolean hasTilt1, hasTilt2, hasTilt3, hasTilt4, hasTilt5, hasTilt6, hasTilt7, hasTilt8, hasTilt9, hasTilt10;
    private boolean hasTilt11, hasTilt12, hasTilt13, hasTilt14, hasTilt15, hasTilt16, hasTilt17, hasTilt18, hasTilt19, hasTilt20;
    ImageView bucket1, bucket2, bucket3, bucket4, bucket5, bucket6, bucket7, bucket8, bucket9, bucket10;

    ImageView bucket11, bucket12, bucket13, bucket14, bucket15, bucket16, bucket17, bucket18, bucket19, bucket20;
    TextView bucketName1, bucketName2, bucketName3, bucketName4, bucketName5, bucketName6, bucketName7, bucketName8, bucketName9, bucketName10;
    TextView bucketName11, bucketName12, bucketName13, bucketName14, bucketName15, bucketName16, bucketName17, bucketName18, bucketName19, bucketName20;
    ImageView back;
    private boolean isDefault_1, isDefault_2, isDefault_3, isDefault_4, isDefault_5, isDefault_6, isDefault_7, isDefault_8, isDefault_9, isDefault_10;
    private boolean isDefault_11, isDefault_12, isDefault_13, isDefault_14, isDefault_15, isDefault_16, isDefault_17, isDefault_18, isDefault_19, isDefault_20;

    int indexBucketSelected;
    int indexMachineSelected;

    ProgressBar progressBar;
    Intent goBackIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buckets_20);
        findView();
        init();
        onClick();
        onLongClick();
        updateUI();
        try {

            String whoDig = getIntent().getStringExtra("whoDig");
            if (whoDig == null) {
                goBackIntent = new Intent(this, Activity_Home_Page.class);
            } else {
                if (whoDig.contains("ExcavatorChooserActivity")) {
                    goBackIntent = new Intent(this, ExcavatorChooserActivity.class);
                } else if (whoDig.contains("My3DActivity")) {
                    goBackIntent = new Intent(this, My3DActivity.class);
                }
            }
        } catch (Exception e) {

            goBackIntent = new Intent(this, Activity_Home_Page.class);
        }

    }


    private void findView() {
        bucket1 = findViewById(R.id.imageBucket1);
        bucket2 = findViewById(R.id.imageBucket2);
        bucket3 = findViewById(R.id.imageBucket3);
        bucket4 = findViewById(R.id.imageBucket4);
        bucket5 = findViewById(R.id.imageBucket5);
        bucket6 = findViewById(R.id.imageBucket6);
        bucket7 = findViewById(R.id.imageBucket7);
        bucket8 = findViewById(R.id.imageBucket8);
        bucket9 = findViewById(R.id.imageBucket9);
        bucket10 = findViewById(R.id.imageBucket10);

        bucket11 = findViewById(R.id.imageBucket11);
        bucket12 = findViewById(R.id.imageBucket12);
        bucket13 = findViewById(R.id.imageBucket13);
        bucket14 = findViewById(R.id.imageBucket14);
        bucket15 = findViewById(R.id.imageBucket15);
        bucket16 = findViewById(R.id.imageBucket16);
        bucket17 = findViewById(R.id.imageBucket17);
        bucket18 = findViewById(R.id.imageBucket18);
        bucket19 = findViewById(R.id.imageBucket19);
        bucket20 = findViewById(R.id.imageBucket20);

        bucketName1 = findViewById(R.id.nameBucket1);
        bucketName2 = findViewById(R.id.nameBucket2);
        bucketName3 = findViewById(R.id.nameBucket3);
        bucketName4 = findViewById(R.id.nameBucket4);
        bucketName5 = findViewById(R.id.nameBucket5);
        bucketName6 = findViewById(R.id.nameBucket6);
        bucketName7 = findViewById(R.id.nameBucket7);
        bucketName8 = findViewById(R.id.nameBucket8);
        bucketName9 = findViewById(R.id.nameBucket9);
        bucketName10 = findViewById(R.id.nameBucket10);

        bucketName11 = findViewById(R.id.nameBucket11);
        bucketName12 = findViewById(R.id.nameBucket12);
        bucketName13 = findViewById(R.id.nameBucket13);
        bucketName14 = findViewById(R.id.nameBucket14);
        bucketName15 = findViewById(R.id.nameBucket15);
        bucketName16 = findViewById(R.id.nameBucket16);
        bucketName17 = findViewById(R.id.nameBucket17);
        bucketName18 = findViewById(R.id.nameBucket18);
        bucketName19 = findViewById(R.id.nameBucket19);
        bucketName20 = findViewById(R.id.nameBucket20);

        back = findViewById(R.id.back);

        progressBar = findViewById(R.id.progressBar);
        dialogUnitOfMeasure = new DialogUnitOfMeasure(this);

    }

    private void init() {
        progressBar.setVisibility(View.INVISIBLE);

        unitOfMeasure = MyData.get_Int("Unit_Of_Measure");
        indexMachineSelected = MyData.get_Int("MachineSelected");
        indexBucketSelected = MyData.get_Int("M" + indexMachineSelected + "BucketSelected");
        bucketName1.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 1 + "_Name"));
        bucketName2.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 2 + "_Name"));
        bucketName3.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 3 + "_Name"));
        bucketName4.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 4 + "_Name"));
        bucketName5.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 5 + "_Name"));
        bucketName6.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 6 + "_Name"));
        bucketName7.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 7 + "_Name"));
        bucketName8.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 8 + "_Name"));
        bucketName9.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 9 + "_Name"));
        bucketName10.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 10 + "_Name"));

        bucketName11.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 11 + "_Name"));
        bucketName12.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 12 + "_Name"));
        bucketName13.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 13 + "_Name"));
        bucketName14.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 14 + "_Name"));
        bucketName15.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 15 + "_Name"));
        bucketName16.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 16 + "_Name"));
        bucketName17.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 17 + "_Name"));
        bucketName18.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 18 + "_Name"));
        bucketName19.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 19 + "_Name"));
        bucketName20.setText(MyData.get_String("M" + indexMachineSelected + "_Bucket_" + 20 + "_Name"));

        isDefault_1 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 1 + "_Length") == 0d;
        isDefault_2 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 2 + "_Length") == 0d;
        isDefault_3 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 3 + "_Length") == 0d;
        isDefault_4 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 4 + "_Length") == 0d;
        isDefault_5 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 5 + "_Length") == 0d;
        isDefault_6 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 6 + "_Length") == 0d;
        isDefault_7 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 7 + "_Length") == 0d;
        isDefault_8 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 8 + "_Length") == 0d;
        isDefault_9 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 9 + "_Length") == 0d;
        isDefault_10 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 10 + "_Length") == 0d;
        isDefault_11 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 11 + "_Length") == 0d;
        isDefault_12 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 12 + "_Length") == 0d;
        isDefault_13 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 13 + "_Length") == 0d;
        isDefault_14 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 14 + "_Length") == 0d;
        isDefault_15 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 15 + "_Length") == 0d;
        isDefault_16 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 16 + "_Length") == 0d;
        isDefault_17 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 17 + "_Length") == 0d;
        isDefault_18 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 18 + "_Length") == 0d;
        isDefault_19 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 19 + "_Length") == 0d;
        isDefault_20 = MyData.get_Double("M" + indexMachineSelected + "_Bucket_" + 20 + "_Length") == 0d;


        hasTilt1 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 1).equals("0");
        hasTilt2 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 2).equals("0");
        hasTilt3 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 3).equals("0");
        hasTilt4 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 4).equals("0");
        hasTilt5 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 5).equals("0");
        hasTilt6 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 6).equals("0");
        hasTilt7 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 7).equals("0");
        hasTilt8 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 8).equals("0");
        hasTilt9 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 9).equals("0");
        hasTilt10 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 10).equals("0");
        hasTilt11 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 11).equals("0");
        hasTilt12 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 12).equals("0");
        hasTilt13 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 13).equals("0");
        hasTilt14 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 14).equals("0");
        hasTilt15 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 15).equals("0");
        hasTilt16 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 16).equals("0");
        hasTilt17 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 17).equals("0");
        hasTilt18 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 18).equals("0");
        hasTilt19 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 19).equals("0");
        hasTilt20 = !MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos" + 20).equals("0");

    }

    public void updateUI() {
        if (hasTilt1) {
            bucket1.setImageResource(R.drawable.benna_tilt_1);

        } else {
            bucket1.setImageResource(R.drawable.benna_vuota1);
        }
        if (hasTilt2) {
            bucket2.setImageResource(R.drawable.benna_tilt_2);

        } else {
            bucket2.setImageResource(R.drawable.benna_vuota2);
        }
        if (hasTilt3) {
            bucket3.setImageResource(R.drawable.benna_tilt_3);

        } else {
            bucket3.setImageResource(R.drawable.benna_vuota3);
        }
        if (hasTilt4) {
            bucket4.setImageResource(R.drawable.benna_tilt_4);

        } else {
            bucket4.setImageResource(R.drawable.benna_vuota4);
        }
        if (hasTilt5) {
            bucket5.setImageResource(R.drawable.benna_tilt_5);

        } else {
            bucket5.setImageResource(R.drawable.benna_vuota5);
        }
        if (hasTilt6) {
            bucket6.setImageResource(R.drawable.benna_tilt_6);

        } else {
            bucket6.setImageResource(R.drawable.benna_vuota6);
        }
        if (hasTilt7) {
            bucket7.setImageResource(R.drawable.benna_tilt_7);

        } else {
            bucket7.setImageResource(R.drawable.benna_vuota7);
        }
        if (hasTilt8) {
            bucket8.setImageResource(R.drawable.benna_tilt_8);

        } else {
            bucket8.setImageResource(R.drawable.benna_vuota8);
        }
        if (hasTilt9) {
            bucket9.setImageResource(R.drawable.benna_tilt_9);

        } else {
            bucket9.setImageResource(R.drawable.benna_vuota9);
        }
        if (hasTilt10) {
            bucket10.setImageResource(R.drawable.benna_tilt_10);

        } else {
            bucket10.setImageResource(R.drawable.benna_vuota10);
        }

        if (hasTilt11) {
            bucket11.setImageResource(R.drawable.benna_tilt_11);

        } else {
            bucket11.setImageResource(R.drawable.benna_vuota11);
        }
        if (hasTilt12) {
            bucket12.setImageResource(R.drawable.benna_tilt_12);

        } else {
            bucket12.setImageResource(R.drawable.benna_vuota12);
        }
        if (hasTilt13) {
            bucket13.setImageResource(R.drawable.benna_tilt_13);

        } else {
            bucket13.setImageResource(R.drawable.benna_vuota13);
        }
        if (hasTilt14) {
            bucket14.setImageResource(R.drawable.benna_tilt_14);

        } else {
            bucket14.setImageResource(R.drawable.benna_vuota14);
        }
        if (hasTilt15) {
            bucket15.setImageResource(R.drawable.benna_tilt_15);

        } else {
            bucket15.setImageResource(R.drawable.benna_vuota15);
        }
        if (hasTilt16) {
            bucket16.setImageResource(R.drawable.benna_tilt_16);

        } else {
            bucket16.setImageResource(R.drawable.benna_vuota16);
        }
        if (hasTilt17) {
            bucket17.setImageResource(R.drawable.benna_tilt_17);

        } else {
            bucket17.setImageResource(R.drawable.benna_vuota17);
        }
        if (hasTilt18) {
            bucket18.setImageResource(R.drawable.benna_tilt_18);

        } else {
            bucket18.setImageResource(R.drawable.benna_vuota18);
        }
        if (hasTilt19) {
            bucket19.setImageResource(R.drawable.benna_tilt_19);

        } else {
            bucket19.setImageResource(R.drawable.benna_vuota19);
        }
        if (hasTilt20) {
            bucket20.setImageResource(R.drawable.benna_tilt_20);

        } else {
            bucket20.setImageResource(R.drawable.benna_vuota20);
        }

        bucket1.setBackground(indexBucketSelected == 1 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));

        if (isDefault_1) {
            bucket1.setAlpha(0.2f);
        } else {
            bucket1.setAlpha(1.0f);
        }

        bucket2.setBackground(indexBucketSelected == 2 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_2) {
            bucket2.setAlpha(0.2f);
        } else {
            bucket2.setAlpha(1.0f);
        }

        bucket3.setBackground(indexBucketSelected == 3 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_3) {
            bucket3.setAlpha(0.2f);
        } else {
            bucket3.setAlpha(1.0f);
        }

        bucket4.setBackground(indexBucketSelected == 4 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_4) {
            bucket4.setAlpha(0.2f);
        } else {
            bucket4.setAlpha(1.0f);
        }

        bucket5.setBackground(indexBucketSelected == 5 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_5) {
            bucket5.setAlpha(0.2f);
        } else {
            bucket5.setAlpha(1.0f);
        }

        bucket6.setBackground(indexBucketSelected == 6 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_6) {
            bucket6.setAlpha(0.2f);
        } else {
            bucket6.setAlpha(1.0f);
        }

        bucket7.setBackground(indexBucketSelected == 7 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_7) {
            bucket7.setAlpha(0.2f);
        } else {
            bucket7.setAlpha(1.0f);
        }

        bucket8.setBackground(indexBucketSelected == 8 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_8) {
            bucket8.setAlpha(0.2f);
        } else {
            bucket8.setAlpha(1.0f);
        }

        bucket9.setBackground(indexBucketSelected == 9 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_9) {
            bucket9.setAlpha(0.2f);
        } else {
            bucket9.setAlpha(1.0f);
        }

        bucket10.setBackground(indexBucketSelected == 10 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_10) {
            bucket10.setAlpha(0.2f);
        } else {
            bucket10.setAlpha(1.0f);
        }

        bucket11.setBackground(indexBucketSelected == 11 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_11) {
            bucket11.setAlpha(0.2f);
        } else {
            bucket11.setAlpha(1.0f);
        }
        bucket12.setBackground(indexBucketSelected == 12 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_12) {
            bucket12.setAlpha(0.2f);
        } else {
            bucket12.setAlpha(1.0f);
        }
        bucket13.setBackground(indexBucketSelected == 13 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_13) {
            bucket13.setAlpha(0.2f);
        } else {
            bucket13.setAlpha(1.0f);
        }
        bucket14.setBackground(indexBucketSelected == 14 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_14) {
            bucket14.setAlpha(0.2f);
        } else {
            bucket14.setAlpha(1.0f);
        }
        bucket15.setBackground(indexBucketSelected == 15 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_15) {
            bucket15.setAlpha(0.2f);
        } else {
            bucket15.setAlpha(1.0f);
        }
        bucket16.setBackground(indexBucketSelected == 16 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_16) {
            bucket16.setAlpha(0.2f);
        } else {
            bucket16.setAlpha(1.0f);
        }
        bucket17.setBackground(indexBucketSelected == 17 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_17) {
            bucket17.setAlpha(0.2f);
        } else {
            bucket17.setAlpha(1.0f);
        }
        bucket18.setBackground(indexBucketSelected == 18 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_18) {
            bucket18.setAlpha(0.2f);
        } else {
            bucket18.setAlpha(1.0f);
        }
        bucket19.setBackground(indexBucketSelected == 19 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_19) {
            bucket19.setAlpha(0.2f);
        } else {
            bucket19.setAlpha(1.0f);
        }
        bucket20.setBackground(indexBucketSelected == 20 ? getResources().getDrawable(R.drawable.sfondo_bottone_selezionato) : getDrawable(R.drawable.sfondo_bottone_non_selezionatoe));
        if (isDefault_20) {
            bucket20.setAlpha(0.2f);
        } else {
            bucket20.setAlpha(1.0f);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    private void disableAll() {
        bucket1.setEnabled(false);
        bucket2.setEnabled(false);
        bucket3.setEnabled(false);
        bucket4.setEnabled(false);
        bucket5.setEnabled(false);
        bucket6.setEnabled(false);
        bucket7.setEnabled(false);
        bucket8.setEnabled(false);
        bucket9.setEnabled(false);
        bucket10.setEnabled(false);
        bucket11.setEnabled(false);
        bucket12.setEnabled(false);
        bucket13.setEnabled(false);
        bucket14.setEnabled(false);
        bucket15.setEnabled(false);
        bucket16.setEnabled(false);
        bucket17.setEnabled(false);
        bucket18.setEnabled(false);
        bucket19.setEnabled(false);
        bucket20.setEnabled(false);
        back.setEnabled(false);

    }

    private void enableAll() {
        bucket1.setEnabled(true);
        bucket2.setEnabled(true);
        bucket3.setEnabled(true);
        bucket4.setEnabled(true);
        bucket5.setEnabled(true);
        bucket6.setEnabled(true);
        bucket7.setEnabled(true);
        bucket8.setEnabled(true);
        bucket9.setEnabled(true);
        bucket10.setEnabled(true);
        bucket11.setEnabled(true);
        bucket12.setEnabled(true);
        bucket13.setEnabled(true);
        bucket14.setEnabled(true);
        bucket15.setEnabled(true);
        bucket16.setEnabled(true);
        bucket17.setEnabled(true);
        bucket18.setEnabled(true);
        bucket19.setEnabled(true);
        bucket20.setEnabled(true);
        back.setEnabled(true);

    }

    private void onClick() {

        bucket1.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 1) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos1").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 1);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket2.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 2) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos2").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 2);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket3.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 3) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos3").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 3);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket4.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 4) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos4").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 4);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket5.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 5) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos5").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 5);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket6.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 6) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos6").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 6);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket7.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 7) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos7").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 7);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket8.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 8) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos8").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 8);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket9.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 9) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos9").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 9);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket10.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 10) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos10").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 10);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        ///////////

        bucket11.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 11) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos11").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 11);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket12.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 12) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos12").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 12);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket13.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 13) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos13").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 13);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket14.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 14) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos14").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 14);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket15.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 15) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos15").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 15);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket16.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 16) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos16").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 16);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket17.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 17) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos17").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 17);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket18.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 18) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos18").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 18);
                    startActivity(intent);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket19.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 19) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos19").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 19);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });

        bucket20.setOnClickListener((View v) -> {
            if (unitOfMeasure == 4 || unitOfMeasure == 5) {
                if (!dialogUnitOfMeasure.alertDialog.isShowing()) {
                    dialogUnitOfMeasure.show();
                }
            } else {
                if (indexBucketSelected == 20) {
                    disableAll();
                    Intent intent;
                    if ((MyData.get_String("M" + indexMachineSelected + "_Tilt_MountPos20").equals("0")))
                        intent = new Intent(this, BucketCalib.class);
                    else
                        intent = new Intent(this, BucketCalibTilt.class);
                    intent.putExtra("indexBucket", 20);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                } else {
                    new CustomToast(this, getResources().getString(R.string.toast_select_bucket)).show();
                }
            }
        });


        ///////////

        back.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(goBackIntent);
            finish();
        });


    }


    private void onLongClick() {
        bucket1.setOnLongClickListener((View v) -> {
            MyData.push("M" + indexMachineSelected + "BucketSelected", "1");
            indexBucketSelected = 1;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket2.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "2");
            indexBucketSelected = 2;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket3.setOnLongClickListener((View v) -> {
            MyData.push("M" + indexMachineSelected + "BucketSelected", "3");
            indexBucketSelected = 3;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket4.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "4");
            indexBucketSelected = 4;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket5.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "5");
            indexBucketSelected = 5;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket6.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "6");
            indexBucketSelected = 6;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket7.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "7");
            indexBucketSelected = 7;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket8.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "8");
            indexBucketSelected = 8;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket9.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "9");
            indexBucketSelected = 9;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket10.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "10");
            indexBucketSelected = 10;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        /////


        bucket11.setOnLongClickListener((View v) -> {
            MyData.push("M" + indexMachineSelected + "BucketSelected", "11");
            indexBucketSelected = 11;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket12.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "12");
            indexBucketSelected = 12;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket13.setOnLongClickListener((View v) -> {
            MyData.push("M" + indexMachineSelected + "BucketSelected", "13");
            indexBucketSelected = 13;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket14.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "14");
            indexBucketSelected = 14;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket15.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "15");
            indexBucketSelected = 15;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket16.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "16");
            indexBucketSelected = 16;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket17.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "17");
            indexBucketSelected = 17;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket18.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "18");
            indexBucketSelected = 18;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket19.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "19");
            indexBucketSelected = 19;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });

        bucket20.setOnLongClickListener((View v) -> {

            MyData.push("M" + indexMachineSelected + "BucketSelected", "20");
            indexBucketSelected = 20;
            startService(new Intent(this, UpdateValuesService.class));
            return true;
        });
    }
}
