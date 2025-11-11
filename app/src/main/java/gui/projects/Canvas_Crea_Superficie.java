package gui.projects;

import static gui.projects.Activity_Crea_Superficie.facceTrench;
import static gui.projects.Activity_Crea_Superficie.point3DS;
import static gui.projects.Activity_Crea_Superficie.polyTrench;
import static gui.projects.Activity_Crea_Superficie.puntiAB;
import static gui.projects.Dialog_Trench.leftS_d;
import static gui.projects.Dialog_Trench.leftW_d;
import static gui.projects.Dialog_Trench.rightS_d;
import static gui.projects.Dialog_Trench.rightW_d;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.stx_dig.R;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.util.ArrayList;
import java.util.List;

import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Vector2D;
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.gnss.NmeaListener;
import utils.DistToPoint;
import utils.Utils;

public class Canvas_Crea_Superficie extends View {
    static String text;
    static double[] puntoselezionato;
    static List<double[]> rotatedPoints;
    Coordinate[] coordinates;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    public float scala = 35;
    public static float offsetX;
    public static float offsetY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    Path path;
    Canvas canvas;
    Paint paint;
    PointF originPointCarro;
    PointF left_top_carro;
    PointF right_bottom_carro;
    PointF originPointStick;
    PointF left_top_stick;
    PointF right_bottom_stick;
    PointF originPointBucket;
    PointF left_top_bucket, left_top_bucket2;
    PointF right_bottom_bucket;
    PointF[] ellipse;
    double w_bucket;
    double d_stick;
    double l_bucket;
    double distanzaPosteriore;
    double lcarro;
    double cingoliLength;
    double stickWidth;
    double distStick;
    double dist;
    double bucketHeight;
    double bucketWidth;
    double buckEst, buckNord, bucketHeightFake;
    float stopX, stopY, stopXLeft, stopXRight, bucketX, bucketY;
    double distanceCingoli;
    float cingoliWidth;
    float cingoliHeight;
    public static int mode;
    float ancorPX, ancorPY;
    double rotationAngle;
    float mRoll;

