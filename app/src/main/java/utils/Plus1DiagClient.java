package utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * PLUS+1 diagnostic client for Danfoss-style extended-address ISO-TP over CAN.
 *
 * Current goal:
 * - Read ECU info with 1A 8A
 * - Detect APPLICATION mode vs BOOT/RECOVERY mode
 * - Test the valid diagnostic path
 * - Log TX/RX as reconstructed diagnostic payloads, not only raw CAN frames
 *
 * IMPORTANT:
 * This class does NOT implement/provide any proprietary seed-key algorithm.
 * Plug your legitimate key provider into SecurityKeyProvider.
 */
public class Plus1DiagClient {

    private static final String TAG = "PLUS1_DIAG";

    public static final int TX_ID = 0x0CDA01F1; // Service Tool -> ECU
    public static final int RX_ID = 0x0CDA0103; // ECU -> Service Tool

    private static final int DEFAULT_CHANNEL = 1;

    private static final byte TX_ADDR = 0x03;
    private static final byte RX_ADDR = (byte) 0xF1;

    private final LinkedBlockingQueue<CanFrame> rxQueue = new LinkedBlockingQueue<>();
    private final Object txLock = new Object();

    private final CanWriter canWriter;
    private final SecurityKeyProvider securityKeyProvider;

    public enum EcuMode {
        APPLICATION,
        BOOT_RECOVERY,
        UNKNOWN
    }

    public static class EcuInfo {
        public final EcuMode mode;
        public final byte[] rawPayload;
        public final String ascii;

        public EcuInfo(EcuMode mode, byte[] rawPayload, String ascii) {
            this.mode = mode;
            this.rawPayload = rawPayload;
            this.ascii = ascii;
        }

        @Override
        public String toString() {
            return "EcuInfo{mode=" + mode
                    + ", raw=" + bytesToHex(rawPayload)
                    + ", ascii='" + ascii + "'}";
        }
    }

    public static class DiagnosticException extends Exception {
        public final byte[] request;
        public final byte[] response;

        public DiagnosticException(String message) {
            super(message);
            this.request = null;
            this.response = null;
        }

        public DiagnosticException(String message, byte[] request, byte[] response) {
            super(message);
            this.request = request;
            this.response = response;
        }
    }

    public interface CanWriter {
        void write(boolean extended, int channel, int id, int dlc, byte[] data) throws Exception;
    }

    /**
     * Provide the official/legitimate security key for the given seed.
     * Return null if no key is available.
     */
    public interface SecurityKeyProvider {
        byte[] calculateKey(byte level, byte[] seed);
    }

    public Plus1DiagClient(CanWriter canWriter) {
        this(canWriter, null);
    }

    public Plus1DiagClient(CanWriter canWriter, SecurityKeyProvider securityKeyProvider) {
        if (canWriter == null) {
            throw new IllegalArgumentException("canWriter == null");
        }
        this.canWriter = canWriter;
        this.securityKeyProvider = securityKeyProvider;
    }

    /**
     * Call this from your CAN RX callback.
     */
    public void onCanFrameReceived(boolean extended, int channel, int id, int dlc, byte[] data) {
        if (!extended) return;
        if (id != RX_ID) return;
        if (data == null || data.length == 0) return;

        int safeDlc = Math.max(0, Math.min(dlc, data.length));
        if (safeDlc <= 0) return;

        byte[] clean = Arrays.copyOf(data, safeDlc);
        rxQueue.offer(new CanFrame(true, channel, id, clean, System.currentTimeMillis()));
    }

    /**
     * Clear old ECU frames before starting a controlled diagnostic request.
     * Useful because the ECU emits periodic frames such as F1 03 C4 01 A4.
     */
    public void clearRxQueue() {
        rxQueue.clear();
    }

