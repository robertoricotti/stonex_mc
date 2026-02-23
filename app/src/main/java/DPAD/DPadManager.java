package DPAD;

import android.content.Context;
import android.hardware.input.InputManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * DPadManager "definitivo":
 * - Supporta device FISICI (JOYSTICK/GAMEPAD) e device VIRTUALI (DPAD / overlay)
 * - Routing LEFT/RIGHT SEMPRE basato su throttle (<0.5 = LEFT, >=0.5 = RIGHT)
 * - Non richiede device.isVirtual() per filtrare
 * - Accetta MotionEvent anche se l'evento arriva con source diverso (joystick/gamepad/dpad)
 * - Gestisce anche hat/dpad via assi (AXIS_HAT_X / AXIS_HAT_Y) se MotionEventAdapter li mappa
 *
 * NOTE IMPORTANTI:
 * - Se su un controller fisico il throttle non esiste o viene mappato su un asse diverso,
 *   il routing LEFT/RIGHT dipenderà da T16000MProfile.throttle(axisData).
 *   In tal caso aggiungi mapping fallback nel profilo (AXIS_Z/RZ/GAS/BRAKE ecc.).
 */
public class DPadManager {

    public interface Listener {
        void onStateDisconnected();
        void onStateLeftUpdated(DPadState state);
        void onStateRightUpdated(DPadState state);
        void onButtonDown(int keyCode);
        void onButtonUp(int keyCode);
    }

    private static final String TAG = "DPadManager";

    private final InputManager inputManager;
    private final DPadProfile profile;
    private Listener listener;
    private volatile boolean isStarted = false;

    public DPadManager(Context ctx, DPadProfile profile) {
        this.inputManager = (InputManager) ctx.getSystemService(Context.INPUT_SERVICE);
        this.profile = profile;
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void start() {
        if (isStarted) return;
        inputManager.registerInputDeviceListener(deviceListener, null);
        isStarted = true;
    }

    public void stop() {
        if (!isStarted) return;
        inputManager.unregisterInputDeviceListener(deviceListener);
        isStarted = false;
    }

    /**
     * Da chiamare da Activity.dispatchGenericMotionEvent(...)
     */
    public boolean handleMotionEvent(MotionEvent event) {
        if (event == null) return false;

        final int action = event.getActionMasked();
        if (action != MotionEvent.ACTION_MOVE && action != MotionEvent.ACTION_HOVER_MOVE) {
            return false;
        }

        final InputDevice device = event.getDevice();
        if (!isControllerDevice(device)) return false;

        // Verifica anche la sorgente dell'EVENTO (non solo del device)
        final int src = event.getSource();
        if (!isControllerSource(src)) return false;

        final T16000MProfile.AxisData axisData = MotionEventAdapter.createAxisData(event);
        if (axisData == null) return false;

        final DPadState state = createDPadState(axisData);

        final Listener l = listener;
        if (l == null) return true;

        // Routing richiesto: SOLO throttle
        if (state.getThrottle() < 0.5) {
            l.onStateLeftUpdated(state);
        } else {
            l.onStateRightUpdated(state);
        }

        return true;
    }

    public boolean handleKeyDown(int keyCode, KeyEvent event) {
        if (event == null) return false;

        final InputDevice device = event.getDevice();
        if (!isControllerDevice(device)) return false;

        // KeyEvent: molti controller riportano SOURCE_KEYBOARD -> non bloccare qui
        if (!isControllerKeyEvent(event, device)) return false;

        final Listener l = listener;
        if (l != null) l.onButtonDown(keyCode);
        return true;
    }

    public boolean handleKeyUp(int keyCode, KeyEvent event) {
        if (event == null) return false;

        final InputDevice device = event.getDevice();
        if (!isControllerDevice(device)) return false;

        if (!isControllerKeyEvent(event, device)) return false;

        final Listener l = listener;
        if (l != null) l.onButtonUp(keyCode);
        return true;
    }

    /** KeyEvent: source può essere KEYBOARD anche per gamepad. */
    private static boolean isControllerKeyEvent(KeyEvent event, InputDevice device) {
        // 1) se Android lo marca correttamente, ok
        if (event.isFromSource(InputDevice.SOURCE_GAMEPAD)
                || event.isFromSource(InputDevice.SOURCE_JOYSTICK)
                || event.isFromSource(InputDevice.SOURCE_DPAD)) {
            return true;
        }

        // 2) fallback: se il DEVICE è un controller, accetta comunque
        // (molti dongle/driver fanno passare i pulsanti come KEYBOARD)
        if (device != null) {
            int ds = device.getSources();
            if (((ds & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((ds & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
                    || ((ds & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD)) {
                return true;
            }
        }

        return false;
    }

    public void release() {
        stop();
        listener = null;
    }

    // ---------------- Privati ----------------

    private DPadState createDPadState(T16000MProfile.AxisData axisData) {
        return new DPadState(
                profile.roll(axisData),
                profile.pitch(axisData),
                profile.yaw(axisData),
                profile.throttle(axisData),
                profile.hatX(axisData),
                profile.hatY(axisData),
                System.currentTimeMillis()
        );
    }

    /**
     * Riconosce un dispositivo come "controller" se ha sorgenti tipiche.
     * Non filtra per isVirtual(): deve funzionare per entrambi.
     */
    private static boolean isControllerDevice(InputDevice device) {
        if (device == null) return false;
        final int sources = device.getSources();

        return ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
                || ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                || ((sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD);
    }

    /**
     * Riconosce se la sorgente dell'evento è compatibile.
     * Alcuni overlay/virtuali arrivano come DPAD, altri come GAMEPAD/JOYSTICK.
     */
    private static boolean isControllerSource(int src) {
        return ((src & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
                || ((src & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                || ((src & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD);
    }

    private static String safeName(InputDevice device) {
        try {
            return device != null ? device.getName() : "null";
        } catch (Throwable t) {
            return "unknown";
        }
    }

    // ---------------- Input device listener ----------------

    private final InputManager.InputDeviceListener deviceListener =
            new InputManager.InputDeviceListener() {
                @Override
                public void onInputDeviceAdded(int deviceId) {
                    // opzionale debug
                    InputDevice dev = InputDevice.getDevice(deviceId);
                    Log.d(TAG, "Device added: " + deviceId + " name=" + safeName(dev)
                            + " virtual=" + (dev != null && dev.isVirtual())
                            + " sources=" + (dev != null ? dev.getSources() : 0));
                }

                @Override
                public void onInputDeviceRemoved(int deviceId) {
                    Log.d(TAG, "Device removed: " + deviceId);
                    final Listener l = listener;
                    if (l != null) l.onStateDisconnected();
                }

                @Override
                public void onInputDeviceChanged(int deviceId) {
                    // opzionale debug
                    InputDevice dev = InputDevice.getDevice(deviceId);
                    Log.d(TAG, "Device changed: " + deviceId + " name=" + safeName(dev)
                            + " virtual=" + (dev != null && dev.isVirtual())
                            + " sources=" + (dev != null ? dev.getSources() : 0));
                }
            };
}