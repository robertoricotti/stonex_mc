package gui.projects;


import java.util.ArrayList;
import java.util.List;

import dxf.Face3D;
import dxf.Layer;
import dxf.Point3D;
import dxf.Polyline;

public class MyRoad_Model {

    public static List<Face3D> facceTrench = new ArrayList<>();
    public static Polyline polyTrench = new Polyline();

    // Modalità profilo verticale
    public enum VerticalMode {NONE, LINEAR, PARABOLIC}

    /**
     * Modello stradale avanzato, con risampling, superelevazione e livelletta.
     *
     * @param centerLine            asse strada (almeno 2 punti)
     * @param sampleStep            passo di risampling in unità del progetto (es. 1.0)
     * @param leftWidth             larghezza carreggiata sinistra
     * @param rightWidth            larghezza carreggiata destra
     * @param leftSlopeDeg          pendenza laterale sinistra in GRADI (es. -2 -> -2%)
     * @param rightSlopeDeg         pendenza laterale destra in GRADI
     * @param shoulderLeftWidth     (opz.) larghezza banchina/scarpata sinistra (0 = disabilitata)
     * @param shoulderRightWidth    (opz.) larghezza banchina/scarpata destra
     * @param shoulderLeftSlopeDeg  pendenza banchina/scarpata SX in GRADI
     * @param shoulderRightSlopeDeg pendenza banchina/scarpata DX in GRADI
     * @param rollAngleDeg          angolo di superelevazione (rotazione trasversale) in GRADI.
     *                              Positivo = lato destro più alto. 0 = disabilitato.
     * @param rollInOutLength       lunghezza di transizione (m) per portare la superelevazione
     *                              da 0 a rollAngleDeg all’inizio e viceversa alla fine.
     * @param verticalMode          NONE / LINEAR / PARABOLIC
     * @param iStartPercent         pendenza iniziale (%) per PARABOLIC (es. +1.5 = 1.5%). Ignorato se NON PARABOLIC.
     * @param iEndPercent           pendenza finale (%) per PARABOLIC. Ignorato se NON PARABOLIC.
     * @param trenchColor           colore facce
     * @param centerLineColor       colore polilinea asse
     * @param trenchLayer           layer facce
     * @param polylineLayer         layer asse
     */
    public static void buildRoadModelAdvanced(
            List<Point3D> centerLine,
            double sampleStep,
            double leftWidth, double rightWidth,
            double leftSlopeDeg, double rightSlopeDeg,
            double shoulderLeftWidth, double shoulderRightWidth,
            double shoulderLeftSlopeDeg, double shoulderRightSlopeDeg,
            double rollAngleDeg, double rollInOutLength,
            VerticalMode verticalMode, double iStartPercent, double iEndPercent,
            int trenchColor, int centerLineColor, Layer trenchLayer, Layer polylineLayer
    ) {
        facceTrench.clear();
        polyTrench = new Polyline(new ArrayList<>(), polylineLayer);
        polyTrench.setLineColor(centerLineColor);
        if (centerLine == null || centerLine.size() < 2) return;

        // 1) RICAMPIONA LA CENTERLINE A PASSO COSTANTE (più smooth e stabile)
        List<Point3D> cl = resampleCenterLine(centerLine, sampleStep);

        // 2) PROFILO VERTICALE: NONE / LINEAR / PARABOLIC
        applyVerticalProfile(cl, verticalMode, iStartPercent, iEndPercent);

        // 3) NORMALI SMUSSATE (bisettrici)
        List<Point3D> normals = computeSmoothedNormals(cl);

        // 4) PARAMETRI TRASVERSALI
        final double sLeft = Math.tan(Math.toRadians(leftSlopeDeg));
        final double sRight = Math.tan(Math.toRadians(rightSlopeDeg));
        final double shLeft = Math.tan(Math.toRadians(shoulderLeftSlopeDeg));
        final double shRight = Math.tan(Math.toRadians(shoulderRightSlopeDeg));
        final double rollRad = Math.toRadians(rollAngleDeg);

        // 5) SUPERELEVATION RAMP: fattore [0..1] lungo l’asse
        double totalLen = polylineLength2D(cl);
        List<Double> rollFactor = new ArrayList<>(cl.size());
        for (int i = 0; i < cl.size(); i++) {
            double s = chainage2D(cl, i);
            double fIn = rollInOutLength > 0 ? clamp01(s / rollInOutLength) : 1.0;
            double fOut = rollInOutLength > 0 ? clamp01((totalLen - s) / rollInOutLength) : 1.0;
            rollFactor.add(Math.min(fIn, fOut)); // rampa all'inizio e alla fine
        }

        // 6) COSTRUISCI SEZIONI (carreggiata + banchine opzionali)
        List<Point3D> L = new ArrayList<>(); // bordo carreggiata SX
        List<Point3D> R = new ArrayList<>(); // bordo carreggiata DX
        List<Point3D> SL = new ArrayList<>(); // fine banchina/scarpata SX (se width>0)
        List<Point3D> SR = new ArrayList<>(); // fine banchina/scarpata DX

        for (int i = 0; i < cl.size(); i++) {
            Point3D c = cl.get(i);
            Point3D n = normals.get(i);

            // superelevazione progressiva (rotazione trasversale)
            double roll = rollRad * rollFactor.get(i);

            // z-offset = (pendenzaLaterale + tan(roll)) * offsetLateral
            double zOffL = (sLeft + Math.tan(roll)) * leftWidth;
            double zOffR = (sRight + Math.tan(-roll)) * rightWidth;

            Point3D left = new Point3D(
                    c.getX() - n.getX() * leftWidth,
                    c.getY() - n.getY() * leftWidth,
                    c.getZ() + zOffL,
                    trenchColor, trenchLayer
            );
            Point3D right = new Point3D(
                    c.getX() + n.getX() * rightWidth,
                    c.getY() + n.getY() * rightWidth,
                    c.getZ() + zOffR,
                    trenchColor, trenchLayer
            );
            L.add(left);
            R.add(right);

            // BANCHINE / SCARPATE (opzionali)
            if (shoulderLeftWidth > 1e-9) {
                double zOffSL = (shLeft + Math.tan(roll)) * (leftWidth + shoulderLeftWidth);
                Point3D sl = new Point3D(
                        c.getX() - n.getX() * (leftWidth + shoulderLeftWidth),
                        c.getY() - n.getY() * (leftWidth + shoulderLeftWidth),
                        c.getZ() + zOffSL,
                        trenchColor, trenchLayer
                );
                SL.add(sl);
            }
            if (shoulderRightWidth > 1e-9) {
                double zOffSR = (shRight + Math.tan(-roll)) * (rightWidth + shoulderRightWidth);
                Point3D sr = new Point3D(
                        c.getX() + n.getX() * (rightWidth + shoulderRightWidth),
                        c.getY() + n.getY() * (rightWidth + shoulderRightWidth),
                        c.getZ() + zOffSR,
                        trenchColor, trenchLayer
                );
                SR.add(sr);
            }
        }

        // 7) TRIANGOLI CARREGGIATA
        for (int i = 0; i < cl.size() - 1; i++) {
            facceTrench.add(new Face3D(L.get(i), R.get(i), L.get(i + 1), L.get(i + 1), trenchColor, trenchLayer));
            facceTrench.add(new Face3D(R.get(i), R.get(i + 1), L.get(i + 1), L.get(i + 1), trenchColor, trenchLayer));
        }

        // 8) TRIANGOLI BANCHINE (se presenti)
        if (!SL.isEmpty()) {
            for (int i = 0; i < SL.size() - 1; i++) {
                facceTrench.add(new Face3D(SL.get(i), L.get(i), SL.get(i + 1), SL.get(i + 1), trenchColor, trenchLayer));
                facceTrench.add(new Face3D(L.get(i), L.get(i + 1), SL.get(i + 1), SL.get(i + 1), trenchColor, trenchLayer));
            }
        }
        if (!SR.isEmpty()) {
            for (int i = 0; i < SR.size() - 1; i++) {
                facceTrench.add(new Face3D(R.get(i), SR.get(i), R.get(i + 1), R.get(i + 1), trenchColor, trenchLayer));
                facceTrench.add(new Face3D(SR.get(i), SR.get(i + 1), R.get(i + 1), R.get(i + 1), trenchColor, trenchLayer));
            }
        }

        // 9) POLYLINE DELL’ASSE (già riscampionata → regolare e liscia)
        polyTrench.getVertices().addAll(cl);
    }

