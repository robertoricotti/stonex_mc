package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;

import gui.draw_class.MyColorClass;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import utils.Utils;

public class DrawDXF_Drill_Point {

    // =========================================================
    // CUSTOM UI SIZE (pixel desiderati A SCHERMO)
    // Questi valori vengono automaticamente compensati con
    // / DataSaved.scale_Factor3D
    // =========================================================

    // --- NORMAL ---
    private static final float UI_HEAD_RADIUS_PX = 10.0f;          // cerchio testa
    private static final float UI_END_RADIUS_RATIO = 0.78f;        // raggio fondo = head * ratio

    private static final float UI_TEXT_SIZE_PX = 21.0f;            // testo normale
    private static final float UI_TEXT_OFFSET_X_PX = 5.0f;         // offset testo X
    private static final float UI_TEXT_OFFSET_Y_PX = 5.0f;         // offset testo Y

    private static final float UI_STROKE_PX = 2.4f;                // bordo cerchi
    private static final float UI_LINK_STROKE_PX = 1.9f;           // generatrici laterali
    private static final float UI_AXIS_STROKE_PX = 1.5f;           // asse cilindro
    private static final float UI_CENTER_POINT_STROKE_PX = 1.4f;   // puntino centrale

    private static final float UI_RING_RADIUS_MULT = 1.55f;        // anello rosso attorno alla testa
    private static final float UI_RING_STROKE_PX = 2.8f;           // spessore anello rosso

    private static final float UI_AXIS_SHADOW_EXTRA_PX = 1.2f;     // bordo scuro asse
    private static final float UI_LINK_SHADOW_EXTRA_PX = 1.0f;     // bordo scuro lati

    // --- SELECTED ---
    private static final float UI_SELECTED_HEAD_RADIUS_PX = 13.0f;
    private static final float UI_SELECTED_END_RADIUS_RATIO = 0.75f;

    private static final float UI_SELECTED_TEXT_SIZE_PX = 31.0f;
    private static final float UI_SELECTED_TEXT_OFFSET_X_PX = 7.0f;
    private static final float UI_SELECTED_TEXT_OFFSET_Y_PX = 7.0f;

    private static final float UI_SELECTED_STROKE_PX = 2.8f;
    private static final float UI_SELECTED_LINK_STROKE_PX = 2.2f;
    private static final float UI_SELECTED_AXIS_STROKE_PX = 1.8f;

    private static final float UI_SELECTED_AXIS_SHADOW_EXTRA_PX = 1.5f;
    private static final float UI_SELECTED_HIGHLIGHT_STROKE_PX = 1.0f;

    // --- MINIMI DI SICUREZZA ---
    private static final float UI_MIN_STROKE_PX = 1.0f;
    private static final float UI_MIN_SCALE = 0.1f;

