package gui.my_opengl;

import android.content.Context;

import java.io.InputStream;
import java.util.ArrayList;

public class ObjLoader {
    public static float[] loadVertices(Context context, int resourceId) {
        ArrayList<Float> vertices = new ArrayList<>();
        try {
            // Carica il file OBJ
            InputStream is = context.getResources().openRawResource(resourceId);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
            String line;

            // Leggi il file linea per linea
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("v ")) {
                    // Esegui il parsing della linea dei vertici
                    String[] tokens = line.split(" ");
                    vertices.add(Float.parseFloat(tokens[1])); // X
                    vertices.add(Float.parseFloat(tokens[2])); // Y
                    vertices.add(Float.parseFloat(tokens[3])); // Z
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Converti la lista di float in un array di float
        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }

        return vertexArray;
    }
    public static short[] loadFaces(Context context, int resourceId) {
        ArrayList<Short> faces = new ArrayList<>();
        try {
            // Carica il file OBJ
            InputStream is = context.getResources().openRawResource(resourceId);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
            String line;

            // Leggi il file linea per linea
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("f ")) {
                    // Esegui il parsing delle facce
                    String[] tokens = line.split(" ");
                    for (int i = 1; i < tokens.length; i++) {
                        // Gli indici delle facce partono da 1 nel file .obj
                        String[] parts = tokens[i].split("/");
                        faces.add((short) (Short.parseShort(parts[0]) - 1)); // Indice del vertice (decrementato di 1)
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Converti la lista in un array di short
        short[] faceArray = new short[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            faceArray[i] = faces.get(i);
        }

        return faceArray;
    }
}
