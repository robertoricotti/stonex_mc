package drill_pile.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import packexcalib.exca.DataSaved;

public class Drill_Bubble extends View {

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean isDrilling = false;
    private float circleDeg = 0f;   // gradi rotazione croce (0..360)


    // -------- Input dinamici (set da fuori) --------
    private boolean showCrossOnly = false;
    // Errore planimetrico bit -> asse foro (metri, in sistema mondo: X=E, Y=N)
    private double errEast = 0.0;
    private double errNorth = 0.0;

    // Distanza da mostrare al centro (metri o ft già convertiti)
    private String distTextValue = "0.0";
    private String distTextUnit = ""; // opzionale

    // Orientamento macchina (deg) per ruotare la freccia: 0=N, 90=E
    private double headingDeg = 0.0;

    // Suggerimenti correzione tilt (tu li calcoli col tuo match):
    // true = mostra triangolo su quell’asse/direzione
    private boolean triUp, triRight, triDown, triLeft;

    // Colori (tu li gestisci come vuoi)
    private int inColor= Color.TRANSPARENT;
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
    public void setCenterDistance(String value) {
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
    public void setColors(int ringColor, int inColor,int arrowColor, int textColor, int triColor) {
        this.inColor=inColor;
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
        if (isDrilling) {
            drawDrillingCircle(canvas, cx, cy);
            return;
        }
        float rOuter = Math.min(w, h) * 0.36f;
        float rInner = rOuter * 0.66f;

        float strokeOuter = Math.max(3f, rOuter * 0.25f);
        float strokeInner = Math.max(2f, rOuter * 0.035f);

        // --- anello esterno ---
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(strokeOuter);
        p.setColor(ringColor);
        canvas.drawCircle(cx, cy, rOuter, p);

        // --- cerchio interno ---
        p.setStyle(Paint.Style.FILL);
        p.setStrokeWidth(strokeInner);
        p.setColor(inColor);
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

        // ✅ posizionati vicino all'anello interno (dentro l'anello esterno)
        float inset = strokeOuter * 0.75f;               // quanto "entrare" rispetto al bordo esterno
        float r = rOuter - (strokeOuter * 0.5f) + inset; // raggio dove posizioni i triangoli

        // ✅ triangoli più "aperti" e bassi
        float height  = strokeOuter * 0.80f;  // meno alti
        float halfBase = strokeOuter * 1.35f; // base più larga (aperta)

        p.setStyle(Paint.Style.FILL);
        p.setColor(triColor);

        // punta verso il centro
        if (triUp)    canvas.drawPath(triangleWide(cx, cy - r, height, halfBase, 180), p);
        if (triRight) canvas.drawPath(triangleWide(cx + r, cy, height, halfBase, 270), p);
        if (triDown)  canvas.drawPath(triangleWide(cx, cy + r, height, halfBase,   0), p);
        if (triLeft)  canvas.drawPath(triangleWide(cx - r, cy, height, halfBase,  90), p);
    }
    private Path triangleWide(float tx, float ty, float height, float halfBase, float angDeg) {
        // Triangolo base "su" (punta in alto), ma con proporzioni personalizzate:
        // - height = altezza
        // - halfBase = metà base (larghezza/2)
        Path path = new Path();
        path.moveTo(tx, ty - height);                 // punta
        path.lineTo(tx + halfBase, ty + height * 0.6f); // base dx (un po' più su per renderlo meno alto)
        path.lineTo(tx - halfBase, ty + height * 0.6f); // base sx
        path.close();

        android.graphics.Matrix m = new android.graphics.Matrix();
        m.setRotate(angDeg, tx, ty);
        path.transform(m);
        return path;
    }

    private void drawInnerArrow(Canvas canvas, float cx, float cy, float rInner) {
        double screenAngleDeg = 0;

        {

            // Bearing nel mondo (0=N,90=E) dal vettore errore
            double bearing = Math.toDegrees(Math.atan2(errEast, errNorth));
            if (bearing < 0) bearing += 360.0;

            screenAngleDeg = bearing - headingDeg;

            // Direzione in schermo: 0° = su
            double a = Math.toRadians(screenAngleDeg);

            float dirX = (float) -Math.sin(a);
            float dirY = (float)  Math.cos(a);

            // Lunghezza: da bordo opposto a bordo verso direzione
            float pad = 0;
            float halfLen = Math.max(20f, rInner+5f);

            float tailX = cx - dirX * halfLen;
            float tailY = cy - dirY * halfLen;
            float tipX  = cx + dirX * halfLen;
            float tipY  = cy + dirY * halfLen;

            // Vettore unitario lungo direzione (tail->tip)
            float ux = tipX - tailX;
            float uy = tipY - tailY;
            float uL = (float) Math.sqrt(ux * ux + uy * uy);
            if (uL < 1e-3f) return;
            ux /= uL;
            uy /= uL;

            // Perpendicolare
            float px = -uy;
            float py = ux;

            // ----------- GEOMETRIA "COME FOTO" -----------
            // spessore tronco (larghezza rettangolo)
            float shaftW = Math.max(32f, rInner * 0.95f);   // ↑ aumenta/diminuisci a gusto
            float halfW  = shaftW * 0.5f;

            // lunghezza punta
            float headLen = Math.max(32f, rInner * 0.55f);

            // piccolo gap tra tronco e punta (come nella foto il tronco si ferma prima)
            float gap = 0;

            // centro base punta
            float baseCx = tipX - ux * headLen;
            float baseCy = tipY - uy * headLen;

            // fine tronco (arretrata rispetto alla base della punta)
            float shaftEndX = baseCx - ux * gap;
            float shaftEndY = baseCy - uy * gap;

            // tronco rettangolare: 4 vertici
            android.graphics.Path shaft = new android.graphics.Path();
            shaft.moveTo(tailX + px * halfW, tailY + py * halfW);
            shaft.lineTo(shaftEndX + px * halfW, shaftEndY + py * halfW);
            shaft.lineTo(shaftEndX - px * halfW, shaftEndY - py * halfW);
            shaft.lineTo(tailX - px * halfW, tailY - py * halfW);
            shaft.close();

            // punta triangolare: tip + due punti base
            float headW = shaftW *1f;     // base punta (più o meno come il tronco)
            float halfHW = headW * 0.5f;

            float b1x = baseCx + px * halfHW;
            float b1y = baseCy + py * halfHW;
            float b2x = baseCx - px * halfHW;
            float b2y = baseCy - py * halfHW;

            android.graphics.Path head = new android.graphics.Path();
            head.moveTo(tipX, tipY);
            head.lineTo(b1x, b1y);
            head.lineTo(b2x, b2y);
            head.close();

            // Disegno pieno (come in foto)
            p.setStyle(Paint.Style.FILL);
            p.setColor(arrowColor);

            if(!showCrossOnly&&DataSaved.Selected_Point3D_Drill!=null) {
                canvas.drawPath(shaft, p);
                canvas.drawPath(head, p);
            }
        }

        // -----------------------------
        // Testo DENTRO la freccia, ruotato con essa
        // -----------------------------
        String txt = (distTextValue);


        float textSize = Math.max(24f, rInner * 0.60f);
        p.setTextSize(textSize);
        p.setFakeBoldText(true);
        p.setTextAlign(Paint.Align.CENTER);

        p.setColor(textColor);
        p.setStyle(Paint.Style.FILL);

        Paint.FontMetrics fm = p.getFontMetrics();
        float textY = cy - (fm.ascent + fm.descent) / 2f;

        canvas.save();
        float rotDeg = (float) screenAngleDeg;
        if (showCrossOnly) {
            p.setColor(Color.DKGRAY);
            rotDeg = 0;
        } else {
            rotDeg = rotDeg + 90;
        }
        rotDeg=rotDeg%360;

        if(rotDeg<270&&rotDeg>90){
            rotDeg+=180;
        }
        if(DataSaved.Selected_Point3D_Drill==null){
            rotDeg=0;
        }
        canvas.rotate(rotDeg, cx, cy);
        canvas.drawText(txt, cx, textY, p);
        canvas.restore();
    }
    private void drawDrillingCircle(Canvas canvas, float cx, float cy) {

        float w = getWidth();
        float h = getHeight();
        float min = Math.min(w, h);

        // stai un po' dentro al bordo
        float margin = Math.max(10f, min * 0.08f);
        float r = (min * 0.5f) - margin;

        float stroke = Math.max(6f, r * 0.12f);

        // --- CERCHIO GIALLO PIENO ---
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.YELLOW);
        canvas.drawCircle(cx, cy, r, p);

        // --- bordo del cerchio (per renderlo leggibile) ---
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(stroke);
        p.setColor(Color.rgb(180, 180, 0)); // giallo più scuro
        canvas.drawCircle(cx, cy, r, p);

        // --- croce grigia ruotata di circleDeg ---
        float crossLen = r * 0.65f;
        float crossStroke = Math.max(3f, stroke * 0.35f);

        canvas.save();
        canvas.rotate(circleDeg, cx, cy);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(crossStroke);
        p.setColor(Color.DKGRAY);

        canvas.drawLine(cx - crossLen, cy, cx + crossLen, cy, p);
        canvas.drawLine(cx, cy - crossLen, cx, cy + crossLen, p);

        canvas.restore();

        // --- testo centrale NON ruota ---
        String t = distTextValue;

        float textSize = Math.max(24f, r * 0.40f);
        p.setTextSize(textSize);
        p.setFakeBoldText(true);
        p.setTextAlign(Paint.Align.CENTER);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.BLUE);

        Paint.FontMetrics fm = p.getFontMetrics();
        float textY = cy - (fm.ascent + fm.descent) / 2f;

        canvas.drawText(t, cx, textY, p);
    }



    public void setDrillingMode(boolean drilling, float circleDeg) {
        this.isDrilling = drilling;
        this.circleDeg = circleDeg;
        invalidate();
    }




    public void setCrossOnly(boolean crossOnly) {
        this.showCrossOnly = crossOnly;
        invalidate();
    }
}
