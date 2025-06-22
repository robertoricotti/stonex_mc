package gui.my_opengl.wheel;


import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;
import static packexcalib.exca.ExcavatorLib.correctBucket;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
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
import packexcalib.surfcreator.DistToPoint;

public class My_Wheel {
    static Point3DF P1_sx, P2_sx, P3_sx, P4_sx, P5_sx, P6_sx, P7_sx, P8_sx;
    static Point3DF P1_dx, P2_dx, P3_dx, P4_dx, P5_dx, P6_dx, P7_dx, P8_dx;
    static Point3DF SPIG_L, SPIG_C, SPIG_R, SPIG_LB, SPIG_CB, SPIG_RB;
    static Point3DF fwF, bwF, ltF, rtF, start,PIVOT_IN,PIVOT_OUT;
    static double[] fw, lt, rt, bw;

    private static Point3DF[] fiancataSX() {
        double flatTop;
        double flatLen;
        double[] p1;
        double[] p2;
        double[] p3;
        double[] p4;
        double[] p5;
        double[] p6;
        double[] p7;
        double[] p8;
        double base, hMax;
        double[] splA;
        double[] spcA;
        double[] sprA;
        double[] splB;
        double[] spcB;
        double[] sprB;
        double[] pivot_in,pivot_out;

        pivot_in=ExcavatorLib.coordST;
        pivot_out=Exca_Quaternion.endPoint(pivot_in,correctBucket+180,Deg_Boom_Roll,0.1,hdt_BOOM);

        flatLen = 0.9 * DataSaved.L_Bucket;
        flatTop = flatLen * 0.6;
        p1 = bucketLeftCoord;
        p2 = Exca_Quaternion.endPoint(bucketLeftCoord, correctBucket + 180, Deg_Boom_Roll, flatLen, hdt_BOOM);
        p3 = Exca_Quaternion.endPoint(p1, correctBucket + 120, Deg_Boom_Roll, (DataSaved.L_Bucket * 0.9), hdt_BOOM);


        p4 = Exca_Quaternion.endPoint(p3, correctBucket + 185, 0, flatTop, hdt_BOOM);
        base = DistToPoint.dist3D(p4, p2);
        hMax = base * 0.4;

        Polyline2DArc.SegmentData[] segments = Polyline2DArc.calculateArcSegmentsInv(base, hMax);
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


        p8 = Exca_Quaternion.endPoint(p2, correctBucket + 90 + (-d1), Deg_Boom_Roll, L1, hdt_BOOM);
        p6 = Exca_Quaternion.endPoint(p8, correctBucket + 90 + (-d2), Deg_Boom_Roll, L2, hdt_BOOM);
        p5 = Exca_Quaternion.endPoint(p6, correctBucket + 90 + (-d3), Deg_Boom_Roll, L3, hdt_BOOM);
        p7 = Exca_Quaternion.endPoint(p5, correctBucket + 90 + (-d4), Deg_Boom_Roll, L4, hdt_BOOM);

        splA = Exca_Quaternion.endPoint(bucketLeftCoord, 90, 0, 0.05, hdt_BOOM + 0);
        spcA = Exca_Quaternion.endPoint(bucketCoord, 90, 0, 0.05, hdt_BOOM + 0);
        sprA = Exca_Quaternion.endPoint(bucketRightCoord, 90, 0, 0.05, hdt_BOOM + 0);
        splB = Exca_Quaternion.endPoint(bucketLeftCoord, -90, 0, 0.05, hdt_BOOM + 0);
        spcB = Exca_Quaternion.endPoint(bucketCoord, -90, 0, 0.05, hdt_BOOM + 0);
        sprB = Exca_Quaternion.endPoint(bucketRightCoord, -90, 0, 0.05, hdt_BOOM + 0);
        double[] pos = new double[0];


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

        fw = Exca_Quaternion.endPoint(pos, 0, 0, 50, hdt_BOOM);
        bw = Exca_Quaternion.endPoint(pos, 0, 0, 15, hdt_BOOM + 180);
        lt = Exca_Quaternion.endPoint(pos, Deg_Boom_Roll, 0, 10, hdt_BOOM + 270);
        rt = Exca_Quaternion.endPoint(pos, -Deg_Boom_Roll, 0, 10, hdt_BOOM + 90);
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

        P1_sx = pTransform(p1, DataSaved.glL_AnchorView, scale);
        P2_sx = pTransform(p2, DataSaved.glL_AnchorView, scale);
        P3_sx = pTransform(p3, DataSaved.glL_AnchorView, scale);
        P4_sx = pTransform(p4, DataSaved.glL_AnchorView, scale);
        P5_sx = pTransform(p5, DataSaved.glL_AnchorView, scale);
        P6_sx = pTransform(p6, DataSaved.glL_AnchorView, scale);
        P7_sx = pTransform(p7, DataSaved.glL_AnchorView, scale);
        P8_sx = pTransform(p8, DataSaved.glL_AnchorView, scale);
        SPIG_L = pTransform(splA, DataSaved.glL_AnchorView, scale);
        SPIG_C = pTransform(spcA, DataSaved.glL_AnchorView, scale);
        SPIG_R = pTransform(sprA, DataSaved.glL_AnchorView, scale);
        SPIG_LB = pTransform(splB, DataSaved.glL_AnchorView, scale);
        SPIG_CB = pTransform(spcB, DataSaved.glL_AnchorView, scale);
        SPIG_RB = pTransform(sprB, DataSaved.glL_AnchorView, scale);
        PIVOT_IN=pTransform(pivot_in,DataSaved.glL_AnchorView,scale);
        PIVOT_OUT=pTransform(pivot_out,DataSaved.glL_AnchorView,scale);
        return new Point3DF[]{
                //sequenza 1,3,4,7,5,6,8,2,1
                P1_sx, P3_sx, P7_sx, P7_sx, P5_sx, P6_sx, P8_sx, P2_sx, P1_sx
        };
    }

