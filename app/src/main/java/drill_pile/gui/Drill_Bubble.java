package drill_pile.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import packexcalib.exca.DataSaved;

public class Drill_Bubble extends View {
    private float uiRotDeg = 0f;


    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ---- Mode ----
    private boolean isDrilling = false;
    private float circleDeg = 0f; // rotazione overlay (croce)

    // ---- Inputs ----
    private boolean showCrossOnly = false; // se true, nasconde freccia e mostra solo overlay (lo usi tu se vuoi)
    private double errEast = 0.0;
    private double errNorth = 0.0;

    private String distTextValue = "0.0";

    // 0=N, 90=E
    private double headingDeg = 0.0;

    private boolean triUp, triRight, triDown, triLeft;

    // ---- Colors ----
    private int inColor = Color.TRANSPARENT;
    private int ringColor = 0xFF2E7D32;
    private int arrowColor = 0xFF2E7D32;
    private int textColor = 0xFFFFFFFF;
    private int triColor = 0xFF2E7D32;
    private int innerBgColor = 0x00000000;

    public Drill_Bubble(Context context) { super(context); }
    public Drill_Bubble(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
    public Drill_Bubble(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    // ---------------- API ----------------

    public void setPlanError(double eastErr, double northErr) {
        this.errEast = eastErr;
        this.errNorth = northErr;
        invalidate();
    }

    public void setCenterDistance(String value) {
        this.distTextValue = value != null ? value : "";
        invalidate();
    }

    public void setHeadingDeg(double headingDeg) {
        this.headingDeg = headingDeg;
        invalidate();
    }

    public void setTriangles(boolean up, boolean right, boolean down, boolean left) {
        this.triUp = up;
        this.triRight = right;
        this.triDown = down;
        this.triLeft = left;
        invalidate();
    }

    public void setColors(int ringColor, int inColor, int arrowColor, int textColor, int triColor) {
        this.inColor = inColor;
        this.ringColor = ringColor;
        this.arrowColor = arrowColor;
        this.textColor = textColor;
        this.triColor = triColor;
        invalidate();
    }

    public void setCrossOnly(boolean crossOnly) {
        this.showCrossOnly = crossOnly;
        invalidate();
    }

    public void setDrillingMode(boolean drilling, float circleDeg) {
        this.isDrilling = drilling;
        this.circleDeg = circleDeg;
        invalidate();
    }

    // ---------------- Draw ----------------

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.rotate(uiRotDeg, getWidth()*0.5f, getHeight()*0.5f);

        float w = getWidth();
        float h = getHeight();
        float cx = w * 0.5f;
        float cy = h * 0.5f;

        // tieniti più “dentro” (anti-bordo)
        float baseR = Math.min(w, h) * 0.46f;     // raggio massimo teorico
        float safe = Math.max(8f, baseR * 0.08f); // margine
        float rOuter = baseR - safe;
        float rInner = rOuter * 0.66f;

        float strokeOuter = Math.max(3f, rOuter * 0.25f);
        float strokeInner = Math.max(2f, rOuter * 0.035f);

        // -------- ring esterno --------
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(strokeOuter);
        p.setColor(ringColor);
        canvas.drawCircle(cx, cy, rOuter, p);

        // -------- cerchio interno --------
        p.setStyle(Paint.Style.FILL);
        p.setColor(inColor);
        canvas.drawCircle(cx, cy, rInner, p);

        // fill extra interno (opzionale)
        if ((innerBgColor >>> 24) != 0) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(innerBgColor);
            canvas.drawCircle(cx, cy, rInner - strokeInner * 0.5f, p);
        }

        // -------- triangoli pitch/roll --------
        drawOuterTriangles(canvas, cx, cy, rOuter, strokeOuter);

        // -------- freccia XY (bit -> asse/testa) --------
        boolean canShowInnerArrow;
        if (isDrilling) {
            // in drilling: mostra solo se sei fuori asse
            canShowInnerArrow = !services.PointService.okDrill;
        } else {
            // in setup: mostra se hai un target selezionato
            canShowInnerArrow = (DataSaved.Selected_Point3D_Drill != null);
        }

        if (!showCrossOnly && canShowInnerArrow) {
            drawInnerArrow(canvas, cx, cy, rInner * 0.82f);
        }

        // -------- overlay DRILLING: croce rotante + piccolo bordo --------
        if (isDrilling) {
            drawDrillingOverlay(canvas, cx, cy, rInner, rOuter, strokeOuter);
        }

        // -------- testo centrale SEMPRE NON RUOTA --------
        drawCenterText(canvas, cx, cy, rInner);
        canvas.restore();
    }

    private void drawOuterTriangles(Canvas canvas, float cx, float cy, float rOuter, float strokeOuter) {

        // più vicino all’anello interno: aumenta inset
        float inset = strokeOuter * 0.95f;               // <--- qui avvicini i triangoli al cerchio interno
        float r = rOuter - (strokeOuter * 0.5f) + inset;

        float height = strokeOuter * 0.80f;
        float halfBase = strokeOuter * 1.35f;

        p.setStyle(Paint.Style.FILL);
        p.setColor(triColor);

        if (triUp)    canvas.drawPath(triangleWide(cx, cy - r, height, halfBase, 180), p);
        if (triRight) canvas.drawPath(triangleWide(cx + r, cy, height, halfBase, 270), p);
        if (triDown)  canvas.drawPath(triangleWide(cx, cy + r, height, halfBase,   0), p);
        if (triLeft)  canvas.drawPath(triangleWide(cx - r, cy, height, halfBase,  90), p);
    }

