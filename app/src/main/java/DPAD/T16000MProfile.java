package DPAD;

import android.view.KeyEvent;

public class T16000MProfile implements DPadProfile {

    private float globalDeadband = 0.05f; // Deadband globale del 5% (valore predefinito)

    @Override public String name() { return "Thrustmaster T.16000M"; }

    @Override public float roll(AxisData data) {
        return applyDeadband(AxisUtil.centered(data, Axis.AXIS_X));
    }

    @Override public float pitch(AxisData data) {
        // spesso avanti è negativo: inverti se ti serve
        return applyDeadband(AxisUtil.centered(data, Axis.AXIS_Y));
    }

    @Override public float yaw(AxisData data) {
        return applyDeadband(AxisUtil.centered(data, Axis.AXIS_RZ));
    }

    @Override public float throttle(AxisData data) {
        // spesso 0..1 oppure -1..1 a seconda del device
        // Per il throttle potresti non voler applicare la deadband
        return AxisUtil.raw(data, Axis.AXIS_THROTTLE);
    }

    @Override public float hatX(AxisData data) {
        // Per l'hat di solito non si applica deadband (è digitale)
        return AxisUtil.raw(data, Axis.AXIS_HAT_X);
    }

    @Override public float hatY(AxisData data) {
        // Per l'hat di solito non si applica deadband (è digitale)
        return AxisUtil.raw(data, Axis.AXIS_HAT_Y);
    }

    @Override public boolean isTrigger(int keyCode) {
        // non è garantito: logga e adatta se necessario
        return keyCode == KeyEvent.KEYCODE_BUTTON_1;
    }

    @Override public boolean isButton(int keyCode) {
        // copre molti controller: puoi ampliare
        return (keyCode >= KeyEvent.KEYCODE_BUTTON_1 && keyCode <= KeyEvent.KEYCODE_BUTTON_16)
                || keyCode == KeyEvent.KEYCODE_BUTTON_A
                || keyCode == KeyEvent.KEYCODE_BUTTON_B
                || keyCode == KeyEvent.KEYCODE_BUTTON_X
                || keyCode == KeyEvent.KEYCODE_BUTTON_Y;
    }

    /**
     * Applica la deadband globale a un valore normalizzato (-1 a 1)
     */
    private float applyDeadband(float value) {
        if (Math.abs(value) <= globalDeadband) {
            return 0f;
        }

        // Normalizzazione per mantenere la linearità dopo la deadband
        // Scala il valore per compensare l'area "morta"
        float sign = Math.signum(value);
        float normalized = (Math.abs(value) - globalDeadband) / (1f - globalDeadband);
        return sign * normalized;
    }

    /**
     * Imposta la deadband globale (0.0 a 1.0)
     */
    public void setGlobalDeadband(float deadband) {
        if (deadband < 0f) {
            this.globalDeadband = 0f;
        } else if (deadband > 0.5f) {
            this.globalDeadband = 0.5f; // Limite massimo ragionevole
        } else {
            this.globalDeadband = deadband;
        }
    }

    /**
     * Ottiene la deadband globale corrente
     */
    public float getGlobalDeadband() {
        return globalDeadband;
    }


    public static class AxisData {
        private final float[] axisValues;
        private final float[] axisFlats;

        public AxisData(float[] axisValues, float[] axisFlats) {
            this.axisValues = axisValues;
            this.axisFlats = axisFlats;
        }

        public float getAxisValue(int axis) {
            if (axisValues == null || axis < 0 || axis >= axisValues.length) {
                return 0f;
            }
            return axisValues[axis];
        }

        public float getAxisFlat(int axis) {
            if (axisFlats == null || axis < 0 || axis >= axisFlats.length) {
                return 0f;
            }
            return axisFlats[axis];
        }

        /**
         * Crea un AxisData con valori specifici per ogni asse
         */
        public static AxisData fromValues(float roll, float pitch, float yaw,
                                          float throttle, float hatX, float hatY) {
            float[] values = {roll, pitch, yaw, throttle, hatX, hatY};
            float[] flats = new float[6]; // Tutti 0
            return new AxisData(values, flats);
        }
    }

    /**
     * Enumerazione per gli assi del controller
     */
    public static class Axis {
        public static final int AXIS_X = 0;
        public static final int AXIS_Y = 1;
        public static final int AXIS_RZ = 2;
        public static final int AXIS_THROTTLE = 3;
        public static final int AXIS_HAT_X = 4;
        public static final int AXIS_HAT_Y = 5;

        // Puoi estendere con altri assi se necessario
    }

    /**
     * Utility interna per deadzone e lettura assi.
     */
    static class AxisUtil {
        static float centered(AxisData data, int axis) {
            if (data == null) return 0f;
            float v = data.getAxisValue(axis);
            float flat = data.getAxisFlat(axis);

            // Combina deadband hardware (flat) e deadband minima
            float effectiveDeadband = Math.max(flat, 0.01f); // Almeno 1%

            return (Math.abs(v) > effectiveDeadband) ? v : 0f;
        }

        static float raw(AxisData data, int axis) {
            if (data == null) return 0f;
            return data.getAxisValue(axis);
        }
    }
}