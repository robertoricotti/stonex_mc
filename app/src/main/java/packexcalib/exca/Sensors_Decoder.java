package packexcalib.exca;

import static packexcalib.exca.DataSaved.HEADING;
import static utils.MyTypes.DEMO_BAG;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.FMI_SENS;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.JOYSTICKS;
import static utils.MyTypes.TSM_ACC;
import static utils.MyTypes.WHEELLOADER;


import android.util.Log;

import packexcalib.gnss.NmeaListener;
import utils.MyMCUtils;


public class Sensors_Decoder {
    static boolean boom1P, boom1M, stickP, stickM, bucketA, bucketC, rotL, rotR, latP, latM, lonP, lonM, qP, qM;
    public static double Deg_roll, Deg_pitch, Deg_boom1, Deg_boom2, Deg_stick, Deg_bucket, Deg_tilt, Deg_Benna_W_Tilt, Deg_bucket_DEMO,
            Deg_Boom_Roll, Deg_Yaw_Tilt, Deg_Yaw_Frame, Deg_Roto, ExtensionBoom, Deg_Tool_Roll,Deg_Tool_Pitch;
    public static int V_Laser = 255, WheelSteer;
    static double norm, ax_norm, ay_norm, az_norm;
    static short acc_x;
    static short acc_y;
    static short acc_z;

    final static int PGN_Tiltrotator = 61460;
    final static int PGN_TiltrotatorEPS = 65488;
    final static int PGN_TiltRotator_EngCon = 131024;
    static int countTiltRot;

    public static void Sensors_Decoder() {

    }

    public static void decode(int id, byte[] data) {
        if(DataSaved.isCanOpen==JOYSTICKS){
            return;
        }
        try {

            if (DataSaved.isExtensionBoom > 0) {
                if (id == 0x188) {
                    int v = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                    ExtensionBoom = -v * 0.0001;
                    ExtensionBoom = Math.max(ExtensionBoom, 0);
                }
            } else {
                ExtensionBoom = 0;
            }

            if (DataSaved.Extra_Heading > 0) {
                if (id == 0x1A2) {
                    WheelSteer = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});

                    WheelSteer = WheelSteer * DataSaved.Wheel_Steer_Rev;
                    DataSaved.SteerWheel_Result = MyMCUtils.profile_3pt(WheelSteer,
                            DataSaved.Wheel_Steer_Min,
                            DataSaved.Wheel_Steer_Med,
                            DataSaved.Wheel_Steer_Max,
                            DataSaved.Wheel_Steer_Range);
                }
            } else {
                WheelSteer = 0;
            }


