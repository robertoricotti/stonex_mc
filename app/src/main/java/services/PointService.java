package services;

import static drill_pile.gui.Drill_Activity.isDrilling;
import static packexcalib.exca.DataSaved.Selected_Point3D_Drill;
import static packexcalib.exca.ExcavatorLib.coordTool;
import static packexcalib.exca.ExcavatorLib.hdt_BOOM;
import static packexcalib.exca.ExcavatorLib.toolEndCoord;
import static packexcalib.exca.Sensors_Decoder.normalizeAngle;
import static utils.MyTypes.JETGROUTING_MODE;
import static utils.MyTypes.SOLARFARM_MODE;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import drill_pile.gui.DrillGuidance;
import drill_pile.gui.PlanError;
import iredes.DrillMatch;
import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.gnss.NmeaListener;
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
    int countTabella = 0;
    public static String[] valoriTabella;

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
        valoriTabella = new String[24];
        triangleHelper = new TriangleHelper();
        lastPosition = new double[]{0, 0, 0};
        countTabella = 0;
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
        final Double hzObj = sel.getHeadZ(); // può essere null o 0 -> "quota non significativa"

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

        // Z "valida" solo se esiste e != 0 (come richiesto: 0 o null => ignora okZ)
        final boolean zValid = (hzObj != null && Math.abs(hzObj) > 1e-9);
        final double hz = zValid ? hzObj : Double.NaN;

        // -------------------------
        // 2) End availability (serve per inclinati / asse foro)
        // -------------------------
        final Double exObj = sel.getEndX();
        final Double eyObj = sel.getEndY();
        final Double ezObj = sel.getEndZ();

        boolean hasEnd = (exObj != null && eyObj != null && ezObj != null);

        // Se END coincide con HEAD (punto), trattalo come "noEnd"
        if (hasEnd && (Math.abs(exObj - hxObj) < 1e-6) && (Math.abs(eyObj - hyObj) < 1e-6)) {
            // se anche Z coincide o non è valida, è un punto
            double hzForCompare = (hzObj != null) ? hzObj : ezObj;
            if (!zValid || Math.abs(ezObj - hzForCompare) < 1e-6) {
                hasEnd = false;
            }
        }

        // -------------------------
        // 3) Always compute "bit -> head" XY (+Z if possible)
        // -------------------------
        {
            double dxh = mastBit[0] - hx;
            double dyh = mastBit[1] - hy;
            distXYToHead = Math.sqrt(dxh * dxh + dyh * dyh);

            if (zValid && !Double.isNaN(mastBit[2])) {
                dzToHead = mastBit[2] - hz;
                dist3DToHead = Math.sqrt(distXYToHead * distXYToHead + dzToHead * dzToHead);
                okZ = (Math.abs(dzToHead) <= DataSaved.Drill_tolleranza_Z);
            } else {
                dzToHead = Double.NaN;
                dist3DToHead = Double.NaN;
                okZ = true; // ✅ Z ignorata => non deve bloccare
            }
        }

        // Default flags
        FrecciaUP = FrecciaRIGHT = FrecciaDOWN = FrecciaLEFT = false;
        holePitchDeg = Double.NaN;
        holeRollDeg = Double.NaN;
        st = null;
        tri = null;

        // -------------------------
        // 4) Determine "hole type"
        // -------------------------
        // - Inclinato: se ho END e tilt significativo
        // - Verticale: altrimenti
        boolean inclined = false;
        double holeTiltDeg = Double.NaN;

        if (hasEnd) {
            // calcola tilt dall'asse foro (0 verticale, 90 orizzontale)
            double dx = exObj - hxObj;
            double dy = eyObj - hyObj;
            double dz = ezObj - (zValid ? hzObj : ezObj);

            double horiz = Math.sqrt(dx * dx + dy * dy);
            double vert = Math.abs(dz);

            if (horiz < 1e-12 && vert < 1e-12) {
                holeTiltDeg = 0.0;
            } else {
                holeTiltDeg = Math.toDegrees(Math.atan2(horiz, vert));
            }

            inclined = holeTiltDeg >= 1.0; // tua soglia "verticale"
        } else {
            inclined = false; // punto => verticale
        }

        // -------------------------
        // 5) Compute okXY and distAxis
        // -------------------------
        // - Verticale/punto: okXY = bit->testa
        // - Inclinato: okXY = bit->ASSE (PlanError)
        okXY = false;
        distAxis = Double.NaN;

        if (!inclined) {
            okXY = (distXYToHead <= DataSaved.Drill_tolleranza_XY);
            distAxis = distXYToHead;

            // compat pe = vettore bit->head
            double dx = mastBit[0] - hx;
            double dy = mastBit[1] - hy;
            pe = new double[]{dx, dy, distXYToHead};

        } else {
            // asse foro in XY (linea head->end)
            PlanError.Result per = PlanError.calcPlanErrorToAxisXY(
                    mastBit[0], mastBit[1],
                    hxObj, hyObj,
                    exObj, eyObj,
                    false
            );
            pe = new double[]{per.errE, per.errN, per.dist};
            distAxis = per.dist;

            okXY = (distAxis <= DataSaved.Drill_tolleranza_XY);
        }

        // -------------------------
        // 6) Compute tilt/orientation + arrows
        // -------------------------
        // Regole richieste:
        // - Verticale/punto: okTilt guida verso bolla (0/0), okOri:
        //     - SOLARFARM: orientamento = ALLINEAMENTO_AB
        //     - JET + ROCK: okOri = true
        // - Inclinato: SEMPRE: okTilt + okOri (bearing foro) + bit sulla testa + okZ (se Z valida)
        okTilt = false;
        okOri = false;

        if (!inclined) {
            // guida verso bolla sempre (target 0/0)
            tri = DrillGuidance.computeTiltTrianglesToTargets(
                    mastHead, mastBit,
                    hdt_BOOM,
                    0.0, 0.0,
                    DataSaved.Drill_tolleranza_Angolo
            );

            FrecciaUP = tri.up;
            FrecciaRIGHT = tri.right;
            FrecciaDOWN = tri.down;
            FrecciaLEFT = tri.left;

            okTilt = !Double.isNaN(tri.deltaPitchDeg) && !Double.isNaN(tri.deltaRollDeg)
                    && Math.abs(tri.deltaPitchDeg) <= DataSaved.Drill_tolleranza_Angolo
                    && Math.abs(tri.deltaRollDeg) <= DataSaved.Drill_tolleranza_Angolo;

            holePitchDeg = 0.0;
            holeRollDeg = 0.0;

            // orientamento:
            if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {
                okOri = isInRangeAngle(
                        normalizeAngle(NmeaListener.mch_Orientation + DataSaved.deltaGPS2),
                        normalizeAngle(DataSaved.ALLINEAMENTO_AB),
                        DataSaved.Drill_tolleranza_HDT
                );
            } else {
                okOri = true; // JET + ROCK
            }

        } else {
            // Inclinato: match completo verso asse foro
            final double[] holeStart = new double[]{hxObj, hyObj, (zValid ? hzObj : 0.0)};
            final double[] holeEnd = new double[]{exObj, eyObj, ezObj};

            st = DrillMatch.matchMastToHole(
                    mastHead, mastBit,
                    holeStart, holeEnd,
                    DataSaved.Drill_tolleranza_XY,
                    DataSaved.Drill_tolleranza_Angolo,
                    DataSaved.Drill_tolleranza_HDT
            );

            okTilt = st.tiltInRange;
            okOri = st.orientationInRange;

            // triangoli verso target foro (non bolla)
            tri = DrillGuidance.computeTiltTriangles(
                    mastHead, mastBit,
                    holeStart, holeEnd,
                    hdt_BOOM,
                    DataSaved.Drill_tolleranza_Angolo
            );

            FrecciaUP = tri.up;
            FrecciaRIGHT = tri.right;
            FrecciaDOWN = tri.down;
            FrecciaLEFT = tri.left;

            holePitchDeg = tri.holePitchDeg;
            holeRollDeg = tri.holeRollDeg;
        }

        // -------------------------
        // 7) OK_START / OK_DRILL rules (pulite e consistenti)
        // -------------------------
        // Richiesta:
        // - Verticale/punto: se Z nulla/0 => ignora okZ.
        //   OK se XY ok e orientamento ok (solo solarfarm), + tilt ok (bolla) per stabilità.
        //
        // - Inclinato: SEMPRE OK se:
        //      bit sulla testa (XY) + okZ (se valida) + okTilt + okOri
        //
        // Nota: "bit sulla testa" in entrambi i casi è distXYToHead <= tolXY (non distAxis).
        final boolean bitOnHeadXY = (distXYToHead <= DataSaved.Drill_tolleranza_XY);
        final boolean zOkForStart = okZ; // già true se z non valida

        if (!inclined) {
            // verticale/punto: non richiedere okZ se z non valida (già true)
            // orientamento: solarfarm, altrimenti true
            // tilt: guida bolla
            okStart = bitOnHeadXY && okTilt && okOri;
        } else {
            okStart = bitOnHeadXY && zOkForStart && okTilt && okOri;
        }

        // OK_DRILL: durante discesa usa distanza dall'asse (se inclinato) o dalla testa (se verticale),
        // e richiede tilt/orientamento solo per inclinati (come tua regola).
        double axisTol = (DataSaved.Drill_tolleranza_Axis > 0)
                ? DataSaved.Drill_tolleranza_Axis
                : DataSaved.Drill_tolleranza_XY;

        if (!inclined) {
            okDrill = (distAxis <= axisTol) && okOri; // ori solo solarfarm ha senso, negli altri è true
        } else {
            okDrill = (distAxis <= axisTol) && okTilt && okOri;
        }

        // -------------------------
        // 8) Debug (opzionale)
        // -------------------------
        if(DataSaved.Drilling_Mode==JETGROUTING_MODE) {
            tabellaValues();
        }
        if (isDrilling) {
            Log.d("DRILL_GUIDE", "------------------------------");
            Log.d("DRILL_GUIDE", "INCLINED=" + inclined + "  hasEnd=" + hasEnd + "  zValid=" + zValid);
            Log.d("DRILL_GUIDE", "OK_START=" + okStart + "  OK_DRILL=" + okDrill +
                    "  OK_XY=" + okXY + "  OK_TILT=" + okTilt + "  OK_ORI=" + okOri + "  OK_Z=" + okZ);
            Log.d("DRILL_GUIDE", String.format(Locale.US,
                    "DIST_TO_HEAD_XY=%.3f  DIST_AXIS=%.3f  DZ_TO_HEAD=%s",
                    distXYToHead, distAxis, Double.isNaN(dzToHead) ? "NaN" : String.format(Locale.US, "%.3f", dzToHead)));
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

        double L = Math.sqrt(ax * ax + ay * ay + az * az);
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
        return (bx * ux + by * uy + bz * uz);
    }

    public static boolean isTiltWithinTolerance() {
        return !(FrecciaUP || FrecciaLEFT || FrecciaDOWN || FrecciaRIGHT);
    }

    public static Point3D_Drill[] getAlignmentPointsById(String idA, String idB) {

        Point3D_Drill pA = null;
        Point3D_Drill pB = null;

        if (DataSaved.drill_points == null || DataSaved.drill_points.isEmpty())
            return new Point3D_Drill[]{null, null};

        for (Point3D_Drill p : DataSaved.drill_points) {
            if (p == null || p.getId() == null) continue;

            String pid = p.getId().trim();

            if (idA != null && pid.equalsIgnoreCase(idA.trim())) {
                pA = p;
            }

            if (idB != null && pid.equalsIgnoreCase(idB.trim())) {
                pB = p;
            }

            // se trovati entrambi esco prima
            if (pA != null && pB != null) break;
        }

        return new Point3D_Drill[]{pA, pB};
    }

    private boolean isInRangeAngle(double angle, double target, double deadband) {
        return Math.abs(angle - target) <= deadband;
    }

    private void tabellaValues() {
        countTabella++;
        String[] result = new String[24]; // esempio

        if (Selected_Point3D_Drill == null)
            return;

        if (countTabella % 10 == 0) {
            double drillbit = toolEndCoord != null ? toolEndCoord[2] : Double.NaN;

            Double start1_drl = parseDoubleSafe(Selected_Point3D_Drill.getDrlStart_1());
            Double stop1_drl = parseDoubleSafe(Selected_Point3D_Drill.getDrlStop_1());

            Double start2_drl = parseDoubleSafe(Selected_Point3D_Drill.getDrlStart_2());
            Double stop2_drl = parseDoubleSafe(Selected_Point3D_Drill.getDrlStop_2());

            Double start3_drl = parseDoubleSafe(Selected_Point3D_Drill.getDrlStart_3());
            Double stop3_drl = parseDoubleSafe(Selected_Point3D_Drill.getDrlStop_3());

            Double start4_drl = parseDoubleSafe(Selected_Point3D_Drill.getDrlStart_4());
            Double stop4_drl = parseDoubleSafe(Selected_Point3D_Drill.getDrlStop_4());

            Double start1_jet = parseDoubleSafe(Selected_Point3D_Drill.getJetStart_1());
            Double stop1_jet = parseDoubleSafe(Selected_Point3D_Drill.getJetStop_1());

            Double start2_jet = parseDoubleSafe(Selected_Point3D_Drill.getJetStart_2());
            Double stop2_jet = parseDoubleSafe(Selected_Point3D_Drill.getDrlStop_2());

            Double start3_jet = parseDoubleSafe(Selected_Point3D_Drill.getJetStart_3());
            Double stop3_jet = parseDoubleSafe(Selected_Point3D_Drill.getDrlStop_3());

            Double start4_jet = parseDoubleSafe(Selected_Point3D_Drill.getJetStart_4());
            Double stop4_jet = parseDoubleSafe(Selected_Point3D_Drill.getDrlStop_4());

            result[0] = formatDoubleOrEmpty(Math.abs(drillbit - start1_drl));
            result[1] = formatDoubleOrEmpty(Math.abs(drillbit - stop1_drl));
            result[2] = Selected_Point3D_Drill.getPr_1();

            result[3] = formatDoubleOrEmpty(Math.abs(drillbit - start2_drl));
            result[4] = formatDoubleOrEmpty(Math.abs(drillbit - stop2_drl));
            result[5] = Selected_Point3D_Drill.getPr_2();

            result[6] = formatDoubleOrEmpty(Math.abs(drillbit - start3_drl));
            result[7] = formatDoubleOrEmpty(Math.abs(drillbit - stop3_drl));
            result[8] = Selected_Point3D_Drill.getPr_3();

            result[9] = formatDoubleOrEmpty(Math.abs(drillbit - start4_drl));
            result[10] = formatDoubleOrEmpty(Math.abs(drillbit - stop4_drl));
            result[11] = Selected_Point3D_Drill.getPr_4();

            //jet
            result[12] = formatDoubleOrEmpty(Math.abs(drillbit - start1_jet));
            result[13] = formatDoubleOrEmpty(Math.abs(drillbit - stop1_jet));
            result[14] = Selected_Point3D_Drill.getPr_j_1();

            result[15] = formatDoubleOrEmpty(Math.abs(drillbit - start2_jet));
            result[16] = formatDoubleOrEmpty(Math.abs(drillbit - stop2_jet));
            result[17] = Selected_Point3D_Drill.getPr_j_2();

            result[18] = formatDoubleOrEmpty(Math.abs(drillbit - start3_jet));
            result[19] = formatDoubleOrEmpty(Math.abs(drillbit - stop3_jet));
            result[20] = Selected_Point3D_Drill.getPr_j_3();

            result[21] = formatDoubleOrEmpty(Math.abs(drillbit - start4_jet));
            result[22] = formatDoubleOrEmpty(Math.abs(drillbit - stop4_jet));
            result[23] = Selected_Point3D_Drill.getPr_j_4();
            valoriTabella = result;

        }

    }

    private static Double parseDoubleSafe(String s) {
        if (s == null) return null;

        s = s.trim();
        if (s.isEmpty()) return null;

        s = s.replace(',', '.'); // nel tuo progetto hai decimali con virgola

        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static String formatDoubleOrEmpty(Double d) {
        if (d == null) return "";
        return String.format(Locale.US, "%.3f", d);
    }
}

