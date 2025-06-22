package packexcalib.gnss;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.DataInputStream;

public class GeoideHeader {
    private double latMin;
    private double latMax;
    private double lonMin;
    private double lonMax;
    private double dLat;
    private double dLon;
    private int quadroY;
    private int quadroX;

    public void readHeader(String filePath, long offset) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             DataInputStream dis = new DataInputStream(fis)) {
            fis.getChannel().position(offset);
            latMin = dis.readDouble();
            latMax = dis.readDouble();
            lonMin = dis.readDouble();
            lonMax = dis.readDouble();
            dLat = dis.readDouble();
            dLon = dis.readDouble();
            quadroY = dis.readInt();
            quadroX = dis.readInt();
        }
    }

    public double getdLat() {
        return dLat;
    }

    public double getdLon() {
        return dLon;
    }

    public double getLatMax() {
        return latMax;
    }

    public double getLatMin() {
        return latMin;
    }

    public double getLonMax() {
        return lonMax;
    }

    public double getLonMin() {
        return lonMin;
    }

    public int getQuadroX() {
        return quadroX;
    }

    public int getQuadroY() {
        return quadroY;
    }

}
