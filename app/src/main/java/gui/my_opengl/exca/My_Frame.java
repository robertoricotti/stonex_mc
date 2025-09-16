package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.ExcavatorLib.coordinateDY;
import static packexcalib.exca.ExcavatorLib.correctPitch;
import static packexcalib.exca.ExcavatorLib.correctRoll;
import static packexcalib.exca.ExcavatorLib.*;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;

import dxf.Point3D;
import gui.my_opengl.Point3DF;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;
import utils.DistToPoint;


public class My_Frame {

    static Point3DF P1_A, P2_A, P3_A, P4_A, P5_A;
    static Point3DF P1_B, P2_B, P3_B, P4_B, P5_B;
    static Point3DF CAB_0_B, CAB_1_B, CAB_2_B, CAB_3_B;
    static Point3DF CAB_0_A, CAB_1_A, CAB_2_A, CAB_3_A;
    static Point3DF AI_2,BI_2,AI_3,BI_3,AI_4,BI_4,AI_5,BI_5;
    static double mHdt_Boom;

    static Point3DF MINP0, MINP1, MINP2, MINP3;
    static Point3DF MINPB0, MINPB1, MINPB2, MINPB3;

    static Point3DF ZAVM0, ZAVM1, ZAVM2, ZAVM3;
    static Point3DF ZAVH0, ZAVH1, ZAVH2, ZAVH3, FINE_ZAV, RALLA_A, RALLA_B;
    static Point3DF PC_2, P2_ALTO, P2_BASSO, P2I_C;
    static Point3DF PC_3, P3_ALTO, P3_BASSO, P3I_C;
    static Point3DF PC_4, P4_ALTO, P4_BASSO, P4I_C;
    static Point3DF PC_5, P5_ALTO, P5_BASSO, P5I_C;

    static double L1, L2, Bordi, H_Cab, L_Cab;
    public static double R_Cingolo, H_Ralla, W_Cingolo;

