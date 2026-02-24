package drill_pile.gui;

import static drill_pile.gui.Drill_Activity.showCroce;
import static services.ReadProjectService.persistAlignmentAB;
import static utils.MyTypes.MAST_FORWARD;
import static utils.MyTypes.MAST_LEFT;
import static utils.MyTypes.MAST_RIGHT;
import static utils.MyTypes.SOLARFARM_MODE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import dxf.DrawDXF_Drill_Point;
import gui.draw_class.MyColorClass;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;
import services.ReadProjectService;


public class Drill_TopView extends View {
    int coloreCingolo;
    private boolean drawMachineSchema = false;
    private float uiRotDeg = 0f;
    private android.graphics.DashPathEffect dashEffect;
    private float lastDashScala = -1f;
    private float targetScale = 1.0f;
    private final android.graphics.Matrix drawMatrix = new android.graphics.Matrix();
    private final android.graphics.Matrix invDrawMatrix = new android.graphics.Matrix();
    private final float[] tmpPt = new float[2];


    private final java.util.ArrayList<ScreenPt> screenPts = new java.util.ArrayList<>();
    private final java.util.HashMap<Long, java.util.ArrayList<Integer>> grid = new java.util.HashMap<>();

    // dimensione cella in px (in schermo) per il picking
    private float gridCellPx = 60f;

    // soglia tap (raggio) in px: scala con zoom
    private float pickRadiusPx = 40f;

    // per capire se rigenerare cache solo quando serve
    private boolean pickCacheDirty = true;

    // salva parametri usati per la cache (così eviti rebuild inutili)
    private float lastScaleFactor = -1;
    private float lastUiRotDeg = Float.NaN;
    private float lastOffsetX = Float.NaN, lastOffsetY = Float.NaN;
    private double lastRot = Double.NaN;
    private double lastToolE = Double.NaN, lastToolN = Double.NaN;

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
    private int colorTarget_Alto = Color.CYAN;
    private int colorTarget_Basso = Color.YELLOW;
    private int colorDashed_Line = Color.BLUE;
    private boolean isBitOnHoleHead = false;
    private int coloreCroce = Color.YELLOW;


