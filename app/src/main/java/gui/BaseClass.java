package gui;

import static gui.MyApp.hAlarm;
import static gui.MyApp.isApollo;
import static gui.MyApp.isOffgrid;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import DPAD.DPadMapper;
import DPAD.DPadHelper;
import DPAD.DPadManager;
import DPAD.DPadState;
import DPAD.T16000MProfile;
import packexcalib.exca.DataSaved;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;
import packexcalib.exca.ExcavatorLib;
import utils.MyDeviceManager;


public class BaseClass extends AppCompatActivity {
    private static double[] nuovaPos;

    public static final String TAG = "BaseClass";
    private DPadManager cm;
    private final SparseArray<Runnable> buttonDownActions = new SparseArray<>();
    private final SparseArray<Runnable> buttonUpActions = new SparseArray<>();
    boolean isPressed, isTouched;
    private  DPadState currentDPadState = new DPadState();
    public enum StepDistance {
        CM_5 ("5cm",  0.05),
        CM_10("10cm", 0.10),
        CM_15("15cm", 0.15),
        CM_20("20cm", 0.20),
        CM_25("25cm", 0.25);

        public final String label;
        public final double meters;

        StepDistance(String label, double meters) {
            this.label = label;
            this.meters = meters;
        }
    }
    private StepDistance stepDistance = StepDistance.CM_5;
    private static final long TICK_MS = 50;

