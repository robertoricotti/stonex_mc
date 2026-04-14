package gui.draw_class;


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
import gui.my_opengl.MyGLRenderer;
import gui.my_opengl.MyGLSurfaceView;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.gnss.NmeaListener;
import packexcalib.surfcreator.DistToLine;
import services.TriangleService;


public class Top_View_DXF extends View {
    private Matrix canvasMatrix = new Matrix();
    public static Point3D lineCoord = new Point3D(0, 0, 0);

    List<PointF> arcPoints;
    List<List<PointF>> arcSegments;
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
    private MyGLSurfaceView glSurfaceView;

    public Top_View_DXF(Context context, MyGLSurfaceView glSurfaceView) {
        super(context);
        this.glSurfaceView = glSurfaceView;

        if (DataSaved.scale_Factor3D == 0) {
            DataSaved.scale_Factor3D = 1f;
        }

        arcPoints = new ArrayList<>();
        arcSegments = new ArrayList<>();
        segmentData = new ArrayList<>();
        paint = new Paint();
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        try {
            isXML = DataSaved.progettoSelected.substring(
                    DataSaved.progettoSelected.lastIndexOf(".") + 1
            ).equalsIgnoreCase("xml");
        } catch (Exception e) {
            isXML = false;
        }

        try {
            isXMLLyne = DataSaved.progettoSelected_POLY.substring(
                    DataSaved.progettoSelected_POLY.lastIndexOf(".") + 1
            ).equalsIgnoreCase("xml");
        } catch (Exception e) {
            isXMLLyne = false;
        }

        try {
            isXMLPoint = DataSaved.progettoSelected_POINT.substring(
                    DataSaved.progettoSelected_POINT.lastIndexOf(".") + 1
            ).equalsIgnoreCase("xml");
        } catch (Exception e) {
            isXMLPoint = false;
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;
/*
        if (My3DActivity.glVista3d == 0) {
            syncTopViewFromGl();
        }

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
            double extraHeading = NmeaListener.roof_Orientation + DataSaved.offsetSwingExca;

            if (DataSaved.Extra_Heading == 0) {
                extraHeading = 0;
            }

            rotationAngle = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
            rotationAngleBoom = Math.toRadians(rotationAngle + extraHeading);

            if (DataSaved.isWL == EXCAVATOR || DataSaved.isWL == WHEELLOADER) {
                bucketWidth = DataSaved.W_Bucket * scala;
                l_bucket = DataSaved.L_Bucket;
                w_bucket = DataSaved.W_Bucket;

                originPointBucket = worldToScreen(
                        ExcavatorLib.bucketCoord[0],
                        ExcavatorLib.bucketCoord[1]
                );

                canvas.save();

                if (My3DActivity.glVista3d != 0) {
                    canvas.scale(
                            (float) DataSaved.scale_Factor3D,
                            (float) DataSaved.scale_Factor3D,
                            originPointBucket.x,
                            originPointBucket.y
                    );
                    canvas.translate(offsetX, offsetY);
                }

                canvasMatrix = new Matrix(canvas.getMatrix());

                dist = new DistToPoint(
                        ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1], 0,
                        ExcavatorLib.coordPivoTilt[0], ExcavatorLib.coordPivoTilt[1], 0
                ).getDist_to_point();

                bucketHeight = dist * scala;
                bucketHeightFake = 0.5 * scala;

                if (ExcavatorLib.correctBucket < -90 || ExcavatorLib.correctBucket > 90) {
                    bucketHeight = bucketHeight * -1;
                }

                left_top_bucket = new PointF(
                        originPointBucket.x - (float) bucketWidth / 2f,
                        originPointBucket.y - (float) bucketHeight
                );
                left_top_bucket2 = new PointF(
                        originPointBucket.x - (float) bucketWidth / 2f,
                        originPointBucket.y - (float) bucketHeightFake
                );
                right_bottom_bucket = new PointF(
                        originPointBucket.x + (float) bucketWidth / 2f,
                        originPointBucket.y
                );

                bennaEst = ExcavatorLib.bucketCoord[0];
                bennaNord = ExcavatorLib.bucketCoord[1];
                bucketX = ((left_top_bucket.x + right_bottom_bucket.x) * 0.5f);
                bucketY = left_top_bucket.y;

                drawDXFElements(bennaEst, bennaNord);

            } else if (DataSaved.isWL == GRADER || DataSaved.isWL == DOZER_SIX || DataSaved.isWL == DOZER) {
                if (DataSaved.isWL == WHEELLOADER) {
                    bucketWidth = DataSaved.W_Bucket * scala;
                    l_bucket = DataSaved.W_Bucket;
                    w_bucket = DataSaved.W_Bucket;
                } else {
                    bucketWidth = DataSaved.W_Blade_TOT * scala;
                    l_bucket = DataSaved.W_Blade_TOT;
                    w_bucket = DataSaved.W_Blade_TOT;
                }

                dist = 0.5;
                bucketHeight = dist * scala;
                originPointBucket = worldToScreen(
                        ExcavatorLib.bucketCoord[0],
                        ExcavatorLib.bucketCoord[1]
                );

                canvas.save();

                if (My3DActivity.glVista3d != 0) {
                    canvas.scale(
                            (float) DataSaved.scale_Factor3D,
                            (float) DataSaved.scale_Factor3D,
                            originPointBucket.x,
                            originPointBucket.y
                    );
                    canvas.translate(offsetX, offsetY);
                }

                canvasMatrix = new Matrix(canvas.getMatrix());

                left_top_bucket = new PointF(
                        originPointBucket.x - (float) bucketWidth / 2f,
                        originPointBucket.y - (float) bucketHeight
                );
                left_top_bucket2 = new PointF(
                        originPointBucket.x - (float) bucketWidth / 2f,
                        originPointBucket.y - (float) bucketHeight
                );
                right_bottom_bucket = new PointF(
                        originPointBucket.x + (float) bucketWidth / 2f,
                        originPointBucket.y
                );

                bennaEst = ExcavatorLib.bucketCoord[0];
                bennaNord = ExcavatorLib.bucketCoord[1];
                bucketX = ((left_top_bucket.x + right_bottom_bucket.x) * 0.5f);
                bucketY = left_top_bucket.y;

                drawDXFElements(bennaEst, bennaNord);
            }

            float stopX = ((left_top_bucket.x + right_bottom_bucket.x) / 2f);
            float stopY = left_top_bucket.y;
            float stopXLeft = left_top_bucket.x;
            float stopXRight = right_bottom_bucket.x;

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
            e.printStackTrace();
        }*/
    }

    private void drawDXFElements(double bucketEst, double bucketNord) {
        try {
            for (Face3D face : DataSaved.filteredFaces) {
                if (!isLayerEnabled(face.getLayer().getLayerName())) continue;

                double[][] verts = face.toArrayWithCentroid();
                if (verts == null || verts.length < 3) continue;

                PointF p1 = worldToScreen(verts[0][0], verts[0][1]);
                PointF p2 = worldToScreen(verts[1][0], verts[1][1]);
                PointF p3 = worldToScreen(verts[2][0], verts[2][1]);

                if (p1 == null || p2 == null || p3 == null) continue;

                float[][] screenPts = new float[][]{
                        {p1.x, p1.y},
                        {p2.x, p2.y},
                        {p3.x, p3.y}
                };

                if (My3DActivity.glGradient) {
                    Draw3DFace.drawFaceGradientScreen(
                            paint,
                            canvas,
                            verts,
                            screenPts,
                            TriangleService.maxZ,
                            TriangleService.minZ
                    );
                } else {
                    int faceColor;
                    if (isXML) {
                        faceColor = myParseColor(
                                AutoCADColor.getColor(String.valueOf(face.getLayer().getColorState()))
                        );
                    } else {
                        faceColor = myParseColor(face.getLayer().getColorState());
                    }

                    Draw3DFace.drawScreen(
                            paint,
                            canvas,
                            screenPts,
                            faceColor,
                            My3DActivity.glFace,
                            My3DActivity.glFill
                    );
                }
            }
        } catch (Exception e) {
            Log.d("expRT", "excFace", e);
        }

        try {
            if (My3DActivity.glPoly) {
                for (Polyline polyline : DataSaved.polylines) {
                    if (isLayerEnabled(polyline.getLayer().getLayerName())) {

                        List<PointF> screenPoints = new ArrayList<>();

                        for (Point3D v : polyline.getVertices()) {
                            screenPoints.add(worldToScreen(v.getX(), v.getY()));
                        }

                        Draw3DPolyline.drawScreen(
                                paint,
                                canvas,
                                screenPoints,
                                myParseColor(polyline.getLineColor())
                        );
                    }
                }


                if (DataSaved.isAutoSnap == 2
                        && DataSaved.selectedPoly != null
                        && DataSaved.selectedPoly.getLayer() != null
                        && DataSaved.selectedPoly.getLayer().isEnable()) {

                    List<PointF> selectedScreenPoints = new ArrayList<>();
                    for (Point3D v : DataSaved.selectedPoly.getVertices()) {
                        selectedScreenPoints.add(worldToScreen(v.getX(), v.getY()));
                    }

                    DrawSelectedPolyline.drawScreen(
                            canvas,
                            paint,
                            selectedScreenPoints,
                            myParseColor(DataSaved.selectedPoly.getLineColor())
                    );

                    if (DataSaved.line_Offset != 0
                            && DataSaved.selectedPoly_OFFSET != null
                            && DataSaved.selectedPoly_OFFSET.getVertices() != null) {

                        List<PointF> offsetScreenPoints = new ArrayList<>();
                        for (Point3D v : DataSaved.selectedPoly_OFFSET.getVertices()) {
                            offsetScreenPoints.add(worldToScreen(v.getX(), v.getY()));
                        }

                        DrawSelectedPolyline.drawScreen(
                                canvas,
                                paint,
                                offsetScreenPoints,
                                MyColorClass.colorConstraint
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
                    if (!isLayerEnabled(point.getLayer().getLayerName())) continue;

                    if (isXMLPoint) {
                        col = myParseColor(Color.WHITE);
                    } else {
                        col = myParseColor(point.getLayer().getColorState());
                    }

                    PointF p = worldToScreen(point.getX(), point.getY());

                    DrawDXFPoint.drawScreen(
                            canvas,
                            paint,
                            p.x,
                            p.y,
                            col
                    );
                }
            }
        } catch (Exception e) {
            Log.d("expRT", "excPT", e);
        }

        try {
            if (My3DActivity.glText) {
                int col;
                for (DxfText dxfText : DataSaved.filteredDxfTexts) {
                    if (!isLayerEnabled(dxfText.getLayer().getLayerName())) continue;

                    if (isXMLPoint) {
                        col = myParseColor(Color.WHITE);
                    } else {
                        col = myParseColor(dxfText.getLayer().getColorState());
                    }

                    PointF p = worldToScreen(dxfText.getX(), dxfText.getY());

                    DrawDXFText.drawScreen(
                            canvas,
                            paint,
                            dxfText.getText(),
                            p.x,
                            p.y,
                            col
                    );
                }
            }
        } catch (Exception e) {
            Log.d("expRT", "excTxT", e);
        }
        if (My3DActivity.glPoly) {
            try {

                for (Polyline_2D polyline2D : DataSaved.polylines_2D) {
                    if (isLayerEnabled(polyline2D.getLayer().getLayerName())) {

                        List<PointF> screenPoints = new ArrayList<>();

                        for (Point3D v : polyline2D.getVertices()) {
                            screenPoints.add(worldToScreen(v.getX(), v.getY()));
                        }

                        Draw2DPolyline.drawScreen(
                                paint,
                                canvas,
                                screenPoints,
                                myParseColor(polyline2D.getLineColor()),
                                polyline2D.isClosed()
                        );
                    }

                }
            } catch (Exception e) {
                Log.d("expRT", "excPoly2D");
            }

            try {
                for (Arc arc : DataSaved.arcs) {
                    if (!isLayerEnabled(arc.getLayer().getLayerName())) continue;

                    PointF center = worldToScreen(
                            arc.getCenter().getX(),
                            arc.getCenter().getY()
                    );

                    PointF edge = worldToScreen(
                            arc.getCenter().getX() + arc.getRadius(),
                            arc.getCenter().getY()
                    );

                    float dx = edge.x - center.x;
                    float dy = edge.y - center.y;
                    float radiusPx = (float) Math.sqrt(dx * dx + dy * dy);

                    int color = isXML
                            ? myParseColor(AutoCADColor.getColor(String.valueOf(arc.getLayer().getColorState())))
                            : myParseColor(arc.getLayer().getColorState());

                    DrawArcs.drawScreen(
                            canvas,
                            paint,
                            center.x,
                            center.y,
                            radiusPx,
                            arc.getStartAngle(),
                            arc.getEndAngle(),
                            color
                    );
                }
            } catch (Exception e) {
                Log.d("expRT", "arcs");
            }

            try {
                for (Line line : DataSaved.lines_2D) {
                    if (isLayerEnabled(line.getLayer().getLayerName())) {

                        PointF p1 = worldToScreen(line.getStart().getX(), line.getStart().getY());
                        PointF p2 = worldToScreen(line.getEnd().getX(), line.getEnd().getY());

                        DrawLines.drawScreen(
                                canvas,
                                paint,
                                p1,
                                p2,
                                myParseColor(line.getColor())
                        );
                    }
                }
            } catch (Exception e) {
                Log.d("expRT", "lines");
            }

            try {
                for (Circle circle : DataSaved.circles) {
                    if (isLayerEnabled(circle.getLayer().getLayerName())) {
                        PointF center = worldToScreen(circle.getCenter().getX(), circle.getCenter().getY());
                        PointF edge = worldToScreen(circle.getCenter().getX() + circle.getRadius(), circle.getCenter().getY());

                        float dx = edge.x - center.x;
                        float dy = edge.y - center.y;
                        float radiusPx = (float) Math.sqrt(dx * dx + dy * dy);

                        int color = isXML
                                ? myParseColor(AutoCADColor.getColor(String.valueOf(circle.getLayer().getColorState())))
                                : myParseColor(circle.getLayer().getColorState());

                        DrawCircles.drawScreen(
                                canvas,
                                paint,
                                center.x,
                                center.y,
                                radiusPx,
                                color
                        );
                    }
                }
            } catch (Exception e) {
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
            drawSegment(closestSegment);
        }
    }

    private void drawSegment(Segment segment) {
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
                pX = left_top_bucket.x;
                pY = left_top_bucket.y;
                break;
            case 0:
                pX = (right_bottom_bucket.x + left_top_bucket.x) / 2f;
                pY = left_top_bucket.y;
                break;
            case 1:
                pX = right_bottom_bucket.x;
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // In 2D il touch master è il GL overlay.
        if (My3DActivity.glVista3d == 0) {
            return false;
        }

        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        int pointerIndex = event.findPointerIndex(activePointerId);

        if (pointerIndex == -1) {
            return true;
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

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            offsetX -= (float) (distanceX / DataSaved.scale_Factor3D);
            offsetY -= (float) (distanceY / DataSaved.scale_Factor3D);
            invalidate();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            offsetX = 0;
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            return super.onSingleTapUp(e);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            DataSaved.scale_Factor3D *= detector.getScaleFactor();
            DataSaved.scale_Factor3D = Math.max(0.05f, Math.min(DataSaved.scale_Factor3D, 10.0f));
            invalidate();
            return true;
        }
    }

    private int myParseColor(int color) {
        try {
            String s = Integer.toHexString(color);

            if (DataSaved.temaSoftware == 0) {
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
                case 2:
                    color = Color.BLACK;
                    break;
            }
        }
        return color;
    }

    private boolean isLayerEnabled(String layerName) {
        if (layerName == null || layerName.isEmpty()) {
            return false;
        }

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

        return false;
    }

    public static boolean isTouchNearSegment(float touchX, float touchY, CanvasSegment segment, float tolerance) {
        float dist = (float) new DistToLine(
                touchX,
                touchY,
                segment.x1,
                segment.y1,
                segment.x2,
                segment.y2
        ).getLinedistance();

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

    private void drawArrowTriangle(Canvas canvas, float cx, float cy, float ux, float uy, float size) {
        float px = -uy;
        float py = ux;
        float h = size;
        float w = size * 0.6f;

        Path p = new Path();

        p.moveTo(cx + ux * h, cy + uy * h);
        p.lineTo(cx - ux * h * 0.3f + px * w, cy - uy * h * 0.3f + py * w);
        p.lineTo(cx - ux * h * 0.3f - px * w, cy - uy * h * 0.3f - py * w);
        p.close();

        canvas.drawPath(p, paint);
    }

    public static void syncGlFromTopView(MyGLSurfaceView glSurfaceView) {
        if (glSurfaceView == null) return;
        if (glSurfaceView.getWidth() == 0 || glSurfaceView.getHeight() == 0) return;
        if (My3DActivity.glVista3d != 0) return;

        float safeScale = Math.max((float) DataSaved.scale_Factor3D, 0.001f);
        MyGLRenderer.scale = safeScale;

        float viewW = glSurfaceView.getWidth();
        float viewH = glSurfaceView.getHeight();

        float ratio = viewW / viewH;
        float orthoHalfHeight = MyGLRenderer.orthoBaseSize / safeScale;
        float orthoHalfWidth = orthoHalfHeight * ratio;

        float effectiveScreenDx = Top_View_DXF.offsetX * safeScale;
        float effectiveScreenDy = Top_View_DXF.offsetY * safeScale;

        MyGLRenderer.panX = (effectiveScreenDx / viewW) * (2f * orthoHalfWidth);
        MyGLRenderer.panY = -(effectiveScreenDy / viewH) * (2f * orthoHalfHeight);

        glSurfaceView.requestRender();
    }

    private float getBucketAnchorX() {
        return getWidth() * 0.5f;
    }

    private float getBucketAnchorY() {
        return getHeight() * 0.75f;
    }

    private void syncTopViewFromGl() {
        if (glSurfaceView == null) return;
        if (glSurfaceView.getWidth() == 0 || glSurfaceView.getHeight() == 0) return;
        if (My3DActivity.glVista3d != 0) return;

        float viewW = glSurfaceView.getWidth();
        float viewH = glSurfaceView.getHeight();

        float safeScale = Math.max(MyGLRenderer.scale, 0.001f);
        float ratio = viewW / viewH;
        float orthoHalfHeight = MyGLRenderer.orthoBaseSize / safeScale;
        float orthoHalfWidth = orthoHalfHeight * ratio;

        float screenDx = (MyGLRenderer.panX / (2f * orthoHalfWidth)) * viewW;
        float screenDy = -(MyGLRenderer.panY / (2f * orthoHalfHeight)) * viewH;

        DataSaved.scale_Factor3D = safeScale;
        offsetX = screenDx / safeScale;
        offsetY = screenDy / safeScale;
    }

    public PointF worldToScreen(double worldX, double worldY) {

        if (glSurfaceView == null || glSurfaceView.getWidth() == 0) {
            return new PointF(0, 0);
        }

        float viewW = glSurfaceView.getWidth();
        float viewH = glSurfaceView.getHeight();

        float scale = Math.max(MyGLRenderer.scale, 0.0001f);
        float ratio = viewW / viewH;

        float halfH = MyGLRenderer.orthoBaseSize / scale;
        float halfW = halfH * ratio;

        // riferimento: benna (come GL)
        double dx = worldX - ExcavatorLib.bucketCoord[0];
        double dy = worldY - ExcavatorLib.bucketCoord[1];

        // rotazione macchina (stessa del GL)
        double theta = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);

        double rx = dx * Math.cos(theta) - dy * Math.sin(theta);
        double ry = -(dx * Math.sin(theta) + dy * Math.cos(theta));

        // pan GL
        rx += MyGLRenderer.panX;
        ry += MyGLRenderer.panY;

        // NDC -> pixel
        float screenX = (float) ((rx / (2f * halfW) + 0.5f) * viewW);
        float screenY = (float) ((0.5f - ry / (2f * halfH)) * viewH);

        return new PointF(screenX, screenY);
    }
}
