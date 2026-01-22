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
 *
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

    // Loop timing
    private static final long LOOP_MS = 100L;

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

        if (sel == null) {
            resetOutputs();
            return;
        }

        // Validazione coordinate testa
        if (sel.getHeadX() == null || sel.getHeadY() == null || sel.getHeadZ() == null) {
            resetOutputs();
            return;
        }

        // Validazione coordinate fondo (evita autounboxing NPE)
        final boolean hasEnd = (sel.getEndX() != null && sel.getEndY() != null && sel.getEndZ() != null);

        // arrays (mast)
        final double[] mastHead = coordTool;    // già double[3]
        final double[] mastBit = toolEndCoord;  // già double[3]

        // arrays (hole)
        final double[] holeStart = new double[]{sel.getHeadX(), sel.getHeadY(), sel.getHeadZ()};

        // Se manca end: puoi decidere policy.
        // Qui: okTilt/okOri = false, okXY calcolato come distanza da head (DrillMatch lo fa in fallback se asse degenero,
        // ma se end mancante non possiamo passare holeEnd valido).
        if (!hasEnd) {
            // XY fallback su head
            double dx = mastBit[0] - holeStart[0];
            double dy = mastBit[1] - holeStart[1];
            double dist = Math.sqrt(dx * dx + dy * dy);

            okXY = dist <= DataSaved.tolleranza_XY;
            okTilt = false;
            okOri = false;

            FrecciaUP = FrecciaRIGHT = FrecciaDOWN = FrecciaLEFT = false;

            pe = new double[]{dx, dy, dist};
            return;
        }

        final double[] holeEnd = new double[]{sel.getEndX(), sel.getEndY(), sel.getEndZ()};

        // --- Match (OK) ---
        st = DrillMatch.matchMastToHole(
                mastHead, mastBit,
                holeStart, holeEnd,
                DataSaved.tolleranza_XY,
                DataSaved.tolleranza_Slope
        );

        okXY = st.xyInRange;
        okTilt = st.tiltInRange;
        okOri = st.orientationInRange;

        // --- Triangoli (guida pitch/roll) ---
        tri = DrillGuidance.computeTiltTriangles(
                mastHead, mastBit,
                holeStart, holeEnd,
                hdt_BOOM,
                DataSaved.tolleranza_Slope
        );

        FrecciaUP = tri.up;
        FrecciaRIGHT = tri.right;
        FrecciaDOWN = tri.down;
        FrecciaLEFT = tri.left;

        // --- Plan Error (bit -> asse foro XY) ---
        // PlanError restituisce Result. Tu vuoi double[3].
        PlanError.Result per = PlanError.calcPlanErrorToAxisXY(
                mastBit[0], mastBit[1],
                holeStart[0], holeStart[1],
                holeEnd[0], holeEnd[1],
                false // retta infinita consigliata per "in asse"
        );
        pe = new double[]{per.errE, per.errN, per.dist};
    }

    private void resetOutputs() {
        okXY = false;
        okTilt = false;
        okOri = false;

        FrecciaUP = false;
        FrecciaRIGHT = false;
        FrecciaDOWN = false;
        FrecciaLEFT = false;

        pe = new double[]{0, 0, 0};
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

    public static Point3D_Drill findNearestDrillPoint(double bucketEst, double bucketNord, List<Point3D_Drill> filteredPoints) {
        if (filteredPoints == null || filteredPoints.isEmpty()) return null;

        Point3D_Drill nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Point3D_Drill p : filteredPoints) {
            if (p == null) continue;
            if (p.getHeadX() == null || p.getHeadY() == null) continue;

            // distanza in XY dalla testa foro
            double d = new DistToPoint(bucketEst, bucketNord, 0,
                    p.getHeadX(), p.getHeadY(), 0).getDist_to_point();

            if (d < minDistance) {
                minDistance = d;
                nearest = p;
            }
        }

        if (nearest == null) return null;

        Integer status = nearest.getStatus();
        if (status == null || status == 0) return nearest;

        return null;
    }
}
