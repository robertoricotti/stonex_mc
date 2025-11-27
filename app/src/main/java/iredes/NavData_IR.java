package iredes;

public final class NavData_IR {

    private final String timestampIso;   // ISO8601, stringa per compatibilità Android
    private final String machineId;

    private final Point3D_IR position;
    private final double headingDeg;
    private final double pitchDeg;
    private final double rollDeg;

    private final String activeHoleId;   // opzionale

    public NavData_IR(String timestampIso,
                      String machineId,
                      Point3D_IR position,
                      double headingDeg,
                      double pitchDeg,
                      double rollDeg,
                      String activeHoleId) {
        this.timestampIso = timestampIso;
        this.machineId = machineId;
        this.position = position;
        this.headingDeg = headingDeg;
        this.pitchDeg = pitchDeg;
        this.rollDeg = rollDeg;
        this.activeHoleId = activeHoleId;
    }

    public String getTimestampIso() { return timestampIso; }

    public String getMachineId() { return machineId; }

    public Point3D_IR getPosition() { return position; }

    public double getHeadingDeg() { return headingDeg; }

    public double getPitchDeg() { return pitchDeg; }

    public double getRollDeg() { return rollDeg; }

    public String getActiveHoleId() { return activeHoleId; }
}