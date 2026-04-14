package packexcalib.mymatrix;

public class EasyPointCalculator {
    private final RotationMatrix rotationMatrix;
    private final double[] startPoint;

    public EasyPointCalculator(double[] startPoint) {
        this.rotationMatrix = new RotationMatrix();
        this.startPoint = startPoint;
    }

    public double[] calculateEndPoint(double slope, double heading, double deltaY) {
        double mheading = 360 - heading;
        double[][] rotationMatrix = this.rotationMatrix.getRotationMatrix(slope, -0, mheading);
        double[][] vector = new double[][]{{0}, {deltaY}, {0}};

        // Applica la moltiplicazione della matrice di rotazione al vettore
        double[][] result = multiplyMatrix(rotationMatrix, vector);

        // Calcola le coordinate dell'estremità
        double[] endPoint = new double[3];
        for (int i = 0; i < 3; i++) {
            endPoint[i] = startPoint[i] + result[i][0];
        }

        // Applica il roll
        double[] rolledEndPoint = applyRoll(endPoint, -0);

        return rolledEndPoint;
    }

    private double[][] multiplyMatrix(double[][] matrix, double[][] vector) {
        int rowsA = matrix.length;
        int colsA = matrix[0].length;
        int colsB = vector[0].length;

        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                double sum = 0.0;
                for (int k = 0; k < colsA; k++) {
                    sum += matrix[i][k] * vector[k][j];
                }
                result[i][j] = sum;
            }
        }

        return result;
    }

    private double[] applyRoll(double[] coordinates, double roll) {
        double[] rolledCoordinates = new double[3];
        double cosRoll = Math.cos(Math.toRadians(roll));
        double sinRoll = Math.sin(Math.toRadians(roll));

        rolledCoordinates[0] = cosRoll * coordinates[0] - sinRoll * coordinates[1]; // X
        rolledCoordinates[1] = sinRoll * coordinates[0] + cosRoll * coordinates[1]; // Y
        rolledCoordinates[2] = coordinates[2]; // Z

        return rolledCoordinates;
    }


}





