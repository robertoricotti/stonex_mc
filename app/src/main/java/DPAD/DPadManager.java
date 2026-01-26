package DPAD;

import android.content.Context;
import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DPadManager {
    public interface Listener {
        void onDPadConnected(DPadState state);
        void onDPadDisconnected(int deviceId);
        void onStateUpdated(DPadState state);
        void onButtonDown(DPadState state, int keyCode);
        void onButtonUp(DPadState state, int keyCode);
    }
    private static final String TAG = "ControllerManager";

    public static final String DPAD_NAME = "Thrustmaster T.16000M";
    private final InputManager inputManager;
    private final DPadProfile profile;
    private Listener listener;

    private final Map<Integer, DPadState> states = new HashMap<>();

    public DPadManager(Context ctx, DPadProfile profile) {
        this.inputManager = (InputManager) ctx.getSystemService(Context.INPUT_SERVICE);
        this.profile = profile;
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void start() {
        for (InputDevice d : listDPads()) {
            connect(d);
        }
        inputManager.registerInputDeviceListener(deviceListener, null);
    }
    public void stop() {
        inputManager.unregisterInputDeviceListener(deviceListener);
        states.clear();
    }

    public DPadState getStateBySide(T16000MProfile.AxisData axisData) {
        final float t = profile.throttle(axisData);
        final int wantedSide = (t < 0.5f) ? DPadState.SIDE_LEFT : DPadState.SIDE_RIGHT;

        // Prima cerca uno stato con il lato già assegnato
        for (DPadState s : states.values()) {
            if (s.side == wantedSide) return s;
        }

        // Se non trovato, cerca uno stato con side ancora UNKNOWN e assegnagli il lato
        for (DPadState s : states.values()) {
            if (s.side == DPadState.SIDE_UNKNOWN) {
                s.side = wantedSide;
                return s;
            }
        }

        // Se ci sono più stati con side UNKNOWN, prendi il primo
        // (questo potrebbe essere raffinato se necessario)
        if (!states.isEmpty()) {
            DPadState firstState = states.values().iterator().next();
            if (firstState.side == DPadState.SIDE_UNKNOWN) {
                firstState.side = wantedSide;
            }
            return firstState;
        }

        return null;
    }


    public boolean handleMotionEvent(MotionEvent event) {

        if (event == null) return false;

        if (event.getActionMasked() != MotionEvent.ACTION_MOVE) return false;

        final InputDevice device = event.getDevice();
        if (device == null || !isDPad(device)) return false;

        final T16000MProfile.AxisData axisData = MotionEventAdapter.createAxisData(event);
        if (axisData == null) return false;

        final DPadState s = getStateBySide(axisData);
        if (s == null) return false;

        // Aggiorna stato
        s.roll     = profile.roll(axisData);
        s.pitch    = profile.pitch(axisData);
        s.yaw      = profile.yaw(axisData);
        s.throttle = profile.throttle(axisData);
        s.hatX     = profile.hatX(axisData);
        s.hatY     = profile.hatY(axisData);
        s.side = s.throttle < 0.5 ? DPadState.SIDE_LEFT : DPadState.SIDE_RIGHT;

        if (listener != null) listener.onStateUpdated(s);

        return true;

    }

    /** Da chiamare da Activity.onKeyDown(...) */
    public boolean handleKeyDown(int keyCode, KeyEvent event) {
        InputDevice dev = event.getDevice();

        if (dev == null) return false;
        if (!isDPad(dev)) return false;

        DPadState s = states.get(event.getDeviceId());
        if (s == null) return false;

        s.lastButtonDown = keyCode;
        if (listener != null) listener.onButtonDown(s, keyCode);
        return true;
    }

    /** Da chiamare da Activity.onKeyUp(...) */
    public boolean handleKeyUp(int keyCode, KeyEvent event) {
        InputDevice dev = event.getDevice();
        if (dev == null) return false;
        if (!isDPad(dev)) return false;

        DPadState s = states.get(event.getDeviceId());
        if (s == null) return false;

        s.lastButtonUp = keyCode;
        if (listener != null) listener.onButtonUp(s, keyCode);
        return true;
    }

    // ---------------- internals ----------------

    private final InputManager.InputDeviceListener deviceListener = new InputManager.InputDeviceListener() {
        @Override public void onInputDeviceAdded(int deviceId) {
            InputDevice d = InputDevice.getDevice(deviceId);
            if (d != null && isDPad(d)) connect(d);
        }

        @Override public void onInputDeviceRemoved(int deviceId) {
            if (states.containsKey(deviceId)) {
                states.remove(deviceId);
                if (listener != null) listener.onDPadDisconnected(deviceId);
            }
        }

        @Override public void onInputDeviceChanged(int deviceId) {
            // opzionale: aggiorna info device
            InputDevice d = InputDevice.getDevice(deviceId);
            DPadState s = states.get(deviceId);
            if (d != null && s != null) fillDeviceInfo(s, d);
        }
    };

    private void connect(InputDevice d) {
        int id = d.getId();

        if (states.containsKey(id)) return; // già connesso

        DPadState s = new DPadState();
        fillDeviceInfo(s, d);

        s.connected = true;
        states.put(id, s);

        //Log.i(TAG, "Connected: " + s.deviceName + " id=" + s.deviceId);
        if (listener != null) listener.onDPadConnected(s);
    }


    private void fillDeviceInfo(DPadState s, InputDevice d) {
        s.deviceId = d.getId();
        s.deviceName = d.getName();
        s.vendorId = d.getVendorId();
        s.productId = d.getProductId();
        s.descriptor = d.getDescriptor();

        //Log.d(TAG, "fillDeviceInfo: " + s.toString());
    }

    private static boolean isDPad(InputDevice d) {
        int s = d.getSources();
        //Log.d(TAG, "isController: " + d.getName() + " gamepad=" + gamepad + " joystick=" + joystick + " dpad=" + dpad);
        return ((s & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) && d.isVirtual();
    }


    private List<InputDevice> listDPads() {
        int[] ids = InputDevice.getDeviceIds();
        List<InputDevice> out = new ArrayList<>();
        for (int id : ids) {
            InputDevice d = InputDevice.getDevice(id);
            if (d != null && isDPad(d) && d.isVirtual()) out.add(d);
        }
        return out;
    }
}
