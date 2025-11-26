package iredes;

public final class DrillHole_IR {

    private final String holeId;      // da <HoleId>
    private final String holeName;    // da <HoleName>
    private final String patternId;   // opzionale, se hai Pattern o BoomSeq

    private final Point3D_IR collar;  // StartPoint
    private final Point3D_IR toe;     // EndPoint

    public DrillHole_IR(String holeId,
                        String holeName,
                        String patternId,
                        Point3D_IR collar,
                        Point3D_IR toe) {
        this.holeId = holeId;
        this.holeName = holeName;
        this.patternId = patternId;
        this.collar = collar;
        this.toe = toe;
    }

    public String getHoleId() { return holeId; }
    public String getHoleName() { return holeName; }
    public String getPatternId() { return patternId; }
    public Point3D_IR getCollar() { return collar; }
    public Point3D_IR getToe() { return toe; }
}