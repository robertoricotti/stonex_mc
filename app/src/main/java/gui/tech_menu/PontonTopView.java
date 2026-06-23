package gui.tech_menu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class PontonTopView extends View {

    private double pontonWidthMeters = 1.0;
    private double pontonLengthMeters = 1.0;
    private double deltaXMeters = 0.0;
    private double deltaYMeters = 0.0;

    private final Paint pontonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint slewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint deltaXPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint deltaYPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF pontonRect = new RectF();

    public PontonTopView(Context context) {
        super(context);
        init();
    }

    public PontonTopView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PontonTopView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        pontonPaint.setStyle(Paint.Style.FILL);
        pontonPaint.setColor(Color.rgb(220, 220, 220));

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setColor(Color.DKGRAY);

        slewPaint.setStyle(Paint.Style.STROKE);
        slewPaint.setStrokeWidth(4f);
        slewPaint.setColor(Color.BLACK);

        deltaXPaint.setStyle(Paint.Style.STROKE);
        deltaXPaint.setStrokeWidth(4f);
        deltaXPaint.setColor(Color.BLUE);
        deltaXPaint.setPathEffect(new DashPathEffect(new float[]{18f, 12f}, 0));

        deltaYPaint.setStyle(Paint.Style.STROKE);
        deltaYPaint.setStrokeWidth(4f);
        deltaYPaint.setColor(Color.RED);
        deltaYPaint.setPathEffect(new DashPathEffect(new float[]{18f, 12f}, 0));

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setValues(double widthMeters,
                          double lengthMeters,
                          double deltaXMeters,
                          double deltaYMeters) {

        this.pontonWidthMeters = Math.max(widthMeters, 0.001);
        this.pontonLengthMeters = Math.max(lengthMeters, 0.001);
        this.deltaXMeters = deltaXMeters;
        this.deltaYMeters = deltaYMeters;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        if (viewWidth <= 0 || viewHeight <= 0) return;

        float padding = 40f;

        float availableWidth = viewWidth - padding * 2f;
        float availableHeight = viewHeight - padding * 2f;

        if (availableWidth <= 0 || availableHeight <= 0) return;

        float scaleX = availableWidth / (float) pontonWidthMeters;
        float scaleY = availableHeight / (float) pontonLengthMeters;
        float scale = Math.min(scaleX, scaleY);

        float drawWidth = (float) pontonWidthMeters * scale;
        float drawHeight = (float) pontonLengthMeters * scale;

        float left = (viewWidth - drawWidth) / 2f;
        float top = (viewHeight - drawHeight) / 2f;
        float right = left + drawWidth;
        float bottom = top + drawHeight;

        pontonRect.set(left, top, right, bottom);

        canvas.drawRect(pontonRect, pontonPaint);
        canvas.drawRect(pontonRect, borderPaint);

        float slewX = left + (float) deltaXMeters * scale;
        float slewY = top + (float) deltaYMeters * scale;

        slewX = clamp(slewX, left, right);
        slewY = clamp(slewY, top, bottom);

        float realRadiusMeters = 1f;
        float slewRadius = realRadiusMeters * scale;

        float minRadius = 8f;
        float maxRadius = 40f;
        slewRadius = clamp(slewRadius, minRadius, maxRadius);

        /*
         * Delta X:
         * linea blu orizzontale dallo spigolo alto sinistro
         * fino alla X dello SLEW.
         */
        canvas.drawLine(left, top, slewX, top, deltaXPaint);

        /*
         * Delta Y:
         * linea rossa verticale dalla fine del Delta X
         * fino allo SLEW.
         */
        canvas.drawLine(slewX, top, slewX, slewY, deltaYPaint);

        canvas.drawCircle(slewX, slewY, slewRadius, slewPaint);
        canvas.drawText("SLEW", slewX, slewY - slewRadius - 12f, textPaint);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}