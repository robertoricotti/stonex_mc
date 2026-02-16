package packexcalib.exca;

import android.util.Log;
import static packexcalib.exca.ExcavatorLib.*;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static packexcalib.exca.Sensors_Decoder.ExtensionBoom;
import static packexcalib.exca.Sensors_Decoder_Drill.RopeLen;
import static utils.MyTypes.AT_BODY;
import static utils.MyTypes.AT_BOOM;


import java.util.Arrays;

import packexcalib.gnss.NmeaListener;

public class DrillLib {
    public static double[] coordBoomLink_1,coordTool_DeltaZ,coordBoomLink_Final;
    public static double [] coorX=new double[3],coorY=new double[3],coorMart=new double[3];
    static double Z_Totale;

    public static void Drill(){

        switch (DataSaved.Drill_Antenna_Mounting){
            case AT_BODY:
                try{
                    startXYZ = new double[]{NmeaListener.Est1, NmeaListener.Nord1, NmeaListener.Quota1};
                    correctPitch = Offset_Applier.realPitch(DataSaved.offsetPitch);//dato da utilizzare nel software già offsettato
                    correctRoll = Offset_Applier.realRoll(DataSaved.offsetRoll);
                    correctBoom1 = Offset_Applier.realBoom1(DataSaved.offsetBoom1);
                    correctBoom2 = Offset_Applier.realBoom2(DataSaved.offsetBoom2);
                    correctMastLink = Offset_Applier.realMastLink(DataSaved.offsetStick);
                    correctToolRoll= Offset_Applier.real_Tool_Roll(DataSaved.offset_Tool_Roll);
                    correctToolPitch= Offset_Applier.real_Tool_Pitch(DataSaved.offset_Tool_Pitch);
                    double hdt0 = ((NmeaListener.mch_Orientation + DataSaved.deltaGPS2) % 360 + 360) % 360;
                    if (DataSaved.Extra_Heading != 0) {

                        if (NmeaListener.roof_Orientation != 999.999) {
                            swing_boom_angle = ((NmeaListener.roof_Orientation + DataSaved.offsetSwingExca) % 360 + 360) % 360;

                        } else {
                            swing_boom_angle = hdt0;
                        }
                        hdt_BOOM = ((swing_boom_angle) % 360 + 360) % 360;
                    } else {
                        swing_boom_angle = 0;
                        hdt_BOOM = ((hdt0 + swing_boom_angle) % 360 + 360) % 360;
                    }
                    double hdtR = ((hdt0 + 90) % 360 + 360) % 360;

                    double hdtL = ((hdt0 - 90) % 360 + 360) % 360;

                    double hdtReverse = ((hdt0 + 180) % 360 + 360) % 360;

                    coordinateDZ = Exca_Quaternion.endPoint(startXYZ, correctPitch - 90, correctRoll, DataSaved.deltaZ, hdt0);
                    if (DataSaved.deltaX < 0) {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, correctRoll, -correctPitch, DataSaved.deltaX, hdtL);
                    } else {
                        coordinateDX = Exca_Quaternion.endPoint(coordinateDZ, -correctRoll, correctPitch, DataSaved.deltaX, hdtR);
                    }
                    if (DataSaved.deltaY < 0) {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, -correctPitch, -correctRoll, Math.abs(DataSaved.deltaY) + DataSaved.miniPitch_L, hdtReverse);
                    } else {
                        coordinateDY = Exca_Quaternion.endPoint(coordinateDX, correctPitch, correctRoll, DataSaved.deltaY - DataSaved.miniPitch_L, hdt0);
                    }//DY = Centro perno boom1

                    if (DataSaved.Extra_Heading != 0) {
                        coordMiniPitch = Exca_Quaternion.endPoint(coordinateDY, correctPitch, Deg_Boom_Roll, DataSaved.miniPitch_L, hdt_BOOM);
                    } else {

                        coordMiniPitch = coordinateDY;
                    }
                    overturn = Math.abs(correctRoll) > 85.0d || Math.abs(correctPitch) > 85.0d;
                    coordB1 = Exca_Quaternion.endPoint(coordMiniPitch, correctBoom1, Deg_Boom_Roll, DataSaved.L_Boom1+ExtensionBoom, hdt_BOOM);
                    if (DataSaved.lrBoom2 != 0) {
                        coordB2 = Exca_Quaternion.endPoint(coordB1, correctBoom2, Deg_Boom_Roll, DataSaved.L_Boom2, hdt_BOOM);

                    } else {
                        coordB2 = coordB1;
                    }
                    coordST = Exca_Quaternion.endPoint(coordB2, correctMastLink+(90*getSign(DataSaved.L_Stick)), Deg_Boom_Roll, DataSaved.L_Stick, hdt_BOOM);
                    //qui siamo al centro perno dell'inizio mast link
                    if(DataSaved.offset_Boom_Tool>0) {
                        coordBoomLink_1 = Exca_Quaternion.endPoint(coordST, -Deg_Boom_Roll, correctMastLink,DataSaved.offset_Boom_Tool,hdt_BOOM+90);
                    }else if(DataSaved.offset_Boom_Tool<0){
                        coordBoomLink_1 = Exca_Quaternion.endPoint(coordST, Deg_Boom_Roll, correctMastLink,Math.abs(DataSaved.offset_Boom_Tool),hdt_BOOM-90);

                    }else {
                        coordBoomLink_1=coordST;
                    }
                    coordBoomLink_Final=Exca_Quaternion.endPoint(coordBoomLink_1,correctMastLink,Deg_Boom_Roll,DataSaved.Tool_Delta_Y,hdt_BOOM);
                    //qui siamo al centro rotazione della fine del mast link

                    if(DataSaved.Tool_Delta_X>0){
                        coordTool=Exca_Quaternion.endPoint(coordBoomLink_Final,correctToolRoll,-correctToolPitch,DataSaved.Tool_Delta_X,hdt_BOOM-90);
                    } else if (DataSaved.Tool_Delta_X<0) {
                        coordTool=Exca_Quaternion.endPoint(coordBoomLink_Final,-correctToolRoll,correctToolPitch,Math.abs(DataSaved.Tool_Delta_X),hdt_BOOM+90);
                    }else {
                        coordTool=coordBoomLink_Final;
                    }

                    //Z Totale
                    Z_Totale=DataSaved.Tool_Delta_Z+RopeLen+DataSaved.drill_First_Rod_Len+(DataSaved.numeroAste*DataSaved.drill_Rod_Len)+DataSaved.drill_Bit_Len;

                    coordTool_DeltaZ=Exca_Quaternion.endPoint(coordTool,correctToolPitch-90,correctToolRoll,DataSaved.Tool_Delta_Z,hdt_BOOM);


                    toolEndCoord=Exca_Quaternion.endPoint(coordTool,correctToolPitch-90,correctToolRoll,Z_Totale,hdt_BOOM);

                    Log.d("coordTool", Arrays.toString(coordTool));

                    Log.d("toolEndCoord", Arrays.toString(toolEndCoord));
                } catch (Exception e) {
                    Log.e("DrillLib",Log.getStackTraceString(e));
                }
                break;

