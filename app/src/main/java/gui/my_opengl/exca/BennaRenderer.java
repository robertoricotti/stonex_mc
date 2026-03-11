package gui.my_opengl.exca;

import static gui.my_opengl.exca.My_Benna.bwF;
import static gui.my_opengl.exca.My_Benna.fwF;
import static gui.my_opengl.exca.My_Benna.ltF;
import static gui.my_opengl.exca.My_Benna.rtF;
import static gui.my_opengl.exca.My_Benna.start;

import android.graphics.Color;
import android.opengl.GLES11;

import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import gui.my_opengl.GL_Methods;
import gui.my_opengl.Point3DF;
import packexcalib.exca.DataSaved;
// Inserisci prima questa classe nel tuo progetto Android OpenGL ES 1.0

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import static gui.my_opengl.exca.My_Benna.coneDownF;
import static gui.my_opengl.exca.My_Benna.coneUpF;
public class BennaRenderer {
    private FloatBuffer edgeBuffer;
    private int edgeCount;
    private final float[] innerColor;
    private final float[] outerColor;
    private final float thickness;

    private FloatBuffer vertexBuffer;
    private ShortBuffer outerIndexBuffer;
    private ShortBuffer innerIndexBuffer;
    private List<LineSegment> edgeLines = new ArrayList<>();

    private int outerIndexCount;
    private int innerIndexCount;


    public BennaRenderer(Point3DF[] inputPoints, short[] fiancateLeft, short[] fiancateRight, short[] culla, float[] innerColor, float[] outerColor, float thickness) {
        this.innerColor = innerColor;
        this.outerColor = outerColor;
        this.thickness = thickness;

        List<Short> allIndices = new ArrayList<>();
        for (short i : fiancateLeft) allIndices.add(i);
        for (short i : fiancateRight) allIndices.add(i);
        for (short i : culla) allIndices.add(i);

        short[] combinedIndices = new short[allIndices.size()];
        for (int i = 0; i < combinedIndices.length; i++) combinedIndices[i] = allIndices.get(i);

        buildExtrudedMesh(inputPoints, combinedIndices);
        addFiancataContour(0, 10, inputPoints);   // fiancata sinistra
        addFiancataContour(11, 21, inputPoints);  // fiancata destra
        addEdgeLine(inputPoints[3], inputPoints[14]);
        addEdgeLine(inputPoints[0],inputPoints[11]);
        addEdgeLine(inputPoints[4],inputPoints[15]);
        // Ricostruzione edgeBuffer con i nuovi contorni
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

            // Add front face (outer)
            short ia = currentIndex++;
            short ib = currentIndex++;
            short ic = currentIndex++;
            addVertex(finalVertices, a);
            addVertex(finalVertices, b);
            addVertex(finalVertices, c);
            outerIndices.add(ia); outerIndices.add(ib); outerIndices.add(ic);

            // Add back face (inner)
            short ia2 = currentIndex++;
            short ib2 = currentIndex++;
            short ic2 = currentIndex++;
            addVertex(finalVertices, a2);
            addVertex(finalVertices, b2);
            addVertex(finalVertices, c2);
            innerIndices.add(ic2); innerIndices.add(ib2); innerIndices.add(ia2); // reversed winding

            // Add edges
          /*  addEdgeLine(a, b);
            addEdgeLine(b, c);
            addEdgeLine(c, a);*/

            // Side walls (we skip coloring distinction here)
            currentIndex = addSide(finalVertices, outerIndices, innerIndices, a, b, a2, b2, currentIndex);
            currentIndex = addSide(finalVertices, outerIndices, innerIndices, b, c, b2, c2, currentIndex);
            currentIndex = addSide(finalVertices, outerIndices, innerIndices, c, a, c2, a2, currentIndex);
        }

        float[] vertexArray = new float[finalVertices.size()];
        for (int i = 0; i < vertexArray.length; i++) vertexArray[i] = finalVertices.get(i);

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
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private short addSide(List<Float> vList, List<Short> outer, List<Short> inner, Point3DF p1, Point3DF p2, Point3DF p1e, Point3DF p2e, short startIndex) {
        short i0 = startIndex;
        short i1 = (short) (startIndex + 1);
        short i2 = (short) (startIndex + 2);
        short i3 = (short) (startIndex + 3);

        addVertex(vList, p1);
        addVertex(vList, p2);
        addVertex(vList, p2e);
        addVertex(vList, p1e);

        // Decide color by face normal later
        outer.add(i0); outer.add(i1); outer.add(i2);
        outer.add(i0); outer.add(i2); outer.add(i3);

        return (short) (startIndex + 4);
    }

    public void draw(GL11 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

        // Outer color (visible exterior)
        gl.glColor4f(outerColor[0], outerColor[1], outerColor[2], 1);
        gl.glDrawElements(GL10.GL_TRIANGLES, outerIndexCount, GL10.GL_UNSIGNED_SHORT, outerIndexBuffer);

        // Inner color (mouth side)
        gl.glColor4f(innerColor[0], innerColor[1], innerColor[2], innerColor[3]);
        gl.glDrawElements(GL10.GL_TRIANGLES, innerIndexCount, GL10.GL_UNSIGNED_SHORT, innerIndexBuffer);


        // Disegna il contorno delle fiancate
        gl.glLineWidth(1.0f);
        gl.glColor4f(0, 0, 0, 0.9f);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, edgeBuffer);
        gl.glDrawArrays(GL10.GL_LINES, 0, edgeCount);

        if(DataSaved.showAlign>0) {
            gl.glLineWidth(3f);
            float[] c = GL_Methods.parseColorToGL(Color.GREEN);
            gl.glColor4f(c[0], c[1], c[2], 1);

            float[] lineVertices = {
                    start.getX(), start.getY(), start.getZ(), fwF.getX(), fwF.getY(), fwF.getZ(),
                    start.getX(), start.getY(), start.getZ(), bwF.getX(), bwF.getY(), bwF.getZ(),
                    start.getX(), start.getY(), start.getZ(), ltF.getX(), ltF.getY(), ltF.getZ(),
                    start.getX(), start.getY(), start.getZ(), rtF.getX(), rtF.getY(), rtF.getZ(),
            };

            ByteBuffer bb = ByteBuffer.allocateDirect(lineVertices.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(lineVertices);
            vertexBuffer.position(0);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glDrawArrays(GL10.GL_LINES, 0, 8);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

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





}




