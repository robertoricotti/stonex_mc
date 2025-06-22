package com.example.mylibrary.positioning;

import java.util.ArrayList;

public class My_Polygon {

    public static boolean isInside(double x, double y, ArrayList<Double> polygonX, ArrayList<Double> polygonY) {
        int n = polygonX.size();
        boolean isInside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = polygonX.get(i), yi = polygonY.get(i);
            double xj = polygonX.get(j), yj = polygonY.get(j);

            boolean intersect = ((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) isInside = !isInside;
        }
        return isInside;
    }
    public static double area(ArrayList<Double> polygonX, ArrayList<Double> polygonY) {
        int n = polygonX.size();
        double area = 0.0;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            area += (polygonX.get(j) + polygonX.get(i)) * (polygonY.get(j) - polygonY.get(i));
        }
        return Math.abs(area) / 2.0;
    }
    public static double volume(ArrayList<Double> polygonX, ArrayList<Double> polygonY, ArrayList<Double> polygonZ) {
        int n = polygonX.size();
        double volume = 0.0;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = polygonX.get(i), yi = polygonY.get(i), zi = polygonZ.get(i);
            double xj = polygonX.get(j), yj = polygonY.get(j), zj = polygonZ.get(j);

            // Calcola l'altezza rispetto al piano xy
            double height = Math.max(zi, zj) - Math.min(zi, zj);

            // Calcola l'area del triangolo proiettato sul piano xy
            double triangleArea = Math.abs((xi * yj + xj * zi + yi * zj - xj * yi - xi * zj - xj * zi) / 2.0);

            // Aggiungi il volume del prisma a base triangolare
            volume += triangleArea * height;
        }
        return volume;
    }
}

