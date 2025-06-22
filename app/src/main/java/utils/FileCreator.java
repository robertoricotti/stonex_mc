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
        String[] fileNames = {
                "USA__ALASKA__144_W_TO_141_W__NAD83_2011__ALASKA_ZONE_2__6395.SP",
                "USA__ALASKA__148_W_TO_144_W__NAD83_2011__ALASKA_ZONE_3__6396.SP",
                "USA__ALASKA__152_W_TO_148_W__NAD83_2011__ALASKA_ZONE_4__6397.SP",
                "USA__ALASKA__160_W_TO_156_W__NAD83_2011__ALASKA_ZONE_6__6399.SP",
                "USA__ALASKA__156_W_TO_152_W__NAD83_2011__ALASKA_ZONE_5__6398.SP",
                "USA__ALASKA__PANHANDLE__NAD83_2011__ALASKA_ZONE_1__6394.SP",
                "USA__ALASKA__164_W_TO_160_W__NAD83_2011__ALASKA_ZONE_7__6400.SP",
                "USA__ALASKA__NORTH_OF_54_5_N__168_W_TO_164_W__NAD83_2011__ALASKA_ZONE_8__6401.SP",
                "USA__ALASKA__NORTH_OF_54_5_N__WEST_OF_168_W__NAD83_2011__ALASKA_ZONE_9__6402.SP",
                "USA__ALASKA__ALEUTIAN_ISLANDS__NAD83_2011__ALASKA_ZONE_10__6403.SP",
                "USA__ALABAMA__SPCS__E__NAD83_HARN__ALABAMA_EAST__2759.SP",
                "USA__ALABAMA__SPCS__W__NAD83_HARN__ALABAMA_WEST__2760.SP",
                "USA__ARIZONA__SPCS__E__NAD83__ARIZONA_EAST__26948.SP",
                "USA__ARIZONA__SPCS__C__NAD83__ARIZONA_CENTRAL__26949.SP",
                "USA__ARIZONA__SPCS__W__NAD83__ARIZONA_WEST__26950.SP",
                "USA__ARKANSAS__SPCS__N__NAD83__ARKANSAS_NORTH__26951.SP",
                "USA__ARKANSAS__SPCS__S__NAD83__ARKANSAS_SOUTH__26952.SP",
                "USA__CALIFORNIA__SPCS__1__NAD83__CALIFORNIA_ZONE_1__26941.SP",
                "USA__CALIFORNIA__SPCS__2__NAD83__CALIFORNIA_ZONE_2__26942.SP",
                "USA__CALIFORNIA__SPCS__3__NAD83__CALIFORNIA_ZONE_3__26943.SP",
                "USA__CALIFORNIA__SPCS__4__NAD83__CALIFORNIA_ZONE_4__26944.SP",
                "USA__CALIFORNIA__SPCS83__5__NAD83__CALIFORNIA_ZONE_5__26945.SP",
                "USA__CALIFORNIA__SPCS__6__NAD83__CALIFORNIA_ZONE_6__26946.SP",
                "USA__TEXAS__SPCS__N__NAD83__TEXAS_NORTH__32137.SP",
                "USA__TEXAS__SPCS__NC__NAD83__TEXAS_NORTH_CENTRAL__32138.SP",
                "USA__TEXAS__SPCS__C__NAD83__TEXAS_CENTRAL__32139.SP",
                "USA__TEXAS__SPCS83__SC__NAD83__TEXAS_SOUTH_CENTRAL__32140.SP",
                "USA__TEXAS__SPCS83__S__NAD83__TEXAS_SOUTH__32141.SP",
                "USA__WISCONSIN__SPCS__N__NAD83__WISCONSIN_NORTH__32152.SP",
                "USA__WISCONSIN__SPCS__C__NAD83__WISCONSIN_CENTRAL__32153.SP",
                "USA__WISCONSIN__SPCS__S__NAD83__WISCONSIN_SOUTH__32154.SP",
                "USA__WYOMING__SPCS__E__NAD83__WYOMING_EAST__32155.SP",
                "USA__WYOMING__SPCS__EC__NAD83__WYOMING_EAST_CENTRAL__32156.SP",
                "USA__WYOMING__SPCS__WC__NAD83__WYOMING_WEST_CENTRAL__32157.SP",
                "USA__WYOMING__SPCS__W__NAD83__WYOMING_WEST__32158.SP",
                "USA__UTAH__SPCS__N__NAD83__UTAH_NORTH__32142.SP",
                "USA__UTAH__SPCS__C__NAD83__UTAH_CENTRAL__32143.SP",
                "USA__UTAH__SPCS__S__NAD83__UTAH_SOUTH__32144.SP",
                "USA__VERMONT__NAD83__VERMONT__32145.SP",
                "USA__VIRGINIA__SPCS__N__NAD83__VIRGINIA_NORTH__32146.SP",
                "USA__VIRGINIA__SPCS__S__NAD83__VIRGINIA_SOUTH__32147.SP",
                "USA__WASHINGTON__SPCS83__N__NAD83__WASHINGTON_NORTH__32148.SP",
                "USA__WASHINGTON__SPCS83__S__NAD83__WASHINGTON_SOUTH__32149.SP",
                "USA__WEST_VIRGINIA__SPCS__N__NAD83__WEST_VIRGINIA_NORTH__32150.SP",
                "USA__WEST_VIRGINIA__SPCS__S__NAD83__WEST_VIRGINIA_SOUTH__32151.SP",
                "USA__COLORADO__SPCS__C__NAD83__COLORADO_CENTRAL__26954.SP",
                "USA__COLORADO__SPCS__S__NAD83__COLORADO_SOUTH__26955.SP",
                "USA__CONNECTICUT__NAD83__CONNECTICUT__26956.SP",
                "USA__DELAWARE__NAD83__DELAWARE__26957.SP",
                "USA__FLORIDA__SPCS__E__NAD83__FLORIDA_EAST__26958.SP",
                "USA__FLORIDA__SPCS__N__NAD83__FLORIDA_NORTH__26960.SP",
                "USA__HAWAII__ISLAND_OF_HAWAII__ONSHORE__NAD83__HAWAII_ZONE_1__26961.SP",
                "USA__HAWAII__MAUI__KAHOOLAWE__LANAI__MOLOKAI__ONSHORE__NAD83__HAWAII_ZONE_2__26962.SP",
                "USA__HAWAII__OAHU__ONSHORE__NAD83__HAWAII_ZONE_3__26963.SP",
                "USA__HAWAII__KAUAI__ONSHORE__NAD83__HAWAII_ZONE_4__26964.SP",
                "USA__HAWAII__NIIHAU__ONSHORE__NAD83__HAWAII_ZONE_5__26965.SP",
                "USA__IDAHO__SPCS__E__NAD83__IDAHO_EAST__26968.SP",
                "USA__IDAHO__SPCS__C__NAD83__IDAHO_CENTRAL__26969.SP",
                "USA__IDAHO__SPCS__W__NAD83__IDAHO_WEST__26970.SP",
                "USA__ILLINOIS__SPCS__E__NAD83__ILLINOIS_EAST__26971.SP",
                "USA__ILLINOIS__SPCS__W__NAD83__ILLINOIS_WEST__26972.SP",
                "USA__INDIANA__SPCS__E__NAD83__INDIANA_EAST__26973.SP",
                "USA__INDIANA__SPCS__W__NAD83__INDIANA_WEST__26974.SP",
                "USA__IOWA__SPCS__N__NAD83__IOWA_NORTH__26975.SP",
                "USA__IOWA__SPCS__S__NAD83__IOWA_SOUTH__26976.SP",
                "USA__KANSAS__SPCS__N__NAD83__KANSAS_NORTH__26977.SP",
                "USA__KANSAS__SPCS__S__NAD83__KANSAS_SOUTH__26978.SP",
                "USA__KENTUCKY__SPCS__N__NAD83__KENTUCKY_NORTH__2205.SP",
                "USA__KENTUCKY__SPCS__S__NAD83__KENTUCKY_SOUTH__26980.SP",
                "USA__LOUISIANA__SPCS__N__NAD83__LOUISIANA_NORTH__26981.SP",
                "USA__LOUISIANA__NAD83__LOUISIANA_OFFSHORE__32199.SP",
                "USA__LOUISIANA__SPCS83__S__NAD83__LOUISIANA_SOUTH__26982.SP",
                "USA__MAINE__SPCS__E__NAD83__MAINE_EAST__26983.SP",
                "USA__MAINE__SPCS__W__NAD83__MAINE_WEST__26984.SP",
                "USA__MARYLAND__NAD83__MARYLAND__26985.SP",
                "USA__MASSACHUSETTS__SPCS__MAINLAND__NAD83__MASSACHUSETTS_MAINLAND__26986.SP",
                "USA__MASSACHUSETTS__SPCS__ISLANDS__NAD83__MASSACHUSETTS_ISLAND__26987.SP",
                "USA__MICHIGAN__SPCS__N__NAD83__MICHIGAN_NORTH__26988.SP",
                "USA__MICHIGAN__SPCS__C__NAD83__MICHIGAN_CENTRAL__26989.SP",
                "USA__MICHIGAN__SPCS__S__NAD83__MICHIGAN_SOUTH__26990.SP",
                "USA__MINNESOTA__SPCS__N__NAD83__MINNESOTA_NORTH__26991.SP",
                "USA__MINNESOTA__SPCS__C__NAD83__MINNESOTA_CENTRAL__26992.SP",
                "USA__MINNESOTA__SPCS__S__NAD83__MINNESOTA_SOUTH__26993.SP",
                "USA__MISSISSIPPI__SPCS__E__NAD83__MISSISSIPPI_EAST__26994.SP",
                "USA__MISSISSIPPI__SPCS__W__NAD83__MISSISSIPPI_WEST__26995.SP",
                "USA__MISSOURI__SPCS__E__NAD83__MISSOURI_EAST__26996.SP",
                "USA__MISSOURI__SPCS__C__NAD83__MISSOURI_CENTRAL__26997.SP",
                "USA__MISSOURI__SPCS__W__NAD83__MISSOURI_WEST__26998.SP",
                "USA__MONTANA__NAD83__MONTANA__32100.SP",
                "USA__NEBRASKA__NAD83__NEBRASKA__32104.SP",
                "USA__NEVADA__SPCS__W__NAD83__NEVADA_WEST__32109.SP",
                "USA__NEVADA__SPCS__E__NAD83__NEVADA_EAST__32107.SP",
                "USA__NEVADA__SPCS__C__NAD83__NEVADA_CENTRAL__32108.SP",
                "USA__NEW_JERSEY__NAD83__NEW_JERSEY__32111.SP",
                "USA__NEW_HAMPSHIRE__NAD83__NEW_HAMPSHIRE__32110.SP",
                "USA__NEW_MEXICO__SPCS__E__NAD83__NEW_MEXICO_EAST__32112.SP",
                "USA__NEW_MEXICO__SPCS83__C__NAD83__NEW_MEXICO_CENTRAL__32113.SP",
                "USA__NEW_MEXICO__SPCS83__W__NAD83__NEW_MEXICO_WEST__32114.SP",
                "USA__NEW_YORK__SPCS__E__NAD83__NEW_YORK_EAST__32115.SP",
                "USA__NEW_YORK__SPCS__C__NAD83__NEW_YORK_CENTRAL__32116.SP",
                "USA__NEW_YORK__SPCS__W__NAD83__NEW_YORK_WEST__32117.SP",
                "USA__NEW_YORK__SPCS__LONG_ISLAND__NAD83__NEW_YORK_LONG_ISLAND__32118.SP",
                "USA__NORTH_CAROLINA__NAD83__NORTH_CAROLINA__32119.SP",
                "USA__NORTH_DAKOTA__SPCS__N__NAD83__NORTH_DAKOTA_NORTH__32120.SP",
                "USA__NORTH_DAKOTA__SPCS__S__NAD83__NORTH_DAKOTA_SOUTH__32121.SP",
                "USA__OHIO__SPCS__N__NAD83__OHIO_NORTH__32122.SP",
                "USA__OHIO__SPCS__S__NAD83__OHIO_SOUTH__32123.SP",
                "USA__OKLAHOMA__SPCS__N__NAD83__OKLAHOMA_NORTH__32124.SP",
                "USA__OKLAHOMA__SPCS__S__NAD83__OKLAHOMA_SOUTH__32125.SP",
                "USA__OREGON__SPCS__N__NAD83__OREGON_NORTH__32126.SP",
                "USA__OREGON__SPCS__S__NAD83__OREGON_SOUTH__32127.SP",
                "USA__PENNSYLVANIA__SPCS__N__NAD83__PENNSYLVANIA_NORTH__32128.SP",
                "USA__PENNSYLVANIA__SPCS__S__NAD83__PENNSYLVANIA_SOUTH__32129.SP",
                "USA__RHODE_ISLAND__NAD83__RHODE_ISLAND__32130.SP",
                "USA__SOUTH_CAROLINA__NAD83__SOUTH_CAROLINA__32133.SP",
                "USA__SOUTH_DAKOTA__SPCS__N__NAD83__SOUTH_DAKOTA_NORTH__32134.SP",
                "USA__SOUTH_DAKOTA__SPCS__S__NAD83__SOUTH_DAKOTA_SOUTH__32135.SP",
                "USA__TENNESSEE__NAD83__TENNESSEE__32136.SP"
                // ... aggiungi qui gli altri  nomi dal tuo switch
        };

        for (String fileName : fileNames) {
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
