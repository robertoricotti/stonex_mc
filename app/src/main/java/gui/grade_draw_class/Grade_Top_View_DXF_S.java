package gui.grade_draw_class;

import static gui.grade_draw_class.Grade_Top_View_DXF.offsetX;
import static gui.grade_draw_class.Grade_Top_View_DXF.offsetY;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.stx_dig.R;

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
import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;

public class Grade_Top_View_DXF_S extends SurfaceView implements SurfaceHolder.Callback {
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    Paint paint;
    PointF originPointCarro;
    PointF left_top_carro;
    PointF right_bottom_carro;
    PointF originPointStick;
    double distanceCingoli;
    float cingoliWidth;
    float cingoliHeight;
    PointF originPointBucket;
    PointF left_top_bucket, left_top_bucket2;
    PointF right_bottom_bucket;
    public float scala = 35;
    double w_bucket;
    double d_stick;
    double l_bucket;
    double lcarro;
    double cingoliLength;
    double stickWidth;
    double bucketWidth;
    double buckEst;
    double buckNord;
    double rotationAngle;
    float bucketX;
    float bucketY;
    float mRoll;
    boolean isXML, isXMLLyne;


    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    private RenderThread renderThread;
    private Canvas canvas;

    public Grade_Top_View_DXF_S(Context context, AttributeSet attrrs) {
        super(context, attrrs);
        getHolder().addCallback(this);
        setFocusable(true);

        // inizializza gestori touch, zoom, gesture ecc.
        init(context);
    }

