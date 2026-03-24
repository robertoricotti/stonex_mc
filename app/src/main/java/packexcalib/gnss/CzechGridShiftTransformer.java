package packexcalib.gnss;

import org.locationtech.proj4j.ProjCoordinate;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class CzechGridShiftTransformer {

    private final double sLat;
    private final double nLat;
    private final double wLon;
    private final double eLon;
    private final double step;

    private final int rows;
    private final int cols;

    // File table_yx: primo float = shift Y, secondo float = shift X
    private final float[] dY;
    private final float[] dX;

    private static final float NODATA = 9999f;

    public CzechGridShiftTransformer(InputStream is) throws IOException {
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
        this.step = dN;

        this.cols = (int) Math.round((eLon - wLon) / step) + 1;
        this.rows = (int) Math.round((nLat - sLat) / step) + 1;

        int nodeCount = rows * cols;
        if (gsCount > 0 && gsCount != nodeCount) {
            // non blocco: solo informativo
            System.out.println("CZ grid warning: GS_COUNT=" + gsCount + " computed=" + nodeCount);
        }

        dY = new float[nodeCount];
        dX = new float[nodeCount];

        byte[] buf = new byte[16];
        for (int i = 0; i < nodeCount; i++) {
            in.readFully(buf);

            float a = leFloat(buf, 0);
            float b = leFloat(buf, 4);

            dY[i] = a;
            dX[i] = b;
        }
    }

    /**
     * Applica lo shift al punto EN.
     * x = Easting
     * y = Northing
     *
     * Ritorna true se applicato, false se fuori griglia o nodata.
     */
    public boolean applyInPlace(ProjCoordinate en) {
        final double E = en.x;
        final double N = en.y;

        // Convenzione coerente con il vecchio debugShift():
        // righe indicizzate da nord verso sud
        final int col = (int) Math.floor((E - wLon) / step);
        final int row = (int) Math.floor((nLat - N) / step);

        if (col < 0 || col >= cols - 1 || row < 0 || row >= rows - 1) {
            return false;
        }

        final int i00 = row * cols + col;
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
            return false;
        }

        final double e0 = wLon + col * step;
        final double n0 = nLat - row * step;

        final double t = (E - e0) / step;
        final double u = (n0 - N) / step;

        final double omt = 1.0 - t;
        final double omu = 1.0 - u;

        final double dYb =
                omt * omu * dy00 +
                        t   * omu * dy10 +
                        omt * u   * dy01 +
                        t   * u   * dy11;

        final double dXb =
                omt * omu * dx00 +
                        t   * omu * dx10 +
                        omt * u   * dx01 +
                        t   * u   * dx11;

        en.x = E + dXb;
        en.y = N + dYb;
        return true;
    }

    public boolean applyInPlace(double[] en) {
        if (en == null || en.length < 2) return false;
        ProjCoordinate tmp = new ProjCoordinate(en[0], en[1], 0);
        boolean ok = applyInPlace(tmp);
        if (ok) {
            en[0] = tmp.x;
            en[1] = tmp.y;
        }
        return ok;
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