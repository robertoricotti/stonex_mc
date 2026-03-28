package gui.boot_and_choose;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;

public class ApkDownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            return;
        }

        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (downloadId == -1) {
            return;
        }

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm == null) {
            return;
        }

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = null;
        try {
            cursor = dm.query(query);
            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }

            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

            if (statusIndex == -1 || localUriIndex == -1) {
                return;
            }

            int status = cursor.getInt(statusIndex);
            String localUri = cursor.getString(localUriIndex);

            if (status != DownloadManager.STATUS_SUCCESSFUL || localUri == null) {
                return;
            }

            Uri uri = Uri.parse(localUri);
            File apkFile;

            if ("file".equalsIgnoreCase(uri.getScheme())) {
                apkFile = new File(uri.getPath());
            } else {
                // fallback (raro)
                apkFile = new File(localUri);
            }

            if (!apkFile.exists()) {
                return;
            }

            if (ApkInstaller.canInstallUnknownApps(context)) {
                ApkInstaller.installApk(context, apkFile);
            } else {
                // opzionale: apri settings (ma serve Activity, quindi meglio gestirlo prima)
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}