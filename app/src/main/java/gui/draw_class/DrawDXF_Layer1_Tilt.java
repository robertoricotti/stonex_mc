package gui.draw_class;


import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static packexcalib.exca.ExcavatorLib.swing_boom_angle;
import static packexcalib.exca.ExcavatorLib.yawSensor;
import static services.TriangleService.tutteLinee;
import static utils.MyTypes.EXCAVATOR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import dxf.Point2D;
import dxf.Point3D;
import dxf.Segment;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import services.TriangleService;

public class DrawDXF_Layer1_Tilt extends View {

    final float PIVOT_X = 0.50f;
    final float PIVOT_Y = 0.75f;

    Paint paint,dashedPaint;

    int scala;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    public static float offsetX;
    public static float offsetY;
    public DrawDXF_Layer1_Tilt(Context context) {
        super(context);
        paint = new Paint();
        dashedPaint=new Paint();

        if (DataSaved.scale_FactorVista1D == 0) {
            DataSaved.scale_FactorVista1D = 1f;
        }
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);

        try {

            Path path = new Path();
            scala = (int) (85 + DataSaved.scale_FactorVista1D);

            canvas.scale((float) DataSaved.scale_FactorVista1D, (float) DataSaved.scale_FactorVista1D, getWidth() * 0.5f, getHeight() * 0.65f);
            canvas.translate(offsetX, offsetY);
            //-------------------------------- INIT BUCKET --------------------------------
            double distance = (DataSaved.L_Bucket) * scala;
            double flatAngle = DataSaved.flat;

            PointF origin = new PointF(getWidth() / 2f, (float) getHeight() / 2f);
            PointF piombo = new PointF(origin.x, origin.y + (float) distance);
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

            double bucketAngle = (ExcavatorLib.correctBucket - (+180.0)) / (-180.0 - (+180.0)) * (359.9 - (-0)) + (-0) + (180 + DataSaved.offsetFlat);

            ArrayList<PointF> bucket = new ArrayList<>();

            bucket.add(new PointF(getWidth() / 2f, getHeight() / 2f));

            setPoints(getDistance(origin.x, origin.y, piombo.x, flat.y), getDegrees(origin.x, origin.y, piombo.x, flat.y), bucketAngle, bucket);
            setPoints(getDistance(origin.x, origin.y, flat.x, flat.y), getDegrees(origin.x, origin.y, flat.x, flat.y), bucketAngle, bucket);
            setPoints(getDistance(origin.x, origin.y, curve.x, curve.y), getDegrees(origin.x, origin.y, curve.x, curve.y), bucketAngle, bucket);

            for (PointF pointF : circus) {
                setPoints(getDistance(origin.x, origin.y, pointF.x, pointF.y), getDegrees(origin.x, origin.y, pointF.x, pointF.y), bucketAngle, bucket);
            }

            float offsX = getWidth() * PIVOT_X - bucket.get(2).x;

            for (int i = 0; i < bucket.size(); i++) {
                bucket.set(i, new PointF(bucket.get(i).x + offsX, bucket.get(i).y));
            }

            float offsY = getHeight() * PIVOT_Y - bucket.get(2).y;
            for (int i = 0; i < bucket.size(); i++) {
                bucket.set(i, new PointF(bucket.get(i).x, bucket.get(i).y + offsY));
            }
            //-----------------------------------------------------------------------------

            //-------------------------------- INIT STICK --------------------------------
            Point[] standard = new Point[]{
                    new Point(960, 396), // 0
                    new Point(958, 396), // 1
                    new Point(950, 300), // 2
                    new Point(960, 300), // 3
                    new Point(970, 300), // 4
                    new Point(962, 396), // 5
            };

            float sizeStick = (float) (DataSaved.L_Stick * scala) * 0.01f;
            ArrayList<PointF> stick = new ArrayList<>();

            stick.add(new PointF(bucket.get(0).x, (float) (bucket.get(0).y)));

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

            //-----------------------------------------------------------------------------



            //-------------------------------- DRAW STICK --------------------------------
            paint.setColor(MyColorClass.colorStick);

            if(!DataSaved.isTiltRotator) {
                path.moveTo(stick.get(0).x, stick.get(0).y);
                for (int i = 1; i < stick.size(); i++) {
                    path.lineTo(stick.get(i).x, stick.get(i).y);
                }

                float radiusStick = (getDistance(standard[3].x, standard[3].y, standard[4].x, standard[4].y) * sizeStick);
                canvas.drawCircle(stick.get(3).x, stick.get(3).y, radiusStick, paint);
                canvas.drawPath(path, paint);
                path.reset();
                canvas.drawCircle(stick.get(3).x, stick.get(3).y, (float) (radiusStick * 0.50), paint);
            }

            //-----------------------------------------------------------------------------

            //-------------------------------- DRAW BUCKET --------------------------------
            paint.setColor(MyColorClass.colorBucket);

            float radius = getDistance(bucket.get(0).x, bucket.get(0).y, bucket.get(13).x, bucket.get(13).y) * 0.80f;


            if(DataSaved.isWL==EXCAVATOR) {
                path.moveTo(bucket.get(0).x + (radius * 0.80f), bucket.get(0).y); //wl inv
                path.quadTo(bucket.get(3).x, bucket.get(3).y, bucket.get(2).x, bucket.get(2).y);
                path.lineTo(bucket.get(1).x, bucket.get(1).y);

                for (int i = 4; i < bucket.size(); i++) {
                    path.lineTo(bucket.get(i).x, bucket.get(i).y);
                }
                path.lineTo(bucket.get(0).x, bucket.get(0).y);
                path.close();
                canvas.drawPath(path, paint);
                path.reset();


                canvas.drawCircle(bucket.get(0).x, (bucket.get(0).y), radius, paint);
            }else {
                float offsetAttacco = (float) (DataSaved.L_Bucket * scala * 0.3); // 30% della lunghezza della benna
                // Centro di rotazione
                float cxA = bucket.get(0).x;
                float cyA = bucket.get(0).y;

                // Spostamento verticale originale
                float xOffset = 0;
                float yOffset = -offsetAttacco;

                // Rotazione rispetto all'angolo del bucket
                float cosTheta = (float) Math.cos(Math.toRadians(360 - bucketAngle));
                float sinTheta = (float) Math.sin(Math.toRadians(360 - bucketAngle));

                float xRot = cxA + xOffset * cosTheta - yOffset * sinTheta;
                float yRot = cyA + xOffset * sinTheta + yOffset * cosTheta;

                // Applica la nuova posizione alla benna
                bucket.set(0, new PointF(xRot, yRot));
                path.moveTo(bucket.get(0).x + (radius * 0.80f), bucket.get(0).y);

                float lunghezzaLinea = offsetAttacco*0.8f ;
                float xLinea = bucket.get(0).x - (float) (lunghezzaLinea * Math.cos(Math.toRadians(45+360-bucketAngle)));
                float yLinea = bucket.get(0).y - (float) (lunghezzaLinea * Math.sin(Math.toRadians(45+360-bucketAngle)));
                path.lineTo(xLinea, yLinea);

                float lunghezzaLinea2 = offsetAttacco*0.5f ;
                float xLinea2 = xLinea - (float) (lunghezzaLinea2 * Math.cos(Math.toRadians(0+360-bucketAngle)));
                float yLinea2 = yLinea - (float) (lunghezzaLinea2 * Math.sin(Math.toRadians(0+360-bucketAngle)));
                path.lineTo(xLinea2, yLinea2); // Linea inclinata di 30°
                path.lineTo(bucket.get(2).x, bucket.get(2).y);
                path.lineTo(bucket.get(1).x, bucket.get(1).y);
                paint.setColor(MyColorClass.colorBucket);

                path.close();
                canvas.drawPath(path, paint);
                path.reset();
            }


            canvas.rotate((float) (ExcavatorLib.correctBucket+90+DataSaved.flat),bucket.get(0).x, (bucket.get(0).y));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.DKGRAY);
            canvas.drawRoundRect((float) (bucket.get(0).x-((DataSaved.L_Bucket/3)*scala)), (float) (bucket.get(0).y-(DataSaved.L_Tilt/2*scala)), (float) (bucket.get(0).x+(0.2*scala)), (float) (bucket.get(0).y+DataSaved.L_Tilt/3*scala),5f,5f,paint);
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect((float) (bucket.get(0).x-((DataSaved.L_Bucket/3)*scala)), (float) (bucket.get(0).y-(DataSaved.L_Tilt/2*scala)), (float) (bucket.get(0).x+(0.2*scala)), (float) (bucket.get(0).y+DataSaved.L_Tilt/3*scala),5f,5f,paint);
            canvas.rotate((float) -(ExcavatorLib.correctBucket+90+DataSaved.flat),bucket.get(0).x, (bucket.get(0).y));
            //--------------------------------- DRAW TILT --------------------------------
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            //canvas.drawCircle(bucket.get(0).x, (float) (bucket.get(0).y), radius * 0.40f, paint);


