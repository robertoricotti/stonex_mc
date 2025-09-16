package gui.draw_class;

import static gui.digging_excavator.Digging2D.flagLaser_D2D;
import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import com.example.stx_dig.R;

import java.util.ArrayList;

import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.surfcreator.DistToLine;
import utils.DistToPoint;
import utils.Utils;

public class Draw2D_Layer2 extends View {

    private final Paint paint;
    public float offsetY;
    public Draw2D_Layer2(Context context) {
        super(context);
        paint = new Paint();
        init();
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);
        canvas.translate(0, 0);
        paint.setColor(MyColorClass.colorX_2D);

        try {


            paint.setStrokeWidth(5);
            float halfWidth = getWidth() / 2f;
            float halfHeight = getHeight() / 2f;
            float scala;
            scala = (float) DataSaved.scale_Factor;
            Path path = new Path();

            float axt = -5000;
            float ayt = (float) (getHeight() - (getHeight() * 30 / 100));

            float bxt = getWidth() + 5000;
            float byt = (float) (getHeight() - (getHeight() * 30 / 100));

            float mxt = (axt + bxt) / 2;
            float myt = (ayt + byt) / 2;

            float cyt = getHeight() + 5000;

            float dyt = getHeight() + 5000;

            float distanceA = getDistance(mxt, myt, axt, ayt);
            float distanceB = getDistance(mxt, myt, bxt, byt);
            float distanceC = getDistance(mxt, myt, axt, cyt);
            float distanceD = getDistance(mxt, myt, bxt, dyt);

            float angleA = getDegrees(mxt, myt, axt, ayt);
            float angleB = getDegrees(mxt, myt, bxt, byt);
            float angleC = getDegrees(mxt, myt, axt, cyt);
            float angleD = getDegrees(mxt, myt, bxt, dyt);

            double pendenza = ExcavatorLib.actualX2D * -1;

            float axGround = (float) (mxt + (distanceA * Math.cos(angleA + Math.toRadians(pendenza * -1))));
            float ayGround = (float) (myt + (distanceA * Math.sin(angleA + Math.toRadians(pendenza * -1))));

            float bxGround = (float) (mxt + (distanceB * Math.cos(angleB + Math.toRadians(pendenza * -1))));
            float byGround = (float) (myt + (distanceB * Math.sin(angleB + Math.toRadians(pendenza * -1))));

            float cpx = (float) (mxt + (distanceC * Math.cos(angleC + Math.toRadians(pendenza * -1))));
            float cpy = (float) (myt + (distanceC * Math.sin(angleC + Math.toRadians(pendenza * -1))));

            float dpx = (float) (mxt + (distanceD * Math.cos(angleD + Math.toRadians(pendenza * -1))));
            float dpy = (float) (myt + (distanceD * Math.sin(angleD + Math.toRadians(pendenza * -1))));

            float mGround = (ayGround - byGround) / (axGround - bxGround);
            float qGround = -axGround * mGround + ayGround;

            //---------START-DRAWING-------
            path.moveTo(axGround, ayGround);
            path.lineTo(bxGround, byGround);
            path.lineTo(dpx, dpy);
            path.lineTo(cpx, cpy);
            canvas.drawPath(path, paint);
            path.reset();


            double bucketWidth = DataSaved.W_Bucket;

            double bucketHeight = DataSaved.L_Bucket;

            double deltaXleft = new DistToPoint(ExcavatorLib.bucketLeftCoord[0], ExcavatorLib.bucketLeftCoord[1], 0, ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0).getDist_to_point();
            double deltaXright = new DistToPoint(ExcavatorLib.bucketRightCoord[0], ExcavatorLib.bucketRightCoord[1], 0, ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0).getDist_to_point();

            if (Deg_Boom_Roll< -90 || Deg_Boom_Roll > 90) {
                deltaXright = deltaXright * -1;
                deltaXleft = deltaXleft * -1;
            }

            DisplayCoordCalc displayCoordCalc = new DisplayCoordCalc(ExcavatorLib.actualX2D, getWidth() * 0.5f, getHeight() * 0.7f, getWidth(), getHeight());

            float displayX1 = 0, displayX2 = 0, displayY1 = 0, displayY2 = 0;

            if (displayCoordCalc.isUp()) {
                displayX1 = displayCoordCalc.output[2];
                displayY1 = 0;
            }
            if (displayCoordCalc.isDown()) {
                displayX2 = displayCoordCalc.output[3];
                displayY2 = getHeight();
            }
            if (displayCoordCalc.isLeft()) {
                displayX1 = 0;
                displayY1 = displayCoordCalc.output[0];
            }
            if (displayCoordCalc.isRight()) {
                displayX2 = getWidth();
                displayY2 = displayCoordCalc.output[1];
            }
            if (displayCoordCalc.isRight() && displayCoordCalc.isDown()) {
                displayX2 = getWidth();
                displayY2 = displayCoordCalc.output[1];
                displayY1 = displayCoordCalc.output[0];
                displayX1 = 0;

            }

            float displayCenterX = (displayX1 + displayX2) / 2f;
            float displayCenterY = (displayY1 + displayY2) / 2f;


            double heightLeft = !flagLaser_D2D ? (ExcavatorLib.quotaSx - DataSaved.offsetZH + DataSaved.offsetH) : ((DataSaved.offsetLaserZH - ExcavatorLib.quotaSx) + DataSaved.offsetH * -1) * -1;
            double heightCenter = !flagLaser_D2D ? (ExcavatorLib.quotaCentro - DataSaved.offsetZH + DataSaved.offsetH) : ((DataSaved.offsetLaserZH - ExcavatorLib.quotaCentro) + DataSaved.offsetH * -1) * -1;
            double heightRight = !flagLaser_D2D ? (ExcavatorLib.quotaDx - DataSaved.offsetZH + DataSaved.offsetH) : ((DataSaved.offsetLaserZH - ExcavatorLib.quotaDx) + DataSaved.offsetH * -1) * -1;
            heightLeft = heightLeft - DataSaved.monumentRelease;
            heightCenter = heightCenter - DataSaved.monumentRelease;
            heightRight = heightRight - DataSaved.monumentRelease;
            float groundY_bucket_left = mGround * (float) (displayCenterX - deltaXleft * scala) + qGround;

            float groundY_bucket_right = mGround * (float) (displayCenterX + deltaXright * scala) + qGround;
            paint.setColor(MyColorClass.colorBucket);


            double offsetCentro = displayCenterY - displayCenterY - (heightCenter * scala);
            double offsetLeft = (groundY_bucket_left - displayCenterY - (heightLeft * scala));
            double offsetRight = (groundY_bucket_right - displayCenterY - (heightRight * scala));


            PointF pointA_bottom = new PointF((float) (displayCenterX - ((deltaXleft)) * scala), (float) (displayCenterY + offsetLeft));
            PointF pointB_bottom = new PointF(displayCenterX, (float) (displayCenterY + offsetCentro));
            PointF pointC_bottom = new PointF((float) (displayCenterX + ((deltaXright)) * scala), (float) (displayCenterY + offsetRight));


            PointF pointA_top;
            PointF pointB_top;
            PointF pointC_top;

            float distance = (float) (bucketHeight * scala); // Distanza tra le due rette

            // Calcolo del vettore direzione della linea 1
            float dx = pointB_bottom.x - pointA_bottom.x;
            float dy = pointB_bottom.y - pointA_bottom.y;

            // Normalizzazione del vettore direzione
            float magnitude = (float) Math.sqrt(dx * dx + dy * dy);
            dx /= magnitude;
            dy /= magnitude;

            // Calcolo dei punti di inizio e fine della linea 2
            pointA_top = new PointF(pointA_bottom.x + distance * dy, pointA_bottom.y - distance * dx);
            pointB_top = new PointF(pointB_bottom.x + distance * dy, pointB_bottom.y - distance * dx);


            // Calcolo del vettore direzione della linea 1
            dx = pointC_bottom.x - pointB_bottom.x;
            dy = pointC_bottom.y - pointB_bottom.y;

            // Normalizzazione del vettore direzione
            magnitude = (float) Math.sqrt(dx * dx + dy * dy);
            dx /= magnitude;
            dy /= magnitude;

            // Calcolo dei punti di inizio e fine della linea 2
            pointC_top = new PointF(pointC_bottom.x + distance * dy, pointC_bottom.y - distance * dx);


            path.moveTo(pointA_top.x, pointA_top.y);
            path.lineTo(pointB_top.x, pointB_top.y);
            path.lineTo(pointC_top.x, pointC_top.y);
            path.lineTo(pointC_bottom.x, pointC_bottom.y);
            path.lineTo(pointB_bottom.x, pointB_bottom.y);
            path.lineTo(pointA_bottom.x, pointA_bottom.y);
            path.close();

            canvas.drawPath(path, paint);
            path.reset();


            double stickAngle = Deg_Boom_Roll * -1;
            double stickLength = (float) (DataSaved.L_Stick * scala) * 0.8;

            float larg=0.15f;
            if(DataSaved.W_Bucket<=0.35){
                larg=0.1f;
            }
            float stickWidth = (float) (larg * scala)* 0.8f;

            float leftStick = pointB_top.x - stickWidth;
            float topStick = (float) (pointB_top.y - stickLength);
            float rightStick = pointB_top.x + stickWidth;
            float bottomStick = pointB_top.y;

            PointF midStick = new PointF(pointB_top.x, pointB_top.y - (getDistance(pointB_top.x, bottomStick, pointB_top.x, topStick) / 2f));

            ArrayList<PointF> pointsStick = new ArrayList<>();

            pointsStick.add(new PointF(midStick.x, midStick.y));

            setPoints(getDistance(midStick.x, midStick.y, leftStick, topStick), getDegrees(midStick.x, midStick.y, leftStick, topStick), stickAngle, pointsStick);

            setPoints(getDistance(midStick.x, midStick.y, leftStick + (getDistance(leftStick, topStick, rightStick, topStick) / 2f), topStick), getDegrees(midStick.x, midStick.y, leftStick + (getDistance(leftStick, topStick, rightStick, topStick) / 2f), topStick), stickAngle, pointsStick);

            setPoints(getDistance(midStick.x, midStick.y, rightStick, topStick), getDegrees(midStick.x, midStick.y, rightStick, topStick), stickAngle, pointsStick);

            setPoints(getDistance(midStick.x, midStick.y, rightStick, bottomStick), getDegrees(midStick.x, midStick.y, rightStick, bottomStick), stickAngle, pointsStick);

            setPoints(getDistance(midStick.x, midStick.y, rightStick - (getDistance(leftStick, bottomStick, rightStick, bottomStick) / 2f), bottomStick), getDegrees(midStick.x, midStick.y, rightStick - (getDistance(leftStick, bottomStick, rightStick, bottomStick) / 2f), bottomStick), stickAngle, pointsStick);

            setPoints(getDistance(midStick.x, midStick.y, leftStick, bottomStick), getDegrees(midStick.x, midStick.y, leftStick, bottomStick), stickAngle, pointsStick);


            float offsYStick = pointB_top.y - pointsStick.get(5).y;
            float offsXStick = pointB_top.x - pointsStick.get(5).x;
            for (int i = 0; i < pointsStick.size(); i++) {
                pointsStick.set(i, new PointF(pointsStick.get(i).x + offsXStick, pointsStick.get(i).y + offsYStick));
            }
            paint.setColor(Color.TRANSPARENT);


            canvas.drawCircle(pointsStick.get(2).x, pointsStick.get(2).y, stickWidth, paint);

            path.moveTo(pointsStick.get(1).x, pointsStick.get(1).y);
            for (int i = 1; i < pointsStick.size(); i++) {
                path.lineTo(pointsStick.get(i).x, pointsStick.get(i).y);
            }
            canvas.drawPath(path, paint);
            path.reset();


            float offsetText = (float) bucketHeight * 0.3f;
            ///
            //denti
            float dente = (float) (bucketWidth / 11f);

            float mGroundAC = (pointA_bottom.y - pointC_bottom.y) / (pointA_bottom.x - pointC_bottom.x);
            float qGroundAC = -pointA_bottom.x * mGroundAC + pointA_bottom.y;


            float y1 = (float) (mGroundAC * (pointA_bottom.x + dente * scala) + qGroundAC);
            float y2 = (float) (mGroundAC * (pointA_bottom.x + dente * 2 * scala) + qGroundAC);
            float y3 = (float) (mGroundAC * (pointA_bottom.x + dente * 3 * scala) + qGroundAC);
            float y4 = (float) (mGroundAC * (pointA_bottom.x + dente * 4 * scala) + qGroundAC);
            float y5 = (float) (mGroundAC * (pointA_bottom.x + dente * 5 * scala) + qGroundAC);
            float y6 = (float) (mGroundAC * (pointA_bottom.x + dente * 6 * scala) + qGroundAC);
            float y7 = (float) (mGroundAC * (pointA_bottom.x + dente * 7 * scala) + qGroundAC);
            float y8 = (float) (mGroundAC * (pointA_bottom.x + dente * 8 * scala) + qGroundAC);
            float y9 = (float) (mGroundAC * (pointA_bottom.x + dente * 9 * scala) + qGroundAC);
            float y10 = (float) (mGroundAC * (pointA_bottom.x + dente * 10 * scala) + qGroundAC);
            paint.setColor(MyColorClass.colorBucket);

            paint.setStrokeWidth(10);
            canvas.drawLine(pointA_bottom.x, pointA_bottom.y, (float) (pointA_bottom.x + dente * scala), y1, paint);

            canvas.drawLine((float) (pointA_bottom.x + dente * 2 * scala), y2, (float) (pointA_bottom.x + dente * 3 * scala), y3, paint);

            canvas.drawLine((float) (pointA_bottom.x + dente * 4 * scala), y4, (float) (pointA_bottom.x + dente * 5 * scala), y5, paint);

            canvas.drawLine((float) (pointA_bottom.x + dente * 6 * scala), y6, (float) (pointA_bottom.x + dente * 7 * scala), y7, paint);

            canvas.drawLine((float) (pointA_bottom.x + dente * 8 * scala), y8, (float) (pointA_bottom.x + dente * 9 * scala), y9, paint);

            canvas.drawLine((float) (pointA_bottom.x + dente * 10 * scala), y10, pointC_bottom.x, pointC_bottom.y, paint);


            ///fine denti


            float radius = (30f * getHeight()) / 756f;
            paint.setColor(MyColorClass.colorConstraint);
            paint.setStrokeWidth(3);

            float mX, mY, dist;
            switch (DataSaved.bucketEdge) {
                case -1:
                    if (DataSaved.projectionFlag == 0) {
                        canvas.drawLine(pointA_bottom.x, pointA_bottom.y, pointA_bottom.x, mGround * pointA_bottom.x + qGround, paint);
                    } else if (DataSaved.projectionFlag == 1) {
                        dist = (float) new DistToLine(pointA_bottom.x, pointA_bottom.y, axGround, ayGround, bxGround, byGround).getLinedistance();
                        mX = (float) (dist * Math.cos(Math.toRadians(90 + ExcavatorLib.actualX2D)));
                        mY = (float) (dist * Math.sin(Math.toRadians(90 + ExcavatorLib.actualX2D)));
                        canvas.drawLine(pointA_bottom.x, pointA_bottom.y, mX + pointA_bottom.x, mY + pointA_bottom.y, paint);
                    }
                    paint.setColor(getResources().getColor(R.color.bg_sfsblue));
                    canvas.drawCircle(pointA_bottom.x, pointA_bottom.y, radius * 0.5f, paint);
                    break;
                case 0:
                    if (DataSaved.projectionFlag == 0) {
                        canvas.drawLine(pointB_bottom.x, pointB_bottom.y, pointB_bottom.x, mGround * pointB_bottom.x + qGround, paint);
                    } else if (DataSaved.projectionFlag == 1) {
                        dist = (float) new DistToLine(pointB_bottom.x, pointB_bottom.y, axGround, ayGround, bxGround, byGround).getLinedistance();
                        mX = (float) (dist * Math.cos(Math.toRadians(90 + ExcavatorLib.actualX2D)));
                        mY = (float) (dist * Math.sin(Math.toRadians(90 + ExcavatorLib.actualX2D)));
                        canvas.drawLine(pointB_bottom.x, pointB_bottom.y, mX + pointB_bottom.x, mY + pointB_bottom.y, paint);
                    }
                    paint.setColor(getResources().getColor(R.color.bg_sfsblue));
                    canvas.drawCircle(pointB_bottom.x, pointB_bottom.y, radius * 0.5f, paint);

                    break;
                case 1:
                    if (DataSaved.projectionFlag == 0) {
                        canvas.drawLine(pointC_bottom.x, pointC_bottom.y, pointC_bottom.x, mGround * pointC_bottom.x + qGround, paint);
                    } else if (DataSaved.projectionFlag == 1) {
                        dist = (float) new DistToLine(pointC_bottom.x, pointC_bottom.y, axGround, ayGround, bxGround, byGround).getLinedistance();
                        mX = (float) (dist * Math.cos(Math.toRadians(90 + ExcavatorLib.actualX2D)));
                        mY = (float) (dist * Math.sin(Math.toRadians(90 + ExcavatorLib.actualX2D)));
                        canvas.drawLine(pointC_bottom.x, pointC_bottom.y, mX + pointC_bottom.x, mY + pointC_bottom.y, paint);


                    }
                    paint.setColor(getResources().getColor(R.color.bg_sfsblue));
                    canvas.drawCircle(pointC_bottom.x, pointC_bottom.y, radius * 0.5f, paint);
                    break;
            }


            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            if (DataSaved.L_Bucket < 0.9) {
                paint.setTextSize(20);
            } else {
                paint.setTextSize(24);
            }
            canvas.drawText(Utils.readAngoloLITE(String.valueOf(Deg_Boom_Roll)) + Utils.getGradiSimbol(), pointB_bottom.x, pointB_bottom.y - (20), paint);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

        private void setPoints ( float distance, float angle, double value, ArrayList<PointF > array)
        {
            array.add(
                    new PointF(
                            (float) (array.get(0).x + distance * Math.cos(angle + Math.toRadians(value * -1))),
                            (float) (array.get(0).y + distance * Math.sin(angle + Math.toRadians(value * -1)))
                    )
            );
        }

        private float getDistance ( float x1, float y1, float x2, float y2){
            final double sqrt = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
            return (float) sqrt;
        }

        private float getDegrees ( float x1, float y1, float x2, float y2){
            float dY = y2 - y1;
            float dX = x2 - x1;
            return (float) Math.atan2(dY, dX); // * 180 / Math.PI;
        }

        public static class Point3D {
            public float x;
            public float y;
            public float z;

            public Point3D(float x, float y, float z) {
                this.x = x;
                this.y = y;
                this.z = z;
            }

            public float getX() {
                return x;
            }

            public float getY() {
                return y;
            }

            public float getZ() {
                return z;
            }

            public void setX(float x) {
                this.x = x;
            }

            public void setY(float y) {
                this.y = y;
            }

            public void setZ(float z) {
                this.z = z;
            }
        }

        private void init () {

            offsetY = 0;

        }

    }