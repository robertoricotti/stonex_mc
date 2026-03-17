package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import packexcalib.exca.DataSaved;

public class DrawLines {

    public static void draw(Canvas canvas,Paint paint,Line line,float bucketX,float bucketY, double bucketEst, double bucketNord, float scala,int color,double rotationAngle) {
        paint.setStrokeWidth((float) (1.5 / DataSaved.scale_Factor3D));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        double diffX1 = (line.getStart().getX() - bucketEst) * scala;
        double diffY1 = (line.getStart().getY() - bucketNord) * scala;
        float rotatedXV1 = (float) (bucketX + diffX1 * Math.cos(rotationAngle) - diffY1 * Math.sin(rotationAngle));
        float rotatedYV1 = (float) (bucketY - diffX1 * Math.sin(rotationAngle) - diffY1 * Math.cos(rotationAngle));

        double diffX2 = (line.getEnd().getX() - bucketEst) * scala;
        double diffY2 = (line.getEnd().getY() - bucketNord) * scala;
        float rotatedXV2 = (float) (bucketX + diffX2 * Math.cos(rotationAngle) - diffY2 * Math.sin(rotationAngle));
        float rotatedYV2 = (float) (bucketY - diffX2 * Math.sin(rotationAngle) - diffY2 * Math.cos(rotationAngle));
        canvas.drawLine(rotatedXV1, rotatedYV1, rotatedXV2, rotatedYV2, paint);
    }
    public static void drawScreen(
            Canvas canvas,
            Paint paint,
            PointF p1,
            PointF p2,
            int color
    ) {
        paint.setColor(color);
        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
    }
}