            case AT_BOOM:
                try {
                    startXYZ = new double[]{NmeaListener.Est1, NmeaListener.Nord1, NmeaListener.Quota1};
                    correctToolRoll= Offset_Applier.real_Tool_Roll(DataSaved.offset_Tool_Roll);
                    correctToolPitch= Offset_Applier.real_Tool_Pitch(DataSaved.offset_Tool_Pitch);
                    double hdt0 = ((NmeaListener.mch_Orientation + DataSaved.deltaGPS2) % 360 + 360) % 360;
                    hdt_BOOM = ((hdt0 ) % 360 + 360) % 360;
                    coorY=Exca_Quaternion.endPoint(startXYZ,correctToolPitch,correctToolRoll,DataSaved.Tool_Delta_Y,hdt_BOOM);
                    if(DataSaved.Tool_Delta_X>0){
                        //vado a dx
                        coorX=Exca_Quaternion.endPoint(coorY,-correctToolRoll,correctToolPitch,DataSaved.Tool_Delta_X,hdt_BOOM+90);

                    }else {
                        //vado a sx
                        coorX=Exca_Quaternion.endPoint(coorY,correctToolRoll,-correctToolPitch,Math.abs(DataSaved.Tool_Delta_X),hdt_BOOM-90);
                    }
                    Z_Totale=DataSaved.Tool_Delta_Z+RopeLen+DataSaved.drill_First_Rod_Len+(DataSaved.numeroAste*DataSaved.drill_Rod_Len)+DataSaved.drill_Bit_Len;
                    coordTool=coorX;
                    toolEndCoord=Exca_Quaternion.endPoint(coordTool,correctToolPitch-90,correctToolRoll,Z_Totale,hdt_BOOM);

                }catch (Exception e) {
                    Log.e("DrillLib",Log.getStackTraceString(e));
                }

                break;
        }


    }

    private static double getSign(double value){
        if(value<0){
            return -1d;
        }else {
            return 1d;
        }
    }
}
