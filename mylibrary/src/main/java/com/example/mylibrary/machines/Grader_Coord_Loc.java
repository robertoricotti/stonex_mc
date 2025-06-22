package com.example.mylibrary.machines;

import com.example.mylibrary.positioning.My_Exca_Quaternion;
import com.example.mylibrary.positioning.My_RodLocation;

public class Grader_Coord_Loc {

    public Grader_Coord_Loc(){

    }
    public static double[][] bladeCoord(double[] startXYZ,double hdt,double deltaLeft,double H_Mast,double BladeH,double BladeW,
                                        double bladePitch,double bladeRoll){
   /*
  ANTENNA ON THE BLADE
  hdt=bladeorientation

    */
        double []rodPos,leftEdge ,center,rightEdge;

        rodPos = My_RodLocation.rodloc(startXYZ,bladePitch,bladeRoll,H_Mast+BladeH,hdt);//coordinate sotto antena
        leftEdge= My_Exca_Quaternion.endPoint(rodPos,-bladeRoll,bladePitch,-deltaLeft,hdt);//coordinate spigolo sinistro
        center=My_Exca_Quaternion.endPoint(rodPos,-bladeRoll,bladePitch,(BladeW*0.5)-deltaLeft,hdt);//coordinate centro
        rightEdge=My_Exca_Quaternion.endPoint(rodPos,-bladeRoll,bladePitch,BladeW-deltaLeft,hdt);

        return new double[][]{leftEdge,center,rightEdge};
    }
}
