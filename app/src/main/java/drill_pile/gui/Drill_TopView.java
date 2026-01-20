package drill_pile.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.service.autofill.Dataset;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.stx_dig.R;

import dxf.DrawDXF_Drill_Point;
import dxf.Point3D;
import gui.draw_class.MyColorClass;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;


public class Drill_TopView extends View {

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    Paint paint;
    PointF originPointTool;
    public float scala = 35;
    public static float offsetX;
    public static float offsetY;
    double toolNord, headNord;
    double toolEast, headEast;
    double rotationAngle;
    double rotationAngleBoom;
    float toolX;
    float toolY;
    Canvas canvas;
    float ancorPX, ancorPY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;


    public Drill_TopView(Context context) {

        super(context);

        if (DataSaved.scale_Factor3D == 0) {
            DataSaved.scale_Factor3D = 1f;
        }

        paint = new Paint();
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        if (offsetY == 0) {
            offsetY = 100;
        }


    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        this.canvas = canvas;

        paint.setAntiAlias(true);

        try {

            double extraHeading = NmeaListener.roof_Orientation + DataSaved.offsetSwingExca;
            if (DataSaved.Extra_Heading == 0) {
                extraHeading = 0;
            }
            rotationAngle = Math.toRadians(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
            rotationAngleBoom = Math.toRadians(rotationAngle + extraHeading);

            originPointTool = new PointF(getWidth() * 0.5f, getHeight() * 0.75f);

            canvas.save();
            canvas.scale((float) DataSaved.scale_Factor3D, (float) DataSaved.scale_Factor3D, getWidth() * 0.5f, getHeight() * 0.75f);
            canvas.translate(offsetX, offsetY);

            toolEast = ExcavatorLib.toolEndCoord[0]; // Coordinata REALI EST del primo punto
            toolNord = ExcavatorLib.toolEndCoord[1]; // Coordinata REALI NORD del primo punto
            headEast = ExcavatorLib.coordTool[0];
            headNord = ExcavatorLib.coordTool[1];
            toolX = originPointTool.x;
            toolY = originPointTool.y;



           /*
           DISEGNARE QUI
            */
            drawDrillPoints();
            if(DataSaved.Selected_Point3D_Drill!=null) {
                drawSelectedPoint(DataSaved.Selected_Point3D_Drill);
            }
            // 1) target giallo: drillbit (fisso sullo schermo)
            PointF toolScreen = new PointF(toolX, toolY);
            drawTarget(toolScreen, Color.YELLOW,
                    Math.max(16f, scala * 0.50f),
                    Math.max(9f,  scala * 0.28f),
                    Math.max(11f, scala * 0.38f)
            );

            // 2) target ciano: drillhead (si muove rispetto al tool)
            PointF headScreen = worldToScreen(headEast, headNord);
            drawTarget(headScreen, Color.CYAN,


                    Math.max(18f, scala * 0.55f),
                    Math.max(10f, scala * 0.32f),
                    Math.max(12f, scala * 0.40f)
            );
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(2f, scala * 0.05f));
            paint.setColor(Color.CYAN);
            canvas.drawLine(toolScreen.x, toolScreen.y, headScreen.x, headScreen.y, paint);



            canvas.restore();


            Log.d("ErrorDrill", DataSaved.scale_Factor3D + "");
        } catch (Exception e) {
            Log.e("ErrorDrill", Log.getStackTraceString(e));

        }
    }

    private void drawDrillPoints() {
        try {
            if (DataSaved.filtered_drill_points == null) {
                Log.e("DrillDraw", "filtered_drill_points == null");
                return;
            }
            Log.i("DrillDraw", "filtered_drill_points size = " + DataSaved.filtered_drill_points.size());

            int col = myParseColor(Color.WHITE);

            for (Point3D_Drill point : DataSaved.filtered_drill_points) {
                if (point == null) continue;
                if (point.getHeadX() == null || point.getHeadY() == null) continue; // evita NPE

                DrawDXF_Drill_Point.draw(canvas,
                        paint,
                        point,
                        toolX,
                        toolY,
                        toolEast,
                        toolNord,
                        scala,
                        col,
                        rotationAngle,true
                );
            }
        } catch (Exception e) {
            Log.e("DrillDraw", "Errore drawDrillPoints", e);
        }
    }
    private void drawSelectedPoint(Point3D_Drill point3DDrill){
        int col=Color.BLUE;
        double size=0.3;
        DrawDXF_Drill_Point.draw(canvas,
                paint,
                point3DDrill,
                toolX,
                toolY,
                toolEast,
                toolNord,
                scala,
                col,
                rotationAngle,true,size
        );
    }

    private void drawTarget(PointF c, int color, float rOuter, float rInner, float cross) {
        paint.setAntiAlias(true);

        // anello esterno
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, scala * 0.06f));
        paint.setColor(color);
        canvas.drawCircle(c.x, c.y, rOuter, paint);

        // anello interno
        paint.setStrokeWidth(Math.max(2f, scala * 0.04f));
        canvas.drawCircle(c.x, c.y, rInner, paint);

        // croce
        paint.setStrokeWidth(Math.max(2f, scala * 0.05f));
        canvas.drawLine(c.x - cross, c.y, c.x + cross, c.y, paint);
        canvas.drawLine(c.x, c.y - cross, c.x, c.y + cross, paint);

        // rombo centrale (come il “quadrotto” in foto)
        float s = Math.max(6f, scala * 0.18f);
        Path diamond = new Path();
        diamond.moveTo(c.x, c.y - s);
        diamond.lineTo(c.x + s, c.y);
        diamond.lineTo(c.x, c.y + s);
        diamond.lineTo(c.x - s, c.y);
        diamond.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawPath(diamond, paint);
    }

    private PointF worldToScreen(double worldE, double worldN) {
        double dx = (worldE - toolEast) * scala;
        double dy = (worldN - toolNord) * scala;

        float sx = (float) (toolX + dx * Math.cos(rotationAngle) - dy * Math.sin(rotationAngle));
        float sy = (float) (toolY - dx * Math.sin(rotationAngle) - dy * Math.cos(rotationAngle));
        return new PointF(sx, sy);
    }





    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
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
            DataSaved.scale_Factor3D = Math.max(0.4f, Math.min(DataSaved.scale_Factor3D, 6.5f));
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


}


