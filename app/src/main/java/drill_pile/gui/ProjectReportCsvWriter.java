package drill_pile.gui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import iredes.DateTimeIsoCompat;

public class ProjectReportCsvWriter {
    private final String machineId;
    // Header richiesto (ordine fisso)
    public static final String[] HEADER = new String[] {
            "Machine",
            "Hole-ID", "Hole-N", "Hole-E", "Hole-Z",
            "Hole-Bearing", "Hole-Tilt", "Hole-Depth", "Hole-Length",
            "Start-Time", "End-Time", "Duration",
            "Start-dN", "Start-dE", "Start-dZ",
            "End-dN", "End-dE", "End-dZ",
            "d-Tilt", "d-Bearing",
            "AVG-Penetration Rate",
            "State"
    };

    // Il tuo GNSS produce ISO_LOCAL_DATE_TIME (es: 2026-02-08T12:34:56.789)
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final File reportFile;

    public ProjectReportCsvWriter(
            File projectOutDir,
            String projectName,
            String machineName,
            String machineSerial
    ) {
        if (projectOutDir == null) throw new IllegalArgumentException("projectOutDir is null");
        if (projectName == null || projectName.trim().isEmpty())
            throw new IllegalArgumentException("projectName is empty");

        this.reportFile = new File(projectOutDir, projectName + "_REPORT.csv");

        // come vuoi tu il formato
        this.machineId = machineName + "_" + machineSerial;
    }


    public File getReportFile() {
        return reportFile;
    }

    /**
     * Crea il file se non esiste, scrive preamble (info progetto) e header.
     * Richiamalo quando apri/parsi il progetto (una sola volta).
     *
     * @param preambleLines es: {"Company":"...", "Machine":"..."} (ordine preservato se LinkedHashMap)
     */
    public void initReport(LinkedHashMap<String, String> preambleLines) throws IOException {
        ensureParentDirExists(reportFile);

        boolean newFile = !reportFile.exists() || reportFile.length() == 0;

        if (newFile) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(reportFile, true), StandardCharsets.UTF_8))) {

                // BOM opzionale per Excel (se hai problemi di encoding, abilitalo)
                // bw.write('\uFEFF');

                writePreamble(bw, preambleLines);
                writeHeader(bw);
            }
        }
    }

    /**
     * Aggiunge una riga foro al report generale.
     * Richiamalo a fine foro (DONE o ABORTED).
     */
    public synchronized void appendHoleRow(HoleSummaryRow r) throws IOException {
        if (r == null) throw new IllegalArgumentException("row is null");
        ensureParentDirExists(reportFile);

        boolean empty = !reportFile.exists() || reportFile.length() == 0;
        if (empty) {
            // Se qualcuno chiama append prima di init, inizializzo senza preamble
            initReport(new LinkedHashMap<>());
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(reportFile, true), StandardCharsets.UTF_8))) {

            bw.newLine();
            bw.write(toCsvLine(r));
        }
    }

    // ---------------- Row model ----------------

    public static class HoleSummaryRow {
        public String holeId;

        public Double holeN;
        public Double holeE;
        public Double holeZ;

        public Double holeBearing;
        public Double holeTilt;
        public Double holeDepth;
        public Double holeLength;

        public String startTimeIso; // ISO_LOCAL_DATE_TIME
        public String endTimeIso;   // ISO_LOCAL_DATE_TIME

        public Double startdN;
        public Double startdE;
        public Double startdZ;

        public Double enddN;
        public Double enddE;
        public Double enddZ;

        public Double dTilt;
        public Double dBearing;

        public Double avgPenetrationRate; // unità che decidi tu (m/s, m/min, ecc.)

        public String state; // TODO/DONE/ABORTED (qui tipicamente DONE/ABORTED)
    }

    // ---------------- Duration helpers ----------------

    /**
     * Calcola durata da due stringhe ISO_LOCAL_DATE_TIME (le tue date_time_iso).
     * Ritorna stringa "HH:mm:ss.SSS".
     */
    public static String durationHHmmssSSS(String startIso, String endIso) {
        if (startIso == null || startIso.isEmpty() || endIso == null || endIso.isEmpty()) return "";

        try {
            LocalDateTime a = DateTimeIsoCompat.parse(startIso);
            LocalDateTime b = DateTimeIsoCompat.parse(endIso);
            if (a == null || b == null) return "";

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
    // ---------------- CSV writing ----------------

    private static void writePreamble(BufferedWriter bw, LinkedHashMap<String, String> preambleLines) throws IOException {
        if (preambleLines == null || preambleLines.isEmpty()) return;

        // Esempio formato:
        // # Company,Stonex
        // # Machine,XYZ
        // (riga vuota)
        for (Map.Entry<String, String> e : preambleLines.entrySet()) {
            bw.write("# ");
            bw.write(escapeCsv(e.getKey()));
            bw.write(",");
            bw.write(escapeCsv(e.getValue()));
            bw.newLine();
        }
        bw.newLine();
    }

    private static void writeHeader(BufferedWriter bw) throws IOException {
        bw.write(String.join(",", HEADER));
        bw.newLine();
    }

    private String toCsvLine(HoleSummaryRow r) {
        String duration = durationHHmmssSSS(r.startTimeIso, r.endTimeIso);

        String[] cols = new String[] {
                machineId,                 // 👈 NUOVA PRIMA COLONNA

                r.holeId,

                fmt(r.holeN), fmt(r.holeE), fmt(r.holeZ),

                fmt(r.holeBearing), fmt(r.holeTilt), fmt(r.holeDepth), fmt(r.holeLength),

                nvl(r.startTimeIso), nvl(r.endTimeIso), duration,

                fmt(r.startdN), fmt(r.startdE), fmt(r.startdZ),

                fmt(r.enddN), fmt(r.enddE), fmt(r.enddZ),

                fmt(r.dTilt), fmt(r.dBearing),

                fmt(r.avgPenetrationRate),

                nvl(r.state)
        };

        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsv(cols[i]));
        }
        return sb.toString();
    }


    private static String fmt(Double v) {
        if (v == null || v.isNaN() || v.isInfinite()) return "";
        // formato con punto decimale, adattabile
        return String.format(Locale.US, "%.3f", v);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    /**
     * CSV escaping: se contiene virgole, doppi apici o newline -> racchiudi tra doppi apici
     * e raddoppia i doppi apici interni.
     */
    private static String escapeCsv(String s) {
        if (s == null) return "";
        boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!mustQuote) return s;

        String q = s.replace("\"", "\"\"");
        return "\"" + q + "\"";
    }

    private static void ensureParentDirExists(File f) throws IOException {
        File dir = f.getParentFile();
        if (dir != null && !dir.exists()) {
            boolean ok = dir.mkdirs();
            if (!ok) throw new IOException("Unable to create directory: " + dir.getAbsolutePath());
        }
    }
}