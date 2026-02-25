package packexcalib.gnss;

import org.locationtech.proj4j.ProjCoordinate;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class CzechGridShiftTransformer {
    private final double sLat, nLat, wLon, eLon; // METERS
    private final double step;
    private final int rows, cols;
    private final float[] dY; // shift su Y/N
    private final float[] dX; // shift su X/E
    private static final float NODATA = 9999f;

    public CzechGridShiftTransformer(InputStream is) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(is));

        // Global header: NUM_OREC=11 record da 16 bytes
        int numOrec = readIntRecordValueLE(in, "NUM_OREC");
        for (int i = 1; i < numOrec; i++) readRecord(in);

        // Subheader: standard 11 record
        double S=0, N=0, E=0, W=0, dN=0, dW=0;
        int gsCount = -1;

        for (int i = 0; i < 11; i++) {
            Record r = readRecord(in);
            String k = r.key.trim();
            if (k.equals("S LAT")) S = leDouble(r.val);
            else if (k.equals("N LAT")) N = leDouble(r.val);
            else if (k.equals("E LONG")) E = leDouble(r.val);
            else if (k.equals("W LONG")) W = leDouble(r.val);
            else if (k.equals("N GRID")) dN = leDouble(r.val);
            else if (k.equals("W GRID")) dW = leDouble(r.val);
            else if (k.equals("GS_COUNT")) gsCount = leInt32(r.val);
        }

        this.sLat = S;
        this.nLat = N;
        this.eLon = E;
        this.wLon = W;

        // nei tuoi file N_GRID e W_GRID sono uguali (step 2000)
        this.step = dN;

        this.cols = (int)Math.round((eLon - wLon) / step) + 1;
        this.rows = (int)Math.round((nLat - sLat) / step) + 1;

        int nodeCount = rows * cols;
        // gsCount lo lasciamo come check informativo (non blocchiamo)
        // if (gsCount > 0 && gsCount != nodeCount) { ... }

        dY = new float[nodeCount];
        dX = new float[nodeCount];

        byte[] buf = new byte[16]; // 4 float LE per nodo
        for (int i = 0; i < nodeCount; i++) {
            in.readFully(buf);
            float a = leFloat(buf, 0);
            float b = leFloat(buf, 4);
            dY[i] = a;
            dX[i] = b;
        }
    }

    /** Versione zero-alloc: applica shift in-place su ProjCoordinate (x=E, y=N) */
    public void applyInPlace(ProjCoordinate en) {
        final double E = en.x;
        final double N = en.y;

        final int col = (int)Math.floor((E - wLon) / step);
        final int row = (int)Math.floor((N - sLat) / step);

        if (col < 0 || col >= cols - 1 || row < 0 || row >= rows - 1) return;

        final double e0 = wLon + col * step;
        final double n0 = sLat + row * step;
        final double t = (E - e0) / step;
        final double u = (N - n0) / step;

        final int i00 = row * cols + col;
        final int i10 = i00 + 1;
        final int i01 = i00 + cols;
        final int i11 = i01 + 1;

        final float dy00 = dY[i00], dy10 = dY[i10], dy01 = dY[i01], dy11 = dY[i11];
        if (dy00 == NODATA || dy10 == NODATA || dy01 == NODATA || dy11 == NODATA) return;

        final float dx00 = dX[i00], dx10 = dX[i10], dx01 = dX[i01], dx11 = dX[i11];

        final double oneMinusT = 1.0 - t;
        final double oneMinusU = 1.0 - u;

        final double dYb =
                oneMinusT * oneMinusU * dy00 +
                        t * oneMinusU * dy10 +
                        oneMinusT * u * dy01 +
                        t * u * dy11;

        final double dXb =
                oneMinusT * oneMinusU * dx00 +
                        t * oneMinusU * dx10 +
                        oneMinusT * u * dx01 +
                        t * u * dx11;

        en.x = E + dXb;
        en.y = N + dYb;
    }

    /** (opzionale) compatibilità col tuo codice attuale */
    public void applyInPlace(double[] en) {
        // delega senza allocazioni interne
        ProjCoordinate tmp = new ProjCoordinate(en[0], en[1], 0);
        applyInPlace(tmp);
        en[0] = tmp.x;
        en[1] = tmp.y;
    }

    // -------- helpers --------
    private static final class Record {
        final String key;
        final byte[] val;
        Record(String k, byte[] v){ key=k; val=v; }
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

    private static int leInt32(byte[] v){
        return (v[0] & 0xff) | ((v[1] & 0xff) << 8) | ((v[2] & 0xff) << 16) | ((v[3] & 0xff) << 24);
    }

    private static double leDouble(byte[] v){
        long bits = 0;
        for (int i=7;i>=0;i--) bits = (bits<<8) | (v[i] & 0xffL);
        return Double.longBitsToDouble(bits);
    }

    private static float leFloat(byte[] b, int off){
        int bits = (b[off] & 0xff) |
                ((b[off+1] & 0xff) << 8) |
                ((b[off+2] & 0xff) << 16) |
                ((b[off+3] & 0xff) << 24);
        return Float.intBitsToFloat(bits);
    }
}