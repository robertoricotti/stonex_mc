package gui.my_opengl;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.microedition.khronos.opengles.GL10;


public class SimpleBucket6Points {
    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private float[] vertices;
    private short[] indices;
    private float[] normalizedVertices;
    private List<short[]> mouthEdges;
    private short[] innerFacesIndices;
    private short[] outerFacesIndices;

    public SimpleBucket6Points(Context context, int resourceId,
                               float[] left, float[] right,
                               float[] topLeft, float[] topRight,
                               float[] center, float[] pivot,
                               float[] centerBack) {
        try {
            String json = loadJSONFromResource(context, resourceId);
            JSONObject obj = new JSONObject(json);

            JSONArray verts = obj.getJSONArray("meshes").getJSONObject(0).getJSONArray("vertices");
            JSONArray faces = obj.getJSONArray("meshes").getJSONObject(0).getJSONArray("faces");

            vertices = new float[verts.length()];
            for (int i = 0; i < verts.length(); i++) {
                vertices[i] = (float) verts.getDouble(i);
            }

            indices = new short[faces.length() * 3];
            for (int i = 0; i < faces.length(); i++) {
                JSONArray face = faces.getJSONArray(i);
                indices[i * 3] = (short) face.getInt(0);
                indices[i * 3 + 1] = (short) face.getInt(1);
                indices[i * 3 + 2] = (short) face.getInt(2);
            }

            normalizeVertices(left, right, topLeft, topRight, center, pivot, centerBack);
            setupBuffers();
            mouthEdges = computeBoundaryEdges();

            // Esempio: prime 10 facce come "interne"
            Set<Integer> internal = new HashSet<>();
            for (int i = 0; i < 122; i++) internal.add(i);
            splitFaceIndices(internal);

        } catch (Exception e) {
        }
    }

