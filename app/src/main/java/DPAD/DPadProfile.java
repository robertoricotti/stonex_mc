package DPAD;

public interface DPadProfile {

    String name();

    // Metodi che ora usano AxisData invece di MotionEvent
    float roll(T16000MProfile.AxisData data);
    float pitch(T16000MProfile.AxisData data);
    float yaw(T16000MProfile.AxisData data);
    float throttle(T16000MProfile.AxisData data);
    float hatX(T16000MProfile.AxisData data);
    float hatY(T16000MProfile.AxisData data);

    // Metodi per i pulsanti rimangono uguali
    boolean isTrigger(int keyCode);
    boolean isButton(int keyCode);

    // Metodi per gestire la deadband globale (opzionali)
    default void setGlobalDeadband(float deadband) {
        // Implementazione di default vuota
    }

    default float getGlobalDeadband() {
        return 0f;
    }
}
