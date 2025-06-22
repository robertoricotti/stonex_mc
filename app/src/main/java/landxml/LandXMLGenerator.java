package landxml;


import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import dxf.Point3D;

public class LandXMLGenerator {



    public static void generateLandXML(
            String filePath,
            String[] layerNames,
            List<List<Point3D>> surfacePoints,
            List<List<List<Point3D>>> polylines // Lista di polilinee per ogni layer
    ) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<LandXML xmlns=\"http://www.landxml.org/schema/LandXML-1.2\" version=\"1.2\">");
            writer.println("  <Project name=\"GeneratedLandXML\">");

            for (int layerIndex = 0; layerIndex < layerNames.length; layerIndex++) {
                String layerName = layerNames[layerIndex];
                writer.println("    <Surface name=\"" + layerName + "\">");
                writer.println("      <Definition>");

                // Aggiungere i punti
                List<Point3D> points = surfacePoints.get(layerIndex);
                writer.println("        <Pnts>");
                for (int i = 0; i < points.size(); i++) {
                    Point3D point = points.get(i);
                    writer.println("          <P id=\"" + (i + 1) + "\">" + point + "</P>");
                }
                writer.println("        </Pnts>");

                // Aggiungere i 3DFACE
                writer.println("        <Faces>");
                for (int i = 0; i < points.size() - 2; i++) { // Supponiamo triangolazione semplice
                    writer.println("          <F>" + (i + 1) + " " + (i + 2) + " " + (i + 3) + "</F>");
                }
                writer.println("        </Faces>");

                writer.println("      </Definition>");
                writer.println("    </Surface>");
            }

            // Aggiungere le polilinee
            if (polylines != null) {
                for (int layerIndex = 0; layerIndex < layerNames.length; layerIndex++) {
                    if (polylines.size() > layerIndex) {
                        List<List<Point3D>> layerPolylines = polylines.get(layerIndex);
                        writer.println("    <CoordGeom>");
                        for (List<Point3D> polyline : layerPolylines) {
                            writer.println("      <Line>");
                            for (int i = 0; i < polyline.size() - 1; i++) {
                                Point3D start = polyline.get(i);
                                Point3D end = polyline.get(i + 1);
                                writer.println("        <Start>" + start + "</Start>");
                                writer.println("        <End>" + end + "</End>");
                            }
                            writer.println("      </Line>");
                        }
                        writer.println("    </CoordGeom>");
                    }
                }
            }

            writer.println("  </Project>");
            writer.println("</LandXML>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo di test
   /* public static void main(String[] args) {
        String[] layerNames = {"Layer1", "Layer2"};

        // Superfici
        List<List<Point3D>> surfacePoints = List.of(
                List.of(
                        new Point3D(100, 200, 10),
                        new Point3D(150, 250, 15),
                        new Point3D(200, 300, 20)
                ),
                List.of(
                        new Point3D(300, 400, 25),
                        new Point3D(350, 450, 30),
                        new Point3D(400, 500, 35)
                )
        );

        // Polilinee
        List<List<List<Point3D>>> polylines = List.of(
                List.of(
                        List.of(
                                new Point3D(100, 200, 10),
                                new Point3D(150, 250, 15),
                                new Point3D(200, 300, 20)
                        )
                ),
                List.of(
                        List.of(
                                new Point3D(300, 400, 25),
                                new Point3D(350, 450, 30),
                                new Point3D(400, 500, 35)
                        )
                )
        );

        // Genera il file LandXML
        generateLandXML("output.xml", layerNames, surfacePoints, polylines);
    }*/
}