    private Path triangleWide(float tx, float ty, float height, float halfBase, float angDeg) {
        Path path = new Path();
        path.moveTo(tx, ty - height);
        path.lineTo(tx + halfBase, ty + height * 0.6f);
        path.lineTo(tx - halfBase, ty + height * 0.6f);
        path.close();

        android.graphics.Matrix m = new android.graphics.Matrix();
        m.setRotate(angDeg, tx, ty);
        path.transform(m);
        return path;
    }

    private void drawInnerArrow(Canvas canvas, float cx, float cy, float rInner) {

        // Bearing mondo (0=N,90=E) dal vettore errore (errE, errN)
        double bearing = Math.toDegrees(Math.atan2(errEast, errNorth));
        if (bearing < 0) bearing += 360.0;

        double screenAngleDeg = bearing - headingDeg;

        // direzione schermo: 0° = su
        double a = Math.toRadians(screenAngleDeg);
        float dirX = (float) Math.sin(a);
        float dirY = (float)  -Math.cos(a);

        float halfLen = Math.max(18f, rInner + 4f);

        float tailX = cx - dirX * halfLen;
        float tailY = cy - dirY * halfLen;
        float tipX  = cx + dirX * halfLen;
        float tipY  = cy + dirY * halfLen;

        // unit dir
        float ux = tipX - tailX;
        float uy = tipY - tailY;
        float uL = (float) Math.sqrt(ux * ux + uy * uy);
        if (uL < 1e-3f) return;
        ux /= uL;
        uy /= uL;

        float px = -uy;
        float py = ux;

        float shaftW = Math.max(26f, rInner * 0.95f);
        float halfW  = shaftW * 0.5f;
        float headLen = Math.max(26f, rInner * 0.55f);

        float baseCx = tipX - ux * headLen;
        float baseCy = tipY - uy * headLen;

        // rettangolo
        Path shaft = new Path();
        shaft.moveTo(tailX + px * halfW, tailY + py * halfW);
        shaft.lineTo(baseCx + px * halfW, baseCy + py * halfW);
        shaft.lineTo(baseCx - px * halfW, baseCy - py * halfW);
        shaft.lineTo(tailX - px * halfW, tailY - py * halfW);
        shaft.close();

        // triangolo punta
        float headW = shaftW;
        float halfHW = headW * 0.5f;

        float b1x = baseCx + px * halfHW;
        float b1y = baseCy + py * halfHW;
        float b2x = baseCx - px * halfHW;
        float b2y = baseCy - py * halfHW;

        Path head = new Path();
        head.moveTo(tipX, tipY);
        head.lineTo(b1x, b1y);
        head.lineTo(b2x, b2y);
        head.close();

        p.setStyle(Paint.Style.FILL);
        p.setColor(arrowColor);
        canvas.drawPath(shaft, p);
        canvas.drawPath(head, p);
    }

    private void drawDrillingOverlay(Canvas canvas, float cx, float cy,
                                     float rInner, float rOuter, float strokeOuter) {

        // piccolo bordo interno per “stato drilling”
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f, strokeOuter * 0.12f));
        p.setColor(Color.argb(140, 255, 255, 0)); // giallo soft
        canvas.drawCircle(cx, cy, rInner * 0.98f, p);

        // croce rotante (sopra a tutto)
        float crossLen = rInner * 0.70f;
        float crossStroke = Math.max(3f, strokeOuter * 0.18f);

        canvas.save();
        canvas.rotate(circleDeg, cx, cy);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(crossStroke);
        p.setColor(Color.argb(160, 50, 50, 50)); // grigio scuro semi

        canvas.drawLine(cx - crossLen, cy, cx + crossLen, cy, p);
        canvas.drawLine(cx, cy - crossLen, cx, cy + crossLen, p);

        canvas.restore();
    }

    private void drawCenterText(Canvas canvas, float cx, float cy, float rInner) {
        canvas.rotate(-uiRotDeg,getWidth()*0.5f,getHeight()*0.5f);
        String txt = distTextValue != null ? distTextValue : "";

        float textSize = Math.max(18f, rInner * 0.50f);
        p.setTextSize(textSize);
        p.setFakeBoldText(true);
        p.setTextAlign(Paint.Align.CENTER);
        p.setStyle(Paint.Style.FILL);
        p.setColor(textColor);

        Paint.FontMetrics fm = p.getFontMetrics();
        float textY = cy - (fm.ascent + fm.descent) / 2f;

        canvas.drawText(txt, cx, textY, p);
        canvas.rotate(uiRotDeg,getWidth()*0.5f,getHeight()*0.5f);
    }
    public void setUiRotationDeg(float deg) {
        uiRotDeg = ((deg % 360f) + 360f) % 360f;
        invalidate();
    }
}
