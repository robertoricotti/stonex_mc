package gui.my_opengl.exca;

import android.opengl.GLES11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import gui.my_opengl.Point3DF;

public class BoomsDrawer {

    private FloatBuffer vertexBuffer;
    private ShortBuffer triangleBuffer1;
    private ShortBuffer triangleBuffer2;
    private ShortBuffer edgeIndexBuffer;

    private int numIndices1;
    private int numIndices2;
    private int numEdgeIndices;

    private float[] color1;
    private float[] color2;

    public BoomsDrawer(Point3DF[] points, short[] triangles1, float[] color1,
                       short[] triangles2, float[] color2, short[] edges) {

        this.color1 = color1;
        this.color2 = color2;

        // Vertex buffer
        float[] vertices = new float[points.length * 3];
        for (int i = 0; i < points.length; i++) {
            vertices[i * 3] = points[i].getX();
            vertices[i * 3 + 1] = points[i].getY();
            vertices[i * 3 + 2] = points[i].getZ();
        }
        vertexBuffer = createFloatBuffer(vertices);

        // Triangle index buffers
        triangleBuffer1 = createShortBuffer(triangles1);
        numIndices1 = triangles1.length;

        triangleBuffer2 = createShortBuffer(triangles2);
        numIndices2 = triangles2.length;

        // Edge index buffer
        edgeIndexBuffer = createShortBuffer(edges);
        numEdgeIndices = edges.length;
    }

    public void draw(GL11 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glEnable(GL10.GL_DEPTH_TEST);

        // Disegna facce
        gl.glColor4f(color1[0], color1[1], color1[2], color1[3]);
        gl.glDrawElements(GL10.GL_TRIANGLES, numIndices1, GL10.GL_UNSIGNED_SHORT, triangleBuffer1);

        gl.glColor4f(color2[0], color2[1], color2[2], color2[3]);
        gl.glDrawElements(GL10.GL_TRIANGLES, numIndices2, GL10.GL_UNSIGNED_SHORT, triangleBuffer2);

        // Contorni
        gl.glEnable(GL10.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(1.0f, 1.0f); // leggero offset per evitare z-fighting



        gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);

        //gl.glColor4f(0f, 0f, 0f, 0.95f);
        gl.glColor4f(color2[0], color2[1], color2[2], 1f);
        gl.glLineWidth(1f);

        // Mantieni il depth test attivo
        gl.glDrawElements(GL10.GL_LINES, numEdgeIndices, GL10.GL_UNSIGNED_SHORT, edgeIndexBuffer);

        gl.glDisable(GL10.GL_POLYGON_OFFSET_FILL);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
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
}