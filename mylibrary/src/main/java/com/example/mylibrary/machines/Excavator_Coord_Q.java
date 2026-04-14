package com.example.mylibrary.machines;

import com.example.mylibrary.positioning.My_Exca_Quaternion;
import com.example.mylibrary.positioning.My_Excavator_RealValues;
import com.example.mylibrary.positioning.My_RodLocation;

public class Excavator_Coord_Q {
    /**
     * @param startXYZ
     * @param flags
     * @param deltaLen
     * @param angles
     * flags
     * 0-frame flag
     * 1-swing flag
     * 2-boom1 flag
     * 3-boom2 flag
     * 4-stick flag
     * 5-laser flag
     * 6-tilt flag
     * 7-bucket flag
     * <p>
     * deltaLen
     * 0-deltaZ
     * 1-deltaX
     * 2-deltaY
     * 3-deltaRoll
     * 4-deltaMiniPitch
     * 5-deltaCyl //dinamica
     * 6-deltaLA
     * 7-deltaLB
     * 8-deltaLC
     * 9-deltaPitch
     * 10-deltaBoom1
     * 11-deltaBoom2
     * 12-deltaStick
     * 13-deltaLSV
     * 14-deltaLSH
     * 15-deltaL1
     * 16-deltaL2
     * 17-deltaL3
     * 18-deltaL4
     * 19-deltaLPivot
     * 20-deltaBucket
     * 21-deltaBWidth
     * 22-deltaPiccolaBucket
     *
     * <p>
     * angles
     * 0-pitch
     * 1-roll
     * 2-swing
     * 3-boom1
     * 4-boom2
     * 5-stick
     * 6-boomRoll
     * 7-deltaangle
     * 8-bucketAngle
     * 9-bucketWTiltAngle
     * 10-TiltAngle
     * <p>
     * * offsets
     * * 0-offset pitch
     * * 1-offset roll
     * * 2-offset swing
     * * 3-offset boom1
     * * 4-offset boom2
     * * 5-offset stick
     * * 6-offset bucket
     * * 7-offset flat
     * * 8-offset DB
     * * 9-offset DegWTilt
     * * 10-offsteTilt
     */

    double swingBoomAngle,boomRoll, degPitch, degRoll, degBoom1, degBoom2, degStick, degBucket, deltaAngle, DBStickAngle, simulBucket,degBennaWtilt,degTilt;
    boolean errorDBCal;

    double[] coordDZ, coordDX, coordDY, coordRoll, coordMiniPitch, coordPitch, coordB1, coordB2, coordStick, coordPivot, coordBukt, coordBuckL, coordBuckR,
            coordLSV, coordLSH;


