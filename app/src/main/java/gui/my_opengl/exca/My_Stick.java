package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.ExcavatorLib.coordB2;
import static packexcalib.exca.ExcavatorLib.coordST;
import static packexcalib.exca.ExcavatorLib.correctStick;
import static packexcalib.exca.ExcavatorLib.*;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;

import gui.my_opengl.Point3DF;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;

public class My_Stick {
    static Point3DF P0_S, P1_S, PA_S, PE_S, PM_S, PM1_S, PM2_S;
    static Point3DF P0_D, P1_D, PA_D, PE_D, PM_D, PM1_D, PM2_D;
    static double LA, LE, LM;
    static double mHdt;

    public static Point3DF[] puntiStick() {
        mHdt=hdt_BOOM;
        double larghezzaBraccio = (float) (Math.max(0.15, Math.min((DataSaved.L_Stick * 0.15f), 0.5)));
        double[] p0_s, p0_d, p1_s, p1_d, pa_s, pa_d, pe_s, pe_d, pm_s, pm_d, pm1_s, pm2_s, pm1_d, pm2_d;
        LA = DataSaved.L_Stick * 0.25;
        LE = DataSaved.L_Stick * 0.28;
        LM = DataSaved.L_Stick * 0.07;
        p0_s = Exca_Quaternion.endPoint(coordST, Deg_Boom_Roll, 0, larghezzaBraccio * 0.5, mHdt - 90);
        p0_d = Exca_Quaternion.endPoint(coordST, -Deg_Boom_Roll, 0, larghezzaBraccio * 0.5, mHdt + 90);
        pm_s = Exca_Quaternion.endPoint(p0_s, correctStick + 135, Deg_Boom_Roll, LM, mHdt);
        pm_d = Exca_Quaternion.endPoint(p0_d, correctStick + 135, Deg_Boom_Roll, LM, mHdt);

        pm1_s = Exca_Quaternion.endPoint(p0_s, correctStick - 135, Deg_Boom_Roll, LM, mHdt);
        pm1_d = Exca_Quaternion.endPoint(p0_d, correctStick - 135, Deg_Boom_Roll, LM, mHdt);


        p1_s = Exca_Quaternion.endPoint(coordB2, Deg_Boom_Roll, 0, larghezzaBraccio * 0.5, mHdt - 90);
        pm2_s = Exca_Quaternion.endPoint(p1_s, correctStick - 90, Deg_Boom_Roll, LM, mHdt);
        p1_d = Exca_Quaternion.endPoint(coordB2, -Deg_Boom_Roll, 0, larghezzaBraccio * 0.5, mHdt + 90);
        pm2_d = Exca_Quaternion.endPoint(p1_d, correctStick - 90, Deg_Boom_Roll, LM, mHdt);
        pe_s = Exca_Quaternion.endPoint(p1_s, correctStick + 105, Deg_Boom_Roll, LE, mHdt);
        pe_d = Exca_Quaternion.endPoint(p1_d, correctStick + 105, Deg_Boom_Roll, LE, mHdt);

        pa_s = Exca_Quaternion.endPoint(pm2_s, correctStick + 55 + 90, Deg_Boom_Roll, LA, mHdt);
        pa_d = Exca_Quaternion.endPoint(pm2_d, correctStick + 55 + 90, Deg_Boom_Roll, LA, mHdt);

        P0_S = pTransform(p0_s, DataSaved.glL_AnchorView, scale);
        PM_S = pTransform(pm_s, DataSaved.glL_AnchorView, scale);
        P0_D = pTransform(p0_d, DataSaved.glL_AnchorView, scale);
        PM_D = pTransform(pm_d, DataSaved.glL_AnchorView, scale);
        P1_S = pTransform(p1_s, DataSaved.glL_AnchorView, scale);
        P1_D = pTransform(p1_d, DataSaved.glL_AnchorView, scale);
        PA_S = pTransform(pa_s, DataSaved.glL_AnchorView, scale);
        PA_D = pTransform(pa_d, DataSaved.glL_AnchorView, scale);
        PE_S = pTransform(pe_s, DataSaved.glL_AnchorView, scale);
        PE_D = pTransform(pe_d, DataSaved.glL_AnchorView, scale);

        PM2_S = pTransform(pm2_s, DataSaved.glL_AnchorView, scale);
        PM2_D = pTransform(pm2_d, DataSaved.glL_AnchorView, scale);

        PM1_S = pTransform(pm1_s, DataSaved.glL_AnchorView, scale);
        PM1_D = pTransform(pm1_d, DataSaved.glL_AnchorView, scale);
        return new Point3DF[]{
                P0_S,
                PM_S,
                PE_S,
                PA_S,
                PM2_S,
                PM1_S,

                P0_D,
                PM_D,
                PE_D,
                PA_D,
                PM2_D,
                PM1_D


        };
    }

    public static short[] facceChiare() {
        return new short[]{
                0, 1, 5,
                1, 2, 4,
                4, 5, 1,
                2, 3, 4,

                6, 7, 11,
                7, 8, 10,
                10, 11, 7,
                8, 9, 10,

                1, 0, 6,
                6, 7, 1,
                2, 1, 7,
                7, 8, 2,
                3, 2, 8,
                8, 9, 3


        };

    }

    public static short[] facceScure() {
        return new short[]{
                4, 3, 9,
                9, 10, 4,
                5, 4, 10,
                10, 11, 5,
                0, 5, 11,
                11, 6, 0

        };

    }

    public static short[] contorno() {
        return new short[]{
                0,1,
                1,2,
                2,3,
                3,4,
                4,5,
                5,0,

                6,7,
                7,8,
                8,9,
                9,10,
                10,11,
                11,6,

                0,6,
                1,7,
                2,8,
                3,9,
                4,10,
                5,11

        };
    }


}
