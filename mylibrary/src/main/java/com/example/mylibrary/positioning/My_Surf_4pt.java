package com.example.mylibrary.positioning;


import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;




public class My_Surf_4pt {

    public double A;
    public double B;
    public double C;
    public double D;

    public double mdetMc, mdetMd, mdetMa, mdetMb;
    public double[] XArray = new double[4];
    public double[] YArray = new double[4];

    public double[] ZArray = new double[4];
    double x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, offsetH;
    double projectOrientation;

    public My_Surf_4pt(double[] points) {
        x1 = points[0];
        y1 = points[1];
        z1 = points[2];
        x2 = points[3];
        y2 = points[4];
        z2 = points[5];
        x3 = points[6];
        y3 = points[7];
        z3 = points[8];
        x4 = points[9];
        y4 = points[10];
        z4 = points[11];
        offsetH = points[12];

        /*

        2-------------------------3
        |                         |
        |                         |
        |                         |
        1-------------------------4
         */

        calculateSurfaceEquation(x1, y1, z1 + offsetH, x2, y2, z2 + offsetH, x3, y3, z3 + offsetH, x4, y4, z4 + offsetH);

        try {
            XArray[0] = x1;
            XArray[1] = x2;
            XArray[2] = x3;
            XArray[3] = x4;
            YArray[0] = y1;
            YArray[1] = y2;
            YArray[2] = y3;
            YArray[3] = y4;
            ZArray[0] = z1 + offsetH;
            ZArray[1] = z2 + offsetH;
            ZArray[2] = z3 + offsetH;
            ZArray[3] = z4 + offsetH;

        } catch (Exception e) {
            XArray = new double[4];
            YArray = new double[4];
            ZArray = new double[4];
        }

    }

    public My_Surf_4pt(Coordinate[] points) {
        x1 = points[0].x;
        y1 = points[0].y;
        z1 = points[0].z;
        x2 = points[1].x;
        y2 = points[1].y;
        z2 = points[1].z;
        x3 = points[2].x;
        y3 = points[2].y;
        z3 = points[2].z;
        x4 = points[3].x;
        y4 = points[3].y;
        z4 = points[3].z;
        offsetH = 0;

        /*

        2-------------------------3
        |                         |
        |                         |
        |                         |
        1-------------------------4
         */

        calculateSurfaceEquation(x1, y1, z1 + offsetH, x2, y2, z2 + offsetH, x3, y3, z3 + offsetH, x4, y4, z4 + offsetH);

        try {
            XArray[0] = x1;
            XArray[1] = x2;
            XArray[2] = x3;
            XArray[3] = x4;
            YArray[0] = y1;
            YArray[1] = y2;
            YArray[2] = y3;
            YArray[3] = y4;
            ZArray[0] = z1 + offsetH;
            ZArray[1] = z2 + offsetH;
            ZArray[2] = z3 + offsetH;
            ZArray[3] = z4 + offsetH;

        } catch (Exception e) {
            XArray = new double[4];
            YArray = new double[4];
            ZArray = new double[4];
        }

    }

    // Calcola l'equazione del piano a partire dai 4 punti
    private void calculateSurfaceEquation(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4) {
        double[][] M = {
                {x1, y1, z1, 1},
                {x2, y2, z2, 1},
                {x3, y3, z3, 1},
                {x4, y4, z4, 1}
        };
        double detM = My_Matrix_3D.determinant(M);
        double[][] Ma = {
                {y1, z1, 1},
                {y2, z2, 1},
                {y3, z3, 1},
                {y4, z4, 1}
        };
        double[][] Mb = {
                {x1, z1, 1},
                {x2, z2, 1},
                {x3, z3, 1},
                {x4, z4, 1}
        };
        double[][] Mc = {
                {x1, y1, 1},
                {x2, y2, 1},
                {x3, y3, 1},
                {x4, y4, 1}
        };
        double[][] Md = {
                {x1, y1, z1},
                {x2, y2, z2},
                {x3, y3, z3},
                {x4, y4, z4}
        };
        double detMa = My_Matrix_3D.determinant(Ma);
        double detMb = My_Matrix_3D.determinant(Mb);
        double detMc = My_Matrix_3D.determinant(Mc);
        double detMd = My_Matrix_3D.determinant(Md);

        this.A = detMa / detM;
        this.B = -detMb / detM;
        this.C = detMc / detM;
        this.D = -detMd / detM;
        mdetMa = detMa;
        mdetMb = detMb;
        mdetMc = detMc;
        mdetMd = detMd;


    }

