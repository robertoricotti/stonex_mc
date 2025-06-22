package gui.my_opengl.exca;

import static gui.my_opengl.MyGLRenderer.scale;
import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;
import static packexcalib.exca.ExcavatorLib.coordB1;
import static packexcalib.exca.ExcavatorLib.coordB2;
import static packexcalib.exca.ExcavatorLib.coordPivoTilt;
import static packexcalib.exca.ExcavatorLib.coordST;
import static packexcalib.exca.ExcavatorLib.coordinateDY;
import static packexcalib.exca.ExcavatorLib.correctBucket;
import static packexcalib.exca.ExcavatorLib.correctTilt;
import static packexcalib.exca.ExcavatorLib.correctWTilt;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static packexcalib.exca.ExcavatorLib.yawSensor;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;

import android.util.Log;

import gui.my_opengl.Point3DF;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;

public class PuntiBenna {

    public static float[][] GLBucketCoord() {

        double[] altoSX;
        double[] altoDX;
        double[] centerBack;
        double[] centerBackSX;
        double[] centerBackDX;
        double[] tempPivot;
        double[] altoBackSX;
        double[] altoBackDX;

        double flatDist = Math.sin(Math.toRadians(DataSaved.flat)) * DataSaved.L_Bucket;
        Log.d("Flat", DataSaved.flat + "     " + flatDist);

        if (DataSaved.lrTilt == 0) {
            tempPivot = Exca_Quaternion.endPoint(coordST, correctBucket, Deg_Boom_Roll, DataSaved.L_Bucket * 0.1d, hdt_BOOM);
            altoSX = Exca_Quaternion.endPoint(tempPivot, Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 270);

            altoDX = Exca_Quaternion.endPoint(tempPivot, -Deg_Boom_Roll, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 90);

            centerBack = Exca_Quaternion.endPoint(bucketCoord, correctBucket + 90 + DataSaved.flat, Deg_Boom_Roll,DataSaved.L_Bucket, hdt_BOOM);
            centerBackSX = Exca_Quaternion.endPoint(bucketLeftCoord, correctBucket + 90 + DataSaved.flat, Deg_Boom_Roll, flatDist, hdt_BOOM);
            centerBackDX = Exca_Quaternion.endPoint(bucketRightCoord, correctBucket + 90 + DataSaved.flat, Deg_Boom_Roll, flatDist, hdt_BOOM);

            altoBackSX=Exca_Quaternion.endPoint(altoSX, correctBucket + 90 -DataSaved.flat, 0, flatDist*0.85, hdt_BOOM );
            altoBackDX=Exca_Quaternion.endPoint(altoDX, correctBucket + 90 -DataSaved.flat, 0, flatDist*0.85, hdt_BOOM );

        } else {
            tempPivot = Exca_Quaternion.endPoint(coordPivoTilt, correctBucket, Deg_Boom_Roll, DataSaved.L_Bucket * 0.1d, hdt_BOOM);
            altoSX = Exca_Quaternion.endPoint(tempPivot, correctTilt, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 270 + yawSensor);

            altoDX = Exca_Quaternion.endPoint(tempPivot, -correctTilt, 0, DataSaved.W_Bucket * 0.5d, hdt_BOOM + 90 + yawSensor);

            centerBack = Exca_Quaternion.endPoint(bucketCoord, correctWTilt + 90 + DataSaved.flat, correctTilt, DataSaved.L_Bucket, hdt_BOOM + yawSensor);
            centerBackSX = Exca_Quaternion.endPoint(bucketLeftCoord, correctWTilt + 90 + DataSaved.flat, correctTilt, flatDist, hdt_BOOM + yawSensor);
            centerBackDX = Exca_Quaternion.endPoint(bucketRightCoord, correctWTilt + 90 + DataSaved.flat, correctTilt, flatDist, hdt_BOOM + yawSensor);
            altoBackSX=Exca_Quaternion.endPoint(altoSX, correctWTilt + 90 -DataSaved.flat, 0, flatDist*0.85, hdt_BOOM+yawSensor );
            altoBackDX=Exca_Quaternion.endPoint(altoDX, correctWTilt + 90 -DataSaved.flat, 0, flatDist*0.85, hdt_BOOM+yawSensor );
        }

        Point3DF left = new Point3DF((float) (bucketLeftCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (bucketLeftCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (bucketLeftCoord[2] - DataSaved.glL_AnchorView[2]) * scale);
        Point3DF right = new Point3DF((float) (bucketRightCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (bucketRightCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (bucketRightCoord[2] - DataSaved.glL_AnchorView[2]) * scale);
        Point3DF altoSx = new Point3DF((float) (altoSX[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (altoSX[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (altoSX[2] - DataSaved.glL_AnchorView[2]) * scale);
        Point3DF altoDx = new Point3DF((float) (altoDX[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (altoDX[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (altoDX[2] - DataSaved.glL_AnchorView[2]) * scale);

        Point3DF center = new Point3DF((float) (bucketCoord[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (bucketCoord[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (bucketCoord[2] - DataSaved.glL_AnchorView[2]) * scale);


        Point3DF pivot = new Point3DF((float) (coordPivoTilt[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (coordPivoTilt[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (coordPivoTilt[2] - DataSaved.glL_AnchorView[2]) * scale);

        Point3DF stick = new Point3DF((float) (coordST[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (coordST[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (coordST[2] - DataSaved.glL_AnchorView[2]) * scale);
        Point3DF boom2 = new Point3DF((float) (coordB2[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (coordB2[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (coordB2[2] - DataSaved.glL_AnchorView[2]) * scale);
        Point3DF boom1 = new Point3DF((float) (coordB1[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (coordB1[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (coordB1[2] - DataSaved.glL_AnchorView[2]) * scale);

        Point3DF deltaY = new Point3DF((float) (coordinateDY[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (coordinateDY[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (coordinateDY[2] - DataSaved.glL_AnchorView[2]) * scale);
        Point3DF centBack = new Point3DF((float) (centerBack[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (centerBack[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (centerBack[2] - DataSaved.glL_AnchorView[2]) * scale);
        Point3DF centBackSX = new Point3DF((float) (centerBackSX[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (centerBackSX[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (centerBackSX[2] - DataSaved.glL_AnchorView[2]) * scale);
        Point3DF centBackDX = new Point3DF((float) (centerBackDX[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (centerBackDX[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (centerBackDX[2] - DataSaved.glL_AnchorView[2]) * scale);
        Point3DF buckPivot = new Point3DF((float) (tempPivot[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (tempPivot[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (tempPivot[2] - DataSaved.glL_AnchorView[2]) * scale);

        Point3DF altoBSX = new Point3DF((float) (altoBackSX[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (altoBackSX[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (altoBackSX[2] - DataSaved.glL_AnchorView[2]) * scale);

        Point3DF altoBDX = new Point3DF((float) (altoBackDX[0] - DataSaved.glL_AnchorView[0]) * scale,
                (float) (altoBackDX[1] - DataSaved.glL_AnchorView[1]) * scale,
                (float) (altoBackDX[2] - DataSaved.glL_AnchorView[2]) * scale);





       /* float[][] out = new float[14][];
        out[0] = new float[]{left.getX(), left.getY(), left.getZ()};
        out[1] = new float[]{right.getX(), right.getY(), right.getZ()};
        out[2] = new float[]{altoSx.getX(), altoSx.getY(), altoSx.getZ()};
        out[3] = new float[]{altoDx.getX(), altoDx.getY(), altoDx.getZ()};
        out[4] = new float[]{center.getX(), center.getY(), center.getZ()};
        out[5] = new float[]{pivot.getX(), pivot.getY(), pivot.getZ()};
        out[6] = new float[]{stick.getX(), stick.getY(), stick.getZ()};
        out[7] = new float[]{boom2.getX(), boom2.getY(), boom2.getZ()};
        out[8] = new float[]{boom1.getX(), boom1.getY(), boom1.getZ()};
        out[9] = new float[]{deltaY.getX(), deltaY.getY(), deltaY.getZ()};
        out[10] = new float[]{centBack.getX(), centBack.getY(), centBack.getZ()};
        out[11] = new float[]{centBackSX.getX(), centBackSX.getY(), centBackSX.getZ()};
        out[12] = new float[]{centBackDX.getX(), centBackDX.getY(), centBackDX.getZ()};
        out[13] = new float[]{buckPivot.getX(), buckPivot.getY(), buckPivot.getZ()};*/
        float[][] out = new float[12][];
        out[0] = new float[]{left.getX(), left.getY(), left.getZ()};
        out[1] = new float[]{right.getX(), right.getY(), right.getZ()};
        out[2] = new float[]{altoSx.getX(), altoSx.getY(), altoSx.getZ()};
        out[3] = new float[]{altoDx.getX(), altoDx.getY(), altoDx.getZ()};
        out[4] = new float[]{center.getX(), center.getY(), center.getZ()};
        out[5] = new float[]{buckPivot.getX(), buckPivot.getY(), buckPivot.getZ()};
        out[6] = new float[]{centBackSX.getX(), centBackSX.getY(), centBackSX.getZ()};
        out[7] = new float[]{centBack.getX(), centBack.getY(), centBack.getZ()};
        out[8] = new float[]{centBackDX.getX(), centBackDX.getY(), centBackDX.getZ()};
        out[9] = new float[]{centBackDX.getX(), centBackDX.getY(), centBackDX.getZ()};
        out[10] = new float[]{altoBSX.getX(), altoBSX.getY(), altoBSX.getZ()};
        out[11] = new float[]{altoBDX.getX(), altoBDX.getY(), altoBDX.getZ()};
        /* out[9] = new float[]{pivot.getX(), pivot.getY(), pivot.getZ()};
        out[10] = new float[]{stick.getX(), stick.getY(), stick.getZ()};
        out[11] = new float[]{boom2.getX(), boom2.getY(), boom2.getZ()};
        out[12] = new float[]{boom1.getX(), boom1.getY(), boom1.getZ()};
        out[13] = new float[]{deltaY.getX(), deltaY.getY(), deltaY.getZ()};
*/
        return out;
    }
    public static float[][] fixedPoints_60() {
        return new float[][]{
                new float[]{-500f, 0, 0},  //  index (left)
                new float[]{500f, 0, 0},   //  index (right)
                new float[]{-500f, 288.68f, 500f},  //  index (topleft)
                new float[]{500f, 288.68f, 500f}, //  index (topright)
                new float[]{0, 0, 0}, //   (center)
                new float[]{0, 200f, 520f},//  index (pivot)
                new float[]{0, 750f, 0}//centerBack
        };
    }
    public static float[][] bennanuda() {
        return new float[][]{
                new float[]{-500f, 0, 0},  //  index (left)
                new float[]{500f, 0, 0},   //  index (right)
                new float[]{-500f, 290f, 500f},  //  index (topleft)
                new float[]{500f, 290f, 500f}, //  index (topright)
                new float[]{0, 0, 0}, //   (center)
                new float[]{0, 290f, 500f},//  index (pivot)
                new float[]{0, 750f, 0}//centerBack
        };
    }

    public static float[][] fixedPoints_45() {
        return new float[][]{
                new float[]{-500f, 0, 0},  //  index (left)
                new float[]{500f, 0, 0},   //  index (right)
                new float[]{-500f, 500f, 500f},  //  index (topleft)
                new float[]{500, 500f, 500}, //  index (topright)
                new float[]{0, 0, 0}, //   (center)
                new float[]{0, 400, 520},//  index (pivot)
                new float[]{0, 1000f, 0}//centerBack
        };
    }

    public static float[][] fixedBucket() {
        return new float[][]{
                new float[]{-500f, 0, 0},  //  index (left)
                new float[]{500f, 0, 0},   //  index (right)
                new float[]{-500f, 290f, 500f},  //  index (topleft)
                new float[]{500, 290f, 500}, //  index (topright)
                new float[]{0, 0, 0}, //   (center)
                new float[]{0, 210, 500},//  index (pivot)
                new float[]{0, 1000f, 0}//centerBack TODO
        };
    }
}
