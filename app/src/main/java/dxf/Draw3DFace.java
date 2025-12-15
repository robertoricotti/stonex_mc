package dxf;

import static gui.my_opengl.GL_Methods.getJetColorInt;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import packexcalib.exca.DataSaved;

public class Draw3DFace {

    public static void draw(Paint paint, Canvas canvas, double[][] vertices, float bucketX, float bucketY, double buckEst, double buckNord, int color, float scala, double rotationAngle, boolean triangola,boolean fill) {

        double pointNV1 = vertices[0][1];
        double pointEV1 = vertices[0][0];

        double pointNV2 = vertices[1][1];
        double pointEV2 = vertices[1][0];

        double pointNV3 = vertices[2][1];
        double pointEV3 = vertices[2][0];


        double diffXV1 = (pointEV1 - buckEst) * scala;
        double diffYV1 = (pointNV1 - buckNord) * scala;

        double diffXV2 = (pointEV2 - buckEst) * scala;
        double diffYV2 = (pointNV2 - buckNord) * scala;

        double diffXV3 = (pointEV3 - buckEst) * scala;
        double diffYV3 = (pointNV3 - buckNord) * scala;


        // nuove coordinate del secondo punto basate sulla rotazione e sui calcoli diff
        float rotatedXV1 = (float) (bucketX + diffXV1 * Math.cos(rotationAngle) - diffYV1 * Math.sin(rotationAngle));
        float rotatedYV1 = (float) (bucketY - diffXV1 * Math.sin(rotationAngle) - diffYV1 * Math.cos(rotationAngle));

        // nuove coordinate del secondo punto basate sulla rotazione e sui calcoli diff
        float rotatedXV2 = (float) (bucketX + diffXV2 * Math.cos(rotationAngle) - diffYV2 * Math.sin(rotationAngle));
        float rotatedYV2 = (float) (bucketY - diffXV2 * Math.sin(rotationAngle) - diffYV2 * Math.cos(rotationAngle));

        // nuove coordinate del secondo punto basate sulla rotazione e sui calcoli diff
        float rotatedXV3 = (float) (bucketX + diffXV3 * Math.cos(rotationAngle) - diffYV3 * Math.sin(rotationAngle));
        float rotatedYV3 = (float) (bucketY - diffXV3 * Math.sin(rotationAngle) - diffYV3 * Math.cos(rotationAngle));


        ///linee

        try {
            if(triangola) {
                paint.setColor(color);
                paint.setStrokeWidth((float) (0.8/ DataSaved.scale_Factor3D));
                canvas.drawLine(rotatedXV1, rotatedYV1, rotatedXV2, rotatedYV2, paint);
                canvas.drawLine(rotatedXV2, rotatedYV2, rotatedXV3, rotatedYV3, paint);
                canvas.drawLine(rotatedXV3, rotatedYV3, rotatedXV1, rotatedYV1, paint);
            }
        } catch (Exception ignored) {

        }


        Path path = new Path();
        path.moveTo(rotatedXV1, rotatedYV1); // Muovi il percorso al primo punto
        path.lineTo(rotatedXV2, rotatedYV2); // Aggiungi una linea dal punto precedente al punto attuale
        path.lineTo(rotatedXV3, rotatedYV3); // Aggiungi una linea dal punto precedente al punto attuale
        path.close(); // Chiudi il percorso
        if (fill) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(applyAlphaToColor(color,0.3f));
            canvas.drawPath(path, paint);

        }
    }
    public static void drawFaceGradientCanvas(
            Paint paint,
            Canvas canvas,
            double[][] vertices,
            float bucketX,
            float bucketY,
            double buckEst,
            double buckNord,
            float scala,
            double rotationAngle,
            double zMin,
            double zMax
    ) {
        if (vertices == null || vertices.length < 3) return;

        // Z media
        double zAvg =
                (vertices[0][2] +
                        vertices[1][2] +
                        vertices[2][2]) / 3.0;

        int color = getJetColorInt(zAvg, zMin, zMax, 0.75f);

        Path path = new Path();

        for (int i = 0; i < 3; i++) {
            double diffX = (vertices[i][0] - buckEst) * scala;
            double diffY = (vertices[i][1] - buckNord) * scala;

            float x = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
            float y = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));

            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }

        path.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawPath(path, paint);
    }






    private static int applyAlphaToColor(int color, float alpha) {
        // Estrai i componenti RGB dal colore (in formato ARGB)
        float red = ((color >> 16) & 0xFF) / 255.0f;  // Estrai il componente rosso (dal bit 16 al bit 23)
        float green = ((color >> 8) & 0xFF) / 255.0f; // Estrai il componente verde (dal bit 8 al bit 15)
        float blue = (color & 0xFF) / 255.0f;         // Estrai il componente blu (dal bit 0 al bit 7)
        float alphaOriginal = ((color >> 24) & 0xFF) / 255.0f; // Estrai il componente alpha (dal bit 24 al bit 31)

        // Applica il nuovo valore alpha, mantenendo gli altri componenti invariati
        float newAlpha = alphaOriginal * alpha; // Combina il valore alpha originale con quello nuovo

        // Ricostruisci il colore con il nuovo alpha
        int newColor = (Math.round(newAlpha * 255) << 24)  // Nuovo componente alpha
                | (Math.round(red * 255) << 16)     // Componente rosso
                | (Math.round(green * 255) << 8)    // Componente verde
                | Math.round(blue * 255);          // Componente blu

        return newColor;
    }
}
