package gui.my_opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import gui.my_opengl.compat.GL11;

public class Cylinder {

    private static CylinderProgram program;

    private float[] col;
    private FloatBuffer sideLineBuffer;
    private int sideLineCount;
    private boolean border;

    private FloatBuffer bottomEdgeBuffer;
    private FloatBuffer topEdgeBuffer;

    private FloatBuffer sideBuffer;
    private FloatBuffer sideColorBuffer;
    private int sideVertexCount;

    private FloatBuffer bottomBuffer;
    private FloatBuffer bottomColorBuffer;
    private int bottomVertexCount;

    private FloatBuffer topBuffer;
    private FloatBuffer topColorBuffer;
    private int topVertexCount;

    private final float[] vpMatrix = new float[16];

    public Cylinder(float[] baseCenter, float[] topCenter, float radius, float topRadius, float[] color, int numSides, boolean border) {
        generateSide(baseCenter, topCenter, radius, topRadius, color, numSides);
        this.border = border;
        this.col = color;

        float[] dir = new float[]{
                topCenter[0] - baseCenter[0],
                topCenter[1] - baseCenter[1],
                topCenter[2] - baseCenter[2]
        };

        generateCap(baseCenter, dir, radius, color, numSides, true);
        generateCap(topCenter, dir, topRadius, color, numSides, false);

        ensureProgram();
    }

    private void generateSide(float[] base, float[] top, float baseRadius, float topRadius, float[] color, int numSides) {
        float[] dir = new float[]{
                top[0] - base[0],
                top[1] - base[1],
                top[2] - base[2]
        };
        float[] up = normalize(dir);
        float[] arbitrary = (Math.abs(up[1]) < 0.99f) ? new float[]{0f, 1f, 0f} : new float[]{1f, 0f, 0f};
        float[] right = normalize(cross(up, arbitrary));
        float[] forward = normalize(cross(right, up));

        float[] vertices = new float[(numSides + 1) * 2 * 3];
        float[] colors = new float[(numSides + 1) * 2 * 4];

        double angleStep = 2.0 * Math.PI / numSides;

        for (int i = 0; i <= numSides; i++) {
            double angle = i * angleStep;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float xDir = right[0] * cos + forward[0] * sin;
            float yDir = right[1] * cos + forward[1] * sin;
            float zDir = right[2] * cos + forward[2] * sin;

            float[] baseOffset = new float[]{
                    xDir * baseRadius,
                    yDir * baseRadius,
                    zDir * baseRadius
            };
            float[] topOffset = new float[]{
                    xDir * topRadius,
                    yDir * topRadius,
                    zDir * topRadius
            };

            vertices[i * 6] = base[0] + baseOffset[0];
            vertices[i * 6 + 1] = base[1] + baseOffset[1];
            vertices[i * 6 + 2] = base[2] + baseOffset[2];

            vertices[i * 6 + 3] = top[0] + topOffset[0];
            vertices[i * 6 + 4] = top[1] + topOffset[1];
            vertices[i * 6 + 5] = top[2] + topOffset[2];

            for (int j = 0; j < 4; j++) {
                colors[i * 8 + j] = color[j];
                colors[i * 8 + 4 + j] = color[j];
            }
        }

        sideVertexCount = (numSides + 1) * 2;
        sideBuffer = createFloatBuffer(vertices);
        sideColorBuffer = createFloatBuffer(colors);

        float[] lineVertices = new float[numSides * 2 * 3];
        for (int i = 0; i < numSides; i++) {
            lineVertices[i * 6] = vertices[i * 6];
            lineVertices[i * 6 + 1] = vertices[i * 6 + 1];
            lineVertices[i * 6 + 2] = vertices[i * 6 + 2];

            lineVertices[i * 6 + 3] = vertices[i * 6 + 3];
            lineVertices[i * 6 + 4] = vertices[i * 6 + 4];
            lineVertices[i * 6 + 5] = vertices[i * 6 + 5];
        }

        sideLineBuffer = createFloatBuffer(lineVertices);
        sideLineCount = numSides * 2;
    }

