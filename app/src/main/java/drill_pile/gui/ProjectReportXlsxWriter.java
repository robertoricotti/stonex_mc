package drill_pile.gui;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ProjectReportXlsxWriter {

    public static final String[] HEADER = new String[] {
            "Machine",
            "Hole-ID", "Hole-N", "Hole-E", "Hole-Z","Hole-End-N", "Hole-End-E", "Hole-End-Z",
            "Hole-Bearing", "Hole-Tilt", "Hole-Depth", "Hole-Length",
            "Start-Time", "End-Time", "Duration",
            "Start-dN", "Start-dE", "Start-dZ",
            "End-dN", "End-dE", "End-dZ",
            "d-Tilt", "d-Bearing",
            "AVG-Penetration Rate mm/S",
            "State"
    };

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final File xlsxFile;
    private final String sheetName = "REPORT";
    private final String machineId;

    // Layout righe:
    // 0..(preamble-1)  -> preamble
    // +1 riga vuota
    // headerRowIndex
    // dataStartRowIndex = headerRowIndex + 1
    private int headerRowIndex = -1;
    private int dataStartRowIndex = -1;

    public ProjectReportXlsxWriter(File projectOutDir, String projectName, String machineName, String machineSerial) {
        if (projectOutDir == null) throw new IllegalArgumentException("projectOutDir is null");
        if (projectName == null || projectName.trim().isEmpty()) throw new IllegalArgumentException("projectName is empty");

        this.xlsxFile = new File(projectOutDir, projectName + "_REPORT.xlsx");
        String mn = machineName == null ? "" : machineName.trim();
        String sn = machineSerial == null ? "" : machineSerial.trim();
        this.machineId = (sn.isEmpty()) ? mn : (mn + "_" + sn);
    }

    public File getXlsxFile() {
        return xlsxFile;
    }

    public static class HoleSummaryRow {
        public String holeId;

        public Double holeN, holeE, holeZ;
        public Double holeEndN, holeEndE, holeEndZ;
        public Double holeBearing, holeTilt, holeDepth, holeLength;

        public String startTimeIso;
        public String endTimeIso;

        public Double startdN, startdE, startdZ;
        public Double enddN, enddE, enddZ;

        public Double dTilt, dBearing;

        public Double avgPenetrationRate;

        public String state; // DONE / ABORTED / TODO
    }

    /** Crea file se manca e scrive preamble+header (idempotente). */
    public synchronized void initReport(LinkedHashMap<String, String> preambleLines) throws IOException {
        ensureParentDirExists(xlsxFile);

        if (!xlsxFile.exists() || xlsxFile.length() == 0) {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sh = wb.createSheet(sheetName);
                setupStylesAndLayout(wb, sh, preambleLines);
                setDefaultColumnWidths(sh);
                try (FileOutputStream fos = new FileOutputStream(xlsxFile, false)) {
                    wb.write(fos);
                }
            }
        } else {
            // esiste già: non tocco niente
        }
    }

    /** Appende una riga (a fine foro). */
    public synchronized void appendHoleRow(HoleSummaryRow r) throws IOException {
        if (r == null) throw new IllegalArgumentException("row is null");
        ensureParentDirExists(xlsxFile);

        // se chiamato prima di init, inizializzo con preamble vuoto
        if (!xlsxFile.exists() || xlsxFile.length() == 0) {
            initReport(new LinkedHashMap<>());
        }

        try (FileInputStream fis = new FileInputStream(xlsxFile);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet sh = wb.getSheet(sheetName);
            if (sh == null) sh = wb.createSheet(sheetName);

            // Individua header/dataStart se non presenti (compatibile con file esistente)
            resolveHeaderAndDataStart(sh);

            int rowIdx = findAppendRowIndex(sh);
            Row row = sh.getRow(rowIdx);
            if (row == null) row = sh.createRow(rowIdx);   // crea solo se manca
            else sh.removeRow(row);                         // (opzionale) se vuoi essere super-safe
            row = sh.createRow(rowIdx);

            String st = (r.state == null) ? "" : r.state.trim().toUpperCase(Locale.US);
            boolean done = "DONE".equals(st);
            boolean aborted = "ABORTED".equals(st) || "ABORT".equals(st);
            boolean reopened = "RE-OPENED".equals(st) || "REOPENED".equals(st);

// stili base
            CellStyle dataStyle;
            CellStyle numStyle;

            if (done) {
                dataStyle = makeRowFillStyle(wb, IndexedColors.LIGHT_GREEN.getIndex(), false);
                numStyle  = makeRowFillStyle(wb, IndexedColors.LIGHT_GREEN.getIndex(), true);
            } else if (aborted) {
                dataStyle = makeRowFillStyle(wb, IndexedColors.RED.getIndex(), false);
                numStyle  = makeRowFillStyle(wb, IndexedColors.RED.getIndex(), true);
            } else if (reopened) {
                dataStyle = makeRowFillStyle(wb, IndexedColors.LIGHT_TURQUOISE.getIndex(), false); // ciano
                numStyle  = makeRowFillStyle(wb, IndexedColors.LIGHT_TURQUOISE.getIndex(), true);
            } else {
                dataStyle = makeDataStyle(wb);
                numStyle  = makeNumberStyle(wb);
            }


            int c = 0;

            // Machine
            setText(row, c++, machineId, dataStyle);

            // Hole-ID
            setText(row, c++, nv(r.holeId), dataStyle);

            // numeri
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

            // tempi
            setText(row, c++, nv(r.startTimeIso), dataStyle);
            setText(row, c++, nv(r.endTimeIso), dataStyle);
            setText(row, c++, durationHHmmssSSS(r.startTimeIso, r.endTimeIso), dataStyle);

            // delta start
            setNum(row, c++, r.startdN, numStyle);
            setNum(row, c++, r.startdE, numStyle);
            setNum(row, c++, r.startdZ, numStyle);

            // delta end
            setNum(row, c++, r.enddN, numStyle);
            setNum(row, c++, r.enddE, numStyle);
            setNum(row, c++, r.enddZ, numStyle);

            // delta angoli
            setNum(row, c++, r.dTilt, numStyle);
            setNum(row, c++, r.dBearing, numStyle);

            // avg rate
            setNum(row, c++, r.avgPenetrationRate, numStyle);

            // state
            setText(row, c++, nv(r.state), dataStyle);

            // Freeze header + auto-size (auto-size costicchia, ma ok per fine foro)
            sh.createFreezePane(0, dataStartRowIndex);


            try (FileOutputStream fos = new FileOutputStream(xlsxFile, false)) {
                wb.write(fos);
            }
        }
    }

    // ---------------- helpers ----------------

    private void setupStylesAndLayout(Workbook wb, Sheet sh, LinkedHashMap<String, String> preambleLines) {
        int r = 0;

        // Preamble (in celle, niente #)
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

        // riga vuota
        r++;

        // header
        headerRowIndex = r;
        dataStartRowIndex = headerRowIndex + 1;

        Row headerRow = sh.createRow(headerRowIndex);
        CellStyle hStyle = makeHeaderStyle(wb);

        for (int i = 0; i < HEADER.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADER[i]);
            cell.setCellStyle(hStyle);
        }

        sh.createFreezePane(0, dataStartRowIndex);
    }

    private void resolveHeaderAndDataStart(Sheet sh) {
        if (headerRowIndex >= 0 && dataStartRowIndex >= 0) return;

        int last = sh.getLastRowNum();

        for (int r = 0; r <= Math.max(last, 80); r++) {
            Row row = sh.getRow(r);
            if (row == null) continue;

            String c0 = getCellString(row.getCell(0)).trim();
            String c1 = getCellString(row.getCell(1)).trim();

            // nuovo formato
            if ("Machine".equalsIgnoreCase(c0) && "Hole-ID".equalsIgnoreCase(c1)) {
                headerRowIndex = r;
                dataStartRowIndex = r + 1;
                return;
            }

            // vecchio formato (senza Machine)
            if ("Hole-ID".equalsIgnoreCase(c0)) {
                headerRowIndex = r;
                dataStartRowIndex = r + 1;
                return;
            }
        }

        // fallback "intelligente": prova a mettere dataStart dopo l'ultima riga non vuota
        headerRowIndex = -1;
        dataStartRowIndex = firstPossibleDataRow(sh);
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

        // sicurezza: se per caso esiste già, vai avanti
        while (true) {
            Row row = sh.getRow(idx);
            if (row == null) break;
            if (row.getPhysicalNumberOfCells() == 0) break;
            idx++;
        }
        return idx;
    }


    private int findNextEmptyRow(Sheet sh) {
        resolveHeaderAndDataStart(sh);

        int r = Math.max(dataStartRowIndex, sh.getLastRowNum() + 1);

        // Se lastRowNum punta a righe vuote, avanza finché trovi una riga davvero vuota
        while (true) {
            Row row = sh.getRow(r);
            if (row == null) return r;
            if (row.getPhysicalNumberOfCells() == 0) return r;
            r++;
        }
    }

    private static String durationHHmmssSSS(String startIso, String endIso) {
        if (startIso == null || startIso.isEmpty() || endIso == null || endIso.isEmpty()) return "";

        // accetta sia "yyyy-MM-ddTHH:mm:ss" che "yyyy-MM-dd HH:mm:ss"
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
            // se stringhe malformate, non bloccare la riga del report
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

    private static String nv(String s) { return s == null ? "" : s; }

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        try {
            if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
            if (cell.getCellType() == CellType.NUMERIC) return String.valueOf(cell.getNumericCellValue());
            if (cell.getCellType() == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
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

        // formato numero se serve
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
    private static void setDefaultColumnWidths(Sheet sh) {
        // width in "1/256 of a character"
        // valori indicativi, poi li rifiniamo
        sh.setColumnWidth(0, 20 * 256); // Machine
        sh.setColumnWidth(1, 14 * 256); // Hole-ID

        // Coordinate
        sh.setColumnWidth(2, 14 * 256); // Hole-N
        sh.setColumnWidth(3, 14 * 256); // Hole-E
        sh.setColumnWidth(4, 12 * 256); // Hole-Z
        sh.setColumnWidth(5, 14 * 256); // Hole End-N
        sh.setColumnWidth(6, 14 * 256); // Hole End-E
        sh.setColumnWidth(7, 12 * 256); // Hole End-Z

        // Angoli/quote
        sh.setColumnWidth(8, 14 * 256); // Bearing
        sh.setColumnWidth(9, 12 * 256); // Tilt
        sh.setColumnWidth(10, 12 * 256); // Depth
        sh.setColumnWidth(11, 12 * 256); // Length

        // Time
        sh.setColumnWidth(12,  24 * 256); // Start-Time
        sh.setColumnWidth(13, 24 * 256); // End-Time
        sh.setColumnWidth(14, 14 * 256); // Duration

        // Deltas
        for (int i = 15; i <= 22; i++) {
            sh.setColumnWidth(i, 12 * 256);
        }

        sh.setColumnWidth(23, 30 * 256); // AVG rate
        sh.setColumnWidth(24, 12 * 256); // State
    }

}
