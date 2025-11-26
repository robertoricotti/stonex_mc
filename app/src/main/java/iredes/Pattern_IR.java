package iredes;

import java.util.List;

public final class Pattern_IR {

    private final String patternId;
    private final String patternName;

    private final List<DrillHole_IR> holes;

    public Pattern_IR(String id, String name, List<DrillHole_IR> holes) {
        this.patternId = id;
        this.patternName = name;
        this.holes = holes;
    }

    // getters...

    public List<DrillHole_IR> getHoles() {
        return holes;
    }

    public String getPatternId() {
        return patternId;
    }

    public String getPatternName() {
        return patternName;
    }

}
