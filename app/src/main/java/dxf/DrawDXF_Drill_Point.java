package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;

import gui.draw_class.MyColorClass;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;

public class DrawDXF_Drill_Point {
    public static void draw(Canvas canvas, Paint paint, Point3D_Drill point,
                            float bucketX, float bucketY,
                            double bucketEst, double bucketNord,
                            float scala, double rotationAngle,
                            boolean txt,float uiDeg) {

        if (point == null) return;
        if (point.getHeadX() == null || point.getHeadY() == null) return;

        // se è il selected, non lo ridisegno qui (lo disegni con drawSelected)
        Point3D_Drill sel = DataSaved.Selected_Point3D_Drill;
        if (isSamePoint(point, sel)) return;

        // coordinate head
        double diffHX = (point.getHeadX() - bucketEst) * scala;
        double diffHY = (point.getHeadY() - bucketNord) * scala;
        float headX = (float) (bucketX + diffHX * Math.cos(rotationAngle) - diffHY * Math.sin(rotationAngle));
        float headY = (float) (bucketY - diffHX * Math.sin(rotationAngle) - diffHY * Math.cos(rotationAngle));

        // dimensioni (un pelo più piccole del selected)
        float rHead = 0.145f * scala;
        float rEnd = rHead * 0.78f;

        // Colore in base allo stato: 0/TODO = default, 1/DONE = verde, -1/ABORTED = magenta
        Integer st = point.getStatus();
        final boolean isDone = (st != null && st == 1);
        final boolean isAborted = (st != null && st == -1);

        int fillColor;
        int strokeColor;

        if (isDone) {
            fillColor = android.graphics.Color.argb(190, 0, 200, 0);      // verde (fill)
            strokeColor = android.graphics.Color.argb(230, 0, 140, 0);    // verde scuro (stroke)
        } else if (isAborted) {
            fillColor = android.graphics.Color.argb(190, 255, 0, 255);    // magenta (fill)
            strokeColor = android.graphics.Color.argb(230, 180, 0, 180);  // magenta scuro (stroke)
        } else {
            fillColor = MyColorClass.colorConstraint;
            strokeColor = MyColorClass.colorConstraint;
        }


        // stroke
        float stroke = Math.max(1.5f, (float) (0.045 * scala));
        float linkStroke = Math.max(1.5f, (float) (0.040 * scala));
        float axisStroke = Math.max(1.5f, (float) (0.030 * scala));

        paint.setAntiAlias(true);

        // controllo fondo
        boolean hasEnd = (point.getEndX() != null && point.getEndY() != null);

        float endX = headX;
        float endY = headY;

        if (hasEnd) {
            double diffEX = (point.getEndX() - bucketEst) * scala;
            double diffEY = (point.getEndY() - bucketNord) * scala;
            endX = (float) (bucketX + diffEX * Math.cos(rotationAngle) - diffEY * Math.sin(rotationAngle));
            endY = (float) (bucketY - diffEX * Math.sin(rotationAngle) - diffEY * Math.cos(rotationAngle));
        }

        // 1) Se ho fondo: disegno linee cilindro + tappo fondo (sempre colorConstraint)
        if (hasEnd) {
            float vx = endX - headX;
            float vy = endY - headY;
            float len = (float) Math.hypot(vx, vy);

            if (len > 1e-3f) {
                float ux = vx / len;
                float uy = vy / len;
                float nx = -uy;
                float ny = ux;

                float headLx = headX + nx * rHead;
                float headLy = headY + ny * rHead;
                float headRx = headX - nx * rHead;
                float headRy = headY - ny * rHead;

                float endLx = endX + nx * rEnd;
                float endLy = endY + ny * rEnd;
                float endRx = endX - nx * rEnd;
                float endRy = endY - ny * rEnd;

                int lineColor = (isDone || isAborted) ? strokeColor : getCylinderLineColor();


                // asse centrale (doppio stroke per contrasto)
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(axisStroke + 1.2f);
                paint.setColor(android.graphics.Color.argb(160, 0, 0, 0));
                canvas.drawLine(headX, headY, endX, endY, paint);

                paint.setStrokeWidth(axisStroke);
                paint.setColor(lineColor);
                canvas.drawLine(headX, headY, endX, endY, paint);

                // generatrici laterali (doppio stroke)
                paint.setStrokeWidth(linkStroke + 1.0f);
                paint.setColor(android.graphics.Color.argb(140, 0, 0, 0));
                canvas.drawLine(headLx, headLy, endLx, endLy, paint);
                canvas.drawLine(headRx, headRy, endRx, endRy, paint);

                paint.setStrokeWidth(linkStroke);
                paint.setColor(lineColor);
                canvas.drawLine(headLx, headLy, endLx, endLy, paint);
                canvas.drawLine(headRx, headRy, endRx, endRy, paint);
            }

            // tappo fondo (end) - colorConstraint
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fillColor);
            canvas.drawCircle(endX, endY, rEnd, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(strokeColor);
            canvas.drawCircle(endX, endY, rEnd, paint);

            // puntino fondo
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(MyColorClass.colorSfondo);
            paint.setStrokeWidth(Math.max(1f, (float) (0.04 * scala)));
            canvas.drawPoint(endX, endY, paint);
        }

        // 2) Tappo testa (head) - sempre
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        canvas.drawCircle(headX, headY, rHead, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(strokeColor);
        canvas.drawCircle(headX, headY, rHead, paint);

        // puntino testa
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(MyColorClass.colorSfondo);
        paint.setStrokeWidth(Math.max(1f, (float) (0.04 * scala)));
        canvas.drawPoint(headX, headY, paint);

        // 3) Testo: SEMPRE se txt == true (anche senza fondo)
        if (txt) {
            canvas.rotate(-uiDeg,headX,headY);
            String testo = point.getRowId() + "-" + point.getId();
            if (point.getRowId() == null||point.getRowId().isEmpty()) {
                testo = point.getId();
            }
            paint.setTextSize(Math.max(18f, 0.22f * scala));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fillColor); // oppure fillColor, a gusto
            canvas.drawText(testo, headX + 3f, headY - 3f, paint);
            canvas.rotate(uiDeg,headX,headY);
        }

        paint.setStrokeWidth(10f);
    }


