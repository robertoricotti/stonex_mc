package gui.my_opengl;

import java.nio.FloatBuffer;

import gui.my_opengl.compat.GL11;


public class VBOHelper {

    // Crea un nuovo VBO e carica i dati
    public static int createVbo(GL11 gl, FloatBuffer buffer) {
        int[] vboIds = new int[1];
        gl.glGenBuffers(1, vboIds, 0);
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vboIds[0]);
        gl.glBufferData(GL11.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GL11.GL_STATIC_DRAW);
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
        return vboIds[0];
    }

    // Aggiorna un VBO già esistente con nuovi dati (upload dinamico)
    public static void uploadVertexBuffer(GL11 gl, int vboId, FloatBuffer buffer) {
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vboId);
        gl.glBufferData(GL11.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GL11.GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
    }

    // Facoltativo: elimina un VBO
    public static void deleteVbo(GL11 gl, int vboId) {
        int[] vboIds = new int[]{vboId};
        gl.glDeleteBuffers(1, vboIds, 0);
    }
}

