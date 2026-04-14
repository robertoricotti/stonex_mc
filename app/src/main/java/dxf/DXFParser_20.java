package dxf;

import static services.ReadProjectService.isFinishedDTM;
import static services.ReadProjectService.isFinishedPOINT;
import static services.ReadProjectService.isFinishedPOLY;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import services.ReadProjectService;

/**
 * Parser DXF ASCII tollerante alle versioni (R12+ / AC1009 e successive)
 * per le entità usate più spesso dal progetto.
 * <p>
 * Supporto principale:
 * - HEADER / TABLES / BLOCKS / ENTITIES
 * - LAYER table con fallback anche se incompleta
 * - LINE, POINT, TEXT, MTEXT, CIRCLE, ARC, 3DFACE
 * - POLYLINE + VERTEX + SEQEND
 * - LWPOLYLINE
 * - BLOCK / INSERT con esplosione
 * - colori: explicit ACI (62), true color (420), BYLAYER (256), BYBLOCK (0)
 * - layer 0 nei blocchi: eredita il layer dell'INSERT
 * <p>
 * Nota onesta: non esiste un parser "universale" che legga davvero TUTTE le varianti DXF
 * senza implementare l'intera specifica Autodesk. Questa classe è però pensata per essere
 * version-tolerant: ignora i group code ignoti, non dipende rigidamente dalla versione e
 * gestisce correttamente il caso classico ByLayer / ByBlock.
 */
public class DXFParser_20 {
    static final Map<String, Layer> layerRegistry = new HashMap<>();
    private static final String TAG = "DXFParser_20";
    private static final int DEFAULT_ARGB = AutoCADColor.getColor("7");

    /**
     * colori layer risolti in ARGB
     */
    static final Map<String, Integer> layerColors = new HashMap<>();

    /**
     * metadati stile per applicare BYLAYER / BYBLOCK anche dopo l'esplosione dei blocchi
     */
    private static final IdentityHashMap<Object, DxfStyle> entityStyles = new IdentityHashMap<>();

    static long drillPointNr = 0;

