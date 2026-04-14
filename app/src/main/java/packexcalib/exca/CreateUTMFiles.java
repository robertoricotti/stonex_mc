package packexcalib.exca;

import java.io.File;
import java.io.FileOutputStream;

public class CreateUTMFiles {

    public static void createUTMFiles(String folderPath) {

        try {

            File folder = new File(folderPath);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            // UTM NORTH
            for (int zone = 1; zone <= 60; zone++) {

                int epsg = 32600 + zone;

                String name = "UTM_" + zone + "N_" + epsg + ".SP";

                File file = new File(folder, name);

                if (!file.exists()) {
                    new FileOutputStream(file).close();
                }
            }

            // UTM SOUTH
            for (int zone = 1; zone <= 60; zone++) {

                int epsg = 32700 + zone;

                String name = "UTM_" + zone + "S_" + epsg + ".SP";

                File file = new File(folder, name);

                if (!file.exists()) {
                    new FileOutputStream(file).close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