    public Drill_TopView(Context context) {

        super(context);

        if (DataSaved.scale_Factor3D == 0) {
            DataSaved.scale_Factor3D = 1f;
        }

        paint = new Paint();
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        if (offsetY == 0) {
            offsetY = 0;
        }
        if(DataSaved.temaSoftware==0){
            coloreCingolo=Color.LTGRAY;
        }else {
            coloreCingolo=Color.DKGRAY;
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
            rotationAngleBoom = (rotationAngle + Math.toRadians(extraHeading));
            rotationAngle = rotationAngleBoom;

            originPointTool = new PointF(getWidth() * 0.5f, getHeight() * 0.7f);

            float pivotX = getWidth() * 0.5f;
            float pivotY = getHeight() * 0.75f;
            float s = (float) DataSaved.scale_Factor3D;


            drawMatrix.reset(); // <<< FIX CRITICO
            // 1) scala (zoom)
            drawMatrix.postScale(s, s, pivotX, pivotY);
// 2) RUOTA TUTTA LA TOPVIEW per il display laterale
//    (questa è la chiave)
            drawMatrix.postRotate(uiRotDeg, pivotX, pivotY);

// 3) pan
            drawMatrix.postTranslate(offsetX, offsetY);

// inversa per il picking
            invDrawMatrix.reset();
            drawMatrix.invert(invDrawMatrix);

            canvas.save();
            canvas.concat(drawMatrix);

            toolEast = ExcavatorLib.toolEndCoord[0]; // Coordinata REALI EST del primo punto
            toolNord = ExcavatorLib.toolEndCoord[1]; // Coordinata REALI NORD del primo punto
            headEast = ExcavatorLib.coordTool[0];
            headNord = ExcavatorLib.coordTool[1];
            toolX = originPointTool.x;
            toolY = originPointTool.y;
            if (DataSaved.isAutoSnap == 2) {
                ensurePickCache();
            }

            drawDrillPoints();
            if (DataSaved.Selected_Point3D_Drill != null) {
                drawSelectedPoint(DataSaved.Selected_Point3D_Drill);
            }

            PointF toolScreen = new PointF(toolX, toolY);
            //0)MCH Frame
            if (drawMachineSchema) {
                // qui metti i tuoi flag reali (da dove li prendi tu)
                boolean mastSX = DataSaved.Drill_Mast_Position.equals(MAST_LEFT); // esempio
                boolean mastFW = DataSaved.Drill_Mast_Position.equals(MAST_FORWARD); // esempio
                boolean mastDX = DataSaved.Drill_Mast_Position.equals(MAST_RIGHT); // esempio

                drawMachineSchemaNearTool(canvas, paint, toolScreen, mastSX, mastFW, mastDX);
            }
            // 1) target giallo: drillbit (fisso sullo schermo)


            drawTarget(toolScreen, colorTarget_Basso,
                    Math.max(18f, scala * 0.55f) * targetScale,
                    Math.max(11f, scala * 0.28f) * targetScale,
                    Math.max(13f, scala * 0.38f) * targetScale,
                    true
            );
            paint.setStrokeWidth(Math.max(1f, scala * 0.002f));
            if (showCroce) {

                float stroke = Math.max(0.8f, scala * 0.001f) * targetScale;
                float arm = 100f * targetScale;

                paint.setColor(coloreCroce);
                paint.setStrokeWidth(stroke);

                // croce
                canvas.drawLine(toolX, toolY, toolX + arm, toolY, paint); // destra
                canvas.drawLine(toolX, toolY, toolX - arm, toolY, paint); // sinistra
                canvas.drawLine(toolX, toolY, toolX, toolY + arm, paint); // dietro
                canvas.drawLine(toolX, toolY, toolX, toolY - arm * 0.5f, paint); // avanti

                // 👇 triangolo in testa (verso avanti = Y negativo)
                drawCrossDirectionTriangle(canvas, paint, toolX, toolY - arm * 0.5f, stroke, targetScale);
            }


            // 2) target ciano: drillhead (si muove rispetto al tool)
            PointF headScreen = worldToScreen(headEast, headNord);
            drawTarget(headScreen, colorTarget_Alto,


                    Math.max(22f, scala * 0.65f) * targetScale,
                    Math.max(0f, scala * 0.00f) * targetScale,
                    Math.max(16f, scala * 0.5f) * targetScale,
                    false


            );


            // dashed più “profondo”: stesso colore ma alpha (trasparente)
            int dashed = Color.argb(155, 0, 255, 255); // ciano trasparente

            // se sei in OK totale, fallo verde anche nella guida

            if (isBitOnHoleHead) {

                dashed = Color.argb(140, 0, 255, 0);
            }

            drawGuidelineExtended(toolScreen, headScreen, colorDashed_Line, dashed, shouldDrawDashedForSelected());
            if (isBitOnHoleHead) {
                paint.setColor(Color.DKGRAY);
                paint.setStrokeWidth(Math.max(0.8f, scala * 0.001f));
                canvas.drawLine(toolX, toolY, toolX + 10f, toolY, paint);//destra
                canvas.drawLine(toolX, toolY, toolX, toolY + 10f, paint);//dietro
                canvas.drawLine(toolX, toolY, toolX - 10f, toolY, paint);//sinistra
                canvas.drawLine(toolX, toolY, toolX, toolY - 10f, paint);//avanti
            }


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
                        rotationAngle, DataSaved.ShowText == 1, uiRotDeg
                );
            }
        } catch (Exception e) {
            Log.e("DrillDraw", "Errore drawDrillPoints", e);
        }
        if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {
            if (DataSaved.alignAId != null && DataSaved.alignBId != null) {
                drawAlignmentAB(canvas, paint,
                        toolX, toolY,
                        toolEast, toolNord,
                        scala, rotationAngle,
                        uiRotDeg);
            }
        }
    }

    private void drawSelectedPoint(Point3D_Drill point3DDrill) {

        double size = 0.3;
        DrawDXF_Drill_Point.drawSelected(canvas,
                paint,
                point3DDrill,
                toolX,
                toolY,
                toolEast,
                toolNord,
                scala,
                rotationAngle, DataSaved.ShowText == 1, size, uiRotDeg
        );
    }

    private void drawTarget(PointF c, int color,
                            float rOuter, float rInner, float cross,
                            boolean drawCross) {

        paint.setAntiAlias(true);

        // anello esterno
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, scala * 0.06f));
        paint.setColor(color);
        canvas.drawCircle(c.x, c.y, rOuter, paint);

        // anello interno
        paint.setStrokeWidth(Math.max(2f, scala * 0.04f));
        canvas.drawCircle(c.x, c.y, rInner, paint);

        // ✖ croce (solo se richiesta)
        if (drawCross) {
            paint.setStrokeWidth(Math.max(2f, scala * 0.05f));
            canvas.drawLine(c.x - cross, c.y, c.x + cross, c.y, paint);
            canvas.drawLine(c.x, c.y - cross, c.x, c.y + cross, paint);
        }

        // rombo centrale
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

        // 1) sempre passare l’evento ai detector
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        // 2) gestisci activePointerId in modo corretto (senza bloccare)
        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = event.getPointerId(0);
                lastTouchX = event.getX(0);
                lastTouchY = event.getY(0);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                int pointerId = event.getPointerId(event.getActionIndex());
                if (pointerId == activePointerId) {
                    int newIndex = (event.getPointerCount() > 1) ? (event.getActionIndex() == 0 ? 1 : 0) : 0;
                    activePointerId = event.getPointerId(newIndex);
                    lastTouchX = event.getX(newIndex);
                    lastTouchY = event.getY(newIndex);
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                activePointerId = INVALID_POINTER_ID;
                break;
            }
        }

        return true;
    }


    // Implementazione della classe interna per il trascinamento
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            if (DataSaved.isAutoSnap != 2 && !DataSaved.isDefiningAB) {
                return; // picking attivo solo in manuale
            }

            Point3D_Drill hit = pickPoint(e.getX(), e.getY());
            if (hit == null) return;

            // --- DEFINIZIONE AB ---
            // --- DEFINIZIONE AB ---
            if (DataSaved.isDefiningAB) {

                String key = buildPointKey(hit); // <-- ROW-ID se esiste, altrimenti solo ID
                if (key == null || key.isEmpty()) return;

                // 1° punto -> A
                if (DataSaved.alignAId == null || DataSaved.alignAId.trim().isEmpty()) {
                    DataSaved.alignAId = key;

                    // feedback
                    // new CustomToast(getContext(), "A set: " + key).show_alert();

                    invalidate();
                    return;
                }

                // 2° punto -> B (evita A==B)
                if (key.equalsIgnoreCase(DataSaved.alignAId.trim())) {
                    // new CustomToast(getContext(), "Pick a different point for B").show_alert();
                    return;
                }

                DataSaved.alignBId = key;

                // Persistenza + uscita modalità
                persistAlignmentAB(DataSaved.alignAId, DataSaved.alignBId);

                DataSaved.isDefiningAB = false;
                DataSaved.isAutoSnap = Drill_Activity.previousState;
                // new CustomToast(getContext(), "Alignment set: A=" + DataSaved.alignAId + " B=" + DataSaved.alignBId).show_alert();
                invalidate();
                return;
            }


            // --- COMPORTAMENTO STANDARD: selezione punto ---
            if (isSamePoint(hit, DataSaved.Selected_Point3D_Drill)) {
                DataSaved.Selected_Point3D_Drill = null;   // toggle off
            } else {
                DataSaved.Selected_Point3D_Drill = hit;
            }
            invalidate();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // distanceX è quanto è "scorso" il contenuto, quindi per seguire il dito va sottratto
            offsetX -= distanceX;
            offsetY -= distanceY;
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
            DataSaved.scale_Factor3D = Math.max(0.1f, Math.min(DataSaved.scale_Factor3D, 6.5f));
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

    public void setColorTarget_Alto(int colorTarget_Alto) {
        this.colorTarget_Alto = colorTarget_Alto;
    }

    public void setColorTarget_Basso(int colorTarget_Basso) {
        this.colorTarget_Basso = colorTarget_Basso;
    }

    public void setColorDashed_Line(int colorDashed_Line) {
        this.colorDashed_Line = colorDashed_Line;
    }

    public void setBitOnHoleHead(boolean isBitOnHoleHead) {
        this.isBitOnHoleHead = isBitOnHoleHead;
    }

    public void setColoreCroce(int coloreCroce) {
        this.coloreCroce = coloreCroce;
    }

    // --- Picking cache ---
    private static class ScreenPt {
        float x, y;
        int index; // indice in filtered_drill_points
    }

    private static long key(int cx, int cy) {
        return (((long) cx) << 32) ^ (cy & 0xffffffffL);
    }

    private int cellX(float x) {
        return (int) Math.floor(x / gridCellPx);
    }

    private int cellY(float y) {
        return (int) Math.floor(y / gridCellPx);
    }

    private void ensurePickCache() {
        if (DataSaved.filtered_drill_points == null) return;

        boolean changed =
                lastScaleFactor != DataSaved.scale_Factor3D ||
                        lastRot != rotationAngle ||
                        lastToolE != toolEast ||
                        lastToolN != toolNord ||
                        lastUiRotDeg != uiRotDeg;     // <--- AGGIUNGI;

        if (!pickCacheDirty && !changed) return;
        lastUiRotDeg = uiRotDeg;
        pickCacheDirty = false;
        lastScaleFactor = (float) DataSaved.scale_Factor3D;
        lastOffsetX = offsetX;
        lastOffsetY = offsetY;
        lastRot = rotationAngle;
        lastToolE = toolEast;
        lastToolN = toolNord;

        // tuning soglie: aumentano un po' con zoom, ma restano in px (non world)
        pickRadiusPx = (float) Math.max(18f, 30f * DataSaved.scale_Factor3D);
        gridCellPx = (float) Math.max(40f, 70f * DataSaved.scale_Factor3D);

        screenPts.clear();
        grid.clear();

        // build: punti in coordinate schermo (post-transform canvas.translate/scale)
        // NOTA: qui stai già disegnando in un canvas scalato/traslato, ma pick usa MotionEvent in px "view".
        // Quindi dobbiamo mappare correttamente: il modo più semplice è convertire il tap in "canvas space"
        // (vedi pickPoint). Qui memorizziamo in "canvas space" coerente con worldToScreen.
        // worldToScreen già restituisce coordinate nello stesso spazio in cui disegni (dopo translate/scale nel canvas).
        // Quindi è OK memorizzare worldToScreen.

        for (int i = 0; i < DataSaved.filtered_drill_points.size(); i++) {
            Point3D_Drill p = DataSaved.filtered_drill_points.get(i);
            if (p == null || p.getHeadX() == null || p.getHeadY() == null) continue;

            // usa le coordinate "head" (o quello che rappresenta il punto in top view)
            double e = p.getHeadX();
            double n = p.getHeadY();

            PointF s = worldToScreen(e, n);

            ScreenPt sp = new ScreenPt();
            sp.x = s.x;
            sp.y = s.y;
            sp.index = i;
            int idx = screenPts.size();
            screenPts.add(sp);

            int cx = cellX(sp.x);
            int cy = cellY(sp.y);
            long k = key(cx, cy);
            java.util.ArrayList<Integer> bucket = grid.get(k);
            if (bucket == null) {
                bucket = new java.util.ArrayList<>();
                grid.put(k, bucket);
            }
            bucket.add(idx); // idx in screenPts
        }
    }

    private Point3D_Drill pickPoint(float touchX_view, float touchY_view) {

        if (DataSaved.isAutoSnap != 2) return null;
        if (DataSaved.filtered_drill_points == null || DataSaved.filtered_drill_points.isEmpty())
            return null;

        // touch in view coords -> local coords (quelle usate da worldToScreen e draw)
        tmpPt[0] = touchX_view;
        tmpPt[1] = touchY_view;
        invDrawMatrix.mapPoints(tmpPt);

        float xLocal = tmpPt[0];
        float yLocal = tmpPt[1];

        ensurePickCache();

        // ora usa xLocal/yLocal per cercare nella grid/cache
        int cx = cellX(xLocal);
        int cy = cellY(yLocal);

        float bestD2 = pickRadiusPx * pickRadiusPx;
        int bestIndex = -1;

        for (int gx = cx - 1; gx <= cx + 1; gx++) {
            for (int gy = cy - 1; gy <= cy + 1; gy++) {
                java.util.ArrayList<Integer> bucket = grid.get(key(gx, gy));
                if (bucket == null) continue;

                for (int b = 0; b < bucket.size(); b++) {
                    ScreenPt sp = screenPts.get(bucket.get(b));
                    float dx = sp.x - xLocal;
                    float dy = sp.y - yLocal;
                    float d2 = dx * dx + dy * dy;
                    if (d2 < bestD2) {
                        bestD2 = d2;
                        bestIndex = sp.index;
                    }
                }
            }
        }

        if (bestIndex >= 0 && bestIndex < DataSaved.filtered_drill_points.size()) {
            return DataSaved.filtered_drill_points.get(bestIndex);
        }
        return null;
    }

    private static boolean isSamePoint(Point3D_Drill a, Point3D_Drill b) {
        if (a == null || b == null) return false;

        // se rowId + id identificano univocamente il punto
        return safeEq(a.getRowId(), b.getRowId()) &&
                safeEq(a.getId(), b.getId());
    }

    private static boolean safeEq(Object x, Object y) {
        return (x == y) || (x != null && x.equals(y));
    }

    private void ensureDashEffect() {
        // pattern proporzionale alla scala, ma con minimi
        float dash = Math.max(10f, scala * 0.25f);
        float gap = Math.max(8f, scala * 0.18f);

        if (dashEffect == null || lastDashScala != scala) {
            dashEffect = new android.graphics.DashPathEffect(new float[]{dash, gap}, 0f);
            lastDashScala = scala;
        }
    }

    private void drawGuidelineExtended(PointF a, PointF b, int solidColor, int dashedColor, boolean drawDashed) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float len = (float) Math.hypot(dx, dy);
        if (len < 1e-3f) return;

        float ux = dx / len;
        float uy = dy / len;

        // quanto estendere: fino a "fuori schermo"
        float W = getWidth();
        float H = getHeight();
        float margin = Math.max(40f, scala * 1.2f);

        // calcolo t massimo per arrivare al bordo in entrambe le direzioni
        float tPos = distanceToRectEdge(b.x, b.y, ux, uy, -margin, -margin, W + margin, H + margin);
        float tNeg = distanceToRectEdge(a.x, a.y, -ux, -uy, -margin, -margin, W + margin, H + margin);

        // punti estesi
        float x2 = b.x + ux * tPos;
        float y2 = b.y + uy * tPos;

        float x0 = a.x - ux * tNeg;
        float y0 = a.y - uy * tNeg;

        // 1) linea piena tra i due target
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(null);
        paint.setColor(solidColor);
        paint.setStrokeWidth(Math.max(2f, scala * 0.05f));
        canvas.drawLine(a.x, a.y, b.x, b.y, paint);
        if (!drawDashed) return;
        // 2) dashed oltre head (b -> esteso)
        ensureDashEffect();
        paint.setPathEffect(dashEffect);
        paint.setColor(colorDashed_Line);
        paint.setStrokeWidth(Math.max(2f, scala * 0.045f));
        canvas.drawLine(b.x, b.y, x2, y2, paint);

        // 3) (opzionale) dashed anche dietro tool (a -> esteso)
        // se non lo vuoi, commenta questo blocco
        paint.setStrokeWidth(Math.max(2f, scala * 0.035f));
        canvas.drawLine(a.x, a.y, x0, y0, paint);

        paint.setPathEffect(null);
    }

    /**
     * Distanza t >= 0 per cui (x + vx*t, y + vy*t) raggiunge il bordo del rettangolo.
     * Ritorna il t massimo entro il rettangolo.
     */
    private float distanceToRectEdge(float x, float y, float vx, float vy,
                                     float left, float top, float right, float bottom) {

        float tMax = Float.POSITIVE_INFINITY;

        if (Math.abs(vx) > 1e-6f) {
            float t1 = (left - x) / vx;
            float t2 = (right - x) / vx;
            float tx = Math.max(t1, t2);
            // in realtà ci serve il primo incrocio "in avanti": prendiamo il più piccolo positivo valido
            float tminPos = minPositive(t1, t2);
            if (tminPos < tMax) tMax = tminPos;
        }
        if (Math.abs(vy) > 1e-6f) {
            float t1 = (top - y) / vy;
            float t2 = (bottom - y) / vy;
            float tminPos = minPositive(t1, t2);
            if (tminPos < tMax) tMax = tminPos;
        }

        // Se qualcosa va storto, fallback a una lunghezza fissa
        if (!Float.isFinite(tMax) || tMax < 0f) {
            return Math.max(getWidth(), getHeight());
        }
        return tMax;
    }

    private float minPositive(float a, float b) {
        boolean ap = a > 0f;
        boolean bp = b > 0f;
        if (ap && bp) return Math.min(a, b);
        if (ap) return a;
        if (bp) return b;
        return Float.POSITIVE_INFINITY;
    }

    private boolean shouldDrawDashedForSelected() {
        Point3D_Drill sel = DataSaved.Selected_Point3D_Drill;
        if (sel == null) return false;

        // Assicurati che headingDeg sia calcolato (se non lo fai già altrove)
        if (sel.getHeadingDeg() == null) {
            sel.recomputeDerived();
        }

        Double h = sel.getHeadingDeg();
        if (h == null) return false;

        double eps = 0.0001; // tolleranza
        double hh = ((h % 360.0) + 360.0) % 360.0; // normalizza 0..360

        // considera "0" anche 360
        return !(Math.abs(hh) < eps || Math.abs(hh - 360.0) < eps);
    }

    public void setTargetScale(float targetScale) {
        this.targetScale = targetScale;
    }

    public void setUiRotationDeg(float deg) {
        uiRotDeg = ((deg % 360f) + 360f) % 360f;

        // consigliato: reset pan per non ritrovarti fuori schermo
        offsetX = 0f;
        offsetY = 0f;

        pickCacheDirty = true;
        invalidate();
    }

    private void drawAlignmentAB(Canvas canvas, Paint paint,
                                 float bucketX, float bucketY,
                                 double bucketEst, double bucketNord,
                                 float scala, double rotationAngle,
                                 float uiDeg) {

        ReadProjectService.AlignmentPair ab =
                ReadProjectService.findAlignmentPoints(
                        DataSaved.drill_points,
                        DataSaved.alignAId,
                        DataSaved.alignBId
                );
        Log.d("AB_DEBUG", "A=" + DataSaved.alignAId + " B=" + DataSaved.alignBId);
        if (ab == null || !ab.isValid()) return;
        if (ab.A.getHeadX() == null || ab.A.getHeadY() == null) return;
        if (ab.B.getHeadX() == null || ab.B.getHeadY() == null) return;

        // --- mondo -> schermo (stessa trasformazione dei punti) ---
        float ax = worldToScreenX(ab.A.getHeadX(), ab.A.getHeadY(), bucketX, bucketEst, bucketNord, scala, rotationAngle);
        float ay = worldToScreenY(ab.A.getHeadX(), ab.A.getHeadY(), bucketY, bucketEst, bucketNord, scala, rotationAngle);

        float bx = worldToScreenX(ab.B.getHeadX(), ab.B.getHeadY(), bucketX, bucketEst, bucketNord, scala, rotationAngle);
        float by = worldToScreenY(ab.B.getHeadX(), ab.B.getHeadY(), bucketY, bucketEst, bucketNord, scala, rotationAngle);

        // --- stile linea (gialla con outline scuro) ---
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        float strokeMain = Math.max(3f, scala * 0.015f);      // scala-based
        float strokeOutline = strokeMain + Math.max(2f, strokeMain * 0.55f);

        // outline scuro
        paint.setStrokeWidth(strokeOutline);
        paint.setColor(MyColorClass.colorConstraint);
        canvas.drawLine(ax, ay, bx, by, paint);

        // linea gialla
        paint.setStrokeWidth(strokeMain);
        paint.setColor(android.graphics.Color.argb(220, 255, 235, 0));
        canvas.drawLine(ax, ay, bx, by, paint);
        drawDirectionTriangle(canvas, paint, ax, ay, bx, by, strokeMain);
        // (opzionale) pallini A/B per chiarezza
        float r = Math.max(6f, strokeMain * 1.1f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(android.graphics.Color.argb(230, 255, 235, 0));
        canvas.drawCircle(ax, ay, r, paint);
        canvas.drawCircle(bx, by, r, paint);
    }

    private float worldToScreenX(double worldE, double worldN,
                                 float bucketX,
                                 double bucketEst, double bucketNord,
                                 float scala, double rotationAngle) {

        double diffX = (worldE - bucketEst) * scala;
        double diffY = (worldN - bucketNord) * scala;
        return (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
    }

    private float worldToScreenY(double worldE, double worldN,
                                 float bucketY,
                                 double bucketEst, double bucketNord,
                                 float scala, double rotationAngle) {

        double diffX = (worldE - bucketEst) * scala;
        double diffY = (worldN - bucketNord) * scala;
        return (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
    }

    private void drawDirectionTriangle(Canvas canvas, Paint paint,
                                       float ax, float ay, float bx, float by,
                                       float strokeMain) {

        float vx = bx - ax;
        float vy = by - ay;
        float len = (float) Math.hypot(vx, vy);
        if (len < 1e-3f) return;

        // unit direction A->B
        float ux = vx / len;
        float uy = vy / len;

        // normal (perp)
        float nx = -uy;
        float ny = ux;

        // centro linea
        float cx = (ax + bx) * 0.5f;
        float cy = (ay + by) * 0.5f;

        // dimensioni triangolo (scalate)
        float triLen = Math.max(16f, strokeMain * 3.2f);   // lunghezza punta
        float triHalfW = Math.max(10f, strokeMain * 2.4f); // metà base

        // punta verso B
        float tipX = cx + ux * triLen;
        float tipY = cy + uy * triLen;

        // base dietro al centro
        float baseCx = cx - ux * triLen * 0.55f;
        float baseCy = cy - uy * triLen * 0.55f;

        float leftX = baseCx + nx * triHalfW;
        float leftY = baseCy + ny * triHalfW;
        float rightX = baseCx - nx * triHalfW;
        float rightY = baseCy - ny * triHalfW;

        // ---- outline scuro ----
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(MyColorClass.colorConstraint);
        android.graphics.Path p = new android.graphics.Path();
        p.moveTo(tipX, tipY);
        p.lineTo(leftX, leftY);
        p.lineTo(rightX, rightY);
        p.close();
        canvas.drawPath(p, paint);

        // ---- fill giallo ----
        paint.setColor(android.graphics.Color.argb(235, 255, 235, 0));
        canvas.drawPath(p, paint);

        // (opzionale) bordo giallo più “pulito”
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, strokeMain * 0.6f));
        paint.setColor(android.graphics.Color.argb(240, 255, 255, 120));
        canvas.drawPath(p, paint);
    }

    private void drawCrossDirectionTriangle(Canvas canvas, Paint paint,
                                            float tipX, float tipY,
                                            float stroke, float scale) {

        // dimensioni triangolo
        float triLen = 20f * scale;
        float triHalfW = 12f * scale;

        // punta già nota (tipX, tipY)
        float baseY = tipY + triLen;

        float leftX = tipX - triHalfW;
        float leftY = baseY;

        float rightX = tipX + triHalfW;
        float rightY = baseY;

        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(tipX, tipY);     // punta
        path.lineTo(leftX, leftY);   // base sx
        path.lineTo(rightX, rightY); // base dx
        path.close();

        paint.setAntiAlias(true);

        // bordo scuro per contrasto
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(android.graphics.Color.argb(170, 0, 0, 0));
        canvas.drawPath(path, paint);

        // riempimento colore croce
        paint.setColor(coloreCroce);
        canvas.drawPath(path, paint);

        // contorno
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, stroke * 0.7f));
        paint.setColor(android.graphics.Color.argb(220, 0, 0, 0));
        canvas.drawPath(path, paint);
    }

    private String buildPointKey(Point3D_Drill p) {
        if (p == null) return null;

        String row = p.getRowId();  // nel tuo getter torna "" se null
        String id = p.getId();

        row = (row == null) ? "" : row.trim();
        id = (id == null) ? "" : id.trim();

        if (id.isEmpty()) return null;

        if (!row.isEmpty()) {
            return row + "-" + id;
        }
        return id;
    }

