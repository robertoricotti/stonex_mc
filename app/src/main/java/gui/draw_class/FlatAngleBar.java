package gui.draw_class;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import com.example.stx_dig.R;

import java.util.ArrayList;

import gui.MyApp;

public class FlatAngleBar extends View {
    Paint paint;

    public int indexFlatBar = 0;

    public FlatAngleBar(Context context) {
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
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        PointF[] standard = new PointF[]{
                new PointF((float)52.5815, (float)13.0176), //0
                new PointF((float)53.1837, (float)1.4166),//1
                new PointF((float)43.7572, (float)5.8822),//2
                new PointF((float)37.7717,	(float)17.3411),//3
                new PointF((float)30.3037,	(float)28.3244),//4
                new PointF((float)17.4508, (float)35.3403),//5
                new PointF((float)5.8659, (float)37.8588),//6
                new PointF((float)1.623, (float)46.8999),//7
                new PointF((float)5.0675, (float)54.6994),//8
                new PointF((float)16.5718, (float)60.0554),//9
                new PointF((float)20.8271, (float)68.1176),//10
                new PointF((float)19.1731, (float)81.4065),//11
                new PointF((float)17.4508, (float)103.5882),//12
                new PointF((float)22.5494, (float)118.7327),//13
                new PointF((float)33.1274, (float)129.9412),//14
                new PointF((float)52.5815, (float)137.0752),//15
                new PointF((float)83.7483, (float)138.307),//16
                new PointF((float)109.0588, (float)138.307),//17
                new PointF((float)134.8235, (float)138.307),//18
                new PointF((float)148.324, (float)140.4469),//19
                new PointF((float)167.0815, (float)137.3857),//20
                new PointF((float)167.0815, (float)133.3857),//21
                new PointF((float)145.4118, (float)129.9412),//22
                new PointF((float)125.8926, (float)110.384),//23
                new PointF((float)110.7811, (float)94.1013),//24
                new PointF((float)97.618, (float)78.9223),//25
                new PointF((float)86.0745, (float)63.4999),//26
                new PointF((float)75.5718, (float)46.8999),//27
                new PointF((float)64.8578, (float)26.6021),//28
                new PointF((float)63.1355, (float)7.6045),//29
                new PointF((float)11.405, (float)46.8999),//30
                new PointF(109.8315f, 75.04639999999999f) //punto medio
        };

        float size = 2.25f * (getHeight() / 536.00f);

        ArrayList<Float> x = new ArrayList<>();
        ArrayList<Float> y = new ArrayList<>();
        ArrayList<Float> x1 = new ArrayList<>();
        ArrayList<Float> y1 = new ArrayList<>();
        ArrayList<Float> x2 = new ArrayList<>();
        ArrayList<Float> y2 = new ArrayList<>();

        x.add(getWidth() / 2f);
        y.add(getHeight() / 2f);
        x1.add(getWidth() / 2f);
        y1.add(getHeight() / 2f);
        x2.add(getWidth() / 2f);
        y2.add(getHeight() / 2f);

        float distanceIndex, angleIndex;
        for(int i = 1; i < standard.length; i++){
            distanceIndex = getDistance(standard[0].x, standard[0].y, standard[i].x, standard[i].y) * size;
            angleIndex = getDegrees(standard[0].x, standard[0].y, standard[i].x, standard[i].y);
            x.add((float) (x.get(0) + distanceIndex * Math.cos(angleIndex)));
            y.add((float) (y.get(0) + distanceIndex * Math.sin(angleIndex)));

            x1.add((float) (x.get(0) + distanceIndex * Math.cos(angleIndex + Math.toRadians(120))));
            y1.add((float) (y.get(0) + distanceIndex * Math.sin(angleIndex + Math.toRadians(120))));

            x2.add((float) (x.get(0) + distanceIndex * Math.cos(angleIndex + Math.toRadians(-30))));
            y2.add((float) (y.get(0) + distanceIndex * Math.sin(angleIndex + Math.toRadians(-30))));
        }

        float offsy = (getHeight() * 70f / 100) - y.get(20);
        for(int i = 0; i < y.size(); i++){
            y.set(i, y.get(i) + offsy);
        }

        float offsx = (getWidth() * 52.5f / 100) - x.get(31);
        for(int i = 0; i < x.size(); i++){
            x.set(i, x.get(i) + offsx);
        }

        float offsy1 = (getHeight() * 70f / 100) - y1.get(20);
        for(int i = 0; i < y1.size(); i++){
            y1.set(i, y1.get(i) + offsy1);
        }

        float offsx1 = (getWidth() * 25f / 100) - x1.get(0);
        for(int i = 0; i < x1.size(); i++){
            x1.set(i, x1.get(i) + offsx1);
        }

        float offsy2 = (getHeight() * 55f / 100) - y2.get(20);
        for(int i = 0; i < y2.size(); i++){
            y2.set(i, y2.get(i) + offsy2);
        }

        float offsx2 = (getWidth() * 72f / 100) - x2.get(7);
        for(int i = 0; i < x2.size(); i++){
            x2.set(i, x2.get(i) + offsx2);
        }

        float radius = (getDistance(standard[0].x, standard[0].y, standard[1].x, standard[1].y) * size);
        float radius2 = (getDistance(standard[30].x, standard[30].y, standard[7].x, standard[7].y) * size);
        /*-------------START-DRAWING--------------*/
        if (indexFlatBar == 0){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(getResources().getColor(R.color.bg_sfsred));
        }
        else{
            paint.setStyle(Paint.Style.STROKE);
            if(MyApp.Actualactivity.contains("3D")){paint.setColor(Color.BLACK);}else{paint.setColor(Color.WHITE);}

        }
        path.moveTo(x1.get(1), y1.get(1));
        for(int i = 1; i < x1.size() - 2; i++){
            path.lineTo(x1.get(i), y1.get(i));
        }
        canvas.drawCircle(x1.get(0), y1.get(0), radius, paint);
        canvas.drawCircle(x1.get(30), y1.get(30), radius2, paint);
        canvas.drawPath(path, paint);
        path.reset();

        if (indexFlatBar == 1){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            if(MyApp.Actualactivity.contains("3D")){paint.setColor(getResources().getColor(R.color.element_green));}else{paint.setColor(Color.GREEN);}


        }
        else{
            paint.setStyle(Paint.Style.STROKE);
            if(MyApp.Actualactivity.contains("3D")){paint.setColor(Color.BLACK);}else{paint.setColor(Color.WHITE);}
        }

        path.moveTo(x.get(1), y.get(1));
        for(int i = 1; i < x.size() - 2; i++){
            path.lineTo(x.get(i), y.get(i));
        }
        canvas.drawPath(path, paint);
        canvas.drawCircle(x.get(0), y.get(0), radius, paint);
        canvas.drawCircle(x.get(30), y.get(30), radius2, paint);
        path.reset();

        if (indexFlatBar == 2){
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(getResources().getColor(R.color.bg_sfsred));
        }
        else{
            paint.setStyle(Paint.Style.STROKE);
            if(MyApp.Actualactivity.contains("3D")){paint.setColor(Color.BLACK);}else{paint.setColor(Color.WHITE);}
        }
        path.moveTo(x2.get(1), y2.get(1));
        for(int i = 1; i < x2.size() - 2; i++){
            path.lineTo(x2.get(i), y2.get(i));
        }
        canvas.drawPath(path, paint);
        canvas.drawCircle(x2.get(0), y2.get(0), radius, paint);
        canvas.drawCircle(x2.get(30), y2.get(30), radius2, paint);
        path.reset();

        paint.setColor(Color.BLACK);
        canvas.drawCircle(x.get(0), y.get(0), (float) (radius * 0.60), paint);
        canvas.drawCircle(x.get(30), y.get(30), (float) (radius * 0.60), paint);
        canvas.drawCircle(x1.get(0), y1.get(0), (float) (radius * 0.60), paint);
        canvas.drawCircle(x1.get(30), y1.get(30), (float) (radius * 0.60), paint);
        canvas.drawCircle(x2.get(0), y2.get(0), (float) (radius * 0.60), paint);
        canvas.drawCircle(x2.get(30), y2.get(30), (float) (radius * 0.60), paint);

        path.rewind();

    }

    private float getDistance(float x1, float y1, float x2, float y2){
        final double sqrt = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        return (float) sqrt;
    }

    private float getDegrees(float x1, float y1, float x2, float y2){
        float dY = y2 - y1;
        float dX = x2 - x1;
        return (float) Math.atan2(dY, dX); // * 180 / Math.PI;
    }
}