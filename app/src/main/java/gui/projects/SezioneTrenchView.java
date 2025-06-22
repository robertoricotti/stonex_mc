package gui.projects;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dxf.Point3D;
import utils.Utils;

public class SezioneTrenchView extends View {

    private Point3D[] points;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SezioneTrenchView(Context context) {
        super(context);
    }

    public SezioneTrenchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPoints(Point3D[] points) {
        this.points = points;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (points == null || points.length < 2) return;

        // Calcolo min/max Z (quote)
        double minZ = points[0].getZ();
        double maxZ = points[0].getZ();

        for (Point3D p : points) {
            minZ = Math.min(minZ, p.getZ());
            maxZ = Math.max(maxZ, p.getZ());
        }

        double zRange = maxZ - minZ;
        if (zRange == 0) zRange = 1.0;

        // Applichiamo la scala verticale 5x
        double visualMinZ = minZ - zRange * 2.5;
        double visualMaxZ = maxZ + zRange * 2.5;
        double visualZRange = visualMaxZ - visualMinZ;

        // Calcolo delle distanze cumulative (X nel canvas)
        List<Double> distances = new ArrayList<>();
        distances.add(0.0); // primo punto

        double totalDistance = 0.0;
        for (int i = 1; i < points.length; i++) {
            double dx = points[i].getX() - points[i - 1].getX();
            double dy = points[i].getY() - points[i - 1].getY();
            double distance = Math.hypot(dx, dy);
            totalDistance += distance;
            distances.add(totalDistance);
        }

        float padding = 40f;
        float width = getWidth();
        float height = getHeight();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(18f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        Path path = new Path();
        Path shading = new Path();

        // Prima passata: mappatura e disegno
        List<Float> xCoords = new ArrayList<>();
        List<Float> yCoords = new ArrayList<>();

        for (int i = 0; i < points.length; i++) {
            float x = padding + (float) (distances.get(i) / totalDistance) * (width - 2 * padding);
            float y = padding + (float) ((visualMaxZ - points[i].getZ()) / visualZRange) * (height - 2 * padding);

            xCoords.add(x);
            yCoords.add(y);

            if (i == 0) {
                path.moveTo(x, y);
                shading.moveTo(x, y);
            } else {
                path.lineTo(x, y);
                shading.lineTo(x, y);
            }

            // Punto
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, y, 6f, paint);

            // Nome del punto sopra
            String nome="";
            if(points[i].getId()!=null) {
                nome = points[i].getId();
            }

            String quota = Utils.readUnitOfMeasureLITE(String.valueOf(points[i].getZ()).replace(",", "."));
            if(i%2==0) {
                canvas.drawText(nome, x, y - 45f, textPaint);
                canvas.drawText(quota, x, y - 25f, textPaint);
            }else {
                canvas.drawText(nome, x, y + 35f, textPaint);
                canvas.drawText(quota, x, y + 55f, textPaint);
            }

            // Linea verticale verso il basso
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(1f);
            if(i%2==0) {
                canvas.drawLine(x, y, x, height - padding, paint);
            }else {
                canvas.drawLine(x, y+60f, x, height - padding, paint);
            }
        }

        // Chiudi la zona ombreggiata sotto la curva
        shading.lineTo(xCoords.get(xCoords.size() - 1), height - padding);
        shading.lineTo(xCoords.get(0), height - padding);
        shading.close();

        // Riempimento
        paint.setColor(Color.argb(85, 50, 50, 50));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(shading, paint);

        // Disegna la polilinea
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        canvas.drawPath(path, paint);
        textPaint.setTextSize(28f);

        // Testi scala
        canvas.drawText("  Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(visualMaxZ))+" "+Utils.getMetriSimbol(), padding + 42f, padding + 25f, textPaint);
        canvas.drawText("  Z: " + Utils.readUnitOfMeasureLITE(String.valueOf(visualMinZ))+" "+Utils.getMetriSimbol(), padding + 42f, height - 10f, textPaint);
    }






}

