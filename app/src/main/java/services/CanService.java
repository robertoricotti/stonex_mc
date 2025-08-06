package services;

import static packexcalib.exca.Sensors_Decoder.isMobaTilt;
import static packexcalib.gnss.CRS_Strings._NONE;
import static serial.OpenSerialPort.mOpened;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

import event_bus.CanEvents;
import gui.MyApp;
import gui.debug_ecu.Can_Msg_Debug;
import gui.gps.Nuovo_Gps;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.gnss.NmeaListener;
import serial.OpenSerialPort;
import serial.SerialPortManager;
import utils.CPCanHelper;
import utils.CanFileReceiver;
import utils.FileCreator;
import utils.MyDeviceManager;

public class CanService extends Service {
    public CanService() {
    }

    public static int isAuto, errorEcu, hydraMachineType, valveType, left_Rise_min, left_Rise_Max, left_Lower_min, left_Lower_Max,
            right_Rise_min, right_Rise_Max, right_Lower_min, right_Lower_Max, hydr_Window, left_Gain, right_Gain, elevationDB, slopeDB, reverseLeft, reverseRight;
    public static int altosx, centrosx, bassosx, altodx, centrodx, m, bassodx;
    public static boolean nolaserbeam_L, nolaserbeam_R, ECU_Connected, isAutoL, isAutoR, outW_L, outW_R;
    public static boolean frameOK, boom1OK, boom2OK, stickOK, bucketOK, tiltOK;
    CanFileReceiver receiver = new CanFileReceiver();
    public static boolean boom1Disc, boom2Disc, stickDisc, bucketDisc, frameDisc, tiltDisc, nmeaSTX_Disc;
    public static boolean CanServiceState = false;
    int dlc;

    static double spigoloX, spigoloY, spigoloZ;
    static CPCanHelper cpCanHelper;

    @Override
    public void onCreate() {

        cpCanHelper = CPCanHelper.getInstance();
        nmeaSTX_Disc = true;
        frameDisc = true;
        boom2Disc = true;
        boom1Disc = true;
        bucketDisc = true;
        tiltDisc = true;
        stickDisc = true;

        frameOK = false;
        boom1OK = false;
        boom2OK = false;
        stickOK = false;
        bucketOK = false;
        tiltOK = false;
        ECU_Connected = false;
        if (DataSaved.lrFrame != 0) {
            handler_frame.postDelayed(timeoutRunnable_frame, 3000);
        }
        if (DataSaved.lrBoom1 != 0) {
            handler_b1.postDelayed(timeoutRunnable_b1, 3000);
        }
        if (DataSaved.lrBoom2 != 0) {
            handler_b2.postDelayed(timeoutRunnable_b2, 3000);
        }
        if (DataSaved.lrStick != 0) {
            handler_st.postDelayed(timeoutRunnable_st, 3000);
        }
        if (DataSaved.lrBucket != 0) {
            handler_bk.postDelayed(timeoutRunnable_bk, 3000);
        }
        if (DataSaved.lrTilt != 0) {
            handler_tl.postDelayed(timeoutRunnable_tl, 3000);
        }
        handler_ECU_Connected.postDelayed(timeoutRunnable_CU_Connected, 3000);
        super.onCreate();
    }

