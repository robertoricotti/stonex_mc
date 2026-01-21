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

    public static void draw(Canvas canvas, Paint paint, Point3D_Drill point, float bucketX, float bucketY, double bucketEst, double bucketNord, float scala, int color, double rotationAngle, boolean txt, double size) {

        double diffX = (point.getHeadX() - bucketEst) * scala;
        double diffY = (point.getHeadY() - bucketNord) * scala;
        float rotatedXV1 = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
        float rotatedYV1 = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
        float r = ((float) size * scala);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(rotatedXV1, rotatedYV1, r, paint);
        paint.setColor(MyColorClass.colorSfondo);
        paint.setStrokeWidth((float) (0.04 * scala));
        canvas.drawPoint(rotatedXV1, rotatedYV1, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(MyColorClass.colorConstraint);
        paint.setStrokeWidth((float) (0.05 * scala));
        canvas.drawCircle(rotatedXV1, rotatedYV1, r, paint);
        paint.setStrokeWidth((float) (0.035 * scala));
        canvas.drawLine(bucketX, bucketY, rotatedXV1, rotatedYV1, paint);
        paint.setStrokeWidth(10f);

        if (txt) {
            float offX = 4f;//alzare il testo dal punto
            float offY = 3.5f;//sposatre a dx il testo dal punto
            diffX = (point.getHeadX() - bucketEst) * scala;
            diffY = (point.getHeadY() - bucketNord) * scala;
            String testo = point.getRowId() + "-" + point.getId();
            rotatedXV1 = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
            rotatedYV1 = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
            paint.setTextSize(28);
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(testo, rotatedXV1 + offX, rotatedYV1 - offY, paint);
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