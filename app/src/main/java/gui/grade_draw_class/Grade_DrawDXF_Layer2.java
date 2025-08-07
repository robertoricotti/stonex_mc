package gui.grade_draw_class;

import static services.TriangleService.tutteLinee;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import dxf.Point2D;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import services.TriangleService;
import utils.Utils;

public class Grade_DrawDXF_Layer2 extends View {
    final float PIVOT_X = 0.50f;
    final float PIVOT_Y = 0.65f;
    Paint paint;
    int scala;
    PointF basePalo;
    PointF testaPalo;
    PointF basePalo2;
    PointF testaPalo2;
    PointF testaShock;
    PointF testaShock2;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    public static float offsetX;
    public static float offsetY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;

    public Grade_DrawDXF_Layer2(Context context) {
        super(context);
        paint = new Paint();
        basePalo = new PointF();
        testaPalo = new PointF();
        basePalo2 = new PointF();
        testaPalo2 = new PointF();
        testaShock = new PointF();
        testaShock2 = new PointF();

        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @SuppressLint({"DrawAllocation", "DefaultLocale"})
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);
        try {

            Path path = new Path();
            scala = (int) (85 + DataSaved.scale_FactorVista2D);
            canvas.scale((float) DataSaved.scale_FactorVista2D, (float) DataSaved.scale_FactorVista2D, getWidth() * 0.5f, getHeight() * 0.65f);
            canvas.translate(offsetX, offsetY);
            //-------------------------------- INIT BUCKET --------------------------------
            double bucketWidth = DataSaved.W_Blade_TOT * scala;
            double bucketHeight = DataSaved.altezzaLama * scala;


            PointF left_top_bucket = new PointF();
            PointF right_bottom_bucket = new PointF();


            left_top_bucket.x = (float) ((getWidth() * PIVOT_X) - (bucketWidth / 2f));
            left_top_bucket.y = (float) ((getHeight() * PIVOT_Y) - bucketHeight);

            right_bottom_bucket.x = (float) ((getWidth() * PIVOT_X) + (bucketWidth / 2f));
            right_bottom_bucket.y = getHeight() * PIVOT_Y;

            PointF rotation_point_bucket = new PointF();

            rotation_point_bucket.x = getWidth() * PIVOT_X;
            rotation_point_bucket.y = right_bottom_bucket.y;

            double bucket_angle = ExcavatorLib.correctRoll * -1;

            // applico le rotazioni della benna e inserisco i punti nell'arraylist
            ArrayList<PointF> bucket = new ArrayList<>();

            bucket.add(new PointF(rotation_point_bucket.x, rotation_point_bucket.y));

            setPoints(getDistance(rotation_point_bucket.x, rotation_point_bucket.y, left_top_bucket.x, left_top_bucket.y), getDegrees(rotation_point_bucket.x, rotation_point_bucket.y, left_top_bucket.x, left_top_bucket.y), bucket_angle, bucket);

            setPoints(getDistance(rotation_point_bucket.x, rotation_point_bucket.y, right_bottom_bucket.x, left_top_bucket.y), getDegrees(rotation_point_bucket.x, rotation_point_bucket.y, right_bottom_bucket.x, left_top_bucket.y), bucket_angle, bucket);

            setPoints(getDistance(rotation_point_bucket.x, rotation_point_bucket.y, right_bottom_bucket.x, right_bottom_bucket.y), getDegrees(rotation_point_bucket.x, rotation_point_bucket.y, right_bottom_bucket.x, right_bottom_bucket.y), bucket_angle, bucket);

            setPoints(getDistance(rotation_point_bucket.x, rotation_point_bucket.y, left_top_bucket.x, right_bottom_bucket.y), getDegrees(rotation_point_bucket.x, rotation_point_bucket.y, left_top_bucket.x, right_bottom_bucket.y), bucket_angle, bucket);
            //-----------------------------------------------------------------------------


            //-----------------------------------------------------------------------------


            //--------------------------------- DRAW BUCKET --------------------------------


            basePalo.x = (float) (((bucket.get(1).x + bucket.get(2).x) / 2) - (DataSaved.deltaX * scala * Math.cos(Math.toRadians(ExcavatorLib.correctRoll))));
            basePalo.y = (float) (((bucket.get(1).y + bucket.get(2).y) / 2) - (DataSaved.deltaX * scala * Math.sin(Math.toRadians(ExcavatorLib.correctRoll))));
            testaPalo.x = (float) (basePalo.x + (DataSaved.altezzaPali * scala * Math.sin(Math.toRadians(ExcavatorLib.correctRoll))));
            testaPalo.y = (float) (basePalo.y - (DataSaved.altezzaPali * scala * Math.cos(Math.toRadians(ExcavatorLib.correctRoll))));
            testaShock.x = (float) (basePalo.x + (DataSaved.altezzaPali * 0.35 * scala * Math.sin(Math.toRadians(ExcavatorLib.correctRoll))));
            testaShock.y = (float) (basePalo.y - (DataSaved.altezzaPali * 0.35 * scala * Math.cos(Math.toRadians(ExcavatorLib.correctRoll))));
            basePalo2.x = (float) (((bucket.get(2).x + bucket.get(1).x) / 2) + ((DataSaved.W_Blade_RIGHT - DataSaved.distBetween) * scala * Math.cos(Math.toRadians(ExcavatorLib.correctRoll))));
            basePalo2.y = (float) (((bucket.get(2).y + bucket.get(1).y) / 2) + ((DataSaved.W_Blade_RIGHT - DataSaved.distBetween) * scala * Math.sin(Math.toRadians(ExcavatorLib.correctRoll))));
            testaPalo2.x = (float) (basePalo2.x + (DataSaved.altezzaPali * scala * Math.sin(Math.toRadians(ExcavatorLib.correctRoll))));
            testaPalo2.y = (float) (basePalo2.y - (DataSaved.altezzaPali * scala * Math.cos(Math.toRadians(ExcavatorLib.correctRoll))));
            testaShock2.x = (float) (basePalo2.x + (DataSaved.altezzaPali * 0.35 * scala * Math.sin(Math.toRadians(ExcavatorLib.correctRoll))));
            testaShock2.y = (float) (basePalo2.y - (DataSaved.altezzaPali * 0.35 * scala * Math.cos(Math.toRadians(ExcavatorLib.correctRoll))));
            paint.setColor(MyColorClass.colorStick);
            paint.setStrokeWidth(10f);
            canvas.drawLine(basePalo.x, basePalo.y, testaPalo.x, testaPalo.y, paint);
            paint.setStrokeWidth(18f);
            canvas.drawLine(basePalo.x, basePalo.y, testaShock.x, testaShock.y, paint);
            paint.setColor(MyColorClass.colorStick);
            paint.setStrokeWidth(10f);
            canvas.drawLine(basePalo2.x, basePalo2.y, testaPalo2.x, testaPalo2.y, paint);
            paint.setStrokeWidth(18f);
            canvas.drawLine(basePalo2.x, basePalo2.y, testaShock2.x, testaShock2.y, paint);
            paint.setStrokeWidth(10f);
            paint.setColor(Color.YELLOW);//ant1
            canvas.save();
            canvas.rotate((float) ExcavatorLib.correctRoll, testaPalo.x, testaPalo.y);
            RectF ovalRect = new RectF(testaPalo.x - 15, testaPalo.y - 5, testaPalo.x + 15, testaPalo.y + 5);
            float centerX = ovalRect.centerX();
            float centerY = ovalRect.centerY();
            float width = ovalRect.width();
            float height = ovalRect.height();
            Path octagon = createStretchedOctagon(centerX, centerY, width, height);
            canvas.drawPath(octagon, paint);
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            canvas.drawPath(octagon, paint);
            canvas.restore();
            canvas.save();
            paint.setColor(Color.YELLOW);//ant2
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(10f);
            canvas.rotate((float) ExcavatorLib.correctRoll, testaPalo2.x, testaPalo2.y);
            RectF ovalRect2 = new RectF(testaPalo2.x - 15, testaPalo2.y - 5, testaPalo2.x + 15, testaPalo2.y + 5);
            float centerX2 = ovalRect2.centerX();
            float centerY2 = ovalRect2.centerY();
            float width2 = ovalRect2.width();
            float height2 = ovalRect2.height();
            Path octagon2 = createStretchedOctagon(centerX2, centerY2, width2, height2);
            canvas.drawPath(octagon2, paint);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            canvas.drawPath(octagon2, paint);
            canvas.restore();

            paint.setColor(MyColorClass.colorBucket);

            path.moveTo(bucket.get(1).x, bucket.get(1).y);
            path.lineTo(bucket.get(2).x, bucket.get(2).y);
            path.lineTo(bucket.get(3).x, bucket.get(3).y);
            path.lineTo(bucket.get(4).x, bucket.get(4).y);
            path.close();
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, paint);
            path.reset();
            //disegnare pali qui
            //-----------------------------------------------------------------------------

//--------------------------------- DRAW LINE --------------------------------
            paint.setColor(MyColorClass.colorConstraint);
            paint.setStrokeWidth(3);

