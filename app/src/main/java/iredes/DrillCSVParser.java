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
                    ReadProjectService.parserStatus = "Reading Points..."+"\n"+out.size();

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
        // Altrimenti usiamo fallback posizionale per vari formati CSV (lungo e point-only).
        boolean useHeader = headerMap != null && !headerMap.isEmpty();

        Point3D_Drill p = new Point3D_Drill();

        if (useHeader) {
            String row = getByKeys(t, headerMap, "row");
            String hole = getByKeys(t, headerMap, "hole", "id", "name");

            Double startE = getDoubleByKeys(t, headerMap, "start point easting", "start easting", "e head", "easting head");
            Double startN = getDoubleByKeys(t, headerMap, "start point northing", "start northing", "n head", "northing head");
            Double startZ = getDoubleByKeys(t, headerMap, "start point elev", "start elev", "start elevation", "z head", "elev head");

            Double endE = getDoubleByKeys(t, headerMap, "end point easting", "end easting", "e end", "easting end");
            Double endN = getDoubleByKeys(t, headerMap, "end point northing", "end northing", "n end", "northing end");
            Double endZ = getDoubleByKeys(t, headerMap, "end point elev", "end elev", "end elevation", "z end", "elev end");

            Double depth = getDoubleByKeys(t, headerMap, "depth");
            Double length = getDoubleByKeys(t, headerMap, "length");
            Double deltaDist = getDoubleByKeys(t, headerMap, "delta distance", "delta", "delta dist");

            p.setRowId(row);
            p.setId((row != null && hole != null) ? (row + "-" + hole) : (hole != null ? hole : row));

            // Coordinate testa/fine
            Double[] head = applyXyzSwap(startE, startN, startZ, xyz, conv);
            Double[] end  = applyXyzSwap(endE, endN, endZ, xyz, conv);

            if (head != null) {
                p.setHeadX(head[0]); p.setHeadY(head[1]); p.setHeadZ(head[2]);
            }
            if (end != null) {
                p.setEndX(end[0]); p.setEndY(end[1]); p.setEndZ(end[2]);
            }

            // Se manca End nel file con header, ma abbiamo almeno Head, possiamo lasciare End null
            // (i derivati verranno null). Se invece vuoi target-point: copia End=Head qui.

            // Depth/Length dal file (convertiti) se presenti -> override
            if (depth != null)  p.setDepth(depth * conv);
            if (length != null) p.setLength(length * conv);
            if (p.getLength() == null && deltaDist != null) p.setLength(deltaDist * conv);

            // Calcola derivati geometrici (depth/length se mancano)
            Double keepDepth  = p.getDepth();
            Double keepLength = p.getLength();

            p.recomputeDerived();

            if (keepDepth != null)  p.setDepth(keepDepth);
            if (keepLength != null) p.setLength(keepLength);

            // Bearing e Tilt SEMPRE calcolati da coordinate (non dal file)
            p.setHeadingDeg(computeBearingDeg(p));
            p.setTilt(computeTiltDeg(p));

        } else {
            // -----------------------------
            // Fallback POSIZIONALE
            // -----------------------------

            // Caso 1) CSV point-only (senza header):
            // t: 0 ID, 1 E, 2 N, 3 Z, 4 Description (opzionale)
            // Esempio: 16496,323903.5179,1155714.3900,479.4549,NE
            if (t.size() >= 4 && t.size() < 11) {

                String id = safeTrim(t.get(0));
                Double e = (t.size() >= 2) ? parseDoubleSafe(t.get(1)) : null;
                Double n = (t.size() >= 3) ? parseDoubleSafe(t.get(2)) : null;
                Double z = (t.size() >= 4) ? parseDoubleSafe(t.get(3)) : null;
                String desc = (t.size() >= 5) ? safeTrim(t.get(4)) : null;

                if (id == null || e == null || n == null || z == null) return null;

                p.setRowId(null);
                p.setId(id);
                p.setDescription(desc);

                Double[] head = applyXyzSwap(e, n, z, xyz, conv);
                if (head != null) {
                    p.setHeadX(head[0]); p.setHeadY(head[1]); p.setHeadZ(head[2]);

                    // ✅ Target-point: End = Head
                    p.setEndX(head[0]); p.setEndY(head[1]); p.setEndZ(head[2]);
                }

                p.setDiameter(null);

            } else {
                // Caso 2) formato lungo (il tuo CSV "completo"):
                // t: 0 Row, 1 Hole, 2 StartE, 3 StartN, 4 StartZ, 5 EndE, 6 EndN, 7 EndZ,
                //    8 Bearing, 9 Inclination, 10 DeltaDistance, 11 extra, 12 extra
                if (t.size() < 11) return null;

                String row = safeTrim(t.get(0));
                String hole = safeTrim(t.get(1));

                Double startE = parseDoubleSafe(t.get(2));
                Double startN = parseDoubleSafe(t.get(3));
                Double startZ = parseDoubleSafe(t.get(4));

                Double endE = parseDoubleSafe(t.get(5));
                Double endN = parseDoubleSafe(t.get(6));
                Double endZ = parseDoubleSafe(t.get(7));

                Double deltaDist = parseDoubleSafe(t.get(10));

                p.setRowId(row);
                p.setId((row != null && hole != null) ? (row + "-" + hole) : (hole != null ? hole : row));

                Double[] head = applyXyzSwap(startE, startN, startZ, xyz, conv);
                Double[] end  = applyXyzSwap(endE, endN, endZ, xyz, conv);

                if (head != null) { p.setHeadX(head[0]); p.setHeadY(head[1]); p.setHeadZ(head[2]); }
                if (end  != null) { p.setEndX(end[0]);  p.setEndY(end[1]);  p.setEndZ(end[2]);  }

                // Se vuoi valorizzare length anche se end manca:
                if (deltaDist != null) p.setLength(deltaDist * conv);

                p.setDiameter(null);
            }

            // In fallback NON usiamo bearing/incl dal file: li calcoliamo sempre da coordinate
            // (se le coordinate ci sono).
            p.recomputeDerived();
            p.setHeadingDeg(computeBearingDeg(p));
            p.setTilt(computeTiltDeg(p));
        }

        // Final consistency pass (anche per il ramo header)
        p.recomputeDerived();
        p.setHeadingDeg(computeBearingDeg(p));
        p.setTilt(computeTiltDeg(p));

        // Scarta righe completamente vuote
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

        // Rimuove BOM UTF-8 eventuale all'inizio del primo campo
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF') {
            s = s.substring(1);
        }

        try {
            // Caso già supportato: decimale con virgola
            if (s.indexOf(',') >= 0 && s.indexOf('.') < 0) {
                return Double.parseDouble(s.replace(',', '.'));
            }

            // Nuovo caso: più punti => verosimilmente separatori delle migliaia
            long dotCount = s.chars().filter(ch -> ch == '.').count();
            if (dotCount > 1 && s.indexOf(',') < 0) {
                return Double.parseDouble(s.replace(".", ""));
            }

            // Caso già funzionante: decimale con punto o intero
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

    // -------------------------------------------------
// Bearing + Tilt calcolati da coordinate
// -------------------------------------------------

    /** Bearing topografico: 0=N, 90=E, 180=S, 270=W (X=Est, Y=Nord) */
    private static Double computeBearingDeg(Point3D_Drill p) {
        if (p.getHeadX() == null || p.getHeadY() == null || p.getEndX() == null || p.getEndY() == null) return null;

        double dx = p.getEndX() - p.getHeadX(); // Est
        double dy = p.getEndY() - p.getHeadY(); // Nord

        double bearing = Math.toDegrees(Math.atan2(dx, dy));
        if (bearing < 0) bearing += 360.0;
        return bearing;
    }

    /** Tilt rispetto alla verticale: 0=verticale, 90=orizzontale */
    private static Double computeTiltDeg(Point3D_Drill p) {
        if (p.getHeadX() == null || p.getHeadY() == null || p.getHeadZ() == null ||
                p.getEndX() == null  || p.getEndY() == null  || p.getEndZ() == null) return null;

        double dx = p.getEndX() - p.getHeadX();
        double dy = p.getEndY() - p.getHeadY();
        double dz = p.getEndZ() - p.getHeadZ();

        double horiz = Math.sqrt(dx * dx + dy * dy);
        double vert  = Math.abs(dz);

        if (horiz == 0 && vert == 0) return 0.0;
        return Math.toDegrees(Math.atan2(horiz, vert));
    }
    // -------------------------------------------------
// COSTRUZIONE FILARI DA POINT-ONLY CSV
// -------------------------------------------------
    public static void assignSyntheticRowsFromAlignment(List<Point3D_Drill> pts,
                                                        Point3D_Drill a,
                                                        Point3D_Drill b) {

        if (pts == null || pts.isEmpty()) return;
        if (a == null || b == null) return;
        if (a.getHeadX() == null || a.getHeadY() == null) return;
        if (b.getHeadX() == null || b.getHeadY() == null) return;

        double vx = b.getHeadX() - a.getHeadX();
        double vy = b.getHeadY() - a.getHeadY();
        double len = Math.hypot(vx, vy);
        if (len < 1e-6) return;

        double ux = vx / len;
        double uy = vy / len;

        double nx = -uy;
        double ny = ux;

        double offsetStep = 0.10;   // distanza tra filari (regolabile)
        double maxGap = 8.0;        // distanza max tra punti consecutivi

        // 1) Raggruppa per parallela ad AB
        Map<Long, List<Point3D_Drill>> groups = new LinkedHashMap<>();

        for (Point3D_Drill p : pts) {
            if (p == null) continue;
            if (p.getHeadX() == null || p.getHeadY() == null) continue;

            double dx = p.getHeadX() - a.getHeadX();
            double dy = p.getHeadY() - a.getHeadY();

            double offset = dx * nx + dy * ny;
            long bucket = Math.round(offset / offsetStep);

            groups.computeIfAbsent(bucket, k -> new ArrayList<>()).add(p);
        }

        int rowCounter = 0;

        // 2) Dentro ogni gruppo → spezza per gap e assegna rowId
        for (List<Point3D_Drill> group : groups.values()) {

            if (group.size() < 2) continue;

            // ordina lungo AB
            group.sort((p1, p2) -> {
                double pr1 = projection(p1, a, ux, uy);
                double pr2 = projection(p2, a, ux, uy);
                return Double.compare(pr1, pr2);
            });

            List<Point3D_Drill> segment = new ArrayList<>();
            segment.add(group.get(0));

            for (int i = 1; i < group.size(); i++) {
                Point3D_Drill prev = group.get(i - 1);
                Point3D_Drill curr = group.get(i);

                double d = distance(prev, curr);

                if (d <= maxGap) {
                    segment.add(curr);
                } else {
                    assignRow(segment, rowCounter++);
                    segment.clear();
                    segment.add(curr);
                }
            }

            assignRow(segment, rowCounter++);
        }
    }

    private static void assignRow(List<Point3D_Drill> segment, int rowId) {
        if (segment == null || segment.size() < 2) return;

        String id = "ROW_" + rowId;

        for (Point3D_Drill p : segment) {
            p.setRowId(id);
        }
    }

    private static double projection(Point3D_Drill p, Point3D_Drill a, double ux, double uy) {
        double dx = p.getHeadX() - a.getHeadX();
        double dy = p.getHeadY() - a.getHeadY();
        return dx * ux + dy * uy;
    }

    private static double distance(Point3D_Drill p1, Point3D_Drill p2) {
        double dx = p2.getHeadX() - p1.getHeadX();
        double dy = p2.getHeadY() - p1.getHeadY();
        return Math.hypot(dx, dy);
    }

}