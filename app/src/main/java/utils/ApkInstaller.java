package utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import gui.dialogs_and_toast.CustomToast;

public class ApkInstaller {
    private Context context;

    public ApkInstaller(Context context) {
        this.context = context;
    }

    public void installApkFromAssets(String apkFileName) {
        try {
            // Copia l'APK dagli assets in una cartella temporanea
            InputStream in = context.getAssets().open(apkFileName);
            File outFile = new File(context.getExternalFilesDir(null), apkFileName);
            FileOutputStream out = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();

            // Avvia il processo di installazione
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri apkUri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Usa FileProvider per Android 7.0+
                apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", outFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                apkUri = Uri.fromFile(outFile);
            }

            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(context, "Installation Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}



