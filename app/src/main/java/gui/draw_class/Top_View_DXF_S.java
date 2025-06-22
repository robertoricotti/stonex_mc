package gui.draw_class;

import static gui.draw_class.Top_View_DXF.offsetY;
import static gui.draw_class.Top_View_DXF.offsetX;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.List;

import dxf.Arc;
import dxf.AutoCADColor;
import dxf.Circle;
import dxf.Draw2DPolyline;
import dxf.Draw3DFace;
import dxf.Draw3DPolyline;
import dxf.DrawArcs;
import dxf.DrawCircles;
import dxf.DrawDXFPoint;
import dxf.DrawDXFText;
import dxf.DrawLines;
import dxf.DrawSelectedPolyline;
import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Line;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Polyline_2D;
import dxf.Segment;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.exca.Sensors_Decoder;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import packexcalib.surfcreator.DistToPoint;

public class Top_View_DXF_S extends SurfaceView implements SurfaceHolder.Callback{
    float cingoliWidth;
    float cingoliHeight;
    List<PointF> arcPoints;
    List<List<PointF>> arcSegments;
    // Lista per memorizzare i dati dei segmenti
    List<float[]> segmentData;
    int coloreSuperficie;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
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

    public float scala = 35;
    double w_bucket;
    double d_stick;
    double l_bucket;
    double distanzaPosteriore;
    double lcarro;
    double cingoliLength;
    double stickWidth;
    double distStick;
    double bucketWidth;
    double buckNord;
    double buckEst;
    double rotationAngle;
    float bucketX;
    float bucketY;
    float ancorPX, ancorPY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    float inizioX, inizioY;
    float mRoll;
    boolean isXML, isXMLLyne;


