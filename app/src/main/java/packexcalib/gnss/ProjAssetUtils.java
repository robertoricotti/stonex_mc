package packexcalib.gnss;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProjAssetUtils {

    public static File copyProjDbIfNeeded(Context context) throws Exception {
        File projDir = new File(context.getFilesDir(), "proj");
        if (!projDir.exists() && !projDir.mkdirs()) {
            throw new RuntimeException("Impossibile creare dir PROJ: " + projDir.getAbsolutePath());
        }

        File projDb = new File(projDir, "proj.db");
        if (projDb.exists() && projDb.length() > 0) {
            return projDir;
        }

        AssetManager assetManager = context.getAssets();

        try (InputStream in = assetManager.open("proj/proj.db");
             FileOutputStream out = new FileOutputStream(projDb)) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }

        return projDir;
    }
}