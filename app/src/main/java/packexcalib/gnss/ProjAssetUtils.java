package packexcalib.gnss;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProjAssetUtils {

    public static File copyProjAssetsIfNeeded(Context context) throws Exception {
        File projDir = new File(context.getFilesDir(), "proj");
        if (!projDir.exists() && !projDir.mkdirs()) {
            throw new RuntimeException("Impossibile creare dir PROJ: " + projDir.getAbsolutePath());
        }

        copyAssetIfNeeded(context.getAssets(), "proj/proj.db", new File(projDir, "proj.db"));
        copyAssetIfNeeded(context.getAssets(), "proj/table_yx_3_v1710_Q1.gsb", new File(projDir, "table_yx_3_v1710_Q1.gsb"));
        copyAssetIfNeeded(context.getAssets(), "proj/table_yx_3_v1710_Q3.gsb", new File(projDir, "table_yx_3_v1710_Q3.gsb"));

        return projDir;
    }

    private static void copyAssetIfNeeded(AssetManager assetManager, String assetPath, File outFile) throws Exception {
        if (outFile.exists() && outFile.length() > 0) {
            return;
        }

        try (InputStream in = assetManager.open(assetPath);
             FileOutputStream out = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
    }
}