    public Canvas_Crea_Superficie(Context context) {
        super(context);
        text = "";
        if (DataSaved.scale_Factor3D == 0) {
            DataSaved.scale_Factor3D = 1f;
        }
        paint = new Paint();
        path = new Path();
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        if (offsetY == 0) {
            offsetY = 150;
        }
        if (DataSaved.isWL == EXCAVATOR) {
            mRoll = (float) (DataSaved.L_Roll * scala);
        } else {
            mRoll = 0;
        }
        puntoselezionato = new double[]{0, 0, 0};
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        paint.setAntiAlias(true);
        try {
            rotationAngle = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
            switch (DataSaved.isWL) {
                case EXCAVATOR:
                    //exca
                    l_bucket = DataSaved.L_Bucket;
                    w_bucket = DataSaved.W_Bucket;
                    d_stick = DataSaved.L_Stick + l_bucket;
                    originPointCarro = new PointF(getWidth() * 0.5f, getHeight() * 0.85f);
                    canvas.save();
                    canvas.scale((float) DataSaved.scale_Factor3D, (float) DataSaved.scale_Factor3D, getWidth() * 0.5f, getHeight() * 0.85f);
                    canvas.translate(offsetX, offsetY);
                    initEscavatore();//inizializza  escavatore
                    dist = new DistToPoint(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0, ExcavatorLib.coordST[0], ExcavatorLib.coordST[1], 0).getDist_to_point();
                    bucketHeight = dist * scala;

                    bucketHeightFake = 0.5 * scala;

                    if (ExcavatorLib.correctBucket < -90 || ExcavatorLib.correctBucket > 90) {
                        bucketHeight = bucketHeight * -1;
                    }


                    bucketWidth = DataSaved.W_Bucket * scala;
                    distStick = (new DistToPoint(ExcavatorLib.coordMiniPitch[0], ExcavatorLib.coordMiniPitch [1], 0, ExcavatorLib.coordPivoTilt[0], ExcavatorLib.coordPivoTilt[1], 0).getDist_to_point() * scala) + 40f;
                    originPointBucket = new PointF(originPointStick.x, originPointStick.y - (float) distStick);
                    stickWidth = scala * 0.30f;
                    left_top_bucket = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
                    left_top_bucket2 = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeightFake);
                    right_bottom_bucket = new PointF(originPointBucket.x + (float) bucketWidth / 2f, originPointBucket.y);

                    buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
                    buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto

                    bucketX = (float) ((((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));
                    bucketY = left_top_bucket.y;

                    drawBenna(canvas);

                    stopX = (float) ((left_top_bucket.x + (mRoll) + right_bottom_bucket.x + (mRoll)) / 2f);
                    stopY = left_top_bucket.y;
                    stopXLeft = (float) (left_top_bucket.x + (mRoll));
                    stopXRight = (float) (right_bottom_bucket.x + (mRoll));
                    if (DataSaved.isWL ==EXCAVATOR) {
                        drawStick(canvas);

                    }

                    paint.setColor(getResources().getColor(R.color.red));
                    paint.setStrokeWidth(1.5f);
                    switch (DataSaved.bucketEdge) {
                        case -1:
                            canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                            canvas.drawCircle(stopXLeft, stopY, (float) (5.5 / DataSaved.scale_Factor3D), paint);
                            canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);

                            break;
                        case 0:
                            canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                            canvas.drawCircle(stopX, stopY, (float) (5.5 / DataSaved.scale_Factor3D), paint);

                            break;
                        case 1:
                            canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                            canvas.drawCircle(stopXRight, stopY, (float) (5.5 / DataSaved.scale_Factor3D), paint);
                            canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);

                            break;
                    }
                    switch (mode) {
                        case 0:
                            disegna1PE(canvas);
                            break;
                        case 1:
                            disegnaAB(canvas);
                            break;
                        case 2:
                            update(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                            switch (DataSaved.bucketEdge) {
                                case -1:
                                    drawArea(canvas, paint, Color.parseColor("#50FFFF00"));
                                    break;

                                case 0:
                                    drawArea(canvas, paint, Color.parseColor("#50FFFF00"));
                                    break;

                                case 1:
                                    drawArea(canvas, paint, Color.parseColor("#50FFFF00"));
                                    break;
                            }
                            break;

                        case 3:
                            updatePoly(NmeaListener.mch_Orientation + DataSaved.deltaGPS2, Activity_Crea_Superficie.indexSel);

                            switch (DataSaved.bucketEdge) {
                                case -1:
                                    drawPolyTrench(canvas, paint,NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                                    break;

                                case 0:
                                    drawPolyTrench(canvas, paint,NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                                    break;

                                case 1:
                                    drawPolyTrench(canvas, paint,NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                                    break;
                            }
                            break;
                        case 4:
                            updateTri(NmeaListener.mch_Orientation + DataSaved.deltaGPS2, Activity_Crea_Superficie.indexSel);
                            switch (DataSaved.bucketEdge) {
                                case -1:
                                    drawTriangles(canvas, paint);
                                    break;

                                case 0:
                                    drawTriangles(canvas, paint);
                                    break;

                                case 1:
                                    drawTriangles(canvas, paint);
                                    break;
                            }
                            break;
                    }
                    canvas.restore();
                    break;
                case WHEELLOADER:
                case DOZER:
                case DOZER_SIX:
                    l_bucket = DataSaved.W_Blade_TOT;
                    w_bucket = DataSaved.W_Blade_TOT;
                    d_stick = 0;
                    originPointCarro = new PointF(getWidth() * 0.5f, getHeight() * 0.5f);
                    canvas.save();
                    canvas.scale((float) DataSaved.scale_Factor3D, (float) DataSaved.scale_Factor3D, getWidth() * 0.5f, getHeight() * 0.65f);
                    canvas.translate(offsetX, offsetY);
                    initDozer();//inizializza  dozer
                    dist = 0.5;

                    bucketHeight = dist * scala;
                    bucketWidth = DataSaved.W_Blade_TOT * scala;


                    originPointBucket = new PointF(originPointStick.x, originPointStick.y);
                    stickWidth = scala * 0.40f;
                    left_top_bucket = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
                    left_top_bucket2 = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
                    right_bottom_bucket = new PointF(originPointBucket.x + (float) bucketWidth / 2f, originPointBucket.y);

                    buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
                    buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
                    stopX = (float) ((left_top_bucket.x + (mRoll) + right_bottom_bucket.x + (mRoll)) / 2f);
                    stopY = left_top_bucket.y;
                    stopXLeft = (float) (left_top_bucket.x + (mRoll));
                    stopXRight = (float) (right_bottom_bucket.x + (mRoll));
                    bucketX = (float) ((((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));
                    bucketY = left_top_bucket.y;

                    paint.setColor(getResources().getColor(R.color.red));
                    paint.setStrokeWidth(1.5f);
                    paint.setStyle(Paint.Style.FILL);
                    switch (DataSaved.bucketEdge) {
                        case -1:

                            canvas.drawCircle(stopXLeft, stopY, (float) (5.5 / DataSaved.scale_Factor3D), paint);

                            break;
                        case 0:

                            canvas.drawCircle(stopX, stopY, (float) (5.5 / DataSaved.scale_Factor3D), paint);

                            break;
                        case 1:

                            canvas.drawCircle(stopXRight, stopY, (float) (5.5 / DataSaved.scale_Factor3D), paint);

                            break;
                    }
                    paint.setStyle(Paint.Style.STROKE);

                    drawLama(canvas);
                    if (DataSaved.isWL == WHEELLOADER) {
                        drawGomme();
                        drawBracciW(canvas);
                        drawLama(canvas);
                        drawWheel();
                    } else {
                        drawCingoliDozer();
                        drawBracci(canvas);
                        drawDozer();
                    }
                    switch (mode) {
                        case 0:
                            disegna1PD(canvas);
                            break;
                        case 1:
                            disegnaAB(canvas);
                            break;
                        case 2:
                            update(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                            switch (DataSaved.bucketEdge) {
                                case -1:

                                    drawArea(canvas, paint, Color.parseColor("#50FFFF00"));
                                    break;

                                case 0:
                                    drawArea(canvas, paint, Color.parseColor("#50FFFF00"));
                                    break;

                                case 1:
                                    drawArea(canvas, paint, Color.parseColor("#50FFFF00"));
                                    break;
                            }

                        case 3:
                            updatePoly(NmeaListener.mch_Orientation + DataSaved.deltaGPS2, Activity_Crea_Superficie.indexSel);

                            switch (DataSaved.bucketEdge) {
                                case -1:
                                    drawPolyTrench(canvas, paint,NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                                    break;

                                case 0:
                                    drawPolyTrench(canvas, paint,NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                                    break;

                                case 1:
                                    drawPolyTrench(canvas, paint,NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                                    break;
                            }
                            break;

                        case 4:
                            updateTri(NmeaListener.mch_Orientation + DataSaved.deltaGPS2, Activity_Crea_Superficie.indexSel);
                            switch (DataSaved.bucketEdge) {
                                case -1:
                                    drawTriangles(canvas, paint);
                                    break;

                                case 0:
                                    drawTriangles(canvas, paint);
                                    break;

                                case 1:
                                    drawTriangles(canvas, paint);
                                    break;
                            }


                            break;
                    }
                    canvas.restore();
                    break;

                case GRADER:

                    //grader

                    l_bucket = DataSaved.W_Blade_TOT;
                    w_bucket = DataSaved.W_Blade_TOT;
                    d_stick = 0;
                    originPointCarro = new PointF(getWidth() * 0.5f, getHeight() * 0.5f);
                    canvas.save();
                    canvas.scale((float) DataSaved.scale_Factor3D, (float) DataSaved.scale_Factor3D, getWidth() * 0.5f, getHeight() * 0.65f);
                    canvas.translate(offsetX, offsetY);

                    dist = 0.5;

                    bucketHeight = dist * scala;

                    bucketWidth = DataSaved.W_Blade_TOT * scala;
                    originPointStick = new PointF(originPointCarro.x, originPointCarro.y - (float) lcarro * 0.7f);

                    originPointBucket = new PointF(originPointStick.x, originPointStick.y);
                    stickWidth = scala * 0.40f;
                    left_top_bucket = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
                    left_top_bucket2 = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
                    right_bottom_bucket = new PointF(originPointBucket.x + (float) bucketWidth / 2f, originPointBucket.y);

                    buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
                    buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
                    stopX = (float) ((left_top_bucket.x + (mRoll) + right_bottom_bucket.x + (mRoll)) / 2f);
                    stopY = left_top_bucket.y;
                    stopXLeft = (float) (left_top_bucket.x + (mRoll));
                    stopXRight = (float) (right_bottom_bucket.x + (mRoll));
                    bucketX = (float) ((((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));
                    bucketY = left_top_bucket.y;
                    drawLama(canvas);
                    drawGrader();
                    drawGommeGrader();
                    paint.setColor(getResources().getColor(R.color.red));
                    paint.setStrokeWidth(1.5f);
                    paint.setStyle(Paint.Style.FILL);
                    switch (DataSaved.bucketEdge) {
                        case -1:

                            canvas.drawCircle(stopXLeft, stopY, (float) (5.5 / DataSaved.scale_Factor3D), paint);

                            break;
                        case 0:

                            canvas.drawCircle(stopX, stopY, (float) (5.5 / DataSaved.scale_Factor3D), paint);

                            break;
                        case 1:

                            canvas.drawCircle(stopXRight, stopY, (float) (5.5 / DataSaved.scale_Factor3D), paint);

                            break;
                    }
                    paint.setStyle(Paint.Style.STROKE);


                    switch (mode) {
                        case 0:
                            disegna1PD(canvas);
                            break;
                        case 1:
                            disegnaAB(canvas);
                            break;
                        case 2:
                            update(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                            switch (DataSaved.bucketEdge) {
                                case -1:

                                    drawArea(canvas, paint, Color.parseColor("#50FFFF00"));
                                    break;

                                case 0:
                                    drawArea(canvas, paint, Color.parseColor("#50FFFF00"));
                                    break;

                                case 1:
                                    drawArea(canvas, paint, Color.parseColor("#50FFFF00"));
                                    break;
                            }

                        case 3:
                            updatePoly(NmeaListener.mch_Orientation + DataSaved.deltaGPS2, Activity_Crea_Superficie.indexSel);
                            switch (DataSaved.bucketEdge) {
                                case -1:
                                    drawPolyTrench(canvas, paint,NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                                    break;

                                case 0:
                                    drawPolyTrench(canvas, paint,NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                                    break;

                                case 1:
                                    drawPolyTrench(canvas, paint,NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                                    break;
                            }

                            break;

                        case 4:
                            updateTri(NmeaListener.mch_Orientation + DataSaved.deltaGPS2, Activity_Crea_Superficie.indexSel);
                            switch (DataSaved.bucketEdge) {
                                case -1:
                                    drawTriangles(canvas, paint);
                                    break;

                                case 0:
                                    drawTriangles(canvas, paint);
                                    break;

                                case 1:
                                    drawTriangles(canvas, paint);
                                    break;
                            }


                            break;
                    }
                    canvas.restore();

                    break;
            }

        } catch (Exception e) {
        }
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
            offsetX -= (float) (distanceX / DataSaved.scale_Factor3D);
            offsetY -= (float) (distanceY / DataSaved.scale_Factor3D);
            invalidate();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Ripristina il pan al doppio tap
            offsetX = 0;
            offsetY = 150;
            invalidate();
            return true;
        }
    }

    // Gestione dello zoom
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            DataSaved.scale_Factor3D *= detector.getScaleFactor();
            // Limita il fattore di scala
            DataSaved.scale_Factor3D = Math.max(0.05f, Math.min(DataSaved.scale_Factor3D, 10.0f));
            invalidate();
            return true;
        }
    }

    private void initEscavatore() {
        lcarro = (DataSaved.L_Boom1 + DataSaved.L_Boom2 + DataSaved.L_Stick) / 4 * scala;
        originPointStick = new PointF(originPointCarro.x, originPointCarro.y - (float) lcarro * 0.7f);
        left_top_carro = new PointF(originPointCarro.x - (float) lcarro, originPointCarro.y - (float) lcarro * 1.15f);
        right_bottom_carro = new PointF(originPointCarro.x + (float) lcarro, originPointCarro.y + (float) lcarro * 1.15f);
    }

    private void drawCingoli(float baseLineX) {
        // Calcola la distanza dei cingoli basata sul centro del carro
        double distanceCingoli = calculateDistance(originPointCarro.x, originPointCarro.y, originPointStick.x, originPointStick.y - (float) lcarro);
        cingoliWidth = (float) (distanceCingoli * 0.20f);
        cingoliHeight = (float) (distanceCingoli * 0.10f);

        // Centra i cingoli rispetto alla nuova posizione laterale
        float carroWidth = (right_bottom_carro.x - left_top_carro.x); // Larghezza del carro
        float carroCenterX = baseLineX; // Il centro del carro segue la X della base della linea
        float leftCarroX = carroCenterX - carroWidth / 2; // Nuova X sinistra del carro
        float rightCarroX = carroCenterX + carroWidth / 2; // Nuova X destra del carro

        // Cingoli sinistri
        paint.setColor(getResources().getColor(R.color._____cancel_text));
        paint.setStyle(Paint.Style.FILL);

        if (DataSaved.isWL == EXCAVATOR) {
            canvas.drawRoundRect(leftCarroX - cingoliWidth, left_top_carro.y - (cingoliHeight * 5),
                    leftCarroX + cingoliWidth, right_bottom_carro.y - cingoliHeight, 10, 10, paint);
        } else if (DataSaved.isWL == WHEELLOADER) {
            //TODO whwwloader
        }

        cingoliLength = calculateDistance(leftCarroX - cingoliWidth, left_top_carro.y - (cingoliHeight * 5),
                leftCarroX + cingoliWidth, right_bottom_carro.y - cingoliHeight);

        // Cingoli destri
        if (DataSaved.isWL == EXCAVATOR) {
            canvas.drawRoundRect(rightCarroX - cingoliWidth, left_top_carro.y - (cingoliHeight * 5),
                    rightCarroX + cingoliWidth, right_bottom_carro.y - cingoliHeight, 10, 10, paint);
        } else if (DataSaved.isWL == WHEELLOADER) {
            //TODO wheeloader
        }

        // Disegna le linee dei cingoli
        double tmpLine = cingoliLength / 15;
        int tmpDist = 0;
        if (DataSaved.isWL == EXCAVATOR) {
            for (int i = 0; i < 15 - 1; i++) {
                paint.setStrokeWidth(3f);
                paint.setColor(Color.rgb(27, 27, 27));
                tmpDist += tmpLine;

                // Linee sui cingoli sinistri
                canvas.drawLine(leftCarroX - cingoliWidth, left_top_carro.y - (cingoliHeight * 5) + tmpDist,
                        leftCarroX + cingoliWidth, left_top_carro.y - (cingoliHeight * 5) + tmpDist, paint);

                // Linee sui cingoli destri
                canvas.drawLine(rightCarroX - cingoliWidth, left_top_carro.y - (cingoliHeight * 5) + tmpDist,
                        rightCarroX + cingoliWidth, left_top_carro.y - (cingoliHeight * 5) + tmpDist, paint);
            }
        }
    }


    private void drawExca(float baseLineX) {
        // Aggiorna la posizione del carro in base alla X della linea
        float carroWidth = (right_bottom_carro.x - left_top_carro.x); // Larghezza attuale del carro
        float carroHeight = (right_bottom_carro.y - left_top_carro.y); // Altezza attuale del carro

        // Centrleft_top_carroa il carro rispetto alla linea
        left_top_carro = new PointF(baseLineX - carroWidth / 2, right_bottom_carro.y - carroHeight);
        right_bottom_carro = new PointF(baseLineX + carroWidth / 2, right_bottom_carro.y);
        Path path = new Path();
        paint.setColor(MyColorClass.colorStick);
        path.moveTo(left_top_carro.x, right_bottom_carro.y);
        canvas.drawRoundRect(left_top_carro.x, left_top_carro.y, right_bottom_carro.x, right_bottom_carro.y, 1, 1, paint);//frame

        //zavorra
        distanzaPosteriore = calculateDistance(left_top_carro.x, left_top_carro.y, left_top_carro.x, right_bottom_carro.y);
        ellipse = new PointF[30];
        RectF oval = new RectF(left_top_carro.x, (float) (right_bottom_carro.y - (distanzaPosteriore * 0.25f)), right_bottom_carro.x, (float) (right_bottom_carro.y + (distanzaPosteriore * 0.20f)));
        float a = oval.width() / 2;
        float b = oval.height() / 2;
        float centerX = oval.centerX();
        float centerY = oval.centerY();

        for (int i = 0; i < ellipse.length; i++) {
            float theta = (float) (i * 2 * Math.PI / ellipse.length);
            float x = centerX + a * (float) Math.cos(theta);
            float y = centerY + b * (float) Math.sin(theta);
            ellipse[i] = new PointF(x, y);
        }

        for (int i = 0; i <= ellipse.length / 2f; i++) {
            path.lineTo(ellipse[i].x, ellipse[i].y);
        }
        path.close();
        canvas.drawPath(path, paint);
        path.reset();

        paint.setColor(getResources().getColor(R.color.volvo_grey));
        canvas.drawRoundRect(left_top_carro.x + (float) (distanzaPosteriore * 0.025f), left_top_carro.y + (float) (distanzaPosteriore * 0.045f), left_top_carro.x + (right_bottom_carro.x - left_top_carro.x) * 0.5f - (float) (distanzaPosteriore * 0.075f), originPointStick.y + (float) (distanzaPosteriore * 0.25f), 2, 2, paint);


    }


    private void drawStick(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);

        // Definizione dei punti chiave
        left_top_stick = new PointF(originPointStick.x - (float) stickWidth / 2f, originPointStick.y - (float) distStick);
        right_bottom_stick = new PointF(originPointStick.x + (float) stickWidth / 2f, originPointStick.y);
        ancorPX = (float) (((left_top_bucket.x + right_bottom_bucket.x) * 0.5f)) + mRoll;
        ancorPY = left_top_bucket.y;
        // Calcolo del centro dei cerchi
        float centerX = (float) ((left_top_stick.x + right_bottom_stick.x) * 0.5);
        float centerY = left_top_stick.y;
        float radius = Math.abs(right_bottom_stick.x - left_top_stick.x) * 0.8f;

        // Rotazione del punto di partenza
        double angleRadians = Math.toRadians(ExcavatorLib.yawSensor);
        float rotatedCenterX = (float) (ancorPX + (centerX - ancorPX) * Math.cos(angleRadians) - (centerY - ancorPY) * Math.sin(angleRadians)) + mRoll;
        float rotatedCenterY = (float) (ancorPY + (centerX - ancorPX) * Math.sin(angleRadians) + (centerY - ancorPY) * Math.cos(angleRadians));

        // Disegna i cerchi
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(5f);
        canvas.drawCircle(rotatedCenterX, rotatedCenterY, radius, paint);
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(rotatedCenterX, rotatedCenterY, radius, paint);
        if (DataSaved.isTiltRotator && DataSaved.lrTilt != 0) {

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(18f);
            paint.setColor(Color.WHITE);
            canvas.drawText(String.format("%.1f", Sensors_Decoder.Deg_Roto).replace(",", ".") + "°", rotatedCenterX - radius, rotatedCenterY - 5f, paint);
        }
        // Disegna la linea verticale
        paint.setColor(MyColorClass.colorStick);
        paint.setStrokeWidth((float) stickWidth);

        // La linea parte dal centro ruotato e finisce in verticale
        float startX = rotatedCenterX; // Stesso X del centro ruotato
        float startY = rotatedCenterY; // Punto di partenza è il centro ruotato
        float endX = rotatedCenterX;   // Rimane verticale
        float endY = right_bottom_stick.y; // Fine della linea è sempre la Y globale richiesta

        canvas.drawLine(startX, startY, endX, endY, paint); // Disegna la linea
        // Calcolo dei vertici del triangolo
        float baseHalf = (float) ((float) stickWidth * 1.5); // Metà della base del triangolo
        float height = (float) (Math.sqrt(3) * baseHalf); // Altezza del triangolo equilatero

        // Vertice superiore del triangolo (punta del triangolo)
        float topX = endX;
        float topY = endY - height; // Punta rivolta verso i cerchi

        // Vertici della base
        float leftBaseX = endX - baseHalf;
        float leftBaseY = endY;

        float rightBaseX = endX + baseHalf;
        float rightBaseY = endY;
        float delta = 40;
        if (DataSaved.L_Boom1 < 2) {
            delta = 25;
        }

        // Disegna il triangolo
        Path trianglePath = new Path();
        trianglePath.moveTo(topX, topY - delta); // Punta del triangolo
        trianglePath.lineTo(leftBaseX, leftBaseY); // Vertice sinistro
        trianglePath.lineTo(rightBaseX, rightBaseY); // Vertice destro
        trianglePath.close();
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(trianglePath, paint);
        float baseLineX = rotatedCenterX - mRoll; // X della base della linea calcolata
        drawCingoli(baseLineX);
        drawExca(baseLineX);

    }


    private void drawBenna(Canvas canvas) {

        canvas.rotate((float) ExcavatorLib.yawSensor, (ancorPX), ancorPY);

        paint.setColor(MyColorClass.colorBucket);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect((float) (left_top_bucket.x + mRoll), left_top_bucket.y, (float) (right_bottom_bucket.x + mRoll), right_bottom_bucket.y, paint);
        if (DataSaved.isWL == EXCAVATOR) {
            if (ExcavatorLib.correctBucket < -90 || ExcavatorLib.correctBucket > 90) {
                paint.setColor(getResources().getColor(R.color.transparentgray));
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1.5f);
                canvas.drawRect((float) (left_top_bucket.x + mRoll) + 2.5f, left_top_bucket.y - 2f, (float) (right_bottom_bucket.x + mRoll) - 2.5f, right_bottom_bucket.y + 1f, paint);

            }
        } else {
            if (ExcavatorLib.correctBucket < 90 && ExcavatorLib.correctBucket > -90) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(getResources().getColor(R.color.transparentgray));
                paint.setStrokeWidth(1.5f);
                canvas.drawRect((float) (left_top_bucket.x + mRoll) + 2.5f, left_top_bucket.y - 2f, (float) (right_bottom_bucket.x + mRoll) - 2.5f, right_bottom_bucket.y + 1f, paint);

            }

        }
        paint.setColor(MyColorClass.colorBucket);
        paint.setStyle(Paint.Style.FILL);

        //disegna la benna fake a larghezza fissa per evitare l'effetto di far scomparire la benna
        canvas.drawRect((float) (left_top_bucket2.x + mRoll), left_top_bucket2.y, (float) (right_bottom_bucket.x + mRoll), right_bottom_bucket.y, paint);
        canvas.rotate((float) -ExcavatorLib.yawSensor, (ancorPX), ancorPY);


        //disegna la croce
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth((float) (2 / DataSaved.scale_Factor3D));
        float stopX = (float) ((left_top_bucket.x + (mRoll) + right_bottom_bucket.x + (mRoll)) / 2f);
        float stopY = left_top_bucket.y;
        float stopXLeft = (float) (left_top_bucket.x + (mRoll));
        float stopXRight = (float) (right_bottom_bucket.x + (mRoll));
        if (DataSaved.showAlign == 1) {
            switch (DataSaved.bucketEdge) {
                case -1:
                    canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    canvas.drawLine(stopXLeft, stopY, stopXLeft, stopY - 10000f, paint);//fw
                    canvas.drawLine(stopXLeft, stopY, stopXLeft, stopY + 10000f, paint);//bw
                    canvas.drawLine(stopXLeft, stopY, stopXLeft - 10000f, stopY, paint);//left
                    canvas.drawLine(stopXLeft, stopY, stopXLeft + 10000f, stopY, paint);//right
                    canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    break;

                case 0:
                    canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    canvas.drawLine(stopX, stopY, stopX, stopY - 10000f, paint);//fw
                    canvas.drawLine(stopX, stopY, stopX, stopY + 10000f, paint);//bw
                    canvas.drawLine(stopX, stopY, stopX - 10000f, stopY, paint);//left
                    canvas.drawLine(stopX, stopY, stopX + 10000f, stopY, paint);//right
                    canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    break;

                case 1:
                    canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    canvas.drawLine(stopXRight, stopY, stopXRight, stopY - 10000f, paint);//fw
                    canvas.drawLine(stopXRight, stopY, stopXRight, stopY + 10000f, paint);//bw
                    canvas.drawLine(stopXRight, stopY, stopXRight - 10000f, stopY, paint);//left
                    canvas.drawLine(stopXRight, stopY, stopXRight + 10000f, stopY, paint);//right
                    canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    break;
            }
            paint.setStrokeWidth(1.5f);
            paint.setColor(MyColorClass.colorBucket);
        }

    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }


    ////grade
    private void initDozer() {
        //carro

        lcarro = w_bucket * scala * 0.5;
        originPointStick = new PointF(originPointCarro.x, originPointCarro.y - (float) lcarro * 0.7f);
        left_top_carro = new PointF(originPointCarro.x + (float) lcarro, originPointCarro.y - (float) lcarro * 1.15f);
        right_bottom_carro = new PointF(originPointCarro.x - (float) lcarro, originPointCarro.y + (float) lcarro * 1.15f);
        paint.setColor(getResources().getColor(R.color._____cancel_text));
        distanceCingoli = calculateDistance(originPointCarro.x, originPointCarro.y, originPointStick.x, originPointStick.y - (float) lcarro);
        cingoliWidth = (float) (distanceCingoli * 0.40f);
        cingoliHeight = (float) (distanceCingoli * 0.10f);


    }

    private void drawCingoliDozer() {


        paint.setColor(getResources().getColor(R.color._____cancel_text));
        // Cingoli sinistri
        canvas.drawRoundRect((float) (left_top_carro.x - cingoliWidth), left_top_carro.y + (cingoliHeight * 5), (float) (left_top_carro.x), right_bottom_carro.y + cingoliHeight * 4, 10, 10, paint);

        cingoliLength = calculateDistance(left_top_carro.x - cingoliWidth, left_top_carro.y - (cingoliHeight * 5), left_top_carro.x + cingoliWidth, right_bottom_carro.y - cingoliHeight);

        // Cingoli destri
        canvas.drawRoundRect(right_bottom_carro.x + cingoliWidth, left_top_carro.y + (cingoliHeight * 5), right_bottom_carro.x, right_bottom_carro.y + cingoliHeight * 4, 10, 10, paint);

        double tmpLine = cingoliLength / 15;
        int tmpDist = 0;

        for (int i = 0; i < 11 - 1; i++) {
            paint.setStrokeWidth((float) (4));
            paint.setColor(Color.LTGRAY);
            tmpDist += tmpLine;
            canvas.drawLine(left_top_carro.x, left_top_carro.y + (cingoliHeight * 4.5f) + tmpDist, left_top_carro.x - cingoliWidth, left_top_carro.y + (cingoliHeight * 4.5f) + tmpDist, paint);
            canvas.drawLine(right_bottom_carro.x, left_top_carro.y + (cingoliHeight * 4.5f) + tmpDist, right_bottom_carro.x + cingoliWidth, left_top_carro.y + (cingoliHeight * 4.5f) + tmpDist, paint);

        }
    }

    private void drawDozer() {
        paint.setColor(MyColorClass.colorStick);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(left_top_carro.x - 10f, (float) (left_top_carro.y + w_bucket * scala), right_bottom_carro.x + 10f, (float) (right_bottom_carro.y + w_bucket * 0.4 * scala), 5, 5, paint);//frame
        canvas.drawRoundRect(left_top_carro.x - 20f, (float) (left_top_carro.y + w_bucket * 0.5 * scala), right_bottom_carro.x + 20f, (float) (right_bottom_carro.y + w_bucket * 0.4 * scala), 5, 5, paint);//frame
        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(left_top_carro.x - 20f, (float) (left_top_carro.y + 15 + w_bucket * scala), right_bottom_carro.x + 20f, (float) (right_bottom_carro.y - 10 + w_bucket * 0.4 * scala), 5, 5, paint);

    }

    private void drawWheel() {
        float midX = (left_top_carro.x + right_bottom_carro.x) / 2;
        paint.setColor(MyColorClass.colorStick);
        paint.setStyle(Paint.Style.FILL);
        if (DataSaved.W_Bucket < 1.80) {
            canvas.drawRoundRect(midX - 5f, (float) (left_top_carro.y + w_bucket * scala), midX + 5f, (float) (right_bottom_carro.y + w_bucket * 0.3 * scala), 3, 3, paint);//frame
            canvas.drawRoundRect(left_top_carro.x - 8f, (float) (left_top_carro.y - 12f + w_bucket * scala), right_bottom_carro.x + 8f, (float) (right_bottom_carro.y + w_bucket * 0.3 * scala), 3, 3, paint);
            paint.setColor(Color.DKGRAY);
            canvas.drawRoundRect(left_top_carro.x - 10f, (float) (left_top_carro.y - 6f + w_bucket * scala), right_bottom_carro.x + 10f, (float) (right_bottom_carro.y - 10f + w_bucket * 0.3 * scala), 3, 3, paint);
        } else {
            canvas.drawRoundRect(midX - 15f, (float) (left_top_carro.y + (w_bucket * 0.5) * scala), midX + 15f, (float) (right_bottom_carro.y + w_bucket * 0.4 * scala), 5, 5, paint);//frame
            canvas.drawRoundRect(left_top_carro.x - 18f, (float) (left_top_carro.y - 18f + w_bucket * scala), right_bottom_carro.x + 18f, (float) (right_bottom_carro.y - 32f + w_bucket * 0.4 * scala), 5, 5, paint);
            paint.setColor(Color.DKGRAY);
            canvas.drawRoundRect(left_top_carro.x - 24f, (float) (left_top_carro.y - 12f + w_bucket * scala), right_bottom_carro.x + 24f, (float) (right_bottom_carro.y - 38f + w_bucket * 0.4 * scala), 5, 5, paint);

        }
    }

    private void drawGomme() {
        paint.setColor(getResources().getColor(R.color._____cancel_text));
        // gomme destre
        canvas.drawRoundRect((float) (left_top_carro.x - cingoliWidth) - 5f, ((right_bottom_carro.y + left_top_carro.y) / 2) - 10f, (float) (left_top_carro.x) - 5f, ((right_bottom_carro.y + left_top_carro.y) / 2) - 10f + 35f, 10, 10, paint);
        canvas.drawRoundRect((float) (left_top_carro.x - cingoliWidth) - 5f, right_bottom_carro.y, (float) (left_top_carro.x) - 5f, right_bottom_carro.y + 35f, 10, 10, paint);

        // gomme sinistre
        canvas.drawRoundRect(right_bottom_carro.x + cingoliWidth + 5f, ((right_bottom_carro.y + left_top_carro.y) / 2) - 10f, right_bottom_carro.x + 5f, ((right_bottom_carro.y + left_top_carro.y) / 2) - 10f + 35f, 10, 10, paint);
        canvas.drawRoundRect(right_bottom_carro.x + cingoliWidth + 5f, right_bottom_carro.y, right_bottom_carro.x + 5f, right_bottom_carro.y + 35f, 10, 10, paint);

    }

    private void drawGommeGrader() {
        float distanza_Cab = (float) (Math.max(0.3, Math.min(DataSaved.W_Blade_TOT / 3, 0.8)) * scala);
        float distanza_ruote_dietro = (float) (Math.max(0.8, Math.min(DataSaved.W_Blade_TOT / 3, 1.6)) * scala);
        float distanza_ruotr_Av = (float) (Math.max(0.6, Math.min(DataSaved.W_Blade_TOT / 2, 1.3)) * scala);
        float larghezza_Cabina = (float) (Math.max(1.2, Math.min(DataSaved.W_Blade_TOT, 1.8)) / 2 * scala);
        float larghezza_Ruote = (float) (Math.max(0.3, Math.min(DataSaved.W_Blade_TOT, 0.8)) / 2 * scala);//base rettangolo
        float altezza_ruote = larghezza_Cabina * 0.7f;
        float larghezza_Arcone = (float) (0.3 * scala);


        float cX_left = ((left_top_bucket.x + right_bottom_bucket.x) / 2) - larghezza_Cabina / 2;
        float cX_right = ((left_top_bucket.x + right_bottom_bucket.x) / 2) + larghezza_Cabina / 2;
        float cY_top = (left_top_bucket.y + right_bottom_bucket.y) / 2 - distanza_ruotr_Av;
        float cY_bottom = (left_top_bucket.y + right_bottom_bucket.y) / 2 + distanza_Cab;

        paint.setColor(getResources().getColor(R.color._____cancel_text));
        //ruote sx
        canvas.drawRoundRect(cX_left - larghezza_Ruote / 2, cY_bottom - altezza_ruote / 2, cX_left + larghezza_Ruote / 2, cY_bottom + altezza_ruote / 2, 3f, 3f, paint);
        canvas.drawRoundRect(cX_left - larghezza_Ruote / 2, (cY_bottom - altezza_ruote / 2) + distanza_ruote_dietro / 1.5f, cX_left + larghezza_Ruote / 2, (cY_bottom + altezza_ruote / 2) + distanza_ruote_dietro / 1.5f, 3f, 3f, paint);
        canvas.drawRoundRect(cX_left - larghezza_Ruote / 2, (cY_top - altezza_ruote / 2), cX_left + larghezza_Ruote / 2, (cY_top + altezza_ruote / 2), 3f, 3f, paint);

        //ruote dx
        canvas.drawRoundRect(cX_right - larghezza_Ruote / 2, cY_bottom - altezza_ruote / 2, cX_right + larghezza_Ruote / 2, cY_bottom + altezza_ruote / 2, 3f, 3f, paint);
        canvas.drawRoundRect(cX_right - larghezza_Ruote / 2, (cY_bottom - altezza_ruote / 2) + distanza_ruote_dietro / 1.5f, cX_right + larghezza_Ruote / 2, (cY_bottom + altezza_ruote / 2) + distanza_ruote_dietro / 1.5f, 3f, 3f, paint);
        canvas.drawRoundRect(cX_right - larghezza_Ruote / 2, (cY_top - altezza_ruote / 2), cX_right + larghezza_Ruote / 2, (cY_top + altezza_ruote / 2), 3f, 3f, paint);

        paint.setColor(MyColorClass.colorStick);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(cX_left, cY_bottom - distanza_Cab / 2, cX_right, (cY_bottom + altezza_ruote / 2), 3f, 3f, paint);

        canvas.drawRoundRect(cX_left + larghezza_Ruote / 3f, (cY_bottom - distanza_Cab / 2) + 4f, cX_right - larghezza_Ruote / 3f, (cY_bottom + altezza_ruote + altezza_ruote), 3f, 3f, paint);

        paint.setStrokeWidth(larghezza_Arcone);
        canvas.drawLine((left_top_bucket.x + right_bottom_bucket.x) / 2, cY_bottom, (left_top_bucket.x + right_bottom_bucket.x) / 2, cY_top, paint);
        paint.setStrokeWidth((float) 0.15 * scala);
        canvas.drawLine(cX_left + larghezza_Ruote / 2, cY_top, cX_right - larghezza_Ruote / 2, cY_top, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(getResources().getColor(R.color._____cancel_text));
        canvas.drawRoundRect(cX_left + 2f, (cY_bottom - distanza_Cab / 2) + 2f, cX_right - 2f, (cY_bottom + altezza_ruote / 2) - 2f, 3f, 3f, paint);
        paint.setStrokeWidth(5.25f);

    }

    private void drawGrader() {
        drawLama(canvas);
    }

    private void drawBracci(Canvas canvas) {
        paint.setColor(MyColorClass.colorStick);
        paint.setStrokeWidth(6f);
        paint.setStyle(Paint.Style.STROKE);
        float inX = (float) ((left_top_bucket.x + (0 * scala) + right_bottom_bucket.x + (0 * scala)) / 2f);
        float inY = left_top_bucket.y + 2f;
        canvas.drawRect(inX - 8f, inY + 2f, inX + 8f, originPointCarro.y, paint);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    private void drawBracciW(Canvas canvas) {
        float stopX = (float) ((left_top_bucket.x + (mRoll) + right_bottom_bucket.x + (mRoll)) / 2f);
        float stopXLeft = (float) (left_top_bucket.x + (mRoll));
        float stopXRight = (float) (right_bottom_bucket.x + (mRoll));
        paint.setColor(MyColorClass.colorStick);
        paint.setStrokeWidth(6f);
        paint.setStyle(Paint.Style.STROKE);
        float inY = left_top_bucket.y + 2f;
        canvas.drawRect((stopXLeft + stopX) / 2f, inY + 3f, (stopXRight + stopX) / 2f, originPointCarro.y, paint);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void drawLama(Canvas canvas) {
        float delta = 0;
        if (DataSaved.isWL == 4) {
            delta = (float) (0.25 * scala);
        }

        if (DataSaved.isWL == 1 || DataSaved.isWL == 4) {
            paint.setColor(MyColorClass.colorBucket);
        } else {
            paint.setColor(MyColorClass.colorStick);
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect((left_top_bucket2.x), left_top_bucket2.y, (right_bottom_bucket.x), right_bottom_bucket.y - delta, paint);


        //disegna la croce
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth((float) (2 / DataSaved.scale_Factor3D));
        float stopX = (float) ((left_top_bucket.x + (mRoll) + right_bottom_bucket.x + (mRoll)) / 2f);
        float stopY = left_top_bucket.y;
        float stopXLeft = (float) (left_top_bucket.x + (mRoll));
        float stopXRight = (float) (right_bottom_bucket.x + (mRoll));
        if (DataSaved.showAlign == 1) {
            switch (DataSaved.bucketEdge) {
                case -1:
                    canvas.drawLine(stopXLeft, stopY, stopXLeft, stopY - 10000f, paint);//fw
                    canvas.drawLine(stopXLeft, stopY, stopXLeft, stopY + 10000f, paint);//bw
                    canvas.drawLine(stopXLeft, stopY, stopXLeft - 10000f, stopY, paint);//left
                    canvas.drawLine(stopXLeft, stopY, stopXLeft + 10000f, stopY, paint);//right
                    break;

                case 0:
                    canvas.drawLine(stopX, stopY, stopX, stopY - 10000f, paint);//fw
                    canvas.drawLine(stopX, stopY, stopX, stopY + 10000f, paint);//bw
                    canvas.drawLine(stopX, stopY, stopX - 10000f, stopY, paint);//left
                    canvas.drawLine(stopX, stopY, stopX + 10000f, stopY, paint);//right
                    break;

                case 1:
                    canvas.drawLine(stopXRight, stopY, stopXRight, stopY - 10000f, paint);//fw
                    canvas.drawLine(stopXRight, stopY, stopXRight, stopY + 10000f, paint);//bw
                    canvas.drawLine(stopXRight, stopY, stopXRight - 10000f, stopY, paint);//left
                    canvas.drawLine(stopXRight, stopY, stopXRight + 10000f, stopY, paint);//right
                    break;
            }
            paint.setColor(MyColorClass.colorBucket);
            paint.setStrokeWidth(1.5f);
        }

    }


    /////////Progettazione da qui
    private void disegna1PE(Canvas canvas) {
        paint.setColor(MyColorClass.colorPoint);
        // Coordinate reali del primo punto (Nord Est) e coordinate display
        double buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
        double buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
        float displayBucketC_X = (float) ((float) (((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));

        // coordinata Y del display dove viene rappresentata la benna
        float displayBucketC_Y = (float) (left_top_bucket.y);

        // Coordinate reali del secondo punto (Nord Est)
        double pointN = DataSaved.puntiProgetto[0].y; // Coordinata REALI NORD del secondo punto
        double pointE = DataSaved.puntiProgetto[0].x; // Coordinata REALI EST del secondo punto

        // rotazione basata su NmeaListener.mch_Orientation e DataSaved.deltaGPS2
        double rotationAngle = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);

        // Calcola le differenze  di coordinate display XY considerando la scala
        double diffX = (pointE - buckEst) * scala;
        double diffY = (pointN - buckNord) * scala;

        // nuove coordinate del secondo punto basate sulla rotazione e sui calcoli diff
        double rotatedX = displayBucketC_X + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle);
        double rotatedY = displayBucketC_Y - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle);


        canvas.drawCircle((float) rotatedX, (float) rotatedY, 7f, paint);

        //NB da fare classe per applicare la logica a tutti i punti
        try {
            paint.setColor(MyColorClass.colorLabel);
            paint.setTextSize(18);
            paint.setStrokeWidth(1f);
            canvas.drawText("E:" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.puntiProgetto[0].x)) + "  N:" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.puntiProgetto[0].y)) + "  Z:" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.puntiProgetto[0].z)), (float) (rotatedX + 10f), (float) (rotatedY - 10f), paint);
        } catch (Exception e) {
        }


    }

    private void disegna1PD(Canvas canvas) {

        paint.setColor(MyColorClass.colorPoint);
        // Coordinate reali del primo punto (Nord Est) e coordinate display
        double buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
        double buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
        float displayBucketC_X = (float) ((float) (((left_top_bucket.x + 0 * scala) + (right_bottom_bucket.x + 0 * scala)) * 0.5f));

        // coordinata Y del display dove viene rappresentata la benna
        float displayBucketC_Y = (float) (left_top_bucket.y);

        // Coordinate reali del secondo punto (Nord Est)
        double pointN = DataSaved.puntiProgetto[0].y; // Coordinata REALI NORD del secondo punto
        double pointE = DataSaved.puntiProgetto[0].x; // Coordinata REALI EST del secondo punto

        // rotazione basata su NmeaListener.mch_Orientation e DataSaved.deltaGPS2
        double rotationAngle = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);

        // Calcola le differenze  di coordinate display XY considerando la scala
        double diffX = (pointE - buckEst) * scala;
        double diffY = (pointN - buckNord) * scala;

        // nuove coordinate del secondo punto basate sulla rotazione e sui calcoli diff
        double rotatedX = displayBucketC_X + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle);
        double rotatedY = displayBucketC_Y - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle);


        canvas.drawCircle((float) rotatedX, (float) rotatedY, 7f, paint);

        //NB da fare classe per applicare la logica a tutti i punti
        try {

            paint.setColor(MyColorClass.colorLabel);
            paint.setTextSize(18);
            paint.setStrokeWidth(1f);
            canvas.drawText("E:" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.puntiProgetto[0].x)) + "  N:" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.puntiProgetto[0].y)) + "  Z:" + Utils.readUnitOfMeasureLITE(String.valueOf(DataSaved.puntiProgetto[0].z)), (float) (rotatedX + 10f), (float) (rotatedY - 10f), paint);
        } catch (Exception e) {
            Log.e("Excp", e.toString());
        }


    }


    private void disegnaAB(Canvas canvas) {
        coordinates = new Coordinate[puntiAB.length];
        paint.setColor(MyColorClass.colorPoint);
        // Coordinate reali del primo punto (Nord Est) e coordinate display
        double buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
        double buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
        for (int i = 0; i < puntiAB.length; i++) {

            // Coordinate reali del secondo punto (Nord Est)
            double pointN = puntiAB[i].getY(); // Coordinata REALI NORD del secondo punto
            double pointE = puntiAB[i].getX(); // Coordinata REALI EST del secondo punto

            // rotazione basata su NmeaListener.mch_Orientation e DataSaved.deltaGPS2
            double rotationAngle = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);

            // Calcola le differenze  di coordinate display XY considerando la scala
            double diffX = (pointE - buckEst) * scala;
            double diffY = (pointN - buckNord) * scala;

            float bucketX = (float) ((((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));
            float bucketY = left_top_bucket.y;

            // nuove coordinate del secondo punto basate sulla rotazione e sui calcoli diff
            float rotatedX = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
            float rotatedY = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));

            coordinates[i] = new Coordinate(rotatedX, rotatedY, 0);

            canvas.drawCircle(rotatedX, rotatedY, 20 / 2.5f, paint);


            paint.setTextSize(34);
            if (i == 0) {
                canvas.drawText("A", rotatedX + 25f, rotatedY - 25f, paint);

            }

            if (i == 1) {
                canvas.drawText("B", rotatedX + 25f, rotatedY - 25f, paint);
            }

            if (puntiAB[1] != puntiAB[2] && i == 2) {
                canvas.drawText("C", rotatedX + 25f, rotatedY - 25f, paint);
            }

            if (puntiAB[0] != puntiAB[3] && i == 3) {
                canvas.drawText("D", rotatedX + 25f, rotatedY - 25f, paint);
            }

            if (puntiAB[1] != puntiAB[4] && i == 4) {
                canvas.drawText("E", rotatedX + 25f, rotatedY - 25f, paint);
            }

            if (puntiAB[0] != puntiAB[5] && i == 5) {
                canvas.drawText("F", rotatedX + 25f, rotatedY - 25f, paint);
            }


        }

        paint.setColor(MyColorClass.colorTriangle);
        paint.setStrokeWidth(3f);
        // A - B
        canvas.drawLine((float) coordinates[0].x, (float) coordinates[0].y, (float) coordinates[1].x, (float) coordinates[1].y, paint);

        // B - C
        canvas.drawLine((float) coordinates[1].x, (float) coordinates[1].y, (float) coordinates[2].x, (float) coordinates[2].y, paint);

        // C - D
        canvas.drawLine((float) coordinates[2].x, (float) coordinates[2].y, (float) coordinates[3].x, (float) coordinates[3].y, paint);

        // D - A
        canvas.drawLine((float) coordinates[3].x, (float) coordinates[3].y, (float) coordinates[0].x, (float) coordinates[0].y, paint);

        // B - E
        canvas.drawLine((float) coordinates[1].x, (float) coordinates[1].y, (float) coordinates[4].x, (float) coordinates[4].y, paint);

        // E - F
        canvas.drawLine((float) coordinates[4].x, (float) coordinates[4].y, (float) coordinates[5].x, (float) coordinates[5].y, paint);

        // F - A
        canvas.drawLine((float) coordinates[5].x, (float) coordinates[5].y, (float) coordinates[0].x, (float) coordinates[0].y, paint);
        // Imposta il colore di riempimento per l'area grigia semitrasparente
        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setARGB(128, 128, 128, 128); // Grigio con opacità 50% (128 su 255)

