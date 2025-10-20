package packexcalib.exca;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import gui.gps.NmeaGenerator;
import utils.MyMCUtils;


public class Sensors_Decoder {
    static double yaw;
    static double previousTimestamp = System.currentTimeMillis(); // Per tenere traccia del tempo
    public static boolean isMobaTilt;
    static boolean boom1P, boom1M, stickP, stickM, bucketA, bucketC, rotL, rotR, latP, latM, lonP, lonM, qP, qM;
    public static double Deg_roll, Deg_pitch, Deg_boom1, Deg_boom2, Deg_stick, Deg_bucket, Deg_tilt, Deg_Benna_W_Tilt, Deg_bucket_DEMO,
            Deg_Boom_Roll, Deg_Yaw_Tilt, Deg_Yaw_Frame, Deg_Roto, ExtensionBoom;
    public static int V_Laser = 255, flagLaserConnected, flagDefault, flagLaser, WheelSteer;

    static double norm, ax_norm, ay_norm, az_norm;
    static double qW, qX, qY, qZ, qnorm, mqW, mqX, mqY, mqZ, _x486, _y486, _z486;
    static double[] eulerAngles;
    static short acc_x;
    static short acc_y;
    static short acc_z;
    static short Gx;
    static short Gy;
    static short Gz;

    final static int PGN_Tiltrotator = 61460;//TODO VALIDO PER GRADER JOHN DEERE
    final static int PGN_TiltrotatorEPS = 65488;
    final static int PGN_TiltRotator_EngCon = 131024;
    static int countTiltRot;


    private static final Queue<Double> boomRollBuffer = new LinkedList<>();
    private static final Queue<Double> tiltBuffer = new LinkedList<>();

    public static void Moba_G2_Decoder_Update(int id, byte[] data) {
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
                case 0://escavatore
                case 1://wheel loader
                    switch (DataSaved.isCanOpen) {
                        case 1:
                            //MOBA TODO replace with newer sensors

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
                                case 90181733:
                                    //0x5601065
                                    //Frame MOBA
                                    mqW = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    mqX = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    mqY = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    mqZ = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[6], data[7]});
                                    qW = mqW / 23768.0d;
                                    qX = mqX / 23768.0d;
                                    qY = mqY / 23768.0d;
                                    qZ = mqZ / 23768.0d;
                                    qnorm = Math.sqrt(qW * qW + qX * qX + qY * qY + qZ * qZ);
                                    qW /= qnorm;
                                    qX /= qnorm;
                                    qY /= qnorm;
                                    qZ /= qnorm;
                                    eulerAngles = quaternionToEuler(qW, qX, qY, qZ);

                                    switch (DataSaved.lrFrame) {
                                        case 0:
                                            Deg_pitch = 0;
                                            Deg_roll = 0;
                                            Deg_Yaw_Frame = 0;
                                            break;
                                        case 1:
                                            //Avanti
                                            Deg_pitch = -eulerAngles[1];
                                            Deg_roll = -eulerAngles[0];
                                            Deg_Yaw_Frame = eulerAngles[2];
                                            break;
                                        case 2:
                                            //Destra
                                            Deg_pitch = -eulerAngles[0];
                                            Deg_roll = eulerAngles[1];
                                            Deg_Yaw_Frame = eulerAngles[2];
                                            break;
                                        case 3:
                                            //Dietro
                                            Deg_pitch = eulerAngles[1];
                                            Deg_roll = eulerAngles[0];
                                            Deg_Yaw_Frame = eulerAngles[2];
                                            break;
                                        case 4:
                                            //Sinistra
                                            Deg_pitch = eulerAngles[0];
                                            Deg_roll = -eulerAngles[1];
                                            Deg_Yaw_Frame = eulerAngles[2];
                                            break;

                                    }
                                    break;
                                case 0x381:
                                    //frame CobID=1
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_x / norm;
                                    ay_norm = (double) acc_y / norm;
                                    az_norm = (double) acc_z / norm;