    // -------------------- UTILITIES --------------------

    private static List<Point3D> resampleCenterLine(List<Point3D> cl, double step) {
        if (step <= 0) return new ArrayList<>(cl);
        List<Point3D> out = new ArrayList<>();
        out.add(new Point3D(cl.get(0).getX(), cl.get(0).getY(), cl.get(0).getZ()));

        double acc = 0.0, nextS = step;
        for (int i = 0; i < cl.size() - 1; i++) {
            Point3D a = cl.get(i), b = cl.get(i + 1);
            double seg = distance2D(a, b);
            while (acc + seg >= nextS) {
                double t = (nextS - acc) / seg;
                out.add(lerp(a, b, t));
                nextS += step;
            }
            acc += seg;
        }
        // Assicura ultimo punto
        Point3D last = cl.get(cl.size() - 1);
        if (!equals2D(out.get(out.size() - 1), last)) {
            out.add(new Point3D(last.getX(), last.getY(), last.getZ()));
        }
        return out;
    }

    private static void applyVerticalProfile(List<Point3D> cl, VerticalMode mode, double iStartPercent, double iEndPercent) {
        if (mode == VerticalMode.NONE || cl.size() < 2) return;

        double L = polylineLength2D(cl);
        double z0 = cl.get(0).getZ();
        double zN = cl.get(cl.size() - 1).getZ();

        if (mode == VerticalMode.LINEAR) {
            // pendenza costante tra primo e ultimo
            for (int i = 0; i < cl.size(); i++) {
                double s = chainage2D(cl, i);
                double z = z0 + (zN - z0) * (s / L);
                cl.get(i).setZ(z);
            }
        } else if (mode == VerticalMode.PARABOLIC) {
            // Z(s) = Z0 + i0*s + ((i1 - i0)/(2L)) * s^2  ; i in frazione (es. 1.5% -> 0.015)
            double i0 = iStartPercent / 100.0;
            double i1 = iEndPercent / 100.0;
            for (int i = 0; i < cl.size(); i++) {
                double s = chainage2D(cl, i);
                double z = z0 + i0 * s + ((i1 - i0) / (2.0 * L)) * s * s;
                cl.get(i).setZ(z);
            }
        }
    }

