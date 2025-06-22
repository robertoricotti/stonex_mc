package com.example.mylibrary.machines;

import com.example.mylibrary.positioning.My_Exca_Quaternion;
import com.example.mylibrary.positioning.My_RodLocation;

public class Dozer_Coord_Loc {

    public Dozer_Coord_Loc(){


    }
    public static double[][] bladeCoord(double[] startXYZ,double hdt,double deltaLeft,double H_Mast,double BladeH,double BladeW,
                                      double bladePitch,double bladeRoll){
   /*
  ANTENNA ON THE BLADE
  hdt=bladeorientation

    */
        double []rodPos,leftEdge ,center,rightEdge;

            rodPos =My_RodLocation.rodloc(startXYZ,bladePitch,bladeRoll,H_Mast+BladeH,hdt);//coordinate sotto antena
            leftEdge= My_Exca_Quaternion.endPoint(rodPos,-bladeRoll,bladePitch,-deltaLeft,hdt);//coordinate spigolo sinistro
            center=My_Exca_Quaternion.endPoint(rodPos,-bladeRoll,bladePitch,(BladeW*0.5)-deltaLeft,hdt);//coordinate centro
            rightEdge=My_Exca_Quaternion.endPoint(rodPos,-bladeRoll,bladePitch,BladeW-deltaLeft,hdt);

            return new double[][]{leftEdge,center,rightEdge};
    }
    public static double[][] bladeCoordMastless(double[] startXYZ,double hdt,double deltaZ,double deltaX,double deltaY,
                                        double deltaBoom,double bladeW,double framePitch,double frameRoll,double bladePitch,double bladeRoll){
   /*
  ANTENNA ON THE ROOF
  hdt= machineOrientation

    */
        double []rodPosZ,posX,posY,leftEdge ,center,rightEdge;

        rodPosZ =My_RodLocation.rodloc(startXYZ,framePitch,frameRoll,deltaZ,hdt);//coordinate sotto antena
        posX=My_Exca_Quaternion.endPoint(rodPosZ,-frameRoll,framePitch,deltaX,hdt+90);
        posY=My_Exca_Quaternion.endPoint(posX,framePitch,frameRoll,deltaY,hdt);
        center=My_Exca_Quaternion.endPoint(posY,bladePitch,bladeRoll,deltaBoom,hdt);
        leftEdge=My_Exca_Quaternion.endPoint(center,bladeRoll,-bladePitch,bladeW*0.5,hdt-90);
        rightEdge=My_Exca_Quaternion.endPoint(center,-bladeRoll,bladePitch,bladeW*0.5,hdt+90);


        return new double[][]{leftEdge,center,rightEdge};
    }

}
