package gui.draw_class;


import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.WHEELLOADER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.stx_dig.R;

import java.util.ArrayList;
import java.util.List;

import dxf.Arc;
import dxf.AutoCADColor;
import dxf.CanvasSegment;
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
import gui.my_opengl.My3DActivity;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import packexcalib.surfcreator.DistToLine;
import services.TriangleService;
import utils.DistToPoint;


public class Top_View_DXF_Nuova extends View {
    private Matrix canvasMatrix = new Matrix();
    public static Point3D lineCoord=new Point3D(0,0,0);

    List<PointF> arcPoints;
    List<List<PointF>> arcSegments;
    // Lista per memorizzare i dati dei segmenti
    List<float[]> segmentData;
    public static double giroFrecciaExca;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    Paint paint;

    PointF originPointBucket;
    PointF left_top_bucket, left_top_bucket2;
    PointF right_bottom_bucket;

    public float scala = 35;
    public static float offsetX;
    public static float offsetY;
    double w_bucket;

    double l_bucket;

    double bucketWidth;
    double bennaNord;
    double bennaEst;
    double rotationAngle;
    double rotationAngleBoom;
    float bucketX;
    float bucketY;
    Canvas canvas;
    float ancorPX, ancorPY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    float inizioX, inizioY;
    boolean isXML, isXMLLyne, isXMLPoint;

    public Top_View_DXF_Nuova(Context context) {

        super(context);

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

        try {
            isXMLPoint = DataSaved.progettoSelected_POINT.substring(DataSaved.progettoSelected_POINT.lastIndexOf(".") + 1).equalsIgnoreCase("xml");

        } catch (Exception e) {
            isXMLPoint = false;
        }

    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        this.canvas = canvas;

        paint.setAntiAlias(true);
        if (My3DActivity.glPoint) {
            DataSaved.Punti_Surf = 1;
        } else {
            DataSaved.Punti_Surf = 0;
        }
        if (My3DActivity.glText) {
            DataSaved.ShowText = 1;
        } else {
            DataSaved.ShowText = 0;
        }


        try {
            double dist = 0;
            double bucketHeight = 0;
            double bucketHeightFake = 0;
            double extraHeading=NmeaListener.roof_Orientation+DataSaved.offsetSwingExca;
            if(DataSaved.Extra_Heading == 0){
                extraHeading=0;
            }
            rotationAngle = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
            rotationAngleBoom=Math.toRadians(rotationAngle+extraHeading);
            if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER) {
                bucketWidth = DataSaved.W_Bucket * scala;

                l_bucket = DataSaved.L_Bucket;
                w_bucket = DataSaved.W_Bucket;
                originPointBucket = new PointF(getWidth() * 0.5f, getHeight() * 0.75f);
                canvas.save();
                canvas.scale((float) DataSaved.scale_Factor3D, (float) DataSaved.scale_Factor3D, getWidth() * 0.5f, getHeight() * 0.75f);
                canvas.translate(offsetX, offsetY);
                dist = new DistToPoint(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0, ExcavatorLib.coordPivoTilt[0], ExcavatorLib.coordPivoTilt[1], 0).getDist_to_point();
                bucketHeight = dist * scala;

                bucketHeightFake = 0.5 * scala;

                if (ExcavatorLib.correctBucket < -90 || ExcavatorLib.correctBucket > 90) {
                    bucketHeight = bucketHeight * -1;
                }
                left_top_bucket = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
                left_top_bucket2 = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeightFake);
                right_bottom_bucket = new PointF(originPointBucket.x + (float) bucketWidth / 2f, originPointBucket.y);
                bennaEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
                bennaNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
                bucketX = (((left_top_bucket.x) + (right_bottom_bucket.x)) * 0.5f);
                bucketY = left_top_bucket.y;
                drawDXFElements(bennaEst, bennaNord);
                drawBenna(canvas);
            }
            else if (DataSaved.isWL == GRADER || DataSaved.isWL == DOZER_SIX || DataSaved.isWL == DOZER) {
                PointF center = new PointF(getWidth() / 2f, getHeight() / 2f);
                canvas.rotate(45, center.x, center.y);
                canvas.rotate(-45, center.x, center.y);

                if (DataSaved.isWL == WHEELLOADER) {
                    bucketWidth = DataSaved.W_Bucket * scala;
                    l_bucket = DataSaved.W_Bucket;
                    w_bucket = DataSaved.W_Bucket;
                } else {
                    bucketWidth = DataSaved.W_Blade_TOT * scala;
                    l_bucket = DataSaved.W_Blade_TOT;
                    w_bucket = DataSaved.W_Blade_TOT;
                }
                canvas.save();
                canvas.scale((float) DataSaved.scale_Factor3D, (float) DataSaved.scale_Factor3D, getWidth() * 0.5f, getHeight() * 0.65f);
                canvas.translate(offsetX, offsetY);
                dist = 0.5;
                bucketHeight = dist * scala;
                originPointBucket = new PointF(getWidth() * 0.5f, getHeight() * 0.75f);
                left_top_bucket = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
                left_top_bucket2 = new PointF(originPointBucket.x - (float) bucketWidth / 2f, originPointBucket.y - (float) bucketHeight);
                right_bottom_bucket = new PointF(originPointBucket.x + (float) bucketWidth / 2f, originPointBucket.y);
                bennaEst = ExcavatorLib.bucketCoord[0]; // Coordinata REALI EST del primo punto
                bennaNord = ExcavatorLib.bucketCoord[1]; // Coordinata REALI NORD del primo punto
                bucketX = (float) ((((left_top_bucket.x + 0) + (right_bottom_bucket.x + 0)) * 0.5f));
                bucketY = left_top_bucket.y;
                drawDXFElements(bennaEst, bennaNord);
                drawLama(canvas);
            }

