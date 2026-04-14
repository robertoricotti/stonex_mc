package gui.my_opengl.dozer;

import static gui.my_opengl.Point3DF.pTransform;
import static packexcalib.exca.DataSaved.GL_LAMA;
import static packexcalib.exca.ExcavatorLib.correctPitch;
import static packexcalib.exca.ExcavatorLib.correctRoll;
import static packexcalib.exca.ExcavatorLib.hdt_LAMA;

import gui.my_opengl.MyGLRenderer;
import gui.my_opengl.Point3DF;
import packexcalib.exca.DataSaved;
import packexcalib.exca.Exca_Quaternion;

public class My_DozerFrame {

    private static final int BODY_OFFSET = 0;
    private static final int CAB_BASE_OFFSET = 8;
    private static final int CAB_ROOF_OFFSET = 16;
    private static final int TOTAL_POINTS = 24;

    private static Point3DF[] cachedPoints;
    private static RollerData[] cachedRollers;
    private static TrackData[] cachedTracks;

    private static float rs() {
        return MyGLRenderer.currentRenderScale();
    }

    public static Point3DF[] puntiDozer() {
        if (GL_LAMA == null || GL_LAMA.length < 30 || GL_LAMA[14] == null) {
            return new Point3DF[0];
        }

        final Point3DF attachCenter = GL_LAMA[14];

        final float wDozer = (float) (DataSaved.W_Blade_TOT * 0.8d * rs());
        final float hDozer = wDozer * 1.2f;
        final float trackLength = wDozer * 1.58f;

        final float trackWidth = wDozer * 0.25f;
        final float innerClear = wDozer - (2f * trackWidth);
        final float hTrack = hDozer * 0.30f;
        final float drumRadius = hTrack * 0.50f;
        final float drumWidth = trackWidth * 0.96f;
        final float shoeThickness = hTrack * 0.16f;

        // Il frame centrale deve scendere quanto la base cabina e toccare internamente i cingoli.
        final float bodyBottomZ = hTrack * 0.50f;
        final float bodyHeight = hDozer * 0.30f;
        final float bodyWidth = innerClear;
        final float bodyFrontX = wDozer * 0.18f;
        final float bodyRearX = trackLength * 0.74f;

        // Cabina a due stadi: base larga e tetto più stretto.
        final float cabBottomZ = bodyBottomZ;
        final float cabBaseHeight = hDozer * 0.26f;
        final float cabRoofHeight = hDozer - cabBottomZ - cabBaseHeight;
        final float cabBaseWidth = innerClear * 0.98f;
        final float cabTopWidth = cabBaseWidth * 0.68f;
        final float cabBaseLength = trackLength * 0.42f;
        final float cabRoofLength = cabBaseLength * 0.74f;
        final float cabRearX = trackLength * 0.98f;
        final float cabFrontX = cabRearX - cabBaseLength;
        final float cabRoofRearX = trackLength * 0.93f;
        final float cabRoofFrontX = cabRoofRearX - cabRoofLength;

        Point3DF longitudinal = buildLongitudinalAxis(attachCenter);
        Point3DF vertical = buildVerticalAxis(attachCenter, longitudinal);
        Point3DF lateral = safeNormalize(vertical.cross(longitudinal), new Point3DF(1f, 0f, 0f));

        Point3DF bladeLateral = safeNormalize(GL_LAMA[9].subtract(GL_LAMA[1]), new Point3DF(1f, 0f, 0f));
        if (lateral.dot(bladeLateral) < 0f) {
            lateral = lateral.scale(-1f);
        }

        final float bladeCutZ = averageVertical(
                attachCenter,
                vertical,
                GL_LAMA[24], GL_LAMA[25], GL_LAMA[26], GL_LAMA[27], GL_LAMA[28], GL_LAMA[29]
        );
        Point3DF origin = attachCenter.add(vertical.scale(bladeCutZ));

        Point3DF[] pts = new Point3DF[TOTAL_POINTS];

        fillBoxFromBottom(
                pts,
                BODY_OFFSET,
                move(origin, longitudinal, bodyFrontX, lateral, 0f, vertical, bodyBottomZ),
                move(origin, longitudinal, bodyRearX, lateral, 0f, vertical, bodyBottomZ),
                bodyWidth,
                bodyHeight,
                lateral,
                vertical
        );

        fillTaperedPrismFromBottom(
                pts,
                CAB_BASE_OFFSET,
                move(origin, longitudinal, cabFrontX, lateral, 0f, vertical, cabBottomZ),
                move(origin, longitudinal, cabRearX, lateral, 0f, vertical, cabBottomZ),
                cabBaseWidth,
                cabTopWidth,
                cabBaseHeight,
                lateral,
                vertical
        );

        fillBoxFromBottom(
                pts,
                CAB_ROOF_OFFSET,
                move(origin, longitudinal, cabRoofFrontX, lateral, 0f, vertical, cabBottomZ + cabBaseHeight),
                move(origin, longitudinal, cabRoofRearX, lateral, 0f, vertical, cabBottomZ + cabBaseHeight),
                cabTopWidth,
                cabRoofHeight,
                lateral,
                vertical
        );

        final float trackCenterY = (wDozer * 0.5f) - (trackWidth * 0.5f);
        final float frontDrumX = drumRadius;
        final float rearDrumX = trackLength - drumRadius;

        cachedTracks = new TrackData[]{
                buildTrack(origin, longitudinal, lateral, vertical, trackCenterY, frontDrumX, rearDrumX, drumWidth, drumRadius, shoeThickness),
                buildTrack(origin, longitudinal, lateral, vertical, -trackCenterY, frontDrumX, rearDrumX, drumWidth, drumRadius, shoeThickness)
        };

        cachedRollers = new RollerData[]{
                new RollerData(cachedTracks[0].frontA, cachedTracks[0].frontB, drumRadius),
                new RollerData(cachedTracks[0].rearA, cachedTracks[0].rearB, drumRadius),
                new RollerData(cachedTracks[1].frontA, cachedTracks[1].frontB, drumRadius),
                new RollerData(cachedTracks[1].rearA, cachedTracks[1].rearB, drumRadius)
        };

        cachedPoints = pts;
        return pts;
    }

