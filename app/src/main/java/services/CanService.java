package services;

import static drill_pile.gui.Dialog_Pile_Hydro.EV1_LOWER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV1_UPPER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV2_LOWER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV2_UPPER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV3_LOWER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV3_UPPER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV4_LOWER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV4_UPPER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV5_LOWER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV5_UPPER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV6_LOWER;
import static drill_pile.gui.Dialog_Pile_Hydro.EV6_UPPER;
import static drill_pile.gui.Dialog_Pile_Hydro.HAMMER_ENGAGEMENT_DELAY_seconds;
import static drill_pile.gui.Dialog_Pile_Hydro.REVERSE_FOOT_ENCODER;
import static drill_pile.gui.Dialog_Pile_Hydro.REVERSE_HAMMER;
import static drill_pile.gui.Dialog_Pile_Hydro.REVERSE_PLUMB_AX_1;
import static drill_pile.gui.Dialog_Pile_Hydro.REVERSE_PLUMB_AX_2;
import static drill_pile.gui.Dialog_Pile_Hydro.REVERSE_RISE_LOW;
import static drill_pile.gui.Dialog_Pile_Hydro.RISE_DIST_mm;
import static drill_pile.gui.Dialog_Pile_Hydro.SWAP_PLUMB_AX;
import static packexcalib.exca.Sensors_Decoder.PGN_TiltRotator_EngCon;
import static packexcalib.exca.Sensors_Decoder.PGN_Tiltrotator;
import static packexcalib.exca.Sensors_Decoder.PGN_TiltrotatorEPS;
import static utils.MyTypes.CASE_BUS;
import static utils.MyTypes.CAT_SEA;
import static utils.MyTypes.DEMO_BAG;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.DRILL;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.FMI_SENS;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.JD_LIEBHERR;
import static utils.MyTypes.JOYSTICKS;
import static utils.MyTypes.KOMATSU_CAN;
import static utils.MyTypes.NOBAS;
import static utils.MyTypes.STX_ECU;
import static utils.MyTypes.TSM_ACC;
import static utils.MyTypes.UNIVERSAL_ECU;
import static utils.MyTypes.WHEELLOADER;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

import drill_pile.gui.Dialog_Pile_Hydro;
import event_bus.CanEvents;
import gui.MyApp;
import gui.debug_ecu.Can_Msg_Debug;
import gui.gps.Nuovo_Gps;
import gui.my_opengl.My3DActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.PGNExtractor;
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
import utils.Plus1DiagClient;

public class CanService extends Service {
    public CanService() {
    }