    private String loadJSONFromResource(Context context, int resourceId) {
        try {
            InputStream is = context.getResources().openRawResource(resourceId);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    private void setupBuffers() {
        ByteBuffer vb = ByteBuffer.allocateDirect(normalizedVertices.length * 4);
        vb.order(ByteOrder.nativeOrder());
        vertexBuffer = vb.asFloatBuffer();
        vertexBuffer.put(normalizedVertices);
        vertexBuffer.position(0);
    }

    private void normalizeVertices(float[] left, float[] right,
                                   float[] topLeft, float[] topRight,
                                   float[] center, float[] pivot,
                                   float[] centerBack) {

        Point3DF pLeft = new Point3DF(left);
        Point3DF pRight = new Point3DF(right);
        Point3DF pTopLeft = new Point3DF(topLeft);
        Point3DF pTopRight = new Point3DF(topRight);
        Point3DF pCenter = new Point3DF(center);
        Point3DF pPivot = new Point3DF(pivot);
        Point3DF pCenterBack = new Point3DF(centerBack);

        Point3DF xDir = pRight.subtract(pLeft).normalize();
        Point3DF zDir = pCenterBack.subtract(pCenter).normalize();
        Point3DF yDir = zDir.cross(xDir).normalize();

        float xLength = pRight.subtract(pLeft).length();
        float zLength = pCenterBack.subtract(pCenter).length();
        float yLength = pTopLeft.subtract(pLeft).dot(yDir);

        normalizedVertices = new float[vertices.length];
        for (int i = 0; i < vertices.length; i += 3) {
            float x = vertices[i];
            float y = vertices[i + 1];
            float z = vertices[i + 2];

            Point3DF original = new Point3DF(x, y, z);
            float nx = original.dot(xDir) / xLength;
            float ny = original.dot(yDir) / yLength;
            float nz = original.dot(zDir) / zLength;

            normalizedVertices[i] = nx;
            normalizedVertices[i + 1] = ny;
            normalizedVertices[i + 2] = nz;
        }
    }

    private List<short[]> computeBoundaryEdges() {
        Map<String, Integer> edgeCount = new HashMap<>();

        for (int i = 0; i < indices.length; i += 3) {
            int[][] edges = {
                    {indices[i], indices[i+1]},
                    {indices[i+1], indices[i+2]},
                    {indices[i+2], indices[i]}
            };

            for (int[] edge : edges) {
                int min = Math.min(edge[0], edge[1]);
                int max = Math.max(edge[0], edge[1]);
                String key = min + "-" + max;
                edgeCount.put(key, edgeCount.getOrDefault(key, 0) + 1);
            }
        }

        List<short[]> boundary = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : edgeCount.entrySet()) {
            if (entry.getValue() == 1) {
                String[] parts = entry.getKey().split("-");
                boundary.add(new short[]{
                        (short) Integer.parseInt(parts[0]),
                        (short) Integer.parseInt(parts[1])
                });
            }
        }

        return boundary;
    }

    private void splitFaceIndices(Set<Integer> innerFaceIndicesSet) {
        List<Short> inner = new ArrayList<>();
        List<Short> outer = new ArrayList<>();

        for (int i = 0; i < indices.length; i += 3) {
            if (innerFaceIndicesSet.contains(i / 3)) {
                inner.add(indices[i]);
                inner.add(indices[i + 1]);
                inner.add(indices[i + 2]);
            } else {
                outer.add(indices[i]);
                outer.add(indices[i + 1]);
                outer.add(indices[i + 2]);
            }
        }

        innerFacesIndices = toShortArray(inner);
        outerFacesIndices = toShortArray(outer);
    }

    private short[] toShortArray(List<Short> list) {
        short[] array = new short[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public void draw(GL10 gl, float[] transformOrigin, float[] left, float[] right, float[] topLeft, float[] topRight,
                     float[] center, float[] pivot, float[] centerBack,
                     float[] innerColor, float[] outerColor) {

        Point3DF pLeft = new Point3DF(left);
        Point3DF pRight = new Point3DF(right);
        Point3DF pTopLeft = new Point3DF(topLeft);
        Point3DF pCenter = new Point3DF(center);
        Point3DF pCenterBack = new Point3DF(centerBack);

        Point3DF xVec = pRight.subtract(pLeft);
        Point3DF zVec = pCenterBack.subtract(pCenter);
        Point3DF yVec = zVec.cross(xVec);

        float xLen = xVec.length();
        float zLen = zVec.length();
        float yLen = pTopLeft.subtract(pLeft).dot(yVec.normalize());

        Point3DF xDir = xVec.normalize();
        Point3DF yDir = yVec.normalize();
        Point3DF zDir = zVec.normalize();

        float[] transformedVertices = new float[normalizedVertices.length];
        for (int i = 0; i < normalizedVertices.length / 3; i++) {
            float x = normalizedVertices[i * 3];
            float y = normalizedVertices[i * 3 + 1];
            float z = normalizedVertices[i * 3 + 2];

            Point3DF local = xDir.scale(x * xLen)
                    .add(yDir.scale(y * yLen))
                    .add(zDir.scale(z * zLen));

            Point3DF world = new Point3DF(transformOrigin).add(local);
            transformedVertices[i * 3] = world.x;
            transformedVertices[i * 3 + 1] = world.y;
            transformedVertices[i * 3 + 2] = world.z;
        }

        vertexBuffer.clear();
        vertexBuffer.put(transformedVertices);
        vertexBuffer.position(0);

        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

        gl.glColor4f(innerColor[0], innerColor[1], innerColor[2], 1f);
        gl.glDrawElements(GL10.GL_TRIANGLES, innerFacesIndices.length, GL10.GL_UNSIGNED_SHORT,
                ShortBuffer.wrap(innerFacesIndices));

        gl.glColor4f(outerColor[0], outerColor[1], outerColor[2], 1f);
        gl.glDrawElements(GL10.GL_TRIANGLES, outerFacesIndices.length, GL10.GL_UNSIGNED_SHORT,
                ShortBuffer.wrap(outerFacesIndices));

        // Bordi visibili
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_LINE_SMOOTH);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        float[] c = GL_Methods.parseColorToGL(Color.BLACK);
        gl.glLineWidth(1.5f);
        gl.glColor4f(c[0], c[1], c[2], 0.5f);
        for (short[] edge : mouthEdges) {
            short i1 = edge[0];
            short i2 = edge[1];
            float[] lineVertices = {
                    transformedVertices[i1 * 3], transformedVertices[i1 * 3 + 1], transformedVertices[i1 * 3 + 2],
                    transformedVertices[i2 * 3], transformedVertices[i2 * 3 + 1], transformedVertices[i2 * 3 + 2]
            };
            FloatBuffer lineBuffer = ByteBuffer.allocateDirect(6 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            lineBuffer.put(lineVertices).position(0);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);
            gl.glDrawArrays(GL10.GL_LINES, 0, 2);
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