// Crea un oggetto Path per il quadrilatero ABCD
        Path pathABCD = new Path();
        pathABCD.moveTo((float) coordinates[0].x, (float) coordinates[0].y); // Punto A
        pathABCD.lineTo((float) coordinates[1].x, (float) coordinates[1].y); // Punto B
        pathABCD.lineTo((float) coordinates[2].x, (float) coordinates[2].y); // Punto C
        pathABCD.lineTo((float) coordinates[3].x, (float) coordinates[3].y); // Punto D
        pathABCD.close(); // Chiude il percorso per creare il quadrilatero ABCD

// Disegna il riempimento grigio semitrasparente per ABCD
        canvas.drawPath(pathABCD, fillPaint);

// Crea un oggetto Path per il quadrilatero ABEF
        Path pathABEF = new Path();
        pathABEF.moveTo((float) coordinates[0].x, (float) coordinates[0].y); // Punto A
        pathABEF.lineTo((float) coordinates[1].x, (float) coordinates[1].y); // Punto B
        pathABEF.lineTo((float) coordinates[4].x, (float) coordinates[4].y); // Punto E
        pathABEF.lineTo((float) coordinates[5].x, (float) coordinates[5].y); // Punto F
        pathABEF.close(); // Chiude il percorso per creare il quadrilatero ABEF

