package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;

public class DrawCircles {

    public static void draw(Canvas canvas,
                            Paint paint,
                            Circle circle,
                            float bucketX,
                            float bucketY,
                            double bucketEst,
                            double bucketNord,
                            float scala,
                            int color,
                            double rotationAngle) {

        // Calcola la posizione del centro del cerchio con gli offset

        double diffX = (circle.getCenter().getX() - bucketEst) * scala;
        double diffY = (circle.getCenter().getY() - bucketNord) * scala;

        // Calcola la posizione ruotata del centro del cerchio
        float rotatedX = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
        float rotatedY = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));

        // Imposta il colore e lo stile per disegnare il cerchio
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);

        // Disegna il cerchio con il raggio scalato
        float radius = (float) (circle.getRadius() * scala);
        canvas.drawCircle(rotatedX, rotatedY, radius, paint);
    }
}