            switch (DataSaved.bucketEdge) {
                case -1:
                    float startX_ = bucket.get(4).x;
                    float startY_ = bucket.get(4).y;
                    float stopY_ = bucket.get(4).y + (float) TriangleService.quota3D_SX * scala;
                    if (!TriangleService.ltOffGrid) {
                        canvas.drawLine(startX_, startY_, startX_, stopY_, paint);
                    }
                    break;
                case 0:
                    float startX = (bucket.get(3).x + bucket.get(4).x) / 2f;
                    float startY = (bucket.get(3).y + bucket.get(4).y) / 2f;
                    float stopY = startY + (float) TriangleService.quota3D_CT * scala;
                    if (!TriangleService.ctOffGrid) {
                        canvas.drawLine(startX, startY + 1f, startX, stopY + 1f, paint);
                    }

                    break;
                case 1:
                    float startX__ = bucket.get(3).x;
                    float startY__ = bucket.get(3).y;
                    float stopY__ = startY__ + (float) TriangleService.quota3D_DX * scala;
                    if (!TriangleService.rtOffGrid) {
                        canvas.drawLine(startX__, startY__ + 1f, startX__, stopY__ + 1f, paint);
                    }

                    break;
            }
            //-----------------------------------------------------------------------------


            //--------------------------------- DRAW SPIGOLO RIFERIMENTO --------------------------------
            if (DataSaved.isLowerEdge) {
                paint.setColor(Color.RED);
            } else {
                paint.setColor(Color.BLUE);
            }


