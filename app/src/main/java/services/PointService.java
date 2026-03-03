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
import utils.Utils;

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
    public static double Solar_Delta_X=0.0d,Solar_Delta_Y=0.0d;
    static double mRaggio;
    public static boolean AB_REVERSED=false;
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
        mRaggio=0;
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
        mRaggio=0;
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
                updateCurrentPosition(ExcavatorLib.toolEndCoord, DataSaved.Raggio_Drill);

                // --- autosnap / selezione punto ---
                switch (DataSaved.isAutoSnap) {
                    case 0:
                        // niente
                        break;

                    case 1:
                        // auto-selezione solo se non drilling
                        if (!isDrilling) {
                            if (DataSaved.filtered_drill_points != null && !DataSaved.filtered_drill_points.isEmpty()) {

                                boolean isBA = AB_REVERSED; // o come si chiama il tuo boolean

                                if (DataSaved.Drilling_Mode == SOLARFARM_MODE) {

                                    Selected_Point3D_Drill = findNearestDrillPointSolarAligned(
                                            toolEndCoord[0], toolEndCoord[1],
                                            DataSaved.filtered_drill_points,
                                            DataSaved.ALLINEAMENTO_AB,
                                            isBA
                                    );

                                } else {
                                    Selected_Point3D_Drill = findNearestDrillPointPlain(
                                            toolEndCoord[0], toolEndCoord[1],
                                            DataSaved.filtered_drill_points
                                    );
                                }
                            }
                        }
                        break;

                    case 2:
                        // selezione manuale (long press) -> qui non fare nulla
                        break;
                }

                // --- guida (ok + frecce + plan error) ---
                computeGuidance();
                if(DataSaved.Drilling_Mode==SOLARFARM_MODE) {
                    try {
                        if (Selected_Point3D_Drill != null && toolEndCoord != null) {
                            MovementDelta.Delta result = MovementDelta.calculateDelta(
                                    toolEndCoord[0], toolEndCoord[1],   // target
                                    Selected_Point3D_Drill.getEndX(), Selected_Point3D_Drill.getEndY()    // destinazione
                            );
                            if(!AB_REVERSED) {
                                Solar_Delta_X = result.deltaX;
                                Solar_Delta_Y = result.deltaY;
                            }else {
                                Solar_Delta_X = -result.deltaX;
                                Solar_Delta_Y = -result.deltaY;
                            }
                        }
                    } catch (Exception e) {
                        Solar_Delta_X=0;
                        Solar_Delta_Y=0;
                    }

                }

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

                double ori = normalizeAngle(NmeaListener.mch_Orientation + DataSaved.deltaGPS2);
                double ab = normalizeAngle(DataSaved.ALLINEAMENTO_AB);

                OrientationResult res = checkLineOrientation(
                        ori,
                        ab,
                        DataSaved.Drill_tolleranza_HDT
                );

                okOri = res.inTolerance;
                AB_REVERSED = res.reverse;

                // ora puoi usare isReverse per UI, frecce, logica, ecc.
            } else {
                okOri = true; // JET + ROCK
            }

        } else {
            // Inclinato: match completo verso asse foro

            // Costruisci asse foro (attenzione: zValid può essere false)
            double[] holeStart = new double[]{hxObj, hyObj, (zValid ? hzObj : 0.0)};
            double[] holeEnd   = new double[]{exObj, eyObj, ezObj};

            // ✅ Normalizza verso: vogliamo sempre un asse "dall'alto verso il basso"
            // (se END è più alto di START, scambia)
            if (!Double.isNaN(holeStart[2]) && !Double.isNaN(holeEnd[2]) && holeEnd[2] > holeStart[2]) {
                double[] tmp = holeStart;
                holeStart = holeEnd;
                holeEnd = tmp;
            }

            // Se per qualche motivo start==end (asse degenerato), degrada a verticale/punto (bolla)
            double dx = holeEnd[0] - holeStart[0];
            double dy = holeEnd[1] - holeStart[1];
            double dz = holeEnd[2] - holeStart[2];
            double norm2 = dx*dx + dy*dy + dz*dz;

            if (norm2 < 1e-10) {
                // fallback: guida a bolla
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

                okOri = true; // oppure mantieni la logica solarfarm se vuoi, ma qui sei in "inclinato" degenerato
                holePitchDeg = 0.0;
                holeRollDeg = 0.0;

            } else {
                // Match completo verso asse foro
                st = DrillMatch.matchMastToHole(
                        mastHead, mastBit,
                        holeStart, holeEnd,
                        DataSaved.Drill_tolleranza_XY,
                        DataSaved.Drill_tolleranza_Angolo,
                        DataSaved.Drill_tolleranza_HDT
                );

                // Guardie: st può essere null in caso di errori interni
                if (st != null) {
                    okTilt = st.tiltInRange;
                    okOri  = st.orientationInRange;
                } else {
                    okTilt = false;
                    okOri  = false;
                }

                // ✅ Triangoli verso target foro (non bolla)
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
                holeRollDeg  = tri.holeRollDeg;
            }
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

        if (DistToPoint.dist2D(position, lastPosition) > r||mRaggio!=DataSaved.Raggio_Drill) {//aggiorna ogni 3 metri
            lastPosition = position.clone(); // <--- FIX CRITICO
            triangleHelper.updatePointRaius(lastPosition, raggio);
            mRaggio=DataSaved.Raggio_Drill;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Point3D_Drill findNearestDrillPointSolarAligned(
            double x0,
            double y0,
            List<Point3D_Drill> points,
            Double allineamentoABDeg,
            boolean isBA   // true = BA, false = AB
    ) {
        if (points == null || points.isEmpty()) return null;

        if (allineamentoABDeg == null) {
            return findNearestDrillPointPlain(x0, y0, points);
        }

        // Direzione effettiva (AB o BA)
        double bearing = normalizeAngle(allineamentoABDeg + (isBA ? 180.0 : 0.0));
        double brad = Math.toRadians(bearing);

        // versore direzione (X=Est, Y=Nord)
        double ux = Math.sin(brad);
        double uy = Math.cos(brad);

        // tolleranza laterale: quanto devo essere vicino alla linea per considerare i pali della mia fila
        final double LINE_TOL = 1.0; // metri (tuning)

        Point3D_Drill best = null;
        double bestAbsAlong = Double.MAX_VALUE;
        double bestPerp = Double.MAX_VALUE;

        boolean foundOnLine = false;

        for (Point3D_Drill p : points) {
            if (p == null || p.getHeadX() == null || p.getHeadY() == null) continue;

            // SOLO TODO
            Integer status = p.getStatus();
            boolean isTodo = (status == null || status == 0);
            if (!isTodo) continue;

            double vx = p.getHeadX() - x0;
            double vy = p.getHeadY() - y0;

            // distanza perpendicolare alla retta che passa per (x0,y0) con direzione u
            double perp = Math.abs(ux * vy - uy * vx);

            // posizione lungo asse
            double along = ux * vx + uy * vy;

            // prima fase: preferisco punti sulla linea
            if (perp <= LINE_TOL) {
                foundOnLine = true;

                double absAlong = Math.abs(along);

                // criterio: più vicino lungo asse (davanti o dietro) => midpoint automatico
                // tie-break: più centrato (perp min)
                if (absAlong < bestAbsAlong || (Math.abs(absAlong - bestAbsAlong) < 1e-6 && perp < bestPerp)) {
                    bestAbsAlong = absAlong;
                    bestPerp = perp;
                    best = p;
                }
            }
        }

        // Se ho trovato qualcosa sulla mia linea, ritorno quello
        if (foundOnLine && best != null) return best;

        // Altrimenti fallback: nearest assoluto TODO
        return findNearestDrillPointPlain(x0, y0, points);
    }
    private static Point3D_Drill findNearestDrillPointPlain(
            double bucketEst,
            double bucketNord,
            List<Point3D_Drill> filteredPoints
    ) {
        Point3D_Drill nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Point3D_Drill p : filteredPoints) {
            if (p == null || p.getHeadX() == null || p.getHeadY() == null) continue;

            Integer status = p.getStatus();
            boolean isTodo = (status == null || status == 0);
            if (!isTodo) continue;

            double d = dist2D(bucketEst, bucketNord, p.getHeadX(), p.getHeadY());
            if (d < minDistance) {
                minDistance = d;
                nearest = p;
            }
        }
        return nearest;
    }
    private static double dist2D(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx*dx + dy*dy);
    }


    public static boolean isTiltWithinTolerance() {
        return !(FrecciaUP || FrecciaLEFT || FrecciaDOWN || FrecciaRIGHT);
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
        return Utils.readUnitOfMeasureLITE(String.valueOf(d));
    }
    private OrientationResult checkLineOrientation(double angle, double target, double deadband) {

        // normalizza differenza angolare in range -180..+180
        double diff = angle - target;
        diff = ((diff + 180) % 360 + 360) % 360 - 180;

        boolean reverse = false;

        // Se differenza > 90° significa che stai guardando nella direzione opposta
        if (Math.abs(diff) > 90) {
            reverse = true;

            // riporta differenza sulla linea (simmetria 180°)
            if (diff > 0)
                diff = diff - 180;
            else
                diff = diff + 180;
        }

        boolean inTol = Math.abs(diff) <= deadband;

        return new OrientationResult(inTol, reverse);
    }
    public static class OrientationResult {
        public boolean inTolerance;
        public boolean reverse;

        public OrientationResult(boolean inTolerance, boolean reverse) {
            this.inTolerance = inTolerance;
            this.reverse = reverse;
        }
    }
    public class MovementDelta {

        public static class Delta {
            public final double deltaX; // linea verde
            public final double deltaY; // linea viola
            public final double distance; // linea blu (opzionale)

            public Delta(double deltaX, double deltaY, double distance) {
                this.deltaX = deltaX;
                this.deltaY = deltaY;
                this.distance = distance;
            }
        }

        public static Delta calculateDelta(
                double xTarget, double yTarget,
                double xDest, double yDest) {

            // Delta con segno secondo le tue regole
            double deltaX = xTarget - xDest; // negativo se a sinistra
            double deltaY = yTarget - yDest; // negativo se oltre (sopra)

            // Lunghezza linea blu (distanza euclidea)
            double distance = Math.sqrt(
                    Math.pow(deltaX, 2) + Math.pow(deltaY, 2)
            );

            return new Delta(deltaY, deltaX, distance);
        }
    }
}

