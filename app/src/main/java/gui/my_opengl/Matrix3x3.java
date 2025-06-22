package gui.my_opengl;

public class Matrix3x3 {
    private final float[][] m;

    // Costruttore: da tre vettori colonna
    public Matrix3x3(Vector3D col1, Vector3D col2, Vector3D col3) {
        m = new float[3][3];
        m[0][0] = col1.x; m[0][1] = col2.x; m[0][2] = col3.x;
        m[1][0] = col1.y; m[1][1] = col2.y; m[1][2] = col3.y;
        m[2][0] = col1.z; m[2][1] = col2.z; m[2][2] = col3.z;
    }

    // Costruttore: da matrice 3x3 esplicita
    public Matrix3x3(float[][] values) {
        if (values.length != 3 || values[0].length != 3)
            throw new IllegalArgumentException("Matrix must be 3x3");
        m = values;
    }

    // Moltiplicazione vettore (Vector3D)
    public Vector3D multiply(Vector3D v) {
        return new Vector3D(
                m[0][0]*v.x + m[0][1]*v.y + m[0][2]*v.z,
                m[1][0]*v.x + m[1][1]*v.y + m[1][2]*v.z,
                m[2][0]*v.x + m[2][1]*v.y + m[2][2]*v.z
        );
    }

    // Moltiplicazione punto (Point3DF)
    public Point3DF multiply(Point3DF p) {
        return new Point3DF(
                m[0][0]*p.x + m[0][1]*p.y + m[0][2]*p.z,
                m[1][0]*p.x + m[1][1]*p.y + m[1][2]*p.z,
                m[2][0]*p.x + m[2][1]*p.y + m[2][2]*p.z
        );
    }

    // Inversa della matrice 3x3
    public Matrix3x3 invert() {
        float a = m[0][0], b = m[0][1], c = m[0][2];
        float d = m[1][0], e = m[1][1], f = m[1][2];
        float g = m[2][0], h = m[2][1], i = m[2][2];

        float det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
        if (Math.abs(det) < 1e-8)
            throw new ArithmeticException("Matrix is singular and cannot be inverted");

        float invDet = 1.0f / det;
        float[][] inv = new float[3][3];

        inv[0][0] = (e * i - f * h) * invDet;
        inv[0][1] = (c * h - b * i) * invDet;
        inv[0][2] = (b * f - c * e) * invDet;

        inv[1][0] = (f * g - d * i) * invDet;
        inv[1][1] = (a * i - c * g) * invDet;
        inv[1][2] = (c * d - a * f) * invDet;

        inv[2][0] = (d * h - e * g) * invDet;
        inv[2][1] = (b * g - a * h) * invDet;
        inv[2][2] = (a * e - b * d) * invDet;

        return new Matrix3x3(inv);
    }
}