    private void generateCap(float[] center, float[] direction, float radius, float[] color, int numSides, boolean isBottom) {
        float[] up = normalize(direction);
        float[] arbitrary = (Math.abs(up[1]) < 0.99f) ? new float[]{0f, 1f, 0f} : new float[]{1f, 0f, 0f};

        float[] right = normalize(cross(up, arbitrary));
        float[] forward = normalize(cross(right, up));

        float[] vertices = new float[(numSides + 2) * 3];
        float[] colors = new float[(numSides + 2) * 4];
        float[] edgeVertices = new float[numSides * 3];

        vertices[0] = center[0];
        vertices[1] = center[1];
        vertices[2] = center[2];
        System.arraycopy(color, 0, colors, 0, 4);

        double angleStep = 2.0 * Math.PI / numSides;
        for (int i = 0; i <= numSides; i++) {
            double angle = i * angleStep;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float x = right[0] * cos + forward[0] * sin;
            float y = right[1] * cos + forward[1] * sin;
            float z = right[2] * cos + forward[2] * sin;

            float vx = center[0] + x * radius;
            float vy = center[1] + y * radius;
            float vz = center[2] + z * radius;

            vertices[(i + 1) * 3] = vx;
            vertices[(i + 1) * 3 + 1] = vy;
            vertices[(i + 1) * 3 + 2] = vz;

            System.arraycopy(color, 0, colors, (i + 1) * 4, 4);

            if (i < numSides) {
                edgeVertices[i * 3] = vx;
                edgeVertices[i * 3 + 1] = vy;
                edgeVertices[i * 3 + 2] = vz;
            }
        }

        if (isBottom) {
            bottomVertexCount = numSides + 2;
            bottomBuffer = createFloatBuffer(vertices);
            bottomColorBuffer = createFloatBuffer(colors);
            bottomEdgeBuffer = createFloatBuffer(edgeVertices);
        } else {
            topVertexCount = numSides + 2;
            topBuffer = createFloatBuffer(vertices);
            topColorBuffer = createFloatBuffer(colors);
            topEdgeBuffer = createFloatBuffer(edgeVertices);
        }
    }

