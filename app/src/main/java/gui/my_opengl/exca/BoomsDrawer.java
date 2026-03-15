package gui.my_opengl.exca;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;

public class BoomsDrawer {

    private static BoomProgram program;

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer triangleBuffer1;
    private final ShortBuffer triangleBuffer2;
    private final ShortBuffer edgeIndexBuffer;

    private final int numIndices1;
    private final int numIndices2;
    private final int numEdgeIndices;

    private final float[] color1;
    private final float[] color2;

    private final float[] vpMatrix = new float[16];

    public BoomsDrawer(Point3DF[] points, short[] triangles1, float[] color1,
                       short[] triangles2, float[] color2, short[] edges) {

        this.color1 = color1;
        this.color2 = color2;

        float[] vertices = new float[points.length * 3];
        for (int i = 0; i < points.length; i++) {
            vertices[i * 3] = points[i].getX();
            vertices[i * 3 + 1] = points[i].getY();
            vertices[i * 3 + 2] = points[i].getZ();
        }
        vertexBuffer = createFloatBuffer(vertices);

        triangleBuffer1 = createShortBuffer(triangles1);
        numIndices1 = triangles1.length;

        triangleBuffer2 = createShortBuffer(triangles2);
        numIndices2 = triangles2.length;

        edgeIndexBuffer = createShortBuffer(edges);
        numEdgeIndices = edges.length;

        ensureProgram();
    }

    public void draw(GL11 gl) {
        ensureProgram();

        float[] currentVp = GL11.getCurrentViewProjectionMatrix();
        if (currentVp == null || currentVp.length < 16) {
            return;
        }
        System.arraycopy(currentVp, 0, vpMatrix, 0, 16);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // facce gruppo 1
        drawElements(vertexBuffer, triangleBuffer1, numIndices1, GLES20.GL_TRIANGLES, color1);

        // facce gruppo 2
        drawElements(vertexBuffer, triangleBuffer2, numIndices2, GLES20.GL_TRIANGLES, color2);

        // contorni
        GLES20.glLineWidth(1f);
        drawElements(vertexBuffer, edgeIndexBuffer, numEdgeIndices, GLES20.GL_LINES,
                new float[]{color2[0], color2[1], color2[2], 1f});
    }

    private void drawElements(FloatBuffer vertices,
                              ShortBuffer indices,
                              int indexCount,
                              int mode,
                              float[] color) {
        if (vertices == null || indices == null || indexCount <= 0) return;

        program.use();

        vertices.position(0);
        indices.position(0);

        GLES20.glUniformMatrix4fv(program.uMvpMatrix, 1, false, vpMatrix, 0);
        GLES20.glUniform4f(
                program.uColor,
                color[0],
                color[1],
                color[2],
                color.length > 3 ? color[3] : 1f
        );

        GLES20.glEnableVertexAttribArray(program.aPosition);
        GLES20.glVertexAttribPointer(program.aPosition, 3, GLES20.GL_FLOAT, false, 0, vertices);

        GLES20.glDrawElements(mode, indexCount, GLES20.GL_UNSIGNED_SHORT, indices);

        GLES20.glDisableVertexAttribArray(program.aPosition);
    }

    private FloatBuffer createFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(data);
        fb.position(0);
        return fb;
    }

    private ShortBuffer createShortBuffer(short[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(data);
        sb.position(0);
        return sb;
    }

    private static void ensureProgram() {
        if (program == null) {
            program = new BoomProgram();
        }
    }

    private static class BoomProgram {
        final int programId;
        final int aPosition;
        final int uMvpMatrix;
        final int uColor;

        BoomProgram() {
            String vertexShader =
                    "uniform mat4 uMVPMatrix;\n" +
                            "attribute vec4 aPosition;\n" +
                            "void main() {\n" +
                            "  gl_Position = uMVPMatrix * aPosition;\n" +
                            "}";

            String fragmentShader =
                    "precision mediump float;\n" +
                            "uniform vec4 uColor;\n" +
                            "void main() {\n" +
                            "  gl_FragColor = uColor;\n" +
                            "}";

            int vs = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
            int fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

            programId = GLES20.glCreateProgram();
            GLES20.glAttachShader(programId, vs);
            GLES20.glAttachShader(programId, fs);
            GLES20.glLinkProgram(programId);

            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                String log = GLES20.glGetProgramInfoLog(programId);
                GLES20.glDeleteProgram(programId);
                throw new RuntimeException("BoomProgram link error: " + log);
            }

            GLES20.glDeleteShader(vs);
            GLES20.glDeleteShader(fs);

            aPosition = GLES20.glGetAttribLocation(programId, "aPosition");
            uMvpMatrix = GLES20.glGetUniformLocation(programId, "uMVPMatrix");
            uColor = GLES20.glGetUniformLocation(programId, "uColor");
        }

        void use() {
            GLES20.glUseProgram(programId);
        }

        private static int compileShader(int type, String source) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);

            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                String log = GLES20.glGetShaderInfoLog(shader);
                GLES20.glDeleteShader(shader);
                throw new RuntimeException("Boom shader compile error: " + log);
            }
            return shader;
        }
    }
    public static void resetGlState() {
        program = null;
    }
}