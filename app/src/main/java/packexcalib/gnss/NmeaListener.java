package packexcalib.gnss;

import static packexcalib.gnss.CRS_Strings._LOCAL_COORDINATES_FROM_GNSS;
import static packexcalib.gnss.CRS_Strings._NONE;
import static packexcalib.gnss.CRS_Strings._UTM;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import packexcalib.exca.DataSaved;
import packexcalib.exca.PLC_DataTypes_LittleEndian;


public class NmeaListener {
    public static String date_time_dmy = "";
    public static String date_time_ymd = "";
    public static String date_time_iso = "";
    public static final int FORMAT_DDMMYYYY = 0;
    public static final int FORMAT_YYYYMMDD = 1;
    public static final int FORMAT_ISO = 2;
    static CoordinateXYZ coordinateXYZLLQ, coordinateXYZJKT, coordinateXYZ_1, coordinateXYZ, coord, coordUTM;
    public static double tmpQuotaUTM;
    static double tmpQuotaLOC;
    static double tmpNordUTM;
    static double tmpEstUTM;
    static double tmpNordLOC;
    static double tmpEstLOC;
    static double tmpLat;
    static double tmpLon;
    public static double tmpGeoidSeparator;

    public static String[] NmeaInput;
    static CalculateXor8 calculateXor8;
    static String myNmea;
    static String mNmea1, mNmea2;

    public static char mChar;
    public static int mZone;
    public static String VRMS_, HRMS_, _3DRMS;
    public static double mLat_1, mLon_1;
    public static double Nord1, Est1, Quota1, mch_Orientation, mch_Hdt, mch_Hdt_1, roof_Orientation;
    public static String ggaNord, ggaEast, ggaNoS, ggaWoE, ggaZ1, ggaZ2, ggaSat, ggaDop, ggaQuality, ggaRtk, fix1;//String data from  GPS1
    static boolean hdtError = false;
    static Can318PositionDecoder posDecoder = new Can318PositionDecoder();
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

            if (DataSaved.my_comPort == 4) {//momentaneamente escluso
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

                            coordinateXYZJKT = Deg2UTM.trasform(mLat_1, mLon_1, Quota1, DataSaved.S_CRS);
                            Nord1 = coordinateXYZJKT.getNorthing();
                            Est1 = coordinateXYZJKT.getEasting();
                            mChar = coordinateXYZJKT.getLetter();
                            mZone = coordinateXYZJKT.getZone();

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
                            coordinateXYZLLQ = Deg2UTM.trasform(mLat_1, mLon_1, Quota1, DataSaved.S_CRS);
                            Nord1 = coordinateXYZLLQ.getNorthing();
                            Est1 = coordinateXYZLLQ.getEasting();
                            mChar = coordinateXYZLLQ.getLetter();
                            mZone = coordinateXYZLLQ.getZone();
                            Log.d("Calling", coordinateXYZLLQ.getNorthing() + "  " + coordinateXYZLLQ.getEasting() + "  " + coordinateXYZLLQ.getQuota());
                        } catch (Exception ignored) {

                        }
                        break;

                    case "$GPHDT":
                    case "$GNHDT":
                    case "$HCHDT":

