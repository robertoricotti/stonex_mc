package packexcalib.gnss;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.nio.channels.FileChannel;

public class GeoideInterpolation {

    private static final int OFFSET_1 = 48;   // dimensione header
    private static final int OFFSET_2 = 146;  // offset dati

    private double latMin, latMax, lonMin, lonMax, dLat, dLon;
    private int quadroY, quadroX;
    private boolean rovescio;
    private double valoreErrato = -9999;
    private String filePath;

    // CACHE: tutta la griglia caricata in RAM
    private float[][] grid;

    // buffer riusato per compatibilità con il tuo codice
    private double[] valoriLat = new double[4];
    private double[] valoriLon = new double[4];
    private double[] valoriCasella = new double[4];
    private int posizioneInGeo = -1;
    private int contaPosizione = 0;

    public GeoideInterpolation(String filePath) {
        this.filePath = filePath;
    }

    /** Legge header + tutti i dati del geoide in memoria una sola volta */
    public void readHeader() throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            FileChannel ch = fis.getChannel();
            ByteBuffer buf8 = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            ByteBuffer buf4 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);

            // === Lettura header ===
            ch.position(OFFSET_1);

            ch.read(buf8); buf8.flip(); latMin = buf8.getDouble(); buf8.clear();
            ch.read(buf8); buf8.flip(); latMax = buf8.getDouble(); buf8.clear();
            ch.read(buf8); buf8.flip(); lonMin = buf8.getDouble(); buf8.clear();
            ch.read(buf8); buf8.flip(); lonMax = buf8.getDouble(); buf8.clear();
            ch.read(buf8); buf8.flip(); dLat = buf8.getDouble(); buf8.clear();
            ch.read(buf8); buf8.flip(); dLon = buf8.getDouble(); buf8.clear();

            ch.read(buf4); buf4.flip(); quadroY = buf4.getInt(); buf4.clear();
            ch.read(buf4); buf4.flip(); quadroX = buf4.getInt(); buf4.clear();

            // === Lettura completa griglia ===
            int totalValues = quadroY * quadroX;
            grid = new float[quadroY][quadroX];
            ByteBuffer data = ByteBuffer.allocateDirect(totalValues * 4).order(ByteOrder.LITTLE_ENDIAN);

            ch.position(OFFSET_2);
            ch.read(data);
            data.flip();

            for (int y = 0; y < quadroY; y++) {
                for (int x = 0; x < quadroX; x++) {
                    grid[y][x] = data.getFloat();
                }
            }
        }
    }

    /** Interpolazione interna (identica alla tua API originale) */
    public boolean internalLetturaGeoide(double latitudine, double longitudine, double altitudine, double[] quota, boolean rovescio) {
        this.rovescio = rovescio;
        try {
            if (grid == null) return false;

            double lo = longitudine;
            if (lo < 0) lo = 360 + lo;
            if (lonMax < 0) lonMax = 360 + lonMax;
            if (lonMin < 0) lonMin = 360 + lonMin;
            if (lonMax < lonMin) lonMax = 360 + lonMax;

            double dy = rovescio ? latMax - latitudine : latitudine - latMin;
            int numeroV = (int) Math.floor(dy / dLat);
            double dx = lo - lonMin;
            if (dx < 0) dx = 360 + dx;
            int numeroH = (int) Math.floor(dx / dLon);

            // Fuori dai limiti
            if (numeroH < 0 || numeroV < 0 || numeroH >= quadroX - 1 || numeroV >= quadroY - 1)
                return false;

            // Lettura 4 punti della cella
            float g00 = grid[numeroV][numeroH];
            float g10 = grid[numeroV][numeroH + 1];
            float g01 = grid[numeroV + 1][numeroH];
            float g11 = grid[numeroV + 1][numeroH + 1];

            if (g00 == valoreErrato || g10 == valoreErrato || g01 == valoreErrato || g11 == valoreErrato)
                return false;

            double latFrac = (dy / dLat) - numeroV;
            double lonFrac = (dx / dLon) - numeroH;

            // Interpolazione bilineare
            double dq =
                    g00 * (1 - latFrac) * (1 - lonFrac) +
                            g10 * (1 - latFrac) * lonFrac +
                            g01 * latFrac * (1 - lonFrac) +
                            g11 * latFrac * lonFrac;

            quota[0] = altitudine - dq;
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Compatibilità: metodo placeholder (non serve più, ma lasciato per non rompere API) */
    private void prepareCasellaValues(int numeroV, int numeroH, int numero) throws IOException {
        // Non serve più con cache in RAM
    }
}
