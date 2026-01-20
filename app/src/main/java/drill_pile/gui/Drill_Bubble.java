package drill_pile.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class Drill_Bubble extends View {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    // -------- Input dinamici (set da fuori) --------
    private boolean showCrossOnly = false;
    // Errore planimetrico bit -> asse foro (metri, in sistema mondo: X=E, Y=N)
    private double errEast = 0.0;
    private double errNorth = 0.0;

    // Distanza da mostrare al centro (metri o ft già convertiti)
    private double distTextValue = 0.0;
    private String distTextUnit = ""; // opzionale

    // Orientamento macchina (deg) per ruotare la freccia: 0=N, 90=E
    private double headingDeg = 0.0;

    // Suggerimenti correzione tilt (tu li calcoli col tuo match):
    // true = mostra triangolo su quell’asse/direzione
    private boolean triUp, triRight, triDown, triLeft;

    // Colori (tu li gestisci come vuoi)
    private int ringColor = 0xFF2E7D32;     // verde default
    private int arrowColor = 0xFF2E7D32;
    private int textColor = 0xFFFFFFFF;
    private int triColor = 0xFF2E7D32;
    private int innerBgColor = 0x00000000; // trasparente, se vuoi riempire

    public Drill_Bubble(Context context) {
        super(context);
    }

    public Drill_Bubble(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Drill_Bubble(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ---------------- API ----------------

    /**
     * Errore planimetrico bit -> asse foro, in metri (o unità mondo coerenti).
     */
    public void setPlanError(double eastErr, double northErr) {
        this.errEast = eastErr;
        this.errNorth = northErr;
        invalidate();
    }

    /**
     * Valore al centro (già in unità display).
     */
    public void setCenterDistance(double value) {
        this.distTextValue = value;
        invalidate();
    }

    /**
     * Heading macchina per ruotare la freccia (0=N, 90=E).
     */
    public void setHeadingDeg(double headingDeg) {
        this.headingDeg = headingDeg;
        invalidate();
    }

    /**
     * Triangoli: accendi quelli che vuoi in base a pitch/roll.
     */
    public void setTriangles(boolean up, boolean right, boolean down, boolean left) {
        this.triUp = up;
        this.triRight = right;
        this.triDown = down;
        this.triLeft = left;
        invalidate();
    }

    /**
     * Colori (opzionale)
     */
    public void setColors(int ringColor, int arrowColor, int textColor, int triColor) {
        this.ringColor = ringColor;
        this.arrowColor = arrowColor;
        this.textColor = textColor;
        this.triColor = triColor;
        invalidate();
    }

    // ---------------- Draw ----------------

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float cx = w * 0.5f;
        float cy = h * 0.5f;

        float rOuter = Math.min(w, h) * 0.42f;
        float rInner = rOuter * 0.72f;

        float strokeOuter = Math.max(3f, rOuter * 0.12f);
        float strokeInner = Math.max(2f, rOuter * 0.035f);

        // --- anello esterno ---
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(strokeOuter);
        p.setColor(ringColor);
        canvas.drawCircle(cx, cy, rOuter, p);

        // --- cerchio interno ---
        p.setStrokeWidth(strokeInner);
        canvas.drawCircle(cx, cy, rInner, p);

        // (opzionale) riempimento interno
        if ((innerBgColor >>> 24) != 0) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(innerBgColor);
            canvas.drawCircle(cx, cy, rInner - strokeInner * 0.5f, p);
        }

        // --- triangoli esterni ---
        drawOuterTriangles(canvas, cx, cy, rOuter, strokeOuter);

        // --- freccia interna: direzione planimetrica (bit -> asse foro) ---
        drawInnerArrow(canvas, cx, cy, rInner * 0.82f);

        // --- testo inclinato al centro ---
        // drawCenterText(canvas, cx, cy, rInner);
    }

    private void drawOuterTriangles(Canvas canvas, float cx, float cy, float rOuter, float strokeOuter) {
        float triSize = strokeOuter * 1.4f;      // grandezza triangolo
        float triGap = strokeOuter * 0.15f;       // distanza dall’anello
        float r = rOuter + strokeOuter * 0.5f + triGap;

        p.setStyle(Paint.Style.FILL);
        p.setColor(triColor);

        // angoli in modo che la punta guardi verso il centro
        if (triUp) canvas.drawPath(triangle(cx, cy - r, triSize, 180), p); // punta giù verso centro
        if (triRight)
            canvas.drawPath(triangle(cx + r, cy, triSize, 270), p); // punta sinistra verso centro
        if (triDown) canvas.drawPath(triangle(cx, cy + r, triSize, 0), p); // punta su verso centro
        if (triLeft)
            canvas.drawPath(triangle(cx - r, cy, triSize, 90), p); // punta destra verso centro
    }

    /**
     * Triangolo centrato in (tx,ty) puntato verso angDeg (0=su, 90=destra...).
     */
    private Path triangle(float tx, float ty, float size, float angDeg) {
        float s = size;

        // triangolo base “su”
        Path path = new Path();
        path.moveTo(tx, ty - s);          // punta
        path.lineTo(tx + s, ty + s);      // base dx
        path.lineTo(tx - s, ty + s);      // base sx
        path.close();

        // ruota attorno al centro (tx,ty)
        android.graphics.Matrix m = new android.graphics.Matrix();
        m.setRotate(angDeg, tx, ty);
        path.transform(m);
        return path;
    }

    private void drawInnerArrow(Canvas canvas, float cx, float cy, float rInner) {
        double screenAngleDeg = 0;
        // Se errore quasi nullo: disegna una piccola croce centrale
        // ✅ Se richiesto: SOLO croce (no freccia, no testo)
        if (showCrossOnly) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(3f, rInner * 0.08f));
            p.setColor(arrowColor);

            float s = Math.max(10f, rInner * 0.18f);
            canvas.drawLine(cx - s, cy, cx + s, cy, p);
            canvas.drawLine(cx, cy - s, cx, cy + s, p);

        } else {

            // Bearing nel mondo (0=N,90=E) dal vettore errore
            double bearing = Math.toDegrees(Math.atan2(errEast, errNorth));
            screenAngleDeg = bearing - headingDeg;
            if (bearing < 0) bearing += 360.0;

            // Ruotiamo in frame schermo in base all'heading macchina

            double a = Math.toRadians(screenAngleDeg);

            // Direzione in schermo: 0° = su
            float dirX = (float) -Math.sin(a);
            float dirY = (float) Math.cos(a);

            // Lunghezza: freccia da bordo opposto a bordo verso la direzione
            // così la base NON è al centro ma dall'altra parte
            float pad = Math.max(8f, rInner * 0.10f);          // margine dai bordi interni
            float halfLen = Math.max(20f, rInner - pad);       // metà lunghezza (da centro a bordo)
            float x1 = cx - dirX * halfLen; // tail (lato opposto)
            float y1 = cy - dirY * halfLen;
            float x2 = cx + dirX * halfLen; // tip (lato verso direzione)
            float y2 = cy + dirY * halfLen;

            // Spessore freccia (stroke)
            float stroke =66f;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeWidth(stroke);
            p.setColor(arrowColor);

            // Corpo freccia (barra)
            canvas.drawLine(x1, y1, x2, y2, p);

            // Punta (solo da un lato) - STROKE, non piena
            float headLen = Math.max(26f, rInner * 0.48f);
            float headAng = (float) Math.toRadians(60); // apertura della punta

            // vettore unitario lungo la direzione (tail->tip)
            float ux = x2 - x1;
            float uy = y2 - y1;
            float uL = (float) Math.sqrt(ux * ux + uy * uy);
            if (uL < 1e-3f) return;
            ux /= uL;
            uy /= uL;

            // due “ali” della punta
            float cos = (float) Math.cos(headAng);
            float sin = (float) Math.sin(headAng);

            // rotazione vettore indietro di ±headAng
            float bx1 = (x2 - (ux * cos - uy * sin) * headLen);
            float by1 = (y2 - (uy * cos + ux * sin) * headLen);

            float bx2 = (x2 - (ux * cos + uy * sin) * headLen);
            float by2 = (y2 - (uy * cos - ux * sin) * headLen);

            canvas.drawLine(x2, y2, bx1, by1, p);
            canvas.drawLine(x2, y2, bx2, by2, p);

        }
        // -----------------------------
        // Testo DENTRO la freccia, ruotato con essa
        // -----------------------------
        String txt = format2(distTextValue);


        float textSize = Math.max(24f, rInner * 0.40f);
        p.setTextSize(textSize);
        p.setFakeBoldText(true);
        p.setTextAlign(Paint.Align.CENTER);

        // colore testo: usa textColor o arrowColor, a tua scelta
        p.setColor(textColor);
        p.setStyle(Paint.Style.FILL);

        // posiziona testo al centro della barra (cx,cy), ruotato come la freccia
        Paint.FontMetrics fm = p.getFontMetrics();
        float textY = cy - (fm.ascent + fm.descent) / 2f;

        canvas.save();
        // rotazione: in android 0° = asse X a destra, noi abbiamo dir (0=su)
        // angolo in gradi per ruotare testo lungo la freccia:
        float rotDeg = (float) screenAngleDeg;
        canvas.rotate(rotDeg+90, cx, cy);
        canvas.drawText(txt, cx, textY, p);
        canvas.restore();
    }


    private void drawCenterText(Canvas canvas, float cx, float cy, float rInner) {
        // testo tipo "0.22" ruotato leggermente come in foto
        String txt = format2(distTextValue);
        if (!distTextUnit.isEmpty()) txt = txt; // puoi aggiungere unità fuori se vuoi

        p.setColor(textColor);
        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);

        float textSize = Math.max(18f, rInner * 0.42f);
        p.setTextSize(textSize);
        p.setFakeBoldText(true);

        // misura baseline centrata
        Paint.FontMetrics fm = p.getFontMetrics();
        float textY = cy - (fm.ascent + fm.descent) / 2f;

        canvas.save();
        canvas.rotate(-25f, cx, cy); // inclinazione del testo
        canvas.drawText(txt, cx, textY, p);
        canvas.restore();
    }

    private static String format2(double v) {
        // formato "0.22" (2 decimali)
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    public void setCrossOnly(boolean crossOnly) {
        this.showCrossOnly = crossOnly;
        invalidate();
    }
}
