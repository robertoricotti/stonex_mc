package packexcalib.gnss;

import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {
    public static byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Files.readAllBytes(file.toPath());
        }
        return new byte[0];
    }
}

