
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

/**
 * Parser DXF riscritto da zero con una logica più tollerante e meno fragile.
 *
 * Obiettivi:
 * - non scartare entità solo perché il layer non è definito nella TABLE LAYER
 * - supportare layer color, entity color (ACI 62) e true color (420)
 * - gestire POLYLINE/VERTEX/SEQEND e LWPOLYLINE senza dipendere dall'ordine perfetto
 * - mantenere compatibilità con i model esistenti del progetto
 */
public class DXFParser_20 {

    private static final String TAG = "DXFParser_20";
    private static final int DEFAULT_ARGB = AutoCADColor.getColor("7"); // fallback classico AutoCAD
    static Map<String, Integer> layerColors = new HashMap<>();
    static long drillPointNr = 0;

    public static DXFData parseDXF(String filePath, double conversionFactor) {
        DXFData dxfData = new DXFData();
        layerColors.clear();

        ParserState state = new ParserState(filePath, conversionFactor, dxfData);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            while (true) {
                String code = br.readLine();
                if (code == null) break;

                String value = br.readLine();
                if (value == null) break;

                code = code.trim();
                value = value.trim();

                try {
                    if ("0".equals(code)) {
                        state.handleZero(value);
                        if (state.eofReached) break;
                    } else {
                        state.handleCodeValue(code, value);
                    }
                } catch (Exception entityEx) {
                    Log.e(TAG, "Errore durante parsing gruppo [" + code + " -> " + value + "]", entityEx);
                }
            }

            state.finishOpenObjectsAtEOF();

        } catch (IOException e) {
            ReadProjectService.parserStatus = e.toString() + "\n";
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            markParserFinished(filePath);
        }

