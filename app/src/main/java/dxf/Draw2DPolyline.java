package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.List;


public class Draw2DPolyline {

    public static void draw(Paint paint, Canvas canvas,List<Point3D> vertices, float bucketX, float bucketY, double bucketEst, double bucketNord,int color,float scala,double rotationAngle,double scaleFactor){

            if (vertices.size() < 2) return; // Una polilinea ha bisogno di almeno 2 punti

            // Angolo di rotazione basato sui dati ricevuti


            // Calcola il centro della benna


            // Inizializza il Paint per le linee e gli archi
            paint.setStrokeWidth((float) (1.5 /scaleFactor));
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);

            // Itera su tutti i punti per disegnare le linee e gli archi
            for (int i = 0; i < vertices.size() - 1; i++) {
                Point3D currentVertex = vertices.get(i);
                Point3D nextVertex = vertices.get(i + 1);

                // Calcola le coordinate scalate e ruotate per il punto corrente
                double diffX = (currentVertex.getX() - bucketEst) * scala;
                double diffY = (currentVertex.getY() - bucketNord) * scala;
                float startX = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
                float startY = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));

                // Calcola le coordinate scalate e ruotate per il punto successivo
                diffX = (nextVertex.getX() - bucketEst) * scala;
                diffY = (nextVertex.getY() - bucketNord) * scala;
                float endX = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
                float endY = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));

                float bulge = (float) currentVertex.getBulge();

                if (bulge == 0) {
                    // Se il bulge è zero, disegna una linea retta tra i due punti
                    canvas.drawLine(startX, startY, endX, endY, paint);
                } else {
                    // Se il bulge è diverso da zero, disegna un arco tra i due punti
                    if (Math.abs(bulge) > 1) {
                        drawArcBetweenPointsM(canvas, new PointF(startX, startY), new PointF(endX, endY), -bulge, paint);
                    } else {
                        drawArcBetweenPoints(canvas, new PointF(startX, startY), new PointF(endX, endY), -bulge, paint);
                    }
                }
            }

    }

    private static void drawArcBetweenPointsM(Canvas canvas, PointF startPoint, PointF endPoint, double bulge, Paint paint) {
        // Step 1: Calcola la lunghezza della corda (distanza tra i punti di inizio e fine)
        double chordLength = Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2));

        // Step 2: Calcola l'angolo di sweep usando il bulge
        double theta = 4 * Math.atan(Math.abs(bulge));

        // Step 3: Calcola il raggio dell'arco
        double radius = Math.abs((chordLength / 2) / Math.sin(theta / 2));

        // Step 4: Calcola il punto medio della corda
        float midX = (startPoint.x + endPoint.x) / 2;
        float midY = (startPoint.y + endPoint.y) / 2;

        // Step 5: Calcola la distanza perpendicolare dal punto medio al centro dell'arco
        double sagitta = Math.sqrt(radius * radius - (chordLength / 2) * (chordLength / 2));

        // Step 6: Determina la direzione perpendicolare alla corda
        float dx = endPoint.x - startPoint.x;
        float dy = endPoint.y - startPoint.y;
        float perpX = -dy;
        float perpY = dx;

        // Normalizza la direzione perpendicolare
        float norm = (float) Math.sqrt(perpX * perpX + perpY * perpY);
        perpX /= norm;
        perpY /= norm;

        // Step 7: Calcola il centro dell'arco (aggiustato in base al segno del bulge)
        float centerX = midX + perpX * (float) sagitta * (bulge > 0 ? -1 : 1);
        float centerY = midY + perpY * (float) sagitta * (bulge > 0 ? -1 : 1);

        // Step 8: Definisce il rettangolo di delimitazione dell'arco
        RectF boundingRect = new RectF(
                centerX - (float) radius,
                centerY - (float) radius,
                centerX + (float) radius,
                centerY + (float) radius
        );

        // Step 9: Calcola gli angoli di partenza e di sweep
        float startAngle = (float) Math.toDegrees(Math.atan2(startPoint.y - centerY, startPoint.x - centerX));
        float endAngle = (float) Math.toDegrees(Math.atan2(endPoint.y - centerY, endPoint.x - centerX));

        // Step 10: Calcola l'angolo di sweep
        float sweepAngle;
        if (bulge > 0) {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle < 0) {
                sweepAngle += 360;  // Assicura che lo sweep sia in senso antiorario
            }
        } else {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle > 0) {
                sweepAngle -= 360;  // Assicura che lo sweep sia in senso orario
            }
        }

        // Step 11: Disegna l'arco
        canvas.drawArc(boundingRect, startAngle, sweepAngle, false, paint);
    }

    private static void drawArcBetweenPoints(Canvas canvas, PointF startPoint, PointF endPoint, double bulge, Paint paint) {
        // Step 1: Calcola la lunghezza della corda
        double distance = Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2));

        // Step 2: Calcola l'angolo di sweep usando il bulge
        double theta = 4 * Math.atan(Math.abs(bulge));

        // Step 3: Calcola il raggio dell'arco
        double radius = (distance / 2) / Math.abs(Math.sin(theta / 2));

        // Step 4: Calcola il punto medio della corda
        float midX = (startPoint.x + endPoint.x) / 2;
        float midY = (startPoint.y + endPoint.y) / 2;

        // Step 5: Calcola la distanza perpendicolare dal punto medio al centro dell'arco
        double height = Math.sqrt(radius * radius - (distance / 2) * (distance / 2));

        // Step 6: Determina la direzione perpendicolare alla corda
        float dx = endPoint.x - startPoint.x;
        float dy = endPoint.y - startPoint.y;
        float perpX = -dy;
        float perpY = dx;

        // Normalizza la direzione perpendicolare
        float norm = (float) Math.sqrt(perpX * perpX + perpY * perpY);
        perpX /= norm;
        perpY /= norm;

        // Step 7: Calcola il centro dell'arco (aggiustato in base al segno del bulge)
        float centerX = midX + perpX * (float) height * (bulge > 0 ? 1 : -1);
        float centerY = midY + perpY * (float) height * (bulge > 0 ? 1 : -1);

        // Step 8: Definisce il rettangolo di delimitazione dell'arco
        RectF boundingRect = new RectF(
                centerX - (float) radius,
                centerY - (float) radius,
                centerX + (float) radius,
                centerY + (float) radius
        );

        // Step 9: Calcola gli angoli di partenza e di sweep
        float startAngle = (float) Math.toDegrees(Math.atan2(startPoint.y - centerY, startPoint.x - centerX));
        float endAngle = (float) Math.toDegrees(Math.atan2(endPoint.y - centerY, endPoint.x - centerX));

        // Calcola l'angolo di sweep
        float sweepAngle;
        if (bulge > 0) {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle < 0) {
                sweepAngle += 360;  // Assicura che lo sweep sia in senso antiorario
            }
        } else {
            sweepAngle = endAngle - startAngle;
            if (sweepAngle > 0) {
                sweepAngle -= 360;  // Assicura che lo sweep sia in senso orario
            }
        }

        // Step 10: Correggi lo sweep dell'arco se necessario
        if (Math.abs(bulge) > 1 && Math.abs(sweepAngle) < 180) {
            sweepAngle = (bulge > 0) ? 360 - Math.abs(sweepAngle) : -(360 - Math.abs(sweepAngle));
        }

        // Step 11: Disegna l'arco
        canvas.drawArc(boundingRect, startAngle, sweepAngle, false, paint);
    }
}
