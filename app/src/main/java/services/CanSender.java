package services;


import static gui.MyApp.hAlarm;
import static gui.MyApp.isApollo;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static gui.dialogs_and_toast.DialogPassword.isTech2;
import static packexcalib.gnss.NmeaListener.Est1;
import static packexcalib.gnss.NmeaListener.Nord1;
import static packexcalib.gnss.NmeaListener.Quota1;
import static packexcalib.gnss.NmeaListener.mLat_1;
import static packexcalib.gnss.NmeaListener.mLon_1;
import static serial.SerialReadThread.serialEmpty;
import static services.CanService.nmeaSTX_Disc;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cloud.WebSocketPlugin;
import event_bus.SerialEvent;
import gui.MyApp;
import gui.debug_ecu.Serial_Msg_Debug;
import gui.gps.NmeaGenerator;
import gui.my_opengl.My3DActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.PLC_DataTypes_BigEndian;
import packexcalib.gnss.NmeaListener;
import utils.MyDeviceManager;


public class CanSender extends Service {
    static Map<String, Object> payload;
    public static boolean tryingBTCAN = false;
    int connections = 0;
    int isTechCount, startCanopen;
    public static byte onGrade, d0;
    private ScheduledExecutorService senderExecutorGrade_50;
    private ScheduledExecutorService senderExecutor500;
    private ScheduledExecutorService senderExecutor2000;
    private ScheduledExecutorService scheduledExecutorService1min;
    Executor mExecutor;
    private static final int THREAD_POOL_SIZE = 1;
    private static boolean FlipFlop;

    public CanSender() {

    }

    @Override
    public void onCreate() {
        payload = new HashMap<>();

        mExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);


        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @SuppressLint("DiscouragedApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        senderExecutor500 = Executors.newSingleThreadScheduledExecutor();
        senderExecutor2000 = Executors.newSingleThreadScheduledExecutor();
        senderExecutorGrade_50 = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService1min = Executors.newSingleThreadScheduledExecutor();

        senderExecutor500.scheduleAtFixedRate(new AsyncSender500(), 1000, 500, TimeUnit.MILLISECONDS);
        senderExecutor2000.scheduleAtFixedRate(new AsyncSender2000(), 1000, 2000, TimeUnit.MILLISECONDS);
        senderExecutorGrade_50.scheduleAtFixedRate(new gradeSender(), 2000, 50, TimeUnit.MILLISECONDS);
        scheduledExecutorService1min.scheduleAtFixedRate(new AsyncSender1min(), 1000, 60000, TimeUnit.MILLISECONDS);
        return START_STICKY;
    }

    private class gradeSender implements Runnable {