  /*  private static final java.util.Map<String, String> PLUS1_KNOWN_KEYS = new java.util.HashMap<>();

    static {
        PLUS1_KNOWN_KEYS.put(
                "25 3D DA C2",
                "C8 2B 07 88 94 F0 0D 2B CA 53 E0 72 2A CE 00 2A " +
                        "92 8A 59 36 10 97 B8 8B D5 CA 11 D2 7A 09 26 AE " +
                        "AD 23 E5 08 E0 37 90 BC"
        );
        PLUS1_KNOWN_KEYS.put(
                "20 5B DF A4",
                "04 B3 37 E8 54 1F D3 F8 6C 70 A6 FE 5D 20 B3 23 " +
                        "EF 70 C2 6F CD 42 12 DF 12 44 62 34 D9 20 1B BB " +
                        "87 18 FC 55 35 F2 1A A8"
        );
        PLUS1_KNOWN_KEYS.put(
                "15 CD EA 32",
                "28 EB E8 39 F6 34 85 54 34 C0 C6 51 6C 42 77 AB " +
                        "90 8E 3E 97 52 7C 6E 48 3C 18 DA 2B E7 33 52 29 " +
                        "CC E1 61 6F 41 75 14 DB"
        );
        PLUS1_KNOWN_KEYS.put(
                "11 FB EE 04",
                "44 33 58 59 36 DB 5B E8 4C 30 26 FE 32 FE 0F 5B " +
                        "1F 90 02 80 7C 20 D6 38 B3 06 89 8D AB C4 BC F5 " +
                        "1B 20 8C B5 F5 72 1A C7"
        );
        PLUS1_KNOWN_KEYS.put(
                "1B C9 E4 36",
                "20 FB C8 79 19 85 88 4E 00 C7 A7 FC 59 47 12 61 " +
                        "6B 78 BD 91 5E 0B EF 25 E6 C3 03 F6 32 F6 D8 52 " +
                        "3A 0D D6 01 9D A2 BA 87"
        );
        PLUS1_KNOWN_KEYS.put(
                "00 D5 FF 2A",
                "18 8B 28 D6 28 E7 23 77 1D 92 0D C7 2F C4 14 6D " +
                        "1C 96 0E 98 4C 40 79 66 0F 7E 79 02 DA 49 A6 AE " +
                        "C2 92 87 CC 68 48 6E 40"
        );

        PLUS1_KNOWN_KEYS.put(
                "59 A3 A6 48",
                "DC 6C E6 25 A1 F5 68 8E 80 C7 A7 FC 36 99 C1 C7 " +
                        "48 3E 31 E6 B0 B8 E6 58 73 86 89 8D C4 1A 6F " +
                        "53 57 B8 D3 64 38 E8 2E C0"
        );

        PLUS1_KNOWN_KEYS.put(
                "09 C1 F6 3E",
                "30 DB 88 96 C7 39 9F 60 33 A1 6B 64 69 48 63 EC " +
                        "1E FD B7 85 19 EA 42 7F 3D 75 6F 2E 82 F9 A9 DF " +
                        "20 56 60 6D 2A CC 66 3F"
        );

        PLUS1_KNOWN_KEYS.put(
                "09 CB F6 34",
                "24 F3 D8 36 87 B9 9F 60 33 A1 6B 64 06 96 B0 4A " +
                        "52 0A 59 36 10 97 B8 E4 64 C7 0B E6 12 D9 E9 5F " +
                        "20 39 BE BE E3 5E 42 77"
        );

        PLUS1_KNOWN_KEYS.put(
                "09 B7 F6 48",
                "DC 03 38 F6 07 B9 9F 60 33 CE DA 69 73 13 D5 EF " +
                        "77 40 A2 AF 4D 42 12 DF 12 2B D3 56 72 19 06 EE " +
                        "2D 23 8A D6 33 91 B3 FA"
        );

        PLUS1_KNOWN_KEYS.put(
                "17 75 E8 8A",
                "58 0B 28 B9 F6 34 EA 8A E7 66 E5 17 8F EB 4A D1 " +
                        "0B B8 52 4F 8D C2 7D 6E 70 80 EA 24 F9 60 9B D4 " +
                        "59 A4 84 A5 BA EC 49 0E"
        );

        PLUS1_KNOWN_KEYS.put(
                "17 61 E8 9E",
                "70 5B 88 F9 76 34 EA 8A E7 66 E5 78 51 38 EC 9D " +
                        "FC 56 E1 46 F0 38 E6 37 C2 E4 22 B4 D9 20 1B D4 " +
                        "36 7A 57 6C 28 C8 01 9E"
        );

        PLUS1_KNOWN_KEYS.put(
                "12 93 ED 6C",
                "94 93 18 D9 36 DB 34 59 2E 9B 1F E3 08 8A E7 E4 " +
                        "61 03 24 CC E4 7F 07 9A F7 8E 99 C2 5A 26 78 7D " +
                        "64 B1 AE 9E CC 00 91 D1"
        );

        PLUS1_KNOWN_KEYS.put(
                "D6 E9 29 02",
                "27 9A 0A FD 7E 4B 7B C7 7D 52 8D C7 2F AB CA BE " +
                        "BA B5 48 14 3B AE CA 6F 1D 35 EF 2E ED 48 A4 AA " +
                        "CA ED 16 81 F2 13 B7 F2"
        );

        PLUS1_KNOWN_KEYS.put(
                "7C 89 E0 1A",
                "78 24 19 DB 5D 62 46 D2 38 B7 28 8D BB EC 2B 7C " +
                        "51 63 8B 92 58 07 98 A4 E4 A8 BA EB 67 33 52 46 " +
                        "12 32 C7 23 B6 9B C8 0C"
        );

        PLUS1_KNOWN_KEYS.put(
                "DF 84 20 7A",
                "D7 7A CA 7D 11 95 C7 D0 53 0E 5A 69 1C CD 06 49 " +
                        "3B D8 FD 7E 80 D8 26 B7 C2 8B 93 B9 C3 7B C2 09 " +
                        "8C 61 61 6F 41 75 7B 6A"
        );

        PLUS1_KNOWN_KEYS.put(
                "FA EF 05 0E",
                "3F AA 05 E3 2D 82 E9 8C EB 11 0B A4 E9 48 63 83 " +
                        "C0 41 CF 75 F9 45 1C AC F4 88 95 DA 6A 29 09 F0 " +
                        "11 5B 15 E8 20 D8 21 B1"
        );

        PLUS1_KNOWN_KEYS.put(
                "D2 FE 2E EA",
                "F7 3A 4A 7D 7E 24 A5 7B 05 A2 02 B6 CD 6F 42 AE " +
                        "F5 2B 1B B2 77 36 95 D1 0E 13 A3 B6 DD 47 D5 27 " +
                        "BF 68 1C 95 DA 43 78 6C"
        );
    }
    static Plus1DiagClient client = new Plus1DiagClient(
            (extended, channel, id, dlc, data) -> {
                MyDeviceManager.CanWrite(extended, channel, id, dlc, data);
            },
            (level, seed) -> {
                String seedHex = Plus1DiagClient.bytesToHex(seed);

                String keyHex = PLUS1_KNOWN_KEYS.get(seedHex);

                if (keyHex == null) {
                    Log.e("PLUS1_KEY", "No known key for seed=" + seedHex);
                    Log.e("PLUS1_KEY_TODO",
                            "PLUS1_KNOWN_KEYS.put(\"" + seedHex + "\", \"PASTE_KEY_HERE\");");
                    return null;
                }

                byte[] key = Plus1DiagClient.hex(keyHex);

                if (key.length != 40) {
                    Log.e("PLUS1_KEY",
                            "Invalid known key length for seed=" + seedHex + ", len=" + key.length);
                    return null;
                }

                Log.e("PLUS1_KEY", "Known key found for seed=" + seedHex);
                return key;
            }
    );*/

