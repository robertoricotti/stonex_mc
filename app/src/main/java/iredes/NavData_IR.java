package iredes;

import java.time.Instant;

public final class NavData_IR {

    private final Instant timestamp;
    private final String machineId;

    private final Point3D_IR position;
    private final double headingDeg;
    private final double pitchDeg;
    private final double rollDeg;

    private final String activeHoleId;  // opzionale

    public NavData_IR(Instant timestamp,
                      String machineId,
                      Point3D_IR position,
                      double headingDeg,
                      double pitchDeg,
                      double rollDeg,
                      String activeHoleId) {
        this.timestamp = timestamp;
        this.machineId = machineId;
        this.position = position;
        this.headingDeg = headingDeg;
        this.pitchDeg = pitchDeg;
        this.rollDeg = rollDeg;
        this.activeHoleId = activeHoleId;
    }

    public Instant getTimestamp() { return timestamp; }
    public String getMachineId() { return machineId; }
    public Point3D_IR getPosition() { return position; }
    public double getHeadingDeg() { return headingDeg; }
    public double getPitchDeg() { return pitchDeg; }
    public double getRollDeg() { return rollDeg; }
    public String getActiveHoleId() { return activeHoleId; }
}