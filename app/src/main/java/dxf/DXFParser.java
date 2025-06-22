
package dxf;

import static services.ReadProjectService.isFinishedDTM;
import static services.ReadProjectService.isFinishedPOINT;
import static services.ReadProjectService.isFinishedPOLY;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;


public class DXFParser {
    static Map<String, Integer> layerColors = new HashMap<>();

    public static DXFData parseDXF(String filePath, boolean isFeet) {
        DXFData dxfData = new DXFData();
        double conversionFactor = isFeet ? 0.3048006096 : 1.0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String colorCode = "";
            Layer currentLayer = null;
            String layerName = "";
            String line;
            Face3D currentFace = null;
            Polyline currentPolyline = null;
            Polyline_2D currentPolyline_2D = null;
            Point3D currentPoint = null;
            Circle currentCircle = null;
            Arc currentArc = null;
            Line currentLine = null;
            Point3D p1 = null, p2 = null, p3 = null, p4 = null;
            DxfText currentText = null;
            boolean isLayer = false;
            boolean vertexRead = false;
            boolean isAcDbLayerPresent = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                try {
                    switch (line) {
                        case "LAYER":
                            isLayer = true;
                            currentLayer = new Layer(filePath, null, null, true);

                            break;
                        case "3DFACE":
                            currentFace = new Face3D(null, null, null, null, MyColorClass.colorTriangle, null);
                            isLayer = false;
                            break;
                        case "POLYLINE":
                            currentPolyline = new Polyline();
                            isLayer = false;
                            break;
                        case "LWPOLYLINE":
                            currentPolyline_2D = new Polyline_2D();
                            isLayer = false;
                            break;
                        case "POINT":
                            currentPoint = new Point3D(0, 0, 0);
                            isLayer = false;
                            break;
                        case "TEXT":
                            currentText = new DxfText("", 0, 0, 0, -1, null);
                            isLayer = false;
                            break;
                        case "CIRCLE":
                            currentCircle = new Circle(new Point3D(0, 0, 0), 0, -1, null); // Inizializza il cerchio
                            isLayer = false;
                            break;
                        case "ARC":
                            currentArc = new Arc(new Point3D(0, 0, 0), 0, 0, 0, -1, null); // Inizializza l’arco
                            isLayer = false;
                            break;
                        case "LINE":
                            currentLine = new Line(new Point3D(0, 0, 0), new Point3D(0, 0, 0), -1, null);
                            isLayer = false;
                            break;
                        default:
                            if (isLayer) {
                                if (line.equals("AcDbLayerTableRecord")) {
                                    isAcDbLayerPresent = true;
                                    br.readLine();
                                    layerName = br.readLine().trim();
                                    currentLayer.setProjName(filePath);
                                    currentLayer.setLayerName(layerName);
                                }
                                if (line.equals("2") && !isAcDbLayerPresent) {
                                    if (currentLayer == null) {
                                        currentLayer = new Layer(filePath, null, null, true); // Inizializzazione di fallback
                                    }
                                    layerName = br.readLine().trim(); // Leggi il nome del layer
                                    currentLayer.setProjName(filePath); // Imposta il percorso del progetto
                                    currentLayer.setLayerName(layerName); // Imposta il nome del layer
                                }
                                if (line.equals("62")) {
                                    try {
                                        colorCode = br.readLine().trim();
                                        currentLayer.setColorState(Integer.parseInt(colorCode));
                                        dxfData.addLayer(currentLayer);
                                        currentLayer = null;
                                        int color = AutoCADColor.getColor(colorCode); // Ottieni il colore
                                        layerColors.put(layerName, color); // Associa il colore al layer

                                    } catch (Exception e) {
                                        int color = AutoCADColor.getColor("0"); // Ottieni il colore
                                        layerColors.put(layerName, color); // Associa il colore al layer
                                    }
                                }
                            }
                            // Elaborazione delle entità
                            if (currentFace != null) {
                                switch (line) {
                                    case "8":

                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName); // Ottieni colore dal layer
                                        if (color != null) {
                                            if (color != -1) {
                                                MyColorClass.colorTriangle = color;
                                            }
                                            currentFace.setLayer(new Layer(filePath, layerName, color, true));
                                        } else {
                                            currentFace.setLayer(new Layer(filePath, layerName, -1, false));
                                        }

                                        break;
                                    case "10":
                                        p1 = new Point3D(readDouble(br) * conversionFactor, 0, 0);
                                        break;
                                    case "20":
                                        if (p1 != null)
                                            p1.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "30":
                                        if (p1 != null)
                                            p1.z = readDouble(br) * conversionFactor;
                                        if (p1 != null) currentFace.p1 = p1;
                                        break;
                                    case "11":
                                        p2 = new Point3D(readDouble(br) * conversionFactor, 0, 0);
                                        break;
                                    case "21":
                                        if (p2 != null)
                                            p2.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "31":
                                        if (p2 != null)
                                            p2.z = readDouble(br) * conversionFactor;
                                        if (p2 != null) currentFace.p2 = p2;
                                        break;
                                    case "12":
                                        p3 = new Point3D(readDouble(br) * conversionFactor, 0, 0);
                                        break;
                                    case "22":
                                        if (p3 != null)
                                            p3.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "32":
                                        if (p3 != null)
                                            p3.z = readDouble(br) * conversionFactor;
                                        if (p3 != null) currentFace.p3 = p3;
                                        break;
                                    case "13":
                                        p4 = new Point3D(readDouble(br) * conversionFactor, 0, 0);
                                        break;
                                    case "23":
                                        if (p4 != null)
                                            p4.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "33":
                                        if (p4 != null)
                                            p4.z = readDouble(br) * conversionFactor;
                                        if (p4 != null) currentFace.p4 = p4;
                                        break;
                                }
                                if (currentFace.p1 != null && currentFace.p2 != null && currentFace.p3 != null) {
                                    if (currentFace.p4 == null) {
                                        currentFace.p4 = currentFace.p3;
                                    }

                                    if (currentFace.getLayer().getColorState() != -1) {
                                        // Aggiungi direttamente l'oggetto Face3D
                                        dxfData.addFace(currentFace); // Metodo per aggiungere Face3D
                                    }
                                    currentFace = null;
                                }
                            } else if (currentPolyline != null) {
                                if (line.equals("8") && !vertexRead) {
                                    layerName = br.readLine().trim();
                                    Integer color = layerColors.get(layerName); // Ottieni colore dal layer
                                    if (color != null) {
                                        if (color != -1) {
                                            currentPolyline.setLineColor(color);
                                        }
                                        currentPolyline.setLayer(new Layer(filePath, layerName, color, true));
                                    } else {
                                        currentPolyline.setLayer(new Layer(filePath, layerName, -1, false));
                                    }
                                    vertexRead = true;

                                }

                                if (line.equals("VERTEX")) {
                                    while ((line = br.readLine()) != null && !line.trim().equals("SEQEND")) {
                                        line = line.trim();
                                        switch (line) {
                                            case "8":
                                                // se layer name =0 assegna quello della polilinea

                                                break;
                                            case "10":
                                                double x = readDouble(br) * conversionFactor;
                                                currentPolyline.vertices.add(new Point3D(x, 0, 0));
                                                break;
                                            case "20":
                                                if (!currentPolyline.vertices.isEmpty()) {
                                                    Point3D lastVertex = currentPolyline.vertices.get(currentPolyline.vertices.size() - 1);
                                                    lastVertex.y = readDouble(br) * conversionFactor;
                                                }
                                                break;
                                            case "30":
                                                if (!currentPolyline.vertices.isEmpty()) {
                                                    Point3D lastVertex = currentPolyline.vertices.get(currentPolyline.vertices.size() - 1);
                                                    lastVertex.z = readDouble(br) * conversionFactor;
                                                }
                                                break;
                                        }
                                    }
                                    if (line.trim().equals("SEQEND")) {
                                        vertexRead = false;
                                    }
                                    if (currentPolyline.getLayer().getColorState() != -1) {
                                        dxfData.addPolyline(currentPolyline);
                                    }
                                    currentPolyline = null;
                                }
                            } else if (currentPolyline_2D != null) {
                                int expectedVertexCount = 0; // Variabile per contare i vertici attesi
                                int currentVertexCount = 0;  // Contatore per i vertici elaborati
                                while ((line = br.readLine()) != null) {
                                    line = line.trim();

                                    if (line.equals("0") && currentVertexCount >= expectedVertexCount && expectedVertexCount > 0) {
                                        // Solo interrompi quando abbiamo letto tutti i vertici attesi
                                        break;
                                    }
                                    switch (line) {
                                        case "8":

                                            layerName = br.readLine().trim();
                                            Integer color = layerColors.get(layerName); // Ottieni colore dal layer
                                            if (color != null) {
                                                if (color != -1) {
                                                    currentPolyline_2D.setLineColor(color);
                                                }
                                                currentPolyline_2D.setLayer(new Layer(filePath, layerName, color, true));
                                            } else {
                                                currentPolyline_2D.setLayer(new Layer(filePath, layerName, -1, false));
                                            }

                                            break;
                                        case "90": // Numero di vertici attesi
                                            String verticiLine = br.readLine().trim();
                                            try {
                                                expectedVertexCount = Integer.parseInt(verticiLine);
                                            } catch (NumberFormatException e) {
                                                break; // Salta al prossimo case se c'è un errore
                                            }
                                            break;

                                        case "10": // Coordinata X del vertice
                                            String xLine = br.readLine().trim();
                                            double x;
                                            try {
                                                x = Double.parseDouble(xLine) * conversionFactor;
                                            } catch (NumberFormatException e) {
                                                break;
                                            }
                                            currentPolyline_2D.addVertex(new Point3D(x, 0, 0)); // Aggiunge il vertice con X
                                            break;

                                        case "20": // Coordinata Y del vertice
                                            String yLine = br.readLine().trim();
                                            double y;
                                            try {
                                                y = Double.parseDouble(yLine) * conversionFactor;
                                            } catch (NumberFormatException e) {
                                                break;
                                            }
                                            if (!currentPolyline_2D.getVertices().isEmpty()) {
                                                Point3D lastVertex = currentPolyline_2D.getVertices().get(currentPolyline_2D.getVertices().size() - 1);
                                                lastVertex.y = y; // Assegna la Y all'ultimo vertice aggiunto
                                                lastVertex.z = 0;
                                                currentVertexCount++; // Incrementa il contatore dei vertici elaborati
                                            }
                                            break;
                                        case "42": // Bulge per rappresentare l'arco
                                            String bulgeLine = br.readLine().trim();
                                            double bulge;
                                            try {
                                                bulge = Double.parseDouble(bulgeLine);
                                            } catch (NumberFormatException e) {
                                                break;
                                            }
                                            if (!currentPolyline_2D.getVertices().isEmpty()) {
                                                Point3D lastVertex = currentPolyline_2D.getVertices().get(currentPolyline_2D.getVertices().size() - 1);
                                                lastVertex.setBulge(bulge); // Imposta il bulge sul vertice attuale
                                            }
                                            break;

                                        default:
                                            break;
                                    }
                                    // Controlla se abbiamo raggiunto il numero di vertici attesi
                                    if (currentVertexCount >= expectedVertexCount && expectedVertexCount > 0) {
                                        break; // Uscita dal ciclo se abbiamo elaborato tutti i vertici
                                    }
                                }

                                if (!currentPolyline_2D.getVertices().isEmpty()) {
                                    if (currentPolyline_2D.getLayer().getColorState() != -1) {
                                        dxfData.addPolyline2D(currentPolyline_2D);
                                    }
                                }

                                currentPolyline_2D = null; // Reset per la prossima polilinea
                            } else if (currentPoint != null) {
                                switch (line) {
                                    case "8": // Layer del punto
                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName); // Ottieni colore dal layer
                                        Layer pointLayer;

                                        if (color != null) {
                                            if (color != -1) {
                                                currentPoint.setColore(color);
                                            }
                                            pointLayer = new Layer(filePath, layerName, color, true);
                                        } else {
                                            pointLayer = new Layer(filePath, layerName, -1, false);
                                        }
                                        currentPoint.setLayer(pointLayer); // Assegna il layer al punto
                                        break;

                                    case "10": // Coordinata X del punto
                                        currentPoint.x = readDouble(br) * conversionFactor;
                                        break;

                                    case "20": // Coordinata Y del punto
                                        currentPoint.y = readDouble(br) * conversionFactor;
                                        currentPoint.z = 0; // Z predefinito
                                        if (currentPoint.x != 0 || currentPoint.y != 0) {
                                            if (currentPoint.getLayer().getColorState() != -1) {
                                                dxfData.addPoint(currentPoint);
                                            }
                                        }
                                        break;

                                    case "30": // Coordinata Z del punto
                                        currentPoint.z = readDouble(br) * conversionFactor;
                                        break;

                                    case "0": // Fine del punto
                                        currentPoint = null; // Reset del punto corrente
                                        break;
                                }
                            } else if (currentText != null) {
                                switch (line) {
                                    case "8": // Layer per il testo
                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName); // Ottieni colore dal layer
                                        Layer textLayer;
                                        if (color != null) {
                                            if (color != -1) {
                                                currentText.setColore(color);
                                            }
                                            textLayer = new Layer(filePath, layerName, color, true);
                                        } else {
                                            textLayer = new Layer(filePath, layerName, -1, false);
                                        }

                                        currentText.setLayer(textLayer); // Assegna il layer al testo
                                        break;

                                    case "10": // Coordinata X del testo
                                        currentText.x = readDouble(br) * conversionFactor;
                                        break;

                                    case "20": // Coordinata Y del testo
                                        currentText.y = readDouble(br) * conversionFactor;
                                        break;

                                    case "30": // Coordinata Z del testo
                                        currentText.z = readDouble(br) * conversionFactor;
                                        break;
                                    case "40": // Altezza del testo
                                        double altezza = (readDouble(br));

                                        break;
                                    case "5":
                                    case "100":
                                    case "39":
                                    case "41":
                                    case "51":
                                    case "7":
                                    case "71":
                                    case "72":
                                    case "11":
                                    case "21":
                                    case "31":
                                    case "210":
                                    case "220":
                                    case "230":
                                    case "73":
                                        br.readLine(); // salta il valore
                                        break;
                                    case "1":
                                        String textContent = br.readLine().trim(); // << Qui leggi il vero testo
                                        currentText.setText(textContent);
                                        if (currentText.getText() != null && !currentText.getText().isEmpty()) {
                                            if(currentText.getLayer()!=null) {
                                                if (currentText.getLayer().getColorState() != -1) {
                                                    dxfData.addText(currentText);
                                                }
                                            }
                                        }
                                        break;

                                    case "50": // Rotazione del testo
                                        double rotation = (readDouble(br));
                                        break;
                                    case "0": // Fine del blocco testo
                                        currentText = null;
                                        break;
                                }
                            } else if (currentCircle != null) {
                                switch (line) {
                                    case "8": // Layer

                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName);
                                        if (color != null) {
                                            if (color != -1) {
                                                currentCircle.setColor(color);
                                            }
                                            currentCircle.setLayer(new Layer(filePath, layerName, color, true));
                                        } else {
                                            currentCircle.setLayer(new Layer(filePath, layerName, -1, false));
                                        }

                                        break;
                                    case "10": // Coordinata X del centro
                                        currentCircle.center.x = readDouble(br) * conversionFactor;
                                        break;
                                    case "20": // Coordinata Y del centro
                                        currentCircle.center.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "30": // Coordinata Z del centro
                                        currentCircle.center.z = readDouble(br) * conversionFactor;
                                        break;
                                    case "40": // Raggio
                                        currentCircle.radius = readDouble(br) * conversionFactor;
                                        break;
                                }
                                if (currentCircle.radius > 0) {
                                    if (currentCircle.getLayer().getColorState() != -1) {
                                        dxfData.addCircle(currentCircle); // Aggiunge il cerchio al DXF
                                    }
                                    currentCircle = null; // Reset per il prossimo cerchio
                                }
                            } else if (currentArc != null) {
                                switch (line) {
                                    case "8": // Layer

                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName);
                                        if (color != null) {
                                            if (color != -1) {
                                                currentArc.setColor(color);
                                            }
                                            currentArc.setLayer(new Layer(filePath, layerName, color, true));
                                        } else {
                                            currentArc.setLayer(new Layer(filePath, layerName, -1, false));
                                        }

                                        break;
                                    case "10": // Coordinata X del centro
                                        currentArc.center.x = readDouble(br) * conversionFactor;
                                        break;
                                    case "20": // Coordinata Y del centro
                                        currentArc.center.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "30": // Coordinata Z del centro
                                        currentArc.center.z = readDouble(br) * conversionFactor;
                                        break;
                                    case "40": // Raggio
                                        currentArc.radius = readDouble(br) * conversionFactor;
                                        break;
                                    case "50": // Angolo iniziale
                                        currentArc.setStartAngle(readDouble(br));

                                        break;
                                    case "51": // Angolo finale

                                        currentArc.setEndAngle(readDouble(br));

                                        break;
                                    case "0": // Fine blocco arco
                                        if (currentArc.radius > 0 && currentArc.startAngle != currentArc.endAngle) {
                                            if (currentArc.getLayer().getColorState() != -1) {
                                                dxfData.addArc(currentArc); // Aggiunge l'arco al DXF
                                            }
                                        }
                                        currentArc = null; // Reset per il prossimo arco
                                        break;
                                }

                            } else if (currentLine != null) {
                                switch (line) {
                                    case "8":
                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName);
                                        if (color != null) {
                                            if (color != -1) {
                                                currentLine.setColor(color);
                                            }
                                            currentLine.setLayer(new Layer(filePath, layerName, color, true));
                                        } else {
                                            currentLine.setLayer(new Layer(filePath, layerName, -1, false));
                                        }

                                        break;
                                    case "10":
                                        currentLine.start.x = readDouble(br) * conversionFactor;
                                        break;
                                    case "20":
                                        currentLine.start.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "30":
                                        currentLine.start.z = 0;
                                        break;
                                    case "11":
                                        currentLine.end.x = readDouble(br) * conversionFactor;
                                        break;
                                    case "21":
                                        currentLine.end.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "31":
                                        currentLine.end.z = 0;
                                        break;
                                    case "0":
                                        //
                                        try {
                                            if (currentLine.start.x != currentLine.end.x || currentLine.start.y != currentLine.end.y) {
                                                if (currentLine.getLayer().getColorState() != null)
                                                    if (currentLine.getLayer().getColorState() > -1) {
                                                        dxfData.addLine(currentLine);
                                                    }
                                                currentLine = null;
                                            }
                                        } catch (Exception e) {
                                            Log.e("ERROR_DXF", Log.getStackTraceString(e));
                                        }

                                        break;
                                }

                            }
                            break;
                    }

                } catch (NumberFormatException e) {
                    ReadProjectService.parserStatus = e.toString() + "\n";
                    if (filePath.equals(DataSaved.progettoSelected)) {
                        isFinishedDTM = true;
                    }
                    if (filePath.equals(DataSaved.progettoSelected_POLY)) {
                        isFinishedPOLY = true;
                    }
                    if (filePath.equals(DataSaved.progettoSelected_POINT)) {
                        isFinishedPOINT = true;
                    }
                }


            }

        } catch (IOException e) {
            ReadProjectService.parserStatus = e.toString() + "\n";
            if (filePath.equals(DataSaved.progettoSelected)) {
                isFinishedDTM = true;
            }
            if (filePath.equals(DataSaved.progettoSelected_POLY)) {
                isFinishedPOLY = true;
            }
            if (filePath.equals(DataSaved.progettoSelected_POINT)) {
                isFinishedPOINT = true;
            }
            e.printStackTrace();
        }
        if (filePath.equals(DataSaved.progettoSelected)) {
            isFinishedDTM = true;
        }
        if (filePath.equals(DataSaved.progettoSelected_POLY)) {
            isFinishedPOLY = true;
        }
        if (filePath.equals(DataSaved.progettoSelected_POINT)) {
            isFinishedPOINT = true;
        }
        return dxfData;
    }

    // Metodo ausiliario per leggere un double
    private static double readDouble(BufferedReader br) throws IOException {
        String line = br.readLine().trim();
        return Double.parseDouble(line);
    }
}