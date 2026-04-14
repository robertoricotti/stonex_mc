package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import java.util.List;

import packexcalib.exca.DataSaved;

public class Draw3DPolyline {

    public static void draw(Paint paint, Canvas canvas, List<Point3D> vertices,
                            float bucketX, float bucketY, double bucketEst, double bucketNord,
                            int color, float scala, double rotationAngle, Polyline polyline) {

        if (vertices == null || vertices.size() < 2) return;

        Path path = new Path();
        path.reset();

        // Primo punto
        Point3D first = vertices.get(0);
        double dx = (first.getX() - bucketEst) * scala;
        double dy = (first.getY() - bucketNord) * scala;
        float prevX = (float) (bucketX + dx * Math.cos(rotationAngle) - dy * Math.sin(rotationAngle));
        float prevY = (float) (bucketY - dx * Math.sin(rotationAngle) - dy * Math.cos(rotationAngle));

        path.moveTo(prevX, prevY);

        for (int i = 1; i < vertices.size(); i++) {
            Point3D curr = vertices.get(i);
            dx = (curr.getX() - bucketEst) * scala;
            dy = (curr.getY() - bucketNord) * scala;

            float currX = (float) (bucketX + dx * Math.cos(rotationAngle) - dy * Math.sin(rotationAngle));
            float currY = (float) (bucketY - dx * Math.sin(rotationAngle) - dy * Math.cos(rotationAngle));

            path.lineTo(currX, currY);

            // Registra ogni segmento per il tap detection
         /*   if (DataSaved.canvasSegment != null) {
                CanvasSegment seg = new CanvasSegment(prevX, prevY, currX, currY, polyline);
                DataSaved.canvasSegment.add(seg);

                Log.d("Segmenti", "Polyline " + polyline.getLayer().getLayerName() +
                        " Segmento [" + prevX + ", " + prevY + "] → [" + currX + ", " + currY + "]");
            }*/

            prevX = currX;
            prevY = currY;

        }

        // Stile paint
        paint.setStrokeWidth((float) (1.5 / DataSaved.scale_Factor3D));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);

        canvas.drawPath(path, paint);
    }

    public static void drawScreen(
            Paint paint,
            Canvas canvas,
            List<PointF> screenPoints,
            int color
    ) {
        if (screenPoints == null || screenPoints.size() < 2) return;

        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);

        Path path = new Path();
        path.moveTo(screenPoints.get(0).x, screenPoints.get(0).y);

        for (int i = 1; i < screenPoints.size(); i++) {
            PointF p = screenPoints.get(i);
            path.lineTo(p.x, p.y);
        }

        canvas.drawPath(path, paint);
    }
}
