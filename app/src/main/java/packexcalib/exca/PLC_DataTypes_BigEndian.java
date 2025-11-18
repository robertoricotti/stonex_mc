package packexcalib.exca;

import android.util.Log;

public class PLC_DataTypes_BigEndian {
     /*
    METODI per convertire un array di byte in formato PLC S16-U16-S32-U32 in BIG ENDIAN
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

    public static short byte_to_S16_be(byte[] data) {
        if (data == null) {
            return -1;
        } else {
            return (short) ((data[0] & 0xff) << 8 | (data[1] & 0xff));
        }
    }

    public static int byte_to_U16_be(byte[] data) {
        if (data == null) {
            return -1;
        } else {
            if (data.length != 2) {
                Log.e("Dig_Err","Il byte array deve avere esattamente 2 elementi.");
            }
            int value = ((data[0] & 0xff) << 8) | (data[1] & 0xff);
            return value & 0xffff; // maschera per ottenere solo i primi 16 bit
        }
    }

    public static int byte_to_S32_be(byte[] bytes) {
        if (bytes.length != 4) {
            Log.e("Dig_Err","Il byte array deve avere esattamente 4 elementi.");

        }
        int value = ((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);
        return value;
    }
    public static long byte_to_U32_be(byte[] bytes) {
        if (bytes.length != 4) {
            Log.e("DIG_Err","Il byte array deve avere esattamente 4 elementi.");

        }
        long value = ((bytes[0] & 0xffL) << 24) | ((bytes[1] & 0xffL) << 16) | ((bytes[2] & 0xffL) << 8) | (bytes[3] & 0xffL);
        return value & 0xffffffffL; // maschera per ottenere solo i primi 32 bit
    }

    public static long byte_to_S64_be(byte[] bytes) {
        if (bytes.length != 8) {
            Log.e("DIG_Err","L'array deve avere 8 byte.");
        }
        long value = 0;
        for (int i = 7; i >= 0; i--) {
            value |= ((long) (bytes[i] & 0xff)) << (8 * (7 - i));
        }
        return value;
    }

    public static byte[] S16_to_bytes_be(short value) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (value & 0xff);
        bytes[0] = (byte) ((value >> 8) & 0xff);
        return bytes;
    }
    public static byte[] U16_to_bytes_be(int value) {
        if (value < 0 || value > 65535) {
            Log.e("DIG_Err","Il valore deve essere compreso tra 0 e 65535.");
        }
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (value & 0xff);
        bytes[0] = (byte) ((value >> 8) & 0xff);
        return bytes;
    }

    public static byte[] S32_to_bytes_be(int value) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (value & 0xff);
        bytes[2] = (byte) ((value >> 8) & 0xff);
        bytes[1] = (byte) ((value >> 16) & 0xff);
        bytes[0] = (byte) ((value >> 24) & 0xff);
        return bytes;
    }

    public static byte[] U32_to_bytes_be(long value) {
        if (value < 0 || value > 4294967295L) {
            Log.e("DIG_Err","Il valore deve essere compreso tra 0 e 4294967295.");
        }
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (value & 0xff);
        bytes[2] = (byte) ((value >> 8) & 0xff);
        bytes[1] = (byte) ((value >> 16) & 0xff);
        bytes[0] = (byte) ((value >> 24) & 0xff);
        return bytes;
    }

    public static byte[] S64_to_bytes_be(long value) {
        byte[] bytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) ((value >> (8 * (7 - i))) & 0xff);
        }
        return bytes;
    }

    public static byte[] U64_to_bytes_be(long value) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (value & 0xff);
            value >>>= 8;
        }
        return bytes;
    }

    public static boolean[] U8_to_bitmask_be(byte b) {
        boolean[] result = new boolean[8];
        for (int i = 0; i < 8; i++) {
            result[i] = ((b >> i) & 1) == 1;
        }
        return result;
        /*
        esempio di utilizzo sotto..
        public static void main (String[]args){
            byte b = 0x1;
            boolean b0, b1, b2, b3, b4, b5, b6, b7;
            boolean[] bits = byteToBooleanArray(b);
            b0 = bits[0];
            b1 = bits[1];
            b2 = bits[2];
            b3 = bits[3];
            b4 = bits[4];
            b5 = bits[5];
            b6 = bits[6];
            b7 = bits[7];
            b0: false

            System.out.println("b0: " + b0);1
            System.out.println("b1: " + b1);0
            System.out.println("b2: " + b2);0
            System.out.println("b3: " + b3);0
            System.out.println("b4: " + b4);0
            System.out.println("b5: " + b5);0
            System.out.println("b6: " + b6);0
            System.out.println("b7: " + b7);0
            */

    }
    public static boolean[] U8_to_bitmask_4_be(byte b) {
        boolean[] result = new boolean[4];
        for (int i = 0; i < 4; i++) {
            result[i] = ((b >> i) & 1) == 1;
        }
        return result;


    }

    public static byte Encode_8_bool_be(boolean[] booleans) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            result |= (booleans[i] ? 1 : 0) << i;
        }
        return (byte) (result & 0xFF);
    }
    public static byte Encode_4_bool_be(boolean[] booleans) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result |= (booleans[i] ? 1 : 0) << i;
        }
        return (byte) (result & 0xFF);
    }







    public static long fiveBytesToLong(byte[] bytes) {
        if (bytes.length != 5) throw new IllegalArgumentException("Servono 5 byte");
        return ((long)(bytes[0] & 0xFF) << 32) |
                ((long)(bytes[1] & 0xFF) << 24) |
                ((long)(bytes[2] & 0xFF) << 16) |
                ((long)(bytes[3] & 0xFF) << 8)  |
                ((long)(bytes[4] & 0xFF));
    }




}
