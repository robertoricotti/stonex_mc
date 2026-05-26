package utils;

public class CanFrame {
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