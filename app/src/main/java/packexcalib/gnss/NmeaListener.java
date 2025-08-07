package packexcalib.gnss;

import static packexcalib.gnss.CRS_Strings._28992;
import static packexcalib.gnss.CRS_Strings._31370;
import static packexcalib.gnss.CRS_Strings._NONE;
import static packexcalib.gnss.CRS_Strings._UTM;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cloud.WebSocketPlugin;
import gui.MyApp;
import packexcalib.exca.DataSaved;
import packexcalib.exca.PLC_DataTypes_LittleEndian;


public class NmeaListener {

    static double tmpQuotaUTM = 0;
    static double tmpQuotaLOC = 0;
    static double tmpNordUTM = 0;
    static double tmpEstUTM = 0;
    static double tmpNordLOC = 0;
    static double tmpEstLOC = 0;
    static double tmpLat = 0;
    static double tmpLon = 0;
    static double tmpGeoidSeparator = 0;

    public static String[] NmeaInput;
    static CalculateXor8 calculateXor8;
    static String myNmea;
    static String mNmea1, mNmea2;

    public static char mChar;
    public static int mZone;
    public static String VRMS_, HRMS_, _3DRMS;
    public static double mLat_1, mLon_1;
    public static double Nord1, Est1, Quota1, mch_Orientation, mch_Hdt;
    public static String ggaNord, ggaEast, ggaNoS, ggaWoE, ggaZ1, ggaZ2, ggaSat, ggaDop, ggaQuality, ggaRtk, fix1;//String data from  GPS1

    /*
    NMEA STX Below
     */


    public NmeaListener() {
    }

