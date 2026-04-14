package dxf;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Locale;

import utils.Utils;

public class DXFWriteMethods {
    private static int layerFCount;

    public static void testa(BufferedWriter writer, int nLayer) throws IOException {
        layerFCount = 10;
        writer.write("0\nSECTION\n2\nHEADER\n");
        //writer.write("9\n$ACADVER\n1\nAC1027\n"); // Versione AutoCAD 2013
        writer.write("0\nENDSEC\n");

        // Sezione TABLES con i layer
        writer.write("0\nSECTION\n2\nTABLES\n");
        writer.write("0\nTABLE\n2\nLAYER\n70\n" + nLayer + "\n100\nAcDbSymbolTable\n");

    }

    public static void writeLayer(BufferedWriter writer, String name, int color) throws IOException {
        layerFCount += 1;
        writer.write("0\nLAYER\n");
        writer.write("5\n" + layerFCount + "\n");
        writer.write("100\nAcDbSymbolTableRecord\n");
        writer.write("100\nAcDbLayerTableRecord\n");
        writer.write("2\n" + name + "\n");
        writer.write("70\n0\n");
        writer.write("62\n" + color + "\n");
        writer.write("6\nContinuous\n");
    }

    public static void endLayers(BufferedWriter writer) throws IOException {
        layerFCount = 0;
        writer.write("0\nENDTAB\n0\nENDSEC\n");
        // Sezione BLOCKS (vuota ma necessaria!)
        writer.write("0\nSECTION\n2\nBLOCKS\n0\nENDSEC\n");
    }

    public static void beginEntities(BufferedWriter writer) throws IOException {
        writer.write("0\nSECTION\n2\nENTITIES\n");
    }

    public static void coda(BufferedWriter writer) throws IOException {
        writer.write("0\nENDSEC\n0\nEOF\n");
        writer.flush();
        writer.close();
    }


    public static void createPolyline(BufferedWriter writer, Point3D start, Point3D end, double METER_TO_FEET_CONVERSION) throws IOException {
        writer.write("0\nPOLYLINE\n8\nLAYER_POLYLINE\n66\n1\n70\n8\n100\nAcDbEntity\n100\nAcDb3dPolyline\n");

        writer.write("0\nVERTEX\n8\nLAYER_POLYLINE\n100\nAcDbEntity\n100\nAcDbVertex\n100\nAcDb3dPolylineVertex\n70\n32\n");
        writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", start.x / METER_TO_FEET_CONVERSION, start.y / METER_TO_FEET_CONVERSION, start.z / METER_TO_FEET_CONVERSION));

        writer.write("0\nVERTEX\n8\nLAYER_POLYLINE\n100\nAcDbEntity\n100\nAcDbVertex\n100\nAcDb3dPolylineVertex\n70\n32\n");
        writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", end.x / METER_TO_FEET_CONVERSION, end.y / METER_TO_FEET_CONVERSION, end.z / METER_TO_FEET_CONVERSION));

        writer.write("0\nSEQEND\n");
    }

