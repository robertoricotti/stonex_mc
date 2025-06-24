package utils;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class FileCreator {

    public static void generateSPFilesFromCases(Context context) {
        String[] metodi = {
                "JAPAN__ZONE_III__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_III__2445.SP",
                "JAPAN__ZONE_III__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_III__6671.SP",
                "JAPAN__ZONE_III__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_III__30163.SP",
                "JAPAN__ZONE_II__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_II__2444.SP",
                "JAPAN__ZONE_II__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_II__6670.SP",
                "JAPAN__ZONE_II__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_II__30162.SP",
                "JAPAN__ZONE_IV__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_IV__2446.SP",
                "JAPAN__ZONE_IV__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_IV__6672.SP",
                "JAPAN__ZONE_IV__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_IV__30164.SP",
                "JAPAN__ZONE_IX__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_IX__2451.SP",
                "JAPAN__ZONE_IX__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_IX__6677.SP",
                "JAPAN__ZONE_IX__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_IX__30169.SP",
                "JAPAN__ZONE_I__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_I__2443.SP",
                "JAPAN__ZONE_I__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_I__6669.SP",
                "JAPAN__ZONE_I__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_I__30161.SP",
                "JAPAN__ZONE_VIII__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_VIII__2450.SP",
                "JAPAN__ZONE_VIII__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_VIII__6676.SP",
                "JAPAN__ZONE_VIII__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_VIII__30168.SP",
                "JAPAN__ZONE_VII__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_VII__2449.SP",
                "JAPAN__ZONE_VII__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_VII__6675.SP",
                "JAPAN__ZONE_VII__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_VII__30167.SP",
                "JAPAN__ZONE_VI__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_VI__2448.SP",
                "JAPAN__ZONE_VI__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_VI__6674.SP",
                "JAPAN__ZONE_VI__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_VI__30166.SP",
                "JAPAN__ZONE_V__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_V__2447.SP",
                "JAPAN__ZONE_V__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_V__6673.SP",
                "JAPAN__ZONE_V__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_V__30165.SP",
                "JAPAN__ZONE_XIII__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_XIII__2455.SP",
                "JAPAN__ZONE_XIII__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_XIII__6681.SP",
                "JAPAN__ZONE_XIII__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_XIII__30173.SP",
                "JAPAN__ZONE_XII__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_XII__2454.SP",
                "JAPAN__ZONE_XII__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_XII__6680.SP",
                "JAPAN__ZONE_XII__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_XII__30172.SP",
                "JAPAN__ZONE_XIV__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_XIV__2456.SP",
                "JAPAN__ZONE_XIV__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_XIV__6682.SP",
                "JAPAN__ZONE_XIV__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_XIV__30174.SP",
                "JAPAN__ZONE_XI__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_XI__2453.SP",
                "JAPAN__ZONE_XI__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_XI__6679.SP",
                "JAPAN__ZONE_XI__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_XI__30171.SP",
                "JAPAN__ZONE_XVIII__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_XVIII__2460.SP",
                "JAPAN__ZONE_XVIII__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_XVIII__6686.SP",
                "JAPAN__ZONE_XVIII__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_XVIII__30178.SP",
                "JAPAN__ZONE_XVII__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_XVII__2459.SP",
                "JAPAN__ZONE_XVII__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_XVII__6685.SP",
                "JAPAN__ZONE_XVII__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_XVII__30177.SP",
                "JAPAN__ZONE_XVI__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_XVI__2458.SP",
                "JAPAN__ZONE_XVI__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_XVI__6684.SP",
                "JAPAN__ZONE_XVI__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_XVI__30176.SP",
                "JAPAN__ZONE_XV__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_XV__2457.SP",
                "JAPAN__ZONE_XV__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_XV__6683.SP",
                "JAPAN__ZONE_XV__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_XV__30175.SP",
                "JAPAN__ZONE_X__JGD2000__JAPAN_PLANE_RECTANGULAR_CS_X__2452.SP",
                "JAPAN__ZONE_X__JGD2011__JAPAN_PLANE_RECTANGULAR_CS_X__6678.SP",
                "JAPAN__ZONE_X__TOKYO__JAPAN_PLANE_RECTANGULAR_CS_X__30170.SP",
        };

        for (String fileName : metodi) {
            try {
                createEmptySPFile(context, "mySP", fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createEmptySPFile(Context context, String subfolder, String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + subfolder);
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream ignored = context.getContentResolver().openOutputStream(uri)) {
                    // non scrivere nulla = file vuoto
                }
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                context.getContentResolver().update(uri, values, null, null);
            }
        } else {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File mySpDir = new File(downloadDir, subfolder);
            if (!mySpDir.exists()) mySpDir.mkdirs();
            File file = new File(mySpDir, fileName);
            file.createNewFile();
        }
    }
}
