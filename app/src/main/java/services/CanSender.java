package services;

import static gui.MyApp.hAlarm;
import static gui.MyApp.isApollo;
import static gui.MyApp.licenseType;
import static gui.dialogs_and_toast.DialogPassword.isTech;
import static gui.dialogs_and_toast.DialogPassword.isTech2;
import static packexcalib.exca.DataSaved.GAIN_LEFT;
import static packexcalib.exca.DataSaved.GAIN_RIGHT;
import static packexcalib.exca.DataSaved.HEADING;
import static packexcalib.exca.DataSaved.HYDRAULIC_CONTROL_POINT_DOZER;
import static packexcalib.exca.DataSaved.HYDRAULIC_CONTROL_POINT_GRADER;
import static packexcalib.exca.DataSaved.OUTPUT_HYDRO;
import static packexcalib.exca.DataSaved.maxSpeedRightDW;
import static packexcalib.exca.DataSaved.maxSpeedRightUP;
import static packexcalib.exca.DataSaved.minSpeedRightDW;
import static packexcalib.exca.DataSaved.minSpeedRightUP;
import static packexcalib.exca.ExcavatorLib.correctRoll;
import static packexcalib.exca.Sensors_Decoder.Deg_Benna_W_Tilt;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static packexcalib.exca.Sensors_Decoder.Deg_Roto;
import static packexcalib.exca.Sensors_Decoder.Deg_Tool_Pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_Tool_Roll;
import static packexcalib.exca.Sensors_Decoder.Deg_boom1;
import static packexcalib.exca.Sensors_Decoder.Deg_bucket;
import static packexcalib.exca.Sensors_Decoder.Deg_pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_roll;
import static packexcalib.exca.Sensors_Decoder.Deg_stick;
import static packexcalib.exca.Sensors_Decoder.Deg_tilt;
import static packexcalib.gnss.CRS_Strings._LOCAL_COORDINATES_FROM_GNSS;
import static packexcalib.gnss.NmeaListener.Est1;
import static packexcalib.gnss.NmeaListener.Nord1;
import static packexcalib.gnss.NmeaListener.Quota1;
import static packexcalib.gnss.NmeaListener.mLat_1;
import static packexcalib.gnss.NmeaListener.mLon_1;
import static serial.SerialReadThread.serialEmpty;
import static services.CanService.Dozer_Auto_Main;
import static services.CanService.Grader_Auto_SS;
import static services.CanService.boom1Disc;
import static services.CanService.boom1OK;
import static services.CanService.boom2Disc;
import static services.CanService.boom2OK;
import static services.CanService.bucketDisc;
import static services.CanService.bucketOK;
import static services.CanService.flagLaser;
import static services.CanService.frameDisc;
import static services.CanService.frameOK;
import static services.CanService.nmeaSTX_Disc;
import static services.CanService.stickDisc;
import static services.CanService.stickOK;
import static services.CanService.tiltDisc;
import static services.CanService.tiltOK;
import static services.CanService.toolDisc;
import static services.CanService.toolOK;
import static services.TriangleService.ctOffGrid;
import static services.TriangleService.ltOffGrid;
import static services.TriangleService.rtOffGrid;
import static utils.MyTypes.DEMO_BAG;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.JOYSTICKS;
import static utils.MyTypes.MC_2D;
import static utils.MyTypes.MC_3D_PRO_AUTO;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import DPAD.DPadHelper;
import DPAD.DPadMapperLeft;
import DPAD.DPadMapperRight;
import cloud.WebSocketPlugin;
import gui.MyApp;
import gui.my_opengl.My3DActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.DrillLib;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.PLC_DataTypes_BigEndian;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import packexcalib.exca.Sensors_Decoder_Drill;
import packexcalib.gnss.NmeaListener;
import utils.MyDeviceManager;
import utils.MyMCUtils;


public class CanSender extends Service {
    public static byte GNSS_MSG = 0x01;
     int heartbitTerzeParti = 0;
    int heartbitTerzeParti_1 = 0;
    int heartbitTerzeParti_2 = 0;
    byte MUX=0x01;
    //private long lastCall = 0;
    public final static double MAX_SCALE = 0.3;
    static boolean sending;
    public static double QL, QC, QR;
    public static double GroundSlope;
    public static int valueCASE_L = 0, valueCASE_R = 0, valueKomL = 0, valueKomR = 0, valueCATL = 0, valueCATR = 0, valueCATSS = 0, valueJDL = 20000, valueJDR = 20000, valueJDSS = 20000;
    public static byte dirCAT_L, dirCAT_R, dirCAT_SS = (byte) 0xF2;
    public static byte dirCase_L = (byte) 0xF2, dirCase_R = (byte) 0xF2;
    public static boolean prepLeft, prepRight;
    static Map<String, Object> payload;
    public static boolean tryingBTCAN = false;
    int connections = 0;
    int isTechCount, startCanopen;
    public static byte onGrade, d0, d1;

    private HandlerThread handlerThread;
    private Handler handler;


    private ScheduledExecutorService senderExecutor500;
    private ScheduledExecutorService senderExecutor2000;
    private ScheduledExecutorService scheduledExecutorService1min;
    Executor mExecutor;
    private static final int THREAD_POOL_SIZE = 1;


    public CanSender() {
    }

    @Override
    public void onCreate() {
        OUTPUT_HYDRO = "";
        payload = new HashMap<>();

        mExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        handlerThread = new HandlerThread("CanSenderWorker");
        handlerThread.start();

        handler = new Handler(handlerThread.getLooper());  // <-- ADESSO funziona!

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
        scheduledExecutorService1min = Executors.newSingleThreadScheduledExecutor();
        senderExecutor500.scheduleAtFixedRate(new AsyncSender500(), 1000, 500, TimeUnit.MILLISECONDS);
        senderExecutor2000.scheduleAtFixedRate(new AsyncSender2000(), 1000, 2000, TimeUnit.MILLISECONDS);

        scheduledExecutorService1min.scheduleAtFixedRate(new AsyncSender1min(), 1000, 60000, TimeUnit.MILLISECONDS);

        handler.post(task); // o postDelayed...


        return START_STICKY;

    }

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            if (MyApp.licenseType == MC_3D_PRO_AUTO) {
                switch (DataSaved.isWL) {
                    case DOZER:
                    case DOZER_SIX:
                    case GRADER:
                        if (MyApp.visibleActivity instanceof My3DActivity) {
                            AutoHandling();
                        }
                        break;
                    case DRILL:
                        //TODO a 25mS
                        break;
                }

            }

