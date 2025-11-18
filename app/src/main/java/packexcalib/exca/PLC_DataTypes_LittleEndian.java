package packexcalib.exca;

import android.util.Log;

import java.math.BigInteger;

public class PLC_DataTypes_LittleEndian {

    /*
    METODI per convertire un array di byte in formato PLC S16-U16-S32-U32 in LITTLE ENDIAN
    PLC DATA TYPES:

    S8= -128/+127
    U8= 0/255
    S16= -32768/+32767
    U16= 0/65535
    U32= 0/4294967295
    S32= -2147483648/+2147483647
    U64= 0/+2^64-1
    S64= -2^32/+2^32-1



     */

    public static short byte_to_S16(byte[] data) {
        if (data == null) {
            return -1;
        } else {
            return (short) ((data[1] & 0xff) << 8 | (data[0] & 0xff));
        }
    }

    public static int byte_to_U16(byte[] data) {
        if (data == null) {
            return -1;
        } else {
            if (data.length != 2) {
                Log.e("DIG_Err","Il byte array deve avere esattamente 2 elementi.");
            }
            int value = ((data[1] & 0xff) << 8) | (data[0] & 0xff);
            return value & 0xffff; // maschera per ottenere solo i primi 16 bit
        }
    }

    public static int byte_to_S32(byte[] bytes) {
        if (bytes.length != 4) {
            Log.e("DIG_Err","Il byte array deve avere esattamente 4 elementi.");
        }
        int value = (bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
        return value;
    }


    public static long byte_to_U32(byte[] bytes) {
        if (bytes.length != 4) {
            Log.e("DIG_Err","Il byte array deve avere esattamente 4 elementi.");
        }
        long value = ((bytes[3] & 0xffL) << 24) | ((bytes[2] & 0xffL) << 16) | ((bytes[1] & 0xffL) << 8) | (bytes[0] & 0xffL);
        return value & 0xffffffffL; // maschera per ottenere solo i primi 32 bit
    }

    public static long byte_to_S64_le(byte[] bytes) {

        if (bytes.length != 8) {
            Log.e("DIG_Err","L'array deve avere 8 byte.");


        }
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (bytes[i] & 0xff)) << (8 * i);
        }
        return value;

    }

    public static BigInteger byte_to_U64(byte[] bytes) {
        if (bytes.length != 8) {
            Log.e("DIG_Err","L'array deve avere 8 byte.");

        }
        BigInteger value = BigInteger.ZERO;
        for (int i = 0; i < 8; i++) {
            BigInteger b = BigInteger.valueOf(bytes[i] & 0xff);
            value = value.shiftLeft(8).or(b);
        }
        return value;
    }


    /*
    METODI per convertire un DATA TYPE PLC S16-U16-S32-U32 in array di byte LITTLE ENDIAN
    */


    public static byte[] S16_to_bytes(short value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value & 0xff);
        bytes[1] = (byte) ((value >> 8) & 0xff);
        return bytes;
    }


    public static byte[] U16_to_bytes(int value) {
        if (value < 0 || value > 65535) {
            Log.e("DIG_Err","Il valore deve essere compreso tra 0 e 65535.");

        }
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value & 0xff);
        bytes[1] = (byte) ((value >> 8) & 0xff);
        return bytes;
    }

    public static byte[] S32_to_bytes(int value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (value & 0xff);
        bytes[1] = (byte) ((value >> 8) & 0xff);
        bytes[2] = (byte) ((value >> 16) & 0xff);
        bytes[3] = (byte) ((value >> 24) & 0xff);
        return bytes;
    }

    public static byte[] U32_to_bytes(long value) {
        if (value < 0 || value > 4294967295L) {
            Log.e("DIG_Err","Il valore deve essere compreso tra 0 e 4294967295.");

        }
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (value & 0xff);
        bytes[1] = (byte) ((value >> 8) & 0xff);
        bytes[2] = (byte) ((value >> 16) & 0xff);
        bytes[3] = (byte) ((value >> 24) & 0xff);
        return bytes;
    }

    public static byte[] S64_to_bytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) ((value >> (8 * i)) & 0xff);
        }
        return bytes;

        /*
        byte[] bytes = convertSignedLongToLittleEndianByteArray(value);
        for (byte b : bytes) {
         System.out.printf("%02x ", b);
}          esempio di utilizzo
         */
    }
    public static byte[] U64_to_bytes(long value) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (value & 0xff);
            value >>>= 8;
        }
        // Reverse the byte order to convert from big-endian to little-endian
        byte temp;
        for (int i = 0; i < 4; i++) {
            temp = bytes[i];
            bytes[i] = bytes[7 - i];
            bytes[7 - i] = temp;
        }
        return bytes;
    }



    /////booleans///
    public static boolean[] U8_to_bitmask(byte b) {
        boolean[] result = new boolean[8];
        for (int i = 0; i < 8; i++) {
            result[i] = ((b >> (7 - i)) & 1) == 1;
        }
        return result;
    }
    public static boolean[] U8_to_bitmask_4(byte b) {
        boolean[] result = new boolean[4];
        for (int i = 0; i < 4; i++) {
            result[i] = ((b >> (3 - i)) & 1) == 1;
        }
        return result;
    }
    public static byte Encode_4_bool(boolean[] booleans) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result |= (booleans[i] ? 1 : 0) << (3 - i);
        }
        return (byte) (result & 0xFF);
    }

    public static byte Encode_8_bool(boolean[] booleans) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            result |= (booleans[i] ? 1 : 0) << (7 - i);
        }
        return (byte) (result & 0xFF);
    }

    public static long fiveBytesToLongLE(byte[] bytes) {
        if (bytes.length != 5) throw new IllegalArgumentException("Servono 5 byte");
        return ((long)(bytes[4] & 0xFF) << 32) |
                ((long)(bytes[3] & 0xFF) << 24) |
                ((long)(bytes[2] & 0xFF) << 16) |
                ((long)(bytes[1] & 0xFF) << 8)  |
                ((long)(bytes[0] & 0xFF));
    }


    }