    public static String CAT_Joystick, KOMATSU_Joystick, JD_Joystick, JD_GP_Joystyck, CASE_Joystick, NOBAS_Joystick;
    public static int SteerConnected, isAuto;
    public static int m, ECU_VALVE_TYPE = -1;
    public static boolean NOBAS_Connected, Dozer_Auto_Main, Grader_Auto_Left, Grader_AutoRight, Grader_Auto_SS,
            ECU_Connected, JD_Connected, CAT_Connected, KOM_Connected, CASE_Connected;
    public static boolean frameOK, boom1OK, boom2OK, stickOK, bucketOK, tiltOK, flagLaser, flagDefault, toolOK;
    CanFileReceiver receiver = new CanFileReceiver();
    public static boolean boom1Disc, boom2Disc, stickDisc, bucketDisc, frameDisc, tiltDisc, nmeaSTX_Disc, toolDisc;
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
        toolDisc = true;
        frameOK = false;
        boom1OK = false;
        boom2OK = false;
        stickOK = false;
        bucketOK = false;
        toolOK = false;
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
        if (DataSaved.lrTool != 0) {
            handler_tool.postDelayed(timeoutRunnable_tool, 3000);
        }
        handler_ECU_Connected.postDelayed(timeoutRunnable_ECU_Connected, 3000);
        handler_steer.postDelayed(timeoutRunnable_steer, 3000);
        handler_CASE_Connected.postDelayed(timeoutRunnable_CASE_Connected, 3000);
        handler_CAT_Connected.postDelayed(timeoutRunnable_CAT_Connected, 3000);
        handler_JD_Connected.postDelayed(timeoutRunnable_JD_Connected, 3000);
        handler_KOM_Connected.postDelayed(timeoutRunnable_KOM_Connected, 3000);
        handler_DEFAULT.postDelayed(timeoutRunnable_DEFAULT, 3000);
        handler_NOBAS_Connected.postDelayed(timeoutRunnable_NOBAS_Connected, 3000);

        handler_rotoTilt.postDelayed(timeoutRunnable_rotoTilt, 3000);