    public static RollerData[] rollers() {
        if (cachedPoints == null) {
            puntiDozer();
        }
        return cachedRollers == null ? new RollerData[0] : cachedRollers;
    }

    public static TrackData[] tracks() {
        if (cachedPoints == null) {
            puntiDozer();
        }
        return cachedTracks == null ? new TrackData[0] : cachedTracks;
    }

    public static short[] dozerChiaro() {
        return concat(lightFaces(BODY_OFFSET), lightFaces(CAB_BASE_OFFSET), lightFaces(CAB_ROOF_OFFSET));
    }

    public static short[] dozerScuro() {
        return concat(darkFaces(BODY_OFFSET), darkFaces(CAB_BASE_OFFSET), darkFaces(CAB_ROOF_OFFSET));
    }

    public static short[] dozerContour() {
        return concat(boxEdges(BODY_OFFSET), boxEdges(CAB_BASE_OFFSET), boxEdges(CAB_ROOF_OFFSET));
    }

    private static TrackData buildTrack(Point3DF origin,
                                        Point3DF longitudinal,
                                        Point3DF lateral,
                                        Point3DF vertical,
                                        float y,
                                        float frontDrumX,
                                        float rearDrumX,
                                        float drumWidth,
                                        float drumRadius,
                                        float shoeThickness) {
        Point3DF frontCenter = move(origin, longitudinal, frontDrumX, lateral, y, vertical, drumRadius);
        Point3DF rearCenter = move(origin, longitudinal, rearDrumX, lateral, y, vertical, drumRadius);
        Point3DF aFront = frontCenter.add(lateral.scale(drumWidth * 0.5f));
        Point3DF bFront = frontCenter.subtract(lateral.scale(drumWidth * 0.5f));
        Point3DF aRear = rearCenter.add(lateral.scale(drumWidth * 0.5f));
        Point3DF bRear = rearCenter.subtract(lateral.scale(drumWidth * 0.5f));

        return new TrackData(
                frontCenter,
                rearCenter,
                aFront,
                bFront,
                aRear,
                bRear,
                drumRadius,
                drumWidth,
                shoeThickness,
                frontDrumX,
                rearDrumX,
                y,
                0f,
                (drumRadius * 2f) - shoeThickness
        );
    }

