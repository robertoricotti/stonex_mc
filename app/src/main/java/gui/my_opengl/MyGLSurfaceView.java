package gui.my_opengl;

import static gui.my_opengl.MyGLRenderer.panX;
import static gui.my_opengl.MyGLRenderer.panX_2d;
import static gui.my_opengl.MyGLRenderer.panY;
import static gui.my_opengl.MyGLRenderer.panY_2d;
import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.MyGLRenderer.scale_2d;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import gui.draw_class.Top_View_DXF;
import packexcalib.exca.DataSaved;

public class MyGLSurfaceView extends GLSurfaceView {
    private Top_View_DXF topView;

    private final MyGLRenderer renderer;
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleDetector;

    private float previousX;
    private float previousY;

    private boolean isZooming = false;
    private long lastZoomTime = 0; // <-- nuovo

    private final float ROTATION_SENSITIVITY = 0.1f;
    private GLSurfaceView glSurfaceView;

    public MyGLSurfaceView(Context context, GLSurfaceView glSurfaceView) {
        super(context);
        this.glSurfaceView = glSurfaceView;

        setEGLConfigChooser(8, 8, 8, 8, 24, 4);
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);

        updateZOrder();

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

                        if (DataSaved.lock3dRotation > 0) {
                            renderer.angleY_extra += dx * ROTATION_SENSITIVITY;
                            renderer.angleX += dy * ROTATION_SENSITIVITY;
                            renderer.angleX = Math.max(-110f, Math.min(0f, renderer.angleX));
                        } else {
                            renderer.angleY += dx * ROTATION_SENSITIVITY;
                            renderer.angleX += dy * ROTATION_SENSITIVITY;
                            renderer.angleX = Math.max(-110f, Math.min(0f, renderer.angleX));
                        }
                    } else {
                        if (renderer.is3D) {
                            float panFactor = 0.005f;
                            panX += dx * panFactor;
                            panY -= dy * panFactor;

                        } else if (renderer.is2D) {
                            float panFactor = (0.005f * renderer.orthoBaseSize) / scale_2d;
                            panX_2d += dx * panFactor;
                            panY_2d -= dy * panFactor;


                        }

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
            if (renderer.is3D) {
                float rawFactor = detector.getScaleFactor();
                float zoomSensitivity = 0.35f;
                float adjustedFactor = 1f + (rawFactor - 1f) * zoomSensitivity;
                float oldScale = scale;
                float newScale = oldScale * adjustedFactor;
                newScale = Math.max(0.09f, Math.min(newScale, 1.5f));

                scale = newScale;
            } else if (renderer.is2D) {
                float rawFactor = detector.getScaleFactor();
                float zoomSensitivity = 0.5f;
                float adjustedFactor = 1f + (rawFactor - 1f) * zoomSensitivity;
                float oldScale = scale_2d;
                float newScale = oldScale * adjustedFactor;
                newScale = Math.max(0.09f, Math.min(newScale, 4.5f));
                scale_2d = newScale;

            }


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

            setDoubleTap();

            return true;
        }
    }

    public void updateZOrder() {
        if (My3DActivity.glVista3d == 0) {
            setZOrderOnTop(false);
            getHolder().setFormat(android.graphics.PixelFormat.TRANSLUCENT);
        } else {
            setZOrderOnTop(false);
            getHolder().setFormat(android.graphics.PixelFormat.OPAQUE);
        }
    }

    public void setTopView(Top_View_DXF topView) {
        this.topView = topView;
    }

    public void setDoubleTap() {
        if (renderer.is3D) {
            panX = 0;
            panY = -0.3f;
        } else if (renderer.is2D) {
            panX_2d = 0f;
            panY_2d = -1f;
        }
    }
}