        @Override
        public void run() {

           /* if (MyApp.licenseType == 5) {
                try {

                    slope = MyMCUtils.limitInt((int) (ExcavatorLib.correctRoll * 100), Short.MIN_VALUE, Short.MAX_VALUE);
                    left = MyMCUtils.limitInt((int) (TriangleService.quota3D_SX * 1000), Short.MIN_VALUE, Short.MAX_VALUE);
                    cent = (int) MyMCUtils.limitD(TriangleService.quota3D_CT * 1000, Short.MIN_VALUE, Short.MAX_VALUE);
                    right = (int) MyMCUtils.limitD(TriangleService.quota3D_DX * 1000, Short.MIN_VALUE, Short.MAX_VALUE);
                    pendenza = PLC_DataTypes_LittleEndian.S16_to_bytes((short) slope);
                    sinistra = PLC_DataTypes_LittleEndian.S16_to_bytes((short) -left);
                    mezzo = PLC_DataTypes_LittleEndian.S16_to_bytes((short) -cent);
                    destra = PLC_DataTypes_LittleEndian.S16_to_bytes((short) -right);

                    switch (DataSaved.isWL) {
                        case 0:
                        case 1:
                            //no send
                            break;
                        case 2:
                        case 3:
                        case 4:
                            if (MyApp.visibleActivity instanceof Grading3D_DXF) {

                                MyDeviceManager.CanWrite(1, 2050, 8, new byte[]{pendenza[0], pendenza[1], sinistra[0], sinistra[1], mezzo[0], mezzo[1], destra[0], destra[1]});
                            } else {
                                MyDeviceManager.CanWrite(1, 2049, 8, new byte[]{0, 0, 0, 0, 0, 0, 0, 0});

                            }
                            break;
                    }
                } catch (Exception e) {
                    MyDeviceManager.CanWrite(1, 2049, 8, new byte[]{-1, -1, -1, -1, -1, -1, -1, -1});
                }
            }*/
            FlipFlop=!FlipFlop;
            if(FlipFlop) {
                if (DataSaved.my_comPort == 4) {
                    new SerialEvent(NmeaGenerator.generateLLQ());
                    new SerialEvent(NmeaGenerator.generateGPHDT());
                    new SerialEvent(NmeaGenerator.generateGPGGA());
                    if (MyApp.visibleActivity instanceof Serial_Msg_Debug) {
                        EventBus.getDefault().post(new SerialEvent(NmeaGenerator.generateLLQ()));
                        EventBus.getDefault().post(new SerialEvent(NmeaGenerator.generateGPHDT()));
                        EventBus.getDefault().post(new SerialEvent(NmeaGenerator.generateGPGGA()));
                    }
                    NmeaListener.NmeaStandard(NmeaGenerator.generateLLQ());
                    NmeaListener.NmeaStandard(NmeaGenerator.generateGPGGA());
                    NmeaListener.NmeaStandard(NmeaGenerator.generateGPHDT());
                }
            }
        }

    }

    private class AsyncSender1min implements Runnable {
        @SuppressLint("NewApi")
        @Override
        public void run() {
            boolean[] bStat = new boolean[8];
            bStat[0] = DataSaved.gpsOk;//vale 1
            bStat[1] = DataSaved.isWL == 0;//exca  vale 2
            bStat[2] = DataSaved.isWL == 1;//wheel  vale 4
            bStat[3] = DataSaved.isWL == 2;//dozer  vale 8
            bStat[4] = DataSaved.isWL == 4;//grader vale 16
            bStat[5] = MyApp.visibleActivity instanceof My3DActivity;  //vale 32
            bStat[6] = CanService.isAuto > 0;//macchina in Automatico  vale 64
            bStat[7] = hAlarm;//allarme attivo  vale 128
            byte status = 0;
            status = PLC_DataTypes_BigEndian.Encode_8_bool_be(bStat);
            if (mLat_1 != 0 && mLon_1 != 0) {
                payload.put("latitude", mLat_1);
                payload.put("longitude", mLon_1);
                payload.put("localX", String.valueOf(Est1));
                payload.put("localY", String.valueOf(Nord1));
                payload.put("localZ", String.valueOf(Quota1));
                payload.put("machineState", status & 0xFF);//bitmask 8 booleans
                payload.put("description", DataSaved.machineName + "\n" + DataSaved.progettoSelected);//testo libero
                WebSocketPlugin.getWebSocketPluginInstance(MyApp.visibleActivity).sendCommand("data_positioning_ack", payload);
                payload.clear();
            }

        }
    }

