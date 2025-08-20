package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.ExcavatorLib.coordB1;
import static packexcalib.exca.ExcavatorLib.coordB2;
import static packexcalib.exca.ExcavatorLib.coordinateDY;
import static packexcalib.exca.ExcavatorLib.correctBoom1;
import static packexcalib.exca.ExcavatorLib.correctBoom2;
import static packexcalib.exca.ExcavatorLib.*;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;

import gui.my_opengl.Point3DF;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;

public class My_Boom1_Boom2 {
    static double mhdt;
    static Point3DF P0_S, P1_S, P2_S, P3_S, P4_S, P5_S, P6_S, P7_S, P8_S, P9_S, P10_S, P11_S;
    static Point3DF P0_D, P1_D, P2_D, P3_D, P4_D, P5_D, P6_D, P7_D, P8_D, P9_D, P10_D, P11_D;
    static double L1, L2, L3, L4;

    public static Point3DF[] puntiBoom() {
        mhdt=hdt_BOOM;
        double larghezzaBraccio_STRETTA = (float) (Math.max(0.22, Math.min((DataSaved.L_Stick * 0.22f), 0.6)));
        double[] p0_s, p1_s, p2_s, p3_s, p4_s, p5_s, p6_s, p7_s, p8_s, p9_s, p10_s, p11_s;
        double[] p0_d, p1_d, p2_d, p3_d, p4_d, p5_d, p6_d, p7_d, p8_d, p9_d, p10_d, p11_d;
        L1 = DataSaved.L_Boom2 * 0.08;
        L2 = DataSaved.L_Boom2 * 0.25;
        L3 = DataSaved.L_Boom2 * 0.05;
        L4 = DataSaved.L_Boom1 * 0.08;

        //Boom 2 below
        p0_s = Exca_Quaternion.endPoint(coordB2, Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mhdt - 90);
        p0_d = Exca_Quaternion.endPoint(coordB2, -Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mhdt + 90);
        p3_s = Exca_Quaternion.endPoint(coordB1, Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mhdt - 90);
        p3_d = Exca_Quaternion.endPoint(coordB1, -Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mhdt + 90);

        //sx
        p1_s = Exca_Quaternion.endPoint(p0_s, correctBoom2 + 125, Deg_Boom_Roll, L1, mhdt);
        p2_s = Exca_Quaternion.endPoint(p3_s, correctBoom2 + 140, Deg_Boom_Roll, L2, mhdt);
        p4_s = Exca_Quaternion.endPoint(p3_s, correctBoom2 + 180 + 45, Deg_Boom_Roll, L3, mhdt);
        p5_s = Exca_Quaternion.endPoint(p0_s, correctBoom2 - 125, Deg_Boom_Roll, L1, mhdt);

        //dx
        p1_d = Exca_Quaternion.endPoint(p0_d, correctBoom2 + 125, Deg_Boom_Roll, L1, mhdt);
        p2_d = Exca_Quaternion.endPoint(p3_d, correctBoom2 + 140, Deg_Boom_Roll, L2, mhdt);
        p4_d = Exca_Quaternion.endPoint(p3_d, correctBoom2 + 180 + 45, Deg_Boom_Roll, L3, mhdt);
        p5_d = Exca_Quaternion.endPoint(p0_d, correctBoom2 - 125, Deg_Boom_Roll, L1, mhdt);

        ////Boom 1 below
        p6_s = Exca_Quaternion.endPoint(coordB1, Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mhdt - 90);
        p6_d = Exca_Quaternion.endPoint(coordB1, -Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mhdt + 90);
        p9_s = Exca_Quaternion.endPoint(coordinateDY, Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mhdt - 90);
        p9_d = Exca_Quaternion.endPoint(coordinateDY, -Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, mhdt + 90);

        //sx
        p7_s = Exca_Quaternion.endPoint(p6_s, correctBoom1 + 125, Deg_Boom_Roll, L4, mhdt);
        p11_s = Exca_Quaternion.endPoint(p6_s, correctBoom1 - 125, Deg_Boom_Roll, L4, mhdt);
        p8_s = Exca_Quaternion.endPoint(p9_s, correctBoom1 + 55, Deg_Boom_Roll, L4*1.4, mhdt);
        p10_s = Exca_Quaternion.endPoint(p9_s, correctBoom1 - 55, Deg_Boom_Roll, L4*1.4, mhdt);

        //dx
        p7_d = Exca_Quaternion.endPoint(p6_d, correctBoom1 + 125, Deg_Boom_Roll, L4, mhdt);
        p11_d = Exca_Quaternion.endPoint(p6_d, correctBoom1 - 125, Deg_Boom_Roll, L4, mhdt);
        p8_d = Exca_Quaternion.endPoint(p9_d, correctBoom1 + 55, Deg_Boom_Roll, L4*1.4, mhdt);
        p10_d = Exca_Quaternion.endPoint(p9_d, correctBoom1 - 55, Deg_Boom_Roll, L4*1.4, mhdt);

        P0_S = pTransform(p0_s, DataSaved.glL_AnchorView, scale);
        P1_S = pTransform(p1_s, DataSaved.glL_AnchorView, scale);
        P2_S = pTransform(p2_s, DataSaved.glL_AnchorView, scale);
        P3_S = pTransform(p3_s, DataSaved.glL_AnchorView, scale);
        P4_S = pTransform(p4_s, DataSaved.glL_AnchorView, scale);
        P5_S = pTransform(p5_s, DataSaved.glL_AnchorView, scale);
        P6_S = pTransform(p6_s, DataSaved.glL_AnchorView, scale);
        P7_S = pTransform(p7_s, DataSaved.glL_AnchorView, scale);
        P8_S = pTransform(p8_s, DataSaved.glL_AnchorView, scale);
        P9_S = pTransform(p9_s, DataSaved.glL_AnchorView, scale);
        P10_S = pTransform(p10_s, DataSaved.glL_AnchorView, scale);
        P11_S = pTransform(p11_s, DataSaved.glL_AnchorView, scale);

        P0_D = pTransform(p0_d, DataSaved.glL_AnchorView, scale);
        P1_D = pTransform(p1_d, DataSaved.glL_AnchorView, scale);
        P2_D = pTransform(p2_d, DataSaved.glL_AnchorView, scale);
        P3_D = pTransform(p3_d, DataSaved.glL_AnchorView, scale);
        P4_D = pTransform(p4_d, DataSaved.glL_AnchorView, scale);
        P5_D = pTransform(p5_d, DataSaved.glL_AnchorView, scale);
        P6_D = pTransform(p6_d, DataSaved.glL_AnchorView, scale);
        P7_D = pTransform(p7_d, DataSaved.glL_AnchorView, scale);
        P8_D = pTransform(p8_d, DataSaved.glL_AnchorView, scale);
        P9_D = pTransform(p9_d, DataSaved.glL_AnchorView, scale);
        P10_D = pTransform(p10_d, DataSaved.glL_AnchorView, scale);
        P11_D = pTransform(p11_d, DataSaved.glL_AnchorView, scale);
        return new Point3DF[]{
                P0_S, P1_S, P2_S, P3_S, P4_S, P5_S, P6_S, P7_S, P8_S, P9_S, P10_S, P11_S,
                P0_D, P1_D, P2_D, P3_D, P4_D, P5_D, P6_D, P7_D, P8_D, P9_D, P10_D, P11_D
        };

    }

