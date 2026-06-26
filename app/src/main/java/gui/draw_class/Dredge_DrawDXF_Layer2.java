package gui.draw_class;

import static packexcalib.exca.ExcavatorLib.bucketCoord;
import static packexcalib.exca.ExcavatorLib.coordB1;
import static packexcalib.exca.ExcavatorLib.coordST;
import static packexcalib.exca.ExcavatorLib.coordinateDY;
import static packexcalib.exca.Sensors_Decoder_Dredge.Angolo_Attrezzo_Dredge;
import static services.TriangleService.tutteLinee;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dxf.Point2D;
import packexcalib.exca.DataSaved;
import packexcalib.exca.DredgeLib;
import services.TriangleService;

public class Dredge_DrawDXF_Layer2 extends View {
    private static final float PIVOT_X = 0.50f;
    private static final float PIVOT_Y = 0.65f;
    private static final double EPS = 0.000001d;

    private float mFixedXX;
    private final Paint paint;
    private final Paint dashedPaint;
    private int scala;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private float lastTouchX;
    private float lastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;
    public static float offsetX;
    public static float offsetY;

    public Dredge_DrawDXF_Layer2(Context context) {
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
        dashedPaint.setAntiAlias(true);

        try {
            scala = (int) (85 + DataSaved.scale_FactorVista2D);
            canvas.scale((float) DataSaved.scale_FactorVista2D,
                    (float) DataSaved.scale_FactorVista2D,
                    getWidth() * 0.5f,
                    getHeight() * 0.65f);
            canvas.translate(offsetX, offsetY);

            PointF toolPoint = new PointF(getWidth() * PIVOT_X, getHeight() * PIVOT_Y);

            drawGround(canvas, 2, 3);

            ToolDrawPoints toolDrawPoints = drawDredgeRopeAndClamshell(canvas, toolPoint, true);
            if (toolDrawPoints != null) {
                drawConstraintAndReference(canvas, toolDrawPoints);
            } else {
                mFixedXX = toolPoint.x;
            }

            drawAutoSnap(canvas);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Layer2 = vista frontale / cabina.
     * L'asse orizzontale della view e quello laterale rispetto alla heading del boom draga.
     * DredgeLib.bucketCoord resta sempre il toolpoint esatto e coincide col pivot della view.
     */
    private ToolDrawPoints drawDredgeRopeAndClamshell(Canvas canvas, PointF toolPoint, boolean frontView) {
        if (!isValid(bucketCoord)) return null;

        BoomAxes axes = getBoomAxes();
        PointF head = projectToCanvas(coordST, toolPoint, axes, frontView);
        PointF ropeTop = projectToCanvas(coordB1, toolPoint, axes, frontView);

        float configuredHeight = (float) Math.max(0.0d, DataSaved.Altezza_Attrezzo) * scala;
        configuredHeight = Math.max(configuredHeight, 0.80f * scala);

        if (!isFinite(head) || distance(head, toolPoint) < Math.max(0.15f * scala, 8f)) {
            head = new PointF(toolPoint.x, toolPoint.y - configuredHeight);
        }

        if (!isFinite(ropeTop) || distance(ropeTop, head) < Math.max(0.20f * scala, 12f)) {
            PointF down = normalizedVector(head, toolPoint, new PointF(0f, 1f));
            ropeTop = new PointF(head.x - down.x * configuredHeight * 2.0f,
                    head.y - down.y * configuredHeight * 2.0f);
        }

        PointF down = normalizedVector(head, toolPoint, new PointF(0f, 1f));
        PointF lateral = new PointF(-down.y, down.x);

        float height = distance(head, toolPoint);
        if (height < configuredHeight * 0.55f) {
            height = configuredHeight;
            head = new PointF(toolPoint.x - down.x * height, toolPoint.y - down.y * height);
        }

        drawRope(canvas, ropeTop, head, height);
        return drawClamshell(canvas, head, toolPoint, down, lateral, height);
    }

    private void drawRope(Canvas canvas, PointF ropeTop, PointF head, float toolHeight) {
        float oldStroke = paint.getStrokeWidth();
        Paint.Style oldStyle = paint.getStyle();
        Paint.Cap oldCap = paint.getStrokeCap();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(Math.max(0.10f * toolHeight, 7f));
        paint.setColor(Color.rgb(45, 45, 45));
        canvas.drawLine(ropeTop.x, ropeTop.y, head.x, head.y, paint);

        paint.setStrokeWidth(Math.max(0.045f * toolHeight, 3f));
        paint.setColor(Color.rgb(90, 90, 90));
        canvas.drawLine(ropeTop.x, ropeTop.y, head.x, head.y, paint);

        paint.setStrokeWidth(oldStroke);
        paint.setStyle(oldStyle);
        paint.setStrokeCap(oldCap);
    }

    private ToolDrawPoints drawClamshell(Canvas canvas,
                                         PointF head,
                                         PointF bottom,
                                         PointF down,
                                         PointF lateral,
                                         float height) {
        float totalWidth = height * 0.75f;
        float halfHeadWidth = totalWidth * 0.23f;
        float halfBellyWidth = totalWidth * 0.50f;
        float halfLipWidth = totalWidth * 0.44f;
        float shellThroat = height * 0.07f;
        float headBlockH = height * 0.16f;

        float splitOpen = (float) Math.min(Math.abs(Angolo_Attrezzo_Dredge), 40d) / 40f;
        float split = clamp(height * (0.02f + 0.10f * splitOpen), height * 0.02f, height * 0.18f);

        int bucketColor = MyColorClass.colorBucket;
        int bucketDark = darken(bucketColor, 0.62f);

        PointF headBlockTop = add(head, scale(down, -headBlockH * 0.20f));
        PointF headBlockBottom = add(head, scale(down, headBlockH * 0.80f));
        drawOrientedBox(canvas,
                midpoint(headBlockTop, headBlockBottom),
                lateral,
                down,
                halfHeadWidth * 2.30f,
                headBlockH,
                bucketColor,
                bucketDark);

        PointF shoulder = add(head, scale(down, height * 0.28f));
        PointF belly = add(head, scale(down, height * 0.66f));
        PointF lip = bottom;

        Path leftShell = makeShellPath(true, head, shoulder, belly, lip, down, lateral,
                halfHeadWidth, halfBellyWidth, halfLipWidth, shellThroat, split);
        Path rightShell = makeShellPath(false, head, shoulder, belly, lip, down, lateral,
                halfHeadWidth, halfBellyWidth, halfLipWidth, shellThroat, split);

        Paint.Style oldStyle = paint.getStyle();
        float oldStroke = paint.getStrokeWidth();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bucketColor);
        canvas.drawPath(leftShell, paint);
        canvas.drawPath(rightShell, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2.0f, height * 0.018f));
        paint.setColor(bucketDark);
        canvas.drawPath(leftShell, paint);
        canvas.drawPath(rightShell, paint);

        PointF hingeLeft = add(head, scale(lateral, -(halfHeadWidth + split)));
        PointF hingeRight = add(head, scale(lateral, halfHeadWidth + split));
        paint.setStrokeWidth(Math.max(3.0f, height * 0.025f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(hingeLeft.x, hingeLeft.y, hingeRight.x, hingeRight.y, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);

        paint.setStyle(oldStyle);
        paint.setStrokeWidth(oldStroke);

        return new ToolDrawPoints(
                add(lip, scale(lateral, -halfLipWidth)),
                lip,
                add(lip, scale(lateral, halfLipWidth))
        );
    }

    private Path makeShellPath(boolean left,
                               PointF head,
                               PointF shoulderCenter,
                               PointF bellyCenter,
                               PointF lipCenter,
                               PointF down,
                               PointF lateral,
                               float halfHeadWidth,
                               float halfBellyWidth,
                               float halfLipWidth,
                               float shellThroat,
                               float split) {
        float sign = left ? -1f : 1f;

        PointF hinge = add(head, scale(lateral, sign * (halfHeadWidth + split)));
        PointF innerTop = add(head, scale(lateral, sign * (split + shellThroat * 0.25f)));
        PointF outerShoulder = add(shoulderCenter, scale(lateral, sign * (halfBellyWidth * 0.68f)));
        PointF outerBelly = add(bellyCenter, scale(lateral, sign * halfBellyWidth));
        PointF outerLip = add(lipCenter, scale(lateral, sign * halfLipWidth));
        PointF innerLip = add(lipCenter, scale(lateral, sign * shellThroat));
        PointF innerBelly = add(bellyCenter, scale(lateral, sign * (shellThroat * 1.25f)));
        PointF innerShoulder = add(shoulderCenter, scale(lateral, sign * (shellThroat * 0.90f)));

        Path path = new Path();
        path.moveTo(hinge.x, hinge.y);
        path.lineTo(outerShoulder.x, outerShoulder.y);
        path.lineTo(outerBelly.x, outerBelly.y);
        path.lineTo(outerLip.x, outerLip.y);
        path.lineTo(innerLip.x, innerLip.y);
        path.lineTo(innerBelly.x, innerBelly.y);
        path.lineTo(innerShoulder.x, innerShoulder.y);
        path.lineTo(innerTop.x, innerTop.y);
        path.close();
        return path;
    }

    private void drawOrientedBox(Canvas canvas,
                                 PointF center,
                                 PointF axisX,
                                 PointF axisY,
                                 float width,
                                 float height,
                                 int fillColor,
                                 int strokeColor) {
        PointF x = normalize(axisX, new PointF(1f, 0f));
        PointF y = normalize(axisY, new PointF(0f, 1f));
        float hw = width * 0.5f;
        float hh = height * 0.5f;

        PointF p1 = add(add(center, scale(x, -hw)), scale(y, -hh));
        PointF p2 = add(add(center, scale(x, hw)), scale(y, -hh));
        PointF p3 = add(add(center, scale(x, hw)), scale(y, hh));
        PointF p4 = add(add(center, scale(x, -hw)), scale(y, hh));

        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.lineTo(p4.x, p4.y);
        path.close();

        Paint.Style oldStyle = paint.getStyle();
        float oldStroke = paint.getStrokeWidth();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2.0f, height * 0.10f));
        paint.setColor(strokeColor);
        canvas.drawPath(path, paint);
        paint.setStyle(oldStyle);
        paint.setStrokeWidth(oldStroke);
    }

