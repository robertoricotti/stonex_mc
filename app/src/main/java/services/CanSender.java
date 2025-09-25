package services;


import static gui.MyApp.hAlarm;
import static gui.MyApp.isApollo;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static gui.dialogs_and_toast.DialogPassword.isTech2;
import static packexcalib.exca.DataSaved.GAIN_LEFT;
import static packexcalib.exca.DataSaved.GAIN_RIGHT;
import static packexcalib.exca.DataSaved.HYDRAULIC_CONTROL_POINT_DOZER;
import static packexcalib.exca.DataSaved.HYDRAULIC_CONTROL_POINT_GRADER;
import static packexcalib.exca.DataSaved.maxSpeedRightDW;
import static packexcalib.exca.DataSaved.maxSpeedRightUP;
import static packexcalib.exca.DataSaved.minSpeedRightDW;
import static packexcalib.exca.DataSaved.minSpeedRightUP;
import static packexcalib.exca.ExcavatorLib.correctRoll;
import static packexcalib.gnss.NmeaListener.Est1;
import static packexcalib.gnss.NmeaListener.Nord1;
import static packexcalib.gnss.NmeaListener.Quota1;
import static packexcalib.gnss.NmeaListener.mLat_1;
import static packexcalib.gnss.NmeaListener.mLon_1;
import static serial.SerialReadThread.serialEmpty;
import static services.CanService.Dozer_Auto_Main;
import static services.CanService.Grader_AutoRight;
import static services.CanService.Grader_Auto_Left;
import static services.CanService.Grader_Auto_SS;
import static services.CanService.nmeaSTX_Disc;
import static services.TriangleService.ctOffGrid;
import static services.TriangleService.ltOffGrid;
import static services.TriangleService.rtOffGrid;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.example.stx_dig.R;

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
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import packexcalib.gnss.NmeaListener;
import utils.MyDeviceManager;
import utils.MyMCUtils;


public class CanSender extends Service {
   public static double QL, QC, QR;
    public static double GroundSlope;
    public static int valueKomL = 0, valueKomR = 0, valueCATL = 0, valueCATR = 0,valueCATSS=0, valueJDL = 20000, valueJDR = 20000,valueJDSS=20000;
    public static byte dirCAT_L, dirCAT_R, dirCAT_SS= (byte) 0xF2;
    public static boolean prepLeft, prepRight, prepSS;
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
            try {
                if(MyApp.visibleActivity instanceof My3DActivity && MyApp.licenseType==5) {
                    AutoHandling();
                }
            } catch (Exception e) {
                Log.e("CanErr",Log.getStackTraceString(e));
            }


