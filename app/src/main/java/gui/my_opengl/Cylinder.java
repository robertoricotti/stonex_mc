package gui.my_opengl;

import android.opengl.GLES11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class Cylinder{
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

    public Cylinder(float[] baseCenter, float[] topCenter, float radius,float topRadius, float[] color, int numSides,boolean border) {
        generateSide(baseCenter, topCenter, radius, topRadius, color, numSides);
        this.border=border;
        this.col=color;
        float[] dir = new float[]{
                topCenter[0] - baseCenter[0],
                topCenter[1] - baseCenter[1],
                topCenter[2] - baseCenter[2]
        };

        generateCap(baseCenter, dir, radius, color, numSides, true);
        generateCap(topCenter, dir, topRadius, color, numSides, false);

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

            // Offset per base e top con raggi diversi
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

            // Bottom vertex
            vertices[i * 6 + 0] = base[0] + baseOffset[0];
            vertices[i * 6 + 1] = base[1] + baseOffset[1];
            vertices[i * 6 + 2] = base[2] + baseOffset[2];

            // Top vertex
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

        //sideLineBuffer = null; // Disabilitato per ora, può essere adattato se vuoi anche perici verticali con raggi differenti
        float[] lineVertices = new float[numSides * 2 * 3]; // 2 vertici per linea, 3 coordinate

        for (int i = 0; i < numSides; i++) {
            // Base vertex
            lineVertices[i * 6 + 0] = vertices[i * 6 + 0];
            lineVertices[i * 6 + 1] = vertices[i * 6 + 1];
            lineVertices[i * 6 + 2] = vertices[i * 6 + 2];

            // Top vertex
            lineVertices[i * 6 + 3] = vertices[i * 6 + 3];
            lineVertices[i * 6 + 4] = vertices[i * 6 + 4];
            lineVertices[i * 6 + 5] = vertices[i * 6 + 5];
        }

        sideLineBuffer = createFloatBuffer(lineVertices);
        sideLineCount = numSides * 2;

    }



    private void generateCap(float[] center, float[] direction, float radius, float[] color, int numSides, boolean isBottom) {
        float[] up = normalize(direction);

        // Trova un vettore non parallelo a "up"
        float[] arbitrary = (Math.abs(up[1]) < 0.99f) ? new float[]{0f, 1f, 0f} : new float[]{1f, 0f, 0f};

        // Costruisci vettori ortogonali al piano del disco
        float[] right = normalize(cross(up, arbitrary));
        float[] forward = normalize(cross(right, up));

        float[] vertices = new float[(numSides + 2) * 3];
        float[] colors = new float[(numSides + 2) * 4];
        float[] edgeVertices = new float[numSides * 3]; // Solo per il contorno

        // Centro del disco
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

            vertices[(i + 1) * 3 + 0] = vx;
            vertices[(i + 1) * 3 + 1] = vy;
            vertices[(i + 1) * 3 + 2] = vz;

            System.arraycopy(color, 0, colors, (i + 1) * 4, 4);

            // Aggiungi ai vertici del bordo (saltando il punto centrale)
            if (i < numSides) {
                edgeVertices[i * 3 + 0] = vx;
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
        // Abilita gli array per vertici e colori
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        // Disegna i lati del cilindro
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sideBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, sideColorBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, sideVertexCount);

        // Disegna la base inferiore
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bottomBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, bottomColorBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, bottomVertexCount);

        // Disegna la base superiore
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, topBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, topColorBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, topVertexCount);

        // Disattiva il color array per usare un colore fisso
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        if(border) {
            // Imposta colore nero per i contorni
            gl.glColor4f(0f, 0f, 0f, 1f);
            gl.glLineWidth(1f);
            // Disegna contorno inferiore
            if (bottomEdgeBuffer != null) {
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bottomEdgeBuffer);
                gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, bottomVertexCount - 2);
            }

            // Disegna contorno superiore
            if (topEdgeBuffer != null) {
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, topEdgeBuffer);
                gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, topVertexCount - 2);
            }

        }



        // Disattiva tutto
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    public  void drawL(GL11 gl,boolean lines){
        // Abilita gli array per vertici e colori
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        // Disegna i lati del cilindro
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sideBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, sideColorBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, sideVertexCount);

        // Disegna la base inferiore
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bottomBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, bottomColorBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, bottomVertexCount);

        // Disegna la base superiore
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, topBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, topColorBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, topVertexCount);

        // Disattiva il color array per usare un colore fisso
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        if(lines) {
            // Imposta colore nero per i contorni
            float[]c=GL_Methods.darkenColor(col,0.5f,0.9f);
            gl.glColor4f(c[0],c[1],c[2],c[3]);
            gl.glLineWidth(0.8f);
            // Disegna contorno inferiore
            if (bottomEdgeBuffer != null) {
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, bottomEdgeBuffer);
                gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, bottomVertexCount - 2);
            }

            // Disegna contorno superiore
            if (topEdgeBuffer != null) {
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, topEdgeBuffer);
                gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, topVertexCount - 2);
            }
            // Linee verticali laterali
            if (sideLineBuffer != null) {
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sideLineBuffer);
                gl.glDrawArrays(GL10.GL_LINES, 0, sideLineCount);
            }
        }



        // Disattiva tutto
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
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

}
