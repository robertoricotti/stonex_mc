package machine_draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;


public class ExcavatorDrawer {

    PointF startXYZ;
    PointF GPS_Z;
    PointF GPS_DX;
    PointF GPS_DY;
    PointF roll;
    PointF pitch;
    PointF degBoomRoll;
    PointF boom1;
    PointF boom2;
    PointF stick;
    PointF bucket;
    PointF bucketLeft;
    PointF bucketRight;

    PointD rStartXYZ;
    PointD rGPS_Z;
    PointD rGPS_DX;
    PointD rGPS_DY;

    PointD rDegBoomRoll;
    PointD rBoom1;
    PointD rBoom2;
    PointD rStick;
    PointD rBucket;
    PointD rBucketLeft;
    PointD rBucketRight;


    public ExcavatorDrawer() {
    }

    // inizializza i punti di congiunzioni reali secondo il pivot point desiderato


    /**
     * aggiorna i punti di congiunzione reali dell'escavatore a dei punti di disegno secondo il pivot point desiderato
     *
     * @param canvasWidth  larghezza canvas
     * @param canvasHeight altezza canvas
     * @param pivotPoint   posizione del pivot point
     * @param percX        percentuale di posizione nel display della x del pivot point
     * @param percY        percentuale di posizione nel display della y del pivot point
     * @param scala        rappresenta la dimensione della scala
     */
    public void update(float canvasWidth, float canvasHeight, String pivotPoint, float percX, float percY, double scala) {

        double heading = 0;// Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);

        rStartXYZ = new PointD(ExcavatorLib.startXYZ[0], ExcavatorLib.startXYZ[1]);
        rGPS_Z = new PointD(ExcavatorLib.coordinateDZ[0], ExcavatorLib.coordinateDZ[1]);
        rGPS_DX = new PointD(ExcavatorLib.coordinateDX[0], ExcavatorLib.coordinateDX[1]);
        rGPS_DY = new PointD(ExcavatorLib.coordinateDY[0], ExcavatorLib.coordinateDY[1]);

        rDegBoomRoll = new PointD(ExcavatorLib.coordPitch[0], ExcavatorLib.coordPitch[1]);
        rBoom1 = new PointD(ExcavatorLib.coordB1[0], ExcavatorLib.coordB1[1]);
        rBoom2 = new PointD(ExcavatorLib.coordB2[0], ExcavatorLib.coordB2[1]);
        rStick = new PointD(ExcavatorLib.coordST[0], ExcavatorLib.coordST[1]);
        rBucket = new PointD(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1]);
        rBucketLeft = new PointD(ExcavatorLib.bucketLeftCoord[0], ExcavatorLib.bucketLeftCoord[1]);
        rBucketRight = new PointD(ExcavatorLib.bucketRightCoord[0], ExcavatorLib.bucketRightCoord[1]);

        PointF pivot = new PointF(canvasWidth * percX, canvasHeight * percY);

        PointD fixPoint;

        switch (pivotPoint.toUpperCase()) {
            case "GPS_DY":
                fixPoint = new PointD(rGPS_DY.x, rGPS_DY.y);
                break;
            case "BOOM1":
                fixPoint = new PointD(rBoom1.x, rBoom1.y);
                break;
            case "BOOM2":
                fixPoint = new PointD(rBoom2.x, rBoom2.y);
                break;
            case "STICK":
                fixPoint = new PointD(rStick.x, rStick.y);
                break;
            default:
                fixPoint = new PointD(rBucket.x, rBucket.y);
                break;
        }


        double diff_x_GPS_DY = (rGPS_DY.x - fixPoint.x) * scala;
        double diff_y_GPS_DY = (rGPS_DY.y - fixPoint.y) * scala;

