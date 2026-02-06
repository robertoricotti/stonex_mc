package services;

import static drill_pile.gui.Drill_Activity.isDrilling;
import static packexcalib.exca.DataSaved.Selected_Point3D_Drill;
import static packexcalib.exca.ExcavatorLib.coordTool;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static packexcalib.exca.ExcavatorLib.toolEndCoord;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import drill_pile.gui.DrillGuidance;
import drill_pile.gui.PlanError;
import iredes.DrillMatch;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.surfcreator.TriangleHelper;
import utils.DistToPoint;

/**
 * PointService "robusto" per guida:
 * - autosnap (1): seleziona punto più vicino (solo se non drilling)
 * - calcola okXY/okTilt/okOri con DrillMatch (XY su asse foro)
 * - calcola triangoli pitch/roll con DrillGuidance (robusto)
 * - calcola PlanError (bit->asse foro in XY) e aggiorna pe[] (compatibilità)
 * <p>
 * IMPORTANTI FIX rispetto al tuo:
 * 1) lastPosition = position.clone() (non reference)
 * 2) gestione null EndX/EndY/EndZ (niente autounboxing NPE silenziosi)
 * 3) PlanError.Result -> pe[] = {errE, errN, dist}
 * 4) sleep senza Math.abs e ciclo più stabile
 */
public class PointService extends Service {

    private static final String TAG = "PointService";

    private volatile boolean isRunning = false;
    private ExecutorService executor;

    private static double[] lastPosition;

    private TriangleHelper triangleHelper;

    // Debug/state (se ti serve loggare)
    private DrillMatch.MatchStates st;
    private DrillGuidance.Triangles tri;

    // OUTPUT pubblici (come li usi già nel resto app)
    public static double[] pe = new double[]{0, 0, 0}; // {errE, errN, dist}
    public static boolean okXY = false;
    public static boolean okTilt = false;
    public static boolean okOri = false;

    public static boolean FrecciaUP = false;
    public static boolean FrecciaRIGHT = false;
    public static boolean FrecciaDOWN = false;
    public static boolean FrecciaLEFT = false;

    public static double holePitchDeg, holeRollDeg;

    // Loop timing
    private static final long LOOP_MS = 100L;

    // --- START vs DRILL ---
    public static boolean okStart = false;   // OK per iniziare
    public static boolean okDrill = false;   // OK durante discesa (asse)
    public static boolean okZ = false;       // quota corretta in start

    public static double distXYToHead = Double.NaN; // bit->testa (XY)
    public static double dzToHead = Double.NaN;     // bitZ - headZ
    public static double dist3DToHead = Double.NaN; // opzionale
    public static double distAxis = Double.NaN;     // bit->asse (XY) (come pe[2] / st.distXY)

    public static double remainingZ = Double.NaN;        // per pali verticali
    public static double remainingAxis = Double.NaN;     // per pali inclinati



    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created");

        triangleHelper = new TriangleHelper();
        lastPosition = new double[]{0, 0, 0};

        // un singolo thread basta (eviti concorrenza)
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Started");

        if (!isRunning) {
            isRunning = true;
            executor.execute(pointRunnable);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPointLoop();
        Log.d(TAG, "Destroyed");
    }

