package gui.my_opengl.dozer;

import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;
import static packexcalib.exca.ExcavatorLib.correctPitch;
import static packexcalib.exca.ExcavatorLib.correctRoll;
import static packexcalib.exca.ExcavatorLib.hdt_LAMA;
import static packexcalib.exca.ExcavatorLib.startXYZ;
import static services.TriangleService.getProjectedPointOnSegment3D;
import static services.TriangleService.*;


import dxf.Point3D;
import gui.my_opengl.MyGLRenderer;
import gui.my_opengl.Point3DF;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;

public class My_Lama {
    private static float rs() {
        return MyGLRenderer.currentRenderScale();
    }

    static Point3DF fwF, bwF, ltF, rtF, start;
    static Point3DF P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, C1, C2, A1A, A1B, A2A, A2B, CAP1, CAP2, COVER_L, COVER_R,
            SPIG_L, SPIG_C, SPIG_R, SPIG_LB, SPIG_CB, SPIG_RB;

    public static Point3DF[] puntiLama() {
        double[] p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, c1, c2, ant1_a, ant1_b, ant2_a, ant2_b, cap1, cap2, cover_l, cover_r;
        double[] fw, bw, lt, rt, origin = new double[0];
        p0 = bucketCoord;
        p1 = bucketRightCoord;
        p9 = bucketLeftCoord;

        p4 = Exca_Quaternion.endPoint(p1, correctPitch + 90, correctRoll, DataSaved.altezzaLama+DataSaved.usuraLamaCX, hdt_LAMA);
        p5 = Exca_Quaternion.endPoint(p0, correctPitch + 90, correctRoll, DataSaved.altezzaLama+DataSaved.usuraLamaCX, hdt_LAMA);
        p6 = Exca_Quaternion.endPoint(p9, correctPitch + 90, correctRoll, DataSaved.altezzaLama+DataSaved.usuraLamaCX, hdt_LAMA);
        p2 = Exca_Quaternion.endPoint(p1, correctPitch + 120, correctRoll, (DataSaved.altezzaLama+DataSaved.usuraLamaCX) * 0.2, hdt_LAMA);
        p8 = Exca_Quaternion.endPoint(p9, correctPitch + 120, correctRoll, (DataSaved.altezzaLama+DataSaved.usuraLamaCX) * 0.2, hdt_LAMA);
        p10 = Exca_Quaternion.endPoint(p1, correctPitch + 160, correctRoll, (DataSaved.altezzaLama+DataSaved.usuraLamaCX) * 0.35, hdt_LAMA);
        p11 = Exca_Quaternion.endPoint(p4, correctPitch + 200, correctRoll, (DataSaved.altezzaLama+DataSaved.usuraLamaCX)* 0.35, hdt_LAMA);
        p12 = Exca_Quaternion.endPoint(p6, correctPitch + 200, correctRoll, (DataSaved.altezzaLama+DataSaved.usuraLamaCX) * 0.35, hdt_LAMA);
        p13 = Exca_Quaternion.endPoint(p9, correctPitch + 160, correctRoll, (DataSaved.altezzaLama+DataSaved.usuraLamaCX) * 0.35, hdt_LAMA);
        p3 = Exca_Quaternion.endPoint(p4, correctPitch - 120, correctRoll, (DataSaved.altezzaLama+DataSaved.usuraLamaCX) * 0.2, hdt_LAMA);
        p7 = Exca_Quaternion.endPoint(p6, correctPitch - 120, correctRoll, (DataSaved.altezzaLama+DataSaved.usuraLamaCX) * 0.2, hdt_LAMA);
        Point3D mC1 = new Point3D(p10[0], p10[1], p10[2]);
        Point3D mC2 = new Point3D(p12[0], p12[1], p12[2]);
        Point3D centro = mC1.interpolate(mC2, 1.0 / 2.0);
        c1 = new double[]{centro.getX(), centro.getY(), centro.getZ()};
        c2 = Exca_Quaternion.endPoint(c1, correctPitch + 180, correctRoll, (DataSaved.altezzaLama+DataSaved.usuraLamaCX) * 0.1, hdt_LAMA);//TODO punto REALE ENZ DOVE PARTE FRAME
        ant1_a = startXYZ;
        ant2_a = Exca_Quaternion.endPoint(startXYZ, -correctRoll, correctPitch, DataSaved.W_Blade_TOT - (DataSaved.W_Blade_LEFT - DataSaved.deltaX) - DataSaved.distBetween, NmeaListener.mch_Orientation);
        ant1_b = Exca_Quaternion.endPoint(ant1_a, correctPitch - 90, correctRoll, DataSaved.altezzaPali, hdt_LAMA);
        cover_l = Exca_Quaternion.endPoint(ant1_b, correctPitch + 90, correctRoll, DataSaved.altezzaPali * 0.35, hdt_LAMA);
        ant2_b = Exca_Quaternion.endPoint(ant2_a, correctPitch - 90, correctRoll, DataSaved.altezzaPali, hdt_LAMA);
        cover_r = Exca_Quaternion.endPoint(ant2_b, correctPitch + 90, correctRoll, DataSaved.altezzaPali * 0.35, hdt_LAMA);
        cap1 = Exca_Quaternion.endPoint(ant1_a, correctPitch + 90, correctRoll, 0.05, hdt_LAMA);
        cap2 = Exca_Quaternion.endPoint(ant2_a, correctPitch + 90, correctRoll, 0.05, hdt_LAMA);

        double[] splA = Exca_Quaternion.endPoint(p9, correctPitch + 90, correctRoll, 0.05, hdt_LAMA);
        double[] spcA = Exca_Quaternion.endPoint(p0, correctPitch + 90, correctRoll, 0.05, hdt_LAMA);
        double[] sprA = Exca_Quaternion.endPoint(p1, correctPitch + 90, correctRoll, 0.05, hdt_LAMA);
        double[] splB = Exca_Quaternion.endPoint(p9, correctPitch - 90, correctRoll, 0.05, hdt_LAMA);
        double[] spcB = Exca_Quaternion.endPoint(p0, correctPitch - 90, correctRoll, 0.05, hdt_LAMA);
        double[] sprB = Exca_Quaternion.endPoint(p1, correctPitch - 90, correctRoll, 0.05, hdt_LAMA);

        switch (DataSaved.bucketEdge) {
            case -1:
                origin = bucketLeftCoord;

                break;
            case 0:
                origin = bucketCoord;


                break;
            case 1:
                origin = bucketRightCoord;

                break;
        }
        switch (DataSaved.isAutoSnap) {
            case 0:

                orientamentoFreccia = 0;
                glLinePoint=new Point3DF(0f,0f,0f);
                 glSegmentPoint=new Point3DF(0f,0f,0f);
                 glSegmentEnd=new Point3DF(0f,0f,0f);
                 glLinePunto=new Point3DF(0f,0f,0f);
                 glPuntoTerra=new Point3DF(0f,0f,0f);
                 glTerraPunto=new Point3DF(0f,0f,0f);

                break;

            case 1:
                orientamentoFreccia = 0;
                if (DataSaved.points != null && !DataSaved.points.isEmpty()) {
                    switch (DataSaved.bucketEdge) {
                        case -1:
                            Point3DF pbuck = new Point3DF((float) (bucketLeftCoord[0] - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (bucketLeftCoord[1] - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (bucketLeftCoord[2] - DataSaved.glL_AnchorView[2]) * rs());
                            Point3DF pline = new Point3DF((float) (DataSaved.nearestPoint.getX() - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (DataSaved.nearestPoint.getY() - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (DataSaved.nearestPoint.getZ() - DataSaved.glL_AnchorView[2]) * rs());
                            glLinePunto = new Point3DF(pbuck.getX(), pbuck.getY(), pbuck.getZ());
                            glPuntoTerra = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glTerraPunto = pline;
                            break;

                        case 0:
                            Point3DF pbuckC = new Point3DF((float) (bucketCoord[0] - DataSaved.glL_AnchorView[0]) *rs(),
                                    (float) (bucketCoord[1] - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (bucketCoord[2] - DataSaved.glL_AnchorView[2]) *rs());
                            Point3DF plineC = new Point3DF((float) (DataSaved.nearestPoint.getX() - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (DataSaved.nearestPoint.getY() - DataSaved.glL_AnchorView[1]) *rs(),
                                    (float) (DataSaved.nearestPoint.getZ() - DataSaved.glL_AnchorView[2]) * rs());
                            glLinePunto = new Point3DF(pbuckC.getX(), pbuckC.getY(), pbuckC.getZ());
                            glPuntoTerra = new Point3DF(pbuckC.getX(), pbuckC.getY(), plineC.getZ());
                            glTerraPunto = plineC;
                            break;

                        case 1:
                            Point3DF pbuckR = new Point3DF((float) (bucketRightCoord[0] - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (bucketRightCoord[1] - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (bucketRightCoord[2] - DataSaved.glL_AnchorView[2]) * rs());
                            Point3DF plineR = new Point3DF((float) (DataSaved.nearestPoint.getX() - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (DataSaved.nearestPoint.getY() - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (DataSaved.nearestPoint.getZ() - DataSaved.glL_AnchorView[2]) * rs());
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
                            Point3DF pbuck = new Point3DF((float) (bucketLeftCoord[0] - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (bucketLeftCoord[1] - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (bucketLeftCoord[2] - DataSaved.glL_AnchorView[2]) * rs());
                            Point3D p = getProjectedPointOnSegment3D(new Point3D(bucketLeftCoord[0], bucketLeftCoord[1], bucketLeftCoord[2]),
                                    DataSaved.nearestSegment.getStart(), DataSaved.nearestSegment.getEnd());
                            Point3DF pline = new Point3DF((float) (p.getX() - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (p.getY() - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (p.getZ() - DataSaved.glL_AnchorView[2]) * rs());
                            glLinePoint = new Point3DF(pbuck.getX(), pbuck.getY(), pbuck.getZ());
                            glSegmentPoint = new Point3DF(pbuck.getX(), pbuck.getY(), pline.getZ());
                            glSegmentEnd = pline;
                            orientamentoFreccia = My_LocationCalc.calcBearingXY(bucketLeftCoord[0], bucketLeftCoord[1], p.getX(), p.getY());

                            break;

                        case 0:

                            Point3DF pbuckC = new Point3DF((float) (bucketCoord[0] - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (bucketCoord[1] - DataSaved.glL_AnchorView[1]) *rs(),
                                    (float) (bucketCoord[2] - DataSaved.glL_AnchorView[2]) * rs());
                            Point3D pC = getProjectedPointOnSegment3D(new Point3D(bucketCoord[0], bucketCoord[1], bucketCoord[2]),
                                    DataSaved.nearestSegment.getStart(), DataSaved.nearestSegment.getEnd());
                            Point3DF plineC = new Point3DF((float) (pC.getX() - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (pC.getY() - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (pC.getZ() - DataSaved.glL_AnchorView[2]) * rs());
                            glLinePoint = new Point3DF(pbuckC.getX(), pbuckC.getY(), pbuckC.getZ());
                            glSegmentPoint = new Point3DF(pbuckC.getX(), pbuckC.getY(), plineC.getZ());
                            glSegmentEnd = plineC;
                            orientamentoFreccia = My_LocationCalc.calcBearingXY(bucketCoord[0], bucketCoord[1], pC.getX(), pC.getY());

                            break;

                        case 1:

                            Point3DF pbuckR = new Point3DF((float) (bucketRightCoord[0] - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (bucketRightCoord[1] - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (bucketRightCoord[2] - DataSaved.glL_AnchorView[2]) * rs());
                            Point3D pR = getProjectedPointOnSegment3D(new Point3D(bucketRightCoord[0], bucketRightCoord[1], bucketRightCoord[2]),
                                    DataSaved.nearestSegment.getStart(), DataSaved.nearestSegment.getEnd());
                            Point3DF plineR = new Point3DF((float) (pR.getX() - DataSaved.glL_AnchorView[0]) * rs(),
                                    (float) (pR.getY() - DataSaved.glL_AnchorView[1]) * rs(),
                                    (float) (pR.getZ() - DataSaved.glL_AnchorView[2]) * rs());
                            glLinePoint = new Point3DF(pbuckR.getX(), pbuckR.getY(), pbuckR.getZ());
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

        fw = Exca_Quaternion.endPoint(origin, 0, 0, 50, hdt_LAMA);
        bw = Exca_Quaternion.endPoint(origin, 0, 0, 15, hdt_LAMA + 180);
        lt = Exca_Quaternion.endPoint(origin, correctRoll, 0, 10, hdt_LAMA + 270);
        rt = Exca_Quaternion.endPoint(origin, -correctRoll, 0, 10, hdt_LAMA + 90);

        P0 = pTransform(p0, DataSaved.glL_AnchorView, rs());
        P1 = pTransform(p1, DataSaved.glL_AnchorView, rs());
        P2 = pTransform(p2, DataSaved.glL_AnchorView, rs());
        P3 = pTransform(p3, DataSaved.glL_AnchorView, rs());
        P4 = pTransform(p4, DataSaved.glL_AnchorView, rs());
        P5 = pTransform(p5, DataSaved.glL_AnchorView, rs());
        P6 = pTransform(p6, DataSaved.glL_AnchorView, rs());
        P7 = pTransform(p7, DataSaved.glL_AnchorView, rs());
        P8 = pTransform(p8, DataSaved.glL_AnchorView, rs());
        P9 = pTransform(p9, DataSaved.glL_AnchorView, rs());
        P10 = pTransform(p10, DataSaved.glL_AnchorView, rs());
        P11 = pTransform(p11, DataSaved.glL_AnchorView, rs());
        P12 = pTransform(p12, DataSaved.glL_AnchorView, rs());
        P13 = pTransform(p13, DataSaved.glL_AnchorView, rs());
        C1 = pTransform(c1, DataSaved.glL_AnchorView, rs());
        C2 = pTransform(c2, DataSaved.glL_AnchorView, rs());
        A1A = pTransform(ant1_a, DataSaved.glL_AnchorView, rs());
        A1B = pTransform(ant1_b, DataSaved.glL_AnchorView, rs());
        A2A = pTransform(ant2_a, DataSaved.glL_AnchorView, rs());
        A2B = pTransform(ant2_b, DataSaved.glL_AnchorView, rs());
        CAP1 = pTransform(cap1, DataSaved.glL_AnchorView, rs());
        CAP2 = pTransform(cap2, DataSaved.glL_AnchorView, rs());
        COVER_L = pTransform(cover_l, DataSaved.glL_AnchorView, rs());
        COVER_R = pTransform(cover_r, DataSaved.glL_AnchorView,rs());
        SPIG_L = pTransform(splA, DataSaved.glL_AnchorView, rs());
        SPIG_C = pTransform(spcA, DataSaved.glL_AnchorView, rs());
        SPIG_R = pTransform(sprA, DataSaved.glL_AnchorView,rs());
        SPIG_LB = pTransform(splB, DataSaved.glL_AnchorView, rs());
        SPIG_CB = pTransform(spcB, DataSaved.glL_AnchorView, rs());
        SPIG_RB = pTransform(sprB, DataSaved.glL_AnchorView, rs());
        start = pTransform(origin, DataSaved.glL_AnchorView, rs());
        fwF = pTransform(fw, DataSaved.glL_AnchorView, rs());
        bwF = pTransform(bw, DataSaved.glL_AnchorView, rs());
        rtF = pTransform(rt, DataSaved.glL_AnchorView, rs());
        ltF = pTransform(lt, DataSaved.glL_AnchorView, rs());
        return new Point3DF[]{
                P0,//0
                P1,//1
                P2,//2
                P3,//3
                P4,//4
                P5,//5
                P6,//6
                P7,//7
                P8,//8
                P9,//9
                P10,//10
                P11,//11
                P12,//12
                P13,//13
                C1,//14
                C2,//15
                A1A,//16
                A1B,//17
                A2A,//18
                A2B,//19
                CAP1,//20
                CAP2,//21
                COVER_L,//22
                COVER_R,//23
                SPIG_C,//24
                SPIG_R,//25
                SPIG_L,//26
                SPIG_CB,//27
                SPIG_RB,//28
                SPIG_LB,//29
                start,//30
                fwF,//31
                bwF,//32
                rtF,//33
                ltF,//34
                glLinePoint,//35
                glSegmentPoint,//36
                glSegmentEnd, //37
                glLinePunto,//38
                glPuntoTerra,//39
                glTerraPunto //40


        };

    }

    public static short[] lamaChiara() {
        return new short[]{
                2, 3, 8,
                8, 3, 7,
                1, 10, 2,
                2, 11, 3,
                3, 11, 4,
                9, 13, 12,
                8, 12, 7,
                12, 11, 10,
                10, 13, 12,
                2, 10, 11,
                6, 12, 7,
                4, 11, 12,
                12, 6, 4


        };
    }

    public static short[] lamaScura() {
        return new short[]{


                9, 1, 2,
                2, 8, 9,

                3, 4, 6,
                6, 7, 3,

                1, 9, 13,
                13, 10, 1,


        };
    }

    public static short[] lamaContour() {
        return new short[]{
                0, 1,
                1, 2,
                2, 3,
                3, 4,
                4, 5,
                5, 6,
                6, 7,
                7, 8,
                8, 9,
                9, 0,

                10, 11,
                12, 13,
                12, 11,
                6, 12,
                4, 11

        };
    }

}
