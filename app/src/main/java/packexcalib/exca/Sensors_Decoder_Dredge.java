package packexcalib.exca;

import static packexcalib.exca.DataSaved.Diametro_Ralla;
import static packexcalib.exca.DataSaved.Diametro_Ruotino_Dredge;
import static packexcalib.exca.DataSaved.Diametro_Tamburo;
import static packexcalib.exca.DataSaved.Pos_BOOM;
import static packexcalib.exca.DataSaved.Pos_ENCODER;
import static packexcalib.exca.DataSaved.Pos_FRAME;
import static packexcalib.exca.DataSaved.Pos_SLEW;
import static packexcalib.exca.Sensors_Decoder_Drill.ropeLenSignedFromAbsolute;
import static packexcalib.exca.Sensors_Decoder_Drill.slewAngleDegFromZeroedEncoder;
import static utils.MyTypes.LIEBHERR_CRANE;
import static utils.MyTypes.SENNEBOGHEN;
import static utils.MyTypes.STONEX_SENSORS;

import android.util.Log;

public class Sensors_Decoder_Dredge {
    private static final int LIEBHERR_ROPE_LENGTH = 0x18FF8080;
    private static final int LIEBHERR_MACHINE = 0x18FF8380;
    private static final int SENNEBOGEN_SLEW = 0x1C044333;
    private static final int SENNEBOGEN_BOOM_ANGLE = 0x1C034333;
    private static final int SENNEBOGEN_ROPE = 0x1F8;
    private static final int SENNEBOGEN_HOOK = 0x3F8;
    public static double Angolo_Pitch_Dredge;
    public static double Angolo_Roll_Dredge;
    public static double Angolo_Braccio_Dredge;
    public static double Angolo_Fune_Dredge = 0d;
    public static double Angolo_Slew_Dredge;
    public static double Lunghezza_Fune;
    public static double Angolo_Attrezzo_Dredge=0d;


    public static double Lunghezza_Fune_2;






