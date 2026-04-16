package gui.dialogs_user_settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.stx_dig.R;

import utils.FullscreenActivity;
import utils.LanguageSetter;
import utils.MyData;

public class DialogLanguages {
    Activity activity;
    public Dialog dialog;
    ImageView ita, usa, fra, spa, eng, deu, rus, chin, pur, gre, kor, rum, nel, jpn, czh,
    nor,swe,den,ice,fin;


    boolean flagactivity = false;


    public DialogLanguages(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        init();
    }

    private void findView() {
        czh = dialog.findViewById(R.id.flagCZH);
        jpn = dialog.findViewById(R.id.flagJPN);
        ita = dialog.findViewById(R.id.flagIT);
        usa = dialog.findViewById(R.id.flagUS);
        fra = dialog.findViewById(R.id.flagFR);
        spa = dialog.findViewById(R.id.flagESP);
        eng = dialog.findViewById(R.id.flagEN);
        deu = dialog.findViewById(R.id.flagDEU);
        rus = dialog.findViewById(R.id.flagRUS);
        chin = dialog.findViewById(R.id.flagCHN);
        pur = dialog.findViewById(R.id.flagPUR);
        gre = dialog.findViewById(R.id.flagGRE);
        nel = dialog.findViewById(R.id.flagNL);
        kor = dialog.findViewById(R.id.flagKOR);
        rum = dialog.findViewById(R.id.flagRUM);
        nor=dialog.findViewById(R.id.flagNOR);
        swe=dialog.findViewById(R.id.flagSWE);
        den=dialog.findViewById(R.id.flagDEN);
        ice=dialog.findViewById(R.id.flagICE);
        fin=dialog.findViewById(R.id.flagFIN);



    }

    @SuppressLint("SetTextI18n")
    private void init() {
        dialog.create();
        dialog.setContentView(R.layout.dialog_languages_2);
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
        int width = (int) (displayMetrics.widthPixels * 0.75);
        int height = (int) (displayMetrics.heightPixels * 0.75);
        dialog.getWindow().setLayout(width, height);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        FullscreenActivity.setFullScreen(dialog);
        findView();
        onClick();


    }

    @SuppressLint("DefaultLocale")
    private void onClick() {
        eng.setOnClickListener(view -> {
            MyData.push("language", "en_GB");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });

        jpn.setOnClickListener(view -> {
            MyData.push("language", "ja");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        czh.setOnClickListener(view -> {
            MyData.push("language", "cs");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        usa.setOnClickListener(view -> {
            MyData.push("language", "en_US");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        ita.setOnClickListener(view -> {
            MyData.push("language", "it");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        nel.setOnClickListener(view -> {
            MyData.push("language", "nl");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        deu.setOnClickListener(view -> {
            MyData.push("language", "de");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        spa.setOnClickListener(view -> {
            MyData.push("language", "es");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });

        fra.setOnClickListener(view -> {
            MyData.push("language", "fr");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        rus.setOnClickListener(view -> {
            MyData.push("language", "ru");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        chin.setOnClickListener(view -> {
            MyData.push("language", "zh");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        pur.setOnClickListener(view -> {
            MyData.push("language", "pt");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        gre.setOnClickListener(view -> {
            MyData.push("language", "el");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        kor.setOnClickListener(view -> {
            MyData.push("language", "ko");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });
        rum.setOnClickListener(view -> {
            MyData.push("language", "ro");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });

        nor.setOnClickListener(view -> {
            MyData.push("language", "nb");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });

        swe.setOnClickListener(view -> {
            MyData.push("language", "sv");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });

        den.setOnClickListener(view -> {
            MyData.push("language", "da");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });

        ice.setOnClickListener(view -> {
            MyData.push("language", "is");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });

        fin.setOnClickListener(view -> {
            MyData.push("language", "fi");
            LanguageSetter.setLocale(activity, MyData.get_String("language"));
            restart();
            dialog.dismiss();
        });


    }

    private void restart() {
        if (!flagactivity) {
            flagactivity = true;
            activity.recreate();
        }
    }

}