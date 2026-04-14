package packexcalib.gnss;

import java.util.HashMap;
import java.util.Map;

public class Can318PositionDecoder {

    // ==================================================================
    // OUTPUT COMPLETO
    // ==================================================================
    public static class Output {
        public double latDeg = Double.NaN;
        public double lonDeg = Double.NaN;
        public double alt = Double.NaN;
        public float cq = Float.NaN;

        public double relLat = Double.NaN;   // m
        public double relLon = Double.NaN;   // m
        public double relAlt = Double.NaN;   // m

        public double headingDeg = Double.NaN; // 0–360°

        public int fixType = -1;        // da 0006
        public int satUsed = -1;        // da somma 000A
        public int satTracked = -1;     // da somma 000A

        public double correctionAge = Double.NaN; // rimane NaN finché non avrai 0008

        public float dopVertical = Float.NaN;   // da 0009
        public float dopHorizontal = Float.NaN;
        public float dopGeometric = Float.NaN;
    }

    private final Output out = new Output();

    public Output getOutput() {
        return out;
    }

    // ==================================================================
    // BUFFER PER FAST PACKET 0003
    // ==================================================================
    private final Map<Integer, byte[]> frames = new HashMap<>();
    private int baseCounter = -1;
    private int payloadLen = -1;

    // per 000A (sat per costellazione)
    private final int[] satUsedConst = new int[16];
    private final int[] satTrackedConst = new int[16];

    // ==================================================================
    // CHIAMATA UNICA PER OGNI FRAME CON CAN-ID 0x318
    // ==================================================================
    public Output feed(byte[] msg, int dlc) {

        if (dlc == 8) {
            // fast-packet → 0003
            handleFastPacket(msg);
        } else if (dlc == 7) {
            // messaggi corti → 0006 / 0009 / 000A
            handleShort(msg);
        }

        // ritorno l'Output quando ho appena chiuso il blocco 0003
        if (!Double.isNaN(out.latDeg) && dlc == 8 && ((msg[0] & 0x1F) == 4)) {
            return out;
        }
        return null;
    }

    // ==================================================================
    // FAST PACKET 0003 (3D Position + Relative)
    // ==================================================================
    private void handleFastPacket(byte[] msg) {
        int counter = msg[0] & 0xFF;

        // inizio nuova sessione: index = x0
        if ((counter & 0x1F) == 0) {
            frames.clear();
            baseCounter = counter;
            payloadLen = msg[1] & 0xFF; // = 34
        }

        frames.put(counter, msg);
        if (frames.size() < 5) return;

        byte[] payload = new byte[payloadLen];
        int pos = 0;
        for (int i = 0; i < 5; i++) {
            int expected = baseCounter + i;
            byte[] f = frames.get(expected);
            if (f == null) return;

            if (i == 0) {
                for (int b = 2; b < 8 && pos < payloadLen; b++)
                    payload[pos++] = f[b];
            } else {
                for (int b = 1; b < 8 && pos < payloadLen; b++)
                    payload[pos++] = f[b];
            }
        }

        if (payloadLen != 34) return;
        if (!(payload[0] == 0x00 && payload[1] == 0x03)) return;

        byte[] d = new byte[30];
        System.arraycopy(payload, 2, d, 0, 30);

        decode3D(d);
    }

    private void decode3D(byte[] d) {

        // ---- Lat/Lon/Alt/CQ (formato Leica, 40bit etc) ----
        long latRaw = u40BE(d, 4);
        long lonRaw = u40BE(d, 9);
        int altRaw = u24BE(d, 14);
        int cqRaw = u16BE(d, 17);

        double latRad = latRaw * 1e-11 - (Math.PI / 2.0);
        double lonRad = lonRaw * 1e-11 - Math.PI;

        out.latDeg = Math.toDegrees(latRad);
        out.lonDeg = Math.toDegrees(lonRad);
        out.alt = altRaw * 1e-3 - 8000.0;
        out.cq = (float) (cqRaw * 1e-3);

        // ---- Relative ----
        long relLatRaw = u24BE(d, 19);
        long relLonRaw = u24BE(d, 22);
        long relAltRaw = u24BE(d, 25);

        double relLatRad = relLatRaw * 1e-10 - 5e-5;
        double relLonRad = relLonRaw * 1e-10 - 5e-5;
        double relAlt = relAltRaw * 1e-4 - 800.0;

        double R = 6378137.0;
        out.relLat = relLatRad * R;
        out.relLon = relLonRad * Math.cos(Math.toRadians(out.latDeg)) * R;
        out.relAlt = relAlt;

        // ---- Heading dal baseline ----
        double headingRad = Math.atan2(out.relLon, out.relLat);
        double headingDeg = Math.toDegrees(headingRad);
        if (headingDeg < 0) headingDeg += 360.0;
        out.headingDeg = headingDeg;


    }

