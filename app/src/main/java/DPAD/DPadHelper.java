package DPAD;

public class DPadHelper {

    private static final DPadHelper instance = new DPadHelper();
    private DPadMapperLeft left = new DPadMapperLeft(0, 0, 0, 0, 0);
    private DPadMapperRight right = new DPadMapperRight(0, 0, 0, 0, 0);
    private double[] xyz = new double[]{0, 0, 0};
    private Step step = Step.CM2;

    private DPadHelper() {
    }

    public static DPadHelper getInstance() {
        return instance;
    }

    public DPadMapperLeft getLeft() {
        return left;
    }

    public void setLeft(double axisX, double axisY, double yaw, double hatX, double hatY) {
        this.left = new DPadMapperLeft(normalize180(axisX), normalize180(axisY), normalize180(yaw), normalize180(hatX), normalize180(hatY));
    }

    public DPadMapperRight getRight() {
        return right;
    }

    public void setRight(double axisX, double axisY, double yaw, double hatX, double hatY) {
        this.right = new DPadMapperRight(normalize180(axisX), normalize180(axisY), normalize180(yaw), normalize180(hatX), normalize180(hatY));
    }

    public double[] getXYZ() {
        return xyz;
    }

    public void setXYZ(double[] xyz) {
        this.xyz = xyz;
    }

    public void setX(double x) {
        this.xyz[0] = x;
    }

    public double getX() {
        return xyz[0];
    }

    public void setY(double y) {
        this.xyz[1] = y;
    }

    public double getY() {
        return xyz[1];
    }

    public void setZ(double z) {
        this.xyz[2] = z;
    }

    public double getZ() {
        return xyz[2];
    }


    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public double normalize180(double a) {
        a = a % 360.0;
        if (a > 180) a -= 360;
        if (a < -180) a += 360;
        return a;
    }

    public static String getMarcia(Step step) {

        return switch (step) {
            case CM2 -> "1";
            case CM5 -> "2";
            case CM10 -> "3";
            case CM20 -> "4";
            case CM25 -> "5";
            case CM30 -> "6";
            default -> "N";
        };
    }
}