    /**
     * Full initial diagnostic:
     * 1) reads ECU info with 1A 8A
     * 2) detects APPLICATION vs BOOT_RECOVERY
     * 3) runs the correct minimal diagnostic branch
     */
    public EcuInfo runInitialDiagnostic() throws Exception {
        clearRxQueue();

        EcuInfo info = readEcuInfo();
        Log.i(TAG, "ECU INFO: " + info);

        if (info.mode == EcuMode.BOOT_RECOVERY) {
            Log.w(TAG, "ECU is in BOOT/RECOVERY mode. Running boot diagnostic branch.");
            runBootRecoveryDiagnostic();
        } else if (info.mode == EcuMode.APPLICATION) {
            Log.i(TAG, "ECU is in APPLICATION mode. Running application diagnostic branch.");
            runApplicationDiagnostic();
        } else {
            Log.w(TAG, "ECU mode UNKNOWN. Not continuing automatically.");
        }

        return info;
    }

    /**
     * Reads ECU information. This is safe and does not start flashing.
     *
     * TX: 1A 8A
     * RX: 5A 0B ...
     */
    public EcuInfo readEcuInfo() throws Exception {
        byte[] response = sendAndWaitPositive(hex("1A 8A"), 8000);
        EcuMode mode = detectModeFromInfoResponse(response);
        String ascii = printableAscii(response);
        return new EcuInfo(mode, response, ascii);
    }

    private EcuMode detectModeFromInfoResponse(byte[] payload) {
        if (payload == null || payload.length < 5) return EcuMode.UNKNOWN;

        String ascii = printableAscii(payload).toLowerCase(Locale.US);

        // In your corrupted/boot traces, the info response contains "Boot".
        if (ascii.contains("boot")) {
            return EcuMode.BOOT_RECOVERY;
        }

        // Observed pattern:
        // 5A 0B 00 03 00 ... normal app
        // 5A 0B 00 03 01 ... boot/recovery
        if ((payload[0] & 0xFF) == 0x5A
                && (payload[1] & 0xFF) == 0x0B
                && (payload[2] & 0xFF) == 0x00
                && (payload[3] & 0xFF) == 0x03) {

            int modeByte = payload[4] & 0xFF;
            if (modeByte == 0x00) return EcuMode.APPLICATION;
            if (modeByte == 0x01) return EcuMode.BOOT_RECOVERY;
        }

        return EcuMode.UNKNOWN;
    }

    /**
     * Branch for ECU with valid application.
     *
     * This does NOT flash. It only confirms session + security seed flow.
     */
    public void runApplicationDiagnostic() throws Exception {
        // In app mode this should be accepted.
        byte[] rSession = sendAndWaitPositive(hex("10 87"), 8000);
        Log.i(TAG, "APP 10 87 OK: " + bytesToHex(rSession));

        // Service Tool tries 27 01 and receives 7F 27 12.
        // Treat this as informational, not fatal.
        try {
            byte[] r2701 = sendAndWaitPositive(hex("27 01"), 3000);
            Log.i(TAG, "APP 27 01 OK/unexpected: " + bytesToHex(r2701));
        } catch (Exception e) {
            Log.w(TAG, "APP 27 01 expected unsupported: " + e.getMessage());
        }

        // Real seed level observed in your traces.
        requestSeedAndOptionallySendKey((byte) 0xFA, (byte) 0xFB, 8000);
    }

    /**
     * Branch for corrupted/boot ECU.
     *
     * Important: in your traces 10 87 is NOT accepted in boot mode.
     * So this branch tests recovery-compatible commands without forcing 10 87 first.
     */
    public void runBootRecoveryDiagnostic() throws Exception {
        // First: direct security seed request in boot mode.
        try {
            requestSeedAndOptionallySendKey((byte) 0xFA, (byte) 0xFB, 8000);
        } catch (Exception e) {
            Log.e(TAG, "BOOT 27 FA/FB failed: " + e.getMessage(), e);
        }

        // Test 10 85, because Service Tool uses it elsewhere and it may be accepted
        // in a different state. Do not make this fatal.
        try {
            byte[] rSession85 = sendAndWaitPositive(hex("10 85"), 8000);
            Log.i(TAG, "BOOT 10 85 OK: " + bytesToHex(rSession85));
        } catch (Exception e) {
            Log.w(TAG, "BOOT 10 85 failed/non-supported: " + e.getMessage());
        }

        // Tester present. Do not make this fatal either.
        try {
            byte[] rTester = sendAndWaitPositive(hex("3E 01"), 3000);
            Log.i(TAG, "BOOT 3E 01 OK: " + bytesToHex(rTester));
        } catch (Exception e) {
            Log.w(TAG, "BOOT 3E 01 failed/non-supported: " + e.getMessage());
        }
    }