    private class AsyncSender500 implements Runnable {
        @SuppressLint("NewApi")
        @Override
        public void run() {


            try {
                switch (DataSaved.my_comPort) {
                    case 0:
                        if (!nmeaSTX_Disc) {
                            if (NmeaListener.ggaQuality.equals("4") && Double.parseDouble(NmeaListener.VRMS_) < DataSaved.Max_CQ3D && NmeaListener.mch_Hdt != 999.999) {
                                DataSaved.gpsOk = true;
                            } else {
                                DataSaved.gpsOk = false;
                                connections++;
                            }
                        } else {
                            DataSaved.gpsOk = false;
                            connections++;
                        }

                        if (connections == 20) {
                            byte speed = 0;
                            switch (DataSaved.reqSpeed) {
                                case 0:
                                    speed = 5;
                                    break;
                                case 1:
                                    speed = 4;
                                    break;
                                case 2:
                                    speed = 3;
                                    break;
                                case 3:
                                    speed = 0;
                                    break;

                            }
                            byte msg = 0x03;


                            MyDeviceManager.CanWrite(0, 0x18FF0001, 4, new byte[]{0x20, msg, speed, (byte) 0x03});
                            connections = 0;
                        }
                        break;
                    case 1:
                    case 2:
                    case 3:

                        DataSaved.gpsOk = gpsStat(NmeaListener.ggaQuality, Quota1, serialEmpty);
                        break;
                    case 4:
                        DataSaved.gpsOk = true;
                        break;
                }


            } catch (Exception e) {
                connections++;
                if (connections == 30) {
                    byte speed = 0;
                    switch (DataSaved.reqSpeed) {
                        case 0:
                            speed = 5;
                            break;
                        case 1:
                            speed = 4;
                            break;
                        case 2:
                            speed = 3;
                            break;
                        case 3:
                            speed = 0;
                            break;

                    }
                    DataSaved.gpsOk = false;
                    byte msg = 0x03;


                    MyDeviceManager.CanWrite(0, 0x18FF0001, 4, new byte[]{0x20, msg, speed, (byte) 0x03});
                    connections = 0;
                }
            }


            try {
                if (isTech) {
                    isTechCount++;
                    if (isTechCount >= 600) {
                        isTechCount = 0;
                        isTech = false;//Tech LogIn dosabled after 5 minutes inside a Work Activity
                        isTech2 = false;
                    }
                }


            } catch (Exception e) {

            }
            //MyDeviceManager.CanWrite(0, 160, 8, new byte[]{d0, 0, onGrade, 0, 0, (byte) 160, (byte) 168, 0});
        }


    }

    private class AsyncSender2000 implements Runnable {
        @SuppressLint("NewApi")
        @Override
        public void run() {
            if(DataSaved.Interface_Type==2){
                if(! (MyApp.visibleActivity instanceof My3DActivity)){
                    MyDeviceManager.CanWrite(1, 0x18EEFF85, 8,
                            new byte[]{(byte) 0xF4,
                                    (byte) 0xF0,
                                    (byte) 0x13,
                                    (byte) 0x23,
                                    (byte) 0x0,
                                    (byte) 0x82,
                                    (byte) 0x0,
                                    (byte) 0xB0});
                }
            }


            if (DataSaved.isCanOpen == 5) {
                startCanopen++;
                if (startCanopen == 2) {
                    if (isApollo) {
                        MyDeviceManager.CanWrite(0, 0, 2, new byte[]{1, 0});
                    }

                }
                if (startCanopen == 30) {
                    startCanopen = 0;
                }
            }


        }


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (senderExecutor500 != null) {
            senderExecutor500.shutdown();
        }

        if (senderExecutor2000 != null) {
            senderExecutor2000.shutdown();
        }
        if (senderExecutorGrade_50 != null) {
            senderExecutorGrade_50.shutdown();
        }
        if (scheduledExecutorService1min != null) {
            scheduledExecutorService1min.shutdown();
        }


    }

    public boolean gpsStat(String quality, double quota, boolean empty) {
        try {
            double rms;
            try {
                rms = Double.parseDouble(NmeaListener.VRMS_);
            } catch (Exception e) {
                rms = DataSaved.Max_CQ3D;
            }
            if (quality.contains("4") && rms <= DataSaved.Max_CQ3D) {

                if (NmeaListener.mch_Hdt == 999.999) {
                    return false;
                } else {
                    return !empty;
                }

            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }


    }


}