    public static Point3DF[] puntiFrame() {
        mHdt_Boom= hdt_BOOM-swing_boom_angle;

        double[] rallabassa, rallaalta;
        double[] p2_c, p2_alto, p2_basso, p2_i_c,ai_2, bi_2;
        double[] p3_c, p3_alto, p3_basso, p3_i_c, ai_3, bi_3;
        double[] p4_c, p4_alto, p4_basso, p4_i_c, ai_4, bi_4;
        double[] p5_c, p5_alto, p5_basso, p5_i_c, ai_5, bi_5;


        double[] p0, p1a, p2a, p3a, p4a, p5a;
        double[] p1b, p2b, p3b, p4b, p5b;

        double[] cab0_b, cab1_b, cab2_b, cab3_b;
        double[] cab0_a, cab1_a, cab2_a, cab3_a;
        double[] minip0, minip1, minip2, minip3;
        double[] minipB0, minipB1, minipB2, minipB3;

        double[] zavm0, zavm1, zavm2, zavm3;//zavorra
        double[] zavh0, zavh1, zavh2, zavh3, finezav;//zavorra alta

        double larghezzaBraccio_STRETTA = (float) (Math.max(0.22, Math.min((DataSaved.L_Stick * 0.22f), 0.6)));
        L1 = (DataSaved.L_Boom1 + DataSaved.L_Boom2 + DataSaved.L_Stick) * 0.2;
        L1 = Math.max(1.5, Math.min(L1, 3.5));
        L2 = L1 * 1.4;
        Bordi = L1 * 0.2;
        H_Cab = 1.2;
        H_Ralla = L2 * 0.065;
        R_Cingolo = (L2 - H_Ralla) * 0.2;
        W_Cingolo = L1 * 0.25;
        if(DataSaved.Extra_Heading!=0){
            p0 = coordMiniPitch;
        }else {
            p0 = coordinateDY;
        }
        p1a = Exca_Quaternion.endPoint(p0, correctPitch, correctRoll, 0.3 + (-DataSaved.L_Pitch), mHdt_Boom);
        p2a = Exca_Quaternion.endPoint(p1a, -correctRoll, correctPitch, (L1 * 0.5) - DataSaved.L_Roll, mHdt_Boom + 90);
        p3a = Exca_Quaternion.endPoint(p2a, -correctPitch, -correctRoll, L2, mHdt_Boom + 180);
        p4a = Exca_Quaternion.endPoint(p3a, correctRoll, -correctPitch, L1 + DataSaved.L_Roll, mHdt_Boom - 90);
        p5a = Exca_Quaternion.endPoint(p4a, correctPitch, correctRoll, L2, mHdt_Boom);
        L_Cab = DistToPoint.dist3D(p5a, p1a) - 0.1 - (larghezzaBraccio_STRETTA * 0.5);
        L_Cab = Math.max(0.8, Math.min(L_Cab, 1.5));
        p1b = Exca_Quaternion.endPoint(p1a, correctPitch - 90, correctRoll, 0.2, mHdt_Boom);
        p2b = Exca_Quaternion.endPoint(p2a, correctPitch - 90, correctRoll, 0.2, mHdt_Boom);
        p3b = Exca_Quaternion.endPoint(p3a, correctPitch - 90, correctRoll, 0.2, mHdt_Boom);
        p4b = Exca_Quaternion.endPoint(p4a, correctPitch - 90, correctRoll, 0.2, mHdt_Boom);
        p5b = Exca_Quaternion.endPoint(p5a, correctPitch - 90, correctRoll, 0.2, mHdt_Boom);

        cab0_b = p5a;
        cab1_b = Exca_Quaternion.endPoint(cab0_b, -correctRoll, correctPitch, L_Cab, mHdt_Boom + 90);
        cab2_b = Exca_Quaternion.endPoint(cab1_b, -correctPitch, -correctRoll, L_Cab * 1.2, mHdt_Boom + 180);
        cab3_b = Exca_Quaternion.endPoint(cab2_b, correctRoll, -correctPitch, L_Cab, mHdt_Boom - 90);

        cab0_a = Exca_Quaternion.endPoint(cab0_b, correctPitch + 95, correctRoll, H_Cab, mHdt_Boom);
        cab1_a = Exca_Quaternion.endPoint(cab1_b, correctPitch + 95, correctRoll, H_Cab, mHdt_Boom);
        cab2_a = Exca_Quaternion.endPoint(cab2_b, correctPitch + 90, correctRoll, H_Cab, mHdt_Boom);
        cab3_a = Exca_Quaternion.endPoint(cab3_b, correctPitch + 90, correctRoll, H_Cab, mHdt_Boom);

        minip0 = Exca_Quaternion.endPoint(p0, Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mHdt_Boom - 90);
        minip1 = Exca_Quaternion.endPoint(p0, -Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mHdt_Boom + 90);
        minip2 = Exca_Quaternion.endPoint(minip1, correctPitch, correctRoll, DataSaved.L_Pitch, mHdt_Boom + 180);
        minip3 = Exca_Quaternion.endPoint(minip0, correctPitch, correctRoll, DataSaved.L_Pitch, mHdt_Boom + 180);

        minipB0 = Exca_Quaternion.endPoint(minip0, correctPitch - 90, correctRoll, 0.05, mHdt_Boom);
        minipB1 = Exca_Quaternion.endPoint(minip1, correctPitch - 90, correctRoll, 0.05, mHdt_Boom);
        minipB2 = Exca_Quaternion.endPoint(minip2, correctPitch - 90, correctRoll, 0.1, mHdt_Boom);
        minipB3 = Exca_Quaternion.endPoint(minip3, correctPitch - 90, correctRoll, 0.1, mHdt_Boom);

        zavm0 = Exca_Quaternion.endPoint(p4a, correctPitch + 90, correctRoll, 0.5, mHdt_Boom);
        zavm1 = Exca_Quaternion.endPoint(cab3_b, correctPitch + 90, correctRoll, 0.5, mHdt_Boom);
        zavm2 = Exca_Quaternion.endPoint(zavm1, -correctRoll, correctPitch, DistToPoint.dist3D(p3a, p4a), mHdt_Boom + 90);
        finezav = Exca_Quaternion.endPoint(zavm2, correctPitch - 90, correctRoll, 0.5, mHdt_Boom);
        zavm3 = Exca_Quaternion.endPoint(p3a, correctPitch + 90, correctRoll, 0.5, mHdt_Boom);
        Point3D punto_0 = new Point3D(zavm0[0], zavm0[1], zavm0[2]);
        Point3D punto_1 = new Point3D(zavm2[0], zavm2[1], zavm2[2]);

        Point3D punto_2 = new Point3D(zavm1[0], zavm1[1], zavm1[2]);
        Point3D punto_3 = new Point3D(zavm3[0], zavm3[1], zavm3[2]);

        Point3D puntomedio0 = punto_0.interpolate(punto_1, 1.0 / 10.0);
        Point3D puntmedio1 = punto_1.interpolate(punto_0, 1.0 / 10.0);

        Point3D puntomedio2 = punto_2.interpolate(punto_3, 1.0 / 10.0);
        Point3D puntmeodio3 = punto_3.interpolate(punto_2, 1.0 / 10.0);

        zavh0 = Exca_Quaternion.endPoint(new double[]{puntomedio0.getX(), puntomedio0.getY(), puntomedio0.getZ()}, correctPitch + 90, correctRoll, 0.15, mHdt_Boom);
        zavh1 = Exca_Quaternion.endPoint(new double[]{puntomedio2.getX(), puntomedio2.getY(), puntomedio2.getZ()}, correctPitch + 90, correctRoll, 0.15, mHdt_Boom);
        zavh2 = Exca_Quaternion.endPoint(new double[]{puntmedio1.getX(), puntmedio1.getY(), puntmedio1.getZ()}, correctPitch + 90, correctRoll, 0.15, mHdt_Boom);
        zavh3 = Exca_Quaternion.endPoint(new double[]{puntmeodio3.getX(), puntmeodio3.getY(), puntmeodio3.getZ()}, correctPitch + 90, correctRoll, 0.15, mHdt_Boom);

        Point3D pmRalla1 = new Point3D(p2b[0], p2b[1], p2b[2]);
        Point3D pmRalla2 = new Point3D(p4b[0], p4b[1], p4b[2]);
        Point3D pmRalla = pmRalla1.interpolate(pmRalla2, 1.0 / 2.0);
        rallaalta = new double[]{pmRalla.getX(), pmRalla.getY(), pmRalla.getZ()};
        rallabassa = Exca_Quaternion.endPoint(rallaalta, correctPitch - 90, correctRoll, H_Ralla, mHdt_Boom);

        p2_c = Exca_Quaternion.endPoint(p2b, correctPitch - 90, correctRoll, R_Cingolo * 0.95, mHdt_Boom);
        p3_c = Exca_Quaternion.endPoint(p3b, correctPitch - 90, correctRoll, R_Cingolo * 0.95, mHdt_Boom);
        p4_c = Exca_Quaternion.endPoint(p4b, correctPitch - 90, correctRoll, R_Cingolo * 0.95, mHdt_Boom);
        p5_c = Exca_Quaternion.endPoint(p5b, correctPitch - 90, correctRoll, R_Cingolo * 0.95, mHdt_Boom);

        p2_alto = Exca_Quaternion.endPoint(p2_c, correctPitch + 90, correctRoll, R_Cingolo * 0.5, mHdt_Boom);
        p2_basso = Exca_Quaternion.endPoint(p2_c, correctPitch - 90, correctRoll, R_Cingolo * 0.5, mHdt_Boom);
        p3_alto = Exca_Quaternion.endPoint(p3_c, correctPitch + 90, correctRoll, R_Cingolo * 0.5, mHdt_Boom);
        p3_basso = Exca_Quaternion.endPoint(p3_c, correctPitch - 90, correctRoll, R_Cingolo * 0.5, mHdt_Boom);
        p4_alto = Exca_Quaternion.endPoint(p4_c, correctPitch + 90, correctRoll, R_Cingolo * 0.5, mHdt_Boom);
        p4_basso = Exca_Quaternion.endPoint(p4_c, correctPitch - 90, correctRoll, R_Cingolo * 0.5, mHdt_Boom);
        p5_alto = Exca_Quaternion.endPoint(p5_c, correctPitch + 90, correctRoll, R_Cingolo * 0.5, mHdt_Boom);
        p5_basso = Exca_Quaternion.endPoint(p5_c, correctPitch - 90, correctRoll, R_Cingolo * 0.5, mHdt_Boom);

        p2_i_c = Exca_Quaternion.endPoint(p2_c, correctRoll, -correctPitch, W_Cingolo, mHdt_Boom - 90);
        p3_i_c = Exca_Quaternion.endPoint(p3_c, correctRoll, -correctPitch, W_Cingolo, mHdt_Boom - 90);

        p4_i_c = Exca_Quaternion.endPoint(p4_c, -correctRoll, correctPitch, W_Cingolo, mHdt_Boom + 90);
        p5_i_c = Exca_Quaternion.endPoint(p5_c, -correctRoll, correctPitch, W_Cingolo, mHdt_Boom + 90);

        ai_2=Exca_Quaternion.endPoint(p2_i_c,correctPitch+90,correctRoll,R_Cingolo*0.5,mHdt_Boom);
        bi_2=Exca_Quaternion.endPoint(p2_i_c,correctPitch-90,correctRoll,R_Cingolo*0.5,mHdt_Boom);
        ai_3=Exca_Quaternion.endPoint(p3_i_c,correctPitch+90,correctRoll,R_Cingolo*0.5,mHdt_Boom);
        bi_3=Exca_Quaternion.endPoint(p3_i_c,correctPitch-90,correctRoll,R_Cingolo*0.5,mHdt_Boom);

        ai_4=Exca_Quaternion.endPoint(p4_i_c,correctPitch+90,correctRoll,R_Cingolo*0.5,mHdt_Boom);
        bi_4=Exca_Quaternion.endPoint(p4_i_c,correctPitch-90,correctRoll,R_Cingolo*0.5,mHdt_Boom);
        ai_5=Exca_Quaternion.endPoint(p5_i_c,correctPitch+90,correctRoll,R_Cingolo*0.5,mHdt_Boom);
        bi_5=Exca_Quaternion.endPoint(p5_i_c,correctPitch-90,correctRoll,R_Cingolo*0.5,mHdt_Boom);
        {
            P1_A = pTransform(p1a, DataSaved.glL_AnchorView, scale);
            P2_A = pTransform(p2a, DataSaved.glL_AnchorView, scale);
            P3_A = pTransform(p3a, DataSaved.glL_AnchorView, scale);
            P4_A = pTransform(p4a, DataSaved.glL_AnchorView, scale);
            P5_A = pTransform(p5a, DataSaved.glL_AnchorView, scale);
            P1_B = pTransform(p1b, DataSaved.glL_AnchorView, scale);
            P2_B = pTransform(p2b, DataSaved.glL_AnchorView, scale);
            P3_B = pTransform(p3b, DataSaved.glL_AnchorView, scale);
            P4_B = pTransform(p4b, DataSaved.glL_AnchorView, scale);
            P5_B = pTransform(p5b, DataSaved.glL_AnchorView, scale);

            CAB_0_B = pTransform(cab0_b, DataSaved.glL_AnchorView, scale);
            CAB_1_B = pTransform(cab1_b, DataSaved.glL_AnchorView, scale);
            CAB_2_B = pTransform(cab2_b, DataSaved.glL_AnchorView, scale);
            CAB_3_B = pTransform(cab3_b, DataSaved.glL_AnchorView, scale);

            CAB_0_A = pTransform(cab0_a, DataSaved.glL_AnchorView, scale);
            CAB_1_A = pTransform(cab1_a, DataSaved.glL_AnchorView, scale);
            CAB_2_A = pTransform(cab2_a, DataSaved.glL_AnchorView, scale);
            CAB_3_A = pTransform(cab3_a, DataSaved.glL_AnchorView, scale);

            MINP0 = pTransform(minip0, DataSaved.glL_AnchorView, scale);
            MINP1 = pTransform(minip1, DataSaved.glL_AnchorView, scale);
            MINP2 = pTransform(minip2, DataSaved.glL_AnchorView, scale);
            MINP3 = pTransform(minip3, DataSaved.glL_AnchorView, scale);

            MINPB0 = pTransform(minipB0, DataSaved.glL_AnchorView, scale);
            MINPB1 = pTransform(minipB1, DataSaved.glL_AnchorView, scale);
            MINPB2 = pTransform(minipB2, DataSaved.glL_AnchorView, scale);
            MINPB3 = pTransform(minipB3, DataSaved.glL_AnchorView, scale);

            ZAVM0 = pTransform(zavm0, DataSaved.glL_AnchorView, scale);
            ZAVM1 = pTransform(zavm1, DataSaved.glL_AnchorView, scale);
            ZAVM2 = pTransform(zavm2, DataSaved.glL_AnchorView, scale);
            ZAVM3 = pTransform(zavm3, DataSaved.glL_AnchorView, scale);

            ZAVH0 = pTransform(zavh0, DataSaved.glL_AnchorView, scale);
            ZAVH1 = pTransform(zavh1, DataSaved.glL_AnchorView, scale);
            ZAVH2 = pTransform(zavh2, DataSaved.glL_AnchorView, scale);
            ZAVH3 = pTransform(zavh3, DataSaved.glL_AnchorView, scale);
            FINE_ZAV = pTransform(finezav, DataSaved.glL_AnchorView, scale);

            RALLA_A = pTransform(rallaalta, DataSaved.glL_AnchorView, scale);
            RALLA_B = pTransform(rallabassa, DataSaved.glL_AnchorView, scale);

            AI_2=pTransform(ai_2,DataSaved.glL_AnchorView,scale);
            BI_2=pTransform(bi_2,DataSaved.glL_AnchorView,scale);
            AI_3=pTransform(ai_3,DataSaved.glL_AnchorView,scale);
            BI_3=pTransform(bi_3,DataSaved.glL_AnchorView,scale);
            AI_4=pTransform(ai_4,DataSaved.glL_AnchorView,scale);
            BI_4=pTransform(bi_4,DataSaved.glL_AnchorView,scale);
            AI_5=pTransform(ai_5,DataSaved.glL_AnchorView,scale);
            BI_5=pTransform(bi_5,DataSaved.glL_AnchorView,scale);
        }
        PC_2 = pTransform(p2_c, DataSaved.glL_AnchorView, scale);
        PC_3 = pTransform(p3_c, DataSaved.glL_AnchorView, scale);
        PC_4 = pTransform(p4_c, DataSaved.glL_AnchorView, scale);
        PC_5 = pTransform(p5_c, DataSaved.glL_AnchorView, scale);
        P2_ALTO = pTransform(p2_alto, DataSaved.glL_AnchorView, scale);
        P3_ALTO = pTransform(p3_alto, DataSaved.glL_AnchorView, scale);
        P4_ALTO = pTransform(p4_alto, DataSaved.glL_AnchorView, scale);
        P5_ALTO = pTransform(p5_alto, DataSaved.glL_AnchorView, scale);
        P2_BASSO = pTransform(p2_basso, DataSaved.glL_AnchorView, scale);
        P3_BASSO = pTransform(p3_basso, DataSaved.glL_AnchorView, scale);
        P4_BASSO = pTransform(p4_basso, DataSaved.glL_AnchorView, scale);
        P5_BASSO = pTransform(p5_basso, DataSaved.glL_AnchorView, scale);
        P2I_C = pTransform(p2_i_c, DataSaved.glL_AnchorView, scale);
        P3I_C = pTransform(p3_i_c, DataSaved.glL_AnchorView, scale);
        P4I_C = pTransform(p4_i_c, DataSaved.glL_AnchorView, scale);
        P5I_C = pTransform(p5_i_c, DataSaved.glL_AnchorView, scale);

        return new Point3DF[]{
                P1_A,//0
                P2_A,//1
                P3_A,//2
                P4_A,//3
                P5_A,//4

                P1_B,//5
                P2_B,//6
                P3_B,//7
                P4_B,//8
                P5_B, //9

                CAB_0_B,//10
                CAB_1_B,//11
                CAB_2_B,//12
                CAB_3_B,//13

                CAB_0_A,//14
                CAB_1_A,//15
                CAB_2_A,//16
                CAB_3_A, //17

                MINP0,//18
                MINP1,//19
                MINP2,//20
                MINP3,//21

                MINPB0,//22
                MINPB1,//23
                MINPB2,//24
                MINPB3,//25

                ZAVM0,//26
                ZAVM1,//27
                ZAVM2,//28
                ZAVM3,//29

                ZAVH0,//30
                ZAVH1,//31
                ZAVH2,//32
                ZAVH3, //33
                FINE_ZAV, //34

                RALLA_A,//35
                RALLA_B, //36

                PC_2,//37
                PC_3,//38
                PC_4,//39
                PC_5,//40

                P2_ALTO,//41
                P3_ALTO,//42
                P4_ALTO,//43
                P5_ALTO,//44

                P2_BASSO,//45
                P3_BASSO,//46
                P4_BASSO,//47
                P5_BASSO,//48

                P2I_C,//49
                P3I_C,//50
                P4I_C,//51
                P5I_C,//52

                AI_2,//53
                BI_2,//54
                AI_3,//55
                BI_3,//56
                AI_4,//57
                BI_4,//58
                AI_5,//59
                BI_5 //60


        };

    }


