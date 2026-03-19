package gui.my_opengl;

import static gui.my_opengl.MyGLRenderer.panX;
import static gui.my_opengl.MyGLRenderer.panX_2d;
import static gui.my_opengl.MyGLRenderer.panY;
import static gui.my_opengl.MyGLRenderer.panY_2d;
import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.MyGLRenderer.scale_2d;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import packexcalib.exca.DataSaved;

public class MyGLSurfaceView extends GLSurfaceView {

    private static final float ROTATION_SENSITIVITY = 0.1f;
    private static final long ZOOM_GUARD_MS = 250L;
    private static final int MSAA_SAMPLES_HIGH = 4;
    private static final int MSAA_SAMPLES_LOW = 2;
    private static final float PAN_FACTOR_3D = 0.005f;
    private static final float PAN_FACTOR_2D_BASE = 0.005f;
    private static final float MIN_SCALE_3D = 0.04f;
    private static final float MAX_SCALE_3D = 1.5f;
    private static final float MIN_SCALE_2D = 0.05f;
    private static final float MAX_SCALE_2D = 4.5f;
    private static final float ZOOM_SENSITIVITY_3D = 0.35f;
    private static final float ZOOM_SENSITIVITY_2D = 0.5f;

    private  MyGLRenderer renderer;
    private  GestureDetector gestureDetector;
    private  ScaleGestureDetector scaleDetector;

    private float previousX;
    private float previousY;

    private boolean isZooming = false;
    private long lastZoomTime = 0L;


    public MyGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        setEGLConfigChooser(new MultisampleConfigChooser());


        renderer = new MyGLRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        boolean recentlyZoomed = (System.currentTimeMillis() - lastZoomTime) < ZOOM_GUARD_MS;

        if (!isZooming && !recentlyZoomed && event.getPointerCount() == 1) {
            gestureDetector.onTouchEvent(event);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    previousX = event.getX();
                    previousY = event.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    handleSingleFingerMove(event);
                    break;

                default:
                    break;
            }
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            isZooming = false;
        }

        return true;
    }

    private void handleSingleFingerMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        float dx = x - previousX;
        float dy = y - previousY;

        if (!My3DActivity.isPan) {
            handleRotation(dx, dy);
        } else {
            handlePan(dx, dy);
        }

        previousX = x;
        previousY = y;
    }

    private void handleRotation(float dx, float dy) {
        if (DataSaved.lock3dRotation > 0) {
            renderer.angleY_extra += dx * ROTATION_SENSITIVITY;
        } else {
            renderer.angleY += dx * ROTATION_SENSITIVITY;
        }

        renderer.angleX += dy * ROTATION_SENSITIVITY;
        renderer.angleX = Math.max(-110f, Math.min(0f, renderer.angleX));
    }

    private void handlePan(float dx, float dy) {
        if (renderer.is3D) {
            panX += dx * PAN_FACTOR_3D;
            panY -= dy * PAN_FACTOR_3D;
        } else if (renderer.is2D) {
            float safeScale = Math.max(0.0001f, scale_2d);
            float panFactor = (PAN_FACTOR_2D_BASE * renderer.orthoBaseSize) / safeScale;
            panX_2d += dx * panFactor;
            panY_2d -= dy * panFactor;
        }
    }

    private static class MultisampleConfigChooser implements EGLConfigChooser {

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            EGLConfig config = chooseMultisampleConfig(egl, display, MSAA_SAMPLES_HIGH);
            if (config != null) {
                return config;
            }

            config = chooseMultisampleConfig(egl, display, MSAA_SAMPLES_LOW);
            if (config != null) {
                return config;
            }

            config = chooseStandardConfig(egl, display, true);
            if (config != null) {
                return config;
            }

            config = chooseStandardConfig(egl, display, false);
            if (config != null) {
                return config;
            }

            throw new IllegalArgumentException("No suitable EGLConfig found for OpenGL ES 2.0");
        }

        private EGLConfig chooseMultisampleConfig(EGL10 egl, EGLDisplay display, int samples) {
            int[] attribs = {
                    EGL10.EGL_RENDERABLE_TYPE, 4,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 24,
                    EGL10.EGL_STENCIL_SIZE, 0,
                    EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_SAMPLES, samples,
                    EGL10.EGL_NONE
            };
            return findFirstConfig(egl, display, attribs);
        }

        private EGLConfig chooseStandardConfig(EGL10 egl, EGLDisplay display, boolean withAlpha) {
            int alphaSize = withAlpha ? 8 : 0;
            int[] attribs = {
                    EGL10.EGL_RENDERABLE_TYPE, 4,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_ALPHA_SIZE, alphaSize,
                    EGL10.EGL_DEPTH_SIZE, 24,
                    EGL10.EGL_STENCIL_SIZE, 0,
                    EGL10.EGL_NONE
            };
            return findFirstConfig(egl, display, attribs);
        }

        private EGLConfig findFirstConfig(EGL10 egl, EGLDisplay display, int[] attribs) {
            int[] numConfigs = new int[1];
            if (!egl.eglChooseConfig(display, attribs, null, 0, numConfigs) || numConfigs[0] <= 0) {
                return null;
            }

            EGLConfig[] configs = new EGLConfig[numConfigs[0]];
            if (!egl.eglChooseConfig(display, attribs, configs, configs.length, numConfigs)) {
                return null;
            }

            return pickBestConfig(egl, display, configs);
        }

        private EGLConfig pickBestConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            EGLConfig best = null;
            int bestScore = Integer.MIN_VALUE;

            for (EGLConfig config : configs) {
                int red = getConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE);
                int green = getConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE);
                int blue = getConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE);
                int alpha = getConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE);
                int depth = getConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE);
                int sampleBuffers = getConfigAttrib(egl, display, config, EGL10.EGL_SAMPLE_BUFFERS);
                int samples = getConfigAttrib(egl, display, config, EGL10.EGL_SAMPLES);

                int score = 0;
                score += (red >= 8 ? 100 : red);
                score += (green >= 8 ? 100 : green);
                score += (blue >= 8 ? 100 : blue);
                score += (alpha >= 8 ? 50 : alpha);
                score += Math.min(depth, 24);
                score += sampleBuffers > 0 ? 200 : 0;
                score += samples * 20;

                if (score > bestScore) {
                    bestScore = score;
                    best = config;
                }
            }

            return best;
        }

        private int getConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute) {
            int[] value = new int[1];
            if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
                return value[0];
            }
            return 0;
        }
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

            if (renderer.is3D) {
                float adjustedFactor = 1f + (rawFactor - 1f) * ZOOM_SENSITIVITY_3D;
                float newScale = scale * adjustedFactor;
                scale = Math.max(MIN_SCALE_3D, Math.min(newScale, MAX_SCALE_3D));
            } else if (renderer.is2D) {
                float adjustedFactor = 1f + (rawFactor - 1f) * ZOOM_SENSITIVITY_2D;
                float newScale = scale_2d * adjustedFactor;
                scale_2d = Math.max(MIN_SCALE_2D, Math.min(newScale, MAX_SCALE_2D));
            }

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isZooming = false;
            lastZoomTime = System.currentTimeMillis();
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



    public void setDoubleTap() {
        if (renderer.is3D) {
            panX = 0f;
            panY = -0.3f;
        } else if (renderer.is2D) {
            panX_2d = 0f;

            float zoom = Math.max(scale_2d, 0.09f);
            float halfHeight = renderer.orthoBaseSize / zoom;

            // circa 25% sotto il centro
            panY_2d = -halfHeight * 0.25f;
        }
    }
}