            float stopX = ((left_top_bucket.x + right_bottom_bucket.x) / 2f);
            float stopY = left_top_bucket.y;
            float stopXLeft = (left_top_bucket.x);
            float stopXRight = (right_bottom_bucket.x);

            paint.setColor(Color.BLUE);
            paint.setStrokeWidth((float) (0.1 * scala));
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

            canvas.restore();


        } catch (Exception e) {
            //Log.d("ErrorDXFWRITE", e.toString());
            e.printStackTrace();
        }
    }


    private void drawDXFElements(double bucketEst, double bucketNord) {
        try {

            if (My3DActivity.glGradient) {
                if (isXML) {
                    for (Face3D face : DataSaved.filteredFaces) {

                        // Cambia il tipo di lista in List<Face3D>
                        if (isLayerEnabled(face.getLayer().getLayerName())) {
                            Draw3DFace.drawFaceGradientCanvas(paint,
                                    canvas,
                                    face.toArrayWithCentroid(),
                                    bucketX,
                                    bucketY,
                                    bucketEst,
                                    bucketNord,
                                    scala,
                                    rotationAngle,
                                    TriangleService.maxZ,
                                    TriangleService.minZ
                            );
                        }
                    }

                } else {
                    for (Face3D face : DataSaved.filteredFaces) {

                        if (isLayerEnabled(face.getLayer().getLayerName())) {
                            Draw3DFace.drawFaceGradientCanvas(paint,
                                    canvas,
                                    face.toArrayWithCentroid(),
                                    bucketX,
                                    bucketY,
                                    bucketEst,
                                    bucketNord,
                                    scala,
                                    rotationAngle,
                                    TriangleService.maxZ,
                                    TriangleService.minZ
                            );
                        }

                    }
                }
            } else {
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
                                    My3DActivity.glFace,
                                    My3DActivity.glFill
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
                                    My3DActivity.glFace,
                                    My3DActivity.glFill
                            );
                        }

                    }
                }
            }


        } catch (Exception e) {
            Log.d("expRT", "excFace");
        }

        try {

            if (My3DActivity.glPoly) {

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

            if (My3DActivity.glPoint) {
                int col = 0;

                for (Point3D point : DataSaved.filteredPoints) {
                    if (isLayerEnabled(point.getLayer().getLayerName())) { // Controlla se il layer è abilitato
                        if (isXMLPoint) {
                            col = myParseColor(Color.WHITE);
                        } else {
                            col = myParseColor(point.getLayer().getColorState());
                        }
                        DrawDXFPoint.draw(canvas,
                                paint,
                                point,
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                scala,
                                col,
                                rotationAngle
                        );
                    }
                }
            }
        } catch (Exception e) {

            Log.d("expRT", "excPT");
        }

        try {
            if (My3DActivity.glText) {
                int col = 0;
                for (DxfText dxfText : DataSaved.filteredDxfTexts) {

                    if (isLayerEnabled(dxfText.getLayer().getLayerName())) { // Controlla se il layer è abilitato
                        if (isXMLPoint) {
                            col = myParseColor(Color.WHITE);
                        } else {
                            col = myParseColor(dxfText.getLayer().getColorState());
                        }
                        DrawDXFText.draw(canvas, paint, dxfText, bucketX, bucketY, bucketEst, bucketNord, scala, col, rotationAngle);
                    }
                }
            }
        } catch (Exception e) {
            Log.d("expRT", "excTxT");
        }

        if (My3DActivity.glPoly) {
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


    private void drawNearestSegmentAndLine() {

        Segment closestSegment = DataSaved.nearestSegment;

        if (closestSegment != null) {
            // Disegna il segmento più vicino di verde
            drawSegment(closestSegment);


        }
    }

    private void drawSegment(Segment segment) {
        // Calcola la rotazione e le coordinate per disegnare il segmento
        Point3D start = segment.getStart();
        Point3D end = segment.getEnd();

        double diffXStart = (start.getX() - ExcavatorLib.bucketCoord[0]) * scala;
        double diffYStart = (start.getY() - ExcavatorLib.bucketCoord[1]) * scala;
        double diffXEnd = (end.getX() - ExcavatorLib.bucketCoord[0]) * scala;
        double diffYEnd = (end.getY() - ExcavatorLib.bucketCoord[1]) * scala;

        double rotatedXStart = (bucketX + diffXStart * Math.cos(rotationAngle) - diffYStart * Math.sin(rotationAngle));
        double rotatedYStart = (bucketY - diffXStart * Math.sin(rotationAngle) - diffYStart * Math.cos(rotationAngle));
        double rotatedXEnd = (bucketX + diffXEnd * Math.cos(rotationAngle) - diffYEnd * Math.sin(rotationAngle));
        double rotatedYEnd = (bucketY - diffXEnd * Math.sin(rotationAngle) - diffYEnd * Math.cos(rotationAngle));
        paint.setStrokeWidth((float) (3 / DataSaved.scale_Factor3D));
        paint.setColor(getResources().getColor(R.color.green));

        drawLineDist(rotatedXStart, rotatedYStart, rotatedXEnd, rotatedYEnd);
    }

    private void drawLineDist(double aX, double aY, double bX, double bY) {
        float pX = 0, pY = 0;
        switch (DataSaved.bucketEdge) {
            case -1:
                pX = (float) (left_top_bucket.x);
                pY = left_top_bucket.y;
                break;
            case 0:
                pX = ((right_bottom_bucket.x) + (float) (left_top_bucket.x)) / 2f;
                pY = left_top_bucket.y;
                break;
            case 1:
                pX = (float) (right_bottom_bucket.x);
                pY = left_top_bucket.y;
                break;
        }

        float pXR = (float) (ancorPX + (pX - ancorPX) * Math.cos(Math.toRadians(ExcavatorLib.yawSensor)) -
                (pY - ancorPY) * Math.sin(Math.toRadians(ExcavatorLib.yawSensor)));
        float pYR = (float) (ancorPY + (pX - ancorPX) * Math.sin(Math.toRadians(ExcavatorLib.yawSensor)) +
                (pY - ancorPY) * Math.cos(Math.toRadians(ExcavatorLib.yawSensor)));

        double dx = bX - aX;
        double dy = bY - aY;
        double lengthSq = dx * dx + dy * dy;

        if (lengthSq == 0) return;

        double APx = pXR - aX;
        double APy = pYR - aY;
        double t = (APx * dx + APy * dy) / lengthSq;
        t = clamp((float) t, 0f, 1f);

        double intersectionX = aX + t * dx;
        double intersectionY = aY + t * dy;

        canvas.drawLine(pXR, pYR, (float) intersectionX, (float) intersectionY, paint);

        Top_View_DXF.giroFrecciaExca = My_LocationCalc.calcBearingXY(intersectionX, intersectionY, pXR, pYR);
        Top_View_DXF.giroFrecciaExca = 360 - Top_View_DXF.giroFrecciaExca;
    }


    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }


    private void drawBenna(Canvas canvas) {
        ancorPX = (float) (((left_top_bucket.x + right_bottom_bucket.x) * 0.5f));
        ancorPY = left_top_bucket.y;
        canvas.rotate((float) ExcavatorLib.yawSensor, (ancorPX), ancorPY);
        paint.setColor(MyColorClass.colorBucket);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect((float) (left_top_bucket.x), left_top_bucket.y, (float) (right_bottom_bucket.x), right_bottom_bucket.y, paint);
        if (DataSaved.isWL == EXCAVATOR) {
            if (ExcavatorLib.correctBucket < -90 || ExcavatorLib.correctBucket > 90) {
                paint.setColor(getResources().getColor(R.color.transparentgray));
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1.5f);
                canvas.drawRect((float) (left_top_bucket.x) + 2.5f, left_top_bucket.y - 2f, (float) (right_bottom_bucket.x) - 2.5f, right_bottom_bucket.y + 1f, paint);

            }
        } else {
            if (ExcavatorLib.correctBucket < 90 && ExcavatorLib.correctBucket > -90) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(getResources().getColor(R.color.transparentgray));
                paint.setStrokeWidth(1.5f);
                canvas.drawRect((float) (left_top_bucket.x) + 2.5f, left_top_bucket.y - 2f, (float) (right_bottom_bucket.x) - 2.5f, right_bottom_bucket.y + 1f, paint);

            }

        }
        paint.setColor(MyColorClass.colorBucket);
        paint.setStyle(Paint.Style.FILL);

        //disegna la benna fake a larghezza fissa per evitare l'effetto di far scomparire la benna
        canvas.drawRect((float) (left_top_bucket2.x), left_top_bucket2.y, (float) (right_bottom_bucket.x), right_bottom_bucket.y, paint);
        canvas.rotate((float) -ExcavatorLib.yawSensor, (ancorPX), ancorPY);


        //disegna la croce
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth((float) (2.5f / DataSaved.scale_Factor3D));
        float stopX = (float) ((left_top_bucket.x + right_bottom_bucket.x) / 2f);
        float stopY = left_top_bucket.y;
        float stopXLeft = (float) (left_top_bucket.x);
        float stopXRight = (float) (right_bottom_bucket.x);
        float x0 = stopXLeft;
        float y0 = stopY;
        float x1 = stopXLeft;
        float y1 = stopY - 1000f;
        float dx = x1 - x0;
        float dy = y1 - y0;
        float step = (float) (2.5f * scala);
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return;

        float ux = dx / length;
        float uy = dy / length;

        int count = (int) (length / step);
        if (DataSaved.showAlign == 1) {
            paint.setStyle(Paint.Style.FILL);
            switch (DataSaved.bucketEdge) {
                case -1:
                    canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    ux = dx / length;
                    uy = dy / length;
                    for (int i = 1; i <= count; i++) {   // 👈 parte da 1, NON da 0
                        float dist = i * step;

                        if (dist > length) dist = length; // forza ultimo esatto

                        float cx = x0 + ux * dist;
                        float cy = y0 + uy * dist;

                        drawArrowTriangle(
                                canvas,
                                cx,
                                cy,
                                ux,
                                uy,
                                (float) (10f / DataSaved.scale_Factor3D)
                        );
                    }
                    canvas.drawLine(stopXLeft, stopY, stopXLeft, stopY - 1000f, paint);//fw
                    canvas.drawLine(stopXLeft, stopY, stopXLeft, stopY + 1000f, paint);//bw
                    canvas.drawLine(stopXLeft, stopY, stopXLeft - 500f, stopY, paint);//left
                    canvas.drawLine(stopXLeft, stopY, stopXLeft + 500f, stopY, paint);//right
                    canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    break;

                case 0:
                    x0 = stopX;
                    y0 = stopY;
                    x1 = stopX;
                    y1 = stopY - 1000f;
                    dx = x1 - x0;
                    dy = y1 - y0;
                    ux = dx / length;
                    uy = dy / length;
                    canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    for (int i = 1; i <= count; i++) {   // 👈 parte da 1, NON da 0
                        float dist = i * step;

                        if (dist > length) dist = length; // forza ultimo esatto

                        float cx = x0 + ux * dist;
                        float cy = y0 + uy * dist;

                        drawArrowTriangle(
                                canvas,
                                cx,
                                cy,
                                ux,
                                uy,
                                (float) (10f / DataSaved.scale_Factor3D)
                        );
                    }
                    canvas.drawLine(stopX, stopY, stopX, stopY - 1000f, paint);//fw
                    canvas.drawLine(stopX, stopY, stopX, stopY + 1000f, paint);//bw
                    canvas.drawLine(stopX, stopY, stopX - 500f, stopY, paint);//left
                    canvas.drawLine(stopX, stopY, stopX + 500f, stopY, paint);//right
                    canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    break;

                case 1:
                    x0 = stopXRight;
                    y0 = stopY;
                    x1 = stopXRight;
                    y1 = stopY - 1000f;
                    dx = x1 - x0;
                    dy = y1 - y0;
                    ux = dx / length;
                    uy = dy / length;
                    canvas.rotate((float) ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    for (int i = 1; i <= count; i++) {   // 👈 parte da 1, NON da 0
                        float dist = i * step;

                        if (dist > length) dist = length; // forza ultimo esatto

                        float cx = x0 + ux * dist;
                        float cy = y0 + uy * dist;

                        drawArrowTriangle(
                                canvas,
                                cx,
                                cy,
                                ux,
                                uy,
                                (float) (10f / DataSaved.scale_Factor3D)
                        );
                    }
                    canvas.drawLine(stopXRight, stopY, stopXRight, stopY - 1000f, paint);//fw
                    canvas.drawLine(stopXRight, stopY, stopXRight, stopY + 1000f, paint);//bw
                    canvas.drawLine(stopXRight, stopY, stopXRight - 500f, stopY, paint);//left
                    canvas.drawLine(stopXRight, stopY, stopXRight + 500f, stopY, paint);//right
                    canvas.rotate((float) -ExcavatorLib.yawSensor, ancorPX, ancorPY);
                    break;
            }
            paint.setStrokeWidth(2.5f);
            paint.setColor(MyColorClass.colorBucket);
        }

    }

    private void drawLama(Canvas canvas) {
        float delta = 0;
        if (DataSaved.isWL == GRADER) {
            delta = (float) (0.25 * scala);
        }
        paint.setColor(MyColorClass.colorBucket);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect((left_top_bucket2.x), left_top_bucket2.y, (right_bottom_bucket.x), right_bottom_bucket.y - delta, paint);
        //disegna la croce
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth((float) (2.5f / DataSaved.scale_Factor3D));
        float stopX = (float) ((left_top_bucket.x + right_bottom_bucket.x) / 2f);
        float stopY = left_top_bucket.y;
        float stopXLeft = (float) (left_top_bucket.x);
        float stopXRight = (float) (right_bottom_bucket.x);
        float x0 = stopXLeft;
        float y0 = stopY;
        float x1 = stopXLeft;
        float y1 = stopY - 1000f;
        float dx = x1 - x0;
        float dy = y1 - y0;
        float step = (float) (2.5f * scala);
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0) return;

        float ux = dx / length;
        float uy = dy / length;

        int count = (int) (length / step);
        if (DataSaved.showAlign == 1) {
            switch (DataSaved.bucketEdge) {
                case -1:
                    ux = dx / length;
                    uy = dy / length;
                    for (int i = 1; i <= count; i++) {   // 👈 parte da 1, NON da 0
                        float dist = i * step;

                        if (dist > length) dist = length; // forza ultimo esatto

                        float cx = x0 + ux * dist;
                        float cy = y0 + uy * dist;

                        drawArrowTriangle(
                                canvas,
                                cx,
                                cy,
                                ux,
                                uy,
                                (float) (10f / DataSaved.scale_Factor3D)
                        );
                    }
                    canvas.drawLine(stopXLeft, stopY, stopXLeft, stopY - 1000f, paint);//fw
                    canvas.drawLine(stopXLeft, stopY, stopXLeft, stopY + 1000f, paint);//bw
                    canvas.drawLine(stopXLeft, stopY, stopXLeft - 500f, stopY, paint);//left
                    canvas.drawLine(stopXLeft, stopY, stopXLeft + 500f, stopY, paint);//right
                    break;

                case 0:
                    x0 = stopX;
                    y0 = stopY;
                    x1 = stopX;
                    y1 = stopY - 1000f;
                    dx = x1 - x0;
                    dy = y1 - y0;
                    ux = dx / length;
                    uy = dy / length;
                    for (int i = 1; i <= count; i++) {   // 👈 parte da 1, NON da 0
                        float dist = i * step;

                        if (dist > length) dist = length; // forza ultimo esatto

                        float cx = x0 + ux * dist;
                        float cy = y0 + uy * dist;

                        drawArrowTriangle(
                                canvas,
                                cx,
                                cy,
                                ux,
                                uy,
                                (float) (10f / DataSaved.scale_Factor3D)
                        );
                    }
                    canvas.drawLine(stopX, stopY, stopX, stopY - 1000f, paint);//fw
                    canvas.drawLine(stopX, stopY, stopX, stopY + 1000f, paint);//bw
                    canvas.drawLine(stopX, stopY, stopX - 500f, stopY, paint);//left
                    canvas.drawLine(stopX, stopY, stopX + 500f, stopY, paint);//right
                    break;

                case 1:
                    x0 = stopXRight;
                    y0 = stopY;
                    x1 = stopXRight;
                    y1 = stopY - 1000f;
                    dx = x1 - x0;
                    dy = y1 - y0;
                    ux = dx / length;
                    uy = dy / length;
                    for (int i = 1; i <= count; i++) {   // 👈 parte da 1, NON da 0
                        float dist = i * step;

                        if (dist > length) dist = length; // forza ultimo esatto

                        float cx = x0 + ux * dist;
                        float cy = y0 + uy * dist;

                        drawArrowTriangle(
                                canvas,
                                cx,
                                cy,
                                ux,
                                uy,
                                (float) (10f / DataSaved.scale_Factor3D)
                        );
                    }
                    canvas.drawLine(stopXRight, stopY, stopXRight, stopY - 1000f, paint);//fw
                    canvas.drawLine(stopXRight, stopY, stopXRight, stopY + 1000f, paint);//bw
                    canvas.drawLine(stopXRight, stopY, stopXRight - 500f, stopY, paint);//left
                    canvas.drawLine(stopXRight, stopY, stopXRight + 500f, stopY, paint);//right
                    break;
            }
            paint.setColor(MyColorClass.colorBucket);
            paint.setStrokeWidth(2.5f);
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
            offsetY = 0;
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {

            return super.onSingleTapUp(e);
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


    public static boolean isTouchNearSegment(float touchX, float touchY, CanvasSegment segment, float tolerance) {
        float dist = (float) new DistToLine(touchX,
                touchY,
                segment.x1,
                segment.y1,
                segment.x2,
                segment.y2).getLinedistance();
        Log.d("Distanza da Tocco", "Dist: " + Math.abs(dist) + "  " + tolerance);
        return Math.abs(dist) <= tolerance;
    }

    private PointF screenToCanvas(float screenX, float screenY) {
        Matrix inverseMatrix = new Matrix();
        if (canvasMatrix != null) {
            canvasMatrix.invert(inverseMatrix);
        }

        float[] pts = new float[]{screenX, screenY};
        inverseMatrix.mapPoints(pts);

        return new PointF(pts[0], pts[1]);
    }

    private void drawArrowTriangle(
            Canvas canvas,
            float cx, float cy,
            float ux, float uy,
            float size
    ) {
        // vettore perpendicolare
        float px = -uy;
        float py = ux;

        float h = size;          // altezza triangolo
        float w = size * 0.6f;   // larghezza base

        Path p = new Path();

        // punta
        p.moveTo(
                cx + ux * h,
                cy + uy * h
        );

        // base sinistra
        p.lineTo(
                cx - ux * h * 0.3f + px * w,
                cy - uy * h * 0.3f + py * w
        );

        // base destra
        p.lineTo(
                cx - ux * h * 0.3f - px * w,
                cy - uy * h * 0.3f - py * w
        );

        p.close();
        canvas.drawPath(p, paint);
    }


}