    public static short[] zavorraMedia() {
        return new short[]{
                26, 30, 31,
                31, 27, 26,
                27, 31, 28,
                31, 32, 28,
                28, 32, 33,
                33, 29, 28,
                29, 33, 30,
                30, 26, 29,
                29, 28, 34,
                13, 27, 26,
                2, 3, 26,
                26, 29, 2,
                34, 28, 13,
                28, 27, 13


        };
    }

    public static short[] cappello() {
        return new short[]{
                30, 31, 32,
                32, 33, 30,
                26, 3, 13,
                2, 29, 34,


        };
    }

    public static short[] bordiCappello() {
        return new short[]{
                26, 30,
                27, 31,
                28, 32,
                29, 33,

                26, 27,
                27, 28,
                28, 29,
                29, 26,
                34, 28,
                34, 13


        };
    }


    public static short[] triangoliMiniChiari() {
        return new short[]{
                22, 23, 24,
                24, 25, 22,
                19, 23, 20,
                23, 24, 20,
                22, 18, 21,
                21, 25, 22


        };
    }

    public static short[] triangoliMiniScuri() {
        return new short[]{
                18, 19, 20,
                20, 21, 18,
                18, 22, 19,
                22, 23, 19,


        };
    }

    public static short[] bordiMiniP() {
        return new short[]{
                18, 22,
                19, 23,
                20, 24,
                21, 25


        };
    }


