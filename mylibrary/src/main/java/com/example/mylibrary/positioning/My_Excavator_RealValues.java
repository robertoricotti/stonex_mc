package com.example.mylibrary.positioning;


public class My_Excavator_RealValues {


    public static boolean dbAlert = false;

    public static double realBoom1(double angle, double offset) {
        double a;
        a = angle - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;
        return a;

    }

    public static double realBoom2(double angle, double offset) {
        double a;
        a = angle - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;
        return a;
    }

    public static double realStick(double angle, double offset) {
        double a;


        if ((angle - offset - 90.00) < -179.99) {
            a = (angle - offset - 90.00) + 360.00;

        } else if ((angle - offset - 90.00) > 179.99) {
            a = (angle - offset - 90.00) - 360.00;

        } else {
            a = (angle - offset - 90.00);

        }


        return a;
    }

    public static double realDeltaAngle(double angle, double offset) {
        double a;
        if ((angle - offset - 90.00) < -179.99) {
            a = (angle - offset - 90.00) + 360.00;

        } else if ((angle - offset - 90.00) > 179.99) {
            a = (angle - offset - 90.00) - 360.00;

        } else {
            a = (angle - offset - 90.00);
        }
        return a;
    }

    public static double realDegWTilt(double angle, double offset) {
        double a;
        if ((angle - offset - 90.00) < -179.99) {
            a = (angle - offset - 90.00) + 360.00;

        } else if ((angle - offset - 90.00) > 179.99) {
            a = (angle - offset - 90.00) - 360.00;

        } else {
            a = (angle - offset - 90.00);
        }
        return a;
    }

    public static double realRoll(double angle, double offset) {
        double a;
        a = (angle - offset);

        return a;
    }

    public static double realPitch(double angle, double offset) {
        double a;
        a = (angle - offset);

        return a;
    }


    public static double realTilt(double angle, double offset) {
        double a;

        a = angle - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;

        return a;

    }

    public static double[] realBucket(double Deg_bucket, double Deg_stick, double offset, double offsetFlat, double dboffset, double L1, double L2, double L3, double L4) {
        double[] a = new double[5];
        double error = 0;
        double correctBucket, correctFlat, DBStickAngle = 0;

        double L5;
        double Ya, Ba, nuovaBucket, bennaSimulata = 0;
        double angle = Deg_bucket;
        if (L1 > 0) {
            DBStickAngle = (((angle - Deg_stick) + dboffset) - 180.0) * -1;
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
                error = 1;
            } else {
                error = 0;
            }
            Ba = Math.acos(method) * 180 / Math.PI;
            nuovaBucket = Ya + Ba;
            bennaSimulata = 180 + (Deg_stick) - nuovaBucket;

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
        a[4] = error;

        return a;

    }


}
