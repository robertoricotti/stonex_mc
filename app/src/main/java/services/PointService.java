package services;

import static drill_pile.gui.Drill_Activity.isDrilling;
import static packexcalib.exca.ExcavatorLib.toolEndCoord;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iredes.Point3D_Drill;
import packexcalib.exca.DataSaved;
import packexcalib.exca.ExcavatorLib;
import packexcalib.surfcreator.TriangleHelper;
import utils.DistToPoint;

public class PointService extends Service {
    private boolean isRunning = false;
    private ExecutorService executor;
    private static double[] lastPosition;
    TriangleHelper triangleHelper;

    public PointService() {
    }

    @Override
    public void onCreate() {
        Log.d("PointService", "Created");
        triangleHelper = new TriangleHelper();
        lastPosition = new double[]{0, 0, 0};  // Posizione iniziale
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PointService", "Started");
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
        Log.d("PointService", "Destroyed");
    }

    private void stopPointLoop() {
        isRunning = false;
        executor.shutdownNow();
    }


    private final Runnable pointRunnable = () -> {
        try {
            while (isRunning) {
                long startTime = System.currentTimeMillis();
                Log.d("PointService", "Running..");
                updateCurrentPosition(ExcavatorLib.toolEndCoord, 1000);
                /** do Running Stuff Here
                 *
                 *
                 */
                switch (DataSaved.isAutoSnap) {
                    case 0:

                        break;

                    case 1:
                        if (!isDrilling) {
                            if (DataSaved.drill_points != null && !DataSaved.drill_points.isEmpty()) {
                                DataSaved.Selected_Point3D_Drill = findNearestDrillPoint(toolEndCoord[0], toolEndCoord[1], DataSaved.filtered_drill_points);
                            }
                        }
                        break;

                    case 2:


                        break;
                }


                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = 100 - elapsedTime;

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(Math.abs(sleepTime));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        } catch (Exception ignored) {

        }
    };


    private void updateCurrentPosition(double[] position, double raggio) {
        double r = raggio / 4;
        r = Math.min(r, 30);
        if (DistToPoint.dist2D(position, lastPosition) > r) {

            lastPosition = position;
            triangleHelper.updatePointRaius(lastPosition, raggio);

        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Point3D_Drill findNearestDrillPoint(double bucketEst, double bucketNord, List<Point3D_Drill> filteredPoints) {

        if (filteredPoints == null || filteredPoints.isEmpty()) {
            return null;
        }

        Point3D_Drill nearestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (Point3D_Drill point : filteredPoints) {

            double distance = new DistToPoint(bucketEst, bucketNord, 0, point.getHeadX(), point.getHeadY(), 0).getDist_to_point();
            if (distance < minDistance) {
                minDistance = distance;
                nearestPoint = point;

            }
        }
        assert nearestPoint != null;
        Log.d("PointStatus", nearestPoint.getStatus() + "");
        if(nearestPoint.getStatus()==null||nearestPoint.getStatus()==0) {
            return nearestPoint;
        }else {
            return null;
        }

    }
}