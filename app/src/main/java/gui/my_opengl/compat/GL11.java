package gui.my_opengl.compat;

import android.opengl.GLES20;

public class GL11 extends GL10 {

    private static final float[] currentViewProjectionMatrix = new float[16];

    public static final int GL_ARRAY_BUFFER = GLES20.GL_ARRAY_BUFFER;
    public static final int GL_STATIC_DRAW = GLES20.GL_STATIC_DRAW;
    public static final int GL_DYNAMIC_DRAW = GLES20.GL_DYNAMIC_DRAW;

    public static final int GL_SMOOTH = 0x1D01;
    public static final int GL_PERSPECTIVE_CORRECTION_HINT = 0x0C50;

    public static void setCurrentViewProjectionMatrix(float[] matrix) {
        if (matrix != null && matrix.length >= 16) {
            System.arraycopy(matrix, 0, currentViewProjectionMatrix, 0, 16);
        }
    }

    public static float[] getCurrentViewProjectionMatrix() {
        float[] copy = new float[16];
        System.arraycopy(currentViewProjectionMatrix, 0, copy, 0, 16);
        return copy;
    }
}