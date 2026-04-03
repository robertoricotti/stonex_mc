package gui.my_opengl.dozer;

import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;

import gui.my_opengl.Cylinder;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import gui.my_opengl.exca.BoomsDrawer;
import gui.my_opengl.dozer.My_DozerFrame.TrackData;

public class GL_DrawDozerBody {

    public static void draw(GL11 gl11) {
        Point3DF[] pts = My_DozerFrame.puntiDozer();
        if (pts.length == 0) {
            return;
        }

        float[] chiaro;
        float[] scuro;
        if (gui.my_opengl.My3DActivity.glVista3d == 1) {
            chiaro = new float[]{coloreEsterno[0], coloreEsterno[1], coloreEsterno[2], 1f};
            scuro = coloreInterno;
        } else {
            chiaro = new float[]{coloreInterno[0], coloreInterno[1], coloreInterno[2], 1f};
            scuro = coloreEsterno;
        }

        drawTracks(gl11, chiaro, scuro);

        BoomsDrawer body = new BoomsDrawer(
                pts,
                My_DozerFrame.dozerChiaro(),
                chiaro,
                My_DozerFrame.dozerScuro(),
                scuro,
                My_DozerFrame.dozerContour()
        );
        body.draw(gl11);
    }

    private static void drawTracks(GL11 gl11, float[] chiaro, float[] scuro) {
        TrackData[] tracks = My_DozerFrame.tracks();
        for (TrackData t : tracks) {
            drawTrack(gl11, t, chiaro, scuro);
        }
    }

    private static void drawTrack(GL11 gl11, TrackData t, float[] chiaro, float[] scuro) {
        Cylinder front = new Cylinder(
                p3tof(t.frontA),
                p3tof(t.frontB),
                t.drumRadius,
                t.drumRadius,
                chiaro,
                18,
                true
        );
        front.drawL(gl11, true);

        Cylinder rear = new Cylinder(
                p3tof(t.rearA),
                p3tof(t.rearB),
                t.drumRadius,
                t.drumRadius,
                chiaro,
                18,
                true
        );
        rear.drawL(gl11, true);

        Point3DF longitudinal = t.rearCenter.subtract(t.frontCenter).normalize();
        Point3DF lateral = t.frontA.subtract(t.frontB).normalize();
        Point3DF vertical = safeNormalize(longitudinal.cross(lateral), new Point3DF(0f, 0f, 1f));
        if (vertical.dot(t.frontCenter) < vertical.dot(t.frontCenter.subtract(vertical))) {
            vertical = vertical.scale(-1f);
        }

        Point3DF bottomFront = t.frontCenter;
        Point3DF bottomRear = t.rearCenter;
        drawBoxFromCenterLine(gl11, bottomFront, bottomRear, t.drumWidth, t.shoeThickness, lateral, vertical, chiaro, scuro);

        Point3DF topOffset = vertical.scale(t.topRunBottomZ - t.drumRadius);
        Point3DF topFront = t.frontCenter.add(topOffset);
        Point3DF topRear = t.rearCenter.add(topOffset);
        drawBoxFromCenterLine(gl11, topFront, topRear, t.drumWidth, t.shoeThickness, lateral, vertical, chiaro, scuro);

        // Riempimento interno tra i due tamburi, senza creare "muri" esterni.
        Point3DF fillFront = t.frontCenter.add(longitudinal.scale(t.drumRadius * 0.85f)).add(vertical.scale(t.shoeThickness));
        Point3DF fillRear = t.rearCenter.subtract(longitudinal.scale(t.drumRadius * 0.85f)).add(vertical.scale(t.shoeThickness));
        float fillHeight = (t.topRunBottomZ + t.shoeThickness) - (t.shoeThickness * 2f);
        drawBoxFromCenterLine(gl11, fillFront, fillRear, t.drumWidth * 0.82f, fillHeight, lateral, vertical, chiaro, scuro);
    }

    private static void drawBoxFromCenterLine(GL11 gl11,
                                              Point3DF frontBottomCenter,
                                              Point3DF rearBottomCenter,
                                              float width,
                                              float height,
                                              Point3DF lateral,
                                              Point3DF vertical,
                                              float[] chiaro,
                                              float[] scuro) {
        Point3DF[] box = new Point3DF[8];
        float halfW = width * 0.5f;
        Point3DF left = lateral.scale(halfW);
        Point3DF right = lateral.scale(-halfW);
        Point3DF up = vertical.scale(height);

        box[0] = frontBottomCenter.add(left);
        box[1] = frontBottomCenter.add(right);
        box[2] = frontBottomCenter.add(right).add(up);
        box[3] = frontBottomCenter.add(left).add(up);

        box[4] = rearBottomCenter.add(left);
        box[5] = rearBottomCenter.add(right);
        box[6] = rearBottomCenter.add(right).add(up);
        box[7] = rearBottomCenter.add(left).add(up);

        BoomsDrawer drawer = new BoomsDrawer(
                box,
                lightFaces(),
                chiaro,
                darkFaces(),
                scuro,
                boxEdges()
        );
        drawer.draw(gl11);
    }

    private static short[] lightFaces() {
        return new short[]{
                0, 1, 2, 0, 2, 3,
                3, 2, 6, 3, 6, 7,
                0, 3, 7, 0, 7, 4
        };
    }

    private static short[] darkFaces() {
        return new short[]{
                1, 5, 6, 1, 6, 2,
                4, 7, 6, 4, 6, 5,
                0, 4, 5, 0, 5, 1,
                0, 2, 6, 0, 6, 4
        };
    }

    private static short[] boxEdges() {
        return new short[]{
                0, 1, 1, 2, 2, 3, 3, 0,
                4, 5, 5, 6, 6, 7, 7, 4,
                0, 4, 1, 5, 2, 6, 3, 7
        };
    }

    private static Point3DF safeNormalize(Point3DF p, Point3DF fallback) {
        if (p == null || p.length() < 0.0001f) {
            return fallback;
        }
        return p.normalize();
    }

    private static float[] p3tof(Point3DF point3DF) {
        return new float[]{
                point3DF.getX(),
                point3DF.getY(),
                point3DF.getZ()
        };
    }
}