    private RenderThread renderThread;
    private Canvas canvas;
    public Top_View_DXF_S(Context context, AttributeSet attrs) {
        super(context,attrs);
        getHolder().addCallback(this);
        setFocusable(true);

        // inizializza gestori touch, zoom, gesture ecc.
        init(context);
    }
    private void init(Context context) {



        if (DataSaved.scale_Factor3D == 0) {
            DataSaved.scale_Factor3D = 1f;
        }
        arcPoints = new ArrayList<>(); // Per punti ruotati e scalati
        arcSegments = new ArrayList<>();
        segmentData = new ArrayList<>();
        paint = new Paint();
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        if (offsetY == 0) {
            offsetY = 100;
        }


        bucketWidth = DataSaved.W_Bucket * scala;

        if (DataSaved.isTiltRotator) {
            mRoll = 0;
        } else {
            mRoll = (float) (DataSaved.L_Roll * scala);
        }
        try {
            isXML = DataSaved.progettoSelected.substring(DataSaved.progettoSelected.lastIndexOf(".") + 1).equalsIgnoreCase("xml");

        } catch (Exception e) {
            isXML = false;
        }
        try {
            isXMLLyne = DataSaved.progettoSelected_POLY.substring(DataSaved.progettoSelected_POLY.lastIndexOf(".") + 1).equalsIgnoreCase("xml");

        } catch (Exception e) {
            isXMLLyne = false;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        renderThread = new RenderThread(holder);
        renderThread.setRunning(true);
        renderThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (renderThread != null) {
            renderThread.setRunning(false);
            try {
                renderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // opzionale
    }
    private void drawInternal(Canvas canvas){

        this.canvas = canvas;

        paint.setAntiAlias(true);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        try {
            rotationAngle = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
            l_bucket = DataSaved.L_Bucket;
            w_bucket = DataSaved.W_Bucket;
            d_stick = DataSaved.L_Stick + l_bucket;
            originPointCarro = new PointF(getWidth() * 0.5f, getHeight() * 0.85f);
            canvas.save();
            canvas.scale((float) DataSaved.scale_Factor3D, (float) DataSaved.scale_Factor3D, getWidth() * 0.5f, getHeight() * 0.85f);
            canvas.translate(offsetX, offsetY);
            initEscavatore();
            double dist = new DistToPoint(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0, ExcavatorLib.coordPivoTilt[0], ExcavatorLib.coordPivoTilt[1], 0).getDist_to_point();
            double bucketHeight = dist * scala;

            double bucketHeightFake = 0.5 * scala;

            if (ExcavatorLib.correctBucket < -90 || ExcavatorLib.correctBucket > 90) {
                bucketHeight = bucketHeight * -1;
            }
            distStick = (new DistToPoint(ExcavatorLib.coordPitch[0], ExcavatorLib.coordPitch[1], 0, ExcavatorLib.coordPivoTilt[0], ExcavatorLib.coordPivoTilt[1], 0).getDist_to_point() * scala) + 40f;
            originPointBucket = new PointF(originPointStick.x, originPointStick.y - (float) distStick);
            stickWidth = scala * 0.30f;
            left_top_bucket = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
            left_top_bucket2 = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeightFake);
            right_bottom_bucket = new PointF(originPointBucket.x + (float) bucketWidth / 2f, originPointBucket.y);

            buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
            buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto

            bucketX = (float) ((((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));
            bucketY = left_top_bucket.y;

            drawDXFElements(buckEst, buckNord);
            drawBenna(canvas);

            float stopX = (float) ((left_top_bucket.x + (mRoll) + right_bottom_bucket.x + (mRoll)) / 2f);
            float stopY = left_top_bucket.y;
            float stopXLeft = (float) (left_top_bucket.x + (mRoll));
            float stopXRight = (float) (right_bottom_bucket.x + (mRoll));
            drawStick(canvas);

            paint.setColor(Color.BLUE);
            paint.setStrokeWidth((float) (0.1*scala));
            switch (DataSaved.bucketEdge) {
                case -1:
                    canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    canvas.drawCircle(stopXLeft, stopY, 3.5f, paint);
                    canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    if (DataSaved.isAutoSnap == 1 || DataSaved.isAutoSnap == 3) {
                        drawNearestPoint(DataSaved.nearestPoint, stopXLeft, stopY);
                    } else if (DataSaved.isAutoSnap == 2 || DataSaved.isAutoSnap == 4) {
                        drawNearestSegmentAndLine();
                    }
                    break;
                case 0:
                    canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    canvas.drawCircle(stopX, stopY, 3.5f, paint);
                    inizioX = stopX;
                    inizioY = stopY;
                    canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    if (DataSaved.isAutoSnap == 1 || DataSaved.isAutoSnap == 3) {
                        drawNearestPoint(DataSaved.nearestPoint, stopX, stopY);

                    } else if (DataSaved.isAutoSnap == 2 || DataSaved.isAutoSnap == 4) {
                        drawNearestSegmentAndLine();
                    }
                    break;
                case 1:
                    canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    canvas.drawCircle(stopXRight, stopY, 3.5f, paint);
                    canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    if (DataSaved.isAutoSnap == 1 || DataSaved.isAutoSnap == 3) {
                        drawNearestPoint(DataSaved.nearestPoint, stopXRight, stopY);

                    } else if (DataSaved.isAutoSnap == 2 || DataSaved.isAutoSnap == 4) {
                        drawNearestSegmentAndLine();
                    }
                    break;
            }
            drawAntenna1(canvas,paint);

            canvas.restore();


        } catch (Exception e) {
            //Log.d("ErrorDXFWRITE", e.toString());
            e.printStackTrace();
        }
    }

    public void requestRedraw() {
        if (renderThread != null) {
            renderThread.requestRedraw();
        }
    }
    private class RenderThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private boolean running = false;
        private boolean needRedraw = true;

        public RenderThread(SurfaceHolder holder) {
            this.surfaceHolder = holder;
        }

        public void setRunning(boolean run) {
            this.running = run;
        }

        public void requestRedraw() {
            this.needRedraw = true;
        }

        @Override
        public void run() {
            while (running) {
                if (!needRedraw) continue;
                needRedraw = false;

                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            drawInternal(canvas);
                        }
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void drawDXFElements(double bucketEst, double bucketNord) {
        try {

            if (isXML) {
                for (Face3D face : DataSaved.filteredFaces) {

                    // Cambia il tipo di lista in List<Face3D>
                    if (isLayerEnabled(face.getLayer().getLayerName())) {
                        Draw3DFace.draw(paint,
                                canvas,
                                face.toArrayWithCentroid(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                myParseColor(AutoCADColor.getColor(String.valueOf(face.getLayer().getColorState()))),
                                scala,
                                rotationAngle,
                                DataSaved.Triangoli_Surf!=0,
                                DataSaved.Colore_Surf == 2
                        );
                    }
                }

            } else {
                for (Face3D face : DataSaved.filteredFaces) {

                    if (isLayerEnabled(face.getLayer().getLayerName())) {
                        Draw3DFace.draw(paint,
                                canvas,
                                face.toArrayWithCentroid(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                myParseColor(face.getLayer().getColorState()),
                                scala,
                                rotationAngle,
                                DataSaved.Triangoli_Surf != 0,
                                DataSaved.Colore_Surf == 2
                        );
                    }

                }
            }
        } catch (Exception e) {
            Log.d("expRT", "excFace");
        }

        try {
            if (false) {//isXMLLyne
                for (Polyline polyline : DataSaved.polylines) {
                    if (isLayerEnabled(polyline.getLayer().getLayerName())) { // Controlla se il layer è abilitato
                        Draw3DPolyline.draw(paint,
                                canvas,
                                polyline.getVertices(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                myParseColor(AutoCADColor.getColor(String.valueOf(polyline.getLayer().getColorState()))),
                                scala,
                                rotationAngle,
                                polyline
                        );
                    }
                }
                if (DataSaved.isAutoSnap == 2 && DataSaved.selectedPoly != null
                        && DataSaved.selectedPoly.getLayer() != null && DataSaved.selectedPoly.getLayer().isEnable()) {
                    DrawSelectedPolyline.draw(canvas,
                            paint,
                            DataSaved.selectedPoly.getVertices(),
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            myParseColor( DataSaved.selectedPoly.getLineColor()),
                            scala,
                            rotationAngle
                    );                }
            } else {

                for (Polyline polyline : DataSaved.polylines) {


                    if (isLayerEnabled(polyline.getLayer().getLayerName())) { // Controlla se il layer è abilitato

                        Draw3DPolyline.draw(paint,
                                canvas,
                                polyline.getVertices(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                myParseColor(polyline.getLineColor()),
                                scala,
                                rotationAngle,
                                polyline
                        );

                    }

                }
                if (DataSaved.isAutoSnap == 2 && DataSaved.selectedPoly != null
                        && DataSaved.selectedPoly.getLayer() != null && DataSaved.selectedPoly.getLayer().isEnable()) {
                    DrawSelectedPolyline.draw(canvas,
                            paint,
                            DataSaved.selectedPoly.getVertices(),
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            myParseColor(DataSaved.selectedPoly.getLineColor()),
                            scala,
                            rotationAngle
                    );
                    if (DataSaved.line_Offset != 0) {
                        DrawSelectedPolyline.draw(canvas,
                                paint,
                                DataSaved.selectedPoly_OFFSET.getVertices(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                MyColorClass.colorConstraint,
                                scala,
                                rotationAngle
                        );


                    }
                }
            }

        } catch (Exception e) {
            Log.d("expRT", "excPoly");
        }

        try {

            if (DataSaved.Punti_Surf == 1) {
                for (Point3D point : DataSaved.filteredPoints) {
                    if (isLayerEnabled(point.getLayer().getLayerName())) { // Controlla se il layer è abilitato
                        DrawDXFPoint.draw(canvas,
                                paint,
                                point,
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                scala,
                                myParseColor(Color.WHITE),
                                rotationAngle
                        );
                    }
                }
            }
        } catch (Exception e) {

            Log.d("expRT", "excPT");
        }

        try {
            if (DataSaved.ShowText == 1) {
                for (DxfText dxfText : DataSaved.filteredDxfTexts) {
                    if (isLayerEnabled(dxfText.getLayer().getLayerName())) { // Controlla se il layer è abilitato
                        DrawDXFText.draw(canvas,paint,dxfText,bucketX,bucketY,bucketEst,bucketNord,scala,myParseColor(MyColorClass.colorLabel),rotationAngle);
                    }
                }
            }
        } catch (Exception e) {
            Log.d("expRT", "excTxT");
        }
        try {


            for (Polyline_2D polyline_2D : DataSaved.polylines_2D) {
                if (isLayerEnabled(polyline_2D.getLayer().getLayerName())) {
                    Draw2DPolyline.draw(paint,
                            canvas,
                            polyline_2D.getVertices(),
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            myParseColor(polyline_2D.getLineColor()),
                            scala,
                            rotationAngle,
                            DataSaved.scale_Factor3D
                    );
                }

            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("expRT", "excPoly2D");
        }
        try {


            for (Arc arc : DataSaved.arcs) {
                if (isLayerEnabled(arc.getLayer().getLayerName())) {
                    DrawArcs.draw(canvas,
                            paint,
                            arc,
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            scala,
                            myParseColor(arc.getColor()),
                            ((NmeaListener.mch_Orientation + DataSaved.deltaGPS2) % 360));
                }

            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("expRT", "arcs");
        }
        try {


            for (Line line : DataSaved.lines_2D) {
                if (isLayerEnabled(line.getLayer().getLayerName())) {
                    DrawLines.draw(canvas,
                            paint,
                            line,
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            scala,
                            myParseColor(line.getColor()),
                            rotationAngle);
                }

            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("expRT", "lines");
        }
        try {


            for (Circle circle : DataSaved.circles) {
                if (isLayerEnabled(circle.getLayer().getLayerName())) {
                    DrawCircles.draw(canvas,
                            paint,
                            circle,
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            scala,
                            myParseColor(circle.getColor()),
                            rotationAngle);
                }

            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("expRT", "circles");
        }



    }


    private void drawNearestPoint(Point3D point, float mX, float mY) {
        double diffX = (point.getX() - ExcavatorLib.bucketCoord[0]) * scala;
        double diffY = (point.getY() - ExcavatorLib.bucketCoord[1]) * scala;
        float rotatedXV1 = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
        float rotatedYV1 = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
        float rotatedX = (float) (ancorPX + (mX - ancorPX) * Math.cos(Math.toRadians(ExcavatorLib.yawSensor)) - (mY - ancorPY) * Math.sin(Math.toRadians(ExcavatorLib.yawSensor)));
        float rotatedY = (float) (ancorPY + (mX - ancorPX) * Math.sin(Math.toRadians(ExcavatorLib.yawSensor)) + (mY - ancorPY) * Math.cos(Math.toRadians(ExcavatorLib.yawSensor)));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.bg_sfsgreen));
        canvas.drawCircle(rotatedX, rotatedY, 6f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(MyColorClass.colorConstraint);
        canvas.drawCircle(rotatedXV1, rotatedYV1, 8f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.green));
        canvas.drawCircle(rotatedXV1, rotatedYV1, 7f, paint);
        paint.setStrokeWidth(3f);
        canvas.drawLine(rotatedXV1, rotatedYV1, rotatedX, rotatedY, paint);
    }

    private void drawPolylineJson(List<Point3D> vertices, int color, double bucketEst, double bucketNord) {
        if (vertices.size() < 2) return; // Una polilinea ha bisogno di almeno 2 punti

        // Angolo di rotazione basato sui dati ricevuti


        // Calcola il centro del secchio


        // Inizializza il Path
        Path path = new Path();
        path.reset();

        // Calcola e sposta il Path al primo punto
        double diffX = (vertices.get(0).getX() - bucketEst) * scala;
        double diffY = (vertices.get(0).getY() - bucketNord) * scala;
        float rotatedX = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
        float rotatedY = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
        path.moveTo(rotatedX, rotatedY);

        // Calcola e traccia i punti successivi
        for (int i = 1; i < vertices.size(); i++) {
            diffX = (vertices.get(i).getX() - bucketEst) * scala;
            diffY = (vertices.get(i).getY() - bucketNord) * scala;
            rotatedX = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
            rotatedY = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
            path.lineTo(rotatedX, rotatedY);
        }

        // Imposta le proprietà del paint
        paint.setStrokeWidth((float) (1.5 / DataSaved.scale_Factor3D));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(myParseColor(color));

        // Disegna il Path
        canvas.drawPath(path, paint);
    }

    private void drawNearestSegmentAndLine() {

        Segment closestSegment = DataSaved.nearestSegment;

        if (closestSegment != null) {
            // Disegna il segmento più vicino di verde
            drawSegment(closestSegment);


        }
    }
    private void drawLineDist(float aX, float aY, float bX, float bY) {
        float pX = 0, pY = 0;
        switch (DataSaved.bucketEdge) {
            case -1:
                pX = (float) (left_top_bucket.x + mRoll);
                pY = left_top_bucket.y;
                break;
            case 0:
                pX = ((float) (right_bottom_bucket.x + mRoll) + (float) (left_top_bucket.x + mRoll)) / 2f;
                pY = left_top_bucket.y;
                break;
            case 1:
                pX = (float) (right_bottom_bucket.x + mRoll);
                pY = left_top_bucket.y;
                break;
        }

        float pXR = (float) (ancorPX + (pX - ancorPX) * Math.cos(Math.toRadians(ExcavatorLib.yawSensor)) -
                (pY - ancorPY) * Math.sin(Math.toRadians(ExcavatorLib.yawSensor)));
        float pYR = (float) (ancorPY + (pX - ancorPX) * Math.sin(Math.toRadians(ExcavatorLib.yawSensor)) +
                (pY - ancorPY) * Math.cos(Math.toRadians(ExcavatorLib.yawSensor)));

        float dx = bX - aX;
        float dy = bY - aY;
        float lengthSq = dx * dx + dy * dy;

        if (lengthSq == 0) return;

        float APx = pXR - aX;
        float APy = pYR - aY;
        float t = (APx * dx + APy * dy) / lengthSq;
        t = clamp(t, 0f, 1f);

        float intersectionX = aX + t * dx;
        float intersectionY = aY + t * dy;

        canvas.drawLine(pXR, pYR, intersectionX, intersectionY, paint);

        Top_View_DXF.giroFrecciaExca = My_LocationCalc.calcBearingXY(intersectionX, intersectionY, pXR, pYR);
        Top_View_DXF.giroFrecciaExca = 360 - Top_View_DXF.giroFrecciaExca;
    }

    /*private void drawLineDist(float aX, float aY, float bX, float bY) {

        float pX = 0, pY = 0;
        switch (DataSaved.bucketEdge) {
            case -1:
                pX = (float) (left_top_bucket.x + mRoll);
                pY = left_top_bucket.y;
                break;
            case 0:
                pX = ((float) (right_bottom_bucket.x + mRoll) + (float) (left_top_bucket.x + mRoll)) / 2f;
                pY = left_top_bucket.y;
                break;
            case 1:
                pX = (float) (right_bottom_bucket.x + mRoll);
                pY = left_top_bucket.y;
                break;
        }
        float pXR = (float) (ancorPX + (pX - ancorPX) * Math.cos(Math.toRadians(ExcavatorLib.yawSensor)) - (pY - ancorPY) * Math.sin(Math.toRadians(ExcavatorLib.yawSensor)));
        float pYR = (float) (ancorPY + (pX - ancorPX) * Math.sin(Math.toRadians(ExcavatorLib.yawSensor)) + (pY - ancorPY) * Math.cos(Math.toRadians(ExcavatorLib.yawSensor)));
        // Calcola il coefficiente angolare della linea AB
        float mAB = (bY - aY) / (bX - aX);

        // Calcola il coefficiente angolare della retta perpendicolare
        float mPerpendicular = -1 / mAB;

        // Calcola l'ordinata all'origine (intercetta) della retta perpendicolare
        float bPerpendicular = pYR - mPerpendicular * pXR;

        // Calcola le coordinate dell'intersezione tra le due linee
        float intersectionX = (bPerpendicular - aY + mAB * aX) / (mAB - mPerpendicular);
        float intersectionY = mAB * (intersectionX - aX) + aY;

        // Limita l'intersezione ai limiti di AB
        intersectionX = clamp(intersectionX, Math.min(aX, bX), Math.max(aX, bX));
        intersectionY = clamp(intersectionY, Math.min(aY, bY), Math.max(aY, bY));

        // Disegna la linea da P all'intersezione

        canvas.drawLine(pXR, pYR, intersectionX, intersectionY, paint);

        Top_View_DXF.giroFrecciaExca = My_LocationCalc.calcBearingXY(intersectionX, intersectionY, pXR, pYR);
        Top_View_DXF.giroFrecciaExca = 360 - Top_View_DXF.giroFrecciaExca;
    }*/

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private void drawSegment(Segment segment) {
        // Calcola la rotazione e le coordinate per disegnare il segmento
        Point3D start = segment.getStart();
        Point3D end = segment.getEnd();

        double diffXStart = (start.getX() - ExcavatorLib.bucketCoord[0]) * scala;
        double diffYStart = (start.getY() - ExcavatorLib.bucketCoord[1]) * scala;
        double diffXEnd = (end.getX() - ExcavatorLib.bucketCoord[0]) * scala;
        double diffYEnd = (end.getY() - ExcavatorLib.bucketCoord[1]) * scala;

        float rotatedXStart = (float) (bucketX + diffXStart * Math.cos(rotationAngle) - diffYStart * Math.sin(rotationAngle));
        float rotatedYStart = (float) (bucketY - diffXStart * Math.sin(rotationAngle) - diffYStart * Math.cos(rotationAngle));
        float rotatedXEnd = (float) (bucketX + diffXEnd * Math.cos(rotationAngle) - diffYEnd * Math.sin(rotationAngle));
        float rotatedYEnd = (float) (bucketY - diffXEnd * Math.sin(rotationAngle) - diffYEnd * Math.cos(rotationAngle));
        paint.setStrokeWidth((float) (3 / DataSaved.scale_Factor3D));
        paint.setColor(getResources().getColor(R.color.green));

        drawLineDist(rotatedXStart, rotatedYStart, rotatedXEnd, rotatedYEnd);
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

        if (DataSaved.isWL == 0) {
            canvas.drawRoundRect(leftCarroX - cingoliWidth, left_top_carro.y - (cingoliHeight * 5),
                    leftCarroX + cingoliWidth, right_bottom_carro.y - cingoliHeight, 10, 10, paint);
        } else if (DataSaved.isWL == 1) {
            //TODO whwwloader
        }

        cingoliLength = calculateDistance(leftCarroX - cingoliWidth, left_top_carro.y - (cingoliHeight * 5),
                leftCarroX + cingoliWidth, right_bottom_carro.y - cingoliHeight);

        // Cingoli destri
        if (DataSaved.isWL == 0) {
            canvas.drawRoundRect(rightCarroX - cingoliWidth, left_top_carro.y - (cingoliHeight * 5),
                    rightCarroX + cingoliWidth, right_bottom_carro.y - cingoliHeight, 10, 10, paint);
        } else if (DataSaved.isWL == 1) {
            //TODO wheeloader
        }

        // Disegna le linee dei cingoli
        double tmpLine = cingoliLength / 15;
        int tmpDist = 0;
        if (DataSaved.isWL == 0) {
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
        if (DataSaved.isWL == 0) {
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
            // Ripristina il pan al doppio tap e lo zoom
            offsetX = 0;
            offsetY = 100;
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

    private int myParseColor(int color) {
        try {
            String s = Integer.toHexString(color);

            if (DataSaved.temaSoftware == 0) {
                //sfondo nero
                if (s.equalsIgnoreCase("ff000000")) {
                    int[] rgb = new int[]{255, 255, 255};
                    return Color.rgb(rgb[0], rgb[1], rgb[2]);
                } else {
                    return color;
                }

            } else {
                if (s.equalsIgnoreCase("ffffffff")) {
                    int[] rgb = new int[]{0, 0, 0};
                    return Color.rgb(rgb[0], rgb[1], rgb[2]);
                } else {
                    return color;
                }
            }
        } catch (Exception e) {
            switch (DataSaved.temaSoftware) {
                case 0:
                    color = Color.WHITE;
                    break;
                case 1:
                    color = Color.BLACK;
                    break;
                case 2:
                    color = Color.BLACK;
                    break;
            }
        }
        return color;
    }

    private boolean isLayerEnabled(String layerName) {
        if (layerName == null || layerName.isEmpty()) {
            return false; // Layer nullo o vuoto non è abilitato
        }

        // Cerca il layer nelle tre liste
        for (Layer layer : DataSaved.dxfLayers_DTM) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                return true;
            }
        }
        for (Layer layer : DataSaved.dxfLayers_POLY) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                return true;
            }
        }
        for (Layer layer : DataSaved.dxfLayers_POINT) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                return true;
            }
        }

        return false; // Se il layer non è trovato o non è abilitato
    }

    private void drawAntenna1(Canvas canvas,Paint paint){
        paint.setStrokeWidth(1f);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        float distX= (float) (originPointStick.x-(DataSaved.deltaX*scala));
        float distY= (float) (originPointStick.y+(DataSaved.deltaY*scala));
        double angleRadians = Math.toRadians(90+DataSaved.deltaGPS2); // converti da gradi a radianti
        float x2 = (float) (distX + DataSaved.distG1_G2*scala * Math.cos(-angleRadians));
        float y2 = (float) (distY + DataSaved.distG1_G2*scala * Math.sin(-angleRadians));
        Path hexagon= createOctagon(distX,distY, (float) (0.15*scala));
        Path hexagon2= createOctagon(x2,y2,(float) 0.15*scala);
        canvas.drawPath(hexagon,paint);
        paint.setColor(Color.RED);
        canvas.drawPath(hexagon2,paint);
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.FILL);
        Path hexagon_= createOctagon(distX,distY, (float) (0.14*scala));
        Path hexagon2_= createOctagon(x2,y2,(float) 0.14*scala);
        canvas.drawPath(hexagon_,paint);
        canvas.drawPath(hexagon2_,paint);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(5f);
        canvas.drawText("1", (float) (distX-(0.05*scala)), (float) (distY+(0.06*scala)),paint);
        canvas.drawText("2", (float) (x2-(0.05*scala)), (float) (y2+(0.06*scala)),paint);

    }
    private Path createOctagon(float centerX, float centerY, float radius) {
        Path path = new Path();
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(45 * i - 22.5); // -22.5 per iniziare piatto in alto
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        return path;
    }
}
