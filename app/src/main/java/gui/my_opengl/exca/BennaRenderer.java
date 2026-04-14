package gui.my_opengl.exca;

import static gui.my_opengl.exca.My_Benna.bwF;
import static gui.my_opengl.exca.My_Benna.fwF;
import static gui.my_opengl.exca.My_Benna.ltF;
import static gui.my_opengl.exca.My_Benna.rtF;
import static gui.my_opengl.exca.My_Benna.start;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import gui.my_opengl.GL_Methods;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import packexcalib.exca.DataSaved;

public class BennaRenderer {

    private static BucketProgram program;

    private FloatBuffer edgeBuffer;
    private int edgeCount;
    private final float[] innerColor;
    private final float[] outerColor;
    private final float thickness;

    private FloatBuffer vertexBuffer;
    private ShortBuffer outerIndexBuffer;
    private ShortBuffer innerIndexBuffer;
    private final List<LineSegment> edgeLines = new ArrayList<>();

    private int outerIndexCount;
    private int innerIndexCount;

    private final float[] identityMatrix = new float[16];
    private final float[] vpMatrix = new float[16];

    public BennaRenderer(Point3DF[] inputPoints,
                         short[] fiancateLeft,
                         short[] fiancateRight,
                         short[] culla,
                         float[] innerColor,
                         float[] outerColor,
                         float thickness) {
        this.innerColor = innerColor;
        this.outerColor = outerColor;
        this.thickness = thickness;

        Matrix.setIdentityM(identityMatrix, 0);

        List<Short> allIndices = new ArrayList<>();
        for (short i : fiancateLeft) allIndices.add(i);
        for (short i : fiancateRight) allIndices.add(i);
        for (short i : culla) allIndices.add(i);

        short[] combinedIndices = new short[allIndices.size()];
        for (int i = 0; i < combinedIndices.length; i++) {
            combinedIndices[i] = allIndices.get(i);
        }

        buildExtrudedMesh(inputPoints, combinedIndices);
        addFiancataContour(0, 10, inputPoints);
        addFiancataContour(11, 21, inputPoints);
        addEdgeLine(inputPoints[3], inputPoints[14]);
        addEdgeLine(inputPoints[0], inputPoints[11]);
        addEdgeLine(inputPoints[4], inputPoints[15]);

        rebuildEdgeBuffer();
        ensureProgram();
    }

    private void buildExtrudedMesh(Point3DF[] vertices, short[] indices) {
        List<Float> finalVertices = new ArrayList<>();
        List<Short> outerIndices = new ArrayList<>();
        List<Short> innerIndices = new ArrayList<>();
        short currentIndex = 0;

        for (int i = 0; i < indices.length; i += 3) {
            Point3DF a = vertices[indices[i]];
            Point3DF b = vertices[indices[i + 1]];
            Point3DF c = vertices[indices[i + 2]];

            Point3DF normal = b.subtract(a).cross(c.subtract(a)).normalize();

            Point3DF a2 = a.add(normal.scale(thickness));
            Point3DF b2 = b.add(normal.scale(thickness));
            Point3DF c2 = c.add(normal.scale(thickness));

            short ia = currentIndex++;
            short ib = currentIndex++;
            short ic = currentIndex++;
            addVertex(finalVertices, a);
            addVertex(finalVertices, b);
            addVertex(finalVertices, c);
            outerIndices.add(ia);
            outerIndices.add(ib);
            outerIndices.add(ic);

            short ia2 = currentIndex++;
            short ib2 = currentIndex++;
            short ic2 = currentIndex++;
            addVertex(finalVertices, a2);
            addVertex(finalVertices, b2);
            addVertex(finalVertices, c2);
            innerIndices.add(ic2);
            innerIndices.add(ib2);
            innerIndices.add(ia2);

            currentIndex = addSide(finalVertices, outerIndices, innerIndices, a, b, a2, b2, currentIndex);
            currentIndex = addSide(finalVertices, outerIndices, innerIndices, b, c, b2, c2, currentIndex);
            currentIndex = addSide(finalVertices, outerIndices, innerIndices, c, a, c2, a2, currentIndex);
        }

        float[] vertexArray = new float[finalVertices.size()];
        for (int i = 0; i < vertexArray.length; i++) {
            vertexArray[i] = finalVertices.get(i);
        }

        ByteBuffer vb = ByteBuffer.allocateDirect(vertexArray.length * 4).order(ByteOrder.nativeOrder());
        vertexBuffer = vb.asFloatBuffer();
        vertexBuffer.put(vertexArray).position(0);

        short[] outerArray = toShortArray(outerIndices);
        short[] innerArray = toShortArray(innerIndices);

        ByteBuffer ob = ByteBuffer.allocateDirect(outerArray.length * 2).order(ByteOrder.nativeOrder());
        outerIndexBuffer = ob.asShortBuffer();
        outerIndexBuffer.put(outerArray).position(0);
        outerIndexCount = outerArray.length;

        ByteBuffer ib = ByteBuffer.allocateDirect(innerArray.length * 2).order(ByteOrder.nativeOrder());
        innerIndexBuffer = ib.asShortBuffer();
        innerIndexBuffer.put(innerArray).position(0);
        innerIndexCount = innerArray.length;

        rebuildEdgeBuffer();
    }