    public static void NmeaStandard(String NMEA0183) {

        try {

            myNmea = NMEA0183.substring(NMEA0183.indexOf("$") + 1, NMEA0183.indexOf("*"));
            calculateXor8 = new CalculateXor8(myNmea.getBytes(StandardCharsets.UTF_8));
            mNmea1 = NMEA0183.substring(NMEA0183.indexOf("*") + 1);
            mNmea2 = String.format("%02X", calculateXor8.xor);

            if (DataSaved.S_CRS.equals(_NONE)) {
                NmeaInput = NMEA0183.split(",");
                switch (NmeaInput[0]) {

                    case "$PTNL":
                        try {
                            ggaEast = NmeaInput[6].replace("+", "").replace(",", ".");
                            ;
                            mLon_1 = Double.parseDouble(ggaEast);
                            ggaNord = NmeaInput[4].replace("+", "").replace(",", ".");
                            ;
                            mLat_1 = Double.parseDouble(ggaNord);
                            ggaQuality = NmeaInput[8];
                            ggaSat = NmeaInput[9];
                            qualityTrimble();
                            VRMS_ = "0.000";
                            Quota1 = DataSaved.offset_Z_antenna + Double.parseDouble(NmeaInput[11].replace("GHT+", "").replace("EHT+", "").replace(",", "."));
                            Deg2UTM deg2UTM = new Deg2UTM(mLat_1, mLon_1, Quota1, DataSaved.S_CRS,MyApp.GEOIDE_PATH);
                            Nord1 = deg2UTM.getNorthing();
                            Est1 = deg2UTM.getEasting();
                            mChar = deg2UTM.getLetter();
                            mZone = deg2UTM.getZone();

                        } catch (Exception e) {
                        }

                        break;

                    case "$GNLLQ":
                    case "$GPLLQ":
                        try {
                            ggaRtk = "0.00";
                            ggaEast = NmeaInput[3].replace(",", ".");
                            mLon_1 = Double.parseDouble(ggaEast);
                            ggaNord = NmeaInput[5].replace(",", ".");
                            mLat_1 = Double.parseDouble(ggaNord);
                            ggaQuality = NmeaInput[7];
                            ggaSat = NmeaInput[8];
                            qualityLeica();
                            VRMS_ = NmeaInput[9];
                            Quota1 = DataSaved.offset_Z_antenna + Double.parseDouble(NmeaInput[10].replace(",", "."));
                            Deg2UTM deg2UTM = new Deg2UTM(mLat_1, mLon_1, Quota1, DataSaved.S_CRS,MyApp.GEOIDE_PATH);
                            Nord1 = deg2UTM.getNorthing();
                            Est1 = deg2UTM.getEasting();
                            mChar = deg2UTM.getLetter();
                            mZone = deg2UTM.getZone();
                        } catch (Exception e) {

                        }
                        break;

                    case "$GPHDT":
                    case "$GNHDT":
                    case "$HCHDT":

                        try {
                            mch_Hdt = Double.parseDouble(NmeaInput[1]);

                            if (NmeaInput[1].equals("0.0000") || NmeaInput[1].equals("")) {
                                mch_Hdt = 999.999;
                            }
                        } catch (Exception e) {
                            mch_Hdt = 999.999;

                        }
                        break;
                }
            } else {

                if (mNmea1.contains(mNmea2)) {

                    NmeaInput = NMEA0183.split(",");
                    switch (NmeaInput[0]) {

                        case "$GNGGA":
                        case "$GPGGA":
                            try {

                                ggaNord = NmeaInput[2];//Latitudine
                                ggaNoS = NmeaInput[3];
                                ggaEast = NmeaInput[4];//Longitudine
                                ggaWoE = NmeaInput[5];
                                ggaQuality = NmeaInput[6];
                                ggaSat = NmeaInput[7];
                                ggaDop = NmeaInput[8];
                                ggaZ1 = NmeaInput[9];
                                ggaZ2 = NmeaInput[11];
                                ggaRtk = NmeaInput[13];

                                quality();

                                //LATITUDINE
                                //4431.1234567
                                int LatInt = Integer.parseInt(ggaNord.substring(0, 2));//estrae il 44 come int
                                double LatDec = Double.parseDouble(ggaNord.substring(2, NmeaInput[2].length()));//estrae 31.1234567 come double
                                if (ggaNoS.equals("N")) {
                                    mLat_1 = LatDec / 60 + LatInt;//divide la parte double /60 e la somma alla componente intera precedentemente estratta
                                    //se è N conclude
                                } else if (ggaNoS.equals("S")) {
                                    mLat_1 = LatDec / 60 + LatInt;
                                    mLat_1 = mLat_1 * -1;
                                    //se è S moltiplica il risultato *-1
                                }


                                //LONGITUDINE
                                //09912.1234567
                                int LonInt = Integer.parseInt(ggaEast.substring(0, 3));//estrae 099 come int
                                double LonDec = Double.parseDouble(ggaEast.substring(3, NmeaInput[2].length()));//estrae 12.1234567 come double
                                if (ggaWoE.equals("E")) {
                                    mLon_1 = LonDec / 60 + LonInt;//divide la parte double /60 e la somma alla componente intera
                                    //se è E conclude
                                } else if (ggaWoE.equals("W")) {
                                    mLon_1 = LonDec / 60 + LonInt;
                                    mLon_1 = mLon_1 * -1;
                                    //se è W moltiplica il risultato *-1
                                }

                                double qtemp = DataSaved.offset_Z_antenna + Double.parseDouble(ggaZ1.replace(",", ".")) + Double.parseDouble(ggaZ2.replace(",", "."));
                                Deg2UTM deg2UTM = new Deg2UTM(mLat_1, mLon_1, qtemp, DataSaved.S_CRS,MyApp.GEOIDE_PATH);
                                Nord1 = deg2UTM.getNorthing();
                                Est1 = deg2UTM.getEasting();
                                Quota1=deg2UTM.getQuota();
                                mChar = deg2UTM.getLetter();
                                mZone = deg2UTM.getZone();

                            } catch (Exception e) {
                            }
                            break;
                        case "$GPHDT":
                        case "$GNHDT":
                        case "$HCHDT":

                            try {
                                mch_Hdt = Double.parseDouble(NmeaInput[1]);

                                if (NmeaInput[1].equals("0.0000") || NmeaInput[1].equals("")) {
                                    mch_Hdt = 999.999;
                                }


                            } catch (Exception e) {
                                mch_Hdt = 999.999;

                            }
                            break;

                        case "$GPGST":
                        case "$GNGST":
                            try {
                                String LatCQ = NmeaInput[6];
                                String LonCQ = NmeaInput[7];
                                String HgtCQ = NmeaInput[8].substring(0, NmeaInput[8].indexOf("*"));
                                VRMS_ = String.format("%.3f", Float.parseFloat(HgtCQ.replace(",",".")));
                                HRMS_ = String.format("%.3f", 2 * Math.sqrt(0.5 * ((Math.pow(Double.parseDouble(LatCQ.replace(",",".")), 2) + Math.pow(Double.parseDouble(LonCQ.replace(",",".")), 2)) / 2)));
                                ;
                                _3DRMS = String.format("%.3f", Math.sqrt(Math.pow(Double.parseDouble(HRMS_), 2) + Math.pow(Double.parseDouble(VRMS_), 2)));
                                ;

                            } catch (Exception e) {
                                VRMS_ = "0.000";
                                HRMS_ = "0.000";
                                _3DRMS = "0.000";
                            }
                            break;
                        case "$GPRMC":
                        case "$GNRMC":
                            break;


                    }
                }
            }
            try {

                mch_Orientation = mch_Hdt;

            } catch (Exception e) {
                mch_Orientation = 0.000;
            }


        } catch (Exception e) {

        }
    }


