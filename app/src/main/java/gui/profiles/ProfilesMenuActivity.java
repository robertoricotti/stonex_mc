package gui.profiles;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.stx_dig.R;

import gui.boot_and_choose.Activity_Home_Page;
import gui.dialogs_and_toast.CustomToast;
import gui.digging_excavator.DiggingProfile;
import services.UpdateValuesService;
import utils.MyData;

public class ProfilesMenuActivity extends AppCompatActivity {
    ProgressBar progressBar;
    ImageButton profile1, profile2, profile3, profile4, profile5, profile6, profileDisabled;
    TextView profileName1, profileName2, profileName3, profileName4, profileName5, profileName6;
    ImageView back, toDig;
    private static boolean isDef_1, isDef_2, isDef_3, isDef_4, isDef_5, isDef_6;

    int indexProfileSelected = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);


        findView();
        init();
        onClick();
        onLongClick();
        updateUI();


    }

    private void findView() {

        back = findViewById(R.id.backHome);
        toDig = findViewById(R.id.pair);
        profile1 = findViewById(R.id.imageProfile1);
        profile2 = findViewById(R.id.imageProfile2);
        profile3 = findViewById(R.id.imageProfile3);
        profile4 = findViewById(R.id.imageProfile4);
        profile5 = findViewById(R.id.imageProfile5);
        profile6 = findViewById(R.id.imageProfile6);
        profileDisabled = findViewById(R.id.imageProfileDisabled);

        profileName1 = findViewById(R.id.profile1Name);
        profileName2 = findViewById(R.id.profile2Name);
        profileName3 = findViewById(R.id.profile3Name);
        profileName4 = findViewById(R.id.profile4Name);
        profileName5 = findViewById(R.id.profile5Name);
        profileName6 = findViewById(R.id.profile6Name);
        progressBar=findViewById(R.id.progressBar);
    }

    private void init() {
        progressBar.setVisibility(View.INVISIBLE);


        indexProfileSelected = MyData.get_Int("ProfileSelected");

        profileName1.setText(MyData.get_String("Profile1_name"));
        profileName2.setText(MyData.get_String("Profile2_name"));
        profileName3.setText(MyData.get_String("Profile3_name"));
        profileName4.setText(MyData.get_String("Profile4_name"));
        profileName5.setText(MyData.get_String("Profile5_name"));
        profileName6.setText(MyData.get_String("Profile6_name"));
        isDef_1 = MyData.get_String("Profile" + 1 + "_punti").equals("");
        isDef_2 =MyData.get_String("Profile" + 2 + "_punti").equals("");
        isDef_3 = MyData.get_String("Profile" + 3 + "_punti").equals("");
        isDef_4 = MyData.get_String("Profile" + 4 + "_punti").equals("");
        isDef_5 = MyData.get_String("Profile" + 5 + "_punti").equals("");
        isDef_6 = MyData.get_String("Profile" + 6 + "_punti").equals("");

    }

    public void updateUI() {


        profile1.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), indexProfileSelected == 1 ? R.color.orange : R.color.light_gray));
        if (isDef_1) {
            profile1.setAlpha(0.2f);

        } else {
            profile1.setAlpha(1.0f);

        }
        profile2.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), indexProfileSelected == 2 ? R.color.orange : R.color.light_gray));
        if (isDef_2) {
            profile2.setAlpha(0.2f);
        } else {
            profile2.setAlpha(1.0f);
        }
        profile3.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), indexProfileSelected == 3 ? R.color.orange : R.color.light_gray));
        if (isDef_3) {
            profile3.setAlpha(0.2f);
        } else {
            profile3.setAlpha(1.0f);
        }
        profile4.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), indexProfileSelected == 4 ? R.color.orange : R.color.light_gray));
        if (isDef_4) {
            profile4.setAlpha(0.2f);
        } else {
            profile4.setAlpha(1.0f);
        }
        profile5.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), indexProfileSelected == 5 ? R.color.orange : R.color.light_gray));
        if (isDef_5) {
            profile5.setAlpha(0.2f);
        } else {
            profile5.setAlpha(1.0f);
        }
        profile6.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), indexProfileSelected == 6 ? R.color.orange : R.color.light_gray));
        if (isDef_6) {
            profile6.setAlpha(0.2f);
        } else {
            profile6.setAlpha(1.0f);
        }
        if (isDef_1 && isDef_2 && isDef_3 && isDef_4 && isDef_5 && isDef_6) {
            MyData.push("ProfileSelected", "0");
            indexProfileSelected = 0;

        }
        if (indexProfileSelected == 1 && isDef_1) {
            new CustomToast(ProfilesMenuActivity.this, "SELECT A VALID PROFILE").show();

            MyData.push("ProfileSelected", "0");
            indexProfileSelected = 0;

        } else if (indexProfileSelected == 2 && isDef_2) {
            new CustomToast(ProfilesMenuActivity.this, "SELECT A VALID PROFILE").show();
            MyData.push("ProfileSelected", "0");
            indexProfileSelected = 0;

        } else if (indexProfileSelected == 3 && isDef_3) {
            new CustomToast(ProfilesMenuActivity.this, "SELECT A VALID PROFILE").show();
            MyData.push("ProfileSelected", "0");

        } else if (indexProfileSelected == 4 && isDef_4) {
            new CustomToast(ProfilesMenuActivity.this, "SELECT A VALID PROFILE").show();
            MyData.push("ProfileSelected", "0");
            indexProfileSelected = 0;

        } else if (indexProfileSelected == 5 && isDef_5) {
            new CustomToast(ProfilesMenuActivity.this, "SELECT A VALID PROFILE").show();
            MyData.push("ProfileSelected", "0");
            indexProfileSelected = 0;

        } else if (indexProfileSelected == 6 && isDef_6) {
            new CustomToast(ProfilesMenuActivity.this, "SELECT A VALID PROFILE").show();
            MyData.push("ProfileSelected", "0");
            indexProfileSelected = 0;

        }

        profileDisabled.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), indexProfileSelected == 0 ? R.color.orange : R.color.light_gray));
        profileDisabled.setAlpha(indexProfileSelected == 0 ? 1.0f : 0.2f);

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    private void disableAll() {
        profile1.setEnabled(false);
        profile2.setEnabled(false);
        profile3.setEnabled(false);
        profile4.setEnabled(false);
        profile5.setEnabled(false);
        profile6.setEnabled(false);
        back.setEnabled(false);
        toDig.setEnabled(false);
    }

    private void onClick() {
        profile1.setOnClickListener((View v) -> {
            disableAll();
            Intent intent = new Intent(this, ProfileCalibAuto.class);
            intent.putExtra("indexProfile", 1);
            startActivity(intent);
            finish();
        });

        profile2.setOnClickListener((View v) -> {
            disableAll();
            Intent intent = new Intent(this, ProfileCalibAuto.class);
            intent.putExtra("indexProfile", 2);
            startActivity(intent);
            finish();
        });

        profile3.setOnClickListener((View v) -> {
            disableAll();
            Intent intent = new Intent(this, ProfileCalibAuto.class);
            intent.putExtra("indexProfile", 3);
            startActivity(intent);
            finish();
        });

        profile4.setOnClickListener((View v) -> {
            disableAll();
            Intent intent = new Intent(this, ProfileCalibAuto.class);
            intent.putExtra("indexProfile", 4);
            startActivity(intent);
            finish();
        });

        profile5.setOnClickListener((View v) -> {
            disableAll();
            Intent intent = new Intent(this, ProfileCalibAuto.class);
            intent.putExtra("indexProfile", 5);
            startActivity(intent);
            finish();
        });

        profile6.setOnClickListener((View v) -> {
            disableAll();
            Intent intent = new Intent(this, ProfileCalibAuto.class);
            intent.putExtra("indexProfile", 6);
            startActivity(intent);
            finish();
        });

        back.setOnClickListener((View v) -> {
            disableAll();
            startService(new Intent(this, UpdateValuesService.class));
            startActivity(new Intent(this, Activity_Home_Page.class));
            finish();
        });

        toDig.setOnClickListener((View v) -> {

            startService(new Intent(this, UpdateValuesService.class));
            int profile = MyData.get_Int("ProfileSelected");
            if (profile == 0) {
                new CustomToast(this,"No Profile Selected").show_error();
            } else {
                disableAll();
                startActivity(new Intent(this, DiggingProfile.class));
                finish();
            }


        });
    }

    private void onLongClick() {
        profile1.setOnLongClickListener((View v) -> {
            if (!MyData.get_String("Profile" + 1 + "_punti").equals("")) {
                MyData.push("ProfileSelected", "1");
                indexProfileSelected = 1;
            } else {
                new CustomToast(ProfilesMenuActivity.this, "Not Init!").show();
            }

            return true;
        });

        profile2.setOnLongClickListener((View v) -> {
            if (!MyData.get_String("Profile" + 2 + "_punti").equals("")) {
                MyData.push("ProfileSelected", "2");
                indexProfileSelected = 2;
            } else {
                new CustomToast(ProfilesMenuActivity.this, "Not Init!").show();

            }
            return true;
        });

        profile3.setOnLongClickListener((View v) -> {
            if (!MyData.get_String("Profile" + 3 + "_punti").equals("")) {
                MyData.push("ProfileSelected", "3");
                indexProfileSelected = 3;
            } else {
                new CustomToast(ProfilesMenuActivity.this, "Not Init!").show();
            }
            return true;
        });

        profile4.setOnLongClickListener((View v) -> {
            if (!MyData.get_String("Profile" + 4 + "_punti").equals("")) {
                MyData.push("ProfileSelected", "4");
                indexProfileSelected = 4;
            } else {
                new CustomToast(ProfilesMenuActivity.this, "Not Init!").show();
            }
            return true;
        });

        profile5.setOnLongClickListener((View v) -> {
            if (!MyData.get_String("Profile" + 5 + "_punti").equals("")) {
                MyData.push("ProfileSelected", "5");
                indexProfileSelected = 5;
            } else {
                new CustomToast(ProfilesMenuActivity.this, "Not Init!").show();
            }

            return true;
        });

        profile6.setOnLongClickListener((View v) -> {
            if (!MyData.get_String("Profile" + 6 + "_punti").equals("")) {
                MyData.push("ProfileSelected", "6");
                indexProfileSelected = 6;
            } else {
                new CustomToast(ProfilesMenuActivity.this, "Not Init!").show();
            }
            return true;
        });

        profileDisabled.setOnLongClickListener((View v) -> {
            MyData.push("ProfileSelected", "0");
            indexProfileSelected = 0;
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
