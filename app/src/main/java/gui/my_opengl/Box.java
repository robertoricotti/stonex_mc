package gui.my_opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Box {
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private int vertexCount;

    private FloatBuffer edgeBuffer;
    private int edgeCount;

    public Box(float[] baseCenter, float[] topCenter, float baseWidth, float baseHeight, float[] color) {
        generateBox(baseCenter, topCenter, baseWidth, baseHeight, color);
    }

    private void generateBox(float[] baseCenter, float[] topCenter, float width, float height, float[] color) {
        // Direzione verticale del parallelepipedo
        float[] dir = new float[]{
                topCenter[0] - baseCenter[0],
                topCenter[1] - baseCenter[1],
                topCenter[2] - baseCenter[2]
        };
        dir = normalize(dir);

        // Trova due vettori ortogonali alla direzione
        float[] arbitrary = (Math.abs(dir[1]) < 0.99f) ? new float[]{0f, 1f, 0f} : new float[]{1f, 0f, 0f};
        float[] right = normalize(cross(dir, arbitrary));
        float[] up = normalize(cross(right, dir));

        float hw = width / 2f;
        float hh = height / 2f;

        // Vertici delle basi
        float[][] baseCorners = new float[4][3];
        float[][] topCorners = new float[4][3];

        float[][] offsets = {
                {-hw, -hh}, {hw, -hh}, {hw, hh}, {-hw, hh}
        };

        for (int i = 0; i < 4; i++) {
            float ox = offsets[i][0];
            float oy = offsets[i][1];

            for (int j = 0; j < 3; j++) {
                baseCorners[i][j] = baseCenter[j] + right[j] * ox + up[j] * oy;
                topCorners[i][j] = topCenter[j] + right[j] * ox + up[j] * oy;
            }
        }

        // Triangoli: 6 facce x 2 triangoli x 3 vertici = 36 vertici
        float[] vertices = new float[36 * 3];
        float[] colors = new float[36 * 4];
        int vi = 0;

        int[][] faces = {
                {0, 1, 2, 3}, // base
                {4, 5, 6, 7}, // top
                {0, 1, 5, 4}, // lato1
                {1, 2, 6, 5}, // lato2
                {2, 3, 7, 6}, // lato3
                {3, 0, 4, 7}  // lato4
        };

        float[][] allVerts = new float[8][3];
        for (int i = 0; i < 4; i++) {
            allVerts[i] = baseCorners[i];
            allVerts[i + 4] = topCorners[i];
        }

        for (int[] face : faces) {
            // triangolo 1
            addVertex(vertices, colors, vi++, allVerts[face[0]], color);
            addVertex(vertices, colors, vi++, allVerts[face[1]], color);
            addVertex(vertices, colors, vi++, allVerts[face[2]], color);
            // triangolo 2
            addVertex(vertices, colors, vi++, allVerts[face[0]], color);
            addVertex(vertices, colors, vi++, allVerts[face[2]], color);
            addVertex(vertices, colors, vi++, allVerts[face[3]], color);
        }

        vertexCount = 36;
        vertexBuffer = createFloatBuffer(vertices);
        colorBuffer = createFloatBuffer(colors);

        // Linee (12 spigoli)
        int[] edges = {
                0,1, 1,2, 2,3, 3,0,
                4,5, 5,6, 6,7, 7,4,
                0,4, 1,5, 2,6, 3,7
        };

        float[] edgeVerts = new float[edges.length * 3];
        for (int i = 0; i < edges.length; i++) {
            float[] v = allVerts[edges[i]];
            edgeVerts[i * 3 + 0] = v[0];
            edgeVerts[i * 3 + 1] = v[1];
            edgeVerts[i * 3 + 2] = v[2];
        }

        edgeCount = edges.length;
        edgeBuffer = createFloatBuffer(edgeVerts);
    }

    private void addVertex(float[] vertices, float[] colors, int index, float[] v, float[] c) {
        int vi = index * 3;
        int ci = index * 4;
        vertices[vi] = v[0];
        vertices[vi + 1] = v[1];
        vertices[vi + 2] = v[2];
        System.arraycopy(c, 0, colors, ci, 4);
    }

    private FloatBuffer createFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(data);
        fb.position(0);
        return fb;
    }

    private float[] normalize(float[] v) {
        float len = (float) Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
        return new float[]{v[0]/len, v[1]/len, v[2]/len};
    }

    private float[] cross(float[] a, float[] b) {
        return new float[]{
                a[1]*b[2] - a[2]*b[1],
                a[2]*b[0] - a[0]*b[2],
                a[0]*b[1] - a[1]*b[0]
        };
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        // Disegna facce
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertexCount);

        // Linee nere
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glColor4f(0f, 0f, 0f, 1f);
        gl.glLineWidth(1f);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, edgeBuffer);
        gl.glDrawArrays(GL10.GL_LINES, 0, edgeCount);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
