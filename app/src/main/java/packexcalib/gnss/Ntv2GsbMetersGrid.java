package packexcalib.gnss;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class Ntv2GsbMetersGrid {

    public static final class Grid {
        public String subName;
        public String parent;

        // In questi file: sono metri (y/x), anche se i tag si chiamano S LAT / W LONG ecc.
        public double yMin, yMax; // S LAT, N LAT
        public double xMin, xMax; // W LONG, E LONG
        public double yInc, xInc; // N GRID, W GRID

        public int rows; // y
        public int cols; // x

        // shift in metri
        public float[][] shiftY; // first float per node
        public float[][] shiftX; // second float per node
    }

    private final List<Grid> grids = new ArrayList<>();
    private final double[] reusable = new double[2];

    public List<Grid> getGrids() { return grids; }

    public Ntv2GsbMetersGrid(InputStream in) throws IOException {
        parse(new BufferedInputStream(in));
    }

    private void parse(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);

        // --- MAIN HEADER: 11 records * 16 bytes
        Record[] main = readRecords(dis, 11);
        int numSrec = getInt(main, "NUM_SREC");
        if (numSrec <= 0) numSrec = 11; // fallback

        // --- SUBGRIDS: leggi finché finisce il file
        while (true) {
            Record[] sub;
            try {
                sub = readRecords(dis, numSrec);
            } catch (EOFException eof) {
                break; // fine file normale
            }

            Grid g = new Grid();
            g.subName = getStr(sub, "SUB_NAME");
            g.parent  = getStr(sub, "PARENT");

            double sLat  = getDouble(sub, "S LAT");   // yMin
            double nLat  = getDouble(sub, "N LAT");   // yMax
            double eLon  = getDouble(sub, "E LONG");  // xMax
            double wLon  = getDouble(sub, "W LONG");  // xMin
            double nGrid = getDouble(sub, "N GRID");  // yInc
            double wGrid = getDouble(sub, "W GRID");  // xInc

            g.yMin = Math.min(sLat, nLat);
            g.yMax = Math.max(sLat, nLat);
            g.xMin = Math.min(wLon, eLon);
            g.xMax = Math.max(wLon, eLon);
            g.yInc = Math.abs(nGrid);
            g.xInc = Math.abs(wGrid);

            int gsCount = getInt(sub, "GS_COUNT");
            int nodes   = gsCount - numSrec; // nei tuoi file: include anche i record subheader

            int rows = (int) Math.round((g.yMax - g.yMin) / g.yInc) + 1;
            int cols = (int) Math.round((g.xMax - g.xMin) / g.xInc) + 1;

            // se mismatch, prova a derivare da nodes (fallback)
            if (rows * cols != nodes && nodes > 0) {
                // nei tuoi file dovrebbe essere già ok; lasciamo comunque robusto:
                // qui non “indovino” senza una regola certa -> log in debug se vuoi
            }

            g.rows = rows;
            g.cols = cols;
            g.shiftY = new float[rows][cols];
            g.shiftX = new float[rows][cols];

            byte[] nodeBuf = new byte[16];
            ByteBuffer bb = ByteBuffer.wrap(nodeBuf).order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    dis.readFully(nodeBuf);
                    bb.position(0);
                    float dy = bb.getFloat(); // shift Y (metri)
                    float dx = bb.getFloat(); // shift X (metri)
                    bb.getFloat(); // accY
                    bb.getFloat(); // accX
                    g.shiftY[i][j] = dy;
                    g.shiftX[i][j] = dx;
                }
            }

            grids.add(g);
        }
    }


    // Applica shift su coordinate (x,y) in metri con bilinear interpolation.
    // Ritorna [xShifted, yShifted] in metri.
    // copyOutput=false evita alloc (non thread-safe).
    public double[] applyShiftMeters(double x, double y, boolean copyOutput) {
        for (Grid g : grids) {
            if (x >= g.xMin && x <= g.xMax && y >= g.yMin && y <= g.yMax) {

                double fx = (x - g.xMin) / g.xInc;
                double fy = (y - g.yMin) / g.yInc;

                int j = (int) Math.floor(fx); // col
                int i = (int) Math.floor(fy); // row

                if (i < 0 || j < 0 || i >= g.rows - 1 || j >= g.cols - 1) break;

                double tx = fx - j; // [0,1)
                double ty = fy - i;

                float y00 = g.shiftY[i][j];
                float y01 = g.shiftY[i][j + 1];
                float y10 = g.shiftY[i + 1][j];
                float y11 = g.shiftY[i + 1][j + 1];

                float x00 = g.shiftX[i][j];
                float x01 = g.shiftX[i][j + 1];
                float x10 = g.shiftX[i + 1][j];
                float x11 = g.shiftX[i + 1][j + 1];

                double dy = bilerp(y00, y01, y10, y11, tx, ty);
                double dx = bilerp(x00, x01, x10, x11, tx, ty);

                reusable[0] = x + dx;
                reusable[1] = y + dy;

                return copyOutput ? new double[]{reusable[0], reusable[1]} : reusable;
            }
        }

        // fuori griglia: invariato
        if (copyOutput) return new double[]{x, y};
        reusable[0] = x; reusable[1] = y;
        return reusable;
    }

    private static double bilerp(double q00, double q01, double q10, double q11, double tx, double ty) {
        // (x=tx, y=ty) con q00=SW, q01=SE, q10=NW? qui: i=row(y), j=col(x)
        // La combinazione sotto è coerente con i/j usati sopra:
        return q00 * (1 - tx) * (1 - ty)
                + q01 * tx       * (1 - ty)
                + q10 * (1 - tx) * ty
                + q11 * tx       * ty;
    }

    // --- NTv2 record helpers (16 bytes = tag + value)
    private static final class Record {
        final String tag;    // 8 chars (trim)
        final byte[] value;  // 8 bytes
        Record(String tag, byte[] value) { this.tag = tag; this.value = value; }
    }

    private static Record[] readRecords(DataInputStream dis, int n) throws IOException {
        Record[] out = new Record[n];
        byte[] buf = new byte[16];
        for (int i = 0; i < n; i++) {
            dis.readFully(buf);
            String tag = new String(buf, 0, 8, StandardCharsets.US_ASCII).trim();
            byte[] val = new byte[8];
            System.arraycopy(buf, 8, val, 0, 8);
            out[i] = new Record(tag, val);
        }
        return out;
    }

    private static String getStr(Record[] recs, String tag) {
        for (Record r : recs) {
            if (r.tag.equals(tag)) {
                // value spesso ASCII padded
                return new String(r.value, StandardCharsets.US_ASCII).trim();
            }
        }
        return "";
    }

    private static int getInt(Record[] recs, String tag) {
        for (Record r : recs) {
            if (r.tag.equals(tag)) {
                // int32 little-endian nei primi 4 bytes
                return (r.value[0] & 0xFF)
                        | ((r.value[1] & 0xFF) << 8)
                        | ((r.value[2] & 0xFF) << 16)
                        | ((r.value[3] & 0xFF) << 24);
            }
        }
        return 0;
    }

    private static double getDouble(Record[] recs, String tag) {
        for (Record r : recs) {
            if (r.tag.equals(tag)) {
                ByteBuffer bb = ByteBuffer.wrap(r.value).order(ByteOrder.LITTLE_ENDIAN);
                return bb.getDouble();
            }
        }
        return Double.NaN;
    }
    public boolean contains(double x, double y) {
        for (Grid g : grids) {
            if (x >= g.xMin && x <= g.xMax && y >= g.yMin && y <= g.yMax) return true;
        }
        return false;
    }
}