        GPS_DY = new PointF(
                (float) (pivot.x + diff_x_GPS_DY * Math.cos(Math.toRadians(heading)) - diff_y_GPS_DY * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_GPS_DY * Math.sin(Math.toRadians(heading)) - diff_y_GPS_DY * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_GPS_DX = (rGPS_DX.x - fixPoint.x) * scala;
        double diff_y_GPS_DX = (rGPS_DX.y - fixPoint.y) * scala;

        GPS_DX = new PointF(
                (float) (pivot.x + diff_x_GPS_DX * Math.cos(Math.toRadians(heading)) - diff_y_GPS_DX * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_GPS_DX * Math.sin(Math.toRadians(heading)) - diff_y_GPS_DX * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_startXYZ = (rStartXYZ.x - fixPoint.x) * scala;
        double diff_y_startXYZ = (rStartXYZ.y - fixPoint.y) * scala;

        startXYZ = new PointF(
                (float) (pivot.x + diff_x_startXYZ * Math.cos(Math.toRadians(heading)) - diff_y_startXYZ * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_startXYZ * Math.sin(Math.toRadians(heading)) - diff_y_startXYZ * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_GPS_Z = (rGPS_Z.x - fixPoint.x) * scala;
        double diff_y_GPS_Z = (rGPS_Z.y - fixPoint.y) * scala;

        GPS_Z = new PointF(
                (float) (pivot.x + diff_x_GPS_Z * Math.cos(Math.toRadians(heading)) - diff_y_GPS_Z * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_GPS_Z * Math.sin(Math.toRadians(heading)) - diff_y_GPS_Z * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_roll = 0;//(rRoll.x - fixPoint.x) * scala;
        double diff_y_roll = 0;// (rRoll.y - fixPoint.y) * scala;

        roll = new PointF(
                (float) (pivot.x + diff_x_roll * Math.cos(Math.toRadians(heading)) - diff_y_roll * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_roll * Math.sin(Math.toRadians(heading)) - diff_y_roll * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_pitch = 0;//(rPitch.x - fixPoint.x) * scala;
        double diff_y_pitch = 0;// (rPitch.y - fixPoint.y) * scala;

        pitch = new PointF(
                (float) (pivot.x + diff_x_pitch * Math.cos(Math.toRadians(heading)) - diff_y_pitch * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_pitch * Math.sin(Math.toRadians(heading)) - diff_y_pitch * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_degBoomRoll = (rDegBoomRoll.x - fixPoint.x) * scala;
        double diff_y_degBoomRoll = (rDegBoomRoll.y - fixPoint.y) * scala;

        degBoomRoll = new PointF(
                (float) (pivot.x + diff_x_degBoomRoll * Math.cos(Math.toRadians(heading)) - diff_y_degBoomRoll * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_degBoomRoll * Math.sin(Math.toRadians(heading)) - diff_y_degBoomRoll * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_startBoom1 = (rBoom1.x - fixPoint.x) * scala;
        double diff_y_startBoom1 = (rBoom1.y - fixPoint.y) * scala;

        boom1 = new PointF(
                (float) (pivot.x + diff_x_startBoom1 * Math.cos(Math.toRadians(heading)) - diff_y_startBoom1 * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_startBoom1 * Math.sin(Math.toRadians(heading)) - diff_y_startBoom1 * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_startBoom2 = (rBoom2.x - fixPoint.x) * scala;
        double diff_y_startBoom2 = (rBoom2.y - fixPoint.y) * scala;

        boom2 = new PointF(
                (float) (pivot.x + diff_x_startBoom2 * Math.cos(Math.toRadians(heading)) - diff_y_startBoom2 * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_startBoom2 * Math.sin(Math.toRadians(heading)) - diff_y_startBoom2 * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_startStick = (rStick.x - fixPoint.x) * scala;
        double diff_y_startStick = (rStick.y - fixPoint.y) * scala;

        stick = new PointF(
                (float) (pivot.x + diff_x_startStick * Math.cos(Math.toRadians(heading)) - diff_y_startStick * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_startStick * Math.sin(Math.toRadians(heading)) - diff_y_startStick * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_bucket = (rBucket.x - fixPoint.x) * scala;
        double diff_y_bucket = (rBucket.y - fixPoint.y) * scala;

        bucket = new PointF(
                (float) (pivot.x + diff_x_bucket * Math.cos(Math.toRadians(heading)) - diff_y_bucket * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_bucket * Math.sin(Math.toRadians(heading)) - diff_y_bucket * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_bucketLeft = (rBucketLeft.x - fixPoint.x) * scala;
        double diff_y_bucketLeft = (rBucketLeft.y - fixPoint.y) * scala;

        bucketLeft = new PointF(
                (float) (pivot.x + diff_x_bucketLeft * Math.cos(Math.toRadians(heading)) - diff_y_bucketLeft * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_bucketLeft * Math.sin(Math.toRadians(heading)) - diff_y_bucketLeft * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_bucketRight = (rBucketRight.x - fixPoint.x) * scala;
        double diff_y_bucketRight = (rBucketRight.y - fixPoint.y) * scala;

        bucketRight = new PointF(
                (float) (pivot.x + diff_x_bucketRight * Math.cos(Math.toRadians(heading)) - diff_y_bucketRight * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_bucketRight * Math.sin(Math.toRadians(heading)) - diff_y_bucketRight * Math.cos(Math.toRadians(heading)))
        );
    }

    /**
     * Disegna il frame dell'escavatore
     *
     * @param canvas il canvas di riferimento
     * @param paint  il paint di riferimento
     * @param color  il colore da assegnare
     * @param scala  la scala da assegnare
     */
    public void drawFrame(Canvas canvas, Paint paint, int color, double scala) {

        double heading = 0;

        double lFrame = DataSaved.lunghezzaFrame / 2;
        double wFrame = DataSaved.larghezzaFrame / 2;

        PointF pivot = new PointF(GPS_DY.x, GPS_DY.y);


        float fw_x = (float) (pivot.x + (lFrame * scala) * Math.cos(Math.toRadians(270 + heading)));
        float fw_y = (float) (pivot.y + (lFrame * scala) * Math.sin(Math.toRadians(270 + heading)));

        float bwd_x = (float) (pivot.x + (lFrame * scala) * Math.cos(Math.toRadians(90 + heading)));
        float bwd_y = (float) (pivot.y + (lFrame * scala) * Math.sin(Math.toRadians(90 + heading)));

        float left_x = (float) (pivot.x + (wFrame * scala) * Math.cos(Math.toRadians(180 + heading)));
        float left_y = (float) (pivot.y + (wFrame * scala) * Math.sin(Math.toRadians(180 + heading)));

        float right_x = (float) (pivot.x + (wFrame * scala) * Math.cos(Math.toRadians(0 + heading)));
        float right_y = (float) (pivot.y + (wFrame * scala) * Math.sin(Math.toRadians(0 + heading)));

        /*.setColor(Color.CYAN);
        canvas.drawCircle(fw_x, fw_y, 20, paint);

        paint.setColor(Color.YELLOW);
        canvas.drawCircle(bwd_x, bwd_y, 20, paint);

        paint.setColor(Color.RED);
        canvas.drawCircle(left_x, left_y, 20, paint);

        paint.setColor(Color.MAGENTA);
        canvas.drawCircle(right_x, right_y, 20, paint);*/

        PointF[] frameVertex = rectVertex(fw_x, fw_y, bwd_x, bwd_y, (float) calculateDistance(pivot.x, pivot.y, left_x, left_y));

        paint.setStrokeWidth(7);
        paint.setColor(Color.BLACK);

        canvas.drawLine(frameVertex[0].x, frameVertex[0].y, frameVertex[2].x, frameVertex[2].y, paint);
        canvas.drawLine(frameVertex[1].x, frameVertex[1].y, frameVertex[3].x, frameVertex[3].y, paint);
        canvas.drawLine(frameVertex[1].x, frameVertex[1].y, frameVertex[0].x, frameVertex[0].y, paint);


        /*paint.setColor(Color.BLUE);
        canvas.drawCircle(frameVertex[0].x, frameVertex[0].y, 20, paint);
        paint.setColor(Color.LTGRAY);
        canvas.drawCircle(frameVertex[1].x, frameVertex[1].y, 20, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(frameVertex[2].x, frameVertex[2].y, 20, paint);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(frameVertex[3].x, frameVertex[3].y, 20, paint);*/

        //cingoli

        double distTruckV = calculateDistance(frameVertex[1].x, frameVertex[1].y, frameVertex[3].x, frameVertex[3].y);

        distTruckV = (distTruckV + (distTruckV * 0.25));


        float mTruckLx = (frameVertex[1].x + ((frameVertex[1].x + fw_x) / 2)) / 2;
        float mTruckLy = (frameVertex[1].y + ((frameVertex[1].y + fw_y) / 2)) / 2;


        double distTruckH = calculateDistance(frameVertex[1].x, frameVertex[1].y, mTruckLx, mTruckLy);

        distTruckH = distTruckH + (distTruckH * 0.20f);


        PointF[] truckLeftV = rectVertex(frameVertex[1].x, frameVertex[1].y, frameVertex[3].x, frameVertex[3].y, (float) distTruckH);
        PointF[] truckRightV = rectVertex(frameVertex[0].x, frameVertex[0].y, frameVertex[2].x, frameVertex[2].y, (float) distTruckH);


        float lv1x = (float) (truckLeftV[3].x + (distTruckV) * Math.cos(Math.toRadians(270 + heading)));
        float lv1y = (float) (truckLeftV[3].y + (distTruckV) * Math.sin(Math.toRadians(270 + heading)));


        float lv2x = (float) (truckLeftV[2].x + (distTruckV) * Math.cos(Math.toRadians(270 + heading)));
        float lv2y = (float) (truckLeftV[2].y + (distTruckV) * Math.sin(Math.toRadians(270 + heading)));


        float rv1x = (float) (truckRightV[3].x + (distTruckV) * Math.cos(Math.toRadians(270 + heading)));
        float rv1y = (float) (truckRightV[3].y + (distTruckV) * Math.sin(Math.toRadians(270 + heading)));


        float rv2x = (float) (truckRightV[2].x + (distTruckV) * Math.cos(Math.toRadians(270 + heading)));
        float rv2y = (float) (truckRightV[2].y + (distTruckV) * Math.sin(Math.toRadians(270 + heading)));


        Path path = new Path();

        paint.setColor(Color.rgb(56, 56, 56));

        path.moveTo(truckLeftV[3].x, truckLeftV[3].y);
        path.lineTo(lv1x, lv1y);
        path.lineTo(lv2x, lv2y);
        path.lineTo(truckLeftV[2].x, truckLeftV[2].y);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();

        path.moveTo(truckRightV[3].x, truckRightV[3].y);
        path.lineTo(rv1x, rv1y);
        path.lineTo(rv2x, rv2y);
        path.lineTo(truckRightV[2].x, truckRightV[2].y);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();

        double distTruck = calculateDistance(lv1x, lv1y, lv2x, lv2y);

        PointF[] topL = rectVertex(lv1x, lv1y, lv2x, lv2y, (float) (distTruck * 0.15));
        PointF[] topR = rectVertex(rv1x, rv1y, rv2x, rv2y, (float) (distTruck * 0.15));
        PointF[] bottomL = rectVertex(truckLeftV[3].x, truckLeftV[3].y, truckLeftV[2].x, truckLeftV[2].y, (float) (distTruck * 0.15));
        PointF[] bottomR = rectVertex(truckRightV[3].x, truckRightV[3].y, truckRightV[2].x, truckRightV[2].y, (float) (distTruck * 0.15));

        path = new Path();
        path.moveTo(lv1x, lv1y);
        path.cubicTo(topL[0].x, topL[0].y, topL[2].x, topL[2].y, lv2x, lv2y);
        canvas.drawPath(path, paint);
        path.reset();

        path = new Path();
        path.moveTo(rv1x, rv1y);
        path.cubicTo(topR[0].x, topR[0].y, topR[2].x, topR[2].y, rv2x, rv2y);
        canvas.drawPath(path, paint);
        path.reset();

        path = new Path();
        path.moveTo(truckLeftV[3].x, truckLeftV[3].y);
        path.cubicTo(bottomL[1].x, bottomL[1].y, bottomL[3].x, bottomL[3].y, truckLeftV[2].x, truckLeftV[2].y);
        canvas.drawPath(path, paint);
        path.reset();

        path = new Path();
        path.moveTo(truckRightV[3].x, truckRightV[3].y);
        path.cubicTo(bottomR[1].x, bottomR[1].y, bottomR[3].x, bottomR[3].y, truckRightV[2].x, truckRightV[2].y);
        canvas.drawPath(path, paint);
        path.reset();

        int dim = 13;

        PointF[] lLeft = new PointF[dim];
        PointF[] lRight = new PointF[dim];

        PointF[] rLeft = new PointF[dim];
        PointF[] rRight = new PointF[dim];

        for (int i = 0; i < dim; i++) {
            lLeft[i] = new PointF(truckLeftV[3].x + i * (lv1x - truckLeftV[3].x) / (dim - 1), truckLeftV[3].y + i * (lv1y - truckLeftV[3].y) / (dim - 1));
            lRight[i] = new PointF(truckLeftV[2].x + i * (lv2x - truckLeftV[2].x) / (dim - 1), truckLeftV[2].y + i * (lv2y - truckLeftV[2].y) / (dim - 1));
            rLeft[i] = new PointF(truckRightV[3].x + i * (rv1x - truckRightV[3].x) / (dim - 1), truckRightV[3].y + i * (rv1y - truckRightV[3].y) / (dim - 1));
            rRight[i] = new PointF(truckRightV[2].x + i * (rv2x - truckRightV[2].x) / (dim - 1), truckRightV[2].y + i * (rv2y - truckRightV[2].y) / (dim - 1));
        }
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(7);
        for (int i = 0; i < dim; i++) {
            if (i != 0 && (i < dim - 1)) {
                canvas.drawLine(lLeft[i].x, lLeft[i].y, lRight[i].x, lRight[i].y, paint);
                canvas.drawLine(rLeft[i].x, rLeft[i].y, rRight[i].x, rRight[i].y, paint);
            }
        }

        paint.setColor(color);

        path.moveTo(frameVertex[1].x, frameVertex[1].y);
        path.lineTo(fw_x, fw_y);
        path.lineTo(frameVertex[0].x, frameVertex[0].y);
        path.lineTo(right_x, right_y);
        path.lineTo(frameVertex[2].x, frameVertex[2].y);
        path.lineTo(bwd_x, bwd_y);
        path.lineTo(frameVertex[3].x, frameVertex[3].y);
        path.lineTo(left_x, left_y);
        path.close();

        canvas.drawPath(path, paint);

        path.reset();


        //ellisse posteriore

        double backDistance = calculateDistance(frameVertex[3].x, frameVertex[3].y, frameVertex[2].x, frameVertex[2].y);

        PointF[] back = rectVertex(frameVertex[3].x, frameVertex[3].y, frameVertex[2].x, frameVertex[2].y, (float) (backDistance * 0.20));

        path = new Path();
        path.moveTo(frameVertex[3].x, frameVertex[3].y);
        path.cubicTo(back[1].x, back[1].y, back[3].x, back[3].y, frameVertex[2].x, frameVertex[2].y);
        canvas.drawPath(path, paint);
        path.reset();

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3.5f);
        path.moveTo(frameVertex[3].x, frameVertex[3].y);
        path.cubicTo(back[1].x, back[1].y, back[3].x, back[3].y, frameVertex[2].x, frameVertex[2].y);
        canvas.drawPath(path, paint);
        path.reset();
        //canvas.drawLine(frameVertex[3].x, frameVertex[3].y, frameVertex[2].x, frameVertex[2].y, paint);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        //cabina
        float dist = lFrame < wFrame ? (float) (lFrame * scala) : (float) (wFrame * scala);

        float topLeftX = (float) (frameVertex[1].x + (dist * 0.20) * Math.cos(Math.toRadians(45 + heading)));
        float topLeftY = (float) (frameVertex[1].y + (dist * 0.20) * Math.sin(Math.toRadians(45 + heading)));


        float topRightX = (float) (fw_x + (dist * 0.20) * Math.cos(Math.toRadians(90 + 45 + heading)));
        float topRightY = (float) (fw_y + (dist * 0.20) * Math.sin(Math.toRadians(90 + 45 + heading)));


        float bottomLeftX = (float) (left_x + (dist * 0.20) * Math.cos(Math.toRadians(-45 + heading)));
        float bottomLeftY = (float) (left_y + (dist * 0.20) * Math.sin(Math.toRadians(-45 + heading)));


        float bottomRightX = (float) (pivot.x + (dist * 0.20) * Math.cos(Math.toRadians(180 + 45 + heading)));
        float bottomRightY = (float) (pivot.y + (dist * 0.20) * Math.sin(Math.toRadians(180 + 45 + heading)));


        paint.setColor(Color.BLACK);

        path.moveTo(topLeftX, topLeftY);
        path.lineTo(topRightX, topRightY);
        path.lineTo(bottomRightX, bottomRightY);
        path.lineTo(bottomLeftX, bottomLeftY);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();

        //ellisse posteriore

        PointF[] roundCabina = rectVertex(topLeftX, topLeftY, topRightX, topRightY, (float) (dist * 0.15));

        path = new Path();
        path.moveTo(topLeftX, topLeftY);
        path.cubicTo(roundCabina[0].x, roundCabina[0].y, roundCabina[2].x, roundCabina[2].y, topRightX, topRightY);
        canvas.drawPath(path, paint);
        path.reset();


        //cofano
        float p1_x = (float) (bwd_x + (dist * 0.5) * Math.cos(Math.toRadians(180 + heading)));
        float p1_y = (float) (bwd_y + (dist * 0.5) * Math.sin(Math.toRadians(180 + heading)));
        float p2_x = (float) (bwd_x + (dist * 0.5) * Math.cos(Math.toRadians(heading)));
        float p2_y = (float) (bwd_y + (dist * 0.5) * Math.sin(Math.toRadians(heading)));
        float p3_x = (float) (p1_x + (dist * 0.5) * Math.cos(Math.toRadians(270 + heading)));
        float p3_y = (float) (p1_y + (dist * 0.5) * Math.sin(Math.toRadians(270 + heading)));
        float p4_x = (float) (p2_x + (dist * 0.5) * Math.cos(Math.toRadians(270 + heading)));
        float p4_y = (float) (p2_y + (dist * 0.5) * Math.sin(Math.toRadians(270 + heading)));

        path.moveTo(p1_x, p1_y);
        path.lineTo(p2_x, p2_y);
        path.lineTo(p4_x, p4_y);
        path.lineTo(p3_x, p3_y);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();
    }

    //disegna boom1

    /**
     * Disegna il boom1 dell'escavatore
     *
     * @param canvas il canvas di riferimento
     * @param paint  il paint di riferimento
     * @param color  il colore da assegnare
     * @param scala  la scala da assegnare
     */
    public void drawBoom1(Canvas canvas, Paint paint, int color, double scala) {

        double wBoom1 = DataSaved.larghezza_Braccio;

        canvas.drawCircle(degBoomRoll.x, degBoomRoll.y, (float) (wBoom1 * scala) / 2f, paint);

        PointF[] boom1Vertex = rectVertex(boom1.x, boom1.y, degBoomRoll.x, degBoomRoll.y, (float) (wBoom1 * scala));

        paint.setStrokeWidth(7);
        paint.setColor(Color.BLACK);
        //canvas.drawLine(boom1Vertex[0].x, boom1Vertex[0].y, boom1Vertex[1].x, boom1Vertex[1].y, paint);
        canvas.drawLine(boom1Vertex[0].x, boom1Vertex[0].y, boom1Vertex[2].x, boom1Vertex[2].y, paint);
        canvas.drawLine(boom1Vertex[2].x, boom1Vertex[2].y, boom1Vertex[3].x, boom1Vertex[3].y, paint);
        canvas.drawLine(boom1Vertex[3].x, boom1Vertex[3].y, boom1Vertex[1].x, boom1Vertex[1].y, paint);

        paint.setColor(color);

        Path path = new Path();

        path.moveTo(boom1Vertex[0].x, boom1Vertex[0].y);
        path.lineTo(boom1Vertex[1].x, boom1Vertex[1].y);
        path.lineTo(boom1Vertex[3].x, boom1Vertex[3].y);
        path.lineTo(boom1Vertex[2].x, boom1Vertex[2].y);
        path.close();

        canvas.drawPath(path, paint);

        path.reset();


        PointF[] midBottomVertex = rectVertex(boom1Vertex[0].x, boom1Vertex[0].y, boom1Vertex[1].x, boom1Vertex[1].y, (float) (calculateDistance(boom1.x, boom1.y, degBoomRoll.x, degBoomRoll.y) * 0.30f));
        PointF[] midTopVertex = rectVertex(boom1Vertex[2].x, boom1Vertex[2].y, boom1Vertex[3].x, boom1Vertex[3].y, (float) (calculateDistance(boom1.x, boom1.y, degBoomRoll.x, degBoomRoll.y) * 0.15f));

        float mxBottom = (midBottomVertex[0].x + midBottomVertex[2].x) / 2;
        float myBottom = (midBottomVertex[0].y + midBottomVertex[2].y) / 2;

        float mxTop = (midTopVertex[1].x + midTopVertex[3].x) / 2;
        float myTop = (midTopVertex[1].y + midTopVertex[3].y) / 2;


        if (boom1.equals(boom2.x, boom2.y)) {
            paint.setColor(Color.DKGRAY);
            paint.setStrokeWidth((float) (wBoom1 * scala * 0.7f));
            canvas.drawLine(boom2.x, boom2.y, mxBottom, myBottom, paint);

            paint.setColor(color);
            double dist = calculateDistance(boom1Vertex[0].x, boom1Vertex[0].y, boom1Vertex[1].x, boom1Vertex[1].y);
            PointF[] roundCurve = rectVertex(boom1Vertex[0].x, boom1Vertex[0].y, boom1Vertex[1].x, boom1Vertex[1].y, (float) (dist * 0.20));
            path.moveTo(boom1Vertex[0].x, boom1Vertex[0].y);
            path.cubicTo(roundCurve[1].x, roundCurve[1].y, roundCurve[3].x, roundCurve[3].y, boom1Vertex[1].x, boom1Vertex[1].y);
            canvas.drawPath(path, paint);
            path.reset();
        }

        paint.setColor(Color.GRAY);
        paint.setStrokeWidth((float) (wBoom1 * scala * 0.2f));
        canvas.drawLine(mxBottom, myBottom, mxTop, myTop, paint);

        //paint.setColor(color);
        //canvas.drawCircle(pitch.x, pitch.y, (float) (calculateDistance(boom1Vertex[2].x, boom1Vertex[2].y, boom1Vertex[3].x, boom1Vertex[3].y) * 1.25), paint);
    }

    //disegna boom2

    /**
     * Disegna il boom2 dell'escavatore
     *
     * @param canvas il canvas di riferimento
     * @param paint  il paint di riferimento
     * @param color  il colore da assegnare
     * @param scala  la scala da assegnare
     */
    public void drawBoom2(Canvas canvas, Paint paint, int color, double scala) {
        double wStick = DataSaved.larghezza_Braccio;

        PointF[] boom2Vertex = rectVertex(boom2.x, boom2.y, boom1.x, boom1.y, (float) (wStick * scala));

        paint.setStrokeWidth(7);
        paint.setColor(Color.BLACK);
        //canvas.drawLine(boom2Vertex[0].x, boom2Vertex[0].y, boom2Vertex[1].x, boom2Vertex[1].y, paint);
        //canvas.drawLine(boom2Vertex[0].x, boom2Vertex[0].y, boom2Vertex[2].x, boom2Vertex[2].y, paint);
        //canvas.drawLine(boom2Vertex[2].x, boom2Vertex[2].y, boom2Vertex[3].x, boom2Vertex[3].y, paint);
        //canvas.drawLine(boom2Vertex[3].x, boom2Vertex[3].y, boom2Vertex[1].x, boom2Vertex[1].y, paint);

        paint.setColor(color);

        Path path = new Path();

        path.moveTo(boom2Vertex[0].x, boom2Vertex[0].y);
        path.lineTo(boom2Vertex[1].x, boom2Vertex[1].y);
        path.lineTo(boom2Vertex[3].x, boom2Vertex[3].y);
        path.lineTo(boom2Vertex[2].x, boom2Vertex[2].y);
        path.close();

        canvas.drawPath(path, paint);

        path.reset();

        paint.setColor(color);
        double dist = calculateDistance(boom2Vertex[0].x, boom2Vertex[0].y, boom2Vertex[1].x, boom2Vertex[1].y);
        PointF[] roundCurve = rectVertex(boom2Vertex[0].x, boom2Vertex[0].y, boom2Vertex[1].x, boom2Vertex[1].y, (float) (dist * 0.20));
        path.moveTo(boom2Vertex[0].x, boom2Vertex[0].y);
        path.cubicTo(roundCurve[1].x, roundCurve[1].y, roundCurve[3].x, roundCurve[3].y, boom2Vertex[1].x, boom2Vertex[1].y);
        canvas.drawPath(path, paint);
        path.reset();

        PointF[] midPVertex = rectVertex(boom2Vertex[0].x, boom2Vertex[0].y, boom2Vertex[1].x, boom2Vertex[1].y, (float) (calculateDistance(boom2.x, boom2.y, boom1.x, boom1.y) * 0.30f));

        float mx = (midPVertex[0].x + midPVertex[2].x) / 2;
        float my = (midPVertex[0].y + midPVertex[2].y) / 2;

        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth((float) (wStick * scala * 0.7f));
        canvas.drawLine(boom2.x, boom2.y, mx, my, paint);
    }

    //disegna stick

    /**
     * Disegna lo stick dell'escavatore
     *
     * @param canvas il canvas di riferimento
     * @param paint  il paint di riferimento
     * @param color  il colore da assegnare
     * @param scala  la scala da assegnare
     */
    public void drawStick(Canvas canvas, Paint paint, int color, double scala) {

        double wStick = DataSaved.larghezza_Braccio * 0.75f;

        PointF[] stickVertex = rectVertex(stick.x, stick.y, boom2.x, boom2.y, (float) (wStick * scala));

        paint.setStrokeWidth(7);
        paint.setColor(Color.BLACK);
        canvas.drawLine(stickVertex[0].x, stickVertex[0].y, stickVertex[1].x, stickVertex[1].y, paint);
        canvas.drawLine(stickVertex[0].x, stickVertex[0].y, stickVertex[2].x, stickVertex[2].y, paint);
        canvas.drawLine(stickVertex[2].x, stickVertex[2].y, stickVertex[3].x, stickVertex[3].y, paint);
        canvas.drawLine(stickVertex[3].x, stickVertex[3].y, stickVertex[1].x, stickVertex[1].y, paint);

        paint.setColor(color);

        Path path = new Path();

        path.moveTo(stickVertex[0].x, stickVertex[0].y);
        path.lineTo(stickVertex[1].x, stickVertex[1].y);
        path.lineTo(stickVertex[3].x, stickVertex[3].y);
        path.lineTo(stickVertex[2].x, stickVertex[2].y);
        path.close();

        canvas.drawPath(path, paint);

        path.reset();

        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth((float) (wStick * scala * 0.5f));
        canvas.drawLine(stick.x, stick.y, boom2.x, boom2.y, paint);

        double dist = calculateDistance(stickVertex[0].x, stickVertex[0].y, stickVertex[1].x, stickVertex[1].y);

        PointF[] roundStick = rectVertex(stickVertex[0].x, stickVertex[0].y, stickVertex[1].x, stickVertex[1].y, (float) (dist * 0.15));

        path = new Path();
        path.moveTo(stickVertex[0].x, stickVertex[0].y);
        path.cubicTo(roundStick[0].x, roundStick[0].y, roundStick[2].x, roundStick[2].y, stickVertex[1].x, stickVertex[1].y);
        canvas.drawPath(path, paint);
        path.reset();


    }

    //disegna bucket

    /**
     * Disegna il bucket dell'escavatore
     *
     * @param canvas il canvas di riferimento
     * @param paint  il paint di riferimento
     * @param color  il colore da assegnare
     */
    public void drawBucket(Canvas canvas, Paint paint, int color) {

        float heading = 0;


        PointF[] bucketVertex = rectVertex(bucketLeft.x, bucketLeft.y, bucketRight.x, bucketRight.y, (float) calculateDistance(bucket.x, bucket.y, stick.x, stick.y));

        double distStickCentro = DistToPoint.getDist_to_point(stick.x, stick.y, 0, pitch.x, pitch.y, 0);

        double distBucketCentro = DistToPoint.getDist_to_point(bucket.x, bucket.y, 0, pitch.x, pitch.y, 0);

        System.out.println("DIST STICK CENTRO: " + distStickCentro);

        System.out.println("DIST BUCKET CENTRO: " + distBucketCentro);

        if (distStickCentro < distBucketCentro) {

            System.out.println("BUCKET AVANTI");

            paint.setStrokeWidth(7);
            paint.setColor(Color.BLACK);
            canvas.drawLine(bucketLeft.x, bucketLeft.y, bucketRight.x, bucketRight.y, paint);
            canvas.drawLine(bucketRight.x, bucketRight.y, bucketVertex[3].x, bucketVertex[3].y, paint);
            canvas.drawLine(bucketVertex[3].x, bucketVertex[3].y, bucketVertex[1].x, bucketVertex[1].y, paint);
            canvas.drawLine(bucketVertex[1].x, bucketVertex[1].y, bucketLeft.x, bucketLeft.y, paint);

            paint.setColor(color);

            Path path = new Path();

            path.moveTo(bucketLeft.x, bucketLeft.y);
            path.lineTo(bucketRight.x, bucketRight.y);
            path.lineTo(bucketVertex[3].x, bucketVertex[3].y);
            path.lineTo(bucketVertex[1].x, bucketVertex[1].y);
            path.close();
            canvas.drawPath(path, paint);
            path.reset();

            int dim = 10;

            PointF[] bucketTeethBase = new PointF[dim];
            PointF[] bucketTeethEnd = new PointF[dim];

            float dist = (float) (calculateDistance(bucketLeft.x, bucketLeft.y, bucketRight.x, bucketRight.y) * 0.10f);

            paint.setColor(Color.WHITE);

            for (int i = 0; i < dim; i++) {
                bucketTeethBase[i] = new PointF(bucketLeft.x + i * (bucketRight.x - bucketLeft.x) / (dim - 1), bucketLeft.y + i * (bucketRight.y - bucketLeft.y) / (dim - 1));
                bucketTeethEnd[i] = new PointF(
                        (float) (bucketTeethBase[i].x + (dist * 0.5) * Math.cos(Math.toRadians(270 + heading))),
                        (float) (bucketTeethBase[i].y + (dist * 0.5) * Math.sin(Math.toRadians(270 + heading)))
                );
            }

            paint.setColor(Color.GRAY);

            for (int i = 0; i < dim; i += 2) {
                path.moveTo(bucketTeethBase[i].x, bucketTeethBase[i].y);
                path.lineTo(bucketTeethBase[i + 1].x, bucketTeethBase[i + 1].y);
                path.lineTo(bucketTeethEnd[i + 1].x, bucketTeethEnd[i + 1].y);
                path.lineTo(bucketTeethEnd[i].x, bucketTeethEnd[i].y);
                path.close();
                canvas.drawPath(path, paint);
                path.reset();
            }

        } else {
            System.out.println("BUCKET INDIETRO");

            paint.setStrokeWidth(7);
            paint.setColor(Color.BLACK);
            canvas.drawLine(bucketLeft.x, bucketLeft.y, bucketRight.x, bucketRight.y, paint);
            canvas.drawLine(bucketRight.x, bucketRight.y, bucketVertex[2].x, bucketVertex[2].y, paint);
            canvas.drawLine(bucketVertex[2].x, bucketVertex[2].y, bucketVertex[0].x, bucketVertex[0].y, paint);
            canvas.drawLine(bucketVertex[0].x, bucketVertex[0].y, bucketLeft.x, bucketLeft.y, paint);

            paint.setColor(color);

            Path path = new Path();

            path.moveTo(bucketLeft.x, bucketLeft.y);
            path.lineTo(bucketRight.x, bucketRight.y);
            path.lineTo(bucketVertex[2].x, bucketVertex[2].y);
            path.lineTo(bucketVertex[0].x, bucketVertex[0].y);
            path.close();
            canvas.drawPath(path, paint);
            path.reset();


            PointF[] leftVertex = rectVertex(bucketLeft.x, bucketLeft.y, bucketVertex[0].x, bucketVertex[0].y, (float) calculateDistance(bucket.x, bucket.y, stick.x, stick.y) * 0.2f);
            PointF[] rightVertex = rectVertex(bucketRight.x, bucketRight.y, bucketVertex[2].x, bucketVertex[2].y, (float) calculateDistance(bucket.x, bucket.y, stick.x, stick.y) * 0.2f);
            PointF[] topVertex = rectVertex(bucketVertex[2].x, bucketVertex[2].y, bucketVertex[0].x, bucketVertex[0].y, (float) calculateDistance(bucket.x, bucket.y, stick.x, stick.y) * 0.2f);
            PointF[] bottomVertex = rectVertex(bucketLeft.x, bucketLeft.y, bucketRight.x, bucketRight.y, (float) calculateDistance(bucket.x, bucket.y, stick.x, stick.y) * 0.2f);


            double m1 = (leftVertex[3].y - leftVertex[1].y) / (leftVertex[3].x - leftVertex[1].x);
            double q1 = leftVertex[1].y - m1 * leftVertex[1].x;

            double m2 = (topVertex[2].y - topVertex[0].y) / (topVertex[2].x - topVertex[0].x);
            double q2 = topVertex[0].y - m2 * topVertex[0].x;

            double m3 = (rightVertex[2].y - rightVertex[0].y) / (rightVertex[2].x - rightVertex[0].x);
            double q3 = rightVertex[0].y - m3 * rightVertex[0].x;

            double m4 = (bottomVertex[2].y - bottomVertex[0].y) / (bottomVertex[2].x - bottomVertex[0].x);
            double q4 = bottomVertex[0].y - m4 * bottomVertex[0].x;


            float topLeftX = (float) ((q2 - q1) / (m1 - m2));
            float topLeftY = (float) (m1 * topLeftX + q1);

            float topRightX = (float) ((q2 - q3) / (m3 - m2));
            float topRightY = (float) (m3 * topRightX + q3);

            float bottomLeftX = (float) ((q4 - q1) / (m1 - m4));
            float bottomLeftY = (float) (m1 * bottomLeftX + q1);

            float bottomRightX = (float) ((q4 - q3) / (m3 - m4));
            float bottomRightY = (float) (m3 * bottomRightX + q3);

            paint.setColor(Color.DKGRAY);

            path.moveTo(topLeftX, topLeftY);
            path.lineTo(topRightX, topRightY);
            path.lineTo(bottomRightX, bottomRightY);
            path.lineTo(bottomLeftX, bottomLeftY);
            path.close();
            canvas.drawPath(path, paint);
            path.reset();

            int dim = 10;

            PointF[] bucketTeethBase = new PointF[dim];
            PointF[] bucketTeethEnd = new PointF[dim];

            float dist = (float) (calculateDistance(bucketLeft.x, bucketLeft.y, bucketRight.x, bucketRight.y) * 0.10f);

            paint.setColor(Color.WHITE);

            for (int i = 0; i < dim; i++) {
                bucketTeethBase[i] = new PointF(bucketLeft.x + i * (bucketRight.x - bucketLeft.x) / (dim - 1), bucketLeft.y + i * (bucketRight.y - bucketLeft.y) / (dim - 1));
                bucketTeethEnd[i] = new PointF(
                        (float) (bucketTeethBase[i].x + (dist * 0.5) * Math.cos(Math.toRadians(90 + heading))),
                        (float) (bucketTeethBase[i].y + (dist * 0.5) * Math.sin(Math.toRadians(90 + heading)))
                );
            }

            paint.setColor(Color.DKGRAY);

            for (int i = 0; i < dim; i += 2) {
                path.moveTo(bucketTeethBase[i].x, bucketTeethBase[i].y);
                path.lineTo(bucketTeethBase[i + 1].x, bucketTeethBase[i + 1].y);
                path.lineTo(bucketTeethEnd[i + 1].x, bucketTeethEnd[i + 1].y);
                path.lineTo(bucketTeethEnd[i].x, bucketTeethEnd[i].y);
                path.close();
                canvas.drawPath(path, paint);
                path.reset();
            }
        }
    }


    /**
     * Calcola i vertici di un rettangolo con lati paralleli agli assi x e y,
     * avente un angolo in basso a sinistra e un angolo in alto a destra.
     *
     * @param x1    coordinata x dell'angolo in basso a sinistra
     * @param y1    coordinata y dell'angolo in basso a sinistra
     * @param x2    coordinata x dell'angolo in alto a destra
     * @param y2    coordinata y dell'angolo in alto a destra
     * @param width larghezza del rettangolo
     * @return array di PointF contenente i vertici del rettangolo
     */
    public PointF[] rectVertex(float x1, float y1, float x2, float y2, float width) {
        PointF v = new PointF(x2 - x1, y2 - y1);
        PointF p = new PointF(v.y, -v.x);
        float length = (float) Math.sqrt(p.x * p.x + p.y * p.y);
        PointF n = new PointF(p.x / length, p.y / length);

        PointF p1 = new PointF(x1 + n.x * width, y1 + n.y * width);
        PointF p2 = new PointF(x1 - n.x * width, y1 - n.y * width);
        PointF p3 = new PointF(x2 + n.x * width, y2 + n.y * width);
        PointF p4 = new PointF(x2 - n.x * width, y2 - n.y * width);

        return new PointF[]{p1, p2, p3, p4};
    }

    /**
     * Calcola la distanza tra due punti
     *
     * @param x1 coordinata x del punto 1
     * @param y1 coordinata y del punto 1
     * @param x2 coordinata x del punto 2
     * @param y2 coordinata y del punto 2
     * @return double della distanza tra i due punti
     */
    public double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }


}
