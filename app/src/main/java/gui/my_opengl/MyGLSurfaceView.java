package gui.my_opengl;

import static gui.my_opengl.MyGLRenderer.panX;
import static gui.my_opengl.MyGLRenderer.panY;
import static gui.my_opengl.MyGLRenderer.scale;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import packexcalib.exca.DataSaved;

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer renderer;
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleDetector;

    private float previousX;
    private float previousY;

    private boolean isZooming = false;
    private long lastZoomTime = 0; // <-- nuovo

    private final float ROTATION_SENSITIVITY = 0.1f;

    public MyGLSurfaceView(Context context) {
        super(context);
        // ✅ Abilita MSAA (4x) per avere bordi più morbidi
        // Parametri: RGBA 8 bit, Depth 24 bit, 4x multisample
        setEGLConfigChooser(8, 8, 8, 8, 24, 4);
        setEGLContextClientVersion(1);

        renderer = new MyGLRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        // Blocca la rotazione/pan per 150ms dopo lo zoom
        boolean recentlyZoomed = (System.currentTimeMillis() - lastZoomTime < 250);//250 tempo di inattività dopo rilascio del tocco

        if (!isZooming && !recentlyZoomed && event.getPointerCount() == 1) {
            gestureDetector.onTouchEvent(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    float y = event.getY();

                    float dx = x - previousX;
                    float dy = y - previousY;

                    if (!My3DActivity.isPan) {

                        if(DataSaved.lock3dRotation>0){
                            renderer.angleY_extra += dx * ROTATION_SENSITIVITY;
                            renderer.angleX += dy * ROTATION_SENSITIVITY;
                            renderer.angleX = Math.max(-110f, Math.min(0f, renderer.angleX));
                        }else {
                            renderer.angleY += dx * ROTATION_SENSITIVITY;
                            renderer.angleX += dy * ROTATION_SENSITIVITY;
                            renderer.angleX = Math.max(-110f, Math.min(0f, renderer.angleX));
                        }
                    } else {
                        float panFactor = 0.005f;
                        panX += dx * panFactor;
                        panY -= dy * panFactor;
                    }


                    previousX = x;
                    previousY = y;
                    break;

                case MotionEvent.ACTION_DOWN:
                    previousX = event.getX();
                    previousY = event.getY();
                    break;
            }
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            isZooming = false;
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isZooming = true;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float rawFactor = detector.getScaleFactor();

            // Applichiamo un fattore di attenuazione per rendere lo zoom più lento
            float zoomSensitivity = 0.25f; // più basso = zoom meno aggressivo (es. 0.1f–0.3f)
            float adjustedFactor = 1f + (rawFactor - 1f) * zoomSensitivity;

            scale *= adjustedFactor;
            scale = Math.max(0.09f, Math.min(scale, 1.5f));

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isZooming = false;
            lastZoomTime = System.currentTimeMillis(); // <-- importante
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

          /*  if (!My3DActivity.isPan) {
                renderer.angleX = -90f;
                renderer.angleY = 0f;
            }*/
            panX = 0;
            panY = -0.3f;


            return true;
        }
    }



}