    // ==================================================================
    // MESSAGGI CORTI (7 BYTE) → 0006 / 0009 / 000A /0008
    // ==================================================================
    private void handleShort(byte[] msg) {
        if (msg.length != 7) return;

        int internalId = ((msg[0] & 0xFF) << 8) | (msg[1] & 0xFF);
        int off = 2;

        switch (internalId) {

            case 0x0006:    // Heartbeat → Fix Type
                decode0006(msg, off);
                break;

            case 0x0008:    // Essential GNSS → Correction Age
                decode0008(msg, off);
                break;

            case 0x0009:    // GNSS DOP
                decode0009(msg, off);
                break;

            case 0x000A:    // SAT count per costellazione
                decode000A(msg, off);
                break;
        }
    }


    private void decode0006(byte[] m, int off) {
        if (m.length < off + 1) return;
        int sol = m[off] & 0xFF;
        out.fixType = (sol & 0x0F); // bits 3..0 → primary solution type
        // bits 7..4 = secondary (se ti serve più avanti)
    }

    private void decode0009(byte[] m, int off) {
        if (m.length < off + 4) return;
        int dopV = m[off + 1] & 0xFF;
        int dopH = m[off + 2] & 0xFF;
        int dopG = m[off + 3] & 0xFF;

        // Resolution da documento: 0.1
        out.dopVertical = dopV * 0.1f;
        out.dopHorizontal = dopH * 0.1f;
        out.dopGeometric = dopG * 0.1f;
    }

    private void decode0008(byte[] m, int off) {
        int age = m[off] & 0xFF;   // 1 byte
        out.correctionAge = age * 0.1;   // resolution: 0.1s
    }

    private void decode000A(byte[] m, int off) {
        if (m.length < off + 4) return;

        int meInstance = (m[off] >> 7) & 0x01; // 0=Primary,1=Secondary
        if (meInstance != 0) return;          // usiamo solo la Primary

        int constellation = m[off + 1] & 0xFF;
        int used = m[off + 2] & 0xFF;
        int tracked = m[off + 3] & 0xFF;

        if (constellation < satUsedConst.length) {
            satUsedConst[constellation] = used;
            satTrackedConst[constellation] = tracked;
        }

        int totUsed = 0, totTracked = 0;
        for (int i = 0; i < satUsedConst.length; i++) {
            totUsed += satUsedConst[i];
            totTracked += satTrackedConst[i];
        }
        out.satUsed = totUsed;
        out.satTracked = totTracked;
    }

    // ==================================================================
    // HELPERS BIG-ENDIAN
    // ==================================================================
    private long u40BE(byte[] buf, int off) {
        return ((long) (buf[off] & 0xFF) << 32) |
                ((long) (buf[off + 1] & 0xFF) << 24) |
                ((long) (buf[off + 2] & 0xFF) << 16) |
                ((long) (buf[off + 3] & 0xFF) << 8) |
                (long) (buf[off + 4] & 0xFF);
    }

    private int u24BE(byte[] buf, int off) {
        return ((buf[off] & 0xFF) << 16) |
                ((buf[off + 1] & 0xFF) << 8) |
                (buf[off + 2] & 0xFF);
    }

    private int u16BE(byte[] buf, int off) {
        return ((buf[off] & 0xFF) << 8) |
                (buf[off + 1] & 0xFF);
    }

}
