package packexcalib.gnss;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GeoidBin {
    private final String filePath;
    private final int rows, cols;
    private final double latStart, lonStart, dLat, dLon;
    private final float[] grid;  // row-major single array

    /**
     * costruttore
     * @param filePath percorso .bin
     * @param latStart latitudine sud minima (°)
     * @param lonStart longitudine ovest minima (° negativo)
     * @param dLat passo latitudine (°)
     * @param dLon passo longitudine (°)
     * @param rows righe totali
     * @param cols colonne totali
     */
    public GeoidBin(String filePath, double latStart, double lonStart,
                    double dLat, double dLon, int rows, int cols) throws IOException {
        this.filePath = filePath;
        this.latStart = latStart;
        this.lonStart = lonStart;
        this.dLat = dLat;
        this.dLon = dLon;
        this.rows = rows;
        this.cols = cols;
        this.grid = new float[rows * cols];
        loadGrid();
    }

    private void loadGrid() throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buf = new byte[4];
            ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < rows * cols; i++) {
                fis.read(buf);
                grid[i] = bb.getFloat(0);
            }
        }
    }

    public boolean isInGrid(double lat, double lon) {
        return lat >= latStart && lat <= latStart + (rows - 1) * dLat &&
                lon >= lonStart && lon <= lonStart + (cols - 1) * dLon;
    }

    public double getOrthometricHeight(double lat, double lon, double ellipsoidH) {
        if (!isInGrid(lat, lon)) throw new IllegalArgumentException("Fuori griglia");
        double r = (lat - latStart) / dLat;
        double c = (lon - lonStart) / dLon;
        int i = (int) Math.floor(r);
        int j = (int) Math.floor(c);
        double dr = r - i, dc = c - j;

        int idx00 = i * cols + j;
        int idx10 = (i + 1) * cols + j;
        int idx01 = i * cols + (j + 1);
        int idx11 = (i + 1) * cols + (j + 1);

        double q00 = grid[idx00];
        double q10 = grid[idx10];
        double q01 = grid[idx01];
        double q11 = grid[idx11];

        double interp =
                q00 * (1 - dr) * (1 - dc) +
                        q10 * dr * (1 - dc) +
                        q01 * (1 - dr) * dc +
                        q11 * dr * dc;

        return ellipsoidH - interp;
    }
}

