package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.ExcavatorLib.coordB2;
import static packexcalib.exca.ExcavatorLib.coordinateDY;
import static packexcalib.exca.ExcavatorLib.correctBoom1;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;

import gui.my_opengl.Point3DF;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;

public class My_Boom1 {
    static Point3DF P0s, P1s, P2s, P3s, P4s, P5s, P6s, P7s, P8s, P9s, P10s;
    static Point3DF P0d, P1d, P2d, P3d, P4d, P5d, P6d, P7d, P8d, P9d, P10d;
    static double Spessore_P, L1, L2, L3, L4, L5;

    public static Point3DF[] puntiBoom() {
        double[] p0s, p1s, p2s, p3s, p4s, p5s, p6s, p7s, p8s, p9s, p10s;
        double[] p0d, p1d, p2d, p3d, p4d, p5d, p6d, p7d, p8d, p9d, p10d;
        double larghezzaBraccio_LARGA = (float) (Math.max(0.30, Math.min((DataSaved.L_Stick * 0.15f) * 2, 0.8)));
        double larghezzaBraccio_STRETTA = (float) (Math.max(0.22, Math.min((DataSaved.L_Stick * 0.22f), 0.6)));
        Spessore_P = (float) (Math.max(0.05, Math.min((DataSaved.L_Boom1 * 0.01f) * 2, 0.1)));
        L1 = DataSaved.L_Boom1 * 0.5;
        L2 = DataSaved.L_Boom1 * 0.2;
        L3 = DataSaved.L_Boom1 * 0.3;
        L4 = DataSaved.L_Boom1 * 0.28;
        L5 = DataSaved.L_Boom1 * 0.58;

        p0s = Exca_Quaternion.endPoint(coordB2, Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, hdt_BOOM - 90);
        p6s = Exca_Quaternion.endPoint(coordinateDY, Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, hdt_BOOM - 90);
        p0d = Exca_Quaternion.endPoint(coordB2, -Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, hdt_BOOM + 90);
        p6d = Exca_Quaternion.endPoint(coordinateDY, -Deg_Boom_Roll, 0, larghezzaBraccio_STRETTA * 0.5, hdt_BOOM + 90);

        //latpo sx
        p1s = Exca_Quaternion.endPoint(p0s, correctBoom1 + 100, Deg_Boom_Roll, Spessore_P, hdt_BOOM);
        p2s = Exca_Quaternion.endPoint(p0s, correctBoom1 + 150, Deg_Boom_Roll, L1, hdt_BOOM);
        p3s = Exca_Quaternion.endPoint(p2s, correctBoom1 + 165, Deg_Boom_Roll, L2, hdt_BOOM);
        p4s = Exca_Quaternion.endPoint(p6s, correctBoom1 + 55, Deg_Boom_Roll, L3, hdt_BOOM);
        p5s = Exca_Quaternion.endPoint(p6s, correctBoom1 + 90, Deg_Boom_Roll, Spessore_P, hdt_BOOM);
        p7s = Exca_Quaternion.endPoint(p6s, correctBoom1 - 35, Deg_Boom_Roll, Spessore_P, hdt_BOOM);
        p8s = Exca_Quaternion.endPoint(p6s, correctBoom1 + 27, Deg_Boom_Roll, L4, hdt_BOOM);
        p10s = Exca_Quaternion.endPoint(p0s, correctBoom1 - 120, Deg_Boom_Roll, Spessore_P, hdt_BOOM);
        p9s = Exca_Quaternion.endPoint(p10s, correctBoom1 + 164, Deg_Boom_Roll, L5, hdt_BOOM);

        //lato dx
        p1d = Exca_Quaternion.endPoint(p0d, correctBoom1 + 100, Deg_Boom_Roll, Spessore_P, hdt_BOOM);
        p2d = Exca_Quaternion.endPoint(p0d, correctBoom1 + 150, Deg_Boom_Roll, L1, hdt_BOOM);
        p3d = Exca_Quaternion.endPoint(p2d, correctBoom1 + 165, Deg_Boom_Roll, L2, hdt_BOOM);
        p4d = Exca_Quaternion.endPoint(p6d, correctBoom1 + 55, Deg_Boom_Roll, L3, hdt_BOOM);
        p5d = Exca_Quaternion.endPoint(p6d, correctBoom1 + 90, Deg_Boom_Roll, Spessore_P, hdt_BOOM);
        p7d = Exca_Quaternion.endPoint(p6d, correctBoom1 - 35, Deg_Boom_Roll, Spessore_P, hdt_BOOM);
        p8d = Exca_Quaternion.endPoint(p6d, correctBoom1 + 27, Deg_Boom_Roll, L4, hdt_BOOM);
        p10d = Exca_Quaternion.endPoint(p0d, correctBoom1 - 120, Deg_Boom_Roll, Spessore_P, hdt_BOOM);
        p9d = Exca_Quaternion.endPoint(p10d, correctBoom1 + 164, Deg_Boom_Roll, L5, hdt_BOOM);

        P0s = pTransform(p0s, DataSaved.glL_AnchorView, scale);
        P1s = pTransform(p1s, DataSaved.glL_AnchorView, scale);
        P2s = pTransform(p2s, DataSaved.glL_AnchorView, scale);
        P3s = pTransform(p3s, DataSaved.glL_AnchorView, scale);
        P4s = pTransform(p4s, DataSaved.glL_AnchorView, scale);
        P5s = pTransform(p5s, DataSaved.glL_AnchorView, scale);
        P6s = pTransform(p6s, DataSaved.glL_AnchorView, scale);
        P7s = pTransform(p7s, DataSaved.glL_AnchorView, scale);
        P8s = pTransform(p8s, DataSaved.glL_AnchorView, scale);
        P9s = pTransform(p9s, DataSaved.glL_AnchorView, scale);
        P10s = pTransform(p10s, DataSaved.glL_AnchorView, scale);

        P0d = pTransform(p0d, DataSaved.glL_AnchorView, scale);
        P1d = pTransform(p1d, DataSaved.glL_AnchorView, scale);
        P2d = pTransform(p2d, DataSaved.glL_AnchorView, scale);
        P3d = pTransform(p3d, DataSaved.glL_AnchorView, scale);
        P4d = pTransform(p4d, DataSaved.glL_AnchorView, scale);
        P5d = pTransform(p5d, DataSaved.glL_AnchorView, scale);
        P6d = pTransform(p6d, DataSaved.glL_AnchorView, scale);
        P7d = pTransform(p7d, DataSaved.glL_AnchorView, scale);
        P8d = pTransform(p8d, DataSaved.glL_AnchorView, scale);
        P9d = pTransform(p9d, DataSaved.glL_AnchorView, scale);
        P10d = pTransform(p10d, DataSaved.glL_AnchorView, scale);
        return new Point3DF[]{
                P0s, P1s, P2s, P3s, P4s, P5s, P6s, P7s, P8s, P9s, P10s,
                P0d, P1d, P2d, P3d, P4d, P5d, P6d, P7d, P8d, P9d, P10d
        };

    }

