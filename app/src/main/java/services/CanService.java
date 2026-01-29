package services;

import static packexcalib.exca.DataSaved.gpsOk;
import static packexcalib.exca.DataSaved.offsetH;
import static utils.MyTypes.DEMO_BAG;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.FMI_SENS;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.JOYSTICKS;
import static utils.MyTypes.SOLARDRILL;
import static utils.MyTypes.TSM_ACC;
import static utils.MyTypes.TSM_ANGOLARI;
import static utils.MyTypes.WHEELLOADER;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import event_bus.CanEvents;
import gui.MyApp;
import gui.debug_ecu.Can_Msg_Debug;
import gui.gps.Nuovo_Gps;
import gui.my_opengl.My3DActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.PLC_DataTypes_LittleEndian;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.exca.Sensors_Decoder_Drill;
import packexcalib.gnss.NmeaListener;
import serial.OpenSerialPort;
import utils.AutoManToggle;
import utils.CPCanHelper;
import utils.CanFileReceiver;
import utils.MyDeviceManager;
import utils.OffsetAdjuster;

public class CanService extends Service {
    public CanService() {
    }

    public static String CAT_Joystick, KOMATSU_Joystick, JD_Joystick, JD_GP_Joystyck, CASE_Joystick;
    public static int SteerConnected, isAuto;
    public static int m;
    public static boolean Dozer_Auto_Main, Grader_Auto_Left, Grader_AutoRight, Grader_Auto_SS, ECU_Connected, JD_Connected, CAT_Connected, KOM_Connected, CASE_Connected;
    public static boolean frameOK, boom1OK, boom2OK, stickOK, bucketOK, tiltOK, flagLaser, flagDefault;
    CanFileReceiver receiver = new CanFileReceiver();
    public static boolean boom1Disc, boom2Disc, stickDisc, bucketDisc, frameDisc, tiltDisc, nmeaSTX_Disc;
    public static boolean CanServiceState = false;
    int dlc;

    @Override
    public void onCreate() {

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
        flagLaser = false;
        tiltOK = false;
        ECU_Connected = false;
        CAT_Connected = false;
        JD_Connected = false;
        KOM_Connected = false;
        CASE_Connected = false;

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
        handler_ECU_Connected.postDelayed(timeoutRunnable_ECU_Connected, 3000);
        handler_steer.postDelayed(timeoutRunnable_steer, 3000);
        handler_CASE_Connected.postDelayed(timeoutRunnable_CASE_Connected, 3000);
        handler_CAT_Connected.postDelayed(timeoutRunnable_CAT_Connected, 3000);
        handler_JD_Connected.postDelayed(timeoutRunnable_JD_Connected, 3000);
        handler_KOM_Connected.postDelayed(timeoutRunnable_KOM_Connected, 3000);
        handler_DEFAULT.postDelayed(timeoutRunnable_DEFAULT, 3000);


        super.onCreate();
    }