    private final Handler tickHandler = new Handler();
    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if(DataSaved.my_comPort==4){
                onTick();
            }

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
            @Override public void onDPadConnected(DPadState state) {

                Log.i(TAG, "Connected: " + "{ name: " + state.deviceName +  " deviceId= " + state.deviceId + " }");
            }
            @Override public void onDPadDisconnected(int deviceId) {
                Log.i(TAG, "Disconnected: " + "{ deviceId= " + deviceId + " }");
            }
            @Override public void onStateUpdated(DPadState s) {
                Log.d(TAG,
                        "State: roll=" + s.roll +
                                " pitch=" + s.pitch +
                                " yaw=" + s.yaw +
                                " thr=" + s.throttle +
                                " hatX=" + s.hatX +
                                " hatY=" + s.hatY +
                                " side=" + s.side +
                                " deviceId=" + s.deviceId
                );

                currentDPadState = s;

            }
            @Override public void onButtonDown(DPadState s, int keyCode) {
                Log.d(TAG,
                        "Button DOWN " + keyCode +
                                " side=" + s.side +
                                " deviceId=" + s.deviceId
                );

                if(keyCode >= 188 && keyCode <= 203){
                    Runnable r = buttonDownActions.get(keyCode);
                    if (r != null) r.run();
                    return;
                }
            }
            @Override public void onButtonUp(DPadState s, int keyCode) {
                Log.d(TAG,
                        "Button UP " + keyCode +
                                " side=" + s.side +
                                " deviceId=" + s.deviceId
                );

                if(keyCode >= 188 && keyCode <= 203){
                    Runnable r = buttonUpActions.get(keyCode);
                    if (r != null) r.run();
                    return;
                }
            }
        });

        cm.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cm.stop();
        tickHandler.removeCallbacks(tickRunnable);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        if (cm.handleMotionEvent(ev)) return true;
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG,event.getKeyCode()+"");
        //131..135

        if(DataSaved.my_comPort==4&& Build.BRAND.equals("MEGA_1")){
            if (event.getAction() == KeyEvent.ACTION_DOWN){
                switch (event.getKeyCode()){
                    case 131:
                        nuovaPos=Exca_Quaternion.endPoint(new double[]{DataSaved.demoEAST,DataSaved.demoNORD,DataSaved.demoZ},0,0,0.05,hdt_BOOM);
                        DataSaved.demoEAST=nuovaPos[0];
                        DataSaved.demoNORD=nuovaPos[1];
                        DataSaved.demoZ=nuovaPos[2];
                        break;
                    case 132:
                        nuovaPos=Exca_Quaternion.endPoint(new double[]{DataSaved.demoEAST,DataSaved.demoNORD,DataSaved.demoZ},0,0,0.05,hdt_BOOM+180);
                        DataSaved.demoEAST=nuovaPos[0];
                        DataSaved.demoNORD=nuovaPos[1];
                        DataSaved.demoZ=nuovaPos[2];
                        break;
                    case 133:
                        nuovaPos=Exca_Quaternion.endPoint(new double[]{DataSaved.demoEAST,DataSaved.demoNORD,DataSaved.demoZ},0,0,0.05,hdt_BOOM+90);
                        DataSaved.demoEAST=nuovaPos[0];
                        DataSaved.demoNORD=nuovaPos[1];
                        DataSaved.demoZ=nuovaPos[2];
                        break;
                    case 134:
                        nuovaPos=Exca_Quaternion.endPoint(new double[]{DataSaved.demoEAST,DataSaved.demoNORD,DataSaved.demoZ},0,0,0.05,hdt_BOOM-90);
                        DataSaved.demoEAST=nuovaPos[0];
                        DataSaved.demoNORD=nuovaPos[1];
                        DataSaved.demoZ=nuovaPos[2];
                        break;
                    case 135:
                       DataSaved.HEADING+=0.5;
                       DataSaved.HEADING=DataSaved.HEADING%360;
                        break;
                }

            }
            return super.dispatchKeyEvent(event);
        }else {
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
    }

    private void initBottonDownMap() {
        buttonDownActions.put(188, () -> {
            StepDistance[] v = StepDistance.values();
            stepDistance = v[(stepDistance.ordinal() + 1) % v.length];
            Log.d(TAG, "Step distance: " + stepDistance.meters);
        });
        buttonDownActions.put(189, () -> {});
        buttonDownActions.put(190, () -> {
            final DPadMapper data = DPadHelper.getInstance().getSnapshot();
            DPadHelper.getInstance().setZ(data.xyz[2] - stepDistance.meters);
        });
        buttonDownActions.put(191, () -> {
            final DPadMapper data = DPadHelper.getInstance().getSnapshot();
            DPadHelper.getInstance().setZ(data.xyz[2] + stepDistance.meters);
        });
        buttonDownActions.put(192, () -> {});
        buttonDownActions.put(193, () -> {});
        buttonDownActions.put(194, () -> {});
        buttonDownActions.put(195, () -> {});
        buttonDownActions.put(196, () -> {});
        buttonDownActions.put(197, () -> {});
        buttonDownActions.put(198, () -> {});
        buttonDownActions.put(199, () -> {});
        buttonDownActions.put(200, () -> {});
        buttonDownActions.put(201, () -> {});
        buttonDownActions.put(202, () -> {});
        buttonDownActions.put(203, () -> {});
    }
    private void initBottonUpMap() {
        buttonUpActions.put(188, () -> {});
        buttonUpActions.put(189, () -> {});
        buttonUpActions.put(190, () -> {});
        buttonUpActions.put(191, () -> {});
        buttonUpActions.put(192, () -> {});
        buttonUpActions.put(193, () -> {});
        buttonUpActions.put(194, () -> {});
        buttonUpActions.put(195, () -> {});
        buttonUpActions.put(196, () -> {});
        buttonUpActions.put(197, () -> {});
        buttonUpActions.put(198, () -> {});
        buttonUpActions.put(199, () -> {});
        buttonUpActions.put(200, () -> {});
        buttonUpActions.put(201, () -> {});
        buttonUpActions.put(202, () -> {});
        buttonUpActions.put(203, () -> {});
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

    private void onTick() {
        final DPadState s = currentDPadState;
        if (s == null) return;

        final DPadHelper helper = DPadHelper.getInstance();
        final DPadMapper cur = helper.getSnapshot(); // Snapshot unico per entrambi i lati

        if (s.side == DPadState.SIDE_LEFT) {
            // Calcola nuovi valori solo per left
            double newLeftY = cur.leftAxisY - s.pitch; // pitch già include check != 0
            double newLeftX = cur.leftAxisX + s.roll;
            double newLeftYaw = cur.leftYaw + s.yaw;

            double newLeftHatY = cur.leftHatY + (s.hatY * -0.1); // -1, 0, o 1
            double newLeftHatX = cur.leftHatX + (s.hatX * 0.1);

            // XYZ: solo se hat premuto
            double[] newXYZ = cur.xyz;
            if (s.hatX != 0.0 || s.hatY != 0.0) {
                double delta = 0.0;
                if (s.hatX != 0.0) {
                    delta = s.hatX < 0.0 ? -90.0 : 90.0;
                } else {
                    delta = s.hatY < 0.0 ? 0.0 : 180.0;
                }

                double heading = helper.normalize180(newLeftX) + delta;
                newXYZ = Exca_Quaternion.endPoint(cur.xyz, 0, 0, stepDistance.meters, heading+DataSaved.deltaGPS2);
            }

            helper.update(
                    newLeftX, newLeftY, newLeftYaw,
                    newLeftHatX, newLeftHatY,
                    cur.rightAxisX, cur.rightAxisY, cur.rightYaw,
                    cur.rightHatX, cur.rightHatY,
                    newXYZ
            );
        }
        else if (s.side == DPadState.SIDE_RIGHT) {
            // Calcola nuovi valori solo per right
            double newRightY = cur.rightAxisY - s.pitch;
            double newRightX = cur.rightAxisX + s.roll;
            double newRightYaw = cur.rightYaw + s.yaw;

            double newRightHatY = cur.rightHatY + (s.hatY * -0.1);
            double newRightHatX = cur.rightHatX + (s.hatX * 0.1);

            helper.update(
                    cur.leftAxisX, cur.leftAxisY, cur.leftYaw,
                    cur.leftHatX, cur.leftHatY,
                    newRightX, newRightY, newRightYaw,
                    newRightHatX, newRightHatY,
                    cur.xyz
            );
        }

        logState(helper);
    }

    private void logState(DPadHelper helper) {
        Log.d(TAG, String.format(
                "ON TICK | LEFT FOOT | step=%s %.3f | AXIS Y: %.3f | AXIS X: %.3f | YAW: %.3f | HAT X: %.3f | HAT Y: %.3f | XYZ: (%.3f, %.3f, %.3f) | HDT: %.3f",
                stepDistance.label,
                stepDistance.meters,
                helper.getLeftAxisY(),
                helper.getLeftAxisX(),
                helper.getLeftYaw(),
                helper.getLeftHatX(),
                helper.getLeftHatY(),
                helper.getX(), helper.getY(), helper.getZ(),
                helper.getLeftAxisX()
        ));

        Log.d(TAG, String.format(
                "ON TICK | RIGHT FOOT | step=%s %.3f | AXIS Y: %.3f | AXIS X: %.3f | YAW: %.3f | HAT X: %.3f | HAT Y: %.3f",
                stepDistance.label,
                stepDistance.meters,
                helper.getRightAxisY(),
                helper.getRightAxisX(),
                helper.getRightYaw(),
                helper.getRightHatX(),
                helper.getRightHatY()
        ));
    }



}