    public static void draw(Canvas canvas, Paint paint, Point3D_Drill point,
                            float bucketX, float bucketY,
                            double bucketEst, double bucketNord,
                            float scala, double rotationAngle,
                            boolean txt, float uiDeg) {
        //Log.d("myScala",DataSaved.scale_Factor3D+"");
        if (point == null) return;
        if (point.getHeadX() == null || point.getHeadY() == null) return;

        // se è selected, non lo ridisegno qui
        Point3D_Drill sel = DataSaved.Selected_Point3D_Drill;
        if (isSamePoint(point, sel)) return;

        // ---------------------------------------------------------
        // POSIZIONE: continua a dipendere da scala (corretto)
        // ---------------------------------------------------------
        double diffHX = (point.getHeadX() - bucketEst) * scala;
        double diffHY = (point.getHeadY() - bucketNord) * scala;

        float headX = (float) (bucketX + diffHX * Math.cos(rotationAngle) - diffHY * Math.sin(rotationAngle));
        float headY = (float) (bucketY - diffHX * Math.sin(rotationAngle) - diffHY * Math.cos(rotationAngle));

        // ---------------------------------------------------------
        // DIMENSIONI VISIVE: fisse a schermo (compensate col canvas scale)
        // ---------------------------------------------------------
        float uiScale = getUiCompensationScale();

        float rHead = px(UI_HEAD_RADIUS_PX);
        float rEnd = rHead * UI_END_RADIUS_RATIO;

        float stroke = px(UI_STROKE_PX, UI_MIN_STROKE_PX);
        float linkStroke = px(UI_LINK_STROKE_PX, UI_MIN_STROKE_PX);
        float axisStroke = px(UI_AXIS_STROKE_PX, UI_MIN_STROKE_PX);
        float centerPointStroke = px(UI_CENTER_POINT_STROKE_PX, UI_MIN_STROKE_PX);

        float axisShadowExtra = px(UI_AXIS_SHADOW_EXTRA_PX);
        float linkShadowExtra = px(UI_LINK_SHADOW_EXTRA_PX);

        // ---------------------------------------------------------
        // STATO / COLORI
        // 0/TODO = default
        // 1/DONE = verde
        // -1/ABORTED = rosso
        // ---------------------------------------------------------
        Integer st = point.getStatus();
        final boolean isDone = (st != null && st == 1);
        final boolean isAborted = (st != null && st == -1);

        int fillColor;
        int strokeColor;

        if (isDone) {
            fillColor = android.graphics.Color.argb(190, 0, 200, 0);
            strokeColor = android.graphics.Color.argb(230, 0, 140, 0);
        } else if (isAborted) {
            fillColor = android.graphics.Color.argb(190, 255, 0, 0);
            strokeColor = android.graphics.Color.argb(230, 250, 0, 55);
        } else {
            fillColor = MyColorClass.colorConstraint;
            strokeColor = MyColorClass.colorConstraint;
        }

        paint.setAntiAlias(true);

        // ---------------------------------------------------------
        // END POINT
        // ---------------------------------------------------------
        boolean hasEnd = (point.getEndX() != null && point.getEndY() != null);

        float endX = headX;
        float endY = headY;

        if (hasEnd) {
            double diffEX = (point.getEndX() - bucketEst) * scala;
            double diffEY = (point.getEndY() - bucketNord) * scala;

            endX = (float) (bucketX + diffEX * Math.cos(rotationAngle) - diffEY * Math.sin(rotationAngle));
            endY = (float) (bucketY - diffEX * Math.sin(rotationAngle) - diffEY * Math.cos(rotationAngle));
        }

        // ---------------------------------------------------------
        // CILINDRO / FONDO
        // ---------------------------------------------------------
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

                // asse centrale - shadow
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(axisStroke + axisShadowExtra);
                paint.setColor(android.graphics.Color.argb(160, 0, 0, 0));
                canvas.drawLine(headX, headY, endX, endY, paint);

                // asse centrale - main
                paint.setStrokeWidth(axisStroke);
                paint.setColor(lineColor);
                canvas.drawLine(headX, headY, endX, endY, paint);

                // generatrici - shadow
                paint.setStrokeWidth(linkStroke + linkShadowExtra);
                paint.setColor(android.graphics.Color.argb(140, 0, 0, 0));
                canvas.drawLine(headLx, headLy, endLx, endLy, paint);
                canvas.drawLine(headRx, headRy, endRx, endRy, paint);

                // generatrici - main
                paint.setStrokeWidth(linkStroke);
                paint.setColor(lineColor);
                canvas.drawLine(headLx, headLy, endLx, endLy, paint);
                canvas.drawLine(headRx, headRy, endRx, endRy, paint);
            }