            //-----------------------------------------------------------------------------





            //--------------------------------- DRAW LINE --------------------------------
            paint.setColor(MyColorClass.colorConstraint);
            paint.setStrokeWidth(3);
            switch (DataSaved.bucketEdge){
                case -1:
                    if(!TriangleService.ltOffGrid) {
                        canvas.drawLine(bucket.get(2).x, bucket.get(2).y+1f, bucket.get(2).x, (float) (bucket.get(2).y + (TriangleService.quota3D_SX * scala)+1f), paint);
                    }
                    break;

                    case 0:
                        if(!TriangleService.ctOffGrid) {
                            canvas.drawLine(bucket.get(2).x, bucket.get(2).y+1f, bucket.get(2).x, (float) (bucket.get(2).y + (TriangleService.quota3D_CT * scala)+1f), paint);
                        }
                break;

                case 1:
                    if(!TriangleService.rtOffGrid) {
                        canvas.drawLine(bucket.get(2).x, bucket.get(2).y+1f, bucket.get(2).x, (float) (bucket.get(2).y + (TriangleService.quota3D_DX * scala)+1f), paint);
                    }
                    break;
            }


            //-----------------------------------------------------------------------------





            //-----------------------------------------------------------------------------

            float fixedX = getWidth() * PIVOT_X;
            float fixedY = getHeight() * PIVOT_Y;
            List<Point2D> sortedPoints0 = new ArrayList<>(Arrays.asList(tutteLinee[0]));
            sortedPoints0.sort(Comparator.comparingDouble(Point2D::getX));
            List<Point2D> sortedPoints1 = new ArrayList<>(Arrays.asList(tutteLinee[1]));
            sortedPoints1.sort(Comparator.comparingDouble(Point2D::getX));
            paint.setStrokeWidth(4f);
            for (int i = 0; i < sortedPoints0.size(); i++) {

                // avanti
                try {
                    paint.setColor(MyColorClass.colorGroundY);
                    float x = fixedX - (float) sortedPoints0.get(i).getX() * scala;
                    float y = fixedY - (float) sortedPoints0.get(i).getY() * scala;

                    if (i < sortedPoints0.size() - 1) {
                        float xNext = fixedX - (float) sortedPoints0.get(i + 1).getX() * scala;
                        float yNext = fixedY - (float) sortedPoints0.get(i + 1).getY() * scala;
                        canvas.drawLine(x, y, xNext, yNext, paint);

                    }

                    if (DataSaved.offsetH != 0) {
                        paint.setColor(MyColorClass.colorOffsetLine);
                        if (i < sortedPoints0.size() - 1) {
                            float xNext = fixedX - (float) sortedPoints0.get(i + 1).getX() * scala;
                            float yNext = fixedY - (float) sortedPoints0.get(i + 1).getY() * scala;
                            canvas.drawLine(x, (float) (y + DataSaved.offsetH * scala), xNext, (float) (yNext + DataSaved.offsetH * scala), paint);

                        }
                    }
                } catch (Exception e) {
                }
            }

