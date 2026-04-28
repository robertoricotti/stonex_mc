package gui.my_opengl;

import static gui.my_opengl.MyGLRenderer.angleX;
import static gui.my_opengl.MyGLRenderer.angleY;
import static gui.my_opengl.MyGLRenderer.angleY_extra;
import static gui.my_opengl.MyGLRenderer.atlas;
import static gui.my_opengl.MyGLRenderer.atlasPNEZD;
import static gui.my_opengl.MyGLRenderer.charSpacingFactor;
import static gui.my_opengl.MyGLRenderer.coloreAttacco;
import static gui.my_opengl.MyGLRenderer.coloreBoom;
import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;
import static gui.my_opengl.MyGLRenderer.coloreAttaccoScuro;
import static gui.my_opengl.MyGLRenderer.coloreBoomScuro;
import static gui.my_opengl.MyGLRenderer.orthoBaseSize;
import static gui.my_opengl.MyGLRenderer.panX;
import static gui.my_opengl.MyGLRenderer.panX_2d;
import static gui.my_opengl.MyGLRenderer.panY;
import static gui.my_opengl.MyGLRenderer.panY_2d;
import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.MyGLRenderer.scale_2d;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.WHEELLOADER;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import gui.draw_class.MyColorClass;
import gui.my_opengl.compat.GL11;
import gui.my_opengl.dozer.GL_DrawDozer;
import gui.my_opengl.dozer.WheelRender;
import gui.my_opengl.exca.BennaRenderer;
import gui.my_opengl.exca.BoomsDrawer;
import gui.my_opengl.exca.GL_DrawExca;
import gui.my_opengl.wheel.GL_DrawWheel;
import packexcalib.exca.DataSaved;
import packexcalib.gnss.NmeaListener;
import services.TriangleService;
import utils.MyData;

