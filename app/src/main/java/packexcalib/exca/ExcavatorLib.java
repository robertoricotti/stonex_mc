package packexcalib.exca;


import static packexcalib.exca.DataSaved.puntiProfilo;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static packexcalib.exca.Sensors_Decoder.Deg_Yaw_Tilt;
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

public class ExcavatorLib {

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
    public static double msideC, msideCX, correctStick, correctPitch, correctToolRoll, correctToolPitch, correctDBStickAngle, quotaCentro, correctRoll, correctBucket, correctFlat, correctTilt, correctDeltaAngle, bennaSimulata, correctBoom1, correctBoom2, highestPoint, deg_Roll, Len_Roll, larghezzabenna, correctWTilt, correctEbubbleX, correctEbubbleY, startRX, startRY, startRZ;
    public static double[] coordinateDZ, coordinateDX, coordinateDY, coordMiniPitch, coordB1, coordB2, coordST, coordiLSV, coordLSH, coordPivoTilt, coordTool;
    public static double[] toolBitCoord = new double[]{0, 0, 0};
    public static double[] toolEndCoord = new double[]{0, 0, 0};

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

    public static void Excavator() {
        try {

            boolean GPS_Enabled = DataSaved.portView >= 2;


            correctPitch = Excavator_RealValues.realPitch(DataSaved.offsetPitch);//dato da utilizzare nel software già offsettato
            correctBoom1 = Excavator_RealValues.realBoom1(DataSaved.offsetBoom1);
            correctBoom2 = Excavator_RealValues.realBoom2(DataSaved.offsetBoom2);
            correctStick = Excavator_RealValues.realStick(DataSaved.offsetStick);
            correctRoll = Excavator_RealValues.realRoll(DataSaved.offsetRoll);
            correctBucket = Excavator_RealValues.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[0];
            correctFlat = Excavator_RealValues.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[1];
            correctDeltaAngle = Excavator_RealValues.realDeltaAngle(DataSaved.offsetTiltDeltaAngle);
            correctDBStickAngle = Excavator_RealValues.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[2];
            correctTilt = Excavator_RealValues.realTilt(DataSaved.offsetTilt);
            bennaSimulata = Excavator_RealValues.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[3];
            deg_Roll = correctRoll;
            correctWTilt = Excavator_RealValues.realDegWTilt(DataSaved.offsetDegWTilt);
            correctEbubbleX = Excavator_RealValues.realEBubble_X(DataSaved.offsetBubble_X);
            correctEbubbleY = Excavator_RealValues.realEBubble_Y(DataSaved.offsetBubble_Y);
            Len_Roll = DataSaved.L_Roll;
            larghezzabenna = DataSaved.W_Bucket;


            if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == DOZER || DataSaved.isWL == DOZER_SIX || DataSaved.isWL == GRADER) {
                if (DataSaved.Extra_Heading != 0) {

                    if (NmeaListener.roof_Orientation != 999.999) {
                        swing_boom_angle = NmeaListener.roof_Orientation - (NmeaListener.mch_Orientation + DataSaved.deltaGPS2);

                    } else {
                        swing_boom_angle = 0;
                    }
                } else {
                    swing_boom_angle = 0;
                }
            }
            if (DataSaved.isWL == WHEELLOADER) {
                if (DataSaved.Extra_Heading != 0) {
                    swing_boom_angle = DataSaved.SteerWheel_Result;
                } else {
                    swing_boom_angle = 0;
                }

            }

            if (GPS_Enabled) {
                double offsetSwing = 0;
                if (DataSaved.isWL == EXCAVATOR && DataSaved.Extra_Heading == 1) {
                    offsetSwing = DataSaved.offsetSwingExca;
                }

                myPitchLen = 0;
                myRollLen = 0;
                double hdt0 = ((NmeaListener.mch_Orientation + DataSaved.deltaGPS2) % 360 + 360) % 360;
                ////////
                hdt_BOOM = ((hdt0 + swing_boom_angle + offsetSwing) % 360 + 360) % 360;

                double hdtR = ((hdt0 + 90) % 360 + 360) % 360;

                double hdtL = ((hdt0 - 90) % 360 + 360) % 360;

                double hdtReverse = ((hdt0 + 180) % 360 + 360) % 360;

                // with GPS

                startXYZ = new double[]{NmeaListener.Est1, NmeaListener.Nord1, NmeaListener.Quota1};
                switch (DataSaved.isWL) {
                    case EXCAVATOR:
                    case WHEELLOADER:
                        coordinateDZ = Exca_Quaternion.endPoint(startXYZ, correctPitch - 90, correctRoll, DataSaved.deltaZ, hdt0);
                        if (DataSaved.deltaX < 0) {
                            coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, correctRoll, -correctPitch, DataSaved.deltaX, hdtL);
                        } else {
                            coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, -correctRoll, correctPitch, DataSaved.deltaX, hdtR);
                        }
                        if (DataSaved.deltaY < 0) {
                            coordinateDY = Exca_Quaternion.endPoint(coordinateDX, -correctPitch, -correctRoll, Math.abs(DataSaved.deltaY) + DataSaved.miniPitch_L, hdtReverse);
                        } else {
                            coordinateDY = Exca_Quaternion.endPoint(coordinateDX, correctPitch, correctRoll, DataSaved.deltaY - DataSaved.miniPitch_L, hdt0);
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
                            coordPivoTilt = coordST;
                            bucketCoord = Exca_Quaternion.endPoint(coordST, correctBucket, Deg_Boom_Roll, DataSaved.L_Bucket, hdt_BOOM);
                            bucketRightCoord = Exca_Quaternion.endPoint(bucketCoord, -Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 90);
                            bucketLeftCoord = Exca_Quaternion.endPoint(bucketCoord, Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 270);
                        } else {
                            yawSensor = 0;

                            if (Sensors_Decoder.isMobaTilt) {
                                // --- Reset automatico yaw quando la benna è dritta
                                if (Math.abs(correctTilt) < 5 && Math.abs(correctWTilt + 90) < 40) {
                                    DataSaved.offsetYaw = Deg_Yaw_Tilt;
                                    yawSensor = 0;
                                } else {
                                    yawSensor = (Deg_Yaw_Tilt - DataSaved.offsetYaw);
                                    yawSensor=0.98*yawSensor;
                                }

                                // --- Rototilt aggiuntivo (se presente)
                                yawSensor += Sensors_Decoder.Deg_Roto;

                                // --- Limita range
                                if (yawSensor < -180) yawSensor += 360;
                                if (yawSensor > 180) yawSensor -= 360;


                            } else {
                                yawSensor = Sensors_Decoder.Deg_Roto;
                            }
                            coordPivoTilt = Exca_Quaternion.endPoint(coordST, correctDeltaAngle, Deg_Boom_Roll, DataSaved.L_Tilt, hdt_BOOM);

                            bucketCoord = Exca_Quaternion.endPoint(coordPivoTilt, correctWTilt, correctTilt, DataSaved.piccolaBucket, hdt_BOOM + yawSensor);
                            bucketRightCoord = Exca_Quaternion.endPoint(bucketCoord, -correctTilt, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 90 + yawSensor);
                            bucketLeftCoord = Exca_Quaternion.endPoint(bucketCoord, correctTilt, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 270 + yawSensor);

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
                double hdt_BOOM = ((hdt_DRITTO + swing_boom_angle) % 360 + 360) % 360;


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
                    actualX2D = surface4pts.getSlopesXY(ActualX, ActualY, hdt_BOOM)[0];
                    actualY2D = surface4pts.getSlopesXY(ActualX, ActualY, hdt_BOOM)[1];


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
            System.out.println("Error XYZ" + e);
        }

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


}