    private static List<Point3D> computeSmoothedNormals(List<Point3D> cl) {
        List<Point3D> normals = new ArrayList<>(cl.size());
        for (int i = 0; i < cl.size(); i++) {
            Point3D dPrev, dNext;
            if (i == 0) {
                dPrev = dir(cl.get(0), cl.get(1));
                dNext = dPrev;
            } else if (i == cl.size() - 1) {
                dPrev = dir(cl.get(i - 1), cl.get(i));
                dNext = dPrev;
            } else {
                dPrev = dir(cl.get(i - 1), cl.get(i));
                dNext = dir(cl.get(i), cl.get(i + 1));
            }
            double tx = dPrev.getX() + dNext.getX();
            double ty = dPrev.getY() + dNext.getY();
            double len = Math.sqrt(tx * tx + ty * ty);
            if (len < 1e-9) { // cuspide → usa dPrev
                tx = dPrev.getX();
                ty = dPrev.getY();
                len = Math.sqrt(tx * tx + ty * ty);
            }
            tx /= len;
            ty /= len;
            normals.add(new Point3D(-ty, tx, 0));
        }
        return normals;
    }

    private static Point3D lerp(Point3D a, Point3D b, double t) {
        return new Point3D(
                a.getX() + t * (b.getX() - a.getX()),
                a.getY() + t * (b.getY() - a.getY()),
                a.getZ() + t * (b.getZ() - a.getZ())
        );
    }

    private static double polylineLength2D(List<Point3D> pts) {
        double sum = 0;
        for (int i = 0; i < pts.size() - 1; i++) sum += distance2D(pts.get(i), pts.get(i + 1));
        return sum;
    }

    private static double chainage2D(List<Point3D> pts, int i) {
        double s = 0;
        for (int k = 0; k < i; k++) s += distance2D(pts.get(k), pts.get(k + 1));
        return s;
    }

    private static Point3D dir(Point3D a, Point3D b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-9) len = 1;
        return new Point3D(dx / len, dy / len, 0);
    }

    private static double distance2D(Point3D a, Point3D b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static boolean equals2D(Point3D a, Point3D b) {
        return Math.abs(a.getX() - b.getX()) < 1e-9 && Math.abs(a.getY() - b.getY()) < 1e-9;
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
