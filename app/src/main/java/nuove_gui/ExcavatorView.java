package nuove_gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import dxf.Arc;
import dxf.AutoCADColor;
import dxf.Circle;
import dxf.Draw2DPolyline;
import dxf.Draw3DFace;
import dxf.Draw3DPolyline;
import dxf.DrawArcs;
import dxf.DrawCircles;
import dxf.DrawDXFPoint;
import dxf.DrawDXFText;
import dxf.DrawLines;
import dxf.DrawSelectedPolyline;
import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Line;
import dxf.Point3D;
import dxf.Polyline;
import dxf.Polyline_2D;
import gui.draw_class.MyColorClass;
import machine_draw.ExcavatorDrawer;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;

public class ExcavatorView extends View {
    Paint paint;
    Canvas canvas;
    double scala = 35;
    float bucketX,bucketY;
    ExcavatorDrawer drawer;
    boolean isXML, isXMLLyne, isXMLPoint;

    public ExcavatorView(Context context) {
        super(context);

        this.paint = new Paint();


        this.drawer = new ExcavatorDrawer();
        try {
            isXML = DataSaved.progettoSelected.substring(DataSaved.progettoSelected.lastIndexOf(".") + 1).equalsIgnoreCase("xml");

        } catch (Exception e) {
            isXML = false;
        }
        try {
            isXMLLyne = DataSaved.progettoSelected_POLY.substring(DataSaved.progettoSelected_POLY.lastIndexOf(".") + 1).equalsIgnoreCase("xml");

        } catch (Exception e) {
            isXMLLyne = false;
        }

        try {
            isXMLPoint = DataSaved.progettoSelected_POINT.substring(DataSaved.progettoSelected_POINT.lastIndexOf(".") + 1).equalsIgnoreCase("xml");

        } catch (Exception e) {
            isXMLPoint = false;
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        paint.setAntiAlias(true);
        //drawDXFElements(ExcavatorLib.bucketCoord[0], ExcavatorLib.bucketCoord[1]);


        drawer.update(getWidth(), getHeight(), "BUCKET", 0.50f, 0.80f, scala);


        drawer.drawFrame(canvas, paint, Color.rgb(255, 165, 0), scala);


        drawer.drawBoom1(canvas, paint, Color.rgb(255, 165, 0), scala);

        drawer.drawBoom2(canvas, paint, Color.rgb(255, 165, 0), scala);

        drawer.drawStick(canvas, paint, Color.rgb(255, 165, 0), scala);

        drawer.drawBucket(canvas, paint, Color.rgb(255, 165, 0));


        /*paint.setColor(Color.MAGENTA);
        canvas.drawCircle(drawer.startXYZ.x, drawer.startXYZ.y, 20, paint);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(drawer.GPS_Z.x, drawer.GPS_Z.y, 20, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(drawer.GPS_DX.x, drawer.GPS_DX.y, 20, paint);
        paint.setColor(Color.GREEN);
        canvas.drawCircle(drawer.GPS_DY.x, drawer.GPS_DY.y, 20, paint);
        paint.setColor(Color.CYAN);
        canvas.drawCircle(drawer.roll.x, drawer.roll.y, 20, paint);
        paint.setColor(Color.GRAY);
        canvas.drawCircle(drawer.pitch.x, drawer.pitch.y, 20, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(drawer.boom1.x, drawer.boom1.y, 20, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(drawer.boom2.x, drawer.boom2.y, 20, paint);
        paint.setColor(Color.RED);
        canvas.drawCircle(drawer.stick.x, drawer.stick.y, 20, paint);
        paint.setColor(Color.rgb(12, 89, 34));
        canvas.drawCircle(drawer.bucket.x, drawer.bucket.y, 20, paint);
        paint.setColor(Color.rgb(34, 22, 56));
        canvas.drawCircle(drawer.bucketLeft.x, drawer.bucketLeft.y, 20, paint);
        paint.setColor(Color.rgb(67, 123, 222));
        canvas.drawCircle(drawer.bucketRight.x, drawer.bucketRight.y, 20, paint);*/

    }

  /*  private void drawDXFElements(double bucketEst, double bucketNord) {
        try {

            if (isXML) {
                for (Face3D face : DataSaved.filteredFaces) {

                    // Cambia il tipo di lista in List<Face3D>
                    if (isLayerEnabled(face.getLayer().getLayerName())) {
                        Draw3DFace.draw(paint,
                                canvas,
                                face.toArrayWithCentroid(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                myParseColor(AutoCADColor.getColor(String.valueOf(face.getLayer().getColorState()))),
                                scala,
                                rotationAngle,
                                DataSaved.Triangoli_Surf != 0,
                                DataSaved.Colore_Surf == 2
                        );
                    }
                }

            } else {
                for (Face3D face : DataSaved.filteredFaces) {

                    if (isLayerEnabled(face.getLayer().getLayerName())) {
                        Draw3DFace.draw(paint,
                                canvas,
                                face.toArrayWithCentroid(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                myParseColor(face.getLayer().getColorState()),
                                scala,
                                rotationAngle,
                                DataSaved.Triangoli_Surf != 0,
                                DataSaved.Colore_Surf == 2
                        );
                    }

                }
            }
        } catch (Exception e) {
            Log.d("expRT", "excFace");
        }

        try {
            if (false) {//isXMLLyne
                for (Polyline polyline : DataSaved.polylines) {
                    if (isLayerEnabled(polyline.getLayer().getLayerName())) { // Controlla se il layer è abilitato
                        Draw3DPolyline.draw(paint,
                                canvas,
                                polyline.getVertices(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                myParseColor(AutoCADColor.getColor(String.valueOf(polyline.getLayer().getColorState()))),
                                scala,
                                rotationAngle,
                                polyline
                        );
                    }
                }
                if (DataSaved.isAutoSnap == 2 && DataSaved.selectedPoly != null
                        && DataSaved.selectedPoly.getLayer() != null && DataSaved.selectedPoly.getLayer().isEnable()) {
                    DrawSelectedPolyline.draw(canvas,
                            paint,
                            DataSaved.selectedPoly.getVertices(),
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            myParseColor(DataSaved.selectedPoly.getLineColor()),
                            scala,
                            rotationAngle
                    );
                }
            } else {

                for (Polyline polyline : DataSaved.polylines) {


                    if (isLayerEnabled(polyline.getLayer().getLayerName())) { // Controlla se il layer è abilitato

                        Draw3DPolyline.draw(paint,
                                canvas,
                                polyline.getVertices(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                myParseColor(polyline.getLineColor()),
                                scala,
                                rotationAngle,
                                polyline
                        );

                    }

                }
                if (DataSaved.isAutoSnap == 2 && DataSaved.selectedPoly != null
                        && DataSaved.selectedPoly.getLayer() != null && DataSaved.selectedPoly.getLayer().isEnable()) {
                    DrawSelectedPolyline.draw(canvas,
                            paint,
                            DataSaved.selectedPoly.getVertices(),
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            myParseColor(DataSaved.selectedPoly.getLineColor()),
                            scala,
                            rotationAngle
                    );
                    if (DataSaved.line_Offset != 0) {


                        DrawSelectedPolyline.draw(canvas,
                                paint,
                                DataSaved.selectedPoly_OFFSET.getVertices(),
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                MyColorClass.colorConstraint,
                                scala,
                                rotationAngle
                        );


                    }
                }
            }

        } catch (Exception e) {
            Log.d("expRT", "excPoly");
        }

        try {

            if (DataSaved.Punti_Surf == 1) {
                int col = 0;

                for (Point3D point : DataSaved.filteredPoints) {
                    if (isLayerEnabled(point.getLayer().getLayerName())) { // Controlla se il layer è abilitato
                        if (isXMLPoint) {
                            col = myParseColor(Color.WHITE);
                        } else {
                            col = myParseColor(point.getLayer().getColorState());
                        }
                        DrawDXFPoint.draw(canvas,
                                paint,
                                point,
                                bucketX,
                                bucketY,
                                bucketEst,
                                bucketNord,
                                scala,
                                col,
                                rotationAngle
                        );
                    }
                }
            }
        } catch (Exception e) {

            Log.d("expRT", "excPT");
        }

        try {
            if (DataSaved.ShowText == 1) {
                int col = 0;
                for (DxfText dxfText : DataSaved.filteredDxfTexts) {

                    if (isLayerEnabled(dxfText.getLayer().getLayerName())) { // Controlla se il layer è abilitato
                        if (isXMLPoint) {
                            col = myParseColor(Color.WHITE);
                        } else {
                            col = myParseColor(dxfText.getLayer().getColorState());
                        }
                        DrawDXFText.draw(canvas, paint, dxfText, bucketX, bucketY, bucketEst, bucketNord, scala, col, rotationAngle);
                    }
                }
            }
        } catch (Exception e) {
            Log.d("expRT", "excTxT");
        }
        try {


            for (Polyline_2D polyline_2D : DataSaved.polylines_2D) {
                if (isLayerEnabled(polyline_2D.getLayer().getLayerName())) {
                    Draw2DPolyline.draw(paint,
                            canvas,
                            polyline_2D.getVertices(),
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            myParseColor(polyline_2D.getLineColor()),
                            scala,
                            rotationAngle,
                            DataSaved.scale_Factor3D
                    );
                }

            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("expRT", "excPoly2D");
        }
        try {


            for (Arc arc : DataSaved.arcs) {
                if (isLayerEnabled(arc.getLayer().getLayerName())) {
                    DrawArcs.draw(canvas,
                            paint,
                            arc,
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            scala,
                            myParseColor(arc.getColor()),
                            ((NmeaListener.mch_Orientation + DataSaved.deltaGPS2) % 360));
                }

            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("expRT", "arcs");
        }
        try {


            for (Line line : DataSaved.lines_2D) {
                if (isLayerEnabled(line.getLayer().getLayerName())) {
                    DrawLines.draw(canvas,
                            paint,
                            line,
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            scala,
                            myParseColor(line.getColor()),
                            rotationAngle);
                }

            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("expRT", "lines");
        }
        try {


            for (Circle circle : DataSaved.circles) {
                if (isLayerEnabled(circle.getLayer().getLayerName())) {
                    DrawCircles.draw(canvas,
                            paint,
                            circle,
                            bucketX,
                            bucketY,
                            bucketEst,
                            bucketNord,
                            scala,
                            myParseColor(circle.getColor()),
                            rotationAngle);
                }

            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("expRT", "circles");
        }
        try {
            if (DataSaved.ShowJson == 1) {

                for (Polyline_2D polyline_2D : DataSaved.polylines_Json) {

                    drawPolylineJson(polyline_2D.getVertices(), polyline_2D.getLineColor(), bucketEst, bucketNord);
                }
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.d("expRT", "excPolyJson");
        }


    }*/

    private boolean isLayerEnabled(String layerName) {
        if (layerName == null || layerName.isEmpty()) {
            return false; // Layer nullo o vuoto non è abilitato
        }

        // Cerca il layer nelle tre liste
        for (Layer layer : DataSaved.dxfLayers_DTM) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                return true;
            }
        }
        for (Layer layer : DataSaved.dxfLayers_POLY) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                return true;
            }
        }
        for (Layer layer : DataSaved.dxfLayers_POINT) {
            if (layerName.equals(layer.getLayerName()) && layer.isEnable()) {
                return true;
            }
        }

        return false; // Se il layer non è trovato o non è abilitato
    }
    private int myParseColor(int color) {
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
}
