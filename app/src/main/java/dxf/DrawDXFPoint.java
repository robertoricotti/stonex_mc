package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;

import gui.draw_class.MyColorClass;

public class DrawDXFPoint {
    public static void draw(Canvas canvas,Paint paint,Point3D point,float bucketX,float bucketY, double bucketEst, double bucketNord, float scala,int color,double rotationAngle) {


        double diffX = (point.getX() - bucketEst) * scala;
        double diffY = (point.getY() - bucketNord) * scala;
        float rotatedXV1 = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
        float rotatedYV1 = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
        float r= (0.15f*scala);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(rotatedXV1, rotatedYV1,r, paint);
        paint.setColor(MyColorClass.colorSfondo);
        paint.setStrokeWidth((float) (0.04*scala));
        canvas.drawPoint(rotatedXV1,rotatedYV1,paint);
        paint.setStrokeWidth(10f);

    }
}