    private PointF projectToCanvas(double[] world, PointF origin, BoomAxes axes, boolean frontView) {
        if (!isValid(world) || !isValid(bucketCoord)) return null;

        double dx = world[0] - bucketCoord[0];
        double dy = world[1] - bucketCoord[1];
        double dz = world[2] - bucketCoord[2];

        double localX;
        if (frontView) {
            localX = dx * axes.sideX + dy * axes.sideY;
        } else {
            localX = -(dx * axes.forwardX + dy * axes.forwardY);
        }

        return new PointF(
                origin.x + (float) (localX * scala),
                origin.y - (float) (dz * scala)
        );
    }

    private BoomAxes getBoomAxes() {
        double fx = 1.0d;
        double fy = 0.0d;

        if (isValid(coordinateDY) && isValid(coordB1)) {
            fx = coordB1[0] - coordinateDY[0];
            fy = coordB1[1] - coordinateDY[1];
        } else if (isValid(bucketCoord) && isValid(coordB1)) {
            fx = coordB1[0] - bucketCoord[0];
            fy = coordB1[1] - bucketCoord[1];
        }

        double len = Math.sqrt(fx * fx + fy * fy);
        if (len < EPS) {
            fx = 1.0d;
            fy = 0.0d;
            len = 1.0d;
        }

        fx /= len;
        fy /= len;

        return new BoomAxes(fx, fy, -fy, fx);
    }

