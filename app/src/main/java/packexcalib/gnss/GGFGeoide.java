package packexcalib.gnss;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GGFGeoide {
    private final double latMin = 35.0;
    private final double lonMin = 5.0;
    private final double latMax = 47.0;
    private final double lonMax = 16.0;
    private final double resolution = 0.01;

    private final int rows = (int)((latMax - latMin) / resolution) + 1;
    private final int cols = (int)((lonMax - lonMin) / resolution) + 1;
    private final float[][] grid;

    public GGFGeoide(String filePath) throws IOException {
        grid = new float[rows][cols];
        loadGrid(filePath);
    }

    private void loadGrid(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            byte[] buffer = new byte[4];
            for (int i = 0; i < rows; i++) { // direzione corretta
                for (int j = 0; j < cols; j++) {
                    if (fis.read(buffer) != 4)
                        throw new IOException("GGF: fine file inattesa");
                    grid[i][j] = ByteBuffer.wrap(buffer)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .getFloat();
                }
            }
        }
    }

    public boolean isInGrid(double lat, double lon) {
        return lat >= latMin && lat <= latMax &&
                lon >= lonMin && lon <= lonMax;
    }

    public double getGeoidSeparation(double lat, double lon) {
        if (!isInGrid(lat, lon)) {
            throw new IllegalArgumentException("Coordinate fuori griglia");
        }

        int iLat = (int)((lat - latMin) / resolution);
        int iLon = (int)((lon - lonMin) / resolution);

        double lat0 = latMin + iLat * resolution;
        double lon0 = lonMin + iLon * resolution;

        double dx = (lon - lon0) / resolution;
        double dy = (lat - lat0) / resolution;

        float z00 = grid[iLat][iLon];
        float z10 = grid[iLat][iLon + 1];
        float z01 = grid[iLat + 1][iLon];
        float z11 = grid[iLat + 1][iLon + 1];

        return bilinearInterpolation(z00, z10, z01, z11, dx, dy);
    }

    private double bilinearInterpolation(double q11, double q21, double q12, double q22, double dx, double dy) {
        return q11 * (1 - dx) * (1 - dy)
                + q21 * dx * (1 - dy)
                + q12 * (1 - dx) * dy
                + q22 * dx * dy;
    }
}
