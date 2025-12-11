package packexcalib.exca;


import static packexcalib.exca.Sensors_Decoder.quaternionToEuler;

import android.util.Log;

public class FMI_Decoder {
    private static double qW, qX, qY, qZ, qnorm, mqW, mqX, mqY, mqZ;
    private static double[] eulerAngles;

    public static double[] decodeTILT(byte[] data, int lr) {
        mqW = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[0], data[1]});
        mqX = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[2], data[3]});
        mqY = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[4], data[5]});
        mqZ = PLC_DataTypes_LittleEndian.byte_to_S16(new byte[]{data[6], data[7]});
        qW = mqW / 32000.0d;
        qX = mqX / 32000.0d;
        qY = mqY / 32000.0d;
        qZ = mqZ / 32000.0d;
        qnorm = Math.sqrt(qW * qW + qX * qX + qY * qY + qZ * qZ);
        qW /= qnorm;
        qX /= qnorm;
        qY /= qnorm;
        qZ /= qnorm;
        eulerAngles = quaternionToEuler(qW, qX, qY, qZ);

        double[] out = new double[3];
        switch (lr) {
            case 1:
                //left
                out = new double[]{-eulerAngles[1], -eulerAngles[0], eulerAngles[2]};
                break;

            case -1:
                //right
                out = new double[]{eulerAngles[1], eulerAngles[0], eulerAngles[2]};
                break;

        }

        Log.w("FMI_Out", "Roll:" + String.format("%.2f", out[0]) + "   Pitch:" + String.format("%.2f", out[1]) + "   Yaw:" + String.format("%.2f", out[2]));
        return out;
    }

    private static double[] quaternionToEulerFMI(double w, double x, double y, double z) {
        double yaw, pitch, roll;

        // Z-Y-X rotation order (Feyman/MCS10)
        double sinr_cosp = 2.0 * (w * x + y * z);
        double cosr_cosp = 1.0 - 2.0 * (x * x + y * y);
        roll = Math.atan2(sinr_cosp, cosr_cosp);

        double sinp = 2.0 * (w * y - z * x);
        if (Math.abs(sinp) >= 1)
            pitch = Math.copySign(Math.PI / 2, sinp);
        else
            pitch = Math.asin(sinp);

        double siny_cosp = 2.0 * (w * z + x * y);
        double cosy_cosp = 1.0 - 2.0 * (y * y + z * z);
        yaw = Math.atan2(siny_cosp, cosy_cosp);

        roll = Math.toDegrees(roll);
        pitch = Math.toDegrees(pitch);
        yaw = Math.toDegrees(yaw);
        if (yaw < 0) yaw += 360.0;
        if (roll < 0) roll += 360.0;

        return new double[]{roll, pitch, yaw};
    }

    private static double[] multiplyQuaternion(double[] q1, double[] q2) {
        double w1 = q1[0], x1 = q1[1], y1 = q1[2], z1 = q1[3];
        double w2 = q2[0], x2 = q2[1], y2 = q2[2], z2 = q2[3];

        double w = w1 * w2 - x1 * x2 - y1 * y2 - z1 * z2;
        double x = w1 * x2 + x1 * w2 + y1 * z2 - z1 * y2;
        double y = w1 * y2 - x1 * z2 + y1 * w2 + z1 * x2;
        double z = w1 * z2 + x1 * y2 - y1 * x2 + z1 * w2;

        return new double[]{w, x, y, z};
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
