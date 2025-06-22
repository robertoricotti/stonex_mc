package com.example.mylibrary.plc;

public class My_SensorsDecoder {

    static double grezzoPitch, grezzoRoll, grezzoBoom1, grezzoSwing, grezzoBoom2, grezzoStick, grezzoBoomRoll, grezzoBenna, grezzoBennaTilt, grezzoTilt;
    static int mPosFrame, mPosSwing, mPosBoom1, mPosBoom2, mPosStick, mPosLaser, mPosTilt, mPosBucket, mID;

    /**
     * flags
     * 0-mPosFrame
     * 1-mPosSwing
     * 2-mPosBoom1
     * 3-mPosBoom2
     * 4-mPosStick
     * 5-mPosLaser
     * 6-mPosTilt
     * 7-mPosBucket
     *
     *
     * return
     * 0-grezzoPitch
     * 1-grezzoRoll
     * 2-grezzoBoom1
     * 3-grezzoSwing
     * 4-grezzoBoom2
     * 5-grezzoStick
     * 6-grezzoBoomRoll
     * 7-grezzoBenna
     * 8-grezzoBennaTilt
     * 9-grezzoTilt
     */
    public My_SensorsDecoder() {

    }

    public static double[] rawDegTSM(int[] flags, int id, byte[] data) {
        try {
            mPosFrame = flags[0];
            mPosSwing = flags[1];
            mPosBoom1 = flags[2];
            mPosBoom2 = flags[3];
            mPosStick = flags[4];
            mPosLaser = flags[5];
            mPosTilt = flags[6];
            mPosBucket = flags[7];
            mID = id;
            switch (mID) {
                case 897:
                    grezzoPitch = My_TSM_AccToDeg.Frame_Pitch(mPosFrame, (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]}));
                    grezzoRoll = My_TSM_AccToDeg.Frame_Roll(mPosFrame, (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]}));
                    break;
                case 898:
                    grezzoBoom1 = My_TSM_AccToDeg.Boom_Pitch(mPosBoom1, (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]}));
                    break;
                case 903:
                    grezzoBoom2 = My_TSM_AccToDeg.Boom_Pitch(mPosBoom2, (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]}));
                    break;
                case 900:
                    grezzoStick = My_TSM_AccToDeg.Boom_Pitch(mPosStick, (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]}));
                    grezzoBoomRoll = My_TSM_AccToDeg.Boom_Roll(mPosStick, (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]}),
                            (short) My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]}));
                    break;
                case 901:
                    short acc_x = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]});
                    short acc_y = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]});
                    short acc_z = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]});

                    double norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                    double ax_norm = (double) acc_y / norm;
                    double ay_norm = (double) acc_x / norm;
                    double az_norm = (double) acc_z / norm;
                    if (mPosBucket == 1) {
                        grezzoBenna = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                    } else if (mPosBucket == -1) {
                        grezzoBenna = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                    } else if (mPosBucket == 2) {
                        grezzoBenna = (Math.atan2(ax_norm, -az_norm) * 180 / Math.PI);
                    } else {
                        grezzoBenna = 0d;
                    }
                    break;
                case 902:
                    short acc_xa = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]});
                    short acc_ya = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]});
                    short acc_za = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]});
                    double norma = Math.sqrt(acc_xa * acc_xa + acc_ya * acc_ya + acc_za * acc_za);
                    double ax_norma = (double) acc_xa / norma;
                    double ay_norma = (double) acc_ya / norma;
                    double az_norma = (double) acc_za / norma;
                    if (mPosTilt == 1) {
                        grezzoBennaTilt = Math.atan2(ay_norma, -az_norma) * 180 / Math.PI;
                        grezzoTilt = -(Math.atan2(ax_norma, Math.sqrt(ay_norma * ay_norma + az_norma * az_norma)) * 180.0 / Math.PI);
                    } else if (mPosTilt == -1) {
                        grezzoBennaTilt = -(Math.atan2(ay_norma, -az_norma) * 180 / Math.PI);
                        grezzoTilt = Math.atan2(ax_norma, Math.sqrt(ay_norma * ay_norma + az_norma * az_norma)) * 180.0 / Math.PI;
                    } else {
                        grezzoBennaTilt = 0;
                        grezzoTilt = 0;
                    }
                    break;

                case 416:

                    int swing = 0;
                    byte dt1, dt2;
                    if ((data[2] & 0xff) == 255) {
                        dt1 = data[2];
                        dt2 = (byte) 255;
                    } else {
                        dt1 = data[2];
                        dt2 = 0;
                    }
                    swing = My_PLC_Conv_LE.byte_to_S32(new byte[]{data[0], data[1], dt1, dt2});
                    swing = swing * -1;
                    swing = swing * mPosSwing;
                    //52919=1900mm scalatura conteggi su filo da 1900mm
                    int res = My_UnitsConv.scaleInt(swing, 0, 52919, 0, 1900);
                    grezzoSwing = res * 0.001;
                    break;
            }
            return new double[]{grezzoPitch, grezzoRoll, grezzoBoom1, grezzoSwing, grezzoBoom2, grezzoStick, grezzoBoomRoll, grezzoBenna, grezzoBennaTilt, grezzoTilt};
        } catch (Exception e) {
            return new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }
    }
    public static double[] rawDegMoba(int[] flags, int id, byte[] data) {
        try {
            mPosFrame = flags[0];
            mPosSwing = flags[1];
            mPosBoom1 = flags[2];
            mPosBoom2 = flags[3];
            mPosStick = flags[4];
            mPosLaser = flags[5];
            mPosTilt = flags[6];
            mPosBucket = flags[7];
            mID = id;
            short acc_x, acc_y, acc_z;
            double norm, ax_norm, ay_norm, az_norm;
            switch (mID) {
                case 897:
                    //frame CobID=1
                    acc_x = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]});
                    acc_y = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]});
                    acc_z = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]});
                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                    ax_norm = (double) acc_x / norm;
                    ay_norm = (double) acc_y / norm;
                    az_norm = (double) acc_z / norm;

                    if (mPosFrame == 1) {
                        grezzoRoll = -((Math.atan2(ay_norm, az_norm) * 180 / Math.PI));
                        grezzoPitch = (Math.atan2(ax_norm, az_norm) * 180 / Math.PI);
                        grezzoRoll += 180;
                        if (grezzoRoll < -180) {
                            grezzoRoll += 360;
                        } else if (grezzoRoll > 180) {
                            grezzoRoll -= 360;
                        }
                        grezzoPitch += 180;
                        if (grezzoPitch < -180) {
                            grezzoPitch += 360;
                        } else if (grezzoPitch > 180) {
                            grezzoPitch -= 360;
                        }

                    } else if (mPosFrame == 2) {
                        //right
                        grezzoRoll = -(Math.atan2(ax_norm, az_norm) * 180 / Math.PI);
                        grezzoPitch = -(Math.atan2(ay_norm, az_norm) * 180 / Math.PI);
                        grezzoRoll += 180;
                        if (grezzoRoll < -180) {
                            grezzoRoll += 360;
                        } else if (grezzoRoll > 180) {
                            grezzoRoll -= 360;
                        }
                        grezzoPitch += 180;
                        if (grezzoPitch < -180) {
                            grezzoPitch += 360;
                        } else if (grezzoPitch > 180) {
                            grezzoPitch -= 360;
                        }

                    } else if (mPosFrame == 3) {
                        grezzoRoll = Math.atan2(ay_norm, az_norm) * 180 / Math.PI;
                        grezzoPitch = -(Math.atan2(ax_norm, az_norm) * 180 / Math.PI);
                        grezzoRoll += 180;
                        if (grezzoRoll < -180) {
                            grezzoRoll += 360;
                        } else if (grezzoRoll > 180) {
                            grezzoRoll -= 360;
                        }
                        grezzoPitch += 180;
                        if (grezzoPitch < -180) {
                            grezzoPitch += 360;
                        } else if (grezzoPitch > 180) {
                            grezzoPitch -= 360;
                        }

                    } else if (mPosFrame == 4) {
                        //left
                        grezzoRoll = Math.atan2(ax_norm, az_norm) * 180 / Math.PI;
                        grezzoPitch = Math.atan2(ay_norm, az_norm) * 180 / Math.PI;
                        grezzoRoll += 180;
                        if (grezzoRoll < -180) {
                            grezzoRoll += 360;
                        } else if (grezzoRoll > 180) {
                            grezzoRoll -= 360;
                        }
                        grezzoPitch += 180;
                        if (grezzoPitch < -180) {
                            grezzoPitch += 360;
                        } else if (grezzoPitch > 180) {
                            grezzoPitch -= 360;
                        }

                    } else {
                        grezzoPitch = 0d;
                        grezzoRoll = 0d;
                    }
                    break;

                case 898:
                    //boom1 CobID=2


                    acc_x = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]});
                    acc_y = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]});
                    acc_z = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]});

                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                    ax_norm = (double) acc_x / norm;
                    ay_norm = (double) acc_y / norm;
                    az_norm = (double) acc_z / norm;
                    if (mPosBoom1 == 1) {
                        grezzoBoom1 = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                    } else if (mPosBoom1 == -1) {
                        grezzoBoom1 = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                    } else {
                        grezzoBoom1 = 0d;
                    }


                    break;

                case 899:


                    //boom2 CobID=3
                    acc_x = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]});
                    acc_y = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]});
                    acc_z = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]});

                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                    ax_norm = (double) acc_x / norm;
                    ay_norm = (double) acc_y / norm;
                    az_norm = (double) acc_z / norm;
                    if (mPosBoom2 == 1) {
                        grezzoBoom2 = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                    } else if (mPosBoom2 == -1) {
                        grezzoBoom2 = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                    } else {
                        grezzoBoom2 = 0d;
                    }


                    break;

                case 900:

                    //stick CobID=4
                    acc_x = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]});
                    acc_y = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]});
                    acc_z = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]});

                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                    ax_norm = (double) acc_x / norm;
                    ay_norm = (double) acc_y / norm;
                    az_norm = (double) acc_z / norm;
                    if (mPosStick == 1) {
                        grezzoStick = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                        grezzoBoomRoll = Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI;

                    } else if (mPosStick == -1) {
                        grezzoStick = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                        grezzoBoomRoll = -Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI;
                    } else {
                        grezzoStick = 0d;
                        grezzoBoomRoll = 0;
                    }
                    if (grezzoBoomRoll > 90) {
                        grezzoBoomRoll -= 180;
                    } else if (grezzoBoomRoll < -90) {
                        grezzoBoomRoll += 180;
                    }

                    break;

                case 901:


                    //bucket CobID=5
                    acc_x = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]});
                    acc_y = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]});
                    acc_z = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]});

                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                    ax_norm = (double) acc_x / norm;
                    ay_norm = (double) acc_y / norm;
                    az_norm = (double) acc_z / norm;
                    if (mPosBucket == 1) {
                        grezzoBenna = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                    } else if (mPosBucket == -1) {
                        grezzoBenna = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                    } else if (mPosBucket == 2) {
                        grezzoBenna = (Math.atan2(ax_norm, az_norm) * 180 / Math.PI);
                    } else {
                        grezzoBenna = 0d;
                    }


                    break;

                case 902:
                    //tilt CobID=6
                    acc_x = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[0], data[1]});
                    acc_y = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[2], data[3]});
                    acc_z = My_PLC_Conv_LE.byte_to_S16(new byte[]{data[4], data[5]});

                    norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
                    ax_norm = (double) acc_x / norm;
                    ay_norm = (double) acc_y / norm;
                    az_norm = (double) acc_z / norm;
                    if (mPosTilt == 1) {
                        grezzoBennaTilt = Math.atan2(ay_norm, az_norm) * 180 / Math.PI;
                        grezzoTilt = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);

                    } else if (mPosTilt == -1) {
                        grezzoBennaTilt = -(Math.atan2(ay_norm, az_norm) * 180 / Math.PI);
                        grezzoTilt = Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI;

                    } else {
                        grezzoBennaTilt = 0;
                        grezzoTilt = 0;
                    }
                    break;
                case 416:

                    int swing = 0;
                    byte dt1, dt2;
                    if ((data[2] & 0xff) == 255) {
                        dt1 = data[2];
                        dt2 = (byte) 255;
                    } else {
                        dt1 = data[2];
                        dt2 = 0;
                    }
                    swing = My_PLC_Conv_LE.byte_to_S32(new byte[]{data[0], data[1], dt1, dt2});
                    swing = swing * -1;
                    swing = swing * mPosSwing;
                    //52919=1900mm scalatura conteggi su filo da 1900mm
                    int res = My_UnitsConv.scaleInt(swing, 0, 52919, 0, 1900);
                    grezzoSwing = res * 0.001;
                    break;

            }
            return new double[]{grezzoPitch, grezzoRoll, grezzoBoom1, grezzoSwing, grezzoBoom2, grezzoStick, grezzoBoomRoll, grezzoBenna, grezzoBennaTilt, grezzoTilt};

        } catch (Exception e) {
            return new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }

    }
}
