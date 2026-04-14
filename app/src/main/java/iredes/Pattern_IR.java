package iredes;

import java.util.Collections;
import java.util.List;

public final class Pattern_IR {

    private final String patternId;
    private final String patternName;
    private final List<DrillHole_IR> holes;

    public Pattern_IR(String patternId, String patternName, List<DrillHole_IR> holes) {
        this.patternId = patternId;
        this.patternName = patternName;
        this.holes = holes == null
                ? Collections.<DrillHole_IR>emptyList()
                : Collections.unmodifiableList(holes);
    }

    public String getPatternId() {
        return patternId;
    }

    public String getPatternName() {
        return patternName;
    }

    public List<DrillHole_IR> getHoles() {
        return holes;
    }
}