            for (int i = 0; i < sortedPoints1.size(); i++) {
                //dietro
                try {
                    paint.setColor(MyColorClass.colorGroundY);
                    float x = fixedX + (float) sortedPoints1.get(i).getX() * scala;
                    float y = fixedY - (float) sortedPoints1.get(i).getY() * scala;

                    if (i < sortedPoints1.size() - 1) {
                        float xNext = fixedX + (float) sortedPoints1.get(i + 1).getX() * scala;
                        float yNext = fixedY - (float) sortedPoints1.get(i + 1).getY() * scala;
                        canvas.drawLine(x, y, xNext, yNext, paint);
                    }

                    if (DataSaved.offsetH != 0) {
                        paint.setColor(MyColorClass.colorOffsetLine);
                        if (i < sortedPoints1.size() - 1) {
                            float xNext = fixedX + (float) sortedPoints1.get(i + 1).getX() * scala;
                            float yNext = fixedY - (float) sortedPoints1.get(i + 1).getY() * scala;
                            canvas.drawLine(x, (float) (y + DataSaved.offsetH * scala), xNext, (float) (yNext + DataSaved.offsetH * scala), paint);
                        }
                    }
                } catch (Exception e) {
                }
            }
            if (!sortedPoints0.isEmpty() && !sortedPoints1.isEmpty()) {
                Point2D lastPoint0 = sortedPoints0.get(0);
                Point2D firstPoint1 = sortedPoints1.get(0);
                float xLast0 = fixedX - (float) lastPoint0.getX() * scala;
                float yLast0 = fixedY - (float) lastPoint0.getY() * scala;
                float xFirst1 = fixedX + (float) firstPoint1.getX() * scala;
                float yFirst1 = fixedY - (float) firstPoint1.getY() * scala;
                paint.setColor(MyColorClass.colorGroundY);
                canvas.drawLine(xLast0, yLast0, xFirst1, yFirst1, paint);
                if (DataSaved.offsetH != 0) {
                    paint.setColor(MyColorClass.colorOffsetLine);
                    canvas.drawLine(xLast0, (float) (yLast0 + DataSaved.offsetH * scala), xFirst1, (float) (yFirst1 + DataSaved.offsetH * scala), paint);
                }
            }

