package gui.draw_class;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import packexcalib.exca.DataSaved;

public class HeightLevelBar extends View {
    Paint paint;
    public int indexLeverBar;

    public HeightLevelBar(Context context) {
        super(context);
        this.paint = new Paint();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);

        Path path = new Path();

        if (indexLeverBar == 0){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            if(DataSaved.colorMode==0){
                paint.setColor(Color.RED);}else{paint.setColor(Color.BLUE);}

        }
        else{
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
        }

        float p1Total = getHeight();
        float p1Start = (float) (0.05 * p1Total);
        float p1End = (float) (0.2 * p1Total);
        float p1Mid = (float) ((p1Start + ((p1End - p1Start) * 0.5)));

        path.moveTo(getWidth() * (float) 0.02, p1Start);
        path.lineTo(getWidth() * (float)0.98, p1Start);
        path.lineTo(getWidth() * (float)0.98, p1Mid);
        path.lineTo(getWidth() / 2f, p1End);
        path.lineTo(getWidth() * (float) 0.02, p1Mid);
        path.lineTo(getWidth() * (float) 0.02, p1Start);
        canvas.drawPath(path, paint);
        path.reset();

        if (indexLeverBar == 1){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            if(DataSaved.colorMode==0){
            paint.setColor(Color.RED);}else{paint.setColor(Color.BLUE);}
        }
        else{
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
        }

        float p2Total = getHeight();
        float p2Start = (float) (0.25 * p2Total);
        float p2End = (float) (0.4 * p2Total);
        float p2Mid = (float) ((p2Start + ((p2End - p2Start) * 0.5)));
        path.moveTo(getWidth() * (float) 0.02, p2Start);
        path.lineTo(getWidth() * (float)0.98, p2Start);
        path.lineTo(getWidth() * (float)0.98, p2Mid);
        path.lineTo(getWidth() / 2f, p2End);
        path.lineTo(getWidth() * (float) 0.02, p2Mid);
        path.lineTo(getWidth() * (float) 0.02, p2Start);
        canvas.drawPath(path, paint);
        path.reset();

        if (indexLeverBar == 2){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.GREEN);
        }
        else{
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
        }

        float p3Total = getHeight();
        float p3Start = (float) (0.45 * p3Total);
        float p3End = (float) (0.55 * p3Total);
        path.moveTo(getWidth() * (float) 0.02, p3Start);
        path.lineTo(getWidth() * (float)0.98, p3Start);
        path.lineTo(getWidth() * (float)0.98, p3End);
        path.lineTo(getWidth() * (float) 0.02, p3End);
        path.lineTo(getWidth() * (float) 0.02, p3Start);
        canvas.drawPath(path, paint);
        path.reset();

        if (indexLeverBar == 3){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            if(DataSaved.colorMode==0){
            paint.setColor(Color.BLUE);}else{paint.setColor(Color.RED);}
        }
        else{
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
        }

        float p4Total = getHeight();
        float p4Start = (float) (0.6 * p4Total);
        float p4End = (float) (0.75 * p4Total);
        float p4Mid = (float) ((p4Start + ((p4End - p4Start) * 0.5)));
        path.moveTo(getWidth() * (float) 0.02, p4End);
        path.lineTo(getWidth() * (float)0.98, p4End);
        path.lineTo(getWidth() * (float)0.98, p4Mid);
        path.lineTo(getWidth() / 2f, p4Start);
        path.lineTo(getWidth() * (float) 0.02, p4Mid);
        path.lineTo(getWidth() * (float) 0.02, p4End);
        canvas.drawPath(path, paint);
        path.reset();

        if (indexLeverBar == 4){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            if(DataSaved.colorMode==0){
                paint.setColor(Color.BLUE);}else{paint.setColor(Color.RED);}
        }
        else{
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
        }

        float p5Total = getHeight();
        float p5Start = (float) (0.8 * p5Total);
        float p5End = (float) (0.95 * p5Total);
        float p5Mid = (float) ((p5Start + ((p5End - p5Start) * 0.5)));
        path.moveTo(getWidth() * (float) 0.02, p5End);
        path.lineTo(getWidth() * (float)0.98, p5End);
        path.lineTo(getWidth() * (float)0.98, p5Mid);
        path.lineTo(getWidth() / 2f, p5Start);
        path.lineTo(getWidth() * (float) 0.02, p5Mid);
        path.lineTo(getWidth() * (float) 0.02, p5End);
        canvas.drawPath(path, paint);
        path.reset();
    }
}
