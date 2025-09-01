package event_bus;

public class CanEvents {

    public final String candata;
    public final int id, channel;
    public final byte[] msg;
    public final int dlc;

    public CanEvents(int channel, String candata, int id,int dlc, byte[] msg) {
        this.channel = channel;
        this.candata = candata;
        this.id = id;
        this.dlc=dlc;
        this.msg = msg;
    }
}
