package packexcalib.surfcreator;


import static gui.MyApp.MAX_NUMERO_FACCE;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dxf.DxfText;
import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import dxf.Polyline;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;

public class TriangleHelper {



    public TriangleHelper() {

    }

    public void updateTrianglesInRadius(double[] currentPosition, double radius) {
        List<Face3D> filteredTriangles = new ArrayList<>();

        // Filtro le facce basandomi sul layer e sulla distanza
        for (Face3D face : DataSaved.dxfFaces) {
            if (face.getLayer() != null && isLayerEnabled(face.getLayer().getLayerName())) {
                for (Point3D vertex : new Point3D[]{face.getP1(), face.getP2(), face.getP3(), face.getP4()}) {
                    if (distance(currentPosition, new double[]{vertex.getX(), vertex.getY(), vertex.getZ()}) <= radius) {
                        filteredTriangles.add(face);
                        break;
                    }
                }
            }
        }

        // Ordina le facce per distanza dalla currentPosition
        filteredTriangles.sort((f1, f2) -> {
            double dist1 = calculateFaceDistance(currentPosition, f1);
            double dist2 = calculateFaceDistance(currentPosition, f2);
            return Double.compare(dist1, dist2);
        });

        // Limita il numero massimo di facce a 5000 o 10000 se MEGA_1
        if (filteredTriangles.size() > MAX_NUMERO_FACCE) {
            filteredTriangles = filteredTriangles.subList(0, MAX_NUMERO_FACCE);
        }

        // Aggiorna DataSaved.filteredFaces
        DataSaved.filteredFaces = filteredTriangles;
        copiaFacce();


        List<Point3D> filteredPunti = new ArrayList<>();
        for (Point3D point : DataSaved.points) {
            if (distance2d(currentPosition, point) <= radius) {
                if (point.getLayer().getLayerName() != null) {
                    if (isLayerEnabled(point.getLayer().getLayerName())) {
                        filteredPunti.add(point);
                    }
                }
            }
        }

        filteredPunti.sort((f1, f2) -> {
            double dist1 = distance2d(currentPosition, f1);
            double dist2 = distance2d(currentPosition, f2);
            return Double.compare(dist1, dist2);
        });
        // Limita il numero massimo di punti a 1500
        if (filteredPunti.size() > 1500) {
            filteredPunti = filteredPunti.subList(0, 1500);
        }

        DataSaved.filteredPoints = filteredPunti;



        //testi
        List<DxfText> filteredText = new ArrayList<>();
        for (DxfText text : DataSaved.dxfTexts) {
            if (distance2d(currentPosition, text) <= radius) {
                if (text.getLayer().getLayerName() != null) {
                    if (isLayerEnabled(text.getLayer().getLayerName())) {
                        filteredText.add(text);
                    }
                }
            }
        }
        filteredText.sort((f1, f2) -> {
            double dist1 = distance2d(currentPosition, f1);
            double dist2 = distance2d(currentPosition, f2);
            return Double.compare(dist1, dist2);
        });
        // Limita il numero massimo di punti a 1500
        if (filteredText.size() > 1500) {
            filteredText = filteredText.subList(0, 1500);
        }
        DataSaved.filteredDxfTexts = filteredText;





    }

    public void updatePointRaius(double[] currentPosition, double radius){
        List<Point3D_Drill> filteredPunti = new ArrayList<>();
        for (Point3D_Drill point : DataSaved.drill_points) {
            if (distance2d(currentPosition, point) <= radius) {

                        filteredPunti.add(point);

            }
        }

        filteredPunti.sort((f1, f2) -> {
            double dist1 = distance2d(currentPosition, f1);
            double dist2 = distance2d(currentPosition, f2);
            return Double.compare(dist1, dist2);
        });
        // Limita il numero massimo di punti a 1500
        if (filteredPunti.size() > 2500) {
            filteredPunti = filteredPunti.subList(0, 2500);
        }

        DataSaved.filtered_drill_points = filteredPunti;
    }