    private void rebuildEdgeBuffer() {
        float[] edgeVertices = new float[edgeLines.size() * 6];
        for (int i = 0; i < edgeLines.size(); i++) {
            LineSegment line = edgeLines.get(i);
            edgeVertices[i * 6] = line.a.getX();
            edgeVertices[i * 6 + 1] = line.a.getY();
            edgeVertices[i * 6 + 2] = line.a.getZ();
            edgeVertices[i * 6 + 3] = line.b.getX();
            edgeVertices[i * 6 + 4] = line.b.getY();
            edgeVertices[i * 6 + 5] = line.b.getZ();
        }

        ByteBuffer eb = ByteBuffer.allocateDirect(edgeVertices.length * 4).order(ByteOrder.nativeOrder());
        edgeBuffer = eb.asFloatBuffer();
        edgeBuffer.put(edgeVertices).position(0);
        edgeCount = edgeVertices.length / 3;
    }

    private void addVertex(List<Float> list, Point3DF p) {
        list.add(p.getX());
        list.add(p.getY());
        list.add(p.getZ());
    }

    private void addEdgeLine(Point3DF p1, Point3DF p2) {
        edgeLines.add(new LineSegment(p1, p2));
    }

    private short[] toShortArray(List<Short> list) {
        short[] arr = new short[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    private short addSide(List<Float> vList,
                          List<Short> outer,
                          List<Short> inner,
                          Point3DF p1,
                          Point3DF p2,
                          Point3DF p1e,
                          Point3DF p2e,
                          short startIndex) {
        short i0 = startIndex;
        short i1 = (short) (startIndex + 1);
        short i2 = (short) (startIndex + 2);
        short i3 = (short) (startIndex + 3);

        addVertex(vList, p1);
        addVertex(vList, p2);
        addVertex(vList, p2e);
        addVertex(vList, p1e);

        outer.add(i0);
        outer.add(i1);
        outer.add(i2);
        outer.add(i0);
        outer.add(i2);
        outer.add(i3);

        return (short) (startIndex + 4);
    }

    public void draw(GL11 gl) {
        ensureProgram();

        float[] currentVp = GL11.getCurrentViewProjectionMatrix();
        if (currentVp == null) {
            return;
        }
        System.arraycopy(currentVp, 0, vpMatrix, 0, 16);

        if (vertexBuffer == null) return;

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        drawIndexed(vertexBuffer, outerIndexBuffer, outerIndexCount, outerColor);
        drawIndexed(vertexBuffer, innerIndexBuffer, innerIndexCount, innerColor);

        if (edgeBuffer != null && edgeCount > 0) {
            GLES20.glLineWidth(1.0f);
            drawArrays(edgeBuffer, edgeCount, GLES20.GL_LINES, new float[]{0f, 0f, 0f, 0.9f});
        }

        if (DataSaved.showAlign > 0) {
            GLES20.glLineWidth(3f);
            float[] c = GL_Methods.parseColorToGL(Color.GREEN);

            float[] lineVertices = {
                    start.getX(), start.getY(), start.getZ(), fwF.getX(), fwF.getY(), fwF.getZ(),
                    start.getX(), start.getY(), start.getZ(), bwF.getX(), bwF.getY(), bwF.getZ(),
                    start.getX(), start.getY(), start.getZ(), ltF.getX(), ltF.getY(), ltF.getZ(),
                    start.getX(), start.getY(), start.getZ(), rtF.getX(), rtF.getY(), rtF.getZ(),
            };

            FloatBuffer alignBuffer = ByteBuffer
                    .allocateDirect(lineVertices.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            alignBuffer.put(lineVertices).position(0);

            drawArrays(alignBuffer, 8, GLES20.GL_LINES, new float[]{c[0], c[1], c[2], 1f});
        }
    }

    private void drawIndexed(FloatBuffer vertices, ShortBuffer indices, int indexCount, float[] color) {
        if (vertices == null || indices == null || indexCount <= 0) return;

        program.use();

        vertices.position(0);
        indices.position(0);

        GLES20.glUniformMatrix4fv(program.uMvpMatrix, 1, false, vpMatrix, 0);
        GLES20.glUniform4f(program.uColor, color[0], color[1], color[2], color.length > 3 ? color[3] : 1f);

        GLES20.glEnableVertexAttribArray(program.aPosition);
        GLES20.glVertexAttribPointer(program.aPosition, 3, GLES20.GL_FLOAT, false, 0, vertices);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indices);

        GLES20.glDisableVertexAttribArray(program.aPosition);
    }

    private void drawArrays(FloatBuffer vertices, int vertexCount, int mode, float[] color) {
        if (vertices == null || vertexCount <= 0) return;

        program.use();

        vertices.position(0);

        GLES20.glUniformMatrix4fv(program.uMvpMatrix, 1, false, vpMatrix, 0);
        GLES20.glUniform4f(program.uColor, color[0], color[1], color[2], color.length > 3 ? color[3] : 1f);

        GLES20.glEnableVertexAttribArray(program.aPosition);
        GLES20.glVertexAttribPointer(program.aPosition, 3, GLES20.GL_FLOAT, false, 0, vertices);

        GLES20.glDrawArrays(mode, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(program.aPosition);
    }

    private static void ensureProgram() {
        if (program == null) {
            program = new BucketProgram();
        }
    }

    private static class LineSegment {
        Point3DF a, b;

        LineSegment(Point3DF a, Point3DF b) {
            this.a = a;
            this.b = b;
        }
    }

    private void addFiancataContour(int start, int end, Point3DF[] points) {
        for (int i = start; i < end; i++) {
            addEdgeLine(points[i], points[i + 1]);
        }
    }

    private static class BucketProgram {
        final int programId;
        final int aPosition;
        final int uMvpMatrix;
        final int uColor;

        BucketProgram() {
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
                throw new RuntimeException("BucketProgram link error: " + log);
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
                throw new RuntimeException("Bucket shader compile error: " + log);
            }
            return shader;
        }
    }

    public static void resetGlState() {
        program = null;
    }
}