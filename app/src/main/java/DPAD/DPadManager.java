package DPAD;

import android.content.Context;
import android.hardware.input.InputManager;
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
        if (!isDPad(device)) {
            return false;
        }

        final T16000MProfile.AxisData axisData = MotionEventAdapter.createAxisData(event);
        if (axisData == null) {
            return false;
        }

        DPadState state = createDPadState(axisData);

        if (listener == null) {
            return true;
        }

        if (state.getThrottle() < 0.5) {
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
        if (!isDPad(device)) return false;

        if (listener != null) {
            listener.onButtonDown(keyCode);
        }
        return true;
    }

    /** Da chiamare da Activity.onKeyUp(...) */
    public boolean handleKeyUp(int keyCode, KeyEvent event) {
        if (event == null) return false;

        final InputDevice device = event.getDevice();
        if (!isDPad(device)) return false;

        if (listener != null) {
            listener.onButtonUp(keyCode);
        }
        return true;
    }

    public void release() {
        stop();
        listener = null;
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

    private static boolean isDPad(InputDevice device) {
        if (device == null) return false;

        int sources = device.getSources();
        return (sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD
                && device.isVirtual();
    }

    private final InputManager.InputDeviceListener deviceListener =
            new InputManager.InputDeviceListener() {
                @Override
                public void onInputDeviceAdded(int deviceId) {
                    // Logica quando un dispositivo viene aggiunto
                }

                @Override
                public void onInputDeviceRemoved(int deviceId) {
                    // Logica quando un dispositivo viene rimosso
                    if (listener != null) {
                        listener.onStateDisconnected();
                    }

                }

                @Override
                public void onInputDeviceChanged(int deviceId) {
                    // Logica quando un dispositivo cambia stato
                }
            };
}