    public static DXFData parseDXF(String filePath, double conversionFactor) {
        DXFData dxfData = new DXFData();
        layerColors.clear();
        entityStyles.clear();
        layerRegistry.clear();
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

        explodeBlocks(dxfData, filePath);
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

        String currentHeaderVar = null;
        String acadVersion = null;

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

        DxfStyle activeStyle = new DxfStyle();

        ParserState(String filePath, double conversionFactor, DXFData data) {
            this.filePath = filePath;
            this.conversionFactor = conversionFactor;
            this.data = data;
        }

        void handleZero(String value) {
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
                    currentHeaderVar = null;
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
                    resetDxfStyle();
                    currentFace = new Face3D(null, null, null, null, DEFAULT_ARGB, null);
                    lastZeroValue = "3DFACE";
                    return;

                case "POLYLINE":
                    resetDxfStyle();
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
                    resetDxfStyle();
                    currentLWPolyline = new Polyline_2D();
                    currentLWPolylineClosed = false;
                    currentLWPolylineElevation = 0.0;
                    lastZeroValue = "LWPOLYLINE";
                    return;

                case "POINT":
                    resetDxfStyle();
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
                    resetDxfStyle();
                    currentText = new DxfText("", 0, 0, 0, -1, null);
                    lastZeroValue = "TEXT";
                    return;

                case "MTEXT":
                    resetDxfStyle();
                    currentMText = new DxfText("", 0, 0, 0, -1, null);
                    lastZeroValue = "MTEXT";
                    return;

                case "CIRCLE":
                    resetDxfStyle();
                    currentCircle = new Circle(new Point3D(0, 0, 0), 0, -1, null);
                    circleCenterTouched = false;
                    circleRadiusTouched = false;
                    lastZeroValue = "CIRCLE";
                    return;

                case "ARC":
                    resetDxfStyle();
                    currentArc = new Arc(new Point3D(0, 0, 0), 0, 0, 0, -1, null);
                    arcCenterTouched = false;
                    arcRadiusTouched = false;
                    arcAnglesTouched = false;
                    lastZeroValue = "ARC";
                    return;

                case "LINE":
                    resetDxfStyle();
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
                    resetDxfStyle();
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
            // HEADER variabili
            if ("9".equals(code) && "HEADER".equals(currentSection)) {
                currentHeaderVar = value;
                return;
            }
            if ("1".equals(code) && "$ACADVER".equals(currentHeaderVar)) {
                acadVersion = value;
                return;
            }

            // SECTION / TABLE / nome layer / nome block / nome block insert
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

            // nome block alternativo (alcuni DXF usano 3)
            if ("3".equals(code)) {
                if (currentBlock != null && currentBlock.getName() == null) {
                    currentBlock.setName(value);
                    return;
                }
                if (currentMText != null) {
                    appendText(currentMText, value, true);
                    return;
                }
            }

            // base point blocco: 10/20/30 nel record BLOCK
            if (currentBlock != null && "BLOCK".equals(lastZeroValue)) {
                if ("10".equals(code)) {
                    Double x = parseDouble(value, conversionFactor);
                    if (x != null) currentBlock.setBaseX(x);
                    return;
                }
                if ("20".equals(code)) {
                    Double y = parseDouble(value, conversionFactor);
                    if (y != null) currentBlock.setBaseY(y);
                    return;
                }
                if ("30".equals(code)) {
                    Double z = parseDouble(value, conversionFactor);
                    if (z != null) currentBlock.setBaseZ(z);
                    return;
                }
            }

            // colore layer nella TABLE LAYER
            if ("62".equals(code) && inLayerRecord && currentLayerRec != null && currentLayerName != null) {
                Integer aci = parseInt(value);
                if (aci == null) aci = 7;

                boolean enabled = aci >= 0;
                int visibleAci = Math.abs(aci);
                if (visibleAci == 0) visibleAci = 7;

                int argb = AutoCADColor.getColor(String.valueOf(visibleAci));

                currentLayerRec.setEnable(enabled);
                currentLayerRec.setColorState(argb);

                layerColors.put(currentLayerName, argb);
                layerRegistry.put(currentLayerName, currentLayerRec);

                data.addLayer(currentLayerRec);
                currentLayerRec = null;
                currentLayerName = null;
                inLayerRecord = false;
                return;
            }

            // layer entità
            if ("8".equals(code)) {
                activeStyle.setLayerName(sanitizeLayerName(value));
                applyLayerToActiveEntity(activeStyle.getLayerName());
                return;
            }

            // colore entità: ACI / BYBLOCK / BYLAYER
            if ("62".equals(code)) {
                applyColorCodeToActiveEntity(value);
                return;
            }

            // true color entità
            if ("420".equals(code)) {
                Integer rgb = parseInt(value);
                if (rgb != null) {
                    activeStyle.setColorMode(DxfStyle.ColorMode.EXPLICIT);
                    activeStyle.setExplicitArgb(0xFF000000 | (rgb & 0x00FFFFFF));
                    repaintActiveEntity();
                }
                return;
            }

            // POLYLINE / LWPOLYLINE chiusa
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

            if ("38".equals(code) && currentLWPolyline != null) {
                Double z = parseDouble(value, conversionFactor);
                if (z != null) currentLWPolylineElevation = z;
                return;
            }

            if (currentFace != null) {
                parseFace(code, value);
            }

            if (currentPolyline != null && inVertex) {
                parsePolylineVertex(code, value);
            }

            if (currentLWPolyline != null) {
                parseLWPolyline(code, value);
            }

            if (currentPoint != null) {
                parsePoint(code, value);
            }

            if (currentDrillPoint != null) {
                parseDrillPoint(code, value);
            }

            if (currentText != null) {
                parseText(currentText, code, value, false);
            }

            if (currentMText != null) {
                parseText(currentMText, code, value, true);
            }

            if (currentCircle != null) {
                parseCircle(code, value);
            }

            if (currentArc != null) {
                parseArc(code, value);
            }

            if (currentLine != null) {
                parseLine(code, value);
            }

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

        private void resetDxfStyle() {
            activeStyle = new DxfStyle();
            activeStyle.setLayerName("0");
            activeStyle.setColorMode(DxfStyle.ColorMode.BYLAYER);
            activeStyle.setExplicitArgb(null);
        }

        private void closeLayerRecordIfNeeded() {
            if (!inLayerRecord || currentLayerRec == null || currentLayerName == null) {
                inLayerRecord = false;
                currentLayerRec = null;
                currentLayerName = null;
                return;
            }

            if (!layerColors.containsKey(currentLayerName)) {
                currentLayerRec.setEnable(true);
                currentLayerRec.setColorState(DEFAULT_ARGB);
                layerColors.put(currentLayerName, DEFAULT_ARGB);
                layerRegistry.put(currentLayerName, currentLayerRec);
                data.addLayer(currentLayerRec);
            }

            inLayerRecord = false;
            currentLayerRec = null;
            currentLayerName = null;
        }
        private void applyColorCodeToActiveEntity(String value) {
            Integer aci = parseInt(value);
            if (aci == null) return;

            int abs = Math.abs(aci);
            if (abs == 0) {
                activeStyle.setColorMode(DxfStyle.ColorMode.BYBLOCK);
                activeStyle.setExplicitArgb(null);
            } else if (abs == 256) {
                activeStyle.setColorMode(DxfStyle.ColorMode.BYLAYER);
                activeStyle.setExplicitArgb(null);
            } else {
                activeStyle.setColorMode(DxfStyle.ColorMode.EXPLICIT);
                activeStyle.setExplicitArgb(AutoCADColor.getColor(String.valueOf(abs)));
            }

            repaintActiveEntity();
        }

        private void repaintActiveEntity() {
            Layer effectiveLayer = resolveLayer(filePath, activeStyle.getLayerName());
            int color = resolvePreviewColor(activeStyle, effectiveLayer, currentBlock != null);

            if (currentFace != null) {
                currentFace.setLayer(effectiveLayer);
                currentFace.setColor(color);
                return;
            }
            if (currentPolyline != null) {
                currentPolyline.setLayer(effectiveLayer);
                currentPolyline.setLineColor(color);
                return;
            }
            if (currentLWPolyline != null) {
                currentLWPolyline.setLayer(effectiveLayer);
                currentLWPolyline.setLineColor(color);
                return;
            }
            if (currentPoint != null) {
                currentPoint.setLayer(effectiveLayer);
                currentPoint.setColore(color);
                return;
            }
            if (currentText != null) {
                currentText.setLayer(effectiveLayer);
                currentText.setColore(color);
                return;
            }
            if (currentMText != null) {
                currentMText.setLayer(effectiveLayer);
                currentMText.setColore(color);
                return;
            }
            if (currentCircle != null) {
                currentCircle.setLayer(effectiveLayer);
                currentCircle.setColor(color);
                return;
            }
            if (currentArc != null) {
                currentArc.setLayer(effectiveLayer);
                currentArc.setColor(color);
                return;
            }
            if (currentLine != null) {
                currentLine.setLayer(effectiveLayer);
                currentLine.setColor(color);
                return;
            }
            if (currentInsert != null) {
                currentInsert.setLayer(effectiveLayer);
            }
        }

        private void applyLayerToActiveEntity(String layerName) {
            activeStyle.setLayerName(sanitizeLayerName(layerName));
            repaintActiveEntity();
        }

        private int resolvePreviewColor(DxfStyle style, Layer layer, boolean insideBlock) {
            if (style == null) return DEFAULT_ARGB;
            switch (style.getColorMode()) {
                case EXPLICIT:
                    return style.getExplicitArgb() != null ? style.getExplicitArgb() : DEFAULT_ARGB;
                case BYBLOCK:
                    return insideBlock ? DEFAULT_ARGB : DEFAULT_ARGB;
                case BYLAYER:
                default:
                    return safeLayerColor(layer);
            }
        }

        private void ensureDefaultLayerOnActiveEntities() {
            if (activeStyle.getLayerName() == null || activeStyle.getLayerName().trim().isEmpty()) {
                activeStyle.setLayerName("0");
            }
            repaintActiveEntity();
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
                rememberStyle(currentFace, activeStyle);
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
                rememberStyle(currentPolyline, activeStyle);
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
                rememberStyle(currentLWPolyline, activeStyle);
                addPolyline2DToContainer(data, currentBlock, currentLWPolyline);
            }
            currentLWPolyline = null;
            currentLWPolylineClosed = false;
            currentLWPolylineElevation = 0.0;
        }

        private void finishPoint() {
            if (currentPoint == null) return;
            if (pointTouched) {
                rememberStyle(currentPoint, activeStyle);
                addPointToContainer(data, currentBlock, currentPoint);
            }
            currentPoint = null;
            pointTouched = false;
        }

        private void finishDrillPoint() {
            if (currentDrillPoint == null) return;
            currentDrillPoint.recomputeDerived();
            Double tilt = computeTiltFromEndpoints(currentDrillPoint);
            if (tilt != null) currentDrillPoint.setTilt(tilt);
            if (drillTouched && currentDrillPoint.getHeadX() != null && currentDrillPoint.getHeadY() != null) {
                rememberStyle(currentDrillPoint, activeStyle);
                addDrillPointToContainer(data, currentBlock, currentDrillPoint);
            }
            currentDrillPoint = null;
            drillTouched = false;
        }

        private void finishText() {
            if (currentText == null) return;
            String txt = currentText.getText();
            if (txt != null && !txt.trim().isEmpty()) {
                rememberStyle(currentText, activeStyle);
                addTextToContainer(data, currentBlock, currentText);
            }
            currentText = null;
        }

        private void finishMText() {
            if (currentMText == null) return;
            String txt = currentMText.getText();
            if (txt != null && !txt.trim().isEmpty()) {
                currentMText.setText(normalizeMText(txt));
                rememberStyle(currentMText, activeStyle);
                addTextToContainer(data, currentBlock, currentMText);
            }
            currentMText = null;
        }

        private void finishCircle() {
            if (currentCircle == null) return;
            if (circleCenterTouched && circleRadiusTouched && currentCircle.radius > 0) {
                rememberStyle(currentCircle, activeStyle);
                addCircleToContainer(data, currentBlock, currentCircle);
            }
            currentCircle = null;
            circleCenterTouched = false;
            circleRadiusTouched = false;
        }

        private void finishArc() {
            if (currentArc == null) return;
            if (arcCenterTouched && arcRadiusTouched && arcAnglesTouched && currentArc.radius > 0) {
                rememberStyle(currentArc, activeStyle);
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
                rememberStyle(currentLine, activeStyle);
                addLineToContainer(data, currentBlock, currentLine);
            }
            currentLine = null;
            lineStartTouched = false;
            lineEndTouched = false;
        }

        private void finishInsert() {
            if (currentInsert == null) return;
            if (currentInsert.getBlockName() != null && !currentInsert.getBlockName().trim().isEmpty()) {
                rememberStyle(currentInsert, activeStyle);
                addInsertToContainer(data, currentBlock, currentInsert);
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
                    return;
                default:
                    return;
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
                    return;
                }
                case "42": {
                    Double bulge = parseDouble(value, 1.0);
                    if (bulge != null && !currentPolyline.getVertices().isEmpty()) {
                        Point3D last = currentPolyline.getVertices().get(currentPolyline.getVertices().size() - 1);
                        last.setBulge(bulge);
                    }
                }
            }
        }

