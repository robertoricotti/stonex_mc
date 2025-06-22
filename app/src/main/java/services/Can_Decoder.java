package services;

import android.util.Log;

import java.util.Arrays;

import packexcalib.exca.PLC_DataTypes_BigEndian;


public class Can_Decoder {
    int messageType;

    public static int counter;
    public static int mID;//ID CAN Rx
    public static int len;//DLC CAN Rx


    public static byte[] msgFrame;//corpo dati CAN Rx


    public Can_Decoder(byte[] msg) {
        try {
            messageType = (int) msg[0];
            len = (int) msg[1] - 3;
            mID = PLC_DataTypes_BigEndian.byte_to_U16_be(new byte[]{msg[3], msg[4]});
            msgFrame = new byte[len];
            System.arraycopy(msg, 5, msgFrame, 0, len);
            //CanService_Other.OnCan(mID,msgFrame);
            Log.d("MYCAN",mID+"  "+ Arrays.toString(msgFrame));



        } catch (Exception e) {
            messageType = 0;
            mID = 0;
            len = 0;
            Log.d("MYCAN",e.toString());
        }


    }


}
