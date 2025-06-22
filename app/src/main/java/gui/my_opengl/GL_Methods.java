package gui.my_opengl;

import android.graphics.Color;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import dxf.Face3D;
import dxf.Point3D;
import packexcalib.exca.DataSaved;

public class GL_Methods {

    public static float averageZ(Face3D face, double[] reference) {
        Point3D[] pts = face.getP4().equals(face.getP3()) ?
                new Point3D[]{face.getP1(), face.getP2(), face.getP3()} :
                new Point3D[]{face.getP1(), face.getP2(), face.getP3(), face.getP4()};

        float sumZ = 0f;
        for (Point3D pt : pts) {
            sumZ += (float) (pt.getZ() - reference[2]);
        }
        return sumZ / pts.length;
    }

    public static int myParseColor(int color) {
        try {
            String s = Integer.toHexString(color);

            if (DataSaved.temaSoftware == 0) {
                //sfondo nero
                if (s.equalsIgnoreCase("ff000000")) {
                    int[] rgb = new int[]{255, 255, 255};
                    return Color.rgb(rgb[0], rgb[1], rgb[2]);
                } else {
                    return color;
                }

            } else {
                if (s.equalsIgnoreCase("ffffffff")) {
                    int[] rgb = new int[]{0, 0, 0};
                    return Color.rgb(rgb[0], rgb[1], rgb[2]);
                } else {
                    return color;
                }
            }
        } catch (Exception e) {
            switch (DataSaved.temaSoftware) {
                case 0:
                    color = Color.WHITE;
                    break;
                case 1:
                    color = Color.BLACK;
                    break;
                case 2:
                    color = Color.BLACK;
                    break;
            }
        }
        return color;
    }
    public static ShortBuffer createIndexBuffer(int[] indices) {
        ByteBuffer bb = ByteBuffer.allocateDirect(indices.length * 2);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer ib = bb.asShortBuffer();
        for (int i : indices) {
            ib.put((short) i);
        }
        ib.position(0);
        return ib;
    }
    public static FloatBuffer createFloatBuffer(List<Point3DF> points) {
        float[] coords = new float[points.size() * 3];
        for (int i = 0; i < points.size(); i++) {
            Point3DF p = points.get(i);
            coords[i * 3] = p.x;
            coords[i * 3 + 1] = p.y;
            coords[i * 3 + 2] = p.z;
        }
        return createFloatBuffer(coords);
    }

    public static FloatBuffer createFloatBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }
    public static float[] parseColorToGL(int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        return new float[] { r, g, b, a };
    }

    public static float[] darkenColor(float[] color, float factor,float alpha) {
        factor = Math.max(0f, Math.min(1f, factor)); // Clamp tra 0 e 1
        return new float[] {
                color[0] * factor,
                color[1] * factor,
                color[2] * factor,
                color[3] =alpha,// lascia alpha invariato
        };
    }
    public static float[] getGradientColor(double z, double min, double max) {
        double normalized = Math.max(0, Math.min(1, (z - min) / (max - min)));

        float r, g, b;

        if (normalized <= 0.5) {
            // Blu → Verde
            float t = (float)(normalized / 0.5); // da 0 a 1
            r = 0f;
            g = t;
            b = 1f - t;
        } else {
            // Verde → Rosso
            float t = (float)((normalized - 0.5) / 0.5); // da 0 a 1
            r = t;
            g = 1f - t;
            b = 0f;
        }

        return new float[]{r, g, b, 1f};
    }


    public static float[] getColormapColor(double z, double min, double max) {
        double normalized = Math.max(0, Math.min(1, (z - min) / (max - min)));

        // Usa una colormap simile a 'Turbo' con transizioni fluide
        float r = (float)(0.5 + 0.5 * Math.sin(2 * Math.PI * normalized));
        float g = (float)(0.5 + 0.5 * Math.sin(2 * Math.PI * (normalized - 0.33)));
        float b = (float)(0.5 + 0.5 * Math.sin(2 * Math.PI * (normalized - 0.66)));

        return new float[]{clamp(r), clamp(g), clamp(b), 1f};
    }

    private static float clamp(float val) {
        return Math.max(0f, Math.min(1f, val));
    }

    public static float[] getJetColor(double z, double zMin, double zMax) {
        double normalized = (z - zMin) / (zMax - zMin);
        normalized = Math.max(0, Math.min(1, normalized)); // clamp tra 0 e 1

        float r, g, b;

        if (normalized < 0.125) { // Blu scuro
            r = 0f; g = 0f; b = 0.5f + (float)(normalized / 0.125) * 0.5f;
        } else if (normalized < 0.375) { // Blu → Ciano
            double t = (normalized - 0.125) / 0.25;
            r = 0f; g = (float)t; b = 1f;
        } else if (normalized < 0.625) { // Ciano → Verde → Giallo
            double t = (normalized - 0.375) / 0.25;
            r = (float)t; g = 1f; b = 1f - (float)t;
        } else if (normalized < 0.875) { // Giallo → Arancione
            double t = (normalized - 0.625) / 0.25;
            r = 1f; g = 1f - (float)(t * 0.5f); b = 0f;
        } else { // Arancione → Rosso
            double t = (normalized - 0.875) / 0.125;
            r = 1f; g = 0.5f * (1f - (float)t); b = 0f;
        }

        return new float[]{r, g, b, 1f};
    }



    public static double findMinZ(List<Face3D> faces) {
        double minZ = Double.MAX_VALUE;

        for (Face3D face : faces) {
            for (Point3D point : face.getVertices()) {
                if (point.getZ() < minZ) {
                    minZ = point.getZ();
                }
            }
        }

        return minZ;
    }
    public static double findMaxZ(List<Face3D> faces) {
        double maxZ = -Double.MAX_VALUE;

        for (Face3D face : faces) {
            for (Point3D point : face.getVertices()) {
                if (point.getZ() > maxZ) {
                    maxZ = point.getZ();
                }
            }
        }

        return maxZ;
    }

}
