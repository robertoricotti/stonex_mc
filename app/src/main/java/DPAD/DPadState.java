package DPAD;

import androidx.annotation.NonNull;

public final class DPadState {

    public static final int SIDE_UNKNOWN = 0;
    public static final int SIDE_LEFT = 1;
    public static final int SIDE_RIGHT = 2;

    public static final int DEVICE_UNINITIALIZED = Integer.MIN_VALUE;
    public int deviceId = DEVICE_UNINITIALIZED;
    public String deviceName = "";
    public int vendorId = 0;
    public int productId = 0;
    public String descriptor = "";

    public float roll = 0f;
    public float pitch = 0f;
    public float yaw = 0f;
    public float throttle = 0f;
    public float hatX = 0f;
    public float hatY = 0f;

    public int lastButtonDown = 0;
    public int lastButtonUp = 0;

    public int side = SIDE_UNKNOWN;
    public boolean connected = false;


    @NonNull
    public String sideName() {
        switch (side) {
            case SIDE_LEFT:  return "LEFT";
            case SIDE_RIGHT: return "RIGHT";
            default:         return "UNKNOWN";
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "DPadState{" +
                "deviceId=" + deviceId +
                ", deviceName='" + deviceName + '\'' +
                ", vendorId=" + vendorId +
                ", productId=" + productId +
                ", descriptor='" + descriptor + '\'' +
                ", roll=" + roll +
                ", pitch=" + pitch +
                ", yaw=" + yaw +
                ", throttle=" + throttle +
                ", hatX=" + hatX +
                ", hatY=" + hatY +
                ", lastButtonDown=" + lastButtonDown +
                ", lastButtonUp=" + lastButtonUp +
                ", side=" + sideName() +
                ", connected=" + connected +
                '}';
    }
}
