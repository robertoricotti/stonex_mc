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

public class DXFParser_20 {

    // Colori associati ai layer (nome layer -> colore ARGB)
    static Map<String, Integer> layerColors = new HashMap<>();

    public static DXFData parseDXF(String filePath, double conversionFactor) {
        DXFData dxfData = new DXFData();

        // Sempre reset della mappa colori per evitare "memory" tra file diversi
        layerColors.clear();

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
            DxfText currentMText = null; // MTEXT mappato su DxfText

            // BLOCK / INSERT
            DxfBlock currentBlock = null;     // se non null, siamo dentro una definizione di blocco
            DxfInsert currentInsert = null;   // entità INSERT corrente

            boolean isLayer = false;
            boolean vertexRead = false;
            boolean isAcDbLayerPresent = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                try {
                    switch (line) {
                        // =========================
                        //   RICONOSCIMENTO ENTITÀ
                        // =========================
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

                        case "MTEXT":
                            // MTEXT: lo gestiamo come DxfText (multi-linea)
                            currentMText = new DxfText("", 0, 0, 0, -1, null);
                            isLayer = false;
                            break;

                        case "CIRCLE":
                            currentCircle = new Circle(new Point3D(0, 0, 0), 0, -1, null);
                            isLayer = false;
                            break;

                        case "ARC":
                            currentArc = new Arc(new Point3D(0, 0, 0), 0, 0, 0, -1, null);
                            isLayer = false;
                            break;

                        case "LINE":
                            currentLine = new Line(new Point3D(0, 0, 0), new Point3D(0, 0, 0), -1, null);
                            isLayer = false;
                            break;

                        // =========================
                        //   BLOCCO: INIZIO / FINE
                        // =========================
                        case "BLOCK":
                            // nuova definizione di blocco
                            currentBlock = new DxfBlock(null); // nome lo leggiamo più avanti (codice 2)
                            isLayer = false;
                            break;

                        case "ENDBLK":
                            // fine definizione blocco
                            if (currentBlock != null) {
                                dxfData.addBlock(currentBlock);
                                currentBlock = null;
                            }
                            break;

                        // =========================
                        //   ENTITÀ INSERT
                        // =========================
                        case "INSERT":
                            currentInsert = new DxfInsert(null); // nome blocco da codice 2
                            isLayer = false;
                            break;

                        default:
                            // ========================
                            //   SEZIONE LAYER/TABLES
                            // ========================
                            if (isLayer) {
                                if (line.equals("AcDbLayerTableRecord")) {
                                    isAcDbLayerPresent = true;
                                    br.readLine();                  // group code (es. "2")
                                    layerName = br.readLine().trim(); // nome layer

                                    currentLayer.setProjName(filePath);
                                    currentLayer.setLayerName(layerName);
                                }

                                if (line.equals("2") && !isAcDbLayerPresent) {
                                    if (currentLayer == null) {
                                        currentLayer = new Layer(filePath, null, null, true);
                                    }
                                    layerName = br.readLine().trim();
                                    currentLayer.setProjName(filePath);
                                    currentLayer.setLayerName(layerName);
                                }

                                if (line.equals("62")) {
                                    try {
                                        colorCode = br.readLine().trim();
                                        currentLayer.setColorState(Integer.parseInt(colorCode));
                                        dxfData.addLayer(currentLayer);
                                        int color = AutoCADColor.getColor(colorCode);
                                        layerColors.put(layerName, color);
                                        currentLayer = null;
                                    } catch (Exception e) {
                                        int color = AutoCADColor.getColor("0");
                                        layerColors.put(layerName, color);
                                    }
                                }
                            }

                            // ========================
                            //   HEADER DEL BLOCK
                            // ========================
                            if (currentBlock != null &&
                                    currentFace == null &&
                                    currentPolyline == null &&
                                    currentPolyline_2D == null &&
                                    currentPoint == null &&
                                    currentText == null &&
                                    currentMText == null &&
                                    currentCircle == null &&
                                    currentArc == null &&
                                    currentLine == null &&
                                    currentInsert == null &&
                                    !isLayer) {

                                switch (line) {
                                    case "2": // nome blocco
                                        String bname = br.readLine().trim();
                                        currentBlock.setName(bname);
                                        break;
                                    case "10": // base point X
                                        double bx = readDouble(br) * conversionFactor;
                                        currentBlock.setBasePoint(bx, currentBlock.getBaseY(), currentBlock.getBaseZ());
                                        break;
                                    case "20": // base point Y
                                        double by = readDouble(br) * conversionFactor;
                                        currentBlock.setBasePoint(currentBlock.getBaseX(), by, currentBlock.getBaseZ());
                                        break;
                                    case "30": // base point Z
                                        double bz = readDouble(br) * conversionFactor;
                                        currentBlock.setBasePoint(currentBlock.getBaseX(), currentBlock.getBaseY(), bz);
                                        break;
                                    default:
                                        // altri codici del BLOCK ignorati
                                        break;
                                }
                            }
                            // ========================
                            //   ENTITÀ INSERT (PROPRIETÀ)
                            // ========================
                            else if (currentInsert != null) {
                                switch (line) {
                                    case "2": // nome blocco
                                        String blockName = br.readLine().trim();
                                        currentInsert.setBlockName(blockName);
                                        break;

                                    case "8": // layer dell'INSERT
                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName);
                                        Layer insertLayer;
                                        if (color != null) {
                                            insertLayer = new Layer(filePath, layerName, color, true);
                                        } else {
                                            insertLayer = new Layer(filePath, layerName, -1, false);
                                        }
                                        currentInsert.setLayer(insertLayer);
                                        break;

                                    case "10": {
                                        double x = readDouble(br) * conversionFactor;
                                        currentInsert.setPosition(x, currentInsert.getY(), currentInsert.getZ());
                                        break;
                                    }
                                    case "20": {
                                        double y = readDouble(br) * conversionFactor;
                                        currentInsert.setPosition(currentInsert.getX(), y, currentInsert.getZ());
                                        break;
                                    }
                                    case "30": {
                                        double z = readDouble(br) * conversionFactor;
                                        currentInsert.setPosition(currentInsert.getX(), currentInsert.getY(), z);
                                        break;
                                    }

                                    case "50": // rotazione (gradi)
                                        currentInsert.setRotation(readDouble(br));
                                        break;

                                    case "41": // scala X
                                        currentInsert.setScaleX(readDouble(br));
                                        break;
                                    case "42": // scala Y
                                        currentInsert.setScaleY(readDouble(br));
                                        break;
                                    case "43": // scala Z
                                        currentInsert.setScaleZ(readDouble(br));
                                        break;

                                    case "0": // fine entità INSERT
                                        dxfData.addInsert(currentInsert);
                                        currentInsert = null;
                                        break;

                                    default:
                                        // altri codici ignorati ma consumati se numerici
                                        break;
                                }

                            }

                            // ========================
                            //   ENTITÀ 3DFACE
                            // ========================
                            else if (currentFace != null) {
                                switch (line) {
                                    case "8":
                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName);
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
                                        if (p1 != null) p1.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "30":
                                        if (p1 != null) {
                                            p1.z = readDouble(br) * conversionFactor;
                                            currentFace.p1 = p1;
                                        }
                                        break;

                                    case "11":
                                        p2 = new Point3D(readDouble(br) * conversionFactor, 0, 0);
                                        break;
                                    case "21":
                                        if (p2 != null) p2.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "31":
                                        if (p2 != null) {
                                            p2.z = readDouble(br) * conversionFactor;
                                            currentFace.p2 = p2;
                                        }
                                        break;

                                    case "12":
                                        p3 = new Point3D(readDouble(br) * conversionFactor, 0, 0);
                                        break;
                                    case "22":
                                        if (p3 != null) p3.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "32":
                                        if (p3 != null) {
                                            p3.z = readDouble(br) * conversionFactor;
                                            currentFace.p3 = p3;
                                        }
                                        break;

                                    case "13":
                                        p4 = new Point3D(readDouble(br) * conversionFactor, 0, 0);
                                        break;
                                    case "23":
                                        if (p4 != null) p4.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "33":
                                        if (p4 != null) {
                                            p4.z = readDouble(br) * conversionFactor;
                                            currentFace.p4 = p4;
                                        }
                                        break;
                                }

                                if (currentFace.p1 != null && currentFace.p2 != null && currentFace.p3 != null) {
                                    if (currentFace.p4 == null) currentFace.p4 = currentFace.p3;

                                    if (currentFace.getLayer() != null &&
                                            currentFace.getLayer().getColorState() != -1) {
                                        addFaceToContainer(dxfData, currentBlock, currentFace);
                                    }
                                    currentFace = null;
                                }

                                // ========================
                                //   ENTITÀ POLYLINE 3D
                                // ========================
                            } else if (currentPolyline != null) {

                                if (line.equals("8") && !vertexRead) {
                                    currentPolyline.setFilename(filePath);
                                    layerName = br.readLine().trim();
                                    Integer color = layerColors.get(layerName);
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
                                            case "10":
                                                double x = readDouble(br) * conversionFactor;
                                                currentPolyline.vertices.add(new Point3D(x, 0, 0));
                                                break;
                                            case "20":
                                                if (!currentPolyline.vertices.isEmpty()) {
                                                    Point3D lastVertex = currentPolyline.vertices
                                                            .get(currentPolyline.vertices.size() - 1);
                                                    lastVertex.y = readDouble(br) * conversionFactor;
                                                }
                                                break;
                                            case "30":
                                                if (!currentPolyline.vertices.isEmpty()) {
                                                    Point3D lastVertex = currentPolyline.vertices
                                                            .get(currentPolyline.vertices.size() - 1);
                                                    lastVertex.z = readDouble(br) * conversionFactor;
                                                }
                                                break;
                                            default:
                                                // altri codici ignorati
                                                break;
                                        }
                                    }

                                    if (line != null && line.trim().equals("SEQEND")) {
                                        vertexRead = false;
                                    }
                                    if (currentPolyline.getLayer() != null &&
                                            currentPolyline.getLayer().getColorState() != -1) {
                                        addPolylineToContainer(dxfData, currentBlock, currentPolyline);
                                    }
                                    currentPolyline = null;
                                }

