package iredes;

import java.util.Collections;
import java.util.List;

public final class DrillPlan_IR {

    private final String planId;
    private final String planName;
    private final String project;
    private final String workOrder;

    private final List<DrillHole_IR> holes;
    private final List<Pattern_IR> patterns;

    public DrillPlan_IR(String planId,
                        String planName,
                        String project,
                        String workOrder,
                        List<DrillHole_IR> holes,
                        List<Pattern_IR> patterns) {
        this.planId = planId;
        this.planName = planName;
        this.project = project;
        this.workOrder = workOrder;
        this.holes = holes == null
                ? Collections.<DrillHole_IR>emptyList()
                : Collections.unmodifiableList(holes);
        this.patterns = patterns == null
                ? Collections.<Pattern_IR>emptyList()
                : Collections.unmodifiableList(patterns);
    }

    public String getPlanId() {
        return planId;
    }

    public String getPlanName() {
        return planName;
    }

    public String getProject() {
        return project;
    }

    public String getWorkOrder() {
        return workOrder;
    }

    public List<DrillHole_IR> getHoles() {
        return holes;
    }

    public List<Pattern_IR> getPatterns() {
        return patterns;
    }
}