    private static Point3DF[] fiancataDX() {
        double flatLen;
        double base;//distanza p2>p4
        double hMax;//base/2
        double[] p1;//left
        double[] p2;//left fine flat basso
        double[] p3;//top left
        double[] p4;//top left fine flat
        double[] p5;//hmax superiore
        double[] p6;//hmax inferiore
        double[] p7;//hmax *0.7 superiore
        double[] p8;//hmax *0.7 inferiore
        double flatTop;

        flatLen = 0.9 * DataSaved.L_Bucket;
        flatTop = flatLen * 0.6;
        p1 = bucketRightCoord;
        p2 = Exca_Quaternion.endPoint(bucketRightCoord, correctBucket + 180, Deg_Boom_Roll, flatLen, hdt_BOOM);
        p3 = Exca_Quaternion.endPoint(p1, correctBucket + 120, Deg_Boom_Roll, (DataSaved.L_Bucket * 0.9), hdt_BOOM);
        p4 = Exca_Quaternion.endPoint(p3, correctBucket + 185, 0, flatTop, hdt_BOOM);
        base = DistToPoint.dist3D(p4, p2);

        hMax = base * 0.4;


        Polyline2DArc.SegmentData[] segments = Polyline2DArc.calculateArcSegmentsInv(base, hMax);
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

        p8 = Exca_Quaternion.endPoint(p2, correctBucket + 90 + (-d1), Deg_Boom_Roll, L1, hdt_BOOM);
        p6 = Exca_Quaternion.endPoint(p8, correctBucket + 90 + (-d2), Deg_Boom_Roll, L2, hdt_BOOM);
        p5 = Exca_Quaternion.endPoint(p6, correctBucket + 90 + (-d3), Deg_Boom_Roll, L3, hdt_BOOM);
        p7 = Exca_Quaternion.endPoint(p5, correctBucket + 90 + (-d4), Deg_Boom_Roll, L4, hdt_BOOM);
        P1_dx = pTransform(p1, DataSaved.glL_AnchorView, scale);
        P2_dx = pTransform(p2, DataSaved.glL_AnchorView, scale);
        P3_dx = pTransform(p3, DataSaved.glL_AnchorView, scale);
        P4_dx = pTransform(p4, DataSaved.glL_AnchorView, scale);
        P5_dx = pTransform(p5, DataSaved.glL_AnchorView, scale);
        P6_dx = pTransform(p6, DataSaved.glL_AnchorView, scale);
        P7_dx = pTransform(p7, DataSaved.glL_AnchorView, scale);
        P8_dx = pTransform(p8, DataSaved.glL_AnchorView, scale);
        return new Point3DF[]{
                //sequenza 1,3,4,7,5,6,8,2,1
                P1_dx, P3_dx, P7_dx, P7_dx, P5_dx, P6_dx, P8_dx, P2_dx, P1_dx
        };

    }

    public static Point3DF[] puntiBenna() {
        return new Point3DF[]{
                //sx
                fiancataSX()[0],//p1
                fiancataSX()[1],//p3
                fiancataSX()[2],//p7
                fiancataSX()[3],//p7
                fiancataSX()[4],
                fiancataSX()[5],
                fiancataSX()[6],
                fiancataSX()[7],//p2
                fiancataSX()[8],

                //dx
                fiancataDX()[0],//9
                fiancataDX()[1],//10
                fiancataDX()[2],//11
                fiancataDX()[3],//12
                fiancataDX()[4],//13
                fiancataDX()[5],//14
                fiancataDX()[6],//15
                fiancataDX()[7],//16
                fiancataDX()[8],//17
                SPIG_C, //18
                SPIG_R, //19
                SPIG_L, //20
                SPIG_CB, //21
                SPIG_RB, //22
                SPIG_LB, //23
                glLinePoint, //24
                glSegmentPoint, //25
                glSegmentEnd, //26
                glLinePunto, //27
                glPuntoTerra, //28
                glTerraPunto, //29
                start,//30
                fwF,//31
                bwF,//32
                rtF,//33
                ltF, //34
                PIVOT_IN,//35
                PIVOT_OUT,//36

        };

    }

    public static final short[] leftSideIndicesW = {
            0, 1, 2,
            0, 2, 4,
            0, 4, 5,
            0, 5, 6,
            0, 6, 7
    };

    public static final short[] rightSideIndicesW = {
            9, 11, 10,
            9, 13, 11,
            9, 14, 13,
            9, 15, 14,
            9, 16, 15


    };
    public static final short[] cullaIndicesW = new short[]{
            0, 8, 17,
            17, 9, 0,
            8, 7, 16,
            16, 17, 8,
            7, 6, 15,
            15, 16, 7,
            6, 5, 14,
            14, 15, 6,
            5, 4, 13,
            13, 14, 5,
            4, 3, 12,
            12, 13, 4,
            3, 2, 11,
            11, 12, 3,
            2, 1, 10,
            10, 11, 2
    };

}