                                    if (DataSaved.lrFrame == 1) {
                                        Deg_roll = -((Math.atan2(ay_norm, az_norm) * 180 / Math.PI));
                                        Deg_pitch = (Math.atan2(ax_norm, az_norm) * 180 / Math.PI);
                                        Deg_roll += 180;
                                        if (Deg_roll < -180) {
                                            Deg_roll += 360;
                                        } else if (Deg_roll > 180) {
                                            Deg_roll -= 360;
                                        }
                                        Deg_pitch += 180;
                                        if (Deg_pitch < -180) {
                                            Deg_pitch += 360;
                                        } else if (Deg_pitch > 180) {
                                            Deg_pitch -= 360;
                                        }

                                    } else if (DataSaved.lrFrame == 2) {
                                        //right
                                        Deg_roll = -(Math.atan2(ax_norm, az_norm) * 180 / Math.PI);
                                        Deg_pitch = -(Math.atan2(ay_norm, az_norm) * 180 / Math.PI);
                                        Deg_roll += 180;
                                        if (Deg_roll < -180) {
                                            Deg_roll += 360;
                                        } else if (Deg_roll > 180) {
                                            Deg_roll -= 360;
                                        }
                                        Deg_pitch += 180;
                                        if (Deg_pitch < -180) {
                                            Deg_pitch += 360;
                                        } else if (Deg_pitch > 180) {
                                            Deg_pitch -= 360;
                                        }

                                    } else if (DataSaved.lrFrame == 3) {
                                        Deg_roll = Math.atan2(ay_norm, az_norm) * 180 / Math.PI;
                                        Deg_pitch = -(Math.atan2(ax_norm, az_norm) * 180 / Math.PI);
                                        Deg_roll += 180;
                                        if (Deg_roll < -180) {
                                            Deg_roll += 360;
                                        } else if (Deg_roll > 180) {
                                            Deg_roll -= 360;
                                        }
                                        Deg_pitch += 180;
                                        if (Deg_pitch < -180) {
                                            Deg_pitch += 360;
                                        } else if (Deg_pitch > 180) {
                                            Deg_pitch -= 360;
                                        }

                                    } else if (DataSaved.lrFrame == 4) {
                                        //left
                                        Deg_roll = Math.atan2(ax_norm, az_norm) * 180 / Math.PI;
                                        Deg_pitch = Math.atan2(ay_norm, az_norm) * 180 / Math.PI;
                                        Deg_roll += 180;
                                        if (Deg_roll < -180) {
                                            Deg_roll += 360;
                                        } else if (Deg_roll > 180) {
                                            Deg_roll -= 360;
                                        }
                                        Deg_pitch += 180;
                                        if (Deg_pitch < -180) {
                                            Deg_pitch += 360;
                                        } else if (Deg_pitch > 180) {
                                            Deg_pitch -= 360;
                                        }

                                    } else {
                                        Deg_pitch = 0d;
                                        Deg_roll = 0d;
                                    }


                                    break;