    private static void fillBoxFromBottom(Point3DF[] pts,
                                          int offset,
                                          Point3DF frontBottomCenter,
                                          Point3DF rearBottomCenter,
                                          float width,
                                          float height,
                                          Point3DF lateral,
                                          Point3DF vertical) {
        float halfW = width * 0.5f;
        Point3DF left = lateral.scale(halfW);
        Point3DF right = lateral.scale(-halfW);
        Point3DF up = vertical.scale(height);

        pts[offset] = frontBottomCenter.add(left);
        pts[offset + 1] = frontBottomCenter.add(right);
        pts[offset + 2] = frontBottomCenter.add(right).add(up);
        pts[offset + 3] = frontBottomCenter.add(left).add(up);

        pts[offset + 4] = rearBottomCenter.add(left);
        pts[offset + 5] = rearBottomCenter.add(right);
        pts[offset + 6] = rearBottomCenter.add(right).add(up);
        pts[offset + 7] = rearBottomCenter.add(left).add(up);
    }

    private static void fillTaperedPrismFromBottom(Point3DF[] pts,
                                                   int offset,
                                                   Point3DF frontBottomCenter,
                                                   Point3DF rearBottomCenter,
                                                   float bottomWidth,
                                                   float topWidth,
                                                   float height,
                                                   Point3DF lateral,
                                                   Point3DF vertical) {
        float halfBottom = bottomWidth * 0.5f;
        float halfTop = topWidth * 0.5f;
        Point3DF leftBottom = lateral.scale(halfBottom);
        Point3DF rightBottom = lateral.scale(-halfBottom);
        Point3DF leftTop = lateral.scale(halfTop);
        Point3DF rightTop = lateral.scale(-halfTop);
        Point3DF up = vertical.scale(height);

        pts[offset] = frontBottomCenter.add(leftBottom);
        pts[offset + 1] = frontBottomCenter.add(rightBottom);
        pts[offset + 2] = frontBottomCenter.add(rightTop).add(up);
        pts[offset + 3] = frontBottomCenter.add(leftTop).add(up);

        pts[offset + 4] = rearBottomCenter.add(leftBottom);
        pts[offset + 5] = rearBottomCenter.add(rightBottom);
        pts[offset + 6] = rearBottomCenter.add(rightTop).add(up);
        pts[offset + 7] = rearBottomCenter.add(leftTop).add(up);
    }

    private static Point3DF buildLongitudinalAxis(Point3DF attachCenter) {
        double[] a = toWorld(attachCenter);
        double[] b = Exca_Quaternion.endPoint(a, correctPitch + 180, correctRoll, 1d, hdt_LAMA);
        Point3DF end = pTransform(b, DataSaved.glL_AnchorView, rs());
        return safeNormalize(end.subtract(attachCenter), new Point3DF(0f, 1f, 0f));
    }

    private static Point3DF buildVerticalAxis(Point3DF attachCenter, Point3DF longitudinal) {
        double[] a = toWorld(attachCenter);
        double[] b = Exca_Quaternion.endPoint(a, correctPitch + 90, correctRoll, 1d, hdt_LAMA);
        Point3DF end = pTransform(b, DataSaved.glL_AnchorView, rs());
        Point3DF vertical = end.subtract(attachCenter);
        vertical = vertical.subtract(longitudinal.scale(longitudinal.dot(vertical)));
        return safeNormalize(vertical, new Point3DF(0f, 0f, 1f));
    }

    private static double[] toWorld(Point3DF p) {
        return new double[]{
                (p.getX() / rs()) + DataSaved.glL_AnchorView[0],
                (p.getY() / rs()) + DataSaved.glL_AnchorView[1],
                (p.getZ() / rs()) + DataSaved.glL_AnchorView[2]
        };
    }

    private static float averageVertical(Point3DF origin, Point3DF vertical, Point3DF... points) {
        float sum = 0f;
        int n = 0;
        for (Point3DF p : points) {
            if (p != null) {
                sum += p.subtract(origin).dot(vertical);
                n++;
            }
        }
        return n == 0 ? 0f : (sum / (float) n);
    }

