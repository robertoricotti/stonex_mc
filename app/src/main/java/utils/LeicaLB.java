package utils;


import android.util.Log;

import java.util.Arrays;

import gui.MyApp;
import packexcalib.exca.PLC_DataTypes_BigEndian;
import packexcalib.exca.PLC_DataTypes_LittleEndian;

public  class LeicaLB {

    public LeicaLB(){

    }
    public static byte[] mapping(boolean offGrid,double value,double tolerance){
        byte d0=0;
        byte d1=0;
        byte d2=0;
        double diff=Math.abs(value)-tolerance;
        if (value > tolerance) {
            if(diff<=0.02){
                d0=(byte) 0xA4;
            }else if(diff>0.02&&diff<0.05){
                d0=(byte) 0xA3;
            }else if(diff>0.05&&diff<0.09){
                d0=(byte) 0xA2;
            }else if(diff>0.09&&diff<0.14){
                d0=(byte) 0xA1;
            }else {
                d0=(byte) 0xA0;
            }

           // heightCT.setText("▼ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT), this));
        } else if (value < -tolerance) {
            if(diff<=0.02){
                d0=(byte) 0xA6;
            }else if(diff>0.02&&diff<0.05){
                d0=(byte) 0xA7;
            }else if(diff>0.05&&diff<0.09){
                d0=(byte) 0xA8;
            }else if(diff>0.09&&diff<0.14){
                d0=(byte) 0xA9;
            }else {
                d0=(byte) 0xAA;
            }

           // heightCT.setText("▲ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT), this));
        } else if (value >= -tolerance && value <= tolerance) {


            d0= (byte) 0xA5;
           // heightCT.setText("⧗ " + Utils.readUnitOfMeasureLITE(String.valueOf(TriangleService.quota3D_CT), this));
        }

        double nuovoVale= Double.parseDouble(Utils.readUnitOfMeasureLB(String.valueOf(value)));

        int mVaule= Math.abs((int)Math.round(nuovoVale*100));



        byte []ds= PLC_DataTypes_BigEndian.U16_to_bytes_be(mVaule);
        d1=ds[0];
        d2=ds[1];
        byte u8;
        boolean [] bitmask;
        bitmask=PLC_DataTypes_BigEndian.U8_to_bitmask_4_be(d1);
        bitmask=new boolean[]{bitmask[0],bitmask[1],bitmask[2],true};
        u8=PLC_DataTypes_BigEndian.Encode_4_bool_be(bitmask);

        if(!offGrid) {
            return new byte[]{d0, u8, d2};
        }else {
            return new byte[]{(byte) 0xff, d1, d2};
        }
    }
}
