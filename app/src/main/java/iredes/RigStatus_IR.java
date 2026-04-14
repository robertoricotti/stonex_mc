package iredes;

public final class RigStatus_IR {

    private final RigOperationMode mode;
    private final double currentDepth;    // m
    private final boolean bitActive;
    private final boolean mwdEnabled;

    public RigStatus_IR(RigOperationMode mode,
                        double currentDepth,
                        boolean bitActive,
                        boolean mwdEnabled) {
        this.mode = mode;
        this.currentDepth = currentDepth;
        this.bitActive = bitActive;
        this.mwdEnabled = mwdEnabled;
    }

    public RigOperationMode getMode() {
        return mode;
    }

    public double getCurrentDepth() {
        return currentDepth;
    }

    public boolean isBitActive() {
        return bitActive;
    }

    public boolean isMwdEnabled() {
        return mwdEnabled;
    }
}