    public void OnCan(int channel, byte[] msg, int dlc, int id) {
        if(DataSaved.isCanOpen==JOYSTICKS){
            nmeaSTX_Disc = false;
            frameDisc = false;
            boom2Disc = false;
            boom1Disc = false;
            bucketDisc = false;
            tiltDisc = false;
            stickDisc = false;
            frameOK = true;
            boom1OK = true;
            boom2OK = true;
            stickOK = true;
            bucketOK = true;
            flagLaser = true;
            tiltOK = true;

            return;
        }


        try {

            if (MyApp.visibleActivity instanceof Nuovo_Gps || MyApp.visibleActivity instanceof Can_Msg_Debug) {

                EventBus.getDefault().post(new CanEvents(channel, null, id, dlc, msg));
            }

            if (channel == 1) {

                if (DataSaved.isCanOpen == TSM_ACC || DataSaved.isCanOpen == TSM_ANGOLARI || DataSaved.isCanOpen == FMI_SENS) {
                    if (id == 0x581) {
                        DataSaved.damp_Fr = msg[4];
                    }
                    if (id == 0x582) {
                        DataSaved.damp_B1 = msg[4];
                    }
                    if (id == 0x587) {
                        DataSaved.damp_B2 = msg[4];
                    }
                    if (id == 0x584) {
                        DataSaved.damp_St = msg[4];
                    }
                    if (id == 0x585) {
                        DataSaved.damp_Bk = msg[4];
                    }
                    if (id == 0x586) {
                        DataSaved.damp_Tl = msg[4];
                    }

                } else if (DataSaved.isCanOpen == DEMO_BAG) {
                    nmeaSTX_Disc = false;
                }
                if (DataSaved.my_comPort == 0 && DataSaved.gpsType == 0) {
                    NmeaListener.NmeaSTX(id, msg);
                    if (id == 0x18FF0510) {
                        nmeaSTX_Disc = false;
                        try {
                            handler_nmeaSTX.removeCallbacks(timeoutRunnable_nmea2k);

                        } catch (Exception e) {
                        }
                        handler_nmeaSTX.postDelayed(timeoutRunnable_nmea2k, 5000);
                    }
                }
                if (DataSaved.my_comPort == 0 && DataSaved.gpsType == 3) {
                    //ICG82
                    if (id == 0x318) {
                        NmeaListener.NmeaLeica(id, msg, dlc);
                        nmeaSTX_Disc = false;
                        try {
                            handler_nmeaSTX.removeCallbacks(timeoutRunnable_nmea2k);

                        } catch (Exception e) {
                        }
                        handler_nmeaSTX.postDelayed(timeoutRunnable_nmea2k, 5000);

                    }
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

                if (DataSaved.isWL == DOZER || DataSaved.isWL == DOZER_SIX || DataSaved.isWL == GRADER) {
                    DataSaved.deltaZ = DataSaved.altezzaLama + DataSaved.altezzaPali;
                }

                if (DataSaved.isWL == WHEELLOADER) {
                    if (DataSaved.Extra_Heading > 0) {
                        if (id == 0x1A2) {
                            SteerConnected = 2;
                            handler_steer.removeCallbacks(timeoutRunnable_steer);
                            handler_steer.postDelayed(timeoutRunnable_steer, 3000);
                        }
                    } else {
                        SteerConnected = 0;
                    }
                } else {
                    SteerConnected = 0;
                }
                if (id == 0x204301) {
                    flagLaser = true;
                    handler_flagLaser.removeCallbacks(timeoutRunnable_flagLaser);
                    handler_flagLaser.postDelayed(timeoutRunnable_flagLaser, 800);
                }

                switch (DataSaved.isCanOpen) {
                    case FMI_SENS:
                        if (id == 0x1FF) {
                            flagDefault = true;
                            handler_DEFAULT.removeCallbacks(timeoutRunnable_DEFAULT);
                            handler_DEFAULT.postDelayed(timeoutRunnable_DEFAULT, 3000);
                        }
                        if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER || DataSaved.isWL == DRILL || DataSaved.isWL == SOLARDRILL) {
                            if (id == 0x181) {
                                frameOK = true;
                                handler_frameOK.removeCallbacks(timeoutRunnable_frameOK);
                                handler_frameOK.postDelayed(timeoutRunnable_frameOK, 3000);
                            }
                            if (id == 0x182) {
                                boom1OK = true;
                                handler_boom1OK.removeCallbacks(timeoutRunnable_boom1OK);
                                handler_boom1OK.postDelayed(timeoutRunnable_boom1OK, 3000);
                            }
                            if (id == 0x187) {
                                boom2OK = true;
                                handler_boom2OK.removeCallbacks(timeoutRunnable_boom2OK);
                                handler_boom2OK.postDelayed(timeoutRunnable_boom2OK, 3000);
                            }
                            if (id == 0x184) {
                                stickOK = true;
                                handler_stickOK.removeCallbacks(timeoutRunnable_stickOK);
                                handler_stickOK.postDelayed(timeoutRunnable_stickOK, 3000);
                            }
                            if (id == 0x185) {
                                bucketOK = true;
                                handler_bucketOK.removeCallbacks(timeoutRunnable_bucketOK);
                                handler_bucketOK.postDelayed(timeoutRunnable_bucketOK, 3000);
                            }
                            if (id == 0x186 || id == 0x560106A) {
                                tiltOK = true;
                                tiltDisc = false;
                                handler_tiltOK.removeCallbacks(timeoutRunnable_tiltOK);
                                handler_tiltOK.postDelayed(timeoutRunnable_tiltOK, 3000);
                            }
                            /*


                             */
                            if (id == 0x181 && DataSaved.lrFrame != 0) {
                                frameDisc = false;
                                handler_frame.removeCallbacks(timeoutRunnable_frame);
                                handler_frame.postDelayed(timeoutRunnable_frame, 3000);
                            }
                            if ((id == 0x182) && DataSaved.lrBoom1 != 0) {
                                boom1Disc = false;
                                handler_b1.removeCallbacks(timeoutRunnable_b1);
                                handler_b1.postDelayed(timeoutRunnable_b1, 3000);
                            }
                            if ((id == 0x187) && DataSaved.lrBoom2 != 0) {

                                boom2Disc = false;
                                handler_b2.removeCallbacks(timeoutRunnable_b2);
                                handler_b2.postDelayed(timeoutRunnable_b2, 3000);
                            }
                            if ((id == 0x184) && DataSaved.lrStick != 0) {
                                stickDisc = false;
                                handler_st.removeCallbacks(timeoutRunnable_st);
                                handler_st.postDelayed(timeoutRunnable_st, 3000);
                            }
                            if ((id == 0x185) && DataSaved.lrBucket != 0) {
                                bucketDisc = false;
                                handler_bk.removeCallbacks(timeoutRunnable_bk);
                                handler_bk.postDelayed(timeoutRunnable_bk, 3000);
                            }
                            if ((id == 0x186) && DataSaved.lrTilt != 0) {
                                tiltOK = true;
                                tiltDisc = false;
                                handler_tl.removeCallbacks(timeoutRunnable_tl);
                                handler_tl.postDelayed(timeoutRunnable_tl, 3000);
                            }
                        } else {
                            if ((id == 0x185 || id == 0x186 || id == 0x560106A) && DataSaved.lrBucket != 0) {
                                tiltOK = true;
                                tiltDisc = false;
                                handler_tl.removeCallbacks(timeoutRunnable_tl);
                                handler_tl.postDelayed(timeoutRunnable_tl, 3000);
                            }
                        }
                        break;
                    case TSM_ACC:
                        //moba o tsm
                        if (id == 899) {
                            flagDefault = true;
                            handler_DEFAULT.removeCallbacks(timeoutRunnable_DEFAULT);
                            handler_DEFAULT.postDelayed(timeoutRunnable_DEFAULT, 3000);
                        }
                        if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER || DataSaved.isWL == DRILL || DataSaved.isWL == SOLARDRILL) {
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
                                tiltDisc = false;
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
                            if ((id == 901 || id == 902 || id == 90181738 || id == 90181733) && DataSaved.lrBucket != 0) {
                                tiltOK = true;
                                tiltDisc = false;
                                handler_tl.removeCallbacks(timeoutRunnable_tl);
                                handler_tl.postDelayed(timeoutRunnable_tl, 3000);
                            }
                        }

                        break;

                    case DEMO_BAG:
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
                            tiltDisc = false;
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


                if (id == 0x7DF || id == 0x560106A) {
                    receiver.receivePacket(msg);
                }
                switch (DataSaved.isWL) {
                    case EXCAVATOR:
                    case WHEELLOADER:
                    case DOZER:
                    case DOZER_SIX:
                    case GRADER:
                        Sensors_Decoder.decode(id, msg);
                        break;

                    case DRILL:
                    case SOLARDRILL:
                        Sensors_Decoder_Drill.decode(id, msg);
                        break;
                }

            }

            if (channel == 2) {
                //FMI_Decoder.decode(id,msg);
                //CAN2
                if (id == 0x18F00DE3 && DataSaved.Interface_Type == 4) {
                    CASE_Connected = true;
                    handler_CASE_Connected.removeCallbacks(timeoutRunnable_CASE_Connected);
                    handler_CASE_Connected.postDelayed(timeoutRunnable_CASE_Connected, 2000);
                    CASE_Joystick = "0x" + Integer.toHexString(id).toUpperCase() + " " + dlc + " " + bytesToHex(msg);
                    boolean[] booleansC = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[0]);
                    if (MyApp.visibleActivity instanceof My3DActivity) {
                        Dozer_Auto_Main = booleansC[6] && booleansC[7];
                        OffsetAdjuster.update(booleansC[2], booleansC[3]);
                    } else {
                        Dozer_Auto_Main = false;
                    }

                }
                if (id == 0x1CF00D22 && DataSaved.Interface_Type == 1) {
                    CAT_Connected = true;
                    handler_CAT_Connected.removeCallbacks(timeoutRunnable_CAT_Connected);
                    handler_CAT_Connected.postDelayed(timeoutRunnable_CAT_Connected, 2000);
                    CAT_Joystick = "0x" + Integer.toHexString(id).toUpperCase() + " " + dlc + " " + bytesToHex(msg);
                    boolean[] booleans = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[0]);
                    boolean[] bGrad_Left = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[1]);
                    boolean[] bGrad_Right = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[2]);
                    boolean[] bGrad_SS = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[3]);

                    if (MyApp.visibleActivity instanceof My3DActivity) {
                        //+4 -5 auto1
                        if (DataSaved.CAT_Type == 0) {
                            AutoManToggle.update(booleans[6]);
                            Dozer_Auto_Main = AutoManToggle.Can_Toggled_Auto;
                            Log.d("JDD", (String.valueOf(-offsetH)));
                            OffsetAdjuster.update(booleans[2], booleans[3]);
                        } else if (DataSaved.CAT_Type == 1) {
                            Dozer_Auto_Main = booleans[6];
                            OffsetAdjuster.update(booleans[2], booleans[3]);
                        } else if (DataSaved.CAT_Type == 2) {
                            AutoManToggle.updateLEFT(bGrad_Left[3]);
                            AutoManToggle.updateRIGHT(bGrad_Right[7]);
                            AutoManToggle.updateSS(bGrad_SS[3]);
                            OffsetAdjuster.update(bGrad_SS[6], bGrad_SS[7]);
                            OffsetAdjuster.update(bGrad_Right[2], bGrad_Right[3]);
                            Dozer_Auto_Main = false;
                            Grader_Auto_Left = AutoManToggle.Can_Toggled_Auto_L;
                            Grader_AutoRight = AutoManToggle.Can_Toggled_Auto_R;
                            Grader_Auto_SS = AutoManToggle.Can_Toggled_Auto_SS;

                        }
                    } else {
                        Dozer_Auto_Main = false;
                        Grader_Auto_Left = false;
                        Grader_AutoRight = false;
                        Grader_Auto_SS = false;
                        AutoManToggle.Can_Toggled_Auto = false;
                        AutoManToggle.Can_Toggled_Auto_L = false;
                        AutoManToggle.Can_Toggled_Auto_R = false;
                        AutoManToggle.Can_Toggled_Auto_SS = false;
                    }

                }
                if (id == 0x0CF00D80 && (DataSaved.Interface_Type == 2 || DataSaved.Interface_Type == 0)) {
                    JD_Connected = true;
                    handler_JD_Connected.removeCallbacks(timeoutRunnable_JD_Connected);
                    handler_JD_Connected.postDelayed(timeoutRunnable_JD_Connected, 2000);
                    JD_Joystick = "0x" + Integer.toHexString(id).toUpperCase() + " " + dlc + " " + bytesToHex(msg);
                    boolean[] booleans = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[0]);
                    if (MyApp.visibleActivity instanceof My3DActivity) {
                        Dozer_Auto_Main = booleans[7];
                        OffsetAdjuster.update(booleans[2], booleans[3]);
                    } else {
                        Dozer_Auto_Main = false;
                        Grader_Auto_Left = false;
                        Grader_AutoRight = false;
                        Grader_Auto_SS = false;
                        AutoManToggle.Can_Toggled_Auto = false;
                        AutoManToggle.Can_Toggled_Auto_L = false;
                        AutoManToggle.Can_Toggled_Auto_R = false;
                        AutoManToggle.Can_Toggled_Auto_SS = false;
                    }

                }
                if (id == 0x0CF00DD5 && (DataSaved.Interface_Type == 2 || DataSaved.Interface_Type == 0)) {
                    JD_Connected = true;
                    handler_JD_Connected.removeCallbacks(timeoutRunnable_JD_Connected);
                    handler_JD_Connected.postDelayed(timeoutRunnable_JD_Connected, 2000);
                    JD_GP_Joystyck = "0x" + Integer.toHexString(id).toUpperCase() + " " + dlc + " " + bytesToHex(msg);
                    boolean[] booleans = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[3]);
                    boolean[] bGrad_Left = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[1]);
                    boolean[] bGrad_Right = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[2]);
                    if (MyApp.visibleActivity instanceof My3DActivity) {
                        Grader_Auto_Left = bGrad_Left[3];
                        Grader_AutoRight = bGrad_Right[7];
                        OffsetAdjuster.update(booleans[6], booleans[7]);
                    } else {
                        Dozer_Auto_Main = false;
                        Grader_Auto_Left = false;
                        Grader_AutoRight = false;
                        Grader_Auto_SS = false;
                        AutoManToggle.Can_Toggled_Auto = false;
                        AutoManToggle.Can_Toggled_Auto_L = false;
                        AutoManToggle.Can_Toggled_Auto_R = false;
                        AutoManToggle.Can_Toggled_Auto_SS = false;
                    }
                    Log.d("JDD", Grader_Auto_Left + " " + Grader_AutoRight + " " + Grader_Auto_SS);
                }
                if (id == 0x0CFF3302 && DataSaved.Interface_Type == 3) {
                    KOM_Connected = true;
                    handler_KOM_Connected.removeCallbacks(timeoutRunnable_KOM_Connected);
                    handler_KOM_Connected.postDelayed(timeoutRunnable_KOM_Connected, 2000);

                    KOMATSU_Joystick = "0x" + Integer.toHexString(id).toUpperCase() + " " + dlc + " " + bytesToHex(msg);
                    if (MyApp.visibleActivity instanceof My3DActivity) {
                        Dozer_Auto_Main = msg[6] == 1;
                    } else {
                        Dozer_Auto_Main = false;
                        Grader_Auto_Left = false;
                        Grader_AutoRight = false;
                        Grader_Auto_SS = false;
                        AutoManToggle.Can_Toggled_Auto = false;
                        AutoManToggle.Can_Toggled_Auto_L = false;
                        AutoManToggle.Can_Toggled_Auto_R = false;
                        AutoManToggle.Can_Toggled_Auto_SS = false;
                    }
                }
                if (id == 2166) {
                    ECU_Connected = true;
                    handler_ECU_Connected.removeCallbacks(timeoutRunnable_ECU_Connected);
                    handler_ECU_Connected.postDelayed(timeoutRunnable_ECU_Connected, 1000);
                }
            }

        } catch (Exception e) {
            Log.e("Can_Error", Log.getStackTraceString(e));
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CanServiceState = true;
        try {
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

            MyDeviceManager.CanWrite(true, 0, 0x18FF0001, 4, new byte[]{0x20, CanSender.GNSS_MSG, speed, (byte) 0x03});
            new OpenSerialPort(this);
        } catch (Exception e) {
            System.out.println(e);
        }

        if (MyApp.isApollo) {

            CPCanHelper.getInstance().start(new CPCanHelper.Action() {
                @Override
                public void execute(int channel, int id, byte[] data) {
                    if (data != null) {
                        dlc = data.length;
                        OnCan(channel, data, dlc, id);
                    }

                }
            });

        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Handler handler_steer = new Handler();
    private final Runnable timeoutRunnable_steer = new Runnable() {
        @Override
        public void run() {
            SteerConnected = 1;
        }
    };
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
            tiltOK = false;

        }
    };
    private final Handler handler_flagLaser = new Handler();
    private final Runnable timeoutRunnable_flagLaser = new Runnable() {
        @Override
        public void run() {
            flagLaser = false;

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

    private final Handler handler_DEFAULT = new Handler();
    private final Runnable timeoutRunnable_DEFAULT = new Runnable() {
        @Override
        public void run() {
            flagDefault = false;

        }
    };

    private final Handler handler_ECU_Connected = new Handler();
    private final Runnable timeoutRunnable_ECU_Connected = new Runnable() {
        @Override
        public void run() {
            ECU_Connected = false;
        }
    };
    private final Handler handler_CASE_Connected = new Handler();
    private final Runnable timeoutRunnable_CASE_Connected = new Runnable() {
        @Override
        public void run() {
            CASE_Connected = false;
        }
    };

    private final Handler handler_CAT_Connected = new Handler();
    private final Runnable timeoutRunnable_CAT_Connected = new Runnable() {
        @Override
        public void run() {
            CAT_Connected = false;
        }
    };
    private final Handler handler_JD_Connected = new Handler();
    private final Runnable timeoutRunnable_JD_Connected = new Runnable() {
        @Override
        public void run() {
            JD_Connected = false;
        }
    };
    private final Handler handler_KOM_Connected = new Handler();
    private final Runnable timeoutRunnable_KOM_Connected = new Runnable() {
        @Override
        public void run() {
            KOM_Connected = false;
        }
    };


    private String byte2String(byte[] array) {
        String txt = "";
        for (byte i : array) {
            txt += String.format(" x%02X", i);
        }
        return txt;
    }

    private String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));  // %02X = due cifre esadecimali maiuscole
        }
        return sb.toString().trim();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopSelf();


    }


}