    public void OnCan(int channel, byte[] msg, int dlc, int id) {
        {
            try {

                String s = null;
                String hexString = Integer.toHexString(id).toUpperCase();

                hexString = "0x" + hexString;
                int[] positiveValue = new int[dlc];
                for (int i = 0; i < dlc; i++) {
                    // Converti il byte in un valore compreso tra 0 e 255
                    positiveValue[i] = msg[i] & 0xFF;
                    s = "ID:   " + hexString + "   DLC: " + dlc + "     " + Arrays.toString(positiveValue).replace("[", " ").replace("]", " ") + " ";

                }
                if (MyApp.visibleActivity instanceof Nuovo_Gps || MyApp.visibleActivity instanceof Can_Msg_Debug) {
                    EventBus.getDefault().post(new CanEvents(channel, s, id, msg));
                }
            } catch (Exception e) {

            }

            if (DataSaved.isCanOpen == 1) {
                if (id == 1409) {
                    DataSaved.damp_Fr = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[4], msg[5]});
                }
                if (id == 1410) {
                    DataSaved.damp_B1 = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[4], msg[5]});
                }
                if (id == 1411) {
                    DataSaved.damp_B2 = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[4], msg[5]});
                }
                if (id == 1412) {
                    DataSaved.damp_St = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[4], msg[5]});
                }
                if (id == 1413) {
                    DataSaved.damp_Bk = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[4], msg[5]});
                }
                if (id == 1414) {
                    DataSaved.damp_Tl = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[4], msg[5]});
                }
            } else if (DataSaved.isCanOpen == 3) {
                if (id == 1409) {
                    DataSaved.damp_Fr = msg[4];
                }
                if (id == 1410) {
                    DataSaved.damp_B1 = msg[4];
                }
                if (id == 1415) {
                    DataSaved.damp_B2 = msg[4];
                }
                if (id == 1412) {
                    DataSaved.damp_St = msg[4];
                }
                if (id == 1413) {
                    DataSaved.damp_Bk = msg[4];
                }
                if (id == 1414) {
                    DataSaved.damp_Tl = msg[4];
                }

            }

            if (id == 2066) {
                Log.d("ECU", Arrays.toString(msg));
                ECU_Connected = true;
                handler_ECU_Connected.removeCallbacks(timeoutRunnable_CU_Connected);
                handler_ECU_Connected.postDelayed(timeoutRunnable_CU_Connected, 1000);
                switch (msg[0]) {
                    case 0:
                        hydraMachineType = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 1:
                        valveType = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 2:
                        left_Rise_min = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 3:
                        left_Rise_Max = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 4:
                        left_Lower_min = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 5:
                        left_Lower_Max = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 6:
                        reverseLeft = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;

                    case 7:
                        right_Rise_min = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 8:
                        right_Rise_Max = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 9:
                        right_Lower_min = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 10:
                        right_Lower_Max = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 11:
                        reverseRight = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 12:
                        hydr_Window = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 13:
                        left_Gain = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 14:
                        right_Gain = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 15:
                        elevationDB = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;
                    case 16:
                        slopeDB = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        break;

                }

                boolean[] stat = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[3]);
                isAutoL = stat[7];
                isAutoR = stat[6];
                outW_L = stat[5];
                outW_R = stat[4];


            }

            if (id == 0x18FF0510 && MyDeviceManager.serialCom(DataSaved.my_comPort).equals("CAN")) {

                NmeaListener.NmeaSTX(id, msg);
                nmeaSTX_Disc = false;
                try {
                    handler_nmeaSTX.removeCallbacks(timeoutRunnable_nmea2k);

                } catch (Exception e) {
                }
                handler_nmeaSTX.postDelayed(timeoutRunnable_nmea2k, 5000);
            }
            if (id == 0x18FFA110) {
                if (msg[1] == 0x11) {
                    switch (msg[2]) {

                        case 0:

                            DataSaved.radioMode = 1;

                            break;
                        case 1:

                            DataSaved.radioMode = 0;

                            break;
                    }
                }
            }


            double hdt = 0;
            double gps_type = 0;

            if (DataSaved.portView == 0) {
                hdt = 0;
                gps_type = 0;
                spigoloX = ExcavatorLib.bucketCoord[0];
                spigoloY = ExcavatorLib.bucketCoord[1];
                spigoloZ = ExcavatorLib.bucketCoord[2];
            } else if (DataSaved.portView == 1) {
                //
                if (DataSaved.useYawFrame == 0) {
                    hdt = NmeaListener.mch_Hdt - DataSaved.offsetHDT;
                } else {
                    hdt = Sensors_Decoder.Deg_Yaw_Frame - DataSaved.offsetHDT;
                }
                if (hdt < 0) {
                    hdt = hdt + 360;
                }
                gps_type = 0;
                switch (DataSaved.bucketEdge) {
                    case 1:
                        spigoloX = ExcavatorLib.bucketRightCoord[0];//x
                        spigoloY = ExcavatorLib.bucketRightCoord[1];//y
                        spigoloZ = ExcavatorLib.bucketRightCoord[2];//z
                        break;
                    case 0:
                        spigoloX = ExcavatorLib.bucketCoord[0];
                        spigoloY = ExcavatorLib.bucketCoord[1];
                        spigoloZ = ExcavatorLib.bucketCoord[2];
                        break;

                    case -1:
                        spigoloX = ExcavatorLib.bucketLeftCoord[0];
                        spigoloY = ExcavatorLib.bucketLeftCoord[1];
                        spigoloZ = ExcavatorLib.bucketLeftCoord[2];
                        break;
                    default:
                        spigoloX = ExcavatorLib.bucketCoord[0];
                        spigoloY = ExcavatorLib.bucketCoord[1];
                        spigoloZ = ExcavatorLib.bucketCoord[2];
                        break;
                }


            } else if (DataSaved.portView >= 2) {
                hdt = 0;
                gps_type = 1;
                switch (DataSaved.bucketEdge) {
                    case -1:
                        spigoloX = ExcavatorLib.bucketLeftCoord[0];
                        spigoloY = ExcavatorLib.bucketLeftCoord[1];
                        spigoloZ = ExcavatorLib.bucketLeftCoord[2];
                        break;

                    case 0:
                        spigoloX = ExcavatorLib.bucketCoord[0];
                        spigoloY = ExcavatorLib.bucketCoord[1];
                        spigoloZ = ExcavatorLib.bucketCoord[2];
                        break;

                    case 1:
                        spigoloX = ExcavatorLib.bucketRightCoord[0];
                        spigoloY = ExcavatorLib.bucketRightCoord[1];
                        spigoloZ = ExcavatorLib.bucketRightCoord[2];
                        break;
                    default:
                        spigoloX = ExcavatorLib.bucketCoord[0];
                        spigoloY = ExcavatorLib.bucketCoord[1];
                        spigoloZ = ExcavatorLib.bucketCoord[2];
                        break;

                }

            }


            if(DataSaved.isWL>1){
                DataSaved.deltaZ = DataSaved.altezzaLama + DataSaved.altezzaPali;
            }
            double[] arrayData = new double[]{hdt, gps_type, DataSaved.deltaX, DataSaved.deltaY, DataSaved.deltaZ, DataSaved.deltaGPS2, spigoloX, spigoloY, spigoloZ, 0};
            Sensors_Decoder.Moba_G2_Decoder_Update(id, msg, arrayData);

            switch (DataSaved.isCanOpen) {
                case 1:
                case 3:
                    //moba o tsm
                    if (DataSaved.isWL < 2) {
                        if (id == 897) {
                            frameOK = true;
                            handler_frameOK.removeCallbacks(timeoutRunnable_frameOK);
                            handler_frameOK.postDelayed(timeoutRunnable_frameOK, 3000);
                        }
                        if (id == 898) {
                            boom1OK = true;
                            handler_boom1OK.removeCallbacks(timeoutRunnable_boom1OK);
                            handler_boom1OK.postDelayed(timeoutRunnable_boom1OK, 3000);
                        }
                        if (id == 903) {
                            boom2OK = true;
                            handler_boom2OK.removeCallbacks(timeoutRunnable_boom2OK);
                            handler_boom2OK.postDelayed(timeoutRunnable_boom2OK, 3000);
                        }
                        if (id == 900) {
                            stickOK = true;
                            handler_stickOK.removeCallbacks(timeoutRunnable_stickOK);
                            handler_stickOK.postDelayed(timeoutRunnable_stickOK, 3000);
                        }
                        if (id == 901) {
                            bucketOK = true;
                            handler_bucketOK.removeCallbacks(timeoutRunnable_bucketOK);
                            handler_bucketOK.postDelayed(timeoutRunnable_bucketOK, 3000);
                        }
                        if (id == 902) {
                            tiltOK = true;
                            handler_tiltOK.removeCallbacks(timeoutRunnable_tiltOK);
                            handler_tiltOK.postDelayed(timeoutRunnable_tiltOK, 3000);
                        }
                        /*


                         */
                        if ((id == 897 || id == 90181733) && DataSaved.lrFrame != 0) {
                            frameDisc = false;
                            handler_frame.removeCallbacks(timeoutRunnable_frame);
                            handler_frame.postDelayed(timeoutRunnable_frame, 3000);
                        }
                        if ((id == 898) && DataSaved.lrBoom1 != 0) {
                            boom1Disc = false;
                            handler_b1.removeCallbacks(timeoutRunnable_b1);
                            handler_b1.postDelayed(timeoutRunnable_b1, 3000);
                        }
                        if ((id == 903) && DataSaved.lrBoom2 != 0) {

                            boom2Disc = false;
                            handler_b2.removeCallbacks(timeoutRunnable_b2);
                            handler_b2.postDelayed(timeoutRunnable_b2, 3000);
                        }
                        if ((id == 900) && DataSaved.lrStick != 0) {
                            stickDisc = false;
                            handler_st.removeCallbacks(timeoutRunnable_st);
                            handler_st.postDelayed(timeoutRunnable_st, 3000);
                        }
                        if ((id == 901) && DataSaved.lrBucket != 0) {
                            bucketDisc = false;
                            handler_bk.removeCallbacks(timeoutRunnable_bk);
                            handler_bk.postDelayed(timeoutRunnable_bk, 3000);
                        }
                        if ((id == 902 || id == 90181738) && DataSaved.lrTilt != 0) {
                            tiltOK = true;
                            tiltDisc = false;
                            handler_tl.removeCallbacks(timeoutRunnable_tl);
                            handler_tl.postDelayed(timeoutRunnable_tl, 3000);
                        }
                    } else {
                        if ((id == 902 || id == 90181738 || id == 90181733) && DataSaved.lrBucket != 0) {
                            tiltOK = true;
                            tiltDisc = false;
                            handler_tl.removeCallbacks(timeoutRunnable_tl);
                            handler_tl.postDelayed(timeoutRunnable_tl, 3000);
                        }
                    }

                    break;
                case 2:
                case 4:
                    //tsm angolari o demo bag

                    if (id == 385 && DataSaved.lrFrame != 0) {
                        frameDisc = false;
                        handler_frame.removeCallbacks(timeoutRunnable_frame);
                        handler_frame.postDelayed(timeoutRunnable_frame, 3000);
                    }
                    if (id == 386 && DataSaved.lrBoom1 != 0) {
                        boom1Disc = false;
                        handler_b1.removeCallbacks(timeoutRunnable_b1);
                        handler_b1.postDelayed(timeoutRunnable_b1, 3000);
                    }
                    if (id == 391 && DataSaved.lrBoom2 != 0) {
                        boom2Disc = false;
                        handler_b2.removeCallbacks(timeoutRunnable_b2);
                        handler_b2.postDelayed(timeoutRunnable_b2, 3000);
                    }
                    if (id == 388 && DataSaved.lrStick != 0) {
                        stickDisc = false;
                        handler_st.removeCallbacks(timeoutRunnable_st);
                        handler_st.postDelayed(timeoutRunnable_st, 3000);
                    }
                    if (id == 389 && DataSaved.lrBucket != 0) {
                        bucketDisc = false;
                        handler_bk.removeCallbacks(timeoutRunnable_bk);
                        handler_bk.postDelayed(timeoutRunnable_bk, 3000);
                    }
                    if (id == 902 && DataSaved.lrTilt != 0) {
                        tiltDisc = false;
                        handler_tl.removeCallbacks(timeoutRunnable_tl);
                        handler_tl.postDelayed(timeoutRunnable_tl, 3000);
                    }
                    break;


                case 5:
                    if (id == 0X195) {
                        frameOK = true;
                        handler_frameOK.removeCallbacks(timeoutRunnable_frameOK);
                        handler_frameOK.postDelayed(timeoutRunnable_frameOK, 3000);
                    }
                    if (id == 0X195) {
                        boom1OK = true;
                        handler_boom1OK.removeCallbacks(timeoutRunnable_boom1OK);
                        handler_boom1OK.postDelayed(timeoutRunnable_boom1OK, 3000);
                    }

                    if (id == 0X1F0) {
                        stickOK = true;
                        handler_stickOK.removeCallbacks(timeoutRunnable_stickOK);
                        handler_stickOK.postDelayed(timeoutRunnable_stickOK, 3000);
                    }
                    if (id == 0X195) {
                        bucketOK = true;
                        handler_bucketOK.removeCallbacks(timeoutRunnable_bucketOK);
                        handler_bucketOK.postDelayed(timeoutRunnable_bucketOK, 3000);
                    }
                    if (id == 0X195) {
                        tiltOK = true;
                        handler_tiltOK.removeCallbacks(timeoutRunnable_tiltOK);
                        handler_tiltOK.postDelayed(timeoutRunnable_tiltOK, 3000);
                    }
                    if (id == 917) {
                        boom1Disc = false;
                        bucketDisc = false;
                        tiltDisc = false;
                    }
                    if (id == 1008) {
                        frameDisc = false;
                        stickDisc = false;
                    }
                    break;
            }

        }
        if (id == 0x7DF) {
            receiver.receivePacket(msg);
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        CanServiceState = true;
        switch (DataSaved.my_comPort) {
            case 0:
                try {
                    if (mOpened) {
                        mOpened = false;
                        SerialPortManager.instance().close();
                    }
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
                            speed = 1;
                            break;

                    }
                    if (mOpened) {
                        mOpened = false;
                        SerialPortManager.instance().close();
                    }
                    byte msg=0x03;



                    MyDeviceManager.CanWrite(0, 0x18FF0001, 4, new byte[]{0x20, msg, speed, (byte) 0x03});
                } catch (Exception e) {
                    System.out.println(e);
                }
                break;
            case 1:
            case 2:
            case 3:
                new OpenSerialPort(this);
                break;
        }


        cpCanHelper.start(new CPCanHelper.Action() {
            @Override
            public void execute(int channel, int id, byte[] data) {
                if (data != null) {
                    dlc = data.length;
                    OnCan(channel, data, dlc, id);
                }

            }
        });


        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Handler handler_nmeaSTX = new Handler();
    private final Runnable timeoutRunnable_nmea2k = new Runnable() {
        @Override
        public void run() {
            nmeaSTX_Disc = true;
        }
    };
    private final Handler handler_frame = new Handler();
    private final Runnable timeoutRunnable_frame = new Runnable() {
        @Override
        public void run() {
            frameDisc = true;
        }
    };
    private final Handler handler_b1 = new Handler();
    private final Runnable timeoutRunnable_b1 = new Runnable() {
        @Override
        public void run() {
            boom1Disc = true;

        }
    };
    private final Handler handler_b2 = new Handler();
    private final Runnable timeoutRunnable_b2 = new Runnable() {
        @Override
        public void run() {
            boom2Disc = true;

        }
    };
    private final Handler handler_st = new Handler();
    private final Runnable timeoutRunnable_st = new Runnable() {
        @Override
        public void run() {
            stickDisc = true;

        }
    };
    private final Handler handler_bk = new Handler();
    private final Runnable timeoutRunnable_bk = new Runnable() {
        @Override
        public void run() {
            bucketDisc = true;

        }
    };
    private final Handler handler_tl = new Handler();
    private final Runnable timeoutRunnable_tl = new Runnable() {
        @Override
        public void run() {
            tiltDisc = true;
            isMobaTilt = false;
            tiltOK=false;

        }
    };

    private final Handler handler_frameOK = new Handler();
    private final Runnable timeoutRunnable_frameOK = new Runnable() {
        @Override
        public void run() {
            frameOK = false;

        }
    };
    private final Handler handler_boom1OK = new Handler();
    private final Runnable timeoutRunnable_boom1OK = new Runnable() {
        @Override
        public void run() {
            boom1OK = false;

        }
    };
    private final Handler handler_boom2OK = new Handler();
    private final Runnable timeoutRunnable_boom2OK = new Runnable() {
        @Override
        public void run() {
            boom2OK = false;

        }
    };
    private final Handler handler_stickOK = new Handler();
    private final Runnable timeoutRunnable_stickOK = new Runnable() {
        @Override
        public void run() {
            stickOK = false;

        }
    };
    private final Handler handler_bucketOK = new Handler();
    private final Runnable timeoutRunnable_bucketOK = new Runnable() {
        @Override
        public void run() {
            bucketOK = false;

        }
    };
    private final Handler handler_tiltOK = new Handler();
    private final Runnable timeoutRunnable_tiltOK = new Runnable() {
        @Override
        public void run() {
            tiltOK = false;

        }
    };

    private final Handler handler_ECU_Connected = new Handler();
    private final Runnable timeoutRunnable_CU_Connected = new Runnable() {
        @Override
        public void run() {
            ECU_Connected = false;
            isAutoL = false;
            isAutoR = false;
            outW_L = false;
            outW_R = false;

        }
    };


    private String byte2String(byte[] array) {
        String txt = "";
        for (byte i : array) {
            txt += String.format(" x%02X", i);
        }
        return txt;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();


    }
}