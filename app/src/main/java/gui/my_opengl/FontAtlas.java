package gui.my_opengl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.opengl.GLES11;
import android.opengl.GLUtils;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class FontAtlas {
    private final Bitmap bitmap;
    private final int textureId;
    private final int cellSize;
    private final int cols = 16; // 16x16 = 256 ASCII
    private final int rows = 16;
    private final int firstChar = 32; // saltiamo i primi 32 caratteri non stampabili

    public FontAtlas(GL11 gl, int textSizePx, int color) {
        cellSize = textSizePx + 8;
        int bitmapSize = cellSize * cols;
        bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSizePx);
        paint.setColor(color);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < 256; i++) {
            int col = i % cols;
            int row = i / cols;
            int x = col * cellSize;
            int y = (row + 1) * cellSize - 8;
            canvas.drawText(String.valueOf((char) i), x, y, paint);
        }

        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        textureId = textures[0];
    }

    public RectF getUV(char c) {
        int index = c;
        int col = index % cols;
        int row = index / cols;
        float u = col / (float) cols;
        float v = row / (float) rows;
        float u2 = (col + 1) / (float) cols;
        float v2 = (row + 1) / (float) rows;
        return new RectF(u, v2, u2, v); // invertito per GL
    }

    public int getTextureId() {
        return textureId;
    }

    public int getCellSize() {
        return cellSize;
    }

    public void dispose() {
        bitmap.recycle();
    }
}

