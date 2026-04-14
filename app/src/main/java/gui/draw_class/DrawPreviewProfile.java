package gui.draw_class;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;


public class DrawPreviewProfile extends View {
    Paint paint;
    public ArrayList<PointF> pPoints;
    public float scala = 1000f;
    public float mScaleFactor = 1.0f;

    public float offsetX, offsetY;

    public int vista = 0;

    public DrawPreviewProfile(Context context, ArrayList<PointF> points) {
        super(context);
        paint = new Paint();
        pPoints = points;
        translateTouch();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);

        float displayWidth = getWidth();
        float displayHeight = getHeight();

        float stroke = 5;
        float radius = 7;

        if (vista == 0) {
            paint.setColor(Color.GREEN);
            if (pPoints.size() == 1) {
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, radius, paint);
            } else if (pPoints.size() > 1) {
                canvas.save();
                canvas.scale(mScaleFactor, mScaleFactor, getWidth() / 2f, getHeight() / 2f);
                canvas.translate(offsetX, offsetY);
                paint.setStrokeWidth(stroke);

                float totalLength = 0.0f;

                for (int i = 1; i < pPoints.size(); i++) {
                    totalLength += Math.abs(pPoints.get(i).x - pPoints.get(i - 1).x);
                }
                float xOffset = (displayWidth - totalLength * scala) / 2; // spostamento delle coordinate x

                ArrayList<PointF> drawPoints = new ArrayList<>();

                for (int i = 0; i < pPoints.size(); i++) {
                    float xp = pPoints.get(i).x * scala + xOffset;
                    float yp = -pPoints.get(i).y * scala + displayHeight / 2f; // inverte il segno di y e somma displayHeight
                    drawPoints.add(new PointF(xp, yp));
                }

                paint.setColor(Color.GREEN);

                float mx = (drawPoints.get(0).x + drawPoints.get(drawPoints.size() - 1).x) / 2f;
                float my = (drawPoints.get(0).y + drawPoints.get(drawPoints.size() - 1).y) / 2f;

                float offsetX = displayWidth * 0.5f - (mx);
                float offsetY = displayHeight * 0.5f - (my);

                for (PointF pts : drawPoints) {
                    pts.x = pts.x + offsetX;
                    pts.y = pts.y + offsetY;
                }

                for (int i = 0; i < drawPoints.size() - 1; i++) {
                    canvas.drawLine(drawPoints.get(i).x, drawPoints.get(i).y, drawPoints.get(i + 1).x, drawPoints.get(i + 1).y, paint);
                }

                for (PointF pts : drawPoints) {
                    canvas.drawCircle(pts.x, pts.y, radius, paint);
                }

                canvas.restore();
            } else {
                paint.setTextSize(80);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("PROFILE EMPTY!", getWidth() / 2f, (getHeight() / 2f - ((paint.descent() + paint.ascent()) / 2)), paint);
            }
        } else if (vista == 1) {
            paint.setColor(Color.MAGENTA);
            if (pPoints.size() == 1) {
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, radius, paint);
            } else if (pPoints.size() > 1) {
                canvas.save();
                canvas.scale(mScaleFactor, mScaleFactor, getWidth() / 2f, getHeight() / 2f);
                canvas.translate(offsetX, offsetY);
                paint.setStrokeWidth(stroke);

                float totalLength = 0.0f;

                for (int i = 1; i < pPoints.size(); i++) {
                    totalLength += Math.abs(pPoints.get(i).x - pPoints.get(i - 1).x);
                }
                float xOffset = (displayWidth - totalLength * scala) / 2; // spostamento delle coordinate x

                ArrayList<PointF> drawPoints = new ArrayList<>();

                for (int i = 0; i < pPoints.size(); i++) {
                    float xp = pPoints.get(i).x * scala + xOffset;
                    float yp = -pPoints.get(i).y * scala + displayHeight / 2f; // inverte il segno di y e somma displayHeight
                    drawPoints.add(new PointF(xp, yp));
                }

                float mx = (drawPoints.get(0).x + drawPoints.get(drawPoints.size() - 1).x) / 2f;
                float my = (drawPoints.get(0).y + drawPoints.get(drawPoints.size() - 1).y) / 2f;

                float offsetX = displayWidth * 0.5f - (mx);
                float offsetY = displayHeight * 0.5f - (my);

                for (PointF pts : drawPoints) {
                    pts.x = pts.x + offsetX;
                    pts.y = pts.y + offsetY;
                }

                float livello = (Math.abs(drawPoints.get(drawPoints.size() - 1).x - drawPoints.get(0).x) * 0.1f);

                PointF yMaxPoint = Collections.max(drawPoints, (p1, p2) -> Float.compare(p1.y, p2.y));

                float yMax = yMaxPoint.y;

                float pointMaxX = Collections.max(pPoints, (p1, p2) -> Float.compare(p1.x, p2.x)).x;

                paint.setStrokeWidth((3 * pointMaxX) / 8.5f);

                Path path = new Path();
                path.moveTo(drawPoints.get(0).x, drawPoints.get(0).y);
                for (int i = 1; i <= drawPoints.size() - 1; i++) {
                    path.lineTo(drawPoints.get(i).x, drawPoints.get(i).y);
                }
                path.lineTo(drawPoints.get(drawPoints.size() - 1).x, yMax + livello);
                path.lineTo(drawPoints.get(0).x, yMax + livello);
                canvas.drawPath(path, paint);
                path.reset();

                paint.setColor(Color.BLACK);
                for (int i = 1; i < drawPoints.size() - 1; i++) {
                    canvas.drawLine(drawPoints.get(i).x, drawPoints.get(i).y, drawPoints.get(i).x, yMax + livello, paint);
                }
                canvas.restore();
            } else {
                paint.setTextSize(80);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("PROFILE EMPTY!", getWidth() / 2f, (getHeight() / 2f - ((paint.descent() + paint.ascent()) / 2)), paint);
            }
        }
    }

    private void translateTouch() {
        offsetX = 0;
        offsetY = 0;
        setOnTouchListener(new OnTouchListener() {
            float lastTouchX, lastTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (pPoints.size() > 1) {
                    float x = event.getX() * scala / 1000f;
                    float y = event.getY() * scala / 1000f;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            lastTouchX = x;
                            lastTouchY = y;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            offsetX += x - lastTouchX;
                            offsetY += y - lastTouchY;
                            lastTouchX = x;
                            lastTouchY = y;
                            invalidate();
                            break;
                        default:
                            return false;
                    }
                }
                return true;
            }
        });
    }
}