    private void drawConstraintAndReference(Canvas canvas, ToolDrawPoints points) {
        PointF ref = points.center;
        boolean offGrid = TriangleService.ctOffGrid;
        double quota = TriangleService.quota3D_CT;

        switch (DataSaved.bucketEdge) {
            case -1:
                ref = points.left;
                offGrid = TriangleService.ltOffGrid;
                quota = TriangleService.quota3D_SX;
                break;
            case 1:
                ref = points.right;
                offGrid = TriangleService.rtOffGrid;
                quota = TriangleService.quota3D_DX;
                break;
            case 0:
            default:
                ref = points.center;
                offGrid = TriangleService.ctOffGrid;
                quota = TriangleService.quota3D_CT;
                break;
        }

        mFixedXX = ref.x;

        Paint.Style oldStyle = paint.getStyle();
        float oldStroke = paint.getStrokeWidth();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(3f);
        paint.setColor(MyColorClass.colorConstraint);
        if (!offGrid) {
            canvas.drawLine(ref.x, ref.y + 1f, ref.x, ref.y + (float) quota * scala + 1f, paint);
        }

        paint.setColor(DataSaved.isLowerEdge ? Color.RED : Color.BLUE);
        canvas.drawCircle(ref.x, ref.y, 8f, paint);
        paint.setStyle(oldStyle);
        paint.setStrokeWidth(oldStroke);
    }

