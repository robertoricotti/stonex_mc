package packexcalib.exca;


import android.util.Log;

public class TiltEncript {
    static double norm, ax_norm, ay_norm, az_norm;
    static double qW, qX, qY, qZ, qnorm, mqW, mqX, mqY, mqZ;

    static short acc_x;
    static short acc_y;
    static short acc_z;


    public static double[] encriptTSM_Frame(byte[] data, int mount) {
        double pitch = 0, roll = 0;
        acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
        acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
        acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
        norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
        ax_norm = (double) acc_y / norm;
        ay_norm = (double) acc_x / norm;
        az_norm = (double) acc_z / norm;
        switch (mount) {
            case 0:
                pitch = 0d;
                roll = 0d;
                break;
            case 1:
                pitch = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                roll = (Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                break;
            case 2:
                //right
                roll = (Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                pitch = (Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                break;
            case 3:
                pitch = (Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                roll = -(Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                break;
            case 4:
                roll = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                pitch = -(Math.atan2(ay_norm, Math.sqrt(ax_norm * ax_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                break;
        }
        return new double[]{pitch, roll};

    }

    public static double[] encriptTSM_Boom(byte[] data, int mount) {
        double pitch = 0, roll = 0;
        acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
        acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
        acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
        norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
        ax_norm = (double) acc_y / norm;
        ay_norm = (double) acc_x / norm;
        az_norm = (double) acc_z / norm;
        switch (mount) {
            case 1:
                pitch = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                roll=(-Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI);
                break;
            case -1:
                pitch = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                roll= (Math.atan2(-az_norm, Math.sqrt(ax_norm * ax_norm + ay_norm * ay_norm)) * 180 / Math.PI);
                break;
            default:
                pitch = 0d;
                roll=0d;
                break;
        }
        return new double[]{pitch,roll};
    }
    public static double[]encriptTSM_Bucket(byte[]data,int mount){
        double pitch=0,roll=0;
        acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
        acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
        acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
        norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
        ax_norm = (double) acc_y / norm;
        ay_norm = (double) acc_x / norm;
        az_norm = (double) acc_z / norm;
        switch (mount) {
            case 1:
                pitch = Math.atan2(ax_norm, ay_norm) * 180 / Math.PI;
                break;
            case -1:
                pitch = Math.atan2(ax_norm, -ay_norm) * 180 / Math.PI;
                break;
            case 2:
                pitch = (Math.atan2(ax_norm, -az_norm) * 180 / Math.PI);
                break;
            case 3:
                pitch = -((Math.atan2(ax_norm, -az_norm) * 180 / Math.PI));
                break;
            default:
                pitch = 0d;
                break;
        }
        return new double[]{pitch,roll};
    }
    public static double[] encriptTSM_Tilt(byte[]data,int mount){
        double pitch=0,roll=0,yaw=0;
        acc_x = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
        acc_y = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
        acc_z = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
        norm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
        ax_norm = (double) acc_x / norm;
        ay_norm = (double) acc_y / norm;
        az_norm = (double) acc_z / norm;
        switch (mount) {
            case 1:
                pitch = Math.atan2(ay_norm, -az_norm) * 180 / Math.PI;
                roll = -(Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI);
                yaw = 0;
                break;
            case -1:
                pitch = -(Math.atan2(ay_norm, -az_norm) * 180 / Math.PI);
                roll = Math.atan2(ax_norm, Math.sqrt(ay_norm * ay_norm + az_norm * az_norm)) * 180.0 / Math.PI;
                yaw=0;
                break;
            default:
                pitch = 0;
                roll = 0;
                yaw = 0;
                break;
        }
        return new double[]{pitch,roll,yaw};
    }

    public static double[] encriptNOVATRON_Tilt(byte[]data,int mount) {
         double[] eulerAngles;
         double pitch=0,roll=0,yaw=0;
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
        switch (mount) {
            case 1:
                //Left
                pitch = eulerAngles[0];
                roll = -eulerAngles[1];
                yaw = eulerAngles[2];

                break;
            case -1:
                //Right
                pitch = -eulerAngles[0];
                roll = eulerAngles[1];
                yaw = eulerAngles[2];

                break;
        }
        return new double[]{pitch,roll,yaw};
    }

    public static double[] encriptNOVATRON_Frame(byte[]data,int mount) {
        double[] eulerAngles;
        double pitch=0,roll=0,yaw=0;
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


        switch (mount) {
            case 0:
                eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                pitch = 0;
                roll = 0;
                yaw = 0;
                break;
            case 1:
                //Avanti
                eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                pitch = -eulerAngles[1];
                roll = -eulerAngles[0];
                yaw = eulerAngles[2];
                break;
            case -1:
                //Dietro
                eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                pitch = eulerAngles[1];
                roll = eulerAngles[0];
                yaw = eulerAngles[2];
                break;
            case 2:
                //vert_up DESTRA
                eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                pitch = -eulerAngles[0];
                roll = eulerAngles[1];
                yaw = eulerAngles[2];
                break;
            case 3:
                //vert_dw-SINISTRA
                eulerAngles = quaternionToEuler(qW, qX, qY, qZ);
                pitch = eulerAngles[0];
                roll = -eulerAngles[1];
                yaw = eulerAngles[2];
                break;
            default:
                pitch = 0;
                roll = 0;
                yaw = 0;
                break;
        }



        return new double[]{pitch,roll,yaw};
    }

    public static double[] encriptFMI_Quaternion(byte [] data, int mount){
        double pitch=0,roll=0,yaw=0;

        mqW = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
        mqX = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
        mqY = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
        mqZ = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[6], data[7]});
        qW = mqW / 32000.0d;
        qX = mqX / 32000.0d;
        qY = mqY / 32000.0d;
        qZ = mqZ / 32000.0d;
        //Log.d("FMI_Out_RAW",String.format("%.6f",qW)+"   "+String.format("%.6f",qX)+"   "+String.format("%.6f",qY)+"   "+String.format("%.6f",qZ));
        qnorm = Math.sqrt(qW * qW + qX * qX + qY * qY + qZ * qZ);
        qW /= qnorm;
        qX /= qnorm;
        qY /= qnorm;
        qZ /= qnorm;
       double[]eulerAngles=quaternionToEuler_YXZ(qW, qX, qY, qZ);
        Log.d("FMI_Out","Roll:"+String.format("%.2f",eulerAngles[0])+"   Pitch:"+String.format("%.2f",eulerAngles[1])+"   Yaw:"+String.format("%.2f",eulerAngles[2]));

        return  quaternionToEuler_YXZ(qW, qX, qY, qZ);
    }







private static double[] quaternionToEuler(double w, double x, double y, double z) {

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
    private static double[] quaternionToEuler_YXZ(double w, double x, double y, double z) {
        double roll, pitch, yaw;

        double sinp = 2 * (w * x - y * z);
        double cosp = 1 - 2 * (x * x + z * z);
        pitch = Math.atan2(sinp, cosp);

        double sinr = 2 * (w * y + x * z);
        double cosr = 1 - 2 * (y * y + z * z);
        roll = Math.atan2(sinr, cosr);

        double siny = 2 * (w * z - x * y);
        double cosy = 1 - 2 * (z * z + x * x);
        yaw = Math.atan2(siny, cosy);

        roll = Math.toDegrees(roll);
        pitch = Math.toDegrees(pitch);
        yaw = Math.toDegrees(yaw);
        if (yaw < 0) yaw += 360.0;
        if (roll < 0) roll += 360.0;

        return new double[]{roll, pitch, yaw};
    }
}
