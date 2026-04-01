package packexcalib.exca;


import static packexcalib.exca.DataSaved.L_Bucket;
import static packexcalib.exca.DataSaved.isTiltRotator;
import static packexcalib.exca.DataSaved.piccolaBucket;
import static packexcalib.exca.DataSaved.puntiProfilo;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static packexcalib.exca.Sensors_Decoder.ExtensionBoom;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.WHEELLOADER;

import android.util.Log;

import java.util.Arrays;

import gui.MyApp;
import gui.digging_excavator.DiggingProfile;
import packexcalib.gnss.NmeaListener;
import packexcalib.mymatrix.PointCalculator;
import packexcalib.surfcreator.DistToLine;
import packexcalib.surfcreator.Surface_4pts;
import services.TriangleService;
import utils.DistToPoint;
import utils.MyMCUtils;

public class ExcavatorLib {
    static double hdt0;
    public static ToolFrame currentToolFrame;
    public static double hdt_BOOM, hdt_LAMA;
    static int lowerEdge;
    static double myRollLen, myPitchLen;
    public static double altezzaProiettata, altezzaVerticale, distanzaOrizzontale, slopeProfile;
    public static double yawSensor;
    public static int indexSezione;
    public static boolean overturn;
    public static double[] coordRoll, coordPitch;
    static double[] arrLower = new double[3];
    static double[] arr = new double[6];
    public static double quota2D, quotaLASER_2D, actualX2D, actualY2D, distToSurf, distToSurfX, quotaSx, quotaDx, distanza_inclinata;
    public static double msideC, msideCX, correctStick, correctPitch, correctToolRoll, correctToolPitch, correctDBStickAngle, quotaCentro, correctRoll, correctBucket, correctFlat, correctTilt, correctDeltaAngle, bennaSimulata, correctBoom1, correctBoom2, highestPoint, deg_Roll, Len_Roll, larghezzabenna, correctWTilt, correctEbubbleX, correctEbubbleY, startRX, startRY, startRZ, correctMastLink;
    public static double[] coordinateDZ, coordinateDX, coordinateDY, coordMiniPitch, coordB1, coordB2, coordST, coordiLSV, coordLSH, coordPivoTilt, coordTool;
    public static double[] toolEndCoord = new double[]{0, 0, 0};
    public static double[] coordRotoCenter = new double[]{0, 0, 0};
    public static double[] coordRotoTop = new double[]{0, 0, 0};
    public static double[] bucketCoord = new double[]{0, 0, 0};//centro benna
    public static double[] bucketLeftCoord = new double[]{0, 0, 0};

    //sinistra benna
    public static double[] bucketRightCoord = new double[]{0, 0, 0};
    public static double[] coordBuckProf = new double[]{0, 0, 0};
    //destra benna
    static double hdt_DESTRA, hdt_DRITTO, hdt_SINISTRA;
    public static double[] coordinateLASER = new double[]{0, 0, 0};
    public static double[] startXYZ;
    public static double swing_boom_angle;//TODO
    static double ActualX, ActualY, ActualZ;
    static boolean hasRoto;