    public static void drawSelected(Canvas canvas, Paint paint, Point3D_Drill point,
                                    float bucketX, float bucketY,
                                    double bucketEst, double bucketNord,
                                    float scala, double rotationAngle,
                                    boolean txt, double size,float uiDeg) {

        if (point == null) return;
        if (point.getHeadX() == null || point.getHeadY() == null) return;

        // --- head (alto) ---
        double diffHX = (point.getHeadX() - bucketEst) * scala;
        double diffHY = (point.getHeadY() - bucketNord) * scala;

        float headX = (float) (bucketX + diffHX * Math.cos(rotationAngle) - diffHY * Math.sin(rotationAngle));
        float headY = (float) (bucketY - diffHX * Math.sin(rotationAngle) - diffHY * Math.cos(rotationAngle));

        float rHead = (float) (size * scala);
        float rEnd = rHead * 0.75f;

        // --- end (basso) ---
        boolean hasEnd = (point.getEndX() != null && point.getEndY() != null);

        float endX = headX;
        float endY = headY;

        if (hasEnd) {
            double diffEX = (point.getEndX() - bucketEst) * scala;
            double diffEY = (point.getEndY() - bucketNord) * scala;

            endX = (float) (bucketX + diffEX * Math.cos(rotationAngle) - diffEY * Math.sin(rotationAngle));
            endY = (float) (bucketY - diffEX * Math.sin(rotationAngle) - diffEY * Math.cos(rotationAngle));
        }

        paint.setStrokeWidth(Math.max(0.8f, scala * 0.001f));
        paint.setColor(MyColorClass.colorConstraint);
        canvas.drawLine(bucketX, bucketY, headX, headY, paint);
        // Colori: alto blu, basso rosso, alpha < 1
        // (metti alpha tra 90..200 a gusto; 150 è un buon compromesso)
        final int aFill = 150;
        final int aStroke = 200;

        final int blueFill = android.graphics.Color.argb(aFill, 0, 120, 255);
        final int blueStroke = android.graphics.Color.argb(aStroke, 0, 90, 200);

        final int redFill = android.graphics.Color.argb(aFill, 255, 70, 70);
        final int redStroke = android.graphics.Color.argb(aStroke, 200, 40, 40);

        // spessori scalati
        float stroke = Math.max(2f, (float) (0.06 * scala));
        float linkStroke = Math.max(2f, (float) (0.05 * scala));
        float axisStroke = Math.max(2f, (float) (0.035 * scala));

        // --- se non ho end: disegno solo head "pro" ---
        if (!hasEnd) {
            paint.setAntiAlias(true);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(blueFill);
            canvas.drawCircle(headX, headY, rHead, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(blueStroke);
            canvas.drawCircle(headX, headY, rHead, paint);

            // puntino centrale
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(MyColorClass.colorSfondo);
            paint.setStrokeWidth(Math.max(1f, (float) (0.04 * scala)));
            canvas.drawPoint(headX, headY, paint);
            if (txt) {
                canvas.rotate(-uiDeg,headX,headY);
                float offX = 6f;
                float offY = 6f;
                String testo = point.getRowId() + "-" + point.getId();
                if (point.getRowId() == null||point.getRowId().isEmpty()) {
                    testo = point.getId();
                }
                paint.setTextSize(28);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(MyColorClass.colorConstraint);
                canvas.drawText(testo, headX + offX, headY - offY, paint);
                canvas.rotate(uiDeg,headX,headY);
            }
            return;
        }

        // --- calcolo asse head->end per costruire le "generatrici" laterali ---
        float vx = endX - headX;
        float vy = endY - headY;
        float len = (float) Math.hypot(vx, vy);

        // se head == end (len ~0), fallback a disegno singolo
        if (len < 1e-3f) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(blueFill);
            canvas.drawCircle(headX, headY, rHead, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(blueStroke);
            canvas.drawCircle(headX, headY, rHead, paint);
            if (txt) {
                canvas.rotate(-uiDeg,headX,headY);
                float offX = 6f;
                float offY = 6f;
                String testo = point.getRowId() + "-" + point.getId();
                if (point.getRowId() == null||point.getRowId().isEmpty()) {
                    testo = point.getId();
                }
                paint.setTextSize(28);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(MyColorClass.colorConstraint);
                canvas.drawText(testo, headX + offX, headY - offY, paint);
                canvas.rotate(uiDeg,headX,headY);
            }
            return;
        }

        // unit vector asse
        float ux = vx / len;
        float uy = vy / len;

        // normale (perpendicolare) per i lati cilindro
        float nx = -uy;
        float ny = ux;

        // punti laterali su head (alto) e su end (basso)
        float headLx = headX + nx * rHead;
        float headLy = headY + ny * rHead;
        float headRx = headX - nx * rHead;
        float headRy = headY - ny * rHead;

        float endLx = endX + nx * rEnd;
        float endLy = endY + ny * rEnd;
        float endRx = endX - nx * rEnd;
        float endRy = endY - ny * rEnd;

        paint.setAntiAlias(true);

        int baseLineColor = getCylinderLineColor();

// --- asse centrale (doppio stroke per contrasto) ---

// bordo scuro (ombra)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(axisStroke + 1.5f);
        paint.setColor(android.graphics.Color.argb(180, 0, 0, 0)); // nero semi
        canvas.drawLine(headX, headY, endX, endY, paint);

// linea principale (chiara o scura in base allo sfondo)
        paint.setStrokeWidth(axisStroke);
        paint.setColor(baseLineColor);
        canvas.drawLine(headX, headY, endX, endY, paint);


        // generatrici laterali (destra/sinistra)
        paint.setStrokeWidth(linkStroke);
        // per look più "3D": uso un colore neutro semi-trasparente
        paint.setColor(android.graphics.Color.argb(180, 40, 40, 40));
        canvas.drawLine(headLx, headLy, endLx, endLy, paint);
        canvas.drawLine(headRx, headRy, endRx, endRy, paint);

        // 2) dischi: prima il basso (rosso) poi l’alto (blu) così l’alto “sta sopra”
        // fondo (rosso)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(redFill);
        canvas.drawCircle(endX, endY, rEnd, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(redStroke);
        canvas.drawCircle(endX, endY, rEnd, paint);

        // testa (blu)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(blueFill);
        canvas.drawCircle(headX, headY, rHead, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(blueStroke);
        canvas.drawCircle(headX, headY, rHead, paint);

        // 3) highlight per effetto “cilindro” (leggero)
        // linea sottile lungo l'asse (sopra), molto trasparente
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, stroke * 0.35f));
        paint.setColor(android.graphics.Color.argb(70, 255, 255, 255));
        canvas.drawLine(headX + nx * (rHead * 0.25f), headY + ny * (rHead * 0.25f),
                endX + nx * (rEnd * 0.25f), endY + ny * (rEnd * 0.25f), paint);

        // 4) puntini centrali (opzionali)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(MyColorClass.colorSfondo);
        paint.setStrokeWidth(Math.max(1f, (float) (0.04 * scala)));
        canvas.drawPoint(headX, headY, paint);
        canvas.drawPoint(endX, endY, paint);

        // 5) testo (se ti serve)

        float offX = 6f;
        float offY = 6f;
        String testo = point.getRowId() + "-" + point.getId();
        if (point.getRowId() == null||point.getRowId().isEmpty()) {
            testo = point.getId();
        }
        paint.setTextSize(28);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(MyColorClass.colorConstraint);
        canvas.rotate(-uiDeg,headX,headY);
        canvas.drawText(testo, headX + offX, headY - offY, paint);
        canvas.rotate(uiDeg,headX,headY);


        paint.setStrokeWidth(10f); // ripristino come fai tu altrove
    }


    private static boolean isSamePoint(Point3D_Drill a, Point3D_Drill b) {
        if (a == null || b == null) return false;
        // se id/rowId sono Integer/String, gestisci null
        return safeEq(a.getRowId(), b.getRowId()) && safeEq(a.getId(), b.getId());
    }

    private static boolean safeEq(Object x, Object y) {
        return (x == y) || (x != null && x.equals(y));
    }

    private static int getCylinderLineColor() {
        if (DataSaved.temaSoftware == 0) {
            // sfondo nero → linee chiare
            return android.graphics.Color.argb(200, 200, 200, 200); // grigio chiaro
            // oppure più “tech”:
            // return Color.argb(200, 120, 200, 255); // azzurro chiaro
        } else {
            // sfondo chiaro → linee scure
            return android.graphics.Color.argb(180, 40, 40, 40); // grigio scuro
        }
    }

}