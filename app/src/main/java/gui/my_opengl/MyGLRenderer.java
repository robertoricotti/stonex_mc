package gui.my_opengl;

import static gui.my_opengl.My3DActivity.PNEZD_FUNCTION;
import static gui.my_opengl.My3DActivity.glPoint;
import static utils.MyTypes.DOZER;
import static utils.MyTypes.DOZER_SIX;
import static utils.MyTypes.EXCAVATOR;
import static utils.MyTypes.GRADER;
import static utils.MyTypes.WHEELLOADER;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

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

public class MyGLRenderer implements GLSurfaceView.Renderer {
    public static float orthoBaseSize = 5f;
    private static final float MIN_2D_SCALE = 0.001f;
    private static final float DEFAULT_CAMERA_Z = -5f;
    private static final float ORTHO_NEAR = -50f;
    private static final float ORTHO_FAR = 50f;
    private static final float PERSPECTIVE_NEAR = 0.1f;
    private static final float PERSPECTIVE_FAR = 100f;
    private static final float PERSPECTIVE_FOV = 45f;
    private int surfaceWidth;
    private int surfaceHeight;
    boolean is2D, is3D;
    public static float scale = 1f;
    public static float angleX = -60f;
    public static float angleY, angleY_extra;
    public static float panX;
    public static float panY = -0.3f;
    public static float scale_2d = 1f;
    public static float panX_2d;
    public static float panY_2d = -1f;
    private boolean isXML, isXMLPoint;
    public static float[] coloreEsterno = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreInterno = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreAttacco = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreAttaccoScuro = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreBoom = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static float[] coloreBoomScuro = new float[]{0.4f, 0.4f, 0.4f, 1f};
    public static FontAtlas atlas;
    public static FontAtlas atlasPNEZD;
    public static float charSpacingFactor = 0.5f;
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
        } catch (Exception ignored) {
        }

        try {
            isXML = DataSaved.progettoSelected.substring(DataSaved.progettoSelected.lastIndexOf(".") + 1).equalsIgnoreCase("xml");
        } catch (Exception e) {
            isXML = false;
        }

        try {
            isXMLPoint = DataSaved.progettoSelected_POINT.substring(DataSaved.progettoSelected_POINT.lastIndexOf(".") + 1).equalsIgnoreCase("xml");
        } catch (Exception e) {
            isXMLPoint = false;
        }

        configureCommonGlState();
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

        } catch (Exception ignored) {
        }
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
        if (!My3DActivity.glFilter) {
            if (My3DActivity.glFace || My3DActivity.glFill) {
                GLDrawer.drawFaces(gl11, DataSaved.dxfFaces, 0.8f, scale, isXML,My3DActivity.glFill,My3DActivity.glFace);
            }
            if (My3DActivity.glGradient) {
                GLDrawer.drawFacesGradientPRO(gl11, DataSaved.dxfFaces, scale, TriangleService.minZ, TriangleService.maxZ);
            }

            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

            if (My3DActivity.glPoly) {
                GLDrawer.drawPolylines(gl11, DataSaved.polylines, 3f, scale);
            }
            if (My3DActivity.glPoint) {
                GLDrawer.drawPoints(gl11, DataSaved.points, 10f, scale, isXMLPoint);
            }
            if (My3DActivity.glText) {
                GLDrawer.drawTextsBilBoard(gl11, DataSaved.dxfTexts, DataSaved.glL_AnchorView, charSpacingFactor, scale, atlas);
            }
            if (PNEZD_FUNCTION || glPoint) {
                GLDrawer.drawPNEZD(gl11, DataSaved.pnezdPoints, 15f, scale);
                GLDrawer.drawTextsBilBoardPNEZD(gl11, DataSaved.pnezdPoints, DataSaved.glL_AnchorView, charSpacingFactor, scale, atlasPNEZD);
            }

            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        } else {
            if (My3DActivity.glFace || My3DActivity.glFill) {
                GLDrawer.drawFaces(gl11, DataSaved.filteredFaces, 0.8f, scale, isXML,My3DActivity.glFill,My3DActivity.glFace);
            }
            if (My3DActivity.glGradient) {
                GLDrawer.drawFacesGradientPRO(gl11, DataSaved.filteredFaces, scale, TriangleService.minZ, TriangleService.maxZ);
            }

            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

            if (My3DActivity.glPoly) {
                GLDrawer.drawPolylines(gl11, DataSaved.polylines, 3f, scale);
            }
            if (My3DActivity.glPoint) {
                GLDrawer.drawPoints(gl11, DataSaved.filteredPoints, 10f, scale, isXMLPoint);
            }
            if (My3DActivity.glText) {
                GLDrawer.drawTextsBilBoard(gl11, DataSaved.filteredDxfTexts, DataSaved.glL_AnchorView, charSpacingFactor, scale, atlas);
            }
            if (PNEZD_FUNCTION || glPoint) {
                GLDrawer.drawPNEZD(gl11, DataSaved.pnezdPoints, 15f, scale);
                GLDrawer.drawTextsBilBoardPNEZD(gl11, DataSaved.pnezdPoints, DataSaved.glL_AnchorView, charSpacingFactor, scale, atlasPNEZD);
            }

            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
    }

    private void drawTerrain2D() {
        if (!My3DActivity.glFilter) {
            if (My3DActivity.glFace || My3DActivity.glFill) {
                GLDrawer.drawFaces2D(gl11, DataSaved.dxfFacesGL_2D, 0.8f, 1f, isXML,My3DActivity.glFill,My3DActivity.glFace);
            }
            if (My3DActivity.glGradient) {
                GLDrawer.drawFacesGradient2D(gl11, DataSaved.dxfFaces, 1f, TriangleService.minZ, TriangleService.maxZ);
            }

            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

            if (My3DActivity.glPoly) {
                // GLDrawer.drawPolylines(gl11, DataSaved.polylinesGL_2D, 3f, 1f);
                GLDrawer.drawPolylines(gl11, DataSaved.polylines, 3f, 1f);
                GLDrawer.drawLines2D(gl11, DataSaved.lines_2D, 3f, 1f);
                GLDrawer.drawArcs2D(gl11, DataSaved.arcs, 2f, 1f);
                GLDrawer.drawPolylines2D(gl11, DataSaved.polylines_2D, 3f, 1f);
                GLDrawer.drawCircles2D(gl11, DataSaved.circles, 2f, 1f);
            }

            if (My3DActivity.glPoint) {
                GLDrawer.drawPoints(gl11, DataSaved.points, 10f, 1f, isXMLPoint);
            }
            if (My3DActivity.glText) {
                GLDrawer.drawTextsBilBoard(gl11, DataSaved.dxfTexts, DataSaved.glL_AnchorView, charSpacingFactor, 1f, atlas);
            }
            if (PNEZD_FUNCTION || glPoint) {
                GLDrawer.drawPNEZD(gl11, DataSaved.pnezdPoints, 15f, 1f);
                GLDrawer.drawTextsBilBoardPNEZD(gl11, DataSaved.pnezdPoints, DataSaved.glL_AnchorView, charSpacingFactor, 1f, atlasPNEZD);
            }

            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        } else {
            if (My3DActivity.glFace || My3DActivity.glFill) {
                GLDrawer.drawFaces2D(gl11, DataSaved.filteredFacesGL_2D, 0.8f, 1f, isXML,My3DActivity.glFill,My3DActivity.glFace);
            }
            if (My3DActivity.glGradient) {
                GLDrawer.drawFacesGradient2D(gl11, DataSaved.filteredFaces, 1f, TriangleService.minZ, TriangleService.maxZ);
            }

            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

            if (My3DActivity.glPoly) {
                // GLDrawer.drawPolylines(gl11, DataSaved.polylinesGL_2D, 3f, 1f);
                GLDrawer.drawPolylines(gl11, DataSaved.polylines, 3f, 1f);
                GLDrawer.drawLines2D(gl11, DataSaved.lines_2D, 3f, 1f);
                GLDrawer.drawArcs2D(gl11, DataSaved.arcs, 2f, 1f);
                GLDrawer.drawPolylines2D(gl11, DataSaved.polylines_2D, 3f, 1f);
                GLDrawer.drawCircles2D(gl11, DataSaved.circles, 2f, 1f);
            }

            if (glPoint) {
                GLDrawer.drawPoints(gl11, DataSaved.filteredPoints, 10f, 1f, isXMLPoint);
            }
            if (My3DActivity.glText) {
                GLDrawer.drawTextsBilBoard(gl11, DataSaved.filteredDxfTexts, DataSaved.glL_AnchorView, charSpacingFactor, 1f, atlas);
            }
            if (PNEZD_FUNCTION || glPoint) {
                GLDrawer.drawPNEZD(gl11, DataSaved.pnezdPoints, 15f, 1f);
                GLDrawer.drawTextsBilBoardPNEZD(gl11, DataSaved.pnezdPoints, DataSaved.glL_AnchorView, charSpacingFactor, 1f, atlasPNEZD);
            }

            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
    }

    public static float currentRenderScale() {
        return 1f;
    }
}







