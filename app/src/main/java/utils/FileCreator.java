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
                "CANADA__102_W_TO_96_W__NAD83_CSRS__UTM_ZONE_14N__3158.SP",
                "CANADA__108_W_TO_102_W__NAD83_CSRS__UTM_ZONE_13N__2957.SP",
                "CANADA__108_W_TO_102_W__SOUTH_OF_60_N__NAD83_CSRS98__UTM_ZONE_13N__2151.SP",
                "CANADA__114_W_TO_108_W__NAD83_CSRS__UTM_ZONE_12N__2956.SP",
                "CANADA__114_W_TO_108_W__SOUTH_OF_60_N__NAD83_CSRS98__UTM_ZONE_12N__2152.SP",
                "CANADA__120_W_TO_114_W__NAD83_CSRS__UTM_ZONE_11N__2955.SP",
                "CANADA__120_W_TO_114_W__SOUTH_OF_60_N__NAD83_CSRS98__UTM_ZONE_11N__2153.SP",
                "CANADA__126_W_TO_120_W__NAD83_CSRS__UTM_ZONE_10N__3157.SP",
                "CANADA__132_W_TO_126_W__NAD83_CSRS__UTM_ZONE_9N__3156.SP",
                "CANADA__138_W_TO_132_W__NAD83_CSRS__UTM_ZONE_8N__3155.SP",
                "CANADA__144_W_TO_138_W__NAD83_CSRS__UTM_ZONE_7N__3154.SP",
                "CANADA__48_W_TO_42_W__NAD83__UTM_ZONE_23N__26923.SP",
                "CANADA__54_W_TO_48_W__NAD27__UTM_ZONE_22N__26722.SP",
                "CANADA__54_W_TO_48_W__NAD83_CSRS__UTM_ZONE_22N__3761.SP",
                "CANADA__54_W_TO_48_W__NAD83__UTM_ZONE_22N__26922.SP",
                "CANADA__60_W_TO_54_W_AND_NAD27__NAD27__UTM_ZONE_21N__26721.SP",
                "CANADA__60_W_TO_54_W__NAD83_CSRS__UTM_ZONE_21N__2962.SP",
                "CANADA__60_W_TO_54_W__NAD83__UTM_ZONE_21N__26921.SP",
                "CANADA__66_W_TO_60_W__NAD83_CSRS__UTM_ZONE_20N__2961.SP",
                "CANADA__66_W_TO_60_W__SOUTH_OF_60_N__NAD83_CSRS98__UTM_ZONE_20N__2038.SP",
                "CANADA__72_W_TO_66_W__NAD83_CSRS__UTM_ZONE_19N__2960.SP",
                "CANADA__72_W_TO_66_W__SOUTH_OF_62_N__NAD83_CSRS98__UTM_ZONE_19N__2037.SP",
                "CANADA__78_W_TO_72_W__NAD83_CSRS__UTM_ZONE_18N__2959.SP",
                "CANADA__84_W_TO_78_W__NAD83_CSRS__UTM_ZONE_17N__2958.SP",
                "CANADA__90_W_TO_84_W__NAD83_CSRS__UTM_ZONE_16N__3160.SP",
                "CANADA__96_W_TO_90_W__NAD83_CSRS__UTM_ZONE_15N__3159.SP",
                "CANADA__ALBERTA__115_5_W_TO_112_5_W__NAD27__ALBERTA_3TM_REF_MERID_114_W__3772.SP",
                "CANADA__ALBERTA__115_5_W_TO_112_5_W__NAD83_CSRS__ALBERTA_3TM_REF_MERID_114_W__3780.SP",
                "CANADA__ALBERTA__115_5_W_TO_112_5_W__NAD83__ALBERTA_3TM_REF_MERID_114_W__3776.SP",
                "CANADA__ALBERTA__118_5_W_TO_115_5_W__NAD27__ALBERTA_3TM_REF_MERID_117_W__3773.SP",
                "CANADA__ALBERTA__118_5_W_TO_115_5_W__NAD83_CSRS__ALBERTA_3TM_REF_MERID_117_W__3781.SP",
                "CANADA__ALBERTA__118_5_W_TO_115_5_W__NAD83__ALBERTA_3TM_REF_MERID_117_W__3777.SP",
                "CANADA__ALBERTA__EAST_OF_112_5_W__NAD27__ALBERTA_3TM_REF_MERID_111_W__3771.SP",
                "CANADA__ALBERTA__EAST_OF_112_5_W__NAD83_CSRS__ALBERTA_3TM_REF_MERID_111_W__3779.SP",
                "CANADA__ALBERTA__EAST_OF_112_5_W__NAD83__ALBERTA_3TM_REF_MERID_111_W__3775.SP",
                "CANADA__YUKON__NAD83__YUKON_ALBERS__3578.SP",
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
