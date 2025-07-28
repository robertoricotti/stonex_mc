package services;


import static gui.MyApp.isApollo;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static gui.dialogs_and_toast.DialogPassword.isTech2;
import static serial.SerialReadThread.serialEmpty;
import static services.CanService.nmeaSTX_Disc;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import event_bus.SerialEvent;
import gui.MyApp;
import gui.gps.NmeaGenerator;
import gui.grading_dozergrader.Grading3D_DXF;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import packexcalib.gnss.Deg2UTM;
import packexcalib.gnss.NmeaListener;
import utils.MyDeviceManager;
import utils.UnitsConversion;


public class CanSender extends Service {
    public static boolean tryingBTCAN = false;
    int connections = 0;
    int isTechCount, startCanopen;
    public static byte onGrade, d0;
    private ScheduledExecutorService senderExecutorGrade_50;
    private ScheduledExecutorService senderExecutor500;
    private ScheduledExecutorService senderExecutor2000;
    Executor mExecutor;
    private static final int THREAD_POOL_SIZE = 1;
    byte[] pendenza;
    byte[] sinistra;
    byte[] mezzo;
    byte[] destra;
    int slope, left, cent, right;
    private static byte indiceECU, btnP, btnM, btnTest;

    public CanSender() {

    }

    @Override
    public void onCreate() {

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
        senderExecutor500.scheduleAtFixedRate(new AsyncSender500(), 1000, 500, TimeUnit.MILLISECONDS);
        senderExecutor2000.scheduleAtFixedRate(new AsyncSender2000(), 1000, 2000, TimeUnit.MILLISECONDS);
        senderExecutorGrade_50.scheduleAtFixedRate(new gradeSender(), 2000, 50, TimeUnit.MILLISECONDS);
        return START_STICKY;
    }

    private class gradeSender implements Runnable {

        @Override
        public void run() {
            if (MyApp.licenseType == 5) {
                try {

                    slope = UnitsConversion.limitInt((int) (ExcavatorLib.correctRoll * 100), Short.MIN_VALUE, Short.MAX_VALUE);
                    left = UnitsConversion.limitInt((int) (TriangleService.quota3D_SX * 1000), Short.MIN_VALUE, Short.MAX_VALUE);
                    cent = (int) UnitsConversion.limitD(TriangleService.quota3D_CT * 1000, Short.MIN_VALUE, Short.MAX_VALUE);
                    right = (int) UnitsConversion.limitD(TriangleService.quota3D_DX * 1000, Short.MIN_VALUE, Short.MAX_VALUE);
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
            }
        }

    }

    private class AsyncSender500 implements Runnable {
        @SuppressLint("NewApi")
        @Override
        public void run() {

            if (DataSaved.my_comPort == 4) {
                EventBus.getDefault().post(new SerialEvent(NmeaGenerator.generateLLQ()));
                EventBus.getDefault().post(new SerialEvent(NmeaGenerator.generateGPHDT()));
                EventBus.getDefault().post(new SerialEvent(NmeaGenerator.generateGPGGA()));
                NmeaListener.NmeaStandard(NmeaGenerator.generateLLQ());
                NmeaListener.NmeaStandard(NmeaGenerator.generateGPGGA());
                NmeaListener.NmeaStandard(NmeaGenerator.generateGPHDT());
            }
            try {
                switch (DataSaved.my_comPort){
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
                            byte msg=0x03;



                            MyDeviceManager.CanWrite(0, 0x18FF0001, 4, new byte[]{0x20, msg, speed, (byte) 0x03});
                            connections = 0;
                        }
                        break;
                    case 1:
                    case 2:
                    case 3:
                        Deg2UTM deg2UTM = new Deg2UTM(NmeaListener.mLat_1, NmeaListener.mLon_1, NmeaListener.Quota1, DataSaved.S_CRS,MyApp.GEOIDE_PATH);
                        DataSaved.gpsOk = gpsStat(NmeaListener.ggaQuality, deg2UTM.getQuota(), serialEmpty);
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
                    byte msg=0x03;


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


            if (DataSaved.isCanOpen == 5) {
                startCanopen++;
                if (startCanopen == 2) {
                    if (isApollo) {
                        MyDeviceManager.CanWrite(0, 0, 2, new byte[]{1, 0});
                    }
                    if (DataSaved.fwbwSwing != 0) {
                        if (isApollo) {
                            MyDeviceManager.CanWrite(0, 1568, 8, new byte[]{43, 0, 98, 0, 50, 0, 0, 0});
                        }
                    }
                }
                if (startCanopen == 30) {
                    startCanopen = 0;
                }
            } else {
                if (DataSaved.fwbwSwing != 0) {
                    if (isApollo) {
                        MyDeviceManager.CanWrite(0, 0, 2, new byte[]{1, 0x20});
                        MyDeviceManager.CanWrite(0, 1568, 8, new byte[]{43, 0, 98, 0, 50, 0, 0, 0});

                    }


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




