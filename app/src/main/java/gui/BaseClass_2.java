package gui;

import static gui.MyApp.hAlarm;
import static gui.MyApp.isApollo;
import static gui.MyApp.isOffgrid;

import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import utils.MyDeviceManager;

public class BaseClass_2 extends AppCompatActivity {

    private static final String TAG_KEY = "JOY_KEY";
    private static final String TAG_AXIS = "JOY_AXIS";
    private static final String TAG_DEV = "JOY_DEVICE";

    boolean isPressed, isTouched;

    /* =========================================================
       LIFECYCLE
       ========================================================= */

    @Override
    protected void onResume() {
        super.onResume();

        // 🔒 Cattura il puntatore (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View v = getWindow().getDecorView();
            v.requestPointerCapture();
        }
    }


    /* -----------------------------
       PULSANTI (TASTI + JOYSTICK)
       -------------------------------------------------------- */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        int source = event.getSource();

        /* ----------BOTTONI JOYSTICK ---------- */
        if ((source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {

            InputDevice device = event.getDevice();
            Log.d(TAG_KEY,
                    "JOY DOWN | dev=" + device.getName() +
                            " id=" + device.getId() +
                            " key=" + KeyEvent.keyCodeToString(keyCode));

            //  Qui dovresti poter mappare pulsanti escavatore
            // es: KEYCODE_BUTTON_A, B, X, Y, L1, R1...

            return true; // BLOCCA la propagaziome
        }

        /* ---------- TASTI FISICI ---------- */
        if (keyCode == KeyEvent.KEYCODE_F1) {

            if (!isTouched) {
                isTouched = true;

                if (hAlarm && isPressed) {
                    isPressed = false;
                }

                if ((hAlarm || isOffgrid) && isApollo && !isPressed) {
                    MyDeviceManager.OUT1(MyApp.visibleActivity, 0);
                    MyDeviceManager.OUT2(MyApp.visibleActivity, 0);
                    isPressed = true;
                    new Handler().postDelayed(this::SafetyDelay, 10000);
                }
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        int source = event.getSource();

        /* ---------- JOYSTICK BUTTON ---------- */
        if ((source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
            Log.d(TAG_KEY, "JOY UP | key=" + KeyEvent.keyCodeToString(keyCode));
            //TODO se non loggabile prova a stampare da qui
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_F1) {
            isTouched = false;
        }

        return super.onKeyUp(keyCode, event);
    }

    /* ---------------------------------------------------------------------
       ASSI ANALOG (LEVE)
       -------------------------------------------------------------------- */

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            InputDevice device = event.getDevice();

            float x  = event.getAxisValue(MotionEvent.AXIS_X);
            float y  = event.getAxisValue(MotionEvent.AXIS_Y);
            float z  = event.getAxisValue(MotionEvent.AXIS_Z);
            float rz = event.getAxisValue(MotionEvent.AXIS_RZ);
            float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
            float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

            Log.d(TAG_AXIS, "JOY MOVE | dev=" + device.getName() +
                            " X=" + x +
                            " Y=" + y +
                            " Z=" + z +
                            " RZ=" + rz +
                            " HAT_X=" + hatX +
                            " HAT_Y=" + hatY);

            //  Qui dovresti avere gli eventi


            return true; // niente cursore
        }

        return super.onGenericMotionEvent(event);
    }

    /* -----------------------------------------------------------------------------------
       DEBUG: ASSI DISPONIBILI (una volta)
       ---------------------------------------------- */

    protected void logJoystickInfo(InputDevice device) {

        Log.d(TAG_DEV, "====== JOYSTICK DETECTED ======");
        Log.d(TAG_DEV, "Name: " + device.getName());
        Log.d(TAG_DEV, "VendorId: " + device.getVendorId());
        Log.d(TAG_DEV, "ProductId: " + device.getProductId());

        for (InputDevice.MotionRange range : device.getMotionRanges()) {
            Log.d(TAG_DEV,
                    "Axis: " + MotionEvent.axisToString(range.getAxis()) +
                            " min=" + range.getMin() +
                            " max=" + range.getMax() +
                            " flat=" + range.getFlat());
        }
    }

    /* -----------------------------------------------------------
       SAFETY PER ALLARME ALTEZZA SICUREZZA SEGGIOLINO
       ----------------------------------------------------------- */

    private void SafetyDelay() {
        if (hAlarm && isApollo) {
            MyDeviceManager.OUT1(MyApp.visibleActivity, 1);
            isPressed = false;
        }

        if (isOffgrid && isApollo) {
            MyDeviceManager.OUT2(MyApp.visibleActivity, 1);
            isPressed = false;
        }
    }
}
