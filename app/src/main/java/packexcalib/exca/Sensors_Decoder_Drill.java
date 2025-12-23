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



import android.util.Log;



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
    static long K=(long) Math.pow(2,32);


    public static void decode(int id, byte[] data) {
        try {
            if(id==0x18F||id==0x190){
                //TODO Encoder connesso 8192 count per revolution = 0x2000
                long revolution=PLC_DataTypes_LittleEndian.byte_to_U32(new byte[]{data[0],data[1],data[3],data[4]});
                if(DataSaved.lrRotary==-1){
                    K=(long) Math.pow(2,32);
                    EncRevolution=K-revolution;
                }else {
                    EncRevolution=revolution;
                }
                RopeLen=calculateRopeLength(EncRevolution,DataSaved.Rotary_Diam);

               // Log.w("Encoder Revolutions",String.valueOf(revolution));
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
    public static double calculateRopeLength(long encoderCounts, double pulleyDiameter) {
        final double COUNTS_PER_REV = 8192.0;

        double revolutions = encoderCounts / COUNTS_PER_REV;
        double circumference = Math.PI * pulleyDiameter;

        return revolutions * circumference;
    }

}