// Disegna il riempimento grigio semitrasparente per ABEF
        canvas.drawPath(pathABEF, fillPaint);

// Poi puoi disegnare i bordi sopra il riempimento come hai fatto prima
        paint.setColor(MyColorClass.colorTriangle);
        paint.setStrokeWidth(3f);

// Disegna i bordi di ABCD
        canvas.drawLine((float) coordinates[0].x, (float) coordinates[0].y, (float) coordinates[1].x, (float) coordinates[1].y, paint); // A - B
        canvas.drawLine((float) coordinates[1].x, (float) coordinates[1].y, (float) coordinates[2].x, (float) coordinates[2].y, paint); // B - C
        canvas.drawLine((float) coordinates[2].x, (float) coordinates[2].y, (float) coordinates[3].x, (float) coordinates[3].y, paint); // C - D
        canvas.drawLine((float) coordinates[3].x, (float) coordinates[3].y, (float) coordinates[0].x, (float) coordinates[0].y, paint); // D - A

// Disegna i bordi di ABEF
        canvas.drawLine((float) coordinates[0].x, (float) coordinates[0].y, (float) coordinates[1].x, (float) coordinates[1].y, paint); // A - B
        canvas.drawLine((float) coordinates[1].x, (float) coordinates[1].y, (float) coordinates[4].x, (float) coordinates[4].y, paint); // B - E
        canvas.drawLine((float) coordinates[4].x, (float) coordinates[4].y, (float) coordinates[5].x, (float) coordinates[5].y, paint); // E - F
        canvas.drawLine((float) coordinates[5].x, (float) coordinates[5].y, (float) coordinates[0].x, (float) coordinates[0].y, paint); // F - A


    }


    private void update(double heading) {
        paint.setColor(MyColorClass.colorPoint);
        // Coordinate reali del primo punto (Nord Est) e coordinate display
        double buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
        double buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto

        rotatedPoints = new ArrayList<>();

        for (int i = 0; i < Activity_Crea_Superficie.coordinateP.size(); i++) {


            double diff_x = (Activity_Crea_Superficie.coordinateP.get(i)[0] - buckEst) * scala;
            double diff_y = (Activity_Crea_Superficie.coordinateP.get(i)[1] - buckNord) * scala;
            float bucketX = (float) ((((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));
            float bucketY = left_top_bucket.y;
            rotatedPoints.add(
                    new double[]{
                            (float) (bucketX + diff_x * Math.cos(Math.toRadians(heading)) - diff_y * Math.sin(Math.toRadians(heading))),
                            (float) (bucketY - diff_x * Math.sin(Math.toRadians(heading)) - diff_y * Math.cos(Math.toRadians(heading)))
                    });

        }

    }

    private void updateTri(double heading, int indice) {
        paint.setColor(MyColorClass.colorPoint);
        // Coordinate reali del primo punto (Nord Est) e coordinate display
        double buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
        double buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto

        rotatedPoints = new ArrayList<>();
        for (int i = 0; i < Activity_Crea_Superficie.point3DS.length; i++) {


            double diff_x = (Activity_Crea_Superficie.point3DS[i].getX() - buckEst) * scala;
            double diff_y = (Activity_Crea_Superficie.point3DS[i].getY() - buckNord) * scala;
            float bucketX = (float) ((((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));
            float bucketY = left_top_bucket.y;
            rotatedPoints.add(
                    new double[]{
                            (float) (bucketX + diff_x * Math.cos(Math.toRadians(heading)) - diff_y * Math.sin(Math.toRadians(heading))),
                            (float) (bucketY - diff_x * Math.sin(Math.toRadians(heading)) - diff_y * Math.cos(Math.toRadians(heading)))
                    });

        }
        try {
            double diff_x_s = (Activity_Crea_Superficie.point3DS[indice].getX() - buckEst) * scala;
            double diff_y_s = (Activity_Crea_Superficie.point3DS[indice].getY() - buckNord) * scala;
            puntoselezionato = new double[]{

                    (float) (bucketX + diff_x_s * Math.cos(Math.toRadians(heading)) - diff_y_s * Math.sin(Math.toRadians(heading))),
                    (float) (bucketY - diff_x_s * Math.sin(Math.toRadians(heading)) - diff_y_s * Math.cos(Math.toRadians(heading)))
            };
        } catch (Exception ignored) {

        }

    }

    private void drawArea(Canvas canvas, Paint paint, int color) {


        if (!rotatedPoints.isEmpty() && rotatedPoints.size() < 3) {


            for (int i = 0; i < rotatedPoints.size(); i++) {
                paint.setColor(MyColorClass.colorPoint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle((float) rotatedPoints.get(i)[0], (float) rotatedPoints.get(i)[1], 10, paint);
                paint.setColor(MyColorClass.colorLabel);
                canvas.drawText("P" + (i + 1), (float) (rotatedPoints.get(i)[0] + 10f), (float) rotatedPoints.get(i)[1] - 10f, paint);
            }

        } else if ((rotatedPoints.size()) >= 3) {


            for (int i = 0; i < rotatedPoints.size(); i++) {
                paint.setColor(MyColorClass.colorPoint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle((float) rotatedPoints.get(i)[0], (float) rotatedPoints.get(i)[1], 10, paint);
                paint.setColor(MyColorClass.colorLabel);
                canvas.drawText("P" + (i + 1), (float) rotatedPoints.get(i)[0] + 10f, (float) rotatedPoints.get(i)[1] - 10f, paint);
            }
            paint.setColor(MyColorClass.colorConstraint);
            for (int i = 0; i < rotatedPoints.size() - 1; i++) {
                canvas.drawLine((float) rotatedPoints.get(i)[0], (float) rotatedPoints.get(i)[1], (float) rotatedPoints.get(i + 1)[0], (float) rotatedPoints.get(i + 1)[1], paint);
            }

            canvas.drawLine((float) rotatedPoints.get(rotatedPoints.size() - 1)[0], (float) rotatedPoints.get(rotatedPoints.size() - 1)[1], (float) rotatedPoints.get(0)[0], (float) rotatedPoints.get(0)[1], paint);


            paint.setColor(color);

            Path path = new Path();

            paint.setStyle(Paint.Style.FILL);

            path.moveTo((float) rotatedPoints.get(0)[0], (float) rotatedPoints.get(0)[1]); // Muovi il percorso al primo punto
            for (int i = 1; i < rotatedPoints.size(); i++) {
                path.lineTo((float) rotatedPoints.get(i)[0], (float) rotatedPoints.get(i)[1]); // Aggiungi una linea dal punto precedente al punto attuale
            }
            path.close();
            canvas.drawPath(path, paint);
            path.reset();
        }

    }


    private void drawTriangles(Canvas canvas, Paint paint) {
        text = "";
        // Se ci sono meno di 3 punti, disegna solo i punti e le etichette
        if (rotatedPoints.isEmpty() || rotatedPoints.size() < 3) {
            for (int i = 0; i < rotatedPoints.size(); i++) {
                paint.setColor(MyColorClass.colorPoint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(
                        (float) rotatedPoints.get(i)[0],
                        (float) rotatedPoints.get(i)[1],
                        (float) (6 / DataSaved.scale_Factor3D),
                        paint
                );
                paint.setColor(MyColorClass.colorLabel);
                paint.setTextSize((float) (10 / DataSaved.scale_Factor3D));
                text = point3DS[i].getName();

                if (text == null || text.isEmpty() || text.equals(" ") || text.equals("")) {
                    text = point3DS[i].getId();
                }

                canvas.drawText(text
                        ,
                        (float) (rotatedPoints.get(i)[0] + 10f),
                        (float) (rotatedPoints.get(i)[1] - 10f),
                        paint
                );
            }
        }
        // Se ci sono 3 o più punti, disegna i punti, le etichette e le linee dei triangoli
        else if (rotatedPoints.size() >= 3) {
            // Disegna i punti e le etichette
            for (int i = 0; i < rotatedPoints.size(); i++) {
                paint.setColor(MyColorClass.colorPoint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(
                        (float) rotatedPoints.get(i)[0],
                        (float) rotatedPoints.get(i)[1],
                        (float) (6 / DataSaved.scale_Factor3D),
                        paint
                );
                paint.setColor(MyColorClass.colorLabel);
                text = point3DS[i].getName();

                if (text == null || text.isEmpty() || text.equals(" ") || text.equals("")) {
                    text = point3DS[i].getId();
                }
                Log.d("Testo", text);
                canvas.drawText(
                        text,
                        (float) (rotatedPoints.get(i)[0] + 10f),
                        (float) (rotatedPoints.get(i)[1] - 10f),
                        paint
                );
            }

            // Costruisci una lista di coordinate 2D per la triangolazione
            List<double[]> coordList = new ArrayList<>();
            for (double[] punto : rotatedPoints) {
                // Usiamo solo x ed y; la terza componente non è necessaria per il disegno 2D
                coordList.add(new double[]{punto[0], punto[1]});
            }

            // Esegui la triangolazione Delaunay (in 2D)
            List<int[]> delaunayTriangles = performDelaunayTriangulation(coordList);

            // Per debug: logga il numero di triangoli trovati
            //Log.d("Delaunay", "Numero di triangoli: " + delaunayTriangles.size());

            // Imposta il Paint per disegnare le linee
            paint.setColor(MyColorClass.colorConstraint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((float) (2 / DataSaved.scale_Factor3D));

            // Itera sui triangoli e disegna i lati
            for (int[] triangle : delaunayTriangles) {
                // Recupera le coordinate dei vertici dai rotatedPoints
                double[] p1 = rotatedPoints.get(triangle[0]);
                double[] p2 = rotatedPoints.get(triangle[1]);
                double[] p3 = rotatedPoints.get(triangle[2]);

                // Disegna i tre lati del triangolo
                canvas.drawLine(
                        (float) p1[0], (float) p1[1],
                        (float) p2[0], (float) p2[1],
                        paint
                );
                canvas.drawLine(
                        (float) p2[0], (float) p2[1],
                        (float) p3[0], (float) p3[1],
                        paint
                );
                canvas.drawLine(
                        (float) p3[0], (float) p3[1],
                        (float) p1[0], (float) p1[1],
                        paint
                );
            }
        }

        // Se c'è un punto selezionato, disegna un indicatore
        try {
            if (puntoselezionato != null) {
                if (Activity_Crea_Superficie.indexSel > -1 && point3DS.length > 1) {
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2f);
                    canvas.drawCircle(
                            (float) puntoselezionato[0],
                            (float) puntoselezionato[1],
                            (float) (8 / DataSaved.scale_Factor3D),
                            paint
                    );
                    /*paint.setColor(Color.BLUE);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(
                            (float) puntoselezionato[0],
                            (float) puntoselezionato[1],
                            (float) (8 / DataSaved.scale_Factor3D),
                            paint
                    );*/
                }
            }
        } catch (Exception ignored) {
        }
    }

    private List<int[]> performDelaunayTriangulation(List<double[]> coordinates) {
        List<int[]> triangles = new ArrayList<>();
        GeometryFactory geometryFactory = new GeometryFactory();

        // Se ci sono meno di 3 punti, non eseguire triangolazione
        if (coordinates.size() < 3) {
            return triangles;
        }

        // Crea un array di Coordinate usando solo x e y
        Coordinate[] coordsArray = new Coordinate[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            double[] pt = coordinates.get(i);
            coordsArray[i] = new Coordinate(pt[0], pt[1]);
        }

        // Crea la triangolazione Delaunay in 2D
        DelaunayTriangulationBuilder dtBuilder = new DelaunayTriangulationBuilder();
        dtBuilder.setSites(geometryFactory.createMultiPointFromCoords(coordsArray));
        Geometry triangulation = dtBuilder.getTriangles(geometryFactory);

        // Imposta una tolleranza per confrontare le coordinate
        // Il valore tol va scelto in base alla scala dei tuoi dati (es. 1.0 se usi coordinate in pixel)
        double tol = 1.0;

        // Itera sulle geometrie (triangoli) ottenute
        int numTriangles = triangulation.getNumGeometries();
        for (int i = 0; i < numTriangles; i++) {
            Polygon trianglePoly = (Polygon) triangulation.getGeometryN(i);
            Coordinate[] triCoords = trianglePoly.getCoordinates();
            // Il poligono triangolo contiene 4 coordinate (l'ultima è uguale alla prima)
            if (triCoords.length < 4) continue;

            int[] triIndices = new int[3];
            // Per ciascuno dei primi 3 vertici, trova l'indice corrispondente in coordsArray
            for (int j = 0; j < 3; j++) {
                boolean found = false;
                for (int k = 0; k < coordsArray.length; k++) {
                    if (triCoords[j].distance(coordsArray[k]) < tol) {
                        triIndices[j] = k;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    triIndices[j] = -1;
                }
            }
            // Aggiungi il triangolo solo se tutti gli indici sono validi
            if (triIndices[0] != -1 && triIndices[1] != -1 && triIndices[2] != -1) {
                triangles.add(triIndices);
            }
        }

        return triangles;
    }

    private void updatePoly(double heading,int indice){
        paint.setColor(MyColorClass.colorPoint);
        // Coordinate reali del primo punto (Nord Est) e coordinate display
        double buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
        double buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto

        rotatedPoints = new ArrayList<>();
        for (int i = 0; i < Activity_Crea_Superficie.point3DS.length; i++) {


            double diff_x = (Activity_Crea_Superficie.point3DS[i].getX() - buckEst) * scala;
            double diff_y = (Activity_Crea_Superficie.point3DS[i].getY() - buckNord) * scala;
            float bucketX = (float) ((((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));
            float bucketY = left_top_bucket.y;
            rotatedPoints.add(
                    new double[]{
                            (float) (bucketX + diff_x * Math.cos(Math.toRadians(heading)) - diff_y * Math.sin(Math.toRadians(heading))),
                            (float) (bucketY - diff_x * Math.sin(Math.toRadians(heading)) - diff_y * Math.cos(Math.toRadians(heading)))
                    });

        }
        try {
            double diff_x_s = (Activity_Crea_Superficie.point3DS[indice].getX() - buckEst) * scala;
            double diff_y_s = (Activity_Crea_Superficie.point3DS[indice].getY() - buckNord) * scala;
            puntoselezionato = new double[]{

                    (float) (bucketX + diff_x_s * Math.cos(Math.toRadians(heading)) - diff_y_s * Math.sin(Math.toRadians(heading))),
                    (float) (bucketY - diff_x_s * Math.sin(Math.toRadians(heading)) - diff_y_s * Math.cos(Math.toRadians(heading)))
            };
        } catch (Exception ignored) {

        }
    }

    private void drawPolyTrench(Canvas canvas,Paint paint,double heading){

        text = "";
        // Se ci sono meno di 2 punti, disegna solo i punti e le etichette
        if (rotatedPoints.isEmpty() || rotatedPoints.size() < 2) {
            for (int i = 0; i < rotatedPoints.size(); i++) {
                paint.setColor(MyColorClass.colorPoint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(
                        (float) rotatedPoints.get(i)[0],
                        (float) rotatedPoints.get(i)[1],
                        (float) (6 / DataSaved.scale_Factor3D),
                        paint
                );
                paint.setColor(MyColorClass.colorLabel);
                paint.setTextSize((float) (20 / DataSaved.scale_Factor3D));
                text = point3DS[i].getName();

                if (text == null || text.isEmpty() || text.equals(" ") || text.equals("")) {
                    text = point3DS[i].getId();
                }
                Log.d("Testo", text);
                canvas.drawText(text
                        ,
                        (float) (rotatedPoints.get(i)[0] + 10f),
                        (float) (rotatedPoints.get(i)[1] - 10f),
                        paint
                );
            }
        }
        // Disegna la polilinea se ci sono almeno 2 punti
        if (rotatedPoints.size() >= 2) {
            paint.setColor(MyColorClass.colorPoly); // Imposta il colore della linea
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((float) (2 / DataSaved.scale_Factor3D)); // Spessore adattato alla scala

            for (int i = 0; i < rotatedPoints.size() - 1; i++) {
                float startX = (float) rotatedPoints.get(i)[0];
                float startY = (float) rotatedPoints.get(i)[1];
                float endX = (float) rotatedPoints.get(i + 1)[0];
                float endY = (float) rotatedPoints.get(i + 1)[1];

                canvas.drawLine(startX, startY, endX, endY, paint);
            }
        }


        // Disegna i punti e le etichette
        for (int i = 0; i < rotatedPoints.size(); i++) {
            // Punto
            paint.setColor(MyColorClass.colorPoint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(
                    (float) rotatedPoints.get(i)[0],
                    (float) rotatedPoints.get(i)[1],
                    (float) (6 / DataSaved.scale_Factor3D),
                    paint
            );

            // Etichetta
            paint.setColor(MyColorClass.colorLabel);
            paint.setTextSize((float) (20 / DataSaved.scale_Factor3D));
            text = point3DS[i].getName();

            if (text == null || text.isEmpty() || text.trim().isEmpty()) {
                text = point3DS[i].getId();
            }

            canvas.drawText(
                    text,
                    (float) (rotatedPoints.get(i)[0] + 10f),
                    (float) (rotatedPoints.get(i)[1] - 10f),
                    paint
            );
        }
        //disegna qui la superficie attorno alla polilinea
// Conversione pendenze in radianti
        double leftAngleRad = Math.toRadians(leftS_d);
        double rightAngleRad = Math.toRadians(rightS_d);

// Controlla che abbiamo almeno 2 punti validi
        if (point3DS == null || point3DS.length < 2 || rotatedPoints.size() != point3DS.length) return;

// Collezioni di punti laterali
        List<float[]> leftSide = new ArrayList<>();
        List<float[]> rightSide = new ArrayList<>();

        for (int i = 0; i < point3DS.length; i++) {
            // Coord 2D disegnate
            float cx = (float) rotatedPoints.get(i)[0];
            float cy = (float) rotatedPoints.get(i)[1];

            // Coord spaziali originali
            double x = point3DS[i].getX();
            double y = point3DS[i].getY();
            double z = point3DS[i].getZ();

            // Calcola direzione della tangente alla polilinea (tra i-1 e i+1)
            double dx, dy;
            if (i == 0) {
                dx = point3DS[i+1].getX() - x;
                dy = point3DS[i+1].getY() - y;
            } else if (i == point3DS.length - 1) {
                dx = x - point3DS[i-1].getX();
                dy = y - point3DS[i-1].getY();
            } else {
                dx = point3DS[i+1].getX() - point3DS[i-1].getX();
                dy = point3DS[i+1].getY() - point3DS[i-1].getY();
            }

            // Normalizzazione direzione
            double length = Math.sqrt(dx*dx + dy*dy);
            if (length == 0) continue;
            dx /= length;
            dy /= length;

            // Offset laterale (world coordinates)
            double ox = -dy;
            double oy = dx;

// Punto di partenza (world)
            double px = point3DS[i].getX();
            double py = point3DS[i].getY();
            double pz = point3DS[i].getZ();

// LEFT offset world-space
            double lwx = px + ox * leftW_d;
            double lwy = py + oy * leftW_d;
            double lwz = pz - Math.tan(leftAngleRad) * leftW_d;

// RIGHT offset world-space
            double rwx = px - ox * rightW_d;
            double rwy = py - oy * rightW_d;
            double rwz = pz - Math.tan(rightAngleRad) * rightW_d;

// Differenze rispetto al bucket
            double diff_lx = (lwx - buckEst) * scala;
            double diff_ly = (lwy - buckNord) * scala;

            double diff_rx = (rwx - buckEst) * scala;
            double diff_ry = (rwy - buckNord) * scala;

// Coordinate canvas ruotate come in updatePoly
            float lx = (float)(bucketX + diff_lx * Math.cos(Math.toRadians(heading)) - diff_ly * Math.sin(Math.toRadians(heading)));
            float ly = (float)(bucketY - diff_lx * Math.sin(Math.toRadians(heading)) - diff_ly * Math.cos(Math.toRadians(heading)));

            float rx = (float)(bucketX + diff_rx * Math.cos(Math.toRadians(heading)) - diff_ry * Math.sin(Math.toRadians(heading)));
            float ry = (float)(bucketY - diff_rx * Math.sin(Math.toRadians(heading)) - diff_ry * Math.cos(Math.toRadians(heading)));

            leftSide.add(new float[]{lx, ly});
            rightSide.add(new float[]{rx, ry});

        }

// Disegna le superfici laterali come poligoni
        paint.setColor(Color.argb(75, 150, 150, 255)); // azzurrino trasparente
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < leftSide.size() - 1; i++) {
            Path path = new Path();
            float[] l0 = leftSide.get(i);
            float[] l1 = leftSide.get(i+1);
            double[] c0 = rotatedPoints.get(i);
            double[] c1 = rotatedPoints.get(i+1);

            path.moveTo(l0[0], l0[1]);
            path.lineTo((float) c0[0], (float) c0[1]);
            path.lineTo((float) c1[0], (float) c1[1]);
            path.lineTo(l1[0], l1[1]);
            path.close();

            canvas.drawPath(path, paint);
        }

        for (int i = 0; i < rightSide.size() - 1; i++) {
            Path path = new Path();
            float[] r0 = rightSide.get(i);
            float[] r1 = rightSide.get(i+1);
            double[] c0 = rotatedPoints.get(i);
            double[] c1 = rotatedPoints.get(i+1);

            path.moveTo(r0[0], r0[1]);
            path.lineTo((float) c0[0], (float) c0[1]);
            path.lineTo((float) c1[0], (float) c1[1]);
            path.lineTo(r1[0], r1[1]);
            path.close();

            canvas.drawPath(path, paint);
        }
        //popola public static List<Face3D>facceTrenc;
        //popola  public static Polyline polyTrench;



    }
    public static void buildTrenchEntities(
            List<Point3D> polylinePoints,
            double distSx, double distDx,
            double slopeSxDeg, double slopeDxDeg,
            int faceColor, int lineColor,
            Layer faceLayer, Layer polylineLayer
    ) {
        facceTrench.clear();

        List<Point3D> leftPoints  = new ArrayList<>();
        List<Point3D> rightPoints = new ArrayList<>();

        double slopeSxRad = Math.toRadians(slopeSxDeg);
        double slopeDxRad = Math.toRadians(slopeDxDeg);

        // 1) Calcola le tangenti smussate
        List<Vector2D> tangents = new ArrayList<>();
        for (int i = 0; i < polylinePoints.size(); i++) {
            Vector2D dirPrev, dirNext;
            if (i == 0) {
                dirPrev = new Vector2D(polylinePoints.get(0), polylinePoints.get(1));
                dirNext = dirPrev;
            } else if (i == polylinePoints.size() - 1) {
                dirPrev = new Vector2D(polylinePoints.get(i - 1), polylinePoints.get(i));
                dirNext = dirPrev;
            } else {
                dirPrev = new Vector2D(polylinePoints.get(i - 1), polylinePoints.get(i));
                dirNext = new Vector2D(polylinePoints.get(i), polylinePoints.get(i + 1));
            }

            // media direzioni con normalizzazione → transizione morbida nelle curve
            Vector2D dir = dirPrev.add(dirNext).normalize();
            tangents.add(dir);
        }

        // 2) Normali XY e punti offset + pendenza verticale
        for (int i = 0; i < polylinePoints.size(); i++) {
            Point3D c = polylinePoints.get(i);
            Vector2D t = tangents.get(i);
            Vector2D n = t.getNormal().normalize(); // normale media smussata

            // ΔZ per pendenza laterale
            double dzSx = Math.tan(slopeSxRad) * distSx;
            double dzDx = Math.tan(slopeDxRad) * distDx;

            // punto sinistro e destro rispetto al centro
            Point3D pSx = new Point3D(
                    c.getX() + n.x * distSx,
                    c.getY() + n.y * distSx,
                    c.getZ() - dzSx
            );

            Point3D pDx = new Point3D(
                    c.getX() - n.x * distDx,
                    c.getY() - n.y * distDx,
                    c.getZ() - dzDx
            );

            leftPoints.add(pSx);
            rightPoints.add(pDx);
        }

        // 3) Generazione superfici
        for (int i = 0; i < polylinePoints.size() - 1; i++) {
            Point3D c1 = polylinePoints.get(i);
            Point3D c2 = polylinePoints.get(i + 1);

            Point3D l1 = leftPoints.get(i);
            Point3D l2 = leftPoints.get(i + 1);

            Point3D r1 = rightPoints.get(i);
            Point3D r2 = rightPoints.get(i + 1);

            // sinistra
            facceTrench.add(new Face3D(c1, c2, l2, l2, faceColor, faceLayer));
            facceTrench.add(new Face3D(c1, l2, l1, l1, faceColor, faceLayer));
            // destra
            facceTrench.add(new Face3D(c1, r2, c2, c2, faceColor, faceLayer));
            facceTrench.add(new Face3D(c1, r1, r2, r2, faceColor, faceLayer));
        }

        // 4) polyline centrale
        Polyline centerPolyline = new Polyline(polylinePoints, polylineLayer);
        centerPolyline.setLineColor(lineColor);
        polyTrench = centerPolyline;
    }

/*
    public static void buildTrenchEntities(
            List<Point3D> polylinePoints,
            double distSx, double distDx,
            double slopeSxDeg, double slopeDxDeg,
            int faceColor, int lineColor,
            Layer faceLayer, Layer polylineLayer
    ) {
        List<Point3D> leftPoints = new ArrayList<>();
        List<Point3D> rightPoints = new ArrayList<>();

        double slopeSxRad = Math.toRadians(slopeSxDeg);
        double slopeDxRad = Math.toRadians(slopeDxDeg);

        for (int i = 0; i < polylinePoints.size(); i++) {
            Point3D current = polylinePoints.get(i);
            Vector2D direction;

            if (i == 0) {
                direction = new Vector2D(polylinePoints.get(i), polylinePoints.get(i + 1));
            } else if (i == polylinePoints.size() - 1) {
                direction = new Vector2D(polylinePoints.get(i - 1), polylinePoints.get(i));
            } else {
                Vector2D dir1 = new Vector2D(polylinePoints.get(i - 1), current);
                Vector2D dir2 = new Vector2D(current, polylinePoints.get(i + 1));
                direction = dir1.add(dir2).normalize();
            }

            Vector2D normal = direction.getNormal(); // normale XY unitaria

            // Punto sinistro
            double dzSx = Math.tan(slopeSxRad) * distSx;
            Point3D left = new Point3D(
                    current.getX() + normal.x * distSx,
                    current.getY() + normal.y * distSx,
                    current.getZ() - dzSx
            );

            // Punto destro
            double dzDx = Math.tan(slopeDxRad) * distDx;
            Point3D right = new Point3D(
                    current.getX() - normal.x * distDx,
                    current.getY() - normal.y * distDx,
                    current.getZ() - dzDx
            );

            leftPoints.add(left);
            rightPoints.add(right);
        }

        // Lati sinistro e destro
        for (int i = 0; i < polylinePoints.size() - 1; i++) {
            Point3D c1 = polylinePoints.get(i);
            Point3D c2 = polylinePoints.get(i + 1);

            Point3D l1 = leftPoints.get(i);
            Point3D l2 = leftPoints.get(i + 1);

            Point3D r1 = rightPoints.get(i);
            Point3D r2 = rightPoints.get(i + 1);

            // Superficie sinistra (in due triangoli)
            facceTrench.add(new Face3D(c1, c2, l2,l2, faceColor, faceLayer));
            facceTrench.add(new Face3D(c1, l2, l1,l1, faceColor, faceLayer));

// Superficie destra (in due triangoli)
            facceTrench.add(new Face3D(c1, r2, c2,c2, faceColor, faceLayer));
            facceTrench.add(new Face3D(c1, r1, r2,r2, faceColor, faceLayer));
        }

        // Polilinea centrale
        Polyline centerPolyline = new Polyline(polylinePoints, polylineLayer);
        centerPolyline.setLineColor(lineColor);
        polyTrench=(centerPolyline);
    }
*/










}
