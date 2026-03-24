package packexcalib.gnss;

import org.locationtech.proj4j.ProjCoordinate;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class CzechGridShiftTransformer {

    public enum RowOrder {
        NORTH_TO_SOUTH,
        SOUTH_TO_NORTH
    }

    public enum ColOrder {
        WEST_TO_EAST,
        EAST_TO_WEST
    }

    public enum ShiftMode {
        ADD,
        SUBTRACT
    }

    public enum ComponentOrder {
        YX,   // primo float = dY, secondo = dX
        XY    // primo float = dX, secondo = dY
    }

    private final double sLat;
    private final double nLat;
    private final double wLon;
    private final double eLon;
    private final double stepX;
    private final double stepY;

    private final int rows;
    private final int cols;

    private final float[] dY;
    private final float[] dX;

    private final RowOrder rowOrder;
    private final ColOrder colOrder;
    private final ShiftMode shiftMode;
    private final ComponentOrder componentOrder;

    private static final float NODATA = 9999f;

    /**
     * Mantiene il comportamento storico:
     * - righe NORTH_TO_SOUTH
     * - colonne WEST_TO_EAST
     * - shift ADD
     * - componenti YX
     */
    public CzechGridShiftTransformer(InputStream is) throws IOException {
        this(is,
                RowOrder.NORTH_TO_SOUTH,
                ColOrder.WEST_TO_EAST,
                ShiftMode.ADD,
                ComponentOrder.YX);
    }

    public CzechGridShiftTransformer(InputStream is,
                                     RowOrder rowOrder,
                                     ColOrder colOrder,
                                     ShiftMode shiftMode,
                                     ComponentOrder componentOrder) throws IOException {

        this.rowOrder = rowOrder;
        this.colOrder = colOrder;
        this.shiftMode = shiftMode;
        this.componentOrder = componentOrder;

        DataInputStream in = new DataInputStream(new BufferedInputStream(is));

        int numOrec = readIntRecordValueLE(in, "NUM_OREC");
        for (int i = 1; i < numOrec; i++) {
            readRecord(in);
        }

        double S = 0, N = 0, E = 0, W = 0, dN = 0, dW = 0;
        int gsCount = -1;

        for (int i = 0; i < 11; i++) {
            Record r = readRecord(in);
            String k = r.key.trim();

            if ("S LAT".equals(k)) {
                S = leDouble(r.val);
            } else if ("N LAT".equals(k)) {
                N = leDouble(r.val);
            } else if ("E LONG".equals(k)) {
                E = leDouble(r.val);
            } else if ("W LONG".equals(k)) {
                W = leDouble(r.val);
            } else if ("N GRID".equals(k)) {
                dN = leDouble(r.val);
            } else if ("W GRID".equals(k)) {
                dW = leDouble(r.val);
            } else if ("GS_COUNT".equals(k)) {
                gsCount = leInt32(r.val);
            }
        }

        this.sLat = S;
        this.nLat = N;
        this.eLon = E;
        this.wLon = W;
        this.stepY = Math.abs(dN);
        this.stepX = Math.abs(dW);

        this.cols = (int) Math.round((eLon - wLon) / stepX) + 1;
        this.rows = (int) Math.round((nLat - sLat) / stepY) + 1;

        int nodeCount = rows * cols;
        if (gsCount > 0 && gsCount != nodeCount) {
            System.out.println("CZ grid warning: GS_COUNT=" + gsCount + " computed=" + nodeCount);
        }

        dY = new float[nodeCount];
        dX = new float[nodeCount];

        byte[] buf = new byte[16];
        for (int i = 0; i < nodeCount; i++) {
            in.readFully(buf);

            float a = leFloat(buf, 0);
            float b = leFloat(buf, 4);

            if (componentOrder == ComponentOrder.YX) {
                dY[i] = a;
                dX[i] = b;
            } else {
                dX[i] = a;
                dY[i] = b;
            }
        }
    }

    public boolean applyInPlace(ProjCoordinate en) {
        ProjCoordinate out = transformInternal(en.x, en.y);
        if (out == null) return false;
        en.x = out.x;
        en.y = out.y;
        return true;
    }

    public boolean applyInPlace(double[] en) {
        if (en == null || en.length < 2) return false;
        ProjCoordinate out = transformInternal(en[0], en[1]);
        if (out == null) return false;
        en[0] = out.x;
        en[1] = out.y;
        return true;
    }

    public ProjCoordinate applyDebug(ProjCoordinate en) {
        return transformInternal(en.x, en.y);
    }

    public ProjCoordinate applyDebug(ProjCoordinate en,
                                     RowOrder rowOrder,
                                     ColOrder colOrder,
                                     ShiftMode shiftMode,
                                     ComponentOrder componentOrder) {
        return transformInternal(en.x, en.y, rowOrder, colOrder, shiftMode, componentOrder);
    }

    private ProjCoordinate transformInternal(double E, double N) {
        return transformInternal(E, N, rowOrder, colOrder, shiftMode, componentOrder);
    }

    private ProjCoordinate transformInternal(double E, double N,
                                             RowOrder rowOrder,
                                             ColOrder colOrder,
                                             ShiftMode shiftMode,
                                             ComponentOrder componentOrder) {

        GridIndex gi = locateCell(E, N, rowOrder, colOrder);
        if (gi == null) return null;

        final int i00 = gi.row * cols + gi.col;
        final int i10 = i00 + 1;
        final int i01 = i00 + cols;
        final int i11 = i01 + 1;

        final float dy00 = dY[i00];
        final float dy10 = dY[i10];
        final float dy01 = dY[i01];
        final float dy11 = dY[i11];

        final float dx00 = dX[i00];
        final float dx10 = dX[i10];
        final float dx01 = dX[i01];
        final float dx11 = dX[i11];

        if (dy00 == NODATA || dy10 == NODATA || dy01 == NODATA || dy11 == NODATA ||
                dx00 == NODATA || dx10 == NODATA || dx01 == NODATA || dx11 == NODATA) {
            return null;
        }

        final double omt = 1.0 - gi.t;
        final double omu = 1.0 - gi.u;

        final double dYb =
                omt * omu * dy00 +
                        gi.t * omu * dy10 +
                        omt * gi.u * dy01 +
                        gi.t * gi.u * dy11;

        final double dXb =
                omt * omu * dx00 +
                        gi.t * omu * dx10 +
                        omt * gi.u * dx01 +
                        gi.t * gi.u * dx11;

        final double sign = (shiftMode == ShiftMode.ADD) ? 1.0 : -1.0;

        return new ProjCoordinate(
                E + sign * dXb,
                N + sign * dYb,
                0
        );
    }

    private GridIndex locateCell(double E, double N,
                                 RowOrder rowOrder,
                                 ColOrder colOrder) {

        final int col;
        final int row;

        if (colOrder == ColOrder.WEST_TO_EAST) {
            col = (int) Math.floor((E - wLon) / stepX);
        } else {
            col = (int) Math.floor((eLon - E) / stepX);
        }

        if (rowOrder == RowOrder.NORTH_TO_SOUTH) {
            row = (int) Math.floor((nLat - N) / stepY);
        } else {
            row = (int) Math.floor((N - sLat) / stepY);
        }

        if (col < 0 || col >= cols - 1 || row < 0 || row >= rows - 1) {
            return null;
        }

        final double e0;
        final double n0;

        if (colOrder == ColOrder.WEST_TO_EAST) {
            e0 = wLon + col * stepX;
        } else {
            e0 = eLon - col * stepX;
        }

        if (rowOrder == RowOrder.NORTH_TO_SOUTH) {
            n0 = nLat - row * stepY;
        } else {
            n0 = sLat + row * stepY;
        }

        final double t;
        final double u;

        if (colOrder == ColOrder.WEST_TO_EAST) {
            t = (E - e0) / stepX;
        } else {
            t = (e0 - E) / stepX;
        }

        if (rowOrder == RowOrder.NORTH_TO_SOUTH) {
            u = (n0 - N) / stepY;
        } else {
            u = (N - n0) / stepY;
        }

        if (t < 0.0 || t > 1.0 || u < 0.0 || u > 1.0) {
            return null;
        }

        return new GridIndex(row, col, t, u);
    }

    public String describe() {
        return "CzechGridShiftTransformer{" +
                "sLat=" + sLat +
                ", nLat=" + nLat +
                ", wLon=" + wLon +
                ", eLon=" + eLon +
                ", stepX=" + stepX +
                ", stepY=" + stepY +
                ", rows=" + rows +
                ", cols=" + cols +
                ", rowOrder=" + rowOrder +
                ", colOrder=" + colOrder +
                ", shiftMode=" + shiftMode +
                ", componentOrder=" + componentOrder +
                '}';
    }

    private static final class GridIndex {
        final int row;
        final int col;
        final double t;
        final double u;

        GridIndex(int row, int col, double t, double u) {
            this.row = row;
            this.col = col;
            this.t = t;
            this.u = u;
        }
    }

    private static final class Record {
        final String key;
        final byte[] val;

        Record(String k, byte[] v) {
            key = k;
            val = v;
        }
    }

    private static Record readRecord(DataInputStream in) throws IOException {
        byte[] k = new byte[8];
        byte[] v = new byte[8];
        in.readFully(k);
        in.readFully(v);
        return new Record(new String(k, StandardCharsets.US_ASCII), v);
    }

    private static int readIntRecordValueLE(DataInputStream in, String expectedKey) throws IOException {
        Record r = readRecord(in);
        if (!r.key.trim().equals(expectedKey)) {
            throw new IOException("Atteso key " + expectedKey + " ma trovato " + r.key);
        }
        return leInt32(r.val);
    }

    private static int leInt32(byte[] v) {
        return (v[0] & 0xff) |
                ((v[1] & 0xff) << 8) |
                ((v[2] & 0xff) << 16) |
                ((v[3] & 0xff) << 24);
    }

    private static double leDouble(byte[] v) {
        long bits = 0;
        for (int i = 7; i >= 0; i--) {
            bits = (bits << 8) | (v[i] & 0xffL);
        }
        return Double.longBitsToDouble(bits);
    }

    private static float leFloat(byte[] b, int off) {
        int bits = (b[off] & 0xff) |
                ((b[off + 1] & 0xff) << 8) |
                ((b[off + 2] & 0xff) << 16) |
                ((b[off + 3] & 0xff) << 24);
        return Float.intBitsToFloat(bits);
    }
}