            // tappo fondo
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
            paint.setStrokeWidth(centerPointStroke);
            canvas.drawPoint(endX, endY, paint);
        }

        // ---------------------------------------------------------
        // TAPPO TESTA
        // ---------------------------------------------------------
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        canvas.drawCircle(headX, headY, rHead, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(strokeColor);
        canvas.drawCircle(headX, headY, rHead, paint);

        // cerchio rosso se A
        if (isAlignA(point)) {
            drawRedRing(canvas, paint, headX, headY, rHead * UI_RING_RADIUS_MULT);
        }

        // puntino testa
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(MyColorClass.colorSfondo);
        paint.setStrokeWidth(centerPointStroke);
        canvas.drawPoint(headX, headY, paint);

        // ---------------------------------------------------------
        // TESTO
        // ---------------------------------------------------------
        if (txt) {
            canvas.rotate(-uiDeg, headX, headY);

            String testo = point.getRowId() + "-" + point.getId();
            if (point.getRowId() == null || point.getRowId().isEmpty()) {
                testo = point.getId();
            }

            String des = " ";
            if (point.getDescription() != null) {
                des = point.getDescription();
            }

            testo = switch (DataSaved.Drill_Text_Mode) {
                case 0 -> testo;
                case 1 -> testo + "\n" + des;
                case 2 ->
                        testo + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                case 3 ->
                        testo + "\n" + des + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                case 4 -> des;
                case 5 ->
                        des + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                case 6 -> Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                default -> "?";
            };

            paint.setTextSize(px(UI_TEXT_SIZE_PX));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fillColor);
            canvas.drawText(testo, headX + px(UI_TEXT_OFFSET_X_PX), headY - px(UI_TEXT_OFFSET_Y_PX), paint);

            canvas.rotate(uiDeg, headX, headY);
        }

        paint.setStrokeWidth(10f);
    }


    public static void drawSelected(Canvas canvas, Paint paint, Point3D_Drill point,
                                    float bucketX, float bucketY,
                                    double bucketEst, double bucketNord,
                                    float scala, double rotationAngle,
                                    boolean txt, double size, float uiDeg) {

        if (point == null) return;
        if (point.getHeadX() == null || point.getHeadY() == null) return;

        // ---------------------------------------------------------
        // HEAD POSITION
        // ---------------------------------------------------------
        double diffHX = (point.getHeadX() - bucketEst) * scala;
        double diffHY = (point.getHeadY() - bucketNord) * scala;

        float headX = (float) (bucketX + diffHX * Math.cos(rotationAngle) - diffHY * Math.sin(rotationAngle));
        float headY = (float) (bucketY - diffHX * Math.sin(rotationAngle) - diffHY * Math.cos(rotationAngle));

        // ---------------------------------------------------------
        // DIMENSIONI VISIVE FISSE A SCHERMO
        // ---------------------------------------------------------
        float uiScale = getUiCompensationScale();

        float rHead = px(UI_SELECTED_HEAD_RADIUS_PX);
        float rEnd = rHead * UI_SELECTED_END_RADIUS_RATIO;

        float stroke = px(UI_SELECTED_STROKE_PX, UI_MIN_STROKE_PX);
        float linkStroke = px(UI_SELECTED_LINK_STROKE_PX, UI_MIN_STROKE_PX);
        float axisStroke = px(UI_SELECTED_AXIS_STROKE_PX, UI_MIN_STROKE_PX);

        float axisShadowExtra = px(UI_SELECTED_AXIS_SHADOW_EXTRA_PX);
        float highlightStroke = px(UI_SELECTED_HIGHLIGHT_STROKE_PX, UI_MIN_STROKE_PX);
        float centerPointStroke = px(UI_CENTER_POINT_STROKE_PX, UI_MIN_STROKE_PX);

        // ---------------------------------------------------------
        // END POSITION
        // ---------------------------------------------------------
        boolean hasEnd = (point.getEndX() != null && point.getEndY() != null);

        float endX = headX;
        float endY = headY;

        if (hasEnd) {
            double diffEX = (point.getEndX() - bucketEst) * scala;
            double diffEY = (point.getEndY() - bucketNord) * scala;

            endX = (float) (bucketX + diffEX * Math.cos(rotationAngle) - diffEY * Math.sin(rotationAngle));
            endY = (float) (bucketY - diffEX * Math.sin(rotationAngle) - diffEY * Math.cos(rotationAngle));
        }

        // linea bucket -> head
        paint.setStrokeWidth(px(1.2f, 0.8f));
        paint.setColor(MyColorClass.colorConstraint);
        canvas.drawLine(bucketX, bucketY, headX, headY, paint);

        // colori selected
        final int aFill = 150;
        final int aStroke = 200;

        final int blueFill = android.graphics.Color.argb(aFill, 0, 120, 255);
        final int blueStroke = android.graphics.Color.argb(aStroke, 0, 90, 200);

        final int redFill = android.graphics.Color.argb(aFill, 255, 70, 70);
        final int redStroke = android.graphics.Color.argb(aStroke, 200, 40, 40);

        // ---------------------------------------------------------
        // CASO SENZA END
        // ---------------------------------------------------------
        if (!hasEnd) {
            paint.setAntiAlias(true);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(blueFill);
            canvas.drawCircle(headX, headY, rHead, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(blueStroke);
            canvas.drawCircle(headX, headY, rHead, paint);

            if (isAlignA(point)) {
                drawRedRing(canvas, paint, headX, headY, rHead * UI_RING_RADIUS_MULT);
            }

            // puntino centrale
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(MyColorClass.colorSfondo);
            paint.setStrokeWidth(centerPointStroke);
            canvas.drawPoint(headX, headY, paint);

            if (txt) {
                canvas.rotate(-uiDeg, headX, headY);

                String testo = point.getRowId() + "-" + point.getId();
                if (point.getRowId() == null || point.getRowId().isEmpty()) {
                    testo = point.getId();
                }

                String des = " ";
                if (point.getDescription() != null) {
                    des = point.getDescription();
                }

                testo = switch (DataSaved.Drill_Text_Mode) {
                    case 0 -> testo;
                    case 1 -> testo + "\n" + des;
                    case 2 ->
                            testo + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                    case 3 ->
                            testo + "\n" + des + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                    case 4 -> des;
                    case 5 ->
                            des + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                    case 6 -> Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                    default -> "?";
                };

                paint.setTextSize(px(UI_SELECTED_TEXT_SIZE_PX));
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(MyColorClass.colorConstraint);
                canvas.drawText(testo,
                        headX + px(UI_SELECTED_TEXT_OFFSET_X_PX),
                        headY - px(UI_SELECTED_TEXT_OFFSET_Y_PX),
                        paint);

                canvas.rotate(uiDeg, headX, headY);
            }

            paint.setStrokeWidth(10f);
            return;
        }

        // ---------------------------------------------------------
        // CASO CON END
        // ---------------------------------------------------------
        float vx = endX - headX;
        float vy = endY - headY;
        float len = (float) Math.hypot(vx, vy);

        // fallback se head == end
        if (len < 1e-3f) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(blueFill);
            canvas.drawCircle(headX, headY, rHead, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(blueStroke);
            canvas.drawCircle(headX, headY, rHead, paint);

            if (isAlignA(point)) {
                drawRedRing(canvas, paint, headX, headY, rHead * UI_RING_RADIUS_MULT);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(MyColorClass.colorSfondo);
            paint.setStrokeWidth(centerPointStroke);
            canvas.drawPoint(headX, headY, paint);

            if (txt) {
                canvas.rotate(-uiDeg, headX, headY);

                String testo = point.getRowId() + "-" + point.getId();
                if (point.getRowId() == null || point.getRowId().isEmpty()) {
                    testo = point.getId();
                }

                String des = " ";
                if (point.getDescription() != null) {
                    des = point.getDescription();
                }

                testo = switch (DataSaved.Drill_Text_Mode) {
                    case 0 -> testo;
                    case 1 -> testo + "\n" + des;
                    case 2 ->
                            testo + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                    case 3 ->
                            testo + "\n" + des + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                    case 4 -> des;
                    case 5 ->
                            des + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                    case 6 -> Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                    default -> "?";
                };

                paint.setTextSize(px(UI_SELECTED_TEXT_SIZE_PX));
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(MyColorClass.colorConstraint);
                canvas.drawText(testo,
                        headX + px(UI_SELECTED_TEXT_OFFSET_X_PX),
                        headY - px(UI_SELECTED_TEXT_OFFSET_Y_PX),
                        paint);

                canvas.rotate(uiDeg, headX, headY);
            }

            paint.setStrokeWidth(10f);
            return;
        }

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

        paint.setAntiAlias(true);

        int baseLineColor = getCylinderLineColor();

        // asse shadow
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(axisStroke + axisShadowExtra);
        paint.setColor(android.graphics.Color.argb(180, 0, 0, 0));
        canvas.drawLine(headX, headY, endX, endY, paint);

        // asse main
        paint.setStrokeWidth(axisStroke);
        paint.setColor(baseLineColor);
        canvas.drawLine(headX, headY, endX, endY, paint);

        // generatrici laterali
        paint.setStrokeWidth(linkStroke);
        paint.setColor(android.graphics.Color.argb(180, 40, 40, 40));
        canvas.drawLine(headLx, headLy, endLx, endLy, paint);
        canvas.drawLine(headRx, headRy, endRx, endRy, paint);

        // fondo rosso
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(redFill);
        canvas.drawCircle(endX, endY, rEnd, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(redStroke);
        canvas.drawCircle(endX, endY, rEnd, paint);

        // testa blu
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(blueFill);
        canvas.drawCircle(headX, headY, rHead, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(blueStroke);
        canvas.drawCircle(headX, headY, rHead, paint);

        if (isAlignA(point)) {
            drawRedRing(canvas, paint, headX, headY, rHead * UI_RING_RADIUS_MULT);
        }

        // highlight leggero
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(highlightStroke);
        paint.setColor(android.graphics.Color.argb(70, 255, 255, 255));
        canvas.drawLine(headX + nx * (rHead * 0.25f), headY + ny * (rHead * 0.25f),
                endX + nx * (rEnd * 0.25f), endY + ny * (rEnd * 0.25f), paint);

        // puntini centrali
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(MyColorClass.colorSfondo);
        paint.setStrokeWidth(centerPointStroke);
        canvas.drawPoint(headX, headY, paint);
        canvas.drawPoint(endX, endY, paint);

        // testo
        if (txt) {
            String testo = point.getRowId() + "-" + point.getId();
            if (point.getRowId() == null || point.getRowId().isEmpty()) {
                testo = point.getId();
            }

            String des = " ";
            if (point.getDescription() != null) {
                des = point.getDescription();
            }

            testo = switch (DataSaved.Drill_Text_Mode) {
                case 0 -> testo;
                case 1 -> testo + "\n" + des;
                case 2 ->
                        testo + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                case 3 ->
                        testo + "\n" + des + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                case 4 -> des;
                case 5 ->
                        des + "\n" + Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                case 6 -> Utils.readUnitOfMeasureLITE(String.valueOf(point.getHeadZ()));
                default -> "?";
            };

            paint.setTextSize(px(UI_SELECTED_TEXT_SIZE_PX));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(MyColorClass.colorConstraint);

            canvas.rotate(-uiDeg, headX, headY);
            canvas.drawText(testo,
                    headX + px(UI_SELECTED_TEXT_OFFSET_X_PX),
                    headY - px(UI_SELECTED_TEXT_OFFSET_Y_PX),
                    paint);
            canvas.rotate(uiDeg, headX, headY);
        }

        paint.setStrokeWidth(10f);
    }


    private static boolean isSamePoint(Point3D_Drill a, Point3D_Drill b) {
        if (a == null || b == null) return false;
        return safeEq(a.getRowId(), b.getRowId()) && safeEq(a.getId(), b.getId());
    }

    private static boolean safeEq(Object x, Object y) {
        return (x == y) || (x != null && x.equals(y));
    }

    private static int getCylinderLineColor() {
        if (DataSaved.temaSoftware == 0) {
            // sfondo nero -> linee chiare
            return android.graphics.Color.argb(200, 200, 200, 200);
        } else {
            // sfondo chiaro -> linee scure
            return android.graphics.Color.argb(180, 40, 40, 40);
        }
    }

    private static boolean isAlignA(Point3D_Drill p) {
        if (p == null) return false;
        if (!DataSaved.isDefiningAB) return false;

        String aKey = DataSaved.alignAId;
        if (aKey == null || aKey.trim().isEmpty()) return false;

        String pk = pointKey(p);
        if (pk == null) return false;

        return pk.equalsIgnoreCase(aKey.trim());
    }

    private static String pointKey(Point3D_Drill p) {
        if (p == null) return null;

        String row = p.getRowId();
        String id = p.getId();

        row = (row == null) ? null : row.trim();
        id = (id == null) ? null : id.trim();

        if (id == null || id.isEmpty()) return null;

        if (row != null && !row.isEmpty()) {
            return row + "-" + id;
        }
        return id;
    }

    private static void drawRedRing(Canvas canvas, Paint paint, float cx, float cy, float radius) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(px(UI_RING_STROKE_PX, UI_MIN_STROKE_PX));
        paint.setColor(android.graphics.Color.argb(230, 255, 0, 0));
        canvas.drawCircle(cx, cy, radius, paint);
    }

    private static float getUiCompensationScale() {
        float s = (float) Math.max(UI_MIN_SCALE, DataSaved.scale_Factor3D);
        return Math.min(s, 1f);
    }

    private static float px(float desiredScreenPx) {
        return desiredScreenPx / getUiCompensationScale();
    }

    private static float px(float desiredScreenPx, float minLocalValue) {
        return Math.max(minLocalValue / getUiCompensationScale(),
                desiredScreenPx / getUiCompensationScale());
    }

}