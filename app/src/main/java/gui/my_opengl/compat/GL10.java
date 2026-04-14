package gui.my_opengl.compat;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.Buffer;
import java.util.ArrayDeque;
import java.util.Deque;

public class GL10 {
    public static final int GL_VERTEX_ARRAY = 0x8074;
    public static final int GL_COLOR_ARRAY = 0x8076;
    public static final int GL_TEXTURE_COORD_ARRAY = 0x8078;
    public static final int GL_TEXTURE_2D = GLES20.GL_TEXTURE_2D;
    public static final int GL_FLOAT = GLES20.GL_FLOAT;
    public static final int GL_UNSIGNED_BYTE = GLES20.GL_UNSIGNED_BYTE;
    public static final int GL_UNSIGNED_SHORT = GLES20.GL_UNSIGNED_SHORT;
    public static final int GL_RGBA = GLES20.GL_RGBA;
    public static final int GL_LINEAR = GLES20.GL_LINEAR;
    public static final int GL_NEAREST = GLES20.GL_NEAREST;
    public static final int GL_CLAMP_TO_EDGE = GLES20.GL_CLAMP_TO_EDGE;
    public static final int GL_TEXTURE_WRAP_S = GLES20.GL_TEXTURE_WRAP_S;
    public static final int GL_TEXTURE_WRAP_T = GLES20.GL_TEXTURE_WRAP_T;
    public static final int GL_TEXTURE_MIN_FILTER = GLES20.GL_TEXTURE_MIN_FILTER;
    public static final int GL_TEXTURE_MAG_FILTER = GLES20.GL_TEXTURE_MAG_FILTER;
    public static final int GL_TRIANGLES = GLES20.GL_TRIANGLES;
    public static final int GL_TRIANGLE_FAN = GLES20.GL_TRIANGLE_FAN;
    public static final int GL_TRIANGLE_STRIP = GLES20.GL_TRIANGLE_STRIP;
    public static final int GL_LINES = GLES20.GL_LINES;
    public static final int GL_LINE_LOOP = GLES20.GL_LINE_LOOP;
    public static final int GL_LINE_STRIP = GLES20.GL_LINE_STRIP;
    public static final int GL_POINTS = GLES20.GL_POINTS;
    public static final int GL_COLOR_BUFFER_BIT = GLES20.GL_COLOR_BUFFER_BIT;
    public static final int GL_DEPTH_BUFFER_BIT = GLES20.GL_DEPTH_BUFFER_BIT;
    public static final int GL_BLEND = GLES20.GL_BLEND;
    public static final int GL_DEPTH_TEST = GLES20.GL_DEPTH_TEST;
    public static final int GL_ONE_MINUS_SRC_ALPHA = GLES20.GL_ONE_MINUS_SRC_ALPHA;
    public static final int GL_SRC_ALPHA = GLES20.GL_SRC_ALPHA;
    public static final int GL_LEQUAL = GLES20.GL_LEQUAL;
    public static final int GL_NICEST = GLES20.GL_NICEST;
    public static final int GL_LIGHTING = 0x0B50;
    public static final int GL_POINT_SMOOTH = 0x0B10;
    public static final int GL_LINE_SMOOTH_HINT = 0x0C52;
    public static final int GL_POLYGON_OFFSET_FILL = GLES20.GL_POLYGON_OFFSET_FILL;
    public static final int GL_MODELVIEW = 0x1700;
    public static final int GL_PROJECTION = 0x1701;
    public static final int GL_MODELVIEW_MATRIX = 0x0BA6;

    protected final float[] modelViewMatrix = new float[16];
    protected final float[] projectionMatrix = new float[16];
    protected int matrixMode = GL_MODELVIEW;
    protected final Deque<float[]> modelViewStack = new ArrayDeque<>();
    protected final Deque<float[]> projectionStack = new ArrayDeque<>();

    protected Buffer vertexPointer;
    protected int vertexSize = 3;
    protected int vertexStride = 0;

    protected Buffer colorPointer;
    protected int colorSize = 4;
    protected int colorStride = 0;

    protected Buffer texCoordPointer;
    protected int texCoordSize = 2;
    protected int texCoordStride = 0;

    protected boolean vertexArrayEnabled = false;
    protected boolean colorArrayEnabled = false;
    protected boolean texCoordArrayEnabled = false;

    protected final float[] currentColor = new float[]{1f, 1f, 1f, 1f};
    protected float pointSize = 1f;
    protected float lineWidth = 1f;

    protected int boundArrayBuffer = 0;

    private final ShaderPrograms programs = new ShaderPrograms();

    public GL10() {
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(projectionMatrix, 0);
    }

    protected float[] currentMatrix() {
        return matrixMode == GL_PROJECTION ? projectionMatrix : modelViewMatrix;
    }

