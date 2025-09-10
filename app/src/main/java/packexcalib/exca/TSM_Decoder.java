package packexcalib.exca;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;


public class TSM_Decoder {

    static short acc_x;
    static short acc_y;
    static short acc_z;
    static double norm, ax_norm, ay_norm, az_norm;
    public static double  KELLER_ROLL_ANGLE,KELLER_BOOM1_ANGLE,KELLER_BOOM2_ANGLE,KELLER_STICK_ANGLE,KELLER_TOOL_ANGLE;

    private static final int WINDOW = 10;//campionamento per media mobile
    private static final Queue<Double> boomRollBuffer = new LinkedList<>();


public static void readSensors(int id, byte[] data,int[] position){
    /**
     * position [0]=montaggio boom1  -1=LEFT 0=OFF 1=RIGHT
     * position [1]=montaggio boom2   -1=LEFT 0=OFF 1=RIGHT
     * position [2]=montaggio stick   -1=LEFT 0=OFF 1=RIGHT
     * position [3]=montaggio bucket  1=LEFT  -1=RIGHT 2=TOP FWD   3=TOP BWD  0=0FF
     */


    switch (id&0x7FF){
        case 0x382:
            acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
            acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
            acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
            norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
            ax_norm = (double) acc_y / norm;
            ay_norm = (double) acc_x / norm;
            az_norm = (double) acc_z / norm;
            switch (position[0]) {
                case 1://RIGHT
                    KELLER_BOOM1_ANGLE = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                    break;
                case -1://LEFT
                    KELLER_BOOM1_ANGLE = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                    break;
                case 0://off
                    KELLER_BOOM1_ANGLE=0;
                    break;
                default:
                    KELLER_BOOM1_ANGLE = 0d;
                    break;
            }
            Log.d("BOOM1","Boom1: "+String.format("%.2f",KELLER_BOOM1_ANGLE));
            break;

        case 0x387:
            acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
            acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
            acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});

            norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
            ax_norm = (double) acc_y / norm;
            ay_norm = (double) acc_x / norm;
            az_norm = (double) acc_z / norm;
            switch (position[1]) {
                case 1:
                    KELLER_BOOM2_ANGLE = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                    break;
                case -1:
                    KELLER_BOOM2_ANGLE = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                    break;
                case 0:
                    KELLER_BOOM2_ANGLE=0;
                    break;
                default:
                    KELLER_BOOM2_ANGLE = 0d;
                    break;
            }

            Log.d("BOOM2","Boom2: "+String.format("%.2f",KELLER_BOOM2_ANGLE));
            break;

        case 0x384:
            acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
            acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
            acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});

            norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
            ax_norm = (double) acc_y / norm;
            ay_norm = (double) acc_x / norm;
            az_norm = (double) acc_z / norm;
            switch (position[2]) {
                case 1:
                    KELLER_STICK_ANGLE = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                    KELLER_ROLL_ANGLE =movingAverage( -Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI);
                    break;
                case -1:
                    KELLER_STICK_ANGLE = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                    KELLER_ROLL_ANGLE =movingAverage( Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI);
                    break;

                case 0:
                    KELLER_STICK_ANGLE=0;
                    KELLER_ROLL_ANGLE=0;
                    break;
                default:
                    KELLER_STICK_ANGLE = 0d;
                    KELLER_ROLL_ANGLE = 0;
                    break;
            }

            Log.d("STICK","Stick: "+String.format("%.2f",KELLER_STICK_ANGLE)+"     Roll: "+String.format("%.2f",KELLER_ROLL_ANGLE));
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
            switch (position[3]) {
                case 1://LEFT
                    KELLER_TOOL_ANGLE = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                    break;
                case -1://RIGHT
                    KELLER_TOOL_ANGLE = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                    break;
                case 2://TOP FORWARD
                    KELLER_TOOL_ANGLE = (Math.atan2(ax_norm, -az_norm) * 180 / Math.PI);
                    break;
                case 3://TOP REVERSE
                    KELLER_TOOL_ANGLE = -((Math.atan2(ax_norm, -az_norm) * 180 / Math.PI));
                    break;
                case 0://OFF
                    KELLER_TOOL_ANGLE=0;
                    break;
                default:
                    KELLER_TOOL_ANGLE = 0d;
                    break;
            }
            Log.d("TOOL","Tool: "+String.format("%.2f",KELLER_TOOL_ANGLE));
            break;

    }
}


    private static double movingAverage(double newVal) {
        if (boomRollBuffer.size() >= WINDOW) {
            boomRollBuffer.poll(); // rimuove il più vecchio
        }
        boomRollBuffer.add(newVal);
        double sum = 0;
        for (double v : boomRollBuffer) sum += v;
        return sum / boomRollBuffer.size();
    }
}
