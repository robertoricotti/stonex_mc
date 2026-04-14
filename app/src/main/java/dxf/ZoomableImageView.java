package dxf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ZoomableImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Matrix matrix;
    private float[] matrixValues;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    private float scale = 1f; // Zoom iniziale
    private float minScale = 1f;
    private float maxScale = 5f;

    private float lastTouchX;
    private float lastTouchY;
    private boolean isPanning;

    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        matrix = new Matrix();
        matrixValues = new float[9];

        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        gestureDetector = new GestureDetector(getContext(), new GestureListener());

        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.concat(matrix);
        super.onDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isPanning = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isPanning) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;

                    matrix.postTranslate(dx, dy);
                    invalidate();

                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                isPanning = false;
                break;
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            scale *= scaleFactor;

            if (scale < minScale) scale = minScale;
            if (scale > maxScale) scale = maxScale;

            matrix.setScale(scale, scale, detector.getFocusX(), detector.getFocusY());
            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Ripristina zoom al doppio tap
            matrix.reset();
            scale = 1f;
            invalidate();
            return true;
        }
    }
}