        private void parseLWPolyline(String code, String value) {
            switch (code) {
                case "10": {
                    Double x = parseDouble(value, conversionFactor);
                    if (x != null)
                        currentLWPolyline.addVertex(new Point3D(x, 0, currentLWPolylineElevation));
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
                    return;
                }
                default:
                    return;
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
                    return;
                default:
                    return;
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
                    return;
                }
                default:
                    return;
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
                    return;
                default:
                    return;
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
                    return;
                }
                default:
                    return;
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
                    return;
                default:
                    return;
            }
        }

        private void parseInsert(String code, String value) {
            switch (code) {
                case "10": {
                    Double x = parseDouble(value, conversionFactor);
                    if (x != null)
                        currentInsert.setPosition(x, currentInsert.getY(), currentInsert.getZ());
                    return;
                }
                case "20": {
                    Double y = parseDouble(value, conversionFactor);
                    if (y != null)
                        currentInsert.setPosition(currentInsert.getX(), y, currentInsert.getZ());
                    return;
                }
                case "30": {
                    Double z = parseDouble(value, conversionFactor);
                    if (z != null)
                        currentInsert.setPosition(currentInsert.getX(), currentInsert.getY(), z);
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
                    return;
                }
                default:
                    return;
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

    private static void rememberStyle(Object entity, DxfStyle style) {
        if (entity == null || style == null) return;
        DxfStyle copy = style.copy();
        entityStyles.put(entity, copy);
        attachStyleIfPossible(entity, copy);
    }

    private static void attachStyleIfPossible(Object entity, DxfStyle style) {
        if (entity instanceof Face3D) {
            ((Face3D) entity).setDxfStyle(style);
        } else if (entity instanceof Polyline) {
            ((Polyline) entity).setDxfStyle(style);
        } else if (entity instanceof Polyline_2D) {
            ((Polyline_2D) entity).setDxfStyle(style);
        } else if (entity instanceof Point3D) {
            ((Point3D) entity).setDxfStyle(style);
        } else if (entity instanceof Line) {
            ((Line) entity).setDxfStyle(style);
        } else if (entity instanceof Arc) {
            ((Arc) entity).setDxfStyle(style);
        }
    }

    private static DxfStyle styleOf(Object entity) {
        if (entity == null) return null;
        if (entity instanceof Face3D) {
            DxfStyle style = ((Face3D) entity).getDxfStyle();
            if (style != null) return style;
        } else if (entity instanceof Polyline) {
            DxfStyle style = ((Polyline) entity).getDxfStyle();
            if (style != null) return style;
        } else if (entity instanceof Polyline_2D) {
            DxfStyle style = ((Polyline_2D) entity).getDxfStyle();
            if (style != null) return style;
        } else if (entity instanceof Point3D) {
            DxfStyle style = ((Point3D) entity).getDxfStyle();
            if (style != null) return style;
        } else if (entity instanceof Line) {
            DxfStyle style = ((Line) entity).getDxfStyle();
            if (style != null) return style;
        } else if (entity instanceof Arc) {
            DxfStyle style = ((Arc) entity).getDxfStyle();
            if (style != null) return style;
        }
        return entityStyles.get(entity);
    }

    private static String sanitizeLayerName(String layerName) {
        if (layerName == null || layerName.trim().isEmpty()) return "0";
        return layerName.trim();
    }

    private static Layer resolveLayer(String filePath, String layerName) {
        String name = sanitizeLayerName(layerName);

        Layer existing = layerRegistry.get(name);
        if (existing != null) {
            return existing;
        }

        Integer argb = layerColors.get(name);
        if (argb != null) {
            Layer l = new Layer(filePath, name, argb, true);
            layerRegistry.put(name, l);
            return l;
        }

        Layer layer0 = layerRegistry.get("0");
        if ("0".equals(name) && layer0 != null) {
            return layer0;
        }

        Integer c0 = layerColors.get("0");
        int fallback = c0 != null ? c0 : DEFAULT_ARGB;

        Layer fallbackLayer = new Layer(filePath, name, fallback, true);
        layerRegistry.put(name, fallbackLayer);
        return fallbackLayer;
    }

    private static int safeLayerColor(Layer layer) {
        if (layer == null || layer.getColorState() == null || layer.getColorState() == -1) {
            return DEFAULT_ARGB;
        }
        return layer.getColorState();
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

    private static void closeIfNeeded(List<Point3D> vertices) {
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
    // HELPERS CONTAINER
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

    private static void addInsertToContainer(DXFData data, DxfBlock block, DxfInsert insert) {
        if (block != null) block.addInsert(insert);
        else data.addInsert(insert);
    }

    // ============================
    // ESPLOSIONE BLOCCHI con BYLAYER/BYBLOCK
    // ============================

    private static void explodeBlocks(DXFData data, String filePath) {
        if (data.getBlocks().isEmpty() || data.getInserts().isEmpty()) return;

        Map<String, DxfBlock> blockMap = new HashMap<>();
        for (DxfBlock b : data.getBlocks()) {
            blockMap.put(b.getName(), b);
        }

        for (DxfInsert ins : data.getInserts()) {
            explodeInsertRecursive(data, blockMap, ins, filePath, null, DEFAULT_ARGB,
                    new ArrayList<InsertTransform>(), new HashSet<String>());
        }
    }

    private static void explodeInsertRecursive(DXFData data,
                                               Map<String, DxfBlock> blockMap,
                                               DxfInsert ins,
                                               String filePath,
                                               Layer parentInsertLayer,
                                               int parentInsertColor,
                                               List<InsertTransform> outerTransforms,
                                               Set<String> visitingBlocks) {
        if (ins == null || ins.getBlockName() == null) return;

        DxfBlock block = blockMap.get(ins.getBlockName());
        if (block == null) return;
        if (!visitingBlocks.add(block.getName())) return;

        try {
            DxfStyle insertStyle = styleOf(ins);
            if (insertStyle == null) insertStyle = new DxfStyle();

            Layer insertLayer = resolveExplodedLayer(filePath, insertStyle, ins.getLayer(), parentInsertLayer);
            int insertColor = resolveExplodedColor(insertStyle, insertLayer, parentInsertColor);

            InsertTransform currentTransform = new InsertTransform(ins, block.getBaseX(), block.getBaseY(), block.getBaseZ());
            List<InsertTransform> nextOuterTransforms = new ArrayList<>();
            nextOuterTransforms.add(currentTransform);
            nextOuterTransforms.addAll(outerTransforms);

            for (Line ln : block.getLines()) {
                Line c = ln.clone();
                DxfStyle style = styleOf(ln);
                Layer finalLayer = resolveExplodedLayer(filePath, style, ln.getLayer(), insertLayer);
                int finalColor = resolveExplodedColor(style, finalLayer, insertColor);
                applyTransformsToLine(c, nextOuterTransforms);
                c.setLayer(finalLayer);
                c.setColor(finalColor);
                data.addLine(c);
            }

            for (Polyline pl : block.getPolylines()) {
                Polyline c = clonePolyline(pl);
                DxfStyle style = styleOf(pl);
                Layer finalLayer = resolveExplodedLayer(filePath, style, pl.getLayer(), insertLayer);
                int finalColor = resolveExplodedColor(style, finalLayer, insertColor);
                applyTransformsToPolyline(c, nextOuterTransforms);
                c.setLayer(finalLayer);
                c.setLineColor(finalColor);
                c.markGlDirty();
                data.addPolyline(c);
            }

            for (Polyline_2D pl : block.getPolylines2D()) {
                Polyline_2D c = clonePolyline2D(pl);
                DxfStyle style = styleOf(pl);
                Layer finalLayer = resolveExplodedLayer(filePath, style, pl.getLayer(), insertLayer);
                int finalColor = resolveExplodedColor(style, finalLayer, insertColor);
                applyTransformsToPolyline2D(c, nextOuterTransforms);
                c.setLayer(finalLayer);
                c.setLineColor(finalColor);
                c.markGlDirty();
                data.addPolyline2D(c);
            }

            for (Circle cc : block.getCircles()) {
                Circle c = cc.clone();
                DxfStyle style = styleOf(cc);
                Layer finalLayer = resolveExplodedLayer(filePath, style, cc.getLayer(), insertLayer);
                int finalColor = resolveExplodedColor(style, finalLayer, insertColor);
                applyTransformsToCircle(c, nextOuterTransforms);
                c.setLayer(finalLayer);
                c.setColor(finalColor);
                data.addCircle(c);
            }

            for (Arc ac : block.getArcs()) {
                Arc c = ac.clone();
                DxfStyle style = styleOf(ac);
                Layer finalLayer = resolveExplodedLayer(filePath, style, ac.getLayer(), insertLayer);
                int finalColor = resolveExplodedColor(style, finalLayer, insertColor);
                applyTransformsToArc(c, nextOuterTransforms);
                c.setLayer(finalLayer);
                c.setColor(finalColor);
                data.addArc(c);
            }

            for (Point3D p : block.getPoints()) {
                Point3D c = p.clone();
                DxfStyle style = styleOf(p);
                Layer finalLayer = resolveExplodedLayer(filePath, style, p.getLayer(), insertLayer);
                int finalColor = resolveExplodedColor(style, finalLayer, insertColor);
                applyTransformsToPoint(c, nextOuterTransforms);
                c.setLayer(finalLayer);
                c.setColore(finalColor);
                data.addPoint(c);
            }

            for (DxfText t : block.getTexts()) {
                DxfText c = t.clone();
                DxfStyle style = styleOf(t);
                Layer finalLayer = resolveExplodedLayer(filePath, style, t.getLayer(), insertLayer);
                int finalColor = resolveExplodedColor(style, finalLayer, insertColor);
                applyTransformsToText(c, nextOuterTransforms);
                c.setLayer(finalLayer);
                c.setColore(finalColor);
                data.addText(c);
            }

            for (Face3D f : block.getFaces()) {
                Face3D c = f.clone();
                DxfStyle style = styleOf(f);
                Layer finalLayer = resolveExplodedLayer(filePath, style, f.getLayer(), insertLayer);
                int finalColor = resolveExplodedColor(style, finalLayer, insertColor);
                applyTransformsToFace3D(c, nextOuterTransforms);
                c.setLayer(finalLayer);
                c.setColor(finalColor);
                data.addFace(c);
            }

            for (DxfInsert nested : block.getInserts()) {
                explodeInsertRecursive(data, blockMap, nested, filePath, insertLayer, insertColor,
                        nextOuterTransforms, visitingBlocks);
            }
        } finally {
            visitingBlocks.remove(block.getName());
        }
    }

    private static final class InsertTransform {
        final DxfInsert insert;
        final double bx;
        final double by;
        final double bz;
        final double cosA;
        final double sinA;

        InsertTransform(DxfInsert insert, double bx, double by, double bz) {
            this.insert = insert;
            this.bx = bx;
            this.by = by;
            this.bz = bz;
            double angle = Math.toRadians(insert.getRotation());
            this.cosA = Math.cos(angle);
            this.sinA = Math.sin(angle);
        }
    }

    private static void applyTransformsToPoint(Point3D p, List<InsertTransform> transforms) {
        for (InsertTransform step : transforms) {
            transformPoint(p, step.insert, step.bx, step.by, step.bz, step.cosA, step.sinA);
        }
    }

    private static void applyTransformsToLine(Line l, List<InsertTransform> transforms) {
        for (InsertTransform step : transforms) {
            transformLine(l, step.insert, step.bx, step.by, step.bz, step.cosA, step.sinA);
        }
    }

    private static void applyTransformsToPolyline(Polyline pl, List<InsertTransform> transforms) {
        for (InsertTransform step : transforms) {
            transformPolyline(pl, step.insert, step.bx, step.by, step.bz, step.cosA, step.sinA);
        }
    }

    private static void applyTransformsToPolyline2D(Polyline_2D pl, List<InsertTransform> transforms) {
        for (InsertTransform step : transforms) {
            transformPolyline2D(pl, step.insert, step.bx, step.by, step.bz, step.cosA, step.sinA);
        }
    }

    private static void applyTransformsToCircle(Circle c, List<InsertTransform> transforms) {
        for (InsertTransform step : transforms) {
            transformCircle(c, step.insert, step.bx, step.by, step.bz, step.cosA, step.sinA);
        }
    }

    private static void applyTransformsToArc(Arc a, List<InsertTransform> transforms) {
        for (InsertTransform step : transforms) {
            transformArc(a, step.insert, step.bx, step.by, step.bz, step.cosA, step.sinA);
        }
    }

    private static void applyTransformsToText(DxfText t, List<InsertTransform> transforms) {
        for (InsertTransform step : transforms) {
            transformText(t, step.insert, step.bx, step.by, step.bz, step.cosA, step.sinA);
        }
    }

    private static void applyTransformsToFace3D(Face3D f, List<InsertTransform> transforms) {
        for (InsertTransform step : transforms) {
            transformFace3D(f, step.insert, step.bx, step.by, step.bz, step.cosA, step.sinA);
        }
    }

    private static Layer resolveExplodedLayer(String filePath, DxfStyle style, Layer originalLayer, Layer insertLayer) {
        String layerName = style != null ? sanitizeLayerName(style.getLayerName()) : "0";
        if ("0".equals(layerName)) {
            return insertLayer != null ? insertLayer : resolveLayer(filePath, "0");
        }
        if (originalLayer != null) return originalLayer;
        return resolveLayer(filePath, layerName);
    }

    private static int resolveExplodedColor(DxfStyle style, Layer finalLayer, int insertColor) {
        if (style == null) return safeLayerColor(finalLayer);
        switch (style.getColorMode()) {
            case EXPLICIT:
                return style.getExplicitArgb() != null ? style.getExplicitArgb() : DEFAULT_ARGB;
            case BYBLOCK:
                return insertColor;
            case BYLAYER:
            default:
                return safeLayerColor(finalLayer);
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
        Polyline c = pl.clone();
        c.markGlDirty();
        return c;
    }

    private static Polyline_2D clonePolyline2D(Polyline_2D pl) {
        Polyline_2D c = pl.clone();
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
        return Math.toDegrees(Math.atan2(horiz, vert));
    }
}