            float mLevel = 10000f;
            paint.setStyle(Paint.Style.FILL); // Assicurati di usare lo stile FILL per riempire le aree
            if (!sortedPoints0.isEmpty() && !sortedPoints1.isEmpty()) {
                Point2D lastPoint0 = sortedPoints0.get(0);
                Point2D firstPoint1 = sortedPoints1.get(0);
                float xLast0 = fixedX - (float) lastPoint0.getX() * scala;
                float yLast0 = fixedY - (float) lastPoint0.getY() * scala;
                float xFirst1 = fixedX + (float) firstPoint1.getX() * scala;
                float yFirst1 = fixedY - (float) firstPoint1.getY() * scala;
                paint.setColor(MyColorClass.colorGroundY);
                canvas.drawLine(xLast0, yLast0, xFirst1, yFirst1, paint);
                if (DataSaved.offsetH != 0) {
                    paint.setColor(MyColorClass.colorOffsetLine);
                    canvas.drawLine(xLast0, (float) (yLast0 + DataSaved.offsetH * scala), xFirst1, (float) (yFirst1 + DataSaved.offsetH * scala), paint);
                }
                // Crea il path chiuso per questa linea
                Path patha = new Path();
                patha.moveTo(xLast0, yLast0);       // Punto iniziale
                patha.lineTo(xFirst1, yFirst1);       // Punto successivo
                patha.lineTo(xFirst1, mLevel); // Punto esteso verso il basso
                patha.lineTo(xLast0, mLevel); // Altro punto esteso verso il basso
                patha.close();

                // Disegna e riempi il path
                paint.setColor(Color.argb(128, 128, 128, 128)); // Colore grigio semitrasparente
                canvas.drawPath(patha, paint);
            }
            for (int i = 0; i < sortedPoints0.size() - 1; i++) {
                try {
                    // Punti della linea "avanti"
                    float x1 = fixedX - (float) sortedPoints0.get(i).getX() * scala;
                    float y1 = fixedY - (float) sortedPoints0.get(i).getY() * scala;
                    float x2 = fixedX - (float) sortedPoints0.get(i + 1).getX() * scala;
                    float y2 = fixedY - (float) sortedPoints0.get(i + 1).getY() * scala;

                    // Estendi i punti verso il basso (y = 1000)
                    float x1Bottom = x1;
                    float y1Bottom = mLevel;
                    float x2Bottom = x2;
                    float y2Bottom = mLevel;

                    // Crea il path chiuso per questa linea
                    Path patha = new Path();
                    patha.moveTo(x1, y1);       // Punto iniziale
                    patha.lineTo(x2, y2);       // Punto successivo
                    patha.lineTo(x2Bottom, y2Bottom); // Punto esteso verso il basso
                    patha.lineTo(x1Bottom, y1Bottom); // Altro punto esteso verso il basso
                    patha.close();

                    // Disegna e riempi il path
                    paint.setColor(Color.argb(128, 128, 128, 128)); // Colore grigio semitrasparente
                    canvas.drawPath(patha, paint);
                } catch (Exception e) {
                }
            }