    private FloatBuffer createFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(data);
        fb.position(0);
        return fb;
    }

    public void draw(GL11 gl) {
        float[] currentVp = GL11.getCurrentViewProjectionMatrix();
        if (currentVp == null || currentVp.length < 16) return;
        System.arraycopy(currentVp, 0, vpMatrix, 0, 16);

        ensureProgram();

        drawColored(sideBuffer, sideColorBuffer, sideVertexCount, GLES20.GL_TRIANGLE_STRIP);
        drawColored(bottomBuffer, bottomColorBuffer, bottomVertexCount, GLES20.GL_TRIANGLE_FAN);
        drawColored(topBuffer, topColorBuffer, topVertexCount, GLES20.GL_TRIANGLE_FAN);

        if (border) {
            drawSolid(bottomEdgeBuffer, bottomVertexCount - 2, GLES20.GL_LINE_LOOP, new float[]{0f, 0f, 0f, 1f}, 1f);
            drawSolid(topEdgeBuffer, topVertexCount - 2, GLES20.GL_LINE_LOOP, new float[]{0f, 0f, 0f, 1f}, 1f);
        }
    }

    public void drawL(GL11 gl, boolean lines) {
        float[] currentVp = GL11.getCurrentViewProjectionMatrix();
        if (currentVp == null || currentVp.length < 16) return;
        System.arraycopy(currentVp, 0, vpMatrix, 0, 16);

        ensureProgram();

        drawColored(sideBuffer, sideColorBuffer, sideVertexCount, GLES20.GL_TRIANGLE_STRIP);
        drawColored(bottomBuffer, bottomColorBuffer, bottomVertexCount, GLES20.GL_TRIANGLE_FAN);
        drawColored(topBuffer, topColorBuffer, topVertexCount, GLES20.GL_TRIANGLE_FAN);

        if (lines) {
            float[] c = GL_Methods.darkenColor(col, 0.5f, 0.9f);

            drawSolid(bottomEdgeBuffer, bottomVertexCount - 2, GLES20.GL_LINE_LOOP, c, 0.8f);
            drawSolid(topEdgeBuffer, topVertexCount - 2, GLES20.GL_LINE_LOOP, c, 0.8f);

            if (sideLineBuffer != null) {
                drawSolid(sideLineBuffer, sideLineCount, GLES20.GL_LINES, c, 0.8f);
            }
        }
    }

    private void drawColored(FloatBuffer vertexBuffer, FloatBuffer colorBuffer, int vertexCount, int mode) {
        if (vertexBuffer == null || colorBuffer == null || vertexCount <= 0) return;

        program.use();

        vertexBuffer.position(0);
        colorBuffer.position(0);

        GLES20.glUniformMatrix4fv(program.uMvpMatrix, 1, false, vpMatrix, 0);
        GLES20.glUniform1i(program.uUseVertexColor, 1);
        GLES20.glUniform4f(program.uColor, 1f, 1f, 1f, 1f);

        GLES20.glEnableVertexAttribArray(program.aPosition);
        GLES20.glVertexAttribPointer(program.aPosition, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(program.aColor);
        GLES20.glVertexAttribPointer(program.aColor, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        GLES20.glDrawArrays(mode, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(program.aPosition);
        GLES20.glDisableVertexAttribArray(program.aColor);
    }

    private void drawSolid(FloatBuffer vertexBuffer, int vertexCount, int mode, float[] color, float lineWidth) {
        if (vertexBuffer == null || vertexCount <= 0) return;

        program.use();

        vertexBuffer.position(0);

        GLES20.glUniformMatrix4fv(program.uMvpMatrix, 1, false, vpMatrix, 0);
        GLES20.glUniform1i(program.uUseVertexColor, 0);
        GLES20.glUniform4f(
                program.uColor,
                color[0],
                color[1],
                color[2],
                color.length > 3 ? color[3] : 1f
        );

        GLES20.glLineWidth(Math.max(1f, lineWidth));

        GLES20.glEnableVertexAttribArray(program.aPosition);
        GLES20.glVertexAttribPointer(program.aPosition, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glDisableVertexAttribArray(program.aColor);
        GLES20.glVertexAttrib4f(program.aColor, 1f, 1f, 1f, 1f);

        GLES20.glDrawArrays(mode, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(program.aPosition);
    }

    private float[] normalize(float[] v) {
        float len = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (len == 0f) return new float[]{0f, 1f, 0f};
        return new float[]{v[0] / len, v[1] / len, v[2] / len};
    }

    private float[] cross(float[] a, float[] b) {
        return new float[]{
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    private static void ensureProgram() {
        if (program == null) {
            program = new CylinderProgram();
        }
    }

    private static class CylinderProgram {
        final int programId;
        final int aPosition;
        final int aColor;
        final int uMvpMatrix;
        final int uColor;
        final int uUseVertexColor;

        CylinderProgram() {
            String vertexShader =
                    "uniform mat4 uMVPMatrix;\n" +
                            "attribute vec4 aPosition;\n" +
                            "attribute vec4 aColor;\n" +
                            "uniform vec4 uColor;\n" +
                            "uniform int uUseVertexColor;\n" +
                            "varying vec4 vColor;\n" +
                            "void main() {\n" +
                            "  gl_Position = uMVPMatrix * aPosition;\n" +
                            "  vColor = (uUseVertexColor == 1) ? aColor : uColor;\n" +
                            "}";

            String fragmentShader =
                    "precision mediump float;\n" +
                            "varying vec4 vColor;\n" +
                            "void main() {\n" +
                            "  gl_FragColor = vColor;\n" +
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
                throw new RuntimeException("CylinderProgram link error: " + log);
            }

            GLES20.glDeleteShader(vs);
            GLES20.glDeleteShader(fs);

            aPosition = GLES20.glGetAttribLocation(programId, "aPosition");
            aColor = GLES20.glGetAttribLocation(programId, "aColor");
            uMvpMatrix = GLES20.glGetUniformLocation(programId, "uMVPMatrix");
            uColor = GLES20.glGetUniformLocation(programId, "uColor");
            uUseVertexColor = GLES20.glGetUniformLocation(programId, "uUseVertexColor");
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
                throw new RuntimeException("Cylinder shader compile error: " + log);
            }
            return shader;
        }
    }

    public static void resetGlState() {
        program = null;
    }
}