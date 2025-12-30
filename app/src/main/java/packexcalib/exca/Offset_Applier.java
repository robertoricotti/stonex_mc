package packexcalib.exca;


import static packexcalib.exca.ExcavatorLib.correctBucket;
import static packexcalib.exca.Sensors_Decoder.Deg_Benna_W_Tilt;
import static packexcalib.exca.Sensors_Decoder.Deg_Tool_Pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_Tool_Roll;
import static packexcalib.exca.Sensors_Decoder.Deg_boom1;
import static packexcalib.exca.Sensors_Decoder.Deg_boom2;
import static packexcalib.exca.Sensors_Decoder.Deg_bucket;
import static packexcalib.exca.Sensors_Decoder.Deg_pitch;
import static packexcalib.exca.Sensors_Decoder.Deg_roll;
import static packexcalib.exca.Sensors_Decoder.Deg_stick;
import static packexcalib.exca.Sensors_Decoder.Deg_tilt;
import static packexcalib.exca.Sensors_Decoder.V_Laser;
import static utils.MyTypes.EXCAVATOR;

public class Offset_Applier {

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
    public static double realMastLink(double offset) {
        double a;
        double d = Deg_stick;
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
        if(DataSaved.isWL==EXCAVATOR) {

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
    public static double real_Tool_Roll(double offset) {
        double a;
        double d = Deg_Tool_Roll;
        a = (d - offset);

        return a;
    }
    public static double real_Tool_Pitch(double offset) {
        double a;
        double d = Deg_Tool_Pitch;
        a = (d - offset);

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
    private static double normalizeAngle(double a) {
        a = a % 360.0;
        if (a > 180) a -= 360;
        if (a < -180) a += 360;
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
            DBStickAngle=normalizeAngle(DBStickAngle);
            double tempA = 180 - DBStickAngle;
            L5 = Math.sqrt(Math.pow(L3, 2) + Math.pow(L1, 2) - 2 * L3 * L1 * Math.cos(tempA * Math.PI / 180));
            Ya = (Math.acos(((((L3 * L3) + (L5 * L5)) - ((L1 * L1))) / (2 * L3 * L5))) * 180 / Math.PI);
            double method = ((Math.pow(L5, 2) + Math.pow(L4, 2) - Math.pow(L2, 2)) / (2 * L5 * L4));
            if (method > 1) {
                method = 1;
                dbAlert=true;
            }else {
                dbAlert=false;
            }
            if (method < -1) {
                method = -1;
                dbAlert=true;
            }else {
                dbAlert=false;
            }
            Ba = Math.acos(method) * 180 / Math.PI;
            nuovaBucket = Ya + Ba;
            bennaSimulata = 180 + (mcorrectStrick) - nuovaBucket;

            bennaSimulata=normalizeAngle(bennaSimulata);

            correctBucket = normalizeAngle(bennaSimulata - offset - 90.0);
        } else {
            if ((angle - offset - 90.00) < -179.99) {
                correctBucket = (angle - offset - 90.00) + 360.00;
            } else if ((angle - offset - 90.00) > 179.99) {
                correctBucket = (angle - offset - 90.00) - 360.00;
            } else {
                correctBucket = (angle - offset - 90.00);
            }
        }
        correctFlat   = normalizeAngle(correctBucket - offsetFlat);

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
            case 145:
            case 140:
                a = -60;
                break;
            case 135:
            case 130:
                a = -50;
                break;
            case 125:
            case 120:
                a = -40;
                break;
            case 115:
            case 110:
                a = -30;
                break;
            case 105:
            case 100:
                a = -20;
                break;
            case 95:
            case 90:
                a = -10;
                break;
            case 85:
            case 80:
            case 75:

                a = 0;
                break;
            case 70:
            case 65:
                a = 10;
                break;
            case 60:
            case 55:
                a = 20;
                break;
            case 50:
            case 45:
                a = 30;
                break;
            case 40:
            case 35:
                a = 40;
                break;
            case 30:
            case 25:
                a = 50;
                break;
            case 20:
            case 15:
                a = 60;
                break;
            case 10:
            case 5:
                a = 70;
                break;
            case 0:
                a = 80;
                break;

        }
        return a;
    }
}
