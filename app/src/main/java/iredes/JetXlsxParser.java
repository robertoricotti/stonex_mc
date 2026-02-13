package iredes;

import android.util.Log;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import services.ReadProjectService;

public class JetXlsxParser {

    public static List<Point3D_Drill> parseJetXlsx(String filePath, int xyz, double conversionFactor) {
        List<Point3D_Drill> out = new ArrayList<>();

        Workbook wb = null;
        InputStream is = null;

        try {
            File inputFile = new File(filePath);
            is = new FileInputStream(inputFile);
            wb = WorkbookFactory.create(is);

            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return out;

            DataFormatter fmt = new DataFormatter(Locale.US);

            int firstRow = sheet.getFirstRowNum();
            Row headerRow = sheet.getRow(firstRow);
            if (headerRow == null) return out;

            Map<String, Integer> col = buildHeaderMap(headerRow, fmt);

            int lastRow = sheet.getLastRowNum();
            Log.i("JET_XLSX", "Righe trovate: " + (lastRow - firstRow));

            for (int r = firstRow + 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String id = getString(row, col, "PtNr", fmt);
                if (isBlank(id)) continue; // riga vuota

                Point3D_Drill p = new Point3D_Drill();
                p.setId(id);

                // ✅ richiesto: rowId mancante => stringa vuota
                p.setRowId("");

                // --- Coordinate HEAD/END ---
                Double headX = getDouble(row, col, "E Head", fmt);
                Double headY = getDouble(row, col, "N Head", fmt);
                Double headZ = getDouble(row, col, "Z Head", fmt);

                Double endX  = getDouble(row, col, "E End", fmt);
                Double endY  = getDouble(row, col, "N End", fmt);
                Double endZ  = getDouble(row, col, "Z End", fmt);

                // ✅ swap XY come nel tuo IrdParser/LandXML
                Double[] headXY = swapXY(headX, headY, xyz);
                Double[] endXY  = swapXY(endX, endY, xyz);

                headX = headXY[0];
                headY = headXY[1];
                endX  = endXY[0];
                endY  = endXY[1];

                // ✅ conversionFactor sulle coordinate (se non null)
                headX = mul(headX, conversionFactor);
                headY = mul(headY, conversionFactor);
                headZ = mul(headZ, conversionFactor);

                endX  = mul(endX, conversionFactor);
                endY  = mul(endY, conversionFactor);
                endZ  = mul(endZ, conversionFactor);

                // ✅ regola richiesta: se manca una coordinata di END, copia HEAD (tipico)
                // e viceversa per Z (come da tua richiesta).
                // (se entrambe null, restano null)
                if (headX == null && endX != null) headX = endX;
                if (endX  == null && headX != null) endX  = headX;

                if (headY == null && endY != null) headY = endY;
                if (endY  == null && headY != null) endY  = headY;

                if (headZ == null && endZ != null) headZ = endZ;
                if (endZ  == null && headZ != null) endZ  = headZ;

                p.setHeadX(headX);
                p.setHeadY(headY);
                p.setHeadZ(headZ);

                p.setEndX(endX);
                p.setEndY(endY);
                p.setEndZ(endZ);

                // --- Campi DRL (String, vuoto => null) ---
                p.setPr_1(getString(row, col, "pr_1", fmt));
                p.setDrlStart_1(getString(row, col, "drlStart_1", fmt));
                p.setDrlStop_1(getString(row, col, "drlStop_1", fmt));

                p.setPr_2(getString(row, col, "pr_2", fmt));
                p.setDrlStart_2(getString(row, col, "drlStart_2", fmt));
                p.setDrlStop_2(getString(row, col, "drlStop_2", fmt));

                p.setPr_3(getString(row, col, "pr_3", fmt));
                p.setDrlStart_3(getString(row, col, "drlStart_3", fmt));
                p.setDrlStop_3(getString(row, col, "drlStop_3", fmt));

                p.setPr_4(getString(row, col, "pr_4", fmt));
                p.setDrlStart_4(getString(row, col, "drlStart_4", fmt));
                p.setDrlStop_4(getString(row, col, "drlStop_4", fmt));

                // --- Campi JET (String, vuoto => null) ---
                p.setPr_j_1(getString(row, col, "pr_j_1", fmt));
                p.setJetStart_1(getString(row, col, "jetStart_1", fmt));
                p.setJetStop_1(getString(row, col, "jetStop_1", fmt));

                p.setPr_j_2(getString(row, col, "pr_j_2", fmt));
                p.setJetStart_2(getString(row, col, "jetStart_2", fmt));
                p.setJetStop_2(getString(row, col, "jetStop_2", fmt));

                p.setPr_j_3(getString(row, col, "pr_j_3", fmt));
                p.setJetStart_3(getString(row, col, "jetStart_3", fmt));
                p.setJetStop_3(getString(row, col, "jetStop_3", fmt));

                p.setPr_j_4(getString(row, col, "pr_j_4", fmt));
                p.setJetStart_4(getString(row, col, "jetStart_4", fmt));
                p.setJetStop_4(getString(row, col, "jetStop_4", fmt));

                // --- Inclinazioni se ti servono (nel file ci sono Incli_X / Incli_Y) ---
                // Se vuoi salvarle dentro "tilt" con una tua formula, dimmelo.
                // Per ora tilt come negli altri: calcolato da endpoints.
                p.setTilt(computeTiltFromEndpoints(p));

                // derived
                p.recomputeDerived();

                out.add(p);
                ReadProjectService.parserStatus = "Reading Points..."+"\n"+out.size();
            }

        } catch (Exception e) {
            Log.e("JET_XLSX", "Errore parsing .xlsx", e);
        } finally {
            try { if (wb != null) wb.close(); } catch (Exception ignore) {}
            try { if (is != null) is.close(); } catch (Exception ignore) {}
        }

        ReadProjectService.isFinishedPOINT = true;
        return out;
    }

    // ----------------- Helpers -----------------

    private static Map<String, Integer> buildHeaderMap(Row headerRow, DataFormatter fmt) {
        Map<String, Integer> map = new HashMap<>();
        short last = headerRow.getLastCellNum();
        for (int c = 0; c < last; c++) {
            Cell cell = headerRow.getCell(c);
            String name = cell != null ? fmt.formatCellValue(cell) : null;
            if (name == null) continue;
            name = name.trim();
            if (!name.isEmpty()) map.put(name, c);
        }
        return map;
    }

    private static String getString(Row row, Map<String, Integer> col, String header, DataFormatter fmt) {
        Integer idx = col.get(header);
        if (idx == null) return null;
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        String s = fmt.formatCellValue(cell);
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static Double getDouble(Row row, Map<String, Integer> col, String header, DataFormatter fmt) {
        String s = getString(row, col, header, fmt);
        return parseDoubleSafe(s);
    }

    private static Double parseDoubleSafe(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        s = s.replace(',', '.');
        try {
            return Double.parseDouble(s);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static Double mul(Double v, double k) {
        return (v == null) ? null : (v * k);
    }

    private static Double[] swapXY(Double x, Double y, int xyz) {
        if (xyz == 1) {
            // come nel tuo IrdParser: xx=y, yy=x
            return new Double[]{ y, x };
        }
        return new Double[]{ x, y };
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
