package packexcalib.exca;

import static gui.gps.NmeaGenerator.HEADING;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static packexcalib.exca.Sensors_Decoder.Deg_Tool_Pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_Tool_Roll;
import static packexcalib.exca.Sensors_Decoder.Deg_boom1;
import static packexcalib.exca.Sensors_Decoder.Deg_boom2;
import static packexcalib.exca.Sensors_Decoder.Deg_bucket;
import static packexcalib.exca.Sensors_Decoder.Deg_pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_roll;
import static packexcalib.exca.Sensors_Decoder.Deg_stick;
import static packexcalib.exca.Sensors_Decoder.ExtensionBoom;
import static utils.MyTypes.DEMO_BAG;
import static utils.MyTypes.FMI_SENS;
import static utils.MyTypes.TSM_ACC;
import static utils.MyTypes.TSM_ANGOLARI;

import android.util.Log;

import gui.gps.NmeaGenerator;
import packexcalib.gnss.NmeaListener;


public class Sensors_Decoder_Drill {
    public static long EncRevolution;
    public static double RopeLen;
    static double yaw;
    static boolean boom1P, boom1M, stickP, stickM, bucketA, bucketC, rotL, rotR, latP, latM, lonP, lonM, qP, qM;
    static double norm, ax_norm, ay_norm, az_norm;
    static double qW, qX, qY, qZ, qnorm, mqW, mqX, mqY, mqZ, _x486, _y486, _z486;
    static double[] eulerAngles;
    static short acc_x;
    static short acc_y;
    static short acc_z;
    static short Gx;
    static short Gy;
    static short Gz;
    static long K = (long) Math.pow(2, 32);


