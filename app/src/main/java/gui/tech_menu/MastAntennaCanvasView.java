package gui.tech_menu;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import static utils.MyTypes.MAST_LEFT;
import static utils.MyTypes.MAST_RIGHT;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Objects;

import packexcalib.exca.DataSaved;

public class MastAntennaCanvasView extends View {
    private GestureDetector gestureDetector;

    private float panX = 0f;
    private float panY = 0f;

    private boolean isScaling = false;
    private ScaleGestureDetector scaleGestureDetector;
    private static final float MIN_ZOOM = 0.80f;
    private static final float MAX_ZOOM = 2.50f;
    private static final float ZOOM_STEP = 0.10f;

    private float userZoom = 2.0f;
    private static final float MACHINE_DRAW_SCALE = 1.25f;
   //private static final float ANTENNA_1_TO_ANTENNA_2_DISTANCE_M = 0.6f;
    private static  float ANTENNA_2_BASE_ANGLE_DEG = 0.0f;

    /*
     * Nuova scala "metrica" della macchina:
     * - larghezza totale macchina (cingolo esterno -> cingolo esterno) ~ 2 m
     * - lunghezza macchina ~ 3 m
     */
    private static final float MACHINE_TOTAL_WIDTH_M = 1.30f;
    private static final float MACHINE_LENGTH_M = 1.50f;
    private static final float TRACK_WIDTH_M = 0.35f;
    private static final float BODY_LENGTH_M = 1.40f;

    /*
     * Anche le dimensioni grafiche di martello e antenne
     * vengono agganciate alla stessa scala.
     */
    private static final float HAMMER_SIZE_M = 0.65f;
    private static final float ANTENNA_1_RADIUS_M = 0.22f;
    private static final float ANTENNA_2_RADIUS_M = 0.22f;

    private final Paint machinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint machineStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hammerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hammerStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint antenna1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint antenna2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MastAntennaCanvasView(Context context) {
        super(context);
        init();
    }

    public MastAntennaCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MastAntennaCanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        machinePaint.setColor(Color.rgb(220, 220, 220));
        machinePaint.setStyle(Paint.Style.FILL);

        machineStrokePaint.setColor(Color.rgb(80, 80, 80));
        machineStrokePaint.setStyle(Paint.Style.STROKE);
        machineStrokePaint.setStrokeWidth(dp(2));

        hammerPaint.setColor(Color.rgb(255, 210, 0));
        hammerPaint.setStyle(Paint.Style.FILL);

        hammerStrokePaint.setColor(Color.BLACK);
        hammerStrokePaint.setStyle(Paint.Style.STROKE);
        hammerStrokePaint.setStrokeWidth(dp(2));

        antenna1Paint.setColor(Color.rgb(40, 90, 255));
        antenna1Paint.setStyle(Paint.Style.FILL);

        antenna2Paint.setColor(Color.rgb(255, 10, 10));
        antenna2Paint.setStyle(Paint.Style.FILL);