        super.onCreate();
    }

    public void OnCan(int channel, byte[] msg, int dlc, int id) {
        if (DataSaved.isCanOpen == JOYSTICKS) {

            return;
        }


        try {


            if (MyApp.visibleActivity instanceof Nuovo_Gps || MyApp.visibleActivity instanceof Can_Msg_Debug) {

                EventBus.getDefault().post(new CanEvents(channel, null, id, dlc, msg));
            }

            if (channel == 1) {

                if (DataSaved.isCanOpen == TSM_ACC || DataSaved.isCanOpen == FMI_SENS) {
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
                    if (id == 0x18FF0510 || id == 0x18FF0501) {
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
                        if (id > 2048 && (PGNExtractor.extractPGN(id) == PGN_Tiltrotator || PGNExtractor.extractPGN(id) == PGN_TiltrotatorEPS || PGNExtractor.extractPGN(id) == PGN_TiltRotator_EngCon)) {
                            handler_rotoTilt.removeCallbacks(timeoutRunnable_rotoTilt);
                            handler_rotoTilt.postDelayed(timeoutRunnable_rotoTilt, 3000);
                        }
                        if (id == 0x1FF) {
                            flagDefault = true;
                            handler_DEFAULT.removeCallbacks(timeoutRunnable_DEFAULT);
                            handler_DEFAULT.postDelayed(timeoutRunnable_DEFAULT, 3000);
                        }
                        if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER || DataSaved.isWL == DRILL) {
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
                        if (id > 2048 && (PGNExtractor.extractPGN(id) == PGN_Tiltrotator || PGNExtractor.extractPGN(id) == PGN_TiltrotatorEPS || PGNExtractor.extractPGN(id) == PGN_TiltRotator_EngCon)) {
                            handler_rotoTilt.removeCallbacks(timeoutRunnable_rotoTilt);
                            handler_rotoTilt.postDelayed(timeoutRunnable_rotoTilt, 3000);
                        }
                        //moba o tsm
                        if (id == 899) {
                            flagDefault = true;
                            handler_DEFAULT.removeCallbacks(timeoutRunnable_DEFAULT);
                            handler_DEFAULT.postDelayed(timeoutRunnable_DEFAULT, 3000);
                        }
                        if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER || DataSaved.isWL == DRILL) {
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
                            if (id == 901 || id == 0x2A0) {
                                bucketOK = true;
                                handler_bucketOK.removeCallbacks(timeoutRunnable_bucketOK);
                                handler_bucketOK.postDelayed(timeoutRunnable_bucketOK, 3000);
                            }
                            if (id == 901 || id == 0x2A0) {
                                toolOK = true;
                                toolDisc = false;
                                handler_tool.removeCallbacks(timeoutRunnable_tool);
                                handler_tool.postDelayed(timeoutRunnable_tool, 3000);
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
                            if ((id == 901 || id == 0x2A0) && DataSaved.lrBucket != 0) {
                                bucketDisc = false;
                                handler_bk.removeCallbacks(timeoutRunnable_bk);
                                handler_bk.postDelayed(timeoutRunnable_bk, 3000);
                            }
                            if ((id == 901 || id == 0x2A0) && DataSaved.lrTool != 0) {
                                toolDisc = false;
                                handler_tool.removeCallbacks(timeoutRunnable_tool);
                                handler_tool.postDelayed(timeoutRunnable_tool, 3000);
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
                            toolOK = true;
                            toolDisc = false;
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
                        if (DataSaved.isCanOpen != UNIVERSAL_ECU) {
                            Sensors_Decoder_Drill.decode(id, msg);
                        }
                        break;
                }

            }

            if (channel == 2) {
                if(id==0xCDA0103||id==0x0CDA01F1) {
                    Log.w("PLUS1_KEY", id + "  " + dlc + "  " + Arrays.toString(msg) + "\n");
                }
                //client.onCanFrameReceived(id>2047, channel, id, dlc, msg);

                //FMI_Decoder.decode(id,msg);
                //CAN2
                if (PGNExtractor.extractPGN(id) == 0xF00D && DataSaved.Interface_Type == CASE_BUS) {
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
                if (PGNExtractor.extractPGN(id) == 0xF00D && DataSaved.Interface_Type == CAT_SEA) {
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
                            //Log.d("JDD", (String.valueOf(-offsetH)));
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
                if (PGNExtractor.extractPGN(id) == 0xF00D && (DataSaved.Interface_Type == JD_LIEBHERR || DataSaved.Interface_Type == STX_ECU)
                        && DataSaved.isWL == DOZER) {
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
                if (PGNExtractor.extractPGN(id) == 0xF00D && (DataSaved.Interface_Type == JD_LIEBHERR || DataSaved.Interface_Type == STX_ECU)
                        && DataSaved.isWL == GRADER) {
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
                if (id == 0x0CFF3302 && DataSaved.Interface_Type == KOMATSU_CAN) {
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
                if (id == 2166 || id == 0x81) {
                    ECU_Connected = true;
                    ECU_VALVE_TYPE = msg[0];
                    handler_ECU_Connected.removeCallbacks(timeoutRunnable_ECU_Connected);
                    handler_ECU_Connected.postDelayed(timeoutRunnable_ECU_Connected, 1000);
                }

                //todo NOBAS Verificare mappatura
                if (PGNExtractor.extractPGN(id) == 0xF00D && DataSaved.Interface_Type == NOBAS) {
                    NOBAS_Connected = true;
                    handler_NOBAS_Connected.removeCallbacks(timeoutRunnable_NOBAS_Connected);
                    handler_NOBAS_Connected.postDelayed(timeoutRunnable_NOBAS_Connected, 2000);
                    NOBAS_Joystick = "0x" + Integer.toHexString(id).toUpperCase() + " " + dlc + " " + bytesToHex(msg);
                    //byte 0 = offset 2F=up 1F=down
                    //byte 1 = 20 manual 22 auto
                    //boolean[] bGrad_Left = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[1]);
                    boolean[] bAuto = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[1]);
                    boolean[] bGrad_SS = PLC_DataTypes_LittleEndian.U8_to_bitmask(msg[0]);

                    if (MyApp.visibleActivity instanceof My3DActivity) {
                        //AutoManToggle.updateLEFT(bGrad_Left[1]);
                        //AutoManToggle.updateRIGHT(bGrad_Right[7]);
                        //AutoManToggle.updateSS(bGrad_SS[3]);
                        OffsetAdjuster.update(bGrad_SS[2], bGrad_SS[3]);
                        Dozer_Auto_Main = !bAuto[6];
                        Grader_Auto_Left = !bAuto[6];
                        Grader_AutoRight = !bAuto[6];


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

                //todo messaggi drill ecu
                if (DataSaved.isWL == DRILL && DataSaved.isCanOpen == UNIVERSAL_ECU) {
                    Sensors_Decoder_Drill.decode(id, msg);
                    if (id == 0x81) {
                        toolOK = true;
                        toolDisc = false;
                        handler_tool.removeCallbacks(timeoutRunnable_tool);
                        handler_tool.postDelayed(timeoutRunnable_tool, 3000);
                    }
                    if (id == 0x3AC) {
                        int index=msg[0];
                        int valore = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{msg[1], msg[2]});
                        switch (index) {
                            case 0:
                                EV1_UPPER = valore;
                                break;
                            case 1:
                                EV1_LOWER = valore;
                                break;
                            case 2:
                                EV2_UPPER=valore;
                                break;
                            case 3:
                                EV2_LOWER=valore;
                                break;
                            case 4:
                                EV3_UPPER=valore;
                                break;
                            case 5:
                                EV3_LOWER=valore;
                                break;
                            case 6:
                                EV4_UPPER=valore;
                                break;
                            case 7:
                                EV4_LOWER=valore;
                                break;
                            case 8:
                                EV5_UPPER=valore;
                                break;
                            case 9:
                                EV5_LOWER=valore;
                                break;
                            case 10:
                                EV6_UPPER=valore;
                                break;
                            case 11:
                                EV6_LOWER=valore;
                                break;
                            case 12:
                                SWAP_PLUMB_AX= valore == 1;
                                break;
                            case 13:
                                REVERSE_FOOT_ENCODER=valore==1;
                                break;
                            case 14:
                                REVERSE_PLUMB_AX_1=valore==1;
                                break;
                            case 15:
                                REVERSE_PLUMB_AX_2=valore==1;
                                break;
                            case 16:
                                    REVERSE_HAMMER=valore==1;
                                break;
                            case 17:
                                HAMMER_ENGAGEMENT_DELAY_seconds=valore;
                                break;
                            case 18:
                                RISE_DIST_mm=valore;
                                break;
                            case 19:
                                Dialog_Pile_Hydro.hasReaded=true;
                                REVERSE_RISE_LOW=valore==1;
                                break;
                        }
                    }
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

        //startPlus1DiagnosticDelayed();
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
    private final Handler handler_tool = new Handler();
    private final Runnable timeoutRunnable_tool = new Runnable() {
        @Override
        public void run() {
            toolDisc = true;
            toolOK = false;

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

    private final Handler handler_NOBAS_Connected = new Handler();
    private final Runnable timeoutRunnable_NOBAS_Connected = new Runnable() {
        @Override
        public void run() {
            NOBAS_Connected = false;
        }
    };
    private final Handler handler_rotoTilt = new Handler();
    private final Runnable timeoutRunnable_rotoTilt = new Runnable() {
        @Override
        public void run() {
            Sensors_Decoder.Deg_Roto = 0;
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

   /* private void startPlus1DiagnosticDelayed() {
        new Thread(() -> {
            try {
                Log.e("PLUS1_TEST", "Starting PLUS+1 initial diagnostic...");

                Plus1DiagClient.EcuInfo info = client.readEcuInfo();
                Log.e("PLUS1_TEST", "Info: " + info);

                client.runApplicationDiagnostic();

                Log.e("PLUS1_TEST", "Application diagnostic completed");

            } catch (Exception e) {
                Log.e("PLUS1_TEST", "Diagnostic failed", e);
            }
        }).start();
    }*/

}