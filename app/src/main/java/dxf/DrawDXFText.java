package dxf;

import android.graphics.Canvas;
import android.graphics.Paint;

public class DrawDXFText {
    public static void draw(Canvas canvas, Paint paint, DxfText dxfText, float bucketX, float bucketY, double bucketEst, double bucketNord, float scala, int color, double rotationAngle) {

        float offX = 3f;//alzare il testo dal punto
        float offY = 3f;//sposatre a dx il testo dal punto
        double diffX = (dxfText.getX() - bucketEst) * scala;
        double diffY = (dxfText.getY() - bucketNord) * scala;
        String testo = dxfText.getText();
        float rotatedXV1 = (float) (bucketX + diffX * Math.cos(rotationAngle) - diffY * Math.sin(rotationAngle));
        float rotatedYV1 = (float) (bucketY - diffX * Math.sin(rotationAngle) - diffY * Math.cos(rotationAngle));
        paint.setTextSize(30);
        paint.setColor(color);
        canvas.drawText(testo, rotatedXV1 + offX, rotatedYV1 - offY, paint);


    }
}
