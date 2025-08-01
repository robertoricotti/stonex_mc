package gui.dialogs_user_settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.stx_dig.R;

import utils.FullscreenActivity;
import utils.LanguageSetter;
import utils.MyData;

public class DialogLanguages {
    Activity activity;
   public Dialog alertDialog;
    ImageView ita, usa, fra, spa, eng, deu,rus,chin,pur,gre,kor,rum,nel;


    boolean flagactivity=false;


    public DialogLanguages(Activity activity) {
        this.activity = activity;
        alertDialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
    }

    public void show() {
        init();
    }

    private void findView() {
        ita = alertDialog.findViewById(R.id.flagIT);
        usa = alertDialog.findViewById(R.id.flagUS);
        fra = alertDialog.findViewById(R.id.flagFR);
        spa = alertDialog.findViewById(R.id.flagESP);
        eng = alertDialog.findViewById(R.id.flagEN);
        deu = alertDialog.findViewById(R.id.flagDEU);
        rus = alertDialog.findViewById(R.id.flagRUS);
        chin = alertDialog.findViewById(R.id.flagCHN);
        pur = alertDialog.findViewById(R.id.flagPUR);
        gre=alertDialog.findViewById(R.id.flagGRE);
        nel=alertDialog.findViewById(R.id.flagNL);
        kor=alertDialog.findViewById(R.id.flagKOR);
        rum=alertDialog.findViewById(R.id.flagRUM);



    }

    @SuppressLint("SetTextI18n")
    private void init() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_languages, null));

        alertDialog.setCancelable(false);
        Window window = alertDialog.getWindow();
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
        alertDialog.getWindow().setLayout(width, height);
        alertDialog.show();
        FullscreenActivity.setFullScreen(alertDialog);
        findView();
        onClick();


    }

    @SuppressLint("DefaultLocale")
    private void onClick() {
        eng.setOnClickListener(view -> {
            MyData.push("language","en_GB");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        usa.setOnClickListener(view -> {
            MyData.push("language","en_US");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        ita.setOnClickListener(view -> {
            MyData.push("language","it");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        nel.setOnClickListener(view -> {
            MyData.push("language","nl");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        deu.setOnClickListener(view -> {
            MyData.push("language","de");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        spa.setOnClickListener(view -> {
            MyData.push("language","es");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });

        fra.setOnClickListener(view -> {
            MyData.push("language","fr");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        rus.setOnClickListener(view -> {
            MyData.push("language","ru");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        chin.setOnClickListener(view -> {
            MyData.push("language","zh");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        pur.setOnClickListener(view -> {
            MyData.push("language","pt");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        gre.setOnClickListener(view -> {
            MyData.push("language","el");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });
        kor.setOnClickListener(view -> {
            MyData.push("language","ko");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));

            restart();
            alertDialog.dismiss();
        });
        rum.setOnClickListener(view -> {
            MyData.push("language","ro");
            LanguageSetter.setLocale(activity,MyData.get_String("language"));
            restart();
            alertDialog.dismiss();
        });



    }
    private void restart(){
        if(!flagactivity) {
            flagactivity=true;
            activity.recreate();
        }
    }

}