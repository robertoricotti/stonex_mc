package drill_pile.gui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.example.stx_dig.R;

import org.bouncycastle.jce.provider.BrokenPBE;

import java.util.Locale;

import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;
import utils.Utils;

public class VerticalTargetIndicatorView extends View {

    // ===== VALORI REALI (metri) =====

    private double currentValue = 0.0;
    private double targetValue = 0.0;
    private double maxHigh = 1.0;
    private double maxLow = -1.0;
    private double tolerance = 0.02; // ± metri
    private int colorUp=Color.BLUE;
    private int colorDown=Color.RED;
    private int colorGreen=Color.GREEN;

    // ===== COSTANTI GRAFICHE =====
    private static final float TARGET_POSITION = 0.85f; // 85% dall’alto

    // ===== PAINT =====
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public VerticalTargetIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        barPaint.setStyle(Paint.Style.STROKE);
        barPaint.setStrokeWidth(150f);
       // barPaint.setStrokeCap(Paint.Cap.SQUARE);

        arrowPaint.setStyle(Paint.Style.FILL);

        targetPaint.setColor(Color.WHITE);
        targetPaint.setStrokeWidth(4f);

        textPaint.setColor(MyColorClass.colorConstraint);

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setFakeBoldText(true);
    }

    // ===== API PUBBLICA =====

    public void setRange(double low, double high) {
        this.maxLow = low;
        this.maxHigh = high;
        invalidate();
    }

    public void setTargetValue(double target) {
        this.targetValue = target;
        invalidate();
    }

    public void setCurrentValue(double value) {
        this.currentValue = value;
        invalidate();
    }

    public void setTolerance(double toleranceMeters) {
        this.tolerance = toleranceMeters;
        invalidate();
    }
    public void setColors(int colorUp,int colorDown,int colorGreen){
        this.colorUp=colorUp;
        this.colorDown=colorDown;
        this.colorGreen=colorGreen;

    }

    // ===== DISEGNO =====

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        float top = getPaddingTop();
        float bottom = h - getPaddingBottom();
        float centerX = w * 0.5f;

        // Target Y fisso
        float targetY = top + h * TARGET_POSITION;



        // Colore unico (barra + freccia)
        double delta = currentValue - targetValue;
        int color;
        if (Math.abs(delta) <= tolerance) {
            color =colorGreen;
            targetPaint.setColor(colorGreen);

        } else if (delta > 0) {
            color = colorUp;
            targetPaint.setColor(colorUp);

        } else {
            color = colorDown;
            targetPaint.setColor(colorUp);

        }
        barPaint.setColor(color);
        arrowPaint.setColor(color);

        // Normalizzazione
        double range = maxHigh - maxLow;
        if (range <= 0) return;

        double normalized = delta / range;

        // Spostamento massimo visivo
        float maxTravel = h * 0.45f;

        // === arrowY DEFINITO QUI ===
        float arrowY = (float) (targetY - normalized * maxTravel);

        // Clamp ai bordi
        arrowY = Math.max(top, Math.min(bottom, arrowY-80));

        // Barra = coda (target → freccia)
        canvas.drawLine(centerX, top, centerX, arrowY, barPaint);

        // Freccia
        drawArrow(canvas, centerX, arrowY);

        // Linea target
        // Calcolo dello spessore della linea in base alla tolleranza
        float toleranceVisual = (float) (tolerance / (maxHigh - maxLow) * h); // proporzionale all'altezza
        toleranceVisual = Math.max(4f, Math.min(40f, toleranceVisual)); // clamp per spessore minimo e massimo

        targetPaint.setStrokeWidth(toleranceVisual);

        targetPaint.setAlpha(225);
        // Disegna la linea "tolleranza"
        canvas.drawLine(centerX - 80, targetY, centerX + 80, targetY, targetPaint);

        targetPaint.setStrokeWidth(1f);
        targetPaint.setColor(Color.YELLOW);
        targetPaint.setAlpha(255);
        canvas.drawLine(centerX - 80, targetY, centerX + 80, targetY, targetPaint);

        // Testo target
        textPaint.setTextSize(22f);
        canvas.drawText(
                Utils.readUnitOfMeasureLITE(String.valueOf(targetValue))+Utils.getMetriSimbol().replace("[","").replace("]",""),
                w - 1,
                targetY - 8,
                textPaint
        );

        // Testo actual
        textPaint.setTextSize(18f);
        canvas.drawText(
                Utils.readUnitOfMeasureLITE(String.valueOf(currentValue))+Utils.getMetriSimbol().replace("[","").replace("]",""),

                w-1,
                arrowY ,
                textPaint
        );
        //DF07.EA01.0E2D.DBAD.D803
    }

    private void drawArrow(Canvas canvas, float x, float y) {
        float size = 80f;

        Path p = new Path();
        p.moveTo(x - size, y);
        p.lineTo(x + size, y);
        p.lineTo(x, y + size);
        p.close();

        canvas.drawPath(p, arrowPaint);
    }
}