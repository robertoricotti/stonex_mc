package drill_pile.gui;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ProjectReportXlsxWriter {

    public enum ReportType {ROCKDRILL, JETGROUTING, SOLARFARM}

    public static final String[] HEADER_ROCK = new String[]{
            "Machine", "Operator",
            "Hole-ID", "Hole-Descr", "Hole-N", "Hole-E", "Hole-Z", "Hole-End-N", "Hole-End-E", "Hole-End-Z",
            "Hole-Bearing", "Hole-Tilt", "Hole-Depth", "Hole-Length",
            "Start-Time", "End-Time", "Duration",
            "Start-dN", "Start-dE", "Start-dZ",
            "End-dN", "End-dE", "End-dZ",
            "d-Tilt", "d-Bearing", "Rods",
            "AVG-Pen Rate mm/S",
            "AVG-Pen Rate ft/S",
            "State", "Comment"
    };

    public static final String[] HEADER_JET = new String[]{
            "Machine", "Operator",
            "Hole-ID", "Hole-Descr", "Hole-N", "Hole-E", "Hole-Z", "Hole-End-N", "Hole-End-E", "Hole-End-Z",
            "Hole-Bearing", "Hole-Tilt", "Hole-Depth", "Hole-Length",
            "Start-Time", "End-Time", "Duration",
            "Start-dN", "Start-dE", "Start-dZ",
            "End-dN", "End-dE", "End-dZ",
            "d-Tilt", "d-Bearing",
            "State", "Comment"
    };

    public static final String[] HEADER_SOLAR = new String[]{
            "Machine", "Operator",
            "Pile-ID", "Pile-Descr", "Pile-N", "Pile-E", "Pile-Z",
            "Pile-Azimuth", "Pile-Tilt",
            "Start-Time", "End-Time", "Duration",
            "Start-dN", "Start-dE", "Start-dZ",
            "End-dN", "End-dE", "End-dZ", "Embedment",
            "d-Tilt", "d-Azimuth",
            "AVG-Pen Rate mm/S",
            "AVG-Pen Rate ft/S",
            "State", "Comment"
    };

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final File xlsxFile;
    private final String sheetName = "REPORT";
    private final String machineId;
    private final ReportType reportType;

    // Layout righe
    private int headerRowIndex = -1;
    private int dataStartRowIndex = -1;

    public ProjectReportXlsxWriter(
            File projectOutDir,
            String projectName,
            String machineName,
            String machineSerial,
            ReportType reportType
    ) {
        if (projectOutDir == null) throw new IllegalArgumentException("projectOutDir is null");
        if (projectName == null || projectName.trim().isEmpty())
            throw new IllegalArgumentException("projectName is empty");
        if (reportType == null) throw new IllegalArgumentException("reportType is null");

        this.xlsxFile = new File(projectOutDir, projectName + "_REPORT_" + shortType(reportType) + ".xlsx");
        String mn = machineName == null ? "" : machineName.trim();
        String sn = machineSerial == null ? "" : machineSerial.trim();
        this.machineId = (sn.isEmpty()) ? mn : (mn + "_" + sn);
        this.reportType = reportType;
    }

    public File getXlsxFile() {
        return xlsxFile;
    }

    // --------------------- DTO (3 varianti) ---------------------

    public static class RockRow {
        public String operator;

        public String holeId;
        public String holeDescr;

        public Double holeN, holeE, holeZ;
        public Double holeEndN, holeEndE, holeEndZ;

        public Double holeBearing, holeTilt, holeDepth, holeLength;

        public String startTimeIso;
        public String endTimeIso;

        public Double startdN, startdE, startdZ;
        public Double enddN, enddE, enddZ;

        public Double dTilt, dBearing;

        public Integer rods;

        public Double avgPenRateMmS;
        public Double avgPenRateFtS;

        public String state;   // DONE / ABORTED / TODO / REFUSED / RE-OPENED ...
        public String comment;
    }

    public static class JetRow {
        public String operator;

        public String holeId;
        public String holeDescr;

        public Double holeN, holeE, holeZ;
        public Double holeEndN, holeEndE, holeEndZ;

        public Double holeBearing, holeTilt, holeDepth, holeLength;

        public String startTimeIso;
        public String endTimeIso;

        public Double startdN, startdE, startdZ;
        public Double enddN, enddE, enddZ;

        public Double dTilt, dBearing;

        public String state;
        public String comment;
    }

    public static class SolarRow {
        public String operator;

        public String pileId;
        public String pileDescr;

        public Double pileN, pileE, pileZ;

        public Double pileAzimuth;
        public Double pileTilt;

        public String startTimeIso;
        public String endTimeIso;

        public Double startdN, startdE, startdZ;
        public Double enddN, enddE, enddZ;
        public Double embedment;
        public Double dTilt, dAzimuth;

        public Double avgPenRateMmS;
        public Double avgPenRateFtS;

        public String state;
        public String comment;
    }

    // --------------------- Public API ---------------------

    /**
     * Crea file se manca e scrive preamble+header (idempotente).
     */
    public synchronized void initReport(LinkedHashMap<String, String> preambleLines) throws IOException {
        ensureParentDirExists(xlsxFile);

        if (!xlsxFile.exists() || xlsxFile.length() == 0) {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sh = wb.createSheet(sheetName);
                setupStylesAndLayout(wb, sh, preambleLines);
                setDefaultColumnWidthsFixed(sh, activeHeader().length);
                try (FileOutputStream fos = new FileOutputStream(xlsxFile, false)) {
                    wb.write(fos);
                }
            }
        }
    }

    public synchronized void appendRockRow(RockRow r) throws IOException {
        if (reportType != ReportType.ROCKDRILL) {
            throw new IllegalStateException("Writer reportType is " + reportType + " but appendRockRow() was called");
        }
        appendGeneric(activeHeader(), (row, dataStyle, numStyle) -> writeRockRow(row, r, dataStyle, numStyle), r.state);
    }

    public synchronized void appendJetRow(JetRow r) throws IOException {
        if (reportType != ReportType.JETGROUTING) {
            throw new IllegalStateException("Writer reportType is " + reportType + " but appendJetRow() was called");
        }
        appendGeneric(activeHeader(), (row, dataStyle, numStyle) -> writeJetRow(row, r, dataStyle, numStyle), r.state);
    }

    public synchronized void appendSolarRow(SolarRow r) throws IOException {
        if (reportType != ReportType.SOLARFARM) {
            throw new IllegalStateException("Writer reportType is " + reportType + " but appendSolarRow() was called");
        }
        appendGeneric(activeHeader(), (row, dataStyle, numStyle) -> writeSolarRow(row, r, dataStyle, numStyle), r.state);
    }

    // --------------------- internal append core ---------------------

    private interface RowWriter {
        void write(Row row, CellStyle dataStyle, CellStyle numStyle);
    }

    private void appendGeneric(String[] expectedHeader, RowWriter writer, String state) throws IOException {
        ensureParentDirExists(xlsxFile);

        if (!xlsxFile.exists() || xlsxFile.length() == 0) {
            initReport(new LinkedHashMap<>());
        }

        try (FileInputStream fis = new FileInputStream(xlsxFile);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet sh = wb.getSheet(sheetName);
            if (sh == null) sh = wb.createSheet(sheetName);

            resolveHeaderAndDataStart(sh);
            verifyHeaderMatches(sh, expectedHeader);

            int rowIdx = findAppendRowIndex(sh);
            Row row = sh.getRow(rowIdx);
            if (row != null) sh.removeRow(row);
            row = sh.createRow(rowIdx);

            String st = (state == null) ? "" : state.trim().toUpperCase(Locale.US);
            boolean done = "DONE".equals(st);
            boolean aborted = "ABORTED".equals(st) || "ABORT".equals(st);
            boolean reopened = "RE-OPENED".equals(st) || "REOPENED".equals(st);
            boolean refused = "REFUSED".equals(st);

            CellStyle dataStyle;
            CellStyle numStyle;

            if (done) {
                dataStyle = makeRowFillStyle(wb, IndexedColors.LIGHT_GREEN.getIndex(), false);
                numStyle = makeRowFillStyle(wb, IndexedColors.LIGHT_GREEN.getIndex(), true);
            } else if (aborted) {
                dataStyle = makeRowFillStyle(wb, IndexedColors.RED.getIndex(), false);
                numStyle = makeRowFillStyle(wb, IndexedColors.RED.getIndex(), true);
            } else if (reopened) {
                dataStyle = makeRowFillStyle(wb, IndexedColors.LIGHT_TURQUOISE.getIndex(), false);
                numStyle = makeRowFillStyle(wb, IndexedColors.LIGHT_TURQUOISE.getIndex(), true);
            } else if (refused) {
                dataStyle = makeRowFillStyle(wb, IndexedColors.GREY_25_PERCENT.getIndex(), false);
                numStyle = makeRowFillStyle(wb, IndexedColors.GREY_25_PERCENT.getIndex(), true);
            } else {
                dataStyle = makeDataStyle(wb);
                numStyle = makeNumberStyle(wb);
            }

            writer.write(row, dataStyle, numStyle);

            sh.createFreezePane(0, dataStartRowIndex);

            try (FileOutputStream fos = new FileOutputStream(xlsxFile, false)) {
                wb.write(fos);
            }
        }
    }

    // --------------------- writers (3 metodi) ---------------------

    private void writeRockRow(Row row, RockRow r, CellStyle dataStyle, CellStyle numStyle) {
        int c = 0;

        setText(row, c++, machineId, dataStyle);
        setText(row, c++, nv(r.operator), dataStyle);

        setText(row, c++, nv(r.holeId), dataStyle);
        setText(row, c++, nv(r.holeDescr), dataStyle);

        setNum(row, c++, r.holeN, numStyle);
        setNum(row, c++, r.holeE, numStyle);
        setNum(row, c++, r.holeZ, numStyle);

        setNum(row, c++, r.holeEndN, numStyle);
        setNum(row, c++, r.holeEndE, numStyle);
        setNum(row, c++, r.holeEndZ, numStyle);

        setNum(row, c++, r.holeBearing, numStyle);
        setNum(row, c++, r.holeTilt, numStyle);
        setNum(row, c++, r.holeDepth, numStyle);
        setNum(row, c++, r.holeLength, numStyle);

        setText(row, c++, nv(r.startTimeIso), dataStyle);
        setText(row, c++, nv(r.endTimeIso), dataStyle);
        setText(row, c++, durationHHmmssSSS(r.startTimeIso, r.endTimeIso), dataStyle);

        setNum(row, c++, r.startdN, numStyle);
        setNum(row, c++, r.startdE, numStyle);
        setNum(row, c++, r.startdZ, numStyle);

        setNum(row, c++, r.enddN, numStyle);
        setNum(row, c++, r.enddE, numStyle);
        setNum(row, c++, r.enddZ, numStyle);

        setNum(row, c++, r.dTilt, numStyle);
        setNum(row, c++, r.dBearing, numStyle);

        setNumInt(row, c++, r.rods, numStyle);

        setNum(row, c++, r.avgPenRateMmS, numStyle);
        setNum(row, c++, r.avgPenRateFtS, numStyle);

        setText(row, c++, nv(r.state), dataStyle);
        setText(row, c++, nv(r.comment), dataStyle);

        // sicurezza (facoltativa)
        // if (c != HEADER_ROCK.length) throw new IllegalStateException("ROCK columns mismatch: " + c);
    }

    private void writeJetRow(Row row, JetRow r, CellStyle dataStyle, CellStyle numStyle) {
        int c = 0;

        setText(row, c++, machineId, dataStyle);
        setText(row, c++, nv(r.operator), dataStyle);

        setText(row, c++, nv(r.holeId), dataStyle);
        setText(row, c++, nv(r.holeDescr), dataStyle);

        setNum(row, c++, r.holeN, numStyle);
        setNum(row, c++, r.holeE, numStyle);
        setNum(row, c++, r.holeZ, numStyle);

        setNum(row, c++, r.holeEndN, numStyle);
        setNum(row, c++, r.holeEndE, numStyle);
        setNum(row, c++, r.holeEndZ, numStyle);

        setNum(row, c++, r.holeBearing, numStyle);
        setNum(row, c++, r.holeTilt, numStyle);
        setNum(row, c++, r.holeDepth, numStyle);
        setNum(row, c++, r.holeLength, numStyle);

        setText(row, c++, nv(r.startTimeIso), dataStyle);
        setText(row, c++, nv(r.endTimeIso), dataStyle);
        setText(row, c++, durationHHmmssSSS(r.startTimeIso, r.endTimeIso), dataStyle);

        setNum(row, c++, r.startdN, numStyle);
        setNum(row, c++, r.startdE, numStyle);
        setNum(row, c++, r.startdZ, numStyle);

        setNum(row, c++, r.enddN, numStyle);
        setNum(row, c++, r.enddE, numStyle);
        setNum(row, c++, r.enddZ, numStyle);

        setNum(row, c++, r.dTilt, numStyle);
        setNum(row, c++, r.dBearing, numStyle);

        setText(row, c++, nv(r.state), dataStyle);
        setText(row, c++, nv(r.comment), dataStyle);

        // if (c != HEADER_JET.length) throw new IllegalStateException("JET columns mismatch: " + c);
    }

    private void writeSolarRow(Row row, SolarRow r, CellStyle dataStyle, CellStyle numStyle) {
        int c = 0;

        setText(row, c++, machineId, dataStyle);
        setText(row, c++, nv(r.operator), dataStyle);

        setText(row, c++, nv(r.pileId), dataStyle);
        setText(row, c++, nv(r.pileDescr), dataStyle);

        setNum(row, c++, r.pileN, numStyle);
        setNum(row, c++, r.pileE, numStyle);
        setNum(row, c++, r.pileZ, numStyle);

        setNum(row, c++, r.pileAzimuth, numStyle);
        setNum(row, c++, r.pileTilt, numStyle);

        setText(row, c++, nv(r.startTimeIso), dataStyle);
        setText(row, c++, nv(r.endTimeIso), dataStyle);
        setText(row, c++, durationHHmmssSSS(r.startTimeIso, r.endTimeIso), dataStyle);

        setNum(row, c++, r.startdN, numStyle);
        setNum(row, c++, r.startdE, numStyle);
        setNum(row, c++, r.startdZ, numStyle);

        setNum(row, c++, r.enddN, numStyle);
        setNum(row, c++, r.enddE, numStyle);
        setNum(row, c++, r.enddZ, numStyle);
        setNum(row, c++, r.embedment, numStyle);
        setNum(row, c++, r.dTilt, numStyle);
        setNum(row, c++, r.dAzimuth, numStyle);

        setNum(row, c++, r.avgPenRateMmS, numStyle);
        setNum(row, c++, r.avgPenRateFtS, numStyle);

        setText(row, c++, nv(r.state), dataStyle);
        setText(row, c++, nv(r.comment), dataStyle);

        // if (c != HEADER_SOLAR.length) throw new IllegalStateException("SOLAR columns mismatch: " + c);
    }

    // --------------------- layout + header ---------------------

    private String[] activeHeader() {
        switch (reportType) {
            case ROCKDRILL:
                return HEADER_ROCK;
            case JETGROUTING:
                return HEADER_JET;
            case SOLARFARM:
                return HEADER_SOLAR;
            default:
                return HEADER_JET;
        }
    }

    private void setupStylesAndLayout(Workbook wb, Sheet sh, LinkedHashMap<String, String> preambleLines) {
        int r = 0;

        if (preambleLines != null && !preambleLines.isEmpty()) {
            CellStyle keyStyle = makePreambleKeyStyle(wb);
            CellStyle valStyle = makePreambleValStyle(wb);

            for (Map.Entry<String, String> e : preambleLines.entrySet()) {
                Row row = sh.createRow(r++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(nv(e.getKey()));
                c0.setCellStyle(keyStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(nv(e.getValue()));
                c1.setCellStyle(valStyle);
            }
        }

        r++; // blank line

        headerRowIndex = r;
        dataStartRowIndex = headerRowIndex + 1;

        Row headerRow = sh.createRow(headerRowIndex);
        CellStyle hStyle = makeHeaderStyle(wb);

        String[] header = activeHeader();
        for (int i = 0; i < header.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header[i]);
            cell.setCellStyle(hStyle);
        }

        sh.createFreezePane(0, dataStartRowIndex);
    }

    private void resolveHeaderAndDataStart(Sheet sh) {
        if (headerRowIndex >= 0 && dataStartRowIndex >= 0) return;

        // Cerca riga header: col0=="Machine" col1=="Operator"
        int last = Math.max(sh.getLastRowNum(), 80);
        for (int r = 0; r <= last; r++) {
            Row row = sh.getRow(r);
            if (row == null) continue;

            String c0 = getCellString(row.getCell(0)).trim();
            String c1 = getCellString(row.getCell(1)).trim();
            if ("Machine".equalsIgnoreCase(c0) && "Operator".equalsIgnoreCase(c1)) {
                headerRowIndex = r;
                dataStartRowIndex = r + 1;
                return;
            }
        }

        headerRowIndex = -1;
        dataStartRowIndex = firstPossibleDataRow(sh);
    }

    private void verifyHeaderMatches(Sheet sh, String[] expected) {
        if (headerRowIndex < 0) return; // fallback: non verifico

        Row hr = sh.getRow(headerRowIndex);
        if (hr == null) return;

        // confronto "soft": stessa lunghezza e stessi titoli
        for (int i = 0; i < expected.length; i++) {
            String got = getCellString(hr.getCell(i)).trim();
            String exp = expected[i].trim();
            if (!exp.equalsIgnoreCase(got)) {
                throw new IllegalStateException("Header mismatch at col " + i + ": expected '" + exp + "' got '" + got + "'");
            }
        }
    }

    private int firstPossibleDataRow(Sheet sh) {
        int last = sh.getLastRowNum();
        for (int r = last; r >= 0; r--) {
            Row row = sh.getRow(r);
            if (row == null) continue;
            if (row.getPhysicalNumberOfCells() > 0) return r + 1;
        }
        return 0;
    }

    private int findAppendRowIndex(Sheet sh) {
        resolveHeaderAndDataStart(sh);

        int start = Math.max(0, dataStartRowIndex);
        int last = sh.getLastRowNum();

        int lastNonEmpty = start - 1;
        for (int r = Math.max(last, start); r >= start; r--) {
            Row row = sh.getRow(r);
            if (row == null) continue;
            if (row.getPhysicalNumberOfCells() > 0) {
                lastNonEmpty = r;
                break;
            }
        }

        int idx = Math.max(start, lastNonEmpty + 1);

        while (true) {
            Row row = sh.getRow(idx);
            if (row == null) break;
            if (row.getPhysicalNumberOfCells() == 0) break;
            idx++;
        }
        return idx;
    }

    // --------------------- misc helpers ---------------------

    private static String durationHHmmssSSS(String startIso, String endIso) {
        if (startIso == null || startIso.isEmpty() || endIso == null || endIso.isEmpty()) return "";

        String s0 = startIso.trim().replace(' ', 'T');
        String s1 = endIso.trim().replace(' ', 'T');

        try {
            LocalDateTime a = LocalDateTime.parse(s0, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime b = LocalDateTime.parse(s1, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            long ms = Duration.between(a, b).toMillis();
            if (ms < 0) ms = 0;

            long hours = ms / 3_600_000;
            ms %= 3_600_000;
            long minutes = ms / 60_000;
            ms %= 60_000;
            long seconds = ms / 1_000;
            long millis = ms % 1_000;

            return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
        } catch (Exception e) {
            return "";
        }
    }

    private static void setText(Row row, int col, String val, CellStyle style) {
        Cell c = row.createCell(col, CellType.STRING);
        c.setCellValue(val == null ? "" : val);
        if (style != null) c.setCellStyle(style);
    }

    private static void setNum(Row row, int col, Double v, CellStyle numStyle) {
        Cell c = row.createCell(col);
        if (v == null || v.isNaN() || v.isInfinite()) {
            c.setBlank();
        } else {
            c.setCellValue(v);
            if (numStyle != null) c.setCellStyle(numStyle);
        }
    }

    private static void setNumInt(Row row, int col, Integer v, CellStyle numStyle) {
        Cell c = row.createCell(col);
        if (v == null) {
            c.setBlank();
        } else {
            c.setCellValue(v);
            if (numStyle != null) c.setCellStyle(numStyle);
        }
    }

    private static String nv(String s) {
        return s == null ? "" : s;
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        try {
            if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
            if (cell.getCellType() == CellType.NUMERIC)
                return String.valueOf(cell.getNumericCellValue());
            if (cell.getCellType() == CellType.BOOLEAN)
                return String.valueOf(cell.getBooleanCellValue());
            return "";
        } catch (Exception ignore) {
            return "";
        }
    }

    // ---- styles ----

    private static CellStyle makeHeaderStyle(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);

        CellStyle st = wb.createCellStyle();
        st.setFont(f);
        st.setAlignment(HorizontalAlignment.CENTER);
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        st.setBorderBottom(BorderStyle.THIN);
        st.setBorderTop(BorderStyle.THIN);
        st.setBorderLeft(BorderStyle.THIN);
        st.setBorderRight(BorderStyle.THIN);
        return st;
    }

    private static CellStyle makeDataStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        st.setVerticalAlignment(VerticalAlignment.CENTER);
        st.setBorderBottom(BorderStyle.THIN);
        st.setBorderTop(BorderStyle.THIN);
        st.setBorderLeft(BorderStyle.THIN);
        st.setBorderRight(BorderStyle.THIN);
        return st;
    }

    private static CellStyle makeNumberStyle(Workbook wb) {
        CellStyle st = makeDataStyle(wb);
        DataFormat fmt = wb.createDataFormat();
        st.setDataFormat(fmt.getFormat("0.000"));
        return st;
    }

    private static CellStyle makePreambleKeyStyle(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        CellStyle st = wb.createCellStyle();
        st.setFont(f);
        return st;
    }

    private static CellStyle makePreambleValStyle(Workbook wb) {
        return wb.createCellStyle();
    }

    private static CellStyle makeRowFillStyle(Workbook wb, short fillColorIndex, boolean isNumber) {
        CellStyle st = makeDataStyle(wb);

        if (isNumber) {
            DataFormat fmt = wb.createDataFormat();
            st.setDataFormat(fmt.getFormat("0.000"));
        }

        st.setFillForegroundColor(fillColorIndex);
        st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return st;
    }

    private static void ensureParentDirExists(File f) throws IOException {
        File dir = f.getParentFile();
        if (dir != null && !dir.exists()) {
            boolean ok = dir.mkdirs();
            if (!ok) throw new IOException("Unable to create directory: " + dir.getAbsolutePath());
        }
    }

    private static void setDefaultColumnWidthsFixed(Sheet sh, int columnCount) {
        // FIX: usare i < columnCount (non <=)
        for (int i = 0; i < columnCount; i++) {
            sh.setColumnWidth(i, 24 * 256);
        }
    }

    private static String shortType(ReportType t) {
        switch (t) {
            case ROCKDRILL:
                return "ROCK";
            case JETGROUTING:
                return "JET";
            case SOLARFARM:
                return "SOLAR";
            default:
                return "REPORT";
        }
    }
}