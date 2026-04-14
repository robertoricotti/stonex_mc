package gui;

import static gui.MyApp.hAlarm;
import static gui.MyApp.isApollo;
import static gui.MyApp.isOffgrid;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import DPAD.DPadHelper;
import DPAD.DPadManager;
import DPAD.DPadMapperLeft;
import DPAD.DPadMapperRight;
import DPAD.DPadState;
import DPAD.T16000MProfile;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;
import utils.MyDeviceManager;


public class BaseClass extends AppCompatActivity {


    public static final String TAG = "BaseClass";
    private DPadManager cm;
    private final SparseArray<Runnable> buttonDownActions = new SparseArray<>();
    private final SparseArray<Runnable> buttonUpActions = new SparseArray<>();
    boolean isPressed, isTouched;
    private DPadState left;
    private DPadState right;
    private static final long TICK_MS = 50;
    private final Handler tickHandler = new Handler();
    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            onTickLeft();
            onTickRight();

            tickHandler.postDelayed(this, TICK_MS);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tickHandler.postDelayed(tickRunnable, TICK_MS);


        initBottonDownMap();
        initBottonUpMap();

        cm = new DPadManager(this, new T16000MProfile());

        cm.setListener(new DPadManager.Listener() {


            @Override
            public void onStateDisconnected() {
                left = null;
                right = null;
            }

            @Override
            public void onStateLeftUpdated(DPadState s) {


                Log.d(TAG,
                        "State LEFT: roll=" + s.roll +
                                " pitch=" + s.pitch +
                                " yaw=" + s.yaw +
                                " thr=" + s.throttle +
                                " hatX=" + s.hatX +
                                " hatY=" + s.hatY
                );
                left = s;

            }

            @Override
            public void onStateRightUpdated(DPadState s) {


                Log.d(TAG,
                        "State RIGHT: roll=" + s.roll +
                                " pitch=" + s.pitch +
                                " yaw=" + s.yaw +
                                " thr=" + s.throttle +
                                " hatX=" + s.hatX +
                                " hatY=" + s.hatY
                );
                right = s;

            }

            @Override
            public void onButtonDown(int keyCode) {
                Log.d(TAG, "Button DOWN " + keyCode);
                if (keyCode >= 188 && keyCode <= 203) {
                    Runnable r = buttonDownActions.get(keyCode);
                    if (r != null) r.run();
                    return;
                }
            }

            @Override
            public void onButtonUp(int keyCode) {
                Log.d(TAG, "Button UP " + keyCode);
            }
        });
    }

    @Override
    protected void onResume() {
        if (cm != null) cm.start();
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (cm != null) cm.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cm != null) cm.release();
        tickHandler.removeCallbacks(tickRunnable);

    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        if (cm.handleMotionEvent(ev)) return true;
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (cm.handleKeyDown(event.getKeyCode(), event)) return true;

            if (event.getKeyCode() == KeyEvent.KEYCODE_F1) {
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
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            if (cm.handleKeyUp(event.getKeyCode(), event)) return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void initBottonDownMap() {
        buttonDownActions.put(188, () -> {
            final DPadHelper helper = DPadHelper.getInstance();
            helper.setStep(helper.getStep().next());
        });
        buttonDownActions.put(189, () -> {
        });
        buttonDownActions.put(190, () -> {
            final DPadHelper helper = DPadHelper.getInstance();
            helper.setZ(helper.getZ() - helper.getStep().meters);
        });
        buttonDownActions.put(191, () -> {
            final DPadHelper helper = DPadHelper.getInstance();
            helper.setZ(helper.getZ() + helper.getStep().meters);
        });
        buttonDownActions.put(192, () -> {
        });
        buttonDownActions.put(193, () -> {
        });
        buttonDownActions.put(194, () -> {
        });
        buttonDownActions.put(195, () -> {
        });
        buttonDownActions.put(196, () -> {
        });
        buttonDownActions.put(197, () -> {
        });
        buttonDownActions.put(198, () -> {
        });
        buttonDownActions.put(199, () -> {
        });
        buttonDownActions.put(200, () -> {
        });
        buttonDownActions.put(201, () -> {
        });
        buttonDownActions.put(202, () -> {
        });
        buttonDownActions.put(203, () -> {
        });
    }

    private void initBottonUpMap() {
        buttonUpActions.put(188, () -> {
        });
        buttonUpActions.put(189, () -> {
        });
        buttonUpActions.put(190, () -> {
        });
        buttonUpActions.put(191, () -> {
        });
        buttonUpActions.put(192, () -> {
        });
        buttonUpActions.put(193, () -> {
        });
        buttonUpActions.put(194, () -> {
        });
        buttonUpActions.put(195, () -> {
        });
        buttonUpActions.put(196, () -> {
        });
        buttonUpActions.put(197, () -> {
        });
        buttonUpActions.put(198, () -> {
        });
        buttonUpActions.put(199, () -> {
        });
        buttonUpActions.put(200, () -> {
        });
        buttonUpActions.put(201, () -> {
        });
        buttonUpActions.put(202, () -> {
        });
        buttonUpActions.put(203, () -> {
        });
    }

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

    private void onTickLeft() {
        final DPadState s = left;
        if (s == null) return;


        final DPadHelper helper = DPadHelper.getInstance();

        long timestamp = System.currentTimeMillis() - s.timestamp;

        // Controllo timestamp
        boolean isOldDataY = timestamp > 100 && s.pitch == 0.0;
        boolean isOldDataX = timestamp > 100 && s.roll == 0.0;
        boolean isOldDataYaw = timestamp > 100 && s.yaw == 0.0;
        boolean isOldDataHatY = timestamp > 100 && s.hatY == 0.0;
        boolean isOldDataHatX = timestamp > 100 && s.hatX == 0.0;


        final DPadMapperLeft cur = helper.getLeft();

        // Calcola nuovi valori con controllo su dati vecchi
        double newLeftY = cur.getLeftAxisY() - (isOldDataY ? 0.0 : s.pitch);
        double newLeftX = cur.getLeftAxisX() + (isOldDataX ? 0.0 : s.roll * 0.5);
        double newLeftYaw = cur.getLeftYaw() + (isOldDataYaw ? 0.0 : s.yaw);

        double newLeftHatY = cur.getLeftHatY() + ((isOldDataHatY ? 0.0 : s.hatY) * -0.5);
        double newLeftHatX = cur.getLeftHatX() + ((isOldDataHatX ? 0.0 : s.hatX) * 0.5);

        // XYZ: solo se hat premuto E dati non vecchi
        double[] newXYZ = helper.getXYZ();

        if ((s.hatX != 0.0 || s.hatY != 0.0)) {
            double delta = 0.0;
            if (s.hatX != 0.0) {
                delta = s.hatX < 0.0 ? -90.0 : 90.0;
            } else {
                delta = s.hatY < 0.0 ? 0.0 : 180.0;
            }

            double heading = helper.normalize180(newLeftX) + delta;
            newXYZ = Exca_Quaternion.endPoint(helper.getXYZ(), 0, 0, helper.getStep().meters, heading + DataSaved.deltaGPS2);
        }

        helper.setLeft(newLeftX, newLeftY, newLeftYaw, newLeftHatX, newLeftHatY);
        helper.setXYZ(newXYZ);


        Log.d(TAG, String.format(
                "ON TICK | LEFT FOOT | step: %.3f | AXIS Y: %.3f | AXIS X: %.3f | YAW: %.3f | HAT X: %.3f | HAT Y: %.3f | XYZ: (%.3f, %.3f, %.3f) | HDT: %.3f",
                helper.getStep().meters,
                helper.getLeft().getLeftAxisY(),
                helper.getLeft().getLeftAxisX(),
                helper.getLeft().getLeftYaw(),
                helper.getLeft().getLeftHatX(),
                helper.getLeft().getLeftHatY(),
                helper.getX(), helper.getY(), helper.getZ(),
                helper.getLeft().getLeftAxisX()
        ));
    }

    private void onTickRight() {
        final DPadState s = right;
        if (s == null) return;


        final DPadHelper helper = DPadHelper.getInstance();

        long timestamp = System.currentTimeMillis() - s.timestamp;

        // Controllo timestamp
        boolean isOldDataY = timestamp > 100 && s.pitch == 0.0;
        boolean isOldDataX = timestamp > 100 && s.roll == 0.0;
        boolean isOldDataYaw = timestamp > 100 && s.yaw == 0.0;
        boolean isOldDataHatY = timestamp > 100 && s.hatY == 0.0;
        boolean isOldDataHatX = timestamp > 100 && s.hatX == 0.0;


        final DPadMapperRight cur = helper.getRight();

        // Calcola nuovi valori con controllo su dati vecchi
        double newRightY = cur.getRightAxisY() - (isOldDataY ? 0.0 : s.pitch);
        double newRightX = cur.getRightAxisX() + (isOldDataX ? 0.0 : s.roll);
        double newRightYaw = cur.getRightYaw() + (isOldDataYaw ? 0.0 : s.yaw);

        double newRightHatY = cur.getRightHatY() + ((isOldDataHatY ? 0.0 : s.hatY) * -0.5);
        double newRightHatX = cur.getRightHatX() + ((isOldDataHatX ? 0.0 : s.hatX) * 0.5);

        helper.setRight(newRightX, newRightY, newRightYaw, newRightHatX, newRightHatY);


        Log.d(TAG, String.format(
                "ON TICK | RIGHT FOOT | step: %.3f | AXIS Y: %.3f | AXIS X: %.3f | YAW: %.3f | HAT X: %.3f | HAT Y: %.3f",
                helper.getStep().meters,
                helper.getRight().getRightAxisY(),
                helper.getRight().getRightAxisX(),
                helper.getRight().getRightYaw(),
                helper.getRight().getRightHatX(),
                helper.getRight().getRightHatY()
        ));
    }

}