    /**
     * Requests seed and, if a SecurityKeyProvider exists, sends the matching key.
     *
     * requestSeedLevel = FA means request 27 FA
     * sendKeyLevel    = FB means send    27 FB <key>
     */
    public byte[] requestSeedAndOptionallySendKey(byte requestSeedLevel,
                                                  byte sendKeyLevel,
                                                  long timeoutMs) throws Exception {
        byte[] seedRequest = new byte[]{0x27, requestSeedLevel};
        byte[] seedResp = sendAndWaitPositive(seedRequest, timeoutMs);

        Log.i(TAG, "SEED RESP: " + bytesToHex(seedResp));

        if (seedResp.length < 6
                || (seedResp[0] & 0xFF) != 0x67
                || seedResp[1] != requestSeedLevel) {
            throw new DiagnosticException("Unexpected seed response: " + bytesToHex(seedResp),
                    seedRequest, seedResp);
        }

        byte[] seed = new byte[]{seedResp[2], seedResp[3], seedResp[4], seedResp[5]};
        Log.i(TAG, "SEED level=" + hexByte(requestSeedLevel & 0xFF)
                + " seed=" + bytesToHex(seed));

        if (securityKeyProvider == null) {
            Log.w(TAG, "No SecurityKeyProvider configured. Stop after seed.");
            return seedResp;
        }

        byte[] key = securityKeyProvider.calculateKey(requestSeedLevel, seed);
        if (key == null || key.length == 0) {
            Log.w(TAG, "SecurityKeyProvider returned no key. Stop after seed.");
            return seedResp;
        }

        byte[] keyRequest = concat(new byte[]{0x27, sendKeyLevel}, key);
        byte[] keyResp = sendAndWaitPositive(keyRequest, timeoutMs);

        Log.i(TAG, "KEY RESP: " + bytesToHex(keyResp));
        return keyResp;
    }

    /**
     * Generic diagnostic send.
     * requestPayload must be the complete diagnostic payload WITHOUT:
     * - CAN ID
     * - extended address byte 03/F1
     * - ISO-TP PCI byte
     */
    public byte[] sendAndWaitPositive(byte[] requestPayload, long timeoutMs) throws Exception {
        if (requestPayload == null || requestPayload.length == 0) {
            throw new IllegalArgumentException("Empty diagnostic request");
        }

        synchronized (txLock) {
            int sid = requestPayload[0] & 0xFF;
            int expectedPositiveSid = (sid + 0x40) & 0xFF;

            Log.i(TAG, "DIAG TX " + bytesToHex(requestPayload)
                    + " expectedSid=" + hexByte(expectedPositiveSid));

            sendIsoTpMessage(requestPayload);

            long deadline = System.currentTimeMillis() + timeoutMs;

            while (System.currentTimeMillis() < deadline) {
                byte[] response = receiveIsoTpMessage(Math.max(1, deadline - System.currentTimeMillis()));

                if (response == null || response.length == 0) continue;

                Log.i(TAG, "DIAG RX " + bytesToHex(response));

                int rSid = response[0] & 0xFF;

                // Negative response.
                if (rSid == 0x7F) {
                    if (response.length < 3) {
                        throw new DiagnosticException("Malformed negative response: " + bytesToHex(response),
                                requestPayload, response);
                    }

                    int originalSid = response[1] & 0xFF;
                    int nrc = response[2] & 0xFF;

                    // 0x78 = response pending. Keep waiting.
                    if (originalSid == sid && nrc == 0x78) {
                        Log.w(TAG, "Response pending for SID=" + hexByte(sid));
                        continue;
                    }

                    throw new DiagnosticException("Negative response. SID="
                            + hexByte(originalSid)
                            + " NRC="
                            + hexByte(nrc)
                            + " full="
                            + bytesToHex(response), requestPayload, response);
                }

                if (rSid == expectedPositiveSid) {
                    if (!subFunctionMatches(requestPayload, response)) {
                        Log.w(TAG, "Positive SID ok but subfunction mismatch. req="
                                + bytesToHex(requestPayload)
                                + " resp="
                                + bytesToHex(response));
                        continue;
                    }

                    return response;
                }

                Log.w(TAG, "Unexpected response. Expected SID="
                        + hexByte(expectedPositiveSid)
                        + " got="
                        + hexByte(rSid)
                        + " resp="
                        + bytesToHex(response));
            }

            throw new DiagnosticException("Timeout waiting positive response for request "
                    + bytesToHex(requestPayload));
        }
    }