    private void stopPointLoop() {
        isRunning = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private final Runnable pointRunnable = () -> {
        while (isRunning) {
            final long startTime = System.currentTimeMillis();

            try {
                // Aggiorna posizione “filtrata” (FIX: clone)
                updateCurrentPosition(ExcavatorLib.toolEndCoord, 1000);

                // --- autosnap / selezione punto ---
                switch (DataSaved.isAutoSnap) {
                    case 0:
                        // niente
                        break;

                    case 1:
                        // auto-selezione solo se non drilling
                        if (!isDrilling) {
                            if (DataSaved.filtered_drill_points != null && !DataSaved.filtered_drill_points.isEmpty()) {
                                Selected_Point3D_Drill = findNearestDrillPoint(
                                        toolEndCoord[0], toolEndCoord[1],
                                        DataSaved.filtered_drill_points
                                );
                            }
                        }
                        break;

                    case 2:
                        // selezione manuale (long press) -> qui non fare nulla
                        break;
                }

                // --- guida (ok + frecce + plan error) ---
                computeGuidance();

            } catch (Throwable t) {
                // non far morire il servizio
                Log.e(TAG, "Loop error", t);
            }

            // sleep stabile
            final long elapsed = System.currentTimeMillis() - startTime;
            final long sleep = LOOP_MS - elapsed;
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    };

    private void computeGuidance() {

        final Point3D_Drill sel = Selected_Point3D_Drill;

        // -------------------------
        // 0) No selection
        // -------------------------
        if (sel == null) {
            resetOutputs();
            return;
        }

        // -------------------------
        // 1) Validate HEAD (min required)
        // -------------------------
        final Double hxObj = sel.getHeadX();
        final Double hyObj = sel.getHeadY();
        final Double hzObj = sel.getHeadZ(); // può essere null: okStart richiede Z

        if (hxObj == null || hyObj == null) {
            resetOutputs();
            return;
        }

        // Mast arrays
        final double[] mastHead = coordTool;   // [E,N,Z]
        final double[] mastBit = toolEndCoord; // [E,N,Z]

        if (mastHead == null || mastHead.length < 3 || mastBit == null || mastBit.length < 3) {
            resetOutputs();
            return;
        }

        final double hx = hxObj;
        final double hy = hyObj;
        final double hz = (hzObj != null) ? hzObj : Double.NaN;

        // -------------------------
        // 2) End availability
        // -------------------------
        final Double exObj = sel.getEndX();
        final Double eyObj = sel.getEndY();
        final Double ezObj = sel.getEndZ();
        final boolean hasEnd = (exObj != null && eyObj != null && ezObj != null);

        final double[] holeStart = new double[]{hx, hy, Double.isNaN(hz) ? 0.0 : hz};
        final double[] holeEnd = hasEnd ? new double[]{exObj, eyObj, ezObj} : holeStart;

        // -------------------------
        // 3) Always compute "bit -> head" XY (+Z if possible)
        // -------------------------
        {
            double dxh = mastBit[0] - hx;
            double dyh = mastBit[1] - hy;
            distXYToHead = Math.sqrt(dxh * dxh + dyh * dyh);

            if (!Double.isNaN(hz) && !Double.isNaN(mastBit[2])) {
                dzToHead = mastBit[2] - hz;
                dist3DToHead = Math.sqrt(distXYToHead * distXYToHead + dzToHead * dzToHead);
                okZ = (Math.abs(dzToHead) <= DataSaved.Drill_tolleranza_Z);
            } else {
                dzToHead = Double.NaN;
                dist3DToHead = Double.NaN;
                okZ = false;
            }
        }

        // -------------------------
        // 4) If no END: degrade mode (no tilt/orientation guidance)
        // -------------------------
        if (!hasEnd) {

            // XY fallback: bit->head
            okXY = (distXYToHead <= DataSaved.Drill_tolleranza_XY);

            okTilt = false;
            okOri = false;

            FrecciaUP = FrecciaRIGHT = FrecciaDOWN = FrecciaLEFT = false;
            holePitchDeg = Double.NaN;
            holeRollDeg = Double.NaN;

            // "pe" as bit->head vector
            double dx = mastBit[0] - hx;
            double dy = mastBit[1] - hy;
            pe = new double[]{dx, dy, distXYToHead};

            distAxis = distXYToHead;

            // Start requires tilt/orientation too -> cannot be true without end
            okStart = false;

            // Drill check: axisTol vs distAxis (qui è dist bit->head)
            double axisTol = (DataSaved.Drill_tolleranza_Axis > 0) ? DataSaved.Drill_tolleranza_Axis : DataSaved.Drill_tolleranza_XY;
            okDrill = (distAxis <= axisTol);

            return;
        }

        // -------------------------
        // 5) Full match (END present)
        // -------------------------
        st = DrillMatch.matchMastToHole(
                mastHead, mastBit,
                holeStart, holeEnd,
                DataSaved.Drill_tolleranza_XY,
                DataSaved.Drill_tolleranza_Angolo
        );

        okXY = st.xyInRange;          // qui = bit->ASSE
        okTilt = st.tiltInRange;
        okOri = st.orientationInRange;

        // -------------------------
        // 6) Triangles (pitch/roll guidance)
        // -------------------------
        tri = DrillGuidance.computeTiltTriangles(
                mastHead, mastBit,
                holeStart, holeEnd,
                hdt_BOOM,
                DataSaved.Drill_tolleranza_Angolo
        );

        // Pitch/Roll del mast e del foro (già calcolati da DrillGuidance)
        double mastPitch = tri.mastPitchDeg;
        double mastRoll  = tri.mastRollDeg;

        double holePitch = tri.holePitchDeg;
        double holeRoll  = tri.holeRollDeg;

// Se il palo è verticale: guida verso "bolla" (pitch=0, roll=0)
        if (isHoleVerticalDeg(st.holeTiltDeg)) {
            setTrianglesToReachTargets(mastPitch, mastRoll, 0.0, 0.0, DataSaved.Drill_tolleranza_Angolo);
        } else {
            // Palo inclinato: guida verso i target del foro
            setTrianglesToReachTargets(mastPitch, mastRoll, holePitch, holeRoll, DataSaved.Drill_tolleranza_Angolo);
        }


        holePitchDeg = tri.holePitchDeg;
        holeRollDeg = tri.holeRollDeg;

        // -------------------------
        // 7) Plan error bit->axis (XY) + distAxis
        // -------------------------
        PlanError.Result per = PlanError.calcPlanErrorToAxisXY(
                mastBit[0], mastBit[1],
                holeStart[0], holeStart[1],
                holeEnd[0], holeEnd[1],
                false // retta infinita consigliata per "in asse"
        );

        pe = new double[]{per.errE, per.errN, per.dist};
        distAxis = per.dist;

        // -------------------------
        // 8) OK_START logic (inizio): bit su testa + tilt + ori (se inclinato) + Z
        // -------------------------
        final boolean ignoreOri = (Double.isNaN(st.holeTiltDeg) || st.holeTiltDeg < 1.0);
        final boolean oriForStart = ignoreOri ? true : okOri;

        okStart =
                (distXYToHead <= DataSaved.Drill_tolleranza_XY) // bit sulla testa in pianta
                        && okZ                             // quota progetto testa
                        && okTilt                          // inclinazione corretta
                        && oriForStart;                    // azimut se significativo

        // -------------------------
        // 9) OK_DRILL logic (discesa): solo distanza bit->asse
        // -------------------------
        double axisTol = (DataSaved.Drill_tolleranza_Axis > 0) ? DataSaved.Drill_tolleranza_Axis : DataSaved.Drill_tolleranza_XY;
        okDrill = (distAxis <= axisTol);
        if (isDrilling) {
            boolean vertical = false;
            double tiltProj = 0.0;
            if (sel != null && sel.getTilt() != null) {
                tiltProj = sel.getTilt();
                vertical = tiltProj < 1.0;
            }

            Log.d("DRILL_GUIDE", "------------------------------");

            // 1) Stato generale
            Log.d("DRILL_GUIDE", "OK_DRILL=" + okDrill +
                    "  OK_XY=" + okXY +
                    "  OK_TILT=" + okTilt +
                    "  OK_ORI=" + okOri);

            // 2) Tipo palo
            Log.d("DRILL_GUIDE", "HOLE_TILT_PROJ=" + tiltProj +
                    " deg  VERTICAL=" + vertical);

            // 3) Discostamento laterale
            Log.d("DRILL_GUIDE", String.format(
                    "AXIS_ERR_XY=%.3f m   (errE=%.3f , errN=%.3f)",
                    distAxis, pe[0], pe[1]
            ));

            // 4) Distanza dalla testa (utile all’inizio)
            Log.d("DRILL_GUIDE", String.format(
                    "DIST_TO_HEAD_XY=%.3f m   DZ_TO_HEAD=%.3f m   DIST3D=%.3f m",
                    distXYToHead, dzToHead, dist3DToHead
            ));

            // 5) Profondità / avanzamento
            if (sel != null &&
                    sel.getHeadZ() != null &&
                    sel.getEndZ() != null &&
                    ExcavatorLib.toolEndCoord != null) {

                double bitZ = ExcavatorLib.toolEndCoord[2];
                double headZ = sel.getHeadZ();
                double endZ  = sel.getEndZ();

                if (vertical) {
                    double remainingZ = endZ - bitZ;
                    PointService.remainingZ = remainingZ;
                    PointService.remainingAxis = Double.NaN;
                    Log.d("DRILL_GUIDE", String.format(
                            "DEPTH_VERTICAL: bitZ=%.3f  endZ=%.3f  REMAIN_Z=%.3f m",
                            bitZ, endZ, remainingZ
                    ));
                } else {
                    // avanzamento lungo asse
                    double s = distAlongAxisFromHead(
                            ExcavatorLib.toolEndCoord,
                            sel.getHeadX(), sel.getHeadY(), sel.getHeadZ(),
                            sel.getEndX(), sel.getEndY(), sel.getEndZ()
                    );

                    double ax = sel.getEndX() - sel.getHeadX();
                    double ay = sel.getEndY() - sel.getHeadY();
                    double az = sel.getEndZ() - sel.getHeadZ();
                    double L = Math.sqrt(ax*ax + ay*ay + az*az);

                    double sClamped = Math.max(0.0, Math.min(L, s));
                    double remainingAxis = L - sClamped;
                    PointService.remainingAxis = remainingAxis;
                    PointService.remainingZ = Double.NaN;
                    Log.d("DRILL_GUIDE", String.format(
                            "DEPTH_INCLINED: ADV=%.3f / %.3f m   REMAIN_AXIS=%.3f m",
                            sClamped, L, remainingAxis
                    ));
                }
            }

            // 6) Angoli mast vs progetto
            if (st != null) {
                Log.d("DRILL_GUIDE", String.format(
                        "TILT: mast=%.2f°  hole=%.2f°  OK=%s",
                        st.mastTiltDeg, st.holeTiltDeg, okTilt
                ));

                Log.d("DRILL_GUIDE", String.format(
                        "ORI: mast=%.2f°  hole=%.2f°  d=%.2f°  OK=%s",
                        st.mastBearingDeg, st.holeBearingDeg, st.dBearingDeg, okOri
                ));
            }

            // 7) Frecce pitch/roll
            Log.d("DRILL_GUIDE", "ARROWS  UP=" + FrecciaUP +
                    " DOWN=" + FrecciaDOWN +
                    " LEFT=" + FrecciaLEFT +
                    " RIGHT=" + FrecciaRIGHT);
        }

    }

    private void resetOutputs() {
        // Match principali
        okXY = false;
        okTilt = false;
        okOri = false;

        // Stati operativi
        okStart = false;
        okDrill = false;
        okZ = false;

        // Frecce guida
        FrecciaUP = false;
        FrecciaRIGHT = false;
        FrecciaDOWN = false;
        FrecciaLEFT = false;

        // Errori planimetrici
        pe = new double[]{0, 0, 0};

        // Distanze diagnostiche
        distAxis = Double.NaN;
        distXYToHead = Double.NaN;
        dzToHead = Double.NaN;
        dist3DToHead = Double.NaN;

        // (opzionale) azzera anche angoli target se li mostri in UI
        holePitchDeg = Double.NaN;
        holeRollDeg = Double.NaN;
    }


    /**
     * FIX: copia position, non reference!
     */
    private void updateCurrentPosition(double[] position, double raggio) {
        if (position == null || position.length < 3) return;

        double r = raggio / 4.0;
        r = Math.min(r, 30.0);

        if (DistToPoint.dist2D(position, lastPosition) > r) {
            lastPosition = position.clone(); // <--- FIX CRITICO
            triangleHelper.updatePointRaius(lastPosition, raggio);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Point3D_Drill findNearestDrillPoint(
            double bucketEst,
            double bucketNord,
            List<Point3D_Drill> filteredPoints
    ) {
        if (filteredPoints == null || filteredPoints.isEmpty()) return null;

        Point3D_Drill nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Point3D_Drill p : filteredPoints) {
            if (p == null) continue;
            if (p.getHeadX() == null || p.getHeadY() == null) continue;

            // Se NON è TODO, non è selezionabile: saltalo subito
            Integer status = p.getStatus();
            boolean isTodo = (status == null || status == 0);
            if (!isTodo) continue;

            // distanza in XY dalla testa foro
            double d = new DistToPoint(bucketEst, bucketNord, 0,
                    p.getHeadX(), p.getHeadY(), 0).getDist_to_point();

            if (d < minDistance) {
                minDistance = d;
                nearest = p;
            }
        }

        return nearest; // null se non c'è alcun TODO vicino
    }


    private static boolean isHoleVerticalDeg(double holeTiltDeg) {
        return !Double.isNaN(holeTiltDeg) && holeTiltDeg < 1.0; // tua soglia
    }

    private static void setTrianglesToReachTargets(double mastPitch, double mastRoll,
                                                   double targetPitch, double targetRoll,
                                                   double tolDeg) {
        double dP = mastPitch - targetPitch;
        double dR = mastRoll - targetRoll;

        // stessa logica di DrillGuidance: se mast < target => UP/RIGHT
        FrecciaUP = (Math.abs(dP) > tolDeg) && (dP < 0);
        FrecciaDOWN = (Math.abs(dP) > tolDeg) && (dP > 0);

        FrecciaRIGHT = (Math.abs(dR) > tolDeg) && (dR < 0);
        FrecciaLEFT = (Math.abs(dR) > tolDeg) && (dR > 0);
    }
    private static double distAlongAxisFromHead(
            double[] bit,  // [E,N,Z]
            double hx, double hy, double hz,
            double ex, double ey, double ez
    ) {
        double ax = ex - hx;
        double ay = ey - hy;
        double az = ez - hz;

        double L = Math.sqrt(ax*ax + ay*ay + az*az);
        if (L < 1e-9) return Double.NaN;

        // versore asse
        double ux = ax / L;
        double uy = ay / L;
        double uz = az / L;

        // vettore head->bit
        double bx = bit[0] - hx;
        double by = bit[1] - hy;
        double bz = bit[2] - hz;

        // proiezione scalare (metri lungo asse, 0=head, L=end)
        return (bx*ux + by*uy + bz*uz);
    }
    public static boolean isTiltWithinTolerance() {
        return !(FrecciaUP || FrecciaLEFT || FrecciaDOWN || FrecciaRIGHT);
    }
}
