package iredes;

import java.io.*;
import java.util.*;

import services.ReadProjectService;

/**
 * Parser robusto per CSV di punti trivellazione -> Point3D_Drill.
 */
public class DrillCSVParser {

    public static List<Point3D_Drill> parse(File csvFile, int skipLines, int xyz, double conversionFactor) throws IOException {
        List<Point3D_Drill> out = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            // 1) skip righe iniziali
            for (int i = 0; i < skipLines; i++) {
                if (br.readLine() == null) return out;
            }

            // 2) leggi prima riga non-vuota
            String first = nextNonEmptyLine(br);
            if (first == null) return out;

            // Determina delimitatore migliore (tra , ; \t)
            char delim = detectDelimiter(first);

            List<String> firstTokens = splitCsvLine(first, delim);

            // Heuristica: se sembra header (contiene lettere) lo usiamo come header,
            // altrimenti è già una riga dati.
            boolean firstIsHeader = looksLikeHeader(firstTokens);

            Map<String, Integer> headerMap = new HashMap<>();
            String line;

            if (firstIsHeader) {
                headerMap = buildHeaderMap(firstTokens);
                line = nextNonEmptyLine(br);
            } else {
                line = first; // già dati
            }

            while (line != null) {
                List<String> t = splitCsvLine(line, delim);
                if (t.size() >= 2) {
                    Point3D_Drill p = parseRow(t, headerMap, xyz, conversionFactor);
                    if (p != null) out.add(p);
                }
                line = nextNonEmptyLine(br);
            }
            ReadProjectService.isFinishedPOINT = true;
        }