            if (DataSaved.my_comPort == 4) {
            FlipFlop=!FlipFlop;
            if(FlipFlop) {

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


    private void AutoHandling() {

        dirCAT_L = (byte) 0xF2;
        dirCAT_R = (byte) 0xF2;
        dirCAT_SS = (byte) 0xF2;

        if (My3DActivity.diaolgGainHydro.dialog.isShowing()) {

            QL = 0;
            QC = 0;
            QR = 0;
            GroundSlope = correctRoll;
            valueKomL = 0;
            valueKomR = 0;
            valueCATL = 0;
            valueCATR = 0;
            valueJDL = 20000;
            valueJDR = 20000;
            dirCAT_L = (byte) 0xF2;
            dirCAT_R = (byte) 0xF2;
            dirCAT_SS = (byte) 0xF2;

        } else {

            QL = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_SX;
            QC = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_CT;
            QR = MyMCUtils.ledder(GAIN_RIGHT) * TriangleService.quota3D_DX;
            //TODO qui calcoli
            if (DataSaved.isWL == 0 || DataSaved.isWL == 1) {

                QL = 0;
                QC = 0;
                QR = 0;
                GroundSlope = correctRoll;
                valueKomL = 0;
                valueKomR = 0;
                valueCATL = 0;
                valueCATR = 0;
                valueJDL = 20000;
                valueJDR = 20000;
                dirCAT_L = (byte) 0xF2;
                dirCAT_R = (byte) 0xF2;
                dirCAT_SS = (byte) 0xF2;
            } else {

                if (DataSaved.isWL == 4) {
                    //GRADER

                    switch (HYDRAULIC_CONTROL_POINT_GRADER) {
                        case 0:

                            handleLamaGrader(QC,QR,ctOffGrid,rtOffGrid);
                            break;

                        case 1:
                            handleLamaGrader(QC,QL,ctOffGrid,ltOffGrid);
                            break;

                        case 2://left right
                            handleLamaGrader(QL,QR,ltOffGrid,rtOffGrid);
                            break;
                    }
                    handleSideShift(DataSaved.bucketEdge,DataSaved.line_Offset);

                } else {
                    //DOZER
                    if (!isInRange(DataSaved.tolleranza_Z, QC) && Math.abs(QC) < DataSaved.HYDRAULIC_WINDOW) {
                        if (QC < -DataSaved.tolleranza_Z) {

                            dirCAT_L = (byte) 0xF2;
                            valueCATL = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, 0.5, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP);
                            valueCATL = (byte) MyMCUtils.limitInt(valueCATL, 0, 255);

                            valueKomL = (int) MyMCUtils.myscaleD(Math.abs(QC), 0, 0.5, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP);
                            valueKomL = (int) MyMCUtils.limitInt(valueKomL, 0, 255);

                            valueJDL = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, 0.5, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP);
                            valueJDL = (int) MyMCUtils.myscaleD(valueJDL, 0, 255, 20000, 10000);
                            valueJDL = MyMCUtils.limitInt(valueJDL, 10000, 20000);


                        } else if (QC > DataSaved.tolleranza_Z) {
                            dirCAT_L = (byte) 0xF1;
                            valueCATL = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, 0.5, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW);
                            valueCATL = (byte) MyMCUtils.limitInt(valueCATL, 0, 255);

                            valueKomL = (int) MyMCUtils.myscaleD(Math.abs(QC), 0, 0.5, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW);
                            valueKomL = (int) MyMCUtils.limitInt(valueKomL, 0, 255);
                            valueKomL = valueKomL * -1;


                            valueJDL = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, 0.5, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW);
                            valueJDL = (int) MyMCUtils.myscaleD(valueJDL, 0, 255, 20000, 30000);
                            valueJDL = MyMCUtils.limitInt(valueJDL, 20000, 30000);
                        }
                    } else {
                        valueKomL = 0;
                        valueCATL = 0;
                        valueJDL = 20000;
                        dirCAT_L = (byte) 0xF2;
                        dirCAT_SS = (byte) 0xF2;
                    }
                    switch (HYDRAULIC_CONTROL_POINT_DOZER) {
                        case 0:
                            GroundSlope = MyMCUtils.bladeSlope(TriangleService.posC, TriangleService.posR);
                            break;

                        case 1:
                            GroundSlope = MyMCUtils.bladeSlope(TriangleService.posL, TriangleService.posC);
                            break;

                    }
                    if (!isInRangeAng(correctRoll, GroundSlope, DataSaved.tolleranza_Slope)) {
                        double deviation = deviationFromSetpoint(correctRoll, GroundSlope, DataSaved.tolleranza_Slope);
                        deviation=MyMCUtils.ledder(GAIN_RIGHT)*deviation;

                        if (deviation > DataSaved.tolleranza_Slope) {
                            dirCAT_R = (byte) 0xF2;
                            valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightUP, maxSpeedRightUP);
                            valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                            valueKomR = (int) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightUP, maxSpeedRightUP);
                            valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);


                            valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightUP, maxSpeedRightUP);
                            valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 10000);
                            valueJDR = MyMCUtils.limitInt(valueJDR, 10000, 20000);

                        } else if (deviation < -DataSaved.tolleranza_Slope) {
                            dirCAT_R = (byte) 0xF1;
                            valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightDW, maxSpeedRightDW);
                            valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                            valueKomR = (int) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightDW, maxSpeedRightDW);
                            valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);
                            valueKomR = valueKomR * -1;


                            valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightUP, maxSpeedRightUP);
                            valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 30000);
                            valueJDR = MyMCUtils.limitInt(valueJDR, 20000, 30000);

                        }


                    } else {
                        valueKomR = 0;
                        valueCATR = 0;
                        valueJDR = 20000;
                        dirCAT_R = (byte) 0xF2;
                        dirCAT_SS = (byte) 0xF2;
                    }


                    prepSS = false;



                    if (prepLeft) {
                        if (Dozer_Auto_Main) {

                            if(Math.abs(QC)>DataSaved.HYDRAULIC_WINDOW||ctOffGrid){
                                valueKomL = 0;
                                valueCATL = 0;
                                valueJDL = 20000;
                                dirCAT_L = (byte) 0xF2;
                            }

                        } else {

                            valueKomL = 0;
                            valueCATL = 0;
                            valueJDL = 20000;
                            dirCAT_L = (byte) 0xF2;

                        }
                    } else {

                        valueKomL = 0;
                        valueCATL = 0;
                        valueJDL = 20000;
                        dirCAT_L = (byte) 0xF2;

                        valueKomR = 0;
                        valueCATR = 0;
                        valueJDR = 20000;
                        dirCAT_R = (byte) 0xF2;
                    }


                    if (prepRight) {
                        if (Dozer_Auto_Main) {
                            if(HYDRAULIC_CONTROL_POINT_DOZER==0) {
                                if (rtOffGrid) {
                                    valueKomR = 0;
                                    valueCATR = 0;
                                    valueJDR = 20000;
                                    dirCAT_R = (byte) 0xF2;
                                }
                            }
                            if(HYDRAULIC_CONTROL_POINT_DOZER==1) {
                                if (ltOffGrid) {
                                    valueKomR = 0;
                                    valueCATR = 0;
                                    valueJDR = 20000;
                                    dirCAT_R = (byte) 0xF2;
                                }
                            }

                        } else {

                            valueKomR = 0;
                            valueCATR = 0;
                            valueJDR = 20000;
                            dirCAT_R = (byte) 0xF2;


                        }
                    } else {

                        valueKomR = 0;
                        valueCATR = 0;
                        valueJDR = 20000;
                        dirCAT_R = (byte) 0xF2;

                    }
                }


            }
        }

        invioMessaggiDozer();

    }

    private boolean isInRange(double range, double value) {
        return Math.abs(value) < Math.abs(range);
    }

    private boolean isInRangeAng(double currentAngle, double setPoint, double tolerance) {
        return Math.abs(currentAngle - setPoint) <= tolerance;
    }

    public static double deviationFromSetpoint(double currentAngle, double setPoint, double tolerance) {
        double diff = currentAngle - setPoint;

        // Se la differenza assoluta è dentro la tolleranza, ritorna 0
        if (Math.abs(diff) <= tolerance) {
            return 0.0;
        }

        // Altrimenti ritorna la differenza (positiva o negativa)
        return diff;
    }

    private void handleLamaGrader(double LL, double RR,boolean checkPointLeft,boolean checkPointRight){
        if (!isInRange(DataSaved.tolleranza_Z, LL) && Math.abs(LL) < DataSaved.HYDRAULIC_WINDOW&& !checkPointLeft) {
            if (LL < -DataSaved.tolleranza_Z) {

                dirCAT_L = (byte) 0xF2;
                valueCATL = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, 0.5, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP);
                valueCATL = (byte) MyMCUtils.limitInt(valueCATL, 0, 255);

                valueKomL = (int) MyMCUtils.myscaleD(Math.abs(LL), 0, 0.5, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP);
                valueKomL = (int) MyMCUtils.limitInt(valueKomL, 0, 255);

                valueJDL = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, 0.5, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP);
                valueJDL = (int) MyMCUtils.myscaleD(valueJDL, 0, 255, 20000, 10000);
                valueJDL = MyMCUtils.limitInt(valueJDL, 10000, 20000);


            } else if (LL > DataSaved.tolleranza_Z) {
                dirCAT_L = (byte) 0xF1;
                valueCATL = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, 0.5, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW);
                valueCATL = (byte) MyMCUtils.limitInt(valueCATL, 0, 255);

                valueKomL = (int) MyMCUtils.myscaleD(Math.abs(LL), 0, 0.5, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW);
                valueKomL = (int) MyMCUtils.limitInt(valueKomL, 0, 255);
                valueKomL = valueKomL * -1;


                valueJDL = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, 0.5, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW);
                valueJDL = (int) MyMCUtils.myscaleD(valueJDL, 0, 255, 20000, 30000);
                valueJDL = MyMCUtils.limitInt(valueJDL, 20000, 30000);
            }
        } else {
            valueKomL = 0;
            valueCATL = 0;
            valueJDL = 20000;
            dirCAT_L = (byte) 0xF2;
            dirCAT_SS = (byte) 0xF2;
        }


        if (!isInRange(DataSaved.tolleranza_Z, RR) && Math.abs(RR) < DataSaved.HYDRAULIC_WINDOW&& !checkPointRight) {
            if (RR < -DataSaved.tolleranza_Z) {

                dirCAT_R = (byte) 0xF2;
                valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, 0.5, minSpeedRightUP, maxSpeedRightUP);
                valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                valueKomR = (int) MyMCUtils.myscaleD(Math.abs(RR), 0, 0.5, minSpeedRightUP, maxSpeedRightUP);
                valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);

                valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, 0.5, minSpeedRightUP, maxSpeedRightUP);
                valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 10000);
                valueJDR = MyMCUtils.limitInt(valueJDR, 10000, 20000);


            } else if (RR > DataSaved.tolleranza_Z) {
                dirCAT_R = (byte) 0xF1;
                valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, 0.5, minSpeedRightDW,maxSpeedRightDW);
                valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                valueKomR = (int) MyMCUtils.myscaleD(Math.abs(RR), 0, 0.5, minSpeedRightDW, maxSpeedRightDW);
                valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);
                valueKomR = valueKomR * -1;


                valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, 0.5, minSpeedRightDW, maxSpeedRightDW);
                valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 30000);
                valueJDR = MyMCUtils.limitInt(valueJDR, 20000, 30000);
            }
        } else {
            valueKomR = 0;
            valueCATR = 0;
            valueJDR = 20000;
            dirCAT_R = (byte) 0xF2;
            dirCAT_SS = (byte) 0xF2;
        }


    }

    private void handleSideShift(int bladeEdge,double offset){
        double dist = 0;
        double rot;
        double rotFix = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rot = TriangleService.orientamentoFreccia + rotFix;
        rot=((rot + 0) % 360 + 360) % 360;

        /*
        se  30 <rot> 120  la lama deve andare verso destra
        se  240<rot>300  la lama deve andare a sinistra
        */
        switch (bladeEdge) {
            case -1:
                dist = TriangleService.dist3D_SX;

                break;
            case 0:
                dist = TriangleService.dist3D_CT;
                break;

            case 1:
                dist = TriangleService.dist3D_DX;
                break;
        }
        dist=dist+offset;
        if(Grader_Auto_SS){
            //TODO sideshift
            Log.w("SideShift",dist+"    "+rot);
        }else {
            valueCATSS=0;
            valueJDSS=20000;
            dirCAT_SS= (byte) 0xF2;
            Log.d("SideShift",dist+"    "+rot);
        }
    
    }

    private void invioMessaggiDozer(){
        switch (DataSaved.Interface_Type){
            case 0:
                //TODO output ECU
                break;

            case 1:
                //CAT
                MyDeviceManager.CanWrite(1, 0x18FE3185, 8,
                        new byte[]{(byte) valueCATL,
                                (byte) 0xFF,
                                dirCAT_L,//F2=Up F1=Down
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF});

                MyDeviceManager.CanWrite(1, 0x18FE3285, 8,
                        new byte[]{(byte) valueCATR,
                                (byte) 0xFF,
                                dirCAT_R,//F2=Up F1=Down
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF});

                MyDeviceManager.CanWrite(1, 0x18FE3385, 8,
                        new byte[]{(byte) valueCATSS,
                                (byte) 0xFF,
                                dirCAT_SS,//F2=Right F1=Left
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF});

                break;


            case 2:
                //JD

                byte[]valoreSX= new byte[]{0x4E,0x20};
                byte[]valoreDX= new byte[]{0x4E,0x20};
                byte[]valoreSS= new byte[]{0x4E,0x20};

                valoreSX= PLC_DataTypes_LittleEndian.U16_to_bytes(valueJDL);
                valoreDX= PLC_DataTypes_LittleEndian.U16_to_bytes(valueJDR);
                valoreSS= PLC_DataTypes_LittleEndian.U16_to_bytes(valueJDSS);
                MyDeviceManager.CanWrite(1, 0x00EFFF85, 8,
                        new byte[]{
                                (byte) 0xF2,
                                (byte) 0x1A,
                                (byte) valoreSX[0],
                                (byte) valoreSX[1],
                                (byte) valoreDX[0],
                                (byte) valoreDX[1],
                                (byte) valoreSS[0],
                                (byte) valoreSS[1]  });

                break;

            case 3:
                //KOMATSU

                byte[] valoreSXK = new byte[]{0, 0};
                byte[] valoreDXK = new byte[]{0, 0};


                valoreSXK = PLC_DataTypes_LittleEndian.U16_to_bytes(valueKomL);
                valoreDXK = PLC_DataTypes_LittleEndian.U16_to_bytes(valueKomR);

                MyDeviceManager.CanWrite(1, 0x0CFF3202, 8,
                        new byte[]{

                                (byte) valoreSXK[0],
                                (byte) valoreSXK[1],
                                (byte) valoreDXK[0],
                                (byte) valoreDXK[1],
                                0,
                                0,
                                0,
                                0});

                break;
        }
    }


}