    protected Deque<float[]> currentStack() {
        return matrixMode == GL_PROJECTION ? projectionStack : modelViewStack;
    }

    public void glClearColor(float r, float g, float b, float a) {
        GLES20.glClearColor(r, g, b, a);
    }

    public void glClear(int mask) {
        GLES20.glClear(mask);
    }

    public void glViewport(int x, int y, int width, int height) {
        GLES20.glViewport(x, y, width, height);
    }

    public void glEnable(int cap) {
        if (cap != GL_TEXTURE_2D) GLES20.glEnable(cap);
    }

    public void glDisable(int cap) {
        if (cap != GL_TEXTURE_2D) GLES20.glDisable(cap);
    }

    public void glBlendFunc(int sfactor, int dfactor) {
        GLES20.glBlendFunc(sfactor, dfactor);
    }

    public void glDepthFunc(int func) {
        GLES20.glDepthFunc(func);
    }

    public void glHint(int target, int mode) {
    }

    public void glShadeModel(int mode) {
    }

    public void glPolygonOffset(float factor, float units) {
        GLES20.glPolygonOffset(factor, units);
    }

    public void glLineWidth(float width) {
        lineWidth = width;
        GLES20.glLineWidth(width);
    }

    public void glPointSize(float size) {
        pointSize = size;
    }

    public void glMatrixMode(int mode) {
        matrixMode = mode;
    }

    public void glLoadIdentity() {
        Matrix.setIdentityM(currentMatrix(), 0);
    }

    public void glPushMatrix() {
        currentStack().push(currentMatrix().clone());
    }

    public void glPopMatrix() {
        if (!currentStack().isEmpty())
            System.arraycopy(currentStack().pop(), 0, currentMatrix(), 0, 16);
    }

    public void glTranslatef(float x, float y, float z) {
        Matrix.translateM(currentMatrix(), 0, x, y, z);
    }

    public void glRotatef(float angle, float x, float y, float z) {
        Matrix.rotateM(currentMatrix(), 0, angle, x, y, z);
    }

    public void glScalef(float x, float y, float z) {
        Matrix.scaleM(currentMatrix(), 0, x, y, z);
    }

    public void glOrthof(float left, float right, float bottom, float top, float near, float far) {
        Matrix.orthoM(currentMatrix(), 0, left, right, bottom, top, near, far);
    }

    public void glLoadMatrixf(float[] m, int offset) {
        System.arraycopy(m, offset, currentMatrix(), 0, 16);
    }

    public void glGetFloatv(int pname, float[] params, int offset) {
        if (pname == GL_MODELVIEW_MATRIX) {
            System.arraycopy(modelViewMatrix, 0, params, offset, 16);
        }
    }

    public void setProjectionMatrix(float[] m) {
        System.arraycopy(m, 0, projectionMatrix, 0, 16);
    }

    public void setModelViewMatrix(float[] m) {
        System.arraycopy(m, 0, modelViewMatrix, 0, 16);
    }

    public void glEnableClientState(int array) {
        if (array == GL_VERTEX_ARRAY) vertexArrayEnabled = true;
        else if (array == GL_COLOR_ARRAY) colorArrayEnabled = true;
        else if (array == GL_TEXTURE_COORD_ARRAY) texCoordArrayEnabled = true;
    }

    public void glDisableClientState(int array) {
        if (array == GL_VERTEX_ARRAY) vertexArrayEnabled = false;
        else if (array == GL_COLOR_ARRAY) colorArrayEnabled = false;
        else if (array == GL_TEXTURE_COORD_ARRAY) texCoordArrayEnabled = false;
    }

    public void glVertexPointer(int size, int type, int stride, Buffer pointer) {
        vertexSize = size;
        vertexStride = stride;
        vertexPointer = pointer;
    }

    public void glColorPointer(int size, int type, int stride, Buffer pointer) {
        colorSize = size;
        colorStride = stride;
        colorPointer = pointer;
    }

    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {
        texCoordSize = size;
        texCoordStride = stride;
        texCoordPointer = pointer;
    }

    public void glColor4f(float r, float g, float b, float a) {
        currentColor[0] = r;
        currentColor[1] = g;
        currentColor[2] = b;
        currentColor[3] = a;
    }

    public void glBindTexture(int target, int texture) {
        GLES20.glBindTexture(target, texture);
    }

    public void glGenTextures(int n, int[] textures, int offset) {
        GLES20.glGenTextures(n, textures, offset);
    }

    public void glDeleteTextures(int n, int[] textures, int offset) {
        GLES20.glDeleteTextures(n, textures, offset);
    }