    public static void decode(int id, byte[] data) {
        try {
            if (id == 0x18F || id == 0x190) {
                //TODO Encoder connesso 8192 count per revolution = 0x2000
                long revolution = PLC_DataTypes_LittleEndian.byte_to_U32(new byte[]{data[0], data[1], data[2], data[3]});
                if (DataSaved.lrRotary == -1) {
                    K = (long) Math.pow(2, 32);
                    EncRevolution = K - revolution;
                } else {
                    EncRevolution = revolution;
                }
                RopeLen = calculateRopeLength(EncRevolution, DataSaved.Rotary_Diam);

            }
            if (DataSaved.isExtensionBoom > 0) {
                if (id == 0x188) {
                    int v = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                    ExtensionBoom = -v * 0.0001;
                    ExtensionBoom = Math.max(ExtensionBoom, 0);
                }
            } else {
                ExtensionBoom = 0;
            }

            switch (DataSaved.isCanOpen) {
                case FMI_SENS:
                    switch (id & 0x1FFFFFFF) {
                        case 0x181:
                            //Frame
                            double[] out = TiltEncript.encriptFMI_Frame(data, DataSaved.lrFrame);
                            Deg_pitch = out[0];
                            Deg_roll = out[1];

                            break;
                        case 0x182:
                            //boom1
                            Deg_boom1 = TiltEncript.encriptFMI_Boom(data, DataSaved.lrBoom1)[0];
                            //Log.d("FEYMAN",String.format("%.2f",Deg_boom1));
                            break;
                        case 0x187:
                            //boom2
                            Deg_boom2 = TiltEncript.encriptFMI_Boom(data, DataSaved.lrBoom2)[0];
                            break;
                        case 0x184:
                            //stick
                            double[] out1 = TiltEncript.encriptFMI_Boom(data, DataSaved.lrStick);
                            Deg_stick = out1[0];
                            if (DataSaved.Extra_Heading > 0) {
                                Deg_Boom_Roll = out1[1];
                            } else {
                                Deg_Boom_Roll = 0d;
                            }
                            //Log.d("FEYMAN",String.format("%.2f",Deg_stick)+"  "+String.format("%.2f",Deg_Boom_Roll));
                            if (DataSaved.lrFrame == 0) {
                                Deg_Boom_Roll = 0;
                            }
                            break;
                        case 0x185:
                            //bucket TSM
                            double[] dat0 = TiltEncript.encriptFMI_Tool(data, DataSaved.lrTool);
                            Deg_Tool_Pitch = dat0[0];
                            Deg_Tool_Roll = dat0[1];
                            break;

                    }
                    if (DataSaved.Extra_Heading == 0) {
                        Deg_Boom_Roll = Deg_roll;
                    }
                    DrillLib.Drill();

                    break;

                case TSM_ANGOLARI:
                case TSM_ACC:
                    switch (id & 0x1FFFFFFF) {
                        case 0x381:
                            double[] out = TiltEncript.encriptTSM_Frame(data, DataSaved.lrFrame);
                            Deg_pitch = out[0];
                            Deg_roll = out[1];
                            break;

                        case 0x382:
                            Deg_boom1 = TiltEncript.encriptTSM_Boom(data, DataSaved.lrBoom1)[0];
                            break;

                        case 0x387:
                            Deg_boom2 = TiltEncript.encriptTSM_Boom(data, DataSaved.lrBoom2)[0];

                            break;

                        case 0x384:
                            double[] out1 = TiltEncript.encriptTSM_Boom(data, DataSaved.lrStick);
                            Deg_stick = out1[0];
                            if (DataSaved.Extra_Heading > 0) {
                                Deg_Boom_Roll = out1[1];
                            } else {
                                Deg_Boom_Roll = 0d;
                            }

                            if (DataSaved.lrFrame == 0) {
                                Deg_Boom_Roll = 0;
                            }

                            break;

                        case 0x385:
                            //bucket TSM
                            double[] dat = TiltEncript.encriptTSM_Tool(data, DataSaved.lrTool);
                            Deg_Tool_Pitch = dat[0];
                            Deg_Tool_Roll = dat[1];
                            break;

                    }
                    if (DataSaved.Extra_Heading == 0) {
                        Deg_Boom_Roll = Deg_roll;
                    }
                    DrillLib.Drill();
                    break;
                case DEMO_BAG:
                    switch (id) {

                        case 0x395:

                            boolean[] b = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                            boom1P = b[3];
                            boom1M = b[7];
                            bucketC = b[1];
                            bucketA = b[5];

                            break;

                        case 0x295:

                            int mTilt = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[1], data[2]});
                            Deg_Tool_Roll = mTilt * 1d;

                            break;

                        case 0x2F0:

                            break;
                        case 0x3F0:
                            boolean[] ba = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                            stickP = ba[7];
                            stickM = ba[3];

                            break;



                        case 0x186:


                            break;

                    }

                    if (boom1P) {
                        Deg_boom1 += 0.05;
                    }
                    if (boom1M) {
                        Deg_boom1 -= 0.05;
                    }
                    if (stickP) {
                        Deg_stick += 0.05;
                    }
                    if (stickM) {
                        Deg_stick -= 0.05;
                    }
                    if (bucketA) {

                        Deg_Tool_Pitch += 0.05;
                    }
                    if (bucketC) {
                        Deg_Tool_Pitch -= 0.05;

                    }

                    if (DataSaved.Extra_Heading == 0) {
                        Deg_Boom_Roll = Deg_roll;
                    }
                    if (DataSaved.my_comPort == 4) {
                        //demo ROLLER
                        switch (id) {
                            case 0x195:

                                boolean[] boo = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                                latP = boo[6];
                                latM = boo[7];
                                break;
                            case 0x1F0:
                                boolean[] be = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                                qP = be[4];
                                qM = be[5];
                                lonP = be[6];
                                lonM = be[7];

                                break;
                            case 0x3F0:
                                boolean[] ba = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                                rotL = ba[1];
                                rotR = ba[5];
                                break;
                        }
                        boolean[] keyEvents = new boolean[]{rotL, rotR, latP, latM, lonP, lonM, qP, qM};


                        if (keyEvents[0]) {
                            // rotLeft
                            HEADING -= 0.05;
                            HEADING = normalizeAngle(HEADING);


                        }
                        if (keyEvents[1]) {
                            // rotRight
                            HEADING += 0.05;
                            HEADING = normalizeAngle(HEADING);

                        }
                        if (DataSaved.portView == 1) {
                            NmeaListener.roof_Orientation = HEADING;
                        }

                        if (keyEvents[2]) {
                            // lat+
                            NmeaGenerator.LATITUDE += 0.005;

                        }
                        if (keyEvents[3]) {
                            // lat-
                            NmeaGenerator.LATITUDE -= 0.005;

                        }
                        if (keyEvents[4]) {
                            // F4
                            NmeaGenerator.LONGITUDE += 0.005;

                        }
                        if (keyEvents[5]) {
                            // lon+
                            NmeaGenerator.LONGITUDE -= 0.005;

                        }


                        if (keyEvents[6]) {
                            NmeaGenerator.ALTITUDE += 0.001;
                        }
                        if (keyEvents[7]) {
                            NmeaGenerator.ALTITUDE -= 0.001;
                        }
                    }
                    DrillLib.Drill();
                    break;
            }

        } catch (Exception e) {
            Log.e("Sens_Drill", Log.getStackTraceString(e));
        }
    }

    private static double normalizeAngle(double a) {
        a = a % 360.0;
        if (a > 180) a -= 360;
        if (a < -180) a += 360;
        return a;
    }

    public static double calculateRopeLength(long encoderCounts, double pulleyDiameter) {
        final double COUNTS_PER_REV = 8192.0;

        double revolutions = encoderCounts / COUNTS_PER_REV;
        double circumference = Math.PI * pulleyDiameter;

        return revolutions * circumference;
    }

}