    public static short[] facceChiare() {
        return new short[]{
                //boom2 left
                0, 1, 5,
                1, 2, 5,
                2, 4, 5,

                //boom2 right
                12, 13, 17,
                13, 14, 17,
                14, 16, 17,

                //boom1 left
                6, 7, 11,
                8, 9, 10,
                7, 8, 11,
                8, 10, 11,

                //boom1 right
                18, 19, 23,
                20, 21, 22,
                19, 20, 23,
                20, 22, 23,

                //top boom 2 chiaro
                1, 0, 12,
                12, 13, 1,

                4, 2, 14,
                14, 16, 4,


        };
    }

    public static short[] facceScure() {
        return new short[]{

                2, 1, 13,
                13, 14, 2,
                5, 4, 16,
                16, 17, 5,

                7, 6, 18,
                18, 19, 7,
                8, 7, 19,
                19, 20, 8,
                9, 8, 20,
                20, 21, 9,
                10, 9, 21,
                21, 22, 10,
                11, 10, 22,
                22, 23, 11

        };
    }

    public static short[] contorni() {
        return new short[]{

                0, 1,
                1, 2,
                2, 4,
                4, 5,
                5, 0,
                12, 13,
                13, 14,
                14, 16,
                16, 17,
                17, 12,

                6, 7,
                7, 8,
                8, 9,
                9, 10,
                10, 11,
                11, 6,
                18, 19,
                19, 20,
                20, 21,
                21, 22,
                22, 23,
                23, 18,

                0, 12,
                1, 13,
                2, 14,
                4, 16,
                5, 17,

                6, 18,
                7, 19,
                8, 20,
                9, 21,
                10, 22,
                11, 23
        };
    }
}
