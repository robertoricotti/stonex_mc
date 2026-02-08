package drill_pile.gui;



import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectStateCsvStore {

    public enum HoleState {
        TODO, DONE, ABORTED;

        public static HoleState fromString(String s) {
            if (s == null) return TODO;
            try {
                return HoleState.valueOf(s.trim().toUpperCase(Locale.US));
            } catch (Exception ignore) {
                return TODO;
            }
        }
    }

    // Header stabile e minimale (puoi aggiungere colonne in futuro senza rompere la logica)
    public static final String[] HEADER = new String[] {
            "Hole-ID",
            "State",
            "Start-Time",  // ISO_LOCAL_DATE_TIME (date_time_iso)
            "End-Time",    // ISO_LOCAL_DATE_TIME (date_time_iso)
            "Note",
            "HoleReportFile" // es: HOLES/<HoleID>.csv (facoltativo)
    };

    public static class HoleStateEntry {
        public String holeId;
        public HoleState state = HoleState.TODO;
        public String startTimeIso = "";
        public String endTimeIso = "";
        public String note = "";
        public String holeReportFile = "";
    }

    private final File stateFile;

    // in-memory cache: Hole-ID -> entry
    private final Map<String, HoleStateEntry> cache = new ConcurrentHashMap<>();

    public ProjectStateCsvStore(File projectOutDir, String projectName) {
        if (projectOutDir == null) throw new IllegalArgumentException("projectOutDir is null");
        if (projectName == null || projectName.trim().isEmpty()) throw new IllegalArgumentException("projectName is empty");
        this.stateFile = new File(projectOutDir, projectName + "_STATE.csv");
    }

    public File getStateFile() {
        return stateFile;
    }

    /**
     * Crea il file STATE se non esiste e carica il contenuto in memoria.
     * Chiamalo quando apri/parsi il progetto.
     */
    public synchronized void initAndLoad() throws IOException {
        ensureParentDirExists(stateFile);

        if (!stateFile.exists() || stateFile.length() == 0) {
            // crea con header
            writeAllToDisk(Collections.emptyList());
            cache.clear();
            return;
        }

        loadFromDisk();
    }

    /**
     * Ritorna lo stato attuale (da cache). Se non presente => TODO.
     */
    public HoleState getState(String holeId) {
        if (holeId == null || holeId.trim().isEmpty()) return HoleState.TODO;
        HoleStateEntry e = cache.get(holeId);
        return (e == null) ? HoleState.TODO : e.state;
    }

    /**
     * Upsert: imposta stato e (opzionalmente) start/end/note/reportfile.
     * Poi salva su disco in modo sicuro (rewrite completo + rename atomico).
     *
     * Chiamalo quando:
     * - inizi foro (per registrare Start-Time)
     * - finisci DONE/ABORTED (per registrare End-Time + stato finale)
     */
    public synchronized void upsertAndSave(
            String holeId,
            HoleState state,
            String startTimeIso,
            String endTimeIso,
            String note,
            String holeReportFile
    ) throws IOException {

        if (holeId == null || holeId.trim().isEmpty()) {
            throw new IllegalArgumentException("holeId is empty");
        }

        HoleStateEntry e = cache.get(holeId);
        if (e == null) {
            e = new HoleStateEntry();
            e.holeId = holeId;
            cache.put(holeId, e);
        }

        if (state != null) e.state = state;
        if (startTimeIso != null) e.startTimeIso = startTimeIso;
        if (endTimeIso != null) e.endTimeIso = endTimeIso;
        if (note != null) e.note = note;
        if (holeReportFile != null) e.holeReportFile = holeReportFile;

        // Scrittura completa (ordinata per ID per file stabile/leggibile)
        List<HoleStateEntry> all = new ArrayList<>(cache.values());
        all.sort(Comparator.comparing(a -> a.holeId, String.CASE_INSENSITIVE_ORDER));
        writeAllToDisk(all);
    }

    /**
     * Utility: ritorna una snapshot della mappa (per debug o UI).
     */
    public Map<String, HoleStateEntry> snapshot() {
        return new HashMap<>(cache);
    }

    // ---------------- Internals: load/save ----------------

    private void loadFromDisk() throws IOException {
        cache.clear();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(stateFile), StandardCharsets.UTF_8))) {

            String line;
            boolean headerRead = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // ignora commenti stile "#"
                if (line.startsWith("#")) continue;

                if (!headerRead) {
                    // prima riga non-commento: header
                    headerRead = true;
                    continue;
                }

                String[] cols = splitCsvLine(line);
                if (cols.length < 2) continue;

                String holeId = unquote(cols[0]).trim();
                if (holeId.isEmpty()) continue;

                HoleStateEntry e = new HoleStateEntry();
                e.holeId = holeId;
                e.state = HoleState.fromString(unquote(cols[1]));

                e.startTimeIso = (cols.length > 2) ? unquote(cols[2]) : "";
                e.endTimeIso   = (cols.length > 3) ? unquote(cols[3]) : "";
                e.note         = (cols.length > 4) ? unquote(cols[4]) : "";
                e.holeReportFile = (cols.length > 5) ? unquote(cols[5]) : "";

                cache.put(holeId, e);
            }
        }
    }

    private void writeAllToDisk(List<HoleStateEntry> entries) throws IOException {
        ensureParentDirExists(stateFile);

        File tmp = new File(stateFile.getParentFile(), stateFile.getName() + ".tmp");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tmp, false), StandardCharsets.UTF_8))) {

            // header
            bw.write(String.join(",", HEADER));
            bw.newLine();

            for (HoleStateEntry e : entries) {
                bw.write(toCsvLine(e));
                bw.newLine();
            }

            bw.flush();
        }

        // replace atomico (best effort su Android/Linux)
        if (stateFile.exists() && !stateFile.delete()) {
            // se non riesce a cancellare, prova comunque rename (dipende dal filesystem)
        }
        if (!tmp.renameTo(stateFile)) {
            throw new IOException("Unable to rename tmp to state file: " + tmp.getAbsolutePath());
        }
    }

    private static String toCsvLine(HoleStateEntry e) {
        String[] cols = new String[] {
                e.holeId,
                e.state != null ? e.state.name() : HoleState.TODO.name(),
                nvl(e.startTimeIso),
                nvl(e.endTimeIso),
                nvl(e.note),
                nvl(e.holeReportFile)
        };

        StringBuilder sb = new StringBuilder(128);
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsv(cols[i]));
        }
        return sb.toString();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    // CSV escaping (compatibile Excel)
    private static String escapeCsv(String s) {
        if (s == null) return "";
        boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!mustQuote) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static String unquote(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            String inner = s.substring(1, s.length() - 1);
            return inner.replace("\"\"", "\"");
        }
        return s;
    }

    /**
     * Split CSV line (semplice ma gestisce doppi apici).
     * Sufficiente per i nostri file (no multiline).
     */
    private static String[] splitCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    // escaped quote
                    cur.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (c == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static void ensureParentDirExists(File f) throws IOException {
        File dir = f.getParentFile();
        if (dir != null && !dir.exists()) {
            boolean ok = dir.mkdirs();
            if (!ok) throw new IOException("Unable to create directory: " + dir.getAbsolutePath());
        }
    }
}

