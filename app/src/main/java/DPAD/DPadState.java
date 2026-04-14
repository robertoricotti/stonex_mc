package DPAD;

public class DPadState {
    public float roll = 0f;
    public float pitch = 0f;
    public float yaw = 0f;
    public float throttle = 0f;
    public float hatX = 0f;
    public float hatY = 0f;
    public long timestamp = 0;

    public DPadState() {
    }

    public DPadState(float roll, float pitch, float yaw, float throttle, float hatX, float hatY, long timestamp) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
        this.throttle = throttle;
        this.hatX = hatX;
        this.hatY = hatY;
        this.timestamp = timestamp;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getThrottle() {
        return throttle;
    }

    public void setThrottle(float throttle) {
        this.throttle = throttle;
    }

    public float getHatX() {
        return hatX;
    }

    public void setHatX(float hatX) {
        this.hatX = hatX;
    }

    public float getHatY() {
        return hatY;
    }

    public void setHatY(float hatY) {
        this.hatY = hatY;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
