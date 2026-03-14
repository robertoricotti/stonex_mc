package gui.draw_class;

import static packexcalib.exca.Sensors_Decoder.Deg_Boom_Roll;
import static services.TriangleService.tutteLinee;
import static utils.MyTypes.EXCAVATOR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import dxf.Point2D;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import services.TriangleService;
import utils.MyMCUtils;
import utils.Utils;

public class DrawDXF_Layer2_Tilt extends View {
    boolean hasTiltRoto = false;
    float mFixedXX;
    final float PIVOT_X = 0.50f;
    final float PIVOT_Y = 0.65f;
    double height;

    Paint paint, dashedPaint;

    int scala;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    public static float offsetX;
    public static float offsetY;

    public DrawDXF_Layer2_Tilt(Context context) {
        super(context);
        paint = new Paint();
        dashedPaint = new Paint();
        if (DataSaved.scale_FactorVista2D == 0) {
            DataSaved.scale_FactorVista2D = 1f;
        }
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

    }

    @SuppressLint({"DrawAllocation", "DefaultLocale"})
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);
        try {
            hasTiltRoto=DataSaved.isTiltRotator==1;

            Path path = new Path();
            scala = (int) (85 + DataSaved.scale_FactorVista2D);

            canvas.scale((float) DataSaved.scale_FactorVista2D, (float) DataSaved.scale_FactorVista2D, getWidth() * 0.5f, getHeight() * 0.65f);
            canvas.translate(offsetX, offsetY);
            //-------------------------------- INIT BUCKET --------------------------------
            double bucketWidth = DataSaved.W_Bucket * scala;
            double mDist = (DataSaved.L_Bucket - DataSaved.L_Tilt) * Math.sin(Math.toRadians(Math.abs(ExcavatorLib.correctBucket)));
            mDist = MyMCUtils.limitD(mDist, 0.3, Double.MAX_VALUE);
            double bucketHeight = mDist * scala;

            PointF left_top_bucket = new PointF();
            PointF right_bottom_bucket = new PointF();


            left_top_bucket.x = (float) ((getWidth() * PIVOT_X) - (bucketWidth / 2f));
            left_top_bucket.y = (float) ((getHeight() * PIVOT_Y) - bucketHeight);

            right_bottom_bucket.x = (float) ((getWidth() * PIVOT_X) + (bucketWidth / 2f));
            right_bottom_bucket.y = getHeight() * PIVOT_Y;

            PointF rotation_point_bucket = new PointF();

            rotation_point_bucket.x = getWidth() * PIVOT_X;
            rotation_point_bucket.y = right_bottom_bucket.y;

            double bucket_angle;
            if (DataSaved.isTiltRotator == 1) {
                double dz = ExcavatorLib.bucketRightCoord[2] - ExcavatorLib.bucketLeftCoord[2];
                double dxy = distXY(ExcavatorLib.bucketLeftCoord, ExcavatorLib.bucketRightCoord);
                bucket_angle = Math.toDegrees(Math.atan2(dz, dxy));
            } else {
                bucket_angle = ExcavatorLib.correctTilt * -1;
            }

            // applico le rotazioni della benna e inserisco i punti nell'arraylist
            ArrayList<PointF> bucket = new ArrayList<>();

            bucket.add(new PointF(rotation_point_bucket.x, rotation_point_bucket.y));

            setPoints(getDistance(rotation_point_bucket.x, rotation_point_bucket.y, left_top_bucket.x, left_top_bucket.y), getDegrees(rotation_point_bucket.x, rotation_point_bucket.y, left_top_bucket.x, left_top_bucket.y), bucket_angle, bucket);

            setPoints(getDistance(rotation_point_bucket.x, rotation_point_bucket.y, right_bottom_bucket.x, left_top_bucket.y), getDegrees(rotation_point_bucket.x, rotation_point_bucket.y, right_bottom_bucket.x, left_top_bucket.y), bucket_angle, bucket);

            setPoints(getDistance(rotation_point_bucket.x, rotation_point_bucket.y, right_bottom_bucket.x, right_bottom_bucket.y), getDegrees(rotation_point_bucket.x, rotation_point_bucket.y, right_bottom_bucket.x, right_bottom_bucket.y), bucket_angle, bucket);

            setPoints(getDistance(rotation_point_bucket.x, rotation_point_bucket.y, left_top_bucket.x, right_bottom_bucket.y), getDegrees(rotation_point_bucket.x, rotation_point_bucket.y, left_top_bucket.x, right_bottom_bucket.y), bucket_angle, bucket);
            //-----------------------------------------------------------------------------


            //--------------------------------- INIT STICK --------------------------------
            double stickLength = DataSaved.L_Stick * scala;

            float stickWidth = (float) (0.25 * scala);

            PointF rotation_point_stick = new PointF();

            rotation_point_stick.x = (bucket.get(1).x + bucket.get(2).x) / 2f;
            rotation_point_stick.y = (bucket.get(1).y + bucket.get(2).y) / 2f;

            PointF left_top_stick = new PointF();
            PointF right_bottom_stick = new PointF();

            left_top_stick.x = rotation_point_stick.x - stickWidth / 2f;
            left_top_stick.y = rotation_point_stick.y - (float) stickLength;

            right_bottom_stick.x = rotation_point_stick.x + stickWidth / 2f;
            right_bottom_stick.y = (bucket.get(1).y + bucket.get(2).y + bucket.get(3).y + bucket.get(4).y) / 4;

            double stick_angle = Deg_Boom_Roll * -1;

            // applico le rotazioni dello stick e inserisco i punti nell'arraylist
            ArrayList<PointF> stick = new ArrayList<>();

            stick.add(new PointF(rotation_point_stick.x, rotation_point_stick.y));

            setPoints(getDistance(rotation_point_stick.x, rotation_point_stick.y, left_top_stick.x, left_top_stick.y), getDegrees(rotation_point_stick.x, rotation_point_stick.y, left_top_stick.x, left_top_stick.y), stick_angle, stick);

            setPoints(getDistance(rotation_point_stick.x, rotation_point_stick.y, right_bottom_stick.x, left_top_stick.y), getDegrees(rotation_point_stick.x, rotation_point_stick.y, right_bottom_stick.x, left_top_stick.y), stick_angle, stick);

            setPoints(getDistance(rotation_point_stick.x, rotation_point_stick.y, right_bottom_stick.x, right_bottom_stick.y), getDegrees(rotation_point_stick.x, rotation_point_stick.y, right_bottom_stick.x, right_bottom_stick.y), stick_angle, stick);

            setPoints(getDistance(rotation_point_stick.x, rotation_point_stick.y, left_top_stick.x, right_bottom_stick.y), getDegrees(rotation_point_stick.x, rotation_point_stick.y, left_top_stick.x, right_bottom_stick.y), stick_angle, stick);

            //-----------------------------------------------------------------------------

            //--------------------------------- INIT TILT --------------------------------

            PointF left_top_tilt = new PointF();
            PointF right_bottom_tilt = new PointF();

            double ly = DataSaved.L_Tilt * scala;

            left_top_tilt.x = ((bucket.get(1).x + bucket.get(2).x) / 2f) - stickWidth * 1.10f;
            left_top_tilt.y = ((bucket.get(1).y + bucket.get(2).y) / 2f) - (float) ly;

            right_bottom_tilt.x = ((bucket.get(1).x + bucket.get(2).x) / 2f) + stickWidth * 1.10f;
            right_bottom_tilt.y = ((bucket.get(1).y + bucket.get(2).y) / 2f);


            //--------------------------------- DRAW STICK --------------------------------
            if (DataSaved.isWL == EXCAVATOR) {
                if (!hasTiltRoto) {
                    paint.setColor(MyColorClass.colorStick);
                    path.moveTo(stick.get(1).x, stick.get(1).y);
                    path.lineTo(stick.get(2).x, stick.get(2).y);
                    path.lineTo(stick.get(3).x, stick.get(3).y);
                    path.lineTo(stick.get(4).x, stick.get(4).y);
                    path.close();
                    canvas.drawPath(path, paint);
                    path.reset();
                    canvas.drawCircle((stick.get(1).x + stick.get(2).x) / 2f, (stick.get(1).y + stick.get(2).y) / 2f, getDistance(stick.get(1).x, stick.get(1).y, stick.get(2).x, stick.get(2).y) / 2f, paint);
                }
            }
            //-----------------------------------------------------------------------------


            //--------------------------------- DRAW BUCKET --------------------------------

            //interno benna
            if ((ExcavatorLib.bucketCoord[2]) < (ExcavatorLib.coordST[2])) {
                paint.setColor(MyColorClass.colorBucket);
                path.moveTo(bucket.get(1).x, bucket.get(1).y);
                path.lineTo(bucket.get(2).x, bucket.get(2).y);
                path.lineTo(bucket.get(3).x, bucket.get(3).y);
                path.lineTo(bucket.get(4).x, bucket.get(4).y);
                path.close();
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);
                path.reset();
                paint.setColor(getResources().getColor(R.color.transparentgray));
                path.moveTo(bucket.get(1).x, bucket.get(1).y);
                path.lineTo(bucket.get(2).x, bucket.get(2).y);
                path.lineTo(bucket.get(3).x, bucket.get(3).y);
                path.lineTo(bucket.get(4).x, bucket.get(4).y);
                path.close();
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);
                path.reset();
                paint.setColor(MyColorClass.colorBucket);
                path.moveTo(bucket.get(1).x, bucket.get(1).y);
                path.lineTo(bucket.get(2).x, bucket.get(2).y);
                path.lineTo(bucket.get(3).x, bucket.get(3).y);
                path.lineTo(bucket.get(4).x, bucket.get(4).y);
                path.close();
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(path, paint);
                path.reset();
                paint.setStyle(Paint.Style.FILL);
            } else {
                paint.setColor(MyColorClass.colorBucket);
                path.moveTo(bucket.get(1).x, bucket.get(1).y);
                path.lineTo(bucket.get(2).x, bucket.get(2).y);
                path.lineTo(bucket.get(3).x, bucket.get(3).y);
                path.lineTo(bucket.get(4).x, bucket.get(4).y);
                path.close();
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);
                path.reset();
            }

            //--------------------------------- DRAW TILT --------------------------------
            paint.setColor(MyColorClass.colorBucket);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.DKGRAY);
            canvas.drawRoundRect(left_top_tilt.x, left_top_tilt.y, right_bottom_tilt.x, right_bottom_tilt.y, 5f, 5f, paint);
            paint.setColor(Color.GRAY);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect(left_top_tilt.x, left_top_tilt.y, right_bottom_tilt.x, right_bottom_tilt.y, 5f, 5f, paint);
            paint.setStyle(Paint.Style.FILL);


            //--------------------------------- DRAW SPIGOLO RIFERIMENTO --------------------------------
            if (DataSaved.isLowerEdge) {
                paint.setColor(Color.RED);
            } else {
                paint.setColor(Color.BLUE);
            }

            int edge = DataSaved.bucketEdge;

            switch (edge) {
                case -1:
                    mFixedXX = bucket.get(4).x;
                    canvas.drawCircle(bucket.get(4).x, bucket.get(4).y, (float) (8f), paint);
                    break;
                case 0:
                    mFixedXX = (bucket.get(3).x + bucket.get(4).x) / 2f;
                    canvas.drawCircle((bucket.get(3).x + bucket.get(4).x) / 2f, (bucket.get(3).y + bucket.get(4).y) / 2f, (float) (8f), paint);
                    break;
                case 1:
                    mFixedXX = bucket.get(3).x;
                    canvas.drawCircle(bucket.get(3).x, bucket.get(3).y, (float) (8f), paint);
                    break;
            }
            //--------------------------------- DRAW LINE --------------------------------


            paint.setColor(MyColorClass.colorConstraint);
            paint.setStrokeWidth(3);

            switch (DataSaved.bucketEdge) {
                case -1:
                    float startX_ = bucket.get(4).x;
                    float startY_ = bucket.get(4).y;
                    float stopY_ = (float) (startY_ + (TriangleService.quota3D_SX * scala));
                    if (!TriangleService.ltOffGrid) {
                        canvas.drawLine(startX_, startY_ + 1f, startX_, stopY_ + 1f, paint);
                    }
                    break;
                case 0:
                    float startX = (bucket.get(3).x + bucket.get(4).x) / 2f;
                    float startY = (bucket.get(3).y + bucket.get(4).y) / 2f;
                    float stopY = startY + (float) TriangleService.quota3D_CT * scala;
                    if (!TriangleService.ctOffGrid) {
                        canvas.drawLine(startX, startY, startX, stopY, paint);
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

            //--------------------------------- DRAW TEXT ANGOLO --------------------------------
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            if (DataSaved.L_Bucket < 0.9) {
                paint.setTextSize(20);
            } else {
                paint.setTextSize(24);
            }

            canvas.drawText(Utils.readAngoloLITE(String.valueOf(-bucket_angle)) + Utils.getGradiSimbol(), (bucket.get(1).x + bucket.get(2).x + bucket.get(3).x + bucket.get(4).x) / 4f, ((bucket.get(3).y + bucket.get(4).y) / 2f) - 30f, paint);
            //-----------------------------------------------------------------------------


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


                float dist = 0;
                switch (DataSaved.bucketEdge) {
                    case -1:
                        dist = (float) (TriangleService.dist3D_SX * scala);
                        break;
                    case 0:
                        dist = (float) (TriangleService.dist3D_CT * scala);
                        break;
                    case 1:
                        dist = (float) (TriangleService.dist3D_DX * scala);
                        break;

                }

                float x = mFixedXX - (dist * TriangleService.segnoLinea);
                dashedPaint.setAntiAlias(true);
                dashedPaint.setStyle(Paint.Style.STROKE);
                dashedPaint.setColor(MyColorClass.colorConstraint);
                dashedPaint.setStrokeWidth((float) (3f / DataSaved.scale_FactorVista2D));
                dashedPaint.setPathEffect(new DashPathEffect(new float[]{20f, 15f}, 0));

                canvas.drawLine(x, -10000f, x, 10000f, dashedPaint);
                // opzionale: pulisci l’effetto per non sporcare altro
                dashedPaint.setPathEffect(null);
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
            DataSaved.scale_FactorVista2D *= detector.getScaleFactor();
            // Limita il fattore di scala
            DataSaved.scale_FactorVista2D = Math.max(0.05f, Math.min(DataSaved.scale_FactorVista2D, 10.0f));
            invalidate();
            return true;
        }
    }
    private double distXY(double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        return Math.sqrt(dx * dx + dy * dy);
    }
}
