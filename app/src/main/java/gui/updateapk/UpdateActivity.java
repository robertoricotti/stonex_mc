package gui.updateapk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stx_dig.BuildConfig;
import com.example.stx_dig.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateActivity extends AppCompatActivity {

    private static final String TAG = "UpdateActivity";
    private static final String APK_URL = "https://github.com/manuelstonex/stx_dig/tree/main/app%20release/stx_dig_"+ BuildConfig.VERSION_NAME+".apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        new DownloadUpdate().execute(APK_URL);

    }

    private class DownloadUpdate extends AsyncTask<String, Void, File> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(UpdateActivity.this);
            progressDialog.setMessage("Downloading update...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected File doInBackground(String... urls) {
            String fileUrl = urls[0];
            File file = null;
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                InputStream inputStream = connection.getInputStream();
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "app-update.apk");
                FileOutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "Error downloading update", e);
            }
            return file;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            progressDialog.dismiss();

            if (file != null) {
                Toast.makeText(UpdateActivity.this, "Download complete", Toast.LENGTH_SHORT).show();
                installApk(file);
            } else {
                Toast.makeText(UpdateActivity.this, "Failed to download update", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void installApk(File file) {
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}