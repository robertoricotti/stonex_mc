package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;
import static packexcalib.exca.ExcavatorLib.coordPivoTilt;
import static packexcalib.exca.ExcavatorLib.coordST;
import static packexcalib.exca.ExcavatorLib.correctBucket;
import static packexcalib.exca.ExcavatorLib.correctTilt;
import static packexcalib.exca.ExcavatorLib.correctWTilt;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static packexcalib.exca.ExcavatorLib.yawSensor;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static services.TriangleService.getProjectedPointOnSegment3D;
import static services.TriangleService.glLinePoint;
import static services.TriangleService.glLinePunto;
import static services.TriangleService.glPuntoTerra;
import static services.TriangleService.glSegmentEnd;
import static services.TriangleService.glSegmentPoint;
import static services.TriangleService.glTerraPunto;
import static services.TriangleService.orientamentoFreccia;

import dxf.Point3D;
import gui.my_opengl.Point3DF;
import gui.my_opengl.Polyline2DArc;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import utils.DistToPoint;

public class My_Benna {
    public static double[] coneDown, coneUp;
    static Point3DF coneDownF, coneUpF;
    static Point3DF P1_sx, P2_sx, P3_sx, P4_sx, P5_sx, P6_sx, P7_sx, P8_sx, PM1_sx, PM2_sx;
    static Point3DF P1_dx, P2_dx, P3_dx, P4_dx, P5_dx, P6_dx, P7_dx, P8_dx, PM1_dx, PM2_dx;
    static Point3DF PBASE, PBASE_ALTA;//punti attacco
    static Point3DF PBASE_H, PBASE_ALTA_H;//punti attacco alto
    static Point3DF P_A_Front, P_A_Back, P_A_FF;//punti tilt
    static Point3DF SPIG_L, SPIG_C, SPIG_R, SPIG_LB, SPIG_CB, SPIG_RB;

    public static double[] fw, bw, lt, rt;
    static Point3DF fwF, bwF, ltF, rtF, start;
    public static float larghezza_attacco, raggioPivot;
    public static float altezzaAttacco;//altezza dell'attacco
    static double mhdt;

