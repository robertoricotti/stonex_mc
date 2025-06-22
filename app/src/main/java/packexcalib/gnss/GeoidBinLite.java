package packexcalib.gnss;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GeoidBinLite {
    private final File file;
    private final double latStart, lonStart, dLat, dLon;
    private final int rows, cols;

    public GeoidBinLite(String filePath, double latStart, double lonStart,
                        double dLat, double dLon, int rows, int cols) {
        this.file = new File(filePath);
        this.latStart = latStart;
        this.lonStart = lonStart;
        this.dLat = dLat;
        this.dLon = dLon;
        this.rows = rows;
        this.cols = cols;
    }

    public boolean isInGrid(double lat, double lon) {
        return lat >= latStart && lat <= latStart + (rows - 1) * dLat &&
                lon >= lonStart && lon <= lonStart + (cols - 1) * dLon;
    }

    public double getOrthometricHeight(double lat, double lon, double ellipsoidH) throws IOException {
        if (!isInGrid(lat, lon)) throw new IllegalArgumentException("Coordinate fuori griglia");

        double r = (lat - latStart) / dLat;
        double c = (lon - lonStart) / dLon;
        int i = (int) Math.floor(r);
        int j = (int) Math.floor(c);
        double dr = r - i;
        double dc = c - j;

        float q00 = readGridValue(i,     j);
        float q10 = readGridValue(i + 1, j);
        float q01 = readGridValue(i,     j + 1);
        float q11 = readGridValue(i + 1, j + 1);

        double interp =
                q00 * (1 - dr) * (1 - dc) +
                        q10 * dr       * (1 - dc) +
                        q01 * (1 - dr) * dc +
                        q11 * dr       * dc;

        return ellipsoidH - interp;
    }

    private float readGridValue(int row, int col) throws IOException {
        if (row < 0 || row >= rows || col < 0 || col >= cols)
            return 0;

        long index = ((long) row * cols + col) * 4;
        byte[] buf = new byte[4];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.skip(index);
            if (fis.read(buf) != 4)
                return 0;
        }
        return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }
}

