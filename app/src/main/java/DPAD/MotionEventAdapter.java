package DPAD;

import android.view.InputDevice;
import android.view.MotionEvent;

public class MotionEventAdapter {

    public static T16000MProfile.AxisData createAxisData(MotionEvent event) {
        if (event == null || event.getDevice() == null) {
            return null;
        }

        // Definire quanti assi vogliamo supportare
        int axisCount = 6; // X, Y, RZ, THROTTLE, HAT_X, HAT_Y
        float[] axisValues = new float[axisCount];
        float[] axisFlats = new float[axisCount];

        InputDevice device = event.getDevice();
        int source = event.getSource();

        // Mappatura degli assi Android agli assi interni
        int[] androidAxes = {
                MotionEvent.AXIS_X,
                MotionEvent.AXIS_Y,
                MotionEvent.AXIS_RZ,
                MotionEvent.AXIS_THROTTLE,
                MotionEvent.AXIS_HAT_X,
                MotionEvent.AXIS_HAT_Y
        };

        for (int i = 0; i < androidAxes.length; i++) {
            axisValues[i] = event.getAxisValue(androidAxes[i]);

            InputDevice.MotionRange range = device.getMotionRange(androidAxes[i], source);
            if (range != null) {
                axisFlats[i] = range.getFlat();
            } else {
                axisFlats[i] = 0f;
            }
        }

        return new T16000MProfile.AxisData(axisValues, axisFlats);
    }
}