    public static short[] indici1() {

        return new short[]{
                0, 1, 10,
                1, 2, 9,
                1, 9, 10,
                2, 3, 8,
                2, 8, 9,
                3, 4, 8,
                4, 5, 8,
                5, 7, 8,
                5, 6, 7,

                11, 12, 21,
                12, 13, 20,
                12, 20, 21,
                13, 14, 19,
                13, 19, 20,
                14, 15, 19,
                15, 16, 19,
                16, 18, 19,
                16, 17, 18,

                0, 11, 1,
                11, 12, 1,
                1, 12, 2,
                12, 13, 2,
                2, 13, 3,
                13, 14, 3
        };

    }

    public static short[] indici2() {
        return new short[]{
                3,14,4,
                14,15,4,
                4,15,5,
                15,16,5,
                5,16,6,
                16,17,6,
                6,17,7,
                17,18,7,
                7,18,8,
                18,19,8,
                8,19,9,
                19,20,9,
                9,20,10,
                20,21,10,
                10,21,0,
                21,11,0
        };

    }

    public static short[] contorno() {
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
                9, 10,
                10, 0,

                11, 12,
                12, 13,
                13, 14,
                14, 15,
                15, 16,
                16, 17,
                17, 18,
                18, 19,
                19, 20,
                20, 21,
                21, 11,

                1, 12,
                2, 13,
                3, 14,
                4, 15,
                5, 16,
                7, 18,
                8, 19,
                9, 20
        };
    }
}
