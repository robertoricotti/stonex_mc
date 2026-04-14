package packexcalib.exca;


import static packexcalib.exca.TSM_Decoder.KELLER_BOOM1_ANGLE;
import static packexcalib.exca.TSM_Decoder.KELLER_BOOM2_ANGLE;
import static packexcalib.exca.TSM_Decoder.KELLER_STICK_ANGLE;
import static packexcalib.exca.TSM_Decoder.KELLER_TOOL_ANGLE;

public class TSM_Offsets {
    public static boolean dbAlertKELLER = false;
    static double correctStick, mcorrectStrick;

    public static double realBoom1(double offset) {
        double a;
        double d = KELLER_BOOM1_ANGLE;
        a = d - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;
        return a;

    }

    public static double realBoom2(double offset) {
        double a;
        double d = KELLER_BOOM2_ANGLE;
        a = d - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;
        return a;
    }

    public static double realStick(double offset) {
        double a;
        double d = KELLER_STICK_ANGLE;
        mcorrectStrick = d;


        if ((d - offset - 90.00) < -179.99) {
            a = (d - offset - 90.00) + 360.00;

        } else if ((d - offset - 90.00) > 179.99) {
            a = (d - offset - 90.00) - 360.00;

        } else {
            a = (d - offset - 90.00);

        }
        correctStick = a;
        return a;


    }

    public static double[] realBucket(double offset, double offsetFlat, double dboffset, double L1, double L2, double L3, double L4) {
        double[] a = new double[4];
        double correctBucket, correctFlat, DBStickAngle = 0;

        double L5;
        double Ya, Ba, nuovaBucket, bennaSimulata = 0;
        double angle = KELLER_TOOL_ANGLE;
        if (L1 > 0) {
            DBStickAngle = (((angle - correctStick) + dboffset) - 180.0) * -1;
            if (DBStickAngle < -180) {
                DBStickAngle += 360;
            }
            if (DBStickAngle > 180) {
                DBStickAngle -= 360;
            }
            double tempA = 180 - DBStickAngle;
            L5 = Math.sqrt(Math.pow(L3, 2) + Math.pow(L1, 2) - 2 * L3 * L1 * Math.cos(tempA * Math.PI / 180));
            Ya = (Math.acos(((((L3 * L3) + (L5 * L5)) - ((L1 * L1))) / (2 * L3 * L5))) * 180 / Math.PI);
            double method = ((Math.pow(L5, 2) + Math.pow(L4, 2) - Math.pow(L2, 2)) / (2 * L5 * L4));
            if (method > 1) {
                method = 1;
                dbAlertKELLER = true;
            } else {
                dbAlertKELLER = false;
            }
            Ba = Math.acos(method) * 180 / Math.PI;
            nuovaBucket = Ya + Ba;
            bennaSimulata = 180 + (mcorrectStrick) - nuovaBucket;

            if (bennaSimulata > 179.99) {
                bennaSimulata = (bennaSimulata) - 360.0;
            }
            if ((bennaSimulata - offset - 90.00) < -179.99) {
                correctBucket = (bennaSimulata - offset - 90.00) + 360.00;
            } else if ((bennaSimulata - offset - 90.00) > 179.99) {
                correctBucket = (bennaSimulata - offset - 90.00) - 360.00;
            } else {
                correctBucket = (bennaSimulata - offset - 90.00);
            }
        } else {
            if ((angle - offset - 90.00) < -179.99) {
                correctBucket = (angle - offset - 90.00) + 360.00;
            } else if ((angle - offset - 90.00) > 179.99) {
                correctBucket = (angle - offset - 90.00) - 360.00;
            } else {
                correctBucket = (angle - offset - 90.00);
            }
        }
        if ((correctBucket - offsetFlat) < -179.99) {
            correctFlat = (correctBucket - offsetFlat) + 360.00;
        } else if ((correctBucket - offsetFlat) > 179.99) {
            correctFlat = (correctBucket - offsetFlat) - 360.00;
        } else {
            correctFlat = (correctBucket - offsetFlat);
        }

        a[0] = correctBucket;
        a[1] = correctFlat;
        a[2] = DBStickAngle;
        a[3] = bennaSimulata;

        return a;

    }
}
