package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;

import gui.draw_class.MyColorClass;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;

public class DrawDXF_Drill_Point {
    public static void draw(Canvas canvas, Paint paint, Point3D_Drill point, float bucketX, float bucketY, double bucketEst, double bucketNord, float scala, int color, double rotationAngle, boolean txt) {

        double diffX = (point.getHeadX() - bucketEst) * scala;
        double diffY = (point.getHeadY() - bucketNord) * scala;
        float rotatedXV1 = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
        float rotatedYV1 = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
        float r = (0.15f * scala);
        Point3D_Drill sel = DataSaved.Selected_Point3D_Drill;
        if (!isSamePoint(point, sel)) {

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            canvas.drawCircle(rotatedXV1, rotatedYV1, r, paint);
            paint.setColor(MyColorClass.colorSfondo);
            paint.setStrokeWidth((float) (0.04 * scala));
            canvas.drawPoint(rotatedXV1, rotatedYV1, paint);
            paint.setStrokeWidth(10f);

            if (txt) {
                float offX = 3f;//alzare il testo dal punto
                float offY = 3f;//sposatre a dx il testo dal punto
                diffX = (point.getHeadX() - bucketEst) * scala;
                diffY = (point.getHeadY() - bucketNord) * scala;
                String testo = point.getRowId() + "-" + point.getId();
                rotatedXV1 = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
                rotatedYV1 = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
                paint.setTextSize(24);
                paint.setColor(color);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(testo, rotatedXV1 + offX, rotatedYV1 - offY, paint);

            }
        }

    }

    public static void draw(Canvas canvas, Paint paint, Point3D_Drill point,
                            float bucketX, float bucketY,
                            double bucketEst, double bucketNord,
                            float scala, int color, double rotationAngle,
                            boolean txt, double size) {

        if (point == null) return;
        if (point.getHeadX() == null || point.getHeadY() == null) return;

        // --- testa (head) ---
        double diffHX = (point.getHeadX() - bucketEst) * scala;
        double diffHY = (point.getHeadY() - bucketNord) * scala;

        float headX = (float) (bucketX + diffHX * Math.cos(rotationAngle) - diffHY * Math.sin(rotationAngle));
        float headY = (float) (bucketY - diffHX * Math.sin(rotationAngle) - diffHY * Math.cos(rotationAngle));

        float rHead = (float) (size * scala);
        float rEnd  = rHead * 0.75f;

        // --- fondo (end) ---
        boolean hasEnd = (point.getEndX() != null && point.getEndY() != null);

        float endX = headX;
        float endY = headY;

        if (hasEnd) {
            double diffEX = (point.getEndX() - bucketEst) * scala;
            double diffEY = (point.getEndY() - bucketNord) * scala;

            endX = (float) (bucketX + diffEX * Math.cos(rotationAngle) - diffEY * Math.sin(rotationAngle));
            endY = (float) (bucketY - diffEX * Math.sin(rotationAngle) - diffEY * Math.cos(rotationAngle));
        }

        // --- linea centro-centro (se ho il fondo) ---
        if (hasEnd) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(MyColorClass.colorConstraint);
            paint.setStrokeWidth((float) (0.035 * scala));
            canvas.drawLine(headX, headY, endX, endY, paint);
        }

        // --- disegno testa ---
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(headX, headY, rHead, paint);

        paint.setColor(MyColorClass.colorSfondo);
        paint.setStrokeWidth((float) (0.04 * scala));
        canvas.drawPoint(headX, headY, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(MyColorClass.colorConstraint);
        paint.setStrokeWidth((float) (0.05 * scala));
        canvas.drawCircle(headX, headY, rHead, paint);

        // --- disegno fondo ---
        if (hasEnd) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            canvas.drawCircle(endX, endY, rEnd, paint);

            paint.setColor(MyColorClass.colorSfondo);
            paint.setStrokeWidth((float) (0.04 * scala));
            canvas.drawPoint(endX, endY, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(MyColorClass.colorConstraint);
            paint.setStrokeWidth((float) (0.05 * scala));
            canvas.drawCircle(endX, endY, rEnd, paint);
        }

        paint.setStrokeWidth(10f); // ripristino come avevi tu

        // --- testo (lo metto vicino alla testa) ---
        if (false) {
            float offX = 4f;
            float offY = 3.5f;
            String testo = point.getRowId() + "-" + point.getId();

            paint.setTextSize(28);
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(testo, headX + offX, headY - offY, paint);
        }
    }


    private static boolean isSamePoint(Point3D_Drill a, Point3D_Drill b) {
        if (a == null || b == null) return false;
        // se id/rowId sono Integer/String, gestisci null
        return safeEq(a.getRowId(), b.getRowId()) && safeEq(a.getId(), b.getId());
    }

    private static boolean safeEq(Object x, Object y) {
        return (x == y) || (x != null && x.equals(y));
    }

}