package packexcalib.exca;


import static packexcalib.exca.ExcavatorLib.correctBucket;
import static packexcalib.exca.Sensors_Decoder.Deg_Benna_W_Tilt;
import static packexcalib.exca.Sensors_Decoder.Deg_boom1;
import static packexcalib.exca.Sensors_Decoder.Deg_boom2;
import static packexcalib.exca.Sensors_Decoder.Deg_bucket;
import static packexcalib.exca.Sensors_Decoder.Deg_pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_roll;
import static packexcalib.exca.Sensors_Decoder.Deg_stick;
import static packexcalib.exca.Sensors_Decoder.Deg_tilt;
import static packexcalib.exca.Sensors_Decoder.V_Laser;

public class Excavator_RealValues {

    public static double correctStick, mcorrectStrick;
    public static boolean dbAlert = false;

    public static double realBoom1(double offset) {
        double a;
        double d = Deg_boom1;
        a = d - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;
        return a;

    }

    public static double realBoom2(double offset) {
        double a;
        double d = Deg_boom2;
        a = d - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;
        return a;
    }

    public static double realStick(double offset) {
        double a;
        double d = Deg_stick;
        mcorrectStrick = d;
        if(DataSaved.isWL==0) {

            if ((d - offset - 90.00) < -179.99) {
                a = (d - offset - 90.00) + 360.00;

            } else if ((d - offset - 90.00) > 179.99) {
                a = (d - offset - 90.00) - 360.00;

            } else {
                a = (d - offset - 90.00);

            }
            correctStick = a;
            return a;
        }else {
            a = d - offset;
            a = (a < -179.99) ? a + 360.00 : (a > 179.99) ? a - 360.00 : a;
            correctStick = a;
            return a;
        }


    }


    public static double realDeltaAngle(double offset) {
        double a;
        double d = correctBucket;
        if ((d - offset - 90.00) < -179.99) {
            a = (d - offset - 90.00) + 360.00;

        } else if ((d - offset - 90.00) > 179.99) {
            a = (d - offset - 90.00) - 360.00;

        } else {
            a = (d - offset - 90.00);
        }
        return a;
    }

    public static double realDegWTilt(double offset) {
        double a;
        double d = Deg_Benna_W_Tilt;
        if ((d - offset - 90.00) < -179.99) {
            a = (d - offset - 90.00) + 360.00;

        } else if ((d - offset - 90.00) > 179.99) {
            a = (d - offset - 90.00) - 360.00;

        } else {
            a = (d - offset - 90.00);
        }
        return a;
    }

    public static double realRoll(double offset) {
        double a;
        double d = Deg_roll;
        a = (d - offset);

        return a;
    }

    public static double realPitch(double offset) {
        double a;
        double d = Deg_pitch;
        a = (d - offset);

        return a;
    }

    public static double realEBubble_X(double offset) {
        double a;
        double d ;
        if(DataSaved.useTiltEbubble==0){
            d = Deg_tilt;
        }else{
            d=Deg_roll;
        }
        a = d - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;
        return a;
    }

    public static double realEBubble_Y(double offset) {
        double a;
        double d;
        if(DataSaved.useTiltEbubble==0){
            d = Deg_Benna_W_Tilt;
        }else{
            d=Deg_bucket;
        }
        a = d - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;
        return a;
    }


    public static double realTilt(double offset) {
        double a;

        double d = Deg_tilt;
        a = d - offset;
        a = (a < -179.99) ? a + 360.00 :
                (a > 179.99) ? a - 360.00 :
                        a;

        return a;

    }

    public static double[] realBucket(double offset, double offsetFlat, double dboffset, double L1, double L2, double L3, double L4) {
        double[] a = new double[4];
        double correctBucket, correctFlat, DBStickAngle = 0;

        double L5;
        double Ya, Ba, nuovaBucket, bennaSimulata = 0;
        double angle = Deg_bucket;
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
                dbAlert = true;
            } else {
                dbAlert = false;
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


    public static int realLaser() {
        int a = 255;

        switch (V_Laser & 0xff) {
            case 150:
                a = -70;
                break;
            case 140:
                a = -60;
                break;
            case 130:
                a = -50;
                break;
            case 120:
                a = -40;
                break;
            case 110:
                a = -30;
                break;
            case 100:
                a = -20;
                break;
            case 90:
                a = -10;
                break;
            case 80:
            case 75:
            case 85:
                a = 0;
                break;
            case 70:
                a = 10;
                break;
            case 60:
                a = 20;
                break;
            case 50:
                a = 30;
                break;
            case 40:
                a = 40;
                break;
            case 30:
                a = 50;
                break;
            case 20:
                a = 60;
                break;
            case 10:
                a = 70;
                break;
            case 0:
                a = 80;
                break;

        }
        return a;
    }
}
