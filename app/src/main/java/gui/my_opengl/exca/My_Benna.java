package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;
import static packexcalib.exca.ExcavatorLib.coordPivoTilt;
import static packexcalib.exca.ExcavatorLib.coordRotoCenter;
import static packexcalib.exca.ExcavatorLib.coordST;
import static packexcalib.exca.ExcavatorLib.correctBucket;
import static packexcalib.exca.ExcavatorLib.correctDeltaAngle;
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
    static Point3DF DBG_PIVO_TILT;
    static Point3DF DBG_ROTO_TOP;
    static Point3DF DBG_ROTO_CENTER;
    static Point3DF DBG_BUCKET_CENTER;


    public static double[] coneDown, coneUp;
    static Point3DF coneDownF, coneUpF;

    static Point3DF P1_sx, P2_sx, P3_sx, P4_sx, P5_sx, P6_sx, P7_sx, P8_sx, PM1_sx, PM2_sx;
    static Point3DF P1_dx, P2_dx, P3_dx, P4_dx, P5_dx, P6_dx, P7_dx, P8_dx, PM1_dx, PM2_dx;

    static Point3DF PBASE, PBASE_ALTA;
    static Point3DF PBASE_H, PBASE_ALTA_H;
    static Point3DF P_A_Front, P_A_Back, P_A_FF;

    static Point3DF SPIG_L, SPIG_C, SPIG_R, SPIG_LB, SPIG_CB, SPIG_RB;

    public static double[] fw, bw, lt, rt;
    static Point3DF fwF, bwF, ltF, rtF, start;

    public static float larghezza_attacco, raggioPivot;
    public static float altezzaAttacco;
    static double mhdt;

    private static final double EPS = 1e-9;

    // =========================
    // FRAME LOCALI
    // =========================

    private static class BucketFrame {
        double[] O; // origine: centro tagliente / centro benna lato tagliente
        double[] R; // asse right
        double[] B; // asse back (verso retro benna / pivot)
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

    private static class UpperTiltFrame {
        double[] O; // pivot tilt
        double[] R; // asse perno tilt
        double[] F; // avanti gruppo superiore
        double[] U; // up gruppo superiore

        UpperTiltFrame(double[] O, double[] R, double[] F, double[] U) {
            this.O = O;
            this.R = R;
            this.F = F;
            this.U = U;
        }

        double[] point(double right, double forward, double up) {
            return add(add(add(O, scale3(R, right)), scale3(F, forward)), scale3(U, up));
        }
    }

    // =========================
    // FIANCATA SX
    // =========================

    private static Point3DF[] fiancataSX() {
        mhdt = hdt_BOOM;

        double[] splA;
        double[] spcA;
        double[] sprA;
        double[] splB;
        double[] spcB;
        double[] sprB;

        double flatLen;
        double base;
        double hMax;
        double segPM1;
        double segPM2;
        double flatTop;

        double[] p1;
        double[] p2;
        double[] p3;
        double[] p4;
        double[] p5;
        double[] p6;
        double[] p7;
        double[] p8;
        double[] pm1;
        double[] pm2;

        if (DataSaved.lrTilt == 0) {
            // NO TILT
            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.L_Bucket;
            flatTop = Math.max(0.18, flatLen * 0.8);

            p1 = bucketLeftCoord;
            p2 = Exca_Quaternion.endPoint(bucketLeftCoord, correctBucket + 90 + DataSaved.flat, Deg_Boom_Roll, flatLen, mhdt);
            p3 = Exca_Quaternion.endPoint(p1, correctBucket + 180, Deg_Boom_Roll, DataSaved.L_Bucket * 0.9, mhdt);
            p4 = Exca_Quaternion.endPoint(p3, correctBucket + 90 - (DataSaved.flat * 0.5), 0, flatTop, mhdt);

            base = DistToPoint.dist3D(p4, p2);
            hMax = base * 0.4;

            segPM1 = DistToPoint.dist3D(p1, p3) * 0.15;
            segPM2 = DistToPoint.dist3D(p1, p3) * 0.25;

            pm1 = Exca_Quaternion.endPoint(p3, correctBucket + 30, Deg_Boom_Roll, segPM1, mhdt);
            pm2 = Exca_Quaternion.endPoint(pm1, correctBucket + 12, Deg_Boom_Roll, segPM2, mhdt);

            Polyline2DArc.SegmentData[] segments = Polyline2DArc.calculateArcSegments(base, hMax);

            p8 = Exca_Quaternion.endPoint(p2, correctBucket + 90 + (90 - segments[0].angleDegrees), Deg_Boom_Roll, segments[0].length, mhdt);
            p6 = Exca_Quaternion.endPoint(p8, correctBucket + 90 + (90 - segments[1].angleDegrees), Deg_Boom_Roll, segments[1].length, mhdt);
            p5 = Exca_Quaternion.endPoint(p6, correctBucket + 90 + (90 - segments[2].angleDegrees), Deg_Boom_Roll, segments[2].length, mhdt);
            p7 = Exca_Quaternion.endPoint(p5, correctBucket + 90 + (90 - segments[3].angleDegrees), Deg_Boom_Roll, segments[3].length, mhdt);

            splA = Exca_Quaternion.endPoint(bucketLeftCoord, 90, 0, 0.05, mhdt);
            spcA = Exca_Quaternion.endPoint(bucketCoord, 90, 0, 0.05, mhdt);
            sprA = Exca_Quaternion.endPoint(bucketRightCoord, 90, 0, 0.05, mhdt);

            splB = Exca_Quaternion.endPoint(bucketLeftCoord, -90, 0, 0.05, mhdt);
            spcB = Exca_Quaternion.endPoint(bucketCoord, -90, 0, 0.05, mhdt);
            sprB = Exca_Quaternion.endPoint(bucketRightCoord, -90, 0, 0.05, mhdt);

        } else if (DataSaved.isTiltRotator!=1) {
            // TILT NORMALE
            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.piccolaBucket;
            flatTop = Math.max(0.18, flatLen * 0.8);

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

            p8 = Exca_Quaternion.endPoint(p2, correctWTilt + 90 + (90 - segmentsT[0].angleDegrees), correctTilt, segmentsT[0].length, mhdt + yawSensor);
            p6 = Exca_Quaternion.endPoint(p8, correctWTilt + 90 + (90 - segmentsT[1].angleDegrees), correctTilt, segmentsT[1].length, mhdt + yawSensor);
            p5 = Exca_Quaternion.endPoint(p6, correctWTilt + 90 + (90 - segmentsT[2].angleDegrees), correctTilt, segmentsT[2].length, mhdt + yawSensor);
            p7 = Exca_Quaternion.endPoint(p5, correctWTilt + 90 + (90 - segmentsT[3].angleDegrees), correctTilt, segmentsT[3].length, mhdt + yawSensor);

            splA = Exca_Quaternion.endPoint(bucketLeftCoord, 90, 0, 0.05, mhdt + yawSensor);
            spcA = Exca_Quaternion.endPoint(bucketCoord, 90, 0, 0.05, mhdt + yawSensor);
            sprA = Exca_Quaternion.endPoint(bucketRightCoord, 90, 0, 0.05, mhdt + yawSensor);

            splB = Exca_Quaternion.endPoint(bucketLeftCoord, -90, 0, 0.05, mhdt + yawSensor);
            spcB = Exca_Quaternion.endPoint(bucketCoord, -90, 0, 0.05, mhdt + yawSensor);
            sprB = Exca_Quaternion.endPoint(bucketRightCoord, -90, 0, 0.05, mhdt + yawSensor);

        } else {
            // TILTROTATOR
            BucketFrame f = buildBucketFrameRototilt();

            double halfW = DistToPoint.dist3D(bucketLeftCoord, bucketRightCoord) * 0.5;
            double depth = DataSaved.piccolaBucket * 0.9;

            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.piccolaBucket;
            flatTop = Math.max(0.18, flatLen * 0.8);

            p1 = f.point(-halfW, 0.0, 0.0);
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

            p8 = moveBU(p2, f, 180.0 - segmentsT[0].angleDegrees, segmentsT[0].length);
            p6 = moveBU(p8, f, 180.0 - segmentsT[1].angleDegrees, segmentsT[1].length);
            p5 = moveBU(p6, f, 180.0 - segmentsT[2].angleDegrees, segmentsT[2].length);
            p7 = moveBU(p5, f, 180.0 - segmentsT[3].angleDegrees, segmentsT[3].length);

            double[] rightEdge = f.point(+halfW, 0.0, 0.0);

            double coneOffsetUp = 0.02;
            double coneOffsetDown = 0.10;

            splA = new double[]{p1[0], p1[1], p1[2] + coneOffsetUp};
            spcA = new double[]{f.O[0], f.O[1], f.O[2] + coneOffsetUp};
            sprA = new double[]{rightEdge[0], rightEdge[1], rightEdge[2] + coneOffsetUp};

            splB = new double[]{p1[0], p1[1], p1[2] - coneOffsetDown};
            spcB = new double[]{f.O[0], f.O[1], f.O[2] - coneOffsetDown};
            sprB = new double[]{rightEdge[0], rightEdge[1], rightEdge[2] - coneOffsetDown};
        }

        double[] pos = bucketCoord;
        switch (DataSaved.bucketEdge) {
            case -1:
                pos = bucketLeftCoord;
                break;
            case 0:
                pos = bucketCoord;
                break;
            case 1:
                pos = bucketRightCoord;
                break;
        }

        double coneLen = 10.0;
        coneDown = new double[]{pos[0], pos[1], pos[2] - coneLen};
        coneUp = new double[]{pos[0], pos[1], pos[2] + coneLen};

        if (DataSaved.lrTilt == 0) {
            fw = Exca_Quaternion.endPoint(pos, 0, 0, 50, mhdt);
            bw = Exca_Quaternion.endPoint(pos, 0, 0, 15, mhdt + 180);
            lt = Exca_Quaternion.endPoint(pos, Deg_Boom_Roll, 0, 10, mhdt + 270);
            rt = Exca_Quaternion.endPoint(pos, -Deg_Boom_Roll, 0, 10, mhdt + 90);

        } else if (DataSaved.isTiltRotator!=1) {
            fw = Exca_Quaternion.endPoint(pos, 0, 0, 50, mhdt + yawSensor);
            bw = Exca_Quaternion.endPoint(pos, 0, 0, 15, mhdt + yawSensor + 180);
            lt = Exca_Quaternion.endPoint(pos, correctTilt, 0, 10, mhdt + yawSensor + 270);
            rt = Exca_Quaternion.endPoint(pos, -correctTilt, 0, 10, mhdt + yawSensor + 90);

        } else {
            // nel rototilt la croce resta assoluta per debug visivo
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
                        case -1: {
                            Point3DF pbuck = new Point3DF(
                                    (float) (bucketLeftCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (bucketLeftCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (bucketLeftCoord[2] - DataSaved.glL_AnchorView[2]) * scale);
                            Point3DF pline = new Point3DF(
                                    (float) (DataSaved.nearestPoint.getX() - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (DataSaved.nearestPoint.getY() - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (DataSaved.nearestPoint.getZ() - DataSaved.glL_AnchorView[2]) * scale);
                            glLinePunto = new Point3DF(pbuck.getX(), pbuck.getY(), pbuck.getZ());
                            glPuntoTerra = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glTerraPunto = pline;
                            break;
                        }
                        case 0: {
                            Point3DF pbuck = new Point3DF(
                                    (float) (bucketCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (bucketCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (bucketCoord[2] - DataSaved.glL_AnchorView[2]) * scale);
                            Point3DF pline = new Point3DF(
                                    (float) (DataSaved.nearestPoint.getX() - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (DataSaved.nearestPoint.getY() - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (DataSaved.nearestPoint.getZ() - DataSaved.glL_AnchorView[2]) * scale);
                            glLinePunto = new Point3DF(pbuck.getX(), pbuck.getY(), pbuck.getZ());
                            glPuntoTerra = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glTerraPunto = pline;
                            break;
                        }
                        case 1: {
                            Point3DF pbuck = new Point3DF(
                                    (float) (bucketRightCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (bucketRightCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (bucketRightCoord[2] - DataSaved.glL_AnchorView[2]) * scale);
                            Point3DF pline = new Point3DF(
                                    (float) (DataSaved.nearestPoint.getX() - DataSaved.glL_AnchorView[0]) * scale,
                                    (float) (DataSaved.nearestPoint.getY() - DataSaved.glL_AnchorView[1]) * scale,
                                    (float) (DataSaved.nearestPoint.getZ() - DataSaved.glL_AnchorView[2]) * scale);
                            glLinePunto = new Point3DF(pbuck.getX(), pbuck.getY(), pbuck.getZ());
                            glPuntoTerra = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glTerraPunto = pline;
                            break;
                        }
                    }
                } else {
                    DataSaved.isAutoSnap = 0;
                }
                break;

            case 2:
                if (DataSaved.filteredPolylines != null && !DataSaved.filteredPolylines.isEmpty()) {
                    switch (DataSaved.bucketEdge) {
                        case -1: {
                            Point3DF pbuck = pTransform(bucketLeftCoord, DataSaved.glL_AnchorView, scale);
                            Point3D p = getProjectedPointOnSegment3D(
                                    new Point3D(bucketLeftCoord[0], bucketLeftCoord[1], bucketLeftCoord[2]),
                                    DataSaved.nearestSegment.getStart(), DataSaved.nearestSegment.getEnd());
                            Point3DF pline = pTransform(new double[]{p.getX(), p.getY(), p.getZ()}, DataSaved.glL_AnchorView, scale);
                            glLinePoint = pbuck;
                            glSegmentPoint = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glSegmentEnd = pline;
                            orientamentoFreccia = My_LocationCalc.calcBearingXY(bucketLeftCoord[0], bucketLeftCoord[1], p.getX(), p.getY());
                            break;
                        }
                        case 0: {
                            Point3DF pbuck = pTransform(bucketCoord, DataSaved.glL_AnchorView, scale);
                            Point3D p = getProjectedPointOnSegment3D(
                                    new Point3D(bucketCoord[0], bucketCoord[1], bucketCoord[2]),
                                    DataSaved.nearestSegment.getStart(), DataSaved.nearestSegment.getEnd());
                            Point3DF pline = pTransform(new double[]{p.getX(), p.getY(), p.getZ()}, DataSaved.glL_AnchorView, scale);
                            glLinePoint = pbuck;
                            glSegmentPoint = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glSegmentEnd = pline;
                            orientamentoFreccia = My_LocationCalc.calcBearingXY(bucketCoord[0], bucketCoord[1], p.getX(), p.getY());
                            break;
                        }
                        case 1: {
                            Point3DF pbuck = pTransform(bucketRightCoord, DataSaved.glL_AnchorView, scale);
                            Point3D p = getProjectedPointOnSegment3D(
                                    new Point3D(bucketRightCoord[0], bucketRightCoord[1], bucketRightCoord[2]),
                                    DataSaved.nearestSegment.getStart(), DataSaved.nearestSegment.getEnd());
                            Point3DF pline = pTransform(new double[]{p.getX(), p.getY(), p.getZ()}, DataSaved.glL_AnchorView, scale);
                            glLinePoint = pbuck;
                            glSegmentPoint = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glSegmentEnd = pline;
                            orientamentoFreccia = My_LocationCalc.calcBearingXY(bucketRightCoord[0], bucketRightCoord[1], p.getX(), p.getY());
                            break;
                        }
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

        DBG_PIVO_TILT   = pTransform(coordPivoTilt, DataSaved.glL_AnchorView, scale);
        DBG_ROTO_TOP    = pTransform(ExcavatorLib.coordRotoTop, DataSaved.glL_AnchorView, scale);
        DBG_ROTO_CENTER = pTransform(ExcavatorLib.coordRotoCenter, DataSaved.glL_AnchorView, scale);
        DBG_BUCKET_CENTER = pTransform(bucketCoord, DataSaved.glL_AnchorView, scale);

        return new Point3DF[]{
                P1_sx, PM2_sx, PM1_sx, P3_sx, P4_sx, P7_sx, P5_sx, P6_sx, P8_sx, P2_sx, P1_sx
        };
    }

    // =========================
    // FIANCATA DX
    // =========================

    private static Point3DF[] fiancataDX() {
        double flatLen;
        double base;
        double hMax;
        double segPM1;
        double segPM2;
        double flatTop;

        double[] p1;
        double[] p2;
        double[] p3;
        double[] p4;
        double[] p5;
        double[] p6;
        double[] p7;
        double[] p8;
        double[] pm1;
        double[] pm2;

        if (DataSaved.lrTilt == 0) {
            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.L_Bucket;
            flatTop = Math.max(0.18, flatLen * 0.8);

            p1 = bucketRightCoord;
            p2 = Exca_Quaternion.endPoint(p1, correctBucket + 90 + DataSaved.flat, Deg_Boom_Roll, flatLen, mhdt);
            p3 = Exca_Quaternion.endPoint(p1, correctBucket + 180, Deg_Boom_Roll, DataSaved.L_Bucket * 0.9, mhdt);
            p4 = Exca_Quaternion.endPoint(p3, correctBucket + 90 - (DataSaved.flat * 0.5), 0, flatTop, mhdt);

            base = DistToPoint.dist3D(p4, p2);
            hMax = base * 0.4;

            segPM1 = DistToPoint.dist3D(p1, p3) * 0.15;
            segPM2 = DistToPoint.dist3D(p1, p3) * 0.25;

            pm1 = Exca_Quaternion.endPoint(p3, correctBucket + 30, Deg_Boom_Roll, segPM1, mhdt);
            pm2 = Exca_Quaternion.endPoint(pm1, correctBucket + 12, Deg_Boom_Roll, segPM2, mhdt);

            Polyline2DArc.SegmentData[] segments = Polyline2DArc.calculateArcSegments(base, hMax);

            p8 = Exca_Quaternion.endPoint(p2, correctBucket + 90 + (90 - segments[0].angleDegrees), Deg_Boom_Roll, segments[0].length, mhdt);
            p6 = Exca_Quaternion.endPoint(p8, correctBucket + 90 + (90 - segments[1].angleDegrees), Deg_Boom_Roll, segments[1].length, mhdt);
            p5 = Exca_Quaternion.endPoint(p6, correctBucket + 90 + (90 - segments[2].angleDegrees), Deg_Boom_Roll, segments[2].length, mhdt);
            p7 = Exca_Quaternion.endPoint(p5, correctBucket + 90 + (90 - segments[3].angleDegrees), Deg_Boom_Roll, segments[3].length, mhdt);

        } else if (DataSaved.isTiltRotator!=1) {
            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.piccolaBucket;
            flatTop = Math.max(0.18, flatLen * 0.8);

            p1 = bucketRightCoord;
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

            p8 = Exca_Quaternion.endPoint(p2, correctWTilt + 90 + (90 - segmentsT[0].angleDegrees), correctTilt, segmentsT[0].length, mhdt + yawSensor);
            p6 = Exca_Quaternion.endPoint(p8, correctWTilt + 90 + (90 - segmentsT[1].angleDegrees), correctTilt, segmentsT[1].length, mhdt + yawSensor);
            p5 = Exca_Quaternion.endPoint(p6, correctWTilt + 90 + (90 - segmentsT[2].angleDegrees), correctTilt, segmentsT[2].length, mhdt + yawSensor);
            p7 = Exca_Quaternion.endPoint(p5, correctWTilt + 90 + (90 - segmentsT[3].angleDegrees), correctTilt, segmentsT[3].length, mhdt + yawSensor);

        } else {
            BucketFrame f = buildBucketFrameRototilt();

            double halfW = DistToPoint.dist3D(bucketLeftCoord, bucketRightCoord) * 0.5;
            double depth = DataSaved.piccolaBucket * 0.9;

            flatLen = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.piccolaBucket;
            flatTop = Math.max(0.18, flatLen * 0.8);

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

            p8 = moveBU(p2, f, 180.0 - segmentsT[0].angleDegrees, segmentsT[0].length);
            p6 = moveBU(p8, f, 180.0 - segmentsT[1].angleDegrees, segmentsT[1].length);
            p5 = moveBU(p6, f, 180.0 - segmentsT[2].angleDegrees, segmentsT[2].length);
            p7 = moveBU(p5, f, 180.0 - segmentsT[3].angleDegrees, segmentsT[3].length);
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
                P1_dx, PM2_dx, PM1_dx, P3_dx, P4_dx, P7_dx, P5_dx, P6_dx, P8_dx, P2_dx, P1_dx
        };
    }

    // =========================
    // ATTACCO / TILTROTATOR
    // =========================

    public static float[] attacco() {
        larghezza_attacco = (float) (Math.max(0.15, Math.min((DataSaved.L_Stick * 0.13f), 0.45)) * scale);

        double altezza;
        double[] pmB;
        double[] pmA;
        double[] paFront;
        double[] paBack;
        double[] paFrontFront;
        double[] pmBH;
        double[] pmAH;

        if (DataSaved.lrTilt == 0) {
            altezza = DataSaved.L_Bucket - (DataSaved.L_Bucket * 0.9);
            altezzaAttacco = (float) (altezza * scale);
            raggioPivot = (float) Math.max(0.05, Math.min(DataSaved.L_Bucket - (DataSaved.L_Bucket * 0.92), 0.10));

            pmB = Exca_Quaternion.endPoint(bucketCoord, correctBucket + 180, Deg_Boom_Roll, DataSaved.L_Bucket * 0.9, mhdt);
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

        } else if (DataSaved.isTiltRotator!=1) {
            altezza = DataSaved.L_Bucket - (DataSaved.piccolaBucket * 0.95) - DataSaved.L_Tilt;
            altezzaAttacco = (float) (altezza * scale);

            pmB = Exca_Quaternion.endPoint(bucketCoord, correctWTilt + 180, correctTilt, DataSaved.piccolaBucket * 0.9, mhdt + yawSensor);
            pmA = Exca_Quaternion.endPoint(pmB, correctWTilt + 180 - (DataSaved.flat * 0.5), correctTilt, altezza, mhdt + yawSensor);

            paFront = coordPivoTilt;
            paBack = Exca_Quaternion.endPoint(coordPivoTilt, correctDeltaAngle + 90, Deg_Boom_Roll, larghezza_attacco * 0.5, mhdt);
            paFrontFront = Exca_Quaternion.endPoint(coordPivoTilt, correctDeltaAngle + 270, Deg_Boom_Roll, larghezza_attacco * 0.5, mhdt);

            pmAH = coordST;
            pmBH = Exca_Quaternion.endPoint(coordST, correctDeltaAngle, Deg_Boom_Roll, DataSaved.L_Tilt, mhdt);

            raggioPivot = (float) (DistToPoint.dist3D(pmA, pmBH) * scale);

            PBASE = pTransform(pmB, DataSaved.glL_AnchorView, scale);
            PBASE_ALTA = pTransform(pmA, DataSaved.glL_AnchorView, scale);
            P_A_Front = pTransform(paFront, DataSaved.glL_AnchorView, scale);
            P_A_Back = pTransform(paBack, DataSaved.glL_AnchorView, scale);
            P_A_FF = pTransform(paFrontFront, DataSaved.glL_AnchorView, scale);
            PBASE_ALTA_H = pTransform(pmAH, DataSaved.glL_AnchorView, scale);
            PBASE_H = pTransform(pmBH, DataSaved.glL_AnchorView, scale);

        } else {
            // TILTROTATOR GRAFICO MIGLIORATO
            // - staffa superiore aperta verso l'alto
            // - perno più piccolo
            // - collegamento inferiore più corto

            BucketFrame lower = buildBucketFrameRototilt();
            UpperTiltFrame upper = buildUpperTiltFrame();

            // proporzioni più compatte
            double visualWidth = Math.max(0.12, Math.min(DataSaved.L_Stick * 0.07, 0.26));
            double pinHalf = visualWidth * 0.55;
            double upperRise = visualWidth * 0.42;
            double upperNarrow = visualWidth * 0.30;
            double lowerDrop = visualWidth * 0.36;
            double lowerNarrow = visualWidth * 0.24;

            // questi due restano come punti principali di collegamento
            pmAH = coordST;
            pmBH = coordPivoTilt;

            // asse perno superiore: piccolo e realistico
            paFront = coordPivoTilt;
            paBack = add(coordPivoTilt, scale3(upper.R, pinHalf));
            paFrontFront = add(coordPivoTilt, scale3(upper.R, -pinHalf));

            // attacco inferiore verso la benna: più corto e meno aggressivo
            double lowerBack = DataSaved.piccolaBucket * 0.55;
            pmB = lower.point(0.0, lowerBack, 0.0);
            pmA = lower.point(0.0, lowerBack, visualWidth * 0.30);

            altezza = DistToPoint.dist3D(pmA, pmB);
            altezzaAttacco = (float) (altezza * scale);

            // raggio perno più piccolo
            raggioPivot = (float) (visualWidth * 0.18 * scale);

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

    // =========================
    // PUNTI BENNA
    // =========================

    public static Point3DF[] puntiBenna() {
        Point3DF[] sx = fiancataSX();
        Point3DF[] dx = fiancataDX();

        return new Point3DF[]{
                sx[0], sx[1], sx[2], sx[3], sx[4], sx[5], sx[6], sx[7], sx[8], sx[9], sx[10],
                dx[0], dx[1], dx[2], dx[3], dx[4], dx[5], dx[6], dx[7], dx[8], dx[9], dx[10],
                SPIG_C, SPIG_R, SPIG_L, SPIG_CB, SPIG_RB, SPIG_LB,
                glLinePoint, glSegmentPoint, glSegmentEnd, glLinePunto, glPuntoTerra, glTerraPunto,
                // DEBUG ROTOTILT
                DBG_PIVO_TILT,     // 34
                DBG_ROTO_TOP,      // 35
                DBG_ROTO_CENTER,   // 36
                DBG_BUCKET_CENTER  // 37
        };
    }

    // =========================
    // INDICI MESH
    // =========================

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
            11, 12, 20,
            20, 12, 19,
            19, 12, 18,
            18, 12, 17,
            17, 12, 16,
            16, 12, 13,
            16, 13, 15,
            15, 13, 14
    };

    public static final short[] cullaIndices = {
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

    // =========================
    // FRAME COSTRUZIONE
    // =========================

    private static UpperTiltFrame buildUpperTiltFrame() {
        org.apache.commons.math3.complex.Quaternion qTop =
                Exca_Quaternion.getOrientationQuaternion(
                        correctDeltaAngle,
                        Deg_Boom_Roll,
                        hdt_BOOM
                );

        double[] F = normalize(Exca_Quaternion.rotateVector(qTop, 0, 1, 0));
        double[] R = normalize(Exca_Quaternion.rotateVector(qTop, 1, 0, 0));
        double[] U = normalize(Exca_Quaternion.rotateVector(qTop, 0, 0, 1));

        U = normalize(cross(R, F));
        R = normalize(cross(F, U));

        return new UpperTiltFrame(coordPivoTilt, R, F, U);
    }

    private static BucketFrame buildBucketFrameRototilt() {
        double[] O = scale3(add(bucketLeftCoord, bucketRightCoord), 0.5);
        double[] R = normalize(sub(bucketRightCoord, bucketLeftCoord));

        double[] backRaw = sub(coordPivoTilt, O);
        double[] B = projectOnPlane(backRaw, R);
        B = normalize(B);

        if (norm(B) < 1e-6) {
            double[] worldUp = new double[]{0.0, 0.0, 1.0};
            B = projectOnPlane(worldUp, R);
            B = normalize(B);
        }

        double[] U = normalize(cross(B, R));

        if (norm(U) < 1e-6) {
            U = new double[]{0.0, 0.0, 1.0};
        }

        B = normalize(cross(R, U));
        U = normalize(cross(B, R));

        return new BucketFrame(O, R, B, U);
    }

    // =========================
    // UTILITIES VETTORI
    // =========================

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
        if (n < EPS) return new double[]{0.0, 0.0, 0.0};
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

    // =========================
    // PROFILO BENNA LOCALE
    // =========================

    private static double[] dirBU(BucketFrame f, double alphaDeg) {
        double a = Math.toRadians(alphaDeg);

        // 0° = avanti = -B
        // 90° = su = +U
        // 180° = dietro = +B
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