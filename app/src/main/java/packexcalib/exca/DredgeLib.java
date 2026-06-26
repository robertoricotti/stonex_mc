package packexcalib.exca;

import static packexcalib.exca.DataSaved.Altezza_Attrezzo;
import static packexcalib.exca.DataSaved.Delta_X_Draga;
import static packexcalib.exca.DataSaved.Delta_X_Pontone;
import static packexcalib.exca.DataSaved.Delta_Y_Draga;
import static packexcalib.exca.DataSaved.Delta_Y_Pontone;
import static packexcalib.exca.DataSaved.Delta_Z_Draga;
import static packexcalib.exca.DataSaved.Delta_Z_Pontone;
import static packexcalib.exca.DataSaved.Larghezza_Pontone;
import static packexcalib.exca.DataSaved.Lunghezza_Braccio;
import static packexcalib.exca.DataSaved.Lunghezza_Pitch;
import static packexcalib.exca.DataSaved.Lunghezza_Pontone;
import static packexcalib.exca.DataSaved.Lunghezza_Roll;
import static packexcalib.exca.DataSaved.Rope_Fixed_Offset;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.coordB1;
import static packexcalib.exca.ExcavatorLib.coordB2;
import static packexcalib.exca.ExcavatorLib.coordST;
import static packexcalib.exca.ExcavatorLib.coordinateDX;
import static packexcalib.exca.ExcavatorLib.coordinateDY;
import static packexcalib.exca.ExcavatorLib.coordinateDZ;
import static packexcalib.exca.ExcavatorLib.correctPitch;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static packexcalib.exca.ExcavatorLib.overturn;
import static packexcalib.exca.ExcavatorLib.startXYZ;
import static packexcalib.exca.Sensors_Decoder_Dredge.Angolo_Slew_Dredge;
import static packexcalib.exca.Sensors_Decoder_Dredge.Lunghezza_Fune;

import android.util.Log;

import packexcalib.gnss.NmeaListener;

public class DredgeLib {
    public static double correctDredgePitch, correctDredgeRoll, correctDredgeBoom;
    public static double[]
            centroRalla = new double[]{0, 0, 0},
            spigolo_Asx = new double[]{0, 0, 0},
            spigolo_Adx = new double[]{0, 0, 0},
            spigolo_Bdx = new double[]{0, 0, 0},
            spigolo_Bsx = new double[]{0, 0, 0},
            spigolo_Asx_BOTTOM = new double[]{0, 0, 0},
            spigolo_Adx_BOTTOM = new double[]{0, 0, 0},
            spigolo_Bdx_BOTTOM = new double[]{0, 0, 0},
            spigolo_Bsx_BOTTOM = new double[]{0, 0, 0};


    static double[] topEdge = new double[]{0, 0, 0};

    static final double spessorePontone = 2.0d;

