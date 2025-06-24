package packexcalib.gnss;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class GGFGeoide {
    double latMin = 47.2208333333;
    double latMax = 55.9791666667;
    double lonMin = 3.25625;
    double lonMax = 15.11875;
    double dLat = 0.0083333333;
    double dLon = 0.0125;
     int rows, cols;
     float[] grid;

    public GGFGeoide() {

        rows = (int) Math.round((latMax - latMin) / dLat) + 1;
        cols = (int) Math.round((lonMax - lonMin) / dLon) + 1;
        grid = new float[rows * cols];
    }

    public void load(String path) throws IOException {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)))) {
            byte[] buffer = new byte[4];
            for (int i = 0; i < rows * cols; i++) {
                dis.readFully(buffer);
                float val = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                grid[i] = val;
            }
        }
    }

    public boolean isInGrid(double lat, double lon) {
        return lat >= latMin && lat <= latMax && lon >= lonMin && lon <= lonMax;
    }

    public double getUndulation(double lat, double lon) {
        if (!isInGrid(lat, lon)) return Double.NaN;

        double r = (latMax - lat) / dLat;  // da nord a sud
        double c = (lon - lonMin) / dLon;

        int i = (int) Math.floor(r);
        int j = (int) Math.floor(c);
        double dr = r - i;
        double dc = c - j;

        if (i < 0 || i >= rows - 1 || j < 0 || j >= cols - 1)
            return Double.NaN;

        float q00 = grid[i * cols + j];
        float q10 = grid[(i + 1) * cols + j];
        float q01 = grid[i * cols + (j + 1)];
        float q11 = grid[(i + 1) * cols + (j + 1)];

        if (Float.isNaN(q00) || Float.isNaN(q10) || Float.isNaN(q01) || Float.isNaN(q11))
            return 0.0;

        return q00 * (1 - dr) * (1 - dc)
                + q10 * dr * (1 - dc)
                + q01 * (1 - dr) * dc
                + q11 * dr * dc;
    }
}
