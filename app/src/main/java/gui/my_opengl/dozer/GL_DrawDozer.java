package gui.my_opengl.dozer;

import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;
import static packexcalib.exca.DataSaved.GL_LAMA;
import static utils.MyTypes.JOYSTICKS;

import android.graphics.Color;

import gui.draw_class.MyColorClass;
import gui.my_opengl.Cylinder;
import gui.my_opengl.GLDrawer;
import gui.my_opengl.GL_Methods;
import gui.my_opengl.My3DActivity;
import gui.my_opengl.MyGLRenderer;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import gui.my_opengl.exca.BoomsDrawer;
import packexcalib.exca.DataSaved;

public class GL_DrawDozer {
    private static float rs() {
        return MyGLRenderer.currentRenderScale();
    }

    public static void draw(GL11 gl11) {
        if (My3DActivity.glVista3d == 1) {
            BoomsDrawer lama = new BoomsDrawer(
                    GL_LAMA,
                    My_Lama.lamaChiara(),
                    new float[]{coloreEsterno[0], coloreEsterno[1], coloreEsterno[2], 1f},
                    My_Lama.lamaScura(),
                    coloreInterno,
                    My_Lama.lamaContour()
            );
            lama.draw(gl11);
        } else {
            BoomsDrawer lama = new BoomsDrawer(
                    GL_LAMA,
                    My_Lama.lamaChiara(),
                    new float[]{coloreInterno[0], coloreInterno[1], coloreInterno[2], 1f},
                    My_Lama.lamaScura(),
                    coloreEsterno,
                    My_Lama.lamaContour()
            );
            lama.draw(gl11);
        }

        Cylinder attacco = new Cylinder(//TODO Da qui si sviluppa la macchina
                p3tof(GL_LAMA[14]),
                p3tof(GL_LAMA[15]),
                (float) (DataSaved.altezzaLama * 0.15f * rs()),
                (float) (DataSaved.altezzaLama * 0.15f * rs()),
                coloreInterno,
                8,
                true
        );
        attacco.draw(gl11);

        Cylinder palo1 = new Cylinder(
                p3tof(GL_LAMA[16]),
                p3tof(GL_LAMA[17]),
                0.05f * rs(),
                0.04f * rs(),
                GL_Methods.parseColorToGL(MyColorClass.colorStick),
                10,
                true
        );
        Cylinder palo2 = new Cylinder(
                p3tof(GL_LAMA[18]),
                p3tof(GL_LAMA[19]),
                0.05f * rs(),
                0.04f * rs(),
                GL_Methods.parseColorToGL(MyColorClass.colorStick),
                10,
                true
        );
        Cylinder ant1 = new Cylinder(
                p3tof(GL_LAMA[20]),
                p3tof(GL_LAMA[16]),
                0.1f * rs(),
                0.09f * rs(),
                GL_Methods.parseColorToGL(Color.BLUE),
                10,
                true
        );
        Cylinder ant2 = new Cylinder(
                p3tof(GL_LAMA[21]),
                p3tof(GL_LAMA[18]),
                0.1f * rs(),
                0.09f * rs(),
                GL_Methods.parseColorToGL(Color.RED),
                10,
                true
        );
        Cylinder cover1 = new Cylinder(
                p3tof(GL_LAMA[17]),
                p3tof(GL_LAMA[22]),
                0.11f * rs(),
                0.11f * rs(),
                GL_Methods.parseColorToGL(MyColorClass.colorStick),
                4,
                true
        );
        Cylinder cover2 = new Cylinder(
                p3tof(GL_LAMA[19]),
                p3tof(GL_LAMA[23]),
                0.11f * rs(),
                0.11f * rs(),
                GL_Methods.parseColorToGL(MyColorClass.colorStick),
                4,
                true
        );

        drawBucketEdge(gl11);

        if (DataSaved.isCanOpen != JOYSTICKS) {
            cover1.drawL(gl11, true);
            cover2.drawL(gl11, true);
            palo1.draw(gl11);
            palo2.draw(gl11);
            ant1.draw(gl11);
            ant2.draw(gl11);
        }

        drawAlignHelpers(gl11);
    }

    public static void drawMachine(GL11 gl11) {
        GL_DrawDozerBody.draw(gl11);
    }

    private static void drawBucketEdge(GL11 gl11) {
        Cylinder spigolo;
        int colore = Color.BLUE;
        if (DataSaved.isLowerEdge) {
            colore = Color.RED;
        }

        switch (DataSaved.bucketEdge) {
            case -1:
                spigolo = new Cylinder(
                        p3tof(GL_LAMA[26]),
                        p3tof(GL_LAMA[29]),
                        0.05f * rs(),
                        0.01f * rs(),
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl11);
                break;

            case 0:
                spigolo = new Cylinder(
                        p3tof(GL_LAMA[24]),
                        p3tof(GL_LAMA[27]),
                        0.05f * rs(),
                        0.01f * rs(),
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl11);
                break;

            case 1:
                spigolo = new Cylinder(
                        p3tof(GL_LAMA[25]),
                        p3tof(GL_LAMA[28]),
                        0.05f * rs(),
                        0.01f * rs(),
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl11);
                break;
        }
    }

    private static float[] p3tof(Point3DF point3DF) {
        return new float[]{
                point3DF.getX(),
                point3DF.getY(),
                point3DF.getZ()
        };
    }

    private static void drawAlignHelpers(GL11 gl11) {
        if (DataSaved.showAlign <= 0) return;

        float[] c = GL_Methods.parseColorToGL(Color.GREEN);

        float[] lineVertices = {
                GL_LAMA[30].getX(), GL_LAMA[30].getY(), GL_LAMA[30].getZ(), GL_LAMA[31].getX(), GL_LAMA[31].getY(), GL_LAMA[31].getZ(),
                GL_LAMA[30].getX(), GL_LAMA[30].getY(), GL_LAMA[30].getZ(), GL_LAMA[32].getX(), GL_LAMA[32].getY(), GL_LAMA[32].getZ(),
                GL_LAMA[30].getX(), GL_LAMA[30].getY(), GL_LAMA[30].getZ(), GL_LAMA[34].getX(), GL_LAMA[34].getY(), GL_LAMA[34].getZ(),
                GL_LAMA[30].getX(), GL_LAMA[30].getY(), GL_LAMA[30].getZ(), GL_LAMA[33].getX(), GL_LAMA[33].getY(), GL_LAMA[33].getZ(),
        };

        GLDrawer.drawRawLines3D(
                lineVertices,
                8,
                new float[]{c[0], c[1], c[2], 1f},
                3f
        );
    }
}