    /**
     * Use this for commands where you know a more strict expected response is needed.
     * Example: TransferData may need 76 73 00 rather than just 76.
     */
    public byte[] sendAndWait(byte[] requestPayload,
                              ResponseMatcher matcher,
                              long timeoutMs) throws Exception {
        if (requestPayload == null || requestPayload.length == 0) {
            throw new IllegalArgumentException("Empty diagnostic request");
        }
        if (matcher == null) throw new IllegalArgumentException("matcher == null");

        synchronized (txLock) {
            Log.i(TAG, "DIAG TX " + bytesToHex(requestPayload));

            sendIsoTpMessage(requestPayload);
            long deadline = System.currentTimeMillis() + timeoutMs;

            while (System.currentTimeMillis() < deadline) {
                byte[] response = receiveIsoTpMessage(Math.max(1, deadline - System.currentTimeMillis()));
                if (response == null || response.length == 0) continue;

                Log.i(TAG, "DIAG RX " + bytesToHex(response));

                if (response[0] == 0x7F) {
                    if (response.length >= 3
                            && (response[2] & 0xFF) == 0x78
                            && (response[1] & 0xFF) == (requestPayload[0] & 0xFF)) {
                        Log.w(TAG, "Response pending for SID=" + hexByte(requestPayload[0] & 0xFF));
                        continue;
                    }

                    throw new DiagnosticException("Negative response: " + bytesToHex(response),
                            requestPayload, response);
                }

                if (matcher.matches(requestPayload, response)) {
                    return response;
                }

                Log.w(TAG, "Response did not match. resp=" + bytesToHex(response));
            }

            throw new DiagnosticException("Timeout waiting matched response for request "
                    + bytesToHex(requestPayload));
        }
    }

    public interface ResponseMatcher {
        boolean matches(byte[] request, byte[] response);
    }

    /**
     * Some services have a subfunction/local id in byte 1 that should match:
     * 10 87 -> 50 87
     * 27 FA -> 67 FA
     * 27 FB -> 67 FB
     * 31 FD -> 71 FD
     * 33 FD -> 73 FD
     * 3B 9A -> 7B 9A
     */
    private boolean subFunctionMatches(byte[] request, byte[] response) {
        if (request.length < 2 || response.length < 2) return true;

        int sid = request[0] & 0xFF;

        switch (sid) {
            case 0x10:
            case 0x11:
            case 0x27:
            case 0x31:
            case 0x33:
            case 0x3B:
            case 0x21:
            case 0x2C:
                return (request[1] & 0xFF) == (response[1] & 0xFF);

            case 0x1A:
                // Observed in your traces:
                // TX 1A 8A
                // RX 5A 0B ...
                // For this service the second byte of the positive response is NOT 0x8A,
                // so do not enforce byte[1] equality here.
                return true;

            default:
                return true;
        }
    }

    /**
     * ISO-TP extended addressing TX.
     * First CAN byte is target address 0x03.
     */
    private void sendIsoTpMessage(byte[] payload) throws Exception {
        if (payload.length <= 6) {
            byte[] frame = new byte[2 + payload.length];
            frame[0] = TX_ADDR;
            frame[1] = (byte) (payload.length & 0x0F);
            System.arraycopy(payload, 0, frame, 2, payload.length);

            writeFrame(TX_ID, frame);
            return;
        }

        if (payload.length > 0xFFF) {
            throw new DiagnosticException("ISO-TP payload too long: " + payload.length);
        }

        byte[] first = new byte[8];
        first[0] = TX_ADDR;
        first[1] = (byte) (0x10 | ((payload.length >> 8) & 0x0F));
        first[2] = (byte) (payload.length & 0xFF);

        int firstDataLen = 5;
        System.arraycopy(payload, 0, first, 3, firstDataLen);

        writeFrame(TX_ID, first);

        FlowControl fc = waitFlowControl(3000);

        int offset = firstDataLen;
        int seq = 1;
        int sentInBlock = 0;

        while (offset < payload.length) {
            int remaining = payload.length - offset;
            int chunkLen = Math.min(6, remaining);

            byte[] cf = new byte[2 + chunkLen];
            cf[0] = TX_ADDR;
            cf[1] = (byte) (0x20 | (seq & 0x0F));
            System.arraycopy(payload, offset, cf, 2, chunkLen);

            writeFrame(TX_ID, cf);

            offset += chunkLen;
            seq = (seq + 1) & 0x0F;
            sentInBlock++;

            if (fc.stMinMs > 0) {
                Thread.sleep(fc.stMinMs);
            }

            if (fc.blockSize > 0 && sentInBlock >= fc.blockSize && offset < payload.length) {
                fc = waitFlowControl(3000);
                sentInBlock = 0;
            }
        }
    }

