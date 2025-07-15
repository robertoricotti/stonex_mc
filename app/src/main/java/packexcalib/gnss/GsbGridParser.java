package packexcalib.gnss;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class GsbGridParser {

    public static class GridShift {
        public String name;
        public double latMin, latMax, lonMin, lonMax;
        public double latInc, lonInc;
        public int nodeCountLat, nodeCountLon;
        public float[][] latShiftSec;
        public float[][] lonShiftSec;
    }

    private final List<GridShift> grids = new ArrayList<>();
    private final double[] reusableResult = new double[2];

    /**
     * Constructor - parse GSB file from InputStream (should be buffered)
     */
    public GsbGridParser(InputStream inputStream) throws IOException {
        parse(inputStream);
    }

    public List<GridShift> getGrids() {
        return grids;
    }

    private void parse(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(in));

        // Skip 11 x 16 bytes = 176-byte header
        byte[] header = new byte[176];
        dis.readFully(header);

        while (true) {
            byte[] subHeader = new byte[176];
            if (dis.read(subHeader) < 176) break;

            ByteBuffer bb = ByteBuffer.wrap(subHeader).order(ByteOrder.LITTLE_ENDIAN);

            String subName = readString(bb, 0, 8).trim();
            bb.position(16); // Skip parent name

            double latMin = bb.getDouble();
            double latMax = bb.getDouble();
            double lonMin = bb.getDouble();
            double lonMax = bb.getDouble();
            double latInc = bb.getDouble();
            double lonInc = bb.getDouble();

            int nodeCountLat = bb.getInt();
            int nodeCountLon = bb.getInt();

            GridShift grid = new GridShift();
            grid.name = subName;
            grid.latMin = latMin;
            grid.latMax = latMax;
            grid.lonMin = lonMin;
            grid.lonMax = lonMax;
            grid.latInc = latInc;
            grid.lonInc = lonInc;
            grid.nodeCountLat = nodeCountLat;
            grid.nodeCountLon = nodeCountLon;

            grid.latShiftSec = new float[nodeCountLat][nodeCountLon];
            grid.lonShiftSec = new float[nodeCountLat][nodeCountLon];

            for (int i = 0; i < nodeCountLat; i++) {
                for (int j = 0; j < nodeCountLon; j++) {
                    float latShift = readFloatLE(dis);
                    float lonShift = readFloatLE(dis);
                    dis.skipBytes(8); // Skip accuracy
                    grid.latShiftSec[i][j] = latShift;
                    grid.lonShiftSec[i][j] = lonShift;
                }
            }

            grids.add(grid);
        }

        dis.close();
    }

    private static float readFloatLE(DataInputStream dis) throws IOException {
        byte[] buffer = new byte[4];
        dis.readFully(buffer);
        return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    private static String readString(ByteBuffer bb, int offset, int length) {
        byte[] bytes = new byte[length];
        bb.position(offset);
        bb.get(bytes);
        return new String(bytes).replaceAll("\0", "");
    }

    /**
     * Applies the shift to WGS84 coordinate using bilinear interpolation.
     * @param latDeg latitude in degrees
     * @param lonDeg longitude in degrees
     * @param copyOutput whether to return a new array or reuse internal buffer (false = fastest, not thread-safe)
     * @return shifted coordinates in degrees [lat, lon]
     */
    public double[] applyShift(double latDeg, double lonDeg, boolean copyOutput) {
        for (GridShift grid : grids) {
            if (latDeg >= grid.latMin && latDeg <= grid.latMax &&
                    lonDeg >= grid.lonMin && lonDeg <= grid.lonMax) {

                int i = (int) ((latDeg - grid.latMin) / grid.latInc);
                int j = (int) ((lonDeg - grid.lonMin) / grid.lonInc);

                if (i >= grid.nodeCountLat - 1 || j >= grid.nodeCountLon - 1) {
                    break;
                }

                double latRel = (latDeg - grid.latMin - i * grid.latInc) / grid.latInc;
                double lonRel = (lonDeg - grid.lonMin - j * grid.lonInc) / grid.lonInc;

                float lat00 = grid.latShiftSec[i][j];
                float lat01 = grid.latShiftSec[i][j + 1];
                float lat10 = grid.latShiftSec[i + 1][j];
                float lat11 = grid.latShiftSec[i + 1][j + 1];

                float lon00 = grid.lonShiftSec[i][j];
                float lon01 = grid.lonShiftSec[i][j + 1];
                float lon10 = grid.lonShiftSec[i + 1][j];
                float lon11 = grid.lonShiftSec[i + 1][j + 1];

                double latShift = bilinearInterpolate(lat00, lat01, lat10, lat11, lonRel, latRel);
                double lonShift = bilinearInterpolate(lon00, lon01, lon10, lon11, lonRel, latRel);

                reusableResult[0] = latDeg + (latShift / 3600.0); // arcsec → degrees
                reusableResult[1] = lonDeg + (lonShift / 3600.0);

                if (copyOutput) {
                    return new double[]{reusableResult[0], reusableResult[1]};
                } else {
                    return reusableResult;
                }
            }
        }

        // Outside grid: return original
        if (copyOutput) {
            return new double[]{latDeg, lonDeg};
        } else {
            reusableResult[0] = latDeg;
            reusableResult[1] = lonDeg;
            return reusableResult;
        }
    }

    private double bilinearInterpolate(double q11, double q12, double q21, double q22, double x, double y) {
        return q11 * (1 - x) * (1 - y)
                + q12 * (1 - x) * y
                + q21 * x * (1 - y)
                + q22 * x * y;
    }
}