    private static Point3DF move(Point3DF origin,
                                 Point3DF ax,
                                 float dx,
                                 Point3DF ay,
                                 float dy,
                                 Point3DF az,
                                 float dz) {
        return origin.add(ax.scale(dx)).add(ay.scale(dy)).add(az.scale(dz));
    }

    private static Point3DF safeNormalize(Point3DF p, Point3DF fallback) {
        if (p == null || p.length() < 0.0001f) {
            return fallback;
        }
        return p.normalize();
    }

    private static short[] lightFaces(int o) {
        return new short[]{
                (short) (o + 0), (short) (o + 1), (short) (o + 2),
                (short) (o + 0), (short) (o + 2), (short) (o + 3),
                (short) (o + 3), (short) (o + 2), (short) (o + 6),
                (short) (o + 3), (short) (o + 6), (short) (o + 7),
                (short) (o + 0), (short) (o + 3), (short) (o + 7),
                (short) (o + 0), (short) (o + 7), (short) (o + 4)
        };
    }

    private static short[] darkFaces(int o) {
        return new short[]{
                (short) (o + 1), (short) (o + 5), (short) (o + 6),
                (short) (o + 1), (short) (o + 6), (short) (o + 2),
                (short) (o + 4), (short) (o + 7), (short) (o + 6),
                (short) (o + 4), (short) (o + 6), (short) (o + 5),
                (short) (o + 0), (short) (o + 4), (short) (o + 5),
                (short) (o + 0), (short) (o + 5), (short) (o + 1),
                (short) (o + 0), (short) (o + 2), (short) (o + 6),
                (short) (o + 0), (short) (o + 6), (short) (o + 4)
        };
    }

    private static short[] boxEdges(int o) {
        return new short[]{
                (short) (o + 0), (short) (o + 1),
                (short) (o + 1), (short) (o + 2),
                (short) (o + 2), (short) (o + 3),
                (short) (o + 3), (short) (o + 0),
                (short) (o + 4), (short) (o + 5),
                (short) (o + 5), (short) (o + 6),
                (short) (o + 6), (short) (o + 7),
                (short) (o + 7), (short) (o + 4),
                (short) (o + 0), (short) (o + 4),
                (short) (o + 1), (short) (o + 5),
                (short) (o + 2), (short) (o + 6),
                (short) (o + 3), (short) (o + 7)
        };
    }

    private static short[] concat(short[]... arrays) {
        int len = 0;
        for (short[] a : arrays) {
            len += a.length;
        }
        short[] out = new short[len];
        int p = 0;
        for (short[] a : arrays) {
            System.arraycopy(a, 0, out, p, a.length);
            p += a.length;
        }
        return out;
    }

    public static class RollerData {
        public final Point3DF a;
        public final Point3DF b;
        public final float radius;

        public RollerData(Point3DF a, Point3DF b, float radius) {
            this.a = a;
            this.b = b;
            this.radius = radius;
        }
    }

    public static class TrackData {
        public final Point3DF frontCenter;
        public final Point3DF rearCenter;
        public final Point3DF frontA;
        public final Point3DF frontB;
        public final Point3DF rearA;
        public final Point3DF rearB;
        public final float drumRadius;
        public final float drumWidth;
        public final float shoeThickness;
        public final float frontX;
        public final float rearX;
        public final float y;
        public final float bottomZ;
        public final float topRunBottomZ;

        public TrackData(Point3DF frontCenter,
                         Point3DF rearCenter,
                         Point3DF frontA,
                         Point3DF frontB,
                         Point3DF rearA,
                         Point3DF rearB,
                         float drumRadius,
                         float drumWidth,
                         float shoeThickness,
                         float frontX,
                         float rearX,
                         float y,
                         float bottomZ,
                         float topRunBottomZ) {
            this.frontCenter = frontCenter;
            this.rearCenter = rearCenter;
            this.frontA = frontA;
            this.frontB = frontB;
            this.rearA = rearA;
            this.rearB = rearB;
            this.drumRadius = drumRadius;
            this.drumWidth = drumWidth;
            this.shoeThickness = shoeThickness;
            this.frontX = frontX;
            this.rearX = rearX;
            this.y = y;
            this.bottomZ = bottomZ;
            this.topRunBottomZ = topRunBottomZ;
        }
    }
}