/// //machine schema


    public void setDrawMachineSchema(boolean v) {
        drawMachineSchema = v;
        invalidate();
    }
    private void drawMachineSchemaNearTool(
            Canvas canvas,
            Paint paint,
            PointF toolScreen,
            boolean mastSX,
            boolean mastFW,
            boolean mastDX
    ) {
        // --- dimensioni base (in px nel canvas già trasformato da drawMatrix) ---
        float s = targetScale;                 // usa la tua scala UI
        float bodyW = 90f * s;
        float bodyH = 85f * s;

        float trackW = 35f * s;
        float gap = -25f * s;
        float trackStroke = 4.5f * s;

        // ingombro totale orizzontale dello schema (cingolo + gap + corpo + gap + cingolo)
        float totalW = bodyW + 2f * (gap + trackW);
        // un margine dal target
        float margin = 35f * s;

        // offset macchina rispetto al target (frame DISPLAY: X destra, Y giù)
        float offX = 0f;
        float offY = 0f;

        // Priorità: se più flag attivi, sommiamo (es: SX+FW => diagonale).
        // Se invece sono mutuamente esclusivi nel tuo UI, andrà comunque bene.
        if (mastSX) offX += (totalW * 0.5f + margin);   // macchina a DESTRA del target
        if (mastDX) offX -= (totalW * 0.5f + margin);   // macchina a SINISTRA del target
        if (mastFW) offY += (bodyH * 0.5f + 70f * s);   // macchina SOTTO al target (come la tua immagine)

        // --- pivot schema = punto blu (fulcro dell’icona) ---
        // Il blu nella tua immagine è vicino al bordo alto del corpo (~20% dall’alto).
        // Quindi: se conosci pivot, il corpo lo piazziamo relativo a pivot.
        float pivotX = toolScreen.x + offX;
        float pivotY = toolScreen.y + offY;

        // corpo: pivot ~20% dall’alto
        float bodyTop = pivotY - bodyH * 0.20f;
        float bodyLeft = pivotX - bodyW * 0.5f;
        float bodyRight = bodyLeft + bodyW;
        float bodyBottom = bodyTop + bodyH;

        // cingoli (stessa altezza del corpo + extra)
        float trackH = bodyH + 55f * s;
        float trackTop = bodyTop - 45f * s;
        float trackBottom = trackTop + trackH;

        float leftTrackLeft = bodyLeft - gap - trackW;
        float leftTrackRight = leftTrackLeft + trackW;

        float rightTrackLeft = bodyRight + gap;
        float rightTrackRight = rightTrackLeft + trackW;

        float corner = 10f * s;

        // --- salva stato paint ---
        Paint.Style oldStyle = paint.getStyle();
        int oldColor = paint.getColor();
        float oldStroke = paint.getStrokeWidth();
        Paint.Cap oldCap = paint.getStrokeCap();

        paint.setAntiAlias(true);

        // --- cingoli neri ---
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(coloreCingolo);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(trackStroke);

        canvas.drawRoundRect(leftTrackLeft, trackTop, leftTrackRight, trackBottom, corner, corner, paint);
        canvas.drawRoundRect(rightTrackLeft, trackTop, rightTrackRight, trackBottom, corner, corner, paint);

        // --- corpo macchina ---
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(MyColorClass.colorStick);
        canvas.drawRoundRect(bodyLeft, bodyTop, bodyRight, bodyBottom,5f,5f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(coloreCingolo);
        canvas.drawRoundRect(bodyLeft, bodyTop, bodyRight, bodyBottom,5f,5f, paint);

        // --- punto blu (fulcro icona) ---
        //float dotR = 12f * s;
        //paint.setColor(Color.BLUE);
        //canvas.drawCircle(pivotX, pivotY, dotR, paint);

        // --- ripristina paint ---
        paint.setStyle(oldStyle);
        paint.setColor(oldColor);
        paint.setStrokeWidth(oldStroke);
        paint.setStrokeCap(oldCap);
    }
    private float metersToPx(float meters) {
        // 1 metro nel mondo = quanto vale in pixel sul canvas con la drawMatrix corrente?
        // Metodo robusto: trasformo un vettore (0,0)->(1,0) con la matrice.
        float[] pts = new float[]{0f, 0f, 1f, 0f};
        drawMatrix.mapPoints(pts);
        float pxPerMeter = (float) Math.hypot(pts[2] - pts[0], pts[3] - pts[1]);
        return meters * pxPerMeter;
    }
}


