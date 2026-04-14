package dxf;


import static packexcalib.exca.ExcavatorLib.bucketLeftCoord;
import static packexcalib.exca.ExcavatorLib.bucketRightCoord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import packexcalib.exca.DataSaved;
import packexcalib.gnss.My_LocationCalc;
import packexcalib.mymatrix.EasyPointCalculator;

public class IntersectionFinder {

    public IntersectionFinder() {
    }

    public static Point2D[] Intersections(double[] currentPos, double HDT) {

        //double heading=(NmeaListener.mch_Orientation + DataSaved.deltaGPS2+ ExcavatorLib.yawSensor)+HDT;
        double tempHeading = My_LocationCalc.calcBearingXY(bucketLeftCoord[0], bucketLeftCoord[1], bucketRightCoord[0], bucketRightCoord[1]) + 270;
        double heading = tempHeading + HDT;
        heading = heading % 360;
        double[] myPos = new EasyPointCalculator(currentPos).calculateEndPoint(0, heading, 500);

        double x1 = currentPos[0];
        double y1 = currentPos[1];
        double x2 = myPos[0];
        double y2 = myPos[1];

        Point2D lineStart = new Point2D(x1, y1);
        Point2D lineEnd = new Point2D(x2, y2);

        List<Point2D> intersections = new ArrayList<>();
        List<Face3D> closestFaces = getClosestFaces(DataSaved.filteredFaces, currentPos, 800);

        for (Face3D face : closestFaces) {
            Point3D[] vertices = new Point3D[]{face.getP1(), face.getP2(), face.getP3(), face.getP4()};
            for (int i = 0; i < vertices.length; i++) {
                Point3D p0 = vertices[i];
                Point3D p1 = vertices[(i + 1) % vertices.length];

                if (p0 != null && p1 != null) {
                    Point2D intersection2D = lineIntersection(
                            lineStart, lineEnd,
                            new Point2D(p0.getX(), p0.getY()), new Point2D(p1.getX(), p1.getY())
                    );

                    if (intersection2D != null) {
                        double z = interpolateZ(
                                new double[]{p0.getX(), p0.getY(), p0.getZ()},
                                new double[]{p1.getX(), p1.getY(), p1.getZ()},
                                intersection2D
                        );
                        double horizontalDistance = calculateHorizontalDistance(currentPos, intersection2D);
                        double heightDifference = z - currentPos[2];
                        intersections.add(new Point2D(horizontalDistance, heightDifference));
                    }
                }
            }
        }


        // Rimuovere duplicati e ordinare
        intersections = removeDuplicatesAndSort(intersections);

        return intersections.toArray(new Point2D[0]);
    }

    public static Point2D lineIntersection(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
        double s1_x = p1.getX() - p0.getX();
        double s1_y = p1.getY() - p0.getY();
        double s2_x = p3.getX() - p2.getX();
        double s2_y = p3.getY() - p2.getY();

        double s = (-s1_y * (p0.getX() - p2.getX()) + s1_x * (p0.getY() - p2.getY())) / (-s2_x * s1_y + s1_x * s2_y);
        double t = (s2_x * (p0.getY() - p2.getY()) - s2_y * (p0.getX() - p2.getX())) / (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            double i_x = p0.getX() + (t * s1_x);
            double i_y = p0.getY() + (t * s1_y);
            return new Point2D(i_x, i_y);
        }
        return null;
    }

    private static double interpolateZ(double[] p0, double[] p1, Point2D intersection) {
        double dx = p1[0] - p0[0];
        double dy = p1[1] - p0[1];
        double dz = p1[2] - p0[2];
        double distance = Math.sqrt(dx * dx + dy * dy);
        double interpDistance = Math.sqrt(Math.pow(intersection.getX() - p0[0], 2) + Math.pow(intersection.getY() - p0[1], 2));
        return p0[2] + dz * (interpDistance / distance);
    }

    private static double calculateHorizontalDistance(double[] currentPos, Point2D intersection) {
        double dx = intersection.getX() - currentPos[0];
        double dy = intersection.getY() - currentPos[1];
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static List<Point2D> removeDuplicatesAndSort(List<Point2D> points) {
        // Usa un HashSet per rimuovere duplicati
        Set<Point2D> uniquePoints = new HashSet<>(points);

        // Converti di nuovo in una lista
        List<Point2D> uniquePointsList = new ArrayList<>(uniquePoints);

        // Ordina i punti per X
        uniquePointsList.sort(Comparator.comparingDouble(Point2D::getX));

        return uniquePointsList;
    }

    static List<Face3D> getClosestFaces(List<Face3D> faces, double[] currentPos, int limit) {
        // Crea una lista di coppie (distanza, faccia)
        List<Pair<Double, Face3D>> faceDistances = new ArrayList<>();

        for (Face3D face : faces) {
            // Usa il primo vertice della faccia come punto rappresentativo
            Point3D firstVertex = face.getP1(); // Assumendo che il primo vertice sia rappresentativo
            if (firstVertex != null) {
                double[] vertexCoords = {firstVertex.getX(), firstVertex.getY(), firstVertex.getZ()};
                double distance = calculateDistance(currentPos, vertexCoords); // Distanza dalla posizione corrente
                faceDistances.add(new Pair<>(distance, face));
            }
        }

        // Ordina le facce in base alla distanza
        faceDistances.sort(Comparator.comparingDouble(Pair::getKey));

        // Prendi solo le prime 'limit' facce
        List<Face3D> closestFaces = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, faceDistances.size()); i++) {
            closestFaces.add(faceDistances.get(i).getValue());
        }

        return closestFaces;
    }


    static double calculateDistance(double[] pos1, double[] pos2) {
        double dx = pos1[0] - pos2[0];
        double dy = pos1[1] - pos2[1];
        double dz = pos1[2] - pos2[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }


}
