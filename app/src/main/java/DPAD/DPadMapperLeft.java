package DPAD;

public class DPadMapperLeft {
    public double leftAxisX, leftAxisY, leftYaw, leftHatX, leftHatY;

    public DPadMapperLeft(double leftAxisX, double leftAxisY, double leftYaw, double leftHatX, double leftHatY) {
        this.leftAxisX = leftAxisX;
        this.leftAxisY = leftAxisY;
        this.leftYaw = leftYaw;
        this.leftHatX = leftHatX;
        this.leftHatY = leftHatY;
    }

    public double getLeftAxisX() {
        return leftAxisX;
    }

    public void setLeftAxisX(double leftAxisX) {
        this.leftAxisX = leftAxisX;
    }

    public double getLeftAxisY() {
        return leftAxisY;
    }

    public void setLeftAxisY(double leftAxisY) {
        this.leftAxisY = leftAxisY;
    }

    public double getLeftYaw() {
        return leftYaw;
    }

    public void setLeftYaw(double leftYaw) {
        this.leftYaw = leftYaw;
    }

    public double getLeftHatX() {
        return leftHatX;
    }

    public void setLeftHatX(double leftHatX) {
        this.leftHatX = leftHatX;
    }

    public double getLeftHatY() {
        return leftHatY;
    }

    public void setLeftHatY(double leftHatY) {
        this.leftHatY = leftHatY;
    }
}