            int edge = DataSaved.bucketEdge;

            switch (edge) {
                case -1:
                    canvas.drawCircle(bucket.get(4).x, bucket.get(4).y, (float) (8f), paint);
                    break;
                case 0:
                    canvas.drawCircle((bucket.get(3).x + bucket.get(4).x) / 2f, (bucket.get(3).y + bucket.get(4).y) / 2f, (float) (8f), paint);
                    break;
                case 1:
                    canvas.drawCircle(bucket.get(3).x, bucket.get(3).y, (float) (8f), paint);
                    break;
            }
            //-----------------------------------------------------------------------------


            //--------------------------------- DRAW TEXT ANGOLO --------------------------------
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            if (DataSaved.L_Bucket < 0.9) {
                paint.setTextSize(20);
            } else {
                paint.setTextSize(24);
            }

            canvas.drawText(Utils.readAngoloLITE(String.valueOf(ExcavatorLib.correctRoll)) + Utils.getGradiSimbol(), (bucket.get(1).x + bucket.get(2).x + bucket.get(3).x + bucket.get(4).x) / 4f, ((bucket.get(3).y + bucket.get(4).y) / 2f) - 25f, paint);
            //-----------------------------------------------------------------------------

            //capire come fare con il terreno, quali punti visualizzare e come farli scorrere ???


            //--------------------------------- DRAW GROUND --------------------------------

            float fixedX = getWidth() * PIVOT_X;
            float fixedY = getHeight() * PIVOT_Y;
            List<Point2D> sortedPoints0 = new ArrayList<>(Arrays.asList(tutteLinee[2]));
            sortedPoints0.sort(Comparator.comparingDouble(Point2D::getX));
            List<Point2D> sortedPoints1 = new ArrayList<>(Arrays.asList(tutteLinee[3]));
            sortedPoints1.sort(Comparator.comparingDouble(Point2D::getX));
            paint.setStrokeWidth(4f);
            for (int i = 0; i < sortedPoints0.size(); i++) {

                // sinistra

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
                //destra
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
        } catch (Exception e) {
            System.out.println(e.toString());
        }
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
            offsetX -= (float) (distanceX / DataSaved.scale_FactorVista2D);
            offsetY -= (float) (distanceY / DataSaved.scale_FactorVista2D);
            invalidate();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Ripristina il pan al doppio tap
            offsetX = 0;
            offsetY = 0;
            invalidate();
            return true;
        }
    }

    // Gestione dello zoom
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            DataSaved.scale_FactorVista2D *= detector.getScaleFactor();

            // Limita il fattore di scala
            DataSaved.scale_FactorVista2D = Math.max(0.05f, Math.min(DataSaved.scale_FactorVista2D, 10.0f));

            invalidate();
            return true;
        }
    }

    private Path createStretchedOctagon(float centerX, float centerY, float width, float height) {
        float w = width / 2f;
        float h = height / 2f;

        float cornerW = w * 0.3f; // quanto "tagliare" gli angoli orizzontali
        float cornerH = h * 0.6f; // quanto "tagliare" gli angoli verticali

        Path path = new Path();
        path.moveTo(centerX - w + cornerW, centerY - h); // Top-left
        path.lineTo(centerX + w - cornerW, centerY - h); // Top-right
        path.lineTo(centerX + w, centerY - h + cornerH); // Right-top
        path.lineTo(centerX + w, centerY + h - cornerH); // Right-bottom
        path.lineTo(centerX + w - cornerW, centerY + h); // Bottom-right
        path.lineTo(centerX - w + cornerW, centerY + h); // Bottom-left
        path.lineTo(centerX - w, centerY + h - cornerH); // Left-bottom
        path.lineTo(centerX - w, centerY - h + cornerH); // Left-top
        path.close();

        return path;
    }

}