        explodeBlocks(dxfData);
        return dxfData;
    }

    private static void markParserFinished(String filePath) {
        if (filePath.equals(DataSaved.progettoSelected)) {
            isFinishedDTM = true;
        }
        if (filePath.equals(DataSaved.progettoSelected_POLY)) {
            isFinishedPOLY = true;
        }
        if (filePath.equals(DataSaved.progettoSelected_POINT)) {
            isFinishedPOINT = true;
            drillPointNr = 0;
        }
    }

    private static final class ParserState {

        final String filePath;
        final double conversionFactor;
        final DXFData data;

        String currentSection = null;
        String currentTable = null;
        String lastZeroValue = null;
        boolean eofReached = false;

        // TABLES -> LAYER
        boolean inLayerRecord = false;
        Layer currentLayerRec = null;
        String currentLayerName = null;

        // BLOCK
        DxfBlock currentBlock = null;

        // Entità aperte
        Face3D currentFace = null;
        Polyline currentPolyline = null;
        Polyline_2D currentLWPolyline = null;
        Point3D currentPoint = null;
        Point3D_Drill currentDrillPoint = null;
        Circle currentCircle = null;
        Arc currentArc = null;
        Line currentLine = null;
        DxfText currentText = null;
        DxfText currentMText = null;
        DxfInsert currentInsert = null;

        // Stato entità
        boolean inVertex = false;
        boolean currentPolylineClosed = false;
        boolean currentLWPolylineClosed = false;
        double currentLWPolylineElevation = 0.0;

        boolean pointTouched = false;
        boolean drillTouched = false;
        boolean lineStartTouched = false;
        boolean lineEndTouched = false;
        boolean circleCenterTouched = false;
        boolean circleRadiusTouched = false;
        boolean arcCenterTouched = false;
        boolean arcRadiusTouched = false;
        boolean arcAnglesTouched = false;

        // Colore entità corrente
        boolean hasExplicitEntityColor = false;
        int currentEntityArgb = DEFAULT_ARGB;

        ParserState(String filePath, double conversionFactor, DXFData data) {
            this.filePath = filePath;
            this.conversionFactor = conversionFactor;
            this.data = data;
        }

        void handleZero(String value) {
            // Se stiamo entrando in un nuovo record/entità, chiudiamo l'entità attuale.
            // Eccezioni: VERTEX e SEQEND fanno parte di una POLYLINE già aperta.
            if (!"VERTEX".equals(value) && !"SEQEND".equals(value)) {
                finishCurrentEntities(false);
                inVertex = false;
            }

            switch (value) {
                case "SECTION":
                    lastZeroValue = "SECTION";
                    currentSection = null;
                    return;

                case "ENDSEC":
                    finishCurrentEntities(false);
                    currentSection = null;
                    currentTable = null;
                    closeLayerRecordIfNeeded();
                    lastZeroValue = "ENDSEC";
                    return;

                case "TABLE":
                    lastZeroValue = "TABLE";
                    currentTable = null;
                    return;

                case "ENDTAB":
                    closeLayerRecordIfNeeded();
                    currentTable = null;
                    lastZeroValue = "ENDTAB";
                    return;

                case "LAYER":
                    if ("TABLES".equals(currentSection) && "LAYER".equals(currentTable)) {
                        closeLayerRecordIfNeeded();
                        inLayerRecord = true;
                        currentLayerRec = new Layer(filePath, null, DEFAULT_ARGB, true);
                        currentLayerName = null;
                    }
                    lastZeroValue = "LAYER";
                    return;

                case "3DFACE":
                    resetEntityStyle();
                    currentFace = new Face3D(null, null, null, null, MyColorClass.colorTriangle, null);
                    lastZeroValue = "3DFACE";
                    return;

                case "POLYLINE":
                    resetEntityStyle();
                    currentPolyline = new Polyline();
                    currentPolyline.setFilename(filePath);
                    currentPolylineClosed = false;
                    inVertex = false;
                    lastZeroValue = "POLYLINE";
                    return;

                case "VERTEX":
                    if (currentPolyline != null) {
                        inVertex = true;
                    }
                    lastZeroValue = "VERTEX";
                    return;

                case "SEQEND":
                    inVertex = false;
                    finishCurrentPolyline();
                    lastZeroValue = "SEQEND";
                    return;

                case "LWPOLYLINE":
                    resetEntityStyle();
                    currentLWPolyline = new Polyline_2D();
                    currentLWPolylineClosed = false;
                    currentLWPolylineElevation = 0.0;
                    lastZeroValue = "LWPOLYLINE";
                    return;

                case "POINT":
                    resetEntityStyle();
                    currentPoint = new Point3D(0, 0, 0);
                    currentPoint.setFilename(filePath);
                    pointTouched = false;

                    currentDrillPoint = new Point3D_Drill(null);
                    currentDrillPoint.setId(String.valueOf(drillPointNr));
                    drillPointNr++;
                    drillTouched = false;
                    ReadProjectService.parserStatus = "Reading Points...\n" + drillPointNr;
                    lastZeroValue = "POINT";
                    return;

                case "TEXT":
                    resetEntityStyle();
                    currentText = new DxfText("", 0, 0, 0, -1, null);
                    lastZeroValue = "TEXT";
                    return;

                case "MTEXT":
                    resetEntityStyle();
                    currentMText = new DxfText("", 0, 0, 0, -1, null);
                    lastZeroValue = "MTEXT";
                    return;

                case "CIRCLE":
                    resetEntityStyle();
                    currentCircle = new Circle(new Point3D(0, 0, 0), 0, -1, null);
                    circleCenterTouched = false;
                    circleRadiusTouched = false;
                    lastZeroValue = "CIRCLE";
                    return;

                case "ARC":
                    resetEntityStyle();
                    currentArc = new Arc(new Point3D(0, 0, 0), 0, 0, 0, -1, null);
                    arcCenterTouched = false;
                    arcRadiusTouched = false;
                    arcAnglesTouched = false;
                    lastZeroValue = "ARC";
                    return;

                case "LINE":
                    resetEntityStyle();
                    currentLine = new Line(new Point3D(0, 0, 0), new Point3D(0, 0, 0), -1, null);
                    lineStartTouched = false;
                    lineEndTouched = false;
                    lastZeroValue = "LINE";
                    return;

                case "BLOCK":
                    currentBlock = new DxfBlock(null);
                    lastZeroValue = "BLOCK";
                    return;

                case "ENDBLK":
                    finishCurrentEntities(false);
                    if (currentBlock != null) {
                        data.addBlock(currentBlock);
                        currentBlock = null;
                    }
                    lastZeroValue = "ENDBLK";
                    return;

                case "INSERT":
                    resetEntityStyle();
                    currentInsert = new DxfInsert(null);
                    currentInsert.setScaleX(1.0);
                    currentInsert.setScaleY(1.0);
                    currentInsert.setScaleZ(1.0);
                    currentInsert.setRotation(0.0);
                    lastZeroValue = "INSERT";
                    return;

                case "EOF":
                    finishCurrentEntities(false);
                    closeLayerRecordIfNeeded();
                    eofReached = true;
                    lastZeroValue = "EOF";
                    return;

                default:
                    lastZeroValue = value;
            }
        }

        void handleCodeValue(String code, String value) {
            // SECTION / TABLE / LAYER NAME / BLOCK NAME / INSERT BLOCK NAME
            if ("2".equals(code)) {
                if ("SECTION".equals(lastZeroValue)) {
                    currentSection = value;
                    return;
                }
                if ("TABLE".equals(lastZeroValue)) {
                    currentTable = value;
                    return;
                }
                if (inLayerRecord && currentLayerRec != null && currentLayerName == null) {
                    currentLayerName = value;
                    currentLayerRec.setProjName(filePath);
                    currentLayerRec.setLayerName(currentLayerName);
                    return;
                }
                if (currentBlock != null && "BLOCK".equals(lastZeroValue) && currentBlock.getName() == null) {
                    currentBlock.setName(value);
                    return;
                }
                if (currentInsert != null && "INSERT".equals(lastZeroValue) && currentInsert.getBlockName() == null) {
                    currentInsert.setBlockName(value);
                    return;
                }
            }

            // Colore layer in TABLE LAYER
            if ("62".equals(code) && inLayerRecord && currentLayerRec != null && currentLayerName != null) {
                Integer aci = parseInt(value);
                if (aci == null) aci = 7;
                currentLayerRec.setColorState(aci);
                layerColors.put(currentLayerName, AutoCADColor.getColor(String.valueOf(aci)));
                data.addLayer(currentLayerRec);
                currentLayerRec = null;
                currentLayerName = null;
                inLayerRecord = false;
                return;
            }

            // Layer entità
            if ("8".equals(code)) {
                applyLayerToActiveEntity(value);
                return;
            }

            // Colore entità (ACI)
            if ("62".equals(code)) {
                Integer aci = parseInt(value);
                if (aci != null) {
                    int argb = AutoCADColor.getColor(String.valueOf(Math.abs(aci)));
                    applyExplicitEntityColor(argb);
                }
                return;
            }

            // True color entità (420 = 0x00RRGGBB)
            if ("420".equals(code)) {
                Integer rgb = parseInt(value);
                if (rgb != null) {
                    int argb = 0xFF000000 | (rgb & 0x00FFFFFF);
                    applyExplicitEntityColor(argb);
                }
                return;
            }

            // Flag polyline chiusa
            if ("70".equals(code)) {
                Integer flags = parseInt(value);
                if (flags != null) {
                    if (currentPolyline != null && !inVertex) {
                        currentPolylineClosed = (flags & 1) != 0;
                    } else if (currentLWPolyline != null) {
                        currentLWPolylineClosed = (flags & 1) != 0;
                    }
                }
            }

            // Elevation LWPOLYLINE
            if ("38".equals(code) && currentLWPolyline != null) {
                Double z = parseDouble(value, conversionFactor);
                if (z != null) currentLWPolylineElevation = z;
                return;
            }

            // BLOCK base point: lasciato volutamente ignorato finché non abbiamo setter certi sul model.
            // Se hai setBaseX/setBaseY/setBaseZ nel tuo DxfBlock, si possono collegare qui.

            // 3DFACE
            if (currentFace != null) {
                parseFace(code, value);
            }

            // POLYLINE old style
            if (currentPolyline != null && inVertex) {
                parsePolylineVertex(code, value);
            }

            // LWPOLYLINE
            if (currentLWPolyline != null) {
                parseLWPolyline(code, value);
            }

            // POINT
            if (currentPoint != null) {
                parsePoint(code, value);
            }

            // DRILL POINT
            if (currentDrillPoint != null) {
                parseDrillPoint(code, value);
            }

            // TEXT
            if (currentText != null) {
                parseText(currentText, code, value, false);
            }

            // MTEXT
            if (currentMText != null) {
                parseText(currentMText, code, value, true);
            }

            // CIRCLE
            if (currentCircle != null) {
                parseCircle(code, value);
            }

            // ARC
            if (currentArc != null) {
                parseArc(code, value);
            }

            // LINE
            if (currentLine != null) {
                parseLine(code, value);
            }

            // INSERT
            if (currentInsert != null) {
                parseInsert(code, value);
            }
        }

        void finishOpenObjectsAtEOF() {
            finishCurrentEntities(false);
            closeLayerRecordIfNeeded();
            if (currentBlock != null) {
                data.addBlock(currentBlock);
                currentBlock = null;
            }
        }

        private void resetEntityStyle() {
            hasExplicitEntityColor = false;
            currentEntityArgb = DEFAULT_ARGB;
        }

        private void closeLayerRecordIfNeeded() {
            if (!inLayerRecord || currentLayerRec == null || currentLayerName == null) {
                inLayerRecord = false;
                currentLayerRec = null;
                currentLayerName = null;
                return;
            }

            if (!layerColors.containsKey(currentLayerName)) {
                currentLayerRec.setColorState(7);
                layerColors.put(currentLayerName, DEFAULT_ARGB);
                data.addLayer(currentLayerRec);
            }

            inLayerRecord = false;
            currentLayerRec = null;
            currentLayerName = null;
        }

        private void applyLayerToActiveEntity(String layerName) {
            Layer layer = resolveLayer(layerName);

            if (currentFace != null) {
                currentFace.setLayer(layer);
                if (hasExplicitEntityColor) {
                    MyColorClass.colorTriangle = currentEntityArgb;
                } else if (layer.getColorState() != null) {
                    MyColorClass.colorTriangle = layer.getColorState();
                }
                return;
            }

            if (currentPolyline != null) {
                currentPolyline.setLayer(layer);
                currentPolyline.setLineColor(hasExplicitEntityColor ? currentEntityArgb : safeLayerColor(layer));
                return;
            }

            if (currentLWPolyline != null) {
                currentLWPolyline.setLayer(layer);
                currentLWPolyline.setLineColor(hasExplicitEntityColor ? currentEntityArgb : safeLayerColor(layer));
                return;
            }

            if (currentPoint != null) {
                currentPoint.setLayer(layer);
                currentPoint.setColore(hasExplicitEntityColor ? currentEntityArgb : safeLayerColor(layer));
                return;
            }

            if (currentText != null) {
                currentText.setLayer(layer);
                currentText.setColore(hasExplicitEntityColor ? currentEntityArgb : safeLayerColor(layer));
                return;
            }

            if (currentMText != null) {
                currentMText.setLayer(layer);
                currentMText.setColore(hasExplicitEntityColor ? currentEntityArgb : safeLayerColor(layer));
                return;
            }

            if (currentCircle != null) {
                currentCircle.setLayer(layer);
                currentCircle.setColor(hasExplicitEntityColor ? currentEntityArgb : safeLayerColor(layer));
                return;
            }

            if (currentArc != null) {
                currentArc.setLayer(layer);
                currentArc.setColor(hasExplicitEntityColor ? currentEntityArgb : safeLayerColor(layer));
                return;
            }

            if (currentLine != null) {
                currentLine.setLayer(layer);
                currentLine.setColor(hasExplicitEntityColor ? currentEntityArgb : safeLayerColor(layer));
                return;
            }

            if (currentInsert != null) {
                currentInsert.setLayer(layer);
            }
        }

        private void applyExplicitEntityColor(int argb) {
            hasExplicitEntityColor = true;
            currentEntityArgb = argb;

            if (currentFace != null) {
                MyColorClass.colorTriangle = argb;
                return;
            }
            if (currentPolyline != null) {
                currentPolyline.setLineColor(argb);
                return;
            }
            if (currentLWPolyline != null) {
                currentLWPolyline.setLineColor(argb);
                return;
            }
            if (currentPoint != null) {
                currentPoint.setColore(argb);
                return;
            }
            if (currentText != null) {
                currentText.setColore(argb);
                return;
            }
            if (currentMText != null) {
                currentMText.setColore(argb);
                return;
            }
            if (currentCircle != null) {
                currentCircle.setColor(argb);
                return;
            }
            if (currentArc != null) {
                currentArc.setColor(argb);
                return;
            }
            if (currentLine != null) {
                currentLine.setColor(argb);
                return;
            }
            if (currentInsert != null) {
                Layer existing = currentInsert.getLayer();
                currentInsert.setLayer(new Layer(filePath, "0", argb, existing != null && existing.getColorState() != null));
            }
        }

        private Layer resolveLayer(String layerName) {
            if (layerName == null || layerName.trim().isEmpty()) {
                layerName = "0";
            }

            Integer argb = layerColors.get(layerName);
            if (argb != null) {
                return new Layer(filePath, layerName, argb, true);
            }

            Integer layer0 = layerColors.get("0");
            int fallback = layer0 != null ? layer0 : DEFAULT_ARGB;
            return new Layer(filePath, layerName, fallback, false);
        }

        private int safeLayerColor(Layer layer) {
            if (layer == null || layer.getColorState() == null || layer.getColorState() == -1) {
                return DEFAULT_ARGB;
            }
            return layer.getColorState();
        }

        private void ensureDefaultLayerOnActiveEntities() {
            Layer defaultLayer = resolveLayer("0");

            if (currentFace != null && currentFace.getLayer() == null) {
                currentFace.setLayer(defaultLayer);
            }
            if (currentPolyline != null && currentPolyline.getLayer() == null) {
                currentPolyline.setLayer(defaultLayer);
                if (!hasExplicitEntityColor) currentPolyline.setLineColor(safeLayerColor(defaultLayer));
            }
            if (currentLWPolyline != null && currentLWPolyline.getLayer() == null) {
                currentLWPolyline.setLayer(defaultLayer);
                if (!hasExplicitEntityColor) currentLWPolyline.setLineColor(safeLayerColor(defaultLayer));
            }
            if (currentPoint != null && currentPoint.getLayer() == null) {
                currentPoint.setLayer(defaultLayer);
                if (!hasExplicitEntityColor) currentPoint.setColore(safeLayerColor(defaultLayer));
            }
            if (currentText != null && currentText.getLayer() == null) {
                currentText.setLayer(defaultLayer);
                if (!hasExplicitEntityColor) currentText.setColore(safeLayerColor(defaultLayer));
            }
            if (currentMText != null && currentMText.getLayer() == null) {
                currentMText.setLayer(defaultLayer);
                if (!hasExplicitEntityColor) currentMText.setColore(safeLayerColor(defaultLayer));
            }
            if (currentCircle != null && currentCircle.getLayer() == null) {
                currentCircle.setLayer(defaultLayer);
                if (!hasExplicitEntityColor) currentCircle.setColor(safeLayerColor(defaultLayer));
            }
            if (currentArc != null && currentArc.getLayer() == null) {
                currentArc.setLayer(defaultLayer);
                if (!hasExplicitEntityColor) currentArc.setColor(safeLayerColor(defaultLayer));
            }
            if (currentLine != null && currentLine.getLayer() == null) {
                currentLine.setLayer(defaultLayer);
                if (!hasExplicitEntityColor) currentLine.setColor(safeLayerColor(defaultLayer));
            }
            if (currentInsert != null && currentInsert.getLayer() == null) {
                currentInsert.setLayer(defaultLayer);
            }
        }

        private void finishCurrentEntities(boolean force) {
            ensureDefaultLayerOnActiveEntities();
            finishFace();
            finishPoint();
            finishDrillPoint();
            finishText();
            finishMText();
            finishCircle();
            finishArc();
            finishLine();
            finishLWPolyline();
            finishInsert();
            if (force || currentPolyline != null) {
                finishCurrentPolyline();
            }
        }

        private void finishFace() {
            if (currentFace == null) return;

            if (currentFace.p1 != null && currentFace.p2 != null && currentFace.p3 != null) {
                if (currentFace.p4 == null) currentFace.p4 = currentFace.p3;
                addFaceToContainer(data, currentBlock, currentFace);
            }
            currentFace = null;
        }

        private void finishCurrentPolyline() {
            if (currentPolyline == null) return;

            if (currentPolylineClosed) {
                closeIfNeeded(currentPolyline.getVertices());
            }

            currentPolyline.markGlDirty();
            if (currentPolyline.getVertices() != null && currentPolyline.getVertices().size() >= 2) {
                addPolylineToContainer(data, currentBlock, currentPolyline);
            }
            currentPolyline = null;
            currentPolylineClosed = false;
        }

        private void finishLWPolyline() {
            if (currentLWPolyline == null) return;

            if (currentLWPolylineClosed) {
                closeIfNeeded(currentLWPolyline.getVertices());
            }

            currentLWPolyline.markGlDirty();
            if (currentLWPolyline.getVertices() != null && currentLWPolyline.getVertices().size() >= 2) {
                addPolyline2DToContainer(data, currentBlock, currentLWPolyline);
            }
            currentLWPolyline = null;
            currentLWPolylineClosed = false;
            currentLWPolylineElevation = 0.0;
        }

        private void finishPoint() {
            if (currentPoint == null) return;

            if (pointTouched) {
                addPointToContainer(data, currentBlock, currentPoint);
            }
            currentPoint = null;
            pointTouched = false;
        }

        private void finishDrillPoint() {
            if (currentDrillPoint == null) return;

            currentDrillPoint.recomputeDerived();
            Double tilt = computeTiltFromEndpoints(currentDrillPoint);
            if (tilt != null) {
                currentDrillPoint.setTilt(tilt);
            }

            if (drillTouched && currentDrillPoint.getHeadX() != null && currentDrillPoint.getHeadY() != null) {
                addDrillPointToContainer(data, currentBlock, currentDrillPoint);
            }
            currentDrillPoint = null;
            drillTouched = false;
        }

        private void finishText() {
            if (currentText == null) return;

            String txt = currentText.getText();
            if (txt != null && !txt.trim().isEmpty()) {
                addTextToContainer(data, currentBlock, currentText);
            }
            currentText = null;
        }

        private void finishMText() {
            if (currentMText == null) return;

            String txt = currentMText.getText();
            if (txt != null && !txt.trim().isEmpty()) {
                currentMText.setText(normalizeMText(txt));
                addTextToContainer(data, currentBlock, currentMText);
            }
            currentMText = null;
        }

        private void finishCircle() {
            if (currentCircle == null) return;

            if (circleCenterTouched && circleRadiusTouched && currentCircle.radius > 0) {
                addCircleToContainer(data, currentBlock, currentCircle);
            }
            currentCircle = null;
            circleCenterTouched = false;
            circleRadiusTouched = false;
        }

        private void finishArc() {
            if (currentArc == null) return;

            if (arcCenterTouched && arcRadiusTouched && arcAnglesTouched && currentArc.radius > 0) {
                addArcToContainer(data, currentBlock, currentArc);
            }
            currentArc = null;
            arcCenterTouched = false;
            arcRadiusTouched = false;
            arcAnglesTouched = false;
        }

        private void finishLine() {
            if (currentLine == null) return;

            if (lineStartTouched && lineEndTouched && !samePoint(currentLine.start, currentLine.end)) {
                addLineToContainer(data, currentBlock, currentLine);
            }
            currentLine = null;
            lineStartTouched = false;
            lineEndTouched = false;
        }

        private void finishInsert() {
            if (currentInsert == null) return;

            if (currentInsert.getBlockName() != null && !currentInsert.getBlockName().trim().isEmpty()) {
                data.addInsert(currentInsert);
            }
            currentInsert = null;
        }

        private void parseFace(String code, String value) {
            Double d = parseDouble(value, conversionFactor);
            if (d == null) return;

            switch (code) {
                case "10":
                    currentFace.p1 = new Point3D(d, 0, 0);
                    return;
                case "20":
                    if (currentFace.p1 != null) currentFace.p1.y = d;
                    return;
                case "30":
                    if (currentFace.p1 != null) currentFace.p1.z = d;
                    return;
                case "11":
                    currentFace.p2 = new Point3D(d, 0, 0);
                    return;
                case "21":
                    if (currentFace.p2 != null) currentFace.p2.y = d;
                    return;
                case "31":
                    if (currentFace.p2 != null) currentFace.p2.z = d;
                    return;
                case "12":
                    currentFace.p3 = new Point3D(d, 0, 0);
                    return;
                case "22":
                    if (currentFace.p3 != null) currentFace.p3.y = d;
                    return;
                case "32":
                    if (currentFace.p3 != null) currentFace.p3.z = d;
                    return;
                case "13":
                    currentFace.p4 = new Point3D(d, 0, 0);
                    return;
                case "23":
                    if (currentFace.p4 != null) currentFace.p4.y = d;
                    return;
                case "33":
                    if (currentFace.p4 != null) currentFace.p4.z = d;
            }
        }

        private void parsePolylineVertex(String code, String value) {
            switch (code) {
                case "10": {
                    Double x = parseDouble(value, conversionFactor);
                    if (x != null) currentPolyline.getVertices().add(new Point3D(x, 0, 0));
                    return;
                }
                case "20": {
                    Double y = parseDouble(value, conversionFactor);
                    if (y != null && !currentPolyline.getVertices().isEmpty()) {
                        Point3D last = currentPolyline.getVertices().get(currentPolyline.getVertices().size() - 1);
                        last.y = y;
                    }
                    return;
                }
                case "30": {
                    Double z = parseDouble(value, conversionFactor);
                    if (z != null && !currentPolyline.getVertices().isEmpty()) {
                        Point3D last = currentPolyline.getVertices().get(currentPolyline.getVertices().size() - 1);
                        last.z = z;
                    }
                }
            }
        }

        private void parseLWPolyline(String code, String value) {
            switch (code) {
                case "10": {
                    Double x = parseDouble(value, conversionFactor);
                    if (x != null) {
                        currentLWPolyline.addVertex(new Point3D(x, 0, currentLWPolylineElevation));
                    }
                    return;
                }
                case "20": {
                    Double y = parseDouble(value, conversionFactor);
                    if (y != null && !currentLWPolyline.getVertices().isEmpty()) {
                        Point3D last = currentLWPolyline.getVertices().get(currentLWPolyline.getVertices().size() - 1);
                        last.y = y;
                        last.z = currentLWPolylineElevation;
                    }
                    return;
                }
                case "42": {
                    Double bulge = parseDouble(value, 1.0);
                    if (bulge != null && !currentLWPolyline.getVertices().isEmpty()) {
                        Point3D last = currentLWPolyline.getVertices().get(currentLWPolyline.getVertices().size() - 1);
                        last.setBulge(bulge);
                    }
                }
            }
        }

        private void parsePoint(String code, String value) {
            Double d = parseDouble(value, conversionFactor);
            if (d == null) return;

            switch (code) {
                case "10":
                    currentPoint.x = d;
                    pointTouched = true;
                    return;
                case "20":
                    currentPoint.y = d;
                    pointTouched = true;
                    return;
                case "30":
                    currentPoint.z = d;
                    pointTouched = true;
            }
        }

        private void parseDrillPoint(String code, String value) {
            Double d = parseDouble(value, conversionFactor);
            if (d == null) return;

            currentDrillPoint.setTilt(0.0d);

            switch (code) {
                case "10":
                    currentDrillPoint.setHeadX(d);
                    if (currentDrillPoint.getEndX() == null) currentDrillPoint.setEndX(d);
                    drillTouched = true;
                    break;
                case "20":
                    currentDrillPoint.setHeadY(d);
                    if (currentDrillPoint.getEndY() == null) currentDrillPoint.setEndY(d);
                    drillTouched = true;
                    break;
                case "30":
                    currentDrillPoint.setHeadZ(d);
                    if (currentDrillPoint.getEndZ() == null) currentDrillPoint.setEndZ(d);
                    drillTouched = true;
                    break;
                case "11":
                    currentDrillPoint.setEndX(d);
                    drillTouched = true;
                    break;
                case "21":
                    currentDrillPoint.setEndY(d);
                    drillTouched = true;
                    break;
                case "31":
                    currentDrillPoint.setEndZ(d);
                    drillTouched = true;
                    break;
                default:
                    break;
            }

            currentDrillPoint.recomputeDerived();
        }

        private void parseText(DxfText target, String code, String value, boolean multiLine) {
            switch (code) {
                case "10": {
                    Double x = parseDouble(value, conversionFactor);
                    if (x != null) target.x = x;
                    return;
                }
                case "20": {
                    Double y = parseDouble(value, conversionFactor);
                    if (y != null) target.y = y;
                    return;
                }
                case "30": {
                    Double z = parseDouble(value, conversionFactor);
                    if (z != null) target.z = z;
                    return;
                }
                case "1":
                    appendText(target, value, multiLine);
                    return;
                case "3":
                    if (multiLine) appendText(target, value, true);
                    return;
                case "50": {
                    Double rot = parseDouble(value, 1.0);
                    if (rot != null) target.setRotation(rot);
                }
            }
        }

        private void parseCircle(String code, String value) {
            Double d = parseDouble(value, conversionFactor);
            if (d == null) return;

            switch (code) {
                case "10":
                    currentCircle.center.x = d;
                    circleCenterTouched = true;
                    return;
                case "20":
                    currentCircle.center.y = d;
                    circleCenterTouched = true;
                    return;
                case "30":
                    currentCircle.center.z = d;
                    circleCenterTouched = true;
                    return;
                case "40":
                    currentCircle.radius = d;
                    circleRadiusTouched = true;
            }
        }

        private void parseArc(String code, String value) {
            switch (code) {
                case "10": {
                    Double x = parseDouble(value, conversionFactor);
                    if (x != null) {
                        currentArc.center.x = x;
                        arcCenterTouched = true;
                    }
                    return;
                }
                case "20": {
                    Double y = parseDouble(value, conversionFactor);
                    if (y != null) {
                        currentArc.center.y = y;
                        arcCenterTouched = true;
                    }
                    return;
                }
                case "30": {
                    Double z = parseDouble(value, conversionFactor);
                    if (z != null) {
                        currentArc.center.z = z;
                        arcCenterTouched = true;
                    }
                    return;
                }
                case "40": {
                    Double r = parseDouble(value, conversionFactor);
                    if (r != null) {
                        currentArc.radius = r;
                        arcRadiusTouched = true;
                    }
                    return;
                }
                case "50": {
                    Double a = parseDouble(value, 1.0);
                    if (a != null) {
                        currentArc.setStartAngle(a);
                        arcAnglesTouched = true;
                    }
                    return;
                }
                case "51": {
                    Double a = parseDouble(value, 1.0);
                    if (a != null) {
                        currentArc.setEndAngle(a);
                        arcAnglesTouched = true;
                    }
                }
            }
        }

        private void parseLine(String code, String value) {
            Double d = parseDouble(value, conversionFactor);
            if (d == null) return;

            switch (code) {
                case "10":
                    currentLine.start.x = d;
                    lineStartTouched = true;
                    return;
                case "20":
                    currentLine.start.y = d;
                    lineStartTouched = true;
                    return;
                case "30":
                    currentLine.start.z = d;
                    lineStartTouched = true;
                    return;
                case "11":
                    currentLine.end.x = d;
                    lineEndTouched = true;
                    return;
                case "21":
                    currentLine.end.y = d;
                    lineEndTouched = true;
                    return;
                case "31":
                    currentLine.end.z = d;
                    lineEndTouched = true;
            }
        }

        private void parseInsert(String code, String value) {
            switch (code) {
                case "10": {
                    Double x = parseDouble(value, conversionFactor);
                    if (x != null) currentInsert.setPosition(x, currentInsert.getY(), currentInsert.getZ());
                    return;
                }
                case "20": {
                    Double y = parseDouble(value, conversionFactor);
                    if (y != null) currentInsert.setPosition(currentInsert.getX(), y, currentInsert.getZ());
                    return;
                }
                case "30": {
                    Double z = parseDouble(value, conversionFactor);
                    if (z != null) currentInsert.setPosition(currentInsert.getX(), currentInsert.getY(), z);
                    return;
                }
                case "41": {
                    Double sx = parseDouble(value, 1.0);
                    if (sx != null) currentInsert.setScaleX(sx);
                    return;
                }
                case "42": {
                    Double sy = parseDouble(value, 1.0);
                    if (sy != null) currentInsert.setScaleY(sy);
                    return;
                }
                case "43": {
                    Double sz = parseDouble(value, 1.0);
                    if (sz != null) currentInsert.setScaleZ(sz);
                    return;
                }
                case "50": {
                    Double rot = parseDouble(value, 1.0);
                    if (rot != null) currentInsert.setRotation(rot);
                }
            }
        }

        private void appendText(DxfText target, String piece, boolean multiLine) {
            if (piece == null) return;

            String existing = target.getText();
            if (existing == null || existing.isEmpty()) {
                target.setText(piece);
            } else if (multiLine) {
                target.setText(existing + "\n" + piece);
            } else {
                target.setText(existing + piece);
            }
        }

        private String normalizeMText(String s) {
            if (s == null) return null;
            return s
                    .replace("\\P", "\n")
                    .replace("\\~", " ")
                    .replace("\\\\", "\\");
        }
    }

    private static Double parseDouble(String s, double factor) {
        try {
            return Double.parseDouble(s.trim()) * factor;
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean samePoint(Point3D a, Point3D b) {
        if (a == null || b == null) return false;
        return Double.compare(a.x, b.x) == 0
                && Double.compare(a.y, b.y) == 0
                && Double.compare(a.z, b.z) == 0;
    }

    private static void closeIfNeeded(java.util.List<Point3D> vertices) {
        if (vertices == null || vertices.size() < 2) return;

        Point3D first = vertices.get(0);
        Point3D last = vertices.get(vertices.size() - 1);

        if (!samePoint(first, last)) {
            Point3D copy = new Point3D(first.x, first.y, first.z);
            copy.setBulge(first.getBulge());
            vertices.add(copy);
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

    private static void addDrillPointToContainer(DXFData data, DxfBlock block, Point3D_Drill point3DDrill) {
        if (block != null) block.addPointDrill(point3DDrill);
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
    //   EXPLODE BLOCKS
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
                if (ins.getLayer() != null) c.setLayer(ins.getLayer());
                data.addLine(c);
            }

            for (Polyline pl : block.getPolylines()) {
                Polyline c = clonePolyline(pl);
                transformPolyline(c, ins, bx, by, bz, cosA, sinA);
                if (ins.getLayer() != null) c.setLayer(ins.getLayer());
                c.markGlDirty();
                data.addPolyline(c);
            }

            for (Polyline_2D pl : block.getPolylines2D()) {
                Polyline_2D c = clonePolyline2D(pl);
                transformPolyline2D(c, ins, bx, by, bz, cosA, sinA);
                if (ins.getLayer() != null) c.setLayer(ins.getLayer());
                c.markGlDirty();
                data.addPolyline2D(c);
            }

            for (Circle cc : block.getCircles()) {
                Circle c = cc.clone();
                transformCircle(c, ins, bx, by, bz, cosA, sinA);
                if (ins.getLayer() != null) c.setLayer(ins.getLayer());
                data.addCircle(c);
            }

            for (Arc ac : block.getArcs()) {
                Arc c = ac.clone();
                transformArc(c, ins, bx, by, bz, cosA, sinA);
                if (ins.getLayer() != null) c.setLayer(ins.getLayer());
                data.addArc(c);
            }

            for (Point3D p : block.getPoints()) {
                Point3D c = new Point3D(p.x, p.y, p.z);
                transformPoint(c, ins, bx, by, bz, cosA, sinA);
                if (ins.getLayer() != null) c.setLayer(ins.getLayer());
                data.addPoint(c);
            }

            for (DxfText t : block.getTexts()) {
                DxfText c = t.clone();
                transformText(c, ins, bx, by, bz, cosA, sinA);
                if (ins.getLayer() != null) c.setLayer(ins.getLayer());
                data.addText(c);
            }

            for (Face3D f : block.getFaces()) {
                Face3D c = f.clone();
                transformFace3D(c, ins, bx, by, bz, cosA, sinA);
                if (ins.getLayer() != null) c.setLayer(ins.getLayer());
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
            Point3D pp = new Point3D(p.x, p.y, p.z);
            pp.setBulge(p.getBulge());
            c.getVertices().add(pp);
        }
        c.markGlDirty();
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
        c.markGlDirty();
        return c;
    }

    private static void transformLine(Line l, DxfInsert ins,
                                      double bx, double by, double bz,
                                      double cosA, double sinA) {
        transformPoint(l.start, ins, bx, by, bz, cosA, sinA);
        transformPoint(l.end, ins, bx, by, bz, cosA, sinA);
    }

    private static Double computeTiltFromEndpoints(Point3D_Drill p) {
        if (p.getHeadX() == null || p.getHeadY() == null || p.getHeadZ() == null ||
                p.getEndX() == null || p.getEndY() == null || p.getEndZ() == null) {
            return null;
        }

        double dx = p.getEndX() - p.getHeadX();
        double dy = p.getEndY() - p.getHeadY();
        double dz = p.getEndZ() - p.getHeadZ();

        double horiz = Math.sqrt(dx * dx + dy * dy);
        double vert = Math.abs(dz);

        if (horiz == 0 && vert == 0) return 0.0;
        return Math.toDegrees(Math.atan2(horiz, vert)); // 0 verticale, 90 orizzontale
    }
}