    public boolean isSurfOk() {
        if (mdetMc == 0.0 && mdetMd == 0.0 && mdetMb == 0.0 && mdetMa == 0.0) {
            return false;
        } else {
            return true;
        }
    }

    // Restituisce la quota in un punto qualsiasi della superficie
    public double getAltitude(double currentX, double currentY) {
        if (Double.isNaN(this.A)) this.A = 0;
        if (Double.isNaN(this.B)) this.B = 0;
        if (Double.isNaN(this.C)) this.C = 0;
        if (Double.isNaN(this.D)) this.D = 0;

        return (-this.A * currentX - this.B * currentY - this.D) / this.C;
    }

    // Restituisce la differenza di quota tra il punto corrente e la superficie
    public double getAltitudeDifference(double currentX, double currentY, double currentZ) {
        double surfaceZ = getAltitude(currentX, currentY);

        return currentZ - surfaceZ;

    }



    public boolean isPointInside(double currentX, double currentY) {
        // calcola i vettori dai punti alle coordinate attuali
        double[] v1 = {currentX - XArray[0], currentY - YArray[0]};
        double[] v2 = {currentX - XArray[1], currentY - YArray[1]};
        double[] v3 = {currentX - XArray[2], currentY - YArray[2]};
        double[] v4 = {currentX - XArray[3], currentY - YArray[3]};
        // calcola i prodotti vettoriali tra i vettori consecutivi
        double cross1 = crossProduct(v1, v2);
        double cross2 = crossProduct(v2, v3);
        double cross3 = crossProduct(v3, v4);
        double cross4 = crossProduct(v4, v1);
        // controlla se i prodotti vettoriali hanno lo stesso segno
        return (cross1 >= 0 && cross2 >= 0 && cross3 >= 0 && cross4 >= 0) || (cross1 <= 0 && cross2 <= 0 && cross3 <= 0 && cross4 <= 0);
    }

    private double crossProduct(double[] v1, double[] v2) {
        return v1[0] * v2[1] - v1[1] * v2[0];
    }


    public double[] getSlopesXY(double currentX, double currentY, double heading) {
        projectOrientation = My_LocationCalc.calcBearingXY(x1, y1, x2, y2);
        double mHeading = (heading - projectOrientation) % 360;
        PointCalculator pointCalculator = new PointCalculator(new double[]{currentX, currentY, 0});
        double[] p1 = pointCalculator.calculateEndPoint(0, 0, mHeading, 0, 2.0, 0);
        double[] p2 = pointCalculator.calculateEndPoint(0, 0, mHeading, 0, -2.0, 0);
        double h1 = getAltitude(p1[0], p1[1]);
        double h2 = getAltitude(p2[0], p2[1]);
        double deltaZ = h1 - h2;
        double alphaRadians = Math.atan(deltaZ / 4.0);
        double alphaDegrees = alphaRadians * 180 / Math.PI;
        double heading1 = mHeading + 90;
        heading1 = heading1 % 360;
        double[] p1x = pointCalculator.calculateEndPoint(0, 0, heading1, 0, 2.0, 0);
        double[] p2x = pointCalculator.calculateEndPoint(0, 0, heading1, 0, -2.0, 0);
        double h1x = getAltitude(p1x[0], p1x[1]);
        double h2x = getAltitude(p2x[0], p2x[1]);
        double deltaZx = h1x - h2x;
        double alphaRadiansx = Math.atan(deltaZx / 4.0);
        double alphaDegreesx = alphaRadiansx * 180 / Math.PI;

        if (Double.isNaN(alphaDegrees)) {
            alphaDegrees = 0;
        }
        if (Double.isNaN(alphaDegreesx)) {
            alphaDegreesx = 0;
        }
        return new double[]{alphaDegreesx * -1, alphaDegrees};

    }




    public int[] getMinuMax_H_index() {
        int minIndex = 0;
        for (int i = 1; i < ZArray.length; i++) {
            if (ZArray[i] < ZArray[minIndex]) {
                minIndex = i;
            }
        }
        int maxIndex = 0;
        for (int i = 1; i < ZArray.length; i++) {
            if (ZArray[i] > ZArray[maxIndex]) {
                maxIndex = i;
            }
        }
        return new int[]{minIndex, maxIndex};
        //restituisce l'indice della quota +bassa e quello della quota più alta

    }

    public double[] getMinMax_Hvalue() {
        Arrays.sort(ZArray);
        return new double[]{ZArray[0], ZArray[ZArray.length - 1]};
    }


}