    private void drawGround(Canvas canvas, int idx0, int idx1) {
        float fixedX = getWidth() * PIVOT_X;
        float fixedY = getHeight() * PIVOT_Y;
        List<Point2D> sortedPoints0 = getSortedLine(idx0);
        List<Point2D> sortedPoints1 = getSortedLine(idx1);

        Paint.Style oldStyle = paint.getStyle();
        float oldStroke = paint.getStrokeWidth();
        paint.setStrokeWidth(4f);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < sortedPoints0.size(); i++) {
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
            } catch (Exception ignored) {
            }
        }

        for (int i = 0; i < sortedPoints1.size(); i++) {
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
            } catch (Exception ignored) {
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
        if (!sortedPoints0.isEmpty() && !sortedPoints1.isEmpty()) {
            Point2D lastPoint0 = sortedPoints0.get(0);
            Point2D firstPoint1 = sortedPoints1.get(0);
            float xLast0 = fixedX - (float) lastPoint0.getX() * scala;
            float yLast0 = fixedY - (float) lastPoint0.getY() * scala;
            float xFirst1 = fixedX + (float) firstPoint1.getX() * scala;
            float yFirst1 = fixedY - (float) firstPoint1.getY() * scala;
            fillGroundSegment(canvas, xLast0, yLast0, xFirst1, yFirst1, mLevel);
        }

        for (int i = 0; i < sortedPoints0.size() - 1; i++) {
            try {
                float x1 = fixedX - (float) sortedPoints0.get(i).getX() * scala;
                float y1 = fixedY - (float) sortedPoints0.get(i).getY() * scala;
                float x2 = fixedX - (float) sortedPoints0.get(i + 1).getX() * scala;
                float y2 = fixedY - (float) sortedPoints0.get(i + 1).getY() * scala;
                fillGroundSegment(canvas, x1, y1, x2, y2, mLevel);
            } catch (Exception ignored) {
            }
        }

        for (int i = 0; i < sortedPoints1.size() - 1; i++) {
            try {
                float x1 = fixedX + (float) sortedPoints1.get(i).getX() * scala;
                float y1 = fixedY - (float) sortedPoints1.get(i).getY() * scala;
                float x2 = fixedX + (float) sortedPoints1.get(i + 1).getX() * scala;
                float y2 = fixedY - (float) sortedPoints1.get(i + 1).getY() * scala;
                fillGroundSegment(canvas, x1, y1, x2, y2, mLevel);
            } catch (Exception ignored) {
            }
        }

        paint.setStyle(oldStyle);
        paint.setStrokeWidth(oldStroke);
    }

    private void fillGroundSegment(Canvas canvas, float x1, float y1, float x2, float y2, float bottomY) {
        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x2, bottomY);
        path.lineTo(x1, bottomY);
        path.close();
        paint.setColor(Color.argb(128, 128, 128, 128));
        canvas.drawPath(path, paint);
    }

    private List<Point2D> getSortedLine(int index) {
        List<Point2D> out = new ArrayList<>();
        try {
            if (tutteLinee == null || index < 0 || index >= tutteLinee.length || tutteLinee[index] == null) {
                return out;
            }
            for (Point2D p : tutteLinee[index]) {
                if (p != null) out.add(p);
            }
            out.sort(Comparator.comparingDouble(Point2D::getX));
        } catch (Exception ignored) {
        }
        return out;
    }

    private void drawAutoSnap(Canvas canvas) {
        if (DataSaved.isAutoSnap != 2) return;

        float dist = 0f;
        switch (DataSaved.bucketEdge) {
            case -1:
                dist = (float) (TriangleService.dist3D_SX * scala);
                break;
            case 1:
                dist = (float) (TriangleService.dist3D_DX * scala);
                break;
            case 0:
            default:
                dist = (float) (TriangleService.dist3D_CT * scala);
                break;
        }

        float x = mFixedXX - (dist * TriangleService.segnoLinea);
        dashedPaint.setStyle(Paint.Style.STROKE);
        dashedPaint.setColor(MyColorClass.colorConstraint);
        dashedPaint.setStrokeWidth((float) (3f / DataSaved.scale_FactorVista2D));
        dashedPaint.setPathEffect(new DashPathEffect(new float[]{20f, 15f}, 0));
        canvas.drawLine(x, -10000f, x, 10000f, dashedPaint);
        dashedPaint.setPathEffect(null);
    }

    private static boolean isValid(double[] p) {
        return p != null
                && p.length >= 3
                && (Math.abs(p[0]) > EPS || Math.abs(p[1]) > EPS || Math.abs(p[2]) > EPS);
    }

    private static boolean isFinite(PointF p) {
        return p != null && Float.isFinite(p.x) && Float.isFinite(p.y);
    }

    private static PointF normalizedVector(PointF from, PointF to, PointF fallback) {
        if (from == null || to == null) return fallback;
        PointF v = new PointF(to.x - from.x, to.y - from.y);
        return normalize(v, fallback);
    }

    private static PointF normalize(PointF v, PointF fallback) {
        if (v == null) return fallback;
        float len = (float) Math.sqrt(v.x * v.x + v.y * v.y);
        if (len < 0.0001f) return fallback;
        return new PointF(v.x / len, v.y / len);
    }

    private static float distance(PointF a, PointF b) {
        if (a == null || b == null) return 0f;
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static PointF add(PointF a, PointF b) {
        return new PointF(a.x + b.x, a.y + b.y);
    }

    private static PointF scale(PointF a, float s) {
        return new PointF(a.x * s, a.y * s);
    }

    private static PointF midpoint(PointF a, PointF b) {
        return new PointF((a.x + b.x) * 0.5f, (a.y + b.y) * 0.5f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int darken(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.max(0, Math.min(255, (int) (Color.red(color) * factor)));
        int g = Math.max(0, Math.min(255, (int) (Color.green(color) * factor)));
        int b = Math.max(0, Math.min(255, (int) (Color.blue(color) * factor)));
        return Color.argb(a, r, g, b);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
            offsetX -= (float) (distanceX / DataSaved.scale_FactorVista2D);
            offsetY -= (float) (distanceY / DataSaved.scale_FactorVista2D);
            invalidate();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            offsetX = 0;
            offsetY = 0;
            invalidate();
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            DataSaved.scale_FactorVista2D *= detector.getScaleFactor();
            DataSaved.scale_FactorVista2D = Math.max(0.05f, Math.min(DataSaved.scale_FactorVista2D, 10.0f));
            invalidate();
            return true;
        }
    }

    private static class BoomAxes {
        final double forwardX;
        final double forwardY;
        final double sideX;
        final double sideY;

        BoomAxes(double forwardX, double forwardY, double sideX, double sideY) {
            this.forwardX = forwardX;
            this.forwardY = forwardY;
            this.sideX = sideX;
            this.sideY = sideY;
        }
    }

    private static class ToolDrawPoints {
        final PointF left;
        final PointF center;
        final PointF right;

        ToolDrawPoints(PointF left, PointF center, PointF right) {
            this.left = left;
            this.center = center;
            this.right = right;
        }
    }
}
