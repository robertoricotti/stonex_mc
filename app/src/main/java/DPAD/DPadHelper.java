package DPAD;

public class DPadHelper {

    private static final DPadHelper instance = new DPadHelper();

    private volatile DPadMapper state = new DPadMapper(
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            new double[]{0, 0, 0}
    );

    private DPadHelper() {}

    public static DPadHelper getInstance() {
        return instance;
    }

    public DPadMapper getSnapshot() {
        return state;
    }

    public void update(double leftAxisX, double leftAxisY, double leftYaw,
                       double leftHatX, double leftHatY,
                       double rightAxisX, double rightAxisY, double rightYaw,
                       double rightHatX, double rightHatY,
                       double[] xyz) {

        state = new DPadMapper(normalize180(leftAxisX), normalize180(leftAxisY), normalize180(leftYaw),
                normalize180(leftHatX), normalize180(leftHatY),
                normalize180(rightAxisX), normalize180(rightAxisY), normalize180(rightYaw),
                normalize180(rightHatX), normalize180(rightHatY),
                xyz);
    }

    private void setState(DPadMapper newState) {
        state = newState;
    }

    public void setLeftAxisX(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(normalize180(v), o.leftAxisY, o.leftYaw, o.leftHatX, o.leftHatY,
                o.rightAxisX, o.rightAxisY, o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setLeftAxisY(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, normalize180(v), o.leftYaw, o.leftHatX, o.leftHatY,
                o.rightAxisX, o.rightAxisY, o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setLeftYaw(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, normalize180(v), o.leftHatX, o.leftHatY,
                o.rightAxisX, o.rightAxisY, o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setLeftHatX(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, normalize180(v), o.leftHatY,
                o.rightAxisX, o.rightAxisY, o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setLeftHatY(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, o.leftHatX, normalize180(v),
                o.rightAxisX, o.rightAxisY, o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setRightAxisX(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, o.leftHatX, o.leftHatY,
                normalize180(v), o.rightAxisY, o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setRightAxisY(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, o.leftHatX, o.leftHatY,
                o.rightAxisX, normalize180(v), o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setRightYaw(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, o.leftHatX, o.leftHatY,
                o.rightAxisX, o.rightAxisY, normalize180(v), o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setRightHatX(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, o.leftHatX, o.leftHatY,
                o.rightAxisX, o.rightAxisY, o.rightYaw, normalize180(v), o.rightHatY, o.xyz));
    }

    public void setRightHatY(double v) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, o.leftHatX, o.leftHatY,
                o.rightAxisX, o.rightAxisY, o.rightYaw, o.rightHatX, normalize180(v), o.xyz));
    }

    public void setXYZ(double[] newXYZ) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, o.leftHatX, o.leftHatY,
                o.rightAxisX, o.rightAxisY, o.rightYaw, o.rightHatX, o.rightHatY, newXYZ));
    }

    public void setLeftAxis(double x, double y) {
        DPadMapper o = state;
        setState(new DPadMapper(normalize180(x), normalize180(y), o.leftYaw, o.leftHatX, o.leftHatY,
                o.rightAxisX, o.rightAxisY, o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setRightAxis(double x, double y) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, o.leftHatX, o.leftHatY,
                normalize180(x), normalize180(y), o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setLeftHat(double x, double y) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, x, y,
                o.rightAxisX, o.rightAxisY, o.rightYaw, o.rightHatX, o.rightHatY, o.xyz));
    }

    public void setRightHat(double x, double y) {
        DPadMapper o = state;
        setState(new DPadMapper(o.leftAxisX, o.leftAxisY, o.leftYaw, o.leftHatX, o.leftHatY,
                o.rightAxisX, o.rightAxisY, o.rightYaw, x, y, o.xyz));
    }

    public void setAllXYZ(double x, double y, double z) {
        setXYZ(new double[]{x, y, z});
    }

    public void setX(double x) {
        DPadMapper o = state;
        setXYZ(new double[]{x, o.xyz[1], o.xyz[2]});
    }

    public void setY(double y) {
        DPadMapper o = state;
        setXYZ(new double[]{o.xyz[0], y, o.xyz[2]});
    }

    public void setZ(double z) {
        DPadMapper o = state;
        setXYZ(new double[]{o.xyz[0], o.xyz[1], z});
    }
    public double getLeftAxisX() { return state.leftAxisX; }
    public double getLeftAxisY() { return state.leftAxisY; }
    public double getLeftYaw()   { return state.leftYaw;   }
    public double getLeftHatX()  { return state.leftHatX;  }
    public double getLeftHatY()  { return state.leftHatY;  }

    public double getRightAxisX() { return state.rightAxisX; }
    public double getRightAxisY() { return state.rightAxisY; }
    public double getRightYaw()   { return state.rightYaw;   }
    public double getRightHatX()  { return state.rightHatX;  }
    public double getRightHatY()  { return state.rightHatY;  }

    public double[] getXYZ() { return state.xyz.clone(); }

    public double getX() { return state.xyz[0]; }
    public double getY() { return state.xyz[1]; }
    public double getZ() { return state.xyz[2]; }

    public double[] getLeftAxis()  { return new double[]{ state.leftAxisX,  state.leftAxisY  }; }
    public double[] getRightAxis() { return new double[]{ state.rightAxisX, state.rightAxisY }; }
    public double[] getLeftHat()   { return new double[]{ state.leftHatX,   state.leftHatY   }; }
    public double[] getRightHat()  { return new double[]{ state.rightHatX,  state.rightHatY  }; }

    public void reset() {
        setState(new DPadMapper(
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                new double[]{0, 0, 0}
        ));
    }

    public double normalize180(double a) {
        a = a % 360.0;
        if (a > 180) a -= 360;
        if (a < -180) a += 360;
        return a;
    }


}