    public static short[] triangoliFrameChiari() {
        return new short[]{
                0, 1, 2,
                2, 3, 4,
                4, 0, 2,//sopra


                1, 6, 7,//contorno
                7, 2, 1,
                3, 2, 7,
                7, 8, 3,
                3, 8, 9,
                9, 4, 3,
                4, 9, 6,
                6, 1, 4


        };
    }

    public static short[] triangoliFrameScuri() {
        return new short[]{


                5, 6, 7,//sotto
                7, 8, 9,
                9, 5, 7,


        };
    }

    public static short[] bordi() {
        return new short[]{
                1, 2,
                2, 3,
                3, 4,
                4, 1,

                6, 7,
                7, 8,
                8, 9,
                9, 6,

                1, 6,
                2, 7,
                3, 8,
                4, 9,
                34, 12

        };
    }


    public static short[] cabinaChiara() {
        return new short[]{
                14, 15, 16,
                16, 17, 14,

                11, 15, 16,
                16, 12, 11,

                10, 14, 17,
                17, 13, 10,
                10, 11, 15,
                15, 14, 10,


        };
    }

    public static short[] cabinaScura() {
        return new short[]{
                12, 13, 17,
                17, 16, 12,


        };
    }

    public static short[] bordiCab() {
        return new short[]{

                10, 11,
                11, 12,
                12, 13,
                15, 16,
                16, 17,
                14, 15,
                10, 14,
                14, 17,
                11, 15,
                2, 29,
                3, 26,


        };
    }

    public static short[] cingoliChiari() {
        return new short[]{

                36, 43, 44,
                36, 41, 42,
                48, 44, 43,
                43, 47, 48,
                46, 42, 41,
                41, 45, 46,
                47,58,60,
                60,48,47,
                54,56,46,
                46,45,54


        };
    }

    public static short[] cingoliScuri() {
        return new short[]{
                53, 54, 55,
                55, 56, 54,
                57,59,58,
                60,58,59,


        };
    }

    public static short[] bordiCingoli() {
        return new short[]{

                41, 42,
                46, 45,
                43, 44,
                47, 48,




        };
    }


}

