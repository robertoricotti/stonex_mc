package utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageSetter {

    public static void setLocale(Activity activity, String lang) {

        if (lang == null || lang.trim().isEmpty()) {
            lang = Locale.getDefault().getLanguage();
        }

        String str = "en";

        switch (lang) {
            case "en_GB":
            case "en_US":
                str = "en";
                break;
            case "it":
                str = "it";
                break;
            case "de":
                str = "de";
                break;
            case "es":
                str = "es";
                break;
            case "fr":
                str = "fr";
                break;
            case "ru":
                str = "ru";
                break;
            case "zh":
                str = "zh";
                break;
            case "pt":
                str = "pt";
                break;
            case "ro":
                str = "ro";
                break;
            case "ko":
                str = "ko";
                break;
            case "ja":
                str = "ja";
                break;
            case "el":
                str = "el";
                break;
            case "nl":
                str = "nl";
                break;
            case "cs":
                str = "cs";
                break;
        }

        Locale locale = new Locale(str);
        Locale.setDefault(locale);

        Resources resources = activity.getBaseContext().getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

}