    public static void Excavator() {
        try {

            boolean GPS_Enabled = DataSaved.portView >= 2;
            hasRoto = isTiltRotator == 1;
            correctPitch = Offset_Applier.realPitch(DataSaved.offsetPitch);//dato da utilizzare nel software già offsettato
            correctBoom1 = Offset_Applier.realBoom1(DataSaved.offsetBoom1);
            correctBoom2 = Offset_Applier.realBoom2(DataSaved.offsetBoom2);
            correctStick = Offset_Applier.realStick(DataSaved.offsetStick);
            correctRoll = Offset_Applier.realRoll(DataSaved.offsetRoll);
            correctBucket = Offset_Applier.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[0];
            correctFlat = Offset_Applier.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[1];
            correctDeltaAngle = Offset_Applier.realDeltaAngle(DataSaved.offsetTiltDeltaAngle);
            correctDBStickAngle = Offset_Applier.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[2];
            correctTilt = Offset_Applier.realTilt(DataSaved.offsetTilt);
            bennaSimulata = Offset_Applier.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[3];
            deg_Roll = correctRoll;
            correctWTilt = Offset_Applier.realDegWTilt(DataSaved.offsetDegWTilt);
            correctEbubbleX = Offset_Applier.realEBubble_X(DataSaved.offsetBubble_X);
            correctEbubbleY = Offset_Applier.realEBubble_Y(DataSaved.offsetBubble_Y);
            Len_Roll = DataSaved.L_Roll;
            larghezzabenna = DataSaved.W_Bucket;

            hdt0 = ((NmeaListener.mch_Orientation + DataSaved.deltaGPS2) % 360 + 360) % 360;
            if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == DOZER || DataSaved.isWL == DOZER_SIX || DataSaved.isWL == GRADER) {
                if (DataSaved.Extra_Heading != 0) {

                    if (NmeaListener.roof_Orientation != 999.999) {
                        swing_boom_angle = ((NmeaListener.roof_Orientation + DataSaved.offsetSwingExca) % 360 + 360) % 360;

                    } else {
                        swing_boom_angle = hdt0;
                    }
                    hdt_BOOM = ((swing_boom_angle) % 360 + 360) % 360;
                } else {
                    swing_boom_angle = 0;
                    hdt_BOOM = ((hdt0 + swing_boom_angle) % 360 + 360) % 360;
                }
            }
            if (DataSaved.isWL == WHEELLOADER) {
                if (DataSaved.Extra_Heading != 0) {
                    swing_boom_angle = DataSaved.SteerWheel_Result;
                } else {
                    swing_boom_angle = 0;
                }
                hdt_BOOM = ((hdt0 + swing_boom_angle) % 360 + 360) % 360;

            }

            if (GPS_Enabled) {
                if(DataSaved.Exca_Antenna_Mounting==0){
                    Execute_Normal();
                }else {
                    Execute_Normal();
                }
            } else {
                startXYZ = new double[]{0, 0, 0};
                //To Do 2D
                coordinateDZ = new double[]{0, 0, 0};
                coordinateDX = new double[]{0, 0, 0};
                coordinateDY = new double[]{0, 0, 0};
                //qui gestire yaw del sensore frame


                if (MyApp.visibleActivity.toString().contains("Profile")) {
                    hdt_DRITTO = 0;//no rotaz
                } else {
                    hdt_DRITTO = (NmeaListener.roof_Orientation - DataSaved.offsetHDT) + 0;//no rotaz
                }
                double hdt_BOOM = ((hdt_DRITTO + 0) % 360 + 360) % 360;


                hdt_DESTRA = ((hdt_DRITTO + 90) % 360 + 360) % 360;

                hdt_SINISTRA = ((hdt_DRITTO + 270) % 360 + 360) % 360;

                coordRoll = Exca_Quaternion.endPoint(startXYZ, -correctRoll, correctPitch, DataSaved.L_Roll, hdt_DESTRA);

                if (DataSaved.Extra_Heading != 0) {
                    coordMiniPitch = Exca_Quaternion.endPoint(coordRoll, correctPitch, correctRoll, DataSaved.L_Pitch - DataSaved.miniPitch_L, hdt_DRITTO);
                    coordPitch = Exca_Quaternion.endPoint(coordMiniPitch, correctPitch, Deg_Boom_Roll, DataSaved.miniPitch_L, hdt_BOOM);
                } else {
                    coordPitch = Exca_Quaternion.endPoint(coordRoll, correctPitch, Deg_Boom_Roll, DataSaved.L_Pitch, hdt_BOOM);
                }

                overturn = Math.abs(correctRoll) > 85.0d || Math.abs(correctPitch) > 85.0d;
                coordB1 = Exca_Quaternion.endPoint(coordPitch, correctBoom1, Deg_Boom_Roll, DataSaved.L_Boom1, hdt_BOOM);

                if (DataSaved.lrBoom2 != 0) {
                    coordB2 = Exca_Quaternion.endPoint(coordB1, correctBoom2, Deg_Boom_Roll, DataSaved.L_Boom2, hdt_BOOM);

                } else {
                    coordB2 = coordB1;
                }
                coordiLSV = Exca_Quaternion.endPoint(coordB2, correctStick, Deg_Boom_Roll, DataSaved.LSV, hdt_BOOM);
                coordLSH = Exca_Quaternion.endPoint(coordiLSV, correctStick + (90 * Double.compare(DataSaved.LSH, 0)), Deg_Boom_Roll, DataSaved.LSH, hdt_BOOM);
                coordinateLASER = coordLSH;
                coordST = Exca_Quaternion.endPoint(coordB2, correctStick, Deg_Boom_Roll, DataSaved.L_Stick + ExtensionBoom, hdt_BOOM);

                if (DataSaved.lrTilt == 0) {
                    coordPivoTilt = new double[]{0, 0, 0};
                    bucketCoord = RodLocation.rodloc(coordST, correctBucket, Deg_Boom_Roll, DataSaved.L_Bucket, hdt_BOOM);
                    bucketRightCoord = Exca_Quaternion.endPoint(bucketCoord, -Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 90);
                    bucketLeftCoord = Exca_Quaternion.endPoint(bucketCoord, Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 270);
                } else {
                    //to do Tilt
                    coordPivoTilt = RodLocation.rodloc(coordST, correctDeltaAngle, Deg_Boom_Roll, DataSaved.L_Tilt, hdt_BOOM);
                    yawSensor = 0;
                    yawSensor = MyMCUtils.computeDeltaYawFromTiltAndCurl(correctTilt - Deg_Boom_Roll, correctBucket, piccolaBucket, L_Bucket);
                    yawSensor += Sensors_Decoder.Deg_Roto;
                    yawSensor = MyMCUtils.wrap(yawSensor);
                    bucketCoord = RodLocation.rodloc(coordPivoTilt, correctWTilt, correctTilt, DataSaved.piccolaBucket, hdt_BOOM + yawSensor);
                    bucketRightCoord = Exca_Quaternion.endPoint(bucketCoord, -correctTilt, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 90 + yawSensor);
                    bucketLeftCoord = Exca_Quaternion.endPoint(bucketCoord, correctTilt, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 270 + yawSensor);

                }


                if (DataSaved.profileSelected == 0) {
                    double[] p1 = new double[]{0, 0, 0};
                    PointCalculator pointCalculatorS = new PointCalculator(p1);
                    double[] p2 = pointCalculatorS.calculateEndPoint(DataSaved.slopeY, 0, 0, 0, 30, 0);
                    PointCalculator pointCalculatorS1 = new PointCalculator(p2);
                    double[] p3 = pointCalculatorS1.calculateEndPoint(-DataSaved.slopeX, 0, 90, 0, 30, 0);
                    PointCalculator pointCalculatorS2 = new PointCalculator(p3);
                    double[] p4 = pointCalculatorS2.calculateEndPoint(-DataSaved.slopeY, 0, 90, 0, 30, 0);

                    Surface_4pts surface4pts = new Surface_4pts(new double[]{p1[0], p1[1], p1[2], p2[0], p2[1], p2[2], p3[0], p3[1], p3[2], p4[0], p4[1], p4[2], 0});
                    switch (DataSaved.bucketEdge) {
                        case 1:
                            ActualX = ExcavatorLib.bucketRightCoord[0];//x
                            ActualY = ExcavatorLib.bucketRightCoord[1];//y
                            ActualZ = ExcavatorLib.bucketRightCoord[2];//z
                            break;
                        case 0:
                            ActualX = ExcavatorLib.bucketCoord[0];
                            ActualY = ExcavatorLib.bucketCoord[1];
                            ActualZ = ExcavatorLib.bucketCoord[2];
                            break;

                        case -1:
                            ActualX = ExcavatorLib.bucketLeftCoord[0];
                            ActualY = ExcavatorLib.bucketLeftCoord[1];
                            ActualZ = ExcavatorLib.bucketLeftCoord[2];
                            break;
                        default:
                            ActualX = ExcavatorLib.bucketCoord[0];
                            ActualY = ExcavatorLib.bucketCoord[1];
                            ActualZ = ExcavatorLib.bucketCoord[2];
                            break;
                    }
                    quota2D = surface4pts.getAltitudeDifference(ActualX, ActualY, ActualZ);
                    quotaCentro = surface4pts.getAltitudeDifference(bucketCoord[0], bucketCoord[1], bucketCoord[2]);
                    quotaSx = surface4pts.getAltitudeDifference(bucketLeftCoord[0], bucketLeftCoord[1], bucketLeftCoord[2]);
                    quotaDx = surface4pts.getAltitudeDifference(bucketRightCoord[0], bucketRightCoord[1], bucketRightCoord[2]);
                    quotaLASER_2D = surface4pts.getAltitudeDifference(coordinateLASER[0], coordinateLASER[1], coordinateLASER[2]);
                    actualX2D = surface4pts.getSlopesXY(ActualX, ActualY, hdt_BOOM + yawSensor)[0];
                    actualY2D = surface4pts.getSlopesXY(ActualX, ActualY, hdt_BOOM + yawSensor)[1];


                    double[] pC = new double[]{startRX, startRY, startRZ};
                    double[] pA = Exca_Coord_Calc_2D.calculateEndPoint(pC, 0, 0, 30, hdt_SINISTRA);
                    double[] pB = Exca_Coord_Calc_2D.calculateEndPoint(pC, 0, 0, 30, hdt_DESTRA);
                    double distToLine = new DistToLine(bucketCoord[0], bucketCoord[1], pA[0], pA[1], pB[0], pB[1]).getLinedistance();

                    distanza_inclinata = new DistToPoint(startRX, startRY, 0, bucketCoord[0], bucketCoord[1], 0).getDist_to_point();
                    if (distToLine < 0) {
                        distanza_inclinata = distanza_inclinata * 1;
                    } else {
                        distanza_inclinata = distanza_inclinata * -1;
                    }

                    arr = new double[]{coordB1[2], coordB2[2], coordST[2], bucketCoord[2], bucketLeftCoord[2], bucketRightCoord[2]};
                    Arrays.sort(arr);
                    highestPoint = arr[arr.length - 1];


                    if (DataSaved.isLowerEdge) {
                        arrLower = new double[]{bucketLeftCoord[2], bucketCoord[2], bucketRightCoord[2]};
                        int minIndex = 0;
                        for (int i = 1; i < arrLower.length; i++) {
                            if (arrLower[i] < arrLower[minIndex]) {
                                minIndex = i;
                            }
                        }
                        lowerEdge = minIndex;
                        switch (lowerEdge) {
                            case 0:
                                DataSaved.bucketEdge = -1;
                                break;
                            case 1:
                                DataSaved.bucketEdge = 0;
                                break;
                            case 2:
                                DataSaved.bucketEdge = 1;
                                break;
                        }
                    }
                    distToSurf = mDist2Surf(msideC, actualY2D);
                    distToSurfX = mDist2Surf_X(msideCX, actualX2D);

                } else {
                    //to do calcoli
                    for (int i = 0; i < puntiProfilo.length; i++) {
                        for (int j = 0; j < puntiProfilo[i].length; j++) {
                        }

                    }
                    double myX, myY, myZ;

                    double[] X;
                    double[] Y;
                    double[] Z;
                    X = new double[puntiProfilo.length];
                    Y = new double[puntiProfilo.length];
                    Z = new double[puntiProfilo.length];

                    for (int i = 0; i < puntiProfilo.length; i++) {
                        X[i] = puntiProfilo[i][0];
                        Y[i] = puntiProfilo[i][1];

                        Z[i] = puntiProfilo[i][2];
                    }
                    myX = bucketCoord[0] - coordBuckProf[0] + X[DiggingProfile.indexOPSelected - 1];
                    myY = -(bucketCoord[1] - coordBuckProf[1]) + Y[DiggingProfile.indexOPSelected - 1];
                    myZ = bucketCoord[2] - coordBuckProf[2] + Z[DiggingProfile.indexOPSelected - 1];

                    DistToLine[] distToLines = new DistToLine[puntiProfilo.length - 1];

                    for (int i = 0; i < distToLines.length; i++) {
                        distToLines[i] = new DistToLine(myY, myZ, X[i], Y[i], Y[i + 1], Z[i + 1]);

                    }
                    for (int i = 0; i < X.length - 1; i++) {
                        if (myY > Y[i] && myY < Y[i + 1]) {
                            indexSezione = i;
                            double m = (Z[i + 1] - Z[i]) / (Y[i + 1] - Y[i]);
                            double q = Z[i] - m * Y[i];
                            altezzaVerticale = myZ - (m * myY + q);
                            altezzaProiettata = -distToLines[i].getLinedistance();
                            altezzaVerticale = altezzaVerticale + DataSaved.offsetH;
                            altezzaProiettata = altezzaProiettata + DataSaved.offsetH;
                            double diff_x = (double) Y[i + 1] - (double) Y[i];
                            double diff_y = (double) Z[i + 1] - (double) Z[i];
                            slopeProfile = Math.toDegrees(Math.atan2(diff_y, diff_x));
                            break;
                        } else {
                            if (myY < Y[0]) {
                                indexSezione = -1;
                            } else {
                                indexSezione = -2;
                            }
                            altezzaVerticale = 0;
                            altezzaProiettata = 0;
                            slopeProfile = 0;
                        }
                    }
                    if (indexSezione >= 0 || indexSezione == -2) {
                        distanzaOrizzontale = new DistToPoint(myX, myY, 0, X[0], Y[0], 0).getDist_to_point();

                    } else {

                        distanzaOrizzontale = new DistToPoint(myX, myY, 0, X[0], Y[0], 0).getDist_to_point() * -1;

                    }

                    arr = new double[]{coordB1[2], coordB2[2], coordST[2], bucketCoord[2], bucketLeftCoord[2], bucketRightCoord[2]};
                    Arrays.sort(arr);
                    highestPoint = arr[arr.length - 1];


                    if (DataSaved.isLowerEdge) {
                        arrLower = new double[]{bucketLeftCoord[2], bucketCoord[2], bucketRightCoord[2]};
                        int minIndex = 0;
                        for (int i = 1; i < arrLower.length; i++) {
                            if (arrLower[i] < arrLower[minIndex]) {
                                minIndex = i;
                            }
                        }
                        lowerEdge = minIndex;
                        switch (lowerEdge) {
                            case 0:
                                DataSaved.bucketEdge = -1;
                                break;
                            case 1:
                                DataSaved.bucketEdge = 0;
                                break;
                            case 2:
                                DataSaved.bucketEdge = 1;
                                break;
                        }
                    }

                }
            }

        } catch (Exception e) {
            Log.e("XYZ",Log.getStackTraceString(e));
        }

    }

    private static double[] rotateAroundAxisRaw(double[] v, double[] axis, double angleDeg) {
        org.apache.commons.math3.complex.Quaternion q = axisAngleQuaternion(axis, angleDeg);
        return Exca_Quaternion.rotateVector(q, v[0], v[1], v[2]);
    }


    private static double mDist2Surf(double sideC, double slopeY) {

        double out = 0;

        if (slopeY >= 0) {
            double mK_radiants = Math.toRadians(slopeY);
            double Beta = (90 - slopeY) % 90.0d;
            double BetaRad = Math.toRadians(Beta);
            double mYdeg = 180.0 - (slopeY + Beta);
            double mYRad = Math.toRadians(mYdeg);
            double sideA = Math.abs(sideC) * Math.sin(mK_radiants) / Math.sin(mYRad);
            out = sideA * Math.sin(BetaRad) / Math.sin(mK_radiants);
        } else {
            double mK_radiants = Math.toRadians(slopeY * -1);
            double Beta = (90 - (slopeY * -1)) % 90.0d;
            double BetaRad = Math.toRadians(Beta);
            double mYdeg = 180.0 - ((slopeY * -1) + Beta);
            double mYRad = Math.toRadians(mYdeg);
            double sideA = Math.abs(sideC) * Math.sin(mK_radiants) / Math.sin(mYRad);
            out = sideA * Math.sin(BetaRad) / Math.sin(mK_radiants);

        }

        if (Double.isNaN(out)) {
            out = sideC;
        }
        return out * Double.compare(sideC, 0);

    }

    static double mDist2Surf_X(double sideC, double slopeX) {

        double out = 0;
        if (slopeX >= 0) {
            double mK_radiants = Math.toRadians(slopeX);
            double Beta = (90 - slopeX) % 90.0d;
            double BetaRad = Math.toRadians(Beta);
            double mYdeg = 180.0 - (slopeX + Beta);
            double mYRad = Math.toRadians(mYdeg);
            double sideA = Math.abs(sideC) * Math.sin(mK_radiants) / Math.sin(mYRad);
            out = sideA * Math.sin(BetaRad) / Math.sin(mK_radiants);
        } else {
            double mK_radiants = Math.toRadians(slopeX * -1);
            double Beta = (90 - (slopeX * -1)) % 90.0d;
            double BetaRad = Math.toRadians(Beta);
            double mYdeg = 180.0 - ((slopeX * -1) + Beta);
            double mYRad = Math.toRadians(mYdeg);
            double sideA = Math.abs(sideC) * Math.sin(mK_radiants) / Math.sin(mYRad);
            out = sideA * Math.sin(BetaRad) / Math.sin(mK_radiants);

        }

        if (Double.isNaN(out)) {
            out = sideC;
        }
        return out * Double.compare(sideC, 0);


    }


    //utility

    private static double[] add(double[] a, double[] b) {
        return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private static double[] sub(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static double[] scale(double[] v, double s) {
        return new double[]{v[0] * s, v[1] * s, v[2] * s};
    }

    private static double norm(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    private static double[] normalize(double[] v) {
        double n = norm(v);
        if (n < 1e-9) return new double[]{0, 0, 0};
        return new double[]{v[0] / n, v[1] / n, v[2] / n};
    }

    private static double[] cross(double[] a, double[] b) {
        return new double[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    private static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static org.apache.commons.math3.complex.Quaternion axisAngleQuaternion(double[] axis, double angleDeg) {
        double[] ax = normalize(axis);
        double half = Math.toRadians(angleDeg) * 0.5;
        double s = Math.sin(half);

        return new org.apache.commons.math3.complex.Quaternion(
                Math.cos(half),
                ax[0] * s,
                ax[1] * s,
                ax[2] * s
        );
    }

    public static class ToolFrame {
        public double[] origin;   // centro benna
        public double[] forward;  // direzione punta -> retro o viceversa, basta che sia coerente
        public double[] right;    // lato dx benna
        public double[] up;       // normale locale
        public double[] rotoCenter;
        public double[] tiltPivot;

        public ToolFrame(double[] origin, double[] forward, double[] right, double[] up,
                         double[] rotoCenter, double[] tiltPivot) {
            this.origin = origin;
            this.forward = forward;
            this.right = right;
            this.up = up;
            this.rotoCenter = rotoCenter;
            this.tiltPivot = tiltPivot;
        }
    }

    private static void Execute_Normal(){
        {
            myPitchLen = 0;
            myRollLen = 0;
            double hdtR = ((hdt0 + 90) % 360 + 360) % 360;

            double hdtL = ((hdt0 - 90) % 360 + 360) % 360;

            double hdtReverse = ((hdt0 + 180) % 360 + 360) % 360;

            // with GPS

            startXYZ = new double[]{NmeaListener.Est1, NmeaListener.Nord1, NmeaListener.Quota1};
            switch (DataSaved.isWL) {
                case EXCAVATOR:
                case WHEELLOADER:
                    double deltaYY = DataSaved.deltaY;
                    if (DataSaved.Extra_Heading != 0) {
                        if (DataSaved.deltaY < 0) {
                            deltaYY = DataSaved.deltaY + DataSaved.miniPitch_L;
                        } else {
                            deltaYY = DataSaved.deltaY - DataSaved.miniPitch_L;
                        }
                    }
                    coordinateDZ = Exca_Quaternion.endPoint(startXYZ, correctPitch - 90, correctRoll, DataSaved.deltaZ, hdt0);
                    if (DataSaved.deltaX < 0) {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, correctRoll, -correctPitch, DataSaved.deltaX, hdtL);
                    } else {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, -correctRoll, correctPitch, DataSaved.deltaX, hdtR);
                    }
                    if (deltaYY < 0) {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, -correctPitch, -correctRoll, Math.abs(deltaYY), hdtReverse);
                    } else {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, correctPitch, correctRoll, deltaYY, hdt0);
                    }//DY = Centro perno boom1

                    if (DataSaved.Extra_Heading != 0) {
                        coordMiniPitch = Exca_Quaternion.endPoint(coordinateDY, correctPitch, Deg_Boom_Roll, DataSaved.miniPitch_L, hdt_BOOM);
                    } else {
                        coordMiniPitch = coordinateDY;
                    }
                    overturn = Math.abs(correctRoll) > 85.0d || Math.abs(correctPitch) > 85.0d;
                    coordB1 = Exca_Quaternion.endPoint(coordMiniPitch, correctBoom1, Deg_Boom_Roll, DataSaved.L_Boom1, hdt_BOOM);
                    if (DataSaved.lrBoom2 != 0) {
                        coordB2 = Exca_Quaternion.endPoint(coordB1, correctBoom2, Deg_Boom_Roll, DataSaved.L_Boom2, hdt_BOOM);

                    } else {
                        coordB2 = coordB1;
                    }
                    coordiLSV = Exca_Quaternion.endPoint(coordB2, correctStick, Deg_Boom_Roll, DataSaved.LSV, hdt_BOOM);
                    coordLSH = Exca_Quaternion.endPoint(coordiLSV, correctStick + (90 * Double.compare(DataSaved.LSH, 0)), Deg_Boom_Roll, DataSaved.LSH, hdt_BOOM);
                    coordinateLASER = coordLSH;
                    coordST = Exca_Quaternion.endPoint(coordB2, correctStick, Deg_Boom_Roll, DataSaved.L_Stick + ExtensionBoom, hdt_BOOM);
                    if (DataSaved.lrTilt == 0) {
                        yawSensor = 0;
                        coordPivoTilt = coordST;
                        bucketCoord = Exca_Quaternion.endPoint(coordST, correctBucket, Deg_Boom_Roll, DataSaved.L_Bucket, hdt_BOOM);
                        bucketRightCoord = Exca_Quaternion.endPoint(bucketCoord, -Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 90);
                        bucketLeftCoord = Exca_Quaternion.endPoint(bucketCoord, Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 270);

                    } else {


                        yawSensor = MyMCUtils.computeDeltaYawFromTiltAndCurl(
                                correctTilt - Deg_Boom_Roll,
                                correctBucket,
                                piccolaBucket,
                                L_Bucket
                        );
                        yawSensor = MyMCUtils.wrap(yawSensor);
                        if (!hasRoto) {


                            coordPivoTilt = Exca_Quaternion.endPoint(
                                    coordST,
                                    correctDeltaAngle,
                                    Deg_Boom_Roll,
                                    DataSaved.L_Tilt,
                                    hdt_BOOM
                            );

                            bucketCoord = Exca_Quaternion.endPoint(
                                    coordPivoTilt,
                                    correctWTilt,
                                    correctTilt,
                                    DataSaved.piccolaBucket,
                                    hdt_BOOM + yawSensor
                            );

                            bucketRightCoord = Exca_Quaternion.endPoint(
                                    bucketCoord,
                                    -correctTilt,
                                    0,
                                    DataSaved.W_Bucket * 0.5d,
                                    hdt_BOOM + 90 + yawSensor
                            );

                            bucketLeftCoord = Exca_Quaternion.endPoint(
                                    bucketCoord,
                                    correctTilt,
                                    0,
                                    DataSaved.W_Bucket * 0.5d,
                                    hdt_BOOM + 270 + yawSensor
                            );

                        } else {

                            // =========================================
                            // ROTOTILT CINEMATICA CORRETTA
                            // roto applicato al vettore reale
                            // =========================================

                            // 1) pivot tilt
                            coordPivoTilt = Exca_Quaternion.endPoint(
                                    coordST,
                                    correctDeltaAngle,
                                    Deg_Boom_Roll,
                                    DataSaved.L_Tilt,
                                    hdt_BOOM
                            );

                            // 2) centro perno di tilt all'altezza del centro di rotazione
                            coordRotoTop = Exca_Quaternion.endPoint(
                                    coordPivoTilt,
                                    correctDeltaAngle + 90,
                                    Deg_Boom_Roll,
                                    DataSaved.Offset_Engcon_Forward, hdt_BOOM
                            );

                            //3 centro rotazione ROTOTILT, ultimo punto della catena cinematica vincolata al BOOM

                            coordRotoCenter = Exca_Quaternion.endPoint(
                                    coordRotoTop,
                                    correctDeltaAngle,
                                    correctTilt,
                                    DataSaved.Offset_Engcon_Down,
                                    hdt_BOOM + yawSensor

                            );

                            // =====================================================
// COMPLETAMENTO ROTOTILT DEFINITIVO
// =====================================================

                            double[] P = coordPivoTilt;    // punto noto sulla retta rossa
                            double[] R = coordRotoCenter;  // centro roto
                            double[] T = coordRotoTop;     // punto sull'asse roto

// -------------------------------------------------
// 1) ASSE ROTO
// -------------------------------------------------
                            double[] k = normalize(sub(T, R));

// -------------------------------------------------
// 2) COSTRUISCO LA DIREZIONE DEL ROSSO
//    usando la stessa cinematica già valida del tilt normale
// -------------------------------------------------
                            double testLen = Math.max(1.0d, DataSaved.L_RotoToBucket + DataSaved.L_Tilt + 1.0d);

                            double[] Q = Exca_Quaternion.endPoint(
                                    P,
                                    correctWTilt,
                                    correctTilt,
                                    testLen,
                                    hdt_BOOM + yawSensor
                            );

// direzione della retta rossa
                            double[] d = normalize(sub(Q, P));

// -------------------------------------------------
// 3) INTERSEZIONE RETTA - SFERA
//    B0 = P + t*d
//    |B0 - R| = L
// -------------------------------------------------
                            double L = DataSaved.L_RotoToBucket;

                            double[] PR = sub(P, R);

                            double B_q = 2.0 * dot(d, PR);
                            double C_q = dot(PR, PR) - (L * L);

                            double disc = B_q * B_q - 4.0 * C_q;
                            if (disc < 0.0) disc = 0.0;

                            double sqrtDisc = Math.sqrt(disc);

                            double t1 = (-B_q + sqrtDisc) * 0.5d;
                            double t2 = (-B_q - sqrtDisc) * 0.5d;

// scelgo il punto "in avanti" lungo la retta rossa
                            double t;
                            if (t1 >= 0.0 && t2 >= 0.0) {
                                t = Math.max(t1, t2);
                            } else if (t1 >= 0.0) {
                                t = t1;
                            } else if (t2 >= 0.0) {
                                t = t2;
                            } else {
                                t = Math.max(t1, t2);
                            }

// centro benna PRE-roto
                            double[] B0 = add(P, scale(d, t));

// -------------------------------------------------
// 4) VETTORE ROTO -> BENNA PRE-ROTO
// -------------------------------------------------
                            double[] v0 = sub(B0, R);

// -------------------------------------------------
// 5) APPLICO IL ROTO
// -------------------------------------------------
                            double rotoDeg = -MyMCUtils.wrap(Sensors_Decoder.Deg_Roto);

                            double[] v = rotateAroundAxisRaw(v0, k, rotoDeg);

// centro benna finale
                            bucketCoord = add(R, v);

// -------------------------------------------------
// 6) ASSE TRASVERSALE BENNA
// -------------------------------------------------
                            double[] right0 = normalize(cross(k, v0));

// fallback sicurezza
                            if (norm(right0) < 1e-9) {
                                // asse laterale arbitrario ma ortogonale a k
                                right0 = normalize(cross(k, d));
                                if (norm(right0) < 1e-9) {
                                    right0 = new double[]{1.0, 0.0, 0.0};
                                }
                            }

                            double[] right = rotateAroundAxisRaw(right0, k, rotoDeg);
                            right = normalize(right);

// -------------------------------------------------
// 7) SPIGOLI DX / SX
// -------------------------------------------------
                            double halfW = DataSaved.W_Bucket * 0.5d;

                            bucketRightCoord = add(bucketCoord, scale(right, halfW));
                            bucketLeftCoord  = add(bucketCoord, scale(right, -halfW));



                        }


                    }


                    //qui abbiamo le coordinate GPS della benna UTM nostre
                    arr = new double[]{coordB1[2], coordB2[2], coordST[2], bucketCoord[2], bucketLeftCoord[2], bucketRightCoord[2]};
                    Arrays.sort(arr);
                    highestPoint = arr[arr.length - 1];

                    if (DataSaved.isLowerEdge) {
                        double sinistra, centro, destra;

                        if (TriangleService.ltOffGrid) {
                            sinistra = Double.MAX_VALUE;
                        } else {
                            sinistra = Math.abs(TriangleService.quota3D_SX);
                        }
                        if (TriangleService.ctOffGrid) {
                            centro = Double.MAX_VALUE;
                        } else {
                            centro = Math.abs(TriangleService.quota3D_CT);
                        }
                        if (TriangleService.rtOffGrid) {
                            destra = Double.MAX_VALUE;
                        } else {
                            destra = Math.abs(TriangleService.quota3D_DX);
                        }
                        arrLower = new double[]{sinistra, centro, destra};
                        int minIndex = 0;
                        for (int i = 1; i < arrLower.length; i++) {
                            if (arrLower[i] < arrLower[minIndex]) {
                                minIndex = i;
                            }
                        }
                        lowerEdge = minIndex;
                        switch (lowerEdge) {
                            case 0:
                                DataSaved.bucketEdge = -1;
                                break;
                            case 1:
                                DataSaved.bucketEdge = 0;
                                break;
                            case 2:
                                DataSaved.bucketEdge = 1;
                                break;
                        }
                    }
                    break;
                case DOZER:
                case DOZER_SIX:
                case GRADER:
                    //dozer e grader
                    hdt_LAMA = hdt0;
                    yawSensor = 0;

                    coordinateDZ = Exca_Quaternion.endPoint(startXYZ, correctPitch - 90, correctRoll, DataSaved.deltaZ + DataSaved.usuraLamaCX, hdt0);
                    if (DataSaved.deltaX < 0) {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, correctRoll, -correctPitch, DataSaved.deltaX, hdtL);
                    } else {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, -correctRoll, correctPitch, DataSaved.deltaX, hdtR);
                    }
                    if (DataSaved.deltaY < 0) {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, -correctPitch, -correctRoll, Math.abs(DataSaved.deltaY), hdtReverse + yawSensor);
                    } else {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, correctPitch, correctRoll, DataSaved.deltaY, hdt0);
                    }//DY = Centro LAMA
                    bucketCoord = coordinateDY;
                    bucketRightCoord = Exca_Quaternion.endPoint(bucketCoord, -correctRoll, correctPitch, DataSaved.W_Blade_RIGHT, hdt0 + 90 + yawSensor);
                    bucketLeftCoord = Exca_Quaternion.endPoint(bucketCoord, correctRoll, -correctPitch, DataSaved.W_Blade_LEFT, hdt0 + 270 + yawSensor);
                    if (DataSaved.isLowerEdge) {
                        arrLower = new double[]{bucketLeftCoord[2], bucketCoord[2], bucketRightCoord[2]};
                        int minIndex = 0;
                        for (int i = 1; i < arrLower.length; i++) {
                            if (arrLower[i] < arrLower[minIndex]) {
                                minIndex = i;
                            }
                        }
                        lowerEdge = minIndex;
                        switch (lowerEdge) {
                            case 0:
                                DataSaved.bucketEdge = -1;
                                break;
                            case 1:
                                DataSaved.bucketEdge = 0;
                                break;
                            case 2:
                                DataSaved.bucketEdge = 1;
                                break;
                        }
                    }

                    break;
            }


        }
    }
    private static void Execute_Body(){
        {
            myPitchLen = 0;
            myRollLen = 0;
            double hdtR = ((hdt0 + 90) % 360 + 360) % 360;

            double hdtL = ((hdt0 - 90) % 360 + 360) % 360;

            double hdtReverse = ((hdt0 + 180) % 360 + 360) % 360;
            hdt_BOOM=hdt0;

            // with GPS

            startXYZ = new double[]{NmeaListener.Est1, NmeaListener.Nord1, NmeaListener.Quota1};
            switch (DataSaved.isWL) {
                case EXCAVATOR:
                case WHEELLOADER:
                    double deltaYY = DataSaved.deltaY;
                    coordinateDZ = Exca_Quaternion.endPoint(startXYZ, correctBoom1 - 90, Deg_Boom_Roll, DataSaved.deltaZ, hdt0);
                    if (DataSaved.deltaX < 0) {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, Deg_Boom_Roll, -correctBoom1, DataSaved.deltaX, hdtL);
                    } else {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, -Deg_Boom_Roll, correctBoom1, DataSaved.deltaX, hdtR);
                    }
                    if (deltaYY < 0) {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, -correctBoom1, -Deg_Boom_Roll, Math.abs(deltaYY), hdtReverse);
                    } else {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, correctBoom1, Deg_Boom_Roll, deltaYY, hdt0);
                    }//DY = Centro perno boom1

                    coordMiniPitch = coordinateDY;
                    coordB1 = coordinateDY;
                    if (DataSaved.lrBoom2 != 0) {
                        coordB2 = Exca_Quaternion.endPoint(coordB1, correctBoom2, Deg_Boom_Roll, DataSaved.L_Boom2, hdt_BOOM);

                    } else {
                        coordB2 = coordB1;
                    }
                    coordiLSV = Exca_Quaternion.endPoint(coordB2, correctStick, Deg_Boom_Roll, DataSaved.LSV, hdt_BOOM);
                    coordLSH = Exca_Quaternion.endPoint(coordiLSV, correctStick + (90 * Double.compare(DataSaved.LSH, 0)), Deg_Boom_Roll, DataSaved.LSH, hdt_BOOM);
                    coordinateLASER = coordLSH;
                    coordST = Exca_Quaternion.endPoint(coordB2, correctStick, Deg_Boom_Roll, DataSaved.L_Stick + ExtensionBoom, hdt_BOOM);
                    if (DataSaved.lrTilt == 0) {
                        yawSensor = 0;
                        coordPivoTilt = coordST;
                        bucketCoord = Exca_Quaternion.endPoint(coordST, correctBucket, Deg_Boom_Roll, DataSaved.L_Bucket, hdt_BOOM);
                        bucketRightCoord = Exca_Quaternion.endPoint(bucketCoord, -Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 90);
                        bucketLeftCoord = Exca_Quaternion.endPoint(bucketCoord, Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 270);

                    } else {


                        yawSensor = MyMCUtils.computeDeltaYawFromTiltAndCurl(
                                correctTilt - Deg_Boom_Roll,
                                correctBucket,
                                piccolaBucket,
                                L_Bucket
                        );
                        yawSensor = MyMCUtils.wrap(yawSensor);
                        if (!hasRoto) {


                            coordPivoTilt = Exca_Quaternion.endPoint(
                                    coordST,
                                    correctDeltaAngle,
                                    Deg_Boom_Roll,
                                    DataSaved.L_Tilt,
                                    hdt_BOOM
                            );

                            bucketCoord = Exca_Quaternion.endPoint(
                                    coordPivoTilt,
                                    correctWTilt,
                                    correctTilt,
                                    DataSaved.piccolaBucket,
                                    hdt_BOOM + yawSensor
                            );

                            bucketRightCoord = Exca_Quaternion.endPoint(
                                    bucketCoord,
                                    -correctTilt,
                                    0,
                                    DataSaved.W_Bucket * 0.5d,
                                    hdt_BOOM + 90 + yawSensor
                            );

                            bucketLeftCoord = Exca_Quaternion.endPoint(
                                    bucketCoord,
                                    correctTilt,
                                    0,
                                    DataSaved.W_Bucket * 0.5d,
                                    hdt_BOOM + 270 + yawSensor
                            );

                        } else {

                            // =========================================
                            // ROTOTILT CINEMATICA CORRETTA
                            // roto applicato al vettore reale
                            // =========================================

                            // 1) pivot tilt
                            coordPivoTilt = Exca_Quaternion.endPoint(
                                    coordST,
                                    correctDeltaAngle,
                                    Deg_Boom_Roll,
                                    DataSaved.L_Tilt,
                                    hdt_BOOM
                            );

                            // 2) centro perno di tilt all'altezza del centro di rotazione
                            coordRotoTop = Exca_Quaternion.endPoint(
                                    coordPivoTilt,
                                    correctDeltaAngle + 90,
                                    Deg_Boom_Roll,
                                    DataSaved.Offset_Engcon_Forward, hdt_BOOM
                            );

                            //3 centro rotazione ROTOTILT, ultimo punto della catena cinematica vincolata al BOOM

                            coordRotoCenter = Exca_Quaternion.endPoint(
                                    coordRotoTop,
                                    correctDeltaAngle,
                                    correctTilt,
                                    DataSaved.Offset_Engcon_Down,
                                    hdt_BOOM + yawSensor

                            );

                            // =====================================================
// COMPLETAMENTO ROTOTILT DEFINITIVO
// =====================================================

                            double[] P = coordPivoTilt;    // punto noto sulla retta rossa
                            double[] R = coordRotoCenter;  // centro roto
                            double[] T = coordRotoTop;     // punto sull'asse roto

// -------------------------------------------------
// 1) ASSE ROTO
// -------------------------------------------------
                            double[] k = normalize(sub(T, R));

// -------------------------------------------------
// 2) COSTRUISCO LA DIREZIONE DEL ROSSO
//    usando la stessa cinematica già valida del tilt normale
// -------------------------------------------------
                            double testLen = Math.max(1.0d, DataSaved.L_RotoToBucket + DataSaved.L_Tilt + 1.0d);

                            double[] Q = Exca_Quaternion.endPoint(
                                    P,
                                    correctWTilt,
                                    correctTilt,
                                    testLen,
                                    hdt_BOOM + yawSensor
                            );

// direzione della retta rossa
                            double[] d = normalize(sub(Q, P));

// -------------------------------------------------
// 3) INTERSEZIONE RETTA - SFERA
//    B0 = P + t*d
//    |B0 - R| = L
// -------------------------------------------------
                            double L = DataSaved.L_RotoToBucket;

                            double[] PR = sub(P, R);

                            double B_q = 2.0 * dot(d, PR);
                            double C_q = dot(PR, PR) - (L * L);

                            double disc = B_q * B_q - 4.0 * C_q;
                            if (disc < 0.0) disc = 0.0;

                            double sqrtDisc = Math.sqrt(disc);

                            double t1 = (-B_q + sqrtDisc) * 0.5d;
                            double t2 = (-B_q - sqrtDisc) * 0.5d;

// scelgo il punto "in avanti" lungo la retta rossa
                            double t;
                            if (t1 >= 0.0 && t2 >= 0.0) {
                                t = Math.max(t1, t2);
                            } else if (t1 >= 0.0) {
                                t = t1;
                            } else if (t2 >= 0.0) {
                                t = t2;
                            } else {
                                t = Math.max(t1, t2);
                            }

// centro benna PRE-roto
                            double[] B0 = add(P, scale(d, t));

// -------------------------------------------------
// 4) VETTORE ROTO -> BENNA PRE-ROTO
// -------------------------------------------------
                            double[] v0 = sub(B0, R);

// -------------------------------------------------
// 5) APPLICO IL ROTO
// -------------------------------------------------
                            double rotoDeg = -MyMCUtils.wrap(Sensors_Decoder.Deg_Roto);

                            double[] v = rotateAroundAxisRaw(v0, k, rotoDeg);

// centro benna finale
                            bucketCoord = add(R, v);

// -------------------------------------------------
// 6) ASSE TRASVERSALE BENNA
// -------------------------------------------------
                            double[] right0 = normalize(cross(k, v0));

// fallback sicurezza
                            if (norm(right0) < 1e-9) {
                                // asse laterale arbitrario ma ortogonale a k
                                right0 = normalize(cross(k, d));
                                if (norm(right0) < 1e-9) {
                                    right0 = new double[]{1.0, 0.0, 0.0};
                                }
                            }

                            double[] right = rotateAroundAxisRaw(right0, k, rotoDeg);
                            right = normalize(right);

// -------------------------------------------------
// 7) SPIGOLI DX / SX
// -------------------------------------------------
                            double halfW = DataSaved.W_Bucket * 0.5d;

                            bucketRightCoord = add(bucketCoord, scale(right, halfW));
                            bucketLeftCoord  = add(bucketCoord, scale(right, -halfW));



                        }


                    }


                    //qui abbiamo le coordinate GPS della benna UTM nostre
                    arr = new double[]{coordB1[2], coordB2[2], coordST[2], bucketCoord[2], bucketLeftCoord[2], bucketRightCoord[2]};
                    Arrays.sort(arr);
                    highestPoint = arr[arr.length - 1];

                    if (DataSaved.isLowerEdge) {
                        double sinistra, centro, destra;

                        if (TriangleService.ltOffGrid) {
                            sinistra = Double.MAX_VALUE;
                        } else {
                            sinistra = Math.abs(TriangleService.quota3D_SX);
                        }
                        if (TriangleService.ctOffGrid) {
                            centro = Double.MAX_VALUE;
                        } else {
                            centro = Math.abs(TriangleService.quota3D_CT);
                        }
                        if (TriangleService.rtOffGrid) {
                            destra = Double.MAX_VALUE;
                        } else {
                            destra = Math.abs(TriangleService.quota3D_DX);
                        }
                        arrLower = new double[]{sinistra, centro, destra};
                        int minIndex = 0;
                        for (int i = 1; i < arrLower.length; i++) {
                            if (arrLower[i] < arrLower[minIndex]) {
                                minIndex = i;
                            }
                        }
                        lowerEdge = minIndex;
                        switch (lowerEdge) {
                            case 0:
                                DataSaved.bucketEdge = -1;
                                break;
                            case 1:
                                DataSaved.bucketEdge = 0;
                                break;
                            case 2:
                                DataSaved.bucketEdge = 1;
                                break;
                        }
                    }
                    break;
                case DOZER:
                case DOZER_SIX:
                case GRADER:
                    //dozer e grader
                    hdt_LAMA = hdt0;
                    yawSensor = 0;

                    coordinateDZ = Exca_Quaternion.endPoint(startXYZ, correctPitch - 90, correctRoll, DataSaved.deltaZ + DataSaved.usuraLamaCX, hdt0);
                    if (DataSaved.deltaX < 0) {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, correctRoll, -correctPitch, DataSaved.deltaX, hdtL);
                    } else {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, -correctRoll, correctPitch, DataSaved.deltaX, hdtR);
                    }
                    if (DataSaved.deltaY < 0) {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, -correctPitch, -correctRoll, Math.abs(DataSaved.deltaY), hdtReverse + yawSensor);
                    } else {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, correctPitch, correctRoll, DataSaved.deltaY, hdt0);
                    }//DY = Centro LAMA
                    bucketCoord = coordinateDY;
                    bucketRightCoord = Exca_Quaternion.endPoint(bucketCoord, -correctRoll, correctPitch, DataSaved.W_Blade_RIGHT, hdt0 + 90 + yawSensor);
                    bucketLeftCoord = Exca_Quaternion.endPoint(bucketCoord, correctRoll, -correctPitch, DataSaved.W_Blade_LEFT, hdt0 + 270 + yawSensor);
                    if (DataSaved.isLowerEdge) {
                        arrLower = new double[]{bucketLeftCoord[2], bucketCoord[2], bucketRightCoord[2]};
                        int minIndex = 0;
                        for (int i = 1; i < arrLower.length; i++) {
                            if (arrLower[i] < arrLower[minIndex]) {
                                minIndex = i;
                            }
                        }
                        lowerEdge = minIndex;
                        switch (lowerEdge) {
                            case 0:
                                DataSaved.bucketEdge = -1;
                                break;
                            case 1:
                                DataSaved.bucketEdge = 0;
                                break;
                            case 2:
                                DataSaved.bucketEdge = 1;
                                break;
                        }
                    }

                    break;
            }


        }
    }

}