    public void glTexParameterf(int target, int pname, float param) {
        GLES20.glTexParameterf(target, pname, param);
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
        GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public void glGenBuffers(int n, int[] ids, int offset) {
        GLES20.glGenBuffers(n, ids, offset);
    }

    public void glBindBuffer(int target, int buffer) {
        boundArrayBuffer = buffer;
        GLES20.glBindBuffer(target, buffer);
    }

    public void glBufferData(int target, int size, Buffer data, int usage) {
        GLES20.glBufferData(target, size, data, usage);
    }

    public void glDeleteBuffers(int n, int[] ids, int offset) {
        GLES20.glDeleteBuffers(n, ids, offset);
    }

    protected float[] mvp() {
        float[] out = new float[16];
        Matrix.multiplyMM(out, 0, projectionMatrix, 0, modelViewMatrix, 0);
        return out;
    }

    public void glDrawArrays(int mode, int first, int count) {
        if (vertexPointer == null) return;
        float[] mvp = mvp();
        if (texCoordArrayEnabled && texCoordPointer != null) {
            programs.drawTextured(vertexPointer, vertexSize, vertexStride, texCoordPointer, texCoordSize, texCoordStride, mvp, count, mode, currentColor);
        } else if (colorArrayEnabled && colorPointer != null) {
            programs.drawVertexColor(vertexPointer, vertexSize, vertexStride, colorPointer, colorSize, colorStride, mvp, count, mode, pointSize);
        } else {
            programs.drawUniformColor(vertexPointer, vertexSize, vertexStride, mvp, currentColor, count, mode, pointSize);
        }
    }

    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        if (vertexPointer == null) return;
        float[] mvp = mvp();
        if (colorArrayEnabled && colorPointer != null) {
            programs.drawVertexColorIndexed(vertexPointer, vertexSize, vertexStride, colorPointer, colorSize, colorStride, mvp, count, mode, indices, type, pointSize);
        } else {
            programs.drawUniformColorIndexed(vertexPointer, vertexSize, vertexStride, mvp, currentColor, count, mode, indices, type, pointSize);
        }
    }