            switch (DataSaved.isWL) {
                case EXCAVATOR:
                case WHEELLOADER://wheel loader
                    switch (DataSaved.isCanOpen) {
                        case FMI_SENS:
                            countTiltRot++;
                            if (id > 2048 && (PGNExtractor.extractPGN(id) == PGN_Tiltrotator || PGNExtractor.extractPGN(id) == PGN_TiltrotatorEPS || PGNExtractor.extractPGN(id) == PGN_TiltRotator_EngCon)) {
                                countTiltRot = 0;
                                DataSaved.isTiltRotator = true;
                                if (PGNExtractor.extractPGN(id) == PGN_Tiltrotator) {
                                    double cost = (PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]}) * 100d) * 0.01d;

                                    Deg_Roto = cost * (1d / 128d) - 200d;
                                } else if (PGNExtractor.extractPGN(id) == PGN_TiltrotatorEPS) {
                                    double costa = (PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[0], data[1]}) * 100d) * 0.01d;

                                    Deg_Roto = costa * (1d / 128d) - 0d;
                                } else if (PGNExtractor.extractPGN(id) == PGN_TiltRotator_EngCon) {
                                    double costa = (PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[0], data[1]}) * 100d) * 0.01d;

                                    Deg_Roto = costa * (1d / 128d) - 0d;

                                }
                                if (DataSaved.reverseRotator == 1) {
                                    Deg_Roto = Deg_Roto * -1;
                                }

                            }
                            if (countTiltRot > 500) {
                                DataSaved.isTiltRotator = false;
                            }

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
                                    //benna
                                    Deg_bucket = TiltEncript.encriptFMI_Bucket(data, DataSaved.lrBucket)[0];
                                    //Log.d("FEYMAN",String.format("%.2f",Deg_bucket));
                                    break;
                                case 0x186:
                                    //tilt
                                    double[] out2 = TiltEncript.encriptFMI_Tilt(data, DataSaved.lrTilt);
                                    Deg_Benna_W_Tilt = out2[0];
                                    Deg_tilt = out2[1];
                                    Deg_Yaw_Tilt = out2[2];
                                    //Log.d("FEYMAN",String.format("%.2f",Deg_Benna_W_Tilt)+"  "+String.format("%.2f",Deg_tilt));
                                    break;
                                case 0x560106A:

                                    //Tilt MOBA
                                    double[] eulrs = TiltEncript.encriptNOVATRON_Tilt(data, DataSaved.lrTilt);
                                    Deg_Benna_W_Tilt = eulrs[0];
                                    Deg_tilt = eulrs[1];
                                    Deg_Yaw_Tilt = eulrs[2];
                                    break;
                                case 0x204301:
                                    if (DataSaved.laserOn == 1) {
                                        V_Laser = (int) data[2] & 0xFF;
                                    }
                                    break;
                            }
                            ExcavatorLib.Excavator();
                            break;

                        case TSM_ACC:

                            countTiltRot++;
                            if (id > 2048 && (PGNExtractor.extractPGN(id) == PGN_Tiltrotator || PGNExtractor.extractPGN(id) == PGN_TiltrotatorEPS || PGNExtractor.extractPGN(id) == PGN_TiltRotator_EngCon)) {
                                countTiltRot = 0;
                                DataSaved.isTiltRotator = true;

                                if (PGNExtractor.extractPGN(id) == PGN_Tiltrotator) {
                                    double cost = (PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]}) * 100d) * 0.01d;

                                    Deg_Roto = cost * (1d / 128d) - 200d;
                                } else if (PGNExtractor.extractPGN(id) == PGN_TiltrotatorEPS) {
                                    double costa = (PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[0], data[1]}) * 100d) * 0.01d;

                                    Deg_Roto = costa * (1d / 128d) - 0d;
                                } else if (PGNExtractor.extractPGN(id) == PGN_TiltRotator_EngCon) {
                                    double costa = (PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[0], data[1]}) * 100d) * 0.01d;

                                    Deg_Roto = costa * (1d / 128d) - 0d;

                                }
                                if (DataSaved.reverseRotator == 1) {
                                    Deg_Roto = Deg_Roto * -1;
                                }


                            }
                            if (countTiltRot > 500) {
                                DataSaved.isTiltRotator = false;
                            }
                            //TSM gravity vector
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
                                    //double [] mout=TiltEncript.encriptTSM_Boom(data, 1);
                                    //Log.w("TestTSM","387:  Pitch:"+String.format("%.2f",mout[0])+"    Roll:"+String.format("%.2f",mout[1]));
                                    Deg_boom2 = TiltEncript.encriptTSM_Boom(data, DataSaved.lrBoom2)[0];

                                    break;

                                case 0x384:
                                   // double [] moutS=TiltEncript.encriptTSM_Boom(data, 1);
                                    //Log.d("TestTSM","384:  Pitch:"+String.format("%.2f",moutS[0])+"    Roll:"+String.format("%.2f",moutS[1]));
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
                                    Deg_bucket = TiltEncript.encriptTSM_Bucket(data, DataSaved.lrBucket)[0];
                                    break;


                                case 0x560106A:
                                    //
                                    //Tilt MOBA
                                    double[] eulrs = TiltEncript.encriptNOVATRON_Tilt(data, DataSaved.lrTilt);
                                    Deg_Benna_W_Tilt = eulrs[0];
                                    Deg_tilt = eulrs[1];
                                    Deg_Yaw_Tilt = eulrs[2];
                                    break;
                                case 0x386:
                                    //tilt TSM
                                    double[] out2 = TiltEncript.encriptTSM_Tilt(data, DataSaved.lrTilt);
                                    Deg_Benna_W_Tilt = out2[0];
                                    Deg_tilt = out2[1];
                                    Deg_Yaw_Tilt = out2[2];

                                    break;


                                case 0x204301://verificare id laser 29bit

                                    if (DataSaved.laserOn == 1) {
                                        V_Laser = (int) data[2] & 0xFF;
                                    }
                                    break;

                            }
                            if (DataSaved.Extra_Heading == 0) {
                                Deg_Boom_Roll = Deg_roll;
                            }
                            ExcavatorLib.Excavator();
                            break;

                    }


                    break;
                case DOZER:
                case DOZER_SIX:
                case GRADER:
                    //Dozer e grader
                    switch (id & 0x1FFFFFFF) {
                        case 0x386:
                        case 0x385:
                        case 0x381:
                            double[] outGrad = TiltEncript.encriptTSM_Blade(data, DataSaved.lrBucket);
                            Deg_pitch = outGrad[0];
                            Deg_roll = outGrad[1];

                            break;
                        //
                        case 0x185:
                        case 0x186:
                            double[] outGradF = TiltEncript.encriptFMI_Blade(data, DataSaved.lrBucket);
                            Deg_pitch = outGradF[0];
                            Deg_roll = outGradF[1];
                            break;
                        case 0x560106A:
                            double[] eulers = TiltEncript.encriptNOVATRON_Frame(data, DataSaved.lrBucket);
                            Deg_pitch = eulers[0];
                            Deg_roll = eulers[1];
                            Deg_Yaw_Frame = eulers[2];
                            break;
                    }
                        if(DataSaved.Dozer_UpsideDown>0){
                            Deg_roll*=-1;
                            Deg_pitch*=-1;
                        }
                    ExcavatorLib.Excavator();
                    break;


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
                    DataSaved.demoNORD += 0.005;

                }
                if (keyEvents[3]) {
                    // lat-
                    DataSaved.demoNORD -= 0.005;

                }
                if (keyEvents[4]) {
                    // F4
                    DataSaved.demoEAST += 0.005;

                }
                if (keyEvents[5]) {
                    // lon+
                    DataSaved.demoEAST -= 0.005;

                }


                if (keyEvents[6]) {
                    DataSaved.demoZ += 0.001;
                }
                if (keyEvents[7]) {
                    DataSaved.demoZ -= 0.001;
                }
            }
            if (DataSaved.isCanOpen == DEMO_BAG) {
                switch (id) {
                    case 0x385:

                        acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                        acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                        acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});

                        norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                        ax_norm = (double) acc_y / norm;
                        ay_norm = (double) acc_x / norm;
                        az_norm = (double) acc_z / norm;

                        Deg_bucket_DEMO = (Math.atan2(ax_norm, -az_norm) * 180 / Math.PI);
                        Deg_bucket_DEMO += 180;
                        if (Deg_bucket_DEMO > 180) {
                            Deg_bucket_DEMO -= 360;
                        }
                        if (Deg_bucket_DEMO < -180) {
                            Deg_bucket_DEMO += 360;
                        }

                        break;
                    case 0x395:

                        boolean[] b = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                        boom1P = b[3];
                        boom1M = b[7];
                        bucketC = b[1];
                        bucketA = b[5];

                        break;

                    case 0x295:

                        int mTilt = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[1], data[2]});
                        Deg_tilt = mTilt * 1d;

                        break;
                    case 234868978:
                        if (DataSaved.lrTilt != 0) {
                            DataSaved.isTiltRotator = true;

                            if (PGNExtractor.extractPGN(id) == PGN_TiltRotator_EngCon) {
                                double costa = (PLC_DataTypes_LittleEndian.byte_to_U16(new byte[]{data[0], data[1]}) * 100d) * 0.01d;

                                Deg_Roto = costa * (1d / 128d) - 0d;

                            }
                            if (DataSaved.reverseRotator == 1) {
                                Deg_Roto = Deg_Roto * -1;
                            }

                        }
                        break;
                    case 0x2F0:

                        if (DataSaved.lrTilt != 0) {
                            DataSaved.isTiltRotator = true;
                            int mRoto = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[1], data[2]});

                            Deg_Roto = mRoto * 1d;
                            Deg_Roto = Deg_Roto % 360;
                            if (DataSaved.reverseRotator == 1) {
                                Deg_Roto = Deg_Roto * -1;
                            }
                        }


                        break;
                    case 0x3F0:
                        boolean[] ba = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                        stickP = ba[7];
                        stickM = ba[3];

                        break;


                    case 0x560106A:
                        //
                        //Tilt MOBA
                        double[] eulrs = TiltEncript.encriptNOVATRON_Tilt(data, DataSaved.lrTilt);
                        Deg_Benna_W_Tilt = eulrs[0];
                        Deg_tilt = eulrs[1];
                        Deg_Yaw_Tilt = eulrs[2];
                        break;
                    case 0x186:
                        //tilt
                        double[] out2 = TiltEncript.encriptFMI_Tilt(data, DataSaved.lrTilt);
                        Deg_Benna_W_Tilt = out2[0];
                        Deg_tilt = out2[1];
                        Deg_Yaw_Tilt = out2[2];
                        break;
                    case 0x386:
                        //tilt
                        double[] outt = TiltEncript.encriptTSM_Tilt(data, DataSaved.lrTilt);
                        Deg_Benna_W_Tilt = outt[0];
                        Deg_tilt = outt[1];
                        Deg_Yaw_Tilt = outt[2];
                        break;
                    case 0x204301://verificare id laser 29bit

                        if (DataSaved.laserOn == 1) {
                            V_Laser = (int) data[2] & 0xFF;
                        }
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
                    Deg_Benna_W_Tilt += 0.05;
                    Deg_bucket += 0.05;
                }
                if (bucketC) {
                    Deg_Benna_W_Tilt -= 0.05;
                    Deg_bucket -= 0.05;
                }

                if (DataSaved.Extra_Heading == 0) {
                    Deg_Boom_Roll = Deg_roll;
                }
                ExcavatorLib.Excavator();
            }

        } catch (
                Exception ignored) {

        }


    }

    public static double normalizeAngle(double a) {
        a = a % 360.0;
        if (a > 180) a -= 360;
        if (a < -180) a += 360;
        return a;
    }


}
