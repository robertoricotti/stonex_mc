package gui.grade_draw_class;

import static packexcalib.exca.ExcavatorLib.correctPitch;
import static services.TriangleService.tutteLinee;
import static utils.MyTypes.EXCAVATOR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import services.TriangleService;


public class Grade_DrawDXF_Layer1 extends View {

    final float PIVOT_X = 0.50f;
    final float PIVOT_Y = 0.75f;
    Paint paint;
    int scala;
    Path groundPath;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    public static float offsetX;
    public static float offsetY;

    public Grade_DrawDXF_Layer1(Context context) {
        super(context);
        paint = new Paint();
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
            groundPath = new Path();
            scala = (int) (85 + DataSaved.scale_FactorVista1D);
            canvas.scale((float) DataSaved.scale_FactorVista1D, (float) DataSaved.scale_FactorVista1D, getWidth() * 0.5f, getHeight() * 0.65f);
            canvas.translate(offsetX, offsetY);
            //-------------------------------- INIT BUCKET --------------------------------
            double distance = DataSaved.L_Bucket * scala;
            double flatAngle = DataSaved.flat;

            PointF origin = new PointF(getWidth() / 2f, (float) getHeight() / 2f);
            PointF piombo = new PointF(origin.x, origin.y + (float) distance);
            float originAngle = getDegrees(origin.x, origin.y, piombo.x, piombo.y);
            PointF flat = new PointF();
            if (DataSaved.isWL == EXCAVATOR) {
                flat.x = (float) (origin.x + distance * Math.cos(originAngle + Math.toRadians(DataSaved.flat * -1)));
                flat.y = (float) (origin.y + distance * Math.sin(originAngle + Math.toRadians(DataSaved.flat * -1)));
            } else {
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
            float sweepAngle = 180;
            if (DataSaved.isWL == 0) {
                sweepAngle = 180;
            } else {
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

            double bucketAngle = (correctPitch - (+180.0)) / (-180.0 - (+180.0)) * (359.9 - (-0)) + (-0) + (180 + DataSaved.offsetFlat);

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

//-------------------------------- DRAW LAMA --------------------------------
            paint.setColor(MyColorClass.colorBucket);

            float deg1 = (float) (correctPitch - 60);
            float deg5 = (float) (correctPitch - 25);
            float l1 = (float) ((float) (DataSaved.altezzaLama) * 0.2 * scala);
            float l5 = (float) ((float) (DataSaved.altezzaLama) * 0.4 * scala);
            float alt = (float) (DataSaved.altezzaLama * scala);

            PointF p0 = bucket.get(2);
            PointF p1 = calcolaDestinazione(p0, l1, deg1);

            PointF p5 = calcolaDestinazione(p0, l5, deg5);
            PointF p3 = calcolaDestinazione(p0, alt, (float) (correctPitch - 90));
            PointF p2 = calcolaDestinazione(p3, l1, (float) correctPitch + 60);
            PointF p4 = calcolaDestinazione(p3, l5, (float) correctPitch + 30);
            Path pathl = new Path();
            pathl.moveTo(p0.x, p0.y);
            pathl.lineTo(p1.x, p1.y);
            pathl.lineTo(p2.x, p2.y);
            pathl.lineTo(p3.x, p3.y);
            pathl.lineTo(p4.x, p4.y);
            pathl.lineTo(p5.x, p5.y);
            pathl.close();
            canvas.drawPath(pathl, paint);
            pathl.rewind();

            PointF ppB = calcolaDestinazione(p3, (float) (DataSaved.deltaY * scala), (float) correctPitch);
            PointF pcoverA = calcolaDestinazione(ppB, (float) (DataSaved.altezzaPali * 0.35 * scala), (float) (correctPitch - 90));
            PointF ppA = calcolaDestinazione(ppB, (float) (DataSaved.altezzaPali * scala), (float) (correctPitch - 90));
            float prev = paint.getStrokeWidth();
            paint.setStrokeWidth(8f);
            paint.setColor(MyColorClass.colorStick);
            canvas.drawLine(ppB.x, ppB.y, ppA.x, ppA.y, paint);
            paint.setStrokeWidth(12f);
            paint.setColor(MyColorClass.colorStick);
            canvas.drawLine(ppB.x, ppB.y, pcoverA.x, pcoverA.y, paint);

            // Salva lo stato del canvas
            canvas.save();

// Ruota il canvas attorno al centro dell’ottagono
            canvas.rotate((float) correctPitch, ppA.x, ppA.y);

            paint.setColor(Color.YELLOW);//ant2
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(6f);
            RectF ovalRect = new RectF(ppA.x - 15, ppA.y - 5, ppA.x + 15, ppA.y + 5);
            float centeX = ovalRect.centerX();
            float centeY = ovalRect.centerY();
            float width = ovalRect.width();
            float height = ovalRect.height();
            Path octagon = createStretchedOctagon(centeX, centeY, width, height);
            canvas.drawPath(octagon, paint);
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            canvas.drawPath(octagon, paint);
            paint.setStrokeWidth(prev);


            // Ripristina il canvas originale
            canvas.restore();


            //-----------------------------------------------------------------------------

            //--------------------------------- DRAW LINE --------------------------------
            paint.setColor(MyColorClass.colorConstraint);
            paint.setStrokeWidth(3);
            switch (DataSaved.bucketEdge) {
                case -1:
                    if (!TriangleService.ltOffGrid) {
                        canvas.drawLine(bucket.get(2).x, bucket.get(2).y + 1f, bucket.get(2).x, (float) (bucket.get(2).y + (TriangleService.quota3D_SX * scala) + 1f), paint);
                    }
                    break;

                case 0:
                    if (!TriangleService.ctOffGrid) {
                        canvas.drawLine(bucket.get(2).x, bucket.get(2).y + 1f, bucket.get(2).x, (float) (bucket.get(2).y + (TriangleService.quota3D_CT * scala) + 1f), paint);
                    }
                    break;

                case 1:
                    if (!TriangleService.rtOffGrid) {
                        canvas.drawLine(bucket.get(2).x, bucket.get(2).y + 1f, bucket.get(2).x, (float) (bucket.get(2).y + (TriangleService.quota3D_DX * scala) + 1f), paint);
                    }
                    break;
            }


            //--------------------------------- DRAW GROUND --------------------------------

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
                    //canvas.drawCircle(x, y, 2f, paint);

                    if (i < sortedPoints0.size() - 1) {
                        float xNext = fixedX - (float) sortedPoints0.get(i + 1).getX() * scala;
                        float yNext = fixedY - (float) sortedPoints0.get(i + 1).getY() * scala;
                        canvas.drawLine(x, y, xNext, yNext, paint);

                    }

                    if (DataSaved.offsetH != 0) {
                        paint.setColor(MyColorClass.colorOffsetLine);
                        //canvas.drawCircle(x, y + (float) DataSaved.offsetH * (float) scala, 2f, paint);
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
                    //canvas.drawCircle(x, y, 2f, paint);

                    if (i < sortedPoints1.size() - 1) {
                        float xNext = fixedX + (float) sortedPoints1.get(i + 1).getX() * scala;
                        float yNext = fixedY - (float) sortedPoints1.get(i + 1).getY() * scala;
                        canvas.drawLine(x, y, xNext, yNext, paint);
                    }

                    if (DataSaved.offsetH != 0) {
                        paint.setColor(MyColorClass.colorOffsetLine);
                        //canvas.drawCircle(x, y + (float) DataSaved.offsetH * (float) scala, 2f, paint);
                        if (i < sortedPoints1.size() - 1) {
                            float xNext = fixedX + (float) sortedPoints1.get(i + 1).getX() * scala;
                            float yNext = fixedY - (float) sortedPoints1.get(i + 1).getY() * scala;
                            canvas.drawLine(x, (float) (y + DataSaved.offsetH * scala), xNext, (float) (yNext + DataSaved.offsetH * scala), paint);
                        }
                    }
                } catch (Exception e) {
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
            offsetX -= (float) (distanceX / DataSaved.scale_FactorVista1D);
            offsetY -= (float) (distanceY / DataSaved.scale_FactorVista1D);
            invalidate();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Ripristina il pan al doppio tap e lo zoom
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
            DataSaved.scale_FactorVista1D *= detector.getScaleFactor();
            // Limita il fattore di scala
            DataSaved.scale_FactorVista1D = Math.max(0.05f, Math.min(DataSaved.scale_FactorVista1D, 10.0f));
            invalidate();
            return true;
        }
    }

    public PointF calcolaDestinazione(PointF start, float distanza, float angoloGradi) {
        double angoloRadianti = Math.toRadians(angoloGradi); // converte gradi in radianti

        float dx = (float) (distanza * Math.cos(angoloRadianti));
        float dy = (float) (distanza * Math.sin(angoloRadianti));

        // Se il sistema di coordinate è con (0,0) in alto a sinistra (Canvas Android)
        PointF destinazione = new PointF(start.x + dx, start.y + dy);

        return destinazione;
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