public class MyGLRenderer_Create implements MyGLSurfaceView_Create.Renderer {
    private static final float MIN_2D_SCALE = 0.001f;
    private static final float DEFAULT_CAMERA_Z = -5f;
    private static final float ORTHO_NEAR = -50f;
    private static final float ORTHO_FAR = 50f;
    private static final float PERSPECTIVE_NEAR = 0.1f;
    private static final float PERSPECTIVE_FAR = 100f;
    private static final float PERSPECTIVE_FOV = 45f;
    private int surfaceWidth;
    private int surfaceHeight;
    public boolean is2D, is3D;
    private final GL11 gl11 = new GL11();
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] vpMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        try {
            GLDrawer.resetGlState();
            Cylinder.resetGlState();
            BoomsDrawer.resetGlState();
            BennaRenderer.resetGlState();
            WheelRender.resetGlState();
            GLDrawer.init();
            atlas = new FontAtlas(32, MyColorClass.colorConstraint);
            atlasPNEZD = new FontAtlas(26, MyColorClass.colorConstraint);

            if (MyData.get_String("colorBucket") == null)
                MyData.push("colorBucket", String.valueOf(Color.DKGRAY));
            if (MyData.get_String("colorBoom") == null)
                MyData.push("colorBoom", String.valueOf(Color.CYAN));
            if (MyData.get_String("colorQuick") == null)
                MyData.push("colorQuick", String.valueOf(Color.GRAY));
        } catch (Exception e) {
            Log.e("GL_CREATE", "Errore onSurfaceCreated", e);

        }


        configureCommonGlState();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        is2D = My3DActivity.glVista3d == 0;
        is3D = My3DActivity.glVista3d == 1;

        if (DataSaved.typeView != 0 && DataSaved.typeView != 1) {
            clearFrameForFlatMode();
            return;
        }

        prepareFrameClear();

        try {
            float angleTest = getHeadingAngle();

            setupProjection();
            setupViewMatrix(angleTest);
            Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
            GL11.setCurrentViewProjectionMatrix(vpMatrix);
            GLDrawer.setViewMatrix(viewMatrix);
            GLDrawer.setViewProjectionMatrix(vpMatrix);
            setupCompatMatrix(angleTest);

            if (is3D) {
                drawTerrain3D();
            }

            if (is2D) {
                drawTerrain2D();
            }
            GLDrawer.drawSnapHelpers(gl11);
            drawMachine();

        } catch (Exception e) {
            Log.e("GL_CREATE", "Errore onDrawFrame", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        surfaceWidth = Math.max(1, width);
        surfaceHeight = Math.max(1, height);
        coloreEsterno = GL_Methods.darkenColor(GL_Methods.parseColorToGL(MyColorClass.colorBucket), 1, 1);
        coloreInterno = GL_Methods.darkenColor(coloreEsterno, 0.5f, 0.95f);
        coloreAttacco = GL_Methods.darkenColor(GL_Methods.parseColorToGL(Color.GRAY), 1, 1);
        coloreAttaccoScuro = GL_Methods.darkenColor(coloreAttacco, 0.75f, 1f);
        coloreBoom = GL_Methods.darkenColor(GL_Methods.parseColorToGL(MyColorClass.colorStick), 1, 1);
        coloreBoomScuro = GL_Methods.darkenColor(coloreBoom, 0.75f, 1f);
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        GLDrawer.setViewportSize(surfaceWidth, surfaceHeight);
    }

    private void configureCommonGlState() {
        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
    }

    private void clearFrameForFlatMode() {
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    private void prepareFrameClear() {
        if (is3D) {
            float[] sf = GL_Methods.parseColorToGL(MyColorClass.colorSfondo);
            GLES20.glClearColor(sf[0], sf[1], sf[2], sf[3]);
        } else {
            // GLES20.glClearColor(0f, 0f, 0f, 0f);
            float[] sf = GL_Methods.parseColorToGL(MyColorClass.colorSfondo);
            GLES20.glClearColor(sf[0], sf[1], sf[2], sf[3]);
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    private float getHeadingAngle() {
        return (float) ((NmeaListener.mch_Orientation + DataSaved.deltaGPS2) % 360f);
    }

    private void setupCompatMatrix(float angleTest) {
        gl11.setProjectionMatrix(projectionMatrix);
        gl11.glMatrixMode(GL11.GL_MODELVIEW);
        gl11.glLoadIdentity();

        if (is3D) {
            gl11.glTranslatef(panX, panY, DEFAULT_CAMERA_Z);
            gl11.glScalef(scale, scale, scale);
            gl11.glRotatef(angleX, 1f, 0f, 0f);
            if (DataSaved.lock3dRotation > 0) {
                gl11.glRotatef(angleTest + angleY_extra, 0f, 0f, 1f);
            } else {
                gl11.glRotatef(angleY, 0f, 0f, 1f);
            }
        } else if (is2D) {
            gl11.glTranslatef(panX_2d, panY_2d, DEFAULT_CAMERA_Z);
            gl11.glRotatef(angleTest, 0f, 0f, 1f);
        }
    }

    private void drawMachine() {
        switch (DataSaved.isWL) {
            case EXCAVATOR:
                GL_DrawExca.draw(gl11);
                break;
            case WHEELLOADER:
                GL_DrawWheel.draw(gl11);
                break;
            case DOZER:
            case DOZER_SIX:
                GL_DrawDozer.draw(gl11);


            case GRADER:
                GL_DrawDozer.draw(gl11);
                break;
        }
    }

    private void setupProjection() {
        float ratio = (float) surfaceWidth / (float) surfaceHeight;

        if (is3D) {
            Matrix.perspectiveM(projectionMatrix, 0, PERSPECTIVE_FOV, ratio, PERSPECTIVE_NEAR, PERSPECTIVE_FAR);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLDrawer.setOrthoMetrics(false, 1f, 1f);
        } else {
            float zoom = Math.max(scale_2d, MIN_2D_SCALE);
            float halfHeight = orthoBaseSize / zoom;
            float halfWidth = ratio * halfHeight;
            Matrix.orthoM(projectionMatrix, 0, -halfWidth, halfWidth, -halfHeight, halfHeight, ORTHO_NEAR, ORTHO_FAR);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLDrawer.setOrthoMetrics(true, halfWidth, halfHeight);
        }
    }

    private void setupViewMatrix(float angleTest) {
        Matrix.setIdentityM(viewMatrix, 0);

        if (is3D) {
            Matrix.translateM(viewMatrix, 0, panX, panY, DEFAULT_CAMERA_Z);
            Matrix.scaleM(viewMatrix, 0, scale, scale, scale);
            Matrix.rotateM(viewMatrix, 0, angleX, 1f, 0f, 0f);

            if (DataSaved.lock3dRotation > 0) {
                Matrix.rotateM(viewMatrix, 0, angleTest + angleY_extra, 0f, 0f, 1f);
            } else {
                Matrix.rotateM(viewMatrix, 0, angleY, 0f, 0f, 1f);
            }
        } else if (is2D) {
            Matrix.translateM(viewMatrix, 0, panX_2d, panY_2d, DEFAULT_CAMERA_Z);
            Matrix.rotateM(viewMatrix, 0, angleTest, 0f, 0f, 1f);
        }
    }

    private void drawTerrain3D() {
        float scale = 1f;

        if(MyGLActivity_Create.gFacce) {
            GLDrawer.drawFaces(gl11, DataSaved.dxfFaces_Create, 0.8f, scale, false,MyGLActivity_Create.gFill,MyGLActivity_Create.gFacce);
        }


        GLES20.glDisable(GLES20.GL_DEPTH_TEST);


        if(MyGLActivity_Create.gPoly) {
            GLDrawer.drawPolylines(gl11, DataSaved.polylines_Create, 3f, scale);
        }
        if(MyGLActivity_Create.gPoint) {
            GLDrawer.drawPoints(gl11, DataSaved.points_Create, 10f, scale, false);
        }

        if(MyGLActivity_Create.gText) {
            GLDrawer.drawTextsBilBoard(gl11, DataSaved.dxfTexts_Create, DataSaved.glL_AnchorView, charSpacingFactor, scale, atlas);
        }

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    }

    private void drawTerrain2D() {
        android.util.Log.e("GL_CREATE_DRAW",
                "2D DRAW"
                        + " points=" + (DataSaved.points_Create == null ? -1 : DataSaved.points_Create.size())
                        + " faces=" + (DataSaved.dxfFaces_Create == null ? -1 : DataSaved.dxfFaces_Create.size())
                        + " polylines=" + (DataSaved.polylines_Create == null ? -1 : DataSaved.polylines_Create.size())
                        + " anchor=" + java.util.Arrays.toString(DataSaved.glL_AnchorView)
                        + " scale2d=" + MyGLRenderer.scale_2d
                        + " pan2d=" + MyGLRenderer.panX_2d + "," + MyGLRenderer.panY_2d
        );
        if(MyGLActivity_Create.gFacce) {
            GLDrawer.drawFaces2D(gl11, DataSaved.dxfFaces_Create, 0.8f, 1f, false,MyGLActivity_Create.gFill,MyGLActivity_Create.gFacce);
        }


        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        if(MyGLActivity_Create.gPoly) {
            GLDrawer.drawPolylines(gl11, DataSaved.polylines_Create, 3f, 1f);
        }

        if(MyGLActivity_Create.gPoint) {
            GLDrawer.drawPoints(gl11, DataSaved.points_Create, 10f, 1f, false);
        }


        if(MyGLActivity_Create.gText) {
            GLDrawer.drawTextsBilBoard(gl11, DataSaved.dxfTexts_Create, DataSaved.glL_AnchorView, charSpacingFactor, 1f, atlas);
        }


        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    }

    public static float currentRenderScale() {
        return 1f;
    }
}
