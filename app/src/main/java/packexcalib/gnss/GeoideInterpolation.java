package packexcalib.gnss;

import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GeoideInterpolation {

    private static final int OFFSET_1 = 48; // Dimensione dell'header in byte
    private static final int OFFSET_2 = 146; // Offset iniziale dei dati nel file

    private double latMin;
    private double latMax;
    private double lonMin;
    private double lonMax;
    private double dLat;
    private double dLon;
    private int quadroY;
    private int quadroX;
    private double[] valoriLat = new double[4];
    private double[] valoriLon = new double[4];
    private double[] valoriCasella = new double[4];
    private int posizioneInGeo = -1;
    private int contaPosizione = 0;
    private boolean rovescio;
    private double valoreErrato = -9999;
    private String filePath;

    public GeoideInterpolation(String filePath) {
        this.filePath = filePath;
    }

    public void readHeader() throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);

            fis.getChannel().position(OFFSET_1);

            fis.read(buffer);

            latMin = byteBuffer.getDouble();
            byteBuffer.rewind();
           // Log.d("GeoideInterpolation", "latMin: " + latMin);

            fis.read(buffer);
            latMax = byteBuffer.getDouble();
            byteBuffer.rewind();
           // Log.d("GeoideInterpolation", "latMax: " + latMax);

            fis.read(buffer);
            lonMin = byteBuffer.getDouble();
            byteBuffer.rewind();
            //Log.d("GeoideInterpolation", "lonMin: " + lonMin);

            fis.read(buffer);
            lonMax = byteBuffer.getDouble();
            byteBuffer.rewind();
            //Log.d("GeoideInterpolation", "lonMax: " + lonMax);

            fis.read(buffer);
            dLat = byteBuffer.getDouble();
            byteBuffer.rewind();
            //Log.d("GeoideInterpolation", "dLat: " + dLat);

            fis.read(buffer);
            dLon = byteBuffer.getDouble();
            byteBuffer.rewind();
            //Log.d("GeoideInterpolation", "dLon: " + dLon);

            buffer = new byte[4];
            byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);

            fis.read(buffer);
            quadroY = byteBuffer.getInt();
            byteBuffer.rewind();
            //Log.d("GeoideInterpolation", "quadroY: " + quadroY);

            fis.read(buffer);
            quadroX = byteBuffer.getInt();
           // Log.d("GeoideInterpolation", "quadroX: " + quadroX);
        }
    }

    public boolean internalLetturaGeoide(double latitudine, double longitudine, double altitudine, double[] quota,boolean rovescio) {
        this.rovescio=rovescio;
        try {
            //Log.d("GeoideInterpolation", "Latitudine: " + latitudine + ", Longitudine: " + longitudine + ", Altitudine: " + altitudine);

            double lo = longitudine;
            if (lo < 0) {
                lo = 360 + lo;
            }
            if (lonMax < 0) {
                lonMax = 360 + lonMax;
            }
            if (lonMin < 0) {
                lonMin = 360 + lonMin;
                if (lonMax < lonMin) {
                    lonMax = 360 + lonMax;
                }
            }

            //Log.d("GeoideInterpolation", "lonMin: " + lonMin + ", lonMax: " + lonMax);

            double dy = rovescio ? latMax - latitudine : latitudine - latMin;
            int numeroV = (int) Math.floor(dy / dLat);
            double dx = lo - lonMin;
            if (dx < 0) {
                dx = 360 + dx;
            }
            int numeroH = (int) Math.floor(dx / dLon) + 1;

            //Log.d("GeoideInterpolation", "dx: " + dx + ", dy: " + dy + ", numeroH: " + numeroH + ", numeroV: " + numeroV);

            if (numeroH < 0 || numeroV < 0 || numeroH >= quadroX || numeroV >= quadroY - 1) {
                //Log.d("GeoideInterpolation", "Coordinate fuori dal range del quadro.");
                return false;
            }

            int numero = numeroV * quadroX + numeroH - 1;

            double gg0, gg1, gg2, gg3;
            int luogoTrovato = -1;

            if (posizioneInGeo > -1) {
                for (int i = 0; i < contaPosizione; i++) {
                    if (latitudine >= valoriLat[i] && latitudine <= valoriLat[i] &&
                            lo >= valoriLon[i] && lo <= valoriLon[i]) {
                        luogoTrovato = i;
                        break;
                    }
                }
                if (luogoTrovato == -1) {
                    posizioneInGeo = -1;
                }
            }

            if (posizioneInGeo == -1) {
                posizioneInGeo = numero;
                prepareCasellaValues(numeroV, numeroH, numero);
            }

            double dlatMis = rovescio ? latMax - numeroV * dLat - latitudine : latitudine - latMin - numeroV * dLat;
            double dlonMis = dx - (numeroH - 1) * dLon;
            int conctg = 0;
            double sommaGG = 0;

            gg0 = valoriCasella[0];
            if (gg0 != valoreErrato) {
                conctg++;
                sommaGG += gg0;
            }
            gg1 = valoriCasella[1];
            if (gg1 != valoreErrato) {
                conctg++;
                sommaGG += gg1;
            }
            gg2 = valoriCasella[2];
            if (gg2 != valoreErrato) {
                conctg++;
                sommaGG += gg2;
            }
            gg3 = valoriCasella[3];
            if (gg3 != valoreErrato) {
                conctg++;
                sommaGG += gg3;
            }

            //Log.d("GeoideInterpolation", "gg0: " + gg0 + ", gg1: " + gg1 + ", gg2: " + gg2 + ", gg3: " + gg3);

            if (conctg == 4) {
                double dq;
                double alfa = (gg1 - gg0) / dLon;
                double beta = (gg2 - gg0) / dLat;
                double gamma = (gg3 - gg2 - gg1 + gg0) / (dLat * dLon);
                dq = alfa * dlonMis + beta * dlatMis + gamma * dlonMis * dlatMis + gg0;
                quota[0] = altitudine - dq;
               // Log.d("GeoideInterpolation", "Interpolazione riuscita, dq: " + dq + ", Quota: " + quota[0]);
            } else {
                if (conctg > 0) {
                    double dq = sommaGG / conctg;
                    quota[0] = altitudine - dq;
                    //Log.d("GeoideInterpolation", "Interpolazione parziale, dq: " + dq + ", Quota: " + quota[0]);
                } else {
                    quota[0] = 0;
                    //Log.d("GeoideInterpolation", "Nessuna quota valida trovata.");
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("GeoideInterpolation", e.toString());
            return false;
        }
    }

    private void prepareCasellaValues(int numeroV, int numeroH, int numero) throws IOException {
        double lat1, lat2, lat3, lat4;
        double lon1, lon2, lon3, lon4;
        if (rovescio) {
            lat1 = latMax - numeroV * dLat;
            lat2 = latMax - (numeroV + 1) * dLat;
            lat3 = latMax - (numeroV + 2) * dLat;
            lat4 = latMax - (numeroV + 3) * dLat;
        } else {
            lat1 = latMin + (numeroV - 1) * dLat;
            lat2 = latMin + numeroV * dLat;
            lat3 = latMin + (numeroV + 1) * dLat;
            lat4 = latMin + (numeroV + 2) * dLat;
        }

        lon1 = lonMin + (numeroH - 1) * dLon;
        lon2 = lonMin + numeroH * dLon;
        lon3 = lonMin + (numeroH + 1) * dLon;
        lon4 = lonMin + (numeroH + 2) * dLon;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);

            contaPosizione = 1;
            valoriLat[0] = lat2;
            valoriLat[1] = lat3;
            valoriLon[0] = lon2;
            valoriLon[1] = lon3;

            // Lettura dei valori della casella
            fis.getChannel().position(OFFSET_2 + numero * 4L);
            fis.read(buffer);
            valoriCasella[0] = byteBuffer.getFloat();
            byteBuffer.rewind();
            //Log.d("GeoideInterpolation", "valoriCasella[0]: " + valoriCasella[0]);

            fis.read(buffer);
            valoriCasella[1] = byteBuffer.getFloat();
            byteBuffer.rewind();
            //Log.d("GeoideInterpolation", "valoriCasella[1]: " + valoriCasella[1]);

            fis.read(buffer);
            valoriCasella[2] = byteBuffer.getFloat();
            byteBuffer.rewind();
            //Log.d("GeoideInterpolation", "valoriCasella[2]: " + valoriCasella[2]);

            fis.read(buffer);
            valoriCasella[3] = byteBuffer.getFloat();
            //Log.d("GeoideInterpolation", "valoriCasella[3]: " + valoriCasella[3]);
        }
    }
}
