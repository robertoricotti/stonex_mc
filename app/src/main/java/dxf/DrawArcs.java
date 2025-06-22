package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import packexcalib.exca.DataSaved;
import packexcalib.gnss.NmeaListener;

public class DrawArcs {

    public static void draw(Canvas canvas,Paint paint,Arc arc,float bucketX,float bucketY,double bucketEst,double bucketNord,float scala,int color,double rotationAngle){



            // Salva lo stato del canvas
            canvas.save();

            // Ruota il canvas attorno al centro del bucket

            canvas.rotate((float) -rotationAngle, bucketX, bucketY);

            // Calcola la posizione dell'arco rispetto al centro del bucket
            double diffX = (arc.getCenter().getX() - bucketEst) * scala;
            double diffY = (arc.getCenter().getY() - bucketNord) * scala;
            float rotatedX = (float) (bucketX + diffX);
            float rotatedY = (float) (bucketY - diffY);

            // Calcola il raggio dell'arco
            float radius = (float) (arc.getRadius() * scala);

            // Definisce il rettangolo che contiene il cerchio completo
            RectF oval = new RectF(
                    rotatedX - radius, // sinistra
                    rotatedY - radius, // alto
                    rotatedX + radius, // destra
                    rotatedY + radius  // basso
            );

            // Calcola l'angolo iniziale e il sweepAngle nel sistema di riferimento del canvas
            float startAngle = ((float) arc.getStartAngle()) % 360; // Angolo iniziale
            float sweepAngle = ((float) (arc.getEndAngle() - arc.getStartAngle())) % 360;

            // Correggi sweepAngle per evitare valori negativi
            if (sweepAngle < 0) {
                sweepAngle += 360;
            }

            // Inverti startAngle per allinearlo al sistema orario del canvas
            startAngle = (360 - startAngle) % 360;

            // Imposta il colore e lo stile per disegnare l'arco
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);

            // Disegna l'arco
            canvas.drawArc(oval, startAngle, -sweepAngle, false, paint);

            // Ripristina lo stato del canvas
            canvas.restore();

    }
}