    public static void NmeaSTX(int id, byte[] data) {

        switch (id) {

            case 0x18FF0110:
                //UTM NORD
                long bigInteger = PLC_DataTypes_LittleEndian.byte_to_S64_le(data);
                tmpNordUTM = bigInteger * 0.001;
                break;

            case 0x18FF0210:
                //UTN EST +WGS84 Z

                tmpEstUTM = PLC_DataTypes_LittleEndian.byte_to_U32(new byte[]{data[0], data[1], data[2], data[3]}) * 0.001;
                tmpQuotaUTM = PLC_DataTypes_LittleEndian.byte_to_S32(new byte[]{data[4], data[5], data[6], data[7]}) * 0.001;
                break;
            case 0x18FF0310:
                mZone = (int) data[0];
                mChar = (char) data[1];
                mch_Orientation = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[2], data[3]}) * 0.01;
                if (mch_Orientation == 655.35) {
                    mch_Hdt = 999.999;
                } else {
                    mch_Hdt = mch_Orientation;
                }
                ggaQuality = String.valueOf(data[4]).replace(",",".");
                quality();
                ggaSat = String.valueOf(data[5]).replace(",",".");
                ggaRtk = String.format("%.1f", (float) PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[6], data[7]}) * 0.1).replace(",",".");

                break;
            case 0x18FF0410:
                //LOCAL NORD
                tmpNordLOC = PLC_DataTypes_LittleEndian.byte_to_S64_le(data) * 0.001;
                break;
            case 0x18FF0510:
                //Lat
                tmpLat = PLC_DataTypes_LittleEndian.byte_to_S64_le(data) * 0.0000000001;
                mLat_1 = tmpLat;

                break;
            case 0x18FF0610:
                //LOCAL EST
                tmpEstLOC = PLC_DataTypes_LittleEndian.byte_to_S64_le(data) * 0.001;
                break;
            case 0x18FF0810:
                //Lon
                tmpLon = PLC_DataTypes_LittleEndian.byte_to_S64_le(data) * 0.000000001;//corrected
                mLon_1 = tmpLon;

                break;
            case 0x18FF0910:
                //RMS
                HRMS_ = String.format("%.3f", PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[0], data[1]}) * 0.001).replace(",",".");
                VRMS_ = String.format("%.3f", PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[2], data[3]}) * 0.001).replace(",",".");
                tmpGeoidSeparator = PLC_DataTypes_LittleEndian.byte_to_S32(new byte[]{data[4], data[5], data[6], data[7]}) * 0.001;
                break;

            case 0x18FF0D10:
                //LOCAL Z
                tmpQuotaLOC = PLC_DataTypes_LittleEndian.byte_to_S32(new byte[]{data[0], data[1], data[2], data[3]}) * 0.001;
                break;


        }
        switch (DataSaved.S_CRS) {
            case _NONE:
                Deg2UTM deg2UTM1 = new Deg2UTM(tmpNordLOC, tmpEstLOC, tmpQuotaLOC, _NONE,MyApp.GEOIDE_PATH);
                Nord1 = deg2UTM1.getNorthing();
                Est1 = deg2UTM1.getEasting();
                Quota1 = DataSaved.offset_Z_antenna + deg2UTM1.getQuota() + tmpGeoidSeparator;
                break;
            case _UTM:
                Deg2UTM deg2UTM_a = new Deg2UTM(tmpNordUTM, tmpEstUTM, tmpQuotaUTM, _NONE,MyApp.GEOIDE_PATH);
                Nord1 = deg2UTM_a.getNorthing();
                Est1 = deg2UTM_a.getEasting();
                Quota1 = DataSaved.offset_Z_antenna + deg2UTM_a.getQuota() + tmpGeoidSeparator;
                break;
            default:
                Deg2UTM deg2UTM2 = new Deg2UTM(tmpLat, tmpLon, tmpQuotaUTM, DataSaved.S_CRS, MyApp.GEOIDE_PATH);
                Nord1 = deg2UTM2.getNorthing();
                Est1 = deg2UTM2.getEasting();
                Quota1 = DataSaved.offset_Z_antenna + deg2UTM2.getQuota() + tmpGeoidSeparator;
                break;

        }


    }


    private static void qualityLeica() {
        if (ggaQuality.equals("3")) {
            ggaQuality = "4";
        }
        switch (ggaQuality) {
            case "1":
                fix1 = "SINGLE";
                break;
            case "2":
                fix1 = "DGPS ";
                break;
            case "3":
                fix1 = "FIX   ";
                break;
            case "4":
                fix1 = "Fix  ";
                break;
            case "5":
                fix1 = "Float";
                break;
            case "6":
                fix1 = "INS  ";
                break;
            default:
                fix1 = "Inv  ";
                break;


        }
    }

    private static void quality() {
        switch (ggaQuality) {
            case "0":
                fix1 = "Inv ";
                break;
            case "1":
                fix1 = "SINGLE";
                break;
            case "2":
                fix1 = "DGPS ";
                break;
            case "3":
                fix1 = "Inv  ";
                break;
            case "4":
                fix1 = "Fix  ";
                break;
            case "5":
                fix1 = "Float";
                break;
            case "6":
                fix1 = "INS  ";
                break;
            default:
                fix1 = "Inv  ";
                break;


        }
    }

    private static void qualityTrimble() {
        if (ggaQuality.equals("3")) {
            ggaQuality = "4";
        }
        switch (ggaQuality) {
            case "0":
                fix1 = "Inv ";
                break;
            case "1":
                fix1 = "SINGLE ";
                break;
            case "2":
                fix1 = "FLOAT ";
                break;
            case "3":
                fix1 = "FIX ";
                break;
            case "4":
                fix1 = "FIX ";
                break;
            case "5":
                fix1 = "SBAS";
                break;
            case "6":
                fix1 = "RTK Float 3D network solution";
                break;
            case "7":
                fix1 = "RTK Fixed 3D network solution";
                break;
            case "8":
                fix1 = "RTK Float 2D network solution";
                break;
            case "9":
                fix1 = "RTK Fixed 2D network solution";
                break;
            case "10":
                fix1 = "OmniSTAR HP/XP solution";
                break;
            case "11":
                fix1 = "OmniSTAR VBS solution";
                break;
            case "12":
                fix1 = "Location RTK";
                break;
            case "13":
                fix1 = "Beacon DGPS";
                break;
            case "14":
                fix1 = "CenterPoint RTX";
                break;
            case "15":
                fix1 = "xFill";
                break;
        }
    }


}
