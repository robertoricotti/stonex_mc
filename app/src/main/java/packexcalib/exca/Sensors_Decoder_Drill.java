package packexcalib.exca;

import static gui.gps.NmeaGenerator.HEADING;
import static packexcalib.exca.Sensors_Decoder.Deg_Benna_W_Tilt;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static packexcalib.exca.Sensors_Decoder.Deg_Tool_Roll;
import static packexcalib.exca.Sensors_Decoder.Deg_Yaw_Tilt;
import static packexcalib.exca.Sensors_Decoder.Deg_boom1;
import static packexcalib.exca.Sensors_Decoder.Deg_boom2;
import static packexcalib.exca.Sensors_Decoder.Deg_bucket;
import static packexcalib.exca.Sensors_Decoder.Deg_bucket_DEMO;
import static packexcalib.exca.Sensors_Decoder.Deg_pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_roll;
import static packexcalib.exca.Sensors_Decoder.Deg_stick;
import static packexcalib.exca.Sensors_Decoder.Deg_tilt;
import static packexcalib.exca.Sensors_Decoder.ExtensionBoom;
import static packexcalib.exca.Sensors_Decoder.isMobaTilt;
import static packexcalib.exca.Sensors_Decoder.quaternionToEuler;
import static utils.MyTypes.MOBA_SENS;
import static utils.MyTypes.TSM_ACC;

import android.util.Log;

import gui.gps.NmeaGenerator;
import packexcalib.gnss.NmeaListener;

public class Sensors_Decoder_Drill {
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


    public static void decode(int id, byte[] data) {
        try {
            if(id==0x18F||id==0x190){
                //TODO Encoder connesso 8192 count per revolution = 0x2000
                long revolution=PLC_DataTypes_LittleEndian.byte_to_U32(new byte[]{data[0],data[1],data[3],data[4]});
            Log.w("Encoder Revolutions",String.valueOf(revolution));
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
                case MOBA_SENS:

                    switch (id & 0x1FFFFFFF) {
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

                        case 0x560106A:
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
                    DrillLib.Drill();
                    break;
                case TSM_ACC://TSM

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
                                    //LEFT
                                    Deg_bucket = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                                    Deg_Tool_Roll = (-Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI);
                                    break;
                                case -1:
                                    //RIGHT
                                    Deg_bucket = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                                    Deg_Tool_Roll = (Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI);
                                    break;
                                case 2:
                                    //TOP
                                    Deg_bucket = (Math.atan2(ax_norm, -az_norm) * 180 / Math.PI);
                                    Deg_Tool_Roll = -((Math.atan2(-ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180 / Math.PI));

                                    break;
                                case 3:
                                    //TOP REV
                                    Deg_bucket = -((Math.atan2(ax_norm, -az_norm) * 180 / Math.PI));
                                    Deg_Tool_Roll = (Math.atan2(-ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180 / Math.PI);

                                    break;
                                default:
                                    Deg_bucket = 0d;
                                    break;
                            }

                            // Log.d("TestToolRoll",DataSaved.lrBucket+"  "+String.format("%.2f",Deg_bucket)+"  "+String.format("%.2f",Deg_Tool_Roll));
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
                            eulerAngles[2] = (eulerAngles[2]);
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


                    }
                    DrillLib.Drill();
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
            if (DataSaved.isCanOpen == 5) {
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
                        Deg_Tool_Roll = mTilt * 1d;
                        break;


                    case 0x2F0:
                        //TODO Spare
                        break;
                    case 0x3F0:
                        boolean[] ba = PLC_DataTypes_LittleEndian.U8_to_bitmask(data[0]);
                        stickP = ba[7];
                        stickM = ba[3];

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


                ExcavatorLib.Excavator();
            }
            DrillLib.Drill();
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

}