                                // ========================
                                //   ENTITÀ LWPOLYLINE
                                // ========================
                            } else if (currentPolyline_2D != null) {

                                int expectedVertexCount = 0;
                                int currentVertexCount = 0;

                                while ((line = br.readLine()) != null) {
                                    line = line.trim();

                                    if (line.equals("0") &&
                                            currentVertexCount >= expectedVertexCount &&
                                            expectedVertexCount > 0) {
                                        break;
                                    }

                                    switch (line) {
                                        case "8":
                                            layerName = br.readLine().trim();
                                            Integer color = layerColors.get(layerName);
                                            if (color != null) {
                                                if (color != -1) {
                                                    currentPolyline_2D.setLineColor(color);
                                                }
                                                currentPolyline_2D.setLayer(new Layer(filePath, layerName, color, true));
                                            } else {
                                                currentPolyline_2D.setLayer(new Layer(filePath, layerName, -1, false));
                                            }
                                            break;

                                        case "90":
                                            String verticiLine = br.readLine().trim();
                                            try {
                                                expectedVertexCount = Integer.parseInt(verticiLine);
                                            } catch (NumberFormatException e) {
                                                // ignore, continua
                                            }
                                            break;

                                        case "10":
                                            String xLine = br.readLine().trim();
                                            double x;
                                            try {
                                                x = Double.parseDouble(xLine) * conversionFactor;
                                            } catch (NumberFormatException e) {
                                                break;
                                            }
                                            currentPolyline_2D.addVertex(new Point3D(x, 0, 0));
                                            break;

                                        case "20":
                                            String yLine = br.readLine().trim();
                                            double y;
                                            try {
                                                y = Double.parseDouble(yLine) * conversionFactor;
                                            } catch (NumberFormatException e) {
                                                break;
                                            }
                                            if (!currentPolyline_2D.getVertices().isEmpty()) {
                                                Point3D lastVertex = currentPolyline_2D.getVertices()
                                                        .get(currentPolyline_2D.getVertices().size() - 1);
                                                lastVertex.y = y;
                                                lastVertex.z = 0;
                                                currentVertexCount++;
                                            }
                                            break;

                                        case "42":
                                            String bulgeLine = br.readLine().trim();
                                            double bulge;
                                            try {
                                                bulge = Double.parseDouble(bulgeLine);
                                            } catch (NumberFormatException e) {
                                                break;
                                            }
                                            if (!currentPolyline_2D.getVertices().isEmpty()) {
                                                Point3D lastVertex = currentPolyline_2D.getVertices()
                                                        .get(currentPolyline_2D.getVertices().size() - 1);
                                                lastVertex.setBulge(bulge);
                                            }
                                            break;

                                        default:
                                            // altri codici ignorati
                                            break;
                                    }

                                    if (currentVertexCount >= expectedVertexCount && expectedVertexCount > 0) {
                                        break;
                                    }
                                }

                                if (!currentPolyline_2D.getVertices().isEmpty()) {
                                    if (currentPolyline_2D.getLayer() != null &&
                                            currentPolyline_2D.getLayer().getColorState() != -1) {
                                        addPolyline2DToContainer(dxfData, currentBlock, currentPolyline_2D);
                                    }
                                }

                                currentPolyline_2D = null;

                                // ========================
                                //   ENTITÀ POINT
                                // ========================
                            } else if (currentPoint != null) {
                                switch (line) {
                                    case "8":
                                        currentPoint.setFilename(filePath);
                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName);
                                        Layer pointLayer;
                                        if (color != null) {
                                            if (color != -1) {
                                                currentPoint.setColore(color);
                                            }
                                            pointLayer = new Layer(filePath, layerName, color, true);
                                        } else {
                                            pointLayer = new Layer(filePath, layerName, -1, false);
                                        }
                                        currentPoint.setLayer(pointLayer);
                                        break;

                                    case "10":
                                        currentPoint.x = readDouble(br) * conversionFactor;
                                        break;

                                    case "20":
                                        currentPoint.y = readDouble(br) * conversionFactor;
                                        break;

                                    case "30":
                                        currentPoint.z = readDouble(br) * conversionFactor;
                                        break;

                                    case "0":
                                        // Fine entità POINT: aggiungiamo se valido
                                        if ((currentPoint.x != 0 || currentPoint.y != 0) &&
                                                currentPoint.getLayer() != null &&
                                                currentPoint.getLayer().getColorState() != -1) {
                                            addPointToContainer(dxfData, currentBlock, currentPoint);
                                        }
                                        currentPoint = null;
                                        break;
                                }

                                // ========================
                                //   ENTITÀ TEXT
                                // ========================
                            } else if (currentText != null) {
                                switch (line) {
                                    case "8":
                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName);
                                        Layer textLayer;
                                        if (color != null) {
                                            if (color != -1) {
                                                currentText.setColore(color);
                                            }
                                            textLayer = new Layer(filePath, layerName, color, true);
                                        } else {
                                            textLayer = new Layer(filePath, layerName, -1, false);
                                        }
                                        currentText.setLayer(textLayer);
                                        break;

                                    case "10":
                                        currentText.x = readDouble(br) * conversionFactor;
                                        break;

                                    case "20":
                                        currentText.y = readDouble(br) * conversionFactor;
                                        break;

                                    case "30":
                                        currentText.z = readDouble(br) * conversionFactor;
                                        break;

                                    case "1":
                                        String textContent = br.readLine().trim();
                                        currentText.setText(textContent);
                                        if (currentText.getText() != null &&
                                                !currentText.getText().isEmpty() &&
                                                currentText.getLayer() != null &&
                                                currentText.getLayer().getColorState() != -1) {
                                            addTextToContainer(dxfData, currentBlock, currentText);
                                        }
                                        break;

                                    // Codici ignorati ma consumati
                                    case "5": case "100": case "39": case "41":
                                    case "51": case "7": case "71": case "72":
                                    case "11": case "21": case "31":
                                    case "210": case "220": case "230":
                                    case "73":
                                    case "40": // height
                                    case "50": // rotation
                                        br.readLine();
                                        break;

                                    case "0":
                                        currentText = null;
                                        break;
                                }

                                // ========================
                                //   ENTITÀ MTEXT (mappata su DxfText)
                                // ========================
                            } else if (currentMText != null) {
                                switch (line) {
                                    case "8":
                                        layerName = br.readLine().trim();
                                        Integer color = layerColors.get(layerName);
                                        Layer textLayer;
                                        if (color != null) {
                                            if (color != -1) {
                                                currentMText.setColore(color);
                                            }
                                            textLayer = new Layer(filePath, layerName, color, true);
                                        } else {
                                            textLayer = new Layer(filePath, layerName, -1, false);
                                        }
                                        currentMText.setLayer(textLayer);
                                        break;

                                    case "10":
                                        currentMText.x = readDouble(br) * conversionFactor;
                                        break;

                                    case "20":
                                        currentMText.y = readDouble(br) * conversionFactor;
                                        break;

                                    case "30":
                                        currentMText.z = readDouble(br) * conversionFactor;
                                        break;

                                    case "1":
                                    case "2":
                                    case "3":
                                        // 1 = primo testo, 3/2 linee aggiuntive (MTEXT)
                                        String part = br.readLine().trim();
                                        String existing = currentMText.getText();
                                        if (existing == null || existing.isEmpty()) {
                                            currentMText.setText(part);
                                        } else {
                                            currentMText.setText(existing + "\n" + part);
                                        }
                                        break;

                                    // codici di formattazione, altezza, rotazione, ecc.
                                    case "40": // height
                                    case "41": // reference rectangle width
                                    case "71": // attachment point
                                    case "72": // drawing direction
                                    case "50": // rotation
                                    case "7":  // text style name
                                    case "210":
                                    case "220":
                                    case "230":
                                        br.readLine(); // consumiamo il valore
                                        break;

                                    case "0":
                                        if (currentMText.getText() != null &&
                                                !currentMText.getText().isEmpty() &&
                                                currentMText.getLayer() != null &&
                                                currentMText.getLayer().getColorState() != -1) {
                                            addTextToContainer(dxfData, currentBlock, currentMText); // lo trattiamo come TEXT
                                        }
                                        currentMText = null;
                                        break;
                                }

                                // ========================
                                //   ENTITÀ CIRCLE
                                // ========================
                            } else if (currentCircle != null) {
                                switch (line) {
                                    case "8":
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

                                    case "10":
                                        currentCircle.center.x = readDouble(br) * conversionFactor;
                                        break;
                                    case "20":
                                        currentCircle.center.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "30":
                                        currentCircle.center.z = readDouble(br) * conversionFactor;
                                        break;
                                    case "40":
                                        currentCircle.radius = readDouble(br) * conversionFactor;
                                        break;

                                    case "0":
                                        if (currentCircle.radius > 0 &&
                                                currentCircle.getLayer() != null &&
                                                currentCircle.getLayer().getColorState() != -1) {
                                            addCircleToContainer(dxfData, currentBlock, currentCircle);
                                        }
                                        currentCircle = null;
                                        break;
                                }

                                // ========================
                                //   ENTITÀ ARC
                                // ========================
                            } else if (currentArc != null) {
                                switch (line) {
                                    case "8":
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

                                    case "10":
                                        currentArc.center.x = readDouble(br) * conversionFactor;
                                        break;
                                    case "20":
                                        currentArc.center.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "30":
                                        currentArc.center.z = readDouble(br) * conversionFactor;
                                        break;
                                    case "40":
                                        currentArc.radius = readDouble(br) * conversionFactor;
                                        break;
                                    case "50":
                                        currentArc.setStartAngle(readDouble(br));
                                        break;
                                    case "51":
                                        currentArc.setEndAngle(readDouble(br));
                                        break;

                                    case "0":
                                        if (currentArc.radius > 0 &&
                                                currentArc.startAngle != currentArc.endAngle &&
                                                currentArc.getLayer() != null &&
                                                currentArc.getLayer().getColorState() != -1) {
                                            addArcToContainer(dxfData, currentBlock, currentArc);
                                        }
                                        currentArc = null;
                                        break;
                                }

                                // ========================
                                //   ENTITÀ LINE
                                // ========================
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
                                        br.readLine(); // consumiamo valore Z se presente (o 0)
                                        break;
                                    case "11":
                                        currentLine.end.x = readDouble(br) * conversionFactor;
                                        break;
                                    case "21":
                                        currentLine.end.y = readDouble(br) * conversionFactor;
                                        break;
                                    case "31":
                                        currentLine.end.z = 0;
                                        br.readLine(); // idem Z
                                        break;

                                    case "0":
                                        try {
                                            if (currentLine.start.x != currentLine.end.x ||
                                                    currentLine.start.y != currentLine.end.y) {
                                                if (currentLine.getLayer() != null &&
                                                        currentLine.getLayer().getColorState() != null &&
                                                        currentLine.getLayer().getColorState() > -1) {
                                                    addLineToContainer(dxfData, currentBlock, currentLine);
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e("ERROR_DXF", Log.getStackTraceString(e));
                                        }
                                        currentLine = null;
                                        break;
                                }
                            }

                            break; // fine default:
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

    // Lettura sicura di un double
    private static double readDouble(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null) {
            throw new IOException("Unexpected end of file while reading double");
        }
        line = line.trim();
        return Double.parseDouble(line);
    }

    // ============================
    //  HELPERS: decide se DXFData o BLOCK
    // ============================

    private static void addFaceToContainer(DXFData data, DxfBlock block, Face3D face) {
        if (block != null) block.addFace(face);
        else data.addFace(face);
    }

    private static void addPolylineToContainer(DXFData data, DxfBlock block, Polyline p) {
        if (block != null) block.addPolyline(p);
        else data.addPolyline(p);
    }

    private static void addPolyline2DToContainer(DXFData data, DxfBlock block, Polyline_2D p) {
        if (block != null) block.addPolyline2D(p);
        else data.addPolyline2D(p);
    }

    private static void addPointToContainer(DXFData data, DxfBlock block, Point3D p) {
        if (block != null) block.addPoint(p);
        else data.addPoint(p);
    }

    private static void addTextToContainer(DXFData data, DxfBlock block, DxfText t) {
        if (block != null) block.addText(t);
        else data.addText(t);
    }

    private static void addCircleToContainer(DXFData data, DxfBlock block, Circle c) {
        if (block != null) block.addCircle(c);
        else data.addCircle(c);
    }

    private static void addArcToContainer(DXFData data, DxfBlock block, Arc a) {
        if (block != null) block.addArc(a);
        else data.addArc(a);
    }

    private static void addLineToContainer(DXFData data, DxfBlock block, Line l) {
        if (block != null) block.addLine(l);
        else data.addLine(l);
    }



    // =====================================================