    private static int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private static int createProgram(String vertex, String fragment) {
        int vs = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
        int fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);
        return program;
    }

    private static final class ShaderPrograms {
        private final int uniformColorProgram;
        private final int vertexColorProgram;
        private final int texturedProgram;

        private ShaderPrograms() {
            uniformColorProgram = createProgram(
                    "uniform mat4 uMVPMatrix; attribute vec4 aPosition; uniform float uPointSize; void main(){ gl_Position = uMVPMatrix * aPosition; gl_PointSize = uPointSize; }",
                    "precision mediump float; uniform vec4 uColor; void main(){ gl_FragColor = uColor; }"
            );
            vertexColorProgram = createProgram(
                    "uniform mat4 uMVPMatrix; attribute vec4 aPosition; attribute vec4 aColor; varying vec4 vColor; uniform float uPointSize; void main(){ gl_Position = uMVPMatrix * aPosition; vColor = aColor; gl_PointSize = uPointSize; }",
                    "precision mediump float; varying vec4 vColor; void main(){ gl_FragColor = vColor; }"
            );
            texturedProgram = createProgram(
                    "uniform mat4 uMVPMatrix; attribute vec4 aPosition; attribute vec2 aTexCoord; varying vec2 vTexCoord; void main(){ gl_Position = uMVPMatrix * aPosition; vTexCoord = aTexCoord; }",
                    "precision mediump float; varying vec2 vTexCoord; uniform sampler2D uTexture; uniform vec4 uColor; void main(){ gl_FragColor = texture2D(uTexture, vTexCoord) * uColor; }"
            );
        }

        private void drawUniformColor(Buffer vertex, int vertexSize, int vertexStride, float[] mvp, float[] color, int count, int mode, float pointSize) {
            GLES20.glUseProgram(uniformColorProgram);
            int aPos = GLES20.glGetAttribLocation(uniformColorProgram, "aPosition");
            int uMvp = GLES20.glGetUniformLocation(uniformColorProgram, "uMVPMatrix");
            int uColor = GLES20.glGetUniformLocation(uniformColorProgram, "uColor");
            int uPointSize = GLES20.glGetUniformLocation(uniformColorProgram, "uPointSize");
            GLES20.glEnableVertexAttribArray(aPos);
            GLES20.glVertexAttribPointer(aPos, vertexSize, GLES20.GL_FLOAT, false, vertexStride, vertex);
            GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
            GLES20.glUniform4fv(uColor, 1, color, 0);
            GLES20.glUniform1f(uPointSize, pointSize);
            GLES20.glDrawArrays(mode, 0, count);
            GLES20.glDisableVertexAttribArray(aPos);
        }

        private void drawUniformColorIndexed(Buffer vertex, int vertexSize, int vertexStride, float[] mvp, float[] color, int count, int mode, Buffer indices, int type, float pointSize) {
            GLES20.glUseProgram(uniformColorProgram);
            int aPos = GLES20.glGetAttribLocation(uniformColorProgram, "aPosition");
            int uMvp = GLES20.glGetUniformLocation(uniformColorProgram, "uMVPMatrix");
            int uColor = GLES20.glGetUniformLocation(uniformColorProgram, "uColor");
            int uPointSize = GLES20.glGetUniformLocation(uniformColorProgram, "uPointSize");
            GLES20.glEnableVertexAttribArray(aPos);
            GLES20.glVertexAttribPointer(aPos, vertexSize, GLES20.GL_FLOAT, false, vertexStride, vertex);
            GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
            GLES20.glUniform4fv(uColor, 1, color, 0);
            GLES20.glUniform1f(uPointSize, pointSize);
            GLES20.glDrawElements(mode, count, type, indices);
            GLES20.glDisableVertexAttribArray(aPos);
        }

        private void drawVertexColor(Buffer vertex, int vertexSize, int vertexStride, Buffer color, int colorSize, int colorStride, float[] mvp, int count, int mode, float pointSize) {
            GLES20.glUseProgram(vertexColorProgram);
            int aPos = GLES20.glGetAttribLocation(vertexColorProgram, "aPosition");
            int aColor = GLES20.glGetAttribLocation(vertexColorProgram, "aColor");
            int uMvp = GLES20.glGetUniformLocation(vertexColorProgram, "uMVPMatrix");
            int uPointSize = GLES20.glGetUniformLocation(vertexColorProgram, "uPointSize");
            GLES20.glEnableVertexAttribArray(aPos);
            GLES20.glEnableVertexAttribArray(aColor);
            GLES20.glVertexAttribPointer(aPos, vertexSize, GLES20.GL_FLOAT, false, vertexStride, vertex);
            GLES20.glVertexAttribPointer(aColor, colorSize, GLES20.GL_FLOAT, false, colorStride, color);
            GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
            GLES20.glUniform1f(uPointSize, pointSize);
            GLES20.glDrawArrays(mode, 0, count);
            GLES20.glDisableVertexAttribArray(aPos);
            GLES20.glDisableVertexAttribArray(aColor);
        }

        private void drawVertexColorIndexed(Buffer vertex, int vertexSize, int vertexStride, Buffer color, int colorSize, int colorStride, float[] mvp, int count, int mode, Buffer indices, int type, float pointSize) {
            GLES20.glUseProgram(vertexColorProgram);
            int aPos = GLES20.glGetAttribLocation(vertexColorProgram, "aPosition");
            int aColor = GLES20.glGetAttribLocation(vertexColorProgram, "aColor");
            int uMvp = GLES20.glGetUniformLocation(vertexColorProgram, "uMVPMatrix");
            int uPointSize = GLES20.glGetUniformLocation(vertexColorProgram, "uPointSize");
            GLES20.glEnableVertexAttribArray(aPos);
            GLES20.glEnableVertexAttribArray(aColor);
            GLES20.glVertexAttribPointer(aPos, vertexSize, GLES20.GL_FLOAT, false, vertexStride, vertex);
            GLES20.glVertexAttribPointer(aColor, colorSize, GLES20.GL_FLOAT, false, colorStride, color);
            GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
            GLES20.glUniform1f(uPointSize, pointSize);
            GLES20.glDrawElements(mode, count, type, indices);
            GLES20.glDisableVertexAttribArray(aPos);
            GLES20.glDisableVertexAttribArray(aColor);
        }

        private void drawTextured(Buffer vertex, int vertexSize, int vertexStride, Buffer tex, int texSize, int texStride, float[] mvp, int count, int mode, float[] color) {
            GLES20.glUseProgram(texturedProgram);
            int aPos = GLES20.glGetAttribLocation(texturedProgram, "aPosition");
            int aTex = GLES20.glGetAttribLocation(texturedProgram, "aTexCoord");
            int uMvp = GLES20.glGetUniformLocation(texturedProgram, "uMVPMatrix");
            int uColor = GLES20.glGetUniformLocation(texturedProgram, "uColor");
            int uTexture = GLES20.glGetUniformLocation(texturedProgram, "uTexture");
            GLES20.glEnableVertexAttribArray(aPos);
            GLES20.glEnableVertexAttribArray(aTex);
            GLES20.glVertexAttribPointer(aPos, vertexSize, GLES20.GL_FLOAT, false, vertexStride, vertex);
            GLES20.glVertexAttribPointer(aTex, texSize, GLES20.GL_FLOAT, false, texStride, tex);
            GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
            GLES20.glUniform4fv(uColor, 1, color, 0);
            GLES20.glUniform1i(uTexture, 0);
            GLES20.glDrawArrays(mode, 0, count);
            GLES20.glDisableVertexAttribArray(aPos);
            GLES20.glDisableVertexAttribArray(aTex);
        }
    }
}