    private void init(Context context) {


        if (DataSaved.scale_Factor3D == 0) {
            DataSaved.scale_Factor3D = 1f;
        }
        paint = new Paint();

        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        if (offsetY == 0) {
            offsetY = 150;
        }


        if (DataSaved.isWL == WHEELLOADER) {
            bucketWidth = DataSaved.W_Bucket * scala;
        } else {
            bucketWidth = DataSaved.W_Blade_TOT * scala;
        }
        mRoll = 0; //DataSaved.L_Roll * scala;
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

    public void requestRedraw() {
        if (renderThread != null) {
            renderThread.requestRedraw();
        }
    }

    private void drawInternal(Canvas canvas) {


        this.canvas = canvas;

        paint.setAntiAlias(true);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        try {
            PointF center = new PointF(getWidth() / 2f, getHeight() / 2f);
            canvas.rotate(45, center.x, center.y);
            canvas.rotate(-45, center.x, center.y);
            rotationAngle = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
            if (DataSaved.isWL == 1) {
                l_bucket = DataSaved.W_Bucket;
                w_bucket = DataSaved.W_Bucket;
            } else {
                l_bucket = DataSaved.W_Blade_TOT;
                w_bucket = DataSaved.W_Blade_TOT;
            }

            d_stick = 0;
            originPointCarro = new PointF(getWidth() * 0.5f, getHeight() * 0.5f);
            canvas.save();
            canvas.scale((float) DataSaved.scale_Factor3D, (float) DataSaved.scale_Factor3D, getWidth() * 0.5f, getHeight() * 0.65f);
            canvas.translate(offsetX, offsetY);
            initDozer();//inizializza  dozer
            double dist = 0.5;

            double bucketHeight = dist * scala;


            originPointBucket = new PointF(originPointStick.x, originPointStick.y);
            stickWidth = scala * 0.40f;
            left_top_bucket = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
            left_top_bucket2 = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
            right_bottom_bucket = new PointF(originPointBucket.x + (float) bucketWidth / 2f, originPointBucket.y);

            buckEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
            buckNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
            bucketX = (float) ((((left_top_bucket.x + mRoll) + (right_bottom_bucket.x + mRoll)) * 0.5f));
            bucketY = left_top_bucket.y;
            drawDXFElements(buckEst, buckNord);

            float stopX = (float) ((left_top_bucket.x + (mRoll) + right_bottom_bucket.x + (mRoll)) / 2f);
            float stopY = left_top_bucket.y;
            float stopXLeft = (float) (left_top_bucket.x + (mRoll));
            float stopXRight = (float) (right_bottom_bucket.x + (mRoll));


            paint.setStyle(Paint.Style.STROKE);

            drawLama(canvas);

            switch (DataSaved.isWL) {
                case 1:

                    drawGomme();
                    drawBracciW(canvas);
                    drawLama(canvas);
                    drawWheel();
                    break;
                case 2:
                case 3:
                    drawCingoliDozer();
                    drawBracci(canvas);
                    drawDozer();
                    drawAntenna1(canvas, paint);
                    break;
                case 4:
                    drawGrader();
                    drawGommeGrader();
                    drawAntenna1(canvas, paint);
                    //disegna il grader
                    break;
            }
            paint.setColor(getResources().getColor(R.color.blue));
            paint.setStrokeWidth((float) (0.1 * scala));
            paint.setStyle(Paint.Style.FILL);
            switch (DataSaved.bucketEdge) {
                case -1:

                    canvas.drawCircle(stopXLeft, stopY, 3.5f, paint);
                    if (DataSaved.isAutoSnap == 1 || DataSaved.isAutoSnap == 3) {
                        drawNearestPoint(DataSaved.nearestPoint, stopXLeft, stopY);

                    } else if (DataSaved.isAutoSnap == 2 || DataSaved.isAutoSnap == 4) {
                        drawNearestSegmentAndLine();
                    }
                    break;
                case 0:

                    canvas.drawCircle(stopX, stopY, 3.5f, paint);
                    if (DataSaved.isAutoSnap == 1 || DataSaved.isAutoSnap == 3) {
                        drawNearestPoint(DataSaved.nearestPoint, stopX, stopY);

                    } else if (DataSaved.isAutoSnap == 2 || DataSaved.isAutoSnap == 4) {
                        drawNearestSegmentAndLine();
                    }
                    break;
                case 1:

                    canvas.drawCircle(stopXRight, stopY, 3.5f, paint);
                    if (DataSaved.isAutoSnap == 1 || DataSaved.isAutoSnap == 3) {
                        drawNearestPoint(DataSaved.nearestPoint, stopXRight, stopY);

                    } else if (DataSaved.isAutoSnap == 2 || DataSaved.isAutoSnap == 4) {
                        drawNearestSegmentAndLine();
                    }
                    break;
            }

            canvas.restore();


        } catch (Exception e) {
            e.printStackTrace();
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

    private void drawAntenna1(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(1f);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        float distX = (float) (bucketX - (DataSaved.deltaX * scala));
        float distY = (float) (bucketY + (DataSaved.deltaY * scala));
        double angleRadians = Math.toRadians(90 + DataSaved.deltaGPS2); // converti da gradi a radianti
        double dist = DataSaved.W_Blade_TOT - (DataSaved.W_Blade_LEFT - DataSaved.deltaX) - DataSaved.distBetween;
        float x2 = (float) (distX + dist * scala * Math.cos(-angleRadians));
        float y2 = (float) (distY + dist * scala * Math.sin(-angleRadians));
        Path hex1 = createOctagon(distX, distY, (float) (0.15 * scala));
        Path hex1_ant2 = createOctagon(x2, y2, (float) (0.15 * scala));
        canvas.drawPath(hex1, paint);
        paint.setColor(Color.RED);
        canvas.drawPath(hex1_ant2, paint);
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.FILL);
        Path hex2 = createOctagon(distX, distY, (float) (0.14 * scala));
        Path hex2_ant2 = createOctagon(x2, y2, (float) (0.14 * scala));
        canvas.drawPath(hex2, paint);
        canvas.drawPath(hex2_ant2, paint);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(5f);
        canvas.drawText("1", (float) (distX - (0.05 * scala)), (float) (distY + (0.06 * scala)), paint);
        canvas.drawText("2", (float) (x2 - (0.05 * scala)), (float) (y2 + (0.06 * scala)), paint);

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

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void drawDXFElements(double bucketEst, double bucketNord) {
        try {
            if (isXML) {
                for (Face3D face : DataSaved.filteredFaces) { // Cambia il tipo di lista in List<Face3D>
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
                                DataSaved.Triangoli_Surf != 0,
                                DataSaved.Colore_Surf == 2
                        );
                    }
                }
            } else {
                for (Face3D face : DataSaved.filteredFaces) { // Cambia il tipo di lista in List<Face3D>
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
                            myParseColor(DataSaved.selectedPoly.getLineColor()),
                            scala,
                            rotationAngle
                    );
                }
            } else {

                for (Polyline polyline : DataSaved.polylines) {
                    if (isLayerEnabled(polyline.getLayer().getLayerName())) {
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

        }

        try {
            if (DataSaved.ShowText == 1) {
                for (DxfText dxfText : DataSaved.filteredDxfTexts) {
                    if (isLayerEnabled(dxfText.getLayer().getLayerName())) { // Controlla se il layer è abilitato
                        DrawDXFText.draw(canvas, paint, dxfText, bucketX, bucketY, bucketEst, bucketNord, scala, myParseColor(MyColorClass.colorLabel), rotationAngle);
                    }
                }
            }
        } catch (Exception e) {
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
        }


    }

    private void drawNearestPoint(Point3D point, float mX, float mY) {


        double diffX = (point.getX() - ExcavatorLib.bucketCoord[0]) * scala;
        double diffY = (point.getY() - ExcavatorLib.bucketCoord[1]) * scala;
        float rotatedXV1 = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
        float rotatedYV1 = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.bg_sfsgreen));
        canvas.drawCircle(rotatedXV1, rotatedYV1, 6f, paint);
        canvas.drawCircle(mX, mY, 6f, paint);
        paint.setStrokeWidth(3f);
        canvas.drawLine(rotatedXV1, rotatedYV1, mX, mY, paint);
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

        // Calcola il coefficiente angolare della linea AB
        float mAB = (bY - aY) / (bX - aX);

        // Calcola il coefficiente angolare della retta perpendicolare
        float mPerpendicular = -1 / mAB;

        // Calcola l'ordinata all'origine (intercetta) della retta perpendicolare
        float bPerpendicular = pY - mPerpendicular * pX;

        // Calcola le coordinate dell'intersezione tra le due linee
        float intersectionX = (bPerpendicular - aY + mAB * aX) / (mAB - mPerpendicular);
        float intersectionY = mAB * (intersectionX - aX) + aY;

        // Limita l'intersezione ai limiti di AB
        intersectionX = clamp(intersectionX, Math.min(aX, bX), Math.max(aX, bX));
        intersectionY = clamp(intersectionY, Math.min(aY, bY), Math.max(aY, bY));

        // Disegna la linea da P all'intersezione
        canvas.drawLine(pX, pY, intersectionX, intersectionY, paint);
        Grade_Top_View_DXF.giroFrecciaDozer = My_LocationCalc.calcBearingXY(intersectionX, intersectionY, pX, pY);
        Grade_Top_View_DXF.giroFrecciaDozer = 360 - Grade_Top_View_DXF.giroFrecciaDozer;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private void drawSegment(Segment segment) {

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

    // Mantieni tutta la logica di disegno come prima (drawDXFElements, onTouchEvent ecc.)
    // Unica differenza: chiami requestRedraw() al posto di invalidate()
}
