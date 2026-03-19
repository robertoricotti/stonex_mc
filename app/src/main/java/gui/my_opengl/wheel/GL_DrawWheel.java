package gui.my_opengl.wheel;

import static gui.my_opengl.MyGLRenderer.coloreEsterno;
import static gui.my_opengl.MyGLRenderer.coloreInterno;
import static gui.my_opengl.MyGLRenderer.scale;
import static gui.my_opengl.wheel.My_Wheel.cullaIndicesW;
import static gui.my_opengl.wheel.My_Wheel.leftSideIndicesW;
import static gui.my_opengl.wheel.My_Wheel.rightSideIndicesW;
import static packexcalib.exca.DataSaved.GL_WHEEL;

import android.graphics.Color;

import gui.my_opengl.Cylinder;
import gui.my_opengl.GLDrawer;
import gui.my_opengl.GL_Methods;
import gui.my_opengl.MyGLRenderer;
import gui.my_opengl.Point3DF;
import gui.my_opengl.compat.GL11;
import gui.my_opengl.dozer.WheelRender;
import packexcalib.exca.DataSaved;

public class GL_DrawWheel {
    private static float rs() {
        return MyGLRenderer.currentRenderScale();
    }

    public static void draw(GL11 gl11) {

        WheelRender wheelRender = new WheelRender(
                DataSaved.GL_WHEEL,
                leftSideIndicesW,
                rightSideIndicesW,
                cullaIndicesW,
                coloreInterno,
                coloreEsterno,
                0.02f
        );
        wheelRender.draw(gl11);

        drawBucketEdge(gl11);
        drawAlignHelpers(gl11);

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
                        p3tof(GL_WHEEL[20]),
                        p3tof(GL_WHEEL[23]),
                        0.05f * rs(),
                        0.02f * rs(),
                        GL_Methods.parseColorToGL(colore),
                        8,
                        false
                );
                spigolo.draw(gl11);
                break;

            case 0:
                spigolo = new Cylinder(
                        p3tof(GL_WHEEL[18]),
                        p3tof(GL_WHEEL[21]),
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
                        p3tof(GL_WHEEL[19]),
                        p3tof(GL_WHEEL[22]),
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

    private static void drawAlignHelpers(GL11 gl11) {
        if (DataSaved.showAlign <= 0) return;

        float[] c = GL_Methods.parseColorToGL(Color.GREEN);

        float[] lineVertices = {
                GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[31].getX(), GL_WHEEL[31].getY(), GL_WHEEL[31].getZ(),
                GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[32].getX(), GL_WHEEL[32].getY(), GL_WHEEL[32].getZ(),
                GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[34].getX(), GL_WHEEL[34].getY(), GL_WHEEL[34].getZ(),
                GL_WHEEL[30].getX(), GL_WHEEL[30].getY(), GL_WHEEL[30].getZ(), GL_WHEEL[33].getX(), GL_WHEEL[33].getY(), GL_WHEEL[33].getZ(),
        };

        GLDrawer.drawRawLines3D(
                lineVertices,
                8,
                new float[]{c[0], c[1], c[2], 1f},
                3f
        );
    }


    private static float[] p3tof(Point3DF point3DF) {
        return new float[]{
                point3DF.getX(),
                point3DF.getY(),
                point3DF.getZ()
        };
    }


}