        return out;
    }

    // -----------------------------
    // Parsing singola riga
    // -----------------------------
    private static Point3D_Drill parseRow(List<String> t, Map<String, Integer> headerMap, int xyz, double conv) {
        // Se abbiamo header affidabile, proviamo mapping per nome.
        // Altrimenti usiamo fallback posizionale per formati tipo il CSV che hai allegato.
        boolean useHeader = headerMap != null && !headerMap.isEmpty();

        Point3D_Drill p = new Point3D_Drill();

        if (useHeader) {
            String row = getByKeys(t, headerMap, "row");
            String hole = getByKeys(t, headerMap, "hole", "id", "name");

            Double startE = getDoubleByKeys(t, headerMap, "start point easting", "start easting");
            Double startN = getDoubleByKeys(t, headerMap, "start point northing", "start northing");
            Double startZ = getDoubleByKeys(t, headerMap, "start point elev", "start elev", "start elevation");

            Double endE = getDoubleByKeys(t, headerMap, "end point easting", "end easting");
            Double endN = getDoubleByKeys(t, headerMap, "end point northing", "end northing");
            Double endZ = getDoubleByKeys(t, headerMap, "end point elev", "end elev", "end elevation");

            Double depth = getDoubleByKeys(t, headerMap, "depth");
            Double length = getDoubleByKeys(t, headerMap, "length");

            Double bearing = getDoubleByKeys(t, headerMap, "bearing", "heading", "azimuth");
            Double incl = getDoubleByKeys(t, headerMap, "inclination", "tilt", "slope");

            Double deltaDist = getDoubleByKeys(t, headerMap, "delta distance", "delta", "delta dist");

            p.setRowId(row);
            p.setId((row != null && hole != null) ? (row + "-" + hole) : (hole != null ? hole : row));

// coordinate testa/fine
            Double[] head = applyXyzSwap(startE, startN, startZ, xyz, conv);
            Double[] end  = applyXyzSwap(endE, endN, endZ, xyz, conv);

            if (head != null) { p.setHeadX(head[0]); p.setHeadY(head[1]); p.setHeadZ(head[2]); }
            if (end  != null) { p.setEndX(end[0]);  p.setEndY(end[1]);  p.setEndZ(end[2]);  }

// Tilt (qui: inclination)
            p.setTilt(incl);

// Se Bearing presente, usalo come heading (altrimenti calcolato da coordinate)
            if (bearing != null) p.setHeadingDeg(bearing);

// Depth/Length dal file (convertiti) se presenti
            if (depth != null)  p.setDepth(depth * conv);
            if (length != null) p.setLength(length * conv);

// se Length manca ma c’è Delta Distance, usalo come fallback
            if (p.getLength() == null && deltaDist != null) p.setLength(deltaDist * conv);

// ricalcolo derivati: SOLO se non ho già depth/length/heading dal file
            Double keepHeading = p.getHeadingDeg();
            Double keepDepth = p.getDepth();
            Double keepLength = p.getLength();

            p.recomputeDerived();

            if (keepHeading != null) p.setHeadingDeg(keepHeading);
            if (keepDepth != null)   p.setDepth(keepDepth);
            if (keepLength != null)  p.setLength(keepLength);

        } else {
            // Fallback POSIZIONALE (il tuo CSV allegato)
            // t: 0 Row, 1 Hole, 2 StartE, 3 StartN, 4 StartZ, 5 EndE, 6 EndN, 7 EndZ, 8 Bearing, 9 Inclination, 10 DeltaDistance, 11 extra, 12 extra
            if (t.size() < 11) return null;

            String row = safeTrim(t.get(0));
            String hole = safeTrim(t.get(1));

            Double startE = parseDoubleSafe(t.get(2));
            Double startN = parseDoubleSafe(t.get(3));
            Double startZ = parseDoubleSafe(t.get(4));

            Double endE = parseDoubleSafe(t.get(5));
            Double endN = parseDoubleSafe(t.get(6));
            Double endZ = parseDoubleSafe(t.get(7));

            Double bearing = parseDoubleSafe(t.get(8));
            Double incl = parseDoubleSafe(t.get(9));
            Double deltaDist = parseDoubleSafe(t.get(10));

            p.setRowId(row);
            // id univoco consigliato:
            p.setId((row != null && hole != null) ? (row + "-" + hole) : (hole != null ? hole : row));

            Double[] head = applyXyzSwap(startE, startN, startZ, xyz, conv);
            Double[] end  = applyXyzSwap(endE, endN, endZ, xyz, conv);

            if (head != null) { p.setHeadX(head[0]); p.setHeadY(head[1]); p.setHeadZ(head[2]); }
            if (end != null)  { p.setEndX(end[0]);  p.setEndY(end[1]);  p.setEndZ(end[2]);  }

            // heading e tilt dal file
            if (bearing != null) p.setHeadingDeg(bearing);
            if (incl != null) p.setTilt(incl);

            // se vuoi valorizzare length anche se end manca:
            if (deltaDist != null) p.setLength(deltaDist * conv);

            // diameter: qui non è chiaro quali siano le ultime colonne; le lasciamo null
            p.setDiameter(null);
        }

        // se hai abbastanza coordinate, ricalcola depth/length/heading "geometrico"
        // (non sovrascrive headingDeg se già settato? qui sì: quindi ricalcoliamo SOLO se headingDeg è null)
        Double existingHeading = p.getHeadingDeg();
        p.recomputeDerived();
        if (existingHeading != null) {
            p.setHeadingDeg(existingHeading); // preserva bearing del file se lo vuoi come "heading"
        }

        // scarta righe completamente vuote
        if (p.getId() == null && p.getHeadX() == null && p.getEndX() == null) return null;
        return p;
    }

    // -----------------------------
    // Utils: header mapping
    // -----------------------------
    private static Map<String, Integer> buildHeaderMap(List<String> headerTokens) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerTokens.size(); i++) {
            String k = normalizeKey(headerTokens.get(i));
            if (k != null && !k.isEmpty()) map.put(k, i);
        }
        return map;
    }

    private static String getByKeys(List<String> t, Map<String, Integer> map, String... keys) {
        for (String k : keys) {
            Integer idx = findKeyIndex(map, k);
            if (idx != null && idx >= 0 && idx < t.size()) {
                String v = safeTrim(t.get(idx));
                if (v != null && !v.isEmpty()) return v;
            }
        }
        return null;
    }

    private static Double getDoubleByKeys(List<String> t, Map<String, Integer> map, String... keys) {
        String v = getByKeys(t, map, keys);
        return parseDoubleSafe(v);
    }

    private static Integer findKeyIndex(Map<String, Integer> map, String keyContains) {
        String needle = normalizeKey(keyContains);
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (e.getKey().contains(needle)) return e.getValue();
        }
        return null;
    }

    private static String normalizeKey(String s) {
        if (s == null) return null;
        s = s.trim().toLowerCase(Locale.ROOT);

        // rimuove unità tra parentesi: "(us-ft)", "(degree)" ecc.
        s = s.replaceAll("\\([^\\)]*\\)", " ");

        // uniforma separatori
        s = s.replace("_", " ").replace("-", " ").replace(".", " ");

        // spazi multipli -> singolo
        s = s.replaceAll("\\s+", " ").trim();

        return s;
    }


    private static boolean looksLikeHeader(List<String> tokens) {
        String joined = normalizeKey(String.join(" ", tokens));
        if (joined.contains("row") && joined.contains("hole")) return true;

        int letters = 0;
        int numeric = 0;
        for (String tok : tokens) {
            String t = safeTrim(tok);
            if (t == null) continue;
            if (t.matches(".*[a-zA-Z].*")) letters++;
            if (parseDoubleSafe(t) != null) numeric++;
        }
        return letters >= 2 && letters >= numeric;
    }

    // -----------------------------
    // Utils: CSV
    // -----------------------------
    private static String nextNonEmptyLine(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            return line;
        }
        return null;
    }

    private static char detectDelimiter(String line) {
        int commas = countChar(line, ',');
        int semis = countChar(line, ';');
        int tabs = countChar(line, '\t');
        if (tabs >= semis && tabs >= commas) return '\t';
        if (semis >= commas) return ';';
        return ',';
    }

    private static int countChar(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) n++;
        return n;
    }

    private static List<String> splitCsvLine(String line, char delim) {
        List<String> out = new ArrayList<>();
        if (line == null) return out;

        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // doppie virgolette escape ("")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == delim && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out;
    }

    // -----------------------------
    // Utils: numeri + xyz swap
    // -----------------------------
    private static String safeTrim(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
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

    private static Double[] applyXyzSwap(Double x, Double y, Double z, int xyz, double conv) {
        if (x == null || y == null || z == null) return null;
        double xx = (xyz == 1) ? y : x;
        double yy = (xyz == 1) ? x : y;
        return new Double[] { xx * conv, yy * conv, z * conv };
    }
}