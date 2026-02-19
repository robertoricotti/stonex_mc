package DPAD;

import android.content.Context;
import android.hardware.input.InputManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class DPadManager {

    public interface Listener {
        void onStateDisconnected();
        void onStateLeftUpdated(DPadState state);
        void onStateRightUpdated(DPadState state);
        void onButtonDown(int keyCode);
        void onButtonUp(int keyCode);
    }

    private static final String TAG = "DPadManager";

    // Slot stabili per il routing su device fisici
    private static final int INVALID_ID = -1;
    private int leftDeviceId = INVALID_ID;
    private int rightDeviceId = INVALID_ID;

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

    public boolean handleMotionEvent(MotionEvent event) {
        if (event == null || event.getActionMasked() != MotionEvent.ACTION_MOVE) {
            return false;
        }

        final InputDevice device = event.getDevice();
        final int source = event.getSource();
        if (!isControllerMotion(device)) return false;

        final T16000MProfile.AxisData axisData = MotionEventAdapter.createAxisData(event);
        if (axisData == null) {
            return false;
        }

        final DPadState state = createDPadState(axisData);

        if (listener == null) {
            // Evento gestito, ma nessun listener registrato
            return true;
        }

        final int side = resolveSide(device, state);
        if (side == Side.LEFT) {
            listener.onStateLeftUpdated(state);
        } else {
            listener.onStateRightUpdated(state);
        }

        return true;
    }

    /** Da chiamare da Activity.onKeyDown(...) */
    public boolean handleKeyDown(int keyCode, KeyEvent event) {
        if (event == null) return false;

        final InputDevice device = event.getDevice();
        final int source = event.getSource();
        if (!isControllerDevice(device)) return false;


        if (listener != null) {
            listener.onButtonDown(keyCode);
        }
        return true;
    }

    /** Da chiamare da Activity.onKeyUp(...) */
    public boolean handleKeyUp(int keyCode, KeyEvent event) {
        if (event == null) return false;

        final InputDevice device = event.getDevice();
        final int source = event.getSource();
        if (!isControllerDevice(device)) return false;


        if (listener != null) {
            listener.onButtonUp(keyCode);
        }
        return true;
    }

    public void release() {
        stop();
        listener = null;
        leftDeviceId = INVALID_ID;
        rightDeviceId = INVALID_ID;
    }

    // ---------------- Metodi privati ----------------

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
     * Riconoscimento device:
     * - NON richiede più device.isVirtual()
     * - accetta joystick/gamepad/dpad purché abbia almeno AXIS_X e AXIS_Y
     */
    private static boolean isControllerDevice(InputDevice device) {
        if (device == null) return false;
        final int sources = device.getSources();
        return ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
                || ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                || ((sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD);
    }

    private static boolean isControllerMotion(InputDevice device) {
        if (!isControllerDevice(device)) return false;

        // Per motion usa SOURCE_JOYSTICK come sorgente “vera” degli assi
        final InputDevice.MotionRange x = device.getMotionRange(MotionEvent.AXIS_X, InputDevice.SOURCE_JOYSTICK);
        final InputDevice.MotionRange y = device.getMotionRange(MotionEvent.AXIS_Y, InputDevice.SOURCE_JOYSTICK);

        // Fallback: alcuni device espongono assi anche su SOURCE_GAMEPAD
        final InputDevice.MotionRange x2 = (x != null) ? x : device.getMotionRange(MotionEvent.AXIS_X, InputDevice.SOURCE_GAMEPAD);
        final InputDevice.MotionRange y2 = (y != null) ? y : device.getMotionRange(MotionEvent.AXIS_Y, InputDevice.SOURCE_GAMEPAD);

        return x2 != null && y2 != null;
    }


    /**
     * Routing:
     * - Virtual: mantiene workaround storico basato sul throttle.
     * - Fisico: assegna in modo stabile (primo device = LEFT, secondo = RIGHT).
     *   Se i due slot sono già occupati, fallback sul throttle senza riassegnare.
     */
    private int resolveSide(InputDevice device, DPadState state) {
        final int id = device.getId();

        if (id == leftDeviceId) return Side.LEFT;
        if (id == rightDeviceId) return Side.RIGHT;

        if (device.isVirtual()) {
            final int side = (state != null && state.getThrottle() < 0.5) ? Side.LEFT : Side.RIGHT;

            // opzionale: blocca l'id allo slot appena possibile, per stabilizzare anche i virtual
            if (side == Side.LEFT && leftDeviceId == INVALID_ID) leftDeviceId = id;
            if (side == Side.RIGHT && rightDeviceId == INVALID_ID) rightDeviceId = id;

            return side;
        }

        // Device fisici: assegnazione stabile per id
        if (leftDeviceId == INVALID_ID) {
            leftDeviceId = id;
            Log.d(TAG, "Assigned device " + id + " to LEFT: " + safeName(device));
            return Side.LEFT;
        }
        if (rightDeviceId == INVALID_ID) {
            rightDeviceId = id;
            Log.d(TAG, "Assigned device " + id + " to RIGHT: " + safeName(device));
            return Side.RIGHT;
        }

        // Troppi device: fallback
        return (state != null && state.getThrottle() < 0.5) ? Side.LEFT : Side.RIGHT;
    }

    private static String safeName(InputDevice device) {
        try {
            return device.getName();
        } catch (Throwable t) {
            return "unknown";
        }
    }

    private static final class Side {
        private static final int LEFT = 0;
        private static final int RIGHT = 1;
        private Side() {}
    }

    private final InputManager.InputDeviceListener deviceListener =
            new InputManager.InputDeviceListener() {
                @Override
                public void onInputDeviceAdded(int deviceId) {
                    // opzionale: qui potresti loggare o inizializzare qualcosa
                    // Log.d(TAG, "Device added: " + deviceId);
                }

                @Override
                public void onInputDeviceRemoved(int deviceId) {
                    // Libera lo slot se stacchi uno dei due device assegnati
                    boolean wasAssigned = false;
                    if (deviceId == leftDeviceId) {
                        leftDeviceId = INVALID_ID;
                        wasAssigned = true;
                    }
                    if (deviceId == rightDeviceId) {
                        rightDeviceId = INVALID_ID;
                        wasAssigned = true;
                    }

                    if (wasAssigned) {
                        Log.d(TAG, "Device removed (was assigned): " + deviceId);
                    } else {
                        Log.d(TAG, "Device removed: " + deviceId);
                    }

                    if (listener != null) {
                        listener.onStateDisconnected();
                    }
                }

                @Override
                public void onInputDeviceChanged(int deviceId) {
                    // opzionale: se cambia e vuoi ricalcolare, puoi anche invalidare gli slot
                    // Log.d(TAG, "Device changed: " + deviceId);
                }
            };
}