    public static void Dredge() {
        try {
            startXYZ = new double[]{NmeaListener.Est1, NmeaListener.Nord1, NmeaListener.Quota1};
            correctDredgePitch = Offset_Applier.realPitch_Drg(DataSaved.offsetPitch);
            correctDredgeRoll = Offset_Applier.realRoll_Drg(DataSaved.offsetRoll);
            correctDredgeBoom = Offset_Applier.realBoom_Drg(DataSaved.offsetBoom1);

            double hdt0 = normalizeDeg360(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
            double hdtR = normalizeDeg360(hdt0 + 90);
            double hdtL = normalizeDeg360(hdt0 - 90);
            double hdtReverse = normalizeDeg360(hdt0 + 180);
            hdt_BOOM = hdt0;

            coordinateDZ = Exca_Quaternion.endPoint(startXYZ, correctDredgePitch - 90, correctDredgeRoll, Delta_Z_Draga, hdt0);

            if (Delta_X_Draga < 0) {
                coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, correctDredgeRoll, -correctDredgePitch, Delta_X_Draga, hdtL);
            } else {
                coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, -correctDredgeRoll, correctDredgePitch, Delta_X_Draga, hdtR);
            }

            if (Delta_Y_Draga < 0) {
                coordinateDY = Exca_Quaternion.endPoint(coordinateDX, -correctDredgePitch, -correctDredgeRoll, Delta_Y_Draga, hdtReverse);
            } else {
                coordinateDY = Exca_Quaternion.endPoint(coordinateDX, correctDredgePitch, correctDredgeRoll, Delta_Y_Draga, hdt0);
            }

            overturn = Math.abs(correctDredgeRoll) > 85.0d || Math.abs(correctDredgePitch) > 85.0d;

            coordB1 = Exca_Quaternion.endPoint(coordinateDY, correctDredgeBoom, correctDredgeRoll, Lunghezza_Braccio, hdt_BOOM);
            coordB2 = coordB1;
            coordST = Exca_Quaternion.endPoint(coordB1, -90, 0, Lunghezza_Fune + Rope_Fixed_Offset, hdt_BOOM);
            ExcavatorLib.bucketCoord = Exca_Quaternion.endPoint(coordST, -90, 0, Altezza_Attrezzo, hdt_BOOM);
            ExcavatorLib.bucketRightCoord = bucketCoord;
            ExcavatorLib.bucketLeftCoord = bucketCoord;

            if (Lunghezza_Roll > 0) {
                ExcavatorLib.coordRoll = Exca_Quaternion.endPoint(coordinateDY, correctDredgeRoll, -correctDredgePitch, Lunghezza_Roll, hdtL);
            } else {
                ExcavatorLib.coordRoll = Exca_Quaternion.endPoint(coordinateDY, -correctDredgeRoll, correctDredgePitch, Lunghezza_Roll, hdtR);
            }

            if (Lunghezza_Pitch > 0) {
                ExcavatorLib.coordPitch = Exca_Quaternion.endPoint(ExcavatorLib.coordRoll, -correctDredgePitch, -correctDredgeRoll, Lunghezza_Pitch, hdtReverse);
            } else {
                ExcavatorLib.coordPitch = Exca_Quaternion.endPoint(ExcavatorLib.coordRoll, correctDredgePitch, correctDredgeRoll, Lunghezza_Pitch, hdt0);
            }


            centroRalla = ExcavatorLib.coordPitch;
            calcolaPontone(hdt_BOOM, hdtL, hdtR, hdtReverse);
        } catch (Exception e) {
            Log.e("Dredge", Log.getStackTraceString(e));
        }
    }

    private static void calcolaPontone(double hdt0, double hdtL, double hdtR, double hdtReverse) {
        if (Lunghezza_Pontone <= 0.0d || Larghezza_Pontone <= 0.0d) {
            resetPontone();
            return;
        }

        double[] riferimentoPontone = Exca_Quaternion.endPoint(
                centroRalla,
                correctDredgePitch - 90,
                correctDredgeRoll,
                Delta_Z_Pontone,
                hdt0
        );

        topEdge = Exca_Quaternion.endPoint(riferimentoPontone, 0, 0, Delta_Y_Pontone, hdt0 - Angolo_Slew_Dredge);
        spigolo_Asx = Exca_Quaternion.endPoint(topEdge, 0, 0, Delta_X_Pontone, hdtL - Angolo_Slew_Dredge);
        spigolo_Adx = Exca_Quaternion.endPoint(spigolo_Asx, 0, 0, Larghezza_Pontone, hdtR - Angolo_Slew_Dredge);
        spigolo_Bdx = Exca_Quaternion.endPoint(spigolo_Adx, 0, 0, Lunghezza_Pontone, hdtReverse - Angolo_Slew_Dredge);
        spigolo_Bsx = Exca_Quaternion.endPoint(spigolo_Asx, 0, 0, Lunghezza_Pontone, hdtReverse - Angolo_Slew_Dredge);

        spigolo_Asx_BOTTOM = new double[]{spigolo_Asx[0], spigolo_Asx[1], spigolo_Asx[2] - spessorePontone};
        spigolo_Adx_BOTTOM = new double[]{spigolo_Adx[0], spigolo_Adx[1], spigolo_Adx[2] - spessorePontone};
        spigolo_Bdx_BOTTOM = new double[]{spigolo_Bdx[0], spigolo_Bdx[1], spigolo_Bdx[2] - spessorePontone};
        spigolo_Bsx_BOTTOM = new double[]{spigolo_Bsx[0], spigolo_Bsx[1], spigolo_Bsx[2] - spessorePontone};
    }

    private static void resetPontone() {
        topEdge = new double[]{0, 0, 0};
        spigolo_Asx = new double[]{0, 0, 0};
        spigolo_Adx = new double[]{0, 0, 0};
        spigolo_Bdx = new double[]{0, 0, 0};
        spigolo_Bsx = new double[]{0, 0, 0};
        spigolo_Asx_BOTTOM = new double[]{0, 0, 0};
        spigolo_Adx_BOTTOM = new double[]{0, 0, 0};
        spigolo_Bdx_BOTTOM = new double[]{0, 0, 0};
        spigolo_Bsx_BOTTOM = new double[]{0, 0, 0};
    }

    private static double normalizeDeg360(double deg) {
        double out = deg % 360.0d;
        if (out < 0.0d) out += 360.0d;
        return out;
    }
}