    // Calcola la distanza minima tra la currentPosition e una Face3D
    private double calculateFaceDistance(double[] currentPosition, Face3D face) {
        double minDistance = Double.MAX_VALUE;
        for (Point3D vertex : new Point3D[]{face.getP1(), face.getP2(), face.getP3(), face.getP4()}) {
            double distance = distance(currentPosition, new double[]{vertex.getX(), vertex.getY(), vertex.getZ()});
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }


    public double calculateDeltaZ(double[] currentPosition) {
        for (Face3D face : DataSaved.filteredFaces) {
            if (isLayerEnabled(face.getLayer().getLayerName())) {
                // Estrai i vertici del triangolo dalla Face3D
                Point3D p1 = face.getP1();
                Point3D p2 = face.getP2();
                Point3D p3 = face.getP3();

                // Converti i Point3D in array di double per il calcolo
                double[] v0 = new double[]{p1.getX(), p1.getY(), p1.getZ()};
                double[] v1 = new double[]{p2.getX(), p2.getY(), p2.getZ()};
                double[] v2 = new double[]{p3.getX(), p3.getY(), p3.getZ()};

                // Controlla se il punto è nel triangolo in 2D (ignora Z)
                double[] projectedCurrentPosition = new double[]{currentPosition[0], currentPosition[1]};
                if (isPointInTriangle(projectedCurrentPosition,
                        new double[]{v0[0], v0[1]},
                        new double[]{v1[0], v1[1]},
                        new double[]{v2[0], v2[1]})) {
                    double currentZ = currentPosition[2];
                    double triangleZ = calculateTriangleHeight(currentPosition, v0, v1, v2);
                    return currentZ - triangleZ;
                }
            }
        }
        return Double.MIN_VALUE; // Se non viene trovato nessun triangolo
    }
    public double calculateZ(double[] currentPosition){
        for (Face3D face : DataSaved.filteredFaces) {
            if (isLayerEnabled(face.getLayer().getLayerName())) {
                // Estrai i vertici del triangolo dalla Face3D
                Point3D p1 = face.getP1();
                Point3D p2 = face.getP2();
                Point3D p3 = face.getP3();

                // Converti i Point3D in array di double per il calcolo
                double[] v0 = new double[]{p1.getX(), p1.getY(), p1.getZ()};
                double[] v1 = new double[]{p2.getX(), p2.getY(), p2.getZ()};
                double[] v2 = new double[]{p3.getX(), p3.getY(), p3.getZ()};

                // Controlla se il punto è nel triangolo in 2D (ignora Z)
                double[] projectedCurrentPosition = new double[]{currentPosition[0], currentPosition[1]};
                if (isPointInTriangle(projectedCurrentPosition,
                        new double[]{v0[0], v0[1]},
                        new double[]{v1[0], v1[1]},
                        new double[]{v2[0], v2[1]})) {

                    return calculateTriangleHeight(currentPosition, v0, v1, v2);

                }
            }
        }
        return Double.MIN_VALUE;
    }


    private boolean isPointInTriangle(double[] p, double[] v0, double[] v1, double[] v2) {
        double dX = p[0] - v2[0];
        double dY = p[1] - v2[1];
        double dX21 = v2[0] - v1[0];
        double dY12 = v1[1] - v2[1];
        double D = dY12 * (v0[0] - v2[0]) + dX21 * (v0[1] - v2[1]);
        double s = dY12 * dX + dX21 * dY;
        double t = (v2[1] - v0[1]) * dX + (v0[0] - v2[0]) * dY;
        if (D < 0) return s <= 0 && t <= 0 && s + t >= D;
        return s >= 0 && t >= 0 && s + t <= D;
    }

    private double calculateTriangleHeight(double[] p, double[] v0, double[] v1, double[] v2) {
        double[] normal = calculateNormal(v0, v1, v2);
        double A = normal[0];
        double B = normal[1];
        double C = normal[2];
        double D = -(A * v0[0] + B * v0[1] + C * v0[2]);

        return -(A * p[0] + B * p[1] + D) / C;
    }

    private double[] calculateNormal(double[] v0, double[] v1, double[] v2) {
        double[] normal = new double[3];
        double[] u = new double[]{v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2]};
        double[] v = new double[]{v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2]};
        normal[0] = u[1] * v[2] - u[2] * v[1];
        normal[1] = u[2] * v[0] - u[0] * v[2];
        normal[2] = u[0] * v[1] - u[1] * v[0];
        return normal;
    }

    private double distance(double[] p1, double[] p2) {
        return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
    }

    private double distance2d(double[] p1, Point3D_Drill p2) {
        return Math.sqrt(Math.pow(p1[0] - p2.getHeadX(), 2) + Math.pow(p1[1] - p2.getHeadY(), 2));
    }

    private double distance2d(double[] p1, Point3D p2) {
        return Math.sqrt(Math.pow(p1[0] - p2.getX(), 2) + Math.pow(p1[1] - p2.getY(), 2));
    }
    private double distance2d(double[] p1, DxfText p2) {
        return Math.sqrt(Math.pow(p1[0] - p2.getX(), 2) + Math.pow(p1[1] - p2.getY(), 2));
    }



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

    private void copiaFacce(){
        for (Face3D face : DataSaved.filteredFaces) {
            // Copia i 3 o 4 punti della faccia, azzerando la Z
            Point3D p1 = new Point3D(face.getP1().getX(), face.getP1().getY(), 0);
            Point3D p2 = new Point3D(face.getP2().getX(), face.getP2().getY(), 0);
            Point3D p3 = new Point3D(face.getP3().getX(), face.getP3().getY(), 0);
            Point3D p4 = face.getP4(); // può essere uguale a p3 (triangolo) o diverso (quadrilatero)
            Point3D p4New = p4.equals(face.getP3()) ? p3 : new Point3D(p4.getX(), p4.getY(), 0);

            // Crea nuova Face3D con layer e colore uguali
            Face3D face2D = new Face3D(p1, p2, p3, p4New, face.getColor(), face.getLayer());
            face2D.setLayer(face.getLayer());

            DataSaved.filteredFacesGL_2D.add(face2D);
        }
    }
    private void copiaPoly(){
        for (Polyline poly : DataSaved.filteredPolylines) {
            List<Point3D> newVertices = new ArrayList<>();
            for (Point3D pt : poly.getVertices()) {
                newVertices.add(new Point3D(pt.getX(), pt.getY(), 0)); // Z azzerata
            }

            Polyline poly2D = new Polyline(newVertices, poly.getLayer());
            poly2D.setLineColor(poly.getLineColor());

            DataSaved.filteredPolylinesGL_2D.add(poly2D);
        }
    }






}