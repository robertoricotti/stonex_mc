package DPAD;

public final class DPadMapper {
    public final double leftAxisX, leftAxisY, leftYaw, leftHatX, leftHatY;
    public final double rightAxisX, rightAxisY, rightYaw, rightHatX, rightHatY;
    public final double[] xyz; // copia difensiva fatta nel costruttore

    public DPadMapper(double leftAxisX, double leftAxisY, double leftYaw,
                      double leftHatX, double leftHatY,
                      double rightAxisX, double rightAxisY, double rightYaw,
                      double rightHatX, double rightHatY,
                      double[] xyz) {

        this.leftAxisX = leftAxisX;
        this.leftAxisY = leftAxisY;
        this.leftYaw = leftYaw;
        this.leftHatX = leftHatX;
        this.leftHatY = leftHatY;

        this.rightAxisX = rightAxisX;
        this.rightAxisY = rightAxisY;
        this.rightYaw = rightYaw;
        this.rightHatX = rightHatX;
        this.rightHatY = rightHatY;

        this.xyz = (xyz == null) ? new double[]{0,0,0} : xyz.clone();
    }


}