                        try {
                            if (NmeaInput[1].equals("0.0000") || NmeaInput[1].equals("")) {
                                mch_Hdt = 999.999;

                            } else {
                                mch_Hdt = Double.parseDouble(NmeaInput[1]);
                                mch_Orientation = mch_Hdt;
                            }
                        } catch (Exception e) {
                            Log.e("erroreHDT", Log.getStackTraceString(e));

                        }
                        break;
                }
            } else {

                if (mNmea1.contains(mNmea2)) {

                    NmeaInput = NMEA0183.split(",");
                    switch (NmeaInput[0]) {

                        case "$GNGGA":
                        case "$GPGGA":

                            if (DataSaved.my_comPort != 0) {
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

                                    coordinateXYZ_1 = Deg2UTM.trasform(mLat_1, mLon_1, qtemp, DataSaved.S_CRS);
                                    Nord1 = coordinateXYZ_1.getNorthing();
                                    Est1 = coordinateXYZ_1.getEasting();
                                    Quota1 = coordinateXYZ_1.getQuota();
                                    mChar = coordinateXYZ_1.getLetter();
                                    mZone = coordinateXYZ_1.getZone();

                                } catch (Exception e) {
                                    Log.e("TestCRSSS", Log.getStackTraceString(e));
                                }
                            }
                            break;
                        case "$GPHDT":
                        case "$GNHDT":
                        case "$HCHDT":

                            if (DataSaved.my_comPort != 0) {
                                try {
                                    if (NmeaInput[1].equals("0.0000") || NmeaInput[1].equals("")) {
                                        mch_Hdt = 999.999;
                                    } else {
                                        mch_Hdt = Double.parseDouble(NmeaInput[1]);
                                        mch_Orientation = mch_Hdt;


                                    }

                                } catch (Exception e) {


                                }
                                if (DataSaved.portView < 2) {
                                    if (DataSaved.my_comPort == 1 || DataSaved.my_comPort == 2 || DataSaved.my_comPort == 3) {
                                        roof_Orientation = mch_Hdt;
                                    }
                                }

                            }

                            break;

                        case "$GPGST":
                        case "$GNGST":
                            try {
                                String LatCQ = NmeaInput[6];
                                String LonCQ = NmeaInput[7];
                                String HgtCQ = NmeaInput[8].substring(0, NmeaInput[8].indexOf("*"));
                                VRMS_ = String.format("%.3f", Float.parseFloat(HgtCQ.replace(",", ".")));
                                HRMS_ = String.format("%.3f", 2 * Math.sqrt(0.5 * ((Math.pow(Double.parseDouble(LatCQ.replace(",", ".")), 2) + Math.pow(Double.parseDouble(LonCQ.replace(",", ".")), 2)) / 2)));
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
                            if (DataSaved.my_comPort != 0) {
                                date_time_dmy = dateTimeFromRMC(NMEA0183, FORMAT_DDMMYYYY);
                                date_time_ymd = dateTimeFromRMC(NMEA0183, FORMAT_YYYYMMDD);
                                date_time_iso = dateTimeFromRMC(NMEA0183, FORMAT_ISO);

                            }
                            break;


                    }
                }
            }


        } catch (Exception ignored) {

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
                //UTM EST +WGS84 Z

                tmpEstUTM = PLC_DataTypes_LittleEndian.byte_to_U32(new byte[]{data[0], data[1], data[2], data[3]}) * 0.001;
                tmpQuotaUTM = PLC_DataTypes_LittleEndian.byte_to_S32(new byte[]{data[4], data[5], data[6], data[7]}) * 0.001;
                break;
            case 0x18FF0310:
                mZone = (int) data[0];
                mChar = (char) data[1];
                mch_Orientation = PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[2], data[3]}) * 0.01;
                if (mch_Orientation == 655.35) {
                    mch_Hdt_1 = 999.999;
                } else {
                    mch_Hdt_1 = mch_Orientation;
                }
                if (DataSaved.portView < 2) {
                    if (DataSaved.my_comPort == 0) {
                        roof_Orientation = mch_Hdt_1;
                    }
                }
                ggaQuality = String.valueOf(data[4]).replace(",", ".");
                quality();
                ggaSat = String.valueOf(data[5]).replace(",", ".");
                ggaRtk = String.format("%.1f", (float) PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[6], data[7]}) * 0.1).replace(",", ".");

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
                HRMS_ = String.format("%.3f", PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[0], data[1]}) * 0.001).replace(",", ".");
                VRMS_ = String.format("%.3f", PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[2], data[3]}) * 0.001).replace(",", ".");
                tmpGeoidSeparator = PLC_DataTypes_LittleEndian.byte_to_S32(new byte[]{data[4], data[5], data[6], data[7]}) * 0.001;
                break;
            case 0x18FF0B10:
                date_time_dmy = parseCanDateTime(data, FORMAT_DDMMYYYY);
                date_time_ymd = parseCanDateTime(data, FORMAT_YYYYMMDD);
                date_time_iso = parseCanDateTime(data, FORMAT_ISO);
                break;

            case 0x18FF0D10:
                //LOCAL Z
                tmpQuotaLOC = PLC_DataTypes_LittleEndian.byte_to_S32(new byte[]{data[0], data[1], data[2], data[3]}) * 0.001;
                break;


        }
        switch (DataSaved.S_CRS) {
            case _NONE:
                coord = Deg2UTM.trasform(tmpLat, tmpLon, tmpQuotaUTM, _NONE);
                Nord1 = coord.getNorthing();
                Est1 = coord.getEasting();
                Quota1 = DataSaved.offset_Z_antenna + coord.getQuota() + tmpGeoidSeparator;

                break;
            case _LOCAL_COORDINATES_FROM_GNSS:
                coord = Deg2UTM.trasform(tmpNordLOC, tmpEstLOC, tmpQuotaLOC, _LOCAL_COORDINATES_FROM_GNSS);
                Nord1 = coord.getNorthing();
                Est1 = coord.getEasting();
                Quota1 = DataSaved.offset_Z_antenna + coord.getQuota();
                break;

            case _UTM:

                coordUTM = Deg2UTM.trasform(mLat_1, mLon_1, tmpQuotaUTM, _UTM);
                Nord1 = coordUTM.getNorthing();
                Est1 = coordUTM.getEasting();
                Quota1 = DataSaved.offset_Z_antenna + coordUTM.getQuota() + tmpGeoidSeparator;

                break;
            default:

                coordinateXYZ = Deg2UTM.trasform(tmpLat, tmpLon, tmpQuotaUTM, DataSaved.S_CRS);
                Nord1 = coordinateXYZ.getNorthing();
                Est1 = coordinateXYZ.getEasting();
                Quota1 = DataSaved.offset_Z_antenna + coordinateXYZ.getQuota() + tmpGeoidSeparator;

                break;

        }


    }


    public static void NmeaLeica(int id, byte[] data, int dlc) {
        Can318PositionDecoder.Output o = posDecoder.feed(data, dlc);

        // o sarà != null quando ha finito di ricomporre il 3D (ultimo frame del blocco)
        if (o != null) {
            mLat_1 = o.latDeg;
            mLon_1 = o.lonDeg;
            tmpQuotaUTM = o.alt;
            mch_Orientation = o.headingDeg;
            if (Double.isNaN(mch_Orientation)) {
                mch_Hdt_1 = 999.999;
            } else {
                mch_Hdt_1 = mch_Orientation;
            }
            if (DataSaved.portView < 2) {
                if (DataSaved.my_comPort == 0) {
                    roof_Orientation = mch_Hdt_1;
                }
            }
            VRMS_ = String.valueOf(o.cq);
            switch (o.fixType) {
                case 0:
                    ggaQuality = "0";
                    fix1 = "Inv";
                    break;
                case 1:
                    ggaQuality = "1";
                    fix1 = "SINGLE";
                    break;
                case 2:
                    ggaQuality = "2";
                    fix1 = "DGPS";
                    break;
                case 3:
                    ggaQuality = "4";
                    fix1 = "FIX";
                    break;
                case 4:
                    ggaQuality = "5";
                    fix1 = "FLOAT";
                    break;

            }
            ggaSat = String.valueOf(o.satUsed);
            if (Double.isNaN(o.correctionAge)) {
                ggaRtk = "0.0";
            } else {
                ggaRtk = String.valueOf(o.correctionAge);
            }

/*
            Log.d("POS3D",
                    "Lat=" + o.latDeg +
                            " Lon=" + o.lonDeg +
                            " Alt=" + o.alt +
                            " RelLat=" + o.relLat +
                            " RelLon=" + o.relLon +
                            " RelAlt=" + o.relAlt +
                            " HDT=" + o.headingDeg +
                            " CQ=" + o.cq +
                            " Fix=" + o.fixType +
                            " SatUsed=" + o.satUsed +
                            " SatTracked=" + o.satTracked +
                            " DOPv=" + o.dopVertical +
                            " DOPh=" + o.dopHorizontal +
                            " DOPg=" + o.dopGeometric+
                            " Age="+o.correctionAge
            );*/

            switch (DataSaved.S_CRS) {
                case _NONE:
                    coord = Deg2UTM.trasform(mLat_1, mLon_1, tmpQuotaUTM, _NONE);
                    Nord1 = coord.getNorthing();
                    Est1 = coord.getEasting();
                    Quota1 = DataSaved.offset_Z_antenna + coord.getQuota();
                    mChar = coord.getLetter();
                    mZone = coord.getZone();

                    break;

                case _UTM:

                    coordUTM = Deg2UTM.trasform(mLat_1, mLon_1, tmpQuotaUTM, _UTM);
                    Nord1 = coordUTM.getNorthing();
                    Est1 = coordUTM.getEasting();
                    Quota1 = DataSaved.offset_Z_antenna + coordUTM.getQuota();
                    mChar = coordUTM.getLetter();
                    mZone = coordUTM.getZone();
                    break;
                default:
                    coordinateXYZ = Deg2UTM.trasform(mLat_1, mLon_1, tmpQuotaUTM, DataSaved.S_CRS);
                    Nord1 = coordinateXYZ.getNorthing();
                    Est1 = coordinateXYZ.getEasting();
                    Quota1 = DataSaved.offset_Z_antenna + coordinateXYZ.getQuota();
                    mChar = coordinateXYZ.getLetter();
                    mZone = coordinateXYZ.getZone();
                    break;

            }
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

    public static String parseCanDateTime(byte[] canData, int formatType) {

        if (canData == null || canData.length != 8) {
            throw new IllegalArgumentException("CAN data must be exactly 8 bytes");
        }

        // ---- DATE d0..d3 : DDMMYYYY (U32 LE) ----
        long dateValue = ByteBuffer
                .wrap(canData, 0, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt() & 0xFFFFFFFFL;

        int day   = (int) (dateValue / 1_000_000);
        int month = (int) ((dateValue / 10_000) % 100);
        int year  = (int) (dateValue % 10_000);

        // ---- TIME d4..d7 : HHMMSSmmm (U32 LE) ----
        long timeValue = ByteBuffer
                .wrap(canData, 4, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt() & 0xFFFFFFFFL;

        int hour        = (int) (timeValue / 10_000_000);
        int minute      = (int) ((timeValue / 100_000) % 100);
        int second      = (int) ((timeValue / 1_000) % 100);
        int millisecond = (int) (timeValue % 1_000);

        // ---- UTC ZonedDateTime ----
        ZonedDateTime zdt = ZonedDateTime.of(
                year, month, day,
                hour, minute, second,
                millisecond * 1_000_000,
                ZoneOffset.UTC
        );

        DateTimeFormatter formatter;

        switch (formatType) {
            case FORMAT_YYYYMMDD:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                break;

            case FORMAT_ISO:
                formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                break;

            case FORMAT_DDMMYYYY:
            default:
                formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
                break;
        }

        return zdt.format(formatter);
    }




    public static String dateTimeFromRMC(String rmc, int formatType) {

        if (rmc == null || !rmc.contains("RMC")) {
            throw new IllegalArgumentException("Invalid RMC sentence");
        }

        String[] f = rmc.split(",");

        if (f.length < 10 || !"A".equals(f[2])) {
            throw new IllegalArgumentException("RMC missing valid date/time");
        }

        // TIME hhmmss.sss
        String t = f[1];
        int hour = Integer.parseInt(t.substring(0, 2));
        int minute = Integer.parseInt(t.substring(2, 4));
        int second = Integer.parseInt(t.substring(4, 6));

        int millisecond = 0;
        if (t.contains(".")) {
            String ms = t.substring(t.indexOf('.') + 1);
            millisecond = Integer.parseInt((ms + "000").substring(0, 3));
        }

        // DATE ddmmyy
        String d = f[9];
        int day = Integer.parseInt(d.substring(0, 2));
        int month = Integer.parseInt(d.substring(2, 4));
        int year = 2000 + Integer.parseInt(d.substring(4, 6));

        ZonedDateTime zdt = ZonedDateTime.of(
                year, month, day,
                hour, minute, second,
                millisecond * 1_000_000,
                ZoneOffset.UTC
        );
        ZonedDateTime local = zdt.withZoneSameInstant(ZoneId.systemDefault());

        return formatDateTime(local, formatType);
    }


    public static String formatDateTime(ZonedDateTime zdt, int formatType) {

        if (zdt == null) return "";

        DateTimeFormatter formatter;

        switch (formatType) {

            case FORMAT_DDMMYYYY:
                // es: 18/02/2026 14:32:10
                formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                break;

            case FORMAT_YYYYMMDD:
                // es: 2026-02-18 14:32:10
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                break;

            case FORMAT_ISO:
                // es: 2026-02-18T14:32:10+01:00
                formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                break;

            default:
                throw new IllegalArgumentException("Unknown formatType: " + formatType);
        }

        return zdt.format(formatter);
    }


}