    private FlowControl waitFlowControl(long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            CanFrame frame = rxQueue.poll(Math.max(1, deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
            if (frame == null) continue;

            Log.i(TAG, "CAN RX id=" + hexId(frame.id) + " data=" + bytesToHex(frame.data));

            if (frame.data.length < 4) continue;
            if (frame.data[0] != RX_ADDR) continue;

            int pci = frame.data[1] & 0xFF;

            if ((pci & 0xF0) != 0x30) {
                // This can happen if periodic ECU diagnostic frames are in the queue.
                // Reinsert once and let the diagnostic receiver process it later.
                rxQueue.offer(frame);
                Thread.sleep(1);
                continue;
            }

            int fs = pci & 0x0F;
            if (fs != 0x00) {
                throw new DiagnosticException("FlowControl not ContinueToSend. FS=" + fs);
            }

            int bs = frame.data[2] & 0xFF;
            int stMinRaw = frame.data[3] & 0xFF;

            int stMinMs;
            if (stMinRaw <= 0x7F) {
                stMinMs = stMinRaw;
            } else {
                // 0xF1..0xF9 = 100us..900us. Java sleep cannot reliably do sub-ms.
                stMinMs = 1;
            }

            return new FlowControl(bs, stMinMs);
        }

        throw new DiagnosticException("Timeout waiting FlowControl");
    }

    /**
     * Receives one full ISO-TP diagnostic payload.
     * Return value excludes:
     * - extended address byte
     * - PCI byte(s)
     */
    private byte[] receiveIsoTpMessage(long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            CanFrame frame = rxQueue.poll(Math.max(1, deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
            if (frame == null) continue;

            Log.i(TAG, "CAN RX id=" + hexId(frame.id) + " data=" + bytesToHex(frame.data));

            if (frame.data.length < 2) continue;
            if (frame.data[0] != RX_ADDR) continue;

            int pci = frame.data[1] & 0xFF;
            int type = pci & 0xF0;

            // Single Frame
            if (type == 0x00) {
                int len = pci & 0x0F;
                if (frame.data.length < 2 + len) {
                    throw new DiagnosticException("Malformed SingleFrame: " + bytesToHex(frame.data));
                }

                byte[] payload = Arrays.copyOfRange(frame.data, 2, 2 + len);

                // Ignore periodic ECU status frame C4 01 A4 if it arrives while waiting
                // for a command response.
                if (payload.length >= 3
                        && (payload[0] & 0xFF) == 0xC4
                        && (payload[1] & 0xFF) == 0x01) {
                    Log.w(TAG, "Ignoring periodic status payload: " + bytesToHex(payload));
                    continue;
                }

                return payload;
            }

            // First Frame
            if (type == 0x10) {
                int totalLen = ((pci & 0x0F) << 8) | (frame.data[2] & 0xFF);
                ByteArrayOutputStream out = new ByteArrayOutputStream(totalLen);

                int firstLen = Math.min(5, totalLen);
                if (frame.data.length < 3 + firstLen) {
                    throw new DiagnosticException("Malformed FirstFrame: " + bytesToHex(frame.data));
                }

                out.write(frame.data, 3, firstLen);

                // Send FC: [03, 30, 00, 00]
                writeFrame(TX_ID, new byte[]{TX_ADDR, 0x30, 0x00, 0x00});

                int expectedSeq = 1;

                while (out.size() < totalLen && System.currentTimeMillis() < deadline) {
                    CanFrame cf = rxQueue.poll(Math.max(1, deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
                    if (cf == null) continue;

                    Log.i(TAG, "CAN RX id=" + hexId(cf.id) + " data=" + bytesToHex(cf.data));

                    if (cf.data.length < 2) continue;
                    if (cf.data[0] != RX_ADDR) continue;

                    int cfPci = cf.data[1] & 0xFF;
                    if ((cfPci & 0xF0) != 0x20) {
                        Log.w(TAG, "Ignoring non-CF while receiving multi-frame: " + bytesToHex(cf.data));
                        continue;
                    }

                    int seq = cfPci & 0x0F;
                    if (seq != expectedSeq) {
                        throw new DiagnosticException("ISO-TP sequence mismatch. expected="
                                + expectedSeq + " got=" + seq);
                    }

                    int remaining = totalLen - out.size();
                    int chunkLen = Math.min(6, remaining);

                    if (cf.data.length < 2 + chunkLen) {
                        throw new DiagnosticException("Malformed ConsecutiveFrame: " + bytesToHex(cf.data));
                    }

                    out.write(cf.data, 2, chunkLen);
                    expectedSeq = (expectedSeq + 1) & 0x0F;
                }

                if (out.size() != totalLen) {
                    throw new DiagnosticException("Timeout receiving multi-frame. got="
                            + out.size() + " expected=" + totalLen);
                }

                return out.toByteArray();
            }

            if (type == 0x30) {
                Log.w(TAG, "Ignoring FlowControl while waiting diagnostic response");
                continue;
            }

            throw new DiagnosticException("Unsupported ISO-TP frame type: " + bytesToHex(frame.data));
        }

        return null;
    }

    private void writeFrame(int id, byte[] data) throws Exception {
        if (data == null) throw new IllegalArgumentException("data null");
        if (data.length < 1 || data.length > 8) {
            throw new IllegalArgumentException("Invalid CAN DLC: " + data.length);
        }

        Log.i(TAG, "CAN TX id=" + hexId(id) + " data=" + bytesToHex(data));
        canWriter.write(true, DEFAULT_CHANNEL, id, data.length, data);
    }

    private static class FlowControl {
        final int blockSize;
        final int stMinMs;

        FlowControl(int blockSize, int stMinMs) {
            this.blockSize = blockSize;
            this.stMinMs = stMinMs;
        }
    }

    public static class CanFrame {
        public final boolean extended;
        public final int channel;
        public final int id;
        public final byte[] data;
        public final long timestampMs;

        public CanFrame(boolean extended, int channel, int id, byte[] data, long timestampMs) {
            this.extended = extended;
            this.channel = channel;
            this.id = id;
            this.data = data;
            this.timestampMs = timestampMs;
        }
    }

    public static byte[] hex(String s) {
        if (s == null) return new byte[0];

        s = s.replace("0x", "")
                .replace(",", " ")
                .replace("[", " ")
                .replace("]", " ")
                .trim();

        if (s.isEmpty()) return new byte[0];

        String[] parts = s.split("\\s+");
        byte[] out = new byte[parts.length];

        for (int i = 0; i < parts.length; i++) {
            out[i] = (byte) Integer.parseInt(parts[i], 16);
        }

        return out;
    }

    public static byte[] concat(byte[] a, byte[] b) {
        if (a == null) a = new byte[0];
        if (b == null) b = new byte[0];

        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    public static String bytesToHex(byte[] data) {
        if (data == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(String.format(Locale.US, "%02X", b & 0xFF));
        }
        return sb.toString();
    }

    private static String printableAscii(byte[] data) {
        if (data == null || data.length == 0) return "";

        String raw = new String(data, StandardCharsets.ISO_8859_1);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= 32 && c <= 126) {
                sb.append(c);
            } else {
                sb.append('.');
            }
        }

        return sb.toString();
    }

    private static String hexByte(int v) {
        return String.format(Locale.US, "0x%02X", v & 0xFF);
    }

    private static String hexId(int id) {
        return String.format(Locale.US, "0x%08X", id);
    }
}
