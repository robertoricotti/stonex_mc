package dxf;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.List;

import gui.draw_class.MyColorClass;

public class OverviewGenerator {

    public static Bitmap generateOverviewBitmap(List<Point3D> points, List<Polyline> polylines, List<Polyline_2D> polylines_2D, List<Face3D> dxfFaces, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        // Sfondo nero
        canvas.drawColor(Color.BLACK);

        // Disegna i triangoli (3DFACE)
        if (dxfFaces != null) {
            for (Face3D face : dxfFaces) {
                paint.setColor(AutoCADColor.getColor(String.valueOf(face.getLayer().getColorState())));
                paint.setStyle(Paint.Style.FILL_AND_STROKE);

                Path path = new Path();
                path.moveTo((float) face.p1.getX(), (float) face.p1.getY());
                path.lineTo((float) face.p2.getX(), (float) face.p2.getY());
                path.lineTo((float) face.p3.getX(), (float) face.p3.getY());
                path.close();
                canvas.drawPath(path, paint);
            }
        }

        // Disegna le polilinee
        if (polylines != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            for (Polyline polyline : polylines) {
                paint.setColor(AutoCADColor.getColor(String.valueOf(polyline.getLayer().getColorState())));
                Path path = new Path();
                boolean first = true;
                for (Point3D vertex : polyline.getVertices()) {
                    if (first) {
                        path.moveTo((float) vertex.getX(), (float) vertex.getY());
                        first = false;
                    } else {
                        path.lineTo((float) vertex.getX(), (float) vertex.getY());
                    }
                }
                canvas.drawPath(path, paint);
            }
        }
        // Disegna le polilinee2D
        if (polylines_2D != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            for (Polyline_2D polyline_2D : polylines_2D) {
                paint.setColor(AutoCADColor.getColor(String.valueOf(polyline_2D.getLayer().getColorState())));
                Path path = new Path();
                boolean first = true;
                for (Point3D vertex : polyline_2D.getVertices()) {
                    if (first) {
                        path.moveTo((float) vertex.getX(), (float) vertex.getY());
                        first = false;
                    } else {
                        path.lineTo((float) vertex.getX(), (float) vertex.getY());
                    }
                }
                canvas.drawPath(path, paint);
            }
        }

        // Disegna i punti
        if (points != null) {
            paint.setStyle(Paint.Style.FILL);
            for (Point3D point : points) {
                paint.setColor(MyColorClass.colorPoint);
                canvas.drawCircle((float) point.getX(), (float) point.getY(), 5, paint);
            }
        }

        return bitmap;
    }


    private static Path createPath(float[] points, int count) {
        Path path = new Path();
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < count; i += 2) {
            path.lineTo(points[i], points[i + 1]);
        }
        path.close();
        return path;
    }
}

