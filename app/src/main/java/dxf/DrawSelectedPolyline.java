package dxf;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import java.util.List;

import gui.draw_class.MyColorClass;
import packexcalib.exca.DataSaved;

public class DrawSelectedPolyline {

    public static void draw(Canvas canvas,Paint paint,List<Point3D> vertices, float bucketX, float bucketY, double bucketEst, double bucketNord, int color, float scala, double rotationAngle)

    {
        if (vertices.size() < 2) return; // Una polilinea ha bisogno di almeno 2 punti

        Path path = new Path();
        path.reset();

        // Calcola e sposta il Path al primo punto
        double diffX = (vertices.get(0).getX() - bucketEst) * scala;
        double diffY = (vertices.get(0).getY() - bucketNord) * scala;
        float rotatedX = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
        float rotatedY = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
        path.moveTo(rotatedX, rotatedY);

        // Calcola e traccia i punti successivi
        for (int i = 1; i < vertices.size(); i++) {
            diffX = (vertices.get(i).getX() - bucketEst) * scala;
            diffY = (vertices.get(i).getY() - bucketNord) * scala;
            rotatedX = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
            rotatedY = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
            path.lineTo(rotatedX, rotatedY);
        }

        // Imposta le proprietà del paint
        paint.setStrokeWidth((float) (3 / DataSaved.scale_Factor3D));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(MyColorClass.colorSfondo);
        // Imposta il tratteggio della linea
        // I parametri: [10, 20] significa un tratto di 10 pixel seguito da uno spazio di 20 pixel
        paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        // Disegna il Path
        canvas.drawPath(path, paint);
        // Resetta l'effetto per evitare effetti indesiderati su altre linee
        paint.setPathEffect(null);

        paint.setStrokeWidth((float) (3 / DataSaved.scale_Factor3D));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        // Imposta il tratteggio della linea
        // I parametri: [10, 20] significa un tratto di 10 pixel seguito da uno spazio di 20 pixel
        paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 10));
        // Disegna il Path
        canvas.drawPath(path, paint);
        // Resetta l'effetto per evitare effetti indesiderati su altre linee
        paint.setPathEffect(null);
    }
    public static void drawScreen(
            Canvas canvas,
            Paint paint,
            List<PointF> screenPoints,
            int color
    ) {
        if (screenPoints == null || screenPoints.size() < 2) return;

        Path path = new Path();
        path.moveTo(screenPoints.get(0).x, screenPoints.get(0).y);

        for (int i = 1; i < screenPoints.size(); i++) {
            PointF p = screenPoints.get(i);
            path.lineTo(p.x, p.y);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth((float) (2.0 / Math.max(DataSaved.scale_Factor3D, 0.001f)));

        canvas.drawPath(path, paint);
    }
}