        linePaint.setColor(Color.rgb(40, 40, 40));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2));

        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(13));
        textPaint.setFakeBoldText(true);
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isScaling = true;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                setUserZoom(userZoom * detector.getScaleFactor());
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
            }
        });

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!isScaling) {
                    panX -= distanceX;
                    panY -= distanceY;
                    invalidate();
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                resetPan();
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float w = getWidth();
        final float h = getHeight();

        if (w <= 0 || h <= 0) return;

        final float machineCenterX = w * 0.5f;
         float machineCenterY = h * 0.65f;



        float hammerX;
        float hammerY;


        /*
         * La macchina rimane sempre fissa e orientata verso l’alto.
         * Cambia solo la posizione del martello attorno alla macchina.
         */
        if (Objects.equals(DataSaved.Drill_Mast_Position, MAST_LEFT)) {
            ANTENNA_2_BASE_ANGLE_DEG=-90.0f;
            hammerX = w * 0.40f;
            hammerY = h * 0.50f;
            machineCenterY = h * 0.5f;
        } else if (Objects.equals(DataSaved.Drill_Mast_Position, MAST_RIGHT)) {
            ANTENNA_2_BASE_ANGLE_DEG=-90.0f;
            hammerX = w * 0.6f;
            hammerY = h * 0.50f;
            machineCenterY = h * 0.5f;
        } else {
            // MAST_FORWARD
            ANTENNA_2_BASE_ANGLE_DEG=0.0f;
            hammerX = w * 0.50f;
            hammerY = h * 0.45f;
            machineCenterY = h * 0.65f;
        }

        final float deltaX = (float) DataSaved.Tool_Delta_X;
        final float deltaY = (float) DataSaved.Tool_Delta_Y;
        final float deltaHdt = (float) DataSaved.deltaGPS2;

        final float scale = calculateAutoScale(w, h, deltaX, deltaY);

        canvas.save();

        /*
         * Prima trasliamo con il pan, poi zoomiamo attorno al centro macchina.
         */
        canvas.translate(panX, panY);
        canvas.scale(userZoom, userZoom, machineCenterX, machineCenterY);

        drawMachine(canvas, machineCenterX, machineCenterY, scale);

        float antenna1X;
        float antenna1Y;

        if (Objects.equals(DataSaved.Drill_Mast_Position, MAST_LEFT) || Objects.equals(DataSaved.Drill_Mast_Position, MAST_RIGHT)) {
            /*
             * MAST LEFT / RIGHT
             * X++ => destra
             * Y++ => basso
             */
            antenna1X = hammerX - deltaX * scale;
            antenna1Y = hammerY + deltaY * scale;
        } else {
            /*
             * MAST FORWARD
             * X++ => basso
             * Y++ => sinistra
             */
            antenna1X = hammerX - deltaY * scale;
            antenna1Y = hammerY - deltaX * scale;
        }

        /*
         * delta negativo => rotazione oraria
         */
        float antenna2AngleDeg = ANTENNA_2_BASE_ANGLE_DEG - deltaHdt;
        double antenna2AngleRad = Math.toRadians(antenna2AngleDeg);

        float antennaDistanceM = getAntennaDistanceM(deltaY);
        float antenna2RadiusPx = antennaDistanceM * scale;

        float antenna2X = antenna1X + (float) Math.cos(antenna2AngleRad) * antenna2RadiusPx;
        float antenna2Y = antenna1Y + (float) Math.sin(antenna2AngleRad) * antenna2RadiusPx;

        drawHammer(canvas, hammerX, hammerY, scale);
        drawAntennas(canvas, antenna1X, antenna1Y, antenna2X, antenna2Y, hammerX, hammerY, DataSaved.Drill_Mast_Position, scale);
        canvas.restore();
    }

    private float calculateAutoScale(float w, float h, float deltaX, float deltaY) {
        float minSide = Math.min(w, h);

        /*
         * Estensione dinamica dovuta agli offset + distanza antenna1 -> antenna2.
         * Ora la distanza antenne è: 2 m + abs(deltaY).
         */
        float antennaDistanceM = getAntennaDistanceM(deltaY);

        float antennaEnvelopeM = Math.max(
                Math.abs(deltaX),
                Math.abs(deltaY)
        ) + antennaDistanceM;

        float fixedEnvelopeM = Math.max(MACHINE_LENGTH_M, MACHINE_TOTAL_WIDTH_M);

        float metersToFit = Math.max(fixedEnvelopeM, antennaEnvelopeM);

        float maxPixelRadius = minSide * 0.18f;

        return maxPixelRadius / metersToFit;
    }
    private void drawHammer(Canvas canvas, float x, float y, float scale) {
        float hammerSize = HAMMER_SIZE_M * scale;

        RectF hammer = new RectF(
                x - hammerSize / 2f,
                y - hammerSize / 2f,
                x + hammerSize / 2f,
                y + hammerSize / 2f
        );

        canvas.drawRoundRect(hammer, dp(3), dp(3), hammerPaint);
        canvas.drawRoundRect(hammer, dp(3), dp(3), hammerStrokePaint);

        //canvas.drawText("MARTELLO", x, y + hammerSize / 2f + dp(18), textPaint);
    }

    private void drawMachine(Canvas canvas, float cx, float cy, float scale) {

        /*
         * Dimensioni macchina in metri convertite in pixel
         */
        float machineScale = scale * MACHINE_DRAW_SCALE;

        float machineTotalWidthPx = MACHINE_TOTAL_WIDTH_M * machineScale;
        float machineLengthPx = MACHINE_LENGTH_M * machineScale;
        float trackWidthPx = TRACK_WIDTH_M * machineScale;
        float bodyWidthPx = machineTotalWidthPx - (2f * trackWidthPx);
        float bodyLengthPx = BODY_LENGTH_M * machineScale;

        /*
         * Corpo centrale
         */
        RectF body = new RectF(
                cx - bodyWidthPx / 2f,
                cy - bodyLengthPx / 2f,
                cx + bodyWidthPx / 2f,
                cy + bodyLengthPx / 2f
        );

        /*
         * Cingoli ATTACCATI al corpo
         */
        RectF leftTrack = new RectF(
                cx - machineTotalWidthPx / 2f,
                cy - machineLengthPx / 2f,
                cx - machineTotalWidthPx / 2f + trackWidthPx,
                cy + machineLengthPx / 2f
        );

        RectF rightTrack = new RectF(
                cx + machineTotalWidthPx / 2f - trackWidthPx,
                cy - machineLengthPx / 2f,
                cx + machineTotalWidthPx / 2f,
                cy + machineLengthPx / 2f
        );

        canvas.drawRoundRect(leftTrack, dp(6), dp(6), machinePaint);
        canvas.drawRoundRect(rightTrack, dp(6), dp(6), machinePaint);
        canvas.drawRoundRect(body, dp(8), dp(8), machinePaint);

        canvas.drawRoundRect(leftTrack, dp(6), dp(6), machineStrokePaint);
        canvas.drawRoundRect(rightTrack, dp(6), dp(6), machineStrokePaint);
        canvas.drawRoundRect(body, dp(8), dp(8), machineStrokePaint);

        /*
         * Freccia frontale verso l’alto display
         */
        float arrowSize = 0.35f * machineScale;
        float arrowStartY = cy - dp(6);
        float arrowEndY = arrowStartY - arrowSize;

        canvas.drawLine(cx, arrowStartY, cx, arrowEndY, machineStrokePaint);
        canvas.drawLine(cx, arrowEndY, cx - arrowSize * 0.35f, arrowEndY + arrowSize * 0.35f, machineStrokePaint);
        canvas.drawLine(cx, arrowEndY, cx + arrowSize * 0.35f, arrowEndY + arrowSize * 0.35f, machineStrokePaint);

        //canvas.drawText("MCH", cx, cy + machineLengthPx / 2f + dp(28), textPaint);
    }

    private void drawAntennas(
            Canvas canvas,
            float antenna1X,
            float antenna1Y,
            float antenna2X,
            float antenna2Y,
            float hammerX,
            float hammerY,
            String mastPosition,
            float scale
    ) {
        float antenna1Radius = ANTENNA_1_RADIUS_M * scale;
        float antenna2Radius = ANTENNA_2_RADIUS_M * scale;

        /*
         * Punto intermedio "deltaY":
         * - in MAST_FORWARD il tratto ANT1 -> deltaPoint è orizzontale
         * - in MAST_LEFT / MAST_RIGHT il tratto ANT1 -> deltaPoint è verticale
         */
        float deltaPointX;
        float deltaPointY;

        boolean isLeftOrRight =
                MAST_LEFT.equals(mastPosition) || MAST_RIGHT.equals(mastPosition);

        if (isLeftOrRight) {
            // deltaY verticale, deltaX orizzontale
            deltaPointX = antenna1X;
            deltaPointY = hammerY;
        } else {
            // MAST_FORWARD: deltaY orizzontale, deltaX verticale
            deltaPointX = hammerX;
            deltaPointY = antenna1Y;
        }

        /*
         * 1) linea ANT1 -> punto deltaY
         * 2) linea punto deltaY -> centro martello
         */
        canvas.drawLine(antenna1X, antenna1Y, deltaPointX, deltaPointY, linePaint);
        canvas.drawLine(deltaPointX, deltaPointY, hammerX, hammerY, linePaint);

        /*
         * Barra tra le due antenne: questa rimane
         */
        canvas.drawLine(antenna1X, antenna1Y, antenna2X, antenna2Y, linePaint);

        // antenna 1
        canvas.drawCircle(antenna1X, antenna1Y, antenna1Radius, antenna1Paint);
        canvas.drawCircle(antenna1X, antenna1Y, antenna1Radius, hammerStrokePaint);
        //canvas.drawText("1", antenna1X, antenna1Y , textPaint);

        // antenna 2
        canvas.drawCircle(antenna2X, antenna2Y, antenna2Radius, antenna2Paint);
        canvas.drawCircle(antenna2X, antenna2Y, antenna2Radius, hammerStrokePaint);
        //canvas.drawText("2", antenna2X, antenna2Y , textPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    public void zoomIn() {
        setUserZoom(userZoom + ZOOM_STEP);
    }

    public void zoomOut() {
        setUserZoom(userZoom - ZOOM_STEP);
    }

    public void resetZoom() {
        setUserZoom(1.0f);
    }

    public void setUserZoom(float zoom) {
        userZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        invalidate();
    }

    public float getUserZoom() {
        return userZoom;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scaleGestureDetector != null) {
            scaleGestureDetector.onTouchEvent(event);
        }

        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event);
        }

        return true;
    }
    public void resetPan() {
        panX = 0f;
        panY = 0f;
        invalidate();
    }

    public void resetViewTransform() {
        panX = 0f;
        panY = 0f;
        userZoom = 1.0f;
        invalidate();
    }
    private float getAntennaDistanceM(float deltaY) {
        /*
         * Regola:
         * distanza ANT1 -> ANT2 non minore di 2 m
         * e dinamica fino a deltaY + 2 m.
         *
         * In pratica:
         * distanza = 2 m + abs(deltaY)
         */
        return (float) DataSaved.distG1_G2;
    }
}