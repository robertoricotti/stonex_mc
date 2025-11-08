package gui.draw_class;

import static utils.MyTypes.EXCAVATOR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.example.stx_dig.R;

import java.util.ArrayList;

import gui.digging_excavator.Digging2D;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.surfcreator.DistToLine;
import utils.DistToPoint;

public class Draw2D_Layer1 extends View {
    Paint paint;
    public float offsetX,offsetY;
    public Draw2D_Layer1(Context context) {
        super(context);
        paint = new Paint();
        init();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);

        double scala;
        scala = DataSaved.scale_Factor;
        canvas.translate(0,0);
        //--------- GROUND -------

        paint.setColor(MyColorClass.colorY_2D);

        try {


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
            float distance = (float) (DataSaved.L_Bucket * scala);
            PointF origin = new PointF(getWidth() / 2f, getHeight() / 2f);
            PointF piombo = new PointF(origin.x, origin.y + distance);
            float originAngle = getDegrees(origin.x, origin.y, piombo.x, piombo.y);
            PointF flat = new PointF();
            if(DataSaved.isWL==EXCAVATOR) {
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
            if(DataSaved.isWL==EXCAVATOR){
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

            double bucketAngle = (ExcavatorLib.correctBucket - (+180.0)) / (-180.0 - (+180.0)) * (359.9 - (-0)) + (-0) + (180 + DataSaved.offsetFlat);

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

            float offsX = 0;

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
                path.moveTo(benna.get(0).x + (radius * 0.80f), benna.get(0).y);
                path.quadTo(benna.get(3).x, benna.get(3).y, benna.get(2).x, benna.get(2).y);
                path.lineTo(benna.get(1).x, benna.get(1).y);
                paint.setColor(MyColorClass.colorBucket);
                for (int i = 4; i < benna.size(); i++) {
                    path.lineTo(benna.get(i).x, benna.get(i).y);
                }
                path.lineTo(benna.get(0).x, benna.get(0).y);
                path.close();
                canvas.drawPath(path, paint);
                path.reset();
                canvas.drawCircle(benna.get(0).x, benna.get(0).y, radius, paint);
                paint.setColor(getResources().getColor(R.color.black));
                canvas.drawCircle(benna.get(0).x, benna.get(0).y, radius * 0.40f, paint);
            }else {
                float offsetAttacco = (float) (DataSaved.L_Bucket * scala * 0.2); // 30% della lunghezza della benna
                // Centro di rotazione
                float cxA = benna.get(0).x;
                float cyA = benna.get(0).y;

                // Spostamento verticale originale
                float xOffset = 0;
                float yOffset = -offsetAttacco;

                // Rotazione rispetto all'angolo del bucket
                float cosTheta = (float) Math.cos(Math.toRadians(25+360 - bucketAngle));
                float sinTheta = (float) Math.sin(Math.toRadians(25+360 - bucketAngle));

                float xRot = cxA + xOffset * cosTheta - yOffset * sinTheta;
                float yRot = cyA + xOffset * sinTheta + yOffset * cosTheta;

                // Applica la nuova posizione alla benna
                benna.set(0, new PointF(xRot, yRot));
                path.moveTo(benna.get(0).x + (radius * 0.80f), benna.get(0).y);

                float xLinea = benna.get(0).x - (float) (offsetAttacco * Math.cos(Math.toRadians(90+360-bucketAngle)));
                float yLinea = benna.get(0).y - (float) (offsetAttacco * Math.sin(Math.toRadians(90+360-bucketAngle)));
                path.lineTo(xLinea, yLinea);

                float lunghezzaLinea0 = offsetAttacco*0.4f ;
                float xLinea0 = xLinea - (float) (lunghezzaLinea0 * Math.cos(Math.toRadians(60+360-bucketAngle)));
                float yLinea0 = yLinea - (float) (lunghezzaLinea0 * Math.sin(Math.toRadians(60+360-bucketAngle)));
                path.lineTo(xLinea0, yLinea0);

                float lunghezzaLinea1 = offsetAttacco*0.4f ;
                float xLinea1 = xLinea0 - (float) (lunghezzaLinea1 * Math.cos(Math.toRadians(30+360-bucketAngle)));
                float yLinea1 = yLinea0 - (float) (lunghezzaLinea1 * Math.sin(Math.toRadians(30+360-bucketAngle)));
                path.lineTo(xLinea1, yLinea1);

                float xLinea2 = xLinea1 - (float) (offsetAttacco*2 * Math.cos(Math.toRadians(10+360-bucketAngle)));
                float yLinea2 = yLinea1 - (float) (offsetAttacco*2 * Math.sin(Math.toRadians(10+360-bucketAngle)));
                path.lineTo(xLinea2, yLinea2); // Linea dritta

                path.lineTo(benna.get(2).x, benna.get(2).y);
                path.lineTo(benna.get(1).x, benna.get(1).y);
                paint.setColor(MyColorClass.colorBucket);
                path.close();
                canvas.drawPath(path, paint);
                path.reset();
                paint.setStrokeWidth(2f);
                canvas.drawLine(benna.get(2).x, benna.get(2).y, benna.get(1).x, benna.get(1).y,paint);

            }





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

    private void init(){
        offsetX = 0;
        offsetY = 0;

    }


}