    public static void createPointsAB(BufferedWriter writer, Point3D[] points, double METER_TO_FEET_CONVERSION) throws IOException {
        char name = 'A';
        for (Point3D point : points) {
            point.description = " " + String.valueOf(name++) + " " + Utils.readUnitOfMeasureLITE(String.valueOf(point.getZ())) + " " + Utils.getMetriSimbol();
            double x = point.x / METER_TO_FEET_CONVERSION;
            double y = point.y / METER_TO_FEET_CONVERSION;
            double z = point.z / METER_TO_FEET_CONVERSION;
            // POINT
            writer.write("0\nPOINT\n8\nLAYER_POINT\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));

            // TEXT
            writer.write("0\nTEXT\n5\n0\n100\nAcDbEntity\n8\nLAYER_POINT\n100\nAcDbText\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));
            writer.write("40\n0.4\n41\n1\n");
            writer.write("1\n" + point.description + "\n");
            writer.write("50\n0\n");
        }
    }

    public static void createPointFree(BufferedWriter writer, Point3D[] points, double METER_TO_FEET_CONVERSION) throws IOException {
        for (Point3D point : points) {
            point.description = Utils.readUnitOfMeasureLITE(String.valueOf(point.getZ())) + " " + Utils.getMetriSimbol();
            double x = point.x / METER_TO_FEET_CONVERSION;
            double y = point.y / METER_TO_FEET_CONVERSION;
            double z = point.z / METER_TO_FEET_CONVERSION;
            // POINT
            writer.write("0\nPOINT\n8\nLAYER_POINT\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));

            // TEXT
            writer.write("0\nTEXT\n5\n0\n100\nAcDbEntity\n8\nLAYER_POINT\n100\nAcDbText\n");
            writer.write(String.format(Locale.US, "10\n%f\n20\n%f\n30\n%f\n", x, y, z));
            writer.write("40\n0.4\n41\n1\n");
            writer.write("1\n" + point.description + "\n");
            writer.write("50\n0\n");
        }


    }


    public static void writeHeader(BufferedWriter writer, int numeroTotaleDiLayer) throws IOException {
        writer.write("0\nSECTION\n2\nHEADER\n");

        writer.write("9\n$ACADVER\n1\nAC1018\n");  // AutoCAD 2007
        writer.write("9\n$INSBASE\n10\n0.0\n20\n0.0\n30\n0.0\n");
        writer.write("9\n$EXTMIN\n10\n0.0\n20\n0.0\n30\n0.0\n");
        writer.write("9\n$EXTMAX\n10\n100.0\n20\n100.0\n30\n100.0\n");
        writer.write("9\n$LIMMIN\n10\n0.0\n20\n0.0\n");
        writer.write("9\n$LIMMAX\n10\n100.0\n20\n100.0\n");

        writer.write("9\n$ORTHOMODE\n70\n0\n");
        writer.write("9\n$LTSCALE\n40\n1.0\n");
        writer.write("9\n$DIMEXO\n40\n0.625\n");
        writer.write("9\n$DIMDLI\n40\n3.75\n");
        writer.write("9\n$DIMTXT\n40\n2.5\n");
        writer.write("9\n$TEXTSIZE\n40\n2.5\n");

        writer.write("0\nENDSEC\n");
        // INIZIO SEZIONE TABLES e TABELLA LAYER
        writer.write("0\nSECTION\n2\nTABLES\n");
        writer.write("0\nTABLE\n2\nLAYER\n70\n" + numeroTotaleDiLayer + "\n");
    }

    public static void writeHeaderModern(BufferedWriter writer, int numeroTotaleDiLayer) throws IOException {
        writer.write("0\nSECTION\n2\nHEADER\n");
        writer.write("9\n$ACADVER\n1\nAC1018\n");
        writer.write("9\n$INSBASE\n10\n0.0\n20\n0.0\n30\n0.0\n");
        writer.write("9\n$EXTMIN\n10\n-1000.0\n20\n-1000.0\n30\n-1000.0\n");
        writer.write("9\n$EXTMAX\n10\n1000.0\n20\n1000.0\n30\n1000.0\n");
        writer.write("9\n$LIMMIN\n10\n0.0\n20\n0.0\n");
        writer.write("9\n$LIMMAX\n10\n420.0\n20\n297.0\n");
        writer.write("9\n$ORTHOMODE\n70\n0\n");
        writer.write("9\n$PDMODE\n70\n0\n");
        writer.write("9\n$PDSIZE\n40\n0\n");
        writer.write("9\n$LTSCALE\n40\n1.0\n");
        writer.write("9\n$DIMSTYLE\n2\nSTANDARD\n");
        writer.write("9\n$TEXTSTYLE\n7\nSTANDARD\n");
        writer.write("9\n$CLAYER\n8\n0\n");
        writer.write("0\nENDSEC\n");
        // INIZIO SEZIONE TABLES e TABELLA LAYER
        writer.write("0\nSECTION\n2\nTABLES\n");
        writer.write("0\nTABLE\n2\nLAYER\n70\n" + numeroTotaleDiLayer + "\n");
    }

    public static void writeHeaderAC1027(BufferedWriter writer, int numeroTotaleDiLayer) throws IOException {
        writer.write("0\nSECTION\n2\nHEADER\n");
        writer.write("9\n$ACADVER\n1\nAC1027\n");  // Versione AutoCAD 2013
        writer.write("9\n$INSBASE\n10\n0.0\n20\n0.0\n30\n0.0\n");  // Punto base di inserimento
        writer.write("9\n$EXTMIN\n10\n-1000.0\n20\n-1000.0\n30\n-1000.0\n");  // Estensione minima
        writer.write("9\n$EXTMAX\n10\n1000.0\n20\n1000.0\n30\n1000.0\n");  // Estensione massima
        writer.write("9\n$LIMMIN\n10\n0.0\n20\n0.0\n");  // Limiti minimi della vista
        writer.write("9\n$LIMMAX\n10\n420.0\n20\n297.0\n");  // Limiti massimi della vista
        writer.write("9\n$ORTHOMODE\n70\n0\n");  // Modalità ortogonale (0 = disabilitata)
        writer.write("9\n$PDMODE\n70\n0\n");  // Modalità punto (0 = punto semplice)
        writer.write("9\n$PDSIZE\n40\n0\n");  // Dimensione punto
        writer.write("9\n$LTSCALE\n40\n1.0\n");  // Scala delle linee
        writer.write("9\n$DIMSTYLE\n2\nSTANDARD\n");  // Stile di dimensionamento predefinito
        writer.write("9\n$TEXTSTYLE\n7\nSTANDARD\n");  // Stile di testo predefinito
        writer.write("9\n$CLAYER\n8\n0\n");  // Layer corrente (default è 0)
        writer.write("0\nENDSEC\n");
        // INIZIO SEZIONE TABLES e TABELLA LAYER
        writer.write("0\nSECTION\n2\nTABLES\n");
        writer.write("0\nTABLE\n2\nLAYER\n70\n" + numeroTotaleDiLayer + "\n");
    }
}