    private static Point3DF[] fiancataSX() {
        mhdt = hdt_BOOM;
        double[] splA;
        double[] spcA;
        double[] sprA;
        double[] splB;
        double[] spcB;
        double[] sprB;
        double flatLen;
        double base;//distanza p2>p4
        double hMax;//base/2
        double segPM1;//dist p1>p3*0.15
        double segPM2;//dist p1>p3*0.25

        double[] p1;//left
        double[] p2;//left fine flat basso
        double[] p3;//top left
        double[] p4;//top left fine flat
        double[] p5;//hmax superiore
        double[] p6;//hmax inferiore
        double[] p7;//hmax *0.7 superiore
        double[] p8;//hmax *0.7 inferiore
        double[] pm1;//bocca left alto
        double[] pm2;//bocca left basso
        double flatTop;


        if (DataSaved.lrTilt == 0) {
            //NO TILT
            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.L_Bucket;
            flatTop = flatLen * 0.8;
            flatTop = Math.max(0.18, Math.min(flatTop, flatLen * 0.8));
            p1 = bucketLeftCoord;
            p2 = Exca_Quaternion.endPoint(bucketLeftCoord, correctBucket + 90 + DataSaved.flat, Deg_Boom_Roll, flatLen, mhdt);

            p3 = Exca_Quaternion.endPoint(p1, correctBucket + 180, Deg_Boom_Roll, (DataSaved.L_Bucket * 0.9), mhdt);


            p4 = Exca_Quaternion.endPoint(p3, correctBucket + 90 - (DataSaved.flat * 0.5), 0, flatTop, mhdt);
            base = DistToPoint.dist3D(p4, p2);
            //

            hMax = base * 0.4;
            segPM1 = DistToPoint.dist3D(p1, p3) * 0.15;
            segPM2 = DistToPoint.dist3D(p1, p3) * 0.25;
            pm1 = Exca_Quaternion.endPoint(p3, correctBucket + 30, Deg_Boom_Roll, segPM1, mhdt);
            pm2 = Exca_Quaternion.endPoint(pm1, correctBucket + 12, Deg_Boom_Roll, segPM2, mhdt);


            Polyline2DArc.SegmentData[] segments = Polyline2DArc.calculateArcSegments(base, hMax);
            double L1, L2, L3, L4;
            double d1, d2, d3, d4;
            L1 = segments[0].length;
            L2 = segments[1].length;
            L3 = segments[2].length;
            L4 = segments[3].length;

            d1 = segments[0].angleDegrees;
            d2 = segments[1].angleDegrees;
            d3 = segments[2].angleDegrees;
            d4 = segments[3].angleDegrees;

            p8 = Exca_Quaternion.endPoint(p2, correctBucket + 90 + (90 - d1), Deg_Boom_Roll, L1, mhdt);
            p6 = Exca_Quaternion.endPoint(p8, correctBucket + 90 + (90 - d2), Deg_Boom_Roll, L2, mhdt);
            p5 = Exca_Quaternion.endPoint(p6, correctBucket + 90 + (90 - d3), Deg_Boom_Roll, L3, mhdt);
            p7 = Exca_Quaternion.endPoint(p5, correctBucket + 90 + (90 - d4), Deg_Boom_Roll, L4, mhdt);

            splA = Exca_Quaternion.endPoint(bucketLeftCoord, 90, 0, 0.05, mhdt + 0);
            spcA = Exca_Quaternion.endPoint(bucketCoord, 90, 0, 0.05, mhdt + 0);
            sprA = Exca_Quaternion.endPoint(bucketRightCoord, 90, 0, 0.05, mhdt + 0);
            splB = Exca_Quaternion.endPoint(bucketLeftCoord, -90, 0, 0.05, mhdt + 0);
            spcB = Exca_Quaternion.endPoint(bucketCoord, -90, 0, 0.05, mhdt + 0);
            sprB = Exca_Quaternion.endPoint(bucketRightCoord, -90, 0, 0.05, mhdt + 0);

        } else if (!DataSaved.isTiltRotator) {
            //TILT
            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.piccolaBucket;
            flatTop = flatLen * 0.8;
            flatTop = Math.max(0.18, Math.min(flatTop, flatLen * 0.8));
            p1 = bucketLeftCoord;
            p2 = Exca_Quaternion.endPoint(bucketLeftCoord, correctWTilt + 90 + DataSaved.flat, correctTilt, flatLen, mhdt + yawSensor);

            p3 = Exca_Quaternion.endPoint(p1, correctWTilt + 180, correctTilt, DataSaved.piccolaBucket * 0.9, mhdt + yawSensor);

            p4 = Exca_Quaternion.endPoint(p3, correctWTilt + 90 - (DataSaved.flat * 0.5), correctTilt, flatTop, mhdt + yawSensor);
            base = DistToPoint.dist3D(p4, p2);

            hMax = base * 0.4;

            segPM1 = DistToPoint.dist3D(p1, p3) * 0.15;
            segPM2 = DistToPoint.dist3D(p1, p3) * 0.25;
            pm1 = Exca_Quaternion.endPoint(p3, correctWTilt + 30, correctTilt, segPM1, mhdt + yawSensor);
            pm2 = Exca_Quaternion.endPoint(pm1, correctWTilt + 12, correctTilt, segPM2, mhdt + yawSensor);

            Polyline2DArc.SegmentData[] segmentsT = Polyline2DArc.calculateArcSegments(base, hMax);
            double L1, L2, L3, L4;
            double d1, d2, d3, d4;
            L1 = segmentsT[0].length;
            L2 = segmentsT[1].length;
            L3 = segmentsT[2].length;
            L4 = segmentsT[3].length;

            d1 = segmentsT[0].angleDegrees;
            d2 = segmentsT[1].angleDegrees;
            d3 = segmentsT[2].angleDegrees;
            d4 = segmentsT[3].angleDegrees;

            p8 = Exca_Quaternion.endPoint(p2, correctWTilt + 90 + (90 - d1), correctTilt, L1, mhdt + yawSensor);
            p6 = Exca_Quaternion.endPoint(p8, correctWTilt + 90 + (90 - d2), correctTilt, L2, mhdt + yawSensor);
            p5 = Exca_Quaternion.endPoint(p6, correctWTilt + 90 + (90 - d3), correctTilt, L3, mhdt + yawSensor);
            p7 = Exca_Quaternion.endPoint(p5, correctWTilt + 90 + (90 - d4), correctTilt, L4, mhdt + yawSensor);

            splA = Exca_Quaternion.endPoint(bucketLeftCoord, 90, 0, 0.05, mhdt + yawSensor);
            spcA = Exca_Quaternion.endPoint(bucketCoord, 90, 0, 0.05, mhdt + yawSensor);
            sprA = Exca_Quaternion.endPoint(bucketRightCoord, 90, 0, 0.05, mhdt + yawSensor);
            splB = Exca_Quaternion.endPoint(bucketLeftCoord, -90, 0, 0.05, mhdt + yawSensor);
            spcB = Exca_Quaternion.endPoint(bucketCoord, -90, 0, 0.05, mhdt + yawSensor);
            sprB = Exca_Quaternion.endPoint(bucketRightCoord, -90, 0, 0.05, mhdt + yawSensor);

        } else {
            // TILT ROTATOR
            BucketFrame f = buildBucketFrameRototilt();

            double halfW = DistToPoint.dist3D(bucketLeftCoord, bucketRightCoord) * 0.5;
            double depth = DataSaved.piccolaBucket * 0.9;

            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.piccolaBucket;
            flatTop = flatLen * 0.8;
            flatTop = Math.max(0.18, Math.min(flatTop, flatLen * 0.8));

            // tagliente sinistro
            p1 = f.point(-halfW, 0.0, 0.0);

            // profilo nel piano locale (B,U)
            p2 = moveBU(p1, f, 90.0 + DataSaved.flat, flatLen);
            p3 = moveBU(p1, f, 180.0, depth);
            p4 = moveBU(p3, f, 90.0 - (DataSaved.flat * 0.5), flatTop);

            base = DistToPoint.dist3D(p4, p2);
            hMax = base * 0.4;

            segPM1 = DistToPoint.dist3D(p1, p3) * 0.15;
            segPM2 = DistToPoint.dist3D(p1, p3) * 0.25;

            pm1 = moveBU(p3, f, 30.0, segPM1);
            pm2 = moveBU(pm1, f, 12.0, segPM2);

            Polyline2DArc.SegmentData[] segmentsT = Polyline2DArc.calculateArcSegments(base, hMax);

            double L1 = segmentsT[0].length;
            double L2 = segmentsT[1].length;
            double L3 = segmentsT[2].length;
            double L4 = segmentsT[3].length;

            double d1 = segmentsT[0].angleDegrees;
            double d2 = segmentsT[1].angleDegrees;
            double d3 = segmentsT[2].angleDegrees;
            double d4 = segmentsT[3].angleDegrees;

            p8 = moveBU(p2, f, 180.0 - d1, L1);
            p6 = moveBU(p8, f, 180.0 - d2, L2);
            p5 = moveBU(p6, f, 180.0 - d3, L3);
            p7 = moveBU(p5, f, 180.0 - d4, L4);

            double[] rightEdge = f.point(+halfW, 0.0, 0.0);

// riferimento cono: verticale assoluto
            double coneOffsetUp = 0.02;
            double coneOffsetDown = 0.10;

// punti superiori (origine cono)
            splA = new double[]{p1[0],      p1[1],      p1[2] + coneOffsetUp};
            spcA = new double[]{f.O[0],     f.O[1],     f.O[2] + coneOffsetUp};
            sprA = new double[]{rightEdge[0], rightEdge[1], rightEdge[2] + coneOffsetUp};

// punti inferiori (verso il basso assoluto)
            splB = new double[]{p1[0],      p1[1],      p1[2] - coneOffsetDown};
            spcB = new double[]{f.O[0],     f.O[1],     f.O[2] - coneOffsetDown};
            sprB = new double[]{rightEdge[0], rightEdge[1], rightEdge[2] - coneOffsetDown};
        }

        double[] pos = new double[0];


        switch (DataSaved.bucketEdge) {
            case -1:
                pos = bucketLeftCoord;

                break;
            case 0:
                pos = ExcavatorLib.bucketCoord;

                break;
            case 1:
                pos = ExcavatorLib.bucketRightCoord;

                break;
        }
        double coneLen = 10.0;

        coneDown = new double[]{
                pos[0],
                pos[1],
                pos[2] - coneLen
        };

        coneUp = new double[]{
                pos[0],
                pos[1],
                pos[2] + coneLen
        };
        if (DataSaved.lrTilt == 0) {
            fw = Exca_Quaternion.endPoint(pos, 0, 0, 50, mhdt);
            bw = Exca_Quaternion.endPoint(pos, 0, 0, 15, mhdt + 180);
            lt = Exca_Quaternion.endPoint(pos, Deg_Boom_Roll, 0, 10, mhdt + 270);
            rt = Exca_Quaternion.endPoint(pos, -Deg_Boom_Roll, 0, 10, mhdt + 90);

        } else if (!DataSaved.isTiltRotator) {
            fw = Exca_Quaternion.endPoint(pos, 0, 0, 50, mhdt + yawSensor);
            bw = Exca_Quaternion.endPoint(pos, 0, 0, 15, mhdt + yawSensor + 180);
            lt = Exca_Quaternion.endPoint(pos, correctTilt, 0, 10, mhdt + yawSensor + 270);
            rt = Exca_Quaternion.endPoint(pos, -correctTilt, 0, 10, mhdt + yawSensor + 90);

        } else {
            // ROTOTILT:
            // croce verde classica sul piano orizzontale assoluto
            fw = Exca_Quaternion.endPoint(pos, 0, 0, 50, mhdt);
            bw = Exca_Quaternion.endPoint(pos, 0, 0, 15, mhdt + 180);
            lt = Exca_Quaternion.endPoint(pos, 0, 0, 10, mhdt + 270);
            rt = Exca_Quaternion.endPoint(pos, 0, 0, 10, mhdt + 90);
        }
        switch (DataSaved.isAutoSnap) {
            case 0:

                orientamentoFreccia = 0;
                glLinePoint = new Point3DF(0f, 0f, 0f);
                glSegmentPoint = new Point3DF(0f, 0f, 0f);
                glSegmentEnd = new Point3DF(0f, 0f, 0f);
                glLinePunto = new Point3DF(0f, 0f, 0f);
                glPuntoTerra = new Point3DF(0f, 0f, 0f);
                glTerraPunto = new Point3DF(0f, 0f, 0f);

                break;

            case 1:
                orientamentoFreccia = 0;
                if (DataSaved.points != null && !DataSaved.points.isEmpty()) {
                    switch (DataSaved.bucketEdge) {
                        case -1:
                            Point3DF pbuck = new Point3DF((float) (bucketLeftCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (bucketLeftCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (bucketLeftCoord[2] - DataSaved.glL_AnchorView[2]) * scale);
                            Point3DF pline = new Point3DF((float) (DataSaved.nearestPoint.getX() - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (DataSaved.nearestPoint.getY() - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (DataSaved.nearestPoint.getZ() - DataSaved.glL_AnchorView[2]) * scale);
                            glLinePunto = new Point3DF(pbuck.getX(), pbuck.getY(), pbuck.getZ());
                            glPuntoTerra = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glTerraPunto = pline;
                            break;

                        case 0:
                            Point3DF pbuckC = new Point3DF((float) (bucketCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (bucketCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (bucketCoord[2] - DataSaved.glL_AnchorView[2]) * scale);
                            Point3DF plineC = new Point3DF((float) (DataSaved.nearestPoint.getX() - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (DataSaved.nearestPoint.getY() - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (DataSaved.nearestPoint.getZ() - DataSaved.glL_AnchorView[2]) * scale);
                            glLinePunto = new Point3DF(pbuckC.getX(), pbuckC.getY(), pbuckC.getZ());
                            glPuntoTerra = new Point3DF(pbuckC.getX(), pbuckC.getY(), plineC.getZ());
                            glTerraPunto = plineC;
                            break;

                        case 1:
                            Point3DF pbuckR = new Point3DF((float) (bucketRightCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (bucketRightCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (bucketRightCoord[2] - DataSaved.glL_AnchorView[2]) * scale);
                            Point3DF plineR = new Point3DF((float) (DataSaved.nearestPoint.getX() - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (DataSaved.nearestPoint.getY() - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (DataSaved.nearestPoint.getZ() - DataSaved.glL_AnchorView[2]) * scale);
                            glLinePunto = new Point3DF(pbuckR.getX(), pbuckR.getY(), pbuckR.getZ());
                            glPuntoTerra = new Point3DF(pbuckR.getX(), pbuckR.getY(), plineR.getZ());
                            glTerraPunto = plineR;
                            break;
                    }
                } else {
                    DataSaved.isAutoSnap = 0;
                }
                break;

            case 2:

                if (DataSaved.filteredPolylines != null && !DataSaved.filteredPolylines.isEmpty()) {
                    switch (DataSaved.bucketEdge) {
                        case -1:
                            Point3DF pbuck = pTransform(bucketLeftCoord, DataSaved.glL_AnchorView, scale);
                            Point3D p = getProjectedPointOnSegment3D(new Point3D(bucketLeftCoord[0], bucketLeftCoord[1], bucketLeftCoord[2]),
                                    DataSaved.nearestSegment.getStart(), DataSaved.nearestSegment.getEnd());
                            Point3DF pline = pTransform(new double[]{p.getX(), p.getY(), p.getZ()}, DataSaved.glL_AnchorView, scale);
                            glLinePoint = pbuck;
                            glSegmentPoint = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glSegmentEnd = pline;
                            orientamentoFreccia = My_LocationCalc.calcBearingXY(bucketLeftCoord[0], bucketLeftCoord[1], p.getX(), p.getY());

                            break;

                        case 0:

                            Point3DF pbuckC = pTransform(bucketCoord, DataSaved.glL_AnchorView, scale);
                            Point3D pC = getProjectedPointOnSegment3D(new Point3D(bucketCoord[0], bucketCoord[1], bucketCoord[2]),
                                    DataSaved.nearestSegment.getStart(), DataSaved.nearestSegment.getEnd());
                            Point3DF plineC = pTransform(new double[]{pC.getX(), pC.getY(), pC.getZ()}, DataSaved.glL_AnchorView, scale);
                            glLinePoint = pbuckC;
                            glSegmentPoint = new Point3DF(pbuckC.getX(), pbuckC.getY(), plineC.getZ());
                            glSegmentEnd = plineC;
                            orientamentoFreccia = My_LocationCalc.calcBearingXY(bucketCoord[0], bucketCoord[1], pC.getX(), pC.getY());

                            break;

                        case 1:

                            Point3DF pbuckR = pTransform(bucketRightCoord, DataSaved.glL_AnchorView, scale);
                            Point3D pR = getProjectedPointOnSegment3D(new Point3D(bucketRightCoord[0], bucketRightCoord[1], bucketRightCoord[2]),
                                    DataSaved.nearestSegment.getStart(), DataSaved.nearestSegment.getEnd());
                            Point3DF plineR = pTransform(new double[]{pR.getX(), pR.getY(), pR.getZ()}, DataSaved.glL_AnchorView, scale);
                            glLinePoint = pbuckR;
                            glSegmentPoint = new Point3DF(pbuckR.getX(), pbuckR.getY(), plineR.getZ());
                            glSegmentEnd = plineR;
                            orientamentoFreccia = My_LocationCalc.calcBearingXY(bucketRightCoord[0], bucketRightCoord[1], pR.getX(), pR.getY());

                            break;


                    }

                } else {
                    DataSaved.isAutoSnap = 0;

                }


                break;


        }

        start = pTransform(pos, DataSaved.glL_AnchorView, scale);
        fwF = pTransform(fw, DataSaved.glL_AnchorView, scale);
        bwF = pTransform(bw, DataSaved.glL_AnchorView, scale);
        ltF = pTransform(lt, DataSaved.glL_AnchorView, scale);
        rtF = pTransform(rt, DataSaved.glL_AnchorView, scale);

        coneDownF = pTransform(coneDown, DataSaved.glL_AnchorView, scale);
        coneUpF = pTransform(coneUp, DataSaved.glL_AnchorView, scale);

        P1_sx = pTransform(p1, DataSaved.glL_AnchorView, scale);
        P2_sx = pTransform(p2, DataSaved.glL_AnchorView, scale);
        P3_sx = pTransform(p3, DataSaved.glL_AnchorView, scale);
        P4_sx = pTransform(p4, DataSaved.glL_AnchorView, scale);
        P5_sx = pTransform(p5, DataSaved.glL_AnchorView, scale);
        P6_sx = pTransform(p6, DataSaved.glL_AnchorView, scale);
        P7_sx = pTransform(p7, DataSaved.glL_AnchorView, scale);
        P8_sx = pTransform(p8, DataSaved.glL_AnchorView, scale);
        PM1_sx = pTransform(pm1, DataSaved.glL_AnchorView, scale);
        PM2_sx = pTransform(pm2, DataSaved.glL_AnchorView, scale);
        SPIG_L = pTransform(splA, DataSaved.glL_AnchorView, scale);
        SPIG_C = pTransform(spcA, DataSaved.glL_AnchorView, scale);
        SPIG_R = pTransform(sprA, DataSaved.glL_AnchorView, scale);
        SPIG_LB = pTransform(splB, DataSaved.glL_AnchorView, scale);
        SPIG_CB = pTransform(spcB, DataSaved.glL_AnchorView, scale);
        SPIG_RB = pTransform(sprB, DataSaved.glL_AnchorView, scale);


        return new Point3DF[]{
                //sequenza 1,pm2,pm1,3,4,7,5,6,8,2,1
                P1_sx, PM2_sx, PM1_sx, P3_sx, P4_sx, P7_sx, P5_sx, P6_sx, P8_sx, P2_sx, P1_sx
        };

    }

    private static Point3DF[] fiancataDX() {
        double flatLen;
        double base;//distanza p2>p4
        double hMax;//base/2
        double segPM1;//dist p1>p3*0.15
        double segPM2;//dist p1>p3*0.25
        double[] p1;//left
        double[] p2;//left fine flat basso
        double[] p3;//top left
        double[] p4;//top left fine flat
        double[] p5;//hmax superiore
        double[] p6;//hmax inferiore
        double[] p7;//hmax *0.7 superiore
        double[] p8;//hmax *0.7 inferiore
        double[] pm1;//bocca left alto
        double[] pm2;//bocca left basso
        double flatTop;


        if (DataSaved.lrTilt == 0) {
            //NO TILT
            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.L_Bucket;
            flatTop = flatLen * 0.8;
            flatTop = Math.max(0.18, Math.min(flatTop, flatLen * 0.8));
            p1 = ExcavatorLib.bucketRightCoord;
            p2 = Exca_Quaternion.endPoint(p1, correctBucket + 90 + DataSaved.flat, Deg_Boom_Roll, flatLen, mhdt);

            p3 = Exca_Quaternion.endPoint(p1, correctBucket + 180, Deg_Boom_Roll, (DataSaved.L_Bucket * 0.9), mhdt);


            p4 = Exca_Quaternion.endPoint(p3, correctBucket + 90 - (DataSaved.flat * 0.5), 0, flatTop, mhdt);
            base = DistToPoint.dist3D(p4, p2);

            hMax = base * 0.4;
            segPM1 = DistToPoint.dist3D(p1, p3) * 0.15;
            segPM2 = DistToPoint.dist3D(p1, p3) * 0.25;
            pm1 = Exca_Quaternion.endPoint(p3, correctBucket + 30, Deg_Boom_Roll, segPM1, mhdt);
            pm2 = Exca_Quaternion.endPoint(pm1, correctBucket + 12, Deg_Boom_Roll, segPM2, mhdt);


            Polyline2DArc.SegmentData[] segments = Polyline2DArc.calculateArcSegments(base, hMax);
            double L1, L2, L3, L4;
            double d1, d2, d3, d4;
            L1 = segments[0].length;
            L2 = segments[1].length;
            L3 = segments[2].length;
            L4 = segments[3].length;

            d1 = segments[0].angleDegrees;
            d2 = segments[1].angleDegrees;
            d3 = segments[2].angleDegrees;
            d4 = segments[3].angleDegrees;

            p8 = Exca_Quaternion.endPoint(p2, correctBucket + 90 + (90 - d1), Deg_Boom_Roll, L1, mhdt);
            p6 = Exca_Quaternion.endPoint(p8, correctBucket + 90 + (90 - d2), Deg_Boom_Roll, L2, mhdt);
            p5 = Exca_Quaternion.endPoint(p6, correctBucket + 90 + (90 - d3), Deg_Boom_Roll, L3, mhdt);
            p7 = Exca_Quaternion.endPoint(p5, correctBucket + 90 + (90 - d4), Deg_Boom_Roll, L4, mhdt);


        } else if (!DataSaved.isTiltRotator) {
            //TILT
            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.piccolaBucket;
            flatTop = flatLen * 0.8;
            flatTop = Math.max(0.18, Math.min(flatTop, flatLen * 0.8));
            p1 = ExcavatorLib.bucketRightCoord;
            p2 = Exca_Quaternion.endPoint(p1, correctWTilt + 90 + DataSaved.flat, correctTilt, flatLen, mhdt + yawSensor);

            p3 = Exca_Quaternion.endPoint(p1, correctWTilt + 180, correctTilt, DataSaved.piccolaBucket * 0.9, mhdt + yawSensor);

            p4 = Exca_Quaternion.endPoint(p3, correctWTilt + 90 - (DataSaved.flat * 0.5), correctTilt, flatTop, mhdt + yawSensor);
            base = DistToPoint.dist3D(p4, p2);

            hMax = base * 0.4;
            segPM1 = DistToPoint.dist3D(p1, p3) * 0.15;
            segPM2 = DistToPoint.dist3D(p1, p3) * 0.25;
            pm1 = Exca_Quaternion.endPoint(p3, correctWTilt + 30, correctTilt, segPM1, mhdt + yawSensor);
            pm2 = Exca_Quaternion.endPoint(pm1, correctWTilt + 12, correctTilt, segPM2, mhdt + yawSensor);

            Polyline2DArc.SegmentData[] segmentsT = Polyline2DArc.calculateArcSegments(base, hMax);
            double L1, L2, L3, L4;
            double d1, d2, d3, d4;
            L1 = segmentsT[0].length;
            L2 = segmentsT[1].length;
            L3 = segmentsT[2].length;
            L4 = segmentsT[3].length;

            d1 = segmentsT[0].angleDegrees;
            d2 = segmentsT[1].angleDegrees;
            d3 = segmentsT[2].angleDegrees;
            d4 = segmentsT[3].angleDegrees;

            p8 = Exca_Quaternion.endPoint(p2, correctWTilt + 90 + (90 - d1), correctTilt, L1, mhdt + yawSensor);
            p6 = Exca_Quaternion.endPoint(p8, correctWTilt + 90 + (90 - d2), correctTilt, L2, mhdt + yawSensor);
            p5 = Exca_Quaternion.endPoint(p6, correctWTilt + 90 + (90 - d3), correctTilt, L3, mhdt + yawSensor);
            p7 = Exca_Quaternion.endPoint(p5, correctWTilt + 90 + (90 - d4), correctTilt, L4, mhdt + yawSensor);

        } else {
            // TILT ROTATOR
            BucketFrame f = buildBucketFrameRototilt();

            double halfW = DistToPoint.dist3D(bucketLeftCoord, bucketRightCoord) * 0.5;
            double depth = DataSaved.piccolaBucket * 0.9;

            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.piccolaBucket;
            flatTop = flatLen * 0.8;
            flatTop = Math.max(0.18, Math.min(flatTop, flatLen * 0.8));

            // tagliente destro
            p1 = f.point(+halfW, 0.0, 0.0);

            p2 = moveBU(p1, f, 90.0 + DataSaved.flat, flatLen);
            p3 = moveBU(p1, f, 180.0, depth);
            p4 = moveBU(p3, f, 90.0 - (DataSaved.flat * 0.5), flatTop);

            base = DistToPoint.dist3D(p4, p2);
            hMax = base * 0.4;

            segPM1 = DistToPoint.dist3D(p1, p3) * 0.15;
            segPM2 = DistToPoint.dist3D(p1, p3) * 0.25;

            pm1 = moveBU(p3, f, 30.0, segPM1);
            pm2 = moveBU(pm1, f, 12.0, segPM2);

            Polyline2DArc.SegmentData[] segmentsT = Polyline2DArc.calculateArcSegments(base, hMax);

            double L1 = segmentsT[0].length;
            double L2 = segmentsT[1].length;
            double L3 = segmentsT[2].length;
            double L4 = segmentsT[3].length;

            double d1 = segmentsT[0].angleDegrees;
            double d2 = segmentsT[1].angleDegrees;
            double d3 = segmentsT[2].angleDegrees;
            double d4 = segmentsT[3].angleDegrees;

            p8 = moveBU(p2, f, 180.0 - d1, L1);
            p6 = moveBU(p8, f, 180.0 - d2, L2);
            p5 = moveBU(p6, f, 180.0 - d3, L3);
            p7 = moveBU(p5, f, 180.0 - d4, L4);
        }
        P1_dx = pTransform(p1, DataSaved.glL_AnchorView, scale);
        P2_dx = pTransform(p2, DataSaved.glL_AnchorView, scale);
        P3_dx = pTransform(p3, DataSaved.glL_AnchorView, scale);
        P4_dx = pTransform(p4, DataSaved.glL_AnchorView, scale);
        P5_dx = pTransform(p5, DataSaved.glL_AnchorView, scale);
        P6_dx = pTransform(p6, DataSaved.glL_AnchorView, scale);
        P7_dx = pTransform(p7, DataSaved.glL_AnchorView, scale);
        P8_dx = pTransform(p8, DataSaved.glL_AnchorView, scale);
        PM1_dx = pTransform(pm1, DataSaved.glL_AnchorView, scale);
        PM2_dx = pTransform(pm2, DataSaved.glL_AnchorView, scale);


        return new Point3DF[]{
                //sequenza 1,pm2,pm1,3,4,7,5,6,8,2,1
                P1_dx, PM2_dx, PM1_dx, P3_dx, P4_dx, P7_dx, P5_dx, P6_dx, P8_dx, P2_dx, P1_dx
        };

    }

    public static float[] attacco() {
        larghezza_attacco = (float) (Math.max(0.15, Math.min((DataSaved.L_Stick * 0.13f), 0.45)) * scale);
        double altezza;//altezza dell'attacco
        double[] pmB;//centro attacco basso
        double[] pmA;//centro attacco alto
        double[] paFront = new double[0], paBack = new double[0], paFrontFront = new double[0];
        double[] pmBH;//centro attacco basso H
        double[] pmAH;//centro attacco alto H

        if (DataSaved.lrTilt == 0) {
            //No tilt
            altezza = DataSaved.L_Bucket - (DataSaved.L_Bucket * 0.9);
            altezzaAttacco = (float) (altezza * scale);
            raggioPivot = (float) (Math.max(0.05, Math.min(DataSaved.L_Bucket - (DataSaved.L_Bucket * 0.92), 0.10)));
            pmB = Exca_Quaternion.endPoint(ExcavatorLib.bucketCoord, correctBucket + 180, Deg_Boom_Roll, (DataSaved.L_Bucket * 0.9), mhdt);
            pmA = Exca_Quaternion.endPoint(pmB, correctBucket + 180 - (DataSaved.flat * 0.5), Deg_Boom_Roll, altezza, mhdt);
            pmAH = coordST;
            pmBH = Exca_Quaternion.endPoint(pmAH, correctBucket - (DataSaved.flat * 0.5), Deg_Boom_Roll, altezza, mhdt);

            PBASE = pTransform(pmB, DataSaved.glL_AnchorView, scale);
            PBASE_ALTA = pTransform(pmA, DataSaved.glL_AnchorView, scale);
            P_A_Front = pTransform(coordST, DataSaved.glL_AnchorView, scale);
            P_A_Back = pTransform(coordST, DataSaved.glL_AnchorView, scale);
            P_A_FF = pTransform(coordST, DataSaved.glL_AnchorView, scale);
            PBASE_ALTA_H = pTransform(pmAH, DataSaved.glL_AnchorView, scale);
            PBASE_H = pTransform(pmBH, DataSaved.glL_AnchorView, scale);

        } else if (!DataSaved.isTiltRotator) {
            // tilt normale - lascia il tuo codice attuale
            altezza = DataSaved.L_Bucket - (DataSaved.piccolaBucket * 0.95) - DataSaved.L_Tilt;
            altezzaAttacco = (float) (altezza * scale);

            pmB = Exca_Quaternion.endPoint(ExcavatorLib.bucketCoord, correctWTilt + 180, correctTilt, DataSaved.piccolaBucket * 0.9, mhdt + yawSensor);
            pmA = Exca_Quaternion.endPoint(pmB, correctWTilt + 180 - (DataSaved.flat * 0.5), correctTilt, altezza, mhdt + yawSensor);

            paFront = coordPivoTilt;
            paBack = Exca_Quaternion.endPoint(coordPivoTilt, ExcavatorLib.correctDeltaAngle + 90, Deg_Boom_Roll, larghezza_attacco * 0.5, mhdt);
            paFrontFront = Exca_Quaternion.endPoint(coordPivoTilt, ExcavatorLib.correctDeltaAngle + 90 + 180, Deg_Boom_Roll, larghezza_attacco * 0.5, mhdt);

            pmAH = coordST;
            pmBH = Exca_Quaternion.endPoint(coordST, ExcavatorLib.correctDeltaAngle, Deg_Boom_Roll, DataSaved.L_Tilt, mhdt);

            raggioPivot = (float) (DistToPoint.dist3D(pmA, pmBH) * scale);

            PBASE = pTransform(pmB, DataSaved.glL_AnchorView, scale);
            PBASE_ALTA = pTransform(pmA, DataSaved.glL_AnchorView, scale);
            P_A_Front = pTransform(paFront, DataSaved.glL_AnchorView, scale);
            P_A_Back = pTransform(paBack, DataSaved.glL_AnchorView, scale);
            P_A_FF = pTransform(paFrontFront, DataSaved.glL_AnchorView, scale);
            PBASE_ALTA_H = pTransform(pmAH, DataSaved.glL_AnchorView, scale);
            PBASE_H = pTransform(pmBH, DataSaved.glL_AnchorView, scale);

        } else {
            // TILT ROTATOR
            BucketFrame f = buildBucketFrameRototilt();
            altezza = DataSaved.L_Bucket - (DataSaved.piccolaBucket * 0.95) - DataSaved.L_Tilt;
            altezzaAttacco = (float) (altezza * scale);
            double depthBack = DataSaved.piccolaBucket * 0.9;
            // retro benna e parte alta, coerenti col frame locale
            pmB = f.point(0.0, depthBack, 0.0);
            pmA = f.point(0.0, depthBack, altezza);
            // attacco sul pivot rototilt
            paFront = coordPivoTilt;
            paBack = add(coordPivoTilt, scale3(f.R, larghezza_attacco * 0.5));
            paFrontFront = add(coordPivoTilt, scale3(f.R, -larghezza_attacco * 0.5));
            // parte alta stick/tilt
            pmAH = coordST;
            pmBH = coordPivoTilt;
            // raggio pivot stabile e rigido
            raggioPivot = (float) (DistToPoint.dist3D(pmA, pmBH) * scale);
            PBASE = pTransform(pmB, DataSaved.glL_AnchorView, scale);
            PBASE_ALTA = pTransform(pmA, DataSaved.glL_AnchorView, scale);
            P_A_Front = pTransform(paFront, DataSaved.glL_AnchorView, scale);
            P_A_Back = pTransform(paBack, DataSaved.glL_AnchorView, scale);
            P_A_FF = pTransform(paFrontFront, DataSaved.glL_AnchorView, scale);
            PBASE_ALTA_H = pTransform(pmAH, DataSaved.glL_AnchorView, scale);
            PBASE_H = pTransform(pmBH, DataSaved.glL_AnchorView, scale);
        }

        return new float[]{
                PBASE.getX(), PBASE.getY(), PBASE.getZ(),
                PBASE_ALTA.getX(), PBASE_ALTA.getY(), PBASE_ALTA.getZ(),
                P_A_Front.getX(), P_A_Front.getY(), P_A_Front.getZ(),
                P_A_Back.getX(), P_A_Back.getY(), P_A_Back.getZ(),
                P_A_FF.getX(), P_A_FF.getY(), P_A_FF.getZ(),
                PBASE_ALTA_H.getX(), PBASE_ALTA_H.getY(), PBASE_ALTA_H.getZ(),
                PBASE_H.getX(), PBASE_H.getY(), PBASE_H.getZ()

        };
    }


    public static Point3DF[] puntiBenna() {
        return new Point3DF[]{
                //sx
                fiancataSX()[0],
                fiancataSX()[1],
                fiancataSX()[2],
                fiancataSX()[3],
                fiancataSX()[4],
                fiancataSX()[5],
                fiancataSX()[6],
                fiancataSX()[7],
                fiancataSX()[8],
                fiancataSX()[9],
                fiancataSX()[10],

                //dx
                fiancataDX()[0],
                fiancataDX()[1],
                fiancataDX()[2],
                fiancataDX()[3],
                fiancataDX()[4],
                fiancataDX()[5],
                fiancataDX()[6],
                fiancataDX()[7],
                fiancataDX()[8],
                fiancataDX()[9],
                fiancataDX()[10],
                SPIG_C, //22
                SPIG_R, //23
                SPIG_L, //24
                SPIG_CB, //25
                SPIG_RB, //26
                SPIG_LB, //27
                glLinePoint, //28
                glSegmentPoint, //29
                glSegmentEnd, //30
                glLinePunto, //31
                glPuntoTerra, //32
                glTerraPunto //33


        };

    }

    public static final short[] leftSideIndices = {
            0, 9, 1,
            9, 8, 1,
            8, 7, 1,
            7, 6, 1,
            6, 5, 1,
            5, 2, 1,
            5, 4, 2,
            4, 3, 2
    };

    public static final short[] rightSideIndices = {
            //sequenza 1,pm2,pm1,3,4,7,5,6,8,2,1
            11, 12, 20,
            20, 12, 19,
            19, 12, 18,
            18, 12, 17,
            17, 12, 16,
            16, 12, 13,
            16, 13, 15,
            15, 13, 14

    };
    public static final short[] cullaIndices = new short[]{
            0, 11, 9,
            9, 11, 20,
            9, 20, 8,
            8, 20, 19,
            8, 19, 7,
            7, 19, 18,
            7, 18, 6,
            6, 18, 17,
            6, 17, 5,
            5, 17, 16,
            5, 16, 4,
            4, 16, 15,
            4, 15, 3,
            3, 15, 14
    };

    //bucketFrame
    private static class BucketFrame {
        double[] O; // origine: centro tagliente
        double[] R; // asse right
        double[] B; // asse back, verso pivot
        double[] U; // asse up

        BucketFrame(double[] O, double[] R, double[] B, double[] U) {
            this.O = O;
            this.R = R;
            this.B = B;
            this.U = U;
        }

        double[] point(double right, double back, double up) {
            return add(add(add(O, scale3(R, right)), scale3(B, back)), scale3(U, up));
        }
    }

    //costruzione frame locale
    private static BucketFrame buildBucketFrameRototilt() {
        double[] O = scale3(add(bucketLeftCoord, bucketRightCoord), 0.5);

        double[] R = normalize(sub(bucketRightCoord, bucketLeftCoord));

        double[] backRaw = sub(coordPivoTilt, O);
        double[] B = projectOnPlane(backRaw, R);
        B = normalize(B);

        if (norm(B) < 1e-6) {
            B = new double[]{0.0, 1.0, 0.0};
            B = projectOnPlane(B, R);
            B = normalize(B);
        }

        // QUI invertiamo la mano della terna
        double[] U = normalize(cross(B, R));

        if (norm(U) < 1e-6) {
            U = new double[]{0.0, 0.0, 1.0};
        }

        // ripulitura coerente con la nuova mano
        B = normalize(cross(R, U));
        U = normalize(cross(B, R));

        return new BucketFrame(O, R, B, U);
    }

    //utilities
    private static double[] sub(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static double[] add(double[] a, double[] b) {
        return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private static double[] scale3(double[] v, double s) {
        return new double[]{v[0] * s, v[1] * s, v[2] * s};
    }

    private static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static double norm(double[] v) {
        return Math.sqrt(dot(v, v));
    }

    private static double[] normalize(double[] v) {
        double n = norm(v);
        if (n < 1e-9) return new double[]{0.0, 0.0, 0.0};
        return new double[]{v[0] / n, v[1] / n, v[2] / n};
    }

    private static double[] cross(double[] a, double[] b) {
        return new double[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    private static double[] projectOnPlane(double[] v, double[] normal) {
        double d = dot(v, normal);
        return sub(v, scale3(normal, d));
    }


    //fiancata
    private static double[] dirBU(BucketFrame f, double alphaDeg) {
        double a = Math.toRadians(alphaDeg);

        // convenzione locale:
        //   0°   = avanti = -B
        //   90°  = su     = +U
        //   180° = dietro = +B
        double[] dir = add(
                scale3(f.B, -Math.cos(a)),
                scale3(f.U, Math.sin(a))
        );

        return normalize(dir);
    }

    private static double[] moveBU(double[] origin, BucketFrame f, double alphaDeg, double len) {
        return add(origin, scale3(dirBU(f, alphaDeg), len));
    }
}