    public static void decode(int id, byte[] data) {
        try {
            switch (DataSaved.Dredge_Interface_Type) {
                case STONEX_SENSORS:

                    switch (id) {
                        case 0x381:
                            double[] out = TiltEncript.encriptTSM_Frame(data, Pos_FRAME);
                            Angolo_Pitch_Dredge = out[0];
                            Angolo_Roll_Dredge = out[1];
                            break;
                        case 0x382:
                            Angolo_Braccio_Dredge = TiltEncript.encriptTSM_Boom(data, Pos_BOOM)[0];
                            break;
                        case 0x18F:
                            // Encoder connected 8192 count per revolution FULL SCALE= 0x20000000(536870912)
                            long revolutionF = PLC_DataTypes_LittleEndian.byte_to_U32(new byte[]{data[0], data[1], data[2], data[3]});
                            Angolo_Slew_Dredge = slewAngleDegFromZeroedEncoder(revolutionF, Diametro_Ruotino_Dredge, Diametro_Ralla, Pos_SLEW);
                            break;
                        case 0x190:
                            // Encoder connected 8192 count per revolution FULL SCALE= 0x20000000(536870912)
                            long revolution = PLC_DataTypes_LittleEndian.byte_to_U32(new byte[]{data[0], data[1], data[2], data[3]});
                            Lunghezza_Fune = ropeLenSignedFromAbsolute(revolution, Diametro_Tamburo, Pos_ENCODER);
                            break;

                    }
                    DredgeLib.Dredge();
                    break;

                case LIEBHERR_CRANE:

                    if (data == null || data.length < 8) {
                        return;
                    }

                    // Utile se la libreria CAN ti passa anche il bit "extended frame"
                    final int canId29 = id & 0x1FFFFFFF;

                    switch (canId29) {

                        case LIEBHERR_MACHINE:
                            // Byte 0-1: slewing gear angle, unsigned, 0.01 deg
                            Angolo_Slew_Dredge = u16le(data, 0) * 0.01d;

                            // Byte 2-3: boom angle, unsigned, 0.01 deg
                            Angolo_Braccio_Dredge = u16le(data, 2) * 0.01d;

                            // Byte 4-5: superstructure X, signed, 0.01 deg
                            double superstructureX_deg = s16le(data, 4) * 0.01d;

                            // Byte 6-7: superstructure Y, signed, 0.01 deg
                            double superstructureY_deg = s16le(data, 6) * 0.01d;

                            /*
                             * ATTENZIONE:
                             * Liebherr nel documento li chiama X e Y, non pitch/roll
                             */
                            Angolo_Pitch_Dredge = -superstructureY_deg;
                            Angolo_Roll_Dredge = superstructureX_deg;
                            break;

                        case LIEBHERR_ROPE_LENGTH:
                            // Byte 0-1: rope_length_hg1, signed, 0.01 m.
                            // Conversione: raw * 0.01 m * 1000 = raw * 10 mm
                            Lunghezza_Fune = s16le(data, 0) * 0.01d;

                            // Se ti servono anche le altre due:
                            Lunghezza_Fune_2 = s16le(data, 2) * 0.01d;
                            // double grabHeight_mm        = s16le(data, 4) * 10.0d;
                            break;
                    }
                    DredgeLib.Dredge();
                    break;

                case SENNEBOGHEN:

                    if (data == null || data.length < 8) {
                        return;
                    }

                    final int canId = id & 0x1FFFFFFF;

                    switch (canId) {
                        case 0x381:
                            double[] out = TiltEncript.encriptTSM_Frame(data, Pos_FRAME);
                            Angolo_Pitch_Dredge = out[0];
                            Angolo_Roll_Dredge = out[1];
                            break;
                        case 0x382:
                            Angolo_Braccio_Dredge = TiltEncript.encriptTSM_Boom(data, Pos_BOOM)[0];
                            break;

                        case SENNEBOGEN_SLEW:
                            /*
                             * InterfacePositionData.sym:
                             * DisplayUppercarriageSlewAngle unsigned 48,16
                             * unit deg, factor 0.01, offset -180
                             *
                             * Byte 48 = byte 6.
                             */
                            double slewDegSigned = u16le(data, 6) * 0.01d - 180.0d;

                            // Coerente con Liebherr/local system: 0..360 deg
                            Angolo_Slew_Dredge = normalizeDeg360(slewDegSigned);

                            // Se invece vuoi il valore nativo Sennebogen -180..+180 circa:
                            // Angolo_Slew_Dredge = slewDegSigned;
                            break;

                        case SENNEBOGEN_BOOM_ANGLE:
                            /*
                             * InterfacePositionData.sym:
                             * DisplayMainBoomAngle unsigned 0,16
                             * unit deg, factor 0.01, offset -200
                             */
                            Angolo_Braccio_Dredge = u16le(data, 0) * 0.01d - 200.0d;
                            break;

                        case SENNEBOGEN_ROPE:
                            /*
                             * InterfacePositionData_Machine.sym:
                             * u16_RopeLength1_cm unsigned 0,16 /u:cm
                             * u16_RopeLength2_cm unsigned 16,16 /u:cm
                             *
                             * 1 count = 1 cm = 10 mm
                             */
                            double ropeLength1_mm = u16le(data, 0) * 10.0d;
                            double ropeLength2_mm = u16le(data, 2) * 10.0d;

                            // Equivalente alla fune principale / hoist, da usare come Lunghezza_Fune
                            Lunghezza_Fune = ropeLength1_mm * 0.001;
                            Lunghezza_Fune_2 = ropeLength2_mm * 0.001;

                            // Se ti serve anche la seconda:
                            // Lunghezza_Fune_2 = ropeLength2_mm;
                            break;

                        case SENNEBOGEN_HOOK:
                            /*
                             * Opzionale:
                             * s32_HookHeight1_cm 0,32
                             * s32_HookHeight2_cm 32,32
                             *
                             * Nel .sym il type è scritto "unsigned", ma il nome è s32.
                             * Per altezze/differenze io lo tratterei come signed 32 bit.
                             */
                            double hookHeight1_mm = s32le(data, 0) * 10.0d;
                            double hookHeight2_mm = s32le(data, 4) * 10.0d;

                            // Se hai una variabile dedicata:
                            // Altezza_Hook_1 = hookHeight1_mm;
                            // Altezza_Hook_2 = hookHeight2_mm;
                            break;
                    }
                    DredgeLib.Dredge();
                    break;
            }
        } catch (Exception e) {
            Log.e("Dredge", Log.getStackTraceString(e));
        }


    }

    private static long u32le(byte[] d, int off) {
        return ((long) d[off] & 0xFF)
                | (((long) d[off + 1] & 0xFF) << 8)
                | (((long) d[off + 2] & 0xFF) << 16)
                | (((long) d[off + 3] & 0xFF) << 24);
    }

    private static int s32le(byte[] d, int off) {
        return (int) u32le(d, off);
    }

    private static double normalizeDeg360(double deg) {
        double out = deg % 360.0d;
        if (out < 0.0d) out += 360.0d;
        return out;
    }

    private static int u16le(byte[] d, int off) {
        return (d[off] & 0xFF) | ((d[off + 1] & 0xFF) << 8);
    }

    private static int s16le(byte[] d, int off) {
        return (short) u16le(d, off);
    }

}
