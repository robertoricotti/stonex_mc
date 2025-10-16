package packexcalib.gnss;



import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.*;
import java.nio.*;

public class GeoidBinLite {

    private static final double LAT_MIN = 24.0;
    private static final double LAT_MAX = 49.0;
    private static final double LON_MIN = -130.0;
    private static final double LON_MAX = -60.0;
    private static final double GRID_SPACING = 1.0 / 60.0;

    private static final int NUM_ROWS = 1501;
    private static final int NUM_COLS = 4201;
    private static final int HEADER_SIZE = 44;

    private float[][] grid;           // cache completa in RAM
    private boolean loaded = false;
    private final File file;

    public GeoidBinLite(File file) throws IOException {
        this.file = file;
        loadGrid(); // carica subito (puoi renderlo lazy se vuoi)
    }

    public boolean isInGrid(double lat, double lon) {
        return lat >= LAT_MIN && lat <= LAT_MAX && lon >= LON_MIN && lon <= LON_MAX;
    }

    public double getUndulation(double lat, double lon) throws IOException {
        if (!isInGrid(lat, lon))
            throw new IllegalArgumentException("Coordinates out of bounds.");
        if (!loaded)
            loadGrid();

        double r = (lat - LAT_MIN) / GRID_SPACING;
        double c = (lon - LON_MIN) / GRID_SPACING;

        int i = (int) Math.floor(r);
        int j = (int) Math.floor(c);

        double dy = r - i;
        double dx = c - j;

        // accesso diretto in RAM — nessun I/O
        float q11 = grid[i][j];       // SW
        float q21 = grid[i][j + 1];   // SE
        float q12 = grid[i + 1][j];   // NW
        float q22 = grid[i + 1][j + 1]; // NE

        return bilinearInterpolation(q11, q21, q12, q22, dx, dy);
    }

    public double getOrthometricHeight(double lat, double lon, double ellipsoidalHeight) throws IOException {
        double undulation = getUndulation(lat, lon);
        return ellipsoidalHeight - undulation;
    }

    public void close() throws IOException {
        // niente da chiudere — tutto in RAM
    }

    // ===================== PRIVATE =====================

    private void loadGrid() throws IOException {
        if (loaded) return;
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.skip(HEADER_SIZE);
            int total = NUM_ROWS * NUM_COLS;
            ByteBuffer buf = ByteBuffer.allocateDirect(total * 4).order(ByteOrder.LITTLE_ENDIAN);
            fis.getChannel().read(buf);
            buf.flip();

            grid = new float[NUM_ROWS][NUM_COLS];
            for (int r = 0; r < NUM_ROWS; r++) {
                for (int c = 0; c < NUM_COLS; c++) {
                    grid[r][c] = buf.getFloat();
                }
            }
            loaded = true;
        }
    }

    private double bilinearInterpolation(double q11, double q21, double q12, double q22, double dx, double dy) {
        return q11 * (1 - dx) * (1 - dy)
                + q21 * dx * (1 - dy)
                + q12 * (1 - dx) * dy
                + q22 * dx * dy;
    }
}












/*
public class GeoidBinLite {
    private static final double LAT_MIN = 24.0;
    private static final double LAT_MAX = 49.0;
    private static final double LON_MIN = -130.0;
    private static final double LON_MAX = -60.0;
    private static final double GRID_SPACING = 1.0 / 60.0;

    private static final int NUM_ROWS = 1501;  // (49 - 24) / (1/60) + 1
    private static final int NUM_COLS = 4201;  // (130 - 60) / (1/60) + 1

    private final RandomAccessFile raf;

    public GeoidBinLite(File file) throws IOException {
        this.raf = new RandomAccessFile(file, "r");
    }

    public boolean isInGrid(double lat, double lon) {
        return lat >= LAT_MIN && lat <= LAT_MAX && lon >= LON_MIN && lon <= LON_MAX;
    }

    public double getUndulation(double lat, double lon) throws IOException {
        if (!isInGrid(lat, lon)) {
            throw new IllegalArgumentException("Coordinates out of bounds.");
        }

        double r = (lat - LAT_MIN) / GRID_SPACING;
        double c = (lon - LON_MIN) / GRID_SPACING;

        int i = (int) Math.floor(r);
        int j = (int) Math.floor(c);

        double dy = r - i;
        double dx = c - j;

        float q11 = readValue(i, j);       // SW
        float q21 = readValue(i, j + 1);   // SE
        float q12 = readValue(i + 1, j);   // NW
        float q22 = readValue(i + 1, j + 1); // NE

        return bilinearInterpolation(q11, q21, q12, q22, dx, dy);
    }

    public double getOrthometricHeight(double lat, double lon, double ellipsoidalHeight) throws IOException {
        double undulation = getUndulation(lat, lon);
        return ellipsoidalHeight - undulation;
    }

    public void close() throws IOException {
        raf.close();
    }

    private float readValue(int row, int col) throws IOException {
        if (row < 0 || row >= NUM_ROWS || col < 0 || col >= NUM_COLS) {
            return 0;
        }

        long offset = 44L + ((long) row * NUM_COLS + col) * 4;
        raf.seek(offset);

        byte[] buffer = new byte[4];
        raf.readFully(buffer);
        ByteBuffer bb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
        return bb.getFloat();
    }

    private double bilinearInterpolation(double q11, double q21, double q12, double q22, double dx, double dy) {
        return q11 * (1 - dx) * (1 - dy)
                + q21 * dx * (1 - dy)
                + q12 * (1 - dx) * dy
                + q22 * dx * dy;
    }
}
*/