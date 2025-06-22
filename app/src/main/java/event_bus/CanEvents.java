package event_bus;

public class CanEvents {

    public final String candata;
    public final int id, channel;
    public final byte[] msg;

    public CanEvents(int channel, String candata, int id, byte[] msg) {
        this.channel = channel;
        this.candata = candata;
        this.id = id;
        this.msg = msg;
    }
}
