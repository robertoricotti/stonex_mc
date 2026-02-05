package DPAD;

public enum Step {

    CM5(0.05), CM10(0.10), CM15(0.15),
    CM20(0.20), CM25(0.25), CM30(0.30);

    public final double meters;
    Step(double m) { this.meters = m; }

    public Step next() {
        Step[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