                                case 0x382:
                                    //boom1 CobID=2
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});

                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_x / norm;
                                    ay_norm = (double) acc_y / norm;
                                    az_norm = (double) acc_z / norm;
                                    if (DataSaved.lrBoom1 == 1) {
                                        Deg_boom1 = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                                    } else if (DataSaved.lrBoom1 == -1) {
                                        Deg_boom1 = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                                    } else {
                                        Deg_boom1 = 0d;
                                    }


                                    break;

                                case 0x387:
                                    //boom2 CobID=3
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});

                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_x / norm;
                                    ay_norm = (double) acc_y / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrBoom2) {
                                        case 1:
                                            Deg_boom2 = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                                            break;
                                        case -1:
                                            Deg_boom2 = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                                            break;
                                        default:
                                            Deg_boom2 = 0d;
                                            break;
                                    }


                                    break;

                                case 0x384:
                                    //stick CobID=4
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});

                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_x / norm;
                                    ay_norm = (double) acc_y / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrStick) {
                                        case 1:
                                            Deg_stick = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                                            Deg_Boom_Roll = Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI;

                                            break;
                                        case -1:
                                            Deg_stick = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                                            Deg_Boom_Roll = -Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI;
                                            break;
                                        default:
                                            Deg_stick = 0d;
                                            Deg_Boom_Roll = Deg_roll;
                                            break;
                                    }

                                    if (DataSaved.lrFrame == 0) {
                                        Deg_Boom_Roll = 0;
                                    }
                                    Deg_Boom_Roll=movingAverage_boomroll(Deg_Boom_Roll,10);
                                    break;

                                case 0x385:
                                    //bucket CobID=5
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});

                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_x / norm;
                                    ay_norm = (double) acc_y / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrBucket) {
                                        case 1:
                                            Deg_bucket = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                                            break;
                                        case -1:
                                            Deg_bucket = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                                            break;
                                        case 2:
                                            Deg_bucket = (Math.atan2(ax_norm, az_norm) * 180 / Math.PI);
                                            break;
                                        case 3:
                                            Deg_bucket = -((Math.atan2(ax_norm, az_norm) * 180 / Math.PI));
                                            break;
                                        default:
                                            Deg_bucket = 0d;
                                            break;
                                    }


                                    break;

                                case 0x386:
                                    //tilt CobID=6
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_x / norm;
                                    ay_norm = (double) acc_y / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrTilt) {
                                        case 1:
                                            Deg_Benna_W_Tilt = Math.atan2(ay_norm, az_norm) * 180 / Math.PI;
                                            Deg_tilt = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);

                                            break;
                                        case -1:
                                            Deg_Benna_W_Tilt = -(Math.atan2(ay_norm, az_norm) * 180 / Math.PI);
                                            Deg_tilt = Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI;

                                            break;
                                        default:
                                            Deg_Benna_W_Tilt = 0;
                                            Deg_tilt = 0;
                                            break;
                                    }


                                    break;

                                case 90181738:
                                    //0x560106A
                                    //Tilt MOBA
                                    isMobaTilt = true;
                                    mqW = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    mqX = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    mqY = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    mqZ = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[6], data[7]});
                                    qW = mqW / 23768.0d;
                                    qX = mqX / 23768.0d;
                                    qY = mqY / 23768.0d;
                                    qZ = mqZ / 23768.0d;
                                    qnorm = Math.sqrt(qW * qW + qX * qX + qY * qY + qZ * qZ);
                                    qW /= qnorm;
                                    qX /= qnorm;
                                    qY /= qnorm;
                                    qZ /= qnorm;
                                    eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                                    eulerAngles[2]=(eulerAngles[2]);
                                    switch (DataSaved.lrTilt) {
                                        case 1:
                                            //Left
                                            Deg_Benna_W_Tilt = eulerAngles[0];
                                            Deg_tilt = -eulerAngles[1];
                                            Deg_Yaw_Tilt = eulerAngles[2];

                                            break;
                                        case -1:
                                            //Right
                                            Deg_Benna_W_Tilt = -eulerAngles[0];
                                            Deg_tilt = eulerAngles[1];
                                            Deg_Yaw_Tilt = eulerAngles[2];

                                            break;
                                    }
                                    break;

                                case 928:
                                    //default sensor CobID=20

                                    flagDefault += 100;
                                    break;
                                case 204301://verificare id laser 29bit
                                    V_Laser = (int) data[2] & 0xFF;
                                    flagLaser += 100;
                                    break;

                                case 417:
                                    flagLaserConnected += 100;
                                    if (DataSaved.laserOn == 1) {

                                        V_Laser = (int) data[0] & 0xFF;
                                        if (data[3] == 0) {
                                            flagLaser += 100;
                                        } else {
                                            flagLaser -= 1;
                                        }
                                    } else {

                                        flagLaser -= 20;
                                    }
                                    break;


                            }
                            ExcavatorLib.Excavator();
                            break;
                        case 2:
                        case 3://TSM
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
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_y / norm;
                                    ay_norm = (double) acc_x / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrFrame) {
                                        case 0:
                                            Deg_pitch = 0d;
                                            Deg_roll = 0d;
                                            break;
                                        case 1:
                                            Deg_pitch = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                            Deg_roll = (Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                            break;
                                        case 2:
                                            //right
                                            Deg_roll = (Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                            Deg_pitch = (Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                            break;
                                        case 3:
                                            Deg_pitch = (Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                            Deg_roll = -(Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                            break;
                                        case 4:
                                            Deg_roll = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                            Deg_pitch = -(Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                            break;
                                    }

                                    break;

                                case 0x382:
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_y / norm;
                                    ay_norm = (double) acc_x / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrBoom1) {
                                        case 1:
                                            Deg_boom1 = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                                            break;
                                        case -1:
                                            Deg_boom1 = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                                            break;
                                        default:
                                            Deg_boom1 = 0d;
                                            break;
                                    }
                                    break;

                                case 0x387:
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});

                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_y / norm;
                                    ay_norm = (double) acc_x / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrBoom2) {
                                        case 1:
                                            Deg_boom2 = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                                            break;
                                        case -1:
                                            Deg_boom2 = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                                            break;
                                        default:
                                            Deg_boom2 = 0d;
                                            break;
                                    }

                                    break;

                                case 0x384:
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});

                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_y / norm;
                                    ay_norm = (double) acc_x / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrStick) {
                                        case 1:
                                            Deg_stick = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                                            Deg_Boom_Roll = (-Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI);
                                            break;
                                        case -1:
                                            Deg_stick = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                                            Deg_Boom_Roll = (Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI);
                                            break;
                                        default:
                                            Deg_stick = 0d;
                                            Deg_Boom_Roll = 0;
                                            break;
                                    }

                                    if (DataSaved.lrFrame == 0) {
                                        Deg_Boom_Roll = 0;
                                    }
                                    Deg_Boom_Roll=movingAverage_boomroll(Deg_Boom_Roll,10);
                                    break;

                                case 0x385:
                                    //bucket TSM
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_y / norm;
                                    ay_norm = (double) acc_x / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrBucket) {
                                        case 1:
                                            Deg_bucket = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                                            break;
                                        case -1:
                                            Deg_bucket = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                                            break;
                                        case 2:
                                            Deg_bucket = (Math.atan2(ax_norm, -az_norm) * 180 / Math.PI);
                                            break;
                                        case 3:
                                            Deg_bucket = -((Math.atan2(ax_norm, -az_norm) * 180 / Math.PI));
                                            break;
                                        default:
                                            Deg_bucket = 0d;
                                            break;
                                    }
                                    break;
                                case 0x383:
                                    flagDefault += 100;
                                    break;


                                case 90181738:
                                    //0x560106A
                                    //Tilt MOBA
                                    isMobaTilt = true;
                                    mqW = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    mqX = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    mqY = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    mqZ = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[6], data[7]});
                                    qW = mqW / 23768.0d;
                                    qX = mqX / 23768.0d;
                                    qY = mqY / 23768.0d;
                                    qZ = mqZ / 23768.0d;
                                    qnorm = Math.sqrt(qW * qW + qX * qX + qY * qY + qZ * qZ);
                                    qW /= qnorm;
                                    qX /= qnorm;
                                    qY /= qnorm;
                                    qZ /= qnorm;
                                    eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                                    eulerAngles[2]=(eulerAngles[2]);
                                    switch (DataSaved.lrTilt) {
                                        case 1:
                                            //Left
                                            Deg_Benna_W_Tilt = eulerAngles[0];
                                            Deg_tilt = -eulerAngles[1];
                                            Deg_Yaw_Tilt = eulerAngles[2];

                                            break;
                                        case -1:
                                            //Right
                                            Deg_Benna_W_Tilt = -eulerAngles[0];
                                            Deg_tilt = eulerAngles[1];
                                            Deg_Yaw_Tilt = eulerAngles[2];

                                            break;
                                    }
                                    break;
                                case 0x386:
                                    //tilt TSM
                                    isMobaTilt = false;
                                    acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                                    ax_norm = (double) acc_x / norm;
                                    ay_norm = (double) acc_y / norm;
                                    az_norm = (double) acc_z / norm;
                                    switch (DataSaved.lrTilt) {
                                        case 1:
                                            Deg_Benna_W_Tilt = Math.atan2(ay_norm, -az_norm) * 180 / Math.PI;
                                            Deg_tilt = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                            Deg_Yaw_Tilt = 0;
                                            break;
                                        case -1:
                                            Deg_Benna_W_Tilt = -(Math.atan2(ay_norm, -az_norm) * 180 / Math.PI);
                                            Deg_tilt = Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI;
                                            break;
                                        default:
                                            Deg_Benna_W_Tilt = 0;
                                            Deg_tilt = 0;
                                            Deg_Yaw_Tilt = 0;
                                            break;
                                    }

                                    break;
                                case 0x486:
                                    isMobaTilt = false;
                                    // Dati del giroscopio
                                    Gx = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    Gy = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    Gz = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    // Converti i dati grezzi del giroscopio in gradi/secondo o radianti/secondo
                                    double gyroScaleFactor = 1000.0 / 32768.0; // Scala in base al range del giroscopio
                                    double gz_dps = Gz * gyroScaleFactor; // Gz in gradi al secondo
                                    // Converti in radianti al secondo (opzionale, puoi anche lavorare con gradi)
                                    double gz_rad = gz_dps * (Math.PI / 180.0); // Gz in radianti al secondo
                                    // Calcola il delta di tempo (in secondi) dall'ultimo aggiornamento
                                    double currentTimestamp = System.currentTimeMillis();
                                    double deltaTime = (currentTimestamp - previousTimestamp) / 1000.0; // Secondi
                                    previousTimestamp = currentTimestamp;
                                    // Integra la velocità angolare sull'asse Z (Gz) per ottenere lo yaw
                                    yaw += gz_rad * deltaTime; // Aggiorna lo yaw integrando Gz nel tempo

                                    // Converti lo yaw da radianti a gradi per una lettura più familiare
                                    double yawDegrees = yaw * (180.0 / Math.PI);

                                    // Assicurati che lo yaw sia compreso tra -180° e 180°
                                    if (yawDegrees > 180) {
                                        yawDegrees -= 360;
                                    } else if (yawDegrees < -180) {
                                        yawDegrees += 360;
                                    }


                                    // Visualizza il valore dello yaw
                                    Deg_Yaw_Tilt = (yawDegrees);


                                    break;


                                case 204301://verificare id laser 29bit

                                    switch (DataSaved.laserOn) {
                                        case 1:
                                            V_Laser = (int) data[2] & 0xFF;
                                            flagLaser += 100;
                                            break;
                                        default:
                                            flagLaser -= 100;
                                            break;
                                    }
                                    break;
                                case 0x1A1:
                                    flagLaserConnected += 100;
                                    switch (DataSaved.laserOn) {
                                        case 1:

                                            V_Laser = (int) data[0] & 0xFF;
                                            if (data[3] == 0) {
                                                flagLaser += 100;
                                            } else {
                                                flagLaser -= 1;
                                            }
                                            break;
                                        default:

                                            flagLaser -= 20;
                                            break;
                                    }
                                    break;
                            }
                            ExcavatorLib.Excavator();
                            break;
                        case 5:
                            //demo ROLLER
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
                                case 0x195:

                                    boolean[] boo = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                                    latP = boo[6];
                                    latM = boo[7];
                                    break;
                                case 0x295:
                                    if (!isMobaTilt) {
                                        int mTilt = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[1], data[2]});

                                        Deg_tilt = mTilt * 1d;
                                    }
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
                                    rotL = ba[1];
                                    rotR = ba[5];
                                    break;

                                case 0x1F0:
                                    boolean[] be = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                                    qP = be[4];
                                    qM = be[5];
                                    lonP = be[6];
                                    lonM = be[7];

                                    break;
                                case 90181738:
                                    //0x560106A
                                    //Tilt MOBA
                                    isMobaTilt = true;
                                    mqW = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                                    mqX = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                                    mqY = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                                    mqZ = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[6], data[7]});
                                    qW = mqW / 23768.0d;
                                    qX = mqX / 23768.0d;
                                    qY = mqY / 23768.0d;
                                    qZ = mqZ / 23768.0d;
                                    qnorm = Math.sqrt(qW * qW + qX * qX + qY * qY + qZ * qZ);
                                    qW /= qnorm;
                                    qX /= qnorm;
                                    qY /= qnorm;
                                    qZ /= qnorm;
                                    eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                                    switch (DataSaved.lrTilt) {
                                        case 1:
                                            //Left
                                            Deg_Benna_W_Tilt = eulerAngles[0];
                                            Deg_tilt = -eulerAngles[1];
                                            Deg_Yaw_Tilt = eulerAngles[2];

                                            break;
                                        case -1:
                                            //Right
                                            Deg_Benna_W_Tilt = -eulerAngles[0];
                                            Deg_tilt = eulerAngles[1];
                                            Deg_Yaw_Tilt = eulerAngles[2];

                                            break;
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


                            gpsSimul(new boolean[]{rotL, rotR, latP, latM, lonP, lonM, qP, qM});
                            ExcavatorLib.Excavator();
                            break;
                    }


                    flagDefault--;
                    flagLaser -= 1;
                    flagLaserConnected--;
                    flagDefault = Math.max(-100, Math.min(flagDefault, 100));
                    flagLaser = Math.max(-101, Math.min(flagLaser, 100));
                    flagLaserConnected = Math.max(-100, Math.min(flagLaserConnected, 100));
                    break;
                case 2:
                case 3:
                case 4:
                    //Dozer e grader
                    if(DataSaved.my_comPort == 4){
                        switch (id) {


                            case 0x195:

                                boolean[] boo = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                                latP = boo[6];
                                latM = boo[7];
                                break;
                            case 0x295:
                                if (!isMobaTilt) {
                                    int mTilt = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[1], data[2]});

                                    Deg_tilt = mTilt * 1d;
                                }
                                break;


                            case 0x3F0:


                                boolean[] ba = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                                rotL = ba[1];
                                rotR = ba[5];
                                break;

                            case 0x1F0:
                                boolean[] be = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                                qP = be[4];
                                qM = be[5];
                                lonP = be[6];
                                lonM = be[7];

                                break;

                        }
                        gpsSimul(new boolean[]{rotL, rotR, latP, latM, lonP, lonM, qP, qM});
                    }
                    switch (id & 0x1FFFFFFF) {
                        case 0x386:
                        case 0x385:
                            acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                            acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                            acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                            norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                            ax_norm = (double) acc_y / norm;
                            ay_norm = (double) acc_x / norm;
                            az_norm = (double) acc_z / norm;
                            switch (DataSaved.lrBucket) {
                                case 0:
                                    Deg_pitch = 0d;
                                    Deg_roll = 0d;
                                    break;
                                case 1:
                                    Deg_pitch = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                    Deg_roll = (Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                    break;
                                case -1:
                                    Deg_pitch = (Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                    Deg_roll = -(Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                    break;
                                case 2://vert_up-DESTRA
                                    Deg_pitch = (Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                    Deg_roll = (Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);


                                    break;
                                case 3://vert_dw-SINISTRA
                                    Deg_pitch = -(Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                                    Deg_roll = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);

                                    break;
                                default:
                                    Deg_roll = 0d;
                                    Deg_pitch = 0d;
                                    break;
                            }
                            break;
                        //
                        case 90181738:
                        case 90181733:
                            mqW = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
                            mqX = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
                            mqY = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
                            mqZ = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[6], data[7]});
                            qW = mqW / 23768.0d;
                            qX = mqX / 23768.0d;
                            qY = mqY / 23768.0d;
                            qZ = mqZ / 23768.0d;
                            qnorm = Math.sqrt(qW * qW + qX * qX + qY * qY + qZ * qZ);
                            qW /= qnorm;
                            qX /= qnorm;
                            qY /= qnorm;
                            qZ /= qnorm;


                            switch (DataSaved.lrBucket) {
                                case 0:
                                    eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                                    Deg_pitch = 0;
                                    Deg_roll = 0;
                                    Deg_Yaw_Frame = 0;
                                    break;
                                case 1:
                                    //Avanti
                                    eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                                    Deg_pitch = -eulerAngles[1];
                                    Deg_roll = -eulerAngles[0];
                                    Deg_Yaw_Frame = eulerAngles[2];
                                    break;
                                case -1:
                                    //Dietro
                                    eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                                    Deg_pitch = eulerAngles[1];
                                    Deg_roll = eulerAngles[0];
                                    Deg_Yaw_Frame = eulerAngles[2];
                                    break;
                                case 2:
                                    //vert_up DESTRA
                                    eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                                    Deg_pitch = -eulerAngles[0];
                                    Deg_roll = eulerAngles[1];
                                    Deg_Yaw_Frame = eulerAngles[2];
                                    break;
                                case 3:
                                    //vert_dw-SINISTRA
                                    eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                                    Deg_pitch = eulerAngles[0];
                                    Deg_roll = -eulerAngles[1];
                                    Deg_Yaw_Frame = eulerAngles[2];
                                    break;
                                default:
                                    Deg_pitch = 0;
                                    Deg_roll = 0;
                                    Deg_Yaw_Frame = 0;
                                    break;
                            }
                            break;

                    }

                    ExcavatorLib.Excavator();
                    break;

            }

        } catch (Exception e) {
            flagLaser--;
            flagDefault--;
        }


    }

    private static void gpsSimul(boolean[] keyEvents) {


        if (keyEvents[2]) {
            // lat+
            NmeaGenerator.LATITUDE += 0.005;

        }
        if (keyEvents[3]) {
            // lat-
            NmeaGenerator.LATITUDE -= 0.005;

        }
        if (keyEvents[5]) {
            // lon+
            NmeaGenerator.LONGITUDE -= 0.005;

        }
        if (keyEvents[4]) {
            // F4
            NmeaGenerator.LONGITUDE += 0.005;

        }
        if (keyEvents[0]) {
            // rotLeft
            NmeaGenerator.HEADING -= 0.05;
            if (NmeaGenerator.HEADING >= 360) {
                NmeaGenerator.HEADING = 0;
            } else if (NmeaGenerator.HEADING <= 0) {
                NmeaGenerator.HEADING = 360;
            }

        }
        if (keyEvents[1]) {
            // rotRight
            NmeaGenerator.HEADING += 0.05;
            if (NmeaGenerator.HEADING >= 360) {
                NmeaGenerator.HEADING = 0;
            } else if (NmeaGenerator.HEADING <= 0) {
                NmeaGenerator.HEADING = 360;
            }

        }
        if (keyEvents[6]) {
            NmeaGenerator.ALTITUDE += 0.001;
        }
        if (keyEvents[7]) {
            NmeaGenerator.ALTITUDE -= 0.001;
        }
    }


    public static double[] quaternionToEuler(double w, double x, double y, double z) {

        double roll, pitch, yaw;
        // Roll (X-axis rotation)
        double sinr_cosp = 2 * (w * x + y * z);
        double cosr_cosp = 1 - 2 * (x * x + y * y);
        roll = Math.atan2(sinr_cosp, cosr_cosp);

        // Pitch (Y-axis rotation)
        double sinp = 2 * (w * y - z * x);
        if (Math.abs(sinp) >= 1)
            pitch = Math.copySign(Math.PI / 2, sinp); // Use 90 degrees if out of range
        else
            pitch = Math.asin(sinp);

        // Yaw (Z-axis rotation)
        double siny_cosp = 2 * (w * z + x * y);
        double cosy_cosp = 1 - 2 * (y * y + z * z);
        yaw = Math.atan2(siny_cosp, cosy_cosp);

        // Converti da radianti a gradi
        roll = Math.toDegrees(roll);
        pitch = Math.toDegrees(pitch);
        yaw = Math.toDegrees(yaw);
        if (yaw < 0) {
            yaw += 360;
        }

        return new double[]{roll, pitch, yaw};
    }

    public static double movingAverage_boomroll(double newVal, int WINDOW) {
        if (boomRollBuffer.size() >= WINDOW) {
            boomRollBuffer.poll(); // rimuove il più vecchio
        }
        boomRollBuffer.add(newVal);
        double sum = 0;
        for (double v : boomRollBuffer) sum += v;
        return sum / boomRollBuffer.size();
    }

    public static double movingAverage_tilt(double newVal, int WINDOW) {
        if (tiltBuffer.size() >= WINDOW) {
            tiltBuffer.poll(); // rimuove il più vecchio
        }
        tiltBuffer.add(newVal);
        double sum = 0;
        for (double v : tiltBuffer) sum += v;
        return sum / tiltBuffer.size();
    }
}
