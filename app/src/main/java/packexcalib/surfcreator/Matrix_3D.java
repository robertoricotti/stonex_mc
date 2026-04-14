package packexcalib.surfcreator;

public class Matrix_3D {

    private final double[][] matrix;

    public Matrix_3D(double[][] matrix) {
        this.matrix = matrix;
    }

    public static double determinant(double[][] matrix) {
        double det = 0;
        for (int i = 0; i < matrix[0].length; i++) {
            det += matrix[0][i] * (matrix[1][(i + 1) % 3] * matrix[2][(i + 2) % 3] - matrix[1][(i + 2) % 3] * matrix[2][(i + 1) % 3]);
        }
        return det;
    }

    public double determinant() {
        return determinant(matrix);
    }

    public Matrix_3D inverse() {
        double[][] inv = new double[3][3];
        double det = determinant();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                inv[i][j] = ((matrix[(j + 1) % 3][(i + 1) % 3] * matrix[(j + 2) % 3][(i + 2) % 3]) - (matrix[(j + 1) % 3][(i + 2) % 3] * matrix[(j + 2) % 3][(i + 1) % 3])) / det;
            }
        }
        return new Matrix_3D(inv);
    }

    public double[] multiply(double[] vector) {
        double[] result = new double[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }
}


