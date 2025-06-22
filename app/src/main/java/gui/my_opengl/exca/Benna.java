package gui.my_opengl.exca;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;



public class Benna {
    private float[][] vertices;
    private float r;
    private FloatBuffer fiancoSinistroBuffer;
    private FloatBuffer fiancoDestroBuffer;
    private FloatBuffer baseBuffer;
    private float[] colore; // RGB array [r,g,b]

    public Benna(float[][] punti, float raggio, float[] colore) {
        this.vertices = punti;
        this.r = raggio;
        this.colore = colore;
        setupGeometry();
    }

    private void setupGeometry() {
        // Normale fiancata sinistra (esempio: vettore calcolato da 0,1,2)
        float[] normaleSinistra = calcolaNormale(vertices[0], vertices[1], vertices[2]);
        float[][] arcoSinistro = generateArcPlanare(vertices[1], vertices[9], r, 20, normaleSinistra,true);
        List<float[]> sinistra = new ArrayList<>();
        sinistra.add(vertices[0]);
        sinistra.add(vertices[1]);
        for (float[] p : arcoSinistro) sinistra.add(p);
        sinistra.add(vertices[9]);
        sinistra.add(vertices[2]);
        sinistra.add(vertices[0]);
        fiancoSinistroBuffer = listToFloatBuffer(sinistra);

        // Normale fiancata destra
        float[] normaleDestra = calcolaNormale(vertices[6], vertices[7], vertices[8]);
        float[][] arcoDestro = generateArcPlanare(vertices[7], vertices[10], r, 20, normaleDestra,true);
        List<float[]> destra = new ArrayList<>();
        destra.add(vertices[6]);
        destra.add(vertices[7]);
        for (float[] p : arcoDestro) destra.add(p);
        destra.add(vertices[10]);
        destra.add(vertices[8]);
        destra.add(vertices[6]);
        fiancoDestroBuffer = listToFloatBuffer(destra);

        // Base come prima, 4 triangoli
        float[][] base = new float[][]{
                vertices[0], vertices[1], vertices[3],
                vertices[1], vertices[4], vertices[3],
                vertices[3], vertices[4], vertices[7],
                vertices[7], vertices[6], vertices[3]
        };
        baseBuffer = listToFloatBuffer(java.util.Arrays.asList(base));
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        // Imposta colore
        gl.glColor4f(colore[0], colore[1], colore[2], 1.0f);

        // Fianco sinistro
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fiancoSinistroBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, fiancoSinistroBuffer.capacity() / 3);

        // Fianco destro
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fiancoDestroBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, fiancoDestroBuffer.capacity() / 3);

        // Base (colore uguale)
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, baseBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, baseBuffer.capacity() / 3);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    private FloatBuffer listToFloatBuffer(List<float[]> points) {
        float[] array = new float[points.size() * 3];
        int i = 0;
        for (float[] p : points) {
            array[i++] = p[0];
            array[i++] = p[1];
            array[i++] = p[2];
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(array);
        fb.position(0);
        return fb;
    }

    private float[] calcolaNormale(float[] p0, float[] p1, float[] p2) {
        float[] v1 = new float[]{p1[0] - p0[0], p1[1] - p0[1], p1[2] - p0[2]};
        float[] v2 = new float[]{p2[0] - p0[0], p2[1] - p0[1], p2[2] - p0[2]};
        float[] cross = new float[]{
                v1[1] * v2[2] - v1[2] * v2[1],
                v1[2] * v2[0] - v1[0] * v2[2],
                v1[0] * v2[1] - v1[1] * v2[0]};
        float len = (float) Math.sqrt(cross[0] * cross[0] + cross[1] * cross[1] + cross[2] * cross[2]);
        return new float[]{cross[0] / len, cross[1] / len, cross[2] / len};
    }

    // Genera un arco piano fra start e end, nel piano definito dalla normale
    private float[][] generateArcPlanare(float[] start, float[] end, float radius, int segments, float[] normale, boolean invertiCurvatura) {
        float[] dir = new float[]{
                end[0] - start[0],
                end[1] - start[1],
                end[2] - start[2]
        };

        float chordLength = (float) Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1] + dir[2] * dir[2]);
        dir[0] /= chordLength;
        dir[1] /= chordLength;
        dir[2] /= chordLength;

        // Vettore ortogonale al piano arco (normale x direzione)
        float[] ortho = new float[]{
                normale[1] * dir[2] - normale[2] * dir[1],
                normale[2] * dir[0] - normale[0] * dir[2],
                normale[0] * dir[1] - normale[1] * dir[0]
        };
        float orthoLen = (float) Math.sqrt(ortho[0] * ortho[0] + ortho[1] * ortho[1] + ortho[2] * ortho[2]);
        ortho[0] /= orthoLen;
        ortho[1] /= orthoLen;
        ortho[2] /= orthoLen;

        // Altezza (da corda al centro del cerchio)
        float h = (float) Math.sqrt(radius * radius - (chordLength * chordLength) / 4);

        if (invertiCurvatura) {
            ortho[0] *= -1;
            ortho[1] *= -1;
            ortho[2] *= -1;
        }

        // Centro dell’arco
        float[] mid = new float[]{
                (start[0] + end[0]) / 2f,
                (start[1] + end[1]) / 2f,
                (start[2] + end[2]) / 2f
        };
        float[] center = new float[]{
                mid[0] + ortho[0] * h,
                mid[1] + ortho[1] * h,
                mid[2] + ortho[2] * h
        };

        // Vettore da centro a start
        float[] vStart = new float[]{
                start[0] - center[0],
                start[1] - center[1],
                start[2] - center[2]
        };

        // Genera i punti
        float[][] arcPoints = new float[segments + 1][3];
        arcPoints[0] = start.clone();
        arcPoints[segments] = end.clone();
        float angle = 2 * (float) Math.asin(chordLength / (2 * radius));
        for (int i = 1; i < segments; i++) {
            float theta = angle * i / segments;
            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);

            arcPoints[i][0] = center[0] + vStart[0] * cosTheta + ortho[0] * sinTheta * radius;
            arcPoints[i][1] = center[1] + vStart[1] * cosTheta + ortho[1] * sinTheta * radius;
            arcPoints[i][2] = center[2] + vStart[2] * cosTheta + ortho[2] * sinTheta * radius;
        }


        return arcPoints;
    }

}