//   EXPLODE BLOCKS
// =====================================================
    private static void explodeBlocks(DXFData data) {

        if (data.getBlocks().isEmpty() || data.getInserts().isEmpty()) return;

        // Prepara lookup rapido blockName -> DxfBlock
        Map<String, DxfBlock> blockMap = new HashMap<>();
        for (DxfBlock b : data.getBlocks()) {
            blockMap.put(b.getName(), b);
        }

        // Per ogni INSERT
        for (DxfInsert ins : data.getInserts()) {

            DxfBlock block = blockMap.get(ins.getBlockName());
            if (block == null) continue;

            // Trasformazioni fondamentali
            double angle = Math.toRadians(ins.getRotation());
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);

            // BLOCK base point (da sottrarre)
            double bx = block.getBaseX();
            double by = block.getBaseY();
            double bz = block.getBaseZ();

            // Loop su LINE
            for (Line ln : block.getLines()) {
                Line c = ln.clone(); // devi implementare clone(), o ti fornisco io la versione
                transformLine(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer()); // override layer
                data.addLine(c);
            }

            // Loop su POLYLINE
            for (Polyline pl : block.getPolylines()) {
                Polyline c = clonePolyline(pl);
                transformPolyline(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addPolyline(c);
            }

            // Loop su LWPOLYLINE
            for (Polyline_2D pl : block.getPolylines2D()) {
                Polyline_2D c = clonePolyline2D(pl);
                transformPolyline2D(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addPolyline2D(c);
            }

            // Loop su CIRCLE
            for (Circle cc : block.getCircles()) {
                Circle c = cc.clone();
                transformCircle(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addCircle(c);
            }

            // Loop su ARC
            for (Arc ac : block.getArcs()) {
                Arc c = ac.clone();
                transformArc(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addArc(c);
            }

            // Loop su POINT
            for (Point3D p : block.getPoints()) {
                Point3D c = new Point3D(p.x, p.y, p.z);
                transformPoint(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addPoint(c);
            }

            // Loop su TEXT
            for (DxfText t : block.getTexts()) {
                DxfText c = t.clone();
                transformText(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addText(c);
            }

            // Loop su FACE3D
            for (Face3D f : block.getFaces()) {
                Face3D c = f.clone();
                transformFace3D(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addFace(c);
            }
        }
    }

    private static void transformPoint(Point3D p, DxfInsert ins,
                                       double bx, double by, double bz,
                                       double cosA, double sinA) {

        // step 1 — porta rispetto all'origine del block
        double x = (p.x - bx) * ins.getScaleX();
        double y = (p.y - by) * ins.getScaleY();
        double z = (p.z - bz) * ins.getScaleZ();

        // step 2 — rotazione
        double x2 = x * cosA - y * sinA;
        double y2 = x * sinA + y * cosA;

        // step 3 — traslazione finale
        p.x = x2 + ins.getX();
        p.y = y2 + ins.getY();
        p.z = z + ins.getZ();
    }
    private static void transformPolyline(Polyline pl, DxfInsert ins,
                                          double bx, double by, double bz,
                                          double cosA, double sinA) {
        for (Point3D p : pl.getVertices()) {
            transformPoint(p, ins, bx, by, bz, cosA, sinA);
        }
    }
    private static void transformPolyline2D(Polyline_2D pl, DxfInsert ins,
                                            double bx, double by, double bz,
                                            double cosA, double sinA) {
        for (Point3D p : pl.getVertices()) {
            transformPoint(p, ins, bx, by, bz, cosA, sinA);
        }
    }
    private static void transformCircle(Circle c, DxfInsert ins,
                                        double bx, double by, double bz,
                                        double cosA, double sinA) {

        transformPoint(c.center, ins, bx, by, bz, cosA, sinA);

        double sx = ins.getScaleX();
        double sy = ins.getScaleY();
        c.radius = c.radius * Math.max(sx, sy);
    }

    private static void transformArc(Arc a, DxfInsert ins,
                                     double bx, double by, double bz,
                                     double cosA, double sinA) {

        // stessa logica dei cerchi
        transformPoint(a.center, ins, bx, by, bz, cosA, sinA);

        double sx = ins.getScaleX();
        double sy = ins.getScaleY();
        a.radius = a.radius * Math.max(sx, sy);

        a.startAngle += ins.getRotation();
        a.endAngle += ins.getRotation();
    }

    private static void transformText(DxfText t, DxfInsert ins,
                                      double bx, double by, double bz,
                                      double cosA, double sinA) {

        Point3D p = new Point3D(t.x, t.y, t.z);
        transformPoint(p, ins, bx, by, bz, cosA, sinA);

        t.x = p.x;
        t.y = p.y;
        t.z = p.z;

        t.setRotation(t.getRotation() + ins.getRotation());
    }
    private static void transformFace3D(Face3D f, DxfInsert ins,
                                        double bx, double by, double bz,
                                        double cosA, double sinA) {

        transformPoint(f.p1, ins, bx, by, bz, cosA, sinA);
        transformPoint(f.p2, ins, bx, by, bz, cosA, sinA);
        transformPoint(f.p3, ins, bx, by, bz, cosA, sinA);
        if (f.p4 != null) transformPoint(f.p4, ins, bx, by, bz, cosA, sinA);

    }

    private static Polyline clonePolyline(Polyline pl) {
        Polyline c = new Polyline();
        c.setLayer(pl.getLayer());
        c.setLineColor(pl.getLineColor());
        for (Point3D p : pl.getVertices()) {
            c.getVertices().add(new Point3D(p.x, p.y, p.z));
        }
        return c;
    }
    private static Polyline_2D clonePolyline2D(Polyline_2D pl) {
        Polyline_2D c = new Polyline_2D();
        c.setLayer(pl.getLayer());
        c.setLineColor(pl.getLineColor());
        for (Point3D p : pl.getVertices()) {
            Point3D pp = new Point3D(p.x, p.y, p.z);
            pp.setBulge(p.getBulge());
            c.getVertices().add(pp);
        }
        return c;
    }
    private static void transformLine(Line l, DxfInsert ins,
                                      double bx, double by, double bz,
                                      double cosA, double sinA) {
        transformPoint(l.start, ins, bx, by, bz, cosA, sinA);
        transformPoint(l.end, ins, bx, by, bz, cosA, sinA);
    }



}
