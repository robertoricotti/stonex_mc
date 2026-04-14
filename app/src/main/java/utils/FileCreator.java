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
                "UKRAINE__22_5_E_TO_25_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_CM_24E__5578.SP",
                "UKRAINE__22_5_E_TO_25_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_ZONE_8__5571.SP",
                "UKRAINE__22_5_E_TO_25_5_E__UCS_2000__UKRAINE_TM_ZONE_8__6382.SP",
                "UKRAINE__24_E_TO_30_E__UCS_2000__GAUSS_KRUGER_CM_27E__5567.SP",
                "UKRAINE__24_E_TO_30_E__UCS_2000__GAUSS_KRUGER_ZONE_5__5563.SP",
                "UKRAINE__25_5_E_TO_28_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_CM_27E__5579.SP",
                "UKRAINE__25_5_E_TO_28_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_ZONE_9__5572.SP",
                "UKRAINE__25_5_E_TO_28_5_E__UCS_2000__UKRAINE_TM_ZONE_9__6383.SP",
                "UKRAINE__28_5_E_TO_31_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_CM_30E__5580.SP",
                "UKRAINE__28_5_E_TO_31_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_ZONE_10__5573.SP",
                "UKRAINE__28_5_E_TO_31_5_E__UCS_2000__UKRAINE_TM_ZONE_10__6384.SP",
                "UKRAINE__30_E_TO_36_E__UCS_2000__GAUSS_KRUGER_CM_33E__5568.SP",
                "UKRAINE__30_E_TO_36_E__UCS_2000__GAUSS_KRUGER_ZONE_6__5564.SP",
                "UKRAINE__31_5_E_TO_34_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_CM_33E__5581.SP",
                "UKRAINE__31_5_E_TO_34_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_ZONE_11__5574.SP",
                "UKRAINE__31_5_E_TO_34_5_E__UCS_2000__UKRAINE_TM_ZONE_11__6385.SP",
                "UKRAINE__34_5_E_TO_37_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_CM_36E__5582.SP",
                "UKRAINE__34_5_E_TO_37_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_ZONE_12__5575.SP",
                "UKRAINE__34_5_E_TO_37_5_E__UCS_2000__UKRAINE_TM_ZONE_12__6386.SP",
                "UKRAINE__EAST_OF_36_E__UCS_2000__GAUSS_KRUGER_CM_39E__5569.SP",
                "UKRAINE__EAST_OF_36_E__UCS_2000__GAUSS_KRUGER_ZONE_7__5565.SP",
                "UKRAINE__EAST_OF_37_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_CM_39E__5583.SP",
                "UKRAINE__EAST_OF_37_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_ZONE_13__5576.SP",
                "UKRAINE__EAST_OF_37_5_E__UCS_2000__UKRAINE_TM_ZONE_13__6387.SP",
                "UKRAINE__UCS_2000__5558.SP",
                "UKRAINE__WEST_OF_22_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_CM_21E__5577.SP",
                "UKRAINE__WEST_OF_22_5_E__UCS_2000__3_DEGREE_GAUSS_KRUGER_ZONE_7__5570.SP",
                "UKRAINE__WEST_OF_22_5_E__UCS_2000__UKRAINE_TM_ZONE_7__6381.SP",
                "UKRAINE__WEST_OF_24_E__UCS_2000__GAUSS_KRUGER_CM_21E__5566.SP",
                "UKRAINE__WEST_OF_24_E__UCS_2000__GAUSS_KRUGER_ZONE_4__5562.SP",
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
