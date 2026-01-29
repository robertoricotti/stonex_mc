package services;

import static packexcalib.exca.Sensors_Decoder.Deg_Benna_W_Tilt;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static packexcalib.exca.Sensors_Decoder.Deg_Roto;
import static packexcalib.exca.Sensors_Decoder.Deg_boom1;
import static packexcalib.exca.Sensors_Decoder.Deg_bucket;
import static packexcalib.exca.Sensors_Decoder.Deg_pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_roll;
import static packexcalib.exca.Sensors_Decoder.Deg_stick;
import static packexcalib.exca.Sensors_Decoder.Deg_tilt;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.JOYSTICKS;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import DPAD.DPadHelper;
import DPAD.DPadMapper;
import gui.gps.NmeaGenerator;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;

public class Joystick_Service extends Service {
    private HandlerThread handlerThread;
    private Handler handler;


    public Joystick_Service() {
    }

    @Override
    public void onCreate() {

        handlerThread = new HandlerThread("JoystickWorker");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        super.onCreate();
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(task);
        return START_STICKY;

    }

    private final Runnable task = new Runnable() {
        @Override
        public void run() {

            if (DataSaved.isCanOpen == JOYSTICKS) {
                switch (DataSaved.isWL) {
                    case EXCAVATOR:
                        final DPadMapper current =DPadHelper.getInstance().getSnapshot();
                        NmeaGenerator.HEADING=current.leftAxisX;
                        Deg_stick=current.leftAxisY;
                        Deg_bucket=current.rightAxisX;
                        Deg_Benna_W_Tilt=current.rightAxisX;
                        Deg_boom1=current.rightAxisY;
                        Deg_Roto=current.leftYaw;
                        Deg_tilt=current.rightYaw;
                        DataSaved.demoEAST=current.xyz[0];
                        DataSaved.demoNORD=current.xyz[1];
                        DataSaved.demoZ=current.xyz[2];
                        Deg_pitch=current.rightHatY;
                        Deg_roll=current.rightHatX;
                        Deg_Boom_Roll=Deg_roll;

                        ExcavatorLib.Excavator();
                        break;

                    case WHEELLOADER:

                        break;

                    case DOZER:
                    case DOZER_SIX:

                        break;


                    case GRADER:

                        break;

                    case DRILL:

                        break;

                }
            }

            handler.postDelayed(this, 100);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            handler.removeCallbacksAndMessages(null);
            handlerThread.quitSafely();
        } catch (Exception ignored) {

        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}