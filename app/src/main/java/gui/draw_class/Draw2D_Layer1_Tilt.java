package gui.draw_class;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import gui.digging_excavator.Digging2D;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.surfcreator.DistToLine;
import utils.DistToPoint;
import utils.MyMCUtils;

public class Draw2D_Layer1_Tilt extends View {
    private long DOUBLE_TAP_TIME_DELTA = 300; // Tempo massimo tra due tap per considerarli un double tap
    private long lastTapTime = 0;
    Paint paint;
    public float offsetX,offsetY;


    public Draw2D_Layer1_Tilt(Context context) {
        super(context);
        paint = new Paint();
        init();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);
        try {


            float scala;
            float scala2;
            scala = (float) DataSaved.scale_Factor;
            scala2 = (float) ((float) (DataSaved.L_Bucket * 0.5 * scala) * 0.2);
            canvas.translate(0, 0);

            //--------- GROUND -------
            paint.setColor(MyColorClass.colorY_2D);

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

            double pendenza = ExcavatorLib.actualY2D;

            float axGround = (float) (mxt + (distanceA * Math.cos(angleA + Math.toRadians(pendenza))));
            float ayGround = (float) (myt + (distanceA * Math.sin(angleA + Math.toRadians(pendenza))));

            float bxGround = (float) (mxt + (distanceB * Math.cos(angleB + Math.toRadians(pendenza))));
            float byGround = (float) (myt + (distanceB * Math.sin(angleB + Math.toRadians(pendenza))));

            float cx = (float) (mxt + (distanceC * Math.cos(angleC + Math.toRadians(pendenza))));
            float cy = (float) (myt + (distanceC * Math.sin(angleC + Math.toRadians(pendenza))));

            float dx = (float) (mxt + (distanceD * Math.cos(angleD + Math.toRadians(pendenza))));
            float dy = (float) (myt + (distanceD * Math.sin(angleD + Math.toRadians(pendenza))));

            float mGround = (ayGround - byGround) / (axGround - bxGround);
            float qGround = -axGround * mGround + ayGround;

            path.moveTo(axGround, ayGround);
            path.lineTo(bxGround, byGround);
            path.lineTo(dx, dy);
            path.lineTo(cx, cy);
            canvas.drawPath(path, paint);
            path.rewind();

            //--------- BENNA -------
            float distance = (float) DataSaved.L_Bucket * scala;
            PointF origin = new PointF(getWidth() / 2f, getHeight() / 2f);
            PointF piombo = new PointF(origin.x, origin.y + distance);
            float originAngle = getDegrees(origin.x, origin.y, piombo.x, piombo.y);
            PointF flat = new PointF();
            if(DataSaved.isWL==0) {
                flat.x = (float) (origin.x + distance * Math.cos(originAngle + Math.toRadians(DataSaved.flat * -1)));
                flat.y = (float) (origin.y + distance * Math.sin(originAngle + Math.toRadians(DataSaved.flat * -1)));
            }else {
                flat.x = (float) (origin.x + distance * Math.cos(originAngle + Math.toRadians(DataSaved.flat * 1)));
                flat.y = (float) (origin.y + distance * Math.sin(originAngle + Math.toRadians(DataSaved.flat * 1)));
            }
            float left = origin.x - (getDistance(origin.x, origin.y, piombo.x, flat.y) / 2f);
            float top = origin.y;
            float right = origin.x + (getDistance(origin.x, origin.y, piombo.x, flat.y) / 2f);
            float bottom = flat.y;
            float controlX = (origin.x + flat.x) / 2;
            float controlY = origin.y + ((flat.y - origin.y) * 0.8f);

            PointF curve = new PointF(controlX, controlY);

            PointF[] circus = new PointF[10];

            RectF oval = new RectF(left, top, right, bottom);
            float startAngle = 90;
            float sweepAngle=180;
            if(DataSaved.isWL==0){
                sweepAngle = 180;}
            else {
                sweepAngle = -180;
            }
            float angleStep = sweepAngle / circus.length;
            float angle = startAngle;
            float centerX = oval.centerX();
            float centerY = oval.centerY();
            float raggio = oval.width() / 2;

            for (int i = 0; i < circus.length; i++) {
                float x = centerX + raggio * (float) Math.cos(Math.toRadians(angle));
                float y = centerY + raggio * (float) Math.sin(Math.toRadians(angle));
                circus[i] = new PointF(x, y);
                angle += angleStep;
            }

            ArrayList<PointF> benna = new ArrayList<>();
            benna.add(new PointF(getWidth() / 2f, getHeight() / 2f));
            float d, a;

            d = getDistance(origin.x, origin.y, piombo.x, flat.y);
            a = getDegrees(origin.x, origin.y, piombo.x, flat.y);

            double bucketAngle = (ExcavatorLib.correctWTilt - (+180.0)) / (-180.0 - (+180.0)) * (359.9 - (-0)) + (-0) + (180 + DataSaved.offsetFlat);

            benna.add(new PointF((float) (benna.get(0).x + d * Math.cos(a + Math.toRadians(bucketAngle * -1))), (float) (benna.get(0).y + d * Math.sin(a + Math.toRadians(bucketAngle * -1)))));

            d = getDistance(origin.x, origin.y, flat.x, flat.y);
            a = getDegrees(origin.x, origin.y, flat.x, flat.y);
            benna.add(new PointF((float) (benna.get(0).x + d * Math.cos(a + Math.toRadians(bucketAngle * -1))), (float) (benna.get(0).y + d * Math.sin(a + Math.toRadians(bucketAngle * -1)))));

            d = getDistance(origin.x, origin.y, curve.x, curve.y);
            a = getDegrees(origin.x, origin.y, curve.x, curve.y);
            benna.add(new PointF((float) (benna.get(0).x + d * Math.cos(a + Math.toRadians(bucketAngle * -1))), (float) (benna.get(0).y + d * Math.sin(a + Math.toRadians(bucketAngle * -1)))));

            for (PointF pointF : circus) {
                d = getDistance(origin.x, origin.y, pointF.x, pointF.y);
                a = getDegrees(origin.x, origin.y, pointF.x, pointF.y);
                benna.add(new PointF((float) (benna.get(0).x + d * Math.cos(a + Math.toRadians(bucketAngle * -1))), (float) (benna.get(0).y + d * Math.sin(a + Math.toRadians(bucketAngle * -1)))));
            }


            DisplayCoordCalc displayCoordCalc = new DisplayCoordCalc(ExcavatorLib.actualY2D, getWidth() * 0.5f, getHeight() * 0.7f, getWidth(), getHeight());

            double mDeltaX = 0;
            double mDeltaZ = 0;

            if (Digging2D.flagLaser_D2D) {
                mDeltaZ = ((DataSaved.offsetLaserZH - ExcavatorLib.quotaCentro) + DataSaved.offsetH * -1) * -1;
                mDeltaX = 0;
            } else {
                try {
                    switch (DataSaved.bucketEdge) {
                        case -1:
                            mDeltaX = (new DistToPoint(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0, ExcavatorLib.coordPitch[0], ExcavatorLib.coordPitch[1], 0).getDist_to_point()) - DataSaved.offsetmDeltaX;
                            mDeltaZ = ExcavatorLib.quotaSx - DataSaved.offsetZH + DataSaved.offsetH;

                            break;
                        case 0:
                            mDeltaX = (new DistToPoint(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0, ExcavatorLib.coordPitch[0], ExcavatorLib.coordPitch[1], 0).getDist_to_point()) - DataSaved.offsetmDeltaX;
                            mDeltaZ = ExcavatorLib.quotaCentro - DataSaved.offsetZH + DataSaved.offsetH;

                            break;
                        case 1:
                            mDeltaX = (new DistToPoint(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0, ExcavatorLib.coordPitch[0], ExcavatorLib.coordPitch[1], 0).getDist_to_point()) - DataSaved.offsetmDeltaX;
                            mDeltaZ = ExcavatorLib.quotaDx - DataSaved.offsetZH + DataSaved.offsetH;

                            break;
                    }

                } catch (Exception e) {

                }
            }
            mDeltaZ = mDeltaZ - DataSaved.monumentRelease;

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

            float displayCenterX = (displayX1 + displayX2) / 2f;
            float displayCenterY = (displayY1 + displayY2) / 2f;


            //float offsY = (float) ((displayCenterY) - benna.get(2).y - (mDeltaZ * scala));


            float offsX = 0;
            //(float) ((displayCenterX) - benna.get(2).x - (mDeltaX * scala));


            for (int i = 0; i < benna.size(); i++) {
                benna.set(i, new PointF(benna.get(i).x + offsX, benna.get(i).y));
            }

            float offsY = (float) ((mGround * (benna.get(2).x) + qGround) - benna.get(2).y - (mDeltaZ * scala));
            for (int i = 0; i < benna.size(); i++) {
                benna.set(i, new PointF(benna.get(i).x, benna.get(i).y + offsY));
            }

            Point[] standard = new Point[]{
                    new Point(960, 396), // 0
                    new Point(958, 396),  // 1
                    new Point(950, 300), // 2
                    new Point(960, 300), // 3
                    new Point(970, 300), // 4
                    new Point(962, 396), // 5
            };

            float sizeStick = (float) (DataSaved.L_Stick * scala) * 0.008f;

            ArrayList<PointF> stick = new ArrayList<>();

            stick.add(new PointF(benna.get(0).x, benna.get(0).y));

            double stickAngle = (ExcavatorLib.correctStick - (+180.0)) / (-180.0 - (+180.0)) * (359.9 - (-0)) + (-0) + 90;

            float distanceIndex, angleIndex;
            for (int i = 1; i < standard.length; i++) {
                distanceIndex = getDistance(standard[0].x, standard[0].y, standard[i].x, standard[i].y) * sizeStick;
                angleIndex = getDegrees(standard[0].x, standard[0].y, standard[i].x, standard[i].y);
                stick.add(new PointF(
                        (float) (stick.get(0).x + distanceIndex * Math.cos(angleIndex + Math.toRadians(stickAngle * -1))),
                        (float) (stick.get(0).y + distanceIndex * Math.sin(angleIndex + Math.toRadians(stickAngle * -1)))
                ));
            }

            path.moveTo(stick.get(0).x, stick.get(0).y);
            for (int i = 1; i < stick.size(); i++) {
                path.lineTo(stick.get(i).x, stick.get(i).y);
            }
            paint.setColor(MyColorClass.colorStick);


            float radiusStick = (getDistance(standard[3].x, standard[3].y, standard[4].x, standard[4].y) * sizeStick);
            canvas.drawCircle(stick.get(3).x, stick.get(3).y, radiusStick, paint);
            canvas.drawPath(path, paint);
            paint.setColor(MyColorClass.colorStick);


            canvas.drawCircle(stick.get(3).x, stick.get(3).y, (float) (radiusStick * 0.50), paint);
            path.rewind();

            float radius = getDistance(benna.get(0).x, benna.get(0).y, benna.get(13).x, benna.get(13).y) * 0.80f;
            paint.setColor(MyColorClass.colorBucket);

            if(DataSaved.isWL==0) {
                path.moveTo(benna.get(0).x + (radius * 0.80f), benna.get(0).y); //wl inv
            }else {
                path.moveTo(benna.get(0).x + (radius * 0.80f), benna.get(0).y);
            }
            path.quadTo(benna.get(3).x, benna.get(3).y, benna.get(2).x, benna.get(2).y);
            path.lineTo(benna.get(1).x, benna.get(1).y);

            paint.setColor(MyColorClass.colorBucket);

            paint.setStrokeWidth(6f);

            for (int i = 4; i < benna.size(); i++) {
                path.lineTo(benna.get(i).x, benna.get(i).y);
            }
            path.lineTo(benna.get(0).x, benna.get(0).y);
            path.close();
            canvas.drawPath(path, paint);
            path.reset();


            PointF[] ellisse = new PointF[30];
            float k = (float) (scala2 * 0.08);

            RectF rectOval = new RectF(stick.get(0).x - 20f * k, stick.get(0).y - 10f * k, stick.get(0).x + 20f * k, stick.get(0).y + 10f * k);

            float aRect = rectOval.width() / 2;
            float bRect = rectOval.height() / 2;
            float rectCenterX = rectOval.centerX();
            float rectCenterY = rectOval.centerY();

            for (int i = 0; i < ellisse.length; i++) {
                float theta = (float) (i * 2 * Math.PI / ellisse.length);
                float x = rectCenterX + aRect * (float) Math.cos(theta);
                float y = rectCenterY + bRect * (float) Math.sin(theta);
                ellisse[i] = new PointF(x, y);
            }

            canvas.drawCircle(benna.get(0).x, (benna.get(0).y), radius, paint);

            canvas.rotate((float) (ExcavatorLib.correctBucket+90+DataSaved.flat),benna.get(0).x, (benna.get(0).y));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.DKGRAY);
            canvas.drawRoundRect((float) (benna.get(0).x-((DataSaved.L_Bucket/3)*scala)), (float) (benna.get(0).y-(DataSaved.L_Tilt/2*scala)), (float) (benna.get(0).x+(0.2*scala)), (float) (benna.get(0).y+DataSaved.L_Tilt/3*scala),5f,5f,paint);
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect((float) (benna.get(0).x-((DataSaved.L_Bucket/3)*scala)), (float) (benna.get(0).y-(DataSaved.L_Tilt/2*scala)), (float) (benna.get(0).x+(0.2*scala)), (float) (benna.get(0).y+DataSaved.L_Tilt/3*scala),5f,5f,paint);
            canvas.rotate((float) -(ExcavatorLib.correctBucket+90+DataSaved.flat),benna.get(0).x, (benna.get(0).y));
            //--------------------------------- DRAW TILT --------------------------------
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            //canvas.drawCircle(benna.get(0).x, (float) (benna.get(0).y), radius * 0.40f, paint);


            //-----------------------------------------------------------------------------
            if (DataSaved.projectionFlag == 0) {
                paint.setColor(MyColorClass.colorConstraint);
                paint.setStrokeWidth(3);
                canvas.drawLine(benna.get(2).x, benna.get(2).y, benna.get(2).x, benna.get(2).x * mGround + qGround, paint);
            } else if (DataSaved.projectionFlag == 1) {
                float mX, mY;
                float dist = (float) new DistToLine(benna.get(2).x, benna.get(2).y, axGround, ayGround, bxGround, byGround).getLinedistance();
                paint.setColor(MyColorClass.colorConstraint);
                paint.setStrokeWidth(3);
                mX = (float) (dist * Math.cos(Math.toRadians(90 + ExcavatorLib.actualY2D)));
                mY = (float) (dist * Math.sin(Math.toRadians(90 + ExcavatorLib.actualY2D)));
                canvas.drawLine(benna.get(2).x, benna.get(2).y, mX + benna.get(2).x, mY + benna.get(2).y, paint);

            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private float getDistance(float x1, float y1, float x2, float y2) {
        final double sqrt = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        return (float) sqrt;
    }

    private float getDegrees(float x1, float y1, float x2, float y2) {
        float dY = y2 - y1;
        float dX = x2 - x1;
        return (float) Math.atan2(dY, dX); // * 180 / Math.PI;
    }

    private void setPoints(float distance, float angle, double value, ArrayList<PointF> array) {
        array.add(
                new PointF(
                        (float) (array.get(0).x + distance * Math.cos(angle + Math.toRadians(value * -1))),
                        (float) (array.get(0).y + distance * Math.sin(angle + Math.toRadians(value * -1)))
                )
        );
    }


    private void init(){
        offsetX = 0;
        offsetY = 0;
        setOnTouchListener(new OnTouchListener() {
            float lastTouchX, lastTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float i= (float) MyMCUtils.myscaleD(getHeight(),500f,600f,0.8,1.6);
                float x = event.getX() *i;
                float y = event.getY() *i;
               /* switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        long tapTime = System.currentTimeMillis();
                        if (tapTime - lastTapTime <= DOUBLE_TAP_TIME_DELTA) {
                            if (Digging2D.vista != 0) {
                                Digging2D.vista = 0;

                            } else {
                                Digging2D.vista = -1;
                            }
                        } else {

                            lastTouchX = x;
                            lastTouchY = y;
                        }
                        lastTapTime = tapTime;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        offsetX += x - lastTouchX;
                        offsetY += y - lastTouchY;
                        lastTouchX = x;
                        lastTouchY = y;
                        invalidate();
                        break;
                    default:
                        return false;
                }*/
                return true;
            }
        });
    }

}