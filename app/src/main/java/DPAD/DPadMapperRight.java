package DPAD;

public class DPadMapperRight {
    public double rightAxisX, rightAxisY, rightYaw, rightHatX, rightHatY;

    public DPadMapperRight(double rightAxisX, double rightAxisY, double rightYaw, double rightHatX, double rightHatY) {
        this.rightAxisX = rightAxisX;
        this.rightAxisY = rightAxisY;
        this.rightYaw = rightYaw;
        this.rightHatX = rightHatX;
        this.rightHatY = rightHatY;
    }

    public double getRightAxisX() {
        return rightAxisX;
    }

    public void setRightAxisX(double rightAxisX) {
        this.rightAxisX = rightAxisX;
    }

    public double getRightAxisY() {
        return rightAxisY;
    }

    public void setRightAxisY(double rightAxisY) {
        this.rightAxisY = rightAxisY;
    }

    public double getRightYaw() {
        return rightYaw;
    }

    public void setRightYaw(double rightYaw) {
        this.rightYaw = rightYaw;
    }

    public double getRightHatX() {
        return rightHatX;
    }

    public void setRightHatX(double rightHatX) {
        this.rightHatX = rightHatX;
    }

    public double getRightHatY() {
        return rightHatY;
    }

    public void setRightHatY(double rightHatY) {
        this.rightHatY = rightHatY;
    }
}
