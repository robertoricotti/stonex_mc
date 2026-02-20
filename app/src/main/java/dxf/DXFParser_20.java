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
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;

public class DXFParser_20 {

    // Colori associati ai layer (nome layer -> colore ARGB)
    static Map<String, Integer> layerColors = new HashMap<>();
    static long drillPointNr = 0;

    public static DXFData parseDXF(String filePath, double conversionFactor) {
        DXFData dxfData = new DXFData();

        // Sempre reset della mappa colori per evitare "memory" tra file diversi
        layerColors.clear();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));

            String code;
            String value;

            // Stato "globale"
            String currentSection = null;   // HEADER / TABLES / BLOCKS / ENTITIES / OBJECTS...
            String currentTable = null;     // LAYER / STYLE ...
            String lastZeroValue = null;    // ultimo "valore" letto con groupCode 0

            // Stato TAB LAYER
            boolean inLayerRecord = false;
            Layer currentLayerRec = null;
            String currentLayerName = null;
            String colorCode = "";

            // Stato ENTITÀ
            Face3D currentFace = null;
            Polyline currentPolyline = null;       // POLYLINE (2D/3D old style)
            Polyline_2D currentPolyline_2D = null; // LWPOLYLINE
            int lwExpectedVertexCount = 0;
            int lwCurrentVertexCount = 0;
            boolean inVertex = false;              // <-- SIAMO DENTRO UN VERTEX DI UNA POLYLINE

            Point3D p1 = null;
            Point3D p2 = null;
            Point3D p3 = null;
            Point3D p4 = null;
            Point3D_Drill currentDrillPoint = null;
            Point3D currentPoint = null;

            Circle currentCircle = null;
            Arc currentArc = null;
            Line currentLine = null;
            DxfText currentText = null;
            DxfText currentMText = null; // MTEXT mappato su DxfText

            // BLOCK / INSERT
            DxfBlock currentBlock = null;     // se non null, siamo dentro una definizione di blocco
            DxfInsert currentInsert = null;   // entità INSERT corrente

            // LOOP PRINCIPALE: sempre a coppie (groupCode / value)
            while (true) {
                code = br.readLine();
                if (code == null) break;
                code = code.trim();

                // DXF è sempre: groupCode -> value
                value = br.readLine();
                if (value == null) break;
                value = value.trim();

                try {

                    // ======================================
                    //   GESTIONE SPECIAL CASE: groupCode == 0
                    //   (nuova entità / record / sezione)
                    // ======================================
                    if ("0".equals(code)) {

                        // Prima di passare a una nuova entità, chiudiamo quelle che
                        // terminano implicitamente quando appare un nuovo "0" generico.
                        // (esclusi: VERTEX e SEQEND, gestiti a parte)
                        if (!"VERTEX".equals(value) && !"SEQEND".equals(value)) {

                            // usciamo comunque da un eventuale VERTEX
                            inVertex = false;

                            // Chiudi 3DFACE se attivo
                            if (currentFace != null) {
                                if (currentFace.p1 != null && currentFace.p2 != null && currentFace.p3 != null) {
                                    if (currentFace.p4 == null) currentFace.p4 = currentFace.p3;
                                    if (currentFace.getLayer() != null &&
                                            currentFace.getLayer().getColorState() != null &&
                                            currentFace.getLayer().getColorState() != -1) {
                                        addFaceToContainer(dxfData, currentBlock, currentFace);
                                    }
                                }
                                currentFace = null;
                            }

                            // Chiudi POINT se attivo
                            if (currentPoint != null) {
                                if ((currentPoint.x != 0 || currentPoint.y != 0) &&
                                        currentPoint.getLayer() != null &&
                                        currentPoint.getLayer().getColorState() != null &&
                                        currentPoint.getLayer().getColorState() != -1) {
                                    addPointToContainer(dxfData, currentBlock, currentPoint);
                                }
                                currentPoint = null;
                            }
                            //Chiudi DRILLPOINT se attiva
                            if (currentDrillPoint != null) {
                                if(currentDrillPoint.getHeadX()!=null&&currentDrillPoint.getHeadY()!=null){
                                    addDrillPointToContainer(dxfData,currentBlock,currentDrillPoint);
                                }
                                currentDrillPoint = null;
                            }

                            // Chiudi TEXT se attivo
                            if (currentText != null) {
                                if (currentText.getText() != null &&
                                        !currentText.getText().isEmpty() &&
                                        currentText.getLayer() != null &&
                                        currentText.getLayer().getColorState() != null &&
                                        currentText.getLayer().getColorState() != -1) {
                                    addTextToContainer(dxfData, currentBlock, currentText);
                                }
                                currentText = null;
                            }

                            // Chiudi MTEXT se attivo (mappato su DxfText)
                            if (currentMText != null) {
                                if (currentMText.getText() != null &&
                                        !currentMText.getText().isEmpty() &&
                                        currentMText.getLayer() != null &&
                                        currentMText.getLayer().getColorState() != null &&
                                        currentMText.getLayer().getColorState() != -1) {
                                    addTextToContainer(dxfData, currentBlock, currentMText);
                                }
                                currentMText = null;
                            }

                            // Chiudi CIRCLE
                            if (currentCircle != null) {
                                if (currentCircle.radius > 0 &&
                                        currentCircle.getLayer() != null &&
                                        currentCircle.getLayer().getColorState() != null &&
                                        currentCircle.getLayer().getColorState() != -1) {
                                    addCircleToContainer(dxfData, currentBlock, currentCircle);
                                }
                                currentCircle = null;
                            }

                            // Chiudi ARC
                            if (currentArc != null) {
                                if (currentArc.radius > 0 &&
                                        currentArc.startAngle != currentArc.endAngle &&
                                        currentArc.getLayer() != null &&
                                        currentArc.getLayer().getColorState() != null &&
                                        currentArc.getLayer().getColorState() != -1) {
                                    addArcToContainer(dxfData, currentBlock, currentArc);
                                }
                                currentArc = null;
                            }

                            // Chiudi LINE
                            if (currentLine != null) {
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
                            }

                            // Chiudi LWPOLYLINE
                            if (currentPolyline_2D != null) {
                                if (!currentPolyline_2D.getVertices().isEmpty() &&
                                        currentPolyline_2D.getLayer() != null &&
                                        currentPolyline_2D.getLayer().getColorState() != null &&
                                        currentPolyline_2D.getLayer().getColorState() != -1) {
                                    addPolyline2DToContainer(dxfData, currentBlock, currentPolyline_2D);
                                }
                                currentPolyline_2D = null;
                                lwExpectedVertexCount = 0;
                                lwCurrentVertexCount = 0;
                            }

                            // Chiudi INSERT
                            if (currentInsert != null) {
                                dxfData.addInsert(currentInsert);
                                currentInsert = null;
                            }
                        }

                        // Gestione "0" = SECTION / TABLE / ecc.
                        switch (value) {
                            case "SECTION":
                                lastZeroValue = "SECTION";
                                currentSection = null;
                                break;

                            case "ENDSEC":
                                currentSection = null;
                                currentTable = null;
                                inLayerRecord = false;
                                currentLayerRec = null;
                                currentLayerName = null;
                                lastZeroValue = "ENDSEC";
                                break;

                            case "TABLE":
                                lastZeroValue = "TABLE";
                                currentTable = null;
                                break;

                            case "ENDTAB":
                                currentTable = null;
                                inLayerRecord = false;
                                currentLayerRec = null;
                                currentLayerName = null;
                                lastZeroValue = "ENDTAB";
                                break;

                            case "LAYER":
                                // Siamo dentro TABLE LAYER?
                                if ("TABLES".equals(currentSection) && "LAYER".equals(currentTable)) {
                                    inLayerRecord = true;
                                    currentLayerRec = new Layer(filePath, null, null, true);
                                    currentLayerName = null;
                                }
                                lastZeroValue = "LAYER";
                                break;

                            case "3DFACE":
                                currentFace = new Face3D(null, null, null, null, MyColorClass.colorTriangle, null);
                                lastZeroValue = "3DFACE";
                                break;

                            case "POLYLINE":
                                currentPolyline = new Polyline();
                                currentPolyline.setFilename(filePath);
                                inVertex = false;
                                lastZeroValue = "POLYLINE";
                                break;

                            case "LWPOLYLINE":
                                currentPolyline_2D = new Polyline_2D();
                                lwExpectedVertexCount = 0;
                                lwCurrentVertexCount = 0;
                                lastZeroValue = "LWPOLYLINE";
                                break;

                            case "VERTEX":
                                // Siamo dentro una POLYLINE: i 10/20/30 che seguono
                                // appartengono a un vertice, NON all'header della polyline.
                                if (currentPolyline != null) {
                                    inVertex = true;
                                }
                                lastZeroValue = "VERTEX";
                                break;

                            case "SEQEND":
                                // chiusura POLYLINE 3D/2D old style
                                inVertex = false;
                                if (currentPolyline != null) {
                                    if (currentPolyline.getLayer() != null &&
                                            currentPolyline.getLayer().getColorState() != null &&
                                            currentPolyline.getLayer().getColorState() != -1) {
                                        addPolylineToContainer(dxfData, currentBlock, currentPolyline);
                                    }
                                    currentPolyline = null;
                                }
                                lastZeroValue = "SEQEND";
                                break;

                            case "POINT":
                                currentPoint = new Point3D(0, 0, 0);
                                currentPoint.setFilename(filePath);
                                currentDrillPoint = new Point3D_Drill(null);
                                currentDrillPoint.setId(String.valueOf(drillPointNr));
                                drillPointNr++;
                                lastZeroValue = "POINT";
                                break;

                            case "TEXT":
                                currentText = new DxfText("", 0, 0, 0, -1, null);
                                lastZeroValue = "TEXT";
                                break;

                            case "MTEXT":
                                currentMText = new DxfText("", 0, 0, 0, -1, null);
                                lastZeroValue = "MTEXT";
                                break;

                            case "CIRCLE":
                                currentCircle = new Circle(new Point3D(0, 0, 0), 0, -1, null);
                                lastZeroValue = "CIRCLE";
                                break;

                            case "ARC":
                                currentArc = new Arc(new Point3D(0, 0, 0), 0, 0, 0, -1, null);
                                lastZeroValue = "ARC";
                                break;

                            case "LINE":
                                currentLine = new Line(new Point3D(0, 0, 0),
                                        new Point3D(0, 0, 0),
                                        -1,
                                        null);
                                lastZeroValue = "LINE";
                                break;

                            case "BLOCK":
                                currentBlock = new DxfBlock(null);
                                lastZeroValue = "BLOCK";
                                break;

                            case "ENDBLK":
                                if (currentBlock != null) {
                                    dxfData.addBlock(currentBlock);
                                    currentBlock = null;
                                }
                                lastZeroValue = "ENDBLK";
                                break;

                            case "INSERT":
                                currentInsert = new DxfInsert(null);
                                lastZeroValue = "INSERT";
                                break;

                            case "EOF":
                                // fine file DXF
                                lastZeroValue = "EOF";
                                code = null;
                                break;

                            default:
                                lastZeroValue = value;
                                break;
                        }

                        if (code == null) break; // EOF gestito

                        continue; // passa alla prossima coppia groupCode/value
                    }

                    // ======================================
                    //   GESTIONE groupCode != 0
                    // ======================================

                    // --------------------------
                    // SECTION / TABLE / LAYER
                    // --------------------------
                    if ("2".equals(code)) {
                        // Nome della SECTION
                        if ("SECTION".equals(lastZeroValue)) {
                            currentSection = value;
                        }
                        // Nome della TABLE
                        else if ("TABLE".equals(lastZeroValue)) {
                            currentTable = value;
                        }
                        // Nome LAYER (dentro tabella LAYER)
                        else if (inLayerRecord && currentLayerRec != null && currentLayerName == null) {
                            currentLayerName = value;
                            currentLayerRec.setProjName(filePath);
                            currentLayerRec.setLayerName(currentLayerName);
                        }
                        // Nome blocco
                        else if (currentBlock != null && "BLOCK".equals(lastZeroValue) && currentBlock.getName() == null) {
                            currentBlock.setName(value);
                        }
                        // Nome blocco per INSERT
                        else if (currentInsert != null && "INSERT".equals(lastZeroValue) && currentInsert.getBlockName() == null) {
                            currentInsert.setBlockName(value);
                        }
                    } else if ("62".equals(code)) {
                        // colore layer nella TAB LAYER
                        if (inLayerRecord && currentLayerRec != null && currentLayerName != null) {
                            colorCode = value;
                            try {
                                currentLayerRec.setColorState(Integer.parseInt(colorCode));
                            } catch (Exception ignore) {
                                // fallback handled below
                            }
                            int argb = AutoCADColor.getColor(colorCode);
                            layerColors.put(currentLayerName, argb);
                            dxfData.addLayer(currentLayerRec);

                            // debug opzionale
                            Log.d("DXF_LAYERS", "Layer=" + currentLayerName + " colorIndex=" + colorCode + " argb=" + argb);

                            currentLayerRec = null;
                            currentLayerName = null;
                            inLayerRecord = false;
                        }
                    }

                    // --------------------------
                    //   LAYER DI ENTITÀ (groupCode 8)
                    // --------------------------
                    if ("8".equals(code)) {
                        String lname = value;
                        Integer color = layerColors.get(lname);
                        Layer entLayer;
                        if (color != null) {
                            entLayer = new Layer(filePath, lname, color, true);
                        } else {
                            entLayer = new Layer(filePath, lname, -1, false);
                        }

                        if (currentFace != null) {
                            if (color != null && color != -1) {
                                MyColorClass.colorTriangle = color;
                            }
                            currentFace.setLayer(entLayer);
                        } else if (currentPolyline != null) {
                            if (color != null && color != -1) {
                                currentPolyline.setLineColor(color);
                            }
                            currentPolyline.setLayer(entLayer);
                        } else if (currentPolyline_2D != null) {
                            if (color != null && color != -1) {
                                currentPolyline_2D.setLineColor(color);
                            }
                            currentPolyline_2D.setLayer(entLayer);
                        } else if (currentPoint != null) {
                            if (color != null && color != -1) {
                                currentPoint.setColore(color);
                            }
                            currentPoint.setLayer(entLayer);
                        } else if (currentText != null) {
                            if (color != null && color != -1) {
                                currentText.setColore(color);
                            }
                            currentText.setLayer(entLayer);
                        } else if (currentMText != null) {
                            if (color != null && color != -1) {
                                currentMText.setColore(color);
                            }
                            currentMText.setLayer(entLayer);
                        } else if (currentCircle != null) {
                            if (color != null && color != -1) {
                                currentCircle.setColor(color);
                            }
                            currentCircle.setLayer(entLayer);
                        } else if (currentArc != null) {
                            if (color != null && color != -1) {
                                currentArc.setColor(color);
                            }
                            currentArc.setLayer(entLayer);
                        } else if (currentLine != null) {
                            if (color != null && color != -1) {
                                currentLine.setColor(color);
                            }
                            currentLine.setLayer(entLayer);
                        } else if (currentInsert != null) {
                            currentInsert.setLayer(entLayer);
                        }
                    }

                    // --------------------------
                    //   ENTITÀ 3DFACE
                    // --------------------------
                    if (currentFace != null) {
                        switch (code) {
                            case "10": {
                                double x = safeDouble(value, conversionFactor);
                                p1 = new Point3D(x, 0, 0);
                                currentFace.p1 = p1;
                                break;
                            }
                            case "20":
                                if (currentFace.p1 != null) {
                                    currentFace.p1.y = safeDouble(value, conversionFactor);
                                }
                                break;
                            case "30":
                                if (currentFace.p1 != null) {
                                    currentFace.p1.z = safeDouble(value, conversionFactor);
                                }
                                break;

                            case "11": {
                                double x = safeDouble(value, conversionFactor);
                                p2 = new Point3D(x, 0, 0);
                                currentFace.p2 = p2;
                                break;
                            }
                            case "21":
                                if (currentFace.p2 != null) {
                                    currentFace.p2.y = safeDouble(value, conversionFactor);
                                }
                                break;
                            case "31":
                                if (currentFace.p2 != null) {
                                    currentFace.p2.z = safeDouble(value, conversionFactor);
                                }
                                break;

                            case "12": {
                                double x = safeDouble(value, conversionFactor);
                                p3 = new Point3D(x, 0, 0);
                                currentFace.p3 = p3;
                                break;
                            }
                            case "22":
                                if (currentFace.p3 != null) {
                                    currentFace.p3.y = safeDouble(value, conversionFactor);
                                }
                                break;
                            case "32":
                                if (currentFace.p3 != null) {
                                    currentFace.p3.z = safeDouble(value, conversionFactor);
                                }
                                break;

                            case "13": {
                                double x = safeDouble(value, conversionFactor);
                                p4 = new Point3D(x, 0, 0);
                                currentFace.p4 = p4;
                                break;
                            }
                            case "23":
                                if (currentFace.p4 != null) {
                                    currentFace.p4.y = safeDouble(value, conversionFactor);
                                }
                                break;
                            case "33":
                                if (currentFace.p4 != null) {
                                    currentFace.p4.z = safeDouble(value, conversionFactor);
                                }
                                break;
                        }
                    }

                    // --------------------------
                    //   ENTITÀ POLYLINE (old style 2D/3D)
                    // --------------------------
                    if (currentPolyline != null && inVertex) {
                        // ATTENZIONE: leggiamo i 10/20/30 SOLO DENTRO UN VERTEX
                        if ("10".equals(code)) {
                            double x = safeDouble(value, conversionFactor);
                            currentPolyline.vertices.add(new Point3D(x, 0, 0));
                        } else if ("20".equals(code)) {
                            if (!currentPolyline.vertices.isEmpty()) {
                                Point3D last = currentPolyline.vertices.get(currentPolyline.vertices.size() - 1);
                                last.y = safeDouble(value, conversionFactor);
                            }
                        } else if ("30".equals(code)) {
                            if (!currentPolyline.vertices.isEmpty()) {
                                Point3D last = currentPolyline.vertices.get(currentPolyline.vertices.size() - 1);
                                last.z = safeDouble(value, conversionFactor);
                            }
                        }
                    }

                    // --------------------------
                    //   ENTITÀ LWPOLYLINE
                    // --------------------------
                    if (currentPolyline_2D != null) {
                        switch (code) {
                            case "90":
                                try {
                                    lwExpectedVertexCount = Integer.parseInt(value.trim());
                                } catch (NumberFormatException ignore) {
                                }
                                break;

                            case "10": {
                                double x = safeDouble(value, conversionFactor);
                                currentPolyline_2D.addVertex(new Point3D(x, 0, 0));
                                break;
                            }

                            case "20": {
                                double y = safeDouble(value, conversionFactor);
                                if (!currentPolyline_2D.getVertices().isEmpty()) {
                                    Point3D last = currentPolyline_2D.getVertices()
                                            .get(currentPolyline_2D.getVertices().size() - 1);
                                    last.y = y;
                                    last.z = 0;
                                    lwCurrentVertexCount++;
                                }
                                break;
                            }

                            case "42": {
                                double bulge;
                                try {
                                    bulge = Double.parseDouble(value.trim());
                                } catch (NumberFormatException e) {
                                    break;
                                }
                                if (!currentPolyline_2D.getVertices().isEmpty()) {
                                    Point3D last = currentPolyline_2D.getVertices()
                                            .get(currentPolyline_2D.getVertices().size() - 1);
                                    last.setBulge(bulge);
                                }
                                break;
                            }
                        }
                    }

                    // --------------------------
                    //   ENTITÀ POINT
                    // --------------------------
                    if (currentPoint != null) {
                        switch (code) {
                            case "10":
                                currentPoint.x = safeDouble(value, conversionFactor);
                                break;
                            case "20":
                                currentPoint.y = safeDouble(value, conversionFactor);
                                break;
                            case "30":
                                currentPoint.z = safeDouble(value, conversionFactor);
                                break;
                        }
                    }
                    if (currentDrillPoint != null) {
                        switch (code) {
                            case "10":
                                currentDrillPoint.setHeadX(safeDouble(value, conversionFactor));
                                currentDrillPoint.setEndX(safeDouble(value, conversionFactor));
                                break;
                            case "20":
                                currentDrillPoint.setHeadY(safeDouble(value, conversionFactor));
                                currentDrillPoint.setEndY(safeDouble(value, conversionFactor));

                                break;
                            case "30":

                                currentDrillPoint.setHeadZ(safeDouble(value, conversionFactor));
                                currentDrillPoint.setEndZ(safeDouble(value, conversionFactor));
                                break;
                        }
                    }

                    // --------------------------
                    //   ENTITÀ TEXT
                    // --------------------------
                    if (currentText != null) {
                        switch (code) {
                            case "10":
                                currentText.x = safeDouble(value, conversionFactor);
                                break;
                            case "20":
                                currentText.y = safeDouble(value, conversionFactor);
                                break;
                            case "30":
                                currentText.z = safeDouble(value, conversionFactor);
                                break;
                            case "1":
                                currentText.setText(value);
                                break;
                        }
                    }

                    // --------------------------
                    //   ENTITÀ MTEXT (come DxfText)
                    // --------------------------
                    if (currentMText != null) {
                        switch (code) {
                            case "10":
                                currentMText.x = safeDouble(value, conversionFactor);
                                break;
                            case "20":
                                currentMText.y = safeDouble(value, conversionFactor);
                                break;
                            case "30":
                                currentMText.z = safeDouble(value, conversionFactor);
                                break;

                            case "1":
                            case "2":
                            case "3": {
                                String existing = currentMText.getText();
                                if (existing == null || existing.isEmpty()) {
                                    currentMText.setText(value);
                                } else {
                                    currentMText.setText(existing + "\n" + value);
                                }
                                break;
                            }
                        }
                    }

                    // --------------------------
                    //   ENTITÀ CIRCLE
                    // --------------------------
                    if (currentCircle != null) {
                        switch (code) {
                            case "10":
                                currentCircle.center.x = safeDouble(value, conversionFactor);
                                break;
                            case "20":
                                currentCircle.center.y = safeDouble(value, conversionFactor);
                                break;
                            case "30":
                                currentCircle.center.z = safeDouble(value, conversionFactor);
                                break;
                            case "40":
                                currentCircle.radius = safeDouble(value, conversionFactor);
                                break;
                        }
                    }

                    // --------------------------
                    //   ENTITÀ ARC
                    // --------------------------
                    if (currentArc != null) {
                        switch (code) {
                            case "10":
                                currentArc.center.x = safeDouble(value, conversionFactor);
                                break;
                            case "20":
                                currentArc.center.y = safeDouble(value, conversionFactor);
                                break;
                            case "30":
                                currentArc.center.z = safeDouble(value, conversionFactor);
                                break;
                            case "40":
                                currentArc.radius = safeDouble(value, conversionFactor);
                                break;
                            case "50":
                                currentArc.setStartAngle(safeDouble(value, 1.0));
                                break;
                            case "51":
                                currentArc.setEndAngle(safeDouble(value, 1.0));
                                break;
                        }
                    }

                    // --------------------------
                    //   ENTITÀ LINE
                    // --------------------------
                    if (currentLine != null) {
                        switch (code) {
                            case "10":
                                currentLine.start.x = safeDouble(value, conversionFactor);
                                break;
                            case "20":
                                currentLine.start.y = safeDouble(value, conversionFactor);
                                break;
                            case "30":
                                currentLine.start.z = 0;
                                break;
                            case "11":
                                currentLine.end.x = safeDouble(value, conversionFactor);
                                break;
                            case "21":
                                currentLine.end.y = safeDouble(value, conversionFactor);
                                break;
                            case "31":
                                currentLine.end.z = 0;
                                break;
                        }
                    }

                    // --------------------------
                    //   ENTITÀ INSERT
                    // --------------------------
                    if (currentInsert != null) {
                        switch (code) {
                            case "10":
                                currentInsert.setPosition(
                                        safeDouble(value, conversionFactor),
                                        currentInsert.getY(),
                                        currentInsert.getZ());
                                break;
                            case "20":
                                currentInsert.setPosition(
                                        currentInsert.getX(),
                                        safeDouble(value, conversionFactor),
                                        currentInsert.getZ());
                                break;
                            case "30":
                                currentInsert.setPosition(
                                        currentInsert.getX(),
                                        currentInsert.getY(),
                                        safeDouble(value, conversionFactor));
                                break;
                            case "50":
                                currentInsert.setRotation(safeDouble(value, 1.0));
                                break;
                            case "41":
                                currentInsert.setScaleX(safeDouble(value, 1.0));
                                break;
                            case "42":
                                currentInsert.setScaleY(safeDouble(value, 1.0));
                                break;
                            case "43":
                                currentInsert.setScaleZ(safeDouble(value, 1.0));
                                break;
                        }
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
                        drillPointNr=0;
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
                drillPointNr=0;
            }
            e.printStackTrace();
            Log.e("ErroDXF", Log.getStackTraceString(e));
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ignore) {
            }
        }

        if (filePath.equals(DataSaved.progettoSelected)) {
            isFinishedDTM = true;
        }
        if (filePath.equals(DataSaved.progettoSelected_POLY)) {
            isFinishedPOLY = true;
        }
        if (filePath.equals(DataSaved.progettoSelected_POINT)) {
            isFinishedPOINT = true;
            drillPointNr=0;
        }

        explodeBlocks(dxfData);
        return dxfData;
    }

    // Lettura sicura di un double con fattore di conversione
    private static double safeDouble(String s, double factor) {
        try {
            return Double.parseDouble(s.trim()) * factor;
        } catch (Exception e) {
            return 0.0;
        }
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
    private static void addDrillPointToContainer(DXFData data,DxfBlock block,Point3D_Drill point3DDrill){
        if(block!=null) block.addPointDrill(point3DDrill);
        else data.addDrill_points(point3DDrill);
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
    //   EXPLODE BLOCKS (tua implementazione, invariata)
    // =====================================================
    private static void explodeBlocks(DXFData data) {

        if (data.getBlocks().isEmpty() || data.getInserts().isEmpty()) return;

        Map<String, DxfBlock> blockMap = new HashMap<>();
        for (DxfBlock b : data.getBlocks()) {
            blockMap.put(b.getName(), b);
        }

        for (DxfInsert ins : data.getInserts()) {

            DxfBlock block = blockMap.get(ins.getBlockName());
            if (block == null) continue;

            double angle = Math.toRadians(ins.getRotation());
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);

            double bx = block.getBaseX();
            double by = block.getBaseY();
            double bz = block.getBaseZ();

            for (Line ln : block.getLines()) {
                Line c = ln.clone();
                transformLine(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addLine(c);
            }

            for (Polyline pl : block.getPolylines()) {
                Polyline c = clonePolyline(pl);
                transformPolyline(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addPolyline(c);
            }

            for (Polyline_2D pl : block.getPolylines2D()) {
                Polyline_2D c = clonePolyline2D(pl);
                transformPolyline2D(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addPolyline2D(c);
            }

            for (Circle cc : block.getCircles()) {
                Circle c = cc.clone();
                transformCircle(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addCircle(c);
            }

            for (Arc ac : block.getArcs()) {
                Arc c = ac.clone();
                transformArc(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addArc(c);
            }

            for (Point3D p : block.getPoints()) {
                Point3D c = new Point3D(p.x, p.y, p.z);
                transformPoint(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addPoint(c);
            }

            for (DxfText t : block.getTexts()) {
                DxfText c = t.clone();
                transformText(c, ins, bx, by, bz, cosA, sinA);
                c.setLayer(ins.getLayer());
                data.addText(c);
            }

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

        double x = (p.x - bx) * ins.getScaleX();
        double y = (p.y - by) * ins.getScaleY();
        double z = (p.z - bz) * ins.getScaleZ();

        double x2 = x * cosA - y * sinA;
        double y2 = x * sinA + y * cosA;

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
