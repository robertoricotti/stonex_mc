package gui.draw_class;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

public class DozerDrawer {

    PointF startXYZ;
    PointF GPS_Z;
    PointF GPS_DX;
    PointF GPS_DY;
    PointF bladeCenter;
    PointF bladeLeft;
    PointF bladeRight;

    PointD rStartXYZ;
    PointD rGPS_Z;
    PointD rGPS_DX;
    PointD rGPS_DY;
    PointD rBladeCenter;
    PointD rBladeLeft;
    PointD rBladeRight;

    public DozerDrawer() {
    }

    // inizializza i punti di congiunzioni reali secondo il pivot point desiderato
    public void update(float canvasWidth, float canvasHeight, String pivotPoint, float percX, float percY, double scala) {

        double heading = 166;

        rStartXYZ = new PointD(514238.763, 5045440.202);
        rGPS_Z = new PointD(514238.740, 5045440.225);
        rGPS_DX = new PointD(514237.918, 5045440.027);
        rGPS_DY = new PointD(514237.974, 5045439.793);
        rBladeCenter = new PointD(514238.602, 5045437.553);
        rBladeLeft = new PointD(514238.990, 5045437.649);
        rBladeRight = new PointD(514238.213, 5045437.458);

        PointF pivot = new PointF(canvasWidth * percX, canvasHeight * percY);

        PointD fixPoint;

        if (pivotPoint.equalsIgnoreCase("GPS_DY")) {
            fixPoint = new PointD(rGPS_DY.x, rGPS_DY.y);
        } else {
            fixPoint = new PointD(rBladeCenter.x, rBladeCenter.y);
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

        double diff_x_blade = (rBladeCenter.x - fixPoint.x) * scala;
        double diff_y_blade = (rBladeCenter.y - fixPoint.y) * scala;

        /*bladeCenter = new PointF(
                (float) (pivot.x + diff_x_blade * Math.cos(Math.toRadians(heading)) - diff_y_blade * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_blade * Math.sin(Math.toRadians(heading)) - diff_y_blade * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_bladeLeft = (rBladeLeft.x - fixPoint.x) * scala;
        double diff_y_bladeLeft = (rBladeLeft.y - fixPoint.y) * scala;

        bladeLeft = new PointF(
                (float) (pivot.x + diff_x_bladeLeft * Math.cos(Math.toRadians(heading)) - diff_y_bladeLeft * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_bladeLeft * Math.sin(Math.toRadians(heading)) - diff_y_bladeLeft * Math.cos(Math.toRadians(heading)))
        );

        double diff_x_bladeRight = (rBladeRight.x - fixPoint.x) * scala;
        double diff_y_bladeRight = (rBladeRight.y - fixPoint.y) * scala;

        bladeRight = new PointF(
                (float) (pivot.x + diff_x_bladeRight * Math.cos(Math.toRadians(heading)) - diff_y_bladeRight * Math.sin(Math.toRadians(heading))),
                (float) (pivot.y - diff_x_bladeRight * Math.sin(Math.toRadians(heading)) - diff_y_bladeRight * Math.cos(Math.toRadians(heading)))
        );*/

        bladeCenter = new PointF(canvasWidth / 2f, canvasHeight / 2f - 100);

        bladeLeft = new PointF(canvasWidth / 2f - 200, canvasHeight / 2f - 100);

        bladeRight = new PointF(canvasWidth / 2f + 200, canvasHeight / 2f - 100);
    }

    public void drawFrame(Canvas canvas, Paint paint, int color, double scala) {

        double heading = 0;

        double lFrame = 1.30;
        double wFrame = 1.10;

        PointF pivot = new PointF(GPS_DY.x, GPS_DY.y);

        float fw_x = (float) (pivot.x + (lFrame * scala) * Math.cos(Math.toRadians(270 + heading)));
        float fw_y = (float) (pivot.y + (lFrame * scala) * Math.sin(Math.toRadians(270 + heading)));

        float bwd_x = (float) (pivot.x + (lFrame * scala) * Math.cos(Math.toRadians(90 + heading)));
        float bwd_y = (float) (pivot.y + (lFrame * scala) * Math.sin(Math.toRadians(90 + heading)));

        float left_x = (float) (pivot.x + (wFrame * scala) * Math.cos(Math.toRadians(180 + heading)));
        float left_y = (float) (pivot.y + (wFrame * scala) * Math.sin(Math.toRadians(180 + heading)));

        float right_x = (float) (pivot.x + (wFrame * scala) * Math.cos(Math.toRadians(0 + heading)));
        float right_y = (float) (pivot.y + (wFrame * scala) * Math.sin(Math.toRadians(0 + heading)));


        /*canvas.drawCircle(fw_x, fw_y, 10, paint);
        canvas.drawCircle(bwd_x, bwd_y, 10, paint);
        canvas.drawCircle(left_x, left_y, 10, paint);
        canvas.drawCircle(right_x, right_y, 10, paint);*/

        PointF[] frameVertex = rectVertex(fw_x, fw_y, bwd_x, bwd_y, (float) calculateDistance(pivot.x, pivot.y, left_x, left_y));

        paint.setStrokeWidth(7);
        paint.setColor(Color.BLACK);

        /*paint.setColor(Color.GREEN);
        canvas.drawCircle(frameVertex[0].x, frameVertex[0].y, 10, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(frameVertex[1].x, frameVertex[1].y, 10, paint);
        paint.setColor(Color.MAGENTA);
        canvas.drawCircle(frameVertex[2].x, frameVertex[2].y, 10, paint);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(frameVertex[3].x, frameVertex[3].y, 10, paint);


        paint.setColor(Color.WHITE);
        canvas.drawCircle(pivot.x, pivot.y, 10, paint);*/

        float distL = (float) calculateDistance(fw_x, fw_y, bwd_x, bwd_y);
        float distW = (float) calculateDistance(left_x, left_y, right_x, right_y);

        float top_x = (float) (pivot.x + (distL * 0.15f) * Math.cos(Math.toRadians(270 + heading)));
        float top_y = (float) (pivot.y + (distL * 0.15f) * Math.sin(Math.toRadians(270 + heading)));

        float med_x = (float) (pivot.x + (distL * 0.15f) * Math.cos(Math.toRadians(90 + heading)));
        float med_y = (float) (pivot.y + (distL * 0.15f) * Math.sin(Math.toRadians(90 + heading)));

        float bottom_left_x = (float) (bwd_x + (distW * 0.22f) * Math.cos(Math.toRadians(180 + heading)));
        float bottom_left_y = (float) (bwd_y + (distW * 0.22f) * Math.sin(Math.toRadians(180 + heading)));

        float bottom_right_x = (float) (bwd_x + (distW * 0.22f) * Math.cos(Math.toRadians(0 + heading)));
        float bottom_right_y = (float) (bwd_y + (distW * 0.22f) * Math.sin(Math.toRadians(0 + heading)));

        float med_left_x = (float) (med_x + (distW * 0.22f) * Math.cos(Math.toRadians(180 + heading)));
        float med_left_y = (float) (med_y + (distW * 0.22f) * Math.sin(Math.toRadians(180 + heading)));

        float med_right_x = (float) (med_x + (distW * 0.22f) * Math.cos(Math.toRadians(0 + heading)));
        float med_right_y = (float) (med_y + (distW * 0.22f) * Math.sin(Math.toRadians(0 + heading)));

        float top_left_x = (float) (top_x + (distW * 0.15f) * Math.cos(Math.toRadians(180 + heading)));
        float top_left_y = (float) (top_y + (distW * 0.15f) * Math.sin(Math.toRadians(180 + heading)));

        float top_right_x = (float) (top_x + (distW * 0.15f) * Math.cos(Math.toRadians(0 + heading)));
        float top_right_y = (float) (top_y + (distW * 0.15f) * Math.sin(Math.toRadians(0 + heading)));

        float pivot_left_x = (float) (pivot.x + (distW * 0.15f) * Math.cos(Math.toRadians(180 + heading)));
        float pivot_left_y = (float) (pivot.y + (distW * 0.15f) * Math.sin(Math.toRadians(180 + heading)));

        float pivot_right_x = (float) (pivot.x + (distW * 0.15f) * Math.cos(Math.toRadians(0 + heading)));
        float pivot_right_y = (float) (pivot.y + (distW * 0.15f) * Math.sin(Math.toRadians(0 + heading)));

        float bottom_left_s_x = (float) (bwd_x + (distW * 0.30f) * Math.cos(Math.toRadians(180 + heading)));
        float bottom_left_s_y = (float) (bwd_y + (distW * 0.30f) * Math.sin(Math.toRadians(180 + heading)));

        float med_left_s_x = (float) (med_x + (distW * 0.30f) * Math.cos(Math.toRadians(180 + heading)));
        float med_left_s_y = (float) (med_y + (distW * 0.30f) * Math.sin(Math.toRadians(180 + heading)));

        float bottom_right_s_x = (float) (bwd_x + (distW * 0.30f) * Math.cos(Math.toRadians(0 + heading)));
        float bottom_right_s_y = (float) (bwd_y + (distW * 0.30f) * Math.sin(Math.toRadians(0 + heading)));

        float med_right_s_x = (float) (med_x + (distW * 0.30f) * Math.cos(Math.toRadians(0 + heading)));
        float med_right_s_y = (float) (med_y + (distW * 0.30f) * Math.sin(Math.toRadians(0 + heading)));

        float pivot_left_truck_x = (float) (pivot.x + (distW * 0.20f) * Math.cos(Math.toRadians(180 + heading)));
        float pivot_left_truck_y = (float) (pivot.y + (distW * 0.20f) * Math.sin(Math.toRadians(180 + heading)));

        float pivot_right_truck_x = (float) (pivot.x + (distW * 0.20f) * Math.cos(Math.toRadians(0 + heading)));
        float pivot_right_truck_y = (float) (pivot.y + (distW * 0.20f) * Math.sin(Math.toRadians(0 + heading)));

        float topLeftTruck_x = (float) (pivot_left_truck_x + (distL * 0.50f) * Math.cos(Math.toRadians(270 + heading)));
        float topLeftTruck_y = (float) (pivot_left_truck_y + (distL * 0.50f) * Math.sin(Math.toRadians(270 + heading)));

        float topRightTruck_x = (float) (pivot_right_truck_x + (distL * 0.50f) * Math.cos(Math.toRadians(270 + heading)));
        float topRightTruck_y = (float) (pivot_right_truck_y + (distL * 0.50f) * Math.sin(Math.toRadians(270 + heading)));

        float bottomLeftTruck_x = (float) (pivot_left_truck_x + (distL * 0.50f) * Math.cos(Math.toRadians(90 + heading)));
        float bottomLeftTruck_y = (float) (pivot_left_truck_y + (distL * 0.50f) * Math.sin(Math.toRadians(90 + heading)));

        float bottomRightTruck_x = (float) (pivot_right_truck_x + (distL * 0.50f) * Math.cos(Math.toRadians(90 + heading)));
        float bottomRightTruck_y = (float) (pivot_right_truck_y + (distL * 0.50f) * Math.sin(Math.toRadians(90 + heading)));

        float topLeftFront_x = (float) (pivot_left_x + (distL * 0.50f) * Math.cos(Math.toRadians(270 + heading)));
        float topLeftFront_y = (float) (pivot_left_y + (distL * 0.50f) * Math.sin(Math.toRadians(270 + heading)));

        float topRightFront_x = (float) (pivot_right_x + (distL * 0.50f) * Math.cos(Math.toRadians(270 + heading)));
        float topRightFront_y = (float) (pivot_right_y + (distL * 0.50f) * Math.sin(Math.toRadians(270 + heading)));

        Path path = new Path();

        paint.setColor(Color.BLACK);

        path.moveTo(frameVertex[3].x, frameVertex[3].y);
        path.lineTo(frameVertex[1].x, frameVertex[1].y);
        path.lineTo(topLeftTruck_x, topLeftTruck_y);
        path.lineTo(bottomLeftTruck_x, bottomLeftTruck_y);
        path.close();

        canvas.drawPath(path, paint);
        path.reset();

        path.moveTo(frameVertex[2].x, frameVertex[2].y);
        path.lineTo(frameVertex[0].x, frameVertex[0].y);
        path.lineTo(topRightTruck_x, topRightTruck_y);
        path.lineTo(bottomRightTruck_x, bottomRightTruck_y);
        path.close();

        canvas.drawPath(path, paint);
        path.reset();

        double distTruck = calculateDistance(frameVertex[1].x, frameVertex[1].y, topLeftTruck_x, topLeftTruck_y);

        PointF[] topL = rectVertex(frameVertex[1].x, frameVertex[1].y, topLeftTruck_x, topLeftTruck_y, (float) (distTruck * 0.05));
        PointF[] topR = rectVertex(topRightTruck_x, topRightTruck_y, frameVertex[0].x, frameVertex[0].y, (float) (distTruck * 0.05));

        path.moveTo(frameVertex[1].x, frameVertex[1].y);
        path.cubicTo(topL[0].x, topL[0].y, topL[2].x, topL[2].y, topLeftTruck_x, topLeftTruck_y);
        canvas.drawPath(path, paint);
        path.reset();


        path.moveTo(topRightTruck_x, topRightTruck_y);
        path.cubicTo(topR[0].x, topR[0].y, topR[2].x, topR[2].y, frameVertex[0].x, frameVertex[0].y);
        canvas.drawPath(path, paint);
        path.reset();


        int dim = 13;

        PointF[] lLeft = new PointF[dim];
        PointF[] lRight = new PointF[dim];

        PointF[] rLeft = new PointF[dim];
        PointF[] rRight = new PointF[dim];

        for (int i = 0; i < dim; i++) {
            lLeft[i] = new PointF(frameVertex[3].x + i * (frameVertex[1].x - frameVertex[3].x) / (dim - 1), frameVertex[3].y + i * (frameVertex[1].y - frameVertex[3].y) / (dim - 1));
            lRight[i] = new PointF(bottomLeftTruck_x + i * (topLeftTruck_x - bottomLeftTruck_x) / (dim - 1), bottomLeftTruck_y + i * (topLeftTruck_y - bottomLeftTruck_y) / (dim - 1));
            rLeft[i] = new PointF(frameVertex[2].x + i * (frameVertex[0].x - frameVertex[2].x) / (dim - 1), frameVertex[2].y + i * (frameVertex[0].y - frameVertex[2].y) / (dim - 1));
            rRight[i] = new PointF(bottomRightTruck_x + i * (topRightTruck_x - bottomRightTruck_x) / (dim - 1), bottomRightTruck_y + i * (topRightTruck_y - bottomRightTruck_y) / (dim - 1));
        }
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth((distL / dim) * 0.5f);
        for (int i = 0; i < dim; i++) {
            if (i != 0 && (i < dim - 1)) {
                canvas.drawLine(lLeft[i].x, lLeft[i].y, lRight[i].x, lRight[i].y, paint);
                canvas.drawLine(rLeft[i].x, rLeft[i].y, rRight[i].x, rRight[i].y, paint);
            }
        }

        paint.setColor(color);
        paint.setStrokeWidth(10);

        canvas.drawLine(bottom_left_x, bottom_left_y, bottom_left_s_x, bottom_left_s_y, paint);
        canvas.drawLine(bottom_left_s_x, bottom_left_s_y, med_left_s_x, med_left_s_y, paint);
        canvas.drawLine(med_left_s_x, med_left_s_y, med_left_x, med_left_y, paint);

        canvas.drawLine(bottom_right_x, bottom_right_y, bottom_right_s_x, bottom_right_s_y, paint);
        canvas.drawLine(bottom_right_s_x, bottom_right_s_y, med_right_s_x, med_right_s_y, paint);
        canvas.drawLine(med_right_s_x, med_right_s_y, med_right_x, med_right_y, paint);

        paint.setColor(Color.GRAY);

        canvas.drawLine(med_left_s_x, med_left_s_y, top_left_x, top_left_y, paint);
        canvas.drawLine(med_right_s_x, med_right_s_y, top_right_x, top_right_y, paint);


        paint.setColor(color);

        path.moveTo(top_left_x, top_left_y);
        path.lineTo(top_right_x, top_right_y);
        path.lineTo(pivot_right_x, pivot_right_y);
        path.lineTo(pivot_left_x, pivot_left_y);
        path.close();

        canvas.drawPath(path, paint);

        path.reset();

        path.moveTo(top_left_x, top_left_y);
        path.lineTo(topLeftFront_x, topLeftFront_y);
        path.lineTo(topRightFront_x, topRightFront_y);
        path.lineTo(top_right_x, top_right_y);
        path.close();

        canvas.drawPath(path, paint);

        path.reset();


        paint.setColor(Color.DKGRAY);

        canvas.drawLine(top_left_x, top_left_y, top_right_x, top_right_y, paint);

        path.moveTo(bottom_left_x, bottom_left_y);
        path.lineTo(bottom_right_x, bottom_right_y);
        path.lineTo(med_right_x, med_right_y);
        path.lineTo(pivot_right_x, pivot_right_y);
        path.lineTo(pivot_left_x, pivot_left_y);
        path.lineTo(med_left_x, med_left_y);
        path.lineTo(bottom_left_x, bottom_left_y);
        path.close();

        canvas.drawPath(path, paint);

        path.reset();

        paint.setColor(Color.YELLOW);

        float back_left_x = (float) (bwd_x + (distW * 0.07f) * Math.cos(Math.toRadians(180 + heading)));
        float back_left_y = (float) (bwd_y + (distW * 0.07f) * Math.sin(Math.toRadians(180 + heading)));

        float back_right_x = (float) (bwd_x + (distW * 0.07f) * Math.cos(Math.toRadians(0 + heading)));
        float back_right_y = (float) (bwd_y + (distW * 0.07f) * Math.sin(Math.toRadians(0 + heading)));

        float backB_left_x = (float) (back_left_x + (distL * 0.07f) * Math.cos(Math.toRadians(90 + heading)));
        float backB_left_y = (float) (back_left_y + (distL * 0.07f) * Math.sin(Math.toRadians(90 + heading)));

        float backB_right_x = (float) (back_right_x + (distL * 0.07f) * Math.cos(Math.toRadians(90 + heading)));
        float backB_right_y = (float) (back_right_y + (distL * 0.07f) * Math.sin(Math.toRadians(90 + heading)));


        paint.setStrokeWidth(15);
        paint.setColor(color);

        canvas.drawLine(back_left_x, back_left_y, backB_left_x, backB_left_y, paint);
        canvas.drawLine(back_right_x, back_right_y, backB_right_x, backB_right_y, paint);


        PointF[] backLeft = rectVertex(back_left_x, back_left_y, backB_left_x, backB_left_y, distW * 0.30f);

        PointF[] backRight = rectVertex(back_right_x, back_right_y, backB_right_x, backB_right_y, distW * 0.30f);


        PointF[] back = rectVertex(backLeft[3].x, backLeft[3].y, backRight[2].x, backRight[2].y, (distL * 0.03f));


        path.moveTo(back[0].x, back[0].y);
        path.lineTo(back[2].x, back[2].y);
        path.lineTo(back[3].x, back[3].y);
        path.lineTo(back[1].x, back[1].y);
        path.close();

        canvas.drawPath(path, paint);

        path.reset();


        paint.setColor(Color.BLACK);

        double roundTop = calculateDistance(topLeftFront_x, topLeftFront_y, topRightFront_x, topRightFront_y);

        PointF[] tops = rectVertex(topLeftFront_x, topLeftFront_y, topRightFront_x, topRightFront_y, (float) (roundTop * 0.20));

        path.moveTo(topLeftFront_x, topLeftFront_y);
        path.cubicTo(tops[0].x, tops[0].y, tops[2].x, tops[2].y, topRightFront_x, topRightFront_y);
        canvas.drawPath(path, paint);
        path.reset();

        float bucketWidth = (float) calculateDistance(bladeLeft.x, bladeLeft.y, bladeRight.x, bladeRight.y);

        float upperLeft_x = (float) (bladeLeft.x + (bucketWidth * 0.20f) * Math.cos(Math.toRadians(45 + heading)));
        float upperLeft_y = (float) (bladeLeft.y + (bucketWidth * 0.20f) * Math.sin(Math.toRadians(45 + heading)));

        float upperRight_x = (float) (bladeRight.x + (bucketWidth * 0.20f) * Math.cos(Math.toRadians(90 + 45 + heading)));
        float upperRight_y = (float) (bladeRight.y + (bucketWidth * 0.20f) * Math.sin(Math.toRadians(90 + 45 + heading)));

        float upper_mx = (upperLeft_x + upperRight_x) / 2f;
        float upper_my = (upperLeft_y + upperRight_y) / 2f;

        float distBlade = (float) calculateDistance(upper_mx, upper_my, top_x, top_y);

        float left_mx = (float) (top_left_x + distBlade * Math.cos(Math.toRadians(270 + heading)));
        float left_my = (float) (top_left_y + distBlade * Math.sin(Math.toRadians(270 + heading)));

        float right_mx = (float) (top_right_x + distBlade * Math.cos(Math.toRadians(270 + heading)));
        float right_my = (float) (top_right_y + distBlade * Math.sin(Math.toRadians(270 + heading)));

        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(10);

        canvas.drawLine(top_left_x, top_left_y, left_mx, left_my, paint);
        canvas.drawLine(top_right_x, top_right_y, right_mx, right_my, paint);
        canvas.drawLine(left_mx, left_my, right_mx, right_my, paint);

    }


    public void drawBlade(Canvas canvas, Paint paint, int color, double scala) {

        double heading = 0;

        float bucketWidth = (float) calculateDistance(bladeLeft.x, bladeLeft.y, bladeRight.x, bladeRight.y);

        float upperLeft_x = (float) (bladeLeft.x + (bucketWidth * 0.20f) * Math.cos(Math.toRadians(45 + heading)));
        float upperLeft_y = (float) (bladeLeft.y + (bucketWidth * 0.20f) * Math.sin(Math.toRadians(45 + heading)));

        float upperRight_x = (float) (bladeRight.x + (bucketWidth * 0.20f) * Math.cos(Math.toRadians(90 + 45 + heading)));
        float upperRight_y = (float) (bladeRight.y + (bucketWidth * 0.20f) * Math.sin(Math.toRadians(90 + 45 + heading)));


        Path path = new Path();

        paint.setColor(color);

        path.moveTo(upperLeft_x, upperLeft_y);
        path.lineTo(upperRight_x, upperRight_y);
        path.lineTo(bladeRight.x, bladeRight.y);
        path.lineTo(bladeLeft.x, bladeLeft.y);
        path.close();

        canvas.drawPath(path, paint);

        path.reset();

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        canvas.drawLine(upperLeft_x, upperLeft_y, upperRight_x, upperRight_y, paint);
        canvas.drawLine(upperRight_x, upperRight_y, bladeRight.x, bladeRight.y, paint);
        canvas.drawLine(bladeRight.x, bladeRight.y, bladeLeft.x, bladeLeft.y, paint);
        canvas.drawLine(bladeLeft.x, bladeLeft.y, upperLeft_x, upperLeft_y, paint);

    }

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

    public double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