            for (int i = 0; i < sortedPoints1.size() - 1; i++) {
                try {
                    // Punti della linea "dietro"
                    float x1 = fixedX + (float) sortedPoints1.get(i).getX() * scala;
                    float y1 = fixedY - (float) sortedPoints1.get(i).getY() * scala;
                    float x2 = fixedX + (float) sortedPoints1.get(i + 1).getX() * scala;
                    float y2 = fixedY - (float) sortedPoints1.get(i + 1).getY() * scala;

                    // Estendi i punti verso il basso (y = 1000)
                    float x1Bottom = x1;
                    float y1Bottom = mLevel;
                    float x2Bottom = x2;
                    float y2Bottom = mLevel;

                    // Crea il path chiuso per questa linea
                    Path pathm = new Path();
                    pathm.moveTo(x1, y1);       // Punto iniziale
                    pathm.lineTo(x2, y2);       // Punto successivo
                    pathm.lineTo(x2Bottom, y2Bottom); // Punto esteso verso il basso
                    pathm.lineTo(x1Bottom, y1Bottom); // Altro punto esteso verso il basso
                    pathm.close();

                    // Disegna e riempi il path
                    paint.setColor(Color.argb(128, 128, 128, 128)); // Colore grigio semitrasparente
                    canvas.drawPath(pathm, paint);
                } catch (Exception e) {
                }
            }
            if (DataSaved.isAutoSnap == 2) {

                float mfixedX = getWidth() * PIVOT_X;

                float x = worldToFrontX(
                        mfixedX,
                        scala,
                        DataSaved.cutWorldX_1, // E
                        DataSaved.cutWorldY_1  // N
                );

                dashedPaint.setAntiAlias(true);
                dashedPaint.setStyle(Paint.Style.STROKE);
                dashedPaint.setColor(MyColorClass.colorConstraint);
                dashedPaint.setStrokeWidth((float) (3f / DataSaved.scale_FactorVista1D));
                dashedPaint.setPathEffect(
                        new DashPathEffect(new float[]{20f, 15f}, 0)
                );

                canvas.drawLine(x, -10000f, x, 10000f, dashedPaint);
                dashedPaint.setPathEffect(null);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }
    private float worldToFrontX(
            float fixedX,
            int scala,
            double worldE,
            double worldN
    ) {
        // riferimento: centro benna in coordinate mondo
        double refE = ExcavatorLib.bucketCoord[0];
        double refN = ExcavatorLib.bucketCoord[1];
        switch (DataSaved.bucketEdge){
            case -1:
                refE = ExcavatorLib.bucketLeftCoord[0];
                 refN = ExcavatorLib.bucketLeftCoord[1];
                break;

            case 0:
                refE = ExcavatorLib.bucketCoord[0];
                refN = ExcavatorLib.bucketCoord[1];
                break;

            case 1:
                refE = ExcavatorLib.bucketRightCoord[0];
                refN = ExcavatorLib.bucketRightCoord[1];
                break;
        }

        double dE = worldE - refE;
        double dN = worldN - refN;

        double yawRad = Math.toRadians(hdt_BOOM + yawSensor);

        // componente laterale macchina (DX/SX)
        double lateral = dE * Math.sin(yawRad) + dN * Math.cos(yawRad);

        // NOTA: nella frontale il lato destro macchina è a destra schermo
        return fixedX - (float) (lateral * scala);
    }

    private void setPoints(float distance, float angle, double value, ArrayList<PointF> array) {
        array.add(
                new PointF(
                        (float) (array.get(0).x + distance * Math.cos(angle + Math.toRadians(value * -1))),
                        (float) (array.get(0).y + distance * Math.sin(angle + Math.toRadians(value * -1)))
                )
        );
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Gestisci lo zoom con il ScaleGestureDetector
        scaleGestureDetector.onTouchEvent(event);

        // Gestisci il pan con il GestureDetector
        gestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        int pointerIndex = event.findPointerIndex(activePointerId);

        if (pointerIndex == -1) {
            return true; // Nessun puntatore valido
        }

        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = x;
                lastTouchY = y;
                activePointerId = event.getPointerId(0);
                break;

            case MotionEvent.ACTION_MOVE:
                if (!scaleGestureDetector.isInProgress()) {
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    offsetX += dx;
                    offsetY += dy;

                    invalidate();

                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointerId = INVALID_POINTER_ID;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                int pointerId = event.getPointerId(event.getActionIndex());
                if (pointerId == activePointerId) {
                    int newPointerIndex = event.getPointerCount() == 2 ? 1 : 0;
                    activePointerId = event.getPointerId(newPointerIndex);
                    lastTouchX = event.getX(newPointerIndex);
                    lastTouchY = event.getY(newPointerIndex);
                }
                break;
        }

        return true;
    }

    // Implementazione della classe interna per il trascinamento
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Aggiorna gli offset in base ai gesti di trascinamento
            offsetX -= (float) (distanceX / DataSaved.scale_FactorVista1D);
            offsetY -= (float) (distanceY / DataSaved.scale_FactorVista1D);
            invalidate();
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Ripristina il pan al doppio tap e lo zoom
            offsetX=0;
            offsetY=0;
            invalidate();
            return true;
        }
    }
    // Gestione dello zoom
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            DataSaved.scale_FactorVista1D *= detector.getScaleFactor();
            // Limita il fattore di scala
            DataSaved.scale_FactorVista1D = Math.max(0.05f, Math.min(DataSaved.scale_FactorVista1D, 10.0f));
            invalidate();
            return true;
        }
    }
}
