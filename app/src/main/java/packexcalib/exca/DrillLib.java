package packexcalib.exca;

import android.util.Log;
import static packexcalib.exca.ExcavatorLib.*;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static packexcalib.exca.Sensors_Decoder.ExtensionBoom;
import static utils.MyTypes.EXCAVATOR;

import java.util.Arrays;

import packexcalib.gnss.NmeaListener;

public class DrillLib {

    public static void Drill(){

        try{
            startXYZ = new double[]{NmeaListener.Est1, NmeaListener.Nord1, NmeaListener.Quota1};
            correctPitch = Excavator_RealValues.realPitch(DataSaved.offsetPitch);//dato da utilizzare nel software già offsettato
            correctRoll = Excavator_RealValues.realRoll(DataSaved.offsetRoll);
            correctBoom1 = Excavator_RealValues.realBoom1(DataSaved.offsetBoom1);
            correctBoom2 = Excavator_RealValues.realBoom2(DataSaved.offsetBoom2);
            correctStick = Excavator_RealValues.realStick(DataSaved.offsetStick);
            correctDBStickAngle = Excavator_RealValues.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[2];
            correctBucket = Excavator_RealValues.realBucket(DataSaved.offsetBucket, DataSaved.offsetFlat, DataSaved.offsetDogBone, DataSaved.L1, DataSaved.L2, DataSaved.L3, DataSaved.L4)[0];
            correctToolRoll=Excavator_RealValues.real_Tool_Roll(DataSaved.offset_Tool_Roll);
            correctToolPitch=correctBucket;
            if (DataSaved.Extra_Heading != 0) {
                if (NmeaListener.roof_Orientation != 999.999) {
                    swing_boom_angle = NmeaListener.roof_Orientation - (NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                } else {
                    swing_boom_angle = 0;
                }
            } else {
                swing_boom_angle = 0;
            }
            double offsetSwing = 0;
            if (DataSaved.Extra_Heading == 1) {
                offsetSwing = DataSaved.offsetSwingExca;
            }
            double hdt0 = ((NmeaListener.mch_Orientation + DataSaved.deltaGPS2) % 360 + 360) % 360;
            hdt_BOOM = ((hdt0 + swing_boom_angle + offsetSwing) % 360 + 360) % 360;

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
            coordB1 = Exca_Quaternion.endPoint(coordMiniPitch, correctBoom1, Deg_Boom_Roll, DataSaved.L_Boom1, hdt_BOOM);
            if (DataSaved.lrBoom2 != 0) {
                coordB2 = Exca_Quaternion.endPoint(coordB1, correctBoom2, Deg_Boom_Roll, DataSaved.L_Boom2, hdt_BOOM);

            } else {
                coordB2 = coordB1;
            }
            coordST = Exca_Quaternion.endPoint(coordB2, correctStick, Deg_Boom_Roll, DataSaved.L_Stick + ExtensionBoom, hdt_BOOM);

            coordTool=Exca_Quaternion.endPoint(coordST,correctToolPitch,correctToolRoll,DataSaved.offset_Tool_Dist,hdt_BOOM+DataSaved.offset_Tool_Orient);
            toolBitCoord=Exca_Quaternion.endPoint(coordTool,correctToolPitch,correctToolRoll,DataSaved.drill_Bit_Len,hdt_BOOM);
            toolEndCoord=Exca_Quaternion.endPoint(toolBitCoord,correctToolPitch,correctToolRoll,DataSaved.drill_Rod_Len,hdt_BOOM);

            Log.d("coordTool", Arrays.toString(coordTool));
            Log.d("toolBitCoord", Arrays.toString(toolBitCoord));
            Log.d("toolEndCoord", Arrays.toString(toolEndCoord));
        } catch (Exception e) {
            Log.e("DrillLib",Log.getStackTraceString(e));
        }

    }
}
