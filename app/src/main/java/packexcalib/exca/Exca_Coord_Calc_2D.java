package packexcalib.exca;


public class Exca_Coord_Calc_2D {

    public static double[] calculateEndPoint(double[] startXYZ, double pitch, double roll, double length, double heading) {
/*
        double x0=startXYZ[0];
        double y0=startXYZ[1];
        double z0=startXYZ[2];
        double roll2=roll;
        roll=0;//annulla l'effetto del roll
        heading=-heading;
        heading=heading-addYaw(pitch,roll2);//applica il valore di yaw calcolato all'heading
        double pitchRad = Math.toRadians(pitch);
        double rollRad = Math.toRadians(roll);
        double headingRad = Math.toRadians(heading);
        double x = length * Math.sin(rollRad) * Math.sin(pitchRad);//double x = length * Math.sin(rollRad) * Math.cos(pitchRad);
        double y = length * Math.cos(rollRad) * Math.cos(pitchRad);
        double z = length * Math.sin(pitchRad) * Math.cos(rollRad);
        // Applica la rotazione di heading alle coordinate relative
        double rotatedX = x * Math.cos(headingRad) - y * Math.sin(headingRad);
        double rotatedY = x * Math.sin(headingRad) + y * Math.cos(headingRad);
        // Calcola le coordinate assolute di P1
        double x1 = x0 + rotatedX;
        double y1 = y0 + rotatedY;
        double z1 = z0 + z;

        return new double[]{x1, y1, z1};*/


        double x0 = startXYZ[0];
        double y0 = startXYZ[1];
        double z0 = startXYZ[2];

        heading = -heading;

        double pitchRad = Math.toRadians(pitch);
        double rollRad = Math.toRadians(roll);
        double headingRad = Math.toRadians(heading);


        double x = length * Math.sin(rollRad) * Math.sin(pitchRad);//double x = length * Math.sin(rollRad) * Math.cos(pitchRad);
        double y = length * Math.cos(rollRad) * Math.cos(pitchRad);
        double z = length * Math.sin(pitchRad) * Math.cos(rollRad);

        // Applica la rotazione di hdt
        double rotatedX = x * Math.cos(headingRad) - y * Math.sin(headingRad);
        double rotatedY = x * Math.sin(headingRad) + y * Math.cos(headingRad);

        // Calcola le coordinate assolute di P1
        double x1 = x0 + rotatedX;
        double y1 = y0 + rotatedY;
        double z1 = z0 + z;

        return new double[]{x1, y1, z1};
    }

    public static double addYaw(double p, double r) {

        double roll = Math.toRadians(r);
        double pitch = Math.toRadians(p);
        double result = Math.toDegrees(Math.atan2(Math.sin(roll) * Math.cos(pitch), Math.cos(roll) * Math.cos(pitch)));
        if (r < 0) {
            result = result * -1;
        }
        if (r > 0) {
            result = result * 1;
        }
        return result;


    }

}