            if (DataSaved.isCanOpen == JOYSTICKS) {
                nmeaSTX_Disc = false;
                frameDisc = false;
                boom2Disc = false;
                boom1Disc = false;
                bucketDisc = false;
                tiltDisc = false;
                stickDisc = false;
                toolDisc = false;
                frameOK = true;
                boom1OK = true;
                boom2OK = true;
                stickOK = true;
                bucketOK = true;
                flagLaser = true;
                tiltOK = true;
                toolOK = true;
                final DPadMapperLeft currentLeft = DPadHelper.getInstance().getLeft();
                final DPadMapperRight currentRight = DPadHelper.getInstance().getRight();
                switch (DataSaved.isWL) {
                    case EXCAVATOR:

                        HEADING = currentLeft.getLeftAxisX();
                        ;
                        Deg_stick = currentLeft.getLeftAxisY();
                        Deg_bucket = currentRight.getRightAxisX();
                        Deg_Benna_W_Tilt = currentRight.getRightAxisX();
                        Deg_boom1 = currentRight.getRightAxisY() * -1;
                        Deg_Roto = currentLeft.getLeftYaw();
                        NmeaListener.roof_Orientation = currentRight.getRightYaw();
                        DataSaved.demoEAST = DPadHelper.getInstance().getX();
                        DataSaved.demoNORD = DPadHelper.getInstance().getY();
                        DataSaved.demoZ = DPadHelper.getInstance().getZ();
                        Deg_pitch = (currentRight.getRightHatY() * -1) * 0.5;
                        Deg_tilt = currentRight.getRightHatX();
                        Deg_Boom_Roll = Deg_roll;


                        if (DataSaved.portView == 1) {
                            NmeaListener.roof_Orientation = HEADING;
                        }


                        ExcavatorLib.Excavator();
                        break;

                    case WHEELLOADER:
                        HEADING = currentLeft.getLeftAxisX();
                        Deg_stick = currentLeft.getLeftAxisY();
                        Deg_bucket = currentRight.getRightAxisX();
                        Deg_Benna_W_Tilt = currentRight.getRightAxisX();
                        Deg_boom1 = currentRight.getRightAxisY() * -1;
                        DataSaved.SteerWheel_Result = currentRight.getRightYaw();
                        DataSaved.demoEAST = DPadHelper.getInstance().getX();
                        DataSaved.demoNORD = DPadHelper.getInstance().getY();
                        DataSaved.demoZ = DPadHelper.getInstance().getZ();
                        Deg_pitch = (currentRight.getRightHatY() * -1) * 0.5;
                        Deg_tilt = currentRight.getRightHatX();
                        Deg_Boom_Roll = Deg_roll;
                        ExcavatorLib.Excavator();
                        break;

                    case DOZER:
                    case DOZER_SIX:
                        HEADING = currentRight.getRightHatX();
                        ;
                        Deg_roll = currentRight.getRightAxisX();
                        Deg_pitch = currentRight.getRightAxisY() * -1;
                        DataSaved.demoEAST = DPadHelper.getInstance().getX();
                        DataSaved.demoNORD = DPadHelper.getInstance().getY();
                        DataSaved.demoZ = DPadHelper.getInstance().getZ();
                        ExcavatorLib.Excavator();
                        break;


                    case GRADER:
                        HEADING = currentRight.getRightHatX();
                        ;
                        Deg_roll = currentRight.getRightAxisX();
                        Deg_pitch = currentRight.getRightAxisY() * -1;
                        DataSaved.demoEAST = DPadHelper.getInstance().getX();
                        DataSaved.demoNORD = DPadHelper.getInstance().getY();
                        DataSaved.demoZ = DPadHelper.getInstance().getZ();
                        ExcavatorLib.Excavator();
                        break;

                    case DRILL:
                        HEADING = currentLeft.getLeftAxisX();
                        NmeaListener.roof_Orientation = currentRight.getRightYaw();
                        Deg_pitch = (currentRight.getRightHatY() * -1) * 0.5;
                        Deg_boom1 = currentRight.getRightAxisY() * -1;
                        Deg_Boom_Roll = Deg_roll;
                        Deg_Tool_Pitch = currentRight.getRightAxisX();
                        Deg_Tool_Roll = currentRight.getRightHatX();
                        double len = 0;
                        len = MyMCUtils.myscaleD(currentLeft.getLeftYaw(), -180, 180, -6, 6);
                        Sensors_Decoder_Drill.RopeLen = len;
                        DataSaved.demoEAST = DPadHelper.getInstance().getX();
                        DataSaved.demoNORD = DPadHelper.getInstance().getY();
                        DataSaved.demoZ = DPadHelper.getInstance().getZ();
                        DrillLib.Drill();
                        break;

                }
            }
            handler.postDelayed(this, 25); // esempio
        }
    };


    private class AsyncSender1min implements Runnable {
        @SuppressLint("NewApi")
        @Override
        public void run() {
            int mchType = 0;
            if (DataSaved.isWL > 4) {
                mchType = 0;
            } else {
                mchType = DataSaved.isWL;
            }
            boolean[] bStat = new boolean[8];
            bStat[0] = DataSaved.gpsOk;//vale 1
            bStat[1] = mchType == 0;//exca  vale 2
            bStat[2] = mchType == 1;//wheel  vale 4
            bStat[3] = mchType == 2;//dozer  vale 8
            bStat[4] = mchType == 4;//grader vale 16
            bStat[5] = MyApp.visibleActivity instanceof My3DActivity;  //vale 32
            bStat[6] = CanService.isAuto > 0;//macchina in Automatico  vale 64
            bStat[7] = hAlarm;//allarme attivo  vale 128
            byte status = 0;
            status = PLC_DataTypes_BigEndian.Encode_8_bool_be(bStat);
            double lat = 45.562253273138325, lon = 9.183073136686842;
            if (DataSaved.my_comPort != 4) {
                lat = mLat_1;
                lon = mLon_1;
            }
            if (mLat_1 != 0 && mLon_1 != 0) {
                payload.put("latitude", lat);
                payload.put("longitude", lon);
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
                if (isTech) {
                    isTechCount++;
                    if (isTechCount >= 600) {
                        isTechCount = 0;
                        isTech = false;//Tech LogIn dosabled after 5 minutes inside a Work Activity
                        isTech2 = false;
                    }
                }


            } catch (Exception ignored) {

            }

            try {
                if (licenseType > MC_2D) {
                    switch (DataSaved.my_comPort) {
                        case 0:
                            if (DataSaved.gpsType == 0) {
                                double vrms = 0;
                                try {
                                    vrms = Double.parseDouble(NmeaListener.VRMS_);
                                } catch (NumberFormatException e) {
                                    vrms = 0.002;
                                }
                                if (!nmeaSTX_Disc) {
                                    if (NmeaListener.ggaQuality.equals("4") && vrms < DataSaved.Max_CQ3D && NmeaListener.mch_Hdt_1 != 999.999) {
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
                                    if (DataSaved.S_CRS.equals(_LOCAL_COORDINATES_FROM_GNSS)) {
                                        GNSS_MSG = 0x03;
                                    } else {
                                        GNSS_MSG = 0x01;
                                    }


                                    MyDeviceManager.CanWrite(true, 0, 0x18FF0001, 4, new byte[]{0x20, GNSS_MSG, speed, (byte) 0x03});
                                    connections = 0;
                                }
                            } else if (DataSaved.gpsType == 3) {
                                try {
                                    double vrms = 0;
                                    try {
                                        vrms = Double.parseDouble(NmeaListener.VRMS_);
                                    } catch (NumberFormatException e) {
                                        vrms = 0.002;
                                    }
                                    if (!nmeaSTX_Disc) {
                                        DataSaved.gpsOk = NmeaListener.ggaQuality.equals("4") && vrms < DataSaved.Max_CQ3D && NmeaListener.mch_Hdt_1 != 999.999;
                                    } else {
                                        DataSaved.gpsOk = false;

                                    }
                                } catch (Exception ignored) {

                                }


                                MyDeviceManager.CanWrite(true, 0, 0x718, 8, new byte[]{0x0, 0x4, (byte) 0x58, 0x0, 0x0, 0x0, (byte) 0x5C});//Leica frame request

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
                } else {
                    DataSaved.gpsOk = true;
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
                    if (DataSaved.S_CRS.equals(_LOCAL_COORDINATES_FROM_GNSS)) {
                        GNSS_MSG = 0x03;
                    } else {
                        GNSS_MSG = 0x01;
                    }


                    MyDeviceManager.CanWrite(true, 0, 0x18FF0001, 4, new byte[]{0x20, GNSS_MSG, speed, (byte) 0x03});
                    connections = 0;
                }
            }


        }


    }

    private class AsyncSender2000 implements Runnable {
        @SuppressLint("NewApi")
        @Override
        public void run() {

            if (DataSaved.isWL == DOZER || DataSaved.isWL == DOZER_SIX || DataSaved.isWL == GRADER) {
                if (DataSaved.Interface_Type == 2 || DataSaved.Interface_Type == 0) {
                    if (!(MyApp.visibleActivity instanceof My3DActivity)) {
                        MyDeviceManager.CanWrite(true, 1, 0x18EEFF85, 8,
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
                if (DataSaved.Interface_Type == 4) {
                    if (!(MyApp.visibleActivity instanceof My3DActivity)) {
                        MyDeviceManager.CanWrite(true, 1, 0x18EEFFE6, 8,
                                new byte[]{(byte) 0xF4,
                                        (byte) 0xF0,
                                        (byte) 0xD3,
                                        (byte) 0xE6,
                                        (byte) 0x0,
                                        (byte) 0x82,
                                        (byte) 0x0,
                                        (byte) 0xB0});
                    }
                }
            } else {
                MyDeviceManager.CanWrite(true, 0, 0, 2, new byte[]{1, 0});
            }
            if (DataSaved.isCanOpen == DEMO_BAG) {
                startCanopen++;
                if (startCanopen == 2) {
                    if (isApollo) {
                        MyDeviceManager.CanWrite(true, 0, 0, 2, new byte[]{1, 0});
                    }

                }
                if (startCanopen == 30) {
                    startCanopen = 0;
                }
            }
            if(DataSaved.my_comPort==4){
                NmeaListener.initFromSystemTime();
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

        if (scheduledExecutorService1min != null) {
            scheduledExecutorService1min.shutdown();
        }
        try {
            handler.removeCallbacksAndMessages(null);
            handlerThread.quitSafely();
        } catch (Exception ignored) {

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

        dirCase_R = (byte) 0xF2;
        dirCase_L = (byte) 0xF2;

        if (My3DActivity.diaolgGainHydro == null ||
                My3DActivity.diaolgGainHydro.dialog == null ||
                My3DActivity.diaolgGainHydro.dialog.isShowing()) {

            QL = 0;
            QC = 0;
            QR = 0;
            GroundSlope = correctRoll;
            valueKomL = 0;
            valueKomR = 0;
            valueCATL = 0;
            valueCATR = 0;
            valueCASE_L = 0;
            valueCASE_R = 0;
            valueJDL = 20000;
            valueJDR = 20000;
            valueJDSS = 20000;
            dirCAT_L = (byte) 0xF2;
            dirCAT_R = (byte) 0xF2;
            dirCAT_SS = (byte) 0xF2;
            dirCase_R = (byte) 0xF2;
            dirCase_L = (byte) 0xF2;


        } else {


            switch (DataSaved.isWL) {
                case EXCAVATOR:
                case WHEELLOADER:
                    OUTPUT_HYDRO = "";
                    QL = 0;
                    QC = 0;
                    QR = 0;
                    GroundSlope = correctRoll;
                    valueKomL = 0;
                    valueKomR = 0;
                    valueCATL = 0;
                    valueCATR = 0;
                    valueCASE_L = 0;
                    valueCASE_R = 0;
                    valueJDL = 20000;
                    valueJDR = 20000;
                    valueJDSS = 20000;
                    dirCAT_L = (byte) 0xF2;
                    dirCAT_R = (byte) 0xF2;
                    dirCAT_SS = (byte) 0xF2;
                    dirCase_R = (byte) 0xF2;
                    dirCase_L = (byte) 0xF2;
                    break;
                case GRADER:
                    //GRADER

                    switch (HYDRAULIC_CONTROL_POINT_GRADER) {
                        case 0:
                            QL = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_SX;
                            QC = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_CT;
                            QR = MyMCUtils.ledder(GAIN_RIGHT) * TriangleService.quota3D_DX;

                            handleLamaGrader(TriangleService.quota3D_CT, QC, TriangleService.quota3D_DX, QR, ctOffGrid, rtOffGrid);
                            break;

                        case 1:
                            QL = MyMCUtils.ledder(GAIN_RIGHT) * TriangleService.quota3D_SX;
                            QC = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_CT;
                            QR = MyMCUtils.ledder(GAIN_RIGHT) * TriangleService.quota3D_DX;
                            handleLamaGrader(TriangleService.quota3D_CT, QC, TriangleService.quota3D_SX, QL, ctOffGrid, ltOffGrid);
                            break;

                        case 2://left right
                            QL = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_SX;
                            QC = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_CT;
                            QR = MyMCUtils.ledder(GAIN_RIGHT) * TriangleService.quota3D_DX;
                            handleLamaGrader(TriangleService.quota3D_SX, QL, TriangleService.quota3D_DX, QR, ltOffGrid, rtOffGrid);
                            break;
                    }
                    handleSideShift(DataSaved.bucketEdge, DataSaved.line_Offset);
                    break;

                case DOZER:
                case DOZER_SIX:
                    valueJDSS = 20000;
                    //DOZER

                    QC = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_CT;
                    if (!isInRange(DataSaved.tolleranza_ZL, TriangleService.quota3D_CT) && Math.abs(TriangleService.quota3D_CT) < DataSaved.HYDRAULIC_WINDOW) {

                        if (TriangleService.quota3D_CT < -DataSaved.tolleranza_ZL) {

                            dirCAT_L = (byte) 0xF2;
                            valueCATL = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, MAX_SCALE, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP) & 0xFF;
                            valueCATL = (byte) MyMCUtils.limitInt(valueCATL, 0, 255) & 0xFF;

                            dirCase_L = (byte) 0xF2;
                            valueCASE_L = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, MAX_SCALE, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP) & 0xFF;
                            valueCASE_L = (byte) MyMCUtils.limitInt(valueCASE_L, 0, 250) & 0xFF;


                            valueKomL = (int) MyMCUtils.myscaleD(Math.abs(QC), 0, MAX_SCALE, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP);
                            valueKomL = (int) MyMCUtils.limitInt(valueKomL, 0, 255);
                            valueJDL = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, MAX_SCALE, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP) & 0xFF;
                            valueJDL = (int) MyMCUtils.myscaleD(valueJDL, 0, 255, 20000, 30000);
                            valueJDL = MyMCUtils.limitInt(valueJDL, 20000, 30000);
                            valueJDL = MyMCUtils.limitIntJDL(TriangleService.quota3D_CT, valueJDL, 20000, 30000);


                        } else if (TriangleService.quota3D_CT > DataSaved.tolleranza_ZL) {
                            dirCAT_L = (byte) 0xF1;
                            valueCATL = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, MAX_SCALE, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW) & 0xFF;
                            valueCATL = (byte) MyMCUtils.limitInt(valueCATL, 0, 255) & 0xFF;

                            dirCase_L = (byte) 0xF1;
                            valueCASE_L = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, MAX_SCALE, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW) & 0xFF;
                            valueCASE_L = (byte) MyMCUtils.limitInt(valueCASE_L, 0, 250) & 0xFF;

                            valueKomL = (int) MyMCUtils.myscaleD(Math.abs(QC), 0, MAX_SCALE, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW);
                            valueKomL = (int) MyMCUtils.limitInt(valueKomL, 0, 255);
                            valueKomL = valueKomL * -1;


                            valueJDL = (byte) MyMCUtils.myscaleD(Math.abs(QC), 0, MAX_SCALE, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW) & 0xFF;
                            valueJDL = (int) MyMCUtils.myscaleD(valueJDL, 0, 255, 20000, 10000);

                            valueJDL = MyMCUtils.limitInt(valueJDL, 10000, 20000);
                            valueJDL = MyMCUtils.limitIntJDL(TriangleService.quota3D_CT, valueJDL, 10000, 20000);
                        }
                    } else {
                        valueKomL = 0;
                        valueCATL = 0;
                        valueCASE_L = 0;
                        valueJDL = 20000;
                        dirCAT_L = (byte) 0xF2;
                        dirCAT_SS = (byte) 0xF2;
                        dirCase_L = (byte) 0xF2;
                    }
                    switch (HYDRAULIC_CONTROL_POINT_DOZER) {
                        case 0://CENTER RIGHT
                            GroundSlope = MyMCUtils.bladeSlope(TriangleService.posC, TriangleService.posR);
                            QC = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_CT;
                            QR = MyMCUtils.ledder(GAIN_RIGHT) * TriangleService.quota3D_DX;
                            if (Math.abs(TriangleService.quota3D_CT) > 0.05) {
                                controlloPendenza();

                            } else {

                                if (!isInRange(DataSaved.tolleranza_ZR, TriangleService.quota3D_DX) && Math.abs(TriangleService.quota3D_DX) < DataSaved.HYDRAULIC_WINDOW) {
                                    if (TriangleService.quota3D_DX < -DataSaved.tolleranza_ZR) {

                                        dirCAT_R = (byte) 0xF2;
                                        valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(QR), 0, MAX_SCALE, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                                        valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                                        dirCase_R = (byte) 0xF2;
                                        valueCASE_R = (byte) MyMCUtils.myscaleD(Math.abs(QR), 0, MAX_SCALE, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                                        valueCASE_R = (byte) MyMCUtils.limitInt(valueCASE_R, 0, 250);

                                        valueKomR = (int) MyMCUtils.myscaleD(Math.abs(QR), 0, MAX_SCALE, DataSaved.minSpeedRightUP, DataSaved.maxSpeedRightUP);
                                        valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);

                                        valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(QR), 0, MAX_SCALE, DataSaved.minSpeedRightUP, DataSaved.maxSpeedRightUP) & 0xFF;
                                        valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 30000);
                                        valueJDR = MyMCUtils.limitInt(valueJDR, 20000, 30000);


                                    } else if (TriangleService.quota3D_DX > DataSaved.tolleranza_ZR) {
                                        dirCAT_R = (byte) 0xF1;
                                        valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(QR), 0, MAX_SCALE, DataSaved.minSpeedRightDW, DataSaved.maxSpeedRightDW) & 0xFF;
                                        valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);


                                        dirCase_R = (byte) 0xF1;
                                        valueCASE_R = (byte) MyMCUtils.myscaleD(Math.abs(QR), 0, MAX_SCALE, DataSaved.minSpeedRightDW, DataSaved.maxSpeedRightDW) & 0xFF;
                                        valueCASE_R = (byte) MyMCUtils.limitInt(valueCASE_R, 0, 250);

                                        valueKomR = (int) MyMCUtils.myscaleD(Math.abs(QR), 0, MAX_SCALE, DataSaved.minSpeedRightDW, DataSaved.maxSpeedRightDW);
                                        valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);
                                        valueKomR = valueKomR * -1;


                                        valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(QR), 0, MAX_SCALE, DataSaved.minSpeedRightDW, DataSaved.maxSpeedRightDW) & 0xFF;
                                        valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 10000);

                                        valueJDR = MyMCUtils.limitInt(valueJDR, 10000, 20000);
                                    }
                                } else {
                                    valueKomR = 0;
                                    valueCATR = 0;
                                    valueCASE_R = 0;
                                    valueJDR = 20000;
                                    dirCAT_R = (byte) 0xF2;
                                    dirCase_R = (byte) 0xF2;
                                    dirCAT_SS = (byte) 0xF2;
                                }
                            }

                            break;

                        case 1://CENTER LEFT
                            GroundSlope = MyMCUtils.bladeSlope(TriangleService.posL, TriangleService.posC);
                            QC = MyMCUtils.ledder(GAIN_LEFT) * TriangleService.quota3D_CT;
                            QL = MyMCUtils.ledder(GAIN_RIGHT) * TriangleService.quota3D_SX;
                            if (Math.abs(TriangleService.quota3D_CT) > 0.05) {
                                controlloPendenza();
                            } else {
                                if (!isInRange(DataSaved.tolleranza_ZR, TriangleService.quota3D_SX) && Math.abs(TriangleService.quota3D_SX) < DataSaved.HYDRAULIC_WINDOW) {
                                    if (TriangleService.quota3D_SX < -DataSaved.tolleranza_ZR) {

                                        dirCAT_R = (byte) 0xF1;
                                        valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(QL), 0, MAX_SCALE, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                                        valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                                        dirCase_R = (byte) 0xF1;
                                        valueCASE_R = (byte) MyMCUtils.myscaleD(Math.abs(QL), 0, MAX_SCALE, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                                        valueCASE_R = (byte) MyMCUtils.limitInt(valueCASE_R, 0, 250);

                                        valueKomR = (int) MyMCUtils.myscaleD(Math.abs(QL), 0, MAX_SCALE, DataSaved.minSpeedRightUP, DataSaved.maxSpeedRightUP);
                                        valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);
                                        valueKomR = valueKomR * -1;

                                        valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(QL), 0, MAX_SCALE, DataSaved.minSpeedRightUP, DataSaved.maxSpeedRightUP) & 0xFF;
                                        valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 30000);
                                        valueJDR = MyMCUtils.limitInt(valueJDR, 20000, 30000);
                                        valueJDR = 40000 - valueJDR;


                                    } else if (TriangleService.quota3D_SX > DataSaved.tolleranza_ZR) {
                                        dirCAT_R = (byte) 0xF2;
                                        valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(QL), 0, MAX_SCALE, DataSaved.minSpeedRightDW, DataSaved.maxSpeedRightDW) & 0xFF;
                                        valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                                        dirCase_R = (byte) 0xF2;
                                        valueCASE_R = (byte) MyMCUtils.myscaleD(Math.abs(QL), 0, MAX_SCALE, DataSaved.minSpeedRightDW, DataSaved.maxSpeedRightDW) & 0xFF;
                                        valueCASE_R = (byte) MyMCUtils.limitInt(valueCASE_R, 0, 250);

                                        valueKomR = (int) MyMCUtils.myscaleD(Math.abs(QL), 0, MAX_SCALE, DataSaved.minSpeedRightDW, DataSaved.maxSpeedRightDW);
                                        valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);

                                        valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(QL), 0, MAX_SCALE, DataSaved.minSpeedRightDW, DataSaved.maxSpeedRightDW) & 0xFF;
                                        valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 10000);
                                        valueJDR = MyMCUtils.limitInt(valueJDR, 10000, 20000);

                                        valueJDR = 40000 - valueJDR;
                                    }
                                } else {
                                    valueKomR = 0;
                                    valueCATR = 0;
                                    valueCASE_R = 0;
                                    valueJDR = 20000;
                                    dirCAT_R = (byte) 0xF2;
                                    dirCase_R = (byte) 0xF2;
                                    dirCAT_SS = (byte) 0xF2;
                                }
                            }
                            break;

                    }


                    if (!Grader_Auto_SS) {
                        valueCATSS = 0;
                        valueJDSS = 20000;
                        dirCAT_SS = (byte) 0xF2;
                    }


                    if (prepLeft) {
                        if (Dozer_Auto_Main) {

                            if (Math.abs(TriangleService.quota3D_CT) > DataSaved.HYDRAULIC_WINDOW || ctOffGrid) {
                                valueKomL = 0;
                                valueCATL = 0;
                                valueCASE_L = 0;
                                valueJDL = 20000;
                                dirCAT_L = (byte) 0xF2;
                                dirCase_L = (byte) 0xF2;
                            }

                        } else {

                            valueKomL = 0;
                            valueCATL = 0;
                            valueCASE_L = 0;
                            valueJDL = 20000;
                            dirCAT_L = (byte) 0xF2;
                            dirCase_L = (byte) 0xF2;

                        }
                    } else {

                        valueKomL = 0;
                        valueCATL = 0;
                        valueCASE_L = 0;
                        valueJDL = 20000;
                        dirCAT_L = (byte) 0xF2;
                        dirCase_L = (byte) 0xF2;

                    }


                    if (prepRight) {
                        if (Dozer_Auto_Main) {
                            if (HYDRAULIC_CONTROL_POINT_DOZER == 0) {
                                if (rtOffGrid) {
                                    valueKomR = 0;
                                    valueCATR = 0;
                                    valueCASE_R = 0;
                                    valueJDR = 20000;
                                    dirCAT_R = (byte) 0xF2;
                                    dirCase_R = (byte) 0xF2;
                                }
                            }
                            if (HYDRAULIC_CONTROL_POINT_DOZER == 1) {
                                if (ltOffGrid) {
                                    valueKomR = 0;
                                    valueCATR = 0;
                                    valueCASE_R = 0;
                                    valueJDR = 20000;
                                    dirCAT_R = (byte) 0xF2;
                                    dirCase_R = (byte) 0xF2;
                                }
                            }

                        } else {

                            valueKomR = 0;
                            valueCATR = 0;
                            valueCASE_R = 0;
                            valueJDR = 20000;
                            dirCAT_R = (byte) 0xF2;
                            dirCase_R = (byte) 0xF2;


                        }
                    } else {

                        valueKomR = 0;
                        valueCATR = 0;
                        valueCASE_R = 0;
                        valueJDR = 20000;
                        dirCAT_R = (byte) 0xF2;
                        dirCase_R = (byte) 0xF2;

                    }
                    break;
            }


        }

        invioMessaggiDozer();


    }

    private boolean isInRange(double range, double value) {
        return Math.abs(value) < Math.abs(range);
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

    private void handleLamaGrader(double referenceL, double LL, double referenceR, double RR, boolean checkPointLeft, boolean checkPointRight) {
        if (!isInRange(DataSaved.tolleranza_ZL, referenceL) && Math.abs(TriangleService.quota3D_SX) < DataSaved.HYDRAULIC_WINDOW && !checkPointLeft) {
            if (referenceL < -DataSaved.tolleranza_ZL) {

                dirCAT_L = (byte) 0xF2;
                valueCATL = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, MAX_SCALE, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP) & 0xFF;
                valueCATL = (byte) MyMCUtils.limitInt(valueCATL, 0, 255);

                dirCase_L = (byte) 0xF2;
                valueCASE_L = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, MAX_SCALE, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP) & 0xFF;
                valueCASE_L = (byte) MyMCUtils.limitInt(valueCASE_L, 0, 250);

                valueKomL = (int) MyMCUtils.myscaleD(Math.abs(LL), 0, MAX_SCALE, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP);
                valueKomL = (int) MyMCUtils.limitInt(valueKomL, 0, 255);

                valueJDL = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, MAX_SCALE, DataSaved.minSpeedLeftUP, DataSaved.maxSpeedLeftUP) & 0xFF;
                valueJDL = (int) MyMCUtils.myscaleD(valueJDL, 0, 255, 20000, 30000);
                valueJDL = MyMCUtils.limitInt(valueJDL, 20000, 30000);
                valueJDL = MyMCUtils.limitIntJDL(TriangleService.quota3D_SX, valueJDL, 20000, 30000);


            } else if (referenceL > DataSaved.tolleranza_ZL) {
                dirCAT_L = (byte) 0xF1;
                valueCATL = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, MAX_SCALE, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW) & 0xFF;
                valueCATL = (byte) MyMCUtils.limitInt(valueCATL, 0, 255);

                dirCase_L = (byte) 0xF1;
                valueCASE_L = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, MAX_SCALE, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW) & 0xFF;
                valueCASE_L = (byte) MyMCUtils.limitInt(valueCASE_L, 0, 250);

                valueKomL = (int) MyMCUtils.myscaleD(Math.abs(LL), 0, MAX_SCALE, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW);
                valueKomL = (int) MyMCUtils.limitInt(valueKomL, 0, 255);
                valueKomL = valueKomL * -1;
                valueJDL = (byte) MyMCUtils.myscaleD(Math.abs(LL), 0, MAX_SCALE, DataSaved.minSpeedLeftDW, DataSaved.maxSpeedLeftDW) & 0xFF;
                valueJDL = (int) MyMCUtils.myscaleD(valueJDL, 0, 255, 20000, 10000);
                valueJDL = MyMCUtils.limitInt(valueJDL, 10000, 20000);
                valueJDL = MyMCUtils.limitIntJDL(TriangleService.quota3D_SX, valueJDL, 10000, 20000);
            }
        } else {
            valueKomL = 0;
            valueCATL = 0;
            valueCASE_L = 0;
            valueJDL = 20000;
            dirCAT_L = (byte) 0xF2;
            dirCase_L = (byte) 0xF2;

        }
        if (!isInRange(DataSaved.tolleranza_ZR, referenceR) && Math.abs(TriangleService.quota3D_DX) < DataSaved.HYDRAULIC_WINDOW && !checkPointRight) {
            if (referenceR < -DataSaved.tolleranza_ZR) {

                dirCAT_R = (byte) 0xF2;
                valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, MAX_SCALE, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                dirCase_R = (byte) 0xF2;
                valueCASE_R = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, MAX_SCALE, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                valueCASE_R = (byte) MyMCUtils.limitInt(valueCASE_R, 0, 250);

                valueKomR = (int) MyMCUtils.myscaleD(Math.abs(RR), 0, MAX_SCALE, minSpeedRightUP, maxSpeedRightUP);
                valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);

                valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, MAX_SCALE, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 30000);
                valueJDR = MyMCUtils.limitInt(valueJDR, 20000, 30000);
                valueJDR = MyMCUtils.limitIntJDL(TriangleService.quota3D_DX, valueJDR, 20000, 30000);
                valueJDR = MyMCUtils.limitIntJDL(TriangleService.quota3D_DX, valueJDR, 20000, 30000);


            } else if (referenceR > DataSaved.tolleranza_ZR) {
                dirCAT_R = (byte) 0xF1;
                valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, MAX_SCALE, minSpeedRightDW, maxSpeedRightDW) & 0xFF;
                valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                dirCase_R = (byte) 0xF1;
                valueCASE_R = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, MAX_SCALE, minSpeedRightDW, maxSpeedRightDW) & 0xFF;
                valueCASE_R = (byte) MyMCUtils.limitInt(valueCASE_R, 0, 250);

                valueKomR = (int) MyMCUtils.myscaleD(Math.abs(RR), 0, MAX_SCALE, minSpeedRightDW, maxSpeedRightDW);
                valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);
                valueKomR = valueKomR * -1;


                valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(RR), 0, MAX_SCALE, minSpeedRightDW, maxSpeedRightDW) & 0xFF;
                valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 10000);
                valueJDR = MyMCUtils.limitInt(valueJDR, 10000, 20000);
                valueJDR = MyMCUtils.limitIntJDL(TriangleService.quota3D_DX, valueJDR, 10000, 20000);
            }
        } else {
            valueKomR = 0;
            valueCATR = 0;
            valueCASE_R = 0;
            valueJDR = 20000;
            dirCAT_R = (byte) 0xF2;
            dirCase_R = (byte) 0xF2;

        }
    }

    private void handleSideShift(int bladeEdge, double offset) {
        double dist = 0;
        double rot;
        double rotFix = 360 - ((float) (NmeaListener.mch_Orientation + DataSaved.deltaGPS2));
        rot = TriangleService.orientamentoFreccia + rotFix;
        rot = ((rot + 0) % 360 + 360) % 360;

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
        dist = dist + offset;
        if (Grader_Auto_SS) {
            //TODO sideshift
            switch (DataSaved.Interface_Type) {
                case 0:
                case 2:
                    //SS JD ECU

                    if (!isInRange(DataSaved.tolleranza_XY, dist) && Math.abs(dist) < MAX_SCALE) {
                        if (rot > 240 && rot < 300) {
                            //LEFT minore di 20000
                            valueJDSS = (byte) MyMCUtils.myscaleD(Math.abs(dist), 0, MAX_SCALE, DataSaved.minSpeedSS_A, DataSaved.maxSpeedSS_A) & 0xFF;
                            valueJDSS = (int) MyMCUtils.myscaleD(valueJDSS, 0, 255, 20000, 10000);
                            valueJDSS = MyMCUtils.limitInt(valueJDSS, 10000, 20000);


                        } else if (rot > 30 && rot < 120) {
                            //RIGHT maggiore di 20000
                            valueJDSS = (byte) MyMCUtils.myscaleD(Math.abs(dist), 0, MAX_SCALE, DataSaved.minSpeedSS_B, DataSaved.maxSpeedSS_B) & 0xFF;
                            valueJDSS = (int) MyMCUtils.myscaleD(valueJDSS, 0, 255, 20000, 30000);
                            valueJDSS = MyMCUtils.limitInt(valueJDSS, 20000, 30000);
                        } else {
                            valueCATSS = 0;
                            valueJDSS = 20000;
                            dirCAT_SS = (byte) 0xF2;
                        }
                    } else {
                        valueCATSS = 0;
                        valueJDSS = 20000;
                        dirCAT_SS = (byte) 0xF2;

                    }

                    break;

                case 1:
                    //SS CAT
                    if (!isInRange(DataSaved.tolleranza_XY, dist) && Math.abs(dist) < MAX_SCALE) {
                        if (rot > 30 && rot < 120) {
                            dirCAT_SS = (byte) 0xF2;
                            //RIGHT maggiore di 20000
                            valueCATSS = (byte) MyMCUtils.myscaleD(Math.abs(dist), 0, MAX_SCALE, DataSaved.minSpeedSS_B, DataSaved.maxSpeedSS_B) & 0xFF;
                            valueCATSS = (byte) MyMCUtils.limitInt(valueCATSS, 0, 255);
                        } else if (rot > 240 && rot < 300) {
                            dirCAT_SS = (byte) 0xF1;
                            //LEFT minore di 20000
                            valueCATSS = (byte) MyMCUtils.myscaleD(Math.abs(dist), 0, MAX_SCALE, DataSaved.minSpeedSS_A, DataSaved.maxSpeedSS_A) & 0xFF;
                            valueCATSS = (byte) MyMCUtils.limitInt(valueCATSS, 0, 255);
                        } else {
                            valueCATSS = 0;
                            valueJDSS = 20000;
                            dirCAT_SS = (byte) 0xF2;
                        }

                    } else {
                        valueCATSS = 0;
                        valueJDSS = 20000;
                        dirCAT_SS = (byte) 0xF2;

                    }
                    break;


            }

            //Log.w("SideShift", dist + "    " + rot);
        } else {
            valueCATSS = 0;
            valueJDSS = 20000;
            dirCAT_SS = (byte) 0xF2;
            //Log.d("SideShift", dist + "    " + rot);
        }

    }

    private void invioMessaggiDozer() {
        sending = !sending;
        switch (DataSaved.Interface_Type) {
            case 255:
                try {
                    // output 3e PARTI
                    byte[] left, cent, right;
                    byte[] dist = new byte[2];
                    byte[] Cq = new byte[2];
                    byte satN = 0, qFix = 0;
                    try {
                        satN = Byte.parseByte(NmeaListener.ggaSat);
                    } catch (Exception e) {
                        satN = 0;
                    }
                    try {
                        qFix = Byte.parseByte(NmeaListener.ggaQuality);
                    } catch (Exception e) {
                        qFix = 0;
                    }
                    try {
                        Cq = PLC_DataTypes_LittleEndian.U16_to_bytes((int) (Double.parseDouble(NmeaListener.VRMS_) * 1000));
                    } catch (Exception e) {
                        Cq = new byte[2];
                    }

                    switch (DataSaved.bucketEdge) {
                        case -1:
                            dist = PLC_DataTypes_LittleEndian.S16_to_bytes(MyMCUtils.limitShort((short) (TriangleService.dist3D_SX * 1000), (short) -32768, (short) 32767));
                            break;
                        case 0:
                            dist = PLC_DataTypes_LittleEndian.S16_to_bytes(MyMCUtils.limitShort((short) (TriangleService.dist3D_SX * 1000), (short) -32768, (short) 32767));
                            break;
                        case 1:
                            dist = PLC_DataTypes_LittleEndian.S16_to_bytes(MyMCUtils.limitShort((short) (TriangleService.dist3D_SX * 1000), (short) -32768, (short) 32767));
                            break;
                    }

                    if(ltOffGrid){
                        left=new byte[]{0x0, (byte) 0x80};
                    }else {
                        left = PLC_DataTypes_LittleEndian.S16_to_bytes(MyMCUtils.limitShort((short) (TriangleService.quota3D_SX * 1000), (short) -32768, (short) 32767));
                    }

                    if(ctOffGrid){
                        cent=new byte[]{0x0, (byte) 0x80};
                    }else {
                        cent = PLC_DataTypes_LittleEndian.S16_to_bytes(MyMCUtils.limitShort((short) (TriangleService.quota3D_CT * 1000), (short) -32768, (short) 32767));
                    }

                    if(rtOffGrid){
                        right=new byte[]{0x0, (byte) 0x80};
                    }else {
                        right = PLC_DataTypes_LittleEndian.S16_to_bytes(MyMCUtils.limitShort((short) (TriangleService.quota3D_DX * 1000), (short) -32768, (short) 32767));

                    }

                    byte[]mainfall=PLC_DataTypes_LittleEndian.S16_to_bytes(TriangleService.Mainfall_Value);
                    byte[]offset=PLC_DataTypes_LittleEndian.S16_to_bytes(MyMCUtils.limitShort((short) (DataSaved.offsetH* 1000), (short) -32768, (short) 32767));
                    byte[]dgm_left = PLC_DataTypes_LittleEndian.S16_to_bytes(MyMCUtils.limitShort((short) (TriangleService.DGM_Letf * 1000), (short) -32768, (short) 32767));
                    byte[]dgm_right = PLC_DataTypes_LittleEndian.S16_to_bytes(MyMCUtils.limitShort((short) (TriangleService.DGM_Right * 1000), (short) -32768, (short) 32767));
                    heartbitTerzeParti++;

                    if(DataSaved.gpsOk) {
                        if (heartbitTerzeParti % 2 == 0) {
                            heartbitTerzeParti_1++;
                            heartbitTerzeParti_1 = heartbitTerzeParti_1 % 255;
                            MUX = 0x01;
                            MyDeviceManager.CanWrite(sending, 1, 0x154, 8, new byte[]{
                                    MUX,
                                    (byte) heartbitTerzeParti_1,
                                    left[0],
                                    left[1],
                                    right[0],
                                    right[1],
                                    mainfall[0],
                                    mainfall[1]
                            });
                        } else {
                            heartbitTerzeParti_2++;
                            heartbitTerzeParti_2 = heartbitTerzeParti_2 % 255;
                            MUX = 0x03;
                            MyDeviceManager.CanWrite(!sending, 1, 0x154, 8, new byte[]{
                                    MUX,
                                    (byte) heartbitTerzeParti_2,
                                    dgm_left[0],
                                    dgm_left[1],
                                    dgm_right[0],
                                    dgm_right[1],
                                    offset[0],
                                    offset[1]


                            });
                        }
                    }else {
                        MyDeviceManager.CanWrite(sending,1,0x154,8,new byte[]{
                                0x01,
                                (byte) heartbitTerzeParti_1,
                                0x0,
                                (byte) 0x80,
                                0x0,
                                (byte) 0x80,
                                0x0,
                                (byte) 0x80
                        });
                        MyDeviceManager.CanWrite(sending,1,0x154,8,new byte[]{
                                0x03,
                                (byte) heartbitTerzeParti_2,
                                0x0,
                                (byte) 0x80,
                                0x0,
                                (byte) 0x80,
                                0x0,
                                (byte) 0x80
                        });
                    }


                } catch (Exception e) {
                    MyDeviceManager.CanWrite(sending,1,0x154,8,new byte[]{
                            0x01,
                            (byte) heartbitTerzeParti_1,
                            0x0,
                            (byte) 0x80,
                            0x0,
                            (byte) 0x80,
                            0x0,
                            (byte) 0x80
                    });
                    MyDeviceManager.CanWrite(sending,1,0x154,8,new byte[]{
                            0x03,
                            (byte) heartbitTerzeParti_2,
                            0x0,
                            (byte) 0x80,
                            0x0,
                            (byte) 0x80,
                            0x0,
                            (byte) 0x80
                    });
                }

                break;
            case 0:
                //ECU
                byte[] valoreSX0 = new byte[]{0x4E, 0x20};
                byte[] valoreDX0 = new byte[]{0x4E, 0x20};
                byte[] valoreSS0 = new byte[]{0x4E, 0x20};
                int resultL, resultR, resultSS;
                if (DataSaved.REVERSE_LEFT == 1) {
                    resultL = 40000 - valueJDL;
                } else {
                    resultL = valueJDL;
                }
                if (DataSaved.REVERSE_RIGHT == 1) {
                    resultR = 40000 - valueJDR;
                } else {
                    resultR = valueJDR;
                }
                if (DataSaved.REVERSE_SS == 1) {
                    resultSS = 40000 - valueJDSS;
                } else {
                    resultSS = valueJDSS;
                }

                valoreSX0 = PLC_DataTypes_LittleEndian.U16_to_bytes(resultL);
                valoreDX0 = PLC_DataTypes_LittleEndian.U16_to_bytes(resultR);
                valoreSS0 = PLC_DataTypes_LittleEndian.U16_to_bytes(resultSS);
                OUTPUT_HYDRO = "L:" + resultL + "\n" + "R:" + resultR;
                byte mD0 = 0;
                if (DataSaved.gpsOk) {
                    mD0 = 1;
                }
                MyDeviceManager.CanWrite(sending, 1, 0x00EFFF85, 8,
                        new byte[]{
                                mD0,
                                (byte) 0x1A,
                                (byte) valoreSX0[0],
                                (byte) valoreSX0[1],
                                (byte) valoreDX0[0],
                                (byte) valoreDX0[1],
                                (byte) valoreSS0[0],
                                (byte) valoreSS0[1]});


                break;

            case 1:
                //CAT
                OUTPUT_HYDRO = "L:" + valueCATL + "\n" + "R:" + valueCATR;
                MyDeviceManager.CanWrite(sending && DataSaved.gpsOk, 1, 0x18FE3185, 8,
                        new byte[]{(byte) valueCATL,
                                (byte) 0xFF,
                                dirCAT_L,//F2=Up F1=Down
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF});

                MyDeviceManager.CanWrite(sending && DataSaved.gpsOk, 1, 0x18FE3285, 8,
                        new byte[]{(byte) valueCATR,
                                (byte) 0xFF,
                                dirCAT_R,//F2=Up F1=Down
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF});

                MyDeviceManager.CanWrite(sending && DataSaved.gpsOk, 1, 0x18FE3385, 8,
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

                byte[] valoreSX = new byte[]{0x4E, 0x20};
                byte[] valoreDX = new byte[]{0x4E, 0x20};
                byte[] valoreSS = new byte[]{0x4E, 0x20};

                int resultL2, resultR2, resultSS2;
                if (DataSaved.REVERSE_LEFT == 1) {
                    resultL2 = 40000 - valueJDL;
                } else {
                    resultL2 = valueJDL;
                }
                if (DataSaved.REVERSE_RIGHT == 1) {
                    resultR2 = 40000 - valueJDR;
                } else {
                    resultR2 = valueJDR;
                }
                if (DataSaved.REVERSE_SS == 1) {
                    resultSS2 = 40000 - valueJDSS;
                } else {
                    resultSS2 = valueJDSS;
                }
                valoreSX = PLC_DataTypes_LittleEndian.U16_to_bytes(resultL2);
                valoreDX = PLC_DataTypes_LittleEndian.U16_to_bytes(resultR2);
                valoreSS = PLC_DataTypes_LittleEndian.U16_to_bytes(resultSS2);
                OUTPUT_HYDRO = "L:" + resultL2 + "\n" + "R:" + resultR2;
                MyDeviceManager.CanWrite(sending && DataSaved.gpsOk, 1, 0x00EFFF85, 8,
                        new byte[]{
                                (byte) 0xF2,
                                (byte) 0x1A,
                                (byte) valoreSX[0],
                                (byte) valoreSX[1],
                                (byte) valoreDX[0],
                                (byte) valoreDX[1],
                                (byte) valoreSS[0],
                                (byte) valoreSS[1]});

                break;

            case 3:
                //KOMATSU

                byte[] valoreSXK = new byte[]{0, 0};
                byte[] valoreDXK = new byte[]{0, 0};


                valoreSXK = PLC_DataTypes_LittleEndian.U16_to_bytes(valueKomL);
                valoreDXK = PLC_DataTypes_LittleEndian.U16_to_bytes(valueKomR);
                OUTPUT_HYDRO = "L:" + valueKomL + "\n" + "R:" + valueKomR;
                MyDeviceManager.CanWrite(sending && DataSaved.gpsOk, 1, 0x0CFF3202, 8,
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

            case 4:
                //CASE
                OUTPUT_HYDRO = "L:" + valueCASE_L + "\n" + "R:" + valueCASE_R;
                MyDeviceManager.CanWrite(sending && DataSaved.gpsOk, 1, 0x18FE31E6, 8,
                        new byte[]{(byte) valueCASE_L,
                                (byte) 0xFF,
                                dirCase_L,//F2=Up F1=Down
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF});

                MyDeviceManager.CanWrite(sending && DataSaved.gpsOk, 1, 0x18FE32E6, 8,
                        new byte[]{(byte) valueCASE_R,
                                (byte) 0xFF,
                                dirCase_R,//F2=Up F1=Down
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF,
                                (byte) 0xFF});

                break;
        }
    }

    private boolean isInRangeAng(double currentAngle, double setPoint, double tolerance) {
        return Math.abs(currentAngle - setPoint) <= tolerance;
    }

    private void controlloPendenza() {
        if (!isInRangeAng(correctRoll, GroundSlope, DataSaved.tolleranza_Slope)) {
            double deviationPure = deviationFromSetpoint(correctRoll, GroundSlope, DataSaved.tolleranza_Slope);
            double deviation = MyMCUtils.ledder(GAIN_RIGHT) * deviationPure;


            if (deviationPure > DataSaved.tolleranza_Slope) {
                dirCAT_R = (byte) 0xF2;
                valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                dirCase_R = (byte) 0xF2;
                valueCASE_R = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                valueCASE_R = (byte) MyMCUtils.limitInt(valueCASE_R, 0, 250);

                valueKomR = (int) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightUP, maxSpeedRightUP);
                valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);


                valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 30000);
                valueJDR = MyMCUtils.limitInt(valueJDR, 20000, 30000);

            } else if (deviationPure < -DataSaved.tolleranza_Slope) {
                dirCAT_R = (byte) 0xF1;
                valueCATR = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightDW, maxSpeedRightDW) & 0xFF;
                valueCATR = (byte) MyMCUtils.limitInt(valueCATR, 0, 255);

                dirCase_R = (byte) 0xF1;
                valueCASE_R = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightDW, maxSpeedRightDW) & 0xFF;
                valueCASE_R = (byte) MyMCUtils.limitInt(valueCASE_R, 0, 250);


                valueKomR = (int) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightDW, maxSpeedRightDW);
                valueKomR = (int) MyMCUtils.limitInt(valueKomR, 0, 255);
                valueKomR = valueKomR * -1;


                valueJDR = (byte) MyMCUtils.myscaleD(Math.abs(deviation), 0, 30, minSpeedRightUP, maxSpeedRightUP) & 0xFF;
                valueJDR = (int) MyMCUtils.myscaleD(valueJDR, 0, 255, 20000, 10000);
                valueJDR = MyMCUtils.limitInt(valueJDR, 10000, 20000);
            } else {
                valueKomR = 0;
                valueCATR = 0;
                dirCase_R = (byte) 0xF2;
                valueCASE_R = 0;
                valueJDR = 20000;
                dirCAT_R = (byte) 0xF2;
                dirCAT_SS = (byte) 0xF2;
            }

        } else {
            valueKomR = 0;
            valueCATR = 0;
            dirCase_R = (byte) 0xF2;
            valueCASE_R = 0;
            valueJDR = 20000;
            dirCAT_R = (byte) 0xF2;
            dirCAT_SS = (byte) 0xF2;
        }

    }


}