    public Excavator_Coord_Q(double[] startXYZ, int[] flags, double hdt, double[] deltaLen, double[] angles, double[] offsets) {
        degPitch = My_Excavator_RealValues.realBoom2(angles[0], offsets[0]);
        degRoll = My_Excavator_RealValues.realRoll(angles[1], offsets[1]);
        degBoom1 = My_Excavator_RealValues.realBoom1(angles[3], offsets[3]);
        degBoom2 = My_Excavator_RealValues.realBoom2(angles[4], offsets[4]);
        degStick = My_Excavator_RealValues.realStick(angles[5], offsets[5]);
        degBucket = My_Excavator_RealValues.realBucket(angles[8], degStick, offsets[6], offsets[7], offsets[8], deltaLen[15], deltaLen[16], deltaLen[17], deltaLen[18])[0];
        deltaAngle = My_Excavator_RealValues.realBucket(angles[8], degStick, offsets[6], offsets[7], offsets[8], deltaLen[15], deltaLen[16], deltaLen[17], deltaLen[18])[1];
        DBStickAngle = My_Excavator_RealValues.realBucket(angles[8], degStick, offsets[6], offsets[7], offsets[8], deltaLen[15], deltaLen[16], deltaLen[17], deltaLen[18])[2];
        simulBucket = My_Excavator_RealValues.realBucket(angles[8], degStick, offsets[6], offsets[7], offsets[8], deltaLen[15], deltaLen[16], deltaLen[17], deltaLen[18])[3];
        double e=My_Excavator_RealValues.realBucket(angles[8], degStick, offsets[6], offsets[7], offsets[8], deltaLen[15], deltaLen[16], deltaLen[17], deltaLen[18])[4];
        errorDBCal= e != 0;
        degBennaWtilt=My_Excavator_RealValues.realDegWTilt(angles[9],offsets[9]);
        degTilt=My_Excavator_RealValues.realTilt(angles[10],offsets[10]);
        boomRoll=angles[6];


        double hdtR = hdt + 90;
        if (hdtR > 360) {
            hdtR -= 360;
        } else if (hdtR < 0) {
            hdtR += 360;
        }
        double hdtL = hdt - 90;
        if (hdtL > 360) {
            hdtL -= 360;
        } else if (hdtL < 0) {
            hdtL += 360;
        }
        double hdtReverse = hdt + 180;
        if (hdtReverse > 360) {
            hdtReverse -= 360;
        } else if (hdtReverse < 0) {
            hdtReverse += 360;
        }

        if (flags[1] != 0) {
            swingBoomAngle = swingCalc(deltaLen[5], deltaLen[7], deltaLen[8]) - swingCalc(deltaLen[6], deltaLen[7], deltaLen[8]);
            if (Double.isNaN(swingBoomAngle)) {
                swingBoomAngle = 0;
            }
        } else {
            swingBoomAngle = 0;
        }
        double hdt_BOOM = hdt + swingBoomAngle;
        if (hdt_BOOM > 360) {
            hdt_BOOM -= 360;
        } else if (hdt_BOOM < 0) {
            hdt_BOOM += 360;
        }

        coordDZ = My_Exca_Quaternion.endPoint(startXYZ, degPitch - 90.0d, degRoll, deltaLen[0], hdt);
        if (deltaLen[1] < 0) {
            coordDX = My_Exca_Quaternion.endPoint(coordDZ, degRoll, -degPitch, deltaLen[1], hdtL);
        } else {
            coordDX = My_Exca_Quaternion.endPoint(coordDZ, -degRoll, degPitch, deltaLen[1], hdtR);
        }
        if (deltaLen[2] < 0) {
            coordDY = My_Exca_Quaternion.endPoint(coordDX, -degPitch, -degRoll, Math.abs(deltaLen[2]), hdtReverse);
        } else {
            coordDY = My_Exca_Quaternion.endPoint(coordDX, degPitch, degRoll, deltaLen[2], hdt);
        }//qui siamo al centro macchina
        if (flags[1] != 0) {
            coordMiniPitch = My_Exca_Quaternion.endPoint(coordDY, degPitch, degRoll, deltaLen[9] - deltaLen[4], hdt);
            coordPitch = My_Exca_Quaternion.endPoint(coordMiniPitch, degPitch, boomRoll, deltaLen[4], hdt_BOOM);
        } else {
            coordPitch = My_Exca_Quaternion.endPoint(coordRoll, degPitch,boomRoll, deltaLen[9], hdt_BOOM);
        }
        coordB1 = My_Exca_Quaternion.endPoint(coordPitch, degBoom1, boomRoll, deltaLen[10], hdt_BOOM);
        if (flags[3] != 0) {
            coordB2 = My_Exca_Quaternion.endPoint(coordB1, degBoom2, boomRoll, deltaLen[11], hdt_BOOM);
        } else {
            coordB2 = coordB1;
        }

        coordLSV = My_Exca_Quaternion.endPoint(coordB2, degStick, boomRoll, deltaLen[13], hdt_BOOM);
        coordLSH = My_Exca_Quaternion.endPoint(coordLSV, degStick + (90 * Double.compare(deltaLen[14], 0)), boomRoll, deltaLen[14], hdt_BOOM);
        coordStick = My_Exca_Quaternion.endPoint(coordB2, degStick, boomRoll, deltaLen[12], hdt_BOOM);
        if (flags[6] == 0) {
            coordPivot = new double[]{0, 0, 0};
            coordBukt = My_Exca_Quaternion.endPoint(coordStick, degBucket, boomRoll, deltaLen[20], hdt_BOOM);
            coordBuckL = My_RodLocation.rodloc(coordBukt, -deltaLen[21] * 0.5d, -hdt_BOOM, degBucket + 90, boomRoll + 90);
            coordBuckR = My_RodLocation.rodloc(coordBukt, -deltaLen[21] * 0.5d, -hdt_BOOM, degBucket + 90, boomRoll - 90);
        } else {
            coordPivot = My_Exca_Quaternion.endPoint(coordStick, deltaAngle, boomRoll, deltaLen[19], hdt_BOOM);
            coordBukt = My_Exca_Quaternion.endPoint(coordPivot, degBennaWtilt, degTilt, deltaLen[22], hdt_BOOM);
            coordBuckL = My_RodLocation.rodloc(coordBukt, -deltaLen[21] * 0.5d, -hdt_BOOM, degBennaWtilt+ 90, degTilt + 90);
            coordBuckR = My_RodLocation.rodloc(coordBukt, -deltaLen[21] * 0.5d, -hdt_BOOM, degBennaWtilt + 90, degTilt - 90);
        }
    }


    private double swingCalc(double a, double b, double c) {
        // Calcola gli angoli usando il teorema del coseno
        double tmp0 = (a * a) + (b * b) - (c * c);
        return Math.toDegrees(Math.acos(tmp0 / (2 * a * b)));
    }

    public boolean isErrorDBCal() {
        return errorDBCal;
    }

    public double getBoomRoll() {
        return boomRoll;
    }

    public double getSwingBoomAngle() {
        return swingBoomAngle;
    }

    public double getDBStickAngle() {
        return DBStickAngle;
    }

    public double getDegBennaWtilt() {
        return degBennaWtilt;
    }

    public double getDegBoom1() {
        return degBoom1;
    }

    public double getDegBoom2() {
        return degBoom2;
    }

    public double getDegBucket() {
        return degBucket;
    }

    public double getDegPitch() {
        return degPitch;
    }

    public double getDegRoll() {
        return degRoll;
    }

    public double getDegStick() {
        return degStick;
    }

    public double getDegTilt() {
        return degTilt;
    }

    public double getDeltaAngle() {
        return deltaAngle;
    }

    public double[] getCoordDZ() {
        return coordDZ;
    }

    public double[] getCoordDX() {
        return coordDX;
    }

    public double[] getCoordDY() {
        return coordDY;
    }

    public double[] getCoordRoll() {
        return coordRoll;
    }

    public double[] getCoordMiniPitch() {
        return coordMiniPitch;
    }

    public double[] getCoordPitch() {
        return coordPitch;
    }


    public double[] getCoordB1() {
        return coordB1;
    }

    public double[] getCoordB2() {
        return coordB2;
    }

    public double[] getCoordStick() {
        return coordStick;
    }

    public double[] getCoordPivot() {
        return coordPivot;
    }

    public double[] getCoordBuckL() {
        return coordBuckL;
    }

    public double[] getCoordBuckR() {
        return coordBuckR;
    }

    public double[] getCoordBukt() {
        return coordBukt;
    }

    public double[] getCoordLSH() {
        return coordLSH;
    }

    public double[] getCoordLSV() {
        return coordLSV;
    }
}
