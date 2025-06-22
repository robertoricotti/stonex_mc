package gui.my_opengl;

public class Polyline2DArc {

    public static class SegmentData {
        public final double angleDegrees;
        public final double length;

        public SegmentData(double angleDegrees, double length) {
            this.angleDegrees = angleDegrees;
            this.length = length;
        }
    }

    public static SegmentData[] calculateArcSegments(double distance, double arcHeight) {
        int numSegments = 5;
        double dx = distance / numSegments;

        // Costruzione dei punti dell'arco
        double[][] points = new double[numSegments + 1][2];
        for (int i = 0; i <= numSegments; i++) {
            double x = i * dx;
            double t = x - distance / 2.0;
            double y = -4 * arcHeight / (distance * distance) * t * t + arcHeight;
            points[i][0] = x;
            points[i][1] = y;
        }

        // Calcolo degli angoli e delle lunghezze dei segmenti
        SegmentData[] segments = new SegmentData[numSegments];
        for (int i = 0; i < numSegments; i++) {
            double dxSeg = points[i + 1][0] - points[i][0];
            double dySeg = points[i + 1][1] - points[i][1];
            double angle = Math.toDegrees(Math.atan2(dySeg, dxSeg));
            double length = Math.hypot(dxSeg, dySeg);
            segments[i] = new SegmentData(angle, length);
        }

        return segments;
    }

    public static SegmentData[] calculateArcSegmentsInv(double distance, double arcHeight) {
        arcHeight=-arcHeight;
        int numSegments = 5;
        double dx = distance / numSegments;

        // Costruzione dei punti dell'arco
        double[][] points = new double[numSegments + 1][2];
        for (int i = 0; i <= numSegments; i++) {
            double x = i * dx;
            double t = x - distance / 2.0;
            double y = -4 * arcHeight / (distance * distance) * t * t + arcHeight;
            points[i][0] = x;
            points[i][1] = y;
        }

        // Calcolo degli angoli e delle lunghezze dei segmenti
        SegmentData[] segments = new SegmentData[numSegments];
        for (int i = 0; i < numSegments; i++) {
            double dxSeg = points[i + 1][0] - points[i][0];
            double dySeg = points[i + 1][1] - points[i][1];
            double angle = Math.toDegrees(Math.atan2(dySeg, dxSeg));
            double length = Math.hypot(dxSeg, dySeg);
            segments[i] = new SegmentData(angle, length);
        